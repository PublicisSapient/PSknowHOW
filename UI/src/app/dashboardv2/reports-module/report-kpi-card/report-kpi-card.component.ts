import { Component, Input, OnInit, SimpleChanges } from '@angular/core';
import { KpiHelperService } from 'src/app/services/kpi-helper.service';

@Component({
  selector: 'app-report-kpi-card',
  templateUrl: './report-kpi-card.component.html',
  styleUrls: ['./report-kpi-card.component.css']
})
export class ReportKpiCardComponent {
  @Input() kpiData: any;
  @Input() currentChartData: any;
  @Input() chartType: string;
  @Input() kpiDataStatusCode: string;
  @Input() kpiTrendsObj: any;
  @Input() trendColors: any;
  colors: any;
  @Input() kpiFilters: any;
  @Input() selectedButtonValue: any;
  @Input() cardData: any;
  @Input() kpiFilterData: any;
  @Input() selectedTab: string;
  @Input() iterationKPIFilterValues: any[] = [];
  @Input() filterApplyData: any;
  @Input() kpiSelectedFilterObj: any;
  @Input() chartColorList: any;
  @Input() yAxis: string = '';
  @Input() capturedAt: string = '';
  @Input() kpiHeight: number;
  @Input() releaseEndDate: string;
  @Input() hieararchy: any = null;
  constructor(private kpiHelperService: KpiHelperService) { }

/**
   * Responds to changes in input properties. 
   * Sorts colors, sets KPI filters, and updates chart type based on changes.
   * 
   * @param changes - An object containing the changed input properties.
   * @returns void
   * @throws None
   */
  ngOnChanges(changes: SimpleChanges) {
    this.sortColors();
    this.setKpiFilters();
    if(changes['chartType']) {
      if(!changes['chartType'].currentValue) {
        this.chartType = 'old-table';
      }
    }
  }

/**
     * Sorts the trend colors based on the hierarchy IDs from the kpiTrendsObj.
     * Updates the trendColors and colors properties of the class instance.
     * 
     * @throws {TypeError} If kpiTrendsObj is not an array or contains invalid entries.
     */
  sortColors() {
    let result = {};

    for (let i = 0; i < this.kpiTrendsObj?.length; i++) {
      result[this.kpiTrendsObj[i].hierarchyId] = (this.trendColors[this.kpiTrendsObj[i].hierarchyId]);
    }

    this.trendColors = result;
    this.colors = Object.keys(this.trendColors).map((key) => this.trendColors[key].color);

  }

  setKpiFilters() {
    if (this.kpiFilters) {
      if (typeof this.kpiFilters === 'string') {
        this.kpiFilters = [this.kpiFilters];
      } else {
        let result = [];
        Object.keys(this.kpiFilters).forEach((key) => {
          result.push(this.kpiFilters[key]);
        });
        this.kpiFilters = result;
      }
    }
  }

  objectKeys(obj) {
    return obj && Object.keys(obj)?.length ? Object.keys(obj) : [];
  }

  /**
   * Calculates the total sum of numeric values associated with a specified key in an array of issue data.
   * @param issueData - An array of objects representing issues, each containing various key-value pairs.
   * @param key - The key whose numeric values will be summed.
   * @returns The total sum as a string.
   * @throws No exceptions are explicitly thrown, but non-numeric values are ignored in the sum.
   */
  calculateValue(issueData, key: string): string {
    const total = issueData.reduce((sum, issue) => {
      const value = issue[key];
      return sum + (typeof value === 'number' ? value : 0); // Only add numeric values
    }, 0);

    return total.toString(); // Convert to string for display
  }

  /**
 * Converts a given value to hours if the specified unit represents time.
 * @param val - The value to be converted.
 * @param unit - The unit of the value, which determines if conversion is necessary.
 * @returns The converted value in days/hours (unit).
 */
  convertToHoursIfTime(val, unit) {
    return this.kpiHelperService.convertToHoursIfTime(val, unit)
  }

  /**
 * Checks for the presence of a filter group in the provided filter data.
 * @param filterData - An object containing filter information, which may include a filterGroup property.
 * @returns The filterGroup property if it exists; otherwise, undefined.
 * @throws No exceptions are thrown.
 */
  checkFilterPresence(filterData) {
    return filterData?.filterGroup;
  }

  checkSprint(value, unit, kpiId) {
    if ((this.kpiSelectedFilterObj?.hasOwnProperty('filter1') && this.kpiSelectedFilterObj['filter1']?.length > 0 && this.kpiSelectedFilterObj['filter1'][0]?.toLowerCase() !== 'overall')
      || (this.kpiSelectedFilterObj?.hasOwnProperty('filter2') && this.kpiSelectedFilterObj['filter2']?.length > 0 && this.kpiSelectedFilterObj['filter2'][0]?.toLowerCase() !== 'overall')) {
      return '-'
    } else {
      return Math.floor(value) < value ? `${Math.round(value)} ${unit}` : `${value} ${unit}`;
    }
  }
}
