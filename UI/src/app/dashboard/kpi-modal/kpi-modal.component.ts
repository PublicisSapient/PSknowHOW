import { Component, Input, OnInit } from '@angular/core';

@Component({
  selector: 'app-kpi-modal',
  templateUrl: './kpi-modal.component.html',
  styleUrls: ['./kpi-modal.component.css']
})
export class KpiModalComponent implements OnInit {

  @Input() kpi: any;
  @Input() kpiChartData: any;
  @Input() chartColorList: any;
  @Input() iSAdditionalFilterSelected: boolean;
  constructor() { }

  ngOnInit(): void {
  }

}
