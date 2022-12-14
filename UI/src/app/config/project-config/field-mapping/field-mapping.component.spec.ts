/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FieldMappingComponent } from './field-mapping.component';
import { MessageService } from 'primeng/api';
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
import { InputNumberModule } from 'primeng/inputnumber';
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
      hierarchyLevelId: 'account',
      hierarchyLevelName: 'Account Name'
    },
    {
      id: '63244d35d1d9f4caf85056fa',
      level: 4,
      hierarchyLevelId: 'subaccount',
      hierarchyLevelName: 'Subaccount'
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
      hierarchyLevelId: 'account',
      hierarchyLevelName: 'Account Name'
    },
    {
      id: '63244d35d1d9f4caf85056fa',
      level: 4,
      hierarchyLevelId: 'subaccount',
      hierarchyLevelName: 'Subaccount'
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
  id: '63282c82487eff1e8b70b1b9',
  name: 'TestMAP',
  type: 'Scrum',
  corporate: 'Leve1',
  business: 'Leve2',
  account: 'Level3',
  subaccount: 'Level4'
};

const fakeSelectedFieldMapping = {
  "id": "63282cbaf5c740241aff32a1",
  "projectToolConfigId": "63282ca6487eff1e8b70b1bb",
  "basicProjectConfigId": "63282c82487eff1e8b70b1b9",
  "sprintName": "customfield_12700",
  "jiradefecttype": [
    "Defect"
  ],
  "defectPriority": [],
  "jiraIssueTypeNames": [
    "Story",
    "Enabler Story",
    "Change request",
    "Defect",
    "Epic"
  ],
  "storyFirstStatus": "Open",
  "rootCause": "customfield_19121",
  "jiraStatusForDevelopment": [
    "Implementing",
    "In Development",
    "In Analysis"
  ],
  "jiraIssueEpicType": [
    "Epic"
  ],
  "jiraStatusForQa": [
    "In Testing"
  ],
  "jiraDefectInjectionIssueType": [
    "Story",
    "Enabler Story",
    "Change request"
  ],
  "jiraDod": [
    "Closed",
    "Ready for Delivery"
  ],
  "jiraDefectCreatedStatus": "Open",
  "jiraTechDebtIssueType": [
    "Story",
    "Enabler Story",
    "Change request"
  ],
  "jiraTechDebtIdentification": "",
  "jiraTechDebtCustomField": "",
  "jiraTechDebtValue": [],
  "jiraDefectRejectionStatus": "Closed",
  "issueStatusExcluMissingWork":  [
    "Open",
  ],
  "jiraBugRaisedByIdentification": "",
  "jiraBugRaisedByValue": [],
  "jiraDefectSeepageIssueType": [
    "Story",
    "Enabler Story",
    "Change request"
  ],
  "jiraBugRaisedByCustomField": "",
  "jiraDefectRemovalStatus": [
    "Closed",
    "Ready for Delivery"
  ],
  "jiraDefectRemovalIssueType": [
    "Story",
    "Enabler Story",
    "Change request"
  ],
  "jiraStoryPointsCustomField": "customfield_20803",
  "jiraTestAutomationIssueType": [
    "Story",
    "Enabler Story",
    "Change request"
  ],
  "jiraCanNotAutomatedTestValue": [],
  "jiraSprintVelocityIssueType": [
    "Story",
    "Enabler Story",
    "Change request"
  ],
  "jiraSprintCapacityIssueType": [
    "Story",
    "Enabler Story",
    "Change request"
  ],
  "jiraDefectRejectionlIssueType": [
    "Story",
    "Enabler Story",
    "Change request"
  ],
  "jiraDefectCountlIssueType": [
    "Story",
    "Enabler Story",
    "Change request"
  ],
  "jiraIssueDeliverdStatus": [
    "Closed",
    "Resolved"
  ],
  "jiraDor": "Ready for Sprint Planning",
  "jiraIntakeToDorIssueType": [
    "Story",
    "Enabler Story",
    "Change request"
  ],
  "jiraStoryIdentification": [
    "Story",
    "Enabler Story",
    "Change request"
  ],
  "jiraLiveStatus": "",
  "regressionAutomationLabels": [],
  "rootCauseValue": [
    "Coding"
  ],
  "excludeRCAFromFTPR": [],
  "resolutionTypeForRejection": [
    "Invalid",
    "Duplicate",
    "Unrequired"
  ],
  "jiraQADefectDensityIssueType": [
    "Story",
    "Enabler Story",
    "Change request"
  ],
  "jiraBugRaisedByQACustomField": "",
  "jiraBugRaisedByQAIdentification": "",
  "jiraBugRaisedByQAValue": [],
  "jiraDefectDroppedStatus": [],
  "epicCostOfDelay": "customfield_58102",
  "epicRiskReduction": "customfield_58101",
  "epicUserBusinessValue": "customfield_58100",
  "epicWsjf": "customfield_58104",
  "epicTimeCriticality": "customfield_51002",
  "epicJobSize": "customfield_61041",
  "productionDefectCustomField": "",
  "productionDefectIdentifier": "",
  "productionDefectValue": [],
  "productionDefectComponentValue": "",
  "jiraTestCaseType": [],
  "testAutomatedIdentification": "",
  "testAutomationCompletedIdentification": "",
  "testRegressionIdentification": "",
  "testAutomated": "",
  "testAutomationCompletedByCustomField": "",
  "testRegressionByCustomField": "",
  "jiraAutomatedTestValue": [],
  "jiraRegressionTestValue": [],
  "jiraCanBeAutomatedTestValue": [],
  "estimationCriteria": "Story Point",
  "storyPointToHourMapping": 8,
  "workingHoursDayCPT": 6,
  "additionalFilterConfig": [

  ]
};
const successResponse = {
  message: 'field mappings added successfully',
  success: true,
  data: {
    id: '63282cbaf5c740241aff32a1',
    projectToolConfigId: '63282ca6487eff1e8b70b1bb',
    basicProjectConfigId: '63282c82487eff1e8b70b1b9',
    sprintName: 'customfield_12700',
    jiradefecttype: [
      'Defect'
    ],
    defectPriority: [],
    jiraIssueTypeNames: [
      'Story',
      'Enabler Story',
      'Change request',
      'Defect',
      'Epic'
    ],
    storyFirstStatus: 'Open',
    rootCause: 'customfield_19121',
    jiraStatusForDevelopment: [
      'Implementing',
      'In Development',
      'In Analysis'
    ],
    jiraIssueEpicType: [
      'Epic'
    ],
    jiraStatusForQa: [
      'In Testing'
    ],
    jiraDefectInjectionIssueType: [
      'Story',
      'Enabler Story',
      'Change request'
    ],
    jiraDod: [
      'Closed',
      'Ready for Delivery'
    ],
    "jiraDefectCreatedStatus": "Open",
    "issueStatusExcluMissingWork": ["Open"],
    "jiraTechDebtIssueType": [
      "Story",
      "Enabler Story",
      "Change request"
    ],
    jiraTechDebtIdentification: '',
    jiraTechDebtCustomField: '',
    jiraTechDebtValue: [],
    jiraDefectRejectionStatus: 'Closed',
    jiraBugRaisedByIdentification: '',
    jiraBugRaisedByValue: [],
    jiraDefectSeepageIssueType: [
      'Story',
      'Enabler Story',
      'Change request'
    ],
    jiraBugRaisedByCustomField: '',
    jiraDefectRemovalStatus: [
      'Closed',
      'Ready for Delivery'
    ],
    jiraDefectRemovalIssueType: [
      'Story',
      'Enabler Story',
      'Change request'
    ],
    jiraStoryPointsCustomField: 'customfield_20803',
    jiraTestAutomationIssueType: [
      'Story',
      'Enabler Story',
      'Change request'
    ],
    jiraCanNotAutomatedTestValue: [],
    jiraSprintVelocityIssueType: [
      'Story',
      'Enabler Story',
      'Change request'
    ],
    jiraSprintCapacityIssueType: [
      'Story',
      'Enabler Story',
      'Change request'
    ],
    jiraDefectRejectionlIssueType: [
      'Story',
      'Enabler Story',
      'Change request'
    ],
    jiraDefectCountlIssueType: [
      'Story',
      'Enabler Story',
      'Change request'
    ],
    jiraIssueDeliverdStatus: [
      'Closed',
      'Resolved'
    ],
    jiraDor: 'Ready for Sprint Planning',
    jiraIntakeToDorIssueType: [
      'Story',
      'Enabler Story',
      'Change request'
    ],
    jiraStoryIdentification: [
      'Story',
      'Enabler Story',
      'Change request'
    ],
    jiraLiveStatus: '',
    regressionAutomationLabels: [],
    rootCauseValue: [
      'Coding'
    ],
    excludeRCAFromFTPR: [],
    resolutionTypeForRejection: [
      'Invalid',
      'Duplicate',
      'Unrequired'
    ],
    jiraQADefectDensityIssueType: [
      'Story',
      'Enabler Story',
      'Change request'
    ],
    jiraBugRaisedByQACustomField: '',
    jiraBugRaisedByQAIdentification: '',
    jiraBugRaisedByQAValue: [],
    jiraDefectDroppedStatus: [],
    epicCostOfDelay: 'customfield_58102',
    epicRiskReduction: 'customfield_58101',
    epicUserBusinessValue: 'customfield_58100',
    epicWsjf: 'customfield_58104',
    epicTimeCriticality: 'customfield_51002',
    epicJobSize: 'customfield_61041',
    productionDefectCustomField: '',
    productionDefectIdentifier: '',
    productionDefectValue: [],
    productionDefectComponentValue: '',
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
    ]
  }
};

