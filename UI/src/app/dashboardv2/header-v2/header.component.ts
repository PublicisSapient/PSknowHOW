import { Component, OnInit } from '@angular/core';
import { HttpService } from '../../services/http.service';
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
  notificationCount: number = 0;
  notificationList: Array<object> = [];
  commentCount: number = 0;
  commentList: Array<object> = [];
  items: MenuItem[] | undefined;
  activeItem: MenuItem | undefined;
  userDetails: object = {};
  userMenuItems: MenuItem[] | undefined;
  backToDashboardLoader: boolean = false;
  kpiListDataProjectLevel: any = {};
  kpiListData: any = {};
  lastVisitedFromUrl: string = '';
  ifSuperUser: boolean = false;
  ifProjectAdmin: boolean = false;

  constructor(
    private httpService: HttpService,
    public sharedService: SharedService,
    private getAuthorizationService: GetAuthorizationService,
    public router: Router,
    private helperService: HelperService) { }

  ngOnInit(): void {
    this.getNotification();
    this.items = [
      { label: 'Dashboard', icon: '' },
    ];
    this.activeItem = this.items[0];

    this.userDetails = this.sharedService.getCurrentUserDetails();
    this.ifSuperUser = this.getAuthorizationService.checkIfSuperUser();
    this.ifProjectAdmin = this.getAuthorizationService.checkIfProjectAdmin();
    this.userMenuItems = [
      {
        label: 'Settings',
        icon: 'fas fa-cog',
        command: () => {
          this.lastVisitedFromUrl = window.location.hash.substring(1);
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

  // when user would want to give access on project from notification list
  routeForAccess(type: string) {
    if (this.ifSuperUser || this.ifProjectAdmin) {
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
    }
  }

  /** when user clicks on Back to dashboard */
  navigateToDashboard() {
    this.backToDashboardLoader = true;
    this.router.navigateByUrl(this.lastVisitedFromUrl);
    this.backToDashboardLoader = false;
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
