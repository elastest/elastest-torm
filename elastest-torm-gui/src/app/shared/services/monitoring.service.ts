import { Observable, Subject } from 'rxjs/Rx';
import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse, HttpHeaders } from '@angular/common/http';
import { ConfigurationService } from '../../config/configuration-service.service';
import { MonitoringQueryModel } from '../monitoring-query.model';
import { PopupService } from './popup.service';
import { MetricsFieldModel } from '../metrics-view/metrics-chart-card/models/metrics-field-model';
import { LineChartMetricModel } from '../metrics-view/models/linechart-metric-model';
import { SingleMetricModel } from '../metrics-view/models/single-metric-model';
import {
  MetricbeatType,
  isMetricFieldGroup,
  getMetricBeatFieldGroupList,
  MetricFieldGroupModel,
  metricFieldGroupList,
  AllMetricsFields,
  Units,
} from '../metrics-view/metrics-chart-card/models/all-metrics-fields-model';
import { defaultStreamMap } from '../defaultESData-model';
import { ESRabComplexMetricsModel } from '../metrics-view/metrics-chart-card/models/es-rab-complex-metrics-model';
import { ESRabLogModel } from '../logs-view/models/es-rab-log-model';
import { MetricsDataType } from '../metrics-view/models/et-res-metrics-model';
import { TJobExecModel } from '../../elastest-etm/tjob-exec/tjobExec-model';
import { LogAnalyzerQueryModel } from '../loganalyzer-query.model';
import { AbstractTJobExecModel } from '../../elastest-etm/models/abstract-tjob-exec-model';
import { MonitorMarkModel } from '../../elastest-etm/etm-monitoring-view/monitor-mark.model';
import { comparisonMode, viewMode } from '../../elastest-log-comparator/model/log-comparison.model';
import { isString } from '../utils';
import { isNumber } from 'util';

@Injectable()
export class MonitoringService {
  etmApiUrl: string;
  allMetricsFields: AllMetricsFields = new AllMetricsFields(); // Object with a list of all metrics
  metricbeatFieldGroupList: MetricFieldGroupModel[];

  constructor(public http: HttpClient, private configurationService: ConfigurationService, public popupService: PopupService) {
    this.etmApiUrl = this.configurationService.configModel.hostApi;
    this.metricbeatFieldGroupList = getMetricBeatFieldGroupList();
  }

  /* ******************************************** */
  /* ***************** API REST ***************** */
  /* ******************************************** */

  public searchAllByTerms(query: MonitoringQueryModel): Observable<any> {
    let url: string = this.etmApiUrl + '/monitoring/byterms';
    return this.http
      .post(url, query, { observe: 'response' })
      .map((response: HttpResponse<any>) => this.parseETMiniTracesIfNecessary(response.body));
  }

  /* *** Logs *** */

  public searchAllLogs(query: MonitoringQueryModel): Observable<any> {
    let url: string = this.etmApiUrl + '/monitoring/log';
    return this.http
      .post(url, query, { observe: 'response' })
      .map((response: HttpResponse<any>) => this.parseETMiniTracesIfNecessary(response.body));
  }

  public searchPreviousLogs(query: MonitoringQueryModel): Observable<any> {
    let url: string = this.etmApiUrl + '/monitoring/log/previous';
    return this.http
      .post(url, query, { observe: 'response' })
      .map((response: HttpResponse<any>) => this.parseETMiniTracesIfNecessary(response.body));
  }

  public searchLastLogs(query: MonitoringQueryModel, size: number): Observable<any> {
    let url: string = this.etmApiUrl + '/monitoring/log/last/' + size;
    return this.http
      .post(url, query, { observe: 'response' })
      .map((response: HttpResponse<any>) => this.parseETMiniTracesIfNecessary(response.body));
  }

  public searchLogsTree(query: MonitoringQueryModel): Observable<any> {
    let url: string = this.etmApiUrl + '/monitoring/log/tree';
    return this.http
      .post(url, query, { observe: 'response' })
      .map((response: HttpResponse<any>) => this.parseETMiniTracesIfNecessary(response.body));
  }

  public searchLogsLevelsTree(query: MonitoringQueryModel): Observable<any> {
    let url: string = this.etmApiUrl + '/monitoring/log/tree/levels';
    return this.http
      .post(url, query, { observe: 'response' })
      .map((response: HttpResponse<any>) => this.parseETMiniTracesIfNecessary(response.body));
  }

  public compareLogsPairByQuery(
    query: MonitoringQueryModel,
    comparison: comparisonMode = 'notimestamp',
    view: viewMode = 'complete',
    diffTimeout: number = 0,
  ): Observable<string> {
    let url: string =
      this.etmApiUrl + '/monitoring/log/compare?comparison=' + comparison + '&view=' + view + '&timeout=' + diffTimeout;

    return (
      this.http
        .post(url, query, { responseType: 'text' })
        // .pipe(timeout(360000))
        .map((data: string) => data)
    );
  }

  public compareLogsPairAsyncByQuery(
    query: MonitoringQueryModel,
    comparison: comparisonMode = 'notimestamp',
    view: viewMode = 'complete',
    diffTimeout: number = 0,
  ): Observable<string> {
    let url: string =
      this.etmApiUrl + '/monitoring/log/compare/async?comparison=' + comparison + '&view=' + view + '&timeout=' + diffTimeout;

    // Returns processId
    return this.http.post(url, query, { responseType: 'text' }).map((data: string) => data);
  }

  public getComparisonByProcessId(processId: string): Observable<string> {
    let url: string = this.etmApiUrl + '/monitoring/log/compare/' + processId;
    // Returns comparison or null
    return this.http.get(url, { responseType: 'text' });
  }

