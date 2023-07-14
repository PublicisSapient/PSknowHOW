import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { APP_CONFIG, AppConfig } from '../../services/app.config';
import { HttpService } from '../../services/http.service';
import { SharedService } from '../../services/shared.service';
import { MessageService } from 'primeng/api';
import { ProjectFilterComponent } from './project-filter.component';
import { MultiSelectModule } from 'primeng/multiselect';
import { environment } from 'src/environments/environment';

const allProjectsData = require('../../../test/resource/projectFilterAllProjects.json');
const filteredData = [
  {
      id: '6335363749794a18e8a4479b',
      projectName: 'Scrum Project',
      hierarchy: [
          {
              hierarchyLevel: {
                  level: 1,
                  hierarchyLevelId: 'hierarchyLevelOne',
                  hierarchyLevelName: 'Level One'
              },
              value: 'Sample One'
          },
          {
              hierarchyLevel: {
                  level: 2,
                  hierarchyLevelId: 'hierarchyLevelTwo',
                  hierarchyLevelName: 'Level Two'
              },
              value: 'Sample Two'
          },
          {
              hierarchyLevel: {
                  level: 3,
                  hierarchyLevelId: 'hierarchyLevelThree',
                  hierarchyLevelName: 'Level Three'
              },
              value: 'Sample Three'
          }
      ]
  },
  {
      id: '6335368249794a18e8a4479f',
      projectName: 'Kanban Project',
      hierarchy: [
          {
              hierarchyLevel: {
                  level: 1,
                  hierarchyLevelId: 'hierarchyLevelOne',
                  hierarchyLevelName: 'Level One'
              },
              value: 'Sample One'
          },
          {
              hierarchyLevel: {
                  level: 2,
                  hierarchyLevelId: 'hierarchyLevelTwo',
                  hierarchyLevelName: 'Level Two'
              },
              value: 'Sample Two'
          },
          {
              hierarchyLevel: {
                  level: 3,
                  hierarchyLevelId: 'hierarchyLevelThree',
                  hierarchyLevelName: 'Level Three'
              },
              value: 'Sample Three'
          }
      ]
  }
];

describe('ProjectFilterComponent', () => {
  let component: ProjectFilterComponent;
  let fixture: ComponentFixture<ProjectFilterComponent>;
  let httpService: HttpService;
  let sharedService: SharedService;
  let httpMock;
  const baseUrl = environment.baseUrl;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ProjectFilterComponent],
      imports: [RouterTestingModule, HttpClientTestingModule, MultiSelectModule],
      providers: [HttpService, SharedService, MessageService
        , { provide: APP_CONFIG, useValue: AppConfig }]
    })
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectFilterComponent);
    component = fixture.componentInstance;
    httpService = TestBed.inject(HttpService);
    sharedService = TestBed.inject(SharedService);
    httpMock = TestBed.inject(HttpTestingController);
    // fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should populate dropdowns', () => {
    spyOn(httpService, 'getAllProjects').and.callThrough();
    component.getProjects();
    expect(httpService.getAllProjects).toHaveBeenCalledTimes(1);
    const allProjectsReq = httpMock.expectOne(baseUrl + '/api/basicconfigs/all');
    expect(allProjectsReq.request.method).toBe('GET');
    allProjectsReq.flush(allProjectsData);
    expect(component.data).toEqual(allProjectsData.data);
  });

  it('should clear filters on click of Clear Filters button', () => {
    component.data = allProjectsData.data;
    component.clearFilters();
    fixture.detectChanges();
    expect(component.filtersApplied).toBeFalsy();
    expect(component.filters).toEqual({});
    expect(component.selectedValProjects).toEqual([]);
  });

  it('should filter data', () => {
    spyOn(httpService, 'getAllProjects').and.callThrough();
    component.getProjects();
    expect(httpService.getAllProjects).toHaveBeenCalledTimes(1);
    const allProjectsReq = httpMock.expectOne(baseUrl + '/api/basicconfigs/all');
    expect(allProjectsReq.request.method).toBe('GET');
    allProjectsReq.flush(allProjectsData);
    expect(component.data).toEqual(allProjectsData.data);

    component.selectedVal = {};
    component.valueRemoved ={};
    fixture.detectChanges();
    const fType = 'hierarchyLevelOne';
    const fValue = 'Sample One';
    const event = {
      isTrusted: true,
      stopPropagation: () => {}
  };
    component.filterData(event, fType, fValue);
    fixture.detectChanges();
    expect(component.selectedVal).toEqual({
      hierarchyLevelOne: [
        {
          name: 'Sample One',
          code: 'Sample One'
        }
      ]
    });
    expect(component.filteredData).toEqual(filteredData);
  });
});
