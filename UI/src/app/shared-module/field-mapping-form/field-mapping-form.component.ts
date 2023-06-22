import { Component, OnInit } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
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

  fieldMappingForm: UntypedFormGroup;
  selectedConfig: any = {};
  disableSave = false;
  selectedToolConfig: any = {};
  fieldMappingMetaData: any = [];
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

  constructor(private sharedService : SharedService,
    private getAuthorizationService : GetAuthorizationService,
    private router : Router,
    private http : HttpService,
    private messenger: MessageService,
    private confirmationService: ConfirmationService) { }

  ngOnInit(): void {
    /** Getting Project details */
    //  if (this.sharedService.getSelectedProject()) {
    //   this.selectedConfig = this.sharedService.getSelectedProject();
    //   this.disableSave = this.getAuthorizationService.checkIfViewer(this.selectedConfig);
    // } else {
    //   this.router.navigate(['./dashboard/Config/ProjectList']);
    // }

    /** Getting Tool details */
    // if (this.sharedService.getSelectedToolConfig()) {
    //   this.selectedToolConfig = this.sharedService.getSelectedToolConfig().filter(tool => tool.toolName === 'Jira' || tool.toolName === 'Azure');
    //   if (!this.selectedToolConfig || !this.selectedToolConfig.length) {
    //     this.router.navigate(['./dashboard/Config/ProjectList']);
    //   } else {
    //     this.getDropdownData();
    //   }
    // }
  }

   resetRadioButton(fieldName){
    this.fieldMappingForm.patchValue({[fieldName]: ''});
  }

  /** Getting meta data for search */
  getDropdownData() {
    if (this.selectedToolConfig && this.selectedToolConfig.length && this.selectedToolConfig[0].id) {
      this.http.getKPIConfigMetadata(this.selectedToolConfig[0].id).subscribe(Response => {
        if (Response.success) {
          this.fieldMappingMetaData = Response.data;
        } else {
          this.fieldMappingMetaData = [];
        }
      });
    }
  }

  /** once user willl click on search btn, assign the search options based on field category */
  showDialogToAddValue(isSingle, fieldName, type) {
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
      if (this.fieldMappingForm.controls[this.selectedField].value) {
        this.selectedValue = this.fieldMappingMultiSelectValues.filter(fieldMappingMultiSelectValue => (fieldMappingMultiSelectValue.data === this.fieldMappingForm.controls[this.selectedField].value));
        if (this.selectedValue && this.selectedValue.length) {
          if (this.selectedValue[0].data) {
            this.selectedValue = this.selectedValue[0].data;
          }
        }
      }
    } else {
      if (this.fieldMappingForm.controls[this.selectedField].value) {
        this.selectedMultiValue = this.fieldMappingMultiSelectValues.filter(fieldMappingMultiSelectValue => (this.fieldMappingForm.controls[this.selectedField].value).includes(fieldMappingMultiSelectValue.data));
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
        this.fieldMappingForm.controls[this.selectedField].setValue(this.selectedValue);
      }
    } else {
      const selectedMultiValueLabels = [];
      if (this.selectedMultiValue.length) {
        if (this.fieldMappingForm.controls[this.selectedField].value) {
          for (const index in this.selectedMultiValue) {
            selectedMultiValueLabels.push(this.selectedMultiValue[index].key);
          }
          const allMultiValueLabels = [];
          for (const index in this.fieldMappingMultiSelectValues) {
            allMultiValueLabels.push(this.fieldMappingMultiSelectValues[index].key);
          }

          if (!selectedMultiValueLabels.includes(this.fieldMappingForm.controls[this.selectedField].value)) {
            for (const selectedFieldIndex in this.fieldMappingForm.controls[this.selectedField].value) {
              if (!allMultiValueLabels.includes(this.fieldMappingForm.controls[this.selectedField].value[selectedFieldIndex])) {
                selectedMultiValueLabels.push(this.fieldMappingForm.controls[this.selectedField].value[selectedFieldIndex]);
              }
            }
          }

        }
      }

      this.fieldMappingForm.controls[this.selectedField].setValue(Array.from(new Set(selectedMultiValueLabels)));
    }
    this.populateDropdowns = false;
    this.displayDialog = false;
  }

  /** Responsible for fetchj field value from shared service */
  getMappings() {
    this.selectedFieldMapping = this.sharedService.getSelectedFieldMapping();
    if (this.selectedFieldMapping && Object.keys(this.selectedFieldMapping).length) {
      for (const obj in this.selectedFieldMapping) {
        if (this.fieldMappingForm && this.fieldMappingForm.controls[obj]) {
          this.fieldMappingForm.controls[obj].setValue(this.selectedFieldMapping[obj]);
        }
      }
    }
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
  
    let submitData = {};
    submitData['basicProjectConfigId'] = this.selectedConfig.id;

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
    this.http.setFieldMappings(this.selectedToolConfig[0].id, mappingData).subscribe(Response => {
      if (Response && Response['success']) {
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

}
