import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HeaderComponent } from './header.component';

import { RouterTestingModule } from '@angular/router/testing';
import { SharedService } from '../../services/shared.service';
import { HelperService } from 'src/app/services/helper.service';
import { GetAuthService } from '../../services/getauth.service';
import { HttpClientModule } from '@angular/common/http';
import { APP_CONFIG, AppConfig } from '../../services/app.config';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpService } from '../../services/http.service';
import { CommonModule, DatePipe } from '@angular/common';
import { of } from 'rxjs';
import { Router, Routes } from '@angular/router';
import { LoginComponent } from 'src/app/authentication/login/login.component';
import { IterationComponent } from 'src/app/dashboard/iteration/iteration.component';
import { RequestStatusComponent } from 'src/app/config/profile/request-status/request-status.component';
import { GetAuthorizationService } from 'src/app/services/get-authorization.service';
import { ViewNewUserAuthRequestComponent } from 'src/app/config/profile/view-new-user-auth-request/view-new-user-auth-request.component';
import { ViewRequestsComponent } from 'src/app/config/profile/view-requests/view-requests.component';

describe('HeaderComponent', () => {
  let component: HeaderComponent;
  let fixture: ComponentFixture<HeaderComponent>;
  let getAuth: GetAuthService;
  let httpService: HttpService
  let sharedService: SharedService;
  let helperService: HelperService;
  let mockGetAuthorizationService;
  let mockRouter;

  const routes: Routes = [
    { path: 'authentication/login', component: LoginComponent },
    { path: 'dashboard', component: IterationComponent },
    { path: 'dashboard/Config/Profile/RequestStatus', component: RequestStatusComponent },
    { path: 'dashboard/Config/Profile/GrantNewUserAuthRequests', component: ViewNewUserAuthRequestComponent },
    { path: 'dashboard/Config/Profile/GrantRequests', component: ViewRequestsComponent }
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [HeaderComponent],
      imports: [RouterTestingModule.withRoutes(routes), HttpClientModule, BrowserAnimationsModule],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],

      providers: [SharedService, GetAuthService, HttpService, HelperService, CommonModule, DatePipe, GetAuthorizationService,
        { provide: APP_CONFIG, useValue: AppConfig }
      ]
    })
      .compileComponents();

    fixture = TestBed.createComponent(HeaderComponent);
    component = fixture.componentInstance;
    getAuth = TestBed.get(GetAuthService);
    httpService = TestBed.inject(HttpService);
    sharedService = TestBed.inject(SharedService);
    helperService = TestBed.inject(HelperService);
    mockGetAuthorizationService = jasmine.createSpyObj(GetAuthorizationService, ['checkIfSuperUser', 'checkIfProjectAdmin']);
    mockRouter = component.router; 
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });




  it('should set backToDashboardLoader to true, navigate to lastVisitedFromUrl, and set backToDashboardLoader to false', () => {
    component.lastVisitedFromUrl = '/dashboard';
    component.backToDashboardLoader = false;

    component.navigateToDashboard();

    expect(component.backToDashboardLoader).toBe(false);
  });



  it('should navigate to the correct route when user is super user', () => {
    component.ifSuperUser = true;
    const type = 'Project Access Request';
    const navigateSpy = spyOn(mockRouter, 'navigate');
    component.routeForAccess(type);

    expect(navigateSpy).toHaveBeenCalledWith(['/dashboard/Config/Profile/GrantRequests']);
  });

  it('should navigate to the correct route when user is project admin', () => {
    component.ifProjectAdmin = true;
    const type = 'User Access Request';
    const navigateSpy = spyOn(mockRouter, 'navigate');
    component.routeForAccess(type);

    expect(navigateSpy).toHaveBeenCalledWith(['/dashboard/Config/Profile/GrantNewUserAuthRequests']);
  });

  it('should navigate to the default route when type is not recognized', () => {
    const type = 'Invalid Type';
    const navigateSpy = spyOn(mockRouter, 'navigate');
    component.routeForAccess(type);

    expect(navigateSpy).toHaveBeenCalledWith(['/dashboard/Config/Profile/RequestStatus']);
  });

  it('should navigate to the default route when user is not super user or project admin', () => {
    component.ifProjectAdmin = false;
    component.ifSuperUser = false;
    const type = 'Project Access Request';
    const navigateSpy = spyOn(mockRouter, 'navigate');
    component.routeForAccess(type);

    expect(navigateSpy).toHaveBeenCalledWith(['/dashboard/Config/Profile/RequestStatus']);
  });

  it("should notification list not null if response is comming",()=>{
    const fakeResponce = {
      message: 'Data came successfully',
      success: true,
      data: [{ count: 2, type: 'User Access Request' }],
    };
    spyOn(httpService,'getAccessRequestsNotifications').and.returnValue(of(fakeResponce));
    component.getNotification();
    expect(component.notificationList).not.toBe(null);
  })
});
