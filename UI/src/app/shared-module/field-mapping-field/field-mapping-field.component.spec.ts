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
import { Router } from '@angular/router';

describe('FieldMappingFieldComponent', () => {
  let component: FieldMappingFieldComponent;
  let fixture: ComponentFixture<FieldMappingFieldComponent>;
  let mockRouter: jasmine.SpyObj<Router>;

  beforeEach(async () => {

    mockRouter = jasmine.createSpyObj('Router', ['navigate']);
    await TestBed.configureTestingModule({
      declarations: [ FieldMappingFieldComponent ],
      providers: [{ provide: Router, useValue: mockRouter }]
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

  xit('should reset radio button',()=>{
    component.resetRadioButton("fakeName");
    expect(component.value).toBe(true)
  })

  it('should set addtional filter value button',()=>{
    component.setAdditionalFilterValue("fakeName");
    component.showDialogToAddValue(true,'Name','field')
    expect(component.value).toBe('fakeName')
  })

  it('should prevent entering non-numeric keys', () => {
    const event = {
      isTrusted: true,
      key: ".",
      preventDefault: jasmine.createSpy('preventDefault')
    }
    component.enterNumericValue(event);

    expect(event.preventDefault).toHaveBeenCalled();
  });

  it('should allow entering numeric keys', () => {
    const event = {
      isTrusted: true,
      key: "1",
      preventDefault: jasmine.createSpy('preventDefault')
    }

    component.enterNumericValue(event);

    expect(event.preventDefault).not.toHaveBeenCalled();
  });

  it('should set value on numeric input box up and down key event', () => {
    const event = {
      isTrusted: true,
    }
    const spy = spyOn(component, 'setValue');
    component.numericInputUpDown(event);
    expect(spy).toHaveBeenCalled();
  })

  it('should write value',()=>{
    component.writeValue("test");
    expect(component.value).toEqual('test');
  })

  it('should fire onChange event',()=>{
    component.registerOnChange(()=>{});
    expect(component.onChange).toBeDefined();
  })

  it('should fire onTouch event',()=>{
    component.registerOnTouched(()=>{});
    expect(component.onTouched).toBeDefined();
  })

  it('should enable/disable field',()=>{
    component.setDisabledState(true);
    expect(component.isDisabled).toBeTruthy();
  })

  it('should set fields values when value is number',()=>{
    const spyObj = spyOn(component,'onChange')
    component.setValue();
    expect(spyObj).toHaveBeenCalled();
  })

  it('should set fields values when value is string',()=>{
    component.value = "TestValue "
    const spyObj = spyOn(component,'onChange')
    component.setValue();
    expect(spyObj).toHaveBeenCalled();
  })

  it('should set fields values when value is array',()=>{
    component.value = ["test ",'test2 ']
    const spyObj = spyOn(component,'onChange')
    component.setValue(false);
    expect(spyObj).toHaveBeenCalled();
  })

  it('should format value for condtional input', () => {
    const spyObj = spyOn(component, 'onChange')
    component.setValueConditionalInput([{
      'labelValue': 'testValue',
      'countValue': 123
    }])
    expect(spyObj).toHaveBeenCalled();
  })

  it('should rest radio button',()=>{
    const spyObj = spyOn(component, 'setValue')
    component.resetRadioButton('test')
    expect(spyObj).toHaveBeenCalled();
  })

  it('should navigate to the provided URL', () => {
    const testUrl = '/dashboard';

    component.navigate(testUrl);

    expect(mockRouter.navigate).toHaveBeenCalledWith([testUrl]);
  });
 
});
