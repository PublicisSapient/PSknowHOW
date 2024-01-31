package com.publicissapient.kpidashboard.notification.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Data
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

