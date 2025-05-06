import { Component, OnInit, ChangeDetectorRef, OnDestroy, ViewChild } from '@angular/core';
import { MessageService } from 'primeng/api';
import { HttpService } from 'src/app/services/http.service';
import { SharedService } from 'src/app/services/shared.service';
import { HelperService } from 'src/app/services/helper.service';
import { switchMap, takeUntil } from 'rxjs/operators';
import { Subject, interval } from 'rxjs';
import { GoogleAnalyticsService } from 'src/app/services/google-analytics.service';
import { MultiSelect } from 'primeng/multiselect';
import { FeatureFlagsService } from 'src/app/services/feature-toggle.service';

@Component({
  selector: 'app-filter-new',
  templateUrl: './filter-new.component.html',
  styleUrls: ['./filter-new.component.css']
})
export class FilterNewComponent implements OnInit, OnDestroy {
  filterDataArr = {};
  masterData = {};
  filterApplyData = {};
  selectedTab: string = '';
  previousSelectedTab: string = '';
  selectedType: string = '';
  previousSelectedType: string = '';
  subscriptions: any[] = [];
  selectedFilterData: {};
  previousSelectedFilterData: {};
  selectedLevel: any = 'Project';
  kanban: boolean = false;
  boardData: object = {};
  kanbanRequired: any = {};
  parentFilterConfig: any = {};
  primaryFilterConfig: any = {};
  additionalFilterConfig: any = {};
  selectedNodeIdArr: any = {
    "basicProjectConfigIds": []
  };
  colorObj: any = {};
  previousFilterEvent: any = [];
  selectedDateFilter = '';
  selectedDayType;
  dateRangeFilter: any;
  selectedDateValue: string;
  toggleDateDropdown = false;
  additionalFiltersArr = [];
  additionalFilterLevelArr = [];
  filterType: string = '';
  selectedSprint: any;
  lastSyncData = {};
  additionalData: boolean = false;
  daysRemaining: any;
  combinedDate: string;
  displayModal: boolean = false;
  selectedProjectLastSyncDate: any;
  selectedProjectLastSyncDetails: any;
  selectedProjectLastSyncStatus: any;
  subject = new Subject();
  dashConfigData: any;
  filterApiData: any = []
  @ViewChild('showHideDdn') showHideDdn: MultiSelect;
  enableShowHideApply: boolean = true;
  showHideSelectAll: boolean = false;
  showChart: string = 'chart';
  iterationConfigData = {};
  isRecommendationsEnabled: boolean = false;
  selectedBoard: any;
  hierarchies: any;
  noSprint: boolean = false;
  projectList = null;
  blockUI: boolean = false;

  kanbanProjectsAvailable: boolean = true;
  scrumProjectsAvailable: boolean = true;
  squadLevel: any;

  constructor(
    private httpService: HttpService,
    public service: SharedService,
    private helperService: HelperService,
    public cdr: ChangeDetectorRef,
    private messageService: MessageService,
    private ga: GoogleAnalyticsService,
    private featureFlagsService: FeatureFlagsService) { }


  async ngOnInit() {
    this.selectedTab = this.service.getSelectedTab() || 'iteration';
    this.selectedType = this.helperService.getBackupOfFilterSelectionState('selected_type') ? this.helperService.getBackupOfFilterSelectionState('selected_type') : 'scrum';
    this.kanban = this.selectedType.toLowerCase() === 'kanban' ? true : false;

    this.dateRangeFilter = {
      "types": [
        "Days",
        "Weeks"
      ],
      "counts": [
        5,
        6,
        7,
        8,
        9,
        10,
        11,
        12,
        13,
        14,
        15
      ]
    };
    this.selectedDateValue = this.dateRangeFilter?.counts?.[0];
    this.selectedDateFilter = `${this.selectedDateValue} ${this.selectedDayType}`;

    this.isRecommendationsEnabled = await this.featureFlagsService.isFeatureEnabled('RECOMMENDATIONS');
    this.service.setRecommendationsFlag(this.isRecommendationsEnabled);

    this.subscriptions.push(
      this.service.onScrumKanbanSwitch
        .subscribe(data => {
          setTimeout(() => {
            this.selectedType = JSON.parse(JSON.stringify(data.selectedType));
            this.setDateFilter();
            this.filterDataArr = {};
            this.setHierarchyLevels();
          }, 0);
        }),

      this.service.onTabSwitch
        .subscribe(data => {
          setTimeout(() => {
            this.selectedTab = JSON.parse(JSON.stringify(data.selectedBoard));
            if (['iteration', 'backlog', 'release', 'dora', 'developer', 'kpi-maturity'].includes(this.selectedTab.toLowerCase())) {
              this.showChart = 'chart';
              this.service.setShowTableView(this.showChart);
            }
            this.service.setSelectedDateFilter(this.selectedDayType);

            // To DO
            if (Object.keys(this.boardData)?.length) {
              this.processBoardData(this.boardData);
            } else {
              this.setHierarchyLevels();
            }
          }, 0);


        }),

      this.service.iterationCongifData.subscribe((iterationDetails) => {
        this.iterationConfigData = iterationDetails;
      })
    );

    this.subscriptions.push(this.service.dateFilterSelectedDateType.subscribe(date => {
      this.selectedDayType = date;
    }));

    this.firstLoadFilterCheck(true);
    this.firstLoadFilterCheck(false);

    this.service.setScrumKanban(this.selectedType);
    this.service.setSelectedBoard(this.selectedTab);
  }

  setDateFilter() {
    if (this.selectedType.toLowerCase() === 'kanban') {
      this.kanban = true;
      if (!this.dateRangeFilter.types.includes('Months')) {
        this.dateRangeFilter.types.push('Months');
      }
      if (this.selectedTab === 'developer') {
        this.dateRangeFilter.types = this.dateRangeFilter.types.filter((type) => type !== 'Months');
      }
    } else {
      this.kanban = false;
      this.dateRangeFilter.types = this.dateRangeFilter.types.filter((type) => type !== 'Months');
    }
  }

