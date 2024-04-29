import { Component, OnInit } from '@angular/core';
import { HttpService } from '../../services/http.service';
import { first } from 'rxjs/operators';
import { MenuItem } from 'primeng/api';
import { SharedService } from 'src/app/services/shared.service';
import { GetAuthorizationService } from 'src/app/services/get-authorization.service';
import { Router } from '@angular/router';
import { HelperService } from 'src/app/services/helper.service';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit {
  clientLogo: string = '';
  notificationCount: number = 0;
  notificationList: Array<object> = [];
  commentCount: number = 0;
  commentList: Array<object> = [];
  items: MenuItem[] | undefined;
  activeItem: MenuItem | undefined;
  userDetails: object = {};
  userMenuItems: MenuItem[] | undefined;

  constructor(
    private httpService: HttpService,
    private sharedService: SharedService,
    private getAuthorizationService: GetAuthorizationService,
    private router: Router,
    private helperService: HelperService) { }

  ngOnInit(): void {
    this.getLogoImage();
    this.getNotification();
    this.items = [
      { label: 'Dashboard', icon: '' },
    ];
    this.activeItem = this.items[0];

    this.userDetails = this.sharedService.getCurrentUserDetails();

    this.userMenuItems = [
      {
        label: 'Settings', 
        icon: 'fas fa-cog', 
        command: () => {
          this.router.navigate(['/dashboard/Config/ProjectList']);
        },
      },
      {
        label: 'Logout', 
        icon: 'fas fa-sign-out-alt', 
        command: () => {
          this.logout();
        }
      },
    ]
  }

  /*Rendered the logo image */
  getLogoImage() {
    this.httpService.getUploadedImage().pipe(first()).subscribe((data) => {
      if (data['image']) {
        this.clientLogo = 'data:image/png;base64,' + data['image'];
      } else {
        this.clientLogo = undefined;
      }
    });
  }

  // when user would want to give access on project from notification list
  routeForAccess(type: string) {
    if (this.getAuthorizationService.checkIfSuperUser() || this.getAuthorizationService.checkIfProjectAdmin()) {
      // this.isAdmin = true;
      switch (type) {
        case 'Project Access Request':
          // this.service.setSideNav(false);
          this.router.navigate(['/dashboard/Config/Profile/GrantRequests']);
          break;
        case 'User Access Request':
          // this.service.setSideNav(false);
          this.router.navigate(['/dashboard/Config/Profile/GrantNewUserAuthRequests']);
          break;
        default:
      }
    } else {
      this.router.navigate(['/dashboard/Config/Profile/RequestStatus']);
      // this.isAdmin = false;
    }
  }

  navigateToHomePage() {
    const previousSelectedTab = this.router.url.split('/')[2];
    if (previousSelectedTab === 'Config' || previousSelectedTab === 'Help') {
      this.sharedService.setEmptyFilter();
      this.sharedService.setSelectedType('scrum');
      this.router.navigateByUrl(`/dashboard/iteration`);
    }
  }

  getNotification() {
    const response = {
      data: [
        {
          "type": "User Access Request",
          "count": 1
        },
        {
          "type": "Project Access Request",
          "count": 0
        }
      ]
    }
    this.notificationList = [...response.data].map((obj) => {
      this.notificationCount = this.notificationCount + obj.count;
      return {
        label: obj.type + ' : ' + obj.count,
        icon: '',
        command: () => {
          this.routeForAccess(obj.type);
        },
      };
    });
    console.log(this.notificationList);

    // this.notificationCount = 0;
    // this.httpService.getAccessRequestsNotifications().subscribe((response: NotificationResponseDTO) => {
    //   if (response && response.success) {
    //     if (response.data?.length) {
    //       this.notificationList = [...response.data].map((obj) => {
    //         this.notificationCount = this.notificationCount + obj.count;
    //         return {
    //           label: obj.type + ' : ' + obj.count,
    //           icon: '',
    //           command: () => {
    //             this.routeForAccess(obj.type);
    //           },
    //         };
    //       });
    //     }
    //   } else {
    //     this.messageService.add({
    //       severity: 'error',
    //       summary: 'Error in fetching requests. Please try after some time.',
    //     });
    //   }
    // });
  }

  // logout is clicked  and removing auth token , username
  logout() {
    this.httpService.logout().subscribe((getData) => {
      if (!(getData !== null && getData[0] === 'error')) {
        localStorage.clear();
        this.helperService.isKanban = false;
        this.sharedService.setSelectedProject(null);
        this.sharedService.setCurrentUserDetails({});
        this.router.navigate(['./authentication/login']);
      }
    });
  }
}
