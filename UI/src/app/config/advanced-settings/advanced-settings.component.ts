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
import { ConfirmationService, MenuItem, MessageService } from 'primeng/api';
import { HttpService } from '../../services/http.service';
import { GetAuthorizationService } from '../../services/get-authorization.service';
import { DatePipe } from '@angular/common';
import { forkJoin, interval, Subscription } from 'rxjs';
import { environment } from 'src/environments/environment';
import { switchMap, takeWhile } from 'rxjs/operators';
import { Router, ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-advanced-settings',
  templateUrl: './advanced-settings.component.html',
  styleUrls: ['./advanced-settings.component.css']
})
export class AdvancedSettingsComponent implements OnInit {
  items: MenuItem[];
  selectedView: string;
  dataLoading = <boolean>false;
  showPreCalculatedDataForScrum = <boolean>false;
  showPreCalculatedDataForKanban = <boolean>false;
  showPrecalculatedConfigSection = <boolean>false;
  processorData = {};
  userProjects = [];
  selectedProject = {};
  processorsTracelogs = [];
  toolConfigsDetails = [];
  ssoLogin = environment.SSO_LOGIN;
  jirsStepsPopup: boolean = false;
  jiraExecutionSteps: any = [];
  jiraStatusContinuePulling = false;
  subscription: Subscription;
  dataMismatchObj: object = {};
  pid: string;
  configuredToolList: any;
  azureSnapshotToggleTooltip = 'Enable and click on "Run Now" to capture the initial scope of your active sprint after sprint planning. Subsequent changes will be tracked as scope changes. Applies to active sprints only-use with caution.'; 

  constructor(
    private httpService: HttpService,
    private messageService: MessageService,
    private getAuthorizationService: GetAuthorizationService,
    private confirmationService: ConfirmationService,
    private route: ActivatedRoute,
    public router: Router
  ) { }

  ngOnInit() {

    this.route.queryParams.subscribe(params => {
      this.pid = params['pid'];
    });

    this.items = [
      {
        label: 'Processor State',
        icon: 'pi pi-fw pi-cog',
        command: (event) => {
          this.switchView(event);
        },
        expanded: true
      }
    ];


    this.selectedView = 'processor_state';
    this.getProjects();
  }

  // called when user selects a tab from the left menu
  switchView(event) {
    if (event.item.label === 'Processor State') {
      this.selectedView = 'processor_state';
      this.getProjects();
    }
  }



  // used to fetch projects
  getProjects() {
    const that = this;
    this.httpService.getUserProjects('includeAll=false')
      .subscribe(response => {
        if (response[0] !== 'error' && !response.error) {
          if (this.getAuthorizationService.checkIfSuperUser()) {
            that.userProjects = response.data.map((proj) => ({
              name: proj.projectDisplayName,
              id: proj.id
            }));
          } else if (this.getAuthorizationService.checkIfProjectAdmin()) {
            that.userProjects = response.data.filter(proj => !this.getAuthorizationService.checkIfViewer(proj))
              .map((filteredProj) => ({
                name: filteredProj.projectDisplayName,
                id: filteredProj.id
              }));
          }
        } else {
          this.messageService.add({ severity: 'error', summary: 'User needs to be assigned a project for the access to work on dashboards.' });
        }
        if (that.userProjects != null && that.userProjects.length > 0) {
          that.userProjects.sort((a, b) => a.name?.localeCompare(b.name, undefined, { numeric: true }));
          that.selectedProject = this.pid ? that.userProjects.find(x => x.id === this.pid) : that.userProjects[0];
          that.getAllToolConfigs(that.selectedProject['id']);
          that.getProcessorsTraceLogsForProject(that.selectedProject['id']);
        }
      });
  }

