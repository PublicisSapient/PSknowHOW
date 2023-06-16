package com.publicissapient.kpidashboard.zephyr.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.zephyr.client.ZephyrClient;
import com.publicissapient.kpidashboard.zephyr.processor.service.impl.ZephyrCloudImpl;
import com.publicissapient.kpidashboard.zephyr.processor.service.impl.ZephyrServerImpl;

/**
 * Provides factory to create Zephyr Clients.
 *
 */
@Component
public class ZephyrClientFactory {

	@Autowired
	private ZephyrServerImpl zephyrServer;

	@Autowired
	private ZephyrCloudImpl zephyrCloud;

	public ZephyrClient getClient(boolean cloudEnv) {
		if (cloudEnv) {
			return zephyrCloud;
		} else {
			return zephyrServer;
		}
	}
}
