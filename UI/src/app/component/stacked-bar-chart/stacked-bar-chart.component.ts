import {
  Component,
  ElementRef,
  Input,
  OnChanges,
  OnInit,
  SimpleChanges,
  ViewContainerRef,
} from '@angular/core';
import * as d3 from 'd3';

@Component({
  selector: 'app-stacked-bar-chart',
  templateUrl: './stacked-bar-chart.component.html',
  styleUrls: ['./stacked-bar-chart.component.css'],
})
export class StackedBarChartComponent implements OnInit, OnChanges {
  @Input() data: any[] = []; // Data to be passed from parent component
  @Input() width;
  @Input() height;
  private svg: any;
  private tooltip: any;
  elem;

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
    const chartHeight = 30; // Height of the bar
    const margin = { top: 50, right: 20, bottom: 50, left: 20 };
    const chartWidth = (d3.select(this.elem).select('.chart-container').node().offsetWidth - margin.left - margin.right) || window.innerWidth; // Adjusted width to fit within the card
    const radius = 12.5;
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
    d3.select(this.elRef.nativeElement).selectAll('svg').remove();

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
      .attr('transform', `translate(${margin.left}, ${margin.top + 55})`)
      .attr('class', 'xAxisG')
      .call(xAxis)
      .selectAll('text')
      .style('font-size', '12px')

    // Create vertical gridlines
    const gridlines = svg.append("g")
      .attr("class", "gridlines");

    // Add vertical gridlines
    gridlines.selectAll(".gridline")
      .data(xScale.ticks(10)) // Set the number of gridlines
      .enter()
      .append("line")
      .attr("class", "gridline")
      .attr("x1", d => xScale(d))
      .attr("x2", d => xScale(d))
      .attr("y1", -100)
      .attr("y2", 100)
      .attr("stroke", "#ccc") // Color of the gridlines
      .attr("stroke-width", 0.5)
      .attr('transform', `translate(${margin.left}, ${margin.top})`)
    // .attr("stroke-dasharray", "2,2"); // Optional: makes lines dashed

    // Draw the stacked bar chart
    let cumulativeOffset = 0;
    let nonZeroBars = this.data.filter(d => parseInt(d.value) > 0);
    const g = svg.selectAll('.slice')
      .data(this.data)
      .enter()
      .append('g')
      .attr('transform', (d, index) => {
        const offset = cumulativeOffset;
        cumulativeOffset += Math.abs(xScale(d.value) - xScale(0));
        const isNegative = d.value < 0;
        if (isNegative) {
          return `translate(${Math.abs(offset) + margin.left}, ${margin.top})`
        } else {
          if (index === 1 || index === 0) {
            return `translate(${Math.abs(offset) + margin.left}, ${margin.top})`
          } else {
            return `translate(${Math.abs(offset) + margin.left - radius}, ${margin.top})`
          }
        }
      })
      .attr('class', 'slice')
      .append('path')
      .attr("d", (d, index) => {
        const width = Math.abs(xScale(d.value) - xScale(0));
        const isNegative = Math.abs(d.value) && d.value < 0;
        if (isNegative) {
          // Rounded corners on the left side
          return `M${radius},0
            H${width} 
            V${chartHeight} 
            H${radius}
            A${radius},${radius} 0 0 1 0,${chartHeight - radius}
            V${radius}
            A${radius},${radius} 0 0 1 ${radius},0
            Z`;
        } else if (Math.abs(d.value)) {
          if (nonZeroBars?.length === 1) {
            return `M${0},0
                H${width - radius} 
                A${radius},${radius} 0 0 1 ${width},${radius} 
                V${chartHeight - radius} 
                A${radius},${radius} 0 0 1 ${width - radius},${chartHeight} 
                H0 
                V0
                Z`;
          }
          if (index === 1 || index === 0) {
            return `M${-radius},0
                H${width - radius} 
                A${radius},${radius} 0 0 1 ${width},${radius} 
                V${chartHeight - radius} 
                A${radius},${radius} 0 0 1 ${width - radius},${chartHeight} 
                H0 
                V0
                Z`;
          } else {
            return `M${0},0
            C${radius},${chartHeight / 4} ${radius},${(3 * chartHeight) / 4} 0,${chartHeight}
            L${width - radius},${chartHeight} 
            C${width},${(3 * chartHeight) / 4} ${width},${chartHeight / 4} ${width - radius},0
            Z`;
          }
        } else if (d.value === 0) {
          return ``;
        }
      })
      .attr('fill', d => d.color);

    // Reset cumulative offset for labels
    cumulativeOffset = 0;

    // Add labels inside each section
    svg.selectAll('.slice')
      .append('text')
      .attr('x', d => {
        const width = Math.abs(xScale(d.value) - xScale(0));
        return width / 2;
      })
      .attr('y', chartHeight / 2)
      .style('fill', 'white')
      .style('font-size', '12px')
      .style('font-weight', 'bold')
      .attr('dominant-baseline', 'middle')
      .text(d => d.value);


    svg.selectAll('.xAxisG path, .xAxisG line')
      .attr('stroke', '#ccc');
  }

  private updateChart(): void {
    // Clear previous chart
    d3.select(this.elRef.nativeElement).select('.chart-container').html('');
    this.createChart();
  }
}
