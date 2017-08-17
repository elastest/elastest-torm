import { TooltipAreaComponent } from './tooltip-area.component';
import { Subject } from 'rxjs/Rx';
import {
  BaseChartComponent,
  calculateViewDimensions,
  ColorHelper,
  LineComponent,
  LineSeriesComponent,
  NgxChartsModule,
  Timeline,
  ViewDimensions,
} from '@swimlane/ngx-charts/release';

import {
  Component,
  Input,
  ViewEncapsulation,
  Output,
  EventEmitter,
  ChangeDetectionStrategy,
  ViewChild,
  HostListener,
  OnInit,
  OnChanges,
  ContentChild,
  TemplateRef,
} from '@angular/core';
import { PathLocationStrategy } from '@angular/common';
import {
  trigger,
  state,
  style,
  animate,
  transition,
} from '@angular/animations';

import { area, line, curveLinear } from 'd3-shape';
import { scaleBand, scaleLinear, scalePoint, scaleTime } from 'd3-scale';
import { id } from '@swimlane/ngx-charts/release/utils/id';


@Component({
  selector: 'combo-chart-component',
  templateUrl: './combo-chart.component.html',
  styleUrls: ['./combo-chart.component.scss'],
  encapsulation: ViewEncapsulation.None,
  animations: [
    trigger('animationState', [
      transition(':leave', [
        style({
          opacity: 1,
        }),
        animate(500, style({
          opacity: 0
        }))
      ])
    ])
  ]
})
export class ComboChartComponent extends BaseChartComponent {

  @ViewChild(LineSeriesComponent) lineSeriesComponent: LineSeriesComponent;
  @ViewChild('timeline') timelineObj: Timeline;
  @ViewChild('tooltip') tooltipObj: TooltipAreaComponent;


  @Input() curve: any = curveLinear;
  @Input() legend = false;
  @Input() legendTitle: string = 'Legend';
  @Input() xAxis;
  @Input() yAxis;
  @Input() timeline;
  @Input() showXAxisLabel;
  @Input() showYAxisLabel;
  @Input() showRightOneYAxisLabel;
  @Input() showRightTwoYAxisLabel;
  @Input() xAxisLabel;
  @Input() yAxisLabel;
  @Input() yAxisLabelRightOne;
  @Input() yAxisLabelRightTwo;
  @Input() tooltipDisabled: boolean = false;
  @Input() gradient: boolean;
  @Input() showGridLines: boolean = true;
  @Input() activeEntries: any[] = [];
  @Input() schemeType: string;
  @Input() xAxisTickFormatting: any;
  @Input() yLeftAxisTickFormatting: any;
  @Input() yRightOneAxisTickFormatting: any;
  @Input() yRightTwoAxisTickFormatting: any;
  @Input() roundDomains: boolean = false;
  @Input() colorSchemeLine: any[];
  @Input() scheme: any[];
  @Input() autoScale: boolean;

  @Input() leftChart: any;
  @Input() rightChartOne: any;
  @Input() rightChartTwo: any;

  @Input() yLeftAxisScaleFactor: any;
  @Input() yRightAxisScaleFactor: any;
  @Input() rangeFillOpacity: number;

  @Output() activate: EventEmitter<any> = new EventEmitter();
  @Output() deactivate: EventEmitter<any> = new EventEmitter();

  @ContentChild('tooltipTemplate') tooltipTemplate: TemplateRef<any>;
  @ContentChild('seriesTooltipTemplate') seriesTooltipTemplate: TemplateRef<any>;

  dims: ViewDimensions;
  dimsRightTwo: ViewDimensions;
  yScale: any;
  xDomain: any;
  yDomain: any;
  transform: string;
  transformRightTwo: string;
  clipPath: string;
  clipPathId: string;
  colors: ColorHelper;
  margin: any[] = [10, 20, 10, 20];
  xAxisHeight: number = 0;
  yAxisWidth: number = 0;
  legendOptions: any;
  scaleType = 'time';
  xScale;
  yScaleRightOne;
  yScaleRightTwo;
  yDomainRightOne;
  yDomainRightTwo;
  seriesDomain;
  scaledAxis;
  combinedSeries;
  xSet;
  filteredDomain;
  hoveredVertical;
  yOrientLeft = 'left';
  yOrientRight = 'right';
  legendSpacing = 0;
  rightTwoSpacing = 95;
  bandwidth;
  barPadding = 8;

