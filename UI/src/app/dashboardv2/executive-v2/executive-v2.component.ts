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
import { Component, OnInit, OnDestroy, ViewChild, ChangeDetectorRef } from '@angular/core';
import { HttpService } from '../../services/http.service';
import { SharedService } from '../../services/shared.service';
import { HelperService } from '../../services/helper.service';
import { faList, faChartPie } from '@fortawesome/free-solid-svg-icons';
import { ActivatedRoute, Router } from '@angular/router';
import { distinctUntilChanged, isEmpty, mergeMap } from 'rxjs/operators';
import { ExportExcelComponent } from 'src/app/component/export-excel/export-excel.component';
import { ExcelService } from 'src/app/services/excel.service';

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
  selectedtype: string = 'scrum';
  configGlobalData;
  selectedPriorityFilter = {};
  selectedSonarFilter;
  selectedTestExecutionFilterData;
  sonarFilterData = [];
  testExecutionFilterData = [];
  selectedJobFilter = 'Select';
  selectedBranchFilter = 'Select';
  processedKPI11Value = {};
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
  noProjects = {};
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
  selectedTrend: any = [];
  iterationKPIData = {};
  dailyStandupKPIDetails = {};

  constructor(public service: SharedService, private httpService: HttpService, public helperService: HelperService,
    private route: ActivatedRoute, private excelService: ExcelService, private cdr: ChangeDetectorRef, private router: Router) {

  }

  arrayDeepCompare(a1, a2) {
    for (let idx = 0; idx < a1.length; idx++) {
      if (!this.helperService.deepEqual(a1[idx], a2[idx])) {
        return false;
      }
    }
    return true;
  }

  resetToDefaults() {
    this.noFilterApplyData = false;
    this.kpiLoader = new Set();
    this.kpiStatusCodeArr = {};
    this.immediateLoader = true;
    this.processedKPI11Value = {};
    this.selectedBranchFilter = 'Select';
    this.serviceObject = {};
    // this.selectedtype = 'scrum';
  }

  setGlobalConfigData(globalConfig) {
    this.configGlobalData = globalConfig[this.selectedtype?.toLowerCase()]?.filter((item) => (item.boardSlug?.toLowerCase() === this.selectedTab.toLowerCase()) || (item.boardName.toLowerCase() === this.selectedTab.toLowerCase().split('-').join(' ')))[0]?.kpis;
    if (!this.configGlobalData) {
      this.configGlobalData = globalConfig['others'].filter((item) => (item.boardSlug?.toLowerCase() === this.selectedTab.toLowerCase()) || (item.boardName.toLowerCase() === this.selectedTab.toLowerCase().split('-').join(' ')))[0]?.kpis;
    }
    this.updatedConfigGlobalData = this.configGlobalData?.filter(item => item.shown);
  }

  processKpiConfigData() {
    const disabledKpis = this.configGlobalData?.filter(item => item.shown && !item.isEnabled);
    // user can enable kpis from show/hide filter, added below flag to show different message to the user
    this.enableByUser = disabledKpis?.length ? true : false;
    // noKpis - if true, all kpis are not shown to the user (not showing kpis to the user)
    this.updatedConfigGlobalData = this.configGlobalData?.filter(item => item.shown);
    let visibleKpis = this.configGlobalData?.filter(item => item.isEnabled);
    this.kpiList = this.configGlobalData?.map((kpi) => kpi.kpiId)
    if (this.updatedConfigGlobalData?.length === 0 || visibleKpis?.length === 0) {
      this.noKpis = true;
      if (this.updatedConfigGlobalData?.length && visibleKpis?.length === 0) {
        this.enableByUser = true;
      } else {
        this.enableByUser = false;
      }
    } else {
      this.noKpis = false;
      this.enableByUser = false;
    }

    this.maturityTableKpiList = []
    this.configGlobalData?.forEach(element => {
      if (element.shown && element.isEnabled) {
        this.kpiConfigData[element.kpiId] = true;
        if (!this.kpiTrendsObj.hasOwnProperty(element.kpiId)) {
          if (this.selectedTab !== 'iteration') {
            this.createTrendsData(element.kpiId);
          }
          this.handleMaturityTableLoader();
        }
      } else {
        this.kpiConfigData[element.kpiId] = false;
      }
    });
  }


  ngOnInit() {
    const selectedTab = window.location.hash.substring(1);
    this.selectedTab = selectedTab?.split('/')[2] ? selectedTab?.split('/')[2] : 'my-knowhow';

    this.subscriptions.push(this.service.onScrumKanbanSwitch.subscribe((data) => {
      this.resetToDefaults();
      this.selectedtype = data.selectedType;
      this.kpiTrendObject = {}
      this.noProjects = this.service.noProjectsObj;
    }));

    this.subscriptions.push(this.service.onTabSwitch.subscribe((data) => {
      this.resetToDefaults();
      this.selectedTab = data.selectedBoard;
      this.noProjects = this.service.noProjectsObj;
    }));

    this.subscriptions.push(this.service.globalDashConfigData.subscribe((globalConfig) => {
      this.globalConfig = JSON.parse(JSON.stringify(globalConfig));
      this.setGlobalConfigData(globalConfig);
      let enabledKPIs = globalConfig['enabledKPIs'] || [];
      setTimeout(() => {
        this.processKpiConfigData();
        this.setUpTabs();
        enabledKPIs.forEach(element => {
          this.reloadKPI(element);
        });
      }, 500);
    }));


    this.subscriptions.push(this.service.noSprintsObs.subscribe((res) => {
      this.noFilterApplyData = res;
    }));

    this.subscriptions.push(this.service.noProjectsObjObs.subscribe((res) => {
      this.noProjects = res;
    }));

    this.subscriptions.push(this.service.mapColorToProject.pipe(mergeMap(x => {
      this.maturityTableKpiList = [];
      this.colorObj = x;
      this.trendBoxColorObj = { ...x };
      this.kpiTableDataObj = {};
      for (const key in this.trendBoxColorObj) {
        const idx = key.lastIndexOf('_');
        const nodeName = key.slice(0, idx);
        this.trendBoxColorObj[nodeName] = this.trendBoxColorObj[key];
        this.kpiTableDataObj[key] = []
      }
      this.projectCount = Object.keys(this.trendBoxColorObj)?.length;
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

    this.selectedTrend = JSON.parse(JSON.stringify(this.service.getSelectedTrends()));
    localStorage.setItem('selectedTrend', JSON.stringify(this.selectedTrend));

    this.subscriptions.push(this.service.selectedTrendsEventSubject.subscribe(trend => {
      const selectedTrendFromLS = localStorage.getItem('selectedTrend') && JSON.parse(localStorage.getItem('selectedTrend'));
      if (selectedTrendFromLS?.length > 0 && (trend.length !== selectedTrendFromLS?.length || !this.arrayDeepCompare(trend, selectedTrendFromLS))) {
        this.selectedTrend = trend;
        localStorage.setItem('selectedTrend', JSON.stringify(this.selectedTrend));
        this.kpiSelectedFilterObj = {};
        this.service.setKpiSubFilterObj(null);
      } else {
        this.service.setKpiSubFilterObj(this.service.getKpiSubFilterObj());
        localStorage.setItem('selectedTrend', JSON.stringify(trend));
      }
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


    this.subscriptions.push(this.service.triggerAdditionalFilters.subscribe((data) => {
      Object.keys(data)?.length && this.updatedConfigGlobalData.forEach(kpi => {
        Promise.resolve().then(() => {
          this.handleSelectedOption(data, kpi);
        });
      });
    }));

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
    this.sprintsOverlayVisible = this.service.getSelectedLevel()['hierarchyLevelId'] === 'project' ? true : false;
    this.selectedtype = $event.selectedType;
    if (localStorage?.getItem('completeHierarchyData')) {
      const hierarchyData = JSON.parse(localStorage.getItem('completeHierarchyData'));
      if (Object.keys(hierarchyData).length > 0 && hierarchyData[this.selectedtype?.toLowerCase()]) {
        this.hierarchyLevel = hierarchyData[this.selectedtype?.toLowerCase()];
      }
    }
    if ($event.dashConfigData && Object.keys($event.dashConfigData).length > 0) {
      this.filterData = $event.filterData;
      this.filterApplyData = $event.filterApplyData;
      this.globalConfig = $event.dashConfigData;
      this.configGlobalData = $event.dashConfigData[this.selectedtype?.toLowerCase()]?.filter((item) => (item.boardName.toLowerCase() === $event?.selectedTab?.toLowerCase()) || (item.boardName.toLowerCase() === $event?.selectedTab?.toLowerCase().split('-').join(' ')))[0]?.kpis
      if (this.selectedTab === 'release') {
        const selectedRelease = this.filterData?.filter(x => x.nodeId === this.filterApplyData?.selectedMap?.release?.[0] && x.labelName?.toLowerCase() === 'release')[0];
        const endDate = selectedRelease !== undefined ? new Date(selectedRelease?.releaseEndDate).toISOString().split('T')[0] : undefined;
        this.releaseEndDate = endDate;
        const today = new Date().toISOString().split('T')[0];
        this.timeRemaining = this.calcBusinessDays(today, endDate);
        this.service.iterationConfigData.next({ daysLeft: this.timeRemaining });
      } else if (this.selectedTab === 'iteration') {
        const selectedSprint = this.filterData?.filter(x => x.nodeId == this.filterApplyData?.selectedMap['sprint'][0])[0];
        if (selectedSprint) {
          const today = new Date().toISOString().split('T')[0];
          const endDate = new Date(selectedSprint?.sprintEndDate).toISOString().split('T')[0];
          this.timeRemaining = this.calcBusinessDays(today, endDate);
          this.service.iterationConfigData.next({ daysLeft: this.timeRemaining });
          this.iterationKPIData = {};
        }
      }
      if (!this.configGlobalData?.length && $event.dashConfigData) {
        this.configGlobalData = $event.dashConfigData[this.selectedtype?.toLowerCase()]?.filter((item) => (item.boardSlug.toLowerCase() === $event?.selectedTab?.toLowerCase()) || (item.boardName.toLowerCase() === $event?.selectedTab?.toLowerCase().split('-').join(' ')))[0]?.kpis;
        if (!this.configGlobalData) {
          this.configGlobalData = $event.dashConfigData['others']?.filter((item) => (item.boardSlug.toLowerCase() === $event?.selectedTab?.toLowerCase()) || (item.boardName.toLowerCase() === $event?.selectedTab?.toLowerCase().split('-').join(' ')))[0]?.kpis;
        }
      }


      this.updatedConfigGlobalData = this.configGlobalData?.filter(item => item.shown);

      this.tooltip = $event.configDetails;
      this.additionalFiltersArr = {};
      this.noOfDataPoints = this.selectedTab.toLowerCase() !== 'developer' && this.coundMaxNoOfSprintSelectedForProject($event);
      this.allKpiArray = [];
      this.kpiChartData = {};
      this.chartColorList = {};
      this.kpiDropdowns = {};
      this.kpiTrendsObj = {};
      this.kpiTableDataObj = {};
      this.kpiLoader = new Set();
      this.kpiStatusCodeArr = {};
      this.immediateLoader = true;
      for (const key in this.colorObj) {
        const idx = key.lastIndexOf('_');
        const nodeName = key.slice(0, idx);
        this.kpiTableDataObj[key] = [];
      }

      this.service.setAddtionalFilterBackup({});
      if (this.configGlobalData?.length) {
        // set up dynamic tabs
        this.setUpTabs();
      }

      if (!$event.filterApplyData['ids'] || !$event.filterApplyData['ids']?.length || !$event.filterApplyData['ids'][0]) {
        this.noFilterApplyData = true;
      } else {
        this.noFilterApplyData = false;
        this.filterData = $event.filterData;
        this.filterApplyData = $event.filterApplyData;
        this.noOfFilterSelected = Object.keys(this.filterApplyData).length;

        this.selectedJobFilter = 'Select';
        this.loading = $event.loading;
        if (this.filterData?.length && $event.makeAPICall) {
          this.noTabAccess = false;
          // call kpi request according to tab selected
          if (this.configGlobalData?.length > 0) {
            this.processKpiConfigData();
            const kpiIdsForCurrentBoard = this.configGlobalData?.map(kpiDetails => kpiDetails.kpiId);

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
              this.groupBitBucketKpi(kpiIdsForCurrentBoard);
            }
            this.immediateLoader = false;
            this.createKpiTableHeads(this.selectedtype?.toLowerCase());

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
    const tabsArray = new Set(this.configGlobalData?.map(element => element.shown && element.isEnabled && element?.kpiDetail?.kpiSubCategory));
    // if (this.selectedTab === 'release') {
    //   const tempArray = [...this.service.getDashConfigData()['scrum'], ...this.service.getDashConfigData()['others']];
    //   const tabTempSet = tempArray.filter(element => tabsArray.has(element.boardName));
    //   this.tabsArr = new Set(tabTempSet.map(element => element.boardName));
    // } else {
    this.tabsArr = new Set([...tabsArray].filter(Boolean));
    // }
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
  downloadExcel(kpiId, kpiName, isKanban, additionalFilterSupport, charType) {
    this.exportExcelComponent.downloadExcel(kpiId, kpiName, isKanban, additionalFilterSupport, this.filterApplyData, this.filterData, this.iSAdditionalFilterSelected, charType);
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

  // Used for grouping all Jira kpi from master data and calling Jira kpi.(only for scrum).
  groupJiraKpi(kpiIdsForCurrentBoard) {
    this.jiraKpiData = {};
    // creating a set of unique group Ids
    const groupIdSet = new Set();
    this.updatedConfigGlobalData?.forEach((obj) => {
      if (!obj['kpiDetail'].kanban && obj['kpiDetail'].kpiSource === 'Jira') {
        groupIdSet.add(obj['kpiDetail'].groupId);
      }
    });

    if (this.selectedTab === 'iteration') {
      // check for Capacity KPI and sort
      this.updatedConfigGlobalData = this.updatedConfigGlobalData.sort((a, b) => a.kpiDetail.defaultOrder - b.kpiDetail.defaultOrder);
    }

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

  handleKPIError(data) {
    data.kpiList.forEach(element => {
      this.kpiLoader.delete(element.kpiId);
      this.kpiStatusCodeArr[element.kpiId] = '500';
    });
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
      this.formatKPI17Data();
      this.createAllKpiArray(this.sonarKpiData);
      this.removeLoaderFromKPIs(this.sonarKpiData);
    } else {
      this.sonarKpiData = getData;
      this.handleKPIError(postData);
    }
  }

  formatKPI17Data() {
    if (this.sonarKpiData['kpi17']?.trendValueList?.length > 0) {
      let overallObj = {
        'filter': 'Overall',
        'value': []
      }
      for (let i = 0; i < this.sonarKpiData['kpi17']?.trendValueList?.length; i++) {
        for (let j = 0; j < this.sonarKpiData['kpi17']?.trendValueList[i]?.value?.length; j++) {
          if (this.sonarKpiData['kpi17']?.trendValueList[i]?.filter === 'Average Coverage') {
            let obj = {
              'filter': this.sonarKpiData['kpi17']?.trendValueList[i]?.filter,
              ...this.sonarKpiData['kpi17']?.trendValueList[i]?.value[j]
            }
            overallObj['value'].push(obj);
          }
        }
      }
      this.sonarKpiData['kpi17']?.trendValueList.push(overallObj);
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
      if (this.selectedtype !== 'kanban') {
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
      this.handleKPIError(postData);
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
      }, (error) => {
        // Handle error
        this.handleKPIError(postData);
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
      }, (error) => {
        // Handle error
        this.handleKPIError(postData);
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
          this.handleKPIError(postData);
        }
      }, (error) => {
        // Handle error
        this.handleKPIError(postData);
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
          this.handleKPIError(postData);
        }
      }, (error) => {
        // Handle error
        this.handleKPIError(postData);
      });
  }

  // calling post request of Zypher(scrum)
  postZypherKpi(postData, source): void {
    this.zypherKpiRequest = this.httpService.postKpi(postData, source)
      .subscribe(getData => {
        this.afterZypherKpiResponseReceived(getData, postData);
      }, (error) => {
        // Handle error
        this.handleKPIError(postData);
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
      }, (error) => {
        // Handle error
        this.handleKPIError(postData);
      });
  }

  // post request of Jira(scrum)
  postJiraKpi(postData, source): void {
    if (this.selectedTab !== 'release' && this.selectedTab !== 'backlog' && this.selectedTab !== 'iteration') {
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
            this.handleKPIError(postData);
          }

        },
          (error) => {
            // Handle error
            this.handleKPIError(postData);
          });
      return;
    } else if (this.selectedTab === 'release') {
      this.postJiraKPIForRelease(postData, source);
    } else if (this.selectedTab === 'backlog') {
      this.postJiraKPIForBacklog(postData, source);
    } else if (this.selectedTab === 'iteration') {
      //this.iterationKPIData = [];
      this.postJiraKPIForIteration(postData, source);
    }
  }

  // post request of Jira(scrum) hygiene
/**
   * Posts KPI data for the current iteration to the Jira service and processes the response.
   * Updates local KPI data and handles errors appropriately.
   * 
   * @param postData - The data to be posted to the Jira service.
   * @param source - The source identifier for the KPI data.
   * @returns void
   * @throws Handles errors internally and calls handleKPIError on failure.
   */
  postJiraKPIForIteration(postData, source): void {
    this.httpService.postKpiNonTrend(postData, source)
      .subscribe(getData => {
        if (getData !== null && getData[0] !== 'error' && !getData['error']) {
          // creating array into object where key is kpi id
          const localVariable = this.helperService.createKpiWiseId(getData);

          this.iterationKPIData = Object.assign({}, this.iterationKPIData, localVariable);
          this.removeLoaderFromKPIs(localVariable);

          if (localVariable && localVariable['kpi121']) {
            const iterationConfigData = {
              daysLeft: this.timeRemaining,
              capacity: {
                value: {
                  value: localVariable['kpi121'].trendValueList?.value ? localVariable['kpi121'].trendValueList?.value : 0
                }
              }
            };
            this.service.iterationConfigData.next(iterationConfigData);
          }
          if (this.iterationKPIData && this.iterationKPIData['kpi154']) {
            this.dailyStandupKPIDetails = this.updatedConfigGlobalData.filter(kpi => kpi.kpiId !== 'kpi154')[0].kpiDetail;
          }
        } else {
          this.handleKPIError(postData);
        }
      }, (error) => {
        // Handle error
        this.handleKPIError(postData);
      });
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
          let localVariable = this.helperService.createKpiWiseId(getData);
          this.fillKPIResponseCode(localVariable);

          this.updateXAxisTicks(localVariable);

          this.jiraKpiData = Object.assign({}, this.jiraKpiData, localVariable);
          this.createAllKpiArrayForBacklog(this.jiraKpiData);
          this.removeLoaderFromKPIs(localVariable);
        } else {
          this.handleKPIError(postData);
        }
      }, (error) => {
        // Handle error
        this.handleKPIError(postData);
      });
  }

  updateXAxisTicks(localVariable) {
    for (const kpi in localVariable) {
      const localVarKpi = localVariable[kpi].trendValueList && localVariable[kpi].xAxisValues
      if (localVarKpi) {
        localVariable[kpi].trendValueList.forEach(trendElem => {
          trendElem.value.forEach(valElem => {
            if (valElem.value.length === 5 && localVariable[kpi].xAxisValues.length === 5) {
              valElem.value.forEach((element, index) => {
                element['xAxisTick'] = localVariable[kpi].xAxisValues[index];
              });
            }
          });
        });
      }
    }
  }


  postJiraKPIForRelease(postData, source) {
    /** Temporary Fix,  sending all KPI in kpiList when refreshing kpi after field mapping change*/
    /** Todo : Need to rework when BE cache issue will be fixed */
    this.updatedConfigGlobalData.forEach(kpi => {
      if (!postData.kpiList.map(obj => obj?.kpiId).includes(kpi.kpiId)) {
        postData.kpiList.push(kpi);
      }
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
          this.handleKPIError(postData);
        }
      }, (error) => {
        // Handle error
        this.handleKPIError(postData);
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
          this.handleKPIError(postData);
        }
      }, (error) => {
        // Handle error
        this.handleKPIError(postData);
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
          this.handleKPIError(postData);
        }
      }, (error) => {
        // Handle error
        this.handleKPIError(postData);
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
          this.handleKPIError(postData);
        }
      }, (error) => {
        // Handle error
        this.handleKPIError(postData);
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
    let developerBopardKpis = this.globalConfig[this.selectedtype?.toLowerCase()]?.filter((item) => (item.boardSlug?.toLowerCase() === 'developer') || (item.boardName.toLowerCase() === 'developer'))[0]?.kpis?.map(x => x.kpiId);
    if (this.selectedTab.toLowerCase() === 'developer' && developerBopardKpis?.includes(kpiId)) {
      this.service.setBackupOfFilterSelectionState({ 'additional_level': null });
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
          trendValueList.map((x) => x[filterProp]).forEach((f) =>
            this.additionalFiltersArr[filterProp].add(f)
          );
          this.additionalFiltersArr[filterProp] = Array.from(this.additionalFiltersArr[filterProp]).map((item: string) => (item));
        });



        if (!kpiFilterChange) {
          Object.keys(this.additionalFiltersArr).forEach((filterProp) => {
            this.additionalFiltersArr[filterProp] = this.additionalFiltersArr[filterProp].map((f) => {
              return {
                nodeId: f,
                nodeName: f,
                nodeDisplayName: f,
                labelName: filterProp === 'filter1' ? 'branch' : filterProp === 'filter' ? 'branch' : 'developer'
              }
            })
          });

          this.service.setAdditionalFilters(this.additionalFiltersArr);
        }
      }
    }

    if (trendValueList?.length > 0) {
      let filterPropArr = Object.keys(trendValueList[0])?.filter((prop) => prop.includes('filter'));

      // get backup KPI filters
      this.getBackupKPIFilters(kpiId, filterPropArr);

      if (filterPropArr?.length) {

        if (filterPropArr.includes('filter')) {

          if (this.kpiSelectedFilterObj[kpiId] && Object.keys(this.kpiSelectedFilterObj[kpiId])?.length > 1) {
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
            if (this.kpiSelectedFilterObj[kpiId] && Object.keys(this.kpiSelectedFilterObj[kpiId])?.length > 0) {
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
              preAggregatedValues?.push(...trendValueList?.filter(k => k['filter1'] == (tempArr[i]?.filter1.length ? tempArr[i]?.filter1 : 'Overall') && k['filter2'] == tempArr[i]?.filter2));
            }
            if (preAggregatedValues && preAggregatedValues.length > 1) {
              const transformFilter = {}
              preAggregatedValues.forEach(obj => {
                transformFilter[obj.filter1 + 'and' + obj.filter2] = obj.value
              })
              this.kpiChartData[kpiId] = this.helperService.applyAggregationLogic(transformFilter, aggregationType, this.tooltip.percentile);
            } else {
              this.kpiChartData[kpiId] = preAggregatedValues[0]?.value ? preAggregatedValues[0]?.value : [];
            }
          }
          else if (filterPropArr.includes('filter1')
            || filterPropArr.includes('filter2')) {
            const filters = this.kpiSelectedFilterObj[kpiId]?.filter1 || this.kpiSelectedFilterObj[kpiId]?.filter2;
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

  getBackupKPIFilters(kpiId, filterPropArr) {
    const filterType = this.updatedConfigGlobalData.find(kpi => kpi?.kpiId === kpiId)?.kpiDetail?.kpiFilter?.toLowerCase();
    if (Object.keys(this.service.getKpiSubFilterObj()).includes(kpiId)) {
      this.kpiSelectedFilterObj[kpiId] = this.service.getKpiSubFilterObj()[kpiId];
    } else {
      this.getDefaultKPIFilters(kpiId, filterPropArr, filterType);
    }
    this.service.setKpiSubFilterObj(this.kpiSelectedFilterObj);
  }

  getDefaultKPIFilters(kpiId, filterPropArr, filterType) {
    if (this.kpiDropdowns[kpiId]?.length && this.kpiDropdowns[kpiId][0]['options'] && this.kpiDropdowns[kpiId][0]['options'].length) {
      if (filterPropArr.includes('filter')) {
        if (filterType && filterType !== 'multiselectdropdown') {
          this.kpiSelectedFilterObj[kpiId] = [this.kpiDropdowns[kpiId][0]['options'][0]];
        } else if (!filterType) {
          this.kpiSelectedFilterObj[kpiId] = [this.kpiDropdowns[kpiId][0]['options'][0]];
        } else {
          this.kpiSelectedFilterObj[kpiId] = [];
        }
      } else if (filterPropArr.includes('filter1')
        && filterPropArr.includes('filter2')) {
        if (this.kpiDropdowns[kpiId]?.length > 1) {
          this.kpiSelectedFilterObj[kpiId] = {};
          for (let i = 0; i < this.kpiDropdowns[kpiId].length; i++) {
            if (filterType?.toLowerCase() === 'multitypefilters') {
              if (this.kpiDropdowns[kpiId][i].filterType.toLowerCase() === 'radiobtn') {
                this.kpiSelectedFilterObj[kpiId]['filter' + (i + 1)] = [this.kpiDropdowns[kpiId][i].options[0]];
              } else {
                this.kpiSelectedFilterObj[kpiId]['filter' + (i + 1)] = [];
              }
            } else {
              this.kpiSelectedFilterObj[kpiId]['filter' + (i + 1)] = [this.kpiDropdowns[kpiId][i].options[0]];
            }
          }
        }
      } else {
        this.kpiSelectedFilterObj[kpiId] = {};
        this.kpiSelectedFilterObj[kpiId]['filter1'] = [this.kpiDropdowns[kpiId][0]['options'][0]];
      }
    }
  }

  getBackupKPIFiltersForRelease(kpiId, filterPropArr) {
    const filterType = this.updatedConfigGlobalData.find(kpi => kpi?.kpiId === kpiId)?.kpiDetail?.kpiFilter?.toLowerCase();
    if (Object.keys(this.service.getKpiSubFilterObj()).includes(kpiId)) {
      this.kpiSelectedFilterObj[kpiId] = this.service.getKpiSubFilterObj()[kpiId];
    } else {
      this.getDefaultKPIFiltersForRelease(kpiId, filterPropArr, filterType);
    }
    this.service.setKpiSubFilterObj(this.kpiSelectedFilterObj);
  }

  /**
     * Sets the default KPI filters based on the provided KPI ID, filter properties, and filter type.
     * It populates the kpiSelectedFilterObj with default options from kpiDropdowns.
     * 
     * @param {string} kpiId - The ID of the KPI for which to set filters.
     * @param {string[]} filterPropArr - An array of filter property names to determine filter behavior.
     * @param {string} [filterType] - The type of filter to apply (optional).
     * @returns {void}
     */
  getDefaultKPIFiltersForRelease(kpiId, filterPropArr, filterType) {
    if (this.kpiDropdowns[kpiId]?.length && this.kpiDropdowns[kpiId][0]['options'] && this.kpiDropdowns[kpiId][0]['options'].length) {
      if (filterPropArr.includes('filter')) {
        if (filterType && filterType !== 'multiselectdropdown') {
          this.kpiSelectedFilterObj[kpiId] = [this.kpiDropdowns[kpiId][0]['options'][0]];
        } else if (!filterType) {
          this.kpiSelectedFilterObj[kpiId] = [this.kpiDropdowns[kpiId][0]['options'][0]];
        } else {
          this.kpiSelectedFilterObj[kpiId] = [];
        }
      } else if (filterPropArr.includes('filter1')
        && filterPropArr.includes('filter2')) {
        if (this.kpiDropdowns[kpiId]?.length > 1) {
          this.kpiSelectedFilterObj[kpiId] = {};
          for (let i = 0; i < this.kpiDropdowns[kpiId].length; i++) {
            this.kpiSelectedFilterObj[kpiId]['filter' + (i + 1)] = [this.kpiDropdowns[kpiId][i].options[0]];
          }
        }
      } else {
        this.kpiSelectedFilterObj[kpiId] = {};
        this.kpiSelectedFilterObj[kpiId]['filter1'] = [this.kpiDropdowns[kpiId][0]['options'][0]];
      }
    }
  }

  getBackupKPIFiltersForBacklog(kpiId) {
    const trendValueList = this.allKpiArray[this.ifKpiExist(kpiId)]?.trendValueList;
    const filters = this.allKpiArray[this.ifKpiExist(kpiId)]?.filters;
    const filterType = this.updatedConfigGlobalData.find(kpi => kpi?.kpiId === kpiId)?.kpiDetail?.kpiFilter?.toLowerCase();

    if (Object.keys(this.service.getKpiSubFilterObj()).includes(kpiId)) {
      this.kpiSelectedFilterObj[kpiId] = this.service.getKpiSubFilterObj()[kpiId];
    } else {
      this.getDefaultKPIFiltersForBacklog(kpiId, trendValueList, filters, filterType);
    }
    this.service.setKpiSubFilterObj(this.kpiSelectedFilterObj);
  }

  getDefaultKPIFiltersForBacklog(kpiId, trendValueList, filters, filterType) {
    if (filters && Object.keys(filters).length !== 0) {
      if (this.kpiDropdowns[kpiId][0]['options']?.length) {
        if (filterType && filterType !== 'multiselectdropdown' || !filterType) {
          this.kpiSelectedFilterObj[kpiId] = [this.kpiDropdowns[kpiId][0]['options'][0]];
        }
      }
    } else if (trendValueList?.length > 0 && trendValueList[0]?.hasOwnProperty('filter')) {
      this.kpiSelectedFilterObj[kpiId] = { filter1: ['Overall'] };
    }

    if (trendValueList?.length > 0 && trendValueList[0]?.hasOwnProperty('filter1')) {
      this.kpiSelectedFilterObj[kpiId] = {};
      for (let i = 0; i < this.kpiDropdowns[kpiId].length; i++) {
        this.kpiSelectedFilterObj[kpiId]['filter' + (i + 1)] = [this.kpiDropdowns[kpiId][i].options[0]];
      }
    }
  }

  getChartDataForBacklog(kpiId, idx, aggregationType) {
    const trendValueList = this.allKpiArray[idx]?.trendValueList;
    this.kpiThresholdObj[kpiId] = this.allKpiArray[idx]?.thresholdValue ? this.allKpiArray[idx]?.thresholdValue : null;

    if (trendValueList?.length) {
      // get backup KPI filters
      this.getBackupKPIFiltersForBacklog(kpiId);
    }


    if (trendValueList?.length > 0 && trendValueList[0]?.hasOwnProperty('filter')) {
      if (Object.values(this.kpiSelectedFilterObj[kpiId]).length > 1) {
        const tempArr = {};
        for (let i = 0; i < this.kpiSelectedFilterObj[kpiId]?.length; i++) {
          tempArr[this.kpiSelectedFilterObj[kpiId][i]] = (trendValueList?.filter(x => x['filter'] == this.kpiSelectedFilterObj[kpiId][i])[0]?.value);
        }
        if (this.getChartType(kpiId) === 'progress-bar') {
          this.kpiChartData[kpiId] = this.applyAggregationLogicForProgressBar(tempArr);
        } else {
          if (tempArr) {
            this.kpiChartData[kpiId] = this.helperService.applyAggregationLogic(tempArr, aggregationType, this.tooltip.percentile);
          }
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
        if (this.kpiSelectedFilterObj[kpiId] && Object.keys(this.kpiSelectedFilterObj[kpiId]).length === 1) {
          preAggregatedValues = [...preAggregatedValues, ...(trendValueList && trendValueList['value'] ? trendValueList['value'] : trendValueList)?.filter(x => x['filter1'] == filters[i] || x['filter2'] == filters[i])];
        } else {
          preAggregatedValues = [...preAggregatedValues, ...(trendValueList && trendValueList['value'] ? trendValueList['value'] : trendValueList)?.filter(x => x['filter1'] == filters[i] && x['filter2'] == filter2[i])];
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

    if (this.colorObj && Object.keys(this.colorObj)?.length > 0 && !['kpi161', 'kpi146', 'kpi148', 'kpi169'].includes(kpiId)) {
      this.kpiChartData[kpiId] = this.generateColorObj(kpiId, this.kpiChartData[kpiId]);
    }

    // this.createTrendData(kpiId);
    if (kpiId !== 'kpi151' && kpiId !== 'kpi152' && kpiId !== 'kpi155') {
      this.createTrendsData(kpiId);
    }
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

  /*   createTrendData(kpiId) {
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
  
    } */

  getChartDataforRelease(kpiId, idx, aggregationType?, kpiFilterChange = false) {
    const trendValueList = this.allKpiArray[idx]?.trendValueList ? JSON.parse(JSON.stringify(this.allKpiArray[idx]?.trendValueList)) : {};
    this.kpiThresholdObj[kpiId] = this.allKpiArray[idx]?.thresholdValue ? this.allKpiArray[idx]?.thresholdValue : null;

    if (trendValueList?.length > 0) {
      let filterPropArr = Object.keys(trendValueList[0])?.filter((prop) => prop.includes('filter'));

      // get backup KPI filters
      // this.getBackupKPIFiltersForRelease(kpiId, filterPropArr);
      this.getBackupKPIFilters(kpiId, filterPropArr);
    }

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
            this.kpiChartData[kpiId] = this.applyAggregationLogic(preAggregatedValues);
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
            this.kpiChartData[kpiId] = this.applyAggregationLogic(preAggregatedValues);
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
      let filtersApplied = [];

      
      for (const key in this.colorObj) {
          filtersApplied.push(this.colorObj[key].nodeId)
      }

      filtersApplied.forEach((hierarchyId) => {
        let obj = {
          'kpiId': kpiId,
          'kpiName': this.allKpiArray[idx]?.kpiName,
          'frequency': enabledKpi?.kpiDetail?.xaxisLabel,
          'show': enabledKpi?.isEnabled && enabledKpi?.shown,
          'hoverText': [],
          'order': enabledKpi?.order
        }
        let chosenItem = iterativeEle?.filter((item) => item['data'] == this.colorObj[hierarchyId]?.nodeDisplayName)[0];

        let trendData = this.kpiTrendsObj[kpiId]?.filter(x => x['hierarchyId']?.toLowerCase() == hierarchyId?.toLowerCase())[0];
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
        let kpiIndex = this.kpiTableDataObj[hierarchyId]?.findIndex((x) => x.kpiId == kpiId);
        if (kpiIndex > -1) {
          this.kpiTableDataObj[hierarchyId]?.splice(kpiIndex, 1);
        }
        if (enabledKpi?.isEnabled && enabledKpi?.shown && this.kpiTableDataObj[hierarchyId]) {
          this.kpiTableDataObj[hierarchyId] = [...this.kpiTableDataObj[hierarchyId], obj];
        }
        this.sortingRowsInTable(hierarchyId);
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
    if (arr1?.length > 0) {
      for (let i = 0; i < arr1?.length; i++) {
        for (let j = 0; j < arr2?.length; j++) {
          arr.push({ filter1: arr1[i], filter2: arr2[j] });
        }
      }
    } else {
      /** Handled for Multi Type Dropdown */
      return [
        {
          filter1: [],
          filter2: arr2
        }
      ]
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
        // this.setFilterValueIfAlreadyHaveBackup(data[key]?.kpiId, {}, ['Overall'], filters)
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
        this.getDropdownArrayForBacklog(data[key]?.kpiId);

        const agType = this.updatedConfigGlobalData?.filter(x => x.kpiId == data[key]?.kpiId)[0]?.kpiDetail?.aggregationCriteria;
        this.getChartDataForBacklog(data[key]?.kpiId, (this.allKpiArray?.length - 1), agType);

      } else {
        this.getDropdownArrayForCard(data[key]?.kpiId);
        this.getChartDataForCard(data[key]?.kpiId, this.ifKpiExist(data[key]?.kpiId));
      }
    }
  }

  getChartDataForCard(kpiId, idx) {
    const trendValueList = this.allKpiArray[idx]?.trendValueList ? JSON.parse(JSON.stringify(this.allKpiArray[idx]?.trendValueList)) : {};



    if (trendValueList && Object.keys(trendValueList)?.length > 0) {
      // get backup KPI filters
      this.getBackupKPIFiltersForBacklog(kpiId);



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
            if (kpiId === 'kpi171') {
              this.kpiChartData[kpiId] = [this.helperService.aggregationCycleTime(preAggregatedValues)];
            } else {
              this.kpiChartData[kpiId] = this.applyAggregationLogic(preAggregatedValues);
            }
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
        if (trendValueList?.hasOwnProperty('value') && trendValueList['value']?.length > 0) {
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
    this.kpi171RoundOff();
  }

  getChartDataForCardWithCombinationFilter(kpiId, trendValueList) {
    this.getBackupKPIFiltersForBacklog(kpiId);
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
      this.kpi171Check(kpiId, preAggregatedValues);
    } else {
      this.kpiChartData[kpiId] = [...preAggregatedValues];
    }
    this.kpi171RoundOff();
  }

  kpi171RoundOff() {
    if (this.kpiChartData.hasOwnProperty('kpi171') && this.kpiChartData['kpi171'].length) {
      const roundOffData = [...this.kpiChartData['kpi171']]
      if (roundOffData && roundOffData?.length) {
        roundOffData[0]['data'] = roundOffData[0]?.data?.map(item => ({
          ...item,
          value: Math.round(item.value),   // Round off `value`
          value1: Math.round(item.value1) // Round off `value1`
        }));
        this.kpiChartData['kpi171'] = roundOffData;
      }
    }
  }

  kpi171Check(kpiId, preAggregatedValues) {
    if (kpiId === 'kpi171') {
      //calculate number of days for lead time
      let kpi171preAggregatedValues = JSON.parse(JSON.stringify(preAggregatedValues));
      kpi171preAggregatedValues = this.helperService.aggregationCycleTime(kpi171preAggregatedValues);
      this.kpiChartData[kpiId] = [kpi171preAggregatedValues];

    } else {
      this.kpiChartData[kpiId] = this.applyAggregationLogic(preAggregatedValues);
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
      delete this.kpiSelectedFilterObj[kpiId]['filter2'];
      const idx = this.ifKpiExist(kpiId);
      if (idx !== -1) {
        this.allKpiArray.splice(idx, 1);
      }

      const kpi171Payload = this.updatedConfigGlobalData?.map(kpiDetails => kpiDetails.kpiId);
      const groupIdSet = new Set();
      this.updatedConfigGlobalData?.forEach((obj) => {
        if (!obj['kpiDetail']?.kanban && obj['kpiDetail']?.kpiSource === 'Jira') {
          groupIdSet.add(obj['kpiDetail']?.groupId);
        }
      });

      // sending requests after grouping the the KPIs according to group Id
      groupIdSet.forEach((groupId) => {
        if (groupId) {
          this.kpiJira = this.helperService.groupKpiFromMaster('Jira', false, this.updatedConfigGlobalData, this.filterApplyData, this.filterData, kpi171Payload, groupId, 'backlog');
          const kpi171 = this.kpiJira.kpiList.filter(kpi => kpi.kpiId === 'kpi171')[0];
          if (kpi171) {
            kpi171['filterDuration'] = {
              duration: this.durationFilter.includes('Week') ? 'WEEKS' : 'MONTHS',
              value: !isNaN(+this.durationFilter.split(' ')[1]) ? +this.durationFilter.split(' ')[1] : 1
            };
            this.kpiJira.kpiList = [kpi171];
            this.kpiLoader.add('kpi171');

            this.httpService.postKpiNonTrend(this.kpiJira, 'jira').subscribe(data => {
              const kpi171Data = data.find(kpi => kpi.kpiId === kpiId);
              this.allKpiArray.push(kpi171Data);
              this.getChartDataForCardWithCombinationFilter(kpiId, JSON.parse(JSON.stringify(kpi171Data.trendValueList)));
              this.kpiLoader.delete('kpi171');
            });
          }
        }
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
    return aggregatedArr;
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

  checkSprint(value, unit, kpiId) {
    if ((this.kpiSelectedFilterObj[kpiId]?.hasOwnProperty('filter1') && this.kpiSelectedFilterObj[kpiId]['filter1']?.length > 0 && this.kpiSelectedFilterObj[kpiId]['filter1'][0]?.toLowerCase() !== 'overall')
      || (this.kpiSelectedFilterObj[kpiId]?.hasOwnProperty('filter2') && this.kpiSelectedFilterObj[kpiId]['filter2']?.length > 0 && this.kpiSelectedFilterObj[kpiId]['filter2'][0]?.toLowerCase() !== 'overall')) {
      return '-'
    } else {
      return Math.floor(value) < value ? `${Math.round(value)} ${unit}` : `${value} ${unit}`;
    }
  }

  handleArrowClick(kpi, label, tableValues) {
    const idx = this.ifKpiExist(kpi?.kpiId);
    const basicConfigId = this.service.selectedTrends[0].basicProjectConfigId;
    this.httpService.getkpiColumns(basicConfigId, kpi.kpiId).subscribe(response => {
      if (response['success']) {
        this.exportExcelComponent.dataTransformForIterationTableWidget([], [], response['data']['kpiColumnDetails'], tableValues, kpi?.kpiName + ' / ' + label, kpi.kpiId)
      }
    });
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

  /**
   * Checks if the KPI data is zero or not based on various conditions and KPI IDs.
   * @param {Object} kpi - The KPI object containing details and ID to evaluate.
   * @returns {boolean} - Returns true if data is present and greater than zero, otherwise false.
   */
  checkIfZeroData(kpi) {
    if (this.checkIfDataPresent(kpi) && this.service.getSelectedTrends()[0]?.labelName?.toLowerCase() === 'project') {
      if (this.service.getSelectedTrends()?.length === 1) {

        /** 19th Nov, 2024: Decision was taken to bypass all the checks and show a warning in case of failed processor run */

        // let data = this.kpiChartData[kpi.kpiId];
        // let dataValue = 0;

        // flow load and flow distributions KPIs
        // if (kpi.kpiId === 'kpi148' || kpi.kpiId === 'kpi146') {
        //   if (this.kpiChartData[kpi.kpiId]?.length) {
        //     return true;
        //   }
        // }

        // // Cycle Time
        // if (kpi.kpiId === 'kpi171') {
        //   if (this.kpiChartData[kpi.kpiId]?.length && this.kpiChartData[kpi.kpiId][0]?.data?.length > 0) {
        //     return true;
        //   } else {
        //     return false;
        //   }
        // }

        // // Refinement Rejection Rate and Production Defects Ageing
        // if (kpi.kpiId === 'kpi139' || kpi.kpiId === 'kpi127') {
        //   if (this.kpiChartData[kpi.kpiId]?.length && this.kpiChartData[kpi.kpiId][0].value?.length) {
        //     if (Array.isArray(data[0].value) && data[0].value.length) {
        //       data[0].value.forEach(element => {
        //         dataValue += parseInt(element.data);
        //       });
        //     }
        //   }
        // }

        // // Sonar Code Quality, Test Execution and Pass Percentage KPI , PI Predicatability
        // else if (kpi.kpiId === 'kpi168' || kpi.kpiId === 'kpi70' || kpi.kpiId === 'kpi153') {
        //   if (this.kpiChartData[kpi.kpiId]?.length && this.kpiChartData[kpi.kpiId][0].value?.length > 0) {
        //     if (Array.isArray(data[0].value) && data[0].value) {
        //       data[0].value.forEach(element => {
        //         if (kpi.kpiId === 'kpi70') {
        //           dataValue += element.value;
        //         } else if (Array.isArray(element.dataValue) && element.dataValue.length) {
        //           element.dataValue.forEach(subElem => {
        //             dataValue += subElem.value;
        //           });
        //         }
        //       });
        //     }
        //   }
        // }

        // else if (Array.isArray(data[0].value)) {
        //   data[0].value.forEach(element => {
        //     dataValue += element.value;
        //   });
        // }

        // if (dataValue > 0) {
        //   return true;
        // } else {
        let processorLastRun = this.findTraceLogForTool(kpi.kpiDetail.combinedKpiSource || kpi.kpiDetail.kpiSource);
        // processorLastRunSuccess = false;
        if (processorLastRun == undefined || processorLastRun == null || processorLastRun.executionEndedAt == 0) {
          return true;
        } else if (!processorLastRun?.executionSuccess) {
          if (this.kpiStatusCodeArr[kpi.kpiId] !== '201') {
            this.kpiStatusCodeArr[kpi.kpiId] = '203';
          }
          return true;
        } else if (processorLastRun?.executionSuccess) {
          return true;
        } else {
          return true;
        }
        // }
      } else {
        return true;
      }
      // return false;
    } else {
      return this.checkIfDataPresent(kpi);
    }
  }

  // /**
  //  * Determines if the execution of a specified processor was successful based on its trace log.
  //  * @param processorName - The name of the processor to check, case insensitive.
  //  * @returns A boolean indicating whether the execution was successful.
  //  * @throws No exceptions are thrown by this function.
  //  */
  // showExecutionDate(processorName) {
  //   const traceLog = this.findTraceLogForTool(processorName.toLowerCase());
  //   if (traceLog == undefined || traceLog == null || traceLog.executionEndedAt == 0) {
  //     return false;
  //   } else {
  //     return traceLog?.executionSuccess === true ? true : false;
  //   }
  // }

  /**
   * Retrieves the trace log for a specified processor by its name.
   * @param processorName - The name of the processor, which may include a path.
   * @returns The log details of the processor if found, otherwise undefined.
   */
  findTraceLogForTool(processorName) {
    const sourceArray = (processorName.includes('/')) ? processorName.split('/') : [processorName];
    return this.service.getProcessorLogDetails().find(ptl => sourceArray.includes(ptl['processorName']));
  }


  checkIfDataPresent(kpi) {
    if (this.kpiStatusCodeArr[kpi.kpiId]) {
      if ((this.kpiStatusCodeArr[kpi.kpiId] === '200' || this.kpiStatusCodeArr[kpi.kpiId] === '201' || this.kpiStatusCodeArr[kpi.kpiId] === '203') && (kpi.kpiId === 'kpi148' || kpi.kpiId === 'kpi146')) {
        if (this.kpiChartData[kpi.kpiId]?.length) {
          return true;
        }
      }

      else if ((this.kpiStatusCodeArr[kpi.kpiId] === '200' || this.kpiStatusCodeArr[kpi.kpiId] === '201' || this.kpiStatusCodeArr[kpi.kpiId] === '203') && (kpi.kpiId === 'kpi139' || kpi.kpiId === 'kpi127')) {
        if (this.kpiChartData[kpi.kpiId]?.length && this.kpiChartData[kpi.kpiId][0].value?.length) {
          return true;
        }
      }

      else if ((this.kpiStatusCodeArr[kpi.kpiId] === '200' || this.kpiStatusCodeArr[kpi.kpiId] === '201' || this.kpiStatusCodeArr[kpi.kpiId] === '203') && (kpi.kpiId === 'kpi168' || kpi.kpiId === 'kpi70' || kpi.kpiId === 'kpi153' || kpi.kpiId === 'kpi135')) {
        if (this.kpiChartData[kpi.kpiId]?.length && this.kpiChartData[kpi.kpiId][0].value?.length > 0) {
          return true;
        }
      }

      else if ((this.kpiStatusCodeArr[kpi.kpiId] === '200' || this.kpiStatusCodeArr[kpi.kpiId] === '201' || this.kpiStatusCodeArr[kpi.kpiId] === '203') && (kpi.kpiId === 'kpi171')) {
        if (this.kpiChartData[kpi.kpiId][0]?.data?.length > 0) {
          return true;
        } else {
          return false;
        }
      }
      else {
        return (this.kpiStatusCodeArr[kpi.kpiId] === '200' || this.kpiStatusCodeArr[kpi.kpiId] === '201' || this.kpiStatusCodeArr[kpi.kpiId] === '203') && this.helperService.checkDataAtGranularLevel(this.kpiChartData[kpi.kpiId], kpi.kpiDetail.chartType, this.selectedTab);
      }
    }
    return false;
  }

  checkIfPartialDataPresent(kpi) {
    let kpiData = this.ifKpiExist(kpi.kpiId) >= 0 ? this.allKpiArray[this.ifKpiExist(kpi.kpiId)]?.trendValueList : null;
    let filters = kpiData?.length ? kpiData.map((x) => x.filter1) : null;
    if (kpiData && filters && kpi.kpiId !== 'kpi171') {
      return this.checkPartialDataCondition(kpi, kpiData, filters);
    } else {
      if (kpi.kpiId === 'kpi171') {
        return this.checkIfPartialDataForKpi171(kpiData);
      }
      return false;
    }
  }

  checkPartialDataCondition(kpi, kpiData, filters) {
    if (filters.length === 2) {
      let partialKpiData1 = kpiData.filter(x => x.filter1 === filters[0]);
      let partialKpiData2 = kpiData.filter(x => x.filter1 === filters[1]);
      if ((this.helperService.checkDataAtGranularLevel(partialKpiData1, kpi.kpiDetail.chartType, this.selectedTab) && !this.helperService.checkDataAtGranularLevel(partialKpiData2, kpi.kpiDetail.chartType, this.selectedTab)) ||
        (this.helperService.checkDataAtGranularLevel(partialKpiData2, kpi.kpiDetail.chartType, this.selectedTab) && !this.helperService.checkDataAtGranularLevel(partialKpiData1, kpi.kpiDetail.chartType, this.selectedTab))) {
        return true;
      }
    } else {
      return false;
    }
  }

  checkIfPartialDataForKpi171(kpiData) {
    kpiData = kpiData?.value;
    let filters = kpiData?.length ? kpiData.map((x) => x.filter1) : null;
    for (let i = 0; i < filters?.length; i++) {
      let partialKpiData = kpiData.filter(x => x.filter1 === filters[i])[0];
      if (partialKpiData && partialKpiData.data?.length) {
        return true;
      }
    }
  }


  generateColorObj(kpiId, arr) {
    // If the arr is empty, return an empty array
    if (!arr?.length) return [];

    const finalArr = [];
    this.chartColorList[kpiId] = [];

    for (let i = 0; i < arr?.length; i++) {
      for (const key in this.colorObj) {

        let selectedNode = this.filterData.filter(x => x.nodeDisplayName === arr[i].value[0].sprojectName);
        let selectedId = selectedNode[0]?.nodeId;

        if (kpiId == 'kpi17' && this.colorObj[key]?.nodeId == selectedId) {
          this.chartColorList[kpiId].push(this.colorObj[key]?.color);
          finalArr.push(JSON.parse(JSON.stringify(arr[i])));
        }
        else if (this.colorObj[key]?.nodeId == selectedId) {
          this.chartColorList[kpiId].push(this.colorObj[key]?.color);
          finalArr.push(arr[i]);
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
        dropdownArr.forEach((arr, i) => {
          arr = Array.from(arr);
          const obj = {};
          const kpiObj = this.updatedConfigGlobalData?.filter(x => x['kpiId'] == kpiId)[0];
          if (this.selectedTab.toLowerCase() !== 'developer' || kpiId !== 'kpi168') {
            if (kpiObj && kpiObj['kpiDetail']?.hasOwnProperty('kpiFilter') && ((kpiObj['kpiDetail']['kpiFilter']?.toLowerCase() == 'multiselectdropdown') || kpiObj['kpiDetail']['kpiFilter']?.toLowerCase() == 'multitypefilters')) {
              const index = arr?.findIndex(x => x?.toLowerCase() == 'overall');
              if (index > -1) {
                arr?.splice(index, 1);
              }
            }
          }

          if (this.allKpiArray[idx]?.filters) {
            const filterConfig = this.allKpiArray[idx].filters;
            obj['filterType'] = filterConfig['filter' + (i + 1)]?.filterType ? filterConfig['filter' + (i + 1)]?.filterType : 'Select a filter';
          } else {
            obj['filterType'] = 'Select a filter';
          }
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
      } else if (kpi?.kpiDetail?.kpiFilter && kpi?.kpiDetail?.kpiFilter.toLowerCase() === 'multitypefilters') {
        this.kpiSelectedFilterObj[kpi?.kpiId] = event;
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
          const trendValueList = this.allKpiArray[this.ifKpiExist(kpi.kpiId)]?.trendValueList;
          if (trendValueList?.length && (trendValueList[0].hasOwnProperty('filter1') || trendValueList[0].hasOwnProperty('filter2'))) {
            if (this.kpiSelectedFilterObj[kpi?.kpiId]) {
              this.kpiSelectedFilterObj[kpi?.kpiId]['filter' + event.index] = [event.value];
            } else {
              this.kpiSelectedFilterObj[kpi?.kpiId] = {};
              this.kpiSelectedFilterObj[kpi?.kpiId]['filter' + event.index] = [event.value];
            }
          } else {
            this.kpiSelectedFilterObj[kpi?.kpiId] = [event.value];
          }
        } else {
          this.kpiSelectedFilterObj[kpi?.kpiId] = [];
          this.kpiSelectedFilterObj[kpi?.kpiId].push(event);
        }
      }

      this.service.setKpiSubFilterObj(this.kpiSelectedFilterObj);

      this.getChartData(kpi?.kpiId, this.ifKpiExist(kpi?.kpiId), kpi?.kpiDetail?.aggregationCriteria, true);
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

    this.service.setKpiSubFilterObj(this.kpiSelectedFilterObj);

    this.getChartDataforRelease(kpi?.kpiId, this.ifKpiExist(kpi?.kpiId), true);
  }

  handleSelectedOptionOnBacklog(event, kpi) {
    const selectedFilterBackup = this.kpiSelectedFilterObj[kpi?.kpiId];
    this.kpiSelectedFilterObj[kpi?.kpiId] = {};
    /** When we have single dropdown */
    if (event && Object.keys(event)?.length !== 0 && typeof event === 'object' && !selectedFilterBackup?.hasOwnProperty('filter2')) {
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

    this.service.setKpiSubFilterObj(this.kpiSelectedFilterObj);

    this.getChartDataForBacklog(kpi?.kpiId, this.ifKpiExist(kpi?.kpiId), kpi?.kpiDetail?.aggregationCriteria);
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

    this.service.setKpiSubFilterObj(this.kpiSelectedFilterObj);

    this.getChartDataForCard(kpi?.kpiId, this.ifKpiExist(kpi?.kpiId));
  }


  checkMaturity(item) {
    let maturity = item.maturity;
    if (maturity == undefined) {
      return 'NA';
    }
    maturity = 'M' + maturity;
    return maturity;
  }

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
            if (isNaN(Number(this.kpiChartData[kpiId][i]?.data))) {
              let selectedNode = this.filterData.filter(x => x.nodeDisplayName === this.kpiChartData[kpiId][i]?.data);
              let selectedId = selectedNode[0].nodeId;
              trendObj = {
                "hierarchyName": this.kpiChartData[kpiId][i]?.data,
                "hierarchyId": selectedId,
                "value": latest,
                "trend": trend,
                "maturity": kpiId != 'kpi3' && kpiId != 'kpi53' ?
                  this.checkMaturity(this.kpiChartData[kpiId][i])
                  : 'M' + this.kpiChartData[kpiId][i]?.maturity,
                "maturityValue": this.kpiChartData[kpiId][i]?.maturityValue,
                "kpiUnit": unit
              };
            }
            if (kpiId === 'kpi997') {
              trendObj['value'] = 'NA';
            }
            if (!this.kpiTrendsObj[kpiId].map(x => x.hierarchyId).includes(trendObj['hierarchyId'])) {
              this.kpiTrendsObj[kpiId]?.push(trendObj);
            }
          }
        }
      } else {
        let averageCoverageIdx = this.kpiChartData[kpiId]?.findIndex((x) => x['filter']?.toLowerCase() == 'average coverage');
        if (averageCoverageIdx > -1) {
          let trendObj = {};
          const [latest, trend, unit] = this.checkLatestAndTrendValue(enabledKpiObj, this.kpiChartData[kpiId][averageCoverageIdx]);
          let selectedNode = this.filterData.filter(x => x.nodeName === this.kpiChartData[kpiId][averageCoverageIdx]?.data);
          let selectedId = selectedNode[0].nodeId;
          trendObj = {
            "hierarchyName": this.kpiChartData[kpiId][averageCoverageIdx]?.data,
            "hiearchyId": selectedId,
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
      let kpiIdsForCurrentBoard;
      if (this.service.getSelectedType().toLowerCase() === 'kanban') {
        kpiIdsForCurrentBoard = this.configGlobalData?.filter(kpi => kpi.kpiDetail.groupId === event.kpiDetail.groupId).map(kpiDetails => kpiDetails.kpiId)
        switch (kpiSource) {
          case 'sonar':
            this.groupSonarKanbanKpi(kpiIdsForCurrentBoard);
            break;
          case 'jenkins':
            this.groupJenkinsKanbanKpi(kpiIdsForCurrentBoard);
            break;
          case 'zypher':
            this.groupZypherKanbanKpi(kpiIdsForCurrentBoard);
            break;
          case 'bitbucket':
            this.groupBitBucketKanbanKpi(kpiIdsForCurrentBoard);
            break;
          default:
            this.groupJiraKanbanKpi(kpiIdsForCurrentBoard);
        }
      } else {
        switch (kpiSource) {
          case 'sonar':
            /** Temporary Fix,  sending all KPI in kpiList when refreshing kpi after field mapping change*/
            /** Todo : Need to rework when BE cache issue will be fixed */
            // this.postSonarKpi(currentKPIGroup, 'sonar');
            kpiIdsForCurrentBoard = this.configGlobalData?.filter(kpi => kpi.kpiDetail.groupId === event.kpiDetail.groupId).map(kpiDetails => kpiDetails.kpiId);
            this.groupSonarKpi(kpiIdsForCurrentBoard);
            break;
          case 'jenkins':
            /** Temporary Fix,  sending all KPI in kpiList when refreshing kpi after field mapping change*/
            /** Todo : Need to rework when BE cache issue will be fixed */
            // this.postJenkinsKpi(currentKPIGroup, 'jenkins');
            kpiIdsForCurrentBoard = this.configGlobalData?.filter(kpi => kpi.kpiDetail.groupId === event.kpiDetail.groupId).map(kpiDetails => kpiDetails.kpiId);
            this.groupJenkinsKpi(kpiIdsForCurrentBoard);
            break;
          case 'zypher':
            /** Temporary Fix,  sending all KPI in kpiList when refreshing kpi after field mapping change*/
            /** Todo : Need to rework when BE cache issue will be fixed */
            // this.postZypherKpi(currentKPIGroup, 'zypher');
            kpiIdsForCurrentBoard = this.configGlobalData?.filter(kpi => kpi.kpiDetail.groupId === event.kpiDetail.groupId).map(kpiDetails => kpiDetails.kpiId);
            this.groupZypherKpi(kpiIdsForCurrentBoard);
            break;
          case 'bitbucket':
            /** Temporary Fix,  sending all KPI in kpiList when refreshing kpi after field mapping change*/
            /** Todo : Need to rework when BE cache issue will be fixed */
            // this.postBitBucketKpi(currentKPIGroup, 'bitbucket');
            kpiIdsForCurrentBoard = this.configGlobalData?.filter(kpi => kpi.kpiDetail.groupId === event.kpiDetail.groupId).map(kpiDetails => kpiDetails.kpiId);
            this.groupBitBucketKpi(kpiIdsForCurrentBoard);
            break;
          default:
            /** Temporary Fix,  sending all KPI in kpiList when refreshing kpi after field mapping change*/
            /** Todo : Need to rework when BE cache issue will be fixed */
            // this.postJiraKpi(currentKPIGroup, 'jira');
            kpiIdsForCurrentBoard = this.configGlobalData?.filter(kpi => kpi.kpiDetail.groupId === event.kpiDetail.groupId).map(kpiDetails => kpiDetails.kpiId);
            this.groupJiraKpi(kpiIdsForCurrentBoard);
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

  /**
     * Calculates the number of business days between two Date objects, inclusive.
     * Business days are defined as weekdays (Monday to Friday), excluding weekends.
     * Returns 0 if the second date is earlier than the first date.
     *
     * @param dDate1 - The start date as a Date object.
     * @param dDate2 - The end date as a Date object.
     * @returns The number of business days between the two dates.
     * @throws Returns 0 if dDate2 is earlier than dDate1.
     */
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

  /**
   * Checks the Y-axis label for a given KPI based on its trend data and selected filters.
   * @param {Object} kpi - The KPI object containing kpiId and other details.
   * @returns {string | undefined} - The Y-axis label if found; otherwise, the default Y-axis label from kpiDetail.
   */
  checkYAxis(kpi) {
    const kpiDataResponce = this.allKpiArray?.find(de => de.kpiId === kpi.kpiId);
    const selectedFilterVal = this.kpiSelectedFilterObj[kpi?.kpiId];
    if (kpiDataResponce && kpiDataResponce?.trendValueList) {
      const trendData = kpiDataResponce.trendValueList?.find(data => {
        const kpiFIlter = (data.filter || data.filter1);
        const selectedFilter = selectedFilterVal.filter1 ? selectedFilterVal.filter1[0] : selectedFilterVal[0];
        return kpiFIlter === selectedFilter;
      })
      if (trendData && Object.keys(trendData).length > 1 && trendData?.yaxisLabel) {
        return trendData.yaxisLabel
      }
    }
    return kpi?.kpiDetail?.yaxisLabel;
  }

  /**
     * Determines the CSS class for column width based on the provided KPI width percentage.
     * Accepts specific width values and defaults to 50% if an unrecognized value is given.
     * @param kpiwidth - The width percentage (100, 50, 66, 33) to determine the column class.
     * @returns A string representing the corresponding CSS class for the column width.
     * No exceptions are thrown.
     */
  getkpiwidth(kpiwidth) {
    let retValue = '';

    switch (kpiwidth) {
      case 100:
        retValue = 'p-col-12';
        break;
      case 50:
        retValue = 'p-col-6';
        break;
      case 66:
        retValue = 'p-col-8';
        break;
      case 33:
        retValue = 'p-col-4';
        break;
      default:
        retValue = 'p-col-6';
        break;
    }

    return retValue;
  }

  checkKPIPresence(kpi) {
    if (this.tabsArr.size > 1) {
      return this.selectedKPITab === kpi.kpiDetail.kpiSubCategory && kpi['isEnabled'];
    } else {
      return kpi['isEnabled'];
    }
  }

}
