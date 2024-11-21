import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

@Component({
  selector: 'app-ps-kpi-card-filter',
  templateUrl: './ps-kpi-card-filter.component.html',
  styleUrls: ['./ps-kpi-card-filter.component.css']
})
export class PsKpiCardFilterComponent implements OnInit {

  @Input()  kpiCardFilter: any;
  @Output() filterChange = new EventEmitter<any>();

  dropdownSelected = {};
  selectedKey: '';
  constructor() { }

  ngOnInit(): void {
  }


  getOptions(filterKey: string) {
    const uniqueValues = [
      ...new Set(this.kpiCardFilter.issueData.map((issue) => issue[filterKey])),
    ];
    return uniqueValues.map((value) => ({ label: value, value: value }));
  }

  handleChange() {
    this.filterChange.emit(this.dropdownSelected)
  
  }



}
