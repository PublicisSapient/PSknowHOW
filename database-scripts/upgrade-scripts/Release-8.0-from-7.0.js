//---------7.3.0 changes----------------------------------------------------------------------

// added new backlog kpis
const kpiIdsToCheck = ["kpi151", "kpi152"];
var kpiData = db.getCollection('kpi_master').find( {kpiId: { $in: kpiIdsToCheck }}).toArray();
var kpiColumnData = db.getCollection('kpi_column_configs').find( {kpiId: { $in: kpiIdsToCheck }}).toArray();

if (kpiColumnData.length === 0) {
db.kpi_column_configs.insertMany([{
                                    basicProjectConfigId: null,
                                    kpiId: 'kpi151',
                                    kpiColumnDetails: [{
                                      columnName: 'Issue ID',
                                      order: 0,
                                      isShown: true,
                                      isDefault: true
                                    },
                                    {
                                      columnName: 'Issue Description',
                                      order: 1,
                                      isShown: true,
                                      isDefault: true
                                    },
                                    {
                                      columnName: 'Issue Type',
                                      order: 2,
                                      isShown: true,
                                      isDefault: true
                                    }, {
                                      columnName: 'Issue Status',
                                      order: 3,
                                      isShown: true,
                                      isDefault: true
                                    }, {
                                      columnName: 'Priority',
                                      order: 4,
                                      isShown: true,
                                      isDefault: true
                                    }, {
                                      columnName: 'Created Date',
                                      order: 5,
                                      isShown: true,
                                      isDefault: true
                                    }, {
                                      columnName: 'Updated Date',
                                      order: 6,
                                      isShown: true,
                                      isDefault: true
                                    },
                                    {
                                      columnName: 'Assignee',
                                      order: 7,
                                      isShown: true,
                                      isDefault: true
                                    }]
                                  },
                                  {
                                    basicProjectConfigId: null,
                                    kpiId: 'kpi152',
                                    kpiColumnDetails: [{
                                      columnName: 'Issue ID',
                                      order: 0,
                                      isShown: true,
                                      isDefault: true
                                    },
                                    {
                                      columnName: 'Issue Description',
                                      order: 1,
                                      isShown: true,
                                      isDefault: true
                                    },
                                    {
                                      columnName: 'Issue Type',
                                      order: 2,
                                      isShown: true,
                                      isDefault: true
                                    }, {
                                      columnName: 'Issue Status',
                                      order: 3,
                                      isShown: true,
                                      isDefault: true
                                    }, {
                                      columnName: 'Priority',
                                      order: 4,
                                      isShown: true,
                                      isDefault: true
                                    }, {
                                      columnName: 'Created Date',
                                      order: 5,
                                      isShown: true,
                                      isDefault: true
                                    }, {
                                      columnName: 'Updated Date',
                                      order: 6,
                                      isShown: true,
                                      isDefault: true
                                    },
                                    {
                                      columnName: 'Assignee',
                                      order: 7,
                                      isShown: true,
                                      isDefault: true
                                    }]
                                  }
                                 ]);
                                 } else {
                                     print("KPI Column Config data is already present");
                                 }

if (kpiData.length === 0) {
db.getCollection('kpi_master').insertMany(
[{
       "kpiId": "kpi151",
       "kpiName": "Backlog Count By Status",
       "kpiUnit": "Count",
       "isDeleted": "False",
       "defaultOrder": 9,
       "kpiCategory": "Backlog",
       "kpiSource": "Jira",
       "groupId": 10,
       "thresholdValue": "",
       "kanban": false,
       "chartType": "pieChart",
       "kpiInfo": {
         "definition": "Total count of issues in the Backlog with a breakup by Status."
       },
       "xAxisLabel": "",
       "yAxisLabel": "",
       "isPositiveTrend": true,
       "showTrend": false,
       "isAdditionalFilterSupport": false,
       "kpiFilter": "dropdown",
       "boxType": "chart",
       "calculateMaturity": false
     },
     {
          "kpiId": "kpi152",
          "kpiName": "Backlog Count By Issue Type",
          "kpiUnit": "Count",
          "isDeleted": "False",
          "defaultOrder": 10,
          "kpiCategory": "Backlog",
          "kpiSource": "Jira",
          "groupId": 10,
          "thresholdValue": "",
          "kanban": false,
          "chartType": "pieChart",
          "kpiInfo": {
            "definition": "Total count of issues in the backlog with a breakup by issue type."
          },
          "xAxisLabel": "",
          "yAxisLabel": "",
          "isPositiveTrend": true,
          "showTrend": false,
          "isAdditionalFilterSupport": false,
          "kpiFilter": "dropdown",
          "boxType": "chart",
          "calculateMaturity": false
      }
 ]);
 } else {
     print("KPI are already present in Kpi master");
 }

 //---------7.4.0 changes----------------------------------------------------------------------

 //-------------------- kpi detail changes for DTS-25745 change in both the DRE operands and field names in field mappings-------
 //-------------------- Backlog KPI divided in two groups to fix performace issue
 const bulkUpdateKpiMaster = [];
 const kpiIdsToUpdate = ["kpi129", "kpi138", "kpi3", "kpi148", "kpi152"];
 const newGroupId = 11;

 bulkUpdateKpiMaster.push({
     updateMany: {
         filter: {
             "kpiId": "kpi34"
         },
         update: {
             $set: {"kpiInfo.formula.$[].operands":  ["No. of defects in the iteration that are fixed",
                                                                               "Total no. of defects in a iteration"]}
         }
     }
 });

 bulkUpdateKpiMaster.push({
     updateMany: {
         filter: {
             "kpiId": { $in: kpiIdsToUpdate }
         },
         update: {
              $set: { "groupId": newGroupId } }
         }

});

 //bulk write to update kpiMaster
 if (bulkUpdateKpiMaster.length > 0) {
     db.kpi_master.bulkWrite(bulkUpdateKpiMaster);
 }

//-------------field mapping config update

const fieldMappings = db.field_mapping.find({});
fieldMappings.forEach(function(fm) {
    if (!fm.createdDate) {
        const defectPriority = fm.defectPriority;
        const jiraStatusForDevelopment = fm.jiraStatusForDevelopment;
        const jiraDod = fm.jiraDod;
        const jiraDefectRejectionStatus = fm.jiraDefectRejectionStatus;
        const jiraSprintVelocityIssueType = fm.jiraSprintVelocityIssueType;
        const jiraDefectCountlIssueType = fm.jiraDefectCountlIssueType;
        const jiraIssueDeliverdStatus = fm.jiraIssueDeliverdStatus;
        const excludeRCAFromFTPR = fm.excludeRCAFromFTPR;
        const jiraIssueTypeNames = fm.jiraIssueTypeNames;
        const resolutionTypeForRejection = fm.resolutionTypeForRejection;
        const jiraLiveStatus = fm.jiraLiveStatus;
        const jiraFTPRStoryIdentification = fm.jiraFTPRStoryIdentification;
        const jiraIterationCompletionStatusCustomField = fm.jiraIterationCompletionStatusCustomField;
        const jiraIterationCompletionTypeCustomField = fm.jiraIterationCompletionTypeCustomField;
        const jiraDor = fm.jiraDor;
        const jiraIntakeToDorIssueType = fm.jiraIntakeToDorIssueType;
        const jiraStoryIdentification = fm.jiraStoryIdentification;
        const jiraStatusForInProgress = fm.jiraStatusForInProgress;
        const issueStatusExcluMissingWork = fm.issueStatusExcluMissingWork;
        const jiraWaitStatus = fm.jiraWaitStatus;
        const jiraBlockedStatus = fm.jiraBlockedStatus;
        const jiraIncludeBlockedStatus = fm.jiraIncludeBlockedStatus;
        const jiraDevDoneStatus = fm.jiraDevDoneStatus;
        const jiraQADefectDensityIssueType = fm.jiraQADefectDensityIssueType;
        const jiraDefectInjectionIssueType = fm.jiraDefectInjectionIssueType;
        const jiraDefectCreatedStatus = fm.jiraDefectCreatedStatus;
        const jiraDefectSeepageIssueType = fm.jiraDefectSeepageIssueType;
        const jiraDefectRemovalStatus = fm.jiraDefectRemovalStatus;
        const jiraDefectRemovalIssueType = fm.jiraDefectRemovalIssueType;
        const jiraSprintCapacityIssueType = fm.jiraSprintCapacityIssueType;
        const jiraDefectRejectionlIssueType = fm.jiraDefectRejectionlIssueType;
        const jiraStatusForQa = fm.jiraStatusForQa;
        const storyFirstStatus = fm.storyFirstStatus;
        const jiraFtprRejectStatus = fm.jiraFtprRejectStatus;
        const jiraDefectClosedStatus = fm.jiraDefectClosedStatus;
        const jiraDefectDroppedStatus = fm.jiraDefectDroppedStatus;
        const jiraAcceptedInRefinement = fm.jiraAcceptedInRefinement;
        const jiraReadyForRefinement = fm.jiraReadyForRefinement;
        const jiraRejectedInRefinement = fm.jiraRejectedInRefinement;
        const readyForDevelopmentStatus = fm.readyForDevelopmentStatus;
        db.field_mapping.updateOne({
            "_id": fm._id
        }, {
             $set: {
                "defectPriorityKPI135": defectPriority,
                "defectPriorityKPI14": defectPriority,
                "defectPriorityQAKPI111": defectPriority,
                "defectPriorityKPI82": defectPriority,
                "defectPriorityKPI133": defectPriority,

                "jiraIssueTypeNamesAVR": jiraIssueTypeNames,

                "jiraStatusForDevelopmentAVR": jiraStatusForDevelopment,
                "jiraStatusForDevelopmentKPI82": jiraStatusForDevelopment,
                "jiraStatusForDevelopmentKPI135": jiraStatusForDevelopment,

                "jiraDefectInjectionIssueTypeKPI14": jiraDefectInjectionIssueType,

                "jiraDodKPI14": jiraDod,
                "jiraDodQAKPI111": jiraDod,
                "jiraDodKPI3": jiraDod,
                "jiraDodKPI127": jiraDod,
                "jiraDodKPI152": jiraDod,
                "jiraDodKPI151": jiraDod,

                "jiraDefectCreatedStatusKPI14": jiraDefectCreatedStatus,

                "jiraDefectRejectionStatusAVR": jiraDefectRejectionStatus,
                "jiraDefectRejectionStatusKPI28": jiraDefectRejectionStatus,
                "jiraDefectRejectionStatusKPI34": jiraDefectRejectionStatus,
                "jiraDefectRejectionStatusKPI37": jiraDefectRejectionStatus,
                "jiraDefectRejectionStatusKPI35": jiraDefectRejectionStatus,
                "jiraDefectRejectionStatusKPI82": jiraDefectRejectionStatus,
                "jiraDefectRejectionStatusKPI135": jiraDefectRejectionStatus,
                "jiraDefectRejectionStatusKPI133": jiraDefectRejectionStatus,
                "jiraDefectRejectionStatusRCAKPI36": jiraDefectRejectionStatus,
                "jiraDefectRejectionStatusKPI14": jiraDefectRejectionStatus,
                "jiraDefectRejectionStatusQAKPI111": jiraDefectRejectionStatus,
                "jiraDefectRejectionStatusKPI152": jiraDefectRejectionStatus,
                "jiraDefectRejectionStatusKPI151": jiraDefectRejectionStatus,

                "jiraIssueTypeKPI35": jiraDefectSeepageIssueType,

                "jiraDefectRemovalStatusKPI34": jiraDefectRemovalStatus,
                "jiraDefectRemovalIssueTypeKPI34": jiraDefectRemovalIssueType,

                "jiraSprintVelocityIssueTypeBR": jiraSprintVelocityIssueType,

                "jiraSprintCapacityIssueTypeKpi46": jiraSprintCapacityIssueType,

                "jiraIssueTypeKPI37": jiraDefectRejectionlIssueType,

                "jiraDefectCountlIssueTypeKPI28": jiraDefectCountlIssueType,
                "jiraDefectCountlIssueTypeKPI36": jiraDefectCountlIssueType,

                "jiraIssueDeliverdStatusKPI138": jiraIssueDeliverdStatus,
                "jiraIssueDeliverdStatusAVR": jiraIssueDeliverdStatus,
                "jiraIssueDeliverdStatusKPI126": jiraIssueDeliverdStatus,
                "jiraIssueDeliverdStatusKPI82": jiraIssueDeliverdStatus,

                "jiraDorKPI3": jiraDor,

                "jiraIssueTypeKPI3": jiraIntakeToDorIssueType,

                "storyFirstStatusKPI3": storyFirstStatus,
                "storyFirstStatusKPI148": storyFirstStatus,

                "jiraStoryIdentificationKpi40": jiraStoryIdentification,
                "jiraStoryIdentificationKPI129": jiraStoryIdentification,

                "jiraDefectClosedStatusKPI137": jiraDefectClosedStatus,

                "jiraKPI82StoryIdentification": jiraFTPRStoryIdentification,
                "jiraKPI135StoryIdentification": jiraFTPRStoryIdentification,

                "jiraLiveStatusKPI3": jiraLiveStatus,
                "jiraLiveStatusLTK": jiraLiveStatus,
                "jiraLiveStatusNOPK": jiraLiveStatus,
                "jiraLiveStatusNOSK": jiraLiveStatus,
                "jiraLiveStatusNORK": jiraLiveStatus,
                "jiraLiveStatusOTA": jiraLiveStatus,
                "jiraLiveStatusKPI127": jiraLiveStatus,
                "jiraLiveStatusKPI152": jiraLiveStatus,
                "jiraLiveStatusKPI151": jiraLiveStatus,

                "excludeRCAFromKPI82": excludeRCAFromFTPR,
                "excludeRCAFromKPI135": excludeRCAFromFTPR,
                "excludeRCAFromKPI14": excludeRCAFromFTPR,
                "excludeRCAFromQAKPI111": excludeRCAFromFTPR,
                "excludeRCAFromKPI133": excludeRCAFromFTPR,

                "resolutionTypeForRejectionAVR": resolutionTypeForRejection,
                "resolutionTypeForRejectionKPI28": resolutionTypeForRejection,
                "resolutionTypeForRejectionKPI34": resolutionTypeForRejection,
                "resolutionTypeForRejectionKPI37": resolutionTypeForRejection,
                "resolutionTypeForRejectionKPI35": resolutionTypeForRejection,
                "resolutionTypeForRejectionKPI82": resolutionTypeForRejection,
                "resolutionTypeForRejectionKPI135": resolutionTypeForRejection,
                "resolutionTypeForRejectionKPI133": resolutionTypeForRejection,
                "resolutionTypeForRejectionRCAKPI36": resolutionTypeForRejection,
                "resolutionTypeForRejectionKPI14": resolutionTypeForRejection,
                "resolutionTypeForRejectionQAKPI111": resolutionTypeForRejection,

                "jiraQAKPI111IssueType": jiraQADefectDensityIssueType,

                "jiraStatusForQaKPI135": jiraStatusForQa,
                "jiraStatusForQaKPI82": jiraStatusForQa,
                "jiraStatusForQaKPI48": jiraStatusForQa,

                "jiraStatusForInProgressKPI122": jiraStatusForInProgress,
                "jiraStatusForInProgressKPI145": jiraStatusForInProgress,
                "jiraStatusForInProgressKPI125": jiraStatusForInProgress,
                "jiraStatusForInProgressKPI128": jiraStatusForInProgress,
                "jiraStatusForInProgressKPI123": jiraStatusForInProgress,
                "jiraStatusForInProgressKPI119": jiraStatusForInProgress,
                "jiraStatusForInProgressKPI148": jiraStatusForInProgress,

                "issueStatusExcluMissingWorkKPI124": issueStatusExcluMissingWork,

                "jiraDevDoneStatusKPI119": jiraDevDoneStatus,
                "jiraDevDoneStatusKPI145": jiraDevDoneStatus,
                "jiraDevDoneStatusKPI128": jiraDevDoneStatus,

                "jiraWaitStatusKPI131": jiraWaitStatus,


                "jiraBlockedStatusKPI131": jiraBlockedStatus,

                "jiraIncludeBlockedStatusKPI131": jiraIncludeBlockedStatus,

                "jiraFtprRejectStatusKPI135": jiraFtprRejectStatus,
                "jiraFtprRejectStatusKPI82": jiraFtprRejectStatus,

                "jiraIterationCompletionStatusKPI135": jiraIterationCompletionStatusCustomField,
                "jiraIterationCompletionStatusKPI122": jiraIterationCompletionStatusCustomField,
                "jiraIterationCompletionStatusKPI75": jiraIterationCompletionStatusCustomField,
                "jiraIterationCompletionStatusKPI145": jiraIterationCompletionStatusCustomField,
                "jiraIterationCompletionStatusKPI140": jiraIterationCompletionStatusCustomField,
                "jiraIterationCompletionStatusKPI132": jiraIterationCompletionStatusCustomField,
                "jiraIterationCompletionStatusKPI136": jiraIterationCompletionStatusCustomField,
                "jiraIterationCompletionStatusKpi40": jiraIterationCompletionStatusCustomField,
                "jiraIterationCompletionStatusKpi72": jiraIterationCompletionStatusCustomField,
                "jiraIterationCompletionStatusKpi39": jiraIterationCompletionStatusCustomField,
                "jiraIterationCompletionStatusKpi5": jiraIterationCompletionStatusCustomField,
                "jiraIterationCompletionStatusKPI124": jiraIterationCompletionStatusCustomField,
                "jiraIterationCompletionStatusKPI123": jiraIterationCompletionStatusCustomField,
                "jiraIterationCompletionStatusKPI125": jiraIterationCompletionStatusCustomField,
                "jiraIterationCompletionStatusKPI120": jiraIterationCompletionStatusCustomField,
                "jiraIterationCompletionStatusKPI128": jiraIterationCompletionStatusCustomField,
                "jiraIterationCompletionStatusKPI134": jiraIterationCompletionStatusCustomField,
                "jiraIterationCompletionStatusKPI133": jiraIterationCompletionStatusCustomField,
                "jiraIterationCompletionStatusKPI119": jiraIterationCompletionStatusCustomField,
                "jiraIterationCompletionStatusKPI131": jiraIterationCompletionStatusCustomField,
                "jiraIterationCompletionStatusKPI138": jiraIterationCompletionStatusCustomField,

                "jiraIterationIssuetypeKPI122": jiraIterationCompletionTypeCustomField,
                "jiraIterationIssuetypeKPI138": jiraIterationCompletionTypeCustomField,
                "jiraIterationIssuetypeKPI131": jiraIterationCompletionTypeCustomField,
                "jiraIterationIssuetypeKPI128": jiraIterationCompletionTypeCustomField,
                "jiraIterationIssuetypeKPI134": jiraIterationCompletionTypeCustomField,
                "jiraIterationIssuetypeKPI145": jiraIterationCompletionTypeCustomField,
                "jiraIterationIssuetypeKpi72": jiraIterationCompletionTypeCustomField,
                "jiraIterationIssuetypeKPI119": jiraIterationCompletionTypeCustomField,
                "jiraIterationIssuetypeKpi5": jiraIterationCompletionTypeCustomField,
                "jiraIterationIssuetypeKPI75": jiraIterationCompletionTypeCustomField,
                "jiraIterationIssuetypeKPI123": jiraIterationCompletionTypeCustomField,
                "jiraIterationIssuetypeKPI125": jiraIterationCompletionTypeCustomField,
                "jiraIterationIssuetypeKPI120": jiraIterationCompletionTypeCustomField,
                "jiraIterationIssuetypeKPI124": jiraIterationCompletionTypeCustomField,

                "jiraDefectDroppedStatusKPI127": jiraDefectDroppedStatus,

                "jiraAcceptedInRefinementKPI139": jiraAcceptedInRefinement,

                "jiraReadyForRefinementKPI139": jiraReadyForRefinement,

                "jiraRejectedInRefinementKPI139": jiraRejectedInRefinement,

                "readyForDevelopmentStatusKPI138": readyForDevelopmentStatus,

                "createdDate": new Date(Date.now())
            }
        })
    }
})

