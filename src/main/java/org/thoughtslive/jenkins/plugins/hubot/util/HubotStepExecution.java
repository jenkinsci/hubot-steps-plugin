package org.thoughtslive.jenkins.plugins.hubot.util;

import static org.thoughtslive.jenkins.plugins.hubot.util.Common.buildErrorResponse;
import static org.thoughtslive.jenkins.plugins.hubot.util.Common.empty;
import static org.thoughtslive.jenkins.plugins.hubot.util.Common.log;

import java.io.PrintStream;

import org.jenkinsci.plugins.workflow.steps.AbstractStepExecutionImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.thoughtslive.jenkins.plugins.hubot.api.ResponseData;
import org.thoughtslive.jenkins.plugins.hubot.service.HubotService;
import org.thoughtslive.jenkins.plugins.hubot.steps.BasicHubotStep;

import hudson.AbortException;
import hudson.EnvVars;
import hudson.Util;
import hudson.model.TaskListener;

/**
 * Common Execution for all Hubot steps.
 * 
 * @see AbstractSynchronousNonBlockingStepExecution
 * @author Naresh Rayapati
 *
 * @param <T>
 *            the type of the return value (may be {@link Void})
 */
public abstract class HubotStepExecution<T> extends AbstractStepExecutionImpl {

	private static final long serialVersionUID = -8253380624161445367L;

	protected transient PrintStream logger = null;
	protected transient String siteName = null;
	protected transient HubotService hubotService = null;
	protected transient boolean failOnError = false;

	// TODO: Duplicate logic between other step execution need to find out a way how we can have a same super class.
	@SuppressWarnings("hiding")
	protected <T> ResponseData<T> verifyCommon(final BasicHubotStep step, final TaskListener listener, final EnvVars envVars) throws AbortException {

		logger = listener.getLogger();
		String errorMessage = null;

		final String url = Util.fixEmpty(step.getUrl()) == null ? envVars.get("HUBOT_URL") : step.getUrl();
		final String room = Util.fixEmpty(step.getRoom()) == null ? envVars.get("HUBOT_DEFAULT_ROOM") : step.getRoom();
		final String message = step.getMessage();

		boolean failOnError = false;
		if (empty(envVars.get("HUBOT_FAIL_ON_ERROR"))) {
			failOnError = step.isFailOnError();
		} else {
			failOnError = Boolean.getBoolean(envVars.get("HUBOT_FAIL_ON_ERROR"));
		}
		if (Util.fixEmpty(url) == null) {
			errorMessage = "Hubot: HUBOT_URL is empty or null.";
		}

		if (Util.fixEmpty(room) == null) {
			errorMessage = "Hubot: Room - empty or null";
		}

		if (Util.fixEmpty(message) == null) {
			errorMessage = "Hubot: Message - empty or null";
		}

		if (errorMessage != null) {
			if (failOnError) {
				throw new AbortException(errorMessage);
			} else {
				return buildErrorResponse(new RuntimeException(errorMessage));
			}
		}

		hubotService = new HubotService(url);
		return null;

	}

	/**
	 * Log code and error message if any.
	 * 
	 * @param response
	 * @return same response back.
	 * @throws AbortException
	 *             if failOnError is true and response is not successful.
	 */
	@SuppressWarnings("hiding")
	protected <T> ResponseData<T> logResponse(ResponseData<T> response) throws AbortException {

		if (response.isSuccessful()) {
			log(logger, "Successful. Code: " + response.getCode());
		} else {
			log(logger, "Error Code: " + response.getCode());
			log(logger, "Error Message: " + response.getError());

			if (failOnError) {
				throw new AbortException(response.getError());
			}
		}

		return response;
	}
}
