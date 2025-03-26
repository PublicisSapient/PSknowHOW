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
  TestBed,
  fakeAsync,
  tick,
  waitForAsync,
} from '@angular/core/testing';
import { UntypedFormGroup, FormControl } from '@angular/forms';
import { ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { MyprofileComponent } from './myprofile.component';
import { RouterTestingModule } from '@angular/router/testing';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { CommonModule } from '@angular/common';
import { HttpService } from '../../../services/http.service';
import { APP_CONFIG, AppConfig } from '../../../services/app.config';
import { ProfileComponent } from '../profile.component';
import { environment } from 'src/environments/environment';
import { SharedService } from 'src/app/services/shared.service';
import { of } from 'rxjs';
import { GetAuthorizationService } from 'src/app/services/get-authorization.service';
import { InputSwitchModule } from 'primeng/inputswitch';
import { MessageService } from 'primeng/api';
describe('MyprofileComponent', () => {
  let component: MyprofileComponent;
  let fixture: ComponentFixture<MyprofileComponent>;
  let httpService;
  let httpMock;
  let shared;
  let authService;
  let messageService;
  const baseUrl = environment.baseUrl;
  const successResponse = {
    message: 'Email updated successfully',
    success: true,
    data: {
      username: 'testUser',
      authorities: ['ROLE_SUPERADMIN'],
      authType: 'STANDARD',
      emailAddress: 'testuser@gmail.com',
    },
  };
  const hierarchyData = [
    {
      level: 1,
      hierarchyLevelId: 'country',
      hierarchyLevelName: 'Country',
      suggestions: [
        {
          name: 'Canada',
          code: 'Canada',
        },
        {
          name: 'India',
          code: 'India',
        },
        {
          name: 'USA',
          code: 'USA',
        },
      ],
      value: '',
      required: true,
    },
    {
      level: 2,
      hierarchyLevelId: 'state',
      hierarchyLevelName: 'State',
      suggestions: [
        {
          name: 'Haryana',
          code: 'Haryana',
        },
        {
          name: 'Karnataka',
          code: 'Karnataka',
        },
        {
          name: 'Ontario',
          code: 'Ontario',
        },
        {
          name: 'Texas',
          code: 'Texas',
        },
        {
          name: 'Washinton',
          code: 'Washinton',
        },
      ],
      value: '',
      required: true,
    },
    {
      level: 3,
      hierarchyLevelId: 'city',
      hierarchyLevelName: 'City',
      suggestions: [
        {
          name: 'Bangalore',
          code: 'Bangalore',
        },
        {
          name: 'Gurgaon',
          code: 'Gurgaon',
        },
        {
          name: 'Houston',
          code: 'Houston',
        },
        {
          name: 'Kurukshetra',
          code: 'Kurukshetra',
        },
        {
          name: 'Ottawa',
          code: 'Ottawa',
        },
        {
          name: 'Remond',
          code: 'Remond',
        },
        {
          name: 'Seattle',
          code: 'Seattle',
        },
      ],
      value: '',
      required: true,
    },
  ];
  const mockProjectsAccess = [
    {
      role: 'ROLE_PROJECT_VIEWER',
      projects: [
        {
          projectName: 'PSknowHOW',
          projectId: '65118da7965fbb0d14bce23c',
          hierarchy: [
            {
              hierarchyLevel: {
                level: 1,
                hierarchyLevelId: 'bu',
                hierarchyLevelName: 'BU',
              },
              value: 'Internal',
            },
            {
              hierarchyLevel: {
                level: 2,
                hierarchyLevelId: 'ver',
                hierarchyLevelName: 'Vertical',
              },
              value: 'PS Internal',
            },
            {
              hierarchyLevel: {
                level: 3,
                hierarchyLevelId: 'acc',
                hierarchyLevelName: 'Account',
              },
              value: 'Methods and Tools',
            },
            {
              hierarchyLevel: {
                level: 4,
                hierarchyLevelId: 'port',
                hierarchyLevelName: 'Engagement',
              },
              value: 'DTS',
            },
          ],
        },
      ],
    },
    {
      role: 'ROLE_PROJECT_ADMIN',
      projects: [
        {
          projectName: 'ABC',
          projectId: '66d7da7258ffc53913fb840c',
          hierarchy: [
            {
              hierarchyLevel: {
                level: 1,
                hierarchyLevelId: 'bu',
                hierarchyLevelName: 'BU',
              },
              value: 'EU',
            },
            {
              hierarchyLevel: {
                level: 2,
                hierarchyLevelId: 'ver',
                hierarchyLevelName: 'Vertical',
              },
              value: 'Consumer Products',
            },
            {
              hierarchyLevel: {
                level: 3,
                hierarchyLevelId: 'acc',
                hierarchyLevelName: 'Account',
              },
              value: 'ABC A/S',
            },
            {
              hierarchyLevel: {
                level: 4,
                hierarchyLevelId: 'port',
                hierarchyLevelName: 'Engagement',
              },
              value: 'ABC',
            },
          ],
        },
      ],
    },
  ];

  const mockCurrentUserDetails = {
    user_name: 'SUPERADMIN',
    user_email: 'abc@publicissapient.com',
    authType: 'STANDARD',
    authorities: ['ROLE_SUPERADMIN'],
    projectsAccess: mockProjectsAccess,
    notificationEmail: {
      accessAlertNotification: false,
      errorAlertNotification: false,
    },
  };

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        FormsModule,
        ReactiveFormsModule,
        CommonModule,
        HttpClientTestingModule,
        RouterTestingModule,
        InputSwitchModule,
      ],
      declarations: [MyprofileComponent],
      providers: [
        HttpService,
        ProfileComponent,
        SharedService,
        MessageService,
        { provide: APP_CONFIG, useValue: AppConfig },
      ],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MyprofileComponent);
    component = fixture.componentInstance;
    httpService = TestBed.inject(HttpService);
    httpMock = TestBed.inject(HttpTestingController);
    shared = TestBed.inject(SharedService);
    authService = TestBed.inject(GetAuthorizationService);
    messageService = TestBed.inject(MessageService);
    let localStore = {};

    spyOn(window.localStorage, 'getItem').and.callFake((key) =>
      key in localStore ? localStore[key] : null,
    );
    spyOn(window.localStorage, 'setItem').and.callFake(
      (key, value) => (localStore[key] = value + ''),
    );
    component.sharedService.currentUserDetails = mockCurrentUserDetails;
    spyOn(window.localStorage, 'clear').and.callFake(() => (localStore = {}));
    httpService.setCurrentUserDetails({
      username: 'testUser',
      authorities: ['ROLE_SUPERADMIN'],
      authType: 'STANDARD',
      emailAddress: 'testuser@gmail.com',
    });
    localStorage.setItem('hierarchyData', JSON.stringify(hierarchyData));
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should group projects role-wise', () => {
    component.groupProjects(
      JSON.parse(
        '[{"role":"DUMMY","projects":[{"projectName":"Jenkin_kanban","projectId":"6331857a7bb22322e4e01479","hierarchy":[{"hierarchyLevel":{"level":1,"hierarchyLevelId":"corporate","hierarchyLevelName":"Corporate Name"},"value":"Leve1"}]}]},{"role":"DUMMY","projects":[{"projectName":"Tools proj","projectId":"6332f0a468b5d05cf59c42a6","hierarchy":[{"hierarchyLevel":{"level":1,"hierarchyLevelId":"corporate","hierarchyLevelName":"Corporate Name"},"value":"Org1"}]}]}]',
      ),
    );
    expect(Object.keys(component.roleBasedProjectList).length).toEqual(2);
  });

  it('should populate dynamicCols with objects based on the hierarchyData from localStorage', () => {
    localStorage.setItem(
      'hierarchyData',
      '[{"hierarchyLevelId": 1, "hierarchyLevelName": "Level 1"}, {"hierarchyLevelId": 2, "hierarchyLevelName": "Level 2"}]',
    );
    component.getTableHeadings();
    expect(component.dynamicCols.length).toEqual(3);
  });

  it('should update notification email flag successfully', fakeAsync(() => {
    // component.ngOnInit();
    const event = { checked: true };
    const toggleField = 'accessAlertNotification';
    component.notificationEmailForm = new UntypedFormGroup({
      accessAlertNotification: new FormControl(false),
      errorAlertNotification: new FormControl(false),
    });

    const successResponse = {
      success: true,
      message: 'Flag Updated successfully in user info details',
      data: {
        username: 'dummyUser',
        authorities: ['ROLE_PROJECT_ADMIN'],
        authType: 'SAML',
        emailAddress: 'someemail@abc.com',
        notificationEmail: {
          accessAlertNotification: true,
          errorAlertNotification: false,
        },
      },
    };
    shared.currentUserDetailsSubject.next({
      user_name: 'dummyUser',
      user_email: 'someemail@abc.com',
      notificationEmail: {
        accessAlertNotification: true,
        errorAlertNotification: false,
      },
    });
    spyOn(httpService, 'notificationEmailToggleChange').and.returnValue(
      of(successResponse),
    );
    const spyObj = spyOn(httpService, 'setCurrentUserDetails');
    component.toggleNotificationEmail(event, toggleField);
    tick();
    expect(spyObj).toHaveBeenCalled();
  }));

  it('should give error while updating notification email flag', fakeAsync(() => {
    // component.ngOnInit();
    const event = { checked: true };
    const toggleField = 'accessAlertNotification';
    component.notificationEmailForm = new UntypedFormGroup({
      accessAlertNotification: new FormControl(false),
      errorAlertNotification: new FormControl(false),
    });

    const errResponse = {
      success: false,
      message: 'Something went wrong',
    };

    spyOn(httpService, 'notificationEmailToggleChange').and.returnValue(
      of(errResponse),
    );
    const spyObj = spyOn(messageService, 'add');
    component.toggleNotificationEmail(event, toggleField);
    tick();
    expect(spyObj).toHaveBeenCalled();
  }));

  describe('MyprofileComponent.ngOnInit() ngOnInit method', () => {
    beforeEach(() => {});

    describe('Happy Path', () => {
      it('should set isSuperAdmin to true if user is a super admin', () => {
        spyOn(authService, 'checkIfSuperUser').and.returnValue(true as any);
        component.ngOnInit();
        expect(component.isSuperAdmin).toBe(true);
      });

      it('should set isProjectAdmin to true if user is a project admin', () => {
        spyOn(authService, 'checkIfProjectAdmin').and.returnValue(true as any);
        component.ngOnInit();
        expect(component.isProjectAdmin).toBe(true);
      });

      it('should initialize userEmailForm with correct validators', () => {
        component.ngOnInit();
        expect(component.userEmailForm.controls.email.validator).toBeDefined();
        expect(
          component.userEmailForm.controls.confirmEmail.validator,
        ).toBeDefined();
      });

      it('should group projects and call getTableHeadings fn when there are projects in projectsAccess', () => {
        spyOn(component, 'groupProjects');
        spyOn(component, 'getTableHeadings');
        spyOn(shared, 'getCurrentUserDetails').and.returnValue(
          mockProjectsAccess,
        );
        component.ngOnInit();
        expect(component.groupProjects).toHaveBeenCalledWith(
          mockProjectsAccess,
        );
        expect(component.getTableHeadings).toHaveBeenCalled();
      });
    });
  });

  describe('MyprofileComponent.getTableHeadings() getTableHeadings method', () => {
    describe('Happy Path', () => {
      it('should populate dynamicCols with hierarchy data from localStorage', () => {
        // Arrange
        const hierarchyData = JSON.stringify([
          {
            hierarchyLevelId: 'level1',
            hierarchyLevelName: 'Level 1',
            level: 1,
          },
          {
            hierarchyLevelId: 'level2',
            hierarchyLevelName: 'Level 2',
            level: 2,
          },
        ]);
        localStorage.setItem('hierarchyData', hierarchyData);

        // Act
        component.getTableHeadings();

        // Assert
        expect(component.dynamicCols.length).toEqual(3);
      });
    });

    describe('Edge Cases', () => {
      it('should handle missing hierarchyData in localStorage gracefully', () => {
        // Arrange
        localStorage.setItem('hierarchyData', null);
        const completeHierarchyData = JSON.stringify({
          scrum: [
            {
              hierarchyLevelId: 'level1',
              hierarchyLevelName: 'Level 1',
              level: 1,
            },
            {
              hierarchyLevelId: 'project',
              hierarchyLevelName: 'Project',
              level: 3,
            },
          ],
        });
        localStorage.setItem('completeHierarchyData', completeHierarchyData);

        // Act
        component.getTableHeadings();

        // Assert
        expect(component.dynamicCols.length).toEqual(2);
      });

      it('should handle empty hierarchyData and completeHierarchyData in localStorage', () => {
        // Arrange
        localStorage.setItem('hierarchyData', JSON.stringify([]));
        localStorage.setItem(
          'completeHierarchyData',
          JSON.stringify({ scrum: [] }),
        );

        // Act
        component.getTableHeadings();

        // Assert
        expect(component.dynamicCols.length).toEqual(1);
      });

      it('should set noAccess to false when user is  SuperAdmin and has project access', () => {
        spyOn(authService, 'checkIfSuperUser').and.returnValue(true as any);
        spyOn(shared, 'getCurrentUserDetails').and.returnValue(
          mockProjectsAccess,
        );
        component.ngOnInit();

        expect(component.noAccess).toBe(false);
      });
    });
  });
});
