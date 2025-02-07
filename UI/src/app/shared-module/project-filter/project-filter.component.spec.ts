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
import { ProjectFilterComponent } from './project-filter.component';
import { HttpService } from '../../services/http.service';
import { SharedService } from '../../services/shared.service';
import { MessageService } from 'primeng/api';
import { of } from 'rxjs';

const allProjectsData = require('../../../test/resource/projectFilterAllProjects.json');

describe('ProjectFilterComponent', () => {
  let component: ProjectFilterComponent;
  let fixture: ComponentFixture<ProjectFilterComponent>;
  let httpService: jasmine.SpyObj<HttpService>;
  let sharedService: jasmine.SpyObj<SharedService>;
  let messageService: jasmine.SpyObj<MessageService>;

  beforeEach(async () => {
    const httpSpy = jasmine.createSpyObj('HttpService', ['getAllProjects']);
    const sharedSpy = jasmine.createSpyObj('SharedService', ['sendProjectData']);
    const messageSpy = jasmine.createSpyObj('MessageService', ['add']);

    await TestBed.configureTestingModule({
      declarations: [ProjectFilterComponent],
      providers: [
        { provide: HttpService, useValue: httpSpy },
        { provide: SharedService, useValue: sharedSpy },
        { provide: MessageService, useValue: messageSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ProjectFilterComponent);
    component = fixture.componentInstance;
    component.selectedVal = {
      level1: [{ name: 'Alpha' }, { name: 'Beta' }],
      level2: [{ name: 'Gamma' }]
    };
    component.data = [
      {
        id: '1',
        projectName: 'Project One',
        hierarchy: [{ hierarchyLevel: { hierarchyLevelId: 'level1' }, value: 'Value1' }]
      }
    ];
    component.hierarchyData = {
      level1: [{ code: 'Value1' }]
    };
    component.hierarchyArray = ['level1'];
    
    httpService = TestBed.inject(HttpService) as jasmine.SpyObj<HttpService>;
    sharedService = TestBed.inject(SharedService) as jasmine.SpyObj<SharedService>;
    messageService = TestBed.inject(MessageService) as jasmine.SpyObj<MessageService>;
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should populate dropdowns', () => {
    httpService.getAllProjects.and.returnValue(of(allProjectsData));
    component.getProjects();
    expect(httpService.getAllProjects).toHaveBeenCalledTimes(1);
    fixture.detectChanges();
    expect(component.data).toEqual(allProjectsData.data);
  });

  it('should fetch projects on init', () => {
    const mockProjects = {
      data: [
        {
          hierarchy: [
            { hierarchyLevel: { hierarchyLevelId: 'level1' }, value: 'Value1' }
          ]
        }
      ]
    };
    httpService.getAllProjects.and.returnValue(of(mockProjects));

    component.ngOnInit();

    expect(httpService.getAllProjects).toHaveBeenCalled();
    expect(component.data).toEqual(mockProjects.data);
    expect(component.hierarchyArray).toEqual(['level1']);
    expect(sharedService.sendProjectData).toHaveBeenCalledWith(mockProjects.data);
  });

  it('should handle error when fetching projects', () => {
    const mockError = { error: true };
    httpService.getAllProjects.and.returnValue(of(mockError));

    component.ngOnInit();

    expect(messageService.add).toHaveBeenCalledWith({
      severity: 'error',
      summary: 'User needs to be assigned a project for the access to work on dashboards.'
    });
  });

  it('should populate data lists correctly', () => {
    const mockData = [
      {
        hierarchy: [
          { hierarchyLevel: { hierarchyLevelId: 'level1' }, value: 'Value1', orgHierarchyNodeId: '1' }
        ]
      }
    ];

    component.populateDataLists(mockData, 'all');

    expect(component.hierarchyData['level1']).toEqual([{ name: 'Value1', code: '1' }]);
  });

  it('should emit projectSelected event with hierarchy values', () => {
    component.hierarchyArray = ['level1'];
    component.selectedVal = {
      level1: [{ code: '1', name: 'Value1' }]
    };
    spyOn(component.projectSelectedEvent, 'emit');

    component.projectSelected();

    expect(component.projectSelectedEvent.emit).toHaveBeenCalled();
  });

  it('should emit projectSelected event with project values', () => {
    component.selectedValProjects = [{ id: 'P1', projectName: 'Project One' }];
    spyOn(component.projectSelectedEvent, 'emit');

    component.projectSelected();

    expect(component.projectSelectedEvent.emit).toHaveBeenCalled();
  });

  it('should match hierarchy correctly', () => {
    component.selectedVal = {
      level1: [{ code: 'Value1' }]
    };
    const project = {
      hierarchy: [
        { hierarchyLevel: { hierarchyLevelId: 'level1' }, value: 'Value1' }
      ]
    };

    expect(component.hierarchyMatch(project)).toBeTrue();
  });

  it('should not match hierarchy if values differ', () => {
    component.selectedVal = {
      level1: [{ code: 'Value2' }]
    };
    const project = {
      hierarchy: [
        { hierarchyLevel: { hierarchyLevelId: 'level1' }, value: 'Value1' }
      ]
    };

    expect(component.hierarchyMatch(project)).toBeFalse();
  });

  it('should filter data when filterType is available', () => {
    component.valueRemoved = {};
    const event = {
      stopPropagation: jasmine.createSpy('stopPropagation')
    };
    component.data = allProjectsData.data;
    const filterType = 'hierarchyLevelOne';
    const filterValueCode = 'Sample One';
    const filterValueName = 'Sample One';
    component.filteredData = [];
    component.selectedVal = {};
    component.filterData(event, filterType, filterValueCode, filterValueName);
    expect(component.filteredData).toEqual(component.data);
  });

  it('should return a comma-separated string of names for a valid hierarchy level', () => {
    const result = component.getSelectedValTemplateValue('level1');
    expect(result).toEqual('Alpha, Beta');
  });

  it('should return a single name for a hierarchy level with one entry', () => {
    const result = component.getSelectedValTemplateValue('level2');
    expect(result).toEqual('Gamma');
  });
// spal test
it('should add a new filter value', () => {
  const event = { stopPropagation: jasmine.createSpy('stopPropagation') };
  component.filterData(event, 'level1', 'Value1', 'Value1');
  expect(component.selectedVal['level1']).toEqual([{ name: 'Value1', code: 'Value1' }]);
  expect(event.stopPropagation).toHaveBeenCalled();
});

it('should remove an existing filter value', () => {
  component.selectedVal['level1'] = [{ name: 'Value1', code: 'Value1' }];
  const event = { stopPropagation: jasmine.createSpy('stopPropagation') };
  component.filterData(event, 'level1', 'Value1', 'Value1');
  expect(component.selectedVal['level1']).toBeUndefined();
});

it('should clear filters if no selected values', () => {
  const event = { stopPropagation: jasmine.createSpy('stopPropagation') };
  component.selectedVal = {};
  component.filterData(event, 'level1', 'Value1', 'Value1');
  expect(component.filtersApplied).toBe(true);
});

it('should apply filters and update filteredData', () => {
  component.selectedVal['level1'] = [{ name: 'Value1', code: 'Value1' }];
  const event = { stopPropagation: jasmine.createSpy('stopPropagation') };
  component.filterData(event, 'level1', 'Value1', 'Value1');
  expect(component.filteredData).toEqual(component.data);
});

it('should call projectSelected if filters are applied', () => {
  spyOn(component, 'projectSelected');
  component.selectedVal['level1'] = [{ name: 'Value1', code: 'Value1' }];
  const event = { stopPropagation: jasmine.createSpy('stopPropagation') };
  component.filterData(event, 'level1', 'Value1', 'Value1');
  expect(component.projectSelected).toHaveBeenCalled();
});

});
