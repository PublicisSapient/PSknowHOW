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

import { Injectable } from '@angular/core';
import { TextEncryptionService } from './text.encryption.service';
import { SharedService } from './shared.service';
@Injectable({
  providedIn: 'root'
})
export class GetAuthorizationService {

  constructor(private aesEncryption: TextEncryptionService,private sharedService : SharedService) { }

  checkIfSuperUser() {
    let decryptedText;
    if(localStorage.getItem('authorities')){
      decryptedText = this.aesEncryption.convertText(localStorage.getItem('authorities'), 'decrypt');
    }
    if (decryptedText && decryptedText !== 'undefined' && JSON.parse(decryptedText).includes('ROLE_SUPERADMIN')) {
      // logged in as SuperUser so return true
      return true;
    } else {
      return false;
    }
  }

  checkIfProjectAdmin() {
    let isProjectAdmin = false;
    if (localStorage.getItem('projectsAccess') && localStorage.getItem('projectsAccess') !== 'undefined' && localStorage.getItem('projectsAccess') !== null) {
      JSON.parse(localStorage.getItem('projectsAccess')).forEach(accessElem => {
        if (accessElem.role === 'ROLE_PROJECT_ADMIN') {
          isProjectAdmin = true;
        }
      });
    }
    return isProjectAdmin;
  }

  checkIfViewer(selectedProject) {
    const projectsAccess = !!localStorage.getItem('projectsAccess') && localStorage.getItem('projectsAccess') !== 'undefined' && localStorage.getItem('projectsAccess') !== 'null' ? JSON.parse(localStorage.getItem('projectsAccess')) : [];
    let isViewer = false;
    projectsAccess.forEach(projectAccess => {
      if (projectAccess.role) {
        if (projectAccess.role === 'ROLE_PROJECT_VIEWER') {
          projectAccess.projects.forEach(project => {
            if (project && project.projectId && project.projectId === selectedProject.id) {
              isViewer = true;
              return;
            }
          });
        }
      }
    });
    return isViewer;
  }

  checkIfRoleViewerPresent() {
    const projectsAccess = !!localStorage.getItem('projectsAccess') && localStorage.getItem('projectsAccess') !== 'undefined' && localStorage.getItem('projectsAccess') !== 'null' ? JSON.parse(localStorage.getItem('projectsAccess')) : [];
    let isViewer = false;
    projectsAccess.forEach(projectAccess => {
      if (projectAccess.role) {
        if (projectAccess.role === 'ROLE_PROJECT_VIEWER') {
          isViewer = true;
        }
      }
    });
    return isViewer;
  }

  getRole() {
    if (this.checkIfSuperUser()) {
      return 'superAdmin';
    } else if (this.checkIfProjectAdmin()) {
      return 'projectAdmin';
    } else if (this.checkIfRoleViewerPresent()) {
      return 'projectViewer';
    } else {
      return 'roleViewer';
    }
  }
}
