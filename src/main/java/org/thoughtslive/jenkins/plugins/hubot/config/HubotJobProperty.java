package org.thoughtslive.jenkins.plugins.hubot.config;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.ExtensionPoint;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Job;
import hudson.model.JobProperty;
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
@Getter
@Builder
public class HubotJobProperty extends JobProperty implements ExtensionPoint {

  private static final Logger LOGGER = Logger.getLogger(HubotJobProperty.class.getName());

  // TODO - Update to select multiple sites for now I didn't find a way to multiselect in Jenkins.
  public final String siteNames;

  private boolean enableNotifications;

  @DataBoundConstructor
  public HubotJobProperty(String siteNames, boolean enableNotifications) {
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
    public boolean isApplicable(Class<? extends Job> jobType) {
      return Job.class.isAssignableFrom(jobType);
    }

    @NonNull
    @Override
    public String getDisplayName() {
      return "Hubot Job Property";
    }

    public ListBoxModel doFillSiteNamesItems(@AncestorInPath Item project) {
      List<Option> hubotSites = new ArrayList<>();
      hubotSites.add(new Option(
          "Optional - Please select, otherwise it will use default site from parent folder(s)/global.",
          ""));
      String folderName = null;

      // Parent folder(s) sites.
      ItemGroup parent = project.getParent();
      while (parent != null) {
        if (parent instanceof AbstractFolder folder) {
            if (folderName == null) {
            folderName = folder.getName();
          } else {
            folderName = folder.getName() + " » " + folderName;
          }
          HubotFolderProperty jfp = (HubotFolderProperty) folder.getProperties()
              .get(HubotFolderProperty.class);
          if (jfp != null) {
            HubotSite[] sites = jfp.getSites();
            if (sites != null) {
              for (HubotSite site : sites) {
                hubotSites.add(new Option(folderName + " - " + site.getName(), site.getName()));
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

      // Query global sites.
      for (HubotSite site : new GlobalConfig().getSites()) {
        hubotSites.add(new Option("Global - " + site.getName(), site.getName()));
      }

      return new ListBoxModel(hubotSites);
    }
  }
}