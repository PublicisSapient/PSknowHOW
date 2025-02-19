import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-report-container',
  templateUrl: './report-container.component.html',
  styleUrls: ['./report-container.component.css']
})
export class ReportContainerComponent implements OnInit {
  chartData: any;
  widthObj = {100: 'p-col-12', 50: 'p-col-6', 66: 'p-col-8', 33: 'p-col-4'};
  constructor() { }

  ngOnInit(): void {
    // this.service.setSelectedTab('Report');
    this.generateChartData();
  }

  generateChartData() {
    this.chartData = JSON.parse(localStorage.getItem('reportData'));
    console.log(this.chartData);
  }


/**
   * Retrieves the width value associated with the specified KPI width key.
   * If the key does not exist, it defaults to 'p-col-6'.
   * 
   * @param kpiwidth - The key for which to retrieve the width value.
   * @returns The width value as a string, or 'p-col-6' if the key is not found.
   * @throws No exceptions are thrown.
   */
  getkpiwidth(kpiwidth) {
    let retValue = this.widthObj[kpiwidth] ? this.widthObj[kpiwidth] : 'p-col-6';
    return retValue;
  }

}
