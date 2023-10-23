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
  currentIssueIndex = 0;
  currentSprint;
  showIssueDetails: boolean = false;
  graphWidth: number = 100;

  @Output() reloadKPITab = new EventEmitter<any>();
  
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
  }

  onPreviousIssue() {
    if (this.currentIssueIndex > 0) {
      this.currentIssueIndex = this.currentIssueIndex - 1;
      this.sharedService.setIssueData(this.issueDataList[this.currentIssueIndex]);
    }
  }

  onNextIssue() {
    if (this.currentIssueIndex !== this.issueDataList.length - 1) {
      this.currentIssueIndex = this.currentIssueIndex + 1;
      this.sharedService.setIssueData(this.issueDataList[this.currentIssueIndex]);
    }
  }

   /** Reload KPI once field mappoing updated */
   reloadKPI(event){
    this.reloadKPITab.emit(event);
  }
}
