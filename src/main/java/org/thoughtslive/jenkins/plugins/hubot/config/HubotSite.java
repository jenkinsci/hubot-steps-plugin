package org.thoughtslive.jenkins.plugins.hubot.config;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.apache.log4j.Logger;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.thoughtslive.jenkins.plugins.hubot.api.Message;
import org.thoughtslive.jenkins.plugins.hubot.api.ResponseData;
import org.thoughtslive.jenkins.plugins.hubot.config.notifications.Config;
import org.thoughtslive.jenkins.plugins.hubot.service.HubotService;
import org.thoughtslive.jenkins.plugins.hubot.util.Common;
import org.thoughtslive.jenkins.plugins.hubot.util.Common.STATUS;

/**
 * Represents a configuration needed to connect to Hubot.
 *
 * @author Naresh Rayapati
 */
@Builder
public class HubotSite extends AbstractDescribableImpl<HubotSite> {

  private static final Logger LOGGER = Logger.getLogger(HubotSite.class.getName());

  @Getter
  private final boolean defaultSite;

  @Getter
  private final String name;

  @Getter
  private final URL url;

  @Getter
  private final String room;

  @Getter
  private final boolean failOnError;

  @Getter
  private final boolean useFolderName;

  @Getter
  private final List<Config> notifications;

  @DataBoundConstructor
  public HubotSite(final boolean defaultSite, final String name, final URL url, final String room,
      final boolean failOnError,
      final boolean useFolderName, final List<Config> notifications) {

    this.defaultSite = defaultSite;
    this.name = Util.fixEmpty(name);
    this.url = url;
    this.room = room;
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
    JobProperty jpp = job.getProperty(JobProperty.class);
    boolean enableNotifications = false;
    String siteName = null;

    if (jpp != null) {
      enableNotifications = jpp.isEnableNotifications();
      siteName = Util.fixEmpty(jpp.getSiteNames());
    }

    if (enableNotifications) {
      return get(job, siteName, listener);
    }

    return null;
  }

  public static HubotSite get(Job<?, ?> job, String siteName, final TaskListener listener) {
    HubotSite hubotSite = null;
    HubotSite defaultSite = null;
    ItemGroup parent = job.getParent();
    while (parent != null) {
      if (parent instanceof AbstractFolder) {
        AbstractFolder folder = (AbstractFolder) parent;
        FolderProperty jfp = (FolderProperty) folder.getProperties()
            .get(FolderProperty.class);
        if (jfp != null) {
          HubotSite[] sites = jfp.getSites();
          if (sites != null && sites.length > 0) {
            for (HubotSite site : sites) {
              if (siteName != null) {
                if (site.getName().equalsIgnoreCase(siteName)) {
                  hubotSite = site;
                }
              } else {
                if (site.isDefaultSite()) {
                  defaultSite = site;
                }
              }
            }
          }
        }
      }

      if (parent instanceof Item) {
        parent = ((Item) parent).getParent();
      } else {
        parent = null;
      }
    }

    // Global Sites.
    if (hubotSite == null && defaultSite == null) {
      for (HubotSite site : new GlobalConfig().getSites()) {
        if (siteName != null) {
          if (site.getName().equalsIgnoreCase(siteName)) {
            hubotSite = site;
          }
        } else {
          if (site.isDefaultSite()) {
            defaultSite = site;
          }
        }
      }
    }

    return hubotSite == null ? defaultSite : hubotSite;
  }

  @Extension
  public static class DescriptorImpl extends Descriptor<HubotSite> {

    @Override
    public String getDisplayName() {
      return "Hubot Site";
    }

    /**
     * Checks if the site is valid and send a test message.
     */
    public FormValidation doValidate(@AncestorInPath Item project, @QueryParameter String name,
        @QueryParameter String url,
        @QueryParameter String room,
        @QueryParameter boolean failOnError,
        @QueryParameter boolean useFolderName)
        throws IOException {
      url = Util.fixEmpty(url);
      name = Util.fixEmpty(name);
      room = Util.fixEmpty(room);

      if (name == null) {
        return FormValidation.error("Site name is empty or null.");
      }

      if (room == null) {
        return FormValidation.error("Room Name is empty or null.");
      }

      try {
        if (url == null) {
          return FormValidation.error("URL is empty or null.");
        }
        new URL(url);
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
            .message("Site: " + name + " configured successfully.")
            .ts(System.currentTimeMillis() / 1000)
            .userId(userId).userName(userName).status(STATUS.INFO.name()).build();
        HubotSite site = HubotSite.builder().room(room).url(new URL(url)).build();
        HubotService service = new HubotService(site);
        ResponseData response = service.sendMessage(message);
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