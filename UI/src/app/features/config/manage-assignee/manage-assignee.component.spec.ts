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
import { Table } from 'primeng/table';

import { ManageAssigneeComponent } from './manage-assignee.component';

describe('ManageAssigneeComponent', () => {
  let component: ManageAssigneeComponent;
  let fixture: ComponentFixture<ManageAssigneeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ManageAssigneeComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ManageAssigneeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should update assignee selected status', () => {
    const assignee = {
      "name": "testDisplayName",
      "displayName": "testDisplayName",
      "checked": true
    };
    const event = {
      target: {
        checked: true
      }
    };
    component.onUserSelectionChange(event, assignee);
    expect(assignee.checked).toBeTrue();
  });

  it('should reset table filter and Search field',()=>{
    component.table = TestBed.createComponent(Table).componentInstance;
    component.reset();
    expect(component.searchText.value).toBeFalsy();
  });
});
