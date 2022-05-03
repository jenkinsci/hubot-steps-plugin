package org.thoughtslive.jenkins.plugins.hubot.config;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import com.cloudbees.hudson.plugins.folder.AbstractFolderProperty;
import com.cloudbees.hudson.plugins.folder.AbstractFolderPropertyDescriptor;
import hudson.Extension;
import hudson.util.CopyOnWriteList;
import edu.umd.cs.findbugs.annotations.NonNull;
import lombok.Getter;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Provides folder level Hubot configuration.
 *
 * @author Naresh Rayapati
 */
public class HubotFolderProperty extends AbstractFolderProperty<AbstractFolder<?>> {

  @Getter
  private final CopyOnWriteList<HubotSite> sites = new CopyOnWriteList<>();

  @DataBoundConstructor
  public HubotFolderProperty() {
  }

  @Override
  public AbstractFolderProperty<?> reconfigure(StaplerRequest req, JSONObject formData) {
    if (formData == null) {
      return null;
    }
    Stapler.CONVERT_UTILS.deregister(java.net.URL.class);
    Stapler.CONVERT_UTILS.register(new EmptyFriendlyURLConverter(), java.net.URL.class);

    sites.replaceBy(req.bindJSONToList(HubotSite.class, formData.get("sites")));
    return this;
  }

  /**
   * Return the Hubot sites.
   *
   * @return the Hubot sites
   */
  public HubotSite[] getSites() {
    return sites.toArray(new HubotSite[0]);
  }

  /**
   * Adds a Hubot site.
   *
   * @param site the JIRA site
   */
  @DataBoundSetter
  public void setSites(HubotSite site) {
    sites.add(site);
  }

  /**
   * Descriptor class.
   */
  @Extension
  public static class DescriptorImpl extends AbstractFolderPropertyDescriptor {

    @NonNull
    @Override
    public String getDisplayName() {
      return "Hubot Folder Property";
    }
  }
}