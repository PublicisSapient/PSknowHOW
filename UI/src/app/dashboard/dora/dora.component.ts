import { Component, OnInit, ViewChild } from '@angular/core';
import { distinctUntilChanged, mergeMap } from 'rxjs/operators';
import { SharedService } from 'src/app/services/shared.service';
import { HelperService } from 'src/app/services/helper.service';
import { HttpService } from 'src/app/services/http.service';
import { ExportExcelComponent } from 'src/app/component/export-excel/export-excel.component';

@Component({
  selector: 'app-dora',
  templateUrl: './dora.component.html',
  styleUrls: ['./dora.component.css']
})
export class DoraComponent implements OnInit {
  @ViewChild('exportExcel') exportExcelComponent: ExportExcelComponent;
  masterData;
  filterData = [];
  jenkinsKpiData = {};
  jiraKpiData = {};
  filterApplyData;
  kpiJenkins;
  kpiJira;
  loaderJenkins = false;
  subscriptions: any[] = [];
  jenkinsKpiRequest;
  jiraKpiRequest;
  tooltip;
  selectedtype = 'Scrum';
  configGlobalData;
  kanbanActivated = false;
  serviceObject = {};
  allKpiArray: any = [];
  colorObj = {};
  chartColorList: object = {};
  kpiSelectedFilterObj = {};
  kpiChartData = {};
  noKpis = false;
  enableByUser = false;
  updatedConfigGlobalData;
  kpiConfigData = {};
  kpiLoader = true;
  noTabAccess = false;
  trendBoxColorObj: any;
  kpiDropdowns = {};
  showKpiTrendIndicator = {};
  hierarchyLevel;
  showChart = 'chart';
  displayModal = false;
  modalDetails = {
    header: '',
    tableHeadings: [],
    tableValues: []
  };
  kpiExcelData;
  isGlobalDownload = false;
  kpiTrendsObj = {};
  selectedTab = 'dora';
  showCommentIcon = false;
  noProjects = false;
  globalConfig;
  sharedObject;
  sprintsOverlayVisible: boolean = false;
  kpiCommentsCountObj: object = {};
  noOfFilterSelected = 0;
  selectedJobFilter = 'Select';
  loaderJiraArray = [];
  updatedConfigDataObj: object = {};
  kpiThresholdObj = {};
  isTooltip = '';
  maturityObj = {};
  toolTipTop: number = 0;

  constructor(private service: SharedService, private httpService: HttpService, private helperService: HelperService) {
    this.subscriptions.push(this.service.passDataToDashboard.pipe(distinctUntilChanged()).subscribe((sharedobject) => {
      if (sharedobject?.filterData?.length && sharedobject.selectedTab.toLowerCase() === 'dora') {
        this.allKpiArray = [];
        this.kpiChartData = {};
        this.kpiSelectedFilterObj = {};
        this.kpiDropdowns = {};
        this.sharedObject = sharedobject;
        if (this.globalConfig || this.service.getDashConfigData()) {
          this.receiveSharedData(sharedobject);
        }
        this.noTabAccess = false;
      } else {
        this.noTabAccess = true;
      }
    }));

    this.subscriptions.push(this.service.globalDashConfigData.subscribe((globalConfig) => {
      this.configGlobalData = globalConfig['others'].filter((item) => item.boardName.toLowerCase() == 'dora')[0]?.kpis;
      this.processKpiConfigData();
    }));
  }

  ngOnInit(): void {
    this.subscriptions.push(this.service.mapColorToProjectObs.subscribe((x) => {
      if (Object.keys(x).length > 0) {
        this.colorObj = x;
        if (this.kpiChartData && Object.keys(this.kpiChartData)?.length > 0) {
          this.trendBoxColorObj = { ...x };
          for (const key in this.trendBoxColorObj) {
            const idx = key.lastIndexOf('_');
            const nodeName = key.slice(0, idx);
            this.trendBoxColorObj[nodeName] = this.trendBoxColorObj[key];
          }
        }
      }
    }));

    this.httpService.getConfigDetails().subscribe(filterData => {
      if (filterData[0] !== 'error') {
        this.tooltip = filterData;
        this.service.setGlobalConfigData(filterData);
      }
    });

    this.subscriptions.push(this.service.noProjectsObs.subscribe((res) => {
      this.noProjects = res;
      this.kanbanActivated = this.service.getSelectedType()?.toLowerCase() === 'kanban' ? true : false;
    }));

    this.service.getEmptyData().subscribe((val) => {
      if (val) {
        this.noTabAccess = true;
      } else {
        this.noTabAccess = false;
      }
    });
  }

