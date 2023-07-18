
//field_mapping_structure
db.getCollection('field_mapping_structure').remove({});
db.getCollection('field_mapping_structure').insert(
[
  {
    "fieldName": "jiraStoryIdentificationKpi40",
    "fieldLabel": "Issue type to identify Story",
    "fieldType": "chips",
    "fieldCategory": "Issue_Type",
    "section": "Issue Types Mapping",
    "tooltip": {
      "definition": "All issue types that are used as/equivalent to Story.",
      "kpiImpacted": "Issue Count Kpi"
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
    "fieldName": "jiraIterationIssuetypeBRE",
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
  },{
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
    "fieldName": "jiraIssueDeliverdStatusBR",
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
    "fieldType": "chips",
    "fieldCategory": "workflow",
    "section": "WorkFlow Status Mapping",
    "tooltip": {
      "definition": " Definition of Readiness. Provide any status from workflow on which DOR is considered. <br>Example: In Sprint<hr>KPI Impacted:Lead Time - Intake to DOR and DOR to DOD"
    }
  },
  {
    "fieldName": "jiraIssueTypeKPI3",
    "fieldLabel": "Lead time issue type",
    "fieldType": "chips",
    "fieldCategory": "Issue_Type",
    "section": "Issue Types Mapping",
    "tooltip": {
      "definition": "The issue type which is to be considered while calculating lead time KPIs, i.e. intake to DOR and DOR and DOD ... <br> Example: Story, Change Request <hr>"
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
    "options": [
      {
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
    "options": [
      {
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
    "options": [
      {
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
    "options": [
      {
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
    "options": [
      {
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
      "definition": " Definition of Doneness. Provide any status from workflow on which DOD is considered. <br> Example: In Testing <hr>"
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
    "fieldName": "jiraDodPDA",
    "fieldLabel": "DOD Status",
    "fieldType": "chips",
    "fieldCategory": "workflow",
    "section": "WorkFlow Status Mapping",
    "tooltip": {
      "definition": " Definition of Doneness. Provide any status from workflow on which DOD is considered. <br> Example: In Testing <hr>"
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
    "processorCommon":true,
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
    "fieldType": "chips",
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
      "fieldLabel": "Iteration Dashboard & SPEED KPIs Completion Status",
      "fieldType": "chips",
      "fieldCategory": "workflow",
      "section": "WorkFlow Status Mapping",
      "tooltip": {
        "definition": "Status to identify as closed"
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
      "fieldName": "jiraIterationCompletionStatusBRE",
      "fieldLabel": "Iteration Dashboard & SPEED KPIs Completion Status",
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
      "definition": "Provide any status from workflow on which Live is considered. <br>Example: Live<hr>"
    }
  },
  {
      "fieldName": "jiraLiveStatusKPI3",
      "fieldLabel": "Live Status - Lead Time",
      "fieldType": "text",
      "fieldCategory": "workflow",
      "section": "WorkFlow Status Mapping",
      "tooltip": {
        "definition": "Provide any status from workflow on which Live is considered. <br>Example: Live<hr>"
      }
  },
  {
      "fieldName": "jiraLiveStatusLTK",
      "fieldLabel": "Live Status - Lead Time",
      "fieldType": "text",
      "fieldCategory": "workflow",
      "section": "WorkFlow Status Mapping",
      "tooltip": {
        "definition": "Provide any status from workflow on which Live is considered. <br>Example: Live<hr>"
      }
   },
  {
      "fieldName": "jiraLiveStatusNOPK",
      "fieldLabel": "Live Status - Lead Time",
      "fieldType": "text",
      "fieldCategory": "workflow",
      "section": "WorkFlow Status Mapping",
      "tooltip": {
        "definition": "Provide any status from workflow on which Live is considered. <br>Example: Live<hr>"
      }
   },
  {
      "fieldName": "jiraLiveStatusNOSK",
      "fieldLabel": "Live Status - Lead Time",
      "fieldType": "text",
      "fieldCategory": "workflow",
      "section": "WorkFlow Status Mapping",
      "tooltip": {
        "definition": "Provide any status from workflow on which Live is considered. <br>Example: Live<hr>"
      }
   },
  {
      "fieldName": "jiraLiveStatusNORK",
      "fieldLabel": "Live Status - Lead Time",
      "fieldType": "text",
      "fieldCategory": "workflow",
      "section": "WorkFlow Status Mapping",
      "tooltip": {
        "definition": "Provide any status from workflow on which Live is considered. <br>Example: Live<hr>"
      }
   },
  {
      "fieldName": "jiraLiveStatusOTA",
      "fieldLabel": "Live Status - Lead Time",
      "fieldType": "text",
      "fieldCategory": "workflow",
      "section": "WorkFlow Status Mapping",
      "tooltip": {
        "definition": "Provide any status from workflow on which Live is considered. <br>Example: Live<hr>"
      }
   },
  {
      "fieldName": "jiraLiveStatusPDA",
      "fieldLabel": "Live Status - Lead Time",
      "fieldType": "text",
      "fieldCategory": "workflow",
      "section": "WorkFlow Status Mapping",
      "tooltip": {
        "definition": "Provide any status from workflow on which Live is considered. <br>Example: Live<hr>"
      }
   },
  {
    "fieldName": "jiradefecttype",
    "fieldLabel": "Status to identify defects",
    "fieldType": "chips",
    "fieldCategory": "Issue_Type",
    "section": "Defects Mapping",
    "processorCommon":true,
    "tooltip": {
      "definition": "All the issue types that signify a defect in Jira/Azure"
    }
  },
{
    "fieldName": "jiraStoryPointsCustomField",
    "fieldLabel": "Story Points Custom Field",
    "fieldType": "text",
    "fieldCategory": "custom",
    "section": "Custom Fields Mapping",
    "tooltip": {
      "definition": "Field used in Jira for Story points"
    }
},
{
    "fieldName": "workingHoursDayCPT",
    "fieldLabel": "Working Hours in a Day",
    "fieldType": "text",
    "fieldCategory": "custom",
    "section": "Custom Fields Mapping",
    "tooltip": {
      "definition": "Working hours in a day"
    }
},
{
    "fieldName": "epicCostOfDelay",
    "fieldLabel": "Custom field for COD",
    "fieldType": "text",
    "fieldCategory": "custom",
    "section": "Custom Fields Mapping",
    "tooltip": {
      "definition": "JIRA/AZURE applications let you add custom fields in addition to the built-in fields. Provide value of Cost Of delay field for Epics that need to show on Trend line. <br> Example:customfield_11111 <hr>",
      "kpiImpacted":"Cost of delay"
    }
},
{
    "fieldName": "epicRiskReduction",
    "fieldLabel": "Custom field for Risk Reduction",
    "fieldType": "text",
    "fieldCategory": "custom",
    "section": "Custom Fields Mapping",
    "tooltip": {
      "definition": "JIRA/AZURE applications let you add custom fields in addition to the built-in fields.Provide value of Risk reduction/ Enablement value for Epic that is required to calculated Cost of delay <br> Example: customfield_11111<hr>",
      "kpiImpacted":"Cost of delay"
    }
},
{
    "fieldName": "epicUserBusinessValue",
    "fieldLabel": "Custom field for BV",
    "fieldType": "text",
    "fieldCategory": "custom",
    "section": "Custom Fields Mapping",
    "tooltip": {
      "definition": "JIRA/AZURE applications let you add custom fields in addition to the built-in fields.Provide value of User-Business Value for Epic that is required to calculated Cost of delay. <br>Example:customfield_11111<hr>",
      "kpiImpacted":"Cost of delay"
    }
},
{
    "fieldName": "epicWsjf",
    "fieldLabel": "Custom field for WSJF",
    "fieldType": "text",
    "fieldCategory": "custom",
    "section": "Custom Fields Mapping",
    "tooltip": {
      "definition": "JIRA/AZURE applications let you add custom fields in addition to the built-in fields.Provide value of WSJF value that is required to calculated Cost of delay <br />Example:customfield_11111<hr>",
      "kpiImpacted":"Cost of delay"
    }
},
{
    "fieldName": "epicTimeCriticality",
    "fieldLabel": "Custom field for Time Criticality",
    "fieldType": "text",
    "fieldCategory": "custom",
    "section": "Custom Fields Mapping",
    "tooltip": {
      "definition": "JIRA/AZURE applications let you add custom fields in addition to the built-in fields.Provide value of Time Criticality value on Epic that is required to calculated Cost of delay .<br />Example:customfield_11111<hr>",
      "kpiImpacted":"Cost of delay"
    }
},
{
    "fieldName": "epicJobSize",
    "fieldLabel": "Custom field for Job Size",
    "fieldType": "text",
    "fieldCategory": "custom",
    "section": "Custom Fields Mapping",
    "tooltip": {
      "definition": "JIRA/AZURE applications let you add custom fields in addition to the built-in fields.Provide value of Job size on EPIC that is required to calculated WSJF. <br>Example:customfield_11111<hr>",
      "kpiImpacted":"Cost of delay"
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
     "options": [
          {
            "label": "Story Point",
            "value": "Story Point"
          },
          {
            "label": "Actual (Original Estimation)",
            "value": "Actual Estimation"
          }
        ],
     "nestedFields": [
          {
            "fieldName": "storyPointToHourMapping",
            "fieldLabel": "Story Point to Hour Conversion",
            "fieldType": "text",
            "filterGroup": ["Story Point"],
            "tooltip": {
              "definition": "Estimation technique used by teams for e.g. story points, Hours etc."
            }
          }
        ]
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
     "options": [
          {
            "label": "Blocked Status",
            "value": "Blocked Status"
          },
          {
            "label": "Include Flagged Issue",
            "value": "Include Flagged Issue"
          }
        ],
     "nestedFields": [
          {
            "fieldName": "jiraBlockedStatusKPI131",
            "fieldLabel": "Status to Identify 'Blocked' status ",
            "fieldType": "chips",
            "filterGroup": ["Blocked Status"],
            "tooltip": {
              "definition": "Provide Status to Identify Blocked Issues<br />Example: On_Hold <hr> KPI Impacted : Iteration Board - Wastage KPI"
            }
          }
        ]
},
{
    "fieldName": "jiraBugRaisedByQAIdentification",
    "fieldLabel": "QA Defect Identification",
    "fieldType": "radiobutton",
    "section": "Defects Mapping",
     "tooltip": {
          "definition": "This field is used to identify if a defect is raised by QA<br>1. CustomField : If a separate custom field is used.<br>2. Labels : If a label is used to identify. Example: QA Defect <hr>"
        },
     "options": [
          {
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
            "fieldName": "jiraBugRaisedByQAValue",
            "fieldLabel": "QA Defect Values",
            "fieldType": "chips",
            "filterGroup": ["CustomField","Labels"],
            "tooltip": {
              "definition": "Provide label name to identify QA raised defects."
            }
          },
          {
            "fieldName": "jiraBugRaisedByQACustomField",
            "fieldLabel": "QA Defect Custom Field",
            "fieldType": "text",
            "fieldCategory": "custom",
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
     "options": [
          {
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
            "fieldCategory": "custom",
            "filterGroup": ["CustomField"],
            "tooltip": {
              "definition": "Provide customfield name to identify UAT or client raised defects. <br> Example: customfield_13907<hr>"
            }
          },
          {
                        "fieldName": "jiraBugRaisedByValue",
                        "fieldLabel": "UAT Defect Values",
                        "fieldType": "chips",
                        "filterGroup": ["CustomField","Labels"],
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
          "kpiImpacted":"Filters"
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
},{
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
},{
    "fieldName": "jiraStatusForInProgressKPI125",
    "fieldLabel": "Status to identify In Progress issues",
     "fieldType": "chips",
    "fieldCategory": "workflow",
     "section": "WorkFlow Status Mapping",
     "tooltip": {
          "definition": "All statuses that issues have moved from the Created status and also has not been completed",
          }
},{
    "fieldName": "jiraStatusForInProgressKPI123",
    "fieldLabel": "Status to identify In Progress issues",
     "fieldType": "chips",
    "fieldCategory": "workflow",
     "section": "WorkFlow Status Mapping",
     "tooltip": {
          "definition": "All statuses that issues have moved from the Created status and also has not been completed",
          }
},{
    "fieldName": "jiraStatusForInProgressKPI119",
    "fieldLabel": "Status to identify In Progress issues",
     "fieldType": "chips",
    "fieldCategory": "workflow",
     "section": "WorkFlow Status Mapping",
     "tooltip": {
          "definition": "All statuses that issues have moved from the Created status and also has not been completed",
          }
},{
    "fieldName": "jiraStatusForInProgressKPI128",
    "fieldLabel": "Status to identify In Progress issues",
     "fieldType": "chips",
    "fieldCategory": "workflow",
     "section": "WorkFlow Status Mapping",
     "tooltip": {
          "definition": "All statuses that issues have moved from the Created status and also has not been completed",
          }
},{
    "fieldName": "jiraDevDoneStatusKPI119",
    "fieldLabel": "Status to identify Dev completed issues",
     "fieldType": "chips",
    "fieldCategory": "workflow",
     "section": "WorkFlow Status Mapping",
     "tooltip": {
          "definition": "Status that confirms that the development work is completed and an issue can be passed on for testing",
          }
},{
    "fieldName": "jiraDevDoneStatusKPI145",
    "fieldLabel": "Status to identify Dev completion",
     "fieldType": "chips",
    "fieldCategory": "workflow",
     "section": "WorkFlow Status Mapping",
     "tooltip": {
          "definition": "Status that confirms that the development work is completed and an issue can be passed on for testing",
          }
},{
    "fieldName": "jiraDevDoneStatusKPI128",
    "fieldLabel": "Status to identify Dev completed issues",
     "fieldType": "chips",
    "fieldCategory": "workflow",
     "section": "WorkFlow Status Mapping",
     "tooltip": {
          "definition": "Status that confirms that the development work is completed and an issue can be passed on for testing",
          }
},{
    "fieldName": "jiraDefectCountlIssueTypeKPI28",
    "fieldLabel": "Issue type to be included",
     "fieldType": "chips",
    "fieldCategory": "Issue_Type",
     "section": "Issue Types Mapping",
     "tooltip": {
          "definition": "Issue types that are considered as defects in Jira.",
          }
},{
    "fieldName": "jiraDefectCountlIssueTypeKPI36",
    "fieldLabel": "Issue type to be included",
     "fieldType": "chips",
    "fieldCategory": "Issue_Type",
     "section": "Issue Types Mapping",
     "tooltip": {
          "definition": "Issue types that are considered as defects in Jira.",
          }
},{
    "fieldName": "jiraStatusForQaKPI135",
    "fieldLabel": "Status to Identify In Testing Status",
     "fieldType": "chips",
    "fieldCategory": "workflow",
     "section": "Workflow Status Mapping",
     "tooltip": {
          "definition": "The status of Defect Issue Type which identifies the 'In-Testing' status in JIRA. <br> Example: Ready For Testing<hr>",
          "Kpi Impacted": "Indiviual Filter"
          }
},{
    "fieldName": "jiraStatusForQaKPI82",
    "fieldLabel": "Status to Identify In Testing Status",
     "fieldType": "chips",
    "fieldCategory": "workflow",
     "section": "Workflow Status Mapping",
     "tooltip": {
          "definition": "The status of Defect Issue Type which identifies the 'In-Testing' status in JIRA. <br> Example: Ready For Testing<hr>",
          "Kpi Impacted": "Indiviual Filter"
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
         "KPI Impacted":"Jira Processor History"
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
   }
]
);