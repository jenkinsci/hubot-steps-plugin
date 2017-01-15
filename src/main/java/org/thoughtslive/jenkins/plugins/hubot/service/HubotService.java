package org.thoughtslive.jenkins.plugins.hubot.service;

import static org.thoughtslive.jenkins.plugins.hubot.util.Common.buildErrorResponse;
import static org.thoughtslive.jenkins.plugins.hubot.util.Common.parseResponse;
import static org.thoughtslive.jenkins.plugins.hubot.util.Common.sanitizeURL;

import org.thoughtslive.jenkins.plugins.hubot.api.Message;
import org.thoughtslive.jenkins.plugins.hubot.api.ResponseData;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class HubotService {

	private final HubotEndPoints hubotEndPoints;

	public HubotService(final String baseUrl) {

		final OkHttpClient httpClient = new OkHttpClient();

		this.hubotEndPoints = new Retrofit.Builder().baseUrl(sanitizeURL(baseUrl)).addConverterFactory(JacksonConverterFactory.create())
				.addCallAdapterFactory(RxJavaCallAdapterFactory.create()).client(httpClient).build().create(HubotEndPoints.class);
	}

	/**
	 * Sends message to given room.
	 * 
	 * @param room
	 *            chat room.
	 * @param message
	 *            actual message to be sent.
	 * @return Nothing except the response with error code if any.
	 */
	public ResponseData<Void> sendMessage(final String room, final String message) {
		final Message jsonMessage = Message.builder().message(message).build();
		try {
			return parseResponse(hubotEndPoints.sendMessage("#"+room, jsonMessage).execute());
		} catch (Exception e) {
			return buildErrorResponse(e);
		}

	}
}
