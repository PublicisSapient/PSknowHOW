db.getCollection('kpi_fieldmapping').remove({});
db.getCollection('kpi_fieldmapping').insert(
[
{
        kpiId: 'kpi14',
        kpiName: 'Defect Injection Rate',
		kpiSource:'Jira',
        type: ['Scrum'],
        fieldNames :  {'Workflow Status Mapping' : ['jiraDod', 'jiraDefectCreatedStatus', 'jiraDefectDroppedStatus'], 'Issue Types Mapping' : ['jiraDefectInjectionIssueType']}
      },
      {
        kpiId: 'kpi82',
        kpiName: 'First Time Pass Rate',
		kpiSource:'Jira',
        type: ['Scrum'],
        fieldNames : {'Workflow Status Mapping' : ['resolutionTypeForRejection','jiraIssueDeliverdStatus'], 'Issue Types Mapping' : ['jiraFTPRStoryIdentification'], 'Defects Mapping' : ['defectPriority', 'excludeRCAFromFTPR'] }
      },
      {
        kpiId: 'kpi111',
        kpiName: 'Defect Density',
		kpiSource:'Jira',
        type: ['Scrum'],
        fieldNames : {'Issue Types Mapping' : ['jiraQADefectDensityIssueType'], 'Workflow Status Mapping' : ['jiraDod'], 'Defects Mapping' : ['jiraBugRaisedByQAIdentification'] }
      },
      {
        kpiId: 'kpi35',
        kpiName: 'Defect Seepage Rate',
		kpiSource:'Jira',
        type: ['Scrum'],
        fieldNames : {'Issue Types Mapping' :['jiraDefectSeepageIssueType'], 'Workflow Status Mapping' : ['jiraDefectDroppedStatus'], 'Defects Mapping' : ['jiraBugRaisedByIdentification'] }
      },
      {
        kpiId: 'kpi34',
        kpiName: 'Defect Removal Efficiency',
		kpiSource:'Jira',
        type: ['Scrum'],
        fieldNames : {'Workflow Status Mapping' :  ['jiraDefectRemovalStatus'], 'Issue Types Mapping' : ['jiraDefectRemovalIssueType'] }
      },
      {
        kpiId: 'kpi37',
        kpiName: 'Defect Rejection Rate',
		kpiSource:'Jira',
        type: ['Scrum'],
        fieldNames : { 'Workflow Status Mapping' : ['jiraDefectRejectionStatus','resolutionTypeForRejection'], 'Issue Types Mapping' :  ['jiraDefectRejectionlIssueType'] }
      },
      {
        kpiId: 'kpi28',
        kpiName: 'Defect Count By Priority',
		kpiSource:'Jira',
        type: ['Scrum'],
        fieldNames : {'Issue Types Mapping' :  ['jiraDefectCountlIssueType'] , 'Workflow Status Mapping' : ['jiraDefectDroppedStatus'] }
      },
      {
        kpiId: 'kpi36',
        kpiName: 'Defect Count By RCA',
		kpiSource:'Jira',
        type: ['Scrum'],
        fieldNames : {'Issue Types Mapping' :  ['jiraDefectCountlIssueType'], 'Workflow Status Mapping' : ['jiraDefectDroppedStatus'] }
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
        kpiName: 'Story Count',
		kpiSource: 'Jira',
        type: ['Scrum'],
        fieldNames : {'Issue Types Mapping' : ['jiraStoryIdentification'] }
      },
      {
        kpiId: 'kpi72',
        kpiName: 'Commitment Reliability',
		kpiSource: 'Jira',
        type: ['Scrum'],
        fieldNames : { 'Issue Types Mapping' : ['jiraSprintVelocityIssueType'], 'Workflow Status Mapping' : ['jiraIssueDeliverdStatus'] }
      },
      {
        kpiId: 'kpi39',
        kpiName: 'Sprint Velocity',
		kpiSource: 'Jira',
        type: ['Scrum'],
        fieldNames : {'Issue Types Mapping' : ['jiraSprintVelocityIssueType'] }
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
        fieldNames : {'Workflow Status Mapping' : ['resolutionTypeForRejection','jiraIssueDeliverdStatus','jiraStatusForDevelopment'], 'Issue Types Mapping' : ['jiraIssueTypeNames'], 'Defects Mapping' : ['jiradefecttype'] }
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
        fieldNames : {'Issue Types Mapping' : ['jiraSprintVelocityIssueType'], 'Workflow Status Mapping' : ['jiraIssueDeliverdStatus'] }
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
        kpiId: 'kpi121',
        kpiName: 'Capacity',
		kpiSource: 'Jira',
        type: ['Other'],
        fieldNames : {'Issue Types Mapping' : ['jiraSprintCapacityIssueType'] }
      },
      {
        kpiId: 'kpi119',
        kpiName: 'Work Remaining',
		kpiSource: 'Jira',
        type: ['Other'],
        fieldNames : { }
      },
      {
        kpiId: 'kpi75',
        kpiName: 'Estimate vs Actual',
		kpiSource: 'Jira',
        type: ['Other'],
        fieldNames : { }
      },
      {
        kpiId: 'kpi123',
        kpiName: 'Issues likely to Spill',
		kpiSource: 'Jira',
        type: ['Other'],
        fieldNames : {'Custom Fields Mapping' : ['workingHoursDayCPT'] }
      },
      {
        kpiId: 'kpi122',
        kpiName: 'Closure Possible Today',
		kpiSource: 'Jira',
        type: ['Other'],
        fieldNames : { 'Workflow Status Mapping' : ['jiraStatusForQa'], 'Custom Fields Mapping' : ['workingHoursDayCPT'] }
      },
      {
        kpiId: 'kpi120',
        kpiName: 'Scope Change',
		kpiSource: 'Jira',
        type: ['Other'],
        fieldNames : { }
      },
      {
        kpiId: 'kpi124',
        kpiName: 'Estimation Hygiene',
		kpiSource: 'Jira',
        type: ['Other'],
        fieldNames : { 'Workflow Status Mapping' : ['issueStatusExcluMissingWork'] }
      },
      {
        kpiId: 'kpi125',
        kpiName: 'Daily Closures',
		kpiSource: 'Jira',
        type: ['Other'],
        fieldNames : { }
      },
	  {
        kpiId: 'kpi128',
        kpiName: 'Work Completed',
		kpiSource: 'Jira',
        type: ['Other'],
        fieldNames : { }
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
        kpiId: 'kpi989',
        kpiName: 'Kpi Maturity',
        type: ['Other'],
        fieldNames : { }
      }
	  ]);