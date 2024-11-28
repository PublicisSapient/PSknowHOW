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

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RatingComponent } from './rating.component';
import { ReactiveFormsModule } from '@angular/forms';
import { SimpleChanges } from '@angular/core';

describe('RatingComponent', () => {
  let component: RatingComponent;
  let fixture: ComponentFixture<RatingComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [RatingComponent],
      imports: [ReactiveFormsModule], // Import ReactiveFormsModule for form handling
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(RatingComponent);
    component = fixture.componentInstance;
    // Provide some default values for @Input() properties
    component.currentAssignee = { happinessRating: 3 };
    component.editable = false;
    fixture.detectChanges(); // Trigger initial data binding
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should set happinessRating value from currentAssignee on ngOnChanges', () => {
    const changes: SimpleChanges = {
      currentAssignee: {
        currentValue: { happinessRating: 4 },
        previousValue: { happinessRating: 3 },
        firstChange: false,
        isFirstChange: () => false,
      },
      editable: {
        currentValue: true,
        previousValue: false,
        firstChange: false,
        isFirstChange: () => false,
      },
    };

    component.ngOnChanges(changes);

    expect(component.form.controls['happinessRating'].value).toEqual('3'); // Set as a string
  });

  it('should enable the form control when editable is true', () => {
    const changes: SimpleChanges = {
      editable: {
        currentValue: true,
        previousValue: false,
        firstChange: false,
        isFirstChange: () => false,
      },
    };

    component.ngOnChanges(changes);

    expect(component.form.controls['happinessRating'].enabled).toBeTrue();
  });

  it('should disable the form control when editable is false', () => {
    const changes: SimpleChanges = {
      editable: {
        currentValue: false,
        previousValue: true,
        firstChange: false,
        isFirstChange: () => false,
      },
    };

    component.ngOnChanges(changes);

    expect(component.form.controls['happinessRating'].disabled).toBeTrue();
  });

  it('should update currentAssignee happinessRating on onChange call', () => {
    component.form.controls['happinessRating'].setValue('5');
    component.onChange();
    expect(component.currentAssignee.happinessRating).toEqual(5); // Should convert string to number
  });

  it('should call setValue when currentAssignee is updated', () => {
    const setValueSpy = spyOn(
      component.form.controls['happinessRating'],
      'setValue',
    );
    const newAssignee = { happinessRating: 4 };

    const changes: SimpleChanges = {
      currentAssignee: {
        currentValue: newAssignee,
        previousValue: component.currentAssignee,
        firstChange: false,
        isFirstChange: () => false,
      },
    };

    component.ngOnChanges(changes);

    expect(setValueSpy).toHaveBeenCalledWith('3');
  });
});
