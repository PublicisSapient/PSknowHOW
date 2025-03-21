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
import { SharedService } from '../../../services/shared.service';
import { Router } from '@angular/router';
import { ConfirmationService, MessageService } from 'primeng/api';
import { HttpService } from '../../../services/http.service';
import { GetAuthorizationService } from 'src/app/services/get-authorization.service';
import { KeyValue } from '@angular/common';

interface Control {
  name: string;
  description: string;
  actionItem: string;
}

@Component({
  selector: 'app-project-settings',
  templateUrl: './project-settings.component.html',
  styleUrls: ['./project-settings.component.css']
})

export class ProjectSettingsComponent implements OnInit {
  userProjects = [];
  selectedProject: any;
  projectOnHold: boolean = false;
  developerKpiEnabled: boolean = false;
  generalControls: Control[];
  oneTimeControls: Control[];
  apiControls: Control[];
  projectConfirm: boolean;
  cols: any[];
  allProjectList: any[];
  loading: boolean;
  projectList: any;
  globalSearchFilter: any;
  isAssigneeSwitchChecked: boolean = false;
  tokenCopied: boolean = false;
  generateTokenLoader: boolean = false;
  userName: string;
  displayGeneratedToken: boolean = false;
  generatedToken: string = '';
  isProjectAdmin: boolean = false;
  isSuperAdmin: boolean = false;
  isDeleteClicked: boolean = false;

  constructor(
    public sharedService: SharedService,
    public router: Router,
    private confirmationService: ConfirmationService,
    public httpService: HttpService,
    private messageService: MessageService,
    public getAuthorizationService: GetAuthorizationService,
  ) { }

  ngOnInit(): void {
    this.getData();
    this.generalControls = [
      {
        name: 'Pause data collection',
        description: 'Pause data collection through tool connections to control when data is gathered from your integrated tools',
        actionItem: 'switch',
      },
      {
        name: 'Delete project',
        description: 'Delete all project data - collected tools data, user permissions, uploaded data, etc',
        actionItem: 'cta',
      },
    ];

    this.oneTimeControls = [
      {
        name: 'Enable People performance KPIs',
        description: 'Enable fetching people info from Agile PM tool or Repo tool connection',
        actionItem: 'switch-people-kpi',
      },
      {
        name: 'Enable Developer KPIs',
        description: 'Provide consent to clone your code repositories (BitBucket, GitLab, GitHub, Azure Repository) to avoid API rate-limiting issues. The repository for this project will be cloned on the KH Server. This will grant access to valuable KPIs on the Developer dashboard.',
        actionItem: 'switch-developer-kpi',
      }
    ];

    this.apiControls = [
      {
        name: 'Generate API token',
        description: 'You can generate KnowHOW POST API token to upload tools data directly',
        actionItem: 'button',
      },
    ];

    this.userName = this.sharedService.getCurrentUserDetails('user_name');

    this.isProjectAdmin = this.getAuthorizationService.checkIfProjectAdmin();
    this.isSuperAdmin = this.getAuthorizationService.checkIfSuperUser();
  }

  onProjectActiveStatusChange(event) {
    if (event.checked) {
      this.confirmationService.confirm({
        message: `Are you sure you want to keep this project on hold?`,
        header: 'Pause data collection',
        key: 'confirmToEnableDialog',
        accept: () => {
          this.updateProjectDetails('Project data collection paused!');
        },
        reject: () => {
          this.projectOnHold = false;
        }
      });
    } else {
      this.confirmationService.confirm({
        message: `Are you sure you want to resume activities for this project?`,
        header: 'Resume data collection',
        key: 'confirmToEnableDialog',
        accept: () => {
          this.updateProjectDetails('Project data collection resumed!');
        },
        reject: () => {
          this.projectOnHold = true;
        }
      });
    }
  }

  onProjectDevKpiStatusChange() {
    this.confirmationService.confirm({
      message: `Once enabled, it cannot be disabled. Do you want to enable Developer KPIs for this project, are you sure?`,
      header: 'Enable Developer KPIs',
      key: 'confirmToEnableDialog',
      accept: () => {
        this.updateProjectDetails('Developer KPI for this project enabled!');
      },
      reject: () => {
        this.developerKpiEnabled = false;
      }
    });
  }

  getData() {
    this.cols = [];
    this.allProjectList = [];
    this.loading = true;
    this.httpService.getProjectListData().subscribe(responseList => {
      if (responseList[0].success) {
        this.projectList = responseList[0]?.data;
        this.fillColumns();
        this.loading = false;
        this.sharedService.setProjectList(this.allProjectList);
        this.getProjects();
      } else {
        this.loading = false;
        this.messageService.add({
          severity: 'error',
          summary: 'Some error occurred. Please try again later.'
        });
      }
    });
  }

