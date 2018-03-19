package org.thoughtslive.jenkins.plugins.hubot.api;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Common response that every pipeline step would receive.
 *
 * @author Naresh Rayapati
 */
@Data
@ToString(of = {"successful", "code", "message", "error", "data"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseData<T> implements Serializable {

  private static final long serialVersionUID = -6177555429105640650L;

  private boolean successful;

  private int code;

  private String message;

  private String error;

  private T data;
}