//--------insert field_mapping_structure
db.kpi_fieldmapping.drop();
if(db.field_mapping_structure.find().count()==0){
db.getCollection('field_mapping_structure').insert(
    [{
            "fieldName": "jiraStoryIdentificationKpi40",
            "fieldLabel": "Issue type to identify Story",
            "fieldType": "chips",
            "fieldCategory": "Issue_Type",
            "section": "Issue Types Mapping",
            "tooltip": {
                "definition": "All issue types that are used as/equivalent to Story.",

            }
        }, {
            "fieldName": "jiraStoryIdentificationKPI129",
            "fieldLabel": "Issue type to identify Story",
            "fieldType": "chips",
            "fieldCategory": "Issue_Type",
            "section": "Issue Types Mapping",
            "tooltip": {
                "definition": "All issue types that are used as/equivalent to Story.",

            }
        },
        {
            "fieldName": "jiraSprintCapacityIssueTypeKpi46",
            "fieldLabel": "Issue type to be included",
            "fieldType": "chips",
            "fieldCategory": "Issue_Type",
            "section": "Issue Types Mapping",
            "tooltip": {
                "definition": "All issues types against work is logged and should be considered for Utilization"
            }
        },
        {
            "fieldName": "jiraIterationIssuetypeKPI122",
            "fieldLabel": "Issue type to be included",
            "fieldType": "chips",
            "fieldCategory": "Issue_Type",
            "section": "Issue Types Mapping",
            "tooltip": {
                "definition": "All issues types added will only be included in showing closures (Note: If nothing is added then all issue types by default will be considered)"
            }
        },
        {
            "fieldName": "jiraIterationIssuetypeKPI124",
            "fieldLabel": "Issue type to be included",
            "fieldType": "chips",
            "fieldCategory": "Issue_Type",
            "section": "Issue Types Mapping",
            "tooltip": {
                "definition": "All issues types added will only be included in showing closures (Note: If nothing is added then all issue types by default will be considered)"
            }
        },
        {
            "fieldName": "jiraIterationIssuetypeKPI138",
            "fieldLabel": "Iteration Board Issue types",
            "fieldType": "chips",
            "fieldCategory": "Issue_Type",
            "section": "Issue Types Mapping",
            "tooltip": {
                "definition": "Issue Types to be considered Completed"
            }
        },
        {
            "fieldName": "jiraIterationIssuetypeKPI131",
            "fieldLabel": "Issue type to be included",
            "fieldType": "chips",
            "fieldCategory": "Issue_Type",
            "section": "Issue Types Mapping",
            "tooltip": {
                "definition": "All issues types added will only be included in showing closures (Note: If nothing is added then all issue types by default will be considered)"
            }
        },
        {
            "fieldName": "jiraIterationIssuetypeKPI128",
            "fieldLabel": "Issue type to be included",
            "fieldType": "chips",
            "fieldCategory": "Issue_Type",
            "section": "Issue Types Mapping",
            "tooltip": {
                "definition": "All issues types added will only be included in showing closures (Note: If nothing is added then all issue types by default will be considered)"
            }
        },
        {
            "fieldName": "jiraIterationIssuetypeKPI134",
            "fieldLabel": "Issue type to be included",
            "fieldType": "chips",
            "fieldCategory": "Issue_Type",
            "section": "Issue Types Mapping",
            "tooltip": {
                "definition": "All issues types added will only be included in showing closures (Note: If nothing is added then all issue types by default will be considered)"
            }
        },
        {
            "fieldName": "jiraIterationIssuetypeKPI145",
            "fieldLabel": "Issue type to be included",
            "fieldType": "chips",
            "fieldCategory": "Issue_Type",
            "section": "Issue Types Mapping",
            "tooltip": {
                "definition": "All issues types added will only be included in showing closures (Note: If nothing is added then all issue types by default will be considered)"
            }
        },
        {
            "fieldName": "jiraIterationIssuetypeKpi72",
            "fieldLabel": "Issue type to be included",
            "fieldType": "chips",
            "fieldCategory": "Issue_Type",
            "section": "Issue Types Mapping",
            "tooltip": {
                "definition": "All issue types that are considered in sprint commitment"
            }
        }, {
            "fieldName": "jiraIterationIssuetypeKpi5",
            "fieldLabel": "Issue type to be included",
            "fieldType": "chips",
            "fieldCategory": "Issue_Type",
            "section": "Issue Types Mapping",
            "tooltip": {
                "definition": "All issue types that should be included in Sprint Predictability calculation"
            }
        },
        {
            "fieldName": "jiraIterationIssuetypeKPI119",
            "fieldLabel": "Issue type to be included",
            "fieldType": "chips",
            "fieldCategory": "Issue_Type",
            "section": "Issue Types Mapping",
            "tooltip": {
                "definition": "All issues types added will only be included in showing closures (Note: If nothing is added then all issue types by default will be considered)"
            }
        },
        {
            "fieldName": "jiraIterationIssuetypeKPI75",
            "fieldLabel": "Issue type to be included",
            "fieldType": "chips",
            "fieldCategory": "Issue_Type",
            "section": "Issue Types Mapping",
            "tooltip": {
                "definition": "All issues types added will only be included in showing closures (Note: If nothing is added then all issue types by default will be considered)"
            }
        },
        {
            "fieldName": "jiraIterationIssuetypeKPI123",
            "fieldLabel": "Issue type to be included",
            "fieldType": "chips",
            "fieldCategory": "Issue_Type",
            "section": "Issue Types Mapping",
            "tooltip": {
                "definition": "All issues types added will only be included in showing closures (Note: If nothing is added then all issue types by default will be considered)"
            }
        },
        {
            "fieldName": "jiraIterationIssuetypeKPI125",
            "fieldLabel": "Issue type to be included",
            "fieldType": "chips",
            "fieldCategory": "Issue_Type",
            "section": "Issue Types Mapping",
            "tooltip": {
                "definition": "All issues types added will only be included in showing closures (Note: If nothing is added then all issue types by default will be considered)"
            }
        },
        {
            "fieldName": "jiraIterationIssuetypeKPI120",
            "fieldLabel": "Issue type to be included",
            "fieldType": "chips",
            "fieldCategory": "Issue_Type",
            "section": "Issue Types Mapping",
            "tooltip": {
                "definition": "All issues types added will only be included in showing closures (Note: If nothing is added then all issue types by default will be considered)"
            }
        },
        {
            "fieldName": "jiraSprintVelocityIssueTypeBR",
            "fieldLabel": "Sprint Velocity - Issue Types with Linked Defect",
            "fieldType": "chips",
            "fieldCategory": "Issue_Type",
            "section": "Issue Types Mapping",
            "tooltip": {
                "definition": "All issue types with which defect is linked. <br>  Example: Story, Change Request .<hr>"
            }
        },
        {
            "fieldName": "jiraSprintVelocityIssueTypeEH",
            "fieldLabel": "Sprint Velocity - Issue Types with Linked Defect",
            "fieldType": "chips",
            "fieldCategory": "Issue_Type",
            "section": "Issue Types Mapping",
            "tooltip": {
                "definition": "All issue types with which defect is linked. <br>  Example: Story, Change Request .<hr>"
            }
        },
        {
            "fieldName": "jiraIssueDeliverdStatusKPI138",
            "fieldLabel": "Issue Delivered Status",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "Status from workflow on which issue is delivered. <br> Example: Closed<hr>"
            }
        },
        {
            "fieldName": "jiraIssueDeliverdStatusKPI126",
            "fieldLabel": "Status to identify completed issues",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "Status considered for defect closure (Mention completed status of all types of defects)"
            }
        },
        {
            "fieldName": "jiraIssueDeliverdStatusKPI82",
            "fieldLabel": "Status to identify completed issues",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "Completion status for all issue types mentioned for calculation of FTPR"
            }
        },
        {
            "fieldName": "resolutionTypeForRejectionKPI28",
            "fieldLabel": "Resolution type to be excluded",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "Resolutions for defects which are to be excluded from 'Defect count by Priority' calculation"
            }
        },
        {
            "fieldName": "resolutionTypeForRejectionKPI34",
            "fieldLabel": "Resolution type to be excluded",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "Resolutions for defects which are to be excluded from 'Defect Removal Efficiency' calculation."
            }
        },
        {
            "fieldName": "resolutionTypeForRejectionKPI37",
            "fieldLabel": "Resolution type to be included",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "Resolutions for defects which are to be excluded from 'Defect Rejection Rate' calculation."
            }
        },
        {
            "fieldName": "resolutionTypeForRejectionDSR",
            "fieldLabel": "Resolution Type for Rejection",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "Resolution type to identify rejected defects. <br>"
            }
        },
        {
            "fieldName": "resolutionTypeForRejectionKPI82",
            "fieldLabel": "Resolution type to be excluded",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "Resolutions for defects which are to be excluded from 'FTPR' calculation"
            }
        },
        {
            "fieldName": "resolutionTypeForRejectionKPI135",
            "fieldLabel": "Resolution type to be excluded",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "Resolutions for defects which are to be excluded from 'FTPR' calculation"
            }
        },
        {
            "fieldName": "resolutionTypeForRejectionKPI133",
            "fieldLabel": "Resolution type to be excluded",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "Resolutions for defects which are to be excluded from 'Quality Status' calculation"
            }
        },
        {
            "fieldName": "resolutionTypeForRejectionRCAKPI36",
            "fieldLabel": "Resolution type to be excluded",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "Resolutions for defects which are to be excluded from 'Defect count by RCA' calculation."
            }
        },
        {
            "fieldName": "resolutionTypeForRejectionKPI14",
            "fieldLabel": "Resolution type to be excluded",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "Resolutions for defects which are to be excluded from 'Defect Injection rate' calculation <br>"
            }
        },
        {
            "fieldName": "resolutionTypeForRejectionQAKPI111",
            "fieldLabel": "Resolution type to be excluded",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "Resolutions for defects which are to be excluded from 'Defect Density' calculation."
            }
        },

        {
            "fieldName": "jiraDefectRejectionStatusKPI28",
            "fieldLabel": "Status to be excluded",
            "fieldType": "text",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "All statuses which are considered for Rejecting defects."
            }
        },
        {
            "fieldName": "jiraDefectRejectionStatusKPI34",
            "fieldLabel": "Status to be excluded",
            "fieldType": "text",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "All statuses which are considered for Rejecting defects."
            }
        },
        {
            "fieldName": "jiraDefectRejectionStatusKPI37",
            "fieldLabel": "Status to identify Rejected defects",
            "fieldType": "text",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "All statuses which are considered for Rejecting defects."
            }
        },
        {
            "fieldName": "jiraDefectRejectionStatusDSR",
            "fieldLabel": "Defect Rejection Status",
            "fieldType": "text",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "Status from workflow on which defect is considered as rejected. <br>Example: Cancelled<hr>"
            }
        },
        {
            "fieldName": "jiraDefectRejectionStatusKPI82",
            "fieldLabel": "Status to be excluded",
            "fieldType": "text",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "All statuses which are considered for Rejecting defects"
            }
        },
        {
            "fieldName": "jiraDefectRejectionStatusKPI135",
            "fieldLabel": "Defect Rejection Status",
            "fieldType": "text",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "Status from workflow on which defect is considered as rejected. <br>Example: Cancelled<hr>"
            }
        },
        {
            "fieldName": "jiraDefectRejectionStatusKPI133",
            "fieldLabel": "Status to be excluded",
            "fieldType": "text",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "All statuses which are considered for Rejecting defects."
            }
        },
        {
            "fieldName": "jiraDefectRejectionStatusRCAKPI36",
            "fieldLabel": "Status to be excluded",
            "fieldType": "text",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "All statuses which are considered for Rejecting defects."
            }
        },
        {
            "fieldName": "jiraDefectRejectionStatusKPI14",
            "fieldLabel": "Status to be excluded",
            "fieldType": "text",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "All statuses which are considered for Rejecting defects"
            }
        },
        {
            "fieldName": "jiraDefectRejectionStatusQAKPI111",
            "fieldLabel": "Status to be excluded",
            "fieldType": "text",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "All statuses which are considered for Rejecting defects."
            }
        },
        {
            "fieldName": "jiraDefectRejectionStatusKPI151",
            "fieldLabel": "Status to be excluded",
            "fieldType": "text",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "All statuses which are considered for Rejecting defects."
            }
        },
        {
            "fieldName": "jiraDefectRejectionStatusKPI152",
            "fieldLabel": "Status to be excluded",
            "fieldType": "text",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "All statuses which are considered for Rejecting defects."
            }
        },
        {
            "fieldName": "jiraStatusForDevelopmentKPI82",
            "fieldLabel": "Status for 'In Development' issues",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "All statuses that relate to In development status of a Story"
            }
        },
        {
            "fieldName": "jiraStatusForDevelopmentKPI135",
            "fieldLabel": "Status for 'In Development' issues",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "All statuses that relate to In development status of a Story"
            }
        },
        {
            "fieldName": "jiraDorKPI3",
            "fieldLabel": "Status to Identify Development Status",
            "fieldType": "text",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "Definition of Readiness. Provide any status from workflow on which DOR is considered."
            }
        },
        {
            "fieldName": "jiraIssueTypeKPI3",
            "fieldLabel": "Lead time issue type",
            "fieldType": "chips",
            "fieldCategory": "Issue_Type",
            "section": "Issue Types Mapping",
            "tooltip": {
                "definition": "The issue type which is to be considered while calculating lead time KPIs, i.e. intake to DOR and DOR and DOD."
            }
        },
        {
            "fieldName": "jiraKPI82StoryIdentification",
            "fieldLabel": "Issue type to be included",
            "fieldType": "chips",
            "fieldCategory": "Issue_Type",
            "section": "Issue Types Mapping",
            "tooltip": {
                "definition": "All issue types for which FTPR should be calculated"
            }
        },
        {
            "fieldName": "jiraKPI135StoryIdentification",
            "fieldLabel": "Issue type to be included",
            "fieldType": "chips",
            "fieldCategory": "Issue_Type",
            "section": "Issue Types Mapping",
            "tooltip": {
                "definition": "All issue types for which FTPR should be calculated"
            }
        },
        {
            "fieldName": "defectPriorityKPI135",
            "fieldLabel": "Defect priority exclusion from Quality KPIs",
            "fieldType": "multiselect",
            "section": "Defects Mapping",
            "tooltip": {
                "definition": "The defects tagged to priority values selected in this field on Mappings screen will be excluded"
            },
            "options": [{
                    "label": "p1",
                    "value": "p1"
                },
                {
                    "label": "p2",
                    "value": "p2"
                },
                {
                    "label": "p3",
                    "value": "p3"
                },
                {
                    "label": "p4",
                    "value": "p4"
                },
                {
                    "label": "p5",
                    "value": "p5"
                }
            ]
        },
        {
            "fieldName": "defectPriorityKPI14",
            "fieldLabel": "Priority to be included",
            "fieldType": "multiselect",
            "section": "Defects Mapping",
            "tooltip": {
                "definition": "Priority values of defects which are to be considered in 'Defect Injection rate' calculation"
            },
            "options": [{
                    "label": "p1",
                    "value": "p1"
                },
                {
                    "label": "p2",
                    "value": "p2"
                },
                {
                    "label": "p3",
                    "value": "p3"
                },
                {
                    "label": "p4",
                    "value": "p4"
                },
                {
                    "label": "p5",
                    "value": "p5"
                }
            ]
        },
        {
            "fieldName": "defectPriorityQAKPI111",
            "fieldLabel": "Priority to be included",
            "fieldType": "multiselect",
            "section": "Defects Mapping",
            "tooltip": {
                "definition": "Priority values of defects which are to be considered in 'Defect Density' calculation"
            },
            "options": [{
                    "label": "p1",
                    "value": "p1"
                },
                {
                    "label": "p2",
                    "value": "p2"
                },
                {
                    "label": "p3",
                    "value": "p3"
                },
                {
                    "label": "p4",
                    "value": "p4"
                },
                {
                    "label": "p5",
                    "value": "p5"
                }
            ]
        },
        {
            "fieldName": "defectPriorityKPI82",
            "fieldLabel": "Priority to be included",
            "fieldType": "multiselect",
            "section": "Defects Mapping",
            "tooltip": {
                "definition": "Priority values of defects which are to be considered in 'FTPR' calculation"
            },
            "options": [{
                    "label": "p1",
                    "value": "p1"
                },
                {
                    "label": "p2",
                    "value": "p2"
                },
                {
                    "label": "p3",
                    "value": "p3"
                },
                {
                    "label": "p4",
                    "value": "p4"
                },
                {
                    "label": "p5",
                    "value": "p5"
                }
            ]
        },
        {
            "fieldName": "defectPriorityKPI133",
            "fieldLabel": "Priority to be included",
            "fieldType": "multiselect",
            "section": "Defects Mapping",
            "tooltip": {
                "definition": "Priority values of defects which are to be considered in 'Quality Status' calculation"
            },
            "options": [{
                    "label": "p1",
                    "value": "p1"
                },
                {
                    "label": "p2",
                    "value": "p2"
                },
                {
                    "label": "p3",
                    "value": "p3"
                },
                {
                    "label": "p4",
                    "value": "p4"
                },
                {
                    "label": "p5",
                    "value": "p5"
                }
            ]
        },
        {
            "fieldName": "excludeRCAFromKPI82",
            "fieldLabel": "Root cause values to be excluded",
            "fieldType": "chips",
            "section": "Defects Mapping",
            "tooltip": {
                "definition": "Root cause reasons for defects which are to be excluded from 'FTPR' calculation"
            }
        },
        {
            "fieldName": "excludeRCAFromKPI135",
            "fieldLabel": "Defect RCA exclusion from Quality KPIs",
            "fieldType": "chips",
            "section": "Defects Mapping",
            "tooltip": {
                "definition": "The defects tagged to priority values selected in this field on Mappings screen will be excluded"
            }
        },
        {
            "fieldName": "excludeRCAFromKPI14",
            "fieldLabel": "Root cause values to be excluded",
            "fieldType": "chips",
            "section": "Defects Mapping",
            "tooltip": {
                "definition": "Root cause reasons for defects which are to be excluded from 'Defect Injection rate' calculation"
            }
        },
        {
            "fieldName": "excludeRCAFromQAKPI111",
            "fieldLabel": "Root cause values to be excluded",
            "fieldType": "chips",
            "section": "Defects Mapping",
            "tooltip": {
                "definition": "Root cause reasons for defects which are to be excluded from 'Defect Density' calculation"
            }
        },
        {
            "fieldName": "excludeRCAFromKPI133",
            "fieldLabel": "Root cause values to be excluded",
            "fieldType": "chips",
            "section": "Defects Mapping",
            "tooltip": {
                "definition": "Root cause reasons for defects which are to be excluded from 'Quality Status' calculation"
            }
        },
        {
            "fieldName": "jiraDefectInjectionIssueTypeKPI14",
            "fieldLabel": "Issue types which will have linked defects",
            "fieldType": "chips",
            "fieldCategory": "Issue_Type",
            "section": "Issue Types Mapping",
            "tooltip": {
                "definition": "Issue type that will have defects linked to them."
            }
        },
        {
            "fieldName": "jiraDefectCreatedStatusKPI14",
            "fieldLabel": "Default status when defect is created",
            "fieldType": "text",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "Default status when upon creation of Defect (Mention default status of all types of defects)"
            }
        },
        {
            "fieldName": "jiraDod",
            "fieldLabel": "Status to identify DOD",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": " Definition of Doneness. Provide any status from workflow on which DOD is considered."
            }
        },
        {
            "fieldName": "jiraDodKPI14",
            "fieldLabel": "Status considered for defect closure",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "Status considered for defect closure (Mention completed status of all types of defects)"
            }
        },
        {
            "fieldName": "jiraDodQAKPI111",
            "fieldLabel": "Status considered for defect closure",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "Status considered for defect closure (Mention completed status of all types of defects)"
            }
        },
        {
            "fieldName": "jiraDodKPI3",
            "fieldLabel": "DOD Status",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "Status/es that identify that an issue is completed based on Definition of Done (DoD)"
            }
        },
        {
            "fieldName": "jiraDodKPI127",
            "fieldLabel": "Status to identify completed issues",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "Status/es that identify that an issue is completed based on Definition of Done (DoD)"
            }
        },
        {
            "fieldName": "jiraDodKPI152",
            "fieldLabel": "DOD Status",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "......."
            }
        },
        {
            "fieldName": "jiraDodKPI151",
            "fieldLabel": "DOD Status",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "......."
            }
        },
        {
            "fieldName": "jiraQAKPI111IssueType",
            "fieldLabel": "Issue types which will have linked defects",
            "fieldType": "chips",
            "fieldCategory": "Issue_Type",
            "section": "Issue Types Mapping",
            "tooltip": {
                "definition": "Issue type that will have defects linked to them."
            }
        },
        {
            "fieldName": "jiraIssueTypeKPI35",
            "fieldLabel": "Issue types which will have linked defects",
            "fieldType": "chips",
            "fieldCategory": "Issue_Type",
            "section": "Issue Types Mapping",
            "tooltip": {
                "definition": "Issue type that will have defects linked to them."
            }
        },
        {
            "fieldName": "jiraDefectRemovalIssueTypeKPI34",
            "fieldLabel": "Issue type to be included.",
            "fieldType": "chips",
            "fieldCategory": "Issue_Type",
            "section": "Issue Types Mapping",
            "tooltip": {
                "definition": "Issue types that are considered as defects in Jira."
            }
        },
        {
            "fieldName": "jiraIssueTypeKPI37",
            "fieldLabel": "Issue type to be included",
            "fieldType": "chips",
            "fieldCategory": "Issue_Type",
            "section": "Issue Types Mapping",
            "tooltip": {
                "definition": "Issue types that are considered as defects in Jira"
            }
        },
        {
            "fieldName": "jiraIssueTypeNames",
            "fieldLabel": "Issue types to be included",
            "fieldType": "chips",
            "fieldCategory": "Issue_Type",
            "processorCommon": true,
            "section": "Issue Types Mapping",
            "tooltip": {
                "definition": "All the issue types used by a project in Jira."
            }
        },
        {
            "fieldName": "jiraDefectRemovalStatusKPI34",
            "fieldLabel": "Status to identify closed defects",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "All statuses which are used when defect is fixed & closed."
            }
        },
        {
            "fieldName": "jiraIssueDeliverdStatusCVR",
            "fieldLabel": "Issue Delivered Status",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "Status from workflow on which issue is delivered. <br> Example: Closed<hr>"
            }
        },
        {
            "fieldName": "resolutionTypeForRejectionKPI35",
            "fieldLabel": "Resolution type to be excluded",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "Resolutions for defects which are to be excluded from 'Defect Seepage rate' calculation."
            }
        },
        {
            "fieldName": "jiraDefectRejectionStatusKPI35",
            "fieldLabel": "Status to be excluded",
            "fieldType": "text",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "All statuses which are considered for Rejecting defects"
            }
        },
        {
            "fieldName": "jiraIterationCompletionStatusKPI135",
            "fieldLabel": "Status to identify completed issues",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "All statuses that signify completion for a team. (If more than one status configured, then the first status that the issue transitions to will be counted as Completion)"
            }
        },
        {
            "fieldName": "jiraIterationCompletionStatusCustomField",
            "fieldLabel": "Status to identify completed issues",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "Status to identify as closed"
            }
        },
        {
            "fieldName": "jiraIterationCompletionStatusKPI122",
            "fieldLabel": "Status to identify completed issues",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "All statuses that signify completion for a team. (If more than one status configured, then the first status that the issue transitions to will be counted as Completion)"
            }
        },
        {
            "fieldName": "jiraIterationCompletionStatusKPI75",
            "fieldLabel": "Status to identify completed issues",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "All statuses that signify completion for a team. (If more than one status configured, then the first status that the issue transitions to will be counted as Completion)"
            }
        },
        {
            "fieldName": "jiraIterationCompletionStatusKPI145",
            "fieldLabel": "Status to identify completed issues",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "All statuses that signify completion for a team. (If more than one status configured, then the first status that the issue transitions to will be counted as Completion)"
            }
        },
        {
            "fieldName": "jiraIterationCompletionStatusKPI140",
            "fieldLabel": "Status to identify completed issues",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "All statuses that signify completion for a team. (If more than one status configured, then the first status that the issue transitions to will be counted as Completion)"
            }
        },
        {
            "fieldName": "jiraIterationCompletionStatusKPI132",
            "fieldLabel": "Status to identify completed issues",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "All statuses that signify completion for a team. (If more than one status configured, then the first status that the issue transitions to will be counted as Completion)"
            }
        },
        {
            "fieldName": "jiraIterationCompletionStatusKPI136",
            "fieldLabel": "Status to identify completed issues",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "All statuses that signify completion for a team. (If more than one status configured, then the first status that the issue transitions to will be counted as Completion)"
            }
        },
        {
            "fieldName": "jiraIterationCompletionStatusKpi72",
            "fieldLabel": "Status to identify completed issues",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "All statuses that signify completion for a team. (If more than one status configured, then the first status that the issue transitions to will be counted as Completion)"
            }
        },
        {
            "fieldName": "jiraIterationCompletionStatusKpi39",
            "fieldLabel": "Status to identify completed issues",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "All statuses that signify completion for a team. (If more than one status configured, then the first status that the issue transitions to will be counted as Completion)"
            }
        },
        {
            "fieldName": "jiraIterationCompletionStatusKpi5",
            "fieldLabel": "Status to identify completed issues",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "All statuses that signify completion for a team. (If more than one status configured, then the first status that the issue transitions to will be counted as Completion)"
            }
        },
        {
            "fieldName": "jiraIterationCompletionStatusKPI124",
            "fieldLabel": "Status to identify completed issues",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "All statuses that signify completion for a team. (If more than one status configured, then the first status that the issue transitions to will be counted as Completion)"
            }
        },
        {
            "fieldName": "jiraIterationCompletionStatusKPI123",
            "fieldLabel": "Status to identify completed issues",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "All statuses that signify completion for a team. (If more than one status configured, then the first status that the issue transitions to will be counted as Completion)"
            }
        },
        {
            "fieldName": "jiraIterationCompletionStatusKPI125",
            "fieldLabel": "Status to identify completed issues",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "All statuses that signify completion for a team. (If more than one status configured, then the first status that the issue transitions to will be counted as Completion)"
            }
        },
        {
            "fieldName": "jiraIterationCompletionStatusKPI120",
            "fieldLabel": "Status to identify completed issues",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "All statuses that signify completion for a team. (If more than one status configured, then the first status that the issue transitions to will be counted as Completion)"
            }
        },
        {
            "fieldName": "jiraIterationCompletionStatusKPI128",
            "fieldLabel": "Status to identify completed issues",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "All statuses that signify completion for a team. (If more than one status configured, then the first status that the issue transitions to will be counted as Completion)"
            }
        },
        {
            "fieldName": "jiraIterationCompletionStatusKPI134",
            "fieldLabel": "Status to identify completed issues",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "All statuses that signify completion for a team. (If more than one status configured, then the first status that the issue transitions to will be counted as Completion)"
            }
        },
        {
            "fieldName": "jiraIterationCompletionStatusKPI133",
            "fieldLabel": "Status to identify completed issues",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "All statuses that signify completion for a team. (If more than one status configured, then the first status that the issue transitions to will be counted as Completion)"
            }
        },
        {
            "fieldName": "jiraIterationCompletionStatusKPI119",
            "fieldLabel": "Status to identify completed issues",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "All statuses that signify completion for a team. (If more than one status configured, then the first status that the issue transitions to will be counted as Completion)"
            }
        },
        {
            "fieldName": "jiraIterationCompletionStatusKPI131",
            "fieldLabel": "Status to identify completed issues",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "All statuses that signify completion for a team. (If more than one status configured, then the first status that the issue transitions to will be counted as Completion)"
            }
        },
        {
            "fieldName": "jiraWaitStatusKPI131",
            "fieldLabel": "Status that signify queue",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "The statuses wherein no activity takes place and signifies that issue is queued and need to move for work to resume on the issue."
            }
        },
        {
            "fieldName": "jiraIterationCompletionStatusKPI138",
            "fieldLabel": "Status to identify completed issues",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "Status to identify as closed"
            }
        },

        {
            "fieldName": "jiraLiveStatus",
            "fieldLabel": "Status to identify Live status",
            "fieldType": "text",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "Provide any status from workflow on which Live is considered."
            }
        }, {
            "fieldName": "jiraLiveStatusKPI152",
            "fieldLabel": "Status to identify Live status",
            "fieldType": "text",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "Provide any status from workflow on which Live is considered."
            }
        }, {
            "fieldName": "jiraLiveStatusKPI151",
            "fieldLabel": "Status to identify Live status",
            "fieldType": "text",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "Provide any status from workflow on which Live is considered."
            }
        },
        {
            "fieldName": "jiraLiveStatusKPI3",
            "fieldLabel": "Live Status - Lead Time",
            "fieldType": "text",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "Provide any status from workflow on which Live is considered."
            }
        },
        {
            "fieldName": "jiraLiveStatusLTK",
            "fieldLabel": "Live Status - Lead Time",
            "fieldType": "text",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "Provide any status from workflow on which Live is considered."
            }
        },
        {
            "fieldName": "jiraLiveStatusNOPK",
            "fieldLabel": "Live Status - Net Open Ticket Count by Priority",
            "fieldType": "text",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "Provide any status from workflow on which Live is considered."
            }
        },
        {
            "fieldName": "jiraLiveStatusNOSK",
            "fieldLabel": "Live Status - Net Open Ticket by Status",
            "fieldType": "text",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "Provide any status from workflow on which Live is considered."
            }
        },
        {
            "fieldName": "jiraLiveStatusNORK",
            "fieldLabel": "Live Status - Net Open Ticket Count By RCA",
            "fieldType": "text",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "Provide any status from workflow on which Live is considered."
            }
        },
        {
            "fieldName": "jiraLiveStatusOTA",
            "fieldLabel": "Live Status - Open Ticket Ageing",
            "fieldType": "text",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "Provide any status from workflow on which Live is considered."
            }
        },
        {
            "fieldName": "jiraLiveStatusKPI127",
            "fieldLabel": "Status to identify live issues",
            "fieldType": "text",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "Status/es that identify that an issue is LIVE in Production."
            }
        },
        {
            "fieldName": "jiradefecttype",
            "fieldLabel": "Status to identify defects",
            "fieldType": "chips",
            "fieldCategory": "Issue_Type",
            "section": "Defects Mapping",
            "processorCommon": true,
            "tooltip": {
                "definition": "All the issue types that signify a defect in Jira/Azure"
            }
        },
        {
            "fieldName": "jiraStoryPointsCustomField",
            "fieldLabel": "Story Points Custom Field",
            "fieldType": "text",
            "fieldCategory": "fields",
            "section": "Custom Fields Mapping",
            "tooltip": {
                "definition": "Field used in Jira for Story points"
            }
        },
        {
            "fieldName": "workingHoursDayCPT",
            "fieldLabel": "Working Hours in a Day",
            "fieldType": "text",
            "fieldCategory": "fields",
            "section": "Custom Fields Mapping",
            "tooltip": {
                "definition": "Working hours in a day"
            }
        },
        {
            "fieldName": "epicCostOfDelay",
            "fieldLabel": "Custom field for COD",
            "fieldType": "text",
            "fieldCategory": "fields",
            "section": "Custom Fields Mapping",
            "tooltip": {
                "definition": "JIRA/AZURE applications let you add custom fields in addition to the built-in fields. Provide value of Cost Of delay field for Epics that need to show on Trend line. <br> Example:customfield_11111 <hr>",

            }
        },
        {
            "fieldName": "epicRiskReduction",
            "fieldLabel": "Custom field for Risk Reduction",
            "fieldType": "text",
            "fieldCategory": "fields",
            "section": "Custom Fields Mapping",
            "tooltip": {
                "definition": "JIRA/AZURE applications let you add custom fields in addition to the built-in fields.Provide value of Risk reduction/ Enablement value for Epic that is required to calculated Cost of delay <br> Example: customfield_11111<hr>",

            }
        },
        {
            "fieldName": "epicUserBusinessValue",
            "fieldLabel": "Custom field for BV",
            "fieldType": "text",
            "fieldCategory": "fields",
            "section": "Custom Fields Mapping",
            "tooltip": {
                "definition": "JIRA/AZURE applications let you add custom fields in addition to the built-in fields.Provide value of User-Business Value for Epic that is required to calculated Cost of delay. <br>Example:customfield_11111<hr>",

            }
        },
        {
            "fieldName": "epicWsjf",
            "fieldLabel": "Custom field for WSJF",
            "fieldType": "text",
            "fieldCategory": "fields",
            "section": "Custom Fields Mapping",
            "tooltip": {
                "definition": "JIRA/AZURE applications let you add custom fields in addition to the built-in fields.Provide value of WSJF value that is required to calculated Cost of delay <br />Example:customfield_11111<hr>",

            }
        },
        {
            "fieldName": "epicTimeCriticality",
            "fieldLabel": "Custom field for Time Criticality",
            "fieldType": "text",
            "fieldCategory": "fields",
            "section": "Custom Fields Mapping",
            "tooltip": {
                "definition": "JIRA/AZURE applications let you add custom fields in addition to the built-in fields.Provide value of Time Criticality value on Epic that is required to calculated Cost of delay .<br />Example:customfield_11111<hr>",
            }
        },
        {
            "fieldName": "epicJobSize",
            "fieldLabel": "Custom field for Job Size",
            "fieldType": "text",
            "fieldCategory": "fields",
            "section": "Custom Fields Mapping",
            "tooltip": {
                "definition": "JIRA/AZURE applications let you add custom fields in addition to the built-in fields.Provide value of Job size on EPIC that is required to calculated WSJF. <br>Example:customfield_11111<hr>",

            }
        },
        {
            "fieldName": "estimationCriteria",
            "fieldLabel": "Estimation Criteria",
            "fieldType": "radiobutton",
            "section": "Custom Fields Mapping",
            "tooltip": {
                "definition": "Estimation criteria for stories. <br> Example: Buffered Estimation."
            },
            "options": [{
                    "label": "Story Point",
                    "value": "Story Point"
                },
                {
                    "label": "Actual (Original Estimation)",
                    "value": "Actual Estimation"
                }
            ],
            "nestedFields": [{
                "fieldName": "storyPointToHourMapping",
                "fieldLabel": "Story Point to Hour Conversion",
                "fieldType": "text",
                "filterGroup": ["Story Point"],
                "tooltip": {
                    "definition": "Estimation technique used by teams for e.g. story points, Hours etc."
                }
            }]
        },
        {
            "fieldName": "jiraIncludeBlockedStatusKPI131",
            "fieldLabel": "Status to identify Blocked issues",
            "fieldType": "radiobutton",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "The statuses that signify that team is unable to proceed on an issue due to internal or external dependency like On Hold, Waiting for user response, dependent work etc."
            },
            "options": [{
                    "label": "Blocked Status",
                    "value": "Blocked Status"
                },
                {
                    "label": "Include Flagged Issue",
                    "value": "Include Flagged Issue"
                }
            ],
            "nestedFields": [{
                "fieldName": "jiraBlockedStatusKPI131",
                "fieldLabel": "Status to Identify 'Blocked' status ",
                "fieldType": "chips",
                "filterGroup": ["Blocked Status"],
                "tooltip": {
                    "definition": "Provide Status to Identify Blocked Issues."
                }
            }]
        },
        {
            "fieldName": "jiraBugRaisedByQAIdentification",
            "fieldLabel": "QA Defect Identification",
            "fieldType": "radiobutton",
            "section": "Defects Mapping",
            "tooltip": {
                "definition": "This field is used to identify if a defect is raised by QA<br>1. CustomField : If a separate custom field is used.<br>2. Labels : If a label is used to identify. Example: QA Defect <hr>"
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
            "nestedFields": [{
                    "fieldName": "jiraBugRaisedByQAValue",
                    "fieldLabel": "QA Defect Values",
                    "fieldType": "chips",
                    "filterGroup": ["CustomField", "Labels"],
                    "tooltip": {
                        "definition": "Provide label name to identify QA raised defects."
                    }
                },
                {
                    "fieldName": "jiraBugRaisedByQACustomField",
                    "fieldLabel": "QA Defect Custom Field",
                    "fieldType": "text",
                    "fieldCategory": "fields",
                    "filterGroup": ["CustomField"],
                    "tooltip": {
                        "definition": "Provide customfield name to identify QA raised defects. <br>Example: customfield_13907"
                    }
                }
            ]
        },
        {
            "fieldName": "jiraBugRaisedByIdentification",
            "fieldLabel": "UAT Defect Identification",
            "fieldType": "radiobutton",
            "section": "Defects Mapping",
            "tooltip": {
                "definition": "This field is used to identify if a defect is raised by third party or client:<br>1. CustomField : If a separate custom field is used<br>2. Labels : If a label is used to identify. Example: TECH_DEBT (This has to be one value).<hr>"
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
                    "fieldName": "jiraBugRaisedByCustomField",
                    "fieldLabel": "UAT Defect Custom Field",
                    "fieldType": "text",
                    "fieldCategory": "fields",
                    "filterGroup": ["CustomField"],
                    "tooltip": {
                        "definition": "Provide customfield name to identify UAT or client raised defects. <br> Example: customfield_13907<hr>"
                    }
                },
                {
                    "fieldName": "jiraBugRaisedByValue",
                    "fieldLabel": "UAT Defect Values",
                    "fieldType": "chips",
                    "filterGroup": ["CustomField", "Labels"],
                    "tooltip": {
                        "definition": "Provide label name to identify UAT or client raised defects.<br /> Example: Clone_by_QA <hr>"
                    }
                }
            ]
        },
        {
            "fieldName": "additionalFilterConfig",
            "fieldLabel": "Filter that can be applied on a Project",
            "section": "Additional Filter Identifier",
            "fieldType": "dropdown",
            "tooltip": {
                "definition": "This field is used to identify Additional Filters. <br> Example: SQUAD<br>",

            }
        },
        {
            "fieldName": "issueStatusExcluMissingWorkKPI124",
            "fieldLabel": "Status to be excluded",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "All statuses of an issue that should be ignored for checking the logged work",
            }
        }, {
            "fieldName": "jiraStatusForInProgressKPI145",
            "fieldLabel": "Status to identify In Progress issues",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "All statuses that issues have moved from the Created status and also has not been completed",
            }
        },
        {
            "fieldName": "jiraStatusForInProgressKPI122",
            "fieldLabel": "Status to identify In Progress issues",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "All statuses that issues have moved from the Created status and also has not been completed",
            }
        }, {
            "fieldName": "jiraStatusForInProgressKPI125",
            "fieldLabel": "Status to identify In Progress issues",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "All statuses that issues have moved from the Created status and also has not been completed",
            }
        }, {
            "fieldName": "jiraStatusForInProgressKPI123",
            "fieldLabel": "Status to identify In Progress issues",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "All statuses that issues have moved from the Created status and also has not been completed",
            }
        }, {
            "fieldName": "jiraStatusForInProgressKPI119",
            "fieldLabel": "Status to identify In Progress issues",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "All statuses that issues have moved from the Created status and also has not been completed",
            }
        }, {
            "fieldName": "jiraStatusForInProgressKPI128",
            "fieldLabel": "Status to identify In Progress issues",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "All statuses that issues have moved from the Created status and also has not been completed",
            }
        }, {
            "fieldName": "jiraStatusForInProgressKPI148",
            "fieldLabel": "Status to identify In Progress issues",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "All statuses that issues have moved from the Created status and also has not been completed",
            }
        }, {
            "fieldName": "jiraDevDoneStatusKPI119",
            "fieldLabel": "Status to identify Dev completed issues",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "Status that confirms that the development work is completed and an issue can be passed on for testing",
            }
        }, {
            "fieldName": "jiraDevDoneStatusKPI145",
            "fieldLabel": "Status to identify Dev completion",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "Status that confirms that the development work is completed and an issue can be passed on for testing",
            }
        }, {
            "fieldName": "jiraDevDoneStatusKPI128",
            "fieldLabel": "Status to identify Dev completed issues",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "Status that confirms that the development work is completed and an issue can be passed on for testing",
            }
        }, {
            "fieldName": "jiraDefectCountlIssueTypeKPI28",
            "fieldLabel": "Issue type to be included",
            "fieldType": "chips",
            "fieldCategory": "Issue_Type",
            "section": "Issue Types Mapping",
            "tooltip": {
                "definition": "Issue types that are considered as defects in Jira.",
            }
        }, {
            "fieldName": "jiraDefectCountlIssueTypeKPI36",
            "fieldLabel": "Issue type to be included",
            "fieldType": "chips",
            "fieldCategory": "Issue_Type",
            "section": "Issue Types Mapping",
            "tooltip": {
                "definition": "Issue types that are considered as defects in Jira.",
            }
        }, {
            "fieldName": "jiraStatusForQaKPI135",
            "fieldLabel": "Status to Identify In Testing Status",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "The status of Defect Issue Type which identifies the 'In-Testing' status in JIRA. <br> Example: Ready For Testing<hr>",

            }
        }, {
            "fieldName": "jiraStatusForQaKPI82",
            "fieldLabel": "Status to Identify In Testing Status",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "The status of Defect Issue Type which identifies the 'In-Testing' status in JIRA. <br> Example: Ready For Testing<hr>",

            }
        }, {
            "fieldName": "jiraStatusForQaKPI148",
            "fieldLabel": "Status to Identify In Testing Status",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "The status of Defect Issue Type which identifies the 'In-Testing' status in JIRA. <br> Example: Ready For Testing<hr>",

            }
        },
        {
            "fieldName": "storyFirstStatusKPI3",
            "fieldLabel": "Status when 'Story' issue type is created",
            "fieldType": "text",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "All issue types that identify with a Story.",

            }
        },
        {
            "fieldName": "storyFirstStatusKPI148",
            "fieldLabel": "Status when 'Story' issue type is created",
            "fieldType": "text",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "All issue types that identify with a Story.",

            }
        },
        {
            "fieldName": "jiraFtprRejectStatusKPI135",
            "fieldLabel": "FTPR Rejection Status ",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "This status depicts the stories which have not passed QA. FTP stories can also be identified by a return transition but if status is mentioned that will be considered."
            }
        },
        {
            "fieldName": "jiraFtprRejectStatusKPI82",
            "fieldLabel": "FTPR Rejection Status ",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "This status depicts the stories which have not passed QA. FTP stories can also be identified by a return transition but if status is mentioned that will be considered."
            }
        },
        {
            "fieldName": "jiraDefectClosedStatusKPI137",
            "fieldLabel": "Status to identify Closed Bugs",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "This field should consider all status that are considered Closed in Jira for e.g. Closed, Done etc."
            }
        },
        {
            "fieldName": "jiraAcceptedInRefinementKPI139",
            "fieldLabel": "Accepted in Refinement",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": " Status that Defines Jira Issue is that is Accepted in Refinement."
            }
        },
        {
            "fieldName": "jiraReadyForRefinementKPI139",
            "fieldLabel": "Ready For Refinement",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "Status that Defines Jira Issue is Ready for Refinement."
            }
        },
        {
            "fieldName": "jiraRejectedInRefinementKPI139",
            "fieldLabel": "Rejected in Refinement",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "Status that Defines Jira Issue is Rejected In Refinement."
            }
        },
        {
            "fieldName": "readyForDevelopmentStatusKPI138",
            "fieldLabel": "Status to identify issues Ready for Development ",
            "fieldType": "text",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "Status to identify Ready for development from the backlog.",
            }
        },
        {
            "fieldName": "jiraDueDateField",
            "fieldLabel": "Due Date",
            "fieldType": "radiobutton",
            "section": "Custom Fields Mapping",
            "tooltip": {
                "definition": "This field is to track due date of issues tagged in the iteration"
            },
            "options": [{
                    "label": "Custom Field",
                    "value": "CustomField"
                },
                {
                    "label": "Due Date",
                    "value": "Due Date"
                }
            ],
            "nestedFields": [

                {
                    "fieldName": "jiraDueDateCustomField",
                    "fieldLabel": "Due Date Custom Field",
                    "fieldType": "text",
                    "fieldCategory": "fields",
                    "filterGroup": ["CustomField"],
                    "tooltip": {
                        "definition": "This field is to track due date of issues tagged in the iteration."
                    }
                }
            ]
        }, {
            "fieldName": "jiraDevDueDateCustomField",
            "fieldLabel": "Dev Due Date",
            "fieldType": "text",
            "fieldCategory": "fields",
            "section": "Custom Fields Mapping",
            "tooltip": {
                "definition": "This field is to track dev due date of issues tagged in the iteration."
            }
        }, {
            "fieldName": "jiraIssueEpicType",
            "fieldLabel": "Epic Issue Type",
            "fieldType": "chips",
            "fieldCategory": "Issue_Type",
            "section": "Issue Types Mapping",
            "tooltip": {
                "definition": "This field is used to identify Epic Issue type.",
            }
        }, {
            "fieldName": "rootCause",
            "fieldLabel": "Root Cause",
            "fieldType": "text",
            "fieldCategory": "fields",
            "section": "Custom Fields Mapping",
            "tooltip": {
                "definition": "JIRA/AZURE applications let you add custom fields in addition to the built-in fields. Root Cause is a custom field in JIRA. So User need to provide that custom field which is associated with Root Cause in Users JIRA Installation.",
            }
        }, {
            "fieldName": "storyFirstStatus",
            "fieldLabel": "Story First Status",
            "fieldType": "text",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "Default status when a Story is opened.",
            }
        }, {
            "fieldName": "jiraTestAutomationIssueType",
            "fieldLabel": "In Sprint Automation - Issue Types with Linked Defect ",
            "fieldType": "chips",
            "fieldCategory": "Issue_Type",
            "section": "Issue Types Mapping",
            "tooltip": {
                "definition": "",
            }
        }, {
            "fieldName": "jiraStoryIdentification",
            "fieldLabel": "In Sprint Automation - Issue Types with Linked Defect ",
            "fieldType": "chips",
            "fieldCategory": "Issue_Type",
            "section": "Issue Types Mapping",
            "tooltip": {
                "definition": "Value to identify kind of stories which are used for identification for story count.",
            }
        }, {
            "fieldName": "jiraDefectDroppedStatus",
            "fieldLabel": "Defect Dropped Status",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "All issue types with which defect is linked.",
            }
        }, {
            "fieldName": "jiraDefectDroppedStatusKPI127",
            "fieldLabel": "Defect Dropped Status",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "All issue types with which defect is linked.",
            }
        }, {
            "fieldName": "productionDefectIdentifier",
            "fieldLabel": "Production defects identification",
            "fieldType": "radiobutton",
            "section": "Defects Mapping",
            "tooltip": {
                "definition": "This field is used to identify if a defect is raised by Production. 1. CustomField : If a separate custom field is used, 2. Labels : If a label is used to identify, 3. Component : If a Component is used to identify"
            },
            "options": [{
                    "label": "CustomField",
                    "value": "CustomField"
                },
                {
                    "label": "Labels",
                    "value": "Labels"
                },
                {
                    "label": "Component",
                    "value": "Component"
                }
            ],
            "nestedFields": [

                {
                    "fieldName": "productionDefectCustomField",
                    "fieldLabel": "Production Defect CustomField",
                    "fieldType": "text",
                    "fieldCategory": "fields",
                    "filterGroup": ["CustomField"],
                    "tooltip": {
                        "definition": " Provide customfield name to identify Production raised defects."
                    }
                },
                {
                    "fieldName": "productionDefectValue",
                    "fieldLabel": "Production Defect Values",
                    "fieldType": "chips",
                    "filterGroup": ["CustomField", "Labels"],
                    "tooltip": {
                        "definition": "Provide label name to identify Production raised defects."
                    }
                },
                {
                    "fieldName": "productionDefectComponentValue",
                    "fieldLabel": "Component",
                    "fieldType": "text",
                    "filterGroup": ["Component"],
                    "tooltip": {
                        "definition": "Provide label name to identify Production raised defects."
                    }
                }
            ]
        }, {
            "fieldName": "ticketCountIssueType",
            "fieldLabel": "Ticket Count Issue Type",
            "fieldType": "chips",
            "fieldCategory": "Issue_Type",
            "section": "Issue Types Mapping",
            "tooltip": {
                "definition": "",

            }
        }, {
            "fieldName": "kanbanRCACountIssueType",
            "fieldLabel": "Ticket RCA Count Issue Type",
            "fieldType": "chips",
            "fieldCategory": "Issue_Type",
            "section": "Issue Types Mapping",
            "tooltip": {
                "definition": "",


            }
        }, {
            "fieldName": "jiraTicketVelocityIssueType",
            "fieldLabel": "Ticket Velocity Issue Type",
            "fieldType": "chips",
            "fieldCategory": "Issue_Type",
            "section": "Issue Types Mapping",
            "tooltip": {
                "definition": "",


            }
        }, {
            "fieldName": "kanbanCycleTimeIssueType",
            "fieldLabel": "Kanban Lead Time Issue Type",
            "fieldType": "chips",
            "fieldCategory": "Issue_Type",
            "section": "Issue Types Mapping",
            "tooltip": {
                "definition": "",


            }
        }, {
            "fieldName": "ticketDeliverdStatus",
            "fieldLabel": "Ticket Delivered Status",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "Status from workflow on which ticket is considered as delivered."
            }
        }, {
            "fieldName": "jiraTicketClosedStatus",
            "fieldLabel": "Ticket Closed Status",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "Status from workflow on which ticket is considered as Resolved."
            }
        }, {
            "fieldName": "jiraTicketTriagedStatus",
            "fieldLabel": "Ticket Triaged Status",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "Status from workflow on which ticket is considered as Triaged."
            }
        }, {
            "fieldName": "jiraTicketRejectedStatus",
            "fieldLabel": "Ticket Rejected/Dropped Status",
            "fieldType": "chips",
            "fieldCategory": "workflow",
            "section": "WorkFlow Status Mapping",
            "tooltip": {
                "definition": "Status from workflow on which ticket is considered as Rejected/Dropped."
            }
        }
    ]
);
    print("Field Mapping Structure executed successfully!");
  } else {
    print("Field Mapping Structure already executed. Skipping...");
  }

