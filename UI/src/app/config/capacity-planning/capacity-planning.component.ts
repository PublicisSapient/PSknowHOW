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

import { ChangeDetectorRef, Component, OnInit, ViewChild } from '@angular/core';
import { FormControl, UntypedFormControl, UntypedFormGroup, Validators } from '@angular/forms';
import { environment } from '../../../environments/environment';
import { MessageService } from 'primeng/api';
import { HttpService } from '../../services/http.service';
import { ManageAssigneeComponent } from '../manage-assignee/manage-assignee.component';
import { GetAuthorizationService } from '../../services/get-authorization.service';
import { HelperService } from 'src/app/services/helper.service';

interface CapacitySubmissionReq {
  projectNodeId: string;
  projectName: string;
  sprintNodeId?: string;
  capacity?: string;
  startDate?: string;
  endDate?: string;
  totalTestCases?: string;
  executedTestCase?: string;
  passedTestCase?: string;
  sprintId?: string;
  sprintName?: string;
  executionDate?: string;
  kanban: boolean;
  basicProjectConfigId?: string;
}

@Component({
  selector: 'app-capacity-planning',
  templateUrl: './capacity-planning.component.html',
  styleUrls: ['./capacity-planning.component.css']
})
export class CapacityPlanningComponent implements OnInit {
  @ViewChild('manageAssignee') manageAssignee: ManageAssigneeComponent;
  trendLineValueList: any[];
  projectListArr: Array<object> = [];
  squadListArr: Array<object> = [];
  filterForm: UntypedFormGroup;
  tabHeaders = ['Scrum', 'Kanban'];
  tabContentHeaders = { upload_tep: 'Test Execution Percentage Table', upload_Sprint_Capacity: 'Capacity Table' };
  cols: any;
  kanban: boolean;
  baseUrl = environment.baseUrl;  // Servers Env
  projects: any;
  selectedProject: any;
  dropdownSettingsProject = {};
  startDate: any;
  endDate: any;
  executionDate: any;
  popupForm: UntypedFormGroup;
  squadForm: UntypedFormGroup;
  selectedSprintAssigneFormArray = [];
  selectedSquad = [];
  selectedSprintAssigneValidator = [];
  isCapacitySaveDisabled = true;
  capacityErrorMessage = '';
  tableLoader = true;
  currentDate = new Date();
  noData = false;
  capacityScrumData: any;
  capacityKanbanData: any;
  filteredSprints: any = [];
  sprintDetails: any;
  projectDetails: any;
  selectedProjectBaseConfigId: string;
  selectedProjectName: string;
  selectedSprintDetails: any;
  selectedSprintId: any;
  selectedSprintName: any;
  filter_kpiRequest = <any>'';
  selectedFilterData = <any>{};
  selectedFilterCount = 0;
  filterData = <any>[];
  masterData = <any>{};
  projectAssigneeEmails = [];
  projectAssigneeEmailsCopy = [];
  isToggleEnableForSelectedProject = false;
  projectAssigneeRoles = [];
  projectAssigneeRolesObj;
  projectCapacityEditMode = false;
  displayAssignee = false;
  projectJiraAssignees = {};
  manageAssigneeList = [];
  jiraAssigneeLoader = false;
  loader = false;
  selectedSprint;
  expandedRows = {};
  selectedView = 'upload_Sprint_Capacity';
  showPopuup = false;
  reqObj: CapacitySubmissionReq;
  isAdminForSelectedProject = false;
  constructor(private http_service: HttpService, private messageService: MessageService, private cdr: ChangeDetectorRef, public getAuthorizationService: GetAuthorizationService,
    private helperService: HelperService) { }

