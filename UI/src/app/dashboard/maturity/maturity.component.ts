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

/*************
Kpi maturity Dashboard
This is used to show maturity of kpi in form of d3.chart (sunburst chart).
@author anuj
**************/

import { Component, OnInit, OnDestroy } from '@angular/core';
// import * as d3 from 'd3-3';
import * as d3 from 'd3';
import { SharedService } from '../../services/shared.service';
import { HttpService } from '../../services/http.service';
import { HelperService } from '../../services/helper.service';
import { Router } from '@angular/router';
import { distinctUntilChanged, mergeMap } from 'rxjs/operators';


@Component({
    selector: 'app-maturity',
    templateUrl: './maturity.component.html',
    styleUrls: ['./maturity.component.css']
})
export class MaturityComponent implements OnInit, OnDestroy {

    masterData = <any>{};
    filterData = <any>[];
    sonarKpiData = <any>{};
    jenkinsKpiData = <any>{};
    zypherKpiData = <any>{};
    jiraKpiData = <any>{};
    bitBucketKpiData = <any>{};
    selectedFilterCount = 0;
    filterRequestData = {};
    filterkeys = <any>[];
    filterApplyData = <any>{};
    kpiListSonar = <any>{};
    kpiJenkins = <any>{};
    kpiZypher = <any>{};
    kpiJira = <any>{};
    kpiBitBucket = <any>{};
    checkinsPerDay = <any>[];
    gaugemap = {};
    defectCount = <any>[];
    jiraGroups = 0;
    loaderJenkins = false;
    loaderJira = false;
    loaderSonar = false;
    loaderZypher = false;
    loaderBitBucket = false;
    loaderMaturity = false;
    maturityValue = <any>{};
    subscription = [];
    jiraKpiRequest = <any>'';
    sonarKpiRequest = <any>'';
    zypherKpiRequest = <any>'';
    jenkinsKpiRequest = <any>'';
    bitBucketKpiRequest = <any>'';
    selectedtype;
    configGlobalData;
    selectedTab = 'Overall';
    selectedTabIndex = 0;
    tabs = [];
    selectedTabKpis = [];
    noKpi = false;
    noOfJiraGroups = 0;
    loader= false;
    showNoDataMsg = false;
    noDataForFilter = false;
    noProjects =false;
    isKanban = false;
    constructor(private service: SharedService, private httpService: HttpService, private helperService: HelperService, private router: Router) {
        this.subscription.push(this.service.passDataToDashboard.pipe(distinctUntilChanged()).subscribe((sharedobject) => {
            this.receiveSharedData(sharedobject);
        }));

        this.subscription.push(this.service.setNoData.subscribe(data =>{
            this.noDataForFilter = data;
            this.receiveSharedData(this.service.getFilterObject());
        }));
        this.selectedtype = this.service.getSelectedType();

        this.subscription.push(this.service.onTypeOrTabRefresh.pipe(distinctUntilChanged()).subscribe(data => {
            this.noOfJiraGroups = 0;
            this.loaderSonar = false;
            this.loaderZypher = false;
            this.loaderBitBucket = false;
            this.loaderJenkins = false;
            this.loaderJira = false;
            this.selectedtype = data?.selectedType;
            this.showNoDataMsg = false;
        }));

    }

    ngOnDestroy() {
        this.subscription.forEach(subscription => subscription.unsubscribe());
    }
    receiveSharedData($event) {
        this.loader =true;
        this.jiraGroups = 0;
        this.showNoDataMsg = false;
        if (this.service.getSelectedTab() === 'Maturity') {
            this.masterData = $event?.masterData;
            this.filterData = $event?.filterData;
            this.filterApplyData = $event?.filterApplyData;
            this.loaderMaturity = true;
            this.isKanban = this.selectedtype?.toLowerCase() === 'kanban';
            const kpiIdsForCurrentBoard = this.service.getMasterData()['kpiList']?.filter(kpi => kpi.calculateMaturity && kpi.kanban === this.isKanban).map(kpi => kpi.kpiId);
            if(this.filterData?.length > 0 && kpiIdsForCurrentBoard?.length > 0 && this.selectedtype){
                // this.drawAreaChart(null, null);
                // this.chart(null);
                if (this.selectedtype?.toLowerCase() === 'scrum') {
                    this.groupJenkinsKpi(kpiIdsForCurrentBoard);
                    this.groupZypherKpi(kpiIdsForCurrentBoard);
                    this.groupBitBucketKpi(kpiIdsForCurrentBoard);
                    this.groupSonarKpi(kpiIdsForCurrentBoard);
                    this.groupJiraKpi(kpiIdsForCurrentBoard);
                } else {
                    this.groupJenkinsKanbanKpi(kpiIdsForCurrentBoard);
                    this.groupZypherKanbanKpi(kpiIdsForCurrentBoard);
                    this.groupBitBucketKanbanKpi(kpiIdsForCurrentBoard);
                    this.groupSonarKanbanKpi(kpiIdsForCurrentBoard);
                    this.groupJiraKanbanKpi(kpiIdsForCurrentBoard);
                }
            }else{
                this.loader = false;
                this.showNoDataMsg = true;
            }
        }
    }

