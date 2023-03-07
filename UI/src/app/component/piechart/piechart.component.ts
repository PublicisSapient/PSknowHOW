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
import { AnyARecord } from 'dns';

@Component({
  selector: 'app-piechart',
  templateUrl: './piechart.component.html',
  styleUrls: ['./piechart.component.css'],
})
export class PiechartComponent implements OnChanges, OnDestroy {
  @Input() data: any; // json data
  elem;



  svg: any;
  margin = 20;
  width = 450;
  height = 190;
  // The radius of the pie chart is half the smallest side
  radius = Math.min(this.width, this.height) / 2 - this.margin;
  colors;
  pieChartValuesArray = [];



  constructor(private viewContainerRef: ViewContainerRef) {
    this.elem = this.viewContainerRef.element.nativeElement;
  }

  createSvg(): void {
    d3.select(this.elem).select('figure#pie').select('svg').remove();
    this.svg = d3
      .select('figure#pie')
      .append('svg')
      .attr('width', this.width)
      .attr('height', this.height)
      .append('g')
      .attr(
        'transform',
        'translate(' + 80 + ',' + this.height / 2 + ')',
      );
  }
  //d.Stars.toString()
  createColors(): void {
    this.colors = d3
    .scaleOrdinal()
    .domain(this.pieChartValuesArray.map((d) => d.value.toString()))
    .range(['#e42256',
      '#ff8370',
      '#00b1b0',
      '#fec84d',
      '#39b48e',
      '#089f8f',
      '#00898a',
      '#08737f',
      '#215d6e',
      '#2a4858']);
  }
  drawChart(): void {
    // Compute the position of each group on the pie:
    this.pieChartValuesArray = [];
    const width = this.width;
    const pie = d3.pie<any>().value((d: any) => Number(d.value));
    const pieChartValues = this.data[0].value[0].value;
    const colors = this.colors;
    for (const property in pieChartValues) {
      this.pieChartValuesArray.push({
        ['title']: property,
        ['value']: pieChartValues[property]
      }
      )
    }
    
    const totalCount = d3.sum(this.pieChartValuesArray, function(d) { return d.value;});
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

    // Add labels
    const labelLocation = d3.arc().innerRadius(0).outerRadius(this.radius);
    this.svg
      .selectAll('pieces')
      .data(pie(this.pieChartValuesArray))
      .enter()
      .append('text')
      .text((d: any) => `${toPercent(d?.data.value / totalCount)}`)
      .attr(
        'transform',
        (d: any) => 'translate(' + labelLocation.centroid(d) + ')',
      )
      .style('text-anchor', 'middle')
      .style('font-size', 14);

    // again rebind for legend
    var legendG = this.svg.selectAll(".legend") // note appending it to mySvg and not svg to make positioning easier
      .data(pie(this.pieChartValuesArray))
      .enter().append("g")
      .attr("transform", function (d, i) {
        return "translate(" + (width - 250) + "," + (i * 15 + 20) + ")"; // place each legend on the right and bump each one down 15 pixels
      })
      .attr("class", "legend");

    legendG.append("rect") // make a matching color rect
      .attr("width", 10)
      .attr("height", 10)
      .attr("fill", function (d, i) {
        return colors(i);
      });

    legendG.append("text") // add the text
      .text((d) => {console.log(d);return `${d?.data?.title} (${d?.data.value})`})
      .style("font-size", 12)
      .attr("y", 10)
      .attr("x", 11);
  }

  ngOnChanges(changes: SimpleChanges) {
    // only run when property "data" changed
    this.createSvg();
    this.createColors();
    this.drawChart();
  }

  ngOnDestroy() {
    // this is used for removing svg already made when value is updated
    d3.select(this.elem).select('figure#pie').select('svg').remove();

    this.pieChartValuesArray = [];
  }


}