  ngOnInit(): void {
    this.cols = {
      capacityScrumKeys: [
        {
          header: 'Sprint Name',
          field: 'sprintName'
        },
        {
          header: 'Sprint Status',
          field: 'sprintState'
        },
        {
          header: 'Team Capacity (in Hrs)',
          field: 'capacity'
        }
      ],
      capacityKanbanKeys: [
        {
          header: 'Start Date',
          field: 'startDate'
        },
        {
          header: 'End Date',
          field: 'endDate'
        },
        {
          header: 'Team Capacity (in Hrs)',
          field: 'capacity'
        }
      ]
    };

    this.dropdownSettingsProject = {
      // singleSelection: false,
      text: 'Select Project',
      selectAllText: 'Select All',
      unSelectAllText: 'UnSelect All',
      enableSearchFilter: true,
      classes: 'multi-select-custom-class'
    };

    this.kanban = false;
    this.startDate = '';
    this.endDate = '';
    this.executionDate = '';
    this.setFormControlValues();

    this.startDate = '';
    this.endDate = '';
    this.capacityErrorMessage = '';
    this.kanban = false;
    this.isCapacitySaveDisabled = true;
    this.loader = true;
    this.selectedProjectBaseConfigId = '';

    this.filterForm = new UntypedFormGroup({
      selectedProjectValue: new UntypedFormControl()
    });
    this.popupForm = new UntypedFormGroup({
      capacity: new UntypedFormControl(),
    });


    this.getFilterDataOnLoad();



  }

  resetProjectSelection() {
    this.projectListArr = [];
    this.trendLineValueList = [];
    this.filterForm?.get('selectedProjectValue').setValue('');
  }

  setFormControlValues() {
    this.filterForm = new UntypedFormGroup({
      selectedProjectValue: new UntypedFormControl()
    });

    this.popupForm = new UntypedFormGroup({
      capacity: new UntypedFormControl()
    });

    //add additionalFilterCapacity checks
    this.squadForm = new UntypedFormGroup({});
  }

  // called when user switches the "Scrum/Kanban" switch
  kanbanActivation(type) {
    this.selectedSprintAssigneValidator = [];
    const scrumTarget = document.querySelector('.horizontal-tabs .btn-tab.pi-scrum-button');
    const kanbanTarget = document.querySelector('.horizontal-tabs .btn-tab.pi-kanban-button');
    if (type === 'scrum') {
      scrumTarget?.classList?.add('btn-active');
      kanbanTarget?.classList?.remove('btn-active');
    } else {
      scrumTarget?.classList?.remove('btn-active');
      kanbanTarget?.classList?.add('btn-active');
    }
    this.kanban = type === 'scrum' ? false : true;
    this.startDate = '';
    this.endDate = '';
    this.executionDate = '';
    this.capacityErrorMessage = '';
    this.isCapacitySaveDisabled = true;
    this.loader = true;
    this.tableLoader = true;
    this.noData = false;
    this.capacityKanbanData = [];
    this.capacityScrumData = [];
    this.projectDetails = {};
    this.selectedProjectBaseConfigId = '';
    this.selectedProjectName = '';
    this.getFilterDataOnLoad();
  }

  // gets data for filters on load
  getFilterDataOnLoad() {

    if (this.filter_kpiRequest !== '') {
      this.filter_kpiRequest.unsubscribe();
    }

    this.selectedFilterData = {};
    this.selectedFilterCount = 0;

    this.selectedFilterData.kanban = this.kanban;
    this.selectedFilterData['sprintIncluded'] = ['CLOSED', 'ACTIVE', 'FUTURE'];
    this.filter_kpiRequest = this.http_service.getFilterData(this.selectedFilterData)
      .subscribe(filterData => {
        if (filterData[0] !== 'error' && filterData?.['data']?.length > 0) {
          this.filterData = filterData['data'];
          this.projectListArr = this.sortAlphabetically(this.filterData.filter(x => x.labelName.toLowerCase() == 'project'));
          this.squadListArr = this.getSortedAdditonalFilter(this.projectListArr);
          this.projectListArr = this.helperService.makeUniqueArrayList(this.projectListArr);
          this.squadListArr = this.helperService.makeUniqueArrayList(this.squadListArr);
          const defaultSelection = this.selectedProjectBaseConfigId ? false : true;
          this.checkDefaultFilterSelection(defaultSelection);
          if (!Object.keys(filterData).length) {
            this.resetProjectSelection();
            // show error message
            this.messageService.add({ severity: 'error', summary: 'Projects not found.' });
          }

        } else {
          this.resetProjectSelection();
          // show error message
          this.messageService.add({ severity: 'error', summary: 'Error in fetching filter data. Please try after some time.' });
        }
        this.loader = false;
      });
  }