    ngOnInit() {
        this.selectedtype = this.service.getSelectedType();
            this.subscription.push(this.service.globalDashConfigData.subscribe((globalConfig) => {
                this.configGlobalData = globalConfig;
                this.tabs = this.configGlobalData[this.selectedtype.toLowerCase()].filter(board => board?.boardName.toLowerCase() !== 'iteration' && board?.boardName.toLowerCase() !== 'developer');
                this.selectedTabKpis = this.tabs[0].kpis.filter(kpi => kpi.kpiDetail.calculateMaturity && kpi.shown && kpi.isEnabled);
            }));
            this.subscription.push(this.service.noProjectsObs.subscribe((res) => {
                this.noProjects = res;
                 this.isKanban= this.service.getSelectedType().toLowerCase() === 'kanban' ? true : false;
              }));

        if (this.service.getFilterObject()) {
            this.receiveSharedData(this.service.getFilterObject());
        }else{
            this.showNoDataMsg = true;
        }
    }



    // Used for grouping all Sonar kpi from master data and calling Sonar kpi.
    groupSonarKpi(kpiIdsForCurrentBoard) {
        this.kpiListSonar = this.helperService.groupKpiFromMaster('Sonar', false, this.masterData, this.filterApplyData, this.filterData, kpiIdsForCurrentBoard, '',this.selectedTab);
        if (this.kpiListSonar?.kpiList?.length > 0) {
            this.postSonarKpi(this.kpiListSonar, 'sonar');
        }
    }

    // Used for grouping all Jenkins kpi from master data and calling jenkins kpi.
    groupJenkinsKpi(kpiIdsForCurrentBoard) {
        this.kpiJenkins = this.helperService.groupKpiFromMaster('Jenkins', false, this.masterData, this.filterApplyData, this.filterData, kpiIdsForCurrentBoard, '',this.selectedTab);
        if (this.kpiJenkins?.kpiList?.length > 0) {
            this.postJenkinsKpi(this.kpiJenkins, 'jenkins');
        }
    }

    // Used for grouping all Sonar kpi from master data and calling Sonar kpi.
    groupZypherKpi(kpiIdsForCurrentBoard) {
        this.kpiZypher = this.helperService.groupKpiFromMaster('Zypher', false, this.masterData, this.filterApplyData, this.filterData, kpiIdsForCurrentBoard, '',this.selectedTab);
        if (this.kpiZypher?.kpiList?.length > 0) {
            this.postZypherKpi(this.kpiZypher, 'zypher');
        }
    }

    // Used for grouping all Sonar kpi from master data and calling Sonar kpi.(only for scrum).
    groupJiraKpi(kpiIdsForCurrentBoard) {

        this.jiraKpiData = {};
        // creating a set of unique group Ids
        const groupIdSet = new Set();
        this.masterData?.kpiList?.forEach((obj) => {
            if (!obj.kanban && obj.kpiSource === 'Jira') {
                groupIdSet.add(obj.groupId);
            }
        });
        this.noOfJiraGroups =groupIdSet.size;

        // sending requests after grouping the the KPIs according to group Id
        groupIdSet.forEach((groupId) => {
            if (groupId) {
                this.kpiJira = this.helperService.groupKpiFromMaster('Jira', false, this.masterData, this.filterApplyData, this.filterData, kpiIdsForCurrentBoard, groupId,this.selectedTab);
                if (this.kpiJira?.kpiList?.length > 0) {
                    this.postJiraKpi(this.kpiJira, 'jira');
                } else {
                    this.noOfJiraGroups--;
                }
            }
        });
    }

    // Used for grouping all BitBucket kpi of scrum from master data and calling BitBucket kpi.
    groupBitBucketKpi(kpiIdsForCurrentBoard) {
        this.kpiBitBucket = this.helperService.groupKpiFromMaster('BitBucket', false, this.masterData, this.filterApplyData, this.filterData, kpiIdsForCurrentBoard, '',this.selectedTab);
        if (this.kpiBitBucket?.kpiList?.length > 0) {
            this.postBitBucketKpi(this.kpiBitBucket, 'bitbucket');
        }
    }

    // Used for grouping all jira kpi of kanban from master data and calling jira kpi of kanban.
    groupJiraKanbanKpi(kpiIdsForCurrentBoard) {
        this.jiraKpiData = {};
        // creating a set of unique group Ids
        const groupIdSet = new Set();
        this.masterData?.kpiList?.forEach((obj) => {
            if (obj.kanban && obj.kpiSource === 'Jira') {
                groupIdSet.add(obj.groupId);
            }
        });

        this.noOfJiraGroups =groupIdSet.size;
        // sending requests after grouping the the KPIs according to group Id
        groupIdSet.forEach((groupId) => {
            if (groupId) {
                this.kpiJira = this.helperService.groupKpiFromMaster('Jira', true, this.masterData, this.filterApplyData, this.filterData, kpiIdsForCurrentBoard, groupId,this.selectedTab);
                if (this.kpiJira?.kpiList?.length > 0) {
                    this.postJiraKpi(this.kpiJira, 'jira');
                } else {
                    this.noOfJiraGroups--;
                }
            }
        });
    }
    // Used for grouping all Sonar kpi of kanban from master data and calling Sonar kpi.
    groupSonarKanbanKpi(kpiIdsForCurrentBoard) {
        this.kpiListSonar = this.helperService.groupKpiFromMaster('Sonar', true, this.masterData, this.filterApplyData, this.filterData, kpiIdsForCurrentBoard, '',this.selectedTab);
        if (this.kpiListSonar?.kpiList?.length > 0) {
            this.postSonarKpi(this.kpiListSonar, 'sonar');
        }
    }

