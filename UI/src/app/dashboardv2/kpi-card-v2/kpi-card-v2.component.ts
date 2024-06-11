import { Component, ElementRef, EventEmitter, Input, OnChanges, OnInit, Output, Renderer2, SimpleChanges, ViewChild } from '@angular/core';
import { SharedService } from 'src/app/services/shared.service';
import { HttpService } from 'src/app/services/http.service';
import { GetAuthorizationService } from 'src/app/services/get-authorization.service';
import { GoogleAnalyticsService } from 'src/app/services/google-analytics.service';
import { MenuItem } from 'primeng/api';
import { DatePipe } from '@angular/common';
import { Menu } from 'primeng/menu';

@Component({
  selector: 'app-kpi-card-v2',
  templateUrl: './kpi-card-v2.component.html',
  styleUrls: ['./kpi-card-v2.component.css']
})
export class KpiCardV2Component implements OnInit, OnChanges {
  isTooltip = false;
  @Input() kpiData: any;
  @Input() showChartView = 'chart';
  @Input() trendData: Array<object>;
  @Input() showTrendIndicator = true;
  @Input() board?: string;
  @Input() showExport: boolean;
  @Input() selectedTab: any;
  @Input() dropdownArr: any;
  @Input() trendBoxColorObj: any;
  @Input() loader: boolean;
  @Input() trendValueList: any;
  @Input() sprintsOverlayVisible: boolean;
  @Input() showCommentIcon: boolean;
  @Input() kpiSize;
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
  filterOption = 'Overall';
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
  metaDataTemplateCode : any;
  @Input() nodeId: string = '';
  loadingKPIConfig : boolean = false
  noDataKPIConfig : boolean = false

  constructor(public service: SharedService, private http: HttpService, private authService: GetAuthorizationService,
    private ga: GoogleAnalyticsService, private renderer: Renderer2) { }

