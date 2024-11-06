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

import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators, AbstractControl } from '@angular/forms';
import { MessageService, MenuItem } from 'primeng/api';
import { HttpService } from '../../../services/http.service';
import { SharedService } from '../../../services/shared.service';
import { GetAuthorizationService } from '../../../services/get-authorization.service';
import { GoogleAnalyticsService } from '../../../services/google-analytics.service';
import { Router } from '@angular/router';
import { environment } from 'src/environments/environment';
declare const require: any;

@Component({
  selector: 'app-basic-config',
  // changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './basic-config.component.html',
  styleUrls: ['./basic-config.component.css']
})
export class BasicConfigComponent implements OnInit {
  basicConfFormObj: any;
  dataLoading: boolean;
  projectTypeOptions: any = [];
  suggestions: any = [];
  submitted = false;
  selectedType = false;
  selectedProject: any;
  disableSave = false;
  ifSuperUser = false;
  configuredTools: any = [];
  loading = false;
  formData: any;
  getFieldsResponse: any;
  public form: UntypedFormGroup = this.formBuilder.group({});
  blocked = true;
  assigneeSwitchInfo = "Turn ON to retrieve people-related information, such as assignees, developer profiles from all relevant source tools connected to your project";
  developerKpiInfo = "By enabling repo cloning, you consent to clone your code repositories (BitBucket, GitLab, GitHub) to avoid API rate-limiting issues. The repository for this project will be cloned on the KH Server. This will grant access to more valuable KPIs on the Developer dashboard. If cloning is disabled, only 2 KPIs will be accessible";
  isProjectAdmin = false;
  breadcrumbs: Array<any>
  @Output() closeProjectSetupPopup = new EventEmitter();
  steps: MenuItem[] | undefined;
  isProjectSetupPopup: boolean = false;
  isProjectCOmpletionPopup: boolean = false;
  allProjectList: any[];
  selectedItems: { [key: string]: any } = {};
  isSpeedSuite = environment?.['SPEED_SUITE'] ? environment?.['SPEED_SUITE'] : false;

  constructor(private formBuilder: UntypedFormBuilder,
    private sharedService: SharedService,
    private http: HttpService,
    private messenger: MessageService,
    private getAuthorizationService: GetAuthorizationService,
    private ga: GoogleAnalyticsService,
    public router: Router) {
    this.projectTypeOptions = [
      { name: 'Scrum', value: false },
      { name: 'Kanban', value: true }
    ];
  }

  ngOnInit(): void {
    this.isProjectSetupPopup = true;
    this.breadcrumbs = [{ label: 'MY PROJECTS', handleEvent: () => { this.closeProjectSetupPopup.emit() } }, { label: 'ADD NEW PROJECT' }];
    this.steps = [
      {
        label: 'Connect tools',
      },
      {
        label: 'Run processor',
      },
      {
        label: 'Data ready on Dashboard',
      }
    ];
    this.getHierarchy();
    this.ifSuperUser = this.getAuthorizationService.checkIfSuperUser();
    this.selectedProject = this.sharedService.getSelectedProject();
    this.sharedService.setSelectedFieldMapping(null);
    this.isProjectAdmin = this.getAuthorizationService.checkIfProjectAdmin();

    this.allProjectList = this.sharedService.getProjectList();
  }

  getFields() {
    // api call to get formData
    this.blocked = true;
    const formFieldData = JSON.parse(localStorage.getItem('hierarchyData'));
    this.formData = JSON.parse(JSON.stringify(formFieldData));
    this.getFieldsResponse = JSON.parse(JSON.stringify(formFieldData));

    if (Array.isArray(this.formData)) {
      this.formData?.unshift({
        level: 0,
        hierarchyLevelId: 'kanban',
        hierarchyLevelName: 'Project Methodology',
        inputType: 'switch',
        value: false,
        required: true
      });
      this.formData?.push(
        {
          level: this.formData.length,
          hierarchyLevelId: 'assigneeDetails',
          label1: 'Enable People performance KPIs',
          label2: this.assigneeSwitchInfo,
          inputType: 'boolean',
          value: false,
          required: false
        }
      );
      this.formData?.push(
        {
          level: this.formData.length,
          hierarchyLevelId: 'developerKpiEnabled',
          label1: 'Enable Developers KPIs',
          label2: this.developerKpiInfo,
          inputType: 'boolean',
          value: false,
          required: false
        }
      );
      this.formData?.forEach(control => {
        this.form.addControl(
          control.hierarchyLevelId,
          this.formBuilder.control(control.value, [Validators.required, this.stringValidator])
        );
      });
    }
    this.blocked = false;

  }

