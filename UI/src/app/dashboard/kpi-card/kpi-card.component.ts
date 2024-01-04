import { Component, OnInit, Input, Output, EventEmitter, OnDestroy,OnChanges, SimpleChanges } from '@angular/core';
import { faShareSquare } from '@fortawesome/free-solid-svg-icons';
import { SharedService } from 'src/app/services/shared.service';
import { HttpService } from 'src/app/services/http.service';
import { GetAuthorizationService } from 'src/app/services/get-authorization.service';
import { GoogleAnalyticsService } from 'src/app/services/google-analytics.service';
@Component({
  selector: 'app-kpi-card',
  templateUrl: './kpi-card.component.html',
  styleUrls: ['./kpi-card.component.css']
})
export class KpiCardComponent implements OnInit, OnDestroy,OnChanges {
  @Input() kpiData: any;
  @Input() trendData: Array<object>;
  @Output() downloadExcel = new EventEmitter<boolean>();
  @Input() dropdownArr: any;
  @Output() optionSelected = new EventEmitter<any>();
  @Output() reloadKPITab = new EventEmitter<any>();
  @Input() board?: string;
  faShareSquare = faShareSquare;
  isTooltip = false;
  filterTooltip = false;
  @Input() trendBoxColorObj: any;
  subscriptions: any[] = [];
  filterOption = 'Overall';
  filterOptions: object = {};
  radioOption: string;
  filterMultiSelectOptionsData: object = {};
  kpiSelectedFilterObj: any = {};
  @Input() isShow?: any;
  @Input() showExport: boolean;
  @Input() showTrendIndicator =true;
  @Input() showChartView = 'chart';
  @Input() cols: Array<object> = [];
  @Input() iSAdditionalFilterSelected =false;
  @Input() showCommentIcon: boolean;
  selectedTab: string;
  @Input() trendValueList : any
  @Input() colors : Array<any>;
  selectedTabIndex : number = 0;
  @Input() sprintsOverlayVisible : boolean;
  projectList : Array<string>;
  displaySprintDetailsModal : boolean = false;
  columnList = [
    { field: 'duration', header: 'Duration'  },
    { field: 'value', header: 'KPI Value', unit : 'unit' },
    { field: 'params', header: 'Calculation Details' },
 ];
 sprintDetailsList : Array<any>;
 colorCssClassArray = ['sprint-hover-project1','sprint-hover-project2','sprint-hover-project3','sprint-hover-project4','sprint-hover-project5','sprint-hover-project6'];
 displayConfigModel = false;
 fieldMappingMetaData = [];
 disableSave = false
 fieldMappingConfig = [];
 selectedFieldMapping = []
 selectedConfig: any = {};
 selectedToolConfig: any = [];
 loading : boolean = false
 noData : boolean = false
 @Input() commentCount : string;
 @Output() getCommentCountByKpi = new EventEmitter();
 userRole : string;
 checkIfViewer : boolean;

  constructor(public service: SharedService,
    private http : HttpService,
    private authService : GetAuthorizationService,
    private ga: GoogleAnalyticsService) {
  }

  ngOnChanges(changes: SimpleChanges) {
    // changes['dropdownArr']?.currentValue ? true : this.dropdownArr = [];
    this.userRole = this.authService.getRole();
   this.checkIfViewer =  (this.authService.checkIfViewer({id : this.service.getSelectedTrends()[0]?.basicProjectConfigId}));
  }

