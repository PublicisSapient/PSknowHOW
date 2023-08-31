import { Component, Input, OnInit, SimpleChanges } from '@angular/core';

@Component({
  selector: 'app-kpi-table',
  templateUrl: './kpi-table.component.html',
  styleUrls: ['./kpi-table.component.css']
})
export class KpiTableComponent implements OnInit {
  @Input() cols: Array<object> = [];
  @Input() kpiData: object = {};
  @Input() colorObj: object = {};
  @Input() kpiConfigData: object = {};
  activeIndex: number = 0;
  tabs:Array<string> = [];
  showToolTip:boolean = false;
  toolTipHtml:string = '';
  left:string = '';
  top: string = '';
  nodeColors:object = {};

  constructor() { }

  ngOnInit(): void {
    this.assignColorToNodes();
  }

  ngOnChanges(changes: SimpleChanges) {
    if(changes['kpiData']?.currentValue != changes['kpiData']?.previousValue){
      this.kpiData = changes['kpiData']?.currentValue;
    }
    if(changes['colorObj']?.currentValue != changes['colorObj']?.previousValue){
      this.assignColorToNodes();
    }
    if(changes['kpiConfigData']?.currentValue != changes['kpiConfigData']?.previousValue){
      this.kpiConfigData = changes['kpiConfigData']?.currentValue;
    }
  }  

  mouseEnter(event, field, data){  
    if(field == 'frequency'){
      if(data?.hoverText?.length > 0){
        data.hoverText.forEach((item) =>{
          this.toolTipHtml += `<span>${item}</span><br/>`;
        });
        this.top = event.pageY + 'px';
        this.left = event.pageX + 'px';
        this.showToolTip = true;
      }
    }
  }

  mouseLeave(){
    this.showToolTip = false;
    this.toolTipHtml = '';
  }

  assignColorToNodes(){
    this.nodeColors = {};
    for(let key in this.colorObj){
      this.nodeColors[this.colorObj[key]?.nodeName] = this.colorObj[key]?.color; 
      this.tabs = Object.keys(this.nodeColors);
    }
  }
}