//DTS-25767 Commitment Reliability - Add Filter by Issue type (add one column for issue type in excel)
 db.kpi_column_configs.updateOne(
   { "kpiId": "kpi72" },
   {
     $set: {
       "kpiColumnDetails": [
         {
           "columnName": "Sprint Name",
           "order": 0,
           "isShown": true,
           "isDefault": false
         },
         {
           "columnName": "Story ID",
           "order": 1,
           "isShown": true,
           "isDefault": false
         },
         {
           "columnName": "Issue Status",
           "order": 2,
           "isShown": true,
           "isDefault": false
         },
         {
           "columnName": "Issue Type",
           "order": 3,
           "isShown": true,
           "isDefault": true
         },
         {
           "columnName": "Initial Commitment",
           "order": 4,
           "isShown": true,
           "isDefault": true
         },
         {
           "columnName": "Size(story point/hours)",
           "order": 5,
           "isShown": true,
           "isDefault": true
         }
       ]
     }
   }
 );

 //---------7.5.0 changes------------------------------------------------------------------
//Defect fix for DTS-27477 (Remove one In-Sprint Automation mapping which is appearing twice)

var fieldNameToUpdate = "jiraStoryIdentification";
  db.getCollection('field_mapping_structure').update(
    { "fieldName": fieldNameToUpdate },
    { $set: { "fieldLabel": "Issue Count KPI Issue type" } },
    { multi: false }
  );


