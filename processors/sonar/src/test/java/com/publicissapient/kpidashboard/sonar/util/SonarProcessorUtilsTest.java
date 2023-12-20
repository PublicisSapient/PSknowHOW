/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package com.publicissapient.kpidashboard.sonar.util;

import com.publicissapient.kpidashboard.common.model.ToolCredential;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.service.ToolCredentialProvider;
import com.publicissapient.kpidashboard.sonar.data.ProjectToolConnectionFactory;
import com.publicissapient.kpidashboard.sonar.service.SonarToolCredentialProvider;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;

/**
 * @author shi6
 */
@RunWith(MockitoJUnitRunner.class)
public class SonarProcessorUtilsTest {
    @Mock
    public ToolCredentialProvider toolCredentialProvider;

	@Test
	public void getHeaders_BasicAuthTrue() {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Authorization", "Basic YWJjOg==");
		Assert.assertEquals(httpHeaders, SonarProcessorUtils.getHeaders("abc", true));
	}

	@Test
	public void getHeaders_BasicAuthFalse() {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Authorization", "Bearer abc");
		Assert.assertEquals(httpHeaders, SonarProcessorUtils.getHeaders("abc", false));
	}

    @Test
    public void testVault() {
        ProjectToolConnectionFactory toolConnectionFactory = ProjectToolConnectionFactory.newInstance();
        ProcessorToolConnection processorToolConnection = toolConnectionFactory.getProcessorToolConnectionList().get(3);
        ToolCredential credential= new ToolCredential();
        credential.setUsername("dummy");
        credential.setPassword("dummy");
        Mockito.when(toolCredentialProvider.findCredential(any())).thenReturn(credential);
        SonarUtils.getToolCredentials(toolCredentialProvider,processorToolConnection);
    }

    @Test
    public void testCloud() {
        ProjectToolConnectionFactory toolConnectionFactory = ProjectToolConnectionFactory.newInstance();
        ProcessorToolConnection processorToolConnection = toolConnectionFactory.getProcessorToolConnectionList().get(2);
        SonarUtils.getToolCredentials(toolCredentialProvider,processorToolConnection);
    }
}