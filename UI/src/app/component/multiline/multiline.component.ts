/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

import {
  Component,
  Input,
  ViewContainerRef,
  OnChanges,
  SimpleChanges,
  OnInit,
} from '@angular/core';

import * as d3 from 'd3';
import { SharedService } from 'src/app/services/shared.service';

/*************
This file is used to create multiseries line chart
using d3.js in v4.
@author anuj
**************/

@Component({
  selector: 'app-multiline',
  templateUrl: './multiline.component.html',
  styleUrls: ['./multiline.component.css'],
})
export class MultilineComponent implements OnChanges {
  @Input() data: any; // json data
  @Input() thresholdValue: any;
  @Input() name: string; // name of kpi
  @Input() kpiId: string; // id of the kpi
  @Input() yCaption: string; // label at y axis
  @Input() isChildComponent: boolean;
  @Input() xCaption: string;
  @Input() unit?: string;
  @Input() color?: Array<string>;
  @Input() selectedtype: string;
  @Input() board:string = '';
  elem;
  sliderLimit = <any>'750';
  sprintList : Array<any> = [];
  @Input() viewType :string = 'chart'
  @Input() lowerThresholdBG : string;
  @Input() upperThresholdBG : string;
  @Input() activeTab?: number = 0;
  elemObserver = new ResizeObserver(() => {this.draw()});

  constructor(
    private viewContainerRef: ViewContainerRef,
    private service: SharedService,
  ) {
    // used to make chart independent from previous made chart
    this.elem = this.viewContainerRef.element.nativeElement;
  }

  ngOnInit(): void {
    this.service.showTableViewObs.subscribe(view => {
      this.viewType = view;
     });
  }

  ngAfterViewInit(): void {
    this.elemObserver.observe(d3.select(this.elem).select('#graphContainer').node());
  }

  // Runs when property "data" changed
  ngOnChanges(changes: SimpleChanges) {
    if (this.selectedtype?.toLowerCase() === 'kanban' || this.service.getSelectedTab().toLowerCase() === 'developer')
    {
      this.xCaption = this.service.getSelectedDateFilter();
    }
    if (Object.keys(changes)?.length > 0) {
        d3.select(this.elem).select('svg').remove();
        d3.select(this.elem).select('.bstimeslider').remove();
        this.draw();
    } else {
      d3.select(this.elem).select('svg').remove();
      d3.select(this.elem).select('.bstimeslider').remove();
      this.draw();
    }
    if(changes['activeTab']){
      /** settimeout applied because dom is loading late */
      setTimeout(() => {
        this.draw();
      }, 0);
    }
  }

