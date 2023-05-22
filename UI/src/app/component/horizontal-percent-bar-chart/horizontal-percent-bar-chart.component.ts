import { Component, HostListener, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import * as d3 from 'd3';

@Component({
  selector: 'app-horizontal-percent-bar-chart',
  templateUrl: './horizontal-percent-bar-chart.component.html',
  styleUrls: ['./horizontal-percent-bar-chart.component.css']
})
export class HorizontalPercentBarChartComponent implements OnChanges {
  @Input() data=[];
  selectedGroup = '';

  constructor() { }

  @HostListener('window:resize')
  onWindowResize() {
    this.draw();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['data']) {
      this.data = this.data[0]['value'];
      this.draw();
    }
  }


  draw(){
    const chart = d3.select('#chart');
    chart.select('svg').remove();
    const margin = {top: 10, right: 22, bottom: 20, left: 100};
    const width = 485 - margin.left - margin.right;
    const height = 180 - margin.top - margin.bottom;

    // append the svg object to the body of the page
    const svg = chart
      .append("svg")
      .attr("width", width + margin.left + margin.right)
      .attr("height", height + margin.top + margin.bottom)
      .append("g")
      .attr("transform", `translate(${margin.left},${margin.top})`);


    const subgroups = Object.keys(this.data[0]['value']);
    const groups = this.data.map(d => d.kpiGroup);

    const y = d3.scaleBand()
      .domain(groups)
      .range([height, 0])
      .padding([0.4]);

    const x = d3.scaleLinear()
      .domain([0, 100])
      .range([0, width]);

    // Add X axis
    const xAxis = svg.append('g')
      .attr('transform', `translate(10, ${height})`)
      .call(d3.axisBottom(x).tickSize(0).tickFormat(d => d + '%').ticks(6));
    xAxis.selectAll('path')
      .style('display', 'none');

    //Add vertical gridlines for each x tick
    svg.append('g')
      .attr('transform', `translate(10, ${height})`).selectAll('line.gridline').data(x.ticks(6)).enter()
      .append('svg:line')
      .attr('x1', d => x(d))
      .attr('x2', d => x(d))
      .attr('y1', 0)
      .attr('y2', -height)
      .style('stroke', '#BDBDBD')
      .style('fill', 'none')
      .attr('class', 'gridline');

    // Add Y axis
    const yAxis = svg.append('g')
      .attr('calss', 'yAxis')
      .call(d3.axisLeft(y).tickSize(0));

    yAxis.selectAll('text')
      .style('font-size', '14px')
      .style('font-weight', 'bold');

    yAxis.select('path')
      .style('display', 'none')
      .style('font-family', 'inherit');

    // color palette = one color per subgroup
    const color = d3.scaleOrdinal()
      .domain(subgroups)
      .range(['#4472C4', '#F4AA46', '#7FBD7F']);

    // Normalize the data -> sum of each group must be 100!
    this.data.forEach((d) => {
      let tot = 0;
      for (const i in subgroups) {
        const name = subgroups[i];
        tot += +d['value'][name];
      }
      for (const i in subgroups) {
        const name = subgroups[i];
        d[name] = d['value'][name] / tot * 100;
      }
    });

    //stack the data? --> stack per subgroup
    const stackedData = d3.stack()
      .keys(subgroups)
      (this.data);

    let tooltipContainer;

    // Show the bars
    svg.append('g')
      .attr('transform', `translate(10, 0)`)
      .selectAll('g')
      .data(stackedData)
      .join('g')
      .attr('fill', d => color(d.key))
      .selectAll('rect')
      .data(d => d)
      .join('rect')
      .attr('x', d => x(d[0]))
      .attr('y', d => y(d.data.kpiGroup))
      .attr('width', d => x(d[1]) - x(d[0]))
      .attr('height', y.bandwidth())
      .style('cursor', 'pointer')
      .on('mouseover', (event, d) => {
        this.selectedGroup = d.data.kpiGroup;
        const tooltipData = stackedData.flat(1).filter(groupData => groupData.data.kpiGroup === this.selectedGroup);
        tooltipContainer = chart
          .select('.tooltip-container')
          .selectAll('div')
          .data(tooltipData)
          .join('div')
          .attr('class', 'tooltip')
          .style('left', d => {
            const percentVal = +(d[1] - d[0]).toFixed(0);
            return (x(d[0] + (percentVal - percentVal / 2))) + margin.left + 'px';
          })
          .style('top', d => y(d.data.kpiGroup) - 8 + 'px')
          .text(d => (d[1] - d[0]).toFixed(2).replace(/\.00$/, '') + '%')
          .transition()
          .duration(500)
          .style('display', d => (+(d[1] - d[0]).toFixed(2).replace(/\.00$/, '') > 0) ? 'block' : 'none')
          .style('opacity', 1);
      })
      .on('mouseout', (event, d) => {
        d3.select('#chart')
          .selectAll('.tooltip')
          .transition()
          .duration(1000)
          .style('display', 'none')
          .style('opacity', 0);
      });

    const legendDiv = d3.select('#chart').select('#legendContainer')
      .style('margin-top', '20px')
      .attr('width', width)
      .style('margin-left', margin.left + 'px');

    legendDiv.transition()
      .duration(200)
      .style('display', 'block')
      .style('opacity', 1)
      .style('width', width + 'px')
      .attr('class', 'p-d-flex p-flex-wrap normal-legend');

    let htmlString = '';

    subgroups.forEach((d, i) => {
      htmlString += `<div class="legend_item"><div class="legend_color_indicator" style="background-color: ${color(d)}"></div> <span class="p-m-1">: ${d}</span></div>`;
    });

    legendDiv.html(htmlString)
      .style('bottom', 60 + 'px');
  }
}
