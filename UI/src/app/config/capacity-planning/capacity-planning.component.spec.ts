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
import { ComponentFixture, fakeAsync, TestBed, tick, waitForAsync } from '@angular/core/testing';
import { CommonModule } from '@angular/common';
import { InputSwitchModule } from 'primeng/inputswitch';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { Routes } from '@angular/router';
import { DashboardComponent } from '../../dashboard/dashboard.component';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HttpService } from '../../services/http.service';
import { environment } from 'src/environments/environment';
import { APP_CONFIG, AppConfig } from '../../services/app.config';
import { SharedService } from '../../services/shared.service';
import { MessageService } from 'primeng/api';
import { GetAuthService } from '../../services/getauth.service';
import { NgSelectModule } from '@ng-select/ng-select';
import { of } from 'rxjs';
import { ManageAssigneeComponent } from '../manage-assignee/manage-assignee.component';
import { CapacityPlanningComponent } from './capacity-planning.component';

describe('CapacityPlanningComponent', () => {
  let fixture: ComponentFixture<CapacityPlanningComponent>;
  const baseUrl = environment.baseUrl;
  let httpMock;
  let httpService;
  let messageService;
  const fakeSuccessResponseCapacity = {
    message: 'Capacity Data',
    success: true,
    data: [
      {
        id: '632c4e17e23ab66523bdbb22',
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintNodeId: '29732_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintName: 'SprintPrioritization_Bucket',
        sprintState: 'FUTURE',
        capacity: 3,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false
      },
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintNodeId: '40249_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintName: 'Tools|PI_11|ITR_2|12 Oct',
        sprintState: 'FUTURE',
        capacity: 0,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false
      },
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintNodeId: '40252_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintName: 'Tools|PI_11|ITR_5| 23 Nov',
        sprintState: 'FUTURE',
        capacity: 0,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false
      },
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintNodeId: '40253_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintName: 'Tools|PI_11|ITR_6| 07 Dec',
        sprintState: 'FUTURE',
        capacity: 0,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false
      },
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintNodeId: '40251_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintName: 'Tools|PI_11|ITR_4| 09 Nov',
        sprintState: 'FUTURE',
        capacity: 0,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false
      },
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintNodeId: '40250_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintName: 'Tools|PI_11|ITR_3|26 Oct',
        sprintState: 'FUTURE',
        capacity: 0,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false
      },
      {
        id: '633eaf5f17c562439124a872',
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintNodeId: '40248_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintName: 'Tools|PI_11|ITR_1|28 Sep',
        sprintState: 'ACTIVE',
        capacity: 500,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false
      },
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintNodeId: '38998_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintName: 'Tools|PI_10|ITR_6|07 Sep',
        sprintState: 'ACTIVE',
        capacity: 0,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false
      },
      {
        id: '63327450dc7db01e674a5379',
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintNodeId: '38997_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintName: 'Tools|PI_10|ITR_5|24 Aug',
        sprintState: 'CLOSED',
        capacity: 520,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false
      },
      {
        id: '63327449dc7db01e674a5378',
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintNodeId: '38996_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintName: 'Tools|PI_10|ITR_4|10 Aug',
        sprintState: 'CLOSED',
        capacity: 500,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false
      },
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintNodeId: '38995_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintName: 'Tools|PI_10|ITR_3|27 Jul',
        sprintState: 'CLOSED',
        capacity: 0,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false
      },
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintNodeId: '39496_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintName: 'Support|PI_10|ITR_2|13 Jul',
        sprintState: 'CLOSED',
        capacity: 0,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false
      },
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintNodeId: '38994_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintName: 'Tools|PI_10|ITR_2|13 Jul',
        sprintState: 'CLOSED',
        capacity: 0,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false
      }
    ]
  };

  const fakeCapacityData = {
    message: 'Capacity Data',
    success: true,
    data: [
      {
        id: '632c4e17e23ab66523bdbb22',
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintNodeId: '29732_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintName: 'SprintPrioritization_Bucket',
        sprintState: 'FUTURE',
        capacity: 3,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false
      },
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintNodeId: '40249_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintName: 'Tools|PI_11|ITR_2|12 Oct',
        sprintState: 'FUTURE',
        capacity: 0,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false
      },
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintNodeId: '40252_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintName: 'Tools|PI_11|ITR_5| 23 Nov',
        sprintState: 'FUTURE',
        capacity: 0,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false
      },
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintNodeId: '40253_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintName: 'Tools|PI_11|ITR_6| 07 Dec',
        sprintState: 'FUTURE',
        capacity: 0,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false
      },
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintNodeId: '40251_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintName: 'Tools|PI_11|ITR_4| 09 Nov',
        sprintState: 'FUTURE',
        capacity: 0,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false
      },
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintNodeId: '40250_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintName: 'Tools|PI_11|ITR_3|26 Oct',
        sprintState: 'FUTURE',
        capacity: 0,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false
      },
      {
        id: '633eaf5f17c562439124a872',
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintNodeId: '40248_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintName: 'Tools|PI_11|ITR_1|28 Sep',
        sprintState: 'ACTIVE',
        capacity: 500,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false
      },
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintNodeId: '38998_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintName: 'Tools|PI_10|ITR_6|07 Sep',
        sprintState: 'ACTIVE',
        capacity: 0,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false
      },
      {
        id: '63327450dc7db01e674a5379',
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintNodeId: '38997_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintName: 'Tools|PI_10|ITR_5|24 Aug',
        sprintState: 'CLOSED',
        capacity: 520,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false
      },
      {
        id: '63327449dc7db01e674a5378',
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintNodeId: '38996_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintName: 'Tools|PI_10|ITR_4|10 Aug',
        sprintState: 'CLOSED',
        capacity: 500,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false
      },
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintNodeId: '38995_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintName: 'Tools|PI_10|ITR_3|27 Jul',
        sprintState: 'CLOSED',
        capacity: 0,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false
      },
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintNodeId: '39496_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintName: 'Support|PI_10|ITR_2|13 Jul',
        sprintState: 'CLOSED',
        capacity: 0,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false
      },
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintNodeId: '38994_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintName: 'Tools|PI_10|ITR_2|13 Jul',
        sprintState: 'CLOSED',
        capacity: 0,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false
      }
    ]
  };
  
  const fakeCapacityKanbanData = require('../../../test/resource/fakeCapacityData.json');

  let component: CapacityPlanningComponent;

  beforeEach(async () => {
    const routes: Routes = [
      { path: 'forget', component: CapacityPlanningComponent },
    ];

    TestBed.configureTestingModule({
      imports: [
        FormsModule,
        InputSwitchModule,
        ReactiveFormsModule,
        CommonModule,
        RouterTestingModule.withRoutes(routes),
        HttpClientTestingModule,
        NgSelectModule
      ],
      declarations: [CapacityPlanningComponent, DashboardComponent],
      providers: [HttpService, SharedService, MessageService, GetAuthService
        , { provide: APP_CONFIG, useValue: AppConfig }

      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    })
      .compileComponents();

      fixture = TestBed.createComponent(CapacityPlanningComponent);
      component = fixture.componentInstance;
      httpService = TestBed.inject(HttpService);
      messageService = TestBed.inject(MessageService);
      httpMock = TestBed.inject(HttpTestingController);
      fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('get capacity of a selected project', () => {
    const projectId = '63284960fdd20276d60e4df5';
    component.getCapacityData(projectId);
    fixture.detectChanges();
    httpMock.match(baseUrl + '/api/capacity/' + projectId)[0].flush(fakeCapacityData);
    expect(component.capacityScrumData).toEqual(fakeCapacityData['data']);
  });

  it("enableDisableSubmitButton() when selectedView === 'upload_Sprint_Capacity'", () => {
    component.selectedView = 'upload_Sprint_Capacity';
    component.setFormControlValues();
    component.popupForm.get('capacity').setValue('Enter Value')
    spyOn(component, 'enableDisableCapacitySubmitButton')
    component.enableDisableSubmitButton();
    fixture.detectChanges();
    expect(component.enableDisableCapacitySubmitButton).toHaveBeenCalled()

  });

  it("enableDisableCapacitySubmitButton() for capacity", () => {
    component.selectedView = 'upload_Sprint_Capacity';
    component.setFormControlValues();
    component.popupForm.get('capacity').setValue('Enter Value')
    component.enableDisableCapacitySubmitButton();
    fixture.detectChanges();
    expect(component.isCapacitySaveDisabled).toBeTrue();
    expect(component.capacityErrorMessage).toBe('Please enter Capacity');
  });

  it("enableDisableCapacitySubmitButton()", () => {
    component.enableDisableCapacitySubmitButton();
    fixture.detectChanges();
    expect(component.isCapacitySaveDisabled).toBeTrue()
  });

  it("should disable save capacity btn", () => {
    component.enableDisableCapacitySubmitButton();
    fixture.detectChanges();
    expect(component.isCapacitySaveDisabled).toBeTrue()
  });

  it('should submit capacity', () => {
    component.reqObj = {
      projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
      projectName: 'DEMO_SONAR',
      kanban: false,
      sprintNodeId: '40248_DEMO_SONAR_63284960fdd20276d60e4df5',
      capacity: '500'
    };
    component.submitCapacity();
    fixture.detectChanges();
    httpMock.match(baseUrl + '/api/capacity')[0].flush(fakeSuccessResponseCapacity);
  });

  it('should get project Assignees for selected project on capacity', () => {
    component.projectJiraAssignees = {};
    let response = {
      "message": "Successfully fetched assignee list",
      "success": true,
      "data": {
        "projectName": "RS MAP",
        "basicProjectConfigId": "63db6583e1b2765622921512",
        "assigneeDetailsList": [{
          "name": "testName1",
          "displayName": "testDisplayName"
        }]
      }
    };
    spyOn(httpService, 'getJiraProjectAssignee').and.returnValue(of(response));
    component.getCapacityJiraAssignee('63db6583e1b2765622921512');
    fixture.detectChanges();
    expect(component.projectJiraAssignees).toEqual(response.data);
  });

  it('should add or remove users from managelist', () => {
    component.manageAssigneeList = [
      {
        "name": "testDisplayName1",
        "displayName": "testDisplayName1",
        "checked": true
      },
      {
        "name": "testDisplayName2",
        "displayName": "testDisplayName2",
        "checked": true
      },
      {
        "name": "testDisplayName3",
        "displayName": "testDisplayName3",
        "checked": false
      }
    ];

    component.selectedSprintDetails = {
      "id": "63e1d151fba71c2bff281502",
      "projectNodeId": "RS MAP_63db6583e1b2765622921512",
      "projectName": "RS MAP",
      "sprintNodeId": "41937_RS MAP_63db6583e1b2765622921512",
      "sprintName": "MAP|PI_12|ITR_5",
      "sprintState": "FUTURE",
      "capacity": -1,
      "basicProjectConfigId": "63db6583e1b2765622921512",
      "assigneeCapacity": [
        {
          "userId": "testUserId1",
          "userName": "testUser",
          "role": "BACKEND_DEVELOPER",
          "plannedCapacity": 55.5,
          "leaves": 0
        },
        {
          "userId": "testUserId2",
          "userName": "testUser",
          "role": "BACKEND_DEVELOPER",
          "plannedCapacity": 15,
          "leaves": 0
        },
        {
          "userId": "testUserId3",
          "userName": "testUser",
          "role": "BACKEND_DEVELOPER",
          "plannedCapacity": 20,
          "leaves": 0
        }
      ],
      "kanban": false,
      "assigneeDetails": true
    };

    const response = {
      "message": "Successfully added Capacity Data",
      "success": true,
      "data": {
        "id": "63e1d151fba71c2bff281502",
        "projectNodeId": "RS MAP_63db6583e1b2765622921512",
        "projectName": "RS MAP",
        "sprintNodeId": "41937_RS MAP_63db6583e1b2765622921512",
        "sprintName": "MAP|PI_12|ITR_5",
        "capacity": -1,
        "basicProjectConfigId": "63db6583e1b2765622921512",
        "assigneeCapacity": [
          {
            "userId": "testUserId4",
            "userName": "testUser",
            "role": "BACKEND_DEVELOPER",
            "plannedCapacity": 55.5,
            "leaves": 0
          },
          {
            "userId": "testUserId5",
            "userName": "testUser",
            "role": "BACKEND_DEVELOPER",
            "plannedCapacity": 15,
            "leaves": 0
          }
        ],
        "kanban": false,
        "assigneeDetails": true
      }
    };

    spyOn(httpService, 'saveOrUpdateAssignee').and.returnValue(of(response));
    const getCapacityDataSpy = spyOn(component, 'getCapacityData');
    component.kanban = true;
    component.addRemoveAssignees();
    fixture.detectChanges();
    expect(getCapacityDataSpy).toHaveBeenCalled();
    expect(component.expandedRows).toBeTruthy();
  });

  it('should get assignee roles if already not available', () => {
    component.projectAssigneeRoles = [];
    const response = {
      "message": "All Roles",
      "success": true,
      "data": {
        "TESTER": "Tester",
        "FRONTEND_DEVELOPER": "Frontend Developer",
        "BACKEND_DEVELOPER": "Backend Developer"
      }
    };
    spyOn(httpService, 'getAssigneeRoles').and.returnValue(of(response));
    component.getAssigneeRoles();
    fixture.detectChanges();
    expect(component.projectAssigneeRoles.length).toEqual(3);
  });

  
  it('should check if assignee toggle enabled', () => {
    const capacityData = [{
      "projectNodeId": "Testproject124_63e4b169fba71c2bff2815ba",
      "projectName": "Testproject124",
      "sprintNodeId": "41411_Testproject124_63e4b169fba71c2bff2815ba",
      "sprintName": "KnowHOW | PI_12| ITR_5",
      "sprintState": "FUTURE",
      "capacity": 0,
      "basicProjectConfigId": "63e4b169fba71c2bff2815ba",
      "kanban": false,
      "assigneeDetails": true
    }];
    const getAssigneeRolesSpy = spyOn(component, 'getAssigneeRoles');
    const getCapacityJiraAssignee = spyOn(component, 'getCapacityJiraAssignee');
    component.checkifAssigneeToggleEnabled(capacityData);
    expect(component.isToggleEnableForSelectedProject).toBeTruthy();
    expect(getAssigneeRolesSpy).toHaveBeenCalled();
    expect(getCapacityJiraAssignee).toHaveBeenCalledWith("63e4b169fba71c2bff2815ba");
  });

  it('should validate plannedCapacity and leaves field value and calculate available capacity', () => {
    const assignee = {
      "userId": "testUserId6",
      "userName": "testUser",
      "leaves": 0
    };
    const assigneeFormControls = {
      "role": new FormControl('TESTER'),
      "plannedCapacity": new FormControl({ value: '', disabled: true }),
      "leaves": new FormControl({ value: 0, disabled: true })

    };

    component.calculateAvaliableCapacity(assignee, assigneeFormControls, 'role');
    fixture.detectChanges();
    expect(assigneeFormControls.plannedCapacity.status).toEqual('VALID');

    assigneeFormControls.plannedCapacity.setValue('40');
    component.calculateAvaliableCapacity(assignee, assigneeFormControls, 'plannedCapacity');
    expect(assignee['availableCapacity']).toEqual(40);

    assigneeFormControls.plannedCapacity.setValue('0');
    component.calculateAvaliableCapacity(assignee, assigneeFormControls, 'plannedCapacity');
    expect(assignee['availableCapacity']).toEqual(0);

    assigneeFormControls.plannedCapacity.setValue('40');
    component.calculateAvaliableCapacity(assignee, assigneeFormControls, 'plannedCapacity');
    expect(assignee['availableCapacity']).toEqual(40);

    component.selectedSprintAssigneValidator = [];
    assigneeFormControls.plannedCapacity.setValue('40');
    assigneeFormControls.leaves.setValue(41);
    component.calculateAvaliableCapacity(assignee, assigneeFormControls, 'leaves');
    expect(component.selectedSprintAssigneValidator.length).toEqual(1);


    assigneeFormControls.leaves.setValue(40);
    component.calculateAvaliableCapacity(assignee, assigneeFormControls, 'leaves');
    expect(component.selectedSprintAssigneValidator.length).toEqual(0);
  });

  it('should reset filter on manage Assignee table on modal open', () => {
    component.manageAssignee = new ManageAssigneeComponent();
    const manageAssigneeResetSpy = spyOn(component.manageAssignee, 'reset');
    component.onAssigneeModalOpen();
    expect(manageAssigneeResetSpy).toHaveBeenCalled();
  });

  it('should calculate total capacity for sprint', () => {
    const selectedSprint = {
      "id": "63e092edfba71c2bff2814b4",
      "projectNodeId": "RS MAP_63db6583e1b2765622921512",
      "projectName": "RS MAP",
      "sprintNodeId": "41935_RS MAP_63db6583e1b2765622921512",
      "sprintName": "MAP|PI_12|ITR_3",
      "sprintState": "CLOSED",
      "capacity": 71,
      "basicProjectConfigId": "63db6583e1b2765622921512",
      "assigneeCapacity": [
        {
          "userId": "testUserId7",
          "userName": "testUser",
          "role": "TESTER",
          "plannedCapacity": 40,
          "leaves": 0,
          "availableCapacity": 40
        },
        {
          "userId": "testUserId8",
          "userName": "testUser",
          "role": "FRONTEND_DEVELOPER",
          "plannedCapacity": 34,
          "leaves": 3,
          "availableCapacity": 31
        },
        {
          "userId": "testUserId9",
          "userName": "testUser",
          "leaves": 0
        }
      ],
      "kanban": false,
      "assigneeDetails": true
    };
    expect(component.calculateTotalCapacityForSprint(selectedSprint)).toEqual(71);

  });

  it('should initialize selectedSprintAssigneFormArray when edit is clicked on sprint', () => {
    const selectedSprint = {
      "id": "63e092edfba71c2bff2814b4",
      "projectNodeId": "RS MAP_63db6583e1b2765622921512",
      "projectName": "RS MAP",
      "sprintNodeId": "41935_RS MAP_63db6583e1b2765622921512",
      "sprintName": "MAP|PI_12|ITR_3",
      "sprintState": "CLOSED",
      "capacity": 71,
      "basicProjectConfigId": "63db6583e1b2765622921512",
      "assigneeCapacity": [
        {
          "userId": "testUserId10",
          "userName": "testUser",
          "role": "TESTER",
          "plannedCapacity": 40,
          "leaves": 0,
          "availableCapacity": 40
        },
        {
          "userId": "testUserId11",
          "userName": "testUser",
          "role": "FRONTEND_DEVELOPER",
          "plannedCapacity": 34,
          "leaves": 3,
          "availableCapacity": 31
        },
        {
          "userId": "testUserId12",
          "userName": "testUser",
          "leaves": 0
        }
      ],
      "kanban": false,
      "assigneeDetails": true
    };
    component.onSprintCapacityEdit(selectedSprint);
    expect(component.selectedSprintAssigneFormArray.length).toEqual(3);
  });

  it('should save the sprint capacity details on click of save', () => {
    const selectedSprint = {
      "projectNodeId": "TestProject123_63d8bca4af279c1d507cb8b0",
      "projectName": "TestProject123",
      "sprintNodeId": "40699_TestProject123_63d8bca4af279c1d507cb8b0",
      "sprintName": "PS HOW |PI_11|ITR_6|07_Dec",
      "sprintState": "CLOSED",
      "capacity": 0,
      "basicProjectConfigId": "63d8bca4af279c1d507cb8b0",
      "assigneeCapacity": [
        {
          "userId": "testUserId13",
          "userName": "testUser",
          "role": "TESTER",
          "plannedCapacity": 40,
          "leaves": 0
        }
      ],
      "kanban": false,
      "assigneeDetails": true
    };

    const response = {
      "message": "Successfully added Capacity Data",
      "success": true,
      "data": {
        "projectNodeId": "TestProject123_63d8bca4af279c1d507cb8b0",
        "sprintNodeId": "40699_TestProject123_63d8bca4af279c1d507cb8b0",
        "sprintName": "PS HOW |PI_11|ITR_6|07_Dec",
        "capacity": 0,
        "basicProjectConfigId": "63d8bca4af279c1d507cb8b0",
        "assigneeCapacity": [
          {
            "userId": "testUserId14",
            "userName": "testUser",
            "role": "TESTER",
            "plannedCapacity": 40,
            "leaves": 0
          }
        ],
        "kanban": false,
        "assigneeDetails": true
      }
    };
    component.kanban=true;

    spyOn(httpService, "saveOrUpdateAssignee").and.returnValue(of(response));
    let getCapacityDataSpy = spyOn(component, 'getCapacityData');
    component.onSprintCapacitySave(selectedSprint);

    fixture.detectChanges();
    expect(getCapacityDataSpy).toHaveBeenCalled();
  });

  it('should send sprint happiness index', () => {
    const selectedSprint = {
      "projectNodeId": "TestProject123_63d8bca4af279c1d507cb8b0",
      "projectName": "TestProject123",
      "sprintNodeId": "40699_TestProject123_63d8bca4af279c1d507cb8b0",
      "sprintName": "PS HOW |PI_11|ITR_6|07_Dec",
      "sprintState": "CLOSED",
      "capacity": 0,
      "basicProjectConfigId": "63d8bca4af279c1d507cb8b0",
      "assigneeCapacity": [
        {
          "userId": "testUserId13",
          "happinessRating":2,
          "userName": "testUser",
          "role": "TESTER",
          "plannedCapacity": 40,
          "leaves": 0
        }
      ],
      "kanban": false,
      "assigneeDetails": true
    };

    const response = {
      "message": "Successfully added Capacity Data",
      "success": true,
      "data": {
        "projectNodeId": "TestProject123_63d8bca4af279c1d507cb8b0",
        "sprintNodeId": "40699_TestProject123_63d8bca4af279c1d507cb8b0",
        "sprintName": "PS HOW |PI_11|ITR_6|07_Dec",
        "capacity": 0,
        "basicProjectConfigId": "63d8bca4af279c1d507cb8b0",
        "assigneeCapacity": [
          {
            "userId": "testUserId14",
            "happinessRating":2,
            "userName": "testUser",
            "role": "TESTER",
            "plannedCapacity": 40,
            "leaves": 0
          }
        ],
        "kanban": false,
        "assigneeDetails": true
      }
    };

    spyOn(httpService, "saveOrUpdateSprintHappinessIndex").and.returnValue(of(response));
    let getCapacityDataSpy = spyOn(component, 'getCapacityData');
    component.sendSprintHappinessIndex(selectedSprint);

    fixture.detectChanges();
    expect(getCapacityDataSpy).toHaveBeenCalled();
  });

  it('should reset  to old values when clicked on cancel btn on selected sprint', () => {
    const selectedSprint = {
      "id": "63e4c5b4fba71c2bff2815d8",
      "projectNodeId": "TestProject123_63d8bca4af279c1d507cb8b0",
      "projectName": "TestProject123",
      "sprintNodeId": "41963_TestProject123_63d8bca4af279c1d507cb8b0",
      "sprintName": "PS HOW |PI_12|ITR_3|25_Jan",
      "sprintState": "CLOSED",
      "capacity": 28,
      "basicProjectConfigId": "63d8bca4af279c1d507cb8b0",
      "assigneeCapacity": [
        {
          "userId": "testUserId15",
          "userName": "testUser",
          "role": "TESTER",
          "plannedCapacity": 40,
          "leaves": 12,
          "availableCapacity": 28
        }
      ],
      "kanban": false,
      "assigneeDetails": true
    };

    component.capacityScrumData = [{
      "id": "63e4c5b4fba71c2bff2815d8",
      "projectNodeId": "TestProject123_63d8bca4af279c1d507cb8b0",
      "projectName": "TestProject123",
      "sprintNodeId": "41963_TestProject123_63d8bca4af279c1d507cb8b0",
      "sprintName": "PS HOW |PI_12|ITR_3|25_Jan",
      "sprintState": "CLOSED",
      "capacity": 28,
      "basicProjectConfigId": "63d8bca4af279c1d507cb8b0",
      "assigneeCapacity": [
        {
          "userId": "testUserId16",
          "userName": "testUser",
          "role": "TESTER",
          "plannedCapacity": 40,
          "leaves": 12,
          "availableCapacity": 28
        }
      ],
      "kanban": false,
      "assigneeDetails": true
    }];

    component.selectedSprint = {
      "id": "63e4c5b4fba71c2bff2815d8",
      "projectNodeId": "TestProject123_63d8bca4af279c1d507cb8b0",
      "projectName": "TestProject123",
      "sprintNodeId": "41963_TestProject123_63d8bca4af279c1d507cb8b0",
      "sprintName": "PS HOW |PI_12|ITR_3|25_Jan",
      "sprintState": "CLOSED",
      "capacity": 28,
      "basicProjectConfigId": "63d8bca4af279c1d507cb8b0",
      "assigneeCapacity": [
        {
          "userId": "testUserId17",
          "userName": "testUser",
          "role": "FRONTEND_DEVELOPER",
          "plannedCapacity": 40,
          "leaves": 12,
          "availableCapacity": 28
        }
      ],
      "kanban": false,
      "assigneeDetails": true
    };

    component.kanban = false;
    component.onSprintCapacityCancel(selectedSprint);
    expect(component.capacityScrumData[0]).toEqual(component.selectedSprint);

  });

  it('should set edit mode to false on sprint row selection', () => {
    component.projectCapacityEditMode = true;
    component.selectedSprint = {
      "id": "63e4c5b4fba71c2bff2815d8",
      "projectNodeId": "TestProject123_63d8bca4af279c1d507cb8b0",
      "projectName": "TestProject123",
      "sprintNodeId": "41963_TestProject123_63d8bca4af279c1d507cb8b0",
      "sprintName": "PS HOW |PI_12|ITR_3|25_Jan",
      "sprintState": "CLOSED",
      "capacity": 28,
      "basicProjectConfigId": "63d8bca4af279c1d507cb8b0",
      "assigneeCapacity": [
        {
          "userId": "testUserId18",
          "userName": "testUser",
          "role": "FRONTEND_DEVELOPER",
          "plannedCapacity": 40,
          "leaves": 12,
          "availableCapacity": 28
        }
      ],
      "kanban": false,
      "assigneeDetails": true
    };

    const onSprintCapacityCancelSpy = spyOn(component, 'onSprintCapacityCancel');
    component.onCapacitySprintRowSelection();
    expect(component.projectCapacityEditMode).toBeFalse();
    expect(onSprintCapacityCancelSpy).toHaveBeenCalled();
  });

  it('should set NoData to true on response for capacity data', () => {
    spyOn(httpService, 'getCapacityData').and.returnValue(of({}));
    component.getCapacityData('TestProject123_63d8bca4af279c1d507cb8b0');
    fixture.detectChanges();
    expect(component.tableLoader).toBeFalse();
    expect(component.noData).toBeTrue();
  });

  it('should set capactiy Data for Kanban', () => {
    const projectId = "testproj2_63d912d2af279c1d507cb93a";
    component.kanban = true;
    const response = {
      "message": "Capacity Data",
      "success": true,
      "data": [
        {
          "projectNodeId": "testproj2_63d912d2af279c1d507cb93a",
          "projectName": "testproj2",
          "capacity": 0,
          "startDate": "2023-01-09",
          "endDate": "2023-01-15",
          "basicProjectConfigId": "63d912d2af279c1d507cb93a",
          "kanban": true,
          "assigneeDetails": true
        },
      ]
    }
    spyOn(component, 'checkifAssigneeToggleEnabled');
    spyOn(httpService, 'getCapacityData').and.returnValue(of(response));
    component.getCapacityData(projectId);
    fixture.detectChanges();
    expect(component.capacityKanbanData.length).toEqual(1);
  });

  it('should show assignee modal on manage User btn click', () => {
    const selectedSprint = {
      "projectNodeId": "RS MAP_63db6583e1b2765622921512",
      "projectName": "RS MAP",
      "sprintNodeId": "41937_RS MAP_63db6583e1b2765622921512",
      "sprintName": "MAP|PI_12|ITR_5",
      "sprintState": "FUTURE",
      "capacity": 0,
      "basicProjectConfigId": "63db6583e1b2765622921512",
      "assigneeCapacity": [],
      "kanban": false,
      "assigneeDetails": true
    };
    spyOn(component, 'generateManageAssigneeData');
    component.manageAssignees(selectedSprint);
    expect(component.displayAssignee).toBeTrue();
  });

  it('should show error message on get project Assignees api fail', () => {
    component.projectJiraAssignees = {};
    const response = {};
    spyOn(httpService, 'getJiraProjectAssignee').and.returnValue(of(response));
    const messageServiceSpy = spyOn(messageService, 'add');
    component.getCapacityJiraAssignee('63db6583e1b2765622921512');
    fixture.detectChanges();
    expect(messageServiceSpy).toHaveBeenCalled();
  });

  it('should generate manage Assignee list data with Selected user on top', () => {
    const selectedSprint = {
      "id": "63e0a78bfba71c2bff2814bf",
      "projectNodeId": "RS MAP_63db6583e1b2765622921512",
      "projectName": "RS MAP",
      "sprintNodeId": "41938_RS MAP_63db6583e1b2765622921512",
      "sprintName": "MAP|PI_12|ITR_6",
      "sprintState": "FUTURE",
      "capacity": 41,
      "basicProjectConfigId": "63db6583e1b2765622921512",
      "assigneeCapacity": [
        {
          "userId": "userId",
          "userName": "testUser",
          "role": "BACKEND_DEVELOPER",
          "plannedCapacity": 55.5,
          "leaves": 0
        }
      ],
      "kanban": false,
      "assigneeDetails": true
    };

    component.projectJiraAssignees = {
      basicProjectConfigId: "63db6583e1b2765622921512",
      projectName: "RS MAP",
      assigneeDetailsList: [
        {
          "name": "testDisplayName1",
          "displayName": "testDisplayName"
        },
        {
          "name": "userId",
          "displayName": "testDisplayName"
        }
      ]
    };
    component.generateManageAssigneeData(selectedSprint);
    expect(component.manageAssigneeList[0].name).toEqual('userId');
  });

});
