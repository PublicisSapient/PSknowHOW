import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';

import { RecommendationsComponent } from './recommendations.component';
import { HttpService } from 'src/app/services/http.service';
import { of, throwError } from 'rxjs';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { APP_CONFIG, AppConfig } from 'src/app/services/app.config';
import { SharedService } from '../../services/shared.service';
import { MessageService } from 'primeng/api';

describe('RecommendationsComponent', () => {
  let component: RecommendationsComponent;
  let fixture: ComponentFixture<RecommendationsComponent>;
  let httpService: HttpService;
  let service: SharedService;
  let messageService: MessageService;
  const filterData = {
    "kpiIdList": ["kpi72"],
    "ids": [
      "xyz_123"
    ],
    "level": 6,
    "selectedMap": {
      "project": [],
      "sprint": ["xyz_123"]
    },
    "sprintIncluded": [
      "CLOSED"
    ],
    "label": "sprint"
  };
  const recommendationsRes = [{
      "projectId": "xyz",
      "sprintId": "xyz_123",
      "recommendations": [
          {
              "kpiId": "kpi14",
              "kpiName": "KPI name for kpi14",
              "maturity": 3,
              "recommendationSummary": "The project quality can be improved!",
              "recommendationDetails": "The last data has showed a decrease in the quality of the project for the last sprints!",
              "recommendationType": "Warnings",
              "filter": "Overall"
          },
          {
              "kpiId": "kpi35",
              "kpiName": "KPI name for kpi35",
              "maturity": 5,
              "recommendationSummary": "Nice job!",
              "recommendationDetails": "The team did a great job during the last sprints!",
              "recommendationType": "Good Practices",
              "filter": "Overall"
          },
          {
            "kpiId": "kpi11",
            "kpiName": "KPI name for kpi11",
            "maturity": 5,
            "recommendationSummary": "Nice job!",
            "recommendationDetails": "The team did a great job during the last sprints!",
            "recommendationType": "Critical",
            "filter": "Overall"
          },
        ]
    }]

  beforeEach(async () => {
    // service = new SharedService();
    await TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule
      ],
      declarations: [ RecommendationsComponent ],
      providers: [
        HttpService,
        { provide: SharedService, useValue: service },
        { provide: APP_CONFIG, useValue: AppConfig },
        MessageService
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    })
    .compileComponents();
    service = TestBed.inject(SharedService);
    messageService = TestBed.inject(MessageService);
    fixture = TestBed.createComponent(RecommendationsComponent);
    component = fixture.componentInstance;
    httpService = TestBed.inject(HttpService);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should get recommendations based on kpiIdList', fakeAsync(() => {
    component.selectedSprint = {};
    const sprintObj = { nodeId: 'xyz_123', 'nodeName':'xyz' };
    spyOn(service, 'getSprintForRnR').and.returnValue(sprintObj);
    component.filterData = filterData;
    component.displayModal = true;
    component.tabs = [];
    component.maturities = [];
    component.recommendationsData = [];
    spyOn(httpService, 'getRecommendations').and.returnValue(of(recommendationsRes))
    component.handleClick();
    tick();
    expect(component.tabs.length).toBe(3);
    expect(component.maturities.length).toBe(2);
    expect(component.recommendationsData.length).toBe(3);
  }))

  it('should display error message on error', fakeAsync(() => {
    component.filterData = filterData;
    // Arrange
    const errorMessage = 'Error in Kpi Column Configurations. Please try after sometime!';
    // spyOn(component, 'handleClick').and.callThrough();
    const spy = spyOn(httpService, 'getRecommendations').and.returnValue(throwError(errorMessage));
    const messageSpy = spyOn(messageService, 'add');

    // Act
    component.handleClick();
    tick();

    // Assert
    expect(spy).toHaveBeenCalled();
    expect(messageSpy).toHaveBeenCalledWith({ severity: 'error', summary: errorMessage });
  }));
});

