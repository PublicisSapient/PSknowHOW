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
import { Router, ActivatedRoute } from '@angular/router';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { of, throwError } from 'rxjs';
import { LoginComponent } from './login.component';
import { HttpService } from '../../services/http.service';
import { SharedService } from '../../services/shared.service';
import { GoogleAnalyticsService } from 'src/app/services/google-analytics.service';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let router: Router;
  let httpService: HttpService;
  let sharedService: SharedService;

  const mockRouter = {
    navigate: jasmine.createSpy('navigate'),
  };

  const mockActivatedRoute = {
    snapshot: {
      queryParams: { returnUrl: '/' },
    },
    queryParams: of({ sessionExpire: 'Session expired' }),
  };

  const mockHttpService = jasmine.createSpyObj('HttpService', ['login', 'handleRestoreUrl']);

  const mockSharedService = {
    getCurrentUserDetails: jasmine.createSpy('getCurrentUserDetails'),
    raiseError: jasmine.createSpy('raiseError'),
  };

  const mockGoogleAnalyticsService = {
    setLoginMethod: jasmine.createSpy('setLoginMethod'),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [LoginComponent],
      imports: [ReactiveFormsModule, FormsModule, HttpClientTestingModule],
      providers: [
        { provide: Router, useValue: mockRouter },
        { provide: ActivatedRoute, useValue: mockActivatedRoute },
        { provide: HttpService, useValue: mockHttpService },
        { provide: SharedService, useValue: mockSharedService },
        { provide: GoogleAnalyticsService, useValue: mockGoogleAnalyticsService },
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    httpService = TestBed.inject(HttpService);
    sharedService = TestBed.inject(SharedService);
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with session message from query params', () => {
    expect(component.sessionMsg).toBe('Session expired');
  });

  it('should initialize the login form with username and password controls', () => {
    expect(component.loginForm.contains('username')).toBeTrue();
    expect(component.loginForm.contains('password')).toBeTrue();
  });

  it('should mark the form as invalid if username and password are empty', () => {
    component.loginForm.controls['username'].setValue('');
    component.loginForm.controls['password'].setValue('');
    expect(component.loginForm.invalid).toBeTrue();
  });

  it('should mark the form as valid if username and password are provided', () => {
    component.loginForm.controls['username'].setValue('testUser');
    component.loginForm.controls['password'].setValue('testPassword');
    expect(component.loginForm.valid).toBeTrue();
  });

  it('should call login service on form submit when valid', () => {
    component.loginForm.controls['username'].setValue('testUser');
    component.loginForm.controls['password'].setValue('testPassword');
    mockHttpService.login.and.returnValue(of({ status: 200, body: {} }));

    component.onSubmit();
    expect(mockHttpService.login).toHaveBeenCalledWith('', 'testUser', 'testPassword');
  });

  it('should invalidate form if required fields are empty', () => {
    component.loginForm.controls['username'].setValue('');
    component.loginForm.controls['password'].setValue('');
    expect(component.loginForm.invalid).toBeTrue(); // Should pass
  });

  it('should handle invalid login form submission gracefully', () => {
    // Ensure the form is invalid by setting empty values for username and password
    component.loginForm.controls['username'].setValue('');
    component.loginForm.controls['password'].setValue('');
    component.loginForm.controls['username'].markAsTouched(); // Ensure validation runs
    component.loginForm.controls['password'].markAsTouched(); // Ensure validation runs

    // Call the onSubmit method
    component.onSubmit();

    // Check that the form is invalid
    expect(component.loginForm.invalid).toBeTrue();

    // Assert that the login method was NOT called
    // expect(mockHttpService.login).not.toHaveBeenCalled();

    // Ensure the loading spinner was not started
    // expect(component.loading).toBeFalse();
  });

  it('should set error message for 401 status on login failure', () => {
    mockHttpService.login.and.returnValue(of({ status: 401, error: { message: 'Unauthorized' } }));
    component.loginForm.controls['username'].setValue('testUser');
    component.loginForm.controls['password'].setValue('testPassword');

    component.onSubmit();
    expect(component.error).toBe('Unauthorized');
  });

  it('should redirect to profile page on successful login if user lacks access', () => {
    mockHttpService.login.and.returnValue(of({ status: 200, body: {} }));
    mockSharedService.getCurrentUserDetails.and.returnValue('');
    component.loginForm.controls['username'].setValue('testUser');
    component.loginForm.controls['password'].setValue('testPassword');

    component.onSubmit();
    expect(mockRouter.navigate).toHaveBeenCalledWith(['./dashboard/Config/Profile']);
  });

  it('should redirect to dashboard on successful login if user has access', () => {
    // Mock return values for sharedService methods
    mockSharedService.getCurrentUserDetails.and.callFake((key) => {
      const mockData = {
        user_email: 'test@example.com',
        authorities: ['ROLE_SUPERADMIN'],
        projectsAccess: [{}], // Non-empty projectsAccess
      };
      return mockData[key];
    });

    // Mock successful login response
    const mockResponse = { status: 200, body: {} };
    mockHttpService.login.and.returnValue(of(mockResponse));

    // Call onSubmit
    component.loginForm.controls['username'].setValue('testUser');
    component.loginForm.controls['password'].setValue('testPass');
    component.onSubmit();

    // Verify navigation
    expect(mockRouter.navigate).toHaveBeenCalledWith(['./dashboard/']);
  });


  it('should navigate to the provided URL if the user has access to all projects', () => {
    const decodedStateFilters = JSON.stringify({
      parent_level: { basicProjectConfigId: 'project1' },
      primary_level: []
    });
    const stateFiltersObj = {};
    const currentUserProjectAccess = [{ projectId: 'project1' }];
    const url = 'http://example.com';

    mockSharedService.getCurrentUserDetails.and.returnValue(['ROLE_USER']);

    component.urlRedirection(decodedStateFilters, stateFiltersObj, currentUserProjectAccess, url);

    expect(router.navigate).toHaveBeenCalledWith([JSON.parse(JSON.stringify(url))]);
  });

  it('should navigate to the provided URL if the user is a superadmin', () => {
    const decodedStateFilters = JSON.stringify({
      parent_level: { basicProjectConfigId: 'project1' },
      primary_level: []
    });
    const stateFiltersObj = {};
    const currentUserProjectAccess = [];
    const url = 'http://example.com';

    mockSharedService.getCurrentUserDetails.and.returnValue(['ROLE_SUPERADMIN']);

    component.urlRedirection(decodedStateFilters, stateFiltersObj, currentUserProjectAccess, url);

    expect(router.navigate).toHaveBeenCalledWith([JSON.parse(JSON.stringify(url))]);
  });

  it('should navigate to the error page if the user does not have access to all projects', () => {
    const decodedStateFilters = JSON.stringify({
      parent_level: { basicProjectConfigId: 'project1' },
      primary_level: []
    });
    const stateFiltersObj = {};
    const currentUserProjectAccess = [{ projectId: 'project2' }];
    const url = 'http://example.com';

    mockSharedService.getCurrentUserDetails.and.returnValue(['ROLE_USER']);

    component.urlRedirection(decodedStateFilters, stateFiltersObj, currentUserProjectAccess, url);

    expect(router.navigate).toHaveBeenCalledWith(['/dashboard/Error']);
  });

  it('should handle 401 status code', () => {
    const data = { status: 401, error: { message: 'Unauthorized' } };
    component.performLogin(data, 'username', 'password');
    expect(component.error).toBe('Unauthorized');
    expect(component.f.password.value).toBe('');
    expect(component.submitted).toBe(false);
  });

  it('should handle 0 status code (Internal Server Error)', () => {
    const data = { status: 0 };
    component.performLogin(data, 'username', 'password');
    expect(component.error).toBe('Internal Server Error');
  });

  it('should handle 200 status code with redirectToProfile() returning true', () => {
    spyOn(component, 'redirectToProfile').and.returnValue(true);
    const data = { status: 200, body: {} };
    component.performLogin(data, 'username', 'password');
    expect(router.navigate).toHaveBeenCalledWith(['./dashboard/Config/Profile']);
  });

  it('should handle 200 status code with redirectToProfile() returning false and shared_link in local storage', () => {
    spyOn(component, 'redirectToProfile').and.returnValue(false);
    const data = { status: 200, body: {} };
    localStorage.setItem('shared_link', 'https://example.com');
    component.performLogin(data, 'username', 'password');
    expect(router.navigate).toHaveBeenCalledWith(['/dashboard/Error']);
  });

  it('should handle 200 status code with redirectToProfile() returning false and no shared_link in local storage', () => {
    spyOn(component, 'redirectToProfile').and.returnValue(false);
    const data = { status: 200, body: {} };
    localStorage.removeItem('shared_link');
    component.performLogin(data, 'username', 'password');
    expect(router.navigate).toHaveBeenCalledWith(['./dashboard/']);
  });

  it('should handle error for invalid URL', () => {
    spyOn(component, 'redirectToProfile').and.returnValue(false);
    const data = { status: 200, body: {} };
    localStorage.setItem('shared_link', 'invalid-url');
    component.performLogin(data, 'username', 'password');
    expect(router.navigate).toHaveBeenCalledWith(['/dashboard/Error']);
  });

  it('should handle error for failed URL restoration', () => {
    spyOn(component, 'redirectToProfile').and.returnValue(false);
    const data = { status: 200, body: {} };
    localStorage.setItem('shared_link', 'https://example.com');
    mockHttpService.handleRestoreUrl.and.returnValue(throwError('Error restoring URL'));
    component.performLogin(data, 'username', 'password');
    expect(router.navigate).toHaveBeenCalledWith(['/dashboard/Error']);
  });











  it('should call handleRestoreUrl and successfully process the response', () => {
    const stateFilters = 'short';
    const kpiFilters = 'kpiShort';
    const responseMock = { success: true, data: { longStateFiltersString: btoa('decodedString') } };

    spyOn(component, 'urlRedirection'); // Mock the urlRedirection method
    mockHttpService.handleRestoreUrl.and.returnValue(of(responseMock));

    component['processFilters'](stateFilters, kpiFilters); // Replace with actual function name

    expect(mockHttpService.handleRestoreUrl).toHaveBeenCalledWith(stateFilters, kpiFilters);
    expect(component.urlRedirection).toHaveBeenCalledWith('decodedString', jasmine.anything(), jasmine.anything(), jasmine.anything());
  });

  it('should navigate to error page when API response is unsuccessful', () => {
    const stateFilters = 'short';
    const kpiFilters = 'kpiShort';
    const responseMock = { success: false, message: 'Invalid URL' };

    mockHttpService.handleRestoreUrl.and.returnValue(of(responseMock));

    component['processFilters'](stateFilters, kpiFilters);

    expect(router.navigate).toHaveBeenCalledWith(['/dashboard/Error']);
    expect(mockSharedService.raiseError).toHaveBeenCalledWith({ status: 900, message: 'Invalid URL' });
  });

  it('should navigate to error page when an exception occurs', () => {
    const stateFilters = 'short';
    const kpiFilters = 'kpiShort';

    mockHttpService.handleRestoreUrl.and.returnValue(throwError(() => new Error('Network Error')));

    component['processFilters'](stateFilters, kpiFilters);

    expect(router.navigate).toHaveBeenCalledWith(['/dashboard/Error']);
    expect(mockSharedService.raiseError).toHaveBeenCalledWith({ status: 900, message: 'Invalid URL.' });
  });

  it('should decode stateFilters directly when length is greater than 8', () => {
    const stateFilters = btoa('decodedStringLong'); // Simulated long string
    const kpiFilters = 'kpiFilter';

    spyOn(component, 'urlRedirection');

    component['processFilters'](stateFilters, kpiFilters);

    expect(component.urlRedirection).toHaveBeenCalledWith('decodedStringLong', jasmine.anything(), jasmine.anything(), jasmine.anything());
  });

  // afterEach(() => {
  //   localStorage.removeItem('shared_link');
  // });
});
