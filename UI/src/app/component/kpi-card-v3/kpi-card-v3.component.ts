import { Component, OnInit, ElementRef, Input } from '@angular/core';
import * as d3 from 'd3';

@Component({
  selector: 'kpi-card-v3',
  templateUrl: './kpi-card-v3.component.html',
  styleUrls: ['./kpi-card-v3.component.css'],
})
export class KpiCardV3Component implements OnInit {
  @Input() cardData: any;
  selectedKey: '';
  dropdownSelected = {};
  currentChartData;
  KpiCategory;

  constructor(private elRef: ElementRef) {}

  ngOnInit(): void {
    console.log(this.cardData)
    this.KpiCategory = this.cardData.categoryData.categoryGroup; // filterGroup
    this.currentChartData =this.generateChartData(this.KpiCategory,this.cardData.issueData);
   
  }

  getOptions(filterKey: string) {
    const uniqueValues = [
      ...new Set(this.cardData.issueData.map((issue) => issue[filterKey])),
    ];
    return uniqueValues.map((value) => ({ label: value, value: value }));
  }

  handleChange() {
    const filterIssues = this.applyDynamicfilter(
      this.cardData.issueData,
      this.dropdownSelected,
    );
    this.currentChartData =this.generateChartData(this.KpiCategory,filterIssues);
  }

  applyDynamicfilter(data: [], filter: { [key: string]: any }) {
    console.log(this.dropdownSelected);
    return data.filter((item) => {
      return Object.entries(filter).every(([key, value]) => {
        return item[key] === value;
      });
    });
  }

  generateChartData(categoryData, kpiIssueData){
    console.log(kpiIssueData);
   
    // Extract category groups
    const categoryGroups = categoryData;
    const colorPalette = ['#167a26','#4ebb1a','#f53535']; // Use D3's built-in color scheme
    const colors = categoryGroups.map((_, index) => colorPalette[index % colorPalette.length]);
  
  
    // Map category groups to their respective issue counts
    const chartData = categoryGroups.map((category: any,index) => {
      const filteredIssues = kpiIssueData.filter(
        (issue: any) => issue.Category[0] === category.categoryName
      );
     // console.log(filteredIssues)
  
      return {
        category: category.categoryName,
        value: filteredIssues.length * (category.categoryValue === '+'?1:-1),
        color: colors[index] // Assign color from the dynamic palette
      };
    });

  console.log(chartData)
    return chartData.sort((a,b)=> a.value - b.value);
  }
  
}
