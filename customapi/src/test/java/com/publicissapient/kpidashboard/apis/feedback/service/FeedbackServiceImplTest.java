package com.publicissapient.kpidashboard.apis.feedback.service;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.publicissapient.kpidashboard.common.service.NotificationService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.auth.model.Authentication;
import com.publicissapient.kpidashboard.apis.auth.repository.AuthenticationRepository;
import com.publicissapient.kpidashboard.apis.common.service.CommonService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.model.FeedbackSubmitDTO;
import com.publicissapient.kpidashboard.common.model.application.EmailServerDetail;
import com.publicissapient.kpidashboard.common.model.application.GlobalConfig;
import com.publicissapient.kpidashboard.common.repository.application.GlobalConfigRepository;

/**
 * @author sanbhand1
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class FeedbackServiceImplTest {

	/*
	 * Creating a new test FeedbackSubmitDTO object
	 */
	FeedbackSubmitDTO feedbackSubmitDTO = new FeedbackSubmitDTO();
	@InjectMocks
	private FeedbackServiceImpl feedbackServiceImpl;
	@Mock
	private CustomApiConfig customApiConfig;
	@Mock
	private CommonService commonService;
	@Mock
	private AuthenticationRepository authenticationRepository;
	@Mock
	private GlobalConfigRepository globalConfigRepository;
	@Mock
	private NotificationService notificationService;

	/**
	 * method includes preprocesses for test cases
	 */
	@Before
	public void setUp() {

		feedbackSubmitDTO.setUsername("testuser");
		feedbackSubmitDTO.setFeedback("feedback");
		feedbackSubmitDTO.setFeedbackType("Idea");
		feedbackSubmitDTO.setCategory("custom api");
	}

	/**
	 * to clean up the code
	 */
	@After
	public void cleanup() {
		feedbackSubmitDTO = new FeedbackSubmitDTO();
	}

	/**
	 * Get method to Test for all the categories
	 * 
	 */
	@Test
	public void getFeedbackCategoriesTest() {
		when(customApiConfig.getFeedbackCategories()).thenReturn(new ArrayList<>());
		List<String> response = feedbackServiceImpl.getFeedBackCategories();
		// assertThat("status: ", response.getSuccess(), equalTo(true));
		assertThat("Data should exist but empty: ", response, equalTo(new ArrayList<>()));
	}

	/**
	 * method to Test submit Feedback Request
	 * 
	 * @throws UnknownHostException
	 */
	@Test
	public void testSubmitFeedbackRequest() throws UnknownHostException {
		Authentication authentication = new Authentication();
		List<String> emailAddresses = new ArrayList<>();
		List<GlobalConfig> globalConfigs = new ArrayList<>();
		emailAddresses.add("abc@gmail.com");
		GlobalConfig globalConfig = new GlobalConfig();
		EmailServerDetail emailServerDetail = new EmailServerDetail();
		emailServerDetail.setFeedbackEmailIds(emailAddresses);
		globalConfig.setEmailServerDetail(emailServerDetail);
		globalConfigs.add(globalConfig);
		Map<String, String> customData = new HashMap<>();
		customData.put("abc", "dfe");
		when(customApiConfig.getFeedbackEmailSubject()).thenReturn("TEST_EMAILS");
		when(commonService.getApiHost()).thenReturn("host");
		when(authenticationRepository.findByUsername(Mockito.anyString())).thenReturn(authentication);
		when(globalConfigRepository.findAll()).thenReturn(globalConfigs);
		boolean response = feedbackServiceImpl.submitFeedback(feedbackSubmitDTO);
		assertThat("status: ", response, equalTo(true));

	}
}