  sortAlphabetically(objArray) {
    objArray?.sort((a, b) => a.nodeName.localeCompare(b.nodeName));
    return objArray;
  }

  checkDefaultFilterSelection(flag) {
    if (flag) {
      this.trendLineValueList = [...this.projectListArr];
      this.filterForm?.get('selectedProjectValue').setValue(this.trendLineValueList?.[0]['nodeId']);
      this.handleIterationFilters('project');
    } else {
      this.getProjectBasedData();
    }
  }

  handleIterationFilters(level) {
    if (this.filterForm?.get('selectedProjectValue')?.value != '') {
      this.isToggleEnableForSelectedProject = false;
      this.tableLoader = true;
      this.noData = false;
      this.selectedSprintDetails = {};
      this.capacityScrumData = [];
      this.capacityKanbanData = [];
      if (level?.toLowerCase() == 'project') {
        const selectedProject = this.filterForm?.get('selectedProjectValue')?.value;
        this.projectDetails = { ...this.trendLineValueList.find(i => i.nodeId === selectedProject) };
        this.selectedProjectBaseConfigId = this.projectDetails?.basicProjectConfigId;
        this.getSquadsOfSelectedProject(this.projectDetails?.nodeId);
        this.getProjectBasedData();
        this.isAdminForSelectedProject = this.getAuthorizationService.checkIfSuperUser() || !this.getAuthorizationService.checkIfViewer(this.projectDetails);
      }
    }
  }

  getProjectBasedData() {
    if (this.selectedProjectBaseConfigId) {
      this.getCapacityData(this.selectedProjectBaseConfigId);
    }
  }

  getSquadsOfSelectedProject(projectNodeId) {
    if (projectNodeId) {
      let sprintData = this.getGridData();
      let sprintNodeIdList = new Set(sprintData.map(sprint => sprint.sprintNodeId));
      this.selectedSquad = [...this.squadListArr?.filter((x) => {
        // sprintNodeIdList.has(x['parentId'])
        for (const sprintId of sprintNodeIdList) {
          if(x['parentId'].includes(sprintId)) {
            return x;
          }
        }
      })];
    }
  }

  getCapacityData(projectId) {
    this.http_service.getCapacityData(projectId).subscribe((response) => {
      if (response && response?.success && response?.data) {
        if (this.kanban) {
          this.capacityKanbanData = response?.data;
          if (this.capacityKanbanData?.length > 0) {
            this.noData = false;
            this.checkifAssigneeToggleEnabled(this.capacityKanbanData);
          } else {
            this.noData = true;
          }
        } else {
          this.capacityScrumData = response?.data;
          if (this.capacityScrumData?.length > 0) {
            this.noData = false;
            this.checkifAssigneeToggleEnabled(this.capacityScrumData);
          } else {
            this.noData = true;
          }
        }
        this.tableLoader = false;
        this.getSquadsOfSelectedProject(projectId);
      } else {
        this.tableLoader = false;
        this.noData = true;
      }
    });
  }

  checkifAssigneeToggleEnabled(capacityData) {
    if (capacityData[0]['assigneeDetails']) {
      this.isToggleEnableForSelectedProject = true;
      this.getAssigneeRoles();
      this.getCapacityJiraAssignee(capacityData[0]['basicProjectConfigId']);
    }
  }

  getAssigneeRoles() {
    if (this.projectAssigneeRoles.length <= 0) {
      this.http_service.getAssigneeRoles()
        .subscribe(response => {
          if (response && response?.success && response?.data) {
            this.projectAssigneeRolesObj = response.data;
            for (const key in response.data) {
              this.projectAssigneeRoles.push({ name: response.data[key], value: key });
            }
          } else {
            this.messageService.add({ severity: 'error', summary: 'Error in fetching Assignee Roles.' });
          }
        });
    }
  }

