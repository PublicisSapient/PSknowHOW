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

import { ComponentFixture, TestBed, fakeAsync, tick, waitForAsync } from '@angular/core/testing';
import { ToastModule } from 'primeng/toast';
import { InputSwitchModule } from 'primeng/inputswitch';
import { FormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HttpService } from '../../../services/http.service';
import { SharedService } from '../../../services/shared.service';
import { APP_CONFIG, AppConfig } from '../../../services/app.config';
import { MessageService } from 'primeng/api';
import { ConfirmationService } from 'primeng/api';
import { environment } from 'src/environments/environment';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RequestStatusComponent } from './request-status.component';
import { TableModule } from 'primeng/table';
import { of } from 'rxjs';

describe('RequestStatusComponent', () => {
  let component: RequestStatusComponent;
  let fixture: ComponentFixture<RequestStatusComponent>;
  let httpService: HttpService;
  let httpMock;
  let messageService;
  let sharedService;
  let confirmationService;
  const baseUrl = environment.baseUrl;

  const fakeRequestsData = {
    message: 'Found access_requests under username testUser',
    success: true,
    data: [{
      _id: '5da46ff3e645ca33dc927b83',
      username: 'testUser',
      status: 'Pending',
      reviewComments: '',
      projects: [{
        projectName: 'Test1',
        projectId: 'Test1_68500_Test1'
      }],
      roles: [{
        _id: '5da46000e645ca33dc927b4a',
        roleName: 'ROLE_PROJECT_VIEWER'
      }]
    }, {
      _id: '5da47bdde645ca33dc927ba8',
      username: 'testUser',
      status: 'Pending',
      reviewComments: '',
      projects: [{
        projectName: 'Test1',
        projectId: 'Test1_68500_Test1'
      }],
      roles: [{
        _id: '5da03f242afa421ae416cad7',
        roleName: 'ROLE_PROJECT_VIEWER'
      }]
    }, {
      _id: '5da47c2ae645ca33dc927bb3',
      username: 'testUser',
      status: 'Pending',
      reviewComments: '',
      projects: [{
        projectName: 'Test2',
        projectId: 'Test2_63102_Test2'
      }],
      roles: [{
        _id: '5da03f242afa421ae416cad7',
        roleName: 'ROLE_PROJECT_VIEWER'
      }]
    }]
  };

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [RequestStatusComponent],
      imports: [
        InputSwitchModule,
        FormsModule,
        RouterTestingModule.withRoutes([]),
        HttpClientTestingModule,
        BrowserAnimationsModule,
        TableModule,
        ToastModule
      ],
      providers: [HttpService, MessageService, ConfirmationService, SharedService
        , { provide: APP_CONFIG, useValue: AppConfig }]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RequestStatusComponent);
    component = fixture.componentInstance;
    httpService = TestBed.get(HttpService);
    httpMock = TestBed.get(HttpTestingController);
    messageService = TestBed.get(MessageService);
    sharedService= TestBed.inject(SharedService);
    confirmationService = TestBed.inject(ConfirmationService);
    fixture.detectChanges();
  });

  afterEach(() => {
    // destroy the component to cancel the timer again
    fixture.destroy();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load pending requests on load', () => {
    component.userName = 'testUser';
    const fakeUserDetails = {user_name: 'testUser'}
    localStorage.setItem('currentUserDetails',JSON.stringify(fakeUserDetails))
    fixture.detectChanges();
    component.ngOnInit();
    httpMock.match(baseUrl + '/api/accessrequests/user/' + component.userName)[0].flush(fakeRequestsData);
    if (component.requestStatusData['success']) {
      expect(Object.keys(component.requestStatusList).length).toEqual(Object.keys(fakeRequestsData.data).length);
    }
  });

  it('should give error on getting pending requests', () => {
    component.dataLoading = true;
    component.userName = 'testUser';
    const errorResponse = {
      message: 'Error in fetching requests. Please try after some time.',
      success: false,
      data: []
    }
    component.requestStatusList = [];
    spyOn(httpService, 'getUserAccessRequests').and.returnValue(of(errorResponse))
    const spy = spyOn(messageService, 'add');
    component.getRequests();
    expect(Object.keys(component.requestStatusList).length).toEqual(0);
    expect(spy).toHaveBeenCalled();
  });  

  it('should delete request', () => {
    const requestId = '6063052eab1d4700013e5aff';
    const fakeEvent = { isTrusted: true };
    const recallResponse = { message: 'Sucessfully deleted.', success: true, data: '6063052eab1d4700013e5aff' };
    
    spyOn<any>(confirmationService, 'confirm').and.callFake((params: any) => {
      params.accept();
      httpMock.expectOne(baseUrl + '/api/accessrequests/6063052eab1d4700013e5aff').flush(recallResponse);
    });
    component.recallRequest(requestId, fakeEvent);
    fixture.detectChanges();
  });

  it('should give error on deleting request', fakeAsync(() => {
    component.dataLoading = true;
    const requestId = '6063052eab1d4700013e5aff';
    const fakeEvent = { isTrusted: true };
    const recallResponse = { message: 'Error in recalling request. Please try after some time.', success: false };
    const spy = spyOn(messageService, 'add');
    spyOn<any>(confirmationService, 'confirm').and.callFake((params: any) => {
      params.accept();
      httpMock.expectOne(baseUrl + '/api/accessrequests/6063052eab1d4700013e5aff').flush(recallResponse);
    });
    component.recallRequest(requestId, fakeEvent);
    tick();
    expect(spy).toHaveBeenCalled();
  }))
});