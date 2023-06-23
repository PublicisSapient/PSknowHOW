
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

                "jiraIssueTypeNamesAVR":jiraIssueTypeNames,

                "jiraLiveStatusLT":jiraLiveStatus,
                "jiraLiveStatusLTK":jiraLiveStatus

            },

            $unset: {
                "defectPriority": "",
                "jiraStatusForDevelopment": "",
                "resolutionTypeForRejection": "",
                "jiraIntakeToDorIssueType": "",
                "jiraDor": "",
                "jiraIssueDeliverdStatus": "",
                "jiraDefectCountlIssueType": "",
                "jiraSprintVelocityIssueType": "",
                "jiraDefectRejectionStatus": "",
                "jiraIssueTypeNames": ""

            }
        })
    }
})