package org.thoughtslive.jenkins.plugins.hubot.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Matchers.any;
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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.thoughtslive.jenkins.plugins.hubot.api.ResponseData;
import org.thoughtslive.jenkins.plugins.hubot.api.ResponseData.ResponseDataBuilder;
import org.thoughtslive.jenkins.plugins.hubot.service.HubotService;

/**
 * Unit test cases for SendStep class.
 *
 * @author Naresh Rayapati
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ApproveStepTest.class})
public class ApproveStepTest {

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

  ApproveStep.ApproveStepExecution stepExecution;

  @Before
  public void setup() throws IOException, InterruptedException {
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

  @Test
  public void testWithEmptyHubotURLThrowsAbortException() throws Exception {
    final ApproveStep step = new ApproveStep("message");
    step.setRoom("room");
    stepExecution = new ApproveStep.ApproveStepExecution(step, contextMock);
    stepExecution.setHubotService(hubotServiceMock);

    // Prepare Test.
    when(envVarsMock.get("HUBOT_URL")).thenReturn("");

    // Execute and assert Test.
    assertThatExceptionOfType(AbortException.class).isThrownBy(() -> {
      stepExecution.start();
    }).withMessage("Hubot: HUBOT_URL or step parameter equivalent is empty or null.")
        .withStackTraceContaining("AbortException")
        .withNoCause();
  }

  @Test
  public void testWithEmptyRoomThrowsAbortException() throws Exception {
    final ApproveStep step = new ApproveStep("message");
    step.setUrl("http://localhost:9090/");
    stepExecution = new ApproveStep.ApproveStepExecution(step, contextMock);
    stepExecution.setHubotService(hubotServiceMock);

    // Execute and assert Test.
    assertThatExceptionOfType(AbortException.class).isThrownBy(() -> {
      stepExecution.start();
    }).withMessage("Hubot: HUBOT_DEFAULT_ROOM or step parameter equivalent is empty or null.")
        .withStackTraceContaining("AbortException")
        .withNoCause();
  }

  @Test
  public void testWithEmptyMessageThrowsAbortException() throws Exception {
    final ApproveStep step = new ApproveStep("");
    step.setRoom("");
    stepExecution = new ApproveStep.ApproveStepExecution(step, contextMock);
    stepExecution.setHubotService(hubotServiceMock);

    // Execute and assert Test.
    assertThatExceptionOfType(AbortException.class).isThrownBy(() -> {
      stepExecution.start();
    }).withMessage("Hubot: Message is empty or null.").withStackTraceContaining("AbortException")
        .withNoCause();
  }

  @Test
  public void testErrorMessageSend() throws Exception {
    final ApproveStep step = new ApproveStep("message");
    step.setRoom("room");
    stepExecution = new ApproveStep.ApproveStepExecution(step, contextMock);
    stepExecution.setHubotService(hubotServiceMock);

    final ResponseDataBuilder<Void> builder = ResponseData.builder();
    when(hubotServiceMock.sendMessage(any()))
        .thenReturn(builder.successful(false).code(400).error("fake error.").build());

    // Assert Test
    assertThatExceptionOfType(AbortException.class).isThrownBy(() -> {
      stepExecution.start();
    }).withMessage("fake error.").withStackTraceContaining("AbortException").withNoCause();
  }

  @Test
  public void testFailOnErrorFalseDoesNotThrowsAbortException() throws Exception {
    final ApproveStep step = new ApproveStep("message");
    step.setFailOnError("false");
    stepExecution = new ApproveStep.ApproveStepExecution(step, contextMock);
    stepExecution.setHubotService(hubotServiceMock);

    // Prepare Test.
    when(envVarsMock.get("HUBOT_URL")).thenReturn("");

    // Execute and assert Test.
    stepExecution.start();
  }

  @Test
  public void testSuccessfulMessageSend() throws Exception {
    final ApproveStep step = new ApproveStep("message");
    step.setRoom("room");
    stepExecution = new ApproveStep.ApproveStepExecution(step, contextMock);
    stepExecution.setHubotService(hubotServiceMock);

    // Execute Test. TODO Mock InputStep too.
    assertThatExceptionOfType(AbortException.class).isThrownBy(() -> {
      stepExecution.start();
    }).withMessageStartingWith("Error while sending message:")
        .withStackTraceContaining("AbortException").withNoCause();

    // Assert Test
    verify(hubotServiceMock, times(1)).sendMessage(any());
    assertThat(step.getFailOnError()).isEqualTo(null);
  }
}
