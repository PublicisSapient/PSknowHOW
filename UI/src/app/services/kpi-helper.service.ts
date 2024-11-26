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
  constructor() {}

  stackedBarChartData(inputData: any, color: any) {
    const dataGroup1 = inputData.dataGroup?.dataGroup1;
    const issueData = inputData.issueData;
    const categoryGroup = inputData.categoryData?.categoryGroup;

    if (!dataGroup1 || dataGroup1.length === 0) {
      throw new Error('Invalid data: Missing dataGroup1');
    }

    const chartData: any = [];

    // Handle categoryGroup if present
    if (categoryGroup && dataGroup1[0]?.showAsLegend === false) {
      categoryGroup.forEach((category: any, index) => {
        // Filter issues matching the categoryName
        const filteredIssues = issueData.filter(
          (issue: any) => issue.Category[0] === category.categoryName,
        );

        chartData.push({
          category: category.categoryName,
          value: filteredIssues.length, // Count of issues for this category
          color: color[index % color.length],
        });
      });
    } else {
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
    }

    const totalCount = chartData.reduce((sum: any, issue: any) => {
      return sum + (issue.value || 0); // Sum up the values for the key
    }, 0);
    return { chartData, totalCount };
  }
}
