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
File contains code for daily scrum component.
@author bhagyashree, rishabh
*******************************/

import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { SortEvent } from 'primeng/api';
import { SharedService } from 'src/app/services/shared.service';

@Component({
  selector: 'app-daily-scrum',
  templateUrl: './daily-scrum.component.html',
  styleUrls: ['./daily-scrum.component.css']
})
export class DailyScrumComponent implements OnInit, OnChanges {

  @Input() filterData = [];
  @Input() assigneeList = [];
  @Input() columns = [];
  @Input() displayModal = false;
  @Input() showLess = false;
  @Input() selectedUser = 'Overall';
  @Input() filters = {};
  @Input() issueData = [];
  @Input() standUpStatusFilter = [];
  @Input() onFullScreen;

  @Output() onExpandOrCollapse = new EventEmitter<boolean>();
  @Output() onShowLessOrMore = new EventEmitter<boolean>();
  @Output() onSelectedUserChange = new EventEmitter<string>();
  @Output() onFilterChange = new EventEmitter<{ [key: string]: string }>();

  totals = {};
  allAssignee = [];
  selectedUserInfo;
  currentAssigneeissueData = [];
  activeIndex2: number = 0;

  @Input() kpiData;
  @Output() reloadKPITab = new EventEmitter<any>();
  @Output() closeModal = new EventEmitter<any>();
  @Input() loader;

  constructor(private service: SharedService) { }

