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

package com.publicissapient.kpidashboard.apis.pushdata.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.bson.types.ObjectId;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.pushdata.model.PushDataResponse;
import com.publicissapient.kpidashboard.apis.pushdata.model.PushDataTraceLog;
import com.publicissapient.kpidashboard.apis.pushdata.model.dto.PushDataTraceLogDTO;
import com.publicissapient.kpidashboard.apis.pushdata.repository.PushDataTraceLogRepository;
import com.publicissapient.kpidashboard.apis.pushdata.service.PushDataTraceLogService;
import com.publicissapient.kpidashboard.apis.pushdata.util.PushDataException;

@Service
public class PushDataTraceLogServiceImpl implements PushDataTraceLogService {

	@Autowired
	private PushDataTraceLogRepository pushDataTraceLogRepository;

	@Override
	public void save(PushDataTraceLog pushDataTraceLog) {
		pushDataTraceLogRepository.save(pushDataTraceLog);
		PushDataTraceLog.destroy();
	}

	@Override
	public List<PushDataTraceLogDTO> getByProjectConfigId(ObjectId basicProjectConfigId) {
		List<PushDataTraceLog> byBasicProjectConfigId = pushDataTraceLogRepository
				.findByBasicProjectConfigId(basicProjectConfigId);
		List<PushDataTraceLogDTO> pushDataTraceLogDTO = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(byBasicProjectConfigId)) {
			byBasicProjectConfigId = byBasicProjectConfigId.stream()
					.sorted(Comparator.comparing(PushDataTraceLog::getRequestTime).reversed())
					.collect(Collectors.toList());
			ModelMapper modelMapper = new ModelMapper();
			byBasicProjectConfigId.stream().forEach(pushDataTraceLog -> pushDataTraceLogDTO
					.add(modelMapper.map(pushDataTraceLog, PushDataTraceLogDTO.class)));
			return pushDataTraceLogDTO;
		}
		return Collections.emptyList();
	}

	@Override
	public void setExceptionTraceLog(String unauthorizedAccessException, Object object) {
		PushDataTraceLog instance = PushDataTraceLog.getInstance();
		instance.setErrorMessage(unauthorizedAccessException);
		if (object instanceof HttpStatus) {
			HttpStatus code = (HttpStatus) object;
			instance.setResponseCode(String.valueOf(code.value()));
			instance.setResponseStatus(code.getReasonPhrase());
			save(instance);
			throw new PushDataException(unauthorizedAccessException, code);
		} else {
			instance.setResponseCode(String.valueOf(HttpStatus.BAD_REQUEST.value()));
			instance.setResponseStatus(HttpStatus.BAD_REQUEST.getReasonPhrase());
			save(instance);
			throw new PushDataException(unauthorizedAccessException, (PushDataResponse) object);
		}

	}

}
