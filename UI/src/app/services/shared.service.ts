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

import { EventEmitter, Injectable } from '@angular/core';
import { Observable, Subject, BehaviorSubject } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';
/*************
SharedService
This Service is used for sharing data and also let filter component know that
user click on tab or type(scrum , kanban).

@author anuj
**************/



@Injectable()
export class SharedService {
  public passDataToDashboard;
  public passAllProjectsData;
  public passEventToNav;
  public allProjectsData;
  public sharedObject;
  public globalDashConfigData;
  public selectedTab;
  public selectedtype;
  public title = <any>{};
  public logoImage;
  public dashConfigData;
  iterationConfigData = new BehaviorSubject({});
  kpiListNewOrder = new BehaviorSubject([]);
  private subject = new Subject<any>();
  private accountType;
  private selectedProject;
  private selectedToolConfig;
  private selectedFieldMapping;
  public passErrorToErrorPage;
  public engineeringMaturityExcelData;
  public suggestionsData: any = [];
  private passServerRole = new BehaviorSubject<boolean>(false);
  public boardId = 1;
  private authToken = '';
  public sprintForRnR;
  public dateFilterSelectedDateType = new BehaviorSubject<String>('Weeks');
  primaryFilterChangeSubject = new BehaviorSubject(false);
  public kpiExcelSubject = new BehaviorSubject<{}>({});

  // make filterdata and masterdata persistent across dashboards
  private filterData = {};
  private masterdata = {};
  private chartColorList = {};
  changedMainDashboardValueSub = new Subject<any>();
  changedMainDashboardValueObs = this.changedMainDashboardValueSub.asObservable();
  currentSelectedSprintSub = new Subject<any>();
  currentSelectedSprint;
  mapColorToProject = new BehaviorSubject<any>({});
  mapColorToProjectObs = this.mapColorToProject.asObservable();
  selectedFilterOption = new BehaviorSubject<any>({});
  selectedFilterOptionObs = this.selectedFilterOption.asObservable();
  noSprints = new BehaviorSubject<any>(false);
  noSprintsObs = this.noSprints.asObservable();
  noProjects = new BehaviorSubject<boolean>(false);
  noProjectsObj = {};
  noProjectsObjObs = new BehaviorSubject<any>({});
  noProjectsObs = this.noProjects.asObservable();
  showTableView = new BehaviorSubject<string>('chart');
  showTableViewObs = this.showTableView.asObservable();
  setNoData = new Subject<boolean>();
  clickedItem = new Subject<any>();
  public xLabelValue: any;
  selectedLevel = {};
  selectedTrends = [];
  public isSideNav;
  currentUserDetails = null;
  currentUserDetailsSubject = new BehaviorSubject<any>(null);
  currentUserDetailsObs = this.currentUserDetailsSubject.asObservable();
  public onTypeOrTabRefresh = new Subject<{ selectedTab: string, selectedType: string }>();
  public onScrumKanbanSwitch = new Subject<{ selectedType: string }>();
  public onTabSwitch = new Subject<{ selectedBoard: string }>();
  noRelease = new BehaviorSubject<any>(false);
  noReleaseObs = this.noRelease.asObservable();
  fieldMappingOptionsMetaData: any = []
  kpiCardView: string = "chart";
  maturityTableLoader = new Subject<boolean>();
  globalConfigData: any
  visibleSideBarSubject = new BehaviorSubject(false);
  visibleSideBarObs = this.visibleSideBarSubject.asObservable();
  addtionalFilterBackup = {};
  projectQueryParamSubject = new BehaviorSubject<any>('');
  projectQueryParamObs = this.projectQueryParamSubject.asObservable();
  sprintQueryParamSubject = new BehaviorSubject<any>('');
  sprintQueryParamObs = this.sprintQueryParamSubject.asObservable();
  processorTraceLogs = [];
  selectedTrendsEvent;
  selectedTrendsEventSubject;
  projectList = [];

  public currentIssue = new BehaviorSubject({});
  public currentData = this.currentIssue.asObservable();

  // For additional filters
  public populateAdditionalFilters;
  public triggerAdditionalFilters;

