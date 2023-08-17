import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

@Component({
  selector: 'app-daily-scrum',
  templateUrl: './daily-scrum.component.html',
  styleUrls: ['./daily-scrum.component.css']
})
export class DailyScrumComponent implements OnInit {

  @Input() filterData;
  @Input() assigneeList = [];
  @Input() columns =[];
  @Input() displayModal=false;
  @Input() showLess = true;
  @Input() selectedUser = 'Overall';
  @Input() selectedRole =null;

  @Output() onExpandOrCollapse = new EventEmitter<boolean>();
  @Output() onShowLessOrMore = new EventEmitter<boolean>();
  @Output() onSelectedUserChange = new EventEmitter<string>();
  @Output() onSelectedRole = new EventEmitter<string>();

  totals ={};
  allAssignee = [];

  constructor() { }

  ngOnInit(): void {
    if(this.assigneeList.length > 0){
      this.allAssignee = [...this.assigneeList];
      this.calculateTotal();
    }
  }

  setSelectedUser(assigneeId){
    this.onSelectedUserChange.emit(assigneeId);
  }

  setShowLess(){
    this.onShowLessOrMore.emit(true);
  }

  handleViewExpandCollapse(){
    this.onExpandOrCollapse.emit(!this.displayModal);
  }

  handleSelectRole(e){
    this.onSelectedRole.emit(this.selectedRole);
    if(this.selectedRole){
      this.assigneeList = this.allAssignee.filter(assignee => assignee.role === this.selectedRole);
    }else{
      this.assigneeList = this.allAssignee;
    }
    this.calculateTotal();
  }

  calculateTotal(){
    console.log(this.columns, this.assigneeList);
    
    this.totals['Team Member'] = this.assigneeList.length + ' Members';
    this.columns.forEach(col =>{
      this.totals[col] = {...this.assigneeList[0].cardDetails[col]};
      if( 'value' in this.totals[col]){
        this.totals[col].value = 0;
      }
      if('value1' in this.totals[col]){
        this.totals[col].value1 = 0;
      }
    });

    this.assigneeList.forEach((assignee) =>{
      this.columns.forEach(col =>{
        this.totals[col].value += isNaN(assignee.cardDetails[col].value) ? 0 : +assignee.cardDetails[col].value;
        if('value1' in this.totals[col]){
          this.totals[col].value1 += isNaN(assignee.cardDetails[col].value1) ? 0 : +assignee.cardDetails[col].value1;
        }
      });
    });

   this.columns.forEach(col =>{
    if(this.totals[col]?.unit === 'day'){
      this.totals[col].value = this.convertToHoursIfTime(this.totals[col].value,this.totals[col].unit)
    }

    if(this.totals[col]?.unit1 === 'day'){
      this.totals[col].value1 = this.convertToHoursIfTime(this.totals[col].value1,this.totals[col].unit1)
    }

   });

   console.log(this.totals);
   
  }

  convertToHoursIfTime(val, unit) {
    if(val === '-'){
      return val;
    }
    const isLessThanZero = val < 0;
    val = Math.abs(val);
    const hours = (val / 60);
    const rhours = Math.floor(hours);
    const minutes = (hours - rhours) * 60;
    const rminutes = Math.round(minutes);
    if (unit?.toLowerCase() === 'hours') {
      val = this.convertToHours(rminutes, rhours);
    } else if (unit?.toLowerCase() === 'day') {
      if (val !== 0) {
        val = this.convertToDays(rminutes, rhours);
      } else {
        val = '0d';
      }
    }
    if (isLessThanZero) {
      val = '-' + val;
    }
    return val;
  }

  convertToHours(rminutes, rhours) {
    if (rminutes === 0) {
      return rhours + 'h';
    } else if (rhours === 0) {
      return rminutes + 'm';
    } else {
      return rhours + 'h ' + rminutes + 'm';
    }
  }

  convertToDays(rminutes, rhours) {
    const days = rhours / 8;
    const rdays = Math.floor(days);
    rhours = (days - rdays) * 8;
    return `${(rdays !== 0) ? rdays + 'd ' : ''}${(rhours !== 0) ? rhours + 'h ' : ''}${(rminutes !== 0) ? rminutes + 'm' : ''}`;
  }


}
