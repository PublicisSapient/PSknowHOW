// import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
// import { NavNewComponent } from './nav-new.component';

// import { RouterTestingModule } from '@angular/router/testing';
// import { SharedService } from '../../services/shared.service';
// import { HelperService } from 'src/app/services/helper.service';
// import { GetAuthService } from '../../services/getauth.service';
// import { HttpClientModule } from '@angular/common/http';
// import { APP_CONFIG, AppConfig } from '../../services/app.config';
// import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
// import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
// import { HttpService } from '../../services/http.service';
// import { CommonModule, DatePipe } from '@angular/common';
// import { MessageService } from 'primeng/api';
// import { of, throwError } from 'rxjs';
// import { Routes } from '@angular/router';
// import { ExecutiveV2Component } from '../executive-v2/executive-v2.component';
// import { MaturityComponent } from 'src/app/dashboard/maturity/maturity.component';

// const getDashConfData = require('../../../test/resource/boardConfigNewServer.json');

// describe('NavNewComponent', () => {
//   let component: NavNewComponent;
//   let fixture: ComponentFixture<NavNewComponent>;
//   let getAuth: GetAuthService;
//   let httpService: HttpService
//   let sharedService: SharedService;
//   let helperService: HelperService;
//   let messageService: MessageService;
//   let mockRouter;

//   beforeEach(async () => {

//     const routes: Routes = [
//       { path: 'dashboard/my-knowhow', component: ExecutiveV2Component },
//       { path: 'dashboard/dashboard', component: ExecutiveV2Component },
//       { path: 'dashboard/kpi-maturity', component: MaturityComponent },
//     ];


//     await TestBed.configureTestingModule({
//       declarations: [NavNewComponent],
//       imports: [RouterTestingModule.withRoutes(routes), HttpClientModule, BrowserAnimationsModule],
//       schemas: [CUSTOM_ELEMENTS_SCHEMA],

//       providers: [SharedService, GetAuthService, HttpService, HelperService, CommonModule, DatePipe, MessageService,
//         { provide: APP_CONFIG, useValue: AppConfig }
//       ]
//     })
//       .compileComponents();

//     fixture = TestBed.createComponent(NavNewComponent);
//     component = fixture.componentInstance;
//     getAuth = TestBed.get(GetAuthService);
//     httpService = TestBed.inject(HttpService);
//     sharedService = TestBed.inject(SharedService);
//     helperService = TestBed.inject(HelperService);
//     messageService = TestBed.inject(MessageService);
//     mockRouter = jasmine.createSpyObj('Router', ['navigate']);
//     fixture.detectChanges();
//   });

//   it('should create', () => {
//     expect(component).toBeTruthy();
//   });

//   it('should set dashConfigData and items correctly on successful response', () => {
//     let httpServiceMock = jasmine.createSpyObj('HttpService', ['getShowHideOnDashboardNewUI']);
//     let localStorageMock = jasmine.createSpyObj('LocalStorage', ['getItem']);
//     const response = {
//       success: true,
//       data: {
//         userBoardConfigDTO: {
//           scrum: [
//             {
//               boardName: 'Scrum Board 1',
//               boardSlug: 'scrum-board-1',
//               filters: {
//                 primaryFilter: {
//                   defaultLevel: {
//                     labelName: 'Level 1',
//                   },
//                 },
//                 parentFilter: {
//                   labelName: 'Parent Level',
//                 },
//                 additionalFilters: [
//                   {
//                     defaultLevel: {
//                       labelName: 'Level 2',
//                     },
//                   },
//                 ],
//               },
//             },
//           ],
//           others: [
//             {
//               boardName: 'Other Board 1',
//               boardSlug: 'other-board-1',
//               filters: {
//                 primaryFilter: {
//                   defaultLevel: {
//                     labelName: 'Level 3',
//                   },
//                 },
//                 parentFilter: {
//                   labelName: 'Parent Level',
//                 },
//               },
//             },
//           ],
//         },
//         configDetails: {},
//       },
//     };

//     const levelDetails = [
//       {
//         hierarchyLevelId: 'level-1',
//         hierarchyLevelName: 'Level 1',
//       },
//       {
//         hierarchyLevelId: 'level-2',
//         hierarchyLevelName: 'Level 2',
//       },
//       {
//         hierarchyLevelId: 'level-3',
//         hierarchyLevelName: 'Level 3',
//       },
//     ];

//     localStorageMock.getItem.and.returnValue(JSON.stringify({ selectedType: 'scrum' }));

//     httpServiceMock.getShowHideOnDashboardNewUI.and.returnValue(of(response));

