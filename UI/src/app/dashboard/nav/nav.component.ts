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

import { Component, OnInit } from '@angular/core';
import { SharedService } from '../../services/shared.service';
import { HttpService } from '../../services/http.service';
import { GoogleAnalyticsService } from '../../services/google-analytics.service';
import { GetAuthorizationService } from '../../services/get-authorization.service';
import { HelperService } from 'src/app/services/helper.service';
import { Router } from '@angular/router';
import { first } from 'rxjs/operators';
import { Subscription } from 'rxjs';
import { MessageService } from 'primeng/api';
import { TextEncryptionService } from '../../services/text.encryption.service';
import { NotificationDTO, NotificationResponseDTO } from 'src/app/model/NotificationDTO.model';

@Component({
  selector: 'app-nav',
  templateUrl: './nav.component.html',
  styleUrls: ['./nav.component.css'],
})
export class NavComponent implements OnInit {
  selectedTab = 'mydashboard';
  username: string;
  logoImage: any;
  currentversion: any;
  subscription: Subscription;
  configOthersData;
  selectedProject: any;
  notificationPlaceHolder: NotificationDTO[] = [];
  showNotifications = <boolean>true;
  worker: any;
  showHelp = false;
  isGuest = false;
  kpiConfigData: Object = {};
  kpiListData: any = {};
  showNotificationPanel = false;
  changedBoardName: any;
  displayEditModal: boolean;
  selectedType: string;

  mainTab: string;
  boardNameArr: any[] = [];
  boardId = 1;
  constructor(
    private httpService: HttpService,
    private messageService: MessageService,
    private service: SharedService,
    private router: Router,
    private getAuth: GetAuthorizationService,
    private ga: GoogleAnalyticsService,
    private helper: HelperService,
    private aesEncryption: TextEncryptionService,
  ) {
    const selectedTab = window.location.hash.substring(1);
    this.selectedTab = selectedTab.split('/')[2];
    this.boardId = isNaN(+selectedTab.split('/')[3]) ? this.boardId : +selectedTab.split('/')[3];
    this.service.setSelectedTab(this.selectedTab, this.boardId);
    this.service.onTypeRefresh.subscribe(type => {
      this.selectedTab = this.service.getSelectedTab();
    });

    this.username = localStorage.getItem('user_name');
    /*subscribe logo image from service*/
    this.subscription = this.service.getLogoImage().subscribe((logoImage) => {
      this.getLogoImage();
    });

    this.service.globalDashConfigData.subscribe((globalConfig) => {
      if (globalConfig['others'] && globalConfig['others'].length > 1) {
        this.configOthersData = globalConfig['others'][1]?.kpis;
        this.processKpiConfigData();
      }
    });

    this.renderMessage();

  }

  processKpiConfigData() {
    for (let i = 0; i < this.configOthersData?.length; i++) {
      if (this.configOthersData[i]?.shown === false && this.configOthersData[i]?.isEnabled === true) {
        this.kpiConfigData[this.configOthersData[i]?.kpiId] = this.configOthersData[i]?.shown;
      } else {
        this.kpiConfigData[this.configOthersData[i]?.kpiId] = this.configOthersData[i]?.isEnabled;
      }
    }
  }

  ngOnInit() {
    this.service.changedMainDashboardValueObs.subscribe((data) => {
      this.mainTab = data;
      this.changedBoardName = data;
      if (this.boardNameArr?.length > 0) {
        this.boardNameArr[0].boardName = this.mainTab;
      }
    });
    let authoritiesArr;
    if (localStorage.getItem('authorities')) {
      authoritiesArr = this.aesEncryption.convertText(
        localStorage.getItem('authorities'),
        'decrypt',
      );
    }
    if (authoritiesArr && authoritiesArr.includes('ROLE_GUEST')) {
      this.isGuest = true;
    }

    if (authoritiesArr && authoritiesArr.indexOf('ROLE_SUPERADMIN') != -1) {
      this.showHelp = true;
    } else {
      const projectsAccess = JSON.parse(localStorage.getItem('projectsAccess'));
      this.showHelp =
        projectsAccess &&
        typeof projectsAccess != 'undefined' &&
        projectsAccess.length !== 0;
    }

    this.getLogoImage();
    this.getMatchVersions();

    document.addEventListener(
      'click',
      function(e) {
        const profileChkBox = document.getElementById(
          'profile2',
        ) as HTMLInputElement;
        const profileChkBoxIcon = document.querySelector(
          'i.fa-bars.profileIcon',
        );
        e.stopPropagation();
        if (e.target !== profileChkBox && e.target !== profileChkBoxIcon) {
          if (profileChkBox && profileChkBox !== null) {
            profileChkBox.checked = false;
          }
        }
      },
      false,
    );

    this.subscription = this.service.passEventToNav.subscribe(() => {
      this.renderMessage();
    });

    this.startWorker();
    this.service.selectedTypeObs.subscribe(selectedType => {
      this.selectedType = selectedType;
      this.getKpiOrderedList();
    });
  }

