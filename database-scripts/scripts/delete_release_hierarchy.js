function splitString(inputValue, delimiter) {
    return inputValue.split(delimiter);
}

function addIntoSet(list, array, delimiter, projectName) {
    splitString(array, delimiter).forEach((element) => {
        list.add(element + "_" + projectName);
    })
};

function deleteFromAccountHierarchy(project_release) {
    const projectId = project_release.projectId;
    const configId = project_release.configId;

    print("projectId", projectId)
    print("configId", configId)

    const projectName = splitString(projectId, "_").shift();
    print("projectName", projectName)

    var list = new Set();
    db.jira_issue_custom_history.find({
        "basicProjectConfigId": configId.str
    }, {
        'fixVersionUpdationLog.changedFrom': 1,
        'fixVersionUpdationLog.changedTo': 1,
        _id: 0
    }).forEach(function(doc) {
        var fixVersionUpdationLog = doc.fixVersionUpdationLog;
        if (fixVersionUpdationLog != undefined) {
            for (var i = 0; i < fixVersionUpdationLog.length; i++) {
                addIntoSet(list, fixVersionUpdationLog[i].changedFrom, ',', projectName);
                addIntoSet(list, fixVersionUpdationLog[i].changedTo, ',', projectName);
            }
        }
    });

    list.forEach(doc => {
        print(doc);
        db.account_hierarchy.update({
            nodeName: doc,
            basicProjectConfigId: configId,
            "labelName": "release"
        }, {
            $set: {
                retain: true,
            }
        }, {
            multi: true
        });
    });
}

function deleteFromKanbanAccountHierarchy(project_release) {
    const configId = project_release.configId;
    print("configId", configId)

    const projectName = splitString(project_release.projectId, "_").shift();
    print("projectName", projectName)

    var list = new Set();
    db.kanban_issue_custom_history.find({
        "basicProjectConfigId": configId.str
    }, {
        'fixVersionUpdationLog.changedFrom': 1,
        'fixVersionUpdationLog.changedTo': 1,
        _id: 0
    }).forEach(function(doc) {
        var fixVersionUpdationLog = doc.fixVersionUpdationLog;
        if (fixVersionUpdationLog != undefined) {
            for (var i = 0; i < fixVersionUpdationLog.length; i++) {
                addIntoSet(list, fixVersionUpdationLog[i].changedFrom, ',', projectName);
                addIntoSet(list, fixVersionUpdationLog[i].changedTo, ',', projectName);
            }
        }
    });

    list.forEach(doc => {
        print(doc);
        db.kanban_account_hierarchy.update({
            nodeName: doc,
            basicProjectConfigId: configId,
            "labelName": "release"
        }, {
            $set: {
                retain: true,
            }
        }, {
            multi: true
        });
    });
}


function removeTempVariable(project_release) {
    const configId = project_release.configId;
    print(configId);
    print(db.account_hierarchy.deleteMany({
        retain: {
            $eq: null
        },
        "labelName": "release",
        basicProjectConfigId: configId
    }))
    print(db.kanban_account_hierarchy.deleteMany({
        retain: {
            $eq: null
        },
        "labelName": "release",
        basicProjectConfigId: configId
    }))
    db.account_hierarchy.update({
        "labelName": "release",
        basicProjectConfigId: configId
    }, {
        $unset: {
            retain: 1
        }
    }, {
        multi: true
    });
    db.kanban_account_hierarchy.update({
        "labelName": "release",
        basicProjectConfigId: configId
    }, {
        $unset: {
            retain: 1
        }
    }, {
        multi: true
    });
}

db.getCollection('project_release').find().forEach(
    project_release => {
        if (project_release.projectId != undefined) {
            deleteFromAccountHierarchy(project_release);
            deleteFromKanbanAccountHierarchy(project_release);
            removeTempVariable(project_release);
        }
    });