  /**
   * Gets a Log Comparison of a Pair of execs
   *
   * @return a `String` with the comparison if is not asyc or a `String` with processId if async.
   */
  compareLogsPair(
    async: boolean = false,
    pair: string[],
    stream: string,
    components: string[],
    from: Date = undefined,
    to: Date = undefined,
    includedFrom: boolean = true,
    includedTo: boolean = true,
    comparison: comparisonMode = 'notimestamp',
    view: viewMode = 'complete',
    diffTimeout: number = 0,
  ): Observable<string> {
    let query: MonitoringQueryModel = new MonitoringQueryModel();
    query.indices = pair;
    query.stream = stream;
    query.components = components;
    query.setTimeRange(from, to, includedFrom, includedTo);
    query.selectedTerms.push('stream', 'component');

    if (async) {
      // Returns processId
      return this.compareLogsPairAsyncByQuery(query, comparison, view, diffTimeout);
    } else {
      // Returns comparison
      return this.compareLogsPairByQuery(query, comparison, view, diffTimeout);
    }
  }

  /* *** Metrics *** */

  public searchAllMetrics(query: MonitoringQueryModel): Observable<any> {
    let url: string = this.etmApiUrl + '/monitoring/metric';
    return this.http
      .post(url, query, { observe: 'response' })
      .map((response: HttpResponse<any>) => this.parseETMiniTracesIfNecessary(response.body));
  }

  public searchPreviousMetrics(query: MonitoringQueryModel): Observable<any> {
    let url: string = this.etmApiUrl + '/monitoring/metric/previous';
    return this.http
      .post(url, query, { observe: 'response' })
      .map((response: HttpResponse<any>) => this.parseETMiniTracesIfNecessary(response.body));
  }

  public searchLastMetrics(query: MonitoringQueryModel, size: number): Observable<any[]> {
    let url: string = this.etmApiUrl + '/monitoring/metric/last/' + size;
    return this.http
      .post(url, query, { observe: 'response' })
      .map((response: HttpResponse<any[]>) => this.parseETMiniTracesIfNecessary(response.body));
  }

  public searchMetricsTree(query: MonitoringQueryModel): Observable<any> {
    let url: string = this.etmApiUrl + '/monitoring/metric/tree';
    return this.http
      .post(url, query, { observe: 'response' })
      .map((response: HttpResponse<any>) => this.parseETMiniTracesIfNecessary(response.body));
  }

  /* *** LogAnalyzer *** */

  public searchLogAnalyzerQuery(query: LogAnalyzerQueryModel): Observable<any> {
    let url: string = this.etmApiUrl + '/monitoring/loganalyzer';
    return this.http
      .post(url, query, { observe: 'response' })
      .map((response: HttpResponse<any>) => this.parseETMiniTracesIfNecessary(response.body));
  }

  /* ******************************************* */
  /* **************** Functions **************** */
  /* ******************************************* */

  /* ************ */
  /* *** Logs *** */
  /* ************ */

  convertToLogTraces(data: any[]): any[] {
    let tracesList: any[] = [];
    for (let logEntry of data) {
      let source: any = logEntry._source;
      if (source === undefined || source === null) {
        source = logEntry;
      }

      if (source['message'] !== undefined) {
        tracesList.push({
          timestamp: source['@timestamp'],
          message: source['message'],
          level: source.level,
          stream: source.stream,
          stream_type: source.stream_type,
          exec: source.exec,
          component: source.component,
        });
      }
    }
    return tracesList;
  }

  getLogsMonitoringQuery(
    indices: string,
    stream: string,
    component: string = undefined,
    timestamp: Date = undefined,
    message: string = undefined,
    from: Date = undefined,
    to: Date = undefined,
    includedFrom: boolean = true,
    includedTo: boolean = true,
  ): MonitoringQueryModel {
    let query: MonitoringQueryModel = new MonitoringQueryModel();
    query.indices = indices.split(',');
    query.stream = stream;

    if (component !== undefined && component !== '') {
      query.component = component;
    }

    if (timestamp !== undefined) {
      query.timestamp = timestamp;
    }
    if (message !== undefined) {
      query.message = message;
    }

    query.setTimeRange(from, to, includedFrom, includedTo);

    return query;
  }

  getAllLogs(
    index: string,
    stream: string,
    component: string,
    from?: Date,
    to?: Date,
    includedFrom: boolean = true,
    includedTo: boolean = true,
  ): Observable<string[]> {
    let _logs: Subject<string[]> = new Subject<string[]>();
    let logs: Observable<string[]> = _logs.asObservable();

    let query: MonitoringQueryModel = this.getLogsMonitoringQuery(
      index,
      stream,
      component,
      undefined,
      undefined,
      from,
      to,
      includedFrom,
      includedTo,
    );
    this.searchAllLogs(query).subscribe((data) => {
      _logs.next(this.convertToLogTraces(data));
    });

    return logs;
  }

  getPrevLogsFromTrace(
    index: string,
    traces: any[],
    stream: string,
    component: string,
    from: Date = undefined,
    to: Date = undefined,
    includedFrom: boolean = true,
    includedTo: boolean = true,
  ): Observable<string[]> {
    let _logs: Subject<string[]> = new Subject<string[]>();
    let logs: Observable<string[]> = _logs.asObservable();

    if (traces.length > 0) {
      let trace: any = traces[0];
      let query: MonitoringQueryModel = this.getLogsMonitoringQuery(
        index,
        stream,
        component,
        trace.timestamp,
        trace.message,
        from,
        to,
        includedFrom,
        includedTo,
      );
      this.searchPreviousLogs(query).subscribe((data) => {
        _logs.next(this.convertToLogTraces(data));
        if (data.length > 0) {
          this.popupService.openSnackBar('Previous traces has been loaded', 'OK');
        } else {
          this.popupService.openSnackBar("There aren't previous traces to load", 'OK');
        }
      });
    } else {
      _logs.error("There isn't reference traces yet to load previous");
      this.popupService.openSnackBar("There isn't reference traces yet to load previous", 'OK');
    }
    return logs;
  }

