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
import { ConfirmationService, MessageService } from 'primeng/api';
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
  isProjectAdmin = false;
  isSuperAdmin = false;
  generateTokenLoader = false;
  displayGeneratedToken= false;
  generatedToken='';
  tokenCopied =false;
  isAssigneeSwitchChecked : boolean = false;
  isAssigneeSwitchDisabled : boolean = false;
  assigneeSwitchInfo = "Enable Individual KPIs will fetch People related information (e.g. Assignees from Jira) from all source tools that are connected to your project";
  userName : string;
  repoTools = ['BitBucket','GitLab','GitHub','Azure Repo'];
  repoToolsEnabled : boolean;

  constructor(
      public router: Router,
      private sharedService: SharedService,
      private http: HttpService,
      private messenger: MessageService,
      private confirmationService: ConfirmationService,
      private getAuthorizationService: GetAuthorizationService,
      private ga: GoogleAnalyticsService,) {

    }

  ngOnInit(): void {
    this.sharedService.currentUserDetailsObs.subscribe(details=>{
      if(details){
        this.userName = details['user_name'];
      }
    });
    this.projectTypeOptions = [
      { name: 'Jira', value: false },
      { name: 'Azure Boards', value: true }
    ];
    this.selectedProject = this.sharedService.getSelectedProject();
    this.isProjectAdmin = this.getAuthorizationService.checkIfProjectAdmin();
    this.isSuperAdmin = this.getAuthorizationService.checkIfSuperUser();
     this.isAssigneeSwitchChecked = this.selectedProject?.saveAssigneeDetails;
     this.repoToolsEnabled = this.sharedService.getGlobalConfigData()?.repoToolFlag;

    if (!this.selectedProject) {
      this.router.navigate(['./dashboard/Config/ProjectList']);
    } else {
      this.dataLoading = true;
      this.http.getAllToolConfigs(this.selectedProject.id).subscribe(response => {
        this.dataLoading = false;
        if (response && response['success'] && response['data']?.length) {
          this.sharedService.setSelectedToolConfig(response['data']);
          this.selectedTools = response['data'];
          this.setGaData();
          const jiraOrAzure = response['data']?.filter(tool => tool.toolName === 'Jira' || tool.toolName === 'Azure');
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
            routerLink2: '/dashboard/Config/FieldMapping',
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
          {
            toolName: 'GitHub Action',
            category: 'ABC',
            description: '-',
            icon: 'fab fa-github',
            routerLink: '/dashboard/Config/JiraConfig',
            queryParams1: 'GitHubAction',
            index: 11
          },
          {
            toolName: 'RepoTool',
            category: 'ABC',
            description: '-',
            icon: '',
            routerLink: '/dashboard/Config/JiraConfig',
            queryParams1: 'RepoTool',
            index: 12
          }


        ];
      }
    }
    if(this.isAssigneeSwitchChecked){
      this.isAssigneeSwitchDisabled = true;
    }

       // filtering tolls based on repoToolFlag
       this.tools = this.tools.filter(details=>{
         if(this.repoToolsEnabled){
            return !this.repoTools.includes(details.toolName)
         }else{
           return details.toolName !== 'RepoTool';
         }
       })
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
      routerLink2: '/dashboard/Config/FieldMapping',
      index: 0
    };
    const jiraType = {
      toolName: 'Jira',
      category: 'ABC',
      description: '-',
      icon: 'fab fa-atlassian',
      routerLink: '/dashboard/Config/JiraConfig',
      queryParams1: 'Jira',
      routerLink2: '/dashboard/Config/FieldMapping',
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

    if(toolName === 'GitHub Action'){
      toolName = 'GitHubAction';
    }
    const configuredProject = this.selectedTools.filter((tool) => tool.toolName.toLowerCase() == toolName.toLowerCase());
    return (configuredProject && configuredProject.length > 0 ? true : false);
  }

  generateTokenConfirmation(){
    this.confirmationService.confirm({
			message:`If you create a token, all previously generated tokens will expire, do you want to continue?`,
			header: `Generate Token?`,
			icon: 'pi pi-info-circle',
			accept: () => {
				this.generateToken();
			},
			reject: null
		});
  }

  generateToken(){
    this.tokenCopied = false;
    this.generateTokenLoader =true;
    const projectDetails = this.sharedService.getSelectedProject();
    const postData = {
      basicProjectConfigId: projectDetails['id'],
      projectName: projectDetails['Project'],
      userName: this.userName
    };

    this.http.generateToken(postData).subscribe(response =>{
      this.generateTokenLoader =false;
      this.displayGeneratedToken =true;
      if(response['success'] && response['data']){
        this.generatedToken = response['data'].apiToken;
      }else{
        this.messenger.add({ severity: 'error', summary: 'Error occured while generating token. Please try after some time' });
      }
    });
  }

  copyToken(){
    this.tokenCopied = true;
    navigator.clipboard.writeText(this.generatedToken);
  }
  // Preserve original property order
  originalOrder = (a: KeyValue<number,string>, b: KeyValue<number,string>): number => 0;

  onAssigneeSwitchChange(){
    if(this.isAssigneeSwitchChecked){
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

  updateProjectDetails(){

    // const formFieldData = JSON.parse(localStorage.getItem('hierarchyData'));
    let hierarchyData = JSON.parse(localStorage.getItem('hierarchyData'));

    const updatedDetails = {};
   updatedDetails['projectName'] = this.selectedProject['name'] || this.selectedProject['Project'];
   updatedDetails['kanban'] = this.selectedProject['Type'] === 'Kanban' ? true : false ;
    updatedDetails['hierarchy'] = [];
    updatedDetails['saveAssigneeDetails'] = this.isAssigneeSwitchChecked;
    updatedDetails['id'] = this.selectedProject['id'];
    updatedDetails["createdAt"] = new Date().toISOString();

    hierarchyData.forEach(element => {
     updatedDetails['hierarchy'].push({
       hierarchyLevel: {
         level: element.level,
         hierarchyLevelId: element.hierarchyLevelId,
         hierarchyLevelName: element.hierarchyLevelName
       },
       value: this.selectedProject[element.hierarchyLevelName]
     });
   });

   this.http.updateProjectDetails(updatedDetails,this.selectedProject.id).subscribe(response=>{
    if (response && response.serviceResponse && response.serviceResponse.success) {
      this.isAssigneeSwitchDisabled = true;
      this.messenger.add({
        severity: 'success',
        summary: 'Assignee Switch Enabled  successfully.'
      });
    }else{
      this.isAssigneeSwitchChecked = false;
      this.isAssigneeSwitchDisabled = false;
      this.messenger.add({
        severity: 'error',
        summary: 'Some error occurred. Please try again later.'
      });

    }

   })

  }

  setGaData(){
    let gaObj = {};
    let toolArr = [];
    this.selectedTools?.forEach((x)=>{
      if(!toolArr.includes(x.toolName)){
        toolArr?.push(x.toolName);
      }
    });
    gaObj = {
      'name': this.selectedProject.Project,
      'tools': [...toolArr]
    }
    const hierarchyData = JSON.parse(localStorage.getItem('hierarchyData'));
    hierarchyData?.forEach((item) => {
      gaObj['category'+ item?.level] = this.selectedProject[item?.hierarchyLevelName];
    })
    this.ga.setProjectToolsData(gaObj);
  }
}
