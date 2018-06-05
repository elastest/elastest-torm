import { ESAggsModel } from '../elasticsearch-model/elasticsearch-model';
import {
  AllMetricsFields,
  getMetricbeatFieldGroupIfItsMetricbeatType,
  getMetricBeatFieldGroupList,
  isMetricFieldGroup,
  MetricbeatType,
  metricFieldGroupList,
  MetricFieldGroupModel,
} from '../metrics-view/metrics-chart-card/models/all-metrics-fields-model';
import { MetricsFieldModel } from '../metrics-view/metrics-chart-card/models/metrics-field-model';
import { SingleMetricModel } from '../metrics-view/models/single-metric-model';
import { ESRabLogModel } from '../logs-view/models/es-rab-log-model';
import { MetricsDataType } from '../metrics-view/models/et-res-metrics-model';
import { LineChartMetricModel } from '../metrics-view/models/linechart-metric-model';
import { ElasticSearchService } from './elasticsearch.service';

import { componentFactoryName } from '@angular/compiler';
import { Injectable } from '@angular/core';
import { PopupService } from './popup.service';
import { Observable, Subject } from 'rxjs/Rx';
import { defaultStreamMap } from '../defaultESData-model';
import { ESBoolQueryModel, ESTermModel, ESRangeModel } from '../elasticsearch-model/es-query-model';
import { TJobExecModel } from '../../elastest-etm/tjob-exec/tjobExec-model';
import { ESRabComplexMetricsModel } from '../metrics-view/metrics-chart-card/models/es-rab-complex-metrics-model';

@Injectable()
export class ElastestESService {
  allMetricsFields: AllMetricsFields = new AllMetricsFields(); // Object with a list of all metrics
  esUrl: string;

  metricbeatFieldGroupList: MetricFieldGroupModel[];

  constructor(private elasticsearchService: ElasticSearchService, public popupService: PopupService) {
    this.esUrl = this.elasticsearchService.esUrl;
    this.metricbeatFieldGroupList = getMetricBeatFieldGroupList();
  }

  search(url: string, searchBody: any) {
    return this.elasticsearchService.internalSearch(url, searchBody);
  }

  getTermsByTypeAndComponent(etType: string, component: string): any[] {
    let terms: any[];
    terms = [{ term: { et_type: etType } }, { term: { component: component } }];
    return terms;
  }

  getTermsByStreamAndComponent(stream: string, component: string): any[] {
    let terms: any[];
    terms = [{ term: { stream: stream } }, { term: { component: component } }];
    return terms;
  }

  getTermsByMetricsField(metricsField: MetricsFieldModel): any[] {
    let terms: any[] = [{ term: { et_type: metricsField.etType } }];
    if (metricsField.component !== undefined && metricsField.component !== '') {
      terms.push({ term: { component: metricsField.component } });
    }
    return terms;
  }

  searchAllLogs(
    index: string,
    stream: string,
    component: string,
    timeRange?: ESRangeModel,
    theQuery?: any,
  ): Observable<string[]> {
    let _logs: Subject<string[]> = new Subject<string[]>();
    let logs: Observable<string[]> = _logs.asObservable();

    let terms: any[] = this.getTermsByStreamAndComponent(stream, component);
    this.elasticsearchService.searchAllByTerm(index, terms, timeRange, theQuery).subscribe((data) => {
      _logs.next(this.convertToLogTraces(data));
    });

    return logs;
  }

