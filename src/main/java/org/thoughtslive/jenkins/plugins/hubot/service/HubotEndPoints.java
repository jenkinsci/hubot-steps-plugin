package org.thoughtslive.jenkins.plugins.hubot.service;

import org.thoughtslive.jenkins.plugins.hubot.api.Message;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Actual hubot endpoint.
 *
 * @author Naresh Rayapati
 */
public interface HubotEndPoints {

  @POST("hubot/notify/{room}")
  Call<Void> sendMessage(@Path("room") String room, @Body Message message);

}
