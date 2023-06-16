package com.publicissapient.kpidashboard.notification.service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import com.publicissapient.kpidashboard.notification.config.NotificationConsumerConfig;
import com.publicissapient.kpidashboard.notification.model.EmailEvent;

@Service
public class NotificationFailedMsgHandlerServiceImpl implements NotificationFailedMsgHandlerService {

	@Autowired
	KafkaTemplate<String, Object> kafkaTemplate;

	@Autowired
	private NotificationConsumerConfig notificationConsumerConfig;

	private static final Logger LOGGER = LoggerFactory.getLogger(NotificationFailedMsgHandlerServiceImpl.class);
	private static final String SUCCESS_MESSAGE = "Mail message to topic sent successfully";
	private static final String FAILURE_MESSAGE = "Error Sending the mail message to topic and the exception is: {}";

	public void handleFailedMessage(ConsumerRecord<String, EmailEvent> consumerRecord) {
		LOGGER.info("Persisting failed messages");
	}

	public void handleRecoverableMessage(ConsumerRecord<String, EmailEvent> consumerRecord) {
		String key = consumerRecord.key();
		EmailEvent email = consumerRecord.value();

		ProducerRecord<String, Object> producerRecord = buildProducerRecord(key, email, new HashMap<>());
		ListenableFuture<SendResult<String, Object>> listenableFuture = kafkaTemplate.send(producerRecord);
		listenableFuture.addCallback(new ListenableFutureCallback<SendResult<String, Object>>() {

			@Override
			public void onFailure(Throwable ex) {
				handleFailure(key, email, ex);
			}

			@Override
			public void onSuccess(SendResult<String, Object> result) {
				handleSuccess(key, email, result);
			}

		});
	}

	private ProducerRecord<String, Object> buildProducerRecord(String key, Object email,
			Map<String, String> headerDetails) {
		List<Header> recordHeaders = new ArrayList<>();
		if (MapUtils.isNotEmpty(headerDetails)) {
			headerDetails.forEach((k, v) -> {
				RecordHeader recordHeader = new RecordHeader(k, v.getBytes(StandardCharsets.UTF_8));
				recordHeaders.add(recordHeader);
			});
		}
		return new ProducerRecord<>(notificationConsumerConfig.getKafkaMailTopic(), null, key, email, recordHeaders);
	}

	private void handleFailure(String key, EmailEvent email, Throwable ex) {
		LOGGER.error(FAILURE_MESSAGE, ex.getMessage());
		try {
			throw ex;
		} catch (Throwable th) {
			LOGGER.error("Error in onFailure :{}", th.getMessage());
		}
	}

	private void handleSuccess(String key, EmailEvent email, SendResult<String, Object> result) {
		LOGGER.info(SUCCESS_MESSAGE + " key : {}, value : {}, Partition : {}", key, email.getSubject(),
				result.getRecordMetadata().partition());
	}
}