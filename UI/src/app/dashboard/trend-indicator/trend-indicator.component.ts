import { Component, Input, OnInit } from '@angular/core';
@Component({
  selector: 'app-trend-indicator',
  templateUrl: './trend-indicator.component.html',
  styleUrls: ['./trend-indicator.component.css']
})

export class TrendIndicatorComponent implements OnInit {
  @Input() dataTrend: object;
  @Input() color: string;
  @Input() kpiData: object;
  @Input() noOfBox: number;
  @Input() cols?: Array<object> = [];


  ngOnInit(): void {
    // if (this.kpiData && this.kpiData['kpiDetail']['chartType'] === 'stackedColumn') {
    //   const trend = this.dataTrend?.length > 1 ? this.dataTrend[1] : this.dataTrend[0];
    //   if (trend && Object.keys(trend)?.length > 0) {
    //     this.isTrendObject = trend && (typeof (trend) === 'object') ? true : false;
    //     const list = [];
    //     if (this.isTrendObject) {
    //       this.dataTrend.forEach((item, index) => {
    //         const values = Object.values(item);
    //         const sum = values && values.length > 0 ? values.reduce((acc: number, value: number) => acc + value) : 0;
    //         list.push(Math.round(parseFloat(sum + '')));
    //         if (index === 1) {
    //           this.lastValue = Math.round(parseFloat(sum + ''));
    //         }
    //       });
    //     }
    //     this.dataTrend = list;
    //   } else {
    //     this.dataTrend = [0];
    //     this.lastValue = 0;
    //   }

    // } else {
    //   this.isTrendObject = false;
    // }
    // if (this.kpiData && this.kpiData['kpiId'] == 'kpi997') {
    //   this.dataTrend = ['NA'];
    //   this.lastValue = 'NA';
    // }

  }

  ngOnDestroy() {
    this.kpiData = {};
    this.dataTrend = {};
    this.color = '';
    this.noOfBox = 0;
  }

}
