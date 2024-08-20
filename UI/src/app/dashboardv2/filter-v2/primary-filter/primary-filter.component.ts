import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges, ViewChild } from '@angular/core';
import { MultiSelect } from 'primeng/multiselect';
import { SharedService } from 'src/app/services/shared.service';
import { HelperService } from 'src/app/services/helper.service';

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

  constructor(private service: SharedService, public helperService: HelperService) {
    this.service.selectedTrendsEvent.subscribe(filters => {
      if (filters?.length && this.primaryFilterConfig['type'] !== 'singleSelect') {
        this.selectedFilters = filters;
      }
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if ((!this.compareObjects(changes['primaryFilterConfig']?.currentValue, changes['primaryFilterConfig']?.previousValue)) ||
      ((changes['selectedType'] && changes['selectedType']?.currentValue !== changes['selectedType'].previousValue && !changes['selectedType']?.firstChange) ||
        (changes['selectedLevel'] && changes['selectedLevel']?.currentValue !== changes['selectedLevel'].previousValue && !changes['selectedLevel']?.firstChange))) {
      // (changes['selectedTab'] && changes['selectedTab']?.currentValue !== changes['selectedTab'].previousValue && !changes['selectedTab']?.firstChange)) {
      this.applyDefaultFilters();
      return;
    }
    this.selectedFilters = [];
    this.populateFilters();
    if (this.filters?.length) {
      this.selectedFilters = new Set();

      this.stateFilters = this.helperService.getBackupOfFilterSelectionState();
      if (this.stateFilters && this.stateFilters['primary_level'] && this.stateFilters['primary_level']?.length > 0 && !this.stateFilters['additional_level']) {
        this.stateFilters['primary_level'].forEach(stateFilter => {
          this.selectedFilters.add(stateFilter);
        });

        this.selectedFilters = [...this.selectedFilters];

        this.selectedFilters = Array.from(
          this.selectedFilters.reduce((map, obj) => map.set(obj.nodeId, obj), new Map()).values()
        );
        // if (this.primaryFilterConfig['defaultLevel']['labelName'].toLowerCase() !== 'sprint' && this.primaryFilterConfig['defaultLevel']['labelName'].toLowerCase() !== 'release') {
        this.helperService.setBackupOfFilterSelectionState({ 'primary_level': this.selectedFilters });
        // }
        if (this.primaryFilterConfig['defaultLevel']['labelName'].toLowerCase() !== 'sprint') {
          this.onPrimaryFilterChange.emit(this.selectedFilters);
        } else {
          if (this.selectedFilters[0].sprintState?.toLowerCase() === 'active') {
            this.onPrimaryFilterChange.emit(this.selectedFilters);
          } else {
            this.service.setNoSprints(true);
            this.onPrimaryFilterChange.emit([]);
          }
        }
        this.setProjectAndLevelBackupBasedOnSelectedLevel();
      } else if (this.stateFilters && this.stateFilters['primary_level'] && this.stateFilters['primary_level']?.length > 0 && this.stateFilters['additional_level'] && Object.keys(this.stateFilters['additional_level'])?.length) {

        this.stateFilters['primary_level'].forEach(stateFilter => {
          this.selectedFilters.add(stateFilter);
        });

        this.selectedFilters = [...this.selectedFilters];

        this.selectedFilters = Array.from(
          this.selectedFilters.reduce((map, obj) => map.set(obj.nodeId, obj), new Map()).values()
        );
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
          }
        }
      } else {
        this.applyDefaultFilters();
      }
    }
  }

  applyDefaultFilters() {
    this.populateFilters();
    setTimeout(() => {
      this.stateFilters = this.helperService.getBackupOfFilterSelectionState();
      if ((this.stateFilters && this.stateFilters['primary_level'] && this.stateFilters['primary_level']?.length > 0 && this.stateFilters['primary_level'][0]?.labelName?.toLowerCase() === 'project' && this.primaryFilterConfig['defaultLevel']['labelName'].toLowerCase() === 'project') ||
        (this.stateFilters && this.stateFilters['primary_level'] && this.stateFilters['primary_level']?.length > 0 && (this.stateFilters['primary_level'][0]?.labelName?.toLowerCase() === 'sprint' || this.stateFilters['primary_level'][0]?.labelName?.toLowerCase() === 'release') && this.primaryFilterConfig['defaultLevel']['labelName'].toLowerCase() === 'project')) {
        this.selectedFilters = [];
        if (this.stateFilters['primary_level'][0]?.labelName.toLowerCase() === 'project') {
          this.selectedFilters.push({ ...this.filters.filter((project) => project.nodeId === this.stateFilters['primary_level'][0].nodeId)[0] });
        } else {
          this.selectedFilters.push({ ...this.filters.filter((project) => project.nodeId === this.stateFilters['primary_level'][0].parentId)[0] });
        }
        this.helperService.setBackupOfFilterSelectionState({ 'primary_level': this.selectedFilters });
      } else {
        this.selectedFilters = [];
        this.selectedFilters.push({ ...this.filters[0] });
        if (this.primaryFilterConfig['defaultLevel']['labelName'].toLowerCase() !== 'sprint' && this.primaryFilterConfig['defaultLevel']['labelName'].toLowerCase() !== 'release') {
          this.helperService.setBackupOfFilterSelectionState({ 'primary_level': this.selectedFilters });
        }
      }
      // if (!this.stateFilters['additional_level']) {
      this.applyPrimaryFilters({});
      this.setProjectAndLevelBackupBasedOnSelectedLevel();
      // }
    }, 100);
  }

  ngOnInit() {
    this.subscriptions.push(this.service.mapColorToProjectObs.subscribe((val) => {
      if (this.selectedFilters?.length && this.selectedFilters[0]) {
        this.selectedFilters = this.selectedFilters.filter((filter) => Object.keys(val).includes(filter.nodeId));
      }
    }));
  }

  populateFilters() {
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
      this.filters = this.helperService.sortAlphabetically(this.filterData[this.selectedLevel]);
    }
  }

  applyPrimaryFilters(event) {
    if (!Array.isArray(this.selectedFilters)) {
      this.selectedFilters = [this.selectedFilters];
    }
    // if (this.primaryFilterConfig['defaultLevel']['labelName'].toLowerCase() !== 'sprint' && this.primaryFilterConfig['defaultLevel']['labelName'].toLowerCase() !== 'release') {
    this.helperService.setBackupOfFilterSelectionState({ 'primary_level': [...this.selectedFilters] })
    // }
   
    if (this.primaryFilterConfig['defaultLevel']['labelName'].toLowerCase() !== 'sprint') {
      this.onPrimaryFilterChange.emit([...this.selectedFilters]);
    } else {
      if(this.selectedFilters[0].sprintState?.toLowerCase() === 'active') {
        this.onPrimaryFilterChange.emit([...this.selectedFilters]);
      } else {
        this.service.setNoSprints(true);
        this.onPrimaryFilterChange.emit([]);
      }
    }
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
