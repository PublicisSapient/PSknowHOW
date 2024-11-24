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
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { HttpService } from '../../../services/http.service';
import { SharedService } from '../../../services/shared.service';
import { APP_CONFIG, AppConfig } from '../../../services/app.config';
import { ConfirmationService, MessageService } from 'primeng/api';
import { environment } from 'src/environments/environment';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { AccessMgmtComponent } from './access-mgmt.component';
import { of, throwError } from 'rxjs';
describe('AccessMgmtComponent', () => {
  let component: AccessMgmtComponent;
  let fixture: ComponentFixture<AccessMgmtComponent>;
  let httpService: HttpService;
  let httpMock;
  let messageService;
  let confirmationService;
  const baseUrl = environment.baseUrl; // Servers Env
  let sharedService: SharedService;

  const fakeUserData = require('../../../../test/resource/fakeUserData.json');
  const fakeRolesData = require('../../../../test/resource/fakeRolesData.json');
  const fakeServiceInputInvalid = require('../../../../test/resource/fakeUserDataChange.json');
  const fakeServiceInputValid = require('../../../../test/resource/fakeUserDataChangeValid.json');
  const fakeFilterByProjectData = require('../../../../test/resource/fakeFilterByProjectData.json');
  const fakeFilterByRoleData = require('../../../../test/resource/fakeFilterByRoleData.json');
  const accessNodes = require('../../../../test/resource/fakeAccessNodes.json');
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
        BrowserAnimationsModule,
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
      providers: [
        HttpService,
        MessageService,
        SharedService,
        ConfirmationService,
        { provide: APP_CONFIG, useValue: AppConfig },
      ],
    }).compileComponents();
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
    expect(Object.keys(component.users).length).toBe(
      Object.keys(fakeUserData.data).length,
    );
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
        label: role.displayName,
        value: role.roleName,
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

  it('should filter by project', () => {
    component.searchProject = 'KnowHOW';
    component.filterByProject();
    component.users = fakeFilterByProjectData;
    expect(
      component.users[0].projectsAccess[0].accessNodes[0].accessItems[0]
        .itemName,
    ).toContain('KnowHOW');
  });

  it('should filter by role', () => {
    component.searchRole = 'ROLE_SUPERADMIN';
    component.filterByRole();
    component.users = fakeFilterByRoleData;
    expect(component.users[0].projectsAccess[0].role).toContain(
      'ROLE_SUPERADMIN',
    );
  });

  it('should show dialog to add', () => {
    const projectsArr = accessNodes;
    const projectAccess = [
      {
        role: 'ROLE_PROJECT_VIEWER',
        accessNodes: accessNodes,
      },
    ];
    const index = 0;
    component.projectFilter = {
      resetDropdowns: true,
      clearFilters: () => {},
    };
    component.showDialogToAdd(projectsArr, projectAccess, index);
    expect(component.selectedProjects.length).toEqual(projectsArr.length);
  });

  it('should save dialog', () => {
    component.addedProjectsOrNodes = accessNodes;
    component.selectedProjectAccess = [
      {
        role: 'ROLE_PROJECT_VIEWER',
        accessNodes: accessNodes,
      },
    ];
    component.saveDialog();
    expect(component.selectedProjectAccess[0].accessNodes.length).toEqual(
      component.selectedProjects.length,
    );
  });

  /**confirm  */
  it('should display message on role change', () => {
    const event = {
      value: 'ROLE_SUPERADMIN',
    };
    const index = 0;
    const access = [
      {
        role: 'ROLE_SUPERADMIN',
        accessNodes: accessNodes,
      },
    ];
    component.submitValidationMessage = `A row for ROLE_SUPERADMIN already exists, please add accesses there`;
    component.onRoleChange(event, index, access);
    expect(component.submitValidationMessage).toContain(event.value);
  });

  it('should check access deletion status', () => {
    const isSuperAdmin = false;
    component.accessDeletionStatus(fakeDeleteAccess, isSuperAdmin);
    spyOn(component, 'getUsers');
    expect(component.accessConfirm).toBe(false);
  });

  it('should show project on tooltip on mouseenter', () => {
    const event = {
      pageX: '805',
      pageY: '393',
    };
    const item = {
      itemId: '6375c3d6b8336258af26e2d9',
      itemName: 'FASTREPLAT',
    };
    const node = {
      accessLevel: 'project',
      accessItems: accessNodes,
    };
    component.allProjectsData = [
      {
        id: '6375c3d6b8336258af26e2d9',
        projectName: 'FASTREPLAT',
        createdAt: '2022-11-17T06:37:53',
        kanban: false,
        hierarchy: [
          {
            hierarchyLevel: {
              level: 1,
              hierarchyLevelId: 'bu',
              hierarchyLevelName: 'BU',
            },
            value: 'International',
          },
        ],
        isKanban: false,
      },
    ];
    component.mouseEnter(event, item, node);
    expect(component.top).toEqual(event['pageY'] + 'px');
    expect(component.left).toEqual(event['pageX'] + 'px');
    expect(component.showToolTip).toBe(true);
    expect(component.toolTipHtml).toContain('Project');
  });

  it('should not show project on tooltip on mouseenter', () => {
    const event = {
      pageX: '805',
      pageY: '393',
    };
    const item = {
      itemId: '6375c3d6b8336258af26e2d9',
      itemName: 'Test',
    };
    const node = {
      accessLevel: 'hierarchy1',
      accessItems: accessNodes,
    };
    component.allProjectsData = [
      {
        id: '6375c3d6b8336258af26e2d9',
        projectName: 'FASTREPLAT',
        createdAt: '2022-11-17T06:37:53',
        kanban: false,
        hierarchy: [
          {
            hierarchyLevel: {
              level: 1,
              hierarchyLevelId: 'hierarchy1',
              hierarchyLevelName: 'Hierarchy One',
            },
            value: 'Test',
          },
        ],
        isKanban: false,
      },
    ];
    component.mouseEnter(event, item, node);
    expect(component.toolTipHtml).not.toContain('Project');
  });

  it('should receive project data', () => {
    const fakeProject = [
      {
        id: '6375c3d6b8336258af26e2d9',
        projectName: 'FASTREPLAT',
        createdAt: '2022-11-17T06:37:53',
        kanban: false,
        hierarchy: [
          {
            hierarchyLevel: {
              level: 1,
              hierarchyLevelId: 'hierarchy1',
              hierarchyLevelName: 'Hierarchy One',
            },
            value: 'Test',
          },
        ],
        isKanban: false,
      },
    ];

    component.receiveProjectsData(fakeProject);
    expect(component.allProjectsData).toEqual(fakeProject);
  });

  it('should false tooltip on mouse leave', () => {
    component.mouseLeave();
    expect(component.showToolTip).toBeFalsy();
  });

  it('should filter clear on add project', () => {
    const accessItem = {
      hierarchyArr: [
        'hierarchyLevelOne',
        'hierarchyLevelTwo',
        'hierarchyLevelThree',
      ],
      valueRemoved: {
        val: {},
      },
    };
    component.projectSelectedEvent(accessItem);
    expect(component.addedProjectsOrNodes.length).toBe(0);
  });

  it('should select some filter for project', () => {
    const accessItem = {
      hierarchyArr: [
        'hierarchyLevelOne',
        'hierarchyLevelTwo',
        'hierarchyLevelThree',
      ],
      valueRemoved: {
        val: {},
      },
      accessType: 'project',
      value: ['v1', 'v2'],
    };
    component.projectSelectedEvent(accessItem);
    expect(component.addedProjectsOrNodes.length).not.toBeNull();
  });

  it('should accessconfirm enabled and superadmin should enabled for superadmin user', () => {
    const userName = 'userName';
    const userRole = ['ROLE_VIEWER', 'ROLE_SUPERADMIN'];
    component.deleteUser(userName, userRole);
    expect(component.accessConfirm).toBeTruthy();
  });

  it('should delete project', () => {
    const userName = 'userName';
    const isSuperAdmin = true;
    spyOn(component, 'accessDeletionStatus');
    spyOn(httpService, 'deleteAccess').and.returnValue(
      of({
        data: {
          message: 'Deleted',
        },
      }),
    );
    component.deleteAccessReq(userName, isSuperAdmin);
    expect(component.accessDeletionStatus).toHaveBeenCalled();
  });

  it('should superadmin delete project', () => {
    spyOn<any>(confirmationService, 'confirm');
    const data = {
      message: 'Deleted',
      success: false,
    };
    const isSuperAdmin = true;
    component.accessDeletionStatus(data, isSuperAdmin);
    expect(confirmationService.confirm).toHaveBeenCalled();
  });

  it('should fail delete response', () => {
    spyOn<any>(confirmationService, 'confirm');
    const data = {
      message: 'Something went wrong',
      success: false,
    };
    const isSuperAdmin = false;
    component.accessDeletionStatus(data, isSuperAdmin);
    expect(confirmationService.confirm).toHaveBeenCalled();
  });

  it('should remove project', () => {
    const itemName = 'Project1';
    const arr = [];
    const spy = spyOn(component, 'removeByAttr');
    component.removeProject(itemName, arr);
    expect(spy).toHaveBeenCalled();
  });

  it('should remove by attribute', () => {
    const itemName = 'abc';
    const arr = [
      {
        itemId: '655f073bd08ea076bfb2c9cf',
        itemName: 'abc',
      },
      {
        itemId: '6449103b3be37902a3f1ba70',
        itemName: 'pqr',
      },
      {
        itemId: '64ab97327d51263c17602b58',
        itemName: 'xyz',
      },
    ];
    component.removeByAttr(arr, 'itemName', itemName);
    expect(arr.length).toBe(2);
  });

  it('should cancel dialog', () => {
    spyOn(component, 'hide');
    component.cancelDialog();
    expect(component.displayDialog).toBe(false);
  });

  it('should hide dialog', () => {
    component.projectFilter = {
      resetDropdowns: false,
    };
    component.hide();
    expect(component.projectFilter.resetDropdowns).toBe(true);
  });

  it('should remove row', () => {
    const projectsAccess = [
      {
        role: 'ROLE_PROJECT_VIEWER',
        accessNodes: [
          {
            accessLevel: 'project',
            accessItems: [
              {
                itemId: '655f073bd08ea076bfb2c9cf',
                itemName: 'K Project',
              },
              {
                itemId: '6449103b3be37902a3f1ba70',
                itemName: 'GearBox Squad 1',
              },
              {
                itemId: '64ab97327d51263c17602b58',
                itemName: "Unified Commerce - Dan's MVP",
              },
            ],
          },
        ],
      },
    ];
    const index = 0;
    component.removeRow(projectsAccess, index);
    expect(projectsAccess.length).toBe(0);
  });

  it('should add row', () => {
    const projectsAccess = [
      {
        role: 'ROLE_PROJECT_VIEWER',
        accessNodes: [
          {
            accessLevel: 'project',
            accessItems: [
              {
                itemId: '655f073bd08ea076bfb2c9cf',
                itemName: 'K Project',
              },
              {
                itemId: '6449103b3be37902a3f1ba70',
                itemName: 'GearBox Squad 1',
              },
              {
                itemId: '64ab97327d51263c17602b58',
                itemName: "Unified Commerce - Dan's MVP",
              },
            ],
          },
        ],
      },
    ];
    component.addRow(projectsAccess);
    expect(projectsAccess.length).toBe(2);
  });

  it('should update access  and showAddUserForm is true', () => {
    const userData = {
      id: '601d3d2630c49e000148b749',
      username: 'Aadil',
      authorities: ['ROLE_PROJECT_VIEWER'],
      authType: 'STANDARD',
      emailAddress: 'aadil.mohan@publicssapient.com',
      projectsAccess: [
        {
          role: 'ROLE_PROJECT_VIEWER',
          accessNodes: [
            {
              accessLevel: 'project',
              accessItems: [
                {
                  itemId: '6449103b3be37902a3f1ba70',
                  itemName: 'GearBox Squad 1',
                },
                {
                  itemId: '64ab97327d51263c17602b58',
                  itemName: "Unified Commerce - Dan's MVP",
                },
                {
                  itemId: '655ef009d08ea076bfb2c9ae',
                  itemName: 'REDCLIFF',
                },
              ],
            },
          ],
        },
      ],
    };
    const response = {
      success: true,
    };
    component.displayDuplicateProject = false;
    component.showAddUserForm = true;
    spyOn(httpService, 'updateAccess').and.returnValue(of(response));
    const spy = spyOn(messageService, 'add');
    spyOn(component, 'resetAddDataForm');
    component.saveAccessChange(userData);
    expect(component.showAddUserForm).toBe(false);
    expect(spy).toHaveBeenCalledWith({
      severity: 'success',
      summary: 'User added.',
      detail: '',
    });
  });

  it('should update access when response is success and showAddUserForm is false', () => {
    const userData = {
      id: '601d3d2630c49e000148b749',
      username: 'Aadil',
      authorities: ['ROLE_PROJECT_VIEWER'],
      authType: 'STANDARD',
      emailAddress: 'aadil.mohan@publicssapient.com',
      projectsAccess: [
        {
          role: 'ROLE_PROJECT_VIEWER',
          accessNodes: [
            {
              accessLevel: 'project',
              accessItems: [
                {
                  itemId: '6449103b3be37902a3f1ba70',
                  itemName: 'GearBox Squad 1',
                },
                {
                  itemId: '64ab97327d51263c17602b58',
                  itemName: "Unified Commerce - Dan's MVP",
                },
                {
                  itemId: '655ef009d08ea076bfb2c9ae',
                  itemName: 'REDCLIFF',
                },
              ],
            },
          ],
        },
      ],
    };
    const response = {
      success: true,
    };
    component.displayDuplicateProject = false;
    component.showAddUserForm = false;
    spyOn(httpService, 'updateAccess').and.returnValue(of(response));
    const spy = spyOn(messageService, 'add');
    // spyOn(component, 'resetAddDataForm');
    component.saveAccessChange(userData);
    expect(component.showAddUserForm).toBe(false);
    expect(spy).toHaveBeenCalledWith({
      severity: 'success',
      summary: 'Access updated.',
      detail: '',
    });
  });

  it('should update access when response has failed', () => {
    const userData = {
      id: '601d3d2630c49e000148b749',
      username: 'Aadil',
      authorities: ['ROLE_PROJECT_VIEWER'],
      authType: 'STANDARD',
      emailAddress: 'aadil.mohan@publicssapient.com',
      projectsAccess: [
        {
          role: 'ROLE_PROJECT_VIEWER',
          accessNodes: [
            {
              accessLevel: 'project',
              accessItems: [
                {
                  itemId: '6449103b3be37902a3f1ba70',
                  itemName: 'GearBox Squad 1',
                },
                {
                  itemId: '64ab97327d51263c17602b58',
                  itemName: "Unified Commerce - Dan's MVP",
                },
                {
                  itemId: '655ef009d08ea076bfb2c9ae',
                  itemName: 'REDCLIFF',
                },
              ],
            },
          ],
        },
      ],
    };
    const response = {
      success: false,
    };
    component.displayDuplicateProject = false;
    spyOn(httpService, 'updateAccess').and.returnValue(of(response));
    const spy = spyOn(messageService, 'add');
    component.saveAccessChange(userData);
    expect(component.showAddUserForm).toBe(false);
    expect(spy).toHaveBeenCalledWith({
      severity: 'error',
      summary: 'Error in updating project access. Please try after some time.',
    });
  });

  it('should give error on getUsers api call', () => {
    const errResponse = {
      error: 'Something went wrong',
    };
    spyOn(httpService, 'getAllUsers').and.returnValue(of(errResponse));
    const spy = spyOn(messageService, 'add');
    component.getUsers();
    expect(spy).toHaveBeenCalled();
  });

  it('should give error on getting role list', () => {
    const errResponse = {
      message: 'Error',
      success: false,
    };
    spyOn(httpService, 'getRolesList').and.returnValue(of(errResponse));
    const spy = spyOn(messageService, 'add');
    component.getRolesList();
    expect(spy).toHaveBeenCalled();
  });

  it('should filter by project when length is 3 or more', () => {
    component.searchProject = 'abc';
    component.users = [
      {
        id: '63ee5d987417635fc8d7d72d',
        username: 'ASOtest',
        authorities: ['ROLE_PROJECT_ADMIN'],
        authType: 'STANDARD',
        emailAddress: 'asotest123@gmail.com',
        projectsAccess: [
          {
            role: 'ROLE_PROJECT_ADMIN',
            accessNodes: [
              {
                accessLevel: 'project',
                accessItems: [
                  {
                    itemId: '655f073bd08ea076bfb2c9cf',
                    itemName: 'abc',
                  },
                  {
                    itemId: '6449103b3be37902a3f1ba70',
                    itemName: 'pqr',
                  },
                ],
              },
            ],
          },
        ],
      },
    ];
    component.filterByProject();
    expect(component.users.length).toBe(1);
  });

  it('should filter by project when length is less than 3', () => {
    component.searchProject = '';
    component.users = [];
    component.allUsers = [
      {
        id: '63ee5d987417635fc8d7d72d',
        username: 'ASOtest',
        authorities: ['ROLE_PROJECT_ADMIN'],
        authType: 'STANDARD',
        emailAddress: 'asotest123@gmail.com',
        projectsAccess: [
          {
            role: 'ROLE_PROJECT_ADMIN',
            accessNodes: [
              {
                accessLevel: 'project',
                accessItems: [
                  {
                    itemId: '655f073bd08ea076bfb2c9cf',
                    itemName: 'abc',
                  },
                  {
                    itemId: '6449103b3be37902a3f1ba70',
                    itemName: 'pqr',
                  },
                ],
              },
            ],
          },
        ],
      },
    ];
    component.filterByProject();
    expect(component.users.length).toBe(component.allUsers.length);
  });

  it('should check if disabled', () => {
    component.addData = {
      authType: 'SSO',
      username: '',
      emailAddress: '',
      projectsAccess: [],
    };
    const spy = component.checkIfDisabled();
    expect(spy).toBeTruthy();
  });

  it('should check if not disabled', () => {
    component.addData = {
      authType: 'SSO',
      username: 'abc',
      emailAddress: 'abc@gmail.com',
      projectsAccess: [
        {
          role: 'ROLE_PROJECT_ADMIN',
          accessNodes: [
            {
              accessLevel: 'project',
              accessItems: [
                {
                  itemId: '655f073bd08ea076bfb2c9cf',
                  itemName: 'abcd',
                },
              ],
            },
          ],
        },
      ],
    };
    const spy = component.checkIfDisabled();
    expect(spy).toBeFalsy();
  });

  it('should save access change when role is not superadmin', () => {
    const userData = {
      id: '63ee5d987417635fc8d7d72d',
      username: 'ASOtest',
      authorities: ['ROLE_PROJECT_ADMIN'],
      authType: 'STANDARD',
      emailAddress: 'asotest123@gmail.com',
      projectsAccess: [
        {
          role: 'ROLE_PROJECT_ADMIN',
          accessNodes: [
            // {
            //     "accessLevel": "project",
            //     "accessItems": [
            //         {
            //             "itemId": "655f073bd08ea076bfb2c9cf",
            //             "itemName": "K Project"
            //         },
            //         {
            //             "itemId": "6449103b3be37902a3f1ba70",
            //             "itemName": "GearBox Squad 1"
            //         },
            //         {
            //             "itemId": "64ab97327d51263c17602b58",
            //             "itemName": "Unified Commerce - Dan's MVP"
            //         },
            //         {
            //             "itemId": "655ef009d08ea076bfb2c9ae",
            //             "itemName": "REDCLIFF"
            //         }
            //     ]
            // },
            // {
            //     "accessLevel": "project",
            //     "accessItems": [
            //         {
            //             "itemId": "647702b25286e83998a56138",
            //             "itemName": "VDOS Outside Hauler"
            //         }
            //     ]
            // }
          ],
        },
      ],
    };
    const msg =
      'You are submitting a role with empty project list. Please add projects.';
    component.submitValidationMessage = '';
    component.displayDuplicateProject = false;
    component.saveAccessChange(userData);
    expect(component.displayDuplicateProject).toBe(true);
    expect(component.submitValidationMessage).toBe(msg);
  });

  it('should handle empty project list for non-SUPERADMIN role', () => {
    const userData = {
      projectsAccess: [
        {
          role: 'ROLE_ADMIN',
          accessNodes: [],
        },
      ],
    };

    component.saveAccessChange(userData);

    expect(component.submitValidationMessage).toBe(
      'You are submitting a role with empty project list. Please add projects.',
    );
    expect(component.displayDuplicateProject).toBe(true);
  });

  it('should display message on role change', () => {
    const event = {
      value: 'ROLE_ADMIN',
    };
    const index = 0;
    const access = [
      {
        role: 'ROLE_SUPERADMIN',
        accessNodes: [],
      },
      {
        role: 'ROLE_ADMIN',
        accessNodes: [],
      },
    ];
    component.submitValidationMessage = '';
    component.displayDuplicateProject = false;
    component.onRoleChange(event, index, access);

    expect(component.submitValidationMessage).toBe(
      'A row for ROLE_ADMIN already exists, please add accesses there.',
    );
    expect(component.displayDuplicateProject).toBe(true);
  });

  it('should handle delete access request', () => {
    const username = 'testUser';
    const isSuperAdmin = true;
    const deleteAccessError = { message: 'Error deleting access' };
    spyOn(httpService, 'deleteAccess').and.returnValue(
      throwError(deleteAccessError),
    );
    const spy = spyOn(component, 'accessDeletionStatus');
    component.deleteAccessReq(username, isSuperAdmin);
    expect(spy).toHaveBeenCalled();
  });

  it('should add or remove projects/nodes when accessType is project', () => {
    const accessItem = {
      value: [
        { itemId: '1', itemName: 'Project 1' },
        { itemId: '2', itemName: 'Project 2' },
      ],
      valueRemoved: {
        val: [{ code: '1', name: 'Project 1' }],
      },
      accessType: 'project',
    };
    component.addedProjectsOrNodes = [
      {
        accessLevel: 'project',
        accessItems: [{ itemId: '3', itemName: 'Project 3' }],
      },
    ];

    component.projectSelectedEvent(accessItem);

    // Check if the projects/nodes are added correctly
    expect(component.addedProjectsOrNodes).toEqual([
      {
        accessLevel: 'project',
        accessItems: [
          { itemId: '1', itemName: 'Project 1' },
          { itemId: '2', itemName: 'Project 2' },
        ],
      },
    ]);
  });

  it('should add or remove projects/nodes when accessItem is not project', () => {
    const accessItem = {
      value: [
        { itemId: '1', itemName: 'item 1' },
        { itemId: '2', itemName: 'item 2' },
      ],
      valueRemoved: {},
      accessType: 'category1',
    };
    component.addedProjectsOrNodes = [
      {
        accessLevel: 'project',
        accessItems: [{ itemId: '3', itemName: 'Project 3' }],
      },
    ];

    component.projectSelectedEvent(accessItem);

    // Check if the projects/nodes are added correctly
    expect(component.addedProjectsOrNodes).toEqual([
      {
        accessLevel: 'category1',
        accessItems: [
          { itemId: '1', itemName: 'item 1' },
          { itemId: '2', itemName: 'item 2' },
        ],
      },
      {
        accessLevel: 'category1',
        accessItems: [
          { itemId: '1', itemName: 'item 1' },
          { itemId: '2', itemName: 'item 2' },
        ],
      },
    ]);
  });

  it('should add or remove projects/nodes when accessItem is not project and valueRemoved is empty', () => {
    const accessItem = {
      value: [
        { itemId: '1', itemName: 'item 1' },
        { itemId: '2', itemName: 'item 2' },
      ],
      valueRemoved: {},
      accessType: 'category1',
    };
    component.addedProjectsOrNodes = [
      {
        accessLevel: 'category1',
        accessItems: [{ itemId: '3', itemName: 'item 3' }],
      },
    ];

    component.projectSelectedEvent(accessItem);

    // Check if the projects/nodes are added correctly
    expect(component.addedProjectsOrNodes).toEqual([
      {
        accessLevel: 'category1',
        accessItems: [
          { itemId: '3', itemName: 'item 3' },
          { itemId: '1', itemName: 'item 1' },
          { itemId: '2', itemName: 'item 2' },
        ],
      },
    ]);
  });

  it('should add or remove projects/nodes when accessItem is not project and valueRemoved is not empty', () => {
    const accessItem = {
      value: [
        { itemId: '1', itemName: 'item 1' },
        { itemId: '2', itemName: 'item 2' },
      ],
      valueRemoved: {
        val: [{ code: '1', name: 'item 1' }],
      },
      accessType: 'category1',
    };
    component.addedProjectsOrNodes = [
      {
        accessLevel: 'category1',
        accessItems: [{ itemId: '3', itemName: 'item 3' }],
      },
    ];

    component.projectSelectedEvent(accessItem);

    // Check if the projects/nodes are added correctly
    expect(component.addedProjectsOrNodes).toEqual([
      {
        accessLevel: 'category1',
        accessItems: [{ itemId: '3', itemName: 'item 3' }],
      },
    ]);
  });
});
