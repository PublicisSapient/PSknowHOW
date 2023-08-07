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

import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import { FieldMappingComponent } from './field-mapping.component';
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
import { InputNumberModule } from 'primeng/inputnumber';
import { environment } from 'src/environments/environment';
import { RadioButtonModule } from 'primeng/radiobutton';
import { BadgeModule } from 'primeng/badge';
import { of } from 'rxjs';

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
const fakeKpiFieldMappingList = require('../../../../test/resource/fakeMappingFieldConfig.json');

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
      ],
      providers: [
        HttpService,
        SharedService,
        MessageService,
        ConfirmationService,
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
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should fetch dropdown metadata on load', () => {
    component.selectedToolConfig = fakeSelectedTool;
    spyOn(httpService, 'getKPIConfigMetadata').and.callThrough();
    component.getDropdownData();
    expect(httpService.getKPIConfigMetadata).toHaveBeenCalledTimes(1);
    const metadataReq = httpMock.expectOne(baseUrl + '/api/editConfig/jira/editKpi/' + sharedService.getSelectedToolConfig()[0].id);
    expect(metadataReq.request.method).toBe('GET');
    metadataReq.flush(dropDownMetaData);
    expect(Object.keys(component.fieldMappingMetaData)).toEqual(Object.keys(dropDownMetaData.data));
  });

 

  it('should initialize component', () => {
    sharedService.setSelectedFieldMapping(fakeSelectedFieldMappingWithAdditionalFilters);
    component.ngOnInit();
    const spy = spyOn(component,'getMappings').and.callThrough();;
    expect(spy).toBeDefined();
  });

  it('should get getKPIFieldMappingRelationships', fakeAsync(() => {
    spyOn(httpService, 'getKPIFieldMappingConfig').and.returnValue(of(fakeKpiFieldMappingList));
    component.getKPIFieldMappingRelationships();
    tick();
    expect(component.fieldMappingConfig.length).toEqual(fakeKpiFieldMappingList.data.fieldConfiguration.length);
  }));

  it('should upload and process file correctly', () => {
    const fileName = 'test-file.json';
    const fileContent = JSON.stringify({ example: 'data' });
    const file = new File([fileContent], fileName, { type: 'application/json' });

    // Simulate file input change event
    const event = {
      target: {
        files: [file]
      }
    };

    const spy = spyOn(component,'getMappings').and.callThrough();;
    component.onUpload(event);
    expect(spy).toBeDefined();
  });

});
