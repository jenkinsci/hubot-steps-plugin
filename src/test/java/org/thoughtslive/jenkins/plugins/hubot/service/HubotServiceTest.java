 package org.thoughtslive.jenkins.plugins.hubot.service;

 import static org.assertj.core.api.Assertions.assertThat;

 import com.fasterxml.jackson.databind.ObjectMapper;
 import okhttp3.HttpUrl;
 import okhttp3.mockwebserver.MockResponse;
 import okhttp3.mockwebserver.MockWebServer;
 import org.junit.After;
 import org.junit.Before;
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

   final String room = "botlab";
   public HubotService hubotService;
   private MockWebServer server;

   @Before
   public void setUp() throws Exception {
     server = new MockWebServer();
     server.start();
     HttpUrl baseUrl = server.url("/");

     final HubotSite site = HubotSite.builder().room(room).url(baseUrl.url()).build();
     hubotService = new HubotService(site);
   }

   @After
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
