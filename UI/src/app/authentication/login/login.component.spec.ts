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
import { ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { LoginComponent } from './login.component';
import { HttpService } from '../../services/http.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { HelperService } from 'src/app/services/helper.service';
import { SharedService } from '../../services/shared.service';
import { GoogleAnalyticsService } from 'src/app/services/google-analytics.service';
import { APP_CONFIG, AppConfig } from '../../services/app.config';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let router: Router;
  let httpService: HttpService;
  let sharedService: SharedService;
  let helperService: HelperService;
  let ga: GoogleAnalyticsService;

  const mockRouter = {
    navigate: jasmine.createSpy('navigate'),
  };

  const mockActivatedRoute = {
    snapshot: {
      queryParams: { returnUrl: '/' },
    },
    queryParams: of({ sessionExpire: 'Session expired' }),
  };

  // const mockHttpService = jasmine.createSpyObj('HttpService', ['login', 'handleRestoreUrl', 'getAllProjects']);

  const mockSharedService = {
    getCurrentUserDetails: jasmine.createSpy('getCurrentUserDetails'),
    raiseError: jasmine.createSpy('raiseError'),
  };

  const mockHelperService = jasmine.createSpyObj('HelperService', ['urlShorteningRedirection']);

  const mockGoogleAnalyticsService = {
    setLoginMethod: jasmine.createSpy('setLoginMethod'),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [LoginComponent],
      imports: [ReactiveFormsModule, HttpClientTestingModule],
      providers: [
        { provide: Router, useValue: mockRouter },
        { provide: ActivatedRoute, useValue: mockActivatedRoute },
        { provide: APP_CONFIG, useValue: AppConfig },
        HttpService,
        { provide: SharedService, useValue: mockSharedService },
        { provide: HelperService, useValue: mockHelperService },
        { provide: GoogleAnalyticsService, useValue: mockGoogleAnalyticsService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    httpService = TestBed.inject(HttpService);
    sharedService = TestBed.inject(SharedService) as jasmine.SpyObj<SharedService>;
    ga = TestBed.inject(GoogleAnalyticsService) as jasmine.SpyObj<GoogleAnalyticsService>;

    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize the form on init', () => {
    expect(component.loginForm).toBeDefined();
    expect(component.loginForm.controls['username']).toBeDefined();
    expect(component.loginForm.controls['password']).toBeDefined();
  });

  it('should not submit if form is invalid', () => {
    spyOn(httpService, 'login');
    component.onSubmit();
    expect(component.submitted).toBeTrue();
    expect(httpService.login).not.toHaveBeenCalled();
  });

  xit('should call login service on valid form submission', () => {
    component.loginForm.setValue({ username: 'test', password: 'password' });
    spyOn(httpService,'login');
    component.onSubmit();

    expect(httpService.login).toHaveBeenCalledWith('', 'test', 'password');
  });

  it('should handle 401 error on login', () => {
    component.loginForm.setValue({ username: 'test', password: 'password' });
    spyOn(httpService,'login').and.returnValue(of({ status: 401, error: { message: 'Unauthorized' } }));

    component.onSubmit();

    expect(component.error).toBe('Unauthorized');
    expect(component.f.password.value).toBe('');
  });

  xit('should redirect to profile if conditions are met', () => {
    component.loginForm.setValue({ username: 'test', password: 'password' });
    spyOn(httpService,'login').and.returnValue(of({ status: 200, body: {} }));

    mockSharedService.getCurrentUserDetails.and.returnValue('');

    component.onSubmit();

    expect(router.navigate).toHaveBeenCalledWith(['./dashboard/Config/Profile']);
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

  it('should return true when user email is missing', () => {
    mockSharedService.getCurrentUserDetails.withArgs('user_email').and.returnValue(null);

    expect(component.redirectToProfile()).toBeTrue();
  });

  it('should return true when user email is an empty string', () => {
    mockSharedService.getCurrentUserDetails.withArgs('user_email').and.returnValue('');

    expect(component.redirectToProfile()).toBeTrue();
  });

  it('should return false when user has ROLE_SUPERADMIN', () => {
    mockSharedService.getCurrentUserDetails.withArgs('user_email').and.returnValue('test@example.com');
    mockSharedService.getCurrentUserDetails.withArgs('authorities').and.returnValue(['ROLE_SUPERADMIN']);

    expect(component.redirectToProfile()).toBeFalse();
  });

  it('should return true when projectsAccess is undefined', () => {
    mockSharedService.getCurrentUserDetails.withArgs('user_email').and.returnValue('test@example.com');
    mockSharedService.getCurrentUserDetails.withArgs('authorities').and.returnValue(['ROLE_USER']);
    mockSharedService.getCurrentUserDetails.withArgs('projectsAccess').and.returnValue(undefined);

    expect(component.redirectToProfile()).toBeTrue();
  });

  it('should return true when projectsAccess is an empty array', () => {
    mockSharedService.getCurrentUserDetails.withArgs('user_email').and.returnValue('test@example.com');
    mockSharedService.getCurrentUserDetails.withArgs('authorities').and.returnValue(['ROLE_USER']);
    mockSharedService.getCurrentUserDetails.withArgs('projectsAccess').and.returnValue([]);

    expect(component.redirectToProfile()).toBeTrue();
  });

  it('should return undefined when projectsAccess has data (valid case)', () => {
    mockSharedService.getCurrentUserDetails.withArgs('user_email').and.returnValue('test@example.com');
    mockSharedService.getCurrentUserDetails.withArgs('authorities').and.returnValue(['ROLE_USER']);
    mockSharedService.getCurrentUserDetails.withArgs('projectsAccess').and.returnValue(['Project1']);

    expect(component.redirectToProfile()).toBeUndefined();
  });


  // afterEach(() => {
  //   localStorage.removeItem('shared_link');
  // });
});
