import { Component, OnInit } from '@angular/core';
import { MenuItem } from 'primeng/api';
import { Router } from '@angular/router';
import { environment } from 'src/environments/environment';
import { GetAuthorizationService } from 'src/app/core/services/get-authorization.service';
import { SharedService } from 'src/app/core/services/shared.service';
import { HttpService } from 'src/app/core/services/http.service';
import { HelperService } from 'src/app/core/services/helper.service';

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
  isGuest: boolean = false;
  appList: MenuItem[] | undefined;
  ssoLogin = environment.SSO_LOGIN;
  auth_service = environment.AUTHENTICATION_SERVICE;
  isSpeedSuite = environment?.['SPEED_SUITE'] ? environment?.['SPEED_SUITE'] : false;
  userRole: string = '';
  noToolsConfigured: boolean;

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
        label: 'Logout',
        icon: 'fas fa-sign-out-alt',
        command: () => {
          this.logout();
        }
      },
    ]
    let authoritiesArr;
    if (this.sharedService.getCurrentUserDetails('authorities')) {
      authoritiesArr = this.sharedService.getCurrentUserDetails('authorities');
    }
    if (authoritiesArr && authoritiesArr.includes('ROLE_GUEST')) {
      this.isGuest = true;
    }

    if (!this.isGuest) {
      this.userMenuItems.unshift({
        label: 'Settings',
        icon: 'fas fa-cog',
        command: () => {
          this.lastVisitedFromUrl = window.location.hash.substring(1);
          this.router.navigate(['/dashboard/Config/ProjectList']);
        },
      });

      this.httpService.getAllConnections().subscribe(response => {
        if (response['data'].length < 1) {
          this.noToolsConfigured = true;
        }
      });
    }

    if (!this.ssoLogin) {

      this.appList = [
        {
          label: 'KnowHOW',
          icon: '',
          styleClass: 'p-menuitem-link-active'
        },
        {
          label: 'Assessments',
          icon: '',
          command: () => {
            window.open(
              environment['MAP_URL'],
              '_blank'
            );
          }
        },
        {
          label: 'Retros',
          icon: '',
          command: () => {
            window.open(
              environment['RETROS_URL'],
              '_blank'
            );
          }
        }
      ];
    }
    this.sharedService.passEventToNav.subscribe(() => {
      this.getNotification();
    })
  }

  // when user would want to give access on project from notification list
  routeForAccess(type: string) {
    if (this.ifSuperUser || this.ifProjectAdmin) {
      switch (type) {
        case 'Project Access Request':
          this.router.navigate(['/dashboard/Config/Profile/GrantRequests']);
          break;
        case 'User Access Request':
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
    this.notificationCount = 0;
    this.httpService.getAccessRequestsNotifications().subscribe((response) => {
      if (response && response.success) {
        if (response.data?.length) {
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
      }
    });
  }

  // logout is clicked  and removing auth token , username
  logout() {
    this.helperService.logoutHttp();
  }

  navigateToMyKnowHOW() {
    const previousSelectedTab = this.router.url.split('/')[2];
    if (previousSelectedTab === 'Config' || previousSelectedTab === 'Help') {
      this.router.navigate([`/dashboard/my-knowhow`]);
    }
  }
}