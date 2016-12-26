package org.thoughtslive.jenkins.plugins.hubot.steps;

import static org.thoughtslive.jenkins.plugins.hubot.util.Common.log;
import static org.thoughtslive.jenkins.plugins.hubot.util.Common.verifyCommon;

import java.net.URL;

import javax.inject.Inject;

import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;
import org.kohsuke.stapler.DataBoundConstructor;
import org.thoughtslive.jenkins.plugins.hubot.service.HubotService;

import hudson.AbortException;
import hudson.EnvVars;
import hudson.Extension;
import hudson.Util;
import hudson.model.TaskListener;

/**
 * Sends a message to hubot
 */
public class SendStep extends BasicStep {
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
			return "Hubot: Send a message";
		}
	}

	public static class Execution extends AbstractSynchronousNonBlockingStepExecution<Boolean> {

		private static final long serialVersionUID = -821037959812310749L;

		@Inject
		private transient SendStep step;

		@StepContextParameter
		private transient TaskListener listener;

		@StepContextParameter
		private transient EnvVars envVars;

		@Override
		protected Boolean run() throws Exception {

			final String url = Util.fixEmpty(step.getUrl()) == null ? envVars.get("HUBOT_URL") : step.getUrl();
			final String room = Util.fixEmpty(step.getRoom()) == null ? "#" + envVars.get("HUBOT_DEFAULT_ROOM") : "#" + step.getRoom();
			final URL buildUrl = new URL(envVars.get("BUILD_URL"));

			if (!verifyCommon(envVars, step, listener)) {
				return false;
			}
			try {
				log(listener, "Hubot: ROOM - " + room + " - Message - " + step.getMessage());
				final HubotService hubotService = new HubotService(url);
				hubotService.sendMessage(room, "Job: " + buildUrl.toString() + "\n\n" + step.getMessage());
				return true;
			} catch (final Exception e) {
				throw new AbortException("Error while sending message: "+ e.getMessage());
			}
		}
	}
}
