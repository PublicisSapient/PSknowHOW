import { Component, OnDestroy, OnInit } from '@angular/core';
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
  kpiExcelData;

  constructor(private service: SharedService, private httpService: HttpService, private excelService: ExcelService, private helperService: HelperService) {
    // this.kanbanActivated = false;
    this.service.setSelectedType('Scrum');
    this.subscriptions.push(this.service.passDataToDashboard.subscribe((sharedobject) => {
      if(sharedobject?.filterData?.length && sharedobject.selectedTab.toLowerCase() === 'backlog') {
        this.allKpiArray = [];
        this.receiveSharedData(sharedobject);
        this.noTabAccess = false;
      } else {
        this.noTabAccess = true;
    }
    }));

    // used to know whether scrum or kanban is clicked
    this.subscriptions.push(this.service.onTypeRefresh.subscribe((sharedobject) => {
      this.getSelectedType(sharedobject);
      if (this.service.getDashConfigData() && Object.keys(this.service.getDashConfigData()).length > 0) {
        this.configGlobalData = this.service.getDashConfigData()['others'].filter((item) => item.boardName.toLowerCase() == 'backlog')[0]?.kpis;
        this.processKpiConfigData();
      }
    }));

    if (this.service.getDashConfigData() && Object.keys(this.service.getDashConfigData()).length > 0) {
      this.configGlobalData = this.service.getDashConfigData()['others'].filter((item) => item.boardName.toLowerCase() == 'backlog')[0]?.kpis;
      this.processKpiConfigData();
    }

    this.subscriptions.push(this.service.globalDashConfigData.subscribe((globalConfig) => {
      this.configGlobalData = globalConfig['others'].filter((item) => item.boardName.toLowerCase() == 'backlog')[0]?.kpis;
      this.processKpiConfigData();
    }));

    this.subscriptions.push(this.service.noSprintsObs.subscribe((res) => {
      this.noSprints = res;
    }));
  }
  ngOnInit() {
    this.selectedtype = this.service.getSelectedType();

    this.service.selectTab('Backlog');
    if (this.service.getFilterObject()) {
      this.receiveSharedData(this.service.getFilterObject());
    }

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
        this.kpiJira = this.helperService.groupKpiFromMaster('Jira', false, this.masterData, this.filterApplyData, this.filterData, kpiIdsForCurrentBoard, groupId);
        this.postJiraKpi(this.kpiJira, 'jira');
      }
    });

  }
   // post request of Jira(scrum) hygiene
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

  generateKpiProgressBarData(kpiData){
    if (kpiData?.value && kpiData.value.length > 0 ) {
          if (kpiData.value[0]?.hoverValue) {
            const total=0;
            Object.keys(kpiData.value[0].hoverValue)?.forEach((key) => {
              if(key?.toLowerCase()?.includes('total')){
                kpiData.value[0].maxValue= kpiData?.value[0]?.hoverValue[key];
              }else{
                kpiData.value[0].value=kpiData?.value[0]?.hoverValue[key];
              }
            });
          }
        }
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
        this.kpiZypher = this.helperService.groupKpiFromMaster('Zypher', false, this.masterData, this.filterApplyData, this.filterData, kpiIdsForCurrentBoard, groupId);
        this.postZypherKpi(this.kpiZypher, 'zypher');
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
        this.createAllKpiArray(this.zypherKpiData);
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
        if (this.getChartType(kpiId) === 'progress-bar') {
          this.generateKpiProgressBarData(this.kpiChartData[kpiId][0]);
        }
      }
    } else {
      if (trendValueList?.length > 0) {
        this.kpiChartData[kpiId] = [...this.sortAlphabetically(trendValueList)];
        if (this.getChartType(kpiId) === 'progress-bar') {
          this.generateKpiProgressBarData(this.kpiChartData[kpiId][0]);
        }
      } else {
        this.kpiChartData[kpiId] = [];
      }
    }

    // if (this.kpiChartData && Object.keys(this.kpiChartData) && Object.keys(this.kpiChartData).length === this.updatedConfigGlobalData.length) {
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

  createAllKpiArray(data) {
    for (const key in data) {
      const idx = this.ifKpiExist(data[key]?.kpiId);
      if (idx == -1) {
        this.allKpiArray.push(data[key]);
        const trendValueList = this.allKpiArray[this.allKpiArray?.length - 1]?.trendValueList;
        if (trendValueList?.length > 0 && trendValueList[0]?.hasOwnProperty('filter')) {
          this.kpiSelectedFilterObj[data[key]?.kpiId] = [];
          if (key === 'kpi3') {
            this.kpiSelectedFilterObj[data[key]?.kpiId]?.push('Lead Time');
          } else {
            this.kpiSelectedFilterObj[data[key]?.kpiId]?.push('Overall');
          }
          this.service.setKpiSubFilterObj(this.kpiSelectedFilterObj);
          this.getDropdownArray(data[key]?.kpiId);
        }

        const agType = this.updatedConfigGlobalData?.filter(x => x.kpiId == data[key]?.kpiId)[0]?.kpiDetail?.aggregationCriteria;

        this.getChartData(data[key]?.kpiId, (this.allKpiArray?.length - 1), agType);
      }
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
            obj['filterType'] = 'Select a filter',
            obj['options'] = optionsArr;
            this.kpiDropdowns[kpiId] = [];
            this.kpiDropdowns[kpiId].push(obj);
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

  downloadExcel(kpiId, kpiName, isKanban) {
    const sprintIncluded = ['CLOSED'];
    this.helperService.downloadExcel(kpiId, kpiName, isKanban, this.filterApplyData, this.filterData, sprintIncluded).subscribe(getData => {
        this.kpiExcelData=this.excelService.generateExcelModalData(getData);
        this.modalDetails['tableHeadings'] = this.kpiExcelData.headerNames;
        this.modalDetails['header'] = kpiName;
        this.modalDetails['tableValues'] = JSON.parse(JSON.stringify(this.kpiExcelData.excelData)).map(data => {
            if(data.hasOwnProperty('rowSpan')){
                delete data.rowSpan;
            }
            return Object.values(data);
        });
        this.displayModal = true;
    });
}

exportExcel(kpiName){
this.excelService.generateExcel(this.kpiExcelData,kpiName);
}

checkIfArray(arr){
    return Array.isArray(arr);
}

  ngOnDestroy() {
    this.subscriptions.forEach(subscription => subscription.unsubscribe());
  }
}