  getCapacityJiraAssignee(projectId) {
    if (Object.keys(this.projectJiraAssignees).length <= 0 || (this.projectJiraAssignees['basicProjectConfigId'] !== projectId)) {
      this.jiraAssigneeLoader = true;
      this.http_service.getJiraProjectAssignee(projectId)
        .subscribe(response => {
          this.jiraAssigneeLoader = false;
          if (response && response?.success && response?.data) {
            this.projectJiraAssignees = response['data'];
            this.http_service.getAssigneeEmails(projectId)
              .subscribe(response => {
                if (response && response?.success && response?.data) {
                  this.projectAssigneeEmails = response.data;
                  this.projectAssigneeEmailsCopy = [...this.projectAssigneeEmails].map(x => {
                    return {
                      'name': x,
                      'value': x
                    }
                  });
                } else {
                  this.messageService.add({ severity: 'error', summary: 'Could not fetch Assignee Emails.' });
                }

              });
          } else {
            this.messageService.add({ severity: 'error', summary: 'Error in fetching Project Assignee.' });
          }
        });
    }
  }

  manageAssignees(selectedSprintData) {
    this.selectedSprintDetails = selectedSprintData;
    this.generateManageAssigneeData(selectedSprintData);
    this.displayAssignee = true;
  }

  generateManageAssigneeData(selectedSprintData) {
    this.manageAssigneeList = [];
    const projectAssignees = JSON.parse(JSON.stringify(this.projectJiraAssignees['assigneeDetailsList']));
    const assigneeCapactiy = selectedSprintData['assigneeCapacity'];
    assigneeCapactiy?.forEach(assignee => {
      const selectedAssigneeIndex = projectAssignees.findIndex(jiraAssignee => assignee.userId === jiraAssignee.name);
      if (selectedAssigneeIndex !== -1) {
        this.manageAssigneeList.push({ ...projectAssignees[selectedAssigneeIndex], checked: true });
        projectAssignees.splice(selectedAssigneeIndex, 1);
      }
    });
    this.manageAssigneeList.push(...projectAssignees);
  }

  onAssigneeModalOpen() {
    this.manageAssignee.reset();
  }

  addRemoveAssignees() {
    this.displayAssignee = false;
    this.manageAssigneeList = this.manageAssigneeList.filter(assignee => assignee?.checked);
    const assigneeCapacity = [];
    this.manageAssigneeList?.forEach(assignee => {
      const assigneePresentForSprint = this.selectedSprintDetails['assigneeCapacity']?.find(selectedAssignee => selectedAssignee.userId === assignee.name);
      if (assigneePresentForSprint) {
        assigneeCapacity.push(assigneePresentForSprint);
      } else {
        assigneeCapacity.push({
          userId: assignee.name,
          userName: assignee.displayName
        });
      }
    });
    const postData = { ...this.selectedSprintDetails };
    delete postData['sprintState'];
    postData['assigneeCapacity'] = assigneeCapacity;

    this.http_service.saveOrUpdateAssignee(postData)
      .subscribe(response => {
        if (response && response?.success && response?.data) {
          if (!this.kanban) {
            this.sendSprintHappinessIndexForAddOrRemove(postData);
          } else {
            this.getCapacityData(this.selectedSprintDetails['basicProjectConfigId']);
            const expandedRowsKey = this.kanban ? this.selectedSprintDetails.startDate : this.selectedSprintDetails.sprintNodeId;
            this.expandedRows = { [expandedRowsKey]: true };
            this.messageService.add({ severity: 'success', summary: 'Assignee Details saved successfully!' });
          }
        } else {
          this.messageService.add({ severity: 'error', summary: 'Error in Saving Assignee Details. Please try after sometime!' });
        }
      });
  }

  sendSprintHappinessIndexForAddOrRemove(capacitySaveData) {
    const postData = {
      basicProjectConfigId: capacitySaveData['basicProjectConfigId'],
      sprintID: capacitySaveData['sprintNodeId'],
      userRatingList: capacitySaveData['assigneeCapacity'].map((assignee) => ({
        userId: assignee['userId'],
        userName: assignee['userName'],
        rating: assignee['happinessRating'] ? assignee['happinessRating'] : 0,
      })),
    };

    this.http_service
      .saveOrUpdateSprintHappinessIndex(postData)
      .subscribe((response) => {
        if (response && response?.success && response?.data) {
          this.getCapacityData(
            this.selectedSprintDetails['basicProjectConfigId'],
          );
          const expandedRowsKey = this.kanban
            ? this.selectedSprintDetails.startDate
            : this.selectedSprintDetails.sprintNodeId;
          this.expandedRows = { [expandedRowsKey]: true };
          this.messageService.add({
            severity: 'success',
            summary: 'Assignee Details saved successfully!',
          });
        } else {
          this.messageService.add({
            severity: 'error',
            summary:
              'Error in Saving Assignee Details. Please try after sometime!',
          });
        }
      });
  }

