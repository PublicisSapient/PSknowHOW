import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NavNewComponent } from './nav-new.component';
import { HttpService } from '../../services/http.service';
import { SharedService } from '../../services/shared.service';
import { MessageService } from 'primeng/api';
import { Router, Routes } from '@angular/router';
import { of, Subject, throwError } from 'rxjs';
import { HelperService } from 'src/app/services/helper.service';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { APP_CONFIG, AppConfig } from '../../services/app.config';
import { RouterTestingModule } from '@angular/router/testing';
import { DashboardV2Component } from '../dashboard-v2/dashboard-v2.component';

const mockHierarchyData = {
  kanban: [
    {
      id: '6442815917ed167d8157f0f5',
      level: 1,
      hierarchyLevelId: 'bu',
      hierarchyLevelName: 'BU',
      hierarchyInfo: 'Business Unit',
    },
    {
      id: '6442815917ed167d8157f0f6',
      level: 2,
      hierarchyLevelId: 'ver',
      hierarchyLevelName: 'Vertical',
      hierarchyInfo: 'Industry',
    },
    {
      id: '6442815917ed167d8157f0f7',
      level: 3,
      hierarchyLevelId: 'acc',
      hierarchyLevelName: 'Account',
      hierarchyInfo: 'Account',
    },
    {
      id: '6442815917ed167d8157f0f8',
      level: 4,
      hierarchyLevelId: 'port',
      hierarchyLevelName: 'Engagement',
      hierarchyInfo: 'Engagement',
    },
    {
      level: 5,
      hierarchyLevelId: 'project',
      hierarchyLevelName: 'Project',
    },
    {
      level: 6,
      hierarchyLevelId: 'release',
      hierarchyLevelName: 'Release',
    },
    {
      level: 7,
      hierarchyLevelId: 'sqd',
      hierarchyLevelName: 'Squad',
    },
  ],
  scrum: [
    {
      id: '6442815917ed167d8157f0f5',
      level: 1,
      hierarchyLevelId: 'bu',
      hierarchyLevelName: 'BU',
      hierarchyInfo: 'Business Unit',
    },
    {
      id: '6442815917ed167d8157f0f6',
      level: 2,
      hierarchyLevelId: 'ver',
      hierarchyLevelName: 'Vertical',
      hierarchyInfo: 'Industry',
    },
    {
      id: '6442815917ed167d8157f0f7',
      level: 3,
      hierarchyLevelId: 'acc',
      hierarchyLevelName: 'Account',
      hierarchyInfo: 'Account',
    },
    {
      id: '6442815917ed167d8157f0f8',
      level: 4,
      hierarchyLevelId: 'port',
      hierarchyLevelName: 'Engagement',
      hierarchyInfo: 'Engagement',
    },
    {
      level: 5,
      hierarchyLevelId: 'project',
      hierarchyLevelName: 'Project',
    },
    {
      level: 6,
      hierarchyLevelId: 'sprint',
      hierarchyLevelName: 'Sprint',
    },
    {
      level: 6,
      hierarchyLevelId: 'release',
      hierarchyLevelName: 'Release',
    },
    {
      level: 7,
      hierarchyLevelId: 'sqd',
      hierarchyLevelName: 'Squad',
    },
  ],
};
describe('NavNewComponent', () => {
  let component: NavNewComponent;
  let fixture: ComponentFixture<NavNewComponent>;
  let httpService: jasmine.SpyObj<HttpService>;
  let sharedService: SharedService;
  let messageService: jasmine.SpyObj<MessageService>;
  let router: jasmine.SpyObj<Router>;
  let helperService: jasmine.SpyObj<HelperService>;

  // const originalLocation = window.location;

  // const mockWindowLocationHash = (hash: string) => {
  //   const location = {
  //     ...window.location,
  //     hash,
  //   };
  //   spyOnProperty(window, 'location', 'get').and.returnValue(location);
  // };

  beforeEach(async () => {
    // const httpSpy = jasmine.createSpyObj('HttpService', ['getShowHideOnDashboardNewUI', 'getAllHierarchyLevels']);

    const messageSpy = jasmine.createSpyObj('MessageService', ['add']);
    const routerSpy = jasmine.createSpyObj('Router', [
      'navigate',
      'navigateByUrl',
      'createUrlTree',
      'serializeUrl',
      'parseUrl',
      'isActive',
      'events',
      'routerState',
      'url',
      'urlHandlingStrategy',
      'config',
      'resetConfig',
      'ngOnDestroy',
      'dispose',
      'initialNavigation',
      'setUpLocationChangeListener',
      'getCurrentNavigation',
      'triggerEvent',
    ]);
    const helperSpy = jasmine.createSpyObj('HelperService', [
      'setBackupOfFilterSelectionState',
      'deepEqual',
    ]);
    const routes: Routes = [
      { path: 'dashboard', component: DashboardV2Component },
    ];
    await TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        RouterTestingModule.withRoutes(routes),
      ],
      declarations: [NavNewComponent],
      providers: [
        HttpService,
        SharedService,
        { provide: MessageService, useValue: messageSpy },
        { provide: APP_CONFIG, useValue: AppConfig },
        { provide: Router, useValue: routerSpy },
        { provide: HelperService, useValue: helperSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(NavNewComponent);
    component = fixture.componentInstance;
    httpService = TestBed.inject(HttpService) as jasmine.SpyObj<HttpService>;
    sharedService = TestBed.inject(
      SharedService,
    ) as jasmine.SpyObj<SharedService>;
    messageService = TestBed.inject(
      MessageService,
    ) as jasmine.SpyObj<MessageService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    helperService = TestBed.inject(
      HelperService,
    ) as jasmine.SpyObj<HelperService>;

    // Set default mocks
    // spyOn(sharedService, 'getSelectedType').and.returnValue('scrum');
    // spyOn(sharedService, 'getSelectedTrends').and.returnValue([]);
    // spyOn(httpService, 'getShowHideOnDashboardNewUI').and.returnValue(of({ data: 'mock data' }));
    component.selectedType = 'scrum';
    component.dashConfigData = {
      scrum: [
        {
          boardName: 'Board 1',
          boardSlug: 'board-1',
          filters: {
            primaryFilter: { defaultLevel: { labelName: 'Project' } },
            parentFilter: { labelName: 'Engagement' },
          },
          kpis: [
            { kpiId: 'kpi1', shown: true },
            { kpiId: 'kpi2', shown: true },
          ],
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
          kpis: [
            { kpiId: 'kpi3', shown: true },
            { kpiId: 'kpi4', shown: true },
          ],
        },
      ],
      configDetails: undefined,
    };
  });

  afterEach(() => {
    localStorage.clear();
    // spyOnProperty(window, 'location', 'get').and.returnValue(originalLocation);
    // sharedService.getSelectedType.calls.reset();
    // sharedService.getSelectedTrends.calls.reset();
    // sharedService.setScrumKanban.calls.reset();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should call setSelectedBoard if selectedTab is not "unauthorized access"', () => {
    const obj = { boardSlug: 'iteration' };
    spyOn(sharedService, 'setSelectedBoard');
    component.handleMenuTabFunctionality(obj);

    expect(sharedService.setSelectedBoard).toHaveBeenCalledWith('iteration');
    expect(router.navigate).toHaveBeenCalledWith(['/dashboard/iteration']);
  });

  it('should not call setSelectedBoard if selectedTab is "unauthorized access"', () => {
    const obj = { boardSlug: 'unauthorized access' };
    spyOn(sharedService, 'setSelectedBoard');
    component.handleMenuTabFunctionality(obj);

    expect(sharedService.setSelectedBoard).not.toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith([
      '/dashboard/unauthorized access',
    ]);
  });

  it('should call setBackupOfFilterSelectionState for specific tabs', () => {
    const obj = { boardSlug: 'iteration' };
    spyOn(sharedService, 'setBackupOfFilterSelectionState');
    component.handleMenuTabFunctionality(obj);

    expect(sharedService.setBackupOfFilterSelectionState).toHaveBeenCalledWith({
      additional_level: null,
    });
    expect(router.navigate).toHaveBeenCalledWith(['/dashboard/iteration']);
  });

  it('should call setBackupOfFilterSelectionState for release tab', () => {
    const obj = { boardSlug: 'release' };
    spyOn(sharedService, 'setBackupOfFilterSelectionState');
    component.handleMenuTabFunctionality(obj);

    expect(sharedService.setBackupOfFilterSelectionState).toHaveBeenCalledWith({
      additional_level: null,
    });
    expect(router.navigate).toHaveBeenCalledWith(['/dashboard/release']);
  });

  xit('should not call setBackupOfFilterSelectionState for other tabs', () => {
    const obj = { boardSlug: 'some-other-tab' };
    spyOn(sharedService, 'setBackupOfFilterSelectionState');
    component.handleMenuTabFunctionality(obj);

    expect(
      sharedService.setBackupOfFilterSelectionState,
    ).not.toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/dashboard/some-other-tab']);
  });

  describe('ngOnDestroy', () => {
    it('should unsubscribe all subscriptions', () => {
      const subscriptionMock = jasmine.createSpyObj('Subscription', [
        'unsubscribe',
      ]);
      component.subscriptions = [subscriptionMock, subscriptionMock];

      component.ngOnDestroy();

      expect(subscriptionMock.unsubscribe).toHaveBeenCalledTimes(2);
    });
  });

  describe('getBoardConfig', () => {
    it('should call httpService and handle success response', () => {
      const responseMock = { success: true, data: { userBoardConfigDTO: {} } };
      spyOn(component, 'setBoards');
      spyOn(httpService, 'getShowHideOnDashboardNewUI').and.returnValue(
        of(responseMock),
      );

      component.getBoardConfig(['proj1']);

      expect(httpService.getShowHideOnDashboardNewUI).toHaveBeenCalledWith({
        basicProjectConfigIds: ['proj1'],
      });
      expect(component.setBoards).toHaveBeenCalledWith(responseMock);
    });

    it('should handle error and call MessageService.add when getShowHideOnDashboardNewUI fails', () => {
      // Simulate an error in the HTTP call
      spyOn(httpService, 'getShowHideOnDashboardNewUI').and.returnValue(
        throwError({ message: 'Error' }),
      );

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
      localStorage.setItem(
        'completeHierarchyData',
        JSON.stringify({
          scrum: [
            { hierarchyLevelId: 'level1', hierarchyLevelName: 'Level 1' },
          ],
        }),
      );
      component.selectedType = 'scrum';
    });

    it('should set board data when localStorage has hierarchy data', () => {
      component.selectedType = 'scrum';
      const responseMock = {
        success: true,
        data: {
          userBoardConfigDTO: {
            scrum: [
              {
                boardName: 'Scrum Board',
                boardSlug: 'scrum-board',
                filters: {
                  primaryFilter: { defaultLevel: { labelName: 'level1' } },
                },
                kpis: [
                  { kpiId: 'kpi1', shown: true },
                  { kpiId: 'kpi2', shown: true },
                ],
              },
            ],
            others: [],
          },
        },
      };

      component.setBoards(responseMock);

      expect(component.items.length).toBe(1);
      expect(component.items[0].label).toBe('Scrum Board');
    });

    it('should call getAllHierarchyLevels when localStorage is empty', () => {
      localStorage.removeItem('completeHierarchyData');
      const responseMock = {
        success: true,
        data: { userBoardConfigDTO: { scrum: [], others: [] } },
      };
      let getAllHierarchyLevelsSpy = spyOn(
        httpService,
        'getAllHierarchyLevels',
      ).and.returnValue(of({ data: 'mockHierarchyData' }));

      component.setBoards(responseMock);

      expect(getAllHierarchyLevelsSpy).toHaveBeenCalled();
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
      spyOn(sharedService, 'setBackupOfFilterSelectionState');
      component.handleMenuTabFunctionality(mockObj);

      expect(
        component.sharedService.setBackupOfFilterSelectionState,
      ).toHaveBeenCalledWith({ additional_level: null });
    });
  });

  it('should set boards and items when response is successful', () => {
    localStorage.setItem(
      'completeHierarchyData',
      JSON.stringify(mockHierarchyData),
    );
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
              kpis: [
                { kpiId: 'kpi1', shown: true },
                { kpiId: 'kpi2', shown: true },
              ],
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
              kpis: [
                { kpiId: 'kpi3', shown: true },
                { kpiId: 'kpi4', shown: true },
              ],
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
          kpis: [
            { kpiId: 'kpi1', shown: true },
            { kpiId: 'kpi2', shown: true },
          ],
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
          kpis: [
            { kpiId: 'kpi3', shown: true },
            { kpiId: 'kpi4', shown: true },
          ],
        },
      ],
      configDetails: undefined,
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

  it('should call getAllHierarchyLevels when completeHierarchyData is not available', () => {
    // localStorage.setItem('completeHierarchyData', JSON.stringify(mockHierarchyData));
    const response = {
      success: true,
      data: { userBoardConfigDTO: {}, configDetails: {} },
    };
    let getAllHierarchyLevelsSpy = spyOn(
      httpService,
      'getAllHierarchyLevels',
    ).and.returnValue(of({ data: [] }));

    component.setBoards(response);

    expect(getAllHierarchyLevelsSpy).toHaveBeenCalled();
  });

  describe('NavNewComponent.ngOnInit() ngOnInit method', () => {
    describe('Happy paths', () => {
      it('should initialize selectedType and set it in sharedService', () => {
        spyOn(sharedService, 'getSelectedType').and.returnValue('kanban');
        spyOn(sharedService, 'setScrumKanban').and.callFake(() => {});

        component.ngOnInit();

        expect(component.selectedType).toBe('kanban');
        expect(sharedService.setScrumKanban).toHaveBeenCalledWith('kanban');
      });

      it('should call getBoardConfig with selected trends', () => {
        const trends = [{ basicProjectConfigId: '123' }];
        spyOn(sharedService, 'getSelectedTrends').and.returnValue(trends);
        spyOn<any>(component, 'getBoardConfig').and.callFake(() => {});

        component.ngOnInit();

        expect(component['getBoardConfig']).toHaveBeenCalledWith(['123']);
      });

      // it('should subscribe to onTabSwitch and update selectedTab', () => {
      //   const tabSwitchData = { selectedBoard: 'newTab' };
      //   spyOn(sharedService.onTabSwitch, 'subscribe').and.callFake((callback: ({ selectedBoard: string }) => void) => {
      //     callback(tabSwitchData);
      //     return { unsubscribe: jasmine.createSpy() };
      //   });

      //   component.ngOnInit();

      //   expect(component.selectedTab).toBe('newTab');
      // });
    });

    describe('Edge cases', () => {
      describe('Edge cases', () => {
        it('should handle empty selected trends gracefully', () => {
          spyOn(sharedService, 'getSelectedTrends').and.returnValue([]);
          spyOn<any>(component, 'getBoardConfig').and.callFake(() => {});

          component.ngOnInit();

          expect(component['getBoardConfig']).toHaveBeenCalledWith([]);
        });

        it('should handle null selectedType and default to scrum', () => {
          spyOn(sharedService, 'getSelectedType').and.returnValue(null);
          spyOn(sharedService, 'setScrumKanban').and.callFake(() => {});

          component.ngOnInit();

          expect(component.selectedType).toBe('scrum');
          expect(sharedService.setScrumKanban).toHaveBeenCalledWith('scrum');
        });

        // it('should handle errors in getBoardConfig gracefully', () => {
        //   spyOn<any>(component, 'getBoardConfig').and.callFake(() => {
        //     throw new Error('Test error');
        //   });
        //   messageService.add.and.callFake(() => { });

        //   expect(() => component.ngOnInit()).not.toThrow();
        //   expect(messageService.add).toHaveBeenCalledWith({
        //     severity: 'error',
        //     summary: 'Test error',
        //   });
        // });
      });
    });
  });
});
