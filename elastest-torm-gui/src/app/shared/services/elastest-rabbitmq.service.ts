import { StompWSManager } from './stomp-ws-manager.service';
import { TJobExecModel } from '../../elastest-etm/tjob-exec/tjobExec-model';
import { Injectable } from '@angular/core';
import { Subject } from 'rxjs/Rx';
import { DefaultESFieldModel, components, defaultStreamMap } from '../defaultESData-model';

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

    public createSubject(streamType: string, component: string, stream: string) {
        let name: string = this.getSubjectName(component, stream, streamType);
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

    public createAndSubscribeToTopic(tjobExecution: TJobExecModel, streamType: string, component: string, stream: string) {
        let topicPrefix: string = component + '.' + stream;
        this.stompWSManager.subscribeToTopicDestination(topicPrefix + '.' + tjobExecution.id + '.' + streamType, this.dynamicObsResponse);

        let obsName: string = this.getSubjectName(component, stream, streamType);
        return this.subjectMap.get(obsName);
    }

    public unsuscribeFromTopic(tjobExecution: TJobExecModel, streamType: string, component: string, stream: string) {
        let topicPrefix: string = component + '.' + stream;
        let destination: string = topicPrefix + '.' + tjobExecution.id + '.' + streamType;
        this.stompWSManager.unsubscribeSpecificWSDestination(destination);
    }


    // Response
    public dynamicObsResponse = (data) => {
        let obs: Subject<string> = this.getSubjectFromData(data);
        let trace: any = this.adaptToStreamType(data);
        if (trace !== undefined) {
            obs.next(trace);
        }
    }

    // Other functions

    getSubjectNameFromData(data: any) {
        return this.getSubjectName(data['component'], data['stream'], data['stream_type']);
    }

    getSubjectName(component: string, stream: string, streamType: string) {
        return component + '.' + stream + '.' + streamType;
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

    adaptToStreamType(data: any) {
        let trace: any;
        if (data['stream_type'] === 'log' && data.message !== undefined) {
            trace = {
                'timestamp': data['@timestamp'],
                'message': data.message
            };
        } else if (data['stream_type'] === 'composed_metrics' || data['stream_type'] === 'atomic_metric') {
            trace = data;
        }
        return trace;
    }

    getDataFromSubjectName(name: string) {
        let splited: string[] = name.split('.');
        let data: any = {
            component: splited[0],
            stream: splited[1],
            streamType: splited[2],
        };
        return data;
    }
}