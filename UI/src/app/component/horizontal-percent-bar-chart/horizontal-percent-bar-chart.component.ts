import { Component, HostListener, Input, OnChanges, SimpleChanges, Type, ViewChild, ViewContainerRef } from '@angular/core';
import * as d3 from 'd3';
declare const require: any;

@Component({
  selector: 'app-horizontal-percent-bar-chart',
  templateUrl: './horizontal-percent-bar-chart.component.html',
  styleUrls: ['./horizontal-percent-bar-chart.component.css']
})


export class HorizontalPercentBarChartComponent implements OnChanges {
  @Input() data = [];
  unmodifiedDataCopy = [];
  @Input() isDrilledDown: boolean = false;
  selectedGroup = '';
  @ViewChild('popupHost', { read: ViewContainerRef })
  popupHost!: ViewContainerRef;
  elem: any;
  constructor(public viewContainerRef: ViewContainerRef) { }

  @HostListener('window:resize')
  onWindowResize() {
    this.draw(this.data);
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['data']) {
      this.isDrilledDown = false;
      this.elem = this.viewContainerRef.element.nativeElement;
      if (!this.isDrilledDown) {
        this.data = this.data[0]['value'];
        this.unmodifiedDataCopy = JSON.parse(JSON.stringify(this.data));
      }
      this.draw(this.data);
      d3.select('.tooltip-chart-container').select('app-horizontal-percent-bar-chart').remove();
    }
  }

  draw(data, selectedNode = '') {
    let self = this;
    const elem = this.elem;
    let chartContainerWidth = (document.getElementById('chart')?.offsetWidth ? document.getElementById('chart')?.offsetWidth : 485);
    chartContainerWidth = chartContainerWidth <= 490 ? chartContainerWidth : chartContainerWidth - 70;
    const chart = d3.select(elem).select('#chart');
    chart.select('.chart-container').select('svg').remove();
    chart.select('.chart-container').remove();
    const margin = { top: 10, right: 22, bottom: 20, left: 100 };
    const width = chartContainerWidth - margin.left - margin.right;
    const height = !this.isDrilledDown ? 180 - margin.top - margin.bottom : 100;

    // append the svg object to the body of the page
    const svg = chart
      .append('div')
      .attr('class', 'chart-container')
      .append("svg")
      .attr("width", width + margin.left + margin.right)
      .attr("height", height + margin.top + margin.bottom)
      .append("g")
      .attr("transform", `translate(${margin.left},${margin.top})`);


    // let subgroups = Object.keys(data[0]['value']);
    const groups = this.isDrilledDown ? ['(' + selectedNode['data']['kpiGroup'] + ')', data.map(d => d.kpiGroup)[0]] : data.map(d => d.kpiGroup);
    let subgroups = [];
    if (!this.isDrilledDown) {
      data[0]['value'].forEach((element) => {
        subgroups.push(element['subFilter']);
      });
    } else {
      data[0].forEach((element) => {
        subgroups.push(element['subFilter']);
      });
    }

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
      .attr('class', 'yAxis')
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
      .range(subgroups.length > 3 ? d3.schemePaired : ['#4472C4', '#F4AA46', '#7FBD7F'])
      .unknown("#ccc");

    // Normalize the data -> sum of each group must be 100!
    if (!self.isDrilledDown) {
      data.forEach((d) => {
        let tot = 0;
        for (const i in subgroups) {
          const name = subgroups[i];
          tot += +d['value'].filter((x) => x.subFilter === name)[0]['value'];
        }
        for (const i in subgroups) {
          const name = subgroups[i];
          if (tot > 0) {
            d[name] = d['value'].filter((x) => x.subFilter === name)[0]['value'] / tot * 100;
          } else {
            d[name] = 0;
          }
        }
      });
    } else {
      data.forEach((d) => {
        let tot = 0;
        for (const i in subgroups) {
          const name = subgroups[i];
          tot += +d.filter((x) => x.subFilter === name)[0]['value'];
        }
        for (const i in subgroups) {
          const name = subgroups[i];
          d[name] = d.filter((x) => x.subFilter === name)[0]['value'] / tot * 100;
        }
      });
    }

    //stack the data? --> stack per subgroup
    const stackedData = d3.stack()
      .keys(subgroups)
      (data);

    // Show the bars
    svg.append('g')
      .attr('transform', `translate(10, 0)`)
      .selectAll('g')
      .data(stackedData)
      .join('g')
      .attr('fill', d => {
        return color(d.key);
      })
      .on('click', (event, d) => {
        if (!self.isDrilledDown) {
          self.isDrilledDown = true;
          let key = d['key'];
          let kpiGroup = event.target.__data__.data.kpiGroup;
          let selectedNode = d.filter((x) => x.data['kpiGroup'] === kpiGroup)[0];
          data = [selectedNode.data.value.filter((val) => val.subFilter === key)[0].drillDown];
          this.draw(data, selectedNode);
          d3.select(elem).select('#back_icon').style('display', 'block')
            .on('click', (event, d) => {
              this.isDrilledDown = false;
              this.draw(this.unmodifiedDataCopy);
              d3.select(elem).select('#back_icon').style('display', 'none');
            });
        }
      })
      .selectAll('rect')
      .data(d => d)
      .join('rect')
      .attr('x', d => x(d[0]))
      .attr('y', d => y(d.data.kpiGroup))
      .on('mouseover', (event, d) => {
        this.selectedGroup = d.data.kpiGroup;
        if (!this.isDrilledDown) {
          const tooltipData = data.filter(tooltip => tooltip.kpiGroup === this.selectedGroup)[0];
          d3.select(elem).select('#chart').select('#legendContainer').selectAll('div').remove();
          this.showTooltip(subgroups, width, margin, color, tooltipData, elem, height);
        }
      })
      .on('mouseout', (event, d) => {
        if (!this.isDrilledDown) {
          d3.select(elem).select('#chart').select('#legendContainer').selectAll('div').remove();
          this.showLegend(subgroups, width, margin, color, elem, data, height);
        }
      })
      .attr('height', y.bandwidth());

    if (this.isDrilledDown) {
      svg.selectAll('rect')
        .transition()
        .ease(d3.easeLinear)
        .duration(200)
        .delay(function (d, i) { return i * 200 }) //a different delay for each rect
        .attr('width', d => x(d[1]) - x(d[0]))
        .style('cursor', this.isDrilledDown ? 'default' : 'pointer')
    } else {
      svg.selectAll('rect')
        .attr('width', d => x(d[1]) - x(d[0]))
        .style('cursor', this.isDrilledDown ? 'default' : 'pointer')
    }



    this.showLegend(subgroups, width, margin, color, elem, data, height);
  }

  showLegend(subgroups, width, margin, color, elem, data, height) {
    let legendDiv;
    legendDiv = d3.select(elem).select('#chart').select('#legendContainer');

    legendDiv
      .style('margin-top', '20px')
      .attr('width', 'auto')
      .style('margin-left', 50 + 'px')
      .transition()
      .duration(200)
      .style('display', 'block')
      .style('opacity', 1)
      .style('width', 'auto')
      .attr('class', 'p-d-flex p-flex-wrap normal-legend');

    let htmlString = '';
    if (!this.isDrilledDown) {
      subgroups.forEach((d, i) => {
        htmlString += `<div class="legend_item"><div class="legend_color_indicator" style="background-color: ${color(d)}"></div> <span class="p-m-1" style="font-weight:bold">: ${d}</span></div>`;
      });
    } else {
      subgroups.forEach((d, i) => {
        htmlString += `<div class="legend_item"><div class="legend_color_indicator" style="background-color: ${color(d)}"></div> <span class="p-m-1" style="font-size: 10px; font-weight:bold;">: ${d} 
        (${data[0].filter((val) => val.subFilter === d)[0]['value']} | <span style="font-weight:bold">${data[0][d].toFixed(2).replace(/\.00$/, '')}%</span>)
        </span></div>`;
      });
    }

    legendDiv.html(htmlString)
    legendDiv.style('bottom', 60 + 'px');
  }

  showTooltip(subgroups, width, margin, color, tooltipData, elem, height) {
    const legendDiv = d3.select(elem).select('#chart').select('#legendContainer')
      .style('margin-top', '20px')
      .attr('width', 'auto')
      .style('margin-left', 50 + 'px');

    legendDiv.transition()
      .duration(200)
      .style('display', 'block')
      .style('opacity', 1)
      .style('width', 'auto')
      .attr('class', 'p-d-flex p-flex-wrap normal-legend');

    let htmlString = '';

    subgroups.forEach((d, i) => {
      htmlString += `<div class="legend_item" style="flex-direction:column; align-items:start; margin-right:1.5rem">
                    <div style="margin-bottom:0.5rem" class="p-d-flex p-align-center">
                      <div class="legend_color_indicator" style="background-color: ${color(d)}">
                      </div>
                      <span class="p-m-1" style="font-weight:bold">: ${d}</span>
                    </div>
                    <div style="font-size: 0.75rem;">${tooltipData.kpiGroup}: <span style="color:${color(d)}; font-weight:bold">${tooltipData['value'].filter((x) => x.subFilter === d)[0]['value']}</span></div>
                    <div style="font-size: 0.75rem;">Percentage: <span style="color:${color(d)} ; font-weight:bold">${tooltipData[d].toFixed(2).replace(/\.00$/, '')}%</span></div>
                    </div>`;
    });

    legendDiv.html(htmlString)
      .style('bottom', 60 + 'px');
  }

  // Required for dynamic component only; not in use right now
  showDistributionChartOnTooltip(event, d) {
    let variableWidthOffset = document.documentElement.clientWidth > 1500 ? -200 : 200;
    // position the tooltip
    const xPosition = event.screenX;
    const yPosition = event.screenY;
    d3.select('.tooltip-chart-container')
      .style('display', 'block')
      .style('height', 'auto')
      .style('min-height', '280px')
      .style('position', 'absolute')
      .style('width', 'auto')
      .style('top', -yPosition + 450 + 'px')
      .style('left', xPosition - variableWidthOffset + 'px')
      .style('padding-right', '50px')
      .style('z-index', '1001');
    // Create component dynamically inside the ng-template
    this.popupHost.clear();
    const popupComponentRef = this.popupHost.createComponent(HorizontalPercentBarChartComponent);
    popupComponentRef.setInput('data', [d]);
    popupComponentRef.setInput('isOnTooltip', true);
  }
}
