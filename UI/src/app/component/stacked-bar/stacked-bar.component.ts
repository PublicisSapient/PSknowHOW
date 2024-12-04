import { Component, ElementRef, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';

import * as d3 from 'd3';

@Component({
  selector: 'app-stacked-bar',
  templateUrl: './stacked-bar.component.html',
  styleUrls: ['./stacked-bar.component.css']
})
export class StackedBarComponent implements OnInit, OnChanges {
  @Input() data: any[] = []; // Data to be passed from parent component
  @Input() width;
  @Input() height;

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
    const element = this.elRef.nativeElement;
    const chartWidth = this.width; // Total width of the chart
    const chartHeight = this.height; // Height of the bar
    const margin = { top: 40, right: 20, bottom: 20, left: 20 };
    const totalValue = this.data.reduce((sum, d) => sum + d.value, 0); // Total value of all sections
  
    // Normalize data for percentage
    const normalizedData = this.data.map(d => ({
      ...d,
      percentage: (d.value / totalValue) * 100,
    }));
  
    // Clear any previous SVG content
    d3.select(element).selectAll('*').remove();
  
    // Create the SVG container
    const svg = d3
      .select(element)
      .append('svg')
      .attr('width', chartWidth + margin.left + margin.right)
      .attr('height', chartHeight + margin.top + margin.bottom);

  
    // Draw the main bar (stacked sections)
    const g = svg
      .append('g')
      .attr('transform', `translate(${margin.left}, ${margin.top})`);
  
    let cumulativeWidth = 0;
  
    g.selectAll('rect')
      .data(normalizedData)
      .enter()
      .append('rect')
      .attr('x', d => {
        const x = cumulativeWidth;
        cumulativeWidth += (chartWidth * d.percentage) / 100;
        return x;
      })
      .attr('y', 0)
      .attr('width', d => (chartWidth * d.percentage) / 100) // Proportional width
      .attr('height', chartHeight)
      .attr('fill', d => d.color) // Dynamic fill color
      // .attr('rx', 25) // Rounded corners
      // .attr('ry', 25); // Rounded corners
  
    // Add labels inside each section
    cumulativeWidth = 0; // Reset cumulativeWidth for labels
    g.selectAll('text')
      .data(normalizedData)
      .enter()
      .append('text')
      .attr('x', d => {
        const x = cumulativeWidth + (chartWidth * d.percentage) / 200;
        cumulativeWidth += (chartWidth * d.percentage) / 100;
        return x;
      })
      .attr('y', chartHeight / 2)
      .style('fill', 'white')
      .style('font-size', '14px')
      .style('font-weight', 'bold')
      .attr('text-anchor', 'middle')
      .attr('dominant-baseline', 'middle')
      .text(d => `${d.value}d`);
  }
  
  private updateChart(): void {
    // Clear previous chart
    d3.select(this.elRef.nativeElement).select('.chart-container').html('');
    this.createChart();
  }
}
