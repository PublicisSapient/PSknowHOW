import { Component, Input, OnInit } from '@angular/core';
import { FormControl, FormGroup, UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { SharedService } from '../../services/shared.service';
import { GetAuthorizationService } from '../../services/get-authorization.service';
import { HttpService } from '../../services/http.service';
import { Router } from '@angular/router';
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
    private getAuthorizationService : GetAuthorizationService,
    private router : Router,
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
    this.fieldMappingSectionList = [...new Set(fieldMappingSections)];
    this.formConfig = fieldMappingConfigration;
    console.log(fieldMappingSections,fieldMappingConfigration);
    
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
    console.log(this.form);
  }

  /** This method is taking config as parameter, creating form control and assigning initial value based on fieldtype */
  generateFromControlBasedOnFieldType(config){
    if(this.formData.hasOwnProperty(config.fieldName)){
      return new FormControl(this.formData[config.fieldName]);
    }else{
      switch(config.fieldType){
        case 'text':
          return new FormControl('');
        case 'radiobutton':
          return new FormControl('');
        default:
          return new FormControl([]);
      }
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
    console.log(submitData);
    
    if(this.selectedToolConfig[0].toolName === 'Jira'){
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
    console.log(this.selectedToolConfig, mappingData);
    
    this.http.setFieldMappings(this.selectedToolConfig[0].id, mappingData).subscribe(response => {
      if (response && response['success']) {
        this.messenger.add({
          severity: 'success',
          summary: 'Field Mappings submitted!!',
        });
        this.uploadedFileName = '';
      } else {
        this.messenger.add({
          severity: 'error',
          summary: 'Some error occurred. Please try again later.'
        });
      }
    });
  }

  export() {
    if (this.form.invalid) {
      return;
    }

    const submitData = {};
    for (const obj in this.form.value) {
      submitData[obj] = this.form.value[obj];
    }
    // this.handleAdditionalFilters(submitData);

    this.dyanmicDownloadByHtmlTag({
      fileName: 'mappings.json',
      text: JSON.stringify(submitData)
    });
  }

  handleAdditionalFilters(submitData: any): any {
    /** addiitional filters start*/
    const additionalFilters = this.filterHierarchy.filter((filter) => filter.level > this.filterHierarchy.filter(f => f.hierarchyLevelId === 'sprint')[0].level);
    // modify submitData
    submitData['additionalFilterConfig'] = [];
    additionalFilters.forEach(element => {
      if (submitData[element.hierarchyLevelId + 'Identifier'] && submitData[element.hierarchyLevelId + 'Identifier'].length) {
        const additionalFilterObj = {};
        additionalFilterObj['filterId'] = element.hierarchyLevelId;
        additionalFilterObj['identifyFrom'] = submitData[element.hierarchyLevelId + 'Identifier'];
        if (additionalFilterObj['identifyFrom'] === 'CustomField') {
          additionalFilterObj['identificationField'] = submitData[element.hierarchyLevelId + 'IdentSingleValue'];
          additionalFilterObj['values'] = [];
        } else {
          additionalFilterObj['identificationField'] = '';
          additionalFilterObj['values'] = submitData[element.hierarchyLevelId + 'IdentMultiValue'] ? submitData[element.hierarchyLevelId + 'IdentMultiValue'] : [];
        }
        submitData['additionalFilterConfig'].push(additionalFilterObj);
      }
      delete submitData[element.hierarchyLevelId + 'Identifier'];
      delete submitData[element.hierarchyLevelId + 'IdentSingleValue'];
      delete submitData[element.hierarchyLevelId + 'IdentMultiValue'];
    });
    return submitData;
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
