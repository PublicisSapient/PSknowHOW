import { Component, Input, OnInit, SimpleChanges } from '@angular/core';

@Component({
  selector: 'app-trend-indicator-v2',
  templateUrl: './trend-indicator-v2.component.html',
  styleUrls: ['./trend-indicator-v2.component.css']
})
export class TrendIndicatorV2Component implements OnInit {
  @Input() cols?: Array<object> = [];

  @Input() trendData: [];
  @Input() colorObj: any;
  dataObj = [];
  headerObj = [];
  constructor() { }

  ngOnChanges(changes: SimpleChanges) {

    if (this.trendData && this.trendData.length) {
      this.dataObj = [];
      this.headerObj = [];
      this.colorObj = Object.keys((this.colorObj)).map((key) => this.colorObj[key]);

      this.trendData.forEach((trend) => {
        this.dataObj.push({
          'Project': this.colorObj.filter((obj) => obj.nodeName === trend['hierarchyName'])[0].color,
          'Latest Trend': trend['value'] + ' (' + trend['trend'] + ')',
          'KPI Maturity': trend['maturity'],
        });
      });
      this.headerObj.push(...Object.keys(this.dataObj[0]));

      this.dataObj = this.generateFlatArray(this.dataObj);
      console.log(this.dataObj);
    }
  }

  ngOnInit(): void {
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

}
