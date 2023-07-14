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
import { ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { LoginComponent } from '../login/login.component';
import { RegisterComponent } from '../register/register.component';
import { Routes } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { APP_CONFIG, AppConfig } from '../../services/app.config';
import { DashboardComponent } from '../../dashboard/dashboard.component';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { HttpService } from '../../services/http.service';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { environment } from 'src/environments/environment';
import { throwError, of } from 'rxjs';
import { SharedService } from 'src/app/services/shared.service';

describe('RegisterComponent', () => {
  let component: RegisterComponent;
  let fixture: ComponentFixture<RegisterComponent>;
  const fakeRegister = JSON.stringify({ 'X-Authentication-Token': 'dummytokenstring', Success: 'Success' });
  let httpMock;
  let httpreq;
  let httpService;
  let sharedService;
  // const errorMsz = 'Cannot complete the registration process, Try with different email';
  const errorMsz = {
    message:'Cannot complete the registration process, Try with different email',
    success:false
  };
  const baseUrl = environment.baseUrl;

  beforeEach(waitForAsync(() => {


    const routes: Routes = [
      {
        path: 'dashboard', component: DashboardComponent
      },
      { path: 'authentication/register', component: RegisterComponent }
    ];

    TestBed.configureTestingModule({
      imports: [
        FormsModule,
        InputSwitchModule,
        ReactiveFormsModule,
        CommonModule,
        RouterTestingModule.withRoutes(routes),
        HttpClientTestingModule],
      declarations: [
        LoginComponent,
        RegisterComponent, DashboardComponent],
      providers: [{ provide: APP_CONFIG, useValue: AppConfig }, HttpService, SharedService],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RegisterComponent);
    component = fixture.componentInstance;
    httpService = TestBed.get(HttpService);
    httpMock = TestBed.get(HttpTestingController);
    sharedService = TestBed.inject(SharedService);
    fixture.detectChanges();
  });

  // 422 status
  /*it('check  if user already exist ', async(() => {
    component.registorForm.controls['username'].setValue('fakeuser');
    component.registorForm.controls['password'].setValue('Fake@123');
    component.registorForm.controls['confirmpassword'].setValue('Fake@123');
    component.registorForm.controls['email'].setValue('fake@gmail.com');
    component.onSubmit();
    const emsg = 'deliberate 422 error';
    httpreq = httpMock.expectOne(baseUrl + '/api/registerUser');
    httpreq.flush(emsg, { status: 422, statusText: 'Not Found' });
    expect(component.error).toBe(errorMsz);
  }));*/

  // error email already registered
  it('email already registered ', waitForAsync(() => {
    component.registorForm.controls['username'].setValue('fakeuser');
    component.registorForm.controls['password'].setValue('Fake@123');
    component.registorForm.controls['confirmpassword'].setValue('Fake@123');
    component.registorForm.controls['email'].setValue('fake@gmail.com');
    component.onSubmit();
    httpreq = httpMock.expectOne(baseUrl + '/api/registerUser');
    httpreq.flush(errorMsz);
    expect(component.error).toBe(errorMsz.message);
  }));


  it('invalid form check', waitForAsync(() => {
    component.registorForm.controls['username'].setValue('');
    component.registorForm.controls['password'].setValue('');
    component.registorForm.controls['confirmpassword'].setValue('');
    component.registorForm.controls['email'].setValue('');
    fixture.detectChanges();
    expect(component.registorForm.valid).toBeFalsy();
  }));

  it('invalid form should not call register', waitForAsync(() => {
    component.registorForm.controls['username'].setValue('');
    component.registorForm.controls['password'].setValue('');
    component.registorForm.controls['confirmpassword'].setValue('');
    fixture.detectChanges();
    component.onSubmit();
    expect(component.registorForm.valid).toBeFalsy();
  }));
  it('valid form should  call register ', waitForAsync(() => {
    component.registorForm.controls['username'].setValue('fakeuser');
    component.registorForm.controls['password'].setValue('Fake@123');
    component.registorForm.controls['confirmpassword'].setValue('Fake@123');
    component.registorForm.controls['email'].setValue('fake@gmail.com');
    component.onSubmit();
    httpreq = httpMock.expectOne(baseUrl + '/api/registerUser');
    httpreq.flush(fakeRegister);
  }));
  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it("should come success response on register",()=>{
    component.registorForm.controls['username'].setValue('fakeuser');
    component.registorForm.controls['password'].setValue('Fake@123');
    component.registorForm.controls['confirmpassword'].setValue('Fake@123');
    component.registorForm.controls['email'].setValue('fake@gmail.com');
    const fakeRegister = {
      success: true
    }
    spyOn(httpService,'register').and.returnValue(of(fakeRegister))
    component.onSubmit();
    expect(component.success).not.toBeNull();
  })

  it("should come failure response on register",()=>{
    component.registorForm.controls['username'].setValue('fakeuser');
    component.registorForm.controls['password'].setValue('Fake@123');
    component.registorForm.controls['confirmpassword'].setValue('Fake@123');
    component.registorForm.controls['email'].setValue('fake@gmail.com');
    const fakeRegister = {
      success: false
    }
    spyOn(httpService,'register').and.returnValue(of(fakeRegister))
    component.onSubmit();
    expect(component.success).not.toBeNull();
  })

  it("should loading false on register error",()=>{
    component.registorForm.controls['username'].setValue('fakeuser');
    component.registorForm.controls['password'].setValue('Fake@123');
    component.registorForm.controls['confirmpassword'].setValue('Fake@123');
    component.registorForm.controls['email'].setValue('fake@gmail.com');
    const fakeRegister = {
      success: false
    }
    spyOn(httpService,'register').and.returnValue(throwError(() => new Error()))
    component.onSubmit();
    expect(component.loading).toBeFalsy();
  })
});
