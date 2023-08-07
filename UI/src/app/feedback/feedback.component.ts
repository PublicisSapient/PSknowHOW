import { Component, Input } from '@angular/core';
import { UntypedFormControl, UntypedFormGroup, Validators } from '@angular/forms';
import { HttpService } from 'src/app/services/http.service';

@Component({
  selector: 'app-feedback',
  templateUrl: './feedback.component.html',
  styleUrls: ['./feedback.component.css']
})

export class FeedbackComponent {
  @Input() visibleSidebar: boolean;
  feedback: boolean = true;
  voiceForm = new UntypedFormGroup({
    feedback: new UntypedFormControl('', { validators: [Validators.required, Validators.maxLength(600)] })
  });
  isFeedbackSubmitted = false;
  formMessage = '';

  constructor(private httpService: HttpService) { }

  toggleFlag() {
    this.feedback = !this.feedback;
  }

  save() {
    const postObj = this.voiceForm.value;
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

  open(){
    document.documentElement.scrollTop = 0;
  }

  OnOverlayHide(){
    this.voiceForm.reset();
  }
}
