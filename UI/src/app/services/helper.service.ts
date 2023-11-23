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

@Injectable()
export class HelperService {
    isKanban = false;
    grossMaturityObj = {};
    public passMaturityToFilter;

    constructor(private httpService: HttpService, private excelService: ExcelService,private sharedService : SharedService) {
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

        return this.httpService.downloadExcel(downloadJson, kpiId);

    }

    // this is used for making request object for kpi .Here first parameter is kpi source i.e
    // sonar , jira etc and second parameter is Kanban is true or false
    // type is quality or productivity
    groupKpiFromMaster(kpiSource, isKanban, masterData, filterApplyData, filterData, kpiIdsForCurrentBoard, type, selectedTab) {
        const kpiRequestObject = <any>{};

        kpiRequestObject.kpiList = <any>[];
        for (let i = 0; i < masterData?.kpiList?.length; i++) {
            const obj = { ...masterData.kpiList[i] };
            obj['chartType'] = '';
            let condition = obj.kpiSource === kpiSource && obj.kanban === isKanban;

            if (type && type !== '' && !isNaN(type)) {
                condition = (obj.groupId && obj.groupId === type) && condition;
            }
            if (obj?.kpiCategory) {
                condition = obj.kpiCategory.toLowerCase() === selectedTab.toLowerCase() && condition;
            }

            if (kpiIdsForCurrentBoard && kpiIdsForCurrentBoard.length && obj?.kpiId) {
                condition = kpiIdsForCurrentBoard.includes(obj.kpiId) && condition;
            }

            if (condition) {
                if (obj.videoLink) {
                    delete obj.videoLink;
                }
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
        if (objArray && objArray?.length > 1) {
            objArray?.sort((a, b) => a.data.localeCompare(b.data));
        }
        return objArray;
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
              const boardB = projectLevelKpi[boards].find(b => b.boardId === boardA.boardId);
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

    getGlobalConfig(){
        this.httpService.getConfigDetails().subscribe(res=>{
          if(res && res['success']){
            this.sharedService.setGlobalConfigData(res['data']);
          }
        })
      }
}
