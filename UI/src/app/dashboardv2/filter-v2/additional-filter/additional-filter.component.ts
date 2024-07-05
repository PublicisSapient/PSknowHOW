import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, ViewChild } from '@angular/core';
import { MultiSelect } from 'primeng/multiselect';
import { HelperService } from 'src/app/services/helper.service';
import { SharedService } from 'src/app/services/shared.service';

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
  @Output() onAdditionalFilterChange = new EventEmitter();
  @ViewChild('multiSelect') multiSelect: MultiSelect;
  stateFilters: any;

  constructor(public service: SharedService, public helperService: HelperService) {
  }

  ngOnInit() {
    this.subscriptions.push(this.service.populateAdditionalFilters.subscribe((data) => {
      this.selectedFilters = [];
      this.selectedTrends = this.service.getSelectedTrends();
      Object.keys(data).forEach((f, index) => {
        this.filterData[index] = data[f];
      });

      if (this.selectedTab !== 'developer') {
        this.filterData.forEach(filterGroup => {
          filterGroup = this.helperService.sortByField(filterGroup, ['nodeName', 'parentId']);
        });

        this.stateFilters = this.helperService.getBackupOfFilterSelectionState('additional_level');
        if (this.stateFilters && Object.keys(this.stateFilters)) {
          Object.keys(this.stateFilters['level']).forEach((key, index) => {
            this.selectedFilters[index] = this.stateFilters['level'][key];
            // setTimeout(() => {
            //   this.applyAdditionalFilter(this.selectedFilters[index], index + 1, true, true);
            // }, 200);
          });
        }

      }

      // Apply the first/ Overall filter
      if (this.selectedTab.toLowerCase() === 'developer') {
        this.applyDefaultFilter();
      }

    }));
  }

  applyDefaultFilter() {
    let fakeEvent = {};
    if (this.filterData.map(f => f.nodeName).includes('Overall')) {
      this.filterData.splice(this.filterData.map(f => f.nodeName).indexOf('Overall'), 1);
      this.filterData.unshift({ nodeId: 'Overall', nodeName: 'Overall' });
      fakeEvent['value'] = 'Overall';
      this.selectedFilters = ['Overall'];
    } else {
      if (this.filterData[0]?.length && this.filterData[0][0]?.nodeId) {
        fakeEvent['value'] = this.filterData[0][0].nodeId;
        this.selectedFilters = [this.filterData[0][0]];
      } else {
        fakeEvent['value'] = 'Overall';
        this.selectedFilters = ['Overall'];
      }
    }
    Promise.resolve().then(() => {
      this.applyAdditionalFilter(fakeEvent, 0 + 1);
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['selectedTab']) {
      this.filterSet = new Set();
      this.selectedFilters = [];
    }
  }

  applyAdditionalFilter(e, index, multi = false, fromBackup = false) {
    const filterKey = this.filterData.length === 1 ? 'filter' : 'filter' + index;
    const isDeveloper = this.selectedTab.toLowerCase() === 'developer';

    if (!isDeveloper) {
      if (!fromBackup) {
        let obj = {};
        for (let i = 0; i < index; i++) {
          let selectedAdditionalFilterLevel = e && e[i] && e[i][0] ? e[i][0]['labelName'] : '';
          obj['level'] = obj['level'] ? obj['level'] : {};
          obj['level'][selectedAdditionalFilterLevel] = e[i] ? e[i] : this.stateFilters['level'][Object.keys(this.stateFilters['level'])[i]];
          this.onAdditionalFilterChange.emit(e[i]);
        }
        this.helperService.setBackupOfFilterSelectionState({ 'additional_level': obj });
      } else {
        this.onAdditionalFilterChange.emit(e);
      }
    } else {
      this.appliedFilters[filterKey] = this.appliedFilters[filterKey] || [];
      this.appliedFilters[filterKey] = !multi ? [...this.appliedFilters[filterKey], e.value] : e && e.length ? [...e] : [];

      const filterValue = this.appliedFilters[filterKey][0];
      const nodeId = filterValue?.nodeId || filterValue;
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
}
