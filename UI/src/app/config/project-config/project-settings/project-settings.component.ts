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
import { Router, ActivatedRoute } from '@angular/router';
import { ConfirmationService, MessageService } from 'primeng/api';
import { HttpService } from '../../../services/http.service';
import { GetAuthorizationService } from 'src/app/services/get-authorization.service';

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
  isAssigneeSwitchDisabled: boolean;
  tokenCopied: boolean = false;
  generateTokenLoader: boolean = false;
  userName: string;
  displayGeneratedToken: boolean = false;
  generatedToken: string = '';
  isProjectAdmin: boolean = false;
  isSuperAdmin: boolean = false;
  isDeleteClicked: boolean = false;
  isDeveloperKpiSwitchDisabled: boolean;

  constructor(
    public sharedService: SharedService,
    public router: Router,
    private confirmationService: ConfirmationService,
    private httpService: HttpService,
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
        description: 'Provide consent to clone your code repositories (BitBucket, GitLab, GitHub) to avoid API rate-limiting issues. The repository for this project will be cloned on the KH Server. This will grant access to valuable KPIs on the Developer dashboard.',
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

    this.sharedService.currentUserDetailsObs.subscribe(details => {
      if (details) {
        this.userName = details['user_name'];
      }
    });

    this.isProjectAdmin = this.getAuthorizationService.checkIfProjectAdmin();
    this.isSuperAdmin = this.getAuthorizationService.checkIfSuperUser();

    // this.selectedProject = this.sharedService.getSelectedProject();
    // this.selectedProject = this.selectedProject !== undefined ? this.selectedProject : this.userProjects[0];
  }

  onProjectActiveStatusChange(event) {
    if(event.checked) {
      this.confirmationService.confirm({
        message: `Are you sure you want to keep this project on hold?`,
        header: 'Pause data collection',
        key: 'confirmToEnableDialog',
        accept: () => {
          this.updateProjectDetails();
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
          this.updateProjectDetails();
        },
        reject: () => {
          this.projectOnHold = true;
        }
      });
    }
  }

  onProjectDevKpiStatusChange() {
    if (this.developerKpiEnabled) {
      this.isDeveloperKpiSwitchDisabled = true;
    }
    this.confirmationService.confirm({
      message: `Once enabled, it cannot be disabled. Do you want to enable Developer KPIs for this project, are you sure?`,
      header: 'Enable Developer KPIs',
      key: 'confirmToEnableDialog',
      accept: () => {
        this.updateProjectDetails();
      },
      reject: () => {
        this.developerKpiEnabled = false;
        this.isDeveloperKpiSwitchDisabled = false;
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
        if (this.projectList?.length > 0) {
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
              name: this.projectList[i]?.projectName,
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

  getProjects() {
    this.userProjects = this.sharedService.getProjectList();
    if (this.userProjects != null && this.userProjects.length > 0) {
      this.userProjects.sort((a, b) => a.name.localeCompare(b.name, undefined, { numeric: true }));
    }
    this.selectedProject = this.sharedService.getSelectedProject();
    if (this.selectedProject && this.router.url.includes(this.selectedProject['id']) && !this.isDeleteClicked) {
      this.selectedProject = this.userProjects.filter((x) => x.id == this.selectedProject?.id)[0]
    } else {
      this.selectedProject = this.userProjects[0];
    }
    this.isAssigneeSwitchChecked = this.selectedProject?.saveAssigneeDetails;
    if (this.isAssigneeSwitchChecked) {
      this.isAssigneeSwitchDisabled = true;
    }
    this.developerKpiEnabled = this.selectedProject?.developerKpiEnabled;
    if(this.developerKpiEnabled) {
      this.isDeveloperKpiSwitchDisabled = true;
    }
    this.projectOnHold = this.selectedProject?.projectOnHold;
  }

  updateProjectSelection() {
    this.sharedService.setSelectedProject(this.selectedProject);
    this.router.navigate([`/dashboard/Config/ConfigSettings/${this.selectedProject['id']}`], { queryParams: { tab: 0 } });
    this.isAssigneeSwitchChecked = this.selectedProject?.saveAssigneeDetails;
    this.developerKpiEnabled = this.selectedProject?.developerKpiEnabled;
    this.projectOnHold = this.selectedProject?.projectOnHold;

    if (this.isAssigneeSwitchChecked) {
      this.isAssigneeSwitchDisabled = true;
    }
    if(this.developerKpiEnabled) {
      this.isDeveloperKpiSwitchDisabled = true;
    }
  }

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
          this.router.navigate([`/dashboard/Config/ConfigSettings/${this.userProjects[0]['id']}`], { queryParams: { tab: 0 } });
          this.selectedProject = this.userProjects[0];
          let arr = this.sharedService.getCurrentUserDetails('projectsAccess');
          if (arr?.length) {
            arr?.map((item) => {
              item.projects = item.projects.filter(x => x.projectId != project.id);
            });
            arr = arr?.filter(item => item.projects?.length > 0);
            console.log(arr)
            this.sharedService.setCurrentUserDetails({ projectsAccess: arr });
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
    // console.log(this.userProjects[this.userProjects.length-1]['id'])
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
    if (this.isAssigneeSwitchChecked) {
      this.isAssigneeSwitchDisabled = true;
    }
    this.confirmationService.confirm({
      message: `Once enabled, it cannot be disabled. Do you want to enable individual KPIs for this project, are you sure?`,
      header: 'Enable Individual KPIs',
      key: 'confirmToEnableDialog',
      accept: () => {
        this.updateProjectDetails();
      },
      reject: () => {
        this.isAssigneeSwitchChecked = false;
        this.isAssigneeSwitchDisabled = false;
      }
    });
  }

  updateProjectDetails() {

    let hierarchyData = JSON.parse(localStorage.getItem('completeHierarchyData'))[this.selectedProject['type']?.toLowerCase()];

    const updatedDetails = {};
    updatedDetails['projectName'] = this.selectedProject['name'] || this.selectedProject['Project'];
    updatedDetails['kanban'] = this.selectedProject['type'] === 'Kanban' ? true : false;
    updatedDetails['hierarchy'] = [];
    updatedDetails['saveAssigneeDetails'] = this.isAssigneeSwitchChecked;
    updatedDetails['id'] = this.selectedProject['id'];
    updatedDetails["createdAt"] = new Date().toISOString();
    updatedDetails["developerKpiEnabled"] = this.developerKpiEnabled;
    updatedDetails["projectOnHold"] = this.projectOnHold;
    for (let element of hierarchyData) {
      if (element.hierarchyLevelId == 'project') {
        break;
      }
      updatedDetails['hierarchy'].push({
        hierarchyLevel: {
          level: element.level,
          hierarchyLevelId: element.hierarchyLevelId,
          hierarchyLevelName: element.hierarchyLevelName
        },
        value: this.selectedProject[element.hierarchyLevelId]
      });
    }

    this.httpService.updateProjectDetails(updatedDetails, this.selectedProject.id).subscribe(response => {
      if (response && response.serviceResponse && response.serviceResponse.success) {
        this.isAssigneeSwitchDisabled = true;
        this.selectedProject.projectOnHold = this.projectOnHold;
        this.messageService.add({
          severity: 'success',
          summary: 'Assignee Switch Enabled  successfully.'
        });
      } else {
        this.isAssigneeSwitchChecked = false;
        this.isAssigneeSwitchDisabled = false;
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
      accept: () => {
        this.generateToken();
      },
      reject: null
    });
  }

  generateToken() {
    this.tokenCopied = false;
    this.generateTokenLoader = true;
    const projectDetails = this.sharedService.getSelectedProject();
    const postData = {
      basicProjectConfigId: projectDetails['id'],
      projectName: projectDetails['Project'],
      userName: this.userName
    };

    this.httpService.generateToken(postData).subscribe(response => {
      this.generateTokenLoader = false;
      this.displayGeneratedToken = true;
      if (response['success'] && response['data']) {
        this.generatedToken = response['data'].apiToken;
      } else {
        this.messageService.add({ severity: 'error', summary: 'Error occured while generating token. Please try after some time' });
      }
    });
  }

  copyToken() {
    this.tokenCopied = true;
    navigator.clipboard.writeText(this.generatedToken);
  }

}