  getLastLogTraces(index: string, stream: string, component: string, size: number = 10): Observable<string[]> {
    let _logs: Subject<string[]> = new Subject<string[]>();
    let logs: Observable<string[]> = _logs.asObservable();

    let query: MonitoringQueryModel = this.getLogsMonitoringQuery(index, stream, component);
    this.searchLastLogs(query, size).subscribe((data) => {
      _logs.next(this.convertToLogTraces(data));
    });
    return logs;
  }

  getLogsTree(tJobExec: AbstractTJobExecModel): Observable<any[]> {
    let query: MonitoringQueryModel = new MonitoringQueryModel();
    if (tJobExec instanceof TJobExecModel && tJobExec.isParent()) {
      query.indices = tJobExec.getChildsMonitoringIndices().split(',');
    } else {
      query.indices = tJobExec.getMonitoringIndexAsList();
    }

    query.selectedTerms.push('component', 'stream');

    return this.searchLogsTree(query);
  }

  /* *************** */
  /* *** Metrics *** */
  /* *************** */

  getMetricsMonitoringQuery(
    indices: string,
    etType: string,
    component: string = undefined,
    timestamp: Date = undefined,
    from: Date = undefined,
    to: Date = undefined,
    includedFrom: boolean = true,
    includedTo: boolean = true,
  ): MonitoringQueryModel {
    let query: MonitoringQueryModel = new MonitoringQueryModel();
    query.indices = indices.split(',');
    query.etType = etType;
    if (timestamp !== undefined) {
      query.timestamp = timestamp;
    }

    if (component !== undefined && component !== '') {
      query.component = component;
    }

    query.setTimeRange(from, to, includedFrom, includedTo);

    return query;
  }

  getAllMetrics(
    index: string,
    metricsField: MetricsFieldModel,
    theQuery?: any,
    from: Date = undefined,
    to: Date = undefined,
    includedFrom: boolean = true,
    includedTo: boolean = true,
  ): Observable<LineChartMetricModel[]> {
    let _metrics: Subject<LineChartMetricModel[]> = new Subject<LineChartMetricModel[]>();
    let metrics: Observable<LineChartMetricModel[]> = _metrics.asObservable();

    let query: MonitoringQueryModel = this.getMetricsMonitoringQuery(
      index,
      metricsField.etType,
      metricsField.component,
      undefined,
      from,
      to,
      includedFrom,
      includedTo,
    );

    this.searchAllMetrics(query).subscribe((data: any[]) => {
      _metrics.next(this.convertToMetricTraces(data, metricsField));
    });

    return metrics;
  }

  getPrevMetricsFromTrace(
    index: string,
    trace: any,
    metricsField: MetricsFieldModel,
    from: Date = undefined,
    to: Date = undefined,
    includedFrom: boolean = true,
    includedTo: boolean = true,
  ): Observable<LineChartMetricModel[]> {
    let _metrics: Subject<LineChartMetricModel[]> = new Subject<LineChartMetricModel[]>();
    let metrics: Observable<LineChartMetricModel[]> = _metrics.asObservable();

    if (trace !== undefined) {
      let query: MonitoringQueryModel = this.getMetricsMonitoringQuery(
        index,
        metricsField.etType,
        metricsField.component,
        trace.timestamp,
        from,
        to,
        includedFrom,
        includedTo,
      );

      this.searchPreviousMetrics(query).subscribe((data) => {
        _metrics.next(this.convertToMetricTraces(data, metricsField));
      });
    } else {
      _metrics.next([]);
      this.popupService.openSnackBar("There isn't reference traces yet to load previous", 'OK');
    }

    return metrics;
  }

  getLastMetricTraces(index: string, metricsField: MetricsFieldModel, size: number = 10): Observable<LineChartMetricModel[]> {
    let _metrics: Subject<LineChartMetricModel[]> = new Subject<LineChartMetricModel[]>();
    let metrics: Observable<LineChartMetricModel[]> = _metrics.asObservable();

    let query: MonitoringQueryModel = this.getMetricsMonitoringQuery(index, metricsField.etType, metricsField.component);

    this.searchLastMetrics(query, size).subscribe((data: any[]) => {
      _metrics.next(this.convertToMetricTraces(data, metricsField));
    });
    return metrics;
  }

  getMetricsTree(tJobExec: AbstractTJobExecModel, oldSupport: boolean = false): Observable<any[]> {
    let _obs: Subject<any[]> = new Subject<any[]>();
    let obs: Observable<any[]> = _obs.asObservable();

    let query: MonitoringQueryModel = new MonitoringQueryModel();
    if (tJobExec instanceof TJobExecModel && tJobExec.isParent()) {
      query.indices = tJobExec.getChildsMonitoringIndices().split(',');
    } else {
      query.indices = tJobExec.getMonitoringIndexAsList();
    }
    query.selectedTerms.push('component', 'stream', 'et_type');
    if (!oldSupport) {
      query.selectedTerms.push('stream_type');
    }

    this.searchMetricsTree(query).subscribe(
      (data: any[]) => {
        _obs.next(data);
      },
      (error: Error) => {
        if (!oldSupport) {
          console.log('Error on get metrics Tree. Retrying');
          this.getMetricsTree(tJobExec, true).subscribe(
            (data: any[]) => {
              _obs.next(data);
            },
            (error2: Error) => {
              console.error(error2);
            },
          );
        } else {
          console.error(error);
        }
      },
    );
    return obs;
  }

  /* ********************* */
  /* ** Convert Metrics ** */
  /* ********************* */

