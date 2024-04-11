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
import { GetAuthorizationService } from '../../services/get-authorization.service';
import { Router } from '@angular/router';
import { SharedService } from 'src/app/services/shared.service';
import { environment } from 'src/environments/environment';

declare let $: any;

@Component({
    selector: 'app-profile',
    templateUrl: './profile.component.html',
    styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {
    isSuperAdmin = false;
    isProjectAdmin = false;
    changePswdDisabled = false;
    adLogin = false;
    ssoLogin = environment.SSO_LOGIN;
    constructor(private getAuthorizationService: GetAuthorizationService, public router: Router, private sharedService : SharedService) {}

    ngOnInit() {
        if (this.getAuthorizationService.checkIfSuperUser()) {
            // logged in as SuperAdmin
            this.isSuperAdmin = true;
        }

        if(this.getAuthorizationService.checkIfProjectAdmin()) {
            this.isProjectAdmin = true;
        }
        this.sharedService.currentUserDetailsObs.subscribe(details=>{
            if (details && !details['user_email']) {
                    this.changePswdDisabled = true;
                }
          })

        this.adLogin = localStorage.loginType === 'AD';
    }

}
