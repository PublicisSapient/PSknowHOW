import { HttpClientModule } from '@angular/common/http';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { environment } from 'src/environments/environment';
import { HttpService } from '../../services/http.service';
import { HelpComponent } from './help.component';
import { APP_CONFIG, AppConfig } from '../../services/app.config';
import { of } from 'rxjs';
import { SharedService } from 'src/app/services/shared.service';

describe('HelpComponent', () => {
  let component: HelpComponent;
  let fixture: ComponentFixture<HelpComponent>;
  let httpMock;
  let httpService;
  const baseUrl = environment.baseUrl;  // Servers Env
  const fakeLandingInfo = require('../../../test/resource/fakeLandingInfo.json');
  const fakeFeedbackAreaList = require('../../../test/resource/fakeFeedbackAreaList.json');

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [HelpComponent],
      imports: [RouterTestingModule, HttpClientModule, HttpClientTestingModule],
      providers: [HttpService,SharedService, { provide: APP_CONFIG, useValue: AppConfig }]
    })
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(HelpComponent);
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

  
});
