package com.publicissapient.kpidashboard.common.model.application;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * This class is email server details for notification server
 *
 * @author Hiren Babariya
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EmailServerDetail {

	private String emailHost;
	private int emailPort;
	private String fromEmail;
	private List<String> feedbackEmailIds;

}
