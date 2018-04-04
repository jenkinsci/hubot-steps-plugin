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

  private static final long serialVersionUID = 682814063675194401L;

  @Getter
  @DataBoundSetter
  @Setter
  protected String room;

  @Getter
  @DataBoundSetter
  @Setter
  protected String message;

  @Getter
  @DataBoundSetter
  @Setter
  private String failOnError;

  @Getter
  @DataBoundSetter
  @Setter
  private String url;

  @Getter
  @DataBoundSetter
  @Setter
  private String status;

  @Getter
  @DataBoundSetter
  @Setter
  private String site;

  @Getter
  @DataBoundSetter
  @Setter
  private Map extraData;

  @Getter
  @DataBoundSetter
  @Setter
  private String tokens;

}
