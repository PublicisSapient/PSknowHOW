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
import { ProjectListComponent } from '../project-list/project-list.component';
import { url } from 'inspector';

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
    // const httpSpy = jasmine.createSpyObj('HttpService', ['getAllToolConfigs']);
    // const sharedSpy = jasmine.createSpyObj('SharedService', ['setSelectedToolConfig']);
    await TestBed.configureTestingModule({
      declarations: [ToolMenuComponent, ProjectListComponent],
      imports: [
        RouterTestingModule.withRoutes([
          { path: 'dashboard/Config/ProjectList', component: ProjectListComponent },
        ]),
        HttpClientTestingModule,
        DataViewModule,
        CommonModule
      ],
      providers: [
        // { provide: HttpService, useValue: httpSpy },
        // { provide: SharedService, useValue: sharedSpy },
        HttpService,
        SharedService,
        MessageService,
        ConfirmationService,
        GoogleAnalyticsService,
        { provide: APP_CONFIG, useValue: AppConfig }
      ]
    }).compileComponents();

    httpService = TestBed.inject(HttpService) as jasmine.SpyObj<HttpService>;
    sharedService = TestBed.inject(SharedService) as jasmine.SpyObj<SharedService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ToolMenuComponent);
    component = fixture.componentInstance;
    confirmationService = TestBed.inject(ConfirmationService);
    messageService = TestBed.inject(MessageService);
    ga = TestBed.inject(GoogleAnalyticsService);
    sharedService.setSelectedProject(fakeProject);
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);

    component.selectedProject = { id: 1, Type: 'Scrum' };
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });


  it('should set selected tools and call setGaData', () => {
    spyOn(component, 'setGaData');

    const response = {
      success: true,
      data: [
        { toolName: 'Jira', id: '1', releaseEndDate: '2023-01-01' },
        { toolName: 'Azure', id: '2', releaseEndDate: '2023-01-02' }
      ]
    };

    // httpService.getAllToolConfigs.and.returnValue(of(response));
    spyOn(httpService, 'getAllToolConfigs').and.returnValue(of(response));
    spyOn(sharedService, 'setSelectedToolConfig').and.callThrough();

    component.getToolsConfigured();

    expect(httpService.getAllToolConfigs).toHaveBeenCalledWith(1);
    expect(sharedService.setSelectedToolConfig).toHaveBeenCalledWith(response.data);
    // expect(component.selectedTools).toEqual(response.data);
    expect(component.setGaData).toHaveBeenCalled();
  });

  it('should handle jiraOrAzure tools', () => {
    spyOn(component, 'setGaData').and.callThrough();
    spyOn(component, 'projectTypeChange').and.callThrough();

    const response = {
      success: true,
      data: [
        { toolName: 'Azure', id: '2', releaseEndDate: '2023-01-02' }
      ]
    };

    spyOn(httpService, 'getAllToolConfigs').and.returnValue(of(response));
    // spyOn(httpService, 'getAllToolConfigs').and.callThrough();

    component.getToolsConfigured();

    expect(component.projectTypeChange).toHaveBeenCalledWith({ value: { value: true } }, false);
    expect(component.selectedType).toBe(true);
  });

  it('should handle the router url and set tools', () => {
    spyOn(component, 'setGaData');
    const selectedProjectId = component.selectedProject.id;
    Object.defineProperty(router, 'url', { value: `/dashboard/Config/ProjectList/${selectedProjectId}/ToolMenu` });

    const response = {
      success: true,
      data: [
        { toolName: 'Jira', id: '1', releaseEndDate: '2023-01-01', connectionName: 'Connection1', updatedAt: '2023-01-01' }
      ]
    };

    spyOn(httpService, 'getAllToolConfigs').and.returnValue(of(response));
    spyOn(component, 'updateProjectSelection');
    component.updateProjectSelection();

    component.getToolsConfigured();
    expect(component.buttonText).toBe('Set Up');
    expect(component.tools.length).toBeGreaterThan(0);
    expect(component.tools[0].toolName).toBe('Jira');
    expect(component.tools[0].connectionName).toBe('Connection1');
    expect(component.tools[0].updatedAt).toBe('2023-01-01');
  });

  it('should set release end date and field mappings', () => {
    spyOn(component, 'setGaData');

    const response = {
      success: true,
      data: [
        { toolName: 'Jira', id: '1', releaseEndDate: '2023-01-01' }
      ]
    };

    spyOn(httpService, 'getAllToolConfigs').and.returnValue(of(response));
    // spyOn(httpService, 'getAllToolConfigs').and.callThrough();

    spyOn(httpService, 'getFieldMappingsWithHistory').and.returnValue(of({ success: true, data: [] }));

    component.getToolsConfigured();

    expect(httpService.getFieldMappingsWithHistory).toHaveBeenCalled();
    spyOn(sharedService, 'setSelectedFieldMapping');
    sharedService.setSelectedFieldMapping([]);
    expect(sharedService.setSelectedFieldMapping).toHaveBeenCalledWith([]);
    expect(component.disableSwitch).toBe(true);
  });

  it('should set field mappings to null if unsuccessful', () => {
    spyOn(component, 'setGaData');

    const response = {
      success: true,
      data: [
        { toolName: 'Jira', id: '1', releaseEndDate: '2023-01-01' }
      ]
    };

    spyOn(httpService, 'getAllToolConfigs').and.returnValue(of(response));
    // spyOn(httpService, 'getAllToolConfigs').and.callThrough();

    spyOn(httpService, 'getFieldMappingsWithHistory').and.returnValue(of({ success: false }));

    component.getToolsConfigured();
    spyOn(sharedService, 'setSelectedFieldMapping');
    sharedService.setSelectedFieldMapping(null);
    expect(sharedService.setSelectedFieldMapping).toHaveBeenCalledWith(null);
    expect(component.disableSwitch).toEqual(false);
  });


  it('should fetch all tool configs', () => {
    component.isAssigneeSwitchChecked = true;
    component.selectedProject = {
      Type : 'Scrum'
    }
    // spyOn(httpService, 'getAllToolConfigs').and.callThrough();
    // spyOn(component, 'setGaData');
    component.ngOnInit();
    // expect(httpService.getAllToolConfigs).toHaveBeenCalledTimes(1);

    // const toolsReq = httpMock.expectOne(`${baseUrl}/api/basicconfigs/${sharedService.getSelectedProject().id}/tools`);
    // expect(toolsReq.request.method).toBe('GET');
    // toolsReq.flush(toolsData);

    // const jiraOrAzure = toolsData['data'].filter(tool => tool.toolName === 'Jira' || tool.toolName === 'Azure');
    // if (jiraOrAzure.length) {
    //   let mappingObj = {
    //     "releaseNodeId": null
    //   }
    //   const mappingsReq = httpMock.expectOne(`${baseUrl}/api/tools/fieldMapping/${jiraOrAzure[0].id}/kpi0`, mappingObj);
    //   expect(mappingsReq.request.method).toBe('POST');
    //   mappingsReq.flush(mappingData);
    //   expect(component.disableSwitch).toBeTrue();
    // }
    // if (component.isAssigneeSwitchChecked) {
    //   expect(component.isAssigneeSwitchDisabled).toBeTruthy();
    // }
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
      {
        "id": "64c780f25fsvr46h46j57n3e",
        "toolName": "ArgoCD",
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
      saveAssigneeDetails: true
    }
    const navigateSpy = spyOn(router, 'navigate');
    component.ngOnInit();
    if (!component.selectedProject) {
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

  it('should make an api call for generating token and dispaly token on modal', () => {
    const response = {
      message: "API token is updated",
      success: true,
      data: {
        basicProjectConfigId: '6360fefc3fa9e175755f0728',
        projectName: '"KnowHOW"',
        userName: 'TESTADMIN',
        apiToken: 'TestToken',
        expiryDate: '2023-03-10',
        createdAt: '2023-02-10'
      }
    };
    spyOn(sharedService, 'getSelectedProject').and.returnValue({
      id: '6360fefc3fa9e175755f0728',
      Project: 'KnowHOW'
    });

    spyOn(httpService, 'generateToken').and.returnValue(of(response));
    component.generateToken();
    fixture.detectChanges();
    expect(component.generatedToken).toEqual(response.data.apiToken);
  });

  it('should show error message if generate token api fails', () => {
    const response = {
      message: "Failed fetching API token",
      success: false,
      data: null
    };
    spyOn(sharedService, 'getSelectedProject').and.returnValue({
      id: '6360fefc3fa9e175755f0728',
      Project: 'KnowHOW'
    });

    spyOn(httpService, 'generateToken').and.returnValue(of(response));
    const messageServiceSpy = spyOn(messageService, 'add');
    component.generateToken();
    fixture.detectChanges();
    expect(messageServiceSpy).toHaveBeenCalled();
  });

  xit('should copy token to clipboard', () => {
    component.generatedToken = 'TestToken1';
    component.copyToken();
    expect(component.tokenCopied).toBeTrue();
  });


  it("should disable assignee switch once assignee switch is on", () => {
    component.isAssigneeSwitchChecked = true;
    const confirmationService = TestBed.get(ConfirmationService); // grab a handle of confirmationService
    spyOn(component, 'updateProjectDetails');
    spyOn<any>(confirmationService, 'confirm').and.callFake((params: any) => {
      params.accept();
      params.reject();
    });
    component.onAssigneeSwitchChange();
    if (component.isAssigneeSwitchChecked) {
      expect(component.isAssigneeSwitchDisabled).toBeTruthy();
    }
  })

  it("should prepare data for update project", () => {
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
      Project: "My Project",
      Type: 'kanban',
      ["Level One"]: "T1",
      ["Level Two"]: "T2",
      ["Level Three"]: "T3",

    }
    localStorage.setItem("hierarchyData", JSON.stringify(hierarchyData));
    component.updateProjectDetails();
  })

  it('should check if project is configured when tool selected is AzurePipeline', () => {
    component.selectedTools = [
      {
        "toolName": "Jira",
      },
      {
        "toolName": "AzurePipeline",
      },
      {
        "toolName": "AzureRepository",
      },
      {
        "toolName": "GitHubAction",
      },
    ];
    expect(component.isProjectConfigured('Azure Pipeline')).toBeTruthy();
  })

  it('should check if project is configured when tool selected is AzureRepository', () => {
    component.selectedTools = [
      {
        "toolName": "Jira",
      },
      {
        "toolName": "AzurePipeline",
      },
      {
        "toolName": "AzureRepository",
      },
      {
        "toolName": "GitHubAction",
      },
    ];
    expect(component.isProjectConfigured('Azure Repo')).toBeTruthy();
  })

  it('should check if project is configured when tool selected is GitHub Action', () => {
    component.selectedTools = [
      {
        "toolName": "Jira",
      },
      {
        "toolName": "AzurePipeline",
      },
      {
        "toolName": "AzureRepository",
      },
      {
        "toolName": "GitHubAction",
      },
    ];
    expect(component.isProjectConfigured('GitHub Action')).toBeTruthy();
  })

  it('should update project details', () => {
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
      Project: "My Project",
      Type: 'kanban',
      ["Level One"]: "T1",
      ["Level Two"]: "T2",
      ["Level Three"]: "T3",

    }
    localStorage.setItem("hierarchyData", JSON.stringify(hierarchyData));
    const response = {
      "serviceResponse": {
          "message": "Updated Successfully.",
          "success": true,
          "data": {
              "id": "63777558175a953a0a49d363",
              "projectName": "VDOS",
          }
      },
      "projectsAccess": []
    }
    spyOn(httpService, 'updateProjectDetails').and.returnValue(of(response));
    component.isAssigneeSwitchDisabled = false;
    spyOn(messageService, 'add');
    component.updateProjectDetails();
    expect(messageService.add).toHaveBeenCalled();
    expect(component.isAssigneeSwitchDisabled).toBeTruthy();
  });

  it('should update project details', () => {
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
      Project: "My Project",
      Type: 'kanban',
      ["Level One"]: "T1",
      ["Level Two"]: "T2",
      ["Level Three"]: "T3",

    }
    localStorage.setItem("hierarchyData", JSON.stringify(hierarchyData));
    spyOn(httpService, 'updateProjectDetails').and.returnValue(of('Error'));
    component.isAssigneeSwitchChecked = true;
    component.isAssigneeSwitchDisabled = true;
    spyOn(messageService, 'add');
    component.updateProjectDetails();
    expect(messageService.add).toHaveBeenCalled();
    expect(component.isAssigneeSwitchChecked).toBeFalsy();
    expect(component.isAssigneeSwitchDisabled).toBeFalsy();
  });

  it('should filter tools based on repo tool config', () => {
    component.tools = [
      {
        "id": "6361050e3fa9",
        "toolName": 'jira'
      }
    ];
    component.repoToolsEnabled = true;
    component.repoTools = ["jira", "bitbucket"];
    component.ngOnInit();
    expect(component.tools.length).toEqual(1);
  })

  it('should filter tools based on repo tool config', () => {
    component.tools = [
      {
        "id": "6361050e3fa9",
        "toolName": 'jira'
      }
    ];
    component.repoToolsEnabled = true;
    component.repoTools = ["bitbucket"];
    component.ngOnInit();
    expect(component.tools.length).toEqual(1);
  })

});