  processKpiConfigData() {
    const disabledKpis = this.configGlobalData?.filter(item => item.shown && !item.isEnabled);
    // user can enable kpis from show/hide filter, added below flag to show different message to the user
    this.enableByUser = disabledKpis?.length ? true : false;
    // noKpis - if true, all kpis are not shown to the user (not showing kpis to the user)
    this.updatedConfigGlobalData = this.configGlobalData?.filter(item => item.shown && item.isEnabled);
    if (this.updatedConfigGlobalData?.length === 0) {
      this.noKpis = true;
    } else {
      this.noKpis = false;
    }
    this.configGlobalData?.forEach(element => {
      if (element.shown && element.isEnabled) {
        this.kpiConfigData[element.kpiId] = true;
      } else {
        this.kpiConfigData[element.kpiId] = false;
      }
    });

    this.updatedConfigGlobalData?.forEach((item) =>
      this.updatedConfigDataObj[item.kpiId] = { ...item }
    );
  }

  generateColorObj(kpiId, arr) {
    const finalArr = [];
    if (arr?.length > 0) {
      this.chartColorList[kpiId] = [];
      for (let i = 0; i < arr?.length; i++) {
        for (const key in this.colorObj) {
          if (this.colorObj[key]?.nodeName == arr[i]?.data) {
            this.chartColorList[kpiId].push(this.colorObj[key]?.color);
            finalArr.push(arr.filter((a) => a.data === this.colorObj[key].nodeName)[0]);
            // break;
          }
        }
      }
    }
    return finalArr;
  }


  getKpiCommentsCount(kpiId?) {
    let requestObj = {
      "nodes": [...this.filterApplyData?.['selectedMap']['project']],
      "level": this.filterApplyData?.level,
      "nodeChildId": "",
      'kpiIds': []
    };
    if (kpiId) {
      requestObj['kpiIds'] = [kpiId];
      this.helperService.getKpiCommentsHttp(requestObj).then((res: object) => {
        this.kpiCommentsCountObj[kpiId] = res[kpiId];
      });
    } else {
      requestObj['kpiIds'] = (this.updatedConfigGlobalData?.map((item) => item.kpiId));
      this.helperService.getKpiCommentsHttp(requestObj).then((res: object) => {
        this.kpiCommentsCountObj = res;
      });
    }
  }

  receiveSharedData($event) {
    this.isTooltip = '';
    this.sprintsOverlayVisible = this.service.getSelectedLevel()['hierarchyLevelId'] === 'project' ? true : false;
    if (localStorage?.getItem('completeHierarchyData')) {
      const hierarchyData = JSON.parse(localStorage.getItem('completeHierarchyData'));
      if (Object.keys(hierarchyData).length > 0 && hierarchyData[this.selectedtype.toLowerCase()]) {
        this.hierarchyLevel = hierarchyData[this.selectedtype.toLowerCase()];
      }
    }
    this.configGlobalData = this.service.getDashConfigData()['others'].filter((item) => item.boardName.toLowerCase() == 'dora')[0]?.kpis;
    this.processKpiConfigData();
    this.masterData = $event.masterData;
    this.filterData = $event.filterData;
    this.filterApplyData = $event.filterApplyData;
    this.noOfFilterSelected = Object.keys(this.filterApplyData).length;
    if (this.filterData?.length) {
      this.noTabAccess = false;
      const kpiIdsForCurrentBoard = this.configGlobalData?.map(kpiDetails => kpiDetails.kpiId);
      // call kpi request according to tab selected
      if (this.masterData && Object.keys(this.masterData).length) {
        this.groupJenkinsKpi(kpiIdsForCurrentBoard);
        this.groupJiraKpi(kpiIdsForCurrentBoard);
        this.getKpiCommentsCount();
      }
    } else {
      this.noTabAccess = true;
    }
    if (this.hierarchyLevel && this.hierarchyLevel[+this.filterApplyData.level - 1]?.hierarchyLevelId === 'project') {
      this.showCommentIcon = true;
    } else {
      this.showCommentIcon = false;
    }
  }

