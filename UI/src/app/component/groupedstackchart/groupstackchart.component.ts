/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 *
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
} from '@angular/core';
import * as d3 from 'd3';
import { SharedService } from 'src/app/services/shared.service';

@Component({
  selector: 'app-groupstackchart',
  templateUrl: './groupstackchart.component.html',
  styleUrls: ['./groupstackchart.component.css'],
})
export class GroupstackchartComponent implements OnChanges {
  elem;
  @Input() data: any;
  transformedData: any;
  @Input() width: any;
  @Input() yCaption: string; // label at y axis
  @Input() thresholdValue: any;
  @Input() xCaption: string;
  newXCaption: string;
  @Input() unit?: string;
  @Input() color?: string;
  @Input() kpiId?: string;
  @Input() maxValue?: any;
  @Input() selectedtype: string;
  @Input() legendType: string;
  @Input() filter?: any;
  maxYValue: any;
  dataPoints = 2;
  dataLength = 0;
  @Input() activeTab?: number = 0;
  @Input() isAggregationStacks; // to determine wheather need to aggrigate stacks
  isDrilledDown = false;
  shouldClickable = true;
  elemObserver = new ResizeObserver(() => {
    this.draw(this.transformedData);
  });
  constructor(
    private viewContainerRef: ViewContainerRef,
    private service: SharedService,
  ) {}

  ngOnChanges(changes: SimpleChanges) {
    if (this.selectedtype?.toLowerCase() === 'kanban') {
      this.xCaption = this.service.getSelectedDateFilter();
    }
    // only run when property "data" changed
    if (Object.keys(changes)?.length > 0) {
      if (changes['data']) {
        this.isDrilledDown = false;
        d3.select(this.elem).select('#back_icon').attr('class', 'p-d-none');
        this.checkIfDrillDownNeedOrNot(this.data);
        this.transformedData = JSON.parse(
          JSON.stringify(this.transformData(this.data)),
        );
        this.dataPoints = this.transformedData.length;
        this.dataLength = this.dataPoints;
        this.elem = this.viewContainerRef.element.nativeElement;
        // this.draw();
        if (!changes['data'].firstChange) {
          this.draw(this.transformedData);
        } else {
          this.draw(this.transformedData);
        }
      }

      if (changes['filter']) {
        this.isDrilledDown = false;
        d3.select(this.elem).select('#back_icon').attr('class', 'p-d-none');
        this.checkIfDrillDownNeedOrNot(this.data);
        this.transformedData = JSON.parse(
          JSON.stringify(this.transformData(this.data)),
        );
        this.dataPoints = this.transformedData.length;
        this.dataLength = this.dataPoints;
        this.elem = this.viewContainerRef.element.nativeElement;

        this.draw(this.transformedData);
      }
    }
    if (changes['activeTab']) {
      setTimeout(() => {
        this.draw(this.transformedData);
      }, 0);
    }
  }

  ngAfterViewInit(): void {
    this.elemObserver.observe(this.elem);
  }

