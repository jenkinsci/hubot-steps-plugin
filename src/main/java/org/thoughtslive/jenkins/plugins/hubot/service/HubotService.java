package org.thoughtslive.jenkins.plugins.hubot.service;

import static org.thoughtslive.jenkins.plugins.hubot.util.Common.sanitizeURL;

import java.io.IOException;

import org.thoughtslive.jenkins.plugins.hubot.api.Message;

import okhttp3.OkHttpClient;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class HubotService {

	private final HubotEndPoints hubotEndPoints;

	public HubotService(final String baseUrl) {

		OkHttpClient httpClient = new OkHttpClient();

		this.hubotEndPoints = new Retrofit.Builder().baseUrl(sanitizeURL(baseUrl)).addConverterFactory(JacksonConverterFactory.create())
				.addCallAdapterFactory(RxJavaCallAdapterFactory.create()).client(httpClient).build().create(HubotEndPoints.class);
	}

	public Response<Void> sendMessage(final String room, final String comment) throws IOException {
		final Message message = new Message(comment);
		return hubotEndPoints.sendMessage(room, message).execute();
	}
}
