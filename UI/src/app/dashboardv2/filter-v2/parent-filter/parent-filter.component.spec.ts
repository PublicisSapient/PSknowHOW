import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ParentFilterComponent } from './parent-filter.component';

import { RouterTestingModule } from '@angular/router/testing';
import { SharedService } from '../../../services/shared.service';
import { HelperService } from 'src/app/services/helper.service';
import { GetAuthService } from '../../../services/getauth.service';
import { HttpClientModule } from '@angular/common/http';
import { APP_CONFIG, AppConfig } from '../../../services/app.config';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpService } from '../../../services/http.service';
import { CommonModule, DatePipe } from '@angular/common';

describe('ParentFilterComponent', () => {
  let component: ParentFilterComponent;
  let fixture: ComponentFixture<ParentFilterComponent>;
  let getAuth: GetAuthService;
  let httpService: HttpService
  let sharedService: SharedService;
  let helperService: HelperService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ParentFilterComponent],
      imports: [RouterTestingModule, HttpClientModule, BrowserAnimationsModule],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],

      providers: [SharedService, GetAuthService, HttpService, HelperService, CommonModule, DatePipe,
        { provide: APP_CONFIG, useValue: AppConfig }
      ]
    })
      .compileComponents();

    fixture = TestBed.createComponent(ParentFilterComponent);
    component = fixture.componentInstance;
    getAuth = TestBed.get(GetAuthService);
    httpService = TestBed.inject(HttpService);
    sharedService = TestBed.inject(SharedService);
    helperService = TestBed.inject(HelperService);

    component.parentFilterConfig = { labelName: "Organization Level" };
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });


  it('should update filterLevels and selectedLevel when selectedTab or selectedType changes and parentFilterConfig labelName is Organization Level', () => {
    component.selectedTab = 'developer';
    component.selectedType = 'scrum';
    component.filterData = {
      Level1: [{ nodeId: 1, nodeName: 'Node 1' }],
      Level2: [{ nodeId: 2, nodeName: 'Node 2' }]
    };
    component.additionalFilterLevels = ['Level2'];
    component.parentFilterConfig = { labelName: 'Organization Level' };
    spyOn(helperService, 'getBackupOfFilterSelectionState').and.returnValue('Level1');
    spyOn(helperService, 'setBackupOfFilterSelectionState');
    spyOn(component.onSelectedLevelChange, 'emit');

    component.ngOnChanges({
      selectedTab: {
        currentValue: 'developer', previousValue: null,
        firstChange: false,
        isFirstChange: function (): boolean {
          return false;
        }
      },
      selectedType: {
        currentValue: 'scrum', previousValue: null,
        firstChange: false,
        isFirstChange: function (): boolean {
          return false;
        }
      },
      parentFilterConfig: {
        currentValue: { labelName: 'Organization Level' }, previousValue: null,
        firstChange: false,
        isFirstChange: function (): boolean {
          return false;
        }
      }
    });

    expect(component.filterLevels).toEqual(['Level1']);
    // expect(component.selectedLevel).toEqual('LEVEL1');
    expect(helperService.getBackupOfFilterSelectionState).toHaveBeenCalledWith('parent_level');
    // expect(helperService.setBackupOfFilterSelectionState).toHaveBeenCalledWith({ 'parent_level': 'LEVEL1' });
    // expect(component.onSelectedLevelChange.emit).toHaveBeenCalledWith('level1');
  });

  it('should update filterLevels and selectedLevel when selectedTab or selectedType changes and parentFilterConfig labelName is not Organization Level', () => {
    component.selectedTab = 'iteration';
    component.selectedType = 'Type';
    component.filterData = {
      level1: [{ nodeId: 1, nodeName: 'Node 1' }, { nodeId: 4, nodeName: 'Node 4' }],
      level2: [{ nodeId: 2, nodeName: 'Node 2' }],
      level3: [{ nodeId: 3, nodeName: 'Node 3' }],
    };
    component.parentFilterConfig = { labelName: 'level1' };
    component.selectedLevel = null;
    component.stateFilters = null;
    spyOn(helperService, 'getBackupOfFilterSelectionState').and.returnValue('project');
    spyOn(helperService, 'setBackupOfFilterSelectionState');
    spyOn(component.onSelectedLevelChange, 'emit');

    component.ngOnChanges({
      selectedTab: {
        currentValue: 'iteration', previousValue: 'speed',
        firstChange: false,
        isFirstChange: function (): boolean {
          return false;
        }
      },
      selectedType: {
        currentValue: 'scrum', previousValue: 'kanban',
        firstChange: false,
        isFirstChange: function (): boolean {
          return false;
        }
      },
      parentFilterConfig: {
        currentValue: { labelName: 'Level1' }, previousValue: {
          labelName: 'Organization Level'
        },
        firstChange: false,
        isFirstChange: function (): boolean {
          return false;
        }
      }
    });

    expect(component.filterLevels).toEqual(['Node 1', 'Node 4']);
    expect(helperService.getBackupOfFilterSelectionState).toHaveBeenCalledWith('parent_level');
  });

  xit('should emit selectedLevel when parentFilterConfig labelName is Organization Level', () => {
    component.parentFilterConfig = { labelName: 'Organization Level' };
    spyOn(component.onSelectedLevelChange, 'emit');
    spyOn(helperService, 'setBackupOfFilterSelectionState');

    component.handleSelectedLevelChange();

    expect(component.onSelectedLevelChange.emit).toHaveBeenCalledWith(component.selectedLevel.toLowerCase());
    expect(helperService.setBackupOfFilterSelectionState).toHaveBeenCalledWith({ 'parent_level': component.selectedLevel.toLowerCase(), 'primary_level': null });
  });

  it('should emit selectedNode when parentFilterConfig labelName is not Organization Level', () => {
    component.parentFilterConfig = { labelName: 'Level1' };
    component.filterData = { Level1: [{ nodeId: 1, nodeName: 'Node 1' }] };
    component.selectedLevel = 'Node 1';
    spyOn(component.onSelectedLevelChange, 'emit');
    spyOn(helperService, 'setBackupOfFilterSelectionState');

    component.handleSelectedLevelChange();
  });


  it('should fill additionalFilterLevels with keys that have a higher level than project', () => {
    component.filterData = {
      Project: [{ level: 2 }],
      department: [{ level: 3 }],
      team: [{ level: 4 }],
    };
    component.fillAdditionalFilterLevels();

    expect(component.additionalFilterLevels).toEqual(['department', 'team']);
  });

  it('should not fill additionalFilterLevels if project has no length', () => {
    component.filterData = {
      project: [{ level: 2 }],
      department: [{ level: 3 }],
      team: [{ level: 4 }],
    };

    component.filterData['project'] = [];
    component.fillAdditionalFilterLevels();

    expect(component.additionalFilterLevels).toEqual([]);
  });

  it('should not fill additionalFilterLevels if no keys have a higher level than project', () => {
    component.filterData = {
      project: [{ level: 2 }],
      department: [{ level: 1 }],
      team: [{ level: 1 }],
    };
    component.fillAdditionalFilterLevels();

    expect(component.additionalFilterLevels).toEqual([]);
  });
});