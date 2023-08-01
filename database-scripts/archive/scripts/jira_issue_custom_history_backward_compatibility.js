print("Start : backward compatibility script for jira issue custom history");

function hasStorySprintDetails(historyObject) {
    return historyObject.storySprintDetails && historyObject.storySprintDetails.length !== 0;
}

var jiraIssueHistorys = db.jira_issue_custom_history.find({
    "storySprintDetails": {
        $exists: true,
        $not: {
            $size: 0
        }
    }
})

jiraIssueHistorys.forEach(function(historyObject) {
    if (hasStorySprintDetails(historyObject)) {
        var storySprintDetails = historyObject.storySprintDetails;
        var prevChangedTo = "";
        var changedLogs = [];
        var _id = historyObject._id;
        storySprintDetails.forEach(function(obj) {
            var changedTo = obj.fromStatus;
            var updatedOn = obj.activityDate;
            changedLogs.push({
                "changedFrom": prevChangedTo,
                changedTo,
                updatedOn
            });
            prevChangedTo = changedTo;
        });

        var result = db.jira_issue_custom_history.updateOne({
           "_id":_id
        }, {
            $set: {
                'statusUpdationLog': changedLogs
            },
            $unset: {
                "storySprintDetails": ""
            }
        });
        print(result);
    }

});

print("End : backward compatibility script for jira issue custom history");