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
      xAxisCoordinates.push(this.formatDate(date));
    }
    return xAxisCoordinates;
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
      .domain([0,this.issusDataList.length +1])
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

    const showMarkers =(issue,index)=>{
      const marker = svg
      .append('g')
      .attr('class', 'circle-group')
      .attr('transform', `translate(0,0)`);

      //show 'Due Date Exceeded' if status not closed after dueDate
        if(this.compareDates(new Date(),issue['Due Date'])){
          const xValue= x(this.formatDate(new Date())) +initialCoordinate/2;
          const yValue =y(index);
          marker.append('image')
          .attr('xlink:href','../../../assets/img/due-date-exceeded.svg')
          .attr('width', '50px') . attr('height', '50px')
          .attr('x',xValue-25)
          .attr('y',yValue-25);
        }

      //Show 'Dev Due Date' if it exist
      if(issue['Dev Due Date']!== '-' &&  this.compareDates(issue['Dev Due Date'], this.selectedSprintInfo.sprintStartDate) && this.compareDates(this.selectedSprintInfo.sprintEndDate,issue['Dev Due Date'])){
        const xValue= x(this.formatDate(new Date(issue['Dev Due Date']))) +initialCoordinate/2;
        const yValue =y(index);
        marker.append('image')
        .attr('xlink:href','../../../assets/img/dev-completed.svg')
        .attr('width', '50px') . attr('height', '50px')
        .attr('x',xValue-25)
        .attr('y',yValue-25);
      }
 
      // show QA completed ifit exist
      if(issue['Test-Completed']!== '-' &&  this.compareDates(issue['Test-Completed'], this.selectedSprintInfo.sprintStartDate) && this.compareDates(this.selectedSprintInfo.sprintEndDate,issue['Test-Completed'])){
        const xValue= x(this.formatDate(new Date(issue['Test-Completed']))) +initialCoordinate/2;
        const yValue =y(index);
        marker.append('image')
        .attr('xlink:href','../../../assets/img/qa-completion.svg')
        .attr('width', '50px') . attr('height', '50px')
        .attr('x',xValue-25)
        .attr('y',yValue-25);
      }

      //show status transition within sprint dates 'statusLogGroup'
    };

    const drawLineForIssue =(issue,i,isSubTask)=>{
      const {startPoint, endPoint} = this.getStartAndEndLinePoints(issue);
          if(startPoint && endPoint){
            const line = svg
            .append('g')
            .attr('transform', `translate(0,0)`)
            .append('svg:line')
            .attr('x1', x(startPoint) + initialCoordinate / 2)
            .attr('x2', x(endPoint) + initialCoordinate / 2)
            .attr('y1', y(i+1))
            .attr('y2',  y(i+1))
            .style('stroke', isSubTask ? '#000000' : '#437495')
            .style('stroke-width', isSubTask ? 1 : 4)
            .style('fill', 'none')
            .attr('class', 'gridline');

            showMarkers(issue,i+1);
          }
    };



    // draw lines for each issues and its subtask
    for(let i = 0;i<this.issusDataList.length;i++){
      drawLineForIssue(this.issusDataList[i],i,false);

      //draw lines for subtask
      if(this.issusDataList[i]['subTask']){
        for(let j=0;j<this.issusDataList[i]['subTask'].length ; j++){
          drawLineForIssue(this.issusDataList[i]['subTask'][j],(i +j+1*0.5),true);
        }
      }
  }


}

// get line start and end coordinates
  getStartAndEndLinePoints(issue) {
    //calculate startDate
    let startPoint;
    if (issue?.['previousSprintName']) {
      const date = new Date(this.selectedSprintInfo.sprintStartDate);
      startPoint = this.formatDate(new Date(date.getFullYear(), date.getMonth(), date.getDate() - 1));
    } else {
      startPoint = this.compareDates(issue['Created Date'],this.selectedSprintInfo.sprintStartDate,) ? this.formatDate(new Date(issue['Created Date'])) : this.formatDate(new Date(this.selectedSprintInfo.sprintStartDate));
    }

    //calculate endDate
    let endPoint;
    if(issue['Issue Status'] === 'Closed'){
      if(this.compareDates(this.selectedSprintInfo.sprintStartDate,issue['Updated Date'])){
        endPoint = null;
      }else{
        endPoint =  this.formatDate(new Date(issue['Updated Date']));
      }
    }else{
      endPoint = this.formatDate(new Date(this.selectedSprintInfo.sprintEndDate));
    }

    return {startPoint,endPoint};
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
