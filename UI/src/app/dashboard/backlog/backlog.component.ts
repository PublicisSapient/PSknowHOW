import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { distinctUntilChanged } from 'rxjs/operators';
import { ExportExcelComponent } from 'src/app/component/export-excel/export-excel.component';
import { ExcelService } from 'src/app/services/excel.service';
import { HelperService } from 'src/app/services/helper.service';
import { HttpService } from 'src/app/services/http.service';
import { SharedService } from 'src/app/services/shared.service';

@Component({
  selector: 'app-backlog',
  templateUrl: './backlog.component.html',
  styleUrls: ['./backlog.component.css']
})
export class BacklogComponent implements OnInit, OnDestroy{
  @ViewChild('exportExcel') exportExcelComponent: ExportExcelComponent;
  subscriptions: any[] = [];
  masterData = <any>{};
  filterData = <any>[];
  filterApplyData = <any>{};
  noOfFilterSelected = 0;
  selectedtype = '';
  configGlobalData;
  kpiJira = <any>{};
  kpiZypher = <any>{};
  loaderJiraArray = [];
  jiraKpiRequest = <any>'';
  jiraKpiData = <any>{};
  zypherKpiRequest = <any>'';
  loaderZypher = false;
  zypherKpiData = <any>{};
  maturityColorCycleTime = <any>['#f5f5f5', '#f5f5f5', '#f5f5f5'];
  kanbanActivated = false;
  kpiConfigData: Object = {};
  noKpis = false;
  enableByUser = false;
  noSprints = false;
  kpiLoader = true;
  noTabAccess = false;
  kpiSelectedFilterObj = {};
  updatedConfigGlobalData;
  tooltip = <any>{};
  colorObj: object = {};
  trendBoxColorObj: any;
  kpiChartData = {};
  allKpiArray: any = [];
  showKpiTrendIndicator={};
  kpiDropdowns: object = {};
  chartColorList: Array<string> = ['#079FFF', '#00E6C3', '#CDBA38', '#FC6471', '#BD608C', '#7D5BA6'];
  displayModal = false;
  modalDetails = {
    header: '',
    tableHeadings: [],
    tableValues: []
  };
  noProjects = false;
  globalConfig;
  sharedObject;


  constructor(private service: SharedService, private httpService: HttpService, private excelService: ExcelService, private helperService: HelperService) {
    this.subscriptions.push(this.service.passDataToDashboard.pipe(distinctUntilChanged()).subscribe((sharedobject) => {
      if(sharedobject?.filterData?.length && sharedobject.selectedTab.toLowerCase() === 'backlog') {
        this.allKpiArray = [];
        this.kpiChartData = {};
        this.kpiSelectedFilterObj = {};
        this.kpiDropdowns = {};
        this.sharedObject = sharedobject;
        if(this.globalConfig || this.service.getDashConfigData()){
          this.receiveSharedData(sharedobject);
        }
        this.noTabAccess = false;
      } else {
        this.noTabAccess = true;
    }
    }));

    this.subscriptions.push(this.service.globalDashConfigData.subscribe((globalConfig) => {
      if(this.sharedObject || this.service.getFilterObject()){
        this.receiveSharedData(this.service.getFilterObject());
      }
      this.configGlobalData = globalConfig['others'].filter((item) => item.boardName.toLowerCase() == 'backlog')[0]?.kpis;
      this.processKpiConfigData();
    }));

  }
  ngOnInit() {
    this.selectedtype = this.service.getSelectedType();

    this.httpService.getTooltipData()
      .subscribe(filterData => {
        if (filterData[0] !== 'error') {
          this.tooltip = filterData;
        }
      });
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
      if(val){
          this.noTabAccess = true;
      } else{
          this.noTabAccess = false;
      }

  });

  this.subscriptions.push(this.service.noProjectsObs.subscribe((res) => {
    this.noProjects = res;
  }));
}
  processKpiConfigData(){
    this.kpiConfigData = {};
    for(let i=0;i<this.configGlobalData?.length; i++){
      if (this.configGlobalData[i]?.shown ===false && this.configGlobalData[i]?.isEnabled === true) {
        this.kpiConfigData[this.configGlobalData[i]?.kpiId] = this.configGlobalData[i]?.shown;
      } else {
        this.kpiConfigData[this.configGlobalData[i]?.kpiId] = this.configGlobalData[i]?.isEnabled;
      }
    }
    const disabledKpis = this.configGlobalData?.filter(item => item.shown && !item.isEnabled);
    // user can enable kpis from show/hide filter, added below flag to show different message to the user
    this.enableByUser = disabledKpis?.length ? true : false;
    this.updatedConfigGlobalData = this.configGlobalData.filter(item => item.shown && item.isEnabled);
    // noKpis - if true, all kpis are not shown to the user (not showing kpis to the user)
    const showKpisCount = (Object.values(this.kpiConfigData).filter(item => item === true))?.length;
    if (showKpisCount === 0) {
        this.noKpis = true;
    } else {
        this.noKpis = false;
    }
  }
