import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CollapsiblePanelComponent } from './collapsible-panel.component';
import { SharedService } from 'src/app/services/shared.service';
import { Subject } from 'rxjs';
import { ActivatedRoute } from '@angular/router';

const RawData = [
  {
    name: 'Recommendations and Retro',
    projectId: '659eb5af75f4a73bf3032dc0',
    hierarchy: [
      {
        hierarchyLevel: {
          level: 1,
          hierarchyLevelId: 'bu',
          hierarchyLevelName: 'BU',
        },
        orgHierarchyNodeId: '96510d59-8a12-4443-96fb-153ba601789f',
        value: 'Internal',
      },
      {
        hierarchyLevel: {
          level: 2,
          hierarchyLevelId: 'ver',
          hierarchyLevelName: 'Vertical',
        },
        orgHierarchyNodeId: 'd4dd4910-0565-4fab-96b5-8165ba27a52d',
        value: 'PS Internal',
      },
      {
        hierarchyLevel: {
          level: 3,
          hierarchyLevelId: 'acc',
          hierarchyLevelName: 'Account',
        },
        orgHierarchyNodeId: '0787e635-2504-48f5-b7ee-eb2242ed86e5',
        value: 'Methods and Tools',
      },
      {
        hierarchyLevel: {
          level: 4,
          hierarchyLevelId: 'port',
          hierarchyLevelName: 'Engagement',
        },
        orgHierarchyNodeId: 'dee77757-9e2e-40f3-b68c-47554ab204ea',
        value: 'DTS',
      },
    ],
    sprintGoals: [
      {
        name: 'R&R|PI_19|ITR_6',
        sprintId: '54093_701d4c46-5d96-4713-bc5a-7c8fe2fd28e1',
        goal: "Deliver a production-ready release that enables users to create projects via UI, filter action item options, and generate sharable links that automatically add team members to a project. Additionally, address defects, integrate common FE library, and provide a Super Admin page (minimum test that the users don't see the super admin button on the dashboard)",
      },
      {
        name: 'R&R|PI_19|ITR_4',
        sprintId: '54091_701d4c46-5d96-4713-bc5a-7c8fe2fd28e1',
        goal: '1. Have user project creation feature on QA\n2. Have recommendations on Retro Prod',
      },
      {
        name: 'R&R|PI_19|ITR_5',
        sprintId: '54092_701d4c46-5d96-4713-bc5a-7c8fe2fd28e1',
        goal: '',
      },
      {
        name: 'R&R|PI_20|ITR_1',
        sprintId: '54146_701d4c46-5d96-4713-bc5a-7c8fe2fd28e1',
        goal: '',
      },
      {
        name: 'R&R|PI_20|ITR_2',
        sprintId: '54147_701d4c46-5d96-4713-bc5a-7c8fe2fd28e1',
        goal: 'Complete backend setup to save Jira project details from KH to Retro\n \nDocument the mechanism to implement integration tests for FE and BE.',
      },
      {
        name: 'R&R|PI_20|ITR_3',
        sprintId: '54148_701d4c46-5d96-4713-bc5a-7c8fe2fd28e1',
        goal: 'Finish backend implementation of Jira configuration feature',
      },
    ],
  },
  {
    name: 'MAP',
    projectId: '63a304a909378702f4eab1d0',
    hierarchy: [
      {
        hierarchyLevel: {
          level: 1,
          hierarchyLevelId: 'bu',
          hierarchyLevelName: 'BU',
        },
        orgHierarchyNodeId: '96510d59-8a12-4443-96fb-153ba601789f',
        value: 'Internal',
      },
      {
        hierarchyLevel: {
          level: 2,
          hierarchyLevelId: 'ver',
          hierarchyLevelName: 'Vertical',
        },
        orgHierarchyNodeId: 'd4dd4910-0565-4fab-96b5-8165ba27a52d',
        value: 'PS Internal',
      },
      {
        hierarchyLevel: {
          level: 3,
          hierarchyLevelId: 'acc',
          hierarchyLevelName: 'Account',
        },
        orgHierarchyNodeId: '0787e635-2504-48f5-b7ee-eb2242ed86e5',
        value: 'Methods and Tools',
      },
      {
        hierarchyLevel: {
          level: 4,
          hierarchyLevelId: 'port',
          hierarchyLevelName: 'Engagement',
        },
        orgHierarchyNodeId: 'dee77757-9e2e-40f3-b68c-47554ab204ea',
        value: 'DTS',
      },
    ],
    sprintGoals: [
      {
        name: 'MAP|PI_19|ITR_5',
        sprintId: '54086_612d0a26-ba00-4e6f-925c-1b331c49ab7a',
        goal: '',
      },
      {
        name: 'MAP|PI_20|ITR_1',
        sprintId: '54140_612d0a26-ba00-4e6f-925c-1b331c49ab7a',
        goal: '',
      },
      {
        name: 'MAP|PI_20|ITR_3',
        sprintId: '54142_612d0a26-ba00-4e6f-925c-1b331c49ab7a',
        goal: '',
      },
      {
        name: 'MAP|PI_20|ITR_2',
        sprintId: '54141_612d0a26-ba00-4e6f-925c-1b331c49ab7a',
        goal: '',
      },
      {
        name: 'MAP|PI_19|ITR_4',
        sprintId: '54085_612d0a26-ba00-4e6f-925c-1b331c49ab7a',
        goal: '',
      },
      {
        name: 'MAP|PI_19|ITR_6',
        sprintId: '54087_612d0a26-ba00-4e6f-925c-1b331c49ab7a',
        goal: '',
      },
    ],
  },
  {
    name: 'DOJO Tools',
    projectId: '670e60adbdf857762e6e0762',
    hierarchy: [
      {
        hierarchyLevel: {
          level: 1,
          hierarchyLevelId: 'bu',
          hierarchyLevelName: 'BU',
        },
        orgHierarchyNodeId: '96510d59-8a12-4443-96fb-153ba601789f',
        value: 'Internal',
      },
      {
        hierarchyLevel: {
          level: 2,
          hierarchyLevelId: 'ver',
          hierarchyLevelName: 'Vertical',
        },
        orgHierarchyNodeId: 'd4dd4910-0565-4fab-96b5-8165ba27a52d',
        value: 'PS Internal',
      },
      {
        hierarchyLevel: {
          level: 3,
          hierarchyLevelId: 'acc',
          hierarchyLevelName: 'Account',
        },
        orgHierarchyNodeId: '0787e635-2504-48f5-b7ee-eb2242ed86e5',
        value: 'Methods and Tools',
      },
      {
        hierarchyLevel: {
          level: 4,
          hierarchyLevelId: 'port',
          hierarchyLevelName: 'Engagement',
        },
        orgHierarchyNodeId: 'dee77757-9e2e-40f3-b68c-47554ab204ea',
        value: 'DTS',
      },
    ],
    sprintGoals: [
      {
        name: 'Platform Team|PI_20|ITR3|29JAN',
        sprintId: '54134_90b0358b-ece8-4858-a11f-0881b7279f4c',
        goal: '',
      },
      {
        name: 'Platform Team|PI_19|ITR4|06NOV',
        sprintId: '54079_90b0358b-ece8-4858-a11f-0881b7279f4c',
        goal: '',
      },
      {
        name: 'Platform Team|PI_20|ITR1|01JAN',
        sprintId: '54132_90b0358b-ece8-4858-a11f-0881b7279f4c',
        goal: '',
      },
      {
        name: 'Platform Team|PI_20|ITR2|15JAN',
        sprintId: '54133_90b0358b-ece8-4858-a11f-0881b7279f4c',
        goal: '',
      },
      {
        name: 'Platform Team|PI_19|ITR6|04DEC',
        sprintId: '54081_90b0358b-ece8-4858-a11f-0881b7279f4c',
        goal: '',
      },
      {
        name: 'Platform Team|PI_19|ITR5|20NOV',
        sprintId: '54080_90b0358b-ece8-4858-a11f-0881b7279f4c',
        goal: '',
      },
    ],
  },
  {
    name: 'Vox',
    projectId: '670f8fe1bdf857762e6e08ed',
    hierarchy: [
      {
        hierarchyLevel: {
          level: 1,
          hierarchyLevelId: 'bu',
          hierarchyLevelName: 'BU',
        },
        orgHierarchyNodeId: '96510d59-8a12-4443-96fb-153ba601789f',
        value: 'Internal',
      },
      {
        hierarchyLevel: {
          level: 2,
          hierarchyLevelId: 'ver',
          hierarchyLevelName: 'Vertical',
        },
        orgHierarchyNodeId: 'd4dd4910-0565-4fab-96b5-8165ba27a52d',
        value: 'PS Internal',
      },
      {
        hierarchyLevel: {
          level: 3,
          hierarchyLevelId: 'acc',
          hierarchyLevelName: 'Account',
        },
        orgHierarchyNodeId: '0787e635-2504-48f5-b7ee-eb2242ed86e5',
        value: 'Methods and Tools',
      },
      {
        hierarchyLevel: {
          level: 4,
          hierarchyLevelId: 'port',
          hierarchyLevelName: 'Engagement',
        },
        orgHierarchyNodeId: 'dee77757-9e2e-40f3-b68c-47554ab204ea',
        value: 'DTS',
      },
    ],
    sprintGoals: [
      {
        name: 'VOX |PI_19|ITR_5|20_Nov',
        sprintId: '54110_13510c2d-7c97-4c55-b207-9b5470f9151c',
        goal: '',
      },
      {
        name: 'VOX |PI_20|ITR_2|15_Jan',
        sprintId: '54160_13510c2d-7c97-4c55-b207-9b5470f9151c',
        goal: '',
      },
      {
        name: 'VOX |PI_19|ITR_6|04_Dec',
        sprintId: '54111_13510c2d-7c97-4c55-b207-9b5470f9151c',
        goal: '',
      },
      {
        name: 'VOX |PI_19|ITR_4|06_Nov',
        sprintId: '54109_13510c2d-7c97-4c55-b207-9b5470f9151c',
        goal: '',
      },
      {
        name: 'VOX |PI_20|ITR_3|29_Jan',
        sprintId: '54161_13510c2d-7c97-4c55-b207-9b5470f9151c',
        goal: '',
      },
      {
        name: 'VOX |PI_20|ITR_1|01_Jan',
        sprintId: '54159_13510c2d-7c97-4c55-b207-9b5470f9151c',
        goal: '',
      },
    ],
  },
  {
    name: 'PSknowHOW',
    projectId: '65118da7965fbb0d14bce23c',
    hierarchy: [
      {
        hierarchyLevel: {
          level: 1,
          hierarchyLevelId: 'bu',
          hierarchyLevelName: 'BU',
        },
        orgHierarchyNodeId: '96510d59-8a12-4443-96fb-153ba601789f',
        value: 'Internal',
      },
      {
        hierarchyLevel: {
          level: 2,
          hierarchyLevelId: 'ver',
          hierarchyLevelName: 'Vertical',
        },
        orgHierarchyNodeId: 'd4dd4910-0565-4fab-96b5-8165ba27a52d',
        value: 'PS Internal',
      },
      {
        hierarchyLevel: {
          level: 3,
          hierarchyLevelId: 'acc',
          hierarchyLevelName: 'Account',
        },
        orgHierarchyNodeId: '0787e635-2504-48f5-b7ee-eb2242ed86e5',
        value: 'Methods and Tools',
      },
      {
        hierarchyLevel: {
          level: 4,
          hierarchyLevelId: 'port',
          hierarchyLevelName: 'Engagement',
        },
        orgHierarchyNodeId: 'dee77757-9e2e-40f3-b68c-47554ab204ea',
        value: 'DTS',
      },
    ],
    sprintGoals: [
      {
        name: 'KnowHOW | PI_20| ITR_3',
        sprintId: '54128_21c03157-8e1c-43e2-93c0-8842d93e977b',
        goal: 'Release 12.1.0 \nCache Epic \nCentral hierarchy \nShared Link (Shortening )\nAccessibility',
      },
      {
        name: 'KnowHOW | PI_20| ITR_2',
        sprintId: '54127_21c03157-8e1c-43e2-93c0-8842d93e977b',
        goal: 'Release 12.1.0 \nCache Implementation\nCentral Hierarchy\nClient Rollout',
      },
      {
        name: 'KnowHOW | PI_20| ITR_1',
        sprintId: '54126_21c03157-8e1c-43e2-93c0-8842d93e977b',
        goal: '1.Deeplinking \n2.Iteration \n3.Cache',
      },
      {
        name: 'KnowHOW | PI_19| ITR_5',
        sprintId: '54057_21c03157-8e1c-43e2-93c0-8842d93e977b',
        goal: 'Release 11.1.0',
      },
      {
        name: 'KnowHOW | PI_19| ITR_4',
        sprintId: '54056_21c03157-8e1c-43e2-93c0-8842d93e977b',
        goal: 'ITR4 - Sprint Goal\n1.Azure Board snapshots\n2.Hierarchy refactoring\n3.Explore - additional fields\n4.Retire Old UI from FE\n5.Fixathon changes',
      },
      {
        name: 'KnowHOW | PI_19| ITR_6',
        sprintId: '54058_21c03157-8e1c-43e2-93c0-8842d93e977b',
        goal: 'Release 12.0.0 \nDeep linking \nKPI Filter Retention \nUI Defects\nNew Component Iteration dashboard',
      },
    ],
  },
  {
    name: 'DOJO Support',
    projectId: '670f64c6bdf857762e6e085b',
    hierarchy: [
      {
        hierarchyLevel: {
          level: 1,
          hierarchyLevelId: 'bu',
          hierarchyLevelName: 'BU',
        },
        orgHierarchyNodeId: '96510d59-8a12-4443-96fb-153ba601789f',
        value: 'Internal',
      },
      {
        hierarchyLevel: {
          level: 2,
          hierarchyLevelId: 'ver',
          hierarchyLevelName: 'Vertical',
        },
        orgHierarchyNodeId: 'd4dd4910-0565-4fab-96b5-8165ba27a52d',
        value: 'PS Internal',
      },
      {
        hierarchyLevel: {
          level: 3,
          hierarchyLevelId: 'acc',
          hierarchyLevelName: 'Account',
        },
        orgHierarchyNodeId: '0787e635-2504-48f5-b7ee-eb2242ed86e5',
        value: 'Methods and Tools',
      },
      {
        hierarchyLevel: {
          level: 4,
          hierarchyLevelId: 'port',
          hierarchyLevelName: 'Engagement',
        },
        orgHierarchyNodeId: 'dee77757-9e2e-40f3-b68c-47554ab204ea',
        value: 'DTS',
      },
    ],
    sprintGoals: [
      {
        name: 'Platform Team|PI_19|ITR6|04DEC',
        sprintId: '54081_210f4ae3-73dd-4134-9d65-cac5f3add4fe',
        goal: '',
      },
      {
        name: 'Support|PI_19 |ITR_6| 04 Dec',
        sprintId: '54099_210f4ae3-73dd-4134-9d65-cac5f3add4fe',
        goal: '',
      },
      {
        name: 'Support|PI_20 |ITR_1| 01 Jan',
        sprintId: '54166_210f4ae3-73dd-4134-9d65-cac5f3add4fe',
        goal: '',
      },
      {
        name: 'Support|PI_20 |ITR_3| 29 Jan',
        sprintId: '54168_210f4ae3-73dd-4134-9d65-cac5f3add4fe',
        goal: '',
      },
      {
        name: 'Support|PI_19 |ITR_5| 20 Nov',
        sprintId: '54098_210f4ae3-73dd-4134-9d65-cac5f3add4fe',
        goal: '',
      },
      {
        name: 'Support|PI_20 |ITR_2| 15 Jan',
        sprintId: '54167_210f4ae3-73dd-4134-9d65-cac5f3add4fe',
        goal: '',
      },
    ],
  },
  {
    name: 'PSHOW-DA',
    projectId: '6718a2b1bdf857762e6e0bea',
    hierarchy: [
      {
        hierarchyLevel: {
          level: 1,
          hierarchyLevelId: 'bu',
          hierarchyLevelName: 'BU',
        },
        orgHierarchyNodeId: '96510d59-8a12-4443-96fb-153ba601789f',
        value: 'Internal',
      },
      {
        hierarchyLevel: {
          level: 2,
          hierarchyLevelId: 'ver',
          hierarchyLevelName: 'Vertical',
        },
        orgHierarchyNodeId: 'd4dd4910-0565-4fab-96b5-8165ba27a52d',
        value: 'PS Internal',
      },
      {
        hierarchyLevel: {
          level: 3,
          hierarchyLevelId: 'acc',
          hierarchyLevelName: 'Account',
        },
        orgHierarchyNodeId: '0787e635-2504-48f5-b7ee-eb2242ed86e5',
        value: 'Methods and Tools',
      },
      {
        hierarchyLevel: {
          level: 4,
          hierarchyLevelId: 'port',
          hierarchyLevelName: 'Engagement',
        },
        orgHierarchyNodeId: 'dee77757-9e2e-40f3-b68c-47554ab204ea',
        value: 'DTS',
      },
    ],
    sprintGoals: [
      {
        name: 'PS HOW |PI_19|ITR_6|04_Dec',
        sprintId: '54105_a234e825-6cc8-4bc7-887f-e3d448a8cf23',
        goal: '',
      },
      {
        name: 'PS HOW |PI_20|ITR_2|15_Jan',
        sprintId: '54154_a234e825-6cc8-4bc7-887f-e3d448a8cf23',
        goal: '',
      },
      {
        name: 'PS HOW |PI_19|ITR_4|06_Nov',
        sprintId: '54103_a234e825-6cc8-4bc7-887f-e3d448a8cf23',
        goal: '',
      },
      {
        name: 'PS HOW |PI_19|ITR_5|20_Nov',
        sprintId: '54104_a234e825-6cc8-4bc7-887f-e3d448a8cf23',
        goal: '',
      },
      {
        name: 'PS HOW |PI_20|ITR_1|1_Jan',
        sprintId: '54153_a234e825-6cc8-4bc7-887f-e3d448a8cf23',
        goal: '',
      },
      {
        name: 'PS HOW |PI_20|ITR_3|29_Jan',
        sprintId: '54155_a234e825-6cc8-4bc7-887f-e3d448a8cf23',
        goal: '',
      },
    ],
  },
];

