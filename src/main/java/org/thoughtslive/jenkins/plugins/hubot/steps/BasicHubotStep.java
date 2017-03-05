package org.thoughtslive.jenkins.plugins.hubot.steps;

import java.io.Serializable;

import org.jenkinsci.plugins.workflow.steps.Step;
import org.kohsuke.stapler.DataBoundSetter;

import lombok.Getter;
import lombok.Setter;

/**
 * Base class for all hubot steps
 */
public abstract class BasicHubotStep extends Step implements Serializable {

  private static final long serialVersionUID = 7268920801605705697L;

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

}
