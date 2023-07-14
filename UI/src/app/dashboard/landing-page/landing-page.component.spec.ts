import { HttpClientModule } from '@angular/common/http';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { environment } from 'src/environments/environment';
import { HttpService } from '../../services/http.service';
import { LandingPageComponent } from './landing-page.component';
import { APP_CONFIG, AppConfig } from '../../services/app.config';
import { of } from 'rxjs';
import { SharedService } from 'src/app/services/shared.service';

describe('LandingPageComponent', () => {
  let component: LandingPageComponent;
  let fixture: ComponentFixture<LandingPageComponent>;
  let httpMock;
  let httpService;
  const baseUrl = environment.baseUrl;  // Servers Env
  const fakeLandingInfo = require('../../../test/resource/fakeLandingInfo.json');
  const fakeFeedbackAreaList = require('../../../test/resource/fakeFeedbackAreaList.json');

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [LandingPageComponent],
      imports: [RouterTestingModule, HttpClientModule, HttpClientTestingModule],
      providers: [HttpService,SharedService, { provide: APP_CONFIG, useValue: AppConfig }]
    })
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(LandingPageComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    httpService = TestBed.inject(HttpService);
    fixture.detectChanges();
  });


  xit('should  return additional information', () => {
    component.ngOnInit();
    httpMock.expectOne(fakeLandingInfo).flush();
    expect(Object.keys(component.landingInfo).length).toBe(Object.keys(fakeLandingInfo.data).length);
  });

  xit('should  return categories for feedback', () => {
    component.ngOnInit();
    httpMock.expectOne(fakeFeedbackAreaList).flush();
    httpMock.expectOne(baseUrl + '/api/feedback/categories').flush(fakeLandingInfo);
    expect(component.area.length).toBe(fakeLandingInfo.data.length);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should set landing Info', fakeAsync(() => {
    const response = {
      data: 'adLogin: false'
    };
    const spy = spyOn(httpService, 'getLandingInfo').and.returnValue(of(response));
    component.getImpInfo();
    tick();
    expect(component.landingInfo).toBe(response.data);
  }));


  it('should set category Info', fakeAsync(() => {
    const response = {
      message: 'Found all feedback categories',
      success: true,
      data: [
        'EMM',
        'Additional KPI',
        'Tool Integration',
        'Admin',
        'UI',
        'Other'
      ]
    };
    const spy = spyOn(httpService, 'getFeedbackCategory').and.returnValue(of(response));
    component.getCategory();
    tick();
    expect(component.area).toEqual(response.data);
  }));


  it('should set TotalUsers Count', fakeAsync(() => {
    const response = {
      message: 'Found TotalUsers count',
      success: true,
      data: {
        'Total Users': 5,
        'New Users Added in last 30 days': 2
      }
    };
    const spy = spyOn(httpService, 'getUsersCount').and.returnValue(of(response));
    component.getTotalUsersCount();
    tick();
    expect(component.totalUsers).toEqual(response.data['Total Users']);
  }));

  it('should save feedback successfully', fakeAsync(() => {
      component.userName = "dummy name";
    const obj = {
      "feedbackType": "feedback",
      "category": "UI",
      "feedback": "test",
      "username": "SUPERADMIN"
    };
    const res = { "message": "Your request has been submitted", "success": true, "data": { "username": "SUPERADMIN", "feedback": "test", "category": "UI", "feedbackType": "feedback" } }
    spyOn(httpService, 'submitFeedbackData').and.returnValue(of(res));
    component.save();
    tick(3000);
    expect(component.isFeedbackSubmitted).toBe(true);
    expect(component.formMessage).toEqual('');
  }))
});
