import { Component, Input, OnInit } from '@angular/core';
import { SharedService } from 'src/app/services/shared.service';

@Component({
  selector: 'app-assignee-board',
  templateUrl: './assignee-board.component.html',
  styleUrls: ['./assignee-board.component.css']
})
export class AssigneeBoardComponent implements OnInit {

@Input() issueDataList =[];
currentIssueIndex = 0;
currentSprint;

  constructor(private sharedService: SharedService) { }

  ngOnInit(): void {
    this.currentSprint = this.sharedService.currentSelectedSprint;
  }

  onPreviousIssue(){
    if(this.currentIssueIndex > 0){
      this.currentIssueIndex = this.currentIssueIndex-1;
    }
  }

  onNextIssue(){
    if(this.currentIssueIndex !== this.issueDataList.length -1){
      this.currentIssueIndex = this.currentIssueIndex +1;
    }
  }
}
