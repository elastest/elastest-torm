import { Observable, Subject } from 'rxjs/Rx';
import { Injectable } from '@angular/core';
import { Http } from '@angular/http';
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
} from '../metrics-view/metrics-chart-card/models/all-metrics-fields-model';
import { defaultStreamMap } from '../defaultESData-model';
import { ESRabComplexMetricsModel } from '../metrics-view/metrics-chart-card/models/es-rab-complex-metrics-model';
import { ESRabLogModel } from '../logs-view/models/es-rab-log-model';
import { MetricsDataType } from '../metrics-view/models/et-res-metrics-model';
import { TJobExecModel } from '../../elastest-etm/tjob-exec/tjobExec-model';
import { LogAnalyzerQueryModel } from '../loganalyzer-query.model';
@Injectable()
export class MonitoringService {
  etmApiUrl: string;
  allMetricsFields: AllMetricsFields = new AllMetricsFields(); // Object with a list of all metrics
  metricbeatFieldGroupList: MetricFieldGroupModel[];

  constructor(public http: Http, private configurationService: ConfigurationService, public popupService: PopupService) {
    this.etmApiUrl = this.configurationService.configModel.hostApi;
    this.metricbeatFieldGroupList = getMetricBeatFieldGroupList();
  }

  /* ******************************************** */
  /* ***************** API REST ***************** */
  /* ******************************************** */

  public searchAllByTerms(query: MonitoringQueryModel): Observable<any> {
    let url: string = this.etmApiUrl + '/monitoring/byterms';
    return this.http.post(url, query).map((response) => response.json());
  }

  /* *** Logs *** */

  public searchAllLogs(query: MonitoringQueryModel): Observable<any> {
    let url: string = this.etmApiUrl + '/monitoring/log';
    return this.http.post(url, query).map((response) => response.json());
  }

  public searchPreviousLogs(query: MonitoringQueryModel): Observable<any> {
    let url: string = this.etmApiUrl + '/monitoring/log/previous';
    return this.http.post(url, query).map((response) => response.json());
  }

  public searchLastLogs(query: MonitoringQueryModel, size: number): Observable<any> {
    let url: string = this.etmApiUrl + '/monitoring/log/last/' + size;
    return this.http.post(url, query).map((response) => response.json());
  }

  public searchLogsTree(query: MonitoringQueryModel): Observable<any> {
    let url: string = this.etmApiUrl + '/monitoring/log/tree';
    return this.http.post(url, query).map((response) => response.json());
  }

  /* *** Metrics *** */

  public searchAllMetrics(query: MonitoringQueryModel): Observable<any> {
    let url: string = this.etmApiUrl + '/monitoring/metric';
    return this.http.post(url, query).map((response) => response.json());
  }

  public searchPreviousMetrics(query: MonitoringQueryModel): Observable<any> {
    let url: string = this.etmApiUrl + '/monitoring/metric/previous';
    return this.http.post(url, query).map((response) => response.json());
  }

  public searchLastMetrics(query: MonitoringQueryModel, size: number): Observable<any> {
    let url: string = this.etmApiUrl + '/monitoring/metric/last/' + size;
    return this.http.post(url, query).map((response) => response.json());
  }

  public searchMetricsTree(query: MonitoringQueryModel): Observable<any> {
    let url: string = this.etmApiUrl + '/monitoring/metric/tree';
    return this.http.post(url, query).map((response) => response.json());
  }

  /* *** LogAnalyzer *** */

