package org.thoughtslive.jenkins.plugins.hubot.service;

import static org.thoughtslive.jenkins.plugins.hubot.util.Common.buildErrorResponse;
import static org.thoughtslive.jenkins.plugins.hubot.util.Common.parseResponse;
import static org.thoughtslive.jenkins.plugins.hubot.util.Common.sanitizeURL;

import hudson.Util;
import okhttp3.OkHttpClient;
import org.thoughtslive.jenkins.plugins.hubot.api.Message;
import org.thoughtslive.jenkins.plugins.hubot.api.ResponseData;
import org.thoughtslive.jenkins.plugins.hubot.config.HubotSite;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * Hubot service which actually sends a message.
 *
 * @author Naresh Rayapati
 */
public class HubotService {

  private final HubotEndPoints hubotEndPoints;
  private final HubotSite hubotSite;

  public HubotService(final HubotSite hubotSite) {

    final OkHttpClient httpClient = new OkHttpClient();
    this.hubotSite = hubotSite;

    this.hubotEndPoints = new Retrofit.Builder().baseUrl(sanitizeURL(hubotSite.getUrl().toString()))
        .addConverterFactory(JacksonConverterFactory.create())
        .addCallAdapterFactory(RxJavaCallAdapterFactory.create()).client(httpClient).build()
        .create(HubotEndPoints.class);
  }

  /**
   * Sends message to given room.
   *
   * @param message actual message to be sent.
   * @return Nothing except the response with error code if any.
   */
  public ResponseData<Void> sendMessage(final Message message) {
    try {
      String room;
      if (this.hubotSite.isUseFolderName()
          && Util.fixEmpty(this.hubotSite.getRoomPrefix()) != null) {
        room = this.hubotSite.getRoomPrefix().trim() + this.hubotSite.getRoom().trim();
      } else {
        room = this.hubotSite.getRoom().trim();
      }

      return parseResponse(hubotEndPoints.sendMessage(room, message).execute());
    } catch (Exception e) {
      return buildErrorResponse(e);
    }
  }
}
