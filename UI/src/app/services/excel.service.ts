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

    generateExcelModalData(kpiData){
        const headerNames = [];
        const excelData = [];

        for(const key in kpiData.excelData[0]){
            headerNames.push({ header: key, key, width: 25 });
        }

        for (const data of kpiData.excelData) {
            const rowData = {};
            for(const key in data){
                if(!(typeof(data[key])== 'object')){
                    rowData[key] = data[key];
                }else{
                    const appendedRowData = [];
                    for (const datakey in data[key]) {
                        if (data[key][datakey]) {
                            appendedRowData.push({ text: datakey, hyperlink: data[key][datakey] });
                        } else {
                            appendedRowData.push(datakey);
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
        return {headerNames, excelData};
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

        if (kpiData === undefined || Object.keys(kpiData).length === 0) {
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
                    for (let j = 0; j < Object.keys(excelData).length; j++) {
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
}
