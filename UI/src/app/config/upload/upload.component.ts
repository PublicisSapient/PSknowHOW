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

import { ChangeDetectorRef, Component, OnInit, ViewChild } from '@angular/core';
import { SharedService } from '../../services/shared.service';
import { DomSanitizer } from '@angular/platform-browser';
import { MenuItem } from 'primeng/api';
import { environment } from '../../../environments/environment';
import { GetAuthService } from '../../services/getauth.service';
import { MessageService } from 'primeng/api';
import { HttpService } from '../../services/http.service';
import { first } from 'rxjs/operators';
import { GetAuthorizationService } from '../../services/get-authorization.service';
import { FormControl, UntypedFormControl, UntypedFormGroup, Validators } from '@angular/forms';
import { ManageAssigneeComponent } from '../manage-assignee/manage-assignee.component';
declare let $: any;

interface TepSubmissionReq {
    projectNodeId: string;
    projectName: string;
    sprintNodeId?: string;
    capacity?: string;
    startDate?: string;
    endDate?: string;
    totalTestCases?: string;
    executedTestCase?: string;
    passedTestCase?: string;
    sprintId?: string;
    sprintName?: string;
    executionDate?: string;
    kanban: boolean;
    basicProjectConfigId?: string;
}

@Component({
    selector: 'app-upload',
    templateUrl: './upload.component.html',
    styleUrls: ['./upload.component.css']
})
export class UploadComponent implements OnInit {
    error = '';
    message = '';
    uploadedFile: File;
    logoImage: any;
    invalid: boolean;
    isUploadFile = true;
    isUploadEnabled = true;
    items: MenuItem[];
    selectedView: string;
    kanban: boolean;
    baseUrl = environment.baseUrl;  // Servers Env
    projects: any;
    selectedProject: any;
    dropdownSettingsProject = {};
    filter_kpiRequest = <any>'';
    selectedFilterData = <any>{};
    selectedFilterCount = 0;
    filterData = <any>[];
    masterData = <any>{};
    filterApplyData = <any>{};
    currentSelectionLabel = '';
    filterType = 'Default';
    startDate: any;
    endDate: any;
    executionDate: any;
    reqObj: TepSubmissionReq;
    isCapacitySaveDisabled = true;
    isTestExecutionSaveDisabled = true;
    capacityErrorMessage = '';
    testExecutionErrorMessage = '';
    isCheckBoxChecked: boolean;
    todayDate: any;
    loader = false;
    executionDateGroup: any;
    isSuperAdmin = false;
    filterForm: UntypedFormGroup;
    projectListArr: Array<object> = [];
    sprintListArr: Array<object> = [];
    selectedFilterArray: Array<any> = [];
    trendLineValueList: any[];
    filteredSprints: any = [];
    sprintDetails: any;
    projectDetails: any;
    selectedProjectBaseConfigId: string;
    popupForm: UntypedFormGroup;
    preventRedirection = true;
    selectedFile: File;

    statusMessage = {
        200: 'Data Saved Successfully!!',
        201: 'Invalid file.',
        202: 'Invalid file type.',
        203: 'Error in saving file on the disk.',
        404: 'Hierarchy Level is  not present in system.',
        500: 'Some Error Occurred. Please try again after sometime.',
        501: 'Emm master is not uploaded please upload it first.'
    };
    cols: any;
    sprintsData: any;
    tabHeaders = ['Scrum', 'Kanban'];
    tabContentHeaders = { upload_tep: 'Test Execution Percentage Table', upload_Sprint_Capacity: 'Capacity Table' };
    selectedHeader: string;
    showPopuup = false;
    noData = false;
    capacityScrumData: any;
    capacityKanbanData: any;
    testExecutionScrumData: any;
    testExecutionKanbanData: any;
    selectedSprintDetails: any;
    selectedSprintId: any;
    selectedSprintName: any;
    tableLoader = true;
    currentDate = new Date();
    isToggleEnableForSelectedProject = false;
    displayAssignee = false;
    projectJiraAssignees = {};
    manageAssigneeList = [];
    projectAssigneeRoles = [];
    projectAssigneeRolesObj;
    projectCapacityEditMode = false;
    selectedSprint;
    expandedRows = {};
    selectedSprintAssigneFormArray = [];
    selectedSprintAssigneValidator = [];
    jiraAssigneeLoader = false;
    isAddtionalTestField = false;
    addtionalTestFieldColumn = [
        {
            header: 'Automatable Test Cases',
            field: 'automatableTestCases'
        },
        {
            header: 'Test Cases Automated',
            field: 'automatedTestCases'
        },
        {
            header: 'Total Regression Test Cases',
            field: 'totalRegressionTestCases'
        },
        {
            header: 'Regression Test Cases Automated',
            field: 'automatedRegressionTestCases'
        }
    ]

