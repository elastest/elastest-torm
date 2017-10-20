import { StompWSManager } from './stomp-ws-manager.service';
import { TJobExecModel } from '../../elastest-etm/tjob-exec/tjobExec-model';
import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs/Rx';
import { DefaultESFieldModel, componentTypes, defaultInfoIdMap } from '../defaultESData-model';

@Injectable()
export class ElastestRabbitmqService {
    public subjectMap: Map<string, Subject<string>>;
    public observableMap: Map<string, Observable<string>>;

    constructor(private stompWSManager: StompWSManager) {
        this.subjectMap = new Map<string, Subject<string>>();
        this.observableMap = new Map<string, Observable<string>>();
        this.initDefaultObservables();
    }

    configWSConnection() { this.stompWSManager.configWSConnection(); }
    startWsConnection() { this.stompWSManager.startWsConnection(); }

    unsubscribeWSDestination() {
        this.stompWSManager.unsubscribeWSDestination();
    }

    // Create Observables

    public initDefaultObservables() {
        for (let type in defaultInfoIdMap) {
            this.createObservable(type, 'test', defaultInfoIdMap[type])
            this.createObservable(type, 'sut', defaultInfoIdMap[type])
        }
    }

    public createObservable(traceType: string, componentType: string, infoId: string) {
        let name: string = componentType + '.' + infoId + '.' + traceType;
        let _dynamicObs = new Subject<string>();
        this.subjectMap.set(name, _dynamicObs);

        let dynamicObs$: Observable<string> = this.subjectMap.get(name).asObservable();
        this.observableMap.set(name, dynamicObs$);

        return dynamicObs$;
    }


    // Create and Subscribe

    public createAndSubscribeToGenericTopics(tjobExecution: TJobExecModel) {
        let withSut: boolean = tjobExecution.tJob.hasSut();
        for (let type in defaultInfoIdMap) {
            this.createAndSubscribeToTopic(tjobExecution, type, 'test', defaultInfoIdMap[type])

            if (withSut) {
                this.createAndSubscribeToTopic(tjobExecution, type, 'sut', defaultInfoIdMap[type])
            }
        }
    }

    public createAndSubscribeToTopic(tjobExecution: TJobExecModel, traceType: string, componentType: string, infoId: string) {
        let topicPrefix: string = componentType + '.' + infoId;
        let obsName: string = componentType + '.' + infoId + '.' + traceType;
        let dynamicObs$: Observable<string> = this.observableMap.get(obsName);
        this.stompWSManager.subscribeToTopicDestination(topicPrefix + '.' + tjobExecution.id + '.' + traceType, this.dynamicObsResponse);

        return dynamicObs$;
    }

    public unsuscribeFromTopic(tjobExecution: TJobExecModel, traceType: string, componentType: string, infoId: string) {
        let topicPrefix: string = componentType + '.' + infoId;
        let destination: string = topicPrefix + '.' + tjobExecution.id + '.' + traceType;
        this.stompWSManager.unsubscribeSpecificWSDestination(destination);
    }


    // Response
    public dynamicObsResponse = (data) => {
        let obs: Subject<string> = this.getObservableFromData(data);
        let trace: any = this.adaptToTraceType(data);
        if (trace !== undefined) {
            obs.next(trace);
        }
    }

    // Other functions

    getObservableNameFromData(data: any) {
        return data['component_type'] + '.' + data['info_id'] + '.' + data['trace_type'];
    }

    getObservableFromData(data: any) {
        let name: string = this.getObservableNameFromData(data);
        return this.subjectMap.get(name);
    }

    adaptToTraceType(data: any) {
        let trace: any;
        if (data['trace_type'] === 'log' && data.message !== undefined) {
            trace = {
                'timestamp': data['@timestamp'],
                'message': data.message
            };
        } else if (data['trace_type'] === 'metrics') {
            trace = data;
        }
        return trace;
    }

    getDataFromObsName(name: string) {
        let splited: string[] = name.split('.');
        let data: any = {
            componentType: splited[0],
            infoId: splited[1],
            traceType: splited[2],
        };
        return data;
    }
}