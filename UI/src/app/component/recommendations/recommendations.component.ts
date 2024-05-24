import { Component, Input, OnInit } from '@angular/core';
import { MessageService } from 'primeng/api';
import { HttpService } from 'src/app/services/http.service';

@Component({
  selector: 'app-recommendations',
  templateUrl: './recommendations.component.html',
  styleUrls: ['./recommendations.component.css']
})
export class RecommendationsComponent implements OnInit {
  displayModal:boolean = false;
  modalDetails = {
    tableHeadings: [],
    tableValues: [],
    kpiId: ''
  };
  recommendationsData: Array<object> = [];
  tabs: Array<string> = []
  tabsContent: object = {};
  maturities: Array<object> = [];
  filteredMaturity;
  @Input() filterData = {}
  @Input() kpiList = [];
  noRecommendations: boolean = false;

  constructor(private httpService: HttpService,  private messageService: MessageService) { }

  ngOnInit(): void {
  }

  handleClick(){
    this.displayModal = true;
    this.filterData['kpiIdList'] = [...this.kpiList]
    console.log(this.filterData);
    
    this.httpService.getRecommendations(this.filterData).subscribe((response) => {
      console.log("response", response);
  
      if(response?.['recommendations']?.length > 0){
        // let res = {
        //   "projectId": "AA Data and Reporting_649c00cd1734471c30843d2d",
        //   "lastSprintId": "284d41ca-0ed6-470e-a664-66983a28eeb4_AA Data and Reporting_649c00cd1734471c30843d2d",
        //   "recommendations": [
        //       {
        //           "kpiId": "kpi14",
        //           "kpiName": "KPI name for kpi14",
        //           "maturity": 3,
        //           "recommendationSummary": "The project quality can be improved!",
        //           "recommendationDetails": "The last data has showed a decrease in the quality of the project for the last sprints!",
        //           "recommendationType": "Warnings",
        //           "filter": "Overall"
        //       },
        //       {
        //           "kpiId": "kpi35",
        //           "kpiName": "KPI name for kpi35",
        //           "maturity": 5,
        //           "recommendationSummary": "Nice job!",
        //           "recommendationDetails": "The team did a great job during the last sprints!",
        //           "recommendationType": "Good Practices",
        //           "filter": "Overall"
        //       },
        //       {
        //         "kpiId": "kpi11",
        //         "kpiName": "KPI name for kpi11",
        //         "maturity": 5,
        //         "recommendationSummary": "Nice job!",
        //         "recommendationDetails": "The team did a great job during the last sprints!",
        //         "recommendationType": "Good Practices",
        //         "filter": "Overall"
        //       },
        //       {
        //         "kpiId": "kpi22",
        //         "kpiName": "KPI name for kpi22",
        //         "maturity": 4,
        //         "recommendationSummary": "Nice job!",
        //         "recommendationDetails": "The team did a great job during the last sprints!",
        //         "recommendationType": "Good Practices",
        //         "filter": "Overall"
        //       },
        //       {
        //         "kpiId": "kpi5",
        //         "kpiName": "KPI name for kpi5",
        //         "maturity": 1,
        //         "recommendationSummary": "Nice job!",
        //         "recommendationDetails": "The team did a great job during the last sprints!",
        //         "recommendationType": "Good Practices",
        //         "filter": "Overall"
        //       }
        //     ] 
        // }
        this.recommendationsData = response['recommendations'];
        this.recommendationsData.forEach((item) => {
          this.maturities = this.maturities?.findIndex((x) => x['value'] == item['maturity']) == -1 ? [...this.maturities, {name: 'M'+item['maturity'], value:item['maturity']}] : [...this.maturities];
          this.tabs = !this.tabs.includes(item['recommendationType']) ? [...this.tabs, item['recommendationType']] : [...this.tabs];
          this.tabsContent[item['recommendationType']] = [];
        });
    
        this.recommendationsData.forEach((item) => {
          this.tabsContent[item['recommendationType']] = [...this.tabsContent[item['recommendationType']], item]
        });
        console.log(this.tabs);
        console.log(this.maturities);
        
        console.log(this.tabsContent);
        
        // console.log(this.recommendationsData);
        
      }else{
        this.noRecommendations = true;
      }
    }, error => {
      console.log(error);
      this.messageService.add({ severity: 'error', summary: 'Error in Kpi Column Configurations. Please try after sometime!' });
      
    })
  }
  
  checkSelected(e){
    console.log(e);
    
  }
}
