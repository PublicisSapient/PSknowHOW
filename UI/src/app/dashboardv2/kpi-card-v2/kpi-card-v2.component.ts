import { Component, EventEmitter, Input, OnChanges, OnInit, Output, Renderer2, SimpleChanges, ViewChild } from '@angular/core';
import { SharedService } from 'src/app/services/shared.service';
import { HelperService } from 'src/app/services/helper.service';
import { HttpService } from 'src/app/services/http.service';
import { GetAuthorizationService } from 'src/app/services/get-authorization.service';
import { GoogleAnalyticsService } from 'src/app/services/google-analytics.service';
import { MenuItem } from 'primeng/api';
import { DatePipe } from '@angular/common';
import { Menu } from 'primeng/menu';
import { DialogService, DynamicDialogRef } from 'primeng/dynamicdialog';
import { CommentsV2Component } from 'src/app/component/comments-v2/comments-v2.component';

@Component({
  selector: 'app-kpi-card-v2',
  templateUrl: './kpi-card-v2.component.html',
  styleUrls: ['./kpi-card-v2.component.css']
})
export class KpiCardV2Component implements OnInit, OnChanges {
  isTooltip = false;
  @Input() kpiData: any;
  @Input() kpiChartData: any;
  @Input() iSAdditionalFilterSelected: boolean;
  @Input() showChartView = 'chart';
  @Input() trendData: Array<object>;
  @Input() showTrendIndicator = true;
  @Input() board?: string;
  @Input() showExport: boolean;
  @Input() selectedTab: any;
  @Input() dropdownArr: any;
  @Input() trendBoxColorObj: any;
  @Input() loader: boolean = true;
  @Input() trendValueList: any;
  @Input() sprintsOverlayVisible: boolean;
  @Input() showCommentIcon: boolean;
  showComments: boolean = false;
  @Input() kpiSize;
  @Input() kpiDataStatusCode: string = '';
  // showComments: boolean = false;
  loading: boolean = false;
  noData: boolean = false;
  displayConfigModel: boolean = false;
  fieldMappingConfig = [];
  selectedToolConfig: any = [];
  selectedConfig: any = {};
  fieldMappingMetaData = [];
  selectedFieldMapping = [];
  userRole: string;
  checkIfViewer: boolean;
  subscriptions: any[] = [];
  filterOptions: object = {};
  radioOption: string;
  filterMultiSelectOptionsData: object = {};
  kpiSelectedFilterObj: any = {};
  selectedTabIndex: number = 0;
  projectList: Array<string>;
  @Output() optionSelected = new EventEmitter<any>();
  @Output() reloadKPITab = new EventEmitter<any>();
  menuItems: MenuItem[] | undefined;
  lastSyncTime: any;
  isSyncPassedOrFailed;
  @ViewChild('kpimenu') kpimenu: Menu;
  @Output() downloadExcel = new EventEmitter<boolean>();
  metaDataTemplateCode: any;
  @Input() nodeId: string = '';
  loadingKPIConfig: boolean = false
  noDataKPIConfig: boolean = false
  displaySprintDetailsModal: boolean = false;
  columnList = [
    { field: 'duration', header: 'Duration' },
    { field: 'value', header: 'KPI Value', unit: 'unit' },
    { field: 'params', header: 'Calculation Details' },
  ];
  sprintDetailsList: Array<any>;
  @Input() colors;
  colorCssClassArray = ['sprint-hover-project1', 'sprint-hover-project2', 'sprint-hover-project3', 'sprint-hover-project4', 'sprint-hover-project5', 'sprint-hover-project6'];
  commentDialogRef: DynamicDialogRef | undefined;
  disableSettings: boolean = false;
  @Input() immediateLoader: boolean = true;
  @Input() partialData: boolean = false;
  warning = '';
  @Input() xCaption: string;

  constructor(public service: SharedService, private http: HttpService, private authService: GetAuthorizationService,
    private ga: GoogleAnalyticsService, private renderer: Renderer2, public dialogService: DialogService,
    private helperService: HelperService) { }

