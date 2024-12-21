import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NavNewComponent } from './nav-new.component';
import { HttpService } from '../../services/http.service';
import { SharedService } from '../../services/shared.service';
import { MessageService } from 'primeng/api';
import { Router } from '@angular/router';
import { of, Subject, throwError } from 'rxjs';
import { HelperService } from 'src/app/services/helper.service';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { APP_CONFIG, AppConfig } from '../../services/app.config';

const mockHierarchyData = {
  "kanban": [
    {
      "id": "6442815917ed167d8157f0f5",
      "level": 1,
      "hierarchyLevelId": "bu",
      "hierarchyLevelName": "BU",
      "hierarchyInfo": "Business Unit"
    },
    {
      "id": "6442815917ed167d8157f0f6",
      "level": 2,
      "hierarchyLevelId": "ver",
      "hierarchyLevelName": "Vertical",
      "hierarchyInfo": "Industry"
    },
    {
      "id": "6442815917ed167d8157f0f7",
      "level": 3,
      "hierarchyLevelId": "acc",
      "hierarchyLevelName": "Account",
      "hierarchyInfo": "Account"
    },
    {
      "id": "6442815917ed167d8157f0f8",
      "level": 4,
      "hierarchyLevelId": "port",
      "hierarchyLevelName": "Engagement",
      "hierarchyInfo": "Engagement"
    },
    {
      "level": 5,
      "hierarchyLevelId": "project",
      "hierarchyLevelName": "Project"
    },
    {
      "level": 6,
      "hierarchyLevelId": "release",
      "hierarchyLevelName": "Release"
    },
    {
      "level": 7,
      "hierarchyLevelId": "sqd",
      "hierarchyLevelName": "Squad"
    }
  ],
  "scrum": [
    {
      "id": "6442815917ed167d8157f0f5",
      "level": 1,
      "hierarchyLevelId": "bu",
      "hierarchyLevelName": "BU",
      "hierarchyInfo": "Business Unit"
    },
    {
      "id": "6442815917ed167d8157f0f6",
      "level": 2,
      "hierarchyLevelId": "ver",
      "hierarchyLevelName": "Vertical",
      "hierarchyInfo": "Industry"
    },
    {
      "id": "6442815917ed167d8157f0f7",
      "level": 3,
      "hierarchyLevelId": "acc",
      "hierarchyLevelName": "Account",
      "hierarchyInfo": "Account"
    },
    {
      "id": "6442815917ed167d8157f0f8",
      "level": 4,
      "hierarchyLevelId": "port",
      "hierarchyLevelName": "Engagement",
      "hierarchyInfo": "Engagement"
    },
    {
      "level": 5,
      "hierarchyLevelId": "project",
      "hierarchyLevelName": "Project"
    },
    {
      "level": 6,
      "hierarchyLevelId": "sprint",
      "hierarchyLevelName": "Sprint"
    },
    {
      "level": 6,
      "hierarchyLevelId": "release",
      "hierarchyLevelName": "Release"
    },
    {
      "level": 7,
      "hierarchyLevelId": "sqd",
      "hierarchyLevelName": "Squad"
    }
  ]
};
describe('NavNewComponent', () => {
  let component: NavNewComponent;
  let fixture: ComponentFixture<NavNewComponent>;
  let httpService: jasmine.SpyObj<HttpService>;
  let sharedService: jasmine.SpyObj<SharedService>;
  let messageService: jasmine.SpyObj<MessageService>;
  let router: jasmine.SpyObj<Router>;
  let helperService: jasmine.SpyObj<HelperService>;

  const mockWindowLocationHash = (hash: string) => {
    const location = {
      ...window.location,
      hash,
    };
    spyOnProperty(window, 'location', 'get').and.returnValue(location);
  };

  beforeEach(async () => {
    const httpSpy = jasmine.createSpyObj('HttpService', ['getShowHideOnDashboardNewUI', 'getAllHierarchyLevels']);
    const sharedSpy = jasmine.createSpyObj('SharedService', ['getSelectedType', 'setSelectedBoard', 'setScrumKanban', 'getSelectedTrends', 'setDashConfigData', 'selectedTrendsEvent', 'onTypeOrTabRefresh', 'setSelectedType', 'setCurrentUserDetails', 'currentUserDetailsSubject']);
    const messageSpy = jasmine.createSpyObj('MessageService', ['add']);
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    const helperSpy = jasmine.createSpyObj('HelperService', ['setBackupOfFilterSelectionState', 'deepEqual']);

    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      declarations: [NavNewComponent],
      providers: [
        { provide: HttpService, useValue: httpSpy },
        { provide: SharedService, useValue: sharedSpy },
        { provide: MessageService, useValue: messageSpy },
        { provide: APP_CONFIG, useValue: AppConfig },
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

    // Set default values for shared service spies
    sharedService.getSelectedType.and.returnValue('scrum');
    sharedService.getSelectedTrends.and.returnValue([]);

    const mockResponse = { data: 'some data' };  // Mock response from the service
    httpService.getShowHideOnDashboardNewUI.and.returnValue(of(mockResponse));
    // sharedService.setCurrentUserDetails({
    //   "user_name": "SUPERADMIN",
    //   "user_email": "abc@def.com",
    //   "authType": "STANDARD",
    //   "authorities": [
    //     "ROLE_SUPERADMIN"
    //   ],
    //   "projectsAccess": [],
    //   "notificationEmail": {
    //     "accessAlertNotification": false,
    //     "errorAlertNotification": false
    //   }
    // });
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  xit('should set selectedTab based on window hash and transform it', () => {
    mockWindowLocationHash('#/some/path/some tab');

    component.ngOnInit();

    expect(component.selectedTab).toBe('some-tab');
    expect(sharedService.setSelectedBoard).toHaveBeenCalledWith('some-tab');
  });

  xit('should use "kanban" type if sharedService.getSelectedType returns "kanban"', () => {
    sharedService.getSelectedType.and.returnValue('kanban');

    component.ngOnInit();

    expect(component.selectedType).toBe('kanban');
    expect(sharedService.setScrumKanban).toHaveBeenCalledWith('kanban');
  });

  it('should call setSelectedBoard if selectedTab is not "unauthorized access"', () => {
    const obj = { boardSlug: 'iteration' };

    component.handleMenuTabFunctionality(obj);

    expect(sharedService.setSelectedBoard).toHaveBeenCalledWith('iteration');
    expect(router.navigate).toHaveBeenCalledWith(['/dashboard/iteration']);
  });

  it('should not call setSelectedBoard if selectedTab is "unauthorized access"', () => {
    const obj = { boardSlug: 'unauthorized access' };

    component.handleMenuTabFunctionality(obj);

    expect(sharedService.setSelectedBoard).not.toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/dashboard/unauthorized access']);
  });

  it('should call setBackupOfFilterSelectionState for specific tabs', () => {
    const obj = { boardSlug: 'iteration' };

    component.handleMenuTabFunctionality(obj);

    expect(helperService.setBackupOfFilterSelectionState).toHaveBeenCalledWith({ 'additional_level': null });
    expect(router.navigate).toHaveBeenCalledWith(['/dashboard/iteration']);
  });

  it('should call setBackupOfFilterSelectionState for release tab', () => {
    const obj = { boardSlug: 'release' };

    component.handleMenuTabFunctionality(obj);

    expect(helperService.setBackupOfFilterSelectionState).toHaveBeenCalledWith({ 'additional_level': null });
    expect(router.navigate).toHaveBeenCalledWith(['/dashboard/release']);
  });

  it('should not call setBackupOfFilterSelectionState for other tabs', () => {
    const obj = { boardSlug: 'some-other-tab' };

    component.handleMenuTabFunctionality(obj);

    expect(helperService.setBackupOfFilterSelectionState).not.toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/dashboard/some-other-tab']);
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
      component.selectedType = 'scrum';
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
    xit('should navigate to the correct route and update selectedTab', () => {
      const mockObj = { boardSlug: 'iteration' };
      component.handleMenuTabFunctionality(mockObj);

      expect(component.selectedTab).toBe('iteration');
      expect(router.navigate).toHaveBeenCalledWith(['/dashboard/iteration']);
    });

    xit('should set backup filter selection state when tab is iteration', () => {
      const mockObj = { boardSlug: 'iteration' };

      component.handleMenuTabFunctionality(mockObj);

      expect(component.helperService.setBackupOfFilterSelectionState).toHaveBeenCalledWith({ 'additional_level': null });
    });
  });

  it('should set boards and items when response is successful', () => {
    localStorage.setItem('completeHierarchyData', JSON.stringify(mockHierarchyData));
    component.selectedType = 'scrum';
    const response = {
      success: true,
      data: {
        userBoardConfigDTO: {
          scrum: [
            {
              boardName: 'Board 1',
              boardSlug: 'board-1',
              filters: {
                primaryFilter: { defaultLevel: { labelName: 'project' } },
                parentFilter: { labelName: 'port' },
              },
            },
          ],
          others: [
            {
              boardName: 'Board 2',
              boardSlug: 'board-2',
              filters: {
                primaryFilter: { defaultLevel: { labelName: 'project' } },
                parentFilter: { labelName: 'port' },
              },
            },
          ],
          configDetails: {},
        },
      },
    };
    // let getAllHierarchyLevelsSpy = spyOn(component.httpService, 'getAllHierarchyLevels').and.returnValue(of({ data: [] }));

    component.setBoards(response);

    expect(component.dashConfigData).toEqual({
      scrum: [
        {
          boardName: 'Board 1',
          boardSlug: 'board-1',
          filters: {
            primaryFilter: { defaultLevel: { labelName: 'Project' } },
            parentFilter: { labelName: 'Engagement' },
          },
        },
      ],
      others: [
        {
          boardName: 'Board 2',
          boardSlug: 'board-2',
          filters: {
            primaryFilter: { defaultLevel: { labelName: 'Project' } },
            parentFilter: { labelName: 'Engagement' },
          },
        },
      ],
      configDetails: undefined
    });
    expect(component.items).toEqual([
      {
        label: 'Board 1',
        slug: 'board-1',
        command: jasmine.any(Function),
      },
      {
        label: 'Board 2',
        slug: 'board-2',
        command: jasmine.any(Function),
      },
    ]);
  });

  xit('should call getAllHierarchyLevels when completeHierarchyData is not available', () => {
    // localStorage.setItem('completeHierarchyData', JSON.stringify(mockHierarchyData));
    const response = { success: true, data: { userBoardConfigDTO: {}, configDetails: {} } };
    let getAllHierarchyLevelsSpy = spyOn(component.httpService, 'getAllHierarchyLevels').and.returnValue(of({ data: [] }));

    component.setBoards(response);

    expect(getAllHierarchyLevelsSpy).toHaveBeenCalled();
  });
});


