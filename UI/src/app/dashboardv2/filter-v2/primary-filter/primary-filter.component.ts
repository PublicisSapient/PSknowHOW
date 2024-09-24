import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges, ViewChild } from '@angular/core';
import { MultiSelect } from 'primeng/multiselect';
import { SharedService } from 'src/app/services/shared.service';
import { HelperService } from 'src/app/services/helper.service';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-primary-filter',
  templateUrl: './primary-filter.component.html',
  styleUrls: ['./primary-filter.component.css']
})
export class PrimaryFilterComponent implements OnChanges, OnInit {
  @Input() filterData = null;
  @Input() selectedLevel: any = '';
  @Input() primaryFilterConfig: {};
  @Input() selectedType: string = '';
  @Input() selectedTab: string = '';
  filters = [];
  selectedFilters: any;
  selectedAdditionalFilters: any;
  subscriptions: any[] = [];
  stateFilters: any = {};
  @Output() onPrimaryFilterChange = new EventEmitter();
  @ViewChild('multiSelect') multiSelect: MultiSelect;

  constructor(
    private service: SharedService,
    public helperService: HelperService,
    public router: Router,
    private route: ActivatedRoute) {
    this.service.selectedTrendsEvent.subscribe(filters => {
      if (filters?.length && this.primaryFilterConfig['type'] !== 'singleSelect') {
        this.selectedFilters = filters;
      }
    });

    console.log('url from -> ', decodeURIComponent(window.location.hash.substring(1)))
    // this.route.queryParams.subscribe(params => {
    //   console.log(params); // All query params as an object
    //   const isRelease = params.release;

    // });
  }

