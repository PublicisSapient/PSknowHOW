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
  selectedAdditionalFilterLevel = [];
  @Output() onAdditionalFilterChange = new EventEmitter();
  @ViewChild('multiSelect') multiSelect: MultiSelect;
  stateFilters: any;

  constructor(public service: SharedService, public helperService: HelperService) {
  }

  ngOnInit() {
    this.subscriptions.push(this.service.populateAdditionalFilters.subscribe((data) => {
      if (data && Object.keys(data)?.length) {
        this.selectedFilters = [];
        // this.filterData = [];
        this.selectedTrends = this.service.getSelectedTrends();
        Object.keys(data).forEach((f, index) => {
          if (this.filterData[index]) {
            // this.filterData[index].concat(data[f]);
            // Array.prototype.push.apply(this.filterData[index],data[f]);
            data[f].forEach(element => {
              if (!this.filterData[index].map(x => x.nodeId).includes(element.nodeId)) {
                this.filterData[index].push(element);
              }
            });


          } else {
            this.filterData[index] = data[f];
          }
          // remove duplicates
          // this.filterData[index] = this.filterData[index].filter(function (item, pos, self) {
          //   return self.indexOf(item) == pos;
          // });
        });

        if (this.selectedTab !== 'developer') {
          this.filterData.forEach(filterGroup => {
            filterGroup = this.helperService.sortByField(filterGroup, ['nodeName', 'parentId']);
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

        }

        // Apply the first/ Overall filter
        if (this.selectedTab.toLowerCase() === 'developer') {
          this.applyDefaultFilter();
        }
      } else {
        this.filterData = [];
      }
    }));
  }

  applyDefaultFilter() {
    let fakeEvent = {};

    this.filterData.forEach((filter, index) => {
      if (filter.map(f => f.nodeName).includes('Overall')) {
        // filter.splice(this.filterData.map(f => f.nodeName).indexOf('Overall'), 1);
        // filter.unshift({ nodeId: 'Overall', nodeName: 'Overall' });
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
    if (changes['selectedTab']) {
      this.filterSet = new Set();
      this.selectedFilters = [];
    } 

    if(changes['additionalFilterConfig'] || changes['selectedLevel']) {
      this.filterData = [];
    }
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
      }
    } else {
      // this.appliedFilters[filterKey] = this.appliedFilters[filterKey] || [];
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
    if (event?.value) {
      event?.value.forEach(selectedItem => {
        this.filterData[index] = this.filterData[index].filter(x => x.nodeName !== selectedItem.nodeName); // remove the item from list
        this.filterData[index].unshift(selectedItem)// this will add selected item on the top 
      });
    }
  }
}
