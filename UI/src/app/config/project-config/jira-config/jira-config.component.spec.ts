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

import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import { JiraConfigComponent } from './jira-config.component';
import { MessageService, ConfirmationService } from 'primeng/api';
import { HttpService } from '../../../services/http.service';
import { SharedService } from '../../../services/shared.service';
import { GetAuthorizationService } from '../../../services/get-authorization.service';
import { FormGroup, ReactiveFormsModule, FormsModule, FormBuilder, FormControl } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HttpClientModule } from '@angular/common/http';
import { APP_CONFIG, AppConfig } from '../../../services/app.config';
import { Router, NavigationEnd, ActivatedRoute, Params, Data } from '@angular/router';

import { InputSwitchModule } from 'primeng/inputswitch';
import { ChipsModule } from 'primeng/chips';
import { ToastModule } from 'primeng/toast';
import { TableModule } from 'primeng/table';
import { TooltipModule } from 'primeng/tooltip';
import { AutoCompleteModule } from 'primeng/autocomplete';
import {DropdownModule} from 'primeng/dropdown';

import { environment } from 'src/environments/environment';
import { Observable } from 'rxjs';
import { of } from 'rxjs';
import { CommonModule } from '@angular/common';

describe('JiraConfigComponent', () => {
  let component: JiraConfigComponent;
  let fixture: ComponentFixture<JiraConfigComponent>;
  let sharedService: SharedService;
  let httpService: HttpService;
  let httpMock;
  let router;
  const baseUrl = environment.baseUrl;
  const mockActivatedRoute = {
    queryParams: of({ toolName: 'Jira' })
  };
  const fakeFetchBoards = require('../../../../test/resource/fakeFetchBoards.json');
  const fakeSonarVersionsList = require('../../../../test/resource/fakeSonarVersionsList.json');
  const fakeJiraConnections = require('../../../../test/resource/fakeJiraConnections.json');
  const fakeBambooPlans = require('../../../../test/resource/fakeBambooPlans.json');
  const fakeDeploymentProjects = require('../../../../test/resource/fakeDeploymentProjects.json');
  const fakeJenkinsJobNames = require('../../../../test/resource/fakeJenkinsJobNames.json');
  const fakeAzurePipelinesList = require('../../../../test/resource/fakeAzurePipelinesList.json');
  const fakeProjectKeyList = require('../../../../test/resource/fakeProjectKeyList.json');
  const fakeBranchesForProject = require('../../../../test/resource/fakeBranchesForProject.json');
  const fakeConfiguredTools = require('../../../../test/resource/fakeConfiguredTools.json');
  const fakeSelectedTool = [{
    id: '5fc086b9410df80001701334',
    toolName: 'Jira',
    basicProjectConfigId: '5fc0867e410df8000170132e',
    connectionId: '5fc0857c410df80001701327',
    projectId: '23932',
    projectKey: 'Test1',
    createdAt: '2020-11-27T04:55:21',
    updatedAt: '2020-11-27T04:55:21',
    queryEnabled: true,
    boardQuery: '',
    metadataTemplateID : "641d986af8d42d02b0c2558f"
  }];
  const fakeBranchListForProject = require('../../../../test/resource/fakeBranchListForProject.json')
  const fakeProject = {
    id: '6335363749794a18e8a4479b',
    name: 'Scrum Project',
    type: 'Scrum',
    hierarchyLevelOne: 'Sample One',
    hierarchyLevelTwo: 'Sample Two',
    hierarchyLevelThree: 'Sample Three'
  };
  const successResponse = {
    message: 'created and saved new project_tools',
    success: true,
    data: {
      id: '5fca41ef193e9300010c87d9',
      toolName: 'Jira',
      basicProjectConfigId: '5fca113c193e9300010c87ce',
      connectionId: '5fc643cd11193836e6545560',
      projectKey: '1212',
      createdAt: '2020-12-04T14:04:31',
      updatedAt: '2020-12-04T14:04:31',
      queryEnabled: true,
      boardQuery: 'Test_Query'
    }
  };
  const selectedConnection = {
    id: '5fc643cd11193836e6545560',
    type: 'Jira',
    connectionName: 'DOJO Transformation Internal -Jira Connection',
    cloudEnv: false,
    baseUrl: 'http://testabc.com/jira',
    username: '',
    password: '****',
    apiEndPoint: 'rest/api/2/',
    consumerKey: '',
    privateKey: '',
    isOAuth: false,
    offline: true,
    offlineFilePath: ''
  };

  const fakeTemplateList = [ {
      id: "641cc51bd830154a05d77370",
      tool: "Jira",
      templateName: "DOJO Studio Template",
      templateCode: "6",
      kanban: false
  },
  {
      id: "641cc51bd830154a05d77371",
      tool: "Jira",
      templateName: "Standard Template",
      templateCode: "7",
      kanban: false
  },
  {
      id: "641cc51bd830154a05d77372",
      tool: "Jira",
      templateName: "Standard Template",
      templateCode: "8",
      kanban: true
  },
  {
      id: "641cc51bd830154a05d77371",
      tool: "Jira",
      templateName: "Custom Template",
      templateCode: "7",
      kanban: true
  },
];

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [JiraConfigComponent],
      imports: [
        FormsModule,
        ReactiveFormsModule,
        RouterTestingModule,
        HttpClientTestingModule,
        InputSwitchModule,
        ChipsModule,
        AutoCompleteModule,
        DropdownModule,
        ToastModule,
        TableModule,
        TooltipModule,
        CommonModule
      ],
      providers: [
        HttpService,
        SharedService,
        MessageService,
        ConfirmationService,
        GetAuthorizationService,
        { provide: APP_CONFIG, useValue: AppConfig },
        { provide: ActivatedRoute, useValue: mockActivatedRoute }
      ]
    })
      .compileComponents();

    fixture = TestBed.createComponent(JiraConfigComponent);
    component = fixture.componentInstance;
    httpService = TestBed.inject(HttpService);
    sharedService = TestBed.inject(SharedService);
    sharedService.setSelectedProject(fakeProject);
    sharedService.setSelectedToolConfig(fakeSelectedTool);
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
  });


  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should recieve url parameter in urlParam property', () => {
    spyOnProperty(router, 'url', 'get').and.returnValue('/dashboard/Config/JiraConfig?toolName=Jira');
    // component.ngOnInit();
    // router.url = '/dashboard/Config/JiraConfig';
    // const navigateSpy = spyOn(router, 'navigate');
    component.ngOnInit();
    expect(component.urlParam).toBe('Jira');
    // expect(navigateSpy).toHaveBeenCalledWith(['./dashboard/Config/ProjectList']);
  });

  it('should fetch connection list', () => {
    spyOn(httpService, 'getAllConnectionTypeBased').and.callThrough();
    spyOn(sharedService, 'getSelectedToolConfig').and.callThrough();
    component.ngOnInit();
    // component.getConnectionList(component.urlParam);
    expect(httpService.getAllConnectionTypeBased).toHaveBeenCalledTimes(1);
    // function is actually being called 3 times
    expect(sharedService.getSelectedToolConfig).toHaveBeenCalledTimes(3);

    const connectionsReq = httpMock.expectOne(`${baseUrl}/api/connections?type=${component.urlParam}`);
    expect(connectionsReq.request.method).toBe('GET');
    connectionsReq.flush(fakeJiraConnections);
    fixture.detectChanges();
  });

  it('should save form', () => {
    component.ngOnInit();
    component.toolForm.controls['projectKey'].setValue('1212');
    component.toolForm.controls['metadataTemplateCode'].setValue({
      id:"641d986af8d42d02b0c2558f",
      kanban:true,
      templateCode: "1",
      templateName:"DOJO Agile Template",
      tool: "Jira"
    });
   
    component.queryEnabled =true;
    component.toolForm.controls['boardQuery'].setValue(`Project = DTS AND component = Panthers AND issuetype in (Story, Defect, "Enabler Story", "Change request", Dependency, Epic, Task, "Studio Job", "Studio Task") and  created > '2022/03/01 00:00'`);
    component.isEdit = false;
    component.selectedConnection = selectedConnection;

    component.save();
    fixture.detectChanges();
    httpMock.match(`${baseUrl}/api/basicconfigs/${sharedService.getSelectedProject().id}/tools`)[0].flush(successResponse);
  });

  it('should submit edit form', () => {
    component.ngOnInit();
    component.toolForm.controls['projectKey'].setValue('1212');
    component.queryEnabled =true;
    component.toolForm.controls['boardQuery'].setValue(`Project = DTS AND component = Panthers AND issuetype in (Story, Defect, "Enabler Story", "Change request", Dependency, Epic, Task, "Studio Job", "Studio Task") and  created > '2022/03/01 00:00'`);
    component.isEdit = true;
    component.selectedConnection = selectedConnection;


    component.save();
    fixture.detectChanges();
    httpMock.match(`${baseUrl}/api/basicconfigs/${sharedService.getSelectedProject().id}/tools/${component.selectedToolConfig[0].id}`)[0].flush(successResponse);
  });

  it('should get sonar version list', () => {
    component.ngOnInit();
    component.urlParam = 'Sonar';
    const sonarVersionListLen = 11;
    const sonarCloudVersionListLen = 13;
    httpMock.match(`${baseUrl}/api/sonar/version`)[0].flush(fakeSonarVersionsList);
    fixture.detectChanges();
    expect(component.versionList.length).toEqual(fakeSonarVersionsList.data.length);
    expect(component.sonarVersionList.length).toEqual(sonarVersionListLen);
    expect(component.sonarCloudVersionList.length).toEqual(sonarCloudVersionListLen);
  })

  it('should get plans for bamboo', fakeAsync(() => {
    spyOn(component, 'showLoadingOnFormElement').and.callFake(() =>{});
    spyOn(component, 'hideLoadingOnFormElement').and.callFake(() =>{});
    spyOn(httpService, 'getPlansForBamboo').and.returnValue(of(fakeBambooPlans))
    component.getPlansForBamboo('63b2bf2544af1c3bc6553977');
    tick();
    expect(Object.keys(component.bambooProjectDataFromAPI).length).toEqual(fakeBambooPlans.data.length);
  }));

  it('should fail getting plans for bamboo', fakeAsync(() => {
    spyOn(component, 'showLoadingOnFormElement').and.callFake(() =>{});
    spyOn(component, 'hideLoadingOnFormElement').and.callFake(() =>{});
    spyOn(httpService, 'getPlansForBamboo').and.returnValue(of({"message":"No plans found","success":false}))
    component.getPlansForBamboo('63b2bf2544af1c3bc6553977');
    tick();
    expect(Object.keys(component.bambooProjectDataFromAPI).length).toEqual(0);
  }));

  it('should get deployment projects', fakeAsync(() => {
    spyOn(component, 'showLoadingOnFormElement').and.callFake(() =>{});
    spyOn(component, 'hideLoadingOnFormElement').and.callFake(() =>{});
    spyOn(httpService, 'getDeploymentProjectsForBamboo').and.returnValue(of(fakeDeploymentProjects))
    component.getDeploymentProjects('63b2bf2544af1c3bc6553977');
    tick();
    expect(Object.keys(component.deploymentProjectList).length).toEqual(fakeDeploymentProjects.data.length);
  }));

  it('should fail getting deployment projects', fakeAsync(() => {
    spyOn(component, 'showLoadingOnFormElement').and.callFake(() =>{});
    spyOn(component, 'hideLoadingOnFormElement').and.callFake(() =>{});
    spyOn(httpService, 'getDeploymentProjectsForBamboo').and.returnValue(of({'error':'Failed getting projects'}))
    component.getDeploymentProjects('63b2bf2544af1c3bc6553977');
    tick();
    expect(Object.keys(component.deploymentProjectList).length).toEqual(0);
  }));

  it('should get jenkins job names', fakeAsync(() => {
    spyOn(component, 'showLoadingOnFormElement').and.callFake(() =>{});
    spyOn(component, 'hideLoadingOnFormElement').and.callFake(() =>{});
    spyOn(httpService, 'getJenkinsJobNameList').and.returnValue(of(fakeJenkinsJobNames))
    component.getJenkinsJobNames('63b4055f8ec44416b3ce96a8');
    tick();
    expect(Object.keys(component.jenkinsJobNameList).length).toEqual(fakeJenkinsJobNames.data.length);
  }));

  it('should fail getting jenkins job names', fakeAsync(() => {
    spyOn(component, 'showLoadingOnFormElement').and.callFake(() =>{});
    spyOn(component, 'hideLoadingOnFormElement').and.callFake(() =>{});
    spyOn(httpService, 'getJenkinsJobNameList').and.returnValue(of({"message":"No Jobs details found","success":false}))
    component.getJenkinsJobNames('63b4055f8ec44416b3ce96a8');
    tick();
    expect(Object.keys(component.jenkinsJobNameList).length).toEqual(0);
  }));

  it('should get azure build pipelines', fakeAsync(() => {
    const connection = {
      "id": "63809ba89939e165ba1e663f",
    }
    component.selectedConnection = true;
    spyOn(component, 'showLoadingOnFormElement').and.callFake(() =>{});
    spyOn(component, 'hideLoadingOnFormElement').and.callFake(() =>{});
    spyOn(httpService, 'getAzurePipelineList').and.returnValue(of(fakeAzurePipelinesList))
    component.getAzureBuildPipelines(connection);
    tick();
    expect(Object.keys(component.azurePipelineResponseList).length).toEqual(fakeAzurePipelinesList.data.length);
  }));

  it('should fail getting azure build pipelines', fakeAsync(() => {
    const connection = {
      "id": "63809ba89939e165ba1e663f",
    }
    spyOn(component, 'showLoadingOnFormElement').and.callFake(() =>{});
    spyOn(component, 'hideLoadingOnFormElement').and.callFake(() =>{});
    spyOn(httpService, 'getAzurePipelineList').and.returnValue(of({"message":"No Azure Builds found","success":false}))
    component.getAzureBuildPipelines(connection);
    tick();
    expect(Object.keys(component.azurePipelineResponseList).length).toEqual(0);
  }));

  it('should get azure release pipelines', fakeAsync(() => {
    const connection = {
      "id": "63809ba89939e165ba1e663f",
    }
    component.selectedConnection = true;
    spyOn(component, 'showLoadingOnFormElement').and.callFake(() =>{});
    spyOn(component, 'hideLoadingOnFormElement').and.callFake(() =>{});
    spyOn(httpService, 'getAzureReleasePipelines').and.returnValue(of(fakeAzurePipelinesList))
    component.getAzureReleasePipelines(connection);
    tick();
    expect(Object.keys(component.azurePipelineResponseList).length).toEqual(fakeAzurePipelinesList.data.length);
  }));

  it('should fail getting azure release pipelines', fakeAsync(() => {
    const connection = {
      "id": "63809ba89939e165ba1e663f",
    }
    spyOn(component, 'showLoadingOnFormElement').and.callFake(() =>{});
    spyOn(component, 'hideLoadingOnFormElement').and.callFake(() =>{});
    spyOn(httpService, 'getAzureReleasePipelines').and.returnValue(of({"message":"No Azure Builds found","success":false}))
    component.getAzureReleasePipelines(connection);
    tick();
    expect(Object.keys(component.azurePipelineResponseList).length).toEqual(0);
  }));

  it('should enable organization key', () => {
    component.urlParam = 'Sonar';
    component.initializeFields(component.urlParam);
    component.enableDisableOrganizationKey(true);
    expect(component.toolForm.controls['organizationKey'].disabled).toBeFalsy();
  })

  it('should disable organization key', () => {
    component.urlParam = 'Sonar';
    component.initializeFields(component.urlParam);
    component.enableDisableOrganizationKey(false);
    expect(component.toolForm.controls['organizationKey'].disabled).toBeTruthy();
  });

  it('should fetch boards', fakeAsync(() => {
    component.urlParam = 'Jira'
    component.initializeFields(component.urlParam);
    component.selectedConnection = {
      "id": "63b3f8ee8ec44416b3ce9698",
    }
    fixture.detectChanges();
    spyOn(httpService, 'getAllBoards').and.returnValue(of(fakeFetchBoards));
    component.fetchBoards(component);
    tick();
    expect(component.boardsData.length).toEqual(fakeFetchBoards.data.length);
  }));

  it('should clear sonar form', ()=>{
    component.urlParam = 'Sonar';
    component.initializeFields(component.urlParam);
    component.clearSonarForm();
    expect(component.toolForm.controls['organizationKey'].value).toBe('');
    expect(component.toolForm.controls['apiVersion'].value).toBe('');
    expect(component.toolForm.controls['projectKey'].value).toBe('');
    expect(component.toolForm.controls['branch'].value).toBe('');
  });

  it('should handle api version', fakeAsync(()=>{
    component.urlParam = 'Sonar';
    component.selectedConnection = {
      "id": "63b3f8ee8ec44416b3ce9698",
    }
    component.initializeFields(component.urlParam);
    spyOn(component, 'showLoadingOnFormElement').and.callFake(() =>{});
    spyOn(component, 'isVersionSupported').and.returnValue(true);
    spyOn(component, 'hideLoadingOnFormElement').and.callFake(() =>{});
    spyOn(httpService, 'getProjectKeyList').and.returnValue(of(fakeProjectKeyList));
    component.apiVersionHandler('9.x', 'apiVersion');
    tick();
    expect(component.projectKeyList.length).toEqual(fakeProjectKeyList.data.length);
  }))

  it('should handle project key click', fakeAsync(() => {
    component.urlParam = 'Sonar';
    component.selectedConnection = {
      "id": "63b3f8ee8ec44416b3ce9698",
    }
    component.initializeFields(component.urlParam);
    component.disableBranchDropDown = true;
    spyOn(component, 'showLoadingOnFormElement').and.callFake(() =>{});
    spyOn(component, 'hideLoadingOnFormElement').and.callFake(() =>{});
    spyOn(httpService, 'getBranchListForProject').and.returnValue(of(fakeBranchListForProject));
    component.projectKeyClickHandler('ENGINEERING.KPIDASHBOARD.PROCESSORS');
    tick();
    expect(component.branchList.length).toEqual(fakeBranchListForProject.data.length);
  }))

  it('should handle bamboo plan select', fakeAsync(() => {
    component.urlParam = 'Bamboo';
    component.selectedConnection = {
      "id": "63b409e88ec44416b3ce96b3",
    }
    component.initializeFields(component.urlParam);
    component.bambooProjectDataFromAPI = [{
      "jobNameKey": "REL-BAM",
      "projectAndPlanName": "12th oct - bamboo-upgrade"
    }];
    spyOn(component, 'showLoadingOnFormElement').and.callFake(() =>{});
    spyOn(component, 'hideLoadingOnFormElement').and.callFake(() =>{});
    spyOn(httpService, 'getBranchesForProject').and.returnValue(of(fakeBranchListForProject));
    component.bambooPlanSelectHandler('12th oct - bamboo-upgrade', 'planName');
    tick();
    expect(component.bambooBranchDataFromAPI.length).toEqual(fakeBranchListForProject.data.length);
  }))

  it('should delete tool', fakeAsync(() => {
    const tool = {
      "id": "63b5277cf33fd2360e9e72dd",
      "toolName": "Bamboo",
      "basicProjectConfigId": "63b3f9098ec44416b3ce9699",
      "connectionId": "63b409e88ec44416b3ce96b3",
      "connectionName": "Bamboo Connection",
      "jobName": "REL-BAM",
      "jobType": "Build",
      "createdAt": "2023-01-04T07:15:08",
      "updatedAt": "2023-01-04T07:15:08",
      "queryEnabled": false,
      "boards": [
          null
      ]
    };
    component.configuredTools = fakeConfiguredTools;
    spyOn(httpService, 'deleteProjectToolConfig').and.returnValue(of({"message":"Tool deleted successfully","success":true}));
    component.deleteTool(tool);
    tick();
    expect(component.configuredTools).not.toContain(tool);
  }))

  it('should Edit tool', fakeAsync(() => {
    component.urlParam = 'Sonar';
    component.initializeFields(component.urlParam);
    const tool = {
      id: "5fc643cd11193836e6545560",
      toolName: "Bamboo",
      basicProjectConfigId: "63b3f9098ec44416b3ce9699",
      connectionId: "5fc643cd11193836e6545560",
      connectionName: "Bamboo Connection",
      jobName: "REL-BAM",
      jobType: "Build",
      createdAt: "2023-01-04T07:15:08",
      updatedAt: "2023-01-04T07:15:08",
      queryEnabled: false,
      boards: [
          null
      ]
    };
    component.connections = fakeJiraConnections.data;
    component.editTool(tool);
    expect(component.isEdit).toBeTruthy();  
  }))

  it("should add new tool",()=>{
    component.urlParam = 'Sonar';
    component.initializeFields(component.urlParam);
    component.addNewTool();
    expect(component.isEdit).toBeFalse();
  })

  it('should disable loading when connection is already Jira', () => {
   
    const fakeConnection = {
      id: '5fc643cd11193836e6545560',
      type: 'Jira',
      connectionName: 'Test Internal -Jira Connection',
      cloudEnv: false,
      baseUrl: 'https://tools.test.test2.com/jira',
      username: '',
      password: '****',
      apiEndPoint: 'rest/api/2/',
      consumerKey: '',
      privateKey: '',
      isOAuth: false,
      offline: true,
      offlineFilePath: '',
    };
    component.urlParam = 'Jira';
    component.initializeFields(component.urlParam);
    component.onConnectionSelect(fakeConnection);
    fixture.detectChanges();
    expect(component.isLoading).toBeFalse();

  });

  it('should disable loading when connection is Bamboo', () => {
   
    const fakeConnection = {
      id: '5fc643cd11193836e6545560',
      type: 'Bamboo',
      connectionName: 'Test Internal -Bamboo Connection',
      cloudEnv: false,
      baseUrl: 'https://tools.test.test2.com/jira',
      username: '',
      password: '****',
      apiEndPoint: 'rest/api/2/',
      consumerKey: '',
      privateKey: '',
      isOAuth: false,
      offline: true,
      offlineFilePath: '',
    };
    component.urlParam = 'Bamboo';
    component.initializeFields(component.urlParam);
    spyOn(component,'getPlansForBamboo')
    spyOn(component,'getDeploymentProjects')
    component.onConnectionSelect(fakeConnection);
    fixture.detectChanges();
   expect(component.getPlansForBamboo).toHaveBeenCalled();
   expect(component.getDeploymentProjects).toHaveBeenCalled();
  });

  it('should clear sonar form  when connection changed to Sonar', () => {
   
    const fakeConnection = {
      id: '5fc643cd11193836e6545560',
      type: 'Sonar',
      connectionName: 'Test Internal -Sonar Connection',
      cloudEnv: false,
      baseUrl: 'https://tools.test.test2.com/jira',
      username: '',
      password: '****',
      apiEndPoint: 'rest/api/2/',
      consumerKey: '',
      privateKey: '',
      isOAuth: false,
      offline: true,
      offlineFilePath: '',
    };
    component.urlParam = 'Sonar';
    component.initializeFields(component.urlParam);
    spyOn(component,'clearSonarForm')
    spyOn(component,'updateSonarConnectionTypeAndVersionList')
    component.onConnectionSelect(fakeConnection);
    fixture.detectChanges();
   expect(component.clearSonarForm).toHaveBeenCalled();
   expect(component.updateSonarConnectionTypeAndVersionList).toHaveBeenCalled();
  });

  it('should get jenkins job name when connection changes to jenkins', () => {
   
    const fakeConnection = {
      id: '5fc643cd11193836e6545560',
      type: 'Jenkins',
      connectionName: 'Test Internal -Jenkins Connection',
      cloudEnv: false,
      baseUrl: 'https://tools.test.test2.com/jira',
      username: '',
      password: '****',
      apiEndPoint: 'rest/api/2/',
      consumerKey: '',
      privateKey: '',
      isOAuth: false,
      offline: true,
      offlineFilePath: '',
    };
    component.urlParam = 'Jenkins';
    component.initializeFields(component.urlParam);
    spyOn(component,'getJenkinsJobNames')
    component.onConnectionSelect(fakeConnection);
    fixture.detectChanges();
   expect(component.getJenkinsJobNames).toHaveBeenCalled();
  });

  it("should load elements for customfield",()=>{
    component.urlParam = 'Jira';
    component.initializeFields(component.urlParam);
    const value = "customfield";
    const elementId = "testAutomatedIdentification";
    spyOn(component, 'showFormElements');
    component.changeHandler(value,elementId);
    expect(component.showFormElements).toHaveBeenCalledTimes(1)
  })

  it("should load elements for lables",()=>{
    component.urlParam = 'Jira';
    component.initializeFields(component.urlParam);
    const value = "labels";
    const elementId = "testAutomatedIdentification";
    spyOn(component, 'showFormElements');
    spyOn(component, 'hideFormElements');
    component.changeHandler(value,elementId);
    expect(component.showFormElements).toHaveBeenCalledTimes(1);
    expect(component.hideFormElements).toHaveBeenCalledTimes(1);
  })

  it("should load and hide elements for customfield and elementID is testAutomationCompletedIdentification",()=>{
    component.urlParam = 'Jira';
    component.initializeFields(component.urlParam);
    const value = "customfield";
    const elementId = "testAutomationCompletedIdentification";
    spyOn(component, 'showFormElements');
    component.changeHandler(value,elementId);
    expect(component.showFormElements).toHaveBeenCalledTimes(1);
  })

  it("should load and hide elements for labels and elementID is testAutomationCompletedIdentification",()=>{
    component.urlParam = 'Jira';
    component.initializeFields(component.urlParam);
    const value = "labels";
    const elementId = "testAutomationCompletedIdentification";
    spyOn(component, 'showFormElements');
    component.changeHandler(value,elementId);
    expect(component.showFormElements).toHaveBeenCalledTimes(1);
  })

  it("should load and hide elements for labels and elementID is testRegressionIdentification",()=>{
    component.urlParam = 'Jira';
    component.initializeFields(component.urlParam);
    const value = "labels";
    const elementId = "testRegressionIdentification";
    spyOn(component, 'showFormElements');
    component.changeHandler(value,elementId);
    expect(component.showFormElements).toHaveBeenCalledTimes(1);
  })



  it("should load and hide elements for customfield and elementID is testRegressionIdentification",()=>{
    component.urlParam = 'Jira';
    component.initializeFields(component.urlParam);
    const value = "customfield";
    const elementId = "testRegressionIdentification";
    spyOn(component, 'showFormElements');
    component.changeHandler(value,elementId);
    expect(component.showFormElements).toHaveBeenCalledTimes(1);
  })

  it("should blank for branch field",()=>{
    const id  = 'projectKey';
    component.getOptionList(id);
    expect(component.getOptionList(id)).toEqual([]);
  })

  it("should getOptionlist for testRegressionIdentification field",()=>{
    const id  = 'testAutomatedIdentification';
    const fakeResponse = [
      {
        code: '',
        name: 'Select',
      },
      {
        code: 'CustomField',
        name: 'CustomField',
      },
      {
        code: 'Labels',
        name: 'Labels',
      },
    ];
    component.getOptionList(id);
    expect(component.getOptionList(id)).toEqual(fakeResponse);
  })

  it("should blank for apiVersion field",()=>{
    const id  = 'apiVersion';
    component.getOptionList(id);
    expect(component.getOptionList(id)).toEqual([]);
  })

  it("should blank for branch field",()=>{
    const id  = 'branch';
    component.getOptionList(id);
    expect(component.getOptionList(id)).toEqual([]);
  })

  it("should load form fiels Bamboo tool and build value",()=>{
    const value = "Build";
    const elementId = "jobType";
    component.urlParam = "Bamboo";
    component.initializeFields(component.urlParam);
    spyOn(component,'hideFormElements');
    component.bambooPlanList = [];
    component.jobTypeChangeHandler(value,elementId);
    expect(component.hideFormElements).toHaveBeenCalled();
  })

  it("should load form fiels Bamboo tool and Deploy value",()=>{
    const value = "deploy";
    const elementId = "jobType";
    component.urlParam = "Bamboo";
    component.initializeFields(component.urlParam);
    spyOn(component,'hideFormElements');
    component.bambooPlanList = [];
    component.jobTypeChangeHandler(value,elementId);
    expect(component.hideFormElements).toHaveBeenCalled();
  })

  it("should load form fiels AzurePipeline tool and build value",()=>{
    const value = "Build";
    const elementId = "jobType";
    component.urlParam = "AzurePipeline";
    component.initializeFields(component.urlParam);
    component.bambooPlanList = [];
    spyOn(component,'getAzureBuildPipelines');
    component.jobTypeChangeHandler(value,elementId);
    expect(component.getAzureBuildPipelines).toHaveBeenCalled();
  })

  it("should load form fiels AzurePipeline tool and deploy value",()=>{
    const value = "deploy";
    const elementId = "jobType";
    component.urlParam = "AzurePipeline";
    component.initializeFields(component.urlParam);
    component.bambooPlanList = [];
    spyOn(component,'getAzureReleasePipelines');
    component.jobTypeChangeHandler(value,elementId);
    expect(component.getAzureReleasePipelines).toHaveBeenCalled();
  })

  it("should load form fiels Jenkins tool and deploy value",()=>{
    const value = "deploy";
    const elementId = "jobType";
    component.urlParam = "Jenkins";
    component.initializeFields(component.urlParam);
    spyOn(component,'showFormElements');
    component.bambooPlanList = [];
    component.jobTypeChangeHandler(value,elementId);
    expect(component.showFormElements).toHaveBeenCalled();
  })

  it("should load form fiels Jenkins tool and build value",()=>{
    const value = "build";
    const elementId = "jobType";
    component.urlParam = "Jenkins";
    component.initializeFields(component.urlParam);
    spyOn(component,'hideFormElements');
    component.bambooPlanList = [];
    component.jobTypeChangeHandler(value,elementId);
    expect(component.hideFormElements).toHaveBeenCalled();
  })

  it("should get template list and filter based on kanban", () => {
    const templateList = fakeTemplateList;
    component.selectedProject = {
      id: "641cc51bd830154a05d77370",
      Type: "kanban"
    }
    spyOn(httpService, 'getJiraTemplate').and.returnValue(of(templateList))
    component.getJiraTemplate()
    expect(component.jiraTemplate.length).toBeGreaterThan(0);
  })

  it("should dropdown disabled for custom template", () => {
    const templateList = fakeTemplateList;
    component.ngOnInit();
    component.urlParam = "jira`";
    component.initializeFields(component.urlParam);
    component.selectedProject = {
      id: "641cc51bd830154a05d77370",
      Type: "kanban"
    }
    spyOn(httpService, 'getJiraTemplate').and.returnValue(of(templateList))
    component.getJiraTemplate()
    expect(component.toolForm.get('metadataTemplateCode').disabled).toBeFalsy();
  })

  it("should get api response for GitHub action tool",()=>{
    component.selectedConnection =  { id: '5fc643cd11193836e6545560' };
    component.initializeFields('GitHubAction');
    const fakeResponse = {
      "message": "FETCHED_SUCCESSFULLY",
      "success": true,
      "data": [
          {
              "workflowName": "fakeWorkflowName",
              "workflowID": "8847411"
          }
      ]
  }
     spyOn(httpService,'getGitActionWorkFlowName').and.returnValue(of(fakeResponse))
     component.getGitActionWorkflowName({target : {value : "fakeRepo"}},component);
     expect(component.gitActionWorkflowNameList.length).toBeGreaterThan(0);
  })

  it("should get api response but workflow list come as blank for GitHub action tool",()=>{
    component.selectedConnection =  { id: '5fc643cd11193836e6545560' };
    component.initializeFields('GitHubAction');
    const fakeResponse = {
      "message": "FETCHED_SUCCESSFULLY",
      "success": false,
      "data": [
          {
              "workflowName": "fakeWorkflowName",
              "workflowID": "8847411"
          }
      ]
  }
     spyOn(httpService,'getGitActionWorkFlowName').and.returnValue(of(fakeResponse))
     component.getGitActionWorkflowName({target : {value : "fakeRepo"}},component);
     expect(component.gitActionWorkflowNameList.length).toBe(0);
  })

  it("should not call workflow api if connection id or repo name is blank",()=>{
     component.selectedConnection =  { id: '' };
     component.initializeFields('GitHubAction');
     const spy = spyOn(httpService,'getGitActionWorkFlowName');
     component.getGitActionWorkflowName({target : {value : ""}},component);
     expect(spy).not.toHaveBeenCalled();
  })

  it('should disable sonar fields if sdm ID is not blank', () => {
    component.initializeFields('Sonar');
    component.toolForm.get('gitLabSdmID').setValue("fakeKey");
    spyOn(component, 'enableDisableOrganizationKey');
    component.onSdmIdChange({ target: { value: 'ID' } }, component);
    expect(component.toolForm.get('apiVersion').disabled).toBeTruthy();
  })

  it('should enable sonar fields if sdm ID is blank', () => {
    component.initializeFields('Sonar');
    component.toolForm.get('gitLabSdmID').setValue("");
    spyOn(component, 'enableDisableOrganizationKey');
    component.onSdmIdChange({ target: { value: 'ID' } }, component);
    expect(component.toolForm.get('apiVersion').disabled).not.toBeTruthy();
  })

});
