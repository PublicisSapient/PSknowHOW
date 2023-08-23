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
  filterApplyData;
  kpiJenkins;
  loaderJenkins = false;
  subscriptions: any[] = [];
  jenkinsKpiRequest;
  tooltip;
  selectedtype = 'Scrum';
  configGlobalData;
  kanbanActivated = false;
  serviceObject = {};
  allKpiArray: any = [];
  colorObj = {};
  chartColorList: Array<string> = ['#079FFF', '#00E6C3', '#CDBA38', '#FC6471', '#BD608C', '#7D5BA6'];
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
  summaryObj = {
    'kpi116': {
      'label': 'Change Failure Rate',
      'value': '0-15%'
    },
    'kpi118': {
      'label': 'Deployment Frequency',
      'value': 'Daily'
    },
  };
  updatedConfigDataObj: object = {};

  constructor(private service: SharedService, private httpService: HttpService, private helperService: HelperService) {
    this.subscriptions.push(this.service.passDataToDashboard.pipe(distinctUntilChanged()).subscribe((sharedobject) => {
      if(sharedobject?.filterData?.length && sharedobject.selectedTab.toLowerCase() === 'dora') {
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
      this.configGlobalData = globalConfig['others'].filter((item) => item.boardName.toLowerCase() == 'dora')[0]?.kpis;
      this.processKpiConfigData();
    }));
  }

  ngOnInit(): void {
    if (this.service.getFilterObject()) {
      this.serviceObject = JSON.parse(JSON.stringify(this.service.getFilterObject()));
    }

    this.httpService.getTooltipData().subscribe(filterData => {
      if (filterData[0] !== 'error') {
        this.tooltip = filterData;
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
    this.configGlobalData = this.service.getDashConfigData()['others'].filter((item) => item.boardName.toLowerCase() == 'dora')[0]?.kpis;
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
        this.groupJenkinsKpi(kpiIdsForCurrentBoard);
        this.getKpiCommentsCount();
      }
    } else {
      this.noTabAccess = true;
    }
  }

  // download excel functionality
  downloadExcel(kpiId, kpiName, isKanban,additionalFilterSupport) {
    this.exportExcelComponent.downloadExcel(kpiId, kpiName, isKanban, additionalFilterSupport,this.filterApplyData,this.filterData,false);
  }

  // Used for grouping all Jenkins kpi from master data and calling jenkins kpi.
  groupJenkinsKpi(kpiIdsForCurrentBoard) {
    this.kpiJenkins = this.helperService.groupKpiFromMaster('Jenkins', false, this.masterData, this.filterApplyData, this.filterData, kpiIdsForCurrentBoard, '', 'dora');
    if (this.kpiJenkins?.kpiList?.length > 0) {
      for(let i = 0; i<this.kpiJenkins?.kpiList?.length; i++){
        this.kpiJenkins.kpiList[i]['filterDuration'] = {
          duration:'WEEKS',
          value:5
        }
      }
      this.postJenkinsKpi(this.kpiJenkins, 'jenkins');
    }
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

  // Used for grouping all Jenkins kpi of kanban from master data and calling jenkins kpi.
  groupJenkinsKanbanKpi(kpiIdsForCurrentBoard) {
    this.kpiJenkins = this.helperService.groupKpiFromMaster('Jenkins', true, this.masterData, this.filterApplyData, this.filterData, kpiIdsForCurrentBoard, '', '');
    if (this.kpiJenkins?.kpiList?.length > 0) {
      this.postJenkinsKanbanKpi(this.kpiJenkins, 'jenkins');
    }
  }

  // calling post request of Jenkins of Kanban and storing in jenkinsKpiData id wise
  postJenkinsKanbanKpi(postData, source): void {
    this.loaderJenkins = true;
    if (this.jenkinsKpiRequest && this.jenkinsKpiRequest !== '') {
      this.jenkinsKpiRequest.unsubscribe();
    }
    this.jenkinsKpiRequest = this.httpService.postKpiKanban(postData, source)
      .subscribe(getData => {
        this.loaderJenkins = false;
        // move Overall to top of trendValueList
        if (getData !== null) { // && getData[0] !== 'error') {
          this.jenkinsKpiData = getData;
          this.createAllKpiArray(this.jenkinsKpiData);
        }
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

  getChartData(kpiId, idx, aggregationType) {
    const trendValueList = this.allKpiArray[idx]?.trendValueList;
    if (trendValueList?.length > 0 && trendValueList[0]?.hasOwnProperty('filter')) {
      if (this.kpiSelectedFilterObj[kpiId]?.length > 1) {
        const tempArr = {};
        for (let i = 0; i < this.kpiSelectedFilterObj[kpiId]?.length; i++) {
          tempArr[this.kpiSelectedFilterObj[kpiId][i]] = (trendValueList?.filter(x => x['filter'] == this.kpiSelectedFilterObj[kpiId][i])[0]?.value);
          tempArr[this.kpiSelectedFilterObj[kpiId][i]][0]['aggregationValue'] = trendValueList?.filter(x => x['filter'] == this.kpiSelectedFilterObj[kpiId][i])[0]?.aggregationValue;
        }
        this.kpiChartData[kpiId] = this.helperService.applyAggregationLogic(tempArr, aggregationType, this.tooltip.percentile);
      } else {
        if (this.kpiSelectedFilterObj[kpiId]?.length > 0) {
          this.kpiChartData[kpiId] = trendValueList?.filter(x => x['filter'] == this.kpiSelectedFilterObj[kpiId][0])[0]?.value;
          this.kpiChartData[kpiId][0]['aggregationValue'] = trendValueList?.filter(x => x['filter'] == this.kpiSelectedFilterObj[kpiId][0])[0]?.aggregationValue;
        } else {
          this.kpiChartData[kpiId] = trendValueList?.filter(x => x['filter'] == 'Overall')[0]?.value;
          this.kpiChartData[kpiId][0]['aggregationValue'] = trendValueList?.filter(x => x['filter'] == 'Overall')[0]?.aggregationValue;
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
    // if (this.kpiChartData && Object.keys(this.kpiChartData) && Object.keys(this.kpiChartData).length === this.updatedConfigGlobalData.length) {
    if (this.kpiChartData && Object.keys(this.kpiChartData).length && this.updatedConfigGlobalData) {
      this.helperService.calculateGrossMaturity(this.kpiChartData, this.updatedConfigGlobalData);
    }
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

}