  /**create dynamic hierarchy levels for filter dropdown */
  setHierarchyLevels() {
    if (!this.hierarchies) {
      this.httpService.getAllHierarchyLevels().subscribe((res) => {
        if (res.data) {
          this.hierarchies = res.data;
          localStorage.setItem('completeHierarchyData', JSON.stringify(this.hierarchies));
          this.setAdditionalHierarchyLevels();
          this.getFiltersData();
        }
      });
    } else {
      this.setAdditionalHierarchyLevels();
      this.getFiltersData();
    }
  }

  setAdditionalHierarchyLevels() {
    this.additionalFilterLevelArr = [];
    const projectLevel = this.hierarchies[this.selectedType]?.filter((x) => x.hierarchyLevelId == 'project')[0]?.level;
    for (let i = 0; i < this.hierarchies[this.selectedType]?.length; i++) {
      if (this.hierarchies[this.selectedType][i].level > projectLevel) {
        this.additionalFilterLevelArr.push(this.hierarchies[this.selectedType][i]);
      }
    }
    this.squadLevel = this.additionalFilterLevelArr.filter(x => x.hierarchyLevelId !== 'sprint' && x.hierarchyLevelId !== 'release');
  }

  setSelectedMapLevels() {
    const selectedType = this.kanban ? 'kanban' : 'scrum';
    const levelDetails = JSON.parse(localStorage.getItem('completeHierarchyData'))[selectedType];
    let dataCopy = {};
    levelDetails.forEach(level => {
      dataCopy[level.hierarchyLevelId] = this.filterApplyData['selectedMap'][level.hierarchyLevelName];
    });
    this.filterApplyData['selectedMap'] = dataCopy;
  }

  // unsubscribing all Kpi Request
  ngOnDestroy() {
    this.subscriptions?.forEach(subscription => subscription?.unsubscribe());
    this.parentFilterConfig = {};
    this.primaryFilterConfig = {};
    this.additionalFilterConfig = {};
    this.boardData = {};
    this.projectList = null;
    this.previousFilterEvent = null;
  }

  setSelectedDateType(label: string) {
    this.service.dateFilterSelectedDateType.next(label);
  }

  setSelectedType(type) {
    this.selectedType = type?.toLowerCase();
    if (type.toLowerCase() === 'kanban') {
      this.kanban = true;
    } else {
      this.kanban = false;
    }
    this.filterApplyData = {};
    this.service.setSelectedType(this.selectedType);
    this.helperService.setBackupOfFilterSelectionState({ 'selected_type': this.selectedType })
    this.service.setScrumKanban(this.selectedType);
  }

  processBoardData(boardData) {
    this.boardData = boardData;
    this.selectedBoard = boardData[this.selectedType ? this.selectedType : 'scrum'].filter((board => board.boardSlug.toLowerCase() === this.selectedTab.toLowerCase()))[0];
    if (!this.selectedBoard) {
      this.selectedBoard = boardData['others']?.filter((board => board.boardSlug.toLowerCase() === this.selectedTab.toLowerCase()))[0];
    }

    if (this.selectedBoard) {
      this.kanbanRequired = this.selectedBoard.filters?.projectTypeSwitch;

      if (!this.kanbanRequired?.enabled && this.selectedType === 'kanban') {
        this.kanban = false;
        this.selectedType = 'scrum';
        this.setSelectedType(this.selectedType);
        this.colorObj = {};
        return;
      }

      this.masterData['kpiList'] = this.selectedBoard.kpis;
      let newMasterData = {
        'kpiList': []
      };
      this.masterData['kpiList']?.forEach(element => {
        element = { ...element, ...element.kpiDetail };
        newMasterData['kpiList'].push(element);
      });
      this.masterData['kpiList'] = newMasterData.kpiList.filter(kpi => kpi.shown);
      this.parentFilterConfig = { ...this.selectedBoard.filters.parentFilter };
      if (!this.parentFilterConfig || !Object.keys(this.parentFilterConfig).length) {
        this.selectedLevel = null;
        this.primaryFilterConfig = { ...this.selectedBoard.filters.primaryFilter };
      }

      if (this.selectedBoard.filters.additionalFilters) {
        this.additionalFilterConfig = [...this.selectedBoard.filters.additionalFilters];
      } else {
        this.additionalFilterConfig = null;
        this.helperService.setBackupOfFilterSelectionState({ 'additional_level': null });
      }
      this.cdr.detectChanges();
    }
  }

  getFiltersData() {
    this.selectedFilterData = {};
    this.selectedFilterData['kanban'] = this.kanban;
    this.selectedFilterData['sprintIncluded'] = !this.kanban ? ['CLOSED', 'ACTIVE'] : ['CLOSED'];
    this.cdr.detectChanges();
    if (!this.objectsEqual(this.selectedFilterData, this.previousSelectedFilterData)) {
      this.previousSelectedFilterData = { ...this.selectedFilterData };
      this.subscriptions.push(
        this.httpService.getFilterData(this.selectedFilterData).subscribe((filterApiData) => {
          if (filterApiData['success']) {
            this.filterApiData = filterApiData['data'];
            this.processFilterData(filterApiData['data']);
          } else {
            // error
          }
        })
      );
    }
  }

  firstLoadFilterCheck(isKanban) {
    let selectedFilterData = {};
    selectedFilterData['kanban'] = isKanban;
    selectedFilterData['sprintIncluded'] = isKanban ? ['CLOSED'] : ['CLOSED', 'ACTIVE'];
    this.httpService.getFilterData(selectedFilterData).subscribe((filterApiData) => {
      if (filterApiData['success']) {
        if (filterApiData['data'].length >= 0) {
          let projects = filterApiData['data'].filter(x => x.labelName === 'project');
          if (isKanban) {
            this.kanbanProjectsAvailable = projects?.length > 0;
          } else {
            this.scrumProjectsAvailable = projects?.length > 0;
          }

          this.service.setNoProjectsForNewUI({
            kanban: !this.kanbanProjectsAvailable,
            scrum: !this.scrumProjectsAvailable
          });
          
          // specifically for Iteration board, to be removed when Iteration comes on ExecutiveV2
          if(!this.scrumProjectsAvailable) {
            this.service.setNoProjects(true);
          }
        }
      } else {
        // error
      }
    });
  }