//     component.getBoardConfig(['project-1']);

//     // expect(component.dashConfigData).toEqual(response.data.userBoardConfigDTO);
//     // expect(component.items).toEqual([
//     //   {
//     //     label: 'Scrum Board 1',
//     //     slug: 'scrum-board-1',
//     //     command: jasmine.any(Function),
//     //   },
//     //   {
//     //     label: 'Other Board 1',
//     //     slug: 'other-board-1',
//     //     command: jasmine.any(Function),
//     //   },
//     // ]);
//     // expect(component.activeItem).toEqual({
//     //   label: 'Scrum Board 1',
//     //   slug: 'scrum-board-1',
//     //   command: jasmine.any(Function),
//     // });
//   });

//   it('should set the selectedTab correctly', fakeAsync(() => {
//     const obj = { boardSlug: 'my-knowhow', boardName: 'My KnowHOW' };
//     const setSelectedTypeOrTabRefreshSpy = spyOn(sharedService, 'setSelectedTypeOrTabRefresh');
//     // const navigateSpy = spyOn(mockRouter, 'navigate');
//     component.handleMenuTabFunctionality(obj);
//     // expect(mockRouter.navigate).toHaveBeenCalledWith(['dashboard/my-knowhow']);
//     tick(200);
//     expect(setSelectedTypeOrTabRefreshSpy).toHaveBeenCalledWith('my-knowhow', 'scrum');
//   }
//   ));

//   it('should not call setDashConfigData when boardName is not "Kpi Maturity"', fakeAsync(() => {
//     const obj = { boardSlug: 'dashboard', boardName: 'Other Board' };
//     const setDashConfigDataSpy = spyOn(sharedService, 'setDashConfigData');
//     component.handleMenuTabFunctionality(obj);
//     tick(200);
//     expect(setDashConfigDataSpy).not.toHaveBeenCalled();
//   }
//   ));

//   xit('should navigate to the correct route', fakeAsync(() => {
//     const obj = { boardSlug: 'kpi-maturity', boardName: 'Kpi Maturity' };

//     component.handleMenuTabFunctionality(obj);
//     tick(200);
//     expect(mockRouter.navigate).toHaveBeenCalledWith(['dashboard/kpi-maturity']);
//   }));

//   it('should return true if obj1 and obj2 are the same object', () => {
//     const obj = { prop: 'value' };

//     const result = component.deepEqual(obj, obj);

//     expect(result).toBe(true);
//   });

//   it('should return false if obj1 or obj2 is null', () => {
//     const obj1 = { prop: 'value' };
//     const obj2 = null;

//     const result = component.deepEqual(obj1, obj2);

//     expect(result).toBe(false);
//   });

//   it('should return false if obj1 or obj2 is not an object', () => {
//     const obj1 = { prop: 'value' };
//     const obj2 = 'string';

//     const result = component.deepEqual(obj1, obj2);

//     expect(result).toBe(false);
//   });

//   it('should return false if obj1 and obj2 have different number of keys', () => {
//     const obj1 = { prop1: 'value1', prop2: 'value2' };
//     const obj2 = { prop1: 'value1' };

//     const result = component.deepEqual(obj1, obj2);

//     expect(result).toBe(false);
//   });

//   it('should return false if obj1 and obj2 have different keys', () => {
//     const obj1 = { prop1: 'value1' };
//     const obj2 = { prop2: 'value2' };

//     const result = component.deepEqual(obj1, obj2);

//     expect(result).toBe(false);
//   });

//   it('should return false if obj1 and obj2 have different values for the same key', () => {
//     const obj1 = { prop: 'value1' };
//     const obj2 = { prop: 'value2' };

//     const result = component.deepEqual(obj1, obj2);

//     expect(result).toBe(false);
//   });

//   it('should return true if obj1 and obj2 have the same keys and values', () => {
//     const obj1 = { prop1: 'value1', prop2: 'value2' };
//     const obj2 = { prop1: 'value1', prop2: 'value2' };

//     const result = component.deepEqual(obj1, obj2);

//     expect(result).toBe(true);
//   });

//   it('should return true if both arrays are empty', () => {
//     const array1: string[] = [];
//     const array2: string[] = [];

//     const result = component.compareStringArrays(array1, array2);

//     expect(result).toBe(true);
//   });

//   it('should return false if arrays have different lengths', () => {
//     const array1 = ['a', 'b', 'c'];
//     const array2 = ['a', 'b'];

//     const result = component.compareStringArrays(array1, array2);

//     expect(result).toBe(false);
//   });

