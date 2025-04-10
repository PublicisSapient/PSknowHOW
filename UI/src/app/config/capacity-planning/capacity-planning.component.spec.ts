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
import {
  ComponentFixture,
  fakeAsync,
  TestBed,
  tick,
  waitForAsync,
} from '@angular/core/testing';
import { CommonModule } from '@angular/common';
import { InputSwitchModule } from 'primeng/inputswitch';
import {
  FormControl,
  ReactiveFormsModule,
  UntypedFormControl,
  UntypedFormGroup,
  FormsModule,
} from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { Routes } from '@angular/router';
import { DashboardComponent } from '../../dashboard/dashboard.component';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { HttpService } from '../../services/http.service';
import { environment } from 'src/environments/environment';
import { APP_CONFIG, AppConfig } from '../../services/app.config';
import { SharedService } from '../../services/shared.service';
import { MessageService } from 'primeng/api';
import { GetAuthService } from '../../services/getauth.service';
import { NgSelectModule } from '@ng-select/ng-select';
import { of } from 'rxjs';
import { ManageAssigneeComponent } from '../manage-assignee/manage-assignee.component';
import { CapacityPlanningComponent } from './capacity-planning.component';
import { GetAuthorizationService } from 'src/app/services/get-authorization.service';
import { HelperService } from 'src/app/services/helper.service';
import { DatePipe } from '@angular/common';
import { ExcelService } from 'src/app/services/excel.service';
import { IsoDateFormatPipe } from 'src/app/shared-module/pipes/iso-date-format.pipe';

