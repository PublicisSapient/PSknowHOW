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
    const obj = {
      "feedback": "test",
    };
    const res = { "message": "Your request has been submitted", "success": true, "data": { "feedback": "test" } }
    spyOn(httpService, 'submitFeedbackData').and.returnValue(of(res));
    component.save();
    tick(3000);
    expect(component.isFeedbackSubmitted).toBe(true);
    expect(component.formMessage).toEqual('');
  }))

  it('should toggle flag value', fakeAsync(() => {
    component.feedback = true;
    component.toggleFlag();
    expect(component.feedback).toBe(false);
  }));
});