  draw(data) {
    if (data.length > 0) {
      const elem = this.elem;
      const self = this;
      d3.select(elem).select('#graphContainer').select('svg').remove();
      d3.select(elem).select('.tooltip').remove();
      d3.select(elem).select('.legend').remove();

      d3.select(elem).select('#verticalSVG').select('svg').remove();
      d3.select(elem).select('#horizontalSVG').select('svg').remove();
      d3.select(elem).select('#svgLegend').select('div').remove();
      // d3.select(elem).select('#legendIndicator').select('svg').remove();
      d3.select(elem).select('#xCaptionContainer').select('text').remove();
      if (!this.isDrilledDown) {
        data = this.formatData(data);
      }
      const width =
        this.dataPoints <= 5 &&
        d3.select(this.elem).select('#groupstackchart').node()?.offsetWidth
          ? d3.select(this.elem).select('#groupstackchart').node()
              ?.offsetWidth - 70
          : this.dataPoints * 20 * 4;
      // let spacingVariable = width > 1500 ? 145 : width > 1000 ? 120 : width > 600 ? 70 : 50;
      // const spacingVariable = 20;
      const height = 225;
      const margin = 50;
      const marginLeft = 40;
      const marginTop = 35;
      // const marginBottom = 60;
      // const marginRight = 100;

      const svgX = d3
        .select(elem)
        .select('#horizontalSVG')
        .append('svg')
        .attr('width', width)
        .attr('height', height + 13 + 'px')
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
        .attr('transform', `translate(${marginLeft}, ${marginTop})`);

      /** Add x-axis */
      const x0 = d3.scaleBand().rangeRound([0, width - margin]);
      // if (this.dataLength < 5) {
      //   x0.paddingInner(.5);
      // } else if (this.dataPoints == 2) {
      //   x0.paddingInner(0.4);
      // } else if (this.dataPoints == 3) {
      x0.paddingInner(0.5);
      // }

      const x1 = d3.scaleBand();

      if (!(this.maxYValue >= 5)) {
        this.maxYValue = 5;
      } else {
        this.maxYValue = Math.ceil(this.maxYValue / 5) * 5;
      }

      const y = d3
        .scaleLinear()
        .range([height - margin, 0])
        .domain([0, this.maxYValue]);

      const y1 = d3.scaleBand();
      let z;
      let stackColorsList;
      // if (this.kpiId != 'kpi125' && this.kpiId != 'kpi127') {
      stackColorsList = [
        '#DEB0D2',
        '#F2C69B',
        '#B395E2',
        '#B5E7BE',
        '#DF9292',
        '#B5C6E7',
        '#ff8c00',
        '#3F51B5',
        '#aaaaaa',
        '#E7FFAC',
        '#85E3FF',
        '#8DA47E',
      ];
      z = d3.scaleOrdinal().range(stackColorsList);
      // } else {
      // z = d3.scaleOrdinal()
      //   .range(this.color);
      // stackColorsList = this.color;
      // }
      // const z2 = d3.scaleOrdinal().range(this.color);
      const stack = d3.stack();

      x0.domain(
        data.map(function (d) {
          return d.group;
        }),
      );
      x1.domain(
        data.map(function (d, i) {
          return d.xName;
        }),
      ).rangeRound([0, x0.bandwidth()]);

      // .padding(0.2);// bar width
      const actualTypes = [];
      data.forEach(function (d) {
        if (d.type && !actualTypes.includes(d.type) && d.type !== 'drillDown') {
          actualTypes.push(d.type);
        }
      });
      actualTypes.reverse();
      z.domain(actualTypes);
      const keys = z.domain();
      let groupData = d3.rollup(
        data,
        (d, i) => {
          const d2 = { xName: d[0].xName, group: d[0].group };
          d2['hoverSum'] = 0;
          d2['hoverText'] = {};
          d.forEach((dx) => {
            d2[dx.type] = dx.value;
            for (let key in dx?.hoverText) {
              if (key.indexOf('drillDown') === -1) {
                if (!this.isAggregationStacks) {
                  d2['hoverSum'] = dx?.value + ' ' + this.unit;
                } else {
                  d2['hoverSum'] += dx?.hoverText[key];
                }
                d2['hoverText'][key] = dx?.hoverText[key];
              }
            }
          });
          return d2;
        },
        function (d) {
          return d.group;
        },
      );

      groupData = Array.from(groupData).map(function (d) {
        return d[1];
      });

      const stackData = stack.keys(keys)(groupData);

      svgX
        .append('g')
        .attr('class', 'xAxis')
        .attr('transform', `translate(0, ${y(0)})`)
        .call(d3.axisBottom(x0));

      d3.select('.xAxis')
        .selectAll('.tick text')
        .style('width', '70px')
        .call(this.wrap, 75); // select all the text elements

      const XCaption = d3
        .select(this.elem)
        .select('#xCaptionContainer')
        .append('text')
        .attr(
          'x',
          (document.getElementById('groupstackchart')?.offsetWidth - 70) / 2 -
            24,
        )
        .attr('y', 44)
        .attr('transform', 'rotate(0)')
        .text(this.isDrilledDown ? this.newXCaption : this.xCaption);
      const xTick =
        self.dataPoints === 1
          ? width > 1600
            ? 20
            : width > 1500
            ? -20
            : width > 1000
            ? 20
            : 10
          : 0;

      svgY
        .append('g')
        .attr('class', 'yAxis')
        .call(
          d3
            .axisLeft(y)
            .ticks(5)
            .tickSize(-width + margin),
        )
        .append('text')
        .attr('x', -80)
        .attr('y', -30)
        .attr('transform', 'rotate(-90)')
        .attr('fill', '#437495')
        .attr('font-size', '12px')
        .text(this.yCaption);

      // gridlines
      svgX
        .selectAll('line.gridline')
        .data(y.ticks(5))
        .enter()
        .append('svg:line')
        .attr('x1', 0)
        .attr('x2', width)
        .attr('y1', function (d) {
          return y(d);
        })
        .attr('y2', function (d) {
          return y(d);
        })
        .style('stroke', '#dedede')
        .style('fill', 'none')
        .attr('class', 'gridline');

      const serie = svgX
        .selectAll('.serie')
        .data(stackData)
        .enter()
        .append('g')
        .attr('class', 'serie')
        .attr('fill', function (d) {
          return z(d.key);
        })
        .attr('data-name', function (d) {
          return d.key;
        });

      // Define the div for the tooltip
      const div = d3
        .select(this.elem)
        .select('#groupstackchart')
        .append('div')
        .attr('class', 'tooltip')
        .style('display', 'none')
        .style('opacity', 0);

      serie
        .selectAll('rect')
        .data(function (d) {
          return d;
        })
        .enter()
        .append('rect')
        .attr('class', 'serie-rect1')
        .attr('x', function (d, i) {
          return x0(d.data.group); //self.dataPoints === 1 ? spacingVariable : x1(d.data.group);
        })
        .attr('y', function (d) {
          return y(d[1]);
        })
        .attr('data-prop', function (d) {
          return JSON.stringify(d.data);
        })
        .style('cursor', 'pointer')
        .attr('height', function (d) {
          return !self.isDrilledDown && !isNaN(y(d[0]) - y(d[1]))
            ? y(d[0]) - y(d[1])
            : 0;
        })
        .attr('width', x0.bandwidth())
        .on('mouseover', function (event, d) {
          const topValue = 75;
          if (d.data?.hoverText) {
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
            if (!self.isDrilledDown) {
              for (let key in d.data?.hoverText) {
                if (key.indexOf('drillDown') === -1) {
                  dataString += `<div class=\'toolTipValue p-d-flex p-align-center\'><div class="stack-key p-mr-1">${key}</div><div>${d.data?.hoverText[key]}</div></div>`;
                }
              }
            } else {
              dataString = `<div class=\'toolTipValue p-d-flex p-align-center\'><div class="stack-key p-mr-1">${
                d.data.group
              }</div><div>${d.data[d.data.group]}</div></div>`;
            }
            if (!self.isDrilledDown) {
              htmlString =
                `${d?.data?.group}` +
                ' : ' +
                `${d?.data?.hoverSum}` +
                "<div class='toolTip'> " +
                `${dataString}` +
                '</div>';
            } else {
              htmlString = `<div class=\'toolTip\'>${dataString}</div>`;
            }
            div
              .html(htmlString)
              .style('left', xPosition + 20 + 'px')
              .style('top', yPosition + 'px')
              .style('position', 'fixed')
              .style('align', 'left');
          }
        })
        .on('mouseout', function (d) {
          div
            .transition()
            .duration(500)
            .style('display', 'none')
            .style('opacity', 0);
        });

      svgX
        .selectAll('rect.serie-rect1')
        .on('click', function (event, d) {
          if (self.shouldClickable) {
            self.isDrilledDown = true;
            d3.select(self.elem)
              .select('#back_icon')
              .attr('class', 'p-d-block');
            const dataName =
              event.target.parentElement.getAttribute('data-name');
            const newData = JSON.parse(event.target.getAttribute('data-prop'))[
              'drillDown_' + dataName
            ];

            if (newData) {
              event.target.setAttribute('fill', '#000');
              self.transformedData = self.formatData(newData);
              self.dataPoints = self.transformedData?.length
                ? self.transformedData.length
                : 0;
              self.dataLength = self.dataPoints;
              self.newXCaption = d.data.group + ' - ' + dataName;
              self.draw(self.transformedData);

              d3.select(elem)
                .select('#back_icon')
                .attr('class', 'p-d-flex')
                .on('click', (event, d) => {
                  self.isDrilledDown = false;
                  self.transformedData = JSON.parse(
                    JSON.stringify(self.transformData(self.data)),
                  );
                  self.dataPoints = self.transformedData.length;
                  self.dataLength = self.dataPoints;
                  self.draw(self.transformedData);
                  d3.select(elem)
                    .select('#back_icon')
                    .attr('class', 'p-d-none');
                });
            }
          }
        })
        .style('cursor', self.isDrilledDown ? 'default' : 'pointer')
        .transition()
        .ease(d3.easeLinear)
        .duration(200)
        .delay(function (d, i) {
          return 200;
        })
        .attr('height', (d) => {
          return !isNaN(y(d[0]) - y(d[1])) ? y(d[0]) - y(d[1]) : 0;
        });

      /** legend code */
      const legendDiv = d3.select(this.elem).select('#svgLegend').append('div');

      if (this.legendType == 'normal') {
        legendDiv
          .transition()
          .duration(200)
          .style('display', 'block')
          .style('opacity', 1)
          .attr('id', 'd3-legend')
          .attr('class', 'p-d-flex p-flex-wrap normal-legend');

        let htmlString = '';
        this.sortAlphabetically(stackData);
        const legendKeys = actualTypes
          .filter((type) => type.indexOf('drillDown') === -1)
          .reverse();

        legendKeys.forEach((key, i) => {
          if (z(key)) {
            htmlString += `<div class="legend_item p-d-flex p-align-center"><div class="legend_color_indicator" style="background-color: ${z(
              key,
            )}"></div> ${key}</div>`;
          }
        });

        legendDiv.html(htmlString);
      }

      const content = this.elem.querySelector('#horizontalSVG');
      content.scrollLeft += width;
    }
  }