  // download excel functionality
  downloadExcel(kpiId, kpiName, isKanban, additionalFilterSupport) {
    this.exportExcelComponent.downloadExcel(kpiId, kpiName, isKanban, additionalFilterSupport, this.filterApplyData, this.filterData, false);
  }

  // Used for grouping all Jenkins kpi from master data and calling jenkins kpi.
  groupJenkinsKpi(kpiIdsForCurrentBoard) {
    // creating a set of unique group Ids
    const groupIdSet = new Set();
    this.masterData?.kpiList.forEach((obj) => {
      if (!obj.kanban && obj.kpiSource === 'Jenkins' && obj.kpiCategory?.toLowerCase() == 'dora') {
        groupIdSet.add(obj.groupId);
      }
    });

    // sending requests after grouping the the KPIs according to group Id
    groupIdSet.forEach((groupId) => {
      if (groupId) {
        this.kpiJenkins = this.helperService.groupKpiFromMaster('Jenkins', false, this.masterData, this.filterApplyData, this.filterData, kpiIdsForCurrentBoard, groupId, 'dora');
        if (this.kpiJenkins?.kpiList?.length > 0) {
          for (let i = 0; i < this.kpiJenkins?.kpiList?.length; i++) {
            this.kpiJenkins.kpiList[i]['filterDuration'] = {
              duration: 'WEEKS',
              value: 8
            }
          }
          this.postJenkinsKpi(this.kpiJenkins, 'jenkins');
        }
      }
    });
  }

  // Used for grouping all Jira kpi from master data and calling Jira kpi.
  groupJiraKpi(kpiIdsForCurrentBoard) {
    this.jiraKpiData = {};
    // creating a set of unique group Ids
    const groupIdSet = new Set();
    this.masterData?.kpiList.forEach((obj) => {
      if (!obj.kanban && obj.kpiSource === 'Jira' && obj.kpiCategory?.toLowerCase() == 'dora') {
        groupIdSet.add(obj.groupId);
      }
    });

    // sending requests after grouping the the KPIs according to group Id
    groupIdSet.forEach((groupId) => {
      if (groupId) {
        this.kpiJira = this.helperService.groupKpiFromMaster('Jira', false, this.masterData, this.filterApplyData, this.filterData, kpiIdsForCurrentBoard, groupId, 'Dora');
        if (this.kpiJira?.kpiList?.length > 0) {
          this.postJiraKpi(this.kpiJira, 'jira');
        }
      }
    });

  }

  // calling post request of Jenkins of scrum and storing in jenkinsKpiData id wise
  postJenkinsKpi(postData, source): void {
    this.loaderJenkins = true;
    if (this.jenkinsKpiRequest && this.jenkinsKpiRequest !== '') {
      this.jenkinsKpiRequest.unsubscribe();
    }
    this.jenkinsKpiRequest = this.httpService.postKpi(postData, source)
      .subscribe(getData => {
        this.loaderJenkins = false;
        if (getData !== null) {
          this.jenkinsKpiData = getData;
          this.createAllKpiArray(this.jenkinsKpiData);
        }
        this.kpiLoader = false;
      });
  }

  // post request of Jira(scrum)
  postJiraKpi(postData, source): void {

    postData.kpiList.forEach(element => {
      this.loaderJiraArray.push(element.kpiId);
    });

    this.jiraKpiRequest = this.httpService.postKpi(postData, source)
      .subscribe(getData => {
        if (getData !== null && getData[0] !== 'error' && !getData['error']) {
          // creating array into object where key is kpi id
          const localVariable = this.helperService.createKpiWiseId(getData);
          for (const kpi in localVariable) {
            this.loaderJiraArray.splice(this.loaderJiraArray.indexOf(kpi), 1);
          }
          this.jiraKpiData = Object.assign({}, this.jiraKpiData, localVariable);
          this.createAllKpiArray(localVariable);
        } else {
          this.jiraKpiData = getData;
          postData.kpiList.forEach(element => {
            this.loaderJiraArray.splice(this.loaderJiraArray.indexOf(element.kpiId), 1);
          });
        }
        this.kpiLoader = false;
      });
  }


