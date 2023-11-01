//------------------------- 8.1.0 changes----------------------------------------------------------------------------------
db.kpi_master.insertOne({
    "kpiId": "kpi166",
    "kpiName": "Mean Time to Recover",
    "maxValue": "100",
    "kpiUnit": "Hours",
    "isDeleted": "False",
    "defaultOrder": 4,
    "kpiSource": "Jira",
    "kpiCategory": "Dora",
    "groupId": 15,
    "thresholdValue": 0,
    "kanban": false,
    "chartType": "line",
    "kpiInfo": {
        "definition": "Mean time to recover will be based on the Production incident tickets raised during a certain period of time.",
        "details": [
            {
                "type": "paragraph",
                "value": "For all the production incident tickets raised during a time period, the time between created date and closed date of the incident ticket will be calculated."
            },
            {
                "type": "paragraph",
                "value": "The average of all such tickets will be shown."
            }
        ],
        "maturityLevels": []
    },
    "xAxisLabel": "Weeks",
    "yAxisLabel": "Hours",
    "isPositiveTrend": false,
    "showTrend": true,
    "kpiFilter": "",
    "aggregationCriteria": "sum",
    "aggregationCircleCriteria": "average",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": false
})

db.field_mapping_structure.insertMany([
    {
        "fieldName": "jiraStoryIdentificationKPI166",
        "fieldLabel": "Issue type to identify Production incidents",
        "fieldType": "chips",
        "fieldCategory": "Issue_Type",
        "section": "Issue Types Mapping",
        "tooltip": {
            "definition": "All issue types that are used as/equivalent to Production incidents.",

        }
    },
    {
        "fieldName": "jiraProductionIncidentIdentification",
        "fieldLabel": "Production incidents identification",
        "fieldType": "radiobutton",
        "section": "Defects Mapping",
        "tooltip": {
            "definition": "This field is used to identify if a production incident is raised by third party or client:<br>1. CustomField : If a separate custom field is used<br>2. Labels : If a label is used to identify. Example: PROD_DEFECT (This has to be one value).<hr>"
        },
        "options": [{
            "label": "CustomField",
            "value": "CustomField"
        },
        {
            "label": "Labels",
            "value": "Labels"
        }
        ],
        "nestedFields": [

            {
                "fieldName": "jiraProdIncidentRaisedByCustomField",
                "fieldLabel": "Production Incident Custom Field",
                "fieldType": "text",
                "fieldCategory": "fields",
                "filterGroup": ["CustomField"],
                "tooltip": {
                    "definition": "Provide customfield name to identify Production Incident. <br> Example: customfield_13907<hr>"
                }
            },
            {
                "fieldName": "jiraProdIncidentRaisedByValue",
                "fieldLabel": "Production Incident Values",
                "fieldType": "chips",
                "filterGroup": ["CustomField", "Labels"],
                "tooltip": {
                    "definition": "Provide label name to identify Production IncidentProduction IncideProd_Incidentxample: Clone_by_QA <hr>"
                }
            }
        ]
    },
    {
        "fieldName": "jiraDodKPI166",
        "fieldLabel": "DOD Status",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "Status/es that identify that an issue is completed based on Definition of Done (DoD)."
        }
    }
])

db.getCollection("kpi_column_configs").insertMany([
    {
        "basicProjectConfigId": null,
        "kpiId": "kpi166",
        "kpiColumnDetails": [
            {
                "columnName": "Project Name",
                "order": 0,
                "isShown": true,
                "isDefault": true
            },
            {
                "columnName": "Date",
                "order": 1,
                "isShown": true,
                "isDefault": true
            },
            {
                "columnName": "Story ID",
                "order": 2,
                "isShown": true,
                "isDefault": true
            },
            {
                "columnName": "Issue Type",
                "order": 3,
                "isShown": true,
                "isDefault": true
            },
            {
                "columnName": "Issue Description",
                "order": 4,
                "isShown": true,
                "isDefault": true
            },
            {
                "columnName": "Created Date",
                "order": 5,
                "isShown": true,
                "isDefault": true
            },
            {
                "columnName": "Completion Date",
                "order": 6,
                "isShown": true,
                "isDefault": true
            },
            {
                "columnName": "Time to Recover (In Hours)",
                "order": 7,
                "isShown": true,
                "isDefault": true
            }
        ]
    }
]);

db.getCollection('metadata_identifier').updateMany(
    { "templateCode": { $in: ["7"] } },
    {
        $push: {
            "workflow": {
                "type": "jiraDodKPI166",
                "value": [
                    "Closed"
                ]
            },
            "issues": {
                "type": "jiraStoryIdentificationKPI166",
                "value": [
                    "Story",
                    "Enabler Story",
                    "Tech Story",
                    "Change request"
                ]
            }
        }
    }
);