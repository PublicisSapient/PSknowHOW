import { Component, Input, OnInit, ElementRef, SimpleChanges } from '@angular/core';
import * as d3 from 'd3';

@Component({
  selector: 'app-semi-circle-donut-chart',
  templateUrl: './semi-circle-donut-chart.component.html',
  styleUrls: ['./semi-circle-donut-chart.component.css']
})
export class SemiCircleDonutChartComponent implements OnInit {

  @Input() value: number = 0; // Value for the chart (e.g., 86 for 86%)
  @Input() max: number = 100; // Maximum value for the chart
  @Input() width: number = 200; // Width of the chart
  @Input() height: number = 100; // Height of the chart (half the width)

  constructor(private elementRef: ElementRef) {}

  ngOnInit(): void {
    this.createDonutChart();
  }

  ngOnChanges(changes: SimpleChanges): void {
    // Check if the value input has changed
    if (changes.value) {
      this.createDonutChart();
    }
  }

private createDonutChart(): void {
  const chartWidth = (this.width === undefined) ? 100 : this.width;; // Width of the chart
  const chartHeight = (this.height === undefined) ? 200 : this.height;; // Height of the chart
  const radius = Math.min(chartWidth, chartHeight) / 2; // Radius of the donut
  const thickness = Math.floor(radius/3); // Thickness of the donut ring
  // Clear existing SVG content
  d3.select(this.elementRef.nativeElement).selectAll('svg').remove();

  // Create the SVG container
  const svg = d3
    .select(this.elementRef.nativeElement)
    .append('svg')
    .attr('width', chartWidth)
    .attr('height', chartHeight)
    .append('g')
    .attr('transform', `translate(${chartWidth / 2}, ${chartHeight / 2})`); // Center the chart

  // Create arc generator
  const arc = d3.arc()
    .innerRadius(radius - thickness)
    .outerRadius(radius);

  // Create pie generator
  const pie = d3.pie()
    .sort(null)
    .value(d => d.value);

  // Define the data (completed and remaining)
  const data = [
    { value: this.value, color: '#4A6CF7' }, // Blue color for completed
    { value: this.max - this.value, color: '#E5EAF2' } // Gray color for remaining
  ];

  // Append the arcs
  const path = svg.selectAll('path')
    .data(pie(data))
    .enter()
    .append('path')
    .attr('d', arc)
    .attr('fill', d => d.data.color);

  // Add central text
  svg.append('text')
    .attr('text-anchor', 'middle')
    .attr('dy', '-0.5em') // Adjust text position
    .style('font-size', '14px')
    .style('fill', '#4A6CF7')
    .text('Total issues');

  svg.append('text')
    .attr('text-anchor', 'middle')
    .attr('dy', '1em') // Adjust text position
    .style('font-size', '24px')
    .style('font-weight', 'bold')
    .style('fill', '#4A6CF7')
    .text(this.value);
}

}
