import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges, ViewChild } from '@angular/core';
import { MultiSelect } from 'primeng/multiselect';
import { SharedService } from 'src/app/services/shared.service';
import { HelperService } from 'src/app/services/helper.service';

@Component({
  selector: 'app-primary-filter',
  templateUrl: './primary-filter.component.html',
  styleUrls: ['./primary-filter.component.css']
})
export class PrimaryFilterComponent implements OnChanges {
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
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['selectedLevel'] && changes['selectedLevel']?.currentValue !== changes['selectedLevel'].previousValue && !changes['selectedLevel']?.firstChange) {
      this.applyDefaultFilters();
      return;
    } else if (changes['primaryFilterConfig'] && Object.keys(changes['primaryFilterConfig'].currentValue).length && !changes['primaryFilterConfig']?.firstChange) {
      this.applyDefaultFilters();
      return;
    }

    if (changes['selectedType'] && changes['selectedType']?.currentValue !== changes['selectedType'].previousValue && !changes['selectedType']?.firstChange) {
      this.applyFirstFilter();
      return;
    }
  }

  applyFirstFilter() {
    this.populateFilters();
    setTimeout(() => {
      this.selectedFilters = [];
      this.selectedFilters.push(this.filters[0]);
      this.applyPrimaryFilters({});
      this.setProjectAndLevelBackupBasedOnSelectedLevel();
    }, 100);
  }

  applyDefaultFilters() {
    this.populateFilters();
    setTimeout(() => {
      this.stateFilters = this.helperService.getBackupOfFilterSelectionState();
      // PROBLEM AREA START
      if (this.filters?.length) {
        if (this.stateFilters && Object.keys(this.stateFilters).length && this.stateFilters['primary_level']) {
          this.selectedFilters = [];
          if (this.stateFilters['primary_level'][0]?.labelName?.toLowerCase() === 'project') {
            if (this.primaryFilterConfig['defaultLevel']['labelName'].toLowerCase() === 'project') {
              this.selectedFilters.push({ ...this.filters?.filter((project) => project.nodeId === this.stateFilters['primary_level'][0].nodeId)[0] });
            } else {
              this.selectedFilters.push({ ...this.filters?.filter((project) => project.parentId === this.stateFilters['primary_level'][0].nodeId)[0] });
            }
          } else {
            if (this.filters[0].labelName === 'project') {
              this.selectedFilters.push({ ...this.filters?.filter((project) => project.nodeId === this.stateFilters['primary_level'][0].parentId)[0] });
            } else {
              this.selectedFilters.push({ ...this.filters[0] });
            }
          }
        } else {
          this.selectedFilters = [];
          this.selectedFilters.push(this.filters[0]);
        }
        // PROBLEM AREA END
        this.applyPrimaryFilters({});
        this.setProjectAndLevelBackupBasedOnSelectedLevel();
      } else {
        this.service.setNoSprints(true);
        this.onPrimaryFilterChange.emit([]);
      }
    }, 100);
  }

  populateFilters() {
    if (this.selectedLevel && typeof this.selectedLevel === 'string' && this.selectedLevel.length) {
      this.filters = this.helperService.sortAlphabetically(this.filterData[this.selectedLevel]);
      if (this.primaryFilterConfig && this.primaryFilterConfig['defaultLevel'] && this.primaryFilterConfig['defaultLevel']?.sortBy) {
        if (this.selectedTab.toLowerCase() === 'iteration') {
          this.filters = this.helperService.sortByField(this.filterData[this.selectedLevel].filter((filter) => filter.parentId === this.selectedLevel.nodeId), [this.primaryFilterConfig['defaultLevel'].sortBy, 'sprintStartDate']);
        } else {
          this.filters = this.helperService.sortByField(this.filterData[this.selectedLevel], [this.primaryFilterConfig['defaultLevel'].sortBy]);
        }
      }
    } else if (this.selectedLevel && Object.keys(this.selectedLevel).length) {
      let selectedLevel = this.selectedLevel.emittedLevel;
      selectedLevel = selectedLevel[0].toUpperCase() + selectedLevel.slice(1);
      // check for iterations and releases
      if (this.primaryFilterConfig && this.primaryFilterConfig['defaultLevel'] && this.primaryFilterConfig['defaultLevel']?.sortBy) {
        if (this.selectedTab.toLowerCase() === 'iteration') {
          this.filters = this.helperService.sortByField(this.filterData[selectedLevel].filter((filter) => filter.parentId === this.selectedLevel.nodeId), [this.primaryFilterConfig['defaultLevel'].sortBy, 'sprintStartDate']);
        } else {
          this.filters = this.helperService.sortByField(this.filterData[selectedLevel].filter((filter) => filter.parentId === this.selectedLevel.nodeId), [this.primaryFilterConfig['defaultLevel'].sortBy]);
        }
      } else {
        this.filters = this.helperService.sortAlphabetically(this.filterData[selectedLevel].filter((filter) => filter.parentId === this.selectedLevel.nodeId));
      }
    } else {
      this.selectedLevel = 'Project';
      this.filters = this.filterData !== null && this.helperService.sortAlphabetically(this.filterData[this.selectedLevel]);
    }
  }

  applyPrimaryFilters(event) {
    if (this.primaryFilterConfig && Object.keys(this.primaryFilterConfig).length) {
      if (!Array.isArray(this.selectedFilters)) {
        this.selectedFilters = [this.selectedFilters];
      }

      if (this.selectedFilters?.length && Object.keys(this.selectedFilters[0]).length) {
        this.service.setNoSprints(false);
        if (this.primaryFilterConfig['defaultLevel']['labelName'].toLowerCase() !== 'sprint' || (this.selectedFilters?.length && this.selectedFilters[0]?.sprintState?.toLowerCase() === 'active')) {
          let addtnlStateFilters = this.helperService.getBackupOfFilterSelectionState('additional_level');
          if(addtnlStateFilters) {
            let combinedEvent = {};
            combinedEvent['additional_level'] = addtnlStateFilters;
            combinedEvent['primary_level'] = [...this.selectedFilters];
            this.onPrimaryFilterChange.emit(combinedEvent);
          } else {
            this.onPrimaryFilterChange.emit([...this.selectedFilters]);
          }
          
        }

        if (this.selectedFilters && this.selectedFilters[0] && Object.keys(this.selectedFilters[0]).length) {
          this.helperService.setBackupOfFilterSelectionState({ 'primary_level': [...this.selectedFilters] })
        }

        this.setProjectAndLevelBackupBasedOnSelectedLevel();
      }

      if (this.multiSelect?.overlayVisible) {
        this.multiSelect.close(event);
      }
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
        this.filters = this.filters.filter(x => x.nodeId !== selectedItem.nodeId); // remove the item from list
        this.filters.unshift(selectedItem)// this will add selected item on the top
      });
    }
  }

}
