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
  showLoader:boolean = false;

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

    const reqObj = {
      node: this.selectedTab !== 'iteration' ? filterData.nodeId : filterData.parentId[0],
      level: filterData.level,
      sprintId: this.selectedTab === 'iteration' ? filterData.nodeId : '',
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
    }, error => {
      console.log(error);
    });
  }

  getComments(){
    this.dataLoaded = false;
    this.http_service.getComment(this.selectedTab, this.selectedFilters[this.selectedTabIndex], this.kpiId)
    .subscribe(response => {
      if(response.data?.CommentsInfo){
        this.commentsList = response.data.CommentsInfo;
      }
      this.showAddComment = false;
      this.dataLoaded = true;
    });
  }

  commentTabChange(data){
    this.selectedTabIndex = data.index;
  }

  commentChanged() {
    if (this.commentText.length == 500) {
      this.commentError = true;
    } else {
      this.commentError = false;
    }
  }

  deleteComment(id){
    this.showLoader = true;
    this.http_service.deleteComment({'commentId': id}).subscribe((res) => {
      if(res.success){
        this.showLoader = false;
        this.getComments();
      }
    })
  }
}
