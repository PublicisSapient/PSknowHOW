import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, ViewChild } from '@angular/core';
import { MultiSelect } from 'primeng/multiselect';
import { HelperService } from 'src/app/services/helper.service';
import { SharedService } from 'src/app/services/shared.service';
import { TooltipModule } from 'primeng/tooltip';

@Component({
  selector: 'app-additional-filter',
  templateUrl: './additional-filter.component.html',
  styleUrls: ['./additional-filter.component.css']
})
export class AdditionalFilterComponent implements OnChanges {
  @Input() selectedLevel: any = '';
  @Input() selectedType: string = '';
  @Input() selectedTab: string = '';
  @Input() additionalFilterConfig = [];
  subscriptions: any[] = [];

  filterSet: any;
  filterData = [];
  appliedFilters = {};
  selectedFilters = [];
  selectedTrends = [];
  previousSelectedTrends = [];
  selectedAdditionalFilterLevel = [];
  @Output() onAdditionalFilterChange = new EventEmitter();
  @ViewChild('multiSelect') multiSelect: MultiSelect;
  stateFilters: any;

  constructor(public service: SharedService, public helperService: HelperService) {
  }

  ngOnInit() {
    this.subscriptions.push(this.service.populateAdditionalFilters.subscribe((data) => {
      if (data && Object.keys(data)?.length && data[Object.keys(data)[0]]?.length) {
        this.selectedFilters = [];
        this.selectedTrends = this.service.getSelectedTrends();

        if (!this.arrayCompare(this.selectedTrends.map(x => x.nodeId).sort(), this.previousSelectedTrends.map(x => x.nodeId).sort())) {
          this.filterData = [];
          this.previousSelectedTrends = [...this.selectedTrends];
          // project changed, reset addtnl. filters
          this.helperService.setBackupOfFilterSelectionState({ 'additional_level': null });
        }

        Object.keys(data).forEach((f, index) => {
          if (this.filterData[index]) {
            if (this.selectedTab === 'developer') {
              data[f].forEach(element => {
                if (!this.filterData[index].map(x => x.nodeId).includes(element.nodeId)) {
                  const correctLevelMapping = {
                    Sprint: 'sprint',
                    Squad: 'sqd'
                  }
                  if (this.filterData[index]?.length && correctLevelMapping[this.additionalFilterConfig[index]?.defaultLevel?.labelName] === this.filterData[index][0].labelName) {
                    this.filterData[index].push(element);
                  } else {
                    this.filterData[index] = data[f];
                  }
                }
              });

              this.filterData.forEach((filterSet, index) => {
                if (!data[Object.keys(data)[index]]) {
                  this.filterData.splice(index,1);
                }
              });

            } else {
              this.filterData[index] = data[f];
            }


          } else {
            this.filterData[index] = data[f];
          }
        });

        if (this.selectedTab !== 'developer') {
          this.filterData.forEach(filterGroup => {
            if (filterGroup) {
              filterGroup = this.helperService.sortByField(filterGroup, ['nodeName', 'parentId']);
            }
          });

          this.stateFilters = this.helperService.getBackupOfFilterSelectionState('additional_level');
          const correctLevelMapping = {
            Sprint: 'sprint',
            Squad: 'sqd'
          }
          if (this.stateFilters && Object.keys(this.stateFilters)) {
            Object.keys(this.stateFilters).forEach((key, index) => {
              let correctIndex = 0;
              this.additionalFilterConfig.forEach((config, index) => {
                if (correctLevelMapping[config.defaultLevel.labelName] === key) {
                  correctIndex = index;
                }
              });
              if (this.stateFilters[key].length) {
                this.selectedFilters[correctIndex] = this.stateFilters[key];
              }
            });

          }
        } else {
          this.applyDefaultFilter();
        }
      } else {
        this.filterData = [];
      }
    }));
  }


