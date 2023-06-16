package com.publicissapient.kpidashboard.zephyr.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.publicissapient.kpidashboard.common.service.AesEncryptionService;
import com.publicissapient.kpidashboard.zephyr.config.ZephyrConfig;

@ExtendWith(SpringExtension.class)
public class ZephyrUtilTest {

	private static final String KEY = "1231231231231234";
	private static final String PLAIN_TEXT = "test";
	private static final String ENCRYPTED_TEXT = "encryptedTest";
	@InjectMocks
	private ZephyrUtil zephyrUtil;
	@Mock
	private ZephyrConfig processorConfiguration;
	@Mock
	private AesEncryptionService aesEncryptionService;
	@Mock
	private ZephyrConfig zephyrConfig;

	@Test
	public void testBuildAuthenticationHeader() {
		assertNotNull(zephyrUtil.buildAuthenticationHeader("credentials"));
	}

	@Test
	public void testBuildAPIUrl() {
		when(processorConfiguration.getProtocol()).thenReturn("http");
		assertNotNull(zephyrUtil.buildAPIUrl("url", "api"));
	}

	@Test
	public void testBuildAuthenticationHeaderUsingToken() {
		assertNotNull(zephyrUtil.buildAuthHeaderUsingToken("accessToken"));
	}

	@Test
	public void testZephyrUrl() {
		assertEquals("test.com/jira", zephyrUtil.getZephyrUrl("https://test.com/jira/"));
	}

	@Test
	public void testDecryptPassword() {
		String result = PLAIN_TEXT;
		when(aesEncryptionService.decrypt(ENCRYPTED_TEXT, KEY)).thenReturn(PLAIN_TEXT);
		assertEquals(aesEncryptionService.decrypt(ENCRYPTED_TEXT, KEY), result);

	}

	@Test
	public void testBase64String() {
		String base64 = zephyrUtil.getCredentialsAsBase64String("user", "pwd");
		assertEquals("dXNlcjpudWxs", base64);

	}
}
