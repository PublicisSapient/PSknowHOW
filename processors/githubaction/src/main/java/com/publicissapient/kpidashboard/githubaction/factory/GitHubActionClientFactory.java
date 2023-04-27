package com.publicissapient.kpidashboard.githubaction.factory;


import com.publicissapient.kpidashboard.githubaction.processor.adapter.GitHubActionClient;
import com.publicissapient.kpidashboard.githubaction.processor.adapter.impl.GitHubActionBuildClient;
import com.publicissapient.kpidashboard.githubaction.processor.adapter.impl.GitHubActionDeployClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GitHubActionClientFactory {

    private static final String BUILD = "build";
    private static final String DEPLOY = "deploy";
    private final GitHubActionBuildClient buildClient;
    private final GitHubActionDeployClient deployClient;

    @Autowired
    public GitHubActionClientFactory(GitHubActionBuildClient buildClient, GitHubActionDeployClient deployClient) {
        this.buildClient = buildClient;
        this.deployClient = deployClient;
    }

    public GitHubActionClient getGitHubActionClient(String jobType) {
        GitHubActionClient gitHubActionClient = null;
        if (jobType.equalsIgnoreCase(BUILD)) {
            gitHubActionClient = buildClient;
        } else if (jobType.equalsIgnoreCase(DEPLOY)) {
            gitHubActionClient = deployClient;
        }
        return gitHubActionClient;
    }
}
