print("Start : Assignee details delete script from all tools");

function deleteAssigneeFromMerge(basicProjectConfigId) {
    if (db.getCollection('merge_requests').find({"basicProjectConfigId": ObjectId(basicProjectConfigId),"author": {$exists: true}}).count() > 0)
    {
        print("Update assignee details for the basic project config id in commit details: ", basicProjectConfigId);
        db.merge_requests.updateMany({"basicProjectConfigId": ObjectId(basicProjectConfigId)}, {$unset: {author: 1}});
    }
}

function deleteAssigneeFromBuild(basicProjectConfigId)
{
    if (db.getCollection('build_details').find({"basicProjectConfigId": ObjectId(basicProjectConfigId),"startedBy": {$exists: true}}).count() > 0)
    {
        print("Update assignee details for the basic project config id in build details:", basicProjectConfigId);
        db.build_details.updateMany({"basicProjectConfigId": ObjectId(basicProjectConfigId)}, {$unset: {startedBy: 1}});
    }
}

function deleteAssigneeFromCommit(basicProjectConfigId)
{
    if (db.getCollection('commit_details').find({"basicProjectConfigId": ObjectId(basicProjectConfigId),"author": {$exists: true}}).count() > 0)
    {
        print("Update assignee details for the basic project config id in commit details: ", basicProjectConfigId);
        db.commit_details.updateMany({"basicProjectConfigId": ObjectId(basicProjectConfigId)}, {$unset: {author: 1}});
    }

}

function deleteAssigneeDetailsFromDeployment(basicProjectConfigId)
{
    if (db.getCollection('deployments').find({"basicProjectConfigId": ObjectId(basicProjectConfigId),"deployedBy": {$exists: true}}).count() > 0)
     {
        print("Update assignee details for the basic config project id in deployments : ", basicProjectConfigId);
        db.deployments.updateMany({"basicProjectConfigId": ObjectId(basicProjectConfigId)}, {$unset: {deployedBy: 1}});
    }

}


function deleteAssigneeDetailsFromJira(basicProjectConfigId)
{
    if (db.getCollection('jira_issue').find({"basicProjectConfigId": basicProjectConfigId,"assigneeName": {$exists: true},"assigneeId": {$exists: true}}).count() > 0)
    {
                  print("Update assignee details for this basic project id in jira issue:", basicProjectConfigId);
                  db.jira_issue.updateMany({"basicProjectConfigId": basicProjectConfigId}, {$unset: {assigneeId: 1, assigneeName: 1}})
    }
}

function deleteAssigneeDetailsFromJiraKanban(basicProjectConfigId) {
    if (db.getCollection('kanban_jira_issue').find({"basicProjectConfigId": basicProjectConfigId,"assigneeName": {$exists: true},"assigneeId": {$exists: true}}).count() > 0)
    {
        print("Update assignee details for this basic project id :", basicProjectConfigId);
        db.kanban_jira_issue.updateMany({ "basicProjectConfigId": basicProjectConfigId}, {$unset: {assigneeId: 1,assigneeName: 1}})
    }
}

if (db.getCollection('project_basic_configs').find({saveAssigneeDetails: false}).count() > 0)
{
    db.getCollection('project_basic_configs').find({saveAssigneeDetails: false}).forEach(
        basicProjectConfig => {
            var basicProjectConfigIdDisableToggle = basicProjectConfig._id;
            print("Project basic cofig Id for disable toggle projects in var form :", basicProjectConfigIdDisableToggle);
            var basicProjectConfigId = basicProjectConfigIdDisableToggle.str;
            print("Converting basicProjectConfigIdDisableToggle to string ", basicProjectConfigId);
            deleteAssigneeDetailsFromJira(basicProjectConfigId);
            deleteAssigneeDetailsFromJiraKanban(basicProjectConfigId);
            deleteAssigneeFromCommit(basicProjectConfigId);
            deleteAssigneeDetailsFromDeployment(basicProjectConfigId);
            deleteAssigneeFromBuild(basicProjectConfigId);
            deleteAssigneeFromMerge(basicProjectConfigId);

        });
} else
    { print(" Assignee details are not present for those project basic config Id ...."); }