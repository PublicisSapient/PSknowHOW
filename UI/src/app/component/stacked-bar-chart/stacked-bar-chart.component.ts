import {
  Component,
  ElementRef,
  Input,
  OnChanges,
  OnInit,
  SimpleChanges,
} from '@angular/core';
import * as d3 from 'd3';

@Component({
  selector: 'app-stacked-bar-chart',
  templateUrl: './stacked-bar-chart.component.html',
  styleUrls: ['./stacked-bar-chart.component.css'],
})
export class StackedBarChartComponent implements OnInit, OnChanges {
  @Input() data: any[] = []; // Data to be passed from parent component
  @Input() width: number = 800; // Chart width
  @Input() height: number = 100; // Chart height

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
    const chartWidth = 700; // Adjusted width to fit within the card
    const chartHeight = 50; // Height of the bar
    const margin = { top: 50, right: 20, bottom: 50, left: 20 };
  
    // Calculate total value for scaling
    const totalNegative = Math.abs(
      this.data.filter(d => d.value < 0).reduce((sum, d) => sum + d.value, 0)
    );
    const totalPositive = this.data
      .filter(d => d.value > 0)
      .reduce((sum, d) => sum + d.value, 0);
    const total = totalNegative + totalPositive;
  
    // X scale for proportional widths
    const xScale = d3
      .scaleLinear()
      .domain([-totalNegative, totalPositive])
      .range([0, chartWidth]);
  
    // Clear any existing SVG content
    d3.select(this.elRef.nativeElement).selectAll('*').remove();
  
    // Create the SVG container
    const svg = d3
      .select(this.elRef.nativeElement)
      .append('svg')
      .attr('width', chartWidth + margin.left + margin.right)
      .attr('height', chartHeight + margin.top + margin.bottom);
  
    // Add axis for context
    const xAxis = d3.axisBottom(xScale).ticks(10);
    svg
      .append('g')
      .attr('transform', `translate(${margin.left}, ${chartHeight + margin.top})`)
      .call(xAxis)
      .selectAll('text')
      .style('font-size', '12px');
  
    // Draw the stacked bar chart
    let cumulativeOffset = 0;
  
    const g = svg
      .append('g')
      .attr('transform', `translate(${margin.left}, ${margin.top})`);
  
    g.selectAll('rect')
      .data(this.data)
      .enter()
      .append('rect')
      .attr('x', d => {
        const offset = cumulativeOffset;
        cumulativeOffset += Math.abs(xScale(d.value) - xScale(0));
        return Math.abs(offset);
      })
      .attr('y', 0)
      .attr('width', d => Math.abs(xScale(d.value) - xScale(0)))
      .attr('height', chartHeight)
      .attr('fill', d => d.color)
      // .attr('rx', 10) // Rounded corners
      // .attr('ry', 10); // Rounded corners
  
    // Reset cumulative offset for labels
    cumulativeOffset = 0;
  
    // Add labels inside each section
    g.selectAll('text')
      .data(this.data)
      .enter()
      .append('text')
      .attr('x', d => {
        const offset = cumulativeOffset + Math.abs(xScale(d.value) - xScale(0)) / 2;
        cumulativeOffset += xScale(d.value) - xScale(0);
        return offset;
      })
      .attr('y', chartHeight / 2)
      .style('fill', 'white')
      .style('font-size', '14px')
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
