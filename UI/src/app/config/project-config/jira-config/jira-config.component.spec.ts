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

import {
  ComponentFixture,
  fakeAsync,
  TestBed,
  tick,
} from '@angular/core/testing';
import { JiraConfigComponent } from './jira-config.component';
import { MessageService, ConfirmationService } from 'primeng/api';
import { HttpService } from '../../../services/http.service';
import { SharedService } from '../../../services/shared.service';
import { GetAuthorizationService } from '../../../services/get-authorization.service';
import {
  FormGroup,
  ReactiveFormsModule,
  FormsModule,
  FormBuilder,
  FormControl,
  UntypedFormGroup,
  UntypedFormControl,
} from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { HttpClientModule } from '@angular/common/http';
import { APP_CONFIG, AppConfig } from '../../../services/app.config';
import {
  Router,
  NavigationEnd,
  ActivatedRoute,
  Params,
  Data,
} from '@angular/router';

import { InputSwitchModule } from 'primeng/inputswitch';
import { ChipsModule } from 'primeng/chips';
import { ToastModule } from 'primeng/toast';
import { TableModule } from 'primeng/table';
import { TooltipModule } from 'primeng/tooltip';
import { AutoCompleteModule } from 'primeng/autocomplete';
import { DropdownModule } from 'primeng/dropdown';

import { environment } from 'src/environments/environment';
import { Observable, throwError } from 'rxjs';
import { of } from 'rxjs';
import { CommonModule } from '@angular/common';
import { compareNumbers } from '@fullcalendar/core';

