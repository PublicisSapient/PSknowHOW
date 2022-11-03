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

import { Component, OnInit } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { HttpService } from '../../../services/http.service';
import { SharedService } from '../../../services/shared.service';
import { GetAuthorizationService } from '../../../services/get-authorization.service';
import { ChangeDetectionStrategy } from '@angular/core';
declare const require: any;

@Component({
  selector: 'app-field-mapping',
  templateUrl: './field-mapping.component.html',
  styleUrls: ['./field-mapping.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class FieldMappingComponent implements OnInit {
  fieldMappingForm: UntypedFormGroup;
  fieldMappingFormObj: any;
  fieldMappingFormDescriptionObj = [];
  selectedConfig: any = {};
  fieldMappingMultiSelectValues: any = [];
  techDebtIdentification: any = [];
  additionalFilterIdentificationOptions: any = [];
  estimationCriteriaTypes: any = [];
  defectIdentification: any = [];
  selectedPriority: any = [];
  isZephyr = false;
  fieldMappingSubmitted = false;
  singleSelectionDropdown = false;
  displayDialog = false;
  fieldMappingMetaData: any = [];
  dropdownSettingsMulti = {};
  dropdownSettingsSingle = {};
  selectedValue = [];
  selectedMultiValue = [];
  selectedField = '';
  bodyScrollPosition = 0;
  selectedToolConfig: any = {};
  selectedFieldMapping: any = {};
  disableSave = false;
  populateDropdowns = true;
  uploadedFileName = '';
  productionDefectIdentificationOptions: any = [];
  testCaseIdentification: any = [];

  // additional filters
  filterHierarchy: any = [];
  additionalFilterIdentifier: any = {};
  additionalFiltersArray: any = [];
  additionalFilterOptions: any = [];
  // kpi to field mapping relationships
  kpiRelationShips: any = [];

  private setting = {
    element: {
      dynamicDownload: null as HTMLElement
    }
  };

  constructor(private formBuilder: UntypedFormBuilder, private router: Router, private sharedService: SharedService,
    private http: HttpService, private messenger: MessageService, private getAuthorizationService: GetAuthorizationService) { }

  ngOnInit(): void {
    this.techDebtIdentification = [
      {
        label: 'Select',
        value: ''
      },
      {
        label: 'CustomField',
        value: 'CustomField'
      },
      {
        label: 'Labels',
        value: 'Labels'
      },
      {
        label: 'IssueType',
        value: 'IssueType'
      }
    ];
    this.additionalFilterIdentificationOptions = [
      {
        label: 'Select',
        value: ''
      },
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
    this.estimationCriteriaTypes = [
      {
        label: 'Select',
        value: ''
      },
      {
        label: 'Story Point',
        value: 'Story Point'
      },
      {
        label: 'Actual Estimation',
        value: 'Actual Estimation'
      },
      {
        label: 'Buffered Estimation',
        value: 'Buffered Estimation'
      }
    ];
    this.defectIdentification = [
      {
        label: 'Select',
        value: ''
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
    this.productionDefectIdentificationOptions = [
      {
        label: 'Select',
        value: ''
      },
      {
        label: 'CustomField',
        value: 'CustomField'
      },
      {
        label: 'Labels',
        value: 'Labels'
      },
      {
        label: 'Component',
        value: 'Component'
      }
    ];
    this.selectedPriority = [
      {
        label: 'p1',
        value: 'p1'
      },
      {
        label: 'p2',
        value: 'p2'
      },
      {
        label: 'p3',
        value: 'p3'
      },
      {
        label: 'p4',
        value: 'p4'
      },
      {
        label: 'p5',
        value: 'p5'
      }
    ];
    this.testCaseIdentification = [
      {
        label: 'Select',
        value: ''
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

    this.filterHierarchy = JSON.parse(localStorage.getItem('completeHierarchyData')).scrum;


    if (this.sharedService.getSelectedProject()) {
      this.selectedConfig = this.sharedService.getSelectedProject();
      this.disableSave = this.getAuthorizationService.checkIfViewer(this.selectedConfig);
    } else {
      this.router.navigate(['./dashboard/Config/ProjectList']);
    }

    this.initializeFields();
    this.fieldMappingForm = this.formBuilder.group(this.fieldMappingFormObj);
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

      this.generateAdditionalFilterMappings();
    }
  }

  getKPIFieldMappingRelationships() {
    this.kpiRelationShips = [
      // Scrum
      {
        kpiId: 'kpi14',
        kpiName: 'Defect Injection Rate',
        type: ['Scrum'],
        fieldNames: ['jiraDod', 'jiraDefectInjectionIssueType', 'jiraDefectCreatedStatus', 'jiraDefectDroppedStatus']
      },
      {
        kpiId: 'kpi82',
        kpiName: 'First Time Pass Rate',
        type: ['Scrum'],
        fieldNames: ['resolutionTypeForRejection', 'jiraStoryIdentification', 'JiraIssueDeliverdStatus', 'defectPriority', 'ExcludeRCAFromFTPR']
      },
      {
        kpiId: 'kpi111',
        kpiName: 'Defect Density',
        type: ['Scrum'],
        fieldNames: ['jiraQADefectDensityIssueType', 'jiraDod']
      },
      {
        kpiId: 'kpi35',
        kpiName: 'Defect Seepage Rate',
        type: ['Scrum'],
        fieldNames: ['jiraDefectSeepageIssueType', 'jiraDefectDroppedStatus']
      },
      {
        kpiId: 'kpi34',
        kpiName: 'Defect Removal Efficiency',
        type: ['Scrum'],
        fieldNames: ['jiraDefectRemovalStatus', 'jiraDefectRemovalIssueType']
      },
      {
        kpiId: 'kpi37',
        kpiName: 'Defect Rejection Rate',
        type: ['Scrum'],
        fieldNames: ['jiraDefectRejectionStatus', 'resolutionTypeForRejection', 'jiraDefectRejectionlIssueType']
      },
      {
        kpiId: 'kpi28',
        kpiName: 'Defect Count By Priority',
        type: ['Scrum'],
        fieldNames: ['jiraDefectCountlIssueType', 'jiraDefectDroppedStatus']
      },
      {
        kpiId: 'kpi36',
        kpiName: 'Defect Count By RCA',
        type: ['Scrum'],
        fieldNames: ['jiraDefectCountlIssueType', 'jiraDefectDroppedStatus']
      },
      {
        kpiId: 'kpi126',
        type: ['Scrum'],
        kpiName: 'Created vs Resolved defects',
        fieldNames: []
      },
      {
        kpiId: 'kpi42',
        kpiName: 'Regression Automation Coverage',
        type: ['Scrum'],
        fieldNames: ['jiraRegressionTestValue', 'testRegressionValue', 'regressionAutomationFolderPath']
      },
      {
        kpiId: 'kpi16',
        kpiName: 'In-Sprint Automation Coverage',
        type: ['Scrum'],
        fieldNames: ['jiraRegressionTestValue', 'testRegressionValue', 'regressionAutomationFolderPath']
      },
      {
        kpiId: 'kpi17',
        type: ['Scrum'],
        kpiName: 'Unit Test Coverage',
        fieldNames: []
      },
      {
        kpiId: 'kpi38',
        kpiName: 'Sonar Violations',
        type: ['Scrum'],
        fieldNames: []
      },
      {
        kpiId: 'kpi27',
        kpiName: 'Sonar Tech Debt',
        type: ['Scrum'],
        fieldNames: []
      },
      {
        kpiId: 'kpi116',
        kpiName: 'Change Failure Rate',
        type: ['Scrum'],
        fieldNames: []
      },
      {
        kpiId: 'kpi70',
        kpiName: 'Test Execution and pass percentage',
        type: ['Scrum'],
        fieldNames: []
      },
      {
        kpiId: 'kpi40',
        kpiName: 'Story Count',
        type: ['Scrum'],
        fieldNames: ['jiraStoryIdentification']
      },
      {
        kpiId: 'kpi72',
        kpiName: 'Commitment Reliability',
        type: ['Scrum'],
        fieldNames: ['jiraSprintVelocityIssueType']
      },
      {
        kpiId: 'kpi39',
        kpiName: 'Sprint Velocity',
        type: ['Scrum'],
        fieldNames: ['jiraSprintVelocityIssueType']
      },
      {
        kpiId: 'kpi46',
        kpiName: 'Sprint Capacity Utilization',
        type: ['Scrum'],
        fieldNames: ['jiraSprintCapacityIssueType']
      },
      {
        kpiId: 'kpi83',
        kpiName: 'Average Resolution Time',
        type: ['Scrum'],
        fieldNames: ['resolutionTypeForRejection', 'jiraIssueTypeNames', 'jiradefecttype', 'jiraIssueDeliverdStatus', 'jiraStatusForDevelopment']
      },
      {
        kpiId: 'kpi84',
        type: ['Scrum'],
        kpiName: 'Mean Time To Merge',
        fieldNames: []
      },
      {
        kpiId: 'kpi11',
        type: ['Scrum'],
        kpiName: 'Check-Ins & Merge Requests',
        fieldNames: []
      },
      {
        kpiId: 'kpi8',
        kpiName: 'Code Build Time',
        type: ['Scrum'],
        fieldNames: []
      },
      {
        kpiId: 'kpi3',
        kpiName: 'Lead Time',
        type: ['Scrum'],
        fieldNames: ['jiraIntakeToDorIssueType', 'jiraDor', 'jiraDod', 'jiraLiveStatus']
      },
      {
        kpiId: 'kpi118',
        kpiName: 'Deployment Frequency',
        type: ['Scrum'],
        fieldNames: []
      },
      {
        kpiId: 'kpi73',
        kpiName: 'Release Frequency',
        type: ['Scrum'],
        fieldNames: []
      },
      {
        kpiId: 'kpi113',
        kpiName: 'Value delivered (Cost of Delay)',
        type: ['Scrum'],
        fieldNames: []
      },

      // Kanban
      {
        kpiId: 'kpi55',
        kpiName: 'Ticket Open vs Closed rate by type',
        type: ['Kanban'],
        fieldNames: ['ticketCountIssueType', 'jiraTicketClosedStatus']
      },
      {
        kpiId: 'kpi54',
        kpiName: 'Ticket Open vs Closed rate by Priority',
        type: ['Kanban'],
        fieldNames: ['ticketCountIssueType', 'jiraTicketClosedStatus']
      },
      {
        kpiId: 'kpi50',
        kpiName: 'Net Open Ticket Count by Priority',
        type: ['Kanban'],
        fieldNames: ['storyFirstStatus', 'kanbanRCACountIssueType', 'ticketCountIssueType', 'jiraTicketClosedStatus', 'jiraLiveStatus', 'jiraTicketRejectedStatus']
      },
      {
        kpiId: 'kpi51',
        kpiName: 'Net Open Ticket Count By RCA',
        type: ['Kanban'],
        fieldNames: ['storyFirstStatus', 'kanbanRCACountIssueType', 'ticketCountIssueType', 'jiraTicketClosedStatus', 'jiraLiveStatus', 'jiraTicketRejectedStatus']
      },
      {
        kpiId: 'kpi48',
        kpiName: 'Net Open Ticket By Status',
        type: ['Kanban'],
        fieldNames: ['storyFirstStatus', 'kanbanRCACountIssueType', 'ticketCountIssueType', 'jiraTicketClosedStatus', 'jiraLiveStatus', 'jiraTicketRejectedStatus']
      },
      {
        kpiId: 'kpi997',
        kpiName: 'Open Ticket Ageing By Priority',
        type: ['Kanban'],
        fieldNames: ['ticketCountIssueType', 'jiraTicketClosedStatus', 'jiraLiveStatus', 'jiraTicketRejectedStatus']
      },
      {
        kpiId: 'kpi63',
        kpiName: 'Regression Automation Coverage',
        type: ['Kanban'],
        fieldNames: ['jiraRegressionTestValue', 'testRegressionValue', 'regressionAutomationFolderPath']
      },
      {
        kpiId: 'kpi49',
        kpiName: 'Ticket Velocity',
        type: ['Kanban'],
        fieldNames: ['jiraTicketVelocityIssueType', 'ticketDeliverdStatus']
      },
      {
        kpiId: 'kpi53',
        kpiName: 'Lead Time',
        type: ['Kanban'],
        fieldNames: ['jiraTicketTriagedStatus', 'jiraTicketClosedStatus', 'jiraLiveStatus']
      },
      {
        kpiId: 'kpi114',
        type: ['Kanban'],
        kpiName: 'Value delivered (Cost of Delay)',
        fieldNames: []
      },
      {
        kpiId: 'kpi74',
        kpiName: 'Release Frequency',
        type: ['Kanban'],
        fieldNames: []
      },
      {
        kpiId: 'kpi62',
        kpiName: 'Unit Test Coverage',
        type: ['Kanban'],
        fieldNames: []
      },
      {
        kpiId: 'kpi64',
        kpiName: 'Sonar Violations',
        type: ['Kanban'],
        fieldNames: []
      },
      {
        kpiId: 'kpi67',
        kpiName: 'Sonar Tech Debt',
        type: ['Kanban'],
        fieldNames: []
      },
      {
        kpiId: 'kpi71',
        kpiName: 'Test Execution and pass percentage',
        type: ['Kanban'],
        fieldNames: []
      },
      {
        kpiId: 'kpi58',
        kpiName: 'Team Capacity',
        type: ['Kanban'],
        fieldNames: []
      },
      {
        kpiId: 'kpi66',
        kpiName: 'Code Build Time',
        type: ['Kanban'],
        fieldNames: []
      },
      {
        kpiId: 'kpi65',
        kpiName: 'Number of Check-ins',
        type: ['Kanban'],
        fieldNames: []
      },
      {
        kpiId: 'kpi121',
        kpiName: 'Capacity',
        type: ['Kanban'],
        fieldNames: ['jiraSprintCapacityIssueType']
      },
      // others
      {
        kpiId: 'kpi119',
        kpiName: 'Work Remaining',
        type: ['Other'],
        fieldNames: []
      },
      {
        kpiId: 'kpi75',
        kpiName: 'Estimate vs Actual',
        type: ['Other'],
        fieldNames: []
      },
      {
        kpiId: 'kpi123',
        kpiName: 'Issues likely to Spill',
        type: ['Other'],
        fieldNames: ['workingHoursDayCPT']
      },
      {
        kpiId: 'kpi122',
        kpiName: 'Closure Possible Today',
        type: ['Other'],
        fieldNames: ['jiraStatusForQa', 'workingHoursDayCPT']
      },
      {
        kpiId: 'kpi120',
        kpiName: 'Scope Change',
        type: ['Other'],
        fieldNames: []
      },
      {
        kpiId: 'kpi124',
        kpiName: 'Estimation Hygiene',
        type: ['Other'],
        fieldNames: []
      },
      {
        kpiId: 'kpi125',
        kpiName: 'Daily Closures',
        type: ['Other'],
        fieldNames: []
      },
      {
        kpiId: 'kpi79',
        kpiName: 'Test Cases Without Story Link',
        type: ['Other'],
        fieldNames: ['jiraStoryIdentification', 'JiraRegressionTestValue', 'testRegressionValue', 'regressionAutomationFolderPath']
      },
      {
        kpiId: 'kpi80',
        kpiName: 'Defects Without Story Link',
        type: ['Other'],
        fieldNames: ['jiraStoryIdentification', 'jiraDefectDroppedStatus']
      },
      {
        kpiId: 'kpi127',
        kpiName: 'Production Defects Ageing',
        type: ['Other'],
        fieldNames: ['jiraDod', 'jiraLiveStatus', 'jiraDefectDroppedStatus']
      },
      {
        kpiId: 'kpi989',
        kpiName: 'Kpi Maturity',
        type: ['Other'],
        fieldNames: []
      }
    ];
    // this.http.getKPIFieldMappingRelationships().subscribe(response => {
    //   console.log(response);
    // });
    this.kpiRelationShips = this.kpiRelationShips.filter((kpi) => kpi.type.includes('Scrum') || kpi.type.includes('Other'));
  }

  generateAdditionalFilterMappings() {
    this.addAdditionalFilterOptions();
    this.selectedFieldMapping = this.sharedService.getSelectedFieldMapping();
    if (this.selectedFieldMapping) {
      const additionalFilterMappings = this.selectedFieldMapping.additionalFilterConfig;
      this.additionalFiltersArray = [];

      const additionalFilters = this.filterHierarchy.filter((filter) => filter.level > this.filterHierarchy.filter(f => f.hierarchyLevelId === 'sprint')[0].level);
      if (additionalFilterMappings && additionalFilterMappings.length) {
        additionalFilterMappings.forEach(element => {

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
              this.fieldMappingForm.addControl(element.filterId + 'IdentSingleValue', this.formBuilder.control('', [Validators.required]));
              this.fieldMappingForm.controls[element.filterId + 'IdentSingleValue'].setValue(element['identificationField']);
            }
          } else {
            if (!this.fieldMappingForm.controls[element.filterId + 'IdentMultiValue']) {
              this.fieldMappingForm.addControl(element.filterId + 'IdentMultiValue', this.formBuilder.control('', [Validators.required]));
              this.fieldMappingForm.controls[element.filterId + 'IdentMultiValue'].setValue(element['values']);
            }
          }
        });
      }

      for (const obj in this.selectedFieldMapping) {
        if (this.fieldMappingForm && this.fieldMappingForm.controls[obj]) {
          this.fieldMappingForm.controls[obj].setValue(this.selectedFieldMapping[obj]);
        }
      }
    }
  }

  getDropdownData() {
    if (this.selectedToolConfig && this.selectedToolConfig.length && this.selectedToolConfig[0].id) {
      this.http.getKPIConfigMetadata(this.selectedToolConfig[0].id).subscribe(response => {
        if (response.success) {
          this.fieldMappingMetaData = response.data;
        } else {
          this.fieldMappingMetaData = [];
        }
      });
    }
  }

  get fieldMapping() {
    return this.fieldMappingForm.controls;
  }

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


  cancelDialog() {
    this.populateDropdowns = false;
    this.displayDialog = false;
  }

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


  initializeFields() {
    this.fieldMappingFormDescriptionObj = [
      // workflow status mapping
      {
        fieldName: 'storyFirstStatus',
        field: [''],
        label: 'Story First Status',
        type: 'text',
        section: 'workflow',
        tooltip: `<span class="tooltiptext">
        Default status when a Story is opened.
        <i>
          Example: Open<br />
          Impacted : Jira Processor History</i>
      </span>`
      },
      {
        fieldName: 'jiraDefectCreatedStatus',
        field: [''],
        label: 'Defect Created Status',
        type: 'text',
        section: 'workflow',
        tooltip: `<span class="tooltiptext">
        First status of defect. Default status when a defect is opened.
        <i>
          Example: Open<br />
          Impacted : Jira Processor History</i>
      </span>`
      },
      {
        fieldName: 'jiraDefectDroppedStatus',
        field: [[]],
        label: 'Defect Dropped Status',
        type: 'chips',
        section: 'workflow',
        tooltip: `<span class="tooltiptext">
        Default status when a defect is dropped.
        <i>
          Example: Open<br />
          Impacted : Jira Processor History</i>
      </span>`
      },
      {
        fieldName: 'jiraLiveStatus',
        field: [''],
        label: 'Live Status - Cycle Time',
        type: 'text',
        section: 'workflow',
        tooltip: `<span class="tooltiptext">
        Provide any status from workflow on which Live is considered.
        <br /><i>
          Example: Live<br />
          Impacted : Cycle Time - DOD to Live</i>
      </span>`
      },
      {
        fieldName: 'jiraDor',
        field: [''],
        label: 'DOR Status - Cycle Time',
        type: 'text',
        section: 'workflow',
        tooltip: `<span class="tooltiptext">
        Definition of Readiness. Provide any status from workflow on which DOR is
        considered.<i>
          Example: In Sprint<br />
          Impacted : Cycle Time - Intake to DOR and DOR to DOD </i>
      </span>`
      },
      {
        fieldName: 'jiraDefectRejectionStatus',
        field: [''],
        label: 'Defect Rejection Status',
        type: 'text',
        section: 'workflow',
        tooltip: `<span class="tooltiptext">
        Status from workflow on which defect is considered as rejected.<i>
          Example: Cancelled<br />
          Impacted : Defect Rejection Rate Kpi </i>
      </span>`
      },
      {
        fieldName: 'issueStatusExcluMissingWork',
        field: [[]],
        label: 'Defect Rejection Status',
        type: 'chips',
        section: 'workflow',
        tooltip: `<span class="tooltiptext">
        Statuses selected depict all created status of issue types included in the Project.
        </span>`
      },
      {
        fieldName: 'jiraDod',
        field: [[]],
        label: 'DOD Status',
        type: 'chips',
        section: 'workflow',
        tooltip: `<span class="tooltiptext">
        Definition of Doneness. Provide any status from workflow on which DOD is
        considered.<br /><i>
          Example: In Testing <br />
          Impacted : QA Defect Density and Cycle Time - DOR to DOD and DOD to Live
        </i>
      </span>`
      },
      {
        fieldName: 'jiraIssueDeliverdStatus',
        field: [[]],
        label: 'Issue Delivered Status - Velocity',
        type: 'chips',
        section: 'workflow',
        tooltip: `<span class="tooltiptext">
        Status from workflow on which issue is delivered.
        <i>
          Example: Closed<br />
          Impacted : Sprint Velocity</i>
      </span>`
      },
      {
        fieldName: 'jiraDefectRemovalStatus',
        field: [[]],
        label: 'Defect Removal Status',
        type: 'chips',
        section: 'workflow',
        tooltip: `<span class="tooltiptext">
        Status from workflow on which defect is considered as removed.
        <br /><i>
          Example: Closed<br />
          Impacted : Defect Removal Rate</i>
      </span>`
      },
      {
        fieldName: 'resolutionTypeForRejection',
        field: [[]],
        label: 'Resolution Type for Rejection',
        type: 'chips',
        section: 'workflow',
        tooltip: `<span class="tooltiptext">
        Resolution type to identify rejected defects. e.g. Invalid
        </span>`
      },
      {
        fieldName: 'jiraStatusForDevelopment',
        field: [[]],
        label: 'Status to Identify Development Status',
        type: 'chips',
        section: 'workflow',
        tooltip: `<span class="tooltiptext">
        The status of Story Issue Type which identifies the "In-Development" status in
        JIRA.
        <br /><i>
          Example: In Development<br />
          Impacted : Indiviual Filter</i>
      </span>`
      },
      {
        fieldName: 'jiraStatusForQa',
        field: [[]],
        label: 'Status to Identify Testing Status',
        type: 'chips',
        section: 'workflow',
        tooltip: `<span class="tooltiptext">
        The status of Defect Issue Type which identifies the "In-Testing" status in
        JIRA.
        <br /><i>
          Example: Ready For Testing<br />
          Impacted : Indiviual Filter</i>
      </span>`
      },
      // issue type mapping
      {
        fieldName: 'jiraIssueTypeNames',
        field: [[]],
        label: 'Issue Types to be fetched from Jira',
        type: 'chips',
        section: 'Issue_Type',
        tooltip: `<span class="tooltiptext">
        Issue Type in Jira. These issue type are fetched in Speedy dashboard.
        <i>
          Example : "Story",
          "Defect",
          "Risk",
          "Change Request",
          "Test"<br />
          Impacted : Jira/Azure Processor and KPIs</i>
      </span>`
      },
      {
        fieldName: 'jiraDefectSeepageIssueType',
        field: [[]],
        label: 'Defect Seepage Rate - Issue Types with Linked Defect',
        type: 'chips',
        section: 'Issue_Type',
        tooltip: `<span class="tooltiptext">
        All issue types with which defect is linked .<br /><i>
            Example: Story, Change Request .
            Impacted : Defect Seepage Kpi </i>
      </span>`
      },
      {
        fieldName: 'jiraQADefectDensityIssueType',
        field: [[]],
        label: 'QA Defect Density - Issue Types with Linked Defect',
        type: 'chips',
        section: 'Issue_Type',
        tooltip: `<span class="tooltiptext">
        All issue types with which defect is linked .<br /><i>
            Example: Story, Change Request, Enhancement <br />
            Impacted KPI: QA Defect Density </i>
      </span>`
      },
      {
        fieldName: 'jiraDefectCountlIssueType',
        field: [[]],
        label: 'Defect Count - Issue Types with Linked Defect',
        type: 'chips',
        section: 'Issue_Type',
        tooltip: `<span class="tooltiptext">
        All issue types with which defect is linked.<br /><i>
            Example: Story, Change Request .<br />
            Impacted : Defect Count Kpi </i>
      </span>`
      },
      {
        fieldName: 'jiraSprintVelocityIssueType',
        field: [[]],
        label: 'Sprint Velocity - Issue Types with Linked Defect',
        type: 'chips',
        section: 'Issue_Type',
        tooltip: `<span class="tooltiptext">
        All issue types with which defect is linked.<br /><i>
            Example: Story, Change Request .<br />
            Impacted : Sprint Velocity Kpi </i>
      </span>`
      },
      {
        fieldName: 'jiraDefectRemovalIssueType',
        field: [[]],
        label: 'Defect Removal Rate - Issue Types with Linked Defect',
        type: 'chips',
        section: 'Issue_Type',
        tooltip: `<span class="tooltiptext">
        All issue types with which defect is linked.<br /><i>
            Example: Story, Change Request .<br />
            Impacted : Defect Removal Rate Kpi </i>
      </span>`
      },
      {
        fieldName: 'jiraDefectRejectionlIssueType',
        field: [[]],
        label: 'Defect Rejection Rate - Issue Types with Linked Defect',
        type: 'chips',
        section: 'Issue_Type',
        tooltip: `<span class="tooltiptext">
        All issue types with which defect is linked.<br /><i>
            Example: Story, Change Request .<br />
            Impacted : Defect Rejection Rate Kpi </i>
      </span>`
      },
      {
        fieldName: 'jiraDefectInjectionIssueType',
        field: [[]],
        label: 'Defect Injection Rate - Issue Types with Linked Defect',
        type: 'chips',
        section: 'Issue_Type',
        tooltip: `<span class="tooltiptext">
        All issue types with which defect is linked.<br /><i>
            Example: Story, Change Request .<br />
            Impacted : Defect Injection Rate Kpi </i>
      </span>`
      },
      {
        fieldName: 'jiraTestAutomationIssueType',
        field: [[]],
        label: 'In Sprint Automation - Issue Types with Linked Defect',
        type: 'chips',
        section: 'Issue_Type',
        tooltip: `<span class="tooltiptext">
        All issue types with which defect is linked.<br /><i>
            Example: Story, Change Request .<br />
            Impacted :Test Automation Kpi </i>
      </span>`
      },
      {
        fieldName: 'jiraIntakeToDorIssueType',
        field: [[]],
        label: 'Cycle Time Issue Type',
        type: 'chips',
        section: 'Issue_Type',
        tooltip: `<span class="tooltiptext">
        The issue type which is to be considered while calculating cycle time KPIs, i.e.
                  intake to DOR and DOR and DOD ...<br /><i>
                    Example: Story, Change Request .<br />
                    Impacted : Cycle Time Kpi </i>
      </span>`
      },
      {
        fieldName: 'jiraTechDebtIssueType',
        field: [[]],
        label: 'Tech Debt Issue Type',
        type: 'chips',
        section: 'Issue_Type',
        tooltip: `<span class="tooltiptext">
        The issue type in JIRA/Azure which is used for managing Tech Debt in JIRA/Azure
                  <br /><i>
                    Example: Story, Change Request .<br />
                    Impacted : Tech Debt Kpi </i>
      </span>`
      },
      {
        fieldName: 'jiraStoryIdentification',
        field: [[]],
        label: 'Story Count Issue Type',
        type: 'chips',
        section: 'Issue_Type',
        tooltip: `<span class="tooltiptext">
        Value to identify kind of stories which are used for identification for story
                  count.<br /><i>
                    Example: Story<br />
                    Impacted : Story Count Kpi </i>
      </span>`
      },
      {
        fieldName: 'jiraSprintCapacityIssueType',
        field: [[]],
        label: 'Sprint Capacity Issue Type',
        type: 'chips',
        section: 'Issue_Type',
        tooltip: `<span class="tooltiptext">
        Value to identify kind of stories which are used for identification for Sprint
                  Capacity.<br /><i>
                    Example: Story<br />
                    Impacted : Sprint Capacity Kpi </i>
      </span>`
      },
      {
        fieldName: 'jiraIssueEpicType',
        field: [[]],
        label: 'Epic Issue Type',
        type: 'chips',
        section: 'Issue_Type',
        tooltip: `<span class="tooltiptext">
        This field is used to identify Epic Issue type.
      </span>`
      },
      // tech debt mapping
      {
        fieldName: 'jiraTechDebtIdentification',
        field: [[]],
        label: 'Tech Debt Identification',
        type: 'dropdown',
        section: 'Issue_Type',
        tooltip: `<span class="tooltiptext">
        This field is used to identify "TECH_DEBT" stories. Only Below three values are
              allowed in this field:

              IssueType : If a separate Issue type is used.<i> Example: Tech Story,
                Tech_Story</i>
              CustomField : If a separate custom field is used.
              Labels : If a label is used to identify.<i> Example: "TECH_DEBT" (This
                has
                to be one value).</i>
              <i>
                Impacted : Jira/Azure Processor and Tech Debt Kpi</i>
      </span>`
      },
      {
        fieldName: 'jiraTechDebtValue',
        field: [[]],
        label: 'Tech Debt Identification',
        type: 'chips',
        section: 'Issue_Type',
        tooltip: `<span class="tooltiptext">
        Provide label name to identify Jira/Azure Tech debt.
        <br /><strong>
          Example: "TECH_DEBT"<br />
          Impacted : Jira/Azure Processor and Jira/Azure Tech Debt Kpi</strong>
  </span>`
      }, {
        fieldName: 'jiraTechDebtCustomField',
        field: [''],
        label: 'Tech Debt Custom Field',
        type: 'text',
        section: 'fields',
        tooltip: `<span class="tooltiptext">
        Provide customfield name to identify Jira/Azure Tech Debt Stories.
        <br /><i>
          Example: "customfield_13907"<br />
          Impacted : Jira/Azure Processor and Jira/Azure Tech Debt Kpi</i>
  </span>`
      },
      // custom field mapping
      {
        fieldName: 'sprintName',
        field: [''],
        label: 'Sprint Name',
        type: 'text',
        section: 'fields',
        tooltip: `<span class="tooltiptext">
        JIRA applications let you add custom fields in addition to the built-in fields.
        Sprint name is a custom field in JIRA. So User need to provide that custom field
        which is associated with Sprint in Users JIRA Installation.<i><br />
          Example : customfield_12700<br />
          Impacted : Jira Collector and all Scrum based KPIs.</i>
  </span>`
      },
      {
        fieldName: 'rootCause',
        field: [''],
        label: 'Root Cause',
        type: 'text',
        section: 'fields',
        tooltip: `<span class="tooltiptext">
        JIRA/AZURE applications let you add custom fields in addition to the built-in
          fields.
          Root Cause is a custom field in JIRA. So User need to provide that custom field
          which is associated with Root Cause in Users JIRA Installation.<br />
          <i>
            Example : customfield_19121 <br />
            Impacted : Jira/Azure processor and Defect Count By RCA Kpi.<br />
          </i>
  </span>`
      },
      {
        fieldName: 'jiraStoryPointsCustomField',
        field: [''],
        label: 'Estimation',
        type: 'text',
        section: 'fields',
        tooltip: `<span class="tooltiptext">
        JIRA/AZURE applications let you add custom fields in addition to the built-in
          fields.
          Story Point is a custom field in JIRA. So User need to provide that custom field
          which is associated with Story point in Users JIRA/AZURE
          Installation.<i><br />
            For
            example : customfield_20803.</i>
  </span>`
      },
      {
        fieldName: 'estimationCriteria',
        field: [''],
        label: 'Estimation Criteria',
        type: 'dropdown',
        section: 'fields',
        tooltip: `<span class="tooltiptext">
        Estimation criteria for stories.
              <br />
              <i>Example: Buffered Estimation.</i>
      </span>`
      },
      {
        fieldName: 'storyPointToHourMapping',
        field: [''],
        label: 'Story Point to Hour Conversion',
        type: 'text',
        section: 'fields',
        tooltip: `<span class="tooltiptext">
        Conversion factor for Story Point to Hour Conversion.
        <br />
        <i>Example: If 1 Story Point is 8 hrs, enter 8.</i>
  </span>`
      },
      {
        fieldName: 'epicCostOfDelay',
        field: [''],
        label: 'Epic Cost of Delay',
        type: 'text',
        section: 'fields',
        tooltip: `<span class="tooltiptext">JIRA/AZURE applications let you add custom fields in addition to the built-in
        fields.
      Provide value of Cost Of delay field for Epics that need to show on Trend line.<br />
      <i> Example:customfield_11111</i>
      <i>Impacted : Cost of delay</i>
      </span>`
      },
      {
        fieldName: 'epicRiskReduction',
        field: [''],
        label: 'Epic Risk Reduction',
        type: 'text',
        section: 'fields',
        tooltip: `<span class="tooltiptext">JIRA/AZURE applications let you add custom fields in addition to the built-in
        fields.
      Provide value of Risk reduction/ Enablement value for Epic that is required to calculated Cost of delay <br />
      <i>eg:<br/> customfield_11111</i><br/>
      <i>Impacted:<br/> Cost of delay KPI</i>
</span>`
      }, {
        fieldName: 'epicUserBusinessValue',
        field: [''],
        label: 'Epic Business Value',
        type: 'text',
        section: 'fields',
        tooltip: `<span class="tooltiptext"> JIRA/AZURE applications let you add custom fields in addition to the built-in
        fields.
      Provide value of User-Business Value for Epic that is required to calculated Cost of delay .<br />
      <i> Example:customfield_11111</i>
      <i>Impacted : Cost of delay</i>
</span>`
      },
      {
        fieldName: 'epicWsjf',
        field: [''],
        label: 'Epic WSJF',
        type: 'text',
        section: 'fields',
        tooltip: `<span class="tooltiptext"> JIRA/AZURE applications let you add custom fields in addition to the built-in
        fields.
      Provide value of WSJF value that is required to calculated Cost of delay <br />
      <i> Example:customfield_11111</i>
      <i>Impacted : Cost of delay</i>
</span>`
      },
      {
        fieldName: 'epicTimeCriticality',
        field: [''],
        label: 'Epic Time Criticality',
        type: 'text',
        section: 'fields',
        tooltip: `<span class="tooltiptext"> JIRA/AZURE applications let you add custom fields in addition to the built-in
        fields.
     Provide value of Time Criticality value on Epic that is required to calculated Cost of delay .<br />
      <i> Example:customfield_11111</i>
      <i>Impacted : Cost of delay</i>
</span>`
      },
      {
        fieldName: 'epicJobSize',
        field: [''],
        label: 'Epic Job Size',
        type: 'text',
        section: 'fields',
        tooltip: `<span class="tooltiptext"> JIRA/AZURE applications let you add custom fields in addition to the built-in
      fields.
    Provide value of Job size on EPIC that is required to calculated WSJF.<br />
    <i> Example:customfield_11111</i>
</span>`
      },
      {
        fieldName: 'workingHoursDayCPT',
        field: [''],
        label: 'Epic Job Size',
        type: 'number',
        section: 'fields',
        tooltip: `<span class="tooltiptext">
        Working hours in a day.
  </span>`
      },
      // defect mapping
      {
        fieldName: 'jiradefecttype',
        field: [[]],
        label: 'Issue Type to Identify Defect',
        type: 'chips',
        section: 'Issue_Type',
        tooltip: `<span class="tooltiptext">
        In JIRA/AZURE a defect can be defined as "Bug", "Defect",
        "Snag"
        or any other value. So user need to provide value with which defect is identified
        in JIRA/AZURE.<br />
        <i>Impacted : "Jira/Azure Collector and KPIs</i>
  </span>`
      },
      {
        fieldName: 'defectPriority',
        field: [[]],
        label: 'Defect priority to exclude from FTPR',
        type: 'multiselect',
        section: '',
        tooltip: `<span class="tooltiptext">
        This field is used to identify if a defect is raised by
        FTPR:
       <i>
          Impacted : "First Time Pass Rate Kpi.</i>
          </span>`
      },
      {
        fieldName: 'jiraBugRaisedByIdentification',
        field: [''],
        label: 'UAT Defect Identification',
        type: 'dropdown',
        section: '',
        tooltip: `<span class="tooltiptext">
        This field is used to identify if a defect is raised by
        third party or client:
        1. CustomField : If a separate custom field is used.
        2. Labels : If a label is used to identify. Example: "TECH_DEBT" (This has to be
        one value).<i>
          Impacted : "Jira/AZURE Collector" and Defect Seepage Rate Kpi.</i>
          </span>`
      },
      {
        fieldName: 'jiraBugRaisedByCustomField',
        field: [''],
        label: 'UAT Defect Custom Field',
        type: 'dropdown',
        section: 'fields',
        tooltip: `<span class="tooltiptext">
        Provide customfield name to identify UAT or client raised defects.<br />
        <i> Example: customfield_13907</i>
  </span>`
      },
      {
        fieldName: 'jiraBugRaisedByValue',
        field: [[]],
        label: 'UAT Defect Values',
        type: 'chips',
        section: '',
        tooltip: `<span class="tooltiptext">
        Provide label name to identify UAT or client raised defects.<br /> <i>
            For Example : "Clone_by_QA"<br />
            Impacted : Jira/Azure Collector and Defect Seepage Rate Kpi.
      </span>`
      },
      {
        fieldName: 'jiraBugRaisedByQAIdentification',
        field: [''],
        label: 'QA Defect Identification',
        type: 'dropdown',
        section: '',
        tooltip: `<span class="tooltiptext">
        This field is used to identify if a defect is raised by
                  QA:<br />
                  1. CustomField : If a separate custom field is used.<br />
                  2. Labels : If a label is used to identify. Example: "QA Defect"<br /><i>
                    Impacted KPI: QA Defect Density</i>
          </span>`
      },
      {
        fieldName: 'jiraBugRaisedByQACustomField',
        field: [''],
        label: 'QA Defect Custom Field',
        type: 'text',
        section: 'fields',
        tooltip: `<span class="tooltiptext">
        Provide customfield name to identify QA raised defects.<br />
        <i> Example: customfield_13907</i>
</span>`
      },
      {
        fieldName: 'jiraBugRaisedByQAValue',
        field: [[]],
        label: 'QA Defect Values',
        type: 'chips',
        section: '',
        tooltip: `<span class="tooltiptext">
        Provide label name to identify QA raised defects.
  </span>`
      },
      {
        fieldName: 'productionDefectCustomField',
        field: [''],
        label: 'Production Defect Custom Field',
        type: 'text',
        section: 'fields',
        tooltip: `<span class="tooltiptext">
        Provide customfield name to identify Production raised defects.<br />
        <i> Example: customfield_13907</i>
</span>`
      },
      {
        fieldName: 'productionDefectIdentifier',
        field: [''],
        label: 'Production defects identification',
        type: 'dropdown',
        section: '',
        tooltip: `<span class="tooltiptext">
        This field is used to identify if a defect is raised by
        Production:<br />
                  1. CustomField : If a separate custom field is used.<br />
                  2. Labels : If a label is used to identify. Example: "Production Defect"<br /><i>
                  3. Component : If a Component is used to identify. Example: "Production Defect"<br /><i>
                    Impacted KPI: Production Defects Ageing</i>
          </span>`
      },
      {
        fieldName: 'productionDefectComponentValue',
        field: [''],
        label: 'Production defects Component',
        type: 'text',
        section: '',
        tooltip: `<span class="tooltiptext">
        Provide label name to identify Production raised defects.
  </span>`
      },
      {
        fieldName: 'productionDefectValue',
        field: [[]],
        label: 'Production Defect Values',
        type: 'chips',
        section: '',
        tooltip: `<span class="tooltiptext">
        Provide label name to identify Production raised defects.
  </span>`
      },
      {
        fieldName: 'rootCauseValue',
        field: [[]],
        label: 'RCA Value on Trend',
        type: 'chips',
        section: '',
        tooltip: `<span class="tooltiptext">
        Provide value of RCA that need to show on Trend line.<br />
        <i> Example: Code Issue</i>
        <i>Impacted : Defect Count By RCA (tagged to Story)</i>
</span>`
      },
      // qaRootCauseValue: [[]],
      // test case mapping
      {
        fieldName: 'testAutomatedIdentification',
        field: [''],
        label: 'Test Case Automation Field',
        type: 'dropdown',
        section: '',
        tooltip: `<span class="tooltiptext">
        Jira/Azure allow addition of filtering data through custom field or labels.
        It can be configured to determine if a test case is automatable or not.
  </span>`
      },
      {
        fieldName: 'testAutomationCompletedIdentification',
        field: [''],
        label: 'Automation completed field',
        type: 'dropdown',
        section: '',
        tooltip: `<span class="tooltiptext">
        Jira/Azure allow addition of filtering data through custom field or labels.
        It can be configured to determine if a test case is automated or not.
  </span>`
      },
      {
        fieldName: 'testRegressionIdentification',
        field: [''],
        label: 'Automation completed field',
        type: 'dropdown',
        section: '',
        tooltip: `<span class="tooltiptext">
        Jira/Azure allow addition of filtering data through custom field or labels.
        It can be used to identify regression test cases
  </span>`,
        options: this.testCaseIdentification
      },
      {
        fieldName: 'testAutomated',
        field: [''],
        label: 'Custom Field Id',
        type: 'text',
        section: 'fields',
        tooltip: `<span class="tooltiptext">
        Provide customfield name to identify test case is automatable or not.<br />
        <i> Example: customfield_13907</i>
  </span>`
      },
      {
        fieldName: 'testAutomationCompletedByCustomField',
        field: [''],
        label: 'Custom Field Id',
        type: 'text',
        section: 'fields',
        tooltip: `<span class="tooltiptext">
        Provide customfield name to identify  if a test case is already automated
  </span>`
      },
      {
        fieldName: 'testRegressionByCustomField',
        field: [''],
        label: 'Custom Field Id',
        type: 'text',
        section: 'fields',
        tooltip: `<span class="tooltiptext">
        Provide customfield name to identify the test cases part of regression suite
  </span>`
      },
      {
        fieldName: 'jiraCanBeAutomatedTestValue',
        field: [[]],
        label: 'Values for Automation',
        type: 'chips',
        section: '',
        tooltip: `<span class="tooltiptext">
        Enter the field labels used in Jira/Azure to identify if a test case can be automated
  </span>`
      },
      {
        fieldName: 'jiraRegressionTestValue',
        field: [[]],
        label: 'Values for regression test cases',
        type: 'chips',
        section: '',
        tooltip: `<span class="tooltiptext">
        Enter the field labels used in Jira/Azure to identify the test cases part of regression suite
  </span>`
      },
      {
        fieldName: 'jiraCanNotAutomatedTestValue',
        field: [[]],
        label: 'Values for regression test cases',
        type: 'chips',
        section: '',
        tooltip: `<span class="tooltiptext">
        Enter the field labels used in Jira/Azure to identify the test cases part of regression suite
  </span>`
      },
      {
        fieldName: 'jiraAutomatedTestValue',
        field: [[]],
        label: 'Values for Automation completed',
        type: 'chips',
        section: '',
        tooltip: `<span class="tooltiptext">
        Enter the field labels used in Jira/Azure to identify if a test case is already automated
  </span>`
      },
      {
        fieldName: 'jiraTestCaseType',
        field: [[]],
        label: 'Test Case Issue Type',
        type: 'chips',
        section: 'Issue_Type',
        tooltip: `<span class="tooltiptext">
        Issue type of Test Case.
        <br /><i>
        Example: "Test"<br />
        Impacted : Sprint Automation and Regression Automation</i>
  </span>`
      },
      {
        fieldName: 'testCaseStatus',
        field: [[]],
        label: 'Status to identify abandoned Test cases',
        type: 'chips',
        section: 'workflow',
        tooltip: `<span class="tooltiptext">
        Select status like "Abandoned", "Deprecated" etc so that these can be excluded from Regression automation coverage, In Sprint automation coverage and Test case without story link KPI
  </span>`
      },
      {
        fieldName: 'regressionAutomationLabels',
        field: [[]],
        label: 'Status to identify abandoned Test cases',
        type: 'chips',
        section: 'workflow',
        tooltip: `<span class="tooltiptext">
        Select status like "Abandoned", "Deprecated" etc so that these can be excluded from Regression automation coverage, In Sprint automation coverage and Test case without story link KPI
  </span>`
      },
      {
        fieldName: 'excludeRCAFromFTPR',
        field: [[]],
        label: 'RCA values to exclude from FTPR',
        type: 'chips',
        section: '',
        tooltip: `<span class="tooltiptext">
        Provide RCA values against which the tagged defects will be excluded from FTPR<br />
        <i> Example: Coding, Requirements etc </i></span>`
      }
    ];
    this.fieldMappingFormObj = {};
    this.fieldMappingFormDescriptionObj.forEach((desc) => {
      this.fieldMappingFormObj[desc['fieldName']] = desc['field'];
    });
    this.addAdditionalFilterOptions();
  }

  addAdditionalFilterOptions() {
    this.additionalFilterOptions = [];
    const additionalFilters = this.filterHierarchy.filter((filter) => filter.level > this.filterHierarchy.filter(f => f.hierarchyLevelId === 'sprint')[0].level);
    additionalFilters.forEach(element => {
      this.additionalFilterOptions.push({
        name: element.hierarchyLevelName,
        code: element.hierarchyLevelId
      });
    });

  }

  addAdditionalFilterMappings() {
    if (!this.additionalFiltersArray.filter((filter) => filter.name === this.additionalFilterIdentifier.name).length) {
      this.additionalFiltersArray.push(this.additionalFilterIdentifier);

      this.additionalFiltersArray.forEach(element => {
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
  }

  changeControl(event) {
    if (event.value === 'Component' || event.value === 'Labels') {
      if (!this.fieldMappingForm.controls[this.additionalFilterIdentifier.code + 'IdentMultiValue']) {
        this.fieldMappingForm.addControl(this.additionalFilterIdentifier.code + 'IdentMultiValue', this.formBuilder.control('', [Validators.required]));
      }
      if (this.fieldMappingForm.controls[this.additionalFilterIdentifier.code + 'IdentSingleValue']) {
        this.fieldMappingForm.removeControl(this.additionalFilterIdentifier.code + 'IdentSingleValue');
      }

    } else {
      if (!this.fieldMappingForm.controls[this.additionalFilterIdentifier.code + 'IdentSingleValue']) {
        this.fieldMappingForm.addControl(this.additionalFilterIdentifier.code + 'IdentSingleValue', this.formBuilder.control('', [Validators.required]));
      }
      if (this.fieldMappingForm.controls[this.additionalFilterIdentifier.code + 'IdentMultiValue']) {
        this.fieldMappingForm.removeControl(this.additionalFilterIdentifier.code + 'IdentMultiValue');
      }
    }
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
  }

  // validate any field in config by a Regular expression
  validateRegexp(e, regexp, singleChar = false) {
  }



  recordScrollPosition() {
    this.bodyScrollPosition = document.documentElement.scrollTop;
  }

  scrollToPosition() {
    this.populateDropdowns = false;
    document.documentElement.scrollTop = this.bodyScrollPosition;
  }

  save() {
    this.fieldMappingSubmitted = true;
    // return if form is invalid
    if (this.fieldMappingForm.invalid) {
      return;
    }

    let submitData = {};
    for (const obj in this.fieldMapping) {
      submitData[obj] = this.fieldMapping[obj].value;
    }

    submitData['basicProjectConfigId'] = this.selectedConfig.id;

    submitData = this.handleAdditionalFilters(submitData);
    this.http.setFieldMappings(this.selectedToolConfig[0].id, submitData).subscribe(response => {
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

  onUpload(event) {
    this.uploadedFileName = event.target.files[0].name;
    const fileReader = new FileReader();
    fileReader.readAsText(event.target.files[0], 'UTF-8');
    fileReader.onload = () => {
      this.sharedService.setSelectedFieldMapping(JSON.parse(fileReader.result as string));
      this.getMappings();
    };
    fileReader.onerror = (error) => {
      console.log(error);
    };
  };

  onSelectPriority(event: any) {

    // if (event == '') {
    //   this.selectedPriority.map(p =>{
    //     return this.selectedPriority.push(p.label)}
    //   ).join(',');
    // } else {
    if (this.selectedPriority.includes(event.value)) { // remove
      this.selectedPriority.push(event.value); // add

    }
  }

  export() {
    this.fieldMappingSubmitted = true;
    // return if form is invalid
    if (this.fieldMappingForm.invalid) {
      return;
    }

    const submitData = {};
    for (const obj in this.fieldMapping) {
      submitData[obj] = this.fieldMapping[obj].value;
    }

    this.handleAdditionalFilters(submitData);

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
          additionalFilterObj['values'] = submitData[element.hierarchyLevelId + 'IdentMultiValue'];
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
