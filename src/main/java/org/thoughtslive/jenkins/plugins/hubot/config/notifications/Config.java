package org.thoughtslive.jenkins.plugins.hubot.config.notifications;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;

import java.io.Serial;
import java.io.Serializable;
import jenkins.model.Jenkins;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Holds the build notification configuration.
 *
 * @author Naresh Rayapati.
 */
@Getter
@ToString
@Builder
public class Config implements Describable<Config>, Serializable, Cloneable {

  @Serial
  private static final long serialVersionUID = -8251804333532726515L;

  private final boolean notifyEnabled;
  private final String roomNames;
  private final Type notificationType;
  private final String tokens;

  @DataBoundConstructor
  public Config(final boolean notifyEnabled, final String roomNames, final Type notificationType,
      final String tokens) {
    this.notifyEnabled = notifyEnabled;
    this.roomNames = roomNames;
    this.notificationType = notificationType;
    this.tokens = tokens;
  }

  @Override
  public Descriptor<Config> getDescriptor() {
    return Jenkins.get().getDescriptorByType(DescriptorImpl.class);
  }

  @Override
  public Config clone() throws CloneNotSupportedException {
    super.clone();
    return Config.builder().notifyEnabled(notifyEnabled).roomNames(roomNames)
        .notificationType(notificationType).tokens(tokens).build();
  }

  @Extension
  public static final class DescriptorImpl extends Descriptor<Config> {

    @NonNull
    @Override
    public String getDisplayName() {
      return "Hubot Notification";
    }
  }
}