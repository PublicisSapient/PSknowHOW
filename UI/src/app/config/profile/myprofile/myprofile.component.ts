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
import { UntypedFormGroup, Validators, UntypedFormBuilder } from '@angular/forms';
import { GetAuthorizationService } from '../../../services/get-authorization.service';
import { HttpService } from '../../../services/http.service';
import { ProfileComponent } from '../profile.component';
import { SharedService } from 'src/app/services/shared.service';
import { environment } from 'src/environments/environment';
import { MessageService } from 'primeng/api';
@Component({
  selector: 'app-myprofile',
  templateUrl: './myprofile.component.html',
  styleUrls: ['./myprofile.component.css', '../profile.component.css']
})
export class MyprofileComponent implements OnInit {
  isSuperAdmin = false;
  isProjectAdmin = false;
  emailSubmitted = false;
  emailConfigured = false;
  userEmailForm: UntypedFormGroup;
  userName: string
  authorities = this.sharedService.getCurrentUserDetails('authorities');
  notificationEmailForm: UntypedFormGroup;
  userRole = this.authorities?.length ? this.authorities.join(',') : '--';
  userEmail: string
  userEmailConfigured = false;
  message: string;
  dataLoading = false;
  noAccess = false;
  roleBasedProjectList = [];
  dynamicCols: Array<any> = [];
  ssoLogin = environment.SSO_LOGIN;
  loginType: string = '';
  constructor(private formBuilder: UntypedFormBuilder, private getAuthorizationService: GetAuthorizationService, private http: HttpService, private profile: ProfileComponent,
    public sharedService: SharedService , private messageService: MessageService) { }



/**
 * Initializes the component by checking user roles, setting access permissions,
 * and configuring forms for user email and notification preferences.
 * 
 * @returns {void} - No return value.
 */
  ngOnInit() {
    if (this.getAuthorizationService.checkIfSuperUser()) {
      // logged in as SuperAdmin
      this.isSuperAdmin = true;
    }
    if (this.getAuthorizationService.checkIfProjectAdmin()) {
      // logged in as projectAdmin
      this.isProjectAdmin = true;
    }

    if ((!this.isSuperAdmin) && (!this.sharedService.getCurrentUserDetails('projectsAccess')?.length)) {
      this.noAccess = true;
    }

    // this.sharedService.currentUserDetailsObs.subscribe(details => {
    //   this.setUserDetails(details);
    // })
    this.setUserDetails(this.sharedService.getCurrentUserDetails());
    if (this.sharedService.getCurrentUserDetails('projectsAccess')?.length) {
      const accessList = JSON.parse(JSON.stringify(this.sharedService.getCurrentUserDetails('projectsAccess')));
      this.groupProjects(accessList);
      this.getTableHeadings();
    }


    this.userEmailForm = this.formBuilder.group({
      email: ['', [Validators.required, Validators.pattern('[a-z0-9._%+-]+@[a-z0-9.-]+\.[a-z]{2,3}$')]],
      confirmEmail: ['', [Validators.required, Validators.pattern('[a-z0-9._%+-]+@[a-z0-9.-]+\.[a-z]{2,3}$')]]
    }, { validator: this.checkConfirmEmail });
    this.loginType = this.sharedService.getCurrentUserDetails('authType');

    this.notificationEmailForm = this.formBuilder.group({
      "accessAlertNotification": [this.sharedService.getCurrentUserDetails('notificationEmail')?.accessAlertNotification || false],
      "errorAlertNotification": [this.sharedService.getCurrentUserDetails('notificationEmail')?.errorAlertNotification || false]
    })
  }

  setUserDetails(details) {
    if (details) {
      this.userName = details['user_name'] ? details['user_name'] : '--';
      if (details['user_email']) {
        this.userEmail = details['user_email']
        this.emailConfigured = true;
      } else {
        this.userEmail = '--';
      }
    }
  }

  getTableHeadings() {
    let cols = JSON.parse(localStorage.getItem('hierarchyData'));
    if (!cols) {
      const tempCols = JSON.parse(localStorage.getItem('completeHierarchyData'))?.['scrum'];
      let projectLevel = tempCols?.filter((item) => item.hierarchyLevelId?.toLowerCase() === 'project')?.[0]?.level;
      cols = tempCols.filter((item) => item.level < projectLevel);
    }
    cols?.forEach((x) => {
      const obj = {
        id: x.hierarchyLevelId,
        name: x.hierarchyLevelName
      };
      this.dynamicCols?.push(obj);
    });
    this.dynamicCols.push({ id: 'projectName', name: 'Projects' });
  }

  groupProjects(inArr) {
    for (let k = 0; k < inArr?.length; k++) {
      const projectsArr = [];
      for (let i = 0; i < inArr[k]?.projects?.length; i++) {
        const obj = {
          role: inArr[k]?.role,
          projectName: inArr[k]?.projects[i]?.projectName,
          projectId: inArr[k]?.projects[i]?.projectId
        };
        const hierarchyArr = inArr[k]?.projects[i]?.hierarchy;
        for (let j = 0; j < hierarchyArr?.length; j++) {
          obj[hierarchyArr[j].hierarchyLevel.hierarchyLevelId] = hierarchyArr[j]?.value;
        }
        projectsArr?.push(obj);
      }
      this.roleBasedProjectList = [...this.roleBasedProjectList, ...projectsArr];
    }
  }

  // Validation for confirm-email
  checkConfirmEmail(group: UntypedFormGroup) {
    const email = group.controls.email.value;
    const confirmEmail = group.controls.confirmEmail.value;
    return email === confirmEmail ? null : { notSame: true };
  }

  // convenience getter for easy access to form fields
  get getEmailForm() {
    return this.userEmailForm.controls;
  }


  toggleNotificationEmail(event: any, toggleField: string) {
    const updatedFlag = event.checked;
    this.notificationEmailForm[toggleField] = updatedFlag;
    let obj = {};
    for(let key in this.notificationEmailForm.value){
      obj[key] = this.notificationEmailForm.value[key]
    }
    //call http service
    this.http.notificationEmailToggleChange(obj)
      .subscribe(
        response => {
          if (response?.['success'] && response['data']) {
            const userDetails = response['data'];
            this.messageService.add({ severity: 'success', summary: response['message'] });
            this.http.setCurrentUserDetails({
              notificationEmail: userDetails['notificationEmail'],
            });
          } else if (response && !response['success']) {
            if (response['message']) {
              this.messageService.add({ severity: 'error', summary: response['message'] });
            }
          }
        }
      );
  }

}
