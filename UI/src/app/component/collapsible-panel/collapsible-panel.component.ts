import { Component, Input, OnInit, ViewChild, ElementRef, HostListener } from '@angular/core';
import { SharedService } from 'src/app/services/shared.service';

@Component({
  selector: 'app-collapsible-panel',
  templateUrl: './collapsible-panel.component.html',
  styleUrls: ['./collapsible-panel.component.css']
})
export class CollapsiblePanelComponent implements OnInit {
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
  const filterRawData = this.sharedService.getDataForSprintGoal();
  this.filterDataArr =  filterRawData.filterDataArr;
  console.log(filterRawData)
  const levelDetails = JSON.parse(localStorage.getItem('completeHierarchyData'))['scrum'];
  const selectedLevelFullDetails = levelDetails.find(details=>details.hierarchyLevelName === filterRawData.selectedLevel.nodeDisplayName)
  this.filterLevels = levelDetails.filter(details=>details.level >= selectedLevelFullDetails.level && !this.addtionalFIlters.includes(details.hierarchyLevelId) )
  this.filters = filterRawData.filters;
  this.selectedLevel = selectedLevelFullDetails;
  this.selectedFilters = filterRawData.selectedFilters;
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

  onHierarchyDropdownChange(event){
    this.filters = this.filterDataArr[event.value.hierarchyLevelName]
    this.selectedFilters = [this.filters[0]]

  }

  onSelectionChange(event){
    console.log("on change")
  }

  toggleAll() {
    if (this.isAllExpanded) {
      this.activeIndices = [];
    } else {
      this.activeIndices = this.rawData.map((_, index) => index); 
    }
    this.isAllExpanded = !this.isAllExpanded;
  }
  
}