    constructor(private http_service: HttpService, private messageService: MessageService, private getAuth: GetAuthService, private sharedService: SharedService, private sanitizer: DomSanitizer, private getAuthorisation: GetAuthorizationService, private cdr: ChangeDetectorRef) {
    }

    ngOnInit() {
        this.cols = {
            testExecutionScrumKeys: [
                {
                    header: 'Sprint Name',
                    field: 'sprintName'
                },
                {
                    header: 'Sprint Status',
                    field: 'sprintState'
                },
                {
                    header: 'Total Test Cases',
                    field: 'totalTestCases'
                },
                {
                    header: 'Executed Test Cases',
                    field: 'executedTestCase'
                },
                {
                    header: 'Passed Test Cases',
                    field: 'passedTestCase'
                },
              
            ],
            testExecutionKanbanKeys: [
                {
                    header: 'Execution Date',
                    field: 'executionDate'
                },
                {
                    header: 'Total Test Cases',
                    field: 'totalTestCases'
                },
                {
                    header: 'Executed Test Cases',
                    field: 'executedTestCase'
                },
                {
                    header: 'Passed Test Case',
                    field: 'passedTestCase'
                },
            ]
        };

        this.cols.testExecutionKanbanKeys = this.cols.testExecutionKanbanKeys.concat(this.addtionalTestFieldColumn);
        this.cols.testExecutionScrumKeys = this.cols.testExecutionScrumKeys.concat(this.addtionalTestFieldColumn);
        this.isSuperAdmin = this.getAuthorisation.checkIfSuperUser();
        this.items = [
            {
                label: 'Test Execution Percentage',
                icon: 'pi pi-pw pi-test-execution',
                command: (event) => {
                    this.switchView(event);
                },
                expanded: !this.isSuperAdmin
            }
        ];
        this.dropdownSettingsProject = {
            // singleSelection: false,
            text: 'Select Project',
            selectAllText: 'Select All',
            unSelectAllText: 'UnSelect All',
            enableSearchFilter: true,
            classes: 'multi-select-custom-class'
        };

        this.selectedView = 'logo_upload';

        if (this.isSuperAdmin) {
            this.items.unshift(
                {
                    label: 'Upload Logo',
                    icon: 'pi pi-image',
                    command: (event) => {
                        this.switchView(event);
                    },
                    expanded: false
                }
            );
            this.selectedView = 'logo_upload';
            this.error = '';
            this.message = '';
        } else {
            this.handleTepSelect('upload_tep');
            document.querySelector('.horizontal-tabs .btn-tab.pi-scrum-button')?.classList?.add('btn-active');
            document.querySelector('.horizontal-tabs .btn-tab.pi-kanban-button')?.classList?.remove('btn-active');
        }
        this.selectedView = 'cert_upload';
        if (this.isSuperAdmin) {
            this.items.unshift(
                {
                    label: 'Upload certificate',
                    icon: 'pi pi-image',
                    command: (event) => {
                        this.switchView(event);
                    },
                    expanded: true
                }
            );
            this.selectedView = 'cert_upload';
        } else {
            this.handleTepSelect('upload_tep');
            document.querySelector('.horizontal-tabs .btn-tab.pi-scrum-button')?.classList?.add('btn-active');
            document.querySelector('.horizontal-tabs .btn-tab.pi-kanban-button')?.classList?.remove('btn-active');
        }

        this.kanban = false;
        this.getUploadedImage();
        this.startDate = '';
        this.endDate = '';
        this.executionDate = '';
        this.setFormControlValues();
    }
    setFormControlValues() {
        this.filterForm = new UntypedFormGroup({
            selectedProjectValue: new UntypedFormControl()
        });
        if (this.selectedView === 'upload_tep') {
            if (this.kanban) {
                this.popupForm = new UntypedFormGroup({
                    executionDate: new UntypedFormControl(),
                    totalTestCases: new UntypedFormControl(),
                    executedTestCase: new UntypedFormControl(),
                    passedTestCase: new UntypedFormControl(),
                    automatedTestCases: new UntypedFormControl(),
                    automatableTestCases: new UntypedFormControl(),
                    automatedRegressionTestCases: new UntypedFormControl(),
                    totalRegressionTestCases: new UntypedFormControl()
                });
            } else {
                this.popupForm = new UntypedFormGroup({
                    totalTestCases: new UntypedFormControl(),
                    executedTestCase: new UntypedFormControl(),
                    passedTestCase: new UntypedFormControl(),
                    automatedTestCases: new UntypedFormControl(),
                    automatableTestCases: new UntypedFormControl(),
                    automatedRegressionTestCases: new UntypedFormControl(),
                    totalRegressionTestCases: new UntypedFormControl()
                });
            }
        }
    }
    /* when "test execution percentage" is selected */
    handleTepSelect(tab) {
        this.selectedView = tab;
        this.todayDate = new Date();
        this.executionDate = '';
        this.testExecutionErrorMessage = '';
        this.kanban = false;
        this.isTestExecutionSaveDisabled = true;
        this.loader = true;
        this.setFormControlValues();
        this.selectedProjectBaseConfigId = '';
        this.getFilterDataOnLoad();
    }
    addActiveToTab() {
        document.querySelector('.horizontal-tabs .btn-tab.pi-scrum-button')?.classList?.add('btn-active');
        document.querySelector('.horizontal-tabs .btn-tab.pi-kanban-button')?.classList?.remove('btn-active');
    }
    switchView(event) {
        switch (event.item.label) {
            case 'Upload Logo': {
                this.selectedView = 'logo_upload';
                this.error = '';
                this.message = '';
            }
                break;
            case 'Upload certificate': {
                this.selectedView = 'cert_upload';
                this.error = '';
                this.message = '';
            }
                break;
            case 'Test Execution Percentage': {
                this.handleTepSelect('upload_tep');
                this.addActiveToTab();
            }
                break;
        }
    }