  onCapacitySprintRowSelection() {
    if (this.projectCapacityEditMode) {
      this.onSprintCapacityCancel(this.selectedSprint);
      this.projectCapacityEditMode = false;
    }
  }

  onSprintCapacityCancel(selectedSprint) {
    this.projectCapacityEditMode = false;
    if (!this.kanban) {
      const selectedSprintRowIndex = this.capacityScrumData.findIndex(sprint => sprint.sprintNodeId === selectedSprint.sprintNodeId);
      this.capacityScrumData[selectedSprintRowIndex] = this.selectedSprint;
    } else {
      const selectedSprintRowIndex = this.capacityKanbanData.findIndex(sprint => (sprint.startDate === selectedSprint.startDate && sprint.endDate === selectedSprint.endDate));
      this.capacityKanbanData[selectedSprintRowIndex] = this.selectedSprint;
    }

  }

  onSprintCapacityEdit(selectedSprint) {
    this.projectCapacityEditMode = true;
    this.selectedSprint = JSON.parse(JSON.stringify(selectedSprint));
    this.selectedSprintAssigneFormArray = [];
    selectedSprint.assigneeCapacity.forEach(assignee => {
      this.selectedSprintAssigneFormArray.push(
        {
          email: new FormControl(assignee.email, [Validators.email]),
          role: new FormControl(assignee.role),
          squad: new FormControl(assignee.squad),
          plannedCapacity: new FormControl({ value: assignee.plannedCapacity, disabled: !assignee.role }, [Validators.pattern('[0-9]*')]),
          leaves: new FormControl({ value: assignee.leaves, disabled: !(assignee?.role && assignee?.plannedCapacity) }, [Validators.min(0), Validators.max(assignee.plannedCapacity)])
        }
      );
    });
  }


  calculateAvaliableCapacity(assignee, assigneeFormControls, fieldName) {
    assignee[fieldName] = assigneeFormControls[fieldName]?.value;

    if (this.checkMandatoryFields(assigneeFormControls, fieldName)) {
      assigneeFormControls.plannedCapacity.enable();
    } else {
      if (assigneeFormControls.plannedCapacity.value > 0) {
        assigneeFormControls.leaves.setValidators([Validators.max(assignee.plannedCapacity)]);
        assigneeFormControls.leaves.enable();
        let totalCapacity = assignee.plannedCapacity - assignee.leaves;
        assignee.availableCapacity = Math.round(totalCapacity * 100) / 100;
      } else {
        assigneeFormControls.leaves.setValue(0);
        assigneeFormControls.leaves.disable();
        assignee.leaves = 0;
        assignee.availableCapacity = 0;
      }
      this.cdr.detectChanges();
      const currentAssigneeExist = this.selectedSprintAssigneValidator.findIndex(selectedassignee => selectedassignee === assignee);
      if (assigneeFormControls['leaves'].status === 'INVALID') {
        if (currentAssigneeExist === -1 && assignee.leaves > assignee.plannedCapacity) {
          this.selectedSprintAssigneValidator.push(assignee);
        }
      } else {
        if (currentAssigneeExist !== -1) {
          this.selectedSprintAssigneValidator.splice(currentAssigneeExist, 1);
        }
      }
    }
  }

  validateInput($event, field?) {
    if ($event.key === '-' || (field !== 'email' && $event.key === 'e')) {
      $event.preventDefault();
    }
  }

  onSprintCapacitySave(selectedSprint) {
    selectedSprint.capacity = this.calculateTotalCapacityForSprint(selectedSprint);
    selectedSprint.additionalFilterCapacityList = this.generateAdditionalFilterCapacityList(selectedSprint);
    this.projectCapacityEditMode = false;
    const postData = { ...selectedSprint };
    delete postData['id'];
    delete postData['projectName'];
    delete postData['sprintState'];
    this.http_service.saveOrUpdateAssignee(postData).subscribe(response => {
      if (response && response?.success && response?.data) {
        if (!this.kanban) {
          this.sendSprintHappinessIndex(selectedSprint);
        } else {
          this.getCapacityData(selectedSprint['basicProjectConfigId']);
          this.messageService.add({ severity: 'success', summary: 'Assignee Details saved successfully!' });
        }
      } else {
        this.messageService.add({ severity: 'error', summary: 'Error in Saving Assignee Details. Please try after sometime!' });
      }
    });
  }

