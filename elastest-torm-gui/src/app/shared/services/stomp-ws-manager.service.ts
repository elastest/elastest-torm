import { Injectable } from '@angular/core';
import { ConfigurationService } from '../../config/configuration-service.service';
import { StompService } from './stomp.service';
import { Http } from '@angular/http';

@Injectable()
export class StompWSManager {
  private wsConf = {
    host: '/rabbitMq',
    debug: false,
    queue: { init: false },
    heartbeatOut: 10000,
    heartbeatIn: 10000,
  };

  subscriptions: Map<string, any>;

  endExecution: boolean = false;

  constructor(private stomp: StompService, private http: Http, private configurationService: ConfigurationService) {
    this.subscriptions = new Map<string, any>();
    this.wsConf.host = this.configurationService.configModel.hostWsServer + this.wsConf.host;
  }

  configWSConnection(host?: string): void {
    if (host !== undefined) {
      this.wsConf.host = this.configurationService.configModel.hostWsServer + host;
    }

    this.stomp.configure(this.wsConf);
  }

  startWsConnection(): void {
    /**sub
     * Start connection
     * @return {Promise} if resolved
     */
    this.stomp.startConnect().then(() => {
      console.log('Connected');
    });
  }

  /**
   * Disconnect
   * @return {Promise} if resolved
   */
  disconnectWSConnection(): void {
    this.stomp.disconnect().then(() => {
      console.log('Connection closed');
    });
  }

  subscribeToTopicDestination(destination: string, callbackFunction: any, exchange?: string): void {
    if (!this.subscriptions.has(destination)) {
      console.log('SUBSCRIBE TO', destination);
      this.subscriptions.set(destination, this.stomp.subscribe('/topic/' + destination, callbackFunction));
    }
  }

  unsubscribeWSDestination(): void {
    this.subscriptions.forEach((value, key) => {
      console.log('UNSUBSCRIBE TOPICS', key, value);
      this.stomp.unsubscribe(value);
      this.subscriptions.delete(key);
    });
  }

  unsubscribeSpecificWSDestination(key: string): void {
    let value = this.subscriptions.get(key);
    if (value !== undefined) {
      this.stomp.unsubscribe(value);
      this.subscriptions.delete(key);
    }
  }

  sendWSMessage(destination: string): void {
    /**
     * Send message.
     * @param {string} destination: send destination.
     * @param {object} body: a object that sends.
     * @param {object} headers: optional headers.
     */
    this.stomp.send(destination, { data: 'data' });
  }
}
