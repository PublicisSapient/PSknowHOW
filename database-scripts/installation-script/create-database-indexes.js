
// Creating Indexes on collection "jira_issue"

db.jira_issue.createIndex( {"basicProjectConfigId":1} )
db.jira_issue.createIndex( {"sprintID":1,"basicProjectConfigId":1,"typeName":1} )
db.jira_issue.createIndex( {"basicProjectConfigId":1,"typeName":1} )
db.jira_issue.createIndex( {"sprintID":1,"basicProjectConfigId":1,"typeName":1,"jiraStatus":1} )
db.jira_issue.createIndex( {"basicProjectConfigId":1,"typeName":1,"status":1} )
db.jira_issue.createIndex( {"basicProjectConfigId":1,"defectStoryID":1,"typeName":1} )
db.jira_issue.createIndex( {"basicProjectConfigId":1,"typeName":1,"defectStoryID":1} )
db.jira_issue.createIndex( {"basicProjectConfigId":1,"typeName":1,"rootCauseList":1} )
db.jira_issue.createIndex( {"basicProjectConfigId":1,"typeName":1,"number":1} )
db.jira_issue.createIndex( {"typeName":1,"defectStoryID":1} )
db.jira_issue.createIndex( {"sprintID":-1,"basicProjectConfigId":1} )

// Creating Indexes on collection "jira_issue_custom_history"

db.jira_issue_custom_history.createIndex({"storyID":1})
db.jira_issue_custom_history.createIndex( {"storyID":1,"basicProjectConfigId":1} )
db.jira_issue_custom_history.createIndex( {"basicProjectConfigId":1,"createdDate":-1} )
db.jira_issue_custom_history.createIndex( {"basicProjectConfigId":1,"createdDate":-1,"storyType":1} )
db.jira_issue_custom_history.createIndex( {"basicProjectConfigId":1,"storyType":1} )
db.jira_issue_custom_history.createIndex( {"basicProjectConfigId":1,"storySprintDetails.fromStatus":1} )
// Creating Indexes on collection "kanban_jira_issue"

db.kanban_jira_issue.createIndex( {"projectID":1} )
db.kanban_jira_issue.createIndex( {"projectID":1,"status":1} )
db.kanban_jira_issue.createIndex( {"projectID":1,"typeName":1} )
db.kanban_jira_issue.createIndex( {"projectID":1,"createdDate":-1} )
db.kanban_jira_issue.createIndex( {"processorId":1,"basicProjectConfigId":1,"changeDate":-1} )

// Creating Indexes on collection "kanban_issue_custom_history"

db.kanban_issue_custom_history.createIndex( {"basicProjectConfigId":1} )
db.kanban_issue_custom_history.createIndex( {"basicProjectConfigId":1,"storyType":1} )
db.kanban_issue_custom_history.createIndex( {"basicProjectConfigId":1,"typeName":1} )
db.kanban_issue_custom_history.createIndex( {"basicProjectConfigId":1,"status":1,"typeName":1} )
db.kanban_issue_custom_history.createIndex( {"basicProjectConfigId":1,"createdDate":-1} )
db.kanban_issue_custom_history.createIndex( {"processorId":1,"basicProjectConfigId":1,"changeDate":-1} )

// sprint details index
db.sprint_details.createIndex( {"sprintID":-1} )

// test case details index
db.test_case_details.createIndex({"basicProjectConfigId":1})
db.test_case_details.createIndex({"basicProjectConfigId":1,"defectStoryID":1})
db.test_case_details.createIndex({"basicProjectConfigId":1,"isTestCanBeAutomated":1,typeName:1})

//test execution index
db.test_execution.createIndex({"sprintId":1,"basicProjectConfigId":1})

// build details index
db.build_details.createIndex({"buildStatus":1,"startTime":1,"endTime":1})

// deployment index
db.deployments.createIndex({"deploymentStatus":1,"startTime":1,"endTime":1,"projectToolConfigId":1})
// Creating Indexes on collection "user_info"

db.user_info.createIndex( {"username":1} )    
db.user_info.createIndex( {"username":1,"authType":1} ) 

// Creating Indexes on collection "usertokendata"

db.usertokendata.createIndex( {"userToken":1} )

// unique index in sprintID
db.getCollection("sprint_details").createIndex({ "sprintID": 1 }, {
    "name": "IDX_UNQ_SPRINTID",
    "unique": true
})

// merge Request index
db.merge_requests.createIndex({"processorItemId":1,"createdDate":1, "fromBranch":1, "closedDate":1})

//processor items index
db.processor_items.createIndex({"toolConfigId":1})




