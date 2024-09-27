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

import { Component, Input, ViewContainerRef, OnChanges, SimpleChanges, AfterViewInit } from '@angular/core';
import * as d3 from 'd3'; // importing d3 version  3


/*********************************************

This file contains Line and bar chart component which can
be reused .It can make line chart ,  bar chart or
combination of both using d3.js (version 3).
@author anuj nandwana

*******************************/



@Component({
    selector: 'app-line-bar-chart',
    templateUrl: './line-bar-chart.component.html',
    styleUrls: ['./line-bar-chart.component.css']
})
export class LineBarChartComponent implements OnChanges, AfterViewInit {

    @Input() lineChart: string;
    @Input() barChart: string;
    @Input() data: any;
    @Input() filter: any;
    @Input() width: number;
    @Input() maturity: string;
    @Input() fillColor: string;
    @Input() maxValue: number;
    @Input() targetValue: number;
    @Input() isChildComponent: boolean;
    @Input() isEnggMaturity?: boolean;
    elem;
    filteredData: any;

    constructor(private viewContainerRef: ViewContainerRef) {
        this.elem = this.viewContainerRef.element.nativeElement;
    }


    ngOnChanges(changes: SimpleChanges) {
        // only run when property "data" changed

        if (changes['data']) {
            this.elem = this.viewContainerRef.element.nativeElement;
            if (!changes['data'].firstChange) {
                this.lineWithBar('update');
            } else {
                this.lineWithBar('new');
            }
        }

        if (this.filter && changes['filter']) {
            if (!changes['filter'].firstChange) {

                this.filteredData = this.filterTrendData();
                this.lineWithBar('update');
            }
        }
    }

    ngAfterViewInit(): void {
        if (this.isChildComponent) {
            this.lineWithBar('new');
        }
    }

    filterTrendData() {
        if (this.filter && this.filter.length !== 0) {
            return this.filter;
        } else {
            return this.data;
        }
    }