/**
    Used to receive all filter data from filter component when user
    click apply and call kpi
 **/
  receiveSharedData($event) {
    this.configGlobalData = this.service.getDashConfigData()['others'].filter((item) => item.boardName.toLowerCase() == 'backlog')[0]?.kpis;
    this.processKpiConfigData();
    this.masterData = $event.masterData;
    this.filterData = $event.filterData;
    this.filterApplyData = $event.filterApplyData;
    this.noOfFilterSelected = Object.keys(this.filterApplyData).length;
    if(this.filterData?.length) {
      this.noTabAccess = false;
      const kpiIdsForCurrentBoard = this.configGlobalData?.map(kpiDetails => kpiDetails.kpiId);
      // call kpi request according to tab selected
      if (this.masterData && Object.keys(this.masterData).length) {
        this.groupZypherKpi(kpiIdsForCurrentBoard);
        this.groupJiraKpi(kpiIdsForCurrentBoard);
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
      if (!obj.kanban && obj.kpiSource === 'Jira' && obj.kpiCategory == 'Backlog') {
        groupIdSet.add(obj.groupId);
      }
    });

    // sending requests after grouping the the KPIs according to group Id
    groupIdSet.forEach((groupId) => {
      if (groupId) {
        this.kpiJira = this.helperService.groupKpiFromMaster('Jira', false, this.masterData, this.filterApplyData, this.filterData, kpiIdsForCurrentBoard, groupId,'Backlog');
        if (this.kpiJira?.kpiList?.length > 0) {
          this.postJiraKpi(this.kpiJira, 'jira');
        }
      }
    });

  }
   // post request of Jira(scrum) hygiene
   postJiraKpi(postData, source): void {
     this.kpiLoader = true;
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
          if (localVariable['kpi127']) {
            if (localVariable['kpi127'].trendValueList && localVariable['kpi127'].xAxisValues) {
                localVariable['kpi127'].trendValueList.forEach(trendElem => {
                    trendElem.value.forEach(valElem => {
                        if (valElem.value.length === 5 && localVariable['kpi127'].xAxisValues.length === 5) {
                            valElem.value.forEach((element, index) => {
                                element['xAxisTick'] = localVariable['kpi127'].xAxisValues[index];
                            });
                        }
                    });
                });
            }
        }
          this.jiraKpiData = Object.assign({}, this.jiraKpiData, localVariable);
          this.createAllKpiArray(this.jiraKpiData);
        } else {
          this.jiraKpiData = getData;
          postData.kpiList.forEach(element => {
            this.loaderJiraArray.splice(this.loaderJiraArray.indexOf(element.kpiId), 1);
          });
        }

        this.kpiLoader = false;
      });
  }

  // Used for grouping all Sonar kpi from master data and calling Sonar kpi.
  groupZypherKpi(kpiIdsForCurrentBoard) {
    // creating a set of unique group Ids
    const groupIdSet = new Set();
    this.masterData.kpiList.forEach((obj) => {
      if (!obj.kanban && obj.kpiSource === 'Zypher' && obj.kpiCategory === 'Backlog') {
        groupIdSet.add(obj.groupId);
      }
    });

    groupIdSet.forEach((groupId) => {
      if (groupId) {
        this.kpiZypher = this.helperService.groupKpiFromMaster('Zypher', false, this.masterData, this.filterApplyData, this.filterData, kpiIdsForCurrentBoard, groupId,'Backlog');
        if (this.kpiZypher?.kpiList?.length > 0) {
          this.postZypherKpi(this.kpiZypher, 'zypher');
        }
      }
    });
  }

  // calling post request of Zypher(scrum)
  postZypherKpi(postData, source): void {
    this.loaderZypher = true;
    if (this.zypherKpiRequest && this.zypherKpiRequest !== '') {
      this.zypherKpiRequest.unsubscribe();
    }
    this.zypherKpiRequest = this.httpService.postKpi(postData, source)
      .subscribe(getData => {
        this.afterZypherKpiResponseReceived(getData);
      });
  }

  // calls after receiving response from zypher
  afterZypherKpiResponseReceived(getData) {
    this.loaderZypher = false;
    if (getData !== null && getData[0] !== 'error' && !getData['error']) {
      // creating array into object where key is kpi id
      this.zypherKpiData = this.helperService.createKpiWiseId(getData);
      this.createAllKpiArray(this.zypherKpiData);
    } else {
      this.zypherKpiData = getData;
    }
    this.kpiLoader = false;

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
  getSelectedType(sharedobject) {
    this.selectedtype = sharedobject;
  }

  sortAlphabetically(objArray) {
    if (objArray && objArray?.length > 1) {
      objArray?.sort((a, b) =>  a?.data?.localeCompare(b?.data) );
    }
    return objArray;
  }

  getChartData(kpiId, idx, aggregationType) {
    const trendValueList = this.allKpiArray[idx]?.trendValueList;

    if (trendValueList?.length > 0 && trendValueList[0]?.hasOwnProperty('filter')) {
      if (this.kpiSelectedFilterObj[kpiId]?.length > 1) {
        const tempArr = {};
        for (let i = 0; i < this.kpiSelectedFilterObj[kpiId]?.length; i++) {

          tempArr[this.kpiSelectedFilterObj[kpiId][i]] = (trendValueList?.filter(x => x['filter'] == this.kpiSelectedFilterObj[kpiId][i])[0]?.value);
        }
        if (this.getChartType(kpiId) === 'progress-bar') {
          this.kpiChartData[kpiId] = this.applyAggregationLogicForProgressBar(tempArr);
        } else {
          this.kpiChartData[kpiId] = this.helperService.applyAggregationLogic(tempArr, aggregationType, this.tooltip.percentile);
        }
      } else {
        if(this.kpiSelectedFilterObj[kpiId]?.length > 0){
          this.kpiChartData[kpiId] = trendValueList?.filter(x => x['filter'] == this.kpiSelectedFilterObj[kpiId][0])[0]?.value;
      }else{
          this.kpiChartData[kpiId] = trendValueList?.filter(x => x['filter'] == 'Overall')[0]?.value;
      }
      }
    } else {
      if (trendValueList?.length > 0) {
        this.kpiChartData[kpiId] = [...this.sortAlphabetically(trendValueList)];
      } else {
        this.kpiChartData[kpiId] = [];
      }
    }

    if (this.kpiChartData && Object.keys(this.kpiChartData).length) {
      this.helperService.calculateGrossMaturity(this.kpiChartData, this.updatedConfigGlobalData);
    }

    this.updatedConfigGlobalData.forEach(kpi => {
      if (kpi.kpiId == kpiId) {
        this.showKpiTrendIndicator[kpiId] = false;
      }
    });

  }

  getChartType(kpiId){
    return this.updatedConfigGlobalData.find(kpi => kpi.kpiId === kpiId)?.kpiDetail?.chartType;
  }

  applyAggregationLogicForProgressBar(obj) {
    let maxValue = 0;
    let value = 0;
    for (const key in obj) {
      if (obj[key].length > 0 && obj[key][0] && obj[key][0]?.value) {
        if (obj[key][0]?.value && obj[key][0]?.value.length > 0) {
          if (obj[key][0]?.value[0]?.hasOwnProperty('hoverValue')) {
            Object.keys(obj[key][0]?.value[0]?.hoverValue)?.forEach((prop) => {
              if (prop?.toLowerCase()?.includes('total')) {
                maxValue += obj[key][0]?.value[0]?.hoverValue[prop];
              } else {
                value += obj[key][0]?.value[0]?.hoverValue[prop];
              }
            });
          }
        }
      }
    }
    const kpiChartData =obj[Object.keys(obj)[0]];
    kpiChartData[0].value[0].maxValue=maxValue;
    kpiChartData[0].value[0].value=value;
    return kpiChartData;
  }


  ifKpiExist(kpiId) {
    const id = this.allKpiArray?.findIndex((kpi) => kpi.kpiId == kpiId);
    return id;
  }


  handleSelectedOptionForCard(event, kpi) {
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

    this.getChartDataForCard(kpi?.kpiId, this.ifKpiExist(kpi?.kpiId));
    this.service.setKpiSubFilterObj(this.kpiSelectedFilterObj);
  }

  createAllKpiArray(data) {
    for (const key in data) {
      const idx = this.ifKpiExist(data[key]?.kpiId);
      if (idx !== -1) {
        this.allKpiArray.splice(idx, 1);
      }
      let trendValueList;
      /**Todo: if else condition to be removed after api integration */
      this.allKpiArray.push(data[key]);
      trendValueList = this.allKpiArray[this.allKpiArray?.length - 1]?.trendValueList;
      const filters = this.allKpiArray[this.allKpiArray?.length - 1]?.filters;
      /** if: for graphs, else: for other than graphs */
      if (this.getChartType(key)) {
        if (trendValueList?.length > 0 && trendValueList[0]?.hasOwnProperty('filter')) {
          this.kpiSelectedFilterObj[data[key]?.kpiId] = [];
          this.kpiSelectedFilterObj[data[key]?.kpiId]?.push('Overall');
          this.service.setKpiSubFilterObj(this.kpiSelectedFilterObj);
          this.getDropdownArray(data[key]?.kpiId);
        }

        const agType = this.updatedConfigGlobalData?.filter(x => x.kpiId == data[key]?.kpiId)[0]?.kpiDetail?.aggregationCriteria;
        this.getChartData(data[key]?.kpiId, (this.allKpiArray?.length - 1), agType);

      } else {
        if (trendValueList && Object.keys(trendValueList)?.length > 0 && filters && Object.keys(filters)?.length > 0) {
          this.kpiSelectedFilterObj[data[key]?.kpiId] = {};
          const tempObj = {};
          for (const prop in filters) {
            tempObj[prop] = ['Overall'];
            if(data[key]?.kpiId === 'kpi3' && filters[prop]?.filterType === 'Lead Time'){
              tempObj[prop] = filters[prop]['options'][0];
              break;
            }
          }
          this.kpiSelectedFilterObj[data[key]?.kpiId] = { ...tempObj };
          this.service.setKpiSubFilterObj(this.kpiSelectedFilterObj);
          this.getDropdownArrayForCard(data[key]?.kpiId);
        }
        this.getChartDataForCard(data[key]?.kpiId, this.ifKpiExist(data[key]?.kpiId));
      }

    }
  }


  getChartDataForCard(kpiId, idx) {
    const trendValueList = this.allKpiArray[idx]?.trendValueList ? JSON.parse(JSON.stringify(this.allKpiArray[idx]?.trendValueList)) : {};
    if (trendValueList && Object.keys(trendValueList)?.length > 0) {
      if (this.kpiSelectedFilterObj[kpiId]?.hasOwnProperty('filter1')
        && this.kpiSelectedFilterObj[kpiId]['filter1']?.length > 0
        && this.kpiSelectedFilterObj[kpiId]?.hasOwnProperty('filter2')
        && this.kpiSelectedFilterObj[kpiId]['filter2']?.length > 0
        && Array.isArray(this.kpiSelectedFilterObj[kpiId]['filter1'])
        && Array.isArray(this.kpiSelectedFilterObj[kpiId]['filter2'])) {
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
      } else if ((this.kpiSelectedFilterObj[kpiId]?.hasOwnProperty('filter1') && Array.isArray(this.kpiSelectedFilterObj[kpiId]['filter1']) && !this.kpiSelectedFilterObj[kpiId]?.hasOwnProperty('filter2'))
        || (this.kpiSelectedFilterObj[kpiId]?.hasOwnProperty('filter2') && Array.isArray(this.kpiSelectedFilterObj[kpiId]['filter2']) && !this.kpiSelectedFilterObj[kpiId]?.hasOwnProperty('filter1'))) {
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
      }else if(this.kpiSelectedFilterObj[kpiId]?.hasOwnProperty('filter1') || this.kpiSelectedFilterObj[kpiId]?.hasOwnProperty('filter2') && (
        !Array.isArray(this.kpiSelectedFilterObj[kpiId]['filter1']) || !Array.isArray(this.kpiSelectedFilterObj[kpiId]['filter2'])
      )){
        this.getChartDataForCardWithCombinationFilter(kpiId,idx,trendValueList);
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

  getChartDataForCardWithCombinationFilter(kpiId, idx,trendValueList){
    let preAggregatedValues =[];
    for(const filter in this.kpiSelectedFilterObj[kpiId]){
      let tempArr = [];
      if(preAggregatedValues.length > 0){
        tempArr = preAggregatedValues;
      }else{
        tempArr = trendValueList?.value ? trendValueList?.value : [];
      }

      if(Array.isArray(this.kpiSelectedFilterObj[kpiId][filter])){
        preAggregatedValues = [ ...tempArr.filter((x) => this.kpiSelectedFilterObj[kpiId][filter].includes(x[filter]))];
      }else{
        preAggregatedValues = [ ...tempArr.filter((x) =>  x[filter] === this.kpiSelectedFilterObj[kpiId][filter])];
      }
    }

    if (preAggregatedValues?.length > 1) {
      this.kpiChartData[kpiId] = this.applyAggregationLogic(preAggregatedValues);
      if(kpiId === 'kpi3'){
        let days = 0;
        const filterName = this.kpiChartData[kpiId][0]['filter1'].split('-').join('to').toLowerCase();
        const issueDetails = this.kpiChartData[kpiId][0]['data'][1]['modalValues'];
        if(issueDetails.length > 0){
          const issueStateName = Object.keys(issueDetails[0]).find(x => x.toLowerCase().includes(filterName));
          let leadHours = 0;
          for (const issue of issueDetails) {
            let timeArr = issue[issueStateName] !== 'NA' ? issue[issueStateName].trim().split(" ") : [];
            if(timeArr?.length > 0){
              for(let i = 0; i<timeArr?.length; i++){
                if(timeArr[i].includes('d')){
                  days += +timeArr[i].slice(0, timeArr[i].length - 1);  
                }else if(timeArr[i].includes('h')){
                  leadHours += +timeArr[i].slice(0, timeArr[i].length - 1);
                }
              }
            }
          }
          days = days + Math.round(leadHours/8);
          days = Math.round(days/issueDetails.length);
        }
        this.kpiChartData[kpiId][0]['data'][0]['value'] = days;
      }
    } else {
      this.kpiChartData[kpiId] = [...preAggregatedValues];
    }
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
                if(trendValueList[i]?.filter?.toLowerCase() != 'overall'){
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

  getDropdownArrayForCard(kpiId) {
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
    this.kpiSelectedFilterObj[kpi?.kpiId] = [];
    if (event && Object.keys(event)?.length !== 0 && typeof event === 'object') {
        for (const key in event) {
            if (event[key]?.length == 0) {
                delete event[key];
                this.kpiSelectedFilterObj[kpi?.kpiId] = event;
            }else{
                for(let i = 0;i<event[key]?.length;i++){
                    this.kpiSelectedFilterObj[kpi?.kpiId] = [...this.kpiSelectedFilterObj[kpi?.kpiId], event[key][i]];
                }
            }
        }
    } else {
        this.kpiSelectedFilterObj[kpi?.kpiId].push(event);
    }
    this.getChartData(kpi?.kpiId, this.ifKpiExist(kpi?.kpiId), kpi?.kpiDetail?.aggregationCriteria);
    this.service.setKpiSubFilterObj(this.kpiSelectedFilterObj);
  }

  downloadExcel(kpiId, kpiName, isKanban,additionalFilterSupport, chartType?) {
    this.exportExcelComponent.downloadExcel(kpiId, kpiName, isKanban, additionalFilterSupport,this.filterApplyData,this.filterData,false, chartType);
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

   checkSprint(value, unit, kpiId){
      if((this.kpiSelectedFilterObj[kpiId]?.hasOwnProperty('filter1') && this.kpiSelectedFilterObj[kpiId]['filter1']?.length > 0 && this.kpiSelectedFilterObj[kpiId]['filter1'][0]?.toLowerCase() !== 'overall')
      || (this.kpiSelectedFilterObj[kpiId]?.hasOwnProperty('filter2') && this.kpiSelectedFilterObj[kpiId]['filter2']?.length > 0 && this.kpiSelectedFilterObj[kpiId]['filter2'][0]?.toLowerCase() !== 'overall')){
        return '-'
      } else{
        return Math.floor(value) < value ? `>${Math.round(value)} ${unit}` : `=${value} ${unit}`;
      }
    }

  handleArrowClick(kpi, label, tableValues) {
    this.displayModal = true;
    const idx = this.ifKpiExist(kpi?.kpiId);
    this.modalDetails['tableHeadings'] = this.allKpiArray[idx]?.modalHeads;
    this.modalDetails['header'] = kpi?.kpiName + ' / ' + label;
    this.modalDetails['tableValues'] = tableValues;
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
    return aggregatedArr;
  }
  generateExcel() {
      const kpiData = {
        headerNames: [],
        excelData: []
      };
      this.modalDetails['tableHeadings'].forEach(colHeader => {
        kpiData.headerNames.push({
          header: colHeader,
          key: colHeader,
          width: 25
        });
      });
      this.modalDetails['tableValues'].forEach(colData => {
        kpiData.excelData.push({ ...colData, ['Issue Id']: { text: colData['Issue Id'], hyperlink: colData['Issue URL'] } })
      });

      this.excelService.generateExcel(kpiData, this.modalDetails['header']);
    }
   typeOf(value) {
       return typeof value === 'object' && value !== null;
   }
  ngOnDestroy() {
    this.subscriptions.forEach(subscription => subscription.unsubscribe());
    this.sharedObject = null;
    this.globalConfig = null;
  }
}
