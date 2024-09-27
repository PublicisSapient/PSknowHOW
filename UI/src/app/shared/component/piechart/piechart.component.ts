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

import {
  Component,
  Input,
  OnChanges,
  OnDestroy,
  SimpleChanges,
  ViewContainerRef,
} from '@angular/core';

import * as d3 from 'd3';

@Component({
  selector: 'app-piechart',
  templateUrl: './piechart.component.html',
  styleUrls: ['./piechart.component.css'],
})
export class PiechartComponent implements OnChanges, OnDestroy {
  @Input() data: any; // json data
  elem;



  svg: any;
  margin = 50;
  width = 482;
  height = 298;
  // The radius of the pie chart is half the smallest side
  radius = this.height / 2 - this.margin;
  colors;
  pieChartValuesArray = [];



  constructor(private viewContainerRef: ViewContainerRef) {}

  createColors(): void {
    this.colors = d3
      .scaleOrdinal()
      .domain(this.pieChartValuesArray.map((d) => d?.value?.toString()))
      .range(['#FFA193',
        '#00B1B0',
        '#FEC84D',
        '#E42256',
        '#7FBD7F',
        '#B79CED',
        '#5CA7CF',
        '#994636',
        '#E3D985',
        '#0072bb',
        '#DC0073',
        '#944075',
        '#80A9A2',
        '#E07373',
        '#6C4F84',
        '#BC2C1A',
        '#50723C',
        '#F17552',
        '#445E93',
        '#885053']);
  }
  drawChart(): void {
    // Compute the position of each group on the pie:
    d3.select(this.elem).select('#pie').select('svg').remove();
    this.pieChartValuesArray = [];
    const pie = d3.pie<any>().value((d: any) => Number(d.value));
    const pieChartValues = this.data[0]?.value[0]?.value;

    for (const property in pieChartValues) {
      this.pieChartValuesArray.push({
        ['title']: property,
        ['value']: pieChartValues[property]
      }
      )
    }
    this.pieChartValuesArray.sort((a, b) => {
      if (a.value === b.value) {
        return a.title.localeCompare(b.title);
      } else {
        return b.value - a.value;
      }
    });
    const svg = d3
      .select(this.elem)
      .select('#pie')
      .append('svg')
      .attr('width', this.width)
      .attr('height', this.height)
      .append('g')
      .attr(
        'transform',
        'translate(' + 120 + ',' + this.height / 2 + ')',
      );
    this.createColors();
    const colors = this.colors;

    const totalCount = d3.sum(this.pieChartValuesArray, function (d) { return d.value; });
    const toPercent = d3.format("0.1%");
    // Build the pie chart
    svg
      .selectAll('pieces')
      .data(pie(this.pieChartValuesArray))
      .enter()
      .append('path')
      .attr('d', d3.arc().innerRadius(0).outerRadius(this.radius))
      .attr('fill', (d: any, i: any) => colors(i))
      .attr('stroke', '#fff')
      .style('stroke-width', '1px');

    const foreignObject = svg.append("foreignObject")
      .attr("width", 222)
      .attr("height", this.height - (2 * this.margin))
      .style('overflow-y', 'auto')
      .attr("transform", `translate(140,${-(this.height / 2 - this.margin)})`)
      .append("xhtml:div")
      .attr("id", "main-div")
      .attr("class", "p-text-left")
      .style('border', '1px solid #dedede')
      .append('table')
      .style('width', '100%')
      .style('border-collapse', 'collapse')
      .attr('class', 'legend-table');

    foreignObject
      .append('thead')
      .html('<th class="p-p-1 font-small">Legend Title</th><th class="p-p-1 font-small">Count</th><th class="p-p-1 font-small p-text-right">%</th>')
      .style('border-bottom', '1px solid #dedede')
      .style('text-align', 'left');

    const tbody = foreignObject
      .append('tbody');

    this.pieChartValuesArray.forEach((x, i) => {
      tbody.append('tr')
        .style('border-bottom', '1px solid #dedede')
        .style('padding-top', '2px')
        .html(`<td class="p-p-1 font-small"><span class='rect' style='display:inline-block;width:10px; height:10px; margin: 0 5px 0 0; vertical-align: middle; background:${colors(i)}'></span><span style="text-transform: capitalize;">${x?.title}</span></td><td class="p-p-1 font-small">${x?.value}</td><td class="p-p-1 font-small p-text-right">${toPercent(x?.value / totalCount)}</td>`)
    })


  }

  ngOnChanges(changes: SimpleChanges) {
    // only run when property "data" changed
    if (Object.keys(changes)?.length > 0) {
      if (changes['data']) {
        this.elem = this.viewContainerRef.element.nativeElement;
        this.drawChart();
      }
    }

  }

  ngOnDestroy() {
    // this is used for removing svg already made when value is updated
    d3.select(this.elem).select('#pie').select('svg').remove();
    this.data = [];
    this.pieChartValuesArray = [];
  }


}