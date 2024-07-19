import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { HelperService } from 'src/app/services/helper.service';
import { DropdownFilterOptions } from 'primeng/dropdown';

@Component({
  selector: 'app-parent-filter',
  templateUrl: './parent-filter.component.html',
  styleUrls: ['./parent-filter.component.css']
})
export class ParentFilterComponent implements OnChanges {
  @Input() filterData = null;
  @Input() parentFilterConfig: {};
  @Input() selectedType: string = '';
  @Input() selectedTab: string = '';
  filterLevels: string[];
  options: any[] = [];
  selectedLevel: any;
  stateFilters: string = '';
  additionalFilterLevels = [];
  @Output() onSelectedLevelChange = new EventEmitter();
  filterValue: string = '';
  constructor(private helperService: HelperService) { }

  ngOnChanges(changes: SimpleChanges) {

    if (changes['selectedTab'] && changes['selectedTab']?.currentValue !== changes['selectedTab']?.previousValue || changes['selectedType'] && changes['selectedType']?.currentValue !== changes['selectedType']?.previousValue) {
      if (this['parentFilterConfig']['labelName'] === 'Organization Level') {
        this.fillAdditionalFilterLevels();
        this.filterLevels = Object.keys(this.filterData);
        this.filterLevels = this.filterLevels.filter((level) => !this.additionalFilterLevels.includes(level));
        this.filterLevels = this.filterLevels.map(level => level.toUpperCase());
        this.stringToObject();
        this.stateFilters = this.helperService.getBackupOfFilterSelectionState('parent_level');
        Promise.resolve().then(() => {
          if ((changes['parentFilterConfig'] && changes['parentFilterConfig'].previousValue?.labelName !== changes['parentFilterConfig'].currentValue.labelName) || !this.selectedLevel) {
            if (this.stateFilters) {
              this.selectedLevel = this.filterLevels.filter((level) => {
                return level.toLowerCase() === this.stateFilters.toLowerCase()
              })[0];
            }

            if (!this.stateFilters || !this.selectedLevel) {
              this.selectedLevel = this.filterLevels[this.filterLevels.length - 1];

            }
            this.helperService.setBackupOfFilterSelectionState({ 'parent_level': this.selectedLevel })
          }

          this.onSelectedLevelChange.emit(this.selectedLevel.toLowerCase());
        });
      } else if (changes['parentFilterConfig'].previousValue?.labelName === 'Organization Level' && changes['parentFilterConfig'].currentValue?.labelName?.toLowerCase() === 'project' ||
      changes['parentFilterConfig'].previousValue?.labelName.toLowerCase() === 'project' && changes['parentFilterConfig'].currentValue?.labelName === 'Organization Level' ||
        (changes['parentFilterConfig'].previousValue?.labelName.toLowerCase() === 'project' && changes['parentFilterConfig'].currentValue?.labelName?.toLowerCase() === 'project') ||
        changes['parentFilterConfig'].firstChange) {
        this.filterLevels = this.filterData[this['parentFilterConfig']['labelName'].toLowerCase()]?.map((item) => item.nodeName);
        this.filterLevels = this.helperService.sortAlphabetically(this.filterLevels);
        this.stringToObject();

        this.stateFilters = this.helperService.getBackupOfFilterSelectionState('primary_level');

        Promise.resolve().then(() => {
          if (this.stateFilters?.length) {
            if (this.stateFilters[0]['labelName'] === 'project') {
              this.selectedLevel = this.filterLevels?.filter((level) => {
                return level === this.stateFilters[0]['nodeName']
              })[0];
            }
          }
          if (!this.stateFilters || !this.selectedLevel) {
            this.selectedLevel = this.filterLevels[0];
          }
          this.helperService.setBackupOfFilterSelectionState({ 'parent_level': this.selectedLevel })
          let selectedNode = this.filterData[this['parentFilterConfig']['labelName'].toLowerCase()].filter((filter) => filter.nodeName === this.selectedLevel);
          this.onSelectedLevelChange.emit({ nodeId: selectedNode[0].nodeId, nodeType: this['parentFilterConfig']['labelName'], emittedLevel: this.parentFilterConfig['emittedLevel'], fullNodeDetails: selectedNode });
        });
      }
      else if (this.filterData && Object.keys(this.filterData).length) {
        this.filterLevels = this.filterData[this['parentFilterConfig']['labelName'].toLowerCase()]?.map((item) => item.nodeName);
        this.filterLevels = this.helperService.sortAlphabetically(this.filterLevels);
        this.stringToObject();
        this.stateFilters = this.helperService.getBackupOfFilterSelectionState('parent_level');

        Promise.resolve().then(() => {
          if ((changes['parentFilterConfig'] && changes['parentFilterConfig'].previousValue?.labelName !== changes['parentFilterConfig'].currentValue.labelName) || !this.selectedLevel) {
            if (this.stateFilters) {
              this.selectedLevel = this.filterLevels?.filter((level) => {
                return level.toLowerCase() === this.stateFilters.toLowerCase()
              })[0];
            }

            if (!this.stateFilters || !this.selectedLevel) {
              this.selectedLevel = this.filterLevels[0];
            }
            this.helperService.setBackupOfFilterSelectionState({ 'parent_level': this.selectedLevel })
          }
          let selectedNode = this.filterData[this['parentFilterConfig']['labelName'].toLowerCase()].filter((filter) => filter.nodeName === this.selectedLevel);
          this.onSelectedLevelChange.emit({ nodeId: selectedNode[0].nodeId, nodeType: this['parentFilterConfig']['labelName'], emittedLevel: this.parentFilterConfig['emittedLevel'], fullNodeDetails: selectedNode });
        });
      }
    }
  }

  fillAdditionalFilterLevels() {
    if (this.filterData['project']?.length) {
      let projectLevel = this.filterData['project'][0].level;
      Object.keys(this.filterData).forEach((key) => {
        if (this.filterData[key][0].level > projectLevel) {
          this.additionalFilterLevels.push(key);
        }
      });
    }
  }

  handleSelectedLevelChange() {
    if (this['parentFilterConfig']['labelName'] === 'Organization Level') {
      this.onSelectedLevelChange.emit(this.selectedLevel.toLowerCase());
    } else {
      let selectedNode = this.filterData[this['parentFilterConfig']['labelName']?.toLowerCase()].filter((filter) => filter.nodeName === this.selectedLevel);
      this.onSelectedLevelChange.emit({ nodeId: selectedNode[0].nodeId, nodeType: this['parentFilterConfig']['labelName'], emittedLevel: this.parentFilterConfig['emittedLevel'], fullNodeDetails: selectedNode });
    }
    this.helperService.setBackupOfFilterSelectionState({ 'parent_level': this.selectedLevel.toLowerCase(), 'primary_level': null });
  }

  stringToObject() {
    this.options = [];
    this.filterLevels.forEach(example_string => {
      this.options.push({ label: example_string });
    });
  }
}
