package com.publicissapient.kpidashboard.bamboo.processor;

import static org.hamcrest.Matchers.instanceOf;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.bamboo.client.BambooClient;
import com.publicissapient.kpidashboard.bamboo.client.impl.BambooClientBuildImpl;
import com.publicissapient.kpidashboard.bamboo.client.impl.BambooClientDeployImpl;
import com.publicissapient.kpidashboard.bamboo.factory.BambooClientFactory;

@RunWith(MockitoJUnitRunner.class)
public class BambooClientFactoryTest {

	@InjectMocks
	private BambooClientFactory factory;
	@Mock
	private BambooClientBuildImpl bambooClientBuild;

	@Mock
	private BambooClientDeployImpl bambooClientDeploy;

	@Test
	public void testGetBambooClient() throws Exception {
		BambooClient bambooClient = factory.getBambooClient("build");
		Assert.assertThat(bambooClient, instanceOf(BambooClientBuildImpl.class));
		bambooClient = factory.getBambooClient("deploy");
		Assert.assertThat(bambooClient, instanceOf(BambooClientDeployImpl.class));
	}

	@Test
	public void testGetBambooClientNull() {
		BambooClient bambooClient = null;
		try {
			bambooClient = factory.getBambooClient(null);
		} catch (Exception exception) {
			Assert.assertNull(bambooClient);
		}
	}

}
