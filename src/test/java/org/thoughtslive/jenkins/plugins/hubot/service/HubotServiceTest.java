package org.thoughtslive.jenkins.plugins.hubot.service;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.net.URL;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.thoughtslive.jenkins.plugins.hubot.api.Message;
import org.thoughtslive.jenkins.plugins.hubot.api.ResponseData;
import org.thoughtslive.jenkins.plugins.hubot.config.HubotSite;

/**
 * Unit test case for HubotService class.
 *
 * @author Naresh Rayapati
 */
public class HubotServiceTest {

  // TODO Need to be dynamic and retry if that port not available.
  final static int PORT = 1052;
  final static String hubotUrl = String.format("http://localhost:%d", PORT);
  final String room = "botlab";
  @Rule
  public WireMockRule wireMockRule = new WireMockRule(PORT);
  public HubotService hubotService;

  @Before
  public void setUp() throws Exception {
    final HubotSite site = HubotSite.builder().room(room).url(new URL(hubotUrl)).build();
    hubotService = new HubotService(site);
  }

  @Test
  public void testSendMessage() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    final String testMessage = "testMessage";
    final Message message = Message.builder().message(testMessage).build();
    final String mockedMessage = mapper.writeValueAsString(message);
    System.out.println(mockedMessage);

    wireMockRule.stubFor(
        post(urlEqualTo("/hubot/notify/" + room)).withRequestBody(equalTo(mockedMessage))
            .willReturn(aResponse().withStatus(200).withBody("{}")));

    final ResponseData<Void> response = hubotService.sendMessage(message);
    assertThat(response.getCode()).isEqualTo(200);
    wireMockRule.verify(postRequestedFor(urlEqualTo("/hubot/notify/" + room))
        .withRequestBody(equalTo(mockedMessage)));
  }
}
