import { Component, OnInit } from '@angular/core';
import { SharedService } from 'src/app/services/shared.service';

@Component({
  selector: 'app-default-report',
  templateUrl: './default-report.component.html',
  styleUrls: ['./default-report.component.css']
})
export class DefaultReportComponent implements OnInit {
  chartData: any;
  constructor(private service: SharedService) { }

  ngOnInit(): void {
    this.service.setSelectedTab('Report');
    this.generateChartData();
  }

  generateChartData() {
    this.chartData = JSON.parse(localStorage.getItem('reportData'));
    console.log(this.chartData);
  }

  getkpiwidth(kpiwidth) {
    let retValue = '';

    switch (kpiwidth) {
      case 100:
        retValue = 'p-col-12';
        break;
      case 50:
        retValue = 'p-col-6';
        break;
      case 66:
        retValue = 'p-col-8';
        break;
      case 33:
        retValue = 'p-col-4';
        break;
      default:
        retValue = 'p-col-6';
        break;
    }

    return retValue;
  }

}
