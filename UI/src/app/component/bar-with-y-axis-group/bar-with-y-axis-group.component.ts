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

@Component({
  selector: 'app-bar-with-y-axis-group',
  templateUrl: './bar-with-y-axis-group.component.html',
  styleUrls: ['./bar-with-y-axis-group.component.css']
})
export class BarWithYAxisGroupComponent implements OnInit, OnChanges {
  @Input() data: any;
  @Input() color: any;
  @Input() yCaption: string;
  @Input() xCaption: string;
  @Input() unit: string;
  @Input() selectedtype: string;
  elem: any;
  dataPoints: any;
  sprintList : Array<any> = [];
  @Input() viewType :string = 'chart'
  @Input() lowerThresholdBG : string;
  @Input() upperThresholdBG : string;
  @Input() yAxisOrder : Array<any>;
  @Input() thresholdValue : number;
  resizeObserver = new ResizeObserver(entries => {
    const data = this.formatData(this.data);
    this.draw(data);
  });

  constructor(private viewContainerRef: ViewContainerRef, private service: SharedService) { }

  ngOnInit(): void {
    this.service.showTableViewObs.subscribe(view => {
      this.viewType = view;
     });
   }

  ngOnChanges(changes: SimpleChanges) {
    if (this.selectedtype?.toLowerCase() === 'kanban'|| this.service.getSelectedTab().toLowerCase() === 'developer') {
      this.xCaption = this.service.getSelectedDateFilter();
    }
      this.elem = this.viewContainerRef.element.nativeElement;
      this.dataPoints = this.data.length;
      const data = this.formatData(this.data);
      this.draw(data);
  }

  formatData(data) {
    const result = [];
    const newObj = {};
    newObj['value'] = [];

    // eslint-disable-next-line @typescript-eslint/prefer-for-of
    for (let i = 0; i < data[0].value.length; i++) {
      if (data[0].value[i].hoverValue) {
        newObj['value'].push({
          value: data[0].value[i].value,
          hoverValue: data[0].value[i].hoverValue,
          sSprintName: data[0].value[i].sSprintName,
          rate: data[0].data,
          date :data[0].value[i].date
        });
      } else {
        newObj['value'].push({
          value: data[0].value[i].value,
          sSprintName: data[0].value[i].sSprintName,
          rate: data[0].data,
          date :data[0].value[i].date
        });
      }
    }

    newObj['value'].forEach((element, index) => {
      const newNewObj = {};
      newNewObj['categorie'] = index + 1;
      newNewObj['value'] = [element];
      result.push(newNewObj);
    });

    for (let i = 1; i < data.length; i++) {
      for (let j = 0; j < data[i].value.length; j++) {
        // eslint-disable-next-line @typescript-eslint/no-shadow
        const newObj = {};
        newObj['value'] = [];
        if (result[j] && result[j]['categorie'] && j + 1 === result[j]['categorie']) {
          if (data[i].value[j].hoverValue) {
            result[j].value.push({
              value: data[i].value[j].value,
              hoverValue: data[i].value[j].hoverValue,
              sSprintName: data[i].value[j].sSprintName,
              rate: data[i].data,
            });
          } else {
            result[j].value.push({
              value: data[i].value[j].value,
              sSprintName: data[i].value[j].sSprintName,
              rate: data[i].data,
            });
          }
        }
      }
    }

    return result;
  }