  processFilterData(data) {
    if (Array.isArray(data)) {
      data.sort((a, b) => a.level - b.level);
      this.filterDataArr[this.selectedType] = data.reduce((result, currentItem) => {
        const category = currentItem.labelName;
        if (!result[category]) {
          result[category] = [];
        }

        result[category].push(currentItem);
        return result;
      }, {});
      this.setCategories();
    }
  }

  setCategories() {
    const selectedType = this.kanban ? 'kanban' : 'scrum';
    const levelDetails = JSON.parse(localStorage.getItem('completeHierarchyData'))[selectedType];
    let dataCopy = {};
    levelDetails.forEach(level => {
      dataCopy[level.hierarchyLevelName] = this.filterDataArr[this.selectedType][level.hierarchyLevelId];
    });
    dataCopy = this.removeUndefinedProperties(dataCopy);
    this.filterDataArr[this.selectedType] = dataCopy;
    if (this.filterDataArr[this.selectedType][this.selectedLevel]?.length) {
      if (!this.service.getSelectedTrends()?.length || this.service.getSelectedTrends()[0]?.labelName?.toLowerCase() === 'project') {
        let stateFilters = this.helperService.getBackupOfFilterSelectionState();
        if (stateFilters && stateFilters['primary_level']) {
          let selectedProject;
          if (stateFilters['primary_level'][0].labelName === 'project') {
            selectedProject = stateFilters['primary_level'][0];
          } else {
            selectedProject = this.filterDataArr[this.selectedType]['Project'].filter(proj => proj.nodeId === stateFilters['primary_level'][0].parentId)[0];
          }
          this.getBoardConfig([selectedProject['basicProjectConfigId']]);
        } else if (this.selectedLevel && typeof this.selectedLevel === 'string') {
          let selectedProject = this.helperService.sortAlphabetically(this.filterDataArr[this.selectedType][this.selectedLevel])[0];
          if (selectedProject) {
            this.getBoardConfig([selectedProject['basicProjectConfigId']]);
          }
        }
        else {
          let selectedProject = this.helperService.sortAlphabetically(this.filterDataArr[this.selectedType]['Project'])[0];
          if (selectedProject) {
            this.getBoardConfig([selectedProject['basicProjectConfigId']]);
          }
        }
      } else {
        this.getBoardConfig([]);
      }
    } else {
      this.getBoardConfig([]);
    }
  }

  compareStringArrays(array1, array2) {
    if (!array1 || !array2) {
      return false;
    }
    // Check if both arrays have the same length
    if (array1.length !== array2.length) {
      return false;
    }

    // Check if each corresponding element is the same
    for (let i = 0; i < array1.length; i++) {
      if (array1[i] !== array2[i]) {
        return false;
      }
    }

    return true;
  }

  getBoardConfig(projectList, event = null) {
    if (!this.compareStringArrays(projectList, this.projectList)) {
      this.blockUI = true;
      this.projectList = [...projectList];
      this.httpService.getShowHideOnDashboardNewUI({ basicProjectConfigIds: projectList?.length && projectList[0] ? projectList : [] }).subscribe(
        (response) => {
          if (response.success === true) {
            let data = response.data.userBoardConfigDTO;
            if (JSON.parse(localStorage.getItem('completeHierarchyData'))) {
              const levelDetails = JSON.parse(localStorage.getItem('completeHierarchyData'))[this.selectedType];
              data[this.selectedType].forEach((board) => {
                if (board?.filters) {
                  if (levelDetails.filter(level => level.hierarchyLevelId.toLowerCase() === board.filters.primaryFilter.defaultLevel.labelName.toLowerCase())[0]) {
                    board.filters.primaryFilter.defaultLevel.labelName = levelDetails.filter(level => level.hierarchyLevelId.toLowerCase() === board.filters.primaryFilter.defaultLevel.labelName.toLowerCase())[0].hierarchyLevelName;
                  }
                  if (board.filters.parentFilter && board.filters.parentFilter.labelName !== 'Organization Level') {
                    board.filters.parentFilter.labelName = levelDetails.filter(level => level.hierarchyLevelId === board.filters.parentFilter.labelName.toLowerCase())[0].hierarchyLevelName;
                  }
                  if (board.filters.parentFilter?.emittedLevel) {
                    if (levelDetails.filter(level => level.hierarchyLevelId === board.filters.parentFilter.emittedLevel)[0]) {
                      board.filters.parentFilter.emittedLevel = levelDetails.filter(level => level.hierarchyLevelId === board.filters.parentFilter.emittedLevel)[0].hierarchyLevelName;
                    }
                  }

                  if (board.boardSlug !== 'developer' && board.boardSlug !== 'dora') {
                    board.filters.additionalFilters?.forEach(element => {
                      if (levelDetails.filter(level => level.hierarchyLevelId === element.defaultLevel.labelName)[0]) {
                        element.defaultLevel.labelName = levelDetails.filter(level => level.hierarchyLevelId === element.defaultLevel.labelName)[0].hierarchyLevelName;
                      }
                    });
                  }
                }
              });

              data['others'].forEach((board) => {
                if (board?.filters) {
                  board.filters.primaryFilter.defaultLevel.labelName = levelDetails.filter(level => level.hierarchyLevelId === board.filters.primaryFilter.defaultLevel.labelName)[0].hierarchyLevelName;
                  if (board.filters.parentFilter && board.filters.parentFilter.labelName !== 'Organization Level') {
                    board.filters.parentFilter.labelName = levelDetails.filter(level => level.hierarchyLevelId === board.filters.parentFilter.labelName.toLowerCase())[0].hierarchyLevelName;
                  }
                  if (board.filters.parentFilter?.emittedLevel) {
                    board.filters.parentFilter.emittedLevel = levelDetails.filter(level => level.hierarchyLevelId === board.filters.parentFilter.emittedLevel)[0].hierarchyLevelName;
                  }
                }
              });
            }
            data['configDetails'] = response.data.configDetails;
            this.dashConfigData = data;
            this.service.setDashConfigData(data, false);
            this.masterData['kpiList'] = [];
            this.parentFilterConfig = {};
            this.primaryFilterConfig = {};
            this.additionalFilterConfig = [];
            this.processBoardData(data);
            this.blockUI = false;
            if (event) {
              this.prepareKPICalls(event);
            }
          }
        },
        (error) => {
          this.blockUI = false;
          this.messageService.add({
            severity: 'error',
            summary: error.message,
          });
        },
      );
    } else {
      if (event) {
        this.prepareKPICalls(event);
      }
    }
  }

