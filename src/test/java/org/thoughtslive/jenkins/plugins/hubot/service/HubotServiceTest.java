package org.thoughtslive.jenkins.plugins.hubot.service;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.thoughtslive.jenkins.plugins.hubot.api.ResponseData;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

/**
 * Unit test case for HubotService class.
 * 
 * @author Naresh Rayapati
 *
 */
public class HubotServiceTest {

	// TODO Need to be dynamic and retry if that port not available. 
	final static int PORT = 1052;
	final static String hubotUrl = String.format("http://localhost:%d", PORT);

	@Rule
	public WireMockRule wireMockRule = new WireMockRule(PORT);
	public HubotService hubotService;

	@Before
	public void setUp() throws Exception {
		hubotService = new HubotService(hubotUrl);
	}

	@Test
	public void testSendMessage() throws Exception {
		final String room = "botlab";
		final String message = "testMessage";
		final String mockedMessage = "{\"message\":\"" + message + "\"}";

		wireMockRule.stubFor(post(urlEqualTo("/hubot/notify/%23" + room)).withRequestBody(equalTo(mockedMessage)).willReturn(aResponse().withStatus(200).withBody("{}")));

		final ResponseData<Void> response = hubotService.sendMessage(room, message);
		assertThat(response.getCode()).isEqualTo(200);
		wireMockRule.verify(postRequestedFor(urlEqualTo("/hubot/notify/%23" + room)).withRequestBody(equalTo(mockedMessage)));
	}

}
