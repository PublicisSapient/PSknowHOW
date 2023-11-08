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
import { RouterModule, Routes } from '@angular/router';
import { ProjectConfigComponent } from './project-config.component';
import { BasicConfigComponent } from './basic-config/basic-config.component';
import { ProjectListComponent } from './project-list/project-list.component';
import { ToolMenuComponent } from './tool-menu/tool-menu.component';
import { FieldMappingComponent } from './field-mapping/field-mapping.component';
import { ConnectionListComponent } from './connection-list/connection-list.component';
import { KanbanFieldMappingComponent } from './kanban-field-mapping/kanban-field-mapping.component';
import { JiraConfigComponent } from './jira-config/jira-config.component';
import { FeatureGuard } from 'src/app/services/feature.guard';

export const ProjectConfigRoutes: Routes = [
    {
        path: '',
        component: ProjectConfigComponent,
        canActivateChild: [FeatureGuard],
        children: [
            {
                path: '',
                redirectTo: 'ProjectList',
                pathMatch: 'full',
                data: {
                    feature: "ProjectList"
                }
            },
            {
                path: 'BasicConfig',
                component: BasicConfigComponent,
                data: {
                    feature: "BasicConfig"
                }
            },
            {
                path: 'ProjectList',
                component: ProjectListComponent,
                data: {
                    feature: "ProjectList"
                }
            },
            {
                path: 'ToolMenu',
                component: ToolMenuComponent,
                data: {
                    feature: "ToolMenu"
                }
            },
            {
                path: 'MappingMenu',
                component: ToolMenuComponent,
                data: {
                    feature: "MappingMenu"
                }
            },
            {
                path: 'FieldMapping',
                component: FieldMappingComponent,
                data: {
                    feature: "FieldMapping"
                }
            },
            {
                path: 'KanbanFieldMapping',
                component: KanbanFieldMappingComponent,
                data: {
                    feature: "KanbanFieldMapping"
                }
            },
            {
                path: 'connection-list',
                component: ConnectionListComponent,
                data: {
                    feature: "connection-list"
                }
            },
            {
                path: 'JiraConfig',
                component: JiraConfigComponent,
                data: {
                    feature: "JiraConfig"
                }
            }
        ]
    }
];

@NgModule({
    imports: [RouterModule.forChild(ProjectConfigRoutes)],
    exports: [RouterModule]
})

export class ProjectConfigRoutingModule { }
