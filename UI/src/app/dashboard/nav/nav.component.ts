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

import { Component, OnInit} from '@angular/core';
import { SharedService } from '../../services/shared.service';
import { HttpService } from '../../services/http.service';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { MessageService } from 'primeng/api';
import { environment } from 'src/environments/environment';

@Component({
  selector: 'app-nav',
  templateUrl: './nav.component.html',
  styleUrls: ['./nav.component.css'],
})
export class NavComponent implements OnInit {
  selectedTab;
  subscription: Subscription;
  configOthersData;
  kpiConfigData = {};
  kpiListData: any = {};
  changedBoardName: any;
  displayEditModal: boolean;
  selectedType: string;
  mainTab: string;
  boardNameArr: any[] = [];
  boardId = 1;
  ssoLogin= environment.SSO_LOGIN;
  visibleSidebar;
  kanban = false;
  navItems: number = 7;

  constructor(
    private httpService: HttpService,
    private messageService: MessageService,
    public service: SharedService,
    public router: Router,
  ) {
    this.selectedType = this.service.getSelectedType() ? this.service.getSelectedType() : 'scrum';
    this.kanban= this.selectedType.toLowerCase() === 'scrum' ? false : true;
    const selectedTab = window.location.hash.substring(1);

    this.selectedTab = selectedTab?.split('/')[2] ? selectedTab?.split('/')[2] :'iteration' ;
    this.selectedTab = this.selectedTab?.split(' ').join('-').toLowerCase();
    // if(this.selectedTab.includes('-')){
    //   this.selectedTab = this.selectedTab.split('-').join(' ');
    // }
    if(this.selectedTab.includes('?')){
      this.selectedTab = this.selectedTab.split('?')[0];
    }
    if(this.selectedTab !== 'unauthorized access'){
      this.service.setSelectedTypeOrTabRefresh(this.selectedTab,this.selectedType);
    }

    this.service.onTypeOrTabRefresh.subscribe(data => {
      this.selectedTab = data?.selectedTab;
    })

    this.service.boardNamesListSubject.subscribe((data) => {
      this.boardNameArr = data;
    })

  }

  ngOnInit() {
    this.service.visibleSideBarObs.subscribe(value =>{
      this.visibleSidebar = value;
    });
    this.service.setSideNav(false);
    this.service.changedMainDashboardValueObs.subscribe((data) => {
      this.mainTab = data;
      this.changedBoardName = data;
      if (this.boardNameArr?.length > 0) {
        this.boardNameArr[0].boardName = this.mainTab;
      }
    });
    this.getKpiOrderedList();
  }

  // call when user is seleting tab
  selectTab(selectedTab) {
    this.selectedTab = this.boardNameArr?.filter(board => board.link === selectedTab)[0]?.boardName;
    if((selectedTab.toLowerCase() === 'iteration' || selectedTab.toLowerCase() === 'backlog' || selectedTab.toLowerCase() === 'release' || selectedTab.toLowerCase() === 'dora') && this.selectedType.toLowerCase() !== 'scrum'){
      this.selectedType = 'Scrum';
    }
    this.setSelectedType(this.selectedType);
  }

  setSelectedType(type) {
    this.selectedType = type?.toLowerCase();
    this.service.setSelectedTypeOrTabRefresh(this.selectedTab,this.selectedType);
    if (type.toLowerCase() === 'kanban') {
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

  getKpiOrderedList() {
    this.kpiListData = this.service.getDashConfigData();
    if (!this.kpiListData || !Object.keys(this.kpiListData).length) {
      this.httpService.getShowHideOnDashboard({basicProjectConfigIds : []}).subscribe(
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
    this.configOthersData = this.kpiListData['others'].find(boardDetails => boardDetails.boardName === 'KPI Maturity')?.kpis;
    this.service.setUpdatedBoardList(this.kpiListData, this.selectedType);

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
    this.httpService.submitShowHideOnDashboard(this.kpiListData).subscribe(
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

  setVisibleSideBar(val){
    this.service.setVisibleSideBar(val);
  }

}
