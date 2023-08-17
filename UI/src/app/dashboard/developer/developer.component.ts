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
File contains Developer dashboard 's
scrum and kanban code .
@author rishabh
*******************************/

/** Importing Services **/
import { Component, OnInit, OnDestroy, Input, ViewChild } from '@angular/core';
import { HttpService } from '../../services/http.service';
import { ExcelService } from '../../services/excel.service';
import { SharedService } from '../../services/shared.service';
import { HelperService } from '../../services/helper.service';
import { CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';
import { ExportExcelComponent } from 'src/app/component/export-excel/export-excel.component';
import { Table } from 'primeng/table';
import { MessageService } from 'primeng/api';
import { distinctUntilChanged } from 'rxjs/operators';

@Component({
  selector: 'app-developer',
  templateUrl: './developer.component.html',
  styleUrls: ['./developer.component.css']
})
export class DeveloperComponent implements OnInit {
  selectedTab = 'developer';
  subscriptions: any[] = [];
  masterData = <any>{};
  filterData = <any>[];
  filterApplyData = <any>{};
  noOfFilterSelected = 0;
  selectedtype = '';
  configGlobalData;
  allKpiArray: any = [];
  colorObj: object = {};
  kpiSelectedFilterObj = {};
  kpiChartData = {};
  updatedConfigGlobalData;
  upDatedConfigData;
  kpiDropdowns = {};
  globalConfig;
  sharedObject;
  kpiCommentsCountObj: object = {};
  noTabAccess = false;
  noSprints = false;
  noProjects = false;
  noKpis = false;
  enableByUser = false;
  tooltip = <any>{};
  trendBoxColorObj: any;
  kpiConfigData: Object = {};
  sprintsOverlayVisible: boolean = false;
  hierarchyLevel;
  kanbanActivated = false;
  serviceObject = {};
  chartColorList = {};
  kpiTrendsObj = {};
  kpiLoader = true;
  selectedJobFilter = 'Select';
  showKpiTrendIndicator = {};
  showCommentIcon = false;
  kpiBitBucket;
  loaderBitBucket = false;
  bitBucketKpiRequest;
  bitBucketKpiData = {};
  enableByeUser: boolean;
  constructor(private service: SharedService, private httpService: HttpService, private excelService: ExcelService, private helperService: HelperService, private messageService: MessageService) {

    this.subscriptions.push(this.service.passDataToDashboard.subscribe((sharedobject) => {
      if (sharedobject?.filterData?.length && sharedobject.selectedTab.toLowerCase() === 'developer') {
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

    /** When click on show/Hide button on filter component */
    this.subscriptions.push(this.service.globalDashConfigData.subscribe((globalConfig) => {
      if (globalConfig) {
        if (this.sharedObject || this.service.getFilterObject()) {
          this.receiveSharedData(this.service.getFilterObject());
        }
        this.configGlobalData = globalConfig['others'].filter((item) => item.boardName.toLowerCase() == 'developer')[0]?.kpis;
        this.processKpiConfigData();
      }
    }));

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

  }

  ngOnInit(): void {
    this.selectedtype = this.service.getSelectedType();

    this.httpService.getTooltipData()
      .subscribe(filterData => {
        if (filterData[0] !== 'error') {
          this.tooltip = filterData;
        }
      });

    this.service.getEmptyData().subscribe((val) => {
      if (val) {
        this.noTabAccess = true;
      } else {
        this.noTabAccess = false;
      }

    });

    this.subscriptions.push(this.service.noProjectsObs.subscribe((res) => {
      this.noProjects = res;
    }));
  }

  /**
      Used to receive all filter data from filter component when user
      click apply and call kpi
       **/
  receiveSharedData($event) {
    this.sprintsOverlayVisible = this.service.getSelectedLevel()['hierarchyLevelId'] === 'project' ? true : false
    if (localStorage?.getItem('completeHierarchyData')) {
      const hierarchyData = JSON.parse(localStorage.getItem('completeHierarchyData'));
      if (Object.keys(hierarchyData).length > 0 && hierarchyData[this.selectedtype.toLowerCase()]) {
        this.hierarchyLevel = hierarchyData[this.selectedtype.toLowerCase()];
      }
    }
    if (this.service.getDashConfigData() && Object.keys(this.service.getDashConfigData()).length > 0 && $event?.selectedTab?.toLowerCase() !== 'iteration') {
      this.configGlobalData = this.service.getDashConfigData()['others'].filter((item) => (item.boardName.toLowerCase() === $event?.selectedTab?.toLowerCase()) || (item.boardName.toLowerCase() === $event?.selectedTab?.toLowerCase().split('-').join(' ')))[0]?.kpis;
      this.updatedConfigGlobalData = this.configGlobalData?.filter(item => item.shown && item.isEnabled);
      if (JSON.stringify(this.filterApplyData) !== JSON.stringify($event.filterApplyData) || this.configGlobalData) {
        if (this.serviceObject['makeAPICall']) {
          this.allKpiArray = [];
          this.kpiChartData = {};
          this.chartColorList = {};
          this.kpiSelectedFilterObj = {};
          this.kpiDropdowns = {};
          this.kpiTrendsObj = {};
          this.kpiLoader = true;
        }
        const kpiIdsForCurrentBoard = this.configGlobalData?.map(kpiDetails => kpiDetails.kpiId);
        this.masterData = $event.masterData;
        this.filterData = $event.filterData;
        this.filterApplyData = $event.filterApplyData;
        this.noOfFilterSelected = Object.keys(this.filterApplyData).length;
        this.selectedJobFilter = 'Select';
        if (this.filterData?.length && $event.makeAPICall) {
          this.noTabAccess = false;
          // call kpi request according to tab selected
          if (this.masterData && Object.keys(this.masterData).length) {
            this.processKpiConfigData();
            if (this.service.getSelectedType().toLowerCase() === 'kanban') {
              // this.configGlobalData = this.service.getDashConfigData()[this.selectedtype.toLowerCase()].filter((item) => (item.boardName.toLowerCase() === this.selectedTab.toLowerCase()) || (item.boardName.toLowerCase() === this.selectedTab.toLowerCase().split('-').join(' ')))[0]?.kpis;
              this.groupBitBucketKanbanKpi(kpiIdsForCurrentBoard);
            } else {
              this.groupBitBucketKpi(kpiIdsForCurrentBoard);
            }
            let projectLevel = this.filterData.filter((x) => x.labelName == 'project')[0]?.level;
            if (projectLevel) {
              if (this.filterApplyData.level == projectLevel) this.getKpiCommentsCount();
            }
          }
        } else if (this.filterData?.length && !$event.makeAPICall) {
          // alert('no call');
          this.allKpiArray.forEach(element => {
            this.getDropdownArray(element?.kpiId);
            // For kpi3 and kpi53 generating table column headers and table data
            if (element.kpiId === 'kpi3' || element.kpiId === 'kpi53') {
              //generating column headers
              const columnHeaders = [];
              if (Object.keys(this.kpiSelectedFilterObj)?.length && this.kpiSelectedFilterObj[element.kpiId]?.length && this.kpiSelectedFilterObj[element.kpiId][0]) {
                columnHeaders.push({ field: 'name', header: this.hierarchyLevel[+this.filterApplyData.level - 1]?.hierarchyLevelName + ' Name' });
                columnHeaders.push({ field: 'value', header: this.kpiSelectedFilterObj[element.kpiId][0] });
                columnHeaders.push({ field: 'maturity', header: 'Maturity' });
              }
              if (this.kpiChartData[element.kpiId]) {
                this.kpiChartData[element.kpiId].columnHeaders = columnHeaders;
              }
              //generating Table data
              const kpiUnit = this.updatedConfigGlobalData?.find(kpi => kpi.kpiId === element.kpiId)?.kpiDetail?.kpiUnit;
              const data = [];
              if (this.kpiChartData[element.kpiId] && this.kpiChartData[element.kpiId].length) {
                for (let i = 0; i < this.kpiChartData[element.kpiId].length; i++) {
                  const rowData = {
                    name: this.kpiChartData[element.kpiId][i].data,
                    maturity: 'M' + this.kpiChartData[element.kpiId][i].maturity,
                    value: this.kpiChartData[element.kpiId][i].value[0].data + ' ' + kpiUnit
                  };
                  data.push(rowData);
                }

                this.kpiChartData[element.kpiId].data = data;
              }
              this.showKpiTrendIndicator[element.kpiId] = false;

            }
          });
        } else {
          this.noTabAccess = true;
        }
        if (this.hierarchyLevel && this.hierarchyLevel[+this.filterApplyData.level - 1]?.hierarchyLevelId === 'project') {
          this.showCommentIcon = true;
        } else {
          this.showCommentIcon = false;
        }
      }
    }
  }


  processKpiConfigData() {
    const disabledKpis = this.configGlobalData?.filter(item => item.shown && !item.isEnabled);
    // user can nable kpis from show/hide filter, added below flag to show different message to the user
    this.enableByeUser = disabledKpis?.length ? true : false;
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
            if(!this.kpiTrendsObj.hasOwnProperty(element.kpiId)){
                this.createTrendsData(element.kpiId);
            }
        } else {
            this.kpiConfigData[element.kpiId] = false;
        }
    });
}

  /** get array of the kpi level dropdown filter */
  getDropdownArray(kpiId) {
    const idx = this.ifKpiExist(kpiId);
    let trendValueList = [];
    const optionsArr = [];
    const optionsArr2 = [];
    if (idx != -1) {
      trendValueList = this.allKpiArray[idx]?.trendValueList;
      if ((trendValueList?.length > 0 && trendValueList[0]?.hasOwnProperty('filter')) || (trendValueList?.length > 0 && trendValueList[0]?.hasOwnProperty('filter1'))) {
        const obj = {};
        const obj2 = {};
        for (let i = 0; i < trendValueList?.length; i++) {
          for (let key in this.colorObj) {
            let kpiFilter = trendValueList[i]?.value?.findIndex(x => this.colorObj[key]?.nodeName == x.data);
            if (kpiFilter != -1) {
              let ifExist = trendValueList[i]?.filter1 ? optionsArr.findIndex(x => x == trendValueList[i]?.filter1) : optionsArr.findIndex(x => x == trendValueList[i]?.filter);
              if (ifExist == -1) {
                optionsArr?.push(trendValueList[i]?.filter1 ? trendValueList[i]?.filter1 : trendValueList[i]?.filter);
              }
              if (trendValueList[i]?.hasOwnProperty('filter2')) {
                let ifF1Exist = optionsArr2.findIndex(x => x == trendValueList[i]?.filter2);
                // if (ifF1Exist == -1 && trendValueList[i]?.filter2?.toLowerCase() !=="overall") {
                if (ifF1Exist == -1) {
                  optionsArr2?.push(trendValueList[i]?.filter2);

                }
              }
            }
          }
        }
        const kpiObj = this.updatedConfigGlobalData?.filter(x => x['kpiId'] == kpiId)[0];
        if (kpiObj && kpiObj['kpiDetail']?.hasOwnProperty('kpiFilter') && (kpiObj['kpiDetail']['kpiFilter']?.toLowerCase() == 'multiselectdropdown' || (kpiObj['kpiDetail']['kpiFilter']?.toLowerCase() == 'dropdown' && kpiObj['kpiDetail'].hasOwnProperty('hideOverallFilter') && kpiObj['kpiDetail']['hideOverallFilter']))) {
          const index = optionsArr?.findIndex(x => x?.toLowerCase() == 'overall');
          if (index > -1) {
            optionsArr?.splice(index, 1);
          }
        }
        obj['filterType'] = 'Select a filter';
        obj['options'] = optionsArr;
        this.kpiDropdowns[kpiId] = [];
        this.kpiDropdowns[kpiId].push(obj);

        if (optionsArr2.length > 0) {
          optionsArr2.sort((a, b) => {
            if (a === "Overall") {
              return -1; // "Overall" should be moved to the beginning (0 index)
            } else if (b === "Overall") {
              return 1; // "Overall" should be moved to the beginning (0 index)
            } else {
              return 0; // Maintain the original order of other elements
            }
          });
          obj2['filterType'] = 'Filter by issue type';
          obj2['options'] = optionsArr2;
          this.kpiDropdowns[kpiId].push(obj2);
        }
      }
    }
  }

  ifKpiExist(kpiId) {
    const id = this.allKpiArray?.findIndex((kpi) => kpi.kpiId == kpiId);
    return id;
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

  sortAlphabetically(objArray) {
    if (objArray && objArray?.length > 1) {
      objArray?.sort((a, b) => a.data?.localeCompare(b.data));
    }
    return objArray;
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

  // Used for grouping all BitBucket kpi of kanban from master data and calling BitBucket kpi.
  groupBitBucketKanbanKpi(kpiIdsForCurrentBoard) {
    this.kpiBitBucket = this.helperService.groupKpiFromMaster('BitBucket', true, this.masterData, this.filterApplyData, this.filterData, kpiIdsForCurrentBoard, '', this.selectedTab);
    if (this.kpiBitBucket?.kpiList?.length > 0) {
      this.postBitBucketKanbanKpi(this.kpiBitBucket, 'bitbucket');
    }
  }

  // Used for grouping all BitBucket kpi of scrum from master data and calling BitBucket kpi.
  groupBitBucketKpi(kpiIdsForCurrentBoard) {
    this.kpiBitBucket = this.helperService.groupKpiFromMaster('BitBucket', false, this.masterData, this.filterApplyData, this.filterData, kpiIdsForCurrentBoard, '', this.selectedTab);
    if (this.kpiBitBucket?.kpiList?.length > 0) {
      this.postBitBucketKpi(this.kpiBitBucket, 'bitbucket');
    }
  }

  // post request of BitBucket(scrum)
  postBitBucketKpi(postData, source): void {
    this.loaderBitBucket = true;
    if (this.bitBucketKpiRequest && this.bitBucketKpiRequest !== '') {
      this.bitBucketKpiRequest.unsubscribe();
    }
    this.bitBucketKpiRequest = this.httpService.postKpi(postData, source)
      .subscribe(getData => {
        this.loaderBitBucket = false;
        // getData = require('../../../test/resource/fakeKPI11.json');
        if (getData !== null && getData[0] !== 'error' && !getData['error']) {
          // creating array into object where key is kpi id
          this.bitBucketKpiData = this.helperService.createKpiWiseId(getData);
          this.createAllKpiArray(this.bitBucketKpiData);

        } else {
          this.bitBucketKpiData = getData;
        }
        this.kpiLoader = false;
      });
  }

  // post request of BitBucket(scrum)
  postBitBucketKanbanKpi(postData, source): void {
    this.loaderBitBucket = true;
    if (this.bitBucketKpiRequest && this.bitBucketKpiRequest !== '') {
      this.bitBucketKpiRequest.unsubscribe();
    }
    this.bitBucketKpiRequest = this.httpService.postKpiKanban(postData, source)
      .subscribe(getData => {
        this.loaderBitBucket = false;
        // getData = require('../../../test/resource/fakeKPI65.json');
        if (getData !== null && getData[0] !== 'error' && !getData['error']) {
          // creating array into object where key is kpi id
          this.bitBucketKpiData = this.helperService.createKpiWiseId(getData);
          this.createAllKpiArray(this.bitBucketKpiData);
        } else {
          this.bitBucketKpiData = getData;
        }
      });
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
          if (data[key]?.kpiId === "kpi72") {
            this.kpiSelectedFilterObj[data[key]?.kpiId] = { 'filter1': [initialC], 'filter2': ['Overall'] };
          }
          else {
            this.kpiSelectedFilterObj[data[key]?.kpiId] = { 'filter': ['Overall'] };
          }
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

  getChartData(kpiId, idx, aggregationType) {
    const trendValueList = this.allKpiArray[idx]?.trendValueList;
    if (trendValueList?.length > 0 && trendValueList[0]?.hasOwnProperty('filter')) {
      if (this.kpiSelectedFilterObj[kpiId]?.length > 1) {
        if (kpiId === 'kpi17') {
          this.kpiChartData[kpiId] = [];
          for (let i = 0; i < this.kpiSelectedFilterObj[kpiId]?.length; i++) {
            let trendList = trendValueList?.filter(x => x['filter'] == this.kpiSelectedFilterObj[kpiId][i])[0];
            trendList?.value.forEach((x) => {
              let obj = {
                'data': this.kpiSelectedFilterObj[kpiId][i],
                'value': x.value
              }
              this.kpiChartData[kpiId].push(obj);
            })
          }
        } else {
          const tempArr = {};
          for (let i = 0; i < this.kpiSelectedFilterObj[kpiId]?.length; i++) {
            tempArr[this.kpiSelectedFilterObj[kpiId][i]] = (trendValueList?.filter(x => x['filter'] == this.kpiSelectedFilterObj[kpiId][i])[0]?.value);
          }
          this.kpiChartData[kpiId] = this.helperService.applyAggregationLogic(tempArr, aggregationType, this.tooltip.percentile);
        }
      } else {
        if (this.kpiSelectedFilterObj[kpiId]?.length > 0) {
          this.kpiChartData[kpiId] = trendValueList?.filter(x => x['filter'] == this.kpiSelectedFilterObj[kpiId][0])[0]?.value;
          if (kpiId == 'kpi17' && this.kpiSelectedFilterObj[kpiId][0]?.toLowerCase() == 'average coverage') {
            for (let i = 0; i < this.kpiChartData[kpiId]?.length; i++) {
              this.kpiChartData[kpiId][i]['filter'] = this.kpiSelectedFilterObj[kpiId][0];
            }
          }
        } else {
          this.kpiChartData[kpiId] = trendValueList?.filter(x => x['filter'] == 'Overall')[0]?.value;
        }
      }
    }
    else if (trendValueList?.length > 0 && trendValueList[0]?.hasOwnProperty('filter1')) {
      if (this.kpiSelectedFilterObj[kpiId]?.hasOwnProperty('filter1')
        && this.kpiSelectedFilterObj[kpiId]?.hasOwnProperty('filter2')) {
        let tempArr = [];
        tempArr = this.createCombinations(this.kpiSelectedFilterObj[kpiId]['filter1'], this.kpiSelectedFilterObj[kpiId]['filter2'])
        const preAggregatedValues = [];
        for (let i = 0; i < tempArr?.length; i++) {
          preAggregatedValues?.push(...trendValueList?.filter(k => k['filter1'] == tempArr[i]?.filter1 && k['filter2'] == tempArr[i]?.filter2));
        }
        this.kpiChartData[kpiId] = preAggregatedValues[0]?.value;
      }
      else if (this.kpiSelectedFilterObj[kpiId]?.hasOwnProperty('filter1')
        || this.kpiSelectedFilterObj[kpiId]?.hasOwnProperty('filter2')) {
        const filters = this.kpiSelectedFilterObj[kpiId]['filter1'] || this.kpiSelectedFilterObj[kpiId]['filter2'];
        let preAggregatedValues = [];
        for (let i = 0; i < filters?.length; i++) {
          preAggregatedValues = [...preAggregatedValues, ...(trendValueList)?.filter(x => x['filter1'] == filters[i] || x['filter2'] == filters[i])];
        }
        this.kpiChartData[kpiId] = preAggregatedValues[0]?.value;
      }
      else {
        this.kpiChartData[kpiId] = [];
        if (trendValueList && trendValueList?.length > 0) {
          this.kpiChartData[kpiId]?.push(trendValueList?.filter((x) => x['filter'] == 'Overall')[0]);
        } else if (trendValueList?.length > 0) {
          this.kpiChartData[kpiId] = [...trendValueList];
        } else {
          //const obj = JSON.parse(JSON.stringify(trendValueList));
          this.kpiChartData[kpiId]?.push(trendValueList);
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
    if (this.kpiChartData && Object.keys(this.kpiChartData).length && this.updatedConfigGlobalData) {
      this.helperService.calculateGrossMaturity(this.kpiChartData, this.updatedConfigGlobalData);
    }
    this.createTrendsData(kpiId);
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

  createTrendsData(kpiId) {
    let enabledKpiObj = this.updatedConfigGlobalData?.filter(x => x.kpiId == kpiId)[0];
    if (enabledKpiObj && Object.keys(enabledKpiObj)?.length != 0) {
      this.kpiTrendsObj[kpiId] = [];
      for (let i = 0; i < this.kpiChartData[kpiId]?.length; i++) {
        if (this.kpiChartData[kpiId][i]?.value?.length > 0) {
          let trendObj = {};
          const [latest, trend, unit] = this.checkLatestAndTrendValue(enabledKpiObj, this.kpiChartData[kpiId][i]);
          trendObj = {
            "hierarchyName": this.kpiChartData[kpiId][i]?.data,
            "value": latest,
            "trend": trend,
            "maturity": kpiId != 'kpi3' && kpiId != 'kpi53' ?
              this.checkMaturity(this.kpiChartData[kpiId][i])
              : 'M' + this.kpiChartData[kpiId][i]?.maturity,
            "maturityValue": this.kpiChartData[kpiId][i]?.maturityValue,
            "kpiUnit": unit
          };
          if (kpiId === 'kpi997') {
            trendObj['value'] = 'NA';
          }
          this.kpiTrendsObj[kpiId]?.push(trendObj);
        }
      }
    }
  }

  checkLatestAndTrendValue(kpiData, item) {
    let latest: string = '';
    let trend: string = '';
    if (item?.value?.length > 0) {
      let tempVal = item?.value[item?.value?.length - 1]?.lineValue ? item?.value[item?.value?.length - 1]?.lineValue : item?.value[item?.value?.length - 1]?.value;
      var unit = kpiData?.kpiDetail?.kpiUnit?.toLowerCase() != 'number' && kpiData?.kpiDetail?.kpiUnit?.toLowerCase() != 'stories' && kpiData?.kpiDetail?.kpiUnit?.toLowerCase() != 'tickets' ? kpiData?.kpiDetail?.kpiUnit.trim() : '';
      latest = tempVal > 0 ? (Math.round(tempVal * 10) / 10) + (unit ? ' ' + unit : '') : tempVal + (unit ? ' ' + unit : '');
    }
    if (item?.value?.length > 0 && kpiData?.kpiDetail?.showTrend) {
      if (kpiData?.kpiDetail?.trendCalculative) {
        let lhsKey = kpiData?.kpiDetail?.trendCalculation?.length > 0 ? kpiData?.kpiDetail?.trendCalculation[0]?.lhs : '';
        let rhsKey = kpiData?.kpiDetail?.trendCalculation?.length > 0 ? kpiData?.kpiDetail?.trendCalculation[0]?.rhs : '';
        let lhs = item?.value[item?.value?.length - 1][lhsKey];
        let rhs = item?.value[item?.value?.length - 1][rhsKey];
        let operator = lhs < rhs ? '<' : lhs > rhs ? '>' : '=';
        let trendObj = kpiData?.kpiDetail?.trendCalculation?.find((item) => item.operator == operator);
        if (trendObj) {
          trend = trendObj['type']?.toLowerCase() == 'downwards' ? '-ve' : trendObj['type']?.toLowerCase() == 'upwards' ? '+ve' : '-- --';
        } else {
          trend = 'NA';
        }
      } else {
        let lastVal = item?.value[item?.value?.length - 1]?.value;
        let secondLastVal = item?.value[item?.value?.length - 2]?.value;
        let isPositive = kpiData?.kpiDetail?.isPositiveTrend;
        if (secondLastVal > lastVal && !isPositive) {
          trend = '+ve';
        } else if (secondLastVal < lastVal && !isPositive) {
          trend = '-ve';
        } else if (secondLastVal < lastVal && isPositive) {
          trend = '+ve';
        } else if (secondLastVal > lastVal && isPositive) {
          trend = '-ve';
        } else {
          trend = '-- --';
        }
      }
    } else {
      trend = 'NA';
    }
    return [latest, trend, unit];
  }

  checkMaturity(item) {
    let maturity = item.maturity;
    if (maturity == undefined) {
      return 'NA';
    }
    if (item.value.length >= 5) {
      const last5ArrItems = item.value.slice(item.value.length - 5, item.value.length);
      const tempArr = last5ArrItems.filter(x => x.data != 0);
      if (tempArr.length == 0) {
        maturity = '--';
      }
    } else {
      maturity = '--';
    }
    maturity = maturity != 'NA' && maturity != '--' && maturity != '-' ? 'M' + maturity : maturity;
    return maturity;
  }



}
