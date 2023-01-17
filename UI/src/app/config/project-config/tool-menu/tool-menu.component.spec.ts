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
import { ToolMenuComponent } from './tool-menu.component';
import { HttpService } from '../../../services/http.service';
import { SharedService } from '../../../services/shared.service';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AppConfig, APP_CONFIG } from 'src/app/services/app.config';
import { MessageService } from 'primeng/api';

import { DataViewModule } from 'primeng/dataview';

import { environment } from 'src/environments/environment';
import { CommonModule } from '@angular/common';

describe('ToolMenuComponent', () => {
  let component: ToolMenuComponent;
  let fixture: ComponentFixture<ToolMenuComponent>;
  let httpService: HttpService;
  let sharedService: SharedService;
  let httpMock;
  let router: Router;
  const baseUrl = environment.baseUrl;

  const toolsData = require('../../../../test/resource/fakeToolsData.json');
  const mappingData = require('../../../../test/resource/fakeToolMappings.json');
  const fakeProject = {
    id: '6335363749794a18e8a4479b',
    name: 'Scrum Project',
    type: 'Scrum',
    hierarchyLevelOne: 'Sample One',
    hierarchyLevelTwo: 'Sample Two',
    hierarchyLevelThree: 'Sample Three'
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ToolMenuComponent],
      imports: [
        RouterTestingModule,
        HttpClientTestingModule,
        DataViewModule,
        CommonModule
      ],
      providers: [
        HttpService,
        SharedService,
        MessageService,
        { provide: APP_CONFIG, useValue: AppConfig }
      ]
    })
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ToolMenuComponent);
    component = fixture.componentInstance;
    httpService = TestBed.inject(HttpService);
    sharedService = TestBed.inject(SharedService);
    sharedService.setSelectedProject(fakeProject);
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should fetch fetch all tool configs', () => {
    spyOn(httpService, 'getAllToolConfigs').and.callThrough();
    component.ngOnInit();
    expect(httpService.getAllToolConfigs).toHaveBeenCalledTimes(1);

    const toolsReq = httpMock.expectOne(`${baseUrl}/api/basicconfigs/${sharedService.getSelectedProject().id}/tools`);
    expect(toolsReq.request.method).toBe('GET');
    toolsReq.flush(toolsData);

    const jiraOrAzure = toolsData['data'].filter(tool => tool.toolName === 'Jira' || tool.toolName === 'Azure');
    expect(component.disableSwitch).toBeTrue();
    if (jiraOrAzure.length) {
      const mappingsReq = httpMock.expectOne(`${baseUrl}/api/tools/${jiraOrAzure[0].id}/fieldMapping`);
      expect(mappingsReq.request.method).toBe('GET');
      mappingsReq.flush(mappingData);
    }
  });

  it('should navigate back to Projects List if no selected project is there', () => {
    sharedService.setSelectedProject(null);
    const navigateSpy = spyOn(router, 'navigate');
    component.ngOnInit();
    expect(navigateSpy).toHaveBeenCalledWith(['./dashboard/Config/ProjectList']);
  });
});