  handleParentFilterChange(event) {
    this.primaryFilterConfig = { ...this.selectedBoard.filters.primaryFilter };
    this.selectedLevel = event;
  }

  setColors(data) {
    let colorsArr = ['#6079C5', '#FFB587', '#D48DEF', '#A4F6A5', '#FBCF5F', '#9FECFF']
    this.colorObj = {};
    for (let i = 0; i < data?.length; i++) {
      if (data[i]?.nodeId) {
        this.colorObj[data[i].nodeId] = { nodeName: data[i].nodeName, color: colorsArr[i], nodeId: data[i].nodeId, labelName: data[i].labelName }
      }
    }
    if (Object.keys(this.colorObj).length) {
      this.service.setColorObj(this.colorObj);
    }
  }

  objectKeys(obj) {
    return this.helperService.getObjectKeys(obj)
  }

  removeFilter(id) {
    let stateFilters = this.helperService.getBackupOfFilterSelectionState();
    if (Object.keys(this.colorObj).length > 1) {
      delete this.colorObj[id];
      if (!stateFilters['additional_level']) {
        let selectedFilters = this.filterDataArr[this.selectedType][this.selectedLevel].filter((f) => Object.values(this.colorObj).map(m => m['nodeId']).includes(f.nodeId));
        this.handlePrimaryFilterChange(selectedFilters);
        this.service.setSelectedTrends(selectedFilters);
        this.helperService.setBackupOfFilterSelectionState({ 'primary_level': selectedFilters });
      } else {
        if (typeof this.selectedLevel === 'string') {
          stateFilters['primary_level'] = this.filterDataArr[this.selectedType][this.selectedLevel].filter((f) => Object.values(this.colorObj).map(m => m['nodeId']).includes(f.nodeId));
        } else {
          stateFilters['primary_level'] = this.filterDataArr[this.selectedType][this.selectedLevel.emittedLevel].filter((f) => Object.values(this.colorObj).map(m => m['nodeId']).includes(f.nodeId));
        }

        delete stateFilters['additional_level'];

        this.filterApplyData['selectedMap']['Project'] = stateFilters['primary_level'].map((proj) => proj.nodeId);
        this.service.setSelectedTrends(stateFilters['primary_level']);

        this.handlePrimaryFilterChange(stateFilters['primary_level']);
        this.helperService.setBackupOfFilterSelectionState(stateFilters);
      }
    }
  }

  handlePrimaryFilterChange(event) {
    if (event['additional_level']) {
      Object.keys(event['additional_level']).forEach((key) => {
        if (!event['additional_level'][key]?.length) {
          delete event['additional_level'][key];
        }
      });

      if (!Object.keys(event['additional_level'])?.length) {
        delete event['additional_level'];
        event = event['primary_level'];
      }
    } else {
      // sort the event array based on nodeId
      event.sort((a, b) => (a.nodeId > b.nodeId) ? 1 : ((b.nodeId > a.nodeId) ? -1 : 0))
    }
    this.noSprint = false;

    // CAUTION
    if (event && !event['additional_level'] && event?.length && Object.keys(event[0])?.length &&
      ((!this.arrayDeepCompare(event, this.previousFilterEvent) || !this.objectsEqual(event, this.previousFilterEvent)) || this.previousSelectedTab !== this.selectedTab || this.previousSelectedType !== this.selectedType)) {
      let previousEventParentNode = ['sprint', 'release'].includes(this.previousFilterEvent[0]?.labelName?.toLowerCase()) ? this.filterDataArr[this.selectedType]['Project'].filter(proj => proj.nodeId === this.previousFilterEvent[0].parentId) : [];
      let currentEventParentNode = ['sprint', 'release'].includes(event[0]?.labelName?.toLowerCase()) ? this.filterDataArr[this.selectedType]['Project'].filter(proj => proj.nodeId === event[0].parentId) : [];
      if (!this.arrayDeepCompare(previousEventParentNode, event)) {
        if (event[0].labelName.toLowerCase() === 'project') {
          // new project selected => make boardConfig call
          this.getBoardConfig(event.map(x => x.basicProjectConfigId), event);
        } else if (!this.arrayDeepCompare(currentEventParentNode, previousEventParentNode) && currentEventParentNode?.length) {
          this.getBoardConfig(currentEventParentNode.map(x => x.basicProjectConfigId), event);
        } else {
          this.prepareKPICalls(event);
        }
      } else {
        this.prepareKPICalls(event);
      }

      // CAUTION
    } else if (event && event['additional_level']) {
      if (this.selectedTab.toLowerCase() !== 'developer') {
        setTimeout(() => {
          this.additionalFiltersArr = [];
          this.populateAdditionalFilters(event['primary_level']);
        }, 0);
      }
      this.previousFilterEvent['additional_level'] = event['additional_level'];
      this.previousFilterEvent['primary_level'] = event['primary_level'];
      this.helperService.setBackupOfFilterSelectionState({ 'additional_level': event['additional_level'] });
      Object.keys(event['additional_level']).forEach(key => {
        this.handleAdditionalChange({ [key]: event['additional_level'][key] })
      });
    } else if (!event.length || event[0].labelName.toLowerCase() !== this.primaryFilterConfig['defaultLevel'].labelName.toLowerCase()) {
      this.noSprint = true;
      this.service.setAdditionalFilters([]);
      this.previousSelectedTab = this.selectedTab;
      this.previousSelectedType = this.selectedType;
      this.colorObj = {};
      this.additionalData = false;
      this.previousFilterEvent = [];
    }
    if (this.filterDataArr && this.filterDataArr?.[this.selectedType] && this.filterDataArr[this.selectedType]?.['Sprint'] && event && event[0]?.labelName === 'project') {
      const allSprints = this.filterDataArr[this.selectedType]['Sprint'];
      const currentProjectSprints = allSprints.filter((x) => x['parentId']?.includes(event[0].nodeId) && x['sprintState']?.toLowerCase() == 'closed');
      if (currentProjectSprints?.length) {
        currentProjectSprints.sort((a, b) => new Date(a.sprintEndDate).getTime() - new Date(b.sprintEndDate).getTime());
        this.service.setSprintForRnR(currentProjectSprints[currentProjectSprints?.length - 1])
        this.noSprint = false;
      } else {
        if (this.selectedTab !== 'developer') {
          this.noSprint = true;
          this.service.setAdditionalFilters([]);
        }
        this.service.setSprintForRnR(null);
      }
    } else {
      this.noSprint = false;
    }
    this.compileGAData(event);
  }


