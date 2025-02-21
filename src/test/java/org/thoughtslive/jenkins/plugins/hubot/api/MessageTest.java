package org.thoughtslive.jenkins.plugins.hubot.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class MessageTest {

  @Test
  public void testToString() {
    final Message message = Message.builder().message("actualMessage").build();

    assertThat(message).hasToString(
        "Message(message=actualMessage, status=null, extraData=null, userName=null, buildCause=null, userId=null, stepName=null, envVars=null, tokens=null, ts=0, id=null, submitter=null, submitterParameter=null, ok=null, parameters=null)");
  }
}
