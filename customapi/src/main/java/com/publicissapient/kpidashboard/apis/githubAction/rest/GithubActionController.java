package com.publicissapient.kpidashboard.apis.githubAction.rest;

import com.publicissapient.kpidashboard.apis.githubAction.model.GithubActionWorkflowsDTO;
import com.publicissapient.kpidashboard.apis.githubAction.service.GithubActionToolConfigServiceImpl;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class GithubActionController {


    @Autowired
    private GithubActionToolConfigServiceImpl githubActionToolConfigService;


    @GetMapping(value = "/githubAction/workflowName/{connectionId}/{repoName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ServiceResponse getGithubActionWorkflows(@PathVariable String connectionId,  @PathVariable String repoName) {
        ServiceResponse response;
        List<GithubActionWorkflowsDTO> workFlowList = githubActionToolConfigService.getGitHubWorkFlowList(connectionId, repoName);
        if (CollectionUtils.isEmpty(workFlowList)) {
            response = new ServiceResponse(false, "No workflow details found",
                    null);
        } else {
            response = new ServiceResponse(true, "FETCHED_SUCCESSFULLY", workFlowList);
        }
        return response;
    }
}
