/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

/*********************************************
This files contain common methods that can be use in application
**********************************************/

import { Injectable } from '@angular/core';
import { filter, Subject } from 'rxjs';
@Injectable()
export class KpiHelperService {
  constructor() { }
  private headerAction = new Subject<any>();
  headerAction$ = this.headerAction.asObservable();

  emitHeaderAction(action: any): void {
    this.headerAction.next(action);
  }

  stackedBarChartData(inputData: any, color: any) {
    const dataGroup1 = inputData.dataGroup?.dataGroup1;
    const issueData = inputData.issueData;
    const categoryGroup = inputData.categoryData?.categoryGroup;

    if (!dataGroup1 || dataGroup1.length === 0) {
      throw new Error('Invalid data: Missing dataGroup1');
    }

    const chartData: any = [];

    // Handle categoryGroup if present
    // if (categoryGroup && dataGroup1[0]?.showAsLegend === false) {
    categoryGroup.forEach((category: any, index) => {
      // Filter issues matching the categoryName
      const filteredIssues = issueData.filter(
        (issue: any) => issue.Category[0] === category.categoryName,
      );

      chartData.push({
        category: category.categoryName,
        value:
          filteredIssues.length * (category.categoryValue === '+' ? 1 : -1), // Count of issues for this category  filteredIssues.length * (category.categoryValue === '+'?1:-1),
        color: color[index % color.length],
      });
    });
    chartData.sort((a, b) => a.value - b.value);

    const totalCount = chartData.reduce((sum: any, issue: any) => {
      return sum + (issue.value || 0); // Sum up the values for the key
    }, 0);
    return { chartData, totalCount };
  }

  stackedChartData(inputData: any, color: any) {
    const dataGroup1 = inputData.dataGroup?.dataGroup1;
    const issueData = inputData.issueData;
    const categoryGroup = inputData.categoryData?.categoryGroup;

    if (!dataGroup1 || dataGroup1.length === 0) {
      throw new Error('Invalid data: Missing dataGroup1');
    }

    const chartData: any = [];
    // Handle dataGroup1 when categoryGroup is not available or showAsLegend is true
    dataGroup1.forEach((group: any, index) => {
      const filteredIssues = issueData.filter(
        (issue: any) => issue[group.key] !== undefined,
      );

      // const value = filteredIssues.reduce((sum: any, issue: any) => {
      //     return sum + (issue[group.key] || 0); // Sum up the values for the key
      // }, 0);
      // const convertedValue = this.convertToHoursIfTime(value, 'day'); // or 'day' depending on your requirement

      // console.log(convertedValue)
      chartData.push({
        category: group.name,
        value: filteredIssues.reduce((sum: any, issue: any) => {
          return sum + (issue[group.key] || 0); // Sum up the values for the key
        }, 0),
        color: color[index % color.length],
      });
    });

    const totalCount = chartData.reduce((sum: any, issue: any) => {
      return sum + (issue.value || 0); // Sum up the values for the key
    }, 0);

    // console.log(convertToHoursIfTime(,'day'))
    const test = chartData.map((item: any) => {
      return {
        ...item,
        tooltipValue: this.convertToHoursIfTime(item.value, 'day'),
        value: Math.floor(Math.floor(Math.abs(item.value) / 60) / 8),
      };
    });
    return { chartData: test, totalCount };
  }

  barChartData(json: any, color: any) {
    let chartData = [];
    const issueData = json.issueData || [];
    const dataGroup = json.dataGroup.dataGroup1; // Access the dataGroup from kpiFilterData

    // Loop through each data group entry to calculate the sums
    for (const groupKey in dataGroup) {
      if (dataGroup.hasOwnProperty(groupKey)) {
        const groupItems = dataGroup[groupKey];

        const key = groupItems.key;
        const name = groupItems.name;
        const aggregation = groupItems.aggregation;

        // Calculate the sum based on the key
        let sum;
        if (key) {
          sum = issueData.reduce((acc: number, issue: any) => {
            return acc + (issue[key] || 0); // Use the key from the data group
          }, 0);
        } else {
          sum = issueData.length;
        }

        // Push the result into chartData array
        chartData.push({ category: name, value: sum, color: color[groupKey] }); // Default color if not specified
        //   });
      }
    }

    const test = chartData.map((item: any) => {
      return {
        ...item,
        tooltipValue: this.convertToHoursIfTime(item.value, json.unit),
        value: Math.floor(item.value / 60),
        unit: json.unit,
      };
    });

    return { chartData: test };
  }

  semicircledonutchartData(inputData: any) {
    return { chartData: inputData.issueData.length };
  }

  pieChartWithFiltersData(inputData: any) {
    let chartData = inputData.issueData;
    let filterGroup = inputData.filterGroup;
    let test = {
      chartData: chartData,
      filterGroup: filterGroup
    }
    return { chartData: test };
  }

  filterLessKPI(inputData) {
    let result = [];
    result = inputData.filter(kpiData => kpiData.filter1.toLowerCase() === 'overall');
    return { chartData: result };
  }

  convertToHoursIfTime(val, unit) {
    const isLessThanZero = val < 0;
    val = Math.abs(val);
    const hours = val / 60;
    const rhours = Math.floor(hours);
    const minutes = (hours - rhours) * 60;
    const rminutes = Math.round(minutes);
    if (unit?.toLowerCase() === 'hours') {
      val = this.convertToHours(rminutes, rhours);
    } else if (unit?.toLowerCase() === 'day') {
      if (val !== 0) {
        val = this.convertToDays(rminutes, rhours);
      } else {
        val = '0d';
      }
    }
    if (isLessThanZero) {
      val = '-' + val;
    }
    return val;
  }

  convertToHours(rminutes, rhours) {
    if (rminutes === 0) {
      return rhours + 'h';
    } else if (rhours === 0) {
      return rminutes + 'm';
    } else {
      return rhours + 'h ' + rminutes + 'm';
    }
  }

  convertToDays(rminutes, rhours) {
    const days = rhours / 8;
    const rdays = Math.floor(days);
    rhours = (days - rdays) * 8;
    return `${rdays !== 0 ? rdays + 'd ' : ''}${rhours !== 0 ? rhours + 'h ' : ''
      }${rminutes !== 0 ? rminutes + 'm' : ''}`;
  }
}