  draw(data) {
    const unitAbbs = {
      'hours' : 'Hrs',
      'sp' : 'SP',
      'days' : 'Day',
      'mrs' : 'MRs',
      'min' : 'Min',
      '%' : '%',
      'check-ins' : 'CI',
      'tickets' : 'T',
      'unit' :'unit'
    }
    let sprintList = [];
    const viewType = this.viewType;
    const selectedProjectCount = this.service.getSelectedTrends().length;
    const showUnit = this.unit?.toLowerCase() !== 'number' ? this.unit : '';
    d3.select(this.elem).select('#verticalSVG').select('svg').remove();
    d3.select(this.elem).select('#horizontalSVG').select('svg').remove();
    d3.select(this.elem).select('#xCaptionContainer').select('text').remove();
    if (viewType === 'large' && selectedProjectCount === 1) {
      data = data.map(details => {
        let finalResult = {};
        const XValue = details.value[0].sSprintName || details.value[0].date;
        const projectName = '_'+this.service.getSelectedTrends()[0]?.nodeName;
        const removeProject = XValue.includes(projectName) ? XValue.replace(projectName,'') : XValue;
        finalResult = { ...details,sortName:removeProject, value: [{ ...details.value[0], sortSprint: removeProject}] }
        sprintList.push(removeProject)
        return finalResult
      })
    }
    
    const self = this;

    const categoriesNames = data.map((d) => d.categorie);
    const rateNames = data[0].value.map((d) => d.rate);
    const paddingTop = 24; 

    const margin = { top: 35, right: 50, bottom: 50, left: 50 };
    const barWidth = 20;
    const containerWidth = d3.select(this.elem).select('#chart').node().offsetWidth || window.innerWidth;
    const resizeWidth = (containerWidth > (data.length * barWidth * 10) ? containerWidth : (data.length * barWidth * 10))
    const width = data.length <= 5 ? containerWidth - 70 : resizeWidth;
    const height = (viewType === 'large' && selectedProjectCount === 1) ? 250 - paddingTop : 210 - paddingTop;
    const paddingFactor = width < 600 ? 0.30 : 0.55;

    const xScale = d3.scaleBand().range([0, width]).padding([((6 + this.dataPoints) / (3 * this.dataPoints)) * paddingFactor]);;

    const barScale = d3.scaleBand();
    let tempAxis;
    if (viewType === 'large' && selectedProjectCount === 1) {
      /** Temporary axis for wrapping text only */
      tempAxis =  d3.scaleBand().rangeRound([0, width - margin.left]).domain(sprintList)
      xScale.domain(sprintList);
    }else{
      xScale.domain(categoriesNames);
    }
    barScale.domain(rateNames).range([0, xScale.bandwidth()]);

    let y = d3.scaleBand().range([height - margin.top, 0]).domain(Object.values(this.yAxisOrder).reverse());;

    let tickPadding = 10;
    if (width < 600) {
      tickPadding = 6;
    }

    const xAxis = d3.axisBottom(xScale).tickSize(0).tickPadding(tickPadding);

    const yAxis = d3.axisLeft(y).ticks(5)

    const color = d3.scaleOrdinal().range(this.color);

    /** Adding tooltip */
    let tooltipContainer;
    if (selectedProjectCount === 1) {
      d3.select(this.elem).select('#horizontalSVG').select('div').remove();
      d3.select(this.elem).select('#horizontalSVG').select('tooltip-container').remove();
      tooltipContainer = d3.select(this.elem).select('#horizontalSVG').
        append('div')
        .attr('class', 'tooltip-container')
        .attr('height', height + 35 + 'px')
        .attr('width', width + 'px')
        .selectAll('div')
        .data(data)
        .join('div')
        .attr('class', d=>{
          let cssClass = 'tooltip2';
          let value = d.lineValue;
          if(this.thresholdValue && this.thresholdValue !==0  && value < this.thresholdValue){
            cssClass += this.lowerThresholdBG === 'red' ? ' red-bg' : ' white-bg';
          } else {
            cssClass += (this.upperThresholdBG === 'red' && this.thresholdValue) ? ' red-bg' : ' white-bg';
          }
          return cssClass;
        })
        .style('left', (d,i) => {
          let left = d.value[0]?.date || d.value[0]?.sortSprint;
          if(viewType === 'large'){
            return xScale(left) + xScale.bandwidth() / 2 + 'px';
          }else{
            return xScale(i+1) + xScale.bandwidth() / 2 + 'px';
          }
        })
        .style('top', d => y(this.yAxisOrder[d.value[0].value]) + 'px')
        .text(d => (d.value[0].value)+ ` ${showUnit ? unitAbbs[showUnit?.toLowerCase()] : ''}`)
        .transition()
        .duration(500)
        .style('display', 'block')
        .style('opacity', 1);
    }else{
      d3.select(this.elem).select('#horizontalSVG').select('div').remove();
      d3.select(this.elem).select('#horizontalSVG').select('tooltip-container').remove();
    }

    const svgX = d3
      .select(this.elem)
      .select('#horizontalSVG')
      .append('svg')
      .attr('width', width)
      .attr('height', height);


    const svgY = d3
      .select(this.elem)
      .select('#verticalSVG')
      .append('svg')
      .attr('height', height)
      .attr('width', 50);

    const xAxisText = svgX
      .append('g')
      .attr('class', 'xAxis')
      .attr('transform', 'translate(0,' + (height - margin.top) + ')')
      .attr('stroke-width', '1')
      .attr('opacity', '1')
      .call(xAxis)
      .selectAll(".tick text")

      if (viewType === 'large' && selectedProjectCount === 1) {
        xAxisText.each((d, i, nodes) => {
          const textElement = d3.select(nodes[i]);
          const width = tempAxis.bandwidth(); 
          this.wrap(textElement, width);
        });
      }

    const XCaption = d3.select(this.elem).select('#xCaptionContainer').append('text');

    if (this.xCaption) {
      XCaption.text(this.xCaption);
    } else {
      XCaption.text('Sprints');
    }

    svgY
      .append('g')
      .attr('class', 'yAxis')
      .call(yAxis.tickSize(0))
      .style('font-size', '10px')
      .attr('transform', `translate(${margin.left} 0)`)
      .append('text')
      .attr('x', -60)
      .attr('y', -40)
      .attr('transform', 'rotate(-90)')
      .attr('fill', '#437495')
      .attr('font-size', '12px')
      .text(self.yCaption);

    // gridlines
    svgX.selectAll('line.gridline').data(Object.values(this.yAxisOrder)).enter()
      .append('svg:line')
      .attr('x1', 0)
      .attr('x2', width)
      .attr('y1', (d) =>y(d) + y.bandwidth()/2)
      .attr('y2', (d) => y(d) + y.bandwidth()/2)
      .style('stroke', '#dedede')
      .style('fill', 'none')
      .attr('class', 'gridline');

    const xTick = self.dataPoints === 1 ? 12 : 0;
    svgX
      .select('.xAxis')
      .selectAll('.tick text')
      .attr('y', 10)
      .attr('x', xTick)
      .style('font-size', '10px')
      .style('fill', 'black');

    svgX
      .select('.xAxis')
      .selectAll('line')
      .attr('x1', xTick)
      .attr('x2', xTick)
      .attr('y1', 0)
      .attr('y2', 5)
      .style('stroke', '#333333');

    const slice = svgX
      .selectAll('.slice')
      .data(data)
      .enter()
      .append('g')
      .attr('transform', (d) => 'translate(' + xScale(d.categorie) + ',0)');

    slice
      .selectAll('rect')
      .data((d) => d.value)
      .enter()
      .append('rect')
      .attr('width', barWidth)
      .attr('x', (d, i) =>{
        if (viewType === 'large' && selectedProjectCount === 1) {
          return paddingFactor < 0.55 && data.length <= 5 && self.dataPoints === 1 ? xScale(d.sortSprint || d.date) + barWidth / 1.5 : xScale(d.sortSprint || d.date)
        }else{
          return paddingFactor < 0.55 && data.length <= 5 && self.dataPoints === 1 ? barScale(d.rate) + barWidth / 1.5 : barScale(d.rate)
        }
      })
      .style('fill', (d) =>color(d.rate))
      .attr('y', (d) =>y(this.yAxisOrder[d.value]) + y.bandwidth()/2)
      .attr('height', (d) => (height - margin.top - (y(this.yAxisOrder[d.value]) + y.bandwidth()/2)))
      .attr('class', 'bar')
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

  ngAfterViewInit() {
    this.resizeObserver.observe(d3.select(this.elem).select('#chart').node());
  }

  ngOnDestroy(): void {
    this.data = []
    this.resizeObserver.unobserve(d3.select(this.elem).select('#chart').node());
  }

}