  showYAxisLabelDefault: boolean;
  showRightOneYAxisLabelDefault: boolean;
  showRightTwoYAxisLabelDefault: boolean;

  hasRange: boolean; // whether the line has a min-max range around it
  timelineWidth: any;
  timelineHeight: number = 50;
  timelineXScale: any;
  timelineYScale: any;
  timelineYScaleRightOne: any;
  timelineYScaleRightTwo: any;
  timelineXDomain: any;
  timelineTransform: any;
  timelinePadding: number = 10;

  // TimeLine Observable
  _timelineObs = new Subject<any>();
  timelineObs = this._timelineObs.asObservable();

  // Hover and leave Observable for tooltip
  _hoverObs = new Subject<any>();
  hoverObs = this._hoverObs.asObservable();

  _leaveObs = new Subject<any>();
  leaveObs = this._leaveObs.asObservable();

  // Functions

  initDimensions() {
    this.dims = this.initSingleDimensions(0)
    this.dimsRightTwo = this.initSingleDimensions(this.rightTwoSpacing);
  }

  initSingleDimensions(spacing: number) {
    return calculateViewDimensions({
      width: this.width - this.legendSpacing + spacing,
      height: this.height,
      margins: this.margin,
      showXAxis: this.xAxis,
      showYAxis: this.yAxis,
      xAxisHeight: this.xAxisHeight,
      yAxisWidth: this.yAxisWidth,
      showXLabel: this.showXAxisLabel,
      showYLabel: this.showYAxisLabel,
      showLegend: this.legend,
      legendType: this.schemeType
    });
  }

  initLegendSpace() {
    if (this.yAxis) {
      if (this.leftChart.length > 0) {
        if (this.rightChartTwo.length > 0) {
          if (this.rightChartOne.length > 0) {
            this.legendSpacing = 195;
          } else {
            this.legendSpacing = 195;
          }
        } else {
          if (this.rightChartOne.length > 0) {
            this.legendSpacing = 70;

          } else {
            this.legendSpacing = 40;
          }
        }

      } else {
        if (this.rightChartTwo.length > 0) {
          this.legendSpacing = 200;
        } else {
          this.legendSpacing = 90;
        }
      }
    } else {
      this.legendSpacing = 0;
    }
  }

  update(): void {
    super.update();
    this.showLabelsDefault();

    this.initLegendSpace();
    this.initDimensions();

    if (this.timeline) {
      this.dims.height -= (this.timelineHeight + this.margin[2] + this.timelinePadding);
    }

    this.seriesDomain = this.getSeriesDomain();

    // X axis
    this.xDomain = this.getXDomain();
    if (this.filteredDomain) {
      this.xDomain = this.filteredDomain;
    }
    this.xScale = this.getXScale(this.xDomain, this.dims.width);

    // line chart right
    this.yDomainRightOne = this.getYDomainRightOne();
    this.yDomainRightTwo = this.getYDomainRightTwo();
    this.yScaleRightOne = this.getYScaleRight(this.yDomainRightOne, this.dims.height);
    this.yScaleRightTwo = this.getYScaleRight(this.yDomainRightTwo, this.dims.height);

    // line chart left
    this.yScale = this.getYScale();

    this.updateTimeline();

    this.setColors();
    this.legendOptions = this.getLegendOptions();

    this.transform = `translate(${this.dims.xOffset} , ${this.margin[0]})`;
    this.transformRightTwo = `translate(${this.dims.xOffset} , ${this.margin[0]})`;

    const pageUrl = this.location instanceof PathLocationStrategy
      ? this.location.path()
      : '';

    this.clipPathId = 'clip' + id().toString();
    this.clipPath = `url(${pageUrl}#${this.clipPathId})`;

    this.hideLabels();
  }

  showLabelsDefault() {
    if (this.showYAxisLabelDefault === undefined) {
      this.showYAxisLabelDefault = this.showYAxisLabel;
    }
    if (this.showRightOneYAxisLabelDefault === undefined) {
      this.showRightOneYAxisLabelDefault = this.showRightOneYAxisLabel;
    }
    if (this.showRightTwoYAxisLabelDefault === undefined) {
      this.showRightTwoYAxisLabelDefault = this.showRightTwoYAxisLabel;
    }
  }

