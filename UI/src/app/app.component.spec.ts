import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { Router, NavigationEnd, RouteConfigLoadStart, RouteConfigLoadEnd } from '@angular/router';
import { HttpService } from './services/http.service';
import { AppComponent } from './app.component';
import { GoogleAnalyticsService } from './services/google-analytics.service';
import { GetAuthorizationService } from './services/get-authorization.service';
import { GetAuthService } from './services/getauth.service';
import { SharedService } from './services/shared.service';
import { PrimeNGConfig } from 'primeng/api';
import { of } from 'rxjs';
import { APP_CONFIG, AppConfig } from './services/app.config';

describe('AppComponent', () => {
  let component: AppComponent;
  let fixture: ComponentFixture<AppComponent>;
  let router: Router;
  let gaService: GoogleAnalyticsService;
  let httpService: HttpService;
  let authorization: GetAuthorizationService;
  let getAuthService: GetAuthService;
  let sharedService: SharedService;
  let primengConfig: PrimeNGConfig;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AppComponent ],
      imports: [ RouterTestingModule, HttpClientTestingModule ],
      providers: [ GoogleAnalyticsService, HttpService, GetAuthorizationService,
         SharedService, PrimeNGConfig, GetAuthService,
         { provide: APP_CONFIG, useValue: AppConfig }]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AppComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    gaService = TestBed.inject(GoogleAnalyticsService);
    httpService = TestBed.inject(HttpService);
    authorization = TestBed.inject(GetAuthorizationService);
    sharedService = TestBed.inject(SharedService);
    primengConfig = TestBed.inject(PrimeNGConfig);
    spyOn(httpService, 'getAnalyticsFlag').and.returnValue(of({ success: true, data: { analyticsSwitch: true } }));
    spyOn(gaService, 'load').and.returnValue(Promise.resolve(['gaTagManager']));
    jasmine.createSpy('checkAuth').and.returnValue(true);
    spyOn(sharedService, 'setSelectedType');
    spyOn(component.ga, 'setPageLoad');
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  /*xit('should load GA script if analyticsSwitch is true and not on localhost', () => {
    spyOn(window.location, 'origin').and.returnValue('example.com');
    component.ngOnInit();
    expect(httpService.getAnalyticsFlag).toHaveBeenCalled();
    expect(gaService.load).toHaveBeenCalledWith('gaTagManager');
  });

  xit('should not load GA script if analyticsSwitch is false', () => {
    spyOn(window.location, 'getOwnPropertyDescriptor').and.returnValue('example.com');
    spyOn(httpService, 'getAnalyticsFlag').and.returnValue(of({ success: true, data: { analyticsSwitch: false } }));
    component.ngOnInit();
    expect(httpService.getAnalyticsFlag).toHaveBeenCalled();
    expect(gaService.load).not.toHaveBeenCalled();
  });

  xit('should not load GA script if on localhost', () => {
    spyOn(window.location, 'getOwnPropertyDescriptor').and.returnValue('localhost:4200');
    component.ngOnInit();
    expect(httpService.getAnalyticsFlag).toHaveBeenCalled();
    expect(gaService.load).not.toHaveBeenCalled();
  });

  xit('should set loadingRouteConfig to true on RouteConfigLoadStart', () => {
    const event = new RouteConfigLoadStart(null);
    component.ngOnInit();
    router.events.next(event);
    expect(component.loadingRouteConfig).toBeTrue();
  });

  xit('should set loadingRouteConfig to false on RouteConfigLoadEnd', () => {
    const event = new RouteConfigLoadEnd(null);
    component.ngOnInit();
    router.events.next(event);
    expect(component.loadingRouteConfig).toBeFalse();
  });

  xit('should set loadingRouteConfig to false and call ga.setPageLoad on NavigationEnd', () => {
    const event = new NavigationEnd(0, '', '');
    component.ngOnInit();
    router.events.next(event);
    expect(component.loadingRouteConfig).toBeFalse();
    expect(component.ga.setPageLoad).toHaveBeenCalled();
    expect(component.ga.setPageLoad).toHaveBeenCalledWith({
      url: '/' + (sharedService.getSelectedType() ? sharedService.getSelectedType() : 'Scrum'),
      userRole: authorization.getRole(),
      version: httpService.currentVersion
    });
  });

  it('should set selectedType to Scrum if sharedService.getSelectedType returns falsy', () => {
    spyOn(sharedService, 'getSelectedType').and.returnValue(null);
    component.ngOnInit();
    expect(sharedService.setSelectedType).toHaveBeenCalledWith('Scrum');
  });

  it('should set selectedType to sharedService.getSelectedType if it returns truthy', () => {
    spyOn(sharedService, 'getSelectedType').and.returnValue('Type');
    component.ngOnInit();
    expect(sharedService.setSelectedType).toHaveBeenCalledWith('Type');
  });

  it('should set authorized to true on init', () => {
    expect(component.authorized).toBeTrue();
  });

  it('should set authorized to getAuth.checkAuth() on init', () => {
    spyOn(getAuthService, 'checkAuth').and.returnValue(false);
    component.ngOnInit();
    expect(component.authorized).toBeFalse();
  });*/
  
  it('should set authorized to true on init', () => {
    expect(component.authorized).toBeTrue();
  });

});