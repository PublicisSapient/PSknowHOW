package com.publicissapient.kpidashboard.apis.feedback.service;

import java.util.List;

import com.publicissapient.kpidashboard.apis.model.FeedbackSubmitDTO;

/**
 * @author sanbhand1
 *
 */
public interface FeedbackService {

	/**
	 * Create an method to submit feedback.
	 * 
	 * @param feedback
	 *
	 * @return responseEntity with message and status
	 */
	boolean submitFeedback(FeedbackSubmitDTO feedback, String loggedUserName);

	/**
	 * @return categories
	 */
	List<String> getFeedBackCategories();

}
