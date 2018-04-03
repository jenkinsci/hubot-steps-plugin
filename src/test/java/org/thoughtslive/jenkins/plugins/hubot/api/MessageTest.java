package org.thoughtslive.jenkins.plugins.hubot.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.joda.time.DateTime;
import org.junit.Test;

public class MessageTest {

  @Test
  public void testToString() throws Exception {
    final Message message = Message.builder().message("actualMessage").build();

    assertThat(message.toString()).isEqualTo(
        "Message(message=actualMessage, status=null, extraData=null, userName=null, buildCause=null, userId=null, stepName=null, envVars=null, tokens=null, ts=0)");
  }
}
