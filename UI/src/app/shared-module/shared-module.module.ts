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
import { CalendarModule } from 'primeng/calendar';
import { MultiSelectModule } from 'primeng/multiselect';
import { DropdownModule } from 'primeng/dropdown';
import { ProjectFilterComponent } from './project-filter/project-filter.component';
import { NamePipePipe } from './name-pipe.pipe';
import { PageLoaderComponent } from './page-loader/page-loader.component';
import { FooterComponent } from './footer/footer.component';
import { KpiFilterComponent } from './kpi-filter/kpi-filter.component';
import { FieldMappingFormComponent } from './field-mapping-form/field-mapping-form.component';
import { DialogModule } from 'primeng/dialog';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ConfirmationService } from 'primeng/api';
import { FieldMappingFieldComponent } from './field-mapping-field/field-mapping-field.component';
import { AccordionModule } from 'primeng/accordion';
import { TooltipModule } from 'primeng/tooltip';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { ChipsModule } from 'primeng/chips';
import { RadioButtonModule } from 'primeng/radiobutton';
import { AdditionalFilterFieldComponent } from './additional-filter-field/additional-filter-field.component';
import { InputSwitchModule } from 'primeng/inputswitch';
import { CarouselModule } from 'primeng/carousel';
import { FeatureFlagDirective } from './custom-directives/feature-flag.directive';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    CalendarModule,
    NgSelectModule,
    MultiSelectModule,
    DropdownModule,
    DialogModule,
    ToastModule,
    ConfirmDialogModule,
    AccordionModule,
    ReactiveFormsModule,
    ToastModule,
    TooltipModule,
    InputTextModule,
    ButtonModule,
    ChipsModule,
    RadioButtonModule,
    InputSwitchModule,
    CarouselModule
  ],
  exports: [
    NgSelectModule,
    ProjectFilterComponent,
    NamePipePipe,
    PageLoaderComponent,
    CalendarModule,
    FooterComponent,
    KpiFilterComponent,
    FieldMappingFormComponent,
    FeatureFlagDirective,
    CarouselModule
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
    FeatureFlagDirective
  ],
  providers: [ConfirmationService]
})
export class SharedModuleModule { }
