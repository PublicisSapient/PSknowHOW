db.getCollection('kpi_fieldmapping').remove({});
db.getCollection('kpi_fieldmapping').insert(
[
{
        kpiId: 'kpi14',
        kpiName: 'Defect Injection Rate',
        type: ['Scrum'],
        fieldNames: ['jiraDod', 'jiraDefectInjectionIssueType', 'jiraDefectCreatedStatus', 'jiraDefectDroppedStatus']
      },
      {
        kpiId: 'kpi82',
        kpiName: 'First Time Pass Rate',
        type: ['Scrum'],
        fieldNames: ['resolutionTypeForRejection', 'jiraStoryIdentification', 'JiraIssueDeliverdStatus', 'defectPriority', 'ExcludeRCAFromFTPR']
      },
      {
        kpiId: 'kpi111',
        kpiName: 'Defect Density',
        type: ['Scrum'],
        fieldNames: ['jiraQADefectDensityIssueType', 'jiraDod']
      },
      {
        kpiId: 'kpi35',
        kpiName: 'Defect Seepage Rate',
        type: ['Scrum'],
        fieldNames: ['jiraDefectSeepageIssueType', 'jiraDefectDroppedStatus']
      },
      {
        kpiId: 'kpi34',
        kpiName: 'Defect Removal Efficiency',
        type: ['Scrum'],
        fieldNames: ['jiraDefectRemovalStatus', 'jiraDefectRemovalIssueType']
      },
      {
        kpiId: 'kpi37',
        kpiName: 'Defect Rejection Rate',
        type: ['Scrum'],
        fieldNames: ['jiraDefectRejectionStatus', 'resolutionTypeForRejection', 'jiraDefectRejectionlIssueType']
      },
      {
        kpiId: 'kpi28',
        kpiName: 'Defect Count By Priority',
        type: ['Scrum'],
        fieldNames: ['jiraDefectCountlIssueType', 'jiraDefectDroppedStatus']
      },
      {
        kpiId: 'kpi36',
        kpiName: 'Defect Count By RCA',
        type: ['Scrum'],
        fieldNames: ['jiraDefectCountlIssueType', 'jiraDefectDroppedStatus']
      },
      {
        kpiId: 'kpi126',
        type: ['Scrum'],
        kpiName: 'Created vs Resolved defects',
        fieldNames: ['JiraIssueDeliverdStatus']
      },
      {
        kpiId: 'kpi42',
        kpiName: 'Regression Automation Coverage',
        type: ['Scrum'],
        fieldNames: ['jiraRegressionTestValue', 'testRegressionValue', 'regressionAutomationFolderPath']
      },
      {
        kpiId: 'kpi16',
        kpiName: 'In-Sprint Automation Coverage',
        type: ['Scrum'],
        fieldNames: ['jiraRegressionTestValue', 'testRegressionValue', 'regressionAutomationFolderPath']
      },
      {
        kpiId: 'kpi17',
        type: ['Scrum'],
        kpiName: 'Unit Test Coverage',
        fieldNames: []
      },
      {
        kpiId: 'kpi38',
        kpiName: 'Sonar Violations',
        type: ['Scrum'],
        fieldNames: []
      },
      {
        kpiId: 'kpi27',
        kpiName: 'Sonar Tech Debt',
        type: ['Scrum'],
        fieldNames: []
      },
      {
        kpiId: 'kpi116',
        kpiName: 'Change Failure Rate',
        type: ['Scrum'],
        fieldNames: []
      },
      {
        kpiId: 'kpi70',
        kpiName: 'Test Execution and pass percentage',
        type: ['Scrum'],
        fieldNames: []
      },
      {
        kpiId: 'kpi40',
        kpiName: 'Story Count',
        type: ['Scrum'],
        fieldNames: ['jiraStoryIdentification']
      },
      {
        kpiId: 'kpi72',
        kpiName: 'Commitment Reliability',
        type: ['Scrum'],
        fieldNames: ['jiraSprintVelocityIssueType','JiraIssueDeliverdStatus']
      },
      {
        kpiId: 'kpi39',
        kpiName: 'Sprint Velocity',
        type: ['Scrum'],
        fieldNames: ['jiraSprintVelocityIssueType']
      },
      {
        kpiId: 'kpi46',
        kpiName: 'Sprint Capacity Utilization',
        type: ['Scrum'],
        fieldNames: ['jiraSprintCapacityIssueType']
      },
      {
        kpiId: 'kpi83',
        kpiName: 'Average Resolution Time',
        type: ['Scrum'],
        fieldNames: ['resolutionTypeForRejection', 'jiraIssueTypeNames', 'jiradefecttype', 'jiraIssueDeliverdStatus', 'jiraStatusForDevelopment']
      },
      {
        kpiId: 'kpi84',
        type: ['Scrum'],
        kpiName: 'Mean Time To Merge',
        fieldNames: []
      },
      {
        kpiId: 'kpi11',
        type: ['Scrum'],
        kpiName: 'Check-Ins & Merge Requests',
        fieldNames: []
      },
      {
        kpiId: 'kpi8',
        kpiName: 'Code Build Time',
        type: ['Scrum'],
        fieldNames: []
      },
      {
        kpiId: 'kpi3',
        kpiName: 'Lead Time',
        type: ['Scrum'],
        fieldNames: ['jiraIntakeToDorIssueType', 'jiraDor', 'jiraDod', 'jiraLiveStatus']
      },
      {
        kpiId: 'kpi118',
        kpiName: 'Deployment Frequency',
        type: ['Scrum'],
        fieldNames: []
      },
      {
        kpiId: 'kpi73',
        kpiName: 'Release Frequency',
        type: ['Scrum'],
        fieldNames: []
      },
      {
        kpiId: 'kpi113',
        kpiName: 'Value delivered (Cost of Delay)',
        type: ['Scrum'],
        fieldNames: []
      },
      {
        kpiId: 'kpi55',
        kpiName: 'Ticket Open vs Closed rate by type',
        type: ['Kanban'],
        fieldNames: ['ticketCountIssueType', 'jiraTicketClosedStatus']
      },
      {
        kpiId: 'kpi54',
        kpiName: 'Ticket Open vs Closed rate by Priority',
        type: ['Kanban'],
        fieldNames: ['ticketCountIssueType', 'jiraTicketClosedStatus']
      },
      {
        kpiId: 'kpi50',
        kpiName: 'Net Open Ticket Count by Priority',
        type: ['Kanban'],
        fieldNames: ['storyFirstStatus', 'kanbanRCACountIssueType', 'ticketCountIssueType', 'jiraTicketClosedStatus', 'jiraLiveStatus', 'jiraTicketRejectedStatus']
      },
      {
        kpiId: 'kpi51',
        kpiName: 'Net Open Ticket Count By RCA',
        type: ['Kanban'],
        fieldNames: ['storyFirstStatus', 'kanbanRCACountIssueType', 'ticketCountIssueType', 'jiraTicketClosedStatus', 'jiraLiveStatus', 'jiraTicketRejectedStatus']
      },
      {
        kpiId: 'kpi48',
        kpiName: 'Net Open Ticket By Status',
        type: ['Kanban'],
        fieldNames: ['storyFirstStatus', 'kanbanRCACountIssueType', 'ticketCountIssueType', 'jiraTicketClosedStatus', 'jiraLiveStatus', 'jiraTicketRejectedStatus']
      },
      {
        kpiId: 'kpi997',
        kpiName: 'Open Ticket Ageing By Priority',
        type: ['Kanban'],
        fieldNames: ['ticketCountIssueType', 'jiraTicketClosedStatus', 'jiraLiveStatus', 'jiraTicketRejectedStatus']
      },
      {
        kpiId: 'kpi63',
        kpiName: 'Regression Automation Coverage',
        type: ['Kanban'],
        fieldNames: ['jiraRegressionTestValue', 'testRegressionValue', 'regressionAutomationFolderPath']
      },
      {
        kpiId: 'kpi49',
        kpiName: 'Ticket Velocity',
        type: ['Kanban'],
        fieldNames: ['jiraTicketVelocityIssueType', 'ticketDeliverdStatus']
      },
      {
        kpiId: 'kpi53',
        kpiName: 'Lead Time',
        type: ['Kanban'],
        fieldNames: ['jiraTicketTriagedStatus', 'jiraTicketClosedStatus', 'jiraLiveStatus']
      },
      {
        kpiId: 'kpi114',
        type: ['Kanban'],
        kpiName: 'Value delivered (Cost of Delay)',
        fieldNames: []
      },
      {
        kpiId: 'kpi74',
        kpiName: 'Release Frequency',
        type: ['Kanban'],
        fieldNames: []
      },
      {
        kpiId: 'kpi62',
        kpiName: 'Unit Test Coverage',
        type: ['Kanban'],
        fieldNames: []
      },
      {
        kpiId: 'kpi64',
        kpiName: 'Sonar Violations',
        type: ['Kanban'],
        fieldNames: []
      },
      {
        kpiId: 'kpi67',
        kpiName: 'Sonar Tech Debt',
        type: ['Kanban'],
        fieldNames: []
      },
      {
        kpiId: 'kpi71',
        kpiName: 'Test Execution and pass percentage',
        type: ['Kanban'],
        fieldNames: []
      },
      {
        kpiId: 'kpi58',
        kpiName: 'Team Capacity',
        type: ['Kanban'],
        fieldNames: []
      },
      {
        kpiId: 'kpi66',
        kpiName: 'Code Build Time',
        type: ['Kanban'],
        fieldNames: []
      },
      {
        kpiId: 'kpi65',
        kpiName: 'Number of Check-ins',
        type: ['Kanban'],
        fieldNames: []
      },
      {
        kpiId: 'kpi121',
        kpiName: 'Capacity',
        type: ['Other'],
        fieldNames: ['jiraSprintCapacityIssueType']
      },
      {
        kpiId: 'kpi119',
        kpiName: 'Work Remaining',
        type: ['Other'],
        fieldNames: []
      },
      {
        kpiId: 'kpi75',
        kpiName: 'Estimate vs Actual',
        type: ['Other'],
        fieldNames: []
      },
      {
        kpiId: 'kpi123',
        kpiName: 'Issues likely to Spill',
        type: ['Other'],
        fieldNames: ['workingHoursDayCPT']
      },
      {
        kpiId: 'kpi122',
        kpiName: 'Closure Possible Today',
        type: ['Other'],
        fieldNames: ['jiraStatusForQa', 'workingHoursDayCPT']
      },
      {
        kpiId: 'kpi120',
        kpiName: 'Scope Change',
        type: ['Other'],
        fieldNames: []
      },
      {
        kpiId: 'kpi124',
        kpiName: 'Estimation Hygiene',
        type: ['Other'],
        fieldNames: []
      },
      {
        kpiId: 'kpi125',
        kpiName: 'Daily Closures',
        type: ['Other'],
        fieldNames: []
      },
      {
        kpiId: 'kpi79',
        kpiName: 'Test Cases Without Story Link',
        type: ['Other'],
        fieldNames: ['jiraStoryIdentification', 'JiraRegressionTestValue', 'testRegressionValue', 'regressionAutomationFolderPath']
      },
      {
        kpiId: 'kpi80',
        kpiName: 'Defects Without Story Link',
        type: ['Other'],
        fieldNames: ['jiraStoryIdentification', 'jiraDefectDroppedStatus']
      },
      {
        kpiId: 'kpi127',
        kpiName: 'Production Defects Ageing',
        type: ['Other'],
        fieldNames: ['jiraDod', 'jiraLiveStatus', 'jiraDefectDroppedStatus']
      },
      {
        kpiId: 'kpi989',
        kpiName: 'Kpi Maturity',
        type: ['Other'],
        fieldNames: []
      }
	  ]);