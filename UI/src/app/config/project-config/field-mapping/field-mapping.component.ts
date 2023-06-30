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

import { Component, OnInit, ViewChild } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MessageService,ConfirmationService } from 'primeng/api';
import { HttpService } from '../../../services/http.service';
import { SharedService } from '../../../services/shared.service';
import { GetAuthorizationService } from '../../../services/get-authorization.service';
import { ChangeDetectionStrategy } from '@angular/core';
import { FieldMappingFormComponent } from 'src/app/shared-module/field-mapping-form/field-mapping-form.component';
declare const require: any;

@Component({
  selector: 'app-field-mapping',
  templateUrl: './field-mapping.component.html',
  styleUrls: ['./field-mapping.component.css'],
  //changeDetection: ChangeDetectionStrategy.OnPush
})
export class FieldMappingComponent implements OnInit {
  fieldMappingForm: UntypedFormGroup;
  fieldMappingFormObj: any;
  selectedConfig: any = {};
  selectedPriority: any = [];
  fieldMappingSubmitted = false;
  singleSelectionDropdown = false;
  fieldMappingMetaData: any[];
  selectedField = '';
  bodyScrollPosition = 0;
  selectedToolConfig: any = {};
  selectedFieldMapping: any = {};
  disableSave = false;
  populateDropdowns = true;
  uploadedFileName = '';
  fieldMappingConfig = [];
 

  // additional filters
  filterHierarchy: any = [];
  additionalFilterIdentifier: any = {};
  additionalFiltersArray: any = [];
  additionalFilterOptions: any = [];
  @ViewChild('fieldMappingFormComp') fieldMappingFormComp : FieldMappingFormComponent;


  private setting = {
    element: {
      dynamicDownload: null as HTMLElement
    }
  };



  constructor(private formBuilder: UntypedFormBuilder, private router: Router, private sharedService: SharedService,
    private http: HttpService, private messenger: MessageService, private getAuthorizationService: GetAuthorizationService,private confirmationService: ConfirmationService) { }

  ngOnInit(): void {

    this.filterHierarchy = JSON.parse(localStorage.getItem('completeHierarchyData')).scrum;

    if (this.sharedService.getSelectedProject()) {
      this.selectedConfig = this.sharedService.getSelectedProject();
      this.disableSave = this.getAuthorizationService.checkIfViewer(this.selectedConfig);
    } else {
      this.router.navigate(['./dashboard/Config/ProjectList']);
    }

    if (this.sharedService.getSelectedToolConfig()) {
      this.selectedToolConfig = this.sharedService.getSelectedToolConfig().filter(tool => tool.toolName === 'Jira' || tool.toolName === 'Azure');
      if (!this.selectedToolConfig || !this.selectedToolConfig.length) {
        this.router.navigate(['./dashboard/Config/ProjectList']);
      } else {
        this.getDropdownData();
      }
    }
    this.getMappings();
    this.getKPIFieldMappingRelationships();
  }

  getMappings() {
    this.selectedFieldMapping = this.sharedService.getSelectedFieldMapping();
    if (this.selectedFieldMapping && Object.keys(this.selectedFieldMapping).length) {
      for (const obj in this.selectedFieldMapping) {
        if (this.fieldMappingForm && this.fieldMappingForm.controls[obj]) {
          this.fieldMappingForm.controls[obj].setValue(this.selectedFieldMapping[obj]);
        }
      }
      // this.generateAdditionalFilterMappings();
    }
  }

  getKPIFieldMappingRelationships() {
    this.http.getKPIFieldMappingConfig(`${this.selectedConfig.id}/kpi0`).subscribe(response => {
      if(response?.fieldConfiguration){
        this.fieldMappingConfig = response.fieldConfiguration;
      }
    });
  }

  // generateAdditionalFilterMappings() {
  //   this.addAdditionalFilterOptions();
  //   this.selectedFieldMapping = this.sharedService.getSelectedFieldMapping();
  //   if (this.selectedFieldMapping) {
  //     const additionalFilterMappings = this.selectedFieldMapping.additionalFilterConfig;
  //     this.additionalFiltersArray = [];

  //     const additionalFilters = this.filterHierarchy.filter((filter) => filter.level > this.filterHierarchy.filter(f => f.hierarchyLevelId === 'sprint')[0].level);

  //     additionalFilterMappings?.forEach(element => {

  //       this.additionalFiltersArray.push({
  //         name: additionalFilters.filter((f) => f.hierarchyLevelId === element.filterId)[0].hierarchyLevelName,
  //         code: element.filterId
  //       });

