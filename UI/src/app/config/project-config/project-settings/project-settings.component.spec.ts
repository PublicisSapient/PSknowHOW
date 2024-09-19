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
import { ProjectSettingsComponent } from './project-settings.component';
import { Confirmation, MessageService } from 'primeng/api';
import { ConfirmationService } from 'primeng/api';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { of, throwError } from 'rxjs';
import { SharedService } from '../../../services/shared.service';
import { HttpService } from '../../../services/http.service';
import { RouterTestingModule } from '@angular/router/testing';
import { AppConfig, APP_CONFIG } from 'src/app/services/app.config';
import { GetAuthorizationService } from 'src/app/services/get-authorization.service';
import { environment } from 'src/environments/environment';
import { Router } from '@angular/router';

describe('ProjectSettingsComponent', () => {
  let component: ProjectSettingsComponent;
  let fixture: ComponentFixture<ProjectSettingsComponent>;
  let sharedService: SharedService;
  let httpService: HttpService;
  let messageService: MessageService;
  let confirmationService: ConfirmationService;
  let getAuthorizationService: GetAuthorizationService;
  let router: Router;
  let httpMock;
  const baseUrl = environment.baseUrl;
  const navigateSpy = jasmine.createSpyObj('Router', ['navigate']);
  const projectListData = require('../../../../test/resource/projectListData.json');

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RouterTestingModule, HttpClientTestingModule],
      declarations: [ProjectSettingsComponent],
      providers: [
        MessageService,
        ConfirmationService,
        SharedService,
        HttpService,
        GetAuthorizationService,
        { provide: APP_CONFIG, useValue: AppConfig },
        { provide: Router, useValue: navigateSpy }
      ]
    }).compileComponents();

    httpService = TestBed.inject(HttpService) as jasmine.SpyObj<HttpService>;
    messageService = TestBed.inject(MessageService) as jasmine.SpyObj<MessageService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectSettingsComponent);
    component = fixture.componentInstance;
    sharedService = TestBed.inject(SharedService);
    // httpService = TestBed.inject(HttpService);
    httpMock = TestBed.inject(HttpTestingController);
    // messageService = TestBed.inject(MessageService);
    confirmationService = TestBed.inject(ConfirmationService);
    getAuthorizationService = TestBed.inject(GetAuthorizationService);
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
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
  })

  it("should prepare data for update project", () => {
    const hierarchyData = {
      kanban: [
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
    ]};
    component.selectedProject = {
      project: "My Project",
      type: 'kanban',
      ["Level One"]: "T1",
      ["Level Two"]: "T2",
      ["Level Three"]: "T3",

    }
    localStorage.setItem("completeHierarchyData", JSON.stringify(hierarchyData));
    component.updateProjectDetails('text');
  })

  it('should update project details successfully', () => {
    const hierarchyData = {
      kanban: [
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
    ]};
    component.selectedProject = {
      project: "My Project",
      type: 'kanban',
      ["Level One"]: "T1",
      ["Level Two"]: "T2",
      ["Level Three"]: "T3",

    }
    localStorage.setItem("completeHierarchyData", JSON.stringify(hierarchyData));
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
    spyOn(messageService, 'add');
    component.updateProjectDetails('text');
    expect(messageService.add).toHaveBeenCalled();
  });

  it('should not update project details', () => {
    const hierarchyData = {
      kanban: [
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
    ]};
    component.selectedProject = {
      project: "My Project",
      type: 'kanban',
      ["Level One"]: "T1",
      ["Level Two"]: "T2",
      ["Level Three"]: "T3",

    }
    localStorage.setItem("completeHierarchyData", JSON.stringify(hierarchyData));
    spyOn(httpService, 'updateProjectDetails').and.returnValue(of('Error'));
    component.isAssigneeSwitchChecked = true;
    spyOn(messageService, 'add');
    component.updateProjectDetails('text');
    expect(messageService.add).toHaveBeenCalled();
    expect(component.isAssigneeSwitchChecked).toBeFalsy();
  });

  it('should call getData on initialization', () => {
    spyOn(component, 'getData');
    component.ngOnInit();
    expect(component.getData).toHaveBeenCalledTimes(1);
  });

  it('should initialize generalControls with correct values', () => {
    component.ngOnInit();
    expect(component.generalControls).toEqual([
      {
        name: 'Pause data collection',
        description: 'Pause data collection through tool connections to control when data is gathered from your integrated tools',
        actionItem: 'switch',
      },
      {
        name: 'Delete project',
        description: 'Delete all project data - collected tools data, user permissions, uploaded data, etc',
        actionItem: 'cta',
      },
    ]);
  });

  it('should initialize oneTimeControls with correct values', () => {
    component.ngOnInit();
    expect(component.oneTimeControls).toEqual([
      {
        name: 'Enable People performance KPIs',
        description: 'Enable fetching people info from Agile PM tool or Repo tool connection',
        actionItem: 'switch-people-kpi',
      },
      {
        name: 'Enable Developer KPIs',
        description: 'Provide consent to clone your code repositories (BitBucket, GitLab, GitHub, Azure Repository) to avoid API rate-limiting issues. The repository for this project will be cloned on the KH Server. This will grant access to valuable KPIs on the Developer dashboard.',
        actionItem: 'switch-developer-kpi',
      }
    ]);
  });

  it('should initialize apiControls with correct values', () => {
    component.ngOnInit();
    expect(component.apiControls).toEqual([
      {
        name: 'Generate API token',
        description: 'You can generate KnowHOW POST API token to upload tools data directly',
        actionItem: 'button',
      },
    ]);
  });

  it('should update isProjectAdmin and isSuperAdmin correctly', () => {
    spyOn(getAuthorizationService, 'checkIfProjectAdmin').and.returnValue(true);
    spyOn(getAuthorizationService, 'checkIfSuperUser').and.returnValue(false);
    component.ngOnInit();
    expect(component.isProjectAdmin).toBe(true);
    expect(component.isSuperAdmin).toBe(false);
  });

  it('should update isProjectAdmin and isSuperAdmin correctly', () => {
    spyOn(getAuthorizationService, 'checkIfProjectAdmin').and.returnValue(false);
    spyOn(getAuthorizationService, 'checkIfSuperUser').and.returnValue(true);
    component.ngOnInit();
    expect(component.isProjectAdmin).toBe(false);
    expect(component.isSuperAdmin).toBe(true);
  });

  /* it('should delete project on click of "Delete"', () => {
    const project = {
      id: '631f394dcfef11709d7ddc7b',
      name: 'MAP',
      type: 'Scrum',
      country: 'India',
      state: 'Haryana',
      city: 'Gurgaon'
  };

    const deleteResponse = {
      message: 'MAP deleted successfully',
      success: true,
      data: {
        id: '631f394dcfef11709d7ddc7b',
        projectName: 'MAP',
        createdAt: '2022-09-12T19:21:09',
        kanban: false,
        hierarchy: [
          {
            hierarchyLevel: {
              level: 1,
              hierarchyLevelId: 'country',
              hierarchyLevelName: 'Country'
            },
            value: 'India'
          },
          {
            hierarchyLevel: {
              level: 2,
              hierarchyLevelId: 'state',
              hierarchyLevelName: 'State'
            },
            value: 'Haryana'
          },
          {
            hierarchyLevel: {
              level: 3,
              hierarchyLevelId: 'city',
              hierarchyLevelName: 'City'
            },
            value: 'Gurgaon'
          }
        ],
        isKanban: false
      }
    };

    const mockConfirm: any = spyOn<any>(confirmationService, 'confirm').and.callFake((confirmation: Confirmation) => confirmation.accept());
    component.deleteProject(project);
    expect(mockConfirm).toHaveBeenCalled();
    httpMock.expectOne(baseUrl + '/api/basicconfigs/631f394dcfef11709d7ddc7b').flush(deleteResponse);
    fixture.detectChanges();
  });

  it('should delete project on click of "Delete" and get projectsAccess', () => {
    const project = {
      id: '631f394dcfef11709d7ddc7b',
      name: 'MAP',
      type: 'Scrum',
      country: 'India',
      state: 'Haryana',
      city: 'Gurgaon'
  };

    const deleteResponse = {
      message: 'MAP deleted successfully',
      success: true,
      data: {
        id: '631f394dcfef11709d7ddc7b',
        projectName: 'MAP',
        createdAt: '2022-09-12T19:21:09',
        kanban: false,
        hierarchy: [
          {
            hierarchyLevel: {
              level: 1,
              hierarchyLevelId: 'country',
              hierarchyLevelName: 'Country'
            },
            value: 'India'
          },
        ],
        isKanban: false
      }
    };

    spyOn(sharedService,'getCurrentUserDetails').and.returnValue([{
      projects : [
        {projectId : '123'}
      ]
    }])
    spyOn(httpService,'deleteProject').and.returnValue(of({success : true}))
    spyOn(component,'projectDeletionStatus');

    const mockConfirm: any = spyOn<any>(confirmationService, 'confirm').and.callFake((confirmation: Confirmation) => confirmation.accept());
    component.deleteProject(project);
    expect(mockConfirm).toHaveBeenCalled();
  });

  it('should get error while deleting proect', () => {
    const project = {
      id: '631f394dcfef11709d7ddc7b',
      name: 'MAP',
      type: 'Scrum',
      country: 'India',
      state: 'Haryana',
      city: 'Gurgaon'
  };

    const deleteResponse = {
      message: 'MAP deleted successfully',
      success: true,
      data: {
        id: '631f394dcfef11709d7ddc7b',
        projectName: 'MAP',
        createdAt: '2022-09-12T19:21:09',
        kanban: false,
        hierarchy: [
          {
            hierarchyLevel: {
              level: 1,
              hierarchyLevelId: 'country',
              hierarchyLevelName: 'Country'
            },
            value: 'India'
          },
        ],
        isKanban: false
      }
    };

    spyOn(sharedService,'getCurrentUserDetails').and.returnValue([{
      projects : [
        {projectId : '123'}
      ]
    }])
    spyOn(httpService,'deleteProject').and.returnValue(throwError('Error'))
    spyOn(component,'projectDeletionStatus');

    const mockConfirm: any = spyOn<any>(confirmationService, 'confirm').and.callFake((confirmation: Confirmation) => confirmation.accept());
    component.deleteProject(project);
    expect(mockConfirm).toHaveBeenCalled();
  }); */

  it('should set isDeleteClicked and projectConfirm to true', () => {
    component.deleteProject({ id: 1, name: 'Test Project' });
    expect(component.isDeleteClicked).toBe(true);
    expect(component.projectConfirm).toBe(true);
  });

  it('should call httpService.deleteProject with correct project when confirmation is accepted', () => {
    const deleteProjectSpy = spyOn(httpService, 'deleteProject');
    component.deleteProject({ id: 1, name: 'Test Project' });
    confirmationService.confirm({
      message: component.getAlertMessageOnClickDelete(),
      header: 'Delete Test Project?',
      icon: 'pi pi-info-circle',
      accept: () => {
        expect(deleteProjectSpy).toHaveBeenCalledWith({ id: 1, name: 'Test Project' });
      }
    });
  });

  it('should call projectDeletionStatus with response when deletion is successful', () => {
    const projectDeletionStatusSpy = spyOn(component, 'projectDeletionStatus');
    component.deleteProject({ id: 1, name: 'Test Project' });
    confirmationService.confirm({
      message: component.getAlertMessageOnClickDelete(),
      header: 'Delete Test Project?',
      icon: 'pi pi-info-circle',
      accept: () => {
        httpService.deleteProject({ id: 1, name: 'Test Project' }).subscribe(response => {
          expect(projectDeletionStatusSpy).toHaveBeenCalledWith(response);
        });
      }
    });
  });

  it('should call router.navigate with correct URL when deletion is successful', () => {
    component.deleteProject({ id: 1, name: 'Test Project' });
    confirmationService.confirm({
      message: component.getAlertMessageOnClickDelete(),
      header: 'Delete Test Project?',
      icon: 'pi pi-info-circle',
      accept: () => {
        httpService.deleteProject({ id: 1, name: 'Test Project' }).subscribe(response => {
          expect(navigateSpy).toHaveBeenCalledWith(['/dashboard/Config/ConfigSettings/1'], { queryParams: { type: 'scrum', tab: 0 } });
        });
      }
    });
  });

  it('should update selectedProject when deletion is successful', () => {
    component.deleteProject({ id: 1, name: 'Test Project' });
    confirmationService.confirm({
      message: component.getAlertMessageOnClickDelete(),
      header: 'Delete Test Project?',
      icon: 'pi pi-info-circle',
      accept: () => {
        httpService.deleteProject({ id: 1, name: 'Test Project' }).subscribe(response => {
          expect(component.selectedProject).toEqual({ id: 1, name: 'Test Project' });
        });
      }
    });
  });

  it('should call sharedService.setCurrentUserDetails with updated projects when deletion is successful', () => {
    const setCurrentUserDetailsSpy = spyOn(sharedService, 'setCurrentUserDetails');
    component.deleteProject({ id: 1, name: 'Test Project' });
    confirmationService.confirm({
      message: component.getAlertMessageOnClickDelete(),
      header: 'Delete Test Project?',
      icon: 'pi pi-info-circle',
      accept: () => {
        httpService.deleteProject({ id: 1, name: 'Test Project' }).subscribe(response => {
          expect(setCurrentUserDetailsSpy).toHaveBeenCalledWith({ projectsAccess: [] });
        });
      }
    });
  });

  it('should call projectDeletionStatus with error when deletion fails', () => {
    const projectDeletionStatusSpy = spyOn(component, 'projectDeletionStatus');
    component.deleteProject({ id: 1, name: 'Test Project' });
    confirmationService.confirm({
      message: component.getAlertMessageOnClickDelete(),
      header: 'Delete Test Project?',
      icon: 'pi pi-info-circle',
      accept: () => {
        httpService.deleteProject({ id: 1, name: 'Test Project' }).subscribe(null, error => {
          expect(projectDeletionStatusSpy).toHaveBeenCalledWith(error);
        });
      }
    });
  });

  xit('should set tokenCopied to true when copyToken is called', () => {
    component.generatedToken = 'test-token';
    component.copyToken();
    expect(component.tokenCopied).toBe(true);
  });

  it('should call navigator.clipboard.writeText with the correct token value', () => {
    const writeTextSpy = spyOn(navigator.clipboard, 'writeText');
    component.generatedToken = 'test-token';
    component.copyToken();
    expect(writeTextSpy).toHaveBeenCalledWith('test-token');
  });

  // it('should call navigator.clipboard.writeText with an empty string when generatedToken is empty or null', () => {
  //   const writeTextSpy = spyOn(navigator.clipboard, 'writeText');
  //   component.generatedToken = '';
  //   component.copyToken();
  //   expect(writeTextSpy).toHaveBeenCalledWith('');

  //   component.generatedToken = null;
  //   component.copyToken();
  //   expect(writeTextSpy).toHaveBeenCalledWith('');
  // });

  // it('should set the cols and allProjectList properties when the response is successful', () => {
  //   const responseList = [{
  //     success: true,
  //     data: [{
  //       id: 'projectId',
  //       projectName: 'Project Name',
  //       hierarchy: [{
  //         hierarchyLevel: {
  //           level: 1,
  //           hierarchyLevelId: 'country',
  //           hierarchyLevelName: 'Country'
  //         },
  //         value: 'India'
  //       }]
  //     }]
  //   }];
  //   spyOn(httpService, 'getProjectListData').and.returnValue(of(responseList));
  //   component.getData();
  //   expect(component.cols).toEqual([{ id: 'country', heading: 'Country' }]);
  //   expect(component.allProjectList).toEqual([{
  //     id: 'projectId',
  //     name: 'Project Name',
  //     type: 'Scrum',
  //     saveAssigneeDetails: false,
  //     developerKpiEnabled: false,
  //     projectOnHold: false,
  //     country: 'India'
  //   }]);
  // });

  it("should get success response while getting project list",()=>{
    const fakeResponse = [{
      message: "Fetched successfully",
      success: true,
      data: [
        {
          id: "631f394dcfef11709d7ddc7b",
          projectName: "MAP",
          createdAt: "2022-09-12T19:21:09",
          kanban: false,
          hierarchy: [
            {
              hierarchyLevel: {
                level: 1,
                hierarchyLevelId: "country",
                hierarchyLevelName: "Country"
              },
              value: "India"
            },
            {
              hierarchyLevel: {
                level: 2,
                hierarchyLevelId: "state",
                hierarchyLevelName: "State"
              },
              value: "Haryana"
            },
            {
              hierarchyLevel: {
                level: 3,
                hierarchyLevelId: "city",
                hierarchyLevelName: "City"
              },
              value: "Gurgaon"
            }
          ],
          isKanban: false
        }
      ]
    }];
    spyOn(httpService,'getProjectListData').and.returnValue(of(fakeResponse));
    component.getData();
    expect(component.loading).toBeFalse();
    expect(component.projectList.length).toBeGreaterThan(0);
  })

  // it("should project list zero while getting project list",()=>{

  //   spyOn(httpService,'getProjectListData').and.returnValue(of(projectListData.data));
  //   component.getData();
  //   expect(component.loading).toBeFalse();
  //   expect(component.projectList?.length).toBe(0)
  // })

  it('should set the loading property to false when the response is not successful', () => {
    const responseList = [{ success: false }];
    spyOn(httpService, 'getProjectListData').and.returnValue(of(responseList));
    component.getData();
    expect(component.loading).toBe(false);
  });

  it('should call the messageService.add method when the response is not successful', () => {
    const responseList = [{ success: false }];
    spyOn(httpService, 'getProjectListData').and.returnValue(of(responseList));
    spyOn(messageService, 'add');
    component.getData();
    expect(messageService.add).toHaveBeenCalledWith({
      severity: 'error',
      summary: 'Some error occurred. Please try again later.'
    });
  });

  it('should assign userProjects correctly', () => {
    const projectList = [
      { id: 1, name: 'Project 1' },
      { id: 2, name: 'Project 2' }
    ];
    spyOn(sharedService, 'getProjectList').and.returnValue(projectList);
    component.getProjects();
    expect(component.userProjects).toEqual(projectList);
  });

  it('should sort userProjects by name', () => {
    const projectList = [
      { id: 1, name: 'Project 2' },
      { id: 2, name: 'Project 1' }
    ];
    spyOn(sharedService, 'getProjectList').and.returnValue(projectList);
    component.getProjects();
    expect(component.userProjects).toEqual([
      { id: 2, name: 'Project 1' },
      { id: 1, name: 'Project 2' }
    ]);
  });

  it('should assign selectedProject to the first project in userProjects when sharedService.getSelectedProject() returns null', () => {
    const projectList = [
      { id: 1, name: 'Project 1' },
      { id: 2, name: 'Project 2' }
    ];
    spyOn(sharedService, 'getProjectList').and.returnValue(projectList);
    spyOn(sharedService, 'getSelectedProject').and.returnValue(null);
    component.getProjects();
    expect(component.selectedProject).toEqual(projectList[0]);
  });

  // it('should assign isAssigneeSwitchChecked, isAssigneeSwitchDisabled, developerKpiEnabled, isDeveloperKpiSwitchDisabled, and projectOnHold correctly', () => {
  //   const project = {
  //     id: 1,
  //     name: 'Project 1',
  //     saveAssigneeDetails: true,
  //     developerKpiEnabled: true,
  //     projectOnHold: true
  //   };
  //   spyOn(sharedService, 'getSelectedProject').and.returnValue(project);
  //   component.getProjects();
  //   expect(component.isAssigneeSwitchChecked).toBe(true);
  //   expect(component.isAssigneeSwitchDisabled).toBe(true);
  //   expect(component.developerKpiEnabled).toBe(true);
  //   expect(component.isDeveloperKpiSwitchDisabled).toBe(true);
  //   expect(component.projectOnHold).toBe(true);
  // });

  // it('should rename selectedProject properties correctly', () => {
  //   const project = {
  //     id: 1,
  //     name: 'Project 1',
  //     hierarchyLevelId: 'hierarchyLevelName'
  //   };
  //   const levelDetails = [
  //     { id: 'hierarchyLevelId', name: 'hierarchyLevelName' }
  //   ];
  //   spyOn(sharedService, 'getSelectedProject').and.returnValue(project);
  //   localStorage.setItem('completeHierarchyData', JSON.stringify({ kanban: levelDetails }));
  //   component.getProjects();
  //   expect(component.selectedProject).toEqual({
  //     id: 1,
  //     name: 'Project 1',
  //     hierarchyLevelName: 'hierarchyLevelName'
  //   });
  // });

  // it('should set selected project', () => {
  //   component.updateProjectSelection();
  //   expect(component.sharedService.getSelectedProject()).toEqual(component.selectedProject);
  // });

  // it('should navigate to correct route', () => {
  //   // spyOn(component.router, 'navigate');
  //   component.updateProjectSelection();
  //   expect(component.router.navigate).toHaveBeenCalledWith(['/dashboard/Config/ConfigSettings/project1'], { queryParams: { tab: 0 } });
  // });

  // it('should update isAssigneeSwitchChecked', () => {
  //   component.updateProjectSelection();
  //   expect(component.isAssigneeSwitchChecked).toBeTrue();
  // });

  // it('should update isDeveloperKpiSwitchDisabled', () => {
  //   component.updateProjectSelection();
  //   expect(component.isDeveloperKpiSwitchDisabled).toBeTrue();
  // });

});
