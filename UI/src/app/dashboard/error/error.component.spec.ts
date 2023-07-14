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

import { ComponentFixture, TestBed, fakeAsync, inject, getTestBed, waitForAsync } from '@angular/core/testing';
import { ErrorComponent } from './error.component';
import { SharedService } from '../../services/shared.service';
import { HttpService } from '../../services/http.service';
import { APP_CONFIG, AppConfig } from '../../services/app.config';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { FormsModule } from '@angular/forms';


describe('ErrorComponent', () => {
  let component: ErrorComponent;
  let fixture: ComponentFixture<ErrorComponent>;
  let sharedService: SharedService;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ErrorComponent],
      imports: [
        FormsModule,
        CommonModule,
        RouterTestingModule

      ],
      providers: [HttpService, SharedService,
        { provide: APP_CONFIG, useValue: AppConfig }
      ]
    })
      .compileComponents();
  }));



  beforeEach(() => {
    fixture = TestBed.createComponent(ErrorComponent);
    component = fixture.componentInstance;
    sharedService = TestBed.get(SharedService);
    fixture.detectChanges();

  });

  const error0 = { status: 0, message: 'Internal Server Error' };
  const error401 = { status: 401, message: 'Session Expired' };
  const error403 = { status: 403, message: 'Unauthorised' };
  const error404 = { status: 404, message: 'Not found' };
  const error500 = { status: 500, message: 'Internal Server Error' };
  const error405 = { status: 405, message: 'Method not allowed' };

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display proper error message on connection refused error', () => {
    sharedService.raiseError(error0);
    spyOn(sharedService.passErrorToErrorPage, 'emit');
    component.ngOnInit();
    fixture.detectChanges();
    expect(component.errorMsg).toEqual('Server not available');
  });

  it('should display proper error message on session expired error', () => {
    sharedService.raiseError(error401);
    spyOn(sharedService.passErrorToErrorPage, 'emit');
    component.ngOnInit();
    fixture.detectChanges();
    expect(component.errorMsg).toEqual('Session Expired');
  });

  it('should display proper error message on Unauthorised error', () => {
    sharedService.raiseError(error403);
    spyOn(sharedService.passErrorToErrorPage, 'emit');
    component.ngOnInit();
    fixture.detectChanges();
    expect(component.errorMsg).toEqual('Unauthorised action');
  });

  it('should display proper error message on 404 error', () => {
    sharedService.raiseError(error404);
    spyOn(sharedService.passErrorToErrorPage, 'emit');
    component.ngOnInit();
    fixture.detectChanges();
    expect(component.errorMsg).toEqual('API Not Found');
  });

  it('should display proper error message on 500 error', () => {
    sharedService.raiseError(error500);
    spyOn(sharedService.passErrorToErrorPage, 'emit');
    component.ngOnInit();
    fixture.detectChanges();
    expect(component.errorMsg).toEqual('Internal Server error');
  });

  it('should display proper error message on any error', () => {
    sharedService.raiseError(error405);
    spyOn(sharedService.passErrorToErrorPage, 'emit');
    component.ngOnInit();
    fixture.detectChanges();
    expect(component.errorMsg).toEqual('Some error occurred');
  });
});
