import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { distinctUntilChanged } from 'rxjs/operators';
import { SharedService } from 'src/app/services/shared.service';

@Component({
  selector: 'app-kpi-modal',
  templateUrl: './kpi-modal.component.html',
  styleUrls: ['./kpi-modal.component.css']
})
export class KpiModalComponent implements OnInit, OnChanges {

  @Input() kpi: any;
  @Input() kpiChartData: any;
  @Input() chartColorList: any;
  @Input() iSAdditionalFilterSelected: boolean;
  @Input() filterApplyData: any;
  @Input() trendData: any;
  @Input() trendBoxColorObj: any;
  @Input() kpiThresholdObj: any;
  @Input() trendValueList : any
  selectedTabIndex: number = 0;
  projectList: Array<string>;
  colorObj: any;
  colorCssClassArray = ['sprint-hover-project1', 'sprint-hover-project2', 'sprint-hover-project3', 'sprint-hover-project4', 'sprint-hover-project5', 'sprint-hover-project6'];
  sprintDetailsList: any;
  displaySprintDetailsModal : boolean = false;
  columnList = [
    { field: 'duration', header: 'Duration'  },
    { field: 'value', header: 'KPI Value', unit : 'unit' },
    { field: 'params', header: 'Calculation Details' },
 ];

  constructor(private service: SharedService) {

  }

  ngOnInit(): void {

  }

  ngOnChanges(changes: SimpleChanges): void {
    this.colorObj = this.service.colorObjGetter();
    this.colorObj = Object.keys(this.colorObj).map(key => this.colorObj[key]);
    this.prepareData();
  }

  getColorCssClasses(index) {
    return this.colorCssClassArray[index];
  }

  prepareData() {
    this.projectList = [];
    this.sprintDetailsList = [];
    this.selectedTabIndex = 0;
    this.projectList = Object.values(this.colorObj).map(obj=> obj['nodeName']);
    this.projectList.forEach((project,index)=>{
      const selectedProjectTrend = this.trendValueList.find(obj=>obj.data === project);
      const tempColorObjArray = Object.values(this.colorObj).find(obj=>obj['nodeName'] === project)['color'];
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
              tempObj['unit'] = ' ' + this.kpi.kpiDetail?.kpiUnit
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
          tempObj['unit'] = ' ' + this.kpi.kpiDetail?.kpiUnit
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
  }

  hasData(field: string): boolean {
    return this.sprintDetailsList[this.selectedTabIndex]['hoverList'].some(rowData => rowData[field] !== null && rowData[field] !== undefined);
  }

}
