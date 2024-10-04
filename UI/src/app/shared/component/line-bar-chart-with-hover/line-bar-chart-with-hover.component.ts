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
@author Rishabh

*******************************/

@Component({
	selector: 'app-line-bar-chart-with-hover',
	templateUrl: './line-bar-chart-with-hover.component.html',
	styleUrls: ['./line-bar-chart-with-hover.component.css'],
})
export class LineBarChartWithHowerComponent implements OnChanges, AfterViewInit {
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
	@Input() hoverValues: any;
	@Input() yCaption?: any;
	@Input() xCaption?: any;
	@Input() xLabelTransform: any;
	@Input() isKanban = false;
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
			// sort data according to date
			data = data.sort(this.compareDate);
			if (status !== 'new') {
				d3.select(this.elem).select('svg').remove();
			}

			// changing format to required format
			const newFormat = [];
			for (const obj1 in data) {
				const tempArray = [];
				tempArray.push(data[obj1].xLabel ? data[obj1].xLabel : data[obj1].date);
				tempArray.push(data[obj1].count);
				tempArray.push(data[obj1].howerValue);
				if (data[obj1].barCount) {
					tempArray.push({ barCount: data[obj1].barCount });
				}
				if (data[obj1].barNo) {
					tempArray.push({ barNo: data[obj1].barNo });
				}

				newFormat.push(tempArray);
			}

			data = newFormat;

