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

package com.publicissapient.kpidashboard.bitbucket.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.bitbucket.processor.service.BitBucketClient;
import com.publicissapient.kpidashboard.bitbucket.processor.service.impl.BitBucketCloudClient;
import com.publicissapient.kpidashboard.bitbucket.processor.service.impl.BitBucketServerClient;

/**
 * Provides factory to create Bitbucket Clients.
 *
 */
@Component
public class BitBucketClientFactory {

	private final BitBucketCloudClient bitBucketCloudClient;
	private final BitBucketServerClient bitBucketServerClient;

	/**
	 * Instantiate BitBucketClientFactory.
	 * 
	 * @param bitBucketCloudClient
	 *            Bitbucket cloud client
	 * @param bitBucketServerClient
	 *            Bitbucket server client
	 */
	@Autowired
	public BitBucketClientFactory(BitBucketCloudClient bitBucketCloudClient,
			BitBucketServerClient bitBucketServerClient) {
		this.bitBucketCloudClient = bitBucketCloudClient;
		this.bitBucketServerClient = bitBucketServerClient;
	}

	/**
	 * Provides the respective Sonar client based on connection type
	 * 
	 * @param cloudEnv
	 * @return the BitBucketClient
	 */
	public BitBucketClient getBitbucketClient(boolean cloudEnv) {
		BitBucketClient temp = null;
		if (cloudEnv) {
			temp = bitBucketCloudClient;
		} else {
			temp = bitBucketServerClient;
		}
		return temp;
	}

}
