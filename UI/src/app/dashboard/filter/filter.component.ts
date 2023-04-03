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

import { Component, OnInit, ElementRef, ViewChild,HostListener } from '@angular/core';
import { HttpService } from '../../services/http.service';
import { ExcelService } from '../../services/excel.service';
import { SharedService } from '../../services/shared.service';
import { HelperService } from '../../services/helper.service';
import { GoogleAnalyticsService } from '../../services/google-analytics.service';
import { GetAuthorizationService } from 'src/app/services/get-authorization.service';
import { Router } from '@angular/router';
import { UntypedFormControl, UntypedFormGroup } from '@angular/forms';
import { MessageService, MenuItem } from 'primeng/api';
import { faRotateRight } from '@fortawesome/fontawesome-free';
import { NgSelectComponent } from '@ng-select/ng-select';
import { NotificationResponseDTO } from 'src/app/model/NotificationDTO.model';
import { TextEncryptionService } from 'src/app/services/text.encryption.service';
import { first } from 'rxjs/operators';

@Component({
  selector: 'app-filter',
  templateUrl: './filter.component.html',
  styleUrls: ['./filter.component.css'],
})
export class FilterComponent implements OnInit {
  @ViewChild('selector') ngselect: NgSelectComponent;
  @ViewChild('toggleButton') toggleButton: ElementRef;
  @ViewChild('drpmenu') drpmenu: ElementRef;
  @ViewChild('dateToggleButton') dateToggleButton: ElementRef;
  @ViewChild('dateDrpmenu') dateDrpmenu: ElementRef;

  headerFixed = false;
  scrollOffset = 150;
  isSuperAdmin = false;
  id = '';
  name = '';
  masterData: any = {};
  filterData: any = [];
  shareDataObject: any = {};
  selectedFilterCount = 0;
  loader: any = {};
  getData: any = [];
  filterRequestData = {};
  filterkeys: any = [];
  selectedFilterData: any = {};
  selectedTab;
  downloadJson: any = {};
  disableDownloadBtn = false;
  subscriptions: any[] = [];
  filterKpiRequest: any = '';
  kanban = false;
  filterType = 'Default';
  currentSelectionLabel = '';
  maxDate: Date;
  enginneringMaturityErrorMessage = '';
  showIndicator = false;
  toggleDropdown = false;
  kpiListData: any = {};
  kpiList = [];
  showKpisList = [];
  kpiForm: UntypedFormGroup;
  activeSprintList: any = [];
  currentSelectedSprintId: any;
  noAccessMsg = false;
  filterForm: UntypedFormGroup;
  toggleFilterDropdown = false;
  selectedFilterArray: Array<any> = [];
  faRotateRight: faRotateRight;
  filterApplyData = {};
  colorObj = {};
  tempParentArray: Array<any> = [];
  selectedNodes = {};
  selectedNodeLevel = 0;
  hierarchyLevels = [];
  trendLineValueList: any = [];
  toggleDateDropdown = false;
  filteredSprints = [];
  showDropdown = {};
  selectedDateFilter = '';
  beginningDate;
  selectedProjectValueOnIteration: string;
  selectedProjectLastSyncDate: any;
  processorsTracelogs = [];
  processorName = 'jira';
  heirarchyCount: number;
  dateRangeFilter: any;
  selectedDayType = 'Weeks';
  selectedDays: any;
  previousType: boolean; // to check if Scrum/Kanban selection has changed
  takeFiltersFromPreviousTab: boolean; // to check if previous tab was following the same filter format
  additionalFiltersArr = [];
  additionalFiltersDdn = {};
  toggleDropdownObj = {};
  hierarchies = {};
  filteredAddFilters = {};
  initFlag = false;
  showChart = true;
  iterationConfigData = {};
  kpisNewOrder = [];
  isTooltip = false;
  projectIndex = 0;
  notificationList = [];
  items: MenuItem[];
  username: string;
  isGuest = false;
  logoImage: any;
  totalRequestCount = 0;
  selectedProjectData ={};

  constructor(
    private service: SharedService,
    private httpService: HttpService,
    private getAuthorizationService: GetAuthorizationService,
    public router: Router,
    private ga: GoogleAnalyticsService,
    private messageService: MessageService,
    private helperService: HelperService,
    private aesEncryption: TextEncryptionService,
  ) {

    this.selectedTab = this.service.getSelectedTab() || 'mydashboard';

    this.subscriptions.push(

      this.service.onTabRefresh.subscribe((selectedTab) => {
        this.selectedTab = selectedTab;
        if (this.selectedTab?.toLowerCase() === 'iteration') {
          this.service.setEmptyFilter();
        }
        this.projectIndex = 0;
        const type = this.service.getSelectedType();
        this.selectedType(type);
      }),
    );

     // added as scrum/kanban moved into nav component
    this.service.onTypeRefresh.subscribe(type=>{
      this.selectedType(type);
    });

    this.subscriptions.push(
      this.service.mapColorToProjectObs.subscribe((x) => {
        if (Object.keys(x).length > 0) {
          this.colorObj = x;
        }
      }),
    );

    this.httpService.getTooltipData().subscribe((filterData) => {
      if (filterData[0] !== 'error') {
        this.heirarchyCount = filterData?.hierarchySelectionCount;
        this.dateRangeFilter = filterData?.dateRangeFilter;
        this.filterForm?.get('date')?.setValue(this.dateRangeFilter?.counts?.[0]);
      }
    });

    this.getNotification();
     /*subscribe logo image from service*/
     this.service.getLogoImage().subscribe((logoImage) => {
      this.getLogoImage();
    });

  }

