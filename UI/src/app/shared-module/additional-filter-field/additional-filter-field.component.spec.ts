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
import { FormControl, FormsModule, ReactiveFormsModule, UntypedFormGroup,UntypedFormControl } from '@angular/forms';
import { MessageService } from 'primeng/api';
import { SharedService } from 'src/app/services/shared.service';

import { AdditionalFilterFieldComponent } from './additional-filter-field.component';

const completeHierarchyData = {
  kanban: [
    {
      id: '63244d35d1d9f4caf85056f7',
      level: 1,
      hierarchyLevelId: 'corporate',
      hierarchyLevelName: 'Corporate Name'
    },
    {
      id: '63244d35d1d9f4caf85056f8',
      level: 2,
      hierarchyLevelId: 'business',
      hierarchyLevelName: 'Business Name'
    },
    {
      id: '63244d35d1d9f4caf85056f9',
      level: 3,
      hierarchyLevelId: 'dummy',
      hierarchyLevelName: 'dummy Name'
    },
    {
      id: '63244d35d1d9f4caf85056fa',
      level: 4,
      hierarchyLevelId: 'subdummy',
      hierarchyLevelName: 'Subdummy'
    },
    {
      level: 5,
      hierarchyLevelId: 'project',
      hierarchyLevelName: 'Project'
    },
    {
      level: 6,
      hierarchyLevelId: 'sqd',
      hierarchyLevelName: 'Squad'
    }
  ],
  scrum: [
    {
      id: '63244d35d1d9f4caf85056f7',
      level: 1,
      hierarchyLevelId: 'corporate',
      hierarchyLevelName: 'Corporate Name'
    },
    {
      id: '63244d35d1d9f4caf85056f8',
      level: 2,
      hierarchyLevelId: 'business',
      hierarchyLevelName: 'Business Name'
    },
    {
      id: '63244d35d1d9f4caf85056f9',
      level: 3,
      hierarchyLevelId: 'dummy',
      hierarchyLevelName: 'dummy Name'
    },
    {
      id: '63244d35d1d9f4caf85056fa',
      level: 4,
      hierarchyLevelId: 'subdummy',
      hierarchyLevelName: 'Subdummy'
    },
    {
      level: 5,
      hierarchyLevelId: 'project',
      hierarchyLevelName: 'Project'
    },
    {
      level: 6,
      hierarchyLevelId: 'sprint',
      hierarchyLevelName: 'Sprint'
    },
    {
      level: 7,
      hierarchyLevelId: 'sqd',
      hierarchyLevelName: 'Squad'
    }
  ]
};