  ngOnChanges(changes: SimpleChanges): void {
    const primaryFilterChanged = !this.compareObjects(
      changes['primaryFilterConfig']?.currentValue,
      changes['primaryFilterConfig']?.previousValue
    );

    const selectedTypeChanged = changes['selectedType'] &&
      changes['selectedType'].currentValue !== changes['selectedType'].previousValue &&
      !changes['selectedType'].firstChange;

    const selectedLevelChanged = changes['selectedLevel'] &&
      changes['selectedLevel'].currentValue !== changes['selectedLevel'].previousValue &&
      !changes['selectedLevel'].firstChange;


    if (primaryFilterChanged || selectedTypeChanged || selectedLevelChanged) {
      // (changes['selectedTab'] && changes['selectedTab']?.currentValue !== changes['selectedTab'].previousValue && !changes['selectedTab']?.firstChange)) {
      console.log('applyDefaultFilters 60 ')
      this.applyDefaultFilters();
      return;
    }
    this.selectedFilters = [];
    this.populateFilters();
    if (this.filters?.length) {
      this.selectedFilters = new Set();
      console.log('primary filter onchange ', this.helperService.getBackupOfFilterSelectionState())
      this.stateFilters = this.helperService.getBackupOfFilterSelectionState();
      console.log(this.stateFilters);
      if (this.stateFilters && this.stateFilters['primary_level'] && this.stateFilters['primary_level']?.length > 0 && !this.stateFilters['additional_level']) {
        this.stateFilters['primary_level'].forEach(stateFilter => {
          this.selectedFilters.add(stateFilter);
        });

        this.selectedFilters = [...this.selectedFilters];
        console.log('selectedFilters after array conversion ', this.selectedFilters);
        this.selectedFilters = Array.from(
          this.selectedFilters.reduce((map, obj) => map.set(obj.nodeId, obj), new Map()).values()
        );
        console.log('68 -> ', this.selectedFilters);
        // if (this.primaryFilterConfig['defaultLevel']['labelName'].toLowerCase() !== 'sprint' && this.primaryFilterConfig['defaultLevel']['labelName'].toLowerCase() !== 'release') {
        this.helperService.setBackupOfFilterSelectionState({ 'primary_level': this.selectedFilters });
        // }
        if (this.selectedFilters?.length) {
          if (this.primaryFilterConfig['defaultLevel']['labelName'].toLowerCase() !== 'sprint') {
            this.onPrimaryFilterChange.emit(this.selectedFilters);
          } else {
            if (this.selectedFilters[0].sprintState?.toLowerCase() === 'active') {
              this.onPrimaryFilterChange.emit(this.selectedFilters);
            } else {
              this.service.setNoSprints(true);
              this.onPrimaryFilterChange.emit([]);
              if (this.filters.length) {
                console.log('81 -> ', this.selectedFilters);
              this.selectedFilters.push({ ...this.filters[0] });
                this.helperService.setBackupOfFilterSelectionState({ 'primary_level': [...this.selectedFilters] })
              } else {
                console.log('85 -> ', this.selectedLevel);
              this.helperService.setBackupOfFilterSelectionState({ 'primary_level': [...this.selectedLevel] })
              }
            }
          }
        } else {
          this.service.setNoSprints(true);
          this.onPrimaryFilterChange.emit([]);
        }
        this.setProjectAndLevelBackupBasedOnSelectedLevel();
      } else if (this.stateFilters && this.stateFilters['primary_level'] && this.stateFilters['primary_level']?.length > 0 && this.stateFilters['additional_level'] && Object.keys(this.stateFilters['additional_level'])?.length) {

        this.stateFilters['primary_level'].forEach(stateFilter => {
          this.selectedFilters.add(stateFilter);
        });

        this.selectedFilters = [...this.selectedFilters];
        console.log('if additional filter exist -> ', this.selectedFilters);
        this.selectedFilters = Array.from(
          this.selectedFilters.reduce((map, obj) => map.set(obj.nodeId, obj), new Map()).values()
        );
        console.log('if additional filter exist after reduce -> ', this.selectedFilters);
        this.selectedFilters = this.filterData[this.selectedLevel]?.filter((f) => this.selectedFilters.map((s) => s.nodeId).includes(f.nodeId));
        this.selectedAdditionalFilters = {};
        Object.keys(this.stateFilters['additional_level']).forEach(key => {

          this.selectedAdditionalFilters[key] = new Set();
          this.stateFilters['additional_level'][key].forEach(stateFilter => {
            this.selectedAdditionalFilters[key].add(stateFilter);
          });

          this.selectedAdditionalFilters[key] = [...this.selectedAdditionalFilters[key]];

          this.selectedAdditionalFilters[key] = Array.from(
            this.selectedAdditionalFilters[key].reduce((map, obj) => map.set(obj.nodeId, obj), new Map()).values()
          );
          // this.selectedAdditionalFilters[key] = this.filterData[this.selectedLevel]?.filter((f) => this.selectedAdditionalFilters[key].map((s) => s.nodeId).includes(f.nodeId));
        });


        let obj = {};
        obj['primary_level'] = this.selectedFilters;
        obj['additional_level'] = this.selectedAdditionalFilters;
        this.onPrimaryFilterChange.emit(obj);

        if (this.primaryFilterConfig['defaultLevel']['labelName'].toLowerCase() !== 'sprint') {
          this.onPrimaryFilterChange.emit(obj);
        } else {
          if (this.selectedFilters[0].sprintState?.toLowerCase() === 'active') {
            this.onPrimaryFilterChange.emit(obj);
          } else {
            this.service.setNoSprints(true);
            this.onPrimaryFilterChange.emit([]);
            if (this.filters.length) {
              console.log('134 -> ', this.selectedFilters);
              this.selectedFilters.push({ ...this.filters[0] });
              this.helperService.setBackupOfFilterSelectionState({ 'primary_level': [...this.selectedFilters] })
            } else {
              console.log('138 -> ', this.selectedLevel);
              this.helperService.setBackupOfFilterSelectionState({ 'primary_level': [...this.selectedLevel] })
            }
          }
        }
      } else {
        console.log('applyDefaultFilters 157')
        this.applyDefaultFilters();
      }
      console.log(this.selectedFilters)
      console.log(this.selectedLevel)
      if (this.selectedFilters instanceof Set) {
        this.selectedFilters = [...this.selectedFilters]
      }
      const selectedParentIds = this.selectedFilters?.map((filter) => {
        if (filter.basicProjectConfigId) {
          return filter.basicProjectConfigId
        } else {
          return filter.nodeName
        }
      });
      console.log(selectedParentIds)
      const selectedParentIdsStr = selectedParentIds?.join(',');
      this.service.setPrimaryFilterSelection(selectedParentIdsStr);
      if (typeof this.selectedLevel === 'string') {
        console.log('1', selectedParentIdsStr)
        this.router.navigate(['/dashboard/' + this.selectedTab], { queryParams: { [this.selectedLevel]: selectedParentIdsStr }, relativeTo: this.route, queryParamsHandling: 'merge' });
      } else {
        console.log('2', selectedParentIdsStr)
        if (this.selectedLevel.emittedLevel.toLowerCase() !== 'release') {
          this.router.navigate(['/dashboard/' + this.selectedTab], { queryParams: { 'sprint': this.selectedFilters[0].nodeName, 'sqd': null }, relativeTo: this.route, queryParamsHandling: 'merge' });
        }
      }
    }
  }

