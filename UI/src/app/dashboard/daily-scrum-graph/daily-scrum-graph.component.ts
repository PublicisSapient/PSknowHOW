import { Component, Input, OnChanges, OnDestroy, OnInit, SimpleChanges, ViewContainerRef } from '@angular/core';
import { arrow } from '@popperjs/core';
import * as d3 from 'd3';

@Component({
  selector: 'app-daily-scrum-graph',
  templateUrl: './daily-scrum-graph.component.html',
  styleUrls: ['./daily-scrum-graph.component.css']
})
export class DailyScrumGraphComponent implements OnChanges,OnDestroy {


  @Input() issusDataList;
  @Input() selectedSprintInfo;
  elem;

  currentDayIndex;
  constructor(private viewContainerRef: ViewContainerRef) { }

  // ngOnInit(): void {
  //   console.log(this.issusDataList,this.selectedSprintInfo);
  //   this.draw();
  // }

  ngOnChanges(changes: SimpleChanges){
    this.elem = this.viewContainerRef.element.nativeElement;
    // console.log(this.issusDataList,this.selectedSprintInfo);
    this.draw();
  }

  generateDates() {
    const currentDate = new Date();
    const _MS_PER_DAY = 1000 * 60 * 60 * 24;
    const startDate = new Date(this.selectedSprintInfo.sprintStartDate);
    const endDate = new Date(this.selectedSprintInfo.sprintEndDate);
    const xAxisCoordinates = [];
    const noOfDaysInCurrentSprint = (Date.UTC(endDate.getFullYear(), endDate.getMonth(), endDate.getDate()) - Date.UTC(startDate.getFullYear(), startDate.getMonth(), startDate.getDate())) / _MS_PER_DAY + 1;
    for (let i = 0; i < noOfDaysInCurrentSprint; i++) {
      const date = new Date(startDate.getFullYear(), startDate.getMonth(), startDate.getDate() + i);
      if (date.toDateString() === currentDate.toDateString()) {
        this.currentDayIndex = i;
      }
      xAxisCoordinates.push(this.pad(date.getMonth() + 1) + '/' + this.pad(date.getDate()));
    }
    return xAxisCoordinates;
  }

  pad(s) {
    return s < 10 ? '0' + s : s;
  }

  draw(){
    const chart = d3.select(this.elem).select('#chart');
    chart.select('svg').remove();

    const xCoordinates = this.generateDates();
    const margin = { top: 30, right: 10, bottom: 20, left: 10 };
    let width = (chart.node().getBoundingClientRect().width < 1200 ? 1200 : chart.node().getBoundingClientRect().width) - margin.left - margin.right;
    const height = chart.node().getBoundingClientRect().height - margin.top - margin.bottom;
    console.log(width,height);

    if(this.issusDataList.length > 14){
      width= width +(( this.issusDataList.length -14) * 200);
    }

    console.log(width);
    
    const svg = chart
      .append('svg')
      .attr('width', width + margin.left + margin.right)
      .attr('height', height + margin.top + margin.bottom)
      .append('g')
      .attr('transform', `translate(${margin.left},0)`);

      const x = d3.scaleBand()
      .domain(xCoordinates)
      .range([0, width])
      .paddingOuter(0);

      const initialCoordinate = x(xCoordinates[1]);

      const svgX = svg.append('g')
      .attr('class', 'xAxis')
      .attr('transform', `translate(0, ${height})`)
      .call(d3.axisBottom(x));

          // highlight todays Date
    if (this.currentDayIndex >= 0) {
      svg
        .select('.xAxis')
        .selectAll(`.tick:nth-of-type(${this.currentDayIndex + 1}) text`)
        .style('color', '#2741D3')
        .style('font-weight','bold');
    }

    //draw line for current day
    const line = svg
    .append('g')
    .attr('transform', `translate(0,0)`)
    .append('svg:line')
    .attr('x1', x(xCoordinates[this.currentDayIndex]) +initialCoordinate/2)
    .attr('x2', x(xCoordinates[this.currentDayIndex]) +initialCoordinate/2)
    .attr('y1', height)
    .attr('y2',0)
    .style('stroke', '#dedede')
    .style('fill', 'none')
    .attr('class', 'gridline');


    // svgX
    // .selectAll('line.gridline')
    // .data(yScale.ticks(5))
    // .enter()
    // .append('svg:line')
    // .attr('x1', 0)
    // .attr('x2', width)
    // .attr('y1', function (d) {
    //   return yScale(d);
    // })
    // .attr('y2', function (d) {
    //   return yScale(d);
    // })
    // .style('stroke', '#dedede')
    // .style('fill', 'none')
    // .attr('class', 'gridline');

  }

  ngOnDestroy(): void {
    const chart = d3.select( this.elem).select('#chart');
    chart.select('svg').remove();
  }
}
