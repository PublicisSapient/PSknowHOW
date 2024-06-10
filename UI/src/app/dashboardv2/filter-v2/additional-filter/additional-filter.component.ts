import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
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
  filterData1 = new Set();
  filterData2 = new Set();
  filterSet: any;
  filterData = [];
  appliedFilters = {};
  selectedFilters: any;

  constructor(private service: SharedService, private helperService: HelperService) {
    this.subscriptions.push(this.service.populateAdditionalFilters.subscribe((data) => {
      this.filterData = [];
      this.filterSet = new Set();
      let primarySet1 = new Set();
      let primarySet2 = new Set();
      this.filterData1 = new Set();
      this.filterData2 = new Set();
      if (Object.keys(data).length) {
        data.filter1?.forEach(f => {
          primarySet1.add(f);
        });

        data.filter2?.forEach(f => {
          primarySet2.add(f);
        });

        primarySet1.forEach((f: any) => {
          f.forEach(element => {
            this.filterData1.add(element);
          });
        });

        primarySet2.forEach((f: any) => {
          f.forEach(element => {
            this.filterData2.add(element);
          });
        });
        this.filterSet.add(this.filterData1);
        this.filterSet.add(this.filterData2);

        this.filterSet = Array.from(this.filterSet);
        this.filterSet.forEach((f, index) => {
          f = Array.from(f);
          this.filterData[index] = f;
        });

        if (this.selectedTab.toLowerCase() === 'developer') {
          this.filterData.forEach((filterArray, index) => {
            let fakeEvent = {};
            if (filterArray.includes('Overall')) {
              filterArray.splice(filterArray.indexOf('Overall'), 1);
              filterArray.unshift('Overall');
              fakeEvent['value'] = 'Overall';
              this.selectedFilters = 'Overall';
            } else {
              fakeEvent['value'] = filterArray[0];
              this.selectedFilters = filterArray[0];
            }

            setTimeout(() => {
              this.applyAdditionalFilter(fakeEvent, index + 1);
            }, 0)
          });
        }
      }
    }));
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['selectedTab']) {
      this.filterData = [];
      this.filterSet = new Set();
    }
  }

  applyAdditionalFilter(e, index, multi = false) {
    if (this.selectedTab.toLowerCase() === 'developer') {
      if (!this.appliedFilters['filter' + index]) {
        this.appliedFilters['filter' + index] = [];
      }
      if (!multi) {
        this.appliedFilters['filter' + index].push(e.value);
      } else {
        this.appliedFilters['filter' + index] = [...e];
      }
      this.service.applyAdditionalFilters(this.appliedFilters);
    } else {
      console.log(this.appliedFilters);
    }
  }
}
