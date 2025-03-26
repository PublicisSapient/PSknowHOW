import { Component, ViewChild, ElementRef } from '@angular/core';
import { HttpService } from 'src/app/services/http.service';
import { SharedService } from 'src/app/services/shared.service';

@Component({
  selector: 'app-recent-comments',
  templateUrl: './recent-comments.component.html',
  styleUrls: ['./recent-comments.component.css'],
})
export class RecentCommentsComponent {
  commentList: Array<object> = [];
  showSpinner: boolean = false;
  displayCommentModal: boolean = false;
  selectedTab: string = '';
  kpiObj: object = {};
  nodeChildName: string = '';

  @ViewChild('commentsDialog') commentsDialog: ElementRef;

  constructor(
    private httpService: HttpService,
    private sharedService: SharedService,
  ) {}

  getRecentComments() {
    this.showSpinner = true;
    this.displayCommentModal = true;
    let reqObj = this.createReqObj();
    this.httpService.getCommentSummary(reqObj).subscribe(
      (response) => {
        if (response['success']) {
          this.commentList = response['data'];
        } else {
          this.commentList = [];
          console.log(this.commentList);
        }
        this.showSpinner = false;
      },
      (error) => {
        console.log(error);
        this.commentList = [];
        this.showSpinner = false;
      },
    );
  }

  getNodeName(nodeId) {
    let filterData: Array<any> = this.sharedService.sharedObject['filterData'];
    return filterData.filter((x) => x.nodeId == nodeId)[0]?.nodeName;
  }

  createReqObj() {
    let filterApplyData: object =
      this.sharedService.sharedObject['filterApplyData'];
    let kpiList: Array<object> =
      this.sharedService.sharedObject['masterData']?.kpiList;
    this.selectedTab = this.sharedService.sharedObject['selectedTab'];
    let filterData: Array<any> = this.sharedService.sharedObject['filterData'];

    let reqObj = {
      level: filterApplyData?.['level'],
      nodeChildId:
        filterApplyData?.['selectedMap']['sprint']?.[0] ||
        filterApplyData?.['selectedMap']['release']?.[0] ||
        '',
      kpiIds: kpiList?.map((item) => item['kpiId']),
      nodes: [],
    };

    if (this.selectedTab?.toLowerCase() == 'backlog') {
      reqObj['nodeChildId'] = '';
    }

    kpiList.forEach((x) => {
      this.kpiObj[x['kpiId']] = x['kpiName'];
    });

    if (
      this.selectedTab?.toLowerCase() == 'iteration' ||
      this.selectedTab?.toLowerCase() == 'release'
    ) {
      reqObj['nodes'] = [
        ...filterData.filter((x) => x.nodeId == filterApplyData?.['ids'][0])[0]
          ?.parentId,
      ];
      this.nodeChildName = filterData.filter(
        (x) => x.nodeId == reqObj.nodeChildId,
      )[0]?.nodeName;
    } else {
      reqObj['nodes'] = [...filterApplyData?.['selectedMap']['project']];
    }
    return reqObj;
  }

  onDialogShow() {
    setTimeout(() => {
      const dialogElement = this.commentsDialog.nativeElement;
      const focusableElements = dialogElement.querySelectorAll(
        'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])',
      );

      if (focusableElements.length > 0) {
        (focusableElements[0] as HTMLElement).focus();
      } else {
        // If no focusable elements, focus on the dialog itself
        dialogElement.focus();
      }
    });
  }
}
