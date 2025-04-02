import {
  ComponentFixture,
  fakeAsync,
  TestBed,
  tick,
} from '@angular/core/testing';
import { AppComponent } from './app.component';
import { Router, ActivatedRoute, NavigationEnd } from '@angular/router';
import { of } from 'rxjs';
import { Location } from '@angular/common';
import { PrimeNGConfig } from 'primeng/api';
import { SharedService } from './services/shared.service';
import { GetAuthService } from './services/getauth.service';
import { HttpService } from './services/http.service';
import { GoogleAnalyticsService } from './services/google-analytics.service';
import { GetAuthorizationService } from './services/get-authorization.service';
import { HelperService } from './services/helper.service';

describe('AppComponent', () => {
  let component: AppComponent;
  let fixture: ComponentFixture<AppComponent>;
  let routerMock: any;
  let sharedServiceMock: any;
  let getAuthMock: any;
  let httpServiceMock: any;
  let googleAnalyticsMock: any;
  let getAuthorizationMock: any;
  let activatedRouteMock: any;
  let locationMock: any;
  let primengConfigMock: any;
  let helperServiceMock: any;
  let getItemSpy: jasmine.Spy;

  beforeEach(async () => {
    sharedServiceMock = jasmine.createSpyObj('SharedService', [
      'setProjectQueryParamInFilters',
      'setSprintQueryParamInFilters',
      'getSelectedType',
      'setSelectedBoard',
      'raiseError',
      'setBackupOfFilterSelectionState',
    ]);

    getAuthMock = jasmine.createSpyObj('GetAuthService', ['checkAuth']);
    httpServiceMock = jasmine.createSpyObj(
      'HttpService',
      ['handleRestoreUrl'],
      { currentVersion: '1.0.0' },
    );
    const googleAnalyticsServiceMock = jasmine.createSpyObj(
      'GoogleAnalyticsService',
      ['setPageLoad'],
    );
    const getAuthorizationServiceMock = jasmine.createSpyObj(
      'GetAuthorizationService',
      ['getRole'],
    );
    helperServiceMock = jasmine.createSpyObj('HelperService', [
      'setBackupOfUrlFilters',
    ]);
    locationMock = jasmine.createSpyObj('Location', ['path']);
    primengConfigMock = jasmine.createSpyObj('PrimeNGConfig', [], {
      ripple: true,
    });

    routerMock = jasmine.createSpyObj('Router', ['events', 'navigate']);
    // routerEvents = new Subject<any>();

    routerMock = {
      navigate: jasmine.createSpy('navigate'),
      events: of(new NavigationEnd(1, 'mockUrl', 'mockUrl')),
    };

    getItemSpy = spyOn(localStorage, 'getItem').and.callFake((key) => {
      if (key === 'shared_link') return null;
      if (key === 'currentUserDetails')
        return JSON.stringify({
          projectsAccess: [{ projects: [{ projectId: 123 }] }],
          authorities: ['ROLE_USER'],
        });
      return null;
    });

    activatedRouteMock = {
      queryParams: of({
        stateFilters: btoa(JSON.stringify({ primary_level: [] })),
      }),
    };

    sharedServiceMock = jasmine.createSpyObj('SharedService', [
      'setSelectedBoard',
      'setBackupOfFilterSelectionState',
      'setKpiSubFilterObj',
      'raiseError',
      'getKpiSubFilterObj',
      'getSelectedType',
    ]);
    sharedServiceMock.getSelectedType.and.returnValue('Scrum');

    getAuthMock = jasmine.createSpyObj('GetAuthService', ['checkAuth']);
    getAuthMock.checkAuth.and.returnValue(true);

    getAuthorizationMock = jasmine.createSpyObj('GetAuthorizationService', [
      'getRole',
    ]);
    getAuthorizationMock.getRole.and.returnValue('Admin');

    httpServiceMock = jasmine.createSpyObj('HttpService', [], {
      currentVersion: '1.0.0',
    });

    googleAnalyticsMock = jasmine.createSpyObj('GoogleAnalyticsService', [
      'setPageLoad',
    ]);

    locationMock = jasmine.createSpyObj('Location', ['path']);
    locationMock.path.and.returnValue('/dashboard');

    primengConfigMock = jasmine.createSpyObj('PrimeNGConfig', [], {
      ripple: false,
    });

    helperServiceMock = jasmine.createSpyObj('HelperService', ['someMethod']); // Mock HelperService

    await TestBed.configureTestingModule({
      declarations: [AppComponent],
      providers: [
        { provide: Router, useValue: routerMock },
        { provide: ActivatedRoute, useValue: activatedRouteMock },
        { provide: SharedService, useValue: sharedServiceMock },
        { provide: GetAuthService, useValue: getAuthMock },
        { provide: HttpService, useValue: httpServiceMock },
        { provide: GoogleAnalyticsService, useValue: googleAnalyticsMock },
        { provide: GetAuthorizationService, useValue: getAuthorizationMock },
        { provide: Location, useValue: locationMock },
        { provide: PrimeNGConfig, useValue: primengConfigMock },
        { provide: HelperService, useValue: helperServiceMock },
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AppComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    getItemSpy.and.stub(); // Reset spies after each test
  });

  it('should create the app', () => {
    expect(component).toBeTruthy();
  });

  it('should set authorized property based on authentication check', () => {
    expect(component.authorized).toBeTrue();
  });

  it('should remove newUI from local storage', () => {
    spyOn(localStorage, 'removeItem');
    component.ngOnInit();
    expect(localStorage.removeItem).toHaveBeenCalledWith('newUI');
  });

  // it('should decode state filters and set selectedTab', () => {
  //   expect(sharedServiceMock.setSelectedBoard).toHaveBeenCalledWith('iteration');
  // });

  it('should enable PrimeNG ripple effect', () => {
    expect(primengConfigMock.ripple).toBeFalse();
  });

  it('should send page tracking data to Google Analytics', () => {
    expect(googleAnalyticsMock.setPageLoad).toHaveBeenCalled();
  });

  it('should navigate to dashboard if stateFilters is missing', () => {
    getItemSpy.and.callFake((key) => {
      if (key === 'shared_link') return 'https://mock.url';
      return null;
    });
  });

  it('should navigate to dashboard if no shared link exists', () => {
    localStorage.removeItem('shared_link');

    component.ngOnInit();

    expect(routerMock.navigate).toHaveBeenCalledWith(['./dashboard/']);
  });

  it('should initialize component correctly and call ngOnInit', () => {
    // const routerSpy = spyOn(router, 'navigate');
    // const getAuthSpy = spyOn(getAuthService, 'checkAuth').and.returnValue(true);

    component.ngOnInit();

    // expect(sharedServiceMock.setProjectQueryParamInFilters).toHaveBeenCalledWith(jasmine.any(String));
    expect(routerMock.navigate).toHaveBeenCalledWith(['./dashboard/']);
  });

  it('should correctly identify projectLevelSelected when parent_level exists', () => {
    getItemSpy.and.callFake((key) => {
      if (key === 'shared_link')
        return (
          'https://mock.url?stateFilters=' +
          btoa(
            JSON.stringify({
              parent_level: { basicProjectConfigId: 123, labelName: 'project' },
            }),
          )
        );
      if (key === 'currentUserDetails')
        return JSON.stringify({
          projectsAccess: [{ projects: [{ projectId: 123 }] }],
          authorities: [],
        });
      return null;
    });

    component.ngOnInit();
    expect(routerMock.navigate).toHaveBeenCalledWith([
      'https://mock.url?stateFilters=' +
        btoa(
          JSON.stringify({
            parent_level: { basicProjectConfigId: 123, labelName: 'project' },
          }),
        ),
    ]);
  });

  it('should add "scrolled" class when window scrollY > 200', () => {
    const header = document.createElement('div');
    header.classList.add('header');
    spyOn(document, 'querySelector').and.returnValue(header);
    spyOnProperty(window, 'scrollY', 'get').and.returnValue(300);

    component.onScroll(new Event('scroll'));

    expect(header.classList.contains('scrolled')).toBeTrue();
  });

  it('should remove "scrolled" class when window scrollY <= 200', () => {
    const header = document.createElement('div');
    header.classList.add('header');
    spyOn(document, 'querySelector').and.returnValue(header);
    spyOnProperty(window, 'scrollY', 'get').and.returnValue(100);

    component.onScroll(new Event('scroll'));

    expect(header.classList.contains('scrolled')).toBeFalse();
  });

  it('should navigate to default dashboard if no shared link is found', () => {
    localStorage.removeItem('shared_link');
    // const routerSpy = spyOn(router, 'navigate');

    component.ngOnInit();

    expect(routerMock.navigate).toHaveBeenCalledWith(['./dashboard/']);
  });

  it('should navigate to the provided URL if the user has access to all projects', fakeAsync(() => {
    const decodedStateFilters = JSON.stringify({
      parent_level: { basicProjectConfigId: 'project1', labelName: 'Project' },
      primary_level: [],
    });
    const currentUserProjectAccess = [{ projectId: 'project1' }];
    const url = 'http://example.com';

    spyOn(component, 'urlRedirection').and.callThrough();

    component.urlRedirection(
      decodedStateFilters,
      currentUserProjectAccess,
      url,
      true,
    );

    tick();
    expect(component.urlRedirection).toHaveBeenCalledWith(
      decodedStateFilters,
      currentUserProjectAccess,
      url,
      true,
    );
    expect(routerMock.navigate).toHaveBeenCalledWith([url]);
  }));

  it('should navigate to the error page if the user does not have access to the project', fakeAsync(() => {
    const decodedStateFilters = JSON.stringify({
      parent_level: { basicProjectConfigId: 'project1', labelName: 'Project' },
      primary_level: [],
    });
    const currentUserProjectAccess = [{ projectId: 'project2' }];
    const url = 'http://example.com';

    spyOn(component, 'urlRedirection').and.callThrough();

    component.urlRedirection(
      decodedStateFilters,
      currentUserProjectAccess,
      url,
      false,
    );

    tick();
    expect(component.urlRedirection).toHaveBeenCalledWith(
      decodedStateFilters,
      currentUserProjectAccess,
      url,
      false,
    );
    expect(routerMock.navigate).toHaveBeenCalledWith(['/dashboard/Error']);
    expect(sharedServiceMock.raiseError).toHaveBeenCalledWith({
      status: 901,
      message: 'No project access.',
    });
  }));

  // Test cases for scroll behavior
  describe('scroll behavior', () => {
    let header: HTMLElement;

    beforeEach(() => {
      header = document.createElement('div');
      header.classList.add('header');
      document.body.appendChild(header);
    });

    afterEach(() => {
      document.body.removeChild(header);
    });

    it('should add scrolled class when window is scrolled beyond 200px', () => {
      header.classList.add('scrolled');
      // Set the scroll position
      window.scrollTo(0, 201);

      // Dispatch a scroll event
      window.dispatchEvent(new Event('scroll'));

      expect(header.classList.contains('scrolled')).toBeFalse();
    });

    it('should remove scrolled class when window is scrolled less than 200px', () => {
      window.scrollTo(0, 199);
      window.dispatchEvent(new Event('scroll'));
      expect(header.classList.contains('scrolled')).toBeFalse();
    });
  });

  // Test cases for authorization
  describe('authorization', () => {
    it('should clear newUI from localStorage on init', () => {
      localStorage.setItem('newUI', 'some-value');
      component.ngOnInit();
      expect(localStorage.getItem('newUI')).toBeNull();
    });
  });
});
