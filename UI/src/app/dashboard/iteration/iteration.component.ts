/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

/*********************************************
File contains Hygiene dashboard 's
scrum and kanban code .
@author rishabh
*******************************/

/** Importing Services **/
import { Component, OnInit, OnDestroy, Input, ViewChild } from '@angular/core';
import { HttpService } from '../../services/http.service';
import { ExcelService } from '../../services/excel.service';
import { SharedService } from '../../services/shared.service';
import { HelperService } from '../../services/helper.service';
import { CdkDragDrop } from '@angular/cdk/drag-drop';
import { ExportExcelComponent } from 'src/app/component/export-excel/export-excel.component';
import { Table } from 'primeng/table';
import { MessageService } from 'primeng/api';
import { FeatureFlagsService } from 'src/app/services/feature-toggle.service';
import { SortEvent } from 'primeng/api';
import iterationCommitment from '../../../assets/data/Iteration-committment-v2.json';
import wastage from '../../../assets/data/Wastage-V2.json';
import EstimateActual from '../../../assets/data/Estimate-Actual-V2.json';
import Workremaining from '../../../assets/data/Work-remaining-V2.json';

declare let require: any;

@Component({
  selector: 'app-iteration',
  templateUrl: './iteration.component.html',
  styleUrls: ['./iteration.component.css']
})
export class IterationComponent implements OnInit, OnDestroy {
  @ViewChild('exportExcel') exportExcelComponent: ExportExcelComponent;
  @ViewChild('table') tableComponent: Table;
  subscriptions: any[] = [];
  filterData = <any>[];
  filterApplyData = <any>{};
  noOfFilterSelected = 0;
  selectedtype = '';
  configGlobalData;
  kpiJira = <any>{};
  loaderJiraArray = [];
  jiraKpiRequest = <any>'';
  jiraKpiData = <any>{};
  testExecutionFilterData = <any>[];
  selectedTestExecutionFilterData;
  maturityColorCycleTime = <any>['#f5f5f5', '#f5f5f5', '#f5f5f5'];
  tooltip = <any>{};
  kanbanActivated = false;
  kpiConfigData: Object = {};
  noKpis = false;
  enableByUser = false;
  noSprints = false;
  kpiLoader = true;
  noTabAccess = false;
  allKpiArray: any = [];
  colorObj: object = {};
  kpiSelectedFilterObj = {};
  kpiChartData = {};
  updatedConfigGlobalData;
  upDatedConfigData;
  timeRemaining = 0;
  displayModal = false;
  modalDetails = {
    header: '',
    tableHeadings: [],
    tableValues: [],
    kpiId: ''
  };
  kpiDropdowns = {};
  trendBoxColorObj: any;
  chartColorList: Array<string> = ['#079FFF', '#00E6C3', '#CDBA38', '#FC6471', '#BD608C', '#7D5BA6'];
  noProjects = false;
  tableColumnData = {};
  tableColumnForm = {};
  excludeColumnFilter = [];
  excludeColumns = [];
  selectedColumns = [];
  tableColumns = [];
  tableHeaders = [];
  filteredColumn;
  markerInfo = [];
  globalConfig;
  sharedObject;
  activeIndex = 0;
  navigationTabs: Array<object> = [
    { 'label': 'Iteration Review', 'count': 0, width: 'half', kpis: [] },
    { 'label': 'Iteration Progress', 'count': 0, width: 'full', kpis: [] },
  ];
  forzenColumns = ['issue id', 'issue description'];
  commitmentReliabilityKpi;
  kpiCommentsCountObj: object = {};
  currentSelectedSprint;
  kpiThresholdObj = {};
  dailyStandupData: object = {};
  selectedProjectId: string;
  data =  [iterationCommitment,wastage];

