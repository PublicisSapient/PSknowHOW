import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpService } from '../../../services/http.service';
import { ViewNewUserAuthRequestComponent } from './view-new-user-auth-request.component';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { APP_CONFIG, AppConfig } from '../../../services/app.config';
import { SharedService } from '../../../services/shared.service';
import { MessageService } from 'primeng/api';
import { environment } from 'src/environments/environment';
import { of } from 'rxjs';


describe('ViewNewUserAuthRequestComponent', () => {
  let component: ViewNewUserAuthRequestComponent;
  let fixture: ComponentFixture<ViewNewUserAuthRequestComponent>;
  let httpService;
  let httpMock;
  const baseUrl = environment.baseUrl;

  const fakeRequestsData = {
    message: 'Unapproved User details',
    success: true,
    data: [
      {
        username: 'testuser8',
        email: 'test8@gmail.com',
        approved: false
      }
    ]
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ViewNewUserAuthRequestComponent],
      providers: [
        HttpService,
        MessageService,
        SharedService,
        { provide: APP_CONFIG, useValue: AppConfig }
      ],
      imports: [
        HttpClientTestingModule,
        RouterTestingModule.withRoutes([]),
      ],
    })
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewNewUserAuthRequestComponent);
    component = fixture.componentInstance;
    httpService = TestBed.inject(HttpService);
    httpMock = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should get new user requests', () => {
    component.ngOnInit();
    fixture.detectChanges();
    httpMock.match(baseUrl + '/api/userapprovals')[0].flush(fakeRequestsData);
    expect(component.newUserAccessRequestData).toEqual(fakeRequestsData.data);
  });

  it('should approve user requests', () => {
    component.newUserAccessRequestData = fakeRequestsData.data;
    const fakeRequest = {
      username: 'testuser8',
      email: 'test8@gmail.com',
      approved: false
    };
    const fakeResponse = {
      message: 'Unapproved User details',
      success: true,
      data: []
    };
    component.updateRequestStatus(fakeRequest, true);
    fixture.detectChanges();
    httpMock.match(baseUrl + '/api/userapprovals')[0].flush(fakeResponse);
    expect(component.showLoader).toBeFalse();
    expect(component.newUserAccessRequestData).toEqual([]);
  });

  it('should update and return response for update request ', () => {
    component.newUserAccessRequestData = fakeRequestsData.data;
    const fakeRequest = {
      username: 'testuser8',
      email: 'test8@gmail.com',
      approved: false
    };
    const fakeResponse = {
      message: 'Unapproved User details',
      success: true,
      data: []
    };
    spyOn(httpService , "updateNewUserAccessRequest").and.returnValue(of(fakeResponse));
    component.updateRequestStatus(fakeRequest, true);
    fixture.detectChanges();
    expect(component.showLoader).toBeFalse();
  });

});
