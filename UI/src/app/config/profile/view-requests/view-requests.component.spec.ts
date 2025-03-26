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

import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { ToastModule } from 'primeng/toast';
import { InputSwitchModule } from 'primeng/inputswitch';
import { FormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { HttpService } from '../../../services/http.service';
import { SharedService } from '../../../services/shared.service';
import { APP_CONFIG, AppConfig } from '../../../services/app.config';
import { MessageService } from 'primeng/api';
import { DropdownModule } from 'primeng/dropdown';
import { environment } from 'src/environments/environment';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { TableModule } from 'primeng/table';
import { ViewRequestsComponent } from './view-requests.component';
import { of } from 'rxjs';
import { GetAuthorizationService } from '../../../services/get-authorization.service';

describe('ViewRequestsComponent', () => {
  let component: ViewRequestsComponent;
  let fixture: ComponentFixture<ViewRequestsComponent>;
  let httpService: HttpService;
  let httpMock;
  let messageService;
  let authService;
  const baseUrl = environment.baseUrl;
  let sharedService;

  const fakeRequestsData = {
    message: 'Found access_requests for status Pending',
    success: true,
    data: [
      {
        _id: '5da428e6e645ca28a026e729',
        username: 'testUser',
        status: 'Pending',
        projects: [
          {
            projectName: 'check',
            projectId: 'a',
          },
        ],
        roles: [
          {
            _id: '5da03f242afa421ae416cad7',
            roleName: 'ROLE_PROJECT_VIEWER',
          },
        ],
      },
      {
        _id: '5da46ff3e645ca33dc927b83',
        username: 'testUser',
        status: 'Pending',
        reviewComments: '',
        projects: [
          {
            projectName:
              'Automotive | Cooper Tire & Rubber Company | 196036 | Digital AOR',
            projectId:
              'Automotive | Cooper Tire & Rubber Company | 196036 | Digital AOR_68500_Automotive | Cooper Tire & Rubber Company | 196036 | Digital AOR',
          },
        ],
        roles: [
          {
            _id: '5da46000e645ca33dc927b4a',
            roleName: 'ROLE_PROJECT_VIEWER',
          },
        ],
      },
      {
        _id: '5da47bdde645ca33dc927ba8',
        username: 'testUser',
        status: 'Pending',
        reviewComments: '',
        projects: [
          {
            projectName:
              'Automotive | Cooper Tire & Rubber Company | 196036 | Digital AOR',
            projectId:
              'Automotive | Cooper Tire & Rubber Company | 196036 | Digital AOR_68500_Automotive | Cooper Tire & Rubber Company | 196036 | Digital AOR',
          },
        ],
        roles: [
          {
            _id: '5da03f242afa421ae416cad7',
            roleName: 'ROLE_PROJECT_VIEWER',
          },
        ],
      },
      {
        _id: '5da47c2ae645ca33dc927bb3',
        username: 'testUser',
        status: 'Pending',
        reviewComments: '',
        projects: [
          {
            projectName: 'DTI',
            projectId: 'DTI_63102_DTI',
          },
        ],
        roles: [
          {
            _id: '5da03f242afa421ae416cad7',
            roleName: 'ROLE_PROJECT_VIEWER',
          },
        ],
      },
    ],
  };

  const fakeRolesData = {
    message: 'Found all roles',
    success: true,
    data: [
      {
        id: '6026576bb975135001bc3487',
        roleName: 'ROLE_PROJECT_VIEWER',
        roleDescription: 'read kpi data at project level',
        createdDate: 1613125483944,
        lastModifiedDate: 1613125483944,
        isDeleted: 'False',
        permissions: [
          {
            id: '6026576bae81aeece081fa7a',
            permissionName: 'View',
            operationName: 'Read',
            resourceName: 'resource4',
            resourceId: '6026576a4ed204e1a35f10bc',
            createdDate: 1613125482976,
            lastModifiedDate: 1613125482976,
            isDeleted: 'False',
          },
        ],
      },
      {
        id: '6026576bb975135001bc3488',
        roleName: 'ROLE_PROJECT_ADMIN',
        roleDescription: 'manage user-roles at project level',
        createdDate: 1613125483944,
        lastModifiedDate: 1613125483944,
        isDeleted: 'False',
        permissions: [
          {
            id: '6026576bae81aeece081fa7a',
            permissionName: 'View',
            operationName: 'Read',
            resourceName: 'resource4',
            resourceId: '6026576a4ed204e1a35f10bc',
            createdDate: 1613125482976,
            lastModifiedDate: 1613125482976,
            isDeleted: 'False',
          },
        ],
      },
    ],
  };

  const fakeAcceptRequestData = {
    id: '61cc34b463780d7cd623cc04',
    username: 'testUserQA',
    status: 'Pending',
    reviewComments: '',
    role: 'ROLE_PROJECT_ADMIN',
    accessNode: {
      accessLevel: 'Project',
      accessItems: [
        {
          itemId: '61c9ba5200d5d4170ced9f74',
          itemName: 'Jiraproj1',
        },
        {
          itemId: '61c99113aeb8700e3a3ac1ed',
          itemName: 'healproj',
        },
      ],
    },
    deleted: false,
  };

  const fakeRequestResponse = {
    message: 'Granted',
    success: true,
  };

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ViewRequestsComponent],
      imports: [
        InputSwitchModule,
        FormsModule,
        RouterTestingModule.withRoutes([]),
        HttpClientTestingModule,
        BrowserAnimationsModule,
        TableModule,
        ToastModule,
        DropdownModule,
      ],
      providers: [
        HttpService,
        MessageService,
        SharedService,
        GetAuthorizationService,
        { provide: APP_CONFIG, useValue: AppConfig },
      ],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewRequestsComponent);
    component = fixture.componentInstance;
    httpService = TestBed.get(HttpService);
    httpMock = TestBed.get(HttpTestingController);
    messageService = TestBed.inject(MessageService);
    authService = TestBed.inject(GetAuthorizationService);
    sharedService = TestBed.inject(SharedService);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load pending requests on load', (done) => {
    component.ngOnInit();
    fixture.detectChanges();
    httpMock
      .match(baseUrl + '/api/accessrequests/status/' + 'Pending')[0]
      .flush(fakeRequestsData);
    if (component.accessRequestData['success']) {
      expect(Object.keys(component.accessRequestList).length).toEqual(
        Object.keys(fakeRequestsData.data).length,
      );
    } else {
      // component.messageService.add({ severity: 'error', summary: 'Error in fetching roles. Please try after some time.' });
    }
    done();
  });

  it('should load roles data on load', (done) => {
    component.ngOnInit();
    fixture.detectChanges();
    httpMock.match(baseUrl + '/api/roles')[0].flush(fakeRolesData);
    if (component.rolesData['success']) {
      expect(Object.keys(component.roleList).length).toEqual(
        Object.keys(fakeRolesData.data).length,
      );
    } else {
      // component.messageService.add({ severity: 'error', summary: 'Error in fetching roles. Please try after some time.' });
    }
    done();
  });

  it('should accept access request', (done) => {
    const requestId = '61cc34b463780d7cd623cc04';
    component.approveRejectRequest(fakeAcceptRequestData, true);
    fixture.detectChanges();
    httpMock
      .match(baseUrl + '/api/accessrequests/' + requestId)[0]
      .flush(fakeAcceptRequestData);
    // httpMock.match(baseUrl + '/api/accessrequests/5de631c02ab79c000990489e')[0].flush(fakeRequestResponse);
    if (component.acceptRequestData['success']) {
      fixture.detectChanges();
      // expect(fakeRequestResponse.success).toContain(true);
      expect(Object.keys(component.acceptRequestData['message'])).toEqual(
        Object.keys(fakeRequestResponse.message),
      );
    } else {
      // this.messageService.add({ severity: 'error', summary: 'Error in updating request. Please try after some time.' });
    }
    done();
  });

  it('should give error when getting requests', () => {
    spyOn(httpService, 'getAccessRequests').and.returnValue(of('Error'));
    const spy = spyOn(messageService, 'add');
    component.dataLoading = [];
    component.getRequests();
    expect(spy).toHaveBeenCalled();
    expect(component.dataLoading).toEqual(['allRequests']);
  });

  it('should getRolesList when project admin', () => {
    const response = fakeRolesData;
    component.rolesData = [];
    component.roleList = [];
    spyOn(authService, 'checkIfProjectAdmin').and.returnValue(true);
    spyOn(httpService, 'getRolesList').and.returnValue(of(response));
    component.getRolesList();
    expect(component.roleList.length).toEqual(response.data.length);
  });

  it('should handle error when getting role list', () => {
    const errResponse = {
      error: 'Error in fetching roles',
      success: false,
    };
    component.rolesData = [];
    spyOn(httpService, 'getRolesList').and.returnValue(of(errResponse));
    const spy = spyOn(messageService, 'add');
    component.getRolesList();
    expect(spy).toHaveBeenCalled();
  });

  it('should approve reject request when role is not superadmin', () => {
    const reqData = {
      id: '61cc34b463780d7cd623cc04',
      username: 'testUserQA',
      status: 'Pending',
      reviewComments: '',
      role: 'ROLE_PROJECT_ADMIN',
      deleted: false,
    };
    const spy = spyOn(messageService, 'add');
    component.approveRejectRequest(reqData, true);
    expect(spy).toHaveBeenCalled();
  });

  it('should approve reject request success', () => {
    spyOn(httpService, 'updateAccessRequest').and.returnValue(
      of(fakeRequestResponse),
    );
    component.acceptRequestData = [];
    const spy = spyOn(messageService, 'add');
    spyOn(component, 'getRequests').and.callThrough();
    spyOn(sharedService, 'notificationUpdate');
    component.approveRejectRequest(fakeAcceptRequestData, true);
    expect(spy).toHaveBeenCalled();
  });
});
