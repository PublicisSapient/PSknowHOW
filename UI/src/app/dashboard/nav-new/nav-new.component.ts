import { Component, OnInit } from '@angular/core';
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
export class NavNewComponent implements OnInit {
  items: MenuItem[] | undefined;

  activeItem: MenuItem | undefined;

  constructor(private httpService: HttpService, private sharedService: SharedService, private messageService: MessageService, private router: Router) { }

  ngOnInit(): void {
    this.getBoardConfig();
  }

  getBoardConfig(){
    // const data = {"basicProjectConfigIds":["ASO Mobile App_64a4fab01734471c30843fda"]}
    // console.log(this.sharedService.getSelectedProject());
    
    this.httpService.getShowHideOnDashboard({basicProjectConfigIds : []}).subscribe(
      (response) => {
        if (response.success === true) {
          // this.items = response.data;
          this.items = [...getDashConfData.data['scrum'], ...getDashConfData.data['others']].map((obj, index) => {

            return {
              label: obj['boardName'],
              icon: index == 0 ? '<i class="fa-solid fa-pencil" (click)="editMyDashboard()"></i>' : '',
              command: () => {
                this.router.navigate(['/dashboard/'+obj['boardSlug']]);
              },
            };
          });
          // this.service.setDashConfigData(response.data);
          this.sharedService.setDashConfigData(getDashConfData.data);
          // this.processKPIListData();
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
