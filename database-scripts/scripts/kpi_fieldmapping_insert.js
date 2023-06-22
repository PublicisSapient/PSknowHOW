db.getCollection('kpi_fieldmapping').remove({});
db.getCollection('kpi_fieldmapping').insert(
[
{
        kpiId: 'kpi14',
        kpiName: 'Defect Injection Rate',
		kpiSource:'Jira',
        type: ['Scrum'],
        fieldNames :  {'Workflow Status Mapping' : ['jiraDod', 'jiraDefectCreatedStatus', 'jiraDefectDroppedStatus','resolutionTypeForRejectionDIR','jiraDefectRejectionStatusDIR'], 'Issue Types Mapping' : ['jiraDefectInjectionIssueType'], 'Defects Mapping' : ['defectPriority', 'excludeRCAFromFTPR'] }
      },
      {
        kpiId: 'kpi82',
        kpiName: 'First Time Pass Rate',
		kpiSource:'Jira',
        type: ['Scrum'],
        fieldNames : {'Workflow Status Mapping' : ['resolutionTypeForRejectionFTPR','jiraIssueDeliverdStatusFTPR','jiraDefectRejectionStatusFTPR','jiraIterationCompletionStatusCustomField','jiraStatusForQa','jiraStatusForDevelopment','jiraFtprRejectStatus'], 'Defects Mapping' : ['defectPriority', 'excludeRCAFromFTPR'] ,'Issue Types Mapping' : ['jiraIterationCompletionTypeCustomField','jiraFTPRStoryIdentification']}
        },
        {
                kpiId: 'kpi135',
                kpiName: 'First Time Pass Rate',
        		kpiSource:'Jira',
                type: ['Scrum'],
                fieldNames : {'Workflow Status Mapping' : ['resolutionTypeForRejectionIFTPR','jiraIssueDeliverdStatusFTPR','jiraDefectRejectionStatusIFTPR','jiraIterationCompletionStatusCustomField','jiraStatusForQa','jiraStatusForDevelopment','jiraFtprRejectStatus'], 'Defects Mapping' : ['defectPriority', 'excludeRCAFromFTPR'] ,'Issue Types Mapping' : ['jiraIterationCompletionTypeCustomField','jiraFTPRStoryIdentification']}
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
        fieldNames : {'Workflow Status Mapping' : ['jiraIterationCompletionStatusCustomField','jiraIssueDeliverdStatusCVR'],'Issue Types Mapping' : ['jiraIterationCompletionTypeCustomField'] }
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
        fieldNames : {'Issue Types Mapping' : ['jiraStoryIdentificationIC','jiraIterationCompletionTypeCustomField'] ,'Workflow Status Mapping' : ['jiraIterationCompletionStatusCustomField'], 'Defects Mapping' : ['jiradefecttype']}
      },
      {
        kpiId: 'kpi72',
        kpiName: 'Commitment Reliability',
		kpiSource: 'Jira',
        type: ['Scrum'],
        fieldNames : { 'Issue Types Mapping' : ['jiraIterationCompletionTypeCustomField'], 'Workflow Status Mapping' : ['jiraIterationCompletionStatusCustomField'], 'Custom Fields Mapping' : ['estimationCriteria', 'storyPointToHourMapping', 'jiraStoryPointsCustomField'] }
      },
      {
        kpiId: 'kpi39',
        kpiName: 'Sprint Velocity',
		kpiSource: 'Jira',
        type: ['Scrum'],
        fieldNames : {'Issue Types Mapping' : ['jiraSprintVelocityIssueTypeSV','jiraIterationCompletionTypeCustomField'], 'Custom Fields Mapping' : ['estimationCriteria', 'storyPointToHourMapping', 'jiraStoryPointsCustomField'] ,'Workflow Status Mapping' : ['jiraIssueDeliverdStatusSV','jiraIterationCompletionStatusCustomField']}
      },
      {
        kpiId: 'kpi46',
        kpiName: 'Sprint Capacity Utilization',
		kpiSource: 'Jira',
        type: ['Scrum'],
        fieldNames : {'Issue Types Mapping' : ['jiraSprintCapacityIssueType'] }
      },
      {
        kpiId: 'kpi83',
        kpiName: 'Average Resolution Time',
		kpiSource: 'Jira',
        type: ['Scrum'],
        fieldNames : {'Workflow Status Mapping' : ['resolutionTypeForRejectionAVR','jiraIssueDeliverdStatusAVR','jiraStatusForDevelopment','jiraDefectRejectionStatusAVR'], 'Issue Types Mapping' : ['jiraIssueTypeNames'], 'Defects Mapping' : ['jiradefecttype'] }
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
        fieldNames : {'Issue Types Mapping' : ['jiraIterationCompletionTypeCustomField'], 'Workflow Status Mapping' : ['jiraIterationCompletionStatusCustomField'], 'Custom Fields Mapping' : ['estimationCriteria', 'storyPointToHourMapping', 'jiraStoryPointsCustomField'] }
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
        fieldNames : { 'Workflow Status Mapping' : ['jiraStatusForInProgress', 'jiraDevDoneStatus','jiraIterationCompletionStatusCustomField'], 'Issue Types Mapping' : ['jiraIterationCompletionTypeCustomField'],'Custom Fields Mapping' : ['jiraDueDateField']}
      },
      {
        kpiId: 'kpi75',
        kpiName: 'Estimate vs Actual',
		kpiSource: 'Jira',
        type: ['Other'],
        fieldNames : { 'Workflow Status Mapping' : ['jiraIterationCompletionStatusCustomField'], 'Issue Types Mapping' : ['jiraIterationCompletionTypeCustomField']}
      },
      {
        kpiId: 'kpi123',
        kpiName: 'Issues likely to Spill',
		kpiSource: 'Jira',
        type: ['Other'],
        fieldNames : { 'Workflow Status Mapping' : ['jiraStatusForInProgress','jiraIterationCompletionStatusCustomField'], 'Custom Fields Mapping' : ['jiraDueDateField'],'Issue Types Mapping' : ['jiraIterationCompletionTypeCustomField']}
      },
      {
        kpiId: 'kpi122',
        kpiName: 'Closure Possible Today',
		kpiSource: 'Jira',
        type: ['Other'],
        fieldNames : { 'Workflow Status Mapping' : ['jiraStatusForInProgress','jiraIterationCompletionStatusCustomField'], 'Custom Fields Mapping' : ['jiraDueDateField'],'Issue Types Mapping' : ['jiraIterationCompletionTypeCustomField']}
      },
      {
        kpiId: 'kpi120',
        kpiName: 'Iteration Commitment',
		kpiSource: 'Jira',
        type: ['Other'],
        fieldNames : {'Workflow Status Mapping' : ['jiraIterationCompletionStatusCustomField'],'Issue Types Mapping' : ['jiraIterationCompletionTypeCustomField'] }
      },
      {
        kpiId: 'kpi124',
        kpiName: 'Estimation Hygiene',
		kpiSource: 'Jira',
        type: ['Other'],
        fieldNames : { 'Workflow Status Mapping' : ['issueStatusExcluMissingWork','jiraIterationCompletionStatusCustomField'],'Issue Types Mapping' : ['jiraIterationCompletionTypeCustomField'] }
      },
      {
        kpiId: 'kpi125',
        kpiName: 'Iteration Burnup',
		kpiSource: 'Jira',
        type: ['Other'],
        fieldNames : {'Workflow Status Mapping' : ['jiraIterationCompletionStatusCustomField'],'Issue Types Mapping' : ['jiraIterationCompletionTypeCustomField'] }
      },
	  {
        kpiId: 'kpi128',
        kpiName: 'Planned Work Status',
		kpiSource: 'Jira',
        type: ['Other'],
        fieldNames : {'Workflow Status Mapping' : ['jiraStatusForInProgress', 'jiraDevDoneStatus','jiraIterationCompletionStatusCustomField'],'Issue Types Mapping' : ['jiraIterationCompletionTypeCustomField']}
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
        fieldNames : { 'Workflow Status Mapping' : ['jiraBlockedStatus', 'jiraWaitStatus','jiraIterationCompletionStatusCustomField'],'Issue Types Mapping' : ['jiraIterationCompletionTypeCustomField'] }
      },
      {
        kpiId: 'kpi133',
        kpiName: 'Quality Status',
        kpiSource: 'Jira',
        type: ['Other'],
        fieldNames : {'Workflow Status Mapping' : ['resolutionTypeForRejectionQS','jiraDefectRejectionStatusQS','jiraIterationCompletionStatusCustomField'], 'Defects Mapping' : ['defectPriority', 'excludeRCAFromFTPR','jiradefecttype'], 'Issue Types Mapping' : ['jiraIterationCompletionTypeCustomField'] }
      },
	  {
        kpiId: 'kpi134',
        kpiName: 'Unplanned Work Status',
		kpiSource: 'Jira',
        type: ['Other'],
        fieldNames : {'Workflow Status Mapping' : ['jiraIterationCompletionStatusCustomField'],'Issue Types Mapping' : ['jiraIterationCompletionTypeCustomField'] }
      },
      {
        kpiId: 'kpi136',
        kpiName: 'Defect Count by Status',
        kpiSource: 'Jira',
        type: ['Other'],
        fieldNames : {'Defects Mapping' : ['jiradefecttype'],'Workflow Status Mapping' : ['jiraIterationCompletionStatusCustomField'],'Issue Types Mapping' : ['jiraIterationCompletionTypeCustomField'] }
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
             type: ['Other'],
             fieldNames : { }
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
    "fieldName": "jiraStoryIdentificationIC",
    "fieldLabel": "Issue Count KPI Issue type",
    "fieldType": "chips",
    "fieldCategory": "Issue_Type",
    "section": "Issue Types Mapping",
    "tooltip": {
      "definition": "Value to identify kind of stories which are used for identification for story count.",
      "kpiImpacted": "Issue Count Kpi"
    }
  },
  {
    "fieldName": "jiradefecttype",
    "fieldLabel": "Issue Type to Identify Defect",
    "fieldType": "chips",
    "fieldCategory": "Issue_Type",
    "section": "Defect Mapping",
    "tooltip": {
      "definition": "In JIRA/AZURE a defect can be defined as or any other value. So user need to provide value with which defect is identified in JIRA/AZURE",
      "kpiImpacted": "Jira/Azure Collector and KPIs"
    }
  },
  {
    "fieldName": "jiraIterationCompletionTypeCustomField",
    "fieldLabel": "Iteration Board Issue types",
    "fieldType": "chips",
    "fieldCategory": "Issue_Type",
    "section": "Issue Types Mapping",
    "tooltip": {
      "kpiImpacted": "Iteration Dashboard and SPEED KPIs - Sprint Velocity, Commitment Reliability, Issue Count, Sprint Predictability"
    }
  },
  {
    "fieldName": "jiraIterationCompletionStatusCustomField",
    "fieldLabel": "Iteration Dashboard & SPEED KPIs Completion Status",
    "fieldType": "chips",
    "fieldCategory": "workflow",
    "section": "WorkFlow Status Mapping",
    "tooltip": {
      "kpiImpacted": "Iteration Dashboard and SPEED KPIs - Sprint Velocity, Commitment Reliability, Issue Count, Sprint Predictability"
    }
  },
  {
    "fieldName": "jiraSprintVelocityIssueTypeSV",
    "fieldLabel": "Sprint Velocity - Issue Types with Linked Defect",
    "fieldType": "chips",
    "fieldCategory": "Issue_Type",
    "section": "Issue Types Mapping",
    "tooltip": {
    "definition":"All issue types with which defect is linked. <br>  Example: Story, Change Request .<hr>"
    }
  },
  {
      "fieldName": "jiraSprintCapacityIssueType",
      "fieldLabel": "Sprint Capacity Issue Type",
      "fieldType": "chips",
      "fieldCategory": "Issue_Type",
      "section": "Issue Types Mapping",
      "tooltip": {
      "definition":"Value to identify kind of stories which are used for identification for Sprint Capacity.<br /> Example: Story<hr>"
      }
    },
  {
      "fieldName": "jiraSprintVelocityIssueTypeBR",
      "fieldLabel": "Sprint Velocity - Issue Types with Linked Defect",
      "fieldType": "chips",
      "fieldCategory": "Issue_Type",
      "section": "Issue Types Mapping",
      "tooltip": {
      "definition":"All issue types with which defect is linked. <br>  Example: Story, Change Request .<hr>"
      }
    },
 {
    "fieldName": "jiraIssueDeliverdStatusSV",
    "fieldLabel": "Issue Delivered Status",
    "fieldType": "chips",
    "fieldCategory": "workflow",
    "section": "WorkFlow Status Mapping",
    "tooltip": {
    "definition":"Status from workflow on which issue is delivered. <br> Example: Closed<hr>"
    }
  },
 {
    "fieldName": "jiraIssueDeliverdStatusBR",
    "fieldLabel": "Issue Delivered Status",
    "fieldType": "chips",
    "fieldCategory": "workflow",
    "section": "WorkFlow Status Mapping",
    "tooltip": {
    "definition":"Status from workflow on which issue is delivered. <br> Example: Closed<hr>"
    }
  },
 {
    "fieldName": "jiraIssueDeliverdStatusCVR",
    "fieldLabel": "Issue Delivered Status",
    "fieldType": "chips",
    "fieldCategory": "workflow",
    "section": "WorkFlow Status Mapping",
    "tooltip": {
    "definition":"Status from workflow on which issue is delivered. <br> Example: Closed<hr>"
    }
  },
 {
    "fieldName": "jiraIssueDeliverdStatusFTPR",
    "fieldLabel": "Issue Delivered Status",
    "fieldType": "chips",
    "fieldCategory": "workflow",
    "section": "WorkFlow Status Mapping",
    "tooltip": {
    "definition":"Status from workflow on which issue is delivered. <br> Example: Closed<hr>"
    }
  },
 {
    "fieldName": "jiraIssueDeliverdStatusAVR",
    "fieldLabel": "Issue Delivered Status",
    "fieldType": "chips",
    "fieldCategory": "workflow",
    "section": "WorkFlow Status Mapping",
    "tooltip": {
    "definition":"Status from workflow on which issue is delivered. <br> Example: Closed<hr>"
    }
  },
   {
      "fieldName": "resolutionTypeForRejectionAVR",
      "fieldLabel": "Resolution Type for Rejection",
      "fieldType": "chips",
      "fieldCategory": "workflow",
      "section": "WorkFlow Status Mapping",
      "tooltip": {
      "definition":"Resolution type to identify rejected defects. <br>"
      }
    },
    {
          "fieldName": "resolutionTypeForRejectionDC",
          "fieldLabel": "Resolution Type for Rejection",
          "fieldType": "chips",
          "fieldCategory": "workflow",
          "section": "WorkFlow Status Mapping",
          "tooltip": {
          "definition":"Resolution type to identify rejected defects. <br>"
          }
        },
{
          "fieldName": "resolutionTypeForRejectionDRE",
          "fieldLabel": "Resolution Type for Rejection",
          "fieldType": "chips",
          "fieldCategory": "workflow",
          "section": "WorkFlow Status Mapping",
          "tooltip": {
          "definition":"Resolution type to identify rejected defects. <br>"
          }
        },
{
          "fieldName": "resolutionTypeForRejectionDRR",
          "fieldLabel": "Resolution Type for Rejection",
          "fieldType": "chips",
          "fieldCategory": "workflow",
          "section": "WorkFlow Status Mapping",
          "tooltip": {
          "definition":"Resolution type to identify rejected defects. <br>"
          }
        },
{
          "fieldName": "resolutionTypeForRejectionDSR",
          "fieldLabel": "Resolution Type for Rejection",
          "fieldType": "chips",
          "fieldCategory": "workflow",
          "section": "WorkFlow Status Mapping",
          "tooltip": {
          "definition":"Resolution type to identify rejected defects. <br>"
          }
        },
{
          "fieldName": "resolutionTypeForRejectionFTPR",
          "fieldLabel": "Resolution Type for Rejection",
          "fieldType": "chips",
          "fieldCategory": "workflow",
          "section": "WorkFlow Status Mapping",
          "tooltip": {
          "definition":"Resolution type to identify rejected defects. <br>"
          }
        },
{
          "fieldName": "resolutionTypeForRejectionIFTPR",
          "fieldLabel": "Resolution Type for Rejection",
          "fieldType": "chips",
          "fieldCategory": "workflow",
          "section": "WorkFlow Status Mapping",
          "tooltip": {
          "definition":"Resolution type to identify rejected defects. <br>"
          }
        },
{
          "fieldName": "resolutionTypeForRejectionQS",
          "fieldLabel": "Resolution Type for Rejection",
          "fieldType": "chips",
          "fieldCategory": "workflow",
          "section": "WorkFlow Status Mapping",
          "tooltip": {
          "definition":"Resolution type to identify rejected defects. <br>"
          }
        },
{
          "fieldName": "resolutionTypeForRejectionRCA",
          "fieldLabel": "Resolution Type for Rejection",
          "fieldType": "chips",
          "fieldCategory": "workflow",
          "section": "WorkFlow Status Mapping",
          "tooltip": {
          "definition":"Resolution type to identify rejected defects. <br>"
          }
        },
{
          "fieldName": "resolutionTypeForRejectionDIR",
          "fieldLabel": "Resolution Type for Rejection",
          "fieldType": "chips",
          "fieldCategory": "workflow",
          "section": "WorkFlow Status Mapping",
          "tooltip": {
          "definition":"Resolution type to identify rejected defects. <br>"
          }
        },




//REJECTION STATUS
    {
          "fieldName": "jiraDefectRejectionStatusAVR",
          "fieldLabel": "Defect Rejection Status",
          "fieldType": "chips",
          "fieldCategory": "workflow",
          "section": "WorkFlow Status Mapping",
          "tooltip": {
          "definition":"Status from workflow on which defect is considered as rejected. <br>Example: Cancelled<hr>"
          }
        },
          {
                  "fieldName": "jiraDefectRejectionStatusDC",
                  "fieldLabel": "Defect Rejection Status",
                  "fieldType": "chips",
                  "fieldCategory": "workflow",
                  "section": "WorkFlow Status Mapping",
                  "tooltip": {
                  "definition":"Status from workflow on which defect is considered as rejected. <br>Example: Cancelled<hr>"
                  }
                },
{
                  "fieldName": "jiraDefectRejectionStatusDRE",
                  "fieldLabel": "Defect Rejection Status",
                  "fieldType": "chips",
                  "fieldCategory": "workflow",
                  "section": "WorkFlow Status Mapping",
                  "tooltip": {
                  "definition":"Status from workflow on which defect is considered as rejected. <br>Example: Cancelled<hr>"
                  }
                },
{
                  "fieldName": "jiraDefectRejectionStatusDRR",
                  "fieldLabel": "Defect Rejection Status",
                  "fieldType": "chips",
                  "fieldCategory": "workflow",
                  "section": "WorkFlow Status Mapping",
                  "tooltip": {
                  "definition":"Status from workflow on which defect is considered as rejected. <br>Example: Cancelled<hr>"
                  }
                },
{
                  "fieldName": "jiraDefectRejectionStatusDSR",
                  "fieldLabel": "Defect Rejection Status",
                  "fieldType": "chips",
                  "fieldCategory": "workflow",
                  "section": "WorkFlow Status Mapping",
                  "tooltip": {
                  "definition":"Status from workflow on which defect is considered as rejected. <br>Example: Cancelled<hr>"
                  }
                },
{
                  "fieldName": "jiraDefectRejectionStatusFTPR",
                  "fieldLabel": "Defect Rejection Status",
                  "fieldType": "chips",
                  "fieldCategory": "workflow",
                  "section": "WorkFlow Status Mapping",
                  "tooltip": {
                  "definition":"Status from workflow on which defect is considered as rejected. <br>Example: Cancelled<hr>"
                  }
                },
{
                  "fieldName": "jiraDefectRejectionStatusIFTPR",
                  "fieldLabel": "Defect Rejection Status",
                  "fieldType": "chips",
                  "fieldCategory": "workflow",
                  "section": "WorkFlow Status Mapping",
                  "tooltip": {
                  "definition":"Status from workflow on which defect is considered as rejected. <br>Example: Cancelled<hr>"
                  }
                },
{
                  "fieldName": "jiraDefectRejectionStatusQS",
                  "fieldLabel": "Defect Rejection Status",
                  "fieldType": "chips",
                  "fieldCategory": "workflow",
                  "section": "WorkFlow Status Mapping",
                  "tooltip": {
                  "definition":"Status from workflow on which defect is considered as rejected. <br>Example: Cancelled<hr>"
                  }
                },
{
                  "fieldName": "jiraDefectRejectionStatusRCA",
                  "fieldLabel": "Defect Rejection Status",
                  "fieldType": "chips",
                  "fieldCategory": "workflow",
                  "section": "WorkFlow Status Mapping",
                  "tooltip": {
                  "definition":"Status from workflow on which defect is considered as rejected. <br>Example: Cancelled<hr>"
                  }
                },
{
                  "fieldName": "jiraDefectRejectionStatusDIR",
                  "fieldLabel": "Defect Rejection Status",
                  "fieldType": "chips",
                  "fieldCategory": "workflow",
                  "section": "WorkFlow Status Mapping",
                  "tooltip": {
                  "definition":"Status from workflow on which defect is considered as rejected. <br>Example: Cancelled<hr>"
                  }
                },
{
                  "fieldName": "jiraStatusForDevelopmentAVR",
                  "fieldLabel": "Status to Identify Development Status",
                  "fieldType": "chips",
                  "fieldCategory": "workflow",
                  "section": "WorkFlow Status Mapping",
                  "tooltip": {
                  "definition":"The status of Story Issue Type which identifies the In-Development status in JIRA. <br> Example: In Development<hr>"
                  }
                },
{
                  "fieldName": "jiraStatusForDevelopmentFTPR",
                  "fieldLabel": "Status to Identify Development Status",
                  "fieldType": "chips",
                  "fieldCategory": "workflow",
                  "section": "WorkFlow Status Mapping",
                  "tooltip": {
                  "definition":"The status of Story Issue Type which identifies the In-Development status in JIRA. <br> Example: In Development<hr>"
                  }
                },
                {
                                  "fieldName": "jiraStatusForDevelopmentIFTPR",
                                  "fieldLabel": "Status to Identify Development Status",
                                  "fieldType": "chips",
                                  "fieldCategory": "workflow",
                                  "section": "WorkFlow Status Mapping",
                                  "tooltip": {
                                  "definition":"The status of Story Issue Type which identifies the In-Development status in JIRA. <br> Example: In Development<hr>"
                                  }
                                },
 {
                                  "fieldName": "jiraDORLT",
                                  "fieldLabel": "Status to Identify Development Status",
                                  "fieldType": "chips",
                                  "fieldCategory": "workflow",
                                  "section": "WorkFlow Status Mapping",
                                  "tooltip": {
                                  "definition":" Definition of Readiness. Provide any status from workflow on which DOR is considered. <br>Example: In Sprint<hr>KPI Impacted:Lead Time - Intake to DOR and DOR to DOD"
                                  }
                                },
 {
                                  "fieldName": "jiraIntakeToDorIssueTypeLT",
                                  "fieldLabel": "Lead time issue type",
                                  "fieldType": "chips",
                                  "fieldCategory": "Issue_Type",
                                  "section": "Issue Types Mapping",
                                  "tooltip": {
                                  "definition":"The issue type which is to be considered while calculating lead time KPIs, i.e. intake to DOR and DOR and DOD ... <br> Example: Story, Change Request <hr>"
                                  }
                                },
{
                                  "fieldName": "jiraFTPRStoryIdentification",
                                  "fieldLabel": "Issues types included in FTPR",
                                  "fieldType": "chips",
                                  "fieldCategory": "Issue_Type",
                                  "section": "Issue Types Mapping",
                                  "tooltip": {
                                  "definition":"Issue types that are required to be tracked for First time pass rate (FTPR).<br> Example: Story or equivalent <hr>"
                                  }
                                },
{
                                  "fieldName": "jiraIFTPRStoryIdentification",
                                  "fieldLabel": "Issues types included in FTPR",
                                  "fieldType": "chips",
                                  "fieldCategory": "Issue_Type",
                                  "section": "Issue Types Mapping",
                                  "tooltip": {
                                  "definition":"Issue types that are required to be tracked for First time pass rate (FTPR).<br> Example: Story or equivalent <hr>"
                                  }
                                }

]
);