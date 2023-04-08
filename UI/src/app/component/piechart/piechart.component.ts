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
  width = '100%';
  height = 298;
  // The radius of the pie chart is half the smallest side
  radius =  this.height / 2 - this.margin;
  colors;
  pieChartValuesArray = [];



  constructor(private viewContainerRef: ViewContainerRef) {
    this.elem = this.viewContainerRef.element.nativeElement;
  }

  createSvg(): void {
    d3.select(this.elem).select('#pie').select('svg').remove();
    this.svg = d3
      .select('#pie')
      .append('svg')
      .attr('width', this.width)
      .attr('height', this.height)
      .append('g')
      .attr(
        'transform',
        'translate(' + 120 + ',' + this.height / 2 + ')',
      );
  }
  //d.Stars.toString()
  createColors(): void {
    this.colors = d3
      .scaleOrdinal()
      .domain(this.pieChartValuesArray.map((d) => d.value.toString()))
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
    this.pieChartValuesArray = [];
    const width = this.width;
    const pie = d3.pie<any>().value((d: any) => Number(d.value));
    const pieChartValues = this.data[0]?.value[0]?.value[0]?.value;
    const colors = this.colors;
    for (const property in pieChartValues) {
      this.pieChartValuesArray.push({
        ['title']: property,
        ['value']: pieChartValues[property]
      }
      )
    }

    const totalCount = d3.sum(this.pieChartValuesArray, function (d) { return d.value; });
    const toPercent = d3.format("0.1%");

    // Build the pie chart
    this.svg
    .selectAll('pieces')
    .data(pie(this.pieChartValuesArray))
    .enter()
      .append('path')
      .attr('d', d3.arc().innerRadius(0).outerRadius(this.radius))
      .attr('fill', (d: any, i: any) => colors(i))
      .attr('stroke', '#fff')
      .style('stroke-width', '1px');
    var legendG = this.svg.selectAll(".legend") // note appending it to mySvg and not svg to make positioning easier
      .data(pie(this.pieChartValuesArray))
      .enter().append("g")
      .attr("transform", function (d, i) {
        return "translate(" + 150 + "," + (i * 15 - 80) + ")"; // place each legend on the right and bump each one down 15 pixels
      })
      .attr("class", "legend");

    legendG.append("rect") // make a matching color rect
      .attr("width", 10)
      .attr("height", 10)
      .attr("fill", function (d, i) {
        return colors(i);
      });

    legendG.append("text") // add the text
      .text((d) => { return `${d?.data?.title} (${d?.data.value}) - ${toPercent(d?.data.value / totalCount)}` })
      .style("font-size", 12)
      .style('text-transform', 'capitalize')
      .attr("y", 10)
      .attr("x", 15);
  }

  ngOnChanges(changes: SimpleChanges) {
    // only run when property "data" changed
    this.createSvg();
    this.createColors();
    this.drawChart();
  }

  ngOnDestroy() {
    // this is used for removing svg already made when value is updated
    d3.select(this.elem).select('#pie').select('svg').remove();

    this.pieChartValuesArray = [];
  }


}
