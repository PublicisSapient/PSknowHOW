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
import { ConfigRoutingModule } from './config.route';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { AutoCompleteComponent } from '../../shared/component/auto-complete/auto-complete.component';
import { ConfigComponent } from './config.component';
import { UploadComponent } from './upload/upload.component';
import { DashboardconfigComponent } from './dashboard-config/dashboard-config.component';
import { ProfileModule } from './profile/profile.module';
import { ProjectConfigModule } from './project-config/project-config.module';
import { NgSelectModule } from '@ng-select/ng-select';
import { AdvancedSettingsComponent } from './advanced-settings/advanced-settings.component';
import { ViewNewUserAuthRequestComponent } from './profile/view-new-user-auth-request/view-new-user-auth-request.component';
import { TypeofPipe } from './pipes/type-of.pipe';
import { CompareStartEndWithCurrentDatePipe } from './pipes/compareStartEndWithCurrentDate';
import { ManageAssigneeComponent } from './manage-assignee/manage-assignee.component';
import { RatingComponent } from './rating/rating.component';
import { CapacityPlanningComponent } from './capacity-planning/capacity-planning.component';
import { SharedModuleModule } from 'src/app/shared/module/shared-module.module';
import { NgPrimeModuleModule } from 'src/app/shared/module/ng-Prime-module.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    ConfigRoutingModule,
    ReactiveFormsModule,
    ProfileModule,
    ProjectConfigModule,
    NgSelectModule,
    SharedModuleModule,
    NgPrimeModuleModule
  ],
  declarations: [
    ConfigComponent,
    AutoCompleteComponent,
    UploadComponent,
    DashboardconfigComponent,
    AdvancedSettingsComponent,
    ViewNewUserAuthRequestComponent,
    TypeofPipe,
    CompareStartEndWithCurrentDatePipe,
    ManageAssigneeComponent,
    RatingComponent,
    CapacityPlanningComponent
  ],
  providers: []

})
export class ConfigModule { }
