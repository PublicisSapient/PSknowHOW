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

  beforeEach(async () => {
    const sharedServiceMock = jasmine.createSpyObj('SharedService', [
      'setProjectQueryParamInFilters',
      'setSprintQueryParamInFilters',
      'getSelectedType',
      'setSelectedBoard',
    ]);

    const getAuthServiceMock = jasmine.createSpyObj('GetAuthService', ['checkAuth']);
    const httpServiceMock = jasmine.createSpyObj('HttpService', [], { currentVersion: '1.0.0' });
    const googleAnalyticsServiceMock = jasmine.createSpyObj('GoogleAnalyticsService', ['setPageLoad']);
    const getAuthorizationServiceMock = jasmine.createSpyObj('GetAuthorizationService', ['getRole']);
    const helperServiceMock = jasmine.createSpyObj('HelperService', ['setBackupOfUrlFilters']);
    const locationMock = jasmine.createSpyObj('Location', ['path']);
    const primengConfigMock = jasmine.createSpyObj('PrimeNGConfig', [], { ripple: true });

    router = jasmine.createSpyObj('Router', ['events']);
    routerEvents = new Subject<any>();

    const routerMock = {
      navigate: jasmine.createSpy('navigate'),
      events: routerEvents, // Mock the read-only events property
    };

    const activatedRouteMock = {
      queryParams: of({
        projectId: '123',
        sprintId: '456',
      }),
    };

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
    expect(sharedService.setProjectQueryParamInFilters).toHaveBeenCalledWith('123');
    expect(sharedService.setSprintQueryParamInFilters).toHaveBeenCalledWith('456');
  });

  it('should enable PrimeNG ripple effect', () => {
    component.ngOnInit();
    expect(primengConfig.ripple).toBeTrue();
  });

  it('should set authorized based on GetAuthService', () => {
    component.ngOnInit();
    expect(component.authorized).toBeTrue();
    expect(getAuthService.checkAuth).toHaveBeenCalled();
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

    expect(component.selectedTab).toBe('iteration');
    expect(sharedService.setSelectedBoard).toHaveBeenCalledWith('iteration');
    expect(googleAnalyticsService.setPageLoad).toHaveBeenCalledWith({
      url: '/dashboard/iteration/Scrum',
      userRole: getAuthorizationService.getRole(),
      version: '1.0.0',
      uiType: 'New',
    });
  });

  xit('should decode and set state filters from URL hash', () => {
    component.ngOnInit();
    expect(helperService.setBackupOfUrlFilters).toHaveBeenCalledWith('SomeEncodedData');
  });
});
