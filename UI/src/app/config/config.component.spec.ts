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

import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
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
// import { ButtonModule, SharedModule } from 'primeng/primeng';
import { TableModule } from 'primeng/table';
import { AutoCompleteComponent } from '../component/auto-complete/auto-complete.component';
import { ConfigComponent } from './config.component';
import { UploadComponent } from './upload/upload.component';
import { DashboardconfigComponent } from './dashboard-config/dashboard-config.component';
import { FileUploadModule } from 'primeng/fileupload';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ScrumKanbanPipe } from './pipes/scrumKanbanPipe';
// import { TextMaskPipe } from './pipes/textMaskPipe';
import { TabMenuModule } from 'primeng/tabmenu';
import { MessageService } from 'primeng/api';
import { ConfirmationService } from 'primeng/api';
import { RouterTestingModule } from '@angular/router/testing';
import { AdvancedSettingsComponent } from './advanced-settings/advanced-settings.component';
import { CheckboxModule } from 'primeng/checkbox';
import { PanelMenuModule } from 'primeng/panelmenu';
import { SharedModuleModule } from '../shared-module/shared-module.module';
import { GetAuthorizationService } from '../services/get-authorization.service';
import { SharedService } from '../services/shared.service';
describe('ConfigComponent', () => {
  let component: ConfigComponent;
  let fixture: ComponentFixture<ConfigComponent>;
  let getAuthorizationService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [CommonModule,
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
        FileUploadModule,
        ToastModule,
        ConfirmDialogModule,
        TabMenuModule,
        RouterTestingModule.withRoutes([]),
        CheckboxModule,
        PanelMenuModule,
        SharedModuleModule],
      declarations: [
        ConfigComponent,
        AutoCompleteComponent,
        UploadComponent,
        DashboardconfigComponent,
        AdvancedSettingsComponent,
        ScrumKanbanPipe,
        // TextMaskPipe,
      ],
      providers: [MessageService, ConfirmationService, GetAuthorizationService,SharedService]
    })
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ConfigComponent);
    component = fixture.componentInstance;
    getAuthorizationService = TestBed.inject(GetAuthorizationService);
    // fixture.detectChanges();
  });

  it('Config component should create', (done) => {
    expect(component).toBeTruthy();
    done();
  });

  it('should check if superadmin has access', () => {
    spyOn(getAuthorizationService, 'checkIfSuperUser').and.returnValue(true);
    component.ngOnInit();
    expect(component.hasAccess).toBe(true);
  })
});
