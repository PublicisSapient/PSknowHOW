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

import { Component, Input, ViewContainerRef, OnInit } from '@angular/core';
import * as d3 from 'd3';
@Component({
  selector: 'app-numberchart',
  templateUrl: './numberchart.component.html',
  styleUrls: ['./numberchart.component.css']
})



export class NumberchartComponent implements OnInit {

  @Input() kpiName: string;
  @Input() value: string;
  @Input() unit: string;
  @Input() backgroundColor: string;
  @Input() width: string;
  @Input() height: string;
  @Input() fontSize: string;

  elem;

  constructor(private viewContainerRef: ViewContainerRef) { }

  ngOnInit() {


    this.elem = this.viewContainerRef.element.nativeElement;
    this.draw();


  }

  draw() {
    d3.select(this.elem).select('.number-chart').style('height', this.height + 'px').style('margin-top', '60px').style('background-color', this.backgroundColor);
    d3.select(this.elem).select('.value').style('font-size', this.fontSize + 'em');

  }

}