describe('AdditionalFilterFieldComponent', () => {
  let component: AdditionalFilterFieldComponent;
  let fixture: ComponentFixture<AdditionalFilterFieldComponent>;
  let sharedService: SharedService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AdditionalFilterFieldComponent ],
      imports:[ReactiveFormsModule, FormsModule],
      providers:[MessageService, SharedService]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdditionalFilterFieldComponent);
    component = fixture.componentInstance;
    sharedService = TestBed.inject(SharedService);
    fixture.detectChanges();
  });

  beforeEach(()=>{
    let localStore = {};

    spyOn(window.localStorage, 'getItem').and.callFake((key) =>
      key in localStore ? localStore[key] : null
    );
    spyOn(window.localStorage, 'setItem').and.callFake(
      (key, value) => (localStore[key] = value + '')
    );
    spyOn(window.localStorage, 'clear').and.callFake(() => (localStore = {}));

    localStorage.setItem('completeHierarchyData', JSON.stringify(completeHierarchyData));
  });

  it('should create', () => {
    spyOn(component,'generateAdditionalFilterMappings');
    expect(component).toBeTruthy();
  });

  it('should add AdditionalFilterOptions',()=>{
    component.filterHierarchy = completeHierarchyData.scrum;
    component.addAdditionalFilterOptions();
    expect(component.additionalFilterOptions.length).toEqual(1);
  });

  it('should generate additional filter mappings',()=>{
    component.filterHierarchy = completeHierarchyData.scrum;
    spyOn(sharedService,'getSelectedFieldMapping').and.returnValue({
      additionalFilterConfig: [
        {
            "filterId": "sqd",
            "identifyFrom": "Component",
            "identificationField": "",
            "values": []
        }
    ]
    });
    spyOn(component,'addAdditionalFilterOptions');
    component.generateAdditionalFilterMappings();
    expect(component.fieldMappingForm.controls['sqdIdentifier']).toBeTruthy();
    expect(component.fieldMappingForm.controls[ 'sqdIdentMultiValue']).toBeTruthy();
  });

  it('should add Additional Filter Mappings',()=>{
    component.additionalFiltersArray=[{
      "name": "Squad",
      "code": "sqd"
  }];
  component.additionalFilterIdentifier = [{
    "name": "Squad",
    "code": "sqd"
}];

component.addAdditionalFilterMappings();
expect(component.fieldMappingForm.controls['sqdIdentifier']).toBeTruthy();
  });

  it('should change control based on selection',()=>{
    const event = {
      value:"Labels"
    };
    const additionalFilterIdentifier = {
      "name": "Squad",
      "code": "sqd"
  };
  const handleAdditionalFiltersSpy = spyOn(component,'handleAdditionalFilters');
  component.changeControl(event,additionalFilterIdentifier);
  expect(component.fieldMappingForm.controls[ 'sqdIdentMultiValue']).toBeTruthy();
  expect(handleAdditionalFiltersSpy).toHaveBeenCalled();
  });

  it('should remove additional filter mapping',()=>{
    component.additionalFiltersArray=[{
      "name": "Squad",
      "code": "sqd"
  }];
    const handleAdditionalFiltersSpy = spyOn(component,'handleAdditionalFilters');
    component.removeAdditionFilterMapping({ "name": "Squad","code": "sqd"});
    expect(component.additionalFiltersArray.length).toEqual(0);
    expect(handleAdditionalFiltersSpy).toHaveBeenCalled();
  });

  it('should handle additional filter',()=>{
    component.filterHierarchy = completeHierarchyData.scrum;
    component.fieldMappingForm = new UntypedFormGroup({
      sqdIdentifier : new FormControl('Labels'),
      sqdIdentMultiValue:new FormControl(['UI'])
    });
    component.handleAdditionalFilters();
    expect(component.additionalFilterConfig.length).toEqual(1);
  });

  it('should open/close the dropdown dialog and set values', () => {
    component.selectedField = 'jiraDefectRejectionStatusDIR';
    component.fieldMappingForm = new UntypedFormGroup({
      'jiraDefectRejectionStatusDIR' : new UntypedFormControl()
    });
    const dropDownMetaData = require('../../../test/resource/KPIConfig.json');
    component.fieldMappingForm.controls['jiraDefectRejectionStatusDIR']?.setValue("fake value")
    component.fieldMappingMetaData = dropDownMetaData.data;
    component.showDialogToAddValue(true,'jiraDefectRejectionStatusDIR','fields');
    expect(component.fieldMappingMultiSelectValues).not.toBeNull();

    component.fieldMappingMetaData = dropDownMetaData;
    component.showDialogToAddValue(true,'jiraDefectRejectionStatusDIR','fields');
    expect(component.fieldMappingMultiSelectValues).not.toBeNull();

    component.fieldMappingMetaData = dropDownMetaData.data;
    component.showDialogToAddValue(true,'jiraDefectRejectionStatusDIR','workflow');
    expect(component.fieldMappingMultiSelectValues).not.toBeNull();
    
    component.fieldMappingMetaData = dropDownMetaData;
    component.showDialogToAddValue(true,'jiraDefectRejectionStatusDIR','workflow');
    expect(component.fieldMappingMultiSelectValues).not.toBeNull();

    component.fieldMappingMetaData = dropDownMetaData.data;
    component.showDialogToAddValue(true,'jiraDefectRejectionStatusDIR','Issue_Link');
    expect(component.fieldMappingMultiSelectValues).not.toBeNull();
    
    component.fieldMappingMetaData = dropDownMetaData;
    component.showDialogToAddValue(true,'jiraDefectRejectionStatusDIR','Issue_Link');
    expect(component.fieldMappingMultiSelectValues).not.toBeNull();

    component.fieldMappingMetaData = dropDownMetaData.data;
    component.showDialogToAddValue(true,'jiraDefectRejectionStatusDIR','Issue_Type');
    expect(component.fieldMappingMultiSelectValues).not.toBeNull();
    
    component.fieldMappingMetaData = dropDownMetaData;
    component.showDialogToAddValue(true,'jiraDefectRejectionStatusDIR','Issue_Type');
    expect(component.fieldMappingMultiSelectValues).not.toBeNull();

    component.fieldMappingMetaData = dropDownMetaData;
    component.showDialogToAddValue(true,'jiraDefectRejectionStatusDIR','default');
    expect(component.fieldMappingMultiSelectValues).not.toBeNull();

    component.fieldMappingMetaData = dropDownMetaData.data;
    component.showDialogToAddValue(false,'jiraDefectRejectionStatusDIR','fields');
    expect(component.fieldMappingMultiSelectValues).not.toBeNull();
  });

  it('should close dialog',()=>{
    component.cancelDialog()
   expect(component.displayDialog).toBeFalsy();
  })

  it('should select values from popup', () => {
    component.singleSelectionDropdown = false;
    component.selectedField = 'jiraDefectRejectionStatusDIR';
    spyOn(component,'handleAdditionalFilters');
    component.fieldMappingForm = new UntypedFormGroup({
      'jiraDefectRejectionStatusDIR' : new UntypedFormControl()
    });
    component.fieldMappingMultiSelectValues = [{
      key: 'New',
      data: 'New'
    }, {
      key: 'Active',
      data: 'Active'
    }, {
      key: 'Resolved',
      data: 'Resolved'
    }, {
      key: 'Closed',
      data: 'Closed'
    }, {
      key: 'Removed',
      data: 'Removed'
    }];
    component.saveDialog();
    expect(component.populateDropdowns).toBeFalsy();
    expect(component.displayDialog).toBeFalsy();
  });
});
