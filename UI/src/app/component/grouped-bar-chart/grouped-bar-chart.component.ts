import { Component, ElementRef, Input, OnInit, SimpleChanges, ViewContainerRef } from '@angular/core';
import * as d3 from 'd3';

@Component({
  selector: 'app-grouped-bar-chart',
  templateUrl: './grouped-bar-chart.component.html',
  styleUrls: ['./grouped-bar-chart.component.css']
})
export class GroupedBarChartComponent implements OnInit {
  @Input() data: any[] = [];
  @Input() filters: any[] = [];
  elem;
  color: any[] = [];
  constructor(private elRef: ElementRef, private viewContainerRef: ViewContainerRef) {
    this.elem = this.viewContainerRef.element.nativeElement;
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.data && !changes.data.firstChange) {
      this.updateChart();
    }
  }

  ngOnInit(): void {
    this.draw();
  }

  private updateChart(): void {
    // Clear previous chart
    d3.select(this.elRef.nativeElement).select('#chart').html('');
    this.draw();
  }


  draw() {
    // Sample data: two values for each category
    const data = this.data['data'];

    // Chart dimensions
    const margin = { top: 20, right: 30, bottom: 40, left: 50 };
    const width = d3.select(this.elem).select('#chart').node().offsetWidth - margin.left - margin.right;
    const height = 250 - margin.top - margin.bottom;

    // Create SVG container
    const svg = d3.select(this.elem).select('#chart')
      .append("svg")
      .attr("width", width + margin.left + margin.right)
      .attr("height", height + margin.top + margin.bottom)
      .append("g")
      .attr("transform", `translate(${margin.left},${margin.top})`);

    // Define scales
    const x0 = d3.scaleBand() // Main x-axis scale for categories
      .domain(data.map(d => d.category))
      .range([0, width])
      .padding(0.05); // Reduced outer padding (bars closer between categories)

    const x1 = d3.scaleBand() // Inner scale for grouping bars
      .domain(["value1", "value2"])
      .range([0, x0.bandwidth()])
      .padding(0.02); // Reduced inner padding (bars closer within a group)

    const y = d3.scaleLinear() // Y-axis scale
      .domain([0, d3.max(data, d => Math.max(d.value1, d.value2))])
      .nice()
      .range([height, 0]);

    // Add x-axis
    svg.append("g")
      .attr('class', 'xAxisG')
      .attr("transform", `translate(0,${height})`)
      .call(d3.axisBottom(x0));

    // Add y-axis
    svg.append("g")
      .attr('class', 'yAxisG')
      .call(d3.axisLeft(y));

    // gridlines
    svg.selectAll('line.gridline').data(y.ticks(4)).enter()
      .append('svg:line')
      .attr('x1', 0)
      .attr('x2', width)
      .attr('y1', (d) => y(d))
      .attr('y2', (d) => y(d))
      .style('stroke', '#ccc')
      .style('stroke-width', 0.5)
      .style('fill', 'none')
      .attr('class', 'gridline');

    const barWidth = 30; // Fixed bar width
    const radius = barWidth / 2; // Corner radius for rounded tops
    // Draw bars for both groups
    const groups = svg.selectAll(".group")
      .data(data)
      .enter()
      .append("g")
      .attr("transform", d => `translate(${x0(d.category)}, 0)`);

    // Function to create rounded top path
    const roundedTopPath = (x, y, width, height, radius) => {
      return `M${x},${y + radius}
                A${radius},${radius} 0 0 1 ${x + radius},${y}
                H${x + width - radius}
                A${radius},${radius} 0 0 1 ${x + width},${y + radius}
                V${y + height}
                H${x}
                Z`;
    };

    const nonRoundedTopPath = (x, y, width, height) => {
      return `M${x},${y}
              H${x + width}
              V${y + height}
              H${x}
              Z`;
    };

    // Bar 1 (value1)
    groups.append("path")
      .attr("class", "bar")
      .attr("d", d => {
        if (height - y(d.value1) >= radius) {
          return roundedTopPath(
            x1("value2") - 40, y(d.value1), barWidth, height - y(d.value1), radius
          )
        } else {
          return nonRoundedTopPath(
            x1("value2") - 40, y(d.value1), barWidth, height - y(d.value1)
          );
        }
      })
      .attr('fill', (d, i) => d.color[0]);

    // Bar 2 (value2)
    groups.append("path")
      .attr("class", "bar2")
      .attr("d", d => {
        if (height - y(d.value2) >= radius) {
          return roundedTopPath(
            x1("value2") + 10, y(d.value2), barWidth, height - y(d.value2), radius
          );
        } else {
          return nonRoundedTopPath(
            x1("value2") + 10, y(d.value2), barWidth, height - y(d.value2)
          );
        }
      })
      .attr('fill', (d, i) => d.color[1]);

    svg.selectAll('.yAxisG path, .yAxisG line, .xAxisG path, .xAxisG line')
      .attr('stroke', '#ccc');

    svg.selectAll('.yAxisG .domain, .yAxisG .tick:nth-child(odd)')
      .style('display', 'none');
  }
}
