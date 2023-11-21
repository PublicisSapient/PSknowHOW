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
import { DropdownModule } from 'primeng/dropdown';
import { GetAuthorizationService } from 'src/app/services/get-authorization.service';


describe('DashboardconfigComponent', () => {
  let component: DashboardconfigComponent;
  let fixture: ComponentFixture<DashboardconfigComponent>;
  const baseUrl = environment.baseUrl;
  let service;
  let httpMock;
  let httpService;
  let messageService;
  let getAuthorizationService;

  const fakeGetDashData = require('../../../test/resource/fakeShowHideApi.json');
  let fakeGetDashDataOthers = fakeGetDashData.data['scrum'][0].kpis.concat(fakeGetDashData.data['kanban'][0].kpis).concat(fakeGetDashData.data['others'][0].kpis);
  // let filteredFakeGetDashDataScrum = fakeGetDashDataOthers.filter((obj, index) => {
  //   return index === fakeGetDashDataOthers.findIndex(o => obj.kpiId === o.kpiId);
  // });
  fakeGetDashDataOthers = fakeGetDashDataOthers.filter((kpi) => kpi.shown);
  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        FormsModule,
        InputSwitchModule,
        ReactiveFormsModule,
        CommonModule,
        RouterTestingModule.withRoutes([]),
        HttpClientTestingModule,
        DropdownModule
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
   getAuthorizationService = TestBed.inject(GetAuthorizationService);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should get dashboard data ', waitForAsync(() => {
    component.getKpisData('pid');
    const httpreq = httpMock.expectOne(baseUrl + '/api/user-board-config/pid');
    component.userProjects = [{name : "p1",id:'id1',type : 'common'}]
    httpreq.flush(fakeGetDashData);
    expect(Object.keys(component.kpiFormValue['kpis']['controls']).length + 4).toBe(fakeGetDashDataOthers.length);
  }));

  it('should get dashboard data with error  ', waitForAsync(() => {
    component.getKpisData('pid');
    const httpreq = httpMock.expectOne(baseUrl + '/api/user-board-config/pid');
    httpreq.flush(['error']);
  }));

  it('should update dashboard config ', waitForAsync(() => {
    component.userName = "dummyName"
    component.selectedProject = {
      name : "fakeName",
      id :'fakeid'
    }
    component.kpiData = fakeGetDashData['data']['scrum'];
    component.kpiToBeHidden = [{
        "kpiId": "kpi121",
        "kpiName": "Defect Count by Status",
        "isEnabled": true,
        "order": 1,
        "kpiDetail": {
          "kpiInfo": {
            "definition": "Defect count by Status shows the breakup of all defects within an iteration by Status. The pie chart representation gives the count of defects in each Status"
          },
          "kpiFilter": "",
          "trendCalculative": false,
          "xaxisLabel": "",
          "yaxisLabel": "",
          "additionalFilterSupport": false
        },
        "shown": true
    }]
    component.kpiListData = fakeGetDashData['data'];
    component.updateData();
    const httpreq1 = httpMock.expectOne(baseUrl + '/api/user-board-config/saveAdmin/fakeid');
    httpreq1.flush(fakeGetDashData);
  }));


  it('should fetch all user projects', () => {
    const getProjectsResponse = { message: 'Fetched successfully', success: true, data: [{ id: '601bca9569515b0001d68182', projectName: 'TestRIshabh', createdAt: '2021-02-04T10:21:09', isKanban: false }] };
    component.getProjects();
    fixture.detectChanges();
    httpMock.match(baseUrl + '/api/basicconfigs')[0].flush(getProjectsResponse);
  });

  it('should allow user to select project', () => {
    const selectedProjects = { originalEvent: { isTrusted: true }, value: {id: '601bca9569515b0001d68182', name: 'test'}, itemValue: '601bca9569515b0001d68182' };
    const processorName = 'Jira';
    component.updateProjectSelection(selectedProjects);
    fixture.detectChanges();
    expect(component.selectedProject).toEqual({id: '601bca9569515b0001d68182', name: 'test'});
  });

  it('should reset form when tab changes',()=>{
    
    component.tabHeaders = ['scrum','kanban'];
    const spy = spyOn(component,'setFormControlData');
    component.handleTabChange({
      event : {
        index : 1
      }
    });
    expect(spy).toHaveBeenCalled();
  })
});