  boardNamesListSubject = new BehaviorSubject<any>([]);
  boardNamesListObs = this.boardNamesListSubject.asObservable();

  isRecommendationsEnabledSubject = new BehaviorSubject<boolean>(false);
  isRecommendationsEnabledObs = this.isRecommendationsEnabledSubject.asObservable();

  selectedMap = {};

  // KPI filter retention
  selectedKPIFilterObj = {};

  // URL Sharing
  selectedFilterArray: any = [];
  selectedFilters: any = {};
  selectedUrlFilters: string = '{}';
  refreshCounter: number = 0;

  constructor(private router: Router, private route: ActivatedRoute) {
    this.passDataToDashboard = new EventEmitter();
    this.globalDashConfigData = new EventEmitter();
    this.passErrorToErrorPage = new EventEmitter();
    this.passAllProjectsData = new EventEmitter();
    this.passEventToNav = new EventEmitter();
    this.isSideNav = new EventEmitter();
    // For additional filters
    this.populateAdditionalFilters = new EventEmitter();
    this.triggerAdditionalFilters = new EventEmitter();
    // this.selectedTrendsEvent = new EventEmitter();

    this.selectedTrendsEventSubject = new Subject<any>();
    // Observable to subscribe to
    this.selectedTrendsEvent = this.selectedTrendsEventSubject.asObservable();
  }

  // for DSV
  setIssueData(data) {
    this.currentIssue.next(data);
  }

  setCurrentSelectedSprint(selectedSprint) {
    this.currentSelectedSprint = selectedSprint;
    this.currentSelectedSprintSub.next(selectedSprint);
  }

  // only Old UI code
  setSelectedTypeOrTabRefresh(selectedTab, selectedType) {
    this.selectedtype = selectedType;
    this.selectedTab = selectedTab;
    this.onTypeOrTabRefresh.next({ selectedTab, selectedType });
  }
  // end here

  setScrumKanban(selectedType) {
    this.selectedtype = selectedType;
    this.onScrumKanbanSwitch.next({ selectedType });
  }

  setSelectedBoard(selectedBoard) {
    this.selectedTab = selectedBoard;
    this.onTabSwitch.next({ selectedBoard });
  }

  setSelectedTab(selectedTab) {
    this.selectedTab = selectedTab;
  }

  setSelectedType(selectedType) {
    this.selectedtype = selectedType;
  }

  // getter for tab i.e Executive/ Iteration/ Developer
  getSelectedTab() {
    return this.selectedTab;
  }

  // getter for tab i.e Scrum/Kanban
  getSelectedType(): string {
    return this.selectedtype;
  }

  // setter dash config data
  setDashConfigData(data, emit = true, enabledKPIs = null) {
    this.dashConfigData = JSON.parse(JSON.stringify(data));
    if (emit) {
      if (enabledKPIs) {
        data['enabledKPIs'] = enabledKPIs;
        this.globalDashConfigData.emit(data);
      } else {
        this.globalDashConfigData.emit(data);
      }
    }
  }

  // getter kpi config data
  getDashConfigData() {
    return this.dashConfigData;
  }

  // Additional Filters in New UI
  setAdditionalFilters(data) {
    this.populateAdditionalFilters.emit(data);
  }

  // Additional Filters in New UI
  applyAdditionalFilters(filters) {
    this.triggerAdditionalFilters.emit(filters);
  }

  // getter-setter for selectedMap
  getSelectedMap() {
    return this.selectedMap;
  }

  setSelectedMap(data) {
    this.selectedMap = data;
  }

  // login account type
  setAccountType(accountType) {
    this.accountType = accountType;
  }

  getAccountType() {
    return this.accountType;
  }

  setLogoImage(logoImage: File) {
    this.subject.next({ File: logoImage });
  }

  setVisibleSideBar(value) {
    this.visibleSideBarSubject.next(value);
  }

  clearLogoImage() {
    this.subject.next(true);
  }

  getLogoImage(): Observable<any> {
    return this.subject.asObservable();
  }

  setEmptyFilter() {
    this.sharedObject = {};
    this.sharedObject.masterData = [];
    this.sharedObject.filterData = [];
    this.sharedObject.filterApplyData = [];
  }