  convertToMetricTraces(data: any[], metricsField: MetricsFieldModel): LineChartMetricModel[] {
    let tracesList: LineChartMetricModel[];
    let position: number = undefined;
    let parsedMetric: any;

    if (data === undefined || data === null) {
      return tracesList;
    }

    if (metricsField.componentIsEmpty()) {
      tracesList = this.getInitMetricsData();
    } else {
      tracesList = this.getInitMetricsDataComplex(metricsField);
    }
    let marks: MonitorMarkModel[];
    for (let metricTrace of data) {
      let source: any = metricTrace._source;
      if (source === undefined || source === null) {
        source = metricTrace;
      }

      if (
        metricsField.tJobExec &&
        metricsField.tJobExec.isChild() &&
        source.exec &&
        metricsField.tJobExec.getMonitoringIndexAsList().indexOf(source.exec) === -1
      ) {
        // ignore
      } else {
        let validTrace: boolean = true;

        // if active View
        if (source['@timestamp'] && metricsField.tJobExec && metricsField.activeView) {
          if (metricsField.tJobExec.hasMonitoringMarks()) {
            let sourceDate: Date = new Date(source['@timestamp']);
            if (!marks) {
              marks = metricsField.tJobExec.getMonitoringMarksById(metricsField.activeView);
            }
            let timeDifference: number;
            let currentValid: boolean = false;
            let markPos: number = 0;
            let selectedMarkPos: number = 0;
            for (let mark of marks) {
              if (!mark) {
                currentValid = false;
              }

              let currentTimeDifference: number = Math.abs(mark.timestamp.getTime() - sourceDate.getTime());
              let sameTime: boolean = currentTimeDifference < 1000;

              if (sameTime) {
                currentValid = true;
                if (!timeDifference || (timeDifference && currentTimeDifference <= timeDifference)) {
                  source.monitorMarkValue = mark.value;
                  timeDifference = currentTimeDifference;
                  selectedMarkPos = markPos;
                }
              } else {
                currentValid = false || currentValid;
              }
              markPos++;
            }
            validTrace = currentValid;
            if (validTrace) {
              // Remove mark to avoid duplicates
              marks.splice(selectedMarkPos, 1);
            }
          } else {
            validTrace = false;
          }
        }

        if (validTrace) {
          parsedMetric = this.convertToMetricTrace(source, metricsField);

          if (metricsField.componentIsEmpty()) {
            position = this.getMetricPosition(source.component);
          } else {
            position = 0;
          }
          if (position !== undefined && parsedMetric !== undefined) {
            tracesList[position].series.push(parsedMetric);
          }
        }
      }
    }

    return tracesList;
  }

  convertToMetricTrace(trace: any, metricsField: MetricsFieldModel): any {
    let parsedData: any = undefined;
    // If come from ET Mini (ETM)
    this.parseETMiniTraceIfNecessary(trace);

    // If it's a ElasTest default metric (dockbeat)
    if (trace.stream === defaultStreamMap.atomic_metric || trace.stream === defaultStreamMap.composed_metrics) {
      if (trace['et_type'] === 'cpu' && metricsField.etType === 'cpu') {
        parsedData = this.convertToCpuData(trace, metricsField);
      } else if (trace['et_type'] === 'memory' && metricsField.etType === 'memory') {
        parsedData = this.convertToMemoryData(trace, metricsField);
      } else if (trace['et_type'] === 'blkio' && metricsField.etType === 'blkio') {
        parsedData = this.convertToDiskData(trace, metricsField);
      } else if (trace['et_type'] === 'net' && metricsField.etType === 'net') {
        parsedData = this.convertToNetData(trace, metricsField);
      } else if (trace['et_type'] === metricsField.etType) {
        parsedData = this.convertToGenericMetricsData(trace, metricsField);
      }
    } else {
      if (isMetricFieldGroup(trace['et_type'], this.metricbeatFieldGroupList)) {
        // metricbeat
        parsedData = this.convertMetricbeatTrace(trace, metricsField);
      } else if (trace['et_type'] === metricsField.etType) {
        parsedData = this.convertToGenericMetricsData(trace, metricsField);
      }
    }
    return parsedData;
  }

  convertToGenericMetricsData(trace: any, metricsField: MetricsFieldModel): SingleMetricModel {
    let parsedData: SingleMetricModel = undefined;
    if (trace['@timestamp'] !== '0001-01-01T00:00:00.000Z' && trace[trace['et_type']]) {
      parsedData = this.getBasicSingleMetric(trace, metricsField);
      if (metricsField.streamType === 'atomic_metric') {
        parsedData.value = trace[trace['et_type']];
      } else {
        parsedData.value = trace[trace['et_type']][metricsField.subtype];
      }
    }
    return parsedData;
  }

  convertToCpuData(trace: any, metricsField: MetricsFieldModel): SingleMetricModel {
    let parsedData: SingleMetricModel = undefined;
    if (trace['@timestamp'] !== '0001-01-01T00:00:00.000Z') {
      switch (metricsField.subtype) {
        case 'totalUsage':
          parsedData = this.getBasicSingleMetric(trace, metricsField);
          parsedData.value = trace.cpu.totalUsage * 100;
          break;
        default:
          // parsedData = this.convertToGenericMetricsData(trace, metricsField);
          break;
      }
    }
    return parsedData;
  }

  convertToMemoryData(trace: any, metricsField: MetricsFieldModel): SingleMetricModel {
    let parsedData: SingleMetricModel = undefined;
    if (trace['@timestamp'] !== '0001-01-01T00:00:00.000Z') {
      switch (metricsField.subtype) {
        case 'usage':
          let perMemoryUsage: number = (trace.memory.usage * 100) / trace.memory.limit;
          if (perMemoryUsage >= 0) {
            parsedData = this.getBasicSingleMetric(trace, metricsField);
            parsedData.value = perMemoryUsage;
          }
          break;
        case 'limit':
          parsedData = this.getBasicSingleMetric(trace, metricsField);
          parsedData.value = trace.memory.limit;
          break;
        case 'maxUsage':
          parsedData = this.getBasicSingleMetric(trace, metricsField);
          parsedData.value = trace.memory.maxUsage;
          break;
        default:
          break;
      }
      if (parsedData && (parsedData.value === undefined || parsedData.value === '')) {
        parsedData = undefined;
      }
    }
    return parsedData;
  }

