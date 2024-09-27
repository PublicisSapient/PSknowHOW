import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { Router, NavigationEnd, RouteConfigLoadStart, RouteConfigLoadEnd } from '@angular/router';
import { HttpService } from './core/services/http.service';
import { AppComponent } from './app.component';
import { GoogleAnalyticsService } from './core/services/google-analytics.service';
import { GetAuthorizationService } from './core/services/get-authorization.service';
import { GetAuthService } from './core/services/getauth.service';
import { SharedService } from './core/services/shared.service';
import { PrimeNGConfig } from 'primeng/api';
import { of } from 'rxjs';
import { APP_CONFIG, AppConfig } from './core/configs/app.config';

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
  let originalLocation: Location;

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
    spyOn(gaService, 'load').and.returnValue(Promise.resolve(['gaTagManager']));
    jasmine.createSpy('checkAuth').and.returnValue(true);
    spyOn(sharedService, 'setSelectedType');
    spyOn(component.ga, 'setPageLoad');
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should set authorized to true on init', () => {
    expect(component.authorized).toBeTrue();
  });

  it('should set localStorage item when switch is checked', () => {
    const event = { checked: true };
    component.uiSwitch(event);
    expect(localStorage.getItem('newUI')).toBe('true');
  });

  it('should remove localStorage item when switch is unchecked', () => {
    const event = { checked: false };
    localStorage.setItem('newUI', 'true');
    component.uiSwitch(event);
    expect(localStorage.getItem('newUI')).toBeNull();
  });
});
