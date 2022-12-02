function hasTestConfigInFieldMapping(fm){
    return fm.jiraTestCaseType && fm.jiraTestCaseType.length !== 0;
}

function createTestTool(jiraTool, fm){
    var testTool = {};
    testTool["toolName"] = "JiraTest";
    testTool["basicProjectConfigId"] = fm.basicProjectConfigId;
    testTool["connectionId"] = jiraTool.connectionId;
    testTool["projectKey"] = jiraTool.projectKey;
    testTool["createdAt"] = new Date();
    testTool["updatedAt"] = new Date();
    testTool["testAutomated"] = fm.testAutomated;
    testTool["jiraTestCaseType"] = fm.jiraTestCaseType;
    testTool["testAutomatedIdentification"] = fm.testAutomatedIdentification;
    testTool["testAutomationCompletedIdentification"] = fm.testAutomationCompletedIdentification;
    testTool["testRegressionIdentification"] = fm.testRegressionIdentification;
    testTool["testAutomationCompletedByCustomField"] = fm.testAutomationCompletedByCustomField;
    testTool["testRegressionByCustomField"] = fm.testRegressionByCustomField;
    testTool["jiraAutomatedTestValue"] = fm.jiraAutomatedTestValue;
    testTool["jiraRegressionTestValue"] = fm.jiraRegressionTestValue;
    testTool["jiraCanBeAutomatedTestValue"] = fm.jiraCanBeAutomatedTestValue;
    testTool["testCaseStatus"] = fm.testCaseStatus;
    testTool["_class"] = "com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig";

   return testTool;

}
var fieldMappings = db.field_mapping.find({});

fieldMappings.forEach(function(fm){
    if(hasTestConfigInFieldMapping(fm)){
       var project = db.project_basic_configs.findOne({"_id": fm.basicProjectConfigId});
       var jiraTool = db.project_tool_configs.findOne({"_id": fm.projectToolConfigId});

        var existingTestTool = db.project_tool_configs.findOne({"toolName": "JiraTest", "basicProjectConfigId": fm.basicProjectConfigId});

        if(project && existingTestTool === null){
            var testTool = createTestTool(jiraTool, fm);
            db.project_tool_configs.save(testTool);
        }

    }
});