  prepareKPICalls(event) {
    // set selected projects(trends)
    if (typeof this.selectedLevel === 'string' || this.selectedLevel === null) {
      this.service.setSelectedTrends(event);
    } else {
      this.service.setSelectedTrends(this.selectedLevel['fullNodeDetails'])
    }
    // Populate additional filters on MyKnowHOW, Speed and Quality
    if (this.selectedTab.toLowerCase() !== 'developer') {
      this.additionalFiltersArr = [];
      if (event && event[0] && event[0]?.labelName?.toLowerCase() === 'project') {
        this.populateAdditionalFilters(event);
      } else if (event && event[0]) {
        this.populateAdditionalFilters(event.map((e) => e.parentId));
      }
    } else {
      this.additionalFiltersArr = [];
    }
    if (event.length === 1) {
      this.additionalData = true;
      this.getProcessorsTraceLogsForProject();
    }
    this.previousFilterEvent = event;
    this.previousSelectedTab = this.selectedTab;
    this.previousSelectedType = this.selectedType;
    this.setColors(event);
    this.filterApplyData['level'] = event[0].level;
    this.filterApplyData['label'] = event[0].labelName;
    this.filterApplyData['selectedMap'] = {};

    if (typeof this.selectedLevel === 'object' && this.selectedLevel !== null) {
      this.filterType = `${this.selectedLevel.emittedLevel}:`;
    } else if (typeof this.selectedLevel === 'string') {
      this.filterType = `${this.selectedLevel}:`;
    } else {
      this.filterType = '';
    }
    if (Object.keys(this.filterDataArr[this.selectedType]).length) {
      if (typeof this.selectedLevel === 'string') {
        Object.keys(this.filterDataArr[this.selectedType]).forEach((filterLevel) => {
          if (filterLevel !== this.selectedLevel) {
            this.filterApplyData['selectedMap'][filterLevel] = [];
          } else {
            this.filterApplyData['selectedMap'][filterLevel] = [...new Set(event.map((item) => item.nodeId))];
          }
        });
      } else if (this.selectedLevel) {
        Object.keys(this.filterDataArr[this.selectedType]).forEach((filterLevel) => {
          if (filterLevel !== this.selectedLevel.emittedLevel) {
            this.filterApplyData['selectedMap'][filterLevel] = [];
          } else {
            this.filterApplyData['selectedMap'][filterLevel] = [...new Set(event.map((item) => item.nodeId))];
          }
        });
      } else {
        Object.keys(this.filterDataArr[this.selectedType]).forEach((filterLevel) => {
          if (filterLevel !== 'Project') {
            this.filterApplyData['selectedMap'][filterLevel] = [];
          } else {
            this.filterApplyData['selectedMap'][filterLevel] = [...new Set(event.map((item) => item.nodeId))];
          }
        });
      }
    }
    this.setSelectedMapLevels();
    if (!this.kanban) {
      if (this.selectedTab.toLocaleLowerCase() !== 'developer') {
        this.filterApplyData['ids'] = [...new Set(event.map((proj) => proj.nodeId))];
      } else {
        this.filterApplyData['ids'] = [5];
        this.filterApplyData['selectedMap']['date'] = this.selectedDayType ? [this.selectedDayType] : ['Weeks'];
        this.selectedDateFilter = `${this.selectedDateValue} ${this.selectedDayType}`;
        this.service.setSelectedDateFilter(this.selectedDayType);
      }
    } else {
      this.filterApplyData['ids'] = [this.selectedDateValue];
      this.filterApplyData['startDate'] = '';
      this.filterApplyData['endDate'] = '';
      this.filterApplyData['selectedMap']['date'] = this.selectedDayType ? [this.selectedDayType] : ['Weeks'];
      this.filterApplyData['selectedMap']['release'] = [];
      if (this.squadLevel && this.squadLevel[0]) {
        this.filterApplyData['selectedMap'][this.squadLevel[0].hierarchyLevelId] = [];
      }
    }

    if (this.selectedTab?.toLowerCase() === 'backlog') {
      this.filterApplyData['selectedMap']['sprint'] = [];
      if (this.filterDataArr[this.selectedType]['Sprint']) {
        this.filterApplyData['selectedMap']['sprint'].push(...this.filterDataArr[this.selectedType]['Sprint']?.filter((x) => x['parentId']?.includes(event[0].nodeId) && x['sprintState']?.toLowerCase() == 'closed').map(de => de.nodeId));
      }
    }

    if (this.selectedTab?.toLowerCase() === 'iteration' || this.selectedTab?.toLowerCase() === 'release') {
      this.additionalData = true;
      this.setSprintDetails(event);
    } else {
      this.additionalData = false;
    }

    this.filterApplyData['sprintIncluded'] = this.selectedTab?.toLowerCase() == 'iteration' ? ['CLOSED', 'ACTIVE'] : ['CLOSED'];
    this.service.setSelectedMap(this.filterApplyData['selectedMap']);
    if (this.filterDataArr[this.selectedType]) {
      if (this.selectedTab.toLowerCase() !== 'developer') {
        if (this.selectedLevel) {
          if (typeof this.selectedLevel === 'string') {
            this.service.select(this.masterData, this.filterDataArr[this.selectedType][this.selectedLevel], this.filterApplyData, this.selectedTab, false, true, this.boardData['configDetails'], true, this.dashConfigData, this.selectedType);
          } else {
            this.service.select(this.masterData, this.filterDataArr[this.selectedType][this.selectedLevel.emittedLevel], this.filterApplyData, this.selectedTab, false, true, this.boardData['configDetails'], true, this.dashConfigData, this.selectedType);
          }
        }
        else {
          this.service.select(this.masterData, this.filterDataArr[this.selectedType]['Project'], this.filterApplyData, this.selectedTab, false, true, this.boardData['configDetails'], true, this.dashConfigData, this.selectedType);
        }
      }
      else {
        this.applyDateFilter();
      }
    }
  }


