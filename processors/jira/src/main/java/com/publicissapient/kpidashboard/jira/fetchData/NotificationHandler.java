package com.publicissapient.kpidashboard.jira.fetchData;

import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.EmailServerDetail;
import com.publicissapient.kpidashboard.common.model.application.GlobalConfig;
import com.publicissapient.kpidashboard.common.model.application.HierarchyValue;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.notification.EmailEvent;
import com.publicissapient.kpidashboard.common.model.rbac.ProjectsAccess;
import com.publicissapient.kpidashboard.common.model.rbac.UserInfo;
import com.publicissapient.kpidashboard.common.repository.application.GlobalConfigRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.rbac.UserInfoRepository;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class NotificationHandler {

    @Autowired
    JiraProcessorConfig jiraProcessorConfig;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private GlobalConfigRepository globalConfigRepository;

    @Autowired
    private ProjectBasicConfigRepository projectBasicConfigRepository;

    @Autowired
    private UserInfoRepository userInfoRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationHandler.class);

    private static final String NOTIFICATION_SUBJECT_KEY = "errorInJiraProcessor";

    private static final String NOTIFICATION_KEY = "Error_In_Jira_Processor";

    private static final String SUCCESS_MESSAGE = "Mail message to topic sent successfully";
    private static final String FAILURE_MESSAGE = "Error Sending the mail message to topic and the exception is: ";

    public static final String ROLE_PROJECT_ADMIN = "ROLE_PROJECT_ADMIN";

    public void sendEmailToProjectAdmin(String key, String value, String projectBasicConfigId) {
        List<String> emailAddresses = getProjectAdminEmailAddressBasedProjectId(projectBasicConfigId);
        emailAddresses.add("guptapurushottam123@gmail.com");

        Map<String, String> notificationSubjects = jiraProcessorConfig.getNotificationSubject();
        if (CollectionUtils.isNotEmpty(emailAddresses) && MapUtils.isNotEmpty(notificationSubjects)) {

            Map<String, String> customData = new HashMap<>();
            customData.put(key,value);
            String subject = notificationSubjects.get(NOTIFICATION_SUBJECT_KEY);
            log.info("Notification message sent to kafka with key : {}", NOTIFICATION_KEY);
            sendNotificationEvent(emailAddresses, customData, subject, NOTIFICATION_KEY,
                   jiraProcessorConfig.getKafkaMailTopic());
        } else {
            log.error("Notification Event not sent : No email address found associated with Superadmin role "
                    + "or Property - notificationSubject.accessRequest not set in property file ");
        }
    }

    private List<String> getProjectAdminEmailAddressBasedProjectId(String projectConfigId) {
        Set<String> emailAddresses = new HashSet<>();
        List<String> usernameList = new ArrayList<>();
        List<UserInfo> usersList = userInfoRepository.findByAuthoritiesIn(Arrays.asList(ROLE_PROJECT_ADMIN));
        Map<String, String> projectMap = getHierarchyMap(projectConfigId);
        if (CollectionUtils.isNotEmpty(usersList)) {
            usersList.forEach(action -> {
                Optional<ProjectsAccess> projectAccess = action.getProjectsAccess().stream()
                        .filter(access -> access.getRole().equalsIgnoreCase(ROLE_PROJECT_ADMIN)).findAny();
                if (projectAccess.isPresent()) {
                    projectAccess.get().getAccessNodes().stream().forEach(accessNode -> {
                        if (accessNode.getAccessItems().stream().anyMatch(item -> item.getItemId()
                                .equalsIgnoreCase(projectMap.get(accessNode.getAccessLevel())))) {
                            usernameList.add(action.getUsername());
                            emailAddresses.add(action.getEmailAddress());
                        }
                    });
                }
            });
        }

        return emailAddresses.stream().filter(StringUtils::isNotEmpty).collect(Collectors.toList());
    }

    private Map<String,String> getHierarchyMap(String projectConfigId){
        Map<String,String> map = new HashMap<>();
        Optional<ProjectBasicConfig> basicConfig = projectBasicConfigRepository.findById(new ObjectId(projectConfigId));
        if(basicConfig.isPresent()) {
            ProjectBasicConfig projectBasicConfig = basicConfig.get();
            CollectionUtils.emptyIfNull(projectBasicConfig.getHierarchy()).stream()
                    .sorted(Comparator.comparing(
                            (HierarchyValue hierarchyValue) -> hierarchyValue.getHierarchyLevel().getLevel()))
                    .forEach(hierarchyValue -> map.put(hierarchyValue.getHierarchyLevel().getHierarchyLevelId(),
                            hierarchyValue.getValue()));
            map.put(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT, projectBasicConfig.getId().toHexString());
        }

        return map;
    }

    private void sendNotificationEvent(List<String> emailAddresses, Map<String, String> customData, String notSubject,
                                      String notKey, String topic) {

        if (StringUtils.isNotBlank(notSubject)) {
            EmailServerDetail emailServerDetail = getEmailServerDetail();
            if (emailServerDetail != null) {
                EmailEvent emailEvent = new EmailEvent(emailServerDetail.getFromEmail(), emailAddresses, null, null,
                        notSubject, null, customData, emailServerDetail.getEmailHost(),
                        emailServerDetail.getEmailPort());
                sendNotificationEvent(notKey, emailEvent, null, topic);
            } else {
                log.error("Notification Event not sent : notification emailServer Details not found in db");
            }
        } else {
            log.error("Notification Event not sent : notification subject for {} not found in properties file",
                    notSubject);
        }

    }

    private EmailServerDetail getEmailServerDetail() {
        List<GlobalConfig> globalConfigs = globalConfigRepository.findAllByOrderByIdDesc();
        GlobalConfig globalConfig = CollectionUtils.isEmpty(globalConfigs) ? null : globalConfigs.get(0);
        return globalConfig == null ? null : globalConfig.getEmailServerDetail();
    }

    private void sendNotificationEvent(String key, EmailEvent email, Map<String, String> headerDetails, String topic) {
        if (jiraProcessorConfig.isNotificationSwitch()) {
            try {
                LOGGER.info(
                        "Notification Switch is on. Sending message now.....");
                ProducerRecord<String, Object> producerRecord = buildProducerRecord(key, email, headerDetails, topic);
                LOGGER.info(
                        "created producer record.....");
                ListenableFuture<SendResult<String, Object>> listenableFuture = kafkaTemplate.send(producerRecord);
                LOGGER.info(
                        "sent msg.....");
                listenableFuture.addCallback(new ListenableFutureCallback<SendResult<String, Object>>() {

                    @Override
                    public void onFailure(Throwable ex) {
                        handleFailure(ex);
                    }

                    @Override
                    public void onSuccess(SendResult<String, Object> result) {
                        handleSuccess(key, email, result);
                    }

                });
            }catch(Exception ex) {
                ex.printStackTrace();
            }
        } else {
            LOGGER.info(
                    "Notification Switch is Off. If want to send notification set true for notification.switch in property");
        }

    }

    private ProducerRecord<String, Object> buildProducerRecord(String key, EmailEvent email,
                                                               Map<String, String> headerDetails, String topic) {
        List<Header> recordHeaders = new ArrayList<>();
        if (MapUtils.isNotEmpty(headerDetails)) {
            headerDetails.forEach((k, v) -> {
                RecordHeader recordHeader = new RecordHeader(k, v.getBytes(StandardCharsets.UTF_8));
                recordHeaders.add(recordHeader);
            });
        }
        return new ProducerRecord<>(topic, null, key, email, recordHeaders);
    }

    private void handleFailure(Throwable ex) {
        LOGGER.error(FAILURE_MESSAGE + ex.getMessage(), ex);
    }

    private void handleSuccess(String key, EmailEvent email, SendResult<String, Object> result) {
        LOGGER.info(SUCCESS_MESSAGE + " key : {}, value : {}, Partition : {}", key, email.getSubject(),
                result.getRecordMetadata().partition());
    }

}
