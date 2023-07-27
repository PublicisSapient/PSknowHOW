//---------7.2.0-------


//7.3 changes

//-------------------- insert new kpi details -------

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


 //7.4 changes

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
