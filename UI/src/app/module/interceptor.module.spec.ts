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
import { HTTP_INTERCEPTORS, HttpClient, HttpRequest } from '@angular/common/http';
import { HttpsRequestInterceptor } from './interceptor.module';
import { GetAuthService } from '../services/getauth.service';
import { SharedService } from '../services/shared.service';
import { HttpService } from '../services/http.service';
import { of, throwError } from 'rxjs';
import { environment } from 'src/environments/environment';
import { Router, ActivatedRoute } from '@angular/router';

describe('HttpsRequestInterceptor', () => {
  let httpClient: HttpClient;
  let httpMock: HttpTestingController;
  let interceptor: HttpsRequestInterceptor;
  let mockGetAuthService: jasmine.SpyObj<GetAuthService>;
  let mockSharedService: jasmine.SpyObj<SharedService>;
  let mockHttpService: jasmine.SpyObj<HttpService>;
  let mockRouter: jasmine.SpyObj<Router>;

  beforeEach(() => {
    mockGetAuthService = jasmine.createSpyObj('GetAuthService', ['checkAuth']);
    mockSharedService = jasmine.createSpyObj('SharedService', ['getCurrentUserDetails', 'clearAllCookies']);
    mockHttpService = jasmine.createSpyObj('HttpService', ['setCurrentUserDetails', 'unauthorisedAccess']);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);
    const mockActivatedRoute = {
      snapshot: {
        queryParams: { returnUrl: '/' },
      },
      queryParams: of({ sessionExpire: 'Session expired' }),
    };

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        HttpsRequestInterceptor,
        { provide: GetAuthService, useValue: mockGetAuthService },
        { provide: SharedService, useValue: mockSharedService },
        { provide: HttpService, useValue: mockHttpService },
        { provide: Router, useValue: mockRouter },
        { provide: ActivatedRoute, useValue: mockActivatedRoute },
        { provide: HTTP_INTERCEPTORS, useClass: HttpsRequestInterceptor, multi: true }
      ]
    });

    httpClient = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
    //interceptor = TestBed.inject(HttpsRequestInterceptor);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should add withCredentials and requestId header', () => {
    const mockUrl = '/api/test';
    const requestIdPattern = /^[0-9a-fA-F-]{36}$/; // UUID format

    httpClient.get(mockUrl).subscribe();

    const req = httpMock.expectOne(mockUrl);
    expect(req.request.withCredentials).toBeTrue();
    expect(req.request.headers.has('request-Id')).toBeTrue();
    expect(req.request.headers.get('request-Id')).toMatch(requestIdPattern);

    req.flush({});
  });

  it('should remove httpErrorHandler and requestArea headers if present', () => {
    const mockUrl = '/api/test';

    httpClient.get(mockUrl, {
      headers: {
        httpErrorHandler: 'local',
        requestArea: 'external'
      }
    }).subscribe();

    const req = httpMock.expectOne(mockUrl);
    expect(req.request.headers.has('httpErrorHandler')).toBeFalse();
    expect(req.request.headers.has('requestArea')).toBeFalse();

    req.flush({});
  });

  it('should set content-type header to application/json for non-upload requests', () => {
    const mockUrl = '/api/test';

    httpClient.get(mockUrl).subscribe();

    const req = httpMock.expectOne(mockUrl);
    expect(req.request.headers.get('Content-Type')).toBe('application/json');

    req.flush({});
  });

  it('should set content-type header to text/csv for emm-feed requests', () => {
    const mockUrl = '/api/emm-feed/test';

    httpClient.get(mockUrl).subscribe();

    const req = httpMock.expectOne(mockUrl);
    expect(req.request.headers.get('Content-Type')).toBe('text/csv');

    req.flush({});
  });

  it('should handle 401 unauthorized error and navigate to login', () => {
    const mockUrl = '/api/test';
    const mockErrorResponse = { status: 401, statusText: 'Unauthorized' };

    httpClient.get(mockUrl).subscribe({
      error: () => {
        expect(mockHttpService.setCurrentUserDetails).toHaveBeenCalledWith({});
        expect(mockRouter.navigate).toHaveBeenCalledWith(['./authentication/login'], { queryParams: {  returnUrl: '/' } });
      }
    });

    httpMock.expectOne(mockUrl).flush(null, mockErrorResponse);
  });

  xit('should handle 403 forbidden error and navigate to unauthorized page', () => {
    const mockUrl = '/api/test';
    const mockErrorResponse = { status: 403, statusText: 'Forbidden' };

    httpClient.get(mockUrl).subscribe({
      error: () => {
        expect(mockHttpService.unauthorisedAccess).toBeUndefined();
        expect(mockRouter.navigate).toHaveBeenCalledWith(['./dashboard/Error']);
      }
    });

    httpMock.expectOne(mockUrl).flush(null, mockErrorResponse);
  });

  it('should navigate to error page if request is blocked', () => {
    const mockUrl = '/api/test';
    const mockErrorResponse = { status: 500, statusText: 'Internal Server Error' };

    httpClient.get(mockUrl).subscribe({
      error: () => {
        expect(mockRouter.navigate).toHaveBeenCalledWith(['./dashboard/Error']);
      }
    });

    httpMock.expectOne(mockUrl).flush(null, mockErrorResponse);
  });

  it('should not navigate to error page if request URL is in redirectExceptions', () => {
    const mockUrl = environment.baseUrl + '/api/jira/kpi';
    const mockErrorResponse = { status: 500, statusText: 'Internal Server Error' };

    httpClient.get(mockUrl).subscribe({
      error: () => {
        expect(mockRouter.navigate).not.toHaveBeenCalled();
      }
    });

    httpMock.expectOne(mockUrl).flush(null, mockErrorResponse);
  });

});

