package com.publicissapient.kpidashboard.apis.githubaction.rest;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.apis.githubaction.model.GithubActionRepoDTO;
import com.publicissapient.kpidashboard.apis.githubaction.model.GithubActionWorkflowsDTO;
import com.publicissapient.kpidashboard.apis.githubaction.service.GithubActionToolConfigServiceImpl;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;

@RestController
public class GithubActionController {

	@Autowired
	private GithubActionToolConfigServiceImpl githubActionToolConfigService;

	@PostMapping(value = "/githubAction/workflowName/{connectionId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ServiceResponse getGithubActionWorkflows(@PathVariable String connectionId,
			@RequestBody GithubActionRepoDTO repoName) {
		ServiceResponse response;
		List<GithubActionWorkflowsDTO> workFlowList = githubActionToolConfigService.getGitHubWorkFlowList(connectionId,
				repoName.getRepositoryName());
		if (CollectionUtils.isEmpty(workFlowList)) {
			response = new ServiceResponse(false, "No workflow details found", null);
		} else {
			response = new ServiceResponse(true, "FETCHED_SUCCESSFULLY", workFlowList);
		}
		return response;
	}
}
