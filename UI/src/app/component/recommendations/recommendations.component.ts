import { Component, Input, OnInit } from '@angular/core';
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
  maturities: Array<string> = [];

  constructor(private httpService: HttpService) { }

  ngOnInit(): void {
  }

  handleClick(){
    this.displayModal = true;
    let obj = {
      
    }
    // this.httpService.getRecommendations(obj).subscribe((response) => {

    // })
    let response = {
      "projectId": "AA Data and Reporting_649c00cd1734471c30843d2d",
      "lastSprintId": "284d41ca-0ed6-470e-a664-66983a28eeb4_AA Data and Reporting_649c00cd1734471c30843d2d",
      "recommendations": [
          {
              "kpiId": "kpi14",
              "kpiName": "KPI name for kpi14",
              "maturity": 3,
              "recommendationSummary": "The project quality can be improved!",
              "recommendationDetails": "The last data has showed a decrease in the quality of the project for the last sprints!",
              "recommendationType": "Warnings",
              "filter": "Overall"
          },
          {
              "kpiId": "kpi35",
              "kpiName": "KPI name for kpi35",
              "maturity": 5,
              "recommendationSummary": "Nice job!",
              "recommendationDetails": "The team did a great job during the last sprints!",
              "recommendationType": "Good Practices",
              "filter": "Overall"
          }
        ] 
    }
    this.recommendationsData = response.recommendations;
    this.recommendationsData.forEach((item) => {
      this.maturities = !this.maturities.includes(item['maturity']) ? [...this.maturities, "M"+item['maturity']] : [...this.maturities];
      this.tabs = !this.tabs.includes(item['recommendationType']) ? [...this.tabs, item['recommendationType']] : [...this.tabs];
      this.tabsContent[item['recommendationType']] = [];
    });

    this.recommendationsData.forEach((item) => {
      this.tabsContent[item['recommendationType']] = [...this.tabsContent[item['recommendationType']], item]
    });
    
    console.log("tabs", this.tabs);
    console.log("maturities", this.maturities);
  }
}
