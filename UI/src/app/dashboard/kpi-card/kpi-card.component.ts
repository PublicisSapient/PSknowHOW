import { Component, OnInit, Input, Output, EventEmitter, OnDestroy } from '@angular/core';
import { faShareSquare } from '@fortawesome/free-solid-svg-icons';
import { HttpService } from 'src/app/services/http.service';
import { SharedService } from 'src/app/services/shared.service';
@Component({
  selector: 'app-kpi-card',
  templateUrl: './kpi-card.component.html',
  styleUrls: ['./kpi-card.component.css']
})
export class KpiCardComponent implements OnInit, OnDestroy {
  @Input() kpiData: any;
  @Input() trendData: Array<object>;
  @Output() downloadExcel = new EventEmitter<boolean>();
  @Input() dropdownArr: any;
  @Output() optionSelected = new EventEmitter<any>();
  faShareSquare = faShareSquare;
  isTooltip = false;
  filterTooltip = false;
  @Input() trendBoxColorObj: any;
  subscriptions: any[] = [];
  filterOption = 'Overall';
  filterOptions: object = {};
  radioOption: string;
  filterMultiSelectOptionsData: object = {};
  kpiSelectedFilterObj: any = {};
  @Input() isShow?: any;
  @Input() showExport: boolean;
  @Input() showTrendIndicator =true;
  @Input() showChartView = true;
  @Input() cols: Array<object> = [];
  @Input() iSAdditionalFilterSelected =false;
  displayCommentsList: boolean;
  showAddComment: boolean = false;
  commentText = '';
  selectedFilters = [];
  selectedTabIndex = 0;
  dummyData = {
    "node": "nodevalue",
    "projectBasicConfig": "projectId",
    "commentKpiWise": [
      {
        "kpiId": "kpi14",
        "commentInfo": [
          {
            "commentId": "1",
            "commentBy": "pravin",
            "commentOn": "22/02/2023 10:00",
            "comment": "With the trends of the KPIs on all dashboards that include (Speed, Quality, Value, Iteration and Backlog), users figure out some reasoning which they would like to save for narrating it to customers/stakeholders/leadership team With the trends of the KPIs on all dashboards that include (Speed, Quality, Value, Iteration and Backlog), users figure out some reasoning which they would like to save for narrating it to customers/stakeholders/leadership team With the trends of the KPIs on all dashboards that include (Speed, Quality, Value, Iteration and Backlog), users figure out some reasoning which they would like to save for narrating it to customers/stakeholders/leadership team With the trends of the KPIs on all dashboards that include (Speed, Quality, Value, Iteration and Backlog), users figure out some reasoning which they would like to save for narrating it to customers/stakeholders/leadership team"
          },
          {
            "commentId": "2",
            "commentBy": "patil",
            "commentOn": "23/02/2023 17:00",
            "comment": "Less data required"
          }
        ]
      },
      {
        "kpiId": "105",
        "commentInfo": [
          {
            "commentId": "1",
            "commentBy": "tom",
            "commentOn": "26/02/2023 20:00",
            "comment": "More data required"
          },
          {
            "docId": "2",
            "commentBy": "hanks",
            "commentOn": "27/02/2023 22:00",
            "comment": "Less data required"
          }
        ]
      }
    ]
  }
  commentsList = [];


  constructor(private service: SharedService, private http_service: HttpService) {
  }
  ngOnInit(): void {
    this.subscriptions.push(this.service.selectedFilterOptionObs.subscribe((x) => {
      if (Object.keys(x)?.length > 0) {
        this.kpiSelectedFilterObj = JSON.parse(JSON.stringify(x));
        for (const key in x[this.kpiData?.kpiId]) {
          if (x[this.kpiData?.kpiId][key]?.includes('Overall')) {
            this.filterOptions = {};
            this.filterOption = 'Overall';
          } else {
            this.filterOption = this.kpiSelectedFilterObj[this.kpiData?.kpiId][0];
          }
        }
        if (this.kpiData?.kpiDetail?.hasOwnProperty('kpiFilter') && this.kpiData?.kpiDetail?.kpiFilter?.toLowerCase() == 'radiobutton') {
          if (this.kpiSelectedFilterObj[this.kpiData?.kpiId]) {
            this.radioOption = this.kpiSelectedFilterObj[this.kpiData?.kpiId][0];
          }
        }
      }
    }));
    /** assign 1st value to radio button by default */
    if(this.kpiData?.kpiDetail?.hasOwnProperty('kpiFilter') && this.kpiData?.kpiDetail?.kpiFilter?.toLowerCase() == 'radiobutton' && this.dropdownArr?.length > 0){
      this.radioOption = this.dropdownArr[0]?.options[0];
    }
  }

