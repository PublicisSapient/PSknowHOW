import { Component, OnInit, ViewChild } from '@angular/core';
import { ExcelService } from 'src/app/services/excel.service';
import { HelperService } from 'src/app/services/helper.service';
import { Table } from 'primeng/table';
import { SharedService } from 'src/app/services/shared.service';
import { HttpService } from 'src/app/services/http.service';
import { SortEvent } from 'primeng/api';

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

  constructor(
    private excelService: ExcelService,
    private helperService: HelperService,
    private sharedService: SharedService,
    private httpService: HttpService,
  ) { }

  ngOnInit(): void { }

  // download excel functionality
  downloadExcel(
    kpiId,
    kpiName,
    isKanban,
    additionalFilterSupport,
    filterApplyData,
    filterData,
    iSAdditionalFilterSelected,
    chartType?,
  ) {
    const sprintIncluded =
      filterApplyData.sprintIncluded.length > 0
        ? filterApplyData.sprintIncluded
        : ['CLOSED'];
    if (!(!additionalFilterSupport && iSAdditionalFilterSelected)) {
      this.helperService
        .downloadExcel(
          kpiId,
          kpiName,
          isKanban,
          filterApplyData,
          filterData,
          sprintIncluded,
        )
        .subscribe((getData) => {
          if (
            getData['excelData'] 
            || !getData?.hasOwnProperty('validationData')
          ) {
            if (chartType == 'stacked-area') {
              let kpiObj = JSON.parse(JSON.stringify(getData));
              kpiObj['excelData'] = kpiObj['excelData'].map((item) => {
                for (let key in item['Count']) {
                  if (!kpiObj['columns']?.includes(key)) {
                    kpiObj['columns'] = [...kpiObj['columns'], key];
                  }
                }
                let obj = { ...item, ...item['Count'] };
                delete obj['Count'];
                return obj;
              });
              this.kpiExcelData =
                this.excelService.generateExcelModalData(kpiObj);
            } else {
              this.kpiExcelData =
                this.excelService.generateExcelModalData(getData);
            }

            this.modalDetails['tableHeadings'] =
              this.kpiExcelData.headerNames.map((column) => column.header);
            // this.modalDetails['tableValues'] = additionalFilterSupport ? this.kpiExcelData.excelData : [];
            this.modalDetails['tableValues'] = this.kpiExcelData.excelData;
            this.generateTableColumnData();
            this.modalDetails['header'] = kpiName;
            this.displayModal = true;
          } else {
            if (getData['kpiId'] === 'kpi83') {
              let dynamicKeys = [];
              for (const key in getData['validationData']) {
                if (dynamicKeys.length === 0) {
                  dynamicKeys = Object.keys(
                    getData['validationData'][key][kpiName][0],
                  );
                }
                for (const x in dynamicKeys) {
                  getData['validationData'][key][dynamicKeys[x]] = [];
                }

                const arr = getData['validationData'][key][kpiName];
                // eslint-disable-next-line @typescript-eslint/prefer-for-of
                for (let i = 0; i < arr.length; i++) {
                  for (const item in arr[i]) {
                    getData['validationData'][key][item].push(arr[i][item]);
                  }
                }
                delete getData['validationData'][key][kpiName];
              }
            }

            this.excelService.exportExcel(
              getData,
              'individual',
              kpiName,
              isKanban,
            );
          }
        });
    } else {
      this.modalDetails['header'] = kpiName;
      this.displayModal = true;
    }
  }

  exportExcel(kpiName) {
    this.excelService.generateExcel(this.kpiExcelData, kpiName);
  }

  clearModalDataOnClose() {
    this.excludeColumnFilter = [];
    this.tableColumnData = {}
    this.tableColumnForm = {}
    this.displayModal = false;
    this.modalDetails = {
      header: '',
      tableHeadings: [],
      tableValues: [],
    };
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

  generateTableColumnData() {
    if(this.modalDetails['tableValues'].length > 0) {
      this.modalDetails['tableHeadings'].forEach(colName => {
        this.tableColumnData[colName] = [...new Set(this.modalDetails['tableValues'].map(item => item[colName]))].map(colData => {
          if (this.typeOf(colData)) {
            if (!this.excludeColumnFilter.includes(colName)) {
              this.excludeColumnFilter.push(colName)
            }
            return { name: colData.text, value: colData.text }
          } else {
            return { name: this.getFormatedWeek(colData,colName), value: this.getFormatedWeek(colData,colName) }
          }
        });
        this.tableColumnForm[colName] = [];
      });
    }
  }

  generateExcel(exportMode) {
    const tableData = {
      columns: [],
      excelData: []
    };
    let excelData = [];
    let columns = [];
    if (exportMode === 'all') {
      this.excelService.generateExcel(this.kpiExcelData, this.modalDetails['header']);
    } else {
      excelData = this.tableComponent?.filteredValue ? this.tableComponent?.filteredValue : this.modalDetails['tableValues'];
      tableData.columns = this.modalDetails['tableHeadings']

      excelData.forEach(colData => {
        let obj = {};
        for (let key in colData) {
          if (this.typeOf(colData[key]) && colData[key].hasOwnProperty('hyperlink')) {
            obj[key] = { [colData[key]['text']]: colData[key]['hyperlink'] }
          } else {
            obj[key] = colData[key]
          }
        }
        tableData.excelData.push(obj);
      });

      let kpiData = this.excelService.generateExcelModalData(tableData);
      this.excelService.generateExcel(kpiData, this.modalDetails['header']);
    }
  }

  typeOf(value) {
    return typeof value === 'object' && value !== null;
  }

  customSort(event: SortEvent) {
    let result = null;
    event.data.sort((data1, data2) => {
      const utcDate1: any = !isNaN(new Date(data1[event.field]).getTime()) && new Date(data1[event.field]).toISOString().slice(0, 10);
      const utcDate2: any = !isNaN(new Date(data2[event.field]).getTime()) && new Date(data2[event.field]).toISOString().slice(0, 10);
      if (event.field === 'Created Date' || event.field === 'Closed Date') {
        result = (utcDate1 < utcDate2) ? -1 : (utcDate1 > utcDate2) ? 1 : 0;
      }
      else {
        result = data1[event.field].localeCompare(data2[event.field])
      }
      return event.order * result;
    });
  }

  getFormatedWeek(date,type){
   return this.helperService.getFormatedDateBasedOnType(date,type)
  }
}
