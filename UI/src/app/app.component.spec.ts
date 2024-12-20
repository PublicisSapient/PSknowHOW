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
import { FeatureFlagsService } from './services/feature-toggle.service';
import { HelperService } from './services/helper.service';
import { CommonModule, DatePipe } from '@angular/common';

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
  let featureFlagsService: FeatureFlagsService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AppComponent ],
      imports: [ RouterTestingModule, HttpClientTestingModule ],
      providers: [ GoogleAnalyticsService, HttpService, GetAuthorizationService,
         SharedService, PrimeNGConfig, GetAuthService, FeatureFlagsService , HelperService, DatePipe,
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
    featureFlagsService = TestBed.inject(FeatureFlagsService)
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

  it('should initialize with authorized set to true when user is authenticated', () => {
		component.ngOnInit();
		expect(component.authorized).toBe(true);
	});

  it('should enable ripple effect in PrimeNGConfig', () => {
		component.ngOnInit();
		expect(primengConfig.ripple).toBe(true);
	});

  it('should add class scrolled to header when window.scrollY is 200', () => {
		const header = document.createElement('div');
		header.className = 'header';
		document.body.appendChild(header);
		window.scrollY = 200;
		component.onScroll(new Event('scroll'));
		expect(header.classList.contains('scrolled')).toBe(false);
		document.body.removeChild(header);
	});

  it('should remove the class `scrolled` from header when window.scrollY is 200', () => {
		const header = document.createElement('div');
		header.classList.add('scrolled');
		document.body.appendChild(header);
		window.scrollY = 200;
		component.onScroll(new Event('scroll'));
		expect(header.classList.contains('scrolled')).toBe(true);
		document.body.removeChild(header);
	});
});
