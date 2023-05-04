import { Component, OnInit } from '@angular/core';
import * as d3 from 'd3';

@Component({
  selector: 'app-stacked-area-chart',
  templateUrl: './stacked-area-chart.component.html',
  styleUrls: ['./stacked-area-chart.component.css']
})
export class StackedAreaChartComponent implements OnInit {

  constructor() { }

  ngOnInit(): void {
    this.draw();
  }

  draw() {
    // set the dimensions and margins of the graph
    const margin = { top: 10, right: 30, bottom: 30, left: 60 },
      width = 460 - margin.left - margin.right,
      height = 400 - margin.top - margin.bottom;

    // append the svg object to the body of the page
    const svg = d3.select("#my_dataviz")
      .append("svg")
      .attr("width", width + margin.left + margin.right)
      .attr("height", height + margin.top + margin.bottom)
      .append("g")
      .attr("transform",
        `translate(${margin.left}, ${margin.top})`);

    //Read the data
    d3.csv("https://raw.githubusercontent.com/holtzy/data_to_viz/master/Example_dataset/5_OneCatSevNumOrdered.csv").then(function (data) {

      // group the data: one array for each value of the X axis.
      const sumstat = d3.group(data, d => d.year);

      // Stack the data: each group will be represented on top of each other
      const mygroups = ["Helen", "Amanda", "Ashley"] // list of group names
      const mygroup = [1, 2, 3] // list of group names
      const stackedData = d3.stack()
        .keys(mygroup)
        .value(function (d, key) {
          return d[1][key].n
        })
        (sumstat)

      // Add X axis --> it is a date format
      const x = d3.scaleLinear()
        .domain(d3.extent(data, function (d) { return d.year; }))
        .range([0, width]);
      svg.append("g")
        .attr("transform", `translate(0, ${height})`)
        .call(d3.axisBottom(x).ticks(5));

      // Add Y axis
      const y = d3.scaleLinear()
        .domain([0, d3.max(data, function (d) { return +d.n; }) * 1.2])
        .range([height, 0]);
      svg.append("g")
        .call(d3.axisLeft(y));

      // color palette
      const color = d3.scaleOrdinal()
        .domain(mygroups)
        .range(['#e41a1c', '#377eb8', '#4daf4a', '#984ea3', '#ff7f00', '#ffff33', '#a65628', '#f781bf', '#999999'])

      // Show the areas
      svg
        .selectAll("mylayers")
        .data(stackedData)
        .join("path")
        .style("fill", function (d) { const name = mygroups[d.key - 1]; return color(name); })
        .attr("d", d3.area()
          .x(function (d, i) { return x(d.data[0]); })
          .y0(function (d) { return y(d[0]); })
          .y1(function (d) { return y(d[1]); })
        )
    })
  }


}