var fieldNameToUpdate = "jiraIssueTypeKPI3";
  db.getCollection('field_mapping_structure').update(
    { "fieldName": fieldNameToUpdate },
    { $set: {
    "fieldLabel": "Issue type to be included",
    "tooltip.definition": "All issue types that should be included in Lead time calculation"
    } },
    { multi: false }
  );

var fieldNameToUpdate = "jiraDorKPI3";
  db.getCollection('field_mapping_structure').update(
    { "fieldName": fieldNameToUpdate },
    { $set: {
    "fieldLabel": "DOR status",
    "tooltip.definition": "Status/es that identify that an issue is ready to be taken in the sprint"
     } },
    { multi: false }
  );

var fieldNameToUpdate = "jiraLiveStatusKPI3";
  db.getCollection('field_mapping_structure').update(
    { "fieldName": fieldNameToUpdate },
    { $set: {
    "tooltip.definition": "Status/es that identify that an issue is LIVE in Production."
    } },
    { multi: false }
  );

// Adding action_policy "Fetch Sprint"
db.action_policy_rule.insertOne({
    "name": "Fetch Sprint",
    "roleAllowed": "",
    "description": "super admin and project admin can run active sprint fetch",
    "roleActionCheck": "action == 'TRIGGER_SPRINT_FETCH'",
    "condition": "subject.authorities.contains('ROLE_SUPERADMIN') || subject.authorities.contains('ROLE_PROJECT_ADMIN')",
    "createdDate": new Date(),
    "lastModifiedDate": new Date(),
    "isDeleted": false
})

