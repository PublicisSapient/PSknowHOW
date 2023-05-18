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

import { ResetPasswordComponent } from './reset-password.component';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { CommonModule } from '@angular/common';
import { InputSwitchModule } from 'primeng/inputswitch';
import { ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { RegisterComponent } from '../register/register.component';
import { RouterTestingModule } from '@angular/router/testing';
import { Routes } from '@angular/router';
import { DashboardComponent } from '../../dashboard/dashboard.component';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HttpService } from '../../services/http.service';
import { environment } from 'src/environments/environment';
import { APP_CONFIG, AppConfig } from '../../services/app.config';
import { SharedService } from 'src/app/services/shared.service';

describe('ResetPasswordComponent', () => {
  let component: ResetPasswordComponent;
  let fixture: ComponentFixture<ResetPasswordComponent>;
  const baseUrl = environment.baseUrl;
  let httpMock;
  let httpService;
  let sharedService;
  const fakeError = { message: 'logError', success: false };
  const fakeSuccess = { message: 'success', success: true };

  beforeEach(waitForAsync(() => {
    const routes: Routes = [
      { path: 'forget', component: ResetPasswordComponent },
    ];

    TestBed.configureTestingModule({
      imports: [
        FormsModule,
        InputSwitchModule,
        ReactiveFormsModule,
        CommonModule,
        RouterTestingModule.withRoutes(routes),
        HttpClientTestingModule
      ],
      declarations: [ResetPasswordComponent,
        RegisterComponent, DashboardComponent],
      providers: [HttpService, SharedService
        , { provide: APP_CONFIG, useValue: AppConfig }

      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ResetPasswordComponent);
    component = fixture.componentInstance;
    httpService = TestBed.get(HttpService);
    sharedService = TestBed.inject(SharedService);
    httpMock = TestBed.get(HttpTestingController);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('get Reset Form control', () => {
    console.log(component.f);
    expect(Object.keys(component.f)).toContain('password');
  });

  it('password and confirm pass word miss match should not call service', waitForAsync(() => {
    component.resetPasswordForm.controls['confirmpassword'].setValue('User@123');
    component.resetPasswordForm.controls['password'].setValue('User@1234');

    component.onSubmit();
    expect(component.resetPasswordForm.invalid).toBeTruthy();
  }));



  it('valid epassword and confirm pass word  with error', waitForAsync(() => {
    component.resetPasswordForm.controls['confirmpassword'].setValue('User@1234');
    component.resetPasswordForm.controls['password'].setValue('User@1234');
    component.onSubmit();
    const httpreq = httpMock.expectOne(baseUrl + '/api/resetPassword');
    httpreq.flush(fakeError);
    expect(component.error).toBe(fakeError.message);
  }));
  it('valid email id  with success', waitForAsync(() => {
    component.resetPasswordForm.controls['confirmpassword'].setValue('User@1234');
    component.resetPasswordForm.controls['password'].setValue('User@1234');
    component.onSubmit();
    const httpreq = httpMock.expectOne(baseUrl + '/api/resetPassword');
    httpreq.flush(fakeSuccess);
    expect(component.message).toBe('Password successfully saved');
  }));
});