    /*Rendering the image */
    getUploadedImage() {
        this.http_service.getUploadedImage().pipe(first())
            .subscribe(
                data => {
                    if (data['image']) {
                        this.logoImage = 'data:image/png;base64,' + data['image'];
                        const blob: Blob = new Blob([this.logoImage], { type: 'image/png' });
                        blob['objectURL'] = this.sanitizer.bypassSecurityTrustUrl((window.URL.createObjectURL(blob)));
                        this.uploadedFile = new File([blob], 'logo.png', { type: 'image/png' });
                    }
                });
    }


    /*After selection of file get the byte array and dimension of file*/
    onSelectImage(event) {
        return new Promise((resolve) => {
            let isImageFit = true;
            this.error = undefined;
            /*convert image to  byte array*/
            if (event.target.files[0]) {
                const reader = new FileReader();
                reader.readAsDataURL(event.target.files[0]);
                reader.onload = (evt: any) => { // when file has loaded
                    this.logoImage = reader.result;
                    const img = new Image();
                    img.src = this.logoImage;
                    img.onload = () => {
                        if (img.width > 250 || img.height > 100) {
                            this.error = 'Image is too big(' + img.width + ' x ' + img.height + '). The maximum dimensions are 250 x 100 pixels';
                            isImageFit = false;
                            resolve(isImageFit);
                        } else {
                            resolve(isImageFit);
                        }
                    };
                };
            }

        });
    }

    /*check validation of file*/
    validate() {
        this.invalid = false;
        this.message = undefined;
        this.error = undefined;


        /*Validate the size*/
        const imagesize = this.uploadedFile.size / 1024;

        if (imagesize > 100) {
            this.invalid = true;
            this.error = 'File should not be more than 100 KB';
        }

        /*Validate the format*/
        const mimeType = this.uploadedFile.name;
        if (mimeType.match(/\.(jpe?g|png|gif|jpeg|JPEG|JPG|GIF|PNG)$/i) == null) {
            this.error = 'Only JPG, PNG and GIF files are allowed.';
            this.invalid = true;
        }
    }


    /*Upload the file*/
    async onUpload(event) {

        this.uploadedFile = event.target.files[0];

        /*validate the file */
        this.validate();
        if (this.invalid) {
            return;
        }
        /*conversion of image to byte array */
        const isImageFit = await this.onSelectImage(event);

        /*call service to upload */
        if (isImageFit) {
            this.http_service.uploadImage(this.uploadedFile).pipe(first())
                .subscribe(
                    data => {
                        if (data['status'] && data['status'] === 500) {
                            this.error = data['statusText'];
                        } else {
                            this.message = data['message'];
                            this.sharedService.setLogoImage(this.uploadedFile);

                        }
                    });
        }
    }

    /*call service to delete*/
    onDelete() {
        this.isUploadFile = false;
        this.http_service.deleteImage().pipe(first())
            .subscribe(
                data => {
                    this.isUploadFile = true;
                    if (data) {
                        this.message = 'File deleted successfully';
                        this.logoImage = undefined;
                        this.error = undefined;
                        this.sharedService.setLogoImage(undefined);
                    }
                });
    }

