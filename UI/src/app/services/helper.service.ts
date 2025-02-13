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

import { Injectable, EventEmitter } from '@angular/core';
import { HttpService } from './http.service';
import { ExcelService } from './excel.service';
import { SharedService } from './shared.service';
import { CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';
import { environment } from 'src/environments/environment';
import { ActivatedRoute, Router } from '@angular/router';
import { catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';
@Injectable()
export class HelperService {
  isKanban = false;
  grossMaturityObj = {};
  public passMaturityToFilter;

  constructor(private httpService: HttpService, private excelService: ExcelService, private sharedService: SharedService, private router: Router, private route: ActivatedRoute) {
    this.passMaturityToFilter = new EventEmitter();
  }

  // use to download excel of kpis
  downloadExcel(kpiId, kpiName, isKanban, filterApplyData, filterData, sprintIncluded) {
    const downloadJson: any = {};
    downloadJson.ids = filterApplyData.ids || [];
    downloadJson.level = filterApplyData.level || '1';
    downloadJson.endDate = filterApplyData.endDate || '';
    downloadJson.startDate = filterApplyData.startDate || '';
    downloadJson.label = filterApplyData.label || '';
    downloadJson.kpiList = [];
    downloadJson.sprintIncluded = sprintIncluded;

    if (filterApplyData && filterApplyData.selectedMap) {
      downloadJson.selectedMap = filterApplyData.selectedMap;
    } else {
      downloadJson.selectedMap = {};
      downloadJson.selectedMap[filterData[0].label] = [];
      for (let i = 0; i < filterData[0].filterData.length; i++) {
        downloadJson.selectedMap[filterData[0].label].push(filterData[0].filterData[i].nodeId);
      }
    }
    if (isKanban === true) {
      downloadJson['selectedMap']['sprint'] = [];
    }
    return this.httpService.downloadExcel(downloadJson, kpiId);

  }

  // this is used for making request object for kpi .Here first parameter is kpi source i.e
  // sonar , jira etc and second parameter is Kanban is true or false
  // type is quality or productivity
  groupKpiFromMaster(kpiSource, isKanban, masterData, filterApplyData, filterData, kpiIdsForCurrentBoard, type, selectedTab) {
    const kpiRequestObject = <any>{};
    const visibleKpis = masterData?.filter(obj => obj.isEnabled && obj.shown).map(x => x.kpiId);
    kpiRequestObject.kpiList = <any>[];
    for (let i = 0; i < masterData?.length; i++) {
      const obj = { ...masterData[i]?.kpiDetail };
      obj['chartType'] = '';
      let condition = obj.kpiSource === kpiSource && obj.kanban === isKanban;

      if (type && type !== '' && !isNaN(type)) {
        condition = (obj.groupId && obj.groupId === type) && condition;
      }

      if (kpiIdsForCurrentBoard && kpiIdsForCurrentBoard.length && obj?.kpiId) {
        condition = kpiIdsForCurrentBoard.includes(obj.kpiId) && condition;
      }

      if (condition) {
        if (obj.videoLink) {
          delete obj.videoLink;
        }
        // if (obj.hasOwnProperty('isEnabled') && obj.hasOwnProperty('shown')) {
        //     if (obj.isEnabled && obj.shown) {
        //         if (!kpiRequestObject.kpiList.filter(kpi => kpi.kpiId === obj.kpiId)?.length) {
        //             kpiRequestObject.kpiList.push(obj)
        //         }
        //     }
        // }
        // else if (visibleKpis.includes(obj.kpiId)) {
        //     if (!kpiRequestObject.kpiList.filter(kpi => kpi.kpiId === obj.kpiId)?.length) {
        //         kpiRequestObject.kpiList.push(obj)
        //     }
        // }
        kpiRequestObject.kpiList.push(obj);
      }
    }

    if (filterApplyData && filterApplyData.ids && filterApplyData.level && filterApplyData.selectedMap && filterApplyData.sprintIncluded) {
      kpiRequestObject.ids = filterApplyData.ids;
      kpiRequestObject.level = filterApplyData.level;
      kpiRequestObject.selectedMap = filterApplyData.selectedMap;
      kpiRequestObject.sprintIncluded = filterApplyData.sprintIncluded;
      kpiRequestObject.label = filterApplyData.label;
      // start date and end Date is required in kanban so ading it if no filter Selected
      if (isKanban) {
        let onlyDateAvailable = true;
        for (const obj in filterApplyData.selectedMap) {
          if (filterApplyData.selectedMap[obj].length !== 0 && obj !== 'Date') {
            onlyDateAvailable = false;
            break;
          }
        }
        if (onlyDateAvailable) {
          for (let i = 0; i < filterData[0].filterData.length; i++) {
            kpiRequestObject.selectedMap[filterData[0].label].push(filterData[0].filterData[i].nodeId);
          }
        }
        if (filterApplyData.startDate && filterApplyData.endDate) {
          kpiRequestObject.startDate = filterApplyData.startDate;
          kpiRequestObject.endDate = filterApplyData.endDate;
        } else {
          kpiRequestObject.startDate = '';
          kpiRequestObject.endDate = '';
        }
      }
    } else {
      kpiRequestObject.ids = [];
      kpiRequestObject.level = filterData[2]?.level;
      kpiRequestObject.selectedMap = {};
      kpiRequestObject.selectedMap[filterData[2]?.label] = [];
      kpiRequestObject.label = filterApplyData.label;
      for (let i = 0; i < filterData[2]?.filterData?.length; i++) {
        kpiRequestObject.ids.push(filterData[2]?.filterData[i]?.nodeId);
        kpiRequestObject.selectedMap[filterData[2]?.label].push(filterData[2]?.filterData[i]?.nodeId);
      }
    }
    return kpiRequestObject;
  }

  // common function to calculate test execution data for Scrum and Kanban
  calculateTestExecutionData(requiredKpi, isKanban, zypherKpiData) {
    // empty the chart data and filter dropdown
    let selectedTestExecutionFilterData;
    const testExecutionFilterData = [];

    if (zypherKpiData && zypherKpiData[requiredKpi] && zypherKpiData[requiredKpi]['trendValueList'] && isKanban) {
      // creating Zypher filter and finding unique keys from  the test execution  kpis
      const uniqueKeys = new Set();
      for (const index in zypherKpiData[requiredKpi]['trendValueList']) {
        const val = zypherKpiData[requiredKpi]['trendValueList'][index].data;
        if (val !== 'aggregatedValue') {
          uniqueKeys.add(val);
        }
      }

      let arrayDetails = [];
      arrayDetails = Array.from(uniqueKeys);
      arrayDetails.sort();
      arrayDetails.unshift('aggregatedValue');
      arrayDetails.forEach((obj) => {
        let tempobj;
        zypherKpiData[requiredKpi]['trendValueList'].map((selectedObj) => {
          if (selectedObj.data === obj) {
            if (obj !== 'aggregatedValue') {
              tempobj = {
                label: obj,
                value: {
                  trendValue: selectedObj.value,
                  aggregatedValue: zypherKpiData[requiredKpi].value[obj]
                }
              };
            } else {
              tempobj = {
                label: 'Overall',
                value: {
                  trendValue: selectedObj.value,
                  aggregatedValue: zypherKpiData[requiredKpi].value[obj]
                }
              };
              // by default selecting Select from the drop down in sonar filter
              selectedTestExecutionFilterData = tempobj.value;
            }
          }
        });
        testExecutionFilterData.push(tempobj);
      });
    }
    // sort array by kanbanDate if Kanban
    if (isKanban && selectedTestExecutionFilterData && selectedTestExecutionFilterData.trendValue) {
      selectedTestExecutionFilterData.trendValue.sort(function compare(a, b) {
        const dateA = +new Date(a.kanbanDate);
        const dateB = +new Date(b.kanbanDate);
        return dateA - dateB;
      });
    }
    const objToReturn = {
      selectedTestExecutionFilterData,
      testExecutionFilterData
    };
    return objToReturn;

  }

  // creating Sonar filter and finding unique keys from all the sonar kpis
  createSonarFilter(sonarKpiData, selectedtype) {
    const sonarFilterData = [];
    const uniqueKeys = new Set();
    for (const index in sonarKpiData) {
      const kpiObj = sonarKpiData[index];
      for (const obj in kpiObj.value) {
        uniqueKeys.add(obj);
      }
    }
    let arrayDetails = [];
    arrayDetails = Array.from(uniqueKeys);
    arrayDetails.sort();
    let hasOverallValue = false;
    arrayDetails.forEach(obj => {
      hasOverallValue = (selectedtype === 'Kanban' && obj === 'Overall') ? true : false;
      const tempobj = {
        label: obj,
        value: obj
      };
      if (obj !== 'aggregatedValue' && (selectedtype === 'Kanban' && obj !== 'Overall')) {
        sonarFilterData.push(tempobj);
      }
    });
    if (selectedtype === 'Scrum') {
      const selectObj = {
        label: 'Overall',
        value: 'aggregatedValue'
      };
      sonarFilterData.unshift(selectObj);
    } else {
      if (hasOverallValue) {
        const selectObj = {
          label: 'Overall',
          value: 'Overall'
        };
        sonarFilterData.unshift(selectObj);
      }
    }
    return sonarFilterData;
  }

  // creating array into object where key is kpi id
  createKpiWiseId(data) {
    const newObject = {};
    for (const obj in data) {
      newObject[data[obj].kpiId] = data[obj];
    }
    return newObject;
  }

  // returns colors according to maturity for all
  colorAccToMaturity(maturity) {
    const green = '#AEDB76';
    const red = '#F06667';
    const yellow = '#eff173';
    const orange = '#ffc35b';
    const darkGreen = '#6cab61';
    const blue = '#44739f';
    let fillColor;

    if (maturity === '1') {
      fillColor = red;
    } else if (maturity === '2') {
      fillColor = orange;
    } else if (maturity === '3') {
      fillColor = yellow;
    } else if (maturity === '4') {
      fillColor = green;
    } else if (maturity === '5') {
      fillColor = darkGreen;
    } else {
      fillColor = blue;
    }
    return fillColor;
  }


  // check aggType is percentile , sum , median or Average and return tooltip eg value according to it
  toolTipValue(a, b, aggType, percentile) {
    if (aggType === 'percentile') {
      const data = [a, b];
      const array = data;
      array.sort(function (a1, b1) {
        return a1 - b1;
      });
      let index = percentile / 100. * (array.length);
      index = Math.round(index);
      const result = array[index - 1];
      return result;
    } else if (aggType === 'sum') {
      return (a + b);
    } else if (aggType === 'median' || aggType === 'average') {
      return (a + b) / 2;
    }
  }

  // sort objects based on properties
  sortObject(unordered) {
    if (unordered) {
      return Object.keys(unordered).sort().reduce(
        (obj, key) => {
          obj[key] = unordered[key];
          return obj;
        },
        {}
      );
    }
    return unordered;
  }

  // compare filters
  compareFilters(obj1, obj2, kanbanActivated) {

    if (this.isKanban !== kanbanActivated) {
      this.isKanban = kanbanActivated;
      return false;
    }

    obj1 = this.sortObject(obj1);
    obj2 = this.sortObject(obj2);

    if (!obj1 && !obj2 || !obj1 && !Object.keys(obj2).length || obj1 && !Object.keys(obj1).length && !obj2) {
      return false;
    }

    if (JSON.stringify(obj1) === JSON.stringify(obj2)) {
      return true;
    } else {
      return false;
    }
  }

  // calculate gross maturity
  /*calculateGrossMaturity(data, globalConfig) {
      if (data && Object.keys(data)?.length && globalConfig?.length && globalConfig[0] !== undefined) {
          const self = this;
          self.grossMaturityObj = {};
          Object.keys(data)?.forEach(key => {
              data[key]?.forEach(element => {
                  if (element.data) {
                      if (typeof element.data === 'string' || element.data instanceof String) {
                          self.grossMaturityObj[element.data] = 0;
                      }
                  }
                  // else if (element.value[0].data) {
                  //     self.grossMaturityObj[element.value[0].data] = 0;
                  // }
              });
          });

          let divisor = 0;
          Object.keys(data)?.forEach(key => {
              data[key]?.forEach(element => {
                  let shouldIncludeMaturity = globalConfig.filter(configData => configData.kpiId === key);
                  if (shouldIncludeMaturity.length) {
                      // console.log(key, shouldIncludeMaturity[0]['kpiDetail'].kpiName, shouldIncludeMaturity[0]['kpiDetail'].calculateMaturity, parseFloat(element.maturity));
                      shouldIncludeMaturity = shouldIncludeMaturity[0]['kpiDetail'].calculateMaturity;

                      if (shouldIncludeMaturity === true && element.data) {
                          if (typeof element.data === 'string' || element.data instanceof String) {
                              self.grossMaturityObj[element.data] += parseFloat((element.maturity ? parseFloat(element.maturity) : 0) + '');
                          }
                      }
                      // else if (shouldIncludeMaturity === true && element.value[0].data) {
                      //     self.grossMaturityObj[element.value[0].data] += parseFloat((element.maturity ? parseFloat(element.maturity) : 0) + '');
                      // }
                  }
              });
              let shouldCalculateMaturity = globalConfig.filter(configData => configData.kpiId === key);
              if (shouldCalculateMaturity.length) {
                  shouldCalculateMaturity = shouldCalculateMaturity[0]['kpiDetail'].calculateMaturity;
              }
              if (shouldCalculateMaturity === true) {
                  divisor++;
              }
          });

          Object.keys(self.grossMaturityObj)?.forEach(key => {
              // console.log(self.grossMaturityObj[key], devisor);
              if (divisor) {
                  self.grossMaturityObj[key] = self.grossMaturityObj[key] / divisor;
              }
          });
          // setInterval(() => {
          //     this.passMaturityToFilter.emit(self.grossMaturityObj);
          // }, 500);
          if (Object.keys(self.grossMaturityObj).length) {
              this.passMaturityToFilter.emit(self.grossMaturityObj);
          }
      }
  }*/


    sortAlphabetically(objArray) {
        if (objArray && objArray.length > 1) {
            objArray.sort((a, b) => {
                const aName = a.nodeDisplayName || a.nodeName || a.data || a.date || a;
                const bName = b.nodeDisplayName || b.nodeName || b.data || b.date || b;
                if (typeof aName === 'string' && typeof bName === 'string') {
                    return aName.localeCompare(bName);
                }
            });
        }
        return objArray;
    }

  sortByField(objArray, propArr): any {
    objArray.sort((a, b) => {
      if (objArray?.[0]?.[propArr[0]] && propArr[0].indexOf('Date') === -1) {
        const propA = a[propArr[0]];
        const propB = b[propArr[0]];
        return propA.localeCompare(propB);
      }
    });

    objArray.sort((a, b) => {
      if (objArray?.[0]?.[propArr[1]] && propArr[1].indexOf('Date') !== -1) {
        const propA = new Date(a[propArr[1]].substring(0, a[propArr[1]].indexOf('T')));
        const propB = new Date(b[propArr[1]].substring(0, b[propArr[1]].indexOf('T')));
        return +propB - +propA;
      }
    });

    return objArray;
  }

  releaseSorting(releaseList) {
    if (releaseList && releaseList.length) {
      releaseList.sort((a, b) => {
        // First, sort by releaseState (Unreleased first, Released second)
        if (a.releaseState === 'Unreleased' && b.releaseState === 'Released') {
          return -1;
        } else if (a.releaseState === 'Released' && b.releaseState === 'Unreleased') {
          return 1;
        }

        // Both are in the same state, so we sort by releaseEndDate
        const dateA = a.releaseEndDate ? new Date(a.releaseEndDate).getTime() : null;
        const dateB = b.releaseEndDate ? new Date(b.releaseEndDate).getTime() : null;

        if (a.releaseState === 'Unreleased') {
          // For Unreleased, sort by ascending releaseEndDate, keeping null dates last
          if (dateA === null) return 1;
          if (dateB === null) return -1;
          return dateA - dateB;
        } else {
          // For Released, sort by descending releaseEndDate, keeping null dates last
          if (dateA === null) return 1;
          if (dateB === null) return -1;
          return dateB - dateA;
        }
      });
      return releaseList
    } else {
      return [];
    }
  }

  /** logic to apply multiselect filter */
  applyAggregationLogic(obj, aggregationType, percentile) {
    const arr = JSON.parse(JSON.stringify(obj[Object.keys(obj)[0]]));
    for (let i = 0; i < Object.keys(obj).length; i++) {
      for (let j = 0; j < obj[Object.keys(obj)[i]].length; j++) {
        if (arr.findIndex(x => x.data == obj[Object.keys(obj)[i]][j]['data']) == -1) {
          arr.push(obj[Object.keys(obj)[i]][j]);
        }
      }
    }
    let aggArr = [];
    aggArr = arr?.map(item => ({
      ...item,
      aggregationValue: item?.hasOwnProperty('aggregationValue') ? [] : null,
      value: item.value.map(x => ({
        ...x,
        value: (typeof x.value === 'object') ? {} : [],
        allHoverValue: [],
        lineValue: x?.hasOwnProperty('lineValue') ? (typeof x.lineValue === 'object') ? {} : [] : null
      }))
    }));

    aggArr = this.sortAlphabetically(aggArr);

    for (const key in obj) {
      for (let i = 0; i < obj[key]?.length; i++) {
        const idx = aggArr?.findIndex(x => x?.data == obj[key][i]?.data);
        if (aggArr[idx].hasOwnProperty('aggregationValue') && obj[key][i]?.hasOwnProperty('aggregationValue')) {
          let tempArr = aggArr[idx]['aggregationValue'] ? [...aggArr[idx]['aggregationValue'], obj[key][i]['aggregationValue']] : [obj[key][i]['aggregationValue']]
          aggArr[idx]['aggregationValue'] = [...tempArr];
        }
        if (idx != -1) {
          for (let j = 0; j < obj[key][i]?.value?.length; j++) {
            if (!Array.isArray(aggArr[idx]?.value[j]?.value)) {
              aggArr[idx].value[j].value = { ...aggArr[idx]?.value[j]?.value, ...obj[key][i]?.value[j]?.value };
              if (aggArr[idx]?.value[j]?.hasOwnProperty('lineValue') && aggArr[idx]?.value[j]?.lineValue != null) {
                aggArr[idx].value[j].lineValue = { ...aggArr[idx]?.value[j]?.lineValue, ...obj[key][i]?.value[j]?.lineValue };
              }
            } else {
              aggArr[idx].value[j].value.push(obj[key][i]?.value[j]?.value);
              aggArr[idx].value[j].allHoverValue.push(obj[key][i]?.value[j]?.hoverValue);
              aggArr[idx].value[j].value.sort();
              if (aggArr[idx]?.value[j]?.hasOwnProperty('lineValue') && aggArr[idx]?.value[j]?.lineValue != null) {
                aggArr[idx].value[j].lineValue.push(obj[key][i]?.value[j]?.lineValue);
                aggArr[idx].value[j].lineValue.sort();
              }
              if (aggArr[idx]?.value[j]?.hasOwnProperty('hoverValue') && aggArr[idx]?.value[j]?.hoverValue != null && Object.keys(aggArr[idx]?.value[j]?.hoverValue).length > 0) {
                aggArr[idx].value[j].hoverValue = { ...aggArr[idx]?.value[j]?.hoverValue, ...obj[key][i]?.value[j]?.hoverValue };
              }
            }
          }
        }
      }
    }


    if (Array.isArray(aggArr[0]?.value[0]?.value)) {
      if (aggregationType?.toLowerCase() == 'average') {
        for (let i = 0; i < aggArr?.length; i++) {
          aggArr[i].value?.map(x => {
            x.value = parseFloat(((x.value?.reduce((partialSum = 0, a) => partialSum + a, 0)) / x.value?.length).toFixed(2));
            x.data = x.value;
            if (x.hasOwnProperty('lineValue') && x?.lineValue != null) {
              x.lineValue = parseFloat(((x.lineValue?.reduce((partialSum, a) => partialSum + a, 0)) / x.lineValue?.length).toFixed(2));
            }
            return x;
          });
        }
      }

      if (aggregationType?.toLowerCase() == 'sum') {
        for (let i = 0; i < aggArr?.length; i++) {
          if (aggArr[i]?.hasOwnProperty('aggregationValue')) {
            aggArr[i]['aggregationValue'] = aggArr[i]['aggregationValue']?.reduce((partialSum, a) => (partialSum + parseFloat(a)), 0);

          }
          aggArr[i].value?.map(x => {
            x.value = (x.value?.reduce((partialSum, a) => partialSum + a, 0));
            x.data = x.value;
            x.hoverValue = x?.allHoverValue && x?.allHoverValue?.length ? this.aggregateHoverValues(x?.allHoverValue) : {}
            if (x.hasOwnProperty('lineValue') && x?.lineValue != null) {
              x.lineValue = (x.lineValue?.reduce((partialSum, a) => partialSum + a, 0));
            }
            return x;
          });
        }
      }

      if (aggregationType?.toLowerCase() == 'median') {
        for (let i = 0; i < aggArr?.length; i++) {
          aggArr[i].value?.map(x => {
            x.value?.length % 2 !== 0 ?
              x.value = x.value?.length == 1 ? x.value : parseFloat((x.value[((x.value?.length + 1) / 2)])?.toFixed(2)) :
              x.value = parseFloat(((x.value[(x.value?.length / 2) - 1] + x.value[((x.value?.length / 2) + 1) - 1]) / 2)?.toFixed(2));
            x.data = x.value;
            if (x.hasOwnProperty('lineValue') && x?.lineValue != null) {
              x.lineValue?.length % 2 !== 0 ?
                x.lineValue = x.lineValue?.length == 1 ? x.lineValue : parseFloat((x.lineValue[((x.lineValue?.length + 1) / 2)])?.toFixed(2)) :
                x.lineValue = parseFloat(((x.lineValue[(x.lineValue?.length / 2) - 1] + x.lineValue[((x.lineValue?.length / 2) + 1) - 1]) / 2)?.toFixed(2));
            }
            return x;
          });
        }
      }
      if (aggregationType?.toLowerCase() == 'percentile') {
        for (let i = 0; i < aggArr?.length; i++) {
          aggArr[i].value?.map(x => {
            x.value?.sort(function (a1, b1) {
              return a1 - b1;
            });
            let index = ((percentile / 100) * (x.value?.length));
            index = Math.round(index);
            x.value = index != 0 ? x.value[index - 1] : x.value[index];
            x.data = x.value;
            if (x.hasOwnProperty('lineValue') && x?.lineValue != null) {
              x.lineValue?.sort(function (a1, b1) {
                return a1 - b1;
              });
              let index = ((percentile / 100) * (x.lineValue?.length));
              index = Math.round(index);
              x.lineValue = index != 0 ? x.lineValue[index - 1] : x.lineValue[index];
            }
            return x;
          });
        }
      }
    }
    return aggArr;
  }

  aggregateHoverValues(objects: any[]): any {
    return objects.reduce((acc, obj) => {
      Object.keys(obj).forEach((key) => {
        acc[key] = (acc[key] || 0) + obj[key];
      });
      return acc;
    }, {});
  }


  getKpiCommentsHttp(data) {
    return new Promise((resolve, reject) => this.httpService.getCommentCount(data).subscribe((response) => {
      if (response.success) {
        resolve({ ...response.data });
      } else {
        resolve({});
      }
    }, error => {
      reject(error);
    }));
  }

  /** sync shown property of project level and user level */
  makeSyncShownProjectLevelAndUserLevelKpis(projectLevelKpi, userLevelKpi) {
    Object.keys(userLevelKpi).forEach(boards => {
      if (Array.isArray(userLevelKpi[boards])) {
        userLevelKpi[boards].forEach(boardA => {
          const boardB = projectLevelKpi[boards]?.find(b => b.boardId === boardA.boardId);
          if (boardB) {
            boardA.kpis.forEach(kpiA => {
              const kpiB = boardB.kpis.find(b => b.kpiId === kpiA.kpiId);
              if (kpiB) {
                kpiA.shown = kpiB.shown;
              }
            });
          }
        });
      }
    });
    return userLevelKpi
  }

  getGlobalConfig() {
    this.httpService.getConfigDetails().subscribe(res => {
      if (res) {
        this.sharedService.setGlobalConfigData(res);
      }
    })
  }

  windowReload() {
    window.location.reload();
  }


  drop(event: CdkDragDrop<string[]>, updatedContainer, navigationTabs, upDatedConfigData, configGlobalData, extraKpis?) {
    if (event?.previousIndex !== event.currentIndex) {
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
      if (updatedContainer.width === 'half') {
        const updatedTabsDetails = navigationTabs.find(tabs => tabs['label'].toLowerCase() === updatedContainer['label'].toLowerCase());
        updatedTabsDetails['kpis'] = [...updatedTabsDetails['kpiPart1'], ...updatedTabsDetails['kpiPart2'], ...updatedTabsDetails['fullWidthKpis']];
      }
      upDatedConfigData = [];
      navigationTabs.forEach(tabs => {
        upDatedConfigData = upDatedConfigData.concat(tabs['kpis']);
      })
      upDatedConfigData.map((kpi, index) => kpi.order = index + 3);
      const disabledKpis = configGlobalData.filter(item => item.shown && !item.isEnabled);
      disabledKpis.map((kpi, index) => kpi.order = upDatedConfigData.length + index + 3);
      const hiddenkpis = configGlobalData.filter(item => !item.shown);
      hiddenkpis.map((kpi, index) => kpi.order = upDatedConfigData.length + disabledKpis.length + index + 3);
      if (extraKpis) {
        this.sharedService.kpiListNewOrder.next([extraKpis, ...upDatedConfigData, ...disabledKpis, ...hiddenkpis]);
      } else {
        this.sharedService.kpiListNewOrder.next([...upDatedConfigData, ...disabledKpis, ...hiddenkpis]);
      }

    }
  }

  createCombinations(arr1, arr2) {
    let arr = [];
    for (let i = 0; i < arr1?.length; i++) {
      for (let j = 0; j < arr2?.length; j++) {
        arr.push({ filter1: arr1[i], filter2: arr2[j] });
      }
    }
    return arr;
  }

  makeUniqueArrayList(arr) {
    let uniqueArray = [];
    for (let i = 0; i < arr?.length; i++) {
      const idx = uniqueArray?.findIndex((x) => x.nodeId == arr[i]?.nodeId);
      if (idx == -1) {
        uniqueArray = [...uniqueArray, arr[i]];
        uniqueArray[uniqueArray?.length - 1]['path'] = Array.isArray(uniqueArray[uniqueArray?.length - 1]['path']) ? [...uniqueArray[uniqueArray?.length - 1]['path']] : [uniqueArray[uniqueArray?.length - 1]['path']];
        uniqueArray[uniqueArray?.length - 1]['parentId'] = Array.isArray(uniqueArray[uniqueArray?.length - 1]['parentId']) ? [...uniqueArray[uniqueArray?.length - 1]['parentId']] : [uniqueArray[uniqueArray?.length - 1]['parentId']]
      } else {
        uniqueArray[idx].path = [...uniqueArray[idx]?.path, arr[i]?.path];
        uniqueArray[idx].parentId = [...uniqueArray[idx]?.parentId, arr[i]?.parentId];
      }
    }
    return uniqueArray;
  }

  async getKpiCommentsCount(kpiCommentsCountObj, nodes, level, nodeChildId, updatedConfigGlobalData, kpiId) {
    let requestObj = {
      "nodes": [...nodes],
      "level": level,
      "nodeChildId": nodeChildId,
      'kpiIds': []
    };
    if (kpiId) {
      requestObj['kpiIds'] = [kpiId];
      await this.getKpiCommentsHttp(requestObj).then((res: object) => {
        kpiCommentsCountObj[kpiId] = res[kpiId];
      });
    } else {
      requestObj['kpiIds'] = (updatedConfigGlobalData?.map((item) => item.kpiId));
      await this.getKpiCommentsHttp(requestObj).then((res: object) => {
        kpiCommentsCountObj = res;
      });
    }
    return kpiCommentsCountObj
  }


  // old UI method, removing
  // createBackupOfFiltersSelection(filterbackup, tab, subFilter) {
  //   let savedDetails = this.sharedService.getAddtionalFilterBackup();
  //   if (tab === 'backlog') {
  //     let tabSpecfic = (savedDetails['kpiFilters'] && savedDetails['kpiFilters'][tab]) ? savedDetails['kpiFilters'][tab] : {}
  //     savedDetails = { ...savedDetails, kpiFilters: { ...savedDetails['kpiFilters'], ...{ [tab]: { ...tabSpecfic, ...filterbackup } } } };
  //   } else {
  //     const subFilterValues = (savedDetails['kpiFilters'] && savedDetails['kpiFilters'][tab] && savedDetails['kpiFilters'][tab][subFilter]) ? savedDetails['kpiFilters'][tab][subFilter] : {};
  //     const combineSubFilterValues = { ...subFilterValues, ...filterbackup };
  //     savedDetails = { ...savedDetails, kpiFilters: { ...savedDetails['kpiFilters'], ...{ [tab]: { [subFilter]: combineSubFilterValues } } } };
  //   }
  //   this.sharedService.setAddtionalFilterBackup(savedDetails);
  // }

  // old UI Method, removing
  // setFilterValueIfAlreadyHaveBackup(kpiId, kpiSelectedFilterObj, tab, refreshValue, initialValue, subFilter, filters?) {
  //   let haveBackup = {}

  //   if (tab === 'backlog') {
  //     if (this.sharedService.getAddtionalFilterBackup().hasOwnProperty('kpiFilters') && this.sharedService.getAddtionalFilterBackup()['kpiFilters'].hasOwnProperty(tab) && this.sharedService.getAddtionalFilterBackup()['kpiFilters'][tab].hasOwnProperty(kpiId)) {
  //       haveBackup = this.sharedService.getAddtionalFilterBackup()['kpiFilters'][tab][kpiId];
  //     }
  //   } else {
  //     if (this.sharedService.getAddtionalFilterBackup().hasOwnProperty('kpiFilters') && this.sharedService.getAddtionalFilterBackup()['kpiFilters'].hasOwnProperty(tab) && this.sharedService.getAddtionalFilterBackup()['kpiFilters'][tab].hasOwnProperty(subFilter)) {
  //       haveBackup = this.sharedService.getAddtionalFilterBackup()['kpiFilters'][tab][subFilter][kpiId];
  //     }
  //   }

  //   kpiSelectedFilterObj[kpiId] = refreshValue;
  //   if (haveBackup && Object.keys(haveBackup).length) {
  //     if (filters) {
  //       const tempObj = {};
  //       for (const key in haveBackup) {
  //         tempObj[key] = haveBackup[key];
  //       }
  //       kpiSelectedFilterObj[kpiId] = { ...tempObj };
  //     }
  //     else if (Array.isArray(refreshValue)) {
  //       kpiSelectedFilterObj[kpiId] = haveBackup;
  //     } else {
  //       kpiSelectedFilterObj[kpiId] = { 'filter1': haveBackup['filter1'] };;
  //     }

  //   } else {
  //     if (filters) {
  //       const tempObj = {};
  //       for (const key in filters) {
  //         tempObj[key] = initialValue;
  //       }
  //       kpiSelectedFilterObj[kpiId] = { ...tempObj };
  //     }
  //     else if (Array.isArray(refreshValue)) {
  //       kpiSelectedFilterObj[kpiId]?.push(initialValue);
  //     } else {
  //       kpiSelectedFilterObj[kpiId] = { 'filter1': initialValue }
  //     }
  //   }
  //   this.createBackupOfFiltersSelection(kpiSelectedFilterObj, tab, subFilter);
  //   this.sharedService.setKpiSubFilterObj(kpiSelectedFilterObj);
  //   return kpiSelectedFilterObj;
  // }

  logoutHttp() {
    this.httpService.logout().subscribe((responseData) => {
      // if (responseData?.success) {
      if (!environment['AUTHENTICATION_SERVICE']) {
        this.isKanban = false;
        // Set blank selectedProject after logged out state
        this.sharedService.setSelectedProject(null);
        this.httpService.setCurrentUserDetails({});
        this.sharedService.setUserDetailsAsBlankObj();
        this.sharedService.setVisibleSideBar(false);
        this.sharedService.setAddtionalFilterBackup({});
        this.sharedService.setKpiSubFilterObj({});
        this.sharedService.setBackupOfFilterSelectionState(null); // -> SENDING NULL SO THAT SELECTED FILTERS ARE RESET ON LOGOUT
        localStorage.clear();
        this.router.navigate(['./authentication/login']).then(() => {
          // window.location.reload();
        });
      } else {
        let redirect_uri = window.location.href;
        window.location.href = environment.CENTRAL_LOGIN_URL + '?redirect_uri=' + redirect_uri;
      }
      //   }
    })
  }

  getObjectKeys(obj) {
    if (obj && Object.keys(obj).length) {
      return Object.keys(obj);
    } else {
      return [];
    }
  }

  checkDataAtGranularLevel(data, chartType, selectedTab) {
    if (selectedTab === "developer" && data?.length) {
      return true;
    }
    if (!data || !data?.length) {
      return false;
    }
    let dataCount = 0;
    if (Array.isArray(data)) {
      data?.forEach(item => {
        if (Array.isArray(item.data) && item.data?.length) {
          ++dataCount;
        } else if (item.data && !isNaN(parseInt(item.data))) {
          // dataCount += item?.data;
          ++dataCount;
        } else if (item.value && (this.checkIfArrayHasData(item) || Object.keys(item.value)?.length)) {
          if (item.value[0]?.hasOwnProperty('data') && this.checkAllValues(item.value, 'data', chartType)) {
            if (chartType !== 'pieChart' && chartType !== 'horizontalPercentBarChart') {
              ++dataCount;
            } else if (this.checkAllValues(item.value, 'data', chartType)) {
              ++dataCount;
            }
          } else if (this.checkIfArrayHasData(item.value[0])) {
            if (chartType !== 'pieChart' && chartType !== 'horizontalPercentBarChart') {
              ++dataCount;
            } else if (this.checkAllValues(item.value[0].value, 'data', chartType)) {
              ++dataCount;
            }
          } else if (item.value.length && chartType !== 'pieChart') {
            ++dataCount;
          }
        } else if (item.dataGroup && item.dataGroup.length) {
          ++dataCount;
        }
      });
    } else if (data && Object.keys(data).length) {
      dataCount = Object.keys(data).length;
    }
    return parseInt(dataCount + '') > 0;
  }

  checkAllValues(arr, prop, chartType) {
    let result = false;
    for (let i = 0; i < arr.length; i++) {
      if (!isNaN(parseInt(arr[i][prop]))) {
        if (chartType === 'pieChart') {
          result = parseInt(arr[i][prop]) > 0;
          break;
        } else {
          result = true;
          break;
        }
      }
    }
    return result;
  }

  checkIfArrayHasData(item) {
    return (Array.isArray(item?.value) && item.value?.length > 0)
  }

  deepEqual(obj1: any, obj2: any): boolean {
    if (typeof obj1 === 'string' && typeof obj2 === 'string' && obj1.toLowerCase() === obj2.toLowerCase()) {
      return true;
    }

    if (obj1 === obj2) {
      return true;
    }

    if (obj1 === null || obj2 === null || typeof obj1 !== 'object' || typeof obj2 !== 'object') {
      return false;
    }

    const keys1 = Object.keys(obj1);
    const keys2 = Object.keys(obj2);

    if (keys1.length !== keys2.length) {
      return false;
    }

    for (const key of keys1) {
      if (!keys2.includes(key) || !this.deepEqual(obj1[key], obj2[key])) {
        return false;
      }
    }

    return true;
  }

  isDropdownElementSelected($event: any): boolean {
    try {
      if ($event.originalEvent.type === 'click' || $event.originalEvent.type === 'keydown') {
        return true;
      } else {
        return false;
      }
    } catch (ex) {
      console.error(ex, 'Not a Browser event');
    }
  }

  transformDateToISO(value: Date | string): string {
    let matches = false
    if (!value) {
      return '-';
    }

    let date: any;
    let time = ''

    if (typeof value === 'string') {
      date = new Date(value);
      const regex = /^(\d{1,2}-(\d{2}|[a-zA-Z]{3})-\d{4}|\d{4}-\d{2}-\d{2})$/i
      matches = regex.test(value.trim());
    }
    if (value instanceof Date) {
      date = value;
    }

    if (isNaN(date.getTime())) {
      return '-';
    } else {
      time = date.getHours() + ':' + date.getMinutes() + ':' + date.getSeconds();
    }
    const monthNames = ['JAN', 'FEB', 'MAR', 'APR', 'MAY', 'JUN', 'JUL', 'AUG', 'SEP', 'OCT', 'NOV', 'DEC'];
    const year = date.getFullYear();
    const month = monthNames[date.getMonth()];
    const day = String(date.getDate()).padStart(2, '0');

    return `${day}-${month}-${year} ${(matches ? '' : time)}`;
  }

  aggregationCycleTime(data) {
    // Object to store intermediate calculations
    const resultData = {};

    // Process each filter
    data.forEach(filter => {
      filter.data.forEach(record => {
        const label = record.label;
        if (!resultData[label]) {
          resultData[label] = { totalValue1: 0, weightedValue: 0 };
        }
        resultData[label].totalValue1 += record.value1;
        resultData[label].weightedValue += record.value * record.value1;
      });
    });

    // Construct the aggregated response
    const aggregatedResponse = {
      filter1: data[0]['filter1'],
      data: Object.entries(resultData).map(([label, values]) => ({
        label,
        value: values['totalValue1'] > 0 ? Math.round(values['weightedValue'] / values['totalValue1']) : 0,
        value1: values['totalValue1'],
        unit: "d",
        unit1: "issues"
      }))
    };

    return aggregatedResponse;
  }


  // url shortening redirection logic
  urlShorteningRedirection() {
    const shared_link = localStorage.getItem('shared_link');
    const currentUserProjectAccess = JSON.parse(localStorage.getItem('currentUserDetails'))?.projectsAccess?.length ? JSON.parse(localStorage.getItem('currentUserDetails'))?.projectsAccess[0]?.projects : [];
    if (shared_link) {
      // Extract query parameters
      const queryParams = new URLSearchParams(shared_link.split('?')[1]);
      const stateFilters = queryParams.get('stateFilters');
      const kpiFilters = queryParams.get('kpiFilters');

      if (stateFilters) {
        let decodedStateFilters: string = '';
        // let stateFiltersObj: Object = {};

        if (stateFilters?.length <= 8) {
          this.httpService.handleRestoreUrl(stateFilters, kpiFilters)
            .pipe(
              catchError((error) => {
                this.router.navigate(['/dashboard/Error']); // Redirect to the error page
                setTimeout(() => {
                  this.sharedService.raiseError({
                    status: 900,
                    message: error.message || 'Invalid URL.'
                  });
                });
                return throwError(error);  // Re-throw the error so it can be caught by a global error handler if needed
              })
            )
            .subscribe((response: any) => {
              if (response.success) {
                const longStateFiltersString = response.data['longStateFiltersString'];
                decodedStateFilters = atob(longStateFiltersString);
                this.urlRedirection(decodedStateFilters, currentUserProjectAccess, shared_link);
              }
            });
        } else {
          decodedStateFilters = atob(stateFilters);
          this.urlRedirection(decodedStateFilters, currentUserProjectAccess, shared_link);
        }
      }
    } else {
      this.router.navigate(['./dashboard/']);
    }
  }

  urlRedirection(decodedStateFilters, currentUserProjectAccess, url) {
    url = decodeURIComponent(url);
    const stateFiltersObjLocal = JSON.parse(decodedStateFilters);

    let stateFilterObj = [];

    if (typeof stateFiltersObjLocal['parent_level'] === 'object' && Object.keys(stateFiltersObjLocal['parent_level']).length > 0) {
      stateFilterObj = [stateFiltersObjLocal['parent_level']];
    } else {
      stateFilterObj = stateFiltersObjLocal['primary_level'];
    }

    // Check if user has access to all project in stateFiltersObjLocal['primary_level']
    const hasAllProjectAccess = stateFilterObj.every(filter =>
      currentUserProjectAccess?.some(project => project.projectId === filter.basicProjectConfigId)
    );

    // Superadmin have all project access hence no need to check project for superadmin
    const getAuthorities = this.sharedService.getCurrentUserDetails('authorities');
    const hasAccessToAll = Array.isArray(getAuthorities) && getAuthorities?.includes('ROLE_SUPERADMIN') || hasAllProjectAccess;

    localStorage.removeItem('shared_link');
    if (hasAccessToAll) {
      this.router.navigate([url]);
    } else {
      this.router.navigate(['/dashboard/Error']);
      setTimeout(() => {
        this.sharedService.raiseError({
          status: 901,
          message: 'No project access.',
        });
      }, 100);
    }
  }
}
