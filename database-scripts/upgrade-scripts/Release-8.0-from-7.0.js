//--------------------------7.1.0--START-----------------
//updating kpiMaster
const bulkUpdateKpiMaster = [];
const bulkUpdateKpiFieldMapping = [];

bulkUpdateKpiMaster.push({
    updateMany: {
        filter: {
            "kpiId": "kpi138"
        },
        update: {
            $set: {
                "kpiInfo": {
                    "details": [{
                            "type": "paragraph",
                            "value": "Ready Backlog: No. of issues which are refined in the backlog. This is identified through a status configured in KnowHOW."
                        },
                        {
                            "type": "paragraph",
                            "value": "Backlog Strength: Total size of 'Refined' issues in the backlog / Average velocity of last 5 sprints. It is calculated in terms of no. of sprints. Recommended strength is 2 sprints."
                        },
                        {
                            "type": "paragraph",
                            "value": "Readiness cycle time: Average time taken for Product Backlog items (PBIs) to be refined."
                        }
                    ]
                }
            }

        }
    }
});


//bulk write to update kpiMaster
if (bulkUpdateKpiMaster.length > 0) {
    db.kpi_master.bulkWrite(bulkUpdateKpiMaster);
}


//updating fieldmapping
bulkUpdateKpiFieldMapping.push({
    updateMany: {
        filter: {
            "kpiId": "Kpi148"
        },
        update: {
            $set: {
                "kpiSource": 'Jira',
                "fieldNames": {
                    'Workflow Status Mapping': [
                        'storyFirstStatus',
                        'jiraStatusForInProgress',
                        'jiraStatusForQa',
                        'jiraLiveStatus'
                    ]
                }

            }

        }
    }
});

//bulk write to update kpiFieldMapping
if (bulkUpdateKpiFieldMapping.length > 0) {
    db.kpi_fieldmapping.bulkWrite(bulkUpdateKpiFieldMapping);
}



print("Start: Script to Removing Backlog Collection")
db.issue_backlog.drop();
db.issue_backlog_custom_history.drop();
print("End: Script to Removing Backlog Collection")




//-------------------------7.1.0.....End---------------------------------------