    // Used for grouping all Jenkins kpi of kanban from master data and calling jenkins kpi.
    groupJenkinsKanbanKpi(kpiIdsForCurrentBoard) {
        this.kpiJenkins = this.helperService.groupKpiFromMaster('Jenkins', true, this.masterData, this.filterApplyData, this.filterData, kpiIdsForCurrentBoard, '',this.selectedTab);
        if (this.kpiJenkins?.kpiList?.length > 0) {
            this.postJenkinsKpi(this.kpiJenkins, 'jenkins');
        }
    }

    // Used for grouping all Zypher kpi of kanban from master data and calling Zypher kpi.
    groupZypherKanbanKpi(kpiIdsForCurrentBoard) {
        this.kpiZypher = this.helperService.groupKpiFromMaster('Zypher', true, this.masterData, this.filterApplyData, this.filterData, kpiIdsForCurrentBoard, '',this.selectedTab);
        if (this.kpiZypher?.kpiList?.length > 0) {
            this.postZypherKpi(this.kpiZypher, 'zypher');
        }
    }

    // Used for grouping all BitBucket kpi of kanban from master data and calling BitBucket kpi.
    groupBitBucketKanbanKpi(kpiIdsForCurrentBoard) {
        this.kpiBitBucket = this.helperService.groupKpiFromMaster('BitBucket', true, this.masterData, this.filterApplyData, this.filterData, kpiIdsForCurrentBoard, '',this.selectedTab);
        if (this.kpiBitBucket?.kpiList?.length > 0) {
            this.postBitBucketKpi(this.kpiBitBucket, 'bitbucket');
        }
    }


    postSonarKpi(postData, source): void {
        this.loaderSonar = true;
        //call api based on selected type like Scrum or Kanban
        this.sonarKpiRequest = this.selectedtype.toLowerCase() === 'scrum' ? this.httpService.postKpi(postData, source) : this.httpService.postKpiKanban(postData, source);
        this.subscription.push(this.sonarKpiRequest
            .subscribe(getData => {
                this.loaderSonar = false;
                // this.loaderMaturity = false;
                if (!(getData !== null && getData[0] === 'error')) {
                    this.sonarKpiData = getData;
                    const newObject = {};
                    for (const obj in this.sonarKpiData) {
                        newObject[this.sonarKpiData[obj].kpiId] = this.sonarKpiData[obj];
                        this.maturityValue[this.sonarKpiData[obj].kpiId] = this.sonarKpiData[obj];

                    }
                    this.sonarKpiData = newObject;

                    // if (this.sonarKpiData && this.sonarKpiData.kpi41 && this.sonarKpiData.kpi39) {
                    //     this.drawAreaChart(this.sonarKpiData.kpi41.value, this.sonarKpiData.kpi39.value);
                    // } else {
                    //     this.drawAreaChart(null, null);
                    // }

                }
            }));

    }
    postJenkinsKpi(postData, source): void {
        this.loaderJenkins = true;
        this.jenkinsKpiRequest = this.selectedtype.toLowerCase() === 'scrum' ? this.httpService.postKpi(postData, source) : this.httpService.postKpiKanban(postData, source);
        this.jenkinsKpiRequest.subscribe(getData => {
            this.loaderJenkins = false;
            if (!(getData !== null && getData[0] === 'error')) {

                this.jenkinsKpiData = getData;
                const newObject = {};
                for (const obj in this.jenkinsKpiData) {
                    newObject[this.jenkinsKpiData[obj].kpiId] = this.jenkinsKpiData[obj];
                    this.maturityValue[this.jenkinsKpiData[obj].kpiId] = this.jenkinsKpiData[obj];
                }
                // this.loaderMaturity = false;
                this.jenkinsKpiData = newObject;
                // if (this.jenkinsKpiData && this.jenkinsKpiData.kpi70 && this.jenkinsKpiData.kpi42) {
                //     this.drawAreaChart(this.jenkinsKpiData.kpi70.trendValueList, this.jenkinsKpiData.kpi42.trendValueList);
                // } else {
                //     // this.drawAreaChart(null, null);
                // }

            }
        });
    }
    postZypherKpi(postData, source): void {
        this.loaderZypher = true;
        this.zypherKpiRequest = this.selectedtype.toLowerCase() === 'scrum' ? this.httpService.postKpi(postData, source) : this.httpService.postKpiKanban(postData, source);
        this.zypherKpiRequest.subscribe(getData => {
            this.loaderZypher = false;
            if (!(getData !== null && getData[0] === 'error')) {
                this.zypherKpiData = getData;
                const newObject = {};
                for (const obj in this.zypherKpiData) {
                    newObject[this.zypherKpiData[obj].kpiId] = this.zypherKpiData[obj];
                    this.maturityValue[this.zypherKpiData[obj].kpiId] = this.zypherKpiData[obj];

                }
                this.loaderMaturity = false;
                /*if (this.zypherKpiData && this.zypherKpiData.kpi16 && this.zypherKpiData.kpi42) {
                    this.drawAreaChart(this.zypherKpiData.kpi16.value, this.zypherKpiData.kpi42.value);
                } else {
                    this.drawAreaChart(null, null);
                }*/
                this.zypherKpiData = newObject;
            }
        });
    }
    postJiraKpi(postData, source): void {
        this.loaderJira =true;
        this.jiraKpiRequest = this.selectedtype.toLowerCase() === 'scrum' ? this.httpService.postKpi(postData, source) : this.httpService.postKpiKanban(postData, source);
        this.jiraKpiRequest.subscribe(getData => {
            // this.jiraGroups = false;
            this.jiraGroups++;
            this.loaderMaturity = false;
            if (!(getData !== null && getData[0] === 'error')) {
                // this.jiraKpiData = getData;
                this.jiraKpiData = { ...this.jiraKpiData, ...getData };
                const newObject = {};
                for (const obj in this.jiraKpiData) {
                    newObject[this.jiraKpiData[obj].kpiId] = this.jiraKpiData[obj];
                    this.maturityValue[this.jiraKpiData[obj].kpiId] = this.jiraKpiData[obj];
                }
                this.jiraKpiData = newObject;
            }
               //call handle tab change only after all jira kpis are fetched
               if (this.jiraGroups === this.noOfJiraGroups) {
                this.jiraGroups = 0;
                this.noOfJiraGroups = 0;
                this.loaderJira = false;
                this.handleTabChange(0);
            }
        });
    }