describe('JiraConfigComponent', () => {
  let component: JiraConfigComponent;
  let fixture: ComponentFixture<JiraConfigComponent>;
  let sharedService: SharedService;
  let httpService: HttpService;
  let messageService: MessageService;
  let httpMock;
  let router;
  const baseUrl = environment.baseUrl;
  const mockActivatedRoute = {
    queryParams: of({ toolName: 'Jira' }),
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
  const fakeCompleteHiearchyData = require('../../../../test/resource/fakeCompleteHierarchyData.json');
  const fakeSelectedTool = [
    {
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
      metadataTemplateID: '641d986af8d42d02b0c2558f',
    },
  ];
  const fakeBranchListForProject = require('../../../../test/resource/fakeBranchListForProject.json');
  const fakeProject = {
    id: '6335363749794a18e8a4479b',
    name: 'Scrum Project',
    type: 'Scrum',
    hierarchyLevelOne: 'Sample One',
    hierarchyLevelTwo: 'Sample Two',
    hierarchyLevelThree: 'Sample Three',
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
      boardQuery: 'Test_Query',
    },
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
    offlineFilePath: '',
  };

  const fakeTemplateList = [
    {
      id: '641cc51bd830154a05d77370',
      tool: 'Jira',
      templateName: 'DOJO Studio Template',
      templateCode: '6',
      kanban: false,
    },
    {
      id: '641cc51bd830154a05d77371',
      tool: 'Jira',
      templateName: 'Standard Template',
      templateCode: '7',
      kanban: false,
    },
    {
      id: '641cc51bd830154a05d77372',
      tool: 'Jira',
      templateName: 'Standard Template',
      templateCode: '8',
      kanban: true,
    },
    {
      id: '641cc51bd830154a05d77371',
      tool: 'Jira',
      templateName: 'Custom Template',
      templateCode: '7',
      kanban: true,
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
        CommonModule,
      ],
      providers: [
        HttpService,
        SharedService,
        MessageService,
        ConfirmationService,
        GetAuthorizationService,
        { provide: APP_CONFIG, useValue: AppConfig },
        { provide: ActivatedRoute, useValue: mockActivatedRoute },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(JiraConfigComponent);
    component = fixture.componentInstance;
    httpService = TestBed.inject(HttpService);
    sharedService = TestBed.inject(SharedService);
    messageService = TestBed.inject(MessageService);
    sharedService.setSelectedProject(fakeProject);
    sharedService.setSelectedToolConfig(fakeSelectedTool);
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    localStorage.setItem(
      'completeHierarchyData',
      JSON.stringify(fakeCompleteHiearchyData),
    );
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should recieve url parameter in urlParam property', () => {
    spyOnProperty(router, 'url', 'get').and.returnValue(
      '/dashboard/Config/JiraConfig?toolName=Jira',
    );
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

    const connectionsReq = httpMock.expectOne(
      `${baseUrl}/api/connections?type=${component.urlParam}`,
    );
    expect(connectionsReq.request.method).toBe('GET');
    connectionsReq.flush(fakeJiraConnections);
    fixture.detectChanges();
  });

  it('should save form', () => {
    component.ngOnInit();
    component.toolForm.controls['projectKey'].setValue('1212');
    component.toolForm.controls['originalTemplateCode'].setValue({
      id: '641d986af8d42d02b0c2558f',
      kanban: true,
      templateCode: '1',
      templateName: 'DOJO Agile Template',
      tool: 'Jira',
    });

    component.queryEnabled = true;
    component.toolForm.controls['boards'].setValue('DTS');
    component.toolForm.controls['jiraConfigurationType'].setValue('2');
    component.toolForm.controls['boardQuery'].setValue(
      `Project = DTS AND component = Panthers AND issuetype in (Story, Defect, "Enabler Story", "Change request", Dependency, Epic, Task, "Studio Job", "Studio Task") and  created > '2022/03/01 00:00'`,
    );
    component.isEdit = false;
    component.selectedConnection = selectedConnection;

    component.save();
    fixture.detectChanges();
    httpMock
      .match(
        `${baseUrl}/api/basicconfigs/${
          sharedService.getSelectedProject().id
        }/tools`,
      )[0]
      .flush(successResponse);
  });

  it('should submit edit form', () => {
    component.ngOnInit();
    component.toolForm.controls['projectKey'].setValue('1212');
    component.queryEnabled = true;
    component.toolForm.controls['boardQuery'].setValue(
      `Project = DTS AND component = Panthers AND issuetype in (Story, Defect, "Enabler Story", "Change request", Dependency, Epic, Task, "Studio Job", "Studio Task") and  created > '2022/03/01 00:00'`,
    );
    component.toolForm.controls['boards'].setValue('DTS');
    component.isEdit = true;
    component.selectedConnection = selectedConnection;

    component.save();
    fixture.detectChanges();
    httpMock
      .match(
        `${baseUrl}/api/basicconfigs/${
          sharedService.getSelectedProject().id
        }/tools/${component.selectedToolConfig[0].id}`,
      )[0]
      .flush(successResponse);
  });

  it('should get sonar version list', () => {
    component.ngOnInit();
    component.urlParam = 'Sonar';
    const sonarVersionListLen = 11;
    const sonarCloudVersionListLen = 13;
    httpMock
      .match(`${baseUrl}/api/sonar/version`)[0]
      .flush(fakeSonarVersionsList);
    fixture.detectChanges();
    expect(component.versionList.length).toEqual(
      fakeSonarVersionsList.data.length,
    );
    expect(component.sonarVersionList.length).toEqual(sonarVersionListLen);
    expect(component.sonarCloudVersionList.length).toEqual(
      sonarCloudVersionListLen,
    );
  });

  it('should get plans for bamboo', fakeAsync(() => {
    spyOn(component, 'showLoadingOnFormElement').and.callFake(() => {});
    spyOn(component, 'hideLoadingOnFormElement').and.callFake(() => {});
    spyOn(httpService, 'getPlansForBamboo').and.returnValue(
      of(fakeBambooPlans),
    );
    component.getPlansForBamboo('63b2bf2544af1c3bc6553977');
    tick();
    expect(Object.keys(component.bambooProjectDataFromAPI).length).toEqual(
      fakeBambooPlans.data.length,
    );
  }));

  it('should fail getting plans for bamboo', fakeAsync(() => {
    spyOn(component, 'showLoadingOnFormElement').and.callFake(() => {});
    spyOn(component, 'hideLoadingOnFormElement').and.callFake(() => {});
    spyOn(httpService, 'getPlansForBamboo').and.returnValue(
      of({ message: 'No plans found', success: false }),
    );
    component.getPlansForBamboo('63b2bf2544af1c3bc6553977');
    tick();
    expect(Object.keys(component.bambooProjectDataFromAPI).length).toEqual(0);
  }));

  it('should get deployment projects', fakeAsync(() => {
    spyOn(component, 'showLoadingOnFormElement').and.callFake(() => {});
    spyOn(component, 'hideLoadingOnFormElement').and.callFake(() => {});
    spyOn(httpService, 'getDeploymentProjectsForBamboo').and.returnValue(
      of(fakeDeploymentProjects),
    );
    component.getDeploymentProjects('63b2bf2544af1c3bc6553977');
    tick();
    expect(Object.keys(component.deploymentProjectList).length).toEqual(
      fakeDeploymentProjects.data.length,
    );
  }));

  it('should fail getting deployment projects', fakeAsync(() => {
    spyOn(component, 'showLoadingOnFormElement').and.callFake(() => {});
    spyOn(component, 'hideLoadingOnFormElement').and.callFake(() => {});
    spyOn(httpService, 'getDeploymentProjectsForBamboo').and.returnValue(
      of({ error: 'Failed getting projects' }),
    );
    component.getDeploymentProjects('63b2bf2544af1c3bc6553977');
    tick();
    expect(Object.keys(component.deploymentProjectList).length).toEqual(0);
  }));

  it('should get jenkins job names', fakeAsync(() => {
    spyOn(component, 'showLoadingOnFormElement').and.callFake(() => {});
    spyOn(component, 'hideLoadingOnFormElement').and.callFake(() => {});
    spyOn(httpService, 'getJenkinsJobNameList').and.returnValue(
      of(fakeJenkinsJobNames),
    );
    component.getJenkinsJobNames('63b4055f8ec44416b3ce96a8');
    tick();
    expect(Object.keys(component.jenkinsJobNameList).length).toEqual(
      fakeJenkinsJobNames.data.length,
    );
  }));

  it('should fail getting jenkins job names', fakeAsync(() => {
    spyOn(component, 'showLoadingOnFormElement').and.callFake(() => {});
    spyOn(component, 'hideLoadingOnFormElement').and.callFake(() => {});
    spyOn(httpService, 'getJenkinsJobNameList').and.returnValue(
      of({ message: 'No Jobs details found', success: false }),
    );
    component.getJenkinsJobNames('63b4055f8ec44416b3ce96a8');
    tick();
    expect(Object.keys(component.jenkinsJobNameList).length).toEqual(0);
  }));

  it('should get azure build pipelines', fakeAsync(() => {
    const connection = {
      id: '63809ba89939e165ba1e663f',
    };
    component.selectedConnection = true;
    spyOn(component, 'showLoadingOnFormElement').and.callFake(() => {});
    spyOn(component, 'hideLoadingOnFormElement').and.callFake(() => {});
    spyOn(httpService, 'getAzurePipelineList').and.returnValue(
      of(fakeAzurePipelinesList),
    );
    component.getAzureBuildPipelines(connection);
    tick();
    expect(Object.keys(component.azurePipelineResponseList).length).toEqual(
      fakeAzurePipelinesList.data.length,
    );
  }));

  it('should fail getting azure build pipelines', fakeAsync(() => {
    const connection = {
      id: '63809ba89939e165ba1e663f',
    };
    spyOn(component, 'showLoadingOnFormElement').and.callFake(() => {});
    spyOn(component, 'hideLoadingOnFormElement').and.callFake(() => {});
    spyOn(httpService, 'getAzurePipelineList').and.returnValue(
      of({ message: 'No Azure Builds found', success: false }),
    );
    component.getAzureBuildPipelines(connection);
    tick();
    expect(Object.keys(component.azurePipelineResponseList).length).toEqual(0);
  }));

  it('should get azure release pipelines', fakeAsync(() => {
    const connection = {
      id: '63809ba89939e165ba1e663f',
    };
    component.selectedConnection = true;
    spyOn(component, 'showLoadingOnFormElement').and.callFake(() => {});
    spyOn(component, 'hideLoadingOnFormElement').and.callFake(() => {});
    spyOn(httpService, 'getAzureReleasePipelines').and.returnValue(
      of(fakeAzurePipelinesList),
    );
    component.getAzureReleasePipelines(connection);
    tick();
    expect(Object.keys(component.azurePipelineResponseList).length).toEqual(
      fakeAzurePipelinesList.data.length,
    );
  }));

  it('should fail getting azure release pipelines', fakeAsync(() => {
    const connection = {
      id: '63809ba89939e165ba1e663f',
    };
    spyOn(component, 'showLoadingOnFormElement').and.callFake(() => {});
    spyOn(component, 'hideLoadingOnFormElement').and.callFake(() => {});
    spyOn(httpService, 'getAzureReleasePipelines').and.returnValue(
      of({ message: 'No Azure Builds found', success: false }),
    );
    component.getAzureReleasePipelines(connection);
    tick();
    expect(Object.keys(component.azurePipelineResponseList).length).toEqual(0);
  }));

  it('should enable organization key', () => {
    component.urlParam = 'Sonar';
    component.initializeFields(component.urlParam);
    component.enableDisableOrganizationKey(true);
    expect(component.toolForm.controls['organizationKey'].disabled).toBeFalsy();
  });

  it('should disable organization key', () => {
    component.urlParam = 'Sonar';
    component.initializeFields(component.urlParam);
    component.enableDisableOrganizationKey(false);
    expect(
      component.toolForm.controls['organizationKey'].disabled,
    ).toBeTruthy();
  });

  it('should fetch boards', fakeAsync(() => {
    component.urlParam = 'Jira';
    component.initializeFields(component.urlParam);
    component.selectedConnection = {
      id: '63b3f8ee8ec44416b3ce9698',
    };
    fixture.detectChanges();
    spyOn(httpService, 'getAllBoards').and.returnValue(of(fakeFetchBoards));
    component.fetchBoards(component);
    tick();
    expect(component.boardsData.length).toEqual(fakeFetchBoards.data.length);
  }));

  it('should clear sonar form', () => {
    component.urlParam = 'Sonar';
    component.initializeFields(component.urlParam);
    component.clearSonarForm();
    expect(component.toolForm.controls['organizationKey'].value).toBe('');
    expect(component.toolForm.controls['apiVersion'].value).toBe('');
    expect(component.toolForm.controls['projectKey'].value).toBe('');
    expect(component.toolForm.controls['branch'].value).toBe('');
  });

  it('should handle api version', fakeAsync(() => {
    component.urlParam = 'Sonar';
    component.selectedConnection = {
      id: '63b3f8ee8ec44416b3ce9698',
    };
    component.initializeFields(component.urlParam);
    spyOn(component, 'showLoadingOnFormElement').and.callFake(() => {});
    spyOn(component, 'isVersionSupported').and.returnValue(true);
    spyOn(component, 'hideLoadingOnFormElement').and.callFake(() => {});
    spyOn(httpService, 'getProjectKeyList').and.returnValue(
      of(fakeProjectKeyList),
    );
    component.apiVersionHandler('9.x', 'apiVersion');
    tick();
    expect(component.projectKeyList.length).toEqual(
      fakeProjectKeyList.data.length,
    );
  }));

  it('should handle project key click', fakeAsync(() => {
    component.urlParam = 'Sonar';
    component.selectedConnection = {
      id: '63b3f8ee8ec44416b3ce9698',
    };
    component.initializeFields(component.urlParam);
    component.disableBranchDropDown = true;
    spyOn(component, 'showLoadingOnFormElement').and.callFake(() => {});
    spyOn(component, 'hideLoadingOnFormElement').and.callFake(() => {});
    spyOn(httpService, 'getBranchListForProject').and.returnValue(
      of(fakeBranchListForProject),
    );
    component.projectKeyClickHandler('ENGINEERING.KPIDASHBOARD.PROCESSORS');
    tick();
    expect(component.branchList.length).toEqual(
      fakeBranchListForProject.data.length,
    );
  }));

  it('should handle bamboo plan select', fakeAsync(() => {
    component.urlParam = 'Bamboo';
    component.selectedConnection = {
      id: '63b409e88ec44416b3ce96b3',
    };
    component.initializeFields(component.urlParam);
    component.bambooProjectDataFromAPI = [
      {
        jobNameKey: 'REL-BAM',
        projectAndPlanName: '12th oct - bamboo-upgrade',
      },
    ];
    spyOn(component, 'showLoadingOnFormElement').and.callFake(() => {});
    spyOn(component, 'hideLoadingOnFormElement').and.callFake(() => {});
    spyOn(httpService, 'getBranchesForProject').and.returnValue(
      of(fakeBranchListForProject),
    );
    component.bambooPlanSelectHandler('12th oct - bamboo-upgrade', 'planName');
    tick();
    expect(component.bambooBranchDataFromAPI.length).toEqual(
      fakeBranchListForProject.data.length,
    );
  }));

  it('should delete tool', fakeAsync(() => {
    const tool = {
      id: '63b5277cf33fd2360e9e72dd',
      toolName: 'Bamboo',
      basicProjectConfigId: '63b3f9098ec44416b3ce9699',
      connectionId: '63b409e88ec44416b3ce96b3',
      connectionName: 'Bamboo Connection',
      jobName: 'REL-BAM',
      jobType: 'Build',
      createdAt: '2023-01-04T07:15:08',
      updatedAt: '2023-01-04T07:15:08',
      queryEnabled: false,
      boards: [null],
    };
    component.configuredTools = fakeConfiguredTools;
    component.showAddNewBtn = true;
    component.isConfigureTool = false;
    component.toolForm = new UntypedFormGroup({});
    spyOn(httpService, 'deleteProjectToolConfig').and.returnValue(
      of({ message: 'Tool deleted successfully', success: true }),
    );
    component.deleteTool(tool);
    tick();
    expect(component.configuredTools).not.toContain(tool);
  }));

  it('should Edit tool', fakeAsync(() => {
    component.urlParam = 'Sonar';
    component.initializeFields(component.urlParam);
    const tool = {
      id: '5fc643cd11193836e6545560',
      toolName: 'Bamboo',
      basicProjectConfigId: '63b3f9098ec44416b3ce9699',
      connectionId: '5fc643cd11193836e6545560',
      connectionName: 'Bamboo Connection',
      jobName: 'REL-BAM',
      jobType: 'Build',
      createdAt: '2023-01-04T07:15:08',
      updatedAt: '2023-01-04T07:15:08',
      queryEnabled: false,
      boards: [null],
    };
    spyOn(component, 'handleToolConfiguration');
    component.connections = fakeJiraConnections.data;
    component.editTool(tool);
    expect(component.isEdit).toBeTruthy();
  }));

  it('should add new tool', () => {
    component.urlParam = 'Sonar';
    component.initializeFields(component.urlParam);
    component.addNewTool();
    expect(component.isEdit).toBeFalse();
  });

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
    spyOn(component, 'getPlansForBamboo');
    spyOn(component, 'getDeploymentProjects');
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
    spyOn(component, 'clearSonarForm');
    spyOn(component, 'updateSonarConnectionTypeAndVersionList');
    component.onConnectionSelect(fakeConnection);
    fixture.detectChanges();
    expect(component.clearSonarForm).toHaveBeenCalled();
    expect(
      component.updateSonarConnectionTypeAndVersionList,
    ).toHaveBeenCalled();
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
    spyOn(component, 'getJenkinsJobNames');
    component.onConnectionSelect(fakeConnection);
    fixture.detectChanges();
    expect(component.getJenkinsJobNames).toHaveBeenCalled();
  });

  it('should load elements for customfield', () => {
    component.urlParam = 'Jira';
    component.initializeFields(component.urlParam);
    const value = 'customfield';
    const elementId = 'testAutomatedIdentification';
    spyOn(component, 'showFormElements');
    component.changeHandler(value, elementId);
    expect(component.showFormElements).toHaveBeenCalledTimes(1);
  });

  it('should load elements for lables', () => {
    component.urlParam = 'Jira';
    component.initializeFields(component.urlParam);
    const value = 'labels';
    const elementId = 'testAutomatedIdentification';
    spyOn(component, 'showFormElements');
    spyOn(component, 'hideFormElements');
    component.changeHandler(value, elementId);
    expect(component.showFormElements).toHaveBeenCalledTimes(1);
    expect(component.hideFormElements).toHaveBeenCalledTimes(1);
  });

  it('should load and hide elements for customfield and elementID is testAutomationCompletedIdentification', () => {
    component.urlParam = 'Jira';
    component.initializeFields(component.urlParam);
    const value = 'customfield';
    const elementId = 'testAutomationCompletedIdentification';
    spyOn(component, 'showFormElements');
    component.changeHandler(value, elementId);
    expect(component.showFormElements).toHaveBeenCalledTimes(1);
  });

  it('should load and hide elements for labels and elementID is testAutomationCompletedIdentification', () => {
    component.urlParam = 'Jira';
    component.initializeFields(component.urlParam);
    const value = 'labels';
    const elementId = 'testAutomationCompletedIdentification';
    spyOn(component, 'showFormElements');
    component.changeHandler(value, elementId);
    expect(component.showFormElements).toHaveBeenCalledTimes(1);
  });

  it('should load and hide elements for labels and elementID is testRegressionIdentification', () => {
    component.urlParam = 'Jira';
    component.initializeFields(component.urlParam);
    const value = 'labels';
    const elementId = 'testRegressionIdentification';
    spyOn(component, 'showFormElements');
    component.changeHandler(value, elementId);
    expect(component.showFormElements).toHaveBeenCalledTimes(1);
  });

  it('should load and hide elements for customfield and elementID is testRegressionIdentification', () => {
    component.urlParam = 'Jira';
    component.initializeFields(component.urlParam);
    const value = 'customfield';
    const elementId = 'testRegressionIdentification';
    spyOn(component, 'showFormElements');
    component.changeHandler(value, elementId);
    expect(component.showFormElements).toHaveBeenCalledTimes(1);
  });

  it('should blank for branch field', () => {
    const id = 'projectKey';
    component.getOptionList(id);
    expect(component.getOptionList(id)).toEqual([]);
  });

  it('should getOptionlist for testRegressionIdentification field', () => {
    const id = 'testAutomatedIdentification';
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
  });

  it('should blank for apiVersion field', () => {
    const id = 'apiVersion';
    component.getOptionList(id);
    expect(component.getOptionList(id)).toEqual([]);
  });

  it('should blank for branch field', () => {
    const id = 'branch';
    component.getOptionList(id);
    expect(component.getOptionList(id)).toEqual([]);
  });

  it('should load form fiels Bamboo tool and build value', () => {
    const value = 'Build';
    const elementId = 'jobType';
    component.urlParam = 'Bamboo';
    component.initializeFields(component.urlParam);
    spyOn(component, 'hideFormElements');
    component.bambooPlanList = [];
    component.jobTypeChangeHandler(value, elementId);
    expect(component.hideFormElements).toHaveBeenCalled();
  });

  it('should load form fiels Bamboo tool and Deploy value', () => {
    const value = 'deploy';
    const elementId = 'jobType';
    component.urlParam = 'Bamboo';
    component.initializeFields(component.urlParam);
    spyOn(component, 'hideFormElements');
    component.bambooPlanList = [];
    component.jobTypeChangeHandler(value, elementId);
    expect(component.hideFormElements).toHaveBeenCalled();
  });

  it('should load form fiels AzurePipeline tool and build value', () => {
    const value = 'Build';
    const elementId = 'jobType';
    component.urlParam = 'AzurePipeline';
    component.initializeFields(component.urlParam);
    component.bambooPlanList = [];
    spyOn(component, 'getAzureBuildPipelines');
    component.jobTypeChangeHandler(value, elementId);
    expect(component.getAzureBuildPipelines).toHaveBeenCalled();
  });

  it('should load form fiels AzurePipeline tool and deploy value', () => {
    const value = 'deploy';
    const elementId = 'jobType';
    component.urlParam = 'AzurePipeline';
    component.initializeFields(component.urlParam);
    component.bambooPlanList = [];
    spyOn(component, 'getAzureReleasePipelines');
    component.jobTypeChangeHandler(value, elementId);
    expect(component.getAzureReleasePipelines).toHaveBeenCalled();
  });

  it('should load form fiels Jenkins tool and deploy value', () => {
    const value = 'deploy';
    const elementId = 'jobType';
    component.urlParam = 'Jenkins';
    component.initializeFields(component.urlParam);
    spyOn(component, 'showFormElements');
    component.bambooPlanList = [];
    component.jobTypeChangeHandler(value, elementId);
    expect(component.showFormElements).toHaveBeenCalled();
  });

  it('should load form fiels Jenkins tool and build value', () => {
    const value = 'build';
    const elementId = 'jobType';
    component.urlParam = 'Jenkins';
    component.initializeFields(component.urlParam);
    spyOn(component, 'hideFormElements');
    component.bambooPlanList = [];
    component.jobTypeChangeHandler(value, elementId);
    expect(component.hideFormElements).toHaveBeenCalled();
  });

  it('should get template list and filter based on kanban', () => {
    const templateList = fakeTemplateList;
    component.selectedProject = {
      id: '641cc51bd830154a05d77370',
      type: 'kanban',
    };
    component.jiraTemplate = [];
    component.toolForm = new UntypedFormGroup({
      originalTemplateCode: new UntypedFormControl(),
    });
    spyOn(httpService, 'getJiraTemplate').and.returnValue(of(templateList));
    component.getJiraTemplate();
    expect(component.jiraTemplate.length).toBeGreaterThan(0);
  });

  it('should dropdown disabled for custom template', () => {
    const templateList = fakeTemplateList;
    component.ngOnInit();
    component.urlParam = 'jira`';
    component.initializeFields(component.urlParam);
    component.selectedProject = {
      id: '641cc51bd830154a05d77370',
      Type: 'kanban',
    };
    spyOn(httpService, 'getJiraTemplate').and.returnValue(of(templateList));
    component.getJiraTemplate();
    expect(
      component.toolForm.get('originalTemplateCode').disabled,
    ).toBeTruthy();
  });

  it('should get api response for GitHub action tool', () => {
    component.selectedConnection = { id: '5fc643cd11193836e6545560' };
    component.initializeFields('GitHubAction');
    const fakeResponse = {
      message: 'FETCHED_SUCCESSFULLY',
      success: true,
      data: [
        {
          workflowName: 'fakeWorkflowName',
          workflowID: '8847411',
        },
      ],
    };
    spyOn(httpService, 'getGitActionWorkFlowName').and.returnValue(
      of(fakeResponse),
    );
    component.getGitActionWorkflowName(
      { target: { value: 'fakeRepo' } },
      component,
    );
    expect(component.gitActionWorkflowNameList.length).toBeGreaterThan(0);
  });

  it('should get api response but workflow list come as blank for GitHub action tool', () => {
    component.selectedConnection = { id: '5fc643cd11193836e6545560' };
    component.initializeFields('GitHubAction');
    const fakeResponse = {
      message: 'FETCHED_SUCCESSFULLY',
      success: false,
      data: [
        {
          workflowName: 'fakeWorkflowName',
          workflowID: '8847411',
        },
      ],
    };
    spyOn(httpService, 'getGitActionWorkFlowName').and.returnValue(
      of(fakeResponse),
    );
    component.getGitActionWorkflowName(
      { target: { value: 'fakeRepo' } },
      component,
    );
    expect(component.gitActionWorkflowNameList.length).toBe(0);
  });

  it('should not call workflow api if connection id or repo name is blank', () => {
    component.selectedConnection = { id: '' };
    component.initializeFields('GitHubAction');
    const spy = spyOn(httpService, 'getGitActionWorkFlowName');
    component.getGitActionWorkflowName({ target: { value: '' } }, component);
    expect(spy).not.toHaveBeenCalled();
  });

  it('should disable sonar fields if sdm ID is not blank', () => {
    component.initializeFields('Sonar');
    component.toolForm.get('gitLabSdmID').setValue('fakeKey');
    spyOn(component, 'enableDisableOrganizationKey');
    component.onSdmIdChange({ target: { value: 'ID' } }, component);
    expect(component.toolForm.get('apiVersion').disabled).toBeTruthy();
  });

  it('should enable sonar fields if sdm ID is blank', () => {
    component.initializeFields('Sonar');
    component.toolForm.get('gitLabSdmID').setValue('');
    spyOn(component, 'enableDisableOrganizationKey');
    component.onSdmIdChange({ target: { value: 'ID' } }, component);
    expect(component.toolForm.get('apiVersion').disabled).not.toBeTruthy();
  });

  it('should fetch teams when selectedConnection is set', () => {
    component.urlParam = 'Azure';
    component.initializeFields(component.urlParam);

    spyOn(component.http, 'getAzureTeams').and.returnValue(
      of({
        success: true,
        data: [
          { id: 1, name: 'Team 1' },
          { id: 2, name: 'Team 2' },
        ],
      }),
    );
    component.selectedConnection = { id: 1, name: 'Connection 1' };
    component.fetchTeams(component);
    expect(component.http.getAzureTeams).toHaveBeenCalledWith(1);
    expect(component.teamData).toEqual([
      { id: 1, name: 'Team 1' },
      { id: 2, name: 'Team 2' },
    ]);
    expect(component.filteredTeam).toEqual([
      { id: 1, name: 'Team 1' },
      { id: 2, name: 'Team 2' },
    ]);
    expect(component.toolForm.controls['team'].enabled).toBeTrue();
    expect(component.isLoading).toBeFalse();
  });

  it('should show error message when no teams are found for selected connection', () => {
    component.urlParam = 'Azure';
    component.initializeFields(component.urlParam);
    spyOn(component.http, 'getAzureTeams').and.returnValue(
      of({ success: false }),
    );
    component.selectedConnection = { id: 1, name: 'Connection 1' };
    component.fetchTeams(component);
    expect(component.http.getAzureTeams).toHaveBeenCalledWith(1);
    expect(component.teamData).toEqual([]);
    expect(component.filteredTeam).toEqual([]);
    expect(component.toolForm.controls['team'].disabled).toBeTrue();
    expect(component.toolForm.controls['team'].value).toEqual('');
    // expect(messageService.add).toHaveBeenCalledWith({
    //   severity: 'error',
    //   summary: 'No teams found for the selected Connection.',
    // });
    expect(component.isLoading).toBeFalse();
  });

  it('should show error message when no connection is selected', () => {
    component.urlParam = 'Azure';
    component.initializeFields(component.urlParam);
    component.selectedConnection = null;
    component.fetchTeams(component);
    expect(component.toolForm.controls['team'].value).toEqual('');
    // expect(messageService.add).toHaveBeenCalledWith({
    //   severity: 'error',
    //   summary: 'Select Connection first.',
    // });
  });
  it('should get azure pipeline data when connection is Azure Pipeline when value is build', () => {
    const fakeConnection = {
      id: '5fc643cd11193836e6545560',
      type: 'Sonar',
      connectionName: 'Test Internal -Sonar Connection',
      cloudEnv: false,
    };
    component.toolForm = new UntypedFormGroup({
      jobType: new UntypedFormControl(),
    });
    component.toolForm.controls['jobType'].setValue({ name: 'Build' });
    component.urlParam = 'AzurePipeline';
    const spyobj = spyOn(component, 'getAzureBuildPipelines');
    component.onConnectionSelect(fakeConnection);
    fixture.detectChanges();
    expect(spyobj).toHaveBeenCalled();
  });

  it('should get azure release pipeline data when connection is Azure Pipeline when value is deploy', () => {
    const fakeConnection = {
      id: '5fc643cd11193836e6545560',
      type: 'Sonar',
      connectionName: 'Test Internal -Sonar Connection',
      cloudEnv: false,
    };
    component.toolForm = new UntypedFormGroup({
      jobType: new UntypedFormControl(),
    });
    component.toolForm.controls['jobType'].setValue({ name: 'Deploy' });
    component.urlParam = 'AzurePipeline';
    const spyobj = spyOn(component, 'getAzureReleasePipelines');
    component.onConnectionSelect(fakeConnection);
    fixture.detectChanges();
    expect(spyobj).toHaveBeenCalled();
  });

  it('should reset toolform fields', () => {
    const fakeConnection = {
      id: '5fc643cd11193836e6545560',
      type: 'Sonar',
      connectionName: 'Test Internal -Sonar Connection',
      cloudEnv: false,
    };
    component.toolForm = new UntypedFormGroup({
      repositoryName: new UntypedFormControl(),
      workflowID: new UntypedFormControl(),
    });
    component.urlParam = 'GitHubAction';
    component.onConnectionSelect(fakeConnection);
    fixture.detectChanges();
    expect(component.gitActionWorkflowNameList.length).toBe(0);
  });

  it('should update sonar connection and list', () => {
    component.sonarCloudVersionList = ['a', 'b', 'c'];
    component.updateSonarConnectionTypeAndVersionList(true);
    expect(component.sonarVersionFinalList.length).toBe(3);
  });

  it('should get option list based on id', () => {
    const result = component.getOptionList('env');
    expect(result).toEqual(component.connectionType);
  });

  it('should return the bambooPlanList for id "planName"', () => {
    const id = 'planName';
    const result = component.getOptionList(id);
    expect(result).toEqual(component.bambooPlanList);
  });

  it('should return the bambooBranchList for id "branchName"', () => {
    const id = 'branchName';
    const result = component.getOptionList(id);
    expect(result).toEqual(component.bambooBranchList);
  });

  it('should return the jenkinsJobNameList for id "jobName"', () => {
    const id = 'jobName';
    const result = component.getOptionList(id);
    expect(result).toEqual(component.jenkinsJobNameList);
  });

  it('should return the azurePipelineList for id "azurePipelineName"', () => {
    const id = 'azurePipelineName';
    const result = component.getOptionList(id);
    expect(result).toEqual(component.azurePipelineList);
  });

  it('should return the jobType list for id "jobType"', () => {
    const id = 'jobType';
    const result = component.getOptionList(id);
    expect(result).toEqual(component.jobType);
  });

  it('should return the deploymentProjectList for id "deploymentProject"', () => {
    const id = 'deploymentProject';
    const result = component.getOptionList(id);
    expect(result).toEqual(component.deploymentProjectList);
  });

  it('should return the gitActionWorkflowNameList for id "workflowID"', () => {
    const id = 'workflowID';
    const result = component.getOptionList(id);
    expect(result).toEqual(component.gitActionWorkflowNameList);
  });

  it('should set loading to true and set finalToolName to "Jira" for toolName "JiraTest"', () => {
    const toolName = 'JiraTest';
    component.getConnectionList(toolName);
    expect(component.loading).toBe(true);
  });

  it('should set loading to true and set finalToolName to "GitHub" for toolName "GitHubAction"', () => {
    const toolName = 'GitHubAction';
    component.getConnectionList(toolName);
    expect(component.loading).toBe(true);
  });

  it('should set loading to true and set finalToolName to the same value as toolName for other tool names', () => {
    const toolName = 'SomeTool';
    component.getConnectionList(toolName);
    expect(component.loading).toBe(true);
  });

  it('should return false if projectKey control exists and has a value', () => {
    component.toolForm = new UntypedFormGroup({
      projectKey: new UntypedFormControl('test'),
    });
    const result = component.checkProjectKey();
    expect(result).toBe(false);
  });

  it('should return true if projectKey control exists but does not have a value', () => {
    component.toolForm = new UntypedFormGroup({
      projectKey: new UntypedFormControl(),
    });
    const result = component.checkProjectKey();
    expect(result).toBe(true);
  });

  it('should filter the boards based on the query and set the filteredBoards property', () => {
    component.boardsData = [
      { boardName: 'board 1' },
      { boardName: 'board 2' },
      { boardName: 'board Another' },
    ];
    const event = { query: 'board' };
    component.filterBoards(event);
    expect(component.filteredBoards).toEqual([
      { boardName: 'board 1' },
      { boardName: 'board 2' },
      { boardName: 'board Another' },
    ]);
  });

  it('should filter the boards based on the query and set the filteredBoards property to an empty array if no boards match', () => {
    component.boardsData = [
      { boardName: 'Board 1' },
      { boardName: 'Board 2' },
      { boardName: 'Another board' },
    ];
    const event = { query: 'xyz' };
    component.filterBoards(event);
    expect(component.filteredBoards).toEqual([]);
  });

  it('should set the filteredBoards property to an empty array if the boardsData property is empty', () => {
    component.boardsData = [
      { boardName: 'Board 1' },
      { boardName: 'Board 2' },
      { boardName: 'Another board' },
    ];
    const event = { query: 'board' };
    component.boardsData = [];
    component.filterBoards(event);
    expect(component.filteredBoards).toEqual([]);
  });

  it('should push the value to the boardsData array', () => {
    component.boardsData = [{ boardName: 'Board 1' }, { boardName: 'Board 2' }];
    const value = { boardName: 'Board 3' };
    component.onBoardUnselect(value);
    expect(component.boardsData).toEqual([
      { boardName: 'Board 1' },
      { boardName: 'Board 2' },
      { boardName: 'Board 3' },
    ]);
  });

  it('should remove the value from the boardsData array based on boardId', () => {
    component.boardsData = [
      { boardId: 1, boardName: 'Board 1' },
      { boardId: 2, boardName: 'Board 2' },
      { boardId: 3, boardName: 'Board 3' },
    ];
    const value = { boardId: 2, boardName: 'Board 2' };
    component.onBoardSelect(value);
    expect(component.boardsData).toEqual([
      { boardId: 1, boardName: 'Board 1' },
      { boardId: 3, boardName: 'Board 3' },
    ]);
  });

  it('should not modify the boardsData array if the value does not exist in the array', () => {
    component.boardsData = [
      { boardId: 1, boardName: 'Board 1' },
      { boardId: 2, boardName: 'Board 2' },
      { boardId: 3, boardName: 'Board 3' },
    ];
    const value = { boardId: 4, boardName: 'Board 4' };
    component.onBoardSelect(value);
    expect(component.boardsData).toEqual([
      { boardId: 1, boardName: 'Board 1' },
      { boardId: 2, boardName: 'Board 2' },
      { boardId: 3, boardName: 'Board 3' },
    ]);
  });

  it('should submit correct plankey', () => {
    component.ngOnInit();
    // component.toolForm.controls['projectKey'].setValue('1212');
    // component.queryEnabled =true;
    // component.toolForm.controls['boardQuery'].setValue(`Project = DTS AND component = Panthers AND issuetype in (Story, Defect, "Enabler Story", "Change request", Dependency, Epic, Task, "Studio Job", "Studio Task") and  created > '2022/03/01 00:00'`);
    // component.isEdit = true;
    component.toolForm = new UntypedFormGroup({
      jobType: new UntypedFormControl(),
      planKey: new UntypedFormControl(),
      branchKey: new UntypedFormControl(),
    });
    component.urlParam = 'Bamboo';
    component.toolForm.controls['jobType'].setValue('Build');
    component.toolForm.controls['planKey'].setValue('Build');
    component.selectedConnection = {
      id: '5fc643cd11193836e6545560',
      type: 'Bamboo',
      connectionName: 'DOJO Transformation Internal -Jira Connection',
      cloudEnv: false,
    };

    component.save();
    expect(component.toolForm).toBeDefined();
  });

  it('should submit correct branchKey', () => {
    component.ngOnInit();
    component.toolForm = new UntypedFormGroup({
      jobType: new UntypedFormControl(),
      planKey: new UntypedFormControl(),
      branchKey: new UntypedFormControl(),
    });
    component.urlParam = 'Bamboo';
    component.toolForm.controls['jobType'].setValue('Build');
    component.toolForm.controls['planKey'].setValue('Build');
    component.toolForm.controls['branchKey'].setValue('Build');
    component.selectedConnection = {
      id: '5fc643cd11193836e6545560',
      type: 'Bamboo',
      connectionName: 'DOJO Transformation Internal -Jira Connection',
      cloudEnv: false,
    };

    component.save();
    expect(component.toolForm).toBeDefined();
  });

  it('should submit correct branchKey for jobtype deploy', () => {
    component.ngOnInit();
    component.toolForm = new UntypedFormGroup({
      jobType: new UntypedFormControl(),
      planKey: new UntypedFormControl(),
      branchKey: new UntypedFormControl(),
    });
    component.urlParam = 'Bamboo';
    component.toolForm.controls['jobType'].setValue('Deploy');
    component.toolForm.controls['planKey'].setValue('Build');
    component.toolForm.controls['branchKey'].setValue('Build');
    component.selectedDeploymentProject = {
      name: 'name',
      code: 'code',
    };
    component.selectedConnection = {
      id: '5fc643cd11193836e6545560',
      type: 'Bamboo',
      connectionName: 'DOJO Transformation Internal -Jira Connection',
      cloudEnv: false,
    };

    component.save();
    expect(component.toolForm).toBeDefined();
  });

  it('should submit correct data for non bomboo', () => {
    component.ngOnInit();
    component.toolForm = new UntypedFormGroup({
      jobType: new UntypedFormControl(),
      planKey: new UntypedFormControl(),
      branchKey: new UntypedFormControl(),
    });
    component.urlParam = 'Bamboo';
    component.toolForm.controls['jobType'].setValue('Deploy');
    component.toolForm.controls['planKey'].setValue('Build');
    component.toolForm.controls['branchKey'].setValue('Build');
    component.selectedDeploymentProject = {
      name: 'name',
      code: 'code',
    };
    component.selectedConnection = {
      id: '5fc643cd11193836e6545560',
      type: 'nonBamboo',
      connectionName: 'DOJO Transformation Internal -Jira Connection',
      cloudEnv: false,
    };
    spyOn(component, 'isInputFieldTypeArray');
    component.save();
    expect(component.toolForm).toBeDefined();
  });

  it('should submit correct data for non bomboo when vakue is blank', () => {
    component.ngOnInit();
    component.toolForm = new UntypedFormGroup({
      jobType: new UntypedFormControl(),
      planKey: new UntypedFormControl(),
      branchKey: new UntypedFormControl(),
    });
    component.urlParam = 'Bamboo';
    component.toolForm.controls['jobType'].setValue('');
    component.toolForm.controls['planKey'].setValue('');
    component.toolForm.controls['branchKey'].setValue('');
    component.selectedDeploymentProject = {
      name: 'name',
      code: 'code',
    };
    component.selectedConnection = {
      id: '5fc643cd11193836e6545560',
      type: 'nonBamboo',
      connectionName: 'DOJO Transformation Internal -Jira Connection',
      cloudEnv: false,
    };
    spyOn(component, 'isInputFieldTypeArray').and.returnValue(true);
    component.save();
    expect(component.toolForm).toBeDefined();
  });

  it('should submit correct data for non bomboo when vakue is blank', () => {
    component.ngOnInit();
    component.toolForm = new UntypedFormGroup({
      jobType: new UntypedFormControl(),
      planKey: new UntypedFormControl(),
      azurePipelineName: new UntypedFormControl(),
    });
    component.urlParam = 'Bamboo';
    component.toolForm.controls['jobType'].setValue('');
    component.toolForm.controls['planKey'].setValue('');
    component.toolForm.controls['azurePipelineName'].setValue('');
    component.selectedDeploymentProject = {
      name: 'name',
      code: 'code',
    };
    component.selectedConnection = {
      id: '5fc643cd11193836e6545560',
      type: 'nonBamboo',
      connectionName: 'DOJO Transformation Internal -Jira Connection',
      cloudEnv: false,
    };
    spyOn(component, 'isInputFieldTypeArray').and.returnValue(true);
    component.save();
    expect(component.toolForm).toBeDefined();
  });

  it('should submit correct data for non bomboo when vakue is blank for branch', () => {
    component.ngOnInit();
    component.toolForm = new UntypedFormGroup({
      jobType: new UntypedFormControl(),
      planKey: new UntypedFormControl(),
      branch: new UntypedFormControl(),
    });
    component.urlParam = 'Bamboo';
    component.toolForm.controls['jobType'].setValue('');
    component.toolForm.controls['planKey'].setValue('');
    component.toolForm.controls['branch'].setValue('');
    component.selectedDeploymentProject = {
      name: 'name',
      code: 'code',
    };
    component.selectedConnection = {
      id: '5fc643cd11193836e6545560',
      type: 'nonBamboo',
      connectionName: 'DOJO Transformation Internal -Jira Connection',
      cloudEnv: false,
    };
    spyOn(component, 'isInputFieldTypeArray').and.returnValue(true);
    component.save();
    expect(component.toolForm).toBeDefined();
  });

  it('should not submit when connection is empty', () => {
    component.ngOnInit();
    component.toolForm = new UntypedFormGroup({
      jobType: new UntypedFormControl(),
      planKey: new UntypedFormControl(),
      branchKey: new UntypedFormControl(),
    });
    component.urlParam = 'Bamboo';
    component.toolForm.controls['jobType'].setValue('Build');
    component.toolForm.controls['planKey'].setValue('Build');
    component.save();
    expect(component.toolForm).toBeDefined();
  });

  it('should submit when url param is GitHubAction', () => {
    component.ngOnInit();
    component.toolForm = new UntypedFormGroup({
      jobType: new UntypedFormControl(),
      planKey: new UntypedFormControl(),
      branch: new UntypedFormControl(),
    });
    component.urlParam = 'GitHubAction';
    component.toolForm.controls['jobType'].setValue('');
    component.toolForm.controls['planKey'].setValue('');
    component.toolForm.controls['branch'].setValue('');
    component.selectedDeploymentProject = {
      name: 'name',
      code: 'code',
    };
    component.gitActionWorkflowNameList = [
      {
        name: 'name',
        code: 'code',
      },
    ];
    component.selectedConnection = {
      id: '5fc643cd11193836e6545560',
      type: 'nonBamboo',
      connectionName: 'DOJO Transformation Internal -Jira Connection',
      cloudEnv: false,
    };
    spyOn(component, 'isInputFieldTypeArray').and.returnValue(true);
    component.save();
    expect(component.toolForm).toBeDefined();
  });

  it('should submit when url param is AzurePipeline', () => {
    component.ngOnInit();
    component.toolForm = new UntypedFormGroup({
      jobType: new UntypedFormControl(),
      planKey: new UntypedFormControl(),
      azurePipelineName: new UntypedFormControl(),
    });
    component.urlParam = 'AzurePipeline';
    component.toolForm.controls['jobType'].setValue('');
    component.toolForm.controls['planKey'].setValue('');
    component.toolForm.controls['azurePipelineName'].setValue('');
    component.selectedDeploymentProject = {
      name: 'name',
      code: 'code',
    };
    component.gitActionWorkflowNameList = [
      {
        name: 'name',
        code: 'code',
      },
    ];
    component.selectedConnection = {
      id: '5fc643cd11193836e6545560',
      type: 'nonBamboo',
      connectionName: 'DOJO Transformation Internal -Jira Connection',
      cloudEnv: false,
    };
    spyOn(component, 'isInputFieldTypeArray').and.returnValue(true);
    component.save();
    expect(component.toolForm).toBeDefined();
  });

  it('should Edit tool and configuration tool', () => {
    component.ngOnInit();
    component.toolForm = new UntypedFormGroup({
      jobType: new UntypedFormControl(),
      planKey: new UntypedFormControl(),
      azurePipelineName: new UntypedFormControl(),
    });
    component.urlParam = 'AzurePipeline';
    component.toolForm.controls['jobType'].setValue('');
    component.toolForm.controls['planKey'].setValue('');
    component.toolForm.controls['azurePipelineName'].setValue('');
    spyOn(httpService, 'editTool').and.returnValue(
      of({ success: true, data: { id: '63b5275df33fd2360e9e72dc' } }),
    );
    component.selectedDeploymentProject = {
      name: 'name',
      code: 'code',
    };
    component.gitActionWorkflowNameList = [
      {
        name: 'name',
        code: 'code',
      },
    ];
    component.selectedConnection = {
      id: '5fc643cd11193836e6545560',
      type: 'nonBamboo',
      connectionName: 'DOJO Transformation Internal -Jira Connection',
      cloudEnv: false,
    };
    component.connections = [
      {
        id: '63b5275df33fd2360e9e72dc',
        name: ' conn1',
      },
    ];
    component.isEdit = true;
    component.configuredTools = fakeConfiguredTools;
    spyOn(component, 'isInputFieldTypeArray').and.returnValue(true);
    component.save();
    expect(component.toolForm).toBeDefined();
  });

  it('should Edit tool and configuration tool', () => {
    component.ngOnInit();
    component.toolForm = new UntypedFormGroup({
      jobType: new UntypedFormControl(),
      planKey: new UntypedFormControl(),
      azurePipelineName: new UntypedFormControl(),
    });
    component.urlParam = 'AzurePipeline';
    component.toolForm.controls['jobType'].setValue('');
    component.toolForm.controls['planKey'].setValue('');
    component.toolForm.controls['azurePipelineName'].setValue('');
    spyOn(httpService, 'editTool').and.returnValue(
      of({ success: true, data: { id: '63b5275df33fd2360e9e72dc' } }),
    );
    component.selectedDeploymentProject = {
      name: 'name',
      code: 'code',
    };
    component.gitActionWorkflowNameList = [
      {
        name: 'name',
        code: 'code',
      },
    ];
    component.selectedConnection = {
      id: '5fc643cd11193836e6545560',
      type: 'nonBamboo',
      connectionName: 'DOJO Transformation Internal -Jira Connection',
      cloudEnv: false,
    };
    component.connections = [
      {
        id: '63b5275df33fd2360e9e72dc',
        name: ' conn1',
      },
    ];
    component.isEdit = true;
    component.configuredTools = fakeConfiguredTools;
    spyOn(component, 'isInputFieldTypeArray').and.returnValue(true);
    component.save();
    expect(component.toolForm).toBeDefined();
  });

  it('should add new tool and configuration', () => {
    component.ngOnInit();
    component.toolForm = new UntypedFormGroup({
      jobType: new UntypedFormControl(),
      planKey: new UntypedFormControl(),
      azurePipelineName: new UntypedFormControl(),
      name: new UntypedFormControl(),
      code: new UntypedFormControl(),
      apiVersion: new UntypedFormControl(),
      projectKey: new UntypedFormControl(),
    });
    component.urlParam = 'Sonar';
    component.toolForm.controls['jobType'].setValue('');
    component.toolForm.controls['planKey'].setValue('');
    component.toolForm.controls['azurePipelineName'].setValue('');
    spyOn(httpService, 'addTool').and.returnValue(
      of({ success: true, data: { id: '63b5275df33fd2360e9e72dc' } }),
    );
    component.selectedDeploymentProject = {
      name: 'name',
      code: 'code',
    };
    component.gitActionWorkflowNameList = [
      {
        name: 'name',
        code: 'code',
      },
    ];
    component.selectedConnection = {
      id: '5fc643cd11193836e6545560',
      type: 'nonBamboo',
      connectionName: 'DOJO Transformation Internal -Jira Connection',
      cloudEnv: false,
    };
    component.connections = [
      {
        id: '63b5275df33fd2360e9e72dc',
        name: ' conn1',
      },
    ];
    component.isEdit = false;
    component.configuredTools = fakeConfiguredTools;
    spyOn(component, 'isInputFieldTypeArray').and.returnValue(true);
    component.save();
    expect(component.toolForm).toBeDefined();
  });

  it('should set the selectedBambooBranchKey and update the branchKey control value', () => {
    component.bambooBranchDataFromAPI = [
      { branchName: 'Branch 1', jobBranchKey: 'branch-1' },
      { branchName: 'Branch 2', jobBranchKey: 'branch-2' },
    ];
    component.toolForm = new UntypedFormGroup({
      jobType: new UntypedFormControl(),
      branchKey: new UntypedFormControl(),
    });
    const value = 'Branch 2';
    component.bambooBranchSelectHandler(value);
    expect(component.selectedBambooBranchKey).toBe('branch-2');
  });

  it('should not update the branchKey control value if the selected branch does not have a jobBranchKey', () => {
    component.bambooBranchDataFromAPI = [
      { branchName: 'Branch 1', jobBranchKey: 'branch-1' },
      { branchName: 'Branch 2', jobBranchKey: 'branch-2' },
    ];
    component.toolForm = new UntypedFormGroup({
      jobType: new UntypedFormControl(),
      branchKey: new UntypedFormControl(),
      azurePipelineName: new UntypedFormControl(),
    });
    const value = 'Branch 3';
    component.bambooBranchSelectHandler(value);
    expect(component.selectedBambooBranchKey).toBeUndefined();
  });

  it('should update the jobName control value based on the selected pipeline', () => {
    const value = 'Pipeline 2';
    component.toolForm = new UntypedFormGroup({
      jobType: new UntypedFormControl(),
      branchKey: new UntypedFormControl(),
      jobName: new UntypedFormControl(),
    });
    component.azurePipelineResponseList = [
      { code: 'Pipeline 1' },
      { code: 'Pipeline 2' },
    ];
    component.pipeLineDropdownHandler(value);
    expect(component.toolForm).toBeDefined();
  });

  it('should not update the jobName control value if the selected pipeline does not exist in the azurePipelineResponseList', () => {
    const value = 'Pipeline 3';
    component.toolForm = new UntypedFormGroup({
      jobType: new UntypedFormControl(),
      branchKey: new UntypedFormControl(),
      jobName: new UntypedFormControl(),
    });
    component.azurePipelineResponseList = [
      { code: 'Pipeline 1' },
      { code: 'Pipeline 2' },
    ];
    component.pipeLineDropdownHandler(value);
    expect(component.toolForm).toBeDefined();
  });

  it('should fail getting azure release pipelines', () => {
    const connection = {
      id: '63809ba89939e165ba1e663f',
    };
    component.selectedConnection = 'test connection';
    spyOn(component, 'showLoadingOnFormElement').and.callFake(() => {});
    spyOn(component, 'hideLoadingOnFormElement').and.callFake(() => {});
    spyOn(httpService, 'getAzurePipelineList').and.returnValue(
      of({ message: 'No Azure Builds found', success: false }),
    );
    component.getAzureBuildPipelines(connection);
    expect(Object.keys(component.azurePipelineResponseList).length).toEqual(0);
  });

  it('should load form field Bamboo tool and GitHubAction', () => {
    const value = 'deploy';
    const elementId = 'jobType';
    component.urlParam = 'GitHubAction';
    component.initializeFields(component.urlParam);
    spyOn(component, 'hideFormElements');
    component.bambooPlanList = [];
    component.jobTypeChangeHandler(value, elementId);
    expect(component.hideFormElements).toHaveBeenCalled();
  });

  it('should load form field Bamboo tool and GitHubAction for build', () => {
    const value = 'build';
    const elementId = 'jobType';
    component.urlParam = 'GitHubAction';
    component.initializeFields(component.urlParam);
    const obj = spyOn(component, 'showFormElements');
    component.bambooPlanList = [];
    component.jobTypeChangeHandler(value, elementId);
    expect(obj).toHaveBeenCalled();
  });

  it('should hide the specified form elements and disable their corresponding controls', () => {
    component.formTemplate = {
      elements: [{ id: 'jobType', show: true }],
    };
    component.toolForm = new UntypedFormGroup({
      jobType: new UntypedFormControl(),
      planKey: new UntypedFormControl(),
    });
    const elementIds = ['jobType'];
    component.hideFormElements(elementIds);
    expect(component.formTemplate.elements).toEqual([
      { id: 'jobType', show: false },
    ]);
  });

  it('should jirachange method', () => {
    component.ngOnInit();
    component.toolForm = new UntypedFormGroup({
      boards: new UntypedFormControl(),
      boardQuery: new UntypedFormControl(),
      jiraConfigurationType: new UntypedFormControl(),
    });
    component.urlParam = 'Jira';
    component.jiraMethodChange(component, { value: '2' });
  });

  it('should jirachange works for board configuration type', () => {
    component.ngOnInit();
    component.toolForm = new UntypedFormGroup({
      boards: new UntypedFormControl(),
      boardQuery: new UntypedFormControl(),
      jiraConfigurationType: new UntypedFormControl(),
    });
    component.urlParam = 'Jira';
    component.jiraMethodChange(component, { value: '1' });
  });

  it('should jirachange works for none configuration type', () => {
    component.ngOnInit();
    component.toolForm = new UntypedFormGroup({
      boards: new UntypedFormControl(),
      boardQuery: new UntypedFormControl(),
      jiraConfigurationType: new UntypedFormControl(),
    });
    component.urlParam = 'Jira';
    component.jiraMethodChange(component, { value: '3' });
  });

  it('should move the selected item to the beginning of the array', () => {
    const arr = [
      { id: 1, name: 'Item 1' },
      { id: 2, name: 'Item 2' },
      { id: 3, name: 'Item 3' },
    ];
    const selectedId = 2;
    component.promote(selectedId, arr);
    expect(arr).toEqual([
      { id: 2, name: 'Item 2' },
      { id: 1, name: 'Item 1' },
      { id: 3, name: 'Item 3' },
    ]);
  });

  it('should not modify the array if the selected item is already at the beginning', () => {
    const arr = [
      { id: 1, name: 'Item 1' },
      { id: 2, name: 'Item 2' },
      { id: 3, name: 'Item 3' },
    ];
    const selectedId = 1;
    component.promote(selectedId, arr);
    expect(arr).toEqual([
      { id: 1, name: 'Item 1' },
      { id: 2, name: 'Item 2' },
      { id: 3, name: 'Item 3' },
    ]);
  });

  it('should not modify the array if the selected item is not found', () => {
    const arr = [
      { id: 1, name: 'Item 1' },
      { id: 2, name: 'Item 2' },
      { id: 3, name: 'Item 3' },
    ];
    const selectedId = 4;
    component.promote(selectedId, arr);
    expect(arr).toEqual([
      { id: 1, name: 'Item 1' },
      { id: 2, name: 'Item 2' },
      { id: 3, name: 'Item 3' },
    ]);
  });

  it('should return true if the version is supported', () => {
    component.versionList = [
      { type: 'type-1', branchSupport: true, versions: ['1.0', '2.0'] },
      { type: 'type-2', branchSupport: true, versions: ['3.0', '4.0'] },
      { type: 'type-3', branchSupport: false, versions: ['5.0', '6.0'] },
    ];
    component.selectedConnectionType = 'type-1';
    const version = '1.0';
    const result = component.isVersionSupported(version);
    expect(result).toBe(true);
  });

  it('should return false if the version is not supported', () => {
    component.versionList = [
      { type: 'type-1', branchSupport: true, versions: ['1.0', '2.0'] },
      { type: 'type-2', branchSupport: true, versions: ['3.0', '4.0'] },
      { type: 'type-3', branchSupport: false, versions: ['5.0', '6.0'] },
    ];
    component.selectedConnectionType = 'type-1';
    const version = '3.0';
    const result = component.isVersionSupported(version);
    expect(result).toBe(false);
  });

  it('should return false if the selected connection type does not have any supported versions', () => {
    component.versionList = [
      { type: 'type-1', branchSupport: true, versions: ['1.0', '2.0'] },
      { type: 'type-2', branchSupport: true, versions: ['3.0', '4.0'] },
      { type: 'type-3', branchSupport: false, versions: ['5.0', '6.0'] },
    ];
    component.selectedConnectionType = 'type-1';
    const version = '1.0';
    const result = component.isVersionSupported(version);
    expect(result).toBe(true);
  });

  it('should give error when getting plans for bamboo', () => {
    const connectionId = 'dsdaddad';
    component.bambooPlanList = [];
    component.formTemplate = {
      group: 'Bamboo',
      elements: [
        {
          type: 'dropdown',
          label: 'Job Type',
          id: 'jobType',
          validators: ['required'],
          containerClass: 'p-sm-6',
          show: true,
          isLoading: false,
        },
      ],
    };
    component.toolForm = new UntypedFormGroup({
      jobType: new UntypedFormControl(),
      planKey: new UntypedFormControl(),
      branchKey: new UntypedFormControl(),
    });
    component.toolForm.controls['jobType'].setValue('Build');
    component.bambooBranchList = [];
    spyOn(component, 'showLoadingOnFormElement').and.callThrough();
    const errResponse = {
      message: 'No plans details found',
      success: false,
    };
    spyOn(component, 'hideLoadingOnFormElement').and.callThrough();
    spyOn(httpService, 'getPlansForBamboo').and.returnValue(of(errResponse));
    const spy = spyOn(messageService, 'add');
    component.getPlansForBamboo(connectionId);
    expect(spy).toHaveBeenCalled();
  });

  it('should handle deployment projects when success false', () => {
    const connectionId = 'dsdaddad';
    component.formTemplate = {
      group: 'Bamboo',
      elements: [
        {
          type: 'dropdown',
          label: 'Job Type',
          id: 'jobType',
          validators: ['required'],
          containerClass: 'p-sm-6',
          show: true,
          isLoading: false,
        },
      ],
    };
    component.deploymentProjectList = [];
    component.toolForm = new UntypedFormGroup({
      jobType: new UntypedFormControl({ name: 'Deploy' }),
    });
    spyOn(component, 'showLoadingOnFormElement').and.callThrough();
    const errResponse = {
      error: 'Something went wrong',
      success: false,
    };
    spyOn(httpService, 'getDeploymentProjectsForBamboo').and.returnValue(
      of(errResponse),
    );
    const spy = spyOn(messageService, 'add');
    spyOn(component, 'hideLoadingOnFormElement').and.callThrough();
    component.getDeploymentProjects(connectionId);
    expect(spy).toHaveBeenCalled();
  });

  it('should handle error on getting deployment projects', () => {
    const connectionId = 'dsdaddad';
    component.formTemplate = {
      group: 'Bamboo',
      elements: [
        {
          type: 'dropdown',
          label: 'Job Type',
          id: 'jobType',
          validators: ['required'],
          containerClass: 'p-sm-6',
          show: true,
          isLoading: false,
        },
      ],
    };
    component.deploymentProjectList = [];
    component.toolForm = new UntypedFormGroup({
      jobType: new UntypedFormControl({ name: 'Deploy' }),
    });
    spyOn(component, 'showLoadingOnFormElement').and.callThrough();
    const errResponse = {
      message: 'Something went wrong',
      error: 'Error',
    };
    spyOn(httpService, 'getDeploymentProjectsForBamboo').and.returnValue(
      of(errResponse),
    );
    const spy = spyOn(messageService, 'add');
    spyOn(component, 'hideLoadingOnFormElement').and.callThrough();
    component.getDeploymentProjects(connectionId);
    expect(spy).toHaveBeenCalled();
  });

  xit('should throw error on getting jenkins job names', () => {
    const connectionId = 'skdhakda';
    const errResponse = {
      error: {
        message: 'error msg',
      },
    };
    spyOn(component, 'showLoadingOnFormElement');
    spyOn(httpService, 'getJenkinsJobNameList').and.returnValue(
      throwError(errResponse),
    );
    component.jenkinsJobNameList = [];
    spyOn(component, 'hideLoadingOnFormElement').and.callThrough();
    const spy = spyOn(messageService, 'add');
    component.getJenkinsJobNames(connectionId);
    expect(spy).toHaveBeenCalled();
  });

  it('should give error while getting connection list', () => {
    component.loading = true;
    const errResponse = {
      error: 'Something went wrong',
      success: false,
    };
    spyOn(httpService, 'getAllConnectionTypeBased').and.returnValue(
      of(errResponse),
    );
    component.connections = [];
    const spy = spyOn(messageService, 'add');
    component.getConnectionList('Jira');
    expect(spy).toHaveBeenCalled();
  });

  it('should check boards when queryEnabled is false', () => {
    component.queryEnabled = false;
    component.toolForm = new UntypedFormGroup({
      projectKey: new UntypedFormControl(false),
    });
    const spy = component.checkBoards();
    expect(spy).toBeTruthy();
  });

  it('should handle error when catching error on getting azure release pipelines', () => {
    const connection = {
      id: '63b3f8ee8ec44416b3ce9698',
    };
    component.selectedConnection = {
      id: '63b3f8ee8ec44416b3ce9698',
    };
    component.formTemplate = {
      elements: [{ id: 'jobType', show: true }],
    };
    spyOn(component, 'showLoadingOnFormElement').and.callThrough();
    component.azurePipelineApiVersion = '6.0';
    const errResponse = {
      error: {
        message: 'No pipelines details found',
      },
      success: false,
    };
    spyOn(httpService, 'getAzureReleasePipelines').and.returnValue(
      of(errResponse),
    );
    spyOn(component, 'hideLoadingOnFormElement').and.callThrough();
    component.azurePipelineList = [];
    const spy = spyOn(messageService, 'add').and.callThrough();
    component.getAzureReleasePipelines(connection);
    expect(spy).toHaveBeenCalled();
  });

  it('should handle error when success is false on getting azure release pipelines', () => {
    const connection = {
      id: '63b3f8ee8ec44416b3ce9698',
    };
    component.selectedConnection = {
      id: '63b3f8ee8ec44416b3ce9698',
    };
    component.formTemplate = {
      elements: [{ id: 'jobType', show: true }],
    };
    spyOn(component, 'showLoadingOnFormElement').and.callThrough();
    component.azurePipelineApiVersion = '6.0';
    const errResponse = {
      error: 'Something went wrong',
      success: false,
    };
    spyOn(httpService, 'getAzureReleasePipelines').and.returnValue(
      of(errResponse),
    );
    component.azurePipelineList = [];
    const spy = spyOn(messageService, 'add');
    component.getAzureReleasePipelines(connection);
    expect(spy).toHaveBeenCalled();
  });

  it('should handle error when apiVersionHandler when sucess is false', () => {
    component.selectedConnection = {
      id: '63b3f8ee8ec44416b3ce9698',
    };
    component.toolForm = new UntypedFormGroup({
      organizationKey: new UntypedFormControl(),
    });
    const errResponse = {
      error: 'Something went wrong',
      success: false,
    };
    component.projectKeyList = [];
    component.branchList = [];
    const spy = spyOn(messageService, 'add');
    spyOn(component, 'showLoadingOnFormElement').and.callThrough();
    spyOn(component, 'hideLoadingOnFormElement').and.callThrough();
    spyOn(httpService, 'getProjectKeyList').and.returnValue(of(errResponse));
    component.apiVersionHandler('6.0');
    expect(spy).toHaveBeenCalled();
  });

  it('should handle error when apiVersionHandler when sucess is false', () => {
    component.selectedConnection = {
      id: '63b3f8ee8ec44416b3ce9698',
    };
    component.toolForm = new UntypedFormGroup({
      organizationKey: new UntypedFormControl(),
      apiVersion: new UntypedFormControl('6.0'),
    });
    component.formTemplate = {
      elements: [{ id: 'jobType', show: true }],
    };
    const errResponse = {
      error: 'Something went wrong',
      success: false,
    };
    component.disableBranchDropDown = true;
    component.branchList = [];
    const spy = spyOn(messageService, 'add');
    spyOn(component, 'showLoadingOnFormElement').and.callThrough();
    spyOn(component, 'hideLoadingOnFormElement').and.callThrough();
    spyOn(httpService, 'getBranchListForProject').and.returnValue(
      of(errResponse),
    );
    component.projectKeyClickHandler('ENGINEERING.KPIDASHBOARD.PROCESSORS');
    expect(spy).toHaveBeenCalled();
  });

  it('should filter teams based on the query', () => {
    // Arrange
    const event = { query: 'team' };
    const teamData = [{ name: 'Team 1' }, { name: 'Team 2' }];
    component.teamData = teamData;

    // Act
    component.filterTeams(event);

    // Assert
    expect(component.filteredTeam).toEqual([
      { name: 'Team 1' },
      { name: 'Team 2' },
    ]);
  });

  it('should handle empty teamData', () => {
    // Arrange
    const event = { query: 'team' };
    component.teamData = [];

    // Act
    component.filterTeams(event);

    // Assert
    expect(component.filteredTeam).toEqual([]);
  });

  it('should handle empty query', () => {
    // Arrange
    const event = { query: '' };
    const teamData = [
      { name: 'Team 1' },
      { name: 'Team 2' },
      { name: 'Another Team' },
    ];
    component.teamData = teamData;

    // Act
    component.filterTeams(event);

    // Assert
    expect(component.filteredTeam).toEqual(teamData);
  });

  it('should handle no matching teams', () => {
    // Arrange
    const event = { query: 'team' };
    const teamData = [
      { name: 'Project 1' },
      { name: 'Project 2' },
      { name: 'Another Project' },
    ];
    component.teamData = teamData;

    // Act
    component.filterTeams(event);

    // Assert
    expect(component.filteredTeam).toEqual([]);
  });

  it('should handle error when fetching project key list', fakeAsync(() => {
    // Arrange
    const version = '1.0';
    component.selectedConnection = { id: '1' };
    component.toolForm = new UntypedFormGroup({
      organizationKey: new UntypedFormControl('orgKey'),
    });
    component.projectKeyList = [];
    component.branchList = [];
    const response = {
      success: false,
      message: 'Error',
    };
    spyOn(component, 'showLoadingOnFormElement');
    spyOn(component, 'hideLoadingOnFormElement');
    const spy = spyOn(messageService, 'add');
    spyOn(httpService, 'getProjectKeyList').and.returnValue(of(response));

    // Act
    component.apiVersionHandler(version);
    tick();

    // Assert
    // expect(component.http.getProjectKeyList).toHaveBeenCalledWith(selectedConnectionId, organizationKey);
    expect(component.projectKeyList).toEqual([]);
    expect(component.branchList).toEqual([]);
    expect(spy).toHaveBeenCalledWith({
      severity: 'error',
      summary: response.message,
    });
    expect(component.hideLoadingOnFormElement).toHaveBeenCalledWith(
      'projectKey',
    );
  }));

  it('should handle exception and show error message', fakeAsync(() => {
    // Arrange
    const version = '1.0';
    component.selectedConnection = { id: '1' };
    component.toolForm = new UntypedFormGroup({
      organizationKey: new UntypedFormControl('orgKey'),
    });
    component.projectKeyList = [];
    component.branchList = [];

    spyOn(component, 'showLoadingOnFormElement');
    const spy = spyOn(messageService, 'add');
    const errorMessage = 'Something went wrong, Please try again';
    spyOn(httpService, 'getProjectKeyList').and.throwError(errorMessage);

    // Act
    component.apiVersionHandler(version);
    tick();

    // Assert
    expect(component.projectKeyList).toEqual([]);
    expect(component.branchList).toEqual([]);
    expect(spy).toHaveBeenCalledWith({
      severity: 'error',
      summary: errorMessage,
    });
  }));

  it('should give error on bamboo plan select', fakeAsync(() => {
    component.urlParam = 'Bamboo';
    component.selectedConnection = {
      id: '63b409e88ec44416b3ce96b3',
    };
    // component.initializeFields(component.urlParam);
    component.bambooProjectDataFromAPI = [
      {
        jobNameKey: 'REL-BAM',
        projectAndPlanName: '12th oct - bamboo-upgrade',
      },
    ];
    component.bambooPlanKeyForSelectedPlan = '';
    component.toolForm = new UntypedFormGroup({
      planKey: new UntypedFormControl(),
    });
    const errorResponse = {
      success: false,
      message: 'No plans details found',
    };
    component.bambooBranchList = [];
    spyOn(component, 'showLoadingOnFormElement').and.callFake(() => {});
    spyOn(component, 'hideLoadingOnFormElement').and.callFake(() => {});
    spyOn(httpService, 'getBranchesForProject').and.returnValue(
      of(errorResponse),
    );
    const spy = spyOn(messageService, 'add');
    component.bambooPlanSelectHandler('12th oct - bamboo-upgrade', 'planName');
    tick();
    expect(spy).toHaveBeenCalled();
  }));

  it('should give error on getting jenkins job name', fakeAsync(() => {
    const connectionId = '331231231';
    const errResponse = {
      success: false,
      error: {
        message: 'No Jenkins Job found',
      },
    };
    component.formTemplate = {
      elements: [{ id: 'jobType', show: true }],
    };
    spyOn(component, 'showLoadingOnFormElement').and.callThrough();
    spyOn(httpService, 'getJenkinsJobNameList').and.returnValue(
      of(errResponse),
    );
    const spy = spyOn(messageService, 'add');
    component.getJenkinsJobNames(connectionId);
    tick();
    expect(spy).toHaveBeenCalled();
  }));

  it('should get azure data when selecting connection', () => {
    const fakeConnection = {
      id: '5fc643cd11193836e6545560',
      type: 'Sonar',
      connectionName: 'Test Internal -Sonar Connection',
      cloudEnv: false,
    };
    component.toolForm = new UntypedFormGroup({
      team: new UntypedFormControl(),
    });
    // component.toolForm.controls['jobType'].setValue({ name: 'Deploy' })
    component.urlParam = 'Azure';
    const spyobj = spyOn(component, 'fetchTeams');
    component.isLoading = false;
    component.onConnectionSelect(fakeConnection);
    fixture.detectChanges();
    expect(spyobj).toHaveBeenCalled();
  });

  it('should give error while fetching boards', fakeAsync(() => {
    component.urlParam = 'Jira';
    component.initializeFields(component.urlParam);
    component.selectedConnection = {
      id: '63b3f8ee8ec44416b3ce9698',
    };
    const err = {
      success: false,
      message: 'Error in fetching boards',
    };
    component.toolForm = new UntypedFormGroup({
      boards: new UntypedFormControl(),
    });
    component.boardsData = [];
    fixture.detectChanges();
    spyOn(httpService, 'getAllBoards').and.returnValue(of(err));
    const spy = spyOn(messageService, 'add');
    component.fetchBoards(component);
    tick();
    expect(spy).toHaveBeenCalled();
  }));

  it('should filter Kanban type projects correctly when type is lowercase', fakeAsync(() => {
    // Arrange
    const mockResponse = {
      data: [
        { tool: 'Jira', kanban: true, name: 'Template1' },
        { tool: 'Jira', kanban: false, name: 'Template2' },
        { tool: 'Other', kanban: true, name: 'Template3' },
      ],
    };
    component.selectedProject = { id: 1, type: 'kanban' };
    spyOn(httpService, 'getJiraConfigurationTypeOptions').and.returnValue(
      of(mockResponse),
    );

    // Act
    component.getJiraConfigurationType();
    tick();

    // Assert
    expect(httpService.getJiraConfigurationTypeOptions).toHaveBeenCalled();
    expect(component.jiraConfigurationTypeOptions).toEqual([
      { tool: 'Jira', kanban: true, name: 'Template1' },
    ]);
  }));

  it('should filter Scrum type projects correctly when Type is uppercase', fakeAsync(() => {
    // Arrange
    const mockResponse = {
      data: [
        { tool: 'Jira', kanban: true, name: 'Template1' },
        { tool: 'Jira', kanban: false, name: 'Template2' },
        { tool: 'Other', kanban: false, name: 'Template3' },
      ],
    };
    component.selectedProject = { id: 1, Type: 'SCRUM' };
    spyOn(httpService, 'getJiraConfigurationTypeOptions').and.returnValue(
      of(mockResponse),
    );

    // Act
    component.getJiraConfigurationType();
    tick();

    // Assert
    expect(httpService.getJiraConfigurationTypeOptions).toHaveBeenCalled();
    expect(component.jiraConfigurationTypeOptions).toEqual([
      { tool: 'Jira', kanban: false, name: 'Template2' },
    ]);
  }));

  it('should handle case when selectedProject is null', fakeAsync(() => {
    // Arrange
    const mockResponse = {
      data: [
        { tool: 'Jira', kanban: true, name: 'Template1' },
        { tool: 'Jira', kanban: false, name: 'Template2' },
      ],
    };
    component.selectedProject = null;
    spyOn(httpService, 'getJiraConfigurationTypeOptions').and.returnValue(
      of(mockResponse),
    );

    // Act
    component.getJiraConfigurationType();
    tick();

    // Assert
    expect(httpService.getJiraConfigurationTypeOptions).toHaveBeenCalled();
    expect(component.jiraConfigurationTypeOptions).toEqual([]);
  }));

  it('should filter non-Jira tools out', fakeAsync(() => {
    // Arrange
    const mockResponse = {
      data: [
        { tool: 'Jira', kanban: true, name: 'Template1' },
        { tool: 'Other', kanban: true, name: 'Template2' },
        { tool: 'Another', kanban: true, name: 'Template3' },
      ],
    };
    component.selectedProject = { id: 1, type: 'kanban' };
    spyOn(httpService, 'getJiraConfigurationTypeOptions').and.returnValue(
      of(mockResponse),
    );

    // Act
    component.getJiraConfigurationType();
    tick();

    // Assert
    expect(httpService.getJiraConfigurationTypeOptions).toHaveBeenCalled();
    expect(component.jiraConfigurationTypeOptions).toEqual([
      { tool: 'Jira', kanban: true, name: 'Template1' },
    ]);
  }));

  it('should handle case-insensitive tool name comparison', fakeAsync(() => {
    // Arrange
    const mockResponse = {
      data: [
        { tool: 'JIRA', kanban: true, name: 'Template1' },
        { tool: 'jira', kanban: true, name: 'Template2' },
      ],
    };
    component.selectedProject = { id: 1, type: 'kanban' };
    spyOn(httpService, 'getJiraConfigurationTypeOptions').and.returnValue(
      of(mockResponse),
    );

    // Act
    component.getJiraConfigurationType();
    tick();

    // Assert
    expect(httpService.getJiraConfigurationTypeOptions).toHaveBeenCalled();
    expect(component.jiraConfigurationTypeOptions).toEqual([
      { tool: 'JIRA', kanban: true, name: 'Template1' },
      { tool: 'jira', kanban: true, name: 'Template2' },
    ]);
  }));
});
