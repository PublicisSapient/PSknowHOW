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
import { FormControl, FormGroup, Validators } from '@angular/forms';
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
  //isFormDirty : boolean = false;
  historyList = [];
  showSpinner: boolean = false;
  isHistoryPopup : any = {};
  @Input() kpiId : string;
  individualFieldHistory = [];
  @Input() metaDataTemplateCode : any;
  @Input() parentComp : string;
  nestedFieldANDParent = {}
  @Input() nodeId: string = '';

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
    this.historyList = [];
    this.filterHierarchy = JSON.parse(localStorage.getItem('completeHierarchyData')).scrum;
    this.initializeForm();
    this.generateFieldMappingConfiguration();
    this.form.valueChanges.subscribe(()=>{
    })
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
   const sectionsInCorrectOrder = ["Custom Fields Mapping","Issue Types Mapping", "Defects Mapping","WorkFlow Status Mapping","Additional Filter Identifier","Project Level Threshold"]
       const sectionsList = [...new Set(fieldMappingSections)].sort((a, b) => a.localeCompare(b, undefined, { sensitivity: 'base' }));
       this.fieldMappingSectionList = this.getSectionsInCorrectOrder(sectionsList,sectionsInCorrectOrder)
       this.formConfig = this.sortingOfFieldMapping(fieldMappingConfigration)

  }

  sortingOfFieldMapping(data) {
    const sortedData = {};
    for (const category in data) {
      if (data.hasOwnProperty(category)) {
        sortedData[category] = data[category].sort((a, b) => a?.fieldDisplayOrder - b?.fieldDisplayOrder);
      }
    }
    return sortedData;
  }

  getSectionsInCorrectOrder(incorrectOrder, correctOrder) {
    const orderMap = new Map();

    // Create a map with the correct order
    correctOrder.forEach((item, index) => {
      orderMap.set(item, index);
    });

    // Sort the incorrect order list based on the correct order map
    return incorrectOrder.sort((a, b) => orderMap.get(a) - orderMap.get(b));
  }

  initializeForm(){
    const formObj ={};
    for (const field of this.fieldMappingConfig) {
      this.isHistoryPopup[field.fieldName] = false;
      formObj[field.fieldName] = this.generateFromControlBasedOnFieldType(field)
      if (field.hasOwnProperty('nestedFields')) {
        for (const nField of field.nestedFields) {
          this.nestedFieldANDParent[nField.fieldName] = field.fieldName;
          this.isHistoryPopup[nField.fieldName] = false;
          formObj[nField.fieldName] = this.generateFromControlBasedOnFieldType(nField)
        }
      }
    }
    this.form = new FormGroup(formObj);
  }

  /** This method is taking config as parameter, creating form control and assigning initial value based on fieldtype */
  generateFromControlBasedOnFieldType(config) {
    const fieldMapping = this.formData.find(data => data.fieldName === config.fieldName)
    if (fieldMapping?.history && fieldMapping?.history?.length) {
      this.historyList.push({
        fieldName: fieldMapping.fieldName,
        history: fieldMapping.history
      })
    }
    if (fieldMapping && (fieldMapping?.originalValue || fieldMapping?.originalValue === false) || (!isNaN(fieldMapping?.originalValue) && fieldMapping?.originalValue >= 0)) {
      return new FormControl(fieldMapping.originalValue,config.mandatory ? Validators.required : []);
    } else {
      switch (config.fieldType) {
        case 'text':
          return new FormControl('',config.mandatory ? Validators.required : []);
        case 'radiobutton':
          return new FormControl('',config.mandatory ? Validators.required : []);
        case 'toggle':
          return new FormControl(false);
        case 'number':
          return new FormControl('');
        default:
          return new FormControl([],config.mandatory ? Validators.required : []);
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
     const finalList = [];

     Object.keys(this.selectedFieldMapping).forEach(fieldName => {
                 const originalVal = this.selectedFieldMapping[fieldName];
             finalList.push({ fieldName: fieldName, originalValue: originalVal })
     });
     this.saveFieldMapping(finalList,true);
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
      case 'releases':
        if (this.fieldMappingMetaData && this.fieldMappingMetaData.releases) {
          // Set the 'disabled' property and segregate items in a single pass
          const { enabledItems, disabledItems } = this.fieldMappingMetaData.releases.reduce((acc: any, item: any) => {
            item['disabled'] = item.data.includes("duration - days");
            if (item['disabled']) {
              acc.disabledItems.push(item);
            } else {
              acc.enabledItems.push(item);
            }
            return acc;
          }, { enabledItems: [], disabledItems: [] });

          // Concatenate the non-disabled items with the disabled items
          this.fieldMappingMetaData.releases = [...enabledItems, ...disabledItems];
          this.fieldMappingMultiSelectValues = this.fieldMappingMetaData.releases;
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
    const preSaveFormValueList = {...this.form.controls[this.selectedField].value}
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
    //#region  DTS-39044 fix
    const afterSaveFormValueList = {...this.form.controls[this.selectedField].value}
    if(JSON.stringify(preSaveFormValueList) != JSON.stringify(afterSaveFormValueList)){
      this.form.markAsDirty();
      this.form.markAsTouched();
      this.form.updateValueAndValidity();
    }
    //#endregion
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
    const finalList = [];

    this.formData.forEach(element => {
      const formValue = this.form.value[element.fieldName];
      const isChangedFromPreviousOne = this.compareValues(element?.originalValue, formValue);
      if (!isChangedFromPreviousOne) {
        finalList.push({ fieldName: element.fieldName, originalValue: formValue, previousValue: element.originalValue })
        /** Adding parent field value if nested field changes */
        if (this.nestedFieldANDParent.hasOwnProperty(element.fieldName)) {
          finalList.push({ fieldName: this.nestedFieldANDParent[element.fieldName], originalValue: this.form.value[this.nestedFieldANDParent[element.fieldName]]})
        }
      }
    });

     if(this.selectedToolConfig[0].toolName.toLowerCase() === 'jira' || this.selectedToolConfig[0].toolName.toLowerCase() === 'azure'){
          if (!(this.metaDataTemplateCode && this.metaDataTemplateCode === '9' || this.metaDataTemplateCode === '10' )) {
            this.confirmationService.confirm({
              message: `Please note that change in mappings is a deviation from initially configured template.
              If you continue with the change in mappings then these changes will be mapped to a
              Custom template in project configurations which cannot be changed again to a initially configured template.`,
              header: 'Template Change Info',
              key: 'templateInfoDialog',
              accept: () => {
                this.saveFieldMapping(finalList);
              },
              reject: () => {}
            });
          } else {
          this.saveFieldMapping(finalList);
          }
    }else{
      this.saveFieldMapping(finalList);
    }

  }

  /** Responsible for handle save */
  saveFieldMapping(mappingData,isImport?) {
    let mappingObj = {
      "releaseNodeId": this.nodeId || null,
      "fieldMappingRequests": [...mappingData]
    }
    this.http.setFieldMappings(this.selectedToolConfig[0].id, mappingObj,this.kpiId,isImport).subscribe(response => {
      if (response && response['success']) {
        this.messenger.add({
          severity: 'success',
          summary: 'Field Mappings submitted!!',
        });
      //#region Bug:39044
      this.form.markAsPristine();
      this.form.markAsUntouched();
      this.form.updateValueAndValidity();
      //#endregion
        this.uploadedFileName = '';
        if(this.parentComp === 'kpicard'){
          this.reloadKPI.emit();
        }else{
          this.refreshFieldMapppingValueANDHistory();
        }
      } else {
        this.messenger.add({
          severity: 'error',
          summary: response['message']
        });
      }
    });
  }

compareValues(originalValue: any, previousValue: any): boolean {
  if (typeof originalValue !== typeof previousValue) {
      return false; // Different types, not equal
  }

  if (Array.isArray(originalValue)) {
      if (!Array.isArray(previousValue) || originalValue.length !== previousValue.length) {
          return false; // Arrays are of different lengths
      }

      // Compare array elements recursively
      for (let i = 0; i < originalValue.length; i++) {
          if (!this.compareValues(originalValue[i], previousValue[i])) {
              return false; // Arrays contain different values
          }
      }
      return true; // Arrays are equal
  } else if (typeof originalValue === 'object' && originalValue !== null) {
      // Compare objects recursively
      const keys1 = Object.keys(originalValue);
      const keys2 = Object.keys(previousValue);
      if (keys1.length !== keys2.length) {
          return false; // Objects have different number of keys
      }

      for (const key of keys1) {
          if (!this.compareValues(originalValue[key], previousValue[key])) {
              return false; // Objects have different values for same keys
          }
      }
      return true; // Objects are equal
  } else {
      // For strings, numbers, and other primitive types, use simple comparison
      return originalValue === previousValue;
  }
}

  handleBtnClick(fieldName) {
    this.individualFieldHistory = []
    Object.keys(this.isHistoryPopup).forEach(key => {
      if (key !== fieldName) {
        this.isHistoryPopup[key] = false;
      }
    });
    this.isHistoryPopup[fieldName] = true;
    this.showSpinner = true;
    if (this.isHistoryPopup[fieldName]) {
      const fieldHistory = this.historyList.find(ele => ele.fieldName === fieldName);
      if (fieldHistory) {
        this.individualFieldHistory = fieldHistory.history;
      }
    }
    this.showSpinner = false;
  }

  onMouseOut(fieldName){
    this.individualFieldHistory = [];
        this.isHistoryPopup[fieldName] = false;
  }

  refreshFieldMapppingValueANDHistory(){
    let obj = {
      "releaseNodeId": this.nodeId || null
    }
    this.http.getFieldMappingsWithHistory(this.selectedToolConfig[0].id,this.kpiId, obj).subscribe(mappings => {
      if (mappings && mappings['success']) {
        this.formData = mappings['data'].fieldMappingResponses;
        this.metaDataTemplateCode = mappings['data'].metaTemplateCode;
        this.ngOnInit();
        this.sharedService.setSelectedFieldMapping(mappings['data']);
      }
    });
  }
}
