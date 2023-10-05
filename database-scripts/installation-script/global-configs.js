//Authentication types supported by the system, these can also be managed from settings section and SMTP details
db.global_config.insertOne({
                               "env": "production",
                               "authTypeStatus": {
                                 "standardLogin": true,
                                 "adLogin": false
                               },
                               "emailServerDetail": {
                                 "emailHost": "mail.example.com",
                                 "emailPort": 25,
                                 "fromEmail": "no-reply@example.com",
                                 "feedbackEmailIds": [
                                   "sampleemail@example.com"
                                 ]
                               },
                               "zephyrCloudBaseUrl": "https://api.zephyrscale.smartbear.com/v2/"
                             }
);

//list of tools supported
if (db.processor.countDocuments({}) === 0) {
    db.processor.insertMany([
                          {
                            "processorName": "Jira",
                            "processorType": "AGILE_TOOL",
                            "isActive": true,
                            "isOnline": true,
                            "errors": [],
                            "isLastSuccess": false,
                            "_class": "com.publicissapient.kpidashboard.jira.model.JiraProcessor"
                          },
                          {
                            "sonarKpiMetrics": [
                              "lines,ncloc,violations,new_vulnerabilities,critical_violations,major_violations,blocker_violations,minor_violations,info_violations,tests,test_success_density,test_errors,test_failures,coverage,line_coverage,sqale_index,alert_status,quality_gate_details,sqale_rating"
                            ],
                            "processorName": "Sonar",
                            "processorType": "SONAR_ANALYSIS",
                            "isActive": true,
                            "isOnline": true,
                            "errors": [],
                            "isLastSuccess": false,
                            "_class": "com.publicissapient.kpidashboard.sonar.model.SonarProcessor"
                          },
                          {
                            "processorName": "Zephyr",
                            "processorType": "TESTING_TOOLS",
                            "isActive": true,
                            "isOnline": true,
                            "errors": [],
                            "isLastSuccess": false,
                            "_class": "com.publicissapient.kpidashboard.zephyr.model.ZephyrProcessor"
                          },
                          {
                            "processorName": "GitHub",
                            "processorType": "SCM",
                            "isActive": true,
                            "isOnline": true,
                            "isLastSuccess": false,
                            "_class": "com.publicissapient.kpidashboard.github.model.GitHubProcessor"
                          },
                          {
                            "processorName": "Teamcity",
                            "processorType": "BUILD",
                            "isActive": true,
                            "isOnline": true,
                            "errors": [],
                            "isLastSuccess": false,
                            "_class": "com.publicissapient.kpidashboard.teamcity.model.TeamcityProcessor"
                          },
                          {
                            "processorName": "Bitbucket",
                            "processorType": "SCM",
                            "isActive": true,
                            "isOnline": true,
                            "isLastSuccess": false,
                            "_class": "com.publicissapient.kpidashboard.bitbucket.model.BitbucketProcessor"
                          },
                          {
                            "processorName": "GitLab",
                            "processorType": "SCM",
                            "isActive": true,
                            "isOnline": true,
                            "isLastSuccess": false,
                            "_class": "com.publicissapient.kpidashboard.gitlab.model.GitLabProcessor"
                          },
                          {
                            "processorName": "Jenkins",
                            "processorType": "BUILD",
                            "isActive": true,
                            "isOnline": true,
                            "isLastSuccess": false,
                            "_class": "com.publicissapient.kpidashboard.jenkins.model.JenkinsProcessor"
                          },
                          {
                            "processorName": "Bamboo",
                            "processorType": "BUILD",
                            "isActive": true,
                            "isOnline": true,
                            "isLastSuccess": false,
                            "_class": "com.publicissapient.kpidashboard.bamboo.model.BambooProcessor"
                          },
                          {
                            "processorName": "Azure",
                            "processorType": "AGILE_TOOL",
                            "isActive": true,
                            "isOnline": true,
                            "errors": [],
                            "isLastSuccess": false,
                            "_class": "com.publicissapient.kpidashboard.azure.model.AzureProcessor"
                          },
                          {
                            "processorName": "AzureRepository",
                            "processorType": "SCM",
                            "isActive": true,
                            "isOnline": true,
                            "errors": [],
                            "isLastSuccess": false,
                            "_class": "com.publicissapient.kpidashboard.azurerepo.model.AzureRepoProcessor"
                          },
                          {
                            "processorName": "AzurePipeline",
                            "processorType": "BUILD",
                            "isActive": true,
                            "isOnline": true,
                            "errors": [],
                            "isLastSuccess": false,
                            "_class": "com.publicissapient.kpidashboard.azurepipeline.model.AzurePipelineProcessor"
                          },
                          {
                            "processorName": "JiraTest",
                            "processorType": "TESTING_TOOLS",
                            "isActive": true,
                            "isOnline": true,
                            "errors": [],
                            "isLastSuccess": false,
                            "_class": "com.publicissapient.kpidashboard.jiratest.model.JiraTestProcessor"
                          },
                         {
                            "processorName": "GitHubAction",
                            "processorType": "BUILD",
                            "isActive": true,
                            "isOnline": true,
                            "errors": [],
                            "isLastSuccess": false,
                            "_class": "com.publicissapient.kpidashboard.githubaction.model.GitHubActionProcessor"
                          },
                          {
                            "processorName": "RepoTool",
                            "processorType": "SCM",
                            "isActive": true,
                            "isOnline": true,
                            "errors": [],
                            "isLastSuccess": false,
                        },
                        ]);
}