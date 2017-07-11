import { StompWSManager } from '../../elastest-etm/stomp-ws-manager.service';
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

    constructor(private stompWSManager: StompWSManager) { }

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
        this._testLogsSource.next(data.message);
    }

    public sutMetricsResponse = (data) => {
        this._sutMetricsSource.next(data);
    }

    public sutLogResponse = (data) => {
        this._sutLogsSource.next(data.message);
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

        this.stompWSManager.subscribeToTopicDestination('test.' + tjobExecution.id + '.log', this.testLogResponse);
        this.stompWSManager.subscribeToTopicDestination('test.' + tjobExecution.id + '.metrics', this.testMetricsResponse);
        if (withSut) {
            this.stompWSManager.subscribeToTopicDestination('sut.' + tjobExecution.id + '.log', this.sutLogResponse);
            this.stompWSManager.subscribeToTopicDestination('sut.' + tjobExecution.id + '.metrics', this.sutMetricsResponse);
        }
    }
}