
const fieldMappings = db.field_mapping.find({});
fieldMappings.forEach(function(fm) {
    if (!fm.createdDate) {
        const defectPriority = fm.defectPriority;
        const jiradefecttype = fm.jiradefecttype;
        const jiraStatusForDevelopment = fm.jiraStatusForDevelopment;
        const jiraDod = fm.jiraDod;
        const jiraDefectRejectionStatus = fm.jiraDefectRejectionStatus;
        const jiraSprintVelocityIssueType = fm.jiraSprintVelocityIssueType;
        const jiraDefectCountlIssueType = fm.jiraDefectCountlIssueType;
        const jiraIssueDeliverdStatus = fm.jiraIssueDeliverdStatus;
        const excludeRCAFromFTPR = fm.excludeRCAFromFTPR;
        const jiraIssueTypeNames=fm.jiraIssueTypeNames;
        const resolutionTypeForRejection = fm.resolutionTypeForRejection;
        const jiraLiveStatus=fm.jiraLiveStatus;
        const jiraFTPRStoryIdentification=fm.jiraFTPRStoryIdentification;
        const jiraIterationCompletionStatusCustomField=fm.jiraIterationCompletionStatusCustomField;
        const jiraIterationCompletionTypeCustomField=fm.jiraIterationCompletionTypeCustomField;
        const jiraDor=fm.jiraDor;
        const jiraIntakeToDorIssueType=fm.jiraIntakeToDorIssueType;
        const jiraStoryIdentification=fm.jiraStoryIdentification;
        const jiraStatusForInProgress=fm.jiraStatusForInProgress;
        const issueStatusExcluMissingWork=fm.issueStatusExcluMissingWork;
        const jiraWaitStatus=fm.jiraWaitStatus;
        const jiraBlockedStatus=fm.jiraBlockedStatus;
        const jiraIncludeBlockedStatus=fm.jiraIncludeBlockedStatus;
        const jiraDevDoneStatus=fm.jiraDevDoneStatus;
        const jiraQADefectDensityIssueType=fm.jiraQADefectDensityIssueType;
        const jiraDefectInjectionIssueType=fm.jiraDefectInjectionIssueType;
        const jiraDefectCreatedStatus=fm.jiraDefectCreatedStatus;
        const jiraDefectSeepageIssueType=fm.jiraDefectSeepageIssueType;
        const jiraDefectRemovalStatus=fm.jiraDefectRemovalStatus;
        const jiraDefectRemovalIssueType=fm.jiraDefectRemovalIssueType;
        const jiraSprintCapacityIssueType=fm.jiraSprintCapacityIssueType;
        const jiraDefectRejectionlIssueType=fm.jiraDefectRejectionlIssueType;
        const jiraStatusForQa=fm.jiraStatusForQa;
        db.field_mapping.updateOne({ "_id": fm._id }, {
          $set: {

            "jiradefecttypeSWE":jiradefecttype,
        
            "defectPriorityKPI135":defectPriority,
            "defectPriorityKPI14":defectPriority,
            "defectPriorityQAKPI111":defectPriority,
            "defectPriorityKPI82":defectPriority,
            "defectPriorityKPI133":defectPriority,
        
            "jiraIssueTypeNamesAVR": jiraIssueTypeNames,
        
            "jiraStatusForDevelopmentAVR": jiraStatusForDevelopment,
            "jiraStatusForDevelopmentKPI82": jiraStatusForDevelopment,
            "jiraStatusForDevelopmentKPI135": jiraStatusForDevelopment,
        
            "jiraDefectInjectionIssueTypeKPI14": jiraDefectInjectionIssueType,
            
            "jiraDodKPI14":jiraDod,
            "jiraDodQAKPI111":jiraDod,
            "jiraDodLT":jiraDod,
            "jiraDodPDA":jiraDod,
            
            "jiraDefectCreatedStatusKPI14": jiraDefectCreatedStatus,
       
            "jiraDefectRejectionStatusAVR":jiraDefectRejectionStatus,
            "jiraDefectRejectionStatusKPI28":jiraDefectRejectionStatus,
            "jiraDefectRejectionStatusKPI34":jiraDefectRejectionStatus,
            "jiraDefectRejectionStatusKPI37":jiraDefectRejectionStatus,
            "jiraDefectRejectionStatusKPI35":jiraDefectRejectionStatus,
            "jiraDefectRejectionStatusKPI82":jiraDefectRejectionStatus,
            "jiraDefectRejectionStatusKPI135":jiraDefectRejectionStatus,
            "jiraDefectRejectionStatusKPI133":jiraDefectRejectionStatus,
            "jiraDefectRejectionStatusRCAKPI36":jiraDefectRejectionStatus,
            "jiraDefectRejectionStatusKPI14":jiraDefectRejectionStatus,
            "jiraDefectRejectionStatusQAKPI111":jiraDefectRejectionStatus,
        
            "jiraIssueTypeKPI35": jiraDefectSeepageIssueType,
        
            "jiraDefectRemovalStatusKPI34": jiraDefectRemovalStatus,
            "jiraDefectRemovalIssueTypeKPI34": jiraDefectRemovalIssueType,
        
            "jiraSprintVelocityIssueTypeKpi39": jiraSprintVelocityIssueType,
            "jiraSprintVelocityIssueTypeBR": jiraSprintVelocityIssueType,
        
            "jiraSprintCapacityIssueTypeKpi46": jiraSprintCapacityIssueType,
        
            "jiraIssueTypeKPI37": jiraDefectRejectionlIssueType,
        
            "jiraDefectCountlIssueTypeKPI28": jiraDefectCountlIssueType,
            "jiraDefectCountlIssueTypeKPI36": jiraDefectCountlIssueType,
        
            "jiraIssueDeliverdStatusBR": jiraIssueDeliverdStatus,
            "jiraIssueDeliverdStatusKpi39": jiraIssueDeliverdStatus,
            "jiraIssueDeliverdStatusAVR": jiraIssueDeliverdStatus,
            "jiraIssueDeliverdStatusKPI126": jiraIssueDeliverdStatus,
            "jiraIssueDeliverdStatusKPI82": jiraIssueDeliverdStatus,
        
            "jiraDorLT": jiraDor,
       
            "jiraIssueTypeLT": jiraIntakeToDorIssueType,
        
            "jiraStoryIdentificationKpi40": jiraStoryIdentification,

            "jiraKPI82StoryIdentification": jiraFTPRStoryIdentification,
            "jiraKPI135StoryIdentification": jiraFTPRStoryIdentification,
        
            "jiraLiveStatusLT": jiraLiveStatus,
            "jiraLiveStatusLTK": jiraLiveStatus,
            "jiraLiveStatusNOPK": jiraLiveStatus,
            "jiraLiveStatusNOSK": jiraLiveStatus,
            "jiraLiveStatusNORK": jiraLiveStatus,
            "jiraLiveStatusOTA": jiraLiveStatus,
            "jiraLiveStatusPDA": jiraLiveStatus,
        
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

            "jiraStatusForQaKPI135":jiraStatusForQa,
            "jiraStatusForQaKPI82":jiraStatusForQa,
        
            "jiraStatusForInProgressKPI122": jiraStatusForInProgress,
            "jiraStatusForInProgressKPI145": jiraStatusForInProgress,
            "jiraStatusForInProgressKPI125": jiraStatusForInProgress,
            "jiraStatusForInProgressKPI128": jiraStatusForInProgress,
            "jiraStatusForInProgressKPI123": jiraStatusForInProgress,
            "jiraStatusForInProgressKPI119": jiraStatusForInProgress,
        
            "issueStatusExcluMissingWorkKPI124": issueStatusExcluMissingWork,
        
            "jiraDevDoneStatusKPI119": jiraDevDoneStatus,
            "jiraDevDoneStatusKPI145": jiraDevDoneStatus,
            "jiraDevDoneStatusKPI128": jiraDevDoneStatus,

            	"jiraWaitStatusKPI131": jiraWaitStatus,
            

            	"jiraBlockedStatusKPI131": jiraBlockedStatus,

            	"jiraIncludeBlockedStatusKPI131": jiraIncludeBlockedStatus,
        
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
            "jiraIterationCompletionStatusBRE": jiraIterationCompletionStatusCustomField,
        
            "jiraIterationIssuetypeKPI122": jiraIterationCompletionTypeCustomField,
            "jiraIterationIssuetypeBRE": jiraIterationCompletionTypeCustomField,
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
          }
//            $unset: {
//                "defectPriority": "",
//                "jiraStatusForDevelopment": "",
//                "resolutionTypeForRejection": "",
//                "jiraIssueDeliverdStatus": "",
//                "jiraDefectCountlIssueType": "",
//                "jiraSprintVelocityIssueType": "",
//                "jiraDefectRejectionStatus": "",
//                "jiraIssueTypeNames": ""
//
//            }
        })
    }
})