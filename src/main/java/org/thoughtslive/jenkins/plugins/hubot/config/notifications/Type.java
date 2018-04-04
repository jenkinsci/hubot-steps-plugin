package org.thoughtslive.jenkins.plugins.hubot.config.notifications;

import hudson.model.Result;
import org.thoughtslive.jenkins.plugins.hubot.Messages;

/**
 * Notification Type.
 *
 * @author Naresh Rayapati.
 */
public enum Type {

  STARTED {
    @Override
    public String getStatus() {
      return Messages.Started();
    }
  },
  ABORTED {
    @Override
    public String getStatus() {
      return Messages.Aborted();
    }
  },
  SUCCESS {
    @Override
    public String getStatus() {
      return Messages.Success();
    }
  },
  FAILURE {
    @Override
    public String getStatus() {
      return Messages.Failure();
    }
  },
  NOT_BUILT {
    @Override
    public String getStatus() {
      return Messages.NotBuilt();
    }
  },
  BACK_TO_NORMAL {
    @Override
    public String getStatus() {
      return Messages.BackToNormal();
    }
  },
  UNSTABLE {
    @Override
    public String getStatus() {
      return Messages.Unstable();
    }
  };

  public static final Type fromResults(Result previousResult, Result result) {
    if (result == Result.ABORTED) {
      return ABORTED;
    } else if (result == Result.FAILURE) {
      return FAILURE;
    } else if (result == Result.NOT_BUILT) {
      return NOT_BUILT;
    } else if (result == Result.UNSTABLE) {
      return UNSTABLE;
    } else if (result == Result.SUCCESS) {
      if (previousResult != null && previousResult != Result.SUCCESS) {
        return BACK_TO_NORMAL;
      } else {
        return SUCCESS;
      }
    }

    throw new IllegalStateException("Unable to determine notification type");
  }

  public abstract String getStatus();
}