  convertToDiskData(trace: any, metricsField: MetricsFieldModel): SingleMetricModel {
    let parsedData: SingleMetricModel = undefined;
    if (trace['@timestamp'] !== '0001-01-01T00:00:00.000Z') {
      switch (metricsField.subtype) {
        case 'read_ps':
          parsedData = this.getBasicSingleMetric(trace, metricsField);
          parsedData.value = trace.blkio.read_ps;
          break;
        case 'write_ps':
          parsedData = this.getBasicSingleMetric(trace, metricsField);
          parsedData.value = trace.blkio.write_ps;
          break;
        case 'total_ps':
          parsedData = this.getBasicSingleMetric(trace, metricsField);
          parsedData.value = trace.blkio.total_ps;
          break;
        default:
          break;
      }
    }
    return parsedData;
  }
  convertToNetData(trace: any, metricsField: MetricsFieldModel): SingleMetricModel {
    let parsedData: SingleMetricModel = undefined;
    if (trace['@timestamp'] !== '0001-01-01T00:00:00.000Z') {
      switch (metricsField.subtype) {
        case 'rxBytes_ps':
          parsedData = this.getBasicSingleMetric(trace, metricsField);
          parsedData.value = trace.net.rxBytes_ps;
          break;
        case 'rxErrors_ps':
          parsedData = this.getBasicSingleMetric(trace, metricsField);
          parsedData.value = trace.net.rxErrors_ps;
          break;
        case 'rxPackets_ps':
          parsedData = this.getBasicSingleMetric(trace, metricsField);
          parsedData.value = trace.net.rxPackets_ps;
          break;
        case 'txBytes_ps':
          parsedData = this.getBasicSingleMetric(trace, metricsField);
          parsedData.value = trace.net.txBytes_ps;
          break;
        case 'txErrors_ps':
          parsedData = this.getBasicSingleMetric(trace, metricsField);
          parsedData.value = trace.net.txErrors_ps;
          break;
        case 'txPackets_ps':
          parsedData = this.getBasicSingleMetric(trace, metricsField);
          parsedData.value = trace.net.txPackets_ps;
          break;
        case 'txDropped_ps':
          parsedData = this.getBasicSingleMetric(trace, metricsField);
          parsedData.value = trace.net.txDropped_ps;
          break;
        case 'rxDropped_ps':
          parsedData = this.getBasicSingleMetric(trace, metricsField);
          parsedData.value = trace.net.rxDropped_ps;
          break;
        default:
          break;
      }
    }
    return parsedData;
  }

  /***************************/
  /* ** Metricbeat traces ** */
  /***************************/
  convertMetricbeatTrace(trace: any, metricsField: MetricsFieldModel): SingleMetricModel {
    let parsedData: SingleMetricModel = undefined;
    let typeArr: string = trace['et_type'].split('_');
    if (trace['@timestamp'] !== '0001-01-01T00:00:00.000Z') {
      if (typeArr[0] in MetricbeatType && trace[trace['et_type']]) {
        let subtypeValueObj: any = trace[trace['et_type']][metricsField.subtype];
        switch (typeArr[1]) {
          case 'cpu':
            if (subtypeValueObj && subtypeValueObj.pct !== undefined) {
              parsedData = this.getBasicSingleMetric(trace, metricsField);
              // pct is 0-1 based percentage
              parsedData.value = this.convertMetricbeatPctTrace(subtypeValueObj.pct);
            }
            break;
          case 'memory':
            // case 'network':
            if (subtypeValueObj && subtypeValueObj.pct !== undefined) {
              parsedData = this.getBasicSingleMetric(trace, metricsField);
              parsedData.value = this.convertMetricbeatPctTrace(subtypeValueObj.pct);
            } else {
              let nestedSubtype: string[] = metricsField.subtype.split('_');
              if (nestedSubtype.length === 2) {
                let nestedSubtypeObj: any = trace[trace['et_type']][nestedSubtype[0]]; // system_memory :{ USED: { pct: xxxx, bytes: xxxx }}
                let nestedSubtypeValueObj: any = nestedSubtypeObj[nestedSubtype[1]]; // system_memory :{ used: { PCT: xxxx }}
                if (nestedSubtypeObj && nestedSubtypeValueObj !== undefined) {
                  parsedData = this.getBasicSingleMetric(trace, metricsField);
                  parsedData.value = nestedSubtypeValueObj;
                }
              }
            }
            break;
          default:
            break;
        }
      }
    }
    return parsedData;
  }

  convertMetricbeatPctTrace(pctValue: any): number {
    let percentage: number;
    if (pctValue !== undefined && pctValue !== null) {
      if (isString(pctValue)) {
        percentage = Number(pctValue) * 100;
      } else if (isNumber) {
        percentage = pctValue * 100;
      }
    }
    return percentage;
  }

  // convertMetricbeatCpuTrace(trace: any, metricsField: MetricsFieldModel): SingleMetricModel {
  //     let parsedData: SingleMetricModel = undefined;
  //     switch (metricsField.subtype) {
  //         case 'cpu':
  //             parsedData = this.getBasicSingleMetric(trace,metricsField);
  //             parsedData.value = trace.cpu.totalUsage * 100;
  //             break;
  //         default:
  //             break;
  //     }
  //     return parsedData;
  // }

  /***********************************/
  /***** Common metric functions *****/
  /***********************************/