  ngOnInit(): void {
    this.subscriptions.push(this.service.selectedFilterOptionObs.subscribe((x) => {
       /** Refreshing multiselect filter when project/sprint/release are changing from the boards */
       this.filterOptions = x?.action === 'new' ? {} : {...this.filterOptions};
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
            else{
              this.filterOptions = {...this.filterOptions };
              this.filterOption = 'Overall';
            }
          }else {
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

              if (!this.filterOption) {
                this.filterOption = this.kpiSelectedFilterObj[this.kpiData?.kpiId]['filter1'] ? this.kpiSelectedFilterObj[this.kpiData?.kpiId]['filter1'][0] : this.kpiSelectedFilterObj[this.kpiData?.kpiId][0];
              }
            }
          }
        }
        if (this.kpiData?.kpiDetail?.hasOwnProperty('kpiFilter') && this.kpiData?.kpiDetail?.kpiFilter?.toLowerCase() == 'radiobutton') {
          if (this.kpiSelectedFilterObj[this.kpiData?.kpiId]) {
            // this.radioOption = this.kpiSelectedFilterObj[this.kpiData?.kpiId][0];
            this.radioOption = this.kpiSelectedFilterObj[this.kpiData?.kpiId]?.hasOwnProperty('filter1')?this.kpiSelectedFilterObj[this.kpiData?.kpiId]['filter1'][0]:this.kpiSelectedFilterObj[this.kpiData?.kpiId][0];
          }
        }
      }
      this.selectedTab = this.service.getSelectedTab() ? this.service.getSelectedTab().toLowerCase() : '';
    }));
    /** assign 1st value to radio button by default */
    if(this.kpiData?.kpiDetail?.hasOwnProperty('kpiFilter') && this.kpiData?.kpiDetail?.kpiFilter?.toLowerCase() == 'radiobutton' && this.dropdownArr?.length > 0){
      this.radioOption = this.dropdownArr[0]?.options[0];
    }
  }

  exportToExcel() {
    this.downloadExcel.emit(true);
  }

  showTooltip(val) {
    this.isTooltip = val;
  }

  handleChange(type, value) {
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

  prepareData() {
    this.projectList = [];
    this.sprintDetailsList = [];
    this.selectedTabIndex = 0;
    this.projectList = Object.values(this.colors).map(obj=> obj['nodeName']);
    this.projectList.forEach((project,index)=>{
      const selectedProjectTrend = this.trendValueList.find(obj=>obj.data === project);
      const tempColorObjArray = Object.values(this.colors).find(obj=>obj['nodeName'] === project)['color'];
      if(selectedProjectTrend && selectedProjectTrend.value){
        let hoverObjectListTemp = [];

        if(selectedProjectTrend.value[0]?.dataValue?.length > 0){
          this.columnList = [  { field: 'duration', header: 'Duration'  }];
         selectedProjectTrend.value[0].dataValue.forEach(d => {
            this.columnList.push({ field: d.name+' value', header: d.name+' KPI Value', unit : 'unit' });
            this.columnList.push({ field: d.name+' params', header: d.name+' Calculation Details', unit : 'unit' });
          });

          selectedProjectTrend.value.forEach(element => {
            let tempObj = {};
            tempObj['duration'] = element['sSprintName'] || element['date'];

            element.dataValue.forEach((d,i) =>{
              tempObj[d.name+' value'] = (Math.round(d['value'] * 100) / 100);
              tempObj['unit'] = ' ' + this.kpiData.kpiDetail?.kpiUnit
              if (d['hoverValue'] && Object.keys(d['hoverValue'])?.length > 0) {
                tempObj[d.name+' params'] = Object.entries(d['hoverValue']).map(([key, value]) => `${key} : ${value}`).join(', ');
              }
            });

            hoverObjectListTemp.push(tempObj);
          });
        }else{
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
          ['color']:tempColorObjArray
        });
      }else{
        this.sprintDetailsList.push({
          ['project']: project,
          ['hoverList']: [],
          ['color']:tempColorObjArray
        });
      }
    });
    this.displaySprintDetailsModal = true;
  }

  hasData(field: string): boolean {
    return this.sprintDetailsList[this.selectedTabIndex]['hoverList'].some(rowData => rowData[field] !== null && rowData[field] !== undefined);
  }

  getColorCssClasses(index){
    return this.colorCssClassArray[index];
  }

  /** When field mapping dialog is opening */
  onOpenFieldMappingDialog(){
    this.getKPIFieldMappingConfig();
  }

  /** This method is responsible for getting field mapping configuration for specfic KPI */
  getKPIFieldMappingConfig() {
    const selectedTab = this.service.getSelectedTab().toLowerCase();
    const selectedType = this.service.getSelectedType().toLowerCase();
    const selectedTrend = this.service.getSelectedTrends();
    if (selectedType === 'scrum' && selectedTrend.length == 1  || selectedTab === 'release') {
      this.loading = true;
      this.noData = false;
      this.displayConfigModel = true;
      this.http.getKPIFieldMappingConfig(`${selectedTrend[0]?.basicProjectConfigId}/${this.kpiData?.kpiId}`).subscribe(data => {
        if(data && data['success']){
          this.fieldMappingConfig = data?.data['fieldConfiguration'];
          const kpiSource = data?.data['kpiSource']?.toLowerCase();
          const toolConfigID = data?.data['projectToolConfigId'];
          this.selectedToolConfig = [{ id: toolConfigID, toolName: kpiSource }];
          if (this.fieldMappingConfig.length > 0) {
            this.selectedConfig = { ...selectedTrend[0], id: selectedTrend[0]?.basicProjectConfigId }
            this.getFieldMapping();
            if (this.service.getFieldMappingMetaData().length) {
              const metaDataList = this.service.getFieldMappingMetaData();
              const metaData = metaDataList.find(data => data.projectID === selectedTrend[0]?.basicProjectConfigId && data.kpiSource === kpiSource);
              if (metaData && metaData.metaData) {
                this.fieldMappingMetaData = metaData.metaData;
              } else {
                this.getFieldMappingMetaData(kpiSource);
              }
            } else {
              this.getFieldMappingMetaData(kpiSource);
            }
          } else {
            this.loading = false;
            this.noData = true;
          }
        }
      })
    }
  }

  getFieldMapping() {
    this.http.getFieldMappings(this.selectedToolConfig[0].id).subscribe(mappings => {
      if (mappings && mappings['success'] && Object.keys(mappings['data']).length >= 2) {
        this.selectedFieldMapping = mappings['data'];
        this.displayConfigModel = true;
        this.loading = false;

      } else {
        this.loading = false;
      }
    });
  }

  getFieldMappingMetaData(kpiSource) {
    this.http.getKPIConfigMetadata(this.selectedToolConfig[0].id).subscribe(Response => {
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

  reloadKPI(){
    this.displayConfigModel = false;
    this.reloadKPITab.emit(this.kpiData);
  }

  handleGetCount(event){
    this.getCommentCountByKpi.emit(event);
  }

  handleKpiClick(){
    const obj = {
      'kpiName': this.kpiData?.kpiName,
      'kpiSource': this.kpiData?.kpiDetail?.kpiSource,
      'filter1':null,
      'filter2':null
    }
    this.triggerGaEvent(obj)
  }

  triggerGaEvent(gaObj){
    this.ga.setKpiData(gaObj);
  }

  ngOnDestroy() {
    this.kpiData = {};
    this.trendData = [];
    this.subscriptions.forEach(subscription => subscription.unsubscribe());
  }
}