    // called when user switches the "Scrum/Kanban" switch
    kanbanActivation(type) {
        this.selectedSprintAssigneValidator = [];
        const scrumTarget = document.querySelector('.horizontal-tabs .btn-tab.pi-scrum-button');
        const kanbanTarget = document.querySelector('.horizontal-tabs .btn-tab.pi-kanban-button');
        if (type === 'scrum') {
            scrumTarget?.classList?.add('btn-active');
            kanbanTarget?.classList?.remove('btn-active');
        } else {
            scrumTarget?.classList?.remove('btn-active');
            kanbanTarget?.classList?.add('btn-active');
        }
        this.kanban = type === 'scrum' ? false : true;
        this.startDate = '';
        this.endDate = '';
        this.executionDate = '';
        this.capacityErrorMessage = '';
        this.testExecutionErrorMessage = '';
        this.isCapacitySaveDisabled = true;
        this.isTestExecutionSaveDisabled = true;
        this.loader = true;
        this.tableLoader = true;
        this.noData = false;
        this.testExecutionKanbanData = [];
        this.testExecutionScrumData = [];
        this.capacityKanbanData = [];
        this.capacityScrumData = [];
        this.projectDetails = {};
        this.selectedProjectBaseConfigId = '';
        this.getFilterDataOnLoad();
    }

    resetProjectSelection() {
        this.projectListArr = [];
        this.trendLineValueList = [];
        this.filterForm?.get('selectedProjectValue').setValue('');

    }

    // gets data for filters on load
    getFilterDataOnLoad() {

        if (this.filter_kpiRequest && this.filter_kpiRequest !== '') {
            this.filter_kpiRequest.unsubscribe();
        }

        this.selectedFilterData = {};
        this.selectedFilterCount = 0;

        this.selectedFilterData.kanban = this.kanban;
        this.selectedFilterData['sprintIncluded'] = ['CLOSED', 'ACTIVE', 'FUTURE'];
        this.filter_kpiRequest = this.http_service.getFilterData(this.selectedFilterData)
            .subscribe(filterData => {
                if (filterData[0] !== 'error') {
                    this.filterData = filterData['data'];
                    if (this.filterData && this.filterData.length > 0) {
                        this.projectListArr = this.sortAlphabetically(this.filterData.filter(x => x.labelName.toLowerCase() == 'project'));
                        this.projectListArr = this.makeUniqueArrayList(this.projectListArr);
                        const defaultSelection = this.selectedProjectBaseConfigId ? false : true;
                        this.checkDefaultFilterSelection(defaultSelection);
                        if (Object.keys(filterData).length === 0) {
                            this.resetProjectSelection();
                            // show error message
                            this.messageService.add({ severity: 'error', summary: 'Projects not found.' });
                        }
                    } else {
                        this.resetProjectSelection();
                    }


                } else {
                    this.resetProjectSelection();
                    // show error message
                    this.messageService.add({ severity: 'error', summary: 'Error in fetching filter data. Please try after some time.' });
                }
                this.loader = false;
            });
    }

    // this function is called when checkbox or canceled is clicked  in filter
    filterSelectedData(e, data, currentSelection, hitApply, currentSelectionLabel, dataArray?, isFilterSelectDeselectInDropDown?) {

        // Remove previous checked filters
        this.filterData.forEach(filter => {
            if (!!filter.filterData && filter.level >= currentSelection && filter.filterData.length == 1) {
                filter.filterData[0].isSelected = false;
            }
        });

        // Remove previous selected elements
        dataArray.forEach(obj => {
            if (obj.nodeId !== data.nodeId) {
                obj.isSelected = false;
            }
        });

        this.currentSelectionLabel = currentSelectionLabel;
        this.selectedFilterData['currentSelection'] = currentSelection;
        this.selectedFilterData['currentSelectionLabel'] = currentSelectionLabel;
        if (typeof (isFilterSelectDeselectInDropDown) !== 'undefined') {
            this.isCheckBoxChecked = isFilterSelectDeselectInDropDown;
        } else {
            this.isCheckBoxChecked = (!!e && !!e.target && !!e.target.checked) ? true : false;
        }
        this.selectedFilterData.filterDataList = <any>[];
        this.selectedFilterData.filterDataList = JSON.parse(JSON.stringify(this.filterData));


        for (let i = 0; i < this.selectedFilterData.filterDataList.length; i++) {
            if (this.selectedFilterData.filterDataList[i].level === currentSelection) {
                for (let j = 0; j < this.selectedFilterData.filterDataList[i].filterData.length; j++) {
                    if (JSON.stringify(this.selectedFilterData.filterDataList[i].filterData[j]) === JSON.stringify(data)) {
                        if (!(e.target !== undefined && e.target.checked)) {
                            this.filterData[i].filterData[j].isSelected = false;
                            this.selectedFilterData.filterDataList[i].filterData[j].isSelected = false;
                        }
                    }

                }
            } else {

                for (let j = 0; j < this.selectedFilterData.filterDataList[i].filterData.length; j++) {
                    const obj = this.selectedFilterData.filterDataList[i].filterData[j];
                    if (obj.isSelected === false || this.selectedFilterData.filterDataList[i].level > currentSelection) {

                        this.selectedFilterData.filterDataList[i].filterData.splice(j, 1);
                        j = -1;
                    }
                }
            }
        }

        this.selectedFilterData.kanban = this.kanban;
        if (this.selectedFilterData) {
            this.http_service.getFilterData(this.selectedFilterData)
                .subscribe(filterData => {
                    this.renderSpecificFilters(filterData, hitApply);
                });
        }
        this.enableDisableSubmitButton();

    }