  draw() {
    if(this.data?.length> 0){

      // this is used for removing svg already made when value is updated
      d3.select(this.elem).select('#verticalSVG').select('svg').remove();
      d3.select(this.elem).select('#horizontalSVG').select('svg').remove();
      d3.select(this.elem).select('#xCaptionContainer').select('text').remove();
      d3.select(this.elem).select('#horizontalSVG').select('tooltip-container').remove();
      const formatedData = this?.data[0]?.value.map(details=>{
        const XValue = details.date || details.sSprintName;
        const projectName = '_'+this.service.getSelectedTrends()[0]?.nodeName;
        const removeProject = XValue?.includes(projectName) ? XValue?.replace(projectName,'') : XValue;
         return {...details,sortSprint:removeProject};
      })
      const isAllBelowFromThreshold = this.data[0]?.value.every(details => ((Math.round(details.value * 100) / 100 )< this.thresholdValue))
      if(this.data[0]){
        this.data[0].value = formatedData;
      }
      const viewType = this.viewType;
      const selectedProjectCount = this.service.getSelectedTrends().length;
      const data = this.data;
      const thresholdValue = this.thresholdValue;
      const elem = this.elem;
      let width = 450;
      const height = (viewType === 'large' && selectedProjectCount === 1) ? 240 : 190;
      const margin = 50;
      const duration = 250;
      const lineOpacity = '1';
      const lineOpacityHover = '0.85';
      const otherLinesOpacityHover = '0.1';
      const lineStroke = '2px';
      const lineStrokeHover = '4px';
      const circleOpacity = '1';
      const circleOpacityOnLineHover = '0.25';
      const circleRadius = 3;
      const circleRadiusHover = 4;
      const marginLeft = 40;
      const marginTop = 35;
      const color = this.color;
      const name = this.name;
      const kpiId = this.kpiId;
      const showPercent = false;
      const showWeek = false;
      const showUnit = this.unit?.toLowerCase() !== 'number' ? this.unit : '';
      const board = this.board;
      const sprintList = data[0]?.value.map(details=>details.date || details?.sortSprint);
      const unitAbbs = {
        'hours' : 'Hrs',
        'sp' : 'SP',
        'days' : 'Day',
        'mrs' : 'MRs',
        'min' : 'Min',
        '%' : '%',
        'check-ins' : 'CI',
        'tickets' : 'T',
        'lines':'lines',
        'unit' : ''
      }
      const tempwidth = d3.select(this.elem).select('#graphContainer').node().offsetWidth || window.innerWidth;
      width = tempwidth - 70;
      let maxXValueCount = 0;
      let maxObjectNo = 0;
      // used to find object whose value is max on x axis
      for (const maxCount in data) {
        if (maxXValueCount < data[maxCount].value.length) {
          maxXValueCount = data[maxCount].value.length;
          maxObjectNo = parseInt(maxCount, 10);
        }
      }
      let maxYValue = 0;
      // used to find maxvalue of y axis
      for (const i in data) {
        for (let j = 0; j < data[i].value.length; j++) {
          data[i].value[j].xName = data[i]?.value[j]?.hasOwnProperty('xAxisTick')
            ? data[i]?.value[j]?.xAxisTick
            : j + 1;
          if (maxYValue < parseInt(data[i].value[j].value, 10)) {
            maxYValue = data[i].value[j].value;
          }
        }
      }
  
      if (maxYValue === 0) {
        maxYValue = 50;
      }
      if (thresholdValue && thresholdValue !== '') {
        if (thresholdValue > maxYValue) {
          maxYValue = thresholdValue;
        }
      }
  
      /* Format Data */
      data.forEach(function (d) {
        d.value.forEach(function (dataObj: { value: number }) {
          dataObj.value = +dataObj.value;
        });
      });
  
      let xScale;
      
      if (viewType === 'large' && selectedProjectCount === 1) {
        xScale = d3
          .scaleBand()
          .domain(sprintList)
          .range([0, width])
          .padding(0)
  
      }else{
        xScale = d3
        .scaleBand()
        .rangeRound([0, width])
        .padding(0)
        .domain(
          data[maxObjectNo].value.map(function (d, i) {
            let returnObj = '';
            if(board == 'dora'){
              returnObj = d.date;
            }else{
              returnObj = i + 1;
            }
            return returnObj;
          }),
        );
      }
  
      let divisor = 10;
      let power = 1;
      let quotient = maxYValue;
      while (quotient >= 1) {
        quotient = quotient / Math.pow(divisor, power);
        ++power;
      }
      divisor = Math.pow(10, power > 1 ? power - 1 : 1);
      if (maxYValue >= 0 && maxYValue <= 5) {
        maxYValue = 5;
      } else if (maxYValue > 5 && maxYValue <= 50) {
        maxYValue = 50;
      } else if (maxYValue > 50 && maxYValue <= 100) {
        maxYValue = 100;
      } else if (maxYValue > 100 && maxYValue <= 200) {
        maxYValue = 200;
      } else if (maxYValue > 200 && maxYValue <= 500) {
        maxYValue = 500;
      } else if (maxYValue > 500) {
        maxYValue += divisor;
      }
  
      if(this.thresholdValue && this.thresholdValue !==0 && isAllBelowFromThreshold && viewType === 'large' && selectedProjectCount === 1){
        maxYValue = this.thresholdValue + 5;
      }
  
      if (this.kpiId === 'kpi149') {
        maxYValue = 5;
      }
  
      const yScale = d3
        .scaleLinear()
        .domain([0, maxYValue])
        .range([height - margin, 0]);
  
      if (selectedProjectCount === 1 && (board === 'executive' || board === 'developer' || board === 'backlog' || board === 'dora')) {
        d3.select(this.elem).select('#horizontalSVG').select('div').remove();
        d3.select(this.elem).select('#horizontalSVG').select('tooltip-container').remove();
        /** Adding tooltip container */
        const tooltipContainer = d3.select(this.elem).select('#horizontalSVG').
          append('div')
          .attr('class', 'tooltip-container')
          .attr('height', height + 35 + 'px')
          .attr('width', width + 'px')
  
          tooltipContainer
          .selectAll('div')
          .data(data[0].value)
          .join('div')
          .attr('class', d=>{
            let cssClass = 'tooltip2';
            let value = Math.round(d.value * 100) / 100;
            if(thresholdValue && thresholdValue !==0  && value < this.thresholdValue){
              cssClass += this.lowerThresholdBG === 'red' ? ' red-bg' : ' white-bg';
            } else {
              cssClass += (this.upperThresholdBG === 'red' && thresholdValue) ? ' red-bg' : ' white-bg';
            }
            return cssClass;
          })
          .style('left', (d,i) => {
            let left = d.date || d.sortSprint;
            if(viewType === 'large' || (board === 'dora' && viewType === 'chart')){
              return xScale(left) + xScale.bandwidth() / 2 + 'px';
            }else{
              return xScale(i+1) + xScale.bandwidth() / 2 + 'px';
            }
  
          })
          .style('top', d => {
            return yScale(Math.round(d.value * 100) / 100)+10 + 'px'
          })
          .text(d => Math.round(d.value * 100) / 100 + ` ${showUnit ? unitAbbs[showUnit?.toLowerCase()] : ''}`)
          .transition()
          .duration(500)
          .style('display', 'block')
          .style('opacity', 1);
      }else{
        d3.select(this.elem).select('#horizontalSVG').select('div').remove();
        d3.select(this.elem).select('#horizontalSVG').select('tooltip-container').remove();
      }
  
      /* Add SVG */
  
      const svgX = d3
        .select(this.elem)
        .select('#horizontalSVG')
        .append('svg')
        .attr('height', height + 35 + 'px')
        .attr('width', width + 'px')
        .style('text-align', 'center')
        .append('g')
        .attr('transform', `translate(${0}, ${marginTop})`);
  
      const svgY = d3
        .select(this.elem)
        .select('#verticalSVG')
        .append('svg')
        .attr('width', '50px')
        .attr('height', height + 35 + 'px')
        .style('text-align', 'center')
        .append('g')
        .attr('transform', `translate(${marginLeft + 15}, ${marginTop})`);
  
      /* Invoke the tip in the context of your visualization */
  
      // Define the div for the tooltip
      const div = d3
        .select(this.elem)
        .select('#multiLineChart')
        .append('div')
        .attr('class', 'tooltip')
        .style('display', 'none')
        .style('opacity', 0);
  
      /* Add Axis into SVG */
      const xAxis = d3.axisBottom(xScale);
      /*var xAxis = d3.axisBottom(xScale).ticks(7);
       */
      const yAxis = d3.axisLeft(yScale).ticks(5).tickFormat(function(tickval) {
        return tickval >= 1000 ? tickval/1000 + "k" : tickval;
      });
  
      const XCaptionSVG = d3
        .select(this.elem)
        .select('#xCaptionContainer')
        .append('text');
  
      svgX
        .append('g')
        .attr('class', 'x axis')
        .attr('transform', `translate(0, ${height - margin})`)
        .call(xAxis)
        .selectAll(".tick text")
        .call(this.wrap, xScale.bandwidth());
  
      const XCaption = XCaptionSVG.append('text')
        .attr('x', width / 2 - 24)
        .attr('y', 46)
        .attr('transform', 'rotate(0)')
        .attr('fill', '#437495')
        .attr('font-size', '12px');
  
      if (this.xCaption) {
        XCaption.text(this.xCaption);
      } else {
        XCaption.text('Sprints');
      }
  
      if (kpiId === 'kpi114' || kpiId === 'kpi74' || kpiId === 'kpi997') {
        XCaption.text('Months');
      }
  
      // this is used for adding horizontal lines in graph
      const YCaption = svgY
        .append('g')
        .attr('class', 'y axis')
        .call(yAxis)
        .append('text')
        .attr('x', -30)
        .attr('y', -30)
        .attr('transform', 'rotate(-90)')
        .attr('fill', '#437495')
        .attr('font-size', '12px');
  
      // adding yaxis caption
  
      if (this.yCaption) {
        YCaption.text(this.yCaption);
      } else {
        YCaption.text('Values');
      }
  
      // threshold line
      if (thresholdValue && thresholdValue !== '') {
        svgX
          .append('svg:line')
          .attr('x1', 0)
          .attr('x2', width - margin - 10)
          .attr('y1', yScale(thresholdValue))
          .attr('y2', yScale(thresholdValue))
          .style('stroke', '#333333')
          .style('stroke-dasharray', '4,4')
          .attr('class', 'thresholdline');
        svgX
          .append('text')
          .attr('x', width - 40)
          .attr('y', yScale(thresholdValue))
          .attr('dy', '.5em')
          .attr('text-anchor', 'end')
          .text(this.thresholdValue)
          .attr('class', 'thresholdlinetext');
      }
  
      // gridlines
      svgX
        .selectAll('line.gridline')
        .data(yScale.ticks(5))
        .enter()
        .append('svg:line')
        .attr('x1', 0)
        .attr('x2', width)
        .attr('y1', function (d) {
          return yScale(d);
        })
        .attr('y2', function (d) {
          return yScale(d);
        })
        .style('stroke', '#dedede')
        .style('fill', 'none')
        .attr('class', 'gridline');
  
  
      /* Add line into SVG acoording to data */
      const line = d3
        .line()
        .x((d, i) => {
          if(board == 'dora'){
            return xScale(d.date)
          }else if(viewType  === 'large' && selectedProjectCount === 1){
            return xScale(d.date || d.sortSprint)
          }else{
            return xScale(i+1)
          }
        })
        .y((d) => yScale(d.value));
  
      const lines = svgX.append('g').attr('class', 'lines')
      .attr('transform', `translate(${xScale.bandwidth()/2}, ${0})`);
  
      function tweenDash() {
        const l = this.getTotalLength();
        const i = d3.interpolateString('0,' + l, l + ',' + l);
        return function (t) {
          return i(t);
        };
      }
  
      function transition(path) {
        path
          .transition()
          .duration(1500)
          .attrTween('stroke-dasharray', tweenDash)
          .on('end', () => {
            d3.select(this).call(transition);
          });
      }
  
      lines
        .selectAll('.line-group')
        .data(data)
        .enter()
        .append('g')
        .attr('class', 'line-group')
        .on('mouseover', function (d, i) {
          svgX
            .append('text')
            .attr('class', 'title-text')
            .style('fill', color[i])
            .text(d.data)
            .attr('text-anchor', 'middle')
            .attr('x', (width - margin) / 2)
            .attr('y', -10);
        })
        .on('mouseout', function (d) {
          svgX.select('.title-text').remove();
        })
        .append('path')
        .attr('class', function (d, i) {
          const className = 'line' + i;
          return 'line ' + className;
        })
        .attr('d', (d) => line(d.value))
        .call(transition)
        .style('stroke', (d, i) => color[i])
        .style('opacity', lineOpacity)
        .style('stroke-dasharray', function (d, i) {
          if (d['filter']?.toLowerCase() == 'average coverage' || d['data']?.toLowerCase() == 'average coverage') {
            return '4,4';
          }
        })
        .style('fill', 'none')
        .style('stroke-width', '2')
        .on('mouseover', function (d) {
          d3.select(elem)
            .selectAll('.line')
            .style('opacity', otherLinesOpacityHover);
          d3.select(elem)
            .selectAll('.circle')
            .style('opacity', circleOpacityOnLineHover);
          d3.select(this)
            .style('opacity', lineOpacityHover)
            .style('stroke-width', lineStrokeHover)
            .style('cursor', 'pointer');
        })
        .on('mouseout', function (d) {
          d3.selectAll('.line').style('opacity', lineOpacity);
          d3.selectAll('.circle').style('opacity', circleOpacity);
          d3.select(this)
            .style('stroke-width', lineStroke)
            .style('cursor', 'none');
        });
  
      /* Add circles (data) on the line */
      lines
        .selectAll('circle-group')
        .data(data)
        .enter()
        .append('g')
        .attr('class', function (d, i) {
          return 'circlegroup' + i;
        })
        .style('fill', (d, i) => color[i])
        .style('stroke', (d, i) => color[i])
        .selectAll('circle')
        .data((d) => d.value)
        .enter()
        .append('g')
        .attr('class', 'circle')
        .on('mouseover', function (event, d) {
          const topValue = 80;
          if (d.hoverValue) {
            div
              .transition()
              .duration(200)
              .style('display', 'block')
              .style('position', 'fixed')
              .style('opacity', 0.9);
  
            const circle = event.target;
            const { top: yPosition, left: xPosition } =
              circle.getBoundingClientRect();
  
            div
              .html(
                `${d.date || d.sSprintName}` +
                ' : ' +
                "<span class='toolTipValue'> " +
                `${Math.round(d.value * 100) / 100 + ' ' + showUnit}` +
                '</span>',
              )
              .style('left', xPosition + 20 + 'px')
              // .style('top', yScale(d.value) - topValue + 'px');
              .style('top', yPosition + 20 + 'px');
            for (const hoverData in d.hoverValue) {
              div
                .append('p')
                .html(
                  `${hoverData}` +
                  ' : ' +
                  "<span class='toolTipValue'> " +
                  `${d.hoverValue[hoverData]}` +
                  ' </span>',
                );
            }
          }
        })
        .on('mouseout', function (d) {
          div
            .transition()
            .duration(500)
            .style('display', 'none')
            .style('opacity', 0);
        })
        .append('circle')
        .attr('cx', function (d, i) {
  
          if(board == 'dora'){
             return xScale(d.date);
          }else if(viewType  === 'large' && selectedProjectCount === 1){
            return xScale(d.date || d.sortSprint)
          }else{
            return xScale(i+1)
          }
        })
        .attr('cy', (d) => yScale(d.value))
        .attr('r', circleRadius)
        .style('stroke-width', 1)
        .style('opacity', circleOpacity)
        .on('mouseover', function (d) {
          d3.select(this)
            .transition()
            .duration(duration)
            .attr('r', circleRadiusHover);
        })
        .on('mouseout', function (d) {
          d3.select(this).transition().duration(duration).attr('r', circleRadius);
        });
  
      // used to allign data on x axis ticks
      svgX
        .select('.x')
        .selectAll('.tick')
        .each(function (dataObj, index) {
          const tick = d3.select(this);
          if (data[0]?.value[0] && data[0]?.value[0]?.xAxisTick &&  !(viewType === 'large' && selectedProjectCount === 1)) {
            const textElement = this.getElementsByTagName('text');
            textElement[0].textContent = data[0].value[dataObj - 1]?.xAxisTick;
          }
          const string = tick.attr('transform');
          const translate = string
          .substring(string.indexOf('(') + 1, string.indexOf(')'))
          .split(',');
          translate[0] = parseInt(translate[0], 10);
          tick.attr(
            'transform',
            'translate(' + translate[0] + ',' + translate[1] + ')',
          );
          if (index === 0) {
            // if (maxXValueCount === 1) {
            //     translate[0] = parseInt(translate[0], 10) - 36;
            // } else if (maxXValueCount === 2) {
            //     translate[0] = parseInt(translate[0], 10) - 20;
            // } else if (maxXValueCount === 3) {
            //     translate[0] = parseInt(translate[0], 10) - 12;
            // } else if (maxXValueCount === 4) {
            //     translate[0] = parseInt(translate[0], 10) - 10;
            // } else {
            //     translate[0] = parseInt(translate[0], 10) - 6;
            // }
            svgX
              .select('.lines')
              .attr(
                'transform',
                'translate(' + translate[0] + ',' + translate[1] + ')',
              );
          }
        });
  
      if (this.kpiId == 'kpi17') {
        d3.select(this.elem).select('#legendContainer').remove();
        const legendDiv = d3.select(this.elem).select('#multiLineChart').append('div')
          .attr('id', 'legendContainer')
          .style('margin-left', 60 + 'px')
          .append('div');
  
        legendDiv.transition()
          .duration(200)
          .style('display', 'block')
          .style('opacity', 1)
          .attr('class', 'p-d-flex p-flex-wrap normal-legend');
  
        let colorArr = [];
        for(let i=0; i<color?.length;i++){
          if(!colorArr.includes(color[i])){
            colorArr.push(color[i]);
          }
        }
  
        if(colorArr?.length>0){
          let htmlString = '<div class="legend_item" style="display:flex; align-items:center;"><div>';
  
  
          colorArr.forEach((d, i) => {
            htmlString += `<div class="legend_color_indicator" style="margin:0 5px 2px 0;width:15px; border-width:2px; border-style:dashed; border-color: ${color[i]}"></div>`;
          });
  
          htmlString += '</div><div class="font-small"> Average Coverage</div></div>'
  
          legendDiv.html(htmlString);
        }
      }
      const content = this.elem.querySelector('#horizontalSVG');
      content.scrollLeft += width;
    }
  }


  wrap(text, width) {
    text.each(function() {
      var text = d3.select(this),
          words = text.text().split(/\s+/).reverse(),
          word,
          line = [],
          lineNumber = 0,
          lineHeight = 1.1, // ems
          y = text.attr("y"),
          dy = parseFloat(text.attr("dy")),
          tspan = text.text(null).append("tspan").attr("x", 0).attr("y", y).attr("dy", dy + "em")
      while (word = words.pop()) {
        line.push(word)
        tspan.text(line.join(" "))
        if (tspan.node().getComputedTextLength() > (width-5)) {
          line.pop()
          tspan.text(line.join(" "))
          line = [word]
          tspan = text.append("tspan").attr("x", 0).attr("y", y).attr("dy", `${++lineNumber * lineHeight + dy}em`).text(word)
        }
      }
    })
  }

  ngOnDestroy() {
    // this is used for removing svg already made when value is updated
    d3.select(this.elem).select('#verticalSVG').select('svg').remove();
    d3.select(this.elem).select('#horizontalSVG').select('svg').remove();
    d3.select(this.elem).select('#xCaptionContainer').select('text').remove();
    d3.select(this.elem).select('#legendContainer').remove();
    this.data = [];
    this.elemObserver.unobserve(this.elem);
  }
}
