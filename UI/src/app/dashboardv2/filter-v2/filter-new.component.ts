import { Component, OnInit, ChangeDetectorRef, Output, EventEmitter } from '@angular/core';
import { HttpService } from 'src/app/services/http.service';
import { SharedService } from 'src/app/services/shared.service';

@Component({
  selector: 'app-filter-new',
  templateUrl: './filter-new.component.html',
  styleUrls: ['./filter-new.component.css']
})
export class FilterNewComponent implements OnInit {
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
  kanbanRequired: boolean = false;
  parentFilterConfig: any = {};
  primaryFilterConfig: any = {};
  additionalFilterConfig: any = {};
  selectedNodeIdArr: any = {
    "basicProjectConfigIds": []
  };
  colorObj: any = {};
  previousFilterEvent: any = [];

  constructor(
    private httpService: HttpService,
    private service: SharedService,
    private cdr: ChangeDetectorRef) { }

  ngOnInit(): void {
    this.selectedTab = this.service.getSelectedTab() || 'iteration';

    this.subscriptions.push(
      this.service.globalDashConfigData.subscribe((boardData) => {
        this.processBoardData(boardData);

        this.subscriptions.push(
          this.service.onTypeOrTabRefresh
            .subscribe(data => {
              this.selectedTab = data.selectedTab;
              this.selectedType = data.selectedType;
              if (this.selectedType.toLowerCase() === 'kanban') {
                this.kanban = true;
              } else {
                this.kanban = false;
              }
              this.processBoardData(this.boardData);
            })
        );
      })
    );
  }

  setSelectedType(type) {
    this.selectedType = type?.toLowerCase();
    if (type.toLowerCase() === 'kanban') {
      this.kanban = true;
    } else {
      this.kanban = false;
    }
    this.filterApplyData = {};
    this.service.setSelectedTypeOrTabRefresh(this.selectedTab, this.selectedType);
  }

  processBoardData(boardData) {
    this.boardData = boardData;
    let selectedBoard = boardData[this.selectedType ? this.selectedType : 'scrum'].filter((board => board.boardSlug.toLowerCase() === this.selectedTab.toLowerCase()))[0];
    if (!selectedBoard) {
      selectedBoard = boardData['others'].filter((board => board.boardSlug.toLowerCase() === this.selectedTab.toLowerCase()))[0];
    }

    if (selectedBoard) {
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
      this.kanbanRequired = selectedBoard.filters.kanbanRequired;
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
    let colorsArr = ['#6079C5', '#A4F6A5', '#FBCF5F', '#9FECFF', '#FFB587', '#D48DEF']
    this.colorObj = {};
    for (let i = 0; i < data?.length; i++) {
      if (data[i] && data[i].nodeId) {
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
      this.previousFilterEvent = [].concat(event);
      // remove the last 2 elements from event
      event.splice(-2);
      this.setColors(event);
      this.filterApplyData['level'] = event[0].level;
      this.filterApplyData['label'] = event[0].labelName;
      this.filterApplyData['selectedMap'] = {};
      console.log(this.selectedLevel);
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
          this.filterApplyData['selectedMap']['date'] = ['DAYS']
        }
      } else {
        if (this.selectedTab === 'Iteration') {
          this.filterApplyData['ids'] = [...new Set(event.map((item) => item.nodeId))];
        } else {
          this.filterApplyData['ids'] = [5];
        }
        this.filterApplyData['startDate'] = '';
        this.filterApplyData['endDate'] = '';
        this.filterApplyData['selectedMap']['date'] = ['WEEKS'];
        this.filterApplyData['selectedMap']['release'] = [];
        this.filterApplyData['selectedMap']['sqd'] = [];
      }

      if (this.selectedTab?.toLowerCase() === 'backlog') {
        this.filterApplyData['selectedMap']['sprint'].push(...this.filterDataArr[this.selectedType]['sprint']?.filter((x) => x['parentId']?.includes(event[0].nodeId) && x['sprintState']?.toLowerCase() == 'closed').map(de => de.nodeId));
      }

      this.filterApplyData['sprintIncluded'] = this.selectedTab?.toLowerCase() == 'iteration' ? ['CLOSED', 'ACTIVE'] : ['CLOSED'];

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