  ifKpiExist(kpiId) {
    const id = this.allKpiArray?.findIndex((kpi) => kpi.kpiId == kpiId);
    return id;
  }

  sortAlphabetically(objArray) {
    if (objArray && objArray?.length > 1) {
      objArray?.sort((a, b) => a.data?.localeCompare(b.data));
    }
    return objArray;
  }

  /** get array of the kpi level dropdown filter */
  getDropdownArray(kpiId) {
    const idx = this.ifKpiExist(kpiId);
    let trendValueList = [];
    const optionsArr = [];

    if (idx != -1) {
      trendValueList = this.allKpiArray[idx]?.trendValueList;
      if (trendValueList?.length > 0 && trendValueList[0]?.hasOwnProperty('filter')) {
        const obj = {};
        for (let i = 0; i < trendValueList?.length; i++) {
          if (trendValueList[i]?.filter?.toLowerCase() != 'overall') {
            optionsArr?.push(trendValueList[i]?.filter);
          }
        }
        obj['filterType'] = 'Select a filter';
        obj['options'] = optionsArr;
        this.kpiDropdowns[kpiId] = [];
        this.kpiDropdowns[kpiId].push(obj);
      }
    }
  }

  getChartData(kpiId, idx, aggregationType) {
    const trendValueList = this.allKpiArray[idx]?.trendValueList;
    this.kpiThresholdObj[kpiId] = this.allKpiArray[idx]?.thresholdValue ? this.allKpiArray[idx]?.thresholdValue : null;
    if (trendValueList?.length > 0 && trendValueList[0]?.hasOwnProperty('filter')) {
      if (this.kpiSelectedFilterObj[kpiId]?.length > 1) {
        const tempArr = {};
        for (let i = 0; i < this.kpiSelectedFilterObj[kpiId]?.length; i++) {
          tempArr[this.kpiSelectedFilterObj[kpiId][i]] = (trendValueList?.filter(x => x['filter'] == this.kpiSelectedFilterObj[kpiId][i])[0]?.value);
          tempArr[this.kpiSelectedFilterObj[kpiId][i]][0]['aggregationValue'] = trendValueList?.filter(x => x['filter'] == this.kpiSelectedFilterObj[kpiId][i])[0]?.value[0]?.aggregationValue;
        }
        this.kpiChartData[kpiId] = this.helperService.applyAggregationLogic(tempArr, aggregationType, this.tooltip.percentile);
      } else {
        if (this.kpiSelectedFilterObj[kpiId]?.length > 0) {
          this.kpiChartData[kpiId] = trendValueList?.filter(x => x['filter'] == this.kpiSelectedFilterObj[kpiId][0])[0]?.value;
          this.kpiChartData[kpiId][0]['aggregationValue'] = trendValueList?.filter(x => x['filter'] == this.kpiSelectedFilterObj[kpiId][0])[0]?.value[0]?.aggregationValue;
        } else {
          this.kpiChartData[kpiId] = trendValueList?.filter(x => x['filter'] == 'Overall')[0]?.value;
          this.kpiChartData[kpiId][0]['aggregationValue'] = trendValueList?.filter(x => x['filter'] == 'Overall')[0]?.value[0]?.aggregationValue;
        }
      }
    }
    else {
      if (trendValueList?.length > 0) {
        this.kpiChartData[kpiId] = [...this.sortAlphabetically(trendValueList)];
      } else {
        this.kpiChartData[kpiId] = [];
      }
    }

    if (this.colorObj && Object.keys(this.colorObj)?.length > 0) {
      this.kpiChartData[kpiId] = this.generateColorObj(kpiId, this.kpiChartData[kpiId]);
    }

    // if (this.kpiChartData && Object.keys(this.kpiChartData) && Object.keys(this.kpiChartData).length === this.updatedConfigGlobalData.length) {
    // if (this.kpiChartData && Object.keys(this.kpiChartData).length && this.updatedConfigGlobalData) {
    //   this.helperService.calculateGrossMaturity(this.kpiChartData, this.updatedConfigGlobalData);
    // }
    this.setMaturityColor(kpiId, this.kpiSelectedFilterObj[kpiId]);
  }

