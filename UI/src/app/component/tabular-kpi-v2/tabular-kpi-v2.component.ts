import { Component, Input, OnInit, SimpleChanges } from '@angular/core';

@Component({
  selector: 'app-tabular-kpi-v2',
  templateUrl: './tabular-kpi-v2.component.html',
  styleUrls: ['./tabular-kpi-v2.component.css'],
})
export class TabularKpiV2Component {
  @Input() data: any[] = [];
  @Input() kpiId: string = '';
}
