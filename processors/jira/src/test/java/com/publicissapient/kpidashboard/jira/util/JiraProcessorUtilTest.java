/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package com.publicissapient.kpidashboard.jira.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.codehaus.jettison.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.item.ExecutionContext;

import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.application.ProgressStatus;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;

@RunWith(MockitoJUnitRunner.class)
public class JiraProcessorUtilTest {

	@Mock
	SprintDetails sprintDetails;

	@Test
	public void deodeUTF8StringNull() {
		Object jiraResponse = null;
		assertNotNull(JiraProcessorUtil.deodeUTF8String(jiraResponse));
	}

	@Test
	public void testDecodeUTF8String_NullInput() {
		String result = JiraProcessorUtil.deodeUTF8String(null);

		assertEquals("", result);
	}

	@Test
	public void testDecodeUTF8String_EmptyInput() {
		Object jiraResponse = ""; // or null, or any other empty string format handled in your method
		String result = JiraProcessorUtil.deodeUTF8String(jiraResponse);

		assertEquals("", result);
	}

	@Test
	public void testDecodeUTF8String_NormalInput() {
		Object jiraResponse = "Some UTF-8 String"; // Replace with your test input
		String result = JiraProcessorUtil.deodeUTF8String(jiraResponse);
		assertTrue(!result.isEmpty());
	}

	@Test
	public void getFormattedDate() {
		String date = "07-09-2021";
		assertNotNull(JiraProcessorUtil.getFormattedDate(date));
	}

	@Test
	public void getFormattedDateForSprintDetails() {
		String date = "2024-01-03T23:01:29.666+05:30";
		assertNotNull(JiraProcessorUtil.getFormattedDateForSprintDetails(date));
	}

