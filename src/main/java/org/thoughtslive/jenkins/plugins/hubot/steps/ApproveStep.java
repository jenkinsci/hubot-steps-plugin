package org.thoughtslive.jenkins.plugins.hubot.steps;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import com.google.common.collect.ImmutableSet;
import hudson.AbortException;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Util;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.ParameterDefinition;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.ListBoxModel;
import hudson.util.ListBoxModel.Option;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.support.steps.input.InputStep;
import org.jenkinsci.plugins.workflow.support.steps.input.InputStepExecution;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.thoughtslive.jenkins.plugins.hubot.api.Message;
import org.thoughtslive.jenkins.plugins.hubot.api.ResponseData;
import org.thoughtslive.jenkins.plugins.hubot.config.GlobalConfig;
import org.thoughtslive.jenkins.plugins.hubot.config.HubotFolderProperty;
import org.thoughtslive.jenkins.plugins.hubot.config.HubotSite;
import org.thoughtslive.jenkins.plugins.hubot.config.notifications.Type;
import org.thoughtslive.jenkins.plugins.hubot.util.Common;
import org.thoughtslive.jenkins.plugins.hubot.util.Common.STEP;
import org.thoughtslive.jenkins.plugins.hubot.util.HubotStepExecution;

/**
 * Sends an approval message to Hubot.
 *
 * @author Naresh Rayapati.
 */
public class ApproveStep extends BasicHubotStep {

  private static final long serialVersionUID = 2750983740619607854L;

  @Getter
  @DataBoundSetter
  private String id;

  @Getter
  @DataBoundSetter
  private String submitter;

  @Getter
  @DataBoundSetter
  private String ok;

  @Getter
  @DataBoundSetter
  private String submitterParameter;

  @Getter
  @DataBoundSetter
  private List<ParameterDefinition> parameters = Collections.emptyList();

  @DataBoundConstructor
  public ApproveStep(final String message) {
    this.message = message;
  }

  @Override
  public StepExecution start(StepContext context) throws Exception {
    return new ApproveStepExecution(this, context);
  }

  @Extension
  public static class DescriptorImpl extends StepDescriptor {

    @Override
    public String getFunctionName() {
      return "hubotApprove";
    }

    @Override
    public String getDisplayName() {
      return "Hubot: Send approval message";
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
      ItemGroup parent = project.getParent();
      while (parent != null) {
        if (parent instanceof AbstractFolder) {
          AbstractFolder folder = (AbstractFolder) parent;
          if (folderName == null) {
            folderName = folder.getName();
          } else {
            folderName = folder.getName() + " » " + folderName;
          }
          HubotFolderProperty jfp = (HubotFolderProperty) folder.getProperties()
              .get(HubotFolderProperty.class);
          if (jfp != null) {
            HubotSite[] sites = jfp.getSites();
            if (sites != null && sites.length > 0) {
              for (HubotSite site : sites) {
                hubotSites.add(new Option(folderName + " - " + site.getName(), site.getName()));
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

      // Query global sites.
      for (HubotSite site : new GlobalConfig().getSites()) {
        hubotSites.add(new Option("Global - " + site.getName(), site.getName()));
      }

      return new ListBoxModel(hubotSites);
    }
  }

  public static class ApproveStepExecution extends HubotStepExecution<ResponseData<Void>> {

    private static final long serialVersionUID = 5535327378092782313L;

    private final ApproveStep step;

    private InputStepExecution inputExecution = null;

    protected ApproveStepExecution(final ApproveStep step, final StepContext context)
        throws IOException, InterruptedException {
      super(context);
      this.step = step;
    }

    @Override
    public boolean start() throws Exception {

      ResponseData<Void> response = verifyCommon(step);
      final String status = step.getStatus() == null ? Type.SUCCESS.name() : step.getStatus();

      if (response == null) {
        if (this.site != null) {
          logger.println(
              "Hubot: Sending " + status + " message to room: " + site.getRoom() + " of site: "
                  + site.getName());
        } else {
          logger.println("Hubot: ROOM - " + room + " - Approval Message - " + step.getMessage());
        }

        FilePath ws = getContext().get(FilePath.class);
        final Map tokens = Common.expandMacros(step.getTokens(), run, ws, listener);
        final String stepId = Util.fixEmpty(step.getId()) == null ? "Proceed" : step.getId().trim();

        final Message message = Message.builder().message(step.getMessage()).userName(buildUserName)
            .userId(buildUserId)
            .buildCause(buildCause)
            .status(status)
            .tokens(tokens)
            .extraData(step.getExtraData())
            .envVars(envVars).stepName(STEP.APPROVE.name()).ts(System.currentTimeMillis())
            .id(stepId)
            .submitter(step.getSubmitter())
            .submitterParameter(step.getSubmitterParameter())
            // TODO - Not Serializable, need to investigate on why.
            //.parameters(step.getParameters())
            .ok(step.getOk())
            .build();

        response = hubotService.sendMessage(message);

      }

      Common.logResponse(response, logger, failOnError);

      try {
        final InputStep input = new InputStep(step.getMessage());
        if(Util.fixEmpty(step.getId()) != null) {
          input.setId(step.getId().trim());
        } else {
          input.setId("Proceed");
        }
        input.setSubmitter(step.getSubmitter());
        input.setOk(step.getOk());
        input.setParameters(step.getParameters());
        input.setSubmitterParameter(step.getSubmitterParameter());

        final InputStepExecution inputExecution = (InputStepExecution) input.start(getContext());
        return inputExecution.start();
      } catch (final Exception e) {
        if (failOnError) {
          throw new AbortException("Error while sending message: " + e.getMessage());
        } else {
          return false;
        }
      }
    }

    @Override
    public void stop(Throwable cause) throws Exception {
      if (inputExecution != null) {
        inputExecution.stop(cause);
      }
    }
  }
}