  /*Rendered the logo image */
  getLogoImage() {
    this.httpService
      .getUploadedImage()
      .pipe(first())
      .subscribe((data) => {
        if (data['image']) {
          this.logoImage = 'data:image/png;base64,' + data['image'];
        } else {
          this.logoImage = undefined;
        }
      });
  }

  // call when user is seleting tab
  selectTab(selectedTab, boardId = this.boardId) {
    this.selectedTab = selectedTab === 'Kpi Maturity' ? 'Maturity' : selectedTab;
    this.helper.isKanban = false;
    this.service.setSelectedTab(this.selectedTab, boardId);
    this.service.selectTab(this.selectedTab);
  }

  // logout is clicked  and removing auth token , username
  logout() {
    this.httpService.logout().subscribe((getData) => {
      if (!(getData !== null && getData[0] === 'error')) {
        this.helper.isKanban = false;
        localStorage.removeItem('auth_token');
        localStorage.removeItem('user_name');
        localStorage.removeItem('authorities');
        localStorage.removeItem('projectsAccess');
        if (localStorage.getItem('loginType') === 'AD') {
          localStorage.removeItem('SpeedyPassword');
        }
        // Set blank selectedProject after logged out state
        this.selectedProject = null;
        this.service.setSelectedProject(this.selectedProject);

        this.router.navigate(['./authentication/login']);
      }
    });
  }

  // getting the version details from server
  getMatchVersions() {
    this.httpService.getMatchVersions().subscribe((filterData) => {
      if (filterData && filterData.versionDetailsMap) {
        this.currentversion = filterData.versionDetailsMap.currentVersion;
      }
    });
  }

  renderMessage() {
    this.httpService.getAccessRequestsNotifications().subscribe((response: NotificationResponseDTO) => {
      if (response && response.success) {
        if (response.data?.length) {
          this.notificationPlaceHolder = [...response.data];
          this.showNotificationPanel = (this.notificationPlaceHolder.length && this.notificationPlaceHolder.some(data => data.count > 0));
        }
      } else {
        this.messageService.add({
          severity: 'error',
          summary: 'Error in fetching requests. Please try after some time.',
        });
      }
    });
  }

  messageLink(type: string) {
    if (this.getAuth.checkIfSuperUser() || this.getAuth.checkIfProjectAdmin()) {
      switch (type) {
        case 'Project Access Request':
          this.router.navigate(['/dashboard/Config/Profile/GrantRequests']);
          break;
        case 'User Access Request':
          this.router.navigate(['/dashboard/Config/Profile/GrantNewUserAuthRequests']);
          break;
        default:
          console.log('default case');
      }
    } else {
      this.router.navigate(['/dashboard/Config/Profile/RequestStatus']);
    }
  }

  hideNotifications() {
    this.showNotifications = false;
  }

  startWorker() {
    if (typeof Worker !== 'undefined') {
      // Create a new
      this.worker = new Worker('../../app.worker', { type: 'module' });
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
      this.httpService.getShowHideKpi().subscribe(
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
    }else{
      this.processKPIListData();
    }

  }


  processKPIListData(){
    this.configOthersData = this.kpiListData['others'][0]?.kpis;
    this.service.setDashConfigData(this.kpiListData);
    this.boardNameArr = [];
    if (this.kpiListData[this.selectedType] && Array.isArray(this.kpiListData[this.selectedType])) {
      for (let i = 0; i < this.kpiListData[this.selectedType]?.length; i++) {
        this.boardNameArr.push(
          {
            boardName: this.kpiListData[this.selectedType][i].boardName,
            link: this.kpiListData[this.selectedType][i].boardName.toLowerCase().split(' ').join('-') + '/' + this.kpiListData[this.selectedType][i].boardId,
            boardId: this.kpiListData[this.selectedType][i].boardId
          });
      }
    }

    for (let i = 0; i < this.kpiListData['others']?.length; i++) {
      this.boardNameArr.push(
        {
          boardName: this.kpiListData['others'][i].boardName,
          link: this.kpiListData['others'][i].boardName.toLowerCase() + '/' + this.kpiListData[this.selectedType][i].boardId
        });
    }
    this.service.changedMainDashboardValueSub.next(
      this.kpiListData?.scrum[0]?.boardName,
    );
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

  assignUserNameForKpiData() {
    if (!this.kpiListData['username']) {
      delete this.kpiListData['id'];
    }
    this.kpiListData['username'] = localStorage.getItem('user_name');
  }

  editDashboardName() {
    this.kpiListData.scrum[0].boardName = this.changedBoardName;
    this.kpiListData.kanban[0].boardName = this.changedBoardName;
    this.assignUserNameForKpiData();
    this.httpService.updateUserBoardConfig(this.kpiListData).subscribe(
      (data) => {
        if (data.success) {
          console.log('Data save successfully');
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
        console.log(error);
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
