 package org.thoughtslive.jenkins.plugins.hubot.service;

 import static org.assertj.core.api.Assertions.assertThat;

 import com.fasterxml.jackson.databind.ObjectMapper;
  import com.squareup.okhttp.HttpUrl;
 import com.squareup.okhttp.mockwebserver.MockResponse;
 import com.squareup.okhttp.mockwebserver.MockWebServer;
 import org.junit.jupiter.api.AfterEach;
 import org.junit.jupiter.api.BeforeEach;
 import org.junit.jupiter.api.Test;
 import org.thoughtslive.jenkins.plugins.hubot.api.Message;
 import org.thoughtslive.jenkins.plugins.hubot.api.ResponseData;
 import org.thoughtslive.jenkins.plugins.hubot.config.HubotSite;

 /**
  * Unit test case for HubotService class.
  *
  * @author Naresh Rayapati
  */
 public class HubotServiceTest {

   final String room = "botlab";
   public HubotService hubotService;
   private MockWebServer server;

   @BeforeEach
   public void setUp() throws Exception {
     server = new MockWebServer();
     server.start();
     HttpUrl baseUrl = server.url("/");

     final HubotSite site = HubotSite.builder().room(room).url(baseUrl.url()).build();
     hubotService = new HubotService(site);
   }

   @AfterEach
   public void tearDown() throws Exception {
       server.shutdown();
   }

   @Test
   public void testSendMessage() throws Exception {
     ObjectMapper mapper = new ObjectMapper();
     final String testMessage = "testMessage";
     final Message message = Message.builder().message(testMessage).build();
     final String mockedMessage = mapper.writeValueAsString(message);
     server.enqueue(new MockResponse().setBody(mockedMessage));

     final ResponseData<Void> response = hubotService.sendMessage(message);
     assertThat(response.getCode()).isEqualTo(200);
   }
 }
