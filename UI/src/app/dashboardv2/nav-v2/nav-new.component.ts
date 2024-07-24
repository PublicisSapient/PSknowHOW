
import { Component, OnChanges, OnDestroy, OnInit } from '@angular/core';
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

  constructor(private httpService: HttpService, public sharedService: SharedService, public messageService: MessageService, public router: Router, private helperService: HelperService) {
  }

  ngOnInit(): void {
    const selectedTab = window.location.hash.substring(1);
    this.selectedTab = selectedTab?.split('/')[2] ? selectedTab?.split('/')[2] : 'iteration';

    this.subscriptions.push(this.sharedService.onTypeOrTabRefresh.subscribe((data) => {
      this.selectedType = data.selectedType ? data.selectedType : 'scrum';
      this.sharedService.setSelectedType(this.selectedType)
    }));

    this.selectedType = this.sharedService.getSelectedType() ? this.sharedService.getSelectedType() : 'scrum';
    this.sharedService.setSelectedTypeOrTabRefresh(this.selectedTab, this.selectedType);
    this.getBoardConfig([...this.sharedService.getSelectedTrends().map(proj => proj['basicProjectConfigId'])]);

    this.subscriptions.push(this.sharedService.selectedTrendsEvent.subscribe((data) => {
      this.getBoardConfig(data.map(proj => proj['basicProjectConfigId']))
    }));
  }

  // unsubscribing all Kpi Request
  ngOnDestroy() {
    this.subscriptions.forEach(subscription => subscription.unsubscribe());
  }

  getBoardConfig(projectList) {
    this.httpService.getShowHideOnDashboardNewUI({ basicProjectConfigIds: projectList?.length && projectList[0] ? projectList : [] }).subscribe(
      (response) => {
        if (response.success === true) {
          let data = response.data.userBoardConfigDTO;
          data['configDetails'] = response.data.configDetails;
          if (!this.deepEqual(this.dashConfigData, data)) {
            this.sharedService.setDashConfigData(data);
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
        }
      },
      (error) => {
        this.messageService.add({
          severity: 'error',
          summary: error.message,
        });
      },
    );
  }

  handleMenuTabFunctionality(obj) {
    this.selectedTab = obj['boardSlug'];
    if (this.selectedTab !== 'unauthorized access') {
      this.sharedService.setSelectedTypeOrTabRefresh(this.selectedTab, this.selectedType);
    }
    if (this.selectedTab === 'iteration' || this.selectedTab === 'release' || this.selectedTab === 'backlog'
      || this.selectedTab === 'dora' || this.selectedTab === 'Maturity') {
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