  fillColumns() {
    if (this.projectList?.length > 0) {
      for (let i = this.projectList[0]?.hierarchy?.length - 1; i >= 0; i--) {
        const obj = {
          id: this.projectList[0]?.hierarchy[i]?.hierarchyLevel['hierarchyLevelId'],
          heading: this.projectList[0]?.hierarchy[i]?.hierarchyLevel['hierarchyLevelName']
        };
        this.cols?.push(obj);
      }

      for (let i = this.projectList[0]?.hierarchy?.length - 1; i >= 0; i--) {
        const obj = {
          id: this.projectList[0]?.hierarchy[i]?.hierarchyLevel['hierarchyLevelId'],
          heading: this.projectList[0]?.hierarchy[i]?.hierarchyLevel['hierarchyLevelName']
        };
        this.cols?.push(obj);
      }
      const projectObj = {
        id: 'name',
        heading: 'Project'
      };
      this.cols?.unshift(projectObj);
      this.setProjectType();
    }
  }

  setProjectType() {
    const typeObj = {
      id: 'type',
      heading: 'Type'
    };
    this.cols?.push(typeObj);
    for (let i = 0; i < this.cols?.length; i++) {
      this.globalSearchFilter?.push(this.cols[i]?.id);
    }

    for (let i = 0; i < this.projectList?.length; i++) {
      const obj = {
        id: this.projectList[i]?.id,
        name: this.projectList[i]?.projectDisplayName,
        type: this.projectList[i]?.kanban ? 'Kanban' : 'Scrum',
        saveAssigneeDetails: this.projectList[i]?.saveAssigneeDetails,
        developerKpiEnabled: this.projectList[i]?.developerKpiEnabled,
        projectOnHold: this.projectList[i]?.projectOnHold,
      };
      for (let j = 0; j < this.projectList[i]?.hierarchy?.length; j++) {
        obj[this.projectList[i]?.hierarchy[j]?.hierarchyLevel['hierarchyLevelId']] = this.projectList[i]?.hierarchy[j]?.value;
      }
      this.allProjectList?.push(obj);
    }
  }

  getProjects() {
    this.userProjects = this.sharedService.getProjectList();
    if (this.userProjects != null && this.userProjects.length > 0) {
      this.userProjects.sort((a, b) => a.name.localeCompare(b.name, undefined, { numeric: true }));
    }
    this.selectedProject = this.sharedService.getSelectedProject();
    if (this.selectedProject && this.router.url?.includes(this.selectedProject['id']) && !this.isDeleteClicked) {
      this.selectedProject = this.userProjects.filter((x) => x.id == this.selectedProject?.id)[0]
    } else {
      this.selectedProject = this.userProjects[0];
    }
    this.isAssigneeSwitchChecked = this.selectedProject?.saveAssigneeDetails;
    this.developerKpiEnabled = this.selectedProject?.developerKpiEnabled;
    this.projectOnHold = this.selectedProject?.projectOnHold;

    this.hierarchyLabelNameChange();

  }

  updateProjectSelection() {
    this.sharedService.setSelectedProject(this.selectedProject);
    this.router.navigate([`/dashboard/Config/ConfigSettings/${this.selectedProject?.id}`], { queryParams: { 'type': this.selectedProject.type.toLowerCase(), tab: 0 } });
    this.isAssigneeSwitchChecked = this.selectedProject?.saveAssigneeDetails;
    this.developerKpiEnabled = this.selectedProject?.developerKpiEnabled;
    this.projectOnHold = this.selectedProject?.projectOnHold;

    this.hierarchyLabelNameChange();
  }

  hierarchyLabelNameChange() {
    const selectedType = this.selectedProject?.type !== 'Scrum' ? 'kanban' : 'scrum';
    const levelDetails = JSON.parse(localStorage.getItem('completeHierarchyData'))[selectedType]?.map((x) => {
      return {
        id: x['hierarchyLevelId'],
        name: x['hierarchyLevelName']
      }
    });

    setTimeout(() => {
      if (this.selectedProject && Object.keys(this.selectedProject)?.length) {
        Object.keys(this.selectedProject).forEach(key => {
          if (levelDetails?.map(x => x.id).includes(key)) {
            let propertyName = levelDetails.filter(x => x.id === key)[0].name;
            this.selectedProject[propertyName] = this.selectedProject[key];
            delete this.selectedProject[key];
          }
        });
      }
    });
  }

/**
 * Deletes a specified project after user confirmation.
 * It updates the project access list and navigates to the configuration settings of the first user project.
 * 
 * @param {Project} project - The project object to be deleted.
 * @returns {void}
 * @throws {HttpErrorResponse} If the deletion request fails.
 */
  deleteProject(project) {
    this.isDeleteClicked = true;
    this.projectConfirm = true;
    this.confirmationService.confirm({
      message: this.getAlertMessageOnClickDelete(),
      header: `Delete ${project.name}?`,
      icon: 'pi pi-info-circle',
      accept: () => {
        this.httpService.deleteProject(project).subscribe(response => {
          this.projectDeletionStatus(response);
          this.router.navigate([`/dashboard/Config/ConfigSettings/${this.userProjects[0]?.id}`], { queryParams: { 'type': this.selectedProject?.type?.toLowerCase(), tab: 0 } });
          this.selectedProject = this.userProjects[0];
          let arr = this.sharedService.getCurrentUserDetails('projectsAccess');
          if (arr?.length) {
            arr?.map((item) => {
              item.projects = item.projects.filter(x => x.projectId != project.id);
            });
            arr = arr?.filter(item => item.projects?.length > 0);

            this.httpService.setCurrentUserDetails({ projectsAccess: arr });
          }
        }, error => {
          this.projectDeletionStatus(error);
        });
      },
      reject: () => {

      }
    });
  }

