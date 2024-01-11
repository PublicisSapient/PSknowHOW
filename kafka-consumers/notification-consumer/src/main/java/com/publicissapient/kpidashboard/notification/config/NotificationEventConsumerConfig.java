package com.publicissapient.kpidashboard.notification.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;
import org.thymeleaf.exceptions.TemplateInputException;

import com.publicissapient.kpidashboard.notification.model.EmailEvent;
import com.publicissapient.kpidashboard.notification.service.NotificationFailedMsgHandlerService;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableKafka
@Slf4j
public class NotificationEventConsumerConfig {

	@Value(value = "${spring.kafka.consumer.bootstrap-servers}")
	private String bootstrapAddress;

	@Value(value = "${spring.kafka.consumer.group-id}")
	private String mailGroup;

	@Autowired
	NotificationFailedMsgHandlerService notificationFailedMsgHandlerService;

	@Bean
	public ConsumerFactory<String, EmailEvent> consumerFactory() {
		JsonDeserializer<EmailEvent> deserializer = new JsonDeserializer<>(EmailEvent.class);
		deserializer.setRemoveTypeHeaders(false);
		deserializer.addTrustedPackages("*");
		deserializer.setUseTypeMapperForKey(true);

		Map<String, Object> config = new HashMap<>();

		config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
		config.put(ConsumerConfig.GROUP_ID_CONFIG, mailGroup);
		config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
		config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deserializer);
		return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), deserializer);
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, EmailEvent> kafkaListenerContainerFactory() {
		ConcurrentKafkaListenerContainerFactory<String, EmailEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory());
		factory.setCommonErrorHandler(new GlobalErrorHandler());
		factory.setCommonErrorHandler(defaultErrorHandler());
		return factory;
	}

	private DefaultErrorHandler defaultErrorHandler() {
		FixedBackOff fixedBackOff = new FixedBackOff(60000L, 3);
		DefaultErrorHandler errorHandler = new DefaultErrorHandler((consumerRecord, exception) -> {
			if (exception.getCause() instanceof RecoverableDataAccessException) {
				// invoke recovery logic
				log.info("Inside the recoverable logic");
				ConsumerRecord<String, EmailEvent> recoverableMessage = (ConsumerRecord<String, EmailEvent>) consumerRecord;
				notificationFailedMsgHandlerService.handleRecoverableMessage(recoverableMessage);
			} else {
				log.info("Inside the non recoverable logic");
				throw new IllegalArgumentException(exception.getMessage());
			}
		}, fixedBackOff);
		errorHandler.addRetryableExceptions(RecoverableDataAccessException.class);
		errorHandler.addRetryableExceptions(TimeoutException.class);
		errorHandler.addRetryableExceptions(TemplateInputException.class);
		errorHandler.addNotRetryableExceptions(IllegalArgumentException.class);
		return errorHandler;
	}
}