    postBitBucketKpi(postData, source): void {
        this.loaderBitBucket = true;

        this.bitBucketKpiRequest = this.selectedtype.toLowerCase() === 'scrum' ? this.httpService.postKpi(postData, source) : this.httpService.postKpiKanban(postData, source);
        this.bitBucketKpiRequest.subscribe(getData => {
            this.loaderBitBucket = false;
            // this.loaderMaturity = false;
            if (!(getData !== null && getData[0] === 'error')) {
                this.bitBucketKpiData = getData;
                const newObject = {};
                for (const obj in this.bitBucketKpiData) {
                    newObject[this.bitBucketKpiData[obj].kpiId] = this.bitBucketKpiData[obj];
                    this.maturityValue[this.bitBucketKpiData[obj].kpiId] = this.bitBucketKpiData[obj];
                }
                this.bitBucketKpiData = newObject;

                // if (this.bitBucketKpiData && this.bitBucketKpiData.kpi11 && this.bitBucketKpiData.kpi84) {
                //     this.drawAreaChart(this.bitBucketKpiData.kpi11.value, this.bitBucketKpiData.kpi84.value);
                // } else {
                //     this.drawAreaChart(null, null);
                // }

            }
        });
    }

    /**
     *
     * @param index
     * selected tab index
     * Called when user  switches between categories
     */
    handleTabChange(index) {
        this.selectedTabIndex = index;
        this.maturityValue = {};
        if(!(this.tabs.length > 0 && this.selectedTabKpis.length >0)){
            this.configGlobalData = this.service.getDashConfigData();
            this.tabs = this.configGlobalData[this.selectedtype.toLowerCase()].filter(board => board?.boardName.toLowerCase() !== 'iteration' &&  board?.boardName.toLowerCase() !== 'developer');
        }
        this.selectedTabKpis = this.tabs[index].kpis.filter(kpi => kpi.kpiDetail.calculateMaturity && kpi.shown && kpi.isEnabled).map(kpi => kpi.kpiId);
        const allCategoriesKpis = [this.jiraKpiData, this.jenkinsKpiData, this.sonarKpiData, this.zypherKpiData, this.bitBucketKpiData];
        //updated the maturityValue with selected Category kpi
        for (const category of allCategoriesKpis) {
            for (const obj in category) {
                if (this.selectedTabKpis.includes(category[obj].kpiId)) {
                    this.maturityValue[category[obj].kpiId] = category[obj];
                    category[obj]['group'] = index + 1;
                }
            }
        }

        this.noKpi = Object.keys(this.maturityValue).length === 0 ? true : false;
        this.selectedTab = index === 0 ? 'Overall' : this.tabs[index]?.boardName;
        this.drawAreaChart(index + 1);
    }


