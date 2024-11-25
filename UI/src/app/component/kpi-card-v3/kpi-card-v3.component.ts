import { Component, OnInit, ElementRef, Input } from '@angular/core';
import * as d3 from 'd3';

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

  constructor(private elRef: ElementRef) {}

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
    this.currentChartData = this.prepareStackedBarChartData(
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
    this.currentChartData = this.prepareStackedBarChartData(
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

  prepareStackedBarChartData(inputData: any, color: any) {
    const dataGroup1 = inputData.dataGroup?.dataGroup1;
    const issueData = inputData.issueData;
    const categoryGroup = inputData.categoryData?.categoryGroup;

    if (!dataGroup1 || dataGroup1.length === 0) {
      throw new Error('Invalid data: Missing dataGroup1');
    }

    const chartData: any = [];

    // Handle categoryGroup if present
    if (categoryGroup && dataGroup1[0]?.showAsLegend === false) {
      categoryGroup.forEach((category: any, index) => {
        // Filter issues matching the categoryName
        const filteredIssues = issueData.filter(
          (issue: any) => issue.Category[0] === category.categoryName,
        );

        chartData.push({
          category: category.categoryName,
          value: filteredIssues.length, // Count of issues for this category
          color: color[index % color.length],
        });
      });
    } else {
      // Handle dataGroup1 when categoryGroup is not available or showAsLegend is true
      dataGroup1.forEach((group: any, index) => {
        const filteredIssues = issueData.filter(
          (issue: any) => issue[group.key] !== undefined,
        );

        chartData.push({
          category: group.name,
          value: filteredIssues.reduce((sum: any, issue: any) => {
            return sum + (issue[group.key] || 0); // Sum up the values for the key
          }, 0),
          color: color[index % color.length],
        });
      });
    }

    const totalCount = chartData.reduce((sum: any, issue: any) => {
      return sum + (issue.value || 0); // Sum up the values for the key
    }, 0);
    return { chartData, totalCount };
  }
}
