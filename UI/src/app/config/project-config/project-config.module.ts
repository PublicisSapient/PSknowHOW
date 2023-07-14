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
import { ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { InputTextModule } from 'primeng/inputtext';
import { ChipsModule } from 'primeng/chips';
import { ButtonModule } from 'primeng/button';
import { SelectButtonModule } from 'primeng/selectbutton';
import { AccordionModule } from 'primeng/accordion';
import { DropdownModule } from 'primeng/dropdown';
import { PanelMenuModule } from 'primeng/panelmenu';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { ConfirmationService } from 'primeng/api';
import { DataViewModule } from 'primeng/dataview';
import { TableModule } from 'primeng/table';
import { TooltipModule } from 'primeng/tooltip';
import { RippleModule } from 'primeng/ripple';
import { DialogModule } from 'primeng/dialog';
import { InputSwitchModule } from 'primeng/inputswitch';
import { MultiSelectModule } from 'primeng/multiselect';
import { ToolbarModule } from 'primeng/toolbar';
import { FocusTrapModule } from 'primeng/focustrap';
import { MessagesModule } from 'primeng/messages';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { MessageModule } from 'primeng/message';
import { InputTextareaModule } from 'primeng/inputtextarea';
import {InputNumberModule} from 'primeng/inputnumber';
import { AutoCompleteModule } from 'primeng/autocomplete';
import { ProjectConfigComponent } from './project-config.component';
import { BasicConfigComponent } from './basic-config/basic-config.component';
import { ProjectConfigRoutingModule } from './project-config.route';
import { RouterModule } from '@angular/router';
import { ToolMenuComponent } from './tool-menu/tool-menu.component';
import { ProjectListComponent } from './project-list/project-list.component';
import { FieldMappingComponent } from './field-mapping/field-mapping.component';
import { ConnectionListComponent } from './connection-list/connection-list.component';
import { KanbanFieldMappingComponent } from './kanban-field-mapping/kanban-field-mapping.component';
import { JiraConfigComponent } from './jira-config/jira-config.component';
import { BlockUIModule } from 'primeng/blockui';
import { PanelModule } from 'primeng/panel';
import { SharedModuleModule } from 'src/app/shared-module/shared-module.module';
import { BadgeModule } from 'primeng/badge';
import { RadioButtonModule } from 'primeng/radiobutton';
import { PasswordModule } from 'primeng/password';
@NgModule({
  declarations: [ProjectConfigComponent,
    BasicConfigComponent,
    ToolMenuComponent,
    ProjectListComponent,
    FieldMappingComponent,
    ConnectionListComponent,
    FieldMappingComponent,
    KanbanFieldMappingComponent,
    JiraConfigComponent],
  imports: [
    ProjectConfigRoutingModule,
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    FormsModule,
    ButtonModule,
    SelectButtonModule,
    ChipsModule,
    InputTextModule,
    AccordionModule,
    DropdownModule,
    PanelMenuModule,
    ToastModule,
    DataViewModule,
    RippleModule,
    TooltipModule,
    DialogModule,
    MultiSelectModule,
    TableModule,
    ToolbarModule,
    InputSwitchModule,
    FocusTrapModule,
    MessagesModule,
    MessageModule,
    InputTextareaModule,
    ConfirmDialogModule,
    AutoCompleteModule,
    InputNumberModule,
    BlockUIModule,
    PanelModule,
    SharedModuleModule,
    BadgeModule,
    RadioButtonModule,
    PasswordModule
  ],
  providers: [MessageService, ConfirmationService]
})
export class ProjectConfigModule { }