  ngOnInit() {
    this.service.setSelectedDateFilter(this.selectedDayType);
    this.filterForm = new UntypedFormGroup({
      selectedTrendValue: new UntypedFormControl(),
      date: new UntypedFormControl(''),
      selectedLevel: new UntypedFormControl(),
      selectedProjectValue: new UntypedFormControl(),
      selectedSprintValue: new UntypedFormControl(),
    });

    if (localStorage.getItem('authorities')) {
      this.getHierarchyLevels();
    }
    this.previousType = false;
    this.initFlag = true;
    if (this.getAuthorizationService.checkIfSuperUser()) {
      // logged in as SuperAdmin
      this.isSuperAdmin = true;
    }
    // setting max date user can select in calendar
    this.maxDate = new Date();
    this.getKpiOrderedList();
    this.resetFilterApplyObj();

    // getting document click event from dashboard and check if it is outside click of the filter and if filter is open then closing it
    this.service.getClickedItem().subscribe((target) => {
      if (target && target !== this.toggleButton?.nativeElement && target?.closest('.kpi-dropdown') !== this.drpmenu?.nativeElement) {
        this.toggleDropdown = false;
      }
      if (Object.keys(this.toggleDropdownObj)?.length > 0) {
        for (const key in this.toggleDropdownObj) {
          const btn = document.getElementById(key + 'Btn');
          const dropdown = document.getElementById(key + 'DDn');
          if (target && target !== btn && target?.closest('.add-filters-dropdown') !== dropdown) {
            this.toggleDropdownObj[key] = false;
          }
        }
      }
      if (target && target !== this.dateToggleButton?.nativeElement && target?.closest('.date-filter-dropdown') !== this.dateDrpmenu?.nativeElement) {
        this.toggleDateDropdown = false;
      }
    });

    const self = this;

    this.service.setShowTableView(this.showChart);
    this.service.iterationCongifData.subscribe((iterationDetails) => {
      this.iterationConfigData = iterationDetails;
    });

    this.service.kpiListNewOrder.subscribe((kpiListNewOrder) => {
      this.kpisNewOrder = kpiListNewOrder;
    });
    this.subscriptions.push(
      this.service.passEventToNav.subscribe(() => {
        this.getNotification();
      }),
    );

    this.username = localStorage.getItem('user_name');
    let authoritiesArr;
    if (localStorage.getItem('authorities')) {
      authoritiesArr = this.aesEncryption.convertText(localStorage.getItem('authorities'),'decrypt');
    }
    if (authoritiesArr && authoritiesArr.includes('ROLE_GUEST')) {
      this.isGuest = true;
    }
    this.items = [
      {
        label: 'Help',
        icon: 'fa fa-info-circle',
        command: () => {
          this.service.setSideNav(false);
          this.router.navigate(['/dashboard/Help']);
        },
      },
      {
        label: 'Logout',
        icon: 'fas fa-sign-out-alt',
        command: () => {
          this.logout();
        },
      },
    ];
    if (!this.isGuest) {
      this.items.unshift({
        label: 'Settings',
        icon: 'fa fa-cog',
        command: () => {
          this.service.setSideNav(false);
          this.router.navigate(['/dashboard/Config/']);
        },
      });
    }
  }

   // for making the header sticky on scroll
   @HostListener('window:scroll', [])
   onWindowScroll() {
     if (this.router.url.indexOf('/Config/') === -1 && this.router.url !== '/dashboard/Maturity' && this.router.url !== '/dashboard/EngineeringMaturity') {
       this.headerFixed = (window.pageYOffset || document.documentElement.scrollTop || document.body.scrollTop || 0) > this.scrollOffset;
     } else {
       this.headerFixed = false;
     }
   }

  /**create dynamic hierarchy levels for filter dropdown */
  getHierarchyLevels() {
    this.httpService.getAllHierarchyLevels().subscribe((res) => {
      if (res.data) {
        this.hierarchies = res.data;
        localStorage.setItem('completeHierarchyData',JSON.stringify(this.hierarchies));
        this.setLevels();
        this.getFilterDataOnLoad();
      }
    });
  }

  setLevels() {
    this.hierarchyLevels = [];
    this.additionalFiltersArr = [];
    const board = this.kanban ? 'kanban' : 'scrum';
    const projectLevel = this.hierarchies[board]?.filter((x) => x.hierarchyLevelId == 'project')[0]?.level;
    for (let i = 0; i < this.hierarchies[board]?.length; i++) {
      if (this.hierarchies[board][i]?.level <= projectLevel) {
        this.hierarchyLevels.push(this.hierarchies[board][i]);
      }
      if (this.hierarchies[board][i].level > projectLevel) {
        this.additionalFiltersArr.push(this.hierarchies[board][i]);
      }
    }
  }

  selectedType(type) {
    if (type === 'Kanban') {
      this.kanban = true;
    } else {
      this.kanban = false;
    }
    this.resetFilterApplyObj();
    this.selectedFilterArray = [];
    this.tempParentArray = [];

    if (this.kanban !== this.previousType) {
      this.filterForm?.reset();
      this.filterForm?.get('date')?.setValue(this.dateRangeFilter?.counts?.[0]);
    }
    this.setLevels();
    this.getFilterDataOnLoad();
    this.previousType = this.kanban;
    // this.service.setSelectedType(type); // Going in infinite loop

    const data = {
      url: this.router.url +'/' + (this.service.getSelectedType() ? this.service.getSelectedType() : 'Scrum'),
      userRole: this.getAuthorizationService.getRole(),
      version: this.httpService.currentVersion,
    };
    this.ga.setPageLoad(data);
    this.navigateToSelectedTab();
    this.getKpiOrderedList();
  }

  selectFilterType(type) {
    this.filterType = type;
  }

  makeUniqueArrayList(arr) {
    let uniqueArray = [];
    for (let i = 0; i < arr?.length; i++) {
      const idx = uniqueArray?.findIndex((x) => x.nodeId == arr[i]?.nodeId);
      if (idx == -1) {
        uniqueArray = [...uniqueArray, arr[i]];
        uniqueArray[uniqueArray?.length - 1]['path'] = [uniqueArray[uniqueArray?.length - 1]['path']];
        uniqueArray[uniqueArray?.length - 1]['parentId'] = [ uniqueArray[uniqueArray?.length - 1]['parentId']];
      } else {
        uniqueArray[idx].path = [...uniqueArray[idx]?.path, arr[i]?.path];
        uniqueArray[idx].parentId = [...uniqueArray[idx]?.parentId,arr[i]?.parentId];
      }
    }
    return uniqueArray;
  }

  getFilterDataOnLoad() {
    if (this.filterKpiRequest && this.filterKpiRequest !== '') {
      this.filterKpiRequest.unsubscribe();
    }
    this.selectedFilterData = {};
    this.selectedFilterCount = 0;
    this.selectedFilterData.kanban = this.kanban;
    this.selectedFilterData['sprintIncluded'] = this.selectedTab?.toLowerCase() == 'iteration' ? ['CLOSED', 'ACTIVE']: ['CLOSED'];
    const filterData = this.service.getFilterData();
    if ( !Object.keys(filterData).length ||this.previousType !== this.kanban ||this.selectedTab?.toLowerCase() == 'iteration' ||this.selectedTab?.toLowerCase() == 'backlog' ||this.initFlag) {
      this.filterKpiRequest = this.httpService.getFilterData(this.selectedFilterData).subscribe((filterApiData) => {
          this.processFilterData(filterApiData);
          this.initFlag = false;
        });
    } else {
      this.processFilterData(filterData);
    }
  }

