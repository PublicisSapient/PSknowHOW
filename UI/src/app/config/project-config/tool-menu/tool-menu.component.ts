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
import { KeyValue } from '@angular/common';
import { GetAuthorizationService } from 'src/app/services/get-authorization.service';
import { GoogleAnalyticsService } from '../../../services/google-analytics.service';
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
  generateTokenLoader = false;
  displayGeneratedToken = false;
  generatedToken = '';
  tokenCopied = false;
  isAssigneeSwitchChecked: boolean = false;
  isAssigneeSwitchDisabled: boolean = false;
  assigneeSwitchInfo = "Turn ON to retrieve people-related information, such as assignees, developer profiles from all relevant source tools connected to your project.";
  userName: string;
  repoTools = ['BitBucket', 'GitLab', 'GitHub', 'Azure Repo'];
  repoToolsEnabled: boolean;
  userProjects = [];
  activeProject: any;
  configOptions: { name: string; value: string; }[];
  selectedTab: string = 'projectConfig';
  uniqueTools: any = [];

  constructor(
    public router: Router,
    public sharedService: SharedService,
    private httpService: HttpService,
    public getAuthorizationService: GetAuthorizationService,
    private ga: GoogleAnalyticsService) {
  }

/**
 * Initializes the component by retrieving the selected project, configuring project type options,
 * and processing hierarchy level details. It also navigates to the project list if no project is selected.
 *
 * @returns {void} - No return value.
 */
  ngOnInit() {
    this.selectedProject = this.sharedService.getSelectedProject();
    this.projectTypeOptions = [
      { name: 'Jira', value: false },
      { name: 'Azure Boards', value: true }
    ];
    this.repoToolsEnabled = this.sharedService.getGlobalConfigData()?.repoToolFlag;

    const selectedType = (this.selectedProject?.type || this.selectedProject?.Type)?.toLowerCase() !== 'scrum' ? 'kanban' : 'scrum';
    const levelDetails = JSON.parse(localStorage.getItem('completeHierarchyData'))[selectedType].map((x) => {
      return {
        id: x['hierarchyLevelId'],
        name: x['hierarchyLevelName']
      }
    });

    setTimeout(() => {
      if (this.selectedProject && Object.keys(this.selectedProject)?.length) {
        Object.keys(this.selectedProject).forEach(key => {
          if (levelDetails.map(x => x.id).includes(key)) {
            let propertyName = levelDetails.filter(x => x.id === key)[0].name;
            this.selectedProject[propertyName] = this.selectedProject[key];
            delete this.selectedProject[key];
          }
        });
      }
    });

    this.getProjects();

    if (!this.selectedProject) {
      this.router.navigate(['./dashboard/Config/ProjectList']);
    } else {
      this.dataLoading = true;
      this.getToolsConfigured();
    }

  }

  getToolsConfigured() {
    this.disableSwitch = false
    this.httpService.getAllToolConfigs(this.selectedProject?.id).subscribe(response => {
      this.dataLoading = false;
      if (response?.success) {
        this.sharedService.setSelectedToolConfig(response['data']);
        this.selectedTools = response['data'];
        this.setGaData();

        this.uniqueTools = Array.from(
          this.selectedTools.reduce((map, item) => map.set(item.toolName, item), new Map()).values()
        );
        
        if(this.router.url.includes('tab=2')){
        this.buttonText = 'Set Up';
          this.tools = [
            {
              toolName: 'Jira',
              category: 'Project Management',
              description: '-',
              icon: 'fab fa-atlassian',
              routerLink: `/dashboard/Config/ConfigSettings/${this.selectedProject.id}/JiraConfig`,
              queryParams1: 'Jira',
              routerLink2: `/dashboard/Config/ConfigSettings/${this.selectedProject.id}/FieldMapping`,
              index: 0,
              connectionName: this.uniqueTools.filter(tool => tool.toolName === 'Jira')[0]?.connectionName,
              updatedAt: this.uniqueTools.filter(tool => tool.toolName === 'Jira')[0]?.updatedAt
            },
            {
              toolName: 'JiraTest',
              category: 'Test Management',
              description: '-',
              icon: 'fab fa-atlassian',
              routerLink: `/dashboard/Config/ConfigSettings/${this.selectedProject.id}/JiraConfig`,
              queryParams1: 'JiraTest',
              index: 11,
              connectionName: this.uniqueTools.filter(tool => tool.toolName === 'JiraTest')[0]?.connectionName,
              updatedAt: this.uniqueTools.filter(tool => tool.toolName === 'JiraTest')[0]?.updatedAt
            },
            {
              toolName: 'Zephyr',
              category: 'Test Management',
              description: '-',
              icon: '',
              routerLink: `/dashboard/Config/ConfigSettings/${this.selectedProject.id}/JiraConfig`,
              queryParams1: 'Zephyr',
              index: 1,
              connectionName: this.uniqueTools.filter(tool => tool.toolName === 'Zephyr')[0]?.connectionName,
              updatedAt: this.uniqueTools.filter(tool => tool.toolName === 'Zephyr')[0]?.updatedAt
            },
            {
              toolName: 'Jenkins',
              category: 'Build',
              description: '-',
              icon: 'fab fa-jenkins',
              routerLink: `/dashboard/Config/ConfigSettings/${this.selectedProject.id}/JiraConfig`,
              queryParams1: 'Jenkins',
              index: 2,
              connectionName: this.uniqueTools.filter(tool => tool.toolName === 'Jenkins')[0]?.connectionName,
              updatedAt: this.uniqueTools.filter(tool => tool.toolName === 'Jenkins')[0]?.updatedAt
            },
            {
              toolName: 'Bitbucket',
              category: 'Source Code Management',
              description: '-',
              icon: 'fab fa-bitbucket',
              routerLink: `/dashboard/Config/ConfigSettings/${this.selectedProject.id}/JiraConfig`,
              queryParams1: 'Bitbucket',
              index: 3,
              connectionName: this.uniqueTools.filter(tool => tool.toolName === 'Bitbucket')[0]?.connectionName,
              updatedAt: this.uniqueTools.filter(tool => tool.toolName === 'Bitbucket')[0]?.updatedAt
            },
            {
              toolName: 'GitLab',
              category: 'Source Code Management',
              description: '-',
              icon: 'fab fa-gitlab',
              routerLink: `/dashboard/Config/ConfigSettings/${this.selectedProject.id}/JiraConfig`,
              queryParams1: 'GitLab',
              index: 4,
              connectionName: this.uniqueTools.filter(tool => tool.toolName === 'GitLab')[0]?.connectionName,
              updatedAt: this.uniqueTools.filter(tool => tool.toolName === 'GitLab')[0]?.updatedAt
            },
            {
              toolName: 'Sonar',
              category: 'Security',
              description: '-',
              icon: '',
              routerLink: `/dashboard/Config/ConfigSettings/${this.selectedProject.id}/JiraConfig`,
              queryParams1: 'Sonar',
              index: 5,
              connectionName: this.uniqueTools.filter(tool => tool.toolName === 'Sonar')[0]?.connectionName,
              updatedAt: this.uniqueTools.filter(tool => tool.toolName === 'Sonar')[0]?.updatedAt
            },
            {
              toolName: 'TeamCity',
              category: 'Build',
              description: '-',
              icon: '',
              routerLink: `/dashboard/Config/ConfigSettings/${this.selectedProject.id}/JiraConfig`,
              queryParams1: 'Teamcity',
              index: 6,
              connectionName: this.uniqueTools.filter(tool => tool.toolName === 'TeamCity')[0]?.connectionName,
              updatedAt: this.uniqueTools.filter(tool => tool.toolName === 'TeamCity')[0]?.updatedAt
            },
            {
              toolName: 'Bamboo',
              category: 'Build',
              description: '-',
              icon: '',
              routerLink: `/dashboard/Config/ConfigSettings/${this.selectedProject.id}/JiraConfig`,
              queryParams1: 'Bamboo',
              index: 7,
              connectionName: this.uniqueTools.filter(tool => tool.toolName === 'Bamboo')[0]?.connectionName,
              updatedAt: this.uniqueTools.filter(tool => tool.toolName === 'Bamboo')[0]?.updatedAt
            },
            {
              toolName: 'Azure Pipeline',
              category: 'Build',
              description: '-',
              icon: 'fab fa-windows',
              routerLink: `/dashboard/Config/ConfigSettings/${this.selectedProject.id}/JiraConfig`,
              queryParams1: 'AzurePipeline',
              index: 8,
              connectionName: this.uniqueTools.filter(tool => tool.toolName === 'AzurePipeline')[0]?.connectionName,
              updatedAt: this.uniqueTools.filter(tool => tool.toolName === 'AzurePipeline')[0]?.updatedAt
            },
            {
              toolName: 'Azure Repo',
              category: 'Source Code Management',
              description: '-',
              icon: 'fab fa-windows',
              routerLink: `/dashboard/Config/ConfigSettings/${this.selectedProject.id}/JiraConfig`,
              queryParams1: 'AzureRepository',
              index: 9,
              connectionName: this.uniqueTools.filter(tool => tool.toolName === 'AzureRepository')[0]?.connectionName,
              updatedAt: this.uniqueTools.filter(tool => tool.toolName === 'AzureRepository')[0]?.updatedAt
            },
            {
              toolName: 'GitHub',
              category: 'Source Code Management',
              description: '-',
              icon: 'fab fa-github',
              routerLink: `/dashboard/Config/ConfigSettings/${this.selectedProject.id}/JiraConfig`,
              queryParams1: 'GitHub',
              index: 10,
              connectionName: this.uniqueTools.filter(tool => tool.toolName === 'GitHub')[0]?.connectionName,
              updatedAt: this.uniqueTools.filter(tool => tool.toolName === 'GitHub')[0]?.updatedAt
            },
            {
              toolName: 'GitHub Action',
              category: 'Build',
              description: '-',
              icon: 'fab fa-github',
              routerLink: `/dashboard/Config/ConfigSettings/${this.selectedProject.id}/JiraConfig`,
              queryParams1: 'GitHubAction',
              index: 11,
              connectionName: this.uniqueTools.filter(tool => tool.toolName === 'GitHubAction')[0]?.connectionName,
              updatedAt: this.uniqueTools.filter(tool => tool.toolName === 'GitHubAction')[0]?.updatedAt
            },
            {
              toolName: 'ArgoCD',
              category: 'Build',
              description: '-',
              icon: '',
              routerLink: `/dashboard/Config/ConfigSettings/${this.selectedProject.id}/JiraConfig`,
              queryParams1: 'ArgoCD',
              index: 13,
              connectionName: this.uniqueTools.filter(tool => tool.toolName === 'ArgoCD')[0]?.connectionName,
              updatedAt: this.uniqueTools.filter(tool => tool.toolName === 'ArgoCD')[0]?.updatedAt
            }

          ];
        }

        const jiraOrAzure = response['data']?.filter(tool => tool.toolName === 'Jira' || tool.toolName === 'Azure');
        if (jiraOrAzure.length) {
          const fakeEvent = {
            value: jiraOrAzure[0].toolName === 'Azure'
          };
          this.projectTypeChange(fakeEvent, false);
          this.selectedType = jiraOrAzure[0].toolName === 'Azure';
          const kpiID = this.selectedProject['type'] === 'Kanban' ? 'kpi1' : 'kpi0';
          let obj = {
            "releaseNodeId": null
          }
          this.httpService.getFieldMappingsWithHistory(jiraOrAzure[0].id, kpiID, obj).subscribe(mappings => {
            if (mappings?.success) {
              this.sharedService.setSelectedFieldMapping(mappings['data']);
              this.disableSwitch =  true;
            } else {
              this.sharedService.setSelectedFieldMapping(null);
            }
          });
        }


      }
    });
  }

  projectTypeChange(event, isClicked) {
    const azureType = {
      toolName: 'Azure',
      category: 'Project Management',
      description: '-',
      icon: 'fab fa-windows',
      routerLink: `/dashboard/Config/ConfigSettings/${this.selectedProject.id}/JiraConfig`,
      queryParams1: 'Azure',
      routerLink2: `/dashboard/Config/ConfigSettings/${this.selectedProject.id}/FieldMapping`,
      index: 0,
      connectionName: this.uniqueTools.filter(tool => tool.toolName === 'Azure')[0]?.connectionName,
      updatedAt: this.uniqueTools.filter(tool => tool.toolName === 'Azure')[0]?.updatedAt
    };
    const jiraType = {
      toolName: 'Jira',
      category: 'Project Management',
      description: '-',
      icon: 'fab fa-atlassian',
      routerLink: `/dashboard/Config/ConfigSettings/${this.selectedProject.id}/JiraConfig`,
      queryParams1: 'Jira',
      routerLink2: `/dashboard/Config/ConfigSettings/${this.selectedProject.id}/FieldMapping`,
      index: 0,
      connectionName: this.uniqueTools.filter(tool => tool.toolName === 'Jira')[0]?.connectionName,
      updatedAt: this.uniqueTools.filter(tool => tool.toolName === 'Jira')[0]?.updatedAt
    };
    this.tools = this.tools.filter((tool) => tool.toolName !== 'Azure' && tool.toolName !== 'Jira');
      if (event && event.value) {
        this.tools.unshift(azureType);
      } else {
        this.tools.unshift(jiraType);
      }

  }

  isProjectConfigured(toolName) {
    //TODO: backend refactor needed, this is hot fix for now
    if (toolName === 'Azure Pipeline') {
      toolName = 'AzurePipeline';
    }
    if (toolName === 'Azure Repo') {
      toolName = 'AzureRepository';
    }

    if (toolName === 'GitHub Action') {
      toolName = 'GitHubAction';
    }
    const configuredProject = this.selectedTools.filter((tool) => tool.toolName.toLowerCase() == toolName.toLowerCase());
    return (configuredProject && configuredProject.length > 0 ? true : false);
  }

  originalOrder = (a: KeyValue<number, string>, b: KeyValue<number, string>): number => 0;

  setGaData() {
    let gaObj = {};
    let toolArr = [];
    this.selectedTools?.forEach((x) => {
      if (!toolArr.includes(x.toolName)) {
        toolArr?.push(x.toolName);
      }
    });
    gaObj = {
      'name': this.selectedProject.Project,
      'tools': [...toolArr]
    }
    const hierarchyData = JSON.parse(localStorage.getItem('hierarchyData'));
    if(Array.isArray(hierarchyData)) {
      hierarchyData?.forEach((item) => {
        gaObj['category' + item?.level] = this.selectedProject[item?.hierarchyLevelName];
      })
    }
    this.ga.setProjectToolsData(gaObj);
  }

  getProjects() {
    this.userProjects = this.sharedService.getProjectList();
    if (this.userProjects != null && this.userProjects.length > 0) {
      this.userProjects.sort((a, b) => a?.name?.localeCompare(b.name, undefined, { numeric: true }));
    }
    if(this.selectedProject && this.router.url.includes(this.selectedProject['id'])) {
      this.selectedProject = this.userProjects.filter((x) => x.id == this.selectedProject?.id)[0]
    } else {
      this.selectedProject = this.userProjects[0];
    }
  }

  updateProjectSelection() {
    this.setSelectedProject();
    this.router.navigate([`/dashboard/Config/ConfigSettings/${this.selectedProject?.id}`], { queryParams: { 'type': (this.selectedProject?.type?.toLowerCase() || this.selectedProject?.Type?.toLowerCase()) ,tab: 2 } });
    this.getToolsConfigured();
}

  gotoProcessor() {
    this.router.navigate(['/dashboard/Config/AdvancedSettings'], { queryParams: { pid: this.selectedProject?.id } });
  }

  setSelectedProject() {
    this.sharedService.setSelectedProject(this.selectedProject);
  }

}
