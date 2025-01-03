
import { Component, OnDestroy, OnInit } from '@angular/core';
import { MessageService } from 'primeng/api';
import { HttpService } from '../../services/http.service';
import { SharedService } from '../../services/shared.service';
import { HelperService } from 'src/app/services/helper.service';
import { Router } from '@angular/router';


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
  dummyData = require('../../../test/resource/board-config-PSKnowHOW.json'); 

  constructor(public httpService: HttpService, public sharedService: SharedService, public messageService: MessageService, public router: Router, public helperService: HelperService) {
  }

  ngOnInit(): void {
    this.selectedType = this.sharedService.getSelectedType() ? this.sharedService.getSelectedType() : 'scrum';
    this.sharedService.setScrumKanban(this.selectedType);

    if (this.sharedService.getSelectedTrends() && this.sharedService.getSelectedTrends()[0]) {
      this.getBoardConfig([...this.sharedService.getSelectedTrends().map(proj => proj['basicProjectConfigId'])]);
    } else {
      this.getBoardConfig([]);
    }

    this.sharedService.onTabSwitch
      .subscribe(data => {
        this.selectedTab = data.selectedBoard;
      });
  }

  // unsubscribing all Kpi Request
  ngOnDestroy() {
    this.subscriptions.forEach(subscription => subscription.unsubscribe());
  }

  getBoardConfig(projectList) {
    this.httpService.getShowHideOnDashboardNewUI({ basicProjectConfigIds: projectList?.length && projectList[0] ? projectList : [] }).subscribe(
      (response) => {
        
        this.setBoards(response);
        // this.setBoards(this.dummyData);
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
        if (!this.helperService.deepEqual(this.dashConfigData, data)) {
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
}
