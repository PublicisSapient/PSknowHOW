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

import { DashboardconfigComponent } from './dashboard-config.component';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { CommonModule } from '@angular/common';
import { InputSwitchModule } from 'primeng/inputswitch';
import { ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { SharedService } from '../../services/shared.service';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HttpService } from '../../services/http.service';
import { environment } from 'src/environments/environment';
import { APP_CONFIG, AppConfig } from '../../services/app.config';
import { MessageService } from 'primeng/api';


describe('DashboardconfigComponent', () => {
  let component: DashboardconfigComponent;
  let fixture: ComponentFixture<DashboardconfigComponent>;
  const baseUrl = environment.baseUrl;
  let service;
  let httpMock;
  let httpService;
  let messageService;

  const fakeGetDashData = require('../../../test/resource/fakeShowHideApi.json');
  const fakeGetDashDataOthers = fakeGetDashData.data['others'][0].kpis;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        FormsModule,
        InputSwitchModule,
        ReactiveFormsModule,
        CommonModule,
        RouterTestingModule.withRoutes([]),
        HttpClientTestingModule
      ],
      declarations: [DashboardconfigComponent],
      providers: [SharedService, HttpService, MessageService
        , { provide: APP_CONFIG, useValue: AppConfig }

      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    service = TestBed.inject(SharedService);
    fixture = TestBed.createComponent(DashboardconfigComponent);
    component = fixture.componentInstance;
    httpService = TestBed.inject(HttpService);
    messageService = TestBed.inject(MessageService);
    httpMock = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('on load get dashboard data ', waitForAsync(() => {
    fixture.detectChanges();
    const httpreq = httpMock.expectOne(baseUrl + '/api/user-board-config');
    httpreq.flush(fakeGetDashData);
    expect(Object.keys(component.kpiFormValue).length).toBe(fakeGetDashDataOthers.length);
  }));

  it('on load get dashboard data with error  ', waitForAsync(() => {
    fixture.detectChanges();
    const httpreq = httpMock.expectOne(baseUrl + '/api/user-board-config');
    httpreq.flush(['error']);
  }));

  it('save dashboard config ', waitForAsync(() => {
    component.userName = "dummyName"
    fixture.detectChanges();
    const httpreq1 = httpMock.expectOne(baseUrl + '/api/user-board-config');
    httpreq1.flush(fakeGetDashData);
    fixture.detectChanges();
    component.save();
    const httpreq = httpMock.match(baseUrl + '/api/user-board-config');
    httpreq[0].flush(fakeGetDashData);
    expect(Object.keys(service.getDashConfigData()).length).toBe(Object.keys(fakeGetDashData.data).length);
  }));

  it('save dashboard config  with error', waitForAsync(() => {
    fixture.detectChanges();
    const httpreq1 = httpMock.expectOne(baseUrl + '/api/user-board-config');
    httpreq1.flush(fakeGetDashData);
    fixture.detectChanges();
    component.save();
    const httpreq = httpMock.match(baseUrl + '/api/user-board-config');
    httpreq[0].flush(['error']);

  }));
});



