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

import { Component, OnDestroy, OnInit } from '@angular/core';
import { HttpService } from '../../../services/http.service';
import { SharedService } from '../../../services/shared.service';
import { MessageService } from 'primeng/api';
import { ConfirmationService } from 'primeng/api';

@Component({
  selector: 'app-request-status',
  templateUrl: './request-status.component.html',
  styleUrls: ['./request-status.component.css', '../profile.component.css', '../view-requests/view-requests.component.css']
})
export class RequestStatusComponent implements OnInit, OnDestroy {
  requestStatusRequest = <any>'';
  requestStatusData = {};
  requestStatusList = [];
  dataLoading = <boolean>false;
  userName : string;
  subscriptions = [];
  constructor(private httpService: HttpService, private messageService: MessageService, private confirmationService: ConfirmationService, private sharedService: SharedService) { }

  ngOnInit() {
   
    this.subscriptions.push(this.sharedService.currentUserDetailsObs.subscribe(details=>{
      if(details && details['user_name']){
        this.userName = details['user_name'];
        this.getRequests();
      }
    }))
  }

  getRequests() {
    this.dataLoading = true;
    this.requestStatusRequest = this.httpService.getUserAccessRequests(this.userName)
      .subscribe(requests => {
        this.dataLoading = false;
        this.requestStatusData = requests;
        if (this.requestStatusData['success']) {
          this.requestStatusList = this.requestStatusData['data'].sort(function(a, b) {
            return +(new Date(b.createdDate)) - +(new Date(a.createdDate));
          });
        } else {
          this.messageService.add({ severity: 'error', summary: 'Error in fetching requests. Please try after some time.' });
        }
      });
  }

  recallRequest(requestId, event) {
    this.confirmationService.confirm({
      target: event.target,
      message: 'Are you sure you want to proceed?',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.dataLoading = true;
        this.httpService.deleteAccessRequest(requestId).subscribe((response) => {
          if (response && response['success']) {
            this.requestStatusList = this.requestStatusList.filter(request => request.id !== requestId);
            this.dataLoading = false;
            this.sharedService.notificationUpdate();
          } else {
            this.messageService.add({ severity: 'error', summary: 'Error in recalling request. Please try after some time.' });
          }
        });
      },
      reject: () => {
        console.log('reject')
       }
    });
  }

  ngOnDestroy(){
    this.subscriptions.forEach(subscription => subscription.unsubscribe());
  }
}
