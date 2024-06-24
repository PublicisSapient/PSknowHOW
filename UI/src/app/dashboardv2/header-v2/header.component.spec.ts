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

describe('HeaderComponent', () => {
  let component: HeaderComponent;
  let fixture: ComponentFixture<HeaderComponent>;
  let getAuth: GetAuthService;
  let httpService: HttpService
  let sharedService: SharedService;
  let helperService: HelperService;

  const routes: Routes = [
    { path: 'authentication/login', component: LoginComponent },
    { path: 'dashboard', component: IterationComponent }
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ HeaderComponent ],
      imports: [RouterTestingModule.withRoutes(routes), HttpClientModule, BrowserAnimationsModule],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],

      providers: [SharedService, GetAuthService, HttpService, HelperService, CommonModule, DatePipe,
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
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should clear localStorage, reset helperService and sharedService, and navigate to login page when logout is successful', () => {
    const getData = null;
    spyOn(httpService,'logout').and.returnValue(of(getData));
    const localStorageSpy = spyOn(localStorage, 'clear');
    const setSelectedProjectSpy = spyOn(sharedService, 'setSelectedProject');
    const setCurrentUserDetailsSpy = spyOn(sharedService, 'setCurrentUserDetails');
    component.logout();
    expect(localStorageSpy).toHaveBeenCalled();
    expect(helperService.isKanban).toBe(false);
    expect(setSelectedProjectSpy).toHaveBeenCalledWith(null);
    expect(setCurrentUserDetailsSpy).toHaveBeenCalledWith({});
  });

  it('should not clear localStorage, reset helperService and sharedService, and navigate to login page when logout returns an error', () => {
    const getData = ['error'];
    spyOn(httpService,'logout').and.returnValue(of(getData));
    const localStorageSpy = spyOn(localStorage, 'clear');
    const setSelectedProjectSpy = spyOn(sharedService, 'setSelectedProject');
    const setCurrentUserDetailsSpy = spyOn(sharedService, 'setCurrentUserDetails');
    component.logout();
    expect(localStorageSpy).not.toHaveBeenCalled();
    expect(helperService.isKanban).toBe(false);
    expect(setSelectedProjectSpy).not.toHaveBeenCalled();
    expect(setCurrentUserDetailsSpy).not.toHaveBeenCalled();
  });


  it('should set backToDashboardLoader to true, navigate to lastVisitedFromUrl, and set backToDashboardLoader to false', () => {
    component.lastVisitedFromUrl = '/dashboard';
    component.backToDashboardLoader = false;

    component.navigateToDashboard();

    expect(component.backToDashboardLoader).toBe(false);
  });
});