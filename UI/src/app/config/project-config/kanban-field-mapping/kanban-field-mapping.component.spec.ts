/*******************************************************************************
/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { KanbanFieldMappingComponent } from './kanban-field-mapping.component';
import { MessageService,ConfirmationService } from 'primeng/api';
import { HttpService } from '../../../services/http.service';
import { SharedService } from '../../../services/shared.service';
import { GetAuthorizationService } from '../../../services/get-authorization.service';
import { FormGroup, ReactiveFormsModule, FormsModule, FormBuilder } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AppConfig, APP_CONFIG } from 'src/app/services/app.config';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import { ChipsModule } from 'primeng/chips';
import { DropdownModule } from 'primeng/dropdown';
import { MultiSelectModule } from 'primeng/multiselect';
import { InputSwitchModule } from 'primeng/inputswitch';
import { TooltipModule } from 'primeng/tooltip';
import { AccordionModule } from 'primeng/accordion';
import { ToastModule } from 'primeng/toast';
import { DialogModule } from 'primeng/dialog';

import { environment } from 'src/environments/environment';
import { RadioButtonModule } from 'primeng/radiobutton';
import { BadgeModule } from 'primeng/badge';

const completeHierarchyData = {
  kanban: [
    {
      id: '63244d35d1d9f4caf85056f7',
      level: 1,
      hierarchyLevelId: 'corporate',
      hierarchyLevelName: 'Corporate Name'
    },
    {
      id: '63244d35d1d9f4caf85056f8',
      level: 2,
      hierarchyLevelId: 'business',
      hierarchyLevelName: 'Business Name'
    },
    {
      id: '63244d35d1d9f4caf85056f9',
      level: 3,
      hierarchyLevelId: 'dummy',
      hierarchyLevelName: 'dummy Name'
    },
    {
      id: '63244d35d1d9f4caf85056fa',
      level: 4,
      hierarchyLevelId: 'subdummy',
      hierarchyLevelName: 'Subdummy'
    },
    {
      level: 5,
      hierarchyLevelId: 'project',
      hierarchyLevelName: 'Project'
    },
    {
      level: 6,
      hierarchyLevelId: 'sqd',
      hierarchyLevelName: 'Squad'
    }
  ],
  scrum: [
    {
      id: '63244d35d1d9f4caf85056f7',
      level: 1,
      hierarchyLevelId: 'corporate',
      hierarchyLevelName: 'Corporate Name'
    },
    {
      id: '63244d35d1d9f4caf85056f8',
      level: 2,
      hierarchyLevelId: 'business',
      hierarchyLevelName: 'Business Name'
    },
    {
      id: '63244d35d1d9f4caf85056f9',
      level: 3,
      hierarchyLevelId: 'dummy',
      hierarchyLevelName: 'dummy Name'
    },
    {
      id: '63244d35d1d9f4caf85056fa',
      level: 4,
      hierarchyLevelId: 'subdummy',
      hierarchyLevelName: 'Subdummy'
    },
    {
      level: 5,
      hierarchyLevelId: 'project',
      hierarchyLevelName: 'Project'
    },
    {
      level: 6,
      hierarchyLevelId: 'sprint',
      hierarchyLevelName: 'Sprint'
    },
    {
      level: 7,
      hierarchyLevelId: 'sqd',
      hierarchyLevelName: 'Squad'
    }
  ]
};

const fakeSelectedTool = [{
  id: '5fc086b9410df80001701334',
  toolName: 'Jira',
  basicProjectConfigId: '5fc0867e410df8000170132e',
  connectionId: '5fc0857c410df80001701327',
  createdAt: '2020-11-27T04:55:21',
  updatedAt: '2020-11-27T04:55:21',
  queryEnabled: false,
  boardQuery: ''
}];

const fakeProject = {
  id: '6332f31d68b5d05cf59c42c2',
  name: 'Map Kanban',
  type: 'Kanban',
  corporate: 'C1',
  business: 'Bus1',
  account: 'Acc1',
  subaccount: 'Org4'
};

const fakeSelectedFieldMapping = {
  id: '6332f6618736d469c66f5e18',
  projectToolConfigId: '6332f32e68b5d05cf59c42c8',
  basicProjectConfigId: '6332f31d68b5d05cf59c42c2',
  defectPriority: [],
  jiraIssueTypeNames: [
    'Issue',
    'Defect',
    'Story',
    'Epic'
  ],
  storyFirstStatus: 'Open',
  rootCause: 'customfield_19121',
  jiraIssueEpicType: [
    'Epic'
  ],
  jiraStatusForQa: [
    'Ready For Testing',
    'In Testing'
  ],
  jiraTechDebtIdentification: '',
  jiraTechDebtCustomField: '',
  jiraTechDebtValue: [],
  jiraStoryPointsCustomField: 'customfield_20803',
  jiraCanNotAutomatedTestValue: [],
  jiraLiveStatus: '',
  regressionAutomationLabels: [],
  ticketCountIssueType: [
    'Issue',
    'Defect',
    'Story'
  ],
  kanbanRCACountIssueType: [
    'Defect'
  ],
  jiraTicketVelocityIssueType: [
    'Issue',
    'Defect',
    'Story'
  ],
  ticketDeliverdStatus: [
    'Closed',
    'Ready for Delivery'
  ],
  kanbanJiraTechDebtIssueType: [
    'Issue',
    'Defect',
    'Story'
  ],
  jiraTicketResolvedStatus: [
    'Closed'
  ],
  jiraTicketClosedStatus: [
    'Closed'
  ],
  kanbanCycleTimeIssueType: [
    'Issue',
    'Defect',
    'Story'
  ],
  jiraTicketTriagedStatus: [
    'In Progress',
    'In Development'
  ],
  jiraTicketWipStatus: [
    'In Progress',
    'In Development'
  ],
  jiraTicketRejectedStatus: [
    'Dropped',
    'Rejected'
  ],
  rootCauseValue: [
    'Coding'
  ],
  epicCostOfDelay: 'customfield_58102',
  epicRiskReduction: 'customfield_58101',
  epicUserBusinessValue: 'customfield_58100',
  epicWsjf: 'customfield_58104',
  epicTimeCriticality: 'customfield_51002',
  epicJobSize: 'customfield_61041',
  squadIdentifier: '',
  squadIdentMultiValue: [],
  squadIdentSingleValue: '',
  jiraTestCaseType: [],
  testAutomatedIdentification: '',
  testAutomationCompletedIdentification: '',
  testRegressionIdentification: '',
  testAutomated: '',
  testAutomationCompletedByCustomField: '',
  testRegressionByCustomField: '',
  jiraAutomatedTestValue: [],
  jiraRegressionTestValue: [],
  jiraCanBeAutomatedTestValue: [],
  estimationCriteria: 'Story Point',
  storyPointToHourMapping: 8,
  workingHoursDayCPT: 6,
  additionalFilterConfig: []
};

const fakeSelectedFieldMappingWithAdditionalFilters = {
  id: '6332f6618736d469c66f5e18',
  projectToolConfigId: '6332f32e68b5d05cf59c42c8',
  basicProjectConfigId: '6332f31d68b5d05cf59c42c2',
  defectPriority: [],
  jiraIssueTypeNames: [
    'Issue',
    'Defect',
    'Story',
    'Epic'
  ],
  storyFirstStatus: 'Open',
  rootCause: 'customfield_19121',
  jiraIssueEpicType: [
    'Epic'
  ],
  jiraStatusForQa: [
    'Ready For Testing',
    'In Testing'
  ],
  jiraTechDebtIdentification: '',
  jiraTechDebtCustomField: '',
  jiraTechDebtValue: [],
  jiraStoryPointsCustomField: 'customfield_20803',
  jiraCanNotAutomatedTestValue: [],
  jiraLiveStatus: '',
  regressionAutomationLabels: [],
  ticketCountIssueType: [
    'Issue',
    'Defect',
    'Story'
  ],
  kanbanRCACountIssueType: [
    'Defect'
  ],
  jiraTicketVelocityIssueType: [
    'Issue',
    'Defect',
    'Story'
  ],
  ticketDeliverdStatus: [
    'Closed',
    'Ready for Delivery'
  ],
  kanbanJiraTechDebtIssueType: [
    'Issue',
    'Defect',
    'Story'
  ],
  jiraTicketResolvedStatus: [
    'Closed'
  ],
  jiraTicketClosedStatus: [
    'Closed'
  ],
  kanbanCycleTimeIssueType: [
    'Issue',
    'Defect',
    'Story'
  ],
  jiraTicketTriagedStatus: [
    'In Progress',
    'In Development'
  ],
  jiraTicketWipStatus: [
    'In Progress',
    'In Development'
  ],
  jiraTicketRejectedStatus: [
    'Dropped',
    'Rejected'
  ],
  rootCauseValue: [
    'Coding'
  ],
  epicCostOfDelay: 'customfield_58102',
  epicRiskReduction: 'customfield_58101',
  epicUserBusinessValue: 'customfield_58100',
  epicWsjf: 'customfield_58104',
  epicTimeCriticality: 'customfield_51002',
  epicJobSize: 'customfield_61041',
  squadIdentifier: '',
  squadIdentMultiValue: [],
  squadIdentSingleValue: '',
  jiraTestCaseType: [],
  testAutomatedIdentification: '',
  testAutomationCompletedIdentification: '',
  testRegressionIdentification: '',
  testAutomated: '',
  testAutomationCompletedByCustomField: '',
  testRegressionByCustomField: '',
  jiraAutomatedTestValue: [],
  jiraRegressionTestValue: [],
  jiraCanBeAutomatedTestValue: [],
  estimationCriteria: 'Story Point',
  storyPointToHourMapping: 8,
  workingHoursDayCPT: 6,
  additionalFilterConfig: [
    {
      filterId: 'sqd',
      identifyFrom: 'Labels',
      identificationField: '',
      values: [
        'Test1',
        'Test2'
      ]
    }
  ]
};
const successResponse = {
  message: 'field mappings added successfully',
  success: true,
  data: {
    id: '5f05ca7224aa9a00011224ee',
    projectToolConfigId: '5fc643ce11193836e6545568',
    atmQueryEndpoint: 'testAPIEndPoint',
    jiraIssueTypeNames: ['Support Request', 'Incident', 'Project Request', 'Member Account Request', 'DOJO Consulting Request', 'Epic', 'Bug'],
    storyFirstStatus: 'Open',
    rootCause: '',
    jiraAtmProjectId: '',
    jiraAtmProjectKey: '',
    jiraIssueEpicType: [],
    testAutomated: 'customfield_12203',
    testAutomationStatusLabel: '',
    testRegressionLabel: '',
    testRegressionValue: [],
    jiraTestCaseType: [],
    jiraTechDebtIdentification: '',
    jiraTechDebtCustomField: '',
    jiraTechDebtValue: [],
    atmFolderPath: [],
    jiraStoryPointsCustomField: 'customfield_20803',
    jiraAutomatedTestValue: [],
    jiraCanNotAutomatedTestValue: [],
    regressionAutomationLabels: [],
    ticketCountIssueType: ['Support Request', 'Incident', 'Project Request', 'DOJO Consulting Request', 'Member Account Request'],
    jiraTicketVelocityIssueType: ['Support Request', 'Incident', 'Project Request', 'DOJO Consulting Request', 'Member Account Request'],
    ticketDeliverdStatus: ['Resolved'],
    ticketReopenStatus: ['Reopened'],
    kanbanJiraTechDebtIssueType: ['Support Request', 'Incident', 'Project Request', 'Member Account Request', 'DOJO Consulting Request'],
    jiraTicketResolvedStatus: ['Resolved', 'CLOSED', 'Closed'],
    jiraTicketClosedStatus: ['Closed', 'CLOSED'],
    jiraLiveStatus: 'Live',
    kanbanCycleTimeIssueType: ['Support Request', 'Incident', 'Project Request', 'Member Account Request', 'DOJO Consulting Request'],
    jiraTicketTriagedStatus: ['Assigned', 'REVIEWING'],
    jiraTicketWipStatus: [],
    jiraTicketRejectedStatus: [],
    rootCauseValue: ['Code Fix'],
    epicCostOfDelay: '',
    epicRiskReduction: '',
    epicUserBusinessValue: '',
    epicWsjf: '',
    epicTimeCriticality: '',
    epicJobSize: '',
    atmSubprojectField: '',
    estimationCriteria: 'Story Point',
    storyPointToHourMapping: 8
  }
};
const dropDownMetaData = require('../../../../test/resource/KPIConfig.json');


describe('KanbanFieldMappingComponent', () => {
  let component: KanbanFieldMappingComponent;
  let fixture: ComponentFixture<KanbanFieldMappingComponent>;
  let getAuthorizationService: GetAuthorizationService;
  let sharedService: SharedService;
  let httpService: HttpService;
  let httpMock;
  const baseUrl = environment.baseUrl;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [KanbanFieldMappingComponent],
      imports: [
        BrowserAnimationsModule,
        FormsModule,
        ReactiveFormsModule,
        RouterTestingModule,
        HttpClientTestingModule,
        ChipsModule,
        DropdownModule,
        MultiSelectModule,
        InputSwitchModule,
        TooltipModule,
        AccordionModule,
        ToastModule,
        DialogModule,
        RadioButtonModule,
        BadgeModule
      ],
      providers: [
        HttpService,
        SharedService,
        MessageService,
        ConfirmationService,
        GetAuthorizationService,
        HttpService,
        { provide: APP_CONFIG, useValue: AppConfig }
      ]
    })
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(KanbanFieldMappingComponent);
    component = fixture.componentInstance;
    getAuthorizationService = TestBed.inject(GetAuthorizationService);
    httpService = TestBed.inject(HttpService);
    sharedService = TestBed.inject(SharedService);
    sharedService.setSelectedProject(fakeProject);
    sharedService.setSelectedToolConfig(fakeSelectedTool);
    sharedService.setSelectedFieldMapping(fakeSelectedFieldMapping);
    httpMock = TestBed.inject(HttpTestingController);

    let localStore = {};

    spyOn(window.localStorage, 'getItem').and.callFake((key) =>
      key in localStore ? localStore[key] : null
    );
    spyOn(window.localStorage, 'setItem').and.callFake(
      (key, value) => (localStore[key] = value + '')
    );
    spyOn(window.localStorage, 'clear').and.callFake(() => (localStore = {}));

    localStorage.setItem('completeHierarchyData', JSON.stringify(completeHierarchyData));
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should fetch dropdown metadata on load', () => {
    component.selectedToolConfig = fakeSelectedTool;
    spyOn(httpService, 'getKPIConfigMetadata').and.callThrough();
    component.getDropdownData();
    // fixture.detectChanges();
    expect(httpService.getKPIConfigMetadata).toHaveBeenCalledTimes(1);
    const metadataReq = httpMock.expectOne(baseUrl + '/api/editConfig/jira/editKpi/' + sharedService.getSelectedToolConfig()[0].id);
    expect(metadataReq.request.method).toBe('GET');
    metadataReq.flush(dropDownMetaData);
    expect(Object.keys(component.fieldMappingMetaData)).toEqual(Object.keys(dropDownMetaData.data));
  });

  it('should open/close the dropdown dialog', () => {
    component.selectedToolConfig = fakeSelectedTool;
    spyOn(httpService, 'getKPIConfigMetadata').and.callThrough();
    component.getDropdownData();
    // fixture.detectChanges();
    expect(httpService.getKPIConfigMetadata).toHaveBeenCalledTimes(1);
    const metadataReq = httpMock.expectOne(baseUrl + '/api/editConfig/jira/editKpi/' + sharedService.getSelectedToolConfig()[0].id);
    expect(metadataReq.request.method).toBe('GET');
    metadataReq.flush(dropDownMetaData);

    component.initializeFields();
    fixture.detectChanges();
    component.showDialogToAddValue(false, 'jiraIssueTypeNames', 'Issue_Type');
    fixture.detectChanges();
    expect(component.displayDialog).toBeTruthy();
    expect(component.fieldMappingMultiSelectValues).toEqual(dropDownMetaData.data.Issue_Type);

    component.showDialogToAddValue(true, 'epicCostOfDelay', 'fields');
    fixture.detectChanges();
    expect(component.displayDialog).toBeTruthy();
    expect(component.fieldMappingMultiSelectValues).toEqual(dropDownMetaData.data.fields);

    component.showDialogToAddValue(true, 'storyFirstStatus', 'workflow');
    fixture.detectChanges();
    expect(component.displayDialog).toBeTruthy();
    expect(component.fieldMappingMultiSelectValues).toEqual(dropDownMetaData.data.workflow);

    component.showDialogToAddValue(true, 'jiraLiveStatus', 'workflow');
    fixture.detectChanges();
    expect(component.displayDialog).toBeTruthy();
    expect(component.fieldMappingMultiSelectValues).toEqual(dropDownMetaData.data.workflow);

    component.showDialogToAddValue(false, 'ticketCountIssueType', 'Issue_Link');
    fixture.detectChanges();
    expect(component.displayDialog).toBeTruthy();
    expect(component.fieldMappingMultiSelectValues).toEqual(dropDownMetaData.data.Issue_Link);

    component.cancelDialog();
    fixture.detectChanges();
    expect(component.displayDialog).toBeFalsy();
  });



  it('should save fieldmappings', () => {
    component.ngOnInit();
    component.save();
    // fixture.detectChanges();
    httpMock.match(baseUrl + '/api/tools/' + sharedService.getSelectedToolConfig()[0].id + '/saveMapping')[0].flush(successResponse);
    expect(component.fieldMappingForm.valid).toBeTruthy();
  });

  it('should select values from popup', () => {
    component.singleSelectionDropdown = false;
    component.selectedField = 'jiraTicketTriagedStatus';
    component.fieldMappingMultiSelectValues = [{
      key: 'Open',
      data: 'Open'
    }, {
      key: 'In Progress',
      data: 'In Progress'
    }, {
      key: 'Reopened',
      data: 'Reopened'
    }, {
      key: 'Resolved',
      data: 'Resolved'
    }, {
      key: 'Closed',
      data: 'Closed'
    }, {
      key: 'Dropped',
      data: 'Dropped'
    }, {
      key: 'In Analysis',
      data: 'In Analysis'
    }, {
      key: 'In Development',
      data: 'In Development'
    }, {
      key: 'In Review',
      data: 'In Review'
    }, {
      key: 'Ready for Testing',
      data: 'Ready for Testing'
    }, {
      key: 'In Testing',
      data: 'In Testing'
    }, {
      key: 'Ready for Sign-off',
      data: 'Ready for Sign-off'
    }, {
      key: 'Test Passed',
      data: 'Test Passed'
    }, {
      key: 'Test Failed',
      data: 'Test Failed'
    }, {
      key: 'On Hold',
      data: 'On Hold'
    }, {
      key: 'Ready for Delivery',
      data: 'Ready for Delivery'
    }, {
      key: 'Approved',
      data: 'Approved'
    }, {
      key: 'Rejected',
      data: 'Rejected'
    }, {
      key: 'Reviewing',
      data: 'Reviewing'
    }, {
      key: 'Planning',
      data: 'Planning'
    }, {
      key: 'Retest',
      data: 'Retest'
    }, {
      key: 'Done',
      data: 'Done'
    }, {
      key: 'At Risk',
      data: 'At Risk'
    }, {
      key: 'Missed',
      data: 'Missed'
    }, {
      key: 'Being Drafted',
      data: 'Being Drafted'
    }, {
      key: 'Draft Completed',
      data: 'Draft Completed'
    }, {
      key: 'Changes Needed',
      data: 'Changes Needed'
    }, {
      key: 'More info',
      data: 'More info'
    }, {
      key: 'Fixed',
      data: 'Fixed'
    }, {
      key: 'Pending Info',
      data: 'Pending Info'
    }, {
      key: 'READY FOR WORK',
      data: 'READY FOR WORK'
    }, {
      key: 'Ready',
      data: 'Ready'
    }, {
      key: 'Not Ready',
      data: 'Not Ready'
    }, {
      key: 'Pending Backlog Prioritization',
      data: 'Pending Backlog Prioritization'
    }, {
      key: 'Ready for Release',
      data: 'Ready for Release'
    }, {
      key: 'Active',
      data: 'Active'
    }, {
      key: 'Request in progress with Tech team',
      data: 'Request in progress with Tech team'
    }, {
      key: 'Blocked',
      data: 'Blocked'
    }, {
      key: 'Fail',
      data: 'Fail'
    }, {
      key: 'Pass',
      data: 'Pass'
    }, {
      key: 'Accepted',
      data: 'Accepted'
    }, {
      key: 'In Rework',
      data: 'In Rework'
    }, {
      key: 'UAT Sign Off',
      data: 'UAT Sign Off'
    }, {
      key: 'Obsolete',
      data: 'Obsolete'
    }, {
      key: 'No Run',
      data: 'No Run'
    }, {
      key: 'Awaiting Approval',
      data: 'Awaiting Approval'
    }, {
      key: 'Being Reviewed',
      data: 'Being Reviewed'
    }, {
      key: 'Deploying to Production',
      data: 'Deploying to Production'
    }, {
      key: 'Sign Off - Internal',
      data: 'Sign Off - Internal'
    }, {
      key: 'Sign Off - Business',
      data: 'Sign Off - Business'
    }, {
      key: 'Ready For Deployment',
      data: 'Ready For Deployment'
    }, {
      key: 'Validating on Staging',
      data: 'Validating on Staging'
    }, {
      key: 'In UAT',
      data: 'In UAT'
    }, {
      key: 'Pending PO Input',
      data: 'Pending PO Input'
    }, {
      key: 'Sprint Planning',
      data: 'Sprint Planning'
    }, {
      key: 'Ready for Informal Testing',
      data: 'Ready for Informal Testing'
    }, {
      key: 'Ready for Formal Testing',
      data: 'Ready for Formal Testing'
    }, {
      key: 'Review',
      data: 'Review'
    }, {
      key: 'In SAT',
      data: 'In SAT'
    }, {
      key: 'In triage',
      data: 'In triage'
    }, {
      key: 'Assigned',
      data: 'Assigned'
    }, {
      key: 'Escalate',
      data: 'Escalate'
    }, {
      key: 'Ready for Sprint Planning',
      data: 'Ready for Sprint Planning'
    }, {
      key: 'Requirement Signed Off',
      data: 'Requirement Signed Off'
    }, {
      key: 'Pending Owner Action',
      data: 'Pending Owner Action'
    }, {
      key: 'In Intake',
      data: 'In Intake'
    }, {
      key: 'Pending User Response',
      data: 'Pending User Response'
    }, {
      key: 'In Investigation',
      data: 'In Investigation'
    }, {
      key: 'In Backlog Refinement',
      data: 'In Backlog Refinement'
    }, {
      key: 'Backlog',
      data: 'Backlog'
    }, {
      key: 'Request with IT Team',
      data: 'Request with IT Team'
    }, {
      key: 'Funnel',
      data: 'Funnel'
    }, {
      key: 'Analyzing',
      data: 'Analyzing'
    }, {
      key: 'Implementing',
      data: 'Implementing'
    }, {
      key: 'Testing',
      data: 'Testing'
    }, {
      key: 'Need Info',
      data: 'Need Info'
    }, {
      key: 'Releasing',
      data: 'Releasing'
    }];
    component.ngOnInit();
    component.fieldMappingForm.controls[component.selectedField].setValue([]);
    fixture.detectChanges();
    component.selectedMultiValue = [{
      key: 'Resolved',
      data: 'Resolved'
    }, {
      key: 'Closed',
      data: 'Closed'
    }];
    component.saveDialog();
    fixture.detectChanges();
    console.log(component.fieldMappingForm.controls[component.selectedField].value);
    expect(component.fieldMappingForm.controls[component.selectedField].value).toEqual(['Resolved', 'Closed']);
    expect(component.populateDropdowns).toBeFalsy();
    expect(component.displayDialog).toBeFalsy();
  });

  it('should add additional filters mapping controls', () => {
    component.ngOnInit();
    component.additionalFiltersArray = [];
    component.additionalFilterIdentifier = {
      name: 'Squad',
      code: 'sqd'
    };
    fixture.detectChanges();
    component.addAdditionalFilterMappings();
    fixture.detectChanges();
    expect(component.fieldMappingForm.controls[component.additionalFilterIdentifier.code + 'Identifier']).toBeTruthy();
  });

  it('should render form controls according to type selected by user', () => {
    let event = {
      originalEvent: {
        isTrusted: true
      },
      value: 'Component'
    };
    component.additionalFilterIdentifier = {
      name: 'Squad',
      code: 'sqd'
    };
    fixture.detectChanges();
    component.changeControl(event, component.additionalFilterIdentifier);
    fixture.detectChanges();
    expect(component.fieldMappingForm.controls[component.additionalFilterIdentifier.code + 'IdentMultiValue']).toBeTruthy();

    event = {
      originalEvent: {
        isTrusted: true
      },
      value: 'CustomField'
    };
    component.changeControl(event, component.additionalFilterIdentifier);
    fixture.detectChanges();
    expect(component.fieldMappingForm.controls[component.additionalFilterIdentifier.code + 'IdentSingleValue']).toBeTruthy();
  });

  it('should remove additional filter mapping controls on click of remove button', () => {
    const filter = {
      name: 'Squad',
      code: 'sqd'
    };
    component.ngOnInit();
    component.additionalFiltersArray = [];
    component.additionalFilterIdentifier = {
      name: 'Squad',
      code: 'sqd'
    };
    component.addAdditionalFilterMappings();
    fixture.detectChanges();
    component.removeAdditionFilterMapping(filter);
    fixture.detectChanges();
    expect(component.fieldMappingForm.controls[component.additionalFilterIdentifier.code + 'Identifier']).toBeFalsy();
    expect(component.fieldMappingForm.controls[component.additionalFilterIdentifier.code + 'IdentSingleValue']).toBeFalsy();
    expect(component.fieldMappingForm.controls[component.additionalFilterIdentifier.code + 'IdentMultiValue']).toBeFalsy();
    expect(component.additionalFiltersArray).toEqual([]);
  });

  it('should generate additional field mapping controls when previously saved mappings are loaded', () => {
    sharedService.setSelectedFieldMapping(fakeSelectedFieldMappingWithAdditionalFilters);
    component.ngOnInit();
    fixture.detectChanges();
    expect(component.fieldMappingForm.controls['sqdIdentifier']).toBeTruthy();
    expect(component.fieldMappingForm.controls[ 'sqdIdentMultiValue']).toBeTruthy();
  });

  it('should save form with additional filters configured by user', () => {
    sharedService.setSelectedFieldMapping(fakeSelectedFieldMappingWithAdditionalFilters);
    component.ngOnInit();
    component.save();
    // fixture.detectChanges();
    httpMock.match(baseUrl + '/api/tools/' + sharedService.getSelectedToolConfig()[0].id + '/saveMapping')[0].flush(successResponse);
    expect(component.fieldMappingForm.valid).toBeTruthy();
  });

});