  processFilterData(filterData) {
    if (filterData[0] !== 'error') {
      this.filterData = filterData['data'];
      if (this.filterData.length == 0) {
        this.service.setNoProjects(true);
      }
      this.service.setFilterData(JSON.parse(JSON.stringify(filterData)));
      /** check if data for additional filters exists in filterData api, if yes create a formControl for the same */
      this.additionalFiltersDdn = {};
      for (let i = 0; i < this.additionalFiltersArr?.length; i++) {
        let arr = this.filterData.filter((x) => x.labelName.toLowerCase() == this.additionalFiltersArr[i]['hierarchyLevelId']?.toLowerCase());
        if (arr?.length > 0) {
          arr = this.sortAlphabetically(arr);
          arr = this.makeUniqueArrayList(arr);
          this.additionalFiltersDdn[this.additionalFiltersArr[i]['hierarchyLevelId']] = arr;
          this.toggleDropdownObj[this.additionalFiltersArr[i]['hierarchyLevelId']] = false;
          if (this.additionalFiltersArr[i]['hierarchyLevelId'] == 'sprint') {
            this.createFormGroup(this.additionalFiltersArr[i]['hierarchyLevelId'],arr);
          } else {
            this.createFormGroup(this.additionalFiltersArr[i]['hierarchyLevelId']);
          }
        }
      }

      if (!this.filterForm?.get('selectedTrendValue')?.value ||this.filterForm?.get('selectedTrendValue')?.value?.length == 0 ||
        (this.takeFiltersFromPreviousTab == false && this.selectedTab?.toLowerCase() !== 'iteration' && this.selectedTab?.toLowerCase() !== 'backlog' && this.selectedTab?.toLowerCase() !== 'maturity') ) {
        this.checkDefaultFilterSelection();
        if (this.selectedTab?.toLowerCase() !== 'iteration' && this.selectedTab?.toLowerCase() !== 'backlog' && this.selectedTab?.toLowerCase() !== 'maturity') {
          this.takeFiltersFromPreviousTab = true;
        }
      } else if (this.selectedTab?.toLowerCase() === 'iteration' || this.selectedTab?.toLowerCase() === 'backlog' || this.selectedTab?.toLowerCase() === 'maturity') {
        this.checkDefaultFilterSelection();
        this.takeFiltersFromPreviousTab = false;
      } else {
        this.takeFiltersFromPreviousTab = true;
      }

      if (this.kanban) {
        this.selectedDateFilter = `${this.filterForm?.get('date')?.value} ${this.selectedDayType}`;
      }

      if (Object.keys(this.filterData).length !== 0) {
        this.disableDownloadBtn = false;
        this.getMasterData();
        this.service.setEmptyData(false);
      } else {
        this.service.setEmptyFilter();
        this.disableDownloadBtn = true;
        this.service.setEmptyData(true);
      }
    } else {
      this.service.setEmptyData(true);
    }
  }

  createFormGroup(level, arr?) {
    if (arr?.length > 0) {
      const obj = {};
      for (let i = 0; i < arr?.length; i++) {
        obj[arr[i]['nodeId']] = new UntypedFormControl(false);
      }
      this.filterForm.controls[level] = new UntypedFormGroup(obj);
    } else {
      this.filterForm.controls[level] = new UntypedFormControl('');
    }
  }

  renderDownloadExcel(response) {
    if (!!response && response.success && !!response.data && response.data.length > 0 ) {
      // this.renderExcelData(response);
    } else if (!!response && response.success && !!response.data && response.data.length === 0) {
      this.enginneringMaturityErrorMessage = 'No Data Available';
    } else if (!!response && !response.success) {
      this.enginneringMaturityErrorMessage = 'No Access!';
    } else {
      this.enginneringMaturityErrorMessage = 'Some error occurred!';
    }
  }

  getMasterData() {
    const masterData = this.service.getMasterData();
    if (!Object.keys(masterData).length) {
      this.httpService.getMasterData().subscribe((masterApiData) => {
        if (masterApiData[0] !== 'error') {
          this.processMasterData(masterApiData);
        }
      });
    } else {
      this.processMasterData(masterData);
    }
  }

  processMasterData(masterData) {
    this.masterData = masterData;
    this.service.setMasterData(JSON.parse(JSON.stringify(masterData)));
    if (this.selectedTab?.toLowerCase() == 'iteration' ||this.selectedTab?.toLowerCase() == 'backlog') {
      this.projectIndex = 0;
      this.handleIterationFilters('project', 1);
    } else {
      this.applyChanges();
    }
  }

  /** get kpi ordered list ends */

  assignUserNameForKpiData() {
    if (!this.kpiListData['username']) {
      delete this.kpiListData['id'];
    }
    this.kpiListData['username'] = localStorage.getItem('user_name');
  }

  closeAllDropdowns() {
    for (const key in this.toggleDropdownObj) {
      this.toggleDropdownObj[key] = false;
    }
  }

  filterAdditionalFilters() {
    this.filteredAddFilters = {};
    const selectedLevel = this.filterForm.get('selectedLevel')?.value;
    if (selectedLevel == 'project') {
      const selectedProjects = this.filterForm?.get('selectedTrendValue')?.value;
      for (const key in this.additionalFiltersDdn) {
        this.filteredAddFilters[key] = [];
      }
      for (let i = 0; i < selectedProjects?.length; i++) {
        for (const key in this.additionalFiltersDdn) {
          if (key == 'sprint') {
            this.filteredAddFilters[key] = [...this.filteredAddFilters[key],...this.additionalFiltersDdn[key]?.filter(
              (x) => x['parentId']?.includes(selectedProjects[i]) && x['sprintState']?.toLowerCase() == 'closed')];
          } else {
            this.filteredAddFilters[key] = [...this.filteredAddFilters[key],...this.additionalFiltersDdn[key]?.filter(
              (x) => x['path'][0]?.includes(selectedProjects[i]))];
          }
        }
      }
    }
  }

