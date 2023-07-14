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

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BasicConfigComponent } from './basic-config.component';
import { MessageService } from 'primeng/api';
import { HttpService } from '../../../services/http.service';
import { SharedService } from '../../../services/shared.service';
import { GetAuthorizationService } from '../../../services/get-authorization.service';
import { FormGroup, ReactiveFormsModule, FormsModule, FormBuilder } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AppConfig, APP_CONFIG } from 'src/app/services/app.config';

import { DropdownModule } from 'primeng/dropdown';
import { SelectButtonModule } from 'primeng/selectbutton';
import { ToolbarModule } from 'primeng/toolbar';
import { MessagesModule } from 'primeng/messages';
import { MessageModule } from 'primeng/message';
import { ToastModule } from 'primeng/toast';
import { TableModule } from 'primeng/table';
import { AutoCompleteModule } from 'primeng/autocomplete';

import { environment } from 'src/environments/environment';
import {InputSwitchModule} from 'primeng/inputswitch';

describe('BasicConfigComponent', () => {
  let component: BasicConfigComponent;
  let fixture: ComponentFixture<BasicConfigComponent>;
  let httpService: HttpService;
  let sharedService: SharedService;
  let httpMock;
  const baseUrl = environment.baseUrl;
  // const toolsData = require('../../../../test/resource/fakeToolsData.json');

  const hierarchyData = [
    {
      level: 1,
      hierarchyLevelId: 'country',
      hierarchyLevelName: 'Country',
      suggestions: [
        {
          name: 'Canada',
          code: 'Canada'
        },
        {
          name: 'India',
          code: 'India'
        },
        {
          name: 'USA',
          code: 'USA'
        }
      ],
      value: '',
      required: true
    },
    {
      level: 2,
      hierarchyLevelId: 'state',
      hierarchyLevelName: 'State',
      suggestions: [
        {
          name: 'Haryana',
          code: 'Haryana'
        },
        {
          name: 'Karnataka',
          code: 'Karnataka'
        },
        {
          name: 'Ontario',
          code: 'Ontario'
        },
        {
          name: 'Texas',
          code: 'Texas'
        },
        {
          name: 'Washinton',
          code: 'Washinton'
        }
      ],
      value: '',
      required: true
    },
    {
      level: 3,
      hierarchyLevelId: 'city',
      hierarchyLevelName: 'City',
      suggestions: [
        {
          name: 'Bangalore',
          code: 'Bangalore'
        },
        {
          name: 'Gurgaon',
          code: 'Gurgaon'
        },
        {
          name: 'Houston',
          code: 'Houston'
        },
        {
          name: 'Kurukshetra',
          code: 'Kurukshetra'
        },
        {
          name: 'Ottawa',
          code: 'Ottawa'
        },
        {
          name: 'Remond',
          code: 'Remond'
        },
        {
          name: 'Seattle',
          code: 'Seattle'
        }
      ],
      value: '',
      required: true
    }
  ];

  const formValue = {
    kanban: false,
    country: {
      name: 'Canada',
      code: 'Canada'
    },
    state: {
      name: 'Ontario',
      code: 'Ontario'
    },
    city: {
      name: 'Ottawa',
      code: 'Ottawa'
    },
    projectName: 'Test44'
  };

  const successResponse = {
    serviceResponse: {
        message: 'Added Successfully.',
        success: true,
        data: {
            id: '6335497f67af3f41656b7b42',
            projectName: 'Test44',
            createdAt: '2022-09-29T13:00:07',
            kanban: false,
            hierarchy: [
                {
                    hierarchyLevel: {
                        level: 1,
                        hierarchyLevelId: 'country',
                        hierarchyLevelName: 'Country'
                    },
                    value: 'Canada'
                },
                {
                    hierarchyLevel: {
                        level: 2,
                        hierarchyLevelId: 'state',
                        hierarchyLevelName: 'State'
                    },
                    value: 'Ontario'
                },
                {
                    hierarchyLevel: {
                        level: 3,
                        hierarchyLevelId: 'city',
                        hierarchyLevelName: 'City'
                    },
                    value: 'Ottawa'
                }
            ],
            isKanban: false
        }
    },
    projectsAccess: []
};

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [BasicConfigComponent],
      imports: [
        FormsModule,
        ReactiveFormsModule,
        RouterTestingModule,
        HttpClientTestingModule,
        DropdownModule,
        SelectButtonModule,
        ToolbarModule,
        MessagesModule,
        MessageModule,
        ToastModule,
        TableModule,
        AutoCompleteModule,
        InputSwitchModule
      ],
      providers: [
        HttpService,
        SharedService,
        MessageService,
        GetAuthorizationService,
        { provide: APP_CONFIG, useValue: AppConfig }
      ]
    })
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(BasicConfigComponent);
    component = fixture.componentInstance;
    httpService = TestBed.inject(HttpService);
    sharedService = TestBed.inject(SharedService);
    // sharedService.setSelectedProject(fakeProject);
    httpMock = TestBed.inject(HttpTestingController);


    let localStore = {};

    spyOn(window.localStorage, 'getItem').and.callFake((key) =>
      key in localStore ? localStore[key] : null
    );
    spyOn(window.localStorage, 'setItem').and.callFake(
      (key, value) => (localStore[key] = value + '')
    );
    spyOn(window.localStorage, 'clear').and.callFake(() => (localStore = {}));

    localStorage.setItem('hierarchyData', JSON.stringify(hierarchyData));
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });


  it('should create the form', () => {
    sharedService.setSelectedProject(null);
    component.ifSuperUser = false;
    component.ngOnInit();
    fixture.detectChanges();
    expect(component.formData.length).toEqual(hierarchyData.length + 3);
    const controls = component.form.controls;
    let totalControl = 0;
    for (const name in controls) {
      totalControl++;
    }
    expect(totalControl).toEqual(hierarchyData.length + 3);
    expect(component.form.valid).toBeFalse();

    const compiled = fixture.debugElement.nativeElement;
    const addressInput = compiled.querySelector('p-autoComplete[id="country"]');
    const nameInput = compiled.querySelector('p-autoComplete[id="state"]');

    expect(addressInput).toBeTruthy();
    expect(nameInput).toBeTruthy();
  });

  it('should filter out and display suggestions', () => {
    const event = {
      originalEvent: {
        isTrusted: true
      },
      query: 'ca'
    };
    const field = {
      level: 1,
      hierarchyLevelId: 'country',
      hierarchyLevelName: 'Country',
      suggestions: [
        {
          name: 'Canada',
          code: 'Canada'
        },
        {
          name: 'India',
          code: 'India'
        },
        {
          name: 'USA',
          code: 'USA'
        }
      ],
      value: '',
      required: true,
      filteredSuggestions: [
        {
          name: 'Canada',
          code: 'Canada'
        }
      ]
    };
    component.search(event, field);
    fixture.detectChanges();
    const filteredSuggestions = [
      {
        name: 'Canada',
        code: 'Canada'
      }
    ];
    expect(field.filteredSuggestions).toEqual(filteredSuggestions);
  });

  it('should submit config', () => {
    component.ngOnInit();
    fixture.detectChanges();
    Object.keys(formValue).forEach((key) => {
      component.form.controls[key].setValue(formValue[key]);
    });
    fixture.detectChanges();
    expect(component.form.valid).toBeTruthy();
    component.onSubmit();
    fixture.detectChanges();
    httpMock.match(`${baseUrl}/api/basicconfigs`)[0].flush(successResponse);
  });

  it('should not allow "###", "~" or "`" in any of the inputs', () => {
    component.ngOnInit();
    fixture.detectChanges();
    component.form.controls['projectName'].setValue('Test###');
    fixture.detectChanges();
    expect(component.form.controls['projectName'].valid).toBeFalsy();
  });
});
