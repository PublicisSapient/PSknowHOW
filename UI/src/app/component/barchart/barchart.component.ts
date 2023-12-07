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
    selector: 'app-barchart',
    templateUrl: './barchart.component.html',
    styleUrls: ['./barchart.component.css']
})
export class BarchartComponent implements OnChanges {
    @Input() data: any;
    elem;
    fields = [];

    constructor(private viewContainerRef: ViewContainerRef) {

    }
    ngOnChanges(changes: SimpleChanges) {
        // only run when property "data" changed
        if (changes['data']) {
            this.elem = this.viewContainerRef.element.nativeElement;
            if (!changes['data'].firstChange) {
                this.draw('update');
            } else {
                this.draw('new');
            }
        }
    }


    draw(status) {
        if (this.data) {
            let data = this.data;
            // sort bars based on value
            data = data.sort(function(a, b) {
                return d3.ascending(a.count, b.count);
            });
            // set up svg using margin conventions - we'll need plenty of room on the left for labels
            const margin = {
                top: 15,
                right: 25,
                bottom: 15,
                left: 60
            };

            const width = 80;
                const height = 100;

            if (status !== 'new') {
                d3.select(this.elem).select('svg').remove();
            }

            const svg = d3.select(this.elem).select('.graphic').append('svg').attr('class', 'barChart').attr('width', width + margin.left + margin.right).attr('height', height + margin.top + margin.bottom)
                .append('g')
                .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')');


            const x = d3.scaleLinear()
                .range([0, width - 20])
                .domain([0, d3.max(data, function(d) {
                    return parseInt(d.count);
                })]);
            const y = d3.scaleBand()
                .rangeRound([height, 0], .1)
                .domain(data.map(function(d) {
                    return d.data;
                }));

            // make y axis to show bar names
            const yAxis = d3.axisLeft(y);

            // no tick marks
            svg.append('g')
                .attr('class', 'y axis')
                .call(yAxis);
            const bars = svg.selectAll('.bar').exit().remove()
                .data(data)
                .enter()
                .append('g');

            // append rects
            bars.append('rect')
                .attr('class', 'bar')
                .attr('y', function(d) {

                    return y(d.data) + 8;
                })
                .attr('height', 10)
                .attr('x', 2)
                .style('fill', function(d, i) {
 return '#ffc001';
})
                .attr('width', function(d) {

                    return (x(d.count));
                });

            // add a value label to the right of each bar
            bars.append('text')
                .attr('class', 'label')
                // y position of the label is halfway down the bar
                .attr('y', function(d) {
                    return y(d.data) + y.bandwidth() / 2 + 6;
                })
                // x position is 3 pixels to the right of the bar
                .attr('x', function(d) {
                    return width - 15;
                })
                .text(function(d) {
                    return d.count;
                })
                .style('color', '#7d7d7d')
                .style('font-size', '12px');

            svg.selectAll('.domain').style('display', 'none');
            svg.selectAll('.tick line').style('display', 'none');
        }

    }

}
