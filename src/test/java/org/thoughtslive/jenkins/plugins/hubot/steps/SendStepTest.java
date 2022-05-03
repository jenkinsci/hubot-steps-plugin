package org.thoughtslive.jenkins.plugins.hubot.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hudson.AbortException;
import hudson.EnvVars;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import java.io.IOException;
import java.io.PrintStream;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.thoughtslive.jenkins.plugins.hubot.api.ResponseData;
import org.thoughtslive.jenkins.plugins.hubot.api.ResponseData.ResponseDataBuilder;
import org.thoughtslive.jenkins.plugins.hubot.service.HubotService;

/**
 * Unit test cases for SendStep class.
 *
 * @author Naresh Rayapati
 */
public class SendStepTest {

  @Mock
  TaskListener taskListenerMock;
  @Mock
  Run runMock;
  @Mock
  EnvVars envVarsMock;
  @Mock
  PrintStream printStreamMock;
  @Mock
  HubotService hubotServiceMock;
  @Mock
  StepContext contextMock;
  @Mock
  Job jobMock;

  SendStep.SendStepExecution stepExecution;

  private AutoCloseable closeable;

  @Before
  public void setup() throws IOException, InterruptedException {
    closeable = MockitoAnnotations.openMocks(this);

    when(runMock.getCauses()).thenReturn(null);
    when(runMock.getParent()).thenReturn(jobMock);
    when(taskListenerMock.getLogger()).thenReturn(printStreamMock);
    doNothing().when(printStreamMock).println();

    final ResponseDataBuilder<Void> builder = ResponseData.builder();
    when(hubotServiceMock.sendMessage(any()))
        .thenReturn(builder.successful(true).code(200).message("Success").build());

    when(envVarsMock.get("HUBOT_URL")).thenReturn("http://localhost:9090/");
    when(envVarsMock.get("BUILD_URL")).thenReturn("http://localhost:9090/hubot-testing/job/01");

    when(contextMock.get(Run.class)).thenReturn(runMock);
    when(contextMock.get(TaskListener.class)).thenReturn(taskListenerMock);
    when(contextMock.get(EnvVars.class)).thenReturn(envVarsMock);
  }

  @After
  public void tearDown() throws Exception {
      closeable.close();
  }  

  @Test
  public void testWithEmptyHubotURLThrowsAbortException() throws Exception {
    final SendStep step = new SendStep("message");
    step.setRoom("room");
    stepExecution = new SendStep.SendStepExecution(step, contextMock);
    stepExecution.setHubotService(hubotServiceMock);

    // Prepare Test.
    when(envVarsMock.get("HUBOT_URL")).thenReturn("");

    // Execute and assert Test.
    assertThatExceptionOfType(AbortException.class).isThrownBy(() -> {
      stepExecution.run();
    }).withMessage("Hubot: HUBOT_URL or step parameter equivalent is empty or null.")
        .withStackTraceContaining("AbortException")
        .withNoCause();
  }

  @Test
  public void testWithEmptyRoomThrowsAbortException() throws Exception {
    final SendStep step = new SendStep("message");
    step.setUrl("http://localhost:9090/");

    stepExecution = new SendStep.SendStepExecution(step, contextMock);
    stepExecution.setHubotService(hubotServiceMock);

    // Execute and assert Test.
    assertThatExceptionOfType(AbortException.class).isThrownBy(() -> {
      stepExecution.run();
    }).withMessage("Hubot: HUBOT_DEFAULT_ROOM or step parameter equivalent is empty or null.")
        .withStackTraceContaining("AbortException")
        .withNoCause();
  }

  @Test
  public void testWithEmptyMessageThrowsAbortException() throws Exception {
    final SendStep step = new SendStep("");
    step.setRoom("room");
    stepExecution = new SendStep.SendStepExecution(step, contextMock);
    stepExecution.setHubotService(hubotServiceMock);

    // Execute and assert Test.
    assertThatExceptionOfType(AbortException.class).isThrownBy(() -> {
      stepExecution.run();
    }).withMessage("Hubot: Message is empty or null.").withStackTraceContaining("AbortException")
        .withNoCause();
  }

  @Test
  public void testErrorMessageSend() throws Exception {
    final SendStep step = new SendStep("message");
    step.setRoom("room");
    stepExecution = new SendStep.SendStepExecution(step, contextMock);
    stepExecution.setHubotService(hubotServiceMock);

    final ResponseDataBuilder<Void> builder = ResponseData.builder();
    when(hubotServiceMock.sendMessage(any())).thenReturn(
        builder.successful(false).code(400).error("Error while sending message to room.").build());

    // Assert Test
    assertThatExceptionOfType(AbortException.class).isThrownBy(() -> {
      stepExecution.run();
    }).withMessage("Error while sending message to room.")
        .withStackTraceContaining("AbortException").withNoCause();
  }

  @Test
  public void testFailOnErrorFalseDoesNotThrowsAbortException() throws Exception {
    final SendStep step = new SendStep("message");
    step.setFailOnError("false");
    stepExecution = new SendStep.SendStepExecution(step, contextMock);
    stepExecution.setHubotService(hubotServiceMock);

    // Prepare Test.
    when(envVarsMock.get("HUBOT_URL")).thenReturn("");

    // Execute and assert Test.
    stepExecution.run();
  }

  @Test
  public void testSuccessfulMessageSend() throws Exception {
    final SendStep step = new SendStep("message");
    step.setRoom("room");
    stepExecution = new SendStep.SendStepExecution(step, contextMock);
    stepExecution.setHubotService(hubotServiceMock);

    // Execute Test.
    stepExecution.run();

    // Assert Test
    verify(hubotServiceMock, times(1)).sendMessage(any());
    assertThat(step.getFailOnError()).isEqualTo(null);
  }
}
