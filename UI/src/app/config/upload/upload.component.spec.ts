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

import { ComponentFixture, fakeAsync, TestBed, tick, waitForAsync } from '@angular/core/testing';
import { UploadComponent } from './upload.component';
import { CommonModule } from '@angular/common';
import { InputSwitchModule } from 'primeng/inputswitch';
import { FormControl, ReactiveFormsModule,UntypedFormControl, UntypedFormGroup, Validators  } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { Routes } from '@angular/router';
import { DashboardComponent } from '../../dashboard/dashboard.component';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HttpService } from '../../services/http.service';
import { environment } from 'src/environments/environment';
import { APP_CONFIG, AppConfig } from '../../services/app.config';
import { SharedService } from '../../services/shared.service';
import { MessageService } from 'primeng/api';
import { GetAuthService } from '../../services/getauth.service';
import { NgSelectModule } from '@ng-select/ng-select';
import { of } from 'rxjs';
import { ManageAssigneeComponent } from '../manage-assignee/manage-assignee.component';

describe('UploadComponent', () => {
  let component: UploadComponent;
  let fixture: ComponentFixture<UploadComponent>;
  const baseUrl = environment.baseUrl;
  let httpMock;
  let httpService;
  let messageService;
  const fakeUploadedImage = { image: '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' };
  const fakeErrorOnUpload = { status: 500, statusText: 'Internal server error' };
 
  const fakeSuccessResponseTestExecution = {
    message: 'Test Execution Data',
    success: true,
    data: [
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintName: 'SprintPrioritization_Bucket',
        sprintId: '29732_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintState: 'FUTURE',
        totalTestCases: 3,
        executedTestCase: 1,
        passedTestCase: 1,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false
      },
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintName: 'Tools|PI_11|ITR_2|12 Oct',
        sprintId: '40249_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintState: 'FUTURE',
        totalTestCases: 0,
        executedTestCase: 0,
        passedTestCase: 0,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false
      },
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintName: 'Tools|PI_11|ITR_5| 23 Nov',
        sprintId: '40252_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintState: 'FUTURE',
        totalTestCases: 0,
        executedTestCase: 0,
        passedTestCase: 0,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false
      },
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintName: 'Tools|PI_11|ITR_6| 07 Dec',
        sprintId: '40253_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintState: 'FUTURE',
        totalTestCases: 0,
        executedTestCase: 0,
        passedTestCase: 0,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false
      },
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintName: 'Tools|PI_11|ITR_4| 09 Nov',
        sprintId: '40251_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintState: 'FUTURE',
        totalTestCases: 0,
        executedTestCase: 0,
        passedTestCase: 0,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false
      },
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintName: 'Tools|PI_11|ITR_3|26 Oct',
        sprintId: '40250_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintState: 'FUTURE',
        totalTestCases: 0,
        executedTestCase: 0,
        passedTestCase: 0,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false
      },
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintName: 'Tools|PI_11|ITR_1|28 Sep',
        sprintId: '40248_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintState: 'ACTIVE',
        totalTestCases: 5,
        executedTestCase: 5,
        passedTestCase: 5,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false
      },
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintName: 'Tools|PI_10|ITR_6|07 Sep',
        sprintId: '38998_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintState: 'ACTIVE',
        totalTestCases: 0,
        executedTestCase: 0,
        passedTestCase: 0,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false
      },
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintName: 'Tools|PI_10|ITR_5|24 Aug',
        sprintId: '38997_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintState: 'CLOSED',
        totalTestCases: 60,
        executedTestCase: 50,
        passedTestCase: 45,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false
      },
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintName: 'Tools|PI_10|ITR_4|10 Aug',
        sprintId: '38996_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintState: 'CLOSED',
        totalTestCases: 50,
        executedTestCase: 50,
        passedTestCase: 50,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false
      },
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintName: 'Tools|PI_10|ITR_3|27 Jul',
        sprintId: '38995_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintState: 'CLOSED',
        totalTestCases: 0,
        executedTestCase: 0,
        passedTestCase: 0,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false
      },
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintName: 'Support|PI_10|ITR_2|13 Jul',
        sprintId: '39496_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintState: 'CLOSED',
        totalTestCases: 0,
        executedTestCase: 0,
        passedTestCase: 0,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false
      },
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintName: 'Tools|PI_10|ITR_2|13 Jul',
        sprintId: '38994_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintState: 'CLOSED',
        totalTestCases: 0,
        executedTestCase: 0,
        passedTestCase: 0,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false
      }
    ]
  };

  
  const fakeTestExecutionData = {
    message: 'Test Execution Data',
    success: true,
    data: [
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintName: 'SprintPrioritization_Bucket',
        sprintId: '29732_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintState: 'FUTURE',
        totalTestCases: 3,
        executedTestCase: 1,
        passedTestCase: 1,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false,
        automatableTestCases : 13,
        automatedTestCases : 10,
        totalRegressionTestCases : 102,
        automatedRegressionTestCases : 10,
      },
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintName: 'Tools|PI_11|ITR_2|12 Oct',
        sprintId: '40249_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintState: 'FUTURE',
        totalTestCases: 0,
        executedTestCase: 0,
        passedTestCase: 0,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false,automatableTestCases : 13,
        automatedTestCases : 10,
        totalRegressionTestCases : 102,
        automatedRegressionTestCases : 10,
      },
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintName: 'Tools|PI_11|ITR_5| 23 Nov',
        sprintId: '40252_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintState: 'FUTURE',
        totalTestCases: 0,
        executedTestCase: 0,
        passedTestCase: 0,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false,automatableTestCases : 13,
        automatedTestCases : 10,
        totalRegressionTestCases : 102,
        automatedRegressionTestCases : 10,
      },
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintName: 'Tools|PI_11|ITR_6| 07 Dec',
        sprintId: '40253_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintState: 'FUTURE',
        totalTestCases: 0,
        executedTestCase: 0,
        passedTestCase: 0,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false,automatableTestCases : 13,
        automatedTestCases : 10,
        totalRegressionTestCases : 102,
        automatedRegressionTestCases : 10,
      },
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintName: 'Tools|PI_11|ITR_4| 09 Nov',
        sprintId: '40251_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintState: 'FUTURE',
        totalTestCases: 0,
        executedTestCase: 0,
        passedTestCase: 0,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false,automatableTestCases : 13,
        automatedTestCases : 10,
        totalRegressionTestCases : 102,
        automatedRegressionTestCases : 10,
      },
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintName: 'Tools|PI_11|ITR_3|26 Oct',
        sprintId: '40250_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintState: 'FUTURE',
        totalTestCases: 0,
        executedTestCase: 0,
        passedTestCase: 0,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false,automatableTestCases : 13,
        automatedTestCases : 10,
        totalRegressionTestCases : 102,
        automatedRegressionTestCases : 10,
      },
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintName: 'Tools|PI_11|ITR_1|28 Sep',
        sprintId: '40248_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintState: 'ACTIVE',
        totalTestCases: 10,
        executedTestCase: 5,
        passedTestCase: 5,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false,automatableTestCases : 13,
        automatedTestCases : 10,
        totalRegressionTestCases : 102,
        automatedRegressionTestCases : 10,
      },
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintName: 'Tools|PI_10|ITR_6|07 Sep',
        sprintId: '38998_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintState: 'ACTIVE',
        totalTestCases: 0,
        executedTestCase: 0,
        passedTestCase: 0,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false,automatableTestCases : 13,
        automatedTestCases : 10,
        totalRegressionTestCases : 102,
        automatedRegressionTestCases : 10,
      },
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintName: 'Tools|PI_10|ITR_5|24 Aug',
        sprintId: '38997_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintState: 'CLOSED',
        totalTestCases: 60,
        executedTestCase: 50,
        passedTestCase: 45,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false,automatableTestCases : 13,
        automatedTestCases : 10,
        totalRegressionTestCases : 102,
        automatedRegressionTestCases : 10,
      },
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintName: 'Tools|PI_10|ITR_4|10 Aug',
        sprintId: '38996_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintState: 'CLOSED',
        totalTestCases: 50,
        executedTestCase: 50,
        passedTestCase: 50,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false,automatableTestCases : 13,
        automatedTestCases : 10,
        totalRegressionTestCases : 102,
        automatedRegressionTestCases : 10,
      },
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintName: 'Tools|PI_10|ITR_3|27 Jul',
        sprintId: '38995_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintState: 'CLOSED',
        totalTestCases: 0,
        executedTestCase: 0,
        passedTestCase: 0,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false,automatableTestCases : 13,
        automatedTestCases : 10,
        totalRegressionTestCases : 102,
        automatedRegressionTestCases : 10,
      },
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintName: 'Support|PI_10|ITR_2|13 Jul',
        sprintId: '39496_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintState: 'CLOSED',
        totalTestCases: 0,
        executedTestCase: 0,
        passedTestCase: 0,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false,automatableTestCases : 13,
        automatedTestCases : 10,
        totalRegressionTestCases : 102,
        automatedRegressionTestCases : 10,
      },
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintName: 'Tools|PI_10|ITR_2|13 Jul',
        sprintId: '38994_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintState: 'CLOSED',
        totalTestCases: 0,
        executedTestCase: 0,
        passedTestCase: 0,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false,automatableTestCases : 13,
        automatedTestCases : 10,
        totalRegressionTestCases : 102,
        automatedRegressionTestCases : 10,
      }
    ]
  };

  

  beforeEach((() => {
    const routes: Routes = [
      { path: 'forget', component: UploadComponent },
    ];

    TestBed.configureTestingModule({
      imports: [
        FormsModule,
        InputSwitchModule,
        ReactiveFormsModule,
        CommonModule,
        RouterTestingModule.withRoutes(routes),
        HttpClientTestingModule,
        NgSelectModule
      ],
      declarations: [UploadComponent, DashboardComponent],
      providers: [HttpService, SharedService, MessageService, GetAuthService
        , { provide: APP_CONFIG, useValue: AppConfig }

      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    })
      .compileComponents();

    fixture = TestBed.createComponent(UploadComponent);
    component = fixture.componentInstance;
    httpService = TestBed.inject(HttpService);
    messageService = TestBed.inject(MessageService);
    httpMock = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  }));


  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('getting uploaded image ', waitForAsync(() => {
    fixture.detectChanges();
    component.getUploadedImage();
    const httpreq = httpMock.match(baseUrl + '/api/file/logo');
    httpreq[0].flush(fakeUploadedImage);
    expect(component.uploadedFile.name).toBe('logo.png');
  }));

  it('Delete uploaded image', waitForAsync(() => {

    component.onDelete();
    const httpreq = httpMock.expectOne(baseUrl + '/api/file/delete');
    httpreq.flush('true');
    expect(component.message).toBe('File deleted successfully');
  }));

  it('check size validation on image', waitForAsync(() => {
    component.logoImage = 'data:image/png;base64,';
    const blob: Blob = new Blob([component.logoImage], { type: 'image/png' });
    component.uploadedFile = new File([blob], 'upload.png', { type: 'image/png' });
    Object.defineProperty(component.uploadedFile, 'size', {
      value: 101 * 1024
    });
    component.validate();
    expect(component.error).toBe('File should not be more than 100 KB');
  }));


  it('check type of validation ', async () => {
    component.logoImage = 'data:image/png;base64,';
    const blob: Blob = new Blob([component.logoImage], { type: 'image/png' });
    component.uploadedFile = new File([blob], 'upload.png', { type: 'image/png' });
    Object.defineProperty(component.uploadedFile, 'size', {
      value: 101 * 1024
    });
    Object.defineProperty(component.uploadedFile, 'name', {
      value: 'upload.tef'
    });
    component.validate();
    expect(component.error).toBe('Only JPG, PNG and GIF files are allowed.');
  });


  it('file should be not be uploaded if size is more ', waitForAsync(() => {
    const event = {
      target: {
        files: File
      }
    };
    component.logoImage = 'data:image/png;base64,';
    const blob: Blob = new Blob([component.logoImage], { type: 'image/png' });
    event.target.files[0] = new File([blob], 'upload.png', { type: 'image/png' });
    Object.defineProperty(event.target.files[0], 'size', {
      value: 101 * 1024
    });
    component.onUpload(event);
    expect(component.invalid).toBeTruthy();
  }));


  it('file should be uploaded if correct size and type ', fakeAsync(() => {
    const event = {
      target: {
        files: File
      }
    };
    component.logoImage = 'data:image/png;base64,';
    const blob: Blob = new Blob([component.logoImage], { type: 'image/png' });
    event.target.files[0] = new File([blob], 'upload.png', { type: 'image/png' });
    Object.defineProperty(event.target.files[0], 'size', {
      value: 99
    });
    Object.defineProperty(event.target.files[0], 'name', {
      value: 'upload.png'
    });
    spyOn(component, 'onSelectImage').and.resolveTo(true);
    component.onUpload(event);
    tick();
    const httpreq = httpMock.expectOne(baseUrl + '/api/file/upload');

    httpreq.flush({ message: 'File uploaded successfully' });
    expect(component.message).toBe('File uploaded successfully');
  }));

  it('file should be uploaded if correct size and type ', fakeAsync(() => {
    const event = {
      target: {
        files: File
      }
    };
    component.logoImage = 'data:image/png;base64,';
    const blob: Blob = new Blob([component.logoImage], { type: 'image/png' });
    event.target.files[0] = new File([blob], 'upload.png', { type: 'image/png' });
    Object.defineProperty(event.target.files[0], 'size', {
      value: 99
    });
    Object.defineProperty(event.target.files[0], 'name', {
      value: 'upload.png'
    });
    spyOn(component, 'onSelectImage').and.resolveTo(true);
    component.onUpload(event);
    tick();
    const httpreq = httpMock.expectOne(baseUrl + '/api/file/upload');
    httpreq.flush(fakeErrorOnUpload);
    expect(component.error).toBe(fakeErrorOnUpload.statusText);
  }));

  // it('checking get filter data on load', (done) => {
  //   component.ngOnInit();
  //   fixture.detectChanges();
  //   component.filter_kpiRequest = null;

  //   component.selectedFilterData = {};
  //   component.selectedFilterCount = 0;
  //   component.selectedFilterData.kanban = false;
  //   component.selectedFilterData['sprintIncluded'] = ['CLOSED', 'ACTIVE'];
  //   component.selectedFilterData = null;
  //   //component.getFilterDataOnLoad();
  //   fixture.detectChanges();
  //   let httpreq = httpMock.expectOne(baseUrl + '/api/filterdata');
  //   httpreq.flush(fakeFilterData);
  //   // fixture.detectChanges();
  //   expect(component.filterData.length).toBe(fakeFilterData.data.length);
  //   done();
  // });

  it('should switch to Kanban', () => {
    component.kanban = true;
    component.kanbanActivation('kanban');
    fixture.detectChanges();
    expect(component.startDate).toBe('');
    expect(component.endDate).toBe('');
    expect(component.executionDate).toBe('');
    expect(component.capacityErrorMessage).toBe('');
    expect(component.testExecutionErrorMessage).toBe('');
    expect(component.isCapacitySaveDisabled).toBeTruthy();
    expect(component.isTestExecutionSaveDisabled).toBeTruthy();
    expect(component.loader).toBeTruthy();
  });

  it('should switch view to Test Execution Percentage', () => {
    const fakeEvent = {
      originalEvent: {
        isTrusted: true
      },
      item: {
        label: 'Test Execution Percentage',
        icon: 'pi pi-pw pi-file',
        expanded: true
      }
    };
    component.switchView(fakeEvent);
    fixture.detectChanges();
    expect(component.selectedView).toBe('upload_tep');
    expect(component.executionDate).toBe('');
    expect(component.testExecutionErrorMessage).toBe('');
    expect(component.kanban).toBeFalse();
  });


  it('should ubmit Test Execution data', () => {
    const event = {
      originalEvent: {
        isTrusted: true
      },
      item: {
        label: 'Test Execution Percentage',
        icon: 'pi pi-pw pi-capacity',
        expanded: true
      }
    };
    component.switchView(event);
    component.reqObj = {
      projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
      projectName: 'DEMO_SONAR',
      kanban: false,
      sprintId: '40248_DEMO_SONAR_63284960fdd20276d60e4df5',
      totalTestCases: '5',
      executedTestCase: '5',
      passedTestCase: '5'
    };
    component.submitTestExecution();
    fixture.detectChanges();
    httpMock.match(baseUrl + '/api/testexecution')[0].flush(fakeSuccessResponseTestExecution);
  });

  

  it('should get test execution data of a selected project', () => {
    const projectId = '63284960fdd20276d60e4df5';
    component.getTestExecutionData(projectId);
    fixture.detectChanges();
    httpMock.match(baseUrl + '/api/testexecution/' + projectId)[0]?.flush(fakeTestExecutionData);
    expect(component.testExecutionScrumData).toBeDefined()
  });

  it('testing Switch View for upload tab', () => {
    const event = {
      originalEvent: {
        isTrusted: true,
      },
      item: {
        label: 'Upload Logo',
        icon: 'pi pi-image',
        expanded: true,
      },
    };
    component.switchView(event);
    fixture.detectChanges();
    expect(component.selectedView).toBe('logo_upload');
  });

  it('All Tabs should visible for superadmin user', () => {
    component.ngOnInit();
    fixture.detectChanges();
    if (component.isSuperAdmin) {
      expect(component.items.length).toBe(3);
    } else {
      expect(component.items.length).toBe(1);
    }
  });

  it('resetProjectSelection()', () => {
    component.resetProjectSelection();
    fixture.detectChanges();
    expect(component.projectListArr.length).toBe(0);
    expect(component.trendLineValueList.length).toBe(0);
  });

  it('getFilterDataOnLoad() success', () => {
    const response = {
      data: [
        {
          basicProjectConfigId: '638f095eaf42db1df033a49a',
          labelName: 'project',
          level: 4,
          nodeId: 'Jenkinsproj_638f095eaf42db1df033a49a',
          nodeName: 'Jenkinsproj',
          parentId: ['Level3_hierarchyLevelThree'],
          path: ['Level3_hierarchyLevelThree###Level2_hierarchyLevelTwo###Level1_hierarchyLevelOne']
        },
      ],
      message: 'fetched successfully',
      success: true,
    };
    spyOn(httpService, 'getFilterData').and.returnValue(of(response));
    component.getFilterDataOnLoad();
    fixture.detectChanges();
    expect(component.projectListArr.length).toBeGreaterThan(0)
  });

  it('getFilterDataOnLoad() Fail', () => {
    const response = ['error'];
    spyOn(httpService, 'getFilterData').and.returnValue(of(response));
    spyOn(component, 'resetProjectSelection')
    component.getFilterDataOnLoad();
    fixture.detectChanges();
    expect(component.resetProjectSelection).toHaveBeenCalled()

  });



  it("enableDisableSubmitButton() when selectedView === 'upload_tep'", () => {
    component.selectedView = 'upload_tep'
    spyOn(component, 'enableDisableTestExecutionSubmitButton')
    component.enableDisableSubmitButton();
    fixture.detectChanges();
    expect(component.enableDisableTestExecutionSubmitButton).toHaveBeenCalled()

  });

 

  xit('should fail on uploading certificate', fakeAsync(() => {
    const errorRes = {
      "message": "LDAP certificate not copied due to some error",
      "success": false,
      "data": "lladldap.hk.net.pem"
    };
    component.selectedFile = new File([""],
      "lladldap.hk.net.pem",
      {
        type: "application/x-x509-ca-cert",
        lastModified: 1678340841040
      }
    )
    component.error = '';
    component.message = '';
    spyOn(httpService, 'uploadCertificate').and.returnValue(of(errorRes));
    // spyOn(component, 'uploadCertificate');
    component.uploadCertificate();
    tick();
    expect(component.error).toEqual(errorRes.message);
  }));
 

  it('should succeed on uploading certificate', fakeAsync(() => {
    const successRes = {
      "message": "LDAP certificate copied",
      "success": true,
      "data": "lladldap.hk.net.cer"
    };
    component.selectedFile = new File([""],
      "lladldap.hk.net.cer",
      {
        type: "application/x-x509-ca-cert",
        lastModified: 1678340841040
      }
    );
    component.error = '';
    component.message = '';
    spyOn(httpService, 'uploadCertificate').and.returnValue(of(successRes));
    // spyOn(component, 'uploadCertificate');
    component.uploadCertificate();
    tick();
    expect(component.message).toEqual(successRes.message);
  }));
  

  it('should clear file uploaded', () => {
    const event = {
      "originalEvent": {
        "isTrusted": true
      },
      "file": {}
    };
    component.selectedFile = null;
    component.clear(event);
    expect(component.isUploadEnabled).toBe(true);
  });

  it('should validate certificate successfully', () => {
    const blob: Blob = new Blob([""], { type: "application/x-x509-ca-cert", });
    const event = {
      "originalEvent": {
        "isTrusted": true
      },
      'files': [new File([blob], 'upload.cer', { type: "application/x-x509-ca-cert", })]
    };
    component.error = '';
    component.message = '';
    component.selectedFile = event.files[0];
    component.validateCertificate(event);
    expect(component.isUploadEnabled).toBe(false);
  })

  it('should switch view for upload certificate tab', () => {
    const event = {
      originalEvent: {
        isTrusted: true,
      },
      item: {
        label: 'Upload certificate',
        icon: 'pi pi-image',
        expanded: true,
      },
    };
    component.switchView(event);
    fixture.detectChanges();
    expect(component.selectedView).toBe('cert_upload');
    expect(component.kanban).toBeFalse();
  });

  it('should enable/disable button based on field group', () => {
    const spy = spyOn(component, 'validateFirstGroupTextCountField')
    component.isAddtionalTestField = false;
    component.enableDisableTestExecutionSubmitButton();
    expect(spy).toBeDefined();

    component.popupForm = new UntypedFormGroup({
      totalTestCases: new UntypedFormControl(''),
      executedTestCase: new UntypedFormControl(''),
      passedTestCase: new UntypedFormControl(''),
      automatedTestCases: new UntypedFormControl(''),
      automatableTestCases: new UntypedFormControl(''),
      automatedRegressionTestCases: new UntypedFormControl(''),
      totalRegressionTestCases: new UntypedFormControl(''),
    });

    component.isAddtionalTestField = true;
    component.popupForm.controls['totalTestCases'].setValue(5);
    component.enableDisableTestExecutionSubmitButton();
    expect(spy).toBeDefined();
  })

  it('should validate fields of first set of group',()=>{
    component.popupForm = new UntypedFormGroup({
      totalTestCases: new UntypedFormControl(''),
      executedTestCase: new UntypedFormControl(''),
      passedTestCase: new UntypedFormControl(''),
      automatedTestCases: new UntypedFormControl(''),
      automatableTestCases: new UntypedFormControl(''),
      automatedRegressionTestCases: new UntypedFormControl(''),
      totalRegressionTestCases: new UntypedFormControl(''),
    });

    component.popupForm.controls['totalTestCases'].setValue(0);
    component.validateFirstGroupTextCountField();
    expect(component.testExecutionErrorMessage).toBeDefined();

    component.popupForm.controls['totalTestCases'].setValue(90);
    component.popupForm.controls['executedTestCase'].setValue(0);
    component.validateFirstGroupTextCountField();
    expect(component.testExecutionErrorMessage).toBeDefined();

    component.popupForm.controls['totalTestCases'].setValue(90);
    component.popupForm.controls['executedTestCase'].setValue(12);
    component.popupForm.controls['passedTestCase'].setValue(0);
    component.validateFirstGroupTextCountField();
    expect(component.testExecutionErrorMessage).toBeDefined();

    component.popupForm.controls['totalTestCases'].setValue(10);
    component.popupForm.controls['executedTestCase'].setValue(12);
    component.popupForm.controls['passedTestCase'].setValue(1);
    component.validateFirstGroupTextCountField();
    expect(component.testExecutionErrorMessage).toBeDefined();

    component.popupForm.controls['totalTestCases'].setValue(90);
    component.popupForm.controls['executedTestCase'].setValue(10);
    component.popupForm.controls['passedTestCase'].setValue(12);
    component.validateFirstGroupTextCountField();
    expect(component.testExecutionErrorMessage).toBeDefined();
  })

  it('should validate fields of second set of group',()=>{
    component.popupForm = new UntypedFormGroup({
      totalTestCases: new UntypedFormControl(''),
      executedTestCase: new UntypedFormControl(''),
      passedTestCase: new UntypedFormControl(''),
      automatedTestCases: new UntypedFormControl(''),
      automatableTestCases: new UntypedFormControl(''),
      automatedRegressionTestCases: new UntypedFormControl(''),
      totalRegressionTestCases: new UntypedFormControl(''),
    });

    component.popupForm.controls['automatedTestCases'].setValue('');
    component.popupForm.controls['automatableTestCases'].setValue(10);
    component.validateSecondGroupTextCountField();
    expect(component.testExecutionErrorMessage).toBeDefined();

    component.popupForm.controls['automatedTestCases'].setValue(10);
    component.popupForm.controls['automatableTestCases'].setValue(10);
    component.popupForm.controls['automatedRegressionTestCases'].setValue('');
    component.popupForm.controls['totalRegressionTestCases'].setValue(10);
    component.validateSecondGroupTextCountField();
    expect(component.testExecutionErrorMessage).toBeDefined();
  })
});
