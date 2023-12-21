import { Component, OnInit } from '@angular/core';
import { HttpService } from 'src/app/services/http.service';
import { SharedService } from 'src/app/services/shared.service';

@Component({
  selector: 'app-recent-comments',
  templateUrl: './recent-comments.component.html',
  styleUrls: ['./recent-comments.component.css']
})
export class RecentCommentsComponent {
  commentList: Array<object> = [];
  showSpinner: boolean = false;
  displayCommentModal: boolean = false;
  selectedTab: string = '';
  kpiObj: object = {};
  nodeChildName: string = '';

  constructor(private httpService: HttpService, private sharedService: SharedService) { }


  getRecentComments() {
    this.showSpinner = true;
    this.displayCommentModal = true;
    let filterApplyData: object = this.sharedService.sharedObject['filterApplyData'];
    let kpiList: Array<object> = this.sharedService.sharedObject['masterData']?.kpiList;
    this.selectedTab = this.sharedService.sharedObject['selectedTab'];
    let filterData: Array<any> = this.sharedService.sharedObject['filterData'];
    // this.sharedService.passDataToDashboard.subscribe((res) => {
    //   console.log(res);
    //   filterApplyData = res['filterApplyData'];
    // }, error => {
    //   console.log(error);

    // })
    console.log(this.sharedService.sharedObject);

    let reqObj = {
      "level": filterApplyData?.['level'],
      "nodeChildId": filterApplyData?.['selectedMap']['sprint']?.[0] || filterApplyData?.['selectedMap']['release']?.[0] || "",
      "kpiIds": kpiList?.map((item) => item['kpiId']),
      "nodes": []
    }

    kpiList.forEach(x => {
      this.kpiObj[x['kpiId']] = x['kpiName'];
    });
    
    if (this.selectedTab?.toLowerCase() == 'iteration' || this.selectedTab?.toLowerCase() == 'release') {
      reqObj['nodes'] = [filterData.filter(x => x.nodeId == filterApplyData?.['ids'][0])[0]?.parentId];
      this.nodeChildName = filterData.filter(x => x.nodeId == reqObj.nodeChildId)[0]?.nodeName;
    } else {
      reqObj['nodes'] = [...filterApplyData?.['selectedMap']['project']];
    }
    console.log(reqObj);

    this.httpService.getCommentSummary(reqObj).subscribe((response) => {

      if (response['success']) {
        this.commentList = response['data'];
      } else {
        this.commentList = [];
        const obj = [{
          "node": "PSKnowHOW_65643593e157cb3b681d2d62",
          "level": "5",
          "nodeChildId": "",
          "kpiId": "kpi121",
          "commentId": "dd0fd79f-7332-4f3a-ade5-fca6b9f7ca6a",
          "commentBy": "SUPERADMIN",
          "commentOn": "08-Dec-2023 12:49",
          "comment": "djhjj"
        }, {
          "node": "PSKnowHOW_65643593e157cb3b681d2d62",
          "level": "5",
          "nodeChildId": "292_PSKnowHOW_65643593e157cb3b681d2d62",
          "kpiId": "kpi121",
          "commentId": "dd0fd79f-7332-4f3a-ade5-fca6b9f7ca6a",
          "commentBy": "SUPERADMIN",
          "commentOn": "08-Dec-2023 12:49",
          "comment": "djhjj"
        }, {
          "node": "PSKnowHOW_65643593e157cb3b681d2d62",
          "level": "5",
          "nodeChildId": "",
          "kpiId": "kpi121",
          "commentId": "dd0fd79f-7332-4f3a-ade5-fca6b9f7ca6a",
          "commentBy": "SUPERADMIN",
          "commentOn": "08-Dec-2023 12:49",
          "comment": "djhjj"
        }, {
          "node": "PSKnowHOW_65643593e157cb3b681d2d62",
          "level": "5",
          "nodeChildId": "",
          "kpiId": "kpi121",
          "commentId": "dd0fd79f-7332-4f3a-ade5-fca6b9f7ca6a",
          "commentBy": "SUPERADMIN",
          "commentOn": "08-Dec-2023 12:49",
          "comment": "djhjj"
        }]
        this.commentList = [...obj]
        console.log(this.commentList);
      }
      this.showSpinner = false;
    }, error => {
      console.log(error);
      this.commentList = [];
      this.showSpinner = false;
    })
  }

  getNodeName(nodeId) {
    let filterData: Array<any> = this.sharedService.sharedObject['filterData'];
    return filterData.filter((x) => x.nodeId == nodeId)[0]?.nodeName;
  }
}
