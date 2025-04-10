package com.publicissapient.kpidashboard.apis.feedback.rest;

import java.util.List;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.apis.auth.service.AuthenticationService;
import com.publicissapient.kpidashboard.apis.feedback.service.FeedbackService;
import com.publicissapient.kpidashboard.apis.model.FeedbackSubmitDTO;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * @author sanbhand1
 */
@RestController
@RequestMapping("/feedback")
@Slf4j
public class FeedbackController {

	/** Instantiates the SubmitFeedbackService */
	@Autowired
	private FeedbackService submitFeedbackService;

	@Autowired
	private AuthenticationService authenticationService;

	/**
	 * @return feedback categories
	 */
	@GetMapping("/categories")
	public ResponseEntity<ServiceResponse> getFeedbackCategories() {
		List<String> feedbackCategories = submitFeedbackService.getFeedBackCategories();
		log.info("Fetching data for Feedback Categories");
		return ResponseEntity.status(HttpStatus.OK)
				.body(new ServiceResponse(true, "Found all feedback categories", feedbackCategories));
	}

	/**
	 * Create an method to submit feedback.
	 *
	 * @return responseEntity with message and status
	 */
	@PostMapping("/submitfeedback")
	public ResponseEntity<ServiceResponse> submitFeedback(@Valid @RequestBody FeedbackSubmitDTO feedback,
			HttpServletRequest httpServletRequest) {
		log.info("creating new request");
		String loggedUserName = authenticationService.getLoggedInUser();
		if (loggedUserName != null) {
			boolean responseStatus = submitFeedbackService.submitFeedback(feedback, loggedUserName);
			if (responseStatus) {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ServiceResponse(responseStatus, "Your request has been submitted", feedback));
			} else {
				return ResponseEntity.status(HttpStatus.OK).body(
						new ServiceResponse(responseStatus, "Email Not Sent ,check emailId and Subject configuration ", feedback));
			}
		}
		return ResponseEntity.status(HttpStatus.OK)
				.body(new ServiceResponse(false, "User is not Valid for feedback", feedback));
	}
}