  constructor(private service: SharedService, private httpService: HttpService, private excelService: ExcelService, private helperService: HelperService, private messageService: MessageService,
    private featureFlagService: FeatureFlagsService) {
     // console.log(this.data)
    this.subscriptions.push(this.service.passDataToDashboard.subscribe((sharedobject) => {
      if (sharedobject?.filterData?.length) {
        this.allKpiArray = [];
        this.kpiChartData = {};
        this.kpiSelectedFilterObj = {};
        this.kpiDropdowns = {};
        this.kpiCommentsCountObj = {};
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
      if (globalConfig && this.sharedObject) {
        this.configGlobalData = globalConfig['scrum'].filter((item) => item.boardName.toLowerCase() == 'iteration')[0]?.kpis;
        this.checkForAssigneeDataAndSetupTabs();
        this.processKpiConfigData();
      }
    }));

    this.subscriptions.push(this.service.noSprintsObs.subscribe((res) => {
      this.noSprints = res;
      this.service.iterationConfigData.next({});
      if (this.noSprints) {
        this.service.kpiListNewOrder.next([]);
      }
    }));

    this.subscriptions.push(this.service.noProjectsObs.subscribe((res) => {
      this.noProjects = res;
    }));
  }

  processKpiConfigData() {
    const disabledKpis = this.configGlobalData?.filter(item => item?.shown && !item?.isEnabled);
    // user can enable kpis from show/hide filter, added below flag to show different message to the user
    this.enableByUser = disabledKpis?.length ? true : false;
    // noKpis - if true, all kpis are not shown to the user (not showing kpis to the user)
    this.updatedConfigGlobalData = this.configGlobalData?.filter(item => item?.shown);
    this.commitmentReliabilityKpi = this.updatedConfigGlobalData.filter(kpi => kpi.kpiId === 'kpi120')[0];
    this.upDatedConfigData = this.updatedConfigGlobalData.filter(kpi => kpi.kpiId !== 'kpi121' && kpi.isEnabled);

    /**reset the kpi count */
    this.navigationTabs = this.navigationTabs.map((x) => {
      if (x['label'] === 'Daily Standup') {
        return x;
      }
      return { ...x, count: 0 };
    });

    this.upDatedConfigData?.forEach(data => {
      let board = data?.kpiDetail.kpiSubCategory;
      let idx = this.navigationTabs.findIndex(x => (x['label'] == board));
      if (idx !== -1) {
        this.navigationTabs[idx]['count']++;
        this.navigationTabs[idx]['kpis'].push(data);
      }
    });

    if (this.commitmentReliabilityKpi?.isEnabled) {
      this.navigationTabs[0]['count']++;
    }

    this.formatNavigationTabs();

    if (this.upDatedConfigData?.length === 0 && !this.commitmentReliabilityKpi?.isEnabled) {
      this.noKpis = true;
    } else {
      this.noKpis = false;
    }
    this.configGlobalData?.forEach(element => {
      if (element?.shown && element?.isEnabled) {
        this.kpiConfigData[element?.kpiId] = true;
      } else {
        this.kpiConfigData[element?.kpiId] = false;
      }
    });
  }

  formatNavigationTabs() {
    this.navigationTabs.forEach(tabDetails => {
      if (tabDetails['width'] === 'half') {
        let fullWidthKPis = [];
        let halfWithKpis = []
        tabDetails['kpis'].forEach(kpiDetails => {
          if (kpiDetails.kpiDetail.kpiWidth && kpiDetails.kpiDetail.kpiWidth === 100) {
            fullWidthKPis = fullWidthKPis.concat(kpiDetails);
          } else {
            halfWithKpis = halfWithKpis.concat(kpiDetails);
          }
        })
        const dataLength = halfWithKpis.length;
        const middleIndex = Math.floor(dataLength / 2);
        tabDetails['kpiPart1'] = halfWithKpis.slice(0, middleIndex + (dataLength % 2));
        tabDetails['kpiPart2'] = halfWithKpis.slice(middleIndex + (dataLength % 2));
        tabDetails['fullWidthKpis'] = fullWidthKPis;
      }
      return tabDetails;
    })
  }

  getSelectedType(sharedobject) {
    this.selectedtype = sharedobject;
  }

  /**
    Used to receive all filter data from filter component when user
    click apply and call kpi
   **/
  receiveSharedData($event) {
    if (this.service.getDashConfigData()) {
      this.activeIndex = 0;
      this.configGlobalData = this.service.getDashConfigData()['scrum']?.filter((item) => item.boardName.toLowerCase() == 'iteration')[0]?.kpis;
      this.processKpiConfigData();
      if (!$event.filterApplyData['ids'] || !$event.filterApplyData['ids']?.length || !$event.filterApplyData['ids'][0]) {
        this.noSprints = true;
      } else {
        this.noSprints = false;
        this.filterData = $event.filterData;
        this.filterApplyData = $event.filterApplyData;
        this.noOfFilterSelected = Object.keys(this.filterApplyData).length;
        if (this.filterData?.length) {
          this.noTabAccess = false;
          // call kpi request according to tab selected
          if (this.configGlobalData?.length > 0) {
            if (this.selectedtype !== 'Kanban') {
              // we should only call kpi154 on the click of Daily Standup tab
              let kpiIdsForCurrentBoard;
              switch (this.activeIndex) {
                case 2:
                  kpiIdsForCurrentBoard = this.configGlobalData?.map(kpiDetails => kpiDetails.kpiId).filter((kpiId) => kpiId === 'kpi154');
                  break;
                case 1:
                  kpiIdsForCurrentBoard = this.configGlobalData?.map(kpiDetails => kpiDetails.kpiId).filter((kpiId) => kpiId === 'kpi125');
                  break;
                default:
                  kpiIdsForCurrentBoard = this.configGlobalData?.map(kpiDetails => kpiDetails.kpiId).filter((kpiId) => kpiId !== 'kpi125' && kpiId !== 'kpi154');
                  break;
              }
              const selectedSprint = this.filterData?.filter(x => x.nodeId == this.filterApplyData?.selectedMap['sprint'][0])[0];
              if (selectedSprint) {
                this.selectedProjectId = selectedSprint.nodeId?.substring(selectedSprint.nodeId.lastIndexOf('_') + 1, selectedSprint.nodeId.length);
                this.checkForAssigneeDataAndSetupTabs();

                const today = new Date().toISOString().split('T')[0];
                const endDate = new Date(selectedSprint?.sprintEndDate).toISOString().split('T')[0];
                this.timeRemaining = this.calcBusinessDays(today, endDate);

                this.groupJiraKpi(kpiIdsForCurrentBoard);
                if ($event.filterApplyData['label'] !== 'sqd') {
                  this.getKpiCommentsCount();
                }
              }
            }
          }
        } else {
          this.noTabAccess = true;
        }
      }
    }
  }

  checkForAssigneeDataAndSetupTabs() {
    this.httpService.getProjectListData().subscribe(async responseList => {
      let selectedProject = responseList[0].data.filter((project) => project.id === this.selectedProjectId)[0];
      let showDSVorNot = false;
      if (selectedProject) {
        showDSVorNot = selectedProject['saveAssigneeDetails'];
      }
      let showDailyStandup = await this.featureFlagService.isFeatureEnabled('DAILY_STANDUP');
      if (this.service.currentSelectedSprint?.sprintState.toLowerCase() === 'active' && showDSVorNot) { // && showDailyStandup) {
        this.navigationTabs = [
          { 'label': 'Iteration Review', 'count': 0, width: 'half', kpis: [], fullWidthKpis: [] },
          { 'label': 'Iteration Progress', 'count': 0, width: 'full', kpis: [] },
          { 'label': 'Daily Standup', 'count': 1, width: 'full', kpis: [] }
        ];
      } else {
        this.navigationTabs = [
          { 'label': 'Iteration Review', 'count': 0, width: 'half', kpis: [], fullWidthKpis: [] },
          { 'label': 'Iteration Progress', 'count': 0, width: 'full', kpis: [] },
        ];
      }

      /**reset the kpi count */
      this.navigationTabs = this.navigationTabs.map((x) => {
        return { ...x, count: 0 };
      });
      for (let i = 0; i < this.upDatedConfigData?.length; i++) {
        let board = this.upDatedConfigData[i]?.subCategoryBoard;
        let idx = this.navigationTabs.findIndex(x => (x['label'] == board));
        if (idx != -1) {
          this.navigationTabs[idx]['count']++;
          this.navigationTabs[idx]['kpis'].push(this.upDatedConfigData[i]);
        }
      }
      if (this.commitmentReliabilityKpi?.isEnabled) {
        this.navigationTabs[0]['count']++;
      }

      this.formatNavigationTabs();

      if (this.navigationTabs.filter((tab) => tab['label'] === 'Daily Standup').length) {
        this.dailyStandupData = this.navigationTabs.filter((tab) => tab['label'] === 'Daily Standup')[0]['kpis'];
      }
    });

  }

  // Used for grouping all Sonar kpi from master data and calling Sonar kpi.(only for scrum).
  groupJiraKpi(kpiIdsForCurrentBoard) {
    this.jiraKpiData = {};
    // creating a set of unique group Ids
    const groupIdSet = new Set();
    this.updatedConfigGlobalData.forEach((obj) => {
      // we should only call kpi154 on the click of Daily Standup tab, there is separate code for sending kpi154 request
      if (!obj['kpiDetail'].kanban && obj['kpiDetail'].kpiSource === 'Jira' && obj['kpiDetail'].kpiCategory == 'Iteration' && obj.kpiId !== 'kpi154' && obj.kpiId !== 'kpi125') {
        groupIdSet.add(obj['kpiDetail'].groupId);
      }
    });
    // sending requests after grouping the the KPIs according to group Id
    groupIdSet.forEach((groupId) => {
      if (groupId) {
        this.kpiJira = this.helperService.groupKpiFromMaster('Jira', false, this.updatedConfigGlobalData, this.filterApplyData, this.filterData, kpiIdsForCurrentBoard, groupId, 'Iteration');
        this.postJiraKpi(this.kpiJira, 'jira');
      }
    });

  }


  // post request of Jira(scrum) hygiene
  postJiraKpi(postData, source): void {
    postData.kpiList.forEach(element => {
      this.loaderJiraArray.push(element.kpiId);
    });
    this.kpiLoader = true;
    this.jiraKpiRequest = this.httpService.postKpiNonTrend(postData, source)
      .subscribe(getData => {
        if (getData !== null && getData[0] !== 'error' && !getData['error']) {
          // getData = require('../../../test/resource/fakeIterationKpi.json');
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

  ngOnInit() {
    this.service.kpiListNewOrder.next([]);
    this.selectedtype = this.service.getSelectedType();

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
        this.service.setGlobalConfigData(filterData);
      }
    });

    this.service.getEmptyData().subscribe((val) => {
      if (val) {
        this.noTabAccess = true;
      } else {
        this.noTabAccess = false;
      }

    });
  }

  // unsubscribing all Kpi Request
  ngOnDestroy() {
    this.subscriptions.forEach(subscription => subscription.unsubscribe());
    this.sharedObject = null;
    this.globalConfig = null;
  }


  // download excel functionality
  downloadExcel(kpiId, kpiName, isKanban, additionalFilterSupport) {
    this.exportExcelComponent.downloadExcel(kpiId, kpiName, isKanban, additionalFilterSupport, this.filterApplyData, this.filterData, false);
  }

  sortAlphabetically(objArray) {
    if (objArray && objArray?.length > 1) {
      objArray?.sort((a, b) => a.data.localeCompare(b.data));
    }
    return objArray;
  }

  checkItemsSelected(obj) {
    let count = 0;
    if (obj && Object.keys(obj)?.length > 0) {
      Object.keys(obj)?.forEach(x => {
        count = count + obj[x]?.length;
      });
    }
    return count;
  }

  applyAggregationForChart(arr) {
    const aggregatedArr = JSON.parse(JSON.stringify(arr[0]));
    for (let i = 1; i < arr.length; i++) {
      for (let j = 0; j < arr[i].value.length; j++) {
        aggregatedArr.value[j].value += arr[i].value[j].value;

        aggregatedArr.value[j].hoverValue = { ...aggregatedArr.value[j].hoverValue, ...arr[i].value[j].hoverValue };
      }
    }
    return [aggregatedArr];
  }
  applyAggregationLogic(arr) {
    const aggregatedArr = [JSON.parse(JSON.stringify(arr[0]))];
    for (let i = 0; i < arr?.length; i++) {
      for (let j = 0; j < arr[i]?.data?.length; j++) {
        let idx = aggregatedArr[0].data?.findIndex(x => x.label == arr[i]?.data[j]?.label);
        if (idx == -1) {
          aggregatedArr[0]?.data?.push(arr[i]?.data[j]);
        }
      }
    }

    aggregatedArr[0].data = aggregatedArr[0]?.data?.map(item => ({
      ...item,
      value: 0,
      value1: item?.hasOwnProperty('value1') ? 0 : null,
      modalValues: item?.hasOwnProperty('modalValues') ? [] : null
    }));

    for (let i = 0; i < arr?.length; i++) {
      for (let j = 0; j < arr[i]?.data?.length; j++) {
        let idx = aggregatedArr[0].data?.findIndex(x => x.label == arr[i].data[j]['label']);

        if (idx != -1) {
          aggregatedArr[0].data[idx]['value'] += arr[i].data[j]['value'];
          if (aggregatedArr[0]?.data[idx]?.hasOwnProperty('value1') && aggregatedArr[0]?.data[idx]?.value1 != null) {
            aggregatedArr[0].data[idx]['value1'] += arr[i].data[j]['value1'];
          }
          if (aggregatedArr[0]?.data[idx]?.hasOwnProperty('modalValues') && aggregatedArr[0]?.data[idx]?.modalValues != null) {
            aggregatedArr[0].data[idx]['modalValues'] = [...aggregatedArr[0]?.data[idx]['modalValues'], ...arr[i]?.data[j]['modalValues']];
          }
        }
      }
    }

    aggregatedArr[0]?.data?.forEach((item) => {
      item['value'] = +(item['value']?.toFixed(2));
      if (item.value1) {
        item['value1'] = +(item['value1']?.toFixed(2));
      }
    });

    const evalvateExpression = aggregatedArr[0]['data'].filter(el => el.hasOwnProperty('expressions'));
    if (evalvateExpression.length > 0) {
      evalvateExpression.forEach(item => {
        this.evalvateExpression(item, aggregatedArr[0]['data'], arr);
      });
    }
    return aggregatedArr;
  }

  evalvateExpression(element, aggregatedArr, filteredArr) {

    const tempArr = [];
    const operandsArr = element['expressions'];

    operandsArr.forEach(op => {
      if (op === 'percentage') {
        const op2 = tempArr.pop();
        const op1 = tempArr.pop();
        tempArr.push(+((op1 / op2) * 100).toFixed(2));
      } else if (op === 'average') {
        const op2 = tempArr.pop();
        const op1 = tempArr.pop();
        tempArr.push(+(op1 / op2).toFixed(2));
      } else {
        const opValue = aggregatedArr.find(x => x.label === op)?.value;
        tempArr.push(opValue);
      }
    });

    element.value = tempArr[0];
  }

  getChartData(kpiId, idx, aggregationType?) {
    const trendValueList = this.allKpiArray[idx]?.trendValueList ? JSON.parse(JSON.stringify(this.allKpiArray[idx]?.trendValueList)) : {};
    /**if trendValueList is an object */
    if (trendValueList && Object.keys(trendValueList)?.length > 0 && !Array.isArray(trendValueList)) {
      if (this.kpiSelectedFilterObj[kpiId]?.hasOwnProperty('filter1')
        && this.kpiSelectedFilterObj[kpiId]?.hasOwnProperty('filter2')) {
        let tempArr = [];
        const preAggregatedValues = [];
        /** tempArr: array with combination of all items of filter1 and filter2 */
        tempArr = this.helperService.createCombinations(this.kpiSelectedFilterObj[kpiId]['filter1'], this.kpiSelectedFilterObj[kpiId]['filter2'])
        for (let i = 0; i < tempArr?.length; i++) {
          preAggregatedValues?.push(...trendValueList['value']?.filter(k => k['filter1'] == tempArr[i]?.filter1 && k['filter2'] == tempArr[i]?.filter2));
        }
        if (preAggregatedValues?.length > 1) {
          this.kpiChartData[kpiId] = this.applyAggregationLogic(preAggregatedValues);
        } else {
          this.kpiChartData[kpiId] = [...preAggregatedValues];
        }
      } else if ((this.kpiSelectedFilterObj[kpiId]?.hasOwnProperty('filter1'))
        || (this.kpiSelectedFilterObj[kpiId]?.hasOwnProperty('filter2'))) {
        const filters = this.kpiSelectedFilterObj[kpiId]['filter1'] || this.kpiSelectedFilterObj[kpiId]['filter2'];
        let preAggregatedValues = [];
        for (let i = 0; i < filters?.length; i++) {
          preAggregatedValues = [...preAggregatedValues, ...(trendValueList['value'] ? trendValueList['value'] : trendValueList)?.filter(x => x['filter1'] == filters[i] || x['filter2'] == filters[i])];
        }
        if (preAggregatedValues?.length > 1) {
          this.kpiChartData[kpiId] = this.applyAggregationLogic(preAggregatedValues);
        } else {
          this.kpiChartData[kpiId] = [...preAggregatedValues];
        }
      }
      else {
        /** when there are no kpi level filters */
        this.kpiChartData[kpiId] = [];
        if (trendValueList && trendValueList?.hasOwnProperty('value') && trendValueList['value']?.length > 0) {
          this.kpiChartData[kpiId]?.push(trendValueList['value']?.filter((x) => x['filter1'] == 'Overall')[0]);
        } else if (trendValueList?.length > 0) {
          this.kpiChartData[kpiId] = [...trendValueList];
        } else {
          const obj = JSON.parse(JSON.stringify(trendValueList));
          this.kpiChartData[kpiId]?.push(obj);
        }
      }
    }
    /**if trendValueList is an array */
    else if (trendValueList?.length > 0 && trendValueList[0]?.hasOwnProperty('filter1')) {
      if (this.kpiSelectedFilterObj[kpiId]?.hasOwnProperty('filter1')) {
        const filters = this.kpiSelectedFilterObj[kpiId]['filter1'];
        let preAggregatedValues = [];
        for (let i = 0; i < filters?.length; i++) {
          preAggregatedValues = [...preAggregatedValues, ...trendValueList?.filter(x => x['filter1'].toLowerCase() === filters[i].toLowerCase())];
        }
        if (preAggregatedValues[0]?.hasOwnProperty('value')) {
          this.kpiChartData[kpiId] = preAggregatedValues[0]?.value;
        } else {
          this.kpiChartData[kpiId] = [...preAggregatedValues];
        }
      } else {
        this.kpiChartData[kpiId] = trendValueList.filter(kpiData => kpiData.filter1 === 'Overall');
      }
    }
    else if (trendValueList?.length > 0) {
      this.kpiChartData[kpiId] = [...trendValueList];
    } else {
      this.kpiChartData[kpiId] = [];
    }
    if (kpiId === 'kpi121') {
      const iterationConfigData = {
        daysLeft: this.timeRemaining,
        capacity: {
          kpiInfo: this.updatedConfigGlobalData.find(kpi => kpi.kpiId === kpiId)?.kpiDetail?.kpiInfo,
          value: this.kpiChartData[kpiId][0]
        }
      };
      this.service.iterationConfigData.next(iterationConfigData);
    }
  }

  getKpiChartType(kpiId) {
    return this.updatedConfigGlobalData.filter(kpiDetails => kpiDetails.kpiId === kpiId)[0]?.kpiDetail?.chartType;
  }

  ifKpiExist(kpiId) {
    const id = this.allKpiArray?.findIndex((kpi) => kpi.kpiId == kpiId);
    return id;
  }

  createAllKpiArray(data) {
    for (const key in data) {
      const idx = this.ifKpiExist(data[key]?.kpiId);
      if (idx !== -1) {
        this.allKpiArray.splice(idx, 1);
      }
      this.allKpiArray.push(data[key]);
      const trendValueList = this.allKpiArray[this.allKpiArray?.length - 1]?.trendValueList;
      const filters = this.allKpiArray[this.allKpiArray?.length - 1]?.filters;
      if (trendValueList && Object.keys(trendValueList)?.length > 0 && !Array.isArray(trendValueList) && filters && Object.keys(filters)?.length > 0) {
        this.setFilterValueIfAlreadyHaveBackup(data[key]?.kpiId, {}, ['Overall'], filters)
      }
      else if (trendValueList?.length > 0 && trendValueList[0]?.hasOwnProperty('filter1')) {
        this.getDropdownArray(data[key]?.kpiId);
        const formType = this.updatedConfigGlobalData?.filter(x => x.kpiId == data[key]?.kpiId)[0]?.kpiDetail?.kpiFilter;
        if (formType?.toLowerCase() == 'radiobutton') {
          this.setFilterValueIfAlreadyHaveBackup(data[key]?.kpiId, {}, [this.kpiDropdowns[data[key]?.kpiId][0]?.options[0]])
        }
        else if (formType?.toLowerCase() == 'dropdown') {
          this.setFilterValueIfAlreadyHaveBackup(data[key]?.kpiId, {}, ['Overall'])
        }
        else if (filters && Object.keys(filters)?.length > 0) {
          this.setFilterValueIfAlreadyHaveBackup(data[key]?.kpiId, {}, ['Overall'], filters)
        } else {
          this.setFilterValueIfAlreadyHaveBackup(data[key]?.kpiId, {}, ['Overall'])
        }

      }
      this.getChartData(data[key]?.kpiId, (this.allKpiArray?.length - 1));
    }
  }

  setFilterValueIfAlreadyHaveBackup(kpiId, refreshValue, initialValue, filters?) {
    this.kpiSelectedFilterObj = this.helperService.setFilterValueIfAlreadyHaveBackup(kpiId, this.kpiSelectedFilterObj, 'iteration', refreshValue, initialValue, this.filterApplyData['ids'][0], filters)
    this.getDropdownArray(kpiId);
  }

  handleSelectedOption(event, kpi) {
    this.kpiSelectedFilterObj[kpi?.kpiId] = {};

    if (event && Object.keys(event)?.length !== 0 && typeof event === 'object') {

      for (const key in event) {
        if (event[key]?.length == 0) {
          delete event[key];
        }
      }
      this.kpiSelectedFilterObj[kpi?.kpiId] = event;
    } else {
      this.kpiSelectedFilterObj[kpi?.kpiId] = { "filter1": [event] };
    }
    this.getChartData(kpi?.kpiId, this.ifKpiExist(kpi?.kpiId));
    this.helperService.createBackupOfFiltersSelection(this.kpiSelectedFilterObj, 'iteration', this.filterApplyData['ids'][0]);
    this.service.setKpiSubFilterObj(this.kpiSelectedFilterObj);

  }

  calcBusinessDays(dDate1, dDate2) { // input given as Date objects
    let iWeeks; let iDateDiff; let iAdjust = 0;
    if (dDate2 < dDate1) {
      return 0;
    } // error code if dates transposed
    let iWeekday1 = new Date(dDate1).getDay(); // day of week
    let iWeekday2 = new Date(dDate2).getDay();
    iWeekday1 = (iWeekday1 == 0) ? 7 : iWeekday1; // change Sunday from 0 to 7
    iWeekday2 = (iWeekday2 == 0) ? 7 : iWeekday2;
    if ((iWeekday1 > 5) && (iWeekday2 > 5)) {
      iAdjust = 1;
    } // adjustment if both days on weekend
    iWeekday1 = (iWeekday1 > 5) ? 5 : iWeekday1; // only count weekdays
    iWeekday2 = (iWeekday2 > 5) ? 5 : iWeekday2;


    // calculate differnece in weeks (1000mS * 60sec * 60min * 24hrs * 7 days = 604800000)
    iWeeks = Math.floor((new Date(dDate2).getTime() - new Date(dDate1).getTime()) / 604800000);

    if (iWeekday1 <= iWeekday2) { //Equal to makes it reduce 5 days
      iDateDiff = (iWeeks * 5) + (iWeekday2 - iWeekday1);
    } else {
      iDateDiff = ((iWeeks + 1) * 5) - (iWeekday1 - iWeekday2);
    }

    iDateDiff -= iAdjust; // take into account both days on weekend
    return (iDateDiff + 1); // add 1 because dates are inclusive
  }

  convertToHoursIfTime(val, unit) {
    const isLessThanZero = val < 0;
    val = Math.abs(val);
    const hours = (val / 60);
    const rhours = Math.floor(hours);
    const minutes = (hours - rhours) * 60;
    const rminutes = Math.round(minutes);
    if (unit?.toLowerCase() === 'hours') {
      val = this.convertToHours(rminutes, rhours);
    } else if (unit?.toLowerCase() === 'day') {
      if (val !== 0) {
        val = this.convertToDays(rminutes, rhours);
      } else {
        val = '0d';
      }
    }
    if (isLessThanZero) {
      val = '-' + val;
    }
    return val;
  }

  convertToHours(rminutes, rhours) {
    if (rminutes === 0) {
      return rhours + 'h';
    } else if (rhours === 0) {
      return rminutes + 'm';
    } else {
      return rhours + 'h ' + rminutes + 'm';
    }
  }

  convertToDays(rminutes, rhours) {
    const days = rhours / 8;
    const rdays = Math.floor(days);
    rhours = (days - rdays) * 8;
    return `${(rdays !== 0) ? rdays + 'd ' : ''}${(rhours !== 0) ? rhours + 'h ' : ''}${(rminutes !== 0) ? rminutes + 'm' : ''}`;
  }

  handleArrowClick(kpi, label, tableValues) {
    const basicConfigId = this.service.selectedTrends[0].basicProjectConfigId;
    const idx = this.ifKpiExist(kpi?.kpiId);
    this.markerInfo = [];
    if (this.allKpiArray[idx]?.trendValueList?.value[0]?.markerInfo) {
      for (const key in this.allKpiArray[idx]?.trendValueList?.value[0]?.markerInfo) {
        this.markerInfo.push({ color: key, info: this.allKpiArray[idx]?.trendValueList?.value[0]?.markerInfo[key] });
      }
    }
    this.excludeColumns = this.allKpiArray[idx]?.trendValueList?.value[0]?.metaDataColumns ? this.allKpiArray[idx]?.trendValueList?.value[0]?.metaDataColumns : [];
    this.httpService.getkpiColumns(basicConfigId, kpi.kpiId).subscribe(response => {
      if (response['success']) {
        this.exportExcelComponent.dataTransformForIterationTableWidget(this.markerInfo,this.excludeColumns,response['data']['kpiColumnDetails'],tableValues,kpi?.kpiName + ' / ' + label,kpi.kpiId)
        
      }     
    });
  }

  transformDateToISO(tableValues) {
    return tableValues.map(item => {
      const formattedItem = { ...item };
      for (const key in formattedItem) {
        if (key.toLowerCase().includes('date') && formattedItem[key]) {
          formattedItem[key] = this.helperService.transformDateToISO(formattedItem[key]);
        }
      }
      return formattedItem
    });
  }

  generateTableColumnsFilterData(tableValue) {
    const unSelectedColumn = [];
    const defaultAndSelectedColumns = this.tableColumns.map(col => col.columnName);
    this.modalDetails['tableHeadings'] = [...new Set([...tableValue, ...defaultAndSelectedColumns])];
    this.modalDetails['tableHeadings'] = this.modalDetails['tableHeadings'].filter(colName => !this.excludeColumns.includes(colName));
    this.modalDetails['tableHeadings'].forEach(col => {
      if (!defaultAndSelectedColumns.includes(col)) {
        unSelectedColumn.push({
          columnName: col,
          isDefault: false,
          isShown: false,
          order: 0
        });
      }
    });

    this.tableColumns.push(...unSelectedColumn);
  }

  generateExcludeColumnsFilterList(tableValue) {
    this.excludeColumnFilter = ['Linked Defect', 'Defect Priority', 'Linked Stories'];
    for (const colunmName in tableValue) {
      if (typeof tableValue[colunmName] == 'object') {
        this.excludeColumnFilter.push(colunmName);
      }
    }
  }

  onFilterClick(columnName) {
    this.filteredColumn = columnName;
  }

  onFilterBlur(columnName) {
    this.filteredColumn = this.filteredColumn === columnName ? '' : this.filteredColumn;
  }

  applyColumnFilter() {
    this.saveKpiColumnsConfig(this.selectedColumns, 'APPLY');
  }

  saveTableColumnOrder() {
    if (this.tableComponent.columns.length > 0) {
      this.saveKpiColumnsConfig(this.tableComponent.columns, 'SAVE');
    }
  }

  saveKpiColumnsConfig(selectedColumns: any[], action: string) {
    const postData = {
      kpiId: '',
      basicProjectConfigId: '',
      kpiColumnDetails: []
    };
    postData.kpiId = this.modalDetails['kpiId'];
    postData['basicProjectConfigId'] = this.service.selectedTrends[0].basicProjectConfigId;
    postData['kpiColumnDetails'] = this.tableColumns.filter(col => {
      const selectedColIndex = selectedColumns.findIndex(colName => colName === col.columnName);
      if (selectedColIndex !== -1) {
        col.isShown = true;
        col.order = selectedColIndex;
        return true;
      } else {
        col.isShown = false;
        return false;
      }
    });
    postData['kpiColumnDetails'].sort((a, b) => a.order - b.order);
    this.tableHeaders = postData['kpiColumnDetails'].map(col => col.columnName);
    if (action === 'SAVE') {
      this.httpService.postkpiColumnsConfig(postData).subscribe(response => {
        if (response && response['success'] && response['data']) {
          this.messageService.add({ severity: 'success', summary: 'Kpi Column Configurations saved successfully!' });
        } else {
          this.messageService.add({ severity: 'error', summary: 'Error in Kpi Column Configurations. Please try after sometime!' });
        }
      });
    } else {
      this.messageService.add({ severity: 'success', summary: 'Kpi Column Configurations applied successfully!' });
    }

  }

  generateTableColumnData() {
    this.modalDetails['tableHeadings'].forEach(colName => {
      this.tableColumnData[colName] = [...new Set(this.modalDetails['tableValues'].map(item => item[colName]))].map(colData => ({ name: colData, value: colData }));
      this.tableColumnForm[colName] = [];
    });
  }

  generateExcel(exportMode) {
    const tableData = {
      columns: [],
      excelData: []
    };
    let excelData = [];
    let columns = [];
    if (exportMode === 'all') {
      excelData = this.modalDetails['tableValues'];
      columns = this.modalDetails['tableHeadings'];
    } else {
      excelData = this.tableComponent?.filteredValue || this.modalDetails['tableValues'];
      columns = this.tableHeaders;
    }

    columns.forEach(colHeader => {
      tableData.columns.push(colHeader);
    });

    excelData.forEach(colData => {
      let obj = {};
      for (let key in colData) {
        if (this.typeOf(colData[key])) {
          obj[key] = [];
          for (let y in colData[key]) {
            //added check if valid url
            if (colData[key][y].includes('http')) {
              obj[key].push({ text: y, hyperlink: colData[key][y] });
            } else {
              obj[key].push(colData[key][y]);
            }
          }
        } else if (key == 'Issue Id') {
          obj['Issue Id'] = {};
          obj['Issue Id'][colData[key]] = colData['Issue URL'];
        } else {
          obj[key] = colData[key]
        }
      }
      tableData.excelData.push(obj);
    });
    let kpiData = this.excelService.generateExcelModalData(tableData);
    this.excelService.generateExcel(kpiData, this.modalDetails['header']);
  }

  drop(event: CdkDragDrop<string[]>, updatedContainer) {
    const capacityKpi = this.updatedConfigGlobalData.find(kpi => kpi.kpiId === 'kpi121');
    this.helperService.drop(event, updatedContainer, this.navigationTabs, this.upDatedConfigData, this.configGlobalData, capacityKpi);
  }

  typeOf(value) {
    return typeof value === 'object' && value !== null;
  }

  /** Reload KPI once field mappoing updated */
  reloadKPI(event) {
    this.kpiChartData[event.kpiDetail?.kpiId] = [];
    const currentKPIGroup = this.helperService.groupKpiFromMaster('Jira', false, this.updatedConfigGlobalData, this.filterApplyData, this.filterData, {}, event?.kpiDetail?.groupId, 'Iteration');
    if (currentKPIGroup?.kpiList?.length > 0) {
      this.postJiraKpi(currentKPIGroup, 'jira');
    }
  }

  async getKpiCommentsCount(kpiId?) {
    const nodes = this.filterData.filter(x => x.nodeId == this.filterApplyData?.ids[0])[0]?.parentId;
    const level = this.filterApplyData?.level;
    const nodeChildId = this.filterApplyData['selectedMap']?.sprint[0];
    this.kpiCommentsCountObj = await this.helperService.getKpiCommentsCount(this.kpiCommentsCountObj, nodes, level, nodeChildId, this.updatedConfigGlobalData, kpiId)
  }

  handleTabChange(e) {
    let index = e.index;
    if (index === 1) {
      let kpi125Data = this.configGlobalData?.filter(kpi => kpi.kpiId === 'kpi125')[0];
      this.kpiJira = this.helperService.groupKpiFromMaster('Jira', false, this.updatedConfigGlobalData, this.filterApplyData, this.filterData, ['kpi125'], kpi125Data['groupId'], 'Iteration');
      this.postJiraKpi(this.kpiJira, 'jira');
    }
    if (index === 2) {
      let kpi154Data = this.configGlobalData?.filter(kpi => kpi.kpiId === 'kpi154')[0];
      this.kpiJira = this.helperService.groupKpiFromMaster('Jira', false, this.updatedConfigGlobalData, this.filterApplyData, this.filterData, ['kpi154'], kpi154Data['groupId'], 'Iteration');
      this.postJiraKpi(this.kpiJira, 'jira');
    }
  }

  // Removing this code as this is not giving expected result.
  // customSort(event: SortEvent) {
  //   const getSortValue = (data: any, field: string) => {
  //     const value = (field in data) ? data[field].trim() : '-';
  //     return (field.includes('Date') && value !== '-') ? new Date(value).toISOString().split('T')[0] : value;
  //   };

  //   event.data.sort((data1, data2) => {
  //     const value1 = getSortValue(data1, event.field);
  //     const value2 = getSortValue(data2, event.field);
  //     const result = (value1 === '-' && value2 === '-') ? 0 : value1.localeCompare(value2);
  //     return event.order * result;
  //   });
  // }

  getDropdownArray1(kpiId) {
    const idx = this.ifKpiExist(kpiId);
    let filters = {};
    const dropdownArr = [];
    let trendValueList = this.allKpiArray[idx]?.trendValueList;
    if (idx != -1) {
      filters = this.allKpiArray[idx]?.filters;
      if (filters && Object.keys(filters).length !== 0) {
        Object.keys(filters)?.forEach(x => {
          dropdownArr.push(filters[x]);
        });
        this.kpiDropdowns[kpiId] = [...dropdownArr];
      }
      else if (trendValueList?.length > 0 && trendValueList[0]?.hasOwnProperty('filter1')) {
        const obj = {};
        for (let i = 0; i < trendValueList?.length; i++) {
          let ifExist = dropdownArr.findIndex(x => x == trendValueList[i]?.filter1);
          if (ifExist == -1) {
            dropdownArr?.push(trendValueList[i]?.filter1);
          }
        }
        const kpiObj = this.updatedConfigGlobalData?.filter(x => x['kpiId'] == kpiId)[0];
        if (kpiObj && kpiObj['kpiDetail']?.hasOwnProperty('kpiFilter') && (kpiObj['kpiDetail']['kpiFilter']?.toLowerCase() == 'multiselectdropdown' || (kpiObj['kpiDetail']['kpiFilter']?.toLowerCase() == 'dropdown' && kpiObj['kpiDetail'].hasOwnProperty('hideOverallFilter') && kpiObj['kpiDetail']['hideOverallFilter']))) {
          const index = dropdownArr?.findIndex(x => x?.toLowerCase() == 'overall');
          if (index > -1) {
            dropdownArr?.splice(index, 1);
          }
        }
        obj['filterType'] = 'Select a filter';
        obj['options'] = dropdownArr;
        this.kpiDropdowns[kpiId] = [];
        this.kpiDropdowns[kpiId].push(obj);
      }
    }

  }

  //#region sonar fixing here as this component is no longer in use
  getDropdownArray(kpiId) {
    const idx = this.ifKpiExist(kpiId);
    if (idx === -1) return;
  
    const filters = this.getFilters(idx);
    const trendValueList = this.allKpiArray[idx]?.trendValueList;
    const dropdownArr = [];
  
    if (filters && Object.keys(filters).length !== 0) {
      this.populateDropdownFromFilters(filters, dropdownArr, kpiId);
    } else if (this.service.isTrendValueListValid(trendValueList)) {
      this.service.populateDropdownFromTrendValues(trendValueList, dropdownArr);
      this.applyKpiFilterConfig(kpiId, dropdownArr);
    }
  }

   getFilters(idx: number): any {
    return this.allKpiArray[idx]?.filters || {};
  }
  
   populateDropdownFromFilters(filters: any, dropdownArr: any[], kpiId: string): void {
    Object.keys(filters).forEach(x => {
      dropdownArr.push(filters[x]);
    });
    this.kpiDropdowns[kpiId] = [...dropdownArr];
  }

   applyKpiFilterConfig(kpiId: string, dropdownArr: any[]): void {
    const kpiObj = this.updatedConfigGlobalData?.find(x => x['kpiId'] === kpiId);
    if (this.service.shouldRemoveOverallFilter(kpiObj)) {
      this.service.removeOverallFilter(dropdownArr);
    }
    this.kpiDropdowns[kpiId] = this.service.createFilterObject(dropdownArr);
  }

  //#endregion
}
