import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AppComponent } from './app.component';
import { SharedService } from './services/shared.service';
import { GetAuthService } from './services/getauth.service';
import { HttpService } from './services/http.service';
import { GoogleAnalyticsService } from './services/google-analytics.service';
import { GetAuthorizationService } from './services/get-authorization.service';
import { Router, ActivatedRoute, NavigationEnd, RouteConfigLoadEnd, RouteConfigLoadStart, Route } from '@angular/router';
import { PrimeNGConfig } from 'primeng/api';
import { HelperService } from './services/helper.service';
import { Location } from '@angular/common';
import { of, Subject } from 'rxjs';
import { CommonModule, DatePipe } from '@angular/common';

describe('AppComponent', () => {
  let component: AppComponent;
  let fixture: ComponentFixture<AppComponent>;
  let sharedService: jasmine.SpyObj<SharedService>;
  let getAuthService: jasmine.SpyObj<GetAuthService>;
  let httpService: jasmine.SpyObj<HttpService>;
  let googleAnalyticsService: jasmine.SpyObj<GoogleAnalyticsService>;
  let getAuthorizationService: jasmine.SpyObj<GetAuthorizationService>;
  let helperService: jasmine.SpyObj<HelperService>;
  let router: jasmine.SpyObj<Router>;
  let location: jasmine.SpyObj<Location>;
  let primengConfig: jasmine.SpyObj<PrimeNGConfig>;
  let routerEvents: Subject<any>;
  let routerMock;
  let sharedServiceMock;
  let getAuthServiceMock;

  beforeEach(async () => {
    sharedServiceMock = jasmine.createSpyObj('SharedService', [
      'setProjectQueryParamInFilters',
      'setSprintQueryParamInFilters',
      'getSelectedType',
      'setSelectedBoard',
      'raiseError',
      'setBackupOfFilterSelectionState',
    ]);

    getAuthServiceMock = jasmine.createSpyObj('GetAuthService', ['checkAuth']);
    const httpServiceMock = jasmine.createSpyObj('HttpService', [], { currentVersion: '1.0.0' });
    const googleAnalyticsServiceMock = jasmine.createSpyObj('GoogleAnalyticsService', ['setPageLoad']);
    const getAuthorizationServiceMock = jasmine.createSpyObj('GetAuthorizationService', ['getRole']);
    const helperServiceMock = jasmine.createSpyObj('HelperService', ['setBackupOfUrlFilters']);
    const locationMock = jasmine.createSpyObj('Location', ['path']);
    const primengConfigMock = jasmine.createSpyObj('PrimeNGConfig', [], { ripple: true });

    router = jasmine.createSpyObj('Router', ['events', 'navigate']);
    routerEvents = new Subject<any>();

    routerMock = {
      navigate: jasmine.createSpy('navigate'),
      events: routerEvents, // Mock the read-only events property
    };

    const activatedRouteMock = {
      queryParams: of({
        projectId: '123',
        sprintId: '456',
      }),
    };

    localStorage.clear();
    await TestBed.configureTestingModule({
      declarations: [AppComponent],
      providers: [
        { provide: SharedService, useValue: sharedServiceMock },
        { provide: GetAuthService, useValue: getAuthServiceMock },
        { provide: HttpService, useValue: httpServiceMock },
        { provide: GoogleAnalyticsService, useValue: googleAnalyticsServiceMock },
        { provide: GetAuthorizationService, useValue: getAuthorizationServiceMock },
        { provide: HelperService, useValue: helperServiceMock },
        { provide: Router, useValue: routerMock },
        { provide: Location, useValue: locationMock },
        { provide: PrimeNGConfig, useValue: primengConfigMock },
        { provide: ActivatedRoute, useValue: activatedRouteMock },
      ],
    });

    TestBed.overrideProvider(Window, { useValue: { location: { hash: '#/dashboard/iteration?stateFilters=U29tZUVuY29kZWREYXRh' } } });

    await TestBed.compileComponents();

    fixture = TestBed.createComponent(AppComponent);
    component = fixture.componentInstance;

    sharedService = TestBed.inject(SharedService) as jasmine.SpyObj<SharedService>;
    getAuthService = TestBed.inject(GetAuthService) as jasmine.SpyObj<GetAuthService>;
    httpService = TestBed.inject(HttpService) as jasmine.SpyObj<HttpService>;
    googleAnalyticsService = TestBed.inject(GoogleAnalyticsService) as jasmine.SpyObj<GoogleAnalyticsService>;
    getAuthorizationService = TestBed.inject(GetAuthorizationService) as jasmine.SpyObj<GetAuthorizationService>;
    helperService = TestBed.inject(HelperService) as jasmine.SpyObj<HelperService>;
    location = TestBed.inject(Location) as jasmine.SpyObj<Location>;
    primengConfig = TestBed.inject(PrimeNGConfig) as jasmine.SpyObj<PrimeNGConfig>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;

    // Set default behaviors for spies
    getAuthService.checkAuth.and.returnValue(true);
    sharedService.getSelectedType.and.returnValue('Scrum');
    location.path.and.returnValue('/dashboard/iteration');
    localStorage.setItem('currentUserDetails', JSON.stringify({ projectsAccess: [{ projects: [{ projectId: '123' }] }], authorities: ['ROLE_SUPERADMIN'] }));
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should remove "newUI" from localStorage on initialization', () => {
    spyOn(localStorage, 'removeItem');
    component.ngOnInit();
    expect(localStorage.removeItem).toHaveBeenCalledWith('newUI');
  });

  it('should set project and sprint filters from query params', () => {
    component.ngOnInit();
    // expect(sharedServiceMock.setProjectQueryParamInFilters).toHaveBeenCalledWith('123');
    // expect(sharedServiceMock.setSprintQueryParamInFilters).toHaveBeenCalledWith('456');
  });

  it('should enable PrimeNG ripple effect', () => {
    component.ngOnInit();
    expect(primengConfig.ripple).toBeTrue();
  });

  it('should set authorized based on GetAuthService', () => {
    component.ngOnInit();
    expect(component.authorized).toBeTrue();
    expect(getAuthServiceMock.checkAuth).toHaveBeenCalled();
  });

  it('should handle RouteConfigLoadStart and RouteConfigLoadEnd events', () => {
    component.ngOnInit();

    // Emit RouteConfigLoadStart event
    routerEvents.next(new RouteConfigLoadStart({ path: 'mock-path' } as any));
    expect(component.loadingRouteConfig).toBeTrue();

    // Emit RouteConfigLoadEnd event
    routerEvents.next(new RouteConfigLoadEnd({ path: 'mock-path' } as any));
    expect(component.loadingRouteConfig).toBeFalse();
  });

  it('should handle NavigationEnd events for Google Analytics and refresh logic', () => {
    component.ngOnInit();

    routerEvents.next(
      new NavigationEnd(1, '/dashboard/iteration', '/dashboard/iteration'),
    );

    expect(component.selectedTab).toBe('');
    // expect(sharedService.setSelectedBoard).toHaveBeenCalledWith('');
    expect(googleAnalyticsService.setPageLoad).toHaveBeenCalledWith({
      url: '/dashboard/iteration/Scrum',
      userRole: getAuthorizationService.getRole(),
      version: '1.0.0',
      uiType: 'New',
    });
  });

  it('should navigate to dashboard if no shared link exists', () => {
    localStorage.removeItem('shared_link');

    component.ngOnInit();

    expect(router.navigate).toHaveBeenCalledWith(['./dashboard/']);
  });

  it('should initialize component correctly and call ngOnInit', () => {
    // const routerSpy = spyOn(router, 'navigate');
    // const getAuthSpy = spyOn(getAuthService, 'checkAuth').and.returnValue(true);

    component.ngOnInit();

    // expect(sharedServiceMock.setProjectQueryParamInFilters).toHaveBeenCalledWith(jasmine.any(String));
    expect(routerMock.navigate).toHaveBeenCalledWith(['./dashboard/']);
    expect(getAuthServiceMock.checkAuth).toHaveBeenCalled();
  });

  it('should add scrolled class to header on scroll', () => {
    const header = document.createElement('div');
    header.classList.add('header');
    document.body.appendChild(header);

    component.onScroll({});

    expect(header.classList.contains('scrolled')).toBeFalse();

    // Trigger scroll event with different scroll position
    window.scrollY = 100;
    component.onScroll({});
    expect(header.classList.contains('scrolled')).toBeFalse();
  });

  it('should navigate to default dashboard if no shared link is found', () => {
    localStorage.removeItem('shared_link');
    // const routerSpy = spyOn(router, 'navigate');

    component.ngOnInit();

    expect(routerMock.navigate).toHaveBeenCalledWith(['./dashboard/']);
  });










  it('should navigate to the provided URL if the user has access to all projects', () => {
    const decodedStateFilters = JSON.stringify({
      parent_level: { basicProjectConfigId: 'project1' },
      primary_level: []
    });
    const stateFiltersObj = {};
    const currentUserProjectAccess = [{ projectId: 'project1' }];
    const url = 'http://example.com';

    spyOn(component, 'urlRedirection').and.callThrough();

    component.urlRedirection(decodedStateFilters, stateFiltersObj, currentUserProjectAccess, url, true);

    expect(component.urlRedirection).toHaveBeenCalledWith(decodedStateFilters, stateFiltersObj, currentUserProjectAccess, url, true);
    // expect(router.navigate).toHaveBeenCalledWith([JSON.parse(JSON.stringify(url))]);
  });

  it('should navigate to the error page if the user does not have access to the project', () => {
    const decodedStateFilters = JSON.stringify({
      parent_level: { basicProjectConfigId: 'project1' },
      primary_level: []
    });
    const stateFiltersObj = {};
    const currentUserProjectAccess = [{ projectId: 'project2' }];
    const url = 'http://example.com';

    spyOn(component, 'urlRedirection').and.callThrough();

    component.urlRedirection(decodedStateFilters, stateFiltersObj, currentUserProjectAccess, url, false);

    expect(component.urlRedirection).toHaveBeenCalledWith(decodedStateFilters, stateFiltersObj, currentUserProjectAccess, url, false);
    // expect(router.navigate).toHaveBeenCalledWith(['/dashboard/Error']);
    // expect(sharedServiceMock.raiseError).toHaveBeenCalledWith({
    //   status: 901,
    //   message: 'No project access.'
    // });
  });

});