  getAllToolConfigs(basicProjectConfigId) {
    this.httpService.getAllToolConfigs(basicProjectConfigId)
      .subscribe(response => {
        if (response['success']) {
          this.configuredToolList = response['data'];
          const uniqueTools = Array.from(new Set(response['data'].map(item => item.toolName)))
            .map(toolName => response['data'].find(item => item.toolName === toolName));
          this.toolConfigsDetails = uniqueTools;
        } else {
          this.messageService.add({ severity: 'error', summary: 'Error in fetching processor\'s data. Please try after some time.' });
        }
      });
  }

  getToolDetailsForProcessor(processorName) {
    return this.configuredToolList?.filter(toolDetails => toolDetails.toolName === processorName);
  }

  getProcessorsTraceLogsForProject(basicProjectConfigId) {
    const that = this;
    this.dataMismatchObj = {};
    this.httpService.getProcessorsTraceLogsForProject(basicProjectConfigId)
      .subscribe(response => {
        this.jiraExecutionSteps = []
        if (response.success) {
          that.processorsTracelogs = response.data;
          that.processorsTracelogs.forEach(pDetails => {
            if (pDetails.processorName !== 'Jira') {
              pDetails['executionOngoing'] = false;
            }
            if (pDetails.dataMismatch && pDetails.firstRunDate) {
              this.dataMismatchObj[pDetails.processorName] = pDetails.dataMismatch;
            }
          })

          if (this.decideWhetherLoaderOrNot(that.findTraceLogForTool('Jira'))) {
            that.jiraStatusContinuePulling = true;
            const runProcessorInput = {
              processor: 'Jira',
              projects: [this.selectedProject['id']]
            };
            that.getProcessorCompletionSteps(runProcessorInput);
          } else {
            that.jiraStatusContinuePulling = false;
            const jiraDAta = that.findTraceLogForTool('Jira');
            jiraDAta.executionOngoing = false;
          }

        } else {
          this.messageService.add({ severity: 'error', summary: 'Error in fetching processor\'s execution date. Please try after some time.' });
        }

      });
  }

  updateProjectSelection(projectSelectionEvent) {
    const currentSelection = projectSelectionEvent.value;
    if (currentSelection) {
      this.selectedProject = currentSelection;
    }
    if (this.subscription) {
      this.subscription.unsubscribe();
      this.jiraStatusContinuePulling = false;
    }
    this.getAllToolConfigs(this.selectedProject['id']);
    this.getProcessorsTraceLogsForProject(this.selectedProject['id']);

  }


  findTraceLogForTool(processorName) {
    if (processorName.toLowerCase() === 'jira') {
      const jiraInd = this.findCorrectJiraDetails();
      return this.processorsTracelogs[jiraInd];
    } else {
      return this.processorsTracelogs.find(ptl => ptl['processorName'] == processorName);
    }
  }

  showExecutionDate(processorName) {
    const traceLog = this.findTraceLogForTool(processorName);
    return (traceLog == undefined || traceLog == null || traceLog.executionEndedAt == 0) ? 'NA' : new DatePipe('en-US').transform(traceLog.executionEndedAt, 'dd-MMM-yyyy (EEE) - hh:mmaaa');
  }

  showProcessorLastState(processorName) {
    const traceLog = this.findTraceLogForTool(processorName);
    if (traceLog == undefined || traceLog == null || traceLog.executionEndedAt == 0) {
      return 'NA';
    } else if(traceLog.executionWarning){
      return 'Warning';
    } else {
      return traceLog.executionSuccess ? 'Success' : 'Failure';
    }
  }

  isProjectSelected() {
    return this.selectedProject != null || this.selectedProject != undefined;
  }

  endTimeConversion(time) {
    return new DatePipe('en-US').transform(time, 'dd-MMM-yyyy (EEE) - hh:mmaaa')
  }

