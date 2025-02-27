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
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DashboardV2Component } from './dashboard-v2.component';
import { RouterTestingModule } from '@angular/router/testing';
import { SharedService } from '../../services/shared.service';
import { GetAuthService } from '../../services/getauth.service';
import { HttpClientModule } from '@angular/common/http';
import { APP_CONFIG, AppConfig } from '../../services/app.config';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpService } from '../../services/http.service';
import { CommonModule, DatePipe } from '@angular/common';
import { of, Subject } from 'rxjs';

describe('DashboardV2Component', () => {
  let component: DashboardV2Component;
  let fixture: ComponentFixture<DashboardV2Component>;
  let getAuth: GetAuthService;
  let sharedService: SharedService;
  let mockHttpService: jasmine.SpyObj<HttpService>;

  beforeEach(async () => {
    mockHttpService = jasmine.createSpyObj('HttpService', ['getAllProjects']);

    await TestBed.configureTestingModule({
      declarations: [DashboardV2Component],
      imports: [RouterTestingModule, HttpClientModule, BrowserAnimationsModule],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
      providers: [
        SharedService, 
        GetAuthService, 
        { provide: HttpService, useValue: mockHttpService }, 
        CommonModule, 
        DatePipe,
        { provide: APP_CONFIG, useValue: AppConfig }
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    mockHttpService.getAllProjects.and.returnValue(of({
      message: "Fetched successfully",
      success: true,
      data: [
        {
          id: "66b5c0401bfcdd465298bff9",
          projectName: "Digital Pharmacy",
          hierarchy: [{ hierarchyLevelName: "BU", value: "North America" }]
        }
      ]
    }));

    fixture = TestBed.createComponent(DashboardV2Component);
    component = fixture.componentInstance;
    sharedService = TestBed.inject(SharedService);

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should update selectedTab when onTabSwitch emits data with selectedBoard', () => {

     sharedService.onTabSwitch.next({ selectedBoard: 'iteration' });

     expect(component.selectedTab).toBe('iteration');


  });

  it('should not store data in localStorage when API returns an error', () => {
    mockHttpService.getAllProjects.and.returnValue(of({ message: "Error", success: false, error: true }));
    spyOn(localStorage, 'setItem');

    fixture = TestBed.createComponent(DashboardV2Component);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(localStorage.setItem).not.toHaveBeenCalled();
  });

});
