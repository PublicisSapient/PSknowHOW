import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup } from '@angular/forms';

@Component({
  selector: 'app-kpi-filter',
  templateUrl: './kpi-filter.component.html',
  styleUrls: ['./kpi-filter.component.css']
})
export class KpiFilterComponent implements OnInit {
  @Input() kpiRelationShips: any;
  @Input() fieldMappings: any;
  @Output() formSavedEvent = new EventEmitter<string>();
  fieldMappingForm: UntypedFormGroup;
  selectedKpis = [];
  isLoading = false;
  disabled = false;
  displayPopup = false;
  fields = [];

  constructor(private formBuilder: UntypedFormBuilder) { }

  ngOnInit(): void {
    this.fieldMappingForm = this.formBuilder.group({});
  }

  getRelatedFieldsCount() {
    let result = 0;
    if (this.selectedKpis && this.selectedKpis.length) {
      this.selectedKpis.forEach((kpi) => {
        result += kpi.fieldNames.length;
      });
    }
    return result;
  }

  showFieldsPopup() {
    if (this.selectedKpis && this.selectedKpis.length) {
      const fieldMappingFormObj = {};
      this.selectedKpis.forEach((selectedKpi) => {
        this.fieldMappings.forEach((field) => {
          if (selectedKpi.fieldNames.includes(field['fieldName'])) {
            fieldMappingFormObj[field['fieldName']] = field['field'];
            this.fields.push({
              fieldName: field['fieldName'],
              type: field['type'],
              searchable: false
            });
          }
        });
      });

      this.fieldMappingForm = this.formBuilder.group(fieldMappingFormObj);
      this.displayPopup = true;
    }
  }

  clearForm() {
    this.fieldMappingForm = this.formBuilder.group({});
    this.fields = [];
  }

}
