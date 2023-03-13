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

import { Component, Input, ViewContainerRef, OnChanges, SimpleChanges } from '@angular/core';
import * as d3 from 'd3';


@Component({
  selector: 'app-piechart',
  templateUrl: './piechart.component.html',
  styleUrls: ['./piechart.component.css']
})

export class PiechartComponent implements OnChanges {


  @Input() value: string;

  elem;

  constructor(private viewContainerRef: ViewContainerRef) { }

  ngOnChanges(changes: SimpleChanges) {
    // only run when property "data" changed
    if (changes['value']) {
      this.elem = this.viewContainerRef.element.nativeElement;


      if (!changes['value'].firstChange) {

        this.draw('update');
      } else {
        this.draw('new');
      }

    }
  }




  draw(status) {


    const elem = this.elem;
    if (status !== 'new') {
      d3.select(this.elem).select('svg').remove();
    }
    const height = 120;
    const width = 220;
    const canvas = d3.select(elem).select('.wrapper').append('svg').attr('width', width + 'px')
      .attr('height', (height + 30) + 'px');


    const data = [{
      label: 'Actual',
      value: parseInt(this.value, 10)
    }, {
      label: 'remaining',
      value: 100 - parseInt(this.value, 10)
    }];

    const colors = ['#81d0e0', '#2d7196'];

    // const colorscale = d3.scaleLinear().domain([0, data.length]).range(colors);

    const arc = d3.arc()
      .innerRadius(0)
      .outerRadius(function(d, i) {
        if (d.data.value <= 50) {
          return 45;
        } else {
          return 60;
        }

      });

    d3.arc()
      .innerRadius(0)
      .outerRadius(60);

    const pie = d3.pie()
      .value(function(d) {
        return d.value;
      });

    const renderarcs = canvas.append('g')
      .attr('transform', 'translate(' + width / 2 + ',' + height / 2 + ')')
      .selectAll('.arc')
      .data(pie(data))
      .enter()
      .append('g')
      .attr('class', 'arc');


    renderarcs.append('path')

      .attr('d', arc)
      .attr('fill', function(d, i) {
        return colors[i];
      })
      .on('mouseover', function(d) {
        const currentEl = d3.select(this);
        currentEl.attr('style', 'fill-opacity:1;');
        currentEl.attr('stroke', 'white')
          .transition()
          .duration(1000)
          .attr('stroke-width', 2);
      })
      .on('mouseout', function(d) {
        const currentEl = d3.select(this);
        currentEl.transition()
          .attr('stroke', 'none');
      });

    renderarcs.append('text')
      .style('font-size', '13px')
      .attr('transform', function(d) {
        const c = arc.centroid(d);
        if (d.value === 100) {
          return 'translate(' + (c[0] - 15) + ',' + 0 + ')';
        } else {
          return 'translate(' + (c[0] - 15) + ',' + c[1] + ')';
        }
      })
      .text(function(d) {
        if (d.value !== 0) {
          return d.value + '%';

        }
      });
    const legendData = ['Actual', 'Remaining']; // The names which you want to appear as legends should be inside this array.
    const margin = 50;

    const legend = d3.select(elem).select('svg')
      .append('g')
      .attr('class', 'legendText')
      .attr('transform', 'translate(' + (margin) + ',' + (height + 15) + ')')
      .selectAll('g')
      .data(legendData)
      .enter()
      .append('g');


    legend.append('circle')
      .style('fill', function(d, i) {
 return colors[i];
})
      .style('stroke', function(d, i) {
 return colors[i];
})
      .attr('r', 5);

    legend.append('text')
      .attr('x', 12)
      .attr('y', 3)
      .attr('dy', '.15em')
      .text((d, i) => d)
      .style('text-anchor', 'start')
      .style('font-size', 12);

    // Now space the groups out after they have been appended:
    const padding = 10;
    legend.attr('transform', function(d, i) {
      return 'translate(' + (d3.sum(legendData, function(e, j) {
        if (j < i) {
 return legend.nodes()[j].getBBox().width;
} else {
 return 0;
}
      }) + padding * i) + ',0)';
    });

  }

}
