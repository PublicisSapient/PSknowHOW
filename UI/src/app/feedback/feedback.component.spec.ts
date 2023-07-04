import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpService } from '../services/http.service';
import { FeedbackComponent } from './feedback.component';
import { of } from 'rxjs';

describe('FeedbackComponent', () => {
  let component: FeedbackComponent;
  let fixture: ComponentFixture<FeedbackComponent>;
  let httpService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FeedbackComponent ],
      providers: [HttpService]
    })
    .compileComponents();

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
});