    renderSpecificFilters(filterData, hitApply) {
        filterData.forEach(filter => {
            if (!!filter.filterData && filter.filterData.length == 1 &&
                (filter.level < this.selectedFilterData['currentSelection'])) {
                filter.filterData[0].isSelected = true;
            }
        });
        this.filterData = filterData;
        this.selectedFilterData.filterDataList = this.filterData;
        this.enableDisableSubmitButton();
        this.checkdisabled();
        if (hitApply && this.selectedFilterCount !== 0) {
            // this.applyChanges(false);
        } else if (hitApply && this.selectedFilterCount === 0) {
            this.filterApplyData = {};
        }
    }

    checkdisabled() {
        this.selectedFilterCount = 0;
        for (const index in this.filterData) {
            for (const filterObjectIndex in this.filterData[index].filterData) {
                if (this.filterData[index].filterData[filterObjectIndex].isSelected) {
                    this.selectedFilterCount++;
                }
            }
        }
    }

    setFormValuesEmpty() {
        if (this.filterForm && this.filterForm.controls) {
            Object.keys(this.filterForm?.controls).forEach(key => {
                if (this.filterForm.get(key) && key !== 'selectedProjectValue') {
                    this.filterForm?.get(key)?.setValue('');
                }
            });
        }
        if (this.popupForm && this.popupForm.controls) {
            Object.keys(this.popupForm?.controls).forEach(key => {
                if (this.popupForm.get(key)) {
                    this.popupForm?.get(key)?.setValue('');
                }
            });
        }
        if (this.reqObj) {
            for (const capReqField in this.reqObj) {
                this.reqObj[capReqField] = '';
            }
        }
    }
    AddOrUpdateData(data) {
        this.showPopuup = true;
        this.executionDate = data?.executionDate ? data?.executionDate : '';
        this.selectedSprintName = data?.sprintName;
        this.selectedSprintId = this.selectedView === 'upload_tep' ? data?.sprintId : data?.sprintNodeId;
        this.startDate = data?.startDate;
        this.endDate = data?.endDate;
        this.reqObj = {
            projectNodeId: data?.projectNodeId,
            projectName: data?.projectName,
            kanban: this.kanban,
            basicProjectConfigId: data?.basicProjectConfigId
        };
        if (!this.kanban) {
            if (this.selectedView === 'upload_tep') {
                this.reqObj['sprintId'] = this.selectedSprintId;
            } else {
                this.reqObj['sprintNodeId'] = this.selectedSprintId;
            }
        } else {
            if (this.selectedView === 'upload_tep') {
                this.reqObj['executionDate'] = this.executionDate;
            }
        }
        if (this.selectedView === 'upload_tep') {
            this.popupForm = new UntypedFormGroup({
                totalTestCases: new UntypedFormControl(data?.totalTestCases ? data?.totalTestCases : ''),
                executedTestCase: new UntypedFormControl(data?.executedTestCase ? data?.executedTestCase : ''),
                passedTestCase: new UntypedFormControl(data?.passedTestCase ? data?.passedTestCase : ''),
                automatedTestCases: new UntypedFormControl(data?.automatedTestCases ? data?.automatedTestCases : ''),
                automatableTestCases: new UntypedFormControl(data?.automatableTestCases ? data?.automatableTestCases : ''),
                automatedRegressionTestCases: new UntypedFormControl(data?.automatedRegressionTestCases ? data?.automatedRegressionTestCases : ''),
                totalRegressionTestCases: new UntypedFormControl(data?.totalRegressionTestCases ? data?.totalRegressionTestCases : ''),
            });

            this.reqObj['totalTestCases'] = data?.totalTestCases;
            this.reqObj['executedTestCase'] = data?.executedTestCase;
            this.reqObj['passedTestCase'] = data?.passedTestCase;
            this.reqObj['automatedTestCases'] = data?.automatedTestCases;
            this.reqObj['automatableTestCases'] = data?.automatableTestCases;
            this.reqObj['automatedRegressionTestCases'] = data?.automatedRegressionTestCases;
            this.reqObj['totalRegressionTestCases'] = data?.totalRegressionTestCases;

        }
        this.enableDisableSubmitButton();
    }

