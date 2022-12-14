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
import { ConfirmationService, MessageService } from 'primeng/api';
import { HttpService } from '../../../services/http.service';
import { SharedService } from '../../../services/shared.service';
import { GetAuthorizationService } from '../../../services/get-authorization.service';
import { Router } from '@angular/router';
import { TextEncryptionService } from '../../../services/text.encryption.service';
import { Table } from 'primeng/table';

declare const require: any;
@Component({
  selector: 'app-project-list',
  templateUrl: './project-list.component.html',
  styleUrls: ['./project-list.component.css']
})
export class ProjectListComponent implements OnInit {
  projectList: any = [];
  allProjectList: any = [];
  loading: boolean;
  projectConfirm: boolean;
  roleAccess: any = {};
  isAdminOrSuperAdmin = false;
  selectedDateFilter = null;
  dateFilterList: any = [
    {
      key: 'Last 10 days',
      value: 10
    },
    {
      key: 'Last 15 days',
      value: 15
    },
    {
      key: 'Last 30 days',
      value: 30
    }
  ];
  authorities: Array<string> = [];
  cols: Array<any> = [];
  globalSearchFilter: Array<string> = [];
  @ViewChild(Table) table: Table;

  constructor(private http: HttpService, private sharedService: SharedService, private messenger: MessageService, private router: Router, private confirmationService: ConfirmationService,
    private authorization: GetAuthorizationService, private aesEncryption: TextEncryptionService) { }

  ngOnInit(): void {
    this.getData();
    this.roleAccessAssign();
    this.getHierarchy();
  }

  /* Assign role along with project Id */
  roleAccessAssign() {
    const projectsAccess = !!localStorage.getItem('projectsAccess') && localStorage.getItem('projectsAccess') !== 'undefined' && localStorage.getItem('projectsAccess') !== 'null' ? JSON.parse(localStorage.getItem('projectsAccess')) : [];
    const decryptedText = this.aesEncryption.convertText(localStorage.getItem('authorities'), 'decrypt');
    this.authorities = !!decryptedText && decryptedText !== 'undefined' && decryptedText !== 'null' ? JSON.parse(decryptedText) : [];
    if (projectsAccess.length) {
      projectsAccess.forEach(projectAccess => {
        this.roleAccess[projectAccess.role] = [];
        projectAccess?.projects?.forEach(project => {
          this.roleAccess[projectAccess.role].push(project.projectId);
        });
      });
    }
    this.checkUserIsAdminOrSuperAdmin();
  }

  /* Check user has admin or superadmin access */
  checkUserIsAdminOrSuperAdmin() {
    let role;
    if (!!this.roleAccess && Object.keys(this.roleAccess).length > 0) {
      for (role in this.roleAccess) {
        if (role === 'ROLE_SUPERADMIN' || role === 'ROLE_PROJECT_ADMIN') {
          this.isAdminOrSuperAdmin = true;
          break;
        } else {
          this.isAdminOrSuperAdmin = false;
        }
      }
    } else if (this.authorities?.includes('ROLE_SUPERADMIN')) {
      this.isAdminOrSuperAdmin = true;
    } else {
      this.isAdminOrSuperAdmin = false;
    }
  }

  getData() {
    this.cols = [];
    this.allProjectList = [];
    this.loading = true;
    this.http.getProjectListData().subscribe(responseList => {
      if (responseList[0].success) {
        this.projectList = responseList[0]?.data;
        // this.allProjectList = this.projectList;
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
              type: this.projectList[i]?.kanban ? 'Kanban' : 'Scrum'
            };
            for (let j = 0; j < this.projectList[i]?.hierarchy?.length; j++) {
              obj[this.projectList[i]?.hierarchy[j]?.hierarchyLevel['hierarchyLevelId']] = this.projectList[i]?.hierarchy[j]?.value;
            }
            this.allProjectList?.push(obj);
          }
        }
        this.loading = false;
        this.table?.reset();

      } else {
        this.loading = false;
        this.messenger.add({
          severity: 'error',
          summary: 'Some error occurred. Please try again later.'
        });
      }
    });
  }

  newProject() {
    this.sharedService.setSelectedProject(null);
    this.router.navigate(['./dashboard/Config/BasicConfig']);
  }

  editProject(project) {
    this.sharedService.setSelectedProject(project);
    this.router.navigate(['./dashboard/Config/BasicConfig']);
  }

  deleteProject(project) {
    this.projectConfirm = true;
    this.confirmationService.confirm({
      message: this.getAlertMessageOnClickDelete(),
      header: `Delete ${project.name}?`,
      icon: 'pi pi-info-circle',
      accept: () => {
        this.http.deleteProject(project).subscribe(response => {
          this.projectDeletionStatus(response);
          let arr = JSON.parse(localStorage.getItem('projectsAccess'));
          arr.map((item) => {
            item.projects = item.projects.filter(x => x.projectId != project.id);
          });
          arr = arr.filter(item => item.projects?.length > 0);
          localStorage.setItem('projectsAccess', JSON.stringify(arr));
        }, error => {
          this.projectDeletionStatus(error);
        });
      },
      reject: () => {

      }
    });
  }


  getAlertMessageOnClickDelete() {
    const commonMsg = 'Project and related data will be deleted forever, are you sure you want to delete it?';

    return commonMsg;
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
        },
        reject: () => {
        }
      });
    } else {
      this.confirmationService.confirm({
        message: 'Something went wrong. Please try again after sometime.',
        header: 'Project Deletion Status',
        icon: 'fa fa-times-circle alert-danger',
        accept: () => {
        },
        reject: () => {
        }
      });
    }
  }

  // handleDateFilterChange() {
  //   let days = this.selectedDateFilter;
  //   let date = new Date();
  //   if (days) {
  //     this.projectList = this.allProjectList.filter((project) => new Date(project.updatedAt) >= new Date(date.getTime() - (days * 24 * 60 * 60 * 1000)));
  //   } else {
  //     this.projectList = this.allProjectList;
  //   }
  // }

  editConfiguration(project) {
    const newProjectObj = {};
    Object.keys(project).forEach((key) => {
      if (key !== 'id') {
        newProjectObj[this.cols.filter((col) => col.id === key)[0].heading] = project[key];
      }
    });
    newProjectObj['id'] = project['id'];
    this.sharedService.setSelectedProject(newProjectObj);
    this.router.navigate(['/dashboard/Config/ToolMenu']);

  }

  getHierarchy() {
    this.http.getHierarchyLevels().subscribe(formFieldData => {
      formFieldData.forEach(element => {
        if (element.suggestions && element.suggestions.length) {
          element.suggestions = element.suggestions.map(suggestion => ({
              name: suggestion,
              code: suggestion
            }));
        }
        element.value = '';
        element.required = true;
      });

      localStorage.setItem('hierarchyData', JSON.stringify(formFieldData));
    });
  }
}
