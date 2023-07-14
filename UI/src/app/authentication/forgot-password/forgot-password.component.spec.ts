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
import { ForgotPasswordComponent } from './forgot-password.component';
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
import { Router } from '@angular/router';
import { SharedService } from 'src/app/services/shared.service';

describe('ForgotPasswordComponent', () => {
  let component: ForgotPasswordComponent;
  let fixture: ComponentFixture<ForgotPasswordComponent>;
  const baseUrl = environment.baseUrl;
  let httpMock;
  let httpService;
  let sharedService;
  const fakeError = { message: 'logError', success: false };
  const fakeSuccess = { message: 'success', success: true };
  const fakeHttp0 = { status : 0};

  beforeEach(waitForAsync(() => {
    const routes: Routes = [
      { path: 'forget', component: ForgotPasswordComponent },
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
      declarations: [ForgotPasswordComponent,
        RegisterComponent, DashboardComponent],
      providers: [HttpService,SharedService
        , { provide: APP_CONFIG, useValue: AppConfig }

      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ForgotPasswordComponent);
    component = fixture.componentInstance;
    httpService = TestBed.get(HttpService);
    sharedService = TestBed.get(SharedService);
    httpMock = TestBed.get(HttpTestingController);
    fixture.detectChanges();
  });
  it('should create', () => {
    expect(component).toBeTruthy();
  });


  it('Invalid email id  should not call service', waitForAsync(() => {
    component.emailForm.controls['email'].setValue('user');
    component.onSubmit();
    expect(component.emailForm.invalid).toBeTruthy();
  }));

  it('valid email id  with error', waitForAsync(() => {
    component.emailForm.controls['email'].setValue('user@gmail.com');
    component.onSubmit();
    const httpreq = httpMock.expectOne(baseUrl + '/api/forgotPassword');
    httpreq.flush(fakeError);
    expect(component.error).toBe('Link could not be sent to the entered email id. Please check if the email id is valid and registered.');
  }));

  it('valid email id  with success', waitForAsync(() => {
    component.emailForm.controls['email'].setValue('user@gmail.com');
    component.onSubmit();
    const httpreq = httpMock.expectOne(baseUrl + '/api/forgotPassword');
    httpreq.flush(fakeSuccess);
    expect(component.success).toBe('Link to reset password has been sent to your registered email address');
  }));

});
