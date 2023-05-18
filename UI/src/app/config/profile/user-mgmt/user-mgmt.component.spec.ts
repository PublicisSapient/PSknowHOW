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
import { HttpService } from '../../../services/http.service';

import { UserMgmtComponent } from './user-mgmt.component';
import { Routes } from '@angular/router';
import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MessageService } from 'primeng/api';
import { CommonModule } from '@angular/common';
import { InputSwitchModule } from 'primeng/inputswitch';
import { ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { APP_CONFIG, AppConfig } from '../../../services/app.config';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { environment } from 'src/environments/environment';
import { SharedService } from 'src/app/services/shared.service';

describe('UserManagementComponent', () => {
  let component: UserMgmtComponent;
  let fixture: ComponentFixture<UserMgmtComponent>;
  let httpService: HttpService;
  let httpMock;
  let httpreq;
  const baseUrl = environment.baseUrl;
  const successResponse = '{\"X-Authentication-Token\":\"dummytoken\",\"Success\":\"Success\"}';
  // const failure400Response = { 'timestamp': 1567511436517, 'status': 400, 'error': 'Bad request', 'message': 'Bad request Failed', 'path': '/api/changePassword' };
  const failureResponse = 'WrongOldPassword';
  beforeEach(waitForAsync(() => {
    const routes: Routes = [
      {
        path: 'dashboard/Config/Profile/UserSettings', component: UserMgmtComponent
      }
    ];

    TestBed.configureTestingModule({
      imports: [
        FormsModule,
        InputSwitchModule,
        ReactiveFormsModule,
        CommonModule,
        HttpClientTestingModule,
        RouterTestingModule.withRoutes(routes)
      ],
      declarations: [UserMgmtComponent],
      providers: [{ provide: APP_CONFIG, useValue: AppConfig },
        HttpService, MessageService,SharedService
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UserMgmtComponent);
    component = fixture.componentInstance;
    httpService = TestBed.get(HttpService);
    httpMock = TestBed.get(HttpTestingController);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('Test happy flow', () => {
    component.changePasswordForm.controls['oldpassword'].setValue('Test123@123');
    component.changePasswordForm.controls['password'].setValue('Qwerty@123');
    component.changePasswordForm.controls['confirmpassword'].setValue('Qwerty@123');
    component.onSubmit();
    fixture.detectChanges();
    httpMock.match(baseUrl + '/api/changePassword')[0].flush(successResponse);
    expect(component.changePasswordForm.valid).toBeTruthy();
    // expect(component.success).toBe('Password changed succesfully');
  });



  it('Test password and confirm password are same', () => {
    fixture.detectChanges();
    component.changePasswordForm.controls['oldpassword'].setValue('somepassword');
    component.changePasswordForm.controls['password'].setValue('Qwerty@123');
    component.changePasswordForm.controls['confirmpassword'].setValue('Qwerty@123');
    expect(component.checkPasswords(component.changePasswordForm)).toBeNull();
  });

  it('Test if new password is empty', () => {
    fixture.detectChanges();
    component.changePasswordForm.controls['oldpassword'].setValue('somepassword');
    component.changePasswordForm.controls['password'].setValue('');
    component.changePasswordForm.controls['confirmpassword'].setValue('');
    component.ngOnInit();
    component.checkPasswords(component.changePasswordForm);
    component.onSubmit();
    expect(component.changePasswordForm.valid).toBeFalsy();
    expect(component.success).toBe('');
  });

  it('Test if new password is not valid', () => {
    fixture.detectChanges();
    component.changePasswordForm.controls['oldpassword'].setValue('somepassword');
    component.changePasswordForm.controls['password'].setValue('abcd');
    component.changePasswordForm.controls['confirmpassword'].setValue('abcd');
    component.ngOnInit();
    component.checkPasswords(component.changePasswordForm);
    component.onSubmit();
    expect(component.changePasswordForm.valid).toBeFalsy();
    expect(component.success).toBe('');
  });

  it('Test if new password and confirm password dont match', () => {
    fixture.detectChanges();
    component.changePasswordForm.controls['oldpassword'].setValue('somepassword');
    component.changePasswordForm.controls['password'].setValue('Qwerty@123');
    component.changePasswordForm.controls['confirmpassword'].setValue('Qwerty@1234');
    expect(component.checkPasswords(component.changePasswordForm) != null).toBeTruthy();
  });

  it('Test if old password is empty', () => {
    fixture.detectChanges();
    component.changePasswordForm.controls['oldpassword'].setValue('');
    component.changePasswordForm.controls['password'].setValue('');
    component.changePasswordForm.controls['confirmpassword'].setValue('');
    component.ngOnInit();
    component.checkPasswords(component.changePasswordForm);
    component.onSubmit();
    expect(component.changePasswordForm.valid).toBeFalsy();
    expect(component.success).toBe('');
  });

  it('Test if confirm password is empty', () => {
    fixture.detectChanges();
    component.changePasswordForm.controls['oldpassword'].setValue('somepass');
    component.changePasswordForm.controls['password'].setValue('Qwerty@123');
    component.changePasswordForm.controls['confirmpassword'].setValue('');
    component.ngOnInit();
    component.onSubmit();
    expect(component.changePasswordForm.valid).toBeFalsy();
    expect(component.success).toBe('');
  });
});
