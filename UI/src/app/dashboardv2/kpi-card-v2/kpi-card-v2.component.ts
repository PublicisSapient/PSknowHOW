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
import { KpiHelperService } from 'src/app/services/kpi-helper.service';
import { MessageService } from 'primeng/api';
import { FeatureFlagsService } from 'src/app/services/feature-toggle.service';


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
  @Input() filterApplyData: any;
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
  //spal
  kpiHeaderData: {};
  kpiFilterData: {};
  copyCardData: any;
  currentChartData;
  KpiCategory;
  colorPalette = ['#FBCF5F', '#6079C5', '#A4F6A5'];
  selectedButtonValue;
  cardData;
  reportModuleEnabled: boolean = false;
  reportObj: any = {};
  displayAddToReportsModal: boolean = false;
  createNewReportTemplate: boolean = false;
  reportName: string = '';
  existingReportData: any[] = [];
  iterationKPIFilterValues: any[] = [];
  @Input() chartColorList: any[];
  @Input() yAxis: string = '';
  @Input() kpiThresholdObj: any;
  @Input() releaseEndDate: string = '';
  @Input() hieararchy: any;


  // reports: chartWithFiltersComponent
  selectedMainCategory: any;
  selectedMainFilter: any;
  selectedFilter2: any;
  success: boolean = false;

  constructor(public service: SharedService, private http: HttpService, private authService: GetAuthorizationService,
    private ga: GoogleAnalyticsService, private renderer: Renderer2, public dialogService: DialogService, private kpiHelperService: KpiHelperService,
    private helperService: HelperService, private messageService: MessageService, private featureFlagService: FeatureFlagsService) { }

  ngOnInit(): void {
    this.subscriptions.push(this.service.selectedFilterOptionObs.subscribe((x) => {
      this.filterOptions = {};
      if (x && Object.keys(x)?.length) {
        this.kpiSelectedFilterObj = JSON.parse(JSON.stringify(x));
        for (const key in x[this.kpiData?.kpiId]) {
          if (Array.isArray(x[this.kpiData?.kpiId][key]) && x[this.kpiData?.kpiId][key]?.includes('Overall')) {
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
        if (this.kpiData?.kpiDetail?.hasOwnProperty('kpiFilter') && (this.kpiData?.kpiDetail?.kpiFilter?.toLowerCase() == 'radiobutton' || this.kpiData?.kpiDetail?.kpiFilter?.toLowerCase() == 'multitypefilters')) {
          if (this.kpiSelectedFilterObj[this.kpiData?.kpiId]) {
            this.radioOption = this.kpiSelectedFilterObj[this.kpiData?.kpiId]?.hasOwnProperty('filter1') ? (this.kpiData?.kpiDetail?.kpiFilter?.toLowerCase() == 'multitypefilters' ? this.kpiSelectedFilterObj[this.kpiData?.kpiId]['filter2'][0] : this.kpiSelectedFilterObj[this.kpiData?.kpiId]['filter1'][0]) : this.kpiSelectedFilterObj[this.kpiData?.kpiId][0];
          }
        }
      }
      this.selectedTab = this.service.getSelectedTab() ? this.service.getSelectedTab().toLowerCase() : '';
    }));
    /** assign 1st value to radio button by default */

    this.subscriptions.push(this.service.onChartChangeObs.subscribe((stringifiedData) => {
      if (stringifiedData) {
        stringifiedData = JSON.parse(stringifiedData);
        this.selectedMainCategory = stringifiedData['selectedMainCategory'];
        this.selectedMainFilter = stringifiedData['selectedMainFilter'];
        this.selectedFilter2 = stringifiedData['selectedFilter2'];
      }
    }))
  }

  async initializeMenu() {
    this.menuItems = [
      {
        label: 'Settings',
        icon: 'fas fa-cog',
        command: () => {
          this.onOpenFieldMappingDialog();
        },
        disabled: this.disableSettings
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
        disabled: !this.kpiData?.kpiDetail?.chartType
      },
      {
        label: 'Comments',
        icon: 'pi pi-comments',
        command: ($event) => {
          this.showComments = true;
          this.openCommentModal();
        },
      },
    ];
  }

  /**
     * Handles various actions based on the event type.
     * Prepares data, opens dialogs, exports data, or shows comments as needed.
     *
     * @param {any} event - The event object containing action indicators.
     * @returns {void}
     */
  handleAction(event: any) {
    if (event.listView) {
      this.prepareData();
    } else if (event.setting) {
      this.onOpenFieldMappingDialog();
    } else if (event.explore) {
      if (event.kpiId) {
        this.exportToExcel(event.kpiId);
      } else {
        this.exportToExcel();
      }
    } else if (event.comment) {
      this.showComments = true;
      this.openCommentModal();
    } else if (event.report) {
      this.addToReportAction();
    }
  }

  async ngOnChanges(changes: SimpleChanges) {
    this.userRole = this.authService.getRole();
    this.checkIfViewer = (this.authService.checkIfViewer({ id: this.service.getSelectedTrends()[0]?.basicProjectConfigId }));
    this.disableSettings = (this.colors && (Object.keys(this.colors)?.length > 1 || (this.colors[Object.keys(this.colors)[0]]?.labelName !== 'project' && this.selectedTab !== 'iteration' && this.selectedTab !== 'release')))
      || this.checkIfViewer || !['superAdmin', 'projectAdmin'].includes(this.userRole);
    this.initializeMenu();

    /** assign 1st value to radio button by default */
    if (changes['dropdownArr'] && changes['dropdownArr'].currentValue?.length && this.kpiData?.kpiDetail?.hasOwnProperty('kpiFilter') && this.kpiData?.kpiDetail?.kpiFilter?.toLowerCase() == 'radiobutton' && this.dropdownArr?.length && this.dropdownArr[0]?.options.length) {
      let backUpValue = this.service.getKpiSubFilterObj()[this.kpiData.kpiId];
      if (!backUpValue || !Object.keys(backUpValue)?.length) {
        this.radioOption = this.dropdownArr[0]?.options[0];
      } else {
        if (backUpValue.hasOwnProperty('filter1')) {
          this.radioOption = backUpValue.filter1[0];
        } else {
          this.radioOption = backUpValue[0];
        }
      }
    }

    // if (changes['trendValueList'] && changes['trendValueList'].currentValue) {
    this.reportModuleEnabled = await this.featureFlagService.isFeatureEnabled('REPORTS');

    if (this.reportModuleEnabled && this.selectedTab !== 'iteration') {
      if (!this.loader && (!this.checkIfDataPresent(this.kpiDataStatusCode)) || (!this.kpiData?.kpiDetail?.isAdditionalFilterSupport && this.iSAdditionalFilterSelected)) {
        this.menuItems = this.menuItems.filter(item => item.label !== 'Add to Report');
        this.menuItems.push({
          label: 'Add to Report',
          icon: 'pi pi-briefcase',
          command: ($event) => {
            this.addToReportAction();
          },
          disabled: true
        });
      } else if (!this.loader) {
        this.menuItems = this.menuItems.filter(item => item.label !== 'Add to Report');
        this.menuItems.push({
          label: 'Add to Report',
          icon: 'pi pi-briefcase',
          command: ($event) => {
            this.addToReportAction();
          },
          disabled: false
        });
      }
    }
    // }



    //#region new card kpi
    if (this.selectedTab === 'iteration' && !this.loader) {
      this.cardData = this.trendValueList;
      const {
        issueData,
        kpiName,
        kpiInfo,
        kpiId,
        dataGroup,
        filterGroup,
        categoryData
      } = this.cardData;
      let responseCode = this.kpiDataStatusCode;
      this.kpiHeaderData = { issueData, kpiName, kpiInfo, kpiId, responseCode };
      this.kpiFilterData = { dataGroup, filterGroup, issueData, chartType: this.kpiData?.kpiDetail?.chartType, categoryData };
      this.copyCardData = JSON.parse(JSON.stringify(this.cardData));
      this.currentChartData = this.prepareChartData(
        this.cardData,
        this.colorPalette,
        this.cardData.kpiId === "kpi128" ? this.cardData?.categoryData?.categoryGroup[0]?.categoryName : ''
      );
    }

    //#endregion
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
      if (this.kpiDataStatusCode === '201') {
        this.warning = 'Configure the missing mandatory field mappings in KPI Settings for accurate data display.';
      } else if (this.kpiDataStatusCode === '203') {
        this.warning = 'Data may be inaccurate due to a failed processor run!';
      }
    } else {
      this.warning = null;
    }
  }

  /**
   * Handles changes in dropdown selections, moving selected options to the top,
   * emitting the selected option, and triggering a Google Analytics event.
   *
   * @param {string} type - The type of selection (e.g., 'radio', 'single').
   * @param {object|null} value - The selected value(s), can be an object or null.
   * @param {number} filterIndex - The index of the dropdown in the array.
   * @returns {void}
   */
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
    if (this.kpiData?.kpiDetail?.kpiFilter?.toLowerCase() == 'multitypefilters') {
      if (value && type?.toLowerCase() === 'radio') {
        this.filterOptions['filter' + (filterIndex + 1)] = [value];
      }
    }
    if (value && type?.toLowerCase() == 'radio' && this.kpiData?.kpiDetail?.kpiFilter?.toLowerCase() !== 'multitypefilters') {
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
    } else if (this.kpiData?.kpiDetail?.kpiFilter?.toLowerCase() !== 'multitypefilters') {
      this.filterOptions[event] = [];
      this.optionSelected.emit(this.filterOptions);
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
    if (selectedTrend.length == 1 || selectedTab === 'release') {
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

  exportToExcel(KpiId?: any) {
    if (!!this.cardData) {
      let exportData = this.cardData['issueData'];
      // const uniqueCategory = [[...new Set(exportData.map(item => item.Category))]];
      // console.log(uniqueCategory)
      if (KpiId === 'kpi176') {
        exportData = exportData.filter(x => x['Issue Type'].includes('Dependency') || x['Issue Type'].includes('Risk'));
      }
      this.service.kpiExcelSubject.next({ markerInfo: this.cardData?.dataGroup?.markerInfo, columns: this.cardData['modalHeads'], excelData: exportData })
    }

    this.downloadExcel.emit(true);
  }

  triggerGaEvent(gaObj) {
    this.ga.setKpiData(gaObj);
  }

  /**
   * Checks if data is present based on the provided status code and KPI ID.
   * Evaluates the trend value list and specific conditions to determine presence.
   *
   * @param {string} data - The status code to check (e.g., '200', '201').
   * @returns {boolean} - Returns true if data is present, otherwise false.
   */
  checkIfDataPresent(data) {
    if ((data === '200' || data === '201' || data === '203') && (this.kpiData?.kpiId === 'kpi148' || this.kpiData?.kpiId === 'kpi146')) {
      if (this.trendValueList?.length) {
        return true;
      }
    }
    else if ((data === '200' || data === '201' || data === '203') && (this.kpiData?.kpiId === 'kpi139' || this.kpiData?.kpiId === 'kpi127')) {
      if (this.trendValueList?.length && this.trendValueList[0].value?.length) {
        return true;
      }
    }
    else if ((data === '200' || data === '201' || data === '203') && (this.kpiData?.kpiId === 'kpi168' || this.kpiData?.kpiId === 'kpi70' || this.kpiData?.kpiId === 'kpi153' || this.kpiData?.kpiId === 'kpi35')) {
      if (this.trendValueList?.length && this.trendValueList[0]?.value?.length > 0) {
        return true;
      }
    }

    else if ((data === '200' || data === '201' || data === '203') && (this.kpiData?.kpiId === 'kpi171')) {
      if (this.trendValueList && this.trendValueList[0] && this.trendValueList[0]?.data?.length > 0) {
        return true;
      } else {
        return false;
      }
    } else {
      return (data === '200' || data === '201' || data === '203') && this.helperService.checkDataAtGranularLevel(this.trendValueList, this.kpiData.kpiDetail.chartType, this.selectedTab);
    }
  }

  getColorCssClasses(index: number): string | undefined {
    if (!Array.isArray(this.colorCssClassArray)) {
      console.warn('colorCssClassArray is not initialized or is not an array.');
      return undefined;
    }
    if (index < 0 || index >= this.colorCssClassArray.length) {
      console.warn(`Index ${index} is out of bounds for colorCssClassArray.`);
      return undefined;
    }
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


  //#region new card

  /**
       * Handles changes in filter selection, updates the issue data based on the selected filters,
       * and prepares the chart data accordingly. It distinguishes between cases where the selected
       * key object has a specific category value.
       *
       * @param event - The event object containing filter selection details.
       * @returns void
       * @throws None
       */
  onFilterChange(event) {
    const { selectedKeyObj, selectedKey, ...updatedEvent } = event;


    // extract the filter values for report
    this.iterationKPIFilterValues = [];
    this.kpiFilterData['filterGroup']?.filterGroup1.forEach(element => {
      let obj = element;
      obj['value'] = updatedEvent[element.filterKey];
      this.iterationKPIFilterValues.push(obj);
    });

    console.log(this.iterationKPIFilterValues);

    // Dynamically determine the exclusion value
    const exclusionValue = selectedKeyObj?.Category;

    const filters = [
      ...Object.entries({ ...updatedEvent, ...selectedKeyObj }).map(([key, value]) => ({ [key]: value }))
    ];

    // Apply dynamic filters
    const filterIssues = this.applyDynamicfilter(this.cardData.issueData, filters.filter(item => !(item.Category && item.Category === exclusionValue)));

    // Update filtered data
    this.copyCardData = { ...this.copyCardData, issueData: filterIssues };

    // Prepare chart data using the appropriate key
    const chartKey = selectedKeyObj?.Category !== exclusionValue ? selectedKey : exclusionValue;
    this.currentChartData = this.prepareChartData(this.copyCardData, this.colorPalette, chartKey);

    // Update selected button value
    this.selectedButtonValue = selectedKeyObj;
  }

  /**
       * Resets the filter by restoring the original issue data and preparing the chart data.
       *
       * @param {void} No parameters are accepted.
       * @returns {void} This function does not return a value.
       * @throws {Error} Throws an error if chart data preparation fails.
       */
  onFilterClear() {
    const filterIssues = this.cardData.issueData;
    this.copyCardData = { ...this.copyCardData, issueData: filterIssues };
    this.currentChartData = this.prepareChartData(
      this.copyCardData,
      this.colorPalette,
    );
  }

  applyDynamicfilter(data: any[], filterArr: any) {
    let filteredData = data;
    // cleanup empty or null or undefined props
    filterArr = this.sanitizeArray(filterArr);

    if (filterArr.length) {
      filterArr.forEach(element => {
        let filterObj = Object.keys(element).map(x => {
          return {
            key: x,
            value: element[x]
          }
        });
        if (Array.isArray(filterObj[0].value)) {
          filteredData = filteredData.filter(issue => filterObj[0]?.value.includes(issue[filterObj[0].key]));
        } else {
          filteredData = filteredData.filter(issue => issue[filterObj[0].key]?.includes(filterObj[0].value));
        }
      });
    }
    return filteredData;
  }

  // cleanup empty or null or undefined props
  /**
       * Recursively sanitizes an array or object by removing null, undefined,
       * and empty objects, returning a cleaned version of the input.
       *
       * @param input - The array or object to sanitize.
       * @returns A sanitized array or object, or null if the input is empty.
       * @throws No exceptions are thrown.
       */
  sanitizeArray(input) {
    // Recursive function to handle nested structures
    function sanitize(item) {
      if (Array.isArray(item)) {
        return item
          .map(sanitize) // Recursively sanitize array elements
          .filter((el) => el !== null && el !== undefined && Object.keys(el).length > 0); // Exclude null, undefined, or empty objects
      } else if (typeof item === "object" && item !== null) {
        const sanitizedObject = {};
        for (const [key, value] of Object.entries(item)) {
          if (value) sanitizedObject[key] = value; // Add key-value pairs with truthy values
        }
        return Object.keys(sanitizedObject).length > 0 ? sanitizedObject : null; // Remove empty objects
      }
      return null; // Exclude non-object and non-array elements
    }

    return sanitize(input);
  }


  prepareChartData(inputData: any, color: any, key?: any) {
    return this.kpiHelperService.getChartDataSet(inputData, this.kpiData.kpiDetail.chartType, color, key);
  }

  /**
     * Calculates the total sum of numeric values associated with a specified key in an array of issue data.
     * @param issueData - An array of objects representing issues, each containing various key-value pairs.
     * @param key - The key whose numeric values will be summed.
     * @returns The total sum as a string.
     * @throws No exceptions are explicitly thrown, but non-numeric values are ignored in the sum.
     */
  calculateValue(issueData, key: string): string {
    const total = issueData.reduce((sum, issue) => {
      const value = issue[key];
      return sum + (typeof value === 'number' ? value : 0); // Only add numeric values
    }, 0);

    return total.toString(); // Convert to string for display
  }

  /**
     * Converts a given value to hours if the specified unit represents time.
     * @param val - The value to be converted.
     * @param unit - The unit of the value, which determines if conversion is necessary.
     * @returns The converted value in days/hours (unit).
     */
  convertToHoursIfTime(val, unit) {
    return this.kpiHelperService.convertToHoursIfTime(val, unit)
  }

  /**
     * Calculates and returns the cumulative value based on the chart type and selected button value.
     * It converts the total count to hours if the chart type is 'stacked-bar' or 'stacked-bar-chart'.
     * Returns the total count or a calculated value based on the selected button value otherwise.
     *
     * @returns {number} The cumulative value or total count.
     * @throws {Error} Throws an error if the data structure is not as expected.
     */
  showCummalative() {
    if (this.kpiData?.kpiDetail?.chartType !== 'chartWithFilter') {
      if (this.kpiData?.kpiDetail?.chartType === 'stacked-bar') {
        return this.kpiHelperService.convertToHoursIfTime(this.currentChartData.totalCount, 'day')
      } else if (this.kpiData?.kpiDetail?.chartType === 'stacked-bar-chart') {
        if (!!this.selectedButtonValue?.length && !!this.selectedButtonValue[0]?.key) {
          return this.copyCardData.issueData.reduce((sum, issue) => sum + (issue.tempCount || 0), 0)
        } else {
          return this.currentChartData.totalCount
        }
      } else {
        if (!!this.selectedButtonValue && Array.isArray(this.selectedButtonValue) && !!this.selectedButtonValue[0].key) {
          const totalValue = this.calculateValue(this.copyCardData.issueData, this.selectedButtonValue[0].key)
          return this.kpiHelperService.convertToHoursIfTime(totalValue, this.selectedButtonValue[0].unit)
        }
        return this.currentChartData.totalCount
      }
    } else {
      return null;
    }
  }

  //#endregion

  /**
   * Checks for the presence of a filter group in the provided filter data.
   * @param filterData - An object containing filter information, which may include a filterGroup property.
   * @returns The filterGroup property if it exists; otherwise, undefined.
   * @throws No exceptions are thrown.
   */
  checkFilterPresence(filterData) {
    return filterData?.filterGroup;
  }

  //#region reports
  /**
       * Prepares and adds KPI report metadata to the report object, 
       * including various configurations based on the current state 
       * and selected options, then displays the report modal.
       * 
       * @param {void} 
       * @returns {void} 
       */
  addToReportAction() {
    this.success = false;
    const today = new Date();
    const formattedDate = today.toLocaleDateString('en-US', {
      weekday: 'long', year: 'numeric', month: 'long', day: 'numeric'
    });

    this.getExistingReports();
    let metaDataObj = {
      kpiName: this.kpiData.kpiName,
      kpiId: this.kpiData.kpiId,
      kpiSource: this.kpiData.kpiDetail.kpiSource,
      kpiUnit: this.kpiData.kpiDetail.kpiUnit,
      kpiCategory: this.kpiData.kpiDetail.kpiCategory,
      kpiFilter: this.kpiData.kpiDetail.kpiFilter,
      chartType: this.kpiData.kpiDetail.chartType,
      filterOptions: this.filterOptions,
      radioOption: this.radioOption,
      trend: this.trendData,
      trendColors: this.trendBoxColorObj,
      selectedKPIFilters: this.kpiData?.kpiDetail?.kpiFilter?.toLowerCase() === 'radiobutton' ? this.radioOption : this.filterOptions,
      selectedTab: this.selectedTab,
      selectedType: this.service.getSelectedType()?.toLowerCase(),
      filterApplyData: this.filterApplyData,
      kpiSelectedFilterObj: this.kpiSelectedFilterObj[this.kpiData?.kpiId],
      yAxis: this.yAxis,
      xAxis: this.kpiData.kpiDetail.xaxisLabel,
      chartColorList: this.chartColorList || {},
      kpiThresholdObj: this.kpiThresholdObj || {},
      capturedAt: formattedDate,
      kpiHeight: 400,
      hieararchy: this.hieararchy
    }

    if (metaDataObj.chartType === 'bar-with-y-axis-group') {
      metaDataObj['yaxisOrder'] = this.kpiData.kpiDetail.yaxisOrder;
    }

    if (metaDataObj.chartType === 'chartWithFilter') {
      metaDataObj['selectedMainCategory'] = this.selectedMainCategory;
      metaDataObj['selectedMainFilter'] = this.selectedMainFilter;
      metaDataObj['selectedFilter2'] = this.selectedFilter2 || null;
    }

    if (this.selectedTab === 'iteration') {
      metaDataObj['selectedButtonValue'] = this.getSelectButtonValue();
      metaDataObj['cardData'] = this.cardData;
      if (metaDataObj['cardData']) {
        metaDataObj['cardData']['summary'] = this.showCummalative();
      }
      metaDataObj['iterationKPIFilterValues'] = this.iterationKPIFilterValues;
    }

    if (metaDataObj.chartType === 'horizontalPercentBarChart') {
      metaDataObj['kpiHeight'] = 500;
    }

    if (this.selectedTab === 'iteration' && metaDataObj.chartType === 'CumulativeMultilineChart') {
      metaDataObj.chartType = 'CumulativeMultilineChartv2';
    }


    metaDataObj['releaseEndDate'] = this.releaseEndDate;

    this.reportObj = {
      id: this.kpiData.kpiId,
      chartData: this.currentChartData?.chartData ? this.currentChartData?.chartData : this.kpiChartData,
      metadata: metaDataObj,
    };

    this.displayAddToReportsModal = true;
  }

  getSelectButtonValue() {
    let result = '';
    let options = this.getSelectButtonOptions();

    if (options?.length) {
      this.selectedButtonValue = this.selectedButtonValue || { Category: (options[0].hasOwnProperty('key') ? options[0]?.key : options[0]?.categoryName) };
      result = options.filter(x => {
        if (x.hasOwnProperty('key')) {
          return x.key === this.selectedButtonValue.Category
        } else if (x.hasOwnProperty('categoryName')) {
          return x.categoryName === this.selectedButtonValue.Category
        }
      })[0][this.getOptionLabel()];
    }
    return result;
  }

  getSelectButtonOptions(): any[] {
    if (this.kpiData.kpiDetail.chartType === 'stacked-bar-chart') {
      return this.kpiFilterData['dataGroup']?.dataGroup1 || [];
    } else if (this.kpiData.kpiDetail.chartType === 'grouped-bar-chart') {
      return this.kpiFilterData['categoryData']?.categoryGroup || [];
    }
    return [];
  }

  getOptionLabel(): string {
    return this.kpiData.kpiDetail.chartType === 'stacked-bar-chart' ? 'name' : 'categoryName';
  }

  getExistingReports() {
    this.http.fetchReports().subscribe({
      next: (response) => {
        if (response['success']) {
          if (response['data']['content'] && response['data']['content'].length) {
            this.generateReportSlider(response['data']['content']);
          }
        }
      },
      error: (error) => {
        this.existingReportData = [];
        this.generateReportSlider([]);
      }
    });
  }

  generateReportSlider(response, newReport = false) {
    let storedReportData = response;
    if (storedReportData?.length) {
      this.existingReportData = storedReportData;
      if (!newReport) {
        this.reportName = this.existingReportData[0].name;
      }
    } else {
      this.existingReportData = [];
    }
    if (!this.existingReportData?.length) {
      this.createNewReportTemplate = true;
      this.service.setNoReports(true);
    } else {
      this.createNewReportTemplate = false;
      this.service.setNoReports(false);
    }
  }

  toggleCreateNewReportTemplate(event) {
    this.reportName = '';
    this.createNewReportTemplate = !this.createNewReportTemplate;
  }

  addToReportPost() {
    let data = { ...this.reportObj };
    data.chartData = JSON.stringify(data.chartData);
    let submitData = {
      name: this.reportName,
      kpis: [data]
    };

    this.http.createReport(submitData).subscribe(data => {
      if (data['success']) {
        this.messageService.add({ severity: 'success', summary: 'Report created successfully' });
        this.existingReportData.push(data['data']);
        this.createNewReportTemplate = false;
        this.reportName = data['data'].name;
        this.generateReportSlider(this.existingReportData, true);
        this.success = true;
      } else {
        this.messageService.add({ severity: 'error', summary: 'Error while creating report' });
        this.success = false;
      }
    });
  }


  addToReportPut() {
    let reportId = this.existingReportData.find(x => x.name === this.reportName).id;
    let existingKPIs = this.existingReportData.find(x => x.name === this.reportName).kpis;

    let data = { ...this.reportObj };
    data.chartData = JSON.stringify(data.chartData);

    if (!existingKPIs.find(x => x.id === data.id)) {
      existingKPIs.push(data)
    } else {
      existingKPIs = this.replaceObjectById(existingKPIs, data);
    }


    let submitData = {
      name: this.reportName,
      kpis: [...existingKPIs]
    };


    this.http.updateReport(reportId, submitData).subscribe(data => {
      if (data['success']) {
        this.messageService.add({ severity: 'success', summary: 'Report updated successfully' });
        this.existingReportData = this.replaceObjectByName(this.existingReportData, data['data']);
        this.success = true;
      } else {
        this.messageService.add({ severity: 'error', summary: 'Error while updating report' });
        this.success = false;
      }
    });
  }

  replaceObjectByName(arr: any[], replacement: any): any[] {
    return arr.map(obj => (obj.name === replacement.name ? replacement : obj));
  }

  replaceObjectById(arr: any[], replacement: any): any[] {
    return arr.map(obj => (obj.id === replacement.id ? replacement : obj));
  }

  closeAddToReportsModal() {
    this.createNewReportTemplate = false;
    this.displayAddToReportsModal = false;
  }

  handleKeyboardSelect(event: KeyboardEvent) {
    // Implement keyboard navigation logic
    // For example, allow arrow key navigation between options
    const currentIndex = this.dropdownArr[0].options.findIndex(
      option => option.value === this.radioOption
    );

    switch (event.key) {
      case 'ArrowRight':
        // Select next option
        if (currentIndex < this.dropdownArr[0].options.length - 1) {
          this.radioOption = this.dropdownArr[0].options[currentIndex + 1].value;
          this.handleChange('radio', { value: this.radioOption });
        }
        break;
      case 'ArrowLeft':
        // Select previous option
        if (currentIndex > 0) {
          this.radioOption = this.dropdownArr[0].options[currentIndex - 1].value;
          this.handleChange('radio', { value: this.radioOption });
        }
        break;
    }
  }

  resetDialogFocus() {
    // Optional: Reset focus to a specific element or return focus to the triggering element
    const triggeringElement = document.getElementById('sprint-details-trigger');
    triggeringElement?.focus();
  }

  onTabChange(event: any) {
    // Optional: Improve tab change accessibility
    const newTabElement = document.getElementById(`project-tab-${event.index}`);
    newTabElement?.focus();
  }
}
