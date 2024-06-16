import { Component, OnChanges, OnDestroy, OnInit } from '@angular/core';
import { MenuItem, MessageService } from 'primeng/api';
import { HttpService } from '../../services/http.service';
import { SharedService } from '../../services/shared.service';
const getDashConfData = require('../../../test/resource/boardConfigNew.json');
import { Router } from '@angular/router';

@Component({
  selector: 'app-nav-new',
  templateUrl: './nav-new.component.html',
  styleUrls: ['./nav-new.component.css']
})
export class NavNewComponent implements OnInit, OnDestroy {
  items: MenuItem[] | undefined;
  activeItem: MenuItem | undefined;
  selectedTab: string = '';
  selectedType: string = '';
  subscriptions: any[] = [];

  constructor(private httpService: HttpService, private sharedService: SharedService, private messageService: MessageService, private router: Router) {
  }

  ngOnInit(): void {
    const selectedTab = window.location.hash.substring(1);
    this.selectedTab = selectedTab?.split('/')[2] ? selectedTab?.split('/')[2] : 'iteration';
    
    this.subscriptions.push(this.sharedService.onTypeOrTabRefresh.subscribe((data) => {
      this.selectedType = data.selectedType ? data.selectedType : 'scrum';
      this.sharedService.setSelectedType(this.selectedType)
    }));

    this.selectedType = this.sharedService.getSelectedType() ? this.sharedService.getSelectedType() : 'scrum';
    // this.sharedService.setSelectedTypeOrTabRefresh(this.selectedTab, this.selectedType);
    this.getBoardConfig();
  }

  // unsubscribing all Kpi Request
  ngOnDestroy() {
    this.subscriptions.forEach(subscription => subscription.unsubscribe());
  }

  getBoardConfig() {
    this.httpService.getShowHideOnDashboard({ basicProjectConfigIds: [] }).subscribe(
      (response) => {
        if (response.success === true) {
          this.sharedService.setDashConfigData(getDashConfData.data);
          // this.service.setDashConfigData(response.data);
          // this.sharedService.setSelectedTypeOrTabRefresh(this.selectedTab, this.selectedType);
          this.items = response.data;
          this.items = [...getDashConfData.data['scrum'], ...getDashConfData.data['others']].map((obj, index) => {
            return {
              label: obj['boardName'],
              icon: index == 0 ? 'fas fa-pencil-alt' : '',
              slug: obj['boardSlug'],
              command: () => {
                this.selectedTab = obj['boardSlug'];
                if (this.selectedTab !== 'unauthorized access') {
                  if (obj['boardName'].toLowerCase() === 'kpi maturity') {
                    setTimeout(() => {
                      this.sharedService.setDashConfigData(getDashConfData.data);
                    }, 100);
                  }
                  setTimeout(() => {
                    this.sharedService.setSelectedTypeOrTabRefresh(this.selectedTab, this.selectedType);
                  }, 100);
                }
                this.router.navigate(['/dashboard/' + obj['boardSlug']]);
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

}