  public searchLogAnalyzerQuery(query: LogAnalyzerQueryModel): Observable<any> {
    let url: string = this.etmApiUrl + '/monitoring/loganalyzer';
    return this.http.post(url, query).map((response) => response.json());
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

      // If come from ET Mini (ETM)
      source = this.parseETMiniTraceIfNecessary(source, true);

      if (source['message'] !== undefined) {
        tracesList.push({
          timestamp: source['@timestamp'],
          message: source['message'],
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
    if (message !== undefined ) {
      query.message = message;
    }
    return query;
  }

  getAllLogs(index: string, stream: string, component: string, theQuery?: any): Observable<string[]> {
    let _logs: Subject<string[]> = new Subject<string[]>();
    let logs: Observable<string[]> = _logs.asObservable();

    let query: MonitoringQueryModel = this.getLogsMonitoringQuery(index, stream, component);
    this.searchAllLogs(query).subscribe((data) => {
      _logs.next(this.convertToLogTraces(data));
    });

    return logs;
  }

  getPrevLogsFromTrace(index: string, traces: any[], stream: string, component: string): Observable<string[]> {
    let _logs: Subject<string[]> = new Subject<string[]>();
    let logs: Observable<string[]> = _logs.asObservable();

    if (traces.length > 0) {
      let trace: any = traces[0];
      let query: MonitoringQueryModel = this.getLogsMonitoringQuery(index, stream, component, trace.timestamp, trace.message);
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

  getLogsTree(tJobExec: TJobExecModel): Observable<any[]> {
    let query: MonitoringQueryModel = new MonitoringQueryModel();
    query.indices = tJobExec.monitoringIndex.split(',');
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
    return query;
  }

  getAllMetrics(index: string, metricsField: MetricsFieldModel, theQuery?: any): Observable<LineChartMetricModel[]> {
    let _metrics: Subject<LineChartMetricModel[]> = new Subject<LineChartMetricModel[]>();
    let metrics: Observable<LineChartMetricModel[]> = _metrics.asObservable();

    let query: MonitoringQueryModel = this.getMetricsMonitoringQuery(index, metricsField.etType, metricsField.component);

    this.searchAllMetrics(query).subscribe((data) => {
      _metrics.next(this.convertToMetricTraces(data, metricsField));
    });

    return metrics;
  }

  getPrevMetricsFromTrace(index: string, trace: any, metricsField: MetricsFieldModel): Observable<LineChartMetricModel[]> {
    let _metrics: Subject<LineChartMetricModel[]> = new Subject<LineChartMetricModel[]>();
    let metrics: Observable<LineChartMetricModel[]> = _metrics.asObservable();

    if (trace !== undefined) {
      let query: MonitoringQueryModel = this.getMetricsMonitoringQuery(
        index,
        metricsField.etType,
        metricsField.component,
        trace.timestamp,
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

    this.searchLastMetrics(query, size).subscribe((data) => {
      _metrics.next(this.convertToMetricTraces(data, metricsField));
    });
    return metrics;
  }

  getMetricsTree(tJobExec: TJobExecModel): Observable<any[]> {
    let query: MonitoringQueryModel = new MonitoringQueryModel();
    query.indices = tJobExec.monitoringIndex.split(',');
    query.selectedTerms.push('component', 'stream', 'et_type');

    return this.searchMetricsTree(query);
  }

  /* ********************* */
  /* ** Convert Metrics ** */
  /* ********************* */

  convertToMetricTraces(data: any[], metricsField: MetricsFieldModel): LineChartMetricModel[] {
    let tracesList: LineChartMetricModel[];
    if (metricsField.component === undefined || metricsField.component === '') {
      tracesList = this.getInitMetricsData();

      let position: number = undefined;
      let parsedMetric: any;
      for (let metricTrace of data) {
        let source: any = metricTrace._source;
        if (source === undefined || source === null) {
          source = metricTrace;
        }

        parsedMetric = this.convertToMetricTrace(source, metricsField);
        position = this.getMetricPosition(source.component);
        if (position !== undefined && parsedMetric !== undefined) {
          tracesList[position].series.push(parsedMetric);
        }
      }
    } else {
      tracesList = this.getInitMetricsDataComplex(metricsField);
      let parsedMetric: any;
      let position: number = undefined;
      for (let metricTrace of data) {
        let source: any = metricTrace._source;
        if (source === undefined || source === null) {
          source = metricTrace;
        }
        parsedMetric = this.convertToMetricTrace(source, metricsField);
        position = 0;
        if (position !== undefined && parsedMetric !== undefined) {
          tracesList[position].series.push(parsedMetric);
        }
      }
    }

    return tracesList;
  }

  convertToMetricTrace(trace: any, metricsField: MetricsFieldModel): any {
    let parsedData: any = undefined;

    // If come from ET Mini (ETM)
    trace = this.parseETMiniTraceIfNecessary(trace, true);

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
      parsedData = this.getBasicSingleMetric(trace);
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
          parsedData = this.getBasicSingleMetric(trace);
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
            parsedData = this.getBasicSingleMetric(trace);
            parsedData.value = perMemoryUsage;
          }
          break;
        case 'limit':
          parsedData = this.getBasicSingleMetric(trace);
          parsedData.value = trace.memory.limit;
          break;
        case 'maxUsage':
          parsedData = this.getBasicSingleMetric(trace);
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
          parsedData = this.getBasicSingleMetric(trace);
          parsedData.value = trace.blkio.read_ps;
          break;
        case 'write_ps':
          parsedData = this.getBasicSingleMetric(trace);
          parsedData.value = trace.blkio.write_ps;
          break;
        case 'total_ps':
          parsedData = this.getBasicSingleMetric(trace);
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
          parsedData = this.getBasicSingleMetric(trace);
          parsedData.value = trace.net.rxBytes_ps;
          break;
        case 'rxErrors_ps':
          parsedData = this.getBasicSingleMetric(trace);
          parsedData.value = trace.net.rxErrors_ps;
          break;
        case 'rxPackets_ps':
          parsedData = this.getBasicSingleMetric(trace);
          parsedData.value = trace.net.rxPackets_ps;
          break;
        case 'txBytes_ps':
          parsedData = this.getBasicSingleMetric(trace);
          parsedData.value = trace.net.txBytes_ps;
          break;
        case 'txErrors_ps':
          parsedData = this.getBasicSingleMetric(trace);
          parsedData.value = trace.net.txErrors_ps;
          break;
        case 'txPackets_ps':
          parsedData = this.getBasicSingleMetric(trace);
          parsedData.value = trace.net.txPackets_ps;
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
              parsedData = this.getBasicSingleMetric(trace);
              parsedData.value = subtypeValueObj.pct;
            }
            break;
          case 'memory':
            // case 'network':
            if (subtypeValueObj && subtypeValueObj.pct !== undefined) {
              parsedData = this.getBasicSingleMetric(trace);
              parsedData.value = subtypeValueObj.pct;
            } else {
              let nestedSubtype: string[] = metricsField.subtype.split('_');
              if (nestedSubtype.length === 2) {
                let nestedSubtypeObj: any = trace[trace['et_type']][nestedSubtype[0]]; // system_memory :{ USED: { pct: xxxx, bytes: xxxx }}
                let nestedSubtypeValueObj: any = nestedSubtypeObj[nestedSubtype[1]]; // system_memory :{ used: { PCT: xxxx }}
                if (nestedSubtypeObj && nestedSubtypeValueObj !== undefined) {
                  parsedData = this.getBasicSingleMetric(trace);
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

  // convertMetricbeatCpuTrace(trace: any, metricsField: MetricsFieldModel): SingleMetricModel {
  //     let parsedData: SingleMetricModel = undefined;
  //     switch (metricsField.subtype) {
  //         case 'cpu':
  //             parsedData = this.getBasicSingleMetric(trace);
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

  getBasicSingleMetric(trace: any): SingleMetricModel {
    let parsedData: SingleMetricModel = new SingleMetricModel();
    parsedData.name = new Date('' + trace['@timestamp']);
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

  searchAllDynamic(index: string, stream: string, component: string, metricName?: string): Observable<any> {
    let _obs: Subject<any> = new Subject<any>();
    let obs: Observable<any> = _obs.asObservable();

    let query: MonitoringQueryModel = new MonitoringQueryModel();
    query.indices = index.split(',');
    query.stream = stream;
    query.component = component;

    query.selectedTerms.push('stream', 'component');
    let metricSubtype: string;
    if (metricName && metricName !== '') {
      let metricType: string = metricName.split('.')[0];
      metricSubtype = metricName.split('.')[1];
      query.etType = metricType;
      query.selectedTerms.push('etType');
    }

    this.searchAllByTerms(query).subscribe((data: any[]) => {
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
    let unit: string;
    if (firstSource.units && firstSource.units[metricName]) {
      unit = firstSource.units[metricName];
    } else if (firstSource.unit) {
      unit = firstSource.unit;
    } else {
      unit = this.allMetricsFields.getDefaultUnitByTypeAndSubtype(obj.etType, metricName);
    }

    let metricsField: MetricsFieldModel = new MetricsFieldModel(
      firstSource['et_type'],
      metricName,
      unit,
      obj.component,
      obj.stream,
      streamType,
      true,
    );
    let metricsTraces: LineChartMetricModel[] = this.convertToMetricTraces(data, metricsField);

    obj.data = metricsTraces;
    obj.streamType = streamType;
    obj['metricFieldModel'] = metricsField;

    obj.unit = unit;
    if (metricsTraces[0].series.length > 0) {
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
    this.getMetricsTree(tJobExec).subscribe((metricsComponentStreamTypes: any[]) => {
      let allMetrics: ESRabComplexMetricsModel[] = [];
      for (let componentStreamType of metricsComponentStreamTypes) {
        for (let streamType of componentStreamType.children) {
          for (let etType of streamType.children) {
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
                  currentMetric.component = componentStreamType.name;
                  currentMetric.stream = streamType.name;
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

  parseETMiniTraceIfNecessary(trace: any, isMetric: boolean = false): any {
    if (trace) {
      if (isMetric && !trace[trace['et_type']] && trace.content) {
        trace[trace['et_type']] = JSON.parse(trace.content);
      }
      if (trace.timestamp && !trace['@timestamp']) {
        trace['@timestamp'] = trace.timestamp;
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
