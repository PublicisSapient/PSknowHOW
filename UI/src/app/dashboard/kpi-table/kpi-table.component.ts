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

  constructor() { }

  ngOnInit(): void {
    this.tabs = Object.keys(this.kpiData);  
  }

}
