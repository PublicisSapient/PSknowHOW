package com.publicissapient.kpidashboard.apis.feedback.service;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.common.service.CommonService;
import com.publicissapient.kpidashboard.apis.common.service.UserInfoService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.enums.NotificationCustomDataEnum;
import com.publicissapient.kpidashboard.apis.model.FeedbackSubmitDTO;
import com.publicissapient.kpidashboard.common.model.application.EmailServerDetail;
import com.publicissapient.kpidashboard.common.model.application.GlobalConfig;
import com.publicissapient.kpidashboard.common.model.rbac.UserInfo;
import com.publicissapient.kpidashboard.common.repository.application.GlobalConfigRepository;
import com.publicissapient.kpidashboard.common.service.NotificationService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author sanbhand1
 *
 */
@Service
@Slf4j
public class FeedbackServiceImpl implements FeedbackService {

	private static final String NOTIFICATION_KEY = "Submit_Feedback";

	@Autowired
	private CustomApiConfig customApiConfig;

	@Autowired
	private CommonService commonService;

	@Autowired
	private UserInfoService userInfoService;

	@Autowired
	private GlobalConfigRepository globalConfigRepository;

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private KafkaTemplate<String, Object> kafkaTemplate;

	@Override
	public boolean submitFeedback(FeedbackSubmitDTO feedback, String loggedUserName) {
		boolean status = true;
		List<String> emailAddresses = null;

		List<GlobalConfig> globalConfigs = globalConfigRepository.findAll();
		GlobalConfig globalConfig = CollectionUtils.isEmpty(globalConfigs) ? null : globalConfigs.get(0);
		EmailServerDetail emailServerDetail = globalConfig == null ? null : globalConfig.getEmailServerDetail();
		if (emailServerDetail != null) {
			emailAddresses = emailServerDetail.getFeedbackEmailIds();
		} else {
			log.error("Notification Event not sent : notification emailServer Details not found in db");
		}
		String feedbackNotificationSubjects = customApiConfig.getFeedbackEmailSubject();
		if (CollectionUtils.isNotEmpty(emailAddresses) && (!(feedbackNotificationSubjects.isEmpty()))
				&& (!feedback.getFeedback().isEmpty())) {
			String serverPath = "";
			try {
				serverPath = commonService.getApiHost();
			} catch (UnknownHostException e) {
				status = false;
				log.error("SubmitFeedbackController: Server Host name is not bind with submit feedback Request mail ");
			}
			Map<String, String> customData = createCustomData(feedback, serverPath, loggedUserName);
			log.info("Notification message sent to kafka with key : {}", NOTIFICATION_KEY);
			String templateKey = customApiConfig.getMailTemplate().getOrDefault(NOTIFICATION_KEY, "");
			notificationService.sendNotificationEvent(emailAddresses, customData, feedbackNotificationSubjects,
					NOTIFICATION_KEY, customApiConfig.getKafkaMailTopic(), customApiConfig.isNotificationSwitch(),
					kafkaTemplate, templateKey, customApiConfig.isMailWithoutKafka());
		} else {
			status = false;
			log.error("Notification Event not sent : No email address "
					+ "or Property - notificationSubject.accessRequest not set in property file ");
		}

		return status;
	}

	private Map<String, String> createCustomData(FeedbackSubmitDTO feedback, String serverPath, String loggedUserName) {
		Map<String, String> customData = new HashMap<>();
		UserInfo userInfo = userInfoService.getUserInfo(loggedUserName);
		if (userInfo != null) {
			customData.put(NotificationCustomDataEnum.USER_NAME.getValue(), loggedUserName);
			customData.put(NotificationCustomDataEnum.USER_EMAIL.getValue(), userInfo.getEmailAddress());
		}
		customData.put(NotificationCustomDataEnum.SERVER_HOST.getValue(), serverPath);
		customData.put(NotificationCustomDataEnum.FEEDBACK_CONTENT.getValue(), feedback.getFeedback());
		return customData;
	}

	@Override
	public List<String> getFeedBackCategories() {
		return customApiConfig.getFeedbackCategories();
	}

}
