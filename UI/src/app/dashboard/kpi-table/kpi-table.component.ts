import { Component, Input, OnInit } from '@angular/core';

@Component({
  selector: 'app-kpi-table',
  templateUrl: './kpi-table.component.html',
  styleUrls: ['./kpi-table.component.css']
})
export class KpiTableComponent implements OnInit {
  @Input() cols: Array<object> = [];
  @Input() kpiData: object = {};
  activeIndex: number = 0;
  tabs:Array<string> = [];
  showToolTip:boolean = false;
  toolTipHtml:string = '';
  left:string = '';
  top: string = '';

  constructor() { }

  ngOnInit(): void {
    this.tabs = Object.keys(this.kpiData);  
  }

  mouseEnter(event, field, data, selectedTab){  
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
}
