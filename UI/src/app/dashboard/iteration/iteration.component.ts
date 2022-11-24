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
import { Component, OnInit, OnDestroy, Input } from '@angular/core';
import { HttpService } from '../../services/http.service';
import { ExcelService } from '../../services/excel.service';
import { SharedService } from '../../services/shared.service';
import { HelperService } from '../../services/helper.service';

declare let require: any;

@Component({
  selector: 'app-iteration',
  templateUrl: './iteration.component.html',
  styleUrls: ['./iteration.component.css']
})
export class IterationComponent implements OnInit, OnDestroy {
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
  timeRemaining = 0;
  displayModal = false;
  modalDetails: object = {
    header: '',
    tableHeadings: [],
    tableValues: []
  };
  kpiDropdowns = {};
  trendBoxColorObj: any;
  chartColorList: Array<string> = ['#079FFF', '#00E6C3', '#CDBA38', '#FC6471', '#BD608C', '#7D5BA6'];
  noProjects = false;

  constructor(private service: SharedService, private httpService: HttpService, private excelService: ExcelService, private helperService: HelperService) {
    // this.kanbanActivated = false;
    this.service.setSelectedType('Scrum');
    this.subscriptions.push(this.service.passDataToDashboard.subscribe((sharedobject) => {
      if (sharedobject?.filterData?.length && sharedobject.selectedTab.toLowerCase() === 'iteration') {
        this.allKpiArray = [];
        this.kpiChartData = {};
        this.kpiSelectedFilterObj = {};
        this.kpiDropdowns = {};
        this.receiveSharedData(sharedobject);
        this.noTabAccess = false;
      } else {
        this.noTabAccess = true;
      }
    }));

    // used to know whether scrum or kanban is clicked
    this.subscriptions.push(this.service.onTypeRefresh.subscribe((sharedobject) => {
      this.getSelectedType(sharedobject);
      // this.kanbanActivated = sharedobject === 'Kanban' ? true : false;
      if (this.service.getDashConfigData() && Object.keys(this.service.getDashConfigData()).length > 0) {
        this.configGlobalData = this.service.getDashConfigData()['scrum'].filter((item) => item.boardName.toLowerCase() == 'iteration')[0]?.kpis;
        this.processKpiConfigData();
      }
    }));

    if (this.service.getDashConfigData() && Object.keys(this.service.getDashConfigData()).length > 0) {
      this.configGlobalData = this.service.getDashConfigData()['scrum'].filter((item) => item.boardName.toLowerCase() == 'iteration')[0]?.kpis;
      this.processKpiConfigData();
    }

    this.subscriptions.push(this.service.globalDashConfigData.subscribe((globalConfig) => {
      this.configGlobalData = globalConfig['scrum'].filter((item) => item.boardName.toLowerCase() == 'iteration')[0]?.kpis;
      this.processKpiConfigData();
    }));

    this.subscriptions.push(this.service.noSprintsObs.subscribe((res) => {
      this.noSprints = res;
    }));

    this.subscriptions.push(this.service.noProjectsObs.subscribe((res) => {
      this.noProjects = res;
    }));
  }

