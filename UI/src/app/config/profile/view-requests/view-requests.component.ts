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
import { HttpService } from '../../../services/http.service';
import { MessageService } from 'primeng/api';
import { SharedService } from '../../../services/shared.service';
import { GetAuthorizationService } from 'src/app/services/get-authorization.service';

@Component({
  selector: 'app-view-requests',
  templateUrl: './view-requests.component.html',
  styleUrls: ['./view-requests.component.css', '../profile.component.css']
})
export class ViewRequestsComponent implements OnInit {
  accessRequestsRequest = <any>'';
  accessRequestData = {};
  acceptRequestData = {};
  accessRequestList = <any>[];
  rolesRequest = <any>'';
  rolesData: any;
  roleList: any;
  dataLoading = <any>[];
  constructor(private httpService: HttpService, private messageService: MessageService, private sharedService: SharedService, private authService: GetAuthorizationService) {

  }

  ngOnInit() {
    this.getRequests();
    this.getRolesList();
  }

  getRequests() {
    this.accessRequestsRequest = this.httpService.getAccessRequests('Pending')
      .subscribe(requests => {
        this.accessRequestData = requests;
        if (this.accessRequestData['success']) {
          this.accessRequestList = requests.data;
        } else {
          this.messageService.add({ severity: 'error', summary: 'Error in fetching requests. Please try after some time.' });
        }
        this.dataLoading.push('allRequests');
      });
  }

  getRolesList() {
    this.rolesRequest = this.httpService.getRolesList()
      .subscribe(roles => {
        this.rolesData = roles;
        if (this.rolesData['success']) {
          this.roleList = roles.data.map((role) => ({
              label: role.roleName,
              value: role.roleName
            }));
          if(this.authService.checkIfProjectAdmin()) {
            this.roleList = this.roleList.filter((role) => role.value !== 'ROLE_SUPERADMIN');
          }
        } else {
          // show error message
          this.messageService.add({ severity: 'error', summary: 'Error in fetching roles. Please try after some time.' });
        }
        this.dataLoading.push('allRoles');
      });
  }

  approveRejectRequest(requestData, approved) {
    const obj = {};
    approved ? obj['status'] = 'Approved' : obj['status'] = 'Rejected';
    obj['role'] = requestData.role;
    obj['message'] = requestData.reviewComments;

    if (requestData.role !== 'ROLE_SUPERADMIN' && (!requestData.accessNode || !requestData.accessNode.accessItems || !requestData.accessNode.accessItems.length)) {
      this.messageService.add({ severity: 'error', summary: 'You cannot modify the role for SUPERADMIN requests as there is no project. You can only accept or reject this request.' });
    } else {
      this.accessRequestsRequest = this.httpService.updateAccessRequest(obj, requestData['id'])
        .subscribe(requests => {
          this.acceptRequestData = requests;
          if (this.acceptRequestData['success']) {
            this.messageService.add({ severity: 'success', summary: `Request ${obj['status']}`, detail: '' });
            this.getRequests();
            this.sharedService.notificationUpdate();
          } else {
            this.messageService.add({ severity: 'error', summary: 'Error in updating request. Please try after some time.' });
          }
        });
    }
  }
}
