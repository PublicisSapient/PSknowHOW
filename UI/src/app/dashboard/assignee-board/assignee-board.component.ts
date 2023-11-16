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
File contains code for assignee board component.
@author bhagyashree, rishabh
*******************************/

import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { SharedService } from 'src/app/services/shared.service';

@Component({
  selector: 'app-assignee-board',
  templateUrl: './assignee-board.component.html',
  styleUrls: ['./assignee-board.component.css']
})
export class AssigneeBoardComponent implements OnInit, OnChanges {

  @Input() issueDataList = [];
  @Input() standUpStatusFilter = [];
  @Input() onFullScreen;
  @Input() kpiData;
  selectedTaskStatusFilter: string;
  currentIssueIndex = 0;
  currentSprint;
  showIssueDetails: boolean = false;
  graphWidth: number = 100;

  @Output() reloadKPITab = new EventEmitter<any>();
  filteredIssueDataList: any[];

  constructor(private sharedService: SharedService) {
    this.sharedService.currentData.subscribe(data => {
      if (data && Object.keys(data).length) {
        this.showIssueDetails = true;
        this.graphWidth = 75;
      } else {
        this.showIssueDetails = false;
        this.graphWidth = 100;
      }
    });
  }

  ngOnInit(): void {
    this.currentSprint = this.sharedService.currentSelectedSprint;
  }

  ngOnChanges(changes: SimpleChanges) {
    this.currentIssueIndex = 0;
    this.filteredIssueDataList = JSON.parse(JSON.stringify(this.issueDataList));
  }

  onPreviousIssue() {
    if (this.currentIssueIndex > 0) {
      this.currentIssueIndex = this.currentIssueIndex - 1;
      this.sharedService.setIssueData(this.filteredIssueDataList[this.currentIssueIndex]);
    }
  }

  onNextIssue() {
    if (this.currentIssueIndex < this.filteredIssueDataList.length - 1) {
      this.currentIssueIndex = this.currentIssueIndex + 1;
      this.sharedService.setIssueData(this.filteredIssueDataList[this.currentIssueIndex]);
    }
  }

  taskFilterSelected(e) {
    if (e) {
      this.selectedTaskStatusFilter = e;
    } else {
      this.selectedTaskStatusFilter = '';
    }
    this.filterTasksByStatus();
  }

  filterTasksByStatus() {
    if (this.selectedTaskStatusFilter && this.selectedTaskStatusFilter.length) {
      this.filteredIssueDataList = this.issueDataList.filter((d) => this.standUpStatusFilter.find(item => item['filterName'].toLowerCase() === this.selectedTaskStatusFilter.toLowerCase())?.options.includes(d['Issue Status']));
    } else {
      this.filteredIssueDataList = JSON.parse(JSON.stringify(this.issueDataList));
    }
    this.currentIssueIndex = 0;
    this.sharedService.setIssueData(this.filteredIssueDataList[this.currentIssueIndex]);
  }

  /** Reload KPI once field mappoing updated */
  reloadKPI(event) {
    this.reloadKPITab.emit(event);
  }
}
