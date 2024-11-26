import { Component, OnInit, ElementRef, Input } from '@angular/core';
import * as d3 from 'd3';
import { KpiHelperService } from 'src/app/services/kpi-helper.service';

@Component({
  selector: 'kpi-card-v3',
  templateUrl: './kpi-card-v3.component.html',
  styleUrls: ['./kpi-card-v3.component.css'],
})
export class KpiCardV3Component implements OnInit {
  @Input() cardData: any;
  kpiHeaderData: {};
  kpiFilterData: {};
  copyCardData: any;
  currentChartData;
  KpiCategory;
  colorPalette = ['#167a26', '#4ebb1a', '#f53535'];

  constructor( private kpihelper:KpiHelperService) {}

  ngOnInit(): void {
    const {
      responseCode,
      issueData,
      kpiName,
      kpiInfo,
      kpiId,
      dataGroup,
      filterGroup,
      kpiFilters
    } = this.cardData;
    this.kpiHeaderData = { responseCode, issueData, kpiName, kpiInfo, kpiId };
    this.kpiFilterData = { dataGroup, filterGroup, issueData,kpiFilters };
    this.copyCardData = JSON.parse(JSON.stringify(this.cardData));
    this.currentChartData = this.prepareChartData(
      this.cardData,
      this.colorPalette,
    );
  }

  onFilterChange(event) {
    const filterIssues = this.applyDynamicfilter(
      this.cardData.issueData,
      event,
    );
    this.copyCardData = { ...this.copyCardData, issueData: filterIssues };
    this.currentChartData = this.prepareChartData(
      this.copyCardData,
      this.colorPalette,
    );
  }

  applyDynamicfilter(data: [], filter: { [key: string]: any }) {
    return data.filter((item) => {
      return Object.entries(filter).every(([key, value]) => {
        return item[key] === value;
      });
    });
  }

  prepareChartData(inputData: any, color: any) {
    let chartData:any;
    switch (this.cardData.chartType) {
      case 'stacked-bar-chart':
        chartData = this.kpihelper.stackedBarChartData(inputData,color)
        break;
      case 'bar-chart':
        chartData = this.prepareSampleData(inputData)
        break;
    
      default:
        break;
    }
    console.log(chartData)
   return chartData;
  }

  prepareSampleData(json: any) {
    let chartData=[];
    const issueData = json.issueData || [];
    const dataGroup = json.dataGroup; // Access the dataGroup from kpiFilterData

    // Loop through each data group entry to calculate the sums
    for (const groupKey in dataGroup) {
        if (dataGroup.hasOwnProperty(groupKey)) {
            const groupItems = dataGroup[groupKey];

            groupItems.forEach((item) => {
                const key = item.key; // Get the key from the dataGroup
                const name = item.name; // Get the name for display
                const aggregation = item.aggregation; // Get aggregation type (not used here, but can be useful)

                // Calculate the sum based on the key
                const sum = issueData.reduce((acc: number, issue: any) => {
                    return acc + (issue[key] || 0); // Use the key from the data group
                }, 0);

                // Push the result into chartData array
                chartData.push({ category: name, value: sum, color: item.color || "#000000" }); // Default color if not specified
            });
        }
    }

    return {chartData} ;
}
  
}
