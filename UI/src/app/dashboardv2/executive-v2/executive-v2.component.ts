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
import { Component, OnInit, OnDestroy, ViewChild, OnChanges, SimpleChanges } from '@angular/core';
import { HttpService } from '../../services/http.service';
import { SharedService } from '../../services/shared.service';
import { HelperService } from '../../services/helper.service';
import { faList, faChartPie } from '@fortawesome/free-solid-svg-icons';
import { ActivatedRoute } from '@angular/router';
import { distinctUntilChanged, mergeMap } from 'rxjs/operators';
import { ExportExcelComponent } from 'src/app/component/export-excel/export-excel.component';

@Component({
  selector: 'app-executive-v2',
  templateUrl: './executive-v2.component.html',
  styleUrls: ['./executive-v2.component.css']
})
export class ExecutiveV2Component implements OnInit, OnDestroy {
  @ViewChild('exportExcel') exportExcelComponent: ExportExcelComponent;
  filterData = [];
  sonarKpiData = {};
  jenkinsKpiData = {};
  zypherKpiData = {};
  jiraKpiData = {};
  bitBucketKpiData = {};
  filterApplyData;
  kpiListSonar;
  kpiJenkins;
  kpiZypher;
  kpiJira;
  kpiBitBucket;
  loaderJenkins = false;
  faList = faList;
  faChartPie = faChartPie;
  subscriptions: any[] = [];
  noOfFilterSelected = 0;
  jiraKpiRequest;
  sonarKpiRequest;
  zypherKpiRequest;
  jenkinsKpiRequest;
  bitBucketKpiRequest;
  maturityColorCycleTime = ['#f5f5f5', '#f5f5f5', '#f5f5f5'];
  tooltip;
  selectedtype = 'Scrum';
  configGlobalData;
  selectedPriorityFilter = {};
  selectedSonarFilter;
  selectedTestExecutionFilterData;
  sonarFilterData = [];
  testExecutionFilterData = [];
  selectedJobFilter = 'Select';
  selectedBranchFilter = 'Select';
  processedKPI11Value = {};
  kanbanActivated = false;
  serviceObject = {};
  isChartView = true;
  allKpiArray: any = [];
  colorObj = {};
  chartColorList = {};
  kpiSelectedFilterObj = {};
  kpiChartData = {};
  kpiThresholdObj = {};
  noKpis = false;
  noFilterApplyData = false;
  enableByUser = false;
  updatedConfigGlobalData;
  kpiConfigData = {};
  kpiLoader = new Set();
  kpiStatusCodeArr = {};
  noTabAccess = false;
  trendBoxColorObj: any;
  iSAdditionalFilterSelected = false;
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
  selectedTab = '';
  showCommentIcon = false;
  noProjects = false;
  sprintsOverlayVisible: boolean = false;
  kpiCommentsCountObj: object = {};
  kpiTableHeadingArr: Array<object> = [];
  kpiTableDataObj: object = {};
  noOfDataPoints: number;
  maturityTableKpiList = [];
  loading: boolean = false;
  tabsArr = new Set();
  selectedKPITab: string;
  additionalFiltersArr = {};
  isRecommendationsEnabled: boolean = false;
  kpiList: Array<string> = [];
  releaseEndDate: string = '';
  timeRemaining = 0;
  immediateLoader = true;
  projectCount: number = 0;
  globalConfig: any;
  kpiTrendObject = {};
  durationFilter = 'Past 6 Months';
  constructor(public service: SharedService, private httpService: HttpService, private helperService: HelperService, private route: ActivatedRoute) {
    const selectedTab = window.location.hash.substring(1);
    this.selectedTab = selectedTab?.split('/')[2] ? selectedTab?.split('/')[2] : 'my-knowhow';

    this.subscriptions.push(this.service.onScrumKanbanSwitch.subscribe((data) => {
      this.noFilterApplyData = false;
      this.kpiLoader = new Set();
      this.kpiStatusCodeArr = {};
      this.immediateLoader = true;
      this.processedKPI11Value = {};
      this.selectedBranchFilter = 'Select';
      this.serviceObject = {};
      this.selectedtype = data.selectedType;
      this.kpiTrendObject = {}
      this.kanbanActivated = this.selectedtype.toLowerCase() === 'kanban' ? true : false;
    }));

    this.subscriptions.push(this.service.onTabSwitch.subscribe((data) => {
      this.noFilterApplyData = false;
      this.kpiLoader = new Set();
      this.kpiStatusCodeArr = {};
      this.immediateLoader = true;
      this.processedKPI11Value = {};
      this.selectedBranchFilter = 'Select';
      this.serviceObject = {};
      this.selectedTab = data.selectedBoard;
    }));

    this.subscriptions.push(this.service.globalDashConfigData.subscribe((globalConfig) => {
      this.globalConfig = globalConfig;
      this.configGlobalData = globalConfig[this.kanbanActivated ? 'kanban' : 'scrum'].filter((item) => (item.boardSlug?.toLowerCase() === this.selectedTab.toLowerCase()) || (item.boardName.toLowerCase() === this.selectedTab.toLowerCase().split('-').join(' ')))[0]?.kpis;
      this.updatedConfigGlobalData = this.configGlobalData?.filter(item => item.shown);
      setTimeout(() => {
        this.processKpiConfigData();
      }, 500);
    }));


    this.subscriptions.push(this.service.noSprintsObs.subscribe((res) => {
      this.noFilterApplyData = res;
    }));

    this.subscriptions.push(this.service.mapColorToProject.pipe(mergeMap(x => {
      this.maturityTableKpiList = [];
      this.colorObj = x;
      this.trendBoxColorObj = { ...x };
      let tempObj = {};
      for (const key in this.trendBoxColorObj) {
        const idx = key.lastIndexOf('_');
        const nodeName = key.slice(0, idx);
        this.trendBoxColorObj[nodeName] = this.trendBoxColorObj[key];
        tempObj[nodeName] = [];
      }
      this.projectCount = Object.keys(this.trendBoxColorObj)?.length;
      this.kpiTableDataObj = { ...tempObj };
      if (!this.kpiChartData || Object.keys(this.kpiChartData)?.length <= 0) return this.service.passDataToDashboard;
      for (const key in this.kpiChartData) {
        this.kpiChartData[key] = this.generateColorObj(key, this.kpiChartData[key]);
        this.createTrendsData(key);
        this.handleMaturityTableLoader();
      }
      return this.service.passDataToDashboard;
    }), distinctUntilChanged()).subscribe((sharedobject: any) => {
      // used to get all filter data when user click on apply button in filter
      this.maturityTableKpiList = [];
      if (!sharedobject?.filterData?.length) {
        this.noTabAccess = true;
        return;
      }
      this.serviceObject = JSON.parse(JSON.stringify(sharedobject));
      this.iSAdditionalFilterSelected = sharedobject?.isAdditionalFilters;
      this.receiveSharedData(sharedobject);
      this.noTabAccess = false;
      this.handleMaturityTableLoader();
    }));

    /**observable to get the type of view */
    this.subscriptions.push(this.service.showTableViewObs.subscribe(view => {
      this.showChart = view;
    }));
  }

  processKpiConfigData() {
    const disabledKpis = this.configGlobalData?.filter(item => item.shown && !item.isEnabled);
    // user can enable kpis from show/hide filter, added below flag to show different message to the user
    this.enableByUser = disabledKpis?.length ? true : false;
    // noKpis - if true, all kpis are not shown to the user (not showing kpis to the user)
    this.updatedConfigGlobalData = this.configGlobalData?.filter(item => item.shown);
    this.kpiList = this.configGlobalData?.map((kpi) => kpi.kpiId)
    if (this.updatedConfigGlobalData?.length === 0) {
      this.noKpis = true;
    } else {
      this.noKpis = false;
    }
    this.maturityTableKpiList = []
    this.configGlobalData?.forEach(element => {
      if (element.shown && element.isEnabled) {
        this.kpiConfigData[element.kpiId] = true;
        if (!this.kpiTrendsObj.hasOwnProperty(element.kpiId)) {
          this.createTrendsData(element.kpiId);
          this.handleMaturityTableLoader();
        }
      } else {
        this.kpiConfigData[element.kpiId] = false;
      }
    });
  }


  ngOnInit() {
    this.subscriptions.push(this.service.noProjectsObs.subscribe((res) => {
      this.noProjects = res;
      this.kanbanActivated = this.service.getSelectedType()?.toLowerCase() === 'kanban' ? true : false;
    }));

    /** Get recommendations flag */
    this.subscriptions.push(this.service.isRecommendationsEnabledObs.subscribe(item => {
      this.isRecommendationsEnabled = item;
    }));

    this.service.getEmptyData().subscribe((val) => {
      if (val) {
        this.noTabAccess = true;
      } else {
        this.noTabAccess = false;
      }
    });

    if (this.selectedTab.toLowerCase() === 'developer') {
      this.subscriptions.push(this.service.triggerAdditionalFilters.subscribe((data) => {
        Object.keys(data)?.length && this.updatedConfigGlobalData.forEach(kpi => {
          this.handleSelectedOption(data, kpi);
        });
      }));
    }
  }

