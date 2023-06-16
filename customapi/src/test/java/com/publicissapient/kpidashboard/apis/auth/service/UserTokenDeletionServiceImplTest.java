package com.publicissapient.kpidashboard.apis.auth.service;

import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.common.repository.rbac.UserTokenReopository;

@RunWith(MockitoJUnitRunner.class)
public class UserTokenDeletionServiceImplTest {

	@InjectMocks
	UserTokenDeletionServiceImpl service;

	@Mock
	UserTokenReopository userTokenReopository;

	@Test
	public void validateDeleteUserDetails() {
		service.deleteUserDetails("token");
		verify(userTokenReopository).deleteByUserToken("token");
	}

	@Test
	public void validateInvalidateSession() {
		service.invalidateSession("user");
		verify(userTokenReopository).deleteByuserName("user");
	}
}
