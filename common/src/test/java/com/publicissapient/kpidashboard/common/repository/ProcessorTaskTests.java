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

package com.publicissapient.kpidashboard.common.repository;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import com.publicissapient.kpidashboard.common.executor.ProcessorJobExecutor;
import com.publicissapient.kpidashboard.common.model.generic.Processor;
import com.publicissapient.kpidashboard.common.repository.generic.ProcessorRepository;

@RunWith(MockitoJUnitRunner.class)
public class ProcessorTaskTests {

	private static final String COLLECTOR_NAME = "Test Processor";
	@Mock
	private TaskScheduler taskScheduler;
	@Mock
	private ProcessorRepository<Processor> processorRepository;
	private ProcessorJobExecutor<Processor> task;

	@Before
	public void init() {
		task = new TestProcessorJobExecutor();
	}

	@Test
	public void run_collectorNotRegistered_savesNewCollector() {
		Processor c = new Processor();
		when(processorRepository.findByProcessorName(COLLECTOR_NAME)).thenReturn(null);
		when(processorRepository.save(any(Processor.class))).thenReturn(c);
		task.run();
		verify(processorRepository).save(any(Processor.class));
	}

	@Test
	public void run_enabled() {
		Processor c = new Processor();
		c.setActive(true);
		long prevLastExecuted = c.getUpdatedTime();
		when(processorRepository.findByProcessorName(COLLECTOR_NAME)).thenReturn(c);
		when(processorRepository.save(any(Processor.class))).thenReturn(c);
		task.run();

		assertThat(c.getUpdatedTime(), greaterThan(prevLastExecuted));
		verify(processorRepository, times(1)).save(c);
	}

	@Test
	public void run_disabled() {
		Processor c = new Processor();
		c.setActive(false);
		when(processorRepository.findByProcessorName(COLLECTOR_NAME)).thenReturn(c);
		when(processorRepository.save(any(Processor.class))).thenReturn(c);
		task.run();

		verify(processorRepository, never()).save(c);
	}

	@Test
	public void onStartup() {
		Processor c = new Processor();
		c.setOnline(false);
		when(processorRepository.findByProcessorName(COLLECTOR_NAME)).thenReturn(c);
		task.onStartup();

		assertThat(c.isOnline(), is(true));
		verify(processorRepository, times(1)).save(c);
		verify(taskScheduler).schedule(any(TestProcessorJobExecutor.class), any(CronTrigger.class));
	}

	@Test
	public void onShutdown() {
		Processor c = new Processor();
		c.setOnline(true);
		when(processorRepository.findByProcessorName(COLLECTOR_NAME)).thenReturn(c);

		task.onShutdown();

		assertThat(c.isOnline(), is(false));
		verify(processorRepository, times(1)).save(c);
	}

	private final class TestProcessorJobExecutor extends ProcessorJobExecutor<Processor> {

		public TestProcessorJobExecutor() {
			super(taskScheduler, COLLECTOR_NAME);
		}

		@Override
		public Processor getProcessor() {
			return new Processor();
		}

		@Override
		public ProcessorRepository<Processor> getProcessorRepository() {
			return processorRepository;
		}

		@Override
		public String getCron() {
			return "0 * * * * *";
		}

		@Override
		public boolean execute(Processor processor) {
			return true;
		}

		@Override
		public boolean executeSprint(String sprintId) {
			return false;
		}
	}
}
