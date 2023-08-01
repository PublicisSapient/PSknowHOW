print("Start : Assignee details delete script from all tools");

function deleteAssigneeFromMerge(processorItemId) {
        print("Remove assignee details for the processorItemId in merge details: ", processorItemId);
        print(db.merge_requests.updateMany({"processorItemId": ObjectId(processorItemId)}, {$unset: {author: 1}}));
}

function deleteAssigneeFromBuild(basicProjectConfigId) {
        print("Remove assignee details for the basic project config id in build details:", basicProjectConfigId);
        print(db.build_details.updateMany({"basicProjectConfigId": ObjectId(basicProjectConfigId)}, {$unset: {startedBy: 1}}));
}

function deleteAssigneeFromCommit(processorItemId) {
        print("Remove assignee details for the processorItemId in commit details: ", processorItemId);
        print(db.commit_details.updateMany({"processorItemId": ObjectId(processorItemId)}, {$unset: {author: 1}}));
}

function deleteAssigneeDetailsFromDeployment(basicProjectConfigId) {
        print("Remove assignee details for the basic config project id in deployments : ", basicProjectConfigId);
        print(db.deployments.updateMany({"basicProjectConfigId": ObjectId(basicProjectConfigId)}, {$unset: {deployedBy: 1}}));
}

function deleteAssigneeDetailsFromCommitAndMerge(basicProjectConfigId) {

        db.getCollection('project_tool_configs').find({"basicProjectConfigId": ObjectId(basicProjectConfigId)}).forEach(
            projectToolConfig => {
                const projectToolId = projectToolConfig._id;
                print("Project Tool config id :", projectToolId);
                const toolConfigId = projectToolId.str;
                fetchProcessorItemId(toolConfigId);
            });
}


function fetchProcessorItemId(toolConfigId) {

        db.getCollection('processor_items').find({"toolConfigId": ObjectId(toolConfigId)}).forEach(
            processorItems => {
                const processorItemElement = processorItems._id;
                print("Processor item id in object form : ", processorItemElement);
                const processorItemId = processorItemElement.str;
                deleteAssigneeFromCommit(processorItemId);
                deleteAssigneeFromMerge(processorItemId);
            });

}


   db.getCollection('project_basic_configs').find({ "saveAssigneeDetails" : {$exists : false }}).forEach(
        basicProjectConfig => {
            const basicProjectConfigIdDisableToggle = basicProjectConfig._id;
            const basicProjectConfigId = basicProjectConfigIdDisableToggle.str;
            print("Project basic configId", basicProjectConfigId);
            deleteAssigneeDetailsFromDeployment(basicProjectConfigId);
            deleteAssigneeFromBuild(basicProjectConfigId);
            deleteAssigneeDetailsFromCommitAndMerge(basicProjectConfigId);
            db.getCollection('project_basic_configs').update({ "_id" : ObjectId(basicProjectConfigId)},
            { $set : {saveAssigneeDetails : false }})

        });
