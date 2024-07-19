
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
    this.getBoardConfig();
  }

  // unsubscribing all Kpi Request
  ngOnDestroy() {
    this.subscriptions.forEach(subscription => subscription.unsubscribe());
  }

  getBoardConfig() {
    this.httpService.getShowHideOnDashboardNewUI({ basicProjectConfigIds: [] }).subscribe(
      (response) => {
        if (response.success === true) {
          let data = response.data.userBoardConfigDTO;
          data['configDetails'] = response.data.configDetails;
          this.sharedService.setDashConfigData(data);
          this.dashConfigData = data;
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
    if(this.selectedTab === 'iteration' || this.selectedTab === 'release' || this.selectedTab === 'backlog'
       || this.selectedTab === 'dora' || this.selectedTab === 'Maturity') {
      this.helperService.setBackupOfFilterSelectionState({ 'additional_level': null });
    }
    this.router.navigate(['/dashboard/' + obj['boardSlug']]);
  }

}
