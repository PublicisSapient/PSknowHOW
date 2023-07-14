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
import { InputSwitchModule } from 'primeng/inputswitch';
import { NgSelectModule } from '@ng-select/ng-select';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ProfileComponent } from './profile.component';
import { ViewRequestsComponent } from './view-requests/view-requests.component';
import { TableModule } from 'primeng/table';
import { RequestStatusComponent } from './request-status/request-status.component';
import { MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';
import { InputTextModule } from 'primeng/inputtext';
import { UserMgmtComponent } from './user-mgmt/user-mgmt.component';
import { MyprofileComponent } from './myprofile/myprofile.component';
import { AccessMgmtComponent } from './access-mgmt/access-mgmt.component';
import { DropdownModule } from 'primeng/dropdown';
import { DialogModule } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { MultiSelectModule } from 'primeng/multiselect';
import { ConfirmPopupModule } from 'primeng/confirmpopup';
import { ConfirmationService } from 'primeng/api';
import { SharedModuleModule } from '../../shared-module/shared-module.module';
import { TooltipModule } from 'primeng/tooltip';
import { AutoApprovalComponent } from './auto-approval/auto-approval.component';
import {CheckboxModule} from 'primeng/checkbox';
import { ConfirmDialogModule } from 'primeng/confirmdialog';


@NgModule({
  imports: [
    CommonModule,
    ProfileRoutingModule,
    InputSwitchModule,
    NgSelectModule,
    FormsModule,
    TableModule,
    ToastModule,
    InputTextModule,
    ReactiveFormsModule,
    DropdownModule,
    DialogModule,
    ButtonModule,
    MultiSelectModule,
    SharedModuleModule,
    ConfirmPopupModule,
    TooltipModule,
    CheckboxModule,
    ConfirmDialogModule
  ],
  declarations: [RaiseAccessRequestComponent, ProfileComponent, ViewRequestsComponent, RequestStatusComponent, UserMgmtComponent, MyprofileComponent, AccessMgmtComponent, AutoApprovalComponent],
  providers: [MessageService, ConfirmationService],
  exports: [RaiseAccessRequestComponent]
})
export class ProfileModule { }
