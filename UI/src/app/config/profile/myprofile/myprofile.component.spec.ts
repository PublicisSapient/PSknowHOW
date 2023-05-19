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
import { FormGroup, Validators, FormBuilder } from '@angular/forms';
import { ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { MyprofileComponent } from './myprofile.component';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { CommonModule } from '@angular/common';
import { HttpService } from '../../../services/http.service';
import { APP_CONFIG, AppConfig } from '../../../services/app.config';
import { ProfileComponent } from '../profile.component';
import { environment } from 'src/environments/environment';
import { SharedService } from 'src/app/services/shared.service';
import { of } from 'rxjs';
describe('MyprofileComponent', () => {
  let component: MyprofileComponent;
  let fixture: ComponentFixture<MyprofileComponent>;
  let httpService;
  let httpMock;
  let shared;
  const baseUrl = environment.baseUrl;
  const successResponse = { message: 'Email updated successfully', success: true, data: { username: 'testUser', authorities: ['ROLE_SUPERADMIN'], authType: 'STANDARD', emailAddress: 'rishabh.shukla@publicissapient.com' } };
  const hierarchyData = [
    {
      level: 1,
      hierarchyLevelId: 'corporate',
      hierarchyLevelName: 'Corporate Name',
      suggestions: [
        {
          name: 'C1',
          code: 'C1'
        },
        {
          name: 'Corpate1',
          code: 'Corpate1'
        },
        {
          name: 'Leve1',
          code: 'Leve1'
        },
        {
          name: 'Org1',
          code: 'Org1'
        },
        {
          name: 'Orgc',
          code: 'Orgc'
        },
        {
          name: 'TESTS',
          code: 'TESTS'
        },
        {
          name: 'Test1',
          code: 'Test1'
        },
        {
          name: 'TestC',
          code: 'TestC'
        },
        {
          name: 'TestCorp',
          code: 'TestCorp'
        },
        {
          name: 'abcv',
          code: 'abcv'
        },
        {
          name: 'bittest',
          code: 'bittest'
        },
        {
          name: 'dfdsg',
          code: 'dfdsg'
        },
        {
          name: 'dgdfhfgjgh',
          code: 'dgdfhfgjgh'
        },
        {
          name: 'dgfdh',
          code: 'dgfdh'
        },
        {
          name: 'dgfg',
          code: 'dgfg'
        },
        {
          name: 'dghhjjh',
          code: 'dghhjjh'
        },
        {
          name: 'djfyyyyyyyyyyyyyyy',
          code: 'djfyyyyyyyyyyyyyyy'
        },
        {
          name: 'dsgfdj',
          code: 'dsgfdj'
        },
        {
          name: 'fghhhj',
          code: 'fghhhj'
        },
        {
          name: 'fhgkl',
          code: 'fhgkl'
        },
        {
          name: 'fhjjjjjj',
          code: 'fhjjjjjj'
        },
        {
          name: 'gfhygjhk',
          code: 'gfhygjhk'
        },
        {
          name: 'ghhjhkjl',
          code: 'ghhjhkjl'
        },
        {
          name: 'ghjk',
          code: 'ghjk'
        },
        {
          name: 'gjhfkjhkj',
          code: 'gjhfkjhkj'
        },
        {
          name: 'gjhjkk',
          code: 'gjhjkk'
        },
        {
          name: 'gjkjllf',
          code: 'gjkjllf'
        },
        {
          name: 'gjtykghk',
          code: 'gjtykghk'
        },
        {
          name: 'hgjhgjk',
          code: 'hgjhgjk'
        },
        {
          name: 'hjkk',
          code: 'hjkk'
        },
        {
          name: 'rduuuuuuuuu',
          code: 'rduuuuuuuuu'
        },
        {
          name: 'trrrrrrrrrrrrrrrrrrr',
          code: 'trrrrrrrrrrrrrrrrrrr'
        },
        {
          name: 'trt',
          code: 'trt'
        },
        {
          name: 'wdddddd',
          code: 'wdddddd'
        },
        {
          name: 'ytttttttttttt',
          code: 'ytttttttttttt'
        }
      ],
      value: '',
      required: true
    },
    {
      level: 2,
      hierarchyLevelId: 'business',
      hierarchyLevelName: 'Business Name',
      suggestions: [
        {
          name: 'B1',
          code: 'B1'
        },
        {
          name: 'Bus1',
          code: 'Bus1'
        },
        {
          name: 'Leve2',
          code: 'Leve2'
        },
        {
          name: 'Org2',
          code: 'Org2'
        },
        {
          name: 'Orgb',
          code: 'Orgb'
        },
        {
          name: 'Test2',
          code: 'Test2'
        },
        {
          name: 'TestB',
          code: 'TestB'
        },
        {
          name: 'TestBus',
          code: 'TestBus'
        },
        {
          name: 'asfd',
          code: 'asfd'
        },
        {
          name: 'dcccccccccc',
          code: 'dcccccccccc'
        },
        {
          name: 'ddddddddddddd',
          code: 'ddddddddddddd'
        },
        {
          name: 'dfhhhhhhh',
          code: 'dfhhhhhhh'
        },
        {
          name: 'erhjjkkjkl',
          code: 'erhjjkkjkl'
        },
        {
          name: 'fbcncvn',
          code: 'fbcncvn'
        },
        {
          name: 'fdjfjk',
          code: 'fdjfjk'
        },
        {
          name: 'fgdsfgdh',
          code: 'fgdsfgdh'
        },
        {
          name: 'fhgjhkjk',
          code: 'fhgjhkjk'
        },
        {
          name: 'ggggggg',
          code: 'ggggggg'
        },
        {
          name: 'ghjjk',
          code: 'ghjjk'
        },
        {
          name: 'hjjhjk',
          code: 'hjjhjk'
        },
        {
          name: 'hjuy',
          code: 'hjuy'
        },
        {
          name: 'jhbjnk',
          code: 'jhbjnk'
        },
        {
          name: 'jhkjljkll',
          code: 'jhkjljkll'
        },
        {
          name: 'jhlkl',
          code: 'jhlkl'
        },
        {
          name: 'jnkmlkm',
          code: 'jnkmlkm'
        },
        {
          name: 'knj,n,m m,',
          code: 'knj,n,m m,'
        },
        {
          name: 'rrrrrrrrr',
          code: 'rrrrrrrrr'
        },
        {
          name: 'rrrrrrrrre',
          code: 'rrrrrrrrre'
        },
        {
          name: 'rytrujjjk',
          code: 'rytrujjjk'
        },
        {
          name: 'ryyhtfjghk',
          code: 'ryyhtfjghk'
        },
        {
          name: 'sdddddddddddd',
          code: 'sdddddddddddd'
        },
        {
          name: 'sgdgf',
          code: 'sgdgf'
        },
        {
          name: 't1',
          code: 't1'
        },
        {
          name: 'ttttttuiiiiiiii',
          code: 'ttttttuiiiiiiii'
        },
        {
          name: 'wAAAAAAAAAA',
          code: 'wAAAAAAAAAA'
        }
      ],
      value: '',
      required: true
    },
    {
      level: 3,
      hierarchyLevelId: 'dummyaccount',
      hierarchyLevelName: 'dummyAccount Name',
      suggestions: [
        {
          name: 'A1',
          code: 'A1'
        },
        {
          name: 'Acc1',
          code: 'Acc1'
        },
        {
          name: 'Level3',
          code: 'Level3'
        },
        {
          name: 'Org3',
          code: 'Org3'
        },
        {
          name: 'Orga',
          code: 'Orga'
        },
        {
          name: 'Test3',
          code: 'Test3'
        },
        {
          name: 'TestAcc',
          code: 'TestAcc'
        },
        {
          name: 'TestC',
          code: 'TestC'
        },
        {
          name: 'WRRRRRRRRR',
          code: 'WRRRRRRRRR'
        },
        {
          name: 'bxccnbcvn',
          code: 'bxccnbcvn'
        },
        {
          name: 'ddddddddddddddddd',
          code: 'ddddddddddddddddd'
        },
        {
          name: 'dddst',
          code: 'dddst'
        },
        {
          name: 'dfdgfdh',
          code: 'dfdgfdh'
        },
        {
          name: 'dfsgdf',
          code: 'dfsgdf'
        },
        {
          name: 'eeeee',
          code: 'eeeee'
        },
        {
          name: 'erttyyuui',
          code: 'erttyyuui'
        },
        {
          name: 'fdddddddddddddddd',
          code: 'fdddddddddddddddd'
        },
        {
          name: 'gjhkjjl',
          code: 'gjhkjjl'
        },
        {
          name: 'gsdddddddddddg',
          code: 'gsdddddddddddg'
        },
        {
          name: 'hjl',
          code: 'hjl'
        },
        {
          name: 'hkjkjlkl',
          code: 'hkjkjlkl'
        },
        {
          name: 'hyjykjl',
          code: 'hyjykjl'
        },
        {
          name: 'jhjkhkk',
          code: 'jhjkhkk'
        },
        {
          name: 'jj,ddddw',
          code: 'jj,ddddw'
        },
        {
          name: 'jjkjkjhk',
          code: 'jjkjkjhk'
        },
        {
          name: 'kmmmk',
          code: 'kmmmk'
        },
        {
          name: 'mn',
          code: 'mn'
        },
        {
          name: 'shhhhhhhhh',
          code: 'shhhhhhhhh'
        },
        {
          name: 'sss',
          code: 'sss'
        },
        {
          name: 'ssssssssssss',
          code: 'ssssssssssss'
        },
        {
          name: 't2',
          code: 't2'
        },
        {
          name: 'tyui',
          code: 'tyui'
        },
        {
          name: 'wwgt',
          code: 'wwgt'
        },
        {
          name: 'xfnnnnnnnnn',
          code: 'xfnnnnnnnnn'
        },
        {
          name: 'yutruityi',
          code: 'yutruityi'
        }
      ],
      value: '',
      required: true
    },
    {
      level: 4,
      hierarchyLevelId: 'dummysubaccount',
      hierarchyLevelName: 'dummySubaccount',
      suggestions: [
        {
          name: 'Level4',
          code: 'Level4'
        },
        {
          name: 'Org4',
          code: 'Org4'
        },
        {
          name: 'Orgs',
          code: 'Orgs'
        },
        {
          name: 'S1',
          code: 'S1'
        },
        {
          name: 'Sub1',
          code: 'Sub1'
        },
        {
          name: 'Test4',
          code: 'Test4'
        },
        {
          name: 'TestS',
          code: 'TestS'
        },
        {
          name: 'Testsub',
          code: 'Testsub'
        },
        {
          name: 'aaaaaaaaaaaaaaaaa',
          code: 'aaaaaaaaaaaaaaaaa'
        },
        {
          name: 'asc',
          code: 'asc'
        },
        {
          name: 'cbvcxcncvn',
          code: 'cbvcxcncvn'
        },
        {
          name: 'eeeeeeeeee',
          code: 'eeeeeeeeee'
        },
        {
          name: 'eeeeeeeeeeee',
          code: 'eeeeeeeeeeee'
        },
        {
          name: 'erweteryu',
          code: 'erweteryu'
        },
        {
          name: 'ffff',
          code: 'ffff'
        },
        {
          name: 'fhfd',
          code: 'fhfd'
        },
        {
          name: 'fhgjhk',
          code: 'fhgjhk'
        },
        {
          name: 'fhjkk',
          code: 'fhjkk'
        },
        {
          name: 'ghthhhhhhhhhht',
          code: 'ghthhhhhhhhhht'
        },
        {
          name: 'hjkhkjk',
          code: 'hjkhkjk'
        },
        {
          name: 'hkkkkkk',
          code: 'hkkkkkk'
        },
        {
          name: 'jhhjkjhkj',
          code: 'jhhjkjhkj'
        },
        {
          name: 'jhhvgvggv',
          code: 'jhhvgvggv'
        },
        {
          name: 'jkjkllk;k;',
          code: 'jkjkllk;k;'
        },
        {
          name: 'kmkkkk',
          code: 'kmkkkk'
        },
        {
          name: 'saaaaaaaaaaaaa',
          code: 'saaaaaaaaaaaaa'
        },
        {
          name: 'sasdfdgfgf',
          code: 'sasdfdgfgf'
        },
        {
          name: 'sdf',
          code: 'sdf'
        },
        {
          name: 'sdgggggggg',
          code: 'sdgggggggg'
        },
        {
          name: 'seeeeee',
          code: 'seeeeee'
        },
        {
          name: 'sytttu',
          code: 'sytttu'
        },
        {
          name: 't3',
          code: 't3'
        },
        {
          name: 'xgggggggggg',
          code: 'xgggggggggg'
        },
        {
          name: 'zfghg',
          code: 'zfghg'
        },
        {
          name: 'zzzzzzzzzzzzzzzzzzf',
          code: 'zzzzzzzzzzzzzzzzzzf'
        }
      ],
      value: '',
      required: true
    }
  ];


  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        FormsModule,
        ReactiveFormsModule,
        CommonModule,
        HttpClientTestingModule,
        RouterTestingModule,
      ],
      declarations: [MyprofileComponent],
      providers: [HttpService, ProfileComponent, SharedService , { provide: APP_CONFIG, useValue: AppConfig }]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MyprofileComponent);
    component = fixture.componentInstance;
    httpService = TestBed.inject(HttpService);
    httpMock = TestBed.inject(HttpTestingController);
    shared = TestBed.inject(SharedService);

    let localStore = {};

    spyOn(window.localStorage, 'getItem').and.callFake((key) =>
      key in localStore ? localStore[key] : null
    );
    spyOn(window.localStorage, 'setItem').and.callFake(
      (key, value) => (localStore[key] = value + '')
    );
    spyOn(window.localStorage, 'clear').and.callFake(() => (localStore = {}));

    localStorage.setItem('hierarchyData', JSON.stringify(hierarchyData));
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should set email', () => {
    component.ngOnInit();
    shared.currentUserDetailsSubject.next({user_name : "dummyUser",user_email:"someemail@abc.com"})
    spyOn(shared,'getCurrentUserDetails').and.returnValue("someemail@abc.com")
    component.userEmailForm.controls['email'].setValue('someemail@abc.com');
    component.userEmailForm.controls['confirmEmail'].setValue('someemail@abc.com');
    spyOn(httpService,'changeEmail').and.returnValue(of(successResponse))
    component.setEmail();
    fixture.detectChanges();
    expect(component.userEmailConfigured).toBeTruthy();
  });

  it('should group projects role-wise', () => {
    component.groupProjects(JSON.parse('[{"role":"DUMMY","projects":[{"projectName":"Jenkin_kanban","projectId":"6331857a7bb22322e4e01479","hierarchy":[{"hierarchyLevel":{"level":1,"hierarchyLevelId":"corporate","hierarchyLevelName":"Corporate Name"},"value":"Leve1"}]}]},{"role":"DUMMY","projects":[{"projectName":"Tools proj","projectId":"6332f0a468b5d05cf59c42a6","hierarchy":[{"hierarchyLevel":{"level":1,"hierarchyLevelId":"corporate","hierarchyLevelName":"Corporate Name"},"value":"Org1"}]}]}]'));
    expect(Object.keys(component.roleBasedProjectList).length).toEqual(2);
  });

});
