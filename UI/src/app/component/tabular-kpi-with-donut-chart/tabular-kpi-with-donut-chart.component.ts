import { Component, OnInit, Input, SimpleChanges } from '@angular/core';

@Component({
  selector: 'app-tabular-kpi-with-donut-chart',
  templateUrl: './tabular-kpi-with-donut-chart.component.html',
  styleUrls: ['./tabular-kpi-with-donut-chart.component.css'],
})
export class TabularKpiWithDonutChartComponent {
  @Input() data: any[] = [];

  ngOnChanges(changes: SimpleChanges): void {}
}
