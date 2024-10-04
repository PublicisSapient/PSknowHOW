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
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ConditionalInputComponent } from '../component/conditional-input/conditional-input.component';
import { FeatureFlagDirective } from '../directives/custom-directives/feature-flag.directive';
import { FieldMappingFormComponent } from '../component/field-mapping-form/field-mapping-form.component';
import { KpiFilterComponent } from '../component/kpi-filter/kpi-filter.component';
import { FooterComponent } from '../component/footer/footer.component';
import { PageLoaderComponent } from '../component/page-loader/page-loader.component';
import { NamePipePipe } from '../pipes/name-pipe.pipe';
import { ProjectFilterComponent } from '../component/project-filter/project-filter.component';
import { AdditionalFilterFieldComponent } from '../component/additional-filter-field/additional-filter-field.component';
import { FieldMappingFieldComponent } from '../component/field-mapping-field/field-mapping-field.component';
import { NgPrimeModuleModule } from './ng-Prime-module.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    NgSelectModule,
    ReactiveFormsModule,
    NgPrimeModuleModule
  ],
  exports: [
    NgSelectModule,
    ProjectFilterComponent,
    NamePipePipe,
    PageLoaderComponent,
    FooterComponent,
    KpiFilterComponent,
    FieldMappingFormComponent,
    FeatureFlagDirective,
    ConditionalInputComponent
  ],
  declarations: [
    ProjectFilterComponent,
    NamePipePipe,
    PageLoaderComponent,
    FooterComponent,
    KpiFilterComponent,
    FieldMappingFormComponent,
    FieldMappingFieldComponent,
    AdditionalFilterFieldComponent,
    FeatureFlagDirective,
    ConditionalInputComponent
  ],
  providers: []
})
export class SharedModuleModule { }
