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

import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpService } from 'src/app/services/http.service';
import { SharedService } from 'src/app/services/shared.service';
import { Router, ActivatedRoute } from '@angular/router';
import { of, throwError } from 'rxjs';
import { Location } from '@angular/common';
import { UntypedFormBuilder } from '@angular/forms';
import { HelperService } from 'src/app/services/helper.service'; // Import HelperService
import { LoginComponent } from './login.component';
import { GoogleAnalyticsService } from 'src/app/services/google-analytics.service';

const mockProjectsData = [
  {
    id: "667d496d7946571a7ed6ae15",
    projectNodeId: "0a556ab3-cb3d-4333-bd69-7cf5ffe06082",
    projectName: "Digital 4 - FCASDP",
    projectDisplayName: "Digital 4 - FCASDP",
    createdAt: "2024-06-27T11:13:49",
    updatedAt: "2024-09-07T11:53:14",
    updatedBy: "KnowHOW System Admin",
    kanban: false,
    hierarchy: [
      {
        hierarchyLevel: { level: 1, hierarchyLevelId: "bu", hierarchyLevelName: "BU" },
        orgHierarchyNodeId: "2c234a25-e1bb-402e-9443-e947d994f9f5",
        value: "International"
      },
      {
        hierarchyLevel: { level: 2, hierarchyLevelId: "ver", hierarchyLevelName: "Vertical" },
        orgHierarchyNodeId: "5db14cc1-23e2-40da-8964-8c39384c3e46",
        value: "Team Intergroup - EU GDD"
      },
      {
        hierarchyLevel: { level: 3, hierarchyLevelId: "acc", hierarchyLevelName: "Account" },
        orgHierarchyNodeId: "781af435-4ba3-451f-9461-62f719becd9a",
        value: "Stellantis N.V."
      },
      {
        hierarchyLevel: { level: 4, hierarchyLevelId: "port", hierarchyLevelName: "Engagement" },
        orgHierarchyNodeId: "a6df0764-317a-4db2-b5df-c82de58d9a04",
        value: "Customer end to end reporting"
      }
    ],
    saveAssigneeDetails: false,
    developerKpiEnabled: false,
    projectOnHold: false,
    isKanban: false
  }
];

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let mockHttpService: jasmine.SpyObj<HttpService>;
  let mockSharedService: jasmine.SpyObj<SharedService>;
  let mockRouter: jasmine.SpyObj<Router>;
  let mockLocation: jasmine.SpyObj<Location>;
  let mockActivatedRoute: any;
  let mockHelperService: jasmine.SpyObj<HelperService>; // Mock HelperService
  let mockGAService: jasmine.SpyObj<GoogleAnalyticsService>;

  beforeEach(async () => {
    mockHttpService = jasmine.createSpyObj('HttpService', ['login', 'getAllProjects','handleRestoreUrl']);
    mockSharedService = jasmine.createSpyObj('SharedService', [
      'setSelectedBoard',
      'setKpiSubFilterObj',
      'getKpiSubFilterObj',
      'setBackupOfFilterSelectionState',
      'raiseError',
      'getCurrentUserDetails'
    ]);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);
    mockLocation = jasmine.createSpyObj('Location', ['path']);
    mockHelperService = jasmine.createSpyObj('HelperService', ['someHelperMethod','urlShorteningRedirection']); // Spy HelperService
    mockGAService = jasmine.createSpyObj('GoogleAnalyticsService', ['setLoginMethod']);
    mockHttpService.login.and.returnValue(of({ status: 200, body: { token: 'dummy-token' } }));
    mockHttpService.getAllProjects.and.returnValue(of(mockProjectsData));


    // Mock ActivatedRoute with queryParams observable
    mockActivatedRoute = {
      snapshot: { queryParams: { returnUrl: '/', sessionExpire: 'true' } },
      queryParams: of({
        stateFilters: btoa(JSON.stringify([{ id: 1, name: 'Filter1' }])),
        kpiFilters: btoa(JSON.stringify([{ id: 2, name: 'KPI1' }]))
      })
    };

    await TestBed.configureTestingModule({
      declarations: [LoginComponent],
      providers: [
        UntypedFormBuilder,
        { provide: HttpService, useValue: mockHttpService },
        { provide: SharedService, useValue: mockSharedService },
        { provide: Router, useValue: mockRouter },
        { provide: Location, useValue: mockLocation },
        { provide: ActivatedRoute, useValue: mockActivatedRoute },
        { provide: HelperService, useValue: mockHelperService }, // ✅ Added HelperService Mock
        { provide: GoogleAnalyticsService, useValue: mockGAService },
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize the loginForm correctly', () => {
    expect(component.loginForm).toBeDefined();
    expect(component.loginForm.controls['username']).toBeDefined();
    expect(component.loginForm.controls['password']).toBeDefined();
  });

  it('should set returnUrl from queryParams', () => {
    expect(component.returnUrl).toBe('/');
  });

  it('should handle stateFilters and kpiFilters', fakeAsync(() => {
    component.ngOnInit();
    tick(); // Simulates async execution

    expect(mockSharedService.setSelectedBoard).toHaveBeenCalled();
    expect(mockSharedService.setBackupOfFilterSelectionState).toHaveBeenCalled();
  }));

  it('should handle URL restore successfully', fakeAsync(() => {
    const mockResponse = {
      success: true,
      data: {
        longKPIFiltersString: btoa(JSON.stringify([{ id: 2, name: 'KPI1' }])),
        longStateFiltersString: btoa(JSON.stringify([{ id: 1, name: 'Filter1' }]))
      }
    };

    mockHttpService.handleRestoreUrl.and.returnValue(of(mockResponse));

    component.ngOnInit();
    tick();

    expect(mockSharedService.setBackupOfFilterSelectionState).toHaveBeenCalledWith([{ id: 1, name: 'Filter1' }]);
    expect(mockSharedService.setKpiSubFilterObj).toHaveBeenCalledWith([{ id: 2, name: 'KPI1' }]);
  }));

  it('should initialize loginForm with empty values', () => {
    expect(component.loginForm).toBeDefined();
    expect(component.loginForm.controls['username'].value).toBe('');
    expect(component.loginForm.controls['password'].value).toBe('');
  });

  it('should return form controls from getter `f()`', () => {
    expect(component.f).toBeDefined();
    expect(component.f.username).toBeDefined();
    expect(component.f.password).toBeDefined();
  });

  it('should stop execution if form is invalid', () => {
    component.loginForm.controls['username'].setValue('');
    component.loginForm.controls['password'].setValue('');
    component.onSubmit();
    expect(component.loading).toBeFalse();
    expect(mockHttpService.login).not.toHaveBeenCalled();
  });

  it('should call login service on valid form submission', fakeAsync(() => {
    component.loginForm.controls['username'].setValue('testuser');
    component.loginForm.controls['password'].setValue('password123');

    mockHttpService.login.and.returnValue(of({ status: 200, body: { token: 'dummy-token' } }));

    component.onSubmit();
    tick(); // Simulates async execution

    expect(component.loading).toBeFalse();
    expect(mockHttpService.login).toHaveBeenCalledWith('', 'testuser', 'password123');
  }));

  it('should return true if user email is missing in `redirectToProfile()`', () => {
    mockSharedService.getCurrentUserDetails.and.returnValue('');
    expect(component.redirectToProfile()).toBeTrue();
  });

  it('should return false if user has ROLE_SUPERADMIN in `redirectToProfile()`', () => {
    mockSharedService.getCurrentUserDetails.and.returnValue(['ROLE_SUPERADMIN']);
    expect(component.redirectToProfile()).toBeFalse();
  });

  it('should return true if projectsAccess is undefined or empty in `redirectToProfile()`', () => {
    mockSharedService.getCurrentUserDetails.and.returnValue(undefined);
    expect(component.redirectToProfile()).toBeTrue();
  });

  it('should handle login failure with status 401 in `performLogin()`', () => {
    const mockData = { status: 401, error: { message: 'Invalid credentials' } };

    component.performLogin(mockData, 'testuser', 'password123');

    expect(component.loading).toBeFalse();
    expect(component.f.password.value).toBe('');
    expect(component.submitted).toBeFalse();
    expect(component.error).toBe('Invalid credentials');
  });

  it('should handle login failure with status 0 (server error) in `performLogin()`', () => {
    const mockData = { status: 0 };

    component.performLogin(mockData, 'testuser', 'password123');

    expect(component.loading).toBeFalse();
    expect(component.error).toBe('Internal Server Error');
  });

  it('should handle successful login and redirect in `performLogin()`', fakeAsync(() => {
    const mockData = { status: 200, body: { token: 'dummy-token' } };
    const mockProjects = { data: [{ id: 1, name: 'Project 1' }] };

    mockHttpService.getAllProjects.and.returnValue(of(mockProjects));
    spyOn(localStorage, 'setItem');

    component.performLogin(mockData, 'testuser', 'password123');
    tick();

    expect(mockGAService.setLoginMethod).toHaveBeenCalledWith(mockData.body, 'standard');
    expect(mockHttpService.getAllProjects).toHaveBeenCalled();
    expect(localStorage.setItem).toHaveBeenCalledWith('projectWithHierarchy', JSON.stringify(mockProjects.data));
  }));

  it('should redirect to profile if `redirectToProfile()` returns true in `performLogin()`', fakeAsync(() => {
    const mockData = { status: 200, body: { token: 'dummy-token' } };
    spyOn(component, 'redirectToProfile').and.returnValue(true);

    component.performLogin(mockData, 'testuser', 'password123');
    tick();

    expect(mockRouter.navigate).toHaveBeenCalledWith(['./dashboard/Config/Profile']);
  }));

  it('should call `urlShorteningRedirection` if `redirectToProfile()` returns false in `performLogin()`', fakeAsync(() => {
    const mockData = { status: 200, body: { token: 'dummy-token' } };
    spyOn(component, 'redirectToProfile').and.returnValue(false);

    component.performLogin(mockData, 'testuser', 'password123');
    tick();

    expect(mockHelperService.urlShorteningRedirection).toHaveBeenCalled();
  }));

  it('should return true when projectsAccess is "undefined"', () => {
    // Mock other calls to avoid the error
    mockSharedService.getCurrentUserDetails.withArgs('user_email').and.returnValue('test@example.com');
    mockSharedService.getCurrentUserDetails.withArgs('authorities').and.returnValue([]);
    
    // Mock the specific case for 'projectsAccess'
    mockSharedService.getCurrentUserDetails.withArgs('projectsAccess').and.returnValue('undefined');
  
    const result = component.redirectToProfile();
    
    expect(result).toBeTrue();
  });
  
  it('should return true when projectsAccess is an empty array', () => {
    // Mock other calls to avoid the error
    mockSharedService.getCurrentUserDetails.withArgs('user_email').and.returnValue('test@example.com');
    mockSharedService.getCurrentUserDetails.withArgs('authorities').and.returnValue([]);
  
    // Mock the specific case for 'projectsAccess'
    mockSharedService.getCurrentUserDetails.withArgs('projectsAccess').and.returnValue([]);
  
    const result = component.redirectToProfile();
  
    expect(result).toBeTrue();
  });
});