  processKpiConfigData() {
    const disabledKpis = this.configGlobalData.filter(item => item.shown && !item.isEnabled);
    // user can enable kpis from show/hide filter, added below flag to show different message to the user
    this.enableByUser = disabledKpis?.length ? true : false;
    // noKpis - if true, all kpis are not shown to the user (not showing kpis to the user)
    this.updatedConfigGlobalData = this.configGlobalData.filter(item => item.shown && item.isEnabled);
    if (this.updatedConfigGlobalData?.length === 0) {
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
    this.masterData = $event.masterData;
    this.filterData = $event.filterData;
    this.filterApplyData = $event.filterApplyData;
    this.noOfFilterSelected = Object.keys(this.filterApplyData).length;
    if (this.filterData?.length) {
      this.noTabAccess = false;
      // call kpi request according to tab selected
      if (this.masterData && Object.keys(this.masterData).length) {
        if (this.selectedtype !== 'Kanban') {
          // this.groupHygieneKpi();
          const kpiIdsForCurrentBoard = this.configGlobalData?.map(kpiDetails => kpiDetails.kpiId);
          const selectedSprint = this.filterData?.filter(x => x.nodeId == this.filterApplyData?.selectedMap['sprint'][0])[0];
          const today = new Date().toISOString().split('T')[0];
          const endDate = new Date(selectedSprint?.sprintEndDate).toISOString().split('T')[0];
          this.timeRemaining = this.calcBusinessDays(today, endDate);
          this.groupJiraKpi(kpiIdsForCurrentBoard);
        }
      }
    } else {
      this.noTabAccess = true;
    }

  }

  // Used for grouping all Sonar kpi from master data and calling Sonar kpi.(only for scrum).
  groupJiraKpi(kpiIdsForCurrentBoard) {
    this.jiraKpiData = {};
    // creating a set of unique group Ids
    const groupIdSet = new Set();
    this.masterData.kpiList.forEach((obj) => {
      if (!obj.kanban && obj.kpiSource === 'Jira' && obj.kpiCategory == 'Iteration') {
        groupIdSet.add(obj.groupId);
      }
    });

    // sending requests after grouping the the KPIs according to group Id
    groupIdSet.forEach((groupId) => {
      if (groupId) {
        this.kpiJira = this.helperService.groupKpiFromMaster('Jira', false, this.masterData, this.filterApplyData, this.filterData, kpiIdsForCurrentBoard, groupId);
        this.postJiraKpi(this.kpiJira, 'jira');
      }
    });

  }



  // Used for grouping all Hygiene kpi from master data and calling Jira kpi.(only for scrum).
  /*groupHygieneKpi() {

    // creating a set of unique group Ids
    const groupIdSet = new Set();
    this.masterData.kpiList.forEach((obj) => {
      if (!obj.kanban && obj.kpiSource === 'Jira' && obj.kpiCategory === 'Iteration') {
        groupIdSet.add(obj.groupId);
      }
    });
    // sending requests after grouping the the KPIs according to group Id
    groupIdSet.forEach((groupId) => {
      if (groupId) {
        this.kpiJira = this.helperService.groupKpiFromMaster('Jira', false, this.masterData, this.filterApplyData, this.filterData, groupId);
        this.postJiraKpi(this.kpiJira, 'jira');
      }
    });

  }*/


  // post request of Jira(scrum) hygiene
  postJiraKpi(postData, source): void {
    postData.kpiList.forEach(element => {
      this.loaderJiraArray.push(element.kpiId);
    });

    this.jiraKpiRequest = this.httpService.postKpi(postData, source)
      .subscribe(getData => {
        if (getData !== null && getData[0] !== 'error' && !getData['error']) {
          // getData = require('../../../test/resource/fakeIterationKpi.json');
          // creating array into object where key is kpi id
          const localVariable = this.helperService.createKpiWiseId(getData);
          for (const kpi in localVariable) {
            this.loaderJiraArray.splice(this.loaderJiraArray.indexOf(kpi), 1);
          }
          // if (localVariable && localVariable['kpi76'] && localVariable['kpi76'].maturityValue) {
          //   this.colorAccToMaturity(localVariable['kpi76'].maturityValue);
          // }
          this.jiraKpiData = Object.assign({}, this.jiraKpiData, localVariable);
          this.createAllKpiArray(localVariable);
        } else {
          this.jiraKpiData = getData;
          postData.kpiList.forEach(element => {
            this.loaderJiraArray.splice(this.loaderJiraArray.indexOf(element.kpiId), 1);
          });
        }
        // if (!!this.jiraKpiData && !!this.jiraKpiData.kpi115 && !!this.jiraKpiData.kpi115.value && this.jiraKpiData.kpi115.value !== 'undefined' && this.jiraKpiData.kpi115.value !== undefined && this.jiraKpiData.kpi115.value.length > 1) {
        //   const data = [{
        //     data: this.jiraKpiData.kpi115.value[1].data,
        //     count: this.jiraKpiData.kpi115.value[1].count,
        //     totalCount: this.jiraKpiData.kpi115.value[0].count,
        //     totalText: this.jiraKpiData.kpi115.value[0].data,
        //     totalOptionalCount: this.jiraKpiData.kpi115.value[2]?.count,
        //     totalOptionalText: this.jiraKpiData.kpi115.value[2]?.data
        //   },
        //   {
        //     data: 'Stories With Worklog',
        //     count: this.jiraKpiData.kpi115.value[0].count - this.jiraKpiData.kpi115.value[1].count,
        //     totalCount: this.jiraKpiData.kpi115.value[0].count,
        //     totalText: this.jiraKpiData.kpi115.value[0].data,
        //     totalOptionalCount: this.jiraKpiData.kpi115.value[2]?.count,
        //     totalOptionalText: this.jiraKpiData.kpi115.value[2]?.data
        //   }];
        //   this.jiraKpiData.kpi115.value = data;
        // }
        // if (!!this.jiraKpiData && !!this.jiraKpiData.kpi76 && !!this.jiraKpiData.kpi76.value && this.jiraKpiData.kpi76.value !== 'undefined' && this.jiraKpiData.kpi76.value !== undefined && this.jiraKpiData.kpi76.value.length > 1) {
        //   const totalStoryCount = this.jiraKpiData.kpi76.value[1]?.count;
        //   this.jiraKpiData.kpi76.value[1].data = 'In Progress';
        //   this.jiraKpiData.kpi76.value[1].count = this.jiraKpiData.kpi76.value[1]?.count - (this.jiraKpiData.kpi76.value[0]?.count + this.jiraKpiData.kpi76.value[2]?.count);
        //   this.jiraKpiData.kpi76.value.forEach(data => {
        //     data.totalCount = totalStoryCount;
        //     data.totalText = 'Total Stories';
        //   });
        //   this.service.currentSelectedSprintSub.next(this.jiraKpiData?.kpi76?.trendValueList[0]?.value[0]?.sSprintID);
        // }
        // if (!!this.jiraKpiData && !!this.jiraKpiData.kpi81 && !!this.jiraKpiData.kpi81.value && this.jiraKpiData.kpi81.value !== 'undefined' && this.jiraKpiData.kpi81.value !== undefined && this.jiraKpiData.kpi81.value.length > 1) {
        //   const totalStoryCount = this.jiraKpiData.kpi81.value[0].count;
        //   this.jiraKpiData.kpi81.value[0].data = 'Stories with Estimate';
        //   this.jiraKpiData.kpi81.value[0].count = this.jiraKpiData.kpi81.value[0].count - this.jiraKpiData.kpi81.value[1].count;
        //   this.jiraKpiData.kpi81.value.forEach(data => {
        //     data.totalCount = totalStoryCount;
        //     data.totalText = 'Total Stories';
        //   });
        // }
        // if (!!this.jiraKpiData && !!this.jiraKpiData.kpi75 && !!this.jiraKpiData.kpi75.value && this.jiraKpiData.kpi75.value !== 'undefined' && this.jiraKpiData.kpi75.value !== undefined && this.jiraKpiData.kpi75.value.length > 0) {
        //   const colorValues = ['#44739f', '#AEDB76'];
        //   this.jiraKpiData.kpi75.value.map((data, index) => {
        //     data.backgroundColor = (parseFloat(data.value.replace('%', '')) <= 100) ? colorValues[index] : '#F06667';
        //   });
        // }
        // if (!!this.jiraKpiData && !!this.jiraKpiData.kpi78 && !!this.jiraKpiData.kpi78.value && this.jiraKpiData.kpi78.value !== 'undefined' && this.jiraKpiData.kpi78.value !== undefined && this.jiraKpiData.kpi78.value.length > 0) {
        //   const dataArray = [];
        //   this.jiraKpiData.kpi78.value.forEach(data => {
        //     if (data.data.indexOf('%') < 0) {
        //       dataArray.push(data);
        //     }
        //   });

        //   this.jiraKpiData.kpi78.value = dataArray;
        //   const colorValues = ['#44739f', '#AEDB76'];
        //   this.jiraKpiData.kpi78.value.map((data, index) => {
        //     data.backgroundColor = (parseFloat(data.value.replace('%', '')) <= 100 && parseFloat(data.value.replace('%', '')) >= 0) ? colorValues[index] : '#F06667';
        //   });
        // }

        // if (this.jiraKpiData && this.jiraKpiData.kpi80 && this.jiraKpiData.kpi80.trendValueList && this.jiraKpiData.kpi80.trendValueList.length) {
        //   if (this.jiraKpiData.kpi80.trendValueList[0] && this.jiraKpiData.kpi80.trendValueList[0].value) {
        //     if (this.jiraKpiData.kpi80.trendValueList[0].value[0].howerValue) {
        //       const dataArray = [];
        //       let self = this;
        //       Object.keys(this.jiraKpiData.kpi80.trendValueList[0].value[0].howerValue).forEach((key) => {
        //         let valueObj = {};
        //         valueObj[key] = self.jiraKpiData.kpi80.trendValueList[0].value[0].howerValue[key];
        //         dataArray.push(valueObj);
        //       });
        //       this.jiraKpiData.kpi80.trendValueList[0].value[0].howerValue = dataArray;
        //       dataArray.forEach((data) => {
        //         if (Object.keys(data)[0] === 'Total Defects') {
        //           this.jiraKpiData.kpi80.maxValue = data['Total Defects'];
        //         }
        //       });
        //     }
        //   }
        // }
        this.kpiLoader = false;
      });
  }

  // return colors according to maturity only for CycleTime
  /*colorAccToMaturity(maturityValue) {
    const maturityArray = maturityValue.toString().split('-');
    for (let index = 0; index <= 2; index++) {
      const maturity = maturityArray[index];
      this.maturityColorCycleTime[index] = this.helperService.colorAccToMaturity(maturity);
    }
  }*/

  ngOnInit() {
    this.selectedtype = this.service.getSelectedType();
    if (this.service.getFilterObject()) {
      this.receiveSharedData(this.service.getFilterObject());
    }

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
    // this.httpService.getTooltipData()
    //   .subscribe(filterData => {
    //     if (filterData[0] !== 'error') {
    //       this.tooltip = filterData;
    //       console.log(this.tooltip);
    //     }
    //   });
  }

  // unsubscribing all Kpi Request
  ngOnDestroy() {
    this.subscriptions.forEach(subscription => subscription.unsubscribe());
  }


  // download excel functionality
  downloadExcel(kpiId, kpiName, isKanban) {
    const sprintIncluded = ['ACTIVE', 'CLOSED'];
    this.helperService.downloadExcel(kpiId, kpiName, isKanban, this.filterApplyData, this.filterData, sprintIncluded);
  }

  // Return video link if video link present
  getVideoLink(kpiId) {
    const kpiData = this.masterData.kpiList.find(kpiObj => kpiObj.kpiId === kpiId);
    if (!kpiData?.videoLink?.disabled && kpiData?.videoLink?.videoUrl) {
      return kpiData?.videoLink?.videoUrl;
    } else {
      // Show message that video is not available
    }
  }

  // Return boolean flag based on link is available and video is enabled
  isVideoLinkAvailable(kpiId) {
    let kpiData;
    try {
      kpiData = this.masterData?.kpiList?.find(kpiObj => kpiObj.kpiId === kpiId);
      if (!kpiData?.videoLink?.disabled && kpiData?.videoLink?.videoUrl) {
        return true;
      } else {
        return false;
      }
    } catch {
      return false;
    }
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

  applyAggregationLogic(arr) {
    const aggregatedArr = [JSON.parse(JSON.stringify(arr[0]))];
      for(let i = 0;i<arr?.length;i++){
        for(let j = 0; j<arr[i]?.data?.length; j++){
          let idx = aggregatedArr[0].data?.findIndex(x => x.label == arr[i]?.data[j]?.label);
          if(idx == -1){
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
          
          if(idx != -1){
            aggregatedArr[0].data[idx]['value'] += arr[i].data[j]['value'];
            if(aggregatedArr[0]?.data[idx]?.hasOwnProperty('value1') && aggregatedArr[0]?.data[idx]?.value1 != null){
              aggregatedArr[0].data[idx]['value1'] += arr[i].data[j]['value1'];
            }
            if(aggregatedArr[0]?.data[idx]?.hasOwnProperty('modalValues') && aggregatedArr[0]?.data[idx]?.modalValues != null){
              aggregatedArr[0].data[idx]['modalValues'] = [...aggregatedArr[0]?.data[idx]['modalValues'], ... arr[i]?.data[j]['modalValues']];
            }
          }
        }
      }
    return aggregatedArr;
  }

  getChartData(kpiId, idx) {
    const trendValueList = this.allKpiArray[idx]?.trendValueList ? JSON.parse(JSON.stringify(this.allKpiArray[idx]?.trendValueList)) : {};
    if (trendValueList && Object.keys(trendValueList)?.length > 0) {
      if (this.kpiSelectedFilterObj[kpiId]?.hasOwnProperty('filter1')
        && this.kpiSelectedFilterObj[kpiId]['filter1']?.length > 0
        && this.kpiSelectedFilterObj[kpiId]?.hasOwnProperty('filter2')
        && this.kpiSelectedFilterObj[kpiId]['filter2']?.length > 0) {
        const tempArr = [];
        const preAggregatedValues = [];
        /** tempArr: array with combination of all items of filter1 and filter2 */
        for (let i = 0; i < this.kpiSelectedFilterObj[kpiId]['filter1']?.length; i++) {
          for (let j = 0; j < this.kpiSelectedFilterObj[kpiId]['filter2']?.length; j++) {
            tempArr.push({ filter1: this.kpiSelectedFilterObj[kpiId]['filter1'][i], filter2: this.kpiSelectedFilterObj[kpiId]['filter2'][j] });
          }
        }

        for (let i = 0; i < tempArr?.length; i++) {
          preAggregatedValues?.push(...trendValueList['value']?.filter(k => k['filter1'] == tempArr[i]?.filter1 && k['filter2'] == tempArr[i]?.filter2));

        }
        if (preAggregatedValues?.length > 1) {
          this.kpiChartData[kpiId] = this.applyAggregationLogic(preAggregatedValues);
        } else {
          this.kpiChartData[kpiId] = [...preAggregatedValues];
        }
      } else if ((this.kpiSelectedFilterObj[kpiId]?.hasOwnProperty('filter1') && this.kpiSelectedFilterObj[kpiId]['filter1']?.length > 0)
        || (this.kpiSelectedFilterObj[kpiId]?.hasOwnProperty('filter2') && this.kpiSelectedFilterObj[kpiId]['filter2']?.length > 0)) {
        const filters = this.kpiSelectedFilterObj[kpiId]['filter1'] || this.kpiSelectedFilterObj[kpiId]['filter2'];
        let preAggregatedValues = [];
        for (let i = 0; i < filters?.length; i++) {
          preAggregatedValues = [...preAggregatedValues, ...trendValueList['value']?.filter(x => x['filter1'] == filters[i] || x['filter2'] == filters[i])];
        }
        if (preAggregatedValues?.length > 1) {
          this.kpiChartData[kpiId] = this.applyAggregationLogic(preAggregatedValues);
        } else {
          this.kpiChartData[kpiId] = [...preAggregatedValues];
        }
      } else {
        /** when there are no kpi level filters */
        this.kpiChartData[kpiId] = [];
        if (trendValueList && trendValueList?.hasOwnProperty('value') && trendValueList['value']?.length > 0) {
          this.kpiChartData[kpiId]?.push(trendValueList['value']?.filter((x) => x['filter1'] == 'Overall')[0]);
        } else if(trendValueList?.length > 0){
          this.kpiChartData[kpiId] = [...trendValueList];
        }  else {
          const obj = JSON.parse(JSON.stringify(trendValueList));
          this.kpiChartData[kpiId]?.push(obj);
        }
      }
    }else{
      this.kpiChartData[kpiId] = [];
    }

    if (Object.keys(this.kpiChartData)?.length === this.updatedConfigGlobalData?.length) {
      this.helperService.calculateGrossMaturity(this.kpiChartData, this.updatedConfigGlobalData);
    }
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
        if (trendValueList && Object.keys(trendValueList)?.length > 0 && filters && Object.keys(filters)?.length > 0) {
          this.kpiSelectedFilterObj[data[key]?.kpiId] = {};
          const tempObj = {};
          for (const key in filters) {
            tempObj[key] = ['Overall'];
          }
          this.kpiSelectedFilterObj[data[key]?.kpiId] = { ...tempObj };

          this.service.setKpiSubFilterObj(this.kpiSelectedFilterObj);
          this.getDropdownArray(data[key]?.kpiId);
        }
        this.getChartData(data[key]?.kpiId, (this.allKpiArray?.length - 1));
    }
  }

  getDropdownArray(kpiId) {
    const idx = this.ifKpiExist(kpiId);
    let filters = {};
    const dropdownArr = [];

    if (idx != -1) {
      filters = this.allKpiArray[idx]?.filters;
      if (filters && Object.keys(filters).length !== 0) {
        Object.keys(filters)?.forEach(x => {
          dropdownArr.push(filters[x]);
        });
      }
    }
    this.kpiDropdowns[kpiId] = [...dropdownArr];
  }

  handleSelectedOption(event, kpi) {
    this.kpiSelectedFilterObj[kpi?.kpiId] = {};
    if (event && Object.keys(event)?.length !== 0) {
      for (const key in event) {
        if (event[key]?.length == 0) {
          delete event[key];
        }
      }
      this.kpiSelectedFilterObj[kpi?.kpiId] = event;
    } else {
      this.kpiSelectedFilterObj[kpi?.kpiId].push(event);
    }

    this.getChartData(kpi?.kpiId, this.ifKpiExist(kpi?.kpiId));
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
    if (unit?.toLowerCase() == 'hours') {
      const hours = (val / 60);
      const rhours = Math.floor(hours);
      const minutes = (hours - rhours) * 60;
      const rminutes = Math.round(minutes);
      if (rminutes == 0) {
        val = rhours + 'h';
      } else if (rhours == 0) {
        val = rminutes + 'm';
      } else {
        val = rhours + 'h ' + rminutes + 'm';
      }
    }
    return val;
  }

  handleArrowClick(kpi, label, tableValues) {
    this.displayModal = true;
    const idx = this.ifKpiExist(kpi?.kpiId);
    this.modalDetails['tableHeadings'] = this.allKpiArray[idx]?.modalHeads;
    this.modalDetails['header'] = kpi?.kpiName + ' / ' + label;
    this.modalDetails['tableValues'] = tableValues;
  }
}
