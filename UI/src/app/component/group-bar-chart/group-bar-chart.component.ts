/* eslint-disable @typescript-eslint/quotes */
/* eslint-disable prefer-arrow/prefer-arrow-functions */
import { ThisReceiver } from '@angular/compiler';
import { Component, Input, ViewContainerRef, OnChanges, SimpleChanges, OnInit } from '@angular/core';
import * as d3 from 'd3';
import { SharedService } from 'src/app/services/shared.service';

@Component({
  selector: 'app-group-bar-chart',
  templateUrl: './group-bar-chart.component.html',
  styleUrls: ['./group-bar-chart.component.css']
})
export class GroupBarChartComponent implements OnChanges {

  @Input() data: any;
  @Input() width: any;
  @Input() yCaption: string; // label at y axis
  @Input() thresholdValue: any;
  @Input() xCaption: string;
  @Input() unit?: string;
  @Input() color?: string;
  @Input() kpiId?: string;
  @Input() maxValue?: any;
  @Input() selectedtype: string;
  @Input() legendType: string;

  elem;
  maxYValue = 0;
  dataPoints = 2;
  dataLength = 0;
  currentDayIndex;
  subGroups = [];
  lineGroups = [];

  constructor(private viewContainerRef: ViewContainerRef, private service: SharedService) { }

  ngOnChanges(changes: SimpleChanges) {
    if (this.selectedtype?.toLowerCase() === 'kanban') {
      this.xCaption = this.service.getSelectedDateFilter();
    }else{
      const duration = this.data[0]?.dataGroup[0]?.duration;
      this.xCaption = duration.charAt(0).toUpperCase() + duration.slice(1).toLowerCase();
    }
    // only run when property "data" changed
    if (changes['data']) {
      this.dataPoints = this.data.length;
      this.dataLength = this.data.length;
      this.elem = this.viewContainerRef.element.nativeElement;
      this.draw();
    }
  }


