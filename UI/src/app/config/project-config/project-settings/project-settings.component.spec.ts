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
import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
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
import { ActivatedRoute, Router } from '@angular/router';

describe('ProjectSettingsComponent', () => {
  let component: ProjectSettingsComponent;
  let fixture: ComponentFixture<ProjectSettingsComponent>;
  let sharedService: SharedService;
  let httpService: HttpService;
  let messageService: MessageService;
  let confirmationService: ConfirmationService;
  let getAuthorizationService: GetAuthorizationService;
  let routerSpy: jasmine.SpyObj<Router>;
  let httpMock;
  const baseUrl = environment.baseUrl;
  const navigateSpy = jasmine.createSpyObj('Router', ['navigate']);
  const projectListData = require('../../../../test/resource/projectListData.json');
  const activatedRouteMock = {
    snapshot: {
      params: {},
      root: {
        children: []
      }
    }
  };

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
        { provide: ActivatedRoute, useValue: activatedRouteMock },
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
    routerSpy = TestBed.inject(Router) as jasmine.SpyObj<Router>;
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
      ]
    };
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
      ]
    };
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
      ]
    };
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

  it("should get success response while getting project list", () => {
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
    spyOn(httpService, 'getProjectListData').and.returnValue(of(fakeResponse));
    component.getData();
    expect(component.loading).toBeFalse();
    expect(component.projectList.length).toBeGreaterThan(0);
  })

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

  // -> updateProjectSelection
  it('should call sharedService.setSelectedProject, router.navigate, and update properties correctly', () => {
    // Mock the selectedProject data
    const mockProject = {
      id: 1,
      type: 'Scrum',
      saveAssigneeDetails: true,
      developerKpiEnabled: false,
      projectOnHold: true
    };
    component.selectedProject = mockProject;

    // Spy on the sharedService method (sharedService already exists, so no need for mock)
    const sharedService = TestBed.inject(SharedService);
    spyOn(sharedService, 'setSelectedProject');  // Reuse the same spy if already defined in previous test cases

    // Spy on hierarchyLabelNameChange
    spyOn(component, 'hierarchyLabelNameChange');

    // Call the method to be tested
    component.updateProjectSelection();

    // Check if sharedService.setSelectedProject() was called with the correct argument
    expect(sharedService.setSelectedProject).toHaveBeenCalledWith(mockProject);

    // Check if router.navigate() was called with the correct arguments
    expect(routerSpy.navigate).toHaveBeenCalledWith(
      ['/dashboard/Config/ConfigSettings/1'],
      { queryParams: { 'type': 'scrum', tab: 0 } }
    );

    // Check if component properties are updated correctly
    expect(component.isAssigneeSwitchChecked).toBe(true);
    expect(component.developerKpiEnabled).toBe(false);
    expect(component.projectOnHold).toBe(true);

    // Check if hierarchyLabelNameChange() was called
    expect(component.hierarchyLabelNameChange).toHaveBeenCalled();
  });
  // -> end of updateProjectSelection

  describe('onProjectActiveStatusChange', () => {
    it('should call updateProjectDetails when event.checked is true and confirmation is accepted', () => {
      spyOn<any>(confirmationService, 'confirm').and.callFake((params: any) => {
        params.accept();
      });
      spyOn(component, 'updateProjectDetails');

      component.onProjectActiveStatusChange({ checked: true });

      expect(confirmationService.confirm).toHaveBeenCalled();
      expect(component.updateProjectDetails).toHaveBeenCalledWith('Project data collection paused!');
    });

    it('should set projectOnHold to false when event.checked is true and confirmation is rejected', () => {
      spyOn<any>(confirmationService, 'confirm').and.callFake((params) => {
        params.reject();
      });

      component.onProjectActiveStatusChange({ checked: true });

      expect(confirmationService.confirm).toHaveBeenCalled();
      expect(component.projectOnHold).toBe(false);
    });

    it('should call updateProjectDetails when event.checked is false and confirmation is accepted', () => {
      spyOn<any>(confirmationService, 'confirm').and.callFake((params) => {
        params.accept();
      });
      spyOn(component, 'updateProjectDetails');

      component.onProjectActiveStatusChange({ checked: false });

      expect(confirmationService.confirm).toHaveBeenCalled();
      expect(component.updateProjectDetails).toHaveBeenCalledWith('Project data collection resumed!');
    });

    it('should set projectOnHold to true when event.checked is false and confirmation is rejected', () => {
      spyOn<any>(confirmationService, 'confirm').and.callFake((params) => {
        params.reject();
      });

      component.onProjectActiveStatusChange({ checked: false });

      expect(confirmationService.confirm).toHaveBeenCalled();
      expect(component.projectOnHold).toBe(true);
    });
  });

  describe('onProjectDevKpiStatusChange', () => {
    it('should call updateProjectDetails when confirmation is accepted', () => {
      spyOn<any>(confirmationService, 'confirm').and.callFake((params) => {
        params.accept();
      });
      spyOn(component, 'updateProjectDetails');

      component.onProjectDevKpiStatusChange();

      expect(confirmationService.confirm).toHaveBeenCalled();
      expect(component.updateProjectDetails).toHaveBeenCalledWith('Developer KPI for this project enabled!');
    });

    it('should set developerKpiEnabled to false when confirmation is rejected', () => {
      spyOn<any>(confirmationService, 'confirm').and.callFake((params) => {
        params.reject();
      });

      component.onProjectDevKpiStatusChange();

      expect(confirmationService.confirm).toHaveBeenCalled();
      expect(component.developerKpiEnabled).toBe(false);
    });
  });

  describe('deleteProject', () => {
    it('should set isDeleteClicked and projectConfirm to true', () => {
      spyOn(confirmationService, 'confirm');

      component.deleteProject({ name: 'Test Project' });

      expect(component.isDeleteClicked).toBe(true);
      expect(component.projectConfirm).toBe(true);
      expect(confirmationService.confirm).toHaveBeenCalled();
    });

    it('should call httpService.deleteProject and navigate to the correct route on accept', () => {
      spyOn<any>(confirmationService, 'confirm').and.callFake((params) => {
        params.accept();
      });
      spyOn<any>(httpService, 'deleteProject').and.returnValue({ subscribe: () => { } });

      component.deleteProject({ name: 'Test Project', id: 1 });

      expect(confirmationService.confirm).toHaveBeenCalled();
      expect(httpService.deleteProject).toHaveBeenCalledWith({ name: 'Test Project', id: 1 });
      expect(component.router.navigate).toHaveBeenCalledWith(['/dashboard/Config/ConfigSettings/1'], {
        queryParams: { type: 'scrum', tab: 0 },
      });
    });

    it('should update the currentUserDetails on accept', () => {
      spyOn<any>(confirmationService, 'confirm').and.callFake((params) => {
        params.accept();
      });
      spyOn(component.sharedService, 'getCurrentUserDetails').and.returnValue({ projectsAccess: [{ projects: [{ projectId: 1 }] }] });
      spyOn(component.sharedService, 'setCurrentUserDetails');

      component.deleteProject({ name: 'Test Project', id: 1 });

      expect(confirmationService.confirm).toHaveBeenCalled();
      // expect(component.sharedService.getCurrentUserDetails).toHaveBeenCalledWith('projectsAccess');
      // expect(component.sharedService.setCurrentUserDetails).toHaveBeenCalledWith({ projectsAccess: [] });
    });

    it('should call projectDeletionStatus on error', () => {
      spyOn<any>(confirmationService, 'confirm').and.callFake((params) => {
        params.accept();
      });
      spyOn<any>(httpService, 'deleteProject').and.returnValue({ subscribe: (_, error) => error('Error') });
      spyOn(component, 'projectDeletionStatus');

      component.deleteProject({ name: 'Test Project' });

      expect(confirmationService.confirm).toHaveBeenCalled();
      expect(httpService.deleteProject).toHaveBeenCalled();
      expect(component.projectDeletionStatus).toHaveBeenCalledWith('Error');
    });
  });

  describe('projectDeletionStatus', () => {
    it('should set projectConfirm to false and call confirmationService.confirm with success message on data.success', () => {
      spyOn(confirmationService, 'confirm');
      const data = { success: true, message: 'Project deleted successfully!' };

      component.projectDeletionStatus(data);

      expect(component.projectConfirm).toBe(false);
      expect(confirmationService.confirm).toHaveBeenCalled();
    });

    it('should set projectConfirm to false and call confirmationService.confirm with error message on !data.success', () => {
      spyOn(confirmationService, 'confirm');
      const data = { success: false };

      component.projectDeletionStatus(data);

      expect(component.projectConfirm).toBe(false);
      expect(confirmationService.confirm).toHaveBeenCalled();
    });
  });

  describe('ProjectSettingsComponent.deleteProject() deleteProject method', () => {
    describe('Happy Path', () => {
      it('should delete a project successfully', () => {
        // Arrange
        const project = { id: 1, name: 'Test Project', type: 'scrum' };
        spyOn(httpService, 'deleteProject').and.returnValue(
          of({ success: true, message: 'Project deleted successfully' }) as any,
        );
        spyOn<any>(
          confirmationService,
          'confirm',
        ).and.callFake((confirmation: Confirmation) => confirmation.accept());
        spyOn(messageService, 'add');
        // Act
        component.deleteProject(project);

        // Assert
        expect(confirmationService.confirm).toHaveBeenCalled();
        expect(httpService.deleteProject).toHaveBeenCalledWith(project);
        expect(routerSpy.navigate).toHaveBeenCalled();
        // expect(messageService.add).toHaveBeenCalledWith({
        //   severity: 'success',
        //   summary: 'Project deleted successfully',
        // });
      });

      it('should update projectsAccess after a project is deleted successfully', () => {
        // Arrange
        const project = {
          "projectName": "ABC",
          "projectId": "64fffa6b9a54ef4627918635",
          "hierarchy": [
            {
              "hierarchyLevel": {
                "level": 1,
                "hierarchyLevelId": "bu",
                "hierarchyLevelName": "BU"
              },
              "value": "North America"
            },
            {
              "hierarchyLevel": {
                "level": 2,
                "hierarchyLevelId": "ver",
                "hierarchyLevelName": "Vertical"
              },
              "value": "Financial Services"
            },
            {
              "hierarchyLevel": {
                "level": 3,
                "hierarchyLevelId": "acc",
                "hierarchyLevelName": "Account"
              },
              "value": "City and County of San Francisco"
            },
            {
              "hierarchyLevel": {
                "level": 4,
                "hierarchyLevelId": "port",
                "hierarchyLevelName": "Engagement"
              },
              "value": "C&C San Francisco - PAS"
            }
          ]
        };

        spyOn(sharedService, 'getCurrentUserDetails').and.returnValue([
          {
            "role": "ROLE_PROJECT_VIEWER",
            "projects": [
              {
                "projectName": "PSknowHOW",
                "projectId": "65118da7965fbb0d14bce23c",
              }
            ],

          },
          {
            "role": "ROLE_PROJECT_ADMIN",
            "projects": [
              {
                "projectName": "ABC",
                "projectId": "64fffa6b9a54ef4627918635",
              }
            ]
          }
        ]);
        spyOn(sharedService, 'setCurrentUserDetails');
        spyOn<any>(
          confirmationService,
          'confirm',
        ).and.callFake((confirmation: Confirmation) => confirmation.accept());
        spyOn(httpService, 'deleteProject').and.returnValue(
          of({ success: true, message: 'Project deleted successfully' }) as any,
        );
        spyOn(messageService, 'add');
        // Act
        component.deleteProject(project);

        // Assert
        expect(confirmationService.confirm).toHaveBeenCalled();
        expect(httpService.deleteProject).toHaveBeenCalledWith(project);
        expect(routerSpy.navigate).toHaveBeenCalled();
        expect(sharedService.setCurrentUserDetails).toHaveBeenCalledWith({
          projectsAccess: [
            {
              "role": "ROLE_PROJECT_VIEWER",
              "projects": [
                {
                  "projectName": "PSknowHOW",
                  "projectId": "65118da7965fbb0d14bce23c",
                }
              ],
            },
            { role: 'ROLE_PROJECT_ADMIN', projects: [ { projectName: 'ABC', projectId: '64fffa6b9a54ef4627918635' } ] }
          ]
        });
        // expect(messageService.add).toHaveBeenCalledWith({
        //   severity: 'success',
        //   summary: 'Project deleted successfully',
        // });
      });
    });

    describe('Edge Cases', () => {
      it('should handle project deletion failure', () => {
        // Arrange
        const project = { id: 1, name: 'Test Project', type: 'scrum' };
        spyOn(httpService, 'deleteProject').and.returnValue(
          throwError({ success: false }) as never,
        );
        spyOn<any>(
          confirmationService,
          'confirm',
        ).and.callFake((confirmation: Confirmation) => confirmation.accept());
        spyOn(messageService, 'add');
        // Act
        component.deleteProject(project);

        // Assert
        expect(confirmationService.confirm).toHaveBeenCalled();
        expect(httpService.deleteProject).toHaveBeenCalledWith(project);
        // expect(messageService.add).toHaveBeenCalledWith({
        //   severity: 'error',
        //   summary: 'Some error occurred. Please try again later.',
        // });
      });

      it('should not delete project if user cancels confirmation', () => {
        // Arrange
        const project = { id: 1, name: 'Test Project', type: 'scrum' };

        spyOn(httpService, 'deleteProject');
        spyOn<any>(
          confirmationService,
          'confirm',
        ).and.callFake((confirmation: Confirmation) => confirmation.reject());
        // Act
        component.deleteProject(project);

        // Assert
        expect(confirmationService.confirm).toHaveBeenCalled();
        expect(httpService.deleteProject).not.toHaveBeenCalled();
      });
    });
  });

  describe('ProjectSettingsComponent.hierarchyLabelNameChange() hierarchyLabelNameChange method', () => {
    describe('Happy Path', () => {
      it('should update selectedProject with hierarchy level names', fakeAsync(() => {
        // Arrange
        const mockHierarchyData = {
          scrum: [
            { hierarchyLevelId: 'level1', hierarchyLevelName: 'Level 1' },
            { hierarchyLevelId: 'level2', hierarchyLevelName: 'Level 2' },
          ],
        };
        localStorage.setItem(
          'completeHierarchyData',
          JSON.stringify(mockHierarchyData),
        );
        component.selectedProject = {
          type: 'Scrum',
          level1: 'Value 1',
          level2: 'Value 2',
        };

        // Act
        component.hierarchyLabelNameChange();
        tick(0);
        // Assert
        expect(component.selectedProject['Level 1']).toBe('Value 1');
        expect(component.selectedProject['Level 2']).toBe('Value 2');
        expect(component.selectedProject['level1']).toBeUndefined();
        expect(component.selectedProject['level2']).toBeUndefined();
      }));
    });

    describe('Edge Cases', () => {
      it('should handle empty selectedProject gracefully', () => {
        // Arrange
        component.selectedProject = {};

        // Act
        component.hierarchyLabelNameChange();

        // Assert
        expect(component.selectedProject).toEqual({});
      });

      it('should not modify selectedProject if type is not Scrum or Kanban', () => {
        // Arrange
        const mockHierarchyData = {
          scrum: [{ hierarchyLevelId: 'level1', hierarchyLevelName: 'Level 1' }],
        };
        localStorage.setItem(
          'completeHierarchyData',
          JSON.stringify(mockHierarchyData),
        );
        component.selectedProject = {
          type: 'Other',
          level1: 'Value 1',
        };

        // Act
        component.hierarchyLabelNameChange();

        // Assert
        expect(component.selectedProject['level1']).toBe('Value 1');
      });
    });
  });
});
