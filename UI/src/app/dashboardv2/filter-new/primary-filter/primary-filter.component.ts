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
  filters: any[];
  selectedFilters: any[] = [];
  subscriptions: any[] = [];
  @Output() onPrimaryFilterChange = new EventEmitter();
  @ViewChild('multiSelect') multiSelect: MultiSelect;

  constructor(private service: SharedService, private helperService: HelperService) {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (this.filterData && Object.keys(this.filterData).length) {
      if (this.selectedLevel && typeof this.selectedLevel === 'string' && this.selectedLevel.length) {
        this.filters = this.helperService.sortAlphabetically(this.filterData[this.selectedLevel]);
      } else if (this.selectedLevel && Object.keys(this.selectedLevel).length) {
        this.filters = this.helperService.sortAlphabetically(this.filterData[this.selectedLevel.emittedLevel.toLowerCase()]);
        // check for iterations and releases
        if (this.selectedLevel.nodeType.toLowerCase() === 'project') {
          this.filters = this.helperService.sortAlphabetically(this.filters.filter((filter) => filter.parentId === this.selectedLevel.nodeId));
        } else {
          this.filters = this.helperService.sortAlphabetically(this.filters.filter((filter) => filter.nodeId === this.selectedLevel.nodeId));
        }
      } else {
        this.selectedLevel = 'project';
        this.filters = this.filterData[this.selectedLevel];
      }

      if (this.filters && this.filters.length && (changes['selectedLevel'] || changes['selectedType']?.currentValue !== changes['selectedType']?.previousValue)) {
        setTimeout(() => {
          this.selectedFilters = [];
          this.filters = this.helperService.sortAlphabetically(this.filters);
          this.selectedFilters.push(this.filters[0]);
          this.onPrimaryFilterChange.emit(this.selectedFilters);
        }, 0);
      } else {
        this.onPrimaryFilterChange.emit(this.selectedFilters);
      }
    }
  }

  ngOnInit() {
    this.subscriptions.push(this.service.mapColorToProjectObs.subscribe((val) => {
      if (this.selectedFilters.length && this.selectedFilters[0]) {
        this.selectedFilters = this.selectedFilters.filter((filter) => Object.keys(val).includes(filter.nodeId));
      }
    }));
  }



  applyPrimaryFilters(event) {
    if (!Array.isArray(this.selectedFilters)) {
      this.selectedFilters = [this.selectedFilters];
    }
    this.onPrimaryFilterChange.emit(this.selectedFilters);
    if (this.multiSelect?.overlayVisible) {
      this.multiSelect.close(event);
    }
  }

}
