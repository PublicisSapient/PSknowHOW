package com.publicissapient.kpidashboard.apis.data;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.publicissapient.kpidashboard.common.model.rbac.UserInfo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserInfoDataFactory {

	private static final String FILE_PATH_ACCOUNT_HIERARCHIES = "/json/default/user_info.json";
	private List<UserInfo> userInfoList;
	private ObjectMapper mapper;

	private UserInfoDataFactory() {
	}

	public static UserInfoDataFactory newInstance(String filePath) {

		UserInfoDataFactory factory = new UserInfoDataFactory();
		factory.createObjectMapper();
		factory.init(filePath);
		return factory;
	}

	public static UserInfoDataFactory newInstance() {

		return newInstance(null);
	}

	private void init(String filePath) {
		try {

			String resultPath = StringUtils.isEmpty(filePath) ? FILE_PATH_ACCOUNT_HIERARCHIES : filePath;

			userInfoList = mapper.readValue(TypeReference.class.getResourceAsStream(resultPath),
					new TypeReference<List<UserInfo>>() {
					});
		} catch (IOException e) {
			log.error("Error in reading account hierarchies from file = " + filePath, e);
		}
	}

	private void createObjectMapper() {

		if (mapper == null) {
			mapper = new ObjectMapper();
			mapper.registerModule(new JavaTimeModule());
			mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		}
	}

	public List<UserInfo> getAllUserInfo() {
		return userInfoList;
	}

	public UserInfo getUserInfoByRole(String role) {
		return userInfoList.stream().filter(user -> user.getAuthorities().contains(role)).findFirst().get();
	}
}
