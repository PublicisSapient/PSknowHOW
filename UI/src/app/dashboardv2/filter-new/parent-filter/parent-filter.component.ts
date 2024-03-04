import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
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
  additionalFilterLevels = ['release', 'sprint', 'sqd'];
  @Output() onSelectedLevelChange = new EventEmitter();
  constructor(private helperService: HelperService) { }

  ngOnChanges(changes: SimpleChanges) {
    if ((changes['selectedTab'] && changes['selectedTab']?.currentValue !== changes['selectedTab']?.previousValue) ||
    changes['selectedType'] && changes['selectedType']?.currentValue !== changes['selectedType']?.previousValue) {
      if (this['parentFilterConfig']['labelName'] === 'Organization Level') {
        this.filterLevels = Object.keys(this.filterData);
        this.filterLevels = this.filterLevels.filter((level) => !this.additionalFilterLevels.includes(level));

        setTimeout(() => {
          if ((changes['parentFilterConfig'] && changes['parentFilterConfig'].previousValue?.labelName !== changes['parentFilterConfig'].currentValue.labelName) || !this.selectedLevel || (changes['selectedType']?.currentValue !== changes['selectedType']?.previousValue)) {
            this.selectedLevel = this.filterLevels[this.filterLevels.length - 1];
          }
          this.onSelectedLevelChange.emit(this.selectedLevel);
        }, 0);

      } else if (this['parentFilterConfig']['labelName'] !== 'Organization Level') {
        if (this.filterData && Object.keys(this.filterData).length) {
          this.filterLevels = this.filterData[this['parentFilterConfig']['labelName'].toLowerCase()].map((item) => item.nodeName);
          this.filterLevels = this.helperService.sortAlphabetically(this.filterLevels);

          setTimeout(() => {
            if ((changes['parentFilterConfig'] && changes['parentFilterConfig'].previousValue?.labelName !== changes['parentFilterConfig'].currentValue.labelName) || !this.selectedLevel || (changes['selectedType']?.currentValue !== changes['selectedType']?.previousValue)) {
              this.selectedLevel = this.filterLevels[0];
            }
            let selectedNodeId = this.filterData[this['parentFilterConfig']['labelName'].toLowerCase()].filter((filter) => filter.nodeName === this.selectedLevel)[0].nodeId;
            this.onSelectedLevelChange.emit({ nodeId: selectedNodeId, nodeType: this['parentFilterConfig']['labelName'], emittedLevel: this.parentFilterConfig['emittedLevel'] });
          }, 0);
        }
      }
    }
  }

  handleSelectedLevelChange() {
    if (this['parentFilterConfig']['labelName'] === 'Organization Level') {
      this.onSelectedLevelChange.emit(this.selectedLevel);
    } else {
      setTimeout(() => {
        let selectedNodeId = this.filterData[this['parentFilterConfig']['labelName'].toLowerCase()].filter((filter) => filter.nodeName === this.selectedLevel)[0].nodeId;
        this.onSelectedLevelChange.emit({ nodeId: selectedNodeId, nodeType: this['parentFilterConfig']['labelName'], emittedLevel: this.parentFilterConfig['emittedLevel'] });
      }, 0);
    }
  }
}
