import { Component, Input } from '@angular/core';
@Component({
  selector: 'app-trend-indicator',
  templateUrl: './trend-indicator.component.html',
  styleUrls: ['./trend-indicator.component.css'],
})
export class TrendIndicatorComponent {
  @Input() dataTrend: object;
  @Input() color: string;
  @Input() kpiData: object;
  @Input() noOfBox: number;
  @Input() cols?: Array<object> = [];

  getTooltipContent(): string {
    if ((this.dataTrend as any).isCumulative) {
      return '<div class="inner-content">Maturity based on latest trend on Cumulative data series</div>';
    } else {
      return (
        '<div class="inner-content">Average maturity for ' +
        (this.dataTrend as any).maturityDenominator +
        ' data points.</div>'
      );
    }
  }

  ngOnDestroy() {
    this.kpiData = {};
    this.dataTrend = {};
    this.color = '';
    this.noOfBox = 0;
  }
}
