import { ComponentFixture, TestBed } from '@angular/core/testing';
import {
  HttpClientTestingModule
} from '@angular/common/http/testing';
import { of } from 'rxjs';

import { CommentsComponent } from './comments.component';
import { SharedService } from 'src/app/services/shared.service';
import { HttpService } from 'src/app/services/http.service';
import { AppConfig, APP_CONFIG } from 'src/app/services/app.config';

describe('CommentsComponent', () => {
  let component: CommentsComponent;
  let fixture: ComponentFixture<CommentsComponent>;
  let sharedService: SharedService;
  let http_service: HttpService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      declarations: [ CommentsComponent ],
      providers: [SharedService, HttpService, { provide: APP_CONFIG, useValue: AppConfig }]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CommentsComponent);
    component = fixture.componentInstance;
    sharedService = TestBed.inject(SharedService);
    http_service = TestBed.inject(HttpService);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should get comments', () => {
    const response = {
      message: "Found comments",
      success: true,
      data: {
        node: "DOTC_63b51633f33fd2360e9e72bd",
        sprintId: "",
        kpiId: "kpi118",
        CommentsInfo: [{
          commentId: "43514629-78a0-4a3c-bf87-82f89c036f04",
          commentBy: "SUPERADMIN",
          commentOn: "2023-03-13 13:23:34",
          comment: "test1"
        }]
      }
    }
    spyOn(http_service, 'getComment').and.returnValue(of(response));
    component.getComments();
    fixture.detectChanges();
    expect(component.commentsList).toEqual(response.data.CommentsInfo);
  });

  it('should submit comments', () => {
    const response = {
      message: "Your comment is submitted successfully.",
      success: true,
      data: {
        node: "DOTC_63b51633f33fd2360e9e72bd",
        level: "4",
        sprintId: "",
        commentsKpiWise: [{
          kpiId: "kpi14",
          commentsInfo: [{
            commentId: "6138f970-1243-4470-b8fb-781787e5713c",
            commentBy: "SUPERADMIN",
            commentOn: "2023-03-13 14:03:33",
            comment: "test 1"
          }]
        }]
      }
    }
    spyOn(http_service, 'submitComment').and.returnValue(of(response));
    component.submitComment({ nodeId: '', level: '' });
    fixture.detectChanges();
    expect(component.commentText).toBe('');
  });

  it('should open comments', () => {
    const sharedObj = {
      "filterData": [
        {
          "nodeId": "DOTC_63b51633f33fd2360e9e72bd",
          "nodeName": "DOTC",
          "path": [
            "D3_hierarchyLevelThree###D2_hierarchyLevelTwo###D1_hierarchyLevelOne"
          ],
          "labelName": "project",
          "parentId": [
            "D3_hierarchyLevelThree"
          ],
          "level": 4,
          "basicProjectConfigId": "63b51633f33fd2360e9e72bd"
        }
      ],
      "filterApplyData": {
        "ids": [
          "DOTC_63b51633f33fd2360e9e72bd"
        ],
        "selectedMap": {
          "project": [
            "DOTC_63b51633f33fd2360e9e72bd"
          ],
          "sprint": ["DOTC_63b51633f33fd2360e9e72bd"],
          "afOne": []
        },
        "level": 4
      }
    }
    const testData = sharedObj.filterData[1];
    spyOn(sharedService, 'getFilterObject').and.returnValue(sharedObj);
    component.openComments();
    fixture.detectChanges();
    expect(component.selectedFilters).toEqual(sharedObj.filterData);

    spyOn(sharedService, 'getSelectedTab').and.returnValue('Iteration');
    component.openComments();
    fixture.detectChanges();
    expect(component.selectedFilters).toEqual(sharedObj.filterData);
  });
});
