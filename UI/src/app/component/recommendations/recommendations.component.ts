import { Component, Input, OnInit } from '@angular/core';
import { MessageService } from 'primeng/api';
import { HttpService } from 'src/app/services/http.service';
import { SharedService } from 'src/app/services/shared.service';

@Component({
  selector: 'app-recommendations',
  templateUrl: './recommendations.component.html',
  styleUrls: ['./recommendations.component.css']
})
export class RecommendationsComponent implements OnInit {
  displayModal: boolean = false;
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
  selectedSprint: object = {};
  loading: boolean = false;

  constructor(private httpService: HttpService, private messageService: MessageService, public service: SharedService) { }

  ngOnInit(): void {

  }

  handleClick() {
    this.selectedSprint = this.service.getSprintForRnR();
    this.displayModal = true;
    let kpiFilterData = JSON.parse(JSON.stringify(this.filterData));
    kpiFilterData['kpiIdList'] = [...this.kpiList];
    kpiFilterData['selectedMap']['project'] = [Array.isArray(this.selectedSprint?.['parentId']) ? this.selectedSprint?.['parentId']?.[0] : this.selectedSprint?.['parentId']];
    kpiFilterData['selectedMap']['sprint'] = [this.selectedSprint?.['nodeId']];
    this.loading = true;
    this.maturities = [];
    this.tabs = [];
    this.tabsContent = {};
    this.httpService.getRecommendations(kpiFilterData).subscribe((response: Array<object>) => {
      if (response?.length > 0) {
        response.forEach((recommendation) => {
          if (this.selectedSprint['nodeId'] == recommendation['sprintId']) {
            if (recommendation?.['recommendations']?.length > 0) {
              this.recommendationsData = recommendation['recommendations'];
              this.recommendationsData.forEach((item) => {
                let idx = this.maturities?.findIndex((x) => x['value'] == item['maturity']);
                if (idx == -1 && item['maturity']) {
                  this.maturities = [...this.maturities, { name: 'M' + item['maturity'], value: item['maturity'] }]
                } else {
                  this.maturities = [...this.maturities]
                }
                this.tabs = !this.tabs.includes(item['recommendationType']) ? [...this.tabs, item['recommendationType']] : [...this.tabs];
                this.tabsContent[item['recommendationType']] = [];
              });

              this.recommendationsData.forEach((item) => {
                this.tabsContent[item['recommendationType']] = [...this.tabsContent[item['recommendationType']], item]
              });
              this.noRecommendations = false;
            } else {
              this.noRecommendations = true;
            }
          }
        })
      } else {
        this.noRecommendations = true;
      }
      this.loading = false;
    }, error => {
      console.log(error);
      this.messageService.add({ severity: 'error', summary: 'Error in Kpi Column Configurations. Please try after sometime!' });
      this.loading = false;
    })
  }
}