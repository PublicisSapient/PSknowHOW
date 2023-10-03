import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { SharedService } from 'src/app/services/shared.service';

@Component({
  selector: 'app-issue-card',
  templateUrl: './issue-card.component.html',
  styleUrls: ['./issue-card.component.css']
})
export class IssueCardComponent implements OnChanges {

  @Input() issueData;
  // issueData = {
  //     "statusLogGroup": {
  //       "2023-08-17": [
  //         "In Analysis"
  //       ],
  //       "2023-06-21": [
  //         "Open"
  //       ]
  //     },
  //     "workLogGroup": {},
  //     "assigneeLogGroup": {
  //       "2023-08-08": [
  //         "Shivani ."
  //       ],
  //       "2023-06-21": [
  //         "Pranav Barar"
  //       ]
  //     },
  //     "timeWithUser": 1373204,
  //     "timeWithStatus": 594339,
  //     "worklogged": 0,
  //     "previousSprintName": "KnowHOW | PI_14| ITR_4",
  //     "Issue Id": "DTS-26340",
  //     "Issue URL": "https://tools.publicis.sapient.com/jira/browse/DTS-26340",
  //     "Issue Description": "BE | Implement Daily Standup View (Screen 2)",
  //     "Issue Status": "In Analysis",
  //     "Issue Type": "Story",
  //     "Size(story point/hours)": "5.0",
  //     "Logged Work": "0d",
  //     "Original Estimate": "5d ",
  //     "Priority": "P4 - Minor",
  //     "Due Date": "28-Aug-2023",
  //     "Remaining Estimate": "3d ",
  //     "Remaining Days": "3d ",
  //     "Predicted Completion Date": "29-Aug-2023",
  //     "Potential Delay(in days)": "1d",
  //     "Dev Due Date": "2023-08-28",
  //     "Assignee": "Shivani .",
  //     "Change Date": "2023-08-23",
  //     "Labels": [
  //       "JAVA",
  //       "UI"
  //     ],
  //     "Created Date": "2023-06-21",
  //     "Root Cause List": [
  //       "None"
  //     ],
  //     "Owner Full Name": [
  //       "Shivani ."
  //     ],
  //     "Sprint Name": "KnowHOW | PI_14| ITR_5",
  //     "Release Name": "KnowHOW v7.9.0",
  //     "Updated Date": "2023-08-23",
  //     "Dev-Completion-Date": "-",
  //     "subTask": [
  //       {
  //         "statusLogGroup": {
  //           "2023-07-21": [
  //             "Open"
  //           ]
  //         },
  //         "workLogGroup": {},
  //         "assigneeLogGroup": {
  //           "2023-08-23": [
  //             "Sanjay Singh Dhami"
  //           ],
  //           "2023-07-21": [
  //             "Raparthi Kalyan"
  //           ]
  //         },
  //         "timeWithUser": 69359,
  //         "timeWithStatus": 2905411,
  //         "worklogged": 0,
  //         "Issue Id": "DTS-27250",
  //         "Issue URL": "https://tools.publicis.sapient.com/jira/browse/DTS-27250",
  //         "Issue Description": "CopyDB dump from prod to Dev server ",
  //         "Issue Status": "Open",
  //         "Issue Type": "Story",
  //         "Size(story point/hours)": "2.0",
  //         "Logged Work": "0d",
  //         "Original Estimate": "2d ",
  //         "Priority": "P4 - Minor",
  //         "Due Date": "31-Aug-2023",
  //         "Remaining Estimate": "2d ",
  //         "Remaining Days": "2d ",
  //         "Predicted Completion Date": "28-Aug-2023",
  //         "Potential Delay(in days)": "-3d",
  //         "Dev Due Date": "2023-08-31",
  //         "Assignee": "Sanjay Singh Dhami",
  //         "Change Date": "2023-08-23",
  //         "Labels": [],
  //         "Created Date": "2023-07-21",
  //         "Root Cause List": [
  //           "None"
  //         ],
  //         "Owner Full Name": [
  //           "Sanjay Singh Dhami"
  //         ],
  //         "Sprint Name": "KnowHOW | PI_14| ITR_5",
  //         "Updated Date": "2023-08-23",
  //         "Dev-Completion-Date": "-"
  //       },
  //       {
  //         "statusLogGroup": {
  //           "2023-08-23": [
  //             "Open"
  //           ]
  //         },
  //         "workLogGroup": {},
  //         "assigneeLogGroup": {
  //           "2023-08-23": [
  //             "Laurentiu Mustata"
  //           ]
  //         },
  //         "timeWithUser": 60140,
  //         "timeWithStatus": 60140,
  //         "worklogged": 0,
  //         "Issue Id": "DTS-27887",
  //         "Issue URL": "https://tools.publicis.sapient.com/jira/browse/DTS-27887",
  //         "Issue Description": "Debbie | Pickup time field name not consistent",
  //         "Issue Status": "Open",
  //         "Issue Type": "Defect",
  //         "Size(story point/hours)": "0.5",
  //         "Logged Work": "0d",
  //         "Original Estimate": "0d",
  //         "Priority": "P4 - Minor",
  //         "Due Date": "-",
  //         "Remaining Estimate": "-",
  //         "Predicted Completion Date": "-",
  //         "Overall Delay": "-",
  //         "Dev Due Date": "-",
  //         "Assignee": "Shivani",
  //         "Change Date": "2023-08-23",
  //         "Labels": [],
  //         "Created Date": "2023-08-23",
  //         "Root Cause List": [
  //           "None"
  //         ],
  //         "Owner Full Name": [
  //           "Shivani"
  //         ],
  //         "Sprint Name": "KnowHOW | PI_14| ITR_5",
  //         "Updated Date": "2023-08-23",
  //         "Dev-Completion-Date": "-"
  //       }
  //     ]
  //   };

  isOverViewSelected = true;
  constructor(private service: SharedService) {
    this.service.currentData.subscribe(data => {
      this.issueData = data;
    });
   }

  ngOnChanges(changes: SimpleChanges){
    console.log(this.issueData);
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