describe('CapacityPlanningComponent', () => {
  let fixture: ComponentFixture<CapacityPlanningComponent>;
  const baseUrl = environment.baseUrl;
  let httpMock;
  let httpService;
  let messageService;
  let helperService;
  const fakeSuccessResponseCapacity = {
    message: 'Capacity Data',
    success: true,
    data: [
      {
        id: '632c4e17e23ab66523bdbb22',
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintNodeId: '29732_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintName: 'SprintPrioritization_Bucket',
        sprintState: 'FUTURE',
        capacity: 3,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false,
      },
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintNodeId: '40249_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintName: 'Tools|PI_11|ITR_2|12 Oct',
        sprintState: 'FUTURE',
        capacity: 0,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false,
      },
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintNodeId: '40252_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintName: 'Tools|PI_11|ITR_5| 23 Nov',
        sprintState: 'FUTURE',
        capacity: 0,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false,
      },
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintNodeId: '40253_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintName: 'Tools|PI_11|ITR_6| 07 Dec',
        sprintState: 'FUTURE',
        capacity: 0,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false,
      },
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintNodeId: '40251_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintName: 'Tools|PI_11|ITR_4| 09 Nov',
        sprintState: 'FUTURE',
        capacity: 0,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false,
      },
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintNodeId: '40250_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintName: 'Tools|PI_11|ITR_3|26 Oct',
        sprintState: 'FUTURE',
        capacity: 0,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false,
      },
      {
        id: '633eaf5f17c562439124a872',
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintNodeId: '40248_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintName: 'Tools|PI_11|ITR_1|28 Sep',
        sprintState: 'ACTIVE',
        capacity: 500,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false,
      },
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintNodeId: '38998_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintName: 'Tools|PI_10|ITR_6|07 Sep',
        sprintState: 'ACTIVE',
        capacity: 0,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false,
      },
      {
        id: '63327450dc7db01e674a5379',
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintNodeId: '38997_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintName: 'Tools|PI_10|ITR_5|24 Aug',
        sprintState: 'CLOSED',
        capacity: 520,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false,
      },
      {
        id: '63327449dc7db01e674a5378',
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintNodeId: '38996_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintName: 'Tools|PI_10|ITR_4|10 Aug',
        sprintState: 'CLOSED',
        capacity: 500,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false,
      },
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintNodeId: '38995_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintName: 'Tools|PI_10|ITR_3|27 Jul',
        sprintState: 'CLOSED',
        capacity: 0,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false,
      },
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintNodeId: '39496_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintName: 'Support|PI_10|ITR_2|13 Jul',
        sprintState: 'CLOSED',
        capacity: 0,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false,
      },
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintNodeId: '38994_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintName: 'Tools|PI_10|ITR_2|13 Jul',
        sprintState: 'CLOSED',
        capacity: 0,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false,
      },
    ],
  };

  const fakeCapacityData = {
    message: 'Capacity Data',
    success: true,
    data: [
      {
        id: '632c4e17e23ab66523bdbb22',
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintNodeId: '29732_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintName: 'SprintPrioritization_Bucket',
        sprintState: 'FUTURE',
        capacity: 3,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false,
      },
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintNodeId: '40249_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintName: 'Tools|PI_11|ITR_2|12 Oct',
        sprintState: 'FUTURE',
        capacity: 0,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false,
      },
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintNodeId: '40252_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintName: 'Tools|PI_11|ITR_5| 23 Nov',
        sprintState: 'FUTURE',
        capacity: 0,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false,
      },
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintNodeId: '40253_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintName: 'Tools|PI_11|ITR_6| 07 Dec',
        sprintState: 'FUTURE',
        capacity: 0,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false,
      },
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintNodeId: '40251_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintName: 'Tools|PI_11|ITR_4| 09 Nov',
        sprintState: 'FUTURE',
        capacity: 0,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false,
      },
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintNodeId: '40250_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintName: 'Tools|PI_11|ITR_3|26 Oct',
        sprintState: 'FUTURE',
        capacity: 0,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false,
      },
      {
        id: '633eaf5f17c562439124a872',
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintNodeId: '40248_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintName: 'Tools|PI_11|ITR_1|28 Sep',
        sprintState: 'ACTIVE',
        capacity: 500,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false,
      },
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintNodeId: '38998_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintName: 'Tools|PI_10|ITR_6|07 Sep',
        sprintState: 'ACTIVE',
        capacity: 0,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false,
      },
      {
        id: '63327450dc7db01e674a5379',
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintNodeId: '38997_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintName: 'Tools|PI_10|ITR_5|24 Aug',
        sprintState: 'CLOSED',
        capacity: 520,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false,
      },
      {
        id: '63327449dc7db01e674a5378',
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintNodeId: '38996_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintName: 'Tools|PI_10|ITR_4|10 Aug',
        sprintState: 'CLOSED',
        capacity: 500,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false,
      },
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintNodeId: '38995_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintName: 'Tools|PI_10|ITR_3|27 Jul',
        sprintState: 'CLOSED',
        capacity: 0,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false,
      },
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintNodeId: '39496_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintName: 'Support|PI_10|ITR_2|13 Jul',
        sprintState: 'CLOSED',
        capacity: 0,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false,
      },
      {
        projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
        projectName: 'DEMO_SONAR',
        sprintNodeId: '38994_DEMO_SONAR_63284960fdd20276d60e4df5',
        sprintName: 'Tools|PI_10|ITR_2|13 Jul',
        sprintState: 'CLOSED',
        capacity: 0,
        basicProjectConfigId: '63284960fdd20276d60e4df5',
        kanban: false,
      },
    ],
  };

  const fakeCapacityKanbanData = require('../../../test/resource/fakeCapacityData.json');
  const trendValueList = [
    {
      nodeId: ' Buy & Deliver_651af337d18501286c28a464',
      nodeName: ' Buy & Deliver',
      path: [
        'Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu',
      ],
      labelName: 'project',
      parentId: ['Sonepar SAS_port'],
      level: 5,
      basicProjectConfigId: '651af337d18501286c28a464',
    },
    {
      nodeId: 'AA Data and Reporting_649c00cd1734471c30843d2d',
      nodeName: 'AA Data and Reporting',
      path: [
        'Anglo American Marketing Limited_port###Anglo American Marketing Limited_acc###Energy & Commodities_ver###EU_bu',
      ],
      labelName: 'project',
      parentId: ['Anglo American Marketing Limited_port'],
      level: 5,
      basicProjectConfigId: '649c00cd1734471c30843d2d',
    },
    {
      nodeId: 'ASO Mobile App_64a4fab01734471c30843fda',
      nodeName: 'ASO Mobile App',
      path: ['ASO_port###Academy Sports_acc###Retail_ver###North America_bu'],
      labelName: 'project',
      parentId: ['ASO_port'],
      level: 5,
      basicProjectConfigId: '64a4fab01734471c30843fda',
    },
    {
      nodeId: 'Azure Project_656d830b9f546b19742cb55b',
      nodeName: 'Azure Project',
      path: ['3PP CRM_port###ADEO_acc###B_ver###Europe_bu'],
      labelName: 'project',
      parentId: ['3PP CRM_port'],
      level: 5,
      basicProjectConfigId: '656d830b9f546b19742cb55b',
    },
    {
      nodeId: 'Canada ELD_64f1d426d8d45b13a8de56cb',
      nodeName: 'Canada ELD',
      path: [
        'Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu',
      ],
      labelName: 'project',
      parentId: ['Sunbelt Rentals_port'],
      level: 5,
      basicProjectConfigId: '64f1d426d8d45b13a8de56cb',
    },
    {
      nodeId: 'Cart & checkout_6441078b72a7c53c78f70590',
      nodeName: 'Cart & checkout',
      path: ['API_port###Academy Sports_acc###Retail_ver###North America_bu'],
      labelName: 'project',
      parentId: ['API_port'],
      level: 5,
      basicProjectConfigId: '6441078b72a7c53c78f70590',
    },
    {
      nodeId: 'CCSF Project_64e90e10dd4f0b7e8ed0a5fe',
      nodeName: 'CCSF Project',
      path: [
        'C&C San Francisco - PAS_port###City and County of San Francisco_acc###Financial Services_ver###North America_bu',
      ],
      labelName: 'project',
      parentId: ['C&C San Francisco - PAS_port'],
      level: 5,
      basicProjectConfigId: '64e90e10dd4f0b7e8ed0a5fe',
    },
    {
      nodeId: 'ChangeDateIssue_655e019308f31814845120b3',
      nodeName: 'ChangeDateIssue',
      path: [
        'DTS_port###Methods and Tools_acc###PS Internal_ver###Internal_bu',
      ],
      labelName: 'project',
      parentId: ['DTS_port'],
      level: 5,
      basicProjectConfigId: '655e019308f31814845120b3',
    },
    {
      nodeId: 'CMS_644103e772a7c53c78f70582',
      nodeName: 'CMS',
      path: ['ASO_port###Academy Sports_acc###Retail_ver###North America_bu'],
      labelName: 'project',
      parentId: ['ASO_port'],
      level: 5,
      basicProjectConfigId: '644103e772a7c53c78f70582',
    },
    {
      nodeId: 'Data Engineering_644258b830d86a7f539c7fd7',
      nodeName: 'Data Engineering',
      path: [
        'Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu',
      ],
      labelName: 'project',
      parentId: ['Sunbelt Rentals_port'],
      level: 5,
      basicProjectConfigId: '644258b830d86a7f539c7fd7',
    },
    {
      nodeId: 'Data Visualization_64425a0a30d86a7f539c7fdc',
      nodeName: 'Data Visualization',
      path: [
        'Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu',
      ],
      labelName: 'project',
      parentId: ['Sunbelt Rentals_port'],
      level: 5,
      basicProjectConfigId: '64425a0a30d86a7f539c7fdc',
    },
    {
      nodeId: 'Design System_64ad9e667d51263c17602c67',
      nodeName: 'Design System',
      path: [
        'Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu',
      ],
      labelName: 'project',
      parentId: ['Sonepar SAS_port'],
      level: 5,
      basicProjectConfigId: '64ad9e667d51263c17602c67',
    },
    {
      nodeId: 'DHL Logistics Scrumban_6549029708f3181484511bbb',
      nodeName: 'DHL Logistics Scrumban',
      path: [
        'DPDHL - CSI DCI - Logistics and CJ_port###Deutsche Post AG_acc###Automotive_ver###EU_bu',
      ],
      labelName: 'project',
      parentId: ['DPDHL - CSI DCI - Logistics and CJ_port'],
      level: 5,
      basicProjectConfigId: '6549029708f3181484511bbb',
    },
    {
      nodeId: 'Do it Best_657049b505ce0569d5612ec7',
      nodeName: 'Do it Best',
      path: ['3PP CRM_port###ADEO_acc###Automotive_ver###North America_bu'],
      labelName: 'project',
      parentId: ['3PP CRM_port'],
      level: 5,
      basicProjectConfigId: '657049b505ce0569d5612ec7',
    },
    {
      nodeId: 'Dotcom + Mobile App_64be65cceb7015715615c4ba',
      nodeName: 'Dotcom + Mobile App',
      path: [
        'Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu',
      ],
      labelName: 'project',
      parentId: ['Sunbelt Rentals_port'],
      level: 5,
      basicProjectConfigId: '64be65cceb7015715615c4ba',
    },
    {
      nodeId: 'DRP - Discovery POD_63dc01e47228be4c30553ce1',
      nodeName: 'DRP - Discovery POD',
      path: [
        'Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu',
      ],
      labelName: 'project',
      parentId: ['Retail_port'],
      level: 5,
      basicProjectConfigId: '63dc01e47228be4c30553ce1',
    },
    {
      nodeId: 'DRP - HomePage POD_64b3f315c4e72b57c94035e2',
      nodeName: 'DRP - HomePage POD',
      path: [
        'Retail_port###The Childrens Place, Inc._acc###Retail_ver###North America_bu',
      ],
      labelName: 'project',
      parentId: ['Retail_port'],
      level: 5,
      basicProjectConfigId: '64b3f315c4e72b57c94035e2',
    },
    {
      nodeId: 'Ecom Post-Purchase Squad_64be67caeb7015715615c4c5',
      nodeName: 'Ecom Post-Purchase Squad',
      path: [
        'Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu',
      ],
      labelName: 'project',
      parentId: ['Sunbelt Rentals_port'],
      level: 5,
      basicProjectConfigId: '64be67caeb7015715615c4c5',
    },
    {
      nodeId: 'Ecom Pre-Purchase Squad_64be66e3eb7015715615c4bd',
      nodeName: 'Ecom Pre-Purchase Squad',
      path: [
        'Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu',
      ],
      labelName: 'project',
      parentId: ['Sunbelt Rentals_port'],
      level: 5,
      basicProjectConfigId: '64be66e3eb7015715615c4bd',
    },
    {
      nodeId: 'Ecom Purchase Squad_64be677aeb7015715615c4c1',
      nodeName: 'Ecom Purchase Squad',
      path: [
        'Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu',
      ],
      labelName: 'project',
      parentId: ['Sunbelt Rentals_port'],
      level: 5,
      basicProjectConfigId: '64be677aeb7015715615c4c1',
    },
    {
      nodeId: 'FDFH_656f0b8d275aa91d24a6e568',
      nodeName: 'FDFH',
      path: ['3PP CRM_port###AAA Auto Club Group_acc###Automotive_ver###A_bu'],
      labelName: 'project',
      parentId: ['3PP CRM_port'],
      level: 5,
      basicProjectConfigId: '656f0b8d275aa91d24a6e568',
    },
    {
      nodeId: 'GearBox_63a02b61bbc09e116d744d9d',
      nodeName: 'GearBox',
      path: [
        'Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu',
      ],
      labelName: 'project',
      parentId: ['Sunbelt Rentals_port'],
      level: 5,
      basicProjectConfigId: '63a02b61bbc09e116d744d9d',
    },
    {
      nodeId: 'GearBox Squad 1_6449103b3be37902a3f1ba70',
      nodeName: 'GearBox Squad 1',
      path: [
        'Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu',
      ],
      labelName: 'project',
      parentId: ['Sunbelt Rentals_port'],
      level: 5,
      basicProjectConfigId: '6449103b3be37902a3f1ba70',
    },
    {
      nodeId: 'GearBox Squad 2_64770ec45286e83998a56141',
      nodeName: 'GearBox Squad 2',
      path: [
        'Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu',
      ],
      labelName: 'project',
      parentId: ['Sunbelt Rentals_port'],
      level: 5,
      basicProjectConfigId: '64770ec45286e83998a56141',
    },
    {
      nodeId: 'GearBox Squad 3_64770ef45286e83998a56143',
      nodeName: 'GearBox Squad 3',
      path: [
        'Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu',
      ],
      labelName: 'project',
      parentId: ['Sunbelt Rentals_port'],
      level: 5,
      basicProjectConfigId: '64770ef45286e83998a56143',
    },
    {
      nodeId: 'Import_656edecc053eaf4d3a2b06da',
      nodeName: 'Import',
      path: [
        '3PP Social - Chrysler_port###ADNOC Global Trading Limited_acc###Consumer Products_ver###Internal_bu',
      ],
      labelName: 'project',
      parentId: ['3PP Social - Chrysler_port'],
      level: 5,
      basicProjectConfigId: '656edecc053eaf4d3a2b06da',
    },
    {
      nodeId: 'Integration Services_6377306a175a953a0a49d322',
      nodeName: 'Integration Services',
      path: [
        'Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu',
      ],
      labelName: 'project',
      parentId: ['Sunbelt Rentals_port'],
      level: 5,
      basicProjectConfigId: '6377306a175a953a0a49d322',
    },
    {
      nodeId: 'KN Server_656969922d6d5f774de2e686',
      nodeName: 'KN Server',
      path: [
        '2021 WLP Brand Retainer_port###ADNOC Global Trading Limited_acc###Consumer Products_ver###Government Services_bu',
      ],
      labelName: 'project',
      parentId: ['2021 WLP Brand Retainer_port'],
      level: 5,
      basicProjectConfigId: '656969922d6d5f774de2e686',
    },
    {
      nodeId: 'KYC _63e5fea5ae1aeb593f3395aa',
      nodeName: 'KYC ',
      path: [
        'ENI-Evolutions_port###ENI S.p.A_acc###Energy & Commodities_ver###EU_bu',
      ],
      labelName: 'project',
      parentId: ['ENI-Evolutions_port'],
      level: 5,
      basicProjectConfigId: '63e5fea5ae1aeb593f3395aa',
    },
    {
      nodeId: 'MAP_63a304a909378702f4eab1d0',
      nodeName: 'MAP',
      path: [
        'DTS_port###Methods and Tools_acc###PS Internal_ver###Internal_bu',
      ],
      labelName: 'project',
      parentId: ['DTS_port'],
      level: 5,
      basicProjectConfigId: '63a304a909378702f4eab1d0',
    },
    {
      nodeId: 'Mobile App_637b17b9175a953a0a49d3c2',
      nodeName: 'Mobile App',
      path: [
        'Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu',
      ],
      labelName: 'project',
      parentId: ['Sunbelt Rentals_port'],
      level: 5,
      basicProjectConfigId: '637b17b9175a953a0a49d3c2',
    },
    {
      nodeId: 'My Account_6441081a72a7c53c78f70595',
      nodeName: 'My Account',
      path: ['API_port###Academy Sports_acc###Retail_ver###North America_bu'],
      labelName: 'project',
      parentId: ['API_port'],
      level: 5,
      basicProjectConfigId: '6441081a72a7c53c78f70595',
    },
    {
      nodeId: 'new test_65714b7752bfa01f6cff043d',
      nodeName: 'new test',
      path: [
        '2021 WLP Brand Retainer_port###AAA Auto Club Group_acc###Consumer Products_ver###Europe_bu',
      ],
      labelName: 'project',
      parentId: ['2021 WLP Brand Retainer_port'],
      level: 5,
      basicProjectConfigId: '65714b7752bfa01f6cff043d',
    },
    {
      nodeId: 'Onsite search_644131b98b61fa2477214bf3',
      nodeName: 'Onsite search',
      path: ['ASO_port###Academy Sports_acc###Retail_ver###North America_bu'],
      labelName: 'project',
      parentId: ['ASO_port'],
      level: 5,
      basicProjectConfigId: '644131b98b61fa2477214bf3',
    },
    {
      nodeId: 'P2P_64ca0b8f5fec906dbc18f3c5',
      nodeName: 'P2P',
      path: [
        'Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu',
      ],
      labelName: 'project',
      parentId: ['Sunbelt Rentals_port'],
      level: 5,
      basicProjectConfigId: '64ca0b8f5fec906dbc18f3c5',
    },
    {
      nodeId: 'PIM_64a4e09e1734471c30843fc2',
      nodeName: 'PIM',
      path: ['ASO_port###Academy Sports_acc###Retail_ver###North America_bu'],
      labelName: 'project',
      parentId: ['ASO_port'],
      level: 5,
      basicProjectConfigId: '64a4e09e1734471c30843fc2',
    },
    {
      nodeId: 'POD 16_657065700615235d92401735',
      nodeName: 'POD 16',
      path: [
        'Nissan Core Team - PS_port###Nissan Motor Co. Ltd._acc###Automotive_ver###International_bu',
      ],
      labelName: 'project',
      parentId: ['Nissan Core Team - PS_port'],
      level: 5,
      basicProjectConfigId: '657065700615235d92401735',
    },
    {
      nodeId: 'pod16 2_657219fd78247c3cd726d630',
      nodeName: 'pod16 2',
      path: [
        '2021 WLP Brand Retainer_port###AAA Auto Club Group_acc###Automative1_ver###A_bu',
      ],
      labelName: 'project',
      parentId: ['2021 WLP Brand Retainer_port'],
      level: 5,
      basicProjectConfigId: '657219fd78247c3cd726d630',
    },
    {
      nodeId: 'PORIO_656ee0e5053eaf4d3a2b06eb',
      nodeName: 'PORIO',
      path: [
        '3PP Social - Chrysler_port###AAA Auto Club Group_acc###Consumer Products_ver###EU_bu',
      ],
      labelName: 'project',
      parentId: ['3PP Social - Chrysler_port'],
      level: 5,
      basicProjectConfigId: '656ee0e5053eaf4d3a2b06eb',
    },
    {
      nodeId: 'Promotion Engine BOF_64c178d7eb7015715615c5a6',
      nodeName: 'Promotion Engine BOF',
      path: ['ASO_port###Academy Sports_acc###Retail_ver###North America_bu'],
      labelName: 'project',
      parentId: ['ASO_port'],
      level: 5,
      basicProjectConfigId: '64c178d7eb7015715615c5a6',
    },
    {
      nodeId: 'Promotion Engine TOF_644105f972a7c53c78f7058c',
      nodeName: 'Promotion Engine TOF',
      path: ['ASO_port###Academy Sports_acc###Retail_ver###North America_bu'],
      labelName: 'project',
      parentId: ['ASO_port'],
      level: 5,
      basicProjectConfigId: '644105f972a7c53c78f7058c',
    },
    {
      nodeId: 'PSknowHOW _6527af981704342160f43748',
      nodeName: 'PSknowHOW ',
      path: [
        'DTS_port###Methods and Tools_acc###PS Internal_ver###Internal_bu',
      ],
      labelName: 'project',
      parentId: ['DTS_port'],
      level: 5,
      basicProjectConfigId: '6527af981704342160f43748',
    },
    {
      nodeId: 'R1+ Frontline_647588bc5286e83998a5609c',
      nodeName: 'R1+ Frontline',
      path: [
        'Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu',
      ],
      labelName: 'project',
      parentId: ['Sunbelt Rentals_port'],
      level: 5,
      basicProjectConfigId: '647588bc5286e83998a5609c',
    },
    {
      nodeId: 'R1+ Logistics_648ab6186803f300a9fd2e0e',
      nodeName: 'R1+ Logistics',
      path: [
        'Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu',
      ],
      labelName: 'project',
      parentId: ['Sunbelt Rentals_port'],
      level: 5,
      basicProjectConfigId: '648ab6186803f300a9fd2e0e',
    },
    {
      nodeId: 'R1+ Sales_648ab46f6803f300a9fd2e09',
      nodeName: 'R1+ Sales',
      path: [
        'Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu',
      ],
      labelName: 'project',
      parentId: ['Sunbelt Rentals_port'],
      level: 5,
      basicProjectConfigId: '648ab46f6803f300a9fd2e09',
    },
    {
      nodeId: 'R1F_65253d241704342160f4364a',
      nodeName: 'R1F',
      path: [
        '3PP - Cross Regional_port###ADNOC Global Trading Limited_acc###Consumer Products_ver###Government Services_bu',
      ],
      labelName: 'project',
      parentId: ['3PP - Cross Regional_port'],
      level: 5,
      basicProjectConfigId: '65253d241704342160f4364a',
    },
    {
      nodeId: 'RABO Scrum (New)_64a5dac31734471c30844068',
      nodeName: 'RABO Scrum (New)',
      path: ['AA_port###ADEO_acc###Financial Services_ver###EU_bu'],
      labelName: 'project',
      parentId: ['AA_port'],
      level: 5,
      basicProjectConfigId: '64a5dac31734471c30844068',
    },
    {
      nodeId: 'Retrol_657152fe52bfa01f6cff044f',
      nodeName: 'Retrol',
      path: [
        '2021 WLP Brand Retainer_port###AB Tetra Pak_acc###B_ver###Government Services_bu',
      ],
      labelName: 'project',
      parentId: ['2021 WLP Brand Retainer_port'],
      level: 5,
      basicProjectConfigId: '657152fe52bfa01f6cff044f',
    },
    {
      nodeId: 'RMMO_6392c9225a7c6d3e49b53f19',
      nodeName: 'RMMO',
      path: [
        'Regina Maria Portofolio_port###Regina Maria_acc###Health_ver###EU_bu',
      ],
      labelName: 'project',
      parentId: ['Regina Maria Portofolio_port'],
      level: 5,
      basicProjectConfigId: '6392c9225a7c6d3e49b53f19',
    },
    {
      nodeId: 'SAME_63a4810c09378702f4eab210',
      nodeName: 'SAME',
      path: [
        'ACE20001_port###Abu Dhabi Investment Authority_acc###PS Internal_ver###EU_bu',
      ],
      labelName: 'project',
      parentId: ['ACE20001_port'],
      level: 5,
      basicProjectConfigId: '63a4810c09378702f4eab210',
    },
    {
      nodeId: 'SBR Mulesoft Tribe_645e15429c05c375596bf94b',
      nodeName: 'SBR Mulesoft Tribe',
      path: [
        'Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu',
      ],
      labelName: 'project',
      parentId: ['Sunbelt Rentals_port'],
      level: 5,
      basicProjectConfigId: '645e15429c05c375596bf94b',
    },
    {
      nodeId: 'SEO_64a4e0591734471c30843fc0',
      nodeName: 'SEO',
      path: ['ASO_port###Academy Sports_acc###Retail_ver###North America_bu'],
      labelName: 'project',
      parentId: ['ASO_port'],
      level: 5,
      basicProjectConfigId: '64a4e0591734471c30843fc0',
    },
    {
      nodeId: 'Service & Assets_6494298bca84920b10dddd3b',
      nodeName: 'Service & Assets',
      path: [
        'Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu',
      ],
      labelName: 'project',
      parentId: ['Sunbelt Rentals_port'],
      level: 5,
      basicProjectConfigId: '6494298bca84920b10dddd3b',
    },
    {
      nodeId: 'Sonepar AFS_654135db88c4b8114af77dba',
      nodeName: 'Sonepar AFS',
      path: [
        'Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu',
      ],
      labelName: 'project',
      parentId: ['Sonepar SAS_port'],
      level: 5,
      basicProjectConfigId: '654135db88c4b8114af77dba',
    },
    {
      nodeId: 'Sonepar BUY_6542b42308f31814845119a8',
      nodeName: 'Sonepar BUY',
      path: [
        'Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu',
      ],
      labelName: 'project',
      parentId: ['Sonepar SAS_port'],
      level: 5,
      basicProjectConfigId: '6542b42308f31814845119a8',
    },
    {
      nodeId: 'Sonepar Cloud_6542b82208f31814845119bb',
      nodeName: 'Sonepar Cloud',
      path: [
        'Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu',
      ],
      labelName: 'project',
      parentId: ['Sonepar SAS_port'],
      level: 5,
      basicProjectConfigId: '6542b82208f31814845119bb',
    },
    {
      nodeId: 'Sonepar EAC_6542b3b008f31814845119a0',
      nodeName: 'Sonepar EAC',
      path: [
        'Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu',
      ],
      labelName: 'project',
      parentId: ['Sonepar SAS_port'],
      level: 5,
      basicProjectConfigId: '6542b3b008f31814845119a0',
    },
    {
      nodeId: 'Sonepar eProcurement_6542947f08f3181484511988',
      nodeName: 'Sonepar eProcurement',
      path: [
        'Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu',
      ],
      labelName: 'project',
      parentId: ['Sonepar SAS_port'],
      level: 5,
      basicProjectConfigId: '6542947f08f3181484511988',
    },
    {
      nodeId: 'Sonepar Global_6448a8213be37902a3f1ba45',
      nodeName: 'Sonepar Global',
      path: [
        'Sonepar Client Cost - MC_port###Sonepar SAS_acc###Retail_ver###North America_bu',
      ],
      labelName: 'project',
      parentId: ['Sonepar Client Cost - MC_port'],
      level: 5,
      basicProjectConfigId: '6448a8213be37902a3f1ba45',
    },
    {
      nodeId: 'Sonepar INT_6542b4bb08f31814845119b2',
      nodeName: 'Sonepar INT',
      path: [
        'Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu',
      ],
      labelName: 'project',
      parentId: ['Sonepar SAS_port'],
      level: 5,
      basicProjectConfigId: '6542b4bb08f31814845119b2',
    },
    {
      nodeId: 'Sonepar Int Test_6557c0f708f3181484511ee1',
      nodeName: 'Sonepar Int Test',
      path: [
        'Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu',
      ],
      labelName: 'project',
      parentId: ['Sonepar SAS_port'],
      level: 5,
      basicProjectConfigId: '6557c0f708f3181484511ee1',
    },
    {
      nodeId: 'Sonepar MAP_6542b43f08f31814845119ab',
      nodeName: 'Sonepar MAP',
      path: [
        'Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu',
      ],
      labelName: 'project',
      parentId: ['Sonepar SAS_port'],
      level: 5,
      basicProjectConfigId: '6542b43f08f31814845119ab',
    },
    {
      nodeId: 'Sonepar Mobile App_6448a96c3be37902a3f1ba48',
      nodeName: 'Sonepar Mobile App',
      path: [
        'Sonepar Client Cost - MC_port###Sonepar SAS_acc###Retail_ver###North America_bu',
      ],
      labelName: 'project',
      parentId: ['Sonepar Client Cost - MC_port'],
      level: 5,
      basicProjectConfigId: '6448a96c3be37902a3f1ba48',
    },
    {
      nodeId: 'Sonepar SAF_6542b3da08f31814845119a2',
      nodeName: 'Sonepar SAF',
      path: [
        'Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu',
      ],
      labelName: 'project',
      parentId: ['Sonepar SAS_port'],
      level: 5,
      basicProjectConfigId: '6542b3da08f31814845119a2',
    },
    {
      nodeId: 'Sonepar SRE_6542b86f08f31814845119be',
      nodeName: 'Sonepar SRE',
      path: [
        'Sonepar SAS_port###Sonepar SAS_acc###Retail_ver###North America_bu',
      ],
      labelName: 'project',
      parentId: ['Sonepar SAS_port'],
      level: 5,
      basicProjectConfigId: '6542b86f08f31814845119be',
    },
    {
      nodeId: 'TAP_65696f2b2d6d5f774de2e68a',
      nodeName: 'TAP',
      path: [
        '2021 WLP Brand Retainer_port###ADNOC Global Trading Limited_acc###Consumer Products_ver###Government Services_bu',
      ],
      labelName: 'project',
      parentId: ['2021 WLP Brand Retainer_port'],
      level: 5,
      basicProjectConfigId: '65696f2b2d6d5f774de2e68a',
    },
    {
      nodeId: 'Test rc_657056c668a1225c0126c843',
      nodeName: 'Test rc',
      path: [
        '2021 WLP Brand Retainer_port###AB Tetra Pak_acc###Consumer Products_ver###Europe_bu',
      ],
      labelName: 'project',
      parentId: ['2021 WLP Brand Retainer_port'],
      level: 5,
      basicProjectConfigId: '657056c668a1225c0126c843',
    },
    {
      nodeId: 'Test Serv Asset_647582005286e83998a56096',
      nodeName: 'Test Serv Asset',
      path: [
        'Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu',
      ],
      labelName: 'project',
      parentId: ['Sunbelt Rentals_port'],
      level: 5,
      basicProjectConfigId: '647582005286e83998a56096',
    },
    {
      nodeId: 'TestConn_6571528eeed1d93352754ba4',
      nodeName: 'TestConn',
      path: [
        '3PP CRM_port###AAA Auto Club Group_acc###Automative1_ver###EU_bu',
      ],
      labelName: 'project',
      parentId: ['3PP CRM_port'],
      level: 5,
      basicProjectConfigId: '6571528eeed1d93352754ba4',
    },
    {
      nodeId: 'testkkk_65684655ae2c8767903e75c2',
      nodeName: 'testkkk',
      path: [
        '3PP - Cross Regional_port###AB Tetra Pak_acc###Consumer Products_ver###A_bu',
      ],
      labelName: 'project',
      parentId: ['3PP - Cross Regional_port'],
      level: 5,
      basicProjectConfigId: '65684655ae2c8767903e75c2',
    },
    {
      nodeId: "Unified Commerce - Dan's MVP_64ab97327d51263c17602b58",
      nodeName: "Unified Commerce - Dan's MVP",
      path: [
        'Endeavour Group Pty Ltd_port###Endeavour Group Limited_acc###Retail_ver###International_bu',
      ],
      labelName: 'project',
      parentId: ['Endeavour Group Pty Ltd_port'],
      level: 5,
      basicProjectConfigId: '64ab97327d51263c17602b58',
    },
    {
      nodeId: 'VDOS_63777558175a953a0a49d363',
      nodeName: 'VDOS',
      path: [
        'Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu',
      ],
      labelName: 'project',
      parentId: ['Sunbelt Rentals_port'],
      level: 5,
      basicProjectConfigId: '63777558175a953a0a49d363',
    },
    {
      nodeId: 'VDOS Outside Hauler_647702b25286e83998a56138',
      nodeName: 'VDOS Outside Hauler',
      path: [
        'Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu',
      ],
      labelName: 'project',
      parentId: ['Sunbelt Rentals_port'],
      level: 5,
      basicProjectConfigId: '647702b25286e83998a56138',
    },
    {
      nodeId: 'VDOS Translations_651fe6bb1704342160f43511',
      nodeName: 'VDOS Translations',
      path: [
        'Sunbelt Rentals_port###Sunbelt Rentals_acc###Retail_ver###North America_bu',
      ],
      labelName: 'project',
      parentId: ['Sunbelt Rentals_port'],
      level: 5,
      basicProjectConfigId: '651fe6bb1704342160f43511',
    },
    {
      nodeId: 'Website BOF_644108c072a7c53c78f7059a',
      nodeName: 'Website BOF',
      path: ['API_port###Academy Sports_acc###Retail_ver###North America_bu'],
      labelName: 'project',
      parentId: ['API_port'],
      level: 5,
      basicProjectConfigId: '644108c072a7c53c78f7059a',
    },
    {
      nodeId: 'Website TOF_6441052372a7c53c78f70588',
      nodeName: 'Website TOF',
      path: ['ASO_port###Academy Sports_acc###Retail_ver###North America_bu'],
      labelName: 'project',
      parentId: ['ASO_port'],
      level: 5,
      basicProjectConfigId: '6441052372a7c53c78f70588',
    },
  ];
  let component: CapacityPlanningComponent;

  beforeEach(async () => {
    const routes: Routes = [
      { path: 'forget', component: CapacityPlanningComponent },
    ];

    TestBed.configureTestingModule({
      imports: [
        FormsModule,
        InputSwitchModule,
        ReactiveFormsModule,
        CommonModule,
        RouterTestingModule.withRoutes(routes),
        HttpClientTestingModule,
        NgSelectModule,
      ],
      declarations: [
        CapacityPlanningComponent,
        DashboardComponent,
        IsoDateFormatPipe,
      ],
      providers: [
        HttpService,
        SharedService,
        MessageService,
        GetAuthService,
        DatePipe,
        HelperService,
        ExcelService,
        { provide: APP_CONFIG, useValue: AppConfig },
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(CapacityPlanningComponent);
    component = fixture.componentInstance;
    httpService = TestBed.inject(HttpService);
    messageService = TestBed.inject(MessageService);
    httpMock = TestBed.inject(HttpTestingController);
    helperService = TestBed.inject(HelperService);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('get capacity of a selected project', () => {
    const projectId = '63284960fdd20276d60e4df5';
    component.getCapacityData(projectId);
    fixture.detectChanges();
    httpMock
      .match(baseUrl + '/api/capacity/' + projectId)[0]
      .flush(fakeCapacityData);
    expect(component.capacityScrumData).toEqual(fakeCapacityData['data']);
  });

  it("enableDisableSubmitButton() when selectedView === 'upload_Sprint_Capacity'", () => {
    component.selectedView = 'upload_Sprint_Capacity';
    component.setFormControlValues();
    component.popupForm.get('capacity').setValue('Enter Value');
    spyOn(component, 'enableDisableCapacitySubmitButton');
    component.enableDisableSubmitButton();
    fixture.detectChanges();
    expect(component.enableDisableCapacitySubmitButton).toHaveBeenCalled();
  });

  it('enableDisableCapacitySubmitButton() for capacity', () => {
    component.selectedView = 'upload_Sprint_Capacity';
    component.setFormControlValues();
    component.popupForm.get('capacity').setValue('Enter Value');
    component.enableDisableCapacitySubmitButton();
    fixture.detectChanges();
    expect(component.isCapacitySaveDisabled).toBeTrue();
    expect(component.capacityErrorMessage).toBe('Please enter Capacity');
  });

  it('enableDisableCapacitySubmitButton()', () => {
    component.enableDisableCapacitySubmitButton();
    fixture.detectChanges();
    expect(component.isCapacitySaveDisabled).toBeTrue();
  });

  it('should disable save capacity btn', () => {
    component.enableDisableCapacitySubmitButton();
    fixture.detectChanges();
    expect(component.isCapacitySaveDisabled).toBeTrue();
  });

  it('should submit capacity', () => {
    component.reqObj = {
      projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
      projectName: 'DEMO_SONAR',
      kanban: false,
      sprintNodeId: '40248_DEMO_SONAR_63284960fdd20276d60e4df5',
      capacity: '500',
    };
    component.submitCapacity();
    fixture.detectChanges();
    httpMock
      .match(baseUrl + '/api/capacity')[0]
      .flush(fakeSuccessResponseCapacity);
  });

  it('should get project Assignees for selected project on capacity', () => {
    component.projectJiraAssignees = {};
    let response = {
      message: 'Successfully fetched assignee list',
      success: true,
      data: {
        projectName: 'RS MAP',
        basicProjectConfigId: '63db6583e1b2765622921512',
        assigneeDetailsList: [
          {
            name: 'testName1',
            displayName: 'testDisplayName',
          },
        ],
      },
    };
    spyOn(httpService, 'getJiraProjectAssignee').and.returnValue(of(response));
    component.getCapacityJiraAssignee('63db6583e1b2765622921512');
    fixture.detectChanges();
    expect(component.projectJiraAssignees).toEqual(response.data);
  });

  it('should add or remove users from managelist', () => {
    component.manageAssigneeList = [
      {
        name: 'testDisplayName1',
        displayName: 'testDisplayName1',
        checked: true,
      },
      {
        name: 'testDisplayName2',
        displayName: 'testDisplayName2',
        checked: true,
      },
      {
        name: 'testDisplayName3',
        displayName: 'testDisplayName3',
        checked: false,
      },
    ];

    component.selectedSprintDetails = {
      id: '63e1d151fba71c2bff281502',
      projectNodeId: 'RS MAP_63db6583e1b2765622921512',
      projectName: 'RS MAP',
      sprintNodeId: '41937_RS MAP_63db6583e1b2765622921512',
      sprintName: 'MAP|PI_12|ITR_5',
      sprintState: 'FUTURE',
      capacity: -1,
      basicProjectConfigId: '63db6583e1b2765622921512',
      assigneeCapacity: [
        {
          userId: 'testUserId1',
          userName: 'testUser',
          role: 'BACKEND_DEVELOPER',
          plannedCapacity: 55.5,
          leaves: 0,
        },
        {
          userId: 'testUserId2',
          userName: 'testUser',
          role: 'BACKEND_DEVELOPER',
          plannedCapacity: 15,
          leaves: 0,
        },
        {
          userId: 'testUserId3',
          userName: 'testUser',
          role: 'BACKEND_DEVELOPER',
          plannedCapacity: 20,
          leaves: 0,
        },
      ],
      kanban: false,
      assigneeDetails: true,
    };

    const response = {
      message: 'Successfully added Capacity Data',
      success: true,
      data: {
        id: '63e1d151fba71c2bff281502',
        projectNodeId: 'RS MAP_63db6583e1b2765622921512',
        projectName: 'RS MAP',
        sprintNodeId: '41937_RS MAP_63db6583e1b2765622921512',
        sprintName: 'MAP|PI_12|ITR_5',
        capacity: -1,
        basicProjectConfigId: '63db6583e1b2765622921512',
        assigneeCapacity: [
          {
            userId: 'testUserId4',
            userName: 'testUser',
            role: 'BACKEND_DEVELOPER',
            plannedCapacity: 55.5,
            leaves: 0,
          },
          {
            userId: 'testUserId5',
            userName: 'testUser',
            role: 'BACKEND_DEVELOPER',
            plannedCapacity: 15,
            leaves: 0,
          },
        ],
        kanban: false,
        assigneeDetails: true,
      },
    };

    spyOn(httpService, 'saveOrUpdateAssignee').and.returnValue(of(response));
    const getCapacityDataSpy = spyOn(component, 'getCapacityData');
    component.kanban = true;
    component.addRemoveAssignees();
    fixture.detectChanges();
    expect(getCapacityDataSpy).toHaveBeenCalled();
    expect(component.expandedRows).toBeTruthy();
  });

  it('should get assignee roles if already not available', () => {
    component.projectAssigneeRoles = [];
    const response = {
      message: 'All Roles',
      success: true,
      data: {
        TESTER: 'Tester',
        FRONTEND_DEVELOPER: 'Frontend Developer',
        BACKEND_DEVELOPER: 'Backend Developer',
      },
    };
    spyOn(httpService, 'getAssigneeRoles').and.returnValue(of(response));
    component.getAssigneeRoles();
    fixture.detectChanges();
    expect(component.projectAssigneeRoles.length).toEqual(3);
  });

  it('should check if assignee toggle enabled', () => {
    const capacityData = [
      {
        projectNodeId: 'Testproject124_63e4b169fba71c2bff2815ba',
        projectName: 'Testproject124',
        sprintNodeId: '41411_Testproject124_63e4b169fba71c2bff2815ba',
        sprintName: 'KnowHOW | PI_12| ITR_5',
        sprintState: 'FUTURE',
        capacity: 0,
        basicProjectConfigId: '63e4b169fba71c2bff2815ba',
        kanban: false,
        assigneeDetails: true,
      },
    ];
    const getAssigneeRolesSpy = spyOn(component, 'getAssigneeRoles');
    const getCapacityJiraAssignee = spyOn(component, 'getCapacityJiraAssignee');
    component.checkifAssigneeToggleEnabled(capacityData);
    expect(component.isToggleEnableForSelectedProject).toBeTruthy();
    expect(getAssigneeRolesSpy).toHaveBeenCalled();
    expect(getCapacityJiraAssignee).toHaveBeenCalledWith(
      '63e4b169fba71c2bff2815ba',
    );
  });

  it('should validate plannedCapacity and leaves field value and calculate available capacity', () => {
    const assignee = {
      userId: 'testUserId6',
      userName: 'testUser',
      leaves: 0,
    };
    const assigneeFormControls = {
      role: new FormControl('TESTER'),
      plannedCapacity: new FormControl({ value: '', disabled: true }),
      leaves: new FormControl({ value: 0, disabled: true }),
    };

    component.calculateAvaliableCapacity(
      assignee,
      assigneeFormControls,
      'role',
    );
    fixture.detectChanges();
    expect(assigneeFormControls.plannedCapacity.status).toEqual('VALID');

    assigneeFormControls.plannedCapacity.setValue('40');
    component.calculateAvaliableCapacity(
      assignee,
      assigneeFormControls,
      'plannedCapacity',
    );
    expect(assignee['availableCapacity']).toEqual(40);

    assigneeFormControls.plannedCapacity.setValue('0');
    component.calculateAvaliableCapacity(
      assignee,
      assigneeFormControls,
      'plannedCapacity',
    );
    expect(assignee['availableCapacity']).toEqual(0);

    assigneeFormControls.plannedCapacity.setValue('40');
    component.calculateAvaliableCapacity(
      assignee,
      assigneeFormControls,
      'plannedCapacity',
    );
    expect(assignee['availableCapacity']).toEqual(40);

    component.selectedSprintAssigneValidator = [];
    assigneeFormControls.plannedCapacity.setValue('40');
    assigneeFormControls.leaves.setValue(41);
    component.calculateAvaliableCapacity(
      assignee,
      assigneeFormControls,
      'leaves',
    );
    expect(component.selectedSprintAssigneValidator.length).toEqual(1);

    assigneeFormControls.leaves.setValue(40);
    component.calculateAvaliableCapacity(
      assignee,
      assigneeFormControls,
      'leaves',
    );
    expect(component.selectedSprintAssigneValidator.length).toEqual(0);
  });

  it('should reset filter on manage Assignee table on modal open', () => {
    component.manageAssignee = new ManageAssigneeComponent();
    const manageAssigneeResetSpy = spyOn(component.manageAssignee, 'reset');
    component.onAssigneeModalOpen();
    expect(manageAssigneeResetSpy).toHaveBeenCalled();
  });

  it('should calculate total capacity for sprint', () => {
    const selectedSprint = {
      id: '63e092edfba71c2bff2814b4',
      projectNodeId: 'RS MAP_63db6583e1b2765622921512',
      projectName: 'RS MAP',
      sprintNodeId: '41935_RS MAP_63db6583e1b2765622921512',
      sprintName: 'MAP|PI_12|ITR_3',
      sprintState: 'CLOSED',
      capacity: 71,
      basicProjectConfigId: '63db6583e1b2765622921512',
      assigneeCapacity: [
        {
          userId: 'testUserId7',
          userName: 'testUser',
          role: 'TESTER',
          plannedCapacity: 40,
          leaves: 0,
          availableCapacity: 40,
        },
        {
          userId: 'testUserId8',
          userName: 'testUser',
          role: 'FRONTEND_DEVELOPER',
          plannedCapacity: 34,
          leaves: 3,
          availableCapacity: 31,
        },
        {
          userId: 'testUserId9',
          userName: 'testUser',
          leaves: 0,
        },
      ],
      kanban: false,
      assigneeDetails: true,
    };
    expect(component.calculateTotalCapacityForSprint(selectedSprint)).toEqual(
      71,
    );
  });

  it('should initialize selectedSprintAssigneFormArray when edit is clicked on sprint', () => {
    const selectedSprint = {
      id: '63e092edfba71c2bff2814b4',
      projectNodeId: 'RS MAP_63db6583e1b2765622921512',
      projectName: 'RS MAP',
      sprintNodeId: '41935_RS MAP_63db6583e1b2765622921512',
      sprintName: 'MAP|PI_12|ITR_3',
      sprintState: 'CLOSED',
      capacity: 71,
      basicProjectConfigId: '63db6583e1b2765622921512',
      assigneeCapacity: [
        {
          userId: 'testUserId10',
          userName: 'testUser',
          role: 'TESTER',
          plannedCapacity: 40,
          leaves: 0,
          availableCapacity: 40,
        },
        {
          userId: 'testUserId11',
          userName: 'testUser',
          role: 'FRONTEND_DEVELOPER',
          plannedCapacity: 34,
          leaves: 3,
          availableCapacity: 31,
        },
        {
          userId: 'testUserId12',
          userName: 'testUser',
          leaves: 0,
        },
      ],
      kanban: false,
      assigneeDetails: true,
    };
    component.onSprintCapacityEdit(selectedSprint);
    expect(component.selectedSprintAssigneFormArray.length).toEqual(3);
  });

  it('should save the sprint capacity details on click of save', () => {
    const selectedSprint = {
      projectNodeId: 'TestProject123_63d8bca4af279c1d507cb8b0',
      projectName: 'TestProject123',
      sprintNodeId: '40699_TestProject123_63d8bca4af279c1d507cb8b0',
      sprintName: 'PS HOW |PI_11|ITR_6|07_Dec',
      sprintState: 'CLOSED',
      capacity: 0,
      basicProjectConfigId: '63d8bca4af279c1d507cb8b0',
      assigneeCapacity: [
        {
          userId: 'testUserId13',
          userName: 'testUser',
          role: 'TESTER',
          plannedCapacity: 40,
          leaves: 0,
        },
      ],
      kanban: false,
      assigneeDetails: true,
    };

    const response = {
      message: 'Successfully added Capacity Data',
      success: true,
      data: {
        projectNodeId: 'TestProject123_63d8bca4af279c1d507cb8b0',
        sprintNodeId: '40699_TestProject123_63d8bca4af279c1d507cb8b0',
        sprintName: 'PS HOW |PI_11|ITR_6|07_Dec',
        capacity: 0,
        basicProjectConfigId: '63d8bca4af279c1d507cb8b0',
        assigneeCapacity: [
          {
            userId: 'testUserId14',
            userName: 'testUser',
            role: 'TESTER',
            plannedCapacity: 40,
            leaves: 0,
          },
        ],
        kanban: false,
        assigneeDetails: true,
      },
    };
    component.kanban = true;

    spyOn(httpService, 'saveOrUpdateAssignee').and.returnValue(of(response));
    let getCapacityDataSpy = spyOn(component, 'getCapacityData');
    component.onSprintCapacitySave(selectedSprint);

    fixture.detectChanges();
    expect(getCapacityDataSpy).toHaveBeenCalled();
  });

  it('should send sprint happiness index', () => {
    const selectedSprint = {
      projectNodeId: 'TestProject123_63d8bca4af279c1d507cb8b0',
      projectName: 'TestProject123',
      sprintNodeId: '40699_TestProject123_63d8bca4af279c1d507cb8b0',
      sprintName: 'PS HOW |PI_11|ITR_6|07_Dec',
      sprintState: 'CLOSED',
      capacity: 0,
      basicProjectConfigId: '63d8bca4af279c1d507cb8b0',
      assigneeCapacity: [
        {
          userId: 'testUserId13',
          happinessRating: 2,
          userName: 'testUser',
          role: 'TESTER',
          plannedCapacity: 40,
          leaves: 0,
        },
      ],
      kanban: false,
      assigneeDetails: true,
    };

    const response = {
      message: 'Successfully added Capacity Data',
      success: true,
      data: {
        projectNodeId: 'TestProject123_63d8bca4af279c1d507cb8b0',
        sprintNodeId: '40699_TestProject123_63d8bca4af279c1d507cb8b0',
        sprintName: 'PS HOW |PI_11|ITR_6|07_Dec',
        capacity: 0,
        basicProjectConfigId: '63d8bca4af279c1d507cb8b0',
        assigneeCapacity: [
          {
            userId: 'testUserId14',
            happinessRating: 2,
            userName: 'testUser',
            role: 'TESTER',
            plannedCapacity: 40,
            leaves: 0,
          },
        ],
        kanban: false,
        assigneeDetails: true,
      },
    };

    spyOn(httpService, 'saveOrUpdateSprintHappinessIndex').and.returnValue(
      of(response),
    );
    let getCapacityDataSpy = spyOn(component, 'getCapacityData');
    component.sendSprintHappinessIndex(selectedSprint);

    fixture.detectChanges();
    expect(getCapacityDataSpy).toHaveBeenCalled();
  });

  it('should reset  to old values when clicked on cancel btn on selected sprint', () => {
    const selectedSprint = {
      id: '63e4c5b4fba71c2bff2815d8',
      projectNodeId: 'TestProject123_63d8bca4af279c1d507cb8b0',
      projectName: 'TestProject123',
      sprintNodeId: '41963_TestProject123_63d8bca4af279c1d507cb8b0',
      sprintName: 'PS HOW |PI_12|ITR_3|25_Jan',
      sprintState: 'CLOSED',
      capacity: 28,
      basicProjectConfigId: '63d8bca4af279c1d507cb8b0',
      assigneeCapacity: [
        {
          userId: 'testUserId15',
          userName: 'testUser',
          role: 'TESTER',
          plannedCapacity: 40,
          leaves: 12,
          availableCapacity: 28,
        },
      ],
      kanban: false,
      assigneeDetails: true,
    };

    component.capacityScrumData = [
      {
        id: '63e4c5b4fba71c2bff2815d8',
        projectNodeId: 'TestProject123_63d8bca4af279c1d507cb8b0',
        projectName: 'TestProject123',
        sprintNodeId: '41963_TestProject123_63d8bca4af279c1d507cb8b0',
        sprintName: 'PS HOW |PI_12|ITR_3|25_Jan',
        sprintState: 'CLOSED',
        capacity: 28,
        basicProjectConfigId: '63d8bca4af279c1d507cb8b0',
        assigneeCapacity: [
          {
            userId: 'testUserId16',
            userName: 'testUser',
            role: 'TESTER',
            plannedCapacity: 40,
            leaves: 12,
            availableCapacity: 28,
          },
        ],
        kanban: false,
        assigneeDetails: true,
      },
    ];

    component.selectedSprint = {
      id: '63e4c5b4fba71c2bff2815d8',
      projectNodeId: 'TestProject123_63d8bca4af279c1d507cb8b0',
      projectName: 'TestProject123',
      sprintNodeId: '41963_TestProject123_63d8bca4af279c1d507cb8b0',
      sprintName: 'PS HOW |PI_12|ITR_3|25_Jan',
      sprintState: 'CLOSED',
      capacity: 28,
      basicProjectConfigId: '63d8bca4af279c1d507cb8b0',
      assigneeCapacity: [
        {
          userId: 'testUserId17',
          userName: 'testUser',
          role: 'FRONTEND_DEVELOPER',
          plannedCapacity: 40,
          leaves: 12,
          availableCapacity: 28,
        },
      ],
      kanban: false,
      assigneeDetails: true,
    };

    component.kanban = false;
    component.onSprintCapacityCancel(selectedSprint);
    expect(component.capacityScrumData[0]).toEqual(component.selectedSprint);
  });

  it('should set edit mode to false on sprint row selection', () => {
    component.projectCapacityEditMode = true;
    component.selectedSprint = {
      id: '63e4c5b4fba71c2bff2815d8',
      projectNodeId: 'TestProject123_63d8bca4af279c1d507cb8b0',
      projectName: 'TestProject123',
      sprintNodeId: '41963_TestProject123_63d8bca4af279c1d507cb8b0',
      sprintName: 'PS HOW |PI_12|ITR_3|25_Jan',
      sprintState: 'CLOSED',
      capacity: 28,
      basicProjectConfigId: '63d8bca4af279c1d507cb8b0',
      assigneeCapacity: [
        {
          userId: 'testUserId18',
          userName: 'testUser',
          role: 'FRONTEND_DEVELOPER',
          plannedCapacity: 40,
          leaves: 12,
          availableCapacity: 28,
        },
      ],
      kanban: false,
      assigneeDetails: true,
    };

    const onSprintCapacityCancelSpy = spyOn(
      component,
      'onSprintCapacityCancel',
    );
    component.onCapacitySprintRowSelection();
    expect(component.projectCapacityEditMode).toBeFalse();
    expect(onSprintCapacityCancelSpy).toHaveBeenCalled();
  });

  it('should set NoData to true on response for capacity data', () => {
    spyOn(httpService, 'getCapacityData').and.returnValue(of({}));
    component.getCapacityData('TestProject123_63d8bca4af279c1d507cb8b0');
    fixture.detectChanges();
    expect(component.tableLoader).toBeFalse();
    expect(component.noData).toBeTrue();
  });

  it('should set capactiy Data for Kanban', () => {
    const projectId = 'testproj2_63d912d2af279c1d507cb93a';
    component.kanban = true;
    const response = {
      message: 'Capacity Data',
      success: true,
      data: [
        {
          projectNodeId: 'testproj2_63d912d2af279c1d507cb93a',
          projectName: 'testproj2',
          capacity: 0,
          startDate: '2023-01-09',
          endDate: '2023-01-15',
          basicProjectConfigId: '63d912d2af279c1d507cb93a',
          kanban: true,
          assigneeDetails: true,
        },
      ],
    };
    spyOn(component, 'checkifAssigneeToggleEnabled');
    spyOn(httpService, 'getCapacityData').and.returnValue(of(response));
    component.getCapacityData(projectId);
    fixture.detectChanges();
    expect(component.capacityKanbanData.length).toEqual(1);
  });

  it('should show assignee modal on manage User btn click', () => {
    const selectedSprint = {
      projectNodeId: 'RS MAP_63db6583e1b2765622921512',
      projectName: 'RS MAP',
      sprintNodeId: '41937_RS MAP_63db6583e1b2765622921512',
      sprintName: 'MAP|PI_12|ITR_5',
      sprintState: 'FUTURE',
      capacity: 0,
      basicProjectConfigId: '63db6583e1b2765622921512',
      assigneeCapacity: [],
      kanban: false,
      assigneeDetails: true,
    };
    spyOn(component, 'generateManageAssigneeData');
    component.manageAssignees(selectedSprint);
    expect(component.displayAssignee).toBeTrue();
  });

  it('should show error message on get project Assignees api fail', () => {
    component.projectJiraAssignees = {};
    const response = {};
    spyOn(httpService, 'getJiraProjectAssignee').and.returnValue(of(response));
    const messageServiceSpy = spyOn(messageService, 'add');
    component.getCapacityJiraAssignee('63db6583e1b2765622921512');
    fixture.detectChanges();
    expect(messageServiceSpy).toHaveBeenCalled();
  });

  it('should generate manage Assignee list data with Selected user on top', () => {
    const selectedSprint = {
      id: '63e0a78bfba71c2bff2814bf',
      projectNodeId: 'RS MAP_63db6583e1b2765622921512',
      projectName: 'RS MAP',
      sprintNodeId: '41938_RS MAP_63db6583e1b2765622921512',
      sprintName: 'MAP|PI_12|ITR_6',
      sprintState: 'FUTURE',
      capacity: 41,
      basicProjectConfigId: '63db6583e1b2765622921512',
      assigneeCapacity: [
        {
          userId: 'userId',
          userName: 'testUser',
          role: 'BACKEND_DEVELOPER',
          plannedCapacity: 55.5,
          leaves: 0,
        },
      ],
      kanban: false,
      assigneeDetails: true,
    };

    component.projectJiraAssignees = {
      basicProjectConfigId: '63db6583e1b2765622921512',
      projectName: 'RS MAP',
      assigneeDetailsList: [
        {
          name: 'testDisplayName1',
          displayName: 'testDisplayName',
        },
        {
          name: 'userId',
          displayName: 'testDisplayName',
        },
      ],
    };
    component.generateManageAssigneeData(selectedSprint);
    expect(component.manageAssigneeList[0].name).toEqual('userId');
  });

  describe('resetProjectSelection', () => {
    it('should reset projectListArr', () => {
      component.projectListArr = [{ nodeId: 1 }, { nodeId: 2 }, { nodeId: 3 }];
      component.resetProjectSelection();
      expect(component.projectListArr).toEqual([]);
    });

    it('should reset trendLineValueList', () => {
      component.trendLineValueList = [1, 2, 3];
      component.resetProjectSelection();
      expect(component.trendLineValueList).toEqual([]);
    });

    it('should reset selectedProjectValue in filterForm', () => {
      component.filterForm.get('selectedProjectValue').setValue('test');
      component.resetProjectSelection();
      expect(component.filterForm.get('selectedProjectValue').value).toEqual(
        '',
      );
    });
  });

  describe('kanbanActivation', () => {
    it('should reset various properties', () => {
      component.startDate = 'test';
      component.endDate = 'test';
      component.executionDate = 'test';
      component.capacityErrorMessage = 'test';
      component.isCapacitySaveDisabled = false;
      component.loader = false;
      component.tableLoader = false;
      component.noData = true;
      component.capacityKanbanData = ['test'];
      component.capacityScrumData = ['test'];
      component.projectDetails = { test: 'test' };
      component.selectedProjectBaseConfigId = 'test';
      spyOn(component, 'getFilterDataOnLoad');
      component.kanbanActivation('scrum');
      expect(component.startDate).toEqual('');
      expect(component.endDate).toEqual('');
      expect(component.executionDate).toEqual('');
      expect(component.capacityErrorMessage).toEqual('');
      expect(component.isCapacitySaveDisabled).toBeTrue();
      expect(component.loader).toBeTrue();
      expect(component.tableLoader).toBeTrue();
      expect(component.noData).toBeFalse();
      expect(component.capacityKanbanData).toEqual([]);
      expect(component.capacityScrumData).toEqual([]);
      expect(component.projectDetails).toEqual({});
      expect(component.selectedProjectBaseConfigId).toEqual('');
      expect(component.getFilterDataOnLoad).toHaveBeenCalled();
    });
  });

  describe('numericInputUpDown', () => {
    it('should call enableDisableSubmitButton if the input value is greater than or equal to 0', () => {
      const event = { target: { value: '1', name: 'test' } };
      component.enableDisableSubmitButton = jasmine.createSpy();
      component.numericInputUpDown(event);
      expect(component.enableDisableSubmitButton).toHaveBeenCalled();
    });

    it('should call enableDisableSubmitButton if the input value is less than 0', () => {
      const event = { target: { value: '-1', name: 'test' } };
      component.enableDisableSubmitButton = jasmine.createSpy();
      component.numericInputUpDown(event);
      expect(component.enableDisableSubmitButton).not.toHaveBeenCalled();
    });
  });

  describe('checkDefaultFilterSelection', () => {
    it('should call getProjectBasedData if flag is false', () => {
      component.getProjectBasedData = jasmine.createSpy();
      component.checkDefaultFilterSelection(false);
      expect(component.getProjectBasedData).toHaveBeenCalled();
    });
  });

  describe('AddOrUpdateData', () => {
    beforeEach(() => {
      component.showPopuup = false;
      component.executionDate = '';
      component.selectedSprintName = '';
      component.selectedSprintId = '';
      component.startDate = '';
      component.endDate = '';
      component.reqObj = {
        projectNodeId: '',
        projectName: '',
        sprintNodeId: '',
        capacity: '',
        startDate: '',
        endDate: '',
        totalTestCases: '',
        executedTestCase: '',
        passedTestCase: '',
        sprintId: '',
        sprintName: '',
        executionDate: '',
        kanban: false,
        basicProjectConfigId: '',
      };
      component.kanban = false;
      component.selectedView = '';
      component.popupForm = null;
      component.enableDisableSubmitButton = jasmine.createSpy();
    });

    it('should update the component properties and call enableDisableSubmitButton', () => {
      const data = {
        executionDate: '2021-08-01',
        sprintName: 'Sprint 1',
        sprintNodeId: 'node1',
        startDate: '2021-07-01',
        endDate: '2021-07-14',
        projectNodeId: 'proj1',
        projectName: 'Project 1',
        basicProjectConfigId: 'config1',
      };
      component.AddOrUpdateData(data);
      expect(component.showPopuup).toBeTrue();
      expect(component.executionDate).toEqual('2021-08-01');
      expect(component.selectedSprintName).toEqual('Sprint 1');
      expect(component.selectedSprintId).toEqual('node1');
      expect(component.startDate).toEqual('2021-07-01');
      expect(component.endDate).toEqual('2021-07-14');
      expect(component.reqObj).toEqual({
        projectNodeId: 'proj1',
        projectName: 'Project 1',
        kanban: false,
        basicProjectConfigId: 'config1',
        sprintNodeId: 'node1',
      });
      expect(component.enableDisableSubmitButton).toHaveBeenCalled();
    });

    xit('should update the reqObj with empty sprintNodeId if kanban is true', () => {
      const data = {
        executionDate: '2021-08-01',
        sprintName: 'Sprint 1',
        sprintNodeId: 'node1',
        startDate: '2021-07-01',
        endDate: '2021-07-14',
        projectNodeId: 'proj1',
        projectName: 'Project 1',
        basicProjectConfigId: 'config1',
      };
      component.kanban = true;
      component.AddOrUpdateData(data);
      expect(component.reqObj).toEqual({
        projectNodeId: 'proj1',
        projectName: 'Project 1',
        kanban: true,
        basicProjectConfigId: 'config1',
        sprintNodeId: '',
        startDate: '2021-07-01',
        endDate: '2021-07-14',
      });
      expect(component.enableDisableSubmitButton).toHaveBeenCalled();
    });

    it('should create a new popupForm with empty capacity if data capacity is not provided', () => {
      const data = {
        executionDate: '2021-08-01',
        sprintName: 'Sprint 1',
        sprintNodeId: 'node1',
        startDate: '2021-07-01',
        endDate: '2021-07-14',
        projectNodeId: 'proj1',
        projectName: 'Project 1',
        basicProjectConfigId: 'config1',
      };
      component.selectedView = 'upload_Sprint_Capacity';
      component.AddOrUpdateData(data);
      expect(component.popupForm).toBeTruthy();
      expect(component.popupForm.controls.capacity.value).toEqual('');
      expect(component.reqObj.capacity).toEqual('');
      expect(component.enableDisableSubmitButton).toHaveBeenCalled();
    });

    it('should update the reqObj with capacity if data capacity is provided', () => {
      const data = {
        executionDate: '2021-08-01',
        sprintName: 'Sprint 1',
        sprintNodeId: 'node1',
        startDate: '2021-07-01',
        endDate: '2021-07-14',
        projectNodeId: 'proj1',
        projectName: 'Project 1',
        basicProjectConfigId: 'config1',
        capacity: '10',
      };
      component.selectedView = 'upload_Sprint_Capacity';
      component.AddOrUpdateData(data);
      expect(component.popupForm).toBeTruthy();
      expect(component.popupForm.controls.capacity.value).toEqual('10');
      expect(component.reqObj.capacity).toEqual('10');
      expect(component.enableDisableSubmitButton).toHaveBeenCalled();
    });

    xit('should update the reqObj with startDate and endDate if kanban is true and selectedView is "upload_Sprint_Capacity"', () => {
      const data = {
        executionDate: '2021-08-01',
        sprintName: 'Sprint 1',
        sprintNodeId: 'node1',
        startDate: '2021-07-01',
        endDate: '2021-07-14',
        projectNodeId: 'proj1',
        projectName: 'Project 1',
        basicProjectConfigId: 'config1',
        capacity: '10',
      };
      component.kanban = true;
      component.selectedView = 'upload_Sprint_Capacity';
      component.AddOrUpdateData(data);
      expect(component.reqObj).toEqual({
        projectNodeId: 'proj1',
        projectName: 'Project 1',
        kanban: true,
        basicProjectConfigId: 'config1',
        sprintNodeId: '',
        startDate: '2021-07-01',
        endDate: '2021-07-14',
        capacity: '10',
      });
      expect(component.enableDisableSubmitButton).toHaveBeenCalled();
    });
  });

  describe('enterNumericValue', () => {
    beforeEach(() => {
      component.enableDisableSubmitButton = jasmine.createSpy();
    });

    it('should prevent default and not call enableDisableSubmitButton if key is "."', () => {
      const event = {
        preventDefault: jasmine.createSpy(),
        key: '.',
      };
      component.enterNumericValue(event);
      expect(event.preventDefault).toHaveBeenCalled();
      expect(component.enableDisableSubmitButton).not.toHaveBeenCalled();
    });

    it('should prevent default and not call enableDisableSubmitButton if key is "e"', () => {
      const event = {
        preventDefault: jasmine.createSpy(),
        key: 'e',
      };
      component.enterNumericValue(event);
      expect(event.preventDefault).toHaveBeenCalled();
      expect(component.enableDisableSubmitButton).not.toHaveBeenCalled();
    });

    it('should prevent default and not call enableDisableSubmitButton if key is "-"', () => {
      const event = {
        preventDefault: jasmine.createSpy(),
        key: '-',
      };
      component.enterNumericValue(event);
      expect(event.preventDefault).toHaveBeenCalled();
      expect(component.enableDisableSubmitButton).not.toHaveBeenCalled();
    });

    it('should prevent default and not call enableDisableSubmitButton if key is "+"', () => {
      const event = {
        preventDefault: jasmine.createSpy(),
        key: '+',
      };
      component.enterNumericValue(event);
      expect(event.preventDefault).toHaveBeenCalled();
      expect(component.enableDisableSubmitButton).not.toHaveBeenCalled();
    });

    it('should call enableDisableSubmitButton if key is not ".", "e", "-", or "+"', () => {
      const event = {
        preventDefault: jasmine.createSpy(),
        key: '1',
      };
      component.enterNumericValue(event);
      expect(event.preventDefault).not.toHaveBeenCalled();
      expect(component.enableDisableSubmitButton).toHaveBeenCalled();
    });
  });

  describe('getFilterDataOnLoad', () => {
    beforeEach(() => {
      component.filter_kpiRequest = null;
      component.selectedFilterData = {};
      component.selectedFilterCount = 0;
      component.kanban = false;
      component.selectedProjectBaseConfigId = '';
      component.filterData = null;
      component.projectListArr = null;
      component.loader = false;
    });

    it('should unsubscribe from filter_kpiRequest if it is not null', () => {
      const unsubscribeSpy = jasmine.createSpy();
      component.filter_kpiRequest = { unsubscribe: unsubscribeSpy };
      component.getFilterDataOnLoad();
      expect(unsubscribeSpy).toHaveBeenCalled();
    });

    it('should set selectedFilterData and selectedFilterCount', () => {
      component.filter_kpiRequest = '';
      component.getFilterDataOnLoad();
      expect(component.selectedFilterData).toEqual({
        kanban: false,
        sprintIncluded: ['CLOSED', 'ACTIVE', 'FUTURE'],
      });
      expect(component.selectedFilterCount).toBe(0);
    });

    it('should call http_service.getFilterData and handle successful response', () => {
      component.filter_kpiRequest = '';
      const filterData = {
        data: [
          { labelName: 'Project', value: 'Project 1' },
          { labelName: 'Project', value: 'Project 2' },
        ],
      };
      spyOn(httpService, 'getFilterData').and.returnValue(of(filterData));
      spyOn(component, 'sortAlphabetically').and.returnValue(filterData.data);
      spyOn(helperService, 'makeUniqueArrayList').and.returnValue(
        filterData.data,
      );
      spyOn(component, 'checkDefaultFilterSelection');
      spyOn(component, 'resetProjectSelection');
      spyOn(messageService, 'add');
      component.getFilterDataOnLoad();
      expect(httpService.getFilterData).toHaveBeenCalledWith(
        component.selectedFilterData,
      );
      expect(component.filterData).toEqual(filterData.data);
      expect(component.sortAlphabetically).toHaveBeenCalledWith(
        filterData.data,
      );
      expect(helperService.makeUniqueArrayList).toHaveBeenCalledWith(
        filterData.data,
      );
      expect(component.checkDefaultFilterSelection).toHaveBeenCalledWith(true);
      expect(component.resetProjectSelection).not.toHaveBeenCalled();
      expect(messageService.add).not.toHaveBeenCalled();
      expect(component.loader).toBe(false);
    });

    it('should call http_service.getFilterData and handle empty response', () => {
      component.filter_kpiRequest = '';
      const filterData = {};
      spyOn(httpService, 'getFilterData').and.returnValue(of(filterData));
      spyOn(component, 'resetProjectSelection');
      // spyOn(messageService, 'add');
      component.getFilterDataOnLoad();
      fixture.detectChanges();
      expect(httpService.getFilterData).toHaveBeenCalledWith(
        component.selectedFilterData,
      );
      expect(component.filterData).toBeNull();
      expect(component.resetProjectSelection).toHaveBeenCalled();
      // expect(messageService.add).toHaveBeenCalledWith({ severity: 'error', summary: 'Projects not found.' });
      expect(component.loader).toBe(false);
    });

    it('should call http_service.getFilterData and handle error response', () => {
      component.filter_kpiRequest = '';
      const filterData = { 0: 'error' };
      spyOn(httpService, 'getFilterData').and.returnValue(of(filterData));
      spyOn(component, 'resetProjectSelection');
      spyOn(messageService, 'add');
      component.getFilterDataOnLoad();
      expect(httpService.getFilterData).toHaveBeenCalledWith(
        component.selectedFilterData,
      );
      expect(component.filterData).toBeNull();
      expect(component.resetProjectSelection).toHaveBeenCalled();
      expect(messageService.add).toHaveBeenCalledWith({
        severity: 'error',
        summary: 'Error in fetching filter data. Please try after some time.',
      });
      expect(component.loader).toBe(false);
    });
  });

  it('should set the trendLineValueList to the projectListArr, set the selectedProjectValue to the first element of the trendLineValueList, and call handleIterationFilters if flag is true', () => {
    component.projectListArr = [
      { nodeId: 'node1', name: 'Project 1' },
      { nodeId: 'node2', name: 'Project 2' },
      { nodeId: 'node3', name: 'Project 3' },
    ];
    component.filterForm = new UntypedFormGroup({
      selectedProjectValue: new UntypedFormControl(),
    });
    const flag = true;
    component.checkDefaultFilterSelection(flag);
    expect(component.trendLineValueList).toEqual(component.projectListArr);
  });

  it('should call getCapacityData if selectedProjectBaseConfigId is truthy', () => {
    const spyObj = spyOn(component, 'getCapacityData');
    component.selectedProjectBaseConfigId = 'testID';
    component.getProjectBasedData();
    expect(spyObj).toHaveBeenCalled();
  });

  it('should call saveOrUpdateSprintHappinessIndex with the correct postData, call getCapacityData with the correct basicProjectConfigId, set the expandedRows, and show a success message if the response is successful', () => {
    component.selectedSprintDetails = {
      basicProjectConfigId: 'config1',
      sprintNodeId: 'sprint1',
      startDate: '2022-01-01',
    };
    component.kanban = true;
    const capacitySaveData = {
      basicProjectConfigId: 'config1',
      sprintNodeId: 'sprint1',
      assigneeCapacity: [
        { userId: 'user1', userName: 'User 1', happinessRating: 3 },
        { userId: 'user2', userName: 'User 2', happinessRating: 4 },
      ],
    };
    const response = { success: true, data: {} };
    spyOn(httpService, 'saveOrUpdateSprintHappinessIndex').and.returnValue(
      of(response),
    );
    component.sendSprintHappinessIndexForAddOrRemove(capacitySaveData);
    expect(httpService.saveOrUpdateSprintHappinessIndex).toHaveBeenCalledWith({
      basicProjectConfigId: capacitySaveData['basicProjectConfigId'],
      sprintID: capacitySaveData['sprintNodeId'],
      userRatingList: capacitySaveData['assigneeCapacity'].map((assignee) => ({
        userId: assignee['userId'],
        userName: assignee['userName'],
        rating: assignee['happinessRating'] ? assignee['happinessRating'] : 0,
      })),
    });
  });

  it('should call saveOrUpdateSprintHappinessIndex error id response is fail', () => {
    component.selectedSprintDetails = {
      basicProjectConfigId: 'config1',
      sprintNodeId: 'sprint1',
      startDate: '2022-01-01',
    };
    component.kanban = true;
    const capacitySaveData = {
      basicProjectConfigId: 'config1',
      sprintNodeId: 'sprint1',
      assigneeCapacity: [
        { userId: 'user1', userName: 'User 1', happinessRating: 3 },
        { userId: 'user2', userName: 'User 2', happinessRating: 4 },
      ],
    };
    const response = { success: false, data: {} };
    spyOn(httpService, 'saveOrUpdateSprintHappinessIndex').and.returnValue(
      of(response),
    );

    // Act
    component.sendSprintHappinessIndexForAddOrRemove(capacitySaveData);

    // Assert
    expect(httpService.saveOrUpdateSprintHappinessIndex).toHaveBeenCalledWith({
      basicProjectConfigId: capacitySaveData['basicProjectConfigId'],
      sprintID: capacitySaveData['sprintNodeId'],
      userRatingList: capacitySaveData['assigneeCapacity'].map((assignee) => ({
        userId: assignee['userId'],
        userName: assignee['userName'],
        rating: assignee['happinessRating'] ? assignee['happinessRating'] : 0,
      })),
    });
  });

  it('should reset  to old values when clicked on cancel btn on selected sprint when kanban is true ', () => {
    const selectedSprint = {
      id: '63e4c5b4fba71c2bff2815d8',
      projectNodeId: 'TestProject123_63d8bca4af279c1d507cb8b0',
      projectName: 'TestProject123',
      sprintNodeId: '41963_TestProject123_63d8bca4af279c1d507cb8b0',
      sprintName: 'PS HOW |PI_12|ITR_3|25_Jan',
      sprintState: 'CLOSED',
      capacity: 28,
      basicProjectConfigId: '63d8bca4af279c1d507cb8b0',
      assigneeCapacity: [
        {
          userId: 'testUserId15',
          userName: 'testUser',
          role: 'TESTER',
          plannedCapacity: 40,
          leaves: 12,
          availableCapacity: 28,
        },
      ],
      kanban: false,
      assigneeDetails: true,
    };

    component.capacityKanbanData = [
      {
        id: '63e4c5b4fba71c2bff2815d8',
        projectNodeId: 'TestProject123_63d8bca4af279c1d507cb8b0',
        projectName: 'TestProject123',
        sprintNodeId: '41963_TestProject123_63d8bca4af279c1d507cb8b0',
        sprintName: 'PS HOW |PI_12|ITR_3|25_Jan',
        sprintState: 'CLOSED',
        capacity: 28,
        basicProjectConfigId: '63d8bca4af279c1d507cb8b0',
        assigneeCapacity: [
          {
            userId: 'testUserId16',
            userName: 'testUser',
            role: 'TESTER',
            plannedCapacity: 40,
            leaves: 12,
            availableCapacity: 28,
          },
        ],
        kanban: true,
        assigneeDetails: true,
      },
    ];

    component.selectedSprint = {
      id: '63e4c5b4fba71c2bff2815d8',
      projectNodeId: 'TestProject123_63d8bca4af279c1d507cb8b0',
      projectName: 'TestProject123',
      sprintNodeId: '41963_TestProject123_63d8bca4af279c1d507cb8b0',
      sprintName: 'PS HOW |PI_12|ITR_3|25_Jan',
      sprintState: 'CLOSED',
      capacity: 28,
      basicProjectConfigId: '63d8bca4af279c1d507cb8b0',
      assigneeCapacity: [
        {
          userId: 'testUserId17',
          userName: 'testUser',
          role: 'FRONTEND_DEVELOPER',
          plannedCapacity: 40,
          leaves: 12,
          availableCapacity: 28,
        },
      ],
      kanban: false,
      assigneeDetails: true,
    };

    component.kanban = true;
    component.onSprintCapacityCancel(selectedSprint);
    expect(component.capacityKanbanData).toBeDefined();
  });

  it('should prevent default behavior if the key is "e"', () => {
    const event = {
      key: 'e',
      preventDefault: jasmine.createSpy('preventDefault'),
    };
    component.validateInput(event);
    expect(event.preventDefault).toHaveBeenCalled();
  });

  it('should prevent default behavior if the key is "-"', () => {
    const event = {
      key: '-',
      preventDefault: jasmine.createSpy('preventDefault'),
    };
    component.validateInput(event);
    expect(event.preventDefault).toHaveBeenCalled();
  });

  it('should sort the array alphabetically by nodeName', () => {
    const objArray = [
      { nodeId: 'node1', nodeName: 'Apple' },
      { nodeId: 'node2', nodeName: 'Banana' },
      { nodeId: 'node3', nodeName: 'Cherry' },
    ];
    const result = component.sortAlphabetically(objArray);
    expect(result).toEqual([
      { nodeId: 'node1', nodeName: 'Apple' },
      { nodeId: 'node2', nodeName: 'Banana' },
      { nodeId: 'node3', nodeName: 'Cherry' },
    ]);
  });

  it('should return the node name of the assignee squad if it exists', () => {
    // Arrange
    const assignee = { squad: 'squad1' };
    component.selectedSquad = [{ nodeId: 'squad1', nodeName: 'Squad 1' }];

    // Act
    const result = component.getNodeName(assignee);

    // Assert
    expect(result).toBe('Squad 1');
  });

  it('should return "- -" if the assignee squad does not exist', () => {
    // Arrange
    const assignee = { squad: 'squad2' };
    component.selectedSquad = [{ nodeId: 'squad1', nodeName: 'Squad 1' }];

    // Act
    const result = component.getNodeName(assignee);

    // Assert
    expect(result).toBe('- -');
  });

  it('should return "- -" if the assignee squad is not provided', () => {
    // Arrange
    const assignee = {};

    // Act
    const result = component.getNodeName(assignee);

    // Assert
    expect(result).toBe('- -');
  });

  it('should toggle off generate additional filter capacity list', () => {
    // Arrange
    const capacityObject = {
      squad1_node1: 10,
      squad1_node2: 5,
      squad2_node1: 8,
      squad2_node2: 3,
    };
    component.selectedSquad = [
      { nodeId: 'squad1_node1', labelName: 'Squad 1' },
      { nodeId: 'squad2_node1', labelName: 'Squad 2' },
    ];
    const spy = spyOn(
      component,
      'createAdditionalFilterCapacityList',
    ).and.callThrough();
    // Act
    component.toggleOffGenerateAdditionalFilterCapacityList(capacityObject);

    // Assert
    expect(spy).toHaveBeenCalled();
  });

  it('should enable submit button for squads', () => {
    // Arrange
    component.selectedSquad = [
      { nodeId: 'squad1_node1', labelName: 'Squad 1' },
      { nodeId: 'squad2_node1', labelName: 'Squad 2' },
    ];
    component.squadForm = new UntypedFormGroup({
      squad1_node1: new FormControl(10),
      squad2_node1: new FormControl(8),
    });
    component.isCapacitySaveDisabled = false;
    component.capacityErrorMessage = '';
    // Act
    component.enableDisableSubmitButton();

    // Assert
    expect(component.isCapacitySaveDisabled).toBeFalse();
  });

  it('should edisable submit button for squads', () => {
    // Arrange
    component.selectedSquad = [
      { nodeId: 'squad1_node1', labelName: 'Squad 1' },
      { nodeId: 'squad2_node1', labelName: 'Squad 2' },
    ];
    component.squadForm = new UntypedFormGroup({
      squad1_node1: new FormControl(10),
      squad2_node1: new FormControl(),
    });
    component.isCapacitySaveDisabled = false;
    component.capacityErrorMessage = '';
    // Act
    component.enableDisableSubmitButton();

    // Assert
    expect(component.isCapacitySaveDisabled).toBeTrue();
  });

  it('should generate additional filter capacity list correctly', () => {
    component.selectedSquad = [
      { nodeId: 'squad1_node1', labelName: 'Squad 1' },
      { nodeId: 'squad2_node1', labelName: 'Squad 2' },
    ];
    const selectedSprint = {
      assigneeCapacity: [
        {
          userId: 'userId1',
          userName: 'User 1',
          role: 'FRONTEND_DEVELOPER',
          plannedCapacity: 40,
          leaves: 12,
          availableCapacity: 28,
          squad: 'UI',
        },
        {
          userId: 'userId2',
          userName: 'User 3',
          role: 'BACKEND_DEVELOPER',
          plannedCapacity: 20,
          leaves: 2,
          availableCapacity: 18,
          squad: 'JAVA',
        },
      ],
    };

    const spy = spyOn(component, 'createAdditionalFilterCapacityList');
    component.generateAdditionalFilterCapacityList(selectedSprint);

    expect(spy).toHaveBeenCalled();
  });

  it('should generate additional filter capacity list correctly', () => {
    component.selectedSquad = [
      { nodeId: 'squad1_node1', labelName: 'Squad 1' },
      { nodeId: 'squad2_node1', labelName: 'Squad 2' },
    ];
    const selectedSprint = {
      assigneeCapacity: [
        {
          userId: 'userId1',
          userName: 'User 1',
          role: 'FRONTEND_DEVELOPER',
          plannedCapacity: 40,
          leaves: 12,
          availableCapacity: 28,
          squad: 'squad1_node1',
        },
        {
          userId: 'userId2',
          userName: 'User 3',
          role: 'BACKEND_DEVELOPER',
          plannedCapacity: 20,
          leaves: 2,
          availableCapacity: 18,
          squad: 'squad2_node1',
        },
      ],
    };
    const spy = spyOn(component, 'createAdditionalFilterCapacityList');
    component.generateAdditionalFilterCapacityList(selectedSprint);

    expect(spy).toHaveBeenCalled();
  });

  it('should add controls for squad when additional Filter id matches with squad node id', () => {
    component.showPopuup = true;
    component.executionDate = '';
    component.selectedSprintName = '';
    component.selectedSprintId = '';
    component.startDate = '';
    component.endDate = '';
    component.kanban = false;
    component.reqObj = {
      projectNodeId: '',
      projectName: '',
      kanban: component.kanban,
      basicProjectConfigId: '',
      sprintNodeId: '',
    };
    component.selectedView = 'upload_Sprint_Capacity';
    component.selectedSquad = [
      { nodeId: 'squad1_node1', labelName: 'Squad 1' },
      { nodeId: 'squad2_node1', labelName: 'Squad 2' },
    ];
    const data = {
      executionDate: '2021-08-01',
      sprintName: 'Sprint 1',
      sprintNodeId: 'node1',
      startDate: '2021-07-01',
      endDate: '2021-07-14',
      projectNodeId: 'proj1',
      projectName: 'Project 1',
      basicProjectConfigId: 'config1',
      capacity: '10',
      additionalFilterCapacityList: [
        {
          nodeCapacityList: [
            {
              additionalFilterId: 'squad1_node1',
              additionalFilterCapacity: '100',
            },
          ],
        },
        {
          nodeCapacityList: [
            {
              additionalFilterId: 'squad2_node1',
              additionalFilterCapacity: '200',
            },
          ],
        },
      ],
    };
    component.squadForm = new UntypedFormGroup({});
    component.popupForm = new UntypedFormGroup({});
    component.selectedView = 'upload_Sprint_Capacity';
    const spy = spyOn(component, 'enableDisableSubmitButton');
    component.AddOrUpdateData(data);
    expect(component.popupForm).toBeTruthy();
    expect(component.popupForm.controls.capacity.value).toEqual('10');
    expect(component.reqObj.capacity).toEqual('10');
    expect(spy).toHaveBeenCalled();
  });

  it('should add controls for squad when additional Filter id doesnt match with squad node id', () => {
    component.showPopuup = true;
    component.executionDate = '';
    component.selectedSprintName = '';
    component.selectedSprintId = '';
    component.startDate = '';
    component.endDate = '';
    component.kanban = false;
    component.reqObj = {
      projectNodeId: '',
      projectName: '',
      kanban: component.kanban,
      basicProjectConfigId: '',
      sprintNodeId: '',
    };
    component.selectedView = 'upload_Sprint_Capacity';
    component.selectedSquad = [
      { nodeId: 'squad1_node1', labelName: 'Squad 1' },
      { nodeId: 'squad2_node1', labelName: 'Squad 2' },
    ];
    const data = {
      executionDate: '2021-08-01',
      sprintName: 'Sprint 1',
      sprintNodeId: 'node1',
      startDate: '2021-07-01',
      endDate: '2021-07-14',
      projectNodeId: 'proj1',
      projectName: 'Project 1',
      basicProjectConfigId: 'config1',
      capacity: '10',
      additionalFilterCapacityList: [
        {
          nodeCapacityList: [
            {
              additionalFilterId: '1',
              additionalFilterCapacity: '100',
            },
          ],
        },
        {
          nodeCapacityList: [
            {
              additionalFilterId: '2',
              additionalFilterCapacity: '200',
            },
          ],
        },
      ],
    };
    component.squadForm = new UntypedFormGroup({});
    component.popupForm = new UntypedFormGroup({});
    component.selectedView = 'upload_Sprint_Capacity';
    const spy = spyOn(component, 'enableDisableSubmitButton');
    component.AddOrUpdateData(data);
    expect(component.popupForm).toBeTruthy();
    expect(component.popupForm.controls.capacity.value).toEqual('10');
    expect(component.reqObj.capacity).toEqual('10');
    expect(spy).toHaveBeenCalled();
  });

  it('should add controls for squad when additional Filter id doesnt match with squad node id', () => {
    component.showPopuup = true;
    component.executionDate = '';
    component.selectedSprintName = '';
    component.selectedSprintId = '';
    component.startDate = '';
    component.endDate = '';
    component.kanban = false;
    component.reqObj = {
      projectNodeId: '',
      projectName: '',
      kanban: component.kanban,
      basicProjectConfigId: '',
      sprintNodeId: '',
    };
    component.selectedView = 'upload_Sprint_Capacity';
    component.selectedSquad = [
      { nodeId: 'squad1_node1', labelName: 'Squad 1' },
      { nodeId: 'squad2_node1', labelName: 'Squad 2' },
    ];
    const data = {
      executionDate: '2021-08-01',
      sprintName: 'Sprint 1',
      sprintNodeId: 'node1',
      startDate: '2021-07-01',
      endDate: '2021-07-14',
      projectNodeId: 'proj1',
      projectName: 'Project 1',
      basicProjectConfigId: 'config1',
      capacity: '10',
    };
    component.squadForm = new UntypedFormGroup({});
    component.popupForm = new UntypedFormGroup({});
    component.selectedView = 'upload_Sprint_Capacity';
    const spy = spyOn(component, 'enableDisableSubmitButton');
    component.AddOrUpdateData(data);
    expect(component.popupForm).toBeTruthy();
    expect(component.popupForm.controls.capacity.value).toEqual('10');
    expect(component.reqObj.capacity).toEqual('10');
    expect(spy).toHaveBeenCalled();
  });

  it('should give unauthorised error on submit capacity', fakeAsync(() => {
    component.squadForm = new UntypedFormGroup({
      squad1_node1: new UntypedFormControl('10'),
      squad2_node1: new UntypedFormControl('8'),
    });
    const res = {
      success: false,
      message: 'Unauthorized',
    };
    component.reqObj = {
      projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
      projectName: 'DEMO_SONAR',
      kanban: false,
      sprintNodeId: '40248_DEMO_SONAR_63284960fdd20276d60e4df5',
      capacity: '500',
    };
    spyOn(component, 'toggleOffGenerateAdditionalFilterCapacityList');
    const spy = spyOn(messageService, 'add');
    spyOn(httpService, 'saveCapacity').and.returnValue(of(res));
    component.submitCapacity();
    tick();
    expect(spy).toHaveBeenCalled();
  }));

  it('should give error in saving scenario on submit capacity', fakeAsync(() => {
    component.squadForm = new UntypedFormGroup({
      squad1_node1: new UntypedFormControl('10'),
      squad2_node1: new UntypedFormControl('8'),
    });
    const res = {};
    component.reqObj = {
      projectNodeId: 'DEMO_SONAR_63284960fdd20276d60e4df5',
      projectName: 'DEMO_SONAR',
      kanban: false,
      sprintNodeId: '40248_DEMO_SONAR_63284960fdd20276d60e4df5',
      capacity: '500',
    };
    spyOn(component, 'toggleOffGenerateAdditionalFilterCapacityList');
    const spy = spyOn(messageService, 'add');
    spyOn(httpService, 'saveCapacity').and.returnValue(of(res));
    component.submitCapacity();
    tick();
    expect(spy).toHaveBeenCalled();
  }));

  xit('should create additional filter capacity list', () => {
    const squadCapacityMap = {
      Squad1: {
        squad1_node1: 10,
      },
      Squad2: {
        squad2_node1: 8,
      },
    };
    const additionalFilterCapacityList = [
      {
        filterId: 'Squad1',
        nodeCapacityList: [
          {
            additionalFilterId: 'squad1_node1',
            additionalFilterCapacity: '10',
          },
        ],
      },
      {
        filterId: 'Squad2',
        nodeCapacityList: [
          {
            additionalFilterId: 'squad2_node1',
            additionalFilterCapacity: '8',
          },
        ],
      },
    ];
    const spy = spyOn(component, 'createAdditionalFilterCapacityList');
    component.createAdditionalFilterCapacityList(squadCapacityMap);
    expect(spy).toEqual(additionalFilterCapacityList);
  });

  it('should return kanban columns when kanban is true', () => {
    component.kanban = true;
    component.cols = {
      capacityKanbanKeys: ['col1', 'col2'],
      capacityScrumKeys: ['scrum1', 'scrum2'],
    };

    expect(component.getGridColumns()).toEqual(['col1', 'col2']);
  });

  it('should return scrum columns when kanban is false', () => {
    component.kanban = false;
    component.cols = {
      capacityKanbanKeys: ['col1', 'col2'],
      capacityScrumKeys: ['scrum1', 'scrum2'],
    };

    expect(component.getGridColumns()).toEqual(['scrum1', 'scrum2']);
  });

  // Test Case 2: checkIfGridDataIdEmpty()
  it('should return true when capacityKanbanData has data and kanban is true', () => {
    component.kanban = true;
    component.capacityKanbanData = [{ id: 1 }, { id: 2 }];

    expect(component.checkIfGridDataIdEmpty()).toBeTrue();
  });

  it('should return false when capacityKanbanData is empty and kanban is true', () => {
    component.kanban = true;
    component.capacityKanbanData = [];

    expect(component.checkIfGridDataIdEmpty()).toBeFalse();
  });

  it('should return true when capacityScrumData has data and kanban is false', () => {
    component.kanban = false;
    component.capacityScrumData = [{ id: 1 }, { id: 2 }];

    expect(component.checkIfGridDataIdEmpty()).toBeTrue();
  });

  it('should return false when capacityScrumData is empty and kanban is false', () => {
    component.kanban = false;
    component.capacityScrumData = [];

    expect(component.checkIfGridDataIdEmpty()).toBeFalse();
  });

  // Test Case 3: getGridData()
  it('should return kanban data when kanban is true', () => {
    component.kanban = true;
    component.capacityKanbanData = [{ id: 1 }];

    expect(component.getGridData()).toEqual([{ id: 1 }]);
  });

  it('should return scrum data when kanban is false', () => {
    component.kanban = false;
    component.capacityScrumData = [{ id: 2 }];

    expect(component.getGridData()).toEqual([{ id: 2 }]);
  });

  // Test Case 4: getDataKey()
  it('should return "startDate" when kanban is true', () => {
    component.kanban = true;
    expect(component.getDataKey()).toBe('startDate');
  });

  it('should return "sprintNodeId" when kanban is false', () => {
    component.kanban = false;
    expect(component.getDataKey()).toBe('sprintNodeId');
  });

  // Test Case 5: getExpandedClass()
  it('should return correct classes when kanban is true', () => {
    component.kanban = true;

    const result = component.getExpandedClass(true, true);

    expect(result).toEqual({ 'tr-active': true, 'row-expanded': true });
  });

  it('should return correct classes when kanban is false and sprintState is active', () => {
    component.kanban = false;

    const result = component.getExpandedClass({ sprintState: 'active' }, true);

    expect(result).toEqual({ 'tr-active': true, 'row-expanded': true });
  });

  it('should return correct classes when kanban is false and sprintState is not active', () => {
    component.kanban = false;

    const result = component.getExpandedClass({ sprintState: 'closed' }, true);

    expect(result).toEqual({ 'tr-active': false, 'row-expanded': true });
  });
});
