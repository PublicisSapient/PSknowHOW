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

import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-circularchart',
  templateUrl: './circularchart.component.html',
  styleUrls: ['./circularchart.component.css']
})
export class CircularchartComponent {
  @Input() kpiName: string;
  @Input() maxValue: string;
  @Input() value: string;
  @Input() unit: string;
  @Input() fillColor: string;
  @Input() backgroundColor: string;
  @Input() width: string;
  @Input() height: string;
  @Input() radius: string;
  @Input() fontSize: string;
  @Input() thickness: string;

}