  ngOnInit(): void {
    this.subscriptions.push(this.service.selectedFilterOptionObs.subscribe((x) => {
      this.filterOptions = {};
      if (Object.keys(x)?.length) {
        this.kpiSelectedFilterObj = JSON.parse(JSON.stringify(x));
        for (const key in x[this.kpiData?.kpiId]) {
          if (x[this.kpiData?.kpiId][key]?.includes('Overall')) {
            if (this.kpiData?.kpiId === "kpi72") {
              if (key === "filter1") {
                this.filterOptions["filter1"] = this.kpiSelectedFilterObj[this.kpiData?.kpiId]['filter1'][0];
              }
              else if (key === "filter2") {
                this.filterOptions["filter2"] = this.kpiSelectedFilterObj[this.kpiData?.kpiId]['filter2'][0];
              }
              else {
                this.filterOptions = { ...this.filterOptions };
              }
            }
            else {
              this.filterOptions = { ...this.filterOptions };
            }
          } else {
            if (this.kpiData?.kpiId === "kpi72") {
              if (key === "filter1") {
                this.filterOptions["filter1"] = this.kpiSelectedFilterObj[this.kpiData?.kpiId]['filter1'][0];
              }
              else if (key === "filter2") {
                this.filterOptions["filter2"] = this.kpiSelectedFilterObj[this.kpiData?.kpiId]['filter2'][0];
              }

            }
            else {
              this.filterOptions = Array.isArray(x[this.kpiData?.kpiId]) ? { 'filter1': x[this.kpiData?.kpiId] } : { ...x[this.kpiData?.kpiId] };
            }
          }
        }
        if (this.kpiData?.kpiDetail?.hasOwnProperty('kpiFilter') && this.kpiData?.kpiDetail?.kpiFilter?.toLowerCase() == 'radiobutton') {
          if (this.kpiSelectedFilterObj[this.kpiData?.kpiId]) {
            this.radioOption = this.kpiSelectedFilterObj[this.kpiData?.kpiId]?.hasOwnProperty('filter1') ? this.kpiSelectedFilterObj[this.kpiData?.kpiId]['filter1'][0] : this.kpiSelectedFilterObj[this.kpiData?.kpiId][0];
          }
        }
      }
      this.selectedTab = this.service.getSelectedTab() ? this.service.getSelectedTab().toLowerCase() : '';
    }));
    /** assign 1st value to radio button by default */
    if (this.kpiData?.kpiDetail?.hasOwnProperty('kpiFilter') && this.kpiData?.kpiDetail?.kpiFilter?.toLowerCase() == 'radiobutton' && this.dropdownArr?.length && this.dropdownArr[0]?.options.length) {
      this.radioOption = this.dropdownArr[0]?.options[0];
    }
  }

  initializeMenu() {
    this.menuItems = [
      {
        label: 'Settings',
        icon: 'fas fa-cog',
        command: () => {
          this.onOpenFieldMappingDialog();
        },
        disabled: this.disableSettings || this.service.getSelectedType()?.toLowerCase() === 'kanban'
      },
      {
        label: 'List View',
        icon: 'pi pi-align-justify',
        command: ($event) => {
          this.prepareData();
        },
        disabled: this.selectedTab === 'release' || this.selectedTab === 'backlog'
      },
      {
        label: 'Explore',
        icon: 'pi pi-table',
        command: () => {
          this.exportToExcel();
        },
        disabled: !this.kpiData.kpiDetail.chartType
      },
      {
        label: 'Comments',
        icon: 'pi pi-comments',
        command: ($event) => {
          this.showComments = true;
          this.openCommentModal();
        },
      }
    ];
  }

