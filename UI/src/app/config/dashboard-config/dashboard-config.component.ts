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
 import { UntypedFormControl, UntypedFormGroup } from '@angular/forms';
 import { HttpService } from '../../services/http.service';
 import { MessageService } from 'primeng/api';
 import { SharedService } from '../../services/shared.service';
 import { GetAuthorizationService } from 'src/app/services/get-authorization.service';

 @Component({
   selector: 'app-dashboard-config',
   templateUrl: './dashboard-config.component.html',
   styleUrls: ['./dashboard-config.component.css']
 })
 export class DashboardconfigComponent implements OnInit {
    kpiListData: any = {};
    tabListContent: Object = {};
    tabHeaders: any = [];
    kpiForm: UntypedFormGroup = new UntypedFormGroup({});
    kpiList: any;
    selectedTab = 'scrum';
    kpiData: any;
    kpiChangesObj = {};
    loader = false;
    kpiToBeHidden;
    userName : string;
    userProjects : Array<any>;
    selectedProject : object;
     constructor(private httpService: HttpService, private service: SharedService, private messageService: MessageService,
     private getAuthorizationService : GetAuthorizationService) {
     }
     ngOnInit() {
         this.service.currentUserDetailsObs.subscribe(details=>{
          if(details){
            this.userName = details['user_name'];
          }
        });
        this.getProjects();
    }
    getKpisData(projectID) {
       // api integration to get kpis data
        this.httpService.getShowHideKpi(projectID).subscribe((response) => {
          this.loader = false;
          if (response[0] === 'error') {
            this.messageService.add({ severity: 'error', summary: 'Internal Server Error !!!' });
          } else {
            if (response.success === true) {
              this.kpiListData = response.data;
              this.setFormControlData();
              const kpiObjects = Object.keys(this.kpiListData);
              for(const i of kpiObjects) {
                  if (typeof this.kpiListData[i] === 'object') {
                    //removing Capacity kpi from iteration
                      if(i === 'scrum'){
                        const iterationData = this.kpiListData[i].find(boardDetails => boardDetails.boardName.toLowerCase() === 'iteration');
                        const kpiIndex= iterationData.kpis.findIndex(kpi => kpi.kpiId === 'kpi121');
                        this.kpiToBeHidden = iterationData.kpis.splice(kpiIndex,1);
                      }
                      this.tabListContent[i] =  this.kpiListData[i];
                      this.tabHeaders.push(i);
                  }
              }
            }
          }
        });
     }
     isEmptyObject(value) {
        return Object.keys(value).length === 0 && value.constructor === Object;
     }
   setFormControlData() {
     const kpiObj = {};
     const boardNames = {};
     let list = [];
     this.kpiData = [...this.kpiListData[this.selectedTab]];
     this.kpiData.forEach((item) => {
       let trueShowCount = 0;
       let allShownFlag = false;
       if ((item?.boardName && item?.kpis)) {
          item.kpis.forEach((kpi) => {
           kpiObj[kpi.kpiId] = new UntypedFormControl(kpi.shown);
           trueShowCount = kpi.shown ? ++trueShowCount : trueShowCount;
         });
         if (trueShowCount === item?.kpis?.length) {
           allShownFlag = true;
         }
         boardNames[item.boardName] = new UntypedFormControl(allShownFlag);
       }
     });
        this.kpiForm = new UntypedFormGroup({
          kpiCategories: new UntypedFormGroup(boardNames),
          kpis: new UntypedFormGroup(kpiObj)
        });
     }

     handleTabChange(event) {
        this.selectedTab = this.tabHeaders[event.index];
        this.setFormControlData();
        this.kpiChangesObj = {};
     }
     get kpiFormValue() {
         return this.kpiForm.controls;
     }
     // once user clicking save, get all the kpi changes form this.kpiChangesObj and sending to api
     save() {
         // getting the updated kpicategories
         const modifiedCategories = Object.keys(this.kpiChangesObj);
         const obj = this.kpiChangesObj;
         modifiedCategories.forEach(category => {
           // find each and every updated kpicategoryIndex
           const index = this.kpiData.findIndex(item => item.boardName === category);
           // updating kpi based on kpicategory
           obj[category].forEach(kpi => {
             const ind = this.kpiData[index].kpis.findIndex(item => item.kpiId === kpi.kpiId);
             this.kpiData[index].kpis.splice(ind,1,kpi);
           });
         });
         this.kpiListData[this.selectedTab] = this.kpiData;
         this.updateData();
     }
   assignUserNameForKpiData() {
     if (!this.kpiListData['username']) {
       delete this.kpiListData['id'];
     }
     this.kpiListData['username'] = this.userName;
     this.kpiListData['basicProjectConfigId'] = this.selectedProject['id'];
   }

  //Save show/hide configuration
   updateData() {
     this.assignUserNameForKpiData();
     this.loader = true;
     //Adding Capacity kpi to the payload if selected tab is scrum
     const kpiListPayload = JSON.parse(JSON.stringify(this.kpiListData));
     if (this.selectedTab.toLowerCase() === 'scrum') {
      const iterationData = JSON.parse(JSON.stringify(this.kpiData.find(kpiDetails => kpiDetails.boardName.toLowerCase() === 'iteration')));
      iterationData.kpis = [...this.kpiToBeHidden, ...iterationData.kpis];
      const iterationKpis = kpiListPayload['scrum'].find(boardDetails => boardDetails.boardName.toLowerCase() === 'iteration');
      if(iterationKpis){
        iterationKpis.kpis = iterationData.kpis;
      }
    }
     this.httpService.submitShowHideKpiData(kpiListPayload,this.selectedProject['id'])
       .subscribe(response => {
         this.loader = false;
         if (response[0] === 'error') {
           this.messageService.add({ severity: 'error', summary: 'Internal Server Error !!!' });
         } else {
           if (response.success === true) {
             this.messageService.add({ severity: 'success', summary: 'Successfully Saved', detail: '' });
             // setting in global Service
             this.service.setDashConfigData(response.data);
           } else {
             this.messageService.add({ severity: 'error', summary: 'Error in Saving Configuraion' });
           }
         }
       });
   }
     // onchanges getting kpi shown flag changes and maping its kpicategory with updated shown flag
     handleKpiChange(event, kpi, boardName, kpis) {
       const kpiObj = {...kpi};
       kpiObj.shown = event.checked;
       const kpiIds = kpis.map(function(item){
return item.kpiId;
});
       let showCount = 0;
       if (!event.checked) {
         this.kpiFormValue.kpiCategories['controls'][boardName].setValue(false);
         this.setMainDashboardKpiShowHideStatus(kpi.kpiId,false);
       } else {
        this.setMainDashboardKpiShowHideStatus(kpi.kpiId,true);
        kpiIds.forEach((id) => {
          if (this.kpiFormValue.kpis['controls'][id] && this.kpiFormValue.kpis['controls'][id].value){
            ++showCount;
          }
         });
         if (kpiIds.length === showCount) {
          this.kpiFormValue.kpiCategories['controls'][boardName].setValue(true);
         }
       }
       if(this.kpiChangesObj[boardName]) {
         this.kpiChangesObj[boardName].push(kpiObj);
       } else {
         this.kpiChangesObj[boardName] = [kpiObj];
       }
     }
     // on kpicategory flag change,  setting all of its kpi flag
     handleKpiCategoryChange(event, boardData) {
       const modifiedObj = {...boardData};
       const targetSelector = event.originalEvent?.target?.closest('.kpi-category-header')?.querySelector('.kpis-list');
       if (event.checked) {
        if(targetSelector.classList.contains('hide-kpisList')) {
          targetSelector.classList.remove('hide-kpisList');
        }
        modifiedObj.kpis.forEach((item) => {
          item.shown = true;
          this.kpiFormValue.kpis['controls'][item.kpiId].setValue(true);
          this.setMainDashboardKpiShowHideStatus(item.kpiId,true);
          return item;
        });
       } else {
        targetSelector.classList.add('hide-kpisList');
        modifiedObj.kpis.forEach((item) => {
          item.shown = false;
          this.kpiFormValue.kpis['controls'][item.kpiId].setValue(false);
          this.setMainDashboardKpiShowHideStatus(item.kpiId,false);
          return item;
        });
       }
     }

     setMainDashboardKpiShowHideStatus(kpiId,shown){
      const selectedKpi = this.tabListContent[this.selectedTab][0].kpis.find(kpiDetail => kpiDetail.kpiId === kpiId);
      if(selectedKpi){
        selectedKpi.shown = shown;
      }
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
          that.userProjects.sort((a, b) => a.name.localeCompare(b.name, undefined, { numeric: true }));
          that.selectedProject = that.userProjects[0];
          this.loader = true;
          this.tabHeaders = [];
          this.getKpisData(that.selectedProject['id'])
        }
      });
  }
  
  updateProjectSelection(projectSelectionEvent) {
    const currentSelection = projectSelectionEvent.value;
    if (currentSelection) {
      this.selectedProject = currentSelection;
    }
    this.loader = true;
    this.tabHeaders = [];
    this.getKpisData(this.selectedProject['id'])
  }
 }
