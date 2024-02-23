import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

@Component({
  selector: 'app-conditional-input',
  templateUrl: './conditional-input.component.html',
  styleUrls: ['./conditional-input.component.css']
})
export class ConditionalInputComponent implements OnInit {
  @Input() id;
  @Input() fieldConfig;
  @Input() valueObj;
  @Output() conditionalInputChange = new EventEmitter();
  finalValue = [];
  templateData = [];
  templateLabels = [];
  constructor() { }

  ngOnInit(): void {
    if (this.valueObj && this.valueObj.length) {
      this.templateLabels = this.valueObj.map((val) => val.labelValue);
      this.templateData = this.fieldConfig.options.filter((opt) => this.templateLabels.includes(opt.labelValue));
      this.finalValue = [...this.templateData];
      this.valueObj.forEach(element => {
        let opt = this.fieldConfig.options.filter((opt) => opt.labelValue === element.labelValue)[0];
        opt.countValue = element.countValue;
      });
    }
  }

  setValue(event) {
    if (event.value.filter((val) => val.labelValue === event.itemValue.labelValue).length > 1) {
      event.value = event.value.filter((val) => val.labelValue !== event.itemValue.labelValue);
    }
    this.templateLabels = event.value.map((val) => val.labelValue);
    this.templateData = this.fieldConfig.options.filter((opt) => this.templateLabels.includes(opt.labelValue));
    let selectedOption = this.templateData.filter((opt) => opt.labelValue ===  event.itemValue.labelValue)[0];
    selectedOption['countValue'] = selectedOption['minValue'];
    this.setOutput();
  }

  setCounter(event, option) {
    if (!this.templateData.filter((opt) => opt.labelValue === option.labelValue).length) {
      let newOption = JSON.parse(JSON.stringify(option));
      newOption.countValue = event.value;
      this.templateData.push(newOption);
    } else {
      this.templateData.filter((opt) => opt.labelValue === option.labelValue)[0].countValue = event.value;
    }
    this.setOutput();
  }

  setOutput() {
    this.conditionalInputChange.emit(this.finalValue);
  }

}
