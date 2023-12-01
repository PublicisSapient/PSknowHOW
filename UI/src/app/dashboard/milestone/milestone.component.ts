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


/** Importing Services **/
import { Component, OnInit, OnDestroy, Input, ViewChild } from '@angular/core';
import { HttpService } from '../../services/http.service';
import { ExcelService } from '../../services/excel.service';
import { SharedService } from '../../services/shared.service';
import { HelperService } from '../../services/helper.service';
import { CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';
import { ExportExcelComponent } from 'src/app/component/export-excel/export-excel.component';
import { TableService } from 'primeng/table';

declare let require: any;

@Component({
  selector: 'app-milestone',
  templateUrl: './milestone.component.html',
  styleUrls: ['./milestone.component.css']
})
export class MilestoneComponent implements OnInit {
  @ViewChild('exportExcel') exportExcelComponent: ExportExcelComponent;
  subscriptions: any[] = [];
  masterData = <any>{};
  filterData = <any>[];
  filterApplyData = <any>{};
  noOfFilterSelected = 0;
  selectedtype = '';
  configGlobalData;
  kpiJira = <any>{};
  loaderJiraArray = [];
  jiraKpiRequest = <any>'';
  jiraKpiData = <any>{};
  kpiConfigData: Object = {};
  noKpis = false;
  enableByUser = false;
  noRelease = false;
  colorObj: object = {};
  updatedConfigGlobalData;
  upDatedConfigData;
  noProjects = false;
  timeRemaining = 0;
  trendBoxColorObj: any;
  kpiChartData = {};
  noTabAccess = false;
  kpiSelectedFilterObj = {};
  allKpiArray: any = [];
  kpiDropdowns = {};
  kpiLoader = true;
  globalConfig;
  sharedObject;
  kpiCommentsCountObj: object = {};
  navigationTabs:Array<object>;
  activeIndex = 0;
  kpiThresholdObj = {};
  constructor(private service: SharedService, private httpService: HttpService, private excelService: ExcelService, private helperService: HelperService) {

    /** When filter dropdown change */
    this.subscriptions.push(this.service.passDataToDashboard.subscribe((sharedobject) => {
      if (sharedobject?.filterData?.length && sharedobject.selectedTab.toLowerCase() === 'release') {
        this.allKpiArray = [];
        this.kpiChartData = {};
        this.kpiSelectedFilterObj = {};
        this.kpiDropdowns = {};
        this.kpiCommentsCountObj = {};
        this.sharedObject = sharedobject;
        if (this.globalConfig || this.service.getDashConfigData()) {
          this.receiveSharedData(sharedobject);
        }
      }
    }));

    /** When release not found for any project */
    this.subscriptions.push(this.service.noReleaseObs.subscribe((res) => {
      this.noRelease = res;
      if (this.noRelease) {
        this.service.kpiListNewOrder.next([]);
      }
    }));

    /** When click on show/Hide button on filter component */
    this.subscriptions.push(this.service.globalDashConfigData.subscribe((globalConfig) => {
      if (globalConfig) {
        // if (this.sharedObject || this.service.getFilterObject()) {
        //   this.receiveSharedData(this.service.getFilterObject());
        // }
        this.configGlobalData = globalConfig['others'].filter((item) => item.boardName.toLowerCase() == 'release')[0]?.kpis;
        this.processKpiConfigData();
      }
    }));

    this.subscriptions.push(this.service.noProjectsObs.subscribe((res) => {
      this.noProjects = res;
    }));
  }

  processKpiConfigData() {
    this.navigationTabs = [
      {'label':'Speed', 'count': 0,kpis : [],width : 'half', fullWidthKpis : []},
      {'label':'Quality', 'count': 0,kpis : [],width :'half'},
      {'label':'Value', 'count': 0,kpis : [],width :'full'},
    ];
    const disabledKpis = this.configGlobalData.filter(item => item.shown && !item.isEnabled);
     /** user can enable kpis from show/hide filter, added below flag to show different message to the user **/
    this.enableByUser = disabledKpis?.length ? true : false;
    /** noKpis - if true, all kpis are not shown to the user (not showing kpis to the user) **/
    this.updatedConfigGlobalData = this.configGlobalData.filter(item => item.shown && item.isEnabled);
    this.upDatedConfigData = this.updatedConfigGlobalData.filter(kpi => kpi.kpiId !== 'kpi121');

    for(let i = 0; i<this.upDatedConfigData?.length; i++){
      let board = this.upDatedConfigData[i]?.kpiDetail.kpiSubCategory;
      let idx = this.navigationTabs?.findIndex(x => (x['label'] == board));
      if(idx != -1) {
        this.navigationTabs[idx]['count']++;
        this.navigationTabs[idx]['kpis'].push(this.upDatedConfigData[i]);
      }
    }

    this.navigationTabs.map(tabDetails => {
      if(tabDetails['width'] === 'half'){
        let fullWidthKPis = [];
        let halfWithKpis = []
        tabDetails['kpis'].forEach(kpiDetails=>{
          if(kpiDetails.kpiDetail.kpiWidth && kpiDetails.kpiDetail.kpiWidth === 100){
            fullWidthKPis = fullWidthKPis.concat(kpiDetails);
          }else{
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
    });

    if (this.upDatedConfigData?.length === 0) {
      this.noKpis = true;
    } else {
      this.noKpis = false;
    }
    this.configGlobalData.forEach(element => {
      if (element.shown && element.isEnabled) {
        this.kpiConfigData[element.kpiId] = true;
      } else {
        this.kpiConfigData[element.kpiId] = false;
      }
    });
  }

  getSelectedType(sharedobject) {
    this.selectedtype = sharedobject;
  }

  /**
    Used to receive all filter data from filter component when user
    click apply and call kpi
   **/
  receiveSharedData($event) {
    if(this.service.getDashConfigData()){
      this.configGlobalData = this.service.getDashConfigData()['others']?.filter((item) => item.boardName.toLowerCase() == 'release')[0]?.kpis;
      this.processKpiConfigData();
      this.masterData = $event.masterData;
      this.filterData = $event.filterData;
      this.filterApplyData = $event.filterApplyData;
      this.noOfFilterSelected = Object.keys(this.filterApplyData).length;
      if (this.filterData?.length) {
        /**  call kpi request according to tab selected */
        if (this.masterData && Object.keys(this.masterData).length) {
          if (this.selectedtype !== 'Kanban') {
            const kpiIdsForCurrentBoard = this.configGlobalData?.map(kpiDetails => kpiDetails.kpiId);
            const selectedRelease = this.filterData?.filter(x => x.nodeId == this.filterApplyData?.selectedMap['release'][0] && x.labelName.toLowerCase() === 'release')[0];
            const today = new Date().toISOString().split('T')[0];
            const endDate = new Date(selectedRelease?.releaseEndDate).toISOString().split('T')[0];
            this.timeRemaining = this.calcBusinessDays(today, endDate);
            this.service.iterationCongifData.next({ daysLeft: this.timeRemaining });
            this.groupJiraKpi(kpiIdsForCurrentBoard);
            this.getKpiCommentsCount();
          }
        }
      }
    }

  }

  /**  Used for grouping all Sonar kpi from master data and calling Sonar kpi.(only for scrum) */
  groupJiraKpi(kpiIdsForCurrentBoard) {
    this.jiraKpiData = {};
    /** creating a set of unique group Ids */
    const groupIdSet = new Set();
    this.masterData.kpiList.forEach((obj) => {
      if (!obj.kanban && obj.kpiSource === 'Jira' && obj.kpiCategory == 'Release') {
        groupIdSet.add(obj.groupId);
      }
    });

    /** sending requests after grouping the the KPIs according to group Id */
    groupIdSet.forEach((groupId) => {
      if (groupId) {
        this.kpiJira = this.helperService.groupKpiFromMaster('Jira', false, this.masterData, this.filterApplyData, this.filterData, kpiIdsForCurrentBoard, groupId, 'Release');
        this.postJiraKpi(this.kpiJira, 'jira');
      }
    });

  }

  // download excel functionality
  downloadExcel(kpiId, kpiName, isKanban, additionalFilterSupport) {
    this.exportExcelComponent.downloadExcel(kpiId, kpiName, isKanban, additionalFilterSupport, this.filterApplyData, this.filterData, false);
  }

  handleSelectedOption(event, kpi) {
    this.kpiSelectedFilterObj[kpi?.kpiId] = {};
    this.kpiSelectedFilterObj['action'] = 'update';
    if (event && Object.keys(event)?.length !== 0 && typeof event === 'object') {

      for (const key in event) {
        if (event[key]?.length == 0) {
          delete event[key];
        }
      }
      this.kpiSelectedFilterObj[kpi?.kpiId] = event;
    } else {
      // this.kpiSelectedFilterObj[kpi?.kpiId].push(event);
      this.kpiSelectedFilterObj[kpi?.kpiId] = { "filter1": [event] };
    }
    this.getChartData(kpi?.kpiId, this.ifKpiExist(kpi?.kpiId));

    this.service.setKpiSubFilterObj(this.kpiSelectedFilterObj);

  }


  /** post request of Jira(scrum) hygiene  */
  postJiraKpi(postData, source): void {
    postData.kpiList.forEach(element => {
      this.loaderJiraArray.push(element.kpiId);
    });
    this.kpiLoader = true;
    this.jiraKpiRequest = this.httpService.postKpi(postData, source)
      .subscribe(getData => {
        if (getData !== null && getData[0] !== 'error' && !getData['error']) {
          /** creating array into object where key is kpi id */
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

    this.service.getEmptyData().subscribe((val) => {
      if (val) {
        this.noTabAccess = true;
      } else {
        this.noTabAccess = false;
      }

    });
  }

  ifKpiExist(kpiId) {
    const id = this.allKpiArray?.findIndex((kpi) => kpi.kpiId == kpiId);
    return id;
  }
  createAllKpiArray(data) {
    this.kpiSelectedFilterObj['action'] = 'new';
    for (const key in data) {
      const idx = this.ifKpiExist(data[key]?.kpiId);
      if (idx !== -1) {
        this.allKpiArray.splice(idx, 1);
      }
      this.allKpiArray.push(data[key]);
      const trendValueList = this.allKpiArray[this.allKpiArray?.length - 1]?.trendValueList;
      const filters = this.allKpiArray[this.allKpiArray?.length - 1]?.filters;
      if (trendValueList && Object.keys(trendValueList)?.length > 0 && !Array.isArray(trendValueList) && filters && Object.keys(filters)?.length > 0) {
        this.kpiSelectedFilterObj[data[key]?.kpiId] = {};
        const tempObj = {};
        for (const key in filters) {
          tempObj[key] = ['Overall'];
        }
        this.kpiSelectedFilterObj[data[key]?.kpiId] = { ...tempObj };
        this.service.setKpiSubFilterObj(this.kpiSelectedFilterObj);
        this.getDropdownArray(data[key]?.kpiId);
      }
      else if (trendValueList?.length > 0 && trendValueList[0]?.hasOwnProperty('filter1')) {
        // this.kpiSelectedFilterObj[data[key]?.kpiId] = [];
        this.kpiSelectedFilterObj[data[key]?.kpiId] = {};
        this.getDropdownArray(data[key]?.kpiId);
        const formType = this.updatedConfigGlobalData?.filter(x => x.kpiId == data[key]?.kpiId)[0]?.kpiDetail?.kpiFilter;
        if (formType?.toLowerCase() == 'radiobutton') {
          // this.kpiSelectedFilterObj[data[key]?.kpiId]?.push(this.kpiDropdowns[data[key]?.kpiId][0]?.options[0]);
          this.kpiSelectedFilterObj[data[key]?.kpiId] = { 'filter1': [this.kpiDropdowns[data[key]?.kpiId][0]?.options[0]] };
        }
        else if (formType?.toLowerCase() == 'dropdown') {
          this.kpiSelectedFilterObj[data[key]?.kpiId] = {};
          this.kpiSelectedFilterObj[data[key]?.kpiId] = { 'filter1': ['Overall'] };
        }
        else if (filters && Object.keys(filters)?.length > 0) {
          this.kpiSelectedFilterObj[data[key]?.kpiId] = {};
          const tempObj = {};
          for (const key in filters) {
            tempObj[key] = ['Overall'];
          }
          this.kpiSelectedFilterObj[data[key]?.kpiId] = { ...tempObj };
        } else {
          // this.kpiSelectedFilterObj[data[key]?.kpiId]?.push('Overall');
          this.kpiSelectedFilterObj[data[key]?.kpiId] = { 'filter1': ['Overall'] };
        }
        this.service.setKpiSubFilterObj(this.kpiSelectedFilterObj);
      } else if (!trendValueList || trendValueList?.length == 0) {
        this.getDropdownArray(data[key]?.kpiId);
      }
      this.getChartData(data[key]?.kpiId, (this.allKpiArray?.length - 1));
    }
  }

  createCombinations(arr1, arr2) {
    let arr = [];
    for (let i = 0; i < arr1?.length; i++) {
      for (let j = 0; j < arr2?.length; j++) {
        arr.push({ filter1: arr1[i], filter2: arr2[j] });
      }
    }
    return arr;
  }

  getChartData(kpiId, idx, aggregationType?) {
    const trendValueList = this.allKpiArray[idx]?.trendValueList ? JSON.parse(JSON.stringify(this.allKpiArray[idx]?.trendValueList)) : {};
    this.kpiThresholdObj[kpiId] = this.allKpiArray[idx]?.thresholdValue ? this.allKpiArray[idx]?.thresholdValue : null;
    /**if trendValueList is an object */
    if (trendValueList && Object.keys(trendValueList)?.length > 0 && !Array.isArray(trendValueList)) {
      if (this.kpiSelectedFilterObj[kpiId]?.hasOwnProperty('filter1')
        && this.kpiSelectedFilterObj[kpiId]?.hasOwnProperty('filter2')) {
        let tempArr = [];
        const preAggregatedValues = [];
        /** tempArr: array with combination of all items of filter1 and filter2 */
        tempArr = this.createCombinations(this.kpiSelectedFilterObj[kpiId]['filter1'], this.kpiSelectedFilterObj[kpiId]['filter2'])
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
      if (this.kpiSelectedFilterObj[kpiId]?.hasOwnProperty('filter1') && this.kpiSelectedFilterObj[kpiId]?.hasOwnProperty('filter2')) {
        let tempArr = [];
        const preAggregatedValues = [];
        /** tempArr: array with combination of all items of filter1 and filter2 */
        tempArr = this.createCombinations(this.kpiSelectedFilterObj[kpiId]['filter1'], this.kpiSelectedFilterObj[kpiId]['filter2'])
        for (let i = 0; i < tempArr?.length; i++) {
          preAggregatedValues?.push(...trendValueList?.filter(k => k['filter1'] == tempArr[i]?.filter1 && k['filter2'] == tempArr[i]?.filter2));
        }
        if (preAggregatedValues?.length > 1) {
          if (this.getKpiChartType(kpiId) === 'GroupBarChart' || this.getKpiChartType(kpiId) === 'horizontalPercentBarChart') {
            this.kpiChartData[kpiId] = this.applyAggregationForChart(preAggregatedValues);
          } else {
            this.kpiChartData[kpiId] = this.applyAggregationLogic(preAggregatedValues);
          }
        } else {
          this.kpiChartData[kpiId] = [...preAggregatedValues];
        }
      } else if (this.kpiSelectedFilterObj[kpiId]?.hasOwnProperty('filter1') || this.kpiSelectedFilterObj[kpiId]?.hasOwnProperty('filter2')) {
        let filters = this.kpiSelectedFilterObj[kpiId]['filter1'] || this.kpiSelectedFilterObj[kpiId]['filter2'];
        let preAggregatedValues = [];
        // for single select dropdown filters
        if(!Array.isArray(filters)) {
          filters = [filters];
        }
        for (let i = 0; i < filters?.length; i++) {
          preAggregatedValues = [...preAggregatedValues, ...trendValueList?.filter(x => x['filter1'] == filters[i] || x['filter2'] == filters[i])];
        }
        if (preAggregatedValues?.length > 1) {
          if (this.getKpiChartType(kpiId) === 'GroupBarChart' || this.getKpiChartType(kpiId) === 'horizontalPercentBarChart') {
            this.kpiChartData[kpiId] = this.applyAggregationForChart(preAggregatedValues);
          } else {
            this.kpiChartData[kpiId] = this.applyAggregationLogic(preAggregatedValues);
          }
        } else {
          if(preAggregatedValues[0]?.hasOwnProperty('value')){
            this.kpiChartData[kpiId] = preAggregatedValues[0]?.value;
          }else{
            this.kpiChartData[kpiId] = [...preAggregatedValues];
          }
        }
      } else {
        this.kpiChartData[kpiId] = trendValueList.filter(kpiData => kpiData.filter1 === 'Overall');
      }
    }
    else if (trendValueList?.length > 0) {
      this.kpiChartData[kpiId] = [...trendValueList[0]?.value];
    } else {
      this.kpiChartData[kpiId] = [];
    }
    // if (Object.keys(this.kpiChartData)?.length === this.updatedConfigGlobalData?.length) {
    //   this.helperService.calculateGrossMaturity(this.kpiChartData, this.updatedConfigGlobalData);
    // }
  }

  getKpiChartType(kpiId) {
    return this.updatedConfigGlobalData.filter(kpiDetails => kpiDetails.kpiId === kpiId)[0]?.kpiDetail?.chartType;
  }

  applyAggregationForChart(arr) {
    const aggregatedArr = JSON.parse(JSON.stringify(arr[0]));
    for (let i = 1; i < arr.length; i++) {
      for (let j = 0; j < arr[i].value.length; j++) {
        if (typeof aggregatedArr.value[j].value === 'number') {
          aggregatedArr.value[j].value += arr[i].value[j].value;
          aggregatedArr.value[j].hoverValue = { ...aggregatedArr.value[j].hoverValue, ...arr[i].value[j].hoverValue };
        }
        if (typeof aggregatedArr.value[j].value === 'object') {
          if (!Array.isArray(aggregatedArr.value[j].value)) {
            for (const key in aggregatedArr.value[j].value) {
              aggregatedArr.value[j].value[key] += arr[i].value[j].value[key];
            }
          } else {
            // kpi147
            for (const key in aggregatedArr.value[j].value) {
              Object.assign(aggregatedArr.value[j].value[key], arr[i].value[j].value[key]);
            }
          }
        }
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

  getDropdownArray(kpiId) {
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
      } else if (!trendValueList || trendValueList?.length == 0) {
        this.kpiDropdowns[kpiId] = [];
      }
    }
  }

  drop(event: CdkDragDrop<string[]>,tab) {
    if (event?.previousIndex !== event.currentIndex) {
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
      if(tab.width === 'half'){
        const updatedTabsDetails = this.navigationTabs.find(tabs=>tabs['label'].toLowerCase() === tab['label'].toLowerCase());
        updatedTabsDetails['kpis'] = [...updatedTabsDetails['kpiPart1'],...updatedTabsDetails['kpiPart2'],...updatedTabsDetails['fullWidthKpis']];
      }
      this.upDatedConfigData = [];
      this.navigationTabs.forEach(tabs=>{
        this.upDatedConfigData  = this.upDatedConfigData.concat(tabs['kpis']);
      })
      this.upDatedConfigData.map((kpi, index) => kpi.order = index + 3);
      const disabledKpis = this.configGlobalData.filter(item => item.shown && !item.isEnabled);
      disabledKpis.map((kpi, index) => kpi.order = this.upDatedConfigData.length + index + 3);
      const hiddenkpis = this.configGlobalData.filter(item => !item.shown);
      hiddenkpis.map((kpi, index) => kpi.order = this.upDatedConfigData.length + disabledKpis.length + index + 3);
      this.service.kpiListNewOrder.next([...this.upDatedConfigData, ...disabledKpis, ...hiddenkpis]);
    }
  }

  getKpiCommentsCount(kpiId?) {
    let requestObj = {
      "nodes": this.filterData.filter(x => x.nodeId == this.filterApplyData?.ids[0])[0]?.parentId,
      "level": this.filterApplyData?.level,
      "nodeChildId": this.filterApplyData['selectedMap']?.release[0],
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

   /** Reload KPI once field mappoing updated */
   reloadKPI(event){
    this.kpiChartData[event.kpiDetail?.kpiId] = [];
    const currentKPIGroup = this.helperService.groupKpiFromMaster('Jira', false, this.masterData, this.filterApplyData, this.filterData, {}, event.kpiDetail?.groupId,'Release');
    if (currentKPIGroup?.kpiList?.length > 0) {
        this.postJiraKpi(this.kpiJira, 'jira');
    }
  }

  handleTabChange(event){
    this.activeIndex = event.index;
  }

  checkIfDataPresent(data) {
    let dataCount = 0;
    if(data[0] && !isNaN(parseInt(data[0].data))) {
      dataCount = data[0].data;
    } else if(data[0] && data[0].value && !isNaN(parseInt(data[0].value[0].data))) {
      dataCount = data[0].value[0].data;
    }
    if(parseInt(dataCount + '') > 0) {
      return true;
    }
    return false;
  }

  /** unsubscribing all Kpi Request  */
  ngOnDestroy() {
    this.subscriptions.forEach(subscription => subscription.unsubscribe());
    this.sharedObject = null;
    this.globalConfig = null;
  }
}
