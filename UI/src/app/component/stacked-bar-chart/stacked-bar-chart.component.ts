import { Component, ElementRef, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import * as d3 from 'd3';

@Component({
  selector: 'app-stacked-bar-chart',
  templateUrl: './stacked-bar-chart.component.html',
  styleUrls: ['./stacked-bar-chart.component.css']
})
export class StackedBarChartComponent implements OnInit,OnChanges {

  @Input() data: any[] = []; // Data to be passed from parent component
  @Input() width: number = 800; // Chart width
  @Input() height: number = 150; // Chart height
  
  

  private svg: any;
  private tooltip:any;

  constructor(private elRef: ElementRef) {}

  ngOnInit(): void {
    console.log(this.data)
    if (this.data && this.data.length) {
      this.createChart();
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.data && !changes.data.firstChange) {
      this.updateChart();
    }
  }
  
  private createChart1(): void {
    const element = this.elRef.nativeElement;
    const margin = { top: 20, right: 20, bottom: 50, left: 20 };
    const chartWidth = this.width - margin.left - margin.right;
    const chartHeight = this.height - margin.top - margin.bottom;

    // Append SVG
    this.svg = d3
      .select(element)
      .select('.chart-container')
      .append('svg')
      .attr('width', this.width)
      .attr('height', this.height + margin.top + margin.bottom)
      .append('g')
      .attr('transform', `translate(${margin.left}, ${margin.top})`);

    // Define scales
    const xScale = d3
      .scaleLinear()
      .domain([
        d3.min(this.data, (d: any) => d.value) || 0,
        d3.sum(this.data, (d: any) => Math.abs(d.value)) || 0
      ])
      .range([0, chartWidth]);

    const colorScale = d3
      .scaleOrdinal()
      .domain(this.data.map((d) => d.category))
      .range(this.data.map((d) => d.color));

    // Compute cumulative positions
    let cumulative = 0;
    const positions = this.data.map((d) => {
      const start = cumulative;
      cumulative += d.value;
      return { ...d, start, end: cumulative };
    });

    // Add bars
    this.svg
      .selectAll('rect')
      .data(positions)
      .enter()
      .append('rect')
      .attr('x', (d) => xScale(Math.min(d.start, d.end)))
      .attr('y', chartHeight / 3)
      .attr('width', (d) => Math.abs(xScale(d.end) - xScale(d.start)))
      .attr('height', chartHeight / 3)
      .attr('fill', (d) => colorScale(d.category));

    // Add labels inside the bars
    this.svg
      .selectAll('text')
      .data(positions)
      .enter()
      .append('text')
      .attr('x', (d) => xScale(d.start) + (xScale(d.end) - xScale(d.start)) / 2)
      .attr('y', chartHeight / 2.2)
      .attr('text-anchor', 'middle')
      .attr('fill', 'white')
      .style('font-size', '12px')
      .text((d) => d.value);

    // Add category labels below the chart
    this.svg
      .selectAll('.category-label')
      .data(positions)
      .enter()
      .append('text')
      .attr('x', (d) => xScale(d.start) + (xScale(d.end) - xScale(d.start)) / 2)
      .attr('y', chartHeight + 30)
      .attr('text-anchor', 'middle')
      .style('font-size', '12px')
      .text((d) => d.category);

    // Add x-axis
    const xAxis = d3.axisBottom(xScale).ticks(10);
    this.svg
      .append('g')
      .attr('transform', `translate(0, ${chartHeight})`)
      .call(xAxis);
  }

  private createChart(): void {
    const element = this.elRef.nativeElement;
    const margin = { top: 20, right: 20, bottom: 50, left: 20 };
    const chartWidth = this.width - margin.left - margin.right;
    const chartHeight = this.height - margin.top - margin.bottom;

    // Append SVG
    this.svg = d3
      .select(element)
      .select('.chart-container')
      .append('svg')
      .attr('width', this.width)
      .attr('height', this.height + margin.top + margin.bottom)
      .append('g')
      .attr('transform', `translate(${margin.left}, ${margin.top})`);

    // Define scales
    const xScale = d3
      .scaleLinear()
      .domain([
        d3.min(this.data, (d: any) => d.value) || 0,
        d3.sum(this.data, (d: any) => Math.abs(d.value)) || 0
      ])
      .range([0, chartWidth]);

    const colorScale = d3
      .scaleOrdinal()
      .domain(this.data.map((d) => d.category))
      .range(this.data.map((d) => d.color));

    // Create tooltip
    this.tooltip = d3
      .select(element)
      .append('div')
      .style('position', 'absolute')
      .style('background', 'rgba(0, 0, 0, 0.7)')
      .style('color', '#fff')
      .style('padding', '5px 10px')
      .style('border-radius', '5px')
      .style('display', 'none')
      .style('pointer-events', 'none');

    // Compute cumulative positions
    let cumulative = 0;
    const positions = this.data.map((d) => {
      const start = cumulative;
      cumulative += d.value;
      return { ...d, start, end: cumulative };
    });

    // Add bars
    this.svg
      .selectAll('rect')
      .data(positions)
      .enter()
      .append('rect')
      .attr('x', (d) => xScale(Math.min(d.start, d.end)))
      .attr('y', chartHeight / 3)
      .attr('width', (d) => Math.abs(xScale(d.end) - xScale(d.start)))
      .attr('height', chartHeight / 3)
      .attr('fill', (d) => colorScale(d.category))
      .on('mouseover', (event, d) => {
        this.tooltip
          .style('display', 'block')
          .html(`<strong>${d.category}</strong>`);
      })
      .on('mousemove', (event) => {
        this.tooltip
          .style('top', `${event.pageY - 30}px`)
          .style('left', `${event.pageX + 10}px`);
      })
      .on('mouseout', () => {
        this.tooltip.style('display', 'none');
      });

    // Add labels inside the bars
    this.svg
      .selectAll('text')
      .data(positions)
      .enter()
      .append('text')
      .attr('x', (d) => xScale(d.start) + (xScale(d.end) - xScale(d.start)) / 2)
      .attr('y', chartHeight / 2.2)
      .attr('text-anchor', 'middle')
      .attr('fill', 'white')
      .style('font-size', '12px')
      .text((d) => d.value);

    // Add x-axis
    const xAxis = d3.axisBottom(xScale).ticks(10);
    this.svg
      .append('g')
      .attr('transform', `translate(0, ${chartHeight})`)
      .call(xAxis);
  }


  private updateChart(): void {
    // Clear previous chart
    d3.select(this.elRef.nativeElement).select('.chart-container').html('');
    this.createChart();
  }


}



