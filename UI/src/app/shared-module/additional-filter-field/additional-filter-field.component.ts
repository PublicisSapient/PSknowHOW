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
import { Component, EventEmitter, OnInit, Output,Input } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup } from '@angular/forms';
import { MessageService } from 'primeng/api';
import { SharedService } from 'src/app/services/shared.service';

@Component({
  selector: 'app-additional-filter-field',
  templateUrl: './additional-filter-field.component.html',
  styleUrls: ['./additional-filter-field.component.css']
})
export class AdditionalFilterFieldComponent implements OnInit {
  @Output() additionalFilterChange = new EventEmitter();
  @Input() fieldMappingMetaData ;
  additionalFilterIdentifier: any = {};
  disableAdditionalFilterAdd =true;
  additionalFiltersArray: any = [];
  additionalFilterIdentificationOptions: any = [];
  fieldMappingForm = new UntypedFormGroup({});
  filterHierarchy: any = [];
  selectedFieldMapping: any = {};
  additionalFilterOptions: any = [];
  additionalFilterConfig = [];
  
  populateDropdowns = true;
  selectedField = '';
  singleSelectionDropdown = false;
  fieldMappingMultiSelectValues: any = [];
  selectedValue = [];
  selectedMultiValue = [];
  displayDialog = false;
  bodyScrollPosition = 0;

  constructor(private formBuilder: UntypedFormBuilder,private messenger: MessageService,private sharedService: SharedService) { }

  ngOnInit(): void {
    this.filterHierarchy = JSON.parse(localStorage.getItem('completeHierarchyData'))?.scrum;
    this.additionalFilterIdentificationOptions = [
      {
        label: 'Component',
        value: 'Component'
      },
      {
        label: 'CustomField',
        value: 'CustomField'
      },
      {
        label: 'Labels',
        value: 'Labels'
      }
    ];
   this.generateAdditionalFilterMappings();
  }


  generateAdditionalFilterMappings() {
    this.addAdditionalFilterOptions();
    this.selectedFieldMapping = this.sharedService.getSelectedFieldMapping();
    if (this.selectedFieldMapping) {
      const additionalFilterMappings = this.selectedFieldMapping.additionalFilterConfig;
      this.additionalFiltersArray = [];

      const additionalFilters = this.filterHierarchy?.filter((filter) => filter.level > this.filterHierarchy?.filter(f => f.hierarchyLevelId === 'sprint')[0].level);

      additionalFilterMappings?.forEach(element => {

        this.additionalFiltersArray.push({
          name: additionalFilters.filter((f) => f.hierarchyLevelId === element.filterId)[0].hierarchyLevelName,
          code: element.filterId
        });

        if (element['identifyFrom'] && element['identifyFrom'].length) {
          if (!this.fieldMappingForm.controls[element.filterId + 'Identifier']) {
            this.fieldMappingForm.addControl(element.filterId + 'Identifier', this.formBuilder.control(''));
            this.fieldMappingForm.controls[element.filterId + 'Identifier'].setValue(element['identifyFrom']);
          }
        }
        if (element['identifyFrom'] === 'CustomField') {
          if (!this.fieldMappingForm.controls[element.filterId + 'IdentSingleValue']) {
            this.fieldMappingForm.addControl(element.filterId + 'IdentSingleValue', this.formBuilder.control(''));
            this.fieldMappingForm.controls[element.filterId + 'IdentSingleValue'].setValue(element['identificationField']);
          }
        } else {
          if (!this.fieldMappingForm.controls[element.filterId + 'IdentMultiValue']) {
            this.fieldMappingForm.addControl(element.filterId + 'IdentMultiValue', this.formBuilder.control(''));
            this.fieldMappingForm.controls[element.filterId + 'IdentMultiValue'].setValue(element['values']);
          }
        }
      });

    }
  }

  get fieldMapping() {
    return this.fieldMappingForm.controls;
  }

  addAdditionalFilterOptions() {
    this.additionalFilterOptions = [];
    const additionalFilters = this.filterHierarchy?.filter((filter) => filter.level > this.filterHierarchy?.filter(f => f.hierarchyLevelId === 'sprint')[0]?.level);
    additionalFilters?.forEach(element => {
      this.additionalFilterOptions.push({
        name: element.hierarchyLevelName,
        code: element.hierarchyLevelId
      });
    });
  }

  addAdditionalFilterMappings() {
    if (!this.additionalFiltersArray.filter((filter) => filter.name === this.additionalFilterIdentifier.name).length) {
      this.additionalFiltersArray.push(this.additionalFilterIdentifier);

      this.additionalFiltersArray?.forEach(element => {
        if (!this.fieldMappingForm.controls[element.code + 'Identifier']) {
          this.fieldMappingForm.addControl(element.code + 'Identifier', this.formBuilder.control(''));
        }
      });
    } else {
      this.messenger.add({
        severity: 'error',
        summary: `Mappings for ${this.additionalFilterIdentifier.name} already exist!!!`
      });
    }
    this.handleAdditionalFilters();
  }

  changeControl(event, additionalFilterIdentifier) {
    if (event.value === 'Component' || event.value === 'Labels') {
      if (!this.fieldMappingForm.controls[additionalFilterIdentifier.code + 'IdentMultiValue']) {
        this.fieldMappingForm.addControl(additionalFilterIdentifier.code + 'IdentMultiValue', this.formBuilder.control(''));
      }

    } else {
      if (!this.fieldMappingForm.controls[additionalFilterIdentifier.code + 'IdentSingleValue']) {
        this.fieldMappingForm.addControl(additionalFilterIdentifier.code + 'IdentSingleValue', this.formBuilder.control(''));
      }
    }
    this.handleAdditionalFilters();
  }

  removeAdditionFilterMapping(filter) {
    if (this.fieldMappingForm.controls[filter.code + 'Identifier']) {
      this.fieldMappingForm.removeControl(filter.code + 'Identifier');
    }
    if (this.fieldMappingForm.controls[filter.code + 'IdentMultiValue']) {
      this.fieldMappingForm.removeControl(filter.code + 'IdentMultiValue');
    }
    if (this.fieldMappingForm.controls[filter.code + 'IdentSingleValue']) {
      this.fieldMappingForm.removeControl(filter.code + 'IdentSingleValue');
    }
    this.additionalFiltersArray = this.additionalFiltersArray.filter((f) => f.code !== filter.code);
    this.handleAdditionalFilters();
  }

handleAdditionalFilters(): any {
  const submitData = this.fieldMappingForm.value;
  const additionalFilters = this.filterHierarchy?.filter((filter) => filter.level > this.filterHierarchy?.filter(f => f.hierarchyLevelId === 'sprint')[0]?.level);
  this.additionalFilterConfig = [];
  additionalFilters?.forEach(element => {
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
      this.additionalFilterConfig.push(additionalFilterObj);
    }
    delete submitData[element.hierarchyLevelId + 'Identifier'];
    delete submitData[element.hierarchyLevelId + 'IdentSingleValue'];
    delete submitData[element.hierarchyLevelId + 'IdentMultiValue'];
  });
  this.additionalFilterChange.emit(this.additionalFilterConfig);

}

resetRadioButton(fieldName){
  this.fieldMappingForm.patchValue({[fieldName]: ''});
  this.handleAdditionalFilters();
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
    } 
    this.handleAdditionalFilters()
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

}