  applyDefaultFilters() {
    console.log('inside func applyDefaultFilters')
    this.populateFilters();
    // console.log('url from -> ', decodeURIComponent(window.location.hash.substring(1)))
    // this.route.queryParams.subscribe(params => {
    //   console.log(params);
    // });
    setTimeout(() => {
      console.log(this.stateFilters)
      const primaryLevel = this.stateFilters?.['primary_level'];
      const primaryLabel = primaryLevel?.[0]?.labelName?.toLowerCase();
      const defaultLabel = this.primaryFilterConfig?.['defaultLevel']?.['labelName']?.toLowerCase();

      this.stateFilters = this.helperService.getBackupOfFilterSelectionState();
      console.log(this.stateFilters)
      // const alreadySelectedFilterObj = this.alreadySelectedFilters(this.stateFilters);
      // console.log('stateFilters -> ', this.stateFilters)
      // console.log('alreadySelectedFilterObj -> ', alreadySelectedFilterObj)
      // this.helperService.setBackupOfFilterSelectionState(alreadySelectedFilterObj);

      if (primaryLevel?.length > 0 && defaultLabel === 'project' && (primaryLabel === 'project' || primaryLabel === 'sprint' || primaryLabel === 'release')) {
        console.log('inside if')
        this.selectedFilters = [];
        if (this.stateFilters['primary_level'][0]?.labelName.toLowerCase() === 'project') {
          this.selectedFilters.push({ ...this.filters?.filter((project) => project.nodeId === this.stateFilters['primary_level'][0].nodeId)[0] });
        } else {
          this.selectedFilters.push({ ...this.filters?.filter((project) => project.nodeId === this.stateFilters['primary_level'][0].parentId)[0] });
        }
        // alreadySelectedFilterObj.primary_level.forEach((filterObj) => {
        //   this.selectedFilters.push(filterObj);
        // })
        console.log(this.selectedFilters)
        this.helperService.setBackupOfFilterSelectionState({ 'primary_level': this.selectedFilters });
      } else {
        console.log('inside else')
        this.selectedFilters = [];
        console.log('this.selectedTab ', this.selectedTab)
        console.log('this.selectedLevel ', this.selectedLevel)
        // console.log('this.filters ', this.filters)
        console.log('stateFilter.primary_level -> ', this.stateFilters.primary_level)
        // this.router.navigate(['/dashboard/' + this.selectedTab], { queryParams: { [this.selectedLevel]: selectedParentIdsStr, 'sprint': selectedSprintIds, 'sqd': selectedSquadIds, 'release': null }, relativeTo: this.route, queryParamsHandling: 'merge' });
        const foundInFiltersArray = this.stateFilters?.primary_level?.filter(primaryLevelObj =>
          this.filters.some(filtersListObj => JSON.stringify(filtersListObj) === JSON.stringify(primaryLevelObj))
        );
        console.log('foundInFiltersArray -> ', foundInFiltersArray)
        if(foundInFiltersArray?.length) {
          foundInFiltersArray.forEach(filterObj => this.selectedFilters.push({ ...filterObj }));
        } else {
          this.selectedFilters.push({ ...this.filters[0] });
        }
        // this.selectedFilters.push({ ...this.filters[0] });
        // this.selectedFilters.push({ ...(this.stateFilters?.primary_level ? this.stateFilters?.primary_level : this.filters[0]) });
        // alreadySelectedFilterObj.primary_level.forEach((filterObj) => {
        //   this.selectedFilters.push(filterObj);
        // })
        console.log('selectedFilters -> ', this.selectedFilters)
        console.log('defaultLabel -> ', defaultLabel)
        if (defaultLabel !== 'sprint' && defaultLabel !== 'release') {
          this.helperService.setBackupOfFilterSelectionState({ 'primary_level': this.selectedFilters });
        }
      }
      // if (!this.stateFilters['additional_level']) {
      this.applyPrimaryFilters({});
      this.setProjectAndLevelBackupBasedOnSelectedLevel();
      // }
    }, 100);
  }

