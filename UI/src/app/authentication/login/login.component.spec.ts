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
import { CommonModule } from '@angular/common';
import { InputSwitchModule } from 'primeng/inputswitch';
import { ReactiveFormsModule, UntypedFormControl, UntypedFormGroup } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { LoginComponent } from '../login/login.component';
import { RegisterComponent } from '../register/register.component';
import { Routes, Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { APP_CONFIG, AppConfig } from '../../services/app.config';
import { DashboardComponent } from '../../dashboard/dashboard.component';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { HttpService } from '../../services/http.service';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { environment } from 'src/environments/environment';
import { SharedService } from '../../services/shared.service';
import { MyprofileComponent } from '../../config/profile/myprofile/myprofile.component';
import { of } from 'rxjs';
import { GoogleAnalyticsService } from 'src/app/services/google-analytics.service';

describe('LoginComponent', () => {

  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  const baseUrl = environment.baseUrl;
  let httpMock;
  let httpreq;
  let httpService;
  let sharedService;
  let ga;
  let router: Router;
  const fakeLogin = {
    instance_owner: 'kbakshi@sapient.com',
    user_email: 'test@gmail.com',
    projectsAccess: [],
    user_name: 'SUPERADMIN',
    account_name: 'XYZ',
    'X-Authentication-Token': 'dummytokenstring',
    project_name: 'XYZ',
    authorities: [
      'ROLE_SUPERADMIN'
    ]
  };
  const fakeInvalidLogin = { timestamp: 1567511436517, status: 401, error: 'Unauthorized', message: 'Authentication Failed: Login Failed: The username or password entered is incorrect', path: '/api/login' };
  const fakeLoginResponse = {
    body: {
      ['X-Authentication-Token']:
        'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJTVVBFUkFETUlOIiwiZGV0YWlscyI6IlNUQU5EQVJEIiwicm9sZXMiOlsiUk9MRV9TVVBFUkFETUlOIl0sImV4cCI6MTY3NDUwNzA1MX0.Ad32D8uiQmXLzdFVUqnIVETM8Vtb55yceFVW4AT-Z4MFixLUWbAeVEpZdvFuyrTKMgqRd08L-gWO-nQH-bi5qw',
      authorities: ['ROLE_SUPERADMIN'],
      projectsAccess: [],
      user_email: 'knowledgesharing@publicissapient.com',
      user_name: 'SUPERADMIN',
    },
    status: 200,
  };

  beforeEach(waitForAsync(() => {

    const routes: Routes = [
      { path: 'dashboard', component: DashboardComponent },
      { path: 'authentication/login', component: LoginComponent },
      { path: 'dashboard/Config/Profile', component: MyprofileComponent },

    ];

    TestBed.configureTestingModule({
      imports: [
        FormsModule,
        InputSwitchModule,
        ReactiveFormsModule,
        CommonModule,
        HttpClientTestingModule, // no need to import http client module
        RouterTestingModule.withRoutes(routes)
      ],
      declarations: [LoginComponent,
        RegisterComponent, DashboardComponent, MyprofileComponent],
      providers: [{ provide: APP_CONFIG, useValue: AppConfig },
        HttpService, SharedService,
      { provide: Router, useValue: { navigate: jasmine.createSpy('navigate') } },
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]

    })
      .compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    httpService = TestBed.get(HttpService);
    sharedService = TestBed.get(SharedService);
    router = TestBed.inject(Router);
    httpMock = TestBed.get(HttpTestingController);
    ga = TestBed.get(GoogleAnalyticsService);
    fixture.detectChanges();
  }));


  it('should create', () => {
    expect(component).toBeTruthy();
  });



  it('invalid form should not call login', waitForAsync(() => {
    component.loginForm.controls['username'].setValue('');
    component.loginForm.controls['password'].setValue('');
    component.onSubmit();
    expect(component.loginForm.invalid).toBeTruthy();
  }));

  xit('valid form with correct username pswd', waitForAsync(() => {
    component.loginForm.controls['username'].setValue('user');
    component.loginForm.controls['password'].setValue('***');
    component.onSubmit();
    httpreq = httpMock.expectOne(baseUrl + '/api/login');
    httpreq.flush(fakeLogin);
    expect(component.loginForm.valid).toBeTruthy();

  }));

  // 0 status
  it('Internal server error login requests', waitForAsync(() => {
    component.loginForm.controls['username'].setValue('user');
    component.loginForm.controls['password'].setValue('***');
    component.onSubmit();
    httpreq = httpMock.expectOne(baseUrl + '/api/login');
    httpreq.error('');
    expect(component.error).toBe('Internal Server Error');
  }));


  // 404 status
  it('Unauthorized login requests', waitForAsync(() => {
    component.loginForm.controls['username'].setValue('user');
    component.loginForm.controls['password'].setValue('***');
    component.onSubmit();
    httpreq = httpMock.expectOne(baseUrl + '/api/login');
    httpreq.error(fakeInvalidLogin, fakeInvalidLogin);
    expect(component.error).toBe(fakeInvalidLogin.message);
  }));

  it("should redirect to profile if user email is blank", () => {
    sharedService.setCurrentUserDetails({ user_email: "" });
    sharedService.setCurrentUserDetails('projectsAccess', JSON.stringify(["abc"]));
    fixture.detectChanges();
    component.redirectToProfile();
    expect(component.redirectToProfile).toBeTruthy();
  });

  it("should redirect on profile for superadmin", () => {
    sharedService.setCurrentUserDetails({ user_email: "abc@gmail.com" });
    sharedService.setCurrentUserDetails({ 'projectsAccess': [] });
    sharedService.setCurrentUserDetails({ authorities: ['ROLE_SUPERADMIN'] });
    fixture.detectChanges();
    const respo = component.redirectToProfile();
    expect(respo).toBeFalsy();
  })

  it("should not redirect on profile if not superadmin", () => {
    sharedService.setCurrentUserDetails({ user_email: "abc@gmail.com" });
    sharedService.setCurrentUserDetails({ projectsAccess: undefined });
    sharedService.setCurrentUserDetails({ authorities: ['NOT_SUPERADMIN'] });
    fixture.detectChanges();
    component.redirectToProfile();
    expect(component.redirectToProfile).toBeTruthy();
  });

  it('should perform login successfully', () => {
    let data = {
      "headers": {
        "normalizedNames": {},
        "lazyUpdate": null,
        "lazyInit": null,
        "headers": {}
      },
      "status": 200,
      "statusText": "OK",
      "url": "https://domain-name/api/login",
      "ok": true,
      "type": 4,
      "body": {
        "user_email": "test@gmail.com",
        "projectsAccess": [],
        "user_name": "dummy_user",
        "X-Authentication-Token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJTVVBFUkFETUlOIiwiZGV0YWlscyI6IlNUQU5EQVJEIiwicm9sZXMiOlsiUk9MRV9TVVBFUkFETUlOIl0sImV4cCI6MTY4OTE0Nzc1OX0.y9uAnMjyNfsqeCxHXQb2zX6akOD_kAeM2AmmbHEyrPcAi7eKtS6yyChwtLQ_BsNM3u56ChdovxBXjNA2LjdLyQ",
        "authorities": [
          "ROLE_SUPERADMIN"
        ]
      }
    }
    const loginType = 'AD';
    localStorage.setItem('loginType', loginType);
    component.adLogin = true;
    spyOn(ga, 'setLoginMethod');
    sharedService.setCurrentUserDetails({ user_email: "abc@gmail.com" });
    sharedService.setCurrentUserDetails({ 'projectsAccess': [] });
    sharedService.setCurrentUserDetails({ authorities: ['ROLE_SUPERADMIN'] });
    const isRedirect = spyOn(component, 'redirectToProfile').and.returnValue(false);
    component.performLogin(data, 'dummy_user', 'dummy_password')
    expect(isRedirect).toHaveBeenCalled();
  })

  it('should navigate to profile if redirectToProfile returns true', () => {
    spyOn(component, 'redirectToProfile').and.returnValue(true);
    component.ngOnInit();
    expect(router.navigate).toHaveBeenCalledWith(['./dashboard/Config/Profile']);
  });

  it('should navigate to dashboard if no shared link is found', () => {
    spyOn(component, 'redirectToProfile').and.returnValue(false);
    spyOn(localStorage, 'getItem').and.returnValue(null);
    component.ngOnInit();
    expect(router.navigate).toHaveBeenCalledWith(['./dashboard/']);
  });

  it('should navigate to error page if stateFilters is invalid', () => {
    spyOn(component, 'redirectToProfile').and.returnValue(false);
    spyOn(localStorage, 'getItem').and.callFake((key) => {
      if (key === 'shared_link') return 'http://pageurl.com?stateFilters=invalid';
      return null;
    });
    component.ngOnInit();
    expect(router.navigate).toHaveBeenCalledWith(['/dashboard/Error']);
    expect(sharedService.raiseError).toHaveBeenCalledWith({
      status: 900,
      message: 'Invalid URL.'
    });
  });

  it('should navigate to error page if user has no project access', () => {
    spyOn(component, 'redirectToProfile').and.returnValue(false);
    spyOn(localStorage, 'getItem').and.callFake((key) => {
      if (key === 'shared_link') return 'http://pageurl.com?stateFilters=valid';
      if (key === 'currentUserDetails') return JSON.stringify({ projectsAccess: [{ projects: [] }] });
      return null;
    });
    spyOn(window, 'atob').and.returnValue(JSON.stringify({ primary_level: [{ basicProjectConfigId: '1' }] }));
    component.ngOnInit();
    expect(router.navigate).toHaveBeenCalledWith(['/dashboard/Error']);
    expect(sharedService.raiseError).toHaveBeenCalledWith({
      status: 901,
      message: 'No project access.'
    });
  });

  it('should navigate to the shared link if user has project access', () => {
    spyOn(component, 'redirectToProfile').and.returnValue(false);
    spyOn(localStorage, 'getItem').and.callFake((key) => {
      if (key === 'shared_link') return 'http://pageurl.com?stateFilters=valid';
      if (key === 'currentUserDetails') return JSON.stringify({ projectsAccess: [{ projects: [{ projectId: '1' }] }] });
      return null;
    });
    spyOn(window, 'atob').and.returnValue(JSON.stringify({ primary_level: [{ basicProjectConfigId: '1' }] }));
    component.ngOnInit();
    expect(router.navigate).toHaveBeenCalledWith([JSON.parse(JSON.stringify('http://pageurl.com?stateFilters=valid'))]);
  });
});
