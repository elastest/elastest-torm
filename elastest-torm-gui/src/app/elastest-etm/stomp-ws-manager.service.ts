/**
 * Created by frdiaz on 19/04/2017.
 */
import { EventEmitter, Injectable } from '@angular/core';
import { StompService } from './stomp.service';
import { Subject } from 'rxjs/Subject';
import { Http } from "@angular/http";
import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';


@Injectable()
export class StompWSManager {

  private wsConf = {
    host: '/rabbitMq',
    debug: true,
    queue: { 'init': false },
    heartbeatOut: 5000,
    heartbeatIn: 5000
  }

  subscription: any;
  subscriptions: Map<string, any>;

  testTraces: string[] = [];
  sutTraces: string[] = [];

  endExecution: boolean = false;

  private _testLogsSource = new Subject<string>();
  testLogs$ = this._testLogsSource.asObservable();

  private _sutLogsSource = new Subject<string>();
  sutLogs$ = this._sutLogsSource.asObservable();

  private _testMetricsSource = new Subject<string>();
  testMetrics$ = this._testMetricsSource.asObservable();

  private _sutMetricsSource = new Subject<string>();
  sutMetrics$ = this._sutMetricsSource.asObservable();

  constructor(private stomp: StompService, private http: Http) {
    this.subscriptions = new Map<string, any>();
  }

  configWSConnection(host: string) {
    this.wsConf.host = host;
    this.stomp.configure(this.wsConf);
  }

  startWsConnection() {
    /**sub
     * Start connection
     * @return {Promise} if resolved
     */
    this.stomp.startConnect().then(() => {
      console.log('connected');
      console.log('Url Ws: ' + this.stomp.getSessionWsId());

    });
  }

  /**
   * Disconnect
   * @return {Promise} if resolved
   */
  disconnectWSConnection() {
    this.stomp.disconnect().then(() => {
      console.log('Connection closed')
    });
  }

  subscribeToQueDestination(destination: string, callbackFunction: any) {
    this.subscriptions.set(destination + this.stomp.getSessionWsId, this.stomp.subscribe('/queue/' + destination, callbackFunction, { auto_delete: true }));
  }

  subscribeToTopicDestination(destination: string, callbackFunction: any) {
    this.subscriptions.set(destination + this.stomp.getSessionWsId, this.stomp.subscribe('/topic/' + destination, callbackFunction));
  }

  ususcribeWSDestination(destination: string) {
    this.subscriptions.forEach((value, key) => {
      console.log("UNSUSCRIBE TOPICS", key, value);
      this.stomp.unsubscribe(value);
    });

  }

  sendWSMessage() {
    /**
     * Send message.
     * @param {string} destination: send destination.
     * @param {object} body: a object that sends.
     * @param {object} headers: optional headers.
     */
    this.stomp.send('/topic/logs', { 'data': 'data' });
  }

  // Response
  public testMetricsResponse = (data) => {
    this._testMetricsSource.next(data);
  }

  public testLogResponse = (data) => {
    console.log('data:',data.message);
    this._testLogsSource.next(data.message);
  }

  public sutMetricsResponse = (data) => {
    this._sutMetricsSource.next(data);
  }

  public sutLogResponse = (data) => {
    this._sutLogsSource.next(data.message);
  }

}
