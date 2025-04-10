import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HeaderComponent } from './header.component';

import { RouterTestingModule } from '@angular/router/testing';
import { SharedService } from '../../services/shared.service';
import { HelperService } from 'src/app/services/helper.service';
import { GetAuthService } from '../../services/getauth.service';
import { HttpClientModule } from '@angular/common/http';
import { APP_CONFIG, AppConfig } from '../../services/app.config';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpService } from '../../services/http.service';
import { CommonModule, DatePipe } from '@angular/common';
import { of } from 'rxjs';
import { Router, Routes } from '@angular/router';
import { LoginComponent } from 'src/app/authentication/login/login.component';
import { RequestStatusComponent } from 'src/app/config/profile/request-status/request-status.component';
import { GetAuthorizationService } from 'src/app/services/get-authorization.service';
import { ViewNewUserAuthRequestComponent } from 'src/app/config/profile/view-new-user-auth-request/view-new-user-auth-request.component';
import { ViewRequestsComponent } from 'src/app/config/profile/view-requests/view-requests.component';
import { ExecutiveV2Component } from '../executive-v2/executive-v2.component';
import { MessageService } from 'primeng/api';

describe('HeaderComponent', () => {
  let component: HeaderComponent;
  let fixture: ComponentFixture<HeaderComponent>;
  let routerSpy: jasmine.SpyObj<Router>;
  let getAuth: GetAuthService;
  let httpService: HttpService;
  let sharedService: SharedService;
  let helperService: HelperService;
  let mockGetAuthorizationService;
  let mockRouter;
  let messageService: jasmine.SpyObj<MessageService>;

  const routes: Routes = [
    { path: 'authentication/login', component: LoginComponent },
    { path: 'dashboard', component: ExecutiveV2Component },
    {
      path: 'dashboard/Config/Profile/RequestStatus',
      component: RequestStatusComponent,
    },
    {
      path: 'dashboard/Config/Profile/GrantNewUserAuthRequests',
      component: ViewNewUserAuthRequestComponent,
    },
    {
      path: 'dashboard/Config/Profile/GrantRequests',
      component: ViewRequestsComponent,
    },
  ];

  beforeEach(async () => {
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    await TestBed.configureTestingModule({
      declarations: [HeaderComponent],
      imports: [
        RouterTestingModule.withRoutes(routes),
        HttpClientModule,
        BrowserAnimationsModule,
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],

      providers: [
        SharedService,
        GetAuthService,
        HttpService,
        HelperService,
        CommonModule,
        DatePipe,
        GetAuthorizationService,
        MessageService,
        { provide: APP_CONFIG, useValue: AppConfig },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(HeaderComponent);
    component = fixture.componentInstance;
    getAuth = TestBed.get(GetAuthService);
    httpService = TestBed.inject(HttpService);
    sharedService = TestBed.inject(SharedService);
    helperService = TestBed.inject(HelperService);
    mockGetAuthorizationService = jasmine.createSpyObj(
      GetAuthorizationService,
      ['checkIfSuperUser', 'checkIfProjectAdmin'],
    );
    mockRouter = component.router;
    httpService.setCurrentUserDetails({
      user_email: 'rishabh@mailinator.com',
      user_id: '67a9dc720edaa90655f684b6',
      projectsAccess: [
        {
          role: 'ROLE_PROJECT_ADMIN',
          projects: [
            {
              projectName: 'Abu Dhabi Housing Authority',
              projectId: '66601953bc80f461490c653d',
              hierarchy: [
                {
                  hierarchyLevel: {
                    level: 1,
                    hierarchyLevelId: 'bu',
                    hierarchyLevelName: 'BU',
                  },
                  orgHierarchyNodeId: '1acc3651-0313-4331-9093-087aa930c4c5',
                  value: 'International',
                },
                {
                  hierarchyLevel: {
                    level: 2,
                    hierarchyLevelId: 'ver',
                    hierarchyLevelName: 'Vertical',
                  },
                  orgHierarchyNodeId: 'b849d1b3-69bd-45fc-8d4f-b117ef0660b0',
                  value: 'Travel',
                },
                {
                  hierarchyLevel: {
                    level: 3,
                    hierarchyLevelId: 'acc',
                    hierarchyLevelName: 'Account',
                  },
                  orgHierarchyNodeId: '0498fcab-69ce-40ca-9e1e-a02e3bb526a5',
                  value: 'ADEO',
                },
                {
                  hierarchyLevel: {
                    level: 4,
                    hierarchyLevelId: 'port',
                    hierarchyLevelName: 'Engagement',
                  },
                  orgHierarchyNodeId: '38a58b11-c94a-486c-9f24-48d815d3ec0c',
                  value: 'App Development',
                },
              ],
            },
            {
              projectName: 'ATS',
              projectId: '6641e8cd1ec9a84d82ce380d',
              hierarchy: [
                {
                  hierarchyLevel: {
                    level: 1,
                    hierarchyLevelId: 'bu',
                    hierarchyLevelName: 'BU',
                  },
                  orgHierarchyNodeId: 'ad7cae57-9f07-44df-8e50-b807e81a156c',
                  value: 'North America',
                },
                {
                  hierarchyLevel: {
                    level: 2,
                    hierarchyLevelId: 'ver',
                    hierarchyLevelName: 'Vertical',
                  },
                  orgHierarchyNodeId: '5267cdbd-5db5-4377-aa1c-7941a94a4f87',
                  value: 'Financial Services',
                },
                {
                  hierarchyLevel: {
                    level: 3,
                    hierarchyLevelId: 'acc',
                    hierarchyLevelName: 'Account',
                  },
                  orgHierarchyNodeId: '75752111-6d80-49f2-9c73-36dff4711b75',
                  value: 'DTCC (TRM-PS)',
                },
                {
                  hierarchyLevel: {
                    level: 4,
                    hierarchyLevelId: 'port',
                    hierarchyLevelName: 'Engagement',
                  },
                  orgHierarchyNodeId: '92b9e249-44c4-4cff-be25-7a09eeed1ad9',
                  value: 'CMRS',
                },
              ],
            },
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
                  orgHierarchyNodeId: '8f3ea064-46ca-45eb-a80d-20574993cb47',
                  value: 'Internal',
                },
                {
                  hierarchyLevel: {
                    level: 2,
                    hierarchyLevelId: 'ver',
                    hierarchyLevelName: 'Vertical',
                  },
                  orgHierarchyNodeId: 'eae72283-edb8-4ea9-98c6-6189351b0942',
                  value: 'PS Internal',
                },
                {
                  hierarchyLevel: {
                    level: 3,
                    hierarchyLevelId: 'acc',
                    hierarchyLevelName: 'Account',
                  },
                  orgHierarchyNodeId: '9edb8a54-3a7a-4fb5-b1c6-0b648f7f77dc',
                  value: 'Methods and Tools',
                },
                {
                  hierarchyLevel: {
                    level: 4,
                    hierarchyLevelId: 'port',
                    hierarchyLevelName: 'Engagement',
                  },
                  orgHierarchyNodeId: 'f27001d2-c935-4b14-ab5a-4d038c586978',
                  value: 'DTS',
                },
              ],
            },
            {
              projectName: ' Bang  Olufsen Omnichannel implementation',
              projectId: '66d7da7258ffc53913fb840c',
              hierarchy: [
                {
                  hierarchyLevel: {
                    level: 1,
                    hierarchyLevelId: 'bu',
                    hierarchyLevelName: 'BU',
                  },
                  orgHierarchyNodeId: 'bea9afff-a419-4b90-a000-21e9f6a280bc',
                  value: 'EU',
                },
                {
                  hierarchyLevel: {
                    level: 2,
                    hierarchyLevelId: 'ver',
                    hierarchyLevelName: 'Vertical',
                  },
                  orgHierarchyNodeId: '12b46925-a494-40e6-8225-b8bc04192072',
                  value: 'Consumer Products',
                },
                {
                  hierarchyLevel: {
                    level: 3,
                    hierarchyLevelId: 'acc',
                    hierarchyLevelName: 'Account',
                  },
                  orgHierarchyNodeId: '835d444e-39e1-4ace-bab5-08de9b618158',
                  value: 'Bang & Olufsen A/S',
                },
                {
                  hierarchyLevel: {
                    level: 4,
                    hierarchyLevelId: 'port',
                    hierarchyLevelName: 'Engagement',
                  },
                  orgHierarchyNodeId: '9b9f47ea-10ab-4426-95bd-fc6e0703c537',
                  value: 'Bang & Olufsen',
                },
              ],
            },
          ],
        },
      ],
      user_name: 'RishabhQA',
      'X-Authentication-Token':
        'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJSaXNoYWJoUUEiLCJkZXRhaWxzIjoiU1RBTkRBUkQiLCJyb2xlcyI6WyJST0xFX1BST0pFQ1RfQURNSU4iXSwiZXhwIjoxNzQxODQyNzM3fQ.XIr0Yyb1ETNS14uUPg923AJgiXW0Th8Njv4vnwJPmysxKk-jey1syMDUqnOzJFRwDnDFl8Mfbc0L48GcxbU6Ow',
      authType: 'STANDARD',
      notificationEmail: null,
      authorities: ['ROLE_PROJECT_ADMIN'],
    });
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should set backToDashboardLoader to true, navigate to lastVisitedFromUrl, and set backToDashboardLoader to false', () => {
    component.lastVisitedFromUrl = '/dashboard';
    component.backToDashboardLoader = false;

    component.navigateToDashboard();

    expect(component.backToDashboardLoader).toBe(false);
  });

  it('should navigate to the correct route when user is super user', () => {
    component.ifSuperUser = true;
    const type = 'Project Access Request';
    const navigateSpy = spyOn(mockRouter, 'navigate');
    component.routeForAccess(type);

    expect(navigateSpy).toHaveBeenCalledWith([
      '/dashboard/Config/Profile/GrantRequests',
    ]);
  });

  it('should navigate to the correct route when user is project admin', () => {
    component.ifProjectAdmin = true;
    const type = 'User Access Request';
    const navigateSpy = spyOn(mockRouter, 'navigate');
    component.routeForAccess(type);

    expect(navigateSpy).toHaveBeenCalledWith([
      '/dashboard/Config/Profile/GrantNewUserAuthRequests',
    ]);
  });

  it('should navigate to the default route when type is not recognized', () => {
    const type = 'Invalid Type';
    const navigateSpy = spyOn(mockRouter, 'navigate');
    component.routeForAccess(type);

    expect(navigateSpy).toHaveBeenCalledWith([
      '/dashboard/Config/Profile/RequestStatus',
    ]);
  });

  it('should navigate to the default route when user is not super user or project admin', () => {
    component.ifProjectAdmin = false;
    component.ifSuperUser = false;
    const type = 'Project Access Request';
    const navigateSpy = spyOn(mockRouter, 'navigate');
    component.routeForAccess(type);

    expect(navigateSpy).toHaveBeenCalledWith([
      '/dashboard/Config/Profile/RequestStatus',
    ]);
  });

  it('should notification list not null if response is comming', () => {
    const fakeResponce = {
      message: 'Data came successfully',
      success: true,
      data: [{ count: 2, type: 'User Access Request' }],
    };
    spyOn(httpService, 'getAccessRequestsNotifications').and.returnValue(
      of(fakeResponce),
    );
    component.getNotification();
    expect(component.notificationList).not.toBe(null);
  });

  it('should navigate to /dashboard/my-knowhow when previousSelectedTab is Config', () => {
    const navigateSpy = spyOn(mockRouter, 'navigate');
    spyOnProperty(mockRouter, 'url', 'get').and.returnValue(
      '/dashboard/Config',
    );
    component.navigateToMyKnowHOW();
    expect(navigateSpy).toHaveBeenCalledWith(['/dashboard/my-knowhow']);
  });

  it('should navigate to /dashboard/my-knowhow when previousSelectedTab is Help', () => {
    const navigateSpy = spyOn(mockRouter, 'navigate');
    spyOnProperty(mockRouter, 'url', 'get').and.returnValue('/dashboard/Help');
    component.navigateToMyKnowHOW();
    expect(navigateSpy).toHaveBeenCalledWith(['/dashboard/my-knowhow']);
  });

  it('should not navigate to /dashboard/my-knowhow when previousSelectedTab is not Config or Help', () => {
    const navigateSpy = spyOn(mockRouter, 'navigate');
    spyOnProperty(mockRouter, 'url', 'get').and.returnValue(
      '/dashboard/SomeOtherTab',
    );
    component.navigateToMyKnowHOW();
    expect(navigateSpy).not.toHaveBeenCalledWith(['/dashboard/my-knowhow']);
  });

  it('should call helperService.logoutHttp() when logout() is called', () => {
    spyOn(helperService, 'logoutHttp');
    component.logout();
    expect(helperService.logoutHttp).toHaveBeenCalled();
  });

  it('should call getNotification when passEventToNav emits an event', () => {
    const getNotificationSpy = spyOn(component, 'getNotification');
    component.ngOnInit();
    sharedService.passEventToNav.subscribe();
    expect(getNotificationSpy).toHaveBeenCalled();
  });
});
