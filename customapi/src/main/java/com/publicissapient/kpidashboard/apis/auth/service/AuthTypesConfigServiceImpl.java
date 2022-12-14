package com.publicissapient.kpidashboard.apis.auth.service;

import com.publicissapient.kpidashboard.apis.activedirectory.service.ADServerDetailsService;
import com.publicissapient.kpidashboard.apis.auth.exceptions.InvalidAuthTypeConfigException;
import com.publicissapient.kpidashboard.apis.auth.token.TokenAuthenticationService;
import com.publicissapient.kpidashboard.apis.common.service.UserInfoService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.common.activedirectory.modal.ADServerDetail;
import com.publicissapient.kpidashboard.common.constant.AuthType;
import com.publicissapient.kpidashboard.common.model.application.AuthTypeConfig;
import com.publicissapient.kpidashboard.common.model.application.AuthTypeStatus;
import com.publicissapient.kpidashboard.common.model.application.GlobalConfig;
import com.publicissapient.kpidashboard.common.model.application.ValidationMessage;
import com.publicissapient.kpidashboard.common.model.rbac.UserInfo;
import com.publicissapient.kpidashboard.common.repository.application.GlobalConfigRepository;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;
import com.publicissapient.kpidashboard.common.service.RsaEncryptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthTypesConfigServiceImpl implements AuthTypesConfigService {

    @Autowired
    private ADServerDetailsService adServerDetailsService;

    @Autowired
    private GlobalConfigRepository globalConfigRepository;

    @Autowired
    private RsaEncryptionService rsaEncryptionService;

    @Autowired
    private AesEncryptionService aesEncryptionService;

    @Autowired
    private CustomApiConfig customApiConfig;

    @Autowired
    private AuthTypeConfigValidator authTypeConfigValidator;

    @Autowired
    private TokenAuthenticationService tokenAuthenticationService;

    @Autowired
    private UserInfoService userInfoService;



    @Override
    public AuthTypeConfig saveAuthTypeConfig(AuthTypeConfig authTypeConfig) {

        ValidationMessage validationMessage = authTypeConfigValidator.validateConfig(authTypeConfig);
        if (validationMessage.isValid()) {

            AuthTypeStatus authTypeStatus = authTypeConfig.getAuthTypeStatus();

            List<GlobalConfig> globalConfigs = globalConfigRepository.findAll();
            globalConfigs.get(0).setAuthTypeStatus(authTypeStatus);
            if (authTypeConfig.getAuthTypeStatus().isAdLogin()) {
                ADServerDetail adServerDetail = authTypeConfig.getAdServerDetail();
                String passEncrypt = encryptStringForDb(adServerDetail.getPassword());
                adServerDetail.setPassword(passEncrypt);
                globalConfigs.get(0).setAdServerDetail(adServerDetail);
            }

            globalConfigRepository.saveAll(globalConfigs);
            invalidateUsersAuthToken(authTypeStatus);
        } else {
            throw new InvalidAuthTypeConfigException(validationMessage.getMessage());
        }

        return authTypeConfig;
    }

    private void invalidateUsersAuthToken(AuthTypeStatus authTypeStatus) {
        List<String> usernames = new ArrayList<>();
        //find users
        if(!authTypeStatus.isStandardLogin()){
           List<UserInfo> standardUsers = userInfoService.getUserInfoByAuthType(AuthType.STANDARD.name());
           if (standardUsers != null && standardUsers.size() > 0){
               usernames.addAll(standardUsers.stream().map(UserInfo::getUsername).collect(Collectors.toList()));
           }
        }

        if(!authTypeStatus.isAdLogin()){
            List<UserInfo> adUsers = userInfoService.getUserInfoByAuthType(AuthType.LDAP.name());
            if (adUsers != null && adUsers.size() > 0){
                usernames.addAll(adUsers.stream().map(UserInfo::getUsername).collect(Collectors.toList()));
            }
        }
        tokenAuthenticationService.invalidateAuthToken(usernames);

    }

    @Override
    public AuthTypeConfig getAuthTypeConfig() {
        GlobalConfig globalConfig = getGlobalConfig();

        AuthTypeConfig authTypeConfig = new AuthTypeConfig();

        if (globalConfig != null) {
            authTypeConfig.setAdServerDetail(globalConfig.getAdServerDetail());
            authTypeConfig.setAuthTypeStatus(globalConfig.getAuthTypeStatus());
        }

        return authTypeConfig;
    }


    @Override
    public AuthTypeStatus getAuthTypesStatus() {
        GlobalConfig globalConfig = getGlobalConfig();
        return globalConfig != null ? globalConfig.getAuthTypeStatus() : null;
    }

    private GlobalConfig getGlobalConfig() {
        List<GlobalConfig> globalConfigs = globalConfigRepository.findAll();
        GlobalConfig globalConfig = CollectionUtils.isEmpty(globalConfigs) ? null : globalConfigs.get(0);
        return globalConfig;
    }

    private String encryptStringForDb(String rasEncryptedStringFromClient) {
        String plainText = rsaEncryptionService.decrypt(rasEncryptedStringFromClient,
                customApiConfig.getRsaPrivateKey());
        String encryptedString = aesEncryptionService.encrypt(plainText, customApiConfig.getAesEncryptionKey());
        return encryptedString == null ? "" : encryptedString;
    }
}
