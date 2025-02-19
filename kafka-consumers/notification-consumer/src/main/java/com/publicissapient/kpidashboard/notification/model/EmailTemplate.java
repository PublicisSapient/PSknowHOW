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
package com.publicissapient.kpidashboard.notification.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * This class is responsible for creating email template to be send as json
 * payload
 *
 * @author purgupta2
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Data
@Slf4j
public class EmailTemplate implements Serializable {

	private List<Personalization> personalizations;
	private Sender from;
	private String subject;
	private List<Content> content;

	public static EmailTemplate fromEmailEvent(EmailEvent emailEvent, String html) {
		EmailTemplate emailTemplate = new EmailTemplate();

		// Map from EmailEvent to EmailTemplate
		Personalization personalization = new Personalization();
		List<Recipient> recipients = new ArrayList<>();
		for (String to : emailEvent.getTo()) {
			Recipient recipient = new Recipient();
			recipient.setEmail(to);
			recipients.add(recipient);
		}
		personalization.setTo(recipients);

		Sender sender = new Sender();
		sender.setEmail(emailEvent.getFrom());

		Content content = new Content();
		content.setType("text/html");
		content.setValue(html);

		emailTemplate.setPersonalizations(Collections.singletonList(personalization));
		emailTemplate.setFrom(sender);
		emailTemplate.setSubject(emailEvent.getSubject());
		emailTemplate.setContent(Collections.singletonList(content));
		log.info("EmailTemplate: " + emailTemplate.getPersonalizations().toString());

		return emailTemplate;
	}

	@Data
	public static class Recipient {
		private String email;
	}

	@Data
	public static class Personalization {
		private List<Recipient> to;
	}

	@Data
	public static class Sender {
		private String email;
	}

	@Data
	public static class Content {
		private String type;
		private String value;
	}
}
