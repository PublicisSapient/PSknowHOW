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
import { Component, OnInit, OnDestroy } from '@angular/core';
import { HttpService } from '../../services/http.service';
import { ExcelService } from '../../services/excel.service';
import { SharedService } from '../../services/shared.service';
import { HelperService } from '../../services/helper.service';
import { faList, faChartPie } from '@fortawesome/free-solid-svg-icons';
import { ActivatedRoute } from '@angular/router';
import { mergeMap } from 'rxjs/operators';
import * as Excel from 'exceljs';
import * as fs from 'file-saver';
declare let require: any;


@Component({
    selector: 'app-executive',
    templateUrl: './executive.component.html',
    styleUrls: ['./executive.component.css']
})

export class ExecutiveComponent implements OnInit, OnDestroy {
    masterData = <any>{};
    filterData = <any>[];
    sonarKpiData = <any>{};
    jenkinsKpiData = <any>{};
    zypherKpiData = <any>{};
    jiraKpiData = <any>{};
    bitBucketKpiData = <any>{};
    filterRequestData = {};
    filterkeys = <any>[];
    filterApplyData = <any>{};
    kpiListSonar = <any>{};
    kpiJenkins = <any>{};
    kpiZypher = <any>{};
    kpiJira = <any>{};
    kpiBitBucket = <any>{};
    gaugemap = {};
    loaderJenkins = false;
    faList = faList;
    faChartPie = faChartPie;
    // loaderJira = false;

    loaderJiraArray = [];
    loaderJiraKanbanArray = [];

    loaderSonar = false;
    loaderZypher = false;
    loaderBitBucket = false;
    subscriptions: any[] = [];
    noOfFilterSelected = 0;
    downloadJson = <any>{};
    jiraKpiRequest = <any>'';
    sonarKpiRequest = <any>'';
    zypherKpiRequest = <any>'';
    jenkinsKpiRequest = <any>'';
    bitBucketKpiRequest = <any>'';
    maturityColorCycleTime = <any>['#f5f5f5', '#f5f5f5', '#f5f5f5'];
    totalDefect = <any>[];
    tooltip = <any>{};
    selectedtype = '';
    configGlobalData;
    selectedPriorityFilter = <any>{};
    prioritySum = <any>{};
    selectedSonarFilter;
    selectedTestExecutionFilterData;
    sonarFilterData = <any>[];
    testExecutionFilterData = <any>[];
    selectedJobFilter = 'Select';
    selectedBranchFilter = 'Select';
    processedKPI11Value = {};
    kanbanActivated = false;
    serviceObject = {};
    isChartView = true;
    processedARTValue: any = {};
    processedARTAggValue: any = {};
    selectedARTFilter = 'All issue types';

    processedRCAValue: any = {};
    processedRCAAggValue: any = {};
    selectedRCAFilter = 'All Causes';
    selectedMTTMFilter = 'Select';
    processedKPI84Value = {};
    allKpiArray: any = [];
    colorObj: object = {};
    chartColorList: object = {};
    kpiSelectedFilterObj = {};
    kpiChartData = {};
    noKpis = false;
    enableByUser = false;
    updatedConfigGlobalData;
    kpiConfigData: object = {};
    kpiLoader = true;
    noTabAccess = false;
    trendBoxColorObj: any;
    iSAdditionalFilterSelected = false;
    kpiDropdowns: object = {};
    showKpiTrendIndicator = {};
    boardId = 1;
    previousBoardId: number;
    hierarchyLevel;
    showChart = true;
    displayModal = false;
    modalDetails = {
        header: '',
        tableHeadings: [],
        tableValues: []
    };
    kpiExcelData;
    isGlobalDownload: boolean = false;
    kpiTrendsObj: object = {};
    constructor(private service: SharedService, private httpService: HttpService, private excelService: ExcelService, private helperService: HelperService, private route: ActivatedRoute) {
        this.kanbanActivated = this.service.getSelectedType() === 'Kanban' ? true : false;
        if (this.boardId) {
            this.previousBoardId = this.boardId;
        }


        this.route.params.subscribe(params => {
            this.boardId = isNaN(+params['boardId']) ? 1 : +params['boardId'];
        });

        this.subscriptions.push(this.service.activateKanban.subscribe((value) => {
            if (value) {
                this.kanbanActivated = true;
            } else {
                this.kanbanActivated = false;
            }
        }));

        // used to know whether scrum or kanban is clicked
        this.service.onTypeRefresh.subscribe((sharedobject) => {
            // this.loaderJira = false;
            this.loaderSonar = false;
            this.loaderZypher = false;
            this.loaderBitBucket = false;
            this.loaderJenkins = false;
            this.processedKPI11Value = {};
            this.selectedBranchFilter = 'Select';
            this.serviceObject = {};
            this.getSelectedType(sharedobject);
            this.kanbanActivated = sharedobject === 'Kanban' ? true : false;
            if (this.service.getDashConfigData() && Object.keys(this.service.getDashConfigData()).length > 0) {
                this.configGlobalData = this.service.getDashConfigData()[this.kanbanActivated ? 'kanban' : 'scrum'].filter((item) => item.boardId === this.boardId)[0]?.kpis;
                this.processKpiConfigData();  
            }
        });

        if (this.service.getDashConfigData() && Object.keys(this.service.getDashConfigData()).length > 0) {
            this.configGlobalData = this.service.getDashConfigData()[this.kanbanActivated ? 'kanban' : 'scrum'].filter((item) => item.boardId === this.boardId)[0]?.kpis;
            this.processKpiConfigData();
        }

        this.subscriptions.push(this.service.globalDashConfigData.subscribe((globalConfig) => {
            this.checkIfBoardIdBelongsToSelectedType(globalConfig);
            this.configGlobalData = globalConfig[this.kanbanActivated ? 'kanban' : 'scrum'].filter((item) => item.boardId === this.boardId)[0]?.kpis;
            this.processKpiConfigData();
        }));

        this.subscriptions.push(this.service.mapColorToProject.pipe(mergeMap(x => {
            if (Object.keys(x).length > 0) {
                this.colorObj = x;
                if (this.kpiChartData && Object.keys(this.kpiChartData)?.length > 0) {
                    for (const key in this.kpiChartData) {
                        this.kpiChartData[key] = this.generateColorObj(key, this.kpiChartData[key]);
                        this.createTrendsData(key);
                    }
                }
                this.trendBoxColorObj = { ...x };
                for (const key in this.trendBoxColorObj) {
                    const idx = key.lastIndexOf('_');
                    const nodeName = key.slice(0, idx);
                    this.trendBoxColorObj[nodeName] = this.trendBoxColorObj[key];
                }
            }
            return this.service.passDataToDashboard;
        })).subscribe((sharedobject: any) => {
            // used to get all filter data when user click on apply button in filter
            if (sharedobject?.filterData?.length) {
                if (!this.helperService.compareFilters(this.serviceObject['filterApplyData'], sharedobject.filterApplyData, this.kanbanActivated) || this.boardId !== this.service.getSelectBoardId()) {
                    this.serviceObject = JSON.parse(JSON.stringify(sharedobject));


                    this.iSAdditionalFilterSelected = sharedobject?.isAdditionalFilters;
                    this.receiveSharedData(sharedobject);
                }
                this.noTabAccess = false;
            } else {
                this.noTabAccess = true;
            }
        }));

        /**observable to get the type of view */
        this.subscriptions.push(this.service.showTableViewObs.subscribe(view => {
            this.showChart = view;
        }));
        
        this.subscriptions.push(this.service.isDownloadExcel.subscribe(isDownload => {
            this.isGlobalDownload = isDownload;
            if(this.isGlobalDownload){
                this.downloadGlobalExcel();
            }
        }))
    }

