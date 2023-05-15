print("Start : backward compatibility script for kanban issue custom history");

function hasStorySprintDetails(historyObject) {
    return historyObject.historyDetails && historyObject.historyDetails.length !== 0;
}

var jiraIssueHistorys = db.kanban_issue_custom_history.find({
    "historyDetails": {
        $exists: true,
        $not: {
            $size: 0
        }
    }
})

jiraIssueHistorys.forEach(function(historyObject) {
    if (hasStorySprintDetails(historyObject)) {
        var historyDetails = historyObject.historyDetails;
        var prevChangedTo = "";
        var changedLogs = [];
        var _id = historyObject._id;
        historyDetails.forEach(function(obj) {
            var changedTo = obj.status;
            var updatedOn = obj.activityDate;
            changedLogs.push({
                "changedFrom": prevChangedTo,
                changedTo,
                updatedOn
            });
            prevChangedTo = changedTo;
        });

        var result = db.kanban_issue_custom_history.updateOne({
           "_id":_id
        }, {
            $set: {
                'statusUpdationLog': changedLogs
            },
            $unset: {
                "historyDetails": ""
            }
        });
        print(result);
    }

});

print("End : backward compatibility script for kanban issue custom history");