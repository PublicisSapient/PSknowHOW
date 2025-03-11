import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { HttpService } from 'src/app/services/http.service';
import { MessageService } from 'primeng/api';

@Component({
  selector: 'app-report-container',
  templateUrl: './report-container.component.html',
  styleUrls: ['./report-container.component.css']
})
export class ReportContainerComponent implements OnInit {
  chartData: any;
  widthObj = { 100: 'p-col-12', 50: 'p-col-6', 66: 'p-col-8', 33: 'p-col-4' };
  reportsData: any[] = [];
  selectedReport: any;

  // Reference to the scrollable container element
  @ViewChild('sliderContainer', { static: true }) sliderContainer!: ElementRef<HTMLDivElement>;

  constructor(private http: HttpService, private messageService: MessageService) { }

  ngOnInit(): void {
    this.http.fetchReports().subscribe((data) => {
      this.reportsData = data['data']['content'];
      
      this.selectedReport = this.reportsData[0];
      this.selectedReport.kpis.forEach((kpi) => {
        kpi.chartData = JSON.parse(kpi.chartData);
      });
      this.generateChartData(this.reportsData[0]);
    });
  }

  generateChartData(report) {
    if (typeof report.kpis[0].chartData === 'string') {
      report.kpis.forEach((kpi) => {
        kpi.chartData = JSON.parse(kpi.chartData);
      });
    }
    this.selectedReport = report;

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
    return 'p-col-8';
  }

  scrollLeft(): void {
    this.sliderContainer.nativeElement.scrollBy({ left: -200, behavior: 'smooth' });
  }

  scrollRight(): void {
    this.sliderContainer.nativeElement.scrollBy({ left: 200, behavior: 'smooth' });
  }

  setSelectedReport(report) {

    setTimeout(() => {
      this.generateChartData(report);
    });
  }

  objectValues(obj): any[] {
    // return this.helperService.getObjectKeys(obj)
    let result = [];
    if (obj && Object.keys(obj)?.length) {
      Object.keys(obj).forEach((x) => {
        result.push(obj[x]);
      });
    }
    return result;
  }

  objectKeys(obj) {
    return obj && Object.keys(obj)?.length ? Object.keys(obj) : [];
  }

  deleteKPIFromReport(selectedReport, kpi) {
    console.log(selectedReport);

    selectedReport.kpis = selectedReport.kpis.filter(x => x.id !== kpi.id);
    // this.messageService.add({ severity: 'success', summary: 'Report updated successfully' });
    let data = { ...selectedReport };
    data.kpis.forEach(element => {
      element.chartData = JSON.stringify(element.chartData);
    });

    let reportId = selectedReport.id;
    this.http.updateReport(reportId, data).subscribe(data => {
      if (data['success']) {
        data['kpis'].forEach(element => {
          element.chartData = JSON.parse(element.chartData);
        });
        selectedReport.kpis = data['kpis'];
        this.messageService.add({ severity: 'success', summary: 'Report updated successfully' });
      } else {
        this.messageService.add({ severity: 'error', summary: 'Error while updating report' });
      }
    });
  }

}