  transformData(data) {
    let result = JSON.parse(JSON.stringify(data));
    result.forEach((element) => {
      let obj = {};
      element?.value?.forEach((val) => {
        obj['drillDown' + '_' + val['subFilter']] = [];
        obj[val['subFilter']] =
          this.filter['filter1'][0] === 'Story Points'
            ? val['size']
            : val['value'];
        obj['drillDown' + '_' + val['subFilter']].push(
          ...(val['drillDown'] ? val['drillDown'] : []),
        );
      });
      element.value = obj;
    });
    return result;
  }

  formatData(dataObj) {
    if (dataObj?.length > 0) {
      // dataObj = this.padData(dataObj);
      let max = 0;
      const targetList = [];
      dataObj?.forEach((item, index) => {
        const sprintValue = index + 1;
        if (
          typeof item?.value === 'object' &&
          Object.keys(item?.value)?.length > 0
        ) {
          const types = Object.keys(item.value);
          types?.forEach((type) => {
            const obj = {};

            obj['group'] = item?.sSprintName;
            obj['type'] = type;
            obj['value'] =
              this.isAggregationStacks == false
                ? item?.data
                : item?.value[type];
            obj['hoverText'] = {};
            obj['hoverText'][type] = item?.value[type];
            obj['xName'] = sprintValue;
            max = Math.max(max, item?.data);
            if (type === 'drillDown') {
              obj['drillDown'] = item.value[type]?.length
                ? item.value[type]
                : [];
            }
            targetList.push(obj);
          });
        } else {
          const obj = {};
          obj['group'] = item?.sSprintName
            ? item?.sSprintName
            : item?.subFilter;
          obj['hoverText'] = {};
          obj['xName'] = sprintValue;
          obj['type'] = item?.subFilter;
          obj['value'] =
            this.filter && this.filter['filter1']
              ? this.filter['filter1'][0] === 'Story Points'
                ? item['size']
                : item['value']
              : item['value'];
          obj['drillDown'] = item.value?.drillDown?.length
            ? item.value['drillDown']
            : [];
          max = Math.max(max, obj['value']);
          targetList.push(obj);
        }
      });
      this.maxYValue = max * 1.07;
      return targetList;
    }
  }

