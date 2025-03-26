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
  @ViewChild('sliderContainer', { static: false }) sliderContainer!: ElementRef<HTMLDivElement>;

  constructor(private http: HttpService, private messageService: MessageService) { }

  /**
     * Initializes the component by fetching reports data and processing the first report's KPIs.
     * It sets the selected report and parses the chart data for each KPI.
     * 
     * @returns {void} - No return value.
     */
  ngOnInit(): void {
    this.getReportsData();
  }

  /**
     * Converts the chartData property of each KPI in the report from a JSON string to an object, 
     * if it is currently a string. Updates the selectedReport with the modified report.
     * 
     * @param report - The report object containing KPIs with potential chartData as a string.
     * @returns void
     */
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
    let retValue = this.widthObj[kpiwidth] ? this.widthObj[kpiwidth] : 'p-col-8';
    return retValue;
  }

  scrollLeft(): void {
    this.sliderContainer.nativeElement.scrollBy({ left: -200, behavior: 'smooth' });
  }

  scrollRight(): void {
    this.sliderContainer.nativeElement.scrollBy({ left: 200, behavior: 'smooth' });
  }

  setSelectedReport(report) {
    Promise.resolve(null).then(() => {
      this.generateChartData(report);
    });
  }

  /**
     * Extracts the values from the given object and returns them as an array.
     * @param obj - The object from which to extract values.
     * @returns An array of values from the object, or an empty array if the object is null or has no keys.
     * @throws No exceptions are thrown by this function.
     */
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

  /**
       * Removes a specified KPI from the selected report and updates the report on the server.
       * @param selectedReport - The report object from which the KPI will be removed.
       * @param kpi - The KPI object to be deleted from the report.
       * @returns void
       * @throws Error if the HTTP request fails or the response indicates an error.
       */
  deleteKPIFromReport(selectedReport, kpi) {
    selectedReport.kpis = selectedReport.kpis.filter(x => x.id !== kpi.id);
    // this.messageService.add({ severity: 'success', summary: 'Report updated successfully' });
    let data = { ...selectedReport };
    data.kpis.forEach(element => {
      element.chartData = JSON.stringify(element.chartData);
    });

    let reportId = selectedReport.id;
    this.http.updateReport(reportId, data).subscribe(data => {
      if (data['success']) {
        data['data']['kpis'].forEach(element => {
          element.chartData = JSON.parse(element.chartData);
        });
        selectedReport.kpis = data['data']['kpis'];
        this.messageService.add({ severity: 'success', summary: 'Report updated successfully' });
      } else {
        this.messageService.add({ severity: 'error', summary: 'Error while updating report' });
      }
    });
  }

  segregateSprints(additional_filters, key, superkey) {
    if (key.toLowerCase() === 'sprint') {
      return additional_filters[key].filter(elem => elem.parentId === superkey.nodeId).map(elem => elem.nodeDisplayName).join(', ');
    } else {
      return additional_filters[key].map(elem => elem.nodeDisplayName).join(', ')
    }
  }
  
  printReport() {
    setTimeout(() => {
      window.print();
    }, 100);
  }

  getReportsData() {
    this.http.fetchReports().subscribe((data) => {
      this.reportsData = data['data']['content'];

      this.selectedReport = this.reportsData[0];
      this.selectedReport.kpis.forEach((kpi) => {
        kpi.chartData = JSON.parse(kpi.chartData);
      });
      this.generateChartData(this.reportsData[0]);
    });
  }

  removeReport(report: any, event: MouseEvent) {
    event.stopPropagation(); // Prevent triggering the button's onClick
    let deletedReportId = report?.id;
    this.http.deleteComment(deletedReportId).subscribe((res) => {
      if(res.success){
        this.reportsData = this.reportsData.filter(r => r !== report);
        //this.getReportsData();
      }
    })
   
  }
}
