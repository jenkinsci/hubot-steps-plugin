package org.thoughtslive.jenkins.plugins.hubot.steps;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import com.google.common.collect.ImmutableSet;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.ListBoxModel;
import hudson.util.ListBoxModel.Option;
import java.io.IOException;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.thoughtslive.jenkins.plugins.hubot.api.Message;
import org.thoughtslive.jenkins.plugins.hubot.api.ResponseData;
import org.thoughtslive.jenkins.plugins.hubot.config.GlobalConfig;
import org.thoughtslive.jenkins.plugins.hubot.config.HubotFolderProperty;
import org.thoughtslive.jenkins.plugins.hubot.config.HubotSite;
import org.thoughtslive.jenkins.plugins.hubot.config.notifications.Type;
import org.thoughtslive.jenkins.plugins.hubot.util.Common;
import org.thoughtslive.jenkins.plugins.hubot.util.Common.STEP;
import org.thoughtslive.jenkins.plugins.hubot.util.HubotAbstractSynchronousNonBlockingStepExecution;

/**
 * Sends a message to hubot
 *
 * @author Naresh Rayapati.
 */
public class SendStep extends BasicHubotStep {

  @Serial
  private static final long serialVersionUID = 5310947910434533239L;

  @DataBoundConstructor
  public SendStep(final String message) {
    this.message = message;
  }

  @Override
  public StepExecution start(StepContext context) throws Exception {
    return new SendStepExecution(this, context);
  }

  @Extension
  public static class DescriptorImpl extends StepDescriptor {

    @Override
    public String getFunctionName() {
      return "hubotSend";
    }

    @NonNull
    @Override
    public String getDisplayName() {
      return "Hubot: Send message";
    }

    @Override
    public Set<? extends Class<?>> getRequiredContext() {
      return ImmutableSet.of(Run.class, TaskListener.class, EnvVars.class);
    }

    public ListBoxModel doFillSiteItems(@AncestorInPath Item project) {
      List<Option> hubotSites = new ArrayList<>();
      hubotSites.add(new Option(
          "Optional - Please select, otherwise it will use default site from parent folder(s)/global.",
          ""));
      String folderName = null;

      // Parent folder(s) sites.
      if (project != null) {
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
      }

      // Query global sites.
      for (HubotSite site : new GlobalConfig().getSites()) {
        hubotSites.add(new Option("Global - " + site.getName(), site.getName()));
      }

      return new ListBoxModel(hubotSites);
    }
  }

  public static class SendStepExecution
      extends HubotAbstractSynchronousNonBlockingStepExecution<Boolean> {

    @Serial
    private static final long serialVersionUID = -7049396675002254309L;

    private final SendStep step;

    protected SendStepExecution(final SendStep step, final StepContext context)
        throws IOException, InterruptedException {
      super(context);
      this.step = step;
    }

    @Override
    protected Boolean run() throws Exception {

      ResponseData<Void> response = verifyCommon(step);
      final String status = step.getStatus() == null ? Type.SUCCESS.name() : step.getStatus();

      if (response == null) {
        if (this.site != null) {
          logger.println(
              "Hubot: Sending " + status + " message to room: " + site.getRoom() + " of site: "
                  + site.getName());
        } else {
          logger.println("Hubot: ROOM - " + room + " - Message - " + step.getMessage());
        }

        FilePath ws = getContext().get(FilePath.class);
        final Map<String, String> tokens = Common.expandMacros(step.getTokens(), run, ws, listener);

        final Message message = Message.builder().message(step.getMessage()).userName(buildUserName)
            .userId(buildUserId).envVars(envVars)
            .buildCause(buildCause)
            .status(status)
            .tokens(tokens)
            .extraData(step.getExtraData()).stepName(STEP.SEND.name())
            .ts(System.currentTimeMillis())
            .build();
        response = hubotService.sendMessage(message);
      }

      return Common.logResponse(response, logger, failOnError).isSuccessful();
    }
  }
}