  //       if (element['identifyFrom'] && element['identifyFrom'].length) {
  //         if (!this.fieldMappingForm.controls[element.filterId + 'Identifier']) {
  //           this.fieldMappingForm.addControl(element.filterId + 'Identifier', this.formBuilder.control(''));
  //           this.fieldMappingForm.controls[element.filterId + 'Identifier'].setValue(element['identifyFrom']);
  //         }
  //       }
  //       if (element['identifyFrom'] === 'CustomField') {
  //         if (!this.fieldMappingForm.controls[element.filterId + 'IdentSingleValue']) {
  //           this.fieldMappingForm.addControl(element.filterId + 'IdentSingleValue', this.formBuilder.control(''));
  //           this.fieldMappingForm.controls[element.filterId + 'IdentSingleValue'].setValue(element['identificationField']);
  //         }
  //       } else {
  //         if (!this.fieldMappingForm.controls[element.filterId + 'IdentMultiValue']) {
  //           this.fieldMappingForm.addControl(element.filterId + 'IdentMultiValue', this.formBuilder.control(''));
  //           this.fieldMappingForm.controls[element.filterId + 'IdentMultiValue'].setValue(element['values']);
  //         }
  //       }
  //     });

  //     for (const obj in this.selectedFieldMapping) {
  //       if (this.fieldMappingForm && this.fieldMappingForm.controls[obj]) {
  //         this.fieldMappingForm.controls[obj].setValue(this.selectedFieldMapping[obj]);
  //       }
  //     }
  //   }
  // }

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

  // get fieldMapping() {
  //   return this.fieldMappingForm.controls;
  // }
  
  // addAdditionalFilterOptions() {
  //   this.additionalFilterOptions = [];
  //   const additionalFilters = this.filterHierarchy.filter((filter) => filter.level > this.filterHierarchy.filter(f => f.hierarchyLevelId === 'sprint')[0].level);
  //   additionalFilters.forEach(element => {
  //     this.additionalFilterOptions.push({
  //       name: element.hierarchyLevelName,
  //       code: element.hierarchyLevelId
  //     });
  //   });

  // }

  // addAdditionalFilterMappings() {
  //   if (!this.additionalFiltersArray.filter((filter) => filter.name === this.additionalFilterIdentifier.name).length) {
  //     this.additionalFiltersArray.push(this.additionalFilterIdentifier);

  //     this.additionalFiltersArray.forEach(element => {
  //       if (!this.fieldMappingForm.controls[element.code + 'Identifier']) {
  //         this.fieldMappingForm.addControl(element.code + 'Identifier', this.formBuilder.control(''));
  //       }
  //     });
  //   } else {
  //     this.messenger.add({
  //       severity: 'error',
  //       summary: `Mappings for ${this.additionalFilterIdentifier.name} already exist!!!`
  //     });
  //   }
  // }

  // changeControl(event,additionalFilterIdentifier) {
  //   if (event.value === 'Component' || event.value === 'Labels') {
  //     if (!this.fieldMappingForm.controls[additionalFilterIdentifier.code + 'IdentMultiValue']) {
  //       this.fieldMappingForm.addControl(additionalFilterIdentifier.code + 'IdentMultiValue', this.formBuilder.control(''));
  //     }

  //   } else {
  //     if (!this.fieldMappingForm.controls[additionalFilterIdentifier.code + 'IdentSingleValue']) {
  //       this.fieldMappingForm.addControl(additionalFilterIdentifier.code + 'IdentSingleValue', this.formBuilder.control(''));
  //     }
  //   }
  // }

  // removeAdditionFilterMapping(filter) {
  //   if (this.fieldMappingForm.controls[filter.code + 'Identifier']) {
  //     this.fieldMappingForm.removeControl(filter.code + 'Identifier');
  //   }
  //   if (this.fieldMappingForm.controls[filter.code + 'IdentMultiValue']) {
  //     this.fieldMappingForm.removeControl(filter.code + 'IdentMultiValue');
  //   }
  //   if (this.fieldMappingForm.controls[filter.code + 'IdentSingleValue']) {
  //     this.fieldMappingForm.removeControl(filter.code + 'IdentSingleValue');
  //   }
  //   this.additionalFiltersArray = this.additionalFiltersArray.filter((f) => f.code !== filter.code);
  // }

  // validate any field in config by a Regular expression
  // validateRegexp(e, regexp, singleChar = false) {
  // }



  // recordScrollPosition() {
  //   this.bodyScrollPosition = document.documentElement.scrollTop;
  // }

  // scrollToPosition() {
  //   this.populateDropdowns = false;
  //   document.documentElement.scrollTop = this.bodyScrollPosition;
  // }

  // save() {
  //   this.fieldMappingSubmitted = true;
  //   // return if form is invalid
  //   if (this.fieldMappingForm.invalid) {
  //     return;
  //   }

