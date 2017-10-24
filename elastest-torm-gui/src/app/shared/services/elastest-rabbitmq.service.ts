import { StompWSManager } from './stomp-ws-manager.service';
import { TJobExecModel } from '../../elastest-etm/tjob-exec/tjobExec-model';
import { Injectable } from '@angular/core';
import { Subject } from 'rxjs/Rx';
import { DefaultESFieldModel, componentTypes, defaultInfoIdMap } from '../defaultESData-model';

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
        for (let type in defaultInfoIdMap) {
            this.createSubject(type, 'test', defaultInfoIdMap[type])
            this.createSubject(type, 'sut', defaultInfoIdMap[type])
        }
    }

    public createSubject(traceType: string, componentType: string, infoId: string) {
        let name: string = this.getSubjectName(componentType, infoId, traceType);
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
        for (let type in defaultInfoIdMap) {
            this.createAndSubscribeToTopic(tjobExecution, type, 'test', defaultInfoIdMap[type])

            if (withSut) {
                this.createAndSubscribeToTopic(tjobExecution, type, 'sut', defaultInfoIdMap[type])
            }
        }
    }

    public createAndSubscribeToTopic(tjobExecution: TJobExecModel, traceType: string, componentType: string, infoId: string) {
        let topicPrefix: string = componentType + '.' + infoId;
        this.stompWSManager.subscribeToTopicDestination(topicPrefix + '.' + tjobExecution.id + '.' + traceType, this.dynamicObsResponse);

        let obsName: string = this.getSubjectName(componentType, infoId, traceType);
        return this.subjectMap.get(obsName);
    }

    public unsuscribeFromTopic(tjobExecution: TJobExecModel, traceType: string, componentType: string, infoId: string) {
        let topicPrefix: string = componentType + '.' + infoId;
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
        return this.getSubjectName(data['component_type'], data['info_id'], data['trace_type']);
    }

    getSubjectName(componentType: string, infoId: string, traceType: string) {
        return componentType + '.' + infoId + '.' + traceType;
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
        return (name in this.subjectMap);
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

    getDataFromSubjectName(name: string) {
        let splited: string[] = name.split('.');
        let data: any = {
            componentType: splited[0],
            infoId: splited[1],
            traceType: splited[2],
        };
        return data;
    }
}