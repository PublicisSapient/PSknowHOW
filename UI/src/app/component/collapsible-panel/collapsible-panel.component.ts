import { Component, Input, OnInit, ViewChild, ElementRef, HostListener, OnChanges, SimpleChanges } from '@angular/core';
import { SharedService } from 'src/app/services/shared.service';

@Component({
  selector: 'app-collapsible-panel',
  templateUrl: './collapsible-panel.component.html',
  styleUrls: ['./collapsible-panel.component.css']
})
export class CollapsiblePanelComponent implements OnInit, OnChanges {
  @Input() rawData: any;
  cols: any;
  filterLevels : any  = [];
  selectedLevel : any =  {};
  filters : any = [];
  selectedFilters : any = {};
  addtionalFIlters = ['sprint','release','sqd']
  filterDataArr : any = {};
  activeIndices: number[] = [];
  isAllExpanded = false;
  filterRawData;
  levelDetails;
  selectedLevelFullDetails;
  accordionData;

@ViewChild('sprintGoalContainer') sprintGoalContainer!: ElementRef;
@HostListener('document:click', ['$event'])

  onClickOutside(event: Event) {
    const targetElement = event.target as HTMLElement;
    // If clicked element has class 'pi pi-check-square', do nothing
    if (targetElement.classList.contains('pi') && targetElement.classList.contains('pi-check-square')) {
      return;
    }

  }

  constructor(private sharedService : SharedService) { }

  ngOnInit(): void {
  this.levelDetails = JSON.parse(localStorage.getItem('completeHierarchyData'))['scrum'];
  this.setUpPanel();
  this.cols = [
    {
      header : 'Sprint Name',
      field : 'name'
    },
    {
      header : 'Sprint Goal',
      field : 'goal'
    }
  ]

  this.sharedService.onScrumKanbanSwitch.subscribe(type => {
    this.sharedService.updateSprintGoalFlag(false)
  })

  this.sharedService.onTabSwitch.subscribe((tab)=>{
    this.sharedService.updateSprintGoalFlag(false) 
  })
  
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.accordionData = this.rawData
    if(changes.rawData.firstChange === false){
      this.setUpPanel();
    }
    
  }

  onHierarchyDropdownChange(event){
   this.filters = this.buildFilterDropdown().get(event.value.hierarchyLevelName);
    this.selectedFilters = [this.filters[0]];
    this.accordionData = this.rawData;
  }

  onSelectionChange(event){
    let selectedNodeIds = event.value.map(item => item.nodeId);
    let filteredResults = this.filterByHierarchyNodeId(this.rawData, selectedNodeIds);
    this.accordionData = filteredResults;
    console.log("Filtered Results:", filteredResults, this.rawData);
  }

  filterByHierarchyNodeId(data: any[], nodeIds: string[]): any[] {
    return data.filter(item => 
        item.hierarchy.some(h => nodeIds.includes(h.orgHierarchyNodeId))
    );
  }

  toggleAll() {
    if (this.isAllExpanded) {
      this.activeIndices = [];
    } else {
      this.activeIndices = this.rawData.map((_, index) => index); 
    }
    this.isAllExpanded = !this.isAllExpanded;
  }

  setUpPanel(){
    this.filterRawData = this.sharedService.getDataForSprintGoal();
    this.filterDataArr =  this.filterRawData.filterDataArr;
     this.selectedLevelFullDetails = this.levelDetails.find(details=>details.hierarchyLevelName === this.filterRawData.selectedLevel.nodeDisplayName)
    this.filterLevels = this.levelDetails.filter(details=>details.level >= this.selectedLevelFullDetails.level && !this.addtionalFIlters.includes(details.hierarchyLevelId) )
    this.filters = this.buildFilterDropdown().get(this.filterRawData.selectedLevel.nodeName);
    this.selectedLevel = this.selectedLevelFullDetails;
    this.selectedFilters = this.filterRawData.selectedFilters;
  }

  buildFilterDropdown() {
    let retValue = new Map<string, any[]>();

    let currentIndex = this.filterRawData.filterLevels.findIndex(
        (x) => x.nodeName === this.filterRawData.selectedLevel.nodeName
    );

    if (currentIndex === -1) {
        console.error("Selected Level Not Found in filterLevels!");
        return;
    }

    let parentIds: string[] = [this.filterRawData.selectedFilters[0].nodeId];
    let children = [];

    for (let index = currentIndex; index < this.filterRawData.filterLevels.length; index++) {
        let levelName = this.filterRawData.filterLevels[index].nodeName;

        if (index === currentIndex) {
            let selectedData = this.filterRawData.filterDataArr[levelName]
                .filter(x => parentIds.includes(x.nodeId));
                retValue.set(levelName, selectedData);
        } else {
            children = this.filterRawData.filterDataArr[levelName]
                .filter(x => parentIds.includes(x.parentId));
                retValue.set(levelName, children);
                
            if (children.length > 0) {
                parentIds = children.map(child => child.nodeId);
            } else {
                console.warn(`No children found for Level: ${levelName}`);
                break;
            }
        }
    }
     return retValue;
  }
}
