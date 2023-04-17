import { Component, OnInit, Input } from '@angular/core';
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
  showCommentIcon = false;
  displayCommentsList: boolean;
  showAddComment: boolean = false;
  commentText = '';
  selectedFilters = [];
  selectedTabIndex = 0;
  commentsList = [];

  constructor(private service: SharedService, private http_service: HttpService) { }

  ngOnInit(): void {
  }

  openComments(){
    this.selectedFilters = []
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
  }

  submitComment(filterData=this.selectedFilters[this.selectedTabIndex]){
    
    const reqObj = {
      node: this.selectedTab !== 'iteration' ? filterData.nodeId : filterData.parentId[0],
      level: filterData.level,
      sprintId: this.selectedTab === 'iteration' ? filterData.nodeId : '',
      kpiId: this.kpiId,
      commentsInfo: [
        {
          commentBy: localStorage.getItem('user_name'),
          comment: this.commentText
        }
      ]
    }
    this.http_service.submitComment(reqObj).subscribe((response) => {
      this.commentText = '';
      if (this.showAddComment) {
        this.getComments();
      }
    }, error => {
      console.log(error);
    });
  }

  viewAllHandler(){
    this.commentsList = [];
    this.displayCommentsList = true;
    this.getComments();
  }

  getComments(){
    this.http_service.getComment(this.selectedTab, this.selectedFilters[this.selectedTabIndex], this.kpiId)
    .subscribe(response => {
      if(response.data?.CommentsInfo){
        this.commentsList = response.data.CommentsInfo;
      }
      this.showAddComment = false;
    });
  }

  commentTabChange(data){
    this.selectedTabIndex = data.index;
  }
}