  hideLabels() {
    this.showYAxisLabel = this.showYAxisLabelDefault && this.leftChart.length > 0;
    this.showRightOneYAxisLabel = this.showRightOneYAxisLabelDefault && this.rightChartOne.length > 0;
    this.showRightTwoYAxisLabel = this.showRightTwoYAxisLabelDefault && this.rightChartTwo.length > 0;
  }

  // line scale

  deactivateAll() {
    this.activeEntries = [...this.activeEntries];
    for (const entry of this.activeEntries) {
      this.deactivate.emit({ value: entry, entries: [] });
    }
    this.activeEntries = [];
  }

  @HostListener('mouseleave')
  hideCircles(): void {
    this.hideCirclesAux();
    this._leaveObs.next('');
  }

  hideCirclesAux(): void {
    this.hoveredVertical = null;
    this.deactivateAll();
    this.tooltipObj.hideTooltip();
  }


  updateHoveredVertical(item): void {
    this.updateHoveredVerticalAux(item);
    if (item.disableObservable !== undefined && !item.disableObservable) {
      this._hoverObs.next(item);
    }
  }

  updateHoveredVerticalAux(item): void {
    this.hoveredVertical = item.value;
    this.deactivateAll();
  }

  updateDomain(domain): void {
    this._timelineObs.next(domain);
    this.updateDomainAux(domain);
  }

  updateDomainAux(domain): void {
    this.filteredDomain = domain;
    this.xDomain = this.filteredDomain;
    this.xScale = this.getXScale(this.xDomain, this.dims.width);
  }

  // Legend
  getSeriesDomain(): any[] {
    this.combinedSeries = this.rightChartOne.concat(this.leftChart).concat(this.rightChartTwo).slice(0);
    return this.combinedSeries.map(d => d.name);
  }

  isDate(value): boolean {
    if (value instanceof Date) {
      return true;
    }

    return false;
  }

  getScaleType(values): string {
    let date = true;
    let num = true;

    for (const value of values) {
      if (!this.isDate(value)) {
        date = false;
      }

      if (typeof value !== 'number') {
        num = false;
      }
    }

    if (date) return 'time';
    if (num) return 'linear';
    return 'ordinal';
  }

  getXDomain(): any[] {
    return this.getXDomainByChart(this.combinedSeries);
  }

  getXDomainByChart(chart: any) {
    let values = [];

    for (const results of chart) {
      for (const d of results.series) {
        if (!values.includes(d.name)) {
          values.push(d.name);
        }
      }
    }

    this.scaleType = this.getScaleType(values);
    let domain = [];

    if (this.scaleType === 'time') {
      const min = Math.min(...values);
      const max = Math.max(...values);
      domain = [new Date(min), new Date(max)];
      this.xSet = [...values].sort((a, b) => {
        const aDate = a.getTime();
        const bDate = b.getTime();
        if (aDate > bDate) return 1;
        if (bDate > aDate) return -1;
        return 0;
      });
    } else if (this.scaleType === 'linear') {
      values = values.map(v => Number(v));
      const min = Math.min(...values);
      const max = Math.max(...values);
      domain = [min, max];
      this.xSet = [...values].sort();
    } else {
      domain = values;
      this.xSet = values;
    }

    this.xSet = values;
    return domain;
  }

  getYDomainRightOne() {
    return this.getYDomainRight(this.rightChartOne);
  }

  getYDomainRightTwo() {
    return this.getYDomainRight(this.rightChartTwo);
  }

  getYDomainRight(chart: any): any[] {
    const domain = [];

    for (const results of chart) {
      for (const d of results.series) {
        if (domain.indexOf(d.value) < 0) {
          domain.push(d.value);
        }
        if (d.min !== undefined) {
          if (domain.indexOf(d.min) < 0) {
            domain.push(d.min);
          }
        }
        if (d.max !== undefined) {
          if (domain.indexOf(d.max) < 0) {
            domain.push(d.max);
          }
        }
      }
    }

    let min = Math.min(...domain);
    const max = Math.max(...domain);
    if (this.yRightAxisScaleFactor) {
      const minMax = this.yRightAxisScaleFactor(min, max);
      return [Math.min(0, minMax.min), minMax.max];
    } else {
      min = Math.min(0, min);
      return [min, max];
    }
  }

