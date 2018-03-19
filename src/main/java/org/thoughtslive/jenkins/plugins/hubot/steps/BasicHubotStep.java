package org.thoughtslive.jenkins.plugins.hubot.steps;

import java.io.Serializable;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.kohsuke.stapler.DataBoundSetter;

/**
 * Base class for all hubot steps.
 *
 * @author Naresh Rayapati.
 */
public abstract class BasicHubotStep extends Step implements Serializable {

  private static final long serialVersionUID = -4439535326265986082L;

  @Getter
  @DataBoundSetter
  protected String room;

  @Getter
  @DataBoundSetter
  protected String message;

  @Getter
  @DataBoundSetter
  @Setter
  private boolean failOnError = true;

  @Getter
  @DataBoundSetter
  private String url;

  @Getter
  @DataBoundSetter
  private String status;

  @Getter
  @DataBoundSetter
  private String site;

  @Getter
  @DataBoundSetter
  private Map extraData;

}
