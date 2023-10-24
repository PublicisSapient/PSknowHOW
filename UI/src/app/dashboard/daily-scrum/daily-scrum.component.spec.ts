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

/*********************************************
File contains test cases for daily scrum component.
@author bhagyashree, rishabh
*******************************/

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SharedService } from 'src/app/services/shared.service';
import { DailyScrumComponent } from './daily-scrum.component';

describe('DailyScrumComponent', () => {
  let component: DailyScrumComponent;
  let fixture: ComponentFixture<DailyScrumComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [DailyScrumComponent],
      providers: [SharedService]
    })
      .compileComponents();

    fixture = TestBed.createComponent(DailyScrumComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should set selected user', () => {
    const spySelecteUserChange = spyOn(component.onSelectedUserChange, 'emit');
    component.setSelectedUser('dummyUserId','dummy');
    expect(spySelecteUserChange).toHaveBeenCalled();

  });

  it('should set showLess', () => {
    const spysetShowLess = spyOn(component.onShowLessOrMore, 'emit');
    component.setShowLess();
    expect(spysetShowLess).toHaveBeenCalled();

  });

  it('should set showLess', () => {
    const spyhandleViewExpandCollapse = spyOn(component.onExpandOrCollapse, 'emit');
    component.handleViewExpandCollapse();
    expect(spyhandleViewExpandCollapse).toHaveBeenCalled();

  });

  it('should convert to hours', () => {
    let result = component.convertToHoursIfTime(25, 'hours');
    expect(result).toEqual(25);

    result = component.convertToHoursIfTime(65, 'hours');
    expect(result).toEqual(65);

    result = component.convertToHoursIfTime(60, 'hours');
    expect(result).toEqual(60);
  });

  it('should convert to day', () => {
    let result = component.convertToHoursIfTime(25, 'day');
    expect(result.trim()).toEqual('0d');

    result = component.convertToHoursIfTime(480, 'day');
    expect(result.trim()).toEqual('1d');

    result = component.convertToHoursIfTime(0, 'day');
    expect(result.trim()).toEqual('0d');
  });

});
