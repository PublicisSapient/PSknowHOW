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

import { Injectable } from '@angular/core';
import * as Excel from 'exceljs';
import * as fs from 'file-saver';
import { DatePipe } from '../../../node_modules/@angular/common';

@Injectable({
    providedIn: 'root'
})

export class ExcelService {
    constructor(private datePipe: DatePipe) {
    }

    generateExcelModalData(kpiData) {
        const headerNames = [];
        const excelData = [];

        if (kpiData['excelData'] && kpiData['columns']) {
            for (const column of kpiData['columns']) {
                headerNames.push({ header: column, key: column, width: 25 });
            }
            for (const data of kpiData.excelData) {
                const rowData = {};
                for (const key in data) {
                    if (!(typeof (data[key]) == 'object')) {
                        rowData[key] = data[key];
                    } else {
                        const appendedRowData = [];
                        if (Array.isArray(data[key])) {
                            for (const cellData of data[key]) {
                                appendedRowData.push(cellData);
                            }
                        } else {
                            for (const datakey in data[key]) {
                                if (data[key][datakey]) {
                                    appendedRowData.push({ text: datakey, hyperlink: data[key][datakey] });
                                } else {
                                    appendedRowData.push(datakey);
                                }
                            }
                        }
                        if (appendedRowData.length === 0) {
                            rowData[key] = '';
                        } else if (appendedRowData.length === 1) {
                            rowData[key] = appendedRowData[0];
                        } else {
                            rowData['rowSpan'] = appendedRowData.length;
                            rowData[key] = appendedRowData;
                        }

                    }
                }
                excelData.push(rowData);
            }
            return { headerNames, excelData };
        } else {
            return { headerNames: [], excelData: [] };
        }
    }

    generateExcel(kpiData, kpiName) {
        const workbook = new Excel.Workbook();
        const worksheet = workbook.addWorksheet('kpi Data');
        let filename = '';

        if (kpiName) {
            filename = kpiName;
        } else {
            filename = kpiData.kpiName;
        }

        if (kpiData === undefined) {
            const noData = worksheet.addRow(['NO Data Available']);
            noData.font = {
                name: 'Comic Sans MS',
                family: 4,
                size: 16,
                underline: 'double',
                bold: true
            };
            worksheet.mergeCells(1, 1, 1, 5); // top,left,bottom,right
        } else {
            worksheet.columns = kpiData.headerNames;
            for (const excelData of  kpiData.excelData) {
                if (!excelData.hasOwnProperty('rowSpan')) {
                    const rowDetails = worksheet.addRow(excelData);
                } else {
                    const lastRow = worksheet.lastRow.number;
                    const rowSpan = excelData['rowSpan'];
                    delete excelData['rowSpan'];
                    for (let j = 0; j < kpiData.headerNames.length; j++) {
                        if (!Array.isArray(excelData[worksheet.getColumn(j + 1).key])) {
                            worksheet.mergeCells(lastRow + 1, j + 1, lastRow + rowSpan, j + 1);
                            worksheet.getCell(worksheet.getColumn(j + 1).letter + (lastRow + 1)).value = excelData[worksheet.getColumn(j + 1).key];
                        } else {
                            for (let k = 0; k < excelData[worksheet.getColumn(j + 1).key].length; k++) {
                                worksheet.getCell(worksheet.getColumn(j + 1).letter + (lastRow + 1 + k)).value = excelData[worksheet.getColumn(j + 1).key][k];
                            }
                        }

                    }
                }
            }

            worksheet.eachRow((row, rowNumber)=> {
                if (rowNumber === 1) {
                    row.eachCell({
                        includeEmpty: true
                    }, (cell)=> {

                        cell.font = {
                            name: 'Arial Rounded MT Bold'
                        };

                        cell.fill = {
                            type: 'pattern',
                            pattern: 'solid',
                            fgColor: {
                                argb: 'FFFFFF00'
                            },
                            bgColor: {
                                argb: 'FF0000FF'
                            }
                        };

                    });
                }
                row.eachCell({
                    includeEmpty: true
                }, (cell) =>{

                    const linkStyle = {
                        underline: true,
                        color: { argb: 'FF0000FF' },
                    };
                    if (cell.hyperlink) {
                        cell.font = linkStyle;
                    }

                    cell.border = {
                        top: {
                            style: 'thin'
                        },
                        left: {
                            style: 'thin'
                        },
                        bottom: {
                            style: 'thin'
                        },
                        right: {
                            style: 'thin'
                        }
                    };
                });
            });
        }

        worksheet.addRow([]);
        // Iterate over all cells in a all row (including empty cells)
        worksheet.eachRow((row)=> {
            row.eachCell({
                includeEmpty: true
            }, (cell)=> {
                cell.alignment = {
                    vertical: 'middle',
                    horizontal: 'left',
                    wrapText: true
                };
            });
        });

        // Footer Row
        let footerRow;
        if (kpiName === 'Engineering Maturity') {
            footerRow = worksheet.addRow(['* Data extracted from Engineering Maturity']);
        } else {
            footerRow = worksheet.addRow(['* Data extracted from KPI tool']);
        }
        footerRow.getCell(1).fill = {
            type: 'pattern',
            pattern: 'solid',
            fgColor: {
                argb: 'FFCCFFE5'
            }
        };
        footerRow.getCell(1).border = {
            top: {
                style: 'thin'
            },
            left: {
                style: 'thin'
            },
            bottom: {
                style: 'thin'
            },
            right: {
                style: 'thin'
            }
        };


        // Merge Cells
        worksheet.mergeCells(`A${footerRow.number}:F${footerRow.number}`);

        // Generate Excel File with given name
        workbook.xlsx.writeBuffer().then((data) => {
            const blob = new Blob([data as BlobPart], {
                type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
            });
            fs.saveAs(blob, filename + '.xlsx');
        });
    }

