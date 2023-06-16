/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import { HttpService } from '../../services/http.service';
import { SharedService } from '../../services/shared.service';
import { MessageService } from 'primeng/api';
import { MultiSelect } from 'primeng/multiselect';

@Component({
  selector: 'app-project-filter',
  templateUrl: './project-filter.component.html',
  styleUrls: ['./project-filter.component.css']
})
export class ProjectFilterComponent implements OnInit {
  @Output() projectSelectedEvent = new EventEmitter<string>();
  data = <any>[];
  filteredData = <any>[];
  projects = <any>[];
  filtersApplied = <boolean>false;
  filters = {};
  hierarchyArray = [];
  resetDropdowns = <boolean>false;
  selectedValProjects = <any>[];
  hierarchyData = <any>{};
  selectedVal = <any>{};
  selectedHierarchy = <any>[];
  selectedValTemplateValue = <any>[];
  selectedValueIsStillThere: any = {};
  valueRemoved: any = {};
  constructor(private httpService: HttpService, private service: SharedService, private messageService: MessageService) { }

  ngOnInit(): void {
    this.getProjects();
  }

  // fetches all projects
  getProjects() {
    this.resetDropdowns = true;
    this.httpService.getAllProjects()
      .subscribe(projectsData => {
        if (projectsData[0] !== 'error' && !projectsData.error && projectsData?.data) {
          this.data = projectsData.data;
          this.hierarchyArray = this.data[0].hierarchy.map((elem) => elem.hierarchyLevel.hierarchyLevelId);

          this.service.sendProjectData(this.data);
          this.populateDataLists(projectsData.data, 'all');
        } else {
          // show error message
          this.messageService.add({ severity: 'error', summary: 'User needs to be assigned a project for the access to work on dashboards.' });
        }
      });
  }

  populateDataLists(data, filterType, projectFilter = null) {
    if (data.length) {
      if (data[0].hierarchy && data[0].hierarchy.length) {
        let selectedFilterValues = [];
        if (filterType !== 'all') {
          selectedFilterValues = this.hierarchyData[filterType];
        }

        this.hierarchyArray.forEach(h => {
          if (h !== filterType) {
            this.hierarchyData[h] = [];
          }
        });

        data.forEach(dataElem => {
          dataElem.hierarchy.forEach(hierarchyElem => {
            if (filterType === 'all') {
              if (!this.hierarchyData[hierarchyElem.hierarchyLevel.hierarchyLevelId] || !this.hierarchyData[hierarchyElem.hierarchyLevel.hierarchyLevelId].length) {
                this.hierarchyData[hierarchyElem.hierarchyLevel.hierarchyLevelId] = [];
              }

              this.hierarchyData[hierarchyElem.hierarchyLevel.hierarchyLevelId].push({
                name: hierarchyElem.value,
                code: hierarchyElem.value
              });
            } else {
              // if (this.hierarchyArray.indexOf(hierarchyElem.hierarchyLevel.hierarchyLevelId) === this.hierarchyArray.indexOf(filterType)) {
              //   this.fillLevel(hierarchyElem.hierarchyLevel.hierarchyLevelId);
              // } else {
              //   let selectedHierarchy = dataElem.hierarchy;
              //   selectedHierarchy.forEach((hier) => {
              //     if (!this.hierarchyData[hier.hierarchyLevel.hierarchyLevelId] || !this.hierarchyData[hier.hierarchyLevel.hierarchyLevelId].length) {
              //       this.hierarchyData[hier.hierarchyLevel.hierarchyLevelId] = [];
              //     }
              //     this.hierarchyData[hier.hierarchyLevel.hierarchyLevelId].push({
              //       name: hier.value,
              //       code: hier.value
              //     });
              //   });
              // }
              // }

              if (hierarchyElem.hierarchyLevel.hierarchyLevelId === filterType) {
                // do nothing
              } else {
                if (!this.hierarchyData[hierarchyElem.hierarchyLevel.hierarchyLevelId] || !this.hierarchyData[hierarchyElem.hierarchyLevel.hierarchyLevelId].length) {
                  this.hierarchyData[hierarchyElem.hierarchyLevel.hierarchyLevelId] = [];
                }

                this.hierarchyData[hierarchyElem.hierarchyLevel.hierarchyLevelId].push({
                  name: hierarchyElem.value,
                  code: hierarchyElem.value
                });
              }
            }
          });
        });
      }
      Object.keys(this.hierarchyData).forEach((key) => {
        this.hierarchyData[key] = this.findUniques(this.hierarchyData[key], ['name', 'code']);
      });
      if (!projectFilter) {
        this.projects = Object.assign([], data);
      }

      const dataIdMap = data.map((d) => d.id);
      const selectedValProjectsIdMap = this.selectedValProjects.map((d) => d.id);
      this.selectedValProjects = [];
      dataIdMap.forEach(element => {
        if (selectedValProjectsIdMap.includes(element)) {
          this.selectedValProjects.push(data.filter((d) => d.id === element));
        }
      });
    }
  }

