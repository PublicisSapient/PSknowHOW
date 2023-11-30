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
import { ProjectListComponent } from './project-list.component';
import { HttpService } from '../../../services/http.service';
import { SharedService } from '../../../services/shared.service';
import { GetAuthorizationService } from '../../../services/get-authorization.service';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AppConfig, APP_CONFIG } from 'src/app/services/app.config';

import { TableModule } from 'primeng/table';
import { DropdownModule } from 'primeng/dropdown';
import { ToolbarModule } from 'primeng/toolbar';
import { Confirmation, ConfirmationService, MessageService } from 'primeng/api';
import { HelperService } from 'src/app/services/helper.service';

import { environment } from 'src/environments/environment';
import { of } from 'rxjs';
import { DatePipe } from '@angular/common';

describe('ProjectListComponent', () => {
  let component: ProjectListComponent;
  let fixture: ComponentFixture<ProjectListComponent>;
  let httpService: HttpService;
  let sharedService: SharedService;
  let httpMock;
  let router: Router;
  const baseUrl = environment.baseUrl;

  const projectListData = require('../../../../test/resource/projectListData.json');
  const formFieldData = [
    {
      level: 1,
      hierarchyLevelId: 'country',
      hierarchyLevelName: 'Country',
      suggestions: [
        'Canada',
        'India',
        'USA'
      ]
    },
    {
      level: 2,
      hierarchyLevelId: 'state',
      hierarchyLevelName: 'State',
      suggestions: [
        'Haryana',
        'Karnataka',
        'Ontario',
        'Texas',
        'Washinton'
      ]
    },
    {
      level: 3,
      hierarchyLevelId: 'city',
      hierarchyLevelName: 'City',
      suggestions: [
        'Bangalore',
        'Gurgaon',
        'Houston',
        'Kurukshetra',
        'Ottawa',
        'Remond',
        'Seattle'
      ]
    }
  ];
  const projectsAccess = [
    {
      role: 'ROLE_PROJECT_ADMIN',
      projects: [
        {
          projectName: 'FieldMappingTest',
          projectId: '6327fcb2106fed5ba66ad750',
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
          ]
        },
        {
          projectName: 'FieldMappingTest2',
          projectId: '63274333106fed5ba66ad748',
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
          ]
        },
        {
          projectName: 'FieldMappingTest3',
          projectId: '6328087d22a86a5df15e53e6',
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
          ]
        }
      ]
    }
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ProjectListComponent],
      imports: [
        RouterTestingModule,
        HttpClientTestingModule,
        TableModule,
        DropdownModule,
        ToolbarModule
      ],
      providers: [
        HttpService,
        SharedService,
        MessageService,
        ConfirmationService,
        GetAuthorizationService,
        { provide: APP_CONFIG, useValue: AppConfig },
        HelperService,
        DatePipe
      ]
    })
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectListComponent);
    component = fixture.componentInstance;
    httpService = TestBed.inject(HttpService);
    sharedService = TestBed.inject(SharedService);
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);

    let localStore = {};

    spyOn(window.localStorage, 'getItem').and.callFake((key) =>
      key in localStore ? localStore[key] : null
    );
    spyOn(window.localStorage, 'setItem').and.callFake(
      (key, value) => (localStore[key] = value + '')
    );
    spyOn(window.localStorage, 'clear').and.callFake(() => (localStore = {}));

  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  xit('should fetch project list data',() => {
    spyOn(httpService, 'getProjectListData').and.callThrough();
    spyOn(httpService, 'getHierarchyLevels').and.callThrough();
    component.ngOnInit();
    expect(httpService.getProjectListData).toHaveBeenCalledTimes(1);
    expect(httpService.getHierarchyLevels).toHaveBeenCalledTimes(1);
    const basicConfigsReq = httpMock.expectOne(`${baseUrl}/api/basicconfigs`);
    const getHierarchyReq = httpMock.expectOne(`${baseUrl}/api/hierarchylevels`);
    expect(basicConfigsReq.request.method).toBe('GET');
    expect(getHierarchyReq.request.method).toBe('GET');
    basicConfigsReq.flush(projectListData);
    getHierarchyReq.flush(formFieldData);
    expect(Object.keys(component.projectList)).toEqual(Object.keys(projectListData.data));
  });

  it('should navigate to basic-config on click of "New"', () => {
    const navigateSpy = spyOn(router, 'navigate');
    component.newProject();
    expect(navigateSpy).toHaveBeenCalledWith(['./dashboard/Config/BasicConfig']);
  });

  it('should navigate to basic-config on click of "Edit"', () => {
    const navigateSpy = spyOn(router, 'navigate');
    const fakeProject =  {
      id: '631f394dcfef11709d7ddc7b',
      name: 'MAP',
      type: 'Scrum',
      country: 'India',
      state: 'Haryana',
      city: 'Gurgaon'
  };
    component.editProject(fakeProject);
    expect(sharedService.getSelectedProject()).toEqual(fakeProject);
    expect(navigateSpy).toHaveBeenCalledWith(['./dashboard/Config/BasicConfig']);
  });

  it('should delete project on click of "Delete"', () => {
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

    const confirmationService = TestBed.inject(ConfirmationService);

    const mockConfirm: any = spyOn<any>(confirmationService, 'confirm').and.callFake((confirmation: Confirmation) => confirmation.accept());
    component.deleteProject(project);
    expect(mockConfirm).toHaveBeenCalled();
    httpMock.expectOne(baseUrl + '/api/basicconfigs/631f394dcfef11709d7ddc7b').flush(deleteResponse);
    fixture.detectChanges();
  });

  it("should route on tool component on edit button ",()=>{

    component.cols = [
      {
        heading: 'Project',
        id: 'name',
      },
      {
        heading: 'Level Three',
        id: 'hierarchyLevelThree',
      },
      {
        heading: 'Level Two',
        id: 'hierarchyLevelTwo',
      },
      {
        heading: 'Level One',
        id: 'hierarchyLevelOne',
      },
      {
        heading: 'Type',
        id: 'type',
      },
    ];

    const fakeProject = {
      hierarchyLevelOne: 'T1',
      hierarchyLevelThree: 'T3',
      hierarchyLevelTwo: 'T2',
      id: '63b3f9098ec44416b3ce9699',
      name: 'JIRAPROJ',
      type: 'Scrum',
    };
    const navigateSpy = spyOn(router, 'navigate');
    spyOn(sharedService , 'setSelectedProject');
    component.editConfiguration(fakeProject);
    expect(sharedService.setSelectedProject).toHaveBeenCalled();
    expect(navigateSpy).toHaveBeenCalledOnceWith(['/dashboard/Config/ToolMenu']);
  })

  it("should get HierarchyLevels",()=>{
    spyOn(httpService,'getHierarchyLevels').and.returnValue(of(formFieldData));
    component.getHierarchy();
  })

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

  it("should project list zero while getting project list",()=>{
   
    spyOn(httpService,'getProjectListData').and.returnValue(of(projectListData.data));
    component.getData();
    expect(component.loading).toBeFalse();
    expect(component.projectList.length).toBe(0)
  })
});
