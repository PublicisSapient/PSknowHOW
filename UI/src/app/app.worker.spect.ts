// import { TestBed } from '@angular/core/testing';
// import { WorkerService } from './worker.service';

// describe('WorkerService', () => {
//   let service: WorkerService;

//   beforeEach(() => {
//     TestBed.configureTestingModule({});
//     service = TestBed.inject(WorkerService);
//   });

//   it('should be created', () => {
//     expect(service).toBeTruthy();
//   });

//   describe('getProjects', () => {
//     it('should return an array of projects', (done) => {
//       const authToken = 'Bearer token';
//       const worker = service.getProjects(authToken);
//       worker.onmessage = (event) => {
//         expect(event.data).toEqual([{ name: 'Project 1', id: 1 }, { name: 'Project 2', id: 2 }]);
//         done();
//       };
//     });

//     it('should make a GET request to the API with the authorization header', () => {
//       const authToken = 'Bearer token';
//       spyOn(XMLHttpRequest.prototype, 'open');
//       spyOn(XMLHttpRequest.prototype, 'setRequestHeader');
//       spyOn(XMLHttpRequest.prototype, 'send');
//       service.getProjects(authToken);
//       expect(XMLHttpRequest.prototype.open).toHaveBeenCalledWith('GET', 'http://localhost:3000/api/basicconfigs/all', true);
//       expect(XMLHttpRequest.prototype.setRequestHeader).toHaveBeenCalledWith('Authorization', authToken);
//       expect(XMLHttpRequest.prototype.send).toHaveBeenCalled();
//     });

//     it('should handle errors in the response', (done) => {
//       const authToken = 'Bearer token';
//       const worker = service.getProjects(authToken);
//       worker.onmessage = (event) => {
//         expect(event.data).toEqual([]);
//         done();
//       };
//       spyOn(XMLHttpRequest.prototype, 'send').and.callFake(() => {
//         const response = { success: false, message: 'Error' };
//         const event = { readyState: 4, status: 500, responseText: JSON.stringify(response) };
//         XMLHttpRequest.prototype.onreadystatechange.call(event);
//       });
//     });

//     it('should handle empty response data', (done) => {
//       const authToken = 'Bearer token';
//       const worker = service.getProjects(authToken);
//       worker.onmessage = (event) => {
//         expect(event.data).toEqual([]);
//         done();
//       };
//       spyOn(XMLHttpRequest.prototype, 'send').and.callFake(() => {
//         const response = { success: true, data: [] };
//         const event = { readyState: 4, status: 200, responseText: JSON.stringify(response) };
//         XMLHttpRequest.prototype.onreadystatechange.call(event);
//       });
//     });
//   });
// });
