package org.thoughtslive.jenkins.plugins.hubot.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class MessageTest {

  @Test
  public void testToString() throws Exception {
    final Message author = new Message("testMessage");

    assertThat(author.toString()).isEqualTo("Message(message=testMessage)");
  }
}