  ngOnChanges(changes: SimpleChanges) {
    this.userRole = this.authService.getRole();
    this.checkIfViewer = (this.authService.checkIfViewer({ id: this.service.getSelectedTrends()[0]?.basicProjectConfigId }));
    this.disableSettings = (this.colors && (Object.keys(this.colors)?.length > 1 || (this.colors[Object.keys(this.colors)[0]]?.labelName !== 'project' && this.selectedTab !== 'iteration' && this.selectedTab !== 'release')))
      || this.checkIfViewer || !['superAdmin', 'projectAdmin'].includes(this.userRole);
    this.initializeMenu();
  }

  openCommentModal = () => {
    this.commentDialogRef = this.dialogService.open(CommentsV2Component, {
      data: {
        kpiId: this.kpiData?.kpiId,
        kpiName: this.kpiData?.kpiName,
        selectedTab: this.selectedTab
      },
    });

    this.commentDialogRef.onClose.subscribe(() => {
      console.log('on close called')
    });
  }

  showTooltip(val) {
    this.isTooltip = val;
  }

  showWarning(val) {
    if (val) {
      this.warning = 'Configure the missing mandatory field mappings in KPI Settings for accurate data display.';
    } else {
      this.warning = null;
    }
  }

  handleChange(type, value = null, filterIndex = 0) {

    // moving selected option to top
    if (value && value.value && Array.isArray(value.value)) {
      value.value.forEach(selectedItem => {
        this.dropdownArr[filterIndex]?.options.splice(this.dropdownArr[filterIndex]?.options.indexOf(selectedItem), 1) // remove the item from list
        this.dropdownArr[filterIndex]?.options.unshift(selectedItem)// this will add selected item on the top
      });
    }
    if (typeof value === 'object') {
      value = value?.value;
    }
    if (value && type?.toLowerCase() == 'radio') {
      this.optionSelected.emit(value);
    } else if (type?.toLowerCase() == 'single') {
      this.optionSelected.emit(this.filterOptions);
    } else {
      if (this.filterOptions && Object.keys(this.filterOptions)?.length == 0) {
        this.optionSelected.emit(['Overall']);
      } else {
        this.optionSelected.emit(this.filterOptions);
      }
    }
    const gaObj = {
      "kpiName": this.kpiData?.kpiName,
      "filter1": this.filterOptions?.['filter1'] || [value],
      "filter2": this.filterOptions?.['filter2'] || null,
      'kpiSource': this.kpiData?.kpiDetail?.kpiSource
    }
    this.triggerGaEvent(gaObj);
  }

  handleClearAll(event) {
    if (this.dropdownArr.length === 1) {
      for (const key in this.filterOptions) {
        if (key?.toLowerCase() == event?.toLowerCase()) {
          delete this.filterOptions[key];
        }
      }
      this.optionSelected.emit(['Overall']);
    } else {
      // hacky way - clear All sets null value, which we want to avoid
      for (const key in this.filterOptions) {
        if (key?.toLowerCase() == event?.toLowerCase()) {
          this.filterOptions[key] = [];
        } else if (!this.filterOptions[key]) {
          this.filterOptions[key] = [];
        }
      }
      this.optionSelected.emit(this.filterOptions);
    }
  }

  toggleMenu(event) {
    this.kpimenu.toggle(event);
  }

  /** When field mapping dialog is opening */
  onOpenFieldMappingDialog() {
    this.getKPIFieldMappingConfig();
  }

