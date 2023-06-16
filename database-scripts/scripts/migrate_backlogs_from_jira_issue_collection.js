if (db.getCollectionNames().indexOf("issue_backlog") == -1) {
    print("Start : script for creating and migrating data in backlog collection");

    let backlogs = db.jira_issue.find({
        "sprintAssetState": {
            "$in": ["", "FUTURE"]
        },
        "typeName": {
            '$ne': "Epic"
        }
    });

    function isValidBacklogStatus(status, dodStatus, liveStatus) {
        if (status == liveStatus) return false;
        for (let sts of dodStatus)
            if (sts.toLowerCase() == status) return false;
        return true;
    }

    backlogs.forEach(function(obj) {
        let basicProjectConfigId = obj.basicProjectConfigId;
        let fm = db.field_mapping.findOne({
            "basicProjectConfigId": ObjectId(basicProjectConfigId)
        });
        let dodStatus = fm ? fm["jiraDod"] : "";
        let liveStatus = fm ? fm.jiraLiveStatus : "";
        let status = obj.status ? obj.status : "";
        liveStatus = liveStatus ? liveStatus : "";
        if (isValidBacklogStatus(status.toLowerCase(), dodStatus, liveStatus.toLowerCase())) {
            if (db.issue_backlog.findOne({
                    "number": obj.number,
                    "basicProjectConfigId": obj.basicProjectConfigId
                }) == null) {
                db.issue_backlog.save(obj);
            }
            let historyObj = db.jira_issue_custom_history.findOne({
                "storyID": obj.number,
                "basicProjectConfigId": obj.basicProjectConfigId
            });
            if (db.issue_backlog_custom_history.findOne({
                    "storyID": obj.number,
                    "basicProjectConfigId": obj.basicProjectConfigId
                }) == null)
                var res3 = db.issue_backlog_custom_history.save(historyObj);
        }
    });

    print("End : script for creating and migrating data in backlog collection");
}