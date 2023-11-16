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

import { ComponentFixture, TestBed, fakeAsync, inject, getTestBed, waitForAsync, tick } from '@angular/core/testing';
import { NavComponent } from './nav.component';
import { SharedService } from '../../services/shared.service';
import { HttpService } from '../../services/http.service';
import { HelperService } from 'src/app/services/helper.service';
import { MessageService } from 'primeng/api';
import { RouterTestingModule } from '@angular/router/testing';
import { Router } from '@angular/router';
import { LoginComponent } from '../../authentication/login/login.component';
import { APP_CONFIG, AppConfig } from '../../services/app.config';
import { CommonModule } from '@angular/common';
import { InputSwitchModule } from 'primeng/inputswitch';
import { ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { HttpTestingController, HttpClientTestingModule } from '@angular/common/http/testing';
import { environment } from 'src/environments/environment';
import { DatePipe } from '../../../../node_modules/@angular/common';
import { of } from 'rxjs';

describe('NavComponent', () => {
  let component: NavComponent;
  let   fixture: ComponentFixture<NavComponent>;
  let router;
  let httpMock;
  let httpService;
  let service;
  const baseUrl = environment.baseUrl;  // Servers Env

  const getversionData = { versionDetailsMap: { currentVersion: '2.8.0' } };

  const getLogo = { image: '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' };
  const getDashConfData = require('../../../test/resource/fakeShowHideApi.json');
  // let httpService;
  let messageService
  let shareService ;

  beforeEach((() => {
    router = {
      navigate: jasmine.createSpy('navigate')
    };
    
    TestBed.configureTestingModule({
      declarations: [NavComponent, LoginComponent],
      imports: [FormsModule,
        InputSwitchModule,
        ReactiveFormsModule,
        HttpClientTestingModule,
        CommonModule, RouterTestingModule.withRoutes([
          { path: 'authentication/login', component: LoginComponent }
        ])

      ],
      providers: [HttpService, SharedService, MessageService, HelperService, DatePipe,
        { provide: APP_CONFIG, useValue: AppConfig }
      ]
    })
      .compileComponents();
      httpService = TestBed.inject(HttpService);
      service = TestBed.get(SharedService);
      httpMock = TestBed.get(HttpTestingController);
      fixture = TestBed.createComponent(NavComponent);
      component = fixture.componentInstance;
      httpService = TestBed.inject(HttpService);
      messageService = TestBed.inject(MessageService);
      shareService = TestBed.inject(SharedService);
      spyOn(component,'startWorker').and.callFake(()=>{});
      fixture.detectChanges();
      router = TestBed.get(Router);
      // httpMock.expectOne(baseUrl + '/api/file/logo').flush(getLogo);
      // httpMock.expectOne(baseUrl + '/api/getversionmetadata').flush(getversionData);
      httpMock.expectOne(baseUrl + '/api/user-board-config/getConfig').flush(getDashConfData);
      document.dispatchEvent(new MouseEvent('click'));
  }));

  it('select tab functionality ', () => {
    component.selectTab('mydashboard');
    expect(service.getSelectedTab()).toBe('mydashboard');
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });


  it('should edit dashboard name', fakeAsync(() => {
    component.kpiListData = getDashConfData.data;
    component.kpiListData.scrum[0].boardName = 'My KnowHOW1';
    component.kpiListData.kanban[0].boardName = 'My KnowHOW1';
    spyOn(httpService, 'submitShowHideOnDashboard').and.returnValue(of(getDashConfData));
    component.editDashboardName();
    tick();
    expect(component.displayEditModal).toBe(false);
  }));

  it("should Edit dash board name and disabled model if successfully response came",()=>{
    const fakeRespose = {
      success : true
    }
    component.changedBoardName = "Updated Board name";
    spyOn(httpService,'submitShowHideOnDashboard').and.returnValue(of(fakeRespose))
    spyOn(messageService,'add');
    component.editDashboardName();
    expect(component.displayEditModal).toBeFalsy();
  })

  it("should notify if any error came while updating dashboard name",()=>{
    const fakeRespose = {
      success : false
    }
    component.changedBoardName = "Updated Board name";
    spyOn(httpService,'submitShowHideOnDashboard').and.returnValue(of(fakeRespose))
    const spy = spyOn(messageService,'add');
    component.editDashboardName();
    expect(spy).toHaveBeenCalled();
  })

  it("should open edit model",(done)=>{
    shareService.changedMainDashboardValueSub.next("updated board name");
    component.openEditModal();
    expect(component.displayEditModal).toBeTruthy();
    done();
  })

  it("should close edit model",()=>{
    shareService.changedMainDashboardValueSub.next("updated board name");
    component.closeEditModal();
    expect(component.displayEditModal).toBeFalsy();
  })

  

  it("should stop worker",()=>{
    component.worker = {terminate: ()=>{
      component.worker = undefined;
    }};
    component.stopWorker();
    expect(component.worker).toBe(undefined);
  })

});
