package org.thoughtslive.jenkins.plugins.hubot.util;

import static org.thoughtslive.jenkins.plugins.hubot.util.Common.buildErrorResponse;
import static org.thoughtslive.jenkins.plugins.hubot.util.Common.log;

import com.google.common.annotations.VisibleForTesting;
import hudson.AbortException;
import hudson.EnvVars;
import hudson.Util;
import hudson.model.Run;
import hudson.model.TaskListener;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.thoughtslive.jenkins.plugins.hubot.api.ResponseData;
import org.thoughtslive.jenkins.plugins.hubot.config.HubotSite;
import org.thoughtslive.jenkins.plugins.hubot.service.HubotService;
import org.thoughtslive.jenkins.plugins.hubot.steps.BasicHubotStep;

/**
 * Common Execution for all Hubot steps.
 *
 * @param <T> the type of the return value (may be {@link Void})
 * @author Naresh Rayapati
 * @see StepExecution
 */
public abstract class HubotStepExecution<T> extends StepExecution {

  private static final long serialVersionUID = -8253380624161445367L;
  protected transient PrintStream logger = null;
  protected transient String siteName = null;
  protected transient HubotService hubotService = null;
  protected transient boolean failOnError = false;
  protected transient String room = null;
  protected transient String buildUserName = null;
  protected transient String buildUserId = null;
  protected transient EnvVars envVars;
  private transient Run<?, ?> run;
  private transient TaskListener listener;

  protected HubotStepExecution(StepContext context) throws IOException, InterruptedException {
    super(context);
    run = context.get(Run.class);
    listener = context.get(TaskListener.class);
    envVars = context.get(EnvVars.class);
  }

  // TODO: Duplicate logic between other step execution need to find out a way how we can have a
  // same super class.
  @SuppressWarnings("hiding")
  protected <T> ResponseData<T> verifyCommon(final BasicHubotStep step) {

    logger = listener.getLogger();
    String errorMessage = null;
    URL mainURL = null;

    final String url =
        Util.fixEmpty(step.getUrl()) == null ? envVars.get("HUBOT_URL") : step.getUrl();
    room =
        Util.fixEmpty(step.getRoom()) == null ? envVars.get("HUBOT_DEFAULT_ROOM") : step.getRoom();
    final String message = step.getMessage();
    final String failOnErrorStr = Util.fixEmpty(envVars.get("HUBOT_FAIL_ON_ERROR"));

    if (failOnErrorStr == null) {
      failOnError = step.isFailOnError();
    } else {
      failOnError = Boolean.parseBoolean(failOnErrorStr);
    }

    if (Util.fixEmpty(url) == null) {
      errorMessage = "Hubot: HUBOT_URL is empty or null.";
    } else {
      try {
        mainURL = new URL(url);
      } catch (MalformedURLException e) {
        errorMessage = "Hubot: Malformed HUBOT_URL.";
      }
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

    setHubotService(mainURL, room);

    buildUserName = Common.prepareBuildUserName(run.getCauses(), envVars);
    buildUserId = Common.prepareBuildUserId(run.getCauses(), envVars);
    return null;

  }

  @VisibleForTesting
  public void setHubotService(final HubotService service) {
    this.hubotService = service;
  }

  private void setHubotService(final URL url, final String room) {
    final HubotSite site = HubotSite.builder().url(url).room(room).build();
    if (this.hubotService == null) {
      this.hubotService = new HubotService(site);
    }
  }

  /**
   * Log code and error message if any.
   *
   * @return same response back.
   * @throws AbortException if failOnError is true and response is not successful.
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
