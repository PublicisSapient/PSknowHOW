//update processor_execution_trace_log collection for one time to run the
//jira processor in order to update assignees with hashKey
function updateTraceLog(basicProjectConfigId) {
    print("Project basic configId", basicProjectConfigId);
    print(db.processor_execution_trace_log.updateMany({
        "basicProjectConfigId": basicProjectConfigId,
        "processorName": "Jira"
    }, {
        $unset: {
            lastSuccessfulRun: 1,
            lastSavedEntryUpdatedDateByType: 1
        },
        $set: {
            "executionSuccess": false
        }
    }));
}

db.getCollection('project_basic_configs').find({
    $or: [{
        "saveAssigneeDetails": {
            $exists: false
        }
    }, {
        "saveAssigneeDetails": false
    }]
}).forEach(
    basicProjectConfig => {
        const basicProjectConfigIdDisableToggle = basicProjectConfig._id;
        const basicProjectConfigId = basicProjectConfigIdDisableToggle.str;
        updateTraceLog(basicProjectConfigId);
    });