  ngOnInit(): void {
    this.menuItems = [
      {
        label: 'Settings',
        icon: 'fas fa-cog',
        command: () => {
          this.onOpenFieldMappingDialog();
        },
      },
      {
        label: 'Explore',
        icon: 'pi pi-table',
        command: () => {
          this.exportToExcel();
        }
      },
    ];

    this.subscriptions.push(this.service.selectedFilterOptionObs.subscribe((x) => {
      if (Object.keys(x)?.length > 1) {
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
                this.filterOption = 'Overall';
              }
            }
            else {
              this.filterOptions = { ...this.filterOptions };
              this.filterOption = 'Overall';
            }
          } else {
            if (this.kpiData?.kpiId === "kpi72") {
              if (key === "filter1") {
                this.filterOptions["filter1"] = this.kpiSelectedFilterObj[this.kpiData?.kpiId]['filter1'][0];
              }
              else {
                this.filterOptions["filter2"] = this.kpiSelectedFilterObj[this.kpiData?.kpiId]['filter2'][0];
              }

            }
            else {
              this.filterOption = this.kpiSelectedFilterObj[this.kpiData?.kpiId][0];
              this.filterOptions = Array.isArray(x[this.kpiData?.kpiId]) ? { 'filter1': x[this.kpiData?.kpiId] } : { ...x[this.kpiData?.kpiId] };
              if (!this.filterOption) {
                this.filterOption = this.kpiSelectedFilterObj[this.kpiData?.kpiId]['filter1'] ? this.kpiSelectedFilterObj[this.kpiData?.kpiId]['filter1'][0] : this.kpiSelectedFilterObj[this.kpiData?.kpiId][0];
              }
            }
          }
        }
        if (this.kpiData?.kpiDetail?.hasOwnProperty('kpiFilter') && this.kpiData?.kpiDetail?.kpiFilter?.toLowerCase() == 'radiobutton') {
          if (this.kpiSelectedFilterObj[this.kpiData?.kpiId]) {
            // this.radioOption = this.kpiSelectedFilterObj[this.kpiData?.kpiId][0];
            this.radioOption = this.kpiSelectedFilterObj[this.kpiData?.kpiId]?.hasOwnProperty('filter1') ? this.kpiSelectedFilterObj[this.kpiData?.kpiId]['filter1'][0] : this.kpiSelectedFilterObj[this.kpiData?.kpiId][0];
          }
        }
      }
      this.selectedTab = this.service.getSelectedTab() ? this.service.getSelectedTab().toLowerCase() : '';
    }));
    /** assign 1st value to radio button by default */
    if (this.kpiData?.kpiDetail?.hasOwnProperty('kpiFilter') && this.kpiData?.kpiDetail?.kpiFilter?.toLowerCase() == 'radiobutton' && this.dropdownArr?.length && this.dropdownArr[0]?.options.length) {
      this.radioOption = this.dropdownArr[0]?.options[0];
      console.log(this.dropdownArr[0]?.options);
    }
  }

  ngOnChanges(changes: SimpleChanges) {
    this.userRole = this.authService.getRole();
    this.checkIfViewer = (this.authService.checkIfViewer({ id: this.service.getSelectedTrends()[0]?.basicProjectConfigId }));
  }

  showTooltip(val) {
    this.isTooltip = val;
  }

  handleChange(type, value) {
    console.log(value);
    if (typeof value === 'object') {
      value = value.value;
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
      // this.showFilterTooltip(true);
    }
    const gaObj = {
      "kpiName": this.kpiData?.kpiName,
      "filter1": this.filterOptions?.['filter1'] || [value],
      "filter2": this.filterOptions?.['filter2'] || null,
      'kpiSource': this.kpiData?.kpiDetail?.kpiSource
    }
    this.triggerGaEvent(gaObj);
  }
  getColor(nodeName) {
    let color = '';
    for (const key in this.trendBoxColorObj) {
      if (this.trendBoxColorObj[key]?.nodeName == nodeName) {
        color = this.trendBoxColorObj[key]?.color;
      }
    }
    return color;
  }
  handleClearAll(event) {
    for (const key in this.filterOptions) {
      if (key?.toLowerCase() == event?.toLowerCase()) {
        delete this.filterOptions[key];
      }
    }
    this.optionSelected.emit(['Overall']);
  }

  showFilterTooltip(showHide, filterNo?) {
    if (showHide) {
      this.filterMultiSelectOptionsData['details'] = {};
      this.filterMultiSelectOptionsData['details'][filterNo] = [];
      for (let i = 0; i < this.filterOptions[filterNo]?.length; i++) {

        this.filterMultiSelectOptionsData['details'][filterNo]?.push(
          {
            type: 'paragraph',
            value: this.filterOptions[filterNo][i]
          }
        );
      }

    } else {
      this.filterMultiSelectOptionsData = {};
    }
  }

  toggleMenu(event) {
    this.kpimenu.toggle(event);
    // const menuElementRef = this.kpimenu as unknown as ElementRef;
    // const nativeElement = menuElementRef.nativeElement;
    // this.renderer.setStyle(nativeElement, 'left', '400px');
  }

  /** When field mapping dialog is opening */
  onOpenFieldMappingDialog() {
    this.getKPIFieldMappingConfig();
  }

  /** This method is responsible for getting field mapping configuration for specfic KPI */
  getKPIFieldMappingConfig() {
    const selectedTab = this.service.getSelectedTab().toLowerCase();
    const selectedType = this.service.getSelectedType().toLowerCase();
    const selectedTrend = this.service.getSelectedTrends();
    if (selectedType === 'scrum' && selectedTrend.length == 1 || selectedTab === 'release') {
      this.loadingKPIConfig = true;
      this.noDataKPIConfig = false;
      this.displayConfigModel = true;
      this.lastSyncTime = this.showExecutionDate(this.kpiData.kpiDetail.combinedKpiSource || this.kpiData.kpiDetail.kpiSource);
      this.http.getKPIFieldMappingConfig(`${selectedTrend[0]?.basicProjectConfigId}/${this.kpiData?.kpiId}`).subscribe(data => {
        if (data && data['success']) {
          this.fieldMappingConfig = data?.data['fieldConfiguration'];
          const kpiSource = data?.data['kpiSource']?.toLowerCase();
          const toolConfigID = data?.data['projectToolConfigId'];
          this.selectedToolConfig = [{ id: toolConfigID, toolName: kpiSource }];
          if (this.fieldMappingConfig.length > 0) {
            this.selectedConfig = { ...selectedTrend[0], id: selectedTrend[0]?.basicProjectConfigId }
            this.getFieldMapping();
            if (this.service.getFieldMappingMetaData().length && this.kpiData.kpiId !== 'kpi150') {
              const metaDataList = this.service.getFieldMappingMetaData();
              const metaData = metaDataList.find(data => data.projectID === selectedTrend[0]?.basicProjectConfigId && data.kpiSource === kpiSource);
              if (metaData && metaData.metaData && this.kpiData.kpiId !== 'kpi150') {
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
    this.http.getFieldMappingsWithHistory(this.selectedToolConfig[0].id,this.kpiData.kpiId, obj).subscribe(mappings => {
      if (mappings && mappings['success'] && Object.keys(mappings['data']).length >= 1) {
        this.selectedFieldMapping = mappings['data'].fieldMappingResponses;
        this.metaDataTemplateCode = mappings['data']?.metaTemplateCode
        this.displayConfigModel = true;
        this.loadingKPIConfig = false;

      } else {
        this.loadingKPIConfig = false;
      }
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
}
