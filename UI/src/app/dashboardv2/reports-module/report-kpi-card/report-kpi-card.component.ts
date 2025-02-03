import { Component, Input, OnInit } from '@angular/core';
import { KpiHelperService } from 'src/app/services/kpi-helper.service';

@Component({
  selector: 'app-report-kpi-card',
  templateUrl: './report-kpi-card.component.html',
  styleUrls: ['./report-kpi-card.component.css']
})
export class ReportKpiCardComponent implements OnInit {
  @Input() kpiData: any;
  @Input() currentChartData: any;
  constructor(private kpiHelperService: KpiHelperService) { }

  ngOnInit(): void {
  }

  showCummalative() {
    if (this.kpiData?.kpiDetail?.chartType === 'stacked-bar') {
      return this.kpiHelperService.convertToHoursIfTime(this.currentChartData.totalCount, 'day')
    }
    return this.currentChartData.totalCount
  }
}
