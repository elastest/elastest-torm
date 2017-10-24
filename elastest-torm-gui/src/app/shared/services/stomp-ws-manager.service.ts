import { EventEmitter, Injectable } from '@angular/core';
import { ConfigurationService } from '../../config/configuration-service.service';
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
    heartbeatOut: 10000,
    heartbeatIn: 10000
  }

  subscriptions: Map<string, any>;

  endExecution: boolean = false;

  constructor(private stomp: StompService, private http: Http, private configurationService: ConfigurationService) {
    this.subscriptions = new Map<string, any>();
    this.wsConf.host = this.configurationService.configModel.hostWsServer + this.wsConf.host;
  }

  configWSConnection(host?: string) {
    if (host !== undefined) {
      this.wsConf.host = this.wsConf.host = this.configurationService.configModel.hostWsServer + host;
    }

    this.stomp.configure(this.wsConf);
  }

  startWsConnection() {
    /**sub
     * Start connection
     * @return {Promise} if resolved
     */
    this.stomp.startConnect().then(() => {
      console.log('connected');
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
    this.subscriptions.set(destination, this.stomp.subscribe('/queue/' + destination, callbackFunction, { auto_delete: true }));
  }

  subscribeToTopicDestination(destination: string, callbackFunction: any, exchange?: string) {
    if (!(destination in this.subscriptions)) {
      this.subscriptions.set(destination, this.stomp.subscribe('/topic/' + destination, callbackFunction));
    }
  }

  unsubscribeWSDestination() {
    this.subscriptions.forEach((value, key) => {
      console.log("UNSUBSCRIBE TOPICS", key, value);
      this.stomp.unsubscribe(value);
      this.subscriptions.delete(key);
    });

  }

  unsubscribeSpecificWSDestination(key: string) {
    let value = this.subscriptions.get(key);
    if (value !== undefined) {
      this.stomp.unsubscribe(value);
      this.subscriptions.delete(key);
    }
  }

  sendWSMessage(destination: string) {
    /**
     * Send message.
     * @param {string} destination: send destination.
     * @param {object} body: a object that sends.
     * @param {object} headers: optional headers.
     */
    this.stomp.send(destination, { 'data': 'data' });
  }
}