import { Component, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';
import { MenuItem } from 'primeng/api';
import { Menu } from 'primeng/menu';
import { KpiHelperService } from 'src/app/services/kpi-helper.service';
import { KPI_HEADER_ACTION } from '../../../model/Constants'
import { SharedService } from 'src/app/services/shared.service';
import { GetAuthorizationService } from 'src/app/services/get-authorization.service';
import { FeatureFlagsService } from 'src/app/services/feature-toggle.service';

@Component({
  selector: 'app-ps-kpi-card-header',
  templateUrl: './ps-kpi-card-header.component.html',
  styleUrls: ['./ps-kpi-card-header.component.css']
})
export class PsKpiCardHeaderComponent implements OnInit {
  @Input() cardHeaderData: any;
  isTooltip = false;
  @Output() actionTriggered = new EventEmitter<any>();
  @ViewChild('kpimenu') kpimenu: Menu;
  menuItems: MenuItem[] | undefined;
  warning = '';
  MenuValues = KPI_HEADER_ACTION;
  disableSettings: boolean = false;
  userRole: string;
  checkIfViewer: boolean;
  reportModuleEnabled: boolean = false;
  constructor(private kpiHelperService: KpiHelperService, public service: SharedService, private authService: GetAuthorizationService, private featureFlagService: FeatureFlagsService) { }

  ngOnInit(): void {
    this.initializeMenu();

    this.userRole = this.authService.getRole();
    this.checkIfViewer = (this.authService.checkIfViewer({ id: this.service.getSelectedTrends()[0]?.basicProjectConfigId }));
    this.disableSettings = (this.service.getSelectedTab().toLowerCase() !== 'iteration' && this.service.getSelectedTab().toLowerCase() !== 'release') || this.checkIfViewer || !['superAdmin', 'projectAdmin'].includes(this.userRole);
    this.initializeMenu();
  }

  showTooltip(val) {
    this.isTooltip = val;
  }

  toggleMenu(event) {
    this.kpimenu.toggle(event);
  }

  async initializeMenu() {
    this.menuItems = [
      {
        label: 'Settings',
        icon: 'fas fa-cog',
        command: () => {
          this.actionTriggered.emit({ ...this.MenuValues, setting: true });
        },
        disabled: this.disableSettings || this.service.getSelectedType()?.toLowerCase() === 'kanban'
      },
      // {
      //   label: 'List View',
      //   icon: 'pi pi-align-justify',
      //   command: ($event) => {
      //     // this.prepareData();
      //     this.actionTriggered.emit({...this.MenuValues,listView:true});
      //   },
      //   disabled: this.selectedTab === 'iteration'
      // },
      {
        label: 'Explore',
        icon: 'pi pi-table',
        command: ($event) => {
          // this.exportToExcel(); modalHeads
          const id = $event?.originalEvent?.target?.parentElement?.parentElement?.parentElement?.parentElement?.id.substring(5);
          this.actionTriggered.emit({ ...this.MenuValues, explore: true, kpiId: id });
        },
        // disabled: !this.kpiData.kpiDetail.chartType
      },
      {
        label: 'Comments',
        icon: 'pi pi-comments',
        command: ($event) => {
          // this.showComments = true;
          // this.openCommentModal();
          this.actionTriggered.emit({ ...this.MenuValues, comment: true });
        },
      }
    ];

    this.reportModuleEnabled = await this.featureFlagService.isFeatureEnabled('REPORTS');
    let alreadyAdded = this.menuItems.find(x => x.label === 'Add to Report');
    if (this.reportModuleEnabled && !alreadyAdded) {
      this.menuItems.push({
        label: 'Add to Report',
        icon: 'pi pi-briefcase',
        command: ($event) => {
          this.addToReport();
        },
      });
    }
  }

  showWarning(val) {
    if (val) {
      this.warning = 'Configure the missing mandatory field mappings in KPI Settings for accurate data display.';
    } else {
      this.warning = null;
    }
  }

  addToReport() {
    this.actionTriggered.emit({ ...this.MenuValues, report: true });
  }

}
