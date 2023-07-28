/*for each project release version will create a hierarchy to populate milestone dashboard*/
function splitString(inputValue, delimiter) {
    return inputValue.split(delimiter).shift();
}
/*provide release state*/
function releaseState(state) {
    if (state) {
        return "Released";
    } else {
        return "Unreleased"
    }
}

function checkMonth(inputMonth) {
    var month;
    if (inputMonth < 10 && inputMonth > 0) {
        month = "0" + inputMonth;
    } else if (inputMonth >= 10) {
        month = inputMonth;
    }
    return month;
}

function checkDay(inputDay) {
    var day;
    if (inputDay < 10 && inputDay > 0) {
        day = "0" + inputDay;
    } else if (inputDay >= 10) {
        day = inputDay;
    }
    return day;
}
/*format datetime string of project_release to normal string*/
function currentDateAsString(releaseDate) {
    if (releaseDate != undefined) {
        var limiter = "-";
        var time = "T00:00:00";
        var month = checkMonth(releaseDate.getMonth() + 1);
        var day = checkDay(releaseDate.getDate());
        return releaseDate.getFullYear() + limiter + month + limiter + day + time;
    }
    return "";
}

/* hierachies will be created when no release hierachy is present in collection update Account Hierarchy*/
function updateAccountHierachy(project_release) {
    const projectId = project_release.projectId;
    print("projectId", projectId)

    if (db.getCollection('account_hierarchy').find({
            "parentId": projectId,
            labelName: 'release'
        }).count() == 0) {
        var hierarchy = db.getCollection('account_hierarchy').findOne({
            "nodeId": projectId,
            labelName: 'project'
        });
        if (hierarchy != null) {
            const path = projectId + "###" + hierarchy.path;
            project_release.listProjectVersion.forEach(
                version => {
                    print("nodeName", version.description + "_" + projectId);
                    db.account_hierarchy.insert([{
                        "nodeId": version._id + "_" + projectId,
                        "nodeName": version.description + "_" + splitString(projectId, "_"),
                        "labelName": "release",
                        "beginDate": "",
                        "endDate": currentDateAsString(version.releaseDate),
                        "parentId": projectId,
                        "basicProjectConfigId": project_release.configId,
                        "isDeleted": "False",
                        "path": path,
                        "releaseState": releaseState(version.isReleased),
                        "createdDate": new Date(Date.now())
                    }]);
                }
            )
        }
    }
}

/*create kanbanAccountHierarchy*/
function updateKanbanAccountHierachy(project_release) {
    const projectId = project_release.projectId;

    if (db.getCollection('kanban_account_hierarchy').find({
            "parentId": projectId,
            labelName: 'release'
        }).count() == 0) {
        var hierarchy = db.getCollection('kanban_account_hierarchy').findOne({
            "nodeId": projectId,
            labelName: 'project'
        });
        if (hierarchy != null) {
            const path = projectId + "###" + hierarchy.path;
            project_release.listProjectVersion.forEach(
                version => {
                    print("nodeName", version.description + "_" + projectId);
                    db.kanban_account_hierarchy.insert([{
                        "nodeId": version._id + "_" + projectId,
                        "nodeName": version.description + "_" + splitString(projectId, "_"),
                        "labelName": "release",
                        "beginDate": "",
                        "endDate": currentDateAsString(version.releaseDate),
                        "parentId": projectId,
                        "basicProjectConfigId": project_release.configId,
                        "isDeleted": "False",
                        "path": path,
                        "releaseState": releaseState(version.isReleased),
                        "createdDate": new Date(Date.now())
                    }]);
                }
            )
        }
    }
}

/*delete from account_hierarchy*/
function deleteByMistakenlyAddedKanbanReleaseToScrum(project_release) {
    const projectId = project_release.projectId;
    if (db.getCollection('kanban_account_hierarchy').find({
            "nodeId": projectId
        }).count() > 0) {
        print("deleting projectId", projectId)
        var hierarchy = db.getCollection('account_hierarchy').deleteMany({
            "parentId": projectId,
            labelName: 'release'
        });
    }
}
//start
db.getCollection('project_release').find().forEach(
    project_release => {
        if (project_release.projectId != undefined) {
            updateAccountHierachy(project_release);
            deleteByMistakenlyAddedKanbanReleaseToScrum(project_release);
            updateKanbanAccountHierachy(project_release);
        }
    });