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
    this.filterData['kpiIdList'] = [...this.kpiList];
    
    this.httpService.getRecommendations(this.filterData).subscribe((response) => {
      console.log("response", response);
  
      if(response?.['recommendations']?.length > 0){
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
}