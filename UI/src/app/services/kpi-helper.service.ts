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
@Injectable()
export class KpiHelperService {
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
    'Original Estimate': 'PieChart.svg',
    'Defect Density': 'visibility_on.svg',
    'Percentage': 'Check.svg',
    'Risks': 'Warning.svg',
    'Dependencies': 'Warning.svg',
    '': 'Check.svg',
    'First Time Pass Stories': 'Warning.svg',
    'Total Stories': 'Warning.svg',
  };

  stackedBarChartData(inputData: any, color: any, key: string) {
    const dataGroup1 = inputData.dataGroup?.dataGroup1;
    const issueData = inputData.issueData;
    const categoryGroup = inputData.categoryData?.categoryGroup;
    let selectedDataGroup;
    let unit;
    if (key) {
      selectedDataGroup = dataGroup1.find((d) => d.key === key);
      unit = selectedDataGroup?.unit;
    }

    // if (!dataGroup1 || dataGroup1.length === 0) {
    //   throw new Error('Invalid data: Missing dataGroup1');
    // }

    if (!dataGroup1 || dataGroup1.length === 0) {
      return { chartData: [], totalCount: 0 };
    }

    let chartData: any = [];

    // Handle categoryGroup if present
    // if (categoryGroup && dataGroup1[0]?.showAsLegend === false) {
    categoryGroup.forEach((category: any, index) => {
      let filteredIssues = [];
      // Filter issues matching the categoryName
      filteredIssues = issueData.filter(
        (issue: any) => issue.Category[0] === category.categoryName,
      );

      chartData.push({
        category: category.categoryName,
        value: (key ? filteredIssues.reduce((sum, issue) => sum + (issue[key]), 0) : filteredIssues.length) * (category.categoryValue === '+' ? 1 : -1),
        color: color[index % color.length],
      });
    });

    let totalCount = chartData.reduce((sum: any, issue: any) => {
      return sum + (issue.value || 0); // Sum up the values for the key
    }, 0);

    if (!unit || !unit.length) {
      chartData.sort((a, b) => a.value - b.value);
    } else if (unit === 'day') {
      chartData.forEach((d) => d.value = d.value / (60 * 8));
      chartData.sort((a, b) => a.value - b.value);
      totalCount = this.convertToHoursIfTime(totalCount, unit);
    } else {
      chartData.sort((a, b) => a.value - b.value);
    }

    return { chartData, totalCount };
  }

  stackedChartData(inputData: any, color: any, key: string) {
    const dataGroup1 = inputData.dataGroup?.dataGroup1;
    const issueData = inputData.issueData;

    if (!dataGroup1 || dataGroup1.length === 0) {
      return { chartData: [], totalCount: 0 };
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
    const issueData = json?.issueData || [];
    const dataGroup = json?.dataGroup?.dataGroup1; // Access the dataGroup from kpiFilterData

    // Loop through each data group entry to calculate the sums
    for (const groupKey in dataGroup) {
      if (dataGroup.hasOwnProperty(groupKey)) {
        const groupItems = dataGroup[groupKey];

        const key = groupItems.key;
        const name = groupItems.name;
        const unit = groupItems.unit;
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
        if (unit && unit === 'day') {
          sum = sum / (60 * 8); // converting to 8hr days
        }
        chartData.push({ category: name, value: sum, color: color[groupKey], unit: unit }); // Default color if not specified
      }
    }

    const modifiedDataSet = chartData.map((item: any) => {
      return {
        ...item,
        tooltipValue: item.value,//this.convertToHoursIfTime(item.value, json.unit),
      };
    });

    return { chartData: modifiedDataSet };
  }

  groupedBarChartData(json, color,filter) {
    let chartData = {};
    chartData['data'] = [];
    const issueData = json.issueData || [];
    const dataGroup = json.dataGroup?.dataGroup1;
    const categoryGroup = json.categoryData?.categoryGroup2;
    const categoryKey = json.categoryData?.categoryKey;  
    let issueDataCopy;
    categoryGroup?.forEach(categoryElem => {
      let categoryValue = categoryElem.categoryName;    
      let i = 0;    
      let value = 0;    
      let test = {};
      test['category'] = categoryValue;  
      const issueDataFiltered = issueData.filter(issue => issue[categoryKey].includes(filter));
      dataGroup.forEach(dataGroupElem => {
        let dataColor = color[i];      
        i = i + 1;      
        issueDataCopy = issueDataFiltered.filter(issue => issue[categoryKey].includes(categoryValue));      
        if (dataGroupElem.aggregation === 'count') {
          value = issueDataCopy.length;      
        } else if (dataGroupElem.aggregation === 'sum') {        
          value = issueDataCopy.reduce((acc: number, issue: any) => {          
            return acc + (issue[dataGroupElem.key] || 0); // Use the key from the data group        
        }, 0);        
        if (dataGroupElem.unit && dataGroupElem.unit === 'day') {          
          value = value / (60 * 8);        
        }      
      }      
      test['value' + i] = value;      
      test['category' + i] = dataGroupElem.name;      
      test['color' + i] = dataColor;      
      test['color'] = color;      
      if(filter==='Unplanned'){        
        test['summaryValue'] = 'NA'      
      }else{        
        test['summaryValue'] = issueDataCopy.reduce((acc: number, issue: any) => {          
          if (issue.hasOwnProperty(json.dataGroup.dataGroup2[0].key) && issue[json.dataGroup.dataGroup2[0].key] > 0) {            
            return acc + issue[json.dataGroup.dataGroup2[0].key] / (60 * 8);          
          } else if (issue.hasOwnProperty(json.dataGroup.dataGroup2[0].key) && issue[json.dataGroup.dataGroup2[0].key] <= 0) {            
            return acc - issue[json.dataGroup.dataGroup2[0].key] / (60 * 8);          
          } else {            
            return acc;          
          }       
        }, 0) + ' ' + dataGroupElem.unit;      
      }    });    
      chartData['data'].push(test);  });  
      chartData['categoryData'] = categoryGroup;  
      chartData['summaryHeader'] = json?.dataGroup?.dataGroup2[0]?.name;  
      // chartData['summaryValue'] = issueDataCopy.reduce((acc: number, issue: any) => {  
      //   if (issue.hasOwnProperty(json.dataGroup.dataGroup2[0].key) && issue[json.dataGroup.dataGroup2[0].key] > 0) {  
      //     return acc + issue[json.dataGroup.dataGroup2[0].key] / (60 * 8);  
      //   } else if (issue.hasOwnProperty(json.dataGroup.dataGroup2[0].key) && issue[json.dataGroup.dataGroup2[0].key] <= 0) {  
      //     return acc - issue[json.dataGroup.dataGroup2[0].key] / (60 * 8);  
      //   } else {  
      //     return acc;  
      //   }  
      // }, 0);  
    return { chartData: chartData };
  }

  semicircledonutchartData(json: any, color: any) {
    return { chartData: json.issueData.length, color: color };
  }

  pieChartWithFiltersData(inputData: any) {
    let chartData = inputData?.issueData;
    let filterGroup = inputData?.filterGroup;
    let categoryGroup = inputData?.categoryData?.categoryGroup;
    let modifiedDataSet = {
      chartData: chartData,
      filterGroup: filterGroup,
      category: categoryGroup,
      modalHeads: inputData?.modalHeads
    }
    return { chartData: modifiedDataSet };
  }

  filterLessKPI(inputData) {
    let result = [];
    result = inputData?.filter(kpiData => kpiData.filter1.toLowerCase() === 'overall');
    return { chartData: result };
  }

  tabularKPI(inputData, color) {
    const dataGroup1 = inputData.dataGroup?.dataGroup1;
    const issueData = inputData.issueData;
    const chartData: any = [];
    dataGroup1?.forEach((group: any, index) => {
        let filteredIssues;
        let aggregateVal = 0;
        let filteredVal = 0;
        if (!group.key1 || group.key1 !== 'Category') {} else {
            filteredIssues = issueData.filter((issue: any) => issue[group.key1].includes(group.value1));
            if (group.aggregation === 'count') {
                if (filteredIssues?.length) {
                    filteredVal = filteredIssues.length;
                }
            } else if (group.aggregation === 'sum') {
                if (filteredIssues?.length) {
                    filteredVal = this.convertToHoursIfTime(filteredIssues.reduce((sum: any, issue: any) => {
                        return sum + (issue[group.key] || 0); // Sum up the values for the key
                    }, 0), group.unit);
                }
            }
        }
        if (group.aggregation === 'count') {
            aggregateVal = issueData.length;
        } else if (group.aggregation === 'sum') {
            aggregateVal = this.convertToHoursIfTime(issueData.reduce((sum: any, issue: any) => {
                return sum + (issue[group.key] || 0); // Sum up the values for the key
            }, 0), group.unit);
        }
        if (group.key1) {
            if (group.showDenominator) {
                chartData.push({
                    category: group.name,
                    value: filteredVal + '/' + aggregateVal,
                    icon: this.iconObj[group.name],
                    color: color[index]
                });
            } else {
                chartData.push({
                    category: group.name,
                    value: filteredVal,
                    icon: this.iconObj[group.name],
                    color: color[index]
                });
            }
        } else {
            chartData.push({
                category: group.name,
                value: aggregateVal,
                icon: this.iconObj[group.name],
                color: color[index]
            });
        }
    });
    return {
        chartData: chartData
    };
}

  tabularKPINonRawData(inputData, issueData = []) {
    const chartData: any = [];

    inputData?.forEach((group: any, index) => {
      chartData.push({
        category: group.name,
        value: group.unit && (group.unit === 'day' || group.unit === 'SP') ?
          this.convertToHoursIfTime(group.kpiValue, group.unit) : !isNaN(group.kpiValue1) ? group.kpiValue1 + '/' + group.kpiValue : group.kpiValue,
        icon: this.iconObj[group.name],
        totalIssues: 100
      });
    });

    return { chartData: chartData };
  }

  convertToHoursIfTime(val, unit) {
    const isLessThanZero = val < 0;
    val = Math.abs(val);
    const hours = unit === 'SP' ? val : val / 60;
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
    } else if (unit === 'SP') {
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

  getChartDataSet(inputData, chartType, color, key?: any) {
    let returnDataSet;
    switch (chartType) {
      case 'stacked-bar-chart':
        returnDataSet = this.stackedBarChartData(inputData, color, key);
        break;
      case 'bar-chart':
        returnDataSet = this.barChartData(inputData, color);
        break;
      case 'stacked-bar':
        returnDataSet = this.stackedChartData(inputData, color, key);
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
        returnDataSet = this.tabularKPI(inputData, color);
        break;
      case 'tableNonRawData':
        returnDataSet = this.tabularKPINonRawData(inputData.dataGroup?.dataGroup1, inputData?.issueData);
        break;
      case 'grouped-bar-chart':
        returnDataSet = this.groupedBarChartData(inputData, color,key);
        break;
      case 'tabular-with-donut-chart':
        returnDataSet = this.tabularKPINonRawData(inputData.dataGroup?.dataGroup1, inputData?.issueData);
        break;
      default:
        break;
    }
    return returnDataSet;
  }
}
