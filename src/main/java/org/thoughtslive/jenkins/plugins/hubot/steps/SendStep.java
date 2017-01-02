package org.thoughtslive.jenkins.plugins.hubot.steps;

import java.net.URL;

import javax.inject.Inject;

import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;
import org.kohsuke.stapler.DataBoundConstructor;
import org.thoughtslive.jenkins.plugins.hubot.api.ResponseData;
import org.thoughtslive.jenkins.plugins.hubot.util.HubotAbstractSynchronousNonBlockingStepExecution;

import hudson.EnvVars;
import hudson.Extension;
import hudson.Util;
import hudson.model.TaskListener;

/**
 * Sends a message to hubot
 */
public class SendStep extends BasicHubotStep {
	private static final long serialVersionUID = 2327375640378098562L;

	@DataBoundConstructor
	public SendStep(final String message, final String room) {
		this.room = room;
		this.message = message;
	}

	@Extension
	public static class DescriptorImpl extends AbstractStepDescriptorImpl {

		public DescriptorImpl() {
			super(Execution.class);
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

	public static class Execution extends HubotAbstractSynchronousNonBlockingStepExecution<Boolean> {

		private static final long serialVersionUID = -821037959812310749L;

		@Inject
		private transient SendStep step;

		@StepContextParameter
		private transient TaskListener listener;

		@StepContextParameter
		private transient EnvVars envVars;

		@Override
		protected Boolean run() throws Exception {

			final String room = Util.fixEmpty(step.getRoom()) == null ? "#" + envVars.get("HUBOT_DEFAULT_ROOM") : "#" + step.getRoom();
			final URL buildUrl = new URL(envVars.get("BUILD_URL"));

			ResponseData<Void> response = verifyCommon(step, listener, envVars);

			if (response == null) {
				logger.println("Hubot: ROOM - " + room + " - Message - " + step.getMessage());
				response = hubotService.sendMessage(room, "Job: " + buildUrl.toString() + "\n\n" + step.getMessage());
			}

			return logResponse(response).isSuccessful();
		}
	}
}
