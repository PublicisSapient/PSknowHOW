import { Component, OnInit } from '@angular/core';
import { HttpService } from 'src/app/services/http.service';
import { SharedService } from 'src/app/services/shared.service';

@Component({
  selector: 'app-recent-comments',
  templateUrl: './recent-comments.component.html',
  styleUrls: ['./recent-comments.component.css']
})
export class RecentCommentsComponent implements OnInit {
  commentList:Array<object> = [];
  showSpinner:Boolean = false;
  displayCommentModal: Boolean = false;

  constructor(private httpService: HttpService, private sharedService: SharedService) { }

  ngOnInit(): void {
  }

  getRecentComments() {
    this.showSpinner = true;
    this.displayCommentModal = true;
    console.log("123");
    this.sharedService.passDataToDashboard.subscribe((res) => {
      console.log(res);
      
    }, error => {
      console.log(error);
      
    })
    console.log(this.sharedService.sharedObject);
    
    // this.sharedService.passDataToDashboard.
    // let reqObj = {
    //   "level": 5,
    //   "nodeChildId": '',
    //   "kpiIds": '',
    //   "nodes": []
    // }
    // let reqObj = {
    //   "level": this.filterApplyData?.['level'],
    //   "nodeChildId": this.filterApplyData?.['selectedMap']['sprint']?.[0] || this.filterApplyData?.['selectedMap']['release']?.[0] || "",
    //   "kpiIds": this.showKpisList?.map((item) => item.kpiId),
    //   "nodes": []
    // }

    // this.showKpisList.forEach(x => {
    //   this.kpiObj[x.kpiId] = x.kpiName;
    // });

    // if (this.selectedTab?.toLowerCase() == 'iteration' || this.selectedTab?.toLowerCase() == 'release') {
    //   reqObj['nodes'] = this.filterData.filter(x => x.nodeId == this.filterApplyData?.['ids'][0])[0]?.parentId;
    // } else {
    //   reqObj['nodes'] = [...this.filterApplyData?.['selectedMap']['project']];
    // }

    // this.httpService.getCommentSummary(reqObj).subscribe((response) => {
    //   if (response['success']) {
        
    //     // const obj = {
    //     //           "node": "PSknowHOW _6527af981704342160f43748",
    //     //           "level": "5",
    //     //           "nodeChildId": "",
    //     //           "kpiId": "kpi3",
    //     //           "commentId": "dd0fd79f-7332-4f3a-ade5-fca6b9f7ca6a",
    //     //           "commentBy": "SUPERADMIN",
    //     //           "commentOn": "08-Dec-2023 12:49",
    //     //           "comment": "djhjj"
    //     //       }

    //     this.commentList = response['data'];
    //   } else {
    //     this.commentList = [];
    //   }
    //   this.showSpinner = false;
    // }, error => {
    //   console.log(error);
    //   this.commentList = [];
    //   this.showSpinner = false;
    // })
  }
}