db.getCollection('field_mapping_structure').insertMany([
    {
        "fieldName": "jiraDodKPI37",
        "fieldLabel": "Status to identify completed issues",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "Status/es that identify that an issue is completed based on Definition of Done (DoD)"
        }
    },
    {
        "fieldName": "sprintName",
        "fieldLabel": "Sprint Name",
        "fieldType": "text",
        "fieldCategory": "fields",
        "section": "Custom Fields Mapping",
        "tooltip": {
            "definition": "JIRA applications let you add custom fields in addition to the built-in fields. Sprint name is a custom field in JIRA. So User need to provide that custom field which is associated with Sprint in Users JIRA Installation."
        }
    }
])

const fieldMapToUpdate = db.field_mapping.find({ "jiraIssueTypeKPI37": { $exists: true } });
fieldMapToUpdate.forEach(function(fm) {
    const jiraDod = fm.jiraDod;

    db.field_mapping.updateOne(
        { "_id": fm._id },
        {
            $set: {
                "jiraDodKPI37": jiraDod
            },
            $unset: {
                "jiraIssueTypeKPI37": ""
            }
        }
    );
});

// changing DRR formula
db.kpi_master.updateOne(
  {
    "kpiId": "kpi37",
    "kpiInfo.formula.operands": "Total no. of defects reported in a sprint"
  },
  {
    $set: {
      "kpiInfo.formula.$[formulaElem].operands.$[operandElem]": "Total no. of defects Closed in a sprint"
    }
  },
  {
    arrayFilters: [
      { "formulaElem.operands": { $exists: true } },
      { "operandElem": "Total no. of defects reported in a sprint" }
    ]
  }
);

//----------------7.6.0 Changes ---------------------------
//updating epicLink from documents of metadata_identifier
db.getCollection('metadata_identifier').updateMany(
   { "templateCode": { $in: ["7", "8"] } },
   { $push: {
      "customfield": {
         "type": "epicLink",
         "value": ["Epic Link"]
      }
   }}
);

//updating metadata_identifier
db.getCollection('metadata_identifier').update(
     { "templateCode": "7" }, // Match documents with templateCode equal to "7"
     {
     $set:
     {
              "tool": "Jira",
              "templateName": "Standard Template",
              "templateCode": "7",
              "isKanban": false,
              "disabled": false,
              "issues": [
                  {
                      "type": "jiradefecttype",
                      "value": [
                          "Defect",
                          "Bug"
                      ]
                  },
                  {
                      "type": "jiraIssueTypeNames",
                      "value": [
                          "Story",
                          "Enabler Story",
                          "Tech Story",
                          "Change request",
                          "Defect",
                          "Bug",
                          "Epic"
                      ]
                  },
                  {
                      "type": "jiraIssueEpicType",
                      "value": [
                          "Epic"
                      ]
                  },
                  {
                      "type": "jiraDefectInjectionIssueTypeKPI14",
                      "value": [
                          "Story",
                          "Enabler Story",
                          "Tech Story",
                          "Change request"
                      ]
                  },
                  {
                      "type": "jiraIssueTypeKPI35",
                      "value": [
                          "Story",
                          "Enabler Story",
                          "Tech Story",
                          "Change request"
                      ]
                  },
                  {
                      "type": "jiraTestAutomationIssueType",
                      "value": [
                          "Story",
                          "Enabler Story",
                          "Tech Story",
                          "Change request"
                      ]
                  },
                  {
                      "type": "jiraSprintVelocityIssueTypeKPI138",
                      "value": [
                          "Story",
                          "Enabler Story",
                          "Tech Story",
                          "Change request"
                      ]
                  },
                  {
                      "type": "jiraSprintCapacityIssueTypeKpi46",
                      "value": [
                          "Story",
                          "Enabler Story",
                          "Tech Story",
                          "Change request"
                      ]
                  },
                  {
                      "type": "jiraDefectCountlIssueTypeKPI28",
                      "value": [
                          "Story",
                          "Enabler Story",
                          "Tech Story",
                          "Change request"

                      ]
                  },
                  {
                      "type": "jiraDefectCountlIssueTypeKPI36",
                      "value": [
                          "Story",
                          "Enabler Story",
                          "Tech Story",
                          "Change request"

                      ]
                  },
                  {
                      "type": "jiraIssueTypeKPI3",
                      "value": [
                          "Story",
                          "Enabler Story",
                          "Tech Story",
                          "Change request"

                      ]
                  },
                  {
                      "type": "jiraQAKPI111IssueType",
                      "value": [
                          "Story",
                          "Enabler Story",
                          "Tech Story",
                          "Change request"
                      ]
                  },
                  {
                      "type": "jiraStoryIdentificationKPI129",
                      "value": [
                          "Story",
                          "Enabler Story",
                          "Tech Story",
                          "Change request"
                      ]
                  },
                  {
                      "type": "jiraStoryIdentificationKpi40",
                      "value": [
                          "Story",
                          "Enabler Story",
                          "Tech Story",
                          "Change request"
                      ]
                  },
                  {
                      "type": "jiraTechDebtIssueType",
                      "value": [
                          "Story",
                          "Enabler Story",
                          "Tech Story",
                          "Change request"
                      ]
                  },
                  {
                      "type": "jiraKPI82StoryIdentification",
                      "value": [
                          "Story",
                          "Enabler Story",
                          "Tech Story",
                          "Change request"
                      ]
                  },
                  {
                      "type": "jiraKPI135StoryIdentification",
                      "value": [
                          "Story",
                          "Enabler Story",
                          "Tech Story",
                          "Change request"
                      ]
                  }
              ],
              "customfield": [
                  {
                      "type": "jiraStoryPointsCustomField",
                      "value": [
                          "Story Points"
                      ]
                  },
                  {
                      "type": "epicCostOfDelay",
                      "value": [
                          "Story Points"
                      ]
                  },
                  {
                      "type": "epicRiskReduction",
                      "value": [
                          "Risk Reduction-Opportunity Enablement Value"
                      ]
                  },
                  {
                      "type": "epicUserBusinessValue",
                      "value": [
                          "User-Business Value"
                      ]
                  },
                  {
                      "type": "epicWsjf",
                      "value": [
                          "WSJF"
                      ]
                  },
                  {
                      "type": "epicTimeCriticality",
                      "value": [
                          "Time Criticality"
                      ]
                  },
                  {
                      "type": "epicJobSize",
                      "value": [
                          "Job Size"
                      ]
                  },
                 {
                     "type": "epicLink",
                     "value": [
                         "Epic Link"
                     ]
                 },
                  {
                      "type": "rootcause",
                      "value": [
                          "Root Cause"
                      ]
                  },
                  {
                      "type": "sprintName",
                      "value": [
                          "Sprint"
                      ]
                  }
              ],
              "workflow": [
                  {
                      "type": "storyFirstStatusKPI148",
                      "value": [
                          "Open"
                      ]
                  },
                  {
                      "type": "storyFirstStatusKPI3",
                      "value": [
                          "Open"
                      ]
                  },
                  {
                      "type": "jiraStatusForQaKPI148",
                      "value": [
                          "In Testing"
                      ]
                  },
                  {
                      "type": "jiraStatusForQaKPI135",
                      "value": [
                          "In Testing"
                      ]
                  },
                  {
                      "type": "jiraStatusForQaKPI82",
                      "value": [
                          "In Testing"
                      ]
                  },
                  {
                      "type": "jiraStatusForDevelopmentKPI82",
                      "value": [
                          "Implementing",
                          "In Development",
                          "In Analysis"
                      ]
                  },
                  {
                      "type": "jiraStatusForDevelopmentKPI135",
                      "value": [
                          "Implementing",
                          "In Development",
                          "In Analysis"
                      ]
                  },
                  {
                      "type": "jiraDefectCreatedStatusKPI14",
                      "value": [
                          "Open"
                      ]
                  },
                  {
                      "type": "jiraDefectRejectionStatusKPI152",
                      "value": [
                          "Rejected"
                      ]
                  },
                  {
                      "type": "jiraDefectRejectionStatusKPI151",
                      "value": [
                          "Rejected"
                      ]
                  },
                  {
                      "type": "jiraDefectRejectionStatusKPI28",
                      "value": [
                          "Rejected"
                      ]
                  },
                  {
                      "type": "jiraDefectRejectionStatusKPI37",
                      "value": [
                          "Rejected"
                      ]
                  },
                  {
                      "type": "jiraDefectRejectionStatusKPI35",
                      "value": [
                          "Rejected"
                      ]
                  },
                  {
                      "type": "jiraDefectRejectionStatusKPI82",
                      "value": [
                          "Rejected"
                      ]
                  },
                  {
                      "type": "jiraDefectRejectionStatusKPI135",
                      "value": [
                          "Rejected"
                      ]
                  },
                  {
                      "type": "jiraDefectRejectionStatusKPI133",
                      "value": [
                          "Rejected"
                      ]
                  },
                  {
                      "type": "jiraDefectRejectionStatusRCAKPI36",
                      "value": [
                          "Rejected"
                      ]
                  },
                  {
                      "type": "jiraDefectRejectionStatusKPI14",
                      "value": [
                          "Rejected"
                      ]
                  },
                  {
                      "type": "jiraDefectRejectionStatusQAKPI111",
                      "value": [
                          "Rejected"
                      ]
                  },
                  {
                      "type": "jiraDefectRemovalStatusKPI34",
                      "value": [
                          "Closed",
                          "Resolved",
                          "Ready for Delivery",
                          "Ready for Release",
                          "Done"
                      ]
                  },
                  {
                      "type": "jiraDefectClosedStatusKPI137",
                      "value": [
                          "Closed",
                          "CLOSED"
                      ]
                  },
                  {
                      "type": "jiraIssueDeliverdStatusKPI138",
                      "value": [
                          "Closed",
                          "Resolved",
                          "Ready for Delivery",
                          "Ready for Release",
                          "Done"
                      ]
                  },
                  {
                      "type": "jiraIssueDeliverdStatusKPI126",
                      "value": [
                          "Closed",
                          "Resolved",
                          "Ready for Delivery",
                          "Ready for Release",
                          "Done"
                      ]
                  },
                  {
                      "type": "jiraIssueDeliverdStatusKPI82",
                      "value": [
                          "Closed",
                          "Resolved",
                          "Ready for Delivery",
                          "Ready for Release",
                          "Done"
                      ]
                  },
                  {
                      "type": "jiraDorKPI3",
                      "value": [
                          "Ready for Sprint Planning"

                      ]
                  },
                  {
                      "type": "jiraLiveStatusKPI3",
                      "value": [
                          "Live"
                      ]
                  },
                  {
                      "type": "jiraLiveStatusKPI127",
                      "value": [
                          "Live"
                      ]
                  },
                  {
                      "type": "jiraLiveStatusKPI152",
                      "value": [
                          "Live"
                      ]
                  },
                  {
                      "type": "jiraLiveStatusKPI151",
                      "value": [
                          "Live"
                      ]
                  },
                  {
                      "type": "resolutionTypeForRejectionKPI28",
                      "value": [
                          "Invalid",
                          "Duplicate",
                          "Unrequired",
                          "Cannot Reproduce",
                          "Won't Fix"
                      ]
                  },
                  {
                      "type": "resolutionTypeForRejectionKPI37",
                      "value": [
                          "Invalid",
                          "Duplicate",
                          "Unrequired",
                          "Cannot Reproduce",
                          "Won't Fix"
                      ]
                  },
                  {
                      "type": "resolutionTypeForRejectionKPI35",
                      "value": [
                          "Invalid",
                          "Duplicate",
                          "Unrequired",
                          "Cannot Reproduce",
                          "Won't Fix"
                      ]
                  },
                  {
                      "type": "resolutionTypeForRejectionKPI135",
                      "value": [
                          "Invalid",
                          "Duplicate",
                          "Unrequired",
                          "Cannot Reproduce",
                          "Won't Fix"
                      ]
                  },
                  {
                      "type": "resolutionTypeForRejectionKPI82",
                      "value": [
                          "Invalid",
                          "Duplicate",
                          "Unrequired",
                          "Cannot Reproduce",
                          "Won't Fix"
                      ]
                  },
                  {
                      "type": "resolutionTypeForRejectionKPI133",
                      "value": [
                          "Invalid",
                          "Duplicate",
                          "Unrequired",
                          "Cannot Reproduce",
                          "Won't Fix"
                      ]
                  },
                  {
                      "type": "resolutionTypeForRejectionRCAKPI36",
                      "value": [
                          "Invalid",
                          "Duplicate",
                          "Unrequired",
                          "Cannot Reproduce",
                          "Won't Fix"
                      ]
                  },
                  {
                      "type": "resolutionTypeForRejectionQAKPI111",
                      "value": [
                          "Invalid",
                          "Duplicate",
                          "Unrequired",
                          "Cannot Reproduce",
                          "Won't Fix"
                      ]
                  },
                  {
                      "type": "resolutionTypeForRejectionKPI14",
                      "value": [
                          "Invalid",
                          "Duplicate",
                          "Unrequired",
                          "Cannot Reproduce",
                          "Won't Fix"
                      ]
                  },
                  {
                      "type": "issueStatusExcluMissingWorkKPI124",
                      "value": [
                          "Open"
                      ]
                  },
                  {
                      "type": "jiraDefectDroppedStatusKPI127",
                      "value": [
                          "Dropped",
                          "Canceled"
                      ]
                  },
                  {
                      "type": "jiraBlockedStatusKPI131",
                      "value": [
                          "On Hold",
                          "Blocked"
                      ]
                  },
                  {
                      "type": "jiraWaitStatusKPI131",
                      "value": [
                          "Ready for Testing",
                          "Code Review"
                      ]
                  },
                  {
                      "type": "jiraStatusForInProgressKPI148",
                      "value": [
                          "In Analysis",
                          "In Development",
                          "In Progress"
                      ]
                  },
                  {
                      "type": "jiraStatusForInProgressKPI122",
                      "value": [
                          "In Analysis",
                          "In Development",
                          "In Progress"
                      ]
                  },
                  {
                      "type": "jiraStatusForInProgressKPI145",
                      "value": [
                          "In Analysis",
                          "In Development",
                          "In Progress"
                      ]
                  },
                  {
                      "type": "jiraStatusForInProgressKPI125",
                      "value": [
                          "In Analysis",
                          "In Development",
                          "In Progress"
                      ]
                  },
                  {
                      "type": "jiraStatusForInProgressKPI128",
                      "value": [
                          "In Analysis",
                          "In Development",
                          "In Progress"
                      ]
                  },
                  {
                      "type": "jiraStatusForInProgressKPI123",
                      "value": [
                          "In Analysis",
                          "In Development",
                          "In Progress"
                      ]
                  },
                  {
                      "type": "jiraStatusForInProgressKPI119",
                      "value": [
                          "In Analysis",
                          "In Development",
                          "In Progress"
                      ]
                  },
                  {
                      "type": "jiraDodKPI14",
                      "value": [
                          "Closed",
                          "Resolved",
                          "Ready for Delivery",
                          "Done",
                          "Ready for Sign-off"
                      ]
                  },
                  {
                      "type": "jiraDodQAKPI111",
                      "value": [
                          "Closed",
                          "Resolved",
                          "Ready for Delivery",
                          "Done",
                          "Ready for Sign-off"
                      ]
                  },
                  {
                      "type": "jiraDodKPI3",
                      "value": [
                          "Closed",
                          "Resolved",
                          "Ready for Delivery",
                          "Done",
                          "Ready for Sign-off"
                      ]
                  },
                  {
                      "type": "jiraDodKPI127",
                      "value": [
                          "Closed",
                          "Resolved",
                          "Ready for Delivery",
                          "Done",
                          "Ready for Sign-off"
                      ]
                  },
                  {
                      "type": "jiraDodKPI152",
                      "value": [
                          "Closed",
                          "Resolved",
                          "Ready for Delivery",
                          "Done",
                          "Ready for Sign-off"
                      ]
                  },
                  {
                      "type": "jiraDodKPI151",
                      "value": [
                          "Closed",
                          "Resolved",
                          "Ready for Delivery",
                          "Done",
                          "Ready for Sign-off"
                      ]
                  },
                  {
                      "type": "jiraDodKPI37",
                      "value": [
                          "Closed",
                          "Resolved",
                          "Ready for Delivery",
                          "Done",
                          "Ready for Sign-off"
                      ]
                  }
              ]
     },
     $unset: {
         "valuestoidentify":1
     }
     },
     { multi: false }
);

//DTS-26121 Enchancement of Quality Status Overlay
db.kpi_column_configs.updateMany({"kpiId" : "kpi133"},
{$set:{"kpiColumnDetails" : [
		{
			"columnName" : "Issue Id",
			"order" : Double("0"),
			"isShown" : true,
			"isDefault" : true
		},
		{
			"columnName" : "Issue Type",
			"order" : Double("1"),
			"isShown" : true,
			"isDefault" : true
		},
		{
			"columnName" : "Issue Description",
			"order" : Double("2"),
			"isShown" : true,
			"isDefault" : true
		},
		{
			"columnName" : "Issue Status",
			"order" : Double("3"),
			"isShown" : true,
			"isDefault" : true
		},
		{
			"columnName" : "Priority",
			"order" : Double("4"),
			"isShown" : true,
			"isDefault" : true
		},
		{
			"columnName" : "Linked Defect",
			"order" : Double("5"),
			"isShown" : true,
			"isDefault" : false
		},
		{
			"columnName" : "Size(story point/hours)",
			"order" : Double("6"),
			"isShown" : true,
			"isDefault" : false
		},
		{
			"columnName" : "DIR",
			"order" : Double("7"),
			"isShown" : true,
			"isDefault" : false
		},
		{
			"columnName" : "Defect Density",
			"order" : Double("8"),
			"isShown" : true,
			"isDefault" : false
		},
		{
			"columnName" : "Assignee",
			"order" : Double("9"),
			"isShown" : true,
			"isDefault" : false
		}
	]}});

