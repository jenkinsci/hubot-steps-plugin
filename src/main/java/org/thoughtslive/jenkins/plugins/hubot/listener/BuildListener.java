package org.thoughtslive.jenkins.plugins.hubot.listener;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.Util;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import java.util.Map;
import org.thoughtslive.jenkins.plugins.hubot.api.Message;
import org.thoughtslive.jenkins.plugins.hubot.config.HubotSite;
import org.thoughtslive.jenkins.plugins.hubot.config.notifications.Config;
import org.thoughtslive.jenkins.plugins.hubot.config.notifications.Type;
import org.thoughtslive.jenkins.plugins.hubot.service.HubotService;
import org.thoughtslive.jenkins.plugins.hubot.util.Common;
import org.thoughtslive.jenkins.plugins.hubot.util.Common.STEP;

/**
 * Listener to send build notifications to hubot.
 *
 * @author Naresh Rayapati.
 */
@Extension
public class BuildListener extends RunListener<Run<?, ?>> {

  @Override
  @SuppressFBWarnings
  public void onCompleted(Run<?, ?> run, TaskListener listener) {
    HubotSite site = HubotSite.get(run.getParent(), listener);
    if(run.getPreviousBuild() != null) {
      Type type = Type.fromResults(run.getPreviousBuild().getResult(), run.getResult());
      if (site != null && site.getNotifications() != null) {
        for (Config config : site.getNotifications()) {
          if (config.isNotifyEnabled()) {
            if (config.getNotificationType().equals(type)) {
              if (Util.fixEmpty(config.getRoomNames()) != null) {
                for (String roomName : config.getRoomNames().split(",")) {
                  sendMessage(run, listener, type, site, roomName.trim(), config);
                }
              } else {
                sendMessage(run, listener, type, site, null, config);
              }
            }
          }
        }
      }
    }
    super.onCompleted(run, listener);
  }

  @Override
  public void onStarted(Run<?, ?> run, TaskListener listener) {
    HubotSite site = HubotSite.get(run.getParent(), listener);
    if (site != null && site.getNotifications() != null) {
      for (Config config : site.getNotifications()) {
        if (config.isNotifyEnabled() && config.getNotificationType().equals(Type.STARTED)) {
          if (Util.fixEmpty(config.getRoomNames()) != null) {
            for (String roomName : config.getRoomNames().split(",")) {
              sendMessage(run, listener, Type.STARTED, site, roomName.trim(), config);
            }
          } else {
            sendMessage(run, listener, Type.STARTED, site, null, config);
          }
        }
      }
    }
    super.onStarted(run, listener);
  }

  /**
   * Sends a message to given site.
   */
  @SuppressFBWarnings
  private void sendMessage(Run<?, ?> run, TaskListener listener, Type type, final HubotSite site,
      final String roomName, final Config config) {
    if (site != null) {
      try {
        HubotSite cloneSite = site.clone();
        if (Util.fixEmpty(roomName) != null) {
          cloneSite.setRoom(roomName);
          // To avoid using the room prefix for individual build events those explicitly specified the room name.
          cloneSite.setRoomPrefix(null);
        }
        HubotService service = new HubotService(cloneSite);
        final String buildUserName = Common
            .prepareBuildUserName(run.getCauses(), run.getEnvironment(listener));
        final String buildUserId = Common
            .prepareBuildUserId(run.getCauses(), run.getEnvironment(listener));

        final Map tokens = Common.expandMacros(config.getTokens(), run, null, listener);

        Message message = Message.builder().message(type.getStatus())
            .ts(System.currentTimeMillis()).envVars(run.getEnvironment(listener))
            .status(type.name()).tokens(tokens)
            .buildCause(Common.prepareBuildCause(run.getCauses()))
            .userId(buildUserId).userName(buildUserName).stepName(STEP.BUILD.name()).build();

        listener.getLogger().println(
            "Hubot: Sending " + type.name() + " message to room: " + cloneSite.getRoom()
                + " of site: "
                + cloneSite.getName());
        Common
            .logResponse(service.sendMessage(message), listener.getLogger(), site.isFailOnError());
      } catch (Exception e) {
        listener.getLogger()
            .println("Unable to send message to Hubot: " + Common.getRootCause(e).getMessage());
        if (site.isFailOnError()) {
          run.setResult(Result.FAILURE);
        }
      }
    }
  }
}