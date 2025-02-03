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
import { SharedService } from '../../services/shared.service';
import { GoogleAnalyticsService } from 'src/app/services/google-analytics.service';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let router: jasmine.SpyObj<Router>;
  let httpService: jasmine.SpyObj<HttpService>;
  let sharedService: jasmine.SpyObj<SharedService>;
  let gaService: jasmine.SpyObj<GoogleAnalyticsService>;

  beforeEach(async () => {
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    const httpSpy = jasmine.createSpyObj('HttpService', ['login']);
    const sharedSpy = jasmine.createSpyObj('SharedService', ['getCurrentUserDetails', 'raiseError']);
    const gaSpy = jasmine.createSpyObj('GoogleAnalyticsService', ['setLoginMethod']);

    await TestBed.configureTestingModule({
      declarations: [LoginComponent],
      imports: [ReactiveFormsModule],
      providers: [
        { provide: Router, useValue: routerSpy },
        { provide: HttpService, useValue: httpSpy },
        { provide: SharedService, useValue: sharedSpy },
        { provide: GoogleAnalyticsService, useValue: gaSpy },
        {
          provide: ActivatedRoute,
          useValue: { queryParams: of({ sessionExpire: 'true' }), snapshot: { queryParams: {} } }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    httpService = TestBed.inject(HttpService) as jasmine.SpyObj<HttpService>;
    sharedService = TestBed.inject(SharedService) as jasmine.SpyObj<SharedService>;
    gaService = TestBed.inject(GoogleAnalyticsService) as jasmine.SpyObj<GoogleAnalyticsService>;

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
    component.onSubmit();
    expect(component.submitted).toBeTrue();
    expect(httpService.login).not.toHaveBeenCalled();
  });

  it('should call login service on valid form submission', () => {
    component.loginForm.setValue({ username: 'test', password: 'password' });
    httpService.login.and.returnValue(of({ status: 200 }));

    component.onSubmit();

    expect(httpService.login).toHaveBeenCalledWith('', 'test', 'password');
  });

  it('should handle 401 error on login', () => {
    component.loginForm.setValue({ username: 'test', password: 'password' });
    httpService.login.and.returnValue(of({ status: 401, error: { message: 'Unauthorized' } }));

    component.onSubmit();

    expect(component.error).toBe('Unauthorized');
    expect(component.f.password.value).toBe('');
  });

  it('should redirect to profile if conditions are met', () => {
    component.loginForm.setValue({ username: 'test', password: 'password' });
    httpService.login.and.returnValue(of({ status: 200, body: {} }));

    sharedService.getCurrentUserDetails.and.returnValue('');

    component.onSubmit();

    expect(router.navigate).toHaveBeenCalledWith(['./dashboard/Config/Profile']);
  });

  it('should redirect to dashboard on successful login', () => {
    component.loginForm.setValue({ username: 'test', password: 'password' });
    httpService.login.and.returnValue(of({ status: 200, body: {} }));

    sharedService.getCurrentUserDetails.and.returnValue('someUserDetails');

    component.onSubmit();

    expect(router.navigate).toHaveBeenCalledWith(['./dashboard/']);
  });

 it('should handle 401 error and reset password', () => {
  const data = { status: 401, error: { message: 'Unauthorized' } };
  
  // Spy on the setValue method
  spyOn(component.f.password, 'setValue');

  component.performLogin(data, 'testUser', 'testPass');

  expect(component.error).toBe('Unauthorized');
  expect(component.f.password.setValue).toHaveBeenCalledWith('');
  expect(component.submitted).toBeFalse();
});


  it('should handle server error with status 0', () => {
    const data = { status: 0 };
    component.performLogin(data, 'testUser', 'testPass');
    expect(component.error).toBe('Internal Server Error');
  });

  it('should redirect to profile if redirectToProfile returns true', () => {
    spyOn(component, 'redirectToProfile').and.returnValue(true);
    const data = { status: 200, body: {} };
    component.performLogin(data, 'testUser', 'testPass');
    expect(router.navigate).toHaveBeenCalledWith(['./dashboard/Config/Profile']);
  });

  it('should redirect to dashboard if no shared link exists', () => {
    spyOn(component, 'redirectToProfile').and.returnValue(false);
    const data = { status: 200, body: {} };
    localStorage.removeItem('shared_link');
    component.performLogin(data, 'testUser', 'testPass');
    expect(router.navigate).toHaveBeenCalledWith(['./dashboard/']);
  });

 it('should handle shared link and navigate accordingly', () => {
  spyOn(component, 'redirectToProfile').and.returnValue(false);
  const data = { status: 200, body: {} };

  // Create a valid Base64-encoded string
  const validBase64 = btoa(JSON.stringify({ parent_level: { basicProjectConfigId: '123' } }));
  const mockUrl = `./dashboard/somePath?stateFilters=${validBase64}`;
  
  localStorage.setItem('shared_link', mockUrl);
  sharedService.getCurrentUserDetails.and.returnValue(['ROLE_USER']);
  
  component.performLogin(data, 'testUser', 'testPass');
  
  expect(router.navigate).toHaveBeenCalledWith(['/dashboard/Error']);
});

});
