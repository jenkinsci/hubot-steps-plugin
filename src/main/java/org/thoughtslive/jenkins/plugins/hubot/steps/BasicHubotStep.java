package org.thoughtslive.jenkins.plugins.hubot.steps;

import java.io.Serial;
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
@Setter
@Getter
public abstract class BasicHubotStep extends Step implements Serializable {

  @Serial
  private static final long serialVersionUID = 682814063675194401L;

  @DataBoundSetter
  protected String room;

  @DataBoundSetter
  protected String message;

  @DataBoundSetter
  private String failOnError;

  @DataBoundSetter
  private String url;

  @DataBoundSetter
  private String status;

  @DataBoundSetter
  private String site;

  @DataBoundSetter
  private Map<String, String> extraData;

  @DataBoundSetter
  private String tokens;

}
