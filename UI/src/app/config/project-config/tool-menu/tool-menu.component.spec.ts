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
import { Confirmation, ConfirmationService, MessageService } from 'primeng/api';
import { GoogleAnalyticsService } from '../../../services/google-analytics.service';
import { DataViewModule } from 'primeng/dataview';

import { environment } from 'src/environments/environment';
import { CommonModule } from '@angular/common';
import { of } from 'rxjs';

describe('ToolMenuComponent', () => {
  let component: ToolMenuComponent;
  let fixture: ComponentFixture<ToolMenuComponent>;
  let httpService: HttpService;
  let sharedService: SharedService;
  let confirmationService: ConfirmationService;
  let messageService: MessageService;
  let ga: GoogleAnalyticsService;
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
        ConfirmationService,
        GoogleAnalyticsService,
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
    confirmationService = TestBed.inject(ConfirmationService);
    messageService = TestBed.inject(MessageService);
    ga = TestBed.inject(GoogleAnalyticsService);
    sharedService.setSelectedProject(fakeProject);
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should fetch fetch all tool configs', () => {
    component.isAssigneeSwitchChecked = true;
    spyOn(httpService, 'getAllToolConfigs').and.callThrough();
    spyOn(component, 'setGaData');
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
    if(component.isAssigneeSwitchChecked){
      expect(component.isAssigneeSwitchDisabled).toBeTruthy();
    }
  });

  it('should set tool data for ga event', () => {
    component.selectedTools = [
      {
          "id": "6361050e3fa9e175755f0730",
          "toolName": "Jira",
      },
      {
          "id": "63615320c7a36b1d53797532",
          "toolName": "Jenkins",
      },
      {
          "id": "63615554c7a36b1d53797537",
          "toolName": "GitHub",
      },
      {
          "id": "6361ff31f6f1c850816cedfe",
          "toolName": "Zephyr",
      },
      {
          "id": "6390106ab3c061d8f778b1d2",
          "toolName": "JiraTest",
      },
      {
          "id": "6486f2796803f300a9fd2c14",
          "toolName": "Sonar",
      },
      {
          "id": "64c780f25fec906dbc18f1d7",
          "toolName": "GitHubAction",
      },
    ]
    component.selectedProject = {
      "Project": "KnowHOW",
      "Type": "Scrum",
      "BU": "Internal",
      "Vertical": "PS Internal",
      "Account": "Methods and Tools",
      "Portfolio": "DTS",
      "id": "6360fefc3fa9e175755f0728",
      "saveAssigneeDetails": true
    };
    const gaSpy = spyOn(ga, 'setProjectToolsData').and.callThrough();
    component.setGaData();
    expect(gaSpy).toHaveBeenCalled();
  })

  it('should navigate back to Projects List if no selected project is there', () => {
    sharedService.setSelectedProject(null);
    component.selectedProject = {
      saveAssigneeDetails : true
    }
    const navigateSpy = spyOn(router, 'navigate');
    component.ngOnInit();
    if(!component.selectedProject){
    expect(navigateSpy).toHaveBeenCalledWith(['./dashboard/Config/ProjectList']);
    }
  });

  it('should call generate token on click of continue on confirmation popup', () => {
    const mockConfirm: any = spyOn<any>(
      confirmationService,
      'confirm',
    ).and.callFake((confirmation: Confirmation) => confirmation.accept());
    const generateTokenSpy = spyOn(component, 'generateToken');
    component.generateTokenConfirmation();
    expect(generateTokenSpy).toHaveBeenCalled();
  });

  it('should not call generate token on click of cancel on confirmation popup', () => {
    const mockConfirm: any = spyOn<any>(
      confirmationService,
      'confirm',
    ).and.callFake((confirmation: Confirmation) => confirmation.reject);
    const generateTokenSpy = spyOn(component, 'generateToken');
    component.generateTokenConfirmation();
    expect(generateTokenSpy).not.toHaveBeenCalled();
  });

  it('should make an api call for generating token and dispaly token on modal',()=>{
    const response = {
      message: "API token is updated",
      success: true,
      data:{
        basicProjectConfigId: '6360fefc3fa9e175755f0728',
        projectName: '"KnowHOW"',
        userName: 'TESTADMIN',
        apiToken: 'TestToken',
        expiryDate:'2023-03-10',
        createdAt: '2023-02-10'
      }
    };
    spyOn(sharedService,'getSelectedProject').and.returnValue({
      id:'6360fefc3fa9e175755f0728',
      Project:'KnowHOW'
    });

    spyOn(httpService,'generateToken').and.returnValue(of(response));
    component.generateToken();
    fixture.detectChanges();
    expect(component.generatedToken).toEqual(response.data.apiToken);
  });

  it('should show error message if generate token api fails',()=>{
    const response = {
      message: "Failed fetching API token",
      success: false,
      data:null
    };
    spyOn(sharedService,'getSelectedProject').and.returnValue({
      id:'6360fefc3fa9e175755f0728',
      Project:'KnowHOW'
    });

    spyOn(httpService,'generateToken').and.returnValue(of(response));
    const messageServiceSpy = spyOn(messageService,'add');
    component.generateToken();
    fixture.detectChanges();
    expect(messageServiceSpy).toHaveBeenCalled();
  });

  it('should copy token to clipboard',()=>{
    component.generatedToken='TestToken1';
    component.copyToken();
    expect(component.tokenCopied).toBeTrue();
  });


  it("should disable assignee switch once assignee switch is on",()=>{
    component.isAssigneeSwitchChecked = true;
    const confirmationService = TestBed.get(ConfirmationService); // grab a handle of confirmationService
    spyOn(component,'updateProjectDetails');
    spyOn<any>(confirmationService, 'confirm').and.callFake((params: any) => {
      params.accept();
      params.reject();
    });
    component.onAssigneeSwitchChange();
    if(component.isAssigneeSwitchChecked){
      expect(component.isAssigneeSwitchDisabled).toBeTruthy();
    }
  })

  it("should prepare data for update project",()=>{
    const hierarchyData = [
      {
        level: 1,
        hierarchyLevelId: 'hierarchyLevelOne',
        hierarchyLevelName: 'Level One',
      },
      {
        level: 2,
        hierarchyLevelId: 'hierarchyLevelTwo',
        hierarchyLevelName: 'Level Two',
      },
      {
        level: 3,
        hierarchyLevelId: 'hierarchyLevelThree',
        hierarchyLevelName: 'Level Three',
      },
    ];
    component.selectedProject = {
      Project : "My Project",
      Type : 'kanban',
      ["Level One"] : "T1",
      ["Level Two"] : "T2",
      ["Level Three"] : "T3",

    }
    localStorage.setItem("hierarchyData",JSON.stringify(hierarchyData));
    component.updateProjectDetails();
  })
});
