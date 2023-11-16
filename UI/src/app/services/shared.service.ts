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
  iterationCongifData=new BehaviorSubject({});
  kpiListNewOrder= new BehaviorSubject([]);
  private subject = new Subject<any>();
  private accountType;
  private selectedProject;
  private selectedToolConfig;
  private selectedFieldMapping;
  public passErrorToErrorPage;
  public engineeringMaturityExcelData;
  public suggestionsData: any = [];
  private passServerRole= new BehaviorSubject<boolean>(false);
  public boardId = 1;
  public isDownloadExcel;

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
  noRelease = new BehaviorSubject<any>(false);
  noReleaseObs = this.noRelease.asObservable();
  fieldMappingOptionsMetaData : any = []
  kpiCardView : string = "chart";
  maturityTableLoader = new Subject<boolean>();
  globalConfigData : any
  visibleSideBarSubject = new BehaviorSubject(false);
  visibleSideBarObs = this.visibleSideBarSubject.asObservable();


  private currentIssue = new BehaviorSubject({});
  currentData = this.currentIssue.asObservable();

  constructor() {
    this.passDataToDashboard = new EventEmitter();
    this.globalDashConfigData = new EventEmitter();
    this.passErrorToErrorPage = new EventEmitter();
    this.passAllProjectsData = new EventEmitter();
    this.passEventToNav = new EventEmitter();
    this.isDownloadExcel = new EventEmitter();
    this.isSideNav = new EventEmitter();
  }

  // for DSV
  setIssueData(data) {
    this.currentIssue.next(data);
  }


  ngOnInit() {
  }

  setCurrentSelectedSprint(selectedSprint){
    this.currentSelectedSprint = selectedSprint;
    this.currentSelectedSprintSub.next(selectedSprint);
  }

  setSelectedTypeOrTabRefresh(selectedTab, selectedType) {
    this.selectedtype = selectedType;
    this.selectedTab = selectedTab;
    this.onTypeOrTabRefresh.next({ selectedTab, selectedType });
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
  getSelectedType() {
    return this.selectedtype;
  }

  // setter dash config data
  setDashConfigData(data) {
    this.dashConfigData = JSON.parse(JSON.stringify(data));
      this.globalDashConfigData.emit(data);
  }

  // getter kpi config data
  getDashConfigData() {
    return this.dashConfigData;
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
    this.subject.next();
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

  setEmptyData(flag){
    this.setNoData.next(flag);
  }
  getEmptyData(){
    return this.setNoData.asObservable();
  }


  raiseError(error) {
    this.passErrorToErrorPage.emit(error);
  }

  // calls when user select different Tab (executive , quality etc)
  select(masterData, filterData, filterApplyData, selectedTab, isAdditionalFilters?, makeAPICall = true) {
    this.sharedObject = {};
    this.sharedObject.masterData = masterData;
    this.sharedObject.filterData = filterData;
    this.sharedObject.filterApplyData = filterApplyData;
    this.sharedObject.selectedTab = selectedTab;
    this.sharedObject.isAdditionalFilters = isAdditionalFilters;
    this.sharedObject.makeAPICall = makeAPICall;
    //emit once navigation complete
    setTimeout(()=>{
      this.passDataToDashboard.emit(this.sharedObject);
    },0);
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

  setColorObj(value){
    this.mapColorToProject.next(value);
  }

  setKpiSubFilterObj(value){
    this.selectedFilterOption.next(value);
  }

  setNoSprints(value){
    this.noSprints.next(value);
  }
  setNoProjects(value){
    this.noProjects.next(value);
  }
  setClickedItem(event){
    this.clickedItem.next(event);
  }
  getClickedItem(){
    return this.clickedItem.asObservable();
  }
  setSelectedDateFilter(xLabel){
    this.xLabelValue = xLabel;
  }
  getSelectedDateFilter(){
    return this.xLabelValue;
  }
  setShowTableView(val){
    this.kpiCardView = val;
    this.showTableView.next(val);
  }
  getKPICardView(){
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
   setGlobalDownload(val){
    this.isDownloadExcel.emit(val);
  }
  setSelectedLevel(val){
    this.selectedLevel = {...val};
  }
  getSelectedLevel(){
    return this.selectedLevel;
  }
  setSelectedTrends(values){
    this.selectedTrends = values;
  }
  getSelectedTrends(){
    return this.selectedTrends;
  }


  // calls when sidenav refresh
  setSideNav(flag) {
    this.isSideNav.emit(flag);
  }

  setCurrentUserDetails(details){

    if(!this.currentUserDetails  || !details || Object.keys(details).length === 0){
      this.currentUserDetails=details;
    }else{
      this.currentUserDetails={...this.currentUserDetails,...details};
    }
    this.currentUserDetailsSubject.next(this.currentUserDetails);
  }

  getCurrentUserDetails(key){
    if(this.currentUserDetails && this.currentUserDetails.hasOwnProperty(key)){
      return this.currentUserDetails[key] ;
     }
    return false;
  }

  setNoRelease(value){
    this.noRelease.next(value)
  }

  setFieldMappingMetaData(metaDataObj){
    this.fieldMappingOptionsMetaData = [...this.fieldMappingOptionsMetaData,metaDataObj];
  }

  getFieldMappingMetaData(){
    return this.fieldMappingOptionsMetaData;
  }

  setMaturiyTableLoader(value){
    this.maturityTableLoader.next(value)
  }

  setGlobalConfigData(data){
    this.globalConfigData = data;
  }

  getGlobalConfigData(){
    return this.globalConfigData;
  }
}


