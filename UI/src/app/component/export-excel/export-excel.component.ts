import { Component, OnInit, ViewChild } from '@angular/core';
import { ExcelService } from 'src/app/services/excel.service';
import { HelperService } from 'src/app/services/helper.service';
import { Table } from 'primeng/table';
import { SharedService } from 'src/app/services/shared.service';
import { HttpService } from 'src/app/services/http.service';
import { MessageService } from 'primeng/api';

@Component({
  selector: 'app-export-excel',
  templateUrl: './export-excel.component.html',
  styleUrls: ['./export-excel.component.css'],
})
export class ExportExcelComponent implements OnInit {
  @ViewChild('table') tableComponent: Table;
  displayModal = false;
  modalDetails = {
    header: '',
    tableHeadings: [],
    tableValues: [],
  };
  kpiExcelData;
  sprintRatingObj = {
    '1': '../assets/img/smiley-1.svg',

    '2': '../assets/img/smiley-2.svg',

    '3': '../assets/img/smiley-3.svg',

    '4': '../assets/img/smiley-4.svg',

    '5': '../assets/img/smiley-5.svg',
  };
  tableColumnData = {};
  tableColumnForm = {};
  filteredColumn;
  excludeColumnFilter = [];
  includeColumnFilter = [];
  selectedColumns = [] // store all columns which is default or shown in table 
  tableColumns = []; // store all table coumns with configurations
  isDisableSaveCOnfigurationBtn: boolean = false;
  markerInfo = [];
  forzenColumns = ['issue id'];
  exportExcelRawVariable;

  constructor(
    private excelService: ExcelService,
    private helperService: HelperService,
    private sharedService: SharedService,
    private httpService: HttpService,
    private messageService: MessageService
  ) { }

  ngOnInit(): void { 
    this.sharedService.kpiExcelSubject.subscribe(x=>{
      this.exportExcelRawVariable = x;
    })
  }

  // download excel functionality
  downloadExcel(kpiId, kpiName, isKanban, additionalFilterSupport, filterApplyData, filterData, iSAdditionalFilterSelected, chartType?,testKpi?) {
    const sprintIncluded = filterApplyData.sprintIncluded.length > 0 ? filterApplyData.sprintIncluded : ['CLOSED'];
    this.modalDetails['kpiId'] = kpiId;
    if (!(!additionalFilterSupport && iSAdditionalFilterSelected)) {
      this.helperService.downloadExcel(kpiId, kpiName, isKanban, filterApplyData, filterData, sprintIncluded,).subscribe((getData) => {
        getData = {...getData,...this.exportExcelRawVariable}
        this.isDisableSaveCOnfigurationBtn = !getData['saveDisplay'];
        if (getData?.['kpiColumnList']?.length && getData?.['excelData']?.length) {
          this.dataTransformatin(getData['kpiColumnList'], getData['excelData'], chartType, kpiName);
        } else {
          this.modalDetails['header'] = kpiName;
          this.displayModal = true;
        }

      });
    } else {
      this.modalDetails['header'] = kpiName;
      this.displayModal = true;
    }
  }


  dataTransformForIterationTableWidget(markerInfo, excludeColumns, rawColumConfig, rawExcelData, kpiName, kpiId) {
    rawColumConfig = this.makeIssueIDOnFirstOrder(rawColumConfig);
    this.markerInfo = markerInfo;
    this.modalDetails['kpiId'] = kpiId;
    const tableData = [];
    rawExcelData.forEach(colData => {
      let obj = {};
      for (let key in colData) {
        if (this.typeOf(colData[key])) {
          obj[key] = [];
          for (let y in colData[key]) {
            //added check if valid url
            if (colData[key][y].includes('http')) {
              obj[key].push({ text: y, hyperlink: colData[key][y] });
            } else {
              obj[key].push(colData[key][y]);
            }
          }
        } else if (key == 'Issue Id') {
          obj['Issue Id'] = {};
          obj['Issue Id'][colData[key]] = colData['Issue URL'];
        } else {
          obj[key] = colData[key]
        }
      }
      tableData.push(obj);
    });

    this.dataTransformatin(rawColumConfig, tableData, '', kpiName);
  }
  
  dataTransformatin(rawColumConfig, rawExcelData, chartType, kpiName) {
    rawColumConfig = this.makeIssueIDOnFirstOrder(rawColumConfig);
    this.tableColumns = rawColumConfig;

    if (chartType == 'stacked-area') {
      const re = {}
      re['excelData'] = rawExcelData;
      re['columns'] = rawColumConfig;
      const allColumns = this.dataTransformForStackedAreaChart(re);
      this.generateAddRemoveData(allColumns);
    } else {
      this.generateAddRemoveData(Object.keys(rawExcelData[0]))
      const re = {}
      re['excelData'] = rawExcelData;
      re['columns'] = rawColumConfig.map(con => con.columnName);
      this.kpiExcelData = this.excelService.generateExcelModalData(re);
    }
    this.formatDate();
    this.selectedColumns = rawColumConfig.filter(colDetails => colDetails.isDefault || colDetails.isShown).map(config => config.columnName);
    this.generateColumnFilterData();
    this.modalDetails['header'] = kpiName;
    this.displayModal = true;

  }