  sortAlphabetically(objArray) {
    if (objArray && objArray?.length > 1) {
      objArray?.sort((a, b) => a.key?.localeCompare(b.data));
    }
    return objArray;
  }

  wrap(text, wrapWidth, yAxisAdjustment = 0) {
    text.each(function () {
      let text = d3.select(this);
      if (text.text().length > 25) {
        text.text(text.text().substring(0, 25) + '...');
      }
      let words = text
          .text()
          .split(/[\s|_-]+/)
          .reverse(),
        word,
        line = [],
        lineNumber = 0,
        lineHeight = 1,
        y = text.attr('y'),
        dy = parseFloat(text.attr('dy')) - yAxisAdjustment,
        tspan = text
          .text(null)
          .append('tspan')
          .attr('x', 15)
          .attr('y', y - 2)
          .attr('dy', `${dy}em`)
          .attr('text-anchor', 'middle');

      while ((word = words.pop())) {
        line.push(word);
        tspan.text(line.join(' '));

        if (tspan.node().getComputedTextLength() > wrapWidth) {
          line.pop();
          tspan.text(line.join(' '));
          line = [word];
          tspan = text
            .append('tspan')
            .attr('x', 15)
            .attr('y', y)
            .attr('dy', ++lineNumber * lineHeight + dy + 'em')
            .text(word)
            .attr('text-anchor', 'middle');
        }
      }
    });
    return 0;
  }

  checkIfDrillDownNeedOrNot(data) {
    var counter = 0;
    data.forEach((val) => {
      val?.value?.forEach((sprint) => {
        if (sprint?.drillDown && sprint?.drillDown?.length) {
          counter++;
        }
      });
    });

    if (counter === 0) {
      this.shouldClickable = false;
    } else {
      this.shouldClickable = true;
    }
  }

  ngOnDestroy() {
    d3.select(this.elem).select('#groupstackchart').select('svg').remove();
    this.data = [];
    this.elemObserver.unobserve(this.elem);
  }
}