  calculateTotalCapacityForSprint(selectedSprint) {
    let totalCapacity = 0;
    selectedSprint.assigneeCapacity.forEach(assignee => {
      totalCapacity += assignee?.availableCapacity ? assignee?.availableCapacity : 0;
    });
    return Math.round(totalCapacity * 100) / 100;
  }

  sendSprintHappinessIndex(selectedSprint) {
    const postData = {
      basicProjectConfigId: selectedSprint['basicProjectConfigId'],
      sprintID: selectedSprint['sprintNodeId'],
      userRatingList: selectedSprint['assigneeCapacity'].map(assignee => ({ userId: assignee['userId'], userName: assignee['userName'], rating: assignee['happinessRating'] ? assignee['happinessRating'] : 0 }))
    };

    this.http_service.saveOrUpdateSprintHappinessIndex(postData).subscribe(response => {
      if (response && response?.success && response?.data) {
        this.projectCapacityEditMode = false;
        this.getCapacityData(selectedSprint['basicProjectConfigId']);
        this.messageService.add({ severity: 'success', summary: 'Assignee Details saved successfully!' });
      } else {
        this.messageService.add({ severity: 'error', summary: 'Error in Saving Assignee Details. Please try after sometime!' });
      }
    });
  }


  enableDisableSubmitButton() {
    if (this.selectedView === 'upload_Sprint_Capacity') {
      if (this.selectedSquad.length > 0 && this.squadForm != null) {
        this.isCapacitySaveDisabled = false;
        this.capacityErrorMessage = '';
        Object.entries(this.squadForm?.value).forEach(([key, value]) => {
          if (value == null || (value instanceof FormControl && value.value == null)) {
            this.isCapacitySaveDisabled = true;
            this.capacityErrorMessage = 'Please enter Capacity';
            return;
          }
        });

      }
      else {
        this.enableDisableCapacitySubmitButton();
      }
    }
  }


  enableDisableCapacitySubmitButton() {
    if (this.popupForm.get('capacity')?.value && this.popupForm.get('capacity')?.value === 'Enter Value') {
      this.isCapacitySaveDisabled = true;
      this.capacityErrorMessage = 'Please enter Capacity';
      return;
    }
    if (!(!!this.popupForm.get('capacity')?.value)) {
      this.isCapacitySaveDisabled = true;
      if (parseInt(this.popupForm.get('capacity')?.value) === 0) {
        this.capacityErrorMessage = 'Capacity Should not be 0';
      } else {
        this.capacityErrorMessage = 'Please enter Capacity';
      }
      return;
    }
    this.isCapacitySaveDisabled = false;
    this.capacityErrorMessage = '';

  }

