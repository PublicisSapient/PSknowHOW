import {
  Component,
  EventEmitter,
  HostListener,
  Input,
  OnChanges,
  Output,
  SimpleChanges,
  ViewChild,
} from '@angular/core';
import { MultiSelect } from 'primeng/multiselect';
import { SharedService } from 'src/app/services/shared.service';
import { HelperService } from 'src/app/services/helper.service';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-primary-filter',
  templateUrl: './primary-filter.component.html',
  styleUrls: ['./primary-filter.component.css'],
})
export class PrimaryFilterComponent implements OnChanges {
  @Input() filterData = null;
  @Input() selectedLevel: any = '';
  @Input() primaryFilterConfig: any;
  @Input() selectedType: string = '';
  @Input() selectedTab: string = '';
  filters: any[];
  previousSelectedFilters: any = [];
  selectedFilters: any = [];
  selectedAdditionalFilters: any;
  subscriptions: any[] = [];
  stateFilters: any = {};
  hierarchyLevels: any[] = [];
  defaultFilterCounter: number = 0;
  @Output() onPrimaryFilterChange = new EventEmitter();
  @ViewChild('multiSelect') multiSelect: MultiSelect;
  applyFilters: boolean = false;
  preventClose: boolean;

  constructor(
    public service: SharedService,
    public helperService: HelperService,
  ) {
    // This is required speecifically when filter is removed from removeFilter fn on filter-new
    this.service.selectedTrendsEvent.subscribe((filters) => {
      if (
        filters?.length &&
        this.primaryFilterConfig['type'] !== 'singleSelect'
      ) {
        this.selectedFilters = filters;
        this.service.setDataForSprintGoal({
          selectedFilters: this.selectedFilters,
        });
      }
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    const selectedLevelChanged =
      changes['selectedLevel'] &&
      !this.helperService.deepEqual(
        changes['selectedLevel']?.currentValue,
        changes['selectedLevel'].previousValue,
      );
    const primaryFilterConfigChanged =
      changes['primaryFilterConfig'] &&
      Object.keys(changes['primaryFilterConfig'].currentValue).length &&
      !changes['primaryFilterConfig']?.firstChange;
    const selectedTypeChanged =
      changes['selectedType'] &&
      changes['selectedType']?.currentValue !==
        changes['selectedType'].previousValue &&
      !changes['selectedType']?.firstChange;
    const selectedTabChanged =
      changes['selectedTab'] &&
      changes['selectedTab']?.currentValue !==
        changes['selectedTab'].previousValue &&
      !changes['selectedTab']?.firstChange;

    if (
      selectedLevelChanged ||
      primaryFilterConfigChanged ||
      selectedTypeChanged
    ) {
      // || selectedTabChanged)
      this.applyDefaultFilters();
      return;
    }

    let completeHiearchyData = JSON.parse(
      localStorage.getItem('completeHierarchyData'),
    )[this.selectedType.toLowerCase()];
    let projectLevelNode = completeHiearchyData?.filter(
      (x) => x.hierarchyLevelId === 'project',
    );
    this.hierarchyLevels = completeHiearchyData
      ?.filter((x) => x.level <= projectLevelNode[0].level)
      .map((x) => x.hierarchyLevelId);
  }

  applyDefaultFilters() {
    this.populateFilters();
    setTimeout(() => {
      this.stateFilters =
        this.service.getBackupOfUrlFilters() &&
        JSON.parse(this.service.getBackupOfUrlFilters())['primary_level']
          ? JSON.parse(this.service.getBackupOfUrlFilters())
          : this.service.getBackupOfFilterSelectionState();
      if (
        this.stateFilters &&
        Object.keys(this.stateFilters).length > 0 &&
        this.primaryFilterConfig &&
        this.primaryFilterConfig['defaultLevel'] &&
        this.primaryFilterConfig['defaultLevel']['labelName']
      ) {
        if (
          (this.filters?.length &&
            this.filters[0] &&
            this.filters[0]?.labelName.toLowerCase() ===
              this.primaryFilterConfig['defaultLevel'][
                'labelName'
              ].toLowerCase()) ||
          this.hierarchyLevels
            .map((x) => x.toLowerCase())
            .includes(this.filters[0]?.labelName.toLowerCase())
        ) {
          if (
            this.stateFilters &&
            Object.keys(this.stateFilters).length &&
            this.stateFilters['primary_level']?.length
          ) {
            this.selectedFilters = [];
            if (
              this.filters[0].labelName ===
              this.stateFilters['primary_level'][0].labelName
            ) {
              if (this.primaryFilterConfig['type'] === 'multiSelect') {
                this.stateFilters['primary_level'].forEach((element) => {
                  this.selectedFilters.push(
                    this.filters?.filter(
                      (project) => project.nodeId === element.nodeId,
                    )[0],
                  );
                });

                // in case project in state filters has been deleted
                if (!this.selectedFilters?.length || !this.selectedFilters[0]) {
                  this.selectedFilters = [this.selectCurrentProject()];
                  this.service.setBackupOfFilterSelectionState({
                    primary_level: null,
                  });
                }
              } else {
                this.selectedFilters = [
                  this.filters?.filter(
                    (project) =>
                      project.nodeId ===
                      this.stateFilters['primary_level'][0].nodeId,
                  )[0],
                ];
              }
            } else if (
              ['sprint', 'release'].includes(
                this.stateFilters['primary_level'][0][
                  'labelName'
                ].toLowerCase(),
              ) &&
              ['sprint', 'release'].includes(
                this.primaryFilterConfig['defaultLevel'][
                  'labelName'
                ].toLowerCase(),
              )
            ) {
              // reset
              this.reset();
              return;
            } else if (
              ['sprint', 'release'].includes(
                this.stateFilters['primary_level'][0][
                  'labelName'
                ].toLowerCase(),
              )
            ) {
              this.selectedFilters = [
                this.filters?.filter(
                  (project) =>
                    project.nodeId ===
                    this.stateFilters['primary_level'][0].parentId,
                )[0],
              ];
            } else {
              // reset
              this.reset();
              return;
            }
          } else {
            if (
              this.stateFilters &&
              this.stateFilters['parent_level'] &&
              this.stateFilters['parent_level']?.labelName?.toLowerCase() ===
                this.primaryFilterConfig['defaultLevel'][
                  'labelName'
                ]?.toLowerCase()
            ) {
              this.selectedFilters = [];
              this.selectedFilters.push(this.stateFilters['parent_level']);
            } else {
              if (
                this.primaryFilterConfig['defaultLevel'][
                  'labelName'
                ].toLowerCase() === this.filters[0]?.labelName.toLowerCase() ||
                this.hierarchyLevels
                  .map((x) => x.toLowerCase())
                  .includes(this.filters[0]?.labelName.toLowerCase())
              ) {
                // reset
                this.selectedFilters = [];
                this.selectedFilters.push(this.selectCurrentProject());
                this.service.setBackupOfFilterSelectionState({
                  primary_level: null,
                });
                this.applyPrimaryFilters({});
                return;
              } else {
                this.service.setNoSprints(true);
                this.onPrimaryFilterChange.emit([]);
                return;
              }
            }
          }
        } else {
          if (
            this.stateFilters['parent_level'] &&
            Object.keys(this.stateFilters['parent_level'])?.length
          ) {
            this.service.setBackupOfFilterSelectionState({
              primary_level: [this.stateFilters['parent_level']],
            });
          }
          this.service.setNoSprints(true);
          this.onPrimaryFilterChange.emit([]);
          return;
        }
        // PROBLEM AREA END
        this.service.setDataForSprintGoal({
          selectedFilters: this.selectedFilters,
        });
        this.applyPrimaryFilters({});
      } else {
        // this.selectedFilters = [this.filters[0]];
        // this.applyPrimaryFilters({});
      }
    }, 100);
  }

  reset() {
    this.selectedFilters = [];
    this.selectedFilters.push(this.selectCurrentProject());
    this.service.setBackupOfFilterSelectionState({
      parent_level: null,
      primary_level: null,
    });
    this.applyPrimaryFilters({});
  }

  populateFilters() {
    if (typeof this.selectedLevel === 'string' && this.selectedLevel.length) {
      this.filters = this.helperService.sortAlphabetically(
        this.filterData[this.selectedLevel],
      );
      if (this.primaryFilterConfig['defaultLevel']?.sortBy) {
        if (this.selectedTab.toLowerCase() === 'iteration') {
          this.filters = this.helperService.sortByField(
            this.filterData[this.selectedLevel].filter(
              (filter) => filter.parentId === this.selectedLevel.nodeId,
            ),
            [
              this.primaryFilterConfig['defaultLevel'].sortBy,
              'sprintStartDate',
            ],
          );
        } else if (this.selectedTab.toLowerCase() === 'release') {
          this.filters = this.helperService.releaseSorting(
            this.filterData[this.selectedLevel].filter(
              (filter) => filter.parentId === this.selectedLevel.nodeId,
            ),
          );
        } else {
          this.filters = this.helperService.sortByField(
            this.filterData[this.selectedLevel],
            [this.primaryFilterConfig['defaultLevel'].sortBy],
          );
        }
      } else {
        this.filters = this.helperService.sortAlphabetically(
          this.filterData[this.selectedLevel],
        );
      }
    } else if (this.selectedLevel && Object.keys(this.selectedLevel).length) {
      let selectedLevel = this.selectedLevel.emittedLevel;
      selectedLevel = selectedLevel[0].toUpperCase() + selectedLevel.slice(1);
      // check for iterations and releases
      if (this.filterData[selectedLevel]?.length) {
        if (this.primaryFilterConfig['defaultLevel']?.sortBy) {
          if (this.selectedTab.toLowerCase() === 'iteration') {
            this.filters = this.setDropdownWithMoreActiveOption(selectedLevel);
          } else if (this.selectedTab.toLowerCase() === 'release') {
            this.filters = this.helperService.releaseSorting(
              this.filterData[selectedLevel]?.filter(
                (filter) => filter.parentId === this.selectedLevel.nodeId,
              ),
            );
          } else {
            this.filters = this.helperService.sortByField(
              this.filterData[selectedLevel]?.filter(
                (filter) => filter.parentId === this.selectedLevel.nodeId,
              ),
              [this.primaryFilterConfig['defaultLevel'].sortBy],
            );
          }
        } else {
          this.filters = this.helperService.sortAlphabetically(
            this.filterData[selectedLevel]?.filter(
              (filter) => filter.parentId === this.selectedLevel.nodeId,
            ),
          );
        }
      } else {
        this.filters = [];
      }
    } else {
      this.selectedLevel = 'Project';
      if (this.filterData && this.filterData[this.selectedLevel]) {
        this.filters = this.helperService.sortAlphabetically(
          this.filterData[this.selectedLevel],
        );
      } else {
        this.filters = [];
      }
    }
    this.service.setDataForSprintGoal({ filters: this.filters });
  }

  applyPrimaryFilters(event) {
    if (
      this.primaryFilterConfig &&
      Object.keys(this.primaryFilterConfig).length
    ) {
      this.applyFilters = true;
      if (!Array.isArray(this.selectedFilters)) {
        this.selectedFilters = [this.selectedFilters];
      }

      if (
        this.selectedFilters?.length &&
        this.selectedFilters[0] &&
        Object.keys(this.selectedFilters[0]).length
      ) {
        this.service.setNoSprints(false);
        if (
          this.primaryFilterConfig['defaultLevel'][
            'labelName'
          ].toLowerCase() !== 'sprint' ||
          (this.selectedFilters?.length &&
            this.selectedFilters[0]?.sprintState?.toLowerCase() === 'active')
        ) {
          let addtnlStateFilters =
            JSON.parse(this.service.getBackupOfUrlFilters())
              ?.additional_level ||
            this.service.getBackupOfFilterSelectionState('additional_level');
          if (
            addtnlStateFilters &&
            (!this.previousSelectedFilters?.length ||
              this.arraysEqual(
                this.selectedFilters,
                this.previousSelectedFilters,
              )) &&
            this.selectedTab !== 'developer'
          ) {
            let combinedEvent = {};
            combinedEvent['additional_level'] = addtnlStateFilters;
            combinedEvent['primary_level'] = [...this.selectedFilters];
            this.previousSelectedFilters = [...this.selectedFilters];
            this.onPrimaryFilterChange.emit(combinedEvent);
          } else if (this.selectedFilters?.length) {
            this.previousSelectedFilters = [...this.selectedFilters];
            this.onPrimaryFilterChange.emit([...this.selectedFilters]);
            // project selection changed, reset addtnl. filters
            this.service.setBackupOfFilterSelectionState({
              additional_level: null,
            });
          } else {
            this.reset();
          }
          // this.defaultFilterCounter++;
        } else {
          this.service.setNoSprints(true);
          this.onPrimaryFilterChange.emit([]);
          this.service.setBackupOfFilterSelectionState({ primary_level: null });
        }

        if (
          this.selectedFilters &&
          this.selectedFilters[0] &&
          Object.keys(this.selectedFilters[0]).length
        ) {
          if (
            this.selectedTab?.toLowerCase() === 'developer' ||
            this.selectedTab?.toLowerCase() === 'backlog'
          ) {
            this.service.setBackupOfFilterSelectionState({
              parent_level: this.selectedFilters[0].labelName,
              primary_level: [...this.selectedFilters],
            });
          } else {
            this.service.setBackupOfFilterSelectionState({
              primary_level: [...this.selectedFilters],
            });
          }
          this.applyFilters = false;

          if (
            this.selectedFilters[0]?.labelName?.toLowerCase() === 'sprint' ||
            this.selectedFilters[0]?.labelName?.toLowerCase() === 'release'
          ) {
            this.service.setSelectedTrends(
              this.filterData['Project'].filter((x) =>
                this.selectedFilters.map((s) => s.parentId).includes(x.nodeId),
              ),
            );
            // this.service.setSelectedTrends(this.selectedFilters);
          } else if (
            this.selectedFilters[0]?.labelName?.toLowerCase() === 'project'
          ) {
            this.service.setSelectedTrends(this.selectedFilters);
          }
        }
      }

      if (this.multiSelect?.overlayVisible) {
        this.multiSelect.close(event);
      }
    }
    // else {
    //   this.onPrimaryFilterChange.emit([...this.selectedFilters]);
    //   this.service.setBackupOfFilterSelectionState({ 'primary_level': [...this.selectedFilters] });
    // }
  }

  compareObjects(obj1, obj2) {
    return JSON.stringify(obj1) === JSON.stringify(obj2);
  }

  moveSelectedOptionToTop() {
    // Filter selected options
    const selected = this.filters.filter((option) =>
      this.selectedFilters?.includes(option),
    );

    // Filter unselected options
    const unselected = this.filters.filter(
      (option) => !this.selectedFilters?.includes(option),
    );

    this.filters = [...selected, ...unselected];
  }

  onSelectionChange(event: any) {
    if (event?.value?.length > 0) {
      this.moveSelectedOptionToTop();
    }
  }

  arraysEqual(arr1, arr2) {
    if (arr1.length !== arr2.length) {
      return false;
    }

    for (let i = 0; i < arr1.length; i++) {
      if (!this.helperService.deepEqual(arr1[i], arr2[i])) {
        return false;
      }
    }

    return true;
  }

  isString(val): boolean {
    return typeof val === 'string';
  }

  onDropdownChange($event: any) {
    if (this.helperService.isDropdownElementSelected($event)) {
      this.applyPrimaryFilters($event);
    }
  }

  isFilterHidden(filterDataSet: any): boolean {
    if (this.selectedTab?.toLowerCase() === 'iteration') {
      if (
        filterDataSet.filter((x) => x.sprintState?.toLowerCase() === 'active')
          .length > 1
      ) {
        return false;
      }
      return true;
    } else {
      return false;
    }
  }

  setDropdownWithMoreActiveOption(selectedLevel) {
    const moreThanOneActiveOption = this.helperService.sortByField(
      this.filterData[selectedLevel]?.filter(
        (filter) =>
          filter.parentId === this.selectedLevel.nodeId &&
          filter.sprintState?.toLowerCase() === 'active',
      ),
      [this.primaryFilterConfig['defaultLevel'].sortBy, 'sprintStartDate'],
    );
    if (moreThanOneActiveOption.length > 1) {
      return moreThanOneActiveOption;
    } else {
      return this.helperService.sortByField(
        this.filterData[selectedLevel]?.filter(
          (filter) => filter.parentId === this.selectedLevel.nodeId,
        ),
        [this.primaryFilterConfig['defaultLevel'].sortBy, 'sprintStartDate'],
      );
    }
  }

  //function will return 1st project if project details are not present in URL and localsotrage.
  selectCurrentProject() {
    let retValue = this.filters[0];
    const backupState = this.service.getBackupOfFilterSelectionState();
    const defaultLabelName =
      this.primaryFilterConfig['defaultLevel']['labelName']?.toLowerCase();

    if (
      backupState?.parent_level?.labelName?.toLowerCase() === defaultLabelName
    ) {
      retValue = backupState.parent_level;
    }

    const selectedTrend = JSON.parse(
      localStorage.getItem('selectedTrend') || 'null',
    );
    if (
      selectedTrend &&
      selectedTrend[0]?.labelName?.toLowerCase() === defaultLabelName &&
      selectedTrend[0]?.labelName?.toLowerCase() ===
        backupState?.parent_level?.toLowerCase()
    ) {
      retValue = selectedTrend[0];
    }

    if (retValue?.typeName !== this.service.getSelectedType()) {
      retValue = this.filters[0];
    }

    return retValue;
  }

  getImmediateParentDisplayName(child) {
    let completeHiearchyData = JSON.parse(
      localStorage.getItem('completeHierarchyData'),
    )[this.selectedType.toLowerCase()];
    let selectedLevelNode = completeHiearchyData?.filter(
      (x) => x.hierarchyLevelName === this.selectedLevel,
    );
    let level = selectedLevelNode[0].level;
    if (level > 1) {
      let parentLevel = level - 1;
      let parentLevelNode = completeHiearchyData?.filter(
        (x) => x.level === parentLevel,
      );
      let parentLevelName = parentLevelNode[0].hierarchyLevelName;

      let immediateParent = this.filterData[parentLevelName].find(
        (x) => x.nodeId === child.parentId,
      );
      return immediateParent?.nodeDisplayName;
    }
    return undefined;
  }

  preventDropdownClose(event: Event) {
    // event.stopPropagation();
    console.log('preventDropdownClose');
    this.preventClose = true;
  }

  handleBlur() {
    // Logic when panel loses focus, if needed
    this.preventClose = false; // Reset flag on blur
  }

  handlePanelHide() {
    if (this.preventClose) {
      this.preventClose = false; // Reset flag after handling
      return false; // Prevent closing
    }
    return true; // Allow closing
  }

  handleFooterKeydown(event: KeyboardEvent) {
    if (event.key === 'Tab') {
      this.preventClose = true; // Prevent close when tabbing within panel
    }
  }
}