    submitTestExecution() {
        this.reqObj['totalTestCases'] = this.popupForm?.get('totalTestCases').value;
        this.reqObj['executedTestCase'] = this.popupForm?.get('executedTestCase').value;
        this.reqObj['passedTestCase'] = this.popupForm?.get('passedTestCase').value;
        this.reqObj['automatedTestCases'] = this.popupForm?.get('automatedTestCases').value === -1 ? '' : this.popupForm?.get('automatedTestCases').value;
        this.reqObj['automatableTestCases'] = this.popupForm?.get('automatableTestCases').value === -1 ? '' : this.popupForm?.get('automatableTestCases').value;
        this.reqObj['automatedRegressionTestCases'] = this.popupForm?.get('automatedRegressionTestCases').value === -1 ? '' : this.popupForm?.get('automatedRegressionTestCases').value ;
        this.reqObj['totalRegressionTestCases'] = this.popupForm?.get('totalRegressionTestCases').value === -1 ? '' : this.popupForm?.get('totalRegressionTestCases').value ;
        this.http_service.saveTestExecutionPercent(this.reqObj)
            .subscribe(response => {
                if (response.success) {
                    this.selectedFilterData = {};
                    this.setFormValuesEmpty();
                    this.testExecutionErrorMessage = '';
                    this.isTestExecutionSaveDisabled = true;

                    this.messageService.add({ severity: 'success', summary: 'Test Execution Percentage saved.', detail: '' });
                    this.getFilterDataOnLoad();
                } else if (!response.success && !!response.message && response.message === 'Unauthorized') {
                    this.messageService.add({ severity: 'error', summary: 'You are not authorized.' });
                } else {
                    this.messageService.add({ severity: 'error', summary: 'Error in saving test execution percentage. Please try after some time.' });
                }
                this.showPopuup = false;
                this.isTestExecutionSaveDisabled = true;
                this.testExecutionErrorMessage = '';
            });
    }

    enableDisableTestExecutionSubmitButton() {
        this.testExecutionErrorMessage = '';
        if (!this.isAddtionalTestField) {
            this.validateFirstGroupTextCountField();
        } else {
            if ((!!this.popupForm?.get('totalTestCases').value) || (!!this.popupForm?.get('executedTestCase').value) || (!!this.popupForm?.get('passedTestCase').value)) {
                this.validateFirstGroupTextCountField();
            }
            if(this.testExecutionErrorMessage === ''){
            this.validateSecondGroupTextCountField();
            }
        }
    }

    validateFirstGroupTextCountField(){
        if (!(!!this.popupForm?.get('totalTestCases').value)) {
            this.isTestExecutionSaveDisabled = true;
            if (parseInt(this.popupForm?.get('totalTestCases').value) === 0) {
                this.testExecutionErrorMessage = 'Total Test Cases should not be 0';
            } else {
                this.testExecutionErrorMessage = 'Please enter total test cases, executed test cases and passed test cases';
            }
            return;

        }
        if (!(!!this.popupForm?.get('executedTestCase').value)) {
            this.isTestExecutionSaveDisabled = true;
            if (parseInt(this.popupForm?.get('executedTestCase').value) === 0) {
                this.testExecutionErrorMessage = 'Executed Test Cases should not be 0';
            } else {
                this.testExecutionErrorMessage = 'Please enter total test cases, executed test cases and passed test cases';
            }
            return;
        }
        if (!(!!this.popupForm?.get('passedTestCase').value)) {
            this.isTestExecutionSaveDisabled = true;
            if (parseInt(this.popupForm?.get('passedTestCase').value) === 0) {
                this.testExecutionErrorMessage = 'Passed Test Cases should not be 0';
            } else {
                this.testExecutionErrorMessage = 'Please enter total test cases, executed test cases and passed test cases';
            }
            return;
        }
        if (parseFloat(this.popupForm?.get('totalTestCases').value) < parseFloat(this.popupForm?.get('executedTestCase').value)) {
            this.isTestExecutionSaveDisabled = true;
            this.testExecutionErrorMessage = 'Executed Test Cases should not be greater than Total Test Cases';
            return;
        }
        if (parseFloat(this.popupForm?.get('executedTestCase').value) < parseFloat(this.popupForm?.get('passedTestCase').value)) {
            this.isTestExecutionSaveDisabled = true;
            this.testExecutionErrorMessage = 'Passed Test Cases should not be greater than Executed Test Cases';
            return;
        }
        this.isTestExecutionSaveDisabled = false;
        this.testExecutionErrorMessage = '';
    }

