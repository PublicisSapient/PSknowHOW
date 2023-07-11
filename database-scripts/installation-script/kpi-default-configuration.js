        db.metadata_identifier.insertMany([
										{
                                          "tool": "Jira",
                                          "templateName": "Standard Template",
                                          "templateCode": "7",
                                          "isKanban": false,
                                          "disabled": false,
                                          "issues": [
                                            {
                                              "type": "story",
                                              "value": [
                                                "Story",
                                                "Enabler Story",
                                                "Tech Story",
                                                "Change request"
                                              ]
                                            },
                                            {
                                              "type": "bug",
                                              "value": [
                                                "Defect",
                                                "Bug"
                                              ]
                                            },
                                            {
                                              "type": "epic",
                                              "value": [
                                                "Epic"
                                              ]
                                            },
                                            {
                                              "type": "issuetype",
                                              "value": [
                                                "Story",
                                                "Enabler Story",
                                                "Tech Story",
                                                "Change request",
                                                "Defect",
                                                "Bug",
                                                "Epic"
                                              ]
                                            },
                                            {
                                              "type": "uatdefect",
                                              "value": [
                                                "UAT Defect"
                                              ]
                                            }
                                          ],
                                          "customfield": [
                                            {
                                              "type": "storypoint",
                                              "value": [
                                                "Story Points"
                                              ]
                                            },
                                            {
                                              "type": "sprint",
                                              "value": [
                                                "Sprint"
                                              ]
                                            },
                                            {
                                              "type": "rootcause",
                                              "value": [
                                                "Root Cause"
                                              ]
                                            },
                                            {
                                              "type": "techdebt",
                                              "value": [
                                                "Tech Debt"
                                              ]
                                            },
                                            {
                                              "type": "uat",
                                              "value": [
                                                "UAT"
                                              ]
                                            },
                                            {
                                              "type": "timeCriticality",
                                              "value": [
                                                "Time Criticality"
                                              ]
                                            },
                                            {
                                              "type": "wsjf",
                                              "value": [
                                                "WSJF"
                                              ]
                                            },
                                            {
                                              "type": "costOfDelay",
                                              "value": [
                                                "Cost of Delay"
                                              ]
                                            },
                                            {
                                              "type": "businessValue",
                                              "value": [
                                                "User-Business Value"
                                              ]
                                            },
                                            {
                                              "type": "riskReduction",
                                              "value": [
                                                "Risk Reduction-Opportunity Enablement Value"
                                              ]
                                            },
                                            {
                                              "type": "jobSize",
                                              "value": [
                                                "Job Size"
                                              ]
                                            }
                                          ],
                                          "workflow": [
                                            {
                                              "type": "dor",
                                              "value": [
                                                "Ready for Sprint Planning",
                                                "In Progress"
                                              ]
                                            },
                                            {
                                              "type": "dod",
                                              "value": [
                                                "Closed",
                                                "Resolved",
                                                "Ready for Delivery"
                                              ]
                                            },
                                            {
                                              "type": "development",
                                              "value": [
                                                "Implementing",
                                                "In Development",
                                                "In Analysis"
                                              ]
                                            },
                                            {
                                              "type": "qa",
                                              "value": [
                                                "In Testing"
                                              ]
                                            },
                                            {
                                              "type": "firststatus",
                                              "value": [
                                                "Open"
                                              ]
                                            },
                                            {
                                              "type": "rejection",
                                              "value": [
                                                "Closed",
                                                "Rejected"
                                              ]
                                            },
                                            {
                                              "type": "delivered",
                                              "value": [
                                                "Closed",
                                                "Resolved",
                                                "Ready for Delivery",
                                                "Ready for Release"
                                              ]
                                            },
                                            {
                                              "type": "jiraWaitStatus",
                                              "value": [
                                                "Ready for Testing"
                                              ]
                                            },
                                            {
                                              "type": "jiraBlockedStatus",
                                              "value": [
                                                "On Hold",
                                                "Blocked"
                                              ]
                                            },
                                            {
                                              "type": "jiraStatusForInProgress",
                                              "value": [
                                                "In Analysis",
                                                "In Development",
                                                "In Progress"
                                              ]
                                            },
                                            {
                                              "type": "jiraStatusForClosed",
                                              "value": [
                                                "Closed",
                                                "CLOSED"
                                              ]
                                            }
                                          ],
                                          "valuestoidentify": [
                                            {
                                              "type": "rootCauseValue",
                                              "value": [
                                                "Coding"
                                              ]
                                            },
                                            {
                                              "type": "rejectionResolution",
                                              "value": [
                                                "Invalid",
                                                "Duplicate",
                                                "Unrequired"
                                              ]
                                            },
                                            {
                                              "type": "qaRootCause",
                                              "value": [
                                                "Coding",
                                                "Configuration",
                                                "Regression",
                                                "Data"
                                              ]
                                            }
                                          ]
                                        },
                                        {
                                          "tool": "Jira",
                                          "templateName": "Standard Template",
                                          "templateCode": "8",
                                          "isKanban": true,
                                          "disabled": false,
                                          "issues": [
                                            {
                                              "type": "epic",
                                              "value": [
                                                "Epic"
                                              ]
                                            },
                                            {
                                              "type": "issuetype",
                                              "value": [
                                                "Support Request",
                                                "Incident",
                                                "Project Request",
                                                "Member Account Request",
                                                "DOJO Consulting Request",
                                                "Ticket",
                                                "Bug",
                                                "Change Request",
                                                "Tech Story",
                                                "Question",
                                                "Request",
                                                "Issue",
                                                "Defect",
                                                "Story"
                                              ]
                                            },
                                            {
                                              "type": "uatdefect",
                                              "value": [
                                                "UAT Defect"
                                              ]
                                            },
                                            {
                                              "type": "ticketVelocityStatusIssue",
                                              "value": [
                                                "Support Request",
                                                "Incident",
                                                "Project Request",
                                                "Member Account Request",
                                                "DOJO Consulting Request",
                                                "Ticket",
                                                "Bug",
                                                "Change Request",
                                                "Tech Story",
                                                "Question",
                                                "Request",
                                                "Issue",
                                                "Defect",
                                                "Story"
                                              ]
                                            },
                                            {
                                              "type": "ticketThroughputIssue",
                                              "value": [
                                                "Support Request",
                                                "Incident",
                                                "Project Request",
                                                "Member Account Request",
                                                "DOJO Consulting Request",
                                                "Ticket",
                                                "Bug",
                                                "Change Request",
                                                "Tech Story",
                                                "Question",
                                                "Request",
                                                "Issue",
                                                "Defect",
                                                "Story"
                                              ]
                                            },
                                            {
                                              "type": "ticketWipClosedIssue",
                                              "value": [
                                                "Support Request",
                                                "Incident",
                                                "Project Request",
                                                "Member Account Request",
                                                "DOJO Consulting Request",
                                                "Ticket",
                                                "Bug",
                                                "Change Request",
                                                "Tech Story",
                                                "Question",
                                                "Request",
                                                "Issue",
                                                "Defect",
                                                "Story"
                                              ]
                                            },
                                            {
                                              "type": "ticketReopenIssue",
                                              "value": [
                                                "Support Request",
                                                "Incident",
                                                "Project Request",
                                                "Member Account Request",
                                                "DOJO Consulting Request"
                                              ]
                                            },
                                            {
                                              "type": "kanbanCycleTimeIssue",
                                              "value": [
                                                "Support Request",
                                                "Incident",
                                                "Project Request",
                                                "Member Account Request",
                                                "DOJO Consulting Request",
                                                "Ticket",
                                                "Bug",
                                                "Change Request",
                                                "Tech Story",
                                                "Question",
                                                "Request",
                                                "Issue",
                                                "Defect",
                                                "Story"
                                              ]
                                            },
                                            {
                                              "type": "kanbanTechDebtIssueType",
                                              "value": [
                                                "Support Request",
                                                "Incident",
                                                "Project Request",
                                                "Member Account Request",
                                                "DOJO Consulting Request",
                                                "Ticket",
                                                "Bug",
                                                "Change Request",
                                                "Tech Story",
                                                "Question",
                                                "Request",
                                                "Issue",
                                                "Defect",
                                                "Story"
                                              ]
                                            }
                                          ],
                                          "customfield": [
                                            {
                                              "type": "storypoint",
                                              "value": [
                                                "Story Points"
                                              ]
                                            },
                                            {
                                              "type": "rootcause",
                                              "value": [
                                                "Root Cause"
                                              ]
                                            },
                                            {
                                              "type": "techdebt",
                                              "value": [
                                                "Tech Debt"
                                              ]
                                            },
                                            {
                                              "type": "uat",
                                              "value": [
                                                "UAT"
                                              ]
                                            },
                                            {
                                              "type": "timeCriticality",
                                              "value": [
                                                "Time Criticality"
                                              ]
                                            },
                                            {
                                              "type": "wsjf",
                                              "value": [
                                                "WSJF"
                                              ]
                                            },
                                            {
                                              "type": "costOfDelay",
                                              "value": [
                                                "Cost of Delay"
                                              ]
                                            },
                                            {
                                              "type": "businessValue",
                                              "value": [
                                                "User-Business Value"
                                              ]
                                            },
                                            {
                                              "type": "riskReduction",
                                              "value": [
                                                "Risk Reduction-Opportunity Enablement Value"
                                              ]
                                            },
                                            {
                                              "type": "jobSize",
                                              "value": [
                                                "Job Size"
                                              ]
                                            }
                                          ],
                                          "workflow": [
                                            {
                                              "type": "firststatus",
                                              "value": [
                                                "Open"
                                              ]
                                            },
                                            {
                                              "type": "rejection",
                                              "value": [
                                                "Rejected",
                                                "Closed"
                                              ]
                                            },
                                            {
                                              "type": "delivered",
                                              "value": [
                                                "Closed",
                                                "Resolved",
                                                "Ready for Delivery",
                                                "Ready for Release"
                                              ]
                                            },
                                            {
                                              "type": "ticketReopenStatus",
                                              "value": [
                                                "Reopened"
                                              ]
                                            },
                                            {
                                              "type": "ticketResolvedStatus",
                                              "value": [
                                                "Resolved",
                                                "Closed",
                                                "CLOSED"
                                              ]
                                            },
                                            {
                                              "type": "ticketClosedStatus",
                                              "value": [
                                                "Closed",
                                                "CLOSED"
                                              ]
                                            },
                                            {
                                              "type": "ticketWIPStatus",
                                              "value": [
                                                "In Progress",
                                                "In Development"
                                              ]
                                            },
                                            {
                                              "type": "ticketRejectedStatus",
                                              "value": [
                                                "Dropped",
                                                "Rejected"
                                              ]
                                            },
                                            {
                                              "type": "ticketTriagedStatus",
                                              "value": [
                                                "Assigned",
                                                "REVIEWING",
                                                "In Progress",
                                                "OPEN",
                                                "IN ANALYSIS",
                                                "Approval for Implementation",
                                                "ACCEPTED",
                                                "In Development"
                                              ]
                                            }
                                          ],
                                          "valuestoidentify": [
                                            {
                                              "type": "rootCauseValue",
                                              "value": [
                                                "Coding"
                                              ]
                                            },
                                            {
                                              "type": "rejectionResolution",
                                              "value": [
                                                "Invalid",
                                                "Duplicate",
                                                "Unrequired"
                                              ]
                                            },
                                            {
                                              "type": "qaRootCause",
                                              "value": [
                                                "Coding",
                                                "Configuration",
                                                "Regression",
                                                "Data"
                                              ]
                                            }
                                          ]
                                        },
                                        {
                                          "tool": "Azure",
                                          "isKanban": false,
                                          "issues": [
                                            {
                                              "type": "story",
                                              "value": [
                                                "User Story",
                                                "Task",
                                                "Feature",
                                                "Epic"
                                              ]
                                            },
                                            {
                                              "type": "bug",
                                              "value": [
                                                "Bug",
                                                "Issue"
                                              ]
                                            },
                                            {
                                              "type": "epic",
                                              "value": [
                                                "Epic"
                                              ]
                                            },
                                            {
                                              "type": "issuetype",
                                              "value": [
                                                "User Story",
                                                "Task",
                                                "Feature",
                                                "Issue",
                                                "Bug",
                                                "Epic"
                                              ]
                                            },
                                            {
                                              "type": "epic",
                                              "value": [
                                                "Epic"
                                              ]
                                            },
                                            {
                                              "type": "uatdefect",
                                              "value": [
                                                "UAT Defect"
                                              ]
                                            }
                                          ],
                                          "customfield": [
                                            {
                                              "type": "rootcause",
                                              "value": [
                                                "RootCause"
                                              ]
                                            },
                                            {
                                              "type": "techdebt",
                                              "value": [
                                                "Tech Debt"
                                              ]
                                            },
                                            {
                                              "type": "uat",
                                              "value": [
                                                "UAT"
                                              ]
                                            },
                                            {
                                              "type": "timeCriticality",
                                              "value": [
                                                "Time Criticality"
                                              ]
                                            },
                                            {
                                              "type": "wsjf",
                                              "value": [
                                                "WSJF Score"
                                              ]
                                            },
                                            {
                                              "type": "businessValue",
                                              "value": [
                                                "Business Value"
                                              ]
                                            },
                                            {
                                              "type": "riskReduction",
                                              "value": [
                                                "Risk Reduction/Opportunity Enablement"
                                              ]
                                            },
                                            {
                                              "type": "jobSize",
                                              "value": [
                                                "Effort"
                                              ]
                                            },
                                            {
                                              "type": "storypoint",
                                              "value": [
                                                "Effort"
                                              ]
                                            }
                                          ],
                                          "workflow": [
                                            {
                                              "type": "dor",
                                              "value": [
                                                "Active"
                                              ]
                                            },
                                            {
                                              "type": "dod",
                                              "value": [
                                                "Resolved",
                                                "Closed",
                                                "Removed",
                                                "Done"
                                              ]
                                            },
                                            {
                                              "type": "development",
                                              "value": [
                                                "Active",
                                                "In Development",
                                                "In Analysis"
                                              ]
                                            },
                                            {
                                              "type": "qa",
                                              "value": [
                                                "In Testing"
                                              ]
                                            },
                                            {
                                              "type": "firststatus",
                                              "value": [
                                                "New",
                                                "To do"
                                              ]
                                            },
                                            {
                                              "type": "rejection",
                                              "value": [
                                                "Rejected",
                                                "Closed"
                                              ]
                                            },
                                            {
                                              "type": "delivered",
                                              "value": [
                                                "Closed",
                                                "Ready for Delivery",
                                                "Ready for Release"
                                              ]
                                            }
                                          ],
                                          "valuestoidentify": [
                                            {
                                              "type": "rootCauseValue",
                                              "value": [
                                                "Coding"
                                              ]
                                            },
                                            {
                                              "type": "rejectionResolution",
                                              "value": [
                                                "Invalid",
                                                "Duplicate",
                                                "Unrequired",
                                                "Removed"
                                              ]
                                            },
                                            {
                                              "type": "qaRootCause",
                                              "value": [
                                                "Coding",
                                                "Configuration",
                                                "Regression",
                                                "Data"
                                              ]
                                            }
                                          ]
                                        },
                                        {
                                          "tool": "Jira",
                                          "templateName": "Custom Template",
                                          "templateCode": "9",
                                          "isKanban": true,
                                          "disabled": true
                                        },
                                        {
                                          "tool": "Jira",
                                          "templateName": "Custom Template",
                                          "templateCode": "10",
                                          "isKanban": false,
                                          "disabled": true
                                        },
                                        {
                                          "tool": "Azure",
                                          "isKanban": true,
                                          "issues": [
                                            {
                                              "type": "story",
                                              "value": [
                                                "User Story",
                                                "Task",
                                                "Feature"
                                              ]
                                            },
                                            {
                                              "type": "bug",
                                              "value": [
                                                "Bug",
                                                "Issue"
                                              ]
                                            },
                                            {
                                              "type": "epic",
                                              "value": [
                                                "Epic"
                                              ]
                                            },
                                            {
                                              "type": "issuetype",
                                              "value": [
                                                "User Story",
                                                "Task",
                                                "Feature",
                                                "Epic",
                                                "Issue",
                                                "Bug",
                                                "Test Case",
                                                "Epic"
                                              ]
                                            },
                                            {
                                              "type": "epic",
                                              "value": [
                                                "Epic"
                                              ]
                                            },
                                            {
                                              "type": "uatdefect",
                                              "value": [
                                                "UAT Defect",
                                                "Bug"
                                              ]
                                            }
                                          ],
                                          "customfield": [
                                            {
                                              "type": "rootcause",
                                              "value": [
                                                "RootCause"
                                              ]
                                            },
                                            {
                                              "type": "techdebt",
                                              "value": [
                                                "Tech Debt"
                                              ]
                                            },
                                            {
                                              "type": "uat",
                                              "value": [
                                                "UAT"
                                              ]
                                            },
                                            {
                                              "type": "timeCriticality",
                                              "value": [
                                                "Time Criticality"
                                              ]
                                            },
                                            {
                                              "type": "wsjf",
                                              "value": [
                                                "WSJF Score"
                                              ]
                                            },
                                            {
                                              "type": "businessValue",
                                              "value": [
                                                "Business Value"
                                              ]
                                            },
                                            {
                                              "type": "riskReduction",
                                              "value": [
                                                "Risk Reduction/Opportunity Enablement"
                                              ]
                                            },
                                            {
                                              "type": "jobSize",
                                              "value": [
                                                "Effort"
                                              ]
                                            }
                                          ],
                                          "workflow": [
                                            {
                                              "type": "dor",
                                              "value": [
                                                "Active"
                                              ]
                                            },
                                            {
                                              "type": "dod",
                                              "value": [
                                                "Resolved",
                                                "Closed"
                                              ]
                                            },
                                            {
                                              "type": "development",
                                              "value": [
                                                "Active",
                                                "In Development",
                                                "In Analysis"
                                              ]
                                            },
                                            {
                                              "type": "qa",
                                              "value": [
                                                "In Testing"
                                              ]
                                            },
                                            {
                                              "type": "firststatus",
                                              "value": [
                                                "New"
                                              ]
                                            },
                                            {
                                              "type": "rejection",
                                              "value": [
                                                "Rejected"
                                              ]
                                            },
                                            {
                                              "type": "delivered",
                                              "value": [
                                                "Closed",
                                                "Ready for Delivery",
                                                "Ready for Release"
                                              ]
                                            },
                                            {
                                              "type": "ticketReopenStatus",
                                              "value": [
                                                "Reopened"
                                              ]
                                            },
                                            {
                                              "type": "ticketResolvedStatus",
                                              "value": [
                                                "Resolved",
                                                "Closed"
                                              ]
                                            },
                                            {
                                              "type": "ticketClosedStatus",
                                              "value": [
                                                "Closed"
                                              ]
                                            },
                                            {
                                              "type": "ticketWIPStatus",
                                              "value": [
                                                "Active",
                                                "In Progress",
                                                "In Development"
                                              ]
                                            },
                                            {
                                              "type": "ticketRejectedStatus",
                                              "value": [
                                                "Dropped",
                                                "Rejected"
                                              ]
                                            },
                                            {
                                              "type": "ticketTriagedStatus",
                                              "value": [
                                                "Active",
                                                "In Progress",
                                                "Accepted",
                                                "In Development"
                                              ]
                                            }
                                          ],
                                          "valuestoidentify": [
                                            {
                                              "type": "rootCauseValue",
                                              "value": [
                                                "Coding"
                                              ]
                                            },
                                            {
                                              "type": "rejectionResolution",
                                              "value": [
                                                "Invalid",
                                                "Duplicate",
                                                "Unrequired"
                                              ]
                                            },
                                            {
                                              "type": "qaRootCause",
                                              "value": [
                                                "Coding",
                                                "Configuration",
                                                "Regression",
                                                "Data"
                                              ]
                                            }
                                          ]
                                        }
                                      ]);