  //   let submitData = {};
  //   for (const obj in this.fieldMapping) {
  //     submitData[obj] = this.fieldMapping[obj].value;
  //   }

  //   submitData['basicProjectConfigId'] = this.selectedConfig.id;

  //   submitData = this.handleAdditionalFilters(submitData);

  //   if(this.selectedToolConfig[0].toolName === 'Jira'){
  //     this.http.getMappingTemplateFlag(this.selectedToolConfig[0].id, submitData).subscribe(response => {
  //       if (response && response['success']) {
  //         if (response['data']) {
  //           this.confirmationService.confirm({
  //             message: `Please note that change in mappings is a deviation from initially configured template.
  //             If you continue with the change in mappings then these changes will be mapped to a
  //             Custom template in project configurations which cannot be changed again to a initially configured template.`,
  //             header: 'Template Change Info',
  //             key: 'templateInfoDialog',
  //             accept: () => {
  //               this.saveFieldMapping(submitData);
  //             },
  //             reject: () => {}
  //           });
  //         } else {
  //         this.saveFieldMapping(submitData);
  //         }
  //       }else{
  //         this.messenger.add({
  //           severity: 'error',
  //           summary: 'Some error occurred. Please try again later.'
  //         });
  //       }

  //     });
  //   }else{
  //     this.saveFieldMapping(submitData);
  //   }

  // }

  onUpload(event) {
    this.uploadedFileName = event.target.files[0].name;
    const fileReader = new FileReader();
    fileReader.readAsText(event.target.files[0], 'UTF-8');
    fileReader.onload = () => {
      const mappingData = JSON.parse(fileReader.result as string);
      this.sharedService.setSelectedFieldMapping(mappingData);
      this.fieldMappingFormComp.setControlValueOnImport(mappingData);
    };
    fileReader.onerror = (error) => {
      console.log(error);
    };
  };

  // onSelectPriority(event: any) {

  //   // if (event == '') {
  //   //   this.selectedPriority.map(p =>{
  //   //     return this.selectedPriority.push(p.label)}
  //   //   ).join(',');
  //   // } else {
  //   if (this.selectedPriority.includes(event.value)) { // remove
  //     this.selectedPriority.push(event.value); // add

  //   }
  // }

  export() {
    this.http.getFieldMappings(this.selectedToolConfig[0].id).subscribe(resp=>{
      this.dyanmicDownloadByHtmlTag({
        fileName: 'mappings.json',
        text: JSON.stringify(resp['data'])
      });
    })


    
  }


  // handleAdditionalFilters(submitData: any): any {
  //   /** addiitional filters start*/
  //   const additionalFilters = this.filterHierarchy.filter((filter) => filter.level > this.filterHierarchy.filter(f => f.hierarchyLevelId === 'sprint')[0].level);
  //   // modify submitData
  //   submitData['additionalFilterConfig'] = [];
  //   additionalFilters.forEach(element => {
  //     if (submitData[element.hierarchyLevelId + 'Identifier'] && submitData[element.hierarchyLevelId + 'Identifier'].length) {
  //       const additionalFilterObj = {};
  //       additionalFilterObj['filterId'] = element.hierarchyLevelId;
  //       additionalFilterObj['identifyFrom'] = submitData[element.hierarchyLevelId + 'Identifier'];
  //       if (additionalFilterObj['identifyFrom'] === 'CustomField') {
  //         additionalFilterObj['identificationField'] = submitData[element.hierarchyLevelId + 'IdentSingleValue'];
  //         additionalFilterObj['values'] = [];
  //       } else {
  //         additionalFilterObj['identificationField'] = '';
  //         additionalFilterObj['values'] = submitData[element.hierarchyLevelId + 'IdentMultiValue'] ? submitData[element.hierarchyLevelId + 'IdentMultiValue'] : [];
  //       }
  //       submitData['additionalFilterConfig'].push(additionalFilterObj);
  //     }
  //     delete submitData[element.hierarchyLevelId + 'Identifier'];
  //     delete submitData[element.hierarchyLevelId + 'IdentSingleValue'];
  //     delete submitData[element.hierarchyLevelId + 'IdentMultiValue'];
  //   });
  //   return submitData;
  // }

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

  // saveFieldMapping(mappingData) {
  //   this.http.setFieldMappings(this.selectedToolConfig[0].id, mappingData).subscribe(Response => {
  //     if (Response && Response['success']) {
  //       this.messenger.add({
  //         severity: 'success',
  //         summary: 'Field Mappings submitted!!',
  //       });
  //       this.uploadedFileName = '';
  //     } else {
  //       this.messenger.add({
  //         severity: 'error',
  //         summary: 'Some error occurred. Please try again later.'
  //       });
  //     }
  //   });
  // }
}
