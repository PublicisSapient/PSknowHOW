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

import org.junit.Test;

import java.time.LocalDateTime;
import java.time.Month;

public class CycleTimeTest {
	// Field intakeTime of type DateTime - was not mocked since Mockito doesn't mock
	// a Final class when 'mock-maker-inline' option is not set
	// Field readyTime of type DateTime - was not mocked since Mockito doesn't mock
	// a Final class when 'mock-maker-inline' option is not set
	// Field deliveryTime of type DateTime - was not mocked since Mockito doesn't
	// mock a Final class when 'mock-maker-inline' option is not set
	// Field liveTime of type DateTime - was not mocked since Mockito doesn't mock a
	// Final class when 'mock-maker-inline' option is not set
	// Field liveLocalDateTime of type LocalDateTime - was not mocked since Mockito
	// doesn't mock a Final class when 'mock-maker-inline' option is not set
	// Field readyLocalDateTime of type LocalDateTime - was not mocked since Mockito
	// doesn't mock a Final class when 'mock-maker-inline' option is not set
	// Field deliveryLocalDateTime of type LocalDateTime - was not mocked since
	// Mockito doesn't mock a Final class when 'mock-maker-inline' option is not set
	CycleTime cycleTime = new CycleTime(null, null, null, null, LocalDateTime.of(2024, Month.JANUARY, 11, 23, 43, 39),
			LocalDateTime.of(2024, Month.JANUARY, 11, 23, 43, 39),
			LocalDateTime.of(2024, Month.JANUARY, 11, 23, 43, 39), Long.valueOf(1), Long.valueOf(1), Long.valueOf(1),
			Long.valueOf(1), Long.valueOf(1));

	@Test
	public void testSetIntakeTime() throws Exception {
		cycleTime.setIntakeTime(null);
	}

	@Test
	public void testSetReadyTime() throws Exception {
		cycleTime.setReadyTime(null);
	}

	@Test
	public void testSetDeliveryTime() throws Exception {
		cycleTime.setDeliveryTime(null);
	}

	@Test
	public void testSetLiveTime() throws Exception {
		cycleTime.setLiveTime(null);
	}

	@Test
	public void testSetLiveLocalDateTime() throws Exception {
		cycleTime.setLiveLocalDateTime(LocalDateTime.of(2024, Month.JANUARY, 11, 23, 43, 39));
	}

	@Test
	public void testSetReadyLocalDateTime() throws Exception {
		cycleTime.setReadyLocalDateTime(LocalDateTime.of(2024, Month.JANUARY, 11, 23, 43, 39));
	}

	@Test
	public void testSetDeliveryLocalDateTime() throws Exception {
		cycleTime.setDeliveryLocalDateTime(LocalDateTime.of(2024, Month.JANUARY, 11, 23, 43, 39));
	}

	@Test
	public void testSetIntakeDor() throws Exception {
		cycleTime.setIntakeDor(Long.valueOf(1));
	}

	@Test
	public void testSetDorDod() throws Exception {
		cycleTime.setDorDod(Long.valueOf(1));
	}

	@Test
	public void testSetDodLive() throws Exception {
		cycleTime.setDodLive(Long.valueOf(1));
	}

	@Test
	public void testSetInProductiveState() throws Exception {
		cycleTime.setInProductiveState(Long.valueOf(1));
	}

	@Test
	public void testSetInWasteState() throws Exception {
		cycleTime.setInWasteState(Long.valueOf(1));
	}
}