    checkIfBoardIdBelongsToSelectedType(globalConfig) {
        if (!globalConfig[this.kanbanActivated ? 'kanban' : 'scrum'].find(boardDetails => boardDetails.boardId === this.boardId)) {
            this.boardId = globalConfig[this.kanbanActivated ? 'kanban' : 'scrum'][0]?.boardId;
        }
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
                if(!this.kpiTrendsObj.hasOwnProperty(element.kpiId)){
                    this.createTrendsData(element.kpiId);
                }
            } else {
                this.kpiConfigData[element.kpiId] = false;
            }
        });
    }


    getSelectedType(sharedobject) {
        this.selectedtype = sharedobject;
    }
    ngOnInit() {
        this.selectedtype = this.service.getSelectedType();
        if (this.service.getFilterObject()) {
            this.serviceObject = JSON.parse(JSON.stringify(this.service.getFilterObject()));
            this.receiveSharedData(this.service.getFilterObject());
        }

        // this.selectedtype='Scrum';

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
    }


    // unsubscribing all Kpi Request
    ngOnDestroy() {
        this.subscriptions.forEach(subscription => subscription.unsubscribe());
    }

    setBoardIdForSelectedTab() {
        if (!this.service.getDashConfigData()[this.selectedtype.toLowerCase() === 'kanban' ? 'kanban' : 'scrum']?.find(boardDetails => boardDetails.boardName.toLowerCase() === this.service.getSelectedTab()?.toLowerCase())) {
            this.boardId = this.service.getDashConfigData()[this.selectedtype.toLowerCase()][0].boardId;
        } else {
            this.boardId = this.service.getDashConfigData()[this.selectedtype.toLowerCase() === 'kanban' ? 'kanban' : 'scrum']?.find(boardDetails => boardDetails.boardName.toLowerCase() === this.service.getSelectedTab().toLowerCase()).boardId;
        }
    }

    /**
    Used to receive all filter data from filter component when user
    click apply and call kpi
     **/
    receiveSharedData($event) {
        if(localStorage?.getItem('completeHierarchyData')){
            const hierarchyData = JSON.parse(localStorage.getItem('completeHierarchyData'));
            if(Object.keys(hierarchyData).length > 0 && hierarchyData[this.selectedtype.toLowerCase()]){
                this.hierarchyLevel = hierarchyData[this.selectedtype.toLowerCase()];
            }
        }
        if (this.service.getDashConfigData() && Object.keys(this.service.getDashConfigData()).length > 0) {
            this.boardId = this.service.getSelectBoardId();
            this.setBoardIdForSelectedTab();
            this.configGlobalData = this.service.getDashConfigData()[this.kanbanActivated ? 'kanban' : 'scrum'].filter((item) => item.boardId === this.boardId)[0]?.kpis;
            this.updatedConfigGlobalData = this.configGlobalData?.filter(item => item.shown && item.isEnabled);
            if (JSON.stringify(this.filterApplyData) !== JSON.stringify($event.filterApplyData) || (this.previousBoardId !== this.boardId) && this.configGlobalData) {
                if (this.serviceObject['makeAPICall']) {
                    this.allKpiArray = [];
                    this.kpiChartData = {};
                    this.chartColorList = {};
                    this.kpiSelectedFilterObj = {};
                    this.kpiDropdowns = {};
                    this.kpiTrendsObj = {};
                }
                const kpiIdsForCurrentBoard = this.configGlobalData?.map(kpiDetails => kpiDetails.kpiId);
                this.previousBoardId = this.boardId;
                this.masterData = $event.masterData;
                this.filterData = $event.filterData;
                this.filterApplyData = $event.filterApplyData;
                this.noOfFilterSelected = Object.keys(this.filterApplyData).length;
                this.selectedJobFilter = 'Select';
                this.selectedtype = this.service.getSelectedType();
                if (this.filterData?.length && $event.makeAPICall) {
                    this.noTabAccess = false;
                    // call kpi request according to tab selected
                    if (this.masterData && Object.keys(this.masterData).length) {
                        if (this.selectedtype === 'Kanban') {
                            this.configGlobalData = this.service.getDashConfigData()[this.selectedtype.toLowerCase()].filter((item) => item.boardId === this.boardId)[0]?.kpis;
                            this.processKpiConfigData();
                            this.groupJiraKanbanKpi(kpiIdsForCurrentBoard);
                            this.groupSonarKanbanKpi(kpiIdsForCurrentBoard);
                            this.groupJenkinsKanbanKpi(kpiIdsForCurrentBoard);
                            this.groupZypherKanbanKpi(kpiIdsForCurrentBoard);
                            this.groupBitBucketKanbanKpi(kpiIdsForCurrentBoard);
                        } else {
                            this.groupJenkinsKpi(kpiIdsForCurrentBoard);
                            this.groupZypherKpi(kpiIdsForCurrentBoard);
                            this.groupJiraKpi(kpiIdsForCurrentBoard);
                            this.groupBitBucketKpi(kpiIdsForCurrentBoard);
                            this.groupSonarKpi(kpiIdsForCurrentBoard);
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
            }
        }
    }


    // download excel functionality
    downloadExcel(kpiId, kpiName, isKanban,additionalFilterSupport) {
        const sprintIncluded = ['CLOSED'];
        if(!(!additionalFilterSupport && this.iSAdditionalFilterSelected)){
            this.helperService.downloadExcel(kpiId, kpiName, isKanban, this.filterApplyData, this.filterData, sprintIncluded).subscribe(getData => {
                if (getData['excelData'] || !getData?.hasOwnProperty('validationData')) {
                    this.kpiExcelData = this.excelService.generateExcelModalData(getData);
                    this.modalDetails['tableHeadings'] = this.kpiExcelData.headerNames.map(column => column.header);
                    this.modalDetails['tableValues'] = this.kpiExcelData.excelData;
                    this.modalDetails['header'] = kpiName;
                    this.displayModal = true;
                }else{
                    if (getData['kpiId'] === 'kpi83') {
                        let dynamicKeys = [];
                        for (const key in getData['validationData']) {
                            if (dynamicKeys.length === 0) {
                                dynamicKeys = Object.keys(getData['validationData'][key][kpiName][0]);
                            }
                            for (const x in dynamicKeys) {
                                getData['validationData'][key][dynamicKeys[x]] = [];
                            }

                            const arr = getData['validationData'][key][kpiName];
                            // eslint-disable-next-line @typescript-eslint/prefer-for-of
                            for (let i = 0; i < arr.length; i++) {
                                for (const item in arr[i]) {
                                    getData['validationData'][key][item].push(arr[i][item]);
                                }
                            }
                            delete getData['validationData'][key][kpiName];

                        }
                    }

                    this.excelService.exportExcel(getData, 'individual', kpiName, isKanban);
                }
            });
        }else{
            this.modalDetails['header'] = kpiName;
            this.displayModal = true;
        }

    }

    exportExcel(kpiName){
    this.excelService.generateExcel(this.kpiExcelData,kpiName);
    }

    checkIfArray(arr){
        return Array.isArray(arr);
    }

    clearModalDataOnClose(){
        this.displayModal=false;
        this.modalDetails = {
            header: '',
            tableHeadings: [],
            tableValues: []
        };
    }
    // Used for grouping all Sonar kpi from master data and calling Sonar kpi.
    groupSonarKpi(kpiIdsForCurrentBoard) {
        this.kpiListSonar = this.helperService.groupKpiFromMaster('Sonar', false, this.masterData, this.filterApplyData, this.filterData, kpiIdsForCurrentBoard, '');
        if (this.kpiListSonar?.kpiList?.length > 0) {
            this.postSonarKpi(this.kpiListSonar, 'sonar');
        }
    }

    // Used for grouping all Jenkins kpi from master data and calling jenkins kpi.
    groupJenkinsKpi(kpiIdsForCurrentBoard) {
        this.kpiJenkins = this.helperService.groupKpiFromMaster('Jenkins', false, this.masterData, this.filterApplyData, this.filterData, kpiIdsForCurrentBoard, '');
        if (this.kpiJenkins?.kpiList?.length > 0) {
            this.postJenkinsKpi(this.kpiJenkins, 'jenkins');
        }
    }

    // Used for grouping all Sonar kpi from master data and calling Sonar kpi.
    groupZypherKpi(kpiIdsForCurrentBoard) {
        // creating a set of unique group Ids
        const groupIdSet = new Set();
        this.masterData.kpiList.forEach((obj) => {
            if (!obj.kanban && obj.kpiSource === 'Zypher' && kpiIdsForCurrentBoard?.includes(obj.kpiId)) {
                groupIdSet.add(obj.groupId);
            }
        });
        // sending requests after grouping the the KPIs according to group Id
        groupIdSet.forEach((groupId) => {
            if (groupId) {
                this.kpiZypher = this.helperService.groupKpiFromMaster('Zypher', false, this.masterData, this.filterApplyData, this.filterData, kpiIdsForCurrentBoard, groupId);
                if (this.kpiZypher?.kpiList?.length > 0) {
                    this.postZypherKpi(this.kpiZypher, 'zypher');
                }
            }
        });

        // this.kpiZypher = this.helperService.groupKpiFromMaster('Zypher', false, this.masterData, this.filterApplyData, this.filterData, '');
        // this.postZypherKpi(this.kpiZypher, 'zypher');

    }

    // Used for grouping all Sonar kpi from master data and calling Sonar kpi.(only for scrum).
    groupJiraKpi(kpiIdsForCurrentBoard) {
        this.jiraKpiData = {};
        // creating a set of unique group Ids
        const groupIdSet = new Set();
        this.masterData.kpiList.forEach((obj) => {
            if (!obj.kanban && obj.kpiSource === 'Jira' && kpiIdsForCurrentBoard?.includes(obj.kpiId)) {
                groupIdSet.add(obj.groupId);
            }
        });

        // sending requests after grouping the the KPIs according to group Id
        groupIdSet.forEach((groupId) => {
            if (groupId) {
                this.kpiJira = this.helperService.groupKpiFromMaster('Jira', false, this.masterData, this.filterApplyData, this.filterData, kpiIdsForCurrentBoard, groupId);
                if (this.kpiJira?.kpiList?.length > 0) {
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
        this.masterData.kpiList.forEach((obj) => {
            if (obj.kanban && obj.kpiSource === 'Jira') {
                groupIdSet.add(obj.groupId);
            }
        });

        // sending requests after grouping the the KPIs according to group Id
        this.loaderJiraKanbanArray = [];
        groupIdSet.forEach((groupId) => {
            if (groupId) {
                this.kpiJira = this.helperService.groupKpiFromMaster('Jira', true, this.masterData, this.filterApplyData, this.filterData, kpiIdsForCurrentBoard, groupId);
                if (this.kpiJira?.kpiList?.length > 0) {
                    this.postJiraKanbanKpi(this.kpiJira, 'jira');
                }
            }
        });
    }
    // Used for grouping all Sonar kpi of kanban from master data and calling Sonar kpi.
    groupSonarKanbanKpi(kpiIdsForCurrentBoard) {
        this.kpiListSonar = this.helperService.groupKpiFromMaster('Sonar', true, this.masterData, this.filterApplyData, this.filterData, kpiIdsForCurrentBoard, '');
        if (this.kpiListSonar?.kpiList?.length > 0) {
            this.postSonarKanbanKpi(this.kpiListSonar, 'sonar');
        }
    }

    // Used for grouping all Jenkins kpi of kanban from master data and calling jenkins kpi.
    groupJenkinsKanbanKpi(kpiIdsForCurrentBoard) {
        this.kpiJenkins = this.helperService.groupKpiFromMaster('Jenkins', true, this.masterData, this.filterApplyData, this.filterData, kpiIdsForCurrentBoard, '');
        if (this.kpiJenkins?.kpiList?.length > 0) {
            this.postJenkinsKanbanKpi(this.kpiJenkins, 'jenkins');
        }
    }

    // Used for grouping all Zypher kpi of kanban from master data and calling Zypher kpi.
    groupZypherKanbanKpi(kpiIdsForCurrentBoard) {
        this.kpiZypher = this.helperService.groupKpiFromMaster('Zypher', true, this.masterData, this.filterApplyData, this.filterData, kpiIdsForCurrentBoard, '');
        if (this.kpiZypher?.kpiList?.length > 0) {
            this.postZypherKanbanKpi(this.kpiZypher, 'zypher');
        }
    }

    // Used for grouping all BitBucket kpi of kanban from master data and calling BitBucket kpi.
    groupBitBucketKanbanKpi(kpiIdsForCurrentBoard) {
        this.kpiBitBucket = this.helperService.groupKpiFromMaster('BitBucket', true, this.masterData, this.filterApplyData, this.filterData, kpiIdsForCurrentBoard, '');
        if (this.kpiBitBucket?.kpiList?.length > 0) {
            this.postBitBucketKanbanKpi(this.kpiBitBucket, 'bitbucket');
        }
    }

    // Used for grouping all BitBucket kpi of scrum from master data and calling BitBucket kpi.
    groupBitBucketKpi(kpiIdsForCurrentBoard) {
        this.kpiBitBucket = this.helperService.groupKpiFromMaster('BitBucket', false, this.masterData, this.filterApplyData, this.filterData, kpiIdsForCurrentBoard, '');
        if (this.kpiBitBucket?.kpiList?.length > 0) {
            this.postBitBucketKpi(this.kpiBitBucket, 'bitbucket');
        }
    }

    // calls after receiving response from sonar
    afterSonarKpiResponseReceived(getData) {
        this.loaderSonar = false;
        this.sonarFilterData.length = 0;
        if (getData !== null && getData[0] !== 'error' && !getData['error']) {
            // creating array into object where key is kpi id
            this.sonarKpiData = this.helperService.createKpiWiseId(getData);
            // creating Sonar filter and finding unique keys from all the sonar kpis
            this.sonarFilterData = this.helperService.createSonarFilter(this.sonarKpiData, this.selectedtype);
            // by default selecting Select from the drop down in sonar filter
            this.selectedSonarFilter = 'Overall';
            this.createAllKpiArray(this.sonarKpiData);

        } else {
            this.sonarKpiData = getData;
        }
        this.kpiLoader = false;
    }

    // calls after receiving response from zypher
    afterZypherKpiResponseReceived(getData) {
        this.testExecutionFilterData.length = 0;
        this.selectedTestExecutionFilterData = {};
        this.loaderZypher = false;
        if (getData !== null && getData[0] !== 'error' && !getData['error']) {
            // creating array into object where key is kpi id
            this.zypherKpiData = this.helperService.createKpiWiseId(getData);
            let calculatedObj;
            if (this.selectedtype !== 'Kanban') {
                calculatedObj = this.helperService.calculateTestExecutionData('kpi70', false, this.zypherKpiData);
            } else {
                calculatedObj = this.helperService.calculateTestExecutionData('kpi71', false, this.zypherKpiData);
            }
            this.selectedTestExecutionFilterData = calculatedObj['selectedTestExecutionFilterData'];
            this.testExecutionFilterData = calculatedObj['testExecutionFilterData'];

            this.createAllKpiArray(this.zypherKpiData);


        } else {
            this.zypherKpiData = getData;
        }
        this.kpiLoader = false;
    }

    // calling post request of sonar of scrum and storing in sonarKpiData id wise
    postSonarKpi(postData, source): void {
        this.loaderSonar = true;
        if (this.sonarKpiRequest && this.sonarKpiRequest !== '') {
            this.sonarKpiRequest.unsubscribe();
        }
        this.sonarKpiRequest = this.httpService.postKpi(postData, source)
            .subscribe(getData => {
                this.afterSonarKpiResponseReceived(getData);
            });
    }
    // calling post request of sonar of Kanban and storing in sonarKpiData id wise
    postSonarKanbanKpi(postData, source): void {
        this.loaderSonar = true;
        if (this.sonarKpiRequest && this.sonarKpiRequest !== '') {
            this.sonarKpiRequest.unsubscribe();
        }
        this.sonarKpiRequest = this.httpService.postKpiKanban(postData, source)
            .subscribe(getData => {
                this.afterSonarKpiResponseReceived(getData);
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
                }
                this.kpiLoader = false;
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
                    // if (getData[0] && getData[0]['trendValueList'] && getData[0]['trendValueList'].length) {
                    //     let modifiedTrendValueList = {};

                    //     getData[0]['trendValueList'].forEach(element => {
                    //         if (element && element.data) {
                    //             if (element.data === 'Overall') {
                    //                 modifiedTrendValueList['Select'] = element.value;
                    //             } else {
                    //                 modifiedTrendValueList[element.data] = element.value;
                    //             }
                    //         }
                    //     });
                    //     getData[0]['trendValueList'] = modifiedTrendValueList;

                    //     // modify the maturityMap
                    //     getData[0]['maturityMap']['Select'] = getData[0]['maturityMap']['Overall'];
                    //     delete getData[0]['maturityMap']['Overall'];
                    //     // modify the value array
                    //     getData[0]['value']['Select'] = getData[0]['value']['Overall'];
                    //     delete getData[0]['value']['Overall'];
                    // }
                    this.jenkinsKpiData = getData;
                    this.createAllKpiArray(this.jenkinsKpiData);
                }
            });
    }

    // calling post request of Zypher(scrum)
    postZypherKpi(postData, source): void {
        this.loaderZypher = true;
        this.zypherKpiRequest = this.httpService.postKpi(postData, source)
            .subscribe(getData => {
                this.afterZypherKpiResponseReceived(getData);
                // this.createAllKpiArray(this.zypherKpiData);
            });
    }
    // calling post request of Zypher(kanban)
    postZypherKanbanKpi(postData, source): void {
        this.loaderZypher = true;
        if (this.zypherKpiRequest && this.zypherKpiRequest !== '') {
            this.zypherKpiRequest.unsubscribe();
        }
        this.zypherKpiRequest = this.httpService.postKpiKanban(postData, source)
            .subscribe(getData => {
                this.afterZypherKpiResponseReceived(getData);
            });
    }

    // post request of Jira(scrum)
    postJiraKpi(postData, source): void {

        postData.kpiList.forEach(element => {
            this.loaderJiraArray.push(element.kpiId);
        });

        this.jiraKpiRequest = this.httpService.postKpi(postData, source)
            .subscribe(getData => {
                // this.loaderJira = false;
                if (getData !== null && getData[0] !== 'error' && !getData['error']) {
                    // creating array into object where key is kpi id
                    const localVariable = this.helperService.createKpiWiseId(getData);
                    for (const kpi in localVariable) {
                        this.loaderJiraArray.splice(this.loaderJiraArray.indexOf(kpi), 1);
                    }
                    if (localVariable && localVariable['kpi3'] && localVariable['kpi3'].maturityValue) {
                        this.colorAccToMaturity(localVariable['kpi3'].maturityValue);
                    }

                    /*if (localVariable && localVariable['kpi46'] && localVariable['kpi46'].value && localVariable['kpi46'].loggedTimeValueList) {
                        localVariable['kpi46'].loggedTimeValueList.map(function (data) {
                            data.data = data.data + '\'s LogTime';
                        });
                        localVariable['kpi46'].trendValueList = localVariable['kpi46'].trendValueList.concat(localVariable['kpi46'].loggedTimeValueList);
                    }*/

                    this.jiraKpiData = Object.assign({}, this.jiraKpiData, localVariable);
                    this.createAllKpiArray(localVariable);
                    /*if(this.jiraKpiData.kpi83){
                        this.processART(false);
                    } else if (this.jiraKpiData.kpi36) {
                      this.processRCA(false);
                    }*/
                } else {
                    this.jiraKpiData = getData;
                    postData.kpiList.forEach(element => {
                        this.loaderJiraArray.splice(this.loaderJiraArray.indexOf(element.kpiId), 1);
                    });
                }
                this.kpiLoader = false;
            });
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
                    // if (this.bitBucketKpiData && this.bitBucketKpiData.kpi65 && this.bitBucketKpiData.kpi65.value) {
                    //     this.processNoOfCheckins(true);
                    // }
                    this.createAllKpiArray(this.bitBucketKpiData);
                } else {
                    this.bitBucketKpiData = getData;
                }
            });
    }


    // post request of Jira(Kanban)
    postJiraKanbanKpi(postData, source): void {
        // this.loaderJira = true;
        postData.kpiList.forEach(element => {
            this.loaderJiraKanbanArray.push(element.kpiId);
        });

        // if (this.jiraKpiRequest && this.jiraKpiRequest !== '') {
        //     this.jiraKpiRequest.unsubscribe();
        // }

        this.jiraKpiRequest = this.httpService.postKpiKanban(postData, source)
            .subscribe(getData => {
                // this.loaderJira = false;
                if (getData !== null && getData[0] !== 'error' && !getData['error']) {
                    // creating array into object where key is kpi id
                    const localVariable = this.helperService.createKpiWiseId(getData);
                    for (const kpi in localVariable) {
                        this.loaderJiraKanbanArray.splice(this.loaderJiraKanbanArray.indexOf(kpi), 1);
                    }

                    if (localVariable['kpi997']) {
                        if (localVariable['kpi997'].trendValueList && localVariable['kpi997'].xAxisValues) {
                            localVariable['kpi997'].trendValueList.forEach(trendElem => {
                                trendElem.value.forEach(valElem => {
                                    if (valElem.value.length === 5 && localVariable['kpi997'].xAxisValues.length === 5) {
                                        valElem.value.forEach((element, index) => {
                                            element['xAxisTick'] = localVariable['kpi997'].xAxisValues[index];
                                        });
                                    }
                                });
                            });
                        }
                    }

                    this.jiraKpiData = Object.assign({}, this.jiraKpiData, localVariable);
                    this.createAllKpiArray(localVariable);
                } else {
                    this.jiraKpiData = getData;
                    postData.kpiList.forEach(element => {
                        this.loaderJiraKanbanArray.splice(this.loaderJiraKanbanArray.indexOf(element.kpiId), 1);
                    });
                }
            });
    }



    // on click of priority cycle time kanban this is called
    changeCycleTimePriority() {
        this.sumPriorityWise('openTriage');
        this.sumPriorityWise('triageClosed');
        this.sumPriorityWise('closedResolved');
    }

    // return sum of all seleted cycle time kanaban with prioritywise
    sumPriorityWise(type) {
        let sum = 0;
        this.prioritySum[type] = 0;
        if (this.jiraKpiData && this.jiraKpiData.kpi53 && this.jiraKpiData.kpi53.value && this.jiraKpiData.kpi53.value[type]) {
            if (this.selectedPriorityFilter && this.selectedPriorityFilter.kpi53 && this.selectedPriorityFilter.kpi53.length !== 0) {
                for (const index in this.jiraKpiData.kpi53.value[type]) {
                    const obj = this.jiraKpiData.kpi53.value[type][index];
                    for (const innerIndex in this.selectedPriorityFilter.kpi53) {
                        const innerObj = this.selectedPriorityFilter.kpi53[innerIndex];
                        if (obj.priority === innerObj.data) {
                            sum += obj.data;
                        }
                    }
                }
            } else {
                for (const index in this.jiraKpiData.kpi53.value[type]) {
                    const obj = this.jiraKpiData.kpi53.value[type][index];
                    if (obj.data) {
                        sum += obj.data;
                    }
                }
            }
            this.prioritySum[type] = sum;
            return sum;
        } else {
            return 0;
        }
    }

    // to get the value of cycle time kanban according to priority
    getValuePrioritywise(type, selectedPriority) {
        let value = 0;
        if (this.jiraKpiData && this.jiraKpiData.kpi53 && this.jiraKpiData.kpi53.value && this.jiraKpiData.kpi53.value[type]) {
            for (let index = 0; index < this.jiraKpiData.kpi53.value[type].length; index++) {
                const obj = this.jiraKpiData.kpi53.value[type][index];
                if (obj.priority === selectedPriority) {
                    value = obj.data;
                    break;
                }
            }
        }
        return value;
    }


    // to get the width of cycle time kanban according to priority
    getWidthPrioritywise(type, selectedPriority) {
        const value = this.getValuePrioritywise(type, selectedPriority);
        if (this.prioritySum[type] !== 0) {
            return (value * 100) / this.prioritySum[type] + '%';
        } else {
            return '0%';
        }

    }

    // get color of cycle time kanban according to priority
    getPriorityColor(index) {
        const color = ['#1F77B4', '#FE7F0C', '#2BA02C', '##D62728', '#9467BD', '#8C554B', '#E376C2', '#7F7F7F', '#BDBD22', '#1ABECF'];
        return color[index];
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

    getKPIName(kpiId) {
        if (this.masterData && this.masterData.kpiList && this.masterData.kpiList.length) {
            return this.masterData.kpiList.filter(kpi => kpi.kpiId === 'kpi11')[0].kpiName;
        } else {
            return ' ';
        }
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

    changeView(text) {
        if (text == 'list') {
            this.isChartView = false;
        } else {
            this.isChartView = true;
        }
    }

    checkTime(time) {
        if (time % 1 !== 0) {
            time = time.toFixed(2);
        }
        return time < 1 ? (time * 60).toFixed(2) + ' mins' : time + ' hrs';
    }

    sortAlphabetically(objArray) {
        if (objArray && objArray?.length > 1) {
            objArray?.sort((a, b) => a.data.localeCompare(b.data));
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
                this.kpiChartData[kpiId] = this.helperService.applyAggregationLogic(tempArr, aggregationType, this.tooltip.percentile);

            } else {
                if (this.kpiSelectedFilterObj[kpiId]?.length > 0) {
                    this.kpiChartData[kpiId] = trendValueList?.filter(x => x['filter'] == this.kpiSelectedFilterObj[kpiId][0])[0]?.value;
                } else {
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
        if (this.colorObj && Object.keys(this.colorObj)?.length > 0) {
           this.kpiChartData[kpiId] = this.generateColorObj(kpiId, this.kpiChartData[kpiId]);
        }

        // if (this.kpiChartData && Object.keys(this.kpiChartData) && Object.keys(this.kpiChartData).length === this.updatedConfigGlobalData.length) {
        if (this.kpiChartData && Object.keys(this.kpiChartData).length && this.updatedConfigGlobalData) {
            this.helperService.calculateGrossMaturity(this.kpiChartData, this.updatedConfigGlobalData);
        }
        // For kpi3 and kpi53 generating table column headers and table data
        if (kpiId === 'kpi3' || kpiId === 'kpi53') {
            //generating column headers
            const columnHeaders = [];
            if (Object.keys(this.kpiSelectedFilterObj)?.length && this.kpiSelectedFilterObj[kpiId]?.length && this.kpiSelectedFilterObj[kpiId][0]) {
                columnHeaders.push({ field: 'name', header: this.hierarchyLevel[+this.filterApplyData.level - 1]?.hierarchyLevelName + ' Name' });
                columnHeaders.push({ field: 'value', header: this.kpiSelectedFilterObj[kpiId][0] });
                columnHeaders.push({ field: 'maturity', header: 'Maturity' });
            }
            if (this.kpiChartData[kpiId]) {
                this.kpiChartData[kpiId].columnHeaders = columnHeaders;
            }
            //generating Table data
            const kpiUnit = this.updatedConfigGlobalData?.find(kpi => kpi.kpiId === kpiId)?.kpiDetail?.kpiUnit;
            const data = [];
            if (this.kpiChartData[kpiId] && this.kpiChartData[kpiId].length) {
                for (let i = 0; i < this.kpiChartData[kpiId].length; i++) {
                    const rowData = {
                        name: this.kpiChartData[kpiId][i].data,
                        maturity: 'M' + this.kpiChartData[kpiId][i].maturity,
                        value: this.kpiChartData[kpiId][i].value[0].data + ' ' + kpiUnit
                    };
                    data.push(rowData);
                }
                
                this.kpiChartData[kpiId].data = data;
            }
            this.showKpiTrendIndicator[kpiId] = false;

        }
        this.createTrendsData(kpiId);
        
        
    }

    ifKpiExist(kpiId) {
        const id = this.allKpiArray?.findIndex((kpi) => kpi.kpiId == kpiId);
        return id;
    }

    createAllKpiArray(data, inputIsChartData = false) {
        for (const key in data) {
            const idx = this.ifKpiExist(data[key]?.kpiId);
            if (idx !== -1) {
                this.allKpiArray.splice(idx, 1);
            }
            this.allKpiArray.push(data[key]);
            const trendValueList = this.allKpiArray[this.allKpiArray?.length - 1]?.trendValueList;
            if (trendValueList?.length > 0 && trendValueList[0]?.hasOwnProperty('filter')) {
                this.kpiSelectedFilterObj[data[key]?.kpiId] = [];
                this.getDropdownArray(data[key]?.kpiId);
                const formType = this.updatedConfigGlobalData?.filter(x => x.kpiId == data[key]?.kpiId)[0]?.kpiDetail?.kpiFilter;
                if (formType?.toLowerCase() == 'radiobutton') {
                    this.kpiSelectedFilterObj[data[key]?.kpiId]?.push(this.kpiDropdowns[data[key]?.kpiId][0]?.options[0]);
                } else if (formType?.toLowerCase() == 'dropdown') {
                    this.kpiSelectedFilterObj[data[key]?.kpiId]?.push(this.kpiDropdowns[data[key]?.kpiId][0]?.options[0]);
                } else {
                    this.kpiSelectedFilterObj[data[key]?.kpiId]?.push('Overall');
                }
                this.service.setKpiSubFilterObj(this.kpiSelectedFilterObj);
            }
            const agType = this.updatedConfigGlobalData?.filter(x => x.kpiId == data[key]?.kpiId)[0]?.kpiDetail?.aggregationCriteria;
            if (!inputIsChartData) {
                this.getChartData(data[key]?.kpiId, (this.allKpiArray?.length - 1), agType);
            }
        }
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
                    for(let key in this.colorObj){
                        let kpiFilter = trendValueList[i]?.value?.findIndex(x => this.colorObj[key]?.nodeName == x.data);
                        if(kpiFilter != -1){
                            let ifExist = optionsArr.findIndex(x=>x == trendValueList[i]?.filter);
                            if(ifExist == -1){
                                optionsArr?.push(trendValueList[i]?.filter);
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
                } else {
                    for (let i = 0; i < event[key]?.length; i++) {
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
    downloadGlobalExcel(){
        let worksheet;
        const workbook = new Excel.Workbook();
        worksheet = workbook.addWorksheet('Kpi Data');
        // let level = this.service.getSelectedLevel();
        let trends = this.service.getSelectedTrends();
        // let firstRow = [level['hierarchyLevelName']];
        // let headerNames = ["KPI Name"];
        let headers = [{header: 'KPI Name', key: 'kpiName', width: 30}];
        for(let i = 0; i<trends.length; i++){
            // firstRow.push(trends[i]['nodeName']);
            // headerNames.push("Latest ("+trends[i]['nodeName'] +")");
            // headerNames.push("Trend ("+trends[i]['nodeName'] +")");
            // headerNames.push("Maturity ("+trends[i]['nodeName'] +")");
            let colorCode = this.trendBoxColorObj[trends[i]['nodeName']]?.color;
            colorCode = colorCode.slice(1);
            headers.push({header:"Latest ("+trends[i]['nodeName'] +")", key: trends[i]['nodeName'] + '_latest', width: 15});
            headers.push({header:"Trend ("+trends[i]['nodeName'] +")", key: trends[i]['nodeName'] + '_trend', width: 15});
            headers.push({header:"Maturity ("+trends[i]['nodeName'] +")", key: trends[i]['nodeName'] + '_maturity', width: 15});
            worksheet.getRow(1).getCell((i*3)+2).fill = { type: 'pattern', pattern: 'solid', fgColor:{argb:colorCode} };
            worksheet.getRow(1).getCell((i*3)+3).fill = { type: 'pattern', pattern: 'solid', fgColor:{argb:colorCode} };
            worksheet.getRow(1).getCell((i*3)+4).fill = { type: 'pattern', pattern: 'solid', fgColor:{argb:colorCode} };
        }
        
        // worksheet.getRow(1).values = [firstRow[0]];
        // for(let i = 1; i<firstRow?.length; i++){
        //     worksheet.mergeCells(1, i+1, 1, i+3);
        //     worksheet.getCell(worksheet.getColumn(i+1)).value = firstRow[i+1];
        //     // worksheet.getCell().value = firstRow[i+1];
        // }
        // worksheet.getRow(1).values = [...firstRow];
        // worksheet.getRow(2).values = [...headerNames];
        worksheet.columns = [...headers];
        
        for(let kpi of this.updatedConfigGlobalData){
            let kpiId = kpi.kpiId;
            if(this.kpiTrendsObj[kpiId]?.length > 0){
                let obj = {};
                obj['kpiName'] = kpi?.kpiName;
                for(let i = 0; i< this.kpiTrendsObj[kpiId]?.length;i++){
                    obj[this.kpiTrendsObj[kpiId][i]?.hierarchyName +'_latest'] = this.kpiTrendsObj[kpiId][i]?.value;
                    obj[this.kpiTrendsObj[kpiId][i]?.hierarchyName +'_maturity'] = this.kpiTrendsObj[kpiId][i]?.maturity;
                    obj[this.kpiTrendsObj[kpiId][i]?.hierarchyName +'_trend'] = this.kpiTrendsObj[kpiId][i]?.trend;
                }
                worksheet.addRow(obj);
            }
        }
       

        worksheet.eachRow(function(row, rowNumber) {
            if (rowNumber === 1) {
                row.eachCell({
                    includeEmpty: true
                }, function(cell) {

                    cell.font = {
                        name: 'Arial Rounded MT Bold'
                    };
                });
            }
            row.eachCell({
                includeEmpty: true
            }, function(cell) {

                cell.border = {
                    top: {
                        style: 'thin'
                    },
                    left: {
                        style: 'thin'
                    },
                    bottom: {
                        style: 'thin'
                    },
                    right: {
                        style: 'thin'
                    }
                };
            });
        });
        // Footer Row
        worksheet.addRow([]);
        let footerRow = worksheet.addRow(['* KPIs which do not have any data are not included in the export']);
        footerRow.getCell(1).fill = {
            type: 'pattern',
            pattern: 'solid',
            fgColor: {
                argb: 'FFCCFFE5'
            }
        };
        footerRow.getCell(1).border = {
            top: {
                style: 'thin'
            },
            left: {
                style: 'thin'
            },
            bottom: {
                style: 'thin'
            },
            right: {
                style: 'thin'
            }
        };


        // Merge Cells
        worksheet.mergeCells(`A${footerRow.number}:F${footerRow.number}`);
       // Generate Excel File with given name
        workbook.xlsx.writeBuffer().then((data) => {
        const blob = new Blob([data as BlobPart], {
            type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        });
        fs.saveAs(blob, 'Kpi Data' + '.xlsx');
    });
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
        maturity = maturity != 'NA' && maturity != '--' && maturity != '-' ? 'M'+maturity : maturity;
        return maturity;
      }

      checkLatestAndTrendValue(kpiData, item){
        let latest:string = '';
        let trend:string = '';
        
        if(item?.value?.length > 0){
            let tempVal = item?.value[item?.value?.length - 1]?.lineValue ? item?.value[item?.value?.length - 1]?.lineValue : item?.value[item?.value?.length - 1]?.value; 
            let unit = kpiData?.kpiDetail?.kpiUnit?.toLowerCase() != 'number' ? kpiData?.kpiDetail?.kpiUnit : '';
            latest = tempVal > 0 ? (Math.round(tempVal * 10) / 10) + (unit ? ' ' + unit : '') : tempVal + (unit ? ' ' + unit : '');
        }
        if(item?.value?.length > 0 && kpiData?.kpiDetail?.showTrend) {
            if(kpiData?.kpiDetail?.trendCalculative){
                let lhsKey = kpiData?.kpiDetail?.trendCalculation?.length > 0 ? kpiData?.kpiDetail?.trendCalculation[0]?.lhs : '';
                let rhsKey = kpiData?.kpiDetail?.trendCalculation?.length > 0 ? kpiData?.kpiDetail?.trendCalculation[0]?.rhs : '';
                let lhs = item?.value[item?.value?.length - 1][lhsKey];
                let rhs = item?.value[item?.value?.length - 1][rhsKey];
                let operator = lhs < rhs ? '<' : lhs > rhs ? '>' : '=';
                let trendObj = kpiData?.kpiDetail?.trendCalculation?.find((item) => item.operator == operator);
                if(trendObj){
                    trend = trendObj['type']?.toLowerCase() == 'downwards' ? '-ve' : trendObj['type']?.toLowerCase() == 'upwards' ? '+ve' : '-- --';
                }else{
                    trend = 'NA'
                }
            }else{
                let lastVal = item?.value[item?.value?.length - 1]?.value;
                let secondLastVal = item?.value[item?.value?.length - 2]?.value;
                let isPositive = kpiData?.kpiDetail?.isPositiveTrend;
                if(secondLastVal > lastVal && !isPositive){
                    trend = '+ve';
                }else if(secondLastVal < lastVal && !isPositive){
                    trend = '-ve';
                }else if(secondLastVal < lastVal && isPositive){
                    trend = '+ve';
                }else if(secondLastVal > lastVal && isPositive){
                    trend = '-ve';
                }else {
                    trend = '-- --';
                }
            }
        }else{
            trend = 'NA';
        }
        return [latest, trend];
      }

      createTrendsData(kpiId){
        let enabledKpiObj = this.updatedConfigGlobalData?.filter(x => x.kpiId == kpiId)[0];
        if(enabledKpiObj && Object.keys(enabledKpiObj)?.length != 0){
            this.kpiTrendsObj[kpiId] = [];
            for(let i = 0; i < this.kpiChartData[kpiId]?.length; i++){
                if(this.kpiChartData[kpiId][i]?.value?.length > 0){
                    let trendObj = {};
                    const [latest, trend] = this.checkLatestAndTrendValue(enabledKpiObj, this.kpiChartData[kpiId][i]);
                    trendObj = {
                        "hierarchyName": this.kpiChartData[kpiId][i]?.data,
                        "value": latest,
                        "trend": trend,
                        "maturity": kpiId != 'kpi3' && kpiId != 'kpi53' ? 
                                    this.checkMaturity(this.kpiChartData[kpiId][i]) 
                                    : 'M'+this.kpiChartData[kpiId][i]?.maturity,
                    };
                    if(kpiId === 'kpi997'){
                        trendObj['value'] = 'NA';
                    }
                    this.kpiTrendsObj[kpiId]?.push(trendObj);
                }
            }
        }
      }
}
