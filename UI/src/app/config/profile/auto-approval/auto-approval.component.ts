import { Component, OnInit } from '@angular/core';
import { HttpService } from '../../../services/http.service';
import { MessageService } from 'primeng/api';
import { UntypedFormGroup, Validators, UntypedFormControl } from '@angular/forms';

@Component({
  selector: 'app-auto-approval',
  templateUrl: './auto-approval.component.html',
  styleUrls: ['./auto-approval.component.css']
})
export class AutoApprovalComponent implements OnInit {
  rolesData: Array<object> = [];
  autoApprovalForm: UntypedFormGroup;
  autoApprovedId = '';
  constructor(private httpService: HttpService, private messageService: MessageService) { }

  ngOnInit(): void {
    this.getAutoApprovedRoles();
    this.autoApprovalForm = new UntypedFormGroup({
      enableAutoApprove: new UntypedFormControl(false, [Validators.required]),
      roles: new UntypedFormControl([]),
    });
    this.getRolesList();
  }

  get autoApprovalFormValue() {
    return this.autoApprovalForm.controls;
  }


  getRolesList() {
    this.httpService.getRolesList()
      .subscribe(roles => {
        if (roles.data && roles.data.length > 0) {
          this.rolesData = roles.data;
        } else {
          // show error message
          this.messageService.add({ severity: 'error', summary: 'Error in fetching roles. Please try after some time.' });
        }
      });
  }

  handleChange(event){
    if(!event.checked){
      this.autoApprovalFormValue['roles'].setValue([]);
    }else{
      this.autoApprovalFormValue['roles'].setValidators([Validators.required]);
    }
  }

  getAutoApprovedRoles() {
    this.httpService.getAutoApprovedRoleList().subscribe((response) => {
      if (response && response['success']) {
        this.autoApprovedId = response.data[0].id;
        const selectedValues = response.data[0];
        this.autoApprovalForm.controls['enableAutoApprove'].setValue(JSON.parse(selectedValues['enableAutoApprove']));
        const selectedRolesName = selectedValues['roles'].map(({ roleName }) => roleName);
        this.autoApprovalForm.controls['roles'].setValue(selectedRolesName);
      }
    }, errorResponse => {
      const error = errorResponse['error'];
      const msg = error['message'] || 'Some error occurred. Please try again later.';
      this.messageService.add({
        severity: 'error',
        summary: msg
      });
    });
  }

  onSubmit() {
    const submitData = {
      roles: []
    };
    submitData['enableAutoApprove'] = this.autoApprovalFormValue['enableAutoApprove'].value;
    if (submitData['enableAutoApprove']) {
      for (let i = 0; i < this.rolesData.length; i++) {
        for (let j = 0; j < this.autoApprovalFormValue['roles'].value.length; j++) {
          if (this.rolesData[i]['roleName'] == this.autoApprovalFormValue['roles'].value[j]) {
            submitData['roles'].push(this.rolesData[i]);
          }
        }
      }
    }


    if (this.autoApprovedId) {
      submitData['id'] = this.autoApprovedId;
    }
    this.httpService.submitAutoApproveData(submitData).subscribe((response) => {
      if (response && response['success']) {
        this.messageService.add({
          severity: 'success',
          summary: 'Added new auto approve role',
        });
      }
    }, errorResponse => {
      const error = errorResponse['error'];
      const msg = error['message'] || 'Some error occurred. Please try again later.';
      this.messageService.add({
        severity: 'error',
        summary: msg
      });
    });
  }

  shouldBeDisabled(){
    let isDisabled = false;
    if(!(this.autoApprovalForm.valid && this.autoApprovalForm.dirty)){
      isDisabled = true;
    }
    if(this.autoApprovalForm.controls['enableAutoApprove'].value && this.autoApprovalForm.controls['roles'].value.length == 0){
      isDisabled = true;
    }
    return isDisabled;
  }

}