describe('CollapsiblePanelComponent', () => {
  let component: CollapsiblePanelComponent;
  let fixture: ComponentFixture<CollapsiblePanelComponent>;
  let sharedService: SharedService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CollapsiblePanelComponent],
      providers: [
        SharedService,
        {
          provide: ActivatedRoute,
          useValue: { params: new Subject(), queryParams: new Subject() },
        },
      ], // ✅ Provide real service
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CollapsiblePanelComponent);
    component = fixture.componentInstance;
    sharedService = TestBed.inject(SharedService); // ✅ Use real service

    // ✅ Initialize observable properties (simulate real observables)
    sharedService.onScrumKanbanSwitch = new Subject();
    sharedService.onTabSwitch = new Subject();

    // ✅ Set up real filter data
    sharedService.getDataForSprintGoal = () => ({
      filterDataArr: {
        Level1: [
          { nodeId: '1', parentId: null },
          { nodeId: '2', parentId: '1' },
        ],
        Level2: [{ nodeId: '3', parentId: '2' }],
      },
      selectedLevel: { nodeName: 'Level1', nodeDisplayName: 'Level1' },
      selectedFilters: [{ nodeId: '1' }],
      filterLevels: [{ nodeName: 'Level1' }, { nodeName: 'Level2' }],
    });

    // ✅ Set localStorage (simulate real hierarchy data)
    localStorage.setItem(
      'completeHierarchyData',
      JSON.stringify({ scrum: [{ hierarchyLevelName: 'Level1', level: 1 }] }),
    );

    component.rawData = RawData;
    component.accordionData = RawData;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  describe('onSelectionChange', () => {
    it('should update accordionData based on selected hierarchy nodeIds', () => {
      component.rawData = [{ hierarchy: [{ orgHierarchyNodeId: '1' }] }];
      const event = { value: [{ nodeId: '1' }] };

      component.onSelectionChange(event);

      expect(component.accordionData.length).toBe(1);
      expect(component.accordionData).toEqual([component.rawData[0]]);
    });

    it('should update accordionData with no results if no match is found', () => {
      component.rawData = [{ hierarchy: [{ orgHierarchyNodeId: '2' }] }];
      const event = { value: [{ nodeId: '999' }] };

      component.onSelectionChange(event);

      expect(component.accordionData.length).toBe(0);
    });
  });

  describe('filterByHierarchyNodeId', () => {
    it('should filter data by hierarchy nodeIds', () => {
      component.rawData = [
        { hierarchy: [{ orgHierarchyNodeId: '1' }] },
        { hierarchy: [{ orgHierarchyNodeId: '2' }] },
      ];
      const nodeIds = ['1'];

      const filteredResults = component.filterByHierarchyNodeId(
        component.rawData,
        nodeIds,
      );

      expect(filteredResults.length).toBe(1);
      expect(filteredResults).toEqual([component.rawData[0]]);
    });

    it('should return an empty array if no matches found', () => {
      component.rawData = [{ hierarchy: [{ orgHierarchyNodeId: '1' }] }];
      const nodeIds = ['999'];

      const filteredResults = component.filterByHierarchyNodeId(
        component.rawData,
        nodeIds,
      );

      expect(filteredResults.length).toBe(0);
    });
  });

  describe('toggleAll', () => {
    it('should expand all when isAllExpanded is false', () => {
      component.rawData = [{}, {}, {}];

      component.toggleAll();

      expect(component.activeIndices.length).toBe(3);
      expect(component.isAllExpanded).toBe(true);
    });

    it('should collapse all when isAllExpanded is true', () => {
      component.isAllExpanded = true;

      component.toggleAll();

      expect(component.activeIndices.length).toBe(0);
      expect(component.isAllExpanded).toBe(false);
    });
  });

  describe('setUpPanel', () => {
    it('should correctly set filter data from sharedService', () => {
      component.setUpPanel();

      expect(component.filterRawData).toBeDefined();
      expect(component.filterDataArr).toEqual(
        sharedService.getDataForSprintGoal().filterDataArr,
      );
      // expect(component.selectedLevel.nodeName).toEqual("Level1");
    });
  });

  describe('buildFilterDropdown', () => {
    it('should return undefined if selected level is not found', () => {
      component.filterRawData = {
        ...sharedService.getDataForSprintGoal(),
        selectedLevel: { nodeName: 'NonExistingLevel' },
      };
      const filterMap = component.buildFilterDropdown();

      expect(filterMap).toBeUndefined();
    });
  });

  describe('SharedService Events', () => {
    it('should update sprint goal flag on onScrumKanbanSwitch event', () => {
      spyOn(sharedService, 'updateSprintGoalFlag');
      sharedService.onScrumKanbanSwitch.next({ selectedType: 'scrum' }); // ✅ Trigger event
      expect(sharedService.updateSprintGoalFlag).toHaveBeenCalledWith(false);
    });

    it('should update sprint goal flag on onTabSwitch event', () => {
      spyOn(sharedService, 'updateSprintGoalFlag');
      sharedService.onTabSwitch.next({ selectedBoard: 'Speed' }); // ✅ Trigger event
      expect(sharedService.updateSprintGoalFlag).toHaveBeenCalledWith(false);
    });
  });
});