//   it('should return false if arrays have different elements', () => {
//     const array1 = ['a', 'b', 'c'];
//     const array2 = ['a', 'd', 'c'];

//     const result = component.compareStringArrays(array1, array2);

//     expect(result).toBe(false);
//   });

//   it('should return true if arrays have the same elements in the same order', () => {
//     const array1 = ['a', 'b', 'c'];
//     const array2 = ['a', 'b', 'c'];

//     const result = component.compareStringArrays(array1, array2);

//     expect(result).toBe(true);
//   });

//   it('should return true if arrays have the same elements in a different order', () => {
//     const array1 = ['a', 'b', 'c'];
//     const array2 = ['c', 'a', 'b'];

//     const result = component.compareStringArrays(array1, array2);

//     expect(result).toBe(false);
//   });

//   // setBoards(response)

//   // end of setBoards(response)
// });

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NavNewComponent } from './nav-new.component';
import { HttpService } from '../../services/http.service';
import { SharedService } from '../../services/shared.service';
import { MessageService } from 'primeng/api';
import { Router } from '@angular/router';
import { of, Subject, throwError } from 'rxjs';
import { HelperService } from 'src/app/services/helper.service';

describe('NavNewComponent', () => {
  let component: NavNewComponent;
  let fixture: ComponentFixture<NavNewComponent>;
  let httpService: jasmine.SpyObj<HttpService>;
  let sharedService: jasmine.SpyObj<SharedService>;
  let messageService: jasmine.SpyObj<MessageService>;
  let router: jasmine.SpyObj<Router>;
  let helperService: jasmine.SpyObj<HelperService>;

  beforeEach(async () => {
    const httpSpy = jasmine.createSpyObj('HttpService', ['getShowHideOnDashboardNewUI', 'getAllHierarchyLevels']);
    const sharedSpy = jasmine.createSpyObj('SharedService', ['getSelectedType', 'setSelectedTypeOrTabRefresh', 'getSelectedTrends', 'setDashConfigData', 'selectedTrendsEvent', 'onTypeOrTabRefresh', 'setSelectedType']);
    const messageSpy = jasmine.createSpyObj('MessageService', ['add']);
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    const helperSpy = jasmine.createSpyObj('HelperService', ['setBackupOfFilterSelectionState']);

    await TestBed.configureTestingModule({
      declarations: [NavNewComponent],
      providers: [
        { provide: HttpService, useValue: httpSpy },
        { provide: SharedService, useValue: sharedSpy },
        { provide: MessageService, useValue: messageSpy },
        { provide: Router, useValue: routerSpy },
        { provide: HelperService, useValue: helperSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(NavNewComponent);
    component = fixture.componentInstance;
    httpService = TestBed.inject(HttpService) as jasmine.SpyObj<HttpService>;
    sharedService = TestBed.inject(SharedService) as jasmine.SpyObj<SharedService>;
    messageService = TestBed.inject(MessageService) as jasmine.SpyObj<MessageService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    helperService = TestBed.inject(HelperService) as jasmine.SpyObj<HelperService>;
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  describe('ngOnInit', () => {
    beforeEach(() => {
      sharedService.onTypeOrTabRefresh = new Subject(); // Mocking Subject
      sharedService.selectedTrendsEvent = new Subject();
      sharedService.getSelectedTrends.and.returnValue([{ basicProjectConfigId: 'proj1' }]);
    });

    it('should initialize selectedTab and selectedType', () => {
      spyOn(component, 'getBoardConfig');

      component.ngOnInit();

      expect(component.selectedTab).toBe('iteration');
      expect(sharedService.setSelectedTypeOrTabRefresh).toHaveBeenCalled();
      expect(component.selectedType).toBe('scrum');
      expect(component.getBoardConfig).toHaveBeenCalledWith(['proj1']);
    });

    it('should update selectedType when onTypeOrTabRefresh emits', () => {
      spyOn(component, 'getBoardConfig');

      component.ngOnInit();
      sharedService.onTypeOrTabRefresh.next({ selectedTab: 'speed', selectedType: 'kanban' });

      expect(component.selectedType).toBe('kanban');
      expect(sharedService.setSelectedType).toHaveBeenCalledWith('kanban');
    });

    it('should refresh the board config when selectedTrendsEvent emits', () => {
      spyOn(component, 'getBoardConfig');
      component.selectedBasicConfigIds = ['proj1'];

      component.ngOnInit();
      sharedService.selectedTrendsEvent.next([{ basicProjectConfigId: 'proj2' }]);

      expect(component.selectedBasicConfigIds).toEqual(['proj2']);
      expect(component.getBoardConfig).toHaveBeenCalledWith(['proj2']);
    });
  });

  describe('ngOnDestroy', () => {
    it('should unsubscribe all subscriptions', () => {
      const subscriptionMock = jasmine.createSpyObj('Subscription', ['unsubscribe']);
      component.subscriptions = [subscriptionMock, subscriptionMock];

      component.ngOnDestroy();

      expect(subscriptionMock.unsubscribe).toHaveBeenCalledTimes(2);
    });
  });

  describe('getBoardConfig', () => {
    it('should call httpService and handle success response', () => {
      const responseMock = { success: true, data: { userBoardConfigDTO: {} } };
      spyOn(component, 'setBoards');
      httpService.getShowHideOnDashboardNewUI.and.returnValue(of(responseMock));

      component.getBoardConfig(['proj1']);

      expect(httpService.getShowHideOnDashboardNewUI).toHaveBeenCalledWith({ basicProjectConfigIds: ['proj1'] });
      expect(component.setBoards).toHaveBeenCalledWith(responseMock);
    });

    it('should handle error and call MessageService.add when getShowHideOnDashboardNewUI fails', () => {
      // Simulate an error in the HTTP call
      httpService.getShowHideOnDashboardNewUI.and.returnValue(throwError({ message: 'Error' }));

      // Call the function that triggers the HTTP call
      component.getBoardConfig([]);

      // Now, check that MessageService.add was called with the expected error object
      expect(messageService.add).toHaveBeenCalledWith({
        severity: 'error',
        summary: 'Error',
      });
    });
  });

  describe('setBoards', () => {
    beforeEach(() => {
      localStorage.setItem('completeHierarchyData', JSON.stringify({
        scrum: [
          { hierarchyLevelId: 'level1', hierarchyLevelName: 'Level 1' }
        ]
      }));
      component.selectedType = 'scrum';
    });

    it('should set board data when localStorage has hierarchy data', () => {
      const responseMock = {
        success: true,
        data: {
          userBoardConfigDTO: {
            scrum: [
              { boardName: 'Scrum Board', boardSlug: 'scrum-board', filters: { primaryFilter: { defaultLevel: { labelName: 'level1' } } } }
            ],
            others: []
          }
        }
      };

      component.setBoards(responseMock);

      expect(component.items.length).toBe(1);
      expect(component.items[0].label).toBe('Scrum Board');
    });

    it('should call getAllHierarchyLevels when localStorage is empty', () => {
      localStorage.removeItem('completeHierarchyData');
      const responseMock = { success: true, data: { userBoardConfigDTO: { scrum: [], others: [] } } };
      httpService.getAllHierarchyLevels.and.returnValue(of({ data: 'mockHierarchyData' }));

      component.setBoards(responseMock);

      expect(httpService.getAllHierarchyLevels).toHaveBeenCalled();
    });
  });

  describe('handleMenuTabFunctionality', () => {
    it('should navigate to the correct route and update selectedTab', () => {
      const mockObj = { boardSlug: 'iteration' };
      component.handleMenuTabFunctionality(mockObj);

      expect(component.selectedTab).toBe('iteration');
      expect(router.navigate).toHaveBeenCalledWith(['/dashboard/iteration']);
    });

    it('should set backup filter selection state when tab is iteration', () => {
      const mockObj = { boardSlug: 'iteration' };

      component.handleMenuTabFunctionality(mockObj);

      expect(component.helperService.setBackupOfFilterSelectionState).toHaveBeenCalledWith({ 'additional_level': null });
    });
  });

  describe('deepEqual', () => {
    it('should return true for equal objects', () => {
      const obj1 = { a: 1, b: 2 };
      const obj2 = { a: 1, b: 2 };

      expect(component.deepEqual(obj1, obj2)).toBeTrue();
    });

    it('should return false for non-equal objects', () => {
      const obj1 = { a: 1, b: 2 };
      const obj2 = { a: 1, b: 3 };

      expect(component.deepEqual(obj1, obj2)).toBeFalse();
    });
  });

  describe('compareStringArrays', () => {
    it('should return true for equal arrays', () => {
      const array1 = ['a', 'b'];
      const array2 = ['a', 'b'];

      expect(component.compareStringArrays(array1, array2)).toBeTrue();
    });

    it('should return false for non-equal arrays', () => {
      const array1 = ['a', 'b'];
      const array2 = ['a', 'c'];

      expect(component.compareStringArrays(array1, array2)).toBeFalse();
    });
  });
});

