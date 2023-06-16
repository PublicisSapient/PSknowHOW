package com.publicissapient.kpidashboard.apis.cleanup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.ProcessorType;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;
import com.publicissapient.kpidashboard.common.repository.zephyr.TestCaseDetailsRepository;

@Service
public class ZephyrDataCleanUpService implements ToolDataCleanUpService {

	@Autowired
	private ProjectToolConfigRepository projectToolConfigRepository;

	@Autowired
	private TestCaseDetailsRepository testCaseDetailsRepository;

	@Autowired
	private CacheService cacheService;

	@Autowired
	private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepository;

	@Override
	public void clean(String projectToolConfigId) {
		ProjectToolConfig tool = projectToolConfigRepository.findById(projectToolConfigId);
		deleteTestCaseDetails(tool);
		// delete processors trace logs
		processorExecutionTraceLogRepository.deleteByBasicProjectConfigIdAndProcessorName(
				tool.getBasicProjectConfigId().toHexString(), tool.getToolName());
		clearCache();
	}

	@Override
	public String getToolCategory() {
		return ProcessorType.TESTING_TOOLS.toString();
	}

	private void deleteTestCaseDetails(ProjectToolConfig tool) {
		if (tool != null) {
			String basicProjectConfigId = tool.getBasicProjectConfigId().toHexString();
			testCaseDetailsRepository.deleteByBasicProjectConfigId(basicProjectConfigId);
		}
	}

	private void clearCache() {
		cacheService.clearCache(CommonConstant.TESTING_KPI_CACHE);
		cacheService.clearCache(CommonConstant.CACHE_TOOL_CONFIG_MAP);
	}
}
