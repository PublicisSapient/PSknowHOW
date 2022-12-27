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
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { ToastModule } from 'primeng/toast';
import { TableModule } from 'primeng/table';
import { DropdownModule } from 'primeng/dropdown';
import { DialogModule } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HttpService } from '../../../services/http.service';
import { SharedService } from '../../../services/shared.service';
import { APP_CONFIG, AppConfig } from '../../../services/app.config';
import { ConfirmationService, MessageService } from 'primeng/api';
import { environment } from 'src/environments/environment';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { AccessMgmtComponent } from './access-mgmt.component';

describe('AccessMgmtComponent', () => {
  let component: AccessMgmtComponent;
  let fixture: ComponentFixture<AccessMgmtComponent>;
  let httpService: HttpService;
  let httpMock;
  let messageService;
  const baseUrl = environment.baseUrl;  // Servers Env

  const fakeUserData = require('../../../../test/resource/fakeUserData.json');
  const fakeRolesData = require('../../../../test/resource/fakeRolesData.json');
  const fakeServiceInputInvalid = require('../../../../test/resource/fakeUserDataChange.json');
  const fakeServiceInputValid = require('../../../../test/resource/fakeUserDataChangeValid.json');
  const fakeFilterByProjectData = require('../../../../test/resource/fakeFilterByProjectData.json');
  const fakeFilterByRoleData = require('../../../../test/resource/fakeFilterByRoleData.json');
  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [AccessMgmtComponent],
      imports: [
        ToastModule,
        TableModule,
        DropdownModule,
        DialogModule,
        ButtonModule,
        InputTextModule,
        FormsModule,
        RouterTestingModule.withRoutes([]),
        HttpClientTestingModule,
        BrowserAnimationsModule
      ],
      schemas: [
        CUSTOM_ELEMENTS_SCHEMA
      ],
      providers: [HttpService, MessageService, SharedService, ConfirmationService
        , { provide: APP_CONFIG, useValue: AppConfig }]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AccessMgmtComponent);
    component = fixture.componentInstance;
    httpService = TestBed.inject(HttpService);
    httpMock = TestBed.inject(HttpTestingController);
    messageService = TestBed.inject(MessageService);
    // fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should get user data on load', (done) => {
    component.ngOnInit();
    // fixture.detectChanges();
    httpMock.match(baseUrl + '/api/userinfo')[0].flush(fakeUserData);
    expect(Object.keys(component.users).length).toBe(Object.keys(fakeUserData.data).length);
    done();
  });

  /*it('should get project data on load', (done) => {
    component.ngOnInit();
    // fixture.detectChanges();
    httpMock.match(baseUrl + '/api/basicconfigs/all')[0].flush(fakeProjectData);
    expect(Object.keys(component.projects).length).toBe(Object.keys(fakeProjectData.data).length);
    done();
  });*/

  it('should get roles data on load', (done) => {
    component.ngOnInit();
    // fixture.detectChanges();
    httpMock.match(baseUrl + '/api/roles')[0].flush(fakeRolesData);
    if (component.rolesData.success) {
      fakeRolesData.data = fakeRolesData.data.map((role) => ({
          label: role.roleName,
          value: role.roleName
        }));
      expect(component.roleList).toEqual(fakeRolesData.data);
    } else {
      // component.messageService.add({ severity: 'error', summary: 'Error in fetching roles. Please try after some time.' });
    }
    done();
  });

  it('should display duplicate popup on invalid configuration', (done) => {
    component.saveAccessChange(fakeServiceInputInvalid);
    // fixture.detectChanges();
    expect(component.displayDuplicateProject).toBeTruthy();
    done();
  });

  it('should update user access', (done) => {
    component.saveAccessChange(fakeServiceInputValid);
    // fixture.detectChanges();
    expect(component.displayDuplicateProject).toBeFalsy();
    httpMock.expectOne(baseUrl + '/api/userinfo/' + fakeServiceInputValid.username);
    done();
  });

  it('should filter by project', ()=>{
    component.searchProject = 'KnowHOW';
    component.filterByProject();
    component.users = fakeFilterByProjectData;
    expect(component.users[0].projectsAccess[0].accessNodes[0].accessItems[0].itemName).toContain('KnowHOW');
  });

  it('should filter by role', ()=>{
    component.searchRole = 'ROLE_SUPERADMIN';
    component.filterByRole();
    component.users = fakeFilterByRoleData;
    expect(component.users[0].projectsAccess[0].role).toContain('ROLE_SUPERADMIN');
  });
});