  createAllKpiArray(data, inputIsChartData = false) {
    for (const key in data) {
      const idx = this.ifKpiExist(data[key]?.kpiId);
      if (idx !== -1) {
        this.allKpiArray.splice(idx, 1);
      }
      this.allKpiArray.push(data[key]);
      const trendValueList = this.allKpiArray[this.allKpiArray?.length - 1]?.trendValueList;
      if ((trendValueList?.length > 0 && trendValueList[0]?.hasOwnProperty('filter')) || (trendValueList?.length > 0 && trendValueList[0]?.hasOwnProperty('filter1'))) {
        this.kpiSelectedFilterObj[data[key]?.kpiId] = [];
        this.getDropdownArray(data[key]?.kpiId);
        const formType = this.updatedConfigGlobalData?.filter(x => x.kpiId == data[key]?.kpiId)[0]?.kpiDetail?.kpiFilter;
        if (formType?.toLowerCase() == 'radiobutton') {
          this.kpiSelectedFilterObj[data[key]?.kpiId]?.push(this.kpiDropdowns[data[key]?.kpiId][0]?.options[0]);
        } else if (formType?.toLowerCase() == 'dropdown') {
          this.kpiSelectedFilterObj[data[key]?.kpiId]?.push(this.kpiDropdowns[data[key]?.kpiId][0]?.options[0]);
          this.kpiSelectedFilterObj[data[key]?.kpiId] = {};
          let initialC = trendValueList[0].filter1;
          this.kpiSelectedFilterObj[data[key]?.kpiId] = { 'filter': ['Overall'] };
        } else {
          this.kpiSelectedFilterObj[data[key]?.kpiId]?.push('Overall');
        }
        this.kpiSelectedFilterObj['action'] = 'new';
        this.service.setKpiSubFilterObj(this.kpiSelectedFilterObj);
      }
      const agType = this.updatedConfigGlobalData?.filter(x => x.kpiId == data[key]?.kpiId)[0]?.kpiDetail?.aggregationCriteria;
      if (!inputIsChartData) {
        this.getChartData(data[key]?.kpiId, (this.allKpiArray?.length - 1), agType);
      }
    }
  }

  handleSelectedOption(event, kpi) {
    this.kpiSelectedFilterObj[kpi?.kpiId] = [];

    if (event && Object.keys(event)?.length !== 0 && typeof event === 'object') {
      for (const key in event) {
        if (event[key]?.length == 0) {
          delete event[key];
          this.kpiSelectedFilterObj[kpi?.kpiId] = event;
        } else if (Array.isArray(event[key])) {
          for (let i = 0; i < event[key]?.length; i++) {
            this.kpiSelectedFilterObj[kpi?.kpiId] = [...this.kpiSelectedFilterObj[kpi?.kpiId], event[key][i]];
          }
        } else {
          this.kpiSelectedFilterObj[kpi?.kpiId] = [...this.kpiSelectedFilterObj[kpi?.kpiId], event[key]];
        }
      }
    } else {
      this.kpiSelectedFilterObj[kpi?.kpiId].push(event);
    }

    this.getChartData(kpi?.kpiId, this.ifKpiExist(kpi?.kpiId), kpi?.kpiDetail?.aggregationCriteria);
    this.kpiSelectedFilterObj['action'] = 'update';
    this.service.setKpiSubFilterObj(this.kpiSelectedFilterObj);
  }

  // unsubscribing all Kpi Request
  ngOnDestroy() {
    this.subscriptions.forEach(subscription => subscription.unsubscribe());
    this.sharedObject = null;
    this.globalConfig = null;
  }

  reloadKPI(event) {
    const idx = this.ifKpiExist(event?.kpiDetail?.kpiId)
    if (idx !== -1) {
      this.allKpiArray.splice(idx, 1);
    }
    const currentKPIGroup = this.helperService.groupKpiFromMaster(event?.kpiDetail?.kpiSource, event?.kpiDetail?.kanban, this.masterData, this.filterApplyData, this.filterData, {}, event.kpiDetail?.groupId, 'Dora');
    if (currentKPIGroup?.kpiList?.length > 0) {
      const kpiSource = event.kpiDetail?.kpiSource?.toLowerCase();
      switch (kpiSource) {
        case 'jenkins':
          this.postJenkinsKpi(currentKPIGroup, 'jenkins');
          break;
        default:
          this.postJiraKpi(currentKPIGroup, 'jira');
      }
    }
  }