//---- KPI info update for KPI 137 (Defect Reopen Rate)
db.getCollection('kpi_master').updateOne(
  { "kpiId": "kpi137" },
  { $set: { "kpiInfo.definition": "It shows number of defects reopened in a given span of time in comparison to the total closed defects. For all the reopened defects, the average time to reopen is also available." } }
);

//updated action_policy "Fetch Sprint"
db.action_policy_rule.updateOne({
    "name": "Fetch Sprint"
}, {
    $set: {
        "name": "Fetch Sprint",
        "roleAllowed": "",
        "description": "Any user can run active sprint fetch except guest user",
        "roleActionCheck": "!subject.authorities.contains('ROLE_GUEST') && action == 'TRIGGER_SPRINT_FETCH'",
        "condition": "true",
        "createdDate": new Date(),
        "lastModifiedDate": new Date(),
        "isDeleted": false
    }
});

//DTS-27561-Mapping name to be corrected 'Priority to be Excluded'
var fieldNameToUpdate = "jiradefecttype";
  db.getCollection('field_mapping_structure').update(
    { "fieldName": fieldNameToUpdate },
    { $set: {
    "fieldLabel": "Issue Type to identify defects"
    } },
    { multi: false }
  );

  var fieldNameToUpdate = "defectPriorityKPI14";
  db.getCollection('field_mapping_structure').update(
    { "fieldName": fieldNameToUpdate },
    { $set: {
    "fieldLabel": "Priority to be excluded",
    "tooltip.definition": "Priority values of defects which are to be excluded in 'Defect Injection rate' calculation"
    } },
    { multi: false }
  );

  var fieldNameToUpdate = "defectPriorityQAKPI111";
  db.getCollection('field_mapping_structure').update(
    { "fieldName": fieldNameToUpdate },
    { $set: {
    "fieldLabel": "Priority to be excluded",
    "tooltip.definition": "Priority values of defects which are to be excluded in 'Defect Density' calculation"
    } },
    { multi: false }
  );

  var fieldNameToUpdate = "defectPriorityKPI82";
  db.getCollection('field_mapping_structure').update(
    { "fieldName": fieldNameToUpdate },
    { $set: {
    "fieldLabel": "Priority to be excluded",
    "tooltip.definition": "Priority values of defects which are to be excluded in 'FTPR' calculation"
    } },
    { multi: false }
  );

  var fieldNameToUpdate = "defectPriorityKPI133";
  db.getCollection('field_mapping_structure').update(
    { "fieldName": fieldNameToUpdate },
    { $set: {
    "fieldLabel": "Priority to be excluded",
    "tooltip.definition": "Priority values of defects which are to be excluded in 'Quality Status' calculation"
    } },
    { multi: false }
  );

  var fieldNameToUpdate = "defectPriorityKPI135";
  db.getCollection('field_mapping_structure').update(
    { "fieldName": fieldNameToUpdate },
    { $set: {
    "fieldLabel": "Priority to be excluded",
    "tooltip.definition": "Priority values of defects which are to be excluded in 'FTPR' calculation"
    } },
    { multi: false }
  );

  var fieldNameToUpdate = "jiraDefectDroppedStatusKPI127";
  db.getCollection('field_mapping_structure').update(
    { "fieldName": fieldNameToUpdate },
    { $set: {
    "tooltip.definition": "All statuses with which defect is linked."
    } },
    { multi: false }
  );

  var fieldNameToUpdate = "jiraDodKPI152";
  db.getCollection('field_mapping_structure').update(
    { "fieldName": fieldNameToUpdate },
    { $set: {
    "tooltip.definition": "Status/es that identify that an issue is completed based on Definition of Done (DoD)"
    } },
    { multi: false }
  );

  var fieldNameToUpdate = "jiraDodKPI151";
  db.getCollection('field_mapping_structure').update(
    { "fieldName": fieldNameToUpdate },
    { $set: {
    "tooltip.definition": "Status/es that identify that an issue is completed based on Definition of Done (DoD)"
    } },
    { multi: false }
  );

  var fieldNameToUpdate = "jiraDefectCountlIssueTypeKPI28";
    db.getCollection('field_mapping_structure').update(
      { "fieldName": fieldNameToUpdate },
      { $set: {
      "fieldLabel": "Issue types which will have linked defects"
      } },
      { multi: false }
    );

  var fieldNameToUpdate = "jiraDefectCountlIssueTypeKPI36";
    db.getCollection('field_mapping_structure').update(
      { "fieldName": fieldNameToUpdate },
      { $set: {
      "fieldLabel": "Issue types which will have linked defects"
      } },
      { multi: false }
    );

//dts-27545_Unrequired fields should be removed from DRE KPI field mapping
db.field_mapping_structure.deleteMany({
    $or: [
        { "fieldName": "jiraDefectRemovalIssueTypeKPI34" },
        { "fieldName": "jiraDefectRejectionStatusKPI34" },
        { "fieldName": "resolutionTypeForRejectionKPI34" },
        { "fieldName": "jiraDefectDroppedStatus" },
        { "fieldName": "jiraStoryIdentification" },
        { "fieldName": "jiraDod" }
    ]
});

const fieldMappings = db.field_mapping.find({});
fieldMappings.forEach(function(fm) {
db.field_mapping.updateOne({
            "_id": fm._id
        },
        {
             $unset: {
                "jiraDefectRejectionStatusKPI34": "",
                "jiraDefectRemovalIssueTypeKPI34": "",
                "resolutionTypeForRejectionKPI34": "",
                "jiraIterationCompletionStatusKPI134": "",
                "jiraIterationIssuetypeKPI134": ""
             }
        }
        );
});


// add kpi issue type mapping for sprint velocity
db.getCollection('field_mapping_structure').insertMany([
{
        "fieldName": "jiraIterationIssuetypeKPI39",
        "fieldLabel": "Issue type to be included",
        "fieldType": "chips",
        "fieldCategory": "Issue_Type",
        "section": "Issue Types Mapping",
        "tooltip": {
            "definition": "All issues types added will only be included in showing closures (Note: If nothing is added then all issue types by default will be considered)"
        }
}
]);

// --- Backlog Readiness KPI Fieldmapping Enhancement (DTS-27535)

var fieldNameToUpdate = "readyForDevelopmentStatusKPI138";
  db.getCollection('field_mapping_structure').update(
    { "fieldName": fieldNameToUpdate },
    { $set: {
    "fieldType": "chips"
    } },
    { multi: false }
  );

// Update the String field by converting it into a list
db.field_mapping.find({ readyForDevelopmentStatusKPI138: { $type: 2 } }).forEach(function(doc) {
    db.field_mapping.updateMany(
        { _id: doc._id },
        {
            $set: {
                readyForDevelopmentStatusKPI138: [doc.readyForDevelopmentStatusKPI138]
            }
        }
    );
});

//------------------------- 7.7.0 changes----------------------------------------------------------------------------------
// kpi issue type mapping for Quality status  ---------------------------------------------------------------------------
// add Enable Notification option
// PI predictability field mapping structure

db.getCollection('field_mapping_structure').insertMany([
    {
            "fieldName": "jiraItrQSIssueTypeKPI133",
            "fieldLabel": "Issue types which will have linked defects",
            "fieldType": "chips",
            "fieldCategory": "Issue_Type",
            "section": "Issue Types Mapping",
            "tooltip": {
                "definition": "Consider issue types which have defects tagged to them"
            }
    },
    {
            "fieldName": "notificationEnabler",
            "fieldLabel": "Processor Failure Notification",
            "fieldType": "radiobutton",
            "section": "Custom Fields Mapping",
            "tooltip": {
                 "definition": "On/Off notification in case processor failure."
            },
            "options": [{
                 "label": "On",
                 "value": "true"
            },
            {
                 "label": "Off",
                 "value": "false"
            }
            ]
    },
    {
            "fieldName": "epicPlannedValue",
            "fieldLabel": "Custom field for Epic Planned Value",
            "fieldType": "text",
            "fieldCategory": "fields",
            "section": "Custom Fields Mapping",
            "tooltip": {
                "definition": "JIRA applications let you add custom fields in addition to the built-in fields. Provide value of Planned Value for Epics that need to show on Trend line. <br> Example:customfield_11111 <hr>",
        }
        },
    {
            "fieldName": "epicAchievedValue",
            "fieldLabel": "Custom field for Epic Achieved Value",
            "fieldType": "text",
            "fieldCategory": "fields",
            "section": "Custom Fields Mapping",
            "tooltip": {
                "definition": "JIRA applications let you add custom fields in addition to the built-in fields. Provide value of Achieved Value for Epics that need to show on Trend line. <br> Example:customfield_11111 <hr>",
        }
        },
    {
        "fieldName": "jiraIssueEpicTypeKPI153",
        "fieldLabel": "Epic Issue Type",
        "fieldType": "chips",
        "fieldCategory": "Issue_Type",
        "section": "Issue Types Mapping",
        "tooltip": {
            "definition": "This field is used to identify Epic Issue type.",
        }
        },
        {
            "fieldName": "epicLink",
            "fieldLabel": "Custom field for Epic Link",
            "fieldType": "text",
            "fieldCategory": "fields",
            "section": "Custom Fields Mapping",
            "tooltip": {
                "definition": "JIRA applications let you add custom fields in addition to the built-in fields.Provide value of Epic Linkage to the story/defect<br />Example:customfield_11111<hr>"
            }
        }

])

// PI predictability KPI column config
db.getCollection('kpi_column_configs').insertOne({
                                 		basicProjectConfigId: null,
                                 		kpiId: 'kpi153',
                                 		kpiColumnDetails: [{
                                 			columnName: 'Project Name',
                                 			order: 0,
                                 			isShown: true,
                                 			isDefault: false
                                 		},  {
                                 			columnName: 'Epic ID',
                                 			order: 2,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Epic Name',
                                 			order: 3,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Status',
                                 			order: 4,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'PI Name',
                                 			order: 5,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Planned Value',
                                 			order: 6,
                                 			isShown: true,
                                 			isDefault: false
                                 		}, {
                                 			columnName: 'Achieved Value',
                                 			order: 7,
                                 			isShown: true,
                                 			isDefault: false
                                 		}
                                 		]
});

// delete dora kpi
db.kpi_category_mapping.deleteMany({
  "kpiId": {
    "$in": ["kpi116", "kpi118"]
  }
});

//adding dailyStandup kpi
//added PI Predictability KPI for categoryThree board
// dora kpi master changes
db.kpi_master.bulkWrite([{
  updateMany: { //changing dora kpi groupId
    filter: {
      kpiId: {
        $in: ["kpi116", "kpi118"]
      }
    },
    update: {
      $set: {
        groupId: 14
      }
    }
  }
}, { // change the x-axis of deployment freq
  updateOne: {
    filter: {
      kpiId: "kpi118"
    },
    update: {
      $set: {
        xAxisLabel: "Weeks"
      }
    }
  }
}, { // adding kpi category dora
  updateMany: {
    filter: {
      kpiId: {
        $in: ["kpi116", "kpi118"]
      }
    },
    update: {
      $set: {
        kpiCategory: "Dora"
      }
    }
  }
}, {
  insertOne: {
    document: {
      "kpiId": "kpi153",
      "kpiName": "PI Predictability",
      "maxValue": "200",
      "kpiUnit": "",
      "isDeleted": "False",
      "defaultOrder": 29,
      "kpiSource": "Jira",
      "groupId": 4,
      "thresholdValue": "",
      "kanban": false,
      "chartType": "multipleline",
      "kpiInfo": {
        "definition": "PI predictability is calculated by the sum of the actual value achieved against the planned value at the beginning of the PI",
        "details": [{
          "type": "link",
          "kpiLinkDetail": {
            "text": "Detailed Information at",
            "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/27131959/Scrum+VALUE+KPIs#PI-Predictability"
          }
        }]
      },
      "xAxisLabel": "PIs",
      "yAxisLabel": "Business Value",
      "isPositiveTrend": true,
      "showTrend": true,
      "aggregationCriteria": "sum",
      "isAdditionalFilterSupport": false,
      "calculateMaturity": false
    },
  }
}, {
  insertOne: {
    document: {
      "kpiId": "kpi154",
      "kpiName": "Daily Standup View",
      "maxValue": "",
      "isDeleted": "False",
      "defaultOrder": 8,
      "kpiCategory": "Iteration",
      "kpiSubCategory": "Daily Standup",
      "kpiSource": "Jira",
      "groupId": 13,
      "thresholdValue": "",
      "kanban": false,
      "isPositiveTrend": true,
      "showTrend": false,
      "isAdditionalFilterSupport": false,
      "kpiFilter": "multiselectdropdown",
      "kpiWidth": 100,
      "calculateMaturity": false
    }
  }
}]);

// Note : below code only For Opensource project
// PI predictability KPI category mapping
db.getCollection('kpi_category_mapping').insertOne( {
                                                    		"kpiId": "kpi153",
                                                    		"categoryId": "categoryThree",
                                                    		"kpiOrder": 4,
                                                    		"kanban": false
                                                    	});

//------------------------- 7.8.0 changes----------------------------------------------------------------------------------
// ---- New FieldMapping Field added to consider subtask defect not tagged in a release ---------------------------------------------------------------------------
// ---- New FieldMapping Upload Enable for In-sprint & Regression Percentage Kpi
// ---- //DTS-26123 new FieldMapping structures
db.getCollection('field_mapping_structure').insertMany([{
	"fieldName": "jiraSubTaskDefectType",
	"fieldLabel": "Issue type for sub-task defect",
	"fieldType": "chips",
	"fieldCategory": "Issue_Type",
	"section": "Issue Types Mapping",
	"tooltip": {
		"definition": "Any issue type mentioned will be considered as sub-task bug on Release dashboard"
	}
}, {
	"fieldName": "uploadDataKPI42",
	"fieldLabel": "KPI calculation logic",
	"fieldType": "toggle",
	"toggleLabel": "Upload Data",
	"section": "WorkFlow Status Mapping",
	"processorCommon": false,
	"tooltip": {
		"definition": "Enabled State (Kpi from data on Upload data screen)"
	}
}, {
	"fieldName": "uploadDataKPI16",
	"fieldLabel": "KPI calculation logic",
	"fieldType": "toggle",
	"toggleLabel": "Upload Data",
	"section": "WorkFlow Status Mapping",
	"processorCommon": false,
	"tooltip": {
		"definition": "Enabled State (Kpi from data on Upload data screen)"
	}
}, {
	"fieldName": "jiraDefectRejectionStatusKPI155",
	"fieldLabel": "Ticket Rejected/Dropped Status",
	"fieldType": "text",
	"fieldCategory": "workflow",
	"section": "WorkFlow Status Mapping",
	"tooltip": {
		"definition": "Status from workflow on which ticket is considered as Rejected/Dropped."
	}
}, {
	"fieldName": "jiraDodKPI155",
	"fieldLabel": "DOD Status",
	"fieldType": "chips",
	"fieldCategory": "workflow",
	"section": "WorkFlow Status Mapping",
	"tooltip": {
		"definition": "Status/es that identify that an issue is completed based on Definition of Done (DoD)."
	}
}, {
	"fieldName": "jiraLiveStatusKPI155",
	"fieldLabel": "Status to identify Live status",
	"fieldType": "text",
	"fieldCategory": "workflow",
	"section": "WorkFlow Status Mapping",
	"tooltip": {
		"definition": "Provide any status from workflow on which Live is considered."
	}
}]);

const fieldMappings = db.field_mapping.find({});
fieldMappings.forEach(function(fm) {
	db.field_mapping.updateOne({
		"_id": fm._id
	}, {
		$set: {
			"jiraDefectRejectionStatusKPI155": fm.jiraDefectRejectionStatus,
			"jiraDodKPI155": fm.jiraDod,
			"jiraLiveStatusKPI155": fm.jiraLiveStatus
		}
	});
});
//DTS-26123
db.getCollection('kpi_master').insertOne({
	"kpiId": "kpi155",
	"kpiName": "Defect Count By Type",
	"kpiUnit": "Count",
	"isDeleted": "False",
	"defaultOrder": 11,
	"kpiCategory": "Backlog",
	"kpiSource": "Jira",
	"groupId": 11,
	"thresholdValue": "",
	"kanban": false,
	"chartType": "pieChart",
	"kpiInfo": {
		"definition": "Total count of issues in the backlog with a breakup by defect type."
	},
	"xAxisLabel": "",
	"yAxisLabel": "",
	"isPositiveTrend": true,
	"showTrend": false,
	"isAdditionalFilterSupport": false,
	"kpiFilter": "dropdown",
	"boxType": "chart",
	"calculateMaturity": false
});

db.getCollection('metadata_identifier').updateMany(
   { "templateCode": { $in: ["4", "5", "6", "7"] } },
   { $push: {
   "workflow": {
                "type":"jiraDodKPI155",
                "value":[
                    "Closed",
                    "Done"
                ]
            }
   }}
);
//------------------------- 7.9.0 changes----------------------------------------------------------------------------------
//remove search options from fieldMapping structure
// make backlog leadTime Kpi chips
// add field for scope churn kpi
db.getCollection('field_mapping_structure').bulkWrite([
  {
    updateMany: {
      filter: { "fieldName": { $in: ["jiraDorKPI3", "jiraLiveStatusKPI3"] } },
      update: { $set: { "fieldType": "chips" } }
    }
  },
  {
    insertOne: {
      document: {
        "fieldName": "jiraStoryIdentificationKPI164",
        "fieldLabel": "Issue type to identify Story",
        "fieldType": "chips",
        "fieldCategory": "Issue_Type",
        "section": "Issue Types Mapping",
        "tooltip": {
          "definition": "All issue types that are used as/equivalent to Story"
        }
      }
    }
  },
  {
    updateMany: {
      filter: {
        "fieldName": {
          $in: [
            "resolutionTypeForRejectionKPI37",
            "resolutionTypeForRejectionKPI28",
            "resolutionTypeForRejectionDSR",
            "resolutionTypeForRejectionKPI82",
            "resolutionTypeForRejectionKPI135",
            "resolutionTypeForRejectionKPI133",
            "resolutionTypeForRejectionRCAKPI36",
            "resolutionTypeForRejectionKPI14",
            "resolutionTypeForRejectionQAKPI111",
            "resolutionTypeForRejectionKPI35"
          ]
        }
      },
          update: { $unset: { "fieldCategory": null } }
    }
  }
]);

);

