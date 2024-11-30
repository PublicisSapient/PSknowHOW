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
  ViewContainerRef,
  OnChanges,
  SimpleChanges,
  OnInit,
  ElementRef,
  AfterViewInit,
} from '@angular/core';
import * as d3 from 'd3';
@Component({
  selector: 'app-barchart',
  templateUrl: './barchart.component.html',
  styleUrls: ['./barchart.component.css'],
})
export class BarchartComponent implements OnInit {
  @Input() data: any[] = []; // Input dataset
  @Input() width: number = 300; // Chart width
  @Input() height: number = 300; // Chart height

  private svg: any;
  private tooltip: any;

  constructor(private elRef: ElementRef) {}

  ngOnInit(): void {
    if (this.data && this.data.length) {
      this.createChart();
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.data && !changes.data.firstChange) {
      this.updateChart();
    }
  }

  private createChart(): void {
    const element = this.elRef.nativeElement.querySelector('.chart-container');
    const margin = { top: 20, right: 20, bottom: 40, left: 50 };
    const chartWidth = this.width - margin.left - margin.right;
    const chartHeight = this.height - margin.top - margin.bottom;
  
    // Append SVG container
    this.svg = d3
      .select(element)
      .append('svg')
      .attr('width', this.width)
      .attr('height', this.height)
      .append('g')
      .attr('transform', `translate(${margin.left}, ${margin.top})`);
  
    // Define scales
    const xScale = d3
      .scaleBand()
      .domain(this.data.map((d) => d.category))
      .range([0, chartWidth])
      .padding(0.3);
  
    const yScale = d3
      .scaleLinear()
      .domain([0, d3.max(this.data, (d) => d.value) || 0])
      .nice()
      .range([chartHeight, 0]);
  
    const colorScale = d3
      .scaleOrdinal()
      .domain(this.data.map((d) => d.category))
      .range(this.data.map((d) => d.color));
  
    // Add axes
    this.svg
      .append('g')
      .attr('transform', `translate(0, ${chartHeight})`)
      .call(d3.axisBottom(xScale));
  
    this.svg
      .append('g')
      .call(d3.axisLeft(yScale).ticks(5).tickFormat((d) => `${d}hr`));
  
    // Tooltip
    this.tooltip = d3
      .select(element)
      .append('div')
      .attr('class', 'tooltip')
      .style('position', 'absolute')
      .style('display', 'none')
      .style('background', '#333')
      .style('color', '#fff')
      .style('padding', '5px 10px')
      .style('border-radius', '5px')
      .style('pointer-events', 'none');
  
    // Add bars
    this.svg
      .selectAll('.bar')
      .data(this.data)
      .enter()
      .append('rect')
      .attr('class', 'bar')
      .attr('x', (d) => xScale(d.category)!)
      .attr('y', (d) => yScale(d.value))
      .attr('width', xScale.bandwidth())
      .attr('height', (d) => chartHeight - yScale(d.value))
      .attr('fill', (d) => colorScale(d.category))
      .attr('rx', 5) // Rounded corners
      .attr('ry', 5)
      .on('mouseover', (event, d) => {
        this.tooltip
          .style('display', 'block')
          .html(`<strong>${d.value}hr</strong>`);
      })
      .on('mousemove', (event) => {
        this.tooltip
          .style('top', `${event.pageY - 40}px`)
          .style('left', `${event.pageX + 10}px`);
      })
      .on('mouseout', () => {
        this.tooltip.style('display', 'none');
      });
  
    // Add labels above bars
    this.svg
      .selectAll('.label')
      .data(this.data)
      .enter()
      .append('text')
      .attr('class', 'label')
      .attr('x', (d) => xScale(d.category)! + xScale.bandwidth() / 2)
      .attr('y', (d) => yScale(d.value) - 10)
      .attr('text-anchor', 'middle')
      .style('font-size', '12px')
      .style('font-weight', 'bold')
      .style('fill', 'black')
      .text((d) => `${d.value}hr`);
  }

  private updateChart(): void {
    // Clear previous chart
    d3.select(this.elRef.nativeElement).select('.chart-container').html('');
    this.createChart();
  }

}
