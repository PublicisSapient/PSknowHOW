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

import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import { SharedService } from '../../services/shared.service';
import { HttpService } from '../../services/http.service';
import { GoogleAnalyticsService } from '../../services/google-analytics.service';
import { HelperService } from 'src/app/services/helper.service';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { MessageService } from 'primeng/api';
import { TextEncryptionService } from '../../services/text.encryption.service';
import { delay } from 'rxjs/operators';

@Component({
  selector: 'app-nav',
  templateUrl: './nav.component.html',
  styleUrls: ['./nav.component.css'],
})
export class NavComponent implements OnInit {
  selectedTab;
  subscription: Subscription;
  configOthersData;
  worker: any;
  kpiConfigData = {};
  kpiListData: any = {};
  changedBoardName: any;
  displayEditModal: boolean;
  selectedType: string;
  mainTab: string;
  boardNameArr: any[] = [];
  boardId = 1;
  visibleSidebar = true;
  kanban = false;
  constructor(
    private httpService: HttpService,
    private messageService: MessageService,
    public service: SharedService,
    public router: Router,
    private ga: GoogleAnalyticsService,
    private helper: HelperService,
    private aesEncryption: TextEncryptionService,
  ) {
    this.service.setSelectedType('Scrum'); //sednig selected tab to service layer
    this.selectedType ='scrum';
    const selectedTab = window.location.hash.substring(1);
    this.selectedTab = selectedTab?.split('/')[2] ? selectedTab?.split('/')[2] :'iteration' ;
    if(this.selectedTab.includes('-')){
      this.selectedTab = this.selectedTab.split('-').join(' ');
    }
    // this.boardId = isNaN(+selectedTab?.split('/')[3])
    //   ? this.boardId
    //   : +selectedTab.split('/')[3];
    this.service.setSelectedTab(this.selectedTab);
  }



  ngOnInit() {
    this.service.setSideNav(true);
    this.service.changedMainDashboardValueObs.subscribe((data) => {
      this.mainTab = data;
      this.changedBoardName = data;
      if (this.boardNameArr?.length > 0) {
        this.boardNameArr[0].boardName = this.mainTab;
      }
    });

    this.startWorker();
    this.getKpiOrderedList();
  }

  // call when user is seleting tab
  selectTab(selectedTab) {
    this.selectedTab = selectedTab === 'Kpi Maturity' ? 'Maturity' : selectedTab;
    if(selectedTab.toLowerCase() === 'iteration' || selectedTab.toLowerCase() === 'backlog'){
      this.setSelectedType('Scrum');
    }
    this.service.setSelectedTab(this.selectedTab);
  }

  setSelectedType(type) {
    this.selectedType = type?.toLowerCase();
    this.service.setSelectedType(type);
    if (type === 'Kanban') {
      this.kanban = true;
    } else {
      this.kanban = false;
    }
    this.getKpiOrderedList();
  }

  processKpiConfigData() {
    for (let i = 0; i < this.configOthersData?.length; i++) {
      if (
        this.configOthersData[i]?.shown === false &&
        this.configOthersData[i]?.isEnabled === true
      ) {
        this.kpiConfigData[this.configOthersData[i]?.kpiId] =
          this.configOthersData[i]?.shown;
      } else {
        this.kpiConfigData[this.configOthersData[i]?.kpiId] =
          this.configOthersData[i]?.isEnabled;
      }
    }
  }

  startWorker() {
    if (typeof Worker !== 'undefined') {
      // Create a new
      this.worker = new Worker(new URL('../../app.worker',import.meta.url), { type: 'module' });
      this.worker.onmessage = ({ data }) => {
        this.ga.setProjectList(data);
        this.stopWorker();
      };
      this.worker.postMessage(localStorage.getItem('auth_token'));
    } else {
      // Web Workers are not supported in this environment.
      // You should add a fallback so that your program still executes correctly.
      console.log('Web workers not supported!!!');
    }
  }

  stopWorker() {
    this.worker.terminate();
    this.worker = undefined;
  }

  getKpiOrderedList() {
    this.kpiListData = this.service.getDashConfigData();
    if (!this.kpiListData || !Object.keys(this.kpiListData).length) {
      this.httpService.getShowHideKpi().pipe(delay(4000)).subscribe(
        (response) => {
          if (response.success === true) {
            this.kpiListData = response.data;
            this.processKPIListData();
          }
        },
        (error) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Error in fetching roles. Please try after some time.',
          });
        },
      );
    } else {
      this.processKPIListData();
    }
  }

  processKPIListData() {
    this.configOthersData = this.kpiListData['others'].find(boardDetails => boardDetails.boardName === 'Kpi Maturity')?.kpis;
    this.boardNameArr = [];
    if (
      this.kpiListData[this.selectedType] &&
      Array.isArray(this.kpiListData[this.selectedType])
    ) {
      for (let i = 0; i < this.kpiListData[this.selectedType]?.length; i++) {
        this.boardNameArr.push({
          boardName: this.kpiListData[this.selectedType][i].boardName,
          link: this.kpiListData[this.selectedType][i].boardName.toLowerCase().split(' ').join('-')
        });
      }
    }

    for (let i = 0; i < this.kpiListData['others']?.length; i++) {
      this.boardNameArr.push({
        boardName: this.kpiListData['others'][i].boardName,
        link:
          this.kpiListData['others'][i].boardName.toLowerCase()
      });
    }
    // renamed tab name was not updating when navigating on iteration/backlog, issue fixed
    if (this.changedBoardName) {
      this.service.changedMainDashboardValueSub.next(this.changedBoardName);
    } else {
      this.service.changedMainDashboardValueSub.next(
        this.kpiListData?.scrum[0]?.boardName,
      );
    }

    this.processKpiConfigData();
  }

  isEmptyObject(value) {
    return Object.keys(value).length === 0 && value.constructor === Object;
  }

  openEditModal() {
    this.service.changedMainDashboardValueObs.subscribe((data) => {
      this.changedBoardName = data;
    });
    this.displayEditModal = true;
  }

  editDashboardName() {
    this.kpiListData.scrum[0].boardName = this.changedBoardName;
    this.kpiListData.kanban[0].boardName = this.changedBoardName;
    this.service.setDashConfigData(this.kpiListData);
    this.selectTab(this.changedBoardName);
    this.httpService.updateUserBoardConfig(this.kpiListData).subscribe(
      (data) => {
        if (data.success) {
          this.messageService.add({
            severity: 'success',
            summary: `Board name changed successfully to ${this.changedBoardName}`,
          });
          this.displayEditModal = false;
          this.service.changedMainDashboardValueSub.next(this.changedBoardName);
        } else {
          this.messageService.add({
            severity: 'error',
            summary: 'Something went wrong, Please try again',
          });
        }
      },
      (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Something went wrong, Please try again.',
        });
      },
    );
  }

  closeEditModal() {
    this.displayEditModal = false;
  }

}
