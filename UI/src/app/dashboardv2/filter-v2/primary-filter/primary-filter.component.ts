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
  filters: any[];
  selectedFilters: any;
  subscriptions: any[] = [];
  stateFilters: any[] = [];
  @Output() onPrimaryFilterChange = new EventEmitter();
  @ViewChild('multiSelect') multiSelect: MultiSelect;

  constructor(private service: SharedService, private helperService: HelperService) {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (this.filterData && Object.keys(this.filterData).length) {
      this.populateFilters();
      if (!this.compareObjects(changes['primaryFilterConfig']?.currentValue, changes['primaryFilterConfig']?.previousValue) && !changes['primaryFilterConfig']?.firstChange) {

        setTimeout(() => {

          this.selectedFilters = [this.filters[0]];

          this.helperService.setBackupOfFilterSelectionState({ 'primary_level': this.selectedFilters });
          this.onPrimaryFilterChange.emit([...this.selectedFilters]);
        }, 100);
      } else {
        this.selectedFilters = [];
        setTimeout(() => {

          if (this.filters.length) {
            this.selectedFilters = new Set();

            this.stateFilters = this.helperService.getBackupOfFilterSelectionState('primary_level');

            if (this.stateFilters?.length > 0) {
              this.stateFilters.map(stateFilter => {
                this.selectedFilters.add(stateFilter);
              })
            } else {
              this.selectedFilters.add(this.filters[0]);
            }
            this.selectedFilters = [...this.selectedFilters];

            this.selectedFilters = Array.from(
              this.selectedFilters.reduce((map, obj) => map.set(obj.nodeId, obj), new Map()).values()
            );

            this.helperService.setBackupOfFilterSelectionState({ 'primary_level': this.selectedFilters });
            this.onPrimaryFilterChange.emit(this.selectedFilters);

          }
        }, 100);
      }
    }
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
        this.filters = this.sortByField(this.filterData[this.selectedLevel], this.primaryFilterConfig['defaultLevel'].sortBy);
      } else {
        this.filters = this.helperService.sortAlphabetically(this.filterData[this.selectedLevel]);
      }
    } else if (this.selectedLevel && Object.keys(this.selectedLevel).length) {
      // check for iterations and releases
      if (this.primaryFilterConfig['defaultLevel'].sortBy) {
        this.filters = this.sortByField(this.filterData[this.selectedLevel.emittedLevel.toLowerCase()].filter((filter) => filter.parentId === this.selectedLevel.nodeId), this.primaryFilterConfig['defaultLevel'].sortBy);
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
    this.helperService.setBackupOfFilterSelectionState({ 'primary_level': this.selectedFilters })
    this.onPrimaryFilterChange.emit(this.selectedFilters);
    if (this.multiSelect?.overlayVisible) {
      this.multiSelect.close(event);
    }
  }

  sortByField(objArray, prop) {
    if (objArray?.[0]?.[prop]) {
      return objArray.sort((a, b) => {
        const propA = a[prop].toLowerCase();
        const propB = b[prop].toLowerCase();
        return propA.localeCompare(propB);
      });
    } else {
      return objArray;
    }
  }

  compareObjects(obj1, obj2) {
    return JSON.stringify(obj1) === JSON.stringify(obj2);
  }

}