  //used to run the processor's run(), called when run button is clicked
  runProcessor(processorName) {
    let runProcessorInput = {
      processor: processorName,
      projects: []
    };;
    if (this.isProjectSelected()) {
      runProcessorInput['projects'] = [this.selectedProject['id']];
    }
    const pDetails = this.findTraceLogForTool(processorName)
    if (pDetails) {
      pDetails['executionOngoing'] = true;
    }

    if (processorName === 'Jira') {
      this.resetLogs()
    }
    this.httpService.runProcessor(runProcessorInput)
      .subscribe(response => {
        if (!response.error && response.success) {
          this.updateflagsAfterTracelogSuccess(runProcessorInput);
        } else if (runProcessorInput['processor'].toLowerCase() === 'jira') {
          this.messageService.add({ severity: 'error', summary: response.data });
        } else {
          this.messageService.add({ severity: 'error', summary: `Error in running ${runProcessorInput['processor']} processor. Please try after some time.` });
          const pDetails = this.findTraceLogForTool(runProcessorInput['processor'])
          if (pDetails) {
            pDetails['executionOngoing'] = false;
          }
        }
      });
  }

    updateflagsAfterTracelogSuccess(runProcessorInput){
      this.messageService.add({ severity: 'success', summary: `${runProcessorInput['processor']} started successfully.` });
      if (runProcessorInput['processor'].toLowerCase() === 'jira') {
        this.jiraStatusContinuePulling = true;
        this.getProcessorCompletionSteps(runProcessorInput)
      }
      else {
        const pDetails = this.findTraceLogForTool(runProcessorInput['processor'])
        if (pDetails) {
          pDetails['executionOngoing'] = false;
        }
      }
     }


  shouldDisableRunProcessor(processorName) {

    if (this.getAuthorizationService.checkIfSuperUser()) {
      return false;
    }

    if (this.getAuthorizationService.checkIfProjectAdmin() && processorName?.toLowerCase() !== 'azure') {
      return false;
    }

    return true;
  }

  deleteProcessorData(processorDetails) {
    this.confirmationService.confirm({
      message: `Do you want to delete ${this.selectedProject['name']} data for ${processorDetails?.toolName}`,
      header: `Delete ${this.selectedProject['name']} Data?`,
      icon: 'pi pi-info-circle',
      accept: () => {
        this.deleteProcessorDataReq(processorDetails, this.selectedProject);
      },
      reject: () => {
        console.log("reject")
      }
    });
  }

  deleteProcessorDataReq(processorDetails, selectedProject) {
    const toolDetails = this.getToolDetailsForProcessor(processorDetails.toolName);
    const toolDetailSubscription = [];
    if (toolDetails?.length > 0) {
      toolDetails.forEach(toolDetail => {
        toolDetailSubscription.push(this.httpService.deleteProcessorData(toolDetail?.id, selectedProject?.id));
      });
      forkJoin(toolDetailSubscription).subscribe(response => {
        if (response.find(res => !res['success'])) {
          this.messageService.add({ severity: 'error', summary: 'Error in deleting project data. Please try after some time.' });
        } else {
          this.messageService.add({ severity: 'success', summary: 'Data deleted Successfully.', detail: '' });
          this.getAllToolConfigs(selectedProject?.id);
          this.getProcessorsTraceLogsForProject(this.selectedProject['id']);
        }
      }, error => {
        this.messageService.add({ severity: 'error', summary: 'Something went wrong. Please try again after sometime.' });
      });
    } else {
      this.messageService.add({ severity: 'error', summary: 'Something went wrong. Please try again after sometime.' });
    }
  }


  getProcessorCompletionSteps(runProcessorInput) {
    const jiraInd = this.findCorrectJiraDetails();
    this.subscription = interval(15000).pipe(
      takeWhile(() => this.jiraStatusContinuePulling),
      switchMap(() => this.httpService.getProgressStatusOfProcessors(runProcessorInput))
    ).subscribe(response => {
      if (response && response['success']) {
        if (this.decideWhetherLoaderOrNot(response['data'][0])) {
          this.processorsTracelogs[jiraInd].executionOngoing = true;
          this.jiraStatusContinuePulling = true
        } else {
          this.processorsTracelogs[jiraInd].executionOngoing = false;
          this.jiraStatusContinuePulling = false;
          this.getProcessorsTraceLogsForProject(this.selectedProject['id'])
        }
        Object.assign(this.findTraceLogForTool('Jira'), response['data'][0])
      }
    })
  }

