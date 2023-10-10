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
import { SharedService } from './shared.service';
@Injectable({
  providedIn: 'root'
})
export class GetAuthorizationService {

  constructor(private sharedService: SharedService) { }

  checkIfSuperUser() {
    if (this.sharedService.getCurrentUserDetails('authorities') && this.sharedService.getCurrentUserDetails('authorities').includes('ROLE_SUPERADMIN')) {
      // logged in as SuperUser so return true
      return true;
    } else {
      return false;
    }
  }

  checkIfProjectAdmin() {
    let isProjectAdmin = false;
    const projectsAccess = !!this.sharedService.getCurrentUserDetails('projectsAccess') && this.sharedService.getCurrentUserDetails('projectsAccess') !== 'undefined' && this.sharedService.getCurrentUserDetails('projectsAccess') !== 'null' ? this.sharedService.getCurrentUserDetails('projectsAccess') : [];
    if (this.sharedService.getCurrentUserDetails('projectsAccess') && this.sharedService.getCurrentUserDetails('projectsAccess') !== 'undefined' && this.sharedService.getCurrentUserDetails('projectsAccess') !== null) {
      projectsAccess?.forEach(accessElem => {
        if (accessElem.role === 'ROLE_PROJECT_ADMIN') {
          isProjectAdmin = true;
        }
      });
    }
    return isProjectAdmin;
  }

  checkIfViewer(selectedProject) {
    const projectsAccess = !!this.sharedService.getCurrentUserDetails('projectsAccess') && this.sharedService.getCurrentUserDetails('projectsAccess') !== 'undefined' && this.sharedService.getCurrentUserDetails('projectsAccess') !== 'null' ? this.sharedService.getCurrentUserDetails('projectsAccess') : [];
    let isViewer = false;
    projectsAccess.forEach(projectAccess => {
      if (projectAccess.role) {
        if (projectAccess.role === 'ROLE_PROJECT_VIEWER') {
          projectAccess.projects.forEach(project => {
            if (project && project.projectId && (project.projectId === selectedProject.id || project.projectId === selectedProject.basicProjectConfigId)) {
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
    const projectsAccess = !!this.sharedService.getCurrentUserDetails('projectsAccess') && this.sharedService.getCurrentUserDetails('projectsAccess') !== 'undefined' && this.sharedService.getCurrentUserDetails('projectsAccess') !== 'null' ? this.sharedService.getCurrentUserDetails('projectsAccess') : [];
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