  AddOrUpdateData(data) {
    this.showPopuup = true;
    this.executionDate = data?.executionDate ? data?.executionDate : '';
    this.selectedSprintName = data?.sprintName;
    this.selectedSprintId = data?.sprintNodeId;
    this.startDate = data?.startDate;
    this.endDate = data?.endDate;
    this.reqObj = {
      projectNodeId: data?.projectNodeId,
      projectName: data?.projectName,
      kanban: this.kanban,
      basicProjectConfigId: data?.basicProjectConfigId
    };
    if (!this.kanban) {
      this.reqObj['sprintNodeId'] = this.selectedSprintId;
    }
    if (this.selectedView === 'upload_Sprint_Capacity') {

      if (this.selectedSquad.length > 0) {

        if (data?.additionalFilterCapacityList) {
          const flattened = data.additionalFilterCapacityList.flatMap(item => item.nodeCapacityList);
          this.selectedSquad.forEach(squad => {
            const matchingFilter = flattened.find(filter => filter.additionalFilterId === squad.nodeId);

            if (matchingFilter) {
              if (!this.squadForm.controls[squad.nodeId]) {
                this.squadForm.addControl(squad.nodeId, new UntypedFormControl(matchingFilter.additionalFilterCapacity));
              } else {
                this.squadForm.controls[squad.nodeId].setValue(matchingFilter.additionalFilterCapacity);
              }
            } else {
              if (!this.squadForm.controls[squad.nodeId]) {
                this.squadForm.addControl(squad.nodeId, new UntypedFormControl(null));
              }
            }

          });
        }
        else {
          this.selectedSquad.forEach(squad => {
            let control = new UntypedFormControl();
            if (this.squadForm.get(squad.nodeId)) {
              // Update existing control
              this.squadForm.get(squad.nodeId).setValue(control);
            } else {
              // Add new control
              this.squadForm.addControl(squad.nodeId, control);
            }
          });
        }
      }

      this.popupForm = new UntypedFormGroup({ capacity: new UntypedFormControl(data?.capacity ? data?.capacity : '') });

      this.reqObj['capacity'] = data?.capacity ? data?.capacity : '';
      if (this.kanban) {
        this.reqObj['startDate'] = data?.startDate;
        this.reqObj['endDate'] = data?.endDate;
      }
    }
    this.enableDisableSubmitButton();
  }

  enterNumericValue(event) {
    if (!!event && !!event.preventDefault && event.key === '.' || event.key === 'e' || event.key === '-' || event.key === '+') {
      event.preventDefault();
      return;
    }
    this.enableDisableSubmitButton();
  }

  // called on the click of the Submit button when creating capacity per sprint(hrs)
  submitCapacity() {
    if (this.squadForm?.value && Object.keys(this.squadForm.value).length > 0) {
      this.reqObj['additionalFilterCapacityList'] = this.toggleOffGenerateAdditionalFilterCapacityList(this.squadForm?.value)
      this.reqObj['capacity'] = Object.values(this.squadForm?.value).reduce((acc: number, value: number) => acc + value, 0).toString();
    }
    else {
      this.reqObj['capacity'] = this.popupForm?.get('capacity').value;
    }
    this.http_service.saveCapacity(this.reqObj)
      .subscribe(response => {
        if (response.success) {
          this.selectedFilterData = {};
          this.startDate = '';
          this.endDate = '';
          this.capacityErrorMessage = '';
          this.isCapacitySaveDisabled = true;
          this.setFormValuesEmpty();
          this.messageService.add({ severity: 'success', summary: 'Capacity saved.', detail: '' });
          this.getFilterDataOnLoad();
        } else if (!response.success && !!response.message && response.message === 'Unauthorized') {
          this.messageService.add({ severity: 'error', summary: 'You are not authorized.' });
        } else {
          this.messageService.add({ severity: 'error', summary: 'Error in saving scenario. Please try after some time.' });
        }
        this.showPopuup = false;
        this.isCapacitySaveDisabled = true;
        this.capacityErrorMessage = '';
      });

  }

  setFormValuesEmpty() {
    if (this.filterForm && this.filterForm.controls) {
      Object.keys(this.filterForm?.controls).forEach(key => {
        if (this.filterForm.get(key) && key !== 'selectedProjectValue') {
          this.filterForm?.get(key)?.setValue('');
        }
      });
    }
    if (this.popupForm && this.popupForm.controls) {
      Object.keys(this.popupForm?.controls).forEach(key => {
        if (this.popupForm.get(key)) {
          this.popupForm?.get(key)?.setValue('');
        }
      });
    }

    if (this.reqObj) {
      for (const capReqField in this.reqObj) {
        this.reqObj[capReqField] = '';
      }
    }

    this.clearSquadForm();
  }

  clearSquadForm() {
    if (this.squadForm && this.squadForm.controls) {
      Object.keys(this.squadForm?.controls).forEach(key => {
        this.squadForm.removeControl(key);
      });

    }
  }


  numericInputUpDown(event: any) {
    if (parseInt(event.target.value) < 0) {
      setTimeout(() => {
        this[event.target.name] = '';
        event.target.value = '';
        this.enableDisableSubmitButton();
      }, 0);
    } else {
      this.enableDisableSubmitButton();
    }
  }

