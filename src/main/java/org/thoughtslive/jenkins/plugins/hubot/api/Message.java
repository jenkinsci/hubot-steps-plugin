package org.thoughtslive.jenkins.plugins.hubot.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import hudson.EnvVars;
import hudson.model.ParameterDefinition;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.thoughtslive.jenkins.plugins.hubot.config.notifications.Type;

/**
 * Message DTO, which is being sent as json to Hubot.
 *
 * @author Naresh Rayapati
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Message implements Serializable {

  private static final long serialVersionUID = 7679509189889358976L;

  @JsonProperty("message")
  private String message;

  @JsonProperty("status")
  private String status = Type.SUCCESS.name();

  @JsonProperty("extraData")
  private Map extraData;

  @JsonProperty("userName")
  private String userName;

  @JsonProperty("buildCause")
  private String buildCause;

  @JsonProperty("userId")
  private String userId;

  @JsonProperty("stepName")
  private String stepName;

  @JsonProperty("envVars")
  private EnvVars envVars;

  @JsonProperty("tokens")
  private Map tokens;

  @JsonProperty("ts")
  private long ts;

  // Approve step related params.
  @JsonProperty("id")
  private String id;

  @JsonProperty("submitter")
  private String submitter;

  @JsonProperty("submitterParameter")
  private String submitterParameter;

  @JsonProperty("ok")
  private String ok;

  @JsonProperty("parameters")
  private String parameters;

}
