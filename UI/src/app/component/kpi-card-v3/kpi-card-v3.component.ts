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

  constructor(private kpihelper: KpiHelperService) {}

  ngOnInit(): void {
    const {
      responseCode,
      issueData,
      kpiName,
      kpiInfo,
      kpiId,
      dataGroup,
      filterGroup,
      kpiFilters,
      chartType
    } = this.cardData;
    this.kpiHeaderData = { responseCode, issueData, kpiName, kpiInfo, kpiId };
    this.kpiFilterData = { dataGroup, filterGroup, issueData, kpiFilters,chartType };
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
    let chartData: any;
    switch (this.cardData.chartType) {
      case 'stacked-bar-chart':
        chartData = this.kpihelper.stackedBarChartData(inputData, color);
        break;
      case 'bar-chart':
        chartData = this.kpihelper.barChartData(inputData, color);
        break;
      case 'stacked-bar':
        chartData = this.kpihelper.stackedChartData(inputData, color);
        break;

      default:
        break;
    }
    return chartData;
  }
}
