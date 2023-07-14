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

import { ComponentFixture, fakeAsync, inject, TestBed, waitForAsync } from '@angular/core/testing';
import { ToastModule } from 'primeng/toast';
import { InputSwitchModule } from 'primeng/inputswitch';
import { RaiseAccessRequestComponent } from './raise-access-request.component';
import { FormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HttpService } from '../../../services/http.service';
import { APP_CONFIG, AppConfig } from '../../../services/app.config';
import { MessageService } from 'primeng/api';
import { environment } from 'src/environments/environment';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { from, of } from 'rxjs';
import { Router } from '@angular/router';
import { SharedService } from 'src/app/services/shared.service';

describe('RaiseRequestComponent', () => {
  let component: RaiseAccessRequestComponent;
  let fixture: ComponentFixture<RaiseAccessRequestComponent>;
  let httpService: HttpService;
  let httpMock;
  let messageService;
  const baseUrl = environment.baseUrl;  // Servers Env

  const fakeRoleList = require('../../../../test/resource/fakeRolesList.json');
  const fakeRolesData = require('../../../../test/resource/fakeRolesData.json');

  const fakeRequestData = {
    username: 'testUser',
    status: 'Pending',
    reviewComments: '',
    roles: [{
      _id: '5da03f242afa421ae416cad7',
      roleName: 'ROLE_PROJECT_VIEWER'
    }],
    projects: [{
      projectId: 'DTI_63102_DTI',
      projectName: 'DTI'
    }]
  };

  const fakeRequestResponse = {
    message: 'created new access_request',
    success: true,
    data: [{
      _id: '5da47c2ae645ca33dc927bb3',
      username: 'testUser',
      status: 'Pending',
      reviewComments: '',
      projects: [{
        projectName: 'DTI',
        projectId: 'DTI_63102_DTI'
      }],
      roles: [{
        _id: '5da03f242afa421ae416cad7',
        roleName: 'ROLE_PROJECT_VIEWER'
      }]
    }]
  };

  const selectedItem = {
    nodeId: 'DOJO Transformation Internal',
    nodeName: 'DOJO Transformation Internal',
    isSelected: false,
    itemName: 'DOJO Transformation Internal',
    id: 1
  };

  const fakeSelectedProject = {
    nodeId: 'DTI_63102_DTI',
    nodeName: 'DTI',
    isSelected: false,
    itemName: 'DTI_63102_DTI',
    id: 4
  };

  const fakeSelectedProjectArr = [{
    nodeId: 'DTI_63102_DTI',
    nodeName: 'DTI',
    isSelected: false,
    itemName: 'DTI_63102_DTI',
    id: 4
  }];

  const fakeRequestDataProjectsArr = [{
    projectId: 'DTI_63102_DTI',
    projectName: 'DTI'
  }];

  const fakeRole = {
    _id: '5da03f242afa421ae416cad7',
    roleName: 'ROLE_PROJECT_VIEWER',
    roleDescription: 'kpi data at project level',
    createdDate: 1570783012645,
    lastModifiedDate: 1570783012646,
    isDeleted: 'False',
    permissions: [{
      _id: '5d96dbb1abcd3e3e10b772f6',
      permissionName: 'View',
      operationName: 'Read',
      resourceName: 'resource4',
      resourceId: '5d932a126c7b0f37981a2cdc',
      createdDate: 1570167729143,
      lastModifiedDate: 1570167729143,
      isDeleted: 'False'
    }]
  };

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [RaiseAccessRequestComponent],
      imports: [
        ToastModule,
        InputSwitchModule,
        FormsModule,
        RouterTestingModule.withRoutes([]),
        HttpClientTestingModule,
        BrowserAnimationsModule
      ],
      providers: [HttpService, MessageService,SharedService
        , { provide: APP_CONFIG, useValue: AppConfig }]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RaiseAccessRequestComponent);
    component = fixture.componentInstance;
    httpService = TestBed.inject(HttpService);
    httpMock = TestBed.inject(HttpTestingController);
    messageService = TestBed.inject(MessageService);
    // fixture.detectChanges();
  });

  afterEach(() => {
    // destroy the component to cancel the timer again
    fixture.destroy();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('Should check roles data on load', () => {
    // fixture.detectChanges();
    component.ngOnInit();
    httpMock.match(baseUrl + '/api/roles')[0].flush(fakeRolesData);
    expect(component.roleList).toEqual(fakeRolesData.data);
  });

  it('Should check if no roles is returned', () => {
    // fixture.detectChanges();
    component.ngOnInit();
    httpMock.match(baseUrl + '/api/roles')[0].flush([{ error: 'error' }]);
    // expect(component.roleList).toEqual(fakeRolesData.data);
  });

  it('Should check if request is getting submitted with successfully', () => {
    const submittedResponse = {
      success: true
    };
    component.roleList = [
      {
        active: true
      }
    ];
    spyOn(httpService, 'saveAccessRequest').and.callFake(() => from([submittedResponse]));
    spyOn(messageService, 'add');
    component.submitRequest();
    expect(component.roleSelected).toBeFalsy();
    expect(component.roleList[0]['active']).toBeFalsy();
  });

  it('Should check if request is getting submitted without successfully', () => {
    const submittedResponse = {
      error: true
    };
    component.roleList = [
      {
        active: true
      }
    ];
    spyOn(httpService, 'saveAccessRequest').and.callFake(() => from([submittedResponse]));
    spyOn(messageService, 'add');
    component.submitRequest();
  });

  /*it('should check if project is getting selected', () => {
    component.selectedProject = [{ 'projectId': 'DTI_63102_DTI', 'projectName': 'DTI', 'isSelected': false, 'projectConfigId': 'DTI_63102_DTI', 'id': 4 }];
    component.onProjectSelect();
    // fixture.detectChanges();
    expect(component.requestData['projects']).toEqual(fakeRequestDataProjectsArr);
  });

  it('should check if project is getting deselected', () => {
    component.selectedProject = [{ 'projectId': 'DTI_63102_DTI', 'projectName': 'DTI', 'isSelected': false, 'projectConfigId': 'DTI_63102_DTI', 'id': 4 }];
    component.onProjectDeSelect();
    // fixture.detectChanges();
    expect(component.requestData['projects']).toEqual(fakeRequestDataProjectsArr);
  });

  it('should check if project is getting selected all', () => {
    component.projects = [{ 'projectId': 'DTI_63102_DTI', 'projectName': 'DTI', 'isSelected': false, 'projectConfigId': 'DTI_63102_DTI', 'id': 4 }];
    component.onProjectSelectAll();
    // fixture.detectChanges();
    expect(component.requestData['projects']).toEqual(fakeRequestDataProjectsArr);
  });

  it('should check if project is getting deselected all', () => {
    component.projects = [{ 'projectId': 'DTI_63102_DTI', 'projectName': 'DTI', 'isSelected': false, 'projectConfigId': 'DTI_63102_DTI', 'id': 4 }];
    component.onProjectDeSelectAll();
    // fixture.detectChanges();
    expect(component.requestData['projects']).toEqual(fakeRequestDataProjectsArr);
  });*/

  it('should check if role is being selected', () => {
    component.selectRole(fakeRole, fakeRoleList);
    // fixture.detectChanges();
    expect(component.roleSelected).toBeTruthy();
  });

  it('should logout application', inject([Router], (router: Router) => {
    spyOn(httpService, 'logout').and.returnValue(of({}));
    spyOn(router, 'navigate').and.stub();
    fixture.detectChanges();
    component.logout();
    expect(router.navigate).toHaveBeenCalledWith(['./authentication/login']);
  }))

});
