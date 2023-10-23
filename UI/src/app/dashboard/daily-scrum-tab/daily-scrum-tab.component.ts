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
  displayModal = true;
  showLess = true;
  selectedRole = null;
  selectedUser = 'Overall';
  filters = {};
  @Output() reloadKPITab = new EventEmitter<any>();
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
}