  getBasicSingleMetric(trace: any, metricsField: MetricsFieldModel): SingleMetricModel {
    let parsedData: SingleMetricModel = new SingleMetricModel();
    let timestampDate: Date = new Date('' + trace['@timestamp']);
    parsedData.name = timestampDate;

    // Multi tJobExec child
    if (metricsField && metricsField.tJobExec && metricsField.tJobExec.isChild()) {
      let newDateInMillis: number = timestampDate.getTime() - metricsField.tJobExec.startDate.getTime();
      parsedData.name = new Date(newDateInMillis);
    }

    // If active View
    // Note: monitorMarkValue set to trace in convertToMetricTrace()
    if (trace.monitorMarkValue) {
      parsedData.name = trace.monitorMarkValue;
    }

    parsedData.timestamp = trace['@timestamp'];
    return parsedData;
  }

  getMetricPosition(component: string): number {
    let position: number = undefined;

    component = component.toLowerCase();
    component = component.charAt(0).toUpperCase() + component.slice(1);

    if (MetricsDataType[component] !== undefined) {
      position = MetricsDataType[component];
    }
    return position;
  }

  getInitMetricsData(): LineChartMetricModel[] {
    let tracesList: LineChartMetricModel[] = [];

    for (let etType in MetricsDataType) {
      if (isNaN(parseInt(etType))) {
        tracesList[MetricsDataType[etType]] = new LineChartMetricModel();
        tracesList[MetricsDataType[etType]].name = etType;
      }
    }
    return tracesList;
  }

  getInitMetricsDataComplex(metricsField: MetricsFieldModel): LineChartMetricModel[] {
    let tracesList: LineChartMetricModel[] = [];
    let trace: LineChartMetricModel = new LineChartMetricModel();
    trace.name = metricsField.name;
    tracesList.push(trace);
    return tracesList;
  }

  getMetricUnitByTrace(dataSource: any, metricName: string, etType: string): Units | string {
    let unit: Units | string;
    if (dataSource && dataSource.units && dataSource.units[metricName]) {
      unit = dataSource.units[metricName];
    } else if (dataSource && dataSource.unit) {
      unit = dataSource.unit;
    } else {
      unit = this.allMetricsFields.getDefaultUnitByTypeAndSubtype(etType, metricName);
    }
    return unit;
  }

  getMetricUnit(index: string, metricsField: MetricsFieldModel): Observable<Units | string> {
    let _metricsObs: Subject<Units | string> = new Subject<Units | string>();
    let metricsObs: Observable<Units | string> = _metricsObs.asObservable();
    let query: MonitoringQueryModel = this.getMetricsMonitoringQuery(index, metricsField.etType, metricsField.component);

    this.searchLastMetrics(query, 1).subscribe(
      (dataSource: any[]) => {
        if (dataSource && dataSource.length === 1) {
          let unit: Units | string = this.getMetricUnitByTrace(dataSource[0], metricsField.subtype, metricsField.etType);
          _metricsObs.next(unit);
        } else {
          _metricsObs.error(new Error('Error on get metric Unit: No traces found'));
        }
      },
      (error: Error) => _metricsObs.error(error),
    );
    return metricsObs;
  }

  initTestLog(log: ESRabLogModel): void {
    log.name = 'Test Logs';
    log.etType = 'et_logs';
    log.stream = 'custom_log';
    log.component = 'test';
  }

  initSutLog(log: ESRabLogModel): void {
    log.name = 'SuT Logs';
    log.etType = 'et_logs';
    log.stream = 'custom_log';
    log.component = 'sut';
  }

  /*******************/
  /***** Dynamic *****/
  /*******************/
  getDynamicTerms(stream: string, component: string): any[] {
    let terms: any[];
    terms = [{ term: { stream: stream } }, { term: { component: component } }];
    return terms;
  }

  searchAllDynamic(
    index: string,
    stream: string,
    component: string,
    metricName?: string,
    tJobExec?: AbstractTJobExecModel,
    from: Date = undefined,
    to: Date = undefined,
    includedFrom: boolean = true,
    includedTo: boolean = true,
    traceType: 'log' | 'metric' = undefined,
  ): Observable<any> {
    let _obs: Subject<any> = new Subject<any>();
    let obs: Observable<any> = _obs.asObservable();

    let query: MonitoringQueryModel = new MonitoringQueryModel();
    query.indices = index.split(',');
    query.stream = stream;
    query.component = component;
    query.setTimeRange(from, to, includedFrom, includedTo);

    query.selectedTerms.push('stream', 'component');
    let metricSubtype: string;
    if (metricName && metricName !== '') {
      let metricType: string = metricName.split('.')[0];
      metricSubtype = metricName.split('.')[1];
      query.etType = metricType;
      query.selectedTerms.push('etType');
    }

    let getTracesSubscription: Observable<any> = this.searchAllByTerms(query);

    if (traceType !== undefined) {
      if (traceType === 'log') {
        getTracesSubscription = this.searchAllLogs(query);
      } else if (traceType === 'metric') {
        getTracesSubscription = this.searchAllMetrics(query);
      }
    }

    getTracesSubscription.subscribe((data: any[]) => {
      if (data.length > 0) {
        let convertedData: any;
        let firstElement: any = data[0];
        let firstSource: any = firstElement._source ? firstElement._source : firstElement;
        let streamType: string = '';
        let etType: string = firstSource['et_type'];
        let obj: any = {
          streamType: streamType,
          etType: etType,
          data: convertedData,
          component: component,
          stream: stream,
          monitoringIndex: index,
          tJobExec: tJobExec as TJobExecModel,
        };

        if (this.isLogTrace(firstSource)) {
          this.addDynamicLog(_obs, obj, data);
        } else if (this.isMetricsTrace(firstSource)) {
          this.addDynamicComposedMetrics(_obs, obj, data, metricSubtype);
        } else if (this.isAtomicMetricTrace(firstSource)) {
          this.addDynamicAtomicMetric(_obs, obj, data);
        } else {
          let errorStr: string = 'Cannot add the traces obtained with the parameters provided';
          _obs.error(errorStr);
        }
      } else {
        let errorStr: string = 'Nothing found';
        _obs.error(errorStr);
      }
    });

    return obs;
  }

