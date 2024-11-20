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
  isTooltip = false;

  constructor(private elRef: ElementRef) {}

  ngOnInit(): void {
    console.log(this.cardData);
  }

  showTooltip(val) {
    this.isTooltip = val;
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
    console.log(filterIssues);
  }

  applyDynamicfilter(data: [], filter: { [key: string]: any }) {
    console.log(this.dropdownSelected);
    return data.filter((item) => {
      return Object.entries(filter).every(([key, value]) => {
        return item[key] === value;
      });
    });
  }
}
