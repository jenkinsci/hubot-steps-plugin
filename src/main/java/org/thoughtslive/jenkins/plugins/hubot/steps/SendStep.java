package org.thoughtslive.jenkins.plugins.hubot.steps;

import com.google.common.collect.ImmutableSet;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import java.io.IOException;
import java.util.Set;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import org.thoughtslive.jenkins.plugins.hubot.api.Message;
import org.thoughtslive.jenkins.plugins.hubot.api.ResponseData;
import org.thoughtslive.jenkins.plugins.hubot.util.Common.STEP;
import org.thoughtslive.jenkins.plugins.hubot.util.HubotAbstractSynchronousNonBlockingStepExecution;

/**
 * Sends a message to hubot
 *
 * @author Naresh Rayapati.
 */
public class SendStep extends BasicHubotStep {

  private static final long serialVersionUID = 2327375640378098562L;

  @DataBoundConstructor
  public SendStep(final String room, final String message) {
    this.room = room;
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

    @Override
    public String getDisplayName() {
      return "Hubot: Send message";
    }

    @Override
    public Set<? extends Class<?>> getRequiredContext() {
      return ImmutableSet.of(Run.class, TaskListener.class, EnvVars.class);
    }
  }

  public static class SendStepExecution
      extends HubotAbstractSynchronousNonBlockingStepExecution<Boolean> {

    private static final long serialVersionUID = -821037959812310749L;

    private final SendStep step;

    protected SendStepExecution(final SendStep step, final StepContext context)
        throws IOException, InterruptedException {
      super(context);
      this.step = step;
    }

    @Override
    protected Boolean run() throws Exception {

      ResponseData<Void> response = verifyCommon(step);

      if (response == null) {
        logger.println("Hubot: ROOM - " + room + " - Message - " + step.getMessage());
        final Message message = Message.builder().message(step.getMessage()).userName(buildUserName)
            .userId(buildUserId).envVars(envVars).status(step.getStatus())
            .extraData(step.getExtraData()).stepName(STEP.SEND.name())
            .ts(System.currentTimeMillis() / 1000)
            .build();
        response = hubotService.sendMessage(message);
      }

      return logResponse(response).isSuccessful();
    }
  }
}
