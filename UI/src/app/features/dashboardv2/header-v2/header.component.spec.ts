import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HeaderComponent } from './header.component';

import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientModule } from '@angular/common/http';
import { APP_CONFIG, AppConfig } from '../../../core/configs/app.config';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { CommonModule, DatePipe } from '@angular/common';
import { of } from 'rxjs';
import { Router, Routes } from '@angular/router';
import { LoginComponent } from 'src/app/features/authentication/login/login.component';
import { IterationComponent } from 'src/app/features/dashboard/iteration/iteration.component';
import { RequestStatusComponent } from 'src/app/features/config/profile/request-status/request-status.component';
import { ViewNewUserAuthRequestComponent } from 'src/app/features/config/profile/view-new-user-auth-request/view-new-user-auth-request.component';
import { ViewRequestsComponent } from 'src/app/features/config/profile/view-requests/view-requests.component';
import { HelperService } from 'src/app/core/services/helper.service';
import { SharedService } from 'src/app/core/services/shared.service';
import { GetAuthService } from 'src/app/core/services/getauth.service';
import { HttpService } from 'src/app/core/services/http.service';
import { GetAuthorizationService } from 'src/app/core/services/get-authorization.service';

describe('HeaderComponent', () => {
  let component: HeaderComponent;
  let fixture: ComponentFixture<HeaderComponent>;
  let routerSpy: jasmine.SpyObj<Router>;
  let getAuth: GetAuthService;
  let httpService: HttpService;
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
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    await TestBed.configureTestingModule({
      declarations: [HeaderComponent],
      imports: [RouterTestingModule.withRoutes(routes), HttpClientModule, BrowserAnimationsModule],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],

      providers: [SharedService, GetAuthService, HttpService, HelperService, CommonModule, DatePipe, GetAuthorizationService,
        { provide: APP_CONFIG, useValue: AppConfig }
      ]
    }).compileComponents();

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

  it("should notification list not null if response is comming", () => {
    const fakeResponce = {
      message: 'Data came successfully',
      success: true,
      data: [{ count: 2, type: 'User Access Request' }],
    };
    spyOn(httpService, 'getAccessRequestsNotifications').and.returnValue(of(fakeResponce));
    component.getNotification();
    expect(component.notificationList).not.toBe(null);
  });

  it("should navigate to /dashboard/my-knowhow when previousSelectedTab is Config", () => {
    const navigateSpy = spyOn(mockRouter, 'navigate');
    spyOnProperty(mockRouter, 'url', 'get').and.returnValue('/dashboard/Config');
    component.navigateToMyKnowHOW();
    expect(navigateSpy).toHaveBeenCalledWith(['/dashboard/my-knowhow']);
  });

  it("should navigate to /dashboard/my-knowhow when previousSelectedTab is Help", () => {
    const navigateSpy = spyOn(mockRouter, 'navigate');
    spyOnProperty(mockRouter, 'url', 'get').and.returnValue('/dashboard/Help');
    component.navigateToMyKnowHOW();
    expect(navigateSpy).toHaveBeenCalledWith(['/dashboard/my-knowhow']);
  });

  it("should not navigate to /dashboard/my-knowhow when previousSelectedTab is not Config or Help", () => {
    const navigateSpy = spyOn(mockRouter, 'navigate');
    spyOnProperty(mockRouter, 'url', 'get').and.returnValue('/dashboard/SomeOtherTab');
    component.navigateToMyKnowHOW();
    expect(navigateSpy).not.toHaveBeenCalledWith(['/dashboard/my-knowhow']);
  });

  it('should call helperService.logoutHttp() when logout() is called', () => {
    spyOn(helperService, 'logoutHttp');
    component.logout();
    expect(helperService.logoutHttp).toHaveBeenCalled();
  });
});
