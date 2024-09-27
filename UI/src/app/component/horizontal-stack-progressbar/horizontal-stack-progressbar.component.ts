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

import { Component, Input, OnChanges, SimpleChanges, ViewContainerRef } from '@angular/core';


import * as d3 from 'd3';

@Component({
    selector: 'app-horizontal-stack-progressbar',
    templateUrl: './horizontal-stack-progressbar.component.html',
    styleUrls: ['./horizontal-stack-progressbar.component.css']
})
export class HorizontalStackProgressbarComponent implements OnChanges {
    @Input() value: any;
    @Input() maturity: string;
    // @Input() data: any;
    maxPercent: number;
    elem;
    constructor(private viewContainerRef: ViewContainerRef) { }

    ngOnChanges(changes: SimpleChanges) {
        this.maxPercent = this.value.reduce((maxVal, item) => Math.max(maxVal.value.replace('%', ''), item.value.replace('%', '')));
        if (this.maxPercent < 100) {
            this.maxPercent = 100;
        }
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
        if (this.value) {
            if (status !== 'new') {
                d3.select(this.elem).select('svg').remove();
            }
            const svg = d3.select(this.elem).select('.progress')
                .append('svg')
                .attr('height', 10)
                .style('width', '100%')
                .style('border-radius', '10px');

            const svgLagends = d3.select(this.elem).select('.lagends')
                .append('svg')
                .attr('height', 90)
                .attr('width', 400);


            const green = '#AEDB76';
            const red = '#F06667';
            const yellow = '#eff173';
            const orange = '#ffc35b';
            const darkGreen = '#6cab61';
            const blue = '#44739f';

            let fillColor;

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

            const totalWidth = parseInt(d3.select(this.elem).select('.progress').style('width').replace('px', ''));
            let xStartPoint = 0;
            this.value.forEach((data, iterator) => {

                const progress = svg.append('rect')
                    .attr('class', 'bg-rect')
                    .attr('fill', data.backgroundColor)
                    .attr('height', 10)
                    .attr('width', data.value)
                    .attr('rx', 8)
                    .attr('ry', 8)
                    .attr('x', xStartPoint);
                xStartPoint = (parseFloat(data.value.replace('%', '')) / 100) * totalWidth;

                svgLagends.append('rect')
                    .attr('fill', data.backgroundColor)
                    .attr('height', 10)
                    .attr('width', 10)
                    .attr('x', 40)
                    .attr('y', 40 + 15 * (iterator));

                svgLagends.append('text')
                    .attr('height', 10)
                    .attr('width', 100)
                    .attr('x', 55)
                    .attr('y', 50 + 15 * (iterator))
                    .html(data.data + ': ' + data.count + ' (' + data.value + ')');


                const tooltip = d3.select(this.elem).select('.progress')
                    .append('div')
                    .attr('class', 'tooltip')
                    .style('background-color', '#dedede')
                    .style('display', 'block');

                tooltip.append('div')
                    .attr('class', 'label');

                svg.append('rect')
                    .attr('class', 'progress-rect')
                    .attr('fill', fillColor)
                    .attr('height', 10)
                    .attr('width', 0)
                    .attr('x', 0);
                const updatedValue = data.data + ': ' + data.count + ' (' + data.value + ')';

                progress.on('mouseover', function(d) {
                    tooltip.select('.label').html(updatedValue);
                    tooltip.style('display', 'block');
                    tooltip.style('opacity', 1);
                    d3.select(this).style('opacity', 1);

                });

                progress.on('mousemove', function(event,d) {
                    tooltip.style('top', (event.layerY + 10) + 'px')
                        .style('left', (event.layerX - 25) + 'px');
                });

                progress.on('mouseout', function() {
                    tooltip.style('display', 'none');
                    tooltip.style('opacity', 0);
                    d3.select(this).style('opacity', 1);
                });

                progress.on('mouseenter', function(d) {
                    d3.select(this)
                        .attr('stroke', 'white')
                        .transition()
                        .duration(1000)
                        .attr('stroke-width', 2);
                });
            });

            // const tooltip = d3.select(this.elem).select('.progress')
            //     .append('div')
            //     .attr('class', 'tooltip')
            //     .style('background-color', '#dedede')
            //     .style('display', 'block');

            // tooltip.append('div')
            //     .attr('class', 'label');

            // const progress = svg.append('rect')
            //     .attr('class', 'progress-rect')
            //     .attr('fill', fillColor)
            //     .attr('height', 10)
            //     .attr('width', 0)
            //     .attr('x', 0);
            // const updatedValue = this.value;
            // progress.on('mouseover', function (d) {
            //     tooltip.select('.label').html('Completed: ' + updatedValue + ' %');
            //     tooltip.style('display', 'block');
            //     tooltip.style('opacity', 1);
            //     d3.select(this).style('opacity', 1);

            // });

            // progress.on('mousemove', function (d) {
            //     tooltip.style('top', (d3.event.layerY + 10) + 'px')
            //         .style('left', (d3.event.layerX - 25) + 'px');
            // });

            // progress.on('mouseout', function () {
            //     tooltip.style('display', 'none');
            //     tooltip.style('opacity', 0);
            //     d3.select(this).style('opacity', 1);
            // });
            // progress.on('mouseenter', function (d) {
            //     d3.select(this)
            //         .attr('stroke', 'white')
            //         .transition()
            //         .duration(1000)
            //         .attr('stroke-width', 2);
            // });
            // progress.on('mouseleave', function (d) {
            //     d3.select(this).transition()
            //         .attr('stroke', 'none');
            // });

            // progress.transition()
            //     .duration(1000)
            //     .attr('width', this.value + '%');
        }
    }






}






