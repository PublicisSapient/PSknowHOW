let basicConfigIds = db.jira_issue_custom_history.distinct("basicProjectConfigId");
basicConfigIds.forEach(function(id) {
    let issues = db.jira_issue.find({
        "basicProjectConfigId": id
    });
    issues.forEach(function(issue) {
        if (issue) {
            let issueHistory = db.jira_issue_custom_history.findOne({
                "basicProjectConfigId": id,
                "storyID": issue.number
            });
            if (issueHistory) {
                db.jira_issue_custom_history.updateOne({
                    "_id": issueHistory._id
                }, {
                    $set: {
                        'updateDate': issue.updateDate
                    }
                });
            }
        }
    })
})

let allIssuesWithoutUpdateDate = db.jira_issue_custom_history.find({ "updateDate": { $exists: false } });
allIssuesWithoutUpdateDate.forEach(function(issueHistory){
     if (issueHistory) {
                db.jira_issue_custom_history.updateOne({
                    "_id": issueHistory._id
                }, {
                    $set: {
                        'updateDate': issueHistory.createdDate
                    }
                });
            }
})