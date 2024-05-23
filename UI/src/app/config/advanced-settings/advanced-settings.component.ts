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

import { Component, OnInit,OnDestroy } from '@angular/core';
import { ConfirmationService, MenuItem, MessageService } from 'primeng/api';
import { HttpService } from '../../services/http.service';
import { SharedService } from '../../services/shared.service';
import { GetAuthorizationService } from '../../services/get-authorization.service';
import { DatePipe } from '@angular/common';
import { forkJoin,interval,Subscription } from 'rxjs';
import { environment } from 'src/environments/environment';
import { switchMap, takeWhile } from 'rxjs/operators';

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
  toolConfigsDetails=[];
  ssoLogin = environment.SSO_LOGIN;
  jirsStepsPopup : boolean = false;
  jiraExecutionSteps : any = [];
  jiraStatusContinuePulling = false;
   subscription: Subscription;

  constructor(private httpService: HttpService, private messageService: MessageService, private getAuthorizationService: GetAuthorizationService,
    private service: SharedService, private confirmationService: ConfirmationService) { }

  ngOnInit() {

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
    // this.getServerRole();
    // this.getPreCalculatedConfig();
    this.getProcessorData();
    this.getProjects();
  }

  // called when user selects a tab from the left menu
  switchView(event) {
    switch (event.item.label) {
      case 'Processor State': {
        this.selectedView = 'processor_state';
        this.getProcessorData();
        this.getProjects();
      }
        break;
    }
  }


  // used to fetch the processors
  getProcessorData() {
    this.dataLoading = true;
    this.httpService.getProcessorData()
      .subscribe(processorData => {
        this.dataLoading = false;
        if (processorData[0] !== 'error' && !processorData.error) {
          this.processorData = processorData;

        } else {
          this.messageService.add({ severity: 'error', summary: 'Error in fetching Processor data. Please try after some time.' });
        }
      });
  }

  // used to fetch projects
  getProjects() {
    const that = this;
    this.httpService.getUserProjects()
      .subscribe(response => {
        if (response[0] !== 'error' && !response.error) {
          if (this.getAuthorizationService.checkIfSuperUser()) {
            that.userProjects = response.data.map((proj) => ({
                name: proj.projectName,
                id: proj.id
              }));
          } else if (this.getAuthorizationService.checkIfProjectAdmin()) {
            that.userProjects = response.data.filter(proj => !this.getAuthorizationService.checkIfViewer(proj))
              .map((filteredProj) => ({
                  name: filteredProj.projectName,
                  id: filteredProj.id
                }));
          }
        } else {
          this.messageService.add({ severity: 'error', summary: 'User needs to be assigned a project for the access to work on dashboards.' });
        }

        if (that.userProjects != null && that.userProjects.length > 0) {
          //a.localeCompare( b, undefined, { numeric: true } )
          that.userProjects.sort((a, b) => a.name.localeCompare(b.name, undefined, { numeric: true }));
          that.selectedProject = that.userProjects[0];
          that.getProcessorsTraceLogsForProject(that.selectedProject['id']);
          that.getAllToolConfigs(that.selectedProject['id']);
        }


      });
  }

  getAllToolConfigs(basicProjectConfigId) {
    this.httpService.getAllToolConfigs(basicProjectConfigId)
      .subscribe(response => {
        if (response['success']) {
          this.toolConfigsDetails = response['data'];
        } else {
          this.messageService.add({ severity: 'error', summary: 'Error in fetching processor\'s data. Please try after some time.' });
        }
      });
  }

  getToolDetailsForProcessor(processorName) {
    return this.toolConfigsDetails.filter(toolDetails => toolDetails.toolName === processorName);
  }

  getProcessorsTraceLogsForProject(basicProjectConfigId) {
    const that = this;
    this.httpService.getProcessorsTraceLogsForProject(basicProjectConfigId)
      .subscribe(response => {
        this.jiraExecutionSteps = []
        if (response.success) {
          that.processorsTracelogs = response.data;
          that.processorsTracelogs.map(pDetails=>{
              if(pDetails.processorName !== 'Jira'){
                pDetails['executionOngoing'] = false;
              }
          })

          if(that.findTraceLogForTool('Jira')?.executionOngoing){
            that.jiraStatusContinuePulling = true;
            const runProcessorInput = {
              processor: 'Jira',
              projects: [this.selectedProject['id']]
            };
            that.getProcessorCompletionSteps(runProcessorInput);
          }

        } else {
          this.messageService.add({ severity: 'error', summary: 'Error in fetching processor\'s execution date. Please try after some time.' });
        }

      });
  }

  updateProjectSelection(projectSelectionEvent) {
    //console.log(JSON.stringify(projectSelectionEvent));
    const currentSelection = projectSelectionEvent.value;
    if (currentSelection) {
      this.selectedProject = currentSelection;
    }
    if (this.subscription) {
      this.subscription.unsubscribe();
      this.jiraStatusContinuePulling = false;
    }


    //console.log(JSON.stringify( this.selectedProject));
    this.getProcessorsTraceLogsForProject(this.selectedProject['id']);
    this.getAllToolConfigs(this.selectedProject['id']);

  }


  findTraceLogForTool(processorName) {
    if(processorName.toLowerCase() === 'jira'){
      const jiraInd = this.findCorrectJiraDetails();
      return this.processorsTracelogs[jiraInd];
    }else{
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
    } else {
      return traceLog.executionSuccess ? 'Success' : 'Failure';
    }
  }

  isProjectSelected() {
    return this.selectedProject != null || this.selectedProject != undefined;
  }

  endTimeConversion(time){
    return new DatePipe('en-US').transform(time, 'dd-MMM-yyyy (EEE) - hh:mmaaa')
  }

  //used to run the processor's run(), called when run button is clicked
  runProcessor(processorName) {
    let runProcessorInput = null;
    if (this.isProjectSelected()) {

      runProcessorInput = {
        processor: processorName,
        projects: [this.selectedProject['id']]
      };


    } else {
      runProcessorInput = {
        processor: processorName,
        projects: []
      };

    }
    const pDetails = this.findTraceLogForTool(processorName)
    if(pDetails){
      pDetails['executionOngoing'] = true;
    }

    if(processorName === 'Jira'){
      this.resetLogs()
    }
    this.httpService.runProcessor(runProcessorInput)
      .subscribe(response => {
        if (response[0] !== 'error' && !response.error && response.success) {
          this.messageService.add({ severity: 'success', summary: `${runProcessorInput['processor']} started successfully.` });
          if(runProcessorInput['processor'].toLowerCase() === 'jira'){
            this.jiraStatusContinuePulling = true;
            this.getProcessorCompletionSteps(runProcessorInput)
          }else{
            const pDetails = this.findTraceLogForTool(runProcessorInput['processor'])
            if(pDetails){
              pDetails['executionOngoing'] = false;
            }
          }
        } else {
          if(runProcessorInput['processor'].toLowerCase() === 'jira'){
            this.messageService.add({ severity: 'error', summary: response.data });
          }else{
            this.messageService.add({ severity: 'error', summary: `Error in running ${runProcessorInput['processor']} processor. Please try after some time.` });
            const pDetails = this.findTraceLogForTool(runProcessorInput['processor'])
            if(pDetails){
              pDetails['executionOngoing'] = false;
            }
          }
        }
      });
  }


  shouldDisableRunProcessor() {

    if (this.getAuthorizationService.checkIfSuperUser()) {
      return false;
    }

    if (this.getAuthorizationService.checkIfProjectAdmin()) {
      return false;
    }

    return true;
  }

  deleteProcessorData(processorDetails){
    this.confirmationService.confirm({
			message:`Do you want to delete ${this.selectedProject['name']} data for ${processorDetails?.processorName}`,
			header: `Delete ${this.selectedProject['name']} Data?`,
			icon: 'pi pi-info-circle',
			accept: () => {
				this.deleteProcessorDataReq(processorDetails,this.selectedProject);
			},
      reject : ()=>{
        console.log("reject")
      }
		});
  }

  deleteProcessorDataReq(processorDetails, selectedProject) {
    const toolDetails = this.getToolDetailsForProcessor(processorDetails.processorName);
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
        }
      }, error => {
        this.messageService.add({ severity: 'error', summary: 'Something went wrong. Please try again after sometime.' });
      });
    } else {
      this.messageService.add({ severity: 'error', summary: 'Something went wrong. Please try again after sometime.' });
    }
  }


  getProcessorCompletionSteps(runProcessorInput){
    const jiraInd = this.findCorrectJiraDetails();
    this.subscription = interval(15000).pipe(
      takeWhile(() => this.jiraStatusContinuePulling),
      switchMap(() => this.httpService.getProgressStatusOfProcessors(runProcessorInput))
    ).subscribe(response => {
      if (response && response['success']) {
        if (response['data'][0]['executionOngoing']) {
            this.processorsTracelogs[jiraInd].executionOngoing = true;
            this.jiraStatusContinuePulling = true
        } else {
          this.processorsTracelogs[jiraInd].executionOngoing = false;
          this.jiraStatusContinuePulling = false;
        }
        let preLOgs = this.findTraceLogForTool('Jira');
        preLOgs= Object.assign(preLOgs,response['data'][0])
      }
    })
  }

  resetLogs(){
    const jiraInd = this.findCorrectJiraDetails();
      if(jiraInd !== -1){
        this.processorsTracelogs[jiraInd].errorMessage = '';
        this.processorsTracelogs[jiraInd].progressStatusList = [];

      }
  }

  findCorrectJiraDetails(){
    const processorName = 'Jira';
    const jiraCount = this.processorsTracelogs.filter(ptl => ptl['processorName'] == processorName).length;
    if(jiraCount === 1){
      return this.processorsTracelogs.findIndex(ptl => ptl['processorName'] == processorName);
    }else if(jiraCount >= 1){
       return this.processorsTracelogs.findIndex(ptl => ptl['processorName'] == processorName && ptl['progressStats'] === true);
    }else{
      this.processorsTracelogs.push({processorName : 'Jira',errorMessage : '',progressStatusList : [],executionOngoing : false,executionEndedAt : 0,isDeleteDisable : true});
      return  this.processorsTracelogs.length;
    }
  }

  ngOnDestroy(): void {
    if (this.subscription) {
      this.subscription.unsubscribe();
      this.jiraStatusContinuePulling = false;
    }
  }
}
