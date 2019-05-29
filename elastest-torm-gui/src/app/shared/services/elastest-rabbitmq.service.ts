import { StompWSManager } from './stomp-ws-manager.service';
import { Injectable } from '@angular/core';
import { Subject } from 'rxjs/Rx';

@Injectable()
export class ElastestRabbitmqService {
  public subjectMap: Map<string, Subject<any>>;

  constructor(private stompWSManager: StompWSManager) {
    this.subjectMap = new Map<string, Subject<any>>();
  }

  public configWSConnection(): void {
    this.stompWSManager.configWSConnection();
  }
  public startWsConnection(): void {
    this.stompWSManager.startWsConnection();
  }

  public unsubscribeWSDestination(): void {
    this.stompWSManager.unsubscribeWSDestination();
  }

  // Create Subjects

  public createSubject(streamType: string, component: string, stream: string): Subject<any> {
    let name: string = this.getSubjectName(component, stream, streamType);
    if (this.existObs(name)) {
      return this.getSubjectByName(name);
    }

    let _dynamicObs: Subject<any> = new Subject<any>();
    this.subjectMap.set(name, _dynamicObs);

    return _dynamicObs;
  }

  // Create and Subscribe

  public createAndSubscribeToTopic(exec: string, streamType: string, component: string, stream: string): Subject<any> {
    let topicPrefix: string = component + '.' + stream;
    this.stompWSManager.subscribeToTopicDestination(topicPrefix + '.' + exec + '.' + streamType, this.dynamicObsResponse);

    let obsName: string = this.getSubjectName(component, stream, streamType);
    return this.subjectMap.get(obsName);
  }

  public unsuscribeFromTopic(exec: string, streamType: string, component: string, stream: string): void {
    let destination: string = this.getDestinationFromData(exec, streamType, component, stream);
    this.stompWSManager.unsubscribeSpecificWSDestination(destination);
  }

  public getDestinationFromData(exec: string, streamType: string, component: string, stream: string): string {
    let topicPrefix: string = component + '.' + stream;
    return topicPrefix + '.' + exec + '.' + streamType;
  }

  // Response
  public dynamicObsResponse = (data) => {
    let obs: Subject<any> = this.getSubjectFromData(data);
    let trace: any = this.adaptToStreamType(data);
    if (trace !== undefined) {
      obs.next(trace);
    }
  };

  // Other functions

  getSubjectNameFromData(data: any): string {
    return this.getSubjectName(data['component'], data['stream'], data['stream_type']);
  }

  getSubjectName(component: string, stream: string, streamType: string): string {
    return component + '.' + stream + '.' + streamType;
  }

  getSubjectFromData(data: any) {
    let name: string = this.getSubjectNameFromData(data);
    return this.subjectMap.get(name);
  }

  getSubjectByName(name: string): Subject<any> {
    if (this.existObs(name)) {
      return this.subjectMap.get(name);
    }
    return undefined;
  }

  existObs(name: string): boolean {
    return this.subjectMap.has(name);
  }

  adaptToStreamType(data: any): any {
    let trace: any;
    if (data['stream_type'] === 'log' && data.message !== undefined) {
      let timestamp: string = data['@timestamp'];
      if (timestamp === undefined || timestamp === null) {
        timestamp = data['timestamp'];
      }
      trace = {
        timestamp: timestamp,
        message: data.message,
        level: data.level,
        stream: data.stream,
        stream_type: data.stream_type,
        exec: data.exec,
        component: data.component,
      };
    } else if (data['stream_type'] === 'composed_metrics' || data['stream_type'] === 'atomic_metric') {
      trace = data;
    }
    return trace;
  }

  getDataFromSubjectName(name: string): any {
    let splited: string[] = name.split('.');
    let data: any = {
      component: splited[0],
      stream: splited[1],
      streamType: splited[2],
    };
    return data;
  }
}
