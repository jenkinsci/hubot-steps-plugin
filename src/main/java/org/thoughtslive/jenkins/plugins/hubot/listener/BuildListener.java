package org.thoughtslive.jenkins.plugins.hubot.listener;

import hudson.Extension;
import hudson.Util;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import org.apache.log4j.Logger;
import org.thoughtslive.jenkins.plugins.hubot.api.Message;
import org.thoughtslive.jenkins.plugins.hubot.config.HubotSite;
import org.thoughtslive.jenkins.plugins.hubot.config.JobProperty;
import org.thoughtslive.jenkins.plugins.hubot.service.HubotService;
import org.thoughtslive.jenkins.plugins.hubot.util.Common;
import org.thoughtslive.jenkins.plugins.hubot.util.Common.STEP;

/**
 * Listener to send failed job notifications to hubot.
 *
 * @author Naresh Rayapati.
 */
@Extension
public class BuildListener extends RunListener<Run<?, ?>> {

  private static final Logger LOGGER = Logger.getLogger(BuildListener.class.getName());

  @Override
  public synchronized void onCompleted(Run<?, ?> run, TaskListener listener) {
    test(run, listener);
  }

  @Override
  public synchronized void onStarted(Run<?, ?> r, TaskListener listener) {

  }

  private void test(Run<?, ?> run, TaskListener listener) {
    Result result = run.getResult();

    if (result != null && result == Result.FAILURE) {

    }

    Job<?, ?> job = run.getParent();
    JobProperty jpp = job.getProperty(JobProperty.class);
    boolean enableNotifications = false;
    String siteName = null;

    if (jpp != null) {
      enableNotifications = jpp.isEnableNotifications();
      siteName = Util.fixEmpty(jpp.getSiteNames());
    }

    if (enableNotifications) {
    }

    HubotSite site = HubotSite.get(run.getParent(), listener);
    if (site != null) {
      try {
        listener.getLogger().println("Hubot: Hooray!");
        HubotService service = new HubotService(site);
        final String buildUserName = Common
            .prepareBuildUserName(run.getCauses(), run.getEnvironment(listener));
        final String buildUserId = Common
            .prepareBuildUserId(run.getCauses(), run.getEnvironment(listener));

        Message message = Message.builder().message("Testing Build Listener")
            .ts(System.currentTimeMillis() / 1000).envVars(run.getEnvironment(listener))
            .userId(buildUserId).userName(buildUserName).stepName(STEP.BUILD.name()).build();
        service.sendMessage(message);
      } catch (Exception e) {
        LOGGER.error("Unable to Send message to Hubot.");
      }
    }

  }
}