  alreadySelectedFilters(filterObj) {
    console.log('filterObj', filterObj)
    // console.log('filters', this.filters)
    // Check if `primary_level` has any object with `labelName` equal to "project"
    const isLabelnameProject = (filterObj?.primary_level) && filterObj?.primary_level.some(obj => obj.labelName === 'project');

    const newSelectedFilterObj = {
      'parent_level': filterObj.parent_level,
      'primary_level': filterObj.primary_level ? filterObj.primary_level : [{ ...this.filters[0] }],
    }

    if (isLabelnameProject) {
      newSelectedFilterObj['additional_level'] = filterObj.additional_level;
    }

    return newSelectedFilterObj;
  }

  ngOnInit() {
    console.log('275 ', this.helperService.getBackupOfFilterSelectionState())
    this.subscriptions.push(this.service.mapColorToProjectObs.subscribe((val) => {
      if (this.selectedFilters?.length && this.selectedFilters[0]) {
        this.selectedFilters = this.selectedFilters.filter((filter) => Object.keys(val).includes(filter.nodeId));
        console.log(this.selectedFilters)
      }
    }));
  }

  populateFilters() {
    // console.log('populateFilters ', this.filters)
    if (this.selectedLevel && typeof this.selectedLevel === 'string' && this.selectedLevel.length) {
      this.filters = this.helperService.sortAlphabetically(this.filterData[this.selectedLevel]);
      if (this.primaryFilterConfig['defaultLevel'].sortBy) {
        if (this.selectedTab.toLowerCase() === 'iteration') {
          this.filters = this.helperService.sortByField(this.filterData[this.selectedLevel].filter((filter) => filter.parentId === this.selectedLevel.nodeId), [this.primaryFilterConfig['defaultLevel'].sortBy, 'sprintStartDate']);
        } else {
          this.filters = this.helperService.sortByField(this.filterData[this.selectedLevel], [this.primaryFilterConfig['defaultLevel'].sortBy]);
        }
      }
    } else if (this.selectedLevel && Object.keys(this.selectedLevel).length) {
      // check for iterations and releases
      if (this.primaryFilterConfig['defaultLevel'].sortBy) {
        if (this.selectedTab.toLowerCase() === 'iteration') {
          this.filters = this.helperService.sortByField(this.filterData[this.selectedLevel.emittedLevel].filter((filter) => filter.parentId === this.selectedLevel.nodeId), [this.primaryFilterConfig['defaultLevel'].sortBy, 'sprintStartDate']);
        } else {
          this.filters = this.helperService.sortByField(this.filterData[this.selectedLevel.emittedLevel].filter((filter) => filter.parentId === this.selectedLevel.nodeId), [this.primaryFilterConfig['defaultLevel'].sortBy]);
        }
      } else {
        this.filters = this.helperService.sortAlphabetically(this.filterData[this.selectedLevel.emittedLevel].filter((filter) => filter.parentId === this.selectedLevel.nodeId));
      }
    } else {
      this.selectedLevel = 'Project';
      this.filters = this.filterData !== null && this.helperService.sortAlphabetically(this.filterData[this.selectedLevel]);
    }
  }

