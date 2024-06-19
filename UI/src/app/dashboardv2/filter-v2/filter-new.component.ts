import { Component, OnInit, ChangeDetectorRef, Output, EventEmitter, OnDestroy } from '@angular/core';
import { MessageService } from 'primeng/api';
import { HttpService } from 'src/app/services/http.service';
import { SharedService } from 'src/app/services/shared.service';
import { HelperService } from 'src/app/services/helper.service';

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
  selectedType: string = '';
  subscriptions: any[] = [];
  selectedFilterData: {};
  selectedLevel: any = 'project';
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
  filterType: string = '';
  selectedSprint: any;

  constructor(
    private httpService: HttpService,
    private service: SharedService,
    private helperService: HelperService,
    private cdr: ChangeDetectorRef,
    private messageService: MessageService,) { }


  ngOnInit(): void {
    this.selectedTab = this.service.getSelectedTab() || 'iteration';
    this.selectedType = this.helperService.getBackupOfFilterSelectionState('selected_type') ? this.helperService.getBackupOfFilterSelectionState('selected_type') : 'scrum';
    this.kanban = this.selectedType.toLowerCase() === 'kanban' ? true : false;
    // this.selectedType = this.service.getSelectedType() || 'scrum';
    this.subscriptions.push(
      this.service.globalDashConfigData.subscribe((boardData) => {
        this.processBoardData(boardData);
      })
    );

    this.subscriptions.push(
      this.service.onTypeOrTabRefresh
        .subscribe(data => {
          // if (!this.helperService.getBackupOfFilterSelectionState('selected_type')) {
          this.selectedTab = data.selectedTab;
          this.selectedType = data.selectedType;
          // } else {
          //   this.selectedTab = data.selectedTab;
          //   this.selectedType = this.helperService.getBackupOfFilterSelectionState('selected_type');
          // }

          if (this.selectedType.toLowerCase() === 'kanban') {
            this.kanban = true;
          } else {
            this.kanban = false;
          }
          this.processBoardData(this.boardData);

          this.selectedDayType = 'Weeks';
          this.dateRangeFilter = {
            "types": [
              "Days",
              "Weeks",
              "Months"
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

          this.service.setSelectedDateFilter(this.selectedDayType);
          this.selectedDateValue = this.dateRangeFilter?.counts?.[0];
          this.selectedDateFilter = `${this.selectedDateValue} ${this.selectedDayType}`;
        })
    );
  }

  // unsubscribing all Kpi Request
  ngOnDestroy() {
    this.subscriptions.forEach(subscription => subscription.unsubscribe());
  }

  setSelectedDateType(label: string) {
    this.selectedDayType = label;
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
    this.service.setSelectedTypeOrTabRefresh(this.selectedTab, this.selectedType);
  }

  processBoardData(boardData) {
    this.boardData = boardData;
    let selectedBoard = boardData[this.selectedType ? this.selectedType : 'scrum'].filter((board => board.boardSlug.toLowerCase() === this.selectedTab.toLowerCase()))[0];
    if (!selectedBoard) {
      selectedBoard = boardData['others'].filter((board => board.boardSlug.toLowerCase() === this.selectedTab.toLowerCase()))[0];
    }

    if (selectedBoard) {
      this.kanbanRequired = selectedBoard.filters?.projectTypeSwitch;

      if (!this.kanbanRequired?.enabled && this.selectedType === 'kanban') {
        this.kanban = false;
        this.selectedType = 'scrum';
        this.setSelectedType(this.selectedType);
      }

      this.getFiltersData();
      this.masterData['kpiList'] = selectedBoard.kpis;
      let newMasterData = {
        'kpiList': []
      };
      this.masterData['kpiList'].forEach(element => {
        element = { ...element, ...element.kpiDetail };
        newMasterData['kpiList'].push(element);
      });
      this.masterData['kpiList'] = newMasterData.kpiList;

      this.parentFilterConfig = selectedBoard.filters.parentFilter;
      if (!this.parentFilterConfig) {
        this.selectedLevel = null;
      }
      this.primaryFilterConfig = selectedBoard.filters.primaryFilter;
      this.additionalFilterConfig = selectedBoard.filters.additionalFilters;
    }
  }

  getFiltersData() {
    if (!Object.keys(this.filterDataArr).length || !this.filterDataArr[this.selectedType]) {
      this.selectedFilterData = {};
      this.selectedFilterData['kanban'] = this.kanban;
      this.selectedFilterData['sprintIncluded'] = !this.kanban ? ['CLOSED', 'ACTIVE'] : ['CLOSED'];
      this.cdr.detectChanges();
      this.subscriptions.push(
        this.httpService.getFilterData(this.selectedFilterData).subscribe((filterApiData) => {
          if (filterApiData['success']) {
            this.processFilterData(filterApiData['data']);
          } else {
            // error
          }
        })
      );
    }
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
    }
  }

  handleParentFilterChange(event) {
    if (typeof event === 'string') {
      this.selectedLevel = event?.toLowerCase();
    } else {
      this.selectedLevel = event;
    }
  }

  setColors(data) {
    let colorsArr = ['#6079C5', '#FFB587', '#D48DEF', '#A4F6A5', '#FBCF5F', '#9FECFF']
    this.colorObj = {};
    for (let i = 0; i < data?.length; i++) {
      if (data[i]?.nodeId) {
        this.colorObj[data[i].nodeId] = { nodeName: data[i].nodeName, color: colorsArr[i], nodeId: data[i].nodeId }
      }
    }
    if (Object.keys(this.colorObj).length) {
      setTimeout(() => {
        this.service.setColorObj(this.colorObj);
      }, 0);
    }
  }

  getObjectKeys(obj) {
    if (obj && Object.keys(obj).length) {
      return Object.keys(obj);
    } else {
      return [];
    }
  }

  removeFilter(id) {
    if (Object.keys(this.colorObj).length > 1) {
      delete this.colorObj[id];
      this.service.setColorObj(this.colorObj);
      this.helperService.setBackupOfFilterSelectionState({ 'primary_level': Object.values(this.colorObj) });
    }
  }

  arraysEqual(arr1, arr2) {
    if (arr1.length !== arr2.length) {
      return false;
    }

    for (let i = 0; i < arr1.length; i++) {
      if (!this.deepEqual(arr1[i], arr2[i])) {
        return false;
      }
    }

    return true;
  }

  deepEqual(obj1, obj2) {
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
      if (!keys2.includes(key) || !this.deepEqual(obj1[key], obj2[key])) {
        return false;
      }
    }

    return true;
  }

  handlePrimaryFilterChange(event) {
    if (event?.length && !this.arraysEqual(event, this.previousFilterEvent)) {
      // Populate additional filters on MyKnowHOW, Speed and Quality
      if (this.selectedTab.toLowerCase() !== 'developer') {
        this.additionalFiltersArr = [];
        this.service.setSelectedTrends(event);
        this.populateAdditionalFilters(event);
      }
      if (event.length === 1) {
        this.getProcessorsTraceLogsForProject();
      }
      this.previousFilterEvent = [].concat(event);
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

      if (typeof this.selectedLevel === 'string') {
        Object.keys(this.filterDataArr[this.selectedType]).forEach((filterLevel) => {
          if (filterLevel !== this.selectedLevel.toLowerCase()) {
            this.filterApplyData['selectedMap'][filterLevel] = [];
          } else {
            this.filterApplyData['selectedMap'][filterLevel] = [...new Set(event.map((item) => item.nodeId))];
          }
        });
      } else if (this.selectedLevel) {
        Object.keys(this.filterDataArr[this.selectedType]).forEach((filterLevel) => {
          if (filterLevel !== this.selectedLevel.emittedLevel.toLowerCase()) {
            this.filterApplyData['selectedMap'][filterLevel] = [];
          } else {
            this.filterApplyData['selectedMap'][filterLevel] = [...new Set(event.map((item) => item.nodeId))];
          }
        });
      } else {
        Object.keys(this.filterDataArr[this.selectedType]).forEach((filterLevel) => {
          if (filterLevel !== 'project') {
            this.filterApplyData['selectedMap'][filterLevel] = [];
          } else {
            this.filterApplyData['selectedMap'][filterLevel] = [...new Set(event.map((item) => item.nodeId))];
          }
        });
      }

      if (!this.kanban) {
        if (this.selectedTab.toLocaleLowerCase() !== 'developer') {
          this.filterApplyData['ids'] = [...new Set(event.map((proj) => proj.nodeId))];
        } else {
          this.filterApplyData['ids'] = [5];
          this.filterApplyData['selectedMap']['date'] = this.selectedDayType ? [this.selectedDayType] : ['Weeks'];
        }
      } else {
        this.filterApplyData['ids'] = [5];
        this.filterApplyData['startDate'] = '';
        this.filterApplyData['endDate'] = '';
        this.filterApplyData['selectedMap']['date'] = this.selectedDayType ? [this.selectedDayType] : ['Weeks'];
        this.filterApplyData['selectedMap']['release'] = [];
        this.filterApplyData['selectedMap']['sqd'] = [];
      }

      if (this.selectedTab?.toLowerCase() === 'backlog') {
        this.filterApplyData['selectedMap']['sprint'].push(...this.filterDataArr[this.selectedType]['sprint']?.filter((x) => x['parentId']?.includes(event[0].nodeId) && x['sprintState']?.toLowerCase() == 'closed').map(de => de.nodeId));
      }

      if (this.selectedTab?.toLowerCase() === 'iteration') {
        this.filterApplyData['ids'] = [...new Set(event.map((item) => item.nodeId))];
        this.selectedSprint = event[0];
        this.service.setCurrentSelectedSprint(this.selectedSprint);
      }

      this.filterApplyData['sprintIncluded'] = this.selectedTab?.toLowerCase() == 'iteration' ? ['CLOSED', 'ACTIVE'] : ['CLOSED'];

      // set selected projects(trends)
      this.service.setSelectedTrends(event);

      if (this.selectedLevel) {
        if (typeof this.selectedLevel === 'string') {
          this.service.select(this.masterData, this.filterDataArr[this.selectedType][this.selectedLevel], this.filterApplyData, this.selectedTab, false, true, this.boardData['configDetails'], true);
        } else {
          this.service.select(this.masterData, this.filterDataArr[this.selectedType][this.selectedLevel.emittedLevel.toLowerCase()], this.filterApplyData, this.selectedTab, false, true, this.boardData['configDetails'], true);
        }
      } else {
        this.service.select(this.masterData, this.filterDataArr[this.selectedType]['project'], this.filterApplyData, this.selectedTab, false, true, this.boardData['configDetails'], true);
      }
    } else {
      if (this.selectedTab.toLowerCase() !== 'developer') {
        this.additionalFiltersArr = [];
        this.populateAdditionalFilters(event);
      }

      if (this.selectedLevel) {
        if (typeof this.selectedLevel === 'string') {
          this.service.select(this.masterData, this.filterDataArr[this.selectedType][this.selectedLevel], this.filterApplyData, this.selectedTab, false, true, this.boardData['configDetails'], true);
        } else {
          this.service.select(this.masterData, this.filterDataArr[this.selectedType][this.selectedLevel.emittedLevel.toLowerCase()], this.filterApplyData, this.selectedTab, false, true, this.boardData['configDetails'], true);
        }
      } else {
        this.service.select(this.masterData, this.filterDataArr[this.selectedType]['project'], this.filterApplyData, this.selectedTab, false, true, this.boardData['configDetails'], true);
      }
    }
  }

  handleAdditionalChange(event) {
    // if (event?.length && !this.arraysEqual(event, this.previousFilterEvent)) {
    if (event?.length) {
      this.filterApplyData['level'] = event[0].level;
      this.filterApplyData['label'] = event[0].labelName;
      // if Additional Filters are selected
      if (this.filterApplyData['level'] > 4) {
        if (this.selectedTab?.toLowerCase() === 'backlog') {
          this.filterApplyData['selectedMap']['sprint'].push(...this.filterDataArr[this.selectedType]['sprint']?.filter((x) => x['parentId']?.includes(event[0].nodeId) && x['sprintState']?.toLowerCase() == 'closed').map(de => de.nodeId));
        }


        this.filterApplyData['ids'] = [...new Set(event.map((item) => item.nodeId))];
        this.filterApplyData['selectedMap'][this.filterApplyData['label']] = [...new Set(event.map((item) => item.nodeId))];


        // set selected projects(trends)
        this.service.setSelectedTrends(event);

        if (this.selectedLevel) {
          if (typeof this.selectedLevel === 'string') {
            this.service.select(this.masterData, this.filterDataArr[this.selectedType][this.selectedLevel], this.filterApplyData, this.selectedTab, false, true, this.boardData['configDetails'], true);
          } else {
            this.service.select(this.masterData, this.filterDataArr[this.selectedType][this.selectedLevel.emittedLevel.toLowerCase()], this.filterApplyData, this.selectedTab, false, true, this.boardData['configDetails'], true);
          }
        } else {
          this.service.select(this.masterData, this.filterDataArr[this.selectedType]['project'], this.filterApplyData, this.selectedTab, false, true, this.boardData['configDetails'], true);
        }
      }
    }
  }

  applyDateFilter() {
    this.selectedDateFilter = `${this.selectedDateValue} ${this.selectedDayType}`;
    this.service.setSelectedDateFilter(this.selectedDayType);
    this.toggleDateDropdown = false;
    this.filterApplyData['selectedMap']['date'] = [this.selectedDayType];
    this.filterApplyData['ids'] = [this.selectedDateValue];

    if (this.selectedLevel) {
      if (typeof this.selectedLevel === 'string') {
        this.service.select(this.masterData, this.filterDataArr[this.selectedType][this.selectedLevel], this.filterApplyData, this.selectedTab, false, true, this.boardData['configDetails'], true);
      } else {
        this.service.select(this.masterData, this.filterDataArr[this.selectedType][this.selectedLevel.emittedLevel.toLowerCase()], this.filterApplyData, this.selectedTab, false, true, this.boardData['configDetails'], true);
      }
    } else {
      this.service.select(this.masterData, this.filterDataArr[this.selectedType]['project'], this.filterApplyData, this.selectedTab, false, true, this.boardData['configDetails'], true);
    }
  }

  populateAdditionalFilters(event) {
    let selectedProjectIds = [...new Set(event.map((item) => item.nodeId))];
    this.additionalFilterConfig?.forEach((addtnlFilter, index) => {
      this.additionalFiltersArr['filter' + (index + 1)] = [];

      let allFilters = this.filterDataArr[this.selectedType][addtnlFilter.defaultLevel.labelName];
      selectedProjectIds.forEach(nodeId => {

        this.additionalFiltersArr['filter' + (index + 1)].push(...allFilters.filter((filterItem) => {
          let parentId = '';
          if (addtnlFilter.defaultLevel.labelName === 'sqd') {
            parentId = filterItem.parentId.substring(filterItem.parentId.indexOf('_') + 1, filterItem.parentId.length)
          } else {
            parentId = filterItem.parentId;
          }
          return parentId === nodeId
        })
        );
      });

      // make arrays unique
      let uniqueIds = new Set();
      this.additionalFiltersArr['filter' + (index + 1)].forEach(element => {
        uniqueIds.add(element.nodeId);
      });
      let uniqueIdsArr = Array.from(uniqueIds);
      let uniqueObjArr = [];
      for (let i = 0; i < uniqueIdsArr.length; i++) {
        let uniqueObj = this.additionalFiltersArr['filter' + (index + 1)].filter(f => f.nodeId === uniqueIdsArr[i])[0];
        uniqueObjArr.push({
          ...uniqueObj
        });
        // continue;
      }
      this.additionalFiltersArr['filter' + (index + 1)] = uniqueObjArr;
    });

    this.service.setAdditionalFilters(this.additionalFiltersArr);
  }

  getProcessorsTraceLogsForProject() {
    this.httpService.getProcessorsTraceLogsForProject(this.previousFilterEvent[0]?.basicProjectConfigId).subscribe((response) => {
      if (response.success) {
        this.service.setProcessorLogDetails(response.data);
      } else {
        this.messageService.add({
          severity: 'error',
          summary:
            "Error in fetching processor's execution date. Please try after some time.",
        });
      }
    });
  }
}