const fakeSelectedFieldMappingWithAdditionalFilters = {
  "storyFirstStatus": "Open",
  "jiraDefectCreatedStatus": "Open",
  "jiraDefectDroppedStatus": [],
  "jiraLiveStatus": "",
  "jiraDor": "Ready for Sprint Planning",
  "jiraDefectRejectionStatus": "Closed",
  "jiraDod": [
    "Closed",
    "Ready for Delivery"
  ],
  "jiraIssueDeliverdStatus": [
    "Closed",
    "Ready for Delivery"
  ],
  "jiraDefectRemovalStatus": [
    "Closed",
    "Ready for Delivery"
  ],
  "issueStatusExcluMissingWork": ["Open"],
  "resolutionTypeForRejection": [
    "Invalid",
    "Duplicate",
    "Unrequired"
  ],
  "jiraStatusForDevelopment": [
    "Implementing",
    "In Development",
    "In Analysis"
  ],
  "jiraStatusForQa": [
    "In Testing"
  ],
  "jiraIssueTypeNames": [
    "Story",
    "Enabler Story",
    "Change request",
    "Defect",
    "Epic"
  ],
  "jiraDefectSeepageIssueType": [
    "Story",
    "Enabler Story",
    "Change request"
  ],
  "jiraQADefectDensityIssueType": [
    "Story",
    "Enabler Story",
    "Change request"
  ],
  "jiraDefectCountlIssueType": [
    "Story",
    "Enabler Story",
    "Change request"
  ],
  "jiraSprintVelocityIssueType": [
    "Story",
    "Enabler Story",
    "Change request"
  ],
  "jiraDefectRemovalIssueType": [
    "Story",
    "Enabler Story",
    "Change request"
  ],
  "jiraDefectRejectionlIssueType": [
    "Story",
    "Enabler Story",
    "Change request"
  ],
  "jiraDefectInjectionIssueType": [
    "Story",
    "Enabler Story",
    "Change request"
  ],
  "jiraTestAutomationIssueType": [
    "Story",
    "Enabler Story",
    "Change request"
  ],
  "jiraIntakeToDorIssueType": [
    "Story",
    "Enabler Story",
    "Change request"
  ],
  "jiraTechDebtIssueType": [
    "Story",
    "Enabler Story",
    "Change request"
  ],
  "jiraStoryIdentification": [
    "Story",
    "Enabler Story",
    "Change request"
  ],
  "jiraSprintCapacityIssueType": [
    "Story",
    "Enabler Story",
    "Change request"
  ],
  "jiraIssueEpicType": [
    "Epic"
  ],
  "jiraTechDebtIdentification": "",
  "jiraTechDebtValue": [],
  "jiraTechDebtCustomField": "",
  "sprintName": "customfield_12700",
  "rootCause": "customfield_19121",
  "jiraStoryPointsCustomField": "customfield_20803",
  "estimationCriteria": "Story Point",
  "storyPointToHourMapping": 8,
  "epicCostOfDelay": "customfield_58102",
  "epicRiskReduction": "customfield_58101",
  "epicUserBusinessValue": "customfield_58100",
  "epicWsjf": "customfield_58104",
  "epicTimeCriticality": "customfield_51002",
  "epicJobSize": "customfield_61041",
  "workingHoursDayCPT": 6,
  "jiradefecttype": [
    "Defect"
  ],
  "defectPriority": [],
  "jiraBugRaisedByIdentification": "",
  "jiraBugRaisedByCustomField": "",
  "jiraBugRaisedByValue": [],
  "jiraBugRaisedByQAIdentification": "",
  "jiraBugRaisedByQACustomField": "",
  "jiraBugRaisedByQAValue": [],
  "productionDefectCustomField": "",
  "productionDefectIdentifier": "",
  "productionDefectComponentValue": "",
  "productionDefectValue": [],
  "rootCauseValue": [
    "Coding"
  ],
  "testAutomatedIdentification": "",
  "testAutomationCompletedIdentification": "",
  "testRegressionIdentification": "",
  "testAutomated": "",
  "testAutomationCompletedByCustomField": "",
  "testRegressionByCustomField": "",
  "jiraCanBeAutomatedTestValue": [],
  "jiraRegressionTestValue": [],
  "jiraCanNotAutomatedTestValue": [],
  "jiraAutomatedTestValue": [],
  "jiraTestCaseType": [],
  "regressionAutomationLabels": [],
  "excludeRCAFromFTPR": [],
  "basicProjectConfigId": "63282c82487eff1e8b70b1b9",
  "additionalFilterConfig": [
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
const dropDownMetaData = require('../../../../test/resource/KPIConfig.json');

describe('FieldMappingComponent', () => {
  let component: FieldMappingComponent;
  let fixture: ComponentFixture<FieldMappingComponent>;
  let getAuthorizationService: GetAuthorizationService;
  let sharedService: SharedService;
  let httpService: HttpService;
  let httpMock;
  const baseUrl = environment.baseUrl;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [FieldMappingComponent],
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
        InputNumberModule,
        RadioButtonModule,
        BadgeModule
      ],
      providers: [
        HttpService,
        SharedService,
        MessageService,
        GetAuthorizationService,
        { provide: APP_CONFIG, useValue: AppConfig }
      ]
    })
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FieldMappingComponent);
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
    localStorage.setItem('projectsAccess', JSON.stringify([]));
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
    // fixture.detectChanges();
    expect(component.displayDialog).toBeTruthy();
    expect(component.fieldMappingMultiSelectValues).toEqual(dropDownMetaData.data.fields);

    component.showDialogToAddValue(true, 'storyFirstStatus', 'workflow');
    fixture.detectChanges();
    expect(component.displayDialog).toBeTruthy();
    expect(component.fieldMappingMultiSelectValues).toEqual(dropDownMetaData.data.workflow);

    component.showDialogToAddValue(false, 'jiradefecttype', 'Issue_Link');
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
    httpMock.match(baseUrl + '/api/tools/' + sharedService.getSelectedToolConfig()[0].id + '/fieldMapping')[0].flush(successResponse);
    expect(component.fieldMappingForm.valid).toBeTruthy();
  });

  it('should select values from popup', () => {
    component.singleSelectionDropdown = false;
    component.selectedField = 'jiraIssueDeliverdStatus';
    component.fieldMappingMultiSelectValues = [{
      key: 'New',
      data: 'New'
    }, {
      key: 'Active',
      data: 'Active'
    }, {
      key: 'Resolved',
      data: 'Resolved'
    }, {
      key: 'Closed',
      data: 'Closed'
    }, {
      key: 'Removed',
      data: 'Removed'
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
    }, {
      key: 'Removed',
      data: 'Removed'
    }];
    component.saveDialog();
    fixture.detectChanges();
    expect(component.fieldMappingForm.controls[component.selectedField].value).toEqual(['Resolved', 'Closed', 'Removed']);
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
    component.changeControl(event,component.additionalFilterIdentifier);
    fixture.detectChanges();
    expect(component.fieldMappingForm.controls[component.additionalFilterIdentifier.code + 'IdentMultiValue']).toBeTruthy();

    event = {
      originalEvent: {
        isTrusted: true
      },
      value: 'CustomField'
    };
    component.changeControl(event,component.additionalFilterIdentifier);
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
    httpMock.match(baseUrl + '/api/tools/' + sharedService.getSelectedToolConfig()[0].id + '/fieldMapping')[0].flush(successResponse);
    expect(component.fieldMappingForm.valid).toBeTruthy();
  });
});