    exportExcel(kpiData, kpiType, kpiName, isKanban){
        let worksheet;
        const workbook = new Excel.Workbook();
        if (kpiName === 'Engineering Maturity') {
            worksheet = workbook.addWorksheet(kpiName);
        } else {
            worksheet = workbook.addWorksheet('kpi Data');
        }

        let filename = '';


            // for individual download
            if (kpiName) {
                filename = kpiName;
            } else {
                filename = kpiData.kpiName;
            }

            // handling response of it kpiwise
            if (kpiName === 'Total Defect Count') {
                kpiData.kpiName = kpiName;
                kpiData = kpiData.validationData.DefectCount['Total Defect Count'];
            } else if (kpiName === 'Total Defect Aging') {
                kpiData.kpiName = kpiName;
                kpiData = kpiData.validationData.DefectAging['Total Defect Count'];
            } else if (kpiName === 'Total Ticket Count') {
                kpiData.kpiName = kpiName;
                kpiData = kpiData.validationData.TicketCount['Total Defect Count'];
            } else if (kpiName === 'Total Ticket Aging') {
                kpiData.kpiName = kpiName;
                kpiData = kpiData.validationData.TicketAging['Total Defect Count'];
            } else {
                kpiData = kpiData.validationData;
            }
            let mergeCellNo; // change by counting no of keys in object
            if (kpiData === undefined) {
                const noData = worksheet.addRow(['NO Data Available']);
                noData.font = {
                    name: 'Comic Sans MS',
                    family: 4,
                    size: 16,
                    underline: 'double',
                    bold: true
                };
                worksheet.mergeCells(1, 1, 1, 5); // top,left,bottom,right
            } else if (kpiName === 'Engineering Maturity') {
                if (Object.keys(kpiData.validationData).length === 0) {
                    const noData = worksheet.addRow(['NO Data Available']);
                    noData.font = {
                        name: 'Comic Sans MS',
                        family: 4,
                        size: 16,
                        underline: 'double',
                        bold: true
                    };
                    worksheet.mergeCells(1, 1, 1, 5); // top,left,bottom,right
                } else {
                    const headerNames = [];
                    const newFormatArray = [];
                    // let mergeCell = 0;
                    let maxCell = 0;


                    // finding max no of cell
                    for (const firstData in kpiData) {
                        mergeCellNo = Object.keys(kpiData[firstData]).length;
                        if (maxCell < mergeCellNo) {
                            maxCell = mergeCellNo;
                        }
                    }

                    // adding blank array
                    for (const firstData in kpiData) {
                        mergeCellNo = Object.keys(kpiData[firstData]).length;
                        if (mergeCellNo === maxCell) {
                            for (const names in kpiData[firstData]) {
                                headerNames.push(names);
                            }
                            break;
                        }
                    }


                    for (let mergeCellIndex = 0; mergeCellIndex < maxCell; mergeCellIndex++) {
                        newFormatArray.push([]);
                    }
                    for (const data in kpiData) {
                        const dataName = kpiData[data];
                        let max = 0;
                        for (const dataChildName in dataName) {
                            const tempMax = dataName[dataChildName].length;
                            if (tempMax > max) {
                                max = tempMax;
                            }
                        }

                        for (const dataChildName in dataName) {
                            for (let i = 0; i < max; i++) {
                                if (dataName[dataChildName][i] === undefined && dataName[dataChildName][i] !== null) {
                                    dataName[dataChildName].push('');
                                }
                            }
                        }

                        // mergeCell += (max + 1);
                        // for (let index = 0; index < max; index++) {
                        //     newFormatArray[0].push(data);
                        // }
                        // let j = 1;
                        let j = 0;
                        for (const dataChildName in dataName) {
                            if (kpiName === 'Engineering Maturity') {
                                for (let index = 0; index < dataName[dataChildName].length; index++) {
                                    let newValue = '';
                                    if (dataName[dataChildName][index] != null && dataName[dataChildName][index] && typeof dataName[dataChildName][index] === 'object') {
                                        for (const newobjKey in dataName[dataChildName][index]) {
                                            const value = dataName[dataChildName][index][newobjKey];
                                            newValue = newValue + newobjKey + ': ' + value + ',' + '\r\n';
                                        }
                                    } else {
                                        newValue = dataName[dataChildName][index];
                                    }

                                    newFormatArray[j].push(newValue);
                                }
                            } else {
                                if (newFormatArray[j]) {
                                    newFormatArray[j].push(...dataName[dataChildName]);
                                }
                            }

                            j++;
                        }
                    }

                    for (let i = 0; i < newFormatArray.length; i++) {
                        newFormatArray[i].unshift(headerNames[i]);
                        worksheet.getColumn(i + 1)['values'] = newFormatArray[i];
                        worksheet.getColumn(i + 1).width = 25;
                    }

                    worksheet.eachRow(function(row, rowNumber) {
                        if (rowNumber === 1) {
                            row.eachCell({
                                includeEmpty: true
                            }, function(cell) {

                                cell.font = {
                                    name: 'Arial Rounded MT Bold'
                                };

                                cell.fill = {
                                    type: 'pattern',
                                    pattern: 'solid',
                                    fgColor: {
                                        argb: 'FFFFFF00'
                                    },
                                    bgColor: {
                                        argb: 'FF0000FF'
                                    }
                                };

                            });
                        }
                        row.eachCell({
                            includeEmpty: true
                        }, function(cell) {

                            cell.border = {
                                top: {
                                    style: 'thin'
                                },
                                left: {
                                    style: 'thin'
                                },
                                bottom: {
                                    style: 'thin'
                                },
                                right: {
                                    style: 'thin'
                                }
                            };
                        });
                    });

                }

            } else {
                const headerNames = [];
                const newFormatArray = [];
                let mergeCell = 0;
                if (kpiName === 'Total Defect Aging' || kpiName === 'Total Defect Count' || kpiName === 'Total Ticket Aging' || kpiName === 'Total Ticket Count') {
                    if (kpiName === 'Total Defect Aging' || kpiName === 'Total Ticket Aging') {
                        headerNames.push('Age');
                    } else {
                        if (isKanban) {
                            headerNames.push('Date');
                        } else {
                            headerNames.push('Sprint');

                        }
                    }

                    if (isKanban && (kpiName === 'Total Ticket Aging' || kpiName === 'Total Ticket Count')) {
                        headerNames.push('Tickets');
                    } else {
                        headerNames.push('Defects');
                    }

                    newFormatArray.push([]);
                    newFormatArray.push([]);
                    for (const data in kpiData) {
                        let maxLength = 0;
                        if (kpiName === 'In-Sprint Automation Coverage') {

                            newFormatArray[1].push(...kpiData[data]['Total Test']);

                            if (kpiData[data]['Total Test']) {
                                maxLength = kpiData[data]['Total Test'].length;
                            }

                        } else {
                            newFormatArray[1].push(...kpiData[data]);
                            maxLength = kpiData[data].length;
                        }

                        for (let index = 0; index < maxLength; index++) {
                            newFormatArray[0].push(data + '');
                        }
                    }
                } else {

                    if (kpiName === 'Release Frequency' || kpiName === 'Test Case Without Story Link'
                      || kpiName === 'Code Commits' || kpiName === 'Defect Count Without Story Link'
                      || kpiName === 'Code Build Time' || kpiName === 'Cost of Delay' || kpiName === 'Sonar Tech Debt'
                      || kpiName === 'Sonar Violations' || kpiName === 'Unit Test Coverage' || kpiName === 'Value delivered (Cost of Delay)'
                      || kpiName === 'Change Failure Rate' || kpiName === 'Number of Check-Ins & Merge Requests'
                      || kpiName === 'Mean Time To Merge' || kpiName === 'Deployment Frequency' || kpiName === 'Total Ticket Count by Priority' || kpiName === 'Total Ticket Count By RCA') {
                        headerNames.push('Project');
                    } else {
                        if (isKanban) {
                            headerNames.push('Date');
                        } else {
                            headerNames.push('Sprint');
                        }
                    }

                    let maxCell = 0;

                    // finding max no of cell
                    for (const firstData in kpiData) {
                        mergeCellNo = Object.keys(kpiData[firstData]).length;
                        if (maxCell < mergeCellNo) {
                            maxCell = mergeCellNo;
                        }
                    }

                    // adding blank array
                    for (const firstData in kpiData) {
                        mergeCellNo = Object.keys(kpiData[firstData]).length;
                        if (mergeCellNo === maxCell) {
                            for (const names in kpiData[firstData]) {
                                headerNames.push(names);
                            }
                            break;
                        }
                    }


                    for (let mergeCellIndex = 0; mergeCellIndex <= maxCell; mergeCellIndex++) {
                        newFormatArray.push([]);
                    }
                    for (const data in kpiData) {
                        const dataName = kpiData[data];
                        let max = 0;
                        for (const dataChildName in dataName) {
                            const tempMax = dataName[dataChildName].length;
                            if (tempMax > max) {
                                max = tempMax;
                            }
                        }

                        for (const dataChildName in dataName) {
                            for (let i = 0; i < max; i++) {
                                if (dataName[dataChildName][i] === undefined && dataName[dataChildName][i] !== null) {
                                    dataName[dataChildName].push('');
                                }
                            }
                        }

                        mergeCell += (max + 1);
                        for (let index = 0; index < max; index++) {
                            newFormatArray[0].push(data);
                        }
                        let j = 1;
                        for (const dataChildName in dataName) {
                            if (kpiName === 'Cycle Time' || kpiName === 'Code Commits') {
                                for (let index = 0; index < dataName[dataChildName].length; index++) {
                                    let newValue = '';
                                    if (dataName[dataChildName][index] != null && dataName[dataChildName][index] && typeof dataName[dataChildName][index] === 'object') {
                                        for (const newobjKey in dataName[dataChildName][index]) {
                                            const value = dataName[dataChildName][index][newobjKey];
                                            newValue = newValue + newobjKey + ': ' + value + ',' + '\r\n';
                                        }
                                    } else {
                                        newValue = dataName[dataChildName][index];
                                    }

                                    newFormatArray[j].push(newValue);
                                }
                            } else {
                                if (newFormatArray[j]) {
                                    newFormatArray[j].push(...dataName[dataChildName]);
                                }
                            }

                            j++;
                        }
                    }

                }
                for (let i = 0; i < newFormatArray.length; i++) {
                    newFormatArray[i].unshift(headerNames[i]);
                    worksheet.getColumn(i + 1)['values'] = newFormatArray[i];
                    worksheet.getColumn(i + 1).width = 25;
                }

                worksheet.eachRow(function(row, rowNumber) {
                    if (rowNumber === 1) {
                        row.eachCell({
                            includeEmpty: true
                        }, function(cell) {

                            cell.font = {
                                name: 'Arial Rounded MT Bold'
                            };

                            cell.fill = {
                                type: 'pattern',
                                pattern: 'solid',
                                fgColor: {
                                    argb: 'FFFFFF00'
                                },
                                bgColor: {
                                    argb: 'FF0000FF'
                                }
                            };

                        });
                    }
                    row.eachCell({
                        includeEmpty: true
                    }, function(cell) {

                        cell.border = {
                            top: {
                                style: 'thin'
                            },
                            left: {
                                style: 'thin'
                            },
                            bottom: {
                                style: 'thin'
                            },
                            right: {
                                style: 'thin'
                            }
                        };
                    });
                });
            }


        worksheet.addRow([]);
        // Iterate over all cells in a all row (including empty cells)
        worksheet.eachRow(function(row) {
            row.eachCell({
                includeEmpty: true
            }, function(cell) {
                cell.alignment = {
                    vertical: 'middle',
                    horizontal: 'left',
                    wrapText: true
                };
            });
        });

        // Footer Row
        let footerRow;
        if (kpiName === 'Engineering Maturity') {
            footerRow = worksheet.addRow(['* Data extracted from Engineering Maturity']);
        } else {
            footerRow = worksheet.addRow(['* Data extracted from KPI tool']);
        }
        footerRow.getCell(1).fill = {
            type: 'pattern',
            pattern: 'solid',
            fgColor: {
                argb: 'FFCCFFE5'
            }
        };
        footerRow.getCell(1).border = {
            top: {
                style: 'thin'
            },
            left: {
                style: 'thin'
            },
            bottom: {
                style: 'thin'
            },
            right: {
                style: 'thin'
            }
        };


        // Merge Cells
        worksheet.mergeCells(`A${footerRow.number}:F${footerRow.number}`);

        // Generate Excel File with given name
        workbook.xlsx.writeBuffer().then((data) => {
            const blob = new Blob([data as BlobPart], {
                type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
            });
            fs.saveAs(blob, filename + '.xlsx');
        });
    }
}
