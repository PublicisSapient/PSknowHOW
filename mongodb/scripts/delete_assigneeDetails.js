print("Start : Assignee details delete script from all tools");

function deleteAssigneeFromMerge(basicProjectConfigId)
{
        print("Update assignee details for the basic project config id in merge details: ", basicProjectConfigId);
        db.merge_requests.updateMany({"basicProjectConfigId": ObjectId(basicProjectConfigId)}, {$unset: {author: 1}});
}

function deleteAssigneeFromBuild(basicProjectConfigId)
{
        print("Update assignee details for the basic project config id in build details:", basicProjectConfigId);
        db.build_details.updateMany({"basicProjectConfigId": ObjectId(basicProjectConfigId)}, {$unset: {startedBy: 1}});
}

function deleteAssigneeFromCommit(basicProjectConfigId)
{
        print("Update assignee details for the basic project config id in commit details: ", basicProjectConfigId);
        db.commit_details.updateMany({"basicProjectConfigId": ObjectId(basicProjectConfigId)}, {$unset: {author: 1}});
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


    db.getCollection('project_basic_configs').find({saveAssigneeDetails: false}).forEach(
        basicProjectConfig => {
            const basicProjectConfigIdDisableToggle = basicProjectConfig._id;
            const basicProjectConfigId = basicProjectConfigIdDisableToggle.str;
             print("Project basic configId for disable toggle projects", basicProjectConfigId);
            deleteAssigneeDetailsFromJira(basicProjectConfigId);
            deleteAssigneeDetailsFromJiraKanban(basicProjectConfigId);
            deleteAssigneeFromCommit(basicProjectConfigId);
            deleteAssigneeDetailsFromDeployment(basicProjectConfigId);
            deleteAssigneeFromBuild(basicProjectConfigId);
            deleteAssigneeFromMerge(basicProjectConfigId);

        });
