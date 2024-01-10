package com.publicissapient.kpidashboard.apis.common.service.impl;

import com.publicissapient.kpidashboard.common.constant.AuthType;
import com.publicissapient.kpidashboard.common.model.rbac.UserInfo;
import com.publicissapient.kpidashboard.common.model.rbac.UsersLoginHistory;
import com.publicissapient.kpidashboard.common.repository.rbac.UserLoginHistoryRepository;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserLoginHistoryServiceImplTest {

    @Mock
    private UserLoginHistoryRepository userLoginHistoryRepository;

    @InjectMocks
    private UserLoginHistoryServiceImpl userLoginHistoryService;

    @Test
    public void testCreateUserLoginHistoryInfo_ValidInput_Success() {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(new ObjectId());
        userInfo.setUsername("username");
        userInfo.setEmailAddress("emailAdd");
        userInfo.setAuthType(AuthType.STANDARD);
        String status = "SUCCESS";

        when(userLoginHistoryRepository.save(any(UsersLoginHistory.class))).thenReturn(new UsersLoginHistory());

        UsersLoginHistory result = userLoginHistoryService.createUserLoginHistoryInfo(userInfo, status);

        assertNotNull(result);
    }

}