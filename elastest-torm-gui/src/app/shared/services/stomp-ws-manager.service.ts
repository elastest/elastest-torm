import { Injectable } from '@angular/core';
import { ConfigurationService } from '../../config/configuration-service.service';
import { StompService } from './stomp.service';
import { Http } from '@angular/http';
import { StompConfig } from '@stomp/ng2-stompjs';
import { Subscription } from 'rxjs';

@Injectable()
export class StompWSManager {
  private wsConf: StompConfig;
  wsPath: string = '/rabbitMq';

  subscriptions: Map<string, Subscription>;

  endExecution: boolean = false;

  constructor(private stomp: StompService, private http: Http, private configurationService: ConfigurationService) {
    this.wsConf = new StompConfig();
    this.wsConf.debug = false;
    this.wsConf.heartbeat_out = 10000;
    this.wsConf.heartbeat_in = 10000;
    this.wsConf.reconnect_delay = 5000;

    this.subscriptions = new Map<string, any>();
    this.wsConf.url = this.configurationService.configModel.hostWsServer + this.wsPath;
  }

  configWSConnection(host?: string): void {
    if (host !== undefined) {
      this.wsConf.url = this.configurationService.configModel.hostWsServer + host;
    }

    this.stomp.configure(this.wsConf);
  }

  startWsConnection(): void {
    /**sub
     * Start connection
     */
    this.stomp.startConnect();

    this.stomp.subscribeToStompStatus().subscribe(
      (status: string) => {
        console.log(`Stomp connection status: ${status}`);
      },
      (error: Error) => console.log(error),
    );
  }

  /**
   * Disconnect
   */
  disconnectWSConnection(): void {
    this.stomp.disconnect();
  }

  subscribeToTopicDestination(destination: string, callbackFunction: any, exchange?: string): void {
    if (!this.subscriptions.has(destination)) {
      console.log('SUBSCRIBED TO', destination);
      this.subscriptions.set(destination, this.stomp.subscribe('/topic/' + destination, callbackFunction));
    }
  }

  unsubscribeWSDestination(): void {
    console.log('UNSUBSCRIBING TO ALL:');

    this.subscriptions.forEach((value, key) => {
      this.unsubscribeSpecificWSDestination(key);
    });
  }

  unsubscribeSpecificWSDestination(key: string): void {
    let value: Subscription = this.subscriptions.get(key);
    if (value !== undefined) {
      console.log('UNSUBSCRIBED TO', key);
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