  setFilterData(data) {
    this.filterData = data;
  }

  getFilterData() {
    return this.filterData;
  }

  setMasterData(data) {
    this.masterdata = data;
  }

  getMasterData() {
    return this.masterdata;
  }

  getFilterObject() {
    return this.sharedObject;
  }

  setEmptyData(flag) {
    this.setNoData.next(flag);
  }
  getEmptyData() {
    return this.setNoData.asObservable();
  }


  raiseError(error) {
    this.passErrorToErrorPage.emit(error);
  }

  // calls when user select different Tab (executive , quality etc)
  select(masterData, filterData, filterApplyData, selectedTab, isAdditionalFilters?, makeAPICall = true, configDetails = null, loading = false, dashConfigData = null, selectedType = null) {
    this.sharedObject = {};
    this.sharedObject.masterData = masterData;
    this.sharedObject.filterData = filterData;
    this.sharedObject.filterApplyData = filterApplyData;
    this.sharedObject.selectedTab = selectedTab;
    this.sharedObject.isAdditionalFilters = isAdditionalFilters;
    this.sharedObject.makeAPICall = makeAPICall;
    this.sharedObject.loading = loading;
    this.sharedObject.dashConfigData = dashConfigData;
    this.sharedObject.selectedType = selectedType;
    if (configDetails) {
      this.sharedObject.configDetails = configDetails;
    }

    //emit once navigation complete
    setTimeout(() => {
      this.passDataToDashboard.emit(this.sharedObject);
    }, 0);
  }

  /** KnowHOW Lite */
  setSelectedProject(project) {
    this.selectedProject = project;
  }

  getSelectedProject() {
    return this.selectedProject;
  }

  setSelectedToolConfig(toolData) {
    this.selectedToolConfig = toolData;
  }

  getSelectedToolConfig() {
    return this.selectedToolConfig;
  }

  setSelectedFieldMapping(fieldMapping) {
    this.selectedFieldMapping = fieldMapping;
  }

  getSelectedFieldMapping() {
    return this.selectedFieldMapping;
  }

  sendProjectData(data) {
    this.allProjectsData = data;
    this.passAllProjectsData.emit(this.allProjectsData);
  }

  notificationUpdate() {
    this.passEventToNav.emit();
  }

  setSuggestions(suggestions) {
    this.suggestionsData = suggestions;
  }

  getSuggestions() {
    return this.suggestionsData;
  }

  setServerRole(value: boolean) {
    this.passServerRole.next(value);
  }

  setColorObj(value) {
    this.mapColorToProject.next(value);
  }

  private tempStateFilters = null;
  setBackupOfFilterSelectionState(selectedFilterObj) {
    if (selectedFilterObj && Object.keys(selectedFilterObj).length === 1 && Object.keys(selectedFilterObj)[0] === 'selected_type') {
      this.selectedFilters = { ...selectedFilterObj };
    } else if (selectedFilterObj) {
      this.selectedFilters = { ...this.selectedFilters, ...selectedFilterObj };
    } else {
      this.selectedFilters = null;
    }

    if (this.refreshCounter === 0) {
      this.refreshCounter++;
    }

    // Navigate and update query parameters
    const stateFilterEnc = btoa(JSON.stringify(this.selectedFilters || {}));
    this.setBackupOfUrlFilters(JSON.stringify(this.selectedFilters || {}));

    // NOTE: Do not navigate if the state filters are same as previous, this is to reduce the number of navigation calls, hence refactoring the code
    if ((this.tempStateFilters !== stateFilterEnc) && (!this.router.url.split('/').includes('Config') && !this.router.url.split('/').includes('Error') && !this.router.url.split('/').includes('Help'))) {
      this.router.navigate([], {
        queryParams: { 'stateFilters': stateFilterEnc },
        relativeTo: this.route
      });
      this.tempStateFilters = stateFilterEnc;
    }
  }

  getBackupOfFilterSelectionState(prop = null) {
    if (this.selectedFilters) {
      if (prop) {
        return this.selectedFilters[prop];
      } else {
        return this.selectedFilters;
      }
    } else {
      return null;
    }
  }

