package com.publicissapient.kpidashboard.apis.feedback.rest;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.publicissapient.kpidashboard.apis.feedback.service.FeedbackService;
import com.publicissapient.kpidashboard.apis.model.FeedbackSubmitDTO;

/**
 * @author sanbhand1
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class FeedbackControllerTest {

	ObjectMapper mapper = new ObjectMapper();
	private MockMvc mockMvc;
	private FeedbackSubmitDTO feedbackSubmitDTO;
	private String testUsername;
	@InjectMocks
	private FeedbackController feedbackController;
	@Mock
	private FeedbackService feedbackService;

	/**
	 * method includes pre-processes for test cases
	 */
	@Before
	public void before() {
		testUsername = "admin77";

		mockMvc = MockMvcBuilders.standaloneSetup(feedbackController).build();

		feedbackSubmitDTO = new FeedbackSubmitDTO();
		feedbackSubmitDTO.setUsername(testUsername);
		feedbackSubmitDTO.setFeedback("feedback");

	}

	/**
	 * method includes post processes for test cases
	 */
	@After
	public void after() {
		mockMvc = null;
		feedbackSubmitDTO = null;
		testUsername = null;

	}

	/**
	 * method to test GET /feedback/categories restPoint ;
	 * 
	 * Get all feedback categories
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGetFeedbackCategories() throws Exception {
		mockMvc.perform(
				MockMvcRequestBuilders.get("/feedback/categories").contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk());
	}

	/**
	 * method to test POST /feedback/submitfeedback restPoint ; to submit all
	 * feedback
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSubmitFeedback() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.post("/feedback/submitfeedback")
				.content(mapper.writeValueAsString(feedbackSubmitDTO)).contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk());

	}
}