//  reorder kpi group for performance
db.kpi_master.updateMany(
   { "kpiId": { $in: ["kpi72", "kpi111", "kpi82"] } }, // Match documents with specified kpiId values
   { $set: { "groupId": 4 } } // Set the new value for groupId
);

//For DTS-27550 making release Progress filter to dropdown
db.kpi_master.updateOne(
  { "kpiId": "kpi147" },
  { $set: { "kpiFilter": "dropDown" } }
);
// scope churn kpi_master
//DTS-28198 added radio button filter to release kpis
db.getCollection("kpi_master").bulkWrite(
  [
    {
      updateMany: {
        filter: { kpiId: { $in: ["kpi142", "kpi143", "kpi144"] } },
        update: { $set: { "kpiFilter": "radioButton" } }
      }
    },
    {
      insertOne: {
        document: {
          "kpiId": "kpi164",
          "kpiName": "Scope Churn",
          "maxValue": 200,
          "kpiUnit": "%",
          "isDeleted": false,
          "defaultOrder": 30,
          "kpiSource": "Jira",
          "groupId": 4,
          "thresholdValue": 85,
          "kanban": false,
          "chartType": "line",
          "kpiInfo": {
            "definition": "Scope churn explains the change in the scope of the sprint since the start of the iteration",
            "formula": [{
              "lhs": "Scope Churn",
              "operator": "division",
              "operands": ["Count of Stories added + Count of Stories removed", "Count of Stories in Initial Commitment at the time of Sprint start"]
            }],
            "details": [{
              "type": "link",
              "kpiLinkDetail": {
                "text": "Detailed Information at",
                "link": "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/26935328/Scrum+SPEED+KPIs#Scope-Churn"
              }
            }]
          },
          "xAxisLabel": "Sprints",
          "yAxisLabel": "Percentage",
          "isPositiveTrend": true,
          "showTrend": true,
          "aggregationCriteria": "average",
          "isAdditionalFilterSupport": true,
          "calculateMaturity": true
        }
      }
    }
  ]
);

// Scope Churn KPI column config
db.getCollection('kpi_column_configs').insertOne({
	basicProjectConfigId: null,
	kpiId: 'kpi164',
	kpiColumnDetails: [{
		columnName: 'Sprint Name',
		order: 0,
		isShown: true,
		isDefault: false
	}, {
		columnName: 'Issue ID',
		order: 2,
		isShown: true,
		isDefault: false
	}, {
		columnName: 'Issue Type',
		order: 3,
		isShown: true,
		isDefault: false
	}, {
		columnName: 'Issue Description',
		order: 4,
		isShown: true,
		isDefault: false
	}, {
		columnName: 'Size(story point/hours)',
		order: 5,
		isShown: true,
		isDefault: false
	}, {
		columnName: 'Scope Change Date',
		order: 6,
		isShown: true,
		isDefault: false
	}, {
		columnName: 'Scope Change (Added/Removed)',
		order: 7,
		isShown: true,
		isDefault: false
	}, {
		columnName: 'Issue Status',
		order: 8,
		isShown: true,
		isDefault: false
	}]
});

// --Converting the String to array and filling with curr str val for leadTime backlog
db.field_mapping.updateMany(
    {},
    [
        {
            $set: {
                jiraDorKPI3: ["$jiraDorKPI3"],
                jiraLiveStatusKPI3: ["$jiraLiveStatusKPI3"]
            }
        }
    ]
);

//DTS-27549 release sub tabs addition in release kpi's
db.kpi_master.updateMany(
    {
        "kpiCategory": "Release",
        "kpiId": { $in: ["kpi141", "kpi142","kpi143","kpi147","kpi144"] }
    },
    {
        $set: { "kpiSubCategory": "Release Review" }
    }
);

db.kpi_master.updateMany(
    {
        "kpiCategory": "Release",
        "kpiId": { $in: ["kpi150"] }
    },
    {
        $set: { "kpiSubCategory": "Release Progress" }
    }
);

// Note : below code only For Opensource project
// Scope Churn KPI category mapping
db.getCollection('kpi_category_mapping').insertOne({
	"kpiId": "kpi164",
	"categoryId": "categoryOne",
	"kpiOrder": 9,
	"kanban": false
});