    drawAreaChart(categoryGroupId?) {
        this.loader=false;
        const getMaturityValueForChart = (kpiId) => {
            let result = 0;
            const getMaturityValueFromOverAllFilter = ['kpi8', 'kpi11', 'kpi84', 'kpi83', 'kpi118', 'kpi116', 'kpi17', 'kpi27', 'kpi65', 'kpi66'];
            if (getMaturityValueFromOverAllFilter.includes(kpiId)) {
                result = getMaturityValue(undefinedCheck(self.maturityValue[kpiId])
                    || undefinedCheck(self.maturityValue[kpiId].trendValueList)
                    || undefinedCheck(self.maturityValue[kpiId].trendValueList[0])
                    || undefinedCheck(self.maturityValue[kpiId].trendValueList[0].value) ? -1
                    : self.maturityValue[kpiId].trendValueList[0].value[0].maturity);
            } else if (kpiId === 'kpi72') {
                if (self.maturityValue[kpiId] && self.maturityValue[kpiId].trendValueList
                    && self.maturityValue[kpiId].trendValueList[0]) {
                    self.maturityValue[kpiId].trendValueList.forEach(terndValue => result += +terndValue.value[0].maturity);
                    result = getAverageMaturityValue(result / self.maturityValue[kpiId].trendValueList.length);
                } else {
                    result = 0;
                }
            } else {
                result = getMaturityValue(undefinedCheck(self.maturityValue[kpiId]) ||
                    undefinedCheck(self.maturityValue[kpiId].trendValueList) ||
                    undefinedCheck(self.maturityValue[kpiId].trendValueList[0]) ? -1 : self.maturityValue[kpiId].trendValueList[0].maturity);
            }
            return result;
        };

        const undefinedCheck = (attr) => {
            if (attr === undefined || attr === 'undefined') {
                return true;
            }
            return false;
        };

        const getMaturityValue = (mv) => {
            if (mv === undefined) {
                return 0;
            } else if (mv === -1) {
                return 0;
            } else {
                return mv;
            }
        };

        const getAverageMaturityValue = (mv) => {
            if (mv <= 0 || isNaN(mv) || null || undefined) {
                return 0;
            } else if (mv > 0 && mv <= 1) {
                return 1;
            } else if (mv > 1 && mv <= 2) {
                return 2;
            } else if (mv > 2 && mv <= 3) {
                return 3;
            } else if (mv > 3 && mv <= 4) {
                return 4;
            } else {
                return 5;
            }
        };

        d3.select('svg').remove();
        d3.select('.tooltip_').remove();
        d3.select('.tooltipForCategory').remove();
        const self = this;

        const startRotation = this.loaderMaturity;
        let root;
        if (startRotation) {

            root = {
                textLines: ['..'],
                children: [{
                    textLines: ['..'],
                    maturity: 0
                }, {
                    textLines: ['..'],
                    maturity: 0
                }, {
                    textLines: ['..'],
                    maturity: 0
                }, {
                    textLines: ['..'],
                    maturity: 0
                }, {
                    textLines: ['..'],
                    maturity: 0
                }, {
                    textLines: ['..'],
                    maturity: 0
                }, {
                    textLines: ['..'],
                    maturity: 0
                }, {
                    textLines: ['..'],
                    maturity: 0
                }, {
                    textLines: ['..'],
                    maturity: 0
                }, {
                    textLines: ['..'],
                    maturity: 0
                }, {
                    textLines: ['..'],
                    maturity: 0
                }]
            };
        } else {
            // on loading show data;
            root = {
                textLines: [this.selectedTab.toUpperCase()],
                children: []
            };

            let sumOfMatirity = 0;
            if (Object.keys(this.maturityValue).length > 0) {
                for (const kpi in this.maturityValue) {
                    if (kpi === 'kpi3' || kpi === 'kpi53') {
                        let maturiyRangeValue = 5;
                        this.maturityValue[kpi]['trendValueList']?.forEach((kpiValue, index) => {
                            if (index !== 0) {
                                root.children.push({
                                    textLines: [kpiValue.filter],
                                    maturity: getMaturityValue(undefinedCheck(this.maturityValue[kpi]) || undefinedCheck(this.maturityValue[kpi].trendValueList) || undefinedCheck(this.maturityValue[kpi].trendValueList[index]) ? -1 : this.maturityValue[kpi].trendValueList[index].value[0].maturity),
                                    maturityRange: undefinedCheck(this.maturityValue[kpi]) || undefinedCheck(this.maturityValue[kpi].maturityRange) ? 'undefined' : this.maturityValue[kpi].maturityRange.slice(maturiyRangeValue, maturiyRangeValue + 5),
                                    group: this.maturityValue[kpi].group ? this.maturityValue[kpi].group : 1,
                                    kpiDefinition: this.maturityValue[kpi].kpiInfo.definition,
                                    kpiId: kpi
                                });
                                maturiyRangeValue = maturiyRangeValue + 5;
                                sumOfMatirity += +getMaturityValue(undefinedCheck(this.maturityValue[kpi]) || undefinedCheck(this.maturityValue[kpi].trendValueList) || undefinedCheck(this.maturityValue[kpi].trendValueList[index]) ? -1 : this.maturityValue[kpi].trendValueList[index].value[0].maturity);
                            }
                        });
                    } else {
                        root.children.push({
                            textLines: [this.maturityValue[kpi].kpiName.split(' ').slice(0, 2).join(' '), this.maturityValue[kpi].kpiName.split(' ').slice(2).join(' ')],
                            maturity: getMaturityValueForChart(this.maturityValue[kpi].kpiId),
                            maturityRange: undefinedCheck(this.maturityValue[kpi]) ? 'undefined' : this.maturityValue[kpi].maturityRange,
                            group: this.maturityValue[kpi].group ? this.maturityValue[kpi].group : 1,
                            kpiDefinition: this.maturityValue[kpi].kpiInfo.definition,
                            kpiId: kpi
                        });
                        sumOfMatirity += +getMaturityValueForChart(this.maturityValue[kpi].kpiId);
                    }
                }
            } else {
                root.children.push({
                    textLines: [''],
                    group: categoryGroupId,
                    maturity: 0,
                    maturityRange: undefined
                });
            }
            if (this.selectedTab !== 'Overall') {
                root.textLines = [...root.textLines, '(M' + getAverageMaturityValue(sumOfMatirity / root.children.length) + ')'];
            } else {
                const allKpis = root.children;
                const tabCategory = {};
                this.tabs.forEach((tab, index) => {
                    if (index !== 0) {
                        tabCategory[tab.boardName] = tab.kpis.filter(kpi => kpi.kpiDetail.calculateMaturity && kpi.shown).map(kpi => kpi.kpiId);
                    }
                });
                const kpisInOverAllTab = this.tabs[0].kpis.filter(kpi => kpi.kpiDetail.calculateMaturity && kpi.shown && kpi.isEnabled).map(kpi => kpi.kpiId);
                const children = [];
                sumOfMatirity = 0;
                for (const category in tabCategory) {
                    const tab = {
                        textLines: [category],
                        maturityRange: 'undefined',
                        group: children.length + 2
                    };
                    const categoryKpis = allKpis.filter(kpi => tabCategory[category].includes(kpi.kpiId) && kpisInOverAllTab.includes(kpi.kpiId));
                    const sumOfMaturityForCategory = categoryKpis.reduce((sum, kpi) => sum + +kpi.maturity, 0);
                    tab['maturity'] = getAverageMaturityValue(sumOfMaturityForCategory !== 0 ? (sumOfMaturityForCategory / categoryKpis.length).toFixed(2) : 0);
                    sumOfMatirity += +tab['maturity'];
                    children.push(tab);
                }
                root.children = children;
                root.textLines = [...root.textLines, '(M' + getAverageMaturityValue(sumOfMatirity / root.children.length) + ')'];
            }
        }
        d3.select('.chart123').datum(root).call(sunburstBarChart());

        const div = d3.select('.chart123').append('div')
            .attr('class', 'tooltip_')
            .style('opacity', 1)
            .style('display', 'none');

        //add tooltip for Overall Tab
        const tooltipForMainCategoryDiv = d3.select('.chart123').append('div')
            .attr('class', 'tooltipForCategory')
            .style('opacity', 1)
            .style('display', 'none');

        function sunburstBarChart() {
            const edge = 720;
            const maxBarValue = 5;
            const rotation = -95 * Math.PI / 180;

            const radius = edge / 2;
            const effectiveEdge = edge * 0.9;
            // scale = d3.scaleLinear().domain([0, maxBarValue + 5]);

            const chart = function (selection) {
                selection.each(function (data) {

                    // Data strucure
                    const partition = d3.partition()
                        .size([2 * Math.PI, radius]);

                    // Find data root
                    const root = d3.hierarchy(getRoot2(JSON.parse(JSON.stringify(data))))
                        .sum(function (d) {
                            return d.size;
                        });


                    // Size arcs
                    partition(root);

                    const svg = d3.select(this).append('svg')
                        .attr('width', effectiveEdge)
                        .attr('height', effectiveEdge)
                        .append('g')
                        .attr('transform', 'translate(' + (effectiveEdge / 2) + ',' + (effectiveEdge / 2) + ')')
                        .attr('id', 'KPI-Maturity-Chart');

                    let rotate = 5;
                    function rotateChart() {
                        svg.transition().attr('transform', 'translate(' + effectiveEdge / 2 + ',' + effectiveEdge / 2 + ') rotate(' + rotate + ')');
                        rotate = (rotate + 5) % 360;
                    }
                    const intervalId = window.setInterval(rotateChart, 100);


                    if (!startRotation) {
                        window.clearInterval(intervalId);
                    }

                    const y = d3.scaleLinear()
                        .range([-(maxBarValue + 1), maxBarValue + 2]);
                    const arc = d3.arc()
                        .startAngle(function (d) {
                            return d.x0 + rotation;
                        })
                        .endAngle(function (d) {
                            return d.x1 + rotation;
                        })
                        .innerRadius(function (d) {
                            if (d.depth && d.depth > 2 && d.depth < maxBarValue + 2) {
                                d.yi = Math.max(0, radius - y(Math.sqrt(d.y1)));
                            } else {
                                d.yi = radius - 24 - d.y1;
                            }
                            return d.yi;
                        })
                        .outerRadius(function (d) {
                            if (d.depth && d.depth > 2 && d.depth < maxBarValue + 2) {
                                d.yo = Math.max(0, radius - y(Math.sqrt(d.y0)));
                            } else {
                                d.yo = radius - 24 - d.y0;
                            }
                            return d.yo;
                        });

                    // Put it all together
                    svg.selectAll('path')
                        .data(root.descendants())
                        .enter().append('g').attr('class', 'node')
                        .append('path')
                        .attr('display', function (d) {
                            return d.depth ? null : 'none';
                        })
                        .attr('d', arc)
                        .style('stroke', '#fff')
                        .attr('class', function (d) {
                            return d.depth === 1 ? 'white' : null
                        })
                        .on('mouseover', function (event, d) {
                            if (d.depth > 1) {
                                let tooltip = d3.select(this).classed('highlight', true)
                                div.transition()
                                    .duration(200)
                                    .style('opacity', .9)
                                    .style('display', (self.selectedTab !== 'Overall' && !self.noKpi) ? 'inline-block' : 'none');
                                div.html(d.data.maturityLevelsToolTip);
                            }
                        })
                        .on('mouseout', function (event, d) {
                            d3.select(this).classed('highlight', false);
                            div.transition()
                                .duration(500)
                                .style('opacity', 1)
                                .style('display', 'none');
                        })
                        .attr('class', function (d) {
                            let styleClass = 'nodesBorder';
                            if (d.depth && d.depth > 1 && d.depth < maxBarValue + 3) {

                                styleClass += ' group-' + d.data.group + (d.data.on ? '-on' : '-off');
                            }
                            // if (d.depth === 2) {
                            //     styleClass += ' labelTextBackground';
                            // }

                            if (d.depth === 1) {
                                styleClass += ' group-' + d.data.children[0].group + '-on';
                            }
                            return styleClass;
                        })
                        .attr('fill-rule', 'evenodd');

                    const labelArc = d3.arc()
                        .startAngle(function (d) {
                            return d.x0 + rotation;
                        })
                        .endAngle(function (d) {
                            return d.x1 + rotation;
                        })
                        .innerRadius(function (d, i) {
                            if (d.data && d.data.textLines) {
                                return d3.scaleLinear().domain([-1, d.data.textLines.length]).range([d.yi, d.yo])(i);
                            }
                            return radius + 80 - d.y1;
                        })
                        .outerRadius(function (d, i) {
                            if (d.data && d.data.textLines) {
                                return d3.scaleLinear().domain([-1, d.data.textLines.length]).range([d.yi, d.yo])(i);
                            }
                            return radius + 80 - d.y0;
                        });

                    // Add labels to the last arc
                    svg.selectAll('.node').filter(function (d) {
                        return d.depth === 1;
                    })
                        .selectAll('.labelPath')
                        .data(function (d, i) {
                            d.i = i; return Array(d.data.textLines.length).fill(d);
                        })
                        .enter()
                        .append('path')
                        .attr('fill', 'none')
                        .attr('stroke', 'none')
                        .attr('id', function (d, i) {
                            return 'arc-label' + d.i + '-' + i;
                        })
                        .attr('d', labelArc);

                    svg.selectAll('.node').filter(function (d) {
                        return d.depth === 1;
                    }).attr('class', 'labelarc')
                        .selectAll('.labelText')
                        .data(function (d, i) {
                            d.i = i; return Array(d.data.textLines.length).fill(d);
                        })
                        .enter()
                        .append('text')
                        .attr('text-anchor', 'middle')
                        .append('textPath')
                        .attr('class', 'labelText')
                        .attr('startOffset', '25%')
                        .attr('xlink:href', function (d, i) {
                            return '#arc-label' + d.i + '-' + i;
                        })
                        .text(function (d, i) {
                            return d.data.textLines[d.data.textLines.length - 1 - i];
                        });
                    //show tooltip when  Overall Tab is selected
                    svg.selectAll('.labelarc').filter(function (d) {
                        d3.select(this)
                            .on('mouseover', function (event, d) {
                                if (self.selectedTab === 'Overall') {
                                    d3.select(this).style('cursor', 'pointer');
                                    const arc = event.target.parentElement.lastElementChild.lastElementChild;
                                    let yPosition = arc?.getBoundingClientRect()?.top;
                                    let xPosition = arc?.getBoundingClientRect()?.right;
                                    tooltipForMainCategoryDiv.html('<strong>Maturity Value: M' + getAverageMaturityValue(d.data['maturity']) + '</strong>');
                                    tooltipForMainCategoryDiv.transition()
                                        .duration(500)
                                        .style('opacity', 1)
                                        .style('left', xPosition + window.scrollX - 30 + 'px')
                                        .style('top', yPosition + window.scrollY - 40 + 'px')
                                        .style('display', 'block')
                                }
                            })
                            .on('mouseout', function (event, d) {
                                tooltipForMainCategoryDiv.transition()
                                    .duration(200)
                                    .style('left', 'unset')
                                    .style('top', 'unset')
                                    .style('display', 'none')
                                    .style('opacity', 0);
                            });
                    });

                    svg.selectAll('.labelText')
                        .on('mouseover', function (event, d) {
                            if (self.selectedTab === 'Overall') {
                                d3.select(this).style('cursor', 'pointer');
                                const arc = event.target;
                                const {
                                    top: yPosition,
                                    right: xPosition
                                } = arc?.getBoundingClientRect();
                                tooltipForMainCategoryDiv.html('<strong>Maturity Value: M' + getAverageMaturityValue(d.data['maturity']) + '</strong>');
                                tooltipForMainCategoryDiv.transition()
                                    .duration(500)
                                    .style('opacity', 1)
                                    .style('left', xPosition + window.scrollX + 'px')
                                    .style('top', yPosition + window.scrollY + 50 + 'px')
                                    .style('display', 'block')
                            }
                        })
                        .on('mouseout', function (event, d) {
                            tooltipForMainCategoryDiv.transition()
                                .duration(200)
                                .style('left', 'unset')
                                .style('top', 'unset')
                                .style('display', 'none')
                                .style('opacity', 0);
                        });

                    // Center labels
                    const cg = svg.append('g');
                    const yScale = d3.scaleLinear().domain([-1, root.data.textLines.length]).range([-root.yo * 0.5, root.yo * 0.8]);

                    cg.selectAll('.centerLabelText')
                        .data(root.data.textLines)
                        .enter()
                        .append('text')
                        .attr('x', 0)
                        .attr('y', function (d, i) {
                            return yScale(i) - 100 * i;
                        })
                        .attr('text-anchor', 'middle')
                        .attr('class', 'centerLabelText')
                        .text(function (d) {
                            return d;
                        });

                });
            };

            function getRoot2(data) {
                const root2 = <any>{};

                root2.textLines = data.textLines;
                root2.children = data.children;
                root2.children.forEach(function (kpi) {
                    kpi.children = [];
                    kpi.group = kpi['group'] ? kpi['group'] : 1;
                    kpi.children = appendChild2(kpi, kpi.group, 0);

                    // swap parents and children
                    const flatData = collectNodes(JSON.parse(JSON.stringify(kpi.children)));
                    kpi.children = reverseNodes(JSON.parse(JSON.stringify(flatData.reverse())));
                });
                return root2;
            }

            function reverseNodes(flatData) {
                const nodes = [];
                nodes[0] = flatData.shift();
                delete nodes[0].children;
                delete nodes[0].size;
                function visitNode(collection) {
                    if (collection) {
                        while (flatData.length > 1) {
                            collection.children = [flatData.shift()];
                            delete collection.children[0].size;
                            collection.children[0].children = [{}];
                            visitNode(collection.children[0]);
                        }

                        if (flatData.length === 1) {
                            collection.children = [flatData.shift()];
                            collection.children[0].size = 1;
                            delete collection.children[0].children;
                        }
                    }
                }
                visitNode(nodes[0]);
                return nodes;
            }

            function collectNodes(rootNode) {
                const nodes = [];
                function visitNode(node) {
                    nodes.push(node);
                    if (node.children) {
                        node.children.forEach(visitNode);
                    }
                }
                visitNode(rootNode[0]);
                return nodes;
            }

            function appendChild2(kpi, groupId, j) {

                const child = <any>{};
                child.group = (groupId);
                child.maturity = kpi.maturity;
                child.maturityRange = kpi.maturityRange;
                child.maturityLevelsToolTip = <any>maturityLevelTooltip(kpi);
                child.textLines = kpi.textLines;
                child.kpiDefinition = kpi.kpiDefinition
                child.kpiId = kpi.kpiId
                if (j < kpi.maturity) {
                    child.on = true;
                }
                if (j + 1 < maxBarValue) {
                    child.children = [];
                    kpi.children.push(child);
                    appendChild2(kpi.children[kpi.children.length - 1], groupId, j + 1);
                } else {
                    child.textLines = kpi.textLines;
                    child.maturity = kpi.maturity;
                    child.group = (groupId);
                    child.size = 1;
                    child.maturityLevelsToolTip = <any>maturityLevelTooltip(kpi);
                    child.maturityRange = kpi.maturityRange;
                    kpi.children.push(child);
                }

                return kpi.children;
            }

            return chart;
        }


        function maturityLevelTooltip(maturityLevelData) {
            if (maturityLevelData.maturityRange === undefined) {
                maturityLevelData.maturityRange = ['NA', 'NA', 'NA', 'NA', 'NA'];
            }
            // currently we are using static descriptio  for display tooltip, when descrioption is available in JSON then remove below function 'getMaturityLevelDescriptio
            const textLine = maturityLevelData.textLines && maturityLevelData.textLines.toString().replace(/,/g, ' ');
            let renderDescription =
                '<div class="table-wrap">' +
                '<p style="font-size:18px"> <strong>' + textLine + '</strong>  </p>';
            renderDescription += '<p><strong>Definition : </strong> <br>' + maturityLevelData.kpiDefinition + '</p>'

            renderDescription += '<div class="p-grid justify-content-start maturity-level-header" ><span class="p-col" style="padding-left:0"><strong>Maturity Level :</strong></span>';

            let kpiIdWithMaturityRangePrefixZero = ['kpi82','kpi34','kpi42','kpi16','kpi17','kpi70','kpi72','kpi11','kpi118','kpi63','kpi62','kpi71','kpi65'];
            
            if (maturityLevelData.maturityRange[0].charAt(0) === '-'
                && !kpiIdWithMaturityRangePrefixZero.includes(maturityLevelData['kpiId'])) {
                maturityLevelData.maturityRange[0] = '>= ' + maturityLevelData.maturityRange[0].substring(1);
            }
            if (maturityLevelData.maturityRange[0].charAt(0) === '-'
                && kpiIdWithMaturityRangePrefixZero.includes(maturityLevelData['kpiId'])) {
                maturityLevelData.maturityRange[0] = '0 - ' + maturityLevelData.maturityRange[0].substring(1);
            }

            renderDescription += '<span class="p-col"><strong>M1</strong></br><sub>' + maturityLevelData.maturityRange[0] + '</sub></span>';

            renderDescription += '<span class="p-col"><strong>M2</strong></br><sub>' + maturityLevelData.maturityRange[1] + '</sub></span>';

            renderDescription += '<span class="p-col"><strong>M3</strong></br><sub>' + maturityLevelData.maturityRange[2] + '</sub></span>';

            renderDescription += '<span class="p-col"><strong>M4</strong></br><sub>' + maturityLevelData.maturityRange[3] + '</sub></span>';


            if (maturityLevelData.maturityRange[4].slice(-1) === '-' 
            && !kpiIdWithMaturityRangePrefixZero.includes(maturityLevelData['kpiId'])) {
                maturityLevelData.maturityRange[4] = maturityLevelData.maturityRange[4] + '0';
            }
            if (maturityLevelData.maturityRange[4].slice(-1) === '-' 
            && kpiIdWithMaturityRangePrefixZero.includes(maturityLevelData['kpiId'])) {
                maturityLevelData.maturityRange[4] = maturityLevelData.maturityRange[4].slice(0, -1) + ' >=';
            }

            renderDescription += '<span class="p-col"><strong>M5</strong></br><sub>' + maturityLevelData.maturityRange[4] + '</sub></span>';
            renderDescription += '';
            renderDescription += '</div> <p><strong>Maturity Value:  <span class="tooltip-group-' + maturityLevelData.group + '">M' + maturityLevelData.maturity + '</span></strong> </p></div>';
            return renderDescription;
        }
    }
}
