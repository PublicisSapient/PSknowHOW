/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/
import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';

@Component({
  selector: 'app-rating',
  templateUrl: './rating.component.html',
  styleUrls: ['./rating.component.css']
})
export class RatingComponent implements OnChanges{
  @Input() editable=false;
  @Input() currentAssignee;
  form= new FormGroup({
    happinessRating: new FormControl()
  });

  ngOnChanges(changes: SimpleChanges): void {
    this.form.controls['happinessRating'].setValue(String(this.currentAssignee.happinessRating));
      if(changes['editable']['currentValue']){
        this.form.controls['happinessRating'].enable();
      }else{
        this.form.controls['happinessRating'].disable();
      }
  }

  onChange(){
    this.currentAssignee.happinessRating = +this.form.value['happinessRating'];
  }

}
