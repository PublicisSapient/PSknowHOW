import { HttpRequest, HttpHandler, HttpClient, HTTP_INTERCEPTORS } from '@angular/common/http';
import { HttpsRequestInterceptor } from './interceptor.module';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed, inject } from '@angular/core/testing';
import { SharedService } from '../../core/services/shared.service';

describe('HttpsRequestInterceptor', () => {
    let httpClient: HttpClient;
    let httpTestingController: HttpTestingController;
    let service: SharedService;
  
    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [HttpClientTestingModule],
        providers: [
          { provide: SharedService, useValue: service },
          {
            provide: HTTP_INTERCEPTORS,
            useClass: HttpsRequestInterceptor,
            multi: true
          }
        ]
      });
  
      httpClient = TestBed.inject(HttpClient);
      httpTestingController = TestBed.inject(HttpTestingController);
    });
    
    afterEach(() => {
        httpTestingController.verify(); // Verifies that no requests are outstanding
      });
    
    // xit('should add custom header to HTTP request', inject(
    //     [HttpClient, HttpTestingController],
    //     (http: HttpClient, controller: HttpTestingController) => {
    //         const testData = { message: 'test data' };

    //         // Make an HTTP request
    //         http.get('/api/getversionmetadata').subscribe(response => {
    //         expect(response).toBeTruthy();
    //         expect(response).toEqual(testData);
    //         });

    //         // Expect outgoing request with custom header
    //         const req = controller.expectOne('/api/getversionmetadata');
    //         expect(req.request.headers.get('auth-details-updated')).toBe('true');

    //         // Respond with mock data
    //         req.flush(testData);
    //     }
    // ));

//   xit('should modify the request headers correctly for upload and emm-feed URLs', () => {
//     // set the sample request
//     mockRequest.headers = jasmine.createSpyObj('HttpHeaders', ['get', 'delete', 'set', 'has']);
//     mockRequest.headers.has.and.returnValue(false);
//     mockRequest.url = 'http://example.com/api/upload';

//     // call the method
//     component.intercept(mockRequest, mockNext);

//     // check if the request headers were modified correctly
//     expect(mockRequest.headers.set).toHaveBeenCalledWith('Content-Type', ['application/json']);

//     // reset the request
//     mockRequest.headers.set.calls.reset();
//     mockRequest.url = 'http://example.com/api/emm-feed';

//     // call the method
//     component.intercept(mockRequest, mockNext);

//     // check if the request headers were modified correctly
//     expect(mockRequest.headers.set).toHaveBeenCalledWith('Content-Type', ['text/csv']);
//   });
});
