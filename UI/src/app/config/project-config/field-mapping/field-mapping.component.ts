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
  fieldMappingMetaData: any[];
  selectedField = '';
  bodyScrollPosition = 0;
  selectedToolConfig: any = {};
  selectedFieldMapping: any = {};
  disableSave = false;
  populateDropdowns = true;
  uploadedFileName = '';
  fieldMappingConfig = [];
  @ViewChild('fieldMappingFormComp') fieldMappingFormComp : FieldMappingFormComponent;


  private setting = {
    element: {
      dynamicDownload: null as HTMLElement
    }
  };

  constructor(private formBuilder: UntypedFormBuilder, private router: Router, private sharedService: SharedService,
    private http: HttpService, private messenger: MessageService, private getAuthorizationService: GetAuthorizationService,private confirmationService: ConfirmationService) { }

  ngOnInit(): void {

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
    const finalMappingURL = this.selectedConfig?.Type?.toLowerCase() === 'kanban' ? `${this.selectedConfig.id}/kpi1` : `${this.selectedConfig.id}/kpi0`
    this.http.getKPIFieldMappingConfig(finalMappingURL).subscribe(response => {
      if(response && response['success']){
        this.fieldMappingConfig = response?.data.fieldConfiguration;
      }
    });
  }

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

  onUpload(event) {
    this.uploadedFileName = event.target.files[0].name;
    const fileReader = new FileReader();
    fileReader.readAsText(event.target.files[0], 'UTF-8');
    fileReader.onload = () => {
      const mappingData = JSON.parse(fileReader.result as string);
      this.sharedService.setSelectedFieldMapping(mappingData);
      this.fieldMappingFormComp?.setControlValueOnImport(mappingData);
    };
    fileReader.onerror = (error) => {
      console.log(error);
    };
  };


  export() {
    this.http.getFieldMappings(this.selectedToolConfig[0].id).subscribe(resp=>{
      this.dyanmicDownloadByHtmlTag({
        fileName: 'mappings.json',
        text: JSON.stringify(resp['data'])
      });
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
