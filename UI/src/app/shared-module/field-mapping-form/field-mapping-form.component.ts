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
import { Component, Input, OnInit,Output,EventEmitter } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { SharedService } from '../../services/shared.service';
import { HttpService } from '../../services/http.service';
import { MessageService,ConfirmationService } from 'primeng/api';

@Component({
  selector: 'app-field-mapping-form',
  templateUrl: './field-mapping-form.component.html',
  styleUrls: ['./field-mapping-form.component.css']
})
export class FieldMappingFormComponent implements OnInit {
  @Input() fieldMappingMetaData;
  @Input() disableSave= false;
  @Input() fieldMappingConfig;
  @Input() formData;
  @Input() selectedConfig;
  @Input() selectedToolConfig;
  @Input() thresholdUnit;
  @Output() reloadKPI = new EventEmitter();
  populateDropdowns = true;
  selectedField = '';
  singleSelectionDropdown = false;
  fieldMappingMultiSelectValues: any = [];
  selectedValue = [];
  selectedMultiValue = [];
  displayDialog = false;
  selectedFieldMapping: any = {};
  bodyScrollPosition = 0;
  uploadedFileName = '';

  filterHierarchy: any = [];
  form: FormGroup;
  fieldMappingSectionList = [];
  formConfig: any;

private setting = {
  element: {
    dynamicDownload: null as HTMLElement
  }
};

  constructor(private sharedService : SharedService,
    private http : HttpService,
    private messenger: MessageService,
    private confirmationService: ConfirmationService) { }

  ngOnInit(): void {
    this.filterHierarchy = JSON.parse(localStorage.getItem('completeHierarchyData')).scrum;
    this.initializeForm();
    this.generateFieldMappingConfiguration();
  }

  generateFieldMappingConfiguration(){
    const fieldMappingSections = [];
    const fieldMappingConfigration = {};
    this.fieldMappingConfig.forEach(field => {
      fieldMappingSections.push(field.section);
      if(!fieldMappingConfigration[field.section]){
        fieldMappingConfigration[field.section] = [field];
      }else{
        fieldMappingConfigration[field.section].push(field);
      }
    });
    this.fieldMappingSectionList = [...new Set(fieldMappingSections)].sort((a, b) => a.localeCompare(b, undefined, { sensitivity: 'base' }));
    this.formConfig = fieldMappingConfigration;

  }

  initializeForm(){
    const formObj ={};
    for (const field of this.fieldMappingConfig) {
      formObj[field.fieldName] = this.generateFromControlBasedOnFieldType(field)
      if (field.hasOwnProperty('nestedFields')) {
        for (const nField of field.nestedFields) {
          formObj[nField.fieldName] = this.generateFromControlBasedOnFieldType(nField)
        }
      }
    }
    this.form = new FormGroup(formObj);
  }

  /** This method is taking config as parameter, creating form control and assigning initial value based on fieldtype */
  generateFromControlBasedOnFieldType(config){
    if(this.formData?.hasOwnProperty(config.fieldName)){
      return new FormControl(this.formData[config.fieldName]);
    }else{
      switch(config.fieldType){
        case 'text':
          return new FormControl('');
        case 'radiobutton':
          return new FormControl('');
        case 'toggle':
          return new FormControl(false);
        case 'number':
          return new FormControl('');
        default:
          return new FormControl([]);
      }
    }
  }

  /** When user import mapping template this method will set values in form */
  setControlValueOnImport(values){
    this.selectedFieldMapping = values;
     if (this.selectedFieldMapping && Object.keys(this.selectedFieldMapping).length) {
      for (const obj in this.selectedFieldMapping) {
        if (this.form && this.form.controls[obj]) {
          this.form.controls[obj].setValue(this.selectedFieldMapping[obj]);
        }
      }
    }
     if (!this.form.invalid) {
      this.formData = {...this.formData,...this.selectedFieldMapping};
      const submitData = {...this.formData};
      submitData['basicProjectConfigId'] = this.selectedConfig.id;
      delete submitData.id;
      this.saveFieldMapping(submitData);
    }
  }


