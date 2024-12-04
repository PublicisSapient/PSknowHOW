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

  form: FormGroup;
  constructor(private fb: FormBuilder) {
    this.form = this.fb.group({
        selectedKey: [''],
    });
  }

  ngOnInit(): void {
    this.kpiCardFilter.filterGroup.filterGroup1.forEach((filter) => {
      this.form.addControl(filter.filterKey, this.fb.control(''));
    });
    this.setDefaultFilter(this.kpiCardFilter);
  }

  getOptions(filterKey: string) {
    const uniqueValues = [
      ...new Set(this.kpiCardFilter.issueData.map((issue) => issue[filterKey])),
    ];
    return uniqueValues.map((value) => ({ label: value, value: value }));
  }

  handleChange() {
    const filterData = Object.fromEntries(
      Object.entries(this.form.value).filter(
        ([_, value]) => value !== '' && value !== null && value !== undefined,
      ),
    );
    this.filterChange.emit(this.form.value);
    
  }

  onSelectButtonChange(event) {
   // this.form.get('selectedKey')?.setValue(event.value); // Update selectedKey in the form
    const test = this.kpiCardFilter.dataGroup.dataGroup1.filter(
      (x) => x.key == event.value,
    );
    //this.selectedKey =
    this.form.get('selectedKey')?.setValue(test.map((item) => ({
        unit: item.unit,
        key: item.key,
      })))

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
}
