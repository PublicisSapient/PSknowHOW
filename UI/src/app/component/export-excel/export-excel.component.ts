import { Component, OnInit } from '@angular/core';
import { ExcelService } from 'src/app/services/excel.service';
import { HelperService } from 'src/app/services/helper.service';

@Component({
  selector: 'app-export-excel',
  templateUrl: './export-excel.component.html',
  styleUrls: ['./export-excel.component.css'],
})
export class ExportExcelComponent implements OnInit {
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

  constructor(
    private excelService: ExcelService,
    private helperService: HelperService,
  ) {}

  ngOnInit(): void {}

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
            getData['excelData'] ||
            !getData?.hasOwnProperty('validationData')
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
            this.modalDetails['tableValues'] = this.kpiExcelData.excelData;
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
}
