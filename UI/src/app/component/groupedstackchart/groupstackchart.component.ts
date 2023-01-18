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

import { Component, Input, ViewContainerRef, OnChanges, SimpleChanges } from '@angular/core';
import * as d3 from 'd3';
import { SharedService } from 'src/app/services/shared.service';


@Component({
  selector: 'app-groupstackchart',
  templateUrl: './groupstackchart.component.html',
  styleUrls: ['./groupstackchart.component.css']
})



export class GroupstackchartComponent implements OnChanges {

  elem;
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
  maxYValue: any;
  dataPoints = 2;
  dataLength = 0;
  constructor(private viewContainerRef: ViewContainerRef, private service: SharedService) { }

  ngOnChanges(changes: SimpleChanges) {
    if (this.selectedtype?.toLowerCase() === 'kanban') {
      this.xCaption = this.service.getSelectedDateFilter();
    }
    // only run when property "data" changed
    if (changes['data']) {
      this.dataPoints = this.data.length;
      this.dataLength = this.data[0]?.value?.length;
      this.elem = this.viewContainerRef.element.nativeElement;
      if (!changes['data'].firstChange) {
        this.draw('update');
      } else {
        this.draw('new');
      }
    }
  }


  draw(status) {
    const elem = this.elem;
    const self = this;
    d3.select(elem).select('svg').remove();
    d3.select(elem).select('.tooltip').remove();
    d3.select(elem).select('.legend').remove();
    d3.select(elem).select('.d3-legend').remove();

    d3.select(elem).select('#verticalSVG').select('svg').remove();
    d3.select(elem).select('#horizontalSVG').select('svg').remove();
    d3.select(elem).select('#svgLegend').select('svg').remove();
    d3.select(elem).select('#legendIndicator').select('svg').remove();
    d3.select(elem).select('#xCaptionContainer').select('text').remove();

    const data = this.formatData();
    const thresholdValue = this.thresholdValue;
    const barWidth = 20;
    // let width = this.dataLength * barWidth * 8;
    const width = this.dataLength <= 5 ? document.getElementById('groupstackchart').offsetWidth - 70 : this.dataLength * barWidth * 4;
    // let spacingVariable = width > 1500 ? 145 : width > 1000 ? 120 : width > 600 ? 70 : 50;
    const spacingVariable = 50;
    const height = 190;
    const margin = 50;
    const marginLeft = 40;
    const marginTop = 35;
    const marginBottom = 60;
    const marginRight = 100;

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

    /** Add x-axis */
    const x0 = d3.scaleBand()
      .rangeRound([0, width - margin]);
    if (this.dataLength < 5) {
      x0.paddingInner(.5);
    } else if (this.dataPoints == 2) {
      x0.paddingInner(0.4);
    } else if (this.dataPoints == 3) {
      x0.paddingInner(0.2);
    }

    const x1 = d3.scaleBand();

    let divisor = 10;
    let power = 1;
    let quotient = this.maxYValue;
    while (quotient > 1) {
      quotient = quotient / Math.pow(divisor, power);
      ++power;
    }
    divisor = Math.pow(10, power > 1 ? power - 1 : 1);



if(!(this.maxYValue >=5)){
  this.maxYValue =5;
}else{
  this.maxYValue = Math.ceil(this.maxYValue/5) *5;

}


    const y = d3.scaleLinear()
      .range([height - margin, 0])
      .domain([0, this.maxYValue]);

    const y1 = d3.scaleBand();
    let z; let stackColorsList;
    if (this.kpiId != 'kpi125' && this.kpiId != 'kpi127') {
      z = d3.scaleOrdinal()
        .range(['#DEB0D2', '#F2C69B', '#B395E2', '#B5E7BE', '#DF9292', '#B5C6E7', '#ff8c00', '#3F51B5', '#aaaaaa']);
      stackColorsList = ['#DEB0D2', '#F2C69B', '#B395E2', '#B5E7BE', '#DF9292', '#B5C6E7', '#ff8c00', '#3F51B5', '#aaaaaa'];
    } else {
      z = d3.scaleOrdinal()
        .range(this.color);
      stackColorsList = this.color;
    }
    const z2 = d3.scaleOrdinal().range(this.color);
    const stack = d3.stack();

    x0.domain(data.map(function(d) {
 return d.xName;
}));
    x1.domain(data.map(function(d, i) {
      return d.sprojectName;
    }))
      .rangeRound([0, x0.bandwidth()]);
    // .padding(0.2);// bar width

    const actualTypes = [];
    data.forEach(function(d) {
 if (d.type && !actualTypes.includes(d.type)) {
 actualTypes.push(d.type);
}
});
    z.domain(actualTypes);
    const keys = z.domain();
    let groupData = d3.rollup(data, function(d, i) {
      const d2 = { sprojectName: d[0].sprojectName, xName: d[0].xName, hoverValue: d[0].hoverValue, sSprintName: d[0].sSprintName };
      d.forEach(function(d) {
        d2[d.type] = d.value;
      });
      return d2;
    }, function(d) {
 return d.xName + d.sprojectName;
});
      // .key(function (d) { return d.xName + d.sprojectName; })
      // .entries(data)
      // .map(function (d) { return d.value; });
      groupData = Array.from(groupData).map(function(d) {
 return d[1];
});

    const stackData = stack
      .keys(keys)(groupData);

    svgX.append('g')
      .attr('class', 'xAxis')
      .attr('transform', `translate(0, ${y(0)})`)
      .call(d3.axisBottom(x0));


    const XCaption = d3
      .select(this.elem).select('#xCaptionContainer').append('text')
      .attr('x', ((document.getElementById('groupstackchart').offsetWidth - 70) / 2) - 24)
      .attr('y', 44)
      .attr('transform', 'rotate(0)')
      .text(this.xCaption);
    const xTick = self.dataPoints === 1 ? width > 1600 ? 20 :width > 1500 ? -20 : width > 1000 ? 20 : 10 : 0;
    svgX
      .select('.xAxis')
      .selectAll('.tick text')
      .attr('x', xTick)
      .attr('y', 15);
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
      .attr('y1', function(d) {
 return y(d);
})
      .attr('y2', function(d) {
 return y(d);
})
      .style('stroke', '#dedede')
      .style('fill', 'none')
      .attr('class', 'gridline');


    const serie = svgX.selectAll('.serie')
      .data(stackData)
      .enter().append('g')
      .attr('class', 'serie')
      .attr('fill', function(d) {
        return z(d.key);
      });

    // Define the div for the tooltip
    const div = d3.select(this.elem).select('#groupstackchart').append('div')
      .attr('class', 'tooltip')
      .style('display', 'none')
      .style('opacity', 0);


    serie.selectAll('rect')
      .data(function(d) {
 return d;
})
      .enter().append('rect')
      .attr('class', 'serie-rect1')
      .attr('x', function(d, i) {
        return self.dataPoints === 1 ? spacingVariable + x1(d.data.sprojectName) : x1(d.data.sprojectName);
      })
      .attr('y', function(d) {
 return y(d[1]);
})
      .attr('transform', function(d) {
 return 'translate(' + x0(d?.data?.xName) + ',0)';
})
      .attr('height', function(d) {
 return Math.abs(y(d[0]) - y(d[1]));
})
      .attr('width', barWidth)
      .on('click', function(d, i) {
})
      .on('mouseover', function(event,d) {
        const topValue = 75;
        if (d.data.hoverValue) {

          const circle = event.target;
          const {
            top: yPosition,
            left: xPosition
          } = circle.getBoundingClientRect();

          div.transition()
            .duration(200)
            .style('display', 'block')
            .style('opacity', .9);

          const dataObj = JSON.parse(JSON.stringify(d.data));
          delete dataObj.sSprintName;
          delete dataObj.sprojectName;
          delete dataObj.hoverValue;
          delete dataObj.xName;

          let dataString = '';
          for (const hoverData in dataObj) {
            dataString += `${hoverData}` + ' : ' + '<span class=\'toolTipValue\'> ' + `${dataObj[hoverData] + ' ' + (self.unit ? self.unit.toLowerCase() != 'number' ? self.unit : '' : '')} </span>`;
          }

          div.html(`${d?.data?.sSprintName}` + ' : ' + '<span class=\'toolTipValue\'> ' + `${dataString}` + '</span>')
            .style('left', xPosition + 20 + 'px')
            // .style('top', y(d[0]) - y(d[1]) - topValue + 'px');
            .style('top', yPosition + 20 + 'px')
            .style('position', 'fixed');

          for (const hoverData in d.data.hoverValue) {
            div.append('p').html(`${hoverData}` + ' : ' + '<span class=\'toolTipValue\'> ' + `${d.data.hoverValue[hoverData]}` + ' </span>');
          }
        }
      })
      .on('mouseout', function(d) {
        div.transition()
          .duration(500)
          .style('display', 'none')
          .style('opacity', 0);

      });
    if (this.kpiId != 'kpi125' && this.kpiId != 'kpi127') {
      const serie2 = svgX.selectAll('.serie2')
        .data(stackData)
        .enter().append('g')
        .attr('class', 'serie2')
        .attr('fill', function(d) {
          return z(d.key);
        });

      const triangle = d3.symbol().type(d3.symbolTriangle);
      const dim = barWidth * 5;
      serie2.selectAll('.path_triangle')
        .data(function(d) {
 return d;
})
        .enter()
        .append('path')
        .attr('d', triangle.size(dim))
        .attr('opacity', 1)
        .attr('class', 'path_triangle')
        .attr('transform', function(d) {
 return 'translate(' + x0(d?.data?.xName) + ',0)';
})
        .attr('transform', function(d, i) {
          const xVal = self.dataPoints === 1 ? spacingVariable + x1(d.data.sprojectName) + x0(d?.data?.xName) : x1(d.data.sprojectName) + x0(d?.data?.xName);
          return 'translate(' + (xVal + 8) + ',' + (y(0) + 10) + ')';
        })
        .attr('fill', function(d) {
          return z2(d?.data?.sprojectName);
        });
    }

    /** legend code */
    const legendDiv = d3.select(this.elem).select('#groupstackchart').append('div');

    const svgLegend = d3
      .select(this.elem)
      .select('#svgLegend')
      .append('svg')
      .attr('width', width)
      .attr('height', 50)
      .append('g');

    const legend = svgLegend.selectAll('.d3-legend')
      .data(Object.keys(stackData)[0])
      .enter()
      .append('g')
      .attr('class', 'd3-legend')
      .attr('transform', function(d, i) {
        return 'translate(40, 10)';
      });

    if (this.legendType == 'normal') {
      const topValue = 30;
      legendDiv.transition()
        .duration(200)
        .style('display', 'block')
        .style('opacity', 1)
        .attr('class', 'p-d-flex p-flex-wrap normal-legend');;

      let htmlString = '';

      stackData.forEach((key, i) => {
        if (stackColorsList[i]) {
          htmlString += `<div class="legend_item"><div class="legend_color_indicator" style="background-color: ${stackColorsList[i]}"></div> : ${key['key']}</div>`;
        }
      });

      legendDiv.html(htmlString)
        .style('left', 70 + 'px')
        .style('top', y[0] - topValue + 'px');

    } else {
      legendDiv.attr('class', 'legend-tooltip')
        .style('display', 'none')
        .style('opacity', 0);

      legend.append('rect')
        .attr('width', 12)
        .attr('height', 12)
        .attr('x', 0)
        .attr('y', -7)
        .style('fill', function(d, i) {
          return '#DF9292';
        });

      legend.append('text')
        .attr('x', 18)
        .attr('y', -6)
        .attr('dy', '.85em')
        .style('text-anchor', 'start')
        .style('font-size', 10)
        .text(function(d) {
 return 'Legend';
});

      legend
        .on('mouseover', function() {
          const topValue = 30;

          legendDiv.transition()
            .duration(200)
            .style('display', 'block')
            .style('opacity', 1);

          let htmlString = '';

          stackData.forEach((key, i) => {
            if (stackColorsList[i]) {
              htmlString += `<div class="legend_item"><div class="legend_color_indicator" style="background-color: ${stackColorsList[i]}"></div> : ${key['key']}</div>`;
            }
          });

          legendDiv.html(htmlString)
            .style('left', 70 + 'px')
            .style('top', y[0] - topValue + 'px');
        })
        .on('mouseout', function() {
          legendDiv.transition()
            .duration(500)
            .style('display', 'none')
            .style('opacity', 0);

        });
    }

    // }

    // wrap legend text
    // legend.selectAll('text').call(wrap, 30);


    function wrap(text, textWidth) {
      text.each(function() {
        const textContent = d3.select(this);
          const words = textContent.text().trim().split(/\s+/).reverse();
          let word;
          let line = [];
          let lineNumber = 0;
          const lineHeight = 1.1; // ems
          const yPosition = textContent.attr('y');
          const dy = parseFloat(textContent.attr('dy'));
          let tspan = textContent
            .text(null)
            .append('tspan')
            .attr('x', 10)
            .attr('y', yPosition)
            .attr('dy', dy + 'em');

        if (words.length > 1) {
          while ((word = words.pop())) {
            line.push(word);
            tspan.text(line.join(' '));
            if (tspan.node().getComputedTextLength() > textWidth) {
              line.pop();
              tspan.text(line.join(' '));
              line = [word];
              tspan = textContent
                .append('tspan')
                .attr('x', 0)
                .attr('y', yPosition)
                .attr('dy', ++lineNumber * lineHeight + dy + 'em')
                .text(word);
            }
          }
        } else {
          tspan.text(words[0]);
          let i = 0;
          while (tspan.node().getComputedTextLength() > textWidth && i <= 4) {
            i = 2;
            word = words[0].substring(0, words[0].length / i);
            tspan = textContent
              .append('tspan')
              .attr('x', 0)
              .attr('y', yPosition)
              .attr('dy', ++lineNumber * lineHeight + dy + 'em')
              .text(word);

            ++i;
          }
        }
      });
    }

    const content = this.elem.querySelector('#horizontalSVG');
    content.scrollLeft += width;
  }

