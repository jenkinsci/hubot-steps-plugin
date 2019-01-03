package org.thoughtslive.jenkins.plugins.hubot.util;

import static org.thoughtslive.jenkins.plugins.hubot.util.Common.buildErrorResponse;

import com.google.common.annotations.VisibleForTesting;
import hudson.EnvVars;
import hudson.Util;
import hudson.model.Run;
import hudson.model.TaskListener;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;
import org.thoughtslive.jenkins.plugins.hubot.api.ResponseData;
import org.thoughtslive.jenkins.plugins.hubot.config.HubotSite;
import org.thoughtslive.jenkins.plugins.hubot.service.HubotService;
import org.thoughtslive.jenkins.plugins.hubot.steps.BasicHubotStep;

/**
 * Common Execution for all Hubot steps.
 *
 * @param <T> the type of the return value (may be {@link Void})
 * @author Naresh Rayapati
 * @see SynchronousNonBlockingStepExecution
 */
public abstract class HubotAbstractSynchronousNonBlockingStepExecution<T>
    extends SynchronousNonBlockingStepExecution<T> {

  private static final long serialVersionUID = -6557391471762434587L;

  protected transient PrintStream logger = null;
  protected transient HubotService hubotService = null;
  protected transient boolean failOnError = true;
  protected transient String room = null;
  protected transient String buildUserName = null;
  protected transient String buildCause = null;
  protected transient String buildUserId = null;
  protected transient EnvVars envVars;
  protected transient HubotSite site = null;
  protected transient Run<?, ?> run;
  protected transient TaskListener listener;
  private String failOnErrorStr = null;
  private String url = null;

  protected HubotAbstractSynchronousNonBlockingStepExecution(StepContext context)
      throws IOException, InterruptedException {
    super(context);
    run = context.get(Run.class);
    listener = context.get(TaskListener.class);
    envVars = context.get(EnvVars.class);
  }

  @SuppressWarnings("hiding")
  protected <T> ResponseData<T> verifyCommon(final BasicHubotStep step) {

    logger = listener.getLogger();
    String errorMessage = null;
    URL mainURL = null;

    final String message = step.getMessage();
    if (Util.fixEmpty(message) == null) {
      errorMessage = "Hubot: Message is empty or null.";
    }

    // Fail immediately.
    if (errorMessage != null) {
      return buildErrorResponse(new RuntimeException(errorMessage));
    }

    room = Util.fixEmpty(step.getRoom());
    url = Util.fixEmpty(step.getUrl());
    failOnErrorStr = Util.fixEmpty(step.getFailOnError());

    if (url == null) {
      site = HubotSite.get(run.getParent(), listener, step.getSite());
    }

    if (site == null) {
      if (url == null) {
        url = envVars.get("HUBOT_URL");
      }
      if (room == null) {
        room = envVars.get("HUBOT_DEFAULT_ROOM");
      }
      if (failOnErrorStr == null) {
        failOnErrorStr = envVars.get("HUBOT_FAIL_ON_ERROR");
      }
      if (Util.fixEmpty(url) == null) {
        errorMessage = "Hubot: HUBOT_URL or step parameter equivalent is empty or null.";
      } else {
        try {
          mainURL = new URL(Common.sanitizeURL(url));
        } catch (MalformedURLException e) {
          errorMessage = "Hubot: Malformed HUBOT_URL.";
        }
      }
      if (room == null) {
        errorMessage = "Hubot: HUBOT_DEFAULT_ROOM or step parameter equivalent is empty or null.";
      }
      if (failOnErrorStr != null) {
        try {
          failOnError = Boolean.parseBoolean(failOnErrorStr);
        } catch (Exception e) {
          errorMessage = "Hubot: Unable to parse failOnError.";
        }
      }
    } else {
      if (room != null) {
        site.setRoom(room);
      }
      if (failOnErrorStr != null) {
        site.setFailOnError(Boolean.parseBoolean(failOnErrorStr));
      }
      if (Util.fixEmpty(site.getUrl().toString()) == null) {
        errorMessage = "Hubot: url is empty or null on site: " + site.getName();
      }
      if (Util.fixEmpty(site.getRoom()) == null) {
        errorMessage = "Hubot: Room is empty or null on site: " + site.getName();
      }
      room = site.getRoom();
      failOnError = site.isFailOnError();
    }

    if (errorMessage != null) {
      return buildErrorResponse(new RuntimeException(errorMessage));
    }

    setHubotService(site, mainURL, room);

    buildUserName = Common.prepareBuildUserName(run.getCauses(), envVars);
    buildUserId = Common.prepareBuildUserId(run.getCauses(), envVars);
    buildCause = Common.prepareBuildCause(run.getCauses());

    return null;

  }

  @VisibleForTesting
  public void setHubotService(final HubotService service) {
    this.hubotService = service;
  }

  private void setHubotService(HubotSite site, final URL url, final String room) {
    if (site == null) {
      site = HubotSite.builder().url(url).room(room).build();
    }
    if (this.hubotService == null) {
      this.hubotService = new HubotService(site);
    }
  }
}
