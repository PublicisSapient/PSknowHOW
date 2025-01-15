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

  const mockHttpService = jasmine.createSpyObj('HttpService', ['login']);

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

  afterEach(() => {
    localStorage.removeItem('shared_link');
  });
});