// Note : below code only For Opensource project
db.getCollection('metadata_identifier').updateMany(
    { "templateCode": { $in: ["7"] } },
    {
        $push: {
            "workflow": {
                "type": "jiraStoryIdentificationKPI164",
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

//--- DTS-28864 ---
db.kpi_master.updateOne(
    {
        "kpiId": "kpi120"
    },
    {
        $set: { "kpiWidth": 100 }
    }
);

//------------------------- 7.10.0 changes----------------------------------------------------------------------------------
//--- For DTS-27550 making release Progress filter to dropdown
//--- DTS-27490 Iteration Readiness KPI for Backlog Dashboard
//--- DTS-28878 scope churn defect fix
db.kpi_master.bulkWrite([
  {
    updateOne: {
      filter: { "kpiId": "kpi147" },
      update: { $set: { "kpiFilter": "dropDown" } }
    }
  },
  {
    updateOne: {
      filter: { "kpiId": "kpi164" },
      update: {
        $set: {
          "thresholdValue": "20",
          "isPositiveTrend": "false",
          "maturityRange": ["-50", "50-30", "30-20", "20-10", "10-"]
        }
      }
    }
  },
  {
    insertOne: {
      document: {
        "kpiId": "kpi161",
        "kpiName": "Iteration Readiness",
        "maxValue": "",
        "kpiUnit": "Count",
        "isDeleted": "False",
        "defaultOrder": 5,
        "kpiCategory": "Backlog",
        "kpiSource": "Jira",
        "groupId": 11,
        "thresholdValue": "",
        "kanban": false,
        "chartType": "stackedColumn",
        "kpiInfo": {
          "definition": "Iteration readiness depicts the state of future iterations w.r.t the quality of refined Backlog"
        },
        "xAxisLabel": "Sprint",
        "yAxisLabel": "Count",
        "isPositiveTrend": true,
        "showTrend": false,
        "isAdditionalFilterSupport": false,
        "kpiFilter": "",
        "boxType": "chart",
        "calculateMaturity": false
      }
    }
  },
  {
      "insertOne":{
          "document":{
              "kpiId":"kpi163",
              "kpiName":"Defect by Testing Phase",
              "maxValue":"",
              "kpiUnit":"Count",
              "isDeleted":"False",
              "defaultOrder":7,
              "kpiCategory":"Release",
              "kpiSource":"Jira",
              "groupId":9,
              "thresholdValue":"",
              "kanban":false,
              "chartType":"horizontalPercentBarChart",
              "kpiInfo":{
                  "definition":" It gives a breakup of escaped defects by testing phase"
              },
              "xAxisLabel":"",
              "yAxisLabel":"",
              "isPositiveTrend":true,
              "showTrend":false,
              "isAdditionalFilterSupport":false,
              "kpiFilter":"radioButton",
              "boxType":"chart",
              "calculateMaturity":false,
              "kpiSubCategory":"Release Review"
          }
      }
  }
]);

db.getCollection('field_mapping_structure').insertMany(
    [{
        "fieldName": "jiraIssueTypeNamesKPI161",
        "fieldLabel": "Issue types to be included",
        "fieldType": "chips",
        "fieldCategory": "Issue_Type",
        "processorCommon": false,
        "section": "Issue Types Mapping",
        "tooltip": {
            "definition": "All the issue types used by a project in Jira."
        }
    },
    {
        "fieldName": "jiraIssueTypeNamesKPI148",
        "fieldLabel": "Issue types to be included",
        "fieldType": "chips",
        "fieldCategory": "Issue_Type",
        "processorCommon": false,
        "section": "Issue Types Mapping",
        "tooltip": {
            "definition": "All the issue types used by a project in Jira."
        }
    },
    {
        "fieldName": "jiraIssueTypeNamesKPI146",
        "fieldLabel": "Issue types to be included",
        "fieldType": "chips",
        "fieldCategory": "Issue_Type",
        "processorCommon": false,
        "section": "Issue Types Mapping",
        "tooltip": {
            "definition": "All the issue types used by a project in Jira."
        }
    },
    {
        "fieldName": "jiraIssueTypeNamesKPI151",
        "fieldLabel": "Issue types to be included",
        "fieldType": "chips",
        "fieldCategory": "Issue_Type",
        "processorCommon": false,
        "section": "Issue Types Mapping",
        "tooltip": {
            "definition": "All the issue types used by a project in Jira."
        }
    },
    {
        "fieldName": "jiraIssueTypeNamesKPI152",
        "fieldLabel": "Issue types to be included",
        "fieldType": "chips",
        "fieldCategory": "Issue_Type",
        "processorCommon": false,
        "section": "Issue Types Mapping",
        "tooltip": {
            "definition": "All the issue types used by a project in Jira."
        }
    },
    {
        "fieldName": "testingPhaseDefectsIdentifier",
        "fieldLabel": "Testing phase defects identification",
        "fieldType": "radiobutton",
        "section": "Defects Mapping",
        "tooltip": {
            "definition": "This field is used to identify a defect in which phase it is raised. 1. CustomField : If a separate custom field is used, 2. Labels : If a label is used to identify, 3. Component : If a Component is used to identify"
        },
        "options": [
            {
                "label": "CustomField",
                "value": "CustomField"
            },
            {
                "label": "Labels",
                "value": "Labels"
            },
            {
                "label": "Component",
                "value": "Component"
            }
        ],
        "nestedFields": [
            {
                "fieldName": "testingPhaseDefectCustomField",
                "fieldLabel": "Testing Phase Defect CustomField",
                "fieldType": "text",
                "fieldCategory": "fields",
                "filterGroup": [
                    "CustomField"
                ],
                "tooltip": {
                    "definition": " Provide customfield name to identify testing phase defects."
                }
            },
            {
                "fieldName": "testingPhaseDefectValue",
                "fieldLabel": "Testing Phase Defect Values",
                "fieldType": "chips",
                "filterGroup": [
                    "CustomField",
                    "Labels"
                ],
                "tooltip": {
                    "definition": "Provide label name to identify testing phase defects."
                }
            },
            {
                "fieldName": "testingPhaseDefectComponentValue",
                "fieldLabel": "Component",
                "fieldType": "text",
                "filterGroup": [
                    "Component"
                ],
                "tooltip": {
                    "definition": "Provide label name to identify testing phase defects."
                }
            }
        ]
    },
    {
        "fieldName": "jiraDodKPI163",
        "fieldLabel": "DOD Status",
        "fieldType": "chips",
        "fieldCategory": "workflow",
        "section": "WorkFlow Status Mapping",
        "tooltip": {
            "definition": "Status/es that identify that an issue is completed based on Definition of Done (DoD)."
        }
    }
    ]
)

const fieldMappings = db.field_mapping.find({});
fieldMappings.forEach(function(fm) {
	db.field_mapping.updateOne({
		"_id": fm._id
	}, {
		$set: {
			"jiraIssueTypeNamesKPI161": ["Story", "Enabler Story", "Tech Story", "Change request", "Defect", "Bug", "Epic"],
			"jiraIssueTypeNamesKPI151": ["Story", "Enabler Story", "Tech Story", "Change request", "Defect", "Bug", "Epic"],
			"jiraIssueTypeNamesKPI152": ["Story", "Enabler Story", "Tech Story", "Change request", "Defect", "Bug", "Epic"],
			"jiraIssueTypeNamesKPI146": ["Story", "Enabler Story", "Tech Story", "Change request", "Defect", "Bug", "Epic"],
			"jiraIssueTypeNamesKPI148": ["Story", "Enabler Story", "Tech Story", "Change request", "Defect", "Bug", "Epic"]
		}
	});
});

// -- DTS-28610 (Defect)

db.kpi_master.updateMany(
{
"kpiId": { $in: ["kpi14", "kpi111", "kpi35", "kpi37", "kpi28", "kpi36", "kpi116", "kpi84", "kpi8", "kpi64", "kpi67", "kpi164", "kpi38", "kpi27"] }
},
{
$set: { "upperThresholdBG": "red", "lowerThresholdBG" : "white" }
}
);

db.kpi_master.updateMany(
{
"kpiId": { $in: ["kpi82", "kpi34", "kpi126", "kpi42", "kpi16", "kpi70", "kpi72", "kpi11", "kpi118", "kpi73", "kpi113", "kpi62", "kpi17"]}
},
{
$set: { "upperThresholdBG": "white", "lowerThresholdBG" : "red" }
}
);

// KPI add Lead time for changes in DORA tab
db.kpi_master.bulkWrite([{ // adding kpi category dora
  updateMany: {
    filter: {
      kpiId: {
        $in: ["kpi116", "kpi118"]
      }
    },
    update: {
      $set: {
    "aggregationCircleCriteria" : "average"
      }
    }
  }
}, {
   insertOne: {
     document: {
    "kpiId": "kpi156",
    "kpiName": "Lead Time For Change",
    "maxValue": "100",
    "kpiUnit": "Days",
    "isDeleted": "False",
    "defaultOrder": 3,
    "kpiSource": "Jira",
    "kpiCategory": "Dora",
    "groupId": 15,
    "thresholdValue": 0,
    "kanban": false,
    "chartType": "line",
    "kpiInfo": {
      "definition": "LEAD TIME FOR CHANGE measures the velocity of software delivery.",
      "details": [
        {
          "type": "paragraph",
          "value": "LEAD TIME FOR CHANGE Captures the time between a code change to commit and deployed to production."
        }
      ],
      "maturityLevels": []
    },
    "xAxisLabel": "Weeks",
    "yAxisLabel": "Days",
    "isPositiveTrend": true,
    "showTrend": true,
    "kpiFilter": "",
    "aggregationCriteria": "sum",
    "aggregationCircleCriteria" : "average",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": false
 }
   }
 }]);

// fieldMapping Structure fields for Lead time for changes in DORA tab
db.getCollection('field_mapping_structure').insertMany([{
            "fieldName": "leadTimeConfigRepoTool",
            "fieldLabel": "Lead Time KPI calculation logic",
            "fieldType": "radiobutton",
            "section": "Custom Fields Mapping",
            "tooltip": {
                "definition": "By Default State Calculation is based on Jira Issues and Releases. <br>  1. Jira : Calculation is based on Jira Issues and Releases, <br>  2. Repo : Calculation is based on Repo Data and Releases. <br> Branch Name Must have Jira Issue Key in it.",
            },
            "options": [{
                    "label": "Jira",
                    "value": "Jira"
                },
                {
                    "label": "Repo",
                    "value": "Repo"
                }
            ],
            "nestedFields": [
{
	"fieldName": "jiraIssueTypeKPI156",
	"fieldLabel": "Issue type to be included",
	"fieldType": "chips",
	"fieldCategory": "Issue_Type",
	 "filterGroup": ["Jira"],
	"tooltip": {
		"definition": "Only these Issue Types will be considered for Lead Time Calculation. If this Configuration is not provided, all the Issue Types will be considered. <br> Example: Story, Enabler Story, Tech Story, Change request <hr>."
	}
}, {
	"fieldName": "jiraDodKPI156",
	"fieldLabel": "Status to identify DOD",
	"fieldType": "chips",
	"fieldCategory": "workflow",
	"filterGroup": ["Jira"],
	"tooltip": {
		"definition": " Definition of Doneness. Provide any status from workflow on which DOD is considered. Difference between the latest date of theses statuses and release end date will be considered as the Lead Time. <br> <br> <b>Note:</b> This configuration will be ignored if Lead Time KPI calculation logic is set to Repo Data. <br> <br> <b>Note:</b> This configuration will be ignored if Issue Type is not provided.br> Example: Closed,Done. <hr> "
	}
},{
	"fieldName": "toBranchForMRKPI156",
	"fieldLabel": "Production Branch Name",
	"fieldType": "text",
	"filterGroup": ["Repo"],
	"tooltip": {
		"definition": "Production Branch in Which all the Child Branches are Merged <br> eg. master <hr>"
	}
}]
}]);

db.getCollection('metadata_identifier').updateMany(
   { "templateCode": { $in: ["7"] } },
   { $push: {
   "workflow": {
                "type":"jiraDodKPI156",
                "value":[
                    "Closed"
                ]
            },
   "issues" : {
                      "type": "jiraIssueTypeKPI156",
                      "value": [
                          "Story",
                          "Enabler Story",
                          "Tech Story",
                          "Change request"
                      ]
}
   }}
);

// merge Request index
db.merge_requests.createIndex({"processorItemId":1,"createdDate":1, "fromBranch":1, "closedDate":1})

//processor items index
db.processor_items.createIndex({"toolConfigId":1})




// RepoTool - DTS-27526
// added new repo tools kpi for developer tab

db.getCollection("kpi_master").bulkWrite([
  {
    updateMany: {
      filter: { kpiId: { $in: ["kpi84", "kpi11", "kpi65"] } },
      update: {
        $set: {
          "isRepoToolKpi": false,
          "kpiCategory": "Developer",
        },
      },
    },
  },
{
    insertOne: {
      document: {
              "kpiId": "kpi157",
              "kpiName": "Check-Ins & Merge Requests",
              "maxValue": "10",
              "kpiUnit": "MRs",
              "isDeleted": "False",
              "defaultOrder": 1,
              "kpiSource": "BitBucket",
              "groupId": 1,
              "thresholdValue": "55",
              "kanban": false,
              "chartType": "grouped_column_plus_line",
              "kpiInfo": {
                "definition":
                  "NUMBER OF CHECK-INS helps in measuring the transparency as well the how well the tasks have been broken down. NUMBER OF MERGE REQUESTS when looked at along with commits highlights the efficiency of the review process",
                "details": [
                  {
                    "type": "paragraph",
                    "value":
                      "It is calculated as a Count. Higher the count better is the ‘Speed’",
                  },
                  {
                    "type": "paragraph",
                    "value":
                      "A progress indicator shows trend of Number of Check-ins & Merge requests between last 2 days. An upward trend is considered positive",
                  },
                ],
                "maturityLevels": [
                  {
                    "level": "M5",
                    "bgColor": "#6cab61",
                    "range": "> 16",
                  },
                  {
                    "level": "M4",
                    "bgColor": "#AEDB76",
                    "range": "8-16",
                  },
                  {
                    "level": "M3",
                    "bgColor": "#eff173",
                    "range": "4-8",
                  },
                  {
                    "level": "M2",
                    "bgColor": "#ffc35b",
                    "range": "2-4",
                  },
                  {
                    "level": "M1",
                    "bgColor": "#F06667",
                    "range": "0-2",
                  },
                ],
              },
              "xAxisLabel": "Days",
              "yAxisLabel": "Count",
              "lineLegend": "Merge Requests",
              "barLegend": "Commits",
              "isPositiveTrend": true,
              "showTrend": true,
              "kpiFilter": "dropDown",
              "aggregationCriteria": "average",
              "isAdditionalFilterSupport": false,
              "calculateMaturity": true,
              "hideOverallFilter": true,
              "maturityRange": ["-2", "2-4", "4-8", "8-16", "16-"],
              "isRepoToolKpi": true,
              "kpiCategory": "Developer",
            },
    },
  },
  {
    insertOne: {
      document: {
              "kpiId": "kpi158",
              "kpiName": "Mean Time To Merge",
              "maxValue": "10",
              "kpiUnit": "Hours",
              "isDeleted": "False",
              "defaultOrder": 2,
              "groupId": 1,
              "kpiSource": "BitBucket",
              "thresholdValue": "55",
              "kanban": false,
              "chartType": "line",
              "kpiInfo": {
                "definition":
                  "MEAN TIME TO MERGE measures the efficiency of the code review process in a team",
                "details": [
                  {
                    "type": "paragraph",
                    "value":
                      "It is calculated in ‘Hours’. Fewer the Hours better is the ‘Speed’",
                  },
                  {
                    "type": "paragraph",
                    "value":
                      "A progress indicator shows trend of Mean time to merge in last 2 weeks. A downward trend is considered positive",
                  },
                  {
                    "type": "paragraph",
                    "value":
                      "Maturity of the KPI is calculated based on the average of the last 5 weeks",
                  },
                ],
                "maturityLevels": [
                  {
                    "level": "M5",
                    "bgColor": "#6cab61",
                    "range": "<4 Hours",
                  },
                  {
                    "level": "M4",
                    "bgColor": "#AEDB76",
                    "range": "4-8 Hours",
                  },
                  {
                    "level": "M3",
                    "bgColor": "#eff173",
                    "range": "8-16 Hours",
                  },
                  {
                    "level": "M2",
                    "bgColor": "#ffc35b",
                    "range": "16-48 Hours",
                  },
                  {
                    "level": "M1",
                    "bgColor": "#F06667",
                    "range": ">48 Hours",
                  },
                ],
              },
              "xAxisLabel": "Weeks",
              "yAxisLabel": "Count(Hours)",
              "isPositiveTrend": false,
              "showTrend": true,
              "kpiFilter": "dropDown",
              "aggregationCriteria": "average",
              "isAdditionalFilterSupport": false,
              "calculateMaturity": true,
              "hideOverallFilter": true,
              "maturityRange": ["-16", "16-8", "8-4", "4-2", "2-"],
              "isRepoToolKpi": true,
              "kpiCategory": "Developer",
            },
    },
  },
  {
    insertOne: {
      document: {
          "kpiId": "kpi159",
          "kpiName": "Number of Check-ins",
          "maxValue": 10,
          "kpiUnit": "check-ins",
          "isDeleted": "False",
          "defaultOrder": 1,
          "groupId": 1,
          "kpiSource": "BitBucket",
          "thresholdValue": 55,
          "kanban": true,
          "chartType": "line",
          "kpiInfo": {
            "definition": "NUMBER OF CHECK-INS helps in measuring the transparency as well the how well the tasks have been broken down.",
            "details": [
              {
                "type": "paragraph",
                "value": "It is calculated as a Count. Higher the count better is the ‘Speed’"
              },
              {
                "type": "paragraph",
                "value": "A progress indicator shows trend of Number of Check-ins & Merge requests between last 2 days. An upward trend is considered positive."
              },
              {
                "type": "paragraph",
                "value": "Maturity of the KPI is calculated based on the latest value"
              }
            ],
            "maturityLevels": [
              {
                "level": "M5",
                "bgColor": "#6cab61",
                "range": ">16"
              },
              {
                "level": "M4",
                "bgColor": "#AEDB76",
                "range": "8-16"
              },
              {
                "level": "M3",
                "bgColor": "#eff173",
                "range": "4-8"
              },
              {
                "level": "M2",
                "bgColor": "#ffc35b",
                "range": "2-4"
              },
              {
                "level": "M1",
                "bgColor": "#F06667",
                "range": "0-2"
              }
            ]
          },
          "xAxisLabel": "Weeks",
          "yAxisLabel": "Count",
          "isPositiveTrend": true,
          "showTrend": true,
          "kpiFilter": "dropDown",
          "aggregationCriteria": "sum",
          "isAdditionalFilterSupport": false,
          "calculateMaturity": true,
          "hideOverallFilter": true,
          "maturityRange": [
            -2,
            "2-4",
            "4-8",
            "8-16",
            "16-"
          ],
          "isRepoToolKpi": true,
          "kpiCategory": "Developer"
        },
    },
  },
  {
    insertOne: {
      document: {
      "kpiId": "kpi160",
      "kpiName": "Pickup Time",
      "maxValue": 10,
      "kpiUnit": "Hours",
      "isDeleted": "False",
      "defaultOrder": 3,
      "groupId": 1,
      "kpiSource": "BitBucket",
      "thresholdValue": 20,
      "kanban": false,
      "chartType": "line",
      "kpiInfo": {
        "definition": "Pickup time measures the time a pull request waits for someone to start reviewing it. Low pickup time represents strong teamwork and a healthy review",
        "details": [
          {
            "type": "paragraph",
            "value": "It is calculated in ‘Hours’. Fewer the Hours better is the ‘Speed’"
          },
          {
            "type": "paragraph",
            "value": "A progress indicator shows trend of Pickup Time in last 2 weeks. A downward trend is considered positive"
          },
          {
            "type": "paragraph",
            "value": "Maturity of the KPI is calculated based on the average of the last 5 weeks"
          }
        ],
        "maturityLevels": [
          {
            "level": "M5",
            "bgColor": "#6cab61",
            "range": "<4 Hours"
          },
          {
            "level": "M4",
            "bgColor": "#AEDB76",
            "range": "4-8 Hours"
          },
          {
            "level": "M3",
            "bgColor": "#eff173",
            "range": "8-16 Hours"
          },
          {
            "level": "M2",
            "bgColor": "#ffc35b",
            "range": "16-48 Hours"
          },
          {
            "level": "M1",
            "bgColor": "#F06667",
            "range": ">48 Hours"
          }
        ]
      },
      "xAxisLabel": "Weeks",
      "yAxisLabel": "Count(Hours)",
      "isPositiveTrend": false,
      "showTrend": true,
      "kpiFilter": "dropDown",
      "aggregationCriteria": "average",
      "isAdditionalFilterSupport": false,
      "calculateMaturity": true,
      "hideOverallFilter": true,
      "maturityRange": [
        -16,
        "16-8",
        "8-4",
        "4-2",
        "2-"
      ],
      "isRepoToolKpi": true,
      "kpiCategory": "Developer"
    },
    },
  },
  {
    insertOne: {
      document: {
          "kpiId": "kpi162",
          "kpiName": "PR Size",
          "maxValue": 10,
          "kpiUnit": "Lines",
          "isDeleted": "False",
          "defaultOrder": 4,
          "groupId": 1,
          "kpiSource": "BitBucket",
          "kanban": false,
          "chartType": "line",
          "kpiInfo": {
            "definition": "Pull request size measures the number of code lines modified in a pull request. Smaller pull requests are easier to review, safer to merge, and correlate to a lower cycle time."
          },
          "xAxisLabel": "Weeks",
          "yAxisLabel": "Count(No. of Lines)",
          "isPositiveTrend": false,
          "showTrend": true,
          "kpiFilter": "dropDown",
          "aggregationCriteria": "average",
          "isAdditionalFilterSupport": false,
          "calculateMaturity": true,
          "hideOverallFilter": true,
          "maturityRange": [
            -16,
            "16-8",
            "8-4",
            "4-2",
            "2-"
          ],
          "isRepoToolKpi": true,
          "kpiCategory": "Developer"
        },
    },
  }
]);


db.getCollection("kpi_column_configs").insertMany([
    {
        "basicProjectConfigId": null,
        "kpiId": "kpi157",
        "kpiColumnDetails": [
          {
            "columnName": "Project Name",
            "order": 0,
            "isShown": true,
            "isDefault": true
          },
          {
            "columnName": "Repository Url",
            "order": 1,
            "isShown": true,
            "isDefault": true
          },
          {
            "columnName": "Branch",
            "order": 2,
            "isShown": true,
            "isDefault": true
          },
          {
            "columnName": "Day",
            "order": 3,
            "isShown": true,
            "isDefault": true
          },
          {
            "columnName": "No. Of Commit",
            "order": 4,
            "isShown": true,
            "isDefault": true
          },
          {
            "columnName": "No. of Merge",
            "order": 5,
            "isShown": true,
            "isDefault": true
          }
        ]
      },
      {
        "basicProjectConfigId": null,
        "kpiId": "kpi158",
        "kpiColumnDetails": [
          {
            "columnName": "Project Name",
            "order": 0,
            "isShown": true,
            "isDefault": true
          },
          {
            "columnName": "Repository Url\t",
            "order": 1,
            "isShown": true,
            "isDefault": true
          },
          {
            "columnName": "Branch",
            "order": 2,
            "isShown": true,
            "isDefault": true
          },
          {
            "columnName": "Weeks",
            "order": 3,
            "isShown": true,
            "isDefault": true
          },
          {
            "columnName": "Mean Time To Merge (In Hours)",
            "order": 4,
            "isShown": true,
            "isDefault": true
          }
        ]
      },
      {
        "basicProjectConfigId": null,
        "kpiId": "kpi159",
        "kpiColumnDetails": [
          {
            "columnName": "Project Name",
            "order": 0,
            "isShown": true,
            "isDefault": true
          },
          {
            "columnName": "Repository Url\t",
            "order": 1,
            "isShown": true,
            "isDefault": true
          },
          {
            "columnName": "Branch",
            "order": 2,
            "isShown": true,
            "isDefault": true
          },
          {
            "columnName": "Days",
            "order": 3,
            "isShown": true,
            "isDefault": true
          },
          {
            "columnName": "No. Of Commit",
            "order": 4,
            "isShown": true,
            "isDefault": true
          }
        ]
      },
      {
        "basicProjectConfigId": null,
        "kpiId": "kpi160",
        "kpiColumnDetails": [
          {
            "columnName": "Project Name",
            "order": 0,
            "isShown": true,
            "isDefault": true
          },
          {
            "columnName": "Repository Url\t",
            "order": 1,
            "isShown": true,
            "isDefault": true
          },
          {
            "columnName": "Branch",
            "order": 2,
            "isShown": true,
            "isDefault": true
          },
          {
            "columnName": "Weeks",
            "order": 3,
            "isShown": true,
            "isDefault": true
          },
          {
            "columnName": "Pickup Time (In Hours)",
            "order": 4,
            "isShown": true,
            "isDefault": true
          }
        ]
      },
      {
        "basicProjectConfigId": null,
        "kpiId": "kpi162",
        "kpiColumnDetails": [
          {
            "columnName": "Project Name",
            "order": 0,
            "isShown": true,
            "isDefault": true
          },
          {
            "columnName": "Repository Url\t",
            "order": 1,
            "isShown": true,
            "isDefault": true
          },
          {
            "columnName": "Branch",
            "order": 2,
            "isShown": true,
            "isDefault": true
          },
          {
            "columnName": "Weeks",
            "order": 3,
            "isShown": true,
            "isDefault": true
          },
          {
            "columnName": "PR Size",
            "order": 4,
            "isShown": true,
            "isDefault": true
          }
        ]
      }
  ]);

//added repo tool processor
db.processor.insertOne(
  {
    "processorName": "RepoTool",
    "processorType": "SCM",
    "isActive": true,
    "isOnline": true,
    "errors": [],
    "isLastSuccess": false,
  }
)


db.repo_tools_provider.insertMany([
  {
    "toolName": "bitbucket",
    "debbieProvider": "bitbucket_oauth2",
    "testApiUrl": "https://api.bitbucket.org/2.0/repositories/",
    "repoToolProvider": "bitbucket_oauth2"
  },
  {
    "toolName": "gitlab",
    "debbieProvider": "gitlab",
    "testApiUrl": "https://gitlab.com/api/v4/projects/"
  },
  {
    "toolName": "github",
    "debbieProvider": "github",
    "testApiUrl": "https://api.github.com/users/",
    "repoToolProvider": "github"
  }
])


//remove bitbucket kpis from speed
db.kpi_category_mapping.deleteMany(
  { "kpiId": { $in: ["kpi84", "kpi65", "kpi11"] } });


db.getCollection('metadata_identifier').updateMany(
   { "templateCode": { $in: ["7"] } },
   { $push: {
   "workflow": {
                "type":"jiraDodKPI163",
                "value":[
                    "Closed",
                    "Done"
                ]
            }
   }}
);
//------------------------- 8.0.0 changes----------------------------------------------------------------------------------
db.kpi_master.bulkWrite([
    {
        insertOne: {
            "document":
            {
                "kpiId": "kpi165",
                "kpiName": "Epic Progress",
                "maxValue": "",
                "kpiUnit": "Count",
                "isDeleted": "False",
                "defaultOrder": 5,
                "kpiCategory": "Release",
                "kpiSubCategory": "Value",
                "kpiSource": "Jira",
                "groupId": 9,
                "thresholdValue": "",
                "kanban": false,
                "chartType": "horizontalPercentBarChart",
                "kpiInfo": {
                    "definition": "It depicts the progress of each epic in a release in terms of total count and %age completion."
                },
                "xAxisLabel": "",
                "yAxisLabel": "",
                "kpiWidth": 100,
                "isPositiveTrend": true,
                "showTrend": false,
                "isAdditionalFilterSupport": false,
                "kpiFilter": "multiSelectDropDown",
                "boxType": "chart",
                "calculateMaturity": false
            }
        }
    },
    {
        insertOne: {
            "document":
            {
                "kpiId": "kpi169",
                "kpiName": "Epic Progress",
                "maxValue": "",
                "kpiUnit": "Count",
                "isDeleted": "False",
                "defaultOrder": 5,
                "kpiCategory": "Backlog",
                "kpiSource": "Jira",
                "groupId": 9,
                "thresholdValue": "",
                "kanban": false,
                "chartType": "horizontalPercentBarChart",
                "kpiInfo": {
                    "definition": "It depicts the progress of each epic in terms of total count and %age completion."
                },
                "xAxisLabel": "",
                "yAxisLabel": "",
                "kpiWidth": 100,
                "isPositiveTrend": true,
                "showTrend": false,
                "isAdditionalFilterSupport": false,
                "kpiFilter": "multiSelectDropDown",
                "boxType": "chart",
                "calculateMaturity": false,
                "kpiSubCategory": "Epic View"
            }

        }
    },
    {
        updateMany: {
            filter: { "kpiId": { $in: ["kpi152", "kpi155", "kpi151"] } },
            update: { $set: { "kpiSubCategory": "Summary" } }
        }
    },
    {
        updateMany: {
            filter: { "kpiId": { $in: ["kpi139", "kpi138", "kpi127", "kpi137", "kpi129", "kpi161"] } },
            update: { $set: { "kpiSubCategory": "Backlog Health" } }
        }
    },
    {
        updateMany: {
            filter: { "kpiId": { $in: ["kpi3", "kpi148", "kpi146"] } },
            update: { $set: { "kpiSubCategory": "Flow KPIs" } }
        }
    }, {
        updateMany: {
            filter: { "kpiId": { $in: ["kpi147", "kpi150"] } },
            update: { $set: { "kpiSubCategory": "Speed" } }
        }
    },
    {
        updateMany: {
            filter: { "kpiId": { $in: ["kpi141", "kpi142", "kpi143", "kpi144", "kpi163"] } },
            update: { $set: { "kpiSubCategory": "Quality" } }
        },
    },
    {
        updateMany: {
            filter: { "kpiId": { $in: ["kpi150","kpi147", "kpi3"] } },
            update: { $set: { "kpiWidth": 100 } }
        },
    }
]);


//DTS-29115 Prod fix
db.kpi_master.updateMany(
   { "kpiId": { $in: ["kpi40", "kpi46", "kpi164"] } }, // Match documents with specified kpiId values
   { $set: { "groupId": 5 } } // Set the new value for groupId
)

db.kpi_master.updateOne({ "kpiId": "kpi14" }, { $set: { "groupId": 3 } })
db.kpi_master.updateOne({ "kpiId": "kpi149" }, { $set: { "groupId": 16 } })

//notificationEnabler fixed
db.getCollection('field_mapping_structure').updateOne(
    {"fieldName": "notificationEnabler"},
    {
        $set: {
            "fieldLabel": "Processor Failure Notification",
            "fieldType": "radiobutton",
            "section": "Custom Fields Mapping",
            "tooltip": {
                "definition": "On/Off notification in case processor failure."
            },
            "options": [
                {
                    "label": "On",
                    "value": true
                },
                {
                    "label": "Off",
                    "value": false
                }
            ]
        }
    }
);
db.kpi_master.updateMany(
    { "kpiId": "kpi34" },
    { $set: { "kpiInfo.formula.$[].operands": ["No. of defects in the iteration that are fixed", "Total no. of defects in an iteration"] } }
);
);

