import { Component, OnInit, Input,EventEmitter,Output } from '@angular/core';
import { HttpService } from 'src/app/services/http.service';
import { SharedService } from 'src/app/services/shared.service';
import { DynamicDialogConfig } from 'primeng/dynamicdialog';

@Component({
  selector: 'app-comments-v2',
  templateUrl: './comments-v2.component.html',
  styleUrls: ['./comments-v2.component.css']
})
export class CommentsV2Component implements OnInit {
  kpiId;
  kpiName;
  selectedTab;
  // @Input() commentCount: string;
  showCommentIcon = false;
  displayCommentsList: boolean;
  showAddComment: boolean = false;
  commentText = '';
  selectedFilters = [];
  selectedTabIndex = 0;
  commentsList = [];
  nodeSet = new Set();
  nodeArray = {};
  commentError = false;
  dataLoaded = false;
  @Output() closeSpringOverlay = new EventEmitter();
  showLoader:object = {};
  showConfirmBtn:object = {};
  @Output() getCommentsCountByKpiId = new EventEmitter();
  showSpinner:boolean = false;
  constructor(private service: SharedService, private http_service: HttpService, public commentsDialogConfig: DynamicDialogConfig) {
    this.kpiId = this.commentsDialogConfig.data.kpiId;
    this.kpiName = this.commentsDialogConfig.data.kpiName;
    this.selectedTab = this.commentsDialogConfig.data.selectedTab;
  }

  ngOnInit(): void {
    // console.log(this.commentsDialogConfig.data)
    this.openComments();
  }

  openComments(){
    // this.closeSpringOverlay.emit();
    this.selectedFilters = [];
    this.selectedTabIndex = 0;
    const sharedObj = this.service.getFilterObject();
    if(this.selectedTab === 'iteration'){
      for (let i = 0; i < sharedObj.filterApplyData.selectedMap?.sprint.length; i++) {
        this.selectedFilters.push(sharedObj.filterData.filter(data => {
          return data.nodeId === sharedObj.filterApplyData.selectedMap?.sprint[i]
        })[0]);
      }
    } else if(this.selectedTab === 'release'){
      for (let i = 0; i < sharedObj.filterApplyData.selectedMap?.release.length; i++) {
        this.selectedFilters.push(sharedObj.filterData.filter(data => {
          return data.nodeId === sharedObj.filterApplyData.selectedMap?.release[i]
        })[0]);
      }
    } else{
      for (let i = 0; i < sharedObj.filterApplyData.selectedMap?.project.length; i++) {
        this.selectedFilters.push(sharedObj.filterData.filter(data => {
          return data.nodeId === sharedObj.filterApplyData.selectedMap?.project[i]
        })[0]);
      }
    }
    this.getComments();
  }

  submitComment(filterData=this.selectedFilters[this.selectedTabIndex]){
    this.showSpinner = true;
    const reqObj = {
      node: (this.selectedTab !== 'iteration' && this.selectedTab !== 'release') ? filterData.nodeId : filterData.parentId[0],
      level: filterData.level,
      nodeChildId: (this.selectedTab === 'iteration' || this.selectedTab === 'release') ? filterData.nodeId : '',
      kpiId: this.kpiId,
      commentsInfo: [
        {
          comment: this.commentText
        }
      ]
    }
    this.http_service.submitComment(reqObj).subscribe((response) => {
      this.commentText = '';
      this.commentError = false;
      this.getComments();
      this.getCommentsCountByKpiId.emit(reqObj.kpiId);
      this.showSpinner = false;
    }, error => {
      console.log(error);
    });
  }

  getComments(){
    if(this.selectedTab !== 'iteration' || this.selectedTab !== 'release'){
      this.nodeSet.add(this.selectedFilters[this.selectedTabIndex]?.nodeId)
      this.nodeArray = [...this.nodeSet][0];
    } else {
      this.nodeArray = this.selectedFilters[this.selectedTabIndex]?.parentId[0]
    }
    const postData = {
      node: this.nodeArray,
      nodeChildId: (this.selectedTab === 'iteration' || this.selectedTab === 'release') ? this.selectedFilters[this.selectedTabIndex].nodeId : '',
      kpiId: this.kpiId,
      level: this.selectedFilters[this.selectedTabIndex]?.level
    };
    this.dataLoaded = false;
    console.log(postData)
    this.http_service.getComment(postData)
    .subscribe(response => {
      if(response.data?.CommentsInfo){
        this.commentsList = response.data.CommentsInfo;
      }
      for(let i=0; i<this.commentsList?.length; i++){
        this.showConfirmBtn[this.commentsList[i]?.commentId] = false;
        this.showLoader[this.commentsList[i]?.commentId] = false;
      }
      this.showAddComment = false;
      this.dataLoaded = true;
    });
  }

  commentTabChange(data){
    this.selectedTabIndex = data.index;
    this.commentsList = [];
    this.getComments();
  }

  commentChanged() {
    if (this.commentText.length == 500) {
      this.commentError = true;
    } else {
      this.commentError = false;
    }
  }

  handleConfirmDelete(commentId){
    for(let key in this.showConfirmBtn){
      if(this.showConfirmBtn[key]){
        this.showConfirmBtn[key] = false;
      }
    }
    this.showConfirmBtn[commentId] = true;
  }

  deleteComment(commentId){
    this.showConfirmBtn[commentId] = false;
    this.showLoader[commentId] = true;
    this.http_service.deleteComment(commentId).subscribe((res) => {
      if(res.success){
        this.showLoader[commentId] = false;
        this.getComments();
        this.getCommentsCountByKpiId.emit(this.kpiId);
      }
    })
  }
}
