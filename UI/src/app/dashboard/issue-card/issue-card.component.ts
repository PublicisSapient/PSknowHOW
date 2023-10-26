import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { SharedService } from 'src/app/services/shared.service';

@Component({
  selector: 'app-issue-card',
  templateUrl: './issue-card.component.html',
  styleUrls: ['./issue-card.component.css']
})
export class IssueCardComponent implements OnChanges {

  @Input() issueData;
  isOverViewSelected = true;
  constructor(private service: SharedService) {
    this.service.currentData.subscribe(data => {
      this.isOverViewSelected = true;
      this.issueData = data;
    });
   }

  ngOnChanges(changes: SimpleChanges){
    this.isOverViewSelected=true;
  }

  getNameInitials(name){
    const initials = name.split(' ').map(d => d[0]);
    if(initials.length > 2){
     return  initials.map(d => d[0]).slice(0,2).join('').toUpperCase();
    }
    return initials.join('').toUpperCase();
}
}
