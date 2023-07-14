import { Component, OnInit, Input,EventEmitter,Output } from '@angular/core';
import { HttpService } from 'src/app/services/http.service';
import { SharedService } from 'src/app/services/shared.service';

@Component({
  selector: 'app-comments',
  templateUrl: './comments.component.html',
  styleUrls: ['./comments.component.css']
})
export class CommentsComponent implements OnInit {
  @Input() kpiId;
  @Input() kpiName;
  @Input() selectedTab;
  @Input() commentCount: string;
  showCommentIcon = false;
  displayCommentsList: boolean;
  showAddComment: boolean = false;
  commentText = '';
  selectedFilters = [];
  selectedTabIndex = 0;
  commentsList = [];
  commentError = false;
  dataLoaded = false;
  @Output() closeSpringOverlay = new EventEmitter();
  showLoader:object = {};
  showConfirmBtn:object = {};
  @Output() getCommentsCountByKpiId = new EventEmitter();
  showSpinner:boolean = false;
  constructor(private service: SharedService, private http_service: HttpService) { }

  ngOnInit(): void {
  }

  openComments(){
    this.closeSpringOverlay.emit();
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
          commentBy: this.service.getCurrentUserDetails('user_name'),
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
    const postData = {
      node: (this.selectedTab !== 'iteration' && this.selectedTab !== 'release') ? this.selectedFilters[this.selectedTabIndex]?.nodeId : this.selectedFilters[this.selectedTabIndex]?.parentId[0],
      nodeChildId: (this.selectedTab === 'iteration' || this.selectedTab === 'release') ? this.selectedFilters[this.selectedTabIndex].nodeId : '',
      kpiId: this.kpiId,
      level: this.selectedFilters[this.selectedTabIndex]?.level
    };
    this.dataLoaded = false;
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