  // fillLevelBefore(filterType) {
  //   this.hierarchyData[filterType] = [];
  //   this.filteredData.forEach(element => {
  //     let hier = element.hierarchy.map((h) => {
  //       return {
  //         level: h.hierarchyLevel.hierarchyLevelId,
  //         value: h.value
  //       }
  //     });
  //     let requiredHier = hier.filter(h => h.level === filterType);
  //     this.hierarchyData[filterType].push({
  //       name: requiredHier[0].value,
  //       code: requiredHier[0].value
  //     })
  //   });
  // }

  // fillLevel(filterType) {
  //   let data = JSON.parse(JSON.stringify(this.data));
  //   let self = this;
  //   Object.keys(this.selectedVal).forEach((key, index) => {
  //     if (index < Object.keys(self.selectedVal).length - 1) {
  //       this.fillLevelBefore(key);
  //       data = data.filter(d => {
  //         if (d.hierarchy.filter(h => h.hierarchyLevel.hierarchyLevelId === key).length) {
  //           if (self.selectedVal[key].map(v => v.code).includes(d.hierarchy.filter(h => h.hierarchyLevel.hierarchyLevelId === key)[0].value)) {
  //             return d;
  //           }
  //         }
  //       });
  //     }
  //   });
  //   this.hierarchyData[filterType] = [];
  //   data.forEach(element => {
  //     let hier = element.hierarchy.map((h) => {
  //       return {
  //         level: h.hierarchyLevel.hierarchyLevelId,
  //         value: h.value
  //       }
  //     });
  //     let requiredHier = hier.filter(h => h.level === filterType);
  //     this.hierarchyData[filterType].push({
  //       name: requiredHier[0].value,
  //       code: requiredHier[0].value
  //     })
  //   });
  // }

  findUniques(data, propertyArray) {
    const seen = Object.create(null);
    return data.filter(o => {
      const key = propertyArray.map(k => o[k]).join('|');
      if (!seen[key]) {
        seen[key] = true;
        return true;
      }
    }).map((proj) => {
      const obj = {};
      propertyArray.forEach(element => {
        obj[element] = proj[element];
      });
      return obj;
    });
  }