  checkMandatoryFields(assigneeFormControls, fieldName) {
    if (fieldName === 'role' || fieldName === 'squad') {
      const roleValue = assigneeFormControls['role']?.value;
      const squadValue = assigneeFormControls['squad']?.value;
      if (this.selectedSquad.length > 0) {
        if ((squadValue !== null) && (roleValue !== null)) {
          return true;
        }
      }
      else if (roleValue !== null) {
        return true;
      }
    }
    return false;
  }

  generateAdditionalFilterCapacityList(selectedSprint) {
    if (this.selectedSquad.length > 0) {
      const squadCapacityMap: { [key: string]: { [key: string]: number } } = {};

      // Group by squad and sum availableCapacity
      selectedSprint.assigneeCapacity.forEach(member => {
        const squad = this.selectedSquad.find(squad => squad.nodeId === member.squad);
        if (squad) {
          if (!squadCapacityMap[squad.labelName]) {
            squadCapacityMap[squad.labelName] = {};
          }
          if (!squadCapacityMap[squad.labelName][squad.nodeId]) {
            squadCapacityMap[squad.labelName][squad.nodeId] = 0;
          }
          squadCapacityMap[squad.labelName][squad.nodeId] += member.availableCapacity;
        }
      });

      return this.createAdditionalFilterCapacityList(squadCapacityMap);
    }

  }

  toggleOffGenerateAdditionalFilterCapacityList(capacityObject: { [key: string]: number }) {
    if (this.selectedSquad.length > 0) {
      const squadCapacityMap: { [key: string]: { [key: string]: number } } = {};

      // Group by squad and sum availableCapacity
      for (const key in capacityObject) {
        const value = capacityObject[key];
        const squad = this.selectedSquad.find(squad => key.includes(squad.nodeId));
        if (squad) {
          if (!squadCapacityMap[squad.labelName]) {
            squadCapacityMap[squad.labelName] = {};
          }
          if (!squadCapacityMap[squad.labelName][squad.nodeId]) {
            squadCapacityMap[squad.labelName][squad.nodeId] = 0;
          }
          squadCapacityMap[squad.labelName][squad.nodeId] += value;
        }
      }
      return this.createAdditionalFilterCapacityList(squadCapacityMap);
    }
  }

  createAdditionalFilterCapacityList(squadCapacityMap) {
    const additionalFilterCapacityList = [];

    for (const labelName in squadCapacityMap) {
      const nodeCapacityList = Object.keys(squadCapacityMap[labelName]).map(nodeId => {
        return {
          additionalFilterId: nodeId,
          additionalFilterCapacity: squadCapacityMap[labelName][nodeId]
        };
      });

      additionalFilterCapacityList.push({
        filterId: labelName,
        nodeCapacityList: nodeCapacityList
      });
    }

    return additionalFilterCapacityList;

  }

  getNodeName(assignee) {
    if (assignee?.squad) {
      const squad = this.selectedSquad.find(s => s.nodeId === assignee.squad);
      return squad ? squad.nodeName : '- -';
    }
    return '- -';
  }

  getSortedAdditonalFilter(projectListArr) {
    //Get the levels of the projects in projectListArr
    let projectMap = projectListArr.map(project => project.level);
    // Filter out the objects from filterData which have a level that is exactly 2 levels above any project level
    return this.sortAlphabetically(this.filterData.filter(data => projectMap.includes(data.level - 2)));

  }

  getGridColumns() {
    return this.kanban ? this.cols.capacityKanbanKeys : this.cols.capacityScrumKeys;
  }

  checkIfGridDataIdEmpty() {
    if (this.kanban) {
      return this.capacityKanbanData?.length > 0;
    } else {
      return this.capacityScrumData?.length > 0
    }
  }

  getGridData() {
    return this.kanban ? this.capacityKanbanData : this.capacityScrumData;
  }

  getDataKey() {
    return this.kanban ? 'startDate' : 'sprintNodeId';
  }

  getExpandedClass(item, expanded) {

    if (this.kanban) {
      return {
        'tr-active': (item) === true, //(item | comparedates
        'row-expanded': expanded
      }
    } else {
      return {
        'tr-active': item?.sprintState?.toLowerCase() === 'active',
        'row-expanded': expanded
      }
    }

  }

}
