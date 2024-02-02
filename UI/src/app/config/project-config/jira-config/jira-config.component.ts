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
import { UntypedFormBuilder, UntypedFormGroup, UntypedFormControl, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { MessageService, ConfirmationService } from 'primeng/api';
import { HttpService } from '../../../services/http.service';
import { SharedService } from '../../../services/shared.service';
import { GetAuthorizationService } from '../../../services/get-authorization.service';
import { KeyValue } from '@angular/common';
declare const require: any;

@Component({
  selector: 'app-jira-config',
  templateUrl: './jira-config.component.html',
  styleUrls: [
    '../field-mapping/field-mapping.component.css',
    './jira-config.component.css',
  ],
})
export class JiraConfigComponent implements OnInit {
  toolForm: UntypedFormGroup;
  toolFormObj: any;
  formTitle = '';
  connections: any;
  connectionTableCols: any[];
  configuredToolTableCols: any[];
  selectedConnection: any;
  queryEnabled = false;
  boardsData: any[];
  filteredBoards: any[];
  submitted = false;
  selectedProject: any;
  selectedToolConfig: any;
  formTemplate: any;
  urlParam = '';
  configuredTools: any[];
  isEdit = false;
  loading = false;
  disableSave = false;
  versionList: any[] = [];
  sonarVersionFinalList: any[] = [];
  sonarVersionList: any[] = [];
  sonarCloudVersionList: any[] = [];
  connectionType: any[] = ['Sonar Server', 'Sonar Cloud'];
  selectedConnectionType = '';
  branchList: any[] = [];
  projectKeyList: any[] = [];
  disableBranchDropDown = false;
  bambooProjectDataFromAPI: any[] = [];
  bambooBranchDataFromAPI: any[] = [];
  bambooBranchList: any[] = [];
  bambooPlanList: any[] = [];
  bambooPlanKeyForSelectedPlan = '';
  selectedBambooBranchKey: string;
  disableOrganizationKey = false;
  singleToolAllowed: any[] = ['Jira', 'Zephyr', 'Azure', 'JiraTest'];
  jenkinsJobNameList: any[] = [];
  azurePipelineList: any[] = [];
  azurePipelineResponseList: any[] = [];
  jobType: any[] = [{
    name: 'Build',
    code: 'Build'
  }, {
    name: 'Deploy',
    code: 'Deploy'
  }];
  showControls = true;
  deploymentProjectList: any[] = [];
  selectedDeploymentProject: any;
  azurePipelineApiVersion = '6.0';
  isLoading = false;
  testCaseIdentification: any = [
    {
      name: 'Select',
      code: ''
    },
    {
      name: 'CustomField',
      code: 'CustomField'
    },
    {
      name: 'Labels',
      code: 'Labels'
    }
  ];

  jiraTemplate : any[];
  gitActionWorkflowNameList : any[];
  cloudEnv : any ;

  constructor(
    private formBuilder: UntypedFormBuilder,
    private router: Router,
    private route: ActivatedRoute,
    private http: HttpService,
    private sharedService: SharedService,
    private messenger: MessageService,
    private getAuthorizationService: GetAuthorizationService,
    private confirmationService: ConfirmationService,
  ) { }

  ngOnInit(): void {
    this.selectedProject = this.sharedService.getSelectedProject();
    if (!this.selectedProject) {
      this.router.navigate(['./dashboard/Config/ProjectList']);
    }

    this.route.queryParams.subscribe((params) => {
      if (params['toolName']) {
        this.urlParam = params['toolName'];
        // get pre-configured tool data
        if (
          this.sharedService.getSelectedToolConfig() &&
          this.sharedService.getSelectedToolConfig().length
        ) {
          this.selectedToolConfig = this.sharedService
            .getSelectedToolConfig()
            .filter((toolConfig) => toolConfig.toolName === (this.urlParam));

        }
        this.getConnectionList(this.urlParam);
        this.initializeFields(this.urlParam);
        this.getJiraTemplate();
      } else {
        this.router.navigate(['./dashboard/Config/ProjectList']);
      }
    });

    this.disableSave = this.getAuthorizationService.checkIfViewer(
      this.selectedProject,
    );

    this.http.getSonarVersionList().subscribe((data) => {
      if (this.urlParam !== 'Sonar') {
        return;
      }
      try {
        if (data.success) {
          this.versionList = [...data.data];
          data.data.forEach((el) => {
            if (el.type === 'Sonar Server') {
              el.versions.forEach((version) =>
                this.sonarVersionList.push(version),
              );
            } else if (el.type === 'Sonar Cloud') {
              el.versions.forEach((version) =>
                this.sonarCloudVersionList.push(version),
              );
            }
          });
          if (this.selectedToolConfig?.length > 0) {
            this.updateSonarConnectionTypeAndVersionList(this.selectedToolConfig[0].cloudEnv);
          }

        } else {
          this.messenger.add({
            severity: 'error',
            summary: 'Some error occurred. Please try again later.',
          });
        }
      } catch (error) {
        this.messenger.add({
          severity: 'error',
          summary: error.message,
        });
      }
    },(err) => {
      console.log(err);
      this.messenger.add({
        severity: 'error',
        summary: err.error.message,
      });
    }
    );
  }

  getPlansForBamboo(connectionId) {
    if (connectionId) {
      this.bambooPlanList = [];
      this.showLoadingOnFormElement('planName');
      this.http.getPlansForBamboo(connectionId).subscribe((data) => {
          if (data.success && data?.data?.length > 0) {
            this.bambooProjectDataFromAPI = [...data.data];
            this.bambooPlanList = [...this.bambooProjectDataFromAPI].map(
              (item) => ({ planName: item.projectAndPlanName }),
            );
            this.bambooPlanList = this.bambooPlanList.map(element => ({
              name: element.planName,
              code: element.planName
            }));
            this.hideLoadingOnFormElement('planName');
          } else {
            this.bambooPlanList = [];
            this.toolForm?.controls['planKey'].setValue('');
            this.toolForm?.controls['branchKey'].setValue('');
            this.bambooBranchList = [];
            this.hideLoadingOnFormElement('planName');
            if (this.toolForm?.controls['jobType'].value === 'Build') {
              this.messenger.add({
                severity: 'error',
                summary: data.message,
              });
            }
          }
      });
    }

  }

  getDeploymentProjects(connectionId) {
    if (connectionId) {
      const self = this;
      this.showLoadingOnFormElement('deploymentProject');

      this.http.getDeploymentProjectsForBamboo(connectionId).subscribe((response) => {
        try {
          if (response.success) {
            self.deploymentProjectList = response.data.map(element => ({
              name: element.deploymentProjectName,
              code: element.deploymentProjectId
            }));

          } else {
            self.deploymentProjectList = [];
            if (this.toolForm.controls['jobType'].value && this.toolForm.controls['jobType'].value.name === 'Deploy') {
              self.messenger.add({
                severity: 'error',
                summary: response.message,
              });
            }
          }
          this.hideLoadingOnFormElement('deploymentProject');
        } catch (error) {
          self.deploymentProjectList = [];
          if (this.toolForm?.controls['jobType'].value && this.toolForm?.controls['jobType'].value.name === 'Deploy') {
            self.messenger.add({
              severity: 'error',
              summary: error.message,
            });
          }
          this.hideLoadingOnFormElement('deploymentProject');

        }
      });
    }
  }

  getJenkinsJobNames(connectionId) {
    this.showLoadingOnFormElement('jobName');
    this.http.getJenkinsJobNameList(connectionId).subscribe(data => {

      try {
        if (data.success) {
          this.jenkinsJobNameList = [];
          data.data.forEach(element => {
            this.jenkinsJobNameList.push({
              name: element,
              code: element
            });
          });
        } else {
          this.jenkinsJobNameList = [];
          this.messenger.add({
            severity: 'error',
            summary: data.message,
          });
        }
        this.hideLoadingOnFormElement('jobName');
      } catch (error) {
        this.jenkinsJobNameList = [];
        this.hideLoadingOnFormElement('jobName');
        this.messenger.add({
          severity: 'error',
          summary: error.message,
        });
      }
    }, (err) => {
      console.log(err);
      this.messenger.add({
        severity: 'error',
        summary: err.error.message,
      });
    });
  }

  onConnectionSelect(connection: any) {
    const connectionId = connection.id;
    if (this.urlParam === 'Bamboo') {
      this.getPlansForBamboo(connectionId);
      this.getDeploymentProjects(connectionId);
    }

    if (this.urlParam === 'Jenkins') {
      this.getJenkinsJobNames(connectionId);
    }

    if (this.urlParam === 'Sonar') {
      this.cloudEnv = connection.cloudEnv
      this.tool['gitLabSdmID'].setValue('');
      this.tool['apiVersion'].enable();
      this.tool['projectKey'].enable();
      this.clearSonarForm();
      this.updateSonarConnectionTypeAndVersionList(connection.cloudEnv);
      this.enableDisableOrganizationKey(connection.cloudEnv);
    }

    if (this.urlParam === 'AzurePipeline') {
      if (this.tool['jobType'].value.name === 'Build') {
        this.getAzureBuildPipelines(this.selectedConnection);
      } else if (this.tool['jobType'].value.name === 'Deploy') {
        this.getAzureReleasePipelines(this.selectedConnection);
      }
    }

    if (this.urlParam === 'Jira') {
      this.isLoading = false;
    }

    if(this.urlParam === 'GitHubAction'){
      this.gitActionWorkflowNameList = [];
      this.toolForm.get('repositoryName').reset();
      this.toolForm.get('workflowID').reset();
    }
  }

  enableDisableOrganizationKey(cloudEnv) {
    if (cloudEnv) {
      this.tool['organizationKey'].enable();
    } else {
      this.tool['organizationKey'].disable();
    }
  }

  clearSonarForm() {
    this.tool['organizationKey'].setValue('');
    this.tool['apiVersion'].setValue('');
    this.tool['projectKey'].setValue('');
    this.tool['branch'].setValue('');

  }

  updateSonarConnectionTypeAndVersionList(cloudEnv) {
    if (cloudEnv) {
      this.selectedConnectionType = 'Sonar Cloud';
      this.sonarVersionFinalList = [];
      this.sonarCloudVersionList.forEach(element => {
        this.sonarVersionFinalList.push({
          name: element,
          code: element
        });
      });
    } else {
      this.selectedConnectionType = 'Sonar Server';
      this.sonarVersionFinalList = [];
      this.sonarVersionList.forEach(element => {
        this.sonarVersionFinalList.push({
          name: element,
          code: element
        });
      });
    }
  }


  getOptionList(id: any) {
    // TODO: refactor needed here
    if (id === 'env') {
      return this.connectionType;
    } else if (id === 'apiVersion') {
      return this.sonarVersionFinalList;
    } else if (id === 'projectKey') {
      return this.projectKeyList;
    } else if (id === 'branch') {
      return this.branchList;
    } else if (id === 'planName') {
      return this.bambooPlanList;
    } else if (id === 'branchName') {
      return this.bambooBranchList;
    } else if (id === 'jobName') {
      return this.jenkinsJobNameList;
    } else if (id === 'azurePipelineName') {
      return this.azurePipelineList;
    } else if (id === 'jobType') {
      return this.jobType;
    } else if (id === 'deploymentProject') {
      return this.deploymentProjectList;
    }else if(id === 'testAutomatedIdentification'
    || id === 'testAutomationCompletedIdentification'
    || id === 'testRegressionIdentification'){
      return this.testCaseIdentification;
    }else if(id === 'workflowID'){
      return this.gitActionWorkflowNameList;
    }
  }
  getConnectionList(toolName) {
    this.loading = true;
    let finalToolName = "";
    if(toolName === 'JiraTest'){
      finalToolName = 'Jira';
    }else if(toolName === 'GitHubAction'){
      finalToolName = 'GitHub';
    }else{
      finalToolName = toolName;
    }

    this.http.getAllConnectionTypeBased(finalToolName).subscribe((response) => {
      this.loading = false;
      if (response && response['success']) {
        this.connections = response['data'];
        if (this.selectedToolConfig && this.selectedToolConfig.length) {
          if (this.isSingleToolAllowed(finalToolName)) {
            this.selectedConnection = this.connections.filter(
              (connection) =>
                connection.id === this.selectedToolConfig[0].connectionId,
            )[0];
            this.promote(this.selectedConnection?.id, this.connections);
          }

          this.configuredTools = this.sharedService
            .getSelectedToolConfig()
            .filter((toolConfig) => toolConfig.toolName === this.urlParam);
          if (this.configuredTools.length) {
            this.configuredTools.forEach((tool) => {
              this.connections.forEach((connection) => {
                if (tool.connectionId === connection.id) {
                  tool['connectionName'] = connection.connectionName;
                }
              });
            });
          }

          // prefetch boards if projectKey is present
          if (this.urlParam === 'Jira') {
            if (this.toolForm.controls['projectKey'].value) {
              this.fetchBoards(this);
            }
          }
        }
      } else {
        this.connections = [];
        this.messenger.add({
          severity: 'error',
          summary:
            'No connections available. Please go to "Connections" tab and create a connection.',
        });
      }
    });
  }

  checkProjectKey = () => {
    if (this.toolForm.controls['projectKey'] && this.toolForm.controls['projectKey'].value) {
      return false;
    }
    return true;
  };

  checkBoards = () => {
    if (this.queryEnabled || (!this.toolForm.controls['projectKey'].value)) {
      return true;
    }
    return false;
  };

  fetchBoards(self) {
    if (self.selectedConnection && self.selectedConnection.id) {
      if (self.toolForm.controls['projectKey'].dirty && self.toolForm.controls['projectKey'].value && self.toolForm.controls['projectKey'].value.length) {
        const postData = {};
        // self.showLoadingOnFormElement('boards');
        self.isLoading = true;
        postData['connectionId'] = self.selectedConnection.id;
        postData['projectKey'] = self.toolForm.controls['projectKey'].value;
        postData['boardType'] = self.selectedProject['Type'];
        self.http.getAllBoards(postData).subscribe((response) => {
          if (response && response['data']) {
            self.boardsData = response['data'];
            self.boardsData.forEach((board) => {
              board['projectKey'] = self.toolForm.controls['projectKey'].value;
            });
            // if boards already has value
            if (self.toolForm.controls['boards'].value.length) {
              self.toolForm.controls['boards'].value.forEach((val) => {
                self.boardsData = self.boardsData.filter((data) => (data.boardId + '') !== (val.boardId + ''));
              });
            }
          } else {
            self.messenger.add({
              severity: 'error',
              summary:
                'No boards found for the selected Project Key.',
            });
            self.boardsData = [];
            self.toolForm.controls['boards'].setValue([]);
          }
          // self.hideLoadingOnFormElement('boards');
          self.isLoading = false;
        });
      }
    } else {
      self.toolForm.controls['projectKey'].setValue('');
      self.messenger.add({
        severity: 'error',
        summary:
          'Select Connection first.',
      });
    }
  };

  selectJIRAType = (event) => {
  };

  onBoardUnselect = (value) => {
    this.boardsData.push(value);
  };

  onBoardSelect = (value) => {
    this.boardsData = this.boardsData.filter((data) => (data.boardId + '') !== (value.boardId + ''));
  };

  filterBoards = (event) => {
    const filtered: any[] = [];
    const query = event.query;
    if (this.boardsData.length) {
      for (const board of this.boardsData) {
        if (board.boardName.toLowerCase().indexOf(query.toLowerCase()) === 0) {
          filtered.push(board);
        }
      }
    }
    this.filteredBoards = filtered;
  };

  getAzureBuildPipelines = (connection: any) => {
    if (this.selectedConnection) {
      this.showLoadingOnFormElement('azurePipelineName');
      this.http.getAzurePipelineList(connection.id, this.azurePipelineApiVersion).subscribe(data => {
        try {
          if (data.success) {
            this.azurePipelineResponseList = data.data.map(element => ({
              name: element.pipelineName,
              code: element.definitions
            }));
            this.azurePipelineList = [...this.azurePipelineResponseList];
          } else {
            this.azurePipelineList = [];
            this.messenger.add({
              severity: 'error',
              summary: data.message,
            });
          }
          this.hideLoadingOnFormElement('azurePipelineName');
        } catch (error) {
          this.hideLoadingOnFormElement('azurePipelineName');
          this.azurePipelineList = [];
          this.messenger.add({
            severity: 'error',
            summary:
              'Something went wrong, Please try again.',
          });
        }

      });
    }

  };

  getAzureReleasePipelines = (connnection: any) => {
    if (this.selectedConnection) {
      this.showLoadingOnFormElement('azurePipelineName');
      this.http.getAzureReleasePipelines(connnection.id, this.azurePipelineApiVersion).subscribe(data => {
        try {
          if (data.success) {
            this.azurePipelineResponseList = data.data.map(element => ({
              name: element.pipelineName,
              code: element.definitions
            }));
            this.azurePipelineList = [...this.azurePipelineResponseList];
            // .map(el => el.name);

          } else {
            this.azurePipelineList = [];
            this.messenger.add({
              severity: 'error',
              summary: data.message,
            });
          }
          this.hideLoadingOnFormElement('azurePipelineName');
        } catch (error) {
          this.hideLoadingOnFormElement('azurePipelineName');
          this.azurePipelineList = [];
          this.messenger.add({
            severity: 'error',
            summary:
              'Something went wrong, Please try again.',
          });
        }

      });
    }

  };

  pipeLineDropdownHandler = (value: any, elementId?) => {
    //TODO: Refactor needed.
    if (value) {
      const selectedJobNameDefinition = this.azurePipelineResponseList.filter(data => data.code === value)[0]?.code;
      this.toolForm.controls['jobName'].setValue(selectedJobNameDefinition);
    }
  };

  jobTypeChangeHandler = (value: string, elementId?) => {
    value = value['name'] || value;
    switch (this.urlParam) {
      case 'Bamboo':
        if (value.toLowerCase() === 'build') {
          const planField =this.formTemplate?.elements?.find(element => element.id === 'planName');
          if (this.bambooPlanList?.length == 0 && !planField?.isLoading) {
            this.messenger.add({
              severity: 'error',
              summary: 'No plan details found',
            });
          }
          this.hideFormElements(['deploymentProject',]);
          this.showFormElements(['planName', 'planKey', 'branchName', 'branchKey']);
        } else if (value.toLowerCase() === 'deploy') {
          this.showFormElements(['deploymentProject']);
          this.hideFormElements(['planName', 'planKey', 'branchName', 'branchKey']);
        }
        break;
      case 'AzurePipeline':

        if (value.toLowerCase() === 'build') {
          this.getAzureBuildPipelines(this.selectedConnection);
        } else if (value.toLowerCase() === 'deploy') {
          this.getAzureReleasePipelines(this.selectedConnection);
        }

        break;
      case 'Jenkins':

        if (value.toLowerCase() === 'build') {
          this.hideFormElements(['parameterNameForEnvironment']);
        } else if (value.toLowerCase() === 'deploy') {
          this.showFormElements(['parameterNameForEnvironment']);
        }
        break;
      case 'GitHubAction':
        if(value.toLowerCase() === 'build'){
          this.showFormElements(['workflowID']);
        } else{
         this.hideFormElements(['workflowID']);
        }
        break;
    }
  };

  showFormElements = (elementIds: string[]) => {
    this.formTemplate.elements.forEach(element => {
      if (elementIds.includes(element.id)) {
        element.show = true;
        if (!element.disabled) {
          this.tool[element.id].enable();
        }
      }
    });
  };
  hideFormElements = (elementIds: string[]) => {
    this.formTemplate.elements.forEach(element => {
      if (elementIds.includes(element.id)) {
        element.show = false;
        this.tool[element.id].setValue('');
        this.tool[element.id].disable();
      }
    });
  };

  showLoadingOnFormElement = (elementId: string) => {
    this.formTemplate.elements.forEach(element => {
      if (element.id === elementId) {
        element.isLoading = true;
      }
    });
  };

  hideLoadingOnFormElement = (elementId: string) => {
    this.formTemplate.elements.forEach(element => {
      if (element.id === elementId) {
        element.isLoading = false;
      }
    });
  };

  promote(selectedId, arr) {
    for (let i = 0; i < arr.length; i++) {
      if (arr[i].id === selectedId) {
        const a = arr.splice(i, 1);
        arr.unshift(a[0]);
        break;
      }
    }
  }


  isSingleToolAllowed(toolName) {

    return this.singleToolAllowed.includes(toolName);
  }



  isVersionSupported = (version: string) => {
    const that = this;
    const supportedVersionList = this.versionList.filter(
      (version_) =>
        version_.branchSupport &&
        version_.type === that.selectedConnectionType
    );
    if (supportedVersionList[0].versions.includes(version)) {
      return true;
    } else {
      return false;
    }
  };

  apiVersionHandler = (version: any, elementId?) => {

    try {
      const selectedConnectionId = this.selectedConnection?.id;
      const organizationKey = this.tool['organizationKey'].value ? this.tool['organizationKey'].value : null;
      this.showLoadingOnFormElement('projectKey');
      if (version && selectedConnectionId) {
        this.http
          .getProjectKeyList(selectedConnectionId, organizationKey)
          .subscribe((data) => {
            if (data.success) {
              this.projectKeyList = [];
              // this.projectKeyList = data.data;
              data.data.forEach(element => {
                this.projectKeyList.push({
                  name: element,
                  code: element
                });
              });
              this.tool['branch'].enable()
              this.hideLoadingOnFormElement('projectKey');
              this.disableBranchDropDown = this.isVersionSupported(version);
            } else {
              this.projectKeyList = [];
              this.branchList = [];
              this.messenger.add({
                severity: 'error',
                summary: data.message,
              });
              this.hideLoadingOnFormElement('projectKey');
            }
          },(err) => {
            console.log(err);
            this.messenger.add({
              severity: 'error',
              summary: err.error.message,
            });
          });
      }
    } catch (error) {
      this.projectKeyList = [];
      this.branchList = [];
      this.messenger.add({
        severity: 'error',
        summary: 'Something went wrong, Please try again',
      });
    }
  };

  projectKeyClickHandler = (value: any, elementId?) => {
    try {
      this.showLoadingOnFormElement('branch');
      if (value && this.disableBranchDropDown) {
        this.http
          .getBranchListForProject(
            this.selectedConnection.id,
            this.toolForm.get('apiVersion').value,
            value,
          )
          .subscribe((data) => {
            if (data.success) {
              this.branchList = [];
              data.data.forEach(element => {
                this.branchList.push({
                  name: element,
                  code: element
                });
              });
              this.hideLoadingOnFormElement('branch');
            } else {
              this.branchList = [];
              this.messenger.add({
                severity: 'error',
                summary: data.message,
              });
              this.hideLoadingOnFormElement('branch');
            }
          });
      } else {
        this.hideLoadingOnFormElement('branch');
      }
    } catch (error) {
      this.branchList = [];
      this.messenger.add({
        severity: 'error',
        summary: 'Something went wrong, Please try again',
      });
      this.hideLoadingOnFormElement('branch');
    }
  };

  branchSelectHandler = (value: any, elementId?) => {
    console.log(value);
  };

  bambooDeploymentPjojectSelectionHandler = (value: any, elementId?) => {
    this.selectedDeploymentProject = this.deploymentProjectList?.filter(x => x.code == value)[0];
  };


  bambooPlanSelectHandler = (value: any, elementId?) => {
    this.showLoadingOnFormElement('branchName');
    this.bambooPlanKeyForSelectedPlan = [...this.bambooProjectDataFromAPI]
    .filter((item) => item.projectAndPlanName === value)[0]?.jobNameKey;
    this.toolForm.controls['planKey'].setValue(this.bambooPlanKeyForSelectedPlan);
    if (this.bambooPlanKeyForSelectedPlan) {
      try {
        this.http.getBranchesForProject(this.selectedConnection.id, this.bambooPlanKeyForSelectedPlan)
          .subscribe(data => {
            if (data.success) {
              this.bambooBranchDataFromAPI = [...data.data];
              this.bambooBranchList = [...this.bambooBranchDataFromAPI].map(item => item.branchName).map(element => ({
                name: element,
                code: element
              }));
              this.hideLoadingOnFormElement('branchName');
            } else {
              this.bambooBranchList = [];
              this.messenger.add({
                severity: 'error',
                summary: data.message,
              });
              this.hideLoadingOnFormElement('branchName');
            }
          });
      } catch (error) {
        this.bambooBranchList = [];
        this.messenger.add({
          severity: 'error',
          summary: error.message,
        });
        this.hideLoadingOnFormElement('branchName');
      }
    }
  };

  bambooBranchSelectHandler = (value: any, elementId?) => {
    this.selectedBambooBranchKey = [...this.bambooBranchDataFromAPI]
      .filter(item => item.branchName === value)[0]?.jobBranchKey;
    this.toolForm.controls['branchKey'].setValue(this.selectedBambooBranchKey);
  };

  initializeFields(toolName) {
    const self = this;
    switch (toolName) {
      case 'Jira':
        {
          this.formTitle = 'Jira';
          this.connectionTableCols = [
            {
              field: 'connectionName',
              header: 'Connection Name',
              class: 'long-text',
            },
            { field: 'username', header: 'User Name', class: 'long-text' },
            { field: 'offline', header: 'Is Offline?', class: 'small-text' },
            {
              field: 'apiEndPoint',
              header: 'API Endpoint',
              class: 'long-text',
            },
            { field: 'apiKey', header: 'API Key', class: 'normal' },
            { field: 'baseUrl', header: 'Base URL', class: 'long-text' },
            { field: 'cloudEnv', header: 'Cloud Env.?', class: 'small-text' },
            { field: 'isOAuth', header: 'OAuth', class: 'small-text' }
          ];

          this.formTemplate = {
            group: 'Jira',
            elements: [
              {
                type: 'text',
                label: 'JIRA Project Key',
                id: 'projectKey',
                validators: ['required'],
                containerClass: 'p-sm-6',
                show: true,
                tooltip: `User can get this value from JIRA/AZURE.<br />
               Generally all issues name are started with Project key<br /> <i>
                Impacted : Jira/Azure Collector and all Kpi</i>`,
                onFocusOut: this.projectKeyChanged
              },
              // {
              //   type: 'button',
              //   label: 'Fetch Boards',
              //   id: 'fetchBoardsBtn',
              //   containerClass: 'p-sm-2 p-d-flex p-ai-center',
              //   class: 'p-button-raised',
              //   show: true,
              //   clickEventHandler: this.fetchBoards,
              //   disabled: this.checkProjectKey
              // },
              {
                type: 'boolean',
                label: 'Use Boards',
                label2: 'Use JQL Query',
                id: 'queryEnabled',
                model: 'queryEnabled',
                onChangeEventHandler: this.jiraMethodChange,
                validators: [],
                containerClass: 'p-sm-12',
                tooltip: ``,
                disabled: 'false',
                show: true,
              },
              {
                type: 'autoComplete',
                label: 'JIRA Boards',
                id: 'boards',
                suggestions: 'filteredBoards',
                validators: ['required'],
                containerClass: 'p-sm-12',
                tooltip: `Shows all the boards that are setup in JIRA in a project associated with the selected project key`,
                filterEventHandler: this.filterBoards,
                selectEventHandler: this.onBoardSelect,
                unselectEventHandler: this.onBoardUnselect,
                show: true,
                isLoading: this.isLoading,
                disabled: this.checkBoards
              },
              {
                type: 'textarea',
                label: 'JQL Query',
                id: 'boardQuery',
                validators: [],
                containerClass: 'p-sm-12',
                disabled: 'queryEnabled',
                show: true,
              },
              {
                type: 'basicDropdown',
                label: 'JIRA Configuration Template',
                label2: '',
                id: 'metadataTemplateCode',
                onChangeEventHandler: this.jiraMethodChange,
                validators: [],
                containerClass: 'p-sm-6',
                tooltip: ``,
                disabled: 'false',
                show: true,
              },
            ],
          };
        }
        break;
      case 'Azure':
        {
          this.formTitle = 'Azure Boards';
          this.connectionTableCols = [
            {
              field: 'connectionName',
              header: 'Connection Name',
              class: 'long-text',
            },
            { field: 'username', header: 'User Name', class: 'normal' },
            { field: 'apiKey', header: 'API Key', class: 'normal' },
            { field: 'baseUrl', header: 'Base URL', class: 'long-text' },
            { field: 'isOAuth', header: 'OAuth', class: 'small-text' },
          ];
          this.formTemplate = {
            group: 'Azure',
            elements: [
              {
                type: 'text',
                label: 'Azure Project Key',
                id: 'projectKey',
                validators: ['required'],
                containerClass: 'p-sm-6',
                show: true,
                tooltip: `User can get this value from AZURE Boards.<br />
            Generally all issues name are started with Project key<br /> <i>
            Impacted : Azure Boards Collector and all Kpi</i>`,

              },
              {
                type: 'text',
                label: 'API Version',
                id: 'apiVersion',
                validators: ['required'],
                containerClass: 'p-sm-6',
                show: true,
                tooltip: `API version to be used for Azure pipeline API's.<br />
              <i>
                Example: 5.1 <br />
                Impacted : All AzurePipeline based KPIs</i>`,
              },
              {
                type: 'boolean',
                label: 'Use WIQL Query',
                label2: '',
                id: 'queryEnabled',
                model: 'queryEnabled',
                validators: [],
                containerClass: 'p-sm-12',
                tooltip: ``,
                disabled: 'false',
                show: true,
              },
              {
                type: 'textarea',
                label: 'Board Query',
                id: 'boardQuery',
                validators: [],
                containerClass: 'p-sm-12',
                disabled: 'queryEnabled',
                show: true,
              },
            ],
          };
        }
        break;
      case 'Zephyr':
        {
          this.formTitle = 'Zephyr';
          this.connectionTableCols = [
            {
              field: 'connectionName',
              header: 'Connection Name',
              class: 'long-text',
            },
            {
              field: 'baseUrl',
              header: 'Base URL',
              class: 'long-text'
            },
            {
              field: 'cloudEnv',
              header: 'Cloud Env.?',
              class: 'small-text'
            }
          ];

          this.formTemplate = {
            group: 'Zephyr',
            elements: [
              {
                type: 'text',
                label: 'Project Key*',
                id: 'projectKey',
                validators: ['required'],
                containerClass: 'p-sm-6 p-mr-6',
                show: true,
                tooltip: `User can get this value from JIRA/AZURE.<br />
              Generally all issues name are started with Project key<br /> <i>
              Impacted : Jira/Azure Collector and all Kpi</i>`,
              },
              {
                type: 'text',
                label: 'Component',
                id: 'projectComponent',
                validators: [],
                containerClass: 'p-sm-6 p-mr-6',
                show: true,
                tooltip: `Component field in Zephyr Server is a categorization tool used to classify specific features or modules within a project.<br />
                identify the component value to fetch particular project data .<br />
                <i> impacted: Regression Automation Coverage, In-Sprint Automation Coverage KPI, Sprint Automation</i>`,
              },
              {
                type: 'array',
                label: 'Label Values to identify Automated Regression Test Cases',
                id: 'regressionAutomationLabels',
                validators: [],
                containerClass: 'p-sm-6 p-mr-6',
                show: true,
                tooltip: `Specify the list Label Values to identify Automated Regression Test Cases.
                   Example: "Regression Automation" <br />
                  <i>Impacted : Regression Automation Coverage KPI</i>`
              },
              {
                type: 'array',
                label: 'Folder Path (In-Sprint Automation Coverage)',
                id: 'inSprintAutomationFolderPath',
                validators: [],
                containerClass: 'p-sm-6',
                show: true,
                tooltip: `Specify the list of folders to locate test cases
                  from.Multiple values can be provided in this fields.<br />
                  Folder path should be of the child folder where the actual test cases reside.
                  It can be copied from any of the test itself.
                  <b>Example: FolderOne/FolderTwo/FolderThree</b>
                  <br />
                  <i>Impacted :Zephyr Processor, In-Sprint Automation Coverage KPI</i>`
              },
              {
                type: 'array',
                label: 'Folder Path (Regression Automation Coverage)',
                id: 'regressionAutomationFolderPath',
                validators: [],
                containerClass: 'p-sm-6',
                show: true,
                tooltip: `Specify the list of folders to locate test cases
                  from.Multiple values can be provided in this fields.<br />
                  Folder path should be of the child folder where the actual test cases reside.
                  It can be copied from any of the test itself.
                  <b>Example: FolderOne/FolderTwo/FolderThree</b>
                  <br />
                  <i>Impacted :Zephyr Processor, Regression Automation Coverage</i>`
              },
              {
                type: 'text',
                label: 'Test Case Automation Field',
                id: 'testAutomated',
                validators: [],
                containerClass: 'p-sm-6',
                show: true,
                tooltip: `JIRA/Azure applications let you add custom fields in addition to the built-in
                  fields.
                  This is a custom field in JIRA/Azure which is used to identify if a test is
                  automated,manual, can be automated etc. So User need to provide that custom field
                  which is associated with testAutomated in Users JIRA/Azure Installation.
                  <br /><i>
                    Example: "customfield_12203"<br />
                    Impacted : Sprint Automation and Regression Automation</i>`

              },
              {
                type: 'array',
                label: 'Values for Automation Exclusion',
                id: 'canNotAutomatedTestValue',
                validators: [],
                containerClass: 'p-sm-6',
                show: true,
                tooltip: `Value to identify if test cases can be automated' }}
                  <br /><i>
                    Example: 'Can not Automate'<br />
                    Impacted : Sprint Automation and Regression Automation</i>`
              },
              {
                type: 'text',
                label: 'Test Case Automation Done Field',
                id: 'testAutomationStatusLabel',
                validators: [],
                containerClass: 'p-sm-6',
                show: true,
                tooltip: `JIRA/Azure applications let you add custom fields in addition to the built-in
                  fields.
                  So User need to provide that custom field is used to identify if the test case
                  automation is done in Users JIRA/Azure Installation.
                  <br /><i>
                    Example: "customfield_12203"<br />
                    Impacted : Sprint Automation and Regression Automation</i>`
              },
              {
                type: 'array',
                label: 'Values for Automation Completed',
                id: 'automatedTestValue',
                validators: [],
                containerClass: 'p-sm-6',
                show: true,
                tooltip: `Values to identify automated test cases. Multiple values can be provided in this
                  fields.
                  <br /><i>
                    Example: "Automated", "Yes"<br />
                    Impacted : Sprint Automation and Regression Automation </i>`
              },
              {
                type: 'text',
                label: 'Test Case Regression Field (For Automated Test Cases)',
                id: 'testRegressionLabel',
                validators: [],
                containerClass: 'p-sm-6',
                show: true,
                tooltip: `JIRA/Azure applications let you add custom fields in addition to the built-in
                  fields.
                  Provide the custom field which is associated with Test Case Regression (For
                  Automated Test Cases) in Users JIRA/Azure Installation.
                  <br /><i>
                    Example: "customfield_12203"<br />
                    Impacted : Sprint Automation and Regression Automation</i>`
              },
              {
                type: 'array',
                label: 'Test Case Regression Field Value',
                id: 'testRegressionValue',
                validators: [],
                containerClass: 'p-sm-6',
                show: true,
                tooltip: `Values to identify Test Case Regression (For Automated Test Cases). Multiple values can be provided in this fields.
        <br /><i>
          Example: "Automated", "Yes"<br />
          Impacted : Sprint Automation and Regression Automation </i>`
              }
            ],
          };
        }
        break;
      case 'Sonar':
        {
          this.formTitle = 'Sonar';
          this.connectionTableCols = [
            {
              field: 'connectionName',
              header: 'Connection Name',
              class: 'long-text',
            },
            { field: 'baseUrl', header: 'Base URL', class: 'long-text' },
            { field: 'username', header: 'User Name', class: 'long-text' },
            {
              field: 'cloudEnv',
              header: 'Cloud Env.?',
              class: 'small-text'
            }
          ];

          this.configuredToolTableCols = [

            {
              field: 'connectionName',
              header: 'Connection Name',
              class: 'long-text',
            },
            {
              field: 'organizationKey',
              header: 'Organization Key',
              class: 'long-text',
            },
            { field: 'apiVersion', header: 'API Version', class: 'normal' },
            { field: 'projectKey', header: 'Project Key', class: 'long-text' },
            { field: 'branch', header: 'Branch', class: 'long-text' },
            { field: 'gitLabSdmID', header: 'SDM ID', class: 'long-text' },
          ];
          
          this.formTemplate = {
            group: 'Sonar',
            elements: [
              {
                type: 'text',
                label: 'SDM ID',
                id: 'gitLabSdmID',
                validators: [{
                  type : 'pattern',
                  value : '^[a-zA-Z0-9,: ]+$'
                }],
                containerClass: 'p-sm-6',
                show: true,
                tooltip: `This key would not use for Sonar.<br />
              <i>
                Impacted : GitLab processor</i>`,
                onFocusOut : this.onSdmIdChange,
                errorMsg : "Only Alphanumeric,Comma and Colon allowed.",
                placeholder : "This key would not use for Sonar."
              },
              {
                type: 'text',
                label: 'Organization Key',
                id: 'organizationKey',
                validators: ['required'],
                containerClass: 'p-sm-6',
                show: true,
                tooltip: `Organization key is required in case of cloud setup.<br />
              <i>
                Impacted : Sonar processor</i>`,
              },
              {
                type: 'dropdown',
                label: 'API Version',
                id: 'apiVersion',
                validators: ['required'],
                containerClass: 'p-sm-6',
                optionsList: this.sonarVersionFinalList,
                changeHandler: this.apiVersionHandler,
                show: true,
                tooltip: `This property is used in Sonar processor.
              <br /><i>
                Example: 6.1<br />
                Impacted : All Sonar based KPIs</i>`,
              },
              {
                type: 'dropdown',
                label: 'Project Key',
                id: 'projectKey',
                validators: ['required'],
                containerClass: 'p-sm-6',
                optionsList: this.projectKeyList,
                filterValue: 'true',
                filterByName: 'name',
                changeHandler: this.projectKeyClickHandler,
                show: true,
                tooltip: `This property is used in Sonar processor.
              <br /><i>
                Impacted : All Sonar based KPIs</i>`,
                disabled: 'queryEnabled',
                isLoading: false
              },
              {
                type: 'dropdown',
                label: 'Branch',
                id: 'branch',
                validators: [],
                containerClass: 'p-sm-6',
                show: true,
                filterValue: 'true',
                filterByName: 'name',
                optionsList: this.branchList,
                changeHandler: this.branchSelectHandler,
                isLoading: false
              },
            ],
          };
        }
        break;
      case 'Jenkins':
        {
          this.formTitle = 'Jenkins';
          this.connectionTableCols = [
            {
              field: 'connectionName',
              header: 'Connection Name',
              class: 'long-text',
            },
            { field: 'baseUrl', header: 'Base URL', class: 'long-text' },
            { field: 'username', header: 'User Name', class: 'long-text' },
          ];

          this.configuredToolTableCols = [
            // { field: 'connectionId', header: 'Connection Id', class: 'long-text' },
            {
              field: 'connectionName',
              header: 'Connection Name',
              class: 'long-text',
            },
            { field: 'jobType', header: 'JobType', class: 'small-text' },
            { field: 'jobName', header: 'Job Name', class: 'long-text' },
          ];

          this.formTemplate = {
            group: 'Jenkins',
            elements: [
              {
                type: 'dropdown',
                label: 'Job Type',
                id: 'jobType',
                validators: ['required'],
                containerClass: 'p-sm-6',
                optionsList: this.jobType,
                changeHandler: this.jobTypeChangeHandler,
                show: true
              },
              {
                type: 'dropdown',
                label: 'Job Name',
                id: 'jobName',
                validators: ['required'],
                containerClass: 'p-sm-6',
                filterValue: 'true',
                filterByName: 'name',
                optionsList: this.jenkinsJobNameList,
                changeHandler: () => true,
                show: true,
                isLoading: false
              },
              {
                type: 'text',
                label: 'Parameter Name for Environment',
                id: 'parameterNameForEnvironment',
                validators: ['required'],
                containerClass: 'p-sm-6',
                disabled: false,
                show: false,
                tooltip: `The name of the parameter variable in which environment info/IP is passed.<br />
              <i>
                For Example : SERVER_IP</i>`,

              }
            ],
          };
        }
        break;
      case 'Teamcity':
        {
          this.formTitle = 'TeamCity';
          this.connectionTableCols = [
            {
              field: 'connectionName',
              header: 'Connection Name',
              class: 'long-text',
            },
            { field: 'baseUrl', header: 'Base URL', class: 'long-text' },
            { field: 'username', header: 'User Name', class: 'long-text' },
          ];

          this.configuredToolTableCols = [
            // { field: 'connectionId', header: 'Connection Id', class: 'long-text' },
            {
              field: 'connectionName',
              header: 'Connection Name',
              class: 'long-text',
            },
            { field: 'jobName', header: 'Job Name', class: 'long-text' },
          ];

          this.formTemplate = {
            group: 'TeamCity',
            elements: [
              {
                type: 'text',
                label: 'Job Name',
                id: 'jobName',
                validators: ['required'],
                containerClass: 'p-sm-6',
                show: true,
                tooltip: `Job name to access teamcity data.<br />
              <i>
                Impacted : All temacity based KPIs</i>`,
              },
            ],
          };
        }
        break;
      case 'Bamboo':
        {
          this.formTitle = 'Bamboo';
          this.connectionTableCols = [
            {
              field: 'connectionName',
              header: 'Connection Name',
              class: 'long-text',
            },
            { field: 'baseUrl', header: 'Base URL', class: 'long-text' },
            { field: 'username', header: 'User Name', class: 'long-text' },
          ];

          this.configuredToolTableCols = [
            { field: 'connectionName', header: 'Connection Name', class: 'long-text' },
            { field: 'jobType', header: 'Job Type', class: 'small-text' },
            { field: 'jobName', header: 'Plan Key', class: 'long-text' },
            { field: 'branch', header: 'Branch Key', class: 'long-text' },
            { field: 'deploymentProjectName', header: 'Deployment Project', class: 'long-text' }
          ];

          this.formTemplate = {
            group: 'Bamboo',
            elements: [
              {
                type: 'dropdown',
                label: 'Job Type',
                id: 'jobType',
                validators: ['required'],
                containerClass: 'p-sm-6',
                optionsList: this.jobType,
                changeHandler: this.jobTypeChangeHandler,
                show: true,
                isLoading: false
              },
              {
                type: 'dropdown',
                label: 'Deployment Project',
                id: 'deploymentProject',
                validators: ['required'],
                containerClass: 'p-sm-6',
                optionsList: this.deploymentProjectList,
                changeHandler: this.bambooDeploymentPjojectSelectionHandler,
                filterValue: 'true',
                filterByName: 'name',
                optionLabel: 'name',
                show: false,
                isLoading: false

              },
              {
                type: 'dropdown',
                label: 'Plan Name',
                id: 'planName',
                validators: ['required'],
                containerClass: 'p-sm-6',
                optionsList: this.bambooPlanList,
                changeHandler: this.bambooPlanSelectHandler,
                filterValue: 'true',
                filterByName: 'name',
                optionLabel: 'name',
                show: false,
                isLoading: false
              },
              {
                type: 'text',
                label: 'Plan Key',
                id: 'planKey',
                validators: [],
                containerClass: 'p-sm-6',
                disabled: true,
                show: false
              },
              {
                type: 'dropdown',
                label: 'Branches',
                id: 'branchName',
                validators: [],
                containerClass: 'p-sm-6',
                optionsList: this.bambooBranchList,
                filterValue: 'true',
                filterByName: 'name',
                changeHandler: this.bambooBranchSelectHandler,
                show: false,
                isLoading: false
              },
              {
                type: 'text',
                label: 'Branch Key',
                id: 'branchKey',
                validators: [],
                containerClass: 'p-sm-6',
                disabled: true,
                show: false
              },
            ],
          };
        }
        break;
      case 'Bitbucket':
        {
          this.formTitle = 'BitBucket';
          this.connectionTableCols = [
            {
              field: 'connectionName',
              header: 'Connection Name',
              class: 'long-text',
            },
            { field: 'baseUrl', header: 'Base URL', class: 'long-text' },
            { field: 'username', header: 'User Name', class: 'normal' },
            { field: 'apiKey', header: 'API Key', class: 'normal' },
            { field: 'cloudEnv', header: 'Cloud Env.?', class: 'small-text' },
          ];

          this.configuredToolTableCols = [
            // { field: 'connectionId', header: 'Connection Id', class: 'normal' },
            {
              field: 'connectionName',
              header: 'Connection Name',
              class: 'long-text',
            },
            { field: 'branch', header: 'Branch', class: 'long-text' },
            { field: 'repoSlug', header: 'Repo Slug', class: 'long-text' },
            {
              field: 'bitbucketProjKey',
              header: 'Project Key',
              class: 'long-text',
            },
          ];

          this.formTemplate = {
            group: 'BitBucket',
            elements: [
              {
                type: 'text',
                label: 'Branch',
                id: 'branch',
                validators: ['required'],
                containerClass: 'p-sm-6',
                show: true,
                tooltip: `Branch name to access BitBucket data.<br />
              <i>
                Impacted : All BitBucket based KPIs</i>`,
              },
              {
                type: 'text',
                label: 'Repo Slug',
                id: 'repoSlug',
                validators: ['required'],
                containerClass: 'p-sm-6',
                show: true,
                tooltip: `Repo Slug to access BitBucket data.<br />
              Eg:protocol//domain/<br/>bitbucket/scm/<br/>projectkey/reposlug
              <i>
                Impacted : All BitBucket based KPIs</i>`,
              },
              {
                type: 'text',
                label: 'Project Key',
                id: 'bitbucketProjKey',
                validators: ['required'],
                containerClass: 'p-sm-6',
                show: true,
                tooltip: `Bitbucket project key to access BitBucket data.<br />
              Eg:protocol//domain/<br/>bitbucket/scm/<br/>projectkey/reposlug
              <i>
                Impacted : All BitBucket based KPIs</i>`,
              },
            ],
          };
        }
        break;
      case 'GitLab':
        {
          this.formTitle = 'GitLab';
          this.connectionTableCols = [
            {
              field: 'connectionName',
              header: 'Connection Name',
              class: 'long-text',
            },
            { field: 'baseUrl', header: 'Base URL', class: 'long-text' },
            { field: 'username', header: 'User Name', class: 'long-text' },
          ];

          this.configuredToolTableCols = [
            // { field: 'connectionId', header: 'Connection Id', class: 'long-text' },
            {
              field: 'connectionName',
              header: 'Connection Name',
              class: 'long-text',
            },
            { field: 'branch', header: 'Branch', class: 'long-text' },
            {
              field: 'projectId',
              header: 'Gitlab Project Id',
              class: 'long-text',
            },
          ];

          this.formTemplate = {
            group: 'GitLab',
            elements: [
              {
                type: 'number',
                label: 'Gitlab Project Id',
                id: 'projectId',
                validators: ['required'],
                containerClass: 'p-sm-6',
                show: true,
                tooltip: ` GitLab Project Id to access GitLab data.<br />
              <i>
                Impacted : All GitLab based KPIs</i>`,
              },
              {
                type: 'text',
                label: 'Branch',
                id: 'branch',
                validators: ['required'],
                containerClass: 'p-sm-6',
                show: true,
                tooltip: `Branch name to access GitLab data.<br />
              <i>
                Impacted : All GitLab based KPIs</i>`,
              },
            ],
          };
        }
        break;
      case 'AzurePipeline':
        {
          this.formTitle = 'Azure Pipeline';
          this.connectionTableCols = [
            {
              field: 'connectionName',
              header: 'Connection Name',
              class: 'long-text',
            },
            { field: 'username', header: 'User Name', class: 'normal' },
            { field: 'baseUrl', header: 'Base URL', class: 'long-text' },
            { field: 'isOAuth', header: 'OAuth', class: 'small-text' },
          ];

          this.configuredToolTableCols = [
            {
              field: 'connectionName',
              header: 'Connection Name',
              class: 'long-text',
            },
            { field: 'jobType', header: 'Job Type', class: 'normal' },
            { field: 'jobName', header: 'Definitions', class: 'normal' },
          ];

          this.formTemplate = {
            group: 'AzurePipeline',
            elements: [
              {
                type: 'dropdown',
                label: 'Job Type',
                id: 'jobType',
                validators: ['required'],
                containerClass: 'p-sm-6',
                optionsList: this.jobType,
                changeHandler: this.jobTypeChangeHandler,
                show: true
              },
              {
                type: 'text',
                label: 'API Version',
                id: 'apiVersion',
                validators: [],
                containerClass: 'p-sm-6',
                tooltip: `API version to be used for Azure pipeline API's.<br />
              <i>
                Example: 5.1 <br />
                Impacted : All AzurePipeline based KPIs</i>`,
                show: false,
                disabled: true
              },
              {
                type: 'dropdown',
                label: 'Pipeline Name',
                id: 'azurePipelineName',
                validators: ['required'],
                containerClass: 'p-sm-6',
                optionsList: this.azurePipelineList,
                changeHandler: this.pipeLineDropdownHandler,
                show: true
              },
              {
                type: 'text',
                label: 'Definitions',
                id: 'jobName',
                validators: ['required'],
                containerClass: 'p-sm-6',
                tooltip: `Please enter Pipeline Id as fetched from Azure Devops Pipeline section<br />
              <i>
                Impacted : All AzurePipeline based KPIs</i>`,
                disabled: 'true',
                show: true,
              },
            ],
          };
        }
        break;
      case 'AzureRepository':
        {
          this.formTitle = 'Azure Repository';
          this.connectionTableCols = [
            {
              field: 'connectionName',
              header: 'Connection Name',
              class: 'long-text',
            },
            { field: 'username', header: 'User Name', class: 'normal' },
            { field: 'baseUrl', header: 'Base URL', class: 'long-text' },
            { field: 'isOAuth', header: 'OAuth', class: 'small-text' },
          ];

          this.configuredToolTableCols = [
            {
              field: 'connectionName',
              header: 'Connection Name',
              class: 'long-text',
            },
            { field: 'apiVersion', header: 'API Verion', class: 'normal' },
            {
              field: 'repositoryName',
              header: 'Repository Name',
              class: 'long-text',
            },
            { field: 'branch', header: 'Branch', class: 'long-text' },
          ];

          this.formTemplate = {
            group: 'AzureRepository',
            elements: [
              {
                type: 'text',
                label: 'API Version',
                id: 'apiVersion',
                validators: ['required'],
                containerClass: 'p-sm-6',
                show: true,
                tooltip: `API version to be used for Azure pipeline API's.<br />
              <i>
                Example: 5.1 <br />
                Impacted : All AzurePipeline based KPIs</i>`,
              },
              {
                type: 'text',
                label: 'Repository Name',
                id: 'repositoryName',
                validators: ['required'],
                containerClass: 'p-sm-6',
                show: true,
                tooltip: `Azure Repository Name.<br />
              <i>
                Impacted : All AzureRepository based KPIs</i>`,
              },
              {
                type: 'text',
                label: 'Branch',
                id: 'branch',
                validators: ['required'],
                containerClass: 'p-sm-6',
                show: true,
                tooltip: `Branch name to access Azure Repository data.<br />
              <i>
                Example: master<br />
                Impacted : All Azure Repository based KPIs</i>`,
              },
            ],
          };
        }
        break;
      case 'GitHub':
        {
          this.formTitle = 'GitHub';
          this.connectionTableCols = [
            {
              field: 'connectionName',
              header: 'Connection Name',
              class: 'long-text',
            },
            { field: 'username', header: 'User Name', class: 'normal' },
            { field: 'baseUrl', header: 'Base URL', class: 'long-text' },
          ];

          this.configuredToolTableCols = [
            {
              field: 'connectionName',
              header: 'Connection Name',
              class: 'long-text',
            },
            {
              field: 'repositoryName',
              header: 'Repository Name',
              class: 'long-text',
            },
            { field: 'branch', header: 'Branch', class: 'long-text' },
          ];

          this.formTemplate = {
            group: 'GitHub',
            elements: [
              {
                type: 'text',
                label: 'Repository Name',
                id: 'repositoryName',
                validators: ['required'],
                containerClass: 'p-sm-6',
                show: true,
                tooltip: `GitHub Repository Name.<br />
                <i>
                  Impacted : All GitHub Repository based KPIs</i>`,
              },
              {
                type: 'text',
                label: 'Branch',
                id: 'branch',
                validators: ['required'],
                containerClass: 'p-sm-6',
                show: true,
                tooltip: `Branch name to access GitHub Repository data.<br />
                <i>
                  Example: master<br />
                  Impacted : All GitHub Repository based KPIs</i>`,
              },
            ],
          };
        }
        break;
        case 'GitHubAction':
          {
            this.formTitle = 'GitHub Action';
            this.connectionTableCols = [
              {
                field: 'connectionName',
                header: 'Connection Name',
                class: 'long-text',
              },
              { field: 'username', header: 'User Name', class: 'normal' },
              { field: 'baseUrl', header: 'Base URL', class: 'long-text' },
            ];

            this.configuredToolTableCols = [
              { field: 'connectionName',header: 'Connection Name',class: 'long-text'},
              {field: 'repositoryName', header: 'Repository Name', class: 'long-text'},
              { field: 'jobType', header: 'Job Type', class: 'long-text' },
              { field: 'jobName', header: 'Workflow Name', class: 'long-text' },
            ];

            this.formTemplate = {
              group: 'GitHub Action',
              elements: [
                {
                  type: 'dropdown',
                  label: 'Job Type',
                  id: 'jobType',
                  containerClass: 'p-sm-6',
                  optionsList: this.jobType,
                  changeHandler: this.jobTypeChangeHandler,
                  validators: ['required'],
                  show: true,
                  isLoading: false
                },
                {
                  type: 'text',
                  label: 'Repository Name',
                  id: 'repositoryName',
                  validators: ['required'],
                  containerClass: 'p-sm-6',
                  show: true,
                  tooltip: `GitHub Repository Name.<br / <i>Impacted : All GitHub Repository based KPIs</i>`,
                  onFocusOut : this.getGitActionWorkflowName
                },
                {
                  type: 'dropdown',
                  label: 'WorkFlow Name',
                  id: 'workflowID',
                  containerClass: 'p-sm-6',
                  optionsList: this.gitActionWorkflowNameList,
                  changeHandler: ()=>true,
                  filterValue: 'true',
                  filterByName: 'name',
                  optionLabel: 'name',
                  show: false,
                  isLoading: false
                },
              ]
            };
          }
          break;
      case 'JiraTest':
        {
          this.formTitle = 'JiraTest';
          this.connectionTableCols = [
            {
              field: 'connectionName',
              header: 'Connection Name',
              class: 'long-text',
            },
            { field: 'username', header: 'User Name', class: 'long-text' },
            { field: 'offline', header: 'Is Offline?', class: 'small-text' },
            {
              field: 'apiEndPoint',
              header: 'API Endpoint',
              class: 'long-text',
            },
            { field: 'apiKey', header: 'API Key', class: 'normal' },
            { field: 'baseUrl', header: 'Base URL', class: 'long-text' },
            { field: 'cloudEnv', header: 'Cloud Env.?', class: 'small-text' },
            { field: 'isOAuth', header: 'OAuth', class: 'small-text' },
          ];

          this.formTemplate = {
            group: 'JiraTest',
            elements: [
              {
                type: 'text',
                label: 'JIRATEST Project Key',
                id: 'projectKey',
                validators: ['required'],
                containerClass: 'p-sm-6',
                show: true,
                tooltip: `User can get this value from JIRA/AZURE.<br />
                Generally all issues name are started with Project key<br /> <i>
                Impacted : Jira/Azure Collector and all Kpi</i>`
              },
              {
                type: 'array',
                label: 'Test Case Issue Type',
                id: 'jiraTestCaseType',
                validators: ['required'],
                containerClass: 'p-sm-6',
                tooltip: `Issue type of Test Case. Example: "Test", Impacted : Sprint Automation and Regression Automation`,
                show: true,
                // disabled: this.checkBoards
              },
              {
                type: 'dropdown',
                label: 'Test Case Automation Field',
                id: 'testAutomatedIdentification',
                validators: [],
                containerClass: 'p-sm-6',
                filterValue: 'true',
                filterByName: 'name',
                optionsList: this.testCaseIdentification,
                changeHandler: this.changeHandler,
                show: true
              },
              {
                type: 'text',
                label: 'Test Case Automation Custom Field Id',
                id: 'testAutomated',
                validators: [],
                containerClass: 'p-sm-6',
                show: false,
                disabled: false,
                tooltip: `Provide customfield name to identify test case is automatable or not.<br />
                Example: customfield_13907`
              },
              {
                type: 'array',
                label: 'Values for Automation',
                id: 'jiraCanBeAutomatedTestValue',
                suggestions: 'filteredBoards',
                validators: [],
                containerClass: 'p-sm-6',
                tooltip: `Enter the field labels used in Jira/Azure to identify if a test case can be automated`,
                show: false,
                isLoading: false,
              },
              {
                type: 'dropdown',
                label: 'Automation completed field',
                id: 'testAutomationCompletedIdentification',
                validators: [],
                containerClass: 'p-sm-6',
                optionsList: this.testCaseIdentification,
                changeHandler: this.changeHandler,
                show: true
              },
              {
                type: 'text',
                label: 'Automation Completed Custom Field Id',
                id: 'testAutomationCompletedByCustomField',
                validators: [],
                containerClass: 'p-sm-6',
                show: false,
                disabled: false,
                tooltip: `Provide customfield name to identify  if a test case is already automated`
              },
              {
                type: 'array',
                label: 'Values for Automation completed',
                id: 'jiraAutomatedTestValue',
                validators: [],
                containerClass: 'p-sm-6',
                tooltip: `Enter the field labels used in Jira/Azure to identify if a test case is already automated`,
                show: false,
                isLoading: false,
              },
              {
                type: 'dropdown',
                label: 'Regression test case identifier',
                id: 'testRegressionIdentification',
                validators: [],
                containerClass: 'p-sm-6',
                optionsList: this.testCaseIdentification,
                changeHandler: this.changeHandler,
                tooltip: `Jira/Azure allow addition of filtering data through custom field or labels. It can be used to identify regression test cases`,
                show: true
              },
              {
                type: 'text',
                label: 'Regression Test Case Custom Field Id',
                id: 'testRegressionByCustomField',
                validators: [],
                containerClass: 'p-sm-6',
                show: false,
                disabled: false,
                tooltip: `Provide customfield name to identify the test cases part of regression suite`
              },
              {
                type: 'array',
                label: 'Values for regression test cases',
                id: 'jiraRegressionTestValue',
                validators: [],
                containerClass: 'p-sm-6',
                tooltip: `Enter the field labels used in Jira/Azure to identify the test cases part of regression suite`,
                show: false,
                isLoading: false,
                // disabled: this.checkBoards
              },
              {
                type: 'array',
                label: 'Status to identify abandoned Test cases',
                id: 'testCaseStatus',
                suggestions: 'filteredBoards',
                validators: [],
                containerClass: 'p-sm-6',
                tooltip: `Select status like "Abandoned", "Deprecated" etc so that these can be excluded from Regression automation coverage, In Sprint automation coverage and Test case without story link KPI`,
                show: true,
                isLoading: false,
              },
            ],
          };
        }
        break;
        case 'RepoTool':
        {
          this.formTitle = 'RepoTool';
          this.connectionTableCols = [
            {
              field: 'connectionName',
              header: 'Connection Name',
              class: 'long-text',
            },
            { field: 'username', header: 'User Name', class: 'normal' },
            { field: 'repoToolProvider', header: 'RepoTool Provider', class: 'normal' },
            { field: 'httpUrl', header: 'Http URL', class: 'long-text' },
          ];
          this.configuredToolTableCols = [
            { field: 'connectionName',header: 'Connection Name',class: 'long-text'},
            { field: 'repositoryName', header: 'Repository Name', class: 'long-text'},
            { field: 'defaultBranch', header: 'Default Branch', class: 'long-text' },
            { field: 'branch', header: 'Scanning Branch', class: 'long-text' },
          ];
          this.formTemplate = {
            group: 'RepoTool',
            elements: [
              {
                type: 'text',
                label: 'Repository Name',
                id: 'repositoryName',
                validators: ['required'],
                containerClass: 'p-sm-6',
                show: true,
                tooltip: `Repository Name.<br / <i>Impacted : All Repository based KPIs</i>`,
                // onFocusOut : this.getGitActionWorkflowName
              },
              {
                type: 'text',
                label: 'Default Branch (to check how ahead/behind is scanning branch)',
                id: 'defaultBranch',
                validators: ['required'],
                containerClass: 'p-sm-6',
                show: true,
                tooltip: `Default Branch name to access Repository data. It is used to check how far ahead/behind it is from Scanning Branch<br />
                <i>
                  Example: master<br />
                  Impacted : All Repository based KPIs</i>`,
              },
              {
                type: 'text',
                label: 'Branch to Scan for KPIs',
                id: 'branch',
                validators: [],
                containerClass: 'p-sm-6',
                show: true,
                tooltip: `Scanning Branch name to access Repository data for Developer KPIs. If kept empty Default branch will be considered for scanning<br />
                <i>
                  Example: develop<br />
                  Impacted : All Repository based KPIs</i>`,
              },
            ],
          };
        }
        break;
    }

    const group = {};
    this.formTemplate.elements.forEach((inputTemplate) => {
      if (inputTemplate.validators) {
        const validatorArr = [];
        inputTemplate.validators.forEach((element) => {
          if(element === 'required'){
            validatorArr.push(Validators[element]);
          }else{
            validatorArr.push(Validators.pattern(element.value));
          }
          
        });

        group[inputTemplate.id] = new UntypedFormControl('', validatorArr);
      } else {
        group[inputTemplate.id] = new UntypedFormControl('');
      }
    });
    this.toolForm = new UntypedFormGroup(group);
    if (this.urlParam === 'Jira' || this.urlParam === 'Azure' || this.urlParam === 'Zephyr' || this.urlParam === 'JiraTest') {
      if (this.selectedToolConfig && this.selectedToolConfig.length) {
        for (const obj in this.selectedToolConfig[0]) {
          if (obj !== 'queryEnabled') {
            if (this.toolForm && this.toolForm.controls[obj]) {
              this.toolForm.controls[obj].setValue(
                this.selectedToolConfig[0][obj],
              );
              this.toolForm.controls[obj].markAsDirty();
            }
          } else {
            if (this.urlParam === 'Jira' || this.urlParam === 'Azure') {
              this.queryEnabled = this.selectedToolConfig[0]['queryEnabled'];
              const fakeEvent = {
                checked: this.queryEnabled
              };
              this.jiraMethodChange(fakeEvent, self);

            }
          }
        }
        if(this.urlParam === 'JiraTest'){
          if(this.toolForm.controls['testAutomatedIdentification']?.value){
            this.changeHandler(this.toolForm.controls['testAutomatedIdentification']?.value, 'testAutomatedIdentification');
          }
          if(this.toolForm.controls['testAutomationCompletedIdentification']?.value){
            this.changeHandler(this.toolForm.controls['testAutomationCompletedIdentification']?.value, 'testAutomationCompletedIdentification');
          }
          if(this.toolForm.controls['testRegressionIdentification']?.value){
            this.changeHandler(this.toolForm.controls['testRegressionIdentification']?.value, 'testRegressionIdentification');
          }
        }
        // this.tool['projectId'].disable();
        this.isEdit = true;
      }

      if (self.urlParam === 'Jira') {
        if(this.isEdit) {
          this.toolForm.controls['queryEnabled'].disable();
        }
      }
    }
  }

  projectKeyChanged(event, self) {
    self.fetchBoards(self);
  }

  jiraMethodChange(event = null, self) {
    this.submitted = false;
    const group = {};
    if (self.urlParam === 'Jira') {
      if (event && event.checked) {
        self.toolForm.controls['boards'].setValue([]);
        self.toolForm.controls['boards'].clearValidators();
        self.toolForm.controls['boards'].updateValueAndValidity();

        self.toolForm.controls['boardQuery'].setValidators([Validators.required]);
        self.toolForm.controls['boardQuery'].updateValueAndValidity();
      } else {
        self.toolForm.controls['boards'].setValidators([Validators.required]);
        self.toolForm.controls['boards'].updateValueAndValidity();

        self.toolForm.controls['boardQuery'].clearValidators();
        self.toolForm.controls['boardQuery'].updateValueAndValidity();
      }
    }

    const formData = {};
    for (const obj in self.tool) {
      formData[obj] = self.tool[obj].value;
    }

    for (const obj in formData) {
      if (self.toolForm && self.toolForm.controls[obj]) {
        self.toolForm.controls[obj].setValue(formData[obj]);
      }
    }

    if (self.selectedToolConfig && self.selectedToolConfig.length) {
      for (const obj in self.selectedToolConfig[0]) {
        if (obj !== 'queryEnabled') {
          if (self.toolForm && self.toolForm.controls[obj]) {
            self.toolForm.controls[obj].setValue(
              self.selectedToolConfig[0][obj],
            );
          }
        }
      }
    }
  }
  // convenience getter for easy access to form fields
  get tool() {
    return this.toolForm.controls;
  }

  save() {
    this.submitted = true;
    // return if form is invalid
    if (this.toolForm.invalid || !this.selectedConnection) {
      this.messenger.add({
        severity: 'error',
        summary: 'Please fill all fields and select a connection.',
      });
      return;
    }
    const submitData = {} as any;

    // TODO: Need to refactor
    if (this.selectedConnection.type === 'Bamboo') {
      submitData.jobType = this.tool['jobType'].value;

      if (this.tool['jobType'].value === 'Build') {
        submitData.jobName = this.tool['planKey'].value;

        if (this.tool['branchKey'].value) {
          submitData.branch = this.tool['branchKey'].value;
        }
      } else if (this.tool['jobType'].value === 'Deploy') {
        submitData.deploymentProjectName = this.selectedDeploymentProject.name;
        submitData.deploymentProjectId = this.selectedDeploymentProject.code;
      }

    } else {
      for (const obj in this.tool) {

        if (this.isInputFieldTypeArray(obj) && this.tool[obj].value === '') {
          submitData[obj] = [];
        } else {
          submitData[obj] = this.tool[obj].value;
        }

        if (obj === 'branch' && this.tool[obj].value === '') {
          delete submitData[obj];
        }

        if (obj === 'azurePipelineName') {
          delete submitData[obj];
        }
      }

    }

    if(this.urlParam === 'Jira'){
      submitData['metadataTemplateCode'] = submitData['metadataTemplateCode'].templateCode;
    }else{
      delete submitData['metadataTemplateCode'];
    }
    if(this.urlParam === 'GitHubAction'){
      submitData['jobName'] = this.gitActionWorkflowNameList.filter(obj=> obj.code === submitData['workflowID'])[0]?.name || "";
    }

    if (this.urlParam === 'AzurePipeline') {
      submitData['apiVersion'] = this.azurePipelineApiVersion;
      submitData['deploymentProjectName'] = this.tool['azurePipelineName'].value;
    }

    submitData['toolName'] = this.urlParam;
    submitData['basicProjectConfigId'] = this.selectedProject.id;
    submitData['connectionId'] = this.selectedConnection.id;


    // delete buttons
    delete submitData['fetchBoardsBtn'];
    // format boards
    if (!Array.isArray(submitData['boards'])) {
      submitData['boards'] = [submitData['boards']];
    }
    let successAlert = '';
    if (this.urlParam === 'Jira') {
      successAlert = 'If Jira processor is run after adding or removing board/s, then all data prior to this change will be deleted and fresh data will be fetched based on the updated list of boards';
    }
    if (!this.isEdit) {

      for (const obj in submitData) {
        if (submitData[obj]?.hasOwnProperty('name') && submitData[obj]?.hasOwnProperty('code')) {
          submitData[obj] = submitData[obj].name;
        }
      }
      this.http
        .addTool(this.selectedProject.id, submitData)
        .subscribe((response) => {
          if (response && response['success']) {
            this.selectedToolConfig = [response['data']];
            this.messenger.add({
              severity: 'success',
              summary: `${this.urlParam} config submitted!!  ${successAlert}`,
            });
            if (this.urlParam !== 'Jira' && this.urlParam !== 'Azure' && this.urlParam !== 'Zephyr') {
              // update the table
              if (!this.configuredTools || !this.configuredTools.length) {
                this.configuredTools = [];
              }

              // empty the form
              this.toolForm.reset();
              if(this.urlParam === 'Sonar'){
                this.tool['apiVersion'].enable();
                 this.tool['projectKey'].enable();
              }

              this.configuredTools.push(response['data']);
              this.configuredTools.forEach((tool) => {
                this.connections.forEach((connection) => {
                  if (tool.connectionId === connection.id) {
                    tool['connectionName'] = connection.connectionName;
                  }
                });
              });
            }
          } else {
            this.messenger.add({
              severity: 'error',
              summary: `${response['message'] ? response['message'] : 'Some error occurred. Please try again later'}`
            });
          }
        });
    } else {

      for (const obj in submitData) {
        if (submitData[obj]?.hasOwnProperty('name') && submitData[obj]?.hasOwnProperty('code')) {
          submitData[obj] = submitData[obj].name;
        }
      }

      this.http
        .editTool(
          this.selectedProject.id,
          this.selectedToolConfig[0].id,
          submitData,
        )
        .subscribe((response) => {
          if (response && response['success']) {
            this.selectedToolConfig = [response['data']];
            this.messenger.add({
              severity: 'success',
              summary: `${this.urlParam} config updated!! ${successAlert}`,
            });

            // update the table
            if (this.configuredTools && this.configuredTools.length) {
              this.configuredTools = this.configuredTools.map((tool) => {
                let newObj = Object.assign({}, tool);
                if (tool.id === response['data'].id) {
                  newObj = response['data'];
                }
                return newObj;
              });

              this.configuredTools.forEach((tool) => {
                this.connections.forEach((connection) => {
                  if (tool.connectionId === connection.id) {
                    tool['connectionName'] = connection.connectionName;
                  }
                });
              });
            }
            // empty the form
            if (this.urlParam !== 'Jira' && this.urlParam !== 'Azure' && this.urlParam !== 'Zephyr') {
              this.toolForm.reset();
            }
          } else {
            this.messenger.add({
              severity: 'error',
              summary: 'Some error occurred. Please try again later.',
            });
          }
        });
    }
  }

  isInputFieldTypeArray(inputFieldName) {

    const formElements = this.formTemplate['elements'];
    const theFormElement = formElements.find(formElement => formElement.id === inputFieldName);

    return theFormElement.type === 'array';


    // return [
    //     'inSprintAutomationFolderPath',
    //     'regressionAutomationFolderPath',
    //     'automatedTestValue',
    //     'canNotAutomatedTestValue',
    //     'testRegressionValue'
    //   ].includes(inputFieldName);
  }

  editTool(tool) {
    this.isEdit = true;
    this.selectedToolConfig = [tool];
    for (const obj in tool) {
      if (this.toolForm && this.toolForm.controls[obj]) {
        this.toolForm.controls[obj].setValue(tool[obj]);
      }
    }

    this.connections.forEach((connection) => {
      if (connection.id === tool.connectionId) {
        this.selectedConnection = connection;
      }
    });
  }

  addNewTool() {
    this.isEdit = false;
    for (const obj in this.toolForm.controls) {
      if (this.toolForm.controls[obj]) {
        this.toolForm.controls[obj].setValue('');
      }
    }
  }

  confirmDeteleTool(tool) {
    const context = this;
    this.confirmationService.confirm({
      message: 'Are you sure that you want to delete this tool?',
      header: 'Delete Confirmation',
      icon: 'pi pi-info-circle',

      accept: () => {
        context.deleteTool(tool);
      },
    });
  }

  deleteTool(tool) {
    this.http
      .deleteProjectToolConfig(tool.basicProjectConfigId, tool.id)
      .subscribe(
        (response) => {
          if (response && response['success']) {
            this.configuredTools = this.configuredTools.filter(
              (configuredTool) => configuredTool.id !== tool.id,
            );
            this.messenger.add({
              severity: 'success',
              summary: response['message'] || 'Tool deleted successfully',
            });
          } else {
            this.messenger.add({
              severity: 'error',
              summary: 'Some error occurred. Please try again later.',
            });
          }
        },
        (errorResponse) => {
          const error = errorResponse['error'];
          const msg =
            error['message'] || 'Some error occurred. Please try again later.';
          this.messenger.add({
            severity: 'error',
            summary: msg,
          });
        },
      );
  }

  // Preserve original property order
  originalOrder = (a: KeyValue<number, string>, b: KeyValue<number, string>): number => 0;

  changeHandler = (value:string, elementId) => {
    value = value['name'] || value;
    if (value.toLowerCase() === 'customfield' && elementId === 'testAutomatedIdentification') {
      this.showFormElements(['testAutomated', 'jiraCanBeAutomatedTestValue']);
    } if (value.toLowerCase() === 'labels' && elementId === 'testAutomatedIdentification') {
      this.hideFormElements(['testAutomated']);
      this.showFormElements(['jiraCanBeAutomatedTestValue']);
    } else if (value.toLowerCase() === 'customfield' && elementId === 'testAutomationCompletedIdentification') {
      this.showFormElements(['testAutomationCompletedByCustomField', 'jiraAutomatedTestValue']);
    } else if(value.toLowerCase() === 'labels' && elementId === 'testAutomationCompletedIdentification'){
      this.hideFormElements(['testAutomationCompletedByCustomField']);
      this.showFormElements(['jiraAutomatedTestValue']);
    }else if (value.toLowerCase() === 'customfield' && elementId === 'testRegressionIdentification') {
      this.showFormElements(['testRegressionByCustomField', 'jiraRegressionTestValue']);
    }else if (value.toLowerCase() === 'labels' && elementId === 'testRegressionIdentification') {
      this.hideFormElements(['testRegressionByCustomField']);
      this.showFormElements(['jiraRegressionTestValue']);
    }
  }

  getJiraTemplate(){
    const isKanban = this.selectedProject?.Type?.toLowerCase() === 'kanban' ? true : false;
    this.http.getJiraTemplate(this.selectedProject?.id).subscribe(resp=>{
      this.jiraTemplate = resp.filter(temp=>temp.tool?.toLowerCase() === 'jira' && temp.kanban === isKanban);
     if (this.selectedToolConfig && this.selectedToolConfig.length && this.jiraTemplate && this.jiraTemplate.length) {
        const selectedTemplate = this.jiraTemplate.find(tem=>tem.templateCode === this.selectedToolConfig[0]['metadataTemplateCode'])
        this.toolForm.get('metadataTemplateCode')?.setValue(selectedTemplate);
        if(selectedTemplate?.templateName === 'Custom Template'){
          this.toolForm.get('metadataTemplateCode').disable();
        }
      }
    })
  }

  getGitActionWorkflowName(event, self){
    self.showLoadingOnFormElement("workflowID");
    self.toolForm.get('workflowID').reset();
    self.gitActionWorkflowNameList = [];
    if(self.toolForm.get('jobType').value === 'Deploy'){
      self.hideLoadingOnFormElement("workflowID");
      return;
    }
    if(!self.selectedConnection?.id || event.target.value === ''){
      self.hideLoadingOnFormElement("workflowID");
      self.showPrompt('error',"Please fill all fields and select a connection.");
      return;
    }
    const postJson = {connectionID : self.selectedConnection?.id,repositoryName:event.target.value};
    self.http.getGitActionWorkFlowName(postJson).subscribe(resp=>{
      if(resp && resp['success']){
        self.gitActionWorkflowNameList = resp['data'].map(option=>{
          return {'name' : option['workflowName'],'code':option['workflowID']}
        })
        self.hideLoadingOnFormElement("workflowID");
      }else{
        self.hideLoadingOnFormElement("workflowID");
        self.showPrompt('error',"No Workflow list found.");
      }
    })

  }

  /** Generic method for showing notification prompt */
  showPrompt(type,msg){
    this.messenger.add({
      severity: type,
      summary: msg,
    });
  }

  onSdmIdChange(event,self){
    const sdmID = self.toolForm.get('gitLabSdmID').value.trim();
     if(sdmID){
      self.clearSonarForm()
      self.tool['organizationKey'].disable();
      self.tool['apiVersion'].disable();
      self.tool['projectKey'].disable();
      self.tool['branch'].disable();
     }else{
      self.toolForm.get('gitLabSdmID').setValidators([Validators.pattern('^[a-zA-Z0-9,: ]+$')]);
      self.enableDisableOrganizationKey(self.cloudEnv);
      self.tool['apiVersion'].enable();
      self.tool['projectKey'].enable();
     }
  }  
}
