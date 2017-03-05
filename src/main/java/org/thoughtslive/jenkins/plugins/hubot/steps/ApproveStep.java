package org.thoughtslive.jenkins.plugins.hubot.steps;

import java.io.IOException;
import java.net.URL;
import java.util.Set;

import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.support.steps.input.InputStep;
import org.jenkinsci.plugins.workflow.support.steps.input.InputStepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import org.thoughtslive.jenkins.plugins.hubot.api.ResponseData;
import org.thoughtslive.jenkins.plugins.hubot.util.HubotStepExecution;

import com.google.common.collect.ImmutableSet;

import hudson.AbortException;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;

/**
 * Sends an approval message to Hubot.
 */
public class ApproveStep extends BasicHubotStep {

  private static final long serialVersionUID = 602836151349543369L;

  @DataBoundConstructor
  public ApproveStep(final String room, final String message) {
    this.room = room;
    this.message = message;
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
  }

  public static class ApproveStepExecution extends HubotStepExecution<ResponseData<Void>> {

    private static final long serialVersionUID = 7827933215699460957L;

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

      if (response == null) {

        final URL url = new URL(buildUrl);

        final String message = step.getMessage() + "\n" + "\tto Proceed reply:  .j proceed "
            + url.getPath() + "\n" + "\tto Abort reply  :  .j abort " + url.getPath() + "\n\n"
            + "Job: " + buildUrl.toString() + "\n" + "User: " + buildUser;

        logger.println("Hubot: ROOM - " + room + " - Approval Message - " + step.getMessage());
        response = hubotService.sendMessage(room, message);
      }

      logResponse(response);

      try {
        final InputStep input = new InputStep(step.getMessage());
        input.setId("Proceed");
        // Until input step is being uplifted to 2.5.
        final Step step = input;
        final InputStepExecution inputExecution = (InputStepExecution) step.start(getContext());
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

  @Override
  public StepExecution start(StepContext context) throws Exception {
    return new ApproveStepExecution(this, context);
  }
}
