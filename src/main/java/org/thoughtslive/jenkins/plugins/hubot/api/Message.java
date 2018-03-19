package org.thoughtslive.jenkins.plugins.hubot.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import hudson.EnvVars;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
public class Message {

  @JsonProperty("message")
  private String message;

  // INFO/SUCCESS/WARN/ERROR
  @JsonProperty("status")
  private String status = "INFO";

  @JsonProperty("extraData")
  private Map extraData;

  @JsonProperty("userName")
  private String userName;

  @JsonProperty("userId")
  private String userId;

  @JsonProperty("stepName")
  private String stepName;

  @JsonProperty("envVars")
  private EnvVars envVars;

  @JsonProperty("ts")
  private long ts;

}
