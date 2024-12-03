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
    this.drawChart();
    this.drawFullCircle();
  }

  ngOnChanges(changes: SimpleChanges): void {
    // Check if the value input has changed
    if (changes.value) {
      this.drawChart();
    }
  }

  private drawChart(): void {
    const chartContainer = this.elementRef.nativeElement.querySelector('.chart-container');
    
    // Clear the container before redrawing
    d3.select(chartContainer).selectAll('*').remove();

    const radius = this.width / 2;
    const chartValue = Math.min(this.value / this.max, 1); // Cap value at 100%

    const containerWidth = chartContainer.offsetWidth;
    const containerHeight = chartContainer.offsetHeight;
    const radius1 = Math.min(containerWidth, containerHeight) / 2;

    // Create SVG element
    const svg = d3
      .select(chartContainer)
      .append('svg')
      .attr('width', this.width)
      .attr('height', this.height)
      .append('g')
      .attr('transform', `translate(${this.width / 2}, ${this.height})`);

    // Background Arc
    const arcBackground = d3.arc()
      .innerRadius(radius - 20)
      .outerRadius(radius)
      .startAngle(0)//.startAngle(-Math.PI / 2)
      .endAngle(2 * Math.PI);//.endAngle(Math.PI / 2);

    svg.append('path')
      .attr('d', arcBackground)
      .attr('fill', '#e6e6e6');

    // Foreground Arc (based on value)
    const arcForeground = d3.arc()
      .innerRadius(radius - 20)
      .outerRadius(radius)
      .startAngle(0)//.startAngle(-Math.PI / 2)
      .endAngle(2* Math.PI);// .endAngle(-Math.PI / 2 + chartValue * Math.PI);

    svg.append('path')
      .attr('d', arcForeground)
      .attr('fill', '#4f81bd');

    // Add Text in the Center
    svg.append('text')
      .attr('text-anchor', 'middle')
      .attr('dy', '-10')
      .attr('font-size', '24px')
      .attr('fill', '#333')
      .text(this.value);

    svg.append('text')
      .attr('text-anchor', 'middle')
      .attr('dy', '20')
      .attr('font-size', '16px')
      .attr('fill', '#666')
      .text('Total issues');
  }

  private drawFullCircle(): void {
    const chartContainer = this.elementRef.nativeElement.querySelector('.chart-container');
    const containerWidth = chartContainer.offsetWidth;
    const containerHeight = chartContainer.offsetHeight;
    const radius = Math.min(containerWidth, containerHeight) / 2;
    const chartValue = Math.min(this.value / this.max, 1); // Cap value at 100%
  
    // Clear existing content
    d3.select(chartContainer).selectAll('*').remove();
  
    // Create SVG element
    const svg = d3.select(chartContainer)
      .append('svg')
      .attr('width', containerWidth)
      .attr('height', containerHeight)
      .append('g')
      .attr('transform', `translate(${containerWidth / 2}, ${containerHeight / 2})`);
  
    // Create full circle arc
    const arc = d3.arc()
      .innerRadius(radius - 50)
      .outerRadius(radius)
      .startAngle(-Math.PI / 2)
      .endAngle(2 * Math.PI);
  
    // Append the full circle to the SVG
    svg.append('path')
      .attr('d', arc)
      .attr('fill', '#e6e6e6'); // Background color

      //test start
// Foreground Arc (based on value)
const arcForeground = d3.arc()
.innerRadius(radius - 20)
.outerRadius(radius)
.startAngle(-Math.PI / 2)
.endAngle(-Math.PI / 2 + chartValue * Math.PI);

svg.append('path')
.attr('d', arcForeground)
.attr('fill', '#4f81bd');
      //test end
  
    // Add text or other elements as needed
    svg.append('text')
      .attr('text-anchor', 'middle')
      .attr('dy', '-10')
      .attr('font-size', '24px')
      .attr('fill', '#333')
      .text(this.value);

    svg.append('text')
      .attr('text-anchor', 'middle')
      .attr('dy', '20')
      .attr('font-size', '16px')
      .attr('fill', '#666')
      .text('Total issues');
  }
}