  draw() {
    const elem = this.elem;
    const self = this;
    this.maxYValue = 0;
    d3.select(elem).select('svg').remove();
    d3.select(elem).select('.tooltip').remove();
    d3.select(elem).select('.legend').remove();
    d3.select(elem).select('.d3-legend').remove();
    d3.select(elem).select('.normal-legend').remove();

    d3.select(elem).select('#verticalSVG').select('svg').remove();
    d3.select(elem).select('#horizontalSVG').select('svg').remove();
    d3.select(elem).select('#svgLegend').select('svg').remove();
    d3.select(elem).select('#legendIndicator').select('svg').remove();
    d3.select(elem).select('#xCaptionContainer').select('text').remove();
    d3.select(elem).select('#horizontalSVG').select('.current-week-tooltip').selectAll('.tooltip').remove();
    let data = this.data[0]?.dataGroup;
    data = this.formatData(data);

    const subgroups = this.subGroups;
    const groups = d3.map(data, (d) => d.group);

    const currentDayIndex = this.currentDayIndex;
    const barWidth = 18;
  
    const spacingVariable = 50;
    const height = 195;
    const margin = 50;
    const marginLeft = 40;
    const marginTop = 35;
    const xTick = barWidth;
    let width = window.innerWidth- 320 - marginLeft;
    if(data.length > 5){
      width += data.length * barWidth * (subgroups.length + 3);
    }

    const svgX = d3.select(elem).select('#horizontalSVG').append('svg')
      .attr('width', width)
      .attr('height', (height + 35) + 'px')
      .style('text-align', 'center')
      .append('g')
      .attr('transform', `translate(${0}, ${marginTop})`);

    const svgY = d3.select(this.elem).select('#verticalSVG').append('svg')
      .attr('width', '50px')
      .attr('height', (height + 35) + 'px')
      .style('text-align', 'center')
      .append('g')
      .attr('transform', `translate(${marginLeft}, ${marginTop})`);

    this.findMaxVal(data);
    if (!(this.maxYValue >= 5)) {
      this.maxYValue = 5;
    } else {
      this.maxYValue = Math.ceil(this.maxYValue / 5) * 5;
    }

    const x = d3.scaleBand()
      .domain(groups)
      .range([0, width - margin])
      .paddingInner(2)
      .paddingOuter(0.5);

    const y = d3.scaleLinear()
      .range([height - margin, 0])
      .domain([0, this.maxYValue]);

    const xSubgroup = d3.scaleBand()
      .domain(subgroups)
      .range([0, subgroups.length * barWidth])
      .paddingInner(0);

    const colorList = ['#049fff', '#f4aa46', '#f8404d', '#00e1ba'];
    const color = d3.scaleOrdinal()
      .domain(subgroups)
      .range(colorList);

    svgX.append('g')
      .attr('class', 'xAxis')
      .attr('transform', `translate(0, ${y(0)})`)
      .call(d3.axisBottom(x));

    d3.select(this.elem).select('#xCaptionContainer').append('text')
      .attr('x', ((document.getElementById('groupstackchart').offsetWidth - 70) / 2) - 24)
      .attr('y', 44)
      .attr('transform', 'rotate(0)')
      .text(this.xCaption);

    svgX
      .select('.xAxis')
      .selectAll('.tick text')
      .attr('x', xTick)
      .attr('y', 15);

   
    if (currentDayIndex >= 0) {
      svgX
        .select('.xAxis')
        .selectAll(`.tick:nth-of-type(${currentDayIndex + 1}) text`)
        .style('color', '#079FFF').
        style("font-weight", "bolder")
    }

     /** Showing  data point for current plot */
    const currentWeekTooltipContainer = d3.select('#horizontalSVG').select('.current-week-tooltip');
    let top = (height / 2) - 30;
    for (const kpiGroup of this.lineGroups) {
      const lineData = data[data.length - 1];
      currentWeekTooltipContainer.append('div')
        .attr('class', 'tooltip')
        .style('left', `${x(lineData.group)}px`)
        .style('top', top + 'px')
        .text(`${kpiGroup} - ${lineData[kpiGroup]}`)
        .style('opacity', 1);
      top = top + 30;
    }

    svgX
      .select('.xAxis')
      .selectAll('line')
      .attr('x1', xTick)
      .attr('x2', xTick)
      .attr('y1', 0)
      .attr('y2', 10)
      .style('stroke', '#333333');

    svgY.append('g')
      .attr('class', 'yAxis')
      .call(d3.axisLeft(y).ticks(5).tickSize(-width + margin))
      .append('text')
      .attr('x', -80)
      .attr('y', -30)
      .attr('transform', 'rotate(-90)')
      .attr('fill', '#437495')
      .attr('font-size', '12px')
      .text(this.yCaption);

    // gridlines
    svgX.selectAll('line.gridline').data(y.ticks(5)).enter()
      .append('svg:line')
      .attr('x1', 0)
      .attr('x2', width)
      .attr('y1', d => y(d))
      .attr('y2', d => y(d))
      .style('stroke', '#dedede')
      .style('fill', 'none')
      .attr('class', 'gridline');

    // Define the div for the tooltip
    const div = d3.select(this.elem).select('#groupstackchart').append('div')
      .attr('class', 'tooltip')
      .style('display', 'none')
      .style('opacity', 0);

    svgX.append("g")
      .selectAll("g")
      .data(data)
      .enter()
      .append("g")
      .attr("transform", d => "translate(" + x(d.group) + ",0)")
      .selectAll("rect")
      .data(d => subgroups.map(key => ({ key, value: d[key], group: d['date'] , hoverValue:d[key+'HoverValue']})))
      .enter().append("rect")
      .attr("x", d => xSubgroup(d.key))
      .attr("y", d=> y(0))
      .attr("width", barWidth)
      .attr("height", d=> height - y(0) - spacingVariable)
      .attr("fill", d=> color(d.key))
      .style('cursor', 'pointer')
      .on('mouseover', (event, d)=> {
        if (d.hoverValue) {
          const circle = event.target;
          const {
            top: yPosition,
            left: xPosition
          } = circle.getBoundingClientRect();

          div.transition()
            .duration(200)
            .style('display', 'block')
            .style('opacity', .9);


          div.html(`${d?.group} : ${Object.values(d?.hoverValue).reduce((a:number, b:number) => a + b, 0)} `+ '<span class=\'toolTipValue\'> </span>')
            .style('left', xPosition + 20 + 'px')
            .style('top', yPosition + 20 + 'px')
            .style('position', 'fixed');

          for (const hoverData in d.hoverValue) {
            div.append('p').html(`${hoverData}` + ' : ' + '<span class=\'toolTipValue\'> ' + `${d.hoverValue[hoverData]}` + ' </span>');
          }
        }
      })
      .on('mouseout', function(d) {
        div.transition()
          .duration(500)
          .style('display', 'none')
          .style('opacity', 0);
      })
      .transition()
      .delay((d) => 200)
      .duration(800)
      .attr("y", d=> y(d.value))
      .attr("height", d=> height - y(d.value) - spacingVariable);

      const tooltipContainer = d3.select('#horizontalSVG').select('.tooltip-container');
      const showTooltip = (linedata) => {
        currentWeekTooltipContainer.style('display','none');
        tooltipContainer
          .selectAll('div')
          .data(linedata)
          .join('div')
          .attr('class', 'tooltip')
          .style('left', d => x(d.filter)  + 0 + 'px')
          .style('top', d => y(d.value) + 6 + 'px')
          .text(d => d.value)
          .transition()
          .duration(500)
          .style('display', 'block')
          .style('opacity', 1);
      };
      const hideTooltip = () => {
        currentWeekTooltipContainer.style('display','block');
        tooltipContainer
          .selectAll('.tooltip')
          .transition()
          .duration(500)
          .style('display', 'none')
          .style('opacity', 0);
        tooltipContainer.selectAll('.tooltip').remove();
      };

      for (const kpiGroup of this.lineGroups) {
        const lineData = data.filter(d => d.hasOwnProperty(kpiGroup)).map(d=>{ return { "filter" : d['group'],"value" : d[kpiGroup]}})
  
        const line = svgX
          .append('g')
          .attr('transform', `translate(17,0)`)
          .append('path')
          .datum(lineData)
          .attr('d', d3.line()
            .x((d) => x(d.filter))
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
  
        const circlegroup = svgX
          .append('g')
          .attr('class', 'circle-group')
          .attr('transform', `translate(17,0)`)
          .selectAll('circle')
          .data(lineData)
          .enter()
          .append('circle')
          .attr('cx', d => x(d.filter))
          .attr('cy', d => y(d.value))
          .attr('r', 3)
          .style('stroke-width', 1)
          .attr('stroke', 'none')
          .attr('fill', color(kpiGroup))
          .on('mouseover', function(event) {
            d3.select(this)
              .transition()
              .duration(500)
              .style('cursor', 'pointer')
              .attr('r', 3);
            showTooltip(lineData);
          })
          .on('mouseout', function(event, d) {
            d3.select(this)
              .transition()
              .duration(500)
              .attr('r', 3);
            hideTooltip();
          });
      }

    const legendDiv = d3.select(this.elem).select('#groupstackchart').append('div');
    legendDiv.style('margin-top', '20px');

    legendDiv.transition()
      .duration(200)
      .style('display', 'block')
      .style('opacity', 1)
      .attr('width', width)
      .attr('class', 'p-d-flex p-flex-wrap normal-legend');

    let htmlString = '';
    var counter = 0;
    subgroups.forEach((d, i) => {
      counter = i;
      htmlString += `<div class="legend_item p-d-flex p-align-center"><div class="legend_color_indicator" style="background-color: ${colorList[i]}"></div> : ${d}</div>`;
    });
    counter ++
    this.lineGroups.forEach((d, i) => {
      htmlString += `<div class="legend_item p-d-flex p-align-center"><div class="legend_color_indicator line-indicator" style="background-color: ${colorList[counter]}"></div> : ${d}</div>`;
      counter ++;
    })

    legendDiv.html(htmlString)
      .style('left', 25 + 'px')
      .style('bottom', 20 + 'px')
      .style('top', y[0] + 30 + 'px');
  }



  findMaxVal(data) {
    data.forEach(item => {
      Object.values(item).forEach(val => {
        if (typeof val === 'number') {
          this.maxYValue = Math.max(this.maxYValue, val);
        }
      });
    });

  }

  formatData(data) {
    const resultData = {};
    data.forEach((d) => {
      const date = d.filter;
      let graphData = {};
      d.value.forEach(groupD=>{
         if (!this.subGroups.includes(groupD.kpiGroup) && groupD.graphType === 'bar' ) {
           this.subGroups.push(groupD.kpiGroup);
         }
         if (!this.lineGroups.includes(groupD.kpiGroup) && groupD.graphType === 'line' ) {
          this.lineGroups.push(groupD.kpiGroup);
        }
         graphData = { ...graphData,
          [groupD.kpiGroup]: groupD.value,
          group: date,
          date : date,
          [groupD.kpiGroup+'HoverValue']:groupD?.hoverValue,
          sprojectName : groupD.sprojectName
         }

      })
      resultData[date] = {...graphData};

    });
    const resultDataList = Object.values(resultData);
    if(this.xCaption.toLowerCase() === 'weeks' || this.xCaption.toLowerCase() === 'days'){
      return this.formatDateOnXAxis(resultDataList);
    }else{
      return resultDataList;
    }
  }

  formatDateOnXAxis(data){
    const days = ["SUN", "MON", "TUE", "WED", "THUR", "FRI", "SAT"];
    return data.map((d, i) => {
      if(d['group'].includes('to')){
        d['group'] = d['group'].replace(" ",'');
        const dateArray = d['group'].split('to');
        const date1 = new Date(dateArray[0]);
        const date2 = new Date(dateArray[1]);
        const today = new Date();
        const startOfCurrentWeek = new Date(today.getFullYear(), today.getMonth(), today.getDate() - today.getDay());
        const endOfCurrentWeek = new Date(today.getFullYear(), today.getMonth(), today.getDate() + (6 - today.getDay()));
        if (date1 <= endOfCurrentWeek && date2 >= startOfCurrentWeek) {
          this.currentDayIndex = i;
        }
        d['group'] = `${(date1.getDate() < 10) ? ('0' + date1.getDate()) : date1.getDate()}/${(date1.getMonth() + 1) < 10 ? '0' + (date1.getMonth() + 1) : date1.getMonth() + 1} - `+
        `${(date2.getDate() < 10) ? ('0' + date2.getDate()) : date2.getDate()}/${(date2.getMonth() + 1) < 10 ? '0' + (date2.getMonth() + 1) : date2.getMonth() + 1}`;
      return d;
      }else{
        const date = new Date(d['group']);
        const currentDate = new Date();
        if (date.toDateString() === currentDate.toDateString()) {
          this.currentDayIndex = i;
        }
        d['group'] = `${days[date.getDay()]} ${(date.getDate() < 10) ? ('0' + date.getDate()) : date.getDate()}/${(date.getMonth() + 1) < 10 ? '0' + (date.getMonth() + 1) : date.getMonth() + 1}`;
      return d;
      }
    });
  }

}
