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
import { ReactiveFormsModule,FormsModule } from '@angular/forms';
import { ProjectConfigComponent } from './project-config.component';
import { BasicConfigComponent } from './basic-config/basic-config.component';
import { ProjectConfigRoutingModule } from './project-config.route';
import { RouterModule } from '@angular/router';
import { ToolMenuComponent } from './tool-menu/tool-menu.component';
import { ProjectListComponent } from './project-list/project-list.component';
import { FieldMappingComponent } from './field-mapping/field-mapping.component';
import { ConnectionListComponent } from './connection-list/connection-list.component';
import { ConfigSettingsComponent } from './config-settings/config-settings.component';
import { ProjectSettingsComponent } from './project-settings/project-settings.component'
import { JiraConfigComponent } from './jira-config/jira-config.component';
import { SharedModuleModule } from 'src/app/shared/module/shared-module.module';
import { NgPrimeModuleModule } from 'src/app/shared/module/ng-Prime-module.module';
@NgModule({
  declarations: [ProjectConfigComponent,
    BasicConfigComponent,
    ToolMenuComponent,
    ProjectListComponent,
    FieldMappingComponent,
    ConnectionListComponent,
    ConfigSettingsComponent,
    ProjectSettingsComponent,
    FieldMappingComponent,
    JiraConfigComponent],
  imports: [
    ProjectConfigRoutingModule,
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    FormsModule,
    SharedModuleModule,
    NgPrimeModuleModule,
  ],
  providers: []
})
export class ProjectConfigModule { }
