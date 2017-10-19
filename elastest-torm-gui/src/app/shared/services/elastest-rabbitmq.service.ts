import { StompWSManager } from './stomp-ws-manager.service';
import { TJobExecModel } from '../../elastest-etm/tjob-exec/tjobExec-model';
import { Injectable } from '@angular/core';
import { Subject } from 'rxjs/Rx';

@Injectable()
export class ElastestRabbitmqService {
    private _testLogsSource = new Subject<string>();
    testLogs$ = this._testLogsSource.asObservable();

    private _sutLogsSource = new Subject<string>();
    sutLogs$ = this._sutLogsSource.asObservable();

    private _testMetricsSource = new Subject<string>();
    testMetrics$ = this._testMetricsSource.asObservable();

    private _sutMetricsSource = new Subject<string>();
    sutMetrics$ = this._sutMetricsSource.asObservable();

    private observableMap: Map<string, Subject<string>>;

    constructor(private stompWSManager: StompWSManager) {
        this.observableMap = new Map<string, any>();
    }

    configWSConnection() { this.stompWSManager.configWSConnection(); }
    startWsConnection() { this.stompWSManager.startWsConnection(); }
    unsubscribeWSDestination() {
        this.stompWSManager.unsubscribeWSDestination();
    }

    // Response
    public testMetricsResponse = (data) => {
        this._testMetricsSource.next(data);
    }

    public testLogResponse = (data) => {
        this._testLogsSource.next(this.adaptToTraceType(data));
    }

    public sutMetricsResponse = (data) => {
        this._sutMetricsSource.next(data);
    }

    public sutLogResponse = (data) => {
        this._sutLogsSource.next(this.adaptToTraceType(data));
    }

    public dynamicObsResponse = (data) => {
        let obs = this.getObservableFromData(data);
        obs.next(this.adaptToTraceType(data));
    }

    adaptToTraceType(data: any) {
        let trace: any;
        if (data['trace_type'] === 'log') {
            trace = {
                'timestamp': data['@timestamp'],
                'message': data.message
            };
        } else if (data['trace_type'] === 'metrics') {
            trace = data;
        }

        return trace;
    }

    // Others
    public createAndSubscribe(tjobExecution: TJobExecModel) {
        let withSut: boolean = tjobExecution.tJob.hasSut();

        this.stompWSManager.subscribeToQueDestination('q-' + tjobExecution.id + '-test-log', this.testLogResponse);
        this.stompWSManager.subscribeToQueDestination('q-' + tjobExecution.id + '-test-metrics', this.testMetricsResponse);
        if (withSut) {
            this.stompWSManager.subscribeToQueDestination('q-' + tjobExecution.id + '-sut-log', this.sutLogResponse);
            this.stompWSManager.subscribeToQueDestination('q-' + tjobExecution.id + '-sut-metrics', this.sutMetricsResponse);
        }
    }

    public createAndSubscribeToTopic(tjobExecution: TJobExecModel) {
        let withSut: boolean = tjobExecution.tJob.hasSut();

        this.stompWSManager.subscribeToTopicDestination('test.default_log.' + tjobExecution.id + '.log', this.testLogResponse);
        this.stompWSManager.subscribeToTopicDestination('test.beats_metrics.' + tjobExecution.id + '.metrics', this.testMetricsResponse);
        if (withSut) {
            this.stompWSManager.subscribeToTopicDestination('sut.default_log.' + tjobExecution.id + '.log', this.sutLogResponse);
            this.stompWSManager.subscribeToTopicDestination('sut.beats_metrics.' + tjobExecution.id + '.metrics', this.sutMetricsResponse);
        }

    }

    public createAndSubscribeToTopicDynamically(tjobExecution: TJobExecModel, traceType: string, componentType: string, infoId: string) {
        let name: string = componentType + '.' + infoId;
        let _dynamicObs = new Subject<string>();
        let dynamicObs$ = _dynamicObs.asObservable();
        this.observableMap.set(name, _dynamicObs)

        this.stompWSManager.subscribeToTopicDestination(name + '.' + tjobExecution.id + '.' + traceType, this.dynamicObsResponse);

        return dynamicObs$;
    }

    getObservableFromData(data: any) {
        let name: string = data['component_type'] + '.' + data['info_id'];
        return this.observableMap.get(name);
    }
}