  dataTransformForStackedAreaChart(getData) {
    let kpiObj = JSON.parse(JSON.stringify(getData));
    kpiObj['columns'] = kpiObj['columns'].map(col=>col.columnName);
    kpiObj['excelData'] = kpiObj['excelData'].map((item) => {
      for (let key in item['Count']) {
        if (!kpiObj['columns'].includes(key)) {
          kpiObj['columns'] = [...kpiObj['columns'], key];
        }
      }
      let obj = { ...item, ...item['Count'] };
      delete obj['Count'];
      return obj;
    });
    const allColumnList = [...kpiObj['columns']];
    this.kpiExcelData = this.excelService.generateExcelModalData(kpiObj);
    return allColumnList;
  }

  generateAddRemoveData(tableValue) {
    const unSelectedColumn = [];
    const defaultAndSelectedColumns = this.tableColumns.map(col => col.columnName);
    this.modalDetails['tableHeadings'] = [...new Set([...tableValue, ...defaultAndSelectedColumns])];
    this.modalDetails['tableHeadings'].forEach(col => {
      if (!defaultAndSelectedColumns.includes(col)) {
        unSelectedColumn.push({
          columnName: col,
          isDefault: false,
          isShown: false,
          order: 0
        });
      }
    });

    this.tableColumns.push(...unSelectedColumn);
    this.modalDetails['tableHeadings'] = this.tableColumns.filter(config => config.isShown === true || config.isDefault).map(config => config.columnName);

  }

  formatDate() {
    this.modalDetails['tableValues'] = this.kpiExcelData.excelData.map(item => {
      const formattedItem = { ...item };
      for (const key in formattedItem) {
        if (key.toLowerCase().includes('date') && formattedItem[key]) {
          formattedItem[key] = this.helperService.transformDateToISO(formattedItem[key]);
        }
      }
      return formattedItem
    });
  }

  exportExcel(kpiName) {
    this.excelService.generateExcel(this.kpiExcelData, kpiName);
  }

  clearModalDataOnClose() {
    this.excludeColumnFilter = [];
    this.includeColumnFilter = [];
    this.tableColumnData = {}
    this.tableColumnForm = {}
    this.displayModal = false;
    this.modalDetails = {
      header: '',
      tableHeadings: [],
      tableValues: [],
    };
    this.selectedColumns = []
    this.tableColumns = [];
    this.isDisableSaveCOnfigurationBtn = false;
  }

  checkIfArray(arr) {
    return Array.isArray(arr);
  }

  onFilterClick(columnName) {
    this.filteredColumn = columnName;
  }

  onFilterBlur(columnName) {
    this.filteredColumn = this.filteredColumn === columnName ? '' : this.filteredColumn;
  }

  generateColumnFilterData() {
    // Define blank values to handle
    const blankValues = ['', null, undefined, '-', 'NA','N/A','Undefined'];
    this.excludeColumnFilter = ['Linked Defect','Linked Stories'].map(item => item.toLowerCase());
    this.includeColumnFilter = ['Issue Id','Story ID','Defect ID','Link Story ID','Build URL','Epic ID','Created Defect ID','Merge Request URL','Ticket issue ID'].map(item => item.toLowerCase());
    if (this.modalDetails['tableValues'].length > 0) {
      // Update tableValues to replace blank values with '(Blanks)'
      this.modalDetails['tableValues'] = this.modalDetails['tableValues'].map(row => {
        const updatedRow = { ...row }; // Create a copy of the row

        if ((updatedRow['Issue Id'] || updatedRow['Defect ID']) && updatedRow['Issue URL']) {
          updatedRow['Issue Id'] = { text: updatedRow['Issue Id'] || updatedRow['Defect ID'], hyperlink: updatedRow['Issue URL'] };
        }

        Object.keys(updatedRow).forEach(colName => {
          if (typeof updatedRow[colName] === 'string') {
            updatedRow[colName] = updatedRow[colName].trim();
          }
          if (blankValues.includes(updatedRow[colName])) {
            updatedRow[colName] = '';
          }
        });
        return updatedRow;
      });
  
      // Generate column filter data
      this.modalDetails['tableHeadings'].forEach(colName => {
        this.tableColumnData[colName] = [...new Set(this.modalDetails['tableValues'].map(item => item[colName]))].map(colData => {
          if (this.typeOf(colData)) {
            if (!this.excludeColumnFilter.includes(colName.toLowerCase()) &&  !this.includeColumnFilter.includes(colName.toLowerCase())) {
              this.excludeColumnFilter.push(colName)
            }
            return { name:blankValues.includes(colData.text)?'(Blanks)':colData.text, value: colData };
          } else {
            return { name: blankValues.includes(colData)?'(Blanks)':colData, value: colData };
          }
        });
        this.tableColumnForm[colName] = [];
      });
    }
  }

