import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

@Component({
  selector: 'app-conditional-input',
  templateUrl: './conditional-input.component.html',
  styleUrls: ['./conditional-input.component.css']
})
export class ConditionalInputComponent implements OnInit {
  @Input() id;
  @Input() fieldConfig;
  @Output() conditionalInputChange = new EventEmitter();
  value = [];
  finalValue = [];
  templateData = [];
  templateLabels = [];
  constructor() { }

  ngOnInit(): void {
    console.log(this.fieldConfig);
  }

  setValue(event) {
    console.log(event);
    if (event.value.filter((val) => val.labelValue === event.itemValue.labelValue).length > 1) {
      event.value = event.value.filter((val) => val.labelValue !== event.itemValue.labelValue);
    }
    this.templateLabels = event.value.map((val) => val.labelValue);
    this.templateData = this.fieldConfig.options.filter((opt) => this.templateLabels.includes(opt.labelValue));
    this.setOutput();
  }

  setCounter(event, option) {
    console.log(event.value);
    if (!this.templateData.filter((opt) => opt.label === option.label).length) {
      let newOption = JSON.parse(JSON.stringify(option));
      newOption.countValue = event.value;
      this.templateData.push(newOption);
    } else {
      this.templateData.filter((opt) => opt.label === option.label)[0].countValue = event.value;
    }

    this.setOutput();

  }

  setOutput() {
    // this.finalValue.forEach((val) => {
    //   delete val.value;
    //   delete val.maxValue;
    //   delete val.minValue;
    //   delete val.operator;
    // });
    // this.finalValue = [];
    // this.templateData.forEach((opt)=>{
    //   this.finalValue.push({
    //     'labelValue': opt.label,
    //     'countValue': opt.countValue
    //   });
    // });

    // this.finalValue = this.finalValue.map((val) => {
    //   return {
    //     'labelValue': val.labelValue,
    //     'countValue': val.countValue
    //   }
    // });
    console.log(this.finalValue);
    this.conditionalInputChange.emit(this.finalValue);
  }

}
