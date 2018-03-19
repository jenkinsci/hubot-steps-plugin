package org.thoughtslive.jenkins.plugins.hubot.config;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import hudson.Extension;
import hudson.ExtensionPoint;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Job;
import hudson.model.JobPropertyDescriptor;
import hudson.util.ListBoxModel;
import hudson.util.ListBoxModel.Option;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import lombok.Builder;
import lombok.Getter;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Associates {@link Job} with {@link HubotSite}.
 *
 * @author Naresh Rayapati
 */
@Builder
public class JobProperty extends hudson.model.JobProperty implements ExtensionPoint {

  private static final Logger LOGGER = Logger.getLogger(JobProperty.class.getName());

  // TODO - Update to select multiple sites for now I didn't find a way to multiselect in Jenkins.
  @Getter
  public final String siteNames;

  @Getter
  private boolean enableNotifications;

  @DataBoundConstructor
  public JobProperty(String siteNames, boolean enableNotifications) {
    this.siteNames = siteNames;
    this.enableNotifications = enableNotifications;
  }

  @Override
  public DescriptorImpl getDescriptor() {
    return (DescriptorImpl) super.getDescriptor();
  }

  @Extension
  public static class DescriptorImpl extends JobPropertyDescriptor {

    @Override
    @SuppressWarnings("unchecked")
    public boolean isApplicable(Class<? extends Job> jobType) {
      return Job.class.isAssignableFrom(jobType);
    }

    @Override
    public String getDisplayName() {
      return "Hubot Job Property";
    }

    public ListBoxModel doFillSiteNamesItems(@AncestorInPath Item project) {
      List<Option> hubotSites = new ArrayList<>();
      hubotSites.add(new Option(
          "Optional - Please select, otherwise it will use default site from parent folder(s)/global.",
          ""));

      ItemGroup parent = project.getParent();
      while (parent != null) {
        if (parent instanceof AbstractFolder) {
          AbstractFolder folder = (AbstractFolder) parent;
          FolderProperty jfp = (FolderProperty) folder.getProperties()
              .get(FolderProperty.class);
          if (jfp != null) {
            HubotSite[] sites = jfp.getSites();
            if (sites != null && sites.length > 0) {
              for (HubotSite site : sites) {
                hubotSites.add(new Option(site.getName(), site.getName()));
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

      for (HubotSite site : new GlobalConfig().getSites()) {
        hubotSites.add(new Option(site.getName(), site.getName()));
      }

      return new ListBoxModel(hubotSites);
    }
  }
}