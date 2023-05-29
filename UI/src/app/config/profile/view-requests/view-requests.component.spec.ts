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
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HttpService } from '../../../services/http.service';
import { SharedService } from '../../../services/shared.service';
import { APP_CONFIG, AppConfig } from '../../../services/app.config';
import { MessageService } from 'primeng/api';
import { DropdownModule } from 'primeng/dropdown';
import { environment } from 'src/environments/environment';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { TableModule } from 'primeng/table';
import { ViewRequestsComponent } from './view-requests.component';

describe('ViewRequestsComponent', () => {
  let component: ViewRequestsComponent;
  let fixture: ComponentFixture<ViewRequestsComponent>;
  let httpService: HttpService;
  let httpMock;
  let messageService;
  const baseUrl = environment.baseUrl;

  const fakeRequestsData = {
    message: 'Found access_requests for status Pending',
    success: true,
    data: [{
      _id: '5da428e6e645ca28a026e729',
      username: 'testUser',
      status: 'Pending',
      projects: [{
        projectName: 'check',
        projectId: 'a'
      }],
      roles: [{
        _id: '5da03f242afa421ae416cad7',
        roleName: 'ROLE_PROJECT_VIEWER'
      }]
    }, {
      _id: '5da46ff3e645ca33dc927b83',
      username: 'testUser',
      status: 'Pending',
      reviewComments: '',
      projects: [{
        projectName: 'Automotive | Cooper Tire & Rubber Company | 196036 | Digital AOR',
        projectId: 'Automotive | Cooper Tire & Rubber Company | 196036 | Digital AOR_68500_Automotive | Cooper Tire & Rubber Company | 196036 | Digital AOR'
      }],
      roles: [{
        _id: '5da46000e645ca33dc927b4a',
        roleName: 'ROLE_PROJECT_VIEWER'
      }]
    }, {
      _id: '5da47bdde645ca33dc927ba8',
      username: 'testUser',
      status: 'Pending',
      reviewComments: '',
      projects: [{
        projectName: 'Automotive | Cooper Tire & Rubber Company | 196036 | Digital AOR',
        projectId: 'Automotive | Cooper Tire & Rubber Company | 196036 | Digital AOR_68500_Automotive | Cooper Tire & Rubber Company | 196036 | Digital AOR'
      }],
      roles: [{
        _id: '5da03f242afa421ae416cad7',
        roleName: 'ROLE_PROJECT_VIEWER'
      }]
    }, {
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

  const fakeRolesData = {
    message: 'Found all roles',
    success: true,
    data: [{
      id: '6026576bb975135001bc3487',
      roleName: 'ROLE_PROJECT_VIEWER',
      roleDescription: 'read kpi data at project level',
      createdDate: 1613125483944,
      lastModifiedDate: 1613125483944,
      isDeleted: 'False',
      permissions: [{
        id: '6026576bae81aeece081fa7a',
        permissionName: 'View',
        operationName: 'Read',
        resourceName: 'resource4',
        resourceId: '6026576a4ed204e1a35f10bc',
        createdDate: 1613125482976,
        lastModifiedDate: 1613125482976,
        isDeleted: 'False'
      }]
    }, {
      id: '6026576bb975135001bc3488',
      roleName: 'ROLE_PROJECT_ADMIN',
      roleDescription: 'manage user-roles at project level',
      createdDate: 1613125483944,
      lastModifiedDate: 1613125483944,
      isDeleted: 'False',
      permissions: [{
        id: '6026576bae81aeece081fa7a',
        permissionName: 'View',
        operationName: 'Read',
        resourceName: 'resource4',
        resourceId: '6026576a4ed204e1a35f10bc',
        createdDate: 1613125482976,
        lastModifiedDate: 1613125482976,
        isDeleted: 'False'
      }]
    }]
  };

  // const fakeAcceptRequestData = {
  //   'id': '5de631c02ab79c000990489e',
  //   'username': 'testUser',
  //   'status': 'Approved',
  //   'reviewComments': '',
  //   'roles': [{
  //     'id': '5de628f779f4eaeb6ef34e2e',
  //     'roleName': 'ROLE_SUPERADMIN',
  //     'roleDescription': 'access to every resource in the instance',
  //     'createdDate': 1575364855699,
  //     'lastModifiedDate': 1575364855699,
  //     'isDeleted': 'False',
  //     'permissions': [{
  //       'id': '5de628f766697355e56b5153',
  //       'permissionName': 'ViewAll',
  //       'operationName': 'Read',
  //       'resourceName': 'resource5',
  //       'resourceId': '5de628f662f3fd18566f054e',
  //       'createdDate': 1575364855372,
  //       'lastModifiedDate': 1575364855372,
  //       'isDeleted': 'False'
  //     }]
  //   }],
  //   'createdDate': 1575366077402,
  //   'lastModifiedDate': 1575366077403
  // };

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
          itemName: 'Jiraproj1'
        },
        {
          itemId: '61c99113aeb8700e3a3ac1ed',
          itemName: 'healproj'
        }
      ]
    },
    deleted: false
  };

  // const fakeRequestResponse = {
  //   'message': 'modified access_request@5de631c02ab79c000990489e',
  //   'success': true,
  //   'data': [{
  //     'id': '5de631c02ab79c000990489e',
  //     'username': 'testUser',
  //     'status': 'Approved',
  //     'reviewComments': '',
  //     'roles': [{
  //       'id': '5de628f779f4eaeb6ef34e2e',
  //       'roleName': 'ROLE_SUPERADMIN',
  //       'roleDescription': 'access to every resource in the instance',
  //       'createdDate': 1575364855699,
  //       'lastModifiedDate': 1575364855699,
  //       'isDeleted': 'False',
  //       'permissions': [{
  //         'id': '5de628f766697355e56b5153',
  //         'permissionName': 'ViewAll',
  //         'operationName': 'Read',
  //         'resourceName': 'resource5',
  //         'resourceId': '5de628f662f3fd18566f054e',
  //         'createdDate': 1575364855372,
  //         'lastModifiedDate': 1575364855372,
  //         'isDeleted': 'False'
  //       }]
  //     }],
  //     'createdDate': 1575366077402,
  //     'lastModifiedDate': 1575366386367
  //   }]
  // };

  const fakeRequestResponse = {
    message: 'Granted',
    success: true
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
        DropdownModule
      ],
      providers: [HttpService, MessageService, SharedService
        , { provide: APP_CONFIG, useValue: AppConfig }]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewRequestsComponent);
    component = fixture.componentInstance;
    httpService = TestBed.get(HttpService);
    httpMock = TestBed.get(HttpTestingController);
    messageService = TestBed.get(MessageService);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load pending requests on load', (done) => {
    component.ngOnInit();
    fixture.detectChanges();
    httpMock.match(baseUrl + '/api/accessrequests/status/' + 'Pending')[0].flush(fakeRequestsData);
    if (component.accessRequestData['success']) {
      expect(Object.keys(component.accessRequestList).length).toEqual(Object.keys(fakeRequestsData.data).length);
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
      expect(Object.keys(component.roleList).length).toEqual(Object.keys(fakeRolesData.data).length);
    } else {
      // component.messageService.add({ severity: 'error', summary: 'Error in fetching roles. Please try after some time.' });
    }
    done();
  });

  it('should accept access request', (done) => {
    const requestId = '61cc34b463780d7cd623cc04';
    component.approveRejectRequest(fakeAcceptRequestData, true);
    fixture.detectChanges();
    httpMock.match(baseUrl + '/api/accessrequests/' + requestId)[0].flush(fakeAcceptRequestData);
    // httpMock.match(baseUrl + '/api/accessrequests/5de631c02ab79c000990489e')[0].flush(fakeRequestResponse);
    if (component.acceptRequestData['success']) {
      fixture.detectChanges();
      // expect(fakeRequestResponse.success).toContain(true);
      expect(Object.keys(component.acceptRequestData['message'])).toEqual(Object.keys(fakeRequestResponse.message));
    } else {
      // this.messageService.add({ severity: 'error', summary: 'Error in updating request. Please try after some time.' });
    }
    done();
  });
});
