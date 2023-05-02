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

import { ProfileModule } from './profile.module';
import { ProfileComponent } from './profile.component';
import { RouterTestingModule } from '@angular/router/testing';
import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import { GetAuthorizationService } from 'src/app/services/get-authorization.service';
import { SharedService } from 'src/app/services/shared.service';

describe('ProfileModule', () => {
  let fixture: ComponentFixture<ProfileComponent>;
  let profileModule: ProfileModule;
  let component : ProfileComponent ;
  let authService: GetAuthorizationService;
  let sharedService: SharedService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations : [ProfileComponent],
      imports : [RouterTestingModule],
      providers : [GetAuthorizationService,SharedService]

    }).compileComponents();
    profileModule = new ProfileModule();
    fixture = TestBed.createComponent(ProfileComponent);
    component = fixture.componentInstance;
    authService = TestBed.inject(GetAuthorizationService);
    sharedService =TestBed.inject(SharedService);
  });

  it('should create an instance', () => {
    expect(profileModule).toBeTruthy();
  });

  it("should user login as super admin",()=>{
    spyOn(authService,'checkIfSuperUser').and.returnValue(true);
    component.ngOnInit();
    expect(component.isSuperAdmin).toBeTruthy();
  })

  it("should user login as project admin",()=>{
    spyOn(authService,'checkIfProjectAdmin').and.returnValue(true);
    component.ngOnInit();
    expect(component.isProjectAdmin).toBeTruthy();
  })

  it("should AD login enable and change password disable",()=>{
   localStorage.setItem('loginType',"AD");
   sharedService.currentUserDetailsSubject.next({});
    component.ngOnInit();
    expect(component.changePswdDisabled).toBeTruthy();
    expect(component.adLogin).toBeTruthy();
  })

});
