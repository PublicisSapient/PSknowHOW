import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

@Component({
  selector: 'app-field-mapping-field',
  templateUrl: './field-mapping-field.component.html',
  styleUrls: ['./field-mapping-field.component.css'],
  providers:[
    {provide:NG_VALUE_ACCESSOR,
    useExisting: FieldMappingFieldComponent,
    multi:true}
  ]
})
export class FieldMappingFieldComponent implements OnInit,ControlValueAccessor {

  @Input() fieldConfig;
  @Output() onSearch = new EventEmitter();
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
    console.log(this.value);
    this.onChange(this.value);
  }

  resetRadioButton(fieldName) {
    this.value = '';
    this.setValue();
  }

  showDialogToAddValue(isSingle, fieldName, type){
    this.onSearch.emit({isSingle,fieldName,type});
  }

}