  objectsEqual(obj1, obj2) {
    if (obj1 === obj2) {
      return true;
    }

    if (typeof obj1 !== 'object' || typeof obj2 !== 'object' || obj1 === null || obj2 === null) {
      return false;
    }

    const keys1 = Object.keys(obj1);
    const keys2 = Object.keys(obj2);

    if (keys1.length !== keys2.length) {
      return false;
    }

    for (let key of keys1) {
      if (!keys2.includes(key) || !this.objectsEqual(obj1[key], obj2[key])) {
        return false;
      }
    }

    return true;
  }

  arrayDeepCompare(a1, a2) {
    return a1.length === a2.length && a1.every((o, idx) => typeof o !== 'string' ? this.objectsEqual(o, a2[idx]) : o === a2[idx]);
  }

  setSprintDetails(event) {
    const startDatePropName = this.selectedTab?.toLowerCase() === 'iteration' ? 'sprintStartDate' : 'releaseStartDate',
      endDatePropName = this.selectedTab?.toLowerCase() === 'iteration' ? 'sprintEndDate' : 'releaseEndDate';
    const startDateFormatted = this.formatDate(event[0][startDatePropName]);
    const endDateFormatted = this.formatDate(event[0][endDatePropName]);
    this.combinedDate = `${startDateFormatted} - ${endDateFormatted}`;
    if (JSON.stringify(event[0]) !== '{}') {
      this.additionalData = true;
    } else {
      this.additionalData = false;
    }
    this.filterApplyData['ids'] = [...new Set(event.map((item) => item.nodeId))];
    this.selectedSprint = event[0];
    this.service.setCurrentSelectedSprint(this.selectedSprint);
  }

  formatDate(dateString) {
    if (dateString !== '') {
      const date = new Date(dateString);
      const day = String(date.getDate()).padStart(2, '0');
      const month = date.toLocaleString('default', { month: 'short' });
      const year = String(date.getFullYear()).slice(-2);
      return `${day} ${month}'${year}`;
    } else {
      return 'N/A';
    }
  }

  handleAdditionalChange(event) {
    let level = Object.keys(event)[0];
    event = event[level];
    if (event && event?.length) {
      if (!this.previousFilterEvent['additional_level']) {
        this.previousFilterEvent['additional_level'] = {};
      }
      this.previousFilterEvent['additional_level'][event[0].labelName] = event;
    }
    if (!event?.length) {
      this.filterApplyData['selectedMap'][level] = [];
      delete this.previousFilterEvent['additional_level'][level];
      if (!Object.keys(this.previousFilterEvent['additional_level']).length) {
        this.handlePrimaryFilterChange(this.previousFilterEvent['primary_level'] ? this.previousFilterEvent['primary_level'] : [this.previousFilterEvent[0]]);
        return;
      }
    }
    this.compileGAData(event);
    this.filterApplyData['level'] = event[0].level;
    this.filterApplyData['label'] = event[0].labelName;
    this.filterApplyData['selectedMap'] = this.service.getSelectedMap();
    // if Additional Filters are selected
    if (this.filterApplyData['level'] <= 4) return;

    if (this.selectedTab?.toLowerCase() === 'backlog') {
      this.filterApplyData['selectedMap']['sprint'] = [];
      this.filterApplyData['selectedMap']['sprint']?.push(...this.filterDataArr[this.selectedType]['sprint']?.filter((x) => x['parentId']?.includes(event[0].nodeId) && x['sprintState']?.toLowerCase() == 'closed').map(de => de.nodeId));
    }

    this.filterApplyData['ids'] = [...new Set(event.map((item) => item.nodeId))];
    this.filterApplyData['selectedMap'][this.filterApplyData['label']] = [...new Set(event.map((item) => item.nodeId))];
    let additionalFilterSelected;
    if (this.squadLevel && this.squadLevel?.length) {
      additionalFilterSelected = (this.filterApplyData['label'] === this.squadLevel[0]?.hierarchyLevelId || this.filterApplyData['label'] === this.squadLevel[0]?.hierarchyLevelName ? true : false)
    }

    this.filterApplyData['sprintIncluded'] = this.selectedTab?.toLowerCase() == 'iteration' ? ['CLOSED', 'ACTIVE'] : ['CLOSED'];
    // Promise.resolve(() => {
    if (this.filterApplyData['selectedMap']) {
      if (!this.selectedLevel) {
        this.service.select(this.masterData, this.filterDataArr[this.selectedType]['Project'], this.filterApplyData, this.selectedTab, additionalFilterSelected, true, this.boardData['configDetails'], true, this.dashConfigData, this.selectedType);
        return;
      }
      if (typeof this.selectedLevel === 'string') {
        this.service.select(this.masterData, this.filterDataArr[this.selectedType][this.selectedLevel], this.filterApplyData, this.selectedTab, additionalFilterSelected, true, this.boardData['configDetails'], true, this.dashConfigData, this.selectedType);
        return;
      }
      this.service.select(this.masterData, this.filterDataArr[this.selectedType][this.selectedLevel.emittedLevel], this.filterApplyData, this.selectedTab, additionalFilterSelected, true, this.boardData['configDetails'], true, this.dashConfigData, this.selectedType);
      // });
    }
  }

