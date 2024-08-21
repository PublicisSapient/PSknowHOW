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

import { Component, OnInit } from '@angular/core';
import { SharedService } from '../../../services/shared.service';
import { Router, ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-project-settings',
  templateUrl: './project-settings.component.html',
  styleUrls: ['./project-settings.component.css']
})
export class ProjectSettingsComponent implements OnInit {
  contrlOptions: { name: string; description: string; note: string; action: boolean; }[];
  userProjects = [];
  selectedProject: any;

  constructor(public sharedService: SharedService, public router: Router) {
  }

  ngOnInit(): void {
    this.contrlOptions = [
      { name: 'Enable People performance KPIs', description: 'Enable fetching people info from Agile PM tool or Repo tool connection', note: 'Once enabled, CANNOT be disabled as it affects the historical data', action: true },
      { name: 'Enable Repo Cloning for Developer KPIs ', description: 'By enabling repo cloning, you consent to clone your code repositories (BitBucket, GitLab, GitHub) to avoid API rate-limiting issues. The repository for this project will be cloned on the KH Server. This will grant access to more valuable KPIs on the Developer dashboard. If cloning is disabled, only 2 KPIs will be accessible.', note: 'Once enabled, CANNOT be disabled as it affects the historical data', action: false },
      { name: 'Generate API token', description: 'You can generate KnowHOW POST API token to upload tools data directly', note: 'Utilize this option when you are unable to establish a direct connection with the desired tools', action: false },
      { name: 'Clean Project Data', description: 'Purges all previously retrieved data obtained through tool connections', note: '', action: true }
    ]

    this.getProjects();

    // this.selectedProject = this.sharedService.getSelectedProject();
    this.selectedProject = this.sharedService.getSelectedProject() !== undefined ? this.sharedService.getSelectedProject() : this.userProjects[0];
  }

  getProjects() {
    this.userProjects = this.sharedService.getProjectList();
    if (this.userProjects != null && this.userProjects.length > 0) {
      this.userProjects.sort((a, b) => a.name.localeCompare(b.name, undefined, { numeric: true }));
      // this.selectedProject = this.userProjects[0];
    }
    if(this.selectedProject && this.router.url.includes(this.selectedProject['id'])) {
      this.selectedProject = this.userProjects.filter((x) => x.id == this.selectedProject?.id)[0]
    } else {
      this.selectedProject = this.userProjects[0];
    }
  }

  updateProjectSelection() {
    this.setSelectedProject();
    this.router.navigate([`/dashboard/Config/ConfigSettings/${this.selectedProject['id']}`], { queryParams: { tab: 0 } });
  }

  setSelectedProject() {
    this.sharedService.setSelectedProject(this.selectedProject);
  }

}
