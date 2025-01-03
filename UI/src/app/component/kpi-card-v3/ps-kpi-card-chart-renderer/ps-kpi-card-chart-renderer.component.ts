import { Component, Input, OnInit } from '@angular/core';

@Component({
  selector: 'app-ps-kpi-card-chart-renderer',
  templateUrl: './ps-kpi-card-chart-renderer.component.html'
})
export class PsKpiCardChartRendererComponent implements OnInit {

  @Input() chartData: any;
  @Input() chartType: string;
  @Input() chartWidth: any;
  @Input() chartHeight: any;
  @Input() kpiId: string = '';
  @Input() kpiDetails: any;
  constructor() { }

  ngOnInit(): void {
  }

}
