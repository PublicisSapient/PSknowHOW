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
import { NgSelectModule } from '@ng-select/ng-select';
import { FormsModule } from '@angular/forms';
import { CalendarModule } from 'primeng/calendar';
import { MultiSelectModule } from 'primeng/multiselect';
import { DropdownModule } from 'primeng/dropdown';
import { ProjectFilterComponent } from './project-filter/project-filter.component';
import { NamePipePipe } from './name-pipe.pipe';
import { PageLoaderComponent } from './page-loader/page-loader.component';
import { FooterComponent } from './footer/footer.component';
import { KpiFilterComponent } from './kpi-filter/kpi-filter.component';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    CalendarModule,
    NgSelectModule,
    MultiSelectModule,
    DropdownModule
  ],
  exports: [
    NgSelectModule,
    ProjectFilterComponent,
    NamePipePipe,
    PageLoaderComponent,
    CalendarModule,
    FooterComponent,
    KpiFilterComponent
  ],
  declarations: [
    ProjectFilterComponent,
    NamePipePipe,
    PageLoaderComponent,
    FooterComponent,
    KpiFilterComponent
  ]
})
export class SharedModuleModule { }
