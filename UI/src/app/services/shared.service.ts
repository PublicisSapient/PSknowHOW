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

import { OnInit, EventEmitter, Injectable, Output } from '@angular/core';
import { Observable, Subject, BehaviorSubject } from 'rxjs';

/*************
SharedService
This Service is used for sharing data and also let filter component know that
user click on tab or type(scrum , kanban).

@author anuj
**************/



@Injectable()
export class SharedService implements OnInit {
  public passDataToDashboard;
  public passAllProjectsData;
  public passEventToNav;
  public allProjectsData;
  public sharedObject;
  public onTabRefresh;
  public onTypeRefresh;
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
  public activateKanban;
  public selectedTypeObs = new BehaviorSubject('scrum');
  public boardId = 1;
  public isDownloadExcel;

  // make filterdata and masterdata persistent across dashboards
  private filterData = {};
  private masterdata = {};
  private chartColorList = {};
  changedMainDashboardValueSub = new Subject<any>();
  changedMainDashboardValueObs = this.changedMainDashboardValueSub.asObservable();
  currentSelectedSprintSub = new Subject<any>();
  currentSelectedSprintObs = this.currentSelectedSprintSub.asObservable();
  mapColorToProject = new BehaviorSubject<any>({});
  mapColorToProjectObs = this.mapColorToProject.asObservable();
  selectedFilterOption = new BehaviorSubject<any>({});
  selectedFilterOptionObs = this.selectedFilterOption.asObservable();
  noSprints = new Subject<any>();
  noSprintsObs = this.noSprints.asObservable();
  noProjects = new Subject<any>();
  noProjectsObs = this.noProjects.asObservable();
  showTableView = new BehaviorSubject<boolean>(true);
  showTableViewObs = this.showTableView.asObservable();
  setNoData = new Subject<boolean>();
  clickedItem = new Subject<any>();
  public xLabelValue: any;
  selectedLevel:object={};
  selectedTrends:Array<object> = [];
  constructor() {
    this.passDataToDashboard = new EventEmitter();
    this.onTabRefresh = new EventEmitter();
    this.onTypeRefresh = new EventEmitter();
    this.globalDashConfigData = new EventEmitter();
    this.passErrorToErrorPage = new EventEmitter();
    this.passAllProjectsData = new EventEmitter();
    this.passEventToNav = new EventEmitter();
    this.activateKanban = new EventEmitter();
    this.isDownloadExcel = new EventEmitter();
  }


  ngOnInit() {
  }

  // calls when tab is selected
  selectTab(selectedTab) {
    this.onTabRefresh.emit(selectedTab);
  }
  // setter for tab i.e executive etc
  setSelectedTab(selectedTab, boardId) {
    this.selectedTab = selectedTab;
    this.boardId = boardId;
  }

  getSelectBoardId() {
    return this.boardId;
  }
  // getter for type i.e scrum or kanban
  getSelectedTab() {
    return this.selectedTab;
  }
  // setter for tab i.e Scrum/Kanban
  setSelectedType(selectedtype) {
    this.selectedtype = selectedtype;
    this.onTypeRefresh.emit(selectedtype);
    this.activateKanban.emit(selectedtype === 'Kanban' ? true : false);
    this.selectedTypeObs.next(selectedtype.toLowerCase());
  }

  // getter for tab i.e Scrum/Kanban
  getSelectedType() {
    return this.selectedtype;
  }

  // setter dash config data
  setDashConfigData(data) {
    this.globalDashConfigData.emit(data);
    this.dashConfigData = JSON.parse(JSON.stringify(data));
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
    this.passDataToDashboard.emit(this.sharedObject);
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
    this.showTableView.next(val);
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
    this.selectedTrends = [...values];
  }
  getSelectedTrends(){
    return this.selectedTrends;
  }
}


