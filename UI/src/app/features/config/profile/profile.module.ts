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

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProfileRoutingModule } from './profile.route';
import { RaiseAccessRequestComponent } from './raise-access-request/raise-access-request.component';

import { NgSelectModule } from '@ng-select/ng-select';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ProfileComponent } from './profile.component';
import { ViewRequestsComponent } from './view-requests/view-requests.component';
import { RequestStatusComponent } from './request-status/request-status.component';
import { UserMgmtComponent } from './user-mgmt/user-mgmt.component';
import { MyprofileComponent } from './myprofile/myprofile.component';
import { AccessMgmtComponent } from './access-mgmt/access-mgmt.component';
import { SharedModuleModule } from '../../../shared/module/shared-module.module';
import { AutoApprovalComponent } from './auto-approval/auto-approval.component';
import { NgPrimeModuleModule } from 'src/app/shared/module/ng-Prime-module.module';


@NgModule({
  imports: [
    CommonModule,
    ProfileRoutingModule,
    NgSelectModule,
    FormsModule,
    ReactiveFormsModule,
    SharedModuleModule,
    NgPrimeModuleModule,
    
  ],
  declarations: [RaiseAccessRequestComponent, ProfileComponent, ViewRequestsComponent, RequestStatusComponent, UserMgmtComponent, MyprofileComponent, AccessMgmtComponent, AutoApprovalComponent],
  providers: [NgPrimeModuleModule],
  exports: [RaiseAccessRequestComponent]
})
export class ProfileModule { }
