import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';

@Component({
  selector: 'app-trend-indicator-v2',
  templateUrl: './trend-indicator-v2.component.html',
  styleUrls: ['./trend-indicator-v2.component.css']
})
export class TrendIndicatorV2Component implements OnChanges {
  @Input() cols?: Array<object> = [];

  @Input() trendData: any;
  @Input() colorObj: any;
  @Input() selectedTab: any;
  dataObj = [];
  headerObj = [];

  ngOnChanges(changes: SimpleChanges) {

    if (this.trendData && this.trendData.length) {
      this.dataObj = [];
      this.headerObj = [];
      this.colorObj = Object.keys((this.colorObj)).map((key) => this.colorObj[key]);

      this.trendData.forEach((trend) => {
        if (trend['hierarchyName']) {
          const trendObj = {
            'Project': this.colorObj.filter((obj) => obj.nodeId === trend['hierarchyId'])[0].color,
            'Latest Trend': trend['value'] + ' (' + trend['trend'] + ')',
            'KPI Maturity': this.getMaturityValue(trend),
          };

          this.dataObj.push(trendObj);
        } else {
          this.dataObj.push({});
        }
      });
      this.headerObj.push(...Object.keys(this.dataObj[0]));

      this.dataObj = this.generateFlatArray(this.dataObj);
    }
  }

  ngOnDestroy() {
    this.trendData = [];
    this.colorObj = '';
    this.dataObj = [];
    this.headerObj = [];
  }

  generateFlatArray(dataSet) {
    let result = [];
    Object.keys(dataSet[0]).forEach((key) => {
      let val = dataSet.map((dataObj) => { if (dataObj[key]) { return dataObj[key] } });
      result.push(val);
    });
    return result;
  }

  getMaturityValue(trend: any): string {
    if (trend.maturityValue && trend.maturity !== '--' && trend.maturity !== 'NA') {
      return trend.maturityValue + ' ' + trend.kpiUnit.charAt(0).toUpperCase() + ' (' + trend.maturity + ')';
    } else {
      return trend.maturity;
    }
  }

}
