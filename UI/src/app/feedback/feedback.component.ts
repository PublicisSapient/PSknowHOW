import { Component, Input, OnInit } from '@angular/core';
import { UntypedFormControl, UntypedFormGroup, Validators } from '@angular/forms';
import { HttpService } from 'src/app/services/http.service';
import { SharedService } from 'src/app/services/shared.service';

@Component({
  selector: 'app-feedback',
  templateUrl: './feedback.component.html',
  styleUrls: ['./feedback.component.css']
})
export class FeedbackComponent implements OnInit {
  showForm: boolean = false;
  voiceForm = new UntypedFormGroup({
    feedbackType: new UntypedFormControl('', Validators.required),
    category: new UntypedFormControl('', Validators.required),
    feedback: new UntypedFormControl('', { validators: [Validators.required, Validators.maxLength(600)] })
  });
  isFeedbackSubmitted = false;
  formMessage = '';
  userName: string;
  area;

  constructor(private httpService: HttpService, private sharedService: SharedService) { }

  ngOnInit(): void {
    this.sharedService.currentUserDetailsObs.subscribe(details => {
      this.userName = details['user_name'];
    })
  }

  save() {
    const postObj = this.voiceForm.value;
    postObj['username'] = this.userName;
    this.httpService.submitFeedbackData(postObj).subscribe((response) => {
      if (response.message) {
        this.isFeedbackSubmitted = true;
        this.formMessage = response.message;
        setTimeout(() => {
          this.formMessage = '';
        }, 3000);
        this.voiceForm.reset();
      }
    }, error => {
      console.log(error);
      this.isFeedbackSubmitted = false;
      setTimeout(() => {
        this.formMessage = '';
      }, 3000);
    });
  }
}