  setBackupOfUrlFilters(data) {
    this.selectedUrlFilters = data;
  }

  getBackupOfUrlFilters() {
    return this.selectedUrlFilters;
  }


  removeQueryParams() {
    this.router.navigate([], {
      queryParams: {}, // Clear query params
    });
  }

  setKpiSubFilterObj(value: any) {
    if (!value) {
      this.selectedKPIFilterObj = {};
    } else if (Object.keys(value)?.length && Object.keys(value)[0].indexOf('kpi') !== -1) {
      Object.keys(value).forEach((key) => {
        this.selectedKPIFilterObj[key] = value[key];
      });
    }
    const kpiFilterParamStr = btoa(Object.keys(this.selectedKPIFilterObj).length ? JSON.stringify(this.selectedKPIFilterObj) : '');

    console.log('this.router.url ', this.router.url)
    if (!this.router.url.split('/').includes('Config') && !this.router.url.split('/').includes('Error') && !this.router.url.split('/').includes('Help')) {
      this.router.navigate([], {
        queryParams: { 'stateFilters': this.tempStateFilters, 'kpiFilters': kpiFilterParamStr }, // Pass the object here
        relativeTo: this.route,
        queryParamsHandling: 'merge'
      });
    }
    this.selectedFilterOption.next(value);
  }

  getKpiSubFilterObj() {
    return this.selectedKPIFilterObj;
  }

  setNoSprints(value) {
    this.noSprints.next(value);
  }
  setNoProjects(value) {
    this.noProjects.next(value);
  }

  setNoProjectsForNewUI(valueObj) {
    this.noProjectsObj = valueObj;
    this.noProjectsObjObs.next(this.noProjectsObj);
  }

  setClickedItem(event) {
    this.clickedItem.next(event);
  }
  getClickedItem() {
    return this.clickedItem.asObservable();
  }
  setSelectedDateFilter(xLabel) {
    this.xLabelValue = xLabel;
  }
  getSelectedDateFilter() {
    return this.xLabelValue;
  }
  setShowTableView(val) {
    this.kpiCardView = val;
    this.showTableView.next(val);
  }
  getKPICardView() {
    return this.kpiCardView;
  }

  clearAllCookies() {
    console.log('clear all cookie Called');
    const cookies = document.cookie.split(';');
    // set past expiry to all cookies
    for (const cookie of cookies) {
      document.cookie = cookie + '=; expires=' + new Date(0).toUTCString();
    }
  }

  setSelectedLevel(val) {
    this.selectedLevel = { ...val };
  }
  getSelectedLevel() {
    return this.selectedLevel;
  }
  setSelectedTrends(values) {
    values.forEach(trend => {
      if (trend?.path) {
        trend.path = trend.path?.replace(/___/g, '###');
      }
    });
    this.selectedTrends = values;
    // this.selectedTrendsEvent.emit(values);
    this.selectedTrendsEventSubject.next(values);
  }
  getSelectedTrends() {
    return this.selectedTrends;
  }


  // calls when sidenav refresh
  setSideNav(flag) {
    this.isSideNav.emit(flag);
  }

  getCurrentUserDetails(key = null) {
    this.currentUserDetails = JSON.parse(localStorage.getItem('currentUserDetails'));
    if (key) {
      if (this.currentUserDetails && this.currentUserDetails.hasOwnProperty(key)) {
        return this.currentUserDetails[key];
      }
    } else if (this.currentUserDetails) {
      return this.currentUserDetails;
    }
    return false;
  }

  setNoRelease(value) {
    this.noRelease.next(value)
  }

  setFieldMappingMetaData(metaDataObj) {
    this.fieldMappingOptionsMetaData = [...this.fieldMappingOptionsMetaData, metaDataObj];
  }

  getFieldMappingMetaData() {
    return this.fieldMappingOptionsMetaData;
  }

  setMaturiyTableLoader(value) {
    this.maturityTableLoader.next(value)
  }

  setGlobalConfigData(data) {
    this.globalConfigData = data;
  }

