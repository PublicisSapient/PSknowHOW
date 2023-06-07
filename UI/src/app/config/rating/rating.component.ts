import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';

@Component({
  selector: 'app-rating',
  templateUrl: './rating.component.html',
  styleUrls: ['./rating.component.css']
})
export class RatingComponent implements OnInit , OnChanges{
  @Input() editable=false;
  @Input() currentAssignee;
  form= new FormGroup({
    happinessRating: new FormControl()
  });

  constructor() { }

  ngOnInit(): void {
  }

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
