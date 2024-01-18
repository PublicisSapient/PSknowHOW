package com.publicissapient.kpidashboard.common.constant;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProcessorConstantsTest {

    @Test
    public void testProcessorConstantsValues() {
        assertEquals("Jira", ProcessorConstants.JIRA);
        assertEquals("Azure", ProcessorConstants.AZURE);
        assertEquals("AzureBoards", ProcessorConstants.AZUREBOARDS);
        assertEquals("VersionOne", ProcessorConstants.VERSIONONE);
        assertEquals("Zephyr", ProcessorConstants.ZEPHYR);
        assertEquals("JiraTest", ProcessorConstants.JIRA_TEST);
        assertEquals("Bitbucket", ProcessorConstants.BITBUCKET);
        assertEquals("AzureRepository", ProcessorConstants.AZUREREPO);
        assertEquals("GitLab", ProcessorConstants.GITLAB);
        assertEquals("Bamboo", ProcessorConstants.BAMBOO);
        assertEquals("NewRelic", ProcessorConstants.NEWREILC);
        assertEquals("Excel", ProcessorConstants.EXCEL);
        assertEquals("Sonar", ProcessorConstants.SONAR);
        assertEquals("Jenkins", ProcessorConstants.JENKINS);
        assertEquals("AzurePipeline", ProcessorConstants.AZUREPIPELINE);
        assertEquals("Teamcity", ProcessorConstants.TEAMCITY);
        assertEquals("GitHub", ProcessorConstants.GITHUB);
        assertEquals("GitHubAction", ProcessorConstants.GITHUBACTION);
        assertEquals("RepoTool", ProcessorConstants.REPO_TOOLS);
    }
}
