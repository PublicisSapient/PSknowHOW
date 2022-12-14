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
     constructor(private httpService: HttpService, private service: SharedService, private messageService: MessageService) {
     }
     ngOnInit() {
         this.getKpisData();
    }
    getKpisData() {
       // api integration to get kpis data
       if (this.isEmptyObject(this.kpiListData)) {
        this.loader = true;
        this.httpService.getShowHideKpi().subscribe((response) => {
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
         list = item.kpis.map((kpi) => {
           kpiObj[kpi.kpiId] = new UntypedFormControl(kpi.shown);
           trueShowCount = kpi.shown ? ++trueShowCount : trueShowCount;
           return kpiObj;
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
     this.kpiListData['username'] = (localStorage.getItem('user_name'));
   }
     //update the changes to api
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
     this.httpService.submitShowHideKpiData(kpiListPayload)
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
       } else {
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
       let targetObj = {};
       const targetSelector = event.originalEvent?.target?.closest('.kpi-category-header')?.querySelector('.kpis-list');
       if (event.checked) {
        if(targetSelector.classList.contains('hide-kpisList')) {
          targetSelector.classList.remove('hide-kpisList');
        }
        targetObj = modifiedObj.kpis.map((item) => {
          item.shown = true;
          this.kpiFormValue.kpis['controls'][item.kpiId].setValue(true);
          return item;
        });
       } else {
        targetSelector.classList.add('hide-kpisList');
        targetObj = modifiedObj.kpis.map((item) => {
          item.shown = false;
          this.kpiFormValue.kpis['controls'][item.kpiId].setValue(false);
          return item;
        });
       }
     }
 }
