import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, NgZone, ChangeDetectorRef } from '@angular/core';
import { HelperService } from 'src/app/services/helper.service';


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
  selectedLevel: any;
  stateFilters: string = '';
  additionalFilterLevels = ['release', 'sprint', 'sqd'];
  @Output() onSelectedLevelChange = new EventEmitter();
  constructor(private helperService: HelperService, private ngZone: NgZone, private cdr: ChangeDetectorRef) { }

  ngOnChanges(changes: SimpleChanges) {

    if (changes['selectedTab'] && changes['selectedTab']?.currentValue !== changes['selectedTab']?.previousValue || changes['selectedType'] && changes['selectedType']?.currentValue !== changes['selectedType']?.previousValue) {
      if (this['parentFilterConfig']['labelName'] === 'Organization Level') {
        this.filterLevels = Object.keys(this.filterData);
        this.filterLevels = this.filterLevels.filter((level) => !this.additionalFilterLevels.includes(level));
        this.filterLevels = this.filterLevels.map(level => level.toUpperCase());
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
          this.cdr.detectChanges();
        });
      } else if (this['parentFilterConfig']['labelName'] !== 'Organization Level') {
        if (this.filterData && Object.keys(this.filterData).length) {
          this.filterLevels = this.filterData[this['parentFilterConfig']['labelName'].toLowerCase()]?.map((item) => item.nodeName);
          this.filterLevels = this.helperService.sortAlphabetically(this.filterLevels);
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

}