  applyDateFilter() {
    this.selectedDateFilter = `${this.selectedDateValue} ${this.selectedDayType}`;
    this.service.setSelectedDateFilter(this.selectedDayType);
    this.toggleDateDropdown = false;
    if (this.filterApplyData && this.filterApplyData['selectedMap']) {
      this.filterApplyData['selectedMap']['date'] = [this.selectedDayType];
    } else {
      this.filterApplyData['selectedMap'] = {};
      this.filterApplyData['selectedMap']['date'] = [this.selectedDayType];
    }
    this.filterApplyData['ids'] = [this.selectedDateValue];
    if (this.filterDataArr[this.selectedType]) {
      if (this.selectedLevel) {
        if (typeof this.selectedLevel === 'string') {
          this.service.select(this.masterData, this.filterDataArr[this.selectedType][this.selectedLevel], this.filterApplyData, this.selectedTab, false, true, this.boardData['configDetails'], true, this.dashConfigData, this.selectedType);
        } else {
          this.service.select(this.masterData, this.filterDataArr[this.selectedType][this.selectedLevel.emittedLevel.toLowerCase()], this.filterApplyData, this.selectedTab, false, true, this.boardData['configDetails'], true, this.dashConfigData, this.selectedType);
        }
      }
      else if (!this.parentFilterConfig || !Object.keys(this.parentFilterConfig).length) {
        this.service.select(this.masterData, this.filterDataArr[this.selectedType]['Project'], this.filterApplyData, this.selectedTab, false, true, this.boardData['configDetails'], true, this.dashConfigData, this.selectedType);
      }
    }
  }

  closeDateFilterModel() {
    this.toggleDateDropdown = false;
  }

  populateAdditionalFilters(event) {
    this.additionalFiltersArr = [];
    if (!Array.isArray(event)) {
      event = [event];
    }
    if (event?.length && event[0]) {
      let selectedProjectIds;
      if (event[0].labelName?.toLowerCase() === 'project') {
        selectedProjectIds = [...new Set(event.map((item) => item.nodeId))];
      } else if (event[0] && typeof event[0] !== 'string') {
        selectedProjectIds = [...new Set(event.map((item) => item.parentId))];
      } else if (typeof event[0] === 'string') {
        selectedProjectIds = [...new Set(event)];
      }
      this.additionalFilterConfig?.forEach((addtnlFilter, index) => {
        this.additionalFiltersArr['filter' + (index + 1)] = [];

        let allFilters = this.filterDataArr[this.selectedType] && this.filterDataArr[this.selectedType][this.getCorrectLevelMapping(addtnlFilter.defaultLevel.labelName)] ? this.filterDataArr[this.selectedType][this.getCorrectLevelMapping(addtnlFilter.defaultLevel.labelName)] : [];
        selectedProjectIds.forEach(nodeId => {
          if (allFilters?.length) {
            this.additionalFiltersArr['filter' + (index + 1)].push(...allFilters?.filter((filterItem) => {
              let parentId = '';
              let squadLevel = this.additionalFilterLevelArr.filter(x => x.hierarchyLevelId !== 'sprint' && x.hierarchyLevelId !== 'release').map(x => x.hierarchyLevelId).includes(addtnlFilter.defaultLevel.labelName) ||
                this.additionalFilterLevelArr.filter(x => x.hierarchyLevelId !== 'sprint' && x.hierarchyLevelId !== 'release').map(x => x.hierarchyLevelName).includes(addtnlFilter.defaultLevel.labelName)
              if (squadLevel && !this.kanban) {
                this.squadLevel = this.additionalFilterLevelArr.filter(x => x.hierarchyLevelId !== 'sprint' && x.hierarchyLevelId !== 'release')
                if (!this.squadLevel.map(x => x.hierarchyLevelId).includes(addtnlFilter.defaultLevel.labelName)) {
                  this.squadLevel = this.additionalFilterLevelArr.filter(x => x.hierarchyLevelId !== 'sprint' && x.hierarchyLevelId !== 'release');
                }
                parentId = filterItem.parentId.substring(filterItem.parentId.indexOf('_') + 1, filterItem.parentId.length)
              } else {
                parentId = filterItem.parentId;
              }
              return parentId === nodeId
            })
            );
          } else {
            this.additionalFiltersArr['filter' + (index + 1)] = [];
          }
        });

        // make arrays unique
        let uniqueIds = new Set();
        this.additionalFiltersArr['filter' + (index + 1)].forEach(element => {
          uniqueIds.add(element.nodeId);
        });
        let uniqueIdsArr = Array.from(uniqueIds);
        let uniqueObjArr = [];
        for (let uniqueId of uniqueIdsArr) {
          let uniqueObj = this.additionalFiltersArr['filter' + (index + 1)].filter(f => f.nodeId === uniqueId)[0];
          uniqueObjArr.push({
            ...uniqueObj
          });
          // continue;
        }
        this.additionalFiltersArr['filter' + (index + 1)] = uniqueObjArr;
      });
      if (this.selectedTab !== 'iteration') {
        this.additionalFiltersArr['filter1'] = this.additionalFiltersArr['filter1']?.filter(f => f.sprintState === 'CLOSED');
      }
      this.service.setAdditionalFilters(this.additionalFiltersArr);
    }
  }

  getCorrectLevelMapping(level) {
    let correctLevel = '';
    let squadLevelIds = this.additionalFilterLevelArr.filter(x => x.hierarchyLevelId !== 'sprint' && x.hierarchyLevelId !== 'release').map(x => x.hierarchyLevelId);

    let squadLevelNames = this.additionalFilterLevelArr.filter(x => x.hierarchyLevelId !== 'sprint' && x.hierarchyLevelId !== 'release').map(x => x.hierarchyLevelName)

    if (!squadLevelIds.includes(level) && !squadLevelNames.includes(level)) {
      correctLevel = this.additionalFilterLevelArr.filter(l => l.hierarchyLevelId.toLowerCase() === level.toLowerCase())[0]?.hierarchyLevelName;
    } else {
      correctLevel = this.additionalFilterLevelArr.filter(l => l.hierarchyLevelId.toLowerCase() === squadLevelIds[0].toLowerCase())[0]?.hierarchyLevelName;
      if (!correctLevel?.length) {
        correctLevel = this.additionalFilterLevelArr.filter(l => l.hierarchyLevelId.toLowerCase() === squadLevelNames[0].toLowerCase())[0]?.hierarchyLevelName;
      }
    }
    return correctLevel;
  }

