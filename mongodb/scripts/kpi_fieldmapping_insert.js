db.getCollection('kpi_fieldmapping').remove({});
db.getCollection('kpi_fieldmapping').insert(
[
  {
    "kpiId": "kpi14",
    "fieldNames": ['jiraDod','jiraDefectInjectionIssueType','jiraDefectCreatedStatus','jiraDefectDroppedStatus']
  },
  {
    "kpiId": "kpi82",
    "fieldNames": ['resolutionTypeForRejection','jiraStoryIdentification','JiraIssueDeliverdStatus','defectPriority','ExcludeRCAFromFTPR']
  },
  {
    "kpiId": "kpi111",
    "fieldNames": ['jiraQADefectDensityIssueType','jiraDod']
  },
  {
    "kpiId": "kpi35",
    "fieldNames": ['jiraDefectSeepageIssueType','jiraDefectDroppedStatus']
  },
  {
    "kpiId": "kpi34",
    "fieldNames": ['jiraDefectRemovalStatus','jiraDefectRemovalIssueType']
  },
  {
    "kpiId": "kpi37",
    "fieldNames": ['jiraDefectRejectionStatus','resolutionTypeForRejection','jiraDefectRejectionlIssueType']
  },
  {
    "kpiId": "kpi28",
    "fieldNames": ['jiraDefectCountlIssueType','jiraDefectDroppedStatus']
  },
  {
    "kpiId": "kpi36",
    "fieldNames": ['jiraDefectCountlIssueType','jiraDefectDroppedStatus']
  },
  {
    "kpiId": "kpi126",
    "fieldNames": []
  },
  {
    "kpiId": "kpi42",
    "fieldNames": ['jiraRegressionTestValue','testRegressionValue','regressionAutomationFolderPath']
  },
  {
    "kpiId": "kpi16",
    "fieldNames": ['jiraRegressionTestValue','testRegressionValue','regressionAutomationFolderPath']
  },
  {
    "kpiId": "kpi17",
    "fieldNames": []
  },
  {
    "kpiId": "kpi38",
    "fieldNames": []
  },
  {
    "kpiId": "kpi27",
    "fieldNames": []
  },
  {
    "kpiId": "kpi116",
    "fieldNames": []
  },
  {
    "kpiId": "kpi70",
    "fieldNames": []
  },
  {
    "kpiId": "kpi40",
    "fieldNames": ['jiraStoryIdentification']
  },
  {
    "kpiId": "kpi72",
    "fieldNames": ['jiraSprintVelocityIssueType']
  },
  {
    "kpiId": "kpi39",
    "fieldNames": ['jiraSprintVelocityIssueType']
  },
  {
    "kpiId": "kpi46",
    "fieldNames": ['jiraSprintCapacityIssueType']
  },
  {
    "kpiId": "kpi83",
    "fieldNames": ['resolutionTypeForRejection','jiraIssueTypeNames','jiradefecttype','jiraIssueDeliverdStatus','jiraStatusForDevelopment']
  },
  {
    "kpiId": "kpi84",
    "fieldNames": []
  },
  {
    "kpiId": "kpi11",
    "fieldNames": []
  },
  {
    "kpiId": "kpi8",
    "fieldNames": []
  },
  {
    "kpiId": "kpi3",
    "fieldNames": ['jiraIntakeToDorIssueType','jiraDor','jiraDod','jiraLiveStatus']
  },
  {
    "kpiId": "kpi118",
    "fieldNames": []
  },
  {
    "kpiId": "kpi73",
    "fieldNames": []
  },
  {
    "kpiId": "kpi113",
    "fieldNames": []
  },
  {
    "kpiId": "kpi55",
    "fieldNames": ['ticketCountIssueType','jiraTicketClosedStatus']
  },
  {
    "kpiId": "kpi54",
    "fieldNames": ['ticketCountIssueType','jiraTicketClosedStatus']
  },
  {
    "kpiId": "kpi50",
    "fieldNames": ['storyFirstStatus','kanbanRCACountIssueType','ticketCountIssueType','jiraTicketClosedStatus','jiraLiveStatus','jiraTicketRejectedStatus']
  },
  {
    "kpiId": "kpi51",
    "fieldNames": ['storyFirstStatus','kanbanRCACountIssueType','ticketCountIssueType','jiraTicketClosedStatus','jiraLiveStatus','jiraTicketRejectedStatus']
  },
  {
    "kpiId": "kpi48",
    "fieldNames": ['storyFirstStatus','kanbanRCACountIssueType','ticketCountIssueType','jiraTicketClosedStatus','jiraLiveStatus','jiraTicketRejectedStatus']
  },
  {
    "kpiId": "kpi997",
    "fieldNames": ['ticketCountIssueType','jiraTicketClosedStatus','jiraLiveStatus','jiraTicketRejectedStatus']
  },
  {
    "kpiId": "kpi63",
    "fieldNames": ['jiraRegressionTestValue','testRegressionValue','regressionAutomationFolderPath']
  },
  {
    "kpiId": "kpi49",
    "fieldNames": ['jiraTicketVelocityIssueType','ticketDeliverdStatus']
  },
  {
    "kpiId": "kpi53",
    "fieldNames": ['jiraTicketTriagedStatus','jiraTicketClosedStatus','jiraLiveStatus']
  },
  {
    "kpiId": "kpi114",
    "fieldNames": []
  },
  {
    "kpiId": "kpi74",
    "fieldNames": []
  },
  {
    "kpiId": "kpi62",
    "fieldNames": []
  },
  {
    "kpiId": "kpi64",
    "fieldNames": []
  },
  {
    "kpiId": "kpi67",
    "fieldNames": []
  },
  {
    "kpiId": "kpi71",
    "fieldNames": []
  },
  {
    "kpiId": "kpi58",
    "fieldNames": []
  },
  {
    "kpiId": "kpi66",
    "fieldNames": []
  },
  {
    "kpiId": "kpi65",
    "fieldNames": []
  },
  {
    "kpiId": "kpi121",
    "fieldNames": ['jiraSprintCapacityIssueType']
  },
  {
    "kpiId": "kpi119",
    "fieldNames": []
  },
  {
    "kpiId": "kpi75",
    "fieldNames": []
  },
  {
    "kpiId": "kpi123",
    "fieldNames": ['workingHoursDayCPT']
  },
  {
    "kpiId": "kpi122",
    "fieldNames": ['jiraStatusForQa','workingHoursDayCPT']
  },
  {
    "kpiId": "kpi120",
    "fieldNames": []
  },
  {
    "kpiId": "kpi124",
    "fieldNames": []
  },
  {
    "kpiId": "kpi125",
    "fieldNames": []
  },
  {
    "kpiId": "kpi79",
    "fieldNames": ['jiraStoryIdentification','JiraRegressionTestValue','testRegressionValue','regressionAutomationFolderPath']
  },
   {
    "kpiId": "kpi80",
    "fieldNames": ['jiraStoryIdentification','jiraDefectDroppedStatus']
  },
  {
    "kpiId": "kpi127",
    "fieldNames": ['jiraDod','jiraLiveStatus','jiraDefectDroppedStatus']
  },
  {
    "kpiId": "kpi989",
    "fieldNames": []
  }
]);
