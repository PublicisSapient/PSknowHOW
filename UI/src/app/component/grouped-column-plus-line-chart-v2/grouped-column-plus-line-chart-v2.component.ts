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
  selector: 'app-grouped-column-plus-line-chart-v2',
  templateUrl: './grouped-column-plus-line-chart-v2.component.html',
  styleUrls: ['./grouped-column-plus-line-chart-v2.component.css'],
})
export class GroupedColumnPlusLineChartV2Component
  implements OnInit, OnChanges
{
  @Input() data: any;
  @Input() lineChart: boolean = true; // Decide whether a line is needed in the bar+line chart
  @Input() thresholdValue: number;
  @Input() color: any;
  @Input() yCaption: string;
  @Input() xCaption: string;
  @Input() unit: string;
  @Input() barLegend: string;
  @Input() lineLegend: string;
  @Input() selectedtype: string;
  @Input() isXaxisGroup: boolean = false; // Decide whether the x-axis should be numeric or non-numeric
  @Input() kpiId: string; // id of the kpi
  elem: any;
  drillDownLevel: number;
  lastLevel: any;
  dataPoints: any;
  barChart = true;
  maxValue = 1000;
  unmodifiedData: any = [];
  sprintList: Array<any> = [];
  height: number = 0;
  @Input() viewType: string = 'chart';
  @Input() lowerThresholdBG: string;
  @Input() upperThresholdBG: string;
  resizeObserver = new ResizeObserver((entries) => {
    const data = this.transform2(this.data);
    this.draw2(data);
  });
  counter: number = 0;

  constructor(
    private viewContainerRef: ViewContainerRef,
    private service: SharedService,
  ) {}

  ngOnInit(): void {
    this.service.showTableViewObs.subscribe((view) => {
      this.viewType = view;
    });
  }

  ngOnChanges(changes: SimpleChanges) {
    if (
      this.selectedtype?.toLowerCase() === 'kanban' ||
      this.service.getSelectedTab().toLowerCase() === 'developer'
    ) {
      this.xCaption = this.service.getSelectedDateFilter();
    }
    this.elem = this.viewContainerRef.element.nativeElement;
    this.unmodifiedData = JSON.parse(JSON.stringify(this.data));
    this.dataPoints = this.unmodifiedData.length;
    const data = this.transform2(this.data);
    this.draw2(data);
  }

  transform2(data) {
    const result = [];
    const newObj = {};
    newObj['value'] = [];

    // eslint-disable-next-line @typescript-eslint/prefer-for-of
    for (let i = 0; i < data[0].value.length; i++) {
      if (data[0].value[i].hoverValue) {
        newObj['value'].push({
          value: data[0].value[i].value,
          lineValue: data[0].value[i].lineValue,
          hoverValue: data[0].value[i].hoverValue,
          sSprintName: data[0].value[i].sSprintName,
          rate: data[0].data,
          date: data[0].value[i].date,
        });
      } else {
        newObj['value'].push({
          value: data[0].value[i].value,
          lineValue: data[0].value[i].lineValue,
          sSprintName: data[0].value[i].sSprintName,
          rate: data[0].data,
          date: data[0].value[i].date,
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
        if (
          result[j] &&
          result[j]['categorie'] &&
          j + 1 === result[j]['categorie']
        ) {
          if (data[i].value[j].hoverValue) {
            result[j].value.push({
              value: data[i].value[j].value,
              lineValue: data[i].value[j].lineValue,
              hoverValue: data[i].value[j].hoverValue,
              sSprintName: data[i].value[j].sSprintName,
              rate: data[i].data,
            });
          } else {
            result[j].value.push({
              value: data[i].value[j].value,
              lineValue: data[i].value[j].lineValue,
              sSprintName: data[i].value[j].sSprintName,
              rate: data[i].data,
            });
          }
        }
      }
    }

    return result;
  }

  draw2(data) {
    const unitAbbs = {
      hours: 'Hrs',
      sp: 'SP',
      days: 'Day',
      mrs: 'MRs',
      min: 'Min',
      '%': '%',
      'check-ins': 'CI',
      tickets: 'T',
    };
    let sprintList = [];
    const kpiId = this.kpiId;
    const viewType = this.viewType;
    const selectedProjectCount = this.service.getSelectedTrends().length;
    const showUnit = this.unit?.toLowerCase() !== 'number' ? this.unit : '';
    d3.select(this.elem).select('#verticalSVG').select('svg').remove();
    d3.select(this.elem).select('#horizontalSVG').select('svg').remove();
    d3.select(this.elem).select('#svgLegend').select('svg').remove();
    d3.select(this.elem).select('#legendIndicator').select('svg').remove();
    d3.select(this.elem).select('#xCaptionContainer').select('text').remove();
    if (this.isXaxisGroup === true && selectedProjectCount === 1) {
      data = data.map((details) => {
        let finalResult = {};
        const XValue = details.value[0].sSprintName || details.value[0].date;
        const sortValue = XValue;
        finalResult = {
          ...details,
          sortName: sortValue,
          value: [{ ...details.value[0], sortSprint: sortValue }],
        };
        sprintList.push(sortValue);
        return finalResult;
      });
    }
    const isAllBelowFromThreshold = data.every(
      (details) => details.value[0].lineValue < this.thresholdValue,
    );

    const self = this;

    const categoriesNames = data.map((d) => d.categorie);
    const rateNames = data[0].value.map((d) => d.rate);
    const paddingTop = 24;

    const margin = { top: 35, right: 50, bottom: 50, left: 50 };

    const containerWidth =
      d3.select(this.elem).select('#chart').node().offsetWidth ||
      window.innerWidth;
    // const resizeWidth = (containerWidth > (data.length * barWidth * 10) ? containerWidth : (data.length * barWidth * 10))
    const width = containerWidth - 40;
    const height =
      this.isXaxisGroup === true && selectedProjectCount === 1 ? 250 : 210;
    this.height = height;
    const paddingFactor = 0;

    const x0 = d3.scaleBand().range([0, width]); // .padding([((6 + this.dataPoints) / (3 * this.dataPoints)) * paddingFactor]);

    const x1 = d3.scaleBand();

    let xScale;
    try {
      const unFormatedData = JSON.parse(JSON.stringify(self.unmodifiedData));
      unFormatedData[0].value = unFormatedData[0].value.map((details) => {
        const XValue = details.sSprintName || details.date;
        const sortValue = XValue;
        return { ...details, sortSprint: sortValue };
      });
      const newRawData = unFormatedData;
      let maxObjectNo = 0;
      let maxXValueCount = 0;
      // used to find object whose value is max on x axis
      for (const maxCount in newRawData) {
        if (maxXValueCount < newRawData[maxCount].value.length) {
          maxXValueCount = newRawData[maxCount].value.length;
          maxObjectNo = parseInt(maxCount, 10);
        }
      }

      if (this.isXaxisGroup === true && selectedProjectCount === 1) {
        xScale = d3.scaleBand().rangeRound([0, width]).domain(sprintList);
        // .padding([((6 + self.dataPoints) / (3 * self.dataPoints)) * paddingFactor]);
      } else {
        xScale = d3
          .scaleBand()
          .rangeRound([0, width])
          .domain(newRawData[maxObjectNo].value.map((d, i) => i + 1));
        // .padding([((6 + self.dataPoints) / (3 * self.dataPoints)) * paddingFactor]);
      }

      const y = d3.scaleLinear().range([height - margin.top, 0]);
      let tempAxis;
      if (this.isXaxisGroup === true && selectedProjectCount === 1) {
        /** Temporary axis for wrapping text only */
        tempAxis = d3
          .scaleBand()
          .rangeRound([0, width - margin.left])
          .domain(sprintList);
        x0.domain(sprintList).padding(0.5);
      } else {
        x0.domain(categoriesNames).padding(0.5);
      }
      x1.domain(rateNames).range([0, x0.bandwidth()]);

      const barWidth = x1.bandwidth();

      const maxBarValue = d3.max(data, (categorie) =>
        d3.max(categorie.value, (d) => d.value),
      );

      const maxLineValue = d3.max(data, (categorie) =>
        d3.max(categorie.value, (d) => d.lineValue),
      );

      let divisor = 10;
      let power = 1;
      let maxVal = maxBarValue >= maxLineValue ? maxBarValue : maxLineValue;
      if (self.thresholdValue) {
        maxVal = maxVal >= self.thresholdValue ? maxVal : self.thresholdValue;
      }
      let quotient = maxVal;
      while (quotient >= 1) {
        quotient = quotient / Math.pow(divisor, power);
        ++power;
      }
      divisor = Math.pow(10, power > 1 ? power - 1 : 1);

      let maxYValue = maxVal;

      if (maxYValue > 0 && maxYValue <= 49) {
        maxYValue = 50;
      } else if (maxYValue > 49 && maxYValue <= 99) {
        maxYValue = 100;
      } else if (maxYValue > 99 && maxYValue <= 199) {
        maxYValue = 200;
      } else if (maxYValue > 199 && maxYValue <= 499) {
        maxYValue = 500;
      } else if (maxYValue > 499) {
        maxYValue += divisor;
      }

      if (!maxYValue) {
        maxYValue = 50;
      }

      // if (this.thresholdValue && this.thresholdValue !== 0 && isAllBelowFromThreshold && this.isXaxisGroup === true && selectedProjectCount === 1) {
      //   maxYValue = this.thresholdValue + 5;
      // }

      y.domain([0, maxYValue]);

      const yAxis = d3
        .axisLeft(y)
        .ticks(5)
        .tickSize(-height, 0, 0)
        .tickFormat(function (tickval) {
          return tickval >= 1000 ? tickval / 1000 + 'k' : tickval;
        });

      const color = d3.scaleOrdinal().range(this.color);

      /** Adding tooltip container */
      let tooltipContainer;
      if (selectedProjectCount === 1) {
        d3.select(this.elem).select('#horizontalSVG').select('div').remove();
        d3.select(this.elem)
          .select('#horizontalSVG')
          .select('tooltip-container')
          .remove();
        tooltipContainer = d3
          .select(this.elem)
          .select('#horizontalSVG')
          .append('div')
          .attr('class', 'tooltip-container')
          .attr('height', height + 35 + 'px')
          .attr('width', width + 'px');
      } else {
        d3.select(this.elem).select('#horizontalSVG').select('div').remove();
        d3.select(this.elem)
          .select('#horizontalSVG')
          .select('tooltip-container')
          .remove();
      }

      const svgX = d3
        .select(this.elem)
        .select('#horizontalSVG')
        .append('svg')
        .attr('width', width)
        .attr('height', height + 5)
        .attr('transform', `translate(${0}, ${5})`);

      const svgY = d3
        .select(this.elem)
        .select('#verticalSVG')
        .append('svg')
        .attr('height', height)
        .attr('width', 50)
        .attr('transform', `translate(${0}, ${-20})`);

      const svgLegend = d3
        .select(this.elem)
        .select('#svgLegend')
        .append('svg')
        .attr('width', width + margin.right - 50)
        .attr('height', 50)
        .append('g');

      // eslint-disable-next-line @typescript-eslint/naming-convention
      const XCaption = d3
        .select(this.elem)
        .select('#xCaptionContainer')
        .append('text');

      if (this.xCaption) {
        XCaption.text(this.xCaption);
      } else {
        XCaption.text('Sprints');
      }

      XCaption.style('fill', '#49535E').style('font-size', '12px');

      svgY
        .append('g')
        .attr('class', 'yAxis')
        .call(yAxis.tickSize(0))
        .style('opacity', '0')
        .attr('transform', 'translate(' + margin.left + ',' + paddingTop + ')')
        .append('text')
        .attr('x', -60)
        .attr('y', -30)
        .attr('transform', 'rotate(-90)')
        .attr('fill', '#49535E')
        .attr('font-size', '12px')
        .text(self.yCaption);

      // gridlines
      svgX
        .selectAll('line.gridline')
        .data(y.ticks(4))
        .enter()
        .append('svg:line')
        .attr('x1', 0)
        .attr('x2', width)
        .attr('y1', (d) => y(d))
        .attr('y2', (d) => y(d))
        .style('stroke', '#dedede')
        .style('fill', 'none')
        .attr('class', 'gridline');

      const xAxisGrid = d3
        .axisBottom(x0)
        .tickSize(-(height - 30))
        .ticks(5);
      svgX
        .append('g')
        .attr('class', 'y-axis-grid')
        .call(xAxisGrid)
        .attr('transform', `translate(${0}, ${height - 35})`);

      d3.select(this.elem)
        .select('.y-axis-grid')
        .selectAll('line')
        .style('stroke', '#EAEDF0')
        .style('fill', 'none');

      d3.select(this.elem)
        .select('#horizontalSVG')
        .select('.x-axis')
        .selectAll('.tick line')
        .style('display', 'none');

      d3.select(this.elem)
        .select('#verticalSVG')
        .select('.y.axis')
        .selectAll('.tick line')
        .style('display', 'none');

      d3.select(this.elem)
        .select('#horizontalSVG')
        .select('.x-axis')
        .select('.domain')
        .style('display', 'none');

      svgY
        .select('.yAxis')
        .transition()
        .duration(500)
        .delay(1300)
        .style('opacity', '1')
        .style('font-size', '10px');

      const slice = svgX
        .selectAll('.slice')
        .data(data)
        .enter()
        .append('g')
        .attr('class', 'rounded-bar')
        .attr('transform', (d) =>
          this.isXaxisGroup === true
            ? 'translate(' + x0(d.sortName) + ',0)'
            : 'translate(' + x0(d.categorie) + ',0)',
        );

      // Applying Bar tooltip for bar chart only.Bar tooltip is not required for bar+line chart.
      if (this.lineChart === false) {
        d3.selectAll('.rounded-bar')
          .on('mouseover', function (event, d) {
            if (d?.value[0]?.hoverValue) {
              const circle = event.target;
              const { top: yPosition, left: xPosition } =
                circle.getBoundingClientRect();

              div
                .transition()
                .duration(200)
                .style('display', 'block')
                .style('opacity', 0.9);

              let dataString = '';
              let htmlString = '';

              for (let key in d.value[0].hoverValue) {
                dataString += `<div class=\'toolTipValue p-d-flex p-align-center\'><div class="stack-key p-mr-1">${key}</div><div>${d.value[0].hoverValue[key]}</div></div>`;
              }

              htmlString =
                "<div class='toolTip'> " + `${dataString}` + '</div>';
              div
                .html(htmlString)
                .style('left', xPosition + 20 + 'px')
                .style('top', yPosition + 'px')
                .style('position', 'fixed')
                .style('align', 'left');
            }
          })
          .on('mouseout', function (e, d) {
            div
              .transition()
              .duration(500)
              .style('display', 'none')
              .style('opacity', 0);
          });
      }

      const rx = x1.bandwidth() / 2;
      const ry = x1.bandwidth() / 2;
      slice
        .selectAll('arc')
        .data((d) => d.value)
        .enter()
        .append('path')
        .style('fill', (d) => color(d.rate))
        .attr('d', (d) => {
          if (height - margin.top - y(d.value) >= rx) {
            return `
              M${x1(d.rate)},${y(d.value) + ry}
              a${rx},${ry} 0 0 1 ${rx},${-ry}
              h${x1.bandwidth() - 2 * rx}
              a${rx},${ry} 0 0 1 ${rx},${ry}
              v${height - margin.top - y(d.value) - ry}
              h${-x1.bandwidth()}Z`;
          } else {
            return `
              M${x1(d.rate)},${
              (Math.min(height - margin.top - y(d.value) - ry), y(0))
            }
              a${rx},${
              ry - (height - margin.top - y(d.value) + rx)
            } 0 0 1 ${rx},${ry - (height - margin.top - y(d.value) + rx)}

              v${height - margin.top - y(d.value) - rx}
              h${x1.bandwidth() - 2 * rx}
              v${-(height - margin.top - y(d.value)) + rx}
              a${rx},${
              ry - (height - margin.top - y(d.value) + rx)
            } 0 0 1 ${rx},${-ry + (height - margin.top - y(d.value) + rx)}
              h${-x1.bandwidth()}Z`;
          }
        });

      // threshold line
      if (self.thresholdValue) {
        if (self.thresholdValue > maxYValue) {
          self.thresholdValue = maxYValue;
          self.thresholdValue++;
        }
        svgX
          .append('svg:line')
          .attr('x1', 0)
          .attr('x2', width)
          .attr('y1', y(self.thresholdValue))
          .attr('y2', y(self.thresholdValue))
          .style('stroke', '#333333')
          .style('stroke-dasharray', '4,4')
          .attr('class', 'thresholdline');
        svgX
          .append('text')
          .attr('x', width - 40)
          .attr('y', y(self.thresholdValue))
          .attr('dy', '.5em')
          .attr('text-anchor', 'end')
          .text(self.thresholdValue)
          .attr('class', 'thresholdlinetext');
      }

      // Define the div for the tooltip
      const div = d3
        .select(this.elem)
        .select('#chart')
        .append('div')
        .attr('class', 'tooltip')
        .style('display', 'none')
        .style('opacity', 0);

      // bar legend
      const prevLength = -40;
      let legend = svgLegend
        .selectAll('.d3-legend')
        .data(data[0].value)
        .enter()
        .append('g')
        .attr('class', 'd3-legend')
        .attr('transform', (d, i) => {
          const len = (i + 1) * 160 + prevLength;
          return 'translate(' + len + ', 0)';
        });

      // Legend indicator  .attr("x", width/2)
      legend
        .append('rect')
        .attr('width', 12)
        .attr('height', 12)
        .style('fill', (d, i) => color(i));

      //Legend text /.attr("x", width/2 + 20)
      legend
        .append('text')
        .attr('x', 24)
        .attr('y', 2)
        .attr('dy', '.85em')
        .style('text-anchor', 'start')
        .style('font-size', 10)
        .text((d) =>
          d.rate.length > 15 ? d.rate.substring(0, 12) + '...' : d.rate,
        );

      // bar legend text
      svgLegend
        .append('text')
        .attr('x', 0)
        .attr('y', 0)
        .attr('dy', '.85em')
        .style('text-anchor', 'start')
        .style('font-size', 10)
        .text(self.barLegend);
      // self.lineChart = false;
      if (self.lineChart !== false) {
        const lineOpacity = '1';
        const lineOpacityHover = '0.85';
        const otherLinesOpacityHover = '0.1';
        const lineStroke = '2px';
        const lineStrokeHover = '4px';
        const circleOpacity = '1';
        const circleOpacityOnLineHover = '0.25';
        const circleRadius = 3;
        const circleRadiusHover = 4;
        const duration = 250;

        const colorArr = this.color;
        /* Add line into SVG acoording to data */

        const yScale = d3
          .scaleLinear()
          .domain([0, maxYValue])
          .range([height - margin.top, 0]);

        const elem = this.elem;

        const lines = svgX.append('g').attr('class', 'lines');

        const line = d3
          .line()
          .x((d, i) => {
            const xValue =
              this.isXaxisGroup === true && selectedProjectCount === 1
                ? d.date || d.sortSprint
                : i + 1;
            return x0(xValue);
          })
          .y((d) => yScale(d.lineValue));

        lines
          .selectAll('.line-group')
          .data(newRawData)
          .enter()
          .append('g')
          .attr('class', (d, i) => 'line-group' + i)
          .on('mouseover', (d, i) => {
            svgX
              .append('text')
              .attr('class', 'title-text')
              .style('fill', colorArr[i])
              .text(d.data)
              .attr('text-anchor', 'middle')
              .attr('x', (width - margin.left) / 2)
              .attr('y', 15);
          })
          .on('mouseout', (d) => {
            svgX.select('.title-text').remove();
          })
          .append('path')
          .attr('class', (d, i) => {
            const className = 'line' + i;
            return className;
          })
          .attr('d', (d, i) => line(d.value))
          .style('stroke', (d, i) => colorArr[i])
          .style('opacity', lineOpacity)
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
          .data(newRawData)
          .enter()
          .append('g')
          .attr('class', (d, i) => 'circlegroup' + i)
          .style('fill', (d, i) => d3.hsl([colorArr[i]]))
          .style('stroke', (d, i) => d3.hsl([colorArr[i]]).brighter())
          .selectAll('circle')
          .data((d, index) => d.value)
          .enter()
          .append('g')
          .attr('class', 'circle')
          .on('mouseover', (event, d) => {
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
                  `${d.date || d.sSprintName} ` +
                    ' : ' +
                    "<span class='toolTipValue'> " +
                    `${d.lineValue + ' ' + showUnit} ` +
                    '</span>',
                )
                .style('left', xPosition - 50 + 'px')
                .style('top', yPosition + 20 + 'px');
              for (const hoverData in d.hoverValue) {
                div
                  .append('p')
                  .html(
                    `${hoverData} ` +
                      ' : ' +
                      "<span class='toolTipValue'> " +
                      `${d.hoverValue[hoverData]} ` +
                      ' </span>',
                  );
              }
            }
          })
          .on('mouseout', (d) => {
            div
              .transition()
              .duration(500)
              .style('display', 'none')
              .style('opacity', 0);
          })
          .append('circle')
          .attr('cx', (d, i) => {
            const xValue =
              this.isXaxisGroup === true && selectedProjectCount === 1
                ? d.date || d.sortSprint
                : i + 1;
            return x0(xValue);
          })
          .attr('cy', (d) => yScale(d.lineValue))
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
            d3.select(this)
              .transition()
              .duration(duration)
              .attr('r', circleRadius);
          });

        /** Adding tooltip text  */
        if (selectedProjectCount === 1) {
          tooltipContainer
            .selectAll('div')
            .data(newRawData[0]['value'])
            .join('div')
            .attr('class', (d) => {
              let cssClass = 'tooltip2';
              let value = d.lineValue;
              if (
                this.thresholdValue &&
                this.thresholdValue !== 0 &&
                value < this.thresholdValue
              ) {
                cssClass +=
                  this.lowerThresholdBG === 'red' ? ' red-bg' : ' white-bg';
              } else {
                cssClass +=
                  this.upperThresholdBG === 'red' && this.thresholdValue
                    ? ' red-bg'
                    : ' white-bg';
              }
              return cssClass;
            })
            .style('left', (d, i) => {
              let left = d.date || d.sortSprint;
              if (this.isXaxisGroup === true) {
                return x0(left) + x0.bandwidth() / 2 + 'px';
              } else {
                return x0(i + 1) + x0.bandwidth() / 2 + 'px';
              }
            })
            .style('top', (d) => {
              return yScale(d.lineValue) - 25 + 'px';
            })
            .text(
              (d) =>
                d.lineValue +
                ` ${showUnit ? unitAbbs[showUnit?.toLowerCase()] : ''} `,
            )
            .transition()
            .duration(500)
            .style('display', 'block')
            .style('opacity', 1);
        } else {
          d3.select(this.elem).select('#horizontalSVG').select('div').remove();
          d3.select(this.elem)
            .select('#horizontalSVG')
            .select('tooltip-container')
            .remove();
        }

        newRawData.forEach((element, index) => {
          d3.select(this.elem)
            .selectAll('.circlegroup' + index)
            .selectAll('circle')
            .each(function (dataObj, idx) {
              const tick = d3.select(this);
              tick.attr(
                'transform',
                () => 'translate(' + (x1(element.data) + barWidth / 2) + ',0)',
              );
            });

          d3.select(this.elem)
            .selectAll('.line-group' + index)
            .each(function (dataObj, idx) {
              const tick = d3.select(this);
              tick.attr(
                'transform',
                () => 'translate(' + (x1(element.data) + barWidth / 2) + ',0)',
              );
            });
        });

        // line legend
        legend = svgLegend
          .selectAll('.d3-lineLegend')
          .data(data[0].value)
          .enter()
          .append('g')
          .attr('class', 'd3-lineLegend')
          .attr('transform', (d, i) => {
            const len = (i + 1) * 160 + prevLength;
            return 'translate(' + len + ', 12)';
          });

        const legendLine = legend
          .append('svg:line')
          .attr('x1', 0)
          .attr('x2', 15)
          .attr('y1', 8)
          .attr('y2', 8)
          .style('stroke', (d, i) => color(i))
          .attr('stroke-width', '2')
          .attr('class', 'legendLine');

        legend
          .append('circle')
          .style('stroke', 'gray')
          .style('fill', (d, i) => color(i))
          .attr('r', 4)
          .attr('cx', 7)
          .attr('cy', 8);

        //Legend text /.attr("x", width/2 + 20)
        legend
          .append('text')
          .attr('x', 24)
          .attr('y', 2)
          .attr('dy', '.85em')
          .style('text-anchor', 'start')
          .style('font-size', 10)
          .text((d) =>
            d.rate.length > 15 ? d.rate.substring(0, 12) + '...' : d.rate,
          );

        svgLegend
          .append('text')
          .attr('x', 0)
          .attr('y', 15)
          .attr('dy', '.85em')
          .style('text-anchor', 'start')
          .style('font-size', 10)
          .text(self.lineLegend);

        const content = this.elem.querySelector('#horizontalSVG');
        content.scrollLeft += width;

        const legendIndicator = d3
          .select(this.elem)
          .select('#legendIndicator')
          .append('svg')
          .attr('height', 30)
          .attr('width', 100)
          .attr('cursor', 'pointer')
          .append('g')
          .attr('class', 'd3-legend');

        legendIndicator
          .append('rect')
          .attr('width', 12)
          .attr('height', 12)
          .attr('x', 32)
          .attr('y', 7)
          .style('fill', (d, i) => '#DF9292');

        legendIndicator
          .append('text')
          .attr('x', 52)
          .attr('y', 8)
          .attr('dy', '.85em')
          .style('text-anchor', 'start')
          .style('font-size', 10)
          .text((d) => 'Legend');

        legendIndicator
          .on('mouseover', () => {
            const topValue = 30;

            div
              .transition()
              .duration(200)
              .style('display', 'block')
              .style('opacity', 1)
              .style('padding', '20px 10px')
              .style('max-width', 'unset')
              .style('width', '400px');

            const htmlString = self.elem.querySelector('#svgLegend').innerHTML;

            div
              .html(htmlString)
              .style('left', 70 + 'px')
              .style('top', y[0] - topValue + 'px');
          })
          .on('mouseout', () => {
            div
              .transition()
              .duration(500)
              .style('display', 'none')
              .style('opacity', 0)
              .style('padding', '5px')
              .style('max-width', '220px')
              .style('width', 'auto');
          });
      }
    } catch (ex) {
      console.log(ex);
    }

    if (
      kpiId !== 'kpi166' &&
      kpiId !== 'kpi156' &&
      kpiId !== 'kpi116' &&
      kpiId !== 'kpi118' &&
      kpiId !== 'kpi13' &&
      kpiId !== 'kpi170' &&
      kpiId !== 'KPI127' &&
      kpiId !== 'kpi997' &&
      kpiId !== 'kpi184'
    ) {
      this.renderSprintsLegend(this.flattenData(this.data), this.xCaption);
    }
  }

  wrap(text, width) {
    text.each(function () {
      var text = d3.select(this),
        words = text.text().split(/\s+/).reverse(),
        word,
        line = [],
        lineNumber = 0,
        lineHeight = 1.1, // ems
        y = text.attr('y'),
        dy = parseFloat(text.attr('dy')),
        tspan = text
          .text(null)
          .append('tspan')
          .attr('x', 0)
          .attr('y', y)
          .attr('dy', dy + 'em');
      while ((word = words.pop())) {
        line.push(word);
        tspan.text(line.join(' '));
        if (tspan.node().getComputedTextLength() > width - 5) {
          line.pop();
          tspan.text(line.join(' '));
          line = [word];
          tspan = text
            .append('tspan')
            .attr('x', 0)
            .attr('y', y)
            .attr('dy', `${++lineNumber * lineHeight + dy} em`)
            .text(word);
        }
      }
    });
  }

  flattenData(data) {
    let sprintMap = new Map();
    let sprintCounter = 1;

    data.forEach(project => {
      const projectName = project.data.trim();

      project.value.forEach((sprint, index) => {
        const sprintKey = index; // You could use sprint.sSprintID if needed for uniqueness
        const sprintName = sprint.sSprintName?.trim() || sprint.date?.trim();

        if (!sprintMap.has(sprintKey)) {
          sprintMap.set(sprintKey, {
            sprintNumber: sprintCounter++,
            projects: {},
            sprints: [],
          });
        }

        const sprintEntry = sprintMap.get(sprintKey);
        const sprintData = sprintEntry.projects;

        // Add the sprint name (only if not already there)
        if (sprintName && !sprintEntry.sprints.includes(sprintName)) {
          sprintEntry.sprints.push(sprintName);
        }

        // Assign hoverValue data (handle if empty object)
        sprintData[projectName] = Object.keys(sprint.hoverValue || {}).reduce((acc, key) => {
          acc[key] = sprint.hoverValue[key] || 0;
          return acc;
        }, {});
      });
    });

    return Array.from(sprintMap.values());
  }

  renderSprintsLegend(data, xAxisCaption) {
          this.counter++;
          if (this.counter === 1) {
            const legendData = data.map(item => {
              return {
                sprintNumber: item.sprintNumber,
                sprintLabel: item.sprints.join(", ")
              };
            });

            // Select the body and insert the legend container at the top
            const body = d3.select(this.elem);

            const container = body.insert("div") // Insert at top of body
              .attr("class", "sprint-legend-container")
              .style("margin", "20px 0 0 0")
              .style("font-family", "Arial, sans-serif")
              .style("font-size", "14px")
              .style("max-width", "100%");

            // Toggle Button
            const toggleButton = container.append("button")
              .text("Show X-Axis Legend")
              .style("margin", "0 0 10px 0")
              .style("padding", "6px 12px")
              .style("cursor", "pointer")
              .style("font-size", "14px")
              .attr("class", "p-element p-ripple p-button-success p-ml-2 p-button p-component")
              .on("click", function () {
                const isVisible = legend.style("display") !== "none";
                legend.style("display", isVisible ? "none" : "block");
                legend.style("aria-hidden", isVisible ? "true" : "false");
                legend.style("tabindex", isVisible ? "-1" : "0");
                toggleButton.text(isVisible ? "Show X-Axis Legend" : "Hide X-Axis Legend");
              });

            // Legend Box
            const legend = container.append("div")
              .attr("class", "sprint-legend")
              .attr("aria-hidden", "true")
              .style("display", "none") // Initially hidden
              .style("background", "#f9f9f9")
              .style("padding", "15px")
              .style("border", "1px solid #ddd")
              .style("border-radius", "6px")
              .style("margin-top", "10px");

            legend.append("h3")
              .text("X-Axis Legend")
              .style("margin", "0 0 10px 0")
              .style("color", "#222");

            const list = legend.selectAll("div.sprint-item")
              .data(legendData)
              .enter()
              .append("div")
              .attr("class", "sprint-item")
              .style("margin-bottom", "8px");

            list.append("strong")
              .text(d => `${xAxisCaption} ${d.sprintNumber}: `)
              .style("color", "#333");

            list.append("span")
              .text(d => d.sprintLabel)
              .style("color", "#666");
          }
        }

  ngAfterViewInit() {
    this.resizeObserver.observe(d3.select(this.elem).select('#chart').node());
  }
}
