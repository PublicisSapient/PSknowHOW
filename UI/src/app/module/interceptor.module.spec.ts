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

import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HTTP_INTERCEPTORS, HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { GetAuthService } from '../services/getauth.service';
import { SharedService } from '../services/shared.service';
import { HttpService } from '../services/http.service';
import { environment } from '../../environments/environment';
import { HttpsRequestInterceptor } from './interceptor.module';

describe('HttpsRequestInterceptor', () => {
  let httpMock: HttpTestingController;
  let httpClient: HttpClient;
  let router: jasmine.SpyObj<Router>;
  let getAuthService: jasmine.SpyObj<GetAuthService>;
  let sharedService: jasmine.SpyObj<SharedService>;
  let httpService: jasmine.SpyObj<HttpService>;

  beforeEach(() => {
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    const getAuthSpy = jasmine.createSpyObj('GetAuthService', ['getAuthDetails']);
    const sharedSpy = jasmine.createSpyObj('SharedService', ['clearAllCookies']);
    const httpSpy = jasmine.createSpyObj('HttpService', ['setCurrentUserDetails', 'getAuthDetails']);

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        { provide: HTTP_INTERCEPTORS, useClass: HttpsRequestInterceptor, multi: true },
        { provide: Router, useValue: routerSpy },
        { provide: GetAuthService, useValue: getAuthSpy },
        { provide: SharedService, useValue: sharedSpy },
        { provide: HttpService, useValue: httpSpy }
      ]
    });

    httpMock = TestBed.inject(HttpTestingController);
    httpClient = TestBed.inject(HttpClient);
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    getAuthService = TestBed.inject(GetAuthService) as jasmine.SpyObj<GetAuthService>;
    sharedService = TestBed.inject(SharedService) as jasmine.SpyObj<SharedService>;
    httpService = TestBed.inject(HttpService) as jasmine.SpyObj<HttpService>;
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should add Content-Type header for JSON requests', () => {
    httpClient.get('/test').subscribe();

    const req = httpMock.expectOne('/test');
    expect(req.request.headers.get('Content-Type')).toBe('application/json');
  });


  it('should handle 403 error by navigating to unauthorized access', () => {
    environment.SSO_LOGIN = true;
    httpClient.get('/test').subscribe(
      () => fail('should have failed with 403 error'),
      () => {
        expect(router.navigate).toHaveBeenCalledWith(['/dashboard/unauthorized-access']);
      }
    );

    const req = httpMock.expectOne('/test');
    req.flush({}, { status: 403, statusText: 'Forbidden' });
  });
});
