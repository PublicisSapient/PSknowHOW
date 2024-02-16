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
import { DropdownModule } from 'primeng/dropdown';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { InputSwitchModule } from 'primeng/inputswitch';
import { ReactiveFormsModule } from '@angular/forms';
import { KeyFilterModule } from 'primeng/keyfilter';
import { MultiSelectModule } from 'primeng/multiselect';
import { ChipsModule } from 'primeng/chips';
import { AccordionModule } from 'primeng/accordion';
import { FieldsetModule } from 'primeng/fieldset';
import { PasswordModule } from 'primeng/password';
import { AutoCompleteModule } from 'primeng/autocomplete';
// import { ButtonModule, SharedModule } from 'primeng/primeng';
import { TableModule } from 'primeng/table';
import { AutoCompleteComponent } from '../component/auto-complete/auto-complete.component';
import { ConfigComponent } from './config.component';
import { UploadComponent } from './upload/upload.component';
import { DashboardconfigComponent } from './dashboard-config/dashboard-config.component';
import { FileUploadModule } from 'primeng/fileupload';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ConfirmationService } from 'primeng/api';
import { ScrumKanbanPipe } from './pipes/scrumKanbanPipe';
// import { TextMaskPipe } from './pipes/textMaskPipe';
import { TabMenuModule } from 'primeng/tabmenu';
import { PanelMenuModule } from 'primeng/panelmenu';
import { ProfileModule } from './profile/profile.module';
import { ProjectConfigModule } from './project-config/project-config.module';
import { NgSelectModule } from '@ng-select/ng-select';
import { CheckboxModule } from 'primeng/checkbox';
import { InputNumberModule } from 'primeng/inputnumber';
import { AdvancedSettingsComponent } from './advanced-settings/advanced-settings.component';
import { CardModule } from 'primeng/card';
import { SharedModuleModule } from '../shared-module/shared-module.module';
import { CalendarModule } from 'primeng/calendar';
import { AdSettingsComponent } from './ad-settings/ad-settings.component';
import { DialogModule } from 'primeng/dialog';
import { ViewNewUserAuthRequestComponent } from './profile/view-new-user-auth-request/view-new-user-auth-request.component';
import { TabViewModule } from 'primeng/tabview';
import { TypeofPipe } from './pipes/type-of.pipe';
import { CompareStartEndWithCurrentDatePipe } from './pipes/compareStartEndWithCurrentDate';
import { ManageAssigneeComponent } from './manage-assignee/manage-assignee.component';
import { RatingComponent } from './rating/rating.component';
import { CapacityPlanningComponent } from './capacity-planning/capacity-planning.component';

@NgModule({
  imports: [
    CommonModule,
    CalendarModule,
    FormsModule,
    ConfigRoutingModule,
    TableModule,
    InputSwitchModule,
    ReactiveFormsModule,
    KeyFilterModule,
    MultiSelectModule,
    ChipsModule,
    AccordionModule,
    // ButtonModule, SharedModule,
    FieldsetModule,
    DropdownModule,
    PasswordModule,
    FileUploadModule,
    ToastModule,
    ConfirmDialogModule,
    TabMenuModule,
    PanelMenuModule,
    ProfileModule,
    ProjectConfigModule,
    InputNumberModule,
    NgSelectModule,
    CheckboxModule,
    SharedModuleModule,
    CardModule,
    DialogModule,
    TabViewModule,
    AutoCompleteModule
  ],
  declarations: [
    ConfigComponent,
    AutoCompleteComponent,
    UploadComponent,
    DashboardconfigComponent,
    ScrumKanbanPipe,
    // TextMaskPipe,
    AdvancedSettingsComponent,
    AdSettingsComponent,
    ViewNewUserAuthRequestComponent,
    TypeofPipe,
    CompareStartEndWithCurrentDatePipe,
    ManageAssigneeComponent,
    RatingComponent,
    CapacityPlanningComponent
    // FilterComponent
  ],
  providers: [MessageService, ConfirmationService]

})
export class ConfigModule { }
