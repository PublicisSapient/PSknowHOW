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

  selectedConfig: any = {};
  selectedToolConfig: any = {};
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

  form: FormGroup;
  fieldMappingSectionList = ['Workflow Status Mapping','Issue Types Mapping','Custom field Mapping'];
  formData ={
    "id": "648cf9c00533dd4339d3ec3f",
    "projectToolConfigId": "648c7fc81acbd04b0407db70",
    "basicProjectConfigId": "648c7f941acbd04b0407db6e",
    "sprintName": "customfield_12700",
    "jiradefecttype": [
        "Defect"
    ],
    "defectPriority": [],
    "jiraIssueTypeNames": [
        "Story",
        "Enabler Story",
        "Change request",
        "Defect"
    ],
    "storyFirstStatus": "Open",
    "rootCause": "customfield_19121",
    "jiraStatusForDevelopment": [
        "In Development"
    ],
    "jiraIssueEpicType": [
        "Epic"
    ],
    "jiraStatusForQa": [
        "In Testing"
    ],
    "jiraDefectInjectionIssueType": [
        "Story",
        "Enabler Story",
        "Change request",
        "Defect"
    ],
    "jiraDod": [
        "Closed"
    ],
    "jiraDefectCreatedStatus": "Open",
    "jiraDefectRejectionStatus": "Rejected",
    "jiraBugRaisedByIdentification": "customfield",
    "jiraBugRaisedByValue": ['open'],
    "jiraDefectSeepageIssueType": [
        "Story",
        "Enabler Story",
        "Change request",
        "Defect"
    ],
    "jiraBugRaisedByCustomField": "",
    "jiraDefectRemovalStatus": [],
    "jiraDefectRemovalIssueType": [
        "Story",
        "Enabler Story",
        "Change request",
        "Defect"
    ],
    "jiraStoryPointsCustomField": "customfield_20803",
    "jiraTestAutomationIssueType": [
        "Story",
        "Enabler Story",
        "Change request",
        "Defect"
    ],
    "jiraSprintVelocityIssueType": [
        "Story",
        "Enabler Story",
        "Change request",
        "Defect"
    ],
    "jiraSprintCapacityIssueType": [
        "Story",
        "Enabler Story",
        "Change request",
        "Defect"
    ],
    "jiraDefectRejectionlIssueType": [
        "Story",
        "Enabler Story",
        "Change request",
        "Defect"
    ],
    "jiraDefectCountlIssueType": [
        "Story",
        "Enabler Story",
        "Change request",
        "Defect"
    ],
    "jiraIssueDeliverdStatus": [
        "Closed"
    ],
    "readyForDevelopmentStatus": "",
    "jiraDor": "Open",
    "jiraIntakeToDorIssueType": [
        "Story",
        "Enabler Story",
        "Change request",
        "Defect"
    ],
    "jiraStoryIdentification": [
        "Story",
        "Enabler Story"
    ],
    "jiraLiveStatus": "Closed",
    "excludeRCAFromFTPR": [],
    "resolutionTypeForRejection": [
        "Dropped",
        "Rejected"
    ],
    "jiraQADefectDensityIssueType": [
        "Story",
        "Enabler Story",
        "Change request",
        "Defect"
    ],
    "jiraBugRaisedByQACustomField": "",
    "jiraBugRaisedByQAIdentification": "",
    "jiraBugRaisedByQAValue": [],
    "jiraDefectDroppedStatus": [
        "Dropped",
        "Rejected"
    ],
    "jiraDefectClosedStatus": [],
    "epicCostOfDelay": "customfield_58102",
    "epicRiskReduction": "customfield_58101",
    "epicUserBusinessValue": "customfield_58100",
    "epicWsjf": "customfield_58104",
    "epicTimeCriticality": "customfield_51002",
    "epicJobSize": "customfield_61041",
    "productionDefectCustomField": "",
    "productionDefectIdentifier": "",
    "productionDefectValue": [],
    "productionDefectComponentValue": "",
    "jiraFTPRStoryIdentification": [],
    "estimationCriteria": "Actual Estimation",
    "storyPointToHourMapping": 8,
    "workingHoursDayCPT": 6,
    "additionalFilterConfig": [],
    "jiraBlockedStatus": [],
    "jiraDueDateField": "Due Date",
    "jiraDueDateCustomField": "",
    "jiraDevDueDateCustomField": "",
    "jiraRejectedInRefinement": [],
    "jiraAcceptedInRefinement": [],
    "jiraReadyForRefinement": [],
    "jiraFtprRejectStatus": [],
    "jiraIterationCompletionStatusCustomField": [],
    "jiraIterationCompletionTypeCustomField": []
};
  formConfig = {
    "Workflow Status Mapping": [
      {
        "fieldLabel": "Story First Status",
        "fieldName": "storyFirstStatus",
        "fieldType": "text",
        "searchBtn": true,
        "fieldCategory": "workflow",
        "tooltip": {
          "definition": "Default status when a Story is opened.<br>  Example: Open",
          "kpiImpacted": "Jira Processor history"
        },
      },
      {
        "fieldLabel": "Defect priority exclusion from Quality KPIs",
        "fieldName": "defectPriority",
        "fieldType": "multiSelect",
        "fieldCategory": "workflow",
        "options": [
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
        ],
        "tooltip": {
          "definition": "Default status when a Story is opened.<br>  Example: Open",
          "kpiImpacted": "Jira Processor history"
        },
      },
      {
        "fieldLabel": "Issue Delivered Status",
        "fieldName": "jiraIssueDeliverdStatus",
        "fieldType": "multiValue",
        "searchBtn": true,
        "fieldCategory": "workiflow",
        "tooltip": {
          "definition": " Status from workflow on which issue is delivered. Example: Closed",
          "kpiImpacted": "Jira Processor history"
        },
      }
    ],
    "Issue Types Mapping": [
      {
        "fieldLabel": "UAT Defect Identification",
        "fieldName": "jiraBugRaisedByIdentification",
        "fieldType": "radio",
        "tooltip": {
          "definition": "Default status when a story is opened",
          "kpiImpacted": "Jira Processor history"
        },
        "options": [
          {
            "label": "Custom Field",
            "value": "customfield"
          },
          {
            "label": "Labels",
            "value": "labels"
          }
        ],
        "nestedFields": [
          {
            "fieldLabel": "UAT Defect Custom Field",
            "fieldName": "jiraBugRaisedByCustomField",
            "fieldType": "text",
            "searchBtn": true,
            "filterGroup": ["customfield","labels"],
            "fieldCategory" : "Issuetype",
            "tooltip": {
              "definition": "Default status when a story is opened",
              "kpiImpacted": "Jira Processor history"
            }
          },
          {
            "fieldLabel": "UAT Defect Values",
            "fieldName": "jiraBugRaisedByValue",
            "fieldType": "multiValue",
            "searchBtn": false,
            "filterGroup": ["labels"],
            "fieldCategory" : "Issuetype",
            "tooltip": {
              "definition": "Default status when a story is opened",
              "kpiImpacted": "Jira Processor history"
            }
          }
        ]
      }
    ],
    "Custom field Mapping":[
      {
        "fieldLabel": "Estimation",
        "fieldName": "jiraStoryPointsCustomField",
        "fieldType": "text",
        "searchBtn": true,
        "fieldCategory": "fields",
        "tooltip": {
          "definition": "Default status when a Story is opened.  Example: Open",
          "kpiImpacted": "Jira Processor history"
        },
      },
    ]

  };

  constructor(private sharedService : SharedService,
    private getAuthorizationService : GetAuthorizationService,
    private router : Router,
    private http : HttpService,
    private messenger: MessageService,
    private confirmationService: ConfirmationService) { }

  ngOnInit(): void {
    this.initializeForm();
  }


  initializeForm(){
    const formObj ={};
    for(const field in this.formData){
      formObj[field] = new FormControl(this.formData[field]);
    }
    this.form = new FormGroup(formObj);
    console.log(this.form);
  }

  showForm(){
    console.log(this.form.value);
    
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
    const submitData = {};
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
