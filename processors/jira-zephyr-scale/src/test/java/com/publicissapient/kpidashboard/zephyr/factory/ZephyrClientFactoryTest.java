package com.publicissapient.kpidashboard.zephyr.factory;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.publicissapient.kpidashboard.zephyr.client.ZephyrClient;
import com.publicissapient.kpidashboard.zephyr.processor.service.impl.ZephyrCloudImpl;
import com.publicissapient.kpidashboard.zephyr.processor.service.impl.ZephyrServerImpl;

@ExtendWith(SpringExtension.class)
public class ZephyrClientFactoryTest {

	@InjectMocks
	private ZephyrClientFactory zephyrClientFactory;

	@Mock
	private ZephyrServerImpl zephyrServer;

	@Mock
	private ZephyrCloudImpl zephyrCloud;

	@Test
	public void getZephyrServerClient() {
		boolean cloudEnv = false;
		ZephyrClient zephyrClient = zephyrClientFactory.getClient(cloudEnv);
		assertEquals(zephyrClient, zephyrServer);
	}

	@Test
	public void getZephyrCloudClient() {
		boolean cloudEnv = true;
		ZephyrClient zephyrClient = zephyrClientFactory.getClient(cloudEnv);
		assertEquals(zephyrClient, zephyrCloud);
	}

}
