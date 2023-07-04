import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpService } from '../services/http.service';
import { FeedbackComponent } from './feedback.component';
import { of } from 'rxjs';
import { APP_CONFIG, AppConfig } from '../services/app.config';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientModule } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { SharedService } from '../services/shared.service';

describe('FeedbackComponent', () => {
  let component: FeedbackComponent;
  let fixture: ComponentFixture<FeedbackComponent>;
  let httpService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [FeedbackComponent],
      imports: [RouterTestingModule, HttpClientModule, HttpClientTestingModule],
      providers: [HttpService,SharedService,  { provide: APP_CONFIG, useValue: AppConfig }]
    })
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FeedbackComponent);
    component = fixture.componentInstance;
    httpService = TestBed.inject(HttpService);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

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

  // it('should set category Info', fakeAsync(() => {
  //   const response = {
  //     message: 'Found all feedback categories',
  //     success: true,
  //     data: [
  //       'EMM',
  //       'Additional KPI',
  //       'Tool Integration',
  //       'Admin',
  //       'UI',
  //       'Other'
  //     ]
  //   };
  //   const spy = spyOn(httpService, 'getFeedbackCategory').and.returnValue(of(response));
  //   component.getCategory();
  //   tick();
  //   expect(component.area).toEqual(response.data);
  // }));
});
