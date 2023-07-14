print("Start : backward compatibility script for test tool");
function hasTestConfigInFieldMapping(fm){
    return fm.jiraTestCaseType && fm.jiraTestCaseType.length !== 0;
}

function getValueAsString(inputValue){
    if(inputValue){
        return inputValue;
    }
    return "";
}

function getValueAsArray(inputArray){
    if(inputArray && inputArray.length > 0){
        return inputArray;
    }
    return [];
}

function currentDateAsString(){

    return new Date().toISOString().slice(0,-5);
}

function createTestTool(jiraTool, fm) {
    var testTool = {};
    testTool["toolName"] = "JiraTest";
    testTool["basicProjectConfigId"] = fm.basicProjectConfigId;
    testTool["connectionId"] = jiraTool.connectionId;
    testTool["projectKey"] = jiraTool.projectKey;
    testTool["createdAt"] = currentDateAsString();
    testTool["updatedAt"] = currentDateAsString();
    testTool["testAutomated"] = getValueAsString(fm.testAutomated);
    testTool["jiraTestCaseType"] = getValueAsArray(fm.jiraTestCaseType);
    testTool["testAutomatedIdentification"] = getValueAsString(fm.testAutomatedIdentification);
    testTool["testAutomationCompletedIdentification"] = getValueAsString(fm.testAutomationCompletedIdentification);
    testTool["testRegressionIdentification"] = getValueAsString(fm.testRegressionIdentification);
    testTool["testAutomationCompletedByCustomField"] = getValueAsString(fm.testAutomationCompletedByCustomField);
    testTool["testRegressionByCustomField"] = getValueAsString(fm.testRegressionByCustomField);
    testTool["jiraAutomatedTestValue"] = getValueAsArray(fm.jiraAutomatedTestValue);
    testTool["jiraRegressionTestValue"] = getValueAsArray(fm.jiraRegressionTestValue);
    testTool["jiraCanBeAutomatedTestValue"] = getValueAsArray(fm.jiraCanBeAutomatedTestValue);
    testTool["testCaseStatus"] = getValueAsArray(fm.testCaseStatus);
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
print("End : backward compatibility script for test tool");
