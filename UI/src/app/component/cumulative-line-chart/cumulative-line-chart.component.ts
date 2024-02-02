import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import * as d3 from 'd3';

@Component({
  selector: 'app-cumulative-line-chart',
  templateUrl: './cumulative-line-chart.component.html',
  styleUrls: ['./cumulative-line-chart.component.css']
})
export class CumulativeLineChartComponent implements OnInit,OnChanges {
  @Input() data;
  @Input() kpiId;
  @Input() xCaption;
  @Input() yCaption;
  currentDayIndex;
  graphData;

  constructor() { }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges) {
    this.graphData= this.data[0]['dataGroup'].map(d => ({...d}));
    this.draw();
  }

  draw() {
    const chart = d3.select('#chart');
    chart.select('svg').remove();
     d3.select('.yaxis-container').select('svg').remove();
    const margin = { top: 30, right: 22, bottom: 20, left: 10 };
    let width = window.innerWidth-340 - margin.left - margin.right;
    const height = 220 - margin.top - margin.bottom;

    if(this.graphData.length > 14){
      width += (this.graphData.length - 14 ) * 79;
    }
    // append the svg object to the body of the page
    const svg = chart
      .append('svg')
      .attr('width', width + margin.left + margin.right)
      .attr('height', height + margin.top + margin.bottom)
      .append('g')
      .attr('transform', `translate(${margin.left},${margin.top})`);

    const categories = [];
    const maxYValue = [];
    this.formatDateOnXAxis(this.graphData);

    this.graphData.forEach(d => {
      const lineData = d.value;
      const lineDataCategorywise = {};
      const maxY = [];
      lineData.forEach(lineDetails => {
        lineDataCategorywise[lineDetails.kpiGroup] = lineDetails;
        lineDetails['filter'] = d.filter;
        maxY.push(lineDetails['value']);
        if (!categories.includes(lineDetails['kpiGroup'])) {
          categories.push(lineDetails['kpiGroup']);
        }
      });
      d['lineDataCategorywise'] = lineDataCategorywise;
      maxYValue.push(Math.max(...maxY));
    });

    const xCoordinates = this.graphData.map(d => d.filter);

    const x = d3.scaleBand()
      .domain(xCoordinates)
      .range([0, width])
      .paddingOuter(0);

    const initialCoordinate = x(xCoordinates[1]);

    const svgX = svg.append('g')
      .attr('class', 'xAxis')
      .attr('transform', `translate(0, ${height})`)
      .call(d3.axisBottom(x));

    const y = d3.scaleLinear()
      .domain([0, Math.ceil(Math.max(...maxYValue) / 5) * 5])
      .range([height, 0]);

    const svgY = d3.select('.yaxis-container')
      .append('svg')
      .attr('width', width + 50)
      .attr('height', height + margin.top + margin.bottom)
      .append('g')
      .attr('transform', `translate(50,${margin.top})`)
      .attr('class', 'yAxis')
      .call(d3.axisLeft(y).ticks(6).tickSize(0));


    // highlight todays Date
    if (this.currentDayIndex >= 0) {
      svg
        .select('.xAxis')
        .selectAll(`.tick:nth-of-type(${this.currentDayIndex + 1}) text`)
        .style('color', '#079FFF');
    }

    //Add horizontal  gridlines for each y tick
    svgY
      .append('g')
      .selectAll('line.gridline').data(y.ticks(6)).enter()
      .append('svg:line')
      .attr('x1', 0)
      .attr('x2', width + 50)
      .attr('y1', d => y(d))
      .attr('y2', d => y(d))
      .style('stroke', '#dedede')
      .style('fill', 'none')
      .attr('class', 'gridline');

    //Add vertical gridlines for each x tick
    svg.append('g')
      .attr('transform', `translate(0, ${height})`)
      .selectAll('line.gridline').data(xCoordinates).enter()
      .append('svg:line')
      .attr('x1', d => x(d)+initialCoordinate/2 )
      .attr('x2', d => x(d)+initialCoordinate/2)
      .attr('y1', 0)
      .attr('y2', -height)
      .style('stroke', '#dedede')
      .style('stroke-dasharray', '4,4')
      .style('fill', 'none')
      .attr('class', 'gridline');

    const color = d3.scaleOrdinal()
      .domain(categories)
      .range(['#5AA5A2', '#4472C4', '#D99748', '#CDBA38', '#D8725F']);

    const tooltipContainer = d3.select('#chart').select('.tooltip-container');

    const showTooltip = (linedata) => {
      tooltipContainer
        .selectAll('div')
        .data(linedata)
        .join('div')
        .attr('class', 'tooltip')
        .style('left', d => x(d.filter) + initialCoordinate/2 + 'px')
        .style('top', d => y(d.value) + 8 + 'px')
        .text(d => d.value)
        .transition()
        .duration(500)
        .style('display', 'block')
        .style('opacity', 1);
    };
    const hideTooltip = () => {
      tooltipContainer
        .selectAll('.tooltip')
        .transition()
        .duration(500)
        .style('display', 'none')
        .style('opacity', 0);
      tooltipContainer.selectAll('.tooltip').remove();
    };

    for (const kpiGroup of categories) {
      const lineData = this.graphData.filter(d => d['lineDataCategorywise'].hasOwnProperty(kpiGroup)).map(d => d['lineDataCategorywise'][kpiGroup]);


      const line = svg
        .append('g')
        .attr('transform', `translate(0,0)`)
        .append('path')
        .datum(lineData)
        .attr('d', d3.line()
          .x((d) => x(d.filter) + initialCoordinate/2)
          .y((d) => y(d.value))
        )
        .attr('stroke', (d) => color(kpiGroup))
        .style('stroke-width', 2)
        .style('fill', 'none')
        .style('cursor', 'pointer')
        .on('mouseover', function(event, linedata) {
          d3.select(this)
            .style('stroke-width', 4);
          showTooltip(linedata);
        })
        .on('mouseout', function(event, d) {
          d3.select(this)
            .style('stroke-width', 2);
          hideTooltip();
        });

        const circlegroup = svg
        .append('g')
        .attr('class', 'circle-group')
        .attr('transform', `translate(0,0)`)
        .selectAll('circle')
        .data(lineData)
        .enter()
        .append('circle')
        .attr('cx', d => x(d.filter) + initialCoordinate/2)
        .attr('cy', d => y(d.value))
        .attr('r', 3)
        .style('stroke-width', 5)
        .attr('stroke', 'transparent')
        .attr('fill', color(kpiGroup))
        .on('mouseover', function(event) {
          d3.select(this)
            .transition()
            .duration(500)
            .style('cursor', 'pointer')
            .attr('r', 3)
            .style('stroke-width', 10);
          showTooltip(lineData);
        })
        .on('mouseout', function(event, d) {
          d3.select(this)
            .transition()
            .duration(500)
            .attr('r', 3)
            .style('stroke-width', 5);
          hideTooltip();
        });
    }
    //Add xCaption
    d3.select('#container')
      .select('.x-caption')
      .append('span')
      .text(this.xCaption);

    //Add YCaption
    d3.select('.yaxis-container')
      .append('div')
      .attr('class', 'y-caption')
      .append('span')
      .text(this.yCaption);

    const legendDiv = d3.select('#legendContainer')
      .style('margin-left', 60 + 'px');

    legendDiv.transition()
      .duration(200)
      .style('display', 'block')
      .style('opacity', 1)
      .attr('class', 'p-d-flex p-flex-wrap normal-legend');

    let htmlString = '';

    categories.forEach((d) => {
      htmlString += `<div class="legend_item"><div class="legend_color_indicator" style="background-color: ${color(d)}"></div> <span class="p-m-1">: ${d}</span></div>`;
    });

    legendDiv.html(htmlString)
      .style('bottom', 0 + 'px');

    if (this.kpiId === 'kpi125') {
      const dottedLineData = this.generateDottedLineDataForKpi();
      categories.push(this.data[0].additionalGroup[0]);
      svg
        .append('g')
        .attr('transform', `translate(0,0)`)
        .append('path')
        .datum(dottedLineData)
        .attr('d', d3.line()
          .x((d) => x(d.filter)+ initialCoordinate/2 )
          .y((d) => y(d.value))
        )
        .attr('stroke', '#D8725F')
        .style('stroke-width', 3)
        .style('stroke-dasharray', '4,4')
        .style('fill', 'none')
        .style('cursor', 'pointer')
        .on('mouseover', function (event, linedata) {
          d3.select(this)
            .style('stroke-width', 5);
          showTooltip(linedata);

        })
        .on('mouseout', function (event, d) {
          d3.select(this)
            .style('stroke-width', 3);
          hideTooltip();
        });

      htmlString += `<div class="legend_item"><div class="legend_color_indicator_dashed"></div> <span class="p-m-1">: ${this.data[0].additionalGroup[0]}</span></div>`;
      legendDiv.html(htmlString);
    }


  }



  formatDateOnXAxis(data) {

    const days = ["SUN", "MON", "TUE", "WED", "THUR", "FRI", "SAT"];
    return data.map((d, i) => {
      const date = new Date(d['filter']);
      const currentDate = new Date();

      if (date.toDateString() === currentDate.toDateString()) {
        this.currentDayIndex = i;
      }
      d['filter'] = `${days[date.getDay()]} ${(date.getDate() < 10) ? ('0' + date.getDate()) : date.getDate()}/${(date.getMonth() + 1) < 10 ? '0' + (date.getMonth() + 1) : date.getMonth() + 1}`;
      return d;
    });
  }

  generateDottedLineDataForKpi() {
    const dottedLineData = [];
    if (this.data[0].additionalGroup) {
      const dottedLineDataIndex = this.graphData.findIndex(d => d['lineDataCategorywise'].hasOwnProperty('Predicted Completion'));
      if (dottedLineDataIndex !== -1) {
        const startlineDataPoint = {
          ...this.graphData[dottedLineDataIndex]['lineDataCategorywise']['Predicted Completion'],
          kpiGroup: this.data[0].additionalGroup[0]
        };
        let endlineDataPoint;
        dottedLineData.push(startlineDataPoint);
        if (this.graphData[dottedLineDataIndex].lineDataCategorywise.hasOwnProperty('Completion Till Date')) {
          endlineDataPoint = {
            ...this.graphData[dottedLineDataIndex]['lineDataCategorywise']['Completion Till Date'],
            kpiGroup: this.data[0].additionalGroup[0]
          };

        } else {
          endlineDataPoint = {
            ...this.graphData[dottedLineDataIndex - 1]['lineDataCategorywise']['Completion Till Date'],
            kpiGroup: this.data[0].additionalGroup[0]
          };
        }
        dottedLineData.push(endlineDataPoint);
      }
    }
    return dottedLineData;
  }

}
