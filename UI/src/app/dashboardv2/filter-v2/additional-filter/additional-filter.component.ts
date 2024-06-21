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
  @Output() onPrimaryFilterChange = new EventEmitter();
  @ViewChild('multiSelect') multiSelect: MultiSelect;

  constructor(private service: SharedService, private helperService: HelperService) {
    this.subscriptions.push(this.service.populateAdditionalFilters.subscribe((data) => {
      this.filterData = [];
      this.selectedFilters = [];
      this.selectedTrends = this.service.getSelectedTrends();
      Object.keys(data).forEach((f, index) => {
        // if (this.selectedTab.toLowerCase() === 'developer') {
        //   this.filterData.push(...data[f]);
        // } else {
          this.filterData[index] = data[f];
        // }
      });

      if (this.selectedTab !== 'developer') {
        this.filterData.forEach(filterGroup => {
          filterGroup = this.helperService.sortByField(filterGroup, ['parentId', 'nodeName']);
        });
      }

      // Apply the first/ Overall filter
      if (this.selectedTab.toLowerCase() === 'developer') {
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
        setTimeout(() => {
          this.applyAdditionalFilter(fakeEvent, 0 + 1);
        }, 100);
      }

    }));
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['selectedTab']) {
      this.filterData = [];
      this.filterSet = new Set();
      this.selectedFilters = [];
    }
  }

  applyAdditionalFilter(e, index, multi = false) {
    if (this.selectedTab.toLowerCase() === 'developer') {
      if (this.filterData.length === 1) {

        this.appliedFilters['filter'] = [];

        if (!multi) {
          this.appliedFilters['filter'].push(e.value);
        } else {
          this.appliedFilters['filter'] = [...e];
        }
        this.appliedFilters['filter'][0]?.nodeId ? this.service.applyAdditionalFilters(this.appliedFilters['filter'][0].nodeId) : this.service.applyAdditionalFilters(this.appliedFilters['filter'][0]);
      } else {
        if (!this.appliedFilters['filter' + index]) {
          this.appliedFilters['filter' + index] = [];
        }
        if (!multi) {
          this.appliedFilters['filter' + index].push(e.value);
        } else {
          this.appliedFilters['filter' + index] = [...e];
        }
        this.service.applyAdditionalFilters(this.appliedFilters['filter' + index][0]);
      }
    } else {
      this.onPrimaryFilterChange.emit(e[index - 1]);
    }
    if (this.multiSelect?.overlayVisible) {
      this.multiSelect.close(event);
    }
  }
}