  addDynamicLog(_obs: Subject<any>, obj: any, data: any[]): void {
    let logTraces: string[] = this.convertToLogTraces(data);

    obj.data = logTraces;
    obj.streamType = 'log';
    _obs.next(obj);
  }

  addDynamicComposedMetrics(_obs: Subject<any>, obj: any, data: any[], subtype?: string): void {
    let firstElement: any = data[0];
    let firstSource: any = firstElement._source ? firstElement._source : firstElement;
    if (subtype) {
      this.addDynamicMetric(_obs, obj, data, firstSource, subtype, 'composed_metrics');
    } else {
      // Add all subtypes of metric
      /* Like: @timestamp: "2019-08-27T13:18:40.295Z"
              component: "tss_eus_browser_0bd778a709b156f16ffab5b6404898ed"
              et_type: "webrtc_inbound_video_4023135614"
              exec: "31159"
              stream: "webRtc"
              stream_type: "composed_metrics"
              type: "_doc"
              units: {bytesReceived: "bytes", jitter: "ms", packetsReceived: "packets", packetsLost: "packets",…}
              webrtc_inbound_video_4023135614: {
                bytesReceived: 215969
                framesDecoded: 30
                jitter: "46"
                nackCount: 0
                packetsLost: 0
                packetsReceived: 167
              }
      */
      let metricObj: any = firstSource[firstSource['et_type']];
      for (let metricName in metricObj) {
        this.addDynamicMetric(_obs, obj, data, firstSource, metricName, 'composed_metrics');
      }
    }
  }

  addDynamicAtomicMetric(_obs: Subject<any>, obj: any, data: any[]): void {
    let firstElement: any = data[0];
    let firstSource: any = firstElement._source ? firstElement._source : firstElement;
    let metricName: string = firstSource['et_type'];
    this.addDynamicMetric(_obs, obj, data, firstSource, metricName, 'atomic_metric');
  }

  addDynamicMetric(_obs: Subject<any>, obj: any, data: any[], firstSource: any, metricName: string, streamType: string): void {
    let unit: Units | string = this.getMetricUnitByTrace(firstSource, metricName, obj.etType);

    let metricsTraces: LineChartMetricModel[] = [];
    obj['metricFieldModels'] = [];

    // If is Multi TJobExec Parent
    if (obj.tJobExec !== undefined && obj.tJobExec.isParent() && obj.tJobExec.hasChilds()) {
      for (let child of obj.tJobExec.execChilds) {
        child.activeView = obj.tJobExec.activeView;
        let metricsField: MetricsFieldModel = new MetricsFieldModel(
          firstSource['et_type'],
          metricName,
          unit,
          obj.component,
          obj.stream,
          streamType,
          true,
          child,
        );
        metricsTraces = metricsTraces.concat(this.convertToMetricTraces(data, metricsField));
        obj['metricFieldModels'].push(metricsField);
      }
    } else {
      // Normal
      let metricsField: MetricsFieldModel = new MetricsFieldModel(
        firstSource['et_type'],
        metricName,
        unit,
        obj.component,
        obj.stream,
        streamType,
        true,
      );
      metricsTraces = this.convertToMetricTraces(data, metricsField);
      obj['metricFieldModels'].push(metricsField);
    }

    obj.data = metricsTraces;
    obj.streamType = streamType;

    obj.unit = unit;

    let nonEmpty: boolean = false;
    for (let metricsTrace of metricsTraces) {
      nonEmpty = nonEmpty || (metricsTrace && metricsTrace.series && metricsTrace.series.length > 0);
    }
    if (nonEmpty) {
      // If chart is not empty, add it
      _obs.next(obj);
    } else {
      _obs.error(new Error('There is no metric traces to add to the graph ' + metricsTraces[0].name));
    }
  }

  isLogTrace(traceSource: any): boolean {
    return (
      traceSource['stream_type'] !== undefined && traceSource['stream_type'] !== null && traceSource['stream_type'] === 'log'
    );
  }

  isMetricsTrace(traceSource: any): boolean {
    return (
      traceSource['stream_type'] !== undefined &&
      traceSource['stream_type'] !== null &&
      traceSource['stream_type'] === 'composed_metrics'
    );
  }

  isAtomicMetricTrace(traceSource: any): boolean {
    return (
      traceSource['stream_type'] !== undefined &&
      traceSource['stream_type'] !== null &&
      traceSource['stream_type'] === 'atomic_metric'
    );
  }

  getBasicFilterFields(streamType?: string): string[] {
    let filters: string[] = ['@timestamp', 'et_type', 'component', 'stream', 'stream_type', 'exec'];

    if (streamType && streamType === 'log') {
      filters.push('message', 'level');
    }

    return filters;
  }

  /* ************** */
  /* **** Logs **** */
  /* ************** */

  getAllTJobExecLogs(tJobExec: TJobExecModel): Observable<LogTraces[]> {
    let _logs: Subject<LogTraces[]> = new Subject<LogTraces[]>();
    let logsObs: Observable<LogTraces[]> = _logs.asObservable();
    let logs: LogTraces[] = [];
    this.getLogsTree(tJobExec).subscribe((logsComponentStreams: any[]) => {
      let allLogs: ESRabLogModel[] = [];
      for (let componentStream of logsComponentStreams) {
        for (let stream of componentStream.children) {
          let currentLog: ESRabLogModel = new ESRabLogModel(this);
          currentLog.component = componentStream.name;
          currentLog.stream = stream.name;
          currentLog.monitoringIndex = tJobExec.monitoringIndex;
          allLogs.push(currentLog);
        }
      }
      this.getAllLogsByGiven(allLogs, _logs, logs);
    });
    return logsObs;
  }

