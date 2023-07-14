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

import { Component, Input, OnInit } from '@angular/core';
import { HelperService } from '../../services/helper.service';
import { ExecutiveComponent } from '../../dashboard/executive/executive.component';
@Component({
  selector: 'app-kpi-line-gauge',
  templateUrl: './kpi-line-gauge.component.html',
  styleUrls: []
})
export class KpiComponent implements OnInit {
  @Input('title') title: string;
  @Input('tooltip') tooltip: any;
  @Input('noOfFilterSelected') noOfFilterSelected: any;
  @Input('loaderJira') loaderJira: boolean;
  @Input('jiraKpiData') jiraKpiData: any;
  @Input('kpi') kpi: string;
  @Input('kpiID') kpiID: string;
  @Input('formulaNumerator') formulaNumerator: string;
  @Input('formulaDenominator') formulaDenominator: string;
  @Input() yAxisCaption: string; // label at y axis
  @Input() loaderArray: string[];
  constructor(
    private helperService: HelperService,
    private executive: ExecutiveComponent
  ) { }

  ngOnInit() { }

  downloadExcel(kpiId, kpiName, isKanban) {
    this.executive.downloadExcel(kpiId, kpiName, isKanban,false);
  }
}