  onSelectedTrendValueChange($event) {
  const selectedValue  = this.filterForm.get('selectedTrendValue').value;
    if(selectedValue && selectedValue.length > 0 && !this.kanban && this.filterForm?.get('selectedLevel').value.toLowerCase() === 'project' ){
      localStorage.setItem('filter',selectedValue[0]);
    }
    this.additionalFiltersArr.forEach((additionalFilter) => {
      this.filterForm.patchValue({[additionalFilter['hierarchyLevelId']]: null});
    });
    this.applyChanges();
  }

  // this method would be called on click of apply button of filter
  applyChanges(applySource?, filterApplied = true): void {
    let selectedLevelId = this.filterForm?.get('selectedLevel')?.value;
    let selectedTrendIds = this.filterForm?.get('selectedTrendValue')?.value;
    let selectedLevel = this.hierarchyLevels?.filter((x) => x.hierarchyLevelId == selectedLevelId)[0];
    if (selectedTrendIds?.length > 0) {
      let selectedTrendValues = [];
      for (let i = 0; i < selectedTrendIds?.length; i++) {
        selectedTrendValues.push(this.trendLineValueList?.filter((x) => x.nodeId == selectedTrendIds[i])[0]);
      }

      this.service.setSelectedLevel(selectedLevel);
      this.service.setSelectedTrends(selectedTrendValues);
      if (!applySource) {
        this.ngselect?.close();
        this.ngselect?.blur();
      } else {
        this.closeAllDropdowns();
      }
      /**push selected upper level hierarchy in selectedFilterArray */
      this.selectedFilterArray = [];
      for (let i = 0;i < this.filterForm?.get('selectedTrendValue')?.value?.length;i++) {
        const selectedItem = {...this.trendLineValueList?.filter((x) =>x.nodeId == this.filterForm?.get('selectedTrendValue')?.value[i])[0]
       };
        selectedItem['additionalFilters'] = [];
        this.selectedFilterArray.push(selectedItem);
      }
      this.selectedFilterArray = this.sortAlphabetically(
        this.selectedFilterArray,
      );
      /** add additional filters like sprints, date etc in selectedFilterArray */
      const isAdditionalFilter = this.additionalFiltersArr?.filter(
        (x) => x['hierarchyLevelId'] == applySource || this.filterForm.get(x['hierarchyLevelId']),
      );
      if (isAdditionalFilter?.length > 0) {
        for (let i = 0;i < Object.keys(this.additionalFiltersDdn)?.length;i++ ) {
          const additionalFilterFormVal = this.filterForm?.get(Object.keys(this.additionalFiltersDdn)[i])?.value;
          if (additionalFilterFormVal) {
            if (
              typeof additionalFilterFormVal === 'object' && Object.keys(additionalFilterFormVal)?.length > 0) {
              const selectedAdditionalFilter = this.additionalFiltersDdn[Object.keys(this.additionalFiltersDdn)[i]]?.filter((x) => additionalFilterFormVal[x['nodeId']] == true);
              for (let j = 0; j < selectedAdditionalFilter?.length; j++) {
                const parentNodeIdx = this.selectedFilterArray?.findIndex((x) => x.nodeId == selectedAdditionalFilter[j]['parentId'][0]);
                if (parentNodeIdx >= 0) {
                  this.selectedFilterArray[parentNodeIdx]['additionalFilters'] =
                    [...this.selectedFilterArray[parentNodeIdx]['additionalFilters'],selectedAdditionalFilter[j]];
                }
              }
            } else {
              const selectedAdditionalFilter = this.additionalFiltersDdn[Object.keys(this.additionalFiltersDdn)[i]]?.filter((x) => x['nodeId'] == additionalFilterFormVal)[0];
              const parentNodeIdx = this.selectedFilterArray?.findIndex((x) => selectedAdditionalFilter['path'][0]?.includes(x.nodeId));
              if (parentNodeIdx >= 0) {
                this.selectedFilterArray[parentNodeIdx]['additionalFilters'] = [...this.selectedFilterArray[parentNodeIdx]['additionalFilters'],selectedAdditionalFilter];
              }
            }
          }
        }
      }

      if (!applySource) {
        // for (let i = 0; i < this.selectedFilterArray?.length; i++) {
        //     this.selectedFilterArray[i]['additionalFilters'] = [];
        // }
        // this.resetAdditionalFiltersToInitialValue();
        this.filterAdditionalFilters();
      }
      if (applySource?.toLowerCase() == 'date' && this.kanban) {
        this.selectedDateFilter = `${this.filterForm?.get('date')?.value} ${this.selectedDayType}`;
        this.service.setSelectedDateFilter(this.selectedDayType);
        this.toggleDateDropdown = false;
      }
      this.createFilterApplyData();
      this.setMarker();
      let isAdditionalFilters = false;
      for (const key in this.additionalFiltersDdn) {
        if (key != 'sprint' && this.filterForm.get(key)?.value) {
          isAdditionalFilters = true;
        }
      }
      this.service.select(this.masterData,this.filterData,this.filterApplyData,this.selectedTab,isAdditionalFilters,filterApplied);
      this.limitSelectedTrendValueListChars();
    }

  }

  limitSelectedTrendValueListChars() {
    const selectedTrendNodeValueList: NodeList = document.querySelectorAll('.trend-line-value .ng-value .ng-value-label');
    for (let i = 0; i < selectedTrendNodeValueList.length; i++) {
      if ((selectedTrendNodeValueList[i] as HTMLElement).innerText.length > 10) {
        (selectedTrendNodeValueList[i] as HTMLElement).innerText =
          (selectedTrendNodeValueList[i] as HTMLElement).innerText.slice(0,10) + '...';
      }
    }
  }

  resetAdditionalFiltersToInitialValue() {
    for (let i = 0; i < Object.keys(this.additionalFiltersDdn)?.length; i++) {
      this.filterForm.get(Object.keys(this.additionalFiltersDdn)[i]).reset();
    }
  }

