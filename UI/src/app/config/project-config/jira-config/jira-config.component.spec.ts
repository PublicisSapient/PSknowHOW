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
import { JiraConfigComponent } from './jira-config.component';
import { MessageService, ConfirmationService } from 'primeng/api';
import { HttpService } from '../../../services/http.service';
import { SharedService } from '../../../services/shared.service';
import { GetAuthorizationService } from '../../../services/get-authorization.service';
import { FormGroup, ReactiveFormsModule, FormsModule, FormBuilder } from '@angular/forms';
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

  const fakeJiraConnections = require('../../../../test/resource/fakeJiraConnections.json');
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
    boardQuery: ''
  }];
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
    baseUrl: 'https://tools.publicis.sapient.com/jira',
    username: '',
    password: '',
    apiEndPoint: 'rest/api/2/',
    consumerKey: '',
    privateKey: '',
    isOAuth: false,
    offline: true,
    offlineFilePath: ''
  };

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
});
