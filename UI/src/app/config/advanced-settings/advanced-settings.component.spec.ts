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
  waitForAsync,
  discardPeriodicTasks,
} from '@angular/core/testing';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { HttpService } from '../../services/http.service';
import { APP_CONFIG, AppConfig } from '../../services/app.config';
import { DatePipe } from '../../../../node_modules/@angular/common';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { Routes } from '@angular/router';
import { environment } from 'src/environments/environment';
import { ToastModule } from 'primeng/toast';
import { TableModule } from 'primeng/table';
import { ConfirmationService, MessageService, Confirmation } from 'primeng/api';
import { AdvancedSettingsComponent } from './advanced-settings.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { GetAuthorizationService } from '../../services/get-authorization.service';
import { SharedService } from '../../services/shared.service';
import { of, throwError, interval } from 'rxjs';
import { takeWhile } from 'rxjs/operators';
describe('AdvancedSettingsComponent', () => {
  let component: AdvancedSettingsComponent;
  let fixture: ComponentFixture<AdvancedSettingsComponent>;
  let httpService;
  let getAuthorizationService;
  let httpMock;
  let messageService;
  let confirmationService;
  const baseUrl = environment.baseUrl; // Servers Env
  // var store = {};
  // var ls = function () {
  //   return JSON.parse(store['storage']);
  // };

  const fakeProjects = require('../../../test/resource/fakeProjectsDashConfig.json');
  const fakeProcessorData = {
    message: '',
    success: true,
    data: [
      {
        id: '5e624a04e4b098dbca2aff3c',
        processorName: 'Jira',
        processorType: 'AgileTool',
        errors: [],
        updatedTime: 1583499780045,
        active: true,
        online: true,
      },
      {
        id: '5e624a7ce4b0d02e2e9938f0',
        processorName: 'Atm',
        processorType: 'AgileTool',
        errors: [],
        updatedTime: 1583731433007,
        active: true,
        online: true,
      },
      {
        id: '5e624a7ce4b0e00531062788',
        processorName: 'Excel',
        processorType: 'Excel',
        errors: [],
        updatedTime: 1583731500244,
        active: true,
        online: true,
      },
      {
        id: '5e625760e4b04faa250e493c',
        processorName: 'Bitbucket',
        processorType: 'Scm',
        errors: [],
        updatedTime: 1583730003541,
        active: true,
        online: true,
      },
      {
        id: '5e62e403e4b0a47e0405900c',
        processorName: 'Precalculated',
        processorType: 'Central',
        errors: [],
        updatedTime: 1583712007726,
        active: true,
        online: true,
      },
      {
        id: '5e62e405e4b034f67720d19b',
        processorName: 'Sonar',
        processorType: 'SonarDetails',
        errors: [],
        updatedTime: 1583715542758,
        active: true,
        online: true,
      },
      {
        id: '5e62e406e4b0baec876653b7',
        processorName: 'Newrelic',
        processorType: 'NewRelic',
        errors: [],
        updatedTime: 1583712003138,
        active: true,
        online: true,
      },
      {
        id: '5e62f217e4b0123940f939c2',
        processorName: 'CentralProducer',
        processorType: 'Central',
        errors: [],
        updatedTime: 1583715625877,
        active: true,
        online: true,
      },
      {
        id: '61ef81046c1a0b242cdb188b',
        processorName: 'GitHub',
        processorType: 'Scm',
        errors: [],
        updatedTime: 1643094625006,
        active: true,
        online: false,
        lastSuccess: true,
      },
    ],
  };

  const switchViewEventProcessor = {
    originalEvent: {
      isTrusted: true,
    },
    item: {
      label: 'Processor State',
      icon: 'pi pi-fw pi-cog',
      expanded: true,
    },
  };

  const switchViewEventServerRole = {
    originalEvent: {
      isTrusted: true,
    },
    item: {
      label: 'Server Role',
      icon: 'pi pi-fw pi-cog',
      expanded: true,
    },
  };

  const fakeServerRole = {
    message: 'Data found for the key',
    success: true,
    data: {
      centralProducer: true,
    },
  };

  const fakeSetRoleResponse = {
    message: 'Data saved for the key',
    success: true,
    data: {
      isCentralProducer: true,
    },
  };

  const fakePreCalculatedConfig = {
    message: 'Success',
    success: true,
    data: {
      showPreCalculatedDataForScrum: true,
      showPreCalculatedDataForKanban: false,
    },
  };

  const fakeGetAllTools = require('../../../test/resource/fakeGetAllTools.json');
  const fakeProcessorsTracelog = require('../../../test/resource/fakeProcessorsTracelog.json');
  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [AdvancedSettingsComponent],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
      imports: [
        HttpClientTestingModule,
        ToastModule,
        TableModule,
        BrowserAnimationsModule,
        RouterTestingModule,
      ],
      providers: [
        HttpService,
        MessageService,
        SharedService,
        DatePipe,
        GetAuthorizationService,
        ConfirmationService,
        { provide: APP_CONFIG, useValue: AppConfig },
      ],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AdvancedSettingsComponent);
    component = fixture.componentInstance;
    httpService = TestBed.inject(HttpService);
    getAuthorizationService = TestBed.inject(GetAuthorizationService);
    httpMock = TestBed.inject(HttpTestingController);
    confirmationService = TestBed.inject(ConfirmationService);
    messageService = TestBed.inject(MessageService);
    // fixture.detectChanges();
  });

  afterEach(() => {
    // store = {};
    fixture.destroy();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  // it('should load processor data', (done) => {
  //   component.selectedView = 'processor_state';
  //   // component.getProcessorData();
  //   // fixture.detectChanges();
  //   httpMock.match(baseUrl + '/api/processor')[0].flush(fakeProcessorData);
  //   if (component.processorData['success']) {
  //     expect(Object.keys(component.processorData).length).toEqual(Object.keys(fakeProcessorData).length);
  //   } else {
  //     // component.messageService.add({ severity: 'error', summary: 'Error in fetching roles. Please try after some time.' });
  //   }
  //   done();
  // });

  it('should switch view to Processor State', (done) => {
    component.switchView(switchViewEventProcessor);
    // fixture.detectChanges();
    expect(component.selectedView).toBe('processor_state');
    done();
  });

  it('should fetch all user projects', () => {
    const getAuthorizationService = TestBed.inject(GetAuthorizationService);
    const getProjectsResponse = {
      message: 'Fetched successfully',
      success: true,
      data: [
        {
          id: '601bca9569515b0001d68182',
          projectName: 'TestRIshabh',
          createdAt: '2021-02-04T10:21:09',
          isKanban: false,
        },
      ],
    };
    component.selectedView = 'processor_state';
    component.getProjects();
    // fixture.detectChanges();
    httpMock
      .match(baseUrl + '/api/basicconfigs')[0]
      ?.flush(getProjectsResponse);
    // expect(component.userProjects).toEqual([{ "name": "TestUser", "id": "601bca9569515b0001d68182" }]);
  });

  it('should allow user to select project', () => {
    const selectedProjects = {
      originalEvent: { isTrusted: true },
      value: { id: '601bca9569515b0001d68182', name: 'test' },
      itemValue: '601bca9569515b0001d68182',
    };
    const processorName = 'Jira';
    component.updateProjectSelection(selectedProjects);
    // fixture.detectChanges();
    expect(component.selectedProject).toEqual({
      id: '601bca9569515b0001d68182',
      name: 'test',
    });
  });

  it('should run Jira Processor for the selected projects', () => {
    component.selectedProject = {
      id: '601bca9569515b0001d68182',
      name: 'test',
    };
    component.processorData = {
      data: [
        {
          processorName: 'Jira',
          executionOngoing: true,
          errorMessage: 'test',
        },
        {
          processorName: 'Github',
          executionOngoing: true,
        },
      ],
    };
    component.processorsTracelogs = [
      {
        processorName: 'Jira',
        executionOngoing: true,
        errorMessage: 'test',
      },
    ];
    component.processorData = {
      data: [
        {
          processorName: 'Jira',
          loader: true,
        },
        {
          processorName: 'Github',
          loader: true,
        },
      ],
    };
    component.runProcessor('Jira');
    fixture.detectChanges();
    httpMock
      .match(baseUrl + '/api/processor/trigger/Jira')[0]
      .flush({
        message:
          'Got HTTP response: 200 on url: http://jira_processor:50008/processor/run',
        success: true,
      });
  });

  it('should continue get stacks of jira processor untill flag true', fakeAsync(() => {
    component.selectedProject = {
      id: '601bca9569515b0001d68182',
      name: 'test',
    };
    component.processorData = {
      data: [
        {
          processorName: 'Jira',
          executionOngoing: true,
          errorMessage: 'test',
        },
        {
          processorName: 'Github',
          executionOngoing: true,
        },
      ],
    };
    component.processorsTracelogs = [
      {
        processorName: 'Jira',
        executionOngoing: true,
        errorMessage: '',
        progressStatusList: [
          {
            stepName: 'Process Issues 0 to 49 out of 475, Board ID : 22',
            endTime: 1716799109813,
            status: 'COMPLETED',
          },
        ],
      },
    ];
    const response = {
      success: true,
      data: [
        {
          executionOngoing: true,
          errorMessage: null,
          progressStatusList: [
            {
              stepName: 'Process Issues 0 to 49 out of 475, Board ID : 22',
              endTime: 1716799109813,
              status: 'COMPLETED',
            },
          ],
        },
      ],
    };
    spyOn(httpService, 'runProcessor').and.returnValue(of(response));
    let jiraStatusContinuePulling = true;
    const mockProgressStatusResponse = {
      success: true,
      data: [
        {
          executionOngoing: true,
          progressStatusList: [
            {
              stepName: 'Process Issues 0 to 49 out of 475, Board ID : 22',
              endTime: 1716799109813,
              status: 'COMPLETED',
            },
          ],
        },
      ],
    };
    const continueCall = spyOn(
      httpService,
      'getProgressStatusOfProcessors',
    ).and.callFake(() => {
      return of(mockProgressStatusResponse).pipe(
        takeWhile(() => jiraStatusContinuePulling),
      );
    });
    component.runProcessor('Jira');
    tick(15000);
    jiraStatusContinuePulling = false;
    discardPeriodicTasks();
    expect(component.processorsTracelogs).toBeDefined();
  }));

  it('should stop get stacks of jira processor when flagis false', fakeAsync(() => {
    component.selectedProject = {
      id: '601bca9569515b0001d68182',
      name: 'test',
    };
    component.processorData = {
      data: [
        {
          processorName: 'Jira',
          executionOngoing: true,
          errorMessage: 'test',
        },
        {
          processorName: 'Github',
          executionOngoing: true,
        },
      ],
    };
    component.processorsTracelogs = [
      {
        processorName: 'Jira',
        executionOngoing: true,
        errorMessage: 'test',
      },
    ];
    const response = {
      success: true,
      data: [{ executionOngoing: true, errorMessage: null }],
    };
    spyOn(httpService, 'runProcessor').and.returnValue(of(response));
    let jiraStatusContinuePulling = true;
    const mockProgressStatusResponse = {
      success: true,
      data: [{ executionOngoing: false }],
    };
    const continueCall = spyOn(
      httpService,
      'getProgressStatusOfProcessors',
    ).and.callFake(() => {
      return of(mockProgressStatusResponse).pipe(
        takeWhile(() => jiraStatusContinuePulling),
      );
    });
    component.runProcessor('Jira');
    tick(3000);
    jiraStatusContinuePulling = false;
    discardPeriodicTasks();
    expect(component.processorsTracelogs).toBeDefined();
  }));

  it('should run Github Processor for the selected projects', () => {
    component.processorData = {
      data: [
        {
          processorName: 'Jira',
          loader: true,
        },
        {
          processorName: 'Github',
          loader: true,
        },
      ],
    };
    component.processorData = {
      data: [
        {
          processorName: 'Jira',
          executionOngoing: true,
        },
        {
          processorName: 'Github',
          executionOngoing: true,
        },
      ],
    };
    component.selectedProject = {
      id: '601bca9569515b0001d68182',
      name: 'test',
    };
    component.runProcessor('Github');
    // fixture.detectChanges();
    httpMock
      .match(baseUrl + '/api/processor/trigger/Github')[0]
      .flush({
        message:
          'Got HTTP response: 200 on url: http://nonjira-processor:50008/processor/run',
        success: true,
      });
  });

  it('should all tools config', fakeAsync(() => {
    const basicProjectConfigId = '63b51633f33fd2360e9e72bd';
    spyOn(httpService, 'getAllToolConfigs').and.returnValue(
      of(fakeGetAllTools),
    );
    component.getAllToolConfigs(basicProjectConfigId);
    tick();
    expect(component.toolConfigsDetails.length).toEqual(
      fakeGetAllTools.data.length,
    );
  }));

  it('should get processors trace logs for project', fakeAsync(() => {
    component.processorData = {
      data: [
        {
          processorName: 'Jira',
          loader: true,
        },
        {
          processorName: 'Github',
          loader: true,
        },
      ],
    };
    const basicProjectConfigId = '63b51633f33fd2360e9e72bd';
    component.processorData = {
      data: [
        {
          processorName: 'Jira',
          executionOngoing: true,
        },
        {
          processorName: 'Github',
          executionOngoing: true,
        },
      ],
    };
    component.processorsTracelogs = [
      {
        processorName: 'Jira',
        executionOngoing: true,
        progressStatusList: [
          {
            stepName: 'Process Issues 0 to 49 out of 475, Board ID : 22',
            endTime: 1716799109813,
            status: 'COMPLETED',
          },
        ],
      },
      {
        processorName: 'Github',
        executionOngoing: true,
      },
    ];
    const fakeProcessorsTracelog = {
      success: true,
      data: [
        {
          processorName: 'Jira',
          executionOngoing: true,
          progressStatusList: [
            {
              stepName: 'Process Issues 0 to 49 out of 475, Board ID : 22',
              endTime: 1716799109813,
              status: 'COMPLETED',
            },
          ],
        },
      ],
    };
    spyOn(httpService, 'getProcessorsTraceLogsForProject').and.returnValue(
      of(fakeProcessorsTracelog),
    );
    const response = {
      success: true,
      data: [{ executionOngoing: true, errorMessage: null }],
    };
    spyOn(httpService, 'runProcessor').and.returnValue(of(response));
    let jiraStatusContinuePulling = true;
    const mockProgressStatusResponse = {
      success: true,
      data: [{ executionOngoing: true }],
    };
    const continueCall = spyOn(
      httpService,
      'getProgressStatusOfProcessors',
    ).and.callFake(() => {
      return of(mockProgressStatusResponse).pipe(
        takeWhile(() => jiraStatusContinuePulling),
      );
    });
    component.getProcessorsTraceLogsForProject(basicProjectConfigId);
    tick(3000);
    jiraStatusContinuePulling = false;
    discardPeriodicTasks();
    expect(component.processorsTracelogs).toBeDefined();
    expect(component.processorsTracelogs.length).toEqual(
      fakeProcessorsTracelog.data.length,
    );
  }));

  it('should disable processor when user is not Super admin', () => {
    const getAuthorizationService = TestBed.inject(GetAuthorizationService);
    spyOn(getAuthorizationService, 'checkIfSuperUser').and.returnValue(true);
    expect(component.shouldDisableRunProcessor('jira')).toBe(false);
  });

  it('should disable processor when user is not Project admin', () => {
    const getAuthorizationService = TestBed.inject(GetAuthorizationService);
    spyOn(getAuthorizationService, 'checkIfProjectAdmin').and.returnValue(true);
    expect(component.shouldDisableRunProcessor('jira')).toBe(false);
  });

  it('should enable processor when user is Project admin/Super Admin', () => {
    const getAuthorizationService = TestBed.inject(GetAuthorizationService);
    spyOn(getAuthorizationService, 'checkIfProjectAdmin').and.returnValue(
      false,
    );
    spyOn(getAuthorizationService, 'checkIfSuperUser').and.returnValue(false);
    expect(component.shouldDisableRunProcessor('jira')).toBe(true);
  });

  it('should delete tool when trying to delete for any project', () => {
    const processDetails = {
      id: '63b51633f33fd2360e9e72bd',
      toolName: 'Sonar',
      basicProjectConfigId: '63b51633f33fd2360e9e72bd',
      connectionId: '62fcbe4adac8a44cd2cb9576',
      brokenConnection: false,
      connectionName: 'Connection Server 1',
      projectKey: 'projectKey',
      branch: 'branchName',
      apiVersion: '8.x',
      createdAt: '2023-10-06T11:55:36',
      updatedAt: '2023-10-06T11:55:36',
      queryEnabled: false,
      boards: [null],
      organizationKey: '',
      gitLabSdmID: '',
      azureIterationStatusFieldUpdate: false,
    };
    const selectedProject = {
      id: '63b51633f33fd2360e9e72bd',
      name: 'testprojName',
    };

    component.toolConfigsDetails = [
      {
        basicProjectConfigId: '63b51633f33fd2360e9e72bd',
        boardQuery: '',
        boards: [],
        connectionId: '62fcbe4adac8a44cd2cb9576',
        connectionName: 'Sunbelt Rental JIra',
        createdAt: '2023-01-04T06:02:20',
        id: '63b51633f33fd2360e9e72bd',
        projectKey: 'DOTC',
        queryEnabled: false,
        toolName: 'Jira',
        updatedAt: '2023-01-04T06:02:20',
      },
    ];
    component.deleteProcessorDataReq(processDetails, selectedProject);
    // expect(component.getToolDetailsForProcessor(processDetails.toolName)?.length).toBeGreaterThan(0);
  });

  it('should NA if processor response not came', () => {
    component.showProcessorLastState('Jira');
    const respo = component.showProcessorLastState('Jira');
    expect(respo).toBe('NA');
  });

  it('should success if processor response is success', () => {
    component.processorsTracelogs = [
      {
        processorName: 'Jira',
        executionSuccess: true,
      },
    ];
    const respo = component.showProcessorLastState('Jira');
    expect(respo).toBe('Success');
  });

  it('should fail if processor response is fail', () => {
    component.processorsTracelogs = [
      {
        processorName: 'Jira',
        executionSuccess: false,
      },
    ];
    const respo = component.showProcessorLastState('Jira');
    expect(respo).toBe('Failure');
  });

  it('should delete processor on delete confirmation Yes', () => {
    const mockConfirm: any = spyOn<any>(
      confirmationService,
      'confirm',
    ).and.callFake((confirmation: Confirmation) => confirmation.accept());

    component.deleteProcessorData({ processorName: 'Jira' });
    expect(mockConfirm).toHaveBeenCalled();
  });

  it('should not delete processor on delete confirmation NO', () => {
    const mockReject: any = spyOn<any>(
      confirmationService,
      'confirm',
    ).and.callFake((confirmation: Confirmation) => confirmation.reject());
    component.deleteProcessorData({ processorName: 'Jira' });
    expect(mockReject).toHaveBeenCalled();
  });

  it('should return execution date of processor', () => {
    component.processorsTracelogs = [
      {
        processorName: 'Jira',
        executionSuccess: false,
        executionEndedAt: '2023-01-04T06:02:20',
      },
    ];
    const resp = component.showExecutionDate('Jira');
    expect(resp).not.toBe('NA');
  });

  xit('should fetch all the projects when superadmin', () => {
    component.userProjects = [];
    component.selectedProject = {};
    const response = fakeProjects;
    spyOn(httpService, 'getUserProjects').and.returnValue(of(response));
    spyOn(getAuthorizationService, 'checkIfSuperUser').and.returnValue(true);
    spyOn(component, 'getProcessorsTraceLogsForProject');
    const spy = spyOn(component, 'getAllToolConfigs');
    component.getProjects();
    expect(spy).toHaveBeenCalledWith(component.selectedProject['id']);
  });

  xit('should fetch all the projects when project admin', () => {
    component.userProjects = [];
    component.selectedProject = {};
    const response = fakeProjects;
    spyOn(httpService, 'getUserProjects').and.returnValue(of(response));
    spyOn(getAuthorizationService, 'checkIfProjectAdmin').and.returnValue(true);
    spyOn(component, 'getProcessorsTraceLogsForProject');
    const spy = spyOn(component, 'getAllToolConfigs');
    component.getProjects();
    expect(spy).toHaveBeenCalledWith(component.selectedProject['id']);
  });

  it('should not fetch all the projects', fakeAsync(() => {
    component.userProjects = [];
    component.selectedProject = {};
    const errResponse = {
      error: 'Something went wrong',
    };
    spyOn(httpService, 'getUserProjects').and.returnValue(of(errResponse));
    const spy = spyOn(messageService, 'add');
    component.getProjects();
    tick();
    expect(spy).toHaveBeenCalled();
  }));

  it('should not get all tools config', fakeAsync(() => {
    const basicProjectConfigId = '63b51633f33fd2360e9e72bd';
    const errResponse = {
      error: 'Something went wrong',
    };
    spyOn(httpService, 'getAllToolConfigs').and.returnValue(of(errResponse));
    const spy = spyOn(messageService, 'add');
    component.getAllToolConfigs(basicProjectConfigId);
    tick();
    expect(spy).toHaveBeenCalled();
  }));

  it('should get processors tracelog for project', fakeAsync(() => {
    const basicProjectConfigId = '63b51633f33fd2360e9e72bd';
    const errResponse = {
      error: 'Something went wrong',
    };
    component.processorData = {
      data: [
        {
          processorName: 'Jira',
          executionOngoing: true,
        },
        {
          processorName: 'Github',
          executionOngoing: true,
        },
      ],
    };
    component.processorsTracelogs = [
      {
        processorName: 'Jira',
        executionOngoing: true,
      },
      {
        processorName: 'Github',
        executionOngoing: true,
      },
    ];

    spyOn(httpService, 'getProcessorsTraceLogsForProject').and.returnValue(
      of(errResponse),
    );
    const spy = spyOn(messageService, 'add');
    component.getProcessorsTraceLogsForProject(basicProjectConfigId);
    tick();
    expect(spy).toHaveBeenCalled();
  }));

  // it('should not get processor data', fakeAsync(() => {
  //   component.dataLoading = true;
  //   const errResponse = {
  //     'error': "Something went wrong"
  //   };
  //   spyOn(httpService, 'getProcessorData').and.returnValue(of(errResponse));
  //   const spy = spyOn(messageService, 'add')
  //   // component.getProcessorData();
  //   tick();
  //   expect(spy).toHaveBeenCalled();
  //   expect(component.dataLoading).toBe(false);
  // }))

  it('should not run Processor when processor is jira', fakeAsync(() => {
    component.processorData = {
      data: [
        {
          processorName: 'Jira',
          executionOngoing: true,
          errorMessage: '',
        },
        {
          processorName: 'Github',
          executionOngoing: true,
        },
      ],
    };
    component.processorData = {
      data: [
        {
          processorName: 'Jira',
          loader: true,
        },
        {
          processorName: 'Github',
          loader: true,
        },
      ],
    };
    component.selectedProject = {
      id: '651af337d18501286c28a464',
    };
    component.processorsTracelogs = [
      {
        processorName: 'Jira',
        executionOngoing: true,
        errorMessage: '',
      },
    ];
    const errResponse = {
      data: 'Error in running Jira processor. Please try after some time.',
      message:
        'Got HTTP response: 404 on url: http://jira-processor:50008/api/job/startprojectwiseissuejob',
      success: false,
    };
    spyOn(component, 'isProjectSelected').and.returnValue(true);
    const spy = spyOn(httpService, 'runProcessor').and.returnValue(
      of(errResponse),
    );
    component.runProcessor('Jira');
    expect(spy).toHaveBeenCalled();
  }));

  it('should not run Processor when processor is not jira', fakeAsync(() => {
    component.processorData = {
      data: [
        {
          processorName: 'Jira',
          loader: true,
        },
        {
          processorName: 'Github',
          loader: true,
        },
      ],
    };
    component.processorData = {
      data: [
        {
          processorName: 'Jira',
          executionOngoing: true,
        },
        {
          processorName: 'Github',
          executionOngoing: true,
        },
      ],
    };
    component.selectedProject = {
      id: 'sdjsagdjagdjagd',
    };
    const errResponse = {
      error: 'Something went wrong',
    };
    spyOn(component, 'isProjectSelected').and.returnValue(false);
    const spy = spyOn(httpService, 'runProcessor').and.returnValue(
      of(errResponse),
    );
    component.runProcessor('Sonar');
    expect(spy).toHaveBeenCalled();
  }));
  it('should delete processor data', (done) => {
    const processorDetails = {
      toolName: 'Jira',
    };
    const selectedProject = {
      id: '601bca9569515b0001d68182',
    };
    const toolDetails = [
      {
        id: '123',
        // other properties
      },
      {
        id: '456',
        // other properties
      },
    ];
    spyOn(component, 'getToolDetailsForProcessor').and.returnValue(toolDetails);
    spyOn(httpService, 'deleteProcessorData').and.returnValues(
      of({ success: true }),
      of({ success: true }),
    );
    spyOn(messageService, 'add');
    spyOn(component, 'getAllToolConfigs');

    component.deleteProcessorDataReq(processorDetails, selectedProject);

    // fixture.detectChanges();

    expect(component.getToolDetailsForProcessor).toHaveBeenCalledWith('Jira');
    expect(httpService.deleteProcessorData).toHaveBeenCalledTimes(2);
    expect(httpService.deleteProcessorData).toHaveBeenCalledWith(
      '123',
      '601bca9569515b0001d68182',
    );
    expect(httpService.deleteProcessorData).toHaveBeenCalledWith(
      '456',
      '601bca9569515b0001d68182',
    );

    setTimeout(() => {
      expect(messageService.add).toHaveBeenCalledWith({
        severity: 'success',
        summary: 'Data deleted Successfully.',
        detail: '',
      });
      expect(component.getAllToolConfigs).toHaveBeenCalledWith(
        '601bca9569515b0001d68182',
      );
      done();
    });
  });

  it('should handle error when deleting processor data', (done) => {
    const processorDetails = {
      toolName: 'Jira',
    };
    const selectedProject = {
      id: '601bca9569515b0001d68182',
    };
    const toolDetails = [
      {
        id: '123',
        // other properties
      },
      {
        id: '456',
        // other properties
      },
    ];
    spyOn(component, 'getToolDetailsForProcessor').and.returnValue(toolDetails);
    spyOn(httpService, 'deleteProcessorData').and.returnValues(
      of({ success: true }),
      of({ success: false }),
    );
    spyOn(messageService, 'add');
    spyOn(component, 'getAllToolConfigs');

    component.deleteProcessorDataReq(processorDetails, selectedProject);

    // fixture.detectChanges();

    expect(component.getToolDetailsForProcessor).toHaveBeenCalledWith('Jira');
    expect(httpService.deleteProcessorData).toHaveBeenCalledTimes(2);
    expect(httpService.deleteProcessorData).toHaveBeenCalledWith(
      '123',
      '601bca9569515b0001d68182',
    );
    expect(httpService.deleteProcessorData).toHaveBeenCalledWith(
      '456',
      '601bca9569515b0001d68182',
    );

    setTimeout(() => {
      expect(messageService.add).toHaveBeenCalledWith({
        severity: 'error',
        summary: 'Error in deleting project data. Please try after some time.',
      });
      expect(component.getAllToolConfigs).not.toHaveBeenCalled();
      done();
    });
  });

  it('should handle error when getting tool details', () => {
    const processorDetails = {
      toolName: 'Jira',
    };
    const selectedProject = {
      id: '601bca9569515b0001d68182',
    };
    spyOn(component, 'getToolDetailsForProcessor').and.returnValue(null);
    spyOn(messageService, 'add');

    component.deleteProcessorDataReq(processorDetails, selectedProject);

    // fixture.detectChanges();

    expect(component.getToolDetailsForProcessor).toHaveBeenCalledWith('Jira');
    expect(messageService.add).toHaveBeenCalledWith({
      severity: 'error',
      summary: 'Something went wrong. Please try again after sometime.',
    });
  });

  it('should convert end time', () => {
    component.endTimeConversion('2023-01-04T06:02:20');
    expect(component.endTimeConversion).not.toBeNull();
  });

  it('should get toll category', () => {
    const spyobj = component.getToolCategory('azure');
    expect(spyobj).toBe('Project Management');
  });

  it('should get blank string if tollname dies not match', () => {
    const spyobj = component.getToolCategory('testtool');
    expect(spyobj).toBe('');
  });

  it('should update toggle Details and get success resonse', () => {
    component.selectedProject = { id: 'test' };
    spyOn(httpService, 'editTool').and.returnValue(of({ success: true }));
    component.azureRefreshActiveSprintReportToggleChange({ id: 'test' });
    expect(component.selectedProject).toBeDefined();
  });

  it('should update toggle Details and get failor resonse', () => {
    component.selectedProject = { id: 'test' };
    spyOn(httpService, 'editTool').and.returnValue(of({ success: false }));
    component.azureRefreshActiveSprintReportToggleChange({ id: 'test' });
    expect(component.selectedProject).toBeDefined();
  });

  it('should return NA when traceLog is undefined', () => {
    const result = component.getSCMToolTimeDetails('GitHub');
    expect(result).toBe('NA');
  });

  it('should return NA when executionResumesAt is 0', () => {
    // Arrange
    component.processorsTracelogs = [
      { processorName: 'GitHub', executionResumesAt: 0 },
    ];

    // Act
    const result = component.getSCMToolTimeDetails('GitHub');

    // Assert
    expect(result).toBe('NA');
  });

  it('should return a formatted date when executionResumesAt is a valid timestamp', () => {
    const validTimestamp = new Date().getTime();
    component.processorsTracelogs = [
      { processorName: 'GitHub', executionResumesAt: validTimestamp },
    ];
    const result = component.getSCMToolTimeDetails('GitHub');
    const expectedDate = new DatePipe('en-US').transform(
      validTimestamp,
      'dd-MMM-yyyy (EEE) - hh:mmaaa',
    );
    expect(result).toBe(expectedDate);
  });

  it('should return formatted date when executionResumesAt is valid', () => {
    const processorName = 'GitHub';
    const executionResumesAt = new Date('2023-10-01T10:00:00Z').getTime();
    component.processorsTracelogs = [
      { processorName: processorName, executionResumesAt: executionResumesAt },
    ];
    const result = component.getSCMToolTimeDetails(processorName);
    expect(result).toBe(
      new DatePipe('en-US').transform(
        executionResumesAt,
        'dd-MMM-yyyy (EEE) - hh:mmaaa',
      ),
    );
  });

  it('should return formatted date when valid processor name is provided', () => {
    const processorName = 'GitHub';
    component.processorsTracelogs = [
      { processorName: 'GitHub', executionResumesAt: new Date().getTime() },
    ];
    const result = component.getSCMToolTimeDetails(processorName);
    expect(result).toBe(
      new DatePipe('en-US').transform(
        component.processorsTracelogs[0].executionResumesAt,
        'dd-MMM-yyyy (EEE) - hh:mmaaa',
      ),
    );
  });

  it('should return true for GitHub', () => {
    const result = component.isSCMToolProcessor('GitHub');
    expect(result).toBe(true);
  });

  // it('should navigate to the project list', () => {
  // 	component.backToProjectList();
  // 	expect(router.navigate).toHaveBeenCalledWith(['/dashboard/Config/ProjectList']);
  // });
});