  applyPrimaryFilters(event) {
    console.log('310 ', this.selectedFilters)
    if (!Array.isArray(this.selectedFilters)) {
      this.selectedFilters = [this.selectedFilters];
    }
    console.log('314 ', this.selectedFilters)
    // if (this.primaryFilterConfig['defaultLevel']['labelName'].toLowerCase() !== 'sprint' && this.primaryFilterConfig['defaultLevel']['labelName'].toLowerCase() !== 'release') {
    this.helperService.setBackupOfFilterSelectionState({ 'primary_level': [...this.selectedFilters] })
    // }
    console.log('323 this.selectedLevel -> ', this.selectedLevel)
    console.log('324 this.selectedFilters -> ', this.selectedFilters)
    const selectedParentIds = this.selectedFilters.map((filter) => {
      if (filter.basicProjectConfigId) {
        return filter.basicProjectConfigId
      } else {
        return filter.nodeName
      }
    });
    const selectedParentIdsStr = selectedParentIds.join(',');
    console.log(selectedParentIdsStr)
    const isSelectedTabSprintSquadAllowed = this.selectedTab === 'developer' || this.selectedTab === 'release' || this.selectedTab === 'backlog' || this.selectedTab === 'dora' || this.selectedTab === 'kpi-maturity';
    console.log(isSelectedTabSprintSquadAllowed, this.stateFilters)
    const isSprint = !isSelectedTabSprintSquadAllowed && this.stateFilters?.additional_level?.sprint && this.stateFilters?.additional_level?.sprint.length ? true : false;
    const isSquad = !isSelectedTabSprintSquadAllowed && this.stateFilters?.additional_level?.sqd && this.stateFilters?.additional_level?.sqd.length ? true : false;
    const selectedSprintIds = isSprint ? this.stateFilters?.additional_level?.sprint.map((sprint) => sprint.nodeName).join(',') : null;
    const selectedSquadIds = isSquad ? this.stateFilters?.additional_level?.sqd.map((sprint) => sprint.nodeName).join(',') : null;
    if (this.primaryFilterConfig['defaultLevel']['labelName'].toLowerCase() !== 'sprint') {
      console.log('parent if')
      this.onPrimaryFilterChange.emit([...this.selectedFilters]);
      console.log(this.selectedLevel, this.selectedTab)
      if (typeof this.selectedLevel === 'string') {
        console.log('3 ', selectedSprintIds)
        this.router.navigate(['/dashboard/' + this.selectedTab], { queryParams: { [this.selectedLevel]: selectedParentIdsStr, 'sprint': selectedSprintIds, 'sqd': selectedSquadIds, 'release': null }, relativeTo: this.route, queryParamsHandling: 'merge' });
      } else {
        console.log('4 ', selectedParentIdsStr)
        this.router.navigate(['/dashboard/' + this.selectedTab], { queryParams: { 'release': selectedParentIdsStr, 'sprint': null, 'sqd': null }, relativeTo: this.route, queryParamsHandling: 'merge' });
      }
      // this.router.navigate(['/dashboard/' + this.selectedTab], { queryParams: { [this.selectedLevel]: selectedParentIdsStr, 'sprint': null, 'sqd': null }, relativeTo: this.route, queryParamsHandling: 'merge' });
    } else {
      console.log('345 -> ', this.selectedFilters)
      if (this.selectedFilters[0].sprintState?.toLowerCase() === 'active') {
        console.log('else > if')
        this.onPrimaryFilterChange.emit([...this.selectedFilters]);
        console.log('5')
        this.router.navigate(['/dashboard/' + this.selectedTab], { queryParams: { 'sprint': this.selectedFilters[0].nodeName, 'sqd': null }, relativeTo: this.route, queryParamsHandling: 'merge' });
      } else {
        this.service.setNoSprints(true);
        this.onPrimaryFilterChange.emit([]);
        console.log(this.filters)
        console.log(this.selectedLevel)
        console.log(this.selectedFilters)
        if (this.filters.length) {
          this.selectedFilters.push({ ...this.filters[0] });
          this.helperService.setBackupOfFilterSelectionState({ 'primary_level': [...this.selectedFilters] })
        } else {
          this.helperService.setBackupOfFilterSelectionState({ 'primary_level': [this.selectedLevel] })
        }
      }
    }
    console.log(this.selectedFilters)

    // update query param goes here ...
    this.setProjectAndLevelBackupBasedOnSelectedLevel();
    if (this.multiSelect?.overlayVisible) {
      this.multiSelect.close(event);
    }
  }

  compareObjects(obj1, obj2) {
    return JSON.stringify(obj1) === JSON.stringify(obj2);
  }

  setProjectAndLevelBackupBasedOnSelectedLevel() {
    if (typeof this.selectedLevel === 'string') {
      this.service.setSelectedTrends(this.selectedFilters);
      this.service.setSelectedLevel({ hierarchyLevelName: this.selectedLevel?.toLowerCase() })
    } else {
      this.service.setSelectedTrends(this.selectedLevel['fullNodeDetails'])
      this.service.setSelectedLevel({ hierarchyLevelName: this.selectedLevel['nodeType']?.toLowerCase() })
    }
  }

  moveSelectedOptionToTop(event) {
    if (event?.value) {
      event?.value.forEach(selectedItem => {
        this.filters = this.filters.filter(x => x.nodeName !== selectedItem.nodeName); // remove the item from list
        this.filters.unshift(selectedItem)// this will add selected item on the top
      });
    }
  }

}