  exportToExcel() {
    this.downloadExcel.emit(true);
  }

  showTooltip(val) {
    this.isTooltip = val;
  }

  handleChange(type, value) {
    if (value && type?.toLowerCase() == 'radio') {
      this.optionSelected.emit(value);
    } else if (type?.toLowerCase() == 'single') {
      const obj = {
        filter1: []
      };
      obj['filter1'].push(this.filterOption);
      this.optionSelected.emit(obj);
    } else {
      if (this.filterOptions && Object.keys(this.filterOptions)?.length == 0) {
        this.optionSelected.emit(['Overall']);
      } else {
        this.optionSelected.emit(this.filterOptions);
      }
      // this.showFilterTooltip(true);
    }
  }
  getColor(nodeName) {
    let color = '';
    for (const key in this.trendBoxColorObj) {
      if (this.trendBoxColorObj[key]?.nodeName == nodeName) {
        color = this.trendBoxColorObj[key]?.color;
      }
    }
    return color;
  }
  handleClearAll(event) {
    for (const key in this.filterOptions) {
      if (key?.toLowerCase() == event?.toLowerCase()) {
        delete this.filterOptions[key];
      }
    }
  }

  showFilterTooltip(showHide, filterNo?) {
    if (showHide) {
      this.filterMultiSelectOptionsData['details'] = {};
      this.filterMultiSelectOptionsData['details'][filterNo] = [];
      for (let i = 0; i < this.filterOptions[filterNo]?.length; i++) {

        this.filterMultiSelectOptionsData['details'][filterNo]?.push(
          {
            type: 'paragraph',
            value: this.filterOptions[filterNo][i]
          }
        );
      }

    } else {
      this.filterMultiSelectOptionsData = {};
    }
  }

  openComments(){
    this.selectedFilters = []
    const sharedObj = this.service.getFilterObject()
    for (let i = 0; i < sharedObj.filterApplyData.ids.length; i++) {
      this.selectedFilters.push(sharedObj.filterData.filter(data => {
        return data.nodeId === sharedObj.filterApplyData.ids[i]
      })[0]);
    }
  }

  submitComment(filterData=this.selectedFilters[this.selectedTabIndex]){
    const reqObj = {
      node: 'nodeValue',
      projectBasicConfig: filterData.nodeId,
      commentsKpiWise: [
        {
          kpiId: this.kpiData?.kpiId,
          commentsInfo: [
            {
              commentBy: localStorage.getItem('user_name'),
              commentOn: new Date(),
              comment: this.commentText
            }
          ]
        }
      ]
    }
    this.http_service.submitComment(reqObj).subscribe((response) => {

    }, error => {
      console.log(error);
      // this.isFeedbackSubmitted = false;
      setTimeout(() => {
        // this.formMessage = '';
      }, 3000);
    });
  }

  viewAllHandler(){
    console.log('kpiid', this.kpiData?.kpiId);

    this.displayCommentsList = true;
    this.http_service.getComment(this.kpiData?.kpiId, this.selectedFilters[this.selectedTabIndex].nodeId)
      .subscribe(response => {
        this.commentsList = response.data.CommentsInfo;
      });
    // this.commentsList = this.dummyData.commentKpiWise.filter(comment=>{
    //   return comment.kpiId === this.kpiData?.kpiId
    // })[0]?.commentInfo;
  }

  commentTabChange(data){
    this.selectedTabIndex = data.index;
  }

  ngOnDestroy() {
    this.kpiData = {};
    this.trendData = [];
    this.subscriptions.forEach(subscription => subscription.unsubscribe());
  }
}
