package org.thoughtslive.jenkins.plugins.hubot.util;

import static org.thoughtslive.jenkins.plugins.hubot.util.Common.buildErrorResponse;
import static org.thoughtslive.jenkins.plugins.hubot.util.Common.log;

import java.io.PrintStream;
import java.util.List;

import org.jenkinsci.plugins.workflow.steps.AbstractStepExecutionImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.thoughtslive.jenkins.plugins.hubot.api.ResponseData;
import org.thoughtslive.jenkins.plugins.hubot.service.HubotService;
import org.thoughtslive.jenkins.plugins.hubot.steps.BasicHubotStep;

import com.google.common.annotations.VisibleForTesting;

import hudson.AbortException;
import hudson.EnvVars;
import hudson.Util;
import hudson.model.Cause;
import hudson.model.TaskListener;
import hudson.model.Cause.UpstreamCause;
import hudson.model.Cause.UserIdCause;

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
		final String failOnErrorStr = Util.fixEmpty(envVars.get("HUBOT_FAIL_ON_ERROR"));
		
		if (failOnErrorStr == null) {
			failOnError = step.isFailOnError();
		} else {
			failOnError = Boolean.parseBoolean(failOnErrorStr);
		}
		
		if (Util.fixEmpty(url) == null) {
			errorMessage = "Hubot: HUBOT_URL is empty or null.";
		}

		if (Util.fixEmpty(room) == null) {
			errorMessage = "Hubot: Room is empty or null.";
		}

		if (Util.fixEmpty(message) == null) {
			errorMessage = "Hubot: Message is empty or null.";
		}

		if (errorMessage != null) {
			return buildErrorResponse(new RuntimeException(errorMessage));
		}

		hubotService = getHubotService(url);
		return null;

	}

	@VisibleForTesting
	public HubotService getHubotService(final String url) {
		return new HubotService(url);
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

	/**
	 * Return the current build user.
	 * 
	 * @param causes build causes.
	 * @return user name.
	 */
	protected static String prepareBuildUser(List<Cause> causes) {
		String buildUser = "anonymous";
        if(causes != null && causes.size() > 0) {
	        if(causes.get(0) instanceof UserIdCause){
	        	buildUser =  ((UserIdCause)causes.get(0)).getUserName();
	        } else if(causes.get(0) instanceof UpstreamCause) {
	        	List<Cause> upstreamCauses = ((UpstreamCause)causes.get(0)).getUpstreamCauses();
	        	prepareBuildUser(upstreamCauses);
	        } 
        }
        return buildUser;
	}
}
