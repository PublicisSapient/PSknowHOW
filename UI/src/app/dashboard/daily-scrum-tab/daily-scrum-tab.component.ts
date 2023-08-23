import { Component, Input, OnInit } from '@angular/core';

@Component({
  selector: 'app-daily-scrum-tab',
  templateUrl: './daily-scrum-tab.component.html',
  styleUrls: ['./daily-scrum-tab.component.css']
})
export class DailyScrumTabComponent implements OnInit {

  @Input() filterData;
  @Input() assigneeList = [];
  @Input() columns =[];
  displayModal = false;
  showLess = true;
  selectedRole =null;
  selectedUser = 'Overall';
  filters={};

  constructor() { }

  ngOnInit(): void {
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
    this.selectedUser = selectedUser;
  }
}
