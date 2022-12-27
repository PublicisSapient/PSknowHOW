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

 import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { KpiFilterComponent } from './kpi-filter.component';

 describe('KpiFilterComponent', () => {
   let component: KpiFilterComponent;
   let fixture: ComponentFixture<KpiFilterComponent>;

   beforeEach(waitForAsync(() => {
     TestBed.configureTestingModule({
       declarations: [ KpiFilterComponent ]
     })
     .compileComponents();
   }));

   beforeEach(() => {
     fixture = TestBed.createComponent(KpiFilterComponent);
     component = fixture.componentInstance;
     fixture.detectChanges();
   });

   it('should create', () => {
     expect(component).toBeTruthy();
   });

   it('should emit seleted kpi on dropdown selection',()=>{
    const spyFieldsToShow = spyOn(component.fieldsToShow, 'emit');
    component.showFieldsPopup();
    expect(spyFieldsToShow).toHaveBeenCalled();
   });

   it('should emit empty string on dropdown clear',()=>{
    component.selectedKpi ='Defect Injection Rate';
    component.onClear();
    expect(component.selectedKpi).toBeFalsy();
   });
 });