  // unsubscribing all Kpi Request
  ngOnDestroy() {
    this.subscriptions.forEach(subscription => subscription.unsubscribe());
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
    if ($event.dashConfigData && Object.keys($event.dashConfigData).length > 0 && $event?.selectedTab?.toLowerCase() !== 'iteration') {
      this.filterData = $event.filterData;
      this.filterApplyData = $event.filterApplyData;
      this.globalConfig = $event.dashConfigData;
      this.configGlobalData = $event.dashConfigData[this.kanbanActivated ? 'kanban' : 'scrum'].filter((item) => (item.boardName.toLowerCase() === $event?.selectedTab?.toLowerCase()) || (item.boardName.toLowerCase() === $event?.selectedTab?.toLowerCase().split('-').join(' ')))[0]?.kpis
      const selectedRelease = this.filterData?.filter(x => x.nodeId === this.filterApplyData?.selectedMap?.release?.[0] && x.labelName.toLowerCase() === 'release')[0];
      const endDate = selectedRelease !== undefined ? new Date(selectedRelease?.releaseEndDate).toISOString().split('T')[0] : undefined;
      this.releaseEndDate = endDate;
      const today = new Date().toISOString().split('T')[0];
      this.timeRemaining = this.calcBusinessDays(today, endDate);
      this.service.iterationCongifData.next({ daysLeft: this.timeRemaining });
      if (!this.configGlobalData?.length && $event.dashConfigData) {
        this.configGlobalData = $event.dashConfigData[this.kanbanActivated ? 'kanban' : 'scrum'].filter((item) => (item.boardSlug.toLowerCase() === $event?.selectedTab?.toLowerCase()) || (item.boardName.toLowerCase() === $event?.selectedTab?.toLowerCase().split('-').join(' ')))[0]?.kpis;
        if (!this.configGlobalData) {
          this.configGlobalData = $event.dashConfigData['others'].filter((item) => (item.boardSlug.toLowerCase() === $event?.selectedTab?.toLowerCase()) || (item.boardName.toLowerCase() === $event?.selectedTab?.toLowerCase().split('-').join(' ')))[0]?.kpis;
        }
      }

      this.updatedConfigGlobalData = this.configGlobalData?.filter(item => item.shown);

      this.tooltip = $event.configDetails;
      this.additionalFiltersArr = {};
      this.noOfDataPoints = this.selectedTab.toLowerCase() !== 'developer' && this.coundMaxNoOfSprintSelectedForProject($event);
      this.allKpiArray = [];
      this.kpiChartData = {};
      this.chartColorList = {};
      this.kpiSelectedFilterObj = {};
      this.kpiDropdowns = {};
      this.kpiTrendsObj = {};
      this.kpiTableDataObj = {};
      this.kpiLoader = new Set();
      this.kpiStatusCodeArr = {};
      this.immediateLoader = true;
      for (const key in this.colorObj) {
        const idx = key.lastIndexOf('_');
        const nodeName = key.slice(0, idx);
        this.kpiTableDataObj[nodeName] = [];
      }

      if (!$event.filterApplyData['ids'] || !$event.filterApplyData['ids']?.length || !$event.filterApplyData['ids'][0]) {
        this.noFilterApplyData = true;
      } else {
        this.noFilterApplyData = false;
        this.filterData = $event.filterData;
        this.filterApplyData = $event.filterApplyData;
        this.noOfFilterSelected = Object.keys(this.filterApplyData).length;

        // if (this.filterApplyData?.selectedMap['date']) {
        //   this.updatedConfigGlobalData?.forEach((kpi) => {
        //     kpi.kpiDetail.xaxisLabel = this.filterApplyData.selectedMap['date'][0];
        //   });
        // }

        this.selectedJobFilter = 'Select';
        this.loading = $event.loading;
        if (this.filterData?.length && $event.makeAPICall) {
          this.noTabAccess = false;
          // call kpi request according to tab selected
          if (this.configGlobalData?.length > 0) {
            this.processKpiConfigData();
            const kpiIdsForCurrentBoard = this.configGlobalData?.map(kpiDetails => kpiDetails.kpiId);
            // set up dynamic tabs
            this.setUpTabs();
            if (this.service.getSelectedType().toLowerCase() === 'kanban') {
              this.groupJiraKanbanKpi(kpiIdsForCurrentBoard);
              this.groupSonarKanbanKpi(kpiIdsForCurrentBoard);
              this.groupJenkinsKanbanKpi(kpiIdsForCurrentBoard);
              this.groupZypherKanbanKpi(kpiIdsForCurrentBoard);
              this.groupBitBucketKanbanKpi(kpiIdsForCurrentBoard);
            } else {
              this.groupJiraKpi(kpiIdsForCurrentBoard);
              this.groupSonarKpi(kpiIdsForCurrentBoard);
              this.groupJenkinsKpi(kpiIdsForCurrentBoard);
              this.groupZypherKpi(kpiIdsForCurrentBoard);
              this.groupBitBucketKpi(kpiIdsForCurrentBoard)
            }
            this.immediateLoader = false;
            this.createKpiTableHeads(this.selectedtype.toLowerCase());

            let projectLevel = this.filterData.filter((x) => x.labelName == 'project')[0]?.level;
            if (projectLevel) {
              if (this.filterApplyData.level == projectLevel) this.getKpiCommentsCount();
            }
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
    }
  }

  setUpTabs() {
    const tabsArray = new Set(this.configGlobalData.map(element => element?.kpiDetail?.kpiSubCategory));
    // const tempArray = [...this.service.getDashConfigData()['scrum'], ...this.service.getDashConfigData()['others']];
    // const tabTempSet = tempArray.filter(element => tabsArray.has(element.boardName));
    // this.tabsArr = new Set(tabTempSet.map(element => element.boardName));
    this.tabsArr = tabsArray;
    let it = this.tabsArr.values();
    //get first entry:
    let first = it.next();
    //get value out of the iterator entry:
    let value = first.value;
    this.selectedKPITab = value;
  }

  selectKPITab(tab) {
    this.selectedKPITab = tab;
  }


  // download excel functionality
  downloadExcel(kpiId, kpiName, isKanban, additionalFilterSupport) {
    this.exportExcelComponent.downloadExcel(kpiId, kpiName, isKanban, additionalFilterSupport, this.filterApplyData, this.filterData, this.iSAdditionalFilterSelected);
  }


  // Used for grouping all Sonar kpi from master data and calling Sonar kpi.
  groupSonarKpi(kpiIdsForCurrentBoard) {
    this.kpiListSonar = this.helperService.groupKpiFromMaster('Sonar', false, this.updatedConfigGlobalData, this.filterApplyData, this.filterData, kpiIdsForCurrentBoard, '', '');
    if (this.kpiListSonar?.kpiList?.length > 0) {
      let kpiArr = this.kpiListSonar.kpiList.map((kpi: { kpiId: any; }) => kpi.kpiId);
      kpiArr.forEach(element => this.kpiLoader.add(element));
      this.postSonarKpi(this.kpiListSonar, 'sonar');
    }
  }

  // Used for grouping all Jenkins kpi from master data and calling jenkins kpi.
  groupJenkinsKpi(kpiIdsForCurrentBoard) {
    this.kpiJenkins = this.helperService.groupKpiFromMaster('Jenkins', false, this.updatedConfigGlobalData, this.filterApplyData, this.filterData, kpiIdsForCurrentBoard, '', '');
    if (this.kpiJenkins?.kpiList?.length > 0) {
      let kpiArr = this.kpiJenkins.kpiList.map((kpi: { kpiId: any; }) => kpi.kpiId);
      kpiArr.forEach(element => this.kpiLoader.add(element));
      this.postJenkinsKpi(this.kpiJenkins, 'jenkins');
    }
  }

  // Used for grouping all Sonar kpi from master data and calling Sonar kpi.
  groupZypherKpi(kpiIdsForCurrentBoard) {
    // creating a set of unique group Ids
    const groupIdSet = new Set();
    this.updatedConfigGlobalData?.forEach((obj) => {
      if (!obj['kpiDetail'].kanban && obj['kpiDetail'].kpiSource === 'Zypher') {
        groupIdSet.add(obj['kpiDetail'].groupId);
      }
    });
    // sending requests after grouping the the KPIs according to group Id
    groupIdSet.forEach((groupId) => {
      if (groupId) {
        this.kpiZypher = this.helperService.groupKpiFromMaster('Zypher', false, this.updatedConfigGlobalData, this.filterApplyData, this.filterData, kpiIdsForCurrentBoard, groupId, '',);
        if (this.kpiZypher?.kpiList?.length > 0) {
          let kpiArr = this.kpiZypher.kpiList.map((kpi: { kpiId: any; }) => kpi.kpiId);
          kpiArr.forEach(element => this.kpiLoader.add(element));
          this.postZypherKpi(this.kpiZypher, 'zypher');
        }
      }
    });
  }

  // Used for grouping all Sonar kpi from master data and calling Sonar kpi.(only for scrum).
  groupJiraKpi(kpiIdsForCurrentBoard) {
    this.jiraKpiData = {};
    // creating a set of unique group Ids
    const groupIdSet = new Set();
    this.updatedConfigGlobalData?.forEach((obj) => {
      if (!obj['kpiDetail'].kanban && obj['kpiDetail'].kpiSource === 'Jira') {
        groupIdSet.add(obj['kpiDetail'].groupId);
      }
    });

    // sending requests after grouping the the KPIs according to group Id   
    groupIdSet.forEach((groupId) => {
      if (groupId) {
        this.kpiJira = this.helperService.groupKpiFromMaster('Jira', false, this.updatedConfigGlobalData, this.filterApplyData, this.filterData, kpiIdsForCurrentBoard, groupId, '');
        if (this.kpiJira?.kpiList?.length > 0) {
          let kpiArr = this.kpiJira.kpiList.map((kpi: { kpiId: any; }) => kpi.kpiId);
          kpiArr.forEach(element => this.kpiLoader.add(element));
          this.postJiraKpi(this.kpiJira, 'jira');
        }
      }
    });

  }

  // Used for grouping all jira kpi of kanban from master data and calling jira kpi of kanban.
  groupJiraKanbanKpi(kpiIdsForCurrentBoard) {
    this.jiraKpiData = {};
    // creating a set of unique group Ids
    const groupIdSet = new Set();
    this.updatedConfigGlobalData?.forEach((obj) => {
      if (obj['kpiDetail'].kanban && obj['kpiDetail'].kpiSource === 'Jira') {
        groupIdSet.add(obj['kpiDetail'].groupId);
      }
    });

    // sending requests after grouping the the KPIs according to group Id
    groupIdSet.forEach((groupId) => {
      if (groupId) {
        this.kpiJira = this.helperService.groupKpiFromMaster('Jira', true, this.updatedConfigGlobalData, this.filterApplyData, this.filterData, kpiIdsForCurrentBoard, groupId, '');
        if (this.kpiJira?.kpiList?.length > 0) {
          let kpiArr = this.kpiJira.kpiList.map((kpi: { kpiId: any; }) => kpi.kpiId);
          kpiArr.forEach(element => this.kpiLoader.add(element));
          this.postJiraKanbanKpi(this.kpiJira, 'jira');
        }
      }
    });
  }
  // Used for grouping all Sonar kpi of kanban from master data and calling Sonar kpi.
  groupSonarKanbanKpi(kpiIdsForCurrentBoard) {
    this.kpiListSonar = this.helperService.groupKpiFromMaster('Sonar', true, this.updatedConfigGlobalData, this.filterApplyData, this.filterData, kpiIdsForCurrentBoard, '', '');
    if (this.kpiListSonar?.kpiList?.length > 0) {
      let kpiArr = this.kpiListSonar.kpiList.map((kpi: { kpiId: any; }) => kpi.kpiId);
      kpiArr.forEach(element => this.kpiLoader.add(element));
      this.postSonarKanbanKpi(this.kpiListSonar, 'sonar');
    }
  }

  // Used for grouping all Jenkins kpi of kanban from master data and calling jenkins kpi.
  groupJenkinsKanbanKpi(kpiIdsForCurrentBoard) {
    this.kpiJenkins = this.helperService.groupKpiFromMaster('Jenkins', true, this.updatedConfigGlobalData, this.filterApplyData, this.filterData, kpiIdsForCurrentBoard, '', '');
    if (this.kpiJenkins?.kpiList?.length > 0) {
      let kpiArr = this.kpiJenkins.kpiList.map((kpi: { kpiId: any; }) => kpi.kpiId);
      kpiArr.forEach(element => this.kpiLoader.add(element));
      this.postJenkinsKanbanKpi(this.kpiJenkins, 'jenkins');
    }
  }

  // Used for grouping all Zypher kpi of kanban from master data and calling Zypher kpi.
  groupZypherKanbanKpi(kpiIdsForCurrentBoard) {
    this.kpiZypher = this.helperService.groupKpiFromMaster('Zypher', true, this.updatedConfigGlobalData, this.filterApplyData, this.filterData, kpiIdsForCurrentBoard, '', '');
    if (this.kpiZypher?.kpiList?.length > 0) {
      let kpiArr = this.kpiZypher.kpiList.map((kpi: { kpiId: any; }) => kpi.kpiId);
      kpiArr.forEach(element => this.kpiLoader.add(element));
      this.postZypherKanbanKpi(this.kpiZypher, 'zypher');
    }
  }

  // Used for grouping all BitBucket kpi of kanban from master data and calling BitBucket kpi.
  groupBitBucketKanbanKpi(kpiIdsForCurrentBoard) {
    this.kpiBitBucket = this.helperService.groupKpiFromMaster('BitBucket', true, this.updatedConfigGlobalData, this.filterApplyData, this.filterData, kpiIdsForCurrentBoard, '', '');
    if (this.kpiBitBucket?.kpiList?.length > 0) {
      let kpiArr = this.kpiBitBucket.kpiList.map((kpi: { kpiId: any; }) => kpi.kpiId);
      kpiArr.forEach(element => this.kpiLoader.add(element));
      this.postBitBucketKanbanKpi(this.kpiBitBucket, 'bitbucket');
    }
  }

  // Used for grouping all BitBucket kpi of scrum from master data and calling BitBucket kpi.
  groupBitBucketKpi(kpiIdsForCurrentBoard) {
    this.kpiBitBucket = this.helperService.groupKpiFromMaster('BitBucket', false, this.updatedConfigGlobalData, this.filterApplyData, this.filterData, kpiIdsForCurrentBoard, '', '');
    if (this.kpiBitBucket?.kpiList?.length > 0) {
      let kpiArr = this.kpiBitBucket.kpiList.map((kpi: { kpiId: any; }) => kpi.kpiId);
      kpiArr.forEach(element => this.kpiLoader.add(element));
      this.postBitBucketKpi(this.kpiBitBucket, 'bitbucket');
    }
  }

  // calls after receiving response from sonar
  afterSonarKpiResponseReceived(getData, postData) {
    this.sonarFilterData.length = 0;
    if (getData !== null && getData[0] !== 'error' && !getData['error']) {
      // creating array into object where key is kpi id
      this.sonarKpiData = this.helperService.createKpiWiseId(getData);
      this.fillKPIResponseCode(this.sonarKpiData);
      // creating Sonar filter and finding unique keys from all the sonar kpis
      this.sonarFilterData = this.helperService.createSonarFilter(this.sonarKpiData, this.selectedtype);
      /** writing hack for unit test coverage kpi */
      if (this.sonarKpiData['kpi17']?.trendValueList?.length > 0) {
        let overallObj = {
          'filter': 'Overall',
          'value': []
        }
        for (let i = 0; i < this.sonarKpiData['kpi17']?.trendValueList?.length; i++) {
          for (let j = 0; j < this.sonarKpiData['kpi17']?.trendValueList[i]?.value?.length; j++) {
            let obj = {
              'filter': this.sonarKpiData['kpi17']?.trendValueList[i]?.filter,
              ...this.sonarKpiData['kpi17']?.trendValueList[i]?.value[j]
            }
            overallObj['value'].push(obj);
          }
        }
        this.sonarKpiData['kpi17']?.trendValueList.push(overallObj);
      }
      this.createAllKpiArray(this.sonarKpiData);
      this.removeLoaderFromKPIs(this.sonarKpiData);
    } else {
      this.sonarKpiData = getData;
      postData.kpiList.forEach(element => {
        this.kpiLoader.delete(element.kpiId);
      });

    }
  }

  // calls after receiving response from zypher
  afterZypherKpiResponseReceived(getData, postData) {
    this.testExecutionFilterData.length = 0;
    this.selectedTestExecutionFilterData = {};
    if (getData !== null && getData[0] !== 'error' && !getData['error']) {
      // creating array into object where key is kpi id
      this.zypherKpiData = this.helperService.createKpiWiseId(getData);
      this.fillKPIResponseCode(this.zypherKpiData);
      let calculatedObj;
      if (this.selectedtype !== 'Kanban') {
        calculatedObj = this.helperService.calculateTestExecutionData('kpi70', false, this.zypherKpiData);
      } else {
        calculatedObj = this.helperService.calculateTestExecutionData('kpi71', false, this.zypherKpiData);
      }
      this.selectedTestExecutionFilterData = calculatedObj['selectedTestExecutionFilterData'];
      this.testExecutionFilterData = calculatedObj['testExecutionFilterData'];

      this.createAllKpiArray(this.zypherKpiData);
      this.removeLoaderFromKPIs(this.zypherKpiData);
    } else {
      this.zypherKpiData = getData;
      postData.kpiList.forEach(element => {
        this.kpiLoader.delete(element.kpiId);
      });
    }
  }

  // calling post request of sonar of scrum and storing in sonarKpiData id wise
  postSonarKpi(postData, source): void {
    if (this.sonarKpiRequest && this.sonarKpiRequest !== '') {
      this.sonarKpiRequest.unsubscribe();
    }
    this.sonarKpiRequest = this.httpService.postKpi(postData, source)
      .subscribe(getData => {
        this.afterSonarKpiResponseReceived(getData, postData);
      });
  }
  // calling post request of sonar of Kanban and storing in sonarKpiData id wise
  postSonarKanbanKpi(postData, source): void {
    if (this.sonarKpiRequest && this.sonarKpiRequest !== '') {
      this.sonarKpiRequest.unsubscribe();
    }
    this.sonarKpiRequest = this.httpService.postKpiKanban(postData, source)
      .subscribe(getData => {
        this.afterSonarKpiResponseReceived(getData, postData);
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
          this.removeLoaderFromKPIs(this.jenkinsKpiData);

          for (const obj in getData) {
            getData[getData[obj].kpiId] = getData[obj];
          }
          this.fillKPIResponseCode(getData);
        } else {
          postData.kpiList.forEach(element => {
            this.kpiLoader.delete(element.kpiId);
          });
        }
      });
  }

  // Keep 'Select' on top
  originalOrder = (a, b): number => a.key === 'Select' ? -1 : a.key;

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
          this.removeLoaderFromKPIs(this.jenkinsKpiData);

          for (const obj in getData) {
            getData[getData[obj].kpiId] = getData[obj];
          }
          this.fillKPIResponseCode(getData);
        } else {
          postData.kpiList.forEach(element => {
            this.kpiLoader.delete(element.kpiId);
          });
        }
      });
  }

  // calling post request of Zypher(scrum)
  postZypherKpi(postData, source): void {
    this.zypherKpiRequest = this.httpService.postKpi(postData, source)
      .subscribe(getData => {
        this.afterZypherKpiResponseReceived(getData, postData);
      });
  }
  // calling post request of Zypher(kanban)
  postZypherKanbanKpi(postData, source): void {
    if (this.zypherKpiRequest && this.zypherKpiRequest !== '') {
      this.zypherKpiRequest.unsubscribe();
      postData?.kpiList?.forEach(element => {
        this.kpiLoader.delete(element.kpiId);
      });
    }
    this.zypherKpiRequest = this.httpService.postKpiKanban(postData, source)
      .subscribe(getData => {
        this.afterZypherKpiResponseReceived(getData, postData);
      });
  }

  // post request of Jira(scrum)
  postJiraKpi(postData, source): void {
    if (this.selectedTab !== 'release' && this.selectedTab !== 'backlog') {
      this.jiraKpiRequest = this.httpService.postKpi(postData, source)
        .subscribe(getData => {
          if (getData !== null && getData[0] !== 'error' && !getData['error']) {

            const releaseFrequencyInd = getData.findIndex(de => de.kpiId === 'kpi73')
            if (releaseFrequencyInd !== -1) {
              getData[releaseFrequencyInd].trendValueList?.map(trendData => {
                const valueLength = trendData.value.length;
                if (valueLength > this.tooltip.sprintCountForKpiCalculation) {
                  trendData.value = trendData.value.splice(-this.tooltip.sprintCountForKpiCalculation)
                }
              })
            }
            // creating array into object where key is kpi id
            const localVariable = this.helperService.createKpiWiseId(getData);
            this.fillKPIResponseCode(localVariable);
            if (localVariable && localVariable['kpi3'] && localVariable['kpi3'].maturityValue) {
              this.colorAccToMaturity(localVariable['kpi3'].maturityValue);
            }

            this.jiraKpiData = Object.assign({}, this.jiraKpiData, localVariable);
            this.createAllKpiArray(localVariable);
            this.removeLoaderFromKPIs(localVariable);

          } else {
            this.jiraKpiData = getData;
            postData.kpiList.forEach(element => {
              this.kpiLoader.delete(element.kpiId);
            });
          }

        });
      return;
    } else if (this.selectedTab === 'release') {
      this.postJiraKPIForRelease(postData, source);
    } else if (this.selectedTab === 'backlog') {
      this.postJiraKPIForBacklog(postData, source);
    }
  }

  postJiraKPIForBacklog(postData, source) {
    const kpi171 = postData.kpiList.find(kpi => kpi.kpiId === 'kpi171');
    if (kpi171) (
      kpi171['filterDuration'] = {
        duration: 'MONTHS',
        value: 6
      });

    this.jiraKpiRequest = this.httpService.postKpiNonTrend(postData, source)
      .subscribe(getData => {
        if (getData !== null && getData[0] !== 'error' && !getData['error']) {
          // creating array into object where key is kpi id
          const localVariable = this.helperService.createKpiWiseId(getData);
          this.fillKPIResponseCode(localVariable);
          const localVarKpi = localVariable['kpi127'] || localVariable['kpi170'] || localVariable['kpi3']
          if (localVarKpi) {
            if (localVarKpi.trendValueList && localVarKpi.xAxisValues) {
              localVarKpi.trendValueList.forEach(trendElem => {
                trendElem.value.forEach(valElem => {
                  if (valElem.value.length === 5 && localVarKpi.xAxisValues.length === 5) {
                    valElem.value.forEach((element, index) => {
                      element['xAxisTick'] = localVarKpi.xAxisValues[index];
                    });
                  }
                });
              });
            }
          }

          this.jiraKpiData = Object.assign({}, this.jiraKpiData, localVariable);
          this.createAllKpiArrayForBacklog(this.jiraKpiData);
          this.removeLoaderFromKPIs(localVariable);
        } else {
          this.jiraKpiData = getData;
          postData.kpiList.forEach(element => {
            this.kpiLoader.delete(element.kpiId);
          });
        }
      });
  }


  postJiraKPIForRelease(postData, source) {
    /** Temporary Fix,  sending all KPI in kpiList when refreshing kpi after field mapping change*/
    /** Todo : Need to rework when BE cache issue will fix */
    this.updatedConfigGlobalData.forEach(kpi => {
      postData.kpiList.push(kpi.kpiDetail)
    });
    this.jiraKpiRequest = this.httpService.postKpiNonTrend(postData, source)
      .subscribe(getData => {
        if (getData !== null && getData[0] !== 'error' && !getData['error']) {
          /** creating array into object where key is kpi id */
          const localVariable = this.helperService.createKpiWiseId(getData);
          this.fillKPIResponseCode(localVariable);
          this.removeLoaderFromKPIs(localVariable);
          this.jiraKpiData = Object.assign({}, this.jiraKpiData, localVariable);
          this.createAllKpiArray(localVariable);

        } else {
          this.jiraKpiData = getData;
          postData.kpiList.forEach(element => {
            this.kpiLoader.delete(element.kpiId);
          });
        }
      });
  }


  // post request of BitBucket(scrum)
  postBitBucketKpi(postData, source): void {
    if (this.bitBucketKpiRequest && this.bitBucketKpiRequest !== '') {
      this.bitBucketKpiRequest.unsubscribe();
    }
    this.bitBucketKpiRequest = this.httpService.postKpi(postData, source)
      .subscribe(getData => {
        if (getData !== null && getData[0] !== 'error' && !getData['error']) {
          // creating array into object where key is kpi id
          this.bitBucketKpiData = this.helperService.createKpiWiseId(getData);
          this.fillKPIResponseCode(this.bitBucketKpiData);
          this.createAllKpiArray(this.bitBucketKpiData);
          this.removeLoaderFromKPIs(this.bitBucketKpiData);

        } else {
          this.bitBucketKpiData = getData;
          postData.kpiList.forEach(element => {
            this.kpiLoader.delete(element.kpiId);
          });
        }
      });
  }

  // post request of BitBucket(scrum)
  postBitBucketKanbanKpi(postData, source): void {
    if (this.bitBucketKpiRequest && this.bitBucketKpiRequest !== '') {
      this.bitBucketKpiRequest.unsubscribe();
    }
    this.bitBucketKpiRequest = this.httpService.postKpiKanban(postData, source)
      .subscribe(getData => {
        if (getData !== null && getData[0] !== 'error' && !getData['error']) {
          // creating array into object where key is kpi id
          this.bitBucketKpiData = this.helperService.createKpiWiseId(getData);
          this.fillKPIResponseCode(this.bitBucketKpiData);
          this.createAllKpiArray(this.bitBucketKpiData);
          this.removeLoaderFromKPIs(this.bitBucketKpiData);
        } else {
          this.bitBucketKpiData = getData;
          postData.kpiList.forEach(element => {
            this.kpiLoader.delete(element.kpiId);
          });
        }
      });
  }


  // post request of Jira(Kanban)
  postJiraKanbanKpi(postData, source): void {
    this.httpService.postKpiKanban(postData, source)
      .subscribe(getData => {
        if (getData !== null && getData[0] !== 'error' && !getData['error']) {
          // creating array into object where key is kpi id
          const localVariable = this.helperService.createKpiWiseId(getData);
          this.fillKPIResponseCode(localVariable);
          const kpi997 = localVariable['kpi997'];
          if (kpi997 && kpi997.trendValueList && kpi997.xAxisValues && kpi997.xAxisValues.length === 5) {
            kpi997.trendValueList.forEach(trendElem => {
              trendElem.value
                .filter(valElem => valElem.value.length === 5)
                .forEach((valElem, index) => {
                  valElem.value['xAxisTick'] = kpi997.xAxisValues[index];
                });
            });
          }

          this.jiraKpiData = Object.assign({}, this.jiraKpiData, localVariable);
          this.createAllKpiArray(localVariable);
          this.removeLoaderFromKPIs(localVariable);
        } else {
          this.jiraKpiData = getData;
          postData.kpiList.forEach(element => {
            this.kpiLoader.delete(element.kpiId);
          });
        }
      });
  }

  removeLoaderFromKPIs(data) {
    if (Array.isArray(data)) {
      let kpis = data.map(kpi => kpi.kpiId);
      kpis.forEach((kpi) => this.kpiLoader.delete(kpi));
    } else {
      for (const kpi in data) {
        this.kpiLoader.delete(kpi);
      }
    }
  }

  // returns colors according to maturity for all
  returnColorAccToMaturity(maturity) {
    return this.helperService.colorAccToMaturity(maturity);
  }
  // return colors according to maturity only for CycleTime
  colorAccToMaturity(maturityValue) {
    const maturityArray = maturityValue.toString().split('-');
    for (let index = 0; index <= 2; index++) {
      const maturity = maturityArray[index];
      this.maturityColorCycleTime[index] = this.helperService.colorAccToMaturity(maturity);
    }
  }

  changeView(text) {
    if (text == 'list') {
      this.isChartView = false;
    } else {
      this.isChartView = true;
    }
  }

  sortAlphabetically(objArray) {
    if (objArray && objArray?.length > 1) {
      objArray?.sort((a, b) => a.data?.localeCompare(b.data));
    }
    return objArray;
  }

  getChartData(kpiId, idx, aggregationType, kpiFilterChange = false) {
    const trendValueList = this.allKpiArray[idx]?.trendValueList ? JSON.parse(JSON.stringify(this.allKpiArray[idx]?.trendValueList)) : {};
    this.kpiThresholdObj[kpiId] = this.allKpiArray[idx]?.thresholdValue ? this.allKpiArray[idx]?.thresholdValue : null;


    // this block populates additional filters on developer dashboard because on developer dashboard, the
    // additional filters depend on KPI response
    let developerBopardKpis = this.globalConfig[this.kanbanActivated ? 'kanban' : 'scrum']?.filter((item) => (item.boardSlug?.toLowerCase() === 'developer') || (item.boardName.toLowerCase() === 'developer'))[0]?.kpis?.map(x => x.kpiId);
    if (this.selectedTab.toLowerCase() === 'developer' && developerBopardKpis?.includes(kpiId)) {
      if (!trendValueList?.length) {
        this.additionalFiltersArr = {};
        this.service.setAdditionalFilters(this.additionalFiltersArr);
      }
      else if (trendValueList?.length) {
        let filterPropArr = Object.keys(trendValueList[0]).filter((prop) => prop.includes('filter'));
        filterPropArr.forEach((filterProp) => {
          if (!this.additionalFiltersArr[filterProp]?.size) {
            this.additionalFiltersArr[filterProp] = new Set();
          }
          trendValueList.map((x) => x[filterProp]).forEach((f) => this.additionalFiltersArr[filterProp].add(f));
          this.additionalFiltersArr[filterProp] = Array.from(this.additionalFiltersArr[filterProp]).map((item: string) => (item));
        });

        if (!kpiFilterChange) {
          Object.keys(this.additionalFiltersArr).forEach((filterProp) => {
            this.additionalFiltersArr[filterProp] = this.additionalFiltersArr[filterProp].map((f) => {
              return {
                nodeId: f,
                nodeName: f
              }
            })
          });
          this.service.setAdditionalFilters(this.additionalFiltersArr);
        }
      }
    }

    if (trendValueList?.length > 0) {
      let filterPropArr = Object.keys(trendValueList[0])?.filter((prop) => prop.includes('filter'));
      if (filterPropArr?.length) {
        if (filterPropArr.includes('filter')) {
          if (Object.keys(this.kpiSelectedFilterObj[kpiId])?.length > 1) {
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
            }
            else {
              const tempArr = {};
              if (Array.isArray(this.kpiSelectedFilterObj[kpiId])) {
                for (let i = 0; i < this.kpiSelectedFilterObj[kpiId]?.length; i++) {
                  tempArr[this.kpiSelectedFilterObj[kpiId][i]] = (trendValueList?.filter(x => x['filter'] == this.kpiSelectedFilterObj[kpiId][i])[0]?.value);
                }
              } else {
                tempArr[this.kpiSelectedFilterObj[kpiId]] = (trendValueList?.filter(x => x['filter'] == this.kpiSelectedFilterObj[kpiId])[0]?.value);
              }
              this.kpiChartData[kpiId] = this.helperService.applyAggregationLogic(tempArr, aggregationType, this.tooltip.percentile);
            }
          } else {
            if (Object.keys(this.kpiSelectedFilterObj[kpiId])?.length > 0) {
              Object.keys(this.kpiSelectedFilterObj[kpiId]).forEach(key => {
                this.kpiChartData[kpiId] = trendValueList?.filter(x => x['filter'] == this.kpiSelectedFilterObj[kpiId][key])[0]?.value;
              });

              if (kpiId == 'kpi17' && this.kpiSelectedFilterObj[kpiId][0]?.toLowerCase() == 'average coverage') {
                for (let i = 0; i < this.kpiChartData[kpiId]?.length; i++) {
                  this.kpiChartData[kpiId][i]['filter'] = this.kpiSelectedFilterObj[kpiId][0];
                }
              }
            } else {
              this.kpiChartData[kpiId] = trendValueList?.filter(x => x['filter'] == 'Overall')[0]?.value;
            }
          }

        } else if (filterPropArr.includes('filter1')) {
          if (filterPropArr.includes('filter1')
            && filterPropArr.includes('filter2')) {
            let tempArr = [];
            tempArr = this.createCombinations(this.kpiSelectedFilterObj[kpiId]['filter1'], this.kpiSelectedFilterObj[kpiId]['filter2'])
            const preAggregatedValues = [];
            for (let i = 0; i < tempArr?.length; i++) {
              preAggregatedValues?.push(...trendValueList?.filter(k => k['filter1'] == tempArr[i]?.filter1 && k['filter2'] == tempArr[i]?.filter2));
            }
            this.kpiChartData[kpiId] = preAggregatedValues[0]?.value;
          }
          else if (filterPropArr.includes('filter1')
            || filterPropArr.includes('filter2')) {
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
              this.kpiChartData[kpiId]?.push(trendValueList);
            }
          }
        }
      }
      // when there are no KPI Level Filters
      else if (trendValueList?.length > 0 && !filterPropArr?.length) {
        this.kpiChartData[kpiId] = [...this.sortAlphabetically(trendValueList)];
      } else {
        this.kpiChartData[kpiId] = [];
      }
    } else {
      this.kpiChartData[kpiId] = [];
    }

    if (this.colorObj && Object.keys(this.colorObj)?.length > 0) {
      this.kpiChartData[kpiId] = this.generateColorObj(kpiId, this.kpiChartData[kpiId]);
    }

    // For kpi3 and kpi53 generating table column headers and table data
    if (kpiId === 'kpi3' || kpiId === 'kpi53') {
      //generating column headers
      const columnHeaders = [];
      let kpiSelectedFilter = this.kpiSelectedFilterObj[kpiId] && this.kpiSelectedFilterObj[kpiId]['filter1'] ? this.kpiSelectedFilterObj[kpiId]['filter1'] : this.kpiSelectedFilterObj[kpiId];
      if (Object.keys(this.kpiSelectedFilterObj)?.length && kpiSelectedFilter?.length && kpiSelectedFilter[0]) {
        columnHeaders.push({ field: 'name', header: this.hierarchyLevel[+this.filterApplyData.level - 1]?.hierarchyLevelName + ' Name' });
        columnHeaders.push({ field: 'value', header: kpiSelectedFilter[0] });
        columnHeaders.push({ field: 'maturity', header: 'Maturity' });
      }
      if (this.kpiChartData[kpiId]) {
        this.kpiChartData[kpiId].columnHeaders = columnHeaders;
      }
      //generating Table data
      const kpiUnit = this.updatedConfigGlobalData?.find(kpi => kpi.kpiId === kpiId)?.kpiDetail?.kpiUnit;
      const data = [];
      if (this.kpiChartData[kpiId] && this.kpiChartData[kpiId].length) {
        for (let element of this.kpiChartData[kpiId]) {
          const rowData = {
            name: element.data,
            maturity: 'M' + element.maturity,
            value: element.value[0].data + ' ' + kpiUnit
          };
          data.push(rowData);
        }
      }
      this.kpiChartData[kpiId].data = data;
      this.showKpiTrendIndicator[kpiId] = false;

    }

    this.createTrendsData(kpiId);
    this.handleMaturityTableLoader();

  }

  setFilterValueIfAlreadyHaveBackup(kpiId, refreshValue, initialValue, filters?) {
    this.kpiSelectedFilterObj = this.helperService.setFilterValueIfAlreadyHaveBackup(kpiId, this.kpiSelectedFilterObj, this.selectedTab, refreshValue, initialValue, this.filterApplyData['ids']?.length ? this.filterApplyData['ids'][0] : {}, filters)
    if(this.selectedTab !== 'backlog') {
    this.getDropdownArray(kpiId);
    } else {
      if (this.updatedConfigGlobalData.filter(kpi => kpi?.kpiId == kpiId)[0]?.kpiDetail?.chartType) {
        this.getDropdownArrayForBacklog(kpiId);
      } else {
        this.getDropdownArrayForCard(kpiId);
      }
    }
  }

  getChartDataForBacklog(kpiId, idx, aggregationType) {
    const trendValueList = this.allKpiArray[idx]?.trendValueList;
    this.kpiThresholdObj[kpiId] = this.allKpiArray[idx]?.thresholdValue ? this.allKpiArray[idx]?.thresholdValue : null;
    if (trendValueList?.length > 0 && trendValueList[0]?.hasOwnProperty('filter')) {
      if (Object.values(this.kpiSelectedFilterObj[kpiId]).length > 1) {
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
        if (Object.values(this.kpiSelectedFilterObj[kpiId]).length === 1) {
          this.kpiChartData[kpiId] = trendValueList?.filter(x => x['filter'] == Object.values(this.kpiSelectedFilterObj[kpiId])[0])[0]?.value;
        } else {
          this.kpiChartData[kpiId] = trendValueList?.filter(x => x['filter'] == 'Overall')[0]?.value;
        }
      }
    } else if ((this.kpiSelectedFilterObj[kpiId]?.hasOwnProperty('filter1')) || (this.kpiSelectedFilterObj[kpiId]?.hasOwnProperty('filter2'))) {
      const filters = this.kpiSelectedFilterObj[kpiId]['filter1'] || this.kpiSelectedFilterObj[kpiId]['filter2'];
      const filter2 = this.kpiSelectedFilterObj[kpiId]['filter2'];
      let preAggregatedValues = [];
      for (let i = 0; i < filters?.length; i++) {
        if (Object.keys(this.kpiSelectedFilterObj[kpiId]).length === 1) {
          preAggregatedValues = [...preAggregatedValues, ...(trendValueList['value'] ? trendValueList['value'] : trendValueList)?.filter(x => x['filter1'] == filters[i] || x['filter2'] == filters[i])];
        } else {
          preAggregatedValues = [...preAggregatedValues, ...(trendValueList['value'] ? trendValueList['value'] : trendValueList)?.filter(x => x['filter1'] == filters[i] && x['filter2'] == filter2[i])];
        }
      }
      this.kpiChartData[kpiId] = preAggregatedValues[0]?.value;
    }
    else {
      if (trendValueList?.length > 0) {
        this.kpiChartData[kpiId] = [...this.helperService.sortAlphabetically(trendValueList)];
      } else if (trendValueList?.hasOwnProperty('value')) {
        this.kpiChartData[kpiId] = [...trendValueList?.value];
      } else {
        this.kpiChartData[kpiId] = [];
      }

    }

    if (this.colorObj && Object.keys(this.colorObj)?.length > 0 && !['kpi161', 'kpi146', 'kpi148'].includes(kpiId)) {
      this.kpiChartData[kpiId] = this.generateColorObj(kpiId, this.kpiChartData[kpiId]);
    }

    this.createTrendData(kpiId);
    this.updatedConfigGlobalData.forEach(kpi => {
      if (kpi.kpiId == kpiId) {
        this.showKpiTrendIndicator[kpiId] = (kpiId === 'kpi3') ? true : false;
      }
    });

  }

  getChartType(kpiId) {
    return this.updatedConfigGlobalData.find(kpi => kpi?.kpiId === kpiId)?.kpiDetail?.chartType;
  }

  applyAggregationLogicForProgressBar(obj) {
    let maxValue = 0;
    let value = 0;
    for (const key in obj) {
      const currentObj = obj[key][0]?.value[0]?.hoverValue;
      if (!currentObj) continue;
      Object.keys(currentObj).forEach((prop) => {
        if (prop?.toLowerCase()?.includes('total')) {
          maxValue += currentObj[prop];
        } else {
          value += currentObj[prop];
        }
      });
    }
    const kpiChartData = obj[Object.keys(obj)[0]];
    kpiChartData[0].value[0].maxValue = maxValue;
    kpiChartData[0].value[0].value = value;
    return kpiChartData;
  }

  createTrendData(kpiId) {
    const kpiDetail = this.configGlobalData.find(details => details.kpiId == kpiId)
    const trendingList = this.kpiChartData[kpiId];
    if (trendingList?.length) {
      this.kpiTrendObject[kpiId] = [];
      if (trendingList[0]?.value?.length > 0 && kpiDetail) {
        let trendObj = {};
        const [latest, trend, unit] = this.checkLatestAndTrendValue(kpiDetail, trendingList[0]);
        trendObj = {
          "hierarchyName": trendingList[0]?.data,
          "trend": trend,
          "maturity": 'M' + trendingList[0]?.maturity,
          "maturityValue": trendingList[0]?.maturityValue,
          "maturityDenominator": trendingList[0]?.value.length,
          "kpiUnit": unit
        };
        this.kpiTrendObject[kpiId]?.push(trendObj);
      }
    }

  }

  getChartDataforRelease(kpiId, idx, aggregationType?, kpiFilterChange = false) {
    const trendValueList = this.allKpiArray[idx]?.trendValueList ? JSON.parse(JSON.stringify(this.allKpiArray[idx]?.trendValueList)) : {};
    this.kpiThresholdObj[kpiId] = this.allKpiArray[idx]?.thresholdValue ? this.allKpiArray[idx]?.thresholdValue : null;

    if (kpiId === 'kpi178') {
      this.kpiChartData[kpiId] = [];
      this.kpiChartData[kpiId].push({
        data: this.allKpiArray[idx]?.issueData,
        filters: this.allKpiArray[idx]?.filterGroup,
        modalHeads: this.allKpiArray[idx]?.modalHeads
      });

    } else {
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
        if (this.kpiSelectedFilterObj[kpiId]?.hasOwnProperty('filter1') && this.kpiSelectedFilterObj[kpiId]?.hasOwnProperty('filter2')) {
          let tempArr = [];
          const preAggregatedValues = [];
          /** tempArr: array with combination of all items of filter1 and filter2 */
          tempArr = this.helperService.createCombinations(this.kpiSelectedFilterObj[kpiId]['filter1'], this.kpiSelectedFilterObj[kpiId]['filter2'])
          for (let i = 0; i < tempArr?.length; i++) {
            preAggregatedValues?.push(...trendValueList?.filter(k => k['filter1'] == tempArr[i]?.filter1 && k['filter2'] == tempArr[i]?.filter2));
          }
          if (preAggregatedValues?.length > 1) {
            // if (this.getKpiChartType(kpiId) === 'GroupBarChart' || this.getKpiChartType(kpiId) === 'horizontalPercentBarChart') {
            //   this.kpiChartData[kpiId] = this.applyAggregationForChart(preAggregatedValues);
            // } else {
            this.kpiChartData[kpiId] = this.applyAggregationLogic(preAggregatedValues);
            // }
          } else {
            this.kpiChartData[kpiId] = [...preAggregatedValues];
          }
        } else if (this.kpiSelectedFilterObj[kpiId]?.hasOwnProperty('filter1') || this.kpiSelectedFilterObj[kpiId]?.hasOwnProperty('filter2')) {
          let filters = this.kpiSelectedFilterObj[kpiId]['filter1'] || this.kpiSelectedFilterObj[kpiId]['filter2'];
          let preAggregatedValues = [];
          // for single select dropdown filters
          if (!Array.isArray(filters)) {
            filters = [filters];
          }
          for (let i = 0; i < filters?.length; i++) {
            preAggregatedValues = [...preAggregatedValues, ...trendValueList?.filter(x => x['filter1'] == filters[i] || x['filter2'] == filters[i])];
          }
          if (preAggregatedValues?.length > 1) {
            // if (this.getKpiChartType(kpiId) === 'GroupBarChart' || this.getKpiChartType(kpiId) === 'horizontalPercentBarChart') {
            //   this.kpiChartData[kpiId] = this.applyAggregationForChart(preAggregatedValues);
            // } else {
            this.kpiChartData[kpiId] = this.applyAggregationLogic(preAggregatedValues);
            // }
          } else {
            if (preAggregatedValues[0]?.hasOwnProperty('value')) {
              this.kpiChartData[kpiId] = preAggregatedValues[0]?.value;
            } else {
              this.kpiChartData[kpiId] = [...preAggregatedValues];
            }
          }
        } else {
          this.kpiChartData[kpiId] = trendValueList.filter(kpiData => kpiData.filter1 === 'Overall');
        }
      } else if (trendValueList?.length > 0) {
        this.kpiChartData[kpiId] = [...trendValueList[0]?.value];
      } else {
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
  }
  /**To create KPI table headings */
  createKpiTableHeads(selectedType) {
    this.kpiTableHeadingArr = [];
    if (selectedType == 'kanban') {
      this.noOfDataPoints = this.filterApplyData['ids']?.[0];
    }
    if (this.noOfDataPoints) {
      this.kpiTableHeadingArr?.push({ 'field': 'kpiName', 'header': 'Kpi Name' });
      this.kpiTableHeadingArr?.push({ 'field': 'frequency', 'header': 'Frequency' });
      for (let i = 0; i < this.noOfDataPoints; i++) {
        this.kpiTableHeadingArr?.push({ 'field': i + 1, 'header': i + 1 });
      }
      this.kpiTableHeadingArr?.push({ 'field': 'trend', 'header': 'Trend' });
      this.kpiTableHeadingArr?.push({ 'field': 'maturity', 'header': 'Maturity' });
    }
  }

  /** to prepare table data */
  getTableData(kpiId, idx, enabledKpi) {
    let trendValueList = [];
    if (idx >= 0) {
      trendValueList = this.allKpiArray[idx]?.trendValueList;
    } else {
      trendValueList = this.allKpiArray?.filter((x) => x[kpiId] == kpiId)[0]?.trendValueList;
    }
    if (trendValueList?.length > 0) {
      let selectedIdx: number = -1;
      let iterativeEle = JSON.parse(JSON.stringify(trendValueList));
      let trendVals = trendValueList[0]?.hasOwnProperty('filter') || trendValueList[0]?.hasOwnProperty('filter1');
      if (trendVals) {
        if (kpiId == 'kpi17') {
          selectedIdx = trendValueList?.findIndex(x => x['filter']?.toLowerCase() == 'average coverage');
        } else if (kpiId == 'kpi72') {
          selectedIdx = trendValueList?.findIndex(x => x['filter1']?.toLowerCase() == 'initial commitment (story points)' && x['filter2']?.toLowerCase() == 'overall');
        } else {
          selectedIdx = trendValueList?.findIndex(x => x['filter']?.toLowerCase() == 'overall');
          if (selectedIdx < 0) {
            selectedIdx = 0;
          }
        }
        if (selectedIdx != -1 && trendValueList[selectedIdx]?.value) {
          iterativeEle = JSON.parse(JSON.stringify(trendValueList[selectedIdx]?.value));
        }
      }
      let filtersApplied = Object.keys(this.colorObj);

      filtersApplied = filtersApplied.map((x) => {
        let parts = x.split('_');
        return parts.slice(0, parts.length - 1).join('_');
      });


      filtersApplied.forEach((hierarchyName) => {
        let obj = {
          'kpiId': kpiId,
          'kpiName': this.allKpiArray[idx]?.kpiName,
          'frequency': enabledKpi?.kpiDetail?.xaxisLabel,
          'show': enabledKpi?.isEnabled && enabledKpi?.shown,
          'hoverText': [],
          'order': enabledKpi?.order
        }
        let chosenItem = iterativeEle?.filter((item) => item['data'] == hierarchyName)[0];

        let trendData = this.kpiTrendsObj[kpiId]?.filter(x => x['hierarchyName']?.toLowerCase() == hierarchyName?.toLowerCase())[0];
        obj['latest'] = trendData?.value || '-';
        obj['trend'] = trendData?.trend || '-';
        obj['maturity'] = trendData?.maturity || '-';
        for (let i = 0; i < this.noOfDataPoints; i++) {
          let item = chosenItem?.value[i];
          const trendDataKpiUnit = (trendData?.kpiUnit ? ' ' + trendData?.kpiUnit : '');
          if (item) {
            obj['hoverText']?.push((i + 1) + ' - ' + (item?.['sprintNames']?.length > 0
              ? item['sprintNames'].join(',') : item?.['sSprintName'] ? item['sSprintName'] : item?.['date']));
            let val = item?.lineValue >= 0 ? item?.lineValue : item?.value;
            obj[i + 1] = val > 0 ? ((Math.round(val * 10) / 10) + trendDataKpiUnit) : (val + trendDataKpiUnit || '-');
            if (kpiId === 'kpi153') {
              obj[i + 1] = item?.dataValue.find(pdata => pdata['name'] === 'Achieved Value').value || '-';
            }
          } else {
            obj[i + 1] = '-';
          }

        }
        let kpiIndex = this.kpiTableDataObj[hierarchyName]?.findIndex((x) => x.kpiId == kpiId);
        if (kpiIndex > -1) {
          this.kpiTableDataObj[hierarchyName]?.splice(kpiIndex, 1);
        }
        if (enabledKpi?.isEnabled && enabledKpi?.shown && this.kpiTableDataObj[hierarchyName]) {
          this.kpiTableDataObj[hierarchyName] = [...this.kpiTableDataObj[hierarchyName], obj];
        }
        this.sortingRowsInTable(hierarchyName);
      })
    } else {
      /** when no data available */
      if (this.allKpiArray[idx]?.kpiName) {
        let obj = {
          'kpiId': kpiId,
          'kpiName': this.allKpiArray[idx]?.kpiName,
          'frequency': enabledKpi?.kpiDetail?.xaxisLabel,
          'show': enabledKpi?.isEnabled && enabledKpi?.shown,
          'hoverText': [],
          'order': enabledKpi?.order
        }
        for (let i = 0; i < this.noOfDataPoints; i++) {
          obj[i + 1] = '-';
        }
        obj['latest'] = '-';
        obj['trend'] = '-';
        obj['maturity'] = '-';
        for (let hierarchyName in this.kpiTableDataObj) {
          if (enabledKpi?.isEnabled && enabledKpi?.shown) {
            let kpiIndex = this.kpiTableDataObj[hierarchyName]?.findIndex((x) => x.kpiId == kpiId);
            if (kpiIndex > -1) {
              this.kpiTableDataObj[hierarchyName]?.splice(kpiIndex, 1);
            }
            this.kpiTableDataObj[hierarchyName]?.push(obj)
            this.sortingRowsInTable(hierarchyName);
          }
        }
      }
    }
    if (!this.maturityTableKpiList.includes(kpiId)) {
      this.maturityTableKpiList.push(kpiId);
    }
  }
  sortingRowsInTable(hierarchyName) {
    this.kpiTableDataObj[hierarchyName]?.sort((a, b) => a.order - b.order);
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
      if (trendValueList && !Array.isArray(trendValueList) && Object.keys(trendValueList)?.length > 0 && filters && Object.keys(filters)?.length > 0) {
        this.getDropdownArray(data[key]?.kpiId);
        this.setFilterValueIfAlreadyHaveBackup(data[key]?.kpiId, {}, ['Overall'], filters)
      }
      else if (trendValueList?.length > 0 && (trendValueList[0]?.hasOwnProperty('filter') || trendValueList[0]?.hasOwnProperty('filter1'))) {
        this.populateKPIFilters(data, key);
      } else if (!trendValueList || trendValueList?.length == 0) {
        this.getDropdownArray(data[key]?.kpiId);
      }
      const agType = this.updatedConfigGlobalData?.filter(x => x.kpiId == data[key]?.kpiId)[0]?.kpiDetail?.aggregationCriteria;


      if (this.selectedTab.toLowerCase() !== 'release' && this.selectedTab.toLowerCase() !== 'backlog') {
        this.getChartData(data[key]?.kpiId, (this.allKpiArray?.length - 1), agType);
      } else if (this.selectedTab.toLowerCase() === 'release') {
        this.getChartDataforRelease(data[key]?.kpiId, (this.allKpiArray?.length - 1), agType);
      }
    }
  }

  createAllKpiArrayForBacklog(data) {
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
      if (this.updatedConfigGlobalData.filter(kpi => kpi?.kpiId == key)[0]?.kpiDetail?.chartType) {
        if (trendValueList?.length > 0 && trendValueList[0]?.hasOwnProperty('filter')) {
          this.setFilterValueIfAlreadyHaveBackup(data[key]?.kpiId, [], 'Overall')
        } else if (trendValueList?.length > 0 && trendValueList[0]?.hasOwnProperty('filter1')) {
          this.getDropdownArrayForBacklog(data[key]?.kpiId);
          const formType = this.updatedConfigGlobalData?.filter(x => x.kpiId == data[key]?.kpiId)[0]?.kpiDetail?.kpiFilter;
          if (formType?.toLowerCase() == 'radiobutton') {
            this.setFilterValueIfAlreadyHaveBackup(data[key]?.kpiId, {}, [this.kpiDropdowns[data[key]?.kpiId][0]?.options[0]])
          }
          else if (formType?.toLowerCase() == 'dropdown' && (!filters)) {
            this.setFilterValueIfAlreadyHaveBackup(data[key]?.kpiId, {}, ['Overall'])
          }
          else if (filters && Object.keys(filters)?.length > 0) {
            this.setFilterValueIfAlreadyHaveBackup(data[key]?.kpiId, {}, ['Overall'], filters)
          } else {
            this.setFilterValueIfAlreadyHaveBackup(data[key]?.kpiId, {}, ['Overall'])
          }
        }

        const agType = this.updatedConfigGlobalData?.filter(x => x.kpiId == data[key]?.kpiId)[0]?.kpiDetail?.aggregationCriteria;
        this.getChartDataForBacklog(data[key]?.kpiId, (this.allKpiArray?.length - 1), agType);

      } else {
        // if (trendValueList && Object.keys(trendValueList)?.length > 0 && filters && Object.keys(filters)?.length > 0) {
        //   this.setFilterValueIfAlreadyHaveBackup(data[key]?.kpiId, {}, ['Overall'], filters)
        // }
        this.getDropdownArrayForCard(data[key]?.kpiId);
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
          if (kpiId === 'kpi138') {
            this.kpiChartData[kpiId] = this.applyAggregationLogicForkpi138(preAggregatedValues);
          } else {
            this.kpiChartData[kpiId] = this.applyAggregationLogic(preAggregatedValues);
          }
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
          if (kpiId === 'kpi138') {
            this.kpiChartData[kpiId] = this.applyAggregationLogicForkpi138(preAggregatedValues);
          } else {
            this.kpiChartData[kpiId] = this.applyAggregationLogic(preAggregatedValues);
          }
        } else {
          this.kpiChartData[kpiId] = [...preAggregatedValues];
        }
      } else if (this.kpiSelectedFilterObj[kpiId]?.hasOwnProperty('filter1') || this.kpiSelectedFilterObj[kpiId]?.hasOwnProperty('filter2') && (
        !Array.isArray(this.kpiSelectedFilterObj[kpiId]['filter1']) || !Array.isArray(this.kpiSelectedFilterObj[kpiId]['filter2'])
      )) {
        if (kpiId === 'kpi171') {
          this.getkpi171Data(kpiId, trendValueList);
        } else {
          this.getChartDataForCardWithCombinationFilter(kpiId, trendValueList);
        }
      } else {
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
    } else {
      this.kpiChartData[kpiId] = [];
    }
  }

  getChartDataForCardWithCombinationFilter(kpiId, trendValueList) {
    let filters = this.kpiSelectedFilterObj[kpiId];
    if (kpiId === 'kpi171') {
      const issueFilter = this.kpiSelectedFilterObj[kpiId].hasOwnProperty('filter2') ? this.kpiSelectedFilterObj[kpiId]['filter2'] : ['Overall'];
      filters = {
        filter1: issueFilter
      };
    }

    let preAggregatedValues = [];
    for (const filter in filters) {
      let tempArr = [];
      if (preAggregatedValues.length > 0) {
        tempArr = preAggregatedValues;
      } else {
        tempArr = trendValueList?.value ? trendValueList?.value : [];
      }

      if (Array.isArray(filters[filter])) {
        preAggregatedValues = [...tempArr.filter((x) => filters[filter].includes(x[filter]))];
      } else {
        preAggregatedValues = [...tempArr.filter((x) => x[filter] === filters[filter])];
      }
    }

    if (preAggregatedValues?.length > 1) {
      this.kpiChartData[kpiId] = this.applyAggregationLogic(preAggregatedValues);
    } else {
      this.kpiChartData[kpiId] = [...preAggregatedValues];
    }
  }

  getkpi171Data(kpiId, trendValueList) {
    let durationChanged = false;
    if (this.kpiSelectedFilterObj[kpiId].hasOwnProperty('filter1') && this.kpiSelectedFilterObj[kpiId]['filter1'] !== this.durationFilter) {
      durationChanged = true;
      this.kpiChartData[kpiId] = [];
      this.durationFilter = this.kpiSelectedFilterObj[kpiId]['filter1'];
    }

    // if duration filter (filter1) changes,  make an api call to fetch data
    if (durationChanged) {
      const idx = this.ifKpiExist(kpiId);
      if (idx !== -1) {
        this.allKpiArray.splice(idx, 1);
      }

      // this.kpiSpecificLoader.push('kpi171');
      const kpi171Payload = JSON.parse(JSON.stringify(this.kpiJira));
      const kpi171 = kpi171Payload.kpiList.filter(kpi => kpi.kpiId === 'kpi171')[0];
      kpi171['filterDuration'] = {
        duration: this.durationFilter.includes('Week') ? 'WEEKS' : 'MONTHS',
        value: !isNaN(+this.durationFilter.split(' ')[1]) ? +this.durationFilter.split(' ')[1] : 1
      };

      kpi171Payload.kpiList = [kpi171];

      this.httpService.postKpiNonTrend(kpi171Payload, 'jira').subscribe(data => {
        const kpi171Data = data.find(kpi => kpi.kpiId === kpiId);
        this.allKpiArray.push(kpi171Data);
        this.getChartDataForCardWithCombinationFilter(kpiId, JSON.parse(JSON.stringify(kpi171Data.trendValueList)));
        // this.kpiSpecificLoader.pop();
      });

    } else {
      this.getChartDataForCardWithCombinationFilter(kpiId, trendValueList);
    }

  }

  applyAggregationLogicForkpi138(arr) {
    const aggregatedArr = JSON.parse(JSON.stringify(arr));
    aggregatedArr.forEach(x => {
      x.data[2].value = x.data[2].value * x.data[0]?.value;
    });

    const kpi138 = this.applyAggregationLogic(aggregatedArr);
    kpi138[0].data[2].value = Math.round(kpi138[0]?.data[2].value / kpi138[0]?.data[0]?.value);
    return kpi138;
  }

  populateKPIFilters(data, key) {
    const filters = this.allKpiArray[this.allKpiArray?.length - 1]?.filters;
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

  getKpiChartType(kpiId) {
    return this.updatedConfigGlobalData.filter(kpiDetails => kpiDetails.kpiId === kpiId)[0]?.kpiDetail?.chartType;
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
    // let evalvateExpression = [];
    // if (aggregatedArr[0]['data']) {
    //   evalvateExpression = aggregatedArr[0]['data'].filter(el => el.hasOwnProperty('expressions'));
    // }
    // if (evalvateExpression.length > 0) {
    //   evalvateExpression.forEach(item => {
    //     this.evalvateExpression(item, aggregatedArr[0]['data'], arr);
    //   });
    // }
    return aggregatedArr;
  }

  // applyAggregationForChart(arr) {
  //   const aggregatedArr = JSON.parse(JSON.stringify(arr[0]));
  //   for (let i = 1; i < arr.length; i++) {
  //     for (let j = 0; j < arr[i].value.length; j++) {
  //       if (typeof aggregatedArr.value[j].value === 'number') {
  //         aggregatedArr.value[j].value += arr[i].value[j].value;
  //         aggregatedArr.value[j].hoverValue = { ...aggregatedArr.value[j].hoverValue, ...arr[i].value[j].hoverValue };
  //       }
  //       if (typeof aggregatedArr.value[j].value === 'object') {
  //         if (!Array.isArray(aggregatedArr.value[j].value)) {
  //           for (const key in aggregatedArr.value[j].value) {
  //             aggregatedArr.value[j].value[key] += arr[i].value[j].value[key];
  //           }
  //         } else {
  //           // kpi147
  //           for (const key in aggregatedArr.value[j].value) {
  //             Object.assign(aggregatedArr.value[j].value[key], arr[i].value[j].value[key]);
  //           }
  //         }
  //       }
  //     }
  //   }
  //   return [aggregatedArr];
  // }

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

  checkSprint(value, unit, kpiId) {
    if ((this.kpiSelectedFilterObj[kpiId]?.hasOwnProperty('filter1') && this.kpiSelectedFilterObj[kpiId]['filter1']?.length > 0 && this.kpiSelectedFilterObj[kpiId]['filter1'][0]?.toLowerCase() !== 'overall')
      || (this.kpiSelectedFilterObj[kpiId]?.hasOwnProperty('filter2') && this.kpiSelectedFilterObj[kpiId]['filter2']?.length > 0 && this.kpiSelectedFilterObj[kpiId]['filter2'][0]?.toLowerCase() !== 'overall')) {
      return '-'
    } else {
      return Math.floor(value) < value ? `${Math.round(value)} ${unit}` : `${value} ${unit}`;
    }
  }

  handleArrowClick(kpi, label, tableValues) {
    this.displayModal = true;
    const idx = this.ifKpiExist(kpi?.kpiId);
    this.modalDetails['tableHeadings'] = this.allKpiArray[idx]?.modalHeads;
    this.modalDetails['header'] = kpi?.kpiName + ' / ' + label;
    this.modalDetails['tableValues'] = tableValues;
  }


  checkIfDataPresent(data) {
    if (this.kpiStatusCodeArr[data]) {
      return this.kpiStatusCodeArr[data] === '200' && this.checkDataAtGranularLevel(this.kpiChartData[data]);
    }
    return false;
  }

  checkDataAtGranularLevel(data) {
    let dataCount = 0;
    if (Array.isArray(data)) {
      data?.forEach(item => {
        if (Array.isArray(item.data) && item.data?.length) {
          ++dataCount;
        } else if (item.data && !isNaN(parseInt(item.data))) {
          // dataCount += item?.data;
          ++dataCount;
        } else if (item.value && ((Array.isArray(item.value) && item.value.length) || Object.keys(item.value)?.length)) {
          ++dataCount;
        } else if (item.dataGroup && item.dataGroup.length) {
          ++dataCount;
        }
      });
    } else if (data && Object.keys(data).length) {
      dataCount = Object.keys(data).length;
    }
    return parseInt(dataCount + '') > 0;
  }


  // evalvateExpression(element, aggregatedArr, filteredArr) {

  //   const tempArr = [];
  //   const operandsArr = element['expressions'];

  //   operandsArr.forEach(op => {
  //     if (op === 'percentage') {
  //       const op2 = tempArr.pop();
  //       const op1 = tempArr.pop();
  //       tempArr.push(+((op1 / op2) * 100).toFixed(2));
  //     } else if (op === 'average') {
  //       const op2 = tempArr.pop();
  //       const op1 = tempArr.pop();
  //       tempArr.push(+(op1 / op2).toFixed(2));
  //     } else {
  //       const opValue = aggregatedArr.find(x => x.label === op)?.value;
  //       tempArr.push(opValue);
  //     }
  //   });

  //   element.value = tempArr[0];
  // }


  generateColorObj(kpiId, arr) {
    // If the arr is empty, return an empty array
    if (!arr?.length) return [];

    const finalArr = [];
    this.chartColorList[kpiId] = [];

    for (let i = 0; i < arr?.length; i++) {
      for (const key in this.colorObj) {
        if (kpiId == 'kpi17' && this.colorObj[key]?.nodeName == arr[i].value[0].sprojectName) {
          this.chartColorList[kpiId].push(this.colorObj[key]?.color);
          finalArr.push(JSON.parse(JSON.stringify(arr[i])));
        }
        else if (this.colorObj[key]?.nodeName == arr[i]?.data) {
          this.chartColorList[kpiId].push(this.colorObj[key]?.color);
          finalArr.push(arr.filter((a) => a.data === this.colorObj[key].nodeName)[0]);
        }
        else continue;
      }
    }
    return finalArr;
  }

  /** get array of the kpi level filter */
  getDropdownArray(kpiId) {
    const idx = this.ifKpiExist(kpiId);
    const dropdownArr = [];
    let trendValueList = this.allKpiArray[idx]?.trendValueList;
    if (idx != -1 && trendValueList?.length) {
      let filterPropArr = Object.keys(trendValueList[0]).filter((prop) => prop.includes('filter'));
      if (trendValueList?.length > 0 && filterPropArr?.length) {

        filterPropArr.forEach((filterProp) => {
          dropdownArr?.push(new Set([...trendValueList.map(x => x[filterProp])]));
        });

        this.kpiDropdowns[kpiId] = [];
        dropdownArr.forEach(arr => {
          arr = Array.from(arr);
          const obj = {};

          obj['filterType'] = 'Select a filter';
          if (arr.length > 0) {
            arr.sort((a, b) => {
              if (a === "Overall") {
                return -1; // "Overall" should be moved to the beginning (0 index)
              } else if (b === "Overall") {
                return 1; // "Overall" should be moved to the beginning (0 index)
              } else {
                return 0; // Maintain the original order of other elements
              }
            });

            obj['options'] = arr;
            this.kpiDropdowns[kpiId].push(obj);
          }
        });
      }
    } else if (!trendValueList || trendValueList?.length == 0) {
      this.kpiDropdowns[kpiId] = [];
    }

    if (this.kpiDropdowns[kpiId]?.length > 1) {
      this.kpiSelectedFilterObj[kpiId] = {};
      for (let i = 0; i < this.kpiDropdowns[kpiId].length; i++) {
        this.kpiSelectedFilterObj[kpiId]['filter' + (i + 1)] = [this.kpiDropdowns[kpiId][i].options[0]];
      }
    }
  }

  getDropdownArrayForBacklog(kpiId) {
    const idx = this.ifKpiExist(kpiId);
    let trendValueList = [];
    const optionsArr = [];
    let filters = {};

    if (idx != -1) {
      trendValueList = this.allKpiArray[idx]?.trendValueList;
      filters = this.allKpiArray[idx]?.filters;
      if (filters && Object.keys(filters).length !== 0) {
        Object.keys(filters)?.forEach(x => {
          optionsArr.push(filters[x]);
        });
        this.kpiDropdowns[kpiId] = [...optionsArr];
      }
      else if (trendValueList?.length > 0 && trendValueList[0]?.hasOwnProperty('filter')) {
        const obj = {};
        for (let i = 0; i < trendValueList?.length; i++) {
          if (trendValueList[i]?.filter?.toLowerCase() != 'overall' && trendValueList.length > 1) {
            optionsArr?.push(trendValueList[i]?.filter);
          }
        }
        obj['filterType'] = 'Select a filter';
        obj['options'] = optionsArr;
        this.kpiDropdowns[kpiId] = [];
        this.kpiDropdowns[kpiId].push(obj);
      } else if (trendValueList?.length > 0 && trendValueList[0]?.hasOwnProperty('filter1')) {
        const obj = {};
        for (let i = 0; i < trendValueList?.length; i++) {
          let ifExist = optionsArr.findIndex(x => x == trendValueList[i]?.filter1);
          if (ifExist == -1) {
            optionsArr?.push(trendValueList[i]?.filter1);
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

    // if (this.kpiDropdowns[kpiId]?.length > 1) {
    //   this.kpiSelectedFilterObj[kpiId] = {};
    //   for (let i = 0; i < this.kpiDropdowns[kpiId].length; i++) {
    //     this.kpiSelectedFilterObj[kpiId]['filter' + (i + 1)] = [this.kpiDropdowns[kpiId][i].options[0]];
    //   }
    // }
  }

  handleSelectedOption(event, kpi) {
    if (this.selectedTab.toLowerCase() === 'release') {
      this.handleSelectedOptionOnRelease(event, kpi);
    } else if (this.selectedTab.toLowerCase() === 'backlog') {
      if (kpi?.kpiDetail?.chartType) { 
        this.handleSelectedOptionOnBacklog(event, kpi) 
      } else { 
        this.handleSelectedOptionForCard(event, kpi) 
      }
    } else {
      // this.kpiSelectedFilterObj[kpi?.kpiId] = [];
      if (kpi.kpiId === "kpi72") {
        if (event.hasOwnProperty('filter1') || event.hasOwnProperty('filter2')) {
          if (!Array.isArray(event.filter1) || !Array.isArray(event.filter2)) {
            const outputObject = {};
            for (const key in event) {
              outputObject[key] = [event[key]];
            }
            event = outputObject;
          }
        }
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

      }
      else {
        if (event && Object.keys(event)?.length !== 0 && typeof event === 'object' && this.selectedTab.toLowerCase() !== 'developer') {
          this.kpiSelectedFilterObj[kpi?.kpiId] = [];
          for (const key in event) {
            if (event[key]?.length == 0) {
              delete event[key];
              this.kpiSelectedFilterObj[kpi?.kpiId] = event;
            } else if (Array.isArray(event[key])) {
              for (let i = 0; i < event[key]?.length; i++) {
                this.kpiSelectedFilterObj[kpi?.kpiId] = [...this.kpiSelectedFilterObj[kpi?.kpiId], Array.isArray(event[key]) ? event[key][i] : event[key]];
              }
            } else {
              if (kpi.kpiDetail.kpiFilter !== 'dropDown') {
                this.kpiSelectedFilterObj[kpi?.kpiId] = Array.isArray(event[key]) ? event[key] : [event[key]];
              } else {
                this.kpiSelectedFilterObj[kpi?.kpiId] = event
              }
            }
          }
        } else if (this.selectedTab.toLowerCase() === 'developer') {
          if (this.kpiSelectedFilterObj[kpi?.kpiId]) {
            this.kpiSelectedFilterObj[kpi?.kpiId]['filter' + event.index] = [event.value];
          } else {
            this.kpiSelectedFilterObj[kpi?.kpiId] = {};
            this.kpiSelectedFilterObj[kpi?.kpiId]['filter' + event.index] = [event.value];
          }
        } else {
          this.kpiSelectedFilterObj[kpi?.kpiId] = [];
          this.kpiSelectedFilterObj[kpi?.kpiId].push(event);
        }
      }
      this.getChartData(kpi?.kpiId, this.ifKpiExist(kpi?.kpiId), kpi?.kpiDetail?.aggregationCriteria, true);
      this.kpiSelectedFilterObj['action'] = 'update';
      this.service.setKpiSubFilterObj(this.kpiSelectedFilterObj);
    }

  }

  handleSelectedOptionOnRelease(event, kpi) {
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
    this.getChartDataforRelease(kpi?.kpiId, this.ifKpiExist(kpi?.kpiId), true);
    this.helperService.createBackupOfFiltersSelection(this.kpiSelectedFilterObj, this.selectedTab, this.filterApplyData['ids'][0]);
    this.service.setKpiSubFilterObj(this.kpiSelectedFilterObj);
  }

  handleSelectedOptionOnBacklog(event, kpi) {
    const selectedFilterBackup = this.kpiSelectedFilterObj[kpi?.kpiId];
    this.kpiSelectedFilterObj[kpi?.kpiId] = {};
    /** When we have single dropdown */
    if (event && Object.keys(event)?.length !== 0 && typeof event === 'object' && !selectedFilterBackup.hasOwnProperty('filter2')) {
      for (const key in event) {
        if (typeof event[key] === 'string') {
          this.kpiSelectedFilterObj[kpi?.kpiId] = event;
        } else {
          for (let i = 0; i < event[key]?.length; i++) {
            this.kpiSelectedFilterObj[kpi?.kpiId] = event[key];
          }
        }
      }
      /** When we have multi dropdown */
    } else if (event && Object.keys(event)?.length !== 0 && typeof event === 'object' && !Array.isArray(selectedFilterBackup) && selectedFilterBackup.hasOwnProperty('filter2')) {
      const selectedFilter = {};
      for (const key in event) {
        const updatedFilter = typeof event[key] === 'string' ? [event[key]] : [...event[key]];
        selectedFilter[key] = updatedFilter;
      }
      this.kpiSelectedFilterObj[kpi?.kpiId] = { ...selectedFilterBackup, ...selectedFilter };
    } else {
      this.kpiSelectedFilterObj[kpi?.kpiId] = { "filter1": [event] };
    }
    this.getChartDataForBacklog(kpi?.kpiId, this.ifKpiExist(kpi?.kpiId), kpi?.kpiDetail?.aggregationCriteria);
    this.helperService.createBackupOfFiltersSelection(this.kpiSelectedFilterObj, 'backlog', '');
    this.service.setKpiSubFilterObj(this.kpiSelectedFilterObj);
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
    this.helperService.createBackupOfFiltersSelection(this.kpiSelectedFilterObj, 'backlog', '');
    this.service.setKpiSubFilterObj(this.kpiSelectedFilterObj);
  }


  checkMaturity(item) {
    // let maturity = item.maturity;
    // if (maturity == undefined) {
    //   return 'NA';
    // }
    // if (item.value.length >= 5) {
    //   const last5ArrItems = item.value.slice(item.value.length - 5, item.value.length);
    //   const tempArr = last5ArrItems.filter(x => x.data != 0);
    //   if (tempArr.length == 0) {
    //     maturity = '--';
    //   }
    // } else {
    //   maturity = '--';
    // }
    // maturity = maturity != 'NA' && maturity != '--' && maturity != '-' ? 'M' + maturity : maturity;
    let maturity = item.maturity;
    if (maturity == undefined) {
      return 'NA';
    }
    maturity = 'M' + maturity;
    return maturity;
  }

  // checkLatestAndTrendValueForKpi(kpiData, item) {
  //   let latest: string = '';
  //   let trend: string = '';
  //   const unit = kpiData?.kpiDetail?.kpiUnit?.toLowerCase() != 'number' && kpiData?.kpiDetail?.kpiUnit?.toLowerCase() != 'stories' && kpiData?.kpiDetail?.kpiUnit?.toLowerCase() != 'tickets' ? kpiData?.kpiDetail?.kpiUnit?.trim() : '';
  //   const modUnit = (unit ? ' ' + unit : '');
  //   if (item?.value?.length > 0) {
  //     let tempVal = item?.value[item?.value?.length - 1]?.dataValue.find(d => d.lineType === 'solid')?.value;
  //     latest = tempVal > 0 ? ((Math.round(tempVal * 10) / 10) + modUnit) : (tempVal + modUnit);
  //   }
  //   if (item?.value?.length > 0 && kpiData?.kpiDetail?.showTrend) {
  //     if (kpiData?.kpiDetail?.trendCalculative) {
  //       let lhsKey = kpiData?.kpiDetail?.trendCalculation?.length > 0 ? kpiData?.kpiDetail?.trendCalculation[0]?.lhs : '';
  //       let rhsKey = kpiData?.kpiDetail?.trendCalculation?.length > 0 ? kpiData?.kpiDetail?.trendCalculation[0]?.rhs : '';
  //       let lhs = item?.value[item?.value?.length - 1][lhsKey];
  //       let rhs = item?.value[item?.value?.length - 1][rhsKey];
  //       let operator = lhs < rhs ? '<' : lhs > rhs ? '>' : '=';
  //       let trendObj = kpiData?.kpiDetail?.trendCalculation?.find((item) => item.operator == operator);
  //       if (trendObj) {
  //         trend = trendObj['type']?.toLowerCase() == 'downwards' ? '-ve' : trendObj['type']?.toLowerCase() == 'upwards' ? '+ve' : '-- --';
  //       } else {
  //         trend = 'NA';
  //       }
  //     } else {
  //       let lastVal = item?.value[item?.value?.length - 1]?.dataValue.find(d => d.lineType === 'solid')?.value;
  //       let secondLastVal = item?.value[item?.value?.length - 2]?.dataValue.find(d => d.lineType === 'solid')?.value;
  //       let isPositive = kpiData?.kpiDetail?.isPositiveTrend;
  //       if (secondLastVal > lastVal && !isPositive) {
  //         trend = '+ve';
  //       } else if (secondLastVal < lastVal && !isPositive) {
  //         trend = '-ve';
  //       } else if (secondLastVal < lastVal && isPositive) {
  //         trend = '+ve';
  //       } else if (secondLastVal > lastVal && isPositive) {
  //         trend = '-ve';
  //       } else {
  //         trend = '-- --';
  //       }
  //     }
  //   } else {
  //     trend = 'NA';
  //   }
  //   return [latest, trend, unit];
  // }

  checkLatestAndTrendValue(kpiData, item) {
    let latest: string = '';
    let trend: string = '';
    let unit = '';
    if (item?.value?.length > 0) {
      let tempVal;
      if (item?.value[item?.value?.length - 1]?.dataValue) {
        tempVal = item?.value[item?.value?.length - 1]?.dataValue.find(d => d.lineType === 'solid')?.value;
      } else {
        tempVal = item?.value[item?.value?.length - 1]?.lineValue ? item?.value[item?.value?.length - 1]?.lineValue : item?.value[item?.value?.length - 1]?.value;
      }
      unit = kpiData?.kpiDetail?.kpiUnit?.toLowerCase() != 'number' && kpiData?.kpiDetail?.kpiUnit?.toLowerCase() != 'stories' && kpiData?.kpiDetail?.kpiUnit?.toLowerCase() != 'tickets' ? kpiData?.kpiDetail?.kpiUnit?.trim() : '';
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
          trend = trendObj['type']?.toLowerCase() == 'downwards' ? '-ve' : trendObj['type']?.toLowerCase() == 'upwards' ? '+ve' : '--';
        } else {
          trend = 'NA';
        }
      } else {
        let lastVal, secondLastVal;
        if (item?.value[item?.value?.length - 1]?.dataValue) {
          lastVal = item?.value[item?.value?.length - 1]?.dataValue.find(d => d.lineType === 'solid')?.value;
          secondLastVal = item?.value[item?.value?.length - 2]?.dataValue.find(d => d.lineType === 'solid')?.value;
        } else {
          lastVal = item?.value[item?.value?.length - 1]?.value;
          secondLastVal = item?.value[item?.value?.length - 2]?.value;
        }
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
          trend = '--';
        }
      }
    } else {
      trend = 'NA';
    }
    return [latest, trend, unit];
  }

  createTrendsData(kpiId) {
    let enabledKpiObj = this.updatedConfigGlobalData?.filter(x => x.kpiId == kpiId)[0];
    if (enabledKpiObj && Object.keys(enabledKpiObj)?.length != 0) {
      this.kpiTrendsObj[kpiId] = [];
      if (kpiId != 'kpi17') {
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
      } else {
        let averageCoverageIdx = this.kpiChartData[kpiId]?.findIndex((x) => x['filter']?.toLowerCase() == 'average coverage');
        if (averageCoverageIdx > -1) {
          let trendObj = {};
          const [latest, trend, unit] = this.checkLatestAndTrendValue(enabledKpiObj, this.kpiChartData[kpiId][averageCoverageIdx]);
          trendObj = {
            "hierarchyName": this.kpiChartData[kpiId][averageCoverageIdx]?.data,
            "value": latest,
            "trend": trend,
            "maturity": this.checkMaturity(this.kpiChartData[kpiId][averageCoverageIdx]),
            "maturityValue": this.kpiChartData[kpiId][averageCoverageIdx]?.maturityValue,
            "kpiUnit": unit
          };
          this.kpiTrendsObj[kpiId]?.push(trendObj);
        }
      }
      let idx = this.allKpiArray.findIndex((x) => x.kpiId == kpiId);
      this.getTableData(kpiId, idx, enabledKpiObj);
    }
  }

  fillKPIResponseCode(data) {
    Object.keys(data).forEach((key) => {
      this.kpiStatusCodeArr[key] = data[key].responseCode
    });
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
      requestObj['kpiIds'] = this.updatedConfigGlobalData?.map((item) => item.kpiId);
      if (requestObj['kpiIds']?.length) {
        this.helperService.getKpiCommentsHttp(requestObj).then((res: object) => {
          this.kpiCommentsCountObj = res;
        });
      }
    }
  }

  reloadKPI(event) {
    const idx = this.ifKpiExist(event?.kpiDetail?.kpiId)
    if (idx !== -1) {
      this.allKpiArray.splice(idx, 1);
    }
    const currentKPIGroup = this.helperService.groupKpiFromMaster(event?.kpiDetail?.kpiSource, event?.kpiDetail?.kanban, this.updatedConfigGlobalData, this.filterApplyData, this.filterData, [event?.kpiDetail?.kpiId], event.kpiDetail?.groupId, this.selectedTab);
    this.kpiLoader.add(event?.kpiDetail?.kpiId);
    if (currentKPIGroup?.kpiList?.length > 0) {
      const kpiSource = event.kpiDetail?.kpiSource?.toLowerCase();
      if (this.service.getSelectedType().toLowerCase() === 'kanban') {
        switch (kpiSource) {
          case 'sonar':
            this.postSonarKanbanKpi(currentKPIGroup, 'sonar');
            break;
          case 'jenkins':
            this.postJenkinsKanbanKpi(currentKPIGroup, 'jenkins');
            break;
          case 'zypher':
            this.postZypherKanbanKpi(currentKPIGroup, 'zypher');
            break;
          case 'bitbucket':
            this.postBitBucketKanbanKpi(currentKPIGroup, 'bitbucket');
            break;
          default:
            this.postJiraKanbanKpi(currentKPIGroup, 'jira');
        }
      } else {
        switch (kpiSource) {
          case 'sonar':
            this.postSonarKpi(currentKPIGroup, 'sonar');
            break;
          case 'jenkins':
            this.postJenkinsKpi(currentKPIGroup, 'jenkins');
            break;
          case 'zypher':
            this.postZypherKpi(currentKPIGroup, 'zypher');
            break;
          case 'bitbucket':
            this.postBitBucketKpi(currentKPIGroup, 'bitbucket');
            break;
          default:
            this.postJiraKpi(currentKPIGroup, 'jira');

        }
      }
    }
  }

  handleMaturityTableLoader() {
    const currentMaturityTableKpiList = this.kpiTableDataObj[Object.keys(this.kpiTableDataObj)[0]]?.map(data => data.kpiId)
    let loader = true;
    this.maturityTableKpiList?.forEach(kpi => {
      const idx = this.ifKpiExist(kpi);
      const idx2 = currentMaturityTableKpiList?.findIndex(kpi => kpi === kpi);
      if (idx2 === -1 || idx === -1) {
        loader = false;
      }
    });
    if (currentMaturityTableKpiList && currentMaturityTableKpiList.length > 0 && loader) {
      this.service.setMaturiyTableLoader(false);
    } else {
      this.service.setMaturiyTableLoader(true);
    }
  }

  // getLastConfigurableTrendingListData(KpiData) {
  //   if (this.tooltip && this.tooltip.sprintCountForKpiCalculation !== undefined) {
  //     if (!(this.filterApplyData['label'] === 'sqd' && this.filterApplyData['selectedMap']['sprint'].length !== 0)) {
  //       KpiData.map(kpiList => {
  //         kpiList.trendValueList?.map(trendData => {
  //           if (trendData.hasOwnProperty('filter') || trendData.hasOwnProperty('filter1')) {
  //             trendData?.value.map(projectWiseData => {
  //               const valueLength = projectWiseData.value.length;
  //               if (valueLength > this.tooltip.sprintCountForKpiCalculation) {
  //                 projectWiseData.value = projectWiseData.value.splice(-this.tooltip.sprintCountForKpiCalculation)
  //               }
  //             })
  //           } else {
  //             const valueLength = trendData.value.length;
  //             if (valueLength > this.tooltip.sprintCountForKpiCalculation) {
  //               trendData.value = trendData.value.splice(-this.tooltip.sprintCountForKpiCalculation)
  //             }
  //           }

  //         })
  //       })
  //     }
  //   }
  // }

  coundMaxNoOfSprintSelectedForProject($event) {
    let maxSprints = 0;
    if ($event.filterApplyData?.selectedMap?.sprint?.length) {
      const sprintCount = new Map();
      $event.filterApplyData?.selectedMap?.sprint.forEach(node => {
        const projectId = node.substring(node.lastIndexOf('_') + 1);
        sprintCount.set(projectId, (sprintCount.get(projectId) || 0) + 1);
      });
      maxSprints = Math.max(...sprintCount.values());
    } else {
      maxSprints = $event?.configDetails?.sprintCountForKpiCalculation
    }
    return maxSprints;
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
}