  arrayCompare(arr1, arr2) {
    if (arr1.length !== arr2.length) {
      return false;
    }

    for (let index = 0; index < arr1.length; index++) {
      if (arr1[index] !== arr2[index]) {
        return false;
      }
    }

    return true;
  }

  applyDefaultFilter() {
    let fakeEvent = {};

    this.filterData.forEach((filter, index) => {
      if (filter.map(f => f.nodeName).includes('Overall')) {
       
        fakeEvent['value'] = 'Overall';

        this.selectedFilters[index] = { nodeId: 'Overall', nodeName: 'Overall' };
      } else {
        if (this.filterData[0]?.length && this.filterData[0][0]?.nodeId) {
          fakeEvent['value'] = this.filterData[0][0].nodeId;
          this.selectedFilters = [this.filterData[0][0]];
        } else {
          fakeEvent['value'] = 'Overall';
          this.selectedFilters = ['Overall'];
        }
      }
    });

    Promise.resolve().then(() => {
      this.applyAdditionalFilter(fakeEvent, 0 + 1);
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['additionalFilterConfig'] && !this.compareObjects(changes['additionalFilterConfig'].previousValue, changes['additionalFilterConfig'].currentValue)) {
      this.filterSet = new Set();
      this.selectedFilters = [];
      this.helperService.setBackupOfFilterSelectionState({ 'additional_level': null });
    }
  }

  compareObjects(obj1, obj2) {
    return JSON.stringify(obj1) === JSON.stringify(obj2);
  }

  applyAdditionalFilter(e, index, multi = false, fromBackup = false) {
    const filterKey = this.filterData.length === 1 ? 'filter' : 'filter' + index;
    const isDeveloper = this.selectedTab.toLowerCase() === 'developer';

    if (!isDeveloper) {
      if (!fromBackup) {
        let obj = {};
        for (let i = 0; i <= index; i++) {
          if (e[i]) {
            this.selectedAdditionalFilterLevel[i] = e && e[i] && e[i][0] ? e[i][0]['labelName'] : this.selectedAdditionalFilterLevel[i];
            obj[this.selectedAdditionalFilterLevel[i]] = e[i] ? e[i] : this.stateFilters[Object.keys(this.stateFilters)[i]];
            this.onAdditionalFilterChange.emit({ [this.selectedAdditionalFilterLevel[i]]: e[i] });
          }
        }
        this.helperService.setBackupOfFilterSelectionState({ 'additional_level': obj });
      } else {
        this.onAdditionalFilterChange.emit(e);
        this.helperService.setBackupOfFilterSelectionState({ 'additional_level': e });
      }
    } else {
      this.appliedFilters[filterKey] = e && e.value ? [e.value] : [];

      const filterValue = this.appliedFilters[filterKey][0];
      const nodeId = {};
      nodeId['value'] = filterValue?.nodeId || filterValue;
      nodeId['index'] = index;

      this.service.applyAdditionalFilters(nodeId);
    }

    if (this.multiSelect?.overlayVisible) {
      if (!e.hasOwnProperty('preventDefault')) {
        e.preventDefault = () => { };
        e.stopPropagation = () => { };
      }
      this.multiSelect.close(e);
    }
  }

  moveSelectedOptionToTop(event, index) {
    if (this.selectedFilters.length > 0) {
      // Get the selected options based on a particular property
      const selected = this.filterData[index]?.filter(option =>
        this.selectedFilters[index]?.some(selected => selected?.nodeName === option?.nodeName) // Match by 'nodeName'
      );

      // Get the unselected options
      const unselected = this.filterData[index]?.filter(option =>
        !this.selectedFilters[index]?.some(selected => selected?.nodeName === option?.nodeName) // Match by 'id'
      );

      // Combine selected and unselected, with selected on top
      if (!selected) return;
      this.filterData[index] = [...selected, ...unselected];
    }
  }

  onSelectionChange(event: any, index) {
    if (event?.value.length > 0) {
      this.moveSelectedOptionToTop(event, index)
    }
  }

}
