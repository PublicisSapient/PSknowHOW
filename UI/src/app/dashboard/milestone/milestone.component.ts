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
import { Component, OnInit, OnDestroy, Input, ViewChild } from '@angular/core';
import { HttpService } from '../../services/http.service';
import { ExcelService } from '../../services/excel.service';
import { SharedService } from '../../services/shared.service';
import { HelperService } from '../../services/helper.service';
import { CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';
import { ExportExcelComponent } from 'src/app/component/export-excel/export-excel.component';

declare let require: any;

 @Component({
  selector: 'app-milestone',
  templateUrl: './milestone.component.html',
  styleUrls: ['./milestone.component.css']
})
export class MilestoneComponent implements OnInit {
  @ViewChild('exportExcel') exportExcelComponent: ExportExcelComponent;
  subscriptions: any[] = [];
  masterData = <any>{};
  filterData = <any>[];
  filterApplyData = <any>{};
  noOfFilterSelected = 0;
  selectedtype = '';
  configGlobalData;
  kpiJira = <any>{};
  loaderJiraArray = [];
  jiraKpiRequest = <any>'';
  jiraKpiData = <any>{};
  kpiConfigData: Object = {};
  noKpis = false;
  enableByUser = false;
  noRelease = false;
  colorObj: object = {};
  updatedConfigGlobalData;
  upDatedConfigData;
  noProjects = false;
  timeRemaining = 0;

  constructor(private service: SharedService, private httpService: HttpService, private excelService: ExcelService, private helperService: HelperService) {
    
    /** When filter dropdown change */
    this.subscriptions.push(this.service.passDataToDashboard.subscribe((sharedobject) => {
      if (sharedobject?.filterData?.length && sharedobject.selectedTab.toLowerCase() === 'milestone') {
        this.receiveSharedData(sharedobject);
      }
    }));

    /** When release not found for any project */
    this.subscriptions.push(this.service.noReleaseObs.subscribe((res) => {
      this.noRelease = res;
    }));

    /** When click on show/Hide button on filter component */
    this.subscriptions.push(this.service.globalDashConfigData.subscribe((globalConfig) => {
      if(globalConfig){
        this.configGlobalData = globalConfig['others'].filter((item) => item.boardName.toLowerCase() == 'milestone')[0]?.kpis;
        this.processKpiConfigData();
      }
    }));

    this.subscriptions.push(this.service.noProjectsObs.subscribe((res) => {
      this.noProjects = res;
    }));
  }

  processKpiConfigData() {
    const disabledKpis = this.configGlobalData.filter(item => item.shown && !item.isEnabled);
     /** user can enable kpis from show/hide filter, added below flag to show different message to the user **/
    this.enableByUser = disabledKpis?.length ? true : false;
    /** noKpis - if true, all kpis are not shown to the user (not showing kpis to the user) **/
    this.updatedConfigGlobalData = this.configGlobalData.filter(item => item.shown && item.isEnabled);
    this.upDatedConfigData = this.updatedConfigGlobalData.filter(kpi => kpi.kpiId !== 'kpi121');
    if (this.upDatedConfigData?.length === 0) {
      this.noKpis = true;
    } else {
      this.noKpis = false;
    }
    this.configGlobalData.forEach(element => {
      if (element.shown && element.isEnabled) {
        this.kpiConfigData[element.kpiId] = true;
      } else {
        this.kpiConfigData[element.kpiId] = false;
      }
    });
  }

  getSelectedType(sharedobject) {
    this.selectedtype = sharedobject;
  }

  /**
    Used to receive all filter data from filter component when user
    click apply and call kpi
   **/
  receiveSharedData($event) {
    if(this.service.getDashConfigData()){
      this.configGlobalData = this.service.getDashConfigData()['others']?.filter((item) => item.boardName.toLowerCase() == 'milestone')[0]?.kpis;
      this.processKpiConfigData();
      this.masterData = $event.masterData;
      this.filterData = $event.filterData;
      this.filterApplyData = $event.filterApplyData;
      this.noOfFilterSelected = Object.keys(this.filterApplyData).length;
      if (this.filterData?.length) {
        /**  call kpi request according to tab selected */
        if (this.masterData && Object.keys(this.masterData).length) {
          if (this.selectedtype !== 'Kanban') {
            const kpiIdsForCurrentBoard = this.configGlobalData?.map(kpiDetails => kpiDetails.kpiId);
            const selectedRelease = this.filterData?.filter(x => x.nodeId == this.filterApplyData?.selectedMap['release'][0] && x.labelName.toLowerCase() === 'release')[0];
            const today = new Date().toISOString().split('T')[0];
            const endDate = new Date(selectedRelease?.releaseEndDate).toISOString().split('T')[0];
            this.timeRemaining = this.calcBusinessDays(today, endDate);
            this.service.iterationCongifData.next({daysLeft: this.timeRemaining});
            this.groupJiraKpi(kpiIdsForCurrentBoard);
          }
        }
      } 
    }

  }

  /**  Used for grouping all Sonar kpi from master data and calling Sonar kpi.(only for scrum) */
  groupJiraKpi(kpiIdsForCurrentBoard) {
    this.jiraKpiData = {};
   /** creating a set of unique group Ids */
    const groupIdSet = new Set();
    this.masterData.kpiList.forEach((obj) => {
      if (!obj.kanban && obj.kpiSource === 'Jira' && obj.kpiCategory == 'Milestone') {
        groupIdSet.add(obj.groupId);
      }
    });

  /** sending requests after grouping the the KPIs according to group Id */
    groupIdSet.forEach((groupId) => {
      if (groupId) {
        this.kpiJira = this.helperService.groupKpiFromMaster('Jira', false, this.masterData, this.filterApplyData, this.filterData, kpiIdsForCurrentBoard, groupId);
        this.postJiraKpi(this.kpiJira, 'jira');
      }
    });

  }


  /** post request of Jira(scrum) hygiene  */
  postJiraKpi(postData, source): void {
    postData.kpiList.forEach(element => {
      this.loaderJiraArray.push(element.kpiId);
    });
    this.jiraKpiRequest = this.httpService.postKpi(postData, source)
      .subscribe(getData => {
        if (getData !== null && getData[0] !== 'error' && !getData['error']) {
          /** creating array into object where key is kpi id */
          const localVariable = this.helperService.createKpiWiseId(getData);
          for (const kpi in localVariable) {
            this.loaderJiraArray.splice(this.loaderJiraArray.indexOf(kpi), 1);
          }
          this.jiraKpiData = Object.assign({}, this.jiraKpiData, localVariable);
         
        } else {
          this.jiraKpiData = getData;
          postData.kpiList.forEach(element => {
            this.loaderJiraArray.splice(this.loaderJiraArray.indexOf(element.kpiId), 1);
          });
        }

       
      });
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


  ngOnInit() {
    this.selectedtype = this.service.getSelectedType();
    if (this.service.getFilterObject()) {
      this.receiveSharedData(this.service.getFilterObject());
    }
  }



   /** unsubscribing all Kpi Request  */
  ngOnDestroy() {
    this.subscriptions.forEach(subscription => subscription.unsubscribe());
  }
}