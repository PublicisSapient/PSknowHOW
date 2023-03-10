package com.publicissapient.kpidashboard.jira.fetchData;

import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ConsumeDataFromQueue {

    private static final String HEADER_X_RETRIES_COUNT = "x-retries-count";
    private static final Integer MAX_RETRIES_COUNT = 3;
    @Value("${rabbitmq.exchange.name}")
    String exchange;

    @Autowired
    private RabbitTemplate template;

    @RabbitListener(queues = {"${rabbitmq.queue.name}"})
    public void consumeJiraIssue(List<JiraIssue> jiraIssues){
        jiraIssues.stream().forEach(jiraIssue -> log.info("jiraissue Data {}",jiraIssue));
    }

    @RabbitListener(queues = "deadQueue")
    public void processFailedMessagesRetryHeaders(Message failedMessage) {
        Integer retriesCnt = (Integer) failedMessage.getMessageProperties()
                .getHeaders().get(HEADER_X_RETRIES_COUNT);
        if (retriesCnt == null) retriesCnt = 1;
        if (retriesCnt > MAX_RETRIES_COUNT) {
            log.info("Discarding message");
            return;
        }
        log.info("Retrying message for the {} time", retriesCnt);
        failedMessage.getMessageProperties()
                .getHeaders().put(HEADER_X_RETRIES_COUNT, ++retriesCnt);
        template.send(exchange,
                failedMessage.getMessageProperties().getReceivedRoutingKey(), failedMessage);
    }

}
