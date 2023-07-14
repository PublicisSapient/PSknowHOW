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

import { Component, Input, ViewContainerRef, OnChanges, SimpleChanges, OnInit } from '@angular/core';
import * as d3 from 'd3';
@Component({
    selector: 'app-circular-progress-with-legends',
    templateUrl: './circular-progress-with-legends.component.html',
    styleUrls: ['./circular-progress-with-legends.component.css']
})
export class CircularProgressWithLegendsComponent implements OnInit, OnChanges {
    @Input() kpiName: string;
    @Input() maxValue: string;
    @Input() value: string;
    @Input() unit: string;
    @Input() fillColor: string;
    @Input() backgroundColor: string;
    @Input() width: string;
    @Input() height: string;
    @Input() radius: string;
    @Input() fontSize: string;
    @Input() thickness: string;
    @Input() maturity: string;
    @Input() data: any;
    @Input() projectName: string;

    elem;
    constructor(private viewContainerRef: ViewContainerRef) { }


    fields = [];

    ngOnChanges(changes: SimpleChanges) {
        // only run when property "data" changed
        if (changes['value']) {
            this.elem = this.viewContainerRef.element.nativeElement;

            if (this.maxValue === '0') {
                this.maxValue = '100';
            }
            this.fields = [{
                value: this.maxValue,
                size: this.maxValue,
                label: this.unit,
                update(value) {
                    return value;
                }
            }];
            if (!changes['value'].firstChange) {
                this.draw('update');
            } else {
                this.draw('new');
            }

        }
    }

    ngOnInit() {
    }

    draw(status) {
        const self = this;
        if (this.value !== undefined) {

            if (status !== 'new') {
                d3.select(this.elem).select('svg').remove();
            }


            let fillColor = this.fillColor;
            const green = '#AEDB76';
            const red = '#F06667';
            const yellow = '#eff173';
            const orange = '#ffc35b';
            const darkGreen = '#6cab61';
            const blue = '#44739f';

            switch (this.maturity) {
                case '1':
                    fillColor = red;
                    break;
                case '2':
                    fillColor = orange;
                    break;
                case '3':
                    fillColor = yellow;
                    break;
                case '4':
                    fillColor = green;
                    break;
                case '5':
                    fillColor = darkGreen;
                    break;
                default:
                    fillColor = blue;
            }

            const svgLegends = d3.select(this.elem).select('.legends')
                .append('svg')
                .attr('height', 220)
                .attr('width', 500);

            this.data?.forEach((eachData, iterator) => {
                svgLegends.append('text')
                    .attr('height', 10)
                    .attr('width', 100)
                    .attr('x', 240)
                    .attr('y', 190 + 15 * (iterator))
                    .html(eachData.data ? eachData.data + ': ' + eachData.count : Object.keys(eachData)[0] + ': ' + eachData[Object.keys(eachData)[0]]);
            });

            if (this.projectName && this.projectName.length) {
                svgLegends.append('rect')
                    .attr('fill', '#AEDB76')
                    .attr('height', 30)
                    .attr('width', 60)
                    .attr('x', 20)
                    .attr('y', 20)
                    .attr('rx', 5)
                    .attr('ry', 5);

                svgLegends.append('text')
                    .html('Project &nbsp;&nbsp;' + truncate(this.projectName))
                    .attr('height', 30)
                    .attr('width', 200)
                    .attr('x', 25)
                    .attr('y', 40)
                    .attr('class', 'projectName')
                    .attr('title', this.projectName);

                const div = d3.select(self.elem).select('.legends').append('div')
                    .attr('class', 'projectNameTooltip')
                    .style('opacity', 0);

                if (this.projectName && this.projectName.length >= 20) {
                    const projectLabel = d3.selectAll('.projectName');
                    projectLabel
                        .on('mouseover', function(event,d) {
                            div.transition()
                                .duration(200)
                                .style('opacity', .9);
                            div.html(self.projectName)
                                .style('left', (event.layerX - 25) + 'px')
                                .style('top', (event.layerY + 10) + 'px');
                        })
                        .on('mouseout', function(d) {
                            div.transition()
                                .duration(500)
                                .style('opacity', 0);
                        });
                }
            }

            let backgroundColor = this.backgroundColor;
            if (this.kpiName === 'Code Build Time') {
                backgroundColor = fillColor;
            }

            d3.select('body')
                .append('div')
                .style('position', 'absolute')
                .style('z-index', '10')
                .style('visibility', 'hidden');
            const radiusNum = parseInt(this.radius, 10);
            const thickness = parseInt(this.thickness, 10);
            var arc = d3.arc().innerRadius(radiusNum).outerRadius(radiusNum + thickness).startAngle(0).endAngle(function(d) {
                return (d.value / d.size) * 2 * Math.PI;
            });
            const svg = d3.select(this.elem).select('.content').append('svg').attr('width', this.width + 'px').attr('height', this.height + 'px');
            const field = svg.selectAll('.field').remove().exit().data(this.fields).enter().append('g').attr('transform', function(d, i) {
                return 'translate(' + (radiusNum + thickness) + ',' + (radiusNum + thickness) + ')';
            });

            field.append('path').attr('class', 'path path--background').attr('d', arc).style('border', '1px solid black').style('fill', function(d, i) {
                return backgroundColor;
            });



            const tooltip = d3.select('.content')
                .append('div')
                .attr('class', 'tooltip')
                .style('background-color', '#dedede')
                .style('display', 'block');

            tooltip.append('div')
                .attr('class', 'label');

            const path = field.append('path').attr('class', 'path path--foreground').style('fill', function(d, i) {
                return fillColor;
            });


            const label = field.append('text').attr('class', 'label').attr('dy', '.15em').attr('dx', '0px').style('fill', '#4a4a4a').style('font-size', this.fontSize + 'px').style('font-weight', 'bold').style('text-anchor', 'middle');

            const updatedValue = this.value;
            path.on('mouseenter', function(d) {
                d3.select(this)
                    .attr('stroke', 'white')
                    .transition()
                    .duration(1000)
                    .attr('stroke-width', 2);
            });
            path.on('mouseleave', function(d) {
                d3.select(this).transition()
                    .attr('stroke', 'none');
            });

            // add values here to update
            field.each(function(d) {
                d.previous = d.value;
                d.value = d.update(updatedValue);
            });


            path.transition().duration(1500).attrTween('d', arcTween);
            label.text(updatedValue + ' ' + this.unit);


        }

        function arcTween(b) {
            const i = d3.interpolate({
                value: b.value
            }, b);
            return function(t) {
                return arc(i(t));
            };
        }

        function truncate(text) {
            if (text && text.length >= 20) {
                return text.substring(0, 20) + '...';
            }
            return text;
        }

    }

}
