import { Component, Input, OnChanges, OnDestroy, OnInit, SimpleChanges, ViewContainerRef } from '@angular/core';
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

  ngOnChanges(changes: SimpleChanges){
    this.elem = this.viewContainerRef.element.nativeElement;
    this.draw();
  }

  //generated dates on MM/DD format 
  generateDates() {
    const currentDate = new Date();

    const startDate = new Date(this.selectedSprintInfo.sprintStartDate);
    const endDate = new Date(this.selectedSprintInfo.sprintEndDate);
    const xAxisCoordinates = [];
    const noOfDaysInCurrentSprint = this.getNoOFDayBetweenStartAndEndDate(startDate,endDate);
    for (let i = 0; i < noOfDaysInCurrentSprint; i++) {
      const date = new Date(startDate.getFullYear(), startDate.getMonth(), startDate.getDate() + i);
      if (date.toDateString() === currentDate.toDateString()) {
        this.currentDayIndex = i;
      }
      xAxisCoordinates.push(this.formatDate(date));
    }
    return xAxisCoordinates;
  }

  getNoOFDayBetweenStartAndEndDate(startDate,endDate){
    const _MS_PER_DAY = 1000 * 60 * 60 * 24;
    return (Date.UTC(endDate.getFullYear(), endDate.getMonth(), endDate.getDate()) - Date.UTC(startDate.getFullYear(), startDate.getMonth(), startDate.getDate())) / _MS_PER_DAY + 1;
  }

  formatDate(date){
    return this.pad(date.getMonth() + 1) + '/' + this.pad(date.getDate());
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
    const height = d3.select(this.elem).node().offsetHeight;

    if(this.issusDataList.length > 15){
      width= width +(( this.issusDataList.length -14) * 200);
    }

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

      const y = d3.scaleLinear()
      .domain([0,this.issusDataList.length +2])
      .range([height,0]);

      const initialCoordinate = x(xCoordinates[1]);

      //add X-Axis
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

      //draw line for todays date if it exist
    if (typeof this.currentDayIndex === 'number') {
      const line = svg
        .append('g')
        .attr('transform', `translate(0,0)`)
        .append('svg:line')
        .attr('x1', x(xCoordinates[this.currentDayIndex]) + initialCoordinate / 2)
        .attr('x2', x(xCoordinates[this.currentDayIndex]) + initialCoordinate / 2)
        .attr('y1', height)
        .attr('y2', 0)
        .style('stroke', '#dedede')
        .style('fill', 'none')
        .attr('class', 'gridline');
    }

    const showMarkers = (issue, index) => {
      const marker = svg
        .append('g')
        .attr('class', 'circle-group')
        .attr('transform', `translate(0,0)`);

      //show status transition within sprint dates 'statusLogGroup'
      Object.keys(issue['statusLogGroup']).forEach(status => {
        const xValue = x(this.formatDate(new Date(status))) + initialCoordinate / 2;
        const yValue = y(index);
        marker.append('circle')
          .attr('cx', d => xValue)
          .attr('cy', yValue)
          .attr('r', 5)
          .style('stroke-width', 1)
          .attr('stroke', '#437495')
          .attr('fill', 'white');

      });

      //show 'Due Date Exceeded' if status not closed after dueDate
      if (this.compareDates(new Date(), issue['Due Date']) && !issue['Actual-Completion-Date']) {
        const xValue = x(this.formatDate(new Date())) + initialCoordinate / 2;
        const yValue = y(index);
        marker.append('image')
          .attr('xlink:href', '../../../assets/img/due-date-exceeded.svg')
          .attr('width', '50px').attr('height', '50px')
          .attr('x', xValue - 25)
          .attr('y', yValue - 25)
          .style('cursor','pointer')
          .on('mouseover',()=>{
           const  data = `<p>Due date exceeded</p><br><p>${issue['Due Date']}</>`;
           showTooltip(data,xValue,yValue);
          })
          .on('mouseout',()=>{
            // hideTooltip();
          });
      }

      //Show 'Dev Due Date completion' if it exist
      if (issue['Dev-Completion-Date'] !== '-' && this.compareDates(issue['Dev-Completion-Date'], this.selectedSprintInfo.sprintStartDate) && this.compareDates(this.selectedSprintInfo.sprintEndDate, issue['Dev-Completion-Date'])) {
        const xValue = x(this.formatDate(new Date(issue['Dev-Completion-Date']))) + initialCoordinate / 2;
        const yValue = y(index);
        marker.append('image')
          .attr('xlink:href', '../../../assets/img/dev-completed.svg')
          .attr('width', '50px').attr('height', '50px')
          .attr('x', xValue - 25)
          .attr('y', yValue - 25);
      }

      // show QA completed ifit exist
      if (issue['Test-Completed'] !== '-' && this.compareDates(issue['Test-Completed'], this.selectedSprintInfo.sprintStartDate) && this.compareDates(this.selectedSprintInfo.sprintEndDate, issue['Test-Completed'])) {
        const xValue = x(this.formatDate(new Date(issue['Test-Completed']))) + initialCoordinate / 2;
        const yValue = y(index);
        marker.append('image')
          .attr('xlink:href', '../../../assets/img/qa-completed.svg')
          .attr('width', '50px').attr('height', '50px')
          .attr('x', xValue - 25)
          .attr('y', yValue - 25);
      }
    };

    const showIssueIdandStatus = (centerDate, issue, i) => {
      const issueId = svg
        .append('g')
        .attr('transform', `translate(0,0)`)
        .append('text')
        .attr('height', 10)
        .attr('width', 100)
        // .attr('x', issue['Actual-Start-Date'] ? x(centerDate) +initialCoordinate/2 : x(centerDate) +initialCoordinate/2 + 30)
        .attr('x', x(centerDate) + initialCoordinate / 2)
        .attr('y', y(i) - 15)
        .html(`${issue['Issue Id']}`)
        .style('font-weight', 'bold');

      const issUeStatus = svg
        .append('g')
        .attr('transform', `translate(0,0)`)
        .append('text')
        .attr('height', 10)
        .attr('width', 100)
        // .attr('x', issue['Actual-Start-Date'] ? x(centerDate) +initialCoordinate/2 : x(centerDate) +initialCoordinate/2 + 30)
        .attr('x', x(centerDate) + initialCoordinate / 2)
        .attr('y', y(i) + 18)
        .html(`${issue['Issue Status']}`);



    };

    //show tooltip
    const tooltipContainer = d3.select('#chart').select('.tooltip-container');
    const showTooltip = (data,xVal,yVal) => {
      svg
      .append('g')
      // .attr('class', 'tooltip')
      .attr('transform', `translate(0,0)`)
      .append('div')
        .attr('class', 'tooltip')
        .style('left',  x(xVal) + initialCoordinate/2 )
        .style('top',  y(yVal))
        .html(data)
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

    const drawLineForIssue =(issue,i,isSubTask)=>{
      const {startPoint, endPoint,centerDate} = this.getStartAndEndLinePoints(issue);
      console.log(startPoint,endPoint,issue['Issue Id']);
          if(startPoint && endPoint){
            const line = svg
            .append('g')
            .attr('transform', `translate(0,0)`)
            .append('svg:line')
            .attr('x1', issue['spill'] ? x(startPoint) -initialCoordinate : x(startPoint) + initialCoordinate / 2)
            .attr('x2', x(endPoint) + initialCoordinate / 2)
            .attr('y1', y(i+1))
            .attr('y2',  y(i+1))
            .style('stroke',issue['spill'] ? '#D8D8D8' : '#437495')
            .style('stroke-width', isSubTask ? 1 : 4)
            .style('stroke-dasharray', issue['spill'] ?  '4,4' : '0,0')
            .style('fill', 'none')
            .attr('class', 'gridline');

            showMarkers(issue,i+1);

          } else if (!issue['Actual-Start-Date']) {
            //draw circle to represent issue 
            const xValue = x(this.formatDate(new Date())) + initialCoordinate / 2;
            const yValue = y(i);

            svg.append('g')
              .attr('transform', `translate(0,0)`)
              .append('circle')
              .attr('cx', d => xValue)
              .attr('cy', yValue)
              .attr('r', 5)
              .style('stroke-width', 1)
              .attr('stroke', '#707070')
              .attr('fill', '#707070');

          }

          showIssueIdandStatus(centerDate,issue,i+1);
    };



    // draw lines for each issues and its subtask
    for(let i = 0;i<this.issusDataList.length;i++){
      drawLineForIssue(this.issusDataList[i],i,false);

      //draw lines for subtask
      // if(this.issusDataList[i]['subTask']){
      //   for(let j=0;j<this.issusDataList[i]['subTask'].length ; j++){
      //     drawLineForIssue(this.issusDataList[i]['subTask'][j],(i +j+1*0.5),true);
      //   }
      // }
  }


}

// get line start and end coordinates
  getStartAndEndLinePoints(issue) {
    //calculate startDate
    let startPoint;
    if (issue['spill']) {
      startPoint = new Date(this.selectedSprintInfo.sprintStartDate);
    } else {
      startPoint = issue['Actual-Start-Date'] &&  issue['Actual-Start-Date'] ? new Date(issue['Actual-Start-Date']) : new Date();
    }
    //calculate endDate
    const endPoint = issue['Actual-Completion-Date'] ? new Date(issue['Actual-Completion-Date']) : new Date(this.selectedSprintInfo.sprintEndDate);

    const noOfDaysBetweenStartandEnd = this.getNoOFDayBetweenStartAndEndDate(startPoint,endPoint);
   
    const centerDate = new Date(startPoint.getFullYear(),startPoint.getMonth(),startPoint.getDate() + noOfDaysBetweenStartandEnd/2);
 
    return {startPoint: this.formatDate(startPoint),endPoint: this.formatDate(endPoint),centerDate:this.formatDate(centerDate)};
  }

  //check if date 1 is greater than date2
  compareDates(date1,date2){
    const d1 = new Date(date1);
    const d2 = new Date(date2);
    return d1 >= d2;
  }


  ngOnDestroy(): void {
    const chart = d3.select( this.elem).select('#chart');
    chart.select('svg').remove();
  }
}
