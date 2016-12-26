package org.thoughtslive.jenkins.plugins.hubot.util;

import java.io.PrintStream;

import org.thoughtslive.jenkins.plugins.hubot.steps.BasicStep;

import hudson.AbortException;
import hudson.EnvVars;
import hudson.Util;
import hudson.model.TaskListener;

public class Common {

	public static String sanitizeURL(String url) {
		if (!url.endsWith("/")) {
			url = url + "/";
		}
		return url;
	}

	public static void log(TaskListener listener, Object message) {
		if (listener != null) {
			PrintStream logger = listener.getLogger();
			if (logger != null) {
				logger.println(message);
			}
		}
	}

	public static boolean verifyCommon(final EnvVars envVars, final BasicStep step, final TaskListener listener) throws AbortException {

		boolean failOnError = false;
		String errorMessage = null;
		final String url = Util.fixEmpty(step.getUrl()) == null ? envVars.get("HUBOT_URL") : step.getUrl();
		final String room = Util.fixEmpty(step.getRoom()) == null ? envVars.get("HUBOT_DEFAULT_ROOM") : step.getRoom();
		final String message = step.getMessage();

		if (Util.fixEmpty(envVars.get("HUBOT_FAIL_ON_ERROR")) == null) {
			failOnError = step.isFailOnError();
		} else {
			failOnError = Boolean.getBoolean(envVars.get("HUBOT_FAIL_ON_ERROR"));
		}

		if (Util.fixEmpty(url) == null) {
			errorMessage = "Hubot: HUBOT_URL is empty or null.";
		}

		if (Util.fixEmpty(room) == null)  {
			errorMessage = "Hubot: Room - empty or null";
		}

		if (Util.fixEmpty(message) == null) {
			errorMessage = "Hubot: Message - empty or null";
		}

		if (errorMessage != null) {
			log(listener, errorMessage);
			if (failOnError) {
				throw new AbortException(errorMessage);
			} else {
				return false;
			}
		}

		return true;
	}
}