  /** This method is responsible for getting field mapping configuration for specfic KPI */
  getKPIFieldMappingConfig() {
    const selectedTab = this.service.getSelectedTab()?.toLowerCase();
    const selectedType = this.service.getSelectedType()?.toLowerCase();
    const selectedTrend = this.service.getSelectedTrends();
    if (selectedType === 'scrum' && selectedTrend.length == 1 || selectedTab === 'release') {
      this.loadingKPIConfig = true;
      this.noDataKPIConfig = false;
      this.displayConfigModel = true;
      this.lastSyncTime = this.showExecutionDate(this.kpiData.kpiDetail.combinedKpiSource || this.kpiData.kpiDetail.kpiSource);
      this.http.getKPIFieldMappingConfig(`${selectedTrend[0]?.basicProjectConfigId}/${this.kpiData?.kpiId}`).subscribe(data => {
        if (data?.success) {
          this.fieldMappingConfig = data?.data['fieldConfiguration'];
          const kpiSource = data?.data['kpiSource']?.toLowerCase();
          const toolConfigID = data?.data['projectToolConfigId'];
          this.selectedToolConfig = [{ id: toolConfigID, toolName: kpiSource }];
          if (this.fieldMappingConfig.length > 0) {
            this.selectedConfig = { ...selectedTrend[0], id: selectedTrend[0]?.basicProjectConfigId }
            this.getFieldMapping();
            const metaDataList = this.service.getFieldMappingMetaData();
            if (metaDataList.length && this.kpiData.kpiId !== 'kpi150') {
              const metaData = metaDataList.find(data => data.projectID === selectedTrend[0]?.basicProjectConfigId && data.kpiSource === kpiSource);
              if (metaData?.metaData) {
                this.fieldMappingMetaData = metaData.metaData;
              } else {
                this.getFieldMappingMetaData(kpiSource);
              }
            } else {
              this.getFieldMappingMetaData(kpiSource);
            }
          } else {
            this.loadingKPIConfig = false;
            this.noDataKPIConfig = true;
          }
        }
      })
    }
  }

  getFieldMapping() {
    let obj = {
      "releaseNodeId": this.nodeId || null
    }
    this.http.getFieldMappingsWithHistory(this.selectedToolConfig[0].id, this.kpiData.kpiId, obj).subscribe(mappings => {
      if (mappings && mappings['success'] && Object.keys(mappings['data']).length >= 1) {
        this.selectedFieldMapping = mappings['data'].fieldMappingResponses;
        this.metaDataTemplateCode = mappings['data']?.metaTemplateCode
        this.displayConfigModel = true;
        this.loadingKPIConfig = false;

      } else {
        this.loadingKPIConfig = false;
      }
    }, error => {
      console.log(error);
    });
  }

  getFieldMappingMetaData(kpiSource) {
    this.http.getKPIConfigMetadata(this.service.getSelectedTrends()[0]?.basicProjectConfigId, this.kpiData?.kpiId).subscribe(Response => {
      if (Response.success) {
        this.fieldMappingMetaData = Response.data;
        this.service.setFieldMappingMetaData({
          projectID: this.service.getSelectedTrends()[0]?.basicProjectConfigId,
          kpiSource: kpiSource,
          metaData: Response.data
        })
      } else {
        this.fieldMappingMetaData = [];
      }
    }, error => {
      console.log(error);
    });
  }

  reloadKPI() {
    this.displayConfigModel = false;
    this.reloadKPITab.emit(this.kpiData);
  }

  showExecutionDate(processorName) {
    const traceLog = this.findTraceLogForTool(processorName.toLowerCase());
    this.isSyncPassedOrFailed = traceLog?.executionSuccess === true ? true : false;
    return (traceLog == undefined || traceLog == null || traceLog.executionEndedAt == 0) ? 'NA' : new DatePipe('en-US').transform(traceLog.executionEndedAt, 'dd-MMM-yyyy (EEE) - hh:mmaaa');
  }

  findTraceLogForTool(processorName) {
    const sourceArray = (processorName.includes('/')) ? processorName.split('/') : [processorName];
    return this.service.getProcessorLogDetails().find(ptl => sourceArray.includes(ptl['processorName'].toLowerCase()));
  }

  exportToExcel() {
    this.downloadExcel.emit(true);
  }

  triggerGaEvent(gaObj) {
    this.ga.setKpiData(gaObj);
  }

  checkIfDataPresent(data) {
    if ((data === '200' || data === '201') && (this.kpiData?.kpiId === 'kpi148' || this.kpiData?.kpiId === 'kpi146')) {
      if (this.trendValueList?.length) {
        return true;
      }
    }
    if ((data === '200' || data === '201') && (this.kpiData?.kpiId === 'kpi139' || this.kpiData?.kpiId === 'kpi127')) {
      if (this.trendValueList?.length && this.trendValueList[0].value?.length) {
        return true;
      }
    }
    if ((data === '200' || data === '201') && (this.kpiData?.kpiId === 'kpi168' || this.kpiData?.kpiId === 'kpi70' || this.kpiData?.kpiId === 'kpi153')) {
      if (this.trendValueList?.length && this.trendValueList[0]?.value?.length > 0) {
        return true;
      }
    }

    if ((data === '200' || data === '201') && (this.kpiData?.kpiId === 'kpi171')) {
      if (this.trendValueList?.length && this.trendValueList[0]?.data?.length > 0) {
        return true;
      } else {
        return false;
      }
    }

    return (data === '200' || data === '201') && this.helperService.checkDataAtGranularLevel(this.trendValueList, this.kpiData.kpiDetail.chartType, this.selectedTab);
  }

  getColorCssClasses(index) {
    return this.colorCssClassArray[index];
  }

  hasData(field: string): boolean {
    return this.sprintDetailsList[this.selectedTabIndex]['hoverList'].some(rowData => rowData[field] !== null && rowData[field] !== undefined);
  }

  prepareData() {
    this.projectList = [];
    this.sprintDetailsList = [];
    this.selectedTabIndex = 0;
    this.projectList = Object.values(this.colors).map(obj => obj['nodeName']);
    this.projectList.forEach((project, index) => {
      const selectedProjectTrend = this.trendValueList.find(obj => obj.data === project);
      const tempColorObjArray = Object.values(this.colors).find(obj => obj['nodeName'] === project)['color'];
      if (selectedProjectTrend?.value) {
        let hoverObjectListTemp = [];

        // if (selectedProjectTrend.value[0]?.dataValue?.length > 0) {
        //   this.columnList = [{ field: 'duration', header: 'Duration' }];
        //   selectedProjectTrend.value[0].dataValue.forEach(d => {
        //     this.columnList.push({ field: d.name + ' value', header: d.name + ' KPI Value', unit: 'unit' });
        //     this.columnList.push({ field: d.name + ' params', header: d.name + ' Calculation Details', unit: 'unit' });
        //   });

        //   selectedProjectTrend.value.forEach(element => {
        //     let tempObj = {};
        //     tempObj['duration'] = element['sSprintName'] || element['date'];

        //     element.dataValue.forEach((d, i) => {
        //       tempObj[d.name + ' value'] = (Math.round(d['value'] * 100) / 100);
        //       tempObj['unit'] = ' ' + this.kpiData.kpiDetail?.kpiUnit
        //       if (d['hoverValue'] && Object.keys(d['hoverValue'])?.length > 0) {
        //         tempObj[d.name + ' params'] = Object.entries(d['hoverValue']).map(([key, value]) => `${key} : ${value}`).join(', ');
        //       }
        //     });

        //     hoverObjectListTemp.push(tempObj);
        //   });
        // } else
        {
          selectedProjectTrend.value.forEach(element => {
            let tempObj = {};
            tempObj['duration'] = element['sSprintName'] || element['date'];
            tempObj['value'] = element['lineValue'] !== undefined ? element['lineValue'] : (Math.round(element['value'] * 100) / 100);
            tempObj['unit'] = ' ' + this.kpiData.kpiDetail?.kpiUnit
            if (element['hoverValue'] && Object.keys(element['hoverValue'])?.length > 0) {
              tempObj['params'] = Object.entries(element['hoverValue']).map(([key, value]) => `${key} : ${value}`).join(', ');
            }
            hoverObjectListTemp.push(tempObj);
          });
        }
        this.sprintDetailsList.push({
          ['project']: selectedProjectTrend['data'],
          ['hoverList']: hoverObjectListTemp,
          ['color']: tempColorObjArray
        });
      } else {
        this.sprintDetailsList.push({
          ['project']: project,
          ['hoverList']: [],
          ['color']: tempColorObjArray
        });
      }
    });
    this.displaySprintDetailsModal = true;
  }
}
