/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

@Component({
  selector: 'app-field-mapping-field',
  templateUrl: './field-mapping-field.component.html',
  styleUrls: ['./field-mapping-field.component.css'],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: FieldMappingFieldComponent,
      multi: true
    }
  ]
})
export class FieldMappingFieldComponent implements OnInit, ControlValueAccessor {

  @Input() fieldConfig;
  @Output() onSearch = new EventEmitter();
  @Input() fieldMappingMetaData;
  @Input() thresholdUnit;
  value;
  isDisabled = false;

  constructor() { }
  onChange = (val) => {
  };
  onTouched = () => { };
  writeValue(val: any): void {
    this.value = val;
  }
  registerOnChange(fn: any): void {
    this.onChange = fn;
  }
  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }
  setDisabledState?(isDisabled: boolean): void {
    this.isDisabled = isDisabled;
  }

  ngOnInit(): void {
  }

  setValue() {
    this.onChange(this.value);
  }

  resetRadioButton(fieldName) {
    this.value = true;
    this.setValue();
  }

  setAdditionalFilterValue(value) {
    this.value = value;
    this.setValue();
  }

  showDialogToAddValue(isSingle, fieldName, type) {
    this.onSearch.emit({ isSingle, fieldName, type });
  }

  enterNumericValue(event) {
    if (!!event && !!event.preventDefault && event.key === '.' || event.key === 'e' || event.key === '-' || event.key === '+') {
        event.preventDefault();
        return;
    }
}
  numericInputUpDown(event: any) {
    this.setValue();
  }
}
