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
  @Input() width;
  @Input() height;
  elem;

  private svg: any;
  private tooltip: any;

  constructor(private elRef: ElementRef, private viewContainerRef: ViewContainerRef) {
    this.elem = this.viewContainerRef.element.nativeElement;
  }

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
    const margin = { top: 20, right: 20, bottom: 40, left: 60 };
    const chartWidth = d3.select(this.elem).select('.chart-container').node().offsetWidth - margin.left - margin.right;
    const chartHeight = 300 - margin.top - margin.bottom - 50;

    // Extract unit from the dataGroup or set default
    const unit = this.data.map((d) => d.unit)[0] || 'hr'; //this.dataGroup?.unit ||

    // Append SVG container
    this.svg = d3
      .select(element)
      .append('svg')
      .attr('width', 300)
      .attr('height', 300)
      .append('g')
      .attr('transform', `translate(${margin.left}, ${margin.top})`);

    // Define scales
    const xScale = d3
      .scaleBand()
      .domain(this.data.map((d) => d.category))
      .range([0, 300])
      .padding(0.3);

    const yScale = d3
      .scaleLinear()
      .domain([0, d3.max(this.data, (d) => d.value) || 0])
      .nice()
      .range([chartHeight, 0]);

    // gridlines
    this.svg.selectAll('line.gridline').data(yScale.ticks(4)).enter()
      .append('svg:line')
      .attr('x1', 0)
      .attr('x2', chartWidth)
      .attr('y1', (d) => yScale(d))
      .attr('y2', (d) => yScale(d))
      .style('stroke', '#ccc')
      .style('stroke-width', 0.5)
      .style('fill', 'none')
      .attr('class', 'gridline');

    const colorScale = d3
      .scaleOrdinal()
      .domain(this.data.map((d) => d.category))
      .range(this.data.map((d) => d.color));

    // Add Y-axis label
    this.svg
      .append('text')
      .attr('transform', 'rotate(-90)')
      .attr('y', -margin.left + 10)
      .attr('x', -chartHeight / 2)
      // .attr('dy', '-1.5em')
      .style('text-anchor', 'middle')
      .style('font-size', '12px')
      .style('font-weight', 'bold')
      .text(unit); // Display the unit dynamically

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
      .append('path')
      .attr('class', 'bar')
      .attr('d', (d) => {
        const x = xScale(d.category) - 15; // Adjust to center the bar (since width is 30px)
        const y = yScale(d.value); // Starting y position of the bar
        const width = 30; // Fixed width of 30px
        const height = chartHeight - yScale(d.value); // Height of the bar
        const rx = 15;
        const ry = 15;

        // Create a custom path for the bar with rounded top corners and sharp bottom corners
        if (d.value) {
          return `M${x},${y + ry}
              a${rx},${ry} 0 0 1 ${rx},${-ry}
              h${width - 2 * rx}
              a${rx},${ry} 0 0 1 ${rx},${ry}
              v${height - 15}
              h${-(width)}Z`;
        } else {
          return ``;
        }
      })
      .attr('transform', (d) => { return `translate(${margin.left - 15}, ${0})` })
      .attr('fill', (d) => d.color)
      .on('mouseover', (event, d) => {
        this.tooltip
          .style('display', 'block')
          .html(
            `<strong>${d.category}:</strong> ${d.value}${unit === 'Count' ? '' : ` ${unit}`}`
          );
      })
      .on('mousemove', (event, d) => {
        console.log(d)
        this.tooltip
          .style('top', `${chartHeight - xScale.bandwidth()}px`)
          .style('left', `${xScale.bandwidth()}px`);
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
      .text((d) => `${d.value}${unit === 'Count' ? '' : 'hr'}`); // Add unit dynamically

    // Add axes
    this.svg
      .append('g')
      .attr('class', 'xAxisG')
      .attr('transform', `translate(0, ${chartHeight})`)
      .call(d3.axisBottom(xScale));

    this.svg
      .append('g')
      .attr('class', 'yAxisG')
      .call(
        d3.axisLeft(yScale)
          .ticks(5)
          .tickFormat((d) => `${d}${unit === 'Count' ? '' : 'hr'}`) // Add unit dynamically this.data.map((d) => d.unit)[0] ||
      );

    this.svg.selectAll('.xAxisG path, .xAxisG line, .yAxisG path, .yAxisG line')
      .attr('stroke', '#ccc');

    this.svg.selectAll('.yAxisG .domain')
      .style('display', 'none');
  }

  private updateChart(): void {
    // Clear previous chart
    d3.select(this.elRef.nativeElement).select('.chart-container').html('');
    this.createChart();
  }

}
