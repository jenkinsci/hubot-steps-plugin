package org.thoughtslive.jenkins.plugins.hubot.config.notifications;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;
import lombok.Getter;
import lombok.ToString;
import org.kohsuke.stapler.DataBoundConstructor;

@ToString
public class Config implements Describable<Config> {

  @Getter
  private final boolean notifyEnabled;
  @Getter
  private final String roomName;
  @Getter
  private final Type notificationType;
  @Getter
  private final String tokens;

  @DataBoundConstructor
  public Config(final boolean notifyEnabled, final String roomName, final Type notificationType,
      final String tokens) {
    this.notifyEnabled = notifyEnabled;
    this.roomName = roomName;
    this.notificationType = notificationType;
    this.tokens = tokens;
  }

  @Override
  public Descriptor<Config> getDescriptor() {
    return Jenkins.getInstance().getDescriptorByType(DescriptorImpl.class);
  }

  @Extension
  public static final class DescriptorImpl extends Descriptor<Config> {

    @Override
    public String getDisplayName() {
      return "Hubot Notification";
    }
  }
}