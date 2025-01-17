/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

import { ComponentFixture, TestBed, fakeAsync, inject, getTestBed, waitForAsync, tick } from '@angular/core/testing';
import { ErrorComponent } from './error.component';
import { SharedService } from '../../services/shared.service';
import { HttpService } from '../../services/http.service';
import { APP_CONFIG, AppConfig } from '../../services/app.config';
import { CommonModule } from '@angular/common';
import { Router, RouterModule, Routes } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { FormsModule } from '@angular/forms';
import { Subscription, timer } from 'rxjs';
import { ExecutiveV2Component } from 'src/app/dashboardv2/executive-v2/executive-v2.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('ErrorComponent', () => {
  const routes: Routes = [
    { path: 'dashboard', component: ExecutiveV2Component },
  ];

  let component: ErrorComponent;
  let fixture: ComponentFixture<ErrorComponent>;
  let sharedService: SharedService;
  let router: Router;
  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ErrorComponent],
      imports: [
        FormsModule,
        CommonModule,
        RouterTestingModule.withRoutes(routes),
        HttpClientTestingModule
      ],
      providers: [HttpService, SharedService,
        { provide: APP_CONFIG, useValue: AppConfig },
      ]
    })
      .compileComponents();
  }));



  beforeEach(() => {
    fixture = TestBed.createComponent(ErrorComponent);
    component = fixture.componentInstance;
    sharedService = TestBed.get(SharedService);
    fixture.detectChanges();
  });

  afterEach(() => {

  });

  const error0 = { status: 0, message: 'Internal Server Error' };
  const error401 = { status: 401, message: 'Session Expired' };
  const error403 = { status: 403, message: 'Unauthorised' };
  const error404 = { status: 404, message: 'Not found' };
  const error500 = { status: 500, message: 'Internal Server Error' };
  const error405 = { status: 405, message: 'Method not allowed' };
  const error900 = { status: 900, message: 'Invalid URL.' };
  const error901 = { status: 901, message: 'No project access.' };

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display proper error message on connection refused error', () => {
    sharedService.raiseError(error0);
    spyOn(sharedService.passErrorToErrorPage, 'emit');
    component.ngOnInit();
    fixture.detectChanges();
    expect(component.errorMsg).toEqual('Server not available');
  });

  it('should display proper error message on session expired error', () => {
    sharedService.raiseError(error401);
    spyOn(sharedService.passErrorToErrorPage, 'emit');
    component.ngOnInit();
    fixture.detectChanges();
    expect(component.errorMsg).toEqual('Session Expired');
  });

  it('should display proper error message on Unauthorised error', () => {
    sharedService.raiseError(error403);
    spyOn(sharedService.passErrorToErrorPage, 'emit');
    component.ngOnInit();
    fixture.detectChanges();
    expect(component.errorMsg).toEqual('Unauthorised action');
  });

  it('should display proper error message on 404 error', () => {
    sharedService.raiseError(error404);
    spyOn(sharedService.passErrorToErrorPage, 'emit');
    component.ngOnInit();
    fixture.detectChanges();
    expect(component.errorMsg).toEqual('API Not Found');
  });

  it('should display proper error message on 500 error', () => {
    sharedService.raiseError(error500);
    spyOn(sharedService.passErrorToErrorPage, 'emit');
    component.ngOnInit();
    fixture.detectChanges();
    expect(component.errorMsg).toEqual('Internal Server error');
  });

  it('should display proper error message on any error', () => {
    sharedService.raiseError(error405);
    spyOn(sharedService.passErrorToErrorPage, 'emit');
    component.ngOnInit();
    fixture.detectChanges();
    expect(component.errorMsg).toEqual('Some error occurred');
  });
  it('should display proper error message on 900 error', () => {
    sharedService.raiseError(error900);
    spyOn(sharedService.passErrorToErrorPage, 'emit');
    component.ngOnInit();
    fixture.detectChanges();
    expect(component.errorMsg).toEqual('Invalid URL.');
  });

  it('should display proper error message on 901 error', () => {
    sharedService.raiseError(error901);
    spyOn(sharedService.passErrorToErrorPage, 'emit');
    component.ngOnInit();
    fixture.detectChanges();
    expect(component.errorMsg).toEqual('No project access.');
  });


  describe('ErrorComponent.pollForAvailability() pollForAvailability method', () => {
    describe('Happy Path', () => {

      it('should initialize the timer and decrement timeLeft every second', fakeAsync(() => {
        component.timeLeft = 10;

        // Call the method with a route
        component.pollForAvailability('/test-route');

        // Simulate the passage of 1 second
        tick(1000);
        expect(component.timeLeft).toBe(9);

        // Simulate another second
        tick(1000);
        expect(component.timeLeft).toBe(8);

        // Clean up subscription
        component.source.unsubscribe();
      }));

      it('should reset timeLeft to 60 when it reaches 0', fakeAsync(() => {
        component.timeLeft = 1;

        // Call the method
        component.pollForAvailability('/test-route');

        // Simulate 1 second to bring timeLeft to 0
        tick(1000);
        expect(component.timeLeft).toBe(0);

        // Simulate another second, timeLeft should reset to 60
        tick(1000);
        expect(component.timeLeft).toBe(60);

        // Clean up subscription
        component.source.unsubscribe();
      }));

      it('should navigate after 60 seconds', fakeAsync(() => {
        component.timeLeft = 60;
        const redirectButtonRoute = 'dashboard';
        spyOn(component.router, 'navigate');
        // Call the method
        component.pollForAvailability(redirectButtonRoute);

        // Simulate 61 seconds
        tick(61000);

        // Ensure button text and navigation are triggered
        expect(component.router.navigate).toHaveBeenCalledWith([redirectButtonRoute]);

        // Clean up subscription
        component.source.unsubscribe();
      }));
    });

    describe('Edge Cases', () => {

      it('should not reinitialize the timer if already subscribed', fakeAsync(() => {
        component.source = new Subscription();
        spyOn(component.router, 'navigate');
        // Call the method
        component.pollForAvailability('/test-route');

        // Check that the timer is not reinitialized
        expect(component.source.closed).toBe(false);

        // Ensure that navigate is not called since timer wasn't reinitialized
        tick(60000);
        expect(component.router.navigate).not.toHaveBeenCalled();
      }));

      it('should handle case when timeLeft is initially zero', fakeAsync(() => {
        component.timeLeft = 0;

        // Call the method
        component.pollForAvailability('/test-route');

        // Simulate 1 second, expect timeLeft to reset to 60
        tick(1000);
        expect(component.timeLeft).toBe(60);

        // Clean up subscription
        component.source.unsubscribe();
      }));

      it('should not navigate if less than 60 seconds have passed', fakeAsync(() => {
        component.timeLeft = 10;
        const redirectButtonRoute = '/test-route';
        spyOn(component.router, 'navigate');
        // Call the method
        component.pollForAvailability(redirectButtonRoute);

        // Simulate 59 seconds
        tick(59000);

        // Ensure navigation has not occurred yet
        expect(component.router.navigate).not.toHaveBeenCalled();

        // Clean up subscription
        component.source.unsubscribe();
      }));

    });

  });
});
