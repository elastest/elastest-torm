import { StompWSManager } from './stomp-ws-manager.service';
import { TJobExecModel } from '../../elastest-etm/tjob-exec/tjobExec-model';
import { Injectable } from '@angular/core';
import { Subject } from 'rxjs/Rx';
import { DefaultESFieldModel, componentTypes, defaultStreamMap } from '../defaultESData-model';

@Injectable()
export class ElastestRabbitmqService {
    public subjectMap: Map<string, Subject<string>>;

    constructor(private stompWSManager: StompWSManager) {
        this.subjectMap = new Map<string, Subject<string>>();
        this.initDefaultSubjects();
    }

    configWSConnection() { this.stompWSManager.configWSConnection(); }
    startWsConnection() { this.stompWSManager.startWsConnection(); }

    unsubscribeWSDestination() {
        this.stompWSManager.unsubscribeWSDestination();
    }

    // Create Subjects

    public initDefaultSubjects() {
        for (let type in defaultStreamMap) {
            this.createSubject(type, 'test', defaultStreamMap[type])
            this.createSubject(type, 'sut', defaultStreamMap[type])
        }
    }

    public createSubject(traceType: string, componentType: string, stream: string) {
        let name: string = this.getSubjectName(componentType, stream, traceType);
        if (this.existObs(name)) {
            return this.getSubjectByName(name);
        }

        let _dynamicObs: Subject<string> = new Subject<string>();
        this.subjectMap.set(name, _dynamicObs);

        return _dynamicObs;
    }

    // Create and Subscribe

    public createAndSubscribeToGenericTopics(tjobExecution: TJobExecModel) {
        let withSut: boolean = tjobExecution.tJob.hasSut();
        for (let type in defaultStreamMap) {
            this.createAndSubscribeToTopic(tjobExecution, type, 'test', defaultStreamMap[type])

            if (withSut) {
                this.createAndSubscribeToTopic(tjobExecution, type, 'sut', defaultStreamMap[type])
            }
        }
    }

    public createAndSubscribeToTopic(tjobExecution: TJobExecModel, traceType: string, componentType: string, stream: string) {
        let topicPrefix: string = componentType + '.' + stream;
        this.stompWSManager.subscribeToTopicDestination(topicPrefix + '.' + tjobExecution.id + '.' + traceType, this.dynamicObsResponse);

        let obsName: string = this.getSubjectName(componentType, stream, traceType);
        return this.subjectMap.get(obsName);
    }

    public unsuscribeFromTopic(tjobExecution: TJobExecModel, traceType: string, componentType: string, stream: string) {
        let topicPrefix: string = componentType + '.' + stream;
        let destination: string = topicPrefix + '.' + tjobExecution.id + '.' + traceType;
        this.stompWSManager.unsubscribeSpecificWSDestination(destination);
    }


    // Response
    public dynamicObsResponse = (data) => {
        let obs: Subject<string> = this.getSubjectFromData(data);
        let trace: any = this.adaptToTraceType(data);
        if (trace !== undefined) {
            obs.next(trace);
        }
    }

    // Other functions

    getSubjectNameFromData(data: any) {
        return this.getSubjectName(data['component_type'], data['stream'], data['trace_type']);
    }

    getSubjectName(componentType: string, stream: string, traceType: string) {
        return componentType + '.' + stream + '.' + traceType;
    }

    getSubjectFromData(data: any) {
        let name: string = this.getSubjectNameFromData(data);
        return this.subjectMap.get(name);
    }

    getSubjectByName(name: string) {
        if (this.existObs(name)) {
            return this.subjectMap.get(name);
        }
        return undefined;
    }

    existObs(name: string): boolean {
        return this.subjectMap.has(name);
    }

    adaptToTraceType(data: any) {
        let trace: any;
        if (data['trace_type'] === 'log' && data.message !== undefined) {
            trace = {
                'timestamp': data['@timestamp'],
                'message': data.message
            };
        } else if (data['trace_type'] === 'composed_metrics') {
            trace = data;
        }
        return trace;
    }

    getDataFromSubjectName(name: string) {
        let splited: string[] = name.split('.');
        let data: any = {
            componentType: splited[0],
            stream: splited[1],
            traceType: splited[2],
        };
        return data;
    }
}