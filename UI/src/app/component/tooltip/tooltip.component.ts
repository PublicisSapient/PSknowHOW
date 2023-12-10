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

import { Component, ElementRef, Input, OnChanges, OnInit, SimpleChanges, ViewChild, ViewContainerRef } from '@angular/core';

@Component({
    selector: 'app-tooltip',
    templateUrl: './tooltip.component.html',
    styleUrls: ['./tooltip.component.css']
})
export class TooltipComponent implements OnChanges {
    @Input() data: any = {};
    @Input() showChartView = 'chart';
    @Input() filterNo?: string = '';
    @Input() kpiName;
    @Input() showingMaturityRange: boolean = false;
    @Input() toolTipTop = 0;
    relativeTooltipTop = 400;
    show: boolean = true;
    bottomArrow: boolean = false;


    constructor(private elementRef: ElementRef) {
    }

    ngOnChanges(changes: SimpleChanges) {
        this.show = true;
    }

    hideTooltip(event) {
        if (this.showingMaturityRange) {
            this.show = false;
        }
    }

    ngAfterViewInit() {
        const element = this.elementRef.nativeElement.querySelector('.tooltip-wrapper');
        const rect = element.getBoundingClientRect();
        const bottomVisible = rect.bottom <= window.innerHeight;
        if (!bottomVisible) {
            setTimeout(() => {
                this.relativeTooltipTop -= (400 + rect.height + 25);
                this.show = true;
                this.bottomArrow = true;
            }, 0);
        } else {
            setTimeout(() => {
                this.relativeTooltipTop = 400;
                this.show = true;
                this.bottomArrow = false;
            }, 0);
        }
    }
}