  setMaturityColor(kpiId, selectedFilter = null) {
    let selectedKPI = this.allKpiArray.filter(kpi => kpi.kpiId === kpiId)[0];
    let maturity = '';
    if (this.kpiChartData[kpiId] && this.kpiChartData[kpiId].length && selectedKPI) {
      if (!selectedFilter) {
        maturity = 'M' + this.kpiChartData[kpiId][0].maturity;
      } else {
        maturity = this.calculateMaturity(kpiId, selectedKPI.maturityRange);
      }
      let maturityColor = selectedKPI.maturityLevel.filter((level) => level.level === maturity)[0]?.bgColor;
      this.kpiChartData[kpiId][0].maturityColor = maturityColor;
      this.getMaturityData(kpiId);
    }
  }

  calculateMaturity(kpiId, maturityRange) {
    if (maturityRange && maturityRange.length) {
      let aggregatedValue = this.kpiChartData[kpiId][0]?.['aggregationValue'];
      let maturity = '';

      let findIncrementalOrDecrementalRange = this.findIncrementalOrDecrementalRange(maturityRange);

      switch (findIncrementalOrDecrementalRange) {
        case 'incremental':
          for (let i = 0; i < maturityRange.length; i++) {
            const range = maturityRange[i].split('-');
            const lowerBound = parseInt(range[0]);
            const upperBound = range[1] === '' ? Infinity : parseInt(range[1]);
            if (upperBound === Infinity) {
              if (aggregatedValue >= lowerBound) {
                maturity = `M${i + 1}`;
                break;
              }
            } else {
              if (aggregatedValue >= lowerBound && aggregatedValue < upperBound) {
                maturity = `M${i + 1}`;
                break;
              }
            }
          }
          break;

        case 'decremental':
          for (let i = 0; i < maturityRange.length; i++) {
            const range = maturityRange[i].split('-');
            const upperBound = range[0] === '' ? Infinity : parseInt(range[0]);
            const lowerBound = parseInt(range[1]);
            if (upperBound === Infinity) {
              if (aggregatedValue >= lowerBound) {
                maturity = `M${i + 1}`;
                break;
              }
            } else {
              if (aggregatedValue >= lowerBound && aggregatedValue < upperBound) {
                maturity = `M${i + 1}`;
                break;
              }
            }
          }
          break;
      }

      return maturity;
    }
  }

  findIncrementalOrDecrementalRange(range) {
    if (parseInt(range[1].split('-')[1] + 1) > parseInt(range[2].split('-')[1] + 1)) {
      return 'decremental';
    } else {
      return 'incremental';
    }
  }

  showTooltip(event, val, kpiId) {
    if (event) {
      const { top, left, width, height } = event.target.getBoundingClientRect();
      this.toolTipTop = top;
    } else {
      this.toolTipTop = 0;
    }
    if (val) {
      this.isTooltip = kpiId;
    } else {
      this.isTooltip = '';
    }
  }

  getMaturityData(kpiId) {
    let selectedKPI = this.allKpiArray.filter(kpi => kpi.kpiId === kpiId)[0];

    if (!this.maturityObj[kpiId]) {

      this.maturityObj[kpiId] = {
        maturityLevels: [],
      };

      let maturityRange = JSON.parse(JSON.stringify(selectedKPI.maturityRange));
      
      let maturityLevel = JSON.parse(JSON.stringify(selectedKPI.maturityLevel));
      let displayRange = maturityLevel.map((item) => item.displayRange);
      let findIncrementalOrDecrementalRange = this.findIncrementalOrDecrementalRange(maturityRange);
    
      if (findIncrementalOrDecrementalRange === 'decremental') {
        maturityRange = maturityRange.reverse();
      } else {
        maturityLevel = maturityLevel.reverse();
        displayRange = displayRange.reverse();
      }

      maturityLevel.forEach((element, index) => {
        this.maturityObj[kpiId]['maturityLevels'].push({
          level: element.level,
          range: displayRange[index],
          color: element.bgColor
        });
      });
    }
  }

}
