import { Component, ElementRef, Input, OnChanges, OnInit, SimpleChanges, ViewContainerRef } from '@angular/core';

import * as d3 from 'd3';

@Component({
  selector: 'app-stacked-bar',
  templateUrl: './stacked-bar.component.html'
})
export class StackedBarComponent implements OnInit, OnChanges {
  @Input() data: any[] = []; // Data to be passed from parent component
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
    const element = this.elRef.nativeElement;
    const chartHeight = 30; // Height of the bar
    const radius = 12.5; // Radius for rounded corners
    const margin = { top: 40, right: 20, bottom: 20, left: 20 };

    // Calculate chart width dynamically
    const chartWidth =
      (d3.select(this.elem).select('.chart-container').node().offsetWidth - margin.left - margin.right) || window.innerWidth;

    // Filter out categories with zero value
    const filteredData = this.data.filter(d => d.value !== 0);

    // If all values are zero, return early
    if (filteredData.length === 0) {
      console.warn('No valid data to render');
      // return;
    }

    // Calculate total value for percentage normalization
    const totalValue = filteredData.reduce((sum, d) => sum + d.value, 0);

    // Normalize data for percentage
    const normalizedData = filteredData.map(d => ({
      ...d,
      percentage: (d.value / totalValue) * 100,
    }));

    // Clear any existing SVG content
    d3.select(this.elRef.nativeElement).selectAll('svg').remove();

    // Create the SVG container
    const svg = d3
      .select(element)
      .append('svg')
      .attr('width', chartWidth + margin.left + margin.right)
      .attr('height', chartHeight + margin.top + margin.bottom);

    // Group for chart
    const g = svg
      .append('g')
      .attr('transform', `translate(${margin.left}, ${margin.top})`);

    let cumulativeWidth = 0;

    // Draw the stacked sections with rounded corners
    g.selectAll('path')
      .data(normalizedData)
      .enter()
      .append('path')
      .attr('d', (d, i) => {
        const width = (chartWidth * d.percentage) / 100;
        const x = cumulativeWidth;
        cumulativeWidth += width;

        // Rounded rectangle path logic
        if (!(i % 2)) {
          return `M${x + radius},0
                H${x + width - radius}
                A${radius},${radius} 0 0 1 ${x + width},${radius}
                V${chartHeight - radius}
                A${radius},${radius} 0 0 1 ${x + width - radius},${chartHeight}
                H${x + radius}
                A${radius},${radius} 0 0 1 ${x},${chartHeight - radius}
                V${radius}
                A${radius},${radius} 0 0 1 ${x + radius},0
                Z`;
        } else {
          // return `M${0},0
          // C${radius},${chartHeight / 4} ${radius},${(3 * chartHeight) / 4} 0,${chartHeight}
          // L${width - radius},${chartHeight} 
          // C${width},${(3 * chartHeight) / 4} ${width},${chartHeight / 4} ${width - radius},0
          // Z`;

          return `M${x - radius},0
        C${x + radius},${chartHeight / 4} ${x },${(4 * chartHeight) / 4} ${x - radius},${chartHeight}
        L${x + width - radius},${chartHeight} 
        C${x + width},${(3 * chartHeight) / 4} ${x + width},${chartHeight / 4} ${x + width - radius},0
        Z`;


        }
      })
      .attr('fill', d => d.color);

    // Reset cumulativeWidth for labels
    cumulativeWidth = 0;

    // Add labels inside each section
    g.selectAll('text')
      .data(normalizedData)
      .enter()
      .append('text')
      .attr('x', d => {
        const width = (chartWidth * d.percentage) / 100;
        const x = cumulativeWidth + width / 2;
        cumulativeWidth += width;
        return x;
      })
      .attr('y', chartHeight / 2)
      .style('fill', 'white')
      .style('font-size', '12px')
      .style('font-weight', 'bold')
      .attr('text-anchor', 'middle')
      .attr('dominant-baseline', 'middle')
      .text(d => d.value);
  }

  private updateChart(): void {
    // Clear previous chart
    d3.select(this.elRef.nativeElement).select('.chart-container').html('');
    this.createChart();
  }
}