	@Test
	public void setSprintDetailsFromString() {
		String str = "\n" + "\"values\": [" + "id= 31227," + "state= closed," + "name= Test|PI_5|ITR_6|9 Jun-29Jun," +
				"startDate= 2021-06-09T08:38:00.000Z," + "endDate= 2021-06-29T08:38:00.000Z," +
				"completeDate= 2021-06-30T05:27:26.503Z," + "activatedDate= 2021-06-09T08:38:16.563Z," +
				"originBoardId= 11856," + "goal=1 " + "    ]";
		assertNull(JiraProcessorUtil.setSprintDetailsFromString(str, sprintDetails));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void processSprintDetail() throws ParseException, JSONException {
		List<String> list = new ArrayList<String>();
		list.add("User1");
		list.add("User2");
		list.add("User3");
		JSONArray array = new JSONArray();
		for (int i = 0; i < list.size(); i++) {
			JSONObject d = new JSONObject();
			d.put(i, list.get(i));
			array.add(d);
		}
		Object data = array;

		assertNotNull(JiraProcessorUtil.processSprintDetail(data));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void processSprintDetailNull() throws ParseException, JSONException {
		List<String> list = new ArrayList<String>();
		list.add("User1");
		list.add("User2");
		list.add(null);
		JSONArray array = new JSONArray();
		for (int i = 0; i < list.size(); i++) {
			JSONObject d = new JSONObject();
			d.put(i, list.get(i));
			array.add(d);
		}
		Object data = array;

		assertNotNull(JiraProcessorUtil.processSprintDetail(data));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void processSprintDetail1() throws ParseException, JSONException {
		List<String> list = new ArrayList<String>();
		list.add("User1");
		list.add("User2");
		list.add("User3");
		org.codehaus.jettison.json.JSONArray array = new org.codehaus.jettison.json.JSONArray();
		for (int i = 0; i < list.size(); i++) {
			JSONObject d = new JSONObject();
			d.put(i, list.get(i));
			array.put(d);
		}
		Object data = array;

		assertNotNull(JiraProcessorUtil.processSprintDetail(data));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void processSprintDetail1Null() throws ParseException, JSONException {
		List<String> list = new ArrayList<String>();
		list.add("User1");
		list.add("User2");
		list.add(null);
		org.codehaus.jettison.json.JSONArray array = new org.codehaus.jettison.json.JSONArray();
		for (int i = 0; i < list.size(); i++) {
			JSONObject d = new JSONObject();
			d.put(i, list.get(i));
			array.put(d);
		}
		Object data = array;

		assertNotNull(JiraProcessorUtil.processSprintDetail(data));
	}

	@Test
	public void processJqlForSprintFetch() {
		// Arrange
		List<String> issueKeys = Arrays.asList("KEY-1", "KEY-2", "KEY-3");
		String expected = "issueKey in (KEY-1, KEY-2, KEY-3)";

		// Act
		String actual = JiraProcessorUtil.processJqlForSprintFetch(issueKeys);

		// Assert
		assertEquals(expected, actual);
	}

	@Test
	public void testSaveChunkProgressInTrace_StepContextIsNull() {
		ProcessorExecutionTraceLog result = JiraProcessorUtil.saveChunkProgressInTrace(new ProcessorExecutionTraceLog(),
				null);
		assertNull(result);
	}

	@Test
	public void testSaveChunkProgressInTrace_ProcessorExecutionTraceLogIsNull() {
		StepContext stepContext = mock(StepContext.class);
		ProcessorExecutionTraceLog result = JiraProcessorUtil.saveChunkProgressInTrace(null, stepContext);
		assertNull(result);
	}

	@Test
	public void testSaveChunkProgressInTrace_BothNotNull() {
		// Prepare test data
		ProcessorExecutionTraceLog processorExecutionTraceLog = new ProcessorExecutionTraceLog();
		StepContext stepContext = mock(StepContext.class);
		StepExecution stepExecution = mock(StepExecution.class);
		JobExecution jobExecution = mock(JobExecution.class);

		// Mock methods
		when(stepContext.getStepExecution()).thenReturn(stepExecution);
		when(stepExecution.getJobExecution()).thenReturn(jobExecution);
		when(jobExecution.getExecutionContext()).thenReturn(new ExecutionContext());

		// Call the method
		ProcessorExecutionTraceLog result = JiraProcessorUtil.saveChunkProgressInTrace(processorExecutionTraceLog,
				stepContext);

		// Verify the result
		List<ProgressStatus> progressStatusList = result.getProgressStatusList();
		assertEquals(1, progressStatusList.size());
		assertEquals("Process Issues 0 to 0 out of 0", progressStatusList.get(0).getStepName());
		assertEquals(BatchStatus.COMPLETED.toString(), progressStatusList.get(0).getStatus());
	}

	@Test
	public void testGenerateLogMessage_withExceptionMessage() {
		Throwable exception = new Throwable("java.lang.NullPointerException: null");
		String expectedMessage = "An unexpected error has occurred. Please contact the KnowHow Support for assistance.";
		String actualMessage = JiraProcessorUtil.generateLogMessage(exception);
		assertEquals(expectedMessage, actualMessage);
	}

	@Test
	public void testGenerateLogMessage_withErrorCollection() {
		Throwable exception = new Throwable(
				"org.codehaus.jettison.json.JSONException: A JSONObject text must begin with '{'");
		String expectedMessage = "An unexpected error has occurred. Please contact the KnowHow Support for assistance.";
		String actualMessage = JiraProcessorUtil.generateLogMessage(exception);
		assertEquals(expectedMessage, actualMessage);
	}

	@Test
	public void testGenerateLogMessage_withErrorStatusCode401() {
		Throwable exception = new Throwable("[ErrorCollection{status=401, errors={}, errorMessages=[]}]");
		String expectedMessage = "Sorry, you are not authorized to access the requested resource.";
		String actualMessage = JiraProcessorUtil.generateLogMessage(exception);
		assertEquals(expectedMessage, actualMessage);
	}

	@Test
	public void testGenerateLogMessage_withErrorStatusCode403() {
		Throwable exception = new Throwable("[ErrorCollection{status=403, errors={}, errorMessages=[]}]");
		String expectedMessage = "Forbidden, check your credentials.";
		String actualMessage = JiraProcessorUtil.generateLogMessage(exception);
		assertEquals(expectedMessage, actualMessage);
	}

	@Test
	public void testGenerateLogMessage_withErrorStatusCode429() {
		Throwable exception = new Throwable("[ErrorCollection{status=429, errors={}, errorMessages=[]}]");
		String expectedMessage = "Too many request try after sometime.";
		String actualMessage = JiraProcessorUtil.generateLogMessage(exception);
		assertEquals(expectedMessage, actualMessage);
	}

	@Test
	public void testGenerateLogMessage_noMatchingPattern() {
		Throwable exception = new Throwable("Some random exception message");
		String expectedMessage = "An unexpected error has occurred. Please contact the KnowHow Support for assistance.";
		String actualMessage = JiraProcessorUtil.generateLogMessage(exception);
		assertEquals(expectedMessage, actualMessage);
	}
}