    validateSecondGroupTextCountField(){
        if((!!this.popupForm?.get('automatedTestCases').value) && !(!!this.popupForm?.get('automatableTestCases').value) ||
        !(!!this.popupForm?.get('automatedTestCases').value) && (!!this.popupForm?.get('automatableTestCases').value)){
            this.isTestExecutionSaveDisabled = true;
            this.testExecutionErrorMessage = 'Please fill Automated Test Case & Automatable Test Case both';
            return;
        }

        if((!!this.popupForm?.get('automatedRegressionTestCases').value) && !(!!this.popupForm?.get('totalRegressionTestCases').value) || 
        !(!!this.popupForm?.get('automatedRegressionTestCases').value) && (!!this.popupForm?.get('totalRegressionTestCases').value)){
            this.isTestExecutionSaveDisabled = true;
            this.testExecutionErrorMessage = 'Please fill Automated Regrassion & Total Regrassion both';
            return;
        }

        if (parseFloat(this.popupForm?.get('automatableTestCases').value) < parseFloat(this.popupForm?.get('automatedTestCases').value)) {
            this.isTestExecutionSaveDisabled = true;
            this.testExecutionErrorMessage = 'Automated should not be greater than Automatable Test Cases';
            return;
        }
        if (parseFloat(this.popupForm?.get('totalRegressionTestCases').value) < parseFloat(this.popupForm?.get('automatedRegressionTestCases').value)) {
            this.isTestExecutionSaveDisabled = true;
            this.testExecutionErrorMessage = 'Automated Regression should not be greater than Total Regression Test Case';
            return;
        }
        this.isTestExecutionSaveDisabled = false;
        this.testExecutionErrorMessage = '';
    }


    enterNumericValue(event) {
        if (!!event && !!event.preventDefault && event.key === '.' || event.key === 'e' || event.key === '-' || event.key === '+') {
            event.preventDefault();
            return;
        }
        this.enableDisableSubmitButton();
    }

    numericInputUpDown(event: any) {
        if (parseInt(event.target.value) < 0) {
            setTimeout(() => {
                this[event.target.name] = '';
                event.target.value = '';
                this.enableDisableSubmitButton();
            }, 0);
        } else {
            this.enableDisableSubmitButton();
        }
    }

    enableDisableSubmitButton() {
        if (this.selectedView === 'upload_tep') {
            this.enableDisableTestExecutionSubmitButton();
        }
    }

    // called when user switches between Default and Additional filters
    selectFilterType(type, event) {
        this.filterType = type;
        $('.ui-menuitem-link.ui-corner-all').removeClass('selected');
        $(event.originalEvent.target).closest('a').addClass('selected');
    }

    checkDefaultFilterSelection(flag) {
        if (flag) {
            this.trendLineValueList = [...this.projectListArr];
            this.filterForm?.get('selectedProjectValue').setValue(this.trendLineValueList?.[0]['nodeId']);
            this.handleIterationFilters('project');
        } else {
            this.getProjectBasedData();
        }
    }

    getSprintsBasedOnState(state, sprints) {
        return sprints?.filter(x => x['sprintState']?.toLowerCase() === state);
    }

    getFirstOrLatestSprint(sprints, type) {
        if (type === 'latest') {
            return sprints?.reduce((a, b) => new Date(a.sprintStartDate) > new Date(b.sprintStartDate) ? a : b);
        } else {
            return sprints?.reduce((a, b) => new Date(a.sprintStartDate) < new Date(b.sprintStartDate) ? a : b);
        };
    }

    handleIterationFilters(level) {
        if (this.filterForm?.get('selectedProjectValue')?.value != '') {
            this.isToggleEnableForSelectedProject = false;
            this.tableLoader = true;
            this.noData = false;
            this.selectedSprintDetails = {};
            this.testExecutionScrumData = [];
            this.testExecutionKanbanData = [];
            this.capacityScrumData = [];
            this.capacityKanbanData = [];
            if (level?.toLowerCase() == 'project') {
                const selectedProject = this.filterForm?.get('selectedProjectValue')?.value;
                this.projectDetails = { ...this.trendLineValueList.find(i => i.nodeId === selectedProject) };
                this.selectedProjectBaseConfigId = this.projectDetails?.basicProjectConfigId;
                this.getProjectBasedData();
            }
        }
    }