  resetLogs() {
    const jiraInd = this.findCorrectJiraDetails();
    if (jiraInd !== -1) {
      this.processorsTracelogs[jiraInd].errorMessage = '';
      this.processorsTracelogs[jiraInd].progressStatusList = [];

    }
  }

  findCorrectJiraDetails() {
    const processorName = 'Jira';
    const jiraCount = this.processorsTracelogs.filter(ptl => ptl['processorName'] == processorName).length;
    if (jiraCount === 1) {
      return this.processorsTracelogs.findIndex(ptl => ptl['processorName'] == processorName);
    } else if (jiraCount >= 1) {
      return this.processorsTracelogs.findIndex(ptl => ptl['processorName'] == processorName && ptl['progressStats'] === true);
    } else {
      this.processorsTracelogs.push({ processorName: 'Jira', errorMessage: '', progressStatusList: [], executionOngoing: false, executionEndedAt: 0, isDeleteDisable: true });
      return this.processorsTracelogs.length;
    }
  }

  decideWhetherLoaderOrNot(jiraLogDetails) {
    if (jiraLogDetails && jiraLogDetails?.executionOngoing && jiraLogDetails?.progressStatusList?.length) {
      const logs = jiraLogDetails.progressStatusList;
      const lastLOgTime = logs[logs.length - 1].endTime;
      const currentTime = new Date().getTime();
      let differenceInMilliseconds = Math.abs(currentTime - lastLOgTime);
      if (differenceInMilliseconds > 600000) {
        return false;
      } else if (differenceInMilliseconds <= 600000) {
        return true;
      } else {
        return false;
      }
    } else {
      return false;
    }

  }

  backToProjectList() {
    this.router.navigate(['/dashboard/Config/ProjectList']);
  }

  getToolCategory(ProcessorName) {
    const categoryWiseTool = {
      'Project Management': ['jira', 'azure'],
      'Test Management': ['zephyr', 'jiratest'],
      'Source Code Management': ['github', 'gitlab', 'bitbucket', 'azurerepository'],
      'Security': ['sonar'],
      'Build': ['bamboo', 'teamcity', 'azurepipeline', 'argocd', 'githubaction', 'jenkins']
    }

    for (const category in categoryWiseTool) {
      if (categoryWiseTool[category].includes(ProcessorName?.toLowerCase())) {
        return category;
      }
    }
    return '';
  }

  getSCMToolTimeDetails(processorName) {
    const traceLog = this.findTraceLogForTool(processorName);
    return (traceLog == undefined || traceLog == null || traceLog.executionResumesAt == 0) ? 'NA' : new DatePipe('en-US').transform(traceLog.executionResumesAt, 'dd-MMM-yyyy (EEE) - hh:mmaaa');
  }

  isSCMToolProcessor(processorName) {
    const scmTools = ['GitHub','GitLab','Bitbucket','AzureRepository'];
    return scmTools.includes(processorName)
  }

  azureRefreshActiveSprintReportToggleChange(details) {
    this.httpService.editTool(this.selectedProject['id'],details['id'],details)
    .subscribe(response => {
      if (response['success']) {
        this.messageService.add({ severity: 'success', summary: 'Configuration updated successfully' });
      } else {
        this.messageService.add({ severity: 'error', summary: 'Error in fetching processor\'s data. Please try after some time.' });
      }
    });
  }

  ngOnDestroy(): void {
    if (this.subscription) {
      this.subscription.unsubscribe();
      this.jiraStatusContinuePulling = false;
    }
  }
}
