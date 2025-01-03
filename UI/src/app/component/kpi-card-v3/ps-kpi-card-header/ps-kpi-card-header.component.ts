import { Component, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';
import { MenuItem } from 'primeng/api';
import { Menu } from 'primeng/menu';
import { KpiHelperService } from 'src/app/services/kpi-helper.service';
import {KPI_HEADER_ACTION} from '../../../model/Constants'

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
  constructor(private kpiHelperService:KpiHelperService) { }

  ngOnInit(): void {
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
        command: () => {
          this.actionTriggered.emit({...this.MenuValues,setting:true});
        },
        // disabled: this.disableSettings || this.service.getSelectedType()?.toLowerCase() === 'kanban'
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
        command: () => {
          // this.exportToExcel(); modalHeads
          this.actionTriggered.emit({...this.MenuValues,explore:true});
        },
        // disabled: !this.kpiData.kpiDetail.chartType
      },
      {
        label: 'Comments',
        icon: 'pi pi-comments',
        command: ($event) => {
          // this.showComments = true;
          // this.openCommentModal();
          this.actionTriggered.emit({...this.MenuValues,comment:true});
        },
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
