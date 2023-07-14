import { Component, OnInit } from '@angular/core';
import { MessageService } from 'primeng/api';
import { UserAccessApprovalDTO, UserAccessApprovalResponseDTO } from 'src/app/model/userAccessApprovalDTO.model';
import { HttpService } from 'src/app/services/http.service';
import { SharedService } from 'src/app/services/shared.service';

@Component({
  selector: 'app-view-new-user-auth-request',
  templateUrl: './view-new-user-auth-request.component.html',
  styleUrls: ['./view-new-user-auth-request.component.css'],
})
export class ViewNewUserAuthRequestComponent implements OnInit {
  newUserAccessRequestData: UserAccessApprovalDTO[] = [];
  showLoader = true;

  constructor(
    private readonly httpService: HttpService,
    private readonly messageService: MessageService,
    private readonly sharedService: SharedService
  ) {}

  ngOnInit(): void {
    this.httpService.getNewUserAccessRequestFromAPI().subscribe((userData: UserAccessApprovalResponseDTO) => {
      console.log(userData);
      this.showLoader = false;
      if (userData?.success) {
        this.newUserAccessRequestData = [...userData?.data];
      } else {
        this.messageService.add({
          severity: 'error',
          summary: 'Error in fetching requests. Please try after some time.',
        });
      }
    });
  }

  updateRequestStatus(req: any, approvalStatus: boolean) {
    console.log(req);
    this.httpService
      .updateNewUserAccessRequest(
        {
          status: approvalStatus ? 'Approved' : 'Rejected',
          role: 'ROLE_PROJECT_ADMIN',
          message: '',
        },
        req.username,
      )
      .subscribe((data) => {
        this.showLoader = false;
        console.log(data);
        if(data.success) {
          this.messageService.add({
            severity: approvalStatus ? 'success' : 'error',
            summary: data.message,
          });

          this.newUserAccessRequestData = this.newUserAccessRequestData.filter(userData => userData.username !== req.username);
          this.sharedService.notificationUpdate();

        } else {
          this.messageService.add({
            severity: 'error',
            summary: 'Something went wrong',
          });
        }
      });
  }
}
