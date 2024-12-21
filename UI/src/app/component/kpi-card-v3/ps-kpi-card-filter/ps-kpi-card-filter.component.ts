import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';

@Component({
  selector: 'app-ps-kpi-card-filter',
  templateUrl: './ps-kpi-card-filter.component.html',
  styleUrls: ['./ps-kpi-card-filter.component.css'],
})
export class PsKpiCardFilterComponent implements OnInit {
  @Input() kpiCardFilter: any;
  @Output() filterChange = new EventEmitter<any>();
  @Output() filterClear = new EventEmitter<any>();
  selectedKeyObj;
  

  form: FormGroup;
  constructor(private fb: FormBuilder) {
    this.form = this.fb.group({
        selectedKey: [],
    });
  }

  ngOnInit(): void {
    this.kpiCardFilter?.filterGroup?.filterGroup1?.forEach((filter: { filterKey: any; }) => {
      this.form.addControl(filter.filterKey, this.fb.control(''));
    });
    this.setDefaultFilter(this.kpiCardFilter);
    this.setDefaultForm();
  }

  getOptions(filterKey: string) {
    const uniqueValues = [
      ...new Set(this.kpiCardFilter.issueData.map((issue: { [x: string]: any; }) => issue[filterKey])),
    ];
    return uniqueValues.map((value) => ({ label: value, value: value }));
  }

  handleChange() {
    const transformedObject = {};

    for (const [key, value] of Object.entries(this.form.value)) {
      if (Array.isArray(value) && value.length === 0) {
        transformedObject[key] = null;
      } else {
        transformedObject[key] = value;
      }
    }
    this.filterChange.emit({...transformedObject,selectedKeyObj:this.selectedKeyObj});
  }

  clearFilters() {
    this.filterClear.emit(true);
  }

  onSelectButtonChange(event) {
    this.form.get('selectedKey')?.setValue(event.value); // Update selectedKey in the form
    const tempObject = {
      [this.kpiCardFilter.categoryData.categoryKey] : event.value
    }
    this.selectedKeyObj = tempObject;
    this.handleChange();
  }

  
  setDefaultFilter(filter: any) {
    if (filter.kpiFilters) {
      Object.entries(filter.kpiFilters).forEach(([key, value]) => {
        this.form.get(key)?.setValue(value);
      });
      this.handleChange();
    }
  }

  clearMultiSelect(controlName: string){
    this.form.get(controlName)?.reset();
    this.handleChange();
  }

  getSelectButtonOptions(): any[] {
    if (this.kpiCardFilter?.chartType === 'stacked-bar-chart') {
      return this.kpiCardFilter?.dataGroup?.dataGroup1 || [];
    } else if (this.kpiCardFilter?.chartType === 'grouped-bar-chart') {
      return this.kpiCardFilter?.categoryData?.categoryGroup || [];
    }
    return [];
  }

  getOptionLabel(): string {
    return this.kpiCardFilter?.chartType === 'stacked-bar-chart' ? 'name' : 'categoryName';
  }
  
  getOptionValue(): string {
    return this.kpiCardFilter?.chartType === 'stacked-bar-chart' ? 'key' : 'categoryName';
  }

  setDefaultForm(){
    let returnVal = '';
    if (this.kpiCardFilter?.chartType === 'stacked-bar-chart') {
      returnVal = this.kpiCardFilter?.dataGroup?.dataGroup1?.[0]?.key 
      
    } else if (this.kpiCardFilter?.chartType === 'grouped-bar-chart') {
      returnVal =this.kpiCardFilter?.categoryData?.categoryGroup[0].categoryName
    }
    this.form.get('selectedKey')?.setValue(returnVal);
  }
}
