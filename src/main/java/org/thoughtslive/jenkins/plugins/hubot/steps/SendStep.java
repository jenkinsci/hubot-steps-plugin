package org.thoughtslive.jenkins.plugins.hubot.steps;

import javax.inject.Inject;

import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;
import org.kohsuke.stapler.DataBoundConstructor;
import org.thoughtslive.jenkins.plugins.hubot.api.ResponseData;
import org.thoughtslive.jenkins.plugins.hubot.util.HubotAbstractSynchronousNonBlockingStepExecution;

import hudson.EnvVars;
import hudson.Extension;
import hudson.Util;
import hudson.model.Run;
import hudson.model.TaskListener;

/**
 * Sends a message to hubot
 */
public class SendStep extends BasicHubotStep {
  private static final long serialVersionUID = 2327375640378098562L;

  @DataBoundConstructor
  public SendStep(final String room, final String message) {
    this.room = room;
    this.message = message;
  }

  @Extension
  public static class DescriptorImpl extends AbstractStepDescriptorImpl {

    public DescriptorImpl() {
      super(SendStepExecution.class);
    }

    @Override
    public String getFunctionName() {
      return "hubotSend";
    }

    @Override
    public String getDisplayName() {
      return "Hubot: Send message";
    }
  }

  public static class SendStepExecution
      extends HubotAbstractSynchronousNonBlockingStepExecution<Boolean> {

    private static final long serialVersionUID = -821037959812310749L;

    @Inject
    transient SendStep step;

    @StepContextParameter
    transient Run run;

    @StepContextParameter
    transient TaskListener listener;

    @StepContextParameter
    transient EnvVars envVars;

    @Override
    protected Boolean run() throws Exception {

      final String room = Util.fixEmpty(step.getRoom()) == null ? envVars.get("HUBOT_DEFAULT_ROOM")
          : step.getRoom();
      final String buildUrl = envVars.get("BUILD_URL");
      final String buildUser = prepareBuildUser(run.getCauses());

      ResponseData<Void> response = verifyCommon(step, listener, envVars);

      if (response == null) {
        logger.println("Hubot: ROOM - " + room + " - Message - " + step.getMessage());
        response = hubotService.sendMessage(room, step.getMessage() + "\n\n" + "Job: "
            + buildUrl.toString() + "\n" + "User: " + buildUser);
      }

      return logResponse(response).isSuccessful();
    }
  }
}
