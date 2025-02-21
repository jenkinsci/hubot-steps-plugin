package org.thoughtslive.jenkins.plugins.hubot.config;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.Util;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Job;
import hudson.model.TaskListener;
import hudson.model.User;
import hudson.util.FormValidation;

import java.io.Serial;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jenkins.model.Jenkins;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.log4j.Logger;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.thoughtslive.jenkins.plugins.hubot.api.Message;
import org.thoughtslive.jenkins.plugins.hubot.api.ResponseData;
import org.thoughtslive.jenkins.plugins.hubot.config.notifications.Config;
import org.thoughtslive.jenkins.plugins.hubot.config.notifications.Type;
import org.thoughtslive.jenkins.plugins.hubot.service.HubotService;
import org.thoughtslive.jenkins.plugins.hubot.util.Common;

/**
 * Represents a configuration needed to connect to Hubot.
 *
 * @author Naresh Rayapati
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class HubotSite extends AbstractDescribableImpl<HubotSite> implements Serializable,
    Cloneable {

  @Serial
  private static final long serialVersionUID = -455439000126041809L;

  private static final Logger LOGGER = Logger.getLogger(HubotSite.class.getName());

  private boolean defaultSite;

  private String name;

  private URL url;

  private String room;

  private String roomPrefix;

  private boolean failOnError;

  private boolean useFolderName;

  private List<Config> notifications;

  @DataBoundConstructor
  public HubotSite(final boolean defaultSite, final String name, final URL url, final String room,
      final String roomPrefix,
      final boolean failOnError,
      final boolean useFolderName, final List<Config> notifications) {

    this.defaultSite = defaultSite;
    this.name = Util.fixEmpty(name);
    this.url = url;
    this.room = room;
    this.roomPrefix = roomPrefix;
    this.failOnError = failOnError;
    this.useFolderName = useFolderName;
    this.notifications = notifications;
  }

  /**
   * Gets the effective {@link HubotSite} associated with the given project.
   *
   * @return null if no such was found.
   */
  public static HubotSite get(Job<?, ?> job, final TaskListener listener) {
    HubotJobProperty jpp = job.getProperty(HubotJobProperty.class);
    boolean enableNotifications = false;
    String siteName = null;

    if (jpp != null) {
      enableNotifications = jpp.isEnableNotifications();
      siteName = Util.fixEmpty(jpp.getSiteNames());
    }

    if (enableNotifications) {
      return get(job, listener, siteName);
    }

    return null;
  }

  public static HubotSite get(final Job<?, ?> job, final TaskListener listener,
      final String siteName) {
    HubotSite hubotSite = null;
    HubotSite defaultSite = null;
    ItemGroup parent = job.getParent();
    String folderName = null;

    // Site from folder(s).
    try {
      while (parent != null) {
        if (parent instanceof AbstractFolder folder) {
            if (folderName == null) {
            folderName = folder.getName();
          }
          HubotFolderProperty jfp = (HubotFolderProperty) folder.getProperties()
              .get(HubotFolderProperty.class);
          if (jfp != null) {
            HubotSite[] sites = jfp.getSites();
            if (sites != null) {
              for (HubotSite site : sites) {
                HubotSite cloneSite = site.clone();
                if (cloneSite.isUseFolderName()) {
                  cloneSite.setRoom(folder.getName());
                }
                if (siteName != null) {
                  if (cloneSite.getName().equalsIgnoreCase(siteName) && hubotSite == null) {
                    hubotSite = cloneSite;
                  }
                } else {
                  if (cloneSite.isDefaultSite() && defaultSite == null) {
                    defaultSite = cloneSite;
                  }
                }
              }
            }
          }
        }

        if (parent instanceof Item item) {
          parent = item.getParent();
        } else {
          parent = null;
        }
      }
      // Global Sites.
      if (hubotSite == null && defaultSite == null) {
        for (HubotSite site : new GlobalConfig().getSites()) {
          HubotSite cloneSite = site.clone();
          if (site.isUseFolderName()) {
            if (folderName != null) {
              cloneSite.setRoom(folderName);
            }
          }
          if (siteName != null) {
            if (cloneSite.getName().equalsIgnoreCase(siteName) && hubotSite == null) {
              hubotSite = cloneSite;
            }
          } else {
            if (cloneSite.isDefaultSite() && defaultSite == null) {
              defaultSite = cloneSite;
            }
          }
        }
      }
    } catch (Exception e) {
      LOGGER.error("Unable to get hubot site", e);
    }

    return hubotSite == null ? defaultSite : hubotSite;
  }

  @Override
  public HubotSite clone() throws CloneNotSupportedException {
    super.clone();
    return HubotSite.builder().defaultSite(this.defaultSite).name(this.name).url(this.url)
        .room(this.room).roomPrefix(this.roomPrefix).failOnError(this.failOnError)
        .useFolderName(this.useFolderName).notifications(this.notifications).build();
  }

  @Extension
  public static class DescriptorImpl extends Descriptor<HubotSite> {

    @NonNull
    @Override
    public String getDisplayName() {
      return "Hubot Site";
    }

    /**
     * Checks if the site is valid and send a test message.
     */
    @SuppressFBWarnings
    public FormValidation doValidate(@AncestorInPath Item project, @QueryParameter String name,
        @QueryParameter String url,
        @QueryParameter String room,
        @QueryParameter String roomPrefix,
        @QueryParameter boolean useFolderName) {
      url = Util.fixEmpty(url);
      name = Util.fixEmpty(name);
      room = Util.fixEmpty(room);
      roomPrefix = Util.fixEmpty(roomPrefix);

      if (roomPrefix == null) {
        roomPrefix = "";
      }

      if (name == null) {
        return FormValidation.error("Site name is empty or null.");
      }

      String folderName;
      String folderUrl;
      Map<String, String> extraData = new HashMap<>();
      extraData.put("JENKINS_URL", Jenkins.get().getRootUrl());

      if (project instanceof AbstractFolder folder) {
          folderName = folder.getName();
        folderUrl = folder.getAbsoluteUrl();
        extraData.put("FOLDER_URL", folderUrl);

        // Parent folder(s) sites.
        ItemGroup parent = project.getParent();
        while (parent != null) {
          if (parent instanceof AbstractFolder parentFolder) {
              folderName = parentFolder.getName() + " » " + folderName;
          }

          if (parent instanceof Item item) {
            parent = item.getParent();
          } else {
            parent = null;
          }
        }
        extraData.put("FOLDER_NAME", folderName);
      }

      if (useFolderName) {
        if (project instanceof AbstractFolder folder) {
            room = roomPrefix + folder.getName();
        } else {
          if (room == null) {
            return FormValidation
                .error("Room Name is empty or null, is required for global config.");
          }
        }
      } else {
        if (room == null) {
          return FormValidation.error("Room Name is empty or null.");
        }
      }

      try {
        if (url == null) {
          return FormValidation.error("URL is empty or null.");
        }
        new URL(Common.sanitizeURL(url));
      } catch (MalformedURLException e) {
        return FormValidation.error(String.format("Malformed URL (%s)", url), e);
      }

      String userName = "anonymous";
      String userId = null;

      if (User.current() != null) {
        userName = User.current().getDisplayName();
        userId = User.current().getId();
      }

      try {
        final Message message = Message.builder().stepName(Common.STEP.TEST.name())
            .message("Hubot Site: " + name + " configured successfully.")
            .ts(System.currentTimeMillis()).extraData(extraData)
            .userId(userId).userName(userName).status(Type.SUCCESS.name()).build();
        HubotSite site = HubotSite.builder().room(room).url(new URL(Common.sanitizeURL(url)))
            .build();
        HubotService service = new HubotService(site);
        ResponseData<Void> response = service.sendMessage(message);
        if (!response.isSuccessful()) {
          return FormValidation.error(
              "Hubot: Error while sending a test message - Error Code: " + response.getCode()
                  + " Error Message: " + response.getError());
        }
        return FormValidation
            .ok("Success  - Please check hubot logs as well to make sure there isn't a problem with script!.");
      } catch (Exception e) {
        e.printStackTrace();
        return FormValidation.error(String.format("Error while configuring site: %s", name), e);
      }
    }
  }
}