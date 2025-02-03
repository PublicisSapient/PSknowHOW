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
import { AbstractControl, UntypedFormBuilder, UntypedFormGroup, UntypedFormControl } from '@angular/forms';
import { MessageService } from 'primeng/api';
import { HttpService } from '../../../services/http.service';
import { SharedService } from '../../../services/shared.service';
import { GetAuthorizationService } from '../../../services/get-authorization.service';
import { GoogleAnalyticsService } from '../../../services/google-analytics.service';
import { BasicConfigComponent } from './basic-config.component';
import { ActivatedRoute, Router } from '@angular/router';
import { BehaviorSubject, of, throwError } from 'rxjs';

describe('BasicConfigComponent', () => {
  let component: BasicConfigComponent;
  let fixture: ComponentFixture<BasicConfigComponent>;
  let httpService: jasmine.SpyObj<HttpService>;
  let sharedService: jasmine.SpyObj<SharedService>;
  let authService: jasmine.SpyObj<GetAuthorizationService>;
  let messageService: jasmine.SpyObj<MessageService>;
  let gaService: jasmine.SpyObj<GoogleAnalyticsService>;
  let mockActivatedRoute;
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
          "createdDate": "2024-08-28T10:17:44",
          "level": 1
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
          "createdDate": "2024-08-28T10:17:44",
          "level": 2
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
          "createdDate": "2024-08-28T10:17:44",
          "level": 3
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
          "createdDate": "2024-08-28T10:17:44",
          "level": 4
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
          "modifiedDate": "2024-09-12T12:44:27",
          "level": 5
        },
      ]
    }
  ];

  const formValue = {
    "kanban": false,
    "bu": {
      "level": 1,
      "hierarchyLevelName": "BU",
      "id": "66e16612d1d34875e9ebc8c6",
      "nodeId": "bu_unique_001",
      "nodeName": "Global Business Unit",
      "nodeDisplayName": "Business Unit 1",
      "hierarchyLevelId": "bu",
      "createdDate": "2024-08-28T10:17:44"
    },
    "ver": {
      "level": 2,
      "hierarchyLevelName": "Vertical",
      "id": "66e16612d1d34875e9ebc8c5",
      "nodeId": "ver_unique_011",
      "nodeName": "PS Internal",
      "nodeDisplayName": "Vertical 11",
      "hierarchyLevelId": "ver",
      "parentId": "bu_unique_001",
      "createdDate": "2024-08-28T10:17:44"
    },
    "acc": {
      "level": 3,
      "hierarchyLevelName": "Account",
      "id": "66fbe5afdcf09bd8a21f1400",
      "nodeId": "acc_unique_011",
      "nodeName": "Global sfsf Account",
      "nodeDisplayName": "Account 11",
      "hierarchyLevelId": "acc",
      "parentId": "ver_unique_011",
      "createdDate": "2024-08-28T10:17:44"
    },
    "port": {
      "level": 4,
      "hierarchyLevelName": "Engagement",
      "id": "66fbe5afdcf09bd8a21f13fc",
      "nodeId": "eng_unique_011",
      "nodeName": "Healthcare sds",
      "nodeDisplayName": "Engagement 11",
      "hierarchyLevelId": "port",
      "parentId": "acc_unique_011",
      "createdDate": "2024-08-28T10:17:44"
    },
    "project": "PSKnowHOW 1101",
    "assigneeDetails": false,
    "developerKpiEnabled": false
  };

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
    const httpServiceSpy = jasmine.createSpyObj('HttpService', ['addBasicConfig', 'getOrganizationHierarchy', 'setCurrentUserDetails']);
    const sharedServiceSpy = jasmine.createSpyObj('SharedService', ['getSelectedProject', 'getProjectList', 'setSelectedProject', 'setSelectedFieldMapping', 'getCurrentUserDetails', 'setProjectList']);
    const authServiceSpy = jasmine.createSpyObj('GetAuthorizationService', ['checkIfSuperUser', 'checkIfProjectAdmin']);
    const messageServiceSpy = jasmine.createSpyObj('MessageService', ['add']);
    const gaServiceSpy = jasmine.createSpyObj('GoogleAnalyticsService', ['createProjectData']);

    // Mock return values for service methods
    httpServiceSpy.addBasicConfig.and.returnValue(of(successResponse));
    httpServiceSpy.getOrganizationHierarchy.and.returnValue(of({ data: [] }));
    sharedServiceSpy.getSelectedProject.and.returnValue(of({ id: 1, name: 'Test Project' }));
    sharedServiceSpy.getCurrentUserDetails.and.returnValue(of('test_user'));

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
        { provide: ActivatedRoute, useValue: {} }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(BasicConfigComponent);
    component = fixture.componentInstance;
    httpService = TestBed.inject(HttpService) as jasmine.SpyObj<HttpService>;
    sharedService = TestBed.inject(SharedService) as jasmine.SpyObj<SharedService>;
    authService = TestBed.inject(GetAuthorizationService) as jasmine.SpyObj<GetAuthorizationService>;
    messageService = TestBed.inject(MessageService) as jasmine.SpyObj<MessageService>;
    gaService = TestBed.inject(GoogleAnalyticsService) as jasmine.SpyObj<GoogleAnalyticsService>;

    component.form = new UntypedFormBuilder().group({
      projectName: ['Test Project'],
      kanban: [true],
      assigneeDetails: [true],
      developerKpiEnabled: [true]
    });

    component.getFieldsResponse = [
      { hierarchyLevelId: 'bu', hierarchyLevelName: 'BU' },
      { hierarchyLevelId: 'ver', hierarchyLevelName: 'Vertical' },
      { hierarchyLevelId: 'project', hierarchyLevelName: 'Project' }
    ];

    spyOn(localStorage, 'getItem').and.callFake((key: string) => {
      if (key === 'completeHierarchyData') {
        return JSON.stringify({
          scrum: [
            { id: '1', hierarchyLevelId: 'bu', hierarchyLevelName: 'Business Unit' },
            { id: '2', hierarchyLevelId: 'ver', hierarchyLevelName: 'Vertical' }
          ]
        });
      }
      return null;
    });
    spyOn(localStorage, 'setItem');
    spyOn(component, 'getFields');
    // Mock return for getOrganizationHierarchy
    httpService.getOrganizationHierarchy.and.returnValue(of({ data: [] }));

    sharedService.setSelectedFieldMapping.and.returnValue(null);
    sharedService.getCurrentUserDetails.and.returnValue('test_user');
    sharedService.setProjectList.and.returnValue(null);
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
    (localStorage.getItem as jasmine.Spy).and.returnValue(JSON.stringify(mockData));

    component.getFields();
    expect(component.form.contains('kanban')).toBeTrue();
    expect(component.form.controls['kanban'].value).toBeTrue();
  });

  it('should filter suggestions based on query in search method', () => {
    const mockEvent = { query: 'Test' };
    const mockField = {
      list: [{ nodeDisplayName: 'Test Node' }, { nodeDisplayName: 'Another Node' }],
      filteredSuggestions: []
    };

    const currentLevel = {
      hierarchyLevelId: 'project',
      list:  [{ nodeDisplayName: 'Test Node' }, { nodeDisplayName: 'Another Node' }],
      filteredSuggestions: []
    };
    component.formData = [currentLevel, { hierarchyLevelId: 'parent', list: [] }];

    component.search(mockEvent, mockField, 2);
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

  it('should submit form data successfully', fakeAsync(() => {
    const mockResponse = {
      serviceResponse: {
        success: true,
        data: {
          id: '1',
          projectName: 'Test Project',
          kanban: true,
          saveAssigneeDetails: true,
          developerKpiEnabled: true,
          hierarchy: [{ hierarchyLevel: { hierarchyLevelName: 'BU' }, value: 'Sample BU' }]
        }
      }
    };

    httpService.addBasicConfig.and.returnValue(of(mockResponse));

    component.onSubmit();
    tick();

    expect(component.selectedProject).toEqual(jasmine.objectContaining({
      id: '1',
      name: 'Test Project',
      Type: 'Kanban',
      saveAssigneeDetails: true,
      developerKpiEnabled: true
    }));

    expect(sharedService.setSelectedProject).toHaveBeenCalledWith(component.selectedProject);
    expect(sharedService.setProjectList).toHaveBeenCalled();
    expect(messageService.add).toHaveBeenCalledWith({
      severity: 'success',
      summary: 'Project setup initiated',
      detail: ''
    });
    expect(gaService.createProjectData).toHaveBeenCalled();
  }));

  it('should handle form submission error', fakeAsync(() => {
    const mockErrorResponse = {
      serviceResponse: {
        success: false,
        message: 'Some error occurred'
      }
    };

    httpService.addBasicConfig.and.returnValue(of(mockErrorResponse));
    component.onSubmit();
    tick();

    expect(messageService.add).toHaveBeenCalledWith({
      severity: 'error',
      summary: 'Some error occurred'
    });
    expect(component.blocked).toBe(false);
  }));

  it('should handle network error during form submission', fakeAsync(() => {
    httpService.addBasicConfig.and.returnValue(throwError(() => new Error('Network error')));
    component.onSubmit();
    tick();

    expect(messageService.add).toHaveBeenCalledWith({
      severity: 'error',
      summary: 'Some error occurred. Please try again later.'
    });
    expect(component.blocked).toBe(false);
  }));

  it('should submit config when superadmin', () => {
    const mockResponse = {
      serviceResponse: {
        success: true,
        data: {
          id: '1',
          projectName: 'Test Project',
          kanban: true,
          saveAssigneeDetails: true,
          developerKpiEnabled: true,
          hierarchy: [{ hierarchyLevel: { hierarchyLevelName: 'BU' }, value: 'Sample BU' }]
        }
      }
    };
    component.form = new UntypedFormGroup({
      bu: new UntypedFormControl('', [component.stringValidator]),
      ver: new UntypedFormControl('', [component.stringValidator]),
      acc: new UntypedFormControl('', [component.stringValidator]),
      port: new UntypedFormControl('', [component.stringValidator]),
      project: new UntypedFormControl('', [component.stringValidator]),
      kanban: new UntypedFormControl(false),
      assigneeDetails: new UntypedFormControl(false),
      developerKpiEnabled: new UntypedFormControl(false)
    });
    component.getFieldsResponse = [...hierarchyData];
    Object.keys(formValue).forEach((key) => {
      component.form.controls[key].setValue(formValue[key]);
    });
    component.blocked = true;
    component.selectedProject = {};

    component.ifSuperUser = true;

    httpService.addBasicConfig.and.returnValue(of(mockResponse));
    component.onSubmit();
    expect(component.form.valid).toBeTruthy();

    expect(component.blocked).toBeFalse();
  });

  it('should submit config when not superadmin', () => {
    const mockResponse = {
      serviceResponse: {
        success: true,
        data: {
          id: '1',
          projectName: 'Test Project',
          kanban: true,
          saveAssigneeDetails: true,
          developerKpiEnabled: true,
          hierarchy: [{ hierarchyLevel: { hierarchyLevelName: 'BU' }, value: 'Sample BU' }]
        }
      }
    };
    component.form = new UntypedFormGroup({
      bu: new UntypedFormControl('', [component.stringValidator]),
      ver: new UntypedFormControl('', [component.stringValidator]),
      acc: new UntypedFormControl('', [component.stringValidator]),
      port: new UntypedFormControl('', [component.stringValidator]),
      project: new UntypedFormControl('', [component.stringValidator]),
      kanban: new UntypedFormControl(false),
      assigneeDetails: new UntypedFormControl(false),
      developerKpiEnabled: new UntypedFormControl(false)
    });
    component.getFieldsResponse = [...hierarchyData];
    Object.keys(formValue).forEach((key) => {
      component.form.controls[key].setValue(formValue[key]);
    });
    component.blocked = true;
    component.selectedProject = {};

    component.ifSuperUser = false;
    component.onSubmit();
    expect(component.form.valid).toBeTruthy();

    expect(component.blocked).toBeFalse();
  });

  // ------------------------- OnSubmit -------------------------

  it('should validate strings using stringValidator', () => {
    const control = { value: 'Valid123' } as AbstractControl;
    const result = component.stringValidator(control);
    expect(result).toBeNull();

    const invalidControl = { value: 'Invalid@123' } as AbstractControl;
    const invalidResult = component.stringValidator(invalidControl);
    expect(invalidResult).toEqual({ stringValidator: true });
  });

  // ----------------------- getHierarchy ------------------------------

  it('should parse and filter localStorage data and make an HTTP call', () => {
    const mockFormFieldData = {
      data: [
        { id: '1', nodeId: 'node_1', nodeName: 'Node 1', nodeDisplayName: 'Node Display 1', hierarchyLevelId: 'bu' },
        { id: '2', nodeId: 'node_2', nodeName: 'Node 2', nodeDisplayName: 'Node Display 2', hierarchyLevelId: 'ver' }
      ]
    };

    httpService.getOrganizationHierarchy.and.returnValue(of(mockFormFieldData));

    component.getHierarchy();

    expect(httpService.getOrganizationHierarchy).toHaveBeenCalled();

    const expectedHierarchyMap = {
      bu: 'Business Unit',
      ver: 'Vertical',
      project: 'Project'
    };
    const expectedTransformedData = [
      {
        "hierarchyLevelId": "bu",
        "hierarchyLevelIdName": "Business Unit",
        "level": 1,
        "list": [
          {
            "level": 1,
            "hierarchyLevelName": "Business Unit",
            "id": "1",
            "nodeId": "node_1",
            "nodeName": "Node 1",
            "nodeDisplayName": "Node Display 1",
            "hierarchyLevelId": "bu"
          }
        ]
      },
      {
        "hierarchyLevelId": "ver",
        "hierarchyLevelIdName": "Vertical",
        "level": 2,
        "list": [
          {
            "level": 2,
            "hierarchyLevelName": "Vertical",
            "id": "2",
            "nodeId": "node_2",
            "nodeName": "Node 2",
            "nodeDisplayName": "Node Display 2",
            "hierarchyLevelId": "ver"
          }
        ]
      },
      {
        "hierarchyLevelId": "project",
        "hierarchyLevelIdName": "Project",
        "level": 3,
        "list": []
      }
    ];

    expect(localStorage.setItem).toHaveBeenCalledWith('hierarchyData', JSON.stringify(expectedTransformedData, null, 2));
    expect(component.getFields).toHaveBeenCalled();
  });

  it('should handle empty localStorage data gracefully', () => {
    (localStorage.getItem as jasmine.Spy).and.returnValue(null);
    httpService.getOrganizationHierarchy.and.returnValue(of({ data: [] }));

    component.getHierarchy();

    expect(localStorage.getItem).toHaveBeenCalledWith('completeHierarchyData');
    expect(httpService.getOrganizationHierarchy).toHaveBeenCalled();
    expect(localStorage.setItem).toHaveBeenCalledWith('hierarchyData', JSON.stringify([], null, 2));
    expect(component.getFields).toHaveBeenCalled();
  });

  it('should add "Project" to the hierarchy map when data exists', () => {
    httpService.getOrganizationHierarchy.and.returnValue(of({ data: [] }));
    component.getHierarchy();

    // Retrieve arguments of the most recent call to setItem
    const hierarchyMap = JSON.parse((localStorage.setItem as jasmine.Spy).calls.mostRecent().args[1]);
    expect(hierarchyMap.some((item: any) => item.hierarchyLevelIdName === 'Project')).toBeTrue();
  });

  // ----------------------- getHierarchy ------------------------------
});