  generateExcel(exportMode) {
    const tableData = {};

    if (exportMode === 'all') {
      this.excelService.generateExcel(this.kpiExcelData, this.modalDetails['header']);
    } else {
      let filteredData = this.tableComponent?.filteredValue ? this.tableComponent?.filteredValue : this.modalDetails['tableValues'];
      let filteredColumns = this.tableComponent.columns;

      const headerNames = [];
      for (const column of filteredColumns) {
        headerNames.push({ header: column, key: column, width: 25 });
      }

      tableData['headerNames'] = headerNames;
      tableData['excelData'] = filteredData;

      this.excelService.generateExcel(tableData, this.modalDetails['header']);
    }
  }

  typeOf(value) {
    return typeof value === 'object' && value !== null;
  }

  //custom sort for sorting Range. 
  // customSort(event: any) {
  //   let result = null;
  //   event.data.sort((data1, data2) => {
  //       let value1 = data1[event.field];
  //     let value2 = data2[event.field];
  //     const utcDate1: any = !isNaN(new Date(data1[event.field]).getTime()) && new Date(data1[event.field]).toISOString().slice(0, 10);
  //     const utcDate2: any = !isNaN(new Date(data2[event.field]).getTime()) && new Date(data2[event.field]).toISOString().slice(0, 10);
  //     if (event.field.toLowerCase().includes('date')) {
  //       result = (utcDate1 < utcDate2) ? -1 : (utcDate1 > utcDate2) ? 1 : 0;
  //     } else if(event.field === 'Weeks'){
  //       const date1 = new Date(value1.split('to')[0]);
  //           const date2 = new Date(value2.split('to')[0]);
  //            result = date1.getTime() - date2.getTime();
  //     } else {
  //       result = data1[event.field].localeCompare(data2[event.field])
  //     }
  //     return event.order * result;
  //   });
  // }

  applyColumnFilter() {
    this.saveKpiColumnsConfig(this.selectedColumns, 'APPLY');
  }

  saveTableColumnOrder() {
    if (this.tableComponent.columns.length > 0) {
      this.saveKpiColumnsConfig(this.tableComponent.columns, 'SAVE');
    }
  }

  saveKpiColumnsConfig(selectedColumns: any[], action: string) {
    const postData = {
      kpiId: '',
      basicProjectConfigId: '',
      kpiColumnDetails: []
    };
    postData.kpiId = this.modalDetails['kpiId'];
    postData['basicProjectConfigId'] = this.sharedService.selectedTrends[0].basicProjectConfigId;
    postData['kpiColumnDetails'] = this.tableColumns.filter(col => {
      const selectedColIndex = selectedColumns.findIndex(colName => colName === col.columnName);
      if (selectedColIndex !== -1) {
        col.isShown = true;
        col.order = selectedColIndex;
        return true;
      } else {
        col.isShown = false;
        return false;
      }
    });
    postData['kpiColumnDetails'].sort((a, b) => a.order - b.order);
    this.modalDetails['tableHeadings'] = postData['kpiColumnDetails'].map(col => col.columnName);
    if (action === 'SAVE') {
      this.httpService.postkpiColumnsConfig(postData).subscribe(response => {
        if (response && response['success'] && response['data']) {
          this.messageService.add({ severity: 'success', summary: 'Kpi Column Configurations saved successfully!' });
        } else {
          this.messageService.add({ severity: 'error', summary: 'Error in Kpi Column Configurations. Please try after sometime!' });
        }
      });

    } else {
      this.generateColumnFilterData();
      this.messageService.add({ severity: 'success', summary: 'Kpi Column Configurations applied successfully!' });
    }

  }

   makeIssueIDOnFirstOrder(columns) {
    // Identify the "issue id" column (case-insensitive)
    const issueIdColumn = columns.find(
      (col) => col.columnName.toLowerCase() === "issue id"
    );
  
    if (!issueIdColumn) {
      return columns; // Return original if "issue id" is not found
    }
  
    // Set "issue id" to the first position and adjust its order
    issueIdColumn.order = 0;
  
    // Filter out the "issue id" column and reassign orders for the rest
    const remainingColumns = columns
      .filter((col) => col !== issueIdColumn)
      .sort((a, b) => a.order - b.order)
      .map((col, index) => ({ ...col, order: index + 1 }));
  
    // Return the updated array with "issue id" at the top
    return [issueIdColumn, ...remainingColumns];
   }

}
