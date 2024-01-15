/*
 *
 *   Copyright 2014 CapitalOne, LLC.
 *   Further development Copyright 2022 Sapient Corporation.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.publicissapient.kpidashboard.common.model.application;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

public class EmailServerDetailTest {
	@Mock
	List<String> feedbackEmailIds;
	@InjectMocks
	EmailServerDetail emailServerDetail;

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testSetEmailHost() throws Exception {
		emailServerDetail.setEmailHost("emailHost");
	}

	@Test
	public void testSetEmailPort() throws Exception {
		emailServerDetail.setEmailPort(0);
	}

	@Test
	public void testSetFromEmail() throws Exception {
		emailServerDetail.setFromEmail("fromEmail");
	}

	@Test
	public void testSetFeedbackEmailIds() throws Exception {
		emailServerDetail.setFeedbackEmailIds(Arrays.<String>asList("String"));
	}
}

// Generated with love by TestMe :) Please report issues and submit feature
// requests at: http://weirddev.com/forum#!/testme