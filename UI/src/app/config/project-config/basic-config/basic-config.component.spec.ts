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
import { AbstractControl, UntypedFormBuilder, UntypedFormGroup, UntypedFormControl } from '@angular/forms';
import { MessageService } from 'primeng/api';
import { HttpService } from '../../../services/http.service';
import { SharedService } from '../../../services/shared.service';
import { GetAuthorizationService } from '../../../services/get-authorization.service';
import { GoogleAnalyticsService } from '../../../services/google-analytics.service';
import { BasicConfigComponent } from './basic-config.component';
import { Router } from '@angular/router';
import { of } from 'rxjs';

describe('BasicConfigComponent', () => {
  let component: BasicConfigComponent;
  let fixture: ComponentFixture<BasicConfigComponent>;
  let httpService: jasmine.SpyObj<HttpService>;
  let sharedService: jasmine.SpyObj<SharedService>;
  let authService: jasmine.SpyObj<GetAuthorizationService>;
  let messageService: jasmine.SpyObj<MessageService>;
  let gaService: jasmine.SpyObj<GoogleAnalyticsService>;

  const hierarchyData = [
    {
      "hierarchyLevelId": "bu",
      "hierarchyLevelIdName": "Business Unit",
      "list": [
        {
          "id": "66e16612d1d34875e9ebc8c6",
          "nodeId": "bu_unique_001",
          "nodeName": "Global Business Unit",
          "nodeDisplayName": "Business Unit 1",
          "hierarchyLevelId": "bu",
          "createdDate": "2024-08-28T10:17:44"
        }
      ]
    },
    {
      "hierarchyLevelId": "ver",
      "hierarchyLevelIdName": "Vertical",
      "list": [
        {
          "id": "66dec9af671cdae0895077ee",
          "nodeId": "ver_unique_001",
          "nodeName": "Technology Vertical",
          "nodeDisplayName": "Vertical 1",
          "hierarchyLevelId": "ver",
          "parentId": "bu_unique_001",
          "createdDate": "2024-08-28T10:17:44"
        }
      ]
    },
    {
      "hierarchyLevelId": "acc",
      "hierarchyLevelIdName": "Account",
      "list": [
        {
          "id": "66dec9af671cdae0895077f0",
          "nodeId": "acc_unique_001",
          "nodeName": "Global Tech Account",
          "nodeDisplayName": "Account 1",
          "hierarchyLevelId": "acc",
          "parentId": "ver_unique_001",
          "createdDate": "2024-08-28T10:17:44"
        },
      ]
    },
    {
      "hierarchyLevelId": "port",
      "hierarchyLevelIdName": "Engagement",
      "list": [
        {
          "id": "66dec9af671cdae0895077f2",
          "nodeId": "eng_unique_001",
          "nodeName": "Sample Engagement",
          "nodeDisplayName": "Engagement 1",
          "hierarchyLevelId": "port",
          "parentId": "acc_unique_001",
          "createdDate": "2024-08-28T10:17:44"
        },
      ]
    },
    {
      "hierarchyLevelId": "project",
      "hierarchyLevelIdName": "Project",
      "list": [
        {
          "id": "66e294d3aa845a08e16f7889",
          "nodeId": "project_unique_001",
          "nodeName": "Local Project",
          "nodeDisplayName": "Project 1",
          "hierarchyLevelId": "project",
          "parentId": "eng_unique_001",
          "createdDate": "2024-09-12T12:44:27",
          "modifiedDate": "2024-09-12T12:44:27"
        },
      ]
    }
  ];

  const successResponse = {
    serviceResponse: {
      message: 'Added Successfully.',
      success: true,
      data: {
        id: '6335497f67af3f41656b7b42',
        projectName: 'Test44',
        createdAt: '2022-09-29T13:00:07',
        kanban: false,
        hierarchy: [
          {
            hierarchyLevel: {
              level: 1,
              hierarchyLevelId: 'country',
              hierarchyLevelName: 'Country'
            },
            value: 'Canada'
          },
          {
            hierarchyLevel: {
              level: 2,
              hierarchyLevelId: 'state',
              hierarchyLevelName: 'State'
            },
            value: 'Ontario'
          },
          {
            hierarchyLevel: {
              level: 3,
              hierarchyLevelId: 'city',
              hierarchyLevelName: 'City'
            },
            value: 'Ottawa'
          }
        ],
        isKanban: false
      }
    },
    projectsAccess: []
  };

  beforeEach(async () => {
    const httpServiceSpy = jasmine.createSpyObj('HttpService', ['addBasicConfig', 'getOrganizationHierarchy']);
    const sharedServiceSpy = jasmine.createSpyObj('SharedService', ['getSelectedProject', 'getProjectList', 'setSelectedProject', 'setSelectedFieldMapping']);
    const authServiceSpy = jasmine.createSpyObj('GetAuthorizationService', ['checkIfSuperUser', 'checkIfProjectAdmin']);
    const messageServiceSpy = jasmine.createSpyObj('MessageService', ['add']);
    const gaServiceSpy = jasmine.createSpyObj('GoogleAnalyticsService', ['createProjectData']);

    await TestBed.configureTestingModule({
      declarations: [BasicConfigComponent],
      providers: [
        UntypedFormBuilder,
        { provide: HttpService, useValue: httpServiceSpy },
        { provide: SharedService, useValue: sharedServiceSpy },
        { provide: GetAuthorizationService, useValue: authServiceSpy },
        { provide: MessageService, useValue: messageServiceSpy },
        { provide: GoogleAnalyticsService, useValue: gaServiceSpy },
        { provide: Router, useValue: {} },
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(BasicConfigComponent);
    component = fixture.componentInstance;
    httpService = TestBed.inject(HttpService) as jasmine.SpyObj<HttpService>;
    sharedService = TestBed.inject(SharedService) as jasmine.SpyObj<SharedService>;
    authService = TestBed.inject(GetAuthorizationService) as jasmine.SpyObj<GetAuthorizationService>;
    messageService = TestBed.inject(MessageService) as jasmine.SpyObj<MessageService>;
    gaService = TestBed.inject(GoogleAnalyticsService) as jasmine.SpyObj<GoogleAnalyticsService>;

    // Mock return for getOrganizationHierarchy
    httpService.getOrganizationHierarchy.and.returnValue(of({ data: [] }));

    sharedService.setSelectedFieldMapping.and.returnValue(null)
  });

  it('should create component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize component variables on ngOnInit', () => {
    authService.checkIfSuperUser.and.returnValue(true);
    sharedService.getSelectedProject.and.returnValue({ id: 1, name: 'Test Project' });
    component.ngOnInit();
    expect(component.isProjectSetupPopup).toBeTrue();
    expect(component.breadcrumbs.length).toBeGreaterThan(0);
    expect(component.steps).toBeDefined();
    expect(component.ifSuperUser).toBeTrue();
  });

  it('should add controls to form in getFields method', () => {
    const mockData = [{
      hierarchyLevelId: 'kanban',
      inputType: 'switch',
      value: false
    }];
    spyOn(localStorage, 'getItem').and.returnValue(JSON.stringify(mockData));

    component.getFields();
    expect(component.form.contains('kanban')).toBeTrue();
    expect(component.form.controls['kanban'].value).toBeFalse();
  });

  it('should filter suggestions based on query in search method', () => {
    const mockEvent = { query: 'Test' };
    const mockField = {
      list: [{ nodeDisplayName: 'Test Node' }, { nodeDisplayName: 'Another Node' }],
      filteredSuggestions: []
    };
    component.search(mockEvent, mockField);
    expect(mockField.filteredSuggestions.length).toBe(1);
    expect(mockField.filteredSuggestions[0].nodeDisplayName).toBe('Test Node');
  });

  it('should filter hierarchy levels above and below the selected item in onSelectOfDropdown method', () => {
    const currentLevel = {
      hierarchyLevelId: 'project',
      list: [{ nodeId: '123', parentId: '456' }],
      filteredSuggestions: []
    };
    component.formData = [currentLevel, { hierarchyLevelId: 'parent', list: [] }];
    component.onSelectOfDropdown({ nodeId: '123', parentId: '456' }, currentLevel);
    expect(currentLevel.filteredSuggestions.length).toBe(0);
  });

  // ------------------------- OnSubmit -------------------------

  // it('should submit config when superadmin', () => {
  //   component.form = new UntypedFormGroup({
  //     projectName: new UntypedFormControl('', [component.stringValidator]),
  //     country: new UntypedFormControl('', [component.stringValidator]),
  //     state: new UntypedFormControl('', [component.stringValidator]),
  //     city: new UntypedFormControl('', [component.stringValidator]),
  //     kanban: new UntypedFormControl(false),
  //     assigneeDetails: new UntypedFormControl(false)
  //   });
  //   component.getFieldsResponse = [...hierarchyData];
  //   Object.keys(formValue).forEach((key) => {
  //     component.form.controls[key].setValue(formValue[key]);
  //   });
  //   component.blocked = true;
  //   component.selectedProject = {};
  //   spyOn(httpService, 'addBasicConfig').and.returnValue(of(successResponse))
  //   spyOn(sharedService, 'setSelectedProject');
  //   component.ifSuperUser = true;
  //   const spy = spyOn(messageService, 'add');
  //   spyOn(gaService, 'createProjectData');
  //   spyOn(component, 'getFields');
  //   component.onSubmit();
  //   expect(component.form.valid).toBeTruthy();
  //   expect(spy).toHaveBeenCalled();
  //   expect(component.blocked).toBeFalse();
  // });

  // it('should submit config when not superadmin', () => {
  //   component.form = new UntypedFormGroup({
  //     projectName: new UntypedFormControl('', [component.stringValidator]),
  //     country: new UntypedFormControl('', [component.stringValidator]),
  //     state: new UntypedFormControl('', [component.stringValidator]),
  //     city: new UntypedFormControl('', [component.stringValidator]),
  //     kanban: new UntypedFormControl(false),
  //     assigneeDetails: new UntypedFormControl(false)
  //   });
  //   component.getFieldsResponse = [...hierarchyData];
  //   Object.keys(formValue).forEach((key) => {
  //     component.form.controls[key].setValue(formValue[key]);
  //   });
  //   component.blocked = true;
  //   component.selectedProject = {};
  //   spyOn(httpService, 'addBasicConfig').and.returnValue(of(successResponse))
  //   spyOn(sharedService, 'setSelectedProject');
  //   component.ifSuperUser = false;
  //   spyOn(sharedService, 'setCurrentUserDetails');
  //   const spy = spyOn(messageService, 'add');
  //   spyOn(gaService, 'createProjectData');
  //   spyOn(component, 'getFields');
  //   component.onSubmit();
  //   expect(component.form.valid).toBeTruthy();
  //   expect(spy).toHaveBeenCalled();
  //   expect(component.blocked).toBeFalse();
  // });

  // it('should call HttpService on form submit and handle success response', () => {
  //   httpService.addBasicConfig.and.returnValue(of({
  //     serviceResponse: { success: true, data: { id: '1', projectName: 'Project A' } }
  //   }));
  //   spyOn(component, 'getFields').and.callThrough();

  //   component.onSubmit();

  //   expect(httpService.addBasicConfig).toHaveBeenCalled();
  //   expect(sharedService.setSelectedProject).toHaveBeenCalledWith(jasmine.objectContaining({
  //     id: '1', name: 'Project A'
  //   }));
  //   expect(messageService.add).toHaveBeenCalledWith(jasmine.objectContaining({
  //     severity: 'success'
  //   }));
  //   expect(component.isProjectSetupPopup).toBeFalse();
  //   expect(component.isProjectCOmpletionPopup).toBeTrue();
  //   expect(component.getFields).toHaveBeenCalled();
  // });

  // ------------------------- OnSubmit -------------------------

  it('should validate strings using stringValidator', () => {
    const control = { value: 'Valid123' } as AbstractControl;
    const result = component.stringValidator(control);
    expect(result).toBeNull();

    const invalidControl = { value: 'Invalid@123' } as AbstractControl;
    const invalidResult = component.stringValidator(invalidControl);
    expect(invalidResult).toEqual({ stringValidator: true });
  });
});
