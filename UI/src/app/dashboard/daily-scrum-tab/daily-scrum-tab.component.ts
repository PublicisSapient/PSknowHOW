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

/*********************************************
File contains code for daily scrum tab component.
@author bhagyashree, rishabh
*******************************/

import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { SharedService } from 'src/app/services/shared.service';

@Component({
  selector: 'app-daily-scrum-tab',
  templateUrl: './daily-scrum-tab.component.html',
  styleUrls: ['./daily-scrum-tab.component.css']
})
export class DailyScrumTabComponent implements OnInit {

  @Input() filterData;
  @Input() assigneeList = [];
  @Input() columns = [];
  @Input() issueData = [];
  @Input() standUpStatusFilter = [];
  @Input() kpiData;
  @Input() loader;
  displayModal = true;
  showLess = true;
  selectedRole = null;
  selectedUser = 'Overall';
  filters = {};
  @Output() reloadKPITab = new EventEmitter<any>();
  @Output() backToIteration = new EventEmitter<any>();
  constructor(private sharedService: SharedService) { }

  ngOnInit(): void {
    this.sharedService.setVisibleSideBar(false);
    this.sharedService.setSideNav(false);
  }

  setExpandView(e) {
    this.assigneeList = this.assigneeList.slice();
    this.displayModal = e;
  }

  onShowLessOrMore() {
    this.showLess = !this.showLess;
  }

  onFilterChange(filters) {
    this.filters = filters;
  }

  onSelectedUserChange(selectedUser) {
    this.selectedUser = this.selectedUser === selectedUser ? 'Overall' : selectedUser;
  }

  /** Reload KPI once field mappoing updated */
  reloadKPI(event) {
    this.reloadKPITab.emit(event);
  }

  closeModal() {
    this.displayModal = false;
  }
}
