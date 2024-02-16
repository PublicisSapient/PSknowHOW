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
import { ConfirmationService, MenuItem } from 'primeng/api';
import { HttpService } from '../../services/http.service';
import { SharedService } from '../../services/shared.service';
import { GetAuthorizationService } from '../../services/get-authorization.service';
import { MessageService } from 'primeng/api';
import { DatePipe } from '@angular/common';
import { forkJoin } from 'rxjs';
import { environment } from 'src/environments/environment';

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

    if (this.getAuthorizationService.checkIfSuperUser() && !this.ssoLogin) {
      this.items.push({
        label: 'Authentication Type',
        icon: 'pi pi-book',
        command: (event) => {
          this.switchView(event);
        }
      });
    }


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
      case 'Authentication Type': {
        this.selectedView = 'ad_settings';
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
          console.log(JSON.stringify(that.selectedProject));
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
        //console.log(JSON.stringify(response));

        if (response.success) {
          that.processorsTracelogs = response.data;
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

    //console.log(JSON.stringify( this.selectedProject));
    this.getProcessorsTraceLogsForProject(this.selectedProject['id']);
    this.getAllToolConfigs(this.selectedProject['id']);

  }


  findTraceLogForTool(processorName) {
    return this.processorsTracelogs.find(ptl => ptl['processorName'] == processorName);
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

    this.httpService.runProcessor(runProcessorInput)
      .subscribe(response => {
        if (response[0] !== 'error' && !response.error && response.success) {
          this.messageService.add({ severity: 'success', summary: `${runProcessorInput['processor']} started successfully.` });
        } else {
          if(runProcessorInput['processor'].toLowerCase() === 'jira'){
            this.messageService.add({ severity: 'error', summary: response.data });
          }else{
            this.messageService.add({ severity: 'error', summary: `Error in running ${runProcessorInput['processor']} processor. Please try after some time.` });
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

}
