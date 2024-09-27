import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { HelperService } from 'src/app/core/services/helper.service';
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
  filterLevels = [];
  options: any[] = [];
  selectedLevel: any;
  stateFilters: string = '';
  additionalFilterLevels = [];
  @Output() onSelectedLevelChange = new EventEmitter();
  filterValue: string = '';
  constructor(private helperService: HelperService) { }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['parentFilterConfig']) {
      if (this['parentFilterConfig']['labelName'] === 'Organization Level') {
        this.fillAdditionalFilterLevels();
        this.filterLevels = Object.keys(this.filterData).map((item) => {
          return {
            nodeId: item,
            nodeName: item
          }
        });
        this.filterLevels = this.filterLevels.filter((level) => !this.additionalFilterLevels.includes(level.nodeName));

        this.stateFilters = this.helperService.getBackupOfFilterSelectionState('parent_level');
        Promise.resolve().then(() => {
          if (this.stateFilters && typeof this.stateFilters === 'string') {
            this.selectedLevel = this.filterLevels.filter((level) => { return level.nodeId.toLowerCase() === this.stateFilters.toLowerCase() })[0];
          } else if (this.stateFilters && typeof this.stateFilters !== 'string' && this.stateFilters['labelName']) {
            this.selectedLevel = this.filterLevels.filter((level) => { return level.nodeId.toLowerCase() === this.stateFilters['labelName'].toLowerCase() })[0];
          } else if (this.stateFilters && typeof this.stateFilters !== 'string') {
            this.selectedLevel = this.filterLevels.filter((level) => { return level.nodeId.toLowerCase() === this.stateFilters['nodeId'].toLowerCase() })[0];
          } else {
            this.selectedLevel = this.filterLevels[this.filterLevels.length - 1];
          }
          this.handleSelectedLevelChange();
        });
      } else {
        this.filterLevels = this.filterData[this['parentFilterConfig']['labelName']]?.map((item) => {
          return {
            nodeId: item.nodeId,
            nodeName: item.nodeName
          }
        });
        this.filterLevels = this.helperService.sortAlphabetically(this.filterLevels);

        this.stateFilters = this.helperService.getBackupOfFilterSelectionState('primary_level');
        Promise.resolve().then(() => {
          if (this.stateFilters) {
            if (Array.isArray(this.stateFilters)) {
              this.stateFilters = this.stateFilters[0];
            }
            if (this.stateFilters['labelName']?.toLowerCase() === this['parentFilterConfig']['labelName']?.toLowerCase()) {
              this.selectedLevel = this.filterLevels.filter((level) => { return level.nodeId === this.stateFilters['nodeId'] })[0];
            } else if (this.stateFilters['labelName']?.toLowerCase() === 'sprint' || this.stateFilters['labelName']?.toLowerCase() === 'release') {
              this.selectedLevel = this.filterLevels.filter((level) => { return level.nodeId === this.stateFilters['parentId'] })[0];
            } else {
              this.selectedLevel = this.filterLevels[0];
              this.handleSelectedLevelChange(true);
              return;
            }
          } else {
            this.selectedLevel = this.filterLevels[0];
            this.handleSelectedLevelChange(true);
            return;
          }
          this.handleSelectedLevelChange();
        });
      }
    }
  }

  fillAdditionalFilterLevels() {
    if (this.filterData['Project']?.length) {
      let projectLevel = this.filterData['Project'][0].level;
      Object.keys(this.filterData).forEach((key) => {
        if (this.filterData[key] !== undefined) {
          if (this.filterData[key][0].level > projectLevel) {
            this.additionalFilterLevels.push(key);
          }
        }
      });
    }
  }

  handleSelectedLevelChange(parentLevelChanged = false) {
    if (this['parentFilterConfig']['labelName'] === 'Organization Level') {
      this.onSelectedLevelChange.emit(this.selectedLevel.nodeName);
      if (parentLevelChanged) {
        this.helperService.setBackupOfFilterSelectionState({ 'parent_level': this.selectedLevel.nodeName, 'primary_level': null });
      } else {
        this.helperService.setBackupOfFilterSelectionState({ 'parent_level': this.selectedLevel.nodeName });
      }
    } else {
      let selectedNode = this.filterData[this['parentFilterConfig']['labelName']].filter((filter) => filter.nodeId === this.selectedLevel.nodeId);
      this.onSelectedLevelChange.emit({ nodeId: selectedNode[0]?.nodeId, nodeType: this['parentFilterConfig']['labelName'], emittedLevel: this.parentFilterConfig['emittedLevel'], fullNodeDetails: selectedNode });
      if (parentLevelChanged) {
        this.helperService.setBackupOfFilterSelectionState({ 'parent_level': selectedNode[0], 'primary_level': null });
      } else {
        this.helperService.setBackupOfFilterSelectionState({ 'parent_level': selectedNode[0] });
      }
    }
  }
}
