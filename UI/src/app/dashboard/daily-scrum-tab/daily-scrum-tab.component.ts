import { Component, Input, OnInit } from '@angular/core';
import { SharedService } from 'src/app/services/shared.service';

@Component({
  selector: 'app-daily-scrum-tab',
  templateUrl: './daily-scrum-tab.component.html',
  styleUrls: ['./daily-scrum-tab.component.css']
})
export class DailyScrumTabComponent implements OnInit {

  @Input() filterData;
  @Input() assigneeList = [];
  @Input() columns =[];
  @Input() issueData =[];
  @Input() standUpStatusFilter =[];
  displayModal = true;
  showLess = true;
  selectedRole =null;
  selectedUser = 'Overall';
  filters={};

  constructor(private sharedService: SharedService) { }

  ngOnInit(): void {
    this.sharedService.setVisibleSideBar(false);
    this.sharedService.setSideNav(false);
  }

  setExpandView(e){
    this.assigneeList = this.assigneeList.slice();
    this.displayModal = e;
  }

  onShowLessOrMore(){
    this.showLess = !this.showLess;
  }

  onFilterChange(filters){
    this.filters = filters;
  }

  onSelectedUserChange(selectedUser){
    this.selectedUser = this.selectedUser === selectedUser ? 'Overall' : selectedUser ;
  }
}