			const max = d3.max(data, function(d) {
				return d[1];
			});

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
				left: 15 + marginOnBasisOfMax,
			};
				const width = this.width;
			let height = 200 - 25 - 25;

			// if (this.lineChart === 'true' && this.barChart === 'true') {
			height -= 10;
			margin.top += 20;
			// }
			// defining scale x
			const xScale = d3
				.scaleBand()
				.rangeRound([0, width - margin.left - margin.right])
				.padding(0.6)
				.domain(
					data.map(function(d) {
						return d[0];
					})
				);
			// defining scale y
			const yScale = d3
				.scaleLinear()
				.rangeRound([height, 0])
				.domain([
					0, this.maxValue && this.maxValue > 0
						? this.maxValue
						: d3.max(data, function(d) {
							return parseInt(d[1]);
						}),
				]);


			const svg = d3
				.select(this.elem)
				.select('.lineWithBar')
				.append('svg')
				.attr('width', width + 'px')
				.attr('height', '225px');
			const colors = ['#009688', '#3f51b5'];

			const grad = svg.append('defs').append('linearGradient').attr('id', 'grad').attr('x1', '0%').attr('x2', '0%').attr('y1', '0%').attr('y2', '100%');

			grad
				.selectAll('stop')
				.data(colors)
				.enter()
				.append('stop')
				.style('stop-color', function(d) {
					return d;
				})
				.attr('offset', function(d, i) {
					return 100 * (i / (colors.length - 1)) + '%';
				});

			const g = svg
				.append('g')
				.attr('transform', 'translate(' + margin.left + ',' + margin.top + ')')
				.attr('width', width + 'px')
				.attr('height', '220px');

			const translateX = height + 40;
			// x-axis
			const XCaption = svg
				.append('g')
				.attr('class', 'axis axis--x')
				.attr('transform', 'translate(20,' + translateX + ')')
				.call(d3.axisBottom(xScale))
				.selectAll('text')
				.style('text-anchor', 'end')
				.attr('dx', '-.8em')
				.attr('dy', '.15em')
				.attr('transform', `rotate(${this.xLabelTransform})`);

			if (this.xCaption) {
				svg
					.append('text')
					.attr('transform', 'translate(700,' + (translateX - 10) + ')')
					.style('text-anchor', 'end')
					.attr('dx', '-.8em')
					.attr('dy', '.15em')
					.attr('font-family', 'sans-serif')
					.attr('font-size', '11px')
					.attr('fill', 'black')
					.text(this.xCaption);
			}

			// axis-y
			g.append('g').attr('class', 'axis axis--y').call(d3.axisLeft(yScale).ticks(5, 'f').tickSize(-width));

			if (this.yCaption) {
				const yAxisLabel = svg.append('g').attr('transform', 'translate(30,8)');

				yAxisLabel.append('text').style('text-anchor', 'end').attr('dx', '-.8em').attr('dy', '.15em').attr('font-family', 'sans-serif').attr('font-size', '11px').attr('fill', 'black').attr('transform', `rotate(-90)`).text(this.yCaption);
			}

			const bar = g.selectAll('rect').data(data).enter().append('g');

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
				const tooltip = d3.select(this.elem).select('.lineWithBar').append('div').attr('class', 'tooltip').style('display', 'none');
				const self = this;

				bar
					.append('rect')
					.attr('x', function(d) {
						let xScaleValue = xScale(d[0]);
						if (d[3].barCount == 2 && d[4].barNo == 1) {
							xScaleValue = xScale(d[0]) - xScale.bandwidth() / 2;
						}
						if (d[3].barCount == 2 && d[4].barNo == 2) {
							xScaleValue = xScale(d[0]) + xScale.bandwidth() / 2;
						}
						return xScaleValue;
					})
					.attr('width', xScale.bandwidth())
					.attr('y', function(d) {
						return height;
					})
					.style('fill', function(d) {
						let barColor = fillColor;
						if (d[3].barCount == 2 && d[4].barNo == 1) {
							barColor = fillColor;
						}
						if (d[3].barCount == 2 && d[4].barNo == 2) {
							barColor = '#00a13c';
						}
						return barColor;
					})
					.on('mouseover', function(event,d) {
						if (d[2] && d[2][d[0]]) {
							tooltip.transition().duration(200).style('display', 'block').style('opacity', 0.9);
							tooltip
								.html(self.stringify(d[2][d[0]]).split(',').join('<br/>'))
								.style('left', (event.layerX - 25 + 100 > width ? event.layerX - 100 : event.layerX - 25) + 'px')
								.style('top', event.layerY + 10 + 'px');
						}
					})
					.on('mouseout', function(d) {
						tooltip.transition().duration(500).style('display', 'none').style('opacity', 0);
					})
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
				bar
					.append('text')
					.attr('dy', '1.3em')
					.attr('x', function(d) {
						let xScaleValue = xScale(d[0]) + xScale.bandwidth() / 2;
						if (d[3].barCount == 2 && d[4].barNo == 1 && d[1] > 0) {
							xScaleValue = xScale(d[0]) - xScale.bandwidth() / 2 + 15;
						}
						if (d[3].barCount == 2 && d[4].barNo == 2 && d[1] > 0) {
							xScaleValue = xScale(d[0]) + xScale.bandwidth() / 2 + 15;
						}
						return xScaleValue;
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
							return d[1];
						}
					});

				if (this.targetValue) {
					svg
						.append('line')
						.attr('x1', 0)
						.attr('x2', width)
						.attr('y1', yScale(this.targetValue))
						.attr('y2', yScale(this.targetValue))
						.attr('transform', 'translate(' + margin.left + ',' + margin.top + ')')
						.attr('stroke-width', 1)
						.attr('stroke', 'black')
						.attr('stroke-dasharray', '4,4');
				}

				const color = ['#44739f', '#00a13c'];

				const legend = svg
					.selectAll('.legend')
					.data(['Commit', 'MR']) //hard coding the labels as the datset may have or may not have but legend should be complete.
					.enter()
					.append('g')
					.attr('class', 'legend')
					.attr('transform', function(d, i) {
						return 'translate(0,' + i * 20 + ')';
					});

				// draw legend colored rectangles
				if (!this.isKanban) {
					legend
						.append('rect')
						.attr('x', width - 18)
						.attr('y', 22)
						.attr('width', 18)
						.attr('height', 18)
						.style('fill', function(d, i) {
							return color[i];
						});

					// draw legend text
					legend
						.append('text')
						.attr('x', width - 24)
						.attr('y', 30)
						.attr('dy', '.35em')
						.style('text-anchor', 'end')
						.text(function(d) {
							return d;
						});
				}
			}

			if (this.lineChart === 'true') {
				// line chart
				const line = d3
					.line()
					.x(function(d, i) {
						return xScale(d[0]) + xScale.bandwidth() / 2;
					})
					.y(function(d) {
						return yScale(d[1]);
					});

				bar
					.append('path')
					.attr('class', 'line') // Assign a class for styling
					.style('fill', 'none')
					.style('stroke', 'var(--color-black)')
					.style('stroke-width', '2px')
					.style('shape-rendering', 'geometricPrecision')
					.attr('d', line(data)); // Calls the line generator

				const tooltip = d3.select(this.elem).select('.lineWithBar').append('div').attr('class', 'tooltip').style('display', 'none');

				const self = this;
				if (!this.isKanban) {
					bar
						.append('circle') // Uses the enter().append() method
						.attr('class', 'dot') // Assign a class for styling
						.attr('cx', function(d, i) {
							return xScale(d[0]) + xScale.bandwidth() / 2;
						})
						.attr('cy', function(d) {
							return yScale(d[1]);
						})
						.attr('r', 5)
						.on('mouseover', function(event,d) {
							if (d[2] && d[2][d[0]]) {
								tooltip.transition().duration(200).style('display', 'block').style('opacity', 0.9);
								tooltip
									.html(self.stringify(d[2][d[0]]).split(',').join('<br/>'))
									.style('left', (event.layerX - 25 + 100 > width ? event.layerX - 100 : event.layerX - 25) + 'px')
									.style('top', event.layerY + 10 + 'px');
							}
						})
						.on('mouseout', function(d) {
							tooltip.transition().duration(500).style('display', 'none').style('opacity', 0);
						});
				}
				if (this.barChart === 'false') {
					bar
						.append('text')
						.attr('dy', '-0.5em')
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

	formatDate(date_string) {
		if (date_string) {
			let date_components;
			if (date_string.indexOf('/') !== -1) {
				date_components = date_string?.split('/');
			} else {
				date_components = date_string?.split('-');
			}
			if (!this.isKanban) {
				var day = date_components[0];
				var month = date_components[1];
				var year = date_components[2];
			} else {
				var year = date_components[0];
				var month = date_components[1];
				var day = date_components[2];
			}
			return new Date(year, month - 1, day);
		}
	}

	compareDate = (a, b) => {
		if (this.formatDate(a.date) < this.formatDate(b.date)) {
			return -1;
		}
		if (this.formatDate(a.date) > this.formatDate(b.date)) {
			return 1;
		}
		return 0;
	};

	stringify(data) {
		if (data) {
			data = JSON.stringify(data);
			data = data.replaceAll('{', '').replaceAll('}', '').replaceAll('"', '').replaceAll(':', ' : ');
			return data;
		} else {
			return '';
		}
	}

	/*appendHoverValue(data) {
		data.forEach(element => {
			element['howerValue'] = {};
			element['howerValue'][element['date']] = '';
			this.hoverValues.forEach(howerValue => {
				element['howerValue'][element['date']] += howerValue.value[0].sprojectName + '  : ' + (howerValue.value[0].howerValue[element['date']] ? howerValue.value[0].howerValue[element['date']] : '0') + '  ; ';
			});
		});
		return data;
	}*/
}
