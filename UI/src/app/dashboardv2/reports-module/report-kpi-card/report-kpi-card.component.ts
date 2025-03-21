import { Component, Input, OnInit } from '@angular/core';
import { KpiHelperService } from 'src/app/services/kpi-helper.service';

@Component({
  selector: 'app-report-kpi-card',
  templateUrl: './report-kpi-card.component.html',
  styleUrls: ['./report-kpi-card.component.css']
})
export class ReportKpiCardComponent {
  @Input() kpiData: any;
  @Input() currentChartData: any;
  @Input() colors: any;
  constructor(private kpiHelperService: KpiHelperService) { }

  checkIfZeroData(data) {
    return true;
  }
}
