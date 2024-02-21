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

package com.publicissapient.kpidashboard.common.executor;

import java.util.List;
import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.context.ExecutionLogContext;
import com.publicissapient.kpidashboard.common.model.generic.Processor;
import com.publicissapient.kpidashboard.common.repository.generic.ProcessorRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public abstract class ProcessorJobExecutor<T extends Processor> implements Runnable {

	private final TaskScheduler taskScheduler;
	private final String processorName;
	private List<String> projectsBasicConfigIds;
	private ExecutionLogContext executionLogContext;

	@Autowired
	protected ProcessorJobExecutor(TaskScheduler taskScheduler, String processorName) {
		this.taskScheduler = taskScheduler;
		this.processorName = processorName;
	}

	public ExecutionLogContext getExecutionLogContext() {
		return executionLogContext;
	}

	public void setExecutionLogContext(ExecutionLogContext executionLogContext) {
		this.executionLogContext = executionLogContext;
	}

	public void destroyLogContext() {
		this.executionLogContext.destroy();
		this.executionLogContext = null;

	}

	public List<String> getProjectsBasicConfigIds() {
		return projectsBasicConfigIds;
	}

	public void setProjectsBasicConfigIds(List<String> projectsBasicConfigIds) {
		this.projectsBasicConfigIds = projectsBasicConfigIds;
	}

	@Override
	public final synchronized void run() {
		setMDCContext();
		log.debug("Running Processor: {}", processorName);
		T processor = getProcessorRepository().findByProcessorName(processorName);
		if (processor == null) {
			// Register new processor
			processor = getProcessorRepository().save(getProcessor());
		} else {
			// In case the processor options changed via processors properties setup.
			// We want to keep the existing processors ID same as it ties to processor
			// items.
			T newProcessor = getProcessor();
			newProcessor.setId(processor.getId());
			newProcessor.setActive(processor.isActive());
			newProcessor.setProcessorType(processor.getProcessorType());
			newProcessor.setUpdatedTime(processor.getUpdatedTime());
			newProcessor.setProcessorName(processor.getProcessorName());
			newProcessor.setLastSuccess(processor.isLastSuccess());
			processor = getProcessorRepository().save(newProcessor);
		}

		if (processor.isActive()) {
			// Do collection run
			processor.setLastSuccess(execute(processor));
			log.debug("Saving the last executed status as: {} for {} processor!", processor.isLastSuccess(),
					processorName);
			// Update lastUpdate timestamp in Processor
			processor.setUpdatedTime(System.currentTimeMillis());
			getProcessorRepository().save(processor);
		}
	}

	public void runSprint(String sprintId) {
		boolean isSuccess = executeSprint(sprintId);
		log.debug("Saving the last executed status as: {} for {} sprint!", isSuccess, sprintId);
	}

	@PostConstruct
	public void onStartup() {
		taskScheduler.schedule(this, new CronTrigger(getCron()));
		setOnline(true);
	}

	@PreDestroy
	public void onShutdown() {
		setOnline(false);
		destroyLogContext();
	}

	public abstract T getProcessor();

	public abstract ProcessorRepository<T> getProcessorRepository();

	public abstract String getCron();

	public abstract boolean execute(T processor);
	public abstract boolean executeSprint(String sprintId);

	private void setOnline(boolean online) {
		T processor = getProcessorRepository().findByProcessorName(processorName);
		if (processor != null) {
			processor.setOnline(online);
			getProcessorRepository().save(processor);
		}
	}

	/**
	 * clean tool item map
	 * 
	 * @param url
	 *            url
	 * 
	 */
	public void clearToolItemCache(String url) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url);
		uriBuilder.path("/");
		uriBuilder.path(CommonConstant.CACHE_CLEAR_ENDPOINT);
		uriBuilder.path("/");
		uriBuilder.path(CommonConstant.CACHE_TOOL_CONFIG_MAP);

		HttpEntity<?> entity = new HttpEntity<>(headers);

		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> response = null;
		try {
			response = restTemplate.exchange(uriBuilder.toUriString(), HttpMethod.GET, entity, String.class);
		} catch (RestClientException e) {
			log.error("[TOOL-ITEM-CACHE-EVICT]. Error while consuming rest service {}", e);
		}

		if (null != response && response.getStatusCode().is2xxSuccessful()) {
			log.info("[TOOL-ITEM-CACHE-EVICT]. Successfully evicted cache: {} ", CommonConstant.CACHE_TOOL_CONFIG_MAP);
		} else {
			log.error("[TOOL-ITEM-CACHE-EVICT]. Error while evicting cache: {}", CommonConstant.CACHE_TOOL_CONFIG_MAP);
		}
	}

	private void setMDCContext() {
		ExecutionLogContext context = getExecutionLogContext();
		if (Objects.nonNull(context) && Objects.nonNull(context.getRequestId())) {
			ExecutionLogContext.updateContext(context);
		}
	}
}