  projectDeletionStatus(data) {
    this.projectConfirm = false;
    if (data.success) {
      this.getData();
      this.confirmationService.confirm({
        message: data.message,
        header: 'Project Deletion Status',
        icon: 'fa fa-check-circle alert-success',
        accept: () => {
          console.log('accept')
        },
        reject: () => {
          console.log('reject')
        }
      });
    } else {
      this.confirmationService.confirm({
        message: 'Something went wrong. Please try again after sometime.',
        header: 'Project Deletion Status',
        icon: 'fa fa-times-circle alert-danger',
        accept: () => {
          console.log('accept')
        },
        reject: () => {
          console.log('reject')
        }
      });
    }
  }

  getAlertMessageOnClickDelete() {
    const commonMsg = 'Project and related data will be deleted forever, are you sure you want to delete it?';
    return commonMsg;
  }

  onAssigneeSwitchChange() {
    this.confirmationService.confirm({
      message: `Once enabled, it cannot be disabled. Do you want to enable individual KPIs for this project, are you sure?`,
      header: 'Enable Individual KPIs',
      key: 'confirmToEnableDialog',
      accept: () => {
        this.updateProjectDetails('Assignee Switch Enabled  successfully.');
      },
      reject: () => {
        this.isAssigneeSwitchChecked = false;
      }
    });
  }

  updateProjectDetails(successMsg) {

    const updatedDetails = {...this.projectList.filter(x => x.projectDisplayName === this.selectedProject.name)[0]};
    updatedDetails['saveAssigneeDetails'] = this.isAssigneeSwitchChecked;
    updatedDetails["createdAt"] = new Date().toISOString();
    updatedDetails["developerKpiEnabled"] = this.developerKpiEnabled;
    updatedDetails["projectOnHold"] = this.projectOnHold;

    this.httpService.updateProjectDetails(updatedDetails, this.selectedProject.id).subscribe(response => {

      if (response && response.serviceResponse && response.serviceResponse.success) {
        this.selectedProject.projectOnHold = this.projectOnHold;
        this.messageService.add({
          severity: 'success',
          summary: successMsg
        });
      } else {
        this.isAssigneeSwitchChecked = false;
        this.projectOnHold = false;
        this.messageService.add({
          severity: 'error',
          summary: 'Some error occurred. Please try again later.'
        });

      }

    })

  }

  generateTokenConfirmation() {
    this.confirmationService.confirm({
      message: `If you create a token, all previously generated tokens will expire, do you want to continue?`,
      header: `Generate Token?`,
      icon: 'pi pi-info-circle',
      key: 'confirmToEnableDialog',
      accept: () => {
        this.generateToken();
      },
      reject: null
    });
  }

  generateToken() {
    this.tokenCopied = false;
    this.generateTokenLoader = true;
    const projectDetails = this.selectedProject;
    const postData = {
      basicProjectConfigId: projectDetails?.id,
      projectName: projectDetails?.name,
      userName: this.userName
    };


    this.httpService.generateToken(postData).subscribe(response => {
      this.generateTokenLoader = false;
      this.displayGeneratedToken = true;
      if (response['success'] && response['data']) {
        this.generatedToken = response['data'].apiToken;
        this.messageService.add({ severity: 'success', summary: 'Token generated!' });
      } else {
        this.messageService.add({ severity: 'error', summary: 'Error occured while generating token. Please try after some time' });
      }
    });
  }

  copyToken() {
    if (this.generatedToken) {
      this.tokenCopied = true;
      navigator.clipboard.writeText(this.generatedToken);
      this.messageService.add({ severity: 'success', summary: 'Token copied!' });
    }
  }

  originalOrder = (a: KeyValue<number, string>, b: KeyValue<number, string>): number => 0;

}