  search(event, field) {
    const filtered: any[] = [];
    const query = event.query;
    const list = (field.filteredSuggestions && field.filteredSuggestions.length) ? field.filteredSuggestions : field.list;
    for (let i = 0; i < list.length; i++) {
      const listItem = list[i];
      if (listItem?.nodeDisplayName?.toLowerCase().indexOf(query?.toLowerCase()) >= 0) {
        filtered.push(listItem);
      }
    }

    field.filteredSuggestions = filtered;
  }

  onSelectOfDropdown(event: any, currentLevel: any) {
    const selectedItem = event;
    const selectedNodeId = selectedItem.nodeId;
    const selectedParentId = selectedItem.parentId;
    const currentIndex = this.formData.findIndex(level => level === currentLevel);

    // Step 1: Filter current level based on selected item
    // currentLevel.filteredSuggestions = currentLevel.list.filter(item => item.nodeId === selectedNodeId);

    // Step 2: Filter items in levels below based on selected item’s nodeId
    this.filterBelowLevels(selectedNodeId, currentIndex);

    // Step 3: Filter items in levels above based on selected item's parentId
    this.filterAboveLevels(selectedParentId, currentIndex);
  }

  filterAboveLevels(selectedParentId: any, currentIndex: number) {
    let selectParentId = selectedParentId;
    for (let i = currentIndex - 1; i >= 0; i--) {
      if (this.formData[i]?.list) {
        this.formData[i].filteredSuggestions = this.formData[i]?.list.filter(item => item.nodeId === selectParentId);
        selectParentId = this.formData[i]?.filteredSuggestions[0].parentId;
      }
    }

    // Method to prepopulate if only one item exists in list
    this.formData.forEach(level => {
      if (level.filteredSuggestions && level.filteredSuggestions.length === 1) {
        this.selectedItems[level.hierarchyLevelId] = level.filteredSuggestions[0];
      }
    });
  }

  filterBelowLevels(selectedNodeId: string, currentIndex: number) {
    for (let i = currentIndex + 1; i < this.formData.length; i++) {
      if (this.formData[i].list) {
        this.formData[i].filteredSuggestions = this.formData[i].list.filter(item => item.parentId === selectedNodeId);
      }
    }
  }