  // padData(data){
  //   let tempArr = [];
  //   if(data?.length < 3){
  //     for(let i = 3; i>data.length;i--){
  //       let obj = JSON.parse(JSON.stringify(data[0]));
  //       obj.data = obj.data+i;
  //       obj.value.forEach(x => {
  //         x.value = {test:0};
  //       })
  //       tempArr.push(obj);
  //     }
  //   }
  //   return tempArr.length ? data?.length == 1 ? [tempArr[0], ...data, tempArr[1]]: [tempArr[0], ...data] : data;
  // }

  formatData() {
    if (this.data?.length > 0) {

      // console.log(this.data);
      // this.data = this.padData(this.data);
      let prevsum = 0; let currentSum = 0; let max = 0;
      const targetList = [];
      this.data.forEach((pro) => {
        pro.value?.forEach((item, index) => {
          const obj = {};
          obj['sprojectName'] = pro.data;
          if (item.hoverValue) {
            obj['hoverValue'] = item.hoverValue;
            obj['sSprintName'] = item.sSprintName;
          }
          const sprintValue = index + 1;
          if (typeof (item.value) === 'object') {
            if (item.value) {
              const types = Object.keys(item.value);
              if (types.length >= 1) {
                types.forEach(function(type) {
                  obj['type'] = type;
                  obj['value'] = item.value[type];
                  obj['xName'] = item.xAxisTick ? item.xAxisTick : sprintValue;
                  targetList.push({ ...obj });
                  currentSum = currentSum + item.value[type];
                });
              } else {
                obj['type'] = '';
                obj['value'] = '';
                obj['xName'] = item.xAxisTick ? item.xAxisTick : sprintValue;
                targetList.push({ ...obj });
                currentSum = currentSum + 0;
              }
            }
          } else {
            obj['value'] = item.value;
            obj['xName'] = item.xAxisTick ? item.xAxisTick : sprintValue;
            targetList.push(obj);
            currentSum = currentSum + item.value;
          }
          max = Math.max(prevsum, currentSum);
          prevsum = currentSum > prevsum ? currentSum : prevsum;
          currentSum = 0;

          if (item.xAxisTick) {
            this.xCaption = 'Months';
          }
        });
      });
      this.maxYValue = max * 1.07;
      // console.log("targetList", targetList);

      return this.data = targetList;
    }
  }


}