  getXScale(domain, width): any {
    let scale;
    if (this.bandwidth === undefined) {
      this.bandwidth = (this.dims.width - this.barPadding);
    }

    if (this.scaleType === 'time') {
      scale = scaleTime()
        .range([0, width])
        .domain(domain);
    } else if (this.scaleType === 'linear') {
      scale = scaleLinear()
        .range([0, width])
        .domain(domain);

      if (this.roundDomains) {
        scale = scale.nice();
      }
    } else if (this.scaleType === 'ordinal') {
      scale = scalePoint()
        .range([this.bandwidth / 2, width - this.bandwidth / 2])
        .domain(domain);
    }

    return scale;
  }

  getYScaleRight(domain, height): any {
    const scale = scaleLinear()
      .range([height, 0])
      .domain(domain);

    return this.roundDomains ? scale.nice() : scale;
  }

  // vertical scales

  getYScale(): any {
    this.yDomain = this.getYDomain();
    const scale = scaleLinear()
      .range([this.dims.height, 0])
      .domain(this.yDomain);
    return this.roundDomains ? scale.nice() : scale;
  }

  getYDomain() {
    let values: any;
    let max: number;
    let min: number;

    for (const result of this.leftChart.slice(0)) {
      values = result.series.map(d => d.value);
      const resultMin: number = Math.min(0, ...values);
      if (min === undefined) {
        min = resultMin;
      }
      min = Math.min(...[min, resultMin]);

      const resultMax: number = Math.max(0, ...values);
      if (max === undefined) {
        max = resultMax;
      }
      max = Math.max(...[max, resultMax]);
    }

    if (this.yLeftAxisScaleFactor) {
      const minMax = this.yLeftAxisScaleFactor(min, max);
      return [Math.min(0, minMax.min), minMax.max];
    } else {
      return [min, max];
    }
  }

  onClick(data) {
    this.select.emit(data);
  }

  setColors(): void {
    let domain;
    if (this.schemeType === 'ordinal') {
      domain = this.xDomain;
    } else {
      domain = this.yDomain;
    }
    this.colors = new ColorHelper(this.scheme, this.schemeType, domain, this.customColors);
  }

  getLegendOptions() {
    const opts = {
      scaleType: this.schemeType,
      colors: undefined,
      domain: [],
      title: undefined
    };
    if (opts.scaleType === 'ordinal') {
      opts.domain = this.seriesDomain;
      opts.colors = this.colors;
      opts.title = this.legendTitle;
    } else {
      opts.domain = this.seriesDomain;
      opts.colors = this.colors.scale;
    }
    return opts;
  }

  updateLineWidth(width): void {
    this.bandwidth = width;
  }

  updateYAxisWidth({ width }): void {
    this.yAxisWidth = width + 20;
    this.update();
  }

  updateXAxisHeight({ height }): void {
    this.xAxisHeight = height;
    this.update();
  }

  onActivate(item) {
    const idx = this.activeEntries.findIndex(d => {
      return d.name === item.name && d.value === item.value && d.series === item.series;
    });
    if (idx > -1) {
      return;
    }

    this.activeEntries = [item, ...this.activeEntries];
    this.activate.emit({ value: item, entries: this.activeEntries });
  }

  onDeactivate(item) {
    const idx = this.activeEntries.findIndex(d => {
      return d.name === item.name && d.value === item.value && d.series === item.series;
    });

    this.activeEntries.splice(idx, 1);
    this.activeEntries = [...this.activeEntries];

    this.deactivate.emit({ value: item, entries: this.activeEntries });
  }

  updateTimeline(): void {
    if (this.timeline) {
      this.timelineWidth = this.dims.width;
      this.timelineXDomain = this.getXDomain();
      this.timelineXScale = this.getXScale(this.timelineXDomain, this.timelineWidth);

      this.timelineYScale = this.getYScaleRight(this.yDomain, this.timelineHeight);
      this.timelineYScaleRightOne = this.getYScaleRight(this.yDomainRightOne, this.timelineHeight);
      this.timelineYScaleRightTwo = this.getYScaleRight(this.yDomainRightTwo, this.timelineHeight);
      this.timelineTransform = `translate(${this.dims.xOffset}, ${-this.margin[2]})`;
    }
  }

  trackBy(index, item): string {
    return item.name;
  }
}
