
import { Component, OnDestroy, OnInit } from '@angular/core';
import { MessageService } from 'primeng/api';
import { Router } from '@angular/router';
import { HttpService } from 'src/app/core/services/http.service';
import { SharedService } from 'src/app/core/services/shared.service';
import { HelperService } from 'src/app/core/services/helper.service';

@Component({
  selector: 'app-nav-new',
  templateUrl: './nav-new.component.html',
  styleUrls: ['./nav-new.component.css']
})
export class NavNewComponent implements OnInit, OnDestroy {
  items: any;
  activeItem: any;
  selectedTab: string = '';
  selectedType: string = '';
  subscriptions: any[] = [];
  dashConfigData: any;
  selectedBasicConfigIds: any[] = [];

  constructor(private httpService: HttpService, public sharedService: SharedService, public messageService: MessageService, public router: Router, public helperService: HelperService) {
  }

  ngOnInit(): void {
    const selectedTab = window.location.hash.substring(1);
    this.selectedTab = selectedTab?.split('/')[2] ? selectedTab?.split('/')[2] : 'iteration';
    this.selectedTab = this.selectedTab?.split(' ').join('-').toLowerCase();
    
    this.sharedService.setSelectedBoard(this.selectedTab);
    this.selectedType = this.sharedService.getSelectedType() ? this.sharedService.getSelectedType() : 'scrum';
    this.sharedService.setScrumKanban(this.selectedType);

    if (this.sharedService.getSelectedTrends() && this.sharedService.getSelectedTrends()[0]) {
      this.getBoardConfig([...this.sharedService.getSelectedTrends().map(proj => proj['basicProjectConfigId'])]);
    } else {
      this.getBoardConfig([]);
    }
  }

  // unsubscribing all Kpi Request
  ngOnDestroy() {
    this.subscriptions.forEach(subscription => subscription.unsubscribe());
  }

  getBoardConfig(projectList) {
    this.httpService.getShowHideOnDashboardNewUI({ basicProjectConfigIds: projectList?.length && projectList[0] ? projectList : [] }).subscribe(
      (response) => {
        this.setBoards(response);
      },
      (error) => {
        this.messageService.add({
          severity: 'error',
          summary: error.message,
        });
      },
    );
  }

  setBoards(response) {
    if (response.success === true) {
      let data = response.data.userBoardConfigDTO;
      if (JSON.parse(localStorage.getItem('completeHierarchyData'))) {
        const levelDetails = JSON.parse(localStorage.getItem('completeHierarchyData'))[this.selectedType];
        data[this.selectedType]?.forEach((board) => {
          if (board?.filters) {
            if (levelDetails.filter(level => level.hierarchyLevelId.toLowerCase() === board.filters.primaryFilter.defaultLevel.labelName.toLowerCase())[0]) {
              board.filters.primaryFilter.defaultLevel.labelName = levelDetails.filter(level => level.hierarchyLevelId.toLowerCase() === board.filters.primaryFilter.defaultLevel.labelName.toLowerCase())[0].hierarchyLevelName;
            }
            if (board.filters.parentFilter && board.filters.parentFilter.labelName !== 'Organization Level') {
              board.filters.parentFilter.labelName = levelDetails.filter(level => level.hierarchyLevelId === board.filters.parentFilter.labelName.toLowerCase())[0]?.hierarchyLevelName;
            }
            if (board.filters.parentFilter?.emittedLevel) {
              board.filters.parentFilter.emittedLevel = levelDetails.filter(level => level.hierarchyLevelId === board.filters.parentFilter.emittedLevel)[0]?.hierarchyLevelName;
            }

            if (board.boardSlug !== 'developer') {
              board.filters.additionalFilters?.forEach(element => {
                if (levelDetails.filter(level => level.hierarchyLevelId === element.defaultLevel.labelName)[0]) {
                  element.defaultLevel.labelName = levelDetails.filter(level => level.hierarchyLevelId === element.defaultLevel.labelName)[0].hierarchyLevelName;
                }
              });
            }
          }
        });

        data['others'].forEach((board) => {
          if (board?.filters) {
            board.filters.primaryFilter.defaultLevel.labelName = levelDetails.filter(level => level.hierarchyLevelId === board.filters.primaryFilter.defaultLevel.labelName)[0].hierarchyLevelName;
            if (board.filters.parentFilter && board.filters.parentFilter.labelName !== 'Organization Level') {
              board.filters.parentFilter.labelName = levelDetails.filter(level => level.hierarchyLevelId === board.filters.parentFilter.labelName.toLowerCase())[0].hierarchyLevelName;
            }
            if (board.filters.parentFilter?.emittedLevel) {
              board.filters.parentFilter.emittedLevel = levelDetails.filter(level => level.hierarchyLevelId === board.filters.parentFilter.emittedLevel)[0].hierarchyLevelName;
            }
          }
        });
        data['configDetails'] = response.data.configDetails;
        if (!this.deepEqual(this.dashConfigData, data)) {
          // this.sharedService.setDashConfigData(data);
          this.dashConfigData = data;
        }

        this.items = [...this.dashConfigData['scrum'], ...this.dashConfigData['others']].map((obj) => {
          return {
            label: obj['boardName'],
            slug: obj['boardSlug'],
            command: () => {
              this.handleMenuTabFunctionality(obj)
            },
          };
        });
        this.activeItem = this.items?.filter((x) => x['slug'] == this.selectedTab?.toLowerCase())[0];
      } else {
        this.httpService.getAllHierarchyLevels().subscribe((res) => {
          if (res.data) {
            localStorage.setItem('completeHierarchyData', JSON.stringify(res.data));
            this.setBoards(response);
          }
        });
      }
    }
  }

  handleMenuTabFunctionality(obj) {
    this.selectedTab = obj['boardSlug'];
    if (this.selectedTab !== 'unauthorized access') {
      // this.sharedService.setSelectedTypeOrTabRefresh(this.selectedTab, this.selectedType);
      this.sharedService.setSelectedBoard(this.selectedTab);
    }
    if (this.selectedTab === 'iteration' || this.selectedTab === 'release' || this.selectedTab === 'backlog'
      || this.selectedTab === 'dora' || this.selectedTab === 'kpi-maturity') {
      this.helperService.setBackupOfFilterSelectionState({ 'additional_level': null });
    }
    this.router.navigate(['/dashboard/' + obj['boardSlug']]);
  }

  deepEqual(obj1: any, obj2: any): boolean {
    if (obj1 === obj2) {
      return true;
    }

    if (obj1 === null || obj2 === null || typeof obj1 !== 'object' || typeof obj2 !== 'object') {
      return false;
    }

    const keys1 = Object.keys(obj1);
    const keys2 = Object.keys(obj2);

    if (keys1.length !== keys2.length) {
      return false;
    }

    for (const key of keys1) {
      if (!keys2.includes(key) || !this.deepEqual(obj1[key], obj2[key])) {
        return false;
      }
    }

    return true;
  }
}
