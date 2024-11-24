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
import { of } from 'rxjs';
import { NavigationEnd, Router } from '@angular/router';
describe('ConfigComponent', () => {
  let component: ConfigComponent;
  let fixture: ComponentFixture<ConfigComponent>;
  let getAuthorizationService;
  let sharedService;
  let router: Router;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        CommonModule,
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
        SharedModuleModule,
      ],
      declarations: [
        ConfigComponent,
        AutoCompleteComponent,
        UploadComponent,
        DashboardconfigComponent,
        AdvancedSettingsComponent,
        ScrumKanbanPipe,
        // TextMaskPipe,
      ],
      providers: [
        MessageService,
        ConfirmationService,
        GetAuthorizationService,
        SharedService,
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ConfigComponent);
    component = fixture.componentInstance;
    getAuthorizationService = TestBed.inject(GetAuthorizationService);
    sharedService = TestBed.inject(SharedService);
    router = TestBed.inject(Router);
  });

  it('Config component should create', (done) => {
    expect(component).toBeTruthy();
    done();
  });

  it('should check if superadmin has access', () => {
    spyOn(getAuthorizationService, 'checkIfSuperUser').and.returnValue(true);
    component.ngOnInit();
    expect(component.hasAccess).toBe(true);
  });

  it('should check if user has access', () => {
    component.hasAccess = false;
    const projectsAccess = [
      {
        role: 'ROLE_PROJECT_VIEWER',
        projects: [
          {
            projectName: 'abc',
            projectId: '65643593e157cb3b681d2d62',
            hierarchy: [
              {
                hierarchyLevel: {
                  level: 1,
                  hierarchyLevelId: 'cat1',
                  hierarchyLevelName: 'CAT1',
                },
                value: 'Europe',
              },
              {
                hierarchyLevel: {
                  level: 2,
                  hierarchyLevelId: 'cat2',
                  hierarchyLevelName: 'CAT2',
                },
                value: 'Education',
              },
              {
                hierarchyLevel: {
                  level: 3,
                  hierarchyLevelId: 'cat3',
                  hierarchyLevelName: 'CAT3',
                },
                value: 'ADQ Financial Services LLC',
              },
              {
                hierarchyLevel: {
                  level: 4,
                  hierarchyLevelId: 'cat4',
                  hierarchyLevelName: 'CAT4',
                },
                value: '3PP - Cross Regional',
              },
            ],
          },
        ],
      },
    ];
    spyOn(sharedService, 'getCurrentUserDetails').and.returnValue(
      projectsAccess,
    );
    spyOn(getAuthorizationService, 'checkIfSuperUser').and.returnValue(false);
    spyOn(getAuthorizationService, 'checkIfProjectAdmin').and.returnValue(true);
    component.ngOnInit();
    expect(component.hasAccess).toBe(true);
  });

  it('should check if user doesnt have access', () => {
    component.hasAccess = true;
    const projectsAccess = [
      {
        role: 'ROLE_PROJECT_VIEWER',
        projects: [
          {
            projectName: 'abc',
            projectId: '65643593e157cb3b681d2d62',
            hierarchy: [
              {
                hierarchyLevel: {
                  level: 1,
                  hierarchyLevelId: 'cat1',
                  hierarchyLevelName: 'CAT1',
                },
                value: 'Europe',
              },
              {
                hierarchyLevel: {
                  level: 2,
                  hierarchyLevelId: 'cat2',
                  hierarchyLevelName: 'CAT2',
                },
                value: 'Education',
              },
              {
                hierarchyLevel: {
                  level: 3,
                  hierarchyLevelId: 'cat3',
                  hierarchyLevelName: 'CAT3',
                },
                value: 'ADQ Financial Services LLC',
              },
              {
                hierarchyLevel: {
                  level: 4,
                  hierarchyLevelId: 'cat4',
                  hierarchyLevelName: 'CAT4',
                },
                value: '3PP - Cross Regional',
              },
            ],
          },
        ],
      },
    ];
    spyOn(sharedService, 'getCurrentUserDetails').and.returnValue(
      projectsAccess,
    );
    spyOn(getAuthorizationService, 'checkIfSuperUser').and.returnValue(false);
    spyOn(getAuthorizationService, 'checkIfProjectAdmin').and.returnValue(
      false,
    );
    component.ngOnInit();
    expect(component.hasAccess).toBe(false);
  });

  it('should check if user has access when not superadmin', () => {
    component.hasAccess = true;
    const projectsAccess = [];
    spyOn(sharedService, 'getCurrentUserDetails').and.returnValue(
      projectsAccess,
    );
    spyOn(getAuthorizationService, 'checkIfSuperUser').and.returnValue(false);
    component.ngOnInit();
    expect(component.hasAccess).toBe(false);
  });

  it('should call setActiveTabOnClick with the current router url', () => {
    spyOn(component, 'setActiveTabOnClick');
    spyOnProperty(router, 'url', 'get').and.returnValue('/dashboard/Config');
    component.ngOnInit();
    expect(component.setActiveTabOnClick).toHaveBeenCalledWith(
      '/dashboard/Config',
    );
  });

  it('should subscribe to router events and call setActiveTabOnClick', () => {
    spyOn(component, 'setActiveTabOnClick');
    const navigationEndEvent = new NavigationEnd(1, 'url', 'urlAfterRedirects');
    spyOn(router.events, 'subscribe').and.callFake((callback: any) => {
      return callback(navigationEndEvent);
    });
    component.ngOnInit();
    expect(component.setActiveTabOnClick).toHaveBeenCalledWith(
      'urlAfterRedirects',
    );
  });
});
