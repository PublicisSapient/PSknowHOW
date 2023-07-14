package com.publicissapient.kpidashboard.apis.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author sanbhand1
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FeedbackSubmitDTO {
	private String username;
	private String feedback;
	private String category;
	private String feedbackType;

	@Override
	public String toString() {
		return "FeedbackSubmitDTO [username=" + this.username + ", feedback=" + this.feedback + "]";
	}

}
