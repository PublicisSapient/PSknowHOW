print("Start : backward compatibility script for jira issue custom history");

function hasStorySprintDetails(historyObject){
    return historyObject.storySprintDetails && historyObject.storySprintDetails.length !== 0;
}

var jiraIssueHistorys = db.jira_issue_custom_history.find({"storySprintDetails":{$exists: true, $not: {$size: 0}}})

jiraIssueHistorys.forEach(function(historyObject){
    if(hasStorySprintDetails(historyObject))
    {
        var storySprintDetails = historyObject.storySprintDetails;
        var prevChangedTo="";
        var changedLogs = [];
        var storyID = historyObject.storyID;
        storySprintDetails.forEach(function(obj){
            var changedTo = obj.fromStatus;
            var updatedOn = obj.activityDate;
            changedLogs.push({"changedFrom":prevChangedTo,changedTo,updatedOn});
            prevChangedTo=changedTo;
        });

         var result = db.jira_issue_custom_history.updateOne({"storyID": storyID},
        				{
                           $set: {
                                'statusUpdationLog':changedLogs
                                  },
                           $unset: {
                             	"storySprintDetails":""
                          }
                    });
        print(result);
    }

});

print("End : backward compatibility script for jira issue custom history");

/*
Query to fetch count of object with StorySprintDetails

db.jira_issue_custom_history.find({"storySprintDetails":{$exists: true, $not: {$size: 0}}})
   .projection({})
   .sort({_id:-1})
   .count()
*/

/*
Query to fetch count of object with statusUpdationLog

db.jira_issue_custom_history.find({"statusUpdationLog":{$exists: true, $not: {$size: 0}}})
   .projection({})
   .sort({_id:-1})
   .count()
*/

