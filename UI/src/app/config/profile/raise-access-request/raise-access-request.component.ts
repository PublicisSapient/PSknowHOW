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

import { Component, OnInit, AfterViewChecked } from '@angular/core';
import { UntypedFormGroup } from '@angular/forms';
import { HttpService } from '../../../services/http.service';
import { MessageService } from 'primeng/api';
import { Router } from '@angular/router';
import { SharedService } from 'src/app/services/shared.service';

declare let $: any;

@Component({
  selector: 'app-profile-mgmt',
  templateUrl: './raise-access-request.component.html',
  styleUrls: ['./raise-access-request.component.css', '../profile.component.css']
})
export class RaiseAccessRequestComponent implements OnInit {
  requestForm: UntypedFormGroup;
  filterKpiRequest = <any>'';
  rolesRequest = <any>'';
  accessRequest = <any>'';
  selectedProject: any;
  kanban = false;
  rolesData = <any>{};
  roleList = <any>[];
  requestData = {};
  raiseRequestResponse = {};
  roleSelected = false;
  constructor(private httpService: HttpService, private messageService: MessageService, private router: Router,private sharedService : SharedService) { }

  ngOnInit() {
    this.getRolesList();
    this.sharedService.currentUserDetailsObs.subscribe(details => {
      if (details) {
        this.requestData['username'] = details['user_name'];
      }
    });
    this.requestData['status'] = 'Pending';
    this.requestData['reviewComments'] = '';
    this.requestData['role'] = '';
    this.requestData['accessNode'] = {
      accessLevel: '',
      accessItems: []
    };
  }

  // sets the selected role as "active"
  selectRole(item, itemList) {
    itemList.forEach(element => {
      element.active = false;
    });
    item.active = true;
    this.requestData['role'] = item.roleName;
    this.roleSelected = true;
  }

  // fetches the roles list
  getRolesList() {
    this.rolesRequest = this.httpService.getRolesList()
      .subscribe(roles => {
        this.rolesData = roles;
        if (this.rolesData['success']) {
          this.roleList = roles.data;
        } else {
          // show error message
          this.messageService.add({ severity: 'error', summary: 'Error in fetching roles. Please try after some time.' });
        }
      });
  }

  // called on the click of "Submit" button
  submitRequest() {
    this.accessRequest = this.httpService.saveAccessRequest(this.requestData)
      .subscribe(request => {
        this.raiseRequestResponse = request;
        if (this.raiseRequestResponse['success']) {
          // clear selections
          this.roleSelected = false;
          this.roleList.forEach(element => {
            element.active = false;
          });

          if (this.raiseRequestResponse['data'] && this.raiseRequestResponse['data'].status.toLowerCase() == 'approved') {
            this.messageService.add({ severity: 'success', summary: 'Request has been auto-approved.', detail: '' });
          } else {
            this.messageService.add({ severity: 'success', summary: 'Request submitted.', detail: '' });
          }
        } else {
          this.messageService.add({ severity: 'error', summary: request.message });
          this.roleSelected = false;
          this.roleList.forEach(element => {
            element.active = false;
          });
        }
      });

  }

  projectSelectedEvent(accessItem): void {
    if (accessItem && accessItem.value && accessItem.value.length) {
      this.roleList.filter((role) => role.roleName === 'ROLE_SUPERADMIN')[0].disabled = true;
      this.roleList.forEach(element => {
        element.active = false;
      });
      this.roleSelected = false;
      this.requestData['role'] = '';
      this.requestData['accessNode'] = {
        accessLevel: accessItem.accessType
      };

        this.requestData['accessNode']['accessItems'] = accessItem.value.map((item) => ({
            itemId: item.itemId,
            itemName: item.itemName
          }));

    } else {
      this.requestData['accessNode'] = {};
      this.roleList.filter((role) => role.roleName === 'ROLE_SUPERADMIN')[0].disabled = false;
    }
  }

  // logout is clicked  and removing auth token , username
  logout() {
    this.httpService.logout()
      .subscribe(getData => {
        if (!(getData !== null && getData[0] === 'error')) {
          localStorage.removeItem('auth_token');
          this.sharedService.setCurrentUserDetails({});

          this.router.navigate(['./authentication/login']);
        }
      });
  }
}