  getProcessorsTraceLogsForProject() {
    this.httpService.getProcessorsTraceLogsForProject(this.service.getSelectedTrends()[0]?.basicProjectConfigId).subscribe(response => {
      if (response.success) {
        this.service.setProcessorLogDetails(response.data);
      } else {
        this.messageService.add({
          severity: 'error',
          summary:
            "Error in fetching processor's execution date. Please try after some time.",
        });
      }
    }, error => {
      console.log(error);
    });
  }

  fetchData() {
    this.blockUI = true;
    const sprintId = this.selectedSprint['nodeId'];
    const sprintState = this.selectedSprint['nodeId'] == sprintId ? this.selectedSprint['sprintState'] : '';
    if (sprintState?.toLowerCase() === 'active') {
      this.lastSyncData = {
        fetchSuccessful: false,
        errorInFetch: false
      };
      this.selectedProjectLastSyncStatus = '';
      this.httpService.getActiveIterationStatus({ sprintId }).subscribe(activeSprintStatus => {
        this.displayModal = false;

        if (!activeSprintStatus['success']) {
          this.lastSyncData = {};
          return;
        }

        interval(3000).pipe(switchMap(() => this.httpService.getactiveIterationfetchStatus(sprintId)), takeUntil(this.subject)).subscribe((response) => {
          if (!(response?.['success'])) {
            this.subject.next(true);
            this.lastSyncData = {};
            return;
          }

          this.selectedProjectLastSyncStatus = '';
          this.lastSyncData = response['data'];

          if (response['data']?.fetchSuccessful === true) {
            this.blockUI = false;
            this.selectedProjectLastSyncDate = response['data'].lastSyncDateTime;
            this.selectedProjectLastSyncStatus = 'SUCCESS';
            this.handlePrimaryFilterChange(this.previousFilterEvent);
            this.lastSyncData = {};
            this.messageService.add({
              severity: 'success',
              summary: 'Refreshing data',
            });
            this.subject.next(true);
          } else if (response['data']?.errorInFetch) {
            this.blockUI = false;
            this.selectedProjectLastSyncDate = response['data'].lastSyncDateTime;
            this.selectedProjectLastSyncStatus = 'FAILURE';
            this.messageService.add({
              severity: 'error',
              summary: 'Error in syncing data.',
            });
            this.subject.next(false);
            this.lastSyncData = {};
            return;
          }
        }, error => {
          this.blockUI = false;
          this.messageService.add({
            severity: 'error',
            summary: 'Error in syncing data. Please try after some time.',
          });
          this.subject.next(false);
          this.lastSyncData = {};
          return;
        });
      });
    }
  }

  compileGAData(selectedFilterArray) {
    if (selectedFilterArray && selectedFilterArray['additional_level']) {
      selectedFilterArray = selectedFilterArray['additional_level'][Object.keys(selectedFilterArray['additional_level'])[0]];
    } else if (selectedFilterArray['primary_level']) {
      selectedFilterArray = selectedFilterArray['primary_level'];
    } else if (!selectedFilterArray) {
      return;
    }
    const gaArray = selectedFilterArray?.map((item) => {
      const catArr = ['category1', 'category2', 'category3', 'category4', 'category5', 'category6'];

      let obj = {};
      let isPathAnArray = Array.isArray(item?.path);
      let pathArr = [];
      if (isPathAnArray) {
        pathArr = item?.path[0]?.split('###');
      } else {
        pathArr = item?.path?.split('###');
      }
      let pathData = {};
      pathArr = pathArr?.reverse();
      pathArr?.forEach((y, i) => {
        let selected = this.filterApiData?.filter((x) => x.nodeId == y)[0];
        pathData[catArr[i]] = selected?.nodeName;
      })
      obj = {
        'id': item.nodeId,
        'name': item.nodeName,
        'level': item.labelName,
        ...pathData
      }
      return obj;
    });
    this.ga.setProjectData(gaArray);
  }

  toggleShowHideMenu(event) {
    if (this.showHideDdn?.overlayVisible) {
      this.showHideDdn.close(event);
    } else {
      this.showHideDdn.show();
    }
  }

  showHideKPIs() {
    const kpiArray = this.dashConfigData[this.selectedType].concat(this.dashConfigData['others']);
    this.assignUserNameForKpiData();
    for (let i = 0; i < kpiArray.length; i++) {
      if (kpiArray[i].boardSlug.toLowerCase() == this.selectedTab.toLowerCase()) {
        if (this.dashConfigData[this.selectedType][i]) {
          this.dashConfigData[this.selectedType][i]['kpis'] = this.masterData['kpiList'];
        } else {
          this.dashConfigData['others'].filter(board => board.boardSlug === this.selectedTab)[0]['kpis'] = this.masterData['kpiList'];
          break;
        }
      }
    }


    let obj = Object.assign({}, this.dashConfigData);
    delete obj['configDetails'];
    this.httpService.submitShowHideOnDashboard(obj).subscribe(
      (response) => {
        if (response.success === true) {
          this.messageService.add({
            severity: 'success',
            summary: 'Successfully Saved',
            detail: '',
          });
          this.service.setDashConfigData(this.dashConfigData);
        } else {
          this.messageService.add({
            severity: 'error',
            summary: 'Error in Saving Configuraion',
          });
        }
      },
      (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error in saving kpis. Please try after some time.',
        });
      },
    );
  }

  assignUserNameForKpiData() {
    delete this.masterData['kpiList'].id;
    this.masterData['kpiList'] = this.masterData['kpiList'].map(element => {
      delete element?.kpiDetail?.id;
      return {
        kpiId: element.kpiId,
        kpiName: element.kpiName,
        isEnabled: element.isEnabled,
        shown: element.shown,
        order: element.order,
        kpiDetail: element.kpiDetail
      }
    });
    this.dashConfigData['username'] = this.service.getCurrentUserDetails('user_name');
  }

  showHideSelectAllApply() {
    this.masterData['kpiList'].forEach(element => {
      if (this.showHideSelectAll) {
        element.isEnabled = true;
      } else {
        element.isEnabled = false;
      }
    });
  }

  showChartToggle(val) {
    this.showChart = val;
    this.service.setShowTableView(this.showChart);
  }

  removeUndefinedProperties(obj) {
    for (let key in obj) {
      if (obj[key] === undefined) {
        delete obj[key];
      }
    }
    return obj;
  }

}
