//The script removes the extra releases other than the required from account_hierarchy table
const bulkUpdateOps = [];

function splitString(inputValue, delimiter) {
    return inputValue.split(delimiter);
}

function addIntoSet(list, array, delimiter, projectName) {
    splitString(array, delimiter).forEach(element => {
        list.add(element + "_" + projectName);
    });
}

function deleteFromAccountHierarchy(project_release) {
    const projectId = project_release.projectId;
    const configId = project_release.configId;

    const projectName = splitString(projectId, "_").shift();

    const list = new Set();
    //issues which had a history of release are maintained in a list
    db.jira_issue_custom_history.find(
        { "basicProjectConfigId": configId.str },
        { 'fixVersionUpdationLog.changedFrom': 1, 'fixVersionUpdationLog.changedTo': 1, _id: 0 }
    ).forEach(doc => {
        const fixVersionUpdationLog = doc.fixVersionUpdationLog;
        if (fixVersionUpdationLog) {
            fixVersionUpdationLog.forEach(log => {
                addIntoSet(list, log.changedFrom + "," + log.changedTo, ',', projectName);
            });
        }
    });

    const nodeNames = Array.from(list);
    print("projectId "+ projectId+ " nodeNames "+nodeNames);

    bulkUpdateOps.push({
        updateMany: {
            filter: {
                nodeName: { $in: nodeNames },
                basicProjectConfigId: configId,
                labelName: "release"
            },
            update: {
                $set: { retain: true }
            }
        }
    });
}

//for each project_release, going to remove extra releases which are not-required
db.getCollection('project_release').find().forEach(project_release => {
    if (project_release.projectId !== undefined) {
        deleteFromAccountHierarchy(project_release);
    }
});

//bulk-updating account_hierarchy with column retian=true, if those are required on release filter
if (bulkUpdateOps.length > 0) {
    db.account_hierarchy.bulkWrite(bulkUpdateOps);
}

//deleting the columns which are not required i.e., retain column absent
db.account_hierarchy.deleteMany({ retain: { $eq: null }, labelName: "release" });

//finally deleting the column name by unsetting the retain column
db.account_hierarchy.update(
    { "labelName": "release" },
    { $unset: { retain: 1 } },
    { multi: true }
);
