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

import { Component, EventEmitter, Input, Output, ViewChild } from '@angular/core';
import { FormControl } from '@angular/forms';
import { Table } from 'primeng/table';

@Component({
  selector: 'app-manage-assignee',
  templateUrl: './manage-assignee.component.html',
  styleUrls: ['./manage-assignee.component.css']
})
export class ManageAssigneeComponent {

  @ViewChild('dt') table: Table;
  @Input() assigneeList = [];
  @Output() onAssigneeSave = new EventEmitter();
  searchText = new FormControl('');

  onUserSelectionChange(event, assignee) {
    assignee['checked'] = event.target.checked;
  }

  reset() {
    this.searchText.reset();
    this.table.clear();
  }
}