  createFilterApplyData() {
    this.resetFilterApplyObj();
    let isAdditionalFilterFlag: boolean =
      this.selectedFilterArray?.filter((item) => item?.additionalFilters?.length > 0)?.length > 0? true : false;
    for (let i = 0; i < this.selectedFilterArray?.length; i++) {
      if (isAdditionalFilterFlag) {
        const temp = this.selectedFilterArray[i]?.additionalFilters;
        for (let j = 0; j < temp?.length; j++) {
          if (this.filterApplyData['level'] < temp[j].level) {
            this.filterApplyData['level'] = temp[j].level;
            this.filterApplyData['selectedMap'][temp[j].labelName]?.push(temp[j].nodeId);
            this.filterApplyData['ids'] = [];
            this.filterApplyData['ids'].push(temp[j].nodeId);
          } else if (this.filterApplyData['level'] == temp[j].level) {
            this.filterApplyData['selectedMap'][temp[j].labelName]?.push(temp[j].nodeId);
            this.filterApplyData['ids'].push(temp[j].nodeId);
          }
          if (temp[j].labelName != 'sprint' || this.filterApplyData['selectedMap']['sprint']?.length == 0) {
            this.filterApplyData['selectedMap']['project'].push(this.selectedFilterArray[i]?.nodeId);
          }
        }
      } else {
        this.filterApplyData['level'] = this.selectedFilterArray[i]?.level;
        this.filterApplyData['selectedMap'][this.selectedFilterArray[i]?.labelName].push(this.selectedFilterArray[i]?.nodeId);
        this.filterApplyData['ids'].push(this.selectedFilterArray[i]?.nodeId);
      }
    }

    this.filterApplyData['sprintIncluded'] = this.selectedTab?.toLowerCase() == 'iteration'? ['CLOSED', 'ACTIVE']: ['CLOSED'];
    const dateFilter = this.filterForm?.get('date')?.value;
    if (dateFilter != '' && this.kanban) {
      this.filterApplyData['ids'] = [];
      this.filterApplyData['selectedMap']['date']?.push(this.selectedDayType.toUpperCase());
      this.filterApplyData['ids'].push(this.filterForm?.get('date')?.value);
      this.selectedDateFilter = `${this.filterForm?.get('date')?.value} ${this.selectedDayType}`;
    }
  }

  checkIfMaturityTabHidden(){
    const maturityBoard = this.kpiListData['others']?.find((board) =>
        board.boardName === 'Kpi Maturity');
    return (maturityBoard && maturityBoard.kpis[0].shown)  ? false : true;
  }

  navigateToSelectedTab() {
    if (this.selectedTab !== 'Config' && Object.keys(this.kpiListData)?.length > 0) {
      if (this.selectedTab === 'Maturity') {
        if (!this.checkIfMaturityTabHidden()) {
          this.router.navigateByUrl(
            `/dashboard/Maturity`,
          );
          return;
        } else {
          this.selectedTab = 'iteration';
          this.kanban = false;
        }
      }
      let boardDetails = this.kpiListData[this.kanban ? 'kanban' : 'scrum']?.find((board) =>board.boardName.toLowerCase() === this.selectedTab.toLowerCase()) ||
        this.kpiListData['others']?.find((board) => board.boardName.toLowerCase() === this.selectedTab.toLowerCase());
      if (!boardDetails && this.kpiListData[this.kanban ? 'kanban' : 'scrum']?.length > 0) {
        boardDetails = this.kpiListData['scrum'].find(boardDetail => boardDetail.boardName.toLowerCase() === 'iteration');
      }
      this.selectedTab = boardDetails?.boardName;
      this.service.setSelectedTab(boardDetails?.boardName,boardDetails?.boardId);
        this.router.navigateByUrl(`/dashboard/${boardDetails?.boardName.split(' ').join('-').toLowerCase()}/${boardDetails?.boardId}`);
    }
  }

  /** get kpi ordered list starts */
  get kpiFormValue() {
    return this.kpiForm.controls;
  }