  onSubmit() {
    let newProjectParentId: string = '';
    const formValue = this.form.getRawValue();
    console.log(formValue);
    const submitData = {};
    submitData['projectName'] = formValue['projectName'];
    submitData['kanban'] = formValue['kanban'];
    submitData['hierarchy'] = [];
    submitData['saveAssigneeDetails'] = formValue['assigneeDetails'];
    submitData['developerKpiEnabled'] = formValue['developerKpiEnabled']
    let gaObj = {
      name: formValue['projectName'],
      kanban: formValue['kanban'],
      saveAssigneeDetails: formValue['assigneeDetails'],
      date: new Date(),
      user_name: this.sharedService.getCurrentUserDetails('user_name'),
      user_email: this.sharedService.getCurrentUserDetails('user_email'),
    }
    this.getFieldsResponse.forEach((element, index) => {
      if (element.hierarchyLevelId === 'project') {
        if (typeof formValue[element.hierarchyLevelId] === 'string') {
          const newProjectName = formValue[element.hierarchyLevelId].trim();
          submitData['projectName'] = newProjectName;
          submitData['projectDisplayName'] = newProjectName;
          submitData['projectNodeId'] = null;
        } else {
          submitData['projectName'] = formValue[element.hierarchyLevelId]?.nodeName;
          submitData['projectDisplayName'] = formValue[element.hierarchyLevelId]?.nodeDisplayName;
          submitData['projectNodeId'] = formValue[element.hierarchyLevelId]?.nodeId;
        }
      }
      submitData['hierarchy'].push({
        hierarchyLevel: {
          level: formValue[element.hierarchyLevelId]?.level,
          hierarchyLevelId: formValue[element.hierarchyLevelId]?.hierarchyLevelId,
          hierarchyLevelName: formValue[element.hierarchyLevelId]?.hierarchyLevelName
        },
        orgHierarchyNodeId: formValue[element.hierarchyLevelId]?.nodeId,
        value: formValue[element.hierarchyLevelId]?.nodeName
      });
      gaObj['category' + (index + 1)] = element.hierarchyLevelId;
    });
    this.blocked = true;
    submitData['hierarchy'].pop();
    this.http.addBasicConfig(submitData).subscribe({
      next: (response: any) => {
        console.log(response)
        if (response && response.serviceResponse && response.serviceResponse.success) {
          this.selectedProject = {};
          this.selectedProject['id'] = response.serviceResponse.data['id'];
          this.selectedProject['name'] = response.serviceResponse.data['projectName'];
          this.selectedProject['Type'] = response.serviceResponse.data['kanban'] ? 'Kanban' : 'Scrum';
          this.selectedProject['saveAssigneeDetails'] = response.serviceResponse.data['saveAssigneeDetails'];
          this.selectedProject['developerKpiEnabled'] = response.serviceResponse.data['developerKpiEnabled'];
          this.selectedProject['projectOnHold'] = response.serviceResponse.data['projectOnHold'];
          response.serviceResponse.data['hierarchy'].forEach(element => {
            this.selectedProject[element.hierarchyLevel.hierarchyLevelName] = element.value;
          });

          this.sharedService.setSelectedProject(this.selectedProject);
          this.allProjectList?.push(this.selectedProject);
          this.sharedService.setProjectList(this.allProjectList);
          if (!this.ifSuperUser) {
            if (response['projectsAccess']) {
              const authorities = response['projectsAccess'].map(projAcc => projAcc.role);
              this.sharedService.setCurrentUserDetails({ authorities });
            }
          }
          this.form.reset();
          this.messenger.add({
            severity: 'success',
            summary: 'Basic config submitted!!',
            detail: ''
          });
          this.isProjectSetupPopup = false;
          this.isProjectCOmpletionPopup = true;

          // Google Analytics
          this.ga.createProjectData(gaObj);
        } else {
          this.messenger.add({
            severity: 'error',
            summary: response.serviceResponse.message && response.serviceResponse.message.length ? response.serviceResponse.message : 'Some error occurred. Please try again later.'
          });
        }
        this.blocked = false;
        this.getFields();
      },
      error: (error) => {
        this.messenger.add({
          severity: 'error',
          summary: 'Some error occurred. Please try again later.'
        });
        this.blocked = false;
      }
    });
  }


  stringValidator(control: AbstractControl): { [key: string]: boolean } | null {
    const inputValue: string = control.value as string;
    if ((typeof control.value === 'string' || control.value instanceof String) && control.value && control.value != null) {
      // no blank spaces, and no value should start with " "
      if (!/^[a-zA-Z0-9][a-zA-Z0-9\s_-]*$/.test(inputValue)) {
        return { stringValidator: true };
      }
    }
    return null;
  }

  getHierarchy() {
    const completeHierarchyData = JSON.parse(localStorage.getItem('completeHierarchyData'));
    const filteredHierarchyData = completeHierarchyData?.scrum.filter(item => item.id);
    const hierarchyMap = filteredHierarchyData?.reduce((acc, item) => {
      acc[item.hierarchyLevelId] = item.hierarchyLevelName;
      return acc;
    }, {});
    if(hierarchyMap) {
      hierarchyMap['project'] = 'Project';
    }
    /* const hierarchyMap = {
        bu: "Business Unit",
        ver: "Vertical",
        acc: "Account",
        port: "Engagement",
        project: "Project"
    }; */
    this.http.getOrganizationHierarchy()?.subscribe(formFieldData => {
      const flatData = formFieldData?.data;

      const transformedData = typeof hierarchyMap === 'object' ? Object.entries(hierarchyMap)?.map(([hierarchyLevelId, hierarchyLevelIdName], index) => {
        return {
          hierarchyLevelId,
          hierarchyLevelIdName,
          level: index + 1,
          list: flatData
            .filter(item => item.hierarchyLevelId === hierarchyLevelId)
            .map(({ id, nodeId, nodeName, nodeDisplayName, hierarchyLevelId, parentId, createdDate, modifiedDate }) => ({
              level: index + 1,
              hierarchyLevelName: hierarchyLevelIdName,
              id,
              nodeId,
              nodeName,
              nodeDisplayName,
              hierarchyLevelId,
              parentId,
              createdDate,
              ...(modifiedDate && { modifiedDate })
            }))
        };
      }) : [];

      localStorage.setItem('hierarchyData', JSON.stringify(transformedData, null, 2));
      this.getFields();
    });
  }

}