  /** once user willl click on search btn, assign the search options based on field category */
  showDialogToAddValue({isSingle, fieldName, type}) {
    this.populateDropdowns = true;
    this.selectedField = fieldName;

    if (isSingle) {
      this.singleSelectionDropdown = true;
    } else {
      this.singleSelectionDropdown = false;
    }

    switch (type) {
      case 'fields':
        if (this.fieldMappingMetaData && this.fieldMappingMetaData.fields) {
          this.fieldMappingMultiSelectValues = this.fieldMappingMetaData.fields;
        } else {
          this.fieldMappingMultiSelectValues = [];
        }
        break;
      case 'workflow':
        if (this.fieldMappingMetaData && this.fieldMappingMetaData.workflow) {
          this.fieldMappingMultiSelectValues = this.fieldMappingMetaData.workflow;
        } else {
          this.fieldMappingMultiSelectValues = [];
        }
        break;
      case 'Issue_Link':
        if (this.fieldMappingMetaData && this.fieldMappingMetaData.Issue_Link) {
          this.fieldMappingMultiSelectValues = this.fieldMappingMetaData.Issue_Link;
        } else {
          this.fieldMappingMultiSelectValues = [];
        }
        break;
      case 'Issue_Type':
        if (this.fieldMappingMetaData && this.fieldMappingMetaData.Issue_Type) {
          this.fieldMappingMultiSelectValues = this.fieldMappingMetaData.Issue_Type;
        } else {
          this.fieldMappingMultiSelectValues = [];
        }
        break;
      default:
        this.fieldMappingMultiSelectValues = [];
        break;
    }

    if (isSingle) {
      if (this.form.controls[this.selectedField].value) {
        this.selectedValue = this.fieldMappingMultiSelectValues.filter(fieldMappingMultiSelectValue => (fieldMappingMultiSelectValue.data === this.form.controls[this.selectedField].value));
        if (this.selectedValue && this.selectedValue.length) {
          if (this.selectedValue[0].data) {
            this.selectedValue = this.selectedValue[0].data;
          }
        }
      }
    } else {
      if (this.form.controls[this.selectedField].value) {
        this.selectedMultiValue = this.fieldMappingMultiSelectValues.filter(fieldMappingMultiSelectValue => (this.form.controls[this.selectedField].value).includes(fieldMappingMultiSelectValue.data));
      }
    }

    this.displayDialog = true;
  }

  /** close search dialog */
  cancelDialog() {
    this.populateDropdowns = false;
    this.displayDialog = false;
  }

  /** Once user select value and click on save then selected option will be populated on chip/textbox */
  saveDialog() {
    if (this.singleSelectionDropdown) {
      if (this.selectedValue.length) {
        this.form.controls[this.selectedField].setValue(this.selectedValue);
      }
    } else {
      const selectedMultiValueLabels = [];
      if (this.selectedMultiValue.length) {
        if (this.form.controls[this.selectedField].value) {
          for (const index in this.selectedMultiValue) {
            selectedMultiValueLabels.push(this.selectedMultiValue[index].key);
          }
          const allMultiValueLabels = [];
          for (const index in this.fieldMappingMultiSelectValues) {
            allMultiValueLabels.push(this.fieldMappingMultiSelectValues[index].key);
          }

          if (!selectedMultiValueLabels.includes(this.form.controls[this.selectedField].value)) {
            for (const selectedFieldIndex in this.form.controls[this.selectedField].value) {
              if (!allMultiValueLabels.includes(this.form.controls[this.selectedField].value[selectedFieldIndex])) {
                selectedMultiValueLabels.push(this.form.controls[this.selectedField].value[selectedFieldIndex]);
              }
            }
          }

        }
      }

      this.form.controls[this.selectedField].setValue(Array.from(new Set(selectedMultiValueLabels)));
    }
    this.populateDropdowns = false;
    this.displayDialog = false;
  }


  recordScrollPosition() {
    this.bodyScrollPosition = document.documentElement.scrollTop;
  }

  scrollToPosition() {
    this.populateDropdowns = false;
    document.documentElement.scrollTop = this.bodyScrollPosition;
  }

  /** Responsible for handle template popup */
  save() {
    const submitData = {...this.formData,...this.form.value};
    submitData['basicProjectConfigId'] = this.selectedConfig.id;
    delete submitData.id;
    if(this.selectedToolConfig[0].toolName.toLowerCase() === 'jira'){
      this.http.getMappingTemplateFlag(this.selectedToolConfig[0].id, submitData).subscribe(response => {
        if (response && response['success']) {
          if (response['data']) {
            this.confirmationService.confirm({
              message: `Please note that change in mappings is a deviation from initially configured template.
              If you continue with the change in mappings then these changes will be mapped to a
              Custom template in project configurations which cannot be changed again to a initially configured template.`,
              header: 'Template Change Info',
              key: 'templateInfoDialog',
              accept: () => {
                this.saveFieldMapping(submitData);
              },
              reject: () => {}
            });
          } else {
          this.saveFieldMapping(submitData);
          }
        }else{
          this.messenger.add({
            severity: 'error',
            summary: 'Some error occurred. Please try again later.'
          });
        }

      });
    }else{
      this.saveFieldMapping(submitData);
    }

  }

  /** Responsible for handle save */
  saveFieldMapping(mappingData) {
    this.http.setFieldMappings(this.selectedToolConfig[0].id, mappingData).subscribe(response => {
      if (response && response['success']) {
        this.messenger.add({
          severity: 'success',
          summary: 'Field Mappings submitted!!',
        });
        this.uploadedFileName = '';
        this.reloadKPI.emit();
      } else {
        this.messenger.add({
          severity: 'error',
          summary: 'Some error occurred. Please try again later.'
        });
      }
    });
  }




  private dyanmicDownloadByHtmlTag(arg: {
    fileName: string;
    text: string;
  }) {
    if (!this.setting.element.dynamicDownload) {
      this.setting.element.dynamicDownload = document.createElement('a');
    }
    const element = this.setting.element.dynamicDownload;
    const fileType = arg.fileName.indexOf('.json') > -1 ? 'text/json' : 'text/plain';
    element.setAttribute('href', `data:${fileType};charset=utf-8,${encodeURIComponent(arg.text)}`);
    element.setAttribute('download', arg.fileName);

    const event = new MouseEvent('click');
    element.dispatchEvent(event);
  }
}