  getKpiOrderedList() {
    if (this.isEmptyObject(this.kpiListData)) {
      this.httpService.getShowHideKpi().subscribe(
        (response) => {
          if (response.success === true) {
            this.kpiListData = response.data;
            this.service.setDashConfigData(this.kpiListData);
            this.navigateToSelectedTab();
            this.service.changedMainDashboardValueSub.next(this.kpiListData?.scrum[0].boardName);
            this.processKpiList();
          }
        },
        (error) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Error in fetching roles. Please try after some time.',
          });
        },
      );
    } else {
      this.processKpiList();
    }
  }

  processKpiList() {
    if (!this.isEmptyObject(this.kpiListData)) {
      switch (this.selectedTab) {
        case 'Iteration':
          this.kpiList = this.kpiListData['scrum'].filter((item) => item.boardName.toLowerCase() == 'iteration')[0]?.kpis.filter((kpi) => kpi.kpiId !== 'kpi121');
          break;
        case 'Backlog':
          this.kpiList = this.kpiListData['others'].filter((item) => item.boardName.toLowerCase() == 'backlog')?.[0]?.kpis;
          break;
        default:
          this.kpiList = this.kpiListData[this.kanban ? 'kanban' : 'scrum'].filter((item) => item.boardId === this.service.getSelectBoardId())[0]?.kpis;
      }
      const kpiObj = {};
      let count = 0;
      this.showKpisList = [];
      for (let i = 0; i < this.kpiList?.length; i++) {
        let showKpi = false;
        if (this.kpiList[i]['shown']) {
          if (this.kpiList[i]['isEnabled']) {
            showKpi = true;
          } else {
            showKpi = false;
          }
          if (!showKpi) {
            count++;
          }
          kpiObj[this.kpiList[i]['kpiId']] = new UntypedFormControl(showKpi);
          this.showKpisList.push(this.kpiList[i]);
        }
      }
      if (this.showKpisList && this.showKpisList?.length > 0) {
        this.noAccessMsg = false;
        this.kpiForm = new UntypedFormGroup({
          enableAllKpis: new UntypedFormControl(count > 0 ? false : true),
          kpis: new UntypedFormGroup(kpiObj),
        });
      } else {
        this.noAccessMsg = true;
      }
    }
  }
  handleAllKpiChange(event) {
    const kpiObj = {};
    for (let i = 0; i < this.showKpisList.length; i++) {
      kpiObj[this.showKpisList[i]['kpiId']] = event.checked;
    }
    this.kpiFormValue['kpis'].setValue(kpiObj);
  }
  handleKpiChange(event) {
    if (!event.checked) {
      this.kpiFormValue['enableAllKpis'].setValue(false);
    } else {
      let checkIfAllKpiEnabled = true;
      for (const kpi in this.kpiFormValue?.kpis['controls']) {
        if (!this.kpiFormValue.kpis['controls'][kpi]['value']) {
          checkIfAllKpiEnabled = false;
          break;
        }
      }
      this.kpiFormValue['enableAllKpis'].setValue(checkIfAllKpiEnabled);
    }
  }
  submitKpiConfigChange() {
    for (let i = 0; i < this.kpiList.length; i++) {
      this.kpiList[i]['isEnabled'] =
        this.kpiFormValue['kpis'].value[this.kpiList[i]['kpiId']];
    }
    const kpiArray = this.kpiListData[this.kanban ? 'kanban' : 'scrum'];
    for (let i = 0; i < kpiArray.length; i++) {
      if (kpiArray[i].boardName.toLowerCase() == this.selectedTab.toLowerCase()) {
        if (this.selectedTab.toLowerCase() === 'iteration') {
          this.kpiListData[this.kanban ? 'kanban' : 'scrum'][i]['kpis'] = [this.kpiListData[this.kanban ? 'kanban' 
          : 'scrum'][i]['kpis'].find((kpi) => kpi.kpiId === 'kpi121'),...this.kpiList];
        } else {
          this.kpiListData[this.kanban ? 'kanban' : 'scrum'][i]['kpis'] = this.kpiList;
        }
      }
    }
    this.assignUserNameForKpiData();
    this.httpService.submitShowHideKpiData(this.kpiListData).subscribe(
      (response) => {
        if (response.success === true) {
          this.messageService.add({
            severity: 'success',
            summary: 'Successfully Saved',
            detail: '',
          });
          this.service.setDashConfigData(this.kpiListData);
          this.toggleDropdown = false;
        } else {
          this.messageService.add({
            severity: 'error',
            summary: 'Error in Saving Configuraion',
          });
        }
      },
      (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error in saving kpis. Please try after some time.',
        });
      },
    );
  }
  /** get kpi ordered list ends */

  sanitizeDate(date) {
    return (
      date.getFullYear() + '/' + (parseInt(date.getMonth()) + 1 < 10 ? '0' + (parseInt(date.getMonth()) + 1) : parseInt(date.getMonth()) + 1) + '/' +
      (parseInt(date.getDate()) < 10 ? '0' + date.getDate() : date.getDate())
    );
  }

  setKPIOrder() {
    const kpiArray = this.kpiListData[this.kanban ? 'kanban' : 'scrum'];
    for (const kpiBoard of kpiArray) {
      if (kpiBoard.boardName.toLowerCase() === this.selectedTab.toLowerCase()) {
        kpiBoard.kpis = this.kpisNewOrder;
      }
    }
    this.kpiList = this.kpisNewOrder.filter((kpi) => kpi.kpiId !== 'kpi121');
    this.httpService.submitShowHideKpiData(this.kpiListData).subscribe(
      (response) => {
        this.kpisNewOrder = [];
        if (response.success === true) {
          this.messageService.add({
            severity: 'success',
            summary: 'Successfully Saved',
            detail: '',
          });
          this.service.setDashConfigData(this.kpiListData);
        } else {
          this.messageService.add({
            severity: 'error',
            summary: 'Error in Saving Configuraion',
          });
        }
      },
      (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error in saving kpis. Please try after some time.',
        });
      },
    );
  }

  showTooltip(val) {
    this.isTooltip = val;
  }

  ngOnDestroy() {
    this.filterApplyData = [];
    this.service.setEmptyFilter();
    this.subscriptions.forEach((subscription) => subscription.unsubscribe());
  }

  isEmptyObject(value) {
    return Object.keys(value).length === 0 && value.constructor === Object;
  }
  handleSelect(event) {
    this.trendLineValueList = this.filterData?.filter((x) => x.labelName?.toLowerCase() == event?.toLowerCase());
    this.trendLineValueList = this.sortAlphabetically(this.trendLineValueList);
    this.trendLineValueList = this.makeUniqueArrayList(this.trendLineValueList);
    this.filterForm?.get('selectedTrendValue').setValue([]);
    // }
  }

  setMarker() {
    const colorObj = {};
    for (let i = 0; i < this.selectedFilterArray?.length; i++) {
      colorObj[this.selectedFilterArray[i].nodeId] = i == 0
          ? { nodeName: this.selectedFilterArray[i].nodeName, color: '#079FFF' } : i == 1
          ? { nodeName: this.selectedFilterArray[i].nodeName, color: '#cdba38' }
          : { nodeName: this.selectedFilterArray[i].nodeName, color: '#00E6C3'};
    }
    this.service.setColorObj(colorObj);
  }

  resetFilterApplyObj() {
    this.filterApplyData = {
      ids: [],
      sprintIncluded: this.selectedTab?.toLowerCase() != 'iteration' ? ['CLOSED'] : ['CLOSED', 'ACTIVE'],
      selectedMap: {},
      level: 0,
    };
    for (let i = 0; i < this.hierarchyLevels?.length; i++) {
      this.filterApplyData['selectedMap'][this.hierarchyLevels[i]?.hierarchyLevelId] = [];
    }
    for (let i = 0; i < this.additionalFiltersArr?.length; i++) {
      this.filterApplyData['selectedMap'][this.additionalFiltersArr[i]['hierarchyLevelId']] = [];
    }
    if (this.kanban) {
      this.filterApplyData['selectedMap']['date'] = [];
    }
  }

  sortAlphabetically(objArray) {
    objArray?.sort((a, b) => a.nodeName?.localeCompare(b.nodeName));
    return objArray;
  }

  getTrendLevelArray() {
    return this.filterForm?.controls['selectedLevel']?.value?.toLowerCase();
  }

  checkDefaultFilterSelection() {
    if (this.selectedTab?.toLowerCase() != 'iteration' && this.selectedTab?.toLowerCase() != 'backlog'
    ) {
      // for (let i = this.hierarchyLevels?.length - 1; i >= 0; i--) { 
        for (let i = 0; i < this.hierarchyLevels?.length; i++) {  
      const arrList = this.filterData?.filter((x) =>x.labelName?.toLowerCase() == this.hierarchyLevels[i]?.hierarchyLevelId?.toLowerCase());
        if (arrList?.length == 1) {
          this.filterForm?.get('selectedLevel')?.setValue(this.hierarchyLevels[i]?.hierarchyLevelId);
          this.trendLineValueList = [...arrList];
          break;
        } else {
          this.filterForm?.get('selectedLevel')?.setValue(this.hierarchyLevels[this.hierarchyLevels.length-1]?.hierarchyLevelId);
          this.trendLineValueList = [...arrList];
        }
      }
      if (this.trendLineValueList?.length == 0) {
        this.filterForm?.get('selectedLevel')?.setValue(this.hierarchyLevels[this.hierarchyLevels.length-1]?.hierarchyLevelId);
        const arrList = this.filterData?.filter((x) =>x.labelName?.toLowerCase() == this.hierarchyLevels[0]?.hierarchyLevelId?.toLowerCase());
        this.trendLineValueList = [...arrList];
      }

      if (this.trendLineValueList?.length > 0) {
        this.trendLineValueList = this.sortAlphabetically(this.trendLineValueList);
        this.trendLineValueList = this.makeUniqueArrayList(this.trendLineValueList);
        this.filterForm?.get('selectedTrendValue').setValue([this.trendLineValueList[0]['nodeId']]);
      } else {
        this.filterForm?.get('selectedTrendValue').setValue([]);
      }
      if(!this.kanban && this.filterForm?.get('selectedLevel').value.toLowerCase() === 'project'){
        const selectedProject = localStorage.getItem('filter');
        this.filterForm?.get('selectedTrendValue')?.setValue([selectedProject]);
      }
    } else {
      this.filterForm?.get('selectedLevel').setValue('project');
      this.trendLineValueList = this.filterData?.filter((x) => x.labelName?.toLowerCase() == 'project');

      if (this.trendLineValueList?.length > 0) {
        this.trendLineValueList = this.sortAlphabetically(this.trendLineValueList);
        this.trendLineValueList = this.makeUniqueArrayList(this.trendLineValueList);
        this.filterForm?.get('selectedProjectValue').setValue(this.trendLineValueList[0]['nodeId']);
          localStorage.setItem('filter',this.filterForm?.get('selectedProjectValue').value);
          if(this.selectedTab?.toLowerCase() != 'backlog'){
            this.getProcessorsTraceLogsForProject(this.trendLineValueList[0]?.basicProjectConfigId)
          }
      } else {
        this.filterForm?.get('selectedProjectValue').setValue('');
        localStorage.setItem('filter','');
      }
    }
  }

  handleRemove() {
    this.ngselect.open();
  }

  handleClose() {
    this.ngselect.close();
  }

  isAddFilterDisabled(hierarchyLevelId) {
    const isProject = this.filterForm?.get('selectedLevel')?.value?.toLowerCase() == 'project';
    let isDisabled = false;
    let projectSelected = 0;

    if (hierarchyLevelId == 'sprint' && !this.kanban && isProject) {
      projectSelected = this.selectedFilterArray?.length;
    }
    if (hierarchyLevelId != 'sprint' && isProject) {
      projectSelected = this.selectedFilterArray?.filter((selectedFilter) => selectedFilter?.labelName === 'project')?.length;
    }

    if (hierarchyLevelId == 'sprint') {
      isDisabled = !isProject || !this.filteredAddFilters[hierarchyLevelId] || this.filteredAddFilters[hierarchyLevelId]?.length == 0 || (isProject && projectSelected == 0);
    } else {
      isDisabled = !isProject || (isProject && projectSelected !== 1) || !this.filteredAddFilters[hierarchyLevelId] || this.filteredAddFilters[hierarchyLevelId]?.length == 0;
    }
    return isDisabled;
  }

  /*'type' argument: to understand onload or onchange
    1: onload
    2: onchange */
  handleIterationFilters(level, type) {
    if (this.filterForm?.get('selectedProjectValue')?.value != '') {
      let selectedSprint = {};
      let activeSprints = [];
      let closedSprints = [];
      this.service.setNoSprints(false);
      if (level?.toLowerCase() == 'project') {
        const selectedProject = this.filterForm?.get('selectedProjectValue')?.value;
         localStorage.setItem('filter',selectedProject);
        this.filterForm?.get('selectedSprintValue')?.setValue('');
        this.selectedProjectData = this.trendLineValueList.find(x => x.nodeId === selectedProject);
        this.filteredAddFilters['sprint'] = [];
        if (this.additionalFiltersDdn && this.additionalFiltersDdn['sprint']) {
          this.filteredAddFilters['sprint'] = [...this.additionalFiltersDdn['sprint']?.filter((x) =>x['parentId']?.includes(selectedProject))];
        }

        activeSprints = [...this.filteredAddFilters['sprint']?.filter((x) => x['sprintState']?.toLowerCase() == 'active')];
        closedSprints = [...this.filteredAddFilters['sprint']?.filter((x) => x['sprintState']?.toLowerCase() == 'closed')];

        if (activeSprints?.length > 0) {
          selectedSprint = { ...activeSprints[0] };
        } else if (closedSprints?.length > 0) {
          selectedSprint = closedSprints[0];
          for (let i = 0; i < closedSprints?.length; i++) {
            const sprintEndDateTS1 = new Date(closedSprints[i]['sprintEndDate']).getTime();
            const sprintEndDateTS2 = new Date(selectedSprint['sprintEndDate']).getTime();
            if (sprintEndDateTS1 > sprintEndDateTS2) {
              selectedSprint = closedSprints[i];
            }
          }
        } else {
          this.selectedFilterArray = [];
          this.service.setNoSprints(true);
        }
        this.filterForm.get('selectedSprintValue').setValue(selectedSprint['nodeId']);
      }
      if (level?.toLowerCase() == 'sprint') {
        const val = this.filterForm.get('selectedSprintValue').value;
        selectedSprint = {...this.filteredAddFilters['sprint']?.filter((x) => x['nodeId'] == val)[0]};
      }
      if (selectedSprint && Object.keys(selectedSprint)?.length > 0) {
        this.selectedFilterArray = [];
        this.selectedFilterArray.push(selectedSprint);
        this.createFilterApplyData();
        this.getProcessorsTraceLogsForProject(this?.selectedProjectData['basicProjectConfigId']);
        this.service.select(this.masterData,this.filterData,this.filterApplyData,this.selectedTab);
      } else {
        if (type == 1) {
          if (this.projectIndex < this.trendLineValueList?.length) {
            this.filterForm?.get('selectedProjectValue')?.setValue(this.trendLineValueList[++this.projectIndex]?.nodeId);
              localStorage.setItem('filter',this.trendLineValueList[++this.projectIndex]?.nodeId)
            this.handleIterationFilters('project', 1);
          } else {
            this.projectIndex = 0;
            this.filterForm?.get('selectedProjectValue')?.setValue(this.trendLineValueList[this.projectIndex]?.nodeId);
            localStorage.setItem('filter',this.trendLineValueList[++this.projectIndex]?.nodeId);
          }
        }
      }
    }
  }

  getDate(type) {
    let dateString = 'N/A';
    const selectedSprint = this.filterForm?.get('selectedSprintValue')?.value;
    if (selectedSprint) {
      const obj = this.filteredAddFilters['sprint']?.filter((x) => x['nodeId'] == selectedSprint)[0];

      if (obj) {
        let d;
        if (type == 'start') {
          d = new Date(obj['sprintStartDate']);
        } else {
          d = new Date(obj['sprintEndDate']);
        }
        dateString = [this.pad(d.getDate()),this.pad(d.getMonth() + 1),d.getFullYear()].join('/');
      }
    }
    return dateString;
  }

  pad(s) {
    return s < 10 ? '0' + s : s;
  }

  startDateSelected(val) {
    this.beginningDate = new Date(val);
  }

  removeItem(hierarchyLevelId, nodeId) {
    const hierarchy = this.filterForm.get(hierarchyLevelId);
    if (typeof hierarchy?.value === 'object' && Object.keys(hierarchy?.value)?.length > 0) {
      this.filterForm.get(hierarchyLevelId).get(nodeId).setValue(false);
    } else {
      this.filterForm.get(hierarchyLevelId).setValue('');
    }

    this.applyChanges(hierarchyLevelId);
  }

  removeNode(nodeId: string) {
    this.selectedFilterArray = this.selectedFilterArray.filter((node) => node.nodeId !== nodeId);
    const selectedNode = this.selectedFilterArray.map((node) => node.nodeId);
    this.filterForm.get('selectedTrendValue').setValue(selectedNode);
    this.applyChanges(null, false);
  }

  getProcessorsTraceLogsForProject(basicProjectConfigId) {
    this.httpService.getProcessorsTraceLogsForProject(basicProjectConfigId).subscribe((response) => {
        if (response.success) {
          if(this?.selectedProjectData['basicProjectConfigId'] === basicProjectConfigId){
            this.processorsTracelogs = response.data;
        }
          this.showExecutionDate();
        } else {
          this.messageService.add({
            severity: 'error',
            summary:
              "Error in fetching processor's execution date. Please try after some time.",
          });
        }
      });
  }
  findTraceLogForTool() {
    return this.processorsTracelogs.find((ptl) => ptl['processorName'].toLowerCase() == this.processorName);
  }

  showExecutionDate() {
    this.selectedProjectLastSyncDate = this.findTraceLogForTool();
  }
  setSelectedDateType(label: string) {
    this.selectedDayType = label;
  }

  checkIfBtnDisabled(hierarchyLevelId) {
    let isDisabled = true;
    if (hierarchyLevelId == 'sprint') {
      for (const item in this.filterForm?.get(hierarchyLevelId)?.value) {
        if (this.filterForm?.get(hierarchyLevelId)?.value[item]) {
          isDisabled = null;
        }
      }
    } else {
      isDisabled = !this.filterForm?.get(hierarchyLevelId)?.value ? true : null;
    }
    return isDisabled;
  }

  getLevelName(id) {
    const name = this.hierarchyLevels?.filter((x) => x.hierarchyLevelId == id)[0]?.hierarchyLevelName;
    return name;
  }

  showChartToggle(val) {
    this.showChart = val;
    this.service.setShowTableView(this.showChart);
  }

  exportToExcel($event = null) {
    this.disableDownloadBtn = true;
    this.service.setGlobalDownload(true);
  }

  getNotification() {
    this.totalRequestCount  = 0 ;
    this.httpService.getAccessRequestsNotifications().subscribe((response: NotificationResponseDTO) => {
        if (response && response.success) {
          if (response.data?.length) {
            this.notificationList = [...response.data].map((obj) => {
                this.totalRequestCount = this.totalRequestCount + obj.count;
              return {
                label: obj.type + ' : ' + obj.count,
                icon: '',
                command: () => {
                  this.routeForAccess(obj.type);
                },
              };
            });
          }
        } else {
          this.messageService.add({
            severity: 'error',
            summary: 'Error in fetching requests. Please try after some time.',
          });
        }
      });
  }

  // logout is clicked  and removing auth token , username
  logout() {
    this.httpService.logout().subscribe((getData) => {
      if (!(getData !== null && getData[0] === 'error')) {
        this.helperService.isKanban = false;
        localStorage.removeItem('auth_token');
        localStorage.removeItem('user_name');
        localStorage.removeItem('authorities');
        localStorage.removeItem('projectsAccess');
        if (localStorage.getItem('loginType') === 'AD') {
          localStorage.removeItem('SpeedyPassword');
        }
        // Set blank selectedProject after logged out state
        this.service.setSelectedProject(null);
        this.service.setSelectedTab(null,null); // doing null bcoz it was landing on previous tab 

        this.router.navigate(['./authentication/login']);
      }
    });
  }

  // when user would want to give access on project from notification list
  routeForAccess(type: string) {
    if (this.getAuthorizationService.checkIfSuperUser() || this.getAuthorizationService.checkIfProjectAdmin()) {
      switch (type) {
        case 'Project Access Request':
          this.service.setSideNav(false);
          this.router.navigate(['/dashboard/Config/Profile/GrantRequests']);
          break;
        case 'User Access Request':
          this.service.setSideNav(false);
          this.router.navigate(['/dashboard/Config/Profile/GrantNewUserAuthRequests']);
          break;
        default:
      }
    } else {
      this.router.navigate(['/dashboard/Config/Profile/RequestStatus']);
    }
  }

    /*Rendered the logo image */
    getLogoImage() {
      this.httpService.getUploadedImage().pipe(first()).subscribe((data) => {
          if (data['image']) {
            this.logoImage = 'data:image/png;base64,' + data['image'];
          } else {
            this.logoImage = undefined;
          }
        });
    }
   
  /** when user clicks on Back to dashboard or logo*/
   navigateToDashboard(){
    this.httpService.getShowHideKpi().subscribe(response =>{
      this.service.setDashConfigData(response.data);
      this.kpiListData = response.data;
      this.getNotification();
      this.processKpiList();
      this.navigateToSelectedTab();
    });

   }
}
