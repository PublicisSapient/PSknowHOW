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

import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

@Component({
  selector: 'app-kpi-filter',
  templateUrl: './kpi-filter.component.html',
})
export class KpiFilterComponent implements OnInit {
  @Input() kpiRelationShips: any;
  @Input() fieldMappings: any;
  @Output() fieldsToShow = new EventEmitter<any>();
  selectedKpi;

  constructor() { }

  ngOnInit(): void {
  }

  showFieldsPopup() {
    this.fieldsToShow.emit(this.selectedKpi);
  }

  onClear(){
    this.selectedKpi='';
    this.fieldsToShow.emit(this.selectedKpi);
  }

}
