package org.thoughtslive.jenkins.plugins.hubot.util;

import hudson.EnvVars;
import hudson.Util;
import hudson.model.Cause;
import hudson.model.Cause.UpstreamCause;
import hudson.model.Cause.UserIdCause;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import org.thoughtslive.jenkins.plugins.hubot.api.ResponseData;
import retrofit2.Response;

/**
 * Common utility functions.
 *
 * @author Naresh Rayapati
 */
public class Common {

  /**
   * Attaches the "/" at end of given url.
   *
   * @param url url as a string.
   * @return url which ends with "/"
   */
  public static String sanitizeURL(String url) {
    if (!url.endsWith("/")) {
      url = url + "/";
    }
    return url;
  }

  /**
   * Write a message to the given print stream.
   *
   * @param logger {@link PrintStream}
   * @param message to log.
   */
  public static void log(final PrintStream logger, final Object message) {
    if (logger != null) {
      logger.println(message);
    }
  }

  public static String getJobName(final EnvVars envVars) {
    return envVars.get("JOB_NAME");
  }

  /**
   * Returns build number from the given Environemnt Vars.
   *
   * @param logger {@link PrintStream}
   * @param envVars {@link EnvVars}
   * @return build number of current job.
   */
  public static String getBuildNumber(final PrintStream logger, final EnvVars envVars) {
    String answer = envVars.get("BUILD_NUMBER");
    if (answer == null) {
      log(logger, "No BUILD_NUMBER!");
      return "1";
    }
    return answer;
  }

  /**
   * Converts Retrofit's {@link Response} to {@link ResponseData}
   *
   * @param response instance of {@link Response}
   * @return an instance of {@link ResponseData}
   */
  public static <T> ResponseData<T> parseResponse(final Response<T> response) throws IOException {
    final ResponseData<T> resData = new ResponseData<T>();
    resData.setSuccessful(response.isSuccessful());
    resData.setCode(response.code());
    resData.setMessage(response.message());
    if (!response.isSuccessful()) {
      final String errorMessage = response.errorBody().string();
      resData.setError(errorMessage);
    } else {
      resData.setData(response.body());
    }
    return resData;
  }

  /**
   * Builds error response from the given exception.
   *
   * @param e instance of {@link Exception}
   * @return an instance of {@link ResponseData}
   */
  public static <T> ResponseData<T> buildErrorResponse(final Exception e) {
    final ResponseData<T> resData = new ResponseData<T>();
    final String errorMessage = getRootCause(e).getMessage();
    resData.setSuccessful(false);
    resData.setCode(-1);
    resData.setError(errorMessage);
    e.printStackTrace();
    return resData;
  }

  /**
   * Returns actual Cause from the given exception.
   *
   * @return {@link Throwable}
   */
  public static Throwable getRootCause(Throwable throwable) {
    if (throwable.getCause() != null) {
      return getRootCause(throwable.getCause());
    }
    return throwable;
  }

  /**
   * Return the current build user.
   *
   * @param causes build causes.
   * @param envVars environment variables.
   * @return user name.
   */
  public static String prepareBuildUserName(List<Cause> causes, EnvVars envVars) {
    String buildUser = "anonymous";

    // For multi branch jobs, while PR building.
    if (Util.fixEmpty(envVars.get("CHANGE_AUTHOR")) != null) {
      return envVars.get("CHANGE_AUTHOR");
    }

    if (causes != null && causes.size() > 0) {
      if (causes.get(0) instanceof UserIdCause) {
        buildUser = ((UserIdCause) causes.get(0)).getUserName();
      } else if (causes.get(0) instanceof UpstreamCause) {
        List<Cause> upstreamCauses = ((UpstreamCause) causes.get(0)).getUpstreamCauses();
        buildUser = prepareBuildUserName(upstreamCauses, envVars);
      }
    }
    return buildUser;
  }

  /**
   * Return the current build user Id.
   *
   * @param causes build causes.
   * @param envVars environment variables.
   * @return user name.
   */
  public static String prepareBuildUserId(List<Cause> causes, EnvVars envVars) {
    String buildUserId = null;

    if (causes != null && causes.size() > 0) {
      if (causes.get(0) instanceof UserIdCause) {
        buildUserId = ((UserIdCause) causes.get(0)).getUserId();
      } else if (causes.get(0) instanceof UpstreamCause) {
        List<Cause> upstreamCauses = ((UpstreamCause) causes.get(0)).getUpstreamCauses();
        buildUserId = prepareBuildUserId(upstreamCauses, envVars);
      }
    }
    return buildUserId;
  }

  public enum STEP {
    SEND, APPROVE, BUILD, TEST
  }

  public enum STATUS {
    INFO, SUCCESS, WARN, ERROR
  }
}