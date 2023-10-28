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

import { FieldMappingFieldComponent } from './field-mapping-field.component';

describe('FieldMappingFieldComponent', () => {
  let component: FieldMappingFieldComponent;
  let fixture: ComponentFixture<FieldMappingFieldComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FieldMappingFieldComponent ]
    })
    .compileComponents();
  });

    beforeEach(() => {
      fixture = TestBed.createComponent(FieldMappingFieldComponent);
      component = fixture.componentInstance;
    });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should reset radio button',()=>{
    component.resetRadioButton("fakeName");
    expect(component.value).toBe(true)
  })

  it('should set addtional filter value button',()=>{
    component.setAdditionalFilterValue("fakeName");
    component.showDialogToAddValue(true,'Name','field')
    expect(component.value).toBe('fakeName')
  })
});
