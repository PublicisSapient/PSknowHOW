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
import { DashboardComponent } from './dashboard.component';
import { RouterTestingModule } from '@angular/router/testing';
import { SharedService } from '../services/shared.service';
import { GetAuthService } from '../services/getauth.service';
import { HttpClientModule } from '@angular/common/http';
import { APP_CONFIG, AppConfig } from '../services/app.config';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpService } from '../services/http.service';

describe('DashboardComponent', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;
  let getAuth: GetAuthService;
  let httpService: HttpService;
  let sharedService: SharedService;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [DashboardComponent],
      imports: [RouterTestingModule, HttpClientModule, BrowserAnimationsModule],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],

      providers: [
        SharedService,
        GetAuthService,
        HttpService,
        { provide: APP_CONFIG, useValue: AppConfig },
      ],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
    getAuth = TestBed.get(GetAuthService);
    httpService = TestBed.inject(HttpService);
    sharedService = TestBed.inject(SharedService);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should set page content wrapper height on initialization', () => {
    // Arrange
    spyOn(component, 'setPageContentWrapperHeight');

    // Act
    component.ngOnInit();

    // Assert
    expect(component.setPageContentWrapperHeight).toHaveBeenCalled();
  });

  it('should update side nav style when isSideNav flag changes', () => {
    // Arrange
    const flag = true;
    spyOn(sharedService.isSideNav, 'subscribe').and.callFake((callback) => {
      callback(flag);
    });

    // Act
    component.ngOnInit();

    // Assert
    expect(component.isApply).toBe(flag);
    expect(component.sideNavStyle).toEqual({ toggled: component.isApply });
  });

  it('should set modal details for created project', () => {
    const projectName = 'Test Project';
    httpService.createdProjectName = projectName;
    spyOn(httpService.loadApp, 'subscribe').and.callThrough();
    component.ngOnInit();
    expect(component.modalDetails.header).toBe('Project Created');
    expect(component.modalDetails.content).toBe(
      `The project "${projectName}" has been created successfully and you have gained admin rights for it.`,
    );
  });

  it('should reload app when reloadApp is called', () => {
    spyOn(httpService.loadApp, 'subscribe').and.callThrough();
    component.ngOnInit();
    component.reloadApp();
    expect(component.displayModal).toBe(false);
  });
});
