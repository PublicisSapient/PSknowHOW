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
import { Router } from '@angular/router';
import { SharedService } from '../../../services/shared.service';
import { HttpService } from '../../../services/http.service';
import { MessageService } from 'primeng/api';
import { KeyValue } from '@angular/common';
@Component({
  selector: 'app-tool-menu',
  templateUrl: './tool-menu.component.html',
  styleUrls: ['./tool-menu.component.css']
})
export class ToolMenuComponent implements OnInit {
  tools: any = [];
  buttonText = '';
  selectedProject: any;
  projectTypeOptions: any = [];
  selectedType = false;
  dataLoading = false;
  disableSwitch = false;
  selectedTools: Array<any> = [];
  constructor(public router: Router, private sharedService: SharedService, private http: HttpService, private messenger: MessageService) {

  }

  ngOnInit(): void {
    this.projectTypeOptions = [
      { name: 'Jira', value: false },
      { name: 'Azure Boards', value: true }
    ];

    this.selectedProject = this.sharedService.getSelectedProject();
    if (!this.selectedProject) {
      this.router.navigate(['./dashboard/Config/ProjectList']);
    } else {
      this.dataLoading = true;
      this.http.getAllToolConfigs(this.selectedProject.id).subscribe(response => {
        this.dataLoading = false;
        if (response && response['success']) {
          this.sharedService.setSelectedToolConfig(response['data']);
          this.selectedTools = response['data'];
          if (response['data'] && response['data'].length) {
            const jiraOrAzure = response['data'].filter(tool => tool.toolName === 'Jira' || tool.toolName === 'Azure');
            if (jiraOrAzure.length) {
              const fakeEvent = {
                value: {
                  value: jiraOrAzure[0].toolName === 'Azure'
                }
              };
              this.projectTypeChange(fakeEvent, false);
              this.selectedType = jiraOrAzure[0].toolName === 'Azure';
              this.disableSwitch = true;
              this.http.getFieldMappings(jiraOrAzure[0].id).subscribe(mappings => {
                if (mappings && mappings['success']) {
                  this.sharedService.setSelectedFieldMapping(mappings['data']);
                } else {
                  this.sharedService.setSelectedFieldMapping(null);
                }
              });
            }
          }
        }
      });
      if (this.router.url === '/dashboard/Config/ToolMenu') {
        this.buttonText = 'Set Up';
        this.tools = [
          {
            toolName: 'Jira',
            category: 'ABC',
            description: '-',
            icon: 'fab fa-atlassian',
            routerLink: '/dashboard/Config/JiraConfig',
            queryParams1: 'Jira',
            routerLink2: this.selectedProject.Type === 'Kanban' ? '/dashboard/Config/KanbanFieldMapping' : '/dashboard/Config/FieldMapping',
            index: 0
          },
          {
            toolName: 'JiraTest',
            category: 'ABC',
            description: '-',
            icon: 'fab fa-atlassian',
            routerLink: '/dashboard/Config/JiraConfig',
            queryParams1: 'JiraTest',
            index: 11
          },
          {
            toolName: 'Zephyr',
            category: 'ABC',
            description: '-',
            icon: '',
            routerLink: '/dashboard/Config/JiraConfig',
            queryParams1: 'Zephyr',
            index: 1
          },
          {
            toolName: 'Jenkins',
            category: 'ABC',
            description: '-',
            icon: 'fab fa-jenkins',
            routerLink: '/dashboard/Config/JiraConfig',
            queryParams1: 'Jenkins',
            index: 2
          },
          {
            toolName: 'BitBucket',
            category: 'ABC',
            description: '-',
            icon: 'fab fa-bitbucket',
            routerLink: '/dashboard/Config/JiraConfig',
            queryParams1: 'Bitbucket',
            index: 3
          },
          {
            toolName: 'GitLab',
            category: 'ABC',
            description: '-',
            icon: 'fab fa-gitlab',
            routerLink: '/dashboard/Config/JiraConfig',
            queryParams1: 'GitLab',
            index: 4
          },
          {
            toolName: 'Sonar',
            category: 'ABC',
            description: '-',
            icon: '',
            routerLink: '/dashboard/Config/JiraConfig',
            queryParams1: 'Sonar',
            index: 5
          },
          {
            toolName: 'TeamCity',
            category: 'ABC',
            description: '-',
            icon: '',
            routerLink: '/dashboard/Config/JiraConfig',
            queryParams1: 'Teamcity',
            index: 6
          },
          {
            toolName: 'Bamboo',
            category: 'ABC',
            description: '-',
            icon: '',
            routerLink: '/dashboard/Config/JiraConfig',
            queryParams1: 'Bamboo',
            index: 7
          },
          {
            toolName: 'Azure Pipeline',
            category: 'ABC',
            description: '-',
            icon: 'fab fa-windows',
            routerLink: '/dashboard/Config/JiraConfig',
            queryParams1: 'AzurePipeline',
            index: 8
          },
          {
            toolName: 'Azure Repo',
            category: 'ABC',
            description: '-',
            icon: 'fab fa-windows',
            routerLink: '/dashboard/Config/JiraConfig',
            queryParams1: 'AzureRepository',
            index: 9
          },
          {
            toolName: 'GitHub',
            category: 'ABC',
            description: '-',
            icon: 'fab fa-github',
            routerLink: '/dashboard/Config/JiraConfig',
            queryParams1: 'GitHub',
            index: 10
          },
          

        ];
      }
    }
  }

  projectTypeChange(event, isClicked) {
    console.log(event);
    const azureType = {
      toolName: 'Azure',
      category: 'ABC',
      description: '-',
      icon: 'fab fa-windows',
      routerLink: '/dashboard/Config/JiraConfig',
      queryParams1: 'Azure',
      routerLink2: this.selectedProject.Type === 'Kanban' ? '/dashboard/Config/KanbanFieldMapping' : '/dashboard/Config/FieldMapping',
      index: 0
    };
    const jiraType = {
      toolName: 'Jira',
      category: 'ABC',
      description: '-',
      icon: 'fab fa-atlassian',
      routerLink: '/dashboard/Config/JiraConfig',
      queryParams1: 'Jira',
      routerLink2: this.selectedProject.Type === 'Kanban' ? '/dashboard/Config/KanbanFieldMapping' : '/dashboard/Config/FieldMapping',
      index: 0
    };
    this.tools = this.tools.filter((tool) => tool.toolName !== 'Azure' && tool.toolName !== 'Jira');
    if (isClicked) {
      if (!!event && !!event.value) {
        this.tools.unshift(azureType);
      } else {
        this.tools.unshift(jiraType);
      }
    } else {
      if (!!event && !!event.value && !!event.value.value) {
        this.tools.unshift(azureType);
      } else {
        this.tools.unshift(jiraType);
      }
    }

  }

  isProjectConfigured(toolName) {
    //TODO: backend refactor needed, this is hot fix for now
    if(toolName === 'Azure Pipeline') {
      toolName = 'AzurePipeline';
    }
    if(toolName === 'Azure Repo') {
      toolName = 'AzureRepository';
    }
    const configuredProject = this.selectedTools.filter((tool) => tool.toolName.toLowerCase() == toolName.toLowerCase());
    return (configuredProject && configuredProject.length > 0 ? true : false);
  }
  // Preserve original property order
  originalOrder = (a: KeyValue<number,string>, b: KeyValue<number,string>): number => 0;
}
