print("Start : Assignee details delete script from all tools");


function basicProjectConfigIdForDisableToggle()
{
  if(db.getCollection('project_basic_configs').find({enableAssigneeDetailToggle: false}).count()>0)
  {
  db.getCollection('project_basic_configs').find({enableAssigneeDetailToggle: false}).forEach(
              basicProjectConfig => {
              var basicProjectConfigIdDisableToggle =  basicProjectConfig._id;
              print("Project basic cofig Id for disable toggle projects in var form :",basicProjectConfigIdDisableToggle);
              var convertingBasicConfigIdToStr = basicProjectConfigIdDisableToggle.str;
              print("Converting basicProjectConfigIdDisableToggle to string ",convertingBasicConfigIdToStr);

              deleteAssigneeDetailsFromJira(convertingBasicConfigIdToStr);
              deleteAssigneeDetailsFromJiraKanban(convertingBasicConfigIdToStr);
              deleteAssigneeDetailsFromNonJiraProcessor(convertingBasicConfigIdToStr);
              deleteAssigneeDetailsFromDeployment(convertingBasicConfigIdToStr);



    });}

}

function deleteAssigneeDetailsFromNonJiraProcessor(var str)
{
 if(db.getCollection('project_tool_configs').find({"basicProjectConfigId" : ObjectId(str)}).count()>0)
{
db.getCollection('project_tool_configs').
find({"basicProjectConfigId" : ObjectId(str)}).forEach(
    projectToolConfig => {
        var projectToolId = projectToolConfig._id;
        print("Project Tool config id :",projectToolId);
        var convertingProjectToolIdToStr = projectToolId.str;
        fetchProcessorItemId(convertingProjectToolIdToStr)

    });


}
}

function fetchProcessorItemId(var convertingProjectToolIdToStr)
{
 if(db.processor_items.find({"toolConfigId" : ObjectId(convertingProjectToolIdToStr)}).count()>0)
                {
                  db.getCollection('processor_items').
        find({"toolConfigId" : ObjectId(convertingProjectToolIdToStr)}).forEach(
            processorItems => {
                var processorItemID = processorItems._id;
                print("Processor item id : ",processorItemID);
                var convertingProcessorItemIDToStr = processorItemID.str;
                deleteAssigneeFromBuild(convertingProcessorItemIDToStr);
                deleteAssigneeFromCommit(convertingProcessorItemIDToStr);
                deleteAssigneeFromMerge(convertingProcessorItemIDToStr);


            })
                }

}


function deleteAssigneeFromMerge(var convertingProcessorItemIDToStr)
{
if(db.getCollection('merge_requests').
   find({"processorItemId" : ObjectId(convertingProcessorItemIDToStr), "author" : {$exists : true}}).count()>0)
{
 print("Update assignee details for the projects in commit details: ",convertingProcessorItemIDToStr);
 db.merge_requests.updateMany({"processorItemId" : ObjectId(convertingProcessorItemIDToStr)}, { $unset: { author: 1 }});

}

}

function deleteAssigneeFromBuild(var convertingProcessorItemIDToStr)
{
if(db.getCollection('build_details').
   find({"processorItemId" : ObjectId(convertingProcessorItemIDToStr), "startedBy" : {$exists : true}}).count()>0)
{
 print("Update assignee details for the projects in build details:",convertingProcessorItemIDToStr);
 db.build_details.updateMany({"processorItemId" : ObjectId(convertingProcessorItemIDToStr)}, { $unset: { startedBy: 1 }});
}

}

function deleteAssigneeFromCommit(var convertingProcessorItemIDToStr)
{
if(db.getCollection('commit_details').
   find({"processorItemId" : ObjectId(convertingProcessorItemIDToStr), "author" : {$exists : true}}).count()>0)
{
 print("Update assignee details for the projects in commit details: ",convertingProcessorItemIDToStr);
 db.commit_details.updateMany({"processorItemId" : ObjectId(convertingProcessorItemIDToStr)}, { $unset: { author: 1 }});
}

}

function deleteAssigneeDetailsFromDeployment(var str)
{
 if(db.getCollection('deployments').find({"basicProjectConfigId": ObjectId(str), "deployedBy" : {$exists : true}}).count()>0)
  {
    print("Update assignee details for the basic config project id in deployments : ",str);
      db.deployments.updateMany({"basicProjectConfigId":ObjectId(str)}, { $unset: { deployedBy: 1}});
  }

}


function deleteAssigneeDetailsFromJira(var str)
{
 if(db.getCollection('jira_issue').find({"basicProjectConfigId": str, "assigneeName" : {$exists : true},"assigneeId" : {$exists : true}}).count()>0)
  {
    print("Update assignee details for this basic project id in jira issue:",str);
      db.jira_issue.updateMany({"basicProjectConfigId":str}, { $unset: { assigneeId: 1, assigneeName: 1 }})

  }

}

function deleteAssigneeDetailsFromJiraKanban(var str)
{
if(db.getCollection('kanban_jira_issue').find({"basicProjectConfigId": str, "assigneeName" : {$exists : true},"assigneeId" : {$exists : true}}).count()>0)
  {
    print("Update assignee details for this basic project id :",str);
      db.kanban_jira_issue.updateMany({"basicProjectConfigId":str}, { $unset: { assigneeId: 1, assigneeName: 1 }})

  }
}