    getProjectBasedData() {
        if (this.selectedProjectBaseConfigId) {
            if (this.selectedView === 'upload_tep') {
                this.getTestExecutionData(this.selectedProjectBaseConfigId)
            }
        }
    }
    sortAlphabetically(objArray) {
        objArray?.sort((a, b) => a.nodeName.localeCompare(b.nodeName));
        return objArray;
    }
    makeUniqueArrayList(arr) {
        let uniqueArray = [];
        for (let i = 0; i < arr?.length; i++) {
            const idx = uniqueArray?.findIndex(x => x.nodeId == arr[i]?.nodeId);
            if (idx == -1) {
                uniqueArray = [...uniqueArray, arr[i]];
                uniqueArray[uniqueArray?.length - 1]['path'] = [uniqueArray[uniqueArray?.length - 1]['path']];
                uniqueArray[uniqueArray?.length - 1]['parentId'] = [uniqueArray[uniqueArray?.length - 1]['parentId']];
            } else {
                uniqueArray[idx].path = [...uniqueArray[idx]?.path, arr[i]?.path];
                uniqueArray[idx].parentId = [...uniqueArray[idx]?.parentId, arr[i]?.parentId];
            }

        }
        return uniqueArray;
    }

    validateInput($event) {
        if ($event.key === 'e' || $event.key === '-') {
            $event.preventDefault();
        }
    }

    getTestExecutionData(projectId) {
        this.isAddtionalTestField = false
        this.http_service.getTestExecutionData(projectId).subscribe((response) => {
            if (response && response?.success && response?.data) {
                if (this.kanban) {
                    this.testExecutionKanbanData = response?.data;
                    this.isAddtionalTestField = this.testExecutionKanbanData[0]['uploadEnable'];
                    if(!this.isAddtionalTestField){
                        this.cols.testExecutionKanbanKeys = this.cols.testExecutionKanbanKeys.filter(col=>!this.addtionalTestFieldColumn.some(obj => obj.field === col.field))
                    }else{
                        for (const additionalColumn of this.addtionalTestFieldColumn) {
                            const fieldAlreadyExists = this.cols.testExecutionKanbanKeys.some(column => column.field === additionalColumn.field);
                            
                            if (!fieldAlreadyExists) {
                                this.cols.testExecutionKanbanKeys.push(additionalColumn);
                            }
                        }
                    }
                    if (this.testExecutionKanbanData?.length > 0) {
                        this.noData = false;
                    } else {
                        this.noData = true;
                    }
                } else {
                    this.testExecutionScrumData = response?.data;
                    this.isAddtionalTestField = this.testExecutionScrumData[0]['uploadEnable'];
                     if(!this.isAddtionalTestField){
                        this.cols.testExecutionScrumKeys = this.cols.testExecutionScrumKeys.filter(col=>!this.addtionalTestFieldColumn.some(obj => obj.field === col.field))
                    }else{
                        for (const additionalColumn of this.addtionalTestFieldColumn) {
                            const fieldAlreadyExists = this.cols.testExecutionScrumKeys.some(column => column.field === additionalColumn.field);
                            
                            if (!fieldAlreadyExists) {
                                this.cols.testExecutionScrumKeys.push(additionalColumn);
                            }
                        }
                    }
                    if (this.testExecutionScrumData?.length > 0) {
                        this.noData = false;
                    } else {
                        this.noData = true;
                    }
                }
                this.tableLoader = false;
            } else {
                this.tableLoader = false;
                this.noData = true;
            }
        });
    }
    /* Upload and  validate certificate */
    validateCertificate(event) {
        this.error = '';
        this.message = '';
        this.selectedFile = event.files[0];
        const allowedExtensions = ['.cer'];
        const fileExtension = this.selectedFile.name.substring(this.selectedFile.name.lastIndexOf('.')).toLowerCase();
        if (allowedExtensions.indexOf(fileExtension) === -1) {
            return;
        }
        const maxFileSize = 2 * 1024 * 1024; // 2 MB
        if (this.selectedFile.size > maxFileSize) {
            return;
        }
        if (this.selectedFile) {
            this.isUploadEnabled = false;
        }
    }
    uploadCertificate() {
        const file = this.selectedFile;
        this.error = '';
        this.message = '';
        this.http_service.uploadCertificate(file).pipe(first())
            .subscribe(
                data => {
                    if (data['status'] && data['status'] === 417) {
                        this.error = data['message'];
                    } else {
                        this.message = data['message'];
                    }
                },
                error => {
                    this.error = error.error.message;
                },
                () => this.clear(null)
            );
        this.isUploadEnabled = true;
    }

    clear(event) {
        this.selectedFile = null;
        this.error = event !== null ? '' : this.error;
        this.message = event !== null ? '' : this.message;
        this.isUploadEnabled = true;
    }

}
