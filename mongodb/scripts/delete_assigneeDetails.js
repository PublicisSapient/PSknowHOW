print("Start : Assignee details delete script from all tools");

function deleteAssigneeFromMerge(processorItemId)
{
        print("Update assignee details for the processorItemId in merge details: ", processorItemId);
        db.merge_requests.updateMany({"processorItemId": ObjectId(processorItemId)}, {$unset: {author: 1}});
}

function deleteAssigneeFromBuild(basicProjectConfigId)
{
        print("Update assignee details for the basic project config id in build details:", basicProjectConfigId);
        db.build_details.updateMany({"basicProjectConfigId": ObjectId(basicProjectConfigId)}, {$unset: {startedBy: 1}});
}

function deleteAssigneeFromCommit(processorItemId)
{
        print("Update assignee details for the processorItemId in commit details: ", processorItemId);
        db.commit_details.updateMany({"processorItemId": ObjectId(processorItemId)}, {$unset: {author: 1}});
}

function deleteAssigneeDetailsFromDeployment(basicProjectConfigId)
{
        print("Update assignee details for the basic config project id in deployments : ", basicProjectConfigId);
        db.deployments.updateMany({"basicProjectConfigId": ObjectId(basicProjectConfigId)}, {$unset: {deployedBy: 1}});
}


function deleteAssigneeDetailsFromJira(basicProjectConfigId)
{
              print("Update assignee details for this basic project id in jira issue:", basicProjectConfigId);
              db.jira_issue.updateMany({"basicProjectConfigId": basicProjectConfigId}, {$unset: {assigneeId: 1, assigneeName: 1}});
}

function deleteAssigneeDetailsFromJiraKanban(basicProjectConfigId)
{
        print("Update assignee details for this basic project id in kanban jira issue:", basicProjectConfigId);
        db.kanban_jira_issue.updateMany({ "basicProjectConfigId": basicProjectConfigId}, {$unset: {assigneeId: 1,assigneeName: 1}});
}

function deleteAssigneeDetailsFromCommitAndMerge(basicProjectConfigId) {
    if (db.getCollection('project_tool_configs').find({"basicProjectConfigId": ObjectId(basicProjectConfigId)}).count() > 0)
    {
        db.getCollection('project_tool_configs').find({"basicProjectConfigId": ObjectId(basicProjectConfigId)}).forEach(
            projectToolConfig => {
                const projectToolId = projectToolConfig._id;
                print("Project Tool config id :", projectToolId);
                const toolConfigId = projectToolId.str;
                fetchProcessorItemId(toolConfigId);
            });
    }
}


function fetchProcessorItemId(toolConfigId)
{
    if (db.processor_items.find({"toolConfigId": ObjectId(toolConfigId)}).count() > 0)
    {
        db.getCollection('processor_items').find({"toolConfigId": ObjectId(toolConfigId)}).forEach(
            processorItems => {
                const processorItemElement = processorItems._id;
                print("Processor item id in object form : ", processorItemElement);
                const processorItemId = processorItemElement.str;
                deleteAssigneeFromCommit(processorItemId);
                deleteAssigneeFromMerge(processorItemId);
            })
    }

}


    db.getCollection('project_basic_configs').find({saveAssigneeDetails: false}).forEach(
        basicProjectConfig => {
            const basicProjectConfigIdDisableToggle = basicProjectConfig._id;
            const basicProjectConfigId = basicProjectConfigIdDisableToggle.str;
             print("Project basic configId for disable toggle projects", basicProjectConfigId);
            deleteAssigneeDetailsFromJira(basicProjectConfigId);
            deleteAssigneeDetailsFromJiraKanban(basicProjectConfigId);
            deleteAssigneeDetailsFromDeployment(basicProjectConfigId);
            deleteAssigneeFromBuild(basicProjectConfigId);
            deleteAssigneeDetailsFromCommitAndMerge(basicProjectConfigId);

        });
