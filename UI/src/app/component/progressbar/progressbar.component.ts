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

import { Component, Input, OnChanges, OnInit, SimpleChanges, ViewContainerRef } from '@angular/core';


import * as d3 from 'd3';


@Component({
    selector: 'app-progressbar',
    templateUrl: './progressbar.component.html',
    styleUrls: ['./progressbar.component.css']
})
export class ProgressbarComponent implements OnChanges,OnInit {
    @Input() value: string;
    @Input() maxValue: string;
    elem;
    percentage: number;

    constructor(private viewContainerRef: ViewContainerRef) { }

    ngOnInit(): void {
        if(+this.value ==0 && +this.maxValue ==0){
            this.percentage=0;
        }else{
            this.percentage =(+this.value / +this.maxValue) *100;
        }
    }

    ngOnChanges(changes: SimpleChanges) {
        // only run when property "data" changed

        if (changes['value']) {
            this.percentage = (+this.value / +this.maxValue) *100;
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

            const red = '#F06667';
            const fillColor=red;

            svg.append('rect')
                .attr('class', 'bg-rect')
                .attr('fill', '#f2f2f2')
                .attr('height', 10)
                .attr('width', '100%')
                .attr('rx', 8)
                .attr('ry', 8)
                .attr('x', 0);

            const progress = svg.append('rect')
                .attr('class', 'progress-rect')
                .attr('fill', fillColor)
                .attr('height', 10)
                .attr('width', 0)
                .attr('x', 0);

            progress.on('mouseover', function(d) {
                d3.select(this).style('opacity', 1);
            });

            progress.on('mousemove', function(d) {
            });

            progress.on('mouseout', function() {
                d3.select(this).style('opacity', 1);
            });
            progress.on('mouseenter', function(d) {
                d3.select(this)
                    .attr('stroke', 'white')
                    .transition()
                    .duration(1000)
                    .attr('stroke-width', 2);
            });
            progress.on('mouseleave', function(d) {
                d3.select(this).transition()
                    .attr('stroke', 'none');
            });

            progress.transition()
                .duration(1000)
                .attr('width', this.percentage + '%');
        }
    }






}






