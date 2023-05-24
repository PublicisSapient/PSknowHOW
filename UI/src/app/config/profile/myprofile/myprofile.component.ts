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
  userName : string 
  authorities = this.sharedService.getCurrentUserDetails('authorities');


  userRole = this.authorities?.length ? this.authorities.join(',') : '--';
  userEmail : string
  userEmailConfigured = false;
  message: string;
  dataLoading = false;
  noAccess = false;
  roleBasedProjectList = [];
  adLogin = false;
  dynamicCols: Array<any> = [];
  ssoLogin = environment.SSO_LOGIN;
  constructor(private formBuilder: UntypedFormBuilder, private getAuthorizationService: GetAuthorizationService, private http: HttpService, private profile: ProfileComponent,
    private sharedService : SharedService) { }



  ngOnInit() {
    if (this.getAuthorizationService.checkIfSuperUser()) {
      // logged in as SuperAdmin
      this.isSuperAdmin = true;
    }
    if(this.getAuthorizationService.checkIfProjectAdmin()) {
      // logged in as projectAdmin
      this.isProjectAdmin = true;
    }

    if ((!this.isSuperAdmin) && (this.sharedService.getCurrentUserDetails('projectsAccess') === 'undefined' || !this.sharedService.getCurrentUserDetails('projectsAccess').length)) {
      this.noAccess = true;
    }
   
    this.sharedService.currentUserDetailsObs.subscribe(details=>{
      if(details){
        this.userName = details['user_name'] ? details['user_name'] : '--';
        this.userEmail = details['user_email'] ? details['user_email'] : '--';
        if (details['user_email']) {
          this.emailConfigured = true;
        }
      }
    })

    if (!!this.sharedService.getCurrentUserDetails('projectsAccess') && this.sharedService.getCurrentUserDetails('projectsAccess')?.length) {
      const accessList = JSON.parse(JSON.stringify(this.sharedService.getCurrentUserDetails('projectsAccess')));

      this.groupProjects(accessList);
      this.getTableHeadings();
    }


    this.userEmailForm = this.formBuilder.group({
      email: ['', [Validators.required, Validators.pattern('[a-z0-9._%+-]+@[a-z0-9.-]+\.[a-z]{2,3}$')]],
      confirmEmail: ['', [Validators.required, Validators.pattern('[a-z0-9._%+-]+@[a-z0-9.-]+\.[a-z]{2,3}$')]]
    }, { validator: this.checkConfirmEmail });

    this.adLogin = localStorage.loginType === 'AD';
    console.log('adLogin: ' + this.adLogin);


  }

  getTableHeadings(){
    const cols = JSON.parse(localStorage.getItem('hierarchyData'));
    cols.forEach((x) => {
      const obj = {
        id: x.hierarchyLevelId,
        name: x.hierarchyLevelName
      };
      this.dynamicCols?.push(obj);
    });
    this.dynamicCols.push({id:'projectName', name: 'Projects'});
  }

  groupProjects(inArr) {
    for(let k = 0; k<inArr?.length;k++){
      const projectsArr = [];
      for(let i = 0; i < inArr[k]?.projects?.length; i++){
        const obj = {
          role: inArr[k]?.role,
          projectName: inArr[k]?.projects[i]?.projectName,
          projectId: inArr[k]?.projects[i]?.projectId
        };
        const hierarchyArr = inArr[k]?.projects[i]?.hierarchy;
        for(let j = 0; j < hierarchyArr?.length; j++){
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

  setEmail() {
    this.emailSubmitted = true;
    if (this.userEmailForm.invalid) {
      return;
    }
    this.dataLoading = true;
    // call http service
    this.http.changeEmail(this.getEmailForm.email.value, this.sharedService.getCurrentUserDetails('user_name') )
      .subscribe(
        response => {
          this.dataLoading = false;
          if (response && response['success']) {
            this.userEmail = response['data'].emailAddress;
            this.sharedService.setCurrentUserDetails({user_email: this.userEmail});
            this.userEmailConfigured = true;
            this.profile.changePswdDisabled = false;
            this.message = '';
          } else if (response && !response['success']) {
            if (response['message']) {
              this.message = response['message'];
            }
          }
        }
      );
  }

}