  getAllLogsByGiven(logsObjList: ESRabLogModel[], _logs: Subject<LogTraces[]>, logs: LogTraces[]): void {
    if (logsObjList.length > 0) {
      let currentLog: ESRabLogModel = logsObjList.shift();
      currentLog.getAllLogsSubscription().subscribe(
        (data: any[]) => {
          let logTraces: LogTraces = new LogTraces();
          logTraces.name = currentLog.component + '-' + currentLog.stream;
          logTraces.traces = data;
          logs.push(logTraces);
          this.getAllLogsByGiven(logsObjList, _logs, logs);
        },
        (error: Error) => {
          this.getAllLogsByGiven(logsObjList, _logs, logs);
        },
      );
    } else {
      _logs.next(logs);
    }
  }

  /* *************** */
  /* *** Metrics *** */
  /* *************** */

  getAllTJobExecMetrics(tJobExec: TJobExecModel): Observable<MetricTraces[]> {
    let _metrics: Subject<MetricTraces[]> = new Subject<MetricTraces[]>();
    let metricsObs: Observable<MetricTraces[]> = _metrics.asObservable();
    let metrics: MetricTraces[] = [];
    this.getMetricsTree(tJobExec).subscribe((metricsComponentStreamEtTypes: any[]) => {
      let allMetrics: ESRabComplexMetricsModel[] = [];
      for (let componentStreamEtType of metricsComponentStreamEtTypes) {
        for (let streamEtType of componentStreamEtType.children) {
          for (let etType of streamEtType.children) {
            let currentMetricFieldGroupList: MetricFieldGroupModel[] = [];
            if (isMetricFieldGroup(etType.name, this.metricbeatFieldGroupList)) {
              // If is Metricbeat etType
              currentMetricFieldGroupList = this.metricbeatFieldGroupList;
            } else if (isMetricFieldGroup(etType.name, metricFieldGroupList)) {
              // If it's Dockbeat etType
              currentMetricFieldGroupList = metricFieldGroupList;
            }

            for (let metricFieldGroup of currentMetricFieldGroupList) {
              if (metricFieldGroup.etType === etType.name) {
                for (let subtype of metricFieldGroup.subtypes) {
                  let currentMetric: ESRabComplexMetricsModel = new ESRabComplexMetricsModel(this);
                  currentMetric.component = componentStreamEtType.name;
                  currentMetric.stream = streamEtType.name;
                  currentMetric.name = etType.name + '.' + subtype.subtype;
                  currentMetric.monitoringIndex = tJobExec.monitoringIndex;

                  allMetrics.push(currentMetric);
                }
              }
            }
          }
        }
      }
      this.getAllMetricsByGiven(allMetrics, _metrics, metrics);
    });
    return metricsObs;
  }

  getAllMetricsByGiven(
    metricsObjList: ESRabComplexMetricsModel[],
    _metrics: Subject<MetricTraces[]>,
    metrics: MetricTraces[],
  ): void {
    if (metricsObjList.length > 0) {
      let currentMetric: ESRabComplexMetricsModel = metricsObjList.shift();
      this.searchAllDynamic(
        currentMetric.monitoringIndex,
        currentMetric.stream,
        currentMetric.component,
        currentMetric.name,
      ).subscribe(
        (obj: any) => {
          let metricTraces: MetricTraces = new MetricTraces();
          metricTraces.name = currentMetric.component + '-' + currentMetric.stream + '-' + currentMetric.name;
          metricTraces.traces = this.getMetricsObjFromRawSource(obj.data);
          metrics.push(metricTraces);
          this.getAllMetricsByGiven(metricsObjList, _metrics, metrics);
        },
        (error: Error) => {
          this.getAllMetricsByGiven(metricsObjList, _metrics, metrics);
        },
      );
    } else {
      _metrics.next(metrics);
    }
  }

  // Utils
  getLogsObjFromRawSource(raw: any[]): any {
    let processedLogs: any = {};
    if (raw !== undefined && raw !== null) {
      for (let logTrace of raw) {
        let logKey: string = logTrace.component + '-' + logTrace.stream;
        if (processedLogs[logKey] === undefined || processedLogs[logKey] === null) {
          processedLogs[logKey] = [];
        }
        processedLogs[logKey].push({
          timestamp: logTrace['@timestamp'],
          message: logTrace.message,
        });
      }
    }
    return processedLogs;
  }

  getMetricsObjFromRawSource(raw: any[]): any {
    let processedMetrics: any = {};
    if (raw !== undefined && raw !== null) {
      for (let metricTrace of raw) {
        let metricKey: string = metricTrace.name;
        if (processedMetrics[metricKey] === undefined || processedMetrics[metricKey] === null) {
          processedMetrics[metricKey] = [];
        }

        processedMetrics[metricKey] = processedMetrics[metricKey].concat(metricTrace.series);
      }
    }
    return processedMetrics;
  }

  /* */

  parseETMiniTracesIfNecessary(traces: any[]): any[] {
    if (traces !== undefined && traces !== null) {
      for (let trace of traces) {
        trace = this.parseETMiniTraceIfNecessary(trace);
      }
    }
    return traces;
  }

  parseETMiniTraceIfNecessary(trace: any): any {
    if (trace) {
      if (trace.etType && !trace['et_type']) {
        trace['et_type'] = trace.etType;
        // delete trace.etType;
      }
      if (trace.streamType && !trace['stream_type']) {
        trace['stream_type'] = trace.streamType;
        // delete trace.streamType;
      }
      if (!trace[trace['et_type']] && trace.content) {
        trace[trace['et_type']] = JSON.parse(trace.content);
        // delete trace.content;
      }
      if (trace.timestamp && !trace['@timestamp']) {
        trace['@timestamp'] = trace.timestamp;
        // delete trace.timestamp;
      }
    }

    return trace;
  }
}

export class LogTraces {
  name: string;
  traces: any[];

  constructor() {
    this.traces = [];
  }
}

export class MetricTraces {
  name: string;
  traces: any[];
  constructor() {
    this.traces = [];
  }
}
