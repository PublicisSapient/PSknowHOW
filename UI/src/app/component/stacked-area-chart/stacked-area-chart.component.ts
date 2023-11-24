import { Component, Input, OnInit, SimpleChanges, ViewContainerRef } from '@angular/core';
import * as d3 from 'd3';

@Component({
  selector: 'app-stacked-area-chart',
  templateUrl: './stacked-area-chart.component.html',
  styleUrls: ['./stacked-area-chart.component.css']
})
export class StackedAreaChartComponent implements OnInit {
  @Input() data: any; // json data
  elem;
  @Input() kpiId:string = '';
  @Input() activeTab?: number = 0;
  elemObserver = new ResizeObserver(() => {this.draw()});
  constructor(private viewContainerRef: ViewContainerRef) { }

  ngOnInit(): void {}
  ngAfterViewInit(): void {
    this.elemObserver.observe(this.elem);
  }
  ngOnChanges(changes: SimpleChanges) {
    // only run when property "data" changed
    if (Object.keys(changes)?.length > 0) {
      if (changes['data']) {
        this.elem = this.viewContainerRef.element.nativeElement;
        this.draw();
      }
    }
    if(changes['activeTab']){
      setTimeout(() => {
        this.draw();
      }, 0);
    }
  }

  draw() {
    /** Preventing Drop event for Bubbling */
    d3.select(this.elem).select('#stacked-area').on('mousedown', (event) => {
      event.stopPropagation();
    });
    d3.select(this.elem).select('#stacked-area').select('svg').remove();
    let kpiId = this.kpiId;
    let keys = [];
    if(this.data[0]){
      keys = Object.keys(this.data[0]?.value);
    }
    let yMax = 0;
    let keyWiseYMax = {};
    for(let i = 0; i<keys.length;i++){
      keyWiseYMax[keys[i]] = 0;
    }
    /** calculating yMax and extracting keys */
    this.data.forEach((x) => {
      for(let item in x.value){
        if(keys.indexOf(item) == -1){
          keys.push(item);
          keyWiseYMax[item] = 0;
        }
        if(keyWiseYMax[item] < x.value[item]) keyWiseYMax[item] = x.value[item];
      }
    });
    for(let key in keyWiseYMax){
      yMax += keyWiseYMax[key];
    }
    yMax += 200;

    /**adding missing issues with value of 0 */
    const data = this.data.map((item) => {
      let dataItems = item?.value ? Object.keys(item?.value) : [];//['story', 'issues', 'change requests']
      let obj = {...item, ...item.value};
      if(keys?.length > dataItems?.length){
        let missingItems: any = [];
        missingItems = keys.filter((x) => !dataItems.includes(x))
        missingItems.forEach((k) => {
          obj[k] = 0
        });
      }
      delete obj['value'];
      return obj;
    });

    // set the dimensions and margins of the graph
    const margin = { top: 20, right: 20, bottom: 150, left: 50 },
      width = this.elem.offsetWidth ? this.elem.offsetWidth - 70 : 0,
      height = 228;

    // append the svg object to the body of the page
    const svg = d3.select(this.elem).select('#stacked-area')
      .append("svg")
      .attr("width", width + margin.left + margin.right)
      .attr("height", height + margin.top + margin.bottom)
      .append("g")
      .attr("transform",
        `translate(${margin.left}, ${margin.top})`);


      //////////
      // GENERAL //
      //////////

      // color palette
      const color = d3.scaleOrdinal()
        .domain(keys)
        .range(['#FFA193',
        '#00B1B0',
        '#FEC84D',
        '#E42256',
        '#7FBD7F',
        '#B79CED',
        '#5CA7CF',
        '#994636',
        '#E3D985',
        '#0072bb',
        '#DC0073',
        '#944075',
        '#80A9A2',
        '#E07373',
        '#6C4F84',
        '#BC2C1A',
        '#50723C',
        '#F17552',
        '#445E93',
        '#885053']);

      //stack the data?
      const stackedData = d3.stack()
        .keys(keys)
        (data)

      //////////
      // AXIS //
      //////////
      // Add X axis
      const x = d3.scaleTime()
        .domain(d3.extent(data, function (d) {return new Date(d.date);}))
        .range([0, width]);
      const xAxis = svg.append("g")
        .attr("transform", `translate(0, ${height})`)
        .call(d3.axisBottom(x).ticks(5))

      // Add X axis label:
      svg.append("text")
        .attr("text-anchor", "end")
        .attr("x", width)
        .attr("y", height + 40)
        .text("Time");

      // Add Y axis label:
      svg.append("text")
        .attr("text-anchor", "end")
        .attr("x", 0)
        .attr("y", -20)
        .text("")
        .attr("text-anchor", "start")

      // Add Y axis
      const y = d3.scaleLinear()
        .domain([0, yMax])
        .range([height, 0]);
      svg.append("g")
        .call(d3.axisLeft(y).ticks(5))


      //////////
      // BRUSHING AND CHART //
      //////////

      // Add a clipPath: everything out of this area won't be drawn.
      const clip = svg.append("defs").append("svg:clipPath")
        .attr("id", "clip")
        .append("svg:rect")
        .attr("width", width)
        .attr("height", height)
        .attr("x", 0)
        .attr("y", 0);

      // Add brushing
      const brush = d3.brushX()                 // Add the brush feature using the d3.brush function
        .extent([[0, 0], [width, height]])// initialise the brush area: start at 0,0 and finishes at width,height: it means I select the whole graph area
        .on("end", updateChart) // Each time the brush selection changes, trigger the 'updateChart' function

      // Create the scatter variable: where both the circles and the brush take place
      const areaChart = svg.append('g')
        .attr("clip-path", "url(#clip)")

      // Area generator
      const area = d3.area()
        .x(function (d) { return x(new Date(d.data.date)); })
        .y0(function (d) { return y(d[0]); })
        .y1(function (d) { return y(d[1]); })

      // Show the areas
      areaChart
        .selectAll("mylayers")
        .data(stackedData)
        .join("path")
        .attr("class", function (d) { return `${"myArea-"+kpiId}` + " " +d.key })
        .style("fill", function (d) { return color(d.key); })
        .attr("d", area)

      // Add the brushing
      areaChart
        .append("g")
        .attr("class", "brush")
        .call(brush);

      let idleTimeout
      function idled() { idleTimeout = null; }

      // A function that update the chart for given boundaries
      function updateChart(event, d) {

        const extent = event.selection

        // If no selection, back to initial coordinate. Otherwise, update X axis domain
        if (!extent) {
          if (!idleTimeout) return idleTimeout = setTimeout(idled, 350); // This allows to wait a little bit
          x.domain(d3.extent(data, function (d) { return new Date(d.date); }))
        } else {
          x.domain([x.invert(extent[0]), x.invert(extent[1])])
          areaChart.select(".brush").call(brush.move, null) // This remove the grey brush area as soon as the selection has been done
        }

        // Update axis and area position
        xAxis.transition().duration(1000).call(d3.axisBottom(x).ticks(5))
        areaChart
          .selectAll("path")
          .transition().duration(1000)
          .attr("d", area)
      }



      //////////
      // HIGHLIGHT GROUP //
      //////////

      // What to do when one group is hovered
      const highlight = function (event, d) {
        // reduce opacity of all groups
        d3.selectAll(".myArea-"+kpiId).style("opacity", .1)
        // expect the one that is hovered
        d3.select("." + d).style("opacity", 1)
      }

      // And when it is not hovered anymore
      const noHighlight = function (event, d) {
        d3.selectAll(".myArea-"+kpiId).style("opacity", 1)
      }

      //////////
      // LEGEND //
      //////////

      // Add one dot in the legend for each name.
      const foreignObject = svg.append("foreignObject")
      .attr("width", width)
      .attr("height", 40)
      .style('overflow-y', 'auto')
      .attr("transform", `translate(0,${(height+60)})`)
      .append("xhtml:div")
      .attr("id", "legend-container")
      .attr("class", "p-d-flex p-flex-wrap h-100");

      keys.forEach((x) => {
        foreignObject.append('div')
          .attr('class', 'p-d-flex p-align-center legend_item')
          .html(`<span class='rect' style='display:inline-block;width:10px; height:10px; margin: 0 5px 0 0; vertical-align: middle; background:${color(x)}'></span>
          <span style="text-transform: capitalize;">${x}</span>`)
          .on("mouseover", (event) => {highlight(event, x)})
          .on("mouseleave", (event) => {noHighlight(event, x)})
        })

      // const size = 20
      // svg.selectAll("myrect")
      //   .data(keys)
      //   .join("rect")
      //   .attr("x", function (d, i) { return 10 + i * (size + 5) })
      //   .attr("y", 300) // 100 is where the first dot appears. 25 is the distance between dots
      //   .attr("width", size)
      //   .attr("height", size)
      //   .style("fill", function (d) { return color(d) })
      //   .on("mouseover", highlight)
      //   .on("mouseleave", noHighlight)

      // // Add one dot in the legend for each name.
      // svg.selectAll("mylabels")
      //   .data(keys)
      //   .join("text")
      //   .attr("x", size * 1.2)
      //   .attr("y", function (d, i) { return 10 + i * (size + 5) + (size / 2) }) // 100 is where the first dot appears. 25 is the distance between dots
      //   .style("fill", function (d) { return color(d) })
      //   .text(function (d) { return d })
      //   .attr("text-anchor", "left")
      //   .style("alignment-baseline", "middle")
      //   .on("mouseover", highlight)
      //   .on("mouseleave", noHighlight)


  }



  ngOnDestroy(){
    d3.select(this.elem).select('#stacked-area').select('svg').remove();
    this.data = [];
    this.elemObserver.unobserve(this.elem);
  }
}
