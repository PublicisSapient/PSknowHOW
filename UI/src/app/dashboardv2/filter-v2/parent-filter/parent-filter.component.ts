import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { HelperService } from 'src/app/services/helper.service';
import { ActivatedRoute } from '@angular/router';

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
  getProjectIdFromParam: any;
  isRelease: any;
  constructor(private helperService: HelperService, private route: ActivatedRoute) {
    this.route.queryParams.subscribe(params => {
      console.log(params); // All query params as an object
      this.getProjectIdFromParam = params.Project;
      this.isRelease = params.release;
      // const isRelease = params.release;
      // if(isRelease) {
      //   this.selectedLevel = this.filterData[this.parentFilterConfig['labelName']]?.filter((level) => level.basicProjectConfigId === params.Project);
      //   console.log('parent selected filter ', this.selectedLevel)
      // }
    });
  }

  ngOnChanges(changes: SimpleChanges) {
    // console.log('parent filter SimpleChanges ', changes)
    // console.log('parent filter onchange ', this.helperService.getBackupOfFilterSelectionState())
    const prevLabel = changes['parentFilterConfig'].previousValue?.labelName?.toLowerCase();
    const currLabel = changes['parentFilterConfig'].currentValue?.labelName?.toLowerCase();
    const isFirstChange = changes['parentFilterConfig'].firstChange;
    console.log('30 parent filter ', this.parentFilterConfig)
    console.log('parent 31 ', this.filterData)
    if (changes['selectedTab'] && changes['selectedTab']?.currentValue !== changes['selectedTab']?.previousValue || changes['selectedType'] && changes['selectedType']?.currentValue !== changes['selectedType']?.previousValue) {
      if (this['parentFilterConfig']['labelName'] === 'Organization Level') {
        this.fillAdditionalFilterLevels();
        this.filterLevels = Object.keys(this.filterData);
        this.filterLevels = this.filterLevels.filter((level) => !this.additionalFilterLevels.includes(level));

        this.stringToObject();
        this.stateFilters = this.helperService.getBackupOfFilterSelectionState('parent_level');
        console.log('35 parent_level filter state', this.stateFilters);
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
          console.log('parent 54 ', this.selectedLevel)
          this.onSelectedLevelChange.emit(this.selectedLevel);
        });
      } else if (
        (prevLabel === 'organization level' && currLabel === 'project') ||
        (prevLabel === 'project' && currLabel === 'organization level') ||
        (prevLabel === 'project' && currLabel === 'project') ||
        isFirstChange
      ) {
        console.log('63')
        this.filterLevels = this.filterData[this.parentFilterConfig['labelName']]?.map((item) => item.nodeName);
        this.filterLevels = this.helperService.sortAlphabetically(this.filterLevels);
        this.stringToObject();
        // console.log('parent 67 ', this.filterLevels)
        if(this.isRelease) {
          this.selectedLevel = this.filterData[this.parentFilterConfig['labelName']]?.filter((level) => {
            return level.basicProjectConfigId === this.getProjectIdFromParam
          })[0].nodeName;
        }
        console.log('parent 68 ', this.selectedLevel)
        this.stateFilters = this.helperService.getBackupOfFilterSelectionState('primary_level');
        console.log('59 primary_level filter state', this.stateFilters);
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
          console.log('101 ', this.filterLevels[0])
          console.log('102 ', this.selectedLevel)
          this.helperService.setBackupOfFilterSelectionState({ 'parent_level': this.selectedLevel })
          let selectedNode = this.filterData[this['parentFilterConfig']['labelName']].filter((filter) => filter.nodeName === this.selectedLevel);
          console.log('parent 54 ', { nodeId: selectedNode[0].nodeId, nodeType: this['parentFilterConfig']['labelName'], emittedLevel: this.parentFilterConfig['emittedLevel'], fullNodeDetails: selectedNode })
          this.onSelectedLevelChange.emit({ nodeId: selectedNode[0].nodeId, nodeType: this['parentFilterConfig']['labelName'], emittedLevel: this.parentFilterConfig['emittedLevel'], fullNodeDetails: selectedNode });
        });
      } else if (this.filterData && Object.keys(this.filterData).length) {
        console.log('86')
        this.filterLevels = this.filterData[this['parentFilterConfig']['labelName']]?.map((item) => item.nodeName);
        this.filterLevels = this.helperService.sortAlphabetically(this.filterLevels);
        this.stringToObject();
        this.stateFilters = this.helperService.getBackupOfFilterSelectionState('parent_level');
        console.log('81 parent_level filter state', this.stateFilters);
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
          console.log('parent 107 ', { nodeId: selectedNode[0].nodeId, nodeType: this['parentFilterConfig']['labelName'], emittedLevel: this.parentFilterConfig['emittedLevel'], fullNodeDetails: selectedNode })
          this.onSelectedLevelChange.emit({ nodeId: selectedNode[0].nodeId, nodeType: this['parentFilterConfig']['labelName'], emittedLevel: this.parentFilterConfig['emittedLevel'], fullNodeDetails: selectedNode });
        });
      }
    }
  }

  fillAdditionalFilterLevels() {
    if (this.filterData['Project']?.length) {
      let projectLevel = this.filterData['Project'][0].level;
      Object.keys(this.filterData).forEach((key) => {
        if (this.filterData[key][0].level > projectLevel) {
          this.additionalFilterLevels.push(key);
        }
      });
    }
  }

  handleSelectedLevelChange() {
    if (this['parentFilterConfig']['labelName'] === 'Organization Level') {
      console.log('parent 127 ', this.selectedLevel)
      this.onSelectedLevelChange.emit(this.selectedLevel);
    } else {
      let selectedNode = this.filterData[this['parentFilterConfig']['labelName']].filter((filter) => filter.nodeName === this.selectedLevel);
      console.log('parent 131 ', { nodeId: selectedNode[0].nodeId, nodeType: this['parentFilterConfig']['labelName'], emittedLevel: this.parentFilterConfig['emittedLevel'], fullNodeDetails: selectedNode })
      this.onSelectedLevelChange.emit({ nodeId: selectedNode[0].nodeId, nodeType: this['parentFilterConfig']['labelName'], emittedLevel: this.parentFilterConfig['emittedLevel'], fullNodeDetails: selectedNode });
    }
    this.helperService.setBackupOfFilterSelectionState({ 'parent_level': this.selectedLevel, 'primary_level': null });
  }

  stringToObject() {
    this.options = [];
    this.filterLevels.forEach(example_string => {
      this.options.push({ label: example_string });
    });
  }
}