  ngOnInit(): void {
    this.filterData?.forEach(filter => {
      this.filters[filter.filterKey] = this.filters[filter.filterKey] ? this.filters[filter.filterKey] : null;
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['assigneeList']) {
      this.allAssignee = changes['assigneeList']?.currentValue;
    }
    if (Object.keys(this.filters).length > 0) {
      for (const key in this.filters) {
        if (this.filters[key]) {
          this.assigneeList = this.allAssignee?.filter(assignee => assignee[key] === this.filters[key]);
        }
      }
    }
    this.selectedUserInfo = this.assigneeList?.find(assignee => assignee.assigneeId === this.selectedUser);
    this.calculateTotal();
    this.getCurrentAssigneeIssueData(this.selectedUserInfo?.assigneeName);
  }

  handleTabChange(id, name) {
    this.setSelectedUser(id, name);
  }

  setSelectedUser(assigneeId, assigneeName) {
    if (assigneeId === 'Overall') {
      this.activeIndex2 = 0;
    }
    this.service.setIssueData(null);
    if (assigneeId) {
      this.selectedUserInfo = this.assigneeList.find(assignee => assignee.assigneeId === assigneeId);
      this.onSelectedUserChange.emit(assigneeId);
      this.getCurrentAssigneeIssueData(assigneeName);
    } else {
      this.onSelectedUserChange.emit('Unassigned');
      this.getCurrentAssigneeIssueData('Unassigned');
    }
    this.onExpandOrCollapse.emit(true);
  }

  setShowLess() {
    this.onShowLessOrMore.emit(true);
  }

  handleViewExpandCollapse() {
    this.onExpandOrCollapse.emit(!this.displayModal);
  }

  handleSingleSelectChange(e, filterKey) {
    if (this.allAssignee && this.allAssignee.length) {
      if (e && e.value) {
        this.assigneeList = this.allAssignee.filter(assignee => assignee[filterKey] === e.value);
      } else {
        this.assigneeList = this.allAssignee;
      }
      this.calculateTotal();
      this.onFilterChange.emit(this.filters);
    }
  }

  calculateTotal() {
    this.totals['Team Member'] = this.assigneeList?.length + ' Members';
    this.columns?.forEach(col => {
      this.totals[col] = { ...this.assigneeList[0]?.cardDetails[col] };
      if ('value' in this.totals[col]) {
        this.totals[col].value = 0;
      }
      if ('value1' in this.totals[col] || 'unit1' in this.totals[col]) {
        this.totals[col].value1 = 0;
      }
    });

    this.assigneeList?.forEach((assignee) => {
      this.columns?.forEach(col => {
        this.totals[col].value += isNaN(assignee.cardDetails[col].value) ? 0 : +assignee.cardDetails[col].value;
        if ('value1' in this.totals[col]) {
          this.totals[col].value1 += isNaN(assignee.cardDetails[col].value1) ? 0 : +assignee.cardDetails[col].value1;
        }
      });
    });

    this.columns?.forEach(col => {
      if (this.totals[col]?.unit === 'day') {
        this.totals[col].value = this.convertToHoursIfTime(this.totals[col].value, this.totals[col].unit);
      } else {
        if (this.totals[col].value) {
          this.totals[col].value = this.totals[col].value.toFixed();
        }
      }

      if (this.totals[col]?.unit1 === 'day') {
        this.totals[col].value1 = this.convertToHoursIfTime(this.totals[col].value1, this.totals[col].unit1);
      } else {
        this.totals[col].value1 = this.totals[col].value1?.toFixed(2);
      }



    });
  }

  convertToHoursIfTime(val, unit) {
    if (val === '-' || isNaN(val)) {
      return val;
    }
    const isLessThanZero = val < 0;
    val = Math.abs(val);
    const hours = (val / 60);
    const rhours = Math.floor(hours);
    const minutes = (hours - rhours) * 60;
    const rminutes = Math.round(minutes);
    if (unit?.toLowerCase() === 'day') {
      if (val !== 0) {
        val = this.convertToDays(rminutes, rhours);
      } else {
        val = '0d';
      }
    }
    if (isLessThanZero) {
      val = '-' + val;
    }
    if (val === '') {
      val = '0d'
    }
    return val;
  }

  convertToHours(rminutes, rhours) {
    return rhours + 'h';
  }

  convertToDays(rminutes, rhours) {
    const days = rhours / 8;
    const rdays = Math.floor(days);
    rhours = (days - rdays) * 8;
    return `${(rdays !== 0) ? rdays + 'd ' : ''}${(rhours !== 0) ? rhours + 'h ' : ''}`;
  }

  customSort(event: SortEvent) {
    if (event.field === 'Team Member') {
      this.assigneeList.sort((a, b) => event.order > 0 ? a.assigneeName.localeCompare(b.assigneeName) : b.assigneeName.localeCompare(a.assigneeName));
    } else {
      this.assigneeList.sort((a, b) => {
        const value1 = a.cardDetails[event.field].value === '-' ? '' : a.cardDetails[event.field].value;
        const value2 = b.cardDetails[event.field].value === '-' ? '' : b.cardDetails[event.field].value;
        return event.order > 0 ? value1.localeCompare(value2) : value2.localeCompare(value1);
      });
    }
  }

  getNameInitials(name) {
    const initials = name.split(' ').map(d => d[0]);
    if (initials.length > 2) {
      return initials.slice(0, 2).join('').toUpperCase();
    }
    return initials.join('').toUpperCase();
  }

  getCurrentAssigneeIssueData(assigneeName) {
    this.currentAssigneeissueData = this.issueData?.filter(issue => issue['Assignee'] === assigneeName);
    this.currentAssigneeissueData?.forEach(issue => {
      if ('subTask' in issue && typeof issue['subTask'][0] === 'string') {
        issue['subTask'] = this.getSubTaskIssueDetails(issue['subTask']);
      }
    });
  }

  getSubTaskIssueDetails(subTaskList) {
    return this.issueData.filter(issue => subTaskList.includes(issue['Issue Id']));
  }

  /** Reload KPI once field mappoing updated */
  reloadKPI(event) {
    this.loader = true;
    this.reloadKPITab.emit(event);
  }

  /**back to iteration review tab */
  backToIterationTab() {
    this.onFullScreen = false;
    this.closeModal.emit(this.onFullScreen);
  }
}