  filterData(event, filterType, filterValue) {
    this.valueRemoved = {};
    event.stopPropagation();
    this.filteredData = JSON.parse(JSON.stringify(this.data));
    if (!this.selectedVal[filterType]) {
      this.selectedVal[filterType] = [];
    }
    if (!this.selectedVal[filterType] || !this.selectedVal[filterType].filter(f => f.code === filterValue).length) {
      const obj = {
        name: filterValue,
        code: filterValue
      };
      this.selectedVal[filterType].push(obj);
    } else {
      this.valueRemoved['val'] = this.selectedVal[filterType].splice(this.selectedVal[filterType].indexOf(this.selectedVal[filterType].filter(f => f.code === filterValue)[0]), 1);
      if (!this.selectedVal[filterType].length) {
        delete this.selectedVal[filterType];
      }
    }

    this.sortFilters();
    let newFilteredData = [];

    if (Object.keys(this.selectedVal).length) {
      Object.keys(this.selectedVal).forEach((filter) => {
        if (this.selectedVal[filter] && this.selectedVal[filter].length) {
          this.selectedValTemplateValue[filter] = this.selectedVal[filter]?.map(s => s.code).join(', ');
          this.filteredData.forEach(proj => {
            if (proj.hierarchy.length) {
              if (this.hierarchyMatch(proj)) {
                newFilteredData.push(proj);
              }
            }
          });
        }
      });

      newFilteredData = this.findUniques(newFilteredData, ['id', 'projectName', 'hierarchy']);
      this.filteredData = newFilteredData;
      if (Object.keys(this.selectedVal).length) {
        this.filtersApplied = true;
      } else {
        this.filtersApplied = false;
      }

      this.populateDataLists(this.filteredData, filterType);

      // refine selectedVal as per the filtered data
      this.hierarchyArray.forEach((level) => {
        const levelData = this.hierarchyData[level].map((m) => m.code);
        if (this.selectedVal[level] && this.selectedVal[level].length) {
          const selectedLevelData = this.selectedVal[level].map((m) => m.code);
          selectedLevelData.forEach(element => {
            if (levelData.indexOf(element) === -1) {
              this.selectedVal[level] = this.selectedVal[level].filter((f) => f.code !== element);
            }
          });
        }
      });

      this.projectSelected();
    } else {
      this.clearFilters();
    }
  }

  hierarchyMatch(project) {
    let result = true;

    const hierarchy = project.hierarchy.reduce(function(a, b) {
      a[b.hierarchyLevel.hierarchyLevelId] = b.value;
      return a;
    }, {});
    const selectedVal = {};
    Object.keys(this.selectedVal).forEach((key) => {
      selectedVal[key] = this.selectedVal[key].map(i => i.code).flat();
    });

    Object.keys(selectedVal).every((val) => {
      if (!selectedVal[val].includes(hierarchy[val])) {
        result = false;
        return false;
      }
      return true;
    });

    return result;
  }

  sortFilters() {
    const sortedFilter = {};
    this.hierarchyArray.forEach((filterType) => {
      if (this.selectedVal[filterType]) {
        sortedFilter[filterType] = this.selectedVal[filterType];
      }
    });
    this.selectedVal = sortedFilter;
  }

  clearFilters() {
    this.valueRemoved['val']= JSON.parse(JSON.stringify(this.selectedVal));
    this.filtersApplied = false;
    Object.keys(this.hierarchyData).forEach((key) => {
      delete this.selectedVal[key];
    });
    this.selectedValProjects = [];
    this.projects = JSON.parse(JSON.stringify(this.data));
    this.populateDataLists(this.data, 'all');
    this.projectSelected();
  }

  projectSelected() {
    const obj: any = {};
    if (!this.selectedValProjects || !this.selectedValProjects.length) {
      this.hierarchyArray.forEach((hierarchy) => {
        if (this.selectedVal[hierarchy] && this.selectedVal[hierarchy].length) {
          obj['accessType'] = hierarchy;
          obj['value'] = [];
          const selectedHierarchyArr = this.selectedVal[hierarchy].map((item) => ({
              itemId: item.name,
              itemName: item.name
            }));
          obj['value'] = [...selectedHierarchyArr];
        }
      });

    } else {
      obj['accessType'] = 'project';
      obj['value'] = this.selectedValProjects.map((item) => ({
          itemId: item.id,
          itemName: item.projectName
        }));
    }
    obj['hierarchyArr'] = this.hierarchyArray;
    obj['valueRemoved'] = this.valueRemoved;
    this.projectSelectedEvent.emit(obj);
  }

  getSelectedValTemplateValue(hierarchyLevelId) {
    return this.selectedVal[hierarchyLevelId]?.map(s => s.code).join(', ');
  }
}
