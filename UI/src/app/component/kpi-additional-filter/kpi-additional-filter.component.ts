import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

@Component({
  selector: 'app-kpi-additional-filter',
  templateUrl: './kpi-additional-filter.component.html',
  styleUrls: ['./kpi-additional-filter.component.css'],
})
export class KpiAdditionalFilterComponent implements OnInit {
  @Input() data;
  @Input() dataCopy;
  @Input() modifiedData;
  @Input() selectedFilter2;
  @Input() selectedMainFilter;
  @Input() filters;
  @Output() modifiedDataResult = new EventEmitter();
  constructor() {}

  ngOnInit(): void {}

  applyAdditionalFilters() {
    this.selectedFilter2.forEach((element) => {
      if (element.selectedValue) {
        if (element.filterType === 'Single') {
          this.dataCopy = this.dataCopy.filter(
            (d) => d[element.filterKey] === element.selectedValue,
          );
        } else {
          this.dataCopy = this.dataCopy.filter((d) => {
            let dataProperty = new Set(d[element.filterKey]);
            return (
              element.selectedValue.filter((item) => dataProperty.has(item))
                .length > 0
            );
          });
        }
      }
    });
    this.modifiedData = this.groupData(
      this.dataCopy,
      this.selectedMainFilter.filterKey,
    );
    let resultObj = {};
    resultObj['dataCopy'] = this.dataCopy;
    resultObj['modifiedData'] = this.modifiedData;
    resultObj['selectedFilter2'] = this.selectedFilter2;
    this.modifiedDataResult.emit(resultObj);
  }

  groupData(arr, property) {
    if (arr?.length) {
      // Create an object to store the grouped counts
      const groupedCounts = {};

      // Iterate through the array
      arr.forEach((item) => {
        // Get the value of the specified property
        const propValue = item[property];

        // If the value is already in the groupedCounts object, increment its count
        if (groupedCounts[propValue]) {
          groupedCounts[propValue]++;
        } else {
          // Otherwise, initialize the count for this value to 1
          groupedCounts[propValue] = 1;
        }
      });

      // Convert the groupedCounts object to an array of objects
      const result = Object.keys(groupedCounts).map((key) => ({
        [key]: groupedCounts[key],
      }));

      return result;
    } else {
      return [];
    }
  }

  clearAdditionalFilters() {
    this.dataCopy = Object.assign([], this.data);
    this.modifiedData = this.groupData(
      this.data,
      this.selectedMainFilter.filterKey,
    );
    this.selectedFilter2.forEach((filter) => {
      filter.selectedValue = null;
    });
    this.selectedFilter2 = null;
    let resultObj = {};
    resultObj['dataCopy'] = this.dataCopy;
    resultObj['modifiedData'] = this.modifiedData;
    resultObj['selectedFilter2'] = this.selectedFilter2;
    this.modifiedDataResult.emit(resultObj);
  }

  clearFilter(i) {
    delete this.selectedFilter2[i].selectedValue;
    this.dataCopy = Object.assign([], this.data);
    this.applyAdditionalFilters();
  }

  additionalFilterChanged() {
    this.dataCopy = Object.assign([], this.data);
  }
}
