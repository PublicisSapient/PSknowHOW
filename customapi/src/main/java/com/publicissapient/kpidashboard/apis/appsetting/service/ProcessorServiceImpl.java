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

package com.publicissapient.kpidashboard.apis.appsetting.service;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.publicissapient.kpidashboard.apis.appsetting.config.ProcessorUrlConfig;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionBasicConfig;
import com.publicissapient.kpidashboard.common.model.generic.Processor;
import com.publicissapient.kpidashboard.common.repository.generic.ProcessorRepository;

import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;

/**
 * This class provides various methods related to operations on Processor Data
 *
 * @author pansharm5
 */
@Service
@Slf4j
public class ProcessorServiceImpl implements ProcessorService {

	@Context
	HttpServletRequest httpServletRequest;
	@Autowired
	private ProcessorRepository<Processor> processorRepository;
	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private ProcessorUrlConfig processorUrlConfig;

	@Override
	public ServiceResponse getAllProcessorDetails() {
		List<Processor> listProcessor = new ArrayList<>();
		processorRepository.findAll().iterator().forEachRemaining(p -> {
			if (null != p) {
				listProcessor.add(p);
			}
		});
		log.debug("Returning list of Processors having size: {}", listProcessor.size());
		return new ServiceResponse(true, StringUtils.EMPTY, listProcessor);
	}

	@Override
	public ServiceResponse runProcessor(String processorName,
			ProcessorExecutionBasicConfig processorExecutionBasicConfig) {

		String url = processorUrlConfig.getProcessorUrl(processorName);
		boolean isSuccess = true;

		httpServletRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
		String token = httpServletRequest.getHeader("Authorization");
		token = CommonUtils.handleCrossScriptingTaintedValue(token);
		int statuscode = HttpStatus.NOT_FOUND.value();
		if (StringUtils.isNotEmpty(url)) {
			try {
				HttpHeaders headers = new HttpHeaders();
				headers.add("Authorization", token);

				HttpEntity<ProcessorExecutionBasicConfig> requestEntity = new HttpEntity<>(
						processorExecutionBasicConfig, headers);
				ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
				statuscode = resp.getStatusCode().value();
			} catch (HttpClientErrorException ex) {
				statuscode = ex.getStatusCode().value();
				isSuccess = false;
			} catch (ResourceAccessException ex) {
				isSuccess = false;
			}
		}
		if (HttpStatus.NOT_FOUND.value() == statuscode || HttpStatus.INTERNAL_SERVER_ERROR.value() == statuscode) {
			isSuccess = false;
		}
		return new ServiceResponse(isSuccess, "Got HTTP response: " + statuscode + " on url: " + url, null);
	}
}
