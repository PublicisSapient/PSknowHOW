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
  subscriptions: any[] = [];
  stateFilters: any[] = [];
  @Output() onPrimaryFilterChange = new EventEmitter();
  @ViewChild('multiSelect') multiSelect: MultiSelect;

  constructor(private service: SharedService, private helperService: HelperService) {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if ((!this.compareObjects(changes['primaryFilterConfig']?.currentValue, changes['primaryFilterConfig']?.previousValue) && !changes['primaryFilterConfig']?.firstChange) ||
      ((changes['selectedType'] && changes['selectedType']?.currentValue !== changes['selectedType'].previousValue && !changes['selectedType']?.firstChange) ||
        (changes['selectedLevel'] && changes['selectedLevel']?.currentValue !== changes['selectedLevel'].previousValue && !changes['selectedLevel']?.firstChange))) {
      this.applyDefaultFilters();
      return;
    }
    this.selectedFilters = [];
    this.populateFilters();
    setTimeout(() => {
      if (this.filters.length) {
        this.selectedFilters = new Set();

        this.stateFilters = this.helperService.getBackupOfFilterSelectionState('primary_level');

        if(this.stateFilters?.length <= 0) {
          this.applyDefaultFilters();
          return;
        }

        this.stateFilters.forEach(stateFilter => {
          this.selectedFilters.add(stateFilter);
        });

        this.selectedFilters = [...this.selectedFilters];

        this.selectedFilters = Array.from(
          this.selectedFilters.reduce((map, obj) => map.set(obj.nodeId, obj), new Map()).values()
        );
        this.selectedFilters = this.filterData[this.selectedLevel]?.filter((f) => this.selectedFilters.map((s) => s.nodeId).includes(f.nodeId));
        this.helperService.setBackupOfFilterSelectionState({ 'primary_level': this.selectedFilters });
        this.onPrimaryFilterChange.emit(this.selectedFilters);
        this.setProjectAndLevelBackupBasedOnSelectedLevel();
      }
    }, 100);
  }

  applyDefaultFilters() {
    this.populateFilters();

    setTimeout(() => {
      this.selectedFilters = [];
      this.selectedFilters.push({ ...this.filters[0] });
      this.helperService.setBackupOfFilterSelectionState({ 'primary_level': this.selectedFilters });
      this.applyPrimaryFilters({});
      this.setProjectAndLevelBackupBasedOnSelectedLevel();
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
        this.filters = this.helperService.sortByField(this.filterData[this.selectedLevel], [this.primaryFilterConfig['defaultLevel'].sortBy]);
      }
    } else if (this.selectedLevel && Object.keys(this.selectedLevel).length) {
      // check for iterations and releases
      if (this.primaryFilterConfig['defaultLevel'].sortBy) {
        this.filters = this.helperService.sortByField(this.filterData[this.selectedLevel.emittedLevel.toLowerCase()].filter((filter) => filter.parentId === this.selectedLevel.nodeId), [this.primaryFilterConfig['defaultLevel'].sortBy]);
      } else {
        this.filters = this.helperService.sortAlphabetically(this.filterData[this.selectedLevel.emittedLevel.toLowerCase()].filter((filter) => filter.parentId === this.selectedLevel.nodeId));
      }
    } else {
      this.selectedLevel = 'project';
      this.filters = this.helperService.sortAlphabetically(this.filterData[this.selectedLevel.toLowerCase()]);
    }
  }

  applyPrimaryFilters(event) {
    if (!Array.isArray(this.selectedFilters)) {
      this.selectedFilters = [this.selectedFilters];
    }
    this.helperService.setBackupOfFilterSelectionState({ 'primary_level': [...this.selectedFilters] })
    this.onPrimaryFilterChange.emit([...this.selectedFilters]);
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

}
