package com.publicissapient.kpidashboard.apis.githubAction.rest;

import com.publicissapient.kpidashboard.apis.bamboo.model.BambooDeploymentProjectsResponseDTO;
import com.publicissapient.kpidashboard.apis.githubAction.model.GithubActionWorkflowsDTO;
import com.publicissapient.kpidashboard.apis.githubAction.service.GithubActionToolConfigServiceImpl;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public class GithubActionController {


    @Autowired
    private GithubActionToolConfigServiceImpl githubActionToolConfigService;


    @GetMapping(value = "/githubAction/workflowName/{connectionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ServiceResponse getGithubActionWorkflows(@PathVariable String connectionId) {
        ServiceResponse response;
        List<GithubActionWorkflowsDTO> workFlowList = githubActionToolConfigService.getGitHubWorkFlowList(connectionId);
        if (CollectionUtils.isEmpty(workFlowList)) {
            response = new ServiceResponse(false, "No workflow details found",
                    null);
        } else {
            response = new ServiceResponse(true, "FETCHED_SUCCESSFULLY", workFlowList);
        }
        return response;
    }

    /**
     *
     * @param connectionId
     *            the bamboo server connection details
     * @return @{@code ServiceResponse}
     */
  /*  @GetMapping(value = "/githubAction/deploy/{connectionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ServiceResponse getBambooDeploymentProject(@PathVariable String connectionId) {
        ServiceResponse response;
        List<BambooDeploymentProjectsResponseDTO> projectKeyList = githubActionToolConfigService.getDeploymentProjectList(connectionId);
        if (CollectionUtils.isEmpty(projectKeyList)) {
            response = new ServiceResponse(false, "No deployment project found",
                    null);
        } else {
            response = new ServiceResponse(true, "FETCHED_SUCCESSFULLY", projectKeyList);
        }
        return response;
    }*/
}
