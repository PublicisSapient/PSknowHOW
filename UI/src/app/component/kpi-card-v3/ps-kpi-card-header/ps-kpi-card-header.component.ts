import { Component, Input, OnInit, ViewChild } from '@angular/core';
import { MenuItem } from 'primeng/api';
import { Menu } from 'primeng/menu';

@Component({
  selector: 'app-ps-kpi-card-header',
  templateUrl: './ps-kpi-card-header.component.html',
  styleUrls: ['./ps-kpi-card-header.component.css']
})
export class PsKpiCardHeaderComponent implements OnInit {
  @Input() cardHeaderData: any;
  isTooltip = false;
  @ViewChild('kpimenu') kpimenu: Menu;
  menuItems: MenuItem[] | undefined;
  warning = '';
  constructor() { }

  ngOnInit(): void {
    console.log(this.cardHeaderData);
    this.initializeMenu()
  }

  showTooltip(val) {
    this.isTooltip = val;
  }

  toggleMenu(event) {
    this.kpimenu.toggle(event);
  }

  initializeMenu() {
    this.menuItems = [
      {
        label: 'Settings',
        icon: 'fas fa-cog',
        // command: () => {
        //   this.onOpenFieldMappingDialog();
        // },
        // disabled: this.disableSettings || this.service.getSelectedType()?.toLowerCase() === 'kanban'
      },
      {
        label: 'List View',
        icon: 'pi pi-align-justify',
        // command: ($event) => {
        //   this.prepareData();
        // },
        // disabled: this.selectedTab === 'release' || this.selectedTab === 'backlog'
      },
      {
        label: 'Explore',
        icon: 'pi pi-table',
        // command: () => {
        //   this.exportToExcel();
        // },
        // disabled: !this.kpiData.kpiDetail.chartType
      },
      {
        label: 'Comments',
        icon: 'pi pi-comments',
        // command: ($event) => {
        //   this.showComments = true;
        //   this.openCommentModal();
        // },
      }
    ];
  }

  showWarning(val) {
    if (val) {
      this.warning = 'Configure the missing mandatory field mappings in KPI Settings for accurate data display.';
    } else {
      this.warning = null;
    }
  }

}
