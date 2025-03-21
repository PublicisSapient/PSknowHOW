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
import { HelperService } from 'src/app/services/helper.service';
import { of } from 'rxjs';
import { ActivatedRoute } from '@angular/router';

describe('ProjectFilterComponent', () => {
  let component: ProjectFilterComponent;
  let fixture: ComponentFixture<ProjectFilterComponent>;
  let httpServiceMock: jasmine.SpyObj<HttpService>;
  let sharedServiceMock: jasmine.SpyObj<SharedService>;
  let messageServiceMock: jasmine.SpyObj<MessageService>;
  let helperMock: jasmine.SpyObj<HelperService>;

  beforeEach(async () => {
    httpServiceMock = jasmine.createSpyObj('HttpService', ['getAllProjects']);
    sharedServiceMock = jasmine.createSpyObj('SharedService', ['sendProjectData']);
    messageServiceMock = jasmine.createSpyObj('MessageService', ['add']);
    helperMock = jasmine.createSpyObj('HelperService', ['sortByField']);

    httpServiceMock.getAllProjects.and.returnValue(of({
      data: [
        {
          id: 'P1',
          hierarchy: [
            { hierarchyLevel: { hierarchyLevelId: 'Level1' }, value: 'Node 1' }
          ]
        }
      ]
    }));

    await TestBed.configureTestingModule({
      declarations: [ProjectFilterComponent],
      providers: [
        { provide: HttpService, useValue: httpServiceMock },
        { provide: SharedService, useValue: sharedServiceMock },
        { provide: MessageService, useValue: messageServiceMock },
        { provide: HelperService, useValue: helperMock },
        { provide: ActivatedRoute, useValue: { queryParams: of({ clone: 'false' }) } }
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectFilterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should call getProjects() on init', () => {
    spyOn(component, 'getProjects');
    component.ngOnInit();
    expect(component.getProjects).toHaveBeenCalled();
  });

  it('should fetch projects and set data in getProjects()', () => {
    const mockProjectData = {
      data: [
        {
          id: 'P1',
          hierarchy: [
            { hierarchyLevel: { hierarchyLevelId: 'Level1' }, value: 'Node 1' }
          ]
        }
      ]
    };

    httpServiceMock.getAllProjects.and.returnValue(of(mockProjectData));

    component.getProjects();
    expect(component.data.length).toBe(1);
    expect(component.hierarchyArray).toEqual(['Level1']);
    expect(sharedServiceMock.sendProjectData).toHaveBeenCalledWith(mockProjectData.data);
  });

  it('should show error message when getProjects() fails', () => {
    httpServiceMock.getAllProjects.and.returnValue(of({ error: true }));

    component.getProjects();
    expect(messageServiceMock.add).toHaveBeenCalledWith({
      severity: 'error',
      summary: 'User needs to be assigned a project for the access to work on dashboards.'
    });
  });

  it('should remove filter from selectedVal in filterData()', () => {
    const eventMock = { stopPropagation: () => {} };
    spyOn(eventMock, 'stopPropagation');

    component.selectedVal['Level1'] = [{ name: 'Node 1', code: 'L1', parent: 'Parent1' }];

    component.filterData(eventMock, 'Level1', 'L1', 'Node 1', 'Parent1');

    expect(eventMock.stopPropagation).toHaveBeenCalled();
    expect(component.selectedVal['Level1']).toBeUndefined();
  });

  it('should clear all filters in clearFilters()', () => {
    component.selectedVal = { Level1: [{ name: 'Node 1', code: 'L1' }] };
    component.projects = [{ id: 'P1', hierarchy: [] }];

    component.clearFilters();

    expect(component.selectedVal).toEqual({});
    expect(component.projects.length).toBe(1);
  });

  it('should sort selectedVal in sortFilters()', () => {
    component.selectedVal = {
      Level2: [{ name: 'Node 2', code: 'L2' }],
      Level1: [{ name: 'Node 1', code: 'L1' }]
    };
    component.hierarchyArray = ['Level1', 'Level2'];

    component.sortFilters();

    expect(Object.keys(component.selectedVal)).toEqual(['Level1', 'Level2']);
  });

  it('should emit project selection event in projectSelected()', () => {
    spyOn(component.projectSelectedEvent, 'emit');

    component.hierarchyArray = ['Level1'];
    component.selectedVal['Level1'] = [{ name: 'Node 1', code: 'L1' }];
    component.valueRemoved = { removedKey: 'Some Value' };

    component.projectSelected();

    expect(component.projectSelectedEvent.emit).toHaveBeenCalledWith(jasmine.objectContaining({
      accessType: 'Level1',
      value: [{ itemId: 'L1', itemName: 'Node 1' }],
      hierarchyArr: ['Level1'],
      valueRemoved: { removedKey: 'Some Value' }
    }));
  });

  it('should return selected template values in getSelectedValTemplateValue()', () => {
    component.selectedVal['Level1'] = [{ name: 'Node 1' }, { name: 'Node 2' }];

    const result = component.getSelectedValTemplateValue('Level1');

    expect(result).toBe('Node 1, Node 2');
  });

  it('should return true when hierarchy matches', () => {
    component.selectedVal = {
      Level1: [{ code: 'L1', name: 'Node 1' }],
      Level2: [{ code: 'L2', name: 'Node 2' }]
    };

    const project = {
      hierarchy: [
        { hierarchyLevel: { hierarchyLevelId: 'Level1' }, orgHierarchyNodeId: 'L1', value: 'Node 1' },
        { hierarchyLevel: { hierarchyLevelId: 'Level2' }, orgHierarchyNodeId: 'L2', value: 'Node 2' }
      ]
    };

    expect(component.hierarchyMatch(project)).toBeTrue();
  });

  it('should return false when no hierarchy matches', () => {
    component.selectedVal = {
      Level1: [{ code: 'L3', name: 'Node 3' }]
    };

    const project = {
      hierarchy: [
        { hierarchyLevel: { hierarchyLevelId: 'Level1' }, orgHierarchyNodeId: 'L1', value: 'Node 1' }
      ]
    };

    expect(component.hierarchyMatch(project)).toBeFalse();
  });
  it('should return false when project hierarchy is empty', () => {
    component.selectedVal = {
      Level1: [{ code: 'L1', name: 'Node 1' }]
    };

    const project = { hierarchy: [] };

    expect(component.hierarchyMatch(project)).toBeFalse();
  });
  it('should return false when hierarchyLevelId is missing', () => {
    component.selectedVal = {
      Level1: [{ code: 'L1', name: 'Node 1' }]
    };

    const project = {
      hierarchy: [
        { hierarchyLevel: {}, orgHierarchyNodeId: 'L1', value: 'Node 1' }
      ]
    };

    expect(component.hierarchyMatch(project)).toBeFalse();
  });
  it('should return false when selectedVal is empty', () => {
    component.selectedVal = {};

    const project = {
      hierarchy: [
        { hierarchyLevel: { hierarchyLevelId: 'Level1' }, orgHierarchyNodeId: 'L1', value: 'Node 1' }
      ]
    };

    expect(component.hierarchyMatch(project)).toBeFalse();
  });

  describe('ProjectFilterComponent - findUniques()', () => {
    let component: ProjectFilterComponent;
    let mockHelperService: jasmine.SpyObj<HelperService>;
  
    beforeEach(() => {
      mockHelperService = jasmine.createSpyObj('HelperService', ['sortByField']);
      component = new ProjectFilterComponent(null, null, null, mockHelperService);
  
      // Mock implementation of sortByField
      mockHelperService.sortByField.and.callFake((data, field) => {
        return data.sort((a, b) => (a[field] > b[field] ? 1 : -1));
      });
    });
  
    it('should return unique objects based on the given properties', () => {
      const data = [
        { name: 'Alpha', code: 'A1', parent: 'P1' },
        { name: 'Beta', code: 'B1', parent: 'P2' },
        { name: 'Alpha', code: 'A1', parent: 'P1' }, // Duplicate
        { name: 'Gamma', code: 'G1', parent: 'P3' }
      ];
      const propertyArray = ['name', 'code'];
  
      const result = component.findUniques(data, propertyArray);
  
      expect(result.length).toBe(3);
      expect(result).toEqual([
        { name: 'Alpha', code: 'A1' },
        { name: 'Beta', code: 'B1' },
        { name: 'Gamma', code: 'G1' }
      ]);
    });
  
    it('should return an empty array when input data is empty', () => {
      const data = [];
      const propertyArray = ['name', 'code'];
  
      const result = component.findUniques(data, propertyArray);
  
      expect(result).toEqual([]);
    });
  
    it('should handle large datasets efficiently', () => {
      const data = Array.from({ length: 1000 }, (_, i) => ({
        name: `Item ${i % 10}`,
        code: `Code ${i % 10}`,
        parent: `Parent ${i % 5}`
      }));
      const propertyArray = ['name', 'code'];
  
      const result = component.findUniques(data, propertyArray);
  
      expect(result.length).toBe(10);
    });
  
  });

  it('should populate hierarchyData correctly in populateDataLists()', () => {
    const mockData = [
      {
        id: '1',
        hierarchy: [
          {
            hierarchyLevel: { hierarchyLevelId: 'level1', hierarchyLevelName: 'Level 1' },
            orgHierarchyNodeId: 'node1',
            value: 'Value 1'
          },
          {
            hierarchyLevel: { hierarchyLevelId: 'level2', hierarchyLevelName: 'Level 2' },
            orgHierarchyNodeId: 'node2',
            value: 'Value 2'
          }
        ]
      },
      {
        id: '2',
        hierarchy: [
          {
            hierarchyLevel: { hierarchyLevelId: 'level1', hierarchyLevelName: 'Level 1' },
            orgHierarchyNodeId: 'node3',
            value: 'Value 3'
          }
        ]
      }
    ];

    spyOn(component, 'findUniques').and.callFake((data) => data);

    component.populateDataLists(mockData, 'all');

    expect(component.hierarchyData['level1'].length).toBe(2);
    expect(component.hierarchyData['level2'].length).toBe(1);
    expect(component.hierarchyData['level1'][0]).toEqual({ name: 'Value 1', code: 'node1', parent: '' });
    expect(component.hierarchyData['level2'][0]).toEqual({ name: 'Value 2', code: 'node2', parent: '(Value 1)' });
  });

  it('should update selectedValProjects correctly in populateDataLists()', () => {
    const mockData = [
      { id: '1', hierarchy: [{ hierarchyLevel: { hierarchyLevelId: 'level1' }, orgHierarchyNodeId: 'node1', value: 'Value 1' }] },
      { id: '2', hierarchy: [{ hierarchyLevel: { hierarchyLevelId: 'level2' }, orgHierarchyNodeId: 'node2', value: 'Value 2' }] }
    ];
    
    component.selectedValProjects = [{ id: '1' }];

    component.populateDataLists(mockData, 'all');

    expect(component.selectedValProjects.length).toBe(1);
  });

  it('should correctly process hierarchyMatch()', () => {
    component.selectedVal = {
      Level1: [{ code: 'L1', name: 'Node 1' }],
      Level2: [{ code: 'L2', name: 'Node 2' }]
    };

    const project = {
      hierarchy: [
        { hierarchyLevel: { hierarchyLevelId: 'Level1' }, orgHierarchyNodeId: 'L1', value: 'Node 1' },
        { hierarchyLevel: { hierarchyLevelId: 'Level2' }, orgHierarchyNodeId: 'L2', value: 'Node 2' }
      ]
    };

    expect(component.hierarchyMatch(project)).toBeTrue();
  });

  it('should return false in hierarchyMatch() when selectedVal is empty', () => {
    component.selectedVal = {};
    const project = {
      hierarchy: [
        { hierarchyLevel: { hierarchyLevelId: 'Level1' }, orgHierarchyNodeId: 'L1', value: 'Node 1' }
      ]
    };

    expect(component.hierarchyMatch(project)).toBeFalse();
  });

  it('should return false in hierarchyMatch() when hierarchy is empty', () => {
    component.selectedVal = { Level1: [{ code: 'L1', name: 'Node 1' }] };
    const project = { hierarchy: [] };

    expect(component.hierarchyMatch(project)).toBeFalse();
  });

  it('should return empty array in findUniques() when input is empty', () => {
    console.log('spal ****',component.findUniques([], ['name', 'code']))
    expect(component.findUniques([], ['name', 'code'])).toBeUndefined();
  });

  describe('ProjectFilterComponent - filterData()', () => {
    let component: ProjectFilterComponent;
    let mockEvent: any;
  
    beforeEach(() => {
      fixture = TestBed.createComponent(ProjectFilterComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
      mockEvent = { stopPropagation: jasmine.createSpy('stopPropagation') };
  
      component.hierarchyData = {
        Level1: [{ name: 'Node 1', code: 'L1', parent: 'Parent1' }],
        Level2: [{ name: 'Node 2', code: 'L2', parent: 'Parent2' }]
      };
      component.hierarchyArray = ['Level1', 'Level2'];
      component.selectedVal = {};
      component.filteredData = [];
      component.data = [
        { id: 'P1', hierarchy: [{ hierarchyLevel: { hierarchyLevelId: 'Level1' }, orgHierarchyNodeId: 'L1', value: 'Node 1' }] },
        { id: 'P2', hierarchy: [{ hierarchyLevel: { hierarchyLevelId: 'Level2' }, orgHierarchyNodeId: 'L2', value: 'Node 2' }] }
      ];
    });
  
    it('should remove a filter when already selected', () => {
      component.selectedVal['Level1'] = [{ name: 'Node 1', code: 'L1', parent: 'Parent1' }];
      
      component.filterData(mockEvent, 'Level1', 'L1', 'Node 1', 'Parent1');
  
      expect(component.selectedVal['Level1']).toBeUndefined();
    });
  
    it('should handle undefined selectedVal[filterType] gracefully', () => {
      component.selectedVal = {}; // Ensure selectedVal is empty
  
      expect(() => {
        component.filterData(mockEvent, 'NonExistingLevel', 'X1', 'Node X', 'ParentX');
      }).not.toThrow();
    });
  
    it('should call populateDataLists() correctly', () => {
      spyOn(component, 'populateDataLists');
  
      component.filterData(mockEvent, 'Level1', 'L1', 'Node 1', 'Parent1');
  
      expect(component.populateDataLists).toHaveBeenCalled();
    });
  
    it('should call clearFilters() when no filters are selected', () => {
      spyOn(component, 'clearFilters');
  
      component.selectedVal = { Level1: [{ name: 'Node 1', code: 'L1' }] };
      component.filterData(mockEvent, 'Level1', 'L1', 'Node 1', 'Parent1');
  
      expect(component.clearFilters).toHaveBeenCalled();
    });
  
  });
});
