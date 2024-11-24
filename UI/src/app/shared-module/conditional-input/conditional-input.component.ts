import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  Output,
  SimpleChanges,
} from '@angular/core';

@Component({
  selector: 'app-conditional-input',
  templateUrl: './conditional-input.component.html',
  styleUrls: ['./conditional-input.component.css'],
})
export class ConditionalInputComponent implements OnChanges {
  @Input() id;
  @Input() fieldConfig;
  @Input() valueObj;
  @Output() conditionalInputChange = new EventEmitter();
  finalValue = [];
  templateData = [];
  templateLabels = [];

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.valueObj && this.valueObj.length) {
      this.templateLabels = this.templateLabelToLowercase(
        this.valueObj.map((val) => val.labelValue),
      );
      this.templateData = this.fieldConfig.options.filter((opt) =>
        this.templateLabels.includes(opt.labelValue),
      );
      this.finalValue = [...this.templateData];
      this.valueObj.forEach((element) => {
        let opt = this.fieldConfig.options.filter(
          (opt) => opt.labelValue === element.labelValue.toLowerCase(),
        )[0];
        if (opt) {
          opt['countValue'] = element.countValue;
        }
      });
    }
  }

  templateLabelToLowercase = (arr: []) =>
    arr.map((val: any) => val.toLowerCase());

  setValue(event) {
    this.templateLabels = this.templateLabelToLowercase(
      event.value.map((val) => val.labelValue),
    );
    this.templateData = this.fieldConfig.options.filter((opt) =>
      this.templateLabels.includes(opt.labelValue),
    );
    let selectedOption = this.templateData.filter(
      (opt) => opt.labelValue === event.itemValue.labelValue,
    )[0];
    if (selectedOption) {
      selectedOption['countValue'] = selectedOption['minValue'];
    }
    this.setOutput();
  }

  setCounter(event, option) {
    if (
      !this.templateData.filter((opt) => opt.labelValue === option.labelValue)
        .length
    ) {
      let newOption = JSON.parse(JSON.stringify(option));
      newOption.countValue = event.value;
      this.templateData.push(newOption);
    } else {
      this.templateData.filter(
        (opt) => opt.labelValue === option.labelValue,
      )[0].countValue = event.value;
    }
    this.setOutput();
  }

  removeFocus(event) {
    event.target.blur();
  }

  setOutput() {
    this.conditionalInputChange.emit(this.finalValue);
  }
}
