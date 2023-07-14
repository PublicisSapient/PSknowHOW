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

import { Component, Input, ViewContainerRef, SimpleChanges, OnChanges } from '@angular/core';
import * as d3 from 'd3';

@Component({
  selector: 'app-gaugechart',
  templateUrl: './gaugechart.component.html',
  styleUrls: ['./gaugechart.component.css']
})
export class GaugechartComponent implements OnChanges {

  gaugemap = <any>{};
  currentValue;

  @Input() value: string;
  @Input() maxValue: string;
  @Input() type: string;

  elem;
  constructor(private viewContainerRef: ViewContainerRef) {

  }

  ngOnChanges(changes: SimpleChanges) {
    // only run when property "data" changed
    if (changes['value']) {
      this.elem = this.viewContainerRef.element.nativeElement;

      if (this.type === 'ART') {
        this.maxValue = '50';
      } else {
        this.maxValue = '100';
      }
      if (this.maxValue === '') {
        this.maxValue = this.value;
      }
      if (!changes['value'].firstChange) {

        this.drawGauge('update');
      } else {
        this.drawGauge('new');
      }

    }
  }

  drawGauge(status) {

    if (status !== 'new') {
      d3.select(this.elem).select('svg').remove();
    }
    let name = '';
    if (this.type == 'RCA') {
      name = this.value + ' ';
    }
    if (this.type == 'ART') {
      name = this.value + ' Days';
    } else {
      name = this.value + '%';
    }
    const gaugeMaxValue = parseInt(this.maxValue, 10);

    if (this.type === 'DIR') {
      if (parseInt(this.value, 10) <= 115) {
        this.value = ((parseInt(this.value, 10) * 15) / 115).toString();

      } else if (parseInt(this.value, 10) > 115 && parseInt(this.value, 10) < 200) {
        this.value = (parseInt(this.value, 10) - 100).toString();

      }
    }

    let value = this.value;

    if (this.type !== 'DIR') {
      if (parseInt(value, 10) > 100) {
        value = '100';
      }
      if (this.type === 'ART' && parseInt(value, 10) > 50) {
        value = '50';
      }
    } else {
      if (parseInt(value, 10) >= 200) {
        value = '100';
      }
    }



    const percentValue = parseInt(value, 10) / gaugeMaxValue;

    const el = d3.select(this.elem).select('.chart-gauge');

    const type = this.type;
    (function() {

      let barWidth; let chart; let chartInset; let degToRad; let repaintGauge;
        let height; let margin; let percToDeg; let percToRad;
        let percent; let radius; let svg; let totalPercent; let width;

      percent = percentValue;
      chartInset = 10;

      // Orientation of gauge:
      totalPercent = .75;
      margin = {
        top: 30,
        right: 20,
        bottom: 30,
        left: 20
      };

      width = 225 - margin.left - margin.right;
      height = width;
      radius = Math.min(width, height) / 2;
      barWidth = 30 * width / 300;

      // Utility methods

      percToDeg = function(perc) {
        return perc * 360;
      };

      percToRad = function(perc) {
        return degToRad(percToDeg(perc));
      };

      degToRad = function(deg) {
        return deg * Math.PI / 180;
      };

      // Create SVG element
      svg = el.append('svg').attr('width', width + margin.left + margin.right - 20).attr('height', 170);


      // Add layer for the panel
      chart = svg.append('g').attr('transform', 'translate(' + ((width + margin.left) / 2) + ', ' + ((height + margin.top) / 2) + ')');

      const green = '#AEDB76';
      const red = '#F06667';
      const yellow = '#eff173';
      const orange = '#ffc35b';
      const darkGreen = '#6cab61';

      const colorArr1 = [darkGreen, green, yellow, orange, red];
      const colorArr2 = [red, orange, yellow, green, darkGreen];

      addColorLayer();

      function addColorLayer() {
        let color;
        if (type === 'DRE' || type === 'FTPR') {
          color = colorArr2;
        } else {
          color = colorArr1;
        }
        chart.append('path').attr('class', 'arc chart-first').style('fill', color[0]);
        chart.append('path').attr('class', 'arc chart-second').style('fill', color[1]);
        chart.append('path').attr('class', 'arc chart-third').style('fill', color[2]);
        chart.append('path').attr('class', 'arc chart-fourth').style('fill', color[3]);
        chart.append('path').attr('class', 'arc chart-fifth').style('fill', color[4]);
      }

      const arc5 = d3.arc().outerRadius(radius - chartInset).innerRadius(radius - chartInset - barWidth);
      const arc4 = d3.arc().outerRadius(radius - chartInset).innerRadius(radius - chartInset - barWidth);
      const arc3 = d3.arc().outerRadius(radius - chartInset).innerRadius(radius - chartInset - barWidth);
      const arc2 = d3.arc().outerRadius(radius - chartInset).innerRadius(radius - chartInset - barWidth);
      const arc1 = d3.arc().outerRadius(radius - chartInset).innerRadius(radius - chartInset - barWidth);

      repaintGauge = function() {
        const perc = 0.5;
        const next_start = totalPercent;
        const arcStartRad1 = percToRad(next_start);

        const range1 = [15, 10, 25, 25, 25];
        const range2 = [40, 20, 20, 10, 10];
        const range3 = [2, 3, 5, 10, 80];
        const range4 = [25, 25, 25, 15, 10];
        const range5 = [5, 3, 4, 8, 30];
        let range;


        if (type === 'DIR') {
          range = range1;
        } else if (type === 'DRE') {
          range = range2;
        } else if (type === 'FTPR') {
          range = range4;
        } else if (type === 'ART') {
          range = range5;
        } else {
          range = range3;
        }

        let divisor = 100;
        if (type === 'ART') {
          divisor = 50;
        }
        const arcEndRad1 = arcStartRad1 + percToRad(perc * range[0] / divisor);
        const arcStartRad2 = arcEndRad1;
        const arcEndRad2 = arcStartRad2 + percToRad(perc * range[1] / divisor);
        const arcStartRad3 = arcEndRad2;
        const arcEndRad3 = arcStartRad3 + percToRad(perc * range[2] / divisor);
        const arcStartRad4 = arcEndRad3;
        const arcEndRad4 = arcStartRad4 + percToRad(perc * range[3] / divisor);
        const arcStartRad5 = arcEndRad4;
        const arcEndRad5 = arcStartRad5 + percToRad(perc * range[4] / divisor);

        arc1.startAngle(arcStartRad1).endAngle(arcEndRad1);
        arc2.startAngle(arcStartRad2).endAngle(arcEndRad2);
        arc3.startAngle(arcStartRad3).endAngle(arcEndRad3);
        arc4.startAngle(arcStartRad4).endAngle(arcEndRad4);
        arc5.startAngle(arcStartRad5).endAngle(arcEndRad5);

        chart.select('.chart-first').attr('d', arc1);
        chart.select('.chart-second').attr('d', arc2);
        chart.select('.chart-third').attr('d', arc3);
        chart.select('.chart-fourth').attr('d', arc4);
        chart.select('.chart-fifth').attr('d', arc5);

      };

      const dataset = [{ metric: name, value }];

      const texts = svg.selectAll('text')
        .data(dataset)
        .enter();

      texts.append('text')
        .text(function() {
          return dataset[0].metric;
        })
        .attr('id', 'Name')
        .attr('transform', 'translate(' + (112) + ', ' + ((height + margin.top) / 1.5) + ')')
        .attr('font-size', 18)
        .style('text-anchor', 'middle')
        .style('fill', '#000000');


      const trX = 180 - 210 * Math.cos(percToRad(percent / 2));
      const trY = 195 - 210 * Math.sin(percToRad(percent / 2));
      // (180, 195) are the coordinates of the center of the gauge.

      function displayValue() {
        texts.append('text')
          .text(function() {
            return dataset[0].value;
          })
          .attr('id', 'Value')
          .attr('transform', 'translate(' + trX + ', ' + trY + ')')
          .attr('font-size', 18)
          .style('fill', '#000000');
      }

      texts.append('text')
        .text(function() {
          return 0;
        })
        .attr('id', 'scale0')
        .attr('transform', 'translate(' + ((width + margin.left) / 100 + 25) + ', ' + ((height + margin.top) / 2 + 15) + ')')
        .attr('font-size', 15)
        .style('fill', '#000000');



      if (type === 'DIR') {

        texts.append('text')
          .text(function() {
            return 115;
          })
          .attr('id', 'scale15')
          .attr('transform', 'translate(' + (12) + ', ' + (70) + ')')
          .attr('font-size', 10)
          .style('fill', '#000000');

        texts.append('text')
          .text(function() {
            return 125;
          })
          .attr('id', 'scale25')
          .attr('transform', 'translate(' + (28) + ', ' + (50) + ')')
          .attr('font-size', 10)
          .style('fill', '#000000');

        texts.append('text')
          .text(function() {
            return 150;
          })
          .attr('id', 'scale50')
          .attr('transform', 'translate(' + (100) + ', ' + (22) + ')')
          .attr('font-size', 10)
          .style('fill', '#000000');

        texts.append('text')
          .text(function() {
            return 175;
          })
          .attr('id', 'scale75')
          .attr('transform', 'translate(' + (160) + ', ' + (49) + ')')
          .attr('font-size', 10)
          .style('fill', '#000000');
      }

      if (type === 'FTPR') {
        texts.append('text')
          .text(function() {
            return 25;
          })
          .attr('id', 'scale25')
          .attr('transform', 'translate(' + (30) + ', ' + (45) + ')')
          .attr('font-size', 10)
          .style('fill', '#000000');

        texts.append('text')
          .text(function() {
            return 50;
          })
          .attr('id', 'scale50')
          .attr('transform', 'translate(' + (95) + ', ' + (20) + ')')
          .attr('font-size', 10)
          .style('fill', '#000000');
        texts.append('text')
          .text(function() {
            return 75;
          })
          .attr('id', 'scale75')
          .attr('transform', 'translate(' + (165) + ', ' + (48) + ')')
          .attr('font-size', 10)
          .style('fill', '#000000');

        texts.append('text')
          .text(function() {
            return 90;
          })
          .attr('id', 'scale90')
          .attr('transform', 'translate(' + (185) + ', ' + (85) + ')')
          .attr('font-size', 10)
          .style('fill', '#000000');
      }

      if (type === 'ART') {
        texts.append('text')
          .text(function() {
            return 5;
          })
          .attr('id', 'scale25')
          .attr('transform', 'translate(' + (15) + ', ' + (85) + ')')
          .attr('font-size', 10)
          .style('fill', '#000000');

        texts.append('text')
          .text(function() {
            return 8;
          })
          .attr('id', 'scale50')
          .attr('transform', 'translate(' + (20) + ', ' + (68) + ')')
          .attr('font-size', 10)
          .style('fill', '#000000');
        texts.append('text')
          .text(function() {
            return 12;
          })
          .attr('id', 'scale75')
          .attr('transform', 'translate(' + (30) + ', ' + (45) + ')')
          .attr('font-size', 10)
          .style('fill', '#000000');

        texts.append('text')
          .text(function() {
            return 20;
          })
          .attr('id', 'scale90')
          .attr('transform', 'translate(' + (70) + ', ' + (25) + ')')
          .attr('font-size', 10)
          .style('fill', '#000000');
      }

      if (type === 'DRE') {
        texts.append('text')
          .text(function() {
            return 40;
          })
          .attr('id', 'scale40')
          .attr('transform', 'translate(' + (65) + ', ' + (28) + ')')
          .attr('font-size', 10)
          .style('fill', '#000000');

        texts.append('text')
          .text(function() {
            return 60;
          })
          .attr('id', 'scale60')
          .attr('transform', 'translate(' + (126) + ', ' + (27) + ')')
          .attr('font-size', 10)
          .style('fill', '#000000');
        texts.append('text')
          .text(function() {
            return 80;
          })
          .attr('id', 'scale80')
          .attr('transform', 'translate(' + (172) + ', ' + (60) + ')')
          .attr('font-size', 10)
          .style('fill', '#000000');

        texts.append('text')
          .text(function() {
            return 90;
          })
          .attr('id', 'scale90')
          .attr('transform', 'translate(' + (185) + ', ' + (85) + ')')
          .attr('font-size', 10)
          .style('fill', '#000000');
      }

      if (type === 'DSR' || type === 'DRR') {
        texts.append('text')
          .text(function() {
            return 5;
          })
          .attr('id', 'scale5')
          .attr('transform', 'translate(' + ((width + margin.left) / 100 + 12) + ', ' + (95) + ')')
          .attr('font-size', 10)
          .style('fill', '#000000');

        texts.append('text')
          .text(function() {
            return 10;
          })
          .attr('id', 'scale10')
          .attr('transform', 'translate(' + (10) + ', ' + (80) + ')')
          .attr('font-size', 10)
          .style('fill', '#000000');

        texts.append('text')
          .text(function() {
            return 20;
          })
          .attr('id', 'scale20')
          .attr('transform', 'translate(' + ((width + margin.left) / 2.15 - 75) + ', ' + ((height + margin.top) / 30 + 55) + ')')
          .attr('font-size', 10)
          .style('fill', '#000000');

        texts.append('text')
          .text(function() {
            return 2;
          })
          .attr('id', 'scale2')
          .attr('transform', 'translate(' + ((width + margin.left) / 100 + 12) + ', ' + (107) + ')')
          .attr('font-size', 10)
          .style('fill', '#000000');
      }

      if (type === 'DIR') {
        texts.append('text')
          .text(function() {
            return '200';
          })
          .attr('id', 'scale200')
          .attr('transform', 'translate(' + ((width + margin.left) / 1.03 - 35) + ', ' + ((height + margin.top) / 2 + 15) + ')')
          .attr('font-size', 15)
          .style('fill', '#000000');
      } else {
        texts.append('text')
          .text(function() {
            return gaugeMaxValue;
          })
          .attr('id', 'scale100')
          .attr('transform', 'translate(' + ((width + margin.left) / 1.03 - 35) + ', ' + ((height + margin.top) / 2 + 15) + ')')
          .attr('font-size', 15)
          .style('fill', '#000000');
      }


      const Needle = (function() {

        // Helper function that returns the `d` value for moving the needle
        const recalcPointerPos = function(perc) {
          let centerX; let centerY; let leftX; let leftY; let rightX; let rightY; let thetaRad; let topX; let topY;
          thetaRad = percToRad(perc / 2);
          centerX = 0;
          centerY = 0;
          topX = centerX - this.len * Math.cos(thetaRad);
          topY = centerY - this.len * Math.sin(thetaRad);
          leftX = centerX - this.radius * Math.cos(thetaRad - Math.PI / 2);
          leftY = centerY - this.radius * Math.sin(thetaRad - Math.PI / 2);
          rightX = centerX - this.radius * Math.cos(thetaRad + Math.PI / 2);
          rightY = centerY - this.radius * Math.sin(thetaRad + Math.PI / 2);
          return 'M ' + leftX + ' ' + leftY + ' L ' + topX + ' ' + topY + ' L ' + rightX + ' ' + rightY;
        };

        function Needle(el) {
          this.el = el;
          this.len = width / 2.5;
          this.radius = this.len / 8;
        }

        Needle.prototype.render = function() {
          this.el.append('circle').attr('class', 'needle-center').attr('cx', 0).attr('cy', 0).attr('r', this.radius);
          return this.el.append('path').attr('class', 'needle').attr('id', 'client-needle').attr('d', recalcPointerPos.call(this, 0));
        };

        Needle.prototype.moveTo = function(perc) {
          let self;
          const oldValue = this.perc || 0;

          this.perc = perc;
          self = this;

          // Reset pointer position
          this.el.transition().delay(100).duration(200).select('.needle').tween('reset-progress', function() {
            const node = this;
            return function(percentOfPercent) {
              const progress = (1 - percentOfPercent) * oldValue;
              repaintGauge(progress);
              return d3.select(node).attr('d', recalcPointerPos.call(self, progress));
            };
          });

          this.el.transition().delay(300).duration(1500).select('.needle').tween('progress', function() {
            const node = this;
            return function(percentOfPercent) {
              const progress = percentOfPercent * perc;
              repaintGauge(progress);
              return d3.select(node).attr('d', recalcPointerPos.call(self, progress));
            };
          });

        };
        return Needle;
      })();

      const needle = new Needle(chart);
      needle.render();
      needle.moveTo(percent);

      /*setTimeout(displayValue, 1350);*/
    })();
  }
}