  getGlobalConfigData() {
    return this.globalConfigData;
  }

  setAuthToken(value) {
    this.authToken = value;
  }

  setProjectList(projects) {
    this.projectList = projects;
  }

  getProjectList() {
    return this.projectList;
  }

  getAuthToken() {
    return this.authToken;
  }

  setProjectQueryParamInFilters(value) {
    this.projectQueryParamSubject.next({ value });
  }

  setSprintQueryParamInFilters(value) {
    this.sprintQueryParamSubject.next({ value });
  }

  setAddtionalFilterBackup(data) {
    this.addtionalFilterBackup = data;
  }

  getAddtionalFilterBackup() {
    return this.addtionalFilterBackup;
  }
  setProcessorLogDetails(data) {
    this.processorTraceLogs = data;
  }

  getProcessorLogDetails() {
    return this.processorTraceLogs
  }

  setUpdatedBoardList(kpiListData, selectedType) {
    const boardNameArr = [];
    if (
      kpiListData[selectedType] &&
      Array.isArray(kpiListData[selectedType])
    ) {
      for (let i = 0; i < kpiListData[selectedType]?.length; i++) {
        let kpiShownCount = 0;
        let board = kpiListData[selectedType][i];
        let kpiList;
        if (board?.boardName?.toLowerCase() === 'iteration') {
          kpiList = board?.['kpis']?.filter((item) => item.kpiId != 'kpi121');
        } else {
          kpiList = board?.['kpis'];
        }
        kpiList?.forEach((item) => {
          if (item.shown) {
            kpiShownCount++;
          }
        });
        if (kpiShownCount > 0) {
          boardNameArr.push({
            boardName: board?.boardName,
            link: board?.boardSlug
          });
        }
      }

    }

    for (let i = 0; i < kpiListData['others']?.length; i++) {
      let kpiShownCount = 0;
      kpiListData['others'][i]['kpis']?.forEach((item) => {
        if (item.shown) {
          kpiShownCount++;
        }
      });
      if (kpiShownCount > 0) {
        boardNameArr.push({
          boardName: kpiListData['others'][i].boardName,
          link:
            kpiListData['others'][i].boardSlug
        });
      }
    }
    this.boardNamesListSubject.next(boardNameArr);
  }

  getSprintForRnR() {
    return this.sprintForRnR;
  }

  setSprintForRnR(sprint) {
    this.sprintForRnR = sprint;
  }

  setRecommendationsFlag(value: boolean) {
    this.isRecommendationsEnabledSubject.next(value);
  }

  //#region  can be remove after iteraction component removal

  isTrendValueListValid(trendValueList: any[]): boolean {
    return trendValueList?.length > 0 && trendValueList[0]?.hasOwnProperty('filter1');
  }

  populateDropdownFromTrendValues(trendValueList: any[], dropdownArr: any[]): void {
    trendValueList.forEach(item => {
      if (!dropdownArr.includes(item?.filter1)) {
        dropdownArr.push(item?.filter1);
      }
    });
  }


  shouldRemoveOverallFilter(kpiObj: any): boolean {
    return (
      kpiObj &&
      kpiObj['kpiDetail']?.hasOwnProperty('kpiFilter') &&
      (
        kpiObj['kpiDetail']['kpiFilter']?.toLowerCase() === 'multiselectdropdown' ||
        (kpiObj['kpiDetail']['kpiFilter']?.toLowerCase() === 'dropdown' &&
          kpiObj['kpiDetail'].hasOwnProperty('hideOverallFilter') &&
          kpiObj['kpiDetail']['hideOverallFilter'])
      )
    );
  }

  removeOverallFilter(dropdownArr: any[]): void {
    const index = dropdownArr.findIndex(x => x?.toLowerCase() === 'overall');
    if (index > -1) {
      dropdownArr.splice(index, 1);
    }
  }

  createFilterObject(dropdownArr: any[]): any[] {
    return [
      {
        filterType: 'Select a filter',
        options: dropdownArr
      }
    ];
  }

  setUserDetailsAsBlankObj(){
    this.currentUserDetails = {}
  }

  //#endregion
}


