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
import { Subject } from 'rxjs';
@Injectable()
export class KpiHelperService {
  constructor() { }
  private headerAction = new Subject<any>();
  headerAction$ = this.headerAction.asObservable();
  iconObj = {
    'Issue count': 'Warning.svg',
    'Issue Count': 'Warning.svg',
    'Issue at Risk': 'Warning.svg',
    'Issue without estimates': 'Warning.svg',
    'Issue with missing worklogs': 'Warning.svg',
    'Unlinked Defects': 'Warning.svg',
    'Story Linked Defects': 'Check.svg',
    'DIR': 'Watch.svg',
    'Story Point': 'PieChart.svg',
    'Defect Density': 'visibility_on.svg',
    'Percentage': 'Check.svg',
    '': 'Check.svg',
  };

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

    const modifiedDataSet = chartData.map((item: any) => {
      return {
        ...item,
        tooltipValue: this.convertToHoursIfTime(item.value, 'day'),
        value: Math.floor(Math.floor(Math.abs(item.value) / 60) / 8),
      };
    });
    return { chartData: modifiedDataSet, totalCount };
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
      }
    }

    const modifiedDataSet = chartData.map((item: any) => {
      return {
        ...item,
        tooltipValue: this.convertToHoursIfTime(item.value, json.unit),
        value: Math.floor(item.value / 60),
        unit: json.unit,
      };
    });

    return { chartData: modifiedDataSet };
  }

  groupedBarChartData(json, color) {
    let chartData = {};
    chartData['data'] = [];
    const issueData = json.issueData || [];
    const dataGroup = json.dataGroup.dataGroup1;
    const categoryGroup = json.categoryData.categoryGroup2;
    let issueDataCopy;

    categoryGroup.forEach(categoryElem => {
      let test = {};
      test['category'] = categoryElem.categoryName;
      test['value1'] = 0;
      test['value2'] = 0;

      issueDataCopy = issueData.filter(issue => issue.Category.includes(categoryElem.categoryName));

      dataGroup.forEach(dataGroupElem => {
        if (dataGroupElem.aggregation === 'count') {
          test['value1'] = issueDataCopy.length;
          test['category1'] = 'Issue Count';
          test['color1'] = color[0];
        } else if (dataGroupElem.aggregation === 'sum') {
          test['value2'] = issueDataCopy.reduce((acc: number, issue: any) => {
            return acc + (issue['Remaining Hours'] || 0); // Use the key from the data group
          }, 0);
          test['value2'] = test['value2'] / (8 * 60);
          test['category2'] = 'Story Points';
          test['color2'] = color[1];
        }
      });
      
      test['color'] = color;
      chartData['data'].push(test);
    });

    chartData['categoryData'] = categoryGroup;
    chartData['summaryHeader'] = json.dataGroup.dataGroup2[0].name;
    chartData['summaryValue'] = this.convertToHoursIfTime(issueData.reduce((acc: number, issue: any) => {
      return acc + (issue['Remaining Hours'] || 0); // Use the key from the data group
    }, 0), 'day');
    return { chartData: chartData };
  }

  semicircledonutchartData(json: any, color: any) {
    return { chartData: json.issueData.length, color: color };
  }

  pieChartWithFiltersData(inputData: any) {
    let chartData = inputData.issueData;
    let filterGroup = inputData.filterGroup;
    let categoryGroup = inputData.categoryData.categoryGroup;
    let modifiedDataSet = {
      chartData: chartData,
      filterGroup: filterGroup,
      category: categoryGroup
    }
    return { chartData: modifiedDataSet };
  }

  filterLessKPI(inputData) {
    let result = [];
    result = inputData.filter(kpiData => kpiData.filter1.toLowerCase() === 'overall');
    return { chartData: result };
  }

  tabularKPI(inputData) {

    const dataGroup1 = inputData.dataGroup?.dataGroup1;
    const issueData = inputData.issueData;
    const chartData: any = [];

    dataGroup1?.forEach((group: any, index) => {
      const filteredIssues = issueData.filter(
        (issue: any) => issue[group.key] !== undefined,
      );
      let aggregateVal = 0;
      if (!filteredIssues.length && group.aggregation === 'count') {
        aggregateVal = issueData.length
      } else {
        aggregateVal = this.convertToHoursIfTime(filteredIssues.reduce((sum: any, issue: any) => {
          return sum + (issue[group.key] || 0); // Sum up the values for the key
        }, 0), 'day');
      }

      chartData.push({
        category: group.name,
        value: aggregateVal,
        icon: this.iconObj[group.name]
      });
    });

    return { chartData: chartData };
  }

  tabularKPINonRawData(inputData) {
    const chartData: any = [];

    inputData.forEach((group: any, index) => {
      chartData.push({
        category: group.name,
        value: group.kpiValue,
        icon: this.iconObj[group.name]
      });
    });

    return { chartData: chartData };
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

  getChartDataSet(inputData, chartType, color) {
    let returnDataSet;
    switch (chartType) {
      case 'stacked-bar-chart':
        returnDataSet = this.stackedBarChartData(inputData, color);
        break;
      case 'bar-chart':
        returnDataSet = this.barChartData(inputData, color);
        break;
      case 'stacked-bar':
        returnDataSet = this.stackedChartData(inputData, color);
        break;
      case 'semi-circle-donut-chart':
        returnDataSet = this.semicircledonutchartData(inputData, color);
        break;
      case 'chartWithFilter':
        returnDataSet = this.pieChartWithFiltersData(inputData);
        break;
      case 'CumulativeMultilineChart':
        returnDataSet = this.filterLessKPI(inputData.trendValueList);
        break;
      case 'table-v2':
        returnDataSet = this.tabularKPI(inputData);
        break;
      case 'tableNonRawData':
        returnDataSet = this.tabularKPINonRawData(inputData.dataGroup.dataGroup1);
        break;
      case 'grouped-bar-chart':
        returnDataSet = this.groupedBarChartData(inputData, color);
        break;
      case 'tabular-with-donut-chart':
        returnDataSet = this.tabularKPINonRawData(inputData.dataGroup.dataGroup1);
        break;
      default:
        break;
    }
    return returnDataSet;
  }
}
