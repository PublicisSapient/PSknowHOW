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
  @Input() color?: string;
  @Input() selectedtype: string;
  elem;
  sliderLimit = <any>'750';
  constructor(
    private viewContainerRef: ViewContainerRef,
    private service: SharedService,
  ) {
    // used to make chart independent from previous made chart
    this.elem = this.viewContainerRef.element.nativeElement;
  }

  // Runs when property "data" changed
  ngOnChanges(changes: SimpleChanges) {
    if (this.selectedtype?.toLowerCase() === 'kanban') {
      this.xCaption = this.service.getSelectedDateFilter();
    }
    if (Object.keys(changes)?.length > 0) {
      if (changes['data']) {
        if (!changes['data'].firstChange) {
          this.draw('update');
        } else {
          this.draw('new');
        }
      }
    } else {
      d3.select(this.elem).select('svg').remove();
      d3.select(this.elem).select('.bstimeslider').remove();
      this.draw('new');
    }
  }

  draw(status) {
    // this is used for removing svg already made when value is updated
    d3.select(this.elem).select('#verticalSVG').select('svg').remove();
    d3.select(this.elem).select('#horizontalSVG').select('svg').remove();
    d3.select(this.elem).select('#xCaptionContainer').select('text').remove();

    const data = this.data;
    const thresholdValue = this.thresholdValue;
    const elem = this.elem;
    let width = 450;
    const height = 190;
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
    const id = this.kpiId;
    const showPercent = false;
    const showWeek = false;
    const showUnit = this.unit;

    // width = $('#multiLineChart').width();
    width =
      data[0].value.length <= 5
        ? document.getElementById('multiLineChart').offsetWidth - 70
        : data[0].value.length * 20 * 8;
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

    const xScale = d3
      .scaleBand()
      .rangeRound([0, width - margin])
      .padding(0)
      .domain(
        data[maxObjectNo].value.map(function (d, i) {
          return i + 1;
        }),
      );

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

    if (this.kpiId === 'kpi149') {
      maxYValue = 5;
    }

    const yScale = d3
      .scaleLinear()
      .domain([0, maxYValue])
      .range([height - margin, 0]);
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
    const yAxis = d3.axisLeft(yScale).ticks(5);

    const XCaptionSVG = d3
      .select(this.elem)
      .select('#xCaptionContainer')
      .append('text');

    svgX
      .append('g')
      .attr('class', 'x axis')
      .attr('transform', `translate(0, ${height - margin})`)
      .call(xAxis);

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

    if (id === 'kpi114' || id === 'kpi74' || id === 'kpi997') {
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

    /************ horizontal legend addition started **********/

    // The names which you want to appear as legends should be inside this array.
    // const legendData = [];
    // let legendWidth = 0;
    // for (const obj in data) {

    //     legendWidth = legendWidth + data[obj].data.length * 10 + 40;
    //     legendData.push(data[obj].data);
    // }

    // const scrollWidth = d3.select(elem).select('#multiLineChart').node().getBoundingClientRect().width;

    // if (this.isChildComponent) {
    //     $(elem).find('.bstimeslider').remove();
    // }

    // d3.select(elem).select('#multiLineChart').append('div')
    //     .attr('class', 'bstimeslider')
    //     .style('width', scrollWidth - 15 + 'px');

    // const slider = d3.select(elem).select('.bstimeslider');

    // const sliderData = slider.append('div').attr('id', 'viewContainer')
    //     .style('margin-left', '-' + (((scrollWidth - 85) / 2) - 15) + 'px')
    //     .style('width', scrollWidth - 100 + 'px')
    //     .append('div')
    //     .attr('id', 'tslshow')
    //     .style('width', '1000px');

    // const sliderText = sliderData
    //     .selectAll('div')
    //     .data(legendData)
    //     .enter()
    //     .append('div')
    //     .attr('class', 'bktibx')
    //     .style('width', (d, i) => d.length);

    // // if wanna use circle instead of line in legend
    // sliderText.append('span')
    //     .attr('cx', 13)
    //     .attr('class', function (d, i) {
    //         return 'legendCircle' + i;
    //     })
    //     .style('background-color', function (d, i) {
    //         return color[i];
    //     })
    //     .style('border', function (d, i) {
    //         return '1px solid ' + color[i];
    //     })
    //     .style('width', '12px')
    //     .style('height', '12px')
    //     .style('border-radius', '50%')
    //     .style('display', ' inline-block')
    //     .style('margin', '0px 10px');

    // sliderText.append('text')
    //     .attr('class', function (d, i) {
    //         return 'legendText' + i;
    //     })
    //     .attr('x', 30)
    //     .attr('y', 3)
    //     .attr('dy', '.15em')
    //     .text((d, i) => d)
    //     .style('text-anchor', 'start')
    //     .style('font-size', 12)
    //     .style('cursor', 'pointer')
    //     .on('mouseover', function (d, i) {
    //         const className = 'line' + i;
    //         d3.select(elem).selectAll('.line')
    //             .style('opacity', otherLinesOpacityHover);
    //         d3.select(elem).selectAll('.circle')
    //             .style('opacity', circleOpacityOnLineHover);
    //         d3.select(elem).select('.' + className)
    //             .style('opacity', lineOpacityHover)
    //             .style('stroke-width', lineStrokeHover)
    //             .style('cursor', 'pointer');
    //         svg.append('text')
    //             .attr('class', 'title-text')
    //             .style('fill', color[i])
    //             .text(d)
    //             .attr('text-anchor', 'middle')
    //             .attr('x', (width - margin) / 2)
    //             .attr('y', -10);
    //     })
    //     .on('click', function (d, i) {

    //         const dvalue = d3.select(elem).select('.line' + i).style('display');
    //         if (dvalue !== 'none') {
    //             d3.select(elem).select('.legendCircle' + i)
    //                 .style('background-color', 'var(--color-white)');
    //             const displayValue = 'none';
    //             d3.select(elem).select('.line' + i)
    //                 .style('display', displayValue);
    //             d3.select(elem).select('.circlegroup' + i)
    //                 .style('display', displayValue);
    //             d3.select(elem).select('.legendText' + i)
    //                 .style('text-decoration', 'line-through');
    //         } else {
    //             d3.select(elem).select('.legendCircle' + i)
    //                 .style('background-color', color[i]);
    //             const displayValue = 'inline';
    //             d3.select(elem).select('.line' + i)
    //                 .style('display', displayValue);
    //             d3.select(elem).select('.circlegroup' + i)
    //                 .style('display', displayValue);
    //             d3.select(elem).select('.legendText' + i)
    //                 .style('text-decoration', 'none');
    //         }

    //     })
    //     .on('mouseout', function (d, i) {
    //         const className = 'line' + i;
    //         svg.select('.title-text').remove();
    //         d3.select(elem).selectAll('.line').style('opacity', lineOpacity);
    //         d3.select(elem).selectAll('.circle')
    //             .style('opacity', circleOpacity);
    //         d3.select(elem).selectAll('.' + className + '')
    //             .style('stroke-width', lineStroke);
    //     });

    // calculating width of

    // const element = d3.select(elem).selectAll('.bktibx');
    // let legendSliderWidth = 0;

    // for (let i = 0; i < element._groups[0].length; i++) {
    //     legendSliderWidth += element._groups[0][i].clientWidth;
    //     legendSliderWidth += 1;

    // }

    // legendWidth = legendSliderWidth;

    // this.sliderLimit = legendWidth;
    // const move = '100px';

    // d3.select(this.elem).select('#tslshow')
    //     .style('width', legendWidth + 'px');

    // if (legendWidth >= scrollWidth - 85) {

    //     d3.select(elem).select('#viewContainer').style('background-color', '#f4f4f4');
    //     slider.append('div')
    //         .attr('id', 'rightArrow')
    //         .on('click', function () {

    //             const view = $(this).siblings('#viewContainer').find('#tslshow');
    //             const currentPosition = parseInt(view.css('left'), 10);
    //             // scrollWidth-100
    //             if (currentPosition + legendWidth >= scrollWidth - 100) {
    //                 view.stop(false, true).animate({ left: '-=' + move }, { duration: 400 });
    //             }
    //         })
    //         .append('span')
    //         .text('>');

    //     slider.append('div')
    //         .attr('id', 'leftArrow')
    //         .on('click', function () {

    //             const view = $(this).siblings('#viewContainer').find('#tslshow');
    //             const currentPosition = parseInt(view.css('left'), 10);
    //             if (currentPosition < -99) {
    //                 view.stop(false, true).animate({ left: '+=' + move }, { duration: 400 });
    //             }

    //         })
    //         .append('span')
    //         .text('<');
    // }

    /********************Horizontal scrollbar end ***********************/

    /* Add line into SVG acoording to data */
    const line = d3
      .line()
      .x((d, i) => xScale(i + 1))
      .y((d) => yScale(d.value));

    const lines = svgX.append('g').attr('class', 'lines');

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
        // if ((d.data).includes('LogTime') && name === 'Sprint Capacity') {
        //     return '4, 4';
        // }
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
        return xScale(i + 1);
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
      .each(function (dataObj) {
        const tick = d3.select(this);
        if (data[0]?.value[0] && data[0]?.value[0]?.xAxisTick) {
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
        if (dataObj === 1) {
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

    const content = this.elem.querySelector('#horizontalSVG');
    content.scrollLeft += width;
  }

  ngOnDestroy() {
    // this is used for removing svg already made when value is updated
    d3.select(this.elem).select('#verticalSVG').select('svg').remove();
    d3.select(this.elem).select('#horizontalSVG').select('svg').remove();
    d3.select(this.elem).select('#xCaptionContainer').select('text').remove();
    this.data = [];
  }
}