  getPrevLogsFromTrace(index: string, traces: any[], stream: string, component: string): Observable<string[]> {
    let _logs: Subject<string[]> = new Subject<string[]>();
    let logs: Observable<string[]> = _logs.asObservable();

    if (traces.length > 0) {
      let trace: any = traces[0];
      let terms: any[] = this.getTermsByStreamAndComponent(stream, component);
      this.elasticsearchService.getPrevFromTimestamp(index, trace.timestamp, terms).subscribe((data) => {
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

  convertToLogTraces(data: any[]): any[] {
    let tracesList: any[] = [];
    for (let logEntry of data) {
      if (logEntry._source['message'] !== undefined) {
        tracesList.push({
          timestamp: logEntry._source['@timestamp'],
          message: logEntry._source['message'],
        });
      }
    }
    return tracesList;
  }

  getLastLogTraces(index: string, stream: string, component: string, size: number = 10): Observable<string[]> {
    let _logs: Subject<string[]> = new Subject<string[]>();
    let logs: Observable<string[]> = _logs.asObservable();

    let terms: any[] = this.getTermsByStreamAndComponent(stream, component);

    this.elasticsearchService.getLast(index, terms, size).subscribe((data) => {
      _logs.next(this.convertToLogTraces(data));
    });
    return logs;
  }

  // Metrics

  searchAllMetrics(
    index: string,
    metricsField: MetricsFieldModel,
    timeRange?: ESRangeModel,
    theQuery?: any,
  ): Observable<LineChartMetricModel[]> {
    let _metrics: Subject<LineChartMetricModel[]> = new Subject<LineChartMetricModel[]>();
    let metrics: Observable<LineChartMetricModel[]> = _metrics.asObservable();
    let terms: any[] = this.getTermsByMetricsField(metricsField);
    this.elasticsearchService.searchAllByTerm(index, terms, timeRange, theQuery).subscribe((data) => {
      _metrics.next(this.convertToMetricTraces(data, metricsField));
    });

    return metrics;
  }

  getPrevMetricsFromTrace(index: string, trace: any, metricsField: MetricsFieldModel): Observable<LineChartMetricModel[]> {
    let _metrics: Subject<LineChartMetricModel[]> = new Subject<LineChartMetricModel[]>();
    let metrics: Observable<LineChartMetricModel[]> = _metrics.asObservable();

    if (trace !== undefined) {
      let terms: any[] = this.getTermsByMetricsField(metricsField);
      this.elasticsearchService.getPrevFromTimestamp(index, trace.timestamp, terms).subscribe((data) => {
        _metrics.next(this.convertToMetricTraces(data, metricsField));
      });
    } else {
      _metrics.next([]);
      this.popupService.openSnackBar("There isn't reference traces yet to load previous", 'OK');
    }

    return metrics;
  }

  convertToMetricTraces(data: any[], metricsField: MetricsFieldModel): LineChartMetricModel[] {
    let tracesList: LineChartMetricModel[];
    if (metricsField.component === undefined || metricsField.component === '') {
      tracesList = this.getInitMetricsData();

      let position: number = undefined;
      let parsedMetric: any;
      for (let metricTrace of data) {
        parsedMetric = this.convertToMetricTrace(metricTrace._source, metricsField);
        position = this.getMetricPosition(metricTrace._source.component);
        if (position !== undefined && parsedMetric !== undefined) {
          tracesList[position].series.push(parsedMetric);
        }
      }
    } else {
      tracesList = this.getInitMetricsDataComplex(metricsField);
      let parsedMetric: any;
      let position: number = undefined;
      for (let metricTrace of data) {
        parsedMetric = this.convertToMetricTrace(metricTrace._source, metricsField);
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

  /*****************************/
  /***** Metricbeat traces *****/
  /*****************************/
  convertMetricbeatTrace(trace: any, metricsField: MetricsFieldModel): SingleMetricModel {
    let parsedData: SingleMetricModel = undefined;
    let typeArr: string = trace['et_type'].split('_');
    if (trace['@timestamp'] !== '0001-01-01T00:00:00.000Z') {
      if (typeArr[0] in MetricbeatType) {
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

  getLastMetricTraces(index: string, metricsField: MetricsFieldModel, size: number = 10): Observable<LineChartMetricModel[]> {
    let _metrics: Subject<LineChartMetricModel[]> = new Subject<LineChartMetricModel[]>();
    let metrics: Observable<LineChartMetricModel[]> = _metrics.asObservable();

    let terms: any[] = this.getTermsByMetricsField(metricsField);

    this.elasticsearchService.getLast(index, terms, size).subscribe((data) => {
      _metrics.next(this.convertToMetricTraces(data, metricsField));
    });
    return metrics;
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
    timeRange?: ESRangeModel,
    theQuery?: any,
  ): Observable<any> {
    let _obs: Subject<any> = new Subject<any>();
    let obs: Observable<any> = _obs.asObservable();

    let terms: any[] = this.getDynamicTerms(stream, component);
    let filters: string[] = this.getBasicFilterFields().concat(['message', 'units', 'unit']);
    let metricSubtype: string;
    if (metricName && metricName !== '') {
      let metricType: string = metricName.split('.')[0];
      metricSubtype = metricName.split('.')[1];
      filters.push(metricType);
      terms.push({ term: { et_type: metricType } });
    }

    this.elasticsearchService.searchAllByTerm(index, terms, timeRange, theQuery, filters).subscribe((data: any[]) => {
      if (data.length > 0) {
        let convertedData: any;
        let firstElement: any = data[0];
        let firstSource: any = firstElement._source;
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

        if (this.isLogTrace(firstElement)) {
          this.addDynamicLog(_obs, obj, data);
        } else if (this.isMetricsTrace(firstElement)) {
          this.addDynamicComposedMetrics(_obs, obj, data, metricSubtype);
        } else if (this.isAtomicMetricTrace(firstElement)) {
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
    let firstSource: any = firstElement._source;
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
    let firstSource: any = firstElement._source;
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

  isLogTrace(trace: any): boolean {
    return (
      trace._source['stream_type'] !== undefined &&
      trace._source['stream_type'] !== null &&
      trace._source['stream_type'] === 'log'
    );
  }

  isMetricsTrace(trace: any): boolean {
    return (
      trace._source['stream_type'] !== undefined &&
      trace._source['stream_type'] !== null &&
      trace._source['stream_type'] === 'composed_metrics'
    );
  }

  isAtomicMetricTrace(trace: any): boolean {
    return (
      trace._source['stream_type'] !== undefined &&
      trace._source['stream_type'] !== null &&
      trace._source['stream_type'] === 'atomic_metric'
    );
  }

  getBasicFilterFields(streamType?: string): string[] {
    let filters: string[] = ['@timestamp', 'et_type', 'component', 'stream', 'stream_type', 'exec'];

    if (streamType && streamType === 'log') {
      filters.push('message', 'level');
    }

    return filters;
  }

  // TODO Refactor:

  getAggTreeList(agg: any, fields: string[]): any[] {
    let aggTreeList: any[] = [];
    if (fields.length > 0) {
      let field: string = fields[0] + 's';
      if (agg[field] && agg[field].buckets) {
        let buckets: any[] = agg[field].buckets;
        for (let bucket of buckets) {
          let aggObj: any = {};
          aggObj.name = bucket.key;
          aggObj.children = this.getAggTreeList(bucket, fields.slice(1));
          aggTreeList.push(aggObj);
        }
      }
    }
    return aggTreeList;
  }

  getAggTreeOfIndex(index: string, fieldsList: string[], query?: any): Observable<any[]> {
    let _aggTreeSub: Subject<any[]> = new Subject<any[]>();
    let aggTreeObs: Observable<any[]> = _aggTreeSub.asObservable();

    let url: string = this.esUrl + index + '/_search?ignore_unavailable';
    let aggsModel: ESAggsModel = new ESAggsModel();
    aggsModel.initNestedByFieldsList(fieldsList);

    let aggsObj: any = aggsModel.convertToESFormat();
    aggsObj.size = 0;
    if (query) {
      aggsObj.query = query;
    }

    this.elasticsearchService.internalSearch(url, aggsObj).subscribe(
      (data: any) => {
        if (data.aggregations) {
          let aggTreeList: any[] = this.getAggTreeList(data.aggregations, fieldsList);
          _aggTreeSub.next(aggTreeList);
        } else {
          _aggTreeSub.next([]);
        }
      },
      (error) => {
        _aggTreeSub.error(error);
      },
    );

    return aggTreeObs;
  }

  /* ************** */
  /* **** Logs **** */
  /* ************** */

  getLogsTree(tJobExec: TJobExecModel): Observable<any[]> {
    let componentStreamQuery: ESBoolQueryModel = new ESBoolQueryModel();
    let streamTypeTerm: ESTermModel = new ESTermModel();
    streamTypeTerm.name = 'stream_type';
    streamTypeTerm.value = 'log';
    componentStreamQuery.bool.must.termList.push(streamTypeTerm);

    let fieldsList: string[] = ['component', 'stream'];

    return this.getAggTreeOfIndex(tJobExec.monitoringIndex, fieldsList, componentStreamQuery.convertToESFormat());
  }

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

  getMetricsTree(tJobExec: TJobExecModel): Observable<any[]> {
    let componentStreamTypeQuery: ESBoolQueryModel = new ESBoolQueryModel();
    let notStreamTypeTerm: ESTermModel = new ESTermModel();
    notStreamTypeTerm.name = 'stream_type';
    notStreamTypeTerm.value = 'log'; // Must NOT
    componentStreamTypeQuery.bool.mustNot.termList.push(notStreamTypeTerm);

    let nonContainerMetric: ESTermModel = new ESTermModel();
    nonContainerMetric.name = 'et_type';
    nonContainerMetric.value = 'container';
    componentStreamTypeQuery.bool.mustNot.termList.push(nonContainerMetric);

    let fieldsList: string[] = ['component', 'stream', 'et_type'];
    return this.getAggTreeOfIndex(tJobExec.monitoringIndex, fieldsList, componentStreamTypeQuery.convertToESFormat());
  }

  getAllTJobExecMetrics(tJobExec: TJobExecModel, timeRange?: ESRangeModel): Observable<MetricTraces[]> {
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
      this.getAllMetricsByGiven(allMetrics, _metrics, metrics, timeRange);
    });
    return metricsObs;
  }

  getAllMetricsByGiven(
    metricsObjList: ESRabComplexMetricsModel[],
    _metrics: Subject<MetricTraces[]>,
    metrics: MetricTraces[],
    timeRange?: ESRangeModel,
  ): void {
    if (metricsObjList.length > 0) {
      let currentMetric: ESRabComplexMetricsModel = metricsObjList.shift();
      let terms: any[] = this.getDynamicTerms(currentMetric.stream, currentMetric.component);
      let theQuery: object = this.elasticsearchService.getDefaultQueryByRawTermList(terms);

      this.searchAllDynamic(
        currentMetric.monitoringIndex,
        currentMetric.stream,
        currentMetric.component,
        currentMetric.name,
        timeRange,
      ).subscribe(
        (obj: any) => {
          let metricTraces: MetricTraces = new MetricTraces();
          metricTraces.name = currentMetric.component + '-' + currentMetric.stream + '-' + currentMetric.name;
          metricTraces.traces = this.getMetricsObjFromRawSource(obj.data);
          metrics.push(metricTraces);
          this.getAllMetricsByGiven(metricsObjList, _metrics, metrics, timeRange);
        },
        (error: Error) => {
          this.getAllMetricsByGiven(metricsObjList, _metrics, metrics, timeRange);
        },
      );
    } else {
      _metrics.next(metrics);
    }
  }

  // Utils
  getContentListFromRaw(raw: any): any[] {
    let data: any[] = [];
    if (raw && raw.hits && raw.hits.hits) {
      data = raw.hits.hits;
    }
    return data;
  }

  getDataListFromRaw(raw: any, onlySource: boolean = true): any[] {
    let data: any[] = [];
    let sourcesList: any[] = this.getContentListFromRaw(raw);
    for (let elem of sourcesList) {
      if (!onlySource) {
        for (let key in elem) {
          if (key !== '_source') {
            elem._source[key] = elem[key]; // Add key and value into source
          }
        }
      }
      data.push(elem._source);
    }

    return data;
  }

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

  getRangeByGiven(
    from: Date | string,
    to: Date | string,
    includedFrom: boolean = true,
    includedTo: boolean = true,
  ): ESRangeModel {
    let range: ESRangeModel = new ESRangeModel();
    range.field = '@timestamp';

    if (includedFrom) {
      range.gte = from;
    } else {
      range.gt = from;
    }

    if (includedTo) {
      range.lte = to;
    } else {
      range.lt = to;
    }

    return range;
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
