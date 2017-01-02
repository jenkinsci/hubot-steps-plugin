package org.thoughtslive.jenkins.plugins.hubot.steps;

import java.net.URL;

import javax.inject.Inject;

import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;
import org.jenkinsci.plugins.workflow.support.steps.input.InputStep;
import org.jenkinsci.plugins.workflow.support.steps.input.InputStepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import org.thoughtslive.jenkins.plugins.hubot.api.ResponseData;
import org.thoughtslive.jenkins.plugins.hubot.util.HubotStepExecution;

import hudson.AbortException;
import hudson.EnvVars;
import hudson.Extension;
import hudson.Util;
import hudson.model.TaskListener;

/**
 * Sends an approval message to Hubot.
 */
public class ApproveStep extends BasicHubotStep {

	private static final long serialVersionUID = 602836151349543369L;

	@DataBoundConstructor
	public ApproveStep(final String message, final String room) {
		this.room = room;
		this.message = message;
	}

	@Extension
	public static class DescriptorImpl extends AbstractStepDescriptorImpl {

		public DescriptorImpl() {
			super(HubotApproveStepExecution.class);
		}

		@Override
		public String getFunctionName() {
			return "hubotApprove";
		}

		@Override
		public String getDisplayName() {
			return "Hubot: Send Approval Message";
		}
	}

	public static class HubotApproveStepExecution extends HubotStepExecution<ResponseData<Void>> {

		private static final long serialVersionUID = 7827933215699460957L;

		@Inject
		private transient ApproveStep step;
		@StepContextParameter
		private transient TaskListener listener;
		@StepContextParameter
		private transient EnvVars envVars;

		private InputStepExecution inputExecution = null;

		@Override
		public boolean start() throws Exception {

			final String room = Util.fixEmpty(step.getRoom()) == null ? "#" + envVars.get("HUBOT_DEFAULT_ROOM") : "#" + step.getRoom();
			final URL buildUrl = new URL(envVars.get("BUILD_URL"));

			ResponseData<Void> response = verifyCommon(step, listener, envVars);

			final String message = "Job: " + buildUrl.toString() + "\n\n" + step.getMessage() + "\n" + "    to Proceed reply:  .j proceed " + buildUrl.getPath() + "\n"
					+ "    to Abort reply  :  .j abort " + buildUrl.getPath() + "\n";

			if (response == null) {
				logger.println("Hubot: ROOM - " + room + " - Approval Message - " + step.getMessage());
				response = hubotService.sendMessage(room, message);
			}

			logResponse(response);

			try {
				final InputStep input = new InputStep(step.getMessage());
				input.setId("Proceed");
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
