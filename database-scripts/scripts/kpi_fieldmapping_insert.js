db.getCollection('kpi_fieldmapping').remove({});
db.getCollection('kpi_fieldmapping').insert(
[
{
        kpiId: 'kpi14',
        kpiName: 'Defect Injection Rate',
		kpiSource:'Jira',
        type: ['Scrum'],
        fieldNames :  {'Workflow Status Mapping' : ['jiraDod', 'jiraDefectCreatedStatus', 'jiraDefectDroppedStatus','resolutionTypeForRejectionKPI14','jiraDefectRejectionStatusKPI14'], 'Issue Types Mapping' : ['jiraDefectInjectionIssueType'], 'Defects Mapping' : ['defectPriority', 'excludeRCAFromFTPR'] }
      },
      {
        kpiId: 'kpi82',
        kpiName: 'First Time Pass Rate',
		kpiSource:'Jira',
        type: ['Scrum'],
        fieldNames : {'Workflow Status Mapping' : ['resolutionTypeForRejectionFTPR','jiraIssueDeliverdStatusFTPR','jiraDefectRejectionStatusFTPR','jiraIterationCompletionStatusCustomField','jiraStatusForQa','jiraStatusForDevelopment','jiraFtprRejectStatus'], 'Defects Mapping' : ['defectPriority', 'excludeRCAFromFTPR'] ,'Issue Types Mapping' : ['jiraIterationIssuetypeCustomField','jiraFTPRStoryIdentification']}
        },
        {
                kpiId: 'kpi135',
                kpiName: 'First Time Pass Rate',
        		kpiSource:'Jira',
                type: ['Scrum'],
                fieldNames : {'Workflow Status Mapping' : ['resolutionTypeForRejectionIFTPR','jiraIssueDeliverdStatusFTPR','jiraDefectRejectionStatusIFTPR','jiraIterationCompletionStatusCustomField','jiraStatusForQa','jiraStatusForDevelopment','jiraFtprRejectStatus'], 'Defects Mapping' : ['defectPriority', 'excludeRCAFromFTPR'] ,'Issue Types Mapping' : ['jiraIterationIssuetypeCustomField','jiraFTPRStoryIdentification']}
                },

      {
        kpiId: 'kpi111',
        kpiName: 'Defect Density',
		kpiSource:'Jira',
        type: ['Scrum'],
        fieldNames : {'Issue Types Mapping' : ['jiraQADefectDensityIssueType'], 'Workflow Status Mapping' : ['jiraDod','resolutionTypeForRejection','jiraDefectRejectionStatus'], 'Defects Mapping' : ['jiraBugRaisedByQAIdentification','defectPriority','excludeRCAFromFTPR'], 'Custom Fields Mapping' : ['estimationCriteria', 'storyPointToHourMapping', 'jiraStoryPointsCustomField'] }
      },
      {
        kpiId: 'kpi35',
        kpiName: 'Defect Seepage Rate',
		kpiSource:'Jira',
        type: ['Scrum'],
        fieldNames : {'Issue Types Mapping' :['jiraDefectSeepageIssueType'], 'Workflow Status Mapping' : ['resolutionTypeForRejectionDSR','jiraDefectRejectionStatusDSR','jiraDefectDroppedStatus'], 'Defects Mapping' : ['jiraBugRaisedByIdentification'] }
      },
      {
        kpiId: 'kpi34',
        kpiName: 'Defect Removal Efficiency',
		kpiSource:'Jira',
        type: ['Scrum'],
        fieldNames : {'Workflow Status Mapping' :  ['resolutionTypeForRejectionDRE','jiraDefectRejectionStatusDRE','jiraDefectRemovalStatus'], 'Issue Types Mapping' : ['jiraDefectRemovalIssueType'] }
      },
      {
        kpiId: 'kpi37',
        kpiName: 'Defect Rejection Rate',
		kpiSource:'Jira',
        type: ['Scrum'],
        fieldNames : { 'Workflow Status Mapping' : ['jiraDefectRejectionStatusDRR','resolutionTypeForRejectionDRR'], 'Issue Types Mapping' :  ['jiraDefectRejectionlIssueType'] }
      },
      {
        kpiId: 'kpi28',
        kpiName: 'Defect Count By Priority',
		kpiSource:'Jira',
        type: ['Scrum'],
        fieldNames : {'Issue Types Mapping' :  ['jiraDefectCountlIssueType'] , 'Workflow Status Mapping' : ['resolutionTypeForRejectionDC','jiraDefectRejectionStatusDC','jiraDefectDroppedStatus'] }
      },
      {
        kpiId: 'kpi36',
        kpiName: 'Defect Count By RCA',
		kpiSource:'Jira',
        type: ['Scrum'],
        fieldNames : {'Issue Types Mapping' :  ['jiraDefectCountlIssueType'], 'Workflow Status Mapping' : ['jiraDefectDroppedStatus','jiraDefectRejectionStatusRCA','resolutionTypeForRejectionRCA'] }
      },
      {
        kpiId: 'kpi126',
        type: ['Scrum'],
        kpiName: 'Created vs Resolved defects',
		kpiSource:'Jira',
        fieldNames : {'Workflow Status Mapping' : ['jiraIssueDeliverdStatus'] }
      },
      {
        kpiId: 'kpi42',
        kpiName: 'Regression Automation Coverage',
		kpiSource: 'Zypher',
        type: ['Scrum'],
        fieldNames : {'Test Cases Mapping' : ['jiraRegressionTestValue', 'testRegressionValue', 'regressionAutomationFolderPath'] }
      },
      {
        kpiId: 'kpi16',
        kpiName: 'In-Sprint Automation Coverage',
		kpiSource: 'Zypher',
        type: ['Scrum'],
        fieldNames : {'Test Cases Mapping' : ['jiraRegressionTestValue', 'testRegressionValue', 'regressionAutomationFolderPath'] }
      },
      {
        kpiId: 'kpi17',
        type: ['Scrum'],
        kpiName: 'Unit Test Coverage',
		kpiSource: 'Sonar',
        fieldNames : { }
      },
      {
        kpiId: 'kpi38',
        kpiName: 'Sonar Violations',
		kpiSource: 'Sonar',
        type: ['Scrum'],
        fieldNames : { }
      },
      {
        kpiId: 'kpi27',
        kpiName: 'Sonar Tech Debt',
		kpiSource: 'Sonar',
        type: ['Scrum'],
        fieldNames : { }
      },
      {
        kpiId: 'kpi116',
        kpiName: 'Change Failure Rate',
		kpiSource: 'Jenkins',
        type: ['Scrum'],
        fieldNames : { }
      },
      {
        kpiId: 'kpi70',
        kpiName: 'Test Execution and pass percentage',
		kpiSource: 'Zypher',
        type: ['Scrum'],
        fieldNames : { }
      },
      {
        kpiId: 'kpi40',
        kpiName: 'Issue Count',
		kpiSource: 'Jira',
        type: ['Scrum'],
        fieldNames : {'Issue Types Mapping' : ['jiraStoryIdentificationKpi40','jiraIterationIssuetypeCustomField'] ,'Workflow Status Mapping' : ['jiraIterationCompletionStatusCustomField'], 'Defects Mapping' : ['jiradefecttype']}
      },
      {
        kpiId: 'kpi72',
        kpiName: 'Commitment Reliability',
		kpiSource: 'Jira',
        type: ['Scrum'],
        fieldNames : { 'Issue Types Mapping' : ['jiraIterationIssuetypeCustomField'], 'Workflow Status Mapping' : ['jiraIterationCompletionStatusCustomField'], 'Custom Fields Mapping' : ['estimationCriteria', 'storyPointToHourMapping', 'jiraStoryPointsCustomField'] }
      },
      {
        kpiId: 'kpi39',
        kpiName: 'Sprint Velocity',
		kpiSource: 'Jira',
        type: ['Scrum'],
        fieldNames : {'Issue Types Mapping' : ['jiraSprintVelocityIssueTypeSV','jiraIterationIssuetypeCustomField'], 'Custom Fields Mapping' : ['estimationCriteria', 'storyPointToHourMapping', 'jiraStoryPointsCustomField'] ,'Workflow Status Mapping' : ['jiraIssueDeliverdStatusSV','jiraIterationCompletionStatusCustomField']}
      },
      {
        kpiId: 'kpi46',
        kpiName: 'Sprint Capacity Utilization',
		kpiSource: 'Jira',
        type: ['Scrum'],
        fieldNames : {'Issue Types Mapping' : ['jiraSprintCapacityIssueType'] }
      },
      {
        kpiId: 'kpi84',
        type: ['Scrum'],
        kpiName: 'Mean Time To Merge',
		kpiSource: 'BitBucket',
        fieldNames : { }
      },
      {
        kpiId: 'kpi11',
        type: ['Scrum'],
        kpiName: 'Check-Ins & Merge Requests',
		kpiSource: 'BitBucket',
        fieldNames : { }
      },
      {
        kpiId: 'kpi8',
        kpiName: 'Code Build Time',
		kpiSource: 'Jenkins',
        type: ['Scrum'],
        fieldNames : { }
      },
      {
        kpiId: 'kpi3',
        kpiName: 'Lead Time',
		kpiSource: 'Jira',
        type: ['Scrum'],
        fieldNames : {'Issue Types Mapping' : ['jiraIntakeToDorIssueType'], 'Workflow Status Mapping' : ['jiraDor', 'jiraDod', 'jiraLiveStatus'] }
      },
      {
        kpiId: 'kpi118',
        kpiName: 'Deployment Frequency',
		kpiSource: 'Jenkins',
        type: ['Scrum'],
        fieldNames : { }
      },
      {
        kpiId: 'kpi73',
        kpiName: 'Release Frequency',
		kpiSource: 'Jira',
        type: ['Scrum'],
        fieldNames : { }
      },
      {
        kpiId: 'kpi113',
        kpiName: 'Value delivered (Cost of Delay)',
		kpiSource: 'Jira',
        type: ['Scrum'],
        fieldNames : {'Custom Fields Mapping' : ['epicCostOfDelay'] }
      },
	  {
        kpiId: 'kpi5',
        kpiName: 'Sprint Predictability',
		kpiSource: 'Jira',
        type: ['Scrum'],
        fieldNames : {'Issue Types Mapping' : ['jiraIterationIssuetypeCustomField'], 'Workflow Status Mapping' : ['jiraIterationCompletionStatusCustomField'], 'Custom Fields Mapping' : ['estimationCriteria', 'storyPointToHourMapping', 'jiraStoryPointsCustomField'] }
      },
      {
        kpiId: 'kpi55',
        kpiName: 'Ticket Open vs Closed rate by type',
		kpiSource: 'Jira',
        type: ['Kanban'],
        fieldNames : {'Issue Types Mapping' : ['ticketCountIssueType'], 'Workflow Status Mapping' : ['jiraTicketClosedStatus'] }
      },
      {
        kpiId: 'kpi54',
        kpiName: 'Ticket Open vs Closed rate by Priority',
		kpiSource: 'Jira',
        type: ['Kanban'],
        fieldNames : {'Issue Types Mapping' : ['ticketCountIssueType'],'Workflow Status Mapping' : ['jiraTicketClosedStatus'] }
      },
      {
        kpiId: 'kpi50',
        kpiName: 'Net Open Ticket Count by Priority',
		kpiSource: 'Jira',
        type: ['Kanban'],
        fieldNames : { 'Workflow Status Mapping' : ['storyFirstStatus', 'jiraTicketClosedStatus', 'jiraLiveStatus', 'jiraTicketRejectedStatus'], 'Issue Types Mapping' : ['kanbanRCACountIssueType', 'ticketCountIssueType'] }
      },
      {
        kpiId: 'kpi51',
        kpiName: 'Net Open Ticket Count By RCA',
		kpiSource: 'Jira',
        type: ['Kanban'],
        fieldNames : { 'Workflow Status Mapping' : ['storyFirstStatus', 'jiraTicketClosedStatus', 'jiraLiveStatus', 'jiraTicketRejectedStatus'], 'Issue Types Mapping' : ['kanbanRCACountIssueType', 'ticketCountIssueType']  }
      },
      {
        kpiId: 'kpi48',
        kpiName: 'Net Open Ticket By Status',
		kpiSource: 'Jira',
        type: ['Kanban'],
        fieldNames : { 'Workflow Status Mapping' : ['storyFirstStatus', 'jiraTicketClosedStatus', 'jiraLiveStatus', 'jiraTicketRejectedStatus'], 'Issue Types Mapping' : ['kanbanRCACountIssueType', 'ticketCountIssueType']  }
      },
      {
        kpiId: 'kpi997',
        kpiName: 'Open Ticket Ageing By Priority',
		kpiSource: 'Jira',
        type: ['Kanban'],
        fieldNames : { 'Workflow Status Mapping' : ['jiraTicketClosedStatus', 'jiraLiveStatus', 'jiraTicketRejectedStatus'], 'Issue Types Mapping' : ['ticketCountIssueType'] }
      },
      {
        kpiId: 'kpi63',
        kpiName: 'Regression Automation Coverage',
		kpiSource: 'Zypher',
        type: ['Kanban'],
        fieldNames : { 'Test Cases Mapping' : ['jiraRegressionTestValue', 'testRegressionValue', 'regressionAutomationFolderPath'] }
      },
      {
        kpiId: 'kpi49',
        kpiName: 'Ticket Velocity',
		kpiSource: 'Jira',
        type: ['Kanban'],
        fieldNames : { 'Issue Types Mapping' : ['jiraTicketVelocityIssueType'], 'Workflow Status Mapping' : ['ticketDeliverdStatus'] }
      },
      {
        kpiId: 'kpi53',
        kpiName: 'Lead Time',
        kpiSource:'Jira',
        type: ['Kanban'],
        fieldNames : {'Workflow Status Mapping' : ['jiraTicketTriagedStatus', 'jiraTicketClosedStatus', 'jiraLiveStatus'] }
      },
      {
        kpiId: 'kpi114',
        type: ['Kanban'],
		kpiSource: 'Jira',
        kpiName: 'Value delivered (Cost of Delay)',
        fieldNames : {'Custom Fields Mapping' : ['epicCostOfDelay'] }
      },
      {
        kpiId: 'kpi74',
        kpiName: 'Release Frequency',
		kpiSource: 'Jira',
        type: ['Kanban'],
        fieldNames : { }
      },
      {
        kpiId: 'kpi62',
        kpiName: 'Unit Test Coverage',
		kpiSource: 'Sonar',
        type: ['Kanban'],
        fieldNames : { }
      },
      {
        kpiId: 'kpi64',
        kpiName: 'Sonar Violations',
		kpiSource: 'Sonar',
        type: ['Kanban'],
        fieldNames : { }
      },
      {
        kpiId: 'kpi67',
        kpiName: 'Sonar Tech Debt',
		kpiSource: 'Sonar',
        type: ['Kanban'],
        fieldNames : { }
      },
      {
        kpiId: 'kpi71',
        kpiName: 'Test Execution and pass percentage',
		kpiSource: 'Zypher',
        type: ['Kanban'],
        fieldNames : { }
      },
      {
        kpiId: 'kpi58',
        kpiName: 'Team Capacity',
		kpiSource: 'Jira',
        type: ['Kanban'],
        fieldNames : { }
      },
      {
        kpiId: 'kpi66',
        kpiName: 'Code Build Time',
		kpiSource: 'Jenkins',
        type: ['Kanban'],
        fieldNames : { }
      },
      {
        kpiId: 'kpi65',
        kpiName: 'Number of Check-ins',
		kpiSource: 'BitBucket',
        type: ['Kanban'],
        fieldNames : { }
      },

      {
        kpiId: 'kpi119',
        kpiName: 'Work Remaining',
		kpiSource: 'Jira',
        type: ['Other'],
        fieldNames : { 'Workflow Status Mapping' : ['jiraStatusForInProgress', 'jiraDevDoneStatus','jiraIterationCompletionStatusCustomField'], 'Issue Types Mapping' : ['jiraIterationIssuetypeCustomField'],'Custom Fields Mapping' : ['jiraDueDateField']}
      },
      {
        kpiId: 'kpi75',
        kpiName: 'Estimate vs Actual',
		kpiSource: 'Jira',
        type: ['Other'],
        fieldNames : { 'Workflow Status Mapping' : ['jiraIterationCompletionStatusCustomField'], 'Issue Types Mapping' : ['jiraIterationIssuetypeCustomField']}
      },
      {
        kpiId: 'kpi123',
        kpiName: 'Issues likely to Spill',
		kpiSource: 'Jira',
        type: ['Other'],
        fieldNames : { 'Workflow Status Mapping' : ['jiraStatusForInProgress','jiraIterationCompletionStatusCustomField'], 'Custom Fields Mapping' : ['jiraDueDateField'],'Issue Types Mapping' : ['jiraIterationIssuetypeCustomField']}
      },
      {
        kpiId: 'kpi122',
        kpiName: 'Closure Possible Today',
		kpiSource: 'Jira',
        type: ['Other'],
        fieldNames : { 'Workflow Status Mapping' : ['jiraStatusForInProgress','jiraIterationCompletionStatusCustomField'], 'Custom Fields Mapping' : ['jiraDueDateField'],'Issue Types Mapping' : ['jiraIterationIssuetypeCustomField']}
      },
      {
        kpiId: 'kpi120',
        kpiName: 'Iteration Commitment',
		kpiSource: 'Jira',
        type: ['Other'],
        fieldNames : {'Workflow Status Mapping' : ['jiraIterationCompletionStatusCustomField'],'Issue Types Mapping' : ['jiraIterationIssuetypeCustomField'] }
      },
      {
        kpiId: 'kpi124',
        kpiName: 'Estimation Hygiene',
		kpiSource: 'Jira',
        type: ['Other'],
        fieldNames : { 'Workflow Status Mapping' : ['issueStatusExcluMissingWork','jiraIterationCompletionStatusCustomField'],'Issue Types Mapping' : ['jiraIterationIssuetypeCustomField'] }
      },
      {
        kpiId: 'kpi125',
        kpiName: 'Iteration Burnup',
		kpiSource: 'Jira',
        type: ['Other'],
        fieldNames : {'Workflow Status Mapping' : ['jiraIterationCompletionStatusCustomField'],'Issue Types Mapping' : ['jiraIterationIssuetypeCustomField'] }
      },
	  {
        kpiId: 'kpi128',
        kpiName: 'Planned Work Status',
		kpiSource: 'Jira',
        type: ['Other'],
        fieldNames : {'Workflow Status Mapping' : ['jiraStatusForInProgress', 'jiraDevDoneStatus','jiraIterationCompletionStatusCustomField'],'Issue Types Mapping' : ['jiraIterationIssuetypeCustomField']}
      },
      {
         kpiId: 'kpi145',
         kpiName: 'Dev Completion Status',
      	 kpiSource: 'Jira',
         type: ['Other'],
         fieldNames : {'Workflow Status Mapping' : ['jiraStatusForInProgress', 'jiraDevDoneStatus']}
      },
      {
        kpiId: 'kpi79',
        kpiName: 'Test Cases Without Story Link',
		kpiSource: 'Zypher',
        type: ['Other'],
        fieldNames : { 'Issue Types Mapping' : ['jiraStoryIdentification'], 'Test Cases Mapping' : ['JiraRegressionTestValue', 'testRegressionValue', 'regressionAutomationFolderPath'] }
      },
      {
        kpiId: 'kpi80',
        kpiName: 'Defects Without Story Link',
		kpiSource: 'Jira',
        type: ['Other'],
        fieldNames : { 'Issue Types Mapping' : ['jiraStoryIdentification'], 'Workflow Status Mapping' : ['jiraDefectDroppedStatus'] }
      },
      {
        kpiId: 'kpi127',
        kpiName: 'Production Defects Ageing',
		kpiSource: 'Jira',
        type: ['Other'],
        fieldNames : { 'Workflow Status Mapping' : ['jiraDod', 'jiraLiveStatus', 'jiraDefectDroppedStatus'], 'Defects Mapping' : ['productionDefectIdentifier'] }
      },
      {
        kpiId: 'kpi131',
        kpiName: 'Wastage',
        kpiSource: 'Jira',
        type: ['Other'],
        fieldNames : { 'Workflow Status Mapping' : ['jiraBlockedStatus', 'jiraWaitStatus','jiraIterationCompletionStatusCustomField'],'Issue Types Mapping' : ['jiraIterationIssuetypeCustomField'] }
      },
      {
        kpiId: 'kpi133',
        kpiName: 'Quality Status',
        kpiSource: 'Jira',
        type: ['Other'],
        fieldNames : {'Workflow Status Mapping' : ['resolutionTypeForRejectionQS','jiraDefectRejectionStatusQS','jiraIterationCompletionStatusCustomField'], 'Defects Mapping' : ['defectPriority', 'excludeRCAFromFTPR','jiradefecttype'], 'Issue Types Mapping' : ['jiraIterationIssuetypeCustomField'] }
      },
	  {
        kpiId: 'kpi134',
        kpiName: 'Unplanned Work Status',
		kpiSource: 'Jira',
        type: ['Other'],
        fieldNames : {'Workflow Status Mapping' : ['jiraIterationCompletionStatusCustomField'],'Issue Types Mapping' : ['jiraIterationIssuetypeCustomField'] }
      },
      {
        kpiId: 'kpi136',
        kpiName: 'Defect Count by Status',
        kpiSource: 'Jira',
        type: ['Other'],
        fieldNames : {'Defects Mapping' : ['jiradefecttype'],'Workflow Status Mapping' : ['jiraIterationCompletionStatusCustomField'],'Issue Types Mapping' : ['jiraIterationIssuetypeCustomField'] }
      },
      {
        kpiId: 'kpi137',
        kpiName: 'Defect Reopen Rate',
		kpiSource: 'Jira',
        type: ['Other'],
        fieldNames : { 'Workflow Status Mapping' : ['jiraDefectClosedStatus'] }
      },
      {
        kpiId: 'kpi141',
        kpiName: 'Defect Count by Status (Release)',
		kpiSource: 'Jira',
        type: ['Other'],
        fieldNames : {'Defects Mapping' : ['jiradefecttype'] }
      },
      {
        kpiId: 'kpi142',
        kpiName: 'Defect Count by RCA (Release)',
		kpiSource: 'Jira',
        type: ['Other'],
        fieldNames : {'Defects Mapping' : ['jiradefecttype'] }
      },
      {
        kpiId: 'kpi143',
        kpiName: 'Defect Count by Assignee (Release)',
		kpiSource: 'Jira',
        type: ['Other'],
        fieldNames : {'Defects Mapping' : ['jiradefecttype'] }
      },
      {
        kpiId: 'kpi144',
        kpiName: 'Defect Count by Priority (Release)',
		kpiSource: 'Jira',
        type: ['Other'],
        fieldNames : {'Defects Mapping' : ['jiradefecttype'] }
      },
      {
        kpiId: 'kpi989',
        kpiName: 'Kpi Maturity',
        type: ['Other'],
        fieldNames : { }
      },
      {
        kpiId: 'kpi139',
        kpiName: 'Refinement Rejection Rate',
        kpiSource: 'Jira',
        type: ['Other'],
        fieldNames: {
          'Workflow Status Mapping': [
            'jiraReadyForRefinement',
            'jiraAcceptedInRefinement',
            'jiraRejectedInRefinement'
          ]
        }
      },
      {
              kpiId: 'kpi138',
              kpiName: 'Backlog Readiness Efficiency',
      		kpiSource: 'Jira',
              type: ['Other'],
              fieldNames : {'Issue Types Mapping' : ['jiraSprintVelocityIssueTypeBR'],'Workflow Status Mapping' : ['readyForDevelopmentStatus','jiraIssueDeliverdStatusBR'] }
            },
            {
             kpiId: 'Kpi148',
             kpiName: 'Flow Load',
             kpiSource: 'Jira',
             type: ['Other'],
             fieldNames: {
                 'Workflow Status Mapping': [
                    'storyFirstStatus',
                    'jiraStatusForInProgress',
                    'jiraStatusForQa',
                    'jiraLiveStatus'
                ]
             }
            },
      {
        kpiId: 'Kpi146',
        kpiName: 'Flow Distribution',
        type: ['Other'],
        fieldNames : { }
      }
	  ]);



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
    "fieldName": "jiraIterationIssuetypeCustomField",
    "fieldLabel": "Iteration Board Issue types",
    "fieldType": "chips",
    "fieldCategory": "Issue_Type",
    "section": "Issue Types Mapping",
    "tooltip": {
      "kpiImpacted": "Iteration Dashboard and SPEED KPIs - Sprint Velocity, Commitment Reliability, Issue Count, Sprint Predictability"
    }
  },
  {
    "fieldName": "jiraSprintVelocityIssueTypeKpi39",
    "fieldLabel": "Issue type to be included",
    "fieldType": "chips",
    "fieldCategory": "Issue_Type",
    "section": "Issue Types Mapping",
    "tooltip": {
      "definition": "All issue types that are sized in story points and should be counted in Velocity calculation"
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
    "fieldLabel": "Iteration Board Issue types",
    "fieldType": "chips",
    "fieldCategory": "Issue_Type",
    "section": "Issue Types Mapping",
    "tooltip": {
      "definition": "Issue Types to be considered Completed"
    }
  },
  {
    "fieldName": "jiraIterationIssuetypeKPI128",
    "fieldLabel": "Iteration Board Issue types",
    "fieldType": "chips",
    "fieldCategory": "Issue_Type",
    "section": "Issue Types Mapping",
    "tooltip": {
      "definition": "Issue Types to be considered Completed"
    }
  },
  {
    "fieldName": "jiraIterationIssuetypeKPI134",
    "fieldLabel": "Iteration Board Issue types",
    "fieldType": "chips",
    "fieldCategory": "Issue_Type",
    "section": "Issue Types Mapping",
    "tooltip": {
      "definition": "Issue Types to be considered Completed"
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
    "fieldLabel": "Iteration Board Issue types",
    "fieldType": "chips",
    "fieldCategory": "Issue_Type",
    "section": "Issue Types Mapping",
    "tooltip": {
      "definition": "Issue Types to be considered Completed"
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
    "fieldLabel": "Resolution Type for Rejection",
    "fieldType": "chips",
    "fieldCategory": "workflow",
    "section": "WorkFlow Status Mapping",
    "tooltip": {
      "definition": "Resolution type to identify rejected defects. <br>"
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
    "fieldName": "jiraDorLT",
    "fieldLabel": "Status to Identify Development Status",
    "fieldType": "chips",
    "fieldCategory": "workflow",
    "section": "WorkFlow Status Mapping",
    "tooltip": {
      "definition": " Definition of Readiness. Provide any status from workflow on which DOR is considered. <br>Example: In Sprint<hr>KPI Impacted:Lead Time - Intake to DOR and DOR to DOD"
    }
  },
  {
    "fieldName": "jiraIssueTypeLT",
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
    "fieldLabel": "DOD Status",
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
    "fieldName": "jiraDodLT",
    "fieldLabel": "DOD Status",
    "fieldType": "chips",
    "fieldCategory": "workflow",
    "section": "WorkFlow Status Mapping",
    "tooltip": {
      "definition": " Definition of Doneness. Provide any status from workflow on which DOD is considered. <br> Example: In Testing <hr>"
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
    "fieldLabel": "Issue Types to be fetched from Jira",
    "fieldType": "chips",
    "fieldCategory": "Issue_Type",
    "processorCommon":true,
    "section": "Issue Types Mapping",
    "tooltip": {
      "definition": "Issue Type in Jira. These issue type are fetched in PsKnowHow dashboard. <br> Example : Story, Defect, Risk, Change Request, Test<hr>"
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
      "fieldLabel": "Iteration Dashboard & SPEED KPIs Completion Status",
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
      "fieldLabel": "Iteration Dashboard & SPEED KPIs Completion Status",
      "fieldType": "chips",
      "fieldCategory": "workflow",
      "section": "WorkFlow Status Mapping",
      "tooltip": {
        "definition": "Status to identify as closed"
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
      "fieldLabel": "Iteration Dashboard & SPEED KPIs Completion Status",
      "fieldType": "chips",
      "fieldCategory": "workflow",
      "section": "WorkFlow Status Mapping",
      "tooltip": {
        "definition": "Status to identify as closed"
      }
    },
  {
      "fieldName": "jiraIterationCompletionStatusKPI128",
      "fieldLabel": "Iteration Dashboard & SPEED KPIs Completion Status",
      "fieldType": "chips",
      "fieldCategory": "workflow",
      "section": "WorkFlow Status Mapping",
      "tooltip": {
        "definition": "Status to identify as closed"
      }
    },
  {
      "fieldName": "jiraIterationCompletionStatusKPI134",
      "fieldLabel": "Iteration Dashboard & SPEED KPIs Completion Status",
      "fieldType": "chips",
      "fieldCategory": "workflow",
      "section": "WorkFlow Status Mapping",
      "tooltip": {
        "definition": "Status to identify as closed"
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
      "fieldLabel": "Iteration Dashboard & SPEED KPIs Completion Status",
      "fieldType": "chips",
      "fieldCategory": "workflow",
      "section": "WorkFlow Status Mapping",
      "tooltip": {
        "definition": "Status to identify as closed"
      }
    },
    {
          "fieldName": "jiraWaitStatusKPI131",
          "fieldLabel": "Wastage - Wait Status",
          "fieldType": "chips",
          "fieldCategory": "workflow",
          "section": "WorkFlow Status Mapping",
          "tooltip": {
            "definition": "The statuses wherein no activity takes place and signify that the issues needs to move to picked up by a team member like Ready for deployment, Ready for testing etc"
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
    "fieldLabel": "Live Status - Lead Time",
    "fieldType": "text",
    "fieldCategory": "workflow",
    "section": "WorkFlow Status Mapping",
    "tooltip": {
      "definition": "Provide any status from workflow on which Live is considered. <br>Example: Live<hr>"
    }
  },
  {
      "fieldName": "jiraLiveStatusLT",
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
    "fieldName": "jiradefecttypeBDRR",
    "fieldLabel": "Issue Type to Identify Defect",
    "fieldType": "chips",
    "fieldCategory": "Issue_Type",
    "section": "Defects Mapping",
    "processorCommon":true,
    "tooltip": {
      "definition": "In JIRA/AZURE a defect can be defined as Bug, Defect, Snag or any other value. So user need to provide value with which defect is identified in JIRA/AZURE.<hr>"
    }
  },
  {
    "fieldName": "jiradefecttypeKPI126",
    "fieldLabel": "Issue type to be included",
    "fieldType": "chips",
    "fieldCategory": "Issue_Type",
    "section": "Defects Mapping",
    "processorCommon":true,
    "tooltip": {
      "definition": "Issue types that are considered as defects in Jira."
    }
  },
  {
    "fieldName": "jiradefecttypeKpi40",
    "fieldLabel": "Status to identify defects",
    "fieldType": "chips",
    "fieldCategory": "Issue_Type",
    "section": "Defects Mapping",
    "processorCommon":true,
    "tooltip": {
      "definition": "Issue types that are considered as defects in Jira."
    }
  },
  {
    "fieldName": "jiradefecttypeKPI135",
    "fieldLabel": "Issue type to identify defects",
    "fieldType": "chips",
    "fieldCategory": "Issue_Type",
    "section": "Defects Mapping",
    "processorCommon":true,
    "tooltip": {
      "definition": "Issue types that are considered as Defects in Jira"
    }
  },
  {
    "fieldName": "jiradefecttypeKPI82",
    "fieldLabel": "Issue Type to Identify Defect",
    "fieldType": "chips",
    "fieldCategory": "Issue_Type",
    "section": "Defects Mapping",
    "processorCommon":true,
    "tooltip": {
      "definition": "Issue types that are considered as defects in Jira"
    }
  },
  {
    "fieldName": "jiradefecttypeMW",
    "fieldLabel": "Issue Type to Identify Defect",
    "fieldType": "chips",
    "fieldCategory": "Issue_Type",
    "section": "Defects Mapping",
    "processorCommon":true,
    "tooltip": {
      "definition": "In JIRA/AZURE a defect can be defined as Bug, Defect, Snag or any other value. So user need to provide value with which defect is identified in JIRA/AZURE.<hr>"
    }
  },
  {
    "fieldName": "jiradefecttypeLT",
    "fieldLabel": "Issue Type to Identify Defect",
    "fieldType": "chips",
    "fieldCategory": "Issue_Type",
    "section": "Defects Mapping",
    "processorCommon":true,
    "tooltip": {
      "definition": "In JIRA/AZURE a defect can be defined as Bug, Defect, Snag or any other value. So user need to provide value with which defect is identified in JIRA/AZURE.<hr>"
    }
  },
  {
    "fieldName": "jiradefecttypeIWS",
    "fieldLabel": "Issue Type to Identify Defect",
    "fieldType": "chips",
    "fieldCategory": "Issue_Type",
    "section": "Defects Mapping",
    "processorCommon":true,
    "tooltip": {
      "definition": "In JIRA/AZURE a defect can be defined as Bug, Defect, Snag or any other value. So user need to provide value with which defect is identified in JIRA/AZURE.<hr>"
    }
  },
  {
    "fieldName": "jiradefecttypeKPI133",
    "fieldLabel": "Issue type to be identify defect",
    "fieldType": "chips",
    "fieldCategory": "Issue_Type",
    "section": "Defects Mapping",
    "processorCommon":true,
    "tooltip": {
      "definition": "Issue types that are considered as defects in Jira"
    }
  },
  {
    "fieldName": "jiradefecttypeRDCS",
    "fieldLabel": "Issue Type to Identify Defect",
    "fieldType": "chips",
    "fieldCategory": "Issue_Type",
    "section": "Defects Mapping",
    "processorCommon":true,
    "tooltip": {
      "definition": "In JIRA/AZURE a defect can be defined as Bug, Defect, Snag or any other value. So user need to provide value with which defect is identified in JIRA/AZURE.<hr>"
    }
  },
  {
    "fieldName": "jiradefecttypeRDCR",
    "fieldLabel": "Issue Type to Identify Defect",
    "fieldType": "chips",
    "fieldCategory": "Issue_Type",
    "section": "Defects Mapping",
    "processorCommon":true,
    "tooltip": {
      "definition": "In JIRA/AZURE a defect can be defined as Bug, Defect, Snag or any other value. So user need to provide value with which defect is identified in JIRA/AZURE.<hr>"
    }
  },
  {
    "fieldName": "jiradefecttypeRDCP",
    "fieldLabel": "Issue Type to Identify Defect",
    "fieldType": "chips",
    "fieldCategory": "Issue_Type",
    "section": "Defects Mapping",
    "processorCommon":true,
    "tooltip": {
      "definition": "In JIRA/AZURE a defect can be defined as Bug, Defect, Snag or any other value. So user need to provide value with which defect is identified in JIRA/AZURE.<hr>"
    }
  },
  {
    "fieldName": "jiradefecttypeRDCA",
    "fieldLabel": "Issue Type to Identify Defect",
    "fieldType": "chips",
    "fieldCategory": "Issue_Type",
    "section": "Defects Mapping",
    "processorCommon":true,
    "tooltip": {
      "definition": "In JIRA/AZURE a defect can be defined as Bug, Defect, Snag or any other value. So user need to provide value with which defect is identified in JIRA/AZURE.<hr>"
    }
  },
  {
    "fieldName": "jiradefecttypeKPI140",
    "fieldLabel": "Issue Type to Identify Defect",
    "fieldType": "chips",
    "fieldCategory": "Issue_Type",
    "section": "Defects Mapping",
    "processorCommon":true,
    "tooltip": {
      "definition": "In JIRA/AZURE a defect can be defined as Bug, Defect, Snag or any other value. So user need to provide value with which defect is identified in JIRA/AZURE.<hr>"
    }
  },
  {
    "fieldName": "jiradefecttypeKPI136",
    "fieldLabel": "Issue Type to Identify Defect",
    "fieldType": "chips",
    "fieldCategory": "Issue_Type",
    "section": "Defects Mapping",
    "processorCommon":true,
    "tooltip": {
      "definition": "Issue types that are considered as Defects in Jira"
    }
  },
  {
    "fieldName": "jiradefecttypeSWE",
    "fieldLabel": "Issue Type to Identify Defect",
    "fieldType": "chips",
    "fieldCategory": "Issue_Type",
    "section": "Defects Mapping",
    "processorCommon":true,
    "tooltip": {
      "definition": "In JIRA/AZURE a defect can be defined as Bug, Defect, Snag or any other value. So user need to provide value with which defect is identified in JIRA/AZURE.<hr>"
    }
  },
  {
    "fieldName": "jiradefecttypeKPI132",
    "fieldLabel": "Issue type to identify defects",
    "fieldType": "chips",
    "fieldCategory": "Issue_Type",
    "section": "Defects Mapping",
    "processorCommon":true,
    "tooltip": {
      "definition": "Issue types that are considered as Defects in Jira"
    }
  },
  {
    "fieldName": "jiradefecttype",
    "fieldLabel": "Issue Type to Identify Defect",
    "fieldType": "chips",
    "fieldCategory": "Issue_Type",
    "section": "Defects Mapping",
    "processorCommon":true,
    "tooltip": {
      "definition": "In JIRA/AZURE a defect can be defined as Bug, Defect, Snag or any other value. So user need to provide value with which defect is identified in JIRA/AZURE.<hr>"
    }
  },
{
    "fieldName": "jiraStoryPointsCustomField",
    "fieldLabel": "Estimation",
    "fieldType": "text",
    "fieldCategory": "custom",
    "section": "Custom Fields Mapping",
    "tooltip": {
      "definition": "JIRA/AZURE applications let you add custom fields in addition to the built-in fields.Story Point is a custom field in JIRA. So User need to provide that custom field which is associated with Story point in Users JIRA/AZURE Installation. <br>Example : customfield_20803."
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
    "fieldLabel": "Epic Cost of Delay",
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
    "fieldLabel": "Epic Risk Reduction",
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
    "fieldLabel": "Epic Business Value",
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
    "fieldLabel": "Epic WSJF",
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
    "fieldLabel": "Epic Time Criticality",
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
    "fieldLabel": "Epic Job Size",
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
              "definition": "Conversion factor for Story Point to Hour Conversion. <br>Example: If 1 Story Point is 8 hrs, enter 8."
            }
          }
        ]
},
{
    "fieldName": "jiraIncludeBlockedStatus131",
    "fieldLabel": "Wastage - Blocked Issues Criteria ",
    "fieldType": "radiobutton",
	"fieldCategory": "workflow",
    "section": "WorkFlow Status Mapping",
    "tooltip": {
          "definition": "The statuses that signify that team is unable to proceed on an issue due to internal
          or external dependency like On Hold, Waiting for user response, Blocked etc should be included."
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
            "fieldName": "jiraBlockedStatusIW",
            "fieldLabel": "Status to Identify 'Blocked' status ",
            "fieldType": "chips",
            "filterGroup": ["Blocked Status"],
            "tooltip": {
              "definition": "Provide Status to Identify Blocked Issues<br />
                                        Example: On_Hold <hr>
                                        KPI Impacted : Iteration Board - Wastage KPI"
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
            "fieldType": "text",
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
            "fieldName": "jiraBugRaisedByValue",
            "fieldLabel": "UAT Defect Values",
            "fieldType": "text",
            "filterGroup": ["CustomField","Labels"],
            "tooltip": {
              "definition": "Provide label name to identify UAT or client raised defects.<br /> Example: Clone_by_QA <hr>"
            }
          },
          {
            "fieldName": "jiraBugRaisedByCustomField",
            "fieldLabel": "UAT Defect Custom Field",
            "fieldType": "text",
            "fieldCategory": "custom",
            "filterGroup": ["CustomField"],
            "tooltip": {
              "definition": "Provide customfield name to identify UAT or client raised defects. <br> Example: customfield_13907<hr>"
            }
          }
        ]
},
{
    "fieldName": "additionalFilterConfig",
    "fieldLabel": "Additional Filter Identifier",
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
    "fieldLabel": "Status to identify In Progress",
     "fieldType": "chips",
    "fieldCategory": "workflow",
     "section": "WorkFlow Status Mapping",
     "tooltip": {
          "definition": "This field should consider all status that are considered In Progress in Jira for e.g. Analysis, development, code review, testing etc <br>",
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
    "fieldLabel": "Dev Completion Status",
     "fieldType": "chips",
    "fieldCategory": "workflow",
     "section": "WorkFlow Status Mapping",
     "tooltip": {
          "definition": "This status identifies when Development is completed for an issue<br>
                                                                        ",
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
}



]
);