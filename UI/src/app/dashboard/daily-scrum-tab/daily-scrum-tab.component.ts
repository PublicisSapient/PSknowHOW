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

  constructor() { }

  ngOnInit(): void {
  }

  setExpandView(e){
    this.displayModal = e;
  }

  onShowLessOrMore(){
    this.showLess = !this.showLess;
  }

  onSelectedRole(role){
    this.selectedRole = role;
  }

  onSelectedUserChange(selectedUser){
    this.selectedUser = selectedUser;
  }
}
