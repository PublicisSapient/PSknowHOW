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

import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { ToastModule } from 'primeng/toast';
import { TableModule } from 'primeng/table';
import { DropdownModule } from 'primeng/dropdown';
import { DialogModule } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HttpService } from '../../../services/http.service';
import { SharedService } from '../../../services/shared.service';
import { APP_CONFIG, AppConfig } from '../../../services/app.config';
import { ConfirmationService, MessageService } from 'primeng/api';
import { environment } from 'src/environments/environment';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { AccessMgmtComponent } from './access-mgmt.component';
import { of } from 'rxjs';
describe('AccessMgmtComponent', () => {
  let component: AccessMgmtComponent;
  let fixture: ComponentFixture<AccessMgmtComponent>;
  let httpService: HttpService;
  let httpMock;
  let messageService;
  let confirmationService;
  const baseUrl = environment.baseUrl;  // Servers Env
  let sharedService : SharedService;

  const fakeUserData = require('../../../../test/resource/fakeUserData.json');
  const fakeRolesData = require('../../../../test/resource/fakeRolesData.json');
  const fakeServiceInputInvalid = require('../../../../test/resource/fakeUserDataChange.json');
  const fakeServiceInputValid = require('../../../../test/resource/fakeUserDataChangeValid.json');
  const fakeFilterByProjectData = require('../../../../test/resource/fakeFilterByProjectData.json');
  const fakeFilterByRoleData = require('../../../../test/resource/fakeFilterByRoleData.json');
  const accessNodes = require('../../../../test/resource/fakeAccessNodes.json')
  const fakeDeleteAccess = require('../../../../test/resource/fakeDeleteAccessRequest.json');
  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [AccessMgmtComponent],
      imports: [
        ToastModule,
        TableModule,
        DropdownModule,
        DialogModule,
        ButtonModule,
        InputTextModule,
        FormsModule,
        RouterTestingModule.withRoutes([]),
        HttpClientTestingModule,
        BrowserAnimationsModule
      ],
      schemas: [
        CUSTOM_ELEMENTS_SCHEMA
      ],
      providers: [HttpService, MessageService, SharedService, ConfirmationService
        , { provide: APP_CONFIG, useValue: AppConfig }]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AccessMgmtComponent);
    component = fixture.componentInstance;
    httpService = TestBed.inject(HttpService);
    httpMock = TestBed.inject(HttpTestingController);
    messageService = TestBed.inject(MessageService);
    confirmationService = TestBed.inject(ConfirmationService);
    sharedService = TestBed.inject(SharedService);
    // fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should get user data on load', (done) => {
    component.ngOnInit();
    // fixture.detectChanges();
    httpMock.match(baseUrl + '/api/userinfo')[0].flush(fakeUserData);
    expect(Object.keys(component.users).length).toBe(Object.keys(fakeUserData.data).length);
    done();
  });

  /*it('should get project data on load', (done) => {
    component.ngOnInit();
    // fixture.detectChanges();
    httpMock.match(baseUrl + '/api/basicconfigs/all')[0].flush(fakeProjectData);
    expect(Object.keys(component.projects).length).toBe(Object.keys(fakeProjectData.data).length);
    done();
  });*/

  it('should get roles data on load', (done) => {
    component.ngOnInit();
    // fixture.detectChanges();
    httpMock.match(baseUrl + '/api/roles')[0].flush(fakeRolesData);
    if (component.rolesData.success) {
      fakeRolesData.data = fakeRolesData.data.map((role) => ({
        label: role.roleName,
        value: role.roleName
      }));
      expect(component.roleList).toEqual(fakeRolesData.data);
    } else {
      // component.messageService.add({ severity: 'error', summary: 'Error in fetching roles. Please try after some time.' });
    }
    done();
  });

  it('should display duplicate popup on invalid configuration', (done) => {
    component.saveAccessChange(fakeServiceInputInvalid);
    // fixture.detectChanges();
    expect(component.displayDuplicateProject).toBeTruthy();
    done();
  });

  it('should update user access', (done) => {
    component.saveAccessChange(fakeServiceInputValid);
    // fixture.detectChanges();
    expect(component.displayDuplicateProject).toBeFalsy();
    httpMock.expectOne(baseUrl + '/api/userinfo/' + fakeServiceInputValid.username);
    done();
  });

  it('should filter by project', () => {
    component.searchProject = 'KnowHOW';
    component.filterByProject();
    component.users = fakeFilterByProjectData;
    expect(component.users[0].projectsAccess[0].accessNodes[0].accessItems[0].itemName).toContain('KnowHOW');
  });

  it('should filter by role', () => {
    component.searchRole = 'ROLE_SUPERADMIN';
    component.filterByRole();
    component.users = fakeFilterByRoleData;
    expect(component.users[0].projectsAccess[0].role).toContain('ROLE_SUPERADMIN');
  });

  it('should show dialog to add', () => {
    const projectsArr = accessNodes;
    const projectAccess = [
      {
        "role": "ROLE_PROJECT_VIEWER",
        "accessNodes": accessNodes
      }
    ]
    const index = 0;
    component.projectFilter = {
      resetDropdowns: true,
      clearFilters: () => { }
    }
    component.showDialogToAdd(projectsArr, projectAccess, index);
    expect(component.selectedProjects.length).toEqual(projectsArr.length);
  })

  it('should save dialog', () => {
    component.addedProjectsOrNodes = accessNodes;
    component.selectedProjectAccess = [
      {
        "role": "ROLE_PROJECT_VIEWER",
        "accessNodes": accessNodes
      }
    ]
    component.saveDialog();
    expect(component.selectedProjectAccess[0].accessNodes.length).toEqual(component.selectedProjects.length);
  })

  /**confirm  */
  it('should display message on role change', () => {
    const event = {
      "value": "ROLE_SUPERADMIN"
    };
    const index = 0;
    const access = [
      {
        "role": "ROLE_SUPERADMIN",
        "accessNodes": accessNodes
      }
    ];
    component.submitValidationMessage = `A row for ROLE_SUPERADMIN already exists, please add accesses there`;
    component.onRoleChange(event, index, access);
    expect(component.submitValidationMessage).toContain(event.value);
  })

  it('should check access deletion status', () => {
    const isSuperAdmin = false;
    component.accessDeletionStatus(fakeDeleteAccess, isSuperAdmin);
    spyOn(component, 'getUsers');
    expect(component.accessConfirm).toBe(false);
  });

  it('should show project on tooltip on mouseenter', () => {
    const event = {
      'pageX': '805',
      'pageY': '393'
    }
    const item = {
      "itemId": "6375c3d6b8336258af26e2d9",
      "itemName": "FASTREPLAT"
    };
    const node = {
      "accessLevel": "project",
      "accessItems": accessNodes
    };
    component.allProjectsData = [
      {
          "id": "6375c3d6b8336258af26e2d9",
          "projectName": "FASTREPLAT",
          "createdAt": "2022-11-17T06:37:53",
          "kanban": false,
          "hierarchy": [
              {
                  "hierarchyLevel": {
                      "level": 1,
                      "hierarchyLevelId": "bu",
                      "hierarchyLevelName": "BU"
                  },
                  "value": "International"
              },
          ],
          "isKanban": false
      }
    ]
    component.mouseEnter(event, item, node);
    expect(component.top).toEqual(event['pageY']+'px');
    expect(component.left).toEqual(event['pageX']+'px');
    expect(component.showToolTip).toBe(true);
    expect(component.toolTipHtml).toContain('Project');
  })

  it('should not show project on tooltip on mouseenter', () => {
    const event = {
      'pageX': '805',
      'pageY': '393'
    }
    const item = {
      "itemId": "6375c3d6b8336258af26e2d9",
      "itemName": "Test"
    };
    const node = {
      "accessLevel": "hierarchy1",
      "accessItems": accessNodes
    };
    component.allProjectsData = [
      {
          "id": "6375c3d6b8336258af26e2d9",
          "projectName": "FASTREPLAT",
          "createdAt": "2022-11-17T06:37:53",
          "kanban": false,
          "hierarchy": [
              {
                  "hierarchyLevel": {
                      "level": 1,
                      "hierarchyLevelId": "hierarchy1",
                      "hierarchyLevelName": "Hierarchy One"
                  },
                  "value": "Test"
              },
          ],
          "isKanban": false
      }
    ]
    component.mouseEnter(event, item, node);
    expect(component.toolTipHtml).not.toContain('Project');
  })

  it("should receive project data",()=>{
    const fakeProject = [
          {
              "id": "6375c3d6b8336258af26e2d9",
              "projectName": "FASTREPLAT",
              "createdAt": "2022-11-17T06:37:53",
              "kanban": false,
              "hierarchy": [
                  {
                      "hierarchyLevel": {
                          "level": 1,
                          "hierarchyLevelId": "hierarchy1",
                          "hierarchyLevelName": "Hierarchy One"
                      },
                      "value": "Test"
                  },
              ],
              "isKanban": false
          }
        ]

        component.receiveProjectsData(fakeProject);
        expect(component.allProjectsData).toEqual(fakeProject);

  })

  it("should false tooltip on mouse leave",()=>{
        component.mouseLeave()
        expect(component.showToolTip).toBeFalsy();
  })

  it("should filter clear on add project",()=>{
    const accessItem = {
      hierarchyArr : ["hierarchyLevelOne","hierarchyLevelTwo","hierarchyLevelThree"],
      valueRemoved : {
        val : {}
      }
    }
    component.projectSelectedEvent(accessItem);
    expect(component.addedProjectsOrNodes.length).toBe(0);
  })

  it("should select some filter for project",()=>{
    const accessItem = {
      hierarchyArr : ["hierarchyLevelOne","hierarchyLevelTwo","hierarchyLevelThree"],
      valueRemoved : {
        val : {}
      },
      accessType : "project",
      value : ["v1","v2"]
    }
    component.projectSelectedEvent(accessItem);
    expect(component.addedProjectsOrNodes.length).not.toBeNull();
  })

  it("should accessconfirm enabled and superadmin should enabled for superadmin user",()=>{
    const userName = "userName";
    const userRole = ['ROLE_VIEWER', 'ROLE_SUPERADMIN'];
    component.deleteUser(userName,userRole);
    expect(component.accessConfirm).toBeTruthy();
  })

  it("should delete project",()=>{
    const userName = "userName";
    const isSuperAdmin = true
    spyOn(component,'accessDeletionStatus');
    spyOn(httpService,'deleteAccess').and.returnValue(of({data:{
      message : "Deleted"
    }}))
    component.deleteAccessReq(userName,isSuperAdmin);
    expect(component.accessDeletionStatus).toHaveBeenCalled();
  })

  it("should superadmin delete project",()=>{
    spyOn<any>(confirmationService, 'confirm');
    const data = {
      message : "Deleted",
      success : false
    }
    const isSuperAdmin = true
    component.accessDeletionStatus(data,isSuperAdmin);
    expect(confirmationService.confirm).toHaveBeenCalled();
  })

  it("should fail delete response",()=>{
    spyOn<any>(confirmationService, 'confirm');
    const data = {
      message : "Something went wrong",
      success : false
    }
    const isSuperAdmin = false
    component.accessDeletionStatus(data,isSuperAdmin);
    expect(confirmationService.confirm).toHaveBeenCalled();
  })

});