    lineWithBar(status) {

        let data = this.filteredData || this.data;

        if (data) {
            if (status !== 'new') {
                d3.select(this.elem).select('svg').remove();
            }

            // changing format to required format
            const newFormat = [];
            for (const obj1 in data) {
                const tempArray = [];
                tempArray.push(data[obj1].data);
                tempArray.push(data[obj1].count);
                if (data[obj1].submissions) {
                    tempArray.push(data[obj1].submissions);
                }
                newFormat.push(tempArray);
            }

            data = newFormat;

            const max = d3.max(data, (function(d) {
                return d[1];
            }));


            let marginOnBasisOfMax = 5;
            if (max >= 1000 && max < 10000) {
                marginOnBasisOfMax = 15;
            } else if (max >= 10000 && max < 100000) {
                marginOnBasisOfMax = 18;
            } else if (max >= 100000) {
                marginOnBasisOfMax = 23;
            }


            // getting parent width and setting it to svg so that width can work properly on diff devices
            this.width = d3.select(this.elem).select('.lineWithBar').node().getBoundingClientRect().width - 20;
            const margin = {
                top: 20,
                right: 15,
                bottom: 5,
                left: 15 + marginOnBasisOfMax
            };
                const width = this.width;
            let height = 200 - 25 - 25;


            if (this.lineChart === 'true' && this.barChart === 'true') {
                height -= 10;
                margin.top += 20;
            }
            // defining scale x
            const xScale = d3.scaleBand()
                .rangeRound([0, width - margin.left - margin.right])
                .padding(0.1)
                .domain(data.map(function(d) {
                    return d[0];
                }));
            // defining scale y
            const yScale = d3.scaleLinear()
                .rangeRound([height, 0])
                .domain([0, this.maxValue && this.maxValue > 0 ? this.maxValue : d3.max(data, (function(d) {
                    return parseInt(d[1]);
                }))]);

            const svg = d3.select(this.elem).select('.lineWithBar').append('svg')
                .attr('width', width + 'px')
                .attr('height', '225px');
            const colors = ['#009688', '#3f51b5'];

            const grad = svg.append('defs')
                .append('linearGradient')
                .attr('id', 'grad')
                .attr('x1', '0%')
                .attr('x2', '0%')
                .attr('y1', '0%')
                .attr('y2', '100%');

            grad.selectAll('stop')
                .data(colors)
                .enter()
                .append('stop')
                .style('stop-color', function(d) {
                    return d;
                })
                .attr('offset', function(d, i) {
                    return 100 * (i / (colors.length - 1)) + '%';
                });



            const g = svg.append('g')
                .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')').attr('width', width + 'px')
                .attr('height', '220px');

            const x = d3.scaleBand()
                .rangeRound([0, width])
                .paddingInner(.1)
                .paddingOuter(.3);

            const wrap = function(text, width) {
                text.each(function() {
                    const text = d3.select(this);
                        const words = text.text().split(/\s+/).reverse();
                        let word;
                        let line = [];
                        let lineNumber = 0;
                        const lineHeight = 1.1; // ems
                        const y = text.attr('y');
                        const dy = parseFloat(text.attr('dy'));
                        let tspan = text.text(null).append('tspan').attr('x', 0).attr('y', y).attr('dy', dy + 'em');
                    while (word = words.pop()) {
                        line.push(word);
                        tspan.text(line.join(' '));
                        if (tspan.node().getComputedTextLength() > width) {
                            line.pop();
                            tspan.text(line.join(' '));
                            line = [word];
                            tspan = text.append('tspan').attr('x', 0).attr('y', y).attr('dy', `${++lineNumber * lineHeight + dy}em`).text(word);
                        }
                    }
                });
            };

            // axis-x
            g.append('g')
                .attr('class', 'axis axis--x')
                .attr('transform', 'translate(0,' + height + ')')
                .call(d3.axisBottom(xScale))
                .selectAll('.tick text')
                .style('text-anchor', 'end')
                .attr('dx', '-.8em')
                .attr('dy', '.15em')
                .attr('transform', 'rotate(-45)')
                .call(wrap, 50);

            // axis-y
            g.append('g')
                .attr('class', 'axis axis--y')
                .call(d3.axisLeft(yScale).tickSize(-width));




            const bar = g.selectAll('rect')
                .data(data)
                .enter().append('g');

            // check whether bar chart is true
            if (this.barChart === 'true') {

                const green = '#AEDB76';
                const red = '#F06667';
                const yellow = '#eff173';
                const orange = '#ffc35b';
                const darkGreen = '#6cab61';
                const blue = '#44739f';


                let fillColor;

                // check naturity and sets colors acc to it

                if (this.maturity === '1') {
                    fillColor = red;
                } else if (this.maturity === '2') {
                    fillColor = orange;
                } else if (this.maturity === '3') {
                    fillColor = yellow;
                } else if (this.maturity === '4') {
                    fillColor = green;
                } else if (this.maturity === '5') {
                    fillColor = darkGreen;
                } else if (this.fillColor && this.fillColor.length) {
                    fillColor = this.fillColor;
                } else {
                    fillColor = blue;
                }


                // bar chart
                bar.append('rect')
                    .attr('x', function(d) {
                        return xScale(d[0]) + 6;
                    })
                    .attr('width', xScale.bandwidth() - 10)
                    .attr('y', function(d) {
                        return height;
                    })
                    .style('fill', fillColor)
                    .transition()
                    .duration(1000)
                    .attr('y', function(d) {
                        return yScale(d[1]);
                    })
                    .attr('height', function(d) {
                        if (d[1] === 0) {
                            return 0;
                        } else {
                            return height - yScale(d[1]);
                        }
                    })
                    .attr('class', 'bar');

                // labels on the bar chart
                bar.append('text')
                    .attr('dy', '1.3em')
                    .attr('x', function(d) {
                        return xScale(d[0]) + xScale.bandwidth() / 2;
                    })
                    .attr('y', function(d) {
                        return yScale(d[1]) - 22;
                    })
                    .attr('text-anchor', 'middle')
                    .attr('font-family', 'sans-serif')
                    .attr('font-size', '11px')
                    .attr('fill', 'black')
                    .text(function(d) {
                        if (d[0] && (d[0] + '').indexOf('*') > -1) {
                            return 'No Data';
                        } else {
                            let temp = '';
                            if (d[2]) {
                                temp = `(${d[2]})` + ' ' + d[1];
                            } else {
                                temp = d[1];
                            }
                            return temp;
                        }
                    });

                if (this.targetValue) {
                    svg.append('line')
                        .attr('x1', 0)
                        .attr('x2', width)
                        .attr('y1', yScale(this.targetValue))
                        .attr('y2', yScale(this.targetValue))
                        .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')')
                        .attr('stroke-width', 1)
                        .attr('stroke', 'black')
                        .attr('stroke-dasharray', '4,4');
                }
            }

            if (this.lineChart === 'true') {
                // line chart
                const line = d3.line()
                    .x(function(d, i) {
                        return xScale(d[0]) + xScale.bandwidth() / 2;
                    })
                    .y(function(d) {
                        return yScale(d[1]);
                    });

                bar.append('path')
                    .attr('class', 'line') // Assign a class for styling
                    .style('fill', 'none')
                    .style('stroke', 'var(--color-black)')
                    .style('stroke-width', '2px')
                    .style('shape-rendering', 'geometricPrecision')
                    .attr('d', line(data)); // Calls the line generator
                bar.append('circle') // Uses the enter().append() method
                    .attr('class', 'dot') // Assign a class for styling
                    .attr('cx', function(d, i) {
                        return xScale(d[0]) + xScale.bandwidth() / 2;
                    })
                    .attr('cy', function(d) {
                        return yScale(d[1]);
                    })
                    .attr('r', 5);
                if (this.barChart === 'false') {
                    bar.append('text')
                        .attr('dy', '1.3em')
                        .attr('x', function(d) {
                            return xScale(d[0]) + xScale.bandwidth() / 2;
                        })
                        .attr('y', function(d) {
                            return yScale(d[1]);
                        })
                        .attr('text-anchor', 'middle')
                        .attr('font-family', 'sans-serif')
                        .attr('font-size', '11px')
                        .attr('fill', 'black')
                        .text(function(d) {
                            return d[1];
                        });

                }
            }
        }
    }

    wrap(text, width) {

    }
}
