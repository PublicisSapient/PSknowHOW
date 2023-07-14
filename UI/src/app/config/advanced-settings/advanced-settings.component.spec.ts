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

import { ComponentFixture, fakeAsync, TestBed, tick, waitForAsync } from '@angular/core/testing';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { HttpService } from '../../services/http.service';
import { APP_CONFIG, AppConfig } from '../../services/app.config';
import { DatePipe } from '../../../../node_modules/@angular/common';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { Routes } from '@angular/router';
import { environment } from 'src/environments/environment';
import { ToastModule } from 'primeng/toast';
import { TableModule } from 'primeng/table';
import { ConfirmationService, MessageService,Confirmation } from 'primeng/api';
import { AdvancedSettingsComponent } from './advanced-settings.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { GetAuthorizationService } from '../../services/get-authorization.service';
import { SharedService } from '../../services/shared.service';
import { of } from 'rxjs';
import { compileComponentFromMetadata } from '@angular/compiler';
describe('AdvancedSettingsComponent', () => {
  let component: AdvancedSettingsComponent;
  let fixture: ComponentFixture<AdvancedSettingsComponent>;
  let httpService;
  let httpMock;
  let confirmationService;
  const baseUrl = environment.baseUrl;  // Servers Env
  // var store = {};
  // var ls = function () {
  //   return JSON.parse(store['storage']);
  // };

  const fakeProcessorData = {
    message: '',
    success: true,
    data: [{
      id: '5e624a04e4b098dbca2aff3c',
      processorName: 'Jira',
      processorType: 'AgileTool',
      errors: [],
      updatedTime: 1583499780045,
      active: true,
      online: true
    }, {
      id: '5e624a7ce4b0d02e2e9938f0',
      processorName: 'Atm',
      processorType: 'AgileTool',
      errors: [],
      updatedTime: 1583731433007,
      active: true,
      online: true
    }, {
      id: '5e624a7ce4b0e00531062788',
      processorName: 'Excel',
      processorType: 'Excel',
      errors: [],
      updatedTime: 1583731500244,
      active: true,
      online: true
    }, {
      id: '5e625760e4b04faa250e493c',
      processorName: 'Bitbucket',
      processorType: 'Scm',
      errors: [],
      updatedTime: 1583730003541,
      active: true,
      online: true
    }, {
      id: '5e62e403e4b0a47e0405900c',
      processorName: 'Precalculated',
      processorType: 'Central',
      errors: [],
      updatedTime: 1583712007726,
      active: true,
      online: true
    }, {
      id: '5e62e405e4b034f67720d19b',
      processorName: 'Sonar',
      processorType: 'SonarDetails',
      errors: [],
      updatedTime: 1583715542758,
      active: true,
      online: true
    }, {
      id: '5e62e406e4b0baec876653b7',
      processorName: 'Newrelic',
      processorType: 'NewRelic',
      errors: [],
      updatedTime: 1583712003138,
      active: true,
      online: true
    }, {
      id: '5e62f217e4b0123940f939c2',
      processorName: 'CentralProducer',
      processorType: 'Central',
      errors: [],
      updatedTime: 1583715625877,
      active: true,
      online: true
    },
    {
      id: '61ef81046c1a0b242cdb188b',
      processorName: 'GitHub',
      processorType: 'Scm',
      errors: [],
      updatedTime: 1643094625006,
      active: true,
      online: false,
      lastSuccess: true
    }]
  };

  const switchViewEventProcessor = {
    originalEvent: {
      isTrusted: true
    },
    item: {
      label: 'Processor State',
      icon: 'pi pi-fw pi-cog',
      expanded: true
    }
  };

  const switchViewEventServerRole = {
    originalEvent: {
      isTrusted: true
    },
    item: {
      label: 'Server Role',
      icon: 'pi pi-fw pi-cog',
      expanded: true
    }
  };

  const fakeServerRole = {
    message: 'Data found for the key',
    success: true,
    data: {
      centralProducer: true
    }
  };

  const fakeSetRoleResponse = {
    message: 'Data saved for the key',
    success: true,
    data: {
      isCentralProducer: true
    }
  };

  const fakePreCalculatedConfig = {
    message: 'Success',
    success: true,
    data: {
      showPreCalculatedDataForScrum: true,
      showPreCalculatedDataForKanban: false
    }
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
        RouterTestingModule
      ],
      providers: [HttpService, MessageService, SharedService, DatePipe, GetAuthorizationService, ConfirmationService, { provide: APP_CONFIG, useValue: AppConfig }]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AdvancedSettingsComponent);
    component = fixture.componentInstance;
    httpService = TestBed.inject(HttpService);
    httpMock = TestBed.inject(HttpTestingController);
    confirmationService = TestBed.inject(ConfirmationService);
    fixture.detectChanges();
  });

  afterEach(() => {
    // store = {};
    fixture.destroy();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load processor data', (done) => {
    component.selectedView = 'processor_state';
    component.getProcessorData();
    fixture.detectChanges();
    httpMock.match(baseUrl + '/api/processor')[0].flush(fakeProcessorData);
    if (component.processorData['success']) {
      expect(Object.keys(component.processorData).length).toEqual(Object.keys(fakeProcessorData).length);
    } else {
      // component.messageService.add({ severity: 'error', summary: 'Error in fetching roles. Please try after some time.' });
    }
    done();
  });

  it('should switch view to Processor State', (done) => {
    component.switchView(switchViewEventProcessor);
    fixture.detectChanges();
    expect(component.selectedView).toBe('processor_state');
    done();
  });

  it('should fetch all user projects', () => {
    const getAuthorizationService = TestBed.inject(GetAuthorizationService);
    const getProjectsResponse = { message: 'Fetched successfully', success: true, data: [{ id: '601bca9569515b0001d68182', projectName: 'TestRIshabh', createdAt: '2021-02-04T10:21:09', isKanban: false }] };
    component.selectedView = 'processor_state';
    component.getProjects();
    fixture.detectChanges();
    httpMock.match(baseUrl + '/api/basicconfigs')[0].flush(getProjectsResponse);
    // expect(component.userProjects).toEqual([{ "name": "TestUser", "id": "601bca9569515b0001d68182" }]);
  });

  it('should allow user to select project', () => {
    const selectedProjects = { originalEvent: { isTrusted: true }, value: {id: '601bca9569515b0001d68182', name: 'test'}, itemValue: '601bca9569515b0001d68182' };
    const processorName = 'Jira';
    component.updateProjectSelection(selectedProjects);
    fixture.detectChanges();
    expect(component.selectedProject).toEqual({id: '601bca9569515b0001d68182', name: 'test'});
  });

  it('should run Jira Processor for the selected projects', () => {
    component.selectedProject = {id: '601bca9569515b0001d68182', name: 'test'};
    component.runProcessor('Jira');
    fixture.detectChanges();
    httpMock.match(baseUrl + '/api/processor/trigger/Jira')[0].flush({ message: 'Got HTTP response: 200 on url: http://jira_processor:50008/processor/run', success: true });
  });

  it('should run Github Processor for the selected projects', () => {
    component.selectedProject = {id: '601bca9569515b0001d68182', name: 'test'};
    component.runProcessor('Github');
    fixture.detectChanges();
    httpMock.match(baseUrl + '/api/processor/trigger/Github')[0].flush({ message: 'Got HTTP response: 200 on url: http://nonjira-processor:50008/processor/run', success: true });
  });

  it('should all tools config', fakeAsync(() => {
    const basicProjectConfigId = '63b51633f33fd2360e9e72bd';
    spyOn(httpService, 'getAllToolConfigs').and.returnValue(of(fakeGetAllTools));
    component.getAllToolConfigs(basicProjectConfigId);
    tick();
    expect(component.toolConfigsDetails.length).toEqual(fakeGetAllTools.data.length);
  }))

  it('should get processors trace logs for project', fakeAsync(() => {
    const basicProjectConfigId = '63b51633f33fd2360e9e72bd';
    spyOn(httpService, 'getProcessorsTraceLogsForProject').and.returnValue(of(fakeProcessorsTracelog));
    component.getProcessorsTraceLogsForProject(basicProjectConfigId);
    tick();
    expect(component.processorsTracelogs.length).toEqual(fakeProcessorsTracelog.data.length);
  }));

  it('should disable processor when user is not Super admin', () => {
    const getAuthorizationService = TestBed.inject(GetAuthorizationService);
    spyOn(getAuthorizationService, 'checkIfSuperUser').and.returnValue(true);
    expect(component.shouldDisableRunProcessor()).toBe(false);
  });

  it('should disable processor when user is not Project admin', () => {
    const getAuthorizationService = TestBed.inject(GetAuthorizationService);
    spyOn(getAuthorizationService, 'checkIfProjectAdmin').and.returnValue(true);
    expect(component.shouldDisableRunProcessor()).toBe(false);
  });

  it('should enable processor when user is Project admin/Super Admin', () => {
    const getAuthorizationService = TestBed.inject(GetAuthorizationService);
    spyOn(getAuthorizationService, 'checkIfProjectAdmin').and.returnValue(
      false,
    );
    spyOn(getAuthorizationService, 'checkIfSuperUser').and.returnValue(false);
    expect(component.shouldDisableRunProcessor()).toBe(true);
  });

  it('should delete tool when trying to delete for any project', () => {
    const processDetails = {
      active: true,
      errors: [],
      id: '63b3f50b6d8d7f44def6ec2f',
      lastSuccess: true,
      online: true,
      processorName: 'Jira',
      processorType: 'AgileTool',
      updatedTime: 1673222624309,
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
        id: '63b5166cf33fd2360e9e72c2',
        projectKey: 'DOTC',
        queryEnabled: false,
        toolName: 'Jira',
        updatedAt: '2023-01-04T06:02:20',
      },
    ];
    component.deleteProcessorDataReq(processDetails,selectedProject);
    expect(component.getToolDetailsForProcessor('Jira').length).toBeGreaterThan(0);
  });

  it("should NA if processor response not came",()=>{
    component.showProcessorLastState('Jira')
    const respo = component.showProcessorLastState('Jira')
    expect(respo).toBe("NA")
  })

  it("should success if processor response is success",()=>{
    component.processorsTracelogs = [{
      processorName : 'Jira',
      executionSuccess : true
    }]
    const respo = component.showProcessorLastState('Jira')
    expect(respo).toBe("Success")
  })

  it("should fail if processor response is fail",()=>{
    component.processorsTracelogs = [{
      processorName : 'Jira',
      executionSuccess : false
    }]
    const respo = component.showProcessorLastState('Jira')
    expect(respo).toBe("Failure")
  })

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

  it("should return execution date of processor",()=>{

    component.processorsTracelogs = [{
      processorName : 'Jira',
      executionSuccess : false,
      executionEndedAt : '2023-01-04T06:02:20'
    }]
    const resp = component.showExecutionDate('Jira')
    expect(resp).not.toBe("NA")
  })  
});
