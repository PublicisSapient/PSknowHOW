
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
        const storyFirstStatus=fm.storyFirstStatus;
        const jiraWaitStatus=fm.jiraWaitStatus;
        const jiraBlockedStatus=fm.jiraBlockedStatus;
        const jiraIncludeBlockedStatus=fm.jiraIncludeBlockedStatus;
        const jiraDevDoneStatus=fm.jiraDevDoneStatus;
        db.field_mapping.updateOne({ "_id": fm._id }, {
            $set: {
                "defectPriorityIFTPR": defectPriority,
                "defectPriorityDIR": defectPriority,
                "defectPriorityQADD": defectPriority,
                "defectPriorityFTPR": defectPriority,
                "defectPriorityQS": defectPriority,

                "jiradefecttypeSWE": jiradefecttype,
                "jiradefecttypeIDCR": jiradefecttype,
                "jiradefecttypeIDCS": jiradefecttype,
                "jiradefecttypeIDCP": jiradefecttype,
                "jiradefecttypeRDCA": jiradefecttype,
                "jiradefecttypeRDCP": jiradefecttype,
                "jiradefecttypeRDCR": jiradefecttype,
                "jiradefecttypeRDCS": jiradefecttype,
                "jiradefecttypeQS": jiradefecttype,
                "jiradefecttypeIWS": jiradefecttype,
                "jiradefecttypeLT": jiradefecttype,
                "jiradefecttypeMW": jiradefecttype,
                "jiradefecttypeFTPR": jiradefecttype,
                "jiradefecttypeIFTPR": jiradefecttype,
                "jiradefecttypeIC": jiradefecttype,
                "jiradefecttypeAVR": jiradefecttype,
                "jiradefecttypeCVR": jiradefecttype,
                "jiradefecttypeBDRR": jiradefecttype,

                "jiraStatusForDevelopmentAVR": jiraStatusForDevelopment,
                "jiraStatusForDevelopmentFTPR": jiraStatusForDevelopment,
                "jiraStatusForDevelopmentIFTPR": jiraStatusForDevelopment,

                "jiraDodDIR": jiraDod,
                "jiraDodQADD": jiraDod,
                "jiraDodLT": jiraDod,
                "jiraDodPDA": jiraDod,

                "jiraDefectRejectionStatusAVR": jiraDefectRejectionStatus,
                "jiraDefectRejectionStatusDC": jiraDefectRejectionStatus,
                "jiraDefectRejectionStatusDRE": jiraDefectRejectionStatus,
                "jiraDefectRejectionStatusDRR": jiraDefectRejectionStatus,
                "jiraDefectRejectionStatusDSR": jiraDefectRejectionStatus,
                "jiraDefectRejectionStatusFTPR": jiraDefectRejectionStatus,
                "jiraDefectRejectionStatusIFTPR": jiraDefectRejectionStatus,
                "jiraDefectRejectionStatusQS": jiraDefectRejectionStatus,
                "jiraDefectRejectionStatusRCA": jiraDefectRejectionStatus,
                "jiraDefectRejectionStatusDIR": jiraDefectRejectionStatus,
                "jiraDefectRejectionStatusQADD": jiraDefectRejectionStatus,

                "jiraSprintVelocityIssueTypeSV": jiraSprintVelocityIssueType,
                "jiraSprintVelocityIssueTypeBR": jiraSprintVelocityIssueType,

                "jiraDefectCountlIssueTypeDC": jiraDefectCountlIssueType,
                "jiraDefectCountlIssueTypeRCA": jiraDefectCountlIssueType,

                "jiraIssueDeliverdStatusBR": jiraIssueDeliverdStatus,
                "jiraIssueDeliverdStatusSV": jiraIssueDeliverdStatus,
                "jiraIssueDeliverdStatusAVR": jiraIssueDeliverdStatus,
                "jiraIssueDeliverdStatusCVR": jiraIssueDeliverdStatus,
                "jiraIssueDeliverdStatusFTPR": jiraIssueDeliverdStatus,

                "jiraIntakeToDorIssueTypeLT": jiraIntakeToDorIssueType,

                "jiraStoryIdentificationIC": jiraStoryIdentification,

                "excludeRCAFromIFTPR": excludeRCAFromFTPR,
                "excludeRCAFromDIR": excludeRCAFromFTPR,
                "excludeRCAFromQADD": excludeRCAFromFTPR,
                "excludeRCAFromQS": excludeRCAFromFTPR,

                "resolutionTypeForRejectionAVR": resolutionTypeForRejection,
                "resolutionTypeForRejectionDC": resolutionTypeForRejection,
                "resolutionTypeForRejectionDRE": resolutionTypeForRejection,
                "resolutionTypeForRejectionDRR": resolutionTypeForRejection,
                "resolutionTypeForRejectionDSR": resolutionTypeForRejection,
                "resolutionTypeForRejectionFTPR": resolutionTypeForRejection,
                "resolutionTypeForRejectionIFTPR": resolutionTypeForRejection,
                "resolutionTypeForRejectionQS": resolutionTypeForRejection,
                "resolutionTypeForRejectionRCA": resolutionTypeForRejection,
                "resolutionTypeForRejectionDIR": resolutionTypeForRejection,
                "resolutionTypeForRejectionQADD": resolutionTypeForRejection,

               "jiraStatusForInProgressCPT":jiraStatusForInProgress,
                	 "jiraStatusForInProgressDCS": jiraStatusForInProgress,
                	 "jiraStatusForInProgressIBU": jiraStatusForInProgress,
                	 "jiraStatusForInProgressPWS": jiraStatusForInProgress,
                	 "jiraStatusForInProgressILS": jiraStatusForInProgress,
                	"jiraStatusForInProgressWR": jiraStatusForInProgress,

                	"issueStatusExcluMissingWorkEH":issueStatusExcluMissingWork,

                "jiraIssueTypeNamesAVR":jiraIssueTypeNames,

                "storyFirstStatusLT":storyFirstStatus,

                "jiraLiveStatusLT":jiraLiveStatus,
                "jiraLiveStatusLTK":jiraLiveStatus,
                "jiraLiveStatusNOPK":jiraLiveStatus,
                "jiraLiveStatusNOSK":jiraLiveStatus,
                "jiraLiveStatusNORK":jiraLiveStatus,
                "jiraLiveStatusOTA":jiraLiveStatus,
                "jiraLiveStatusPDA":jiraLiveStatus,

                "jiraDorLT":jiraDor,

                "jiraIFTPRStoryIdentification":jiraFTPRStoryIdentification,

                "jiraWaitStatusIW": jiraWaitStatus,

                "jiraBlockedStatusIW": jiraBlockedStatus,

                "jiraIncludeBlockedStatusIW": jiraIncludeBlockedStatus,

                "jiraDevDoneStatusWR": jiraDevDoneStatus,
                "jiraDevDoneStatusDCS": jiraDevDoneStatus,
                "jiraDevDoneStatusPWS": jiraDevDoneStatus,

                "jiraIterationCompletionStatusIFTPR" : jiraIterationCompletionStatusCustomField,
                "jiraIterationCompletionStatusCPT" : jiraIterationCompletionStatusCustomField,
                "jiraIterationCompletionStatusEVA" : jiraIterationCompletionStatusCustomField,
                "jiraIterationCompletionStatusDCS" : jiraIterationCompletionStatusCustomField,
                "jiraIterationCompletionStatusIDCP" : jiraIterationCompletionStatusCustomField,
                "jiraIterationCompletionStatusIDCR" : jiraIterationCompletionStatusCustomField,
                "jiraIterationCompletionStatusIDCS" : jiraIterationCompletionStatusCustomField,
                "jiraIterationCompletionStatusIC" : jiraIterationCompletionStatusCustomField,
                "jiraIterationCompletionStatusCR" : jiraIterationCompletionStatusCustomField,
                "jiraIterationCompletionStatusSV" : jiraIterationCompletionStatusCustomField,
                "jiraIterationCompletionStatusSP" : jiraIterationCompletionStatusCustomField,
                "jiraIterationCompletionStatusEH" : jiraIterationCompletionStatusCustomField,
                "jiraIterationCompletionStatusILS" : jiraIterationCompletionStatusCustomField,
                "jiraIterationCompletionStatusIBU" : jiraIterationCompletionStatusCustomField,
                "jiraIterationCompletionStatusICO" : jiraIterationCompletionStatusCustomField,
                "jiraIterationCompletionStatusPWS" : jiraIterationCompletionStatusCustomField,
                "jiraIterationCompletionStatusUWS" : jiraIterationCompletionStatusCustomField,
                "jiraIterationCompletionStatusQS" : jiraIterationCompletionStatusCustomField,
                "jiraIterationCompletionStatusWR" : jiraIterationCompletionStatusCustomField,
                "jiraIterationCompletionStatusIW" : jiraIterationCompletionStatusCustomField,
                "jiraIterationCompletionStatusBRE" : jiraIterationCompletionStatusCustomField,

                "jiraIterationCompletionTypeCPT" : jiraIterationCompletionTypeCustomField,
                "jiraIterationCompletionTypeBRE" : jiraIterationCompletionTypeCustomField,
                "jiraIterationCompletionTypeIW" : jiraIterationCompletionTypeCustomField,
                "jiraIterationCompletionTypePWS" : jiraIterationCompletionTypeCustomField,
                "jiraIterationCompletionTypeUPWS" : jiraIterationCompletionTypeCustomField,
                "jiraIterationCompletionTypeDCS" : jiraIterationCompletionTypeCustomField,
                "jiraIterationCompletionTypeCR" : jiraIterationCompletionTypeCustomField,
                "jiraIterationCompletionTypeWR" : jiraIterationCompletionTypeCustomField,
                "jiraIterationCompletionTypeSP" : jiraIterationCompletionTypeCustomField,
                "jiraIterationCompletionTypeEVA" : jiraIterationCompletionTypeCustomField,
                "jiraIterationCompletionTypeILS" : jiraIterationCompletionTypeCustomField,
                "jiraIterationCompletionTypeIBU" : jiraIterationCompletionTypeCustomField,
                "jiraIterationCompletionTypeICO" : jiraIterationCompletionTypeCustomField,
                "jiraIterationCompletionTypeEH" : jiraIterationCompletionTypeCustomField




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