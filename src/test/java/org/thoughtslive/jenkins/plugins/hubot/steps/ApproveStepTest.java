package org.thoughtslive.jenkins.plugins.hubot.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.spy;

import java.io.PrintStream;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.assertj.core.api.ThrowableTypeAssert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.thoughtslive.jenkins.plugins.hubot.api.ResponseData;
import org.thoughtslive.jenkins.plugins.hubot.api.ResponseData.ResponseDataBuilder;
import org.thoughtslive.jenkins.plugins.hubot.service.HubotService;

import hudson.AbortException;
import hudson.EnvVars;
import hudson.model.TaskListener;

/**
 * Unit test cases for SendStep class.
 * 
 * @author Naresh Rayapati
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ApproveStepTest.class})
public class ApproveStepTest {

	@Mock
	TaskListener taskListenerMock;
	@Mock
	EnvVars envVarsMock;
	@Mock
	PrintStream printStreamMock;
	@Mock
	HubotService hubotServiceMock;
	ApproveStep.ApproveStepExecution stepExecution;

	@Before
	public void setup() {
		stepExecution = spy(new ApproveStep.ApproveStepExecution());
		
		when(taskListenerMock.getLogger()).thenReturn(printStreamMock);
		doNothing().when(printStreamMock).println();
		
		final ResponseDataBuilder<Void> builder = ResponseData.builder();
		when(hubotServiceMock.sendMessage(anyString(), anyString())).thenReturn(builder.successful(true).code(200).message("Success").build());
		
		when(envVarsMock.get("HUBOT_URL")).thenReturn("http://localhost:9090/");
		when(envVarsMock.get("BUILD_URL")).thenReturn("http://localhost:9090/hubot-testing/job/01");
		
		stepExecution.listener = taskListenerMock;
		stepExecution.envVars = envVarsMock;
		
		doReturn(hubotServiceMock).when(stepExecution).getHubotService(anyString());
	}

	@Test
	public void testWithEmptyHubotURLThrowsAbortException() throws Exception {
		final ApproveStep hubotSendStep = new ApproveStep("room", "message");
		stepExecution.step = hubotSendStep;

		// Prepare Test.
		when(envVarsMock.get("HUBOT_URL")).thenReturn("");

		// Execute and assert Test.
		assertThatExceptionOfType(AbortException.class)
			.isThrownBy(new ThrowingCallable() {
				@Override
				public void call() throws Throwable {
					stepExecution.start(); 
				}
			})
			.withMessage("Hubot: HUBOT_URL is empty or null.")
			.withStackTraceContaining("AbortException")
			.withNoCause();
	}

	@Test
	public void testWithEmptyRoomThrowsAbortException() throws Exception {
		final ApproveStep hubotSendStep = new ApproveStep("", "message");
		stepExecution.step = hubotSendStep;

		// Execute and assert Test.
		assertThatExceptionOfType(AbortException.class)
			.isThrownBy(new ThrowingCallable() {
				@Override
				public void call() throws Throwable {
					stepExecution.start(); 
				}
			})
			.withMessage("Hubot: Room is empty or null.")
			.withStackTraceContaining("AbortException")
			.withNoCause();
	}

	@Test
	public void testWithEmptyMessageThrowsAbortException() throws Exception {
		final ApproveStep hubotSendStep = new ApproveStep("room", "");
		stepExecution.step = hubotSendStep;

		// Execute and assert Test.
		assertThatExceptionOfType(AbortException.class)
			.isThrownBy(new ThrowingCallable() {
				@Override
				public void call() throws Throwable {
					stepExecution.start(); 
				}
			})
			.withMessage("Hubot: Message is empty or null.")
			.withStackTraceContaining("AbortException")
			.withNoCause();
	}

	@Test
	public void testErrorMessageSend() throws Exception {
		final ApproveStep hubotSendStep = new ApproveStep("room", "message");
		stepExecution.step = hubotSendStep;

		final ResponseDataBuilder<Void> builder = ResponseData.builder();
		when(hubotServiceMock.sendMessage(anyString(), anyString())).thenReturn(builder.successful(false).code(400).error("fake error.").build());

		// Assert Test
		assertThatExceptionOfType(AbortException.class)
			.isThrownBy(new ThrowingCallable() {
				@Override
				public void call() throws Throwable {
					stepExecution.start(); 
				}
			})
			.withMessage("fake error.")
			.withStackTraceContaining("AbortException")
			.withNoCause();
	}

	@Test
	public void testFailOnErrorFalseDoesNotThrowsAbortException() throws Exception {
		final ApproveStep hubotSendStep = new ApproveStep("", "");
		hubotSendStep.setFailOnError(false);
		stepExecution.step = hubotSendStep;

		// Prepare Test.
		when(envVarsMock.get("HUBOT_URL")).thenReturn("");

		// Execute and assert Test.
		stepExecution.start();
	}

	@Test
	public void testSuccessfulMessageSend() throws Exception {
		final ApproveStep hubotSendStep = new ApproveStep("room", "message");
		stepExecution.step = hubotSendStep;

		// Execute Test.  TODO Mock InputStep too.
		assertThatExceptionOfType(AbortException.class)
		.isThrownBy(new ThrowingCallable() {
			@Override
			public void call() throws Throwable {
				stepExecution.start(); 
			}
		})
		.withMessageStartingWith("Error while sending message:")
		.withStackTraceContaining("AbortException")
		.withNoCause();

		// Assert Test
		verify(hubotServiceMock, times(1)).sendMessage("room", "Job: http://localhost:9090/hubot-testing/job/01\n\n"
				+ "message\n\tto Proceed reply:  .j proceed /hubot-testing/job/01"
				+ "\n\tto Abort reply  :  .j abort /hubot-testing/job/01\n");
		assertThat(stepExecution.step.isFailOnError()).isEqualTo(true);
	}
}