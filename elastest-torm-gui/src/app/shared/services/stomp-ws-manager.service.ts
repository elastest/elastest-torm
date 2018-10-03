import { Injectable } from '@angular/core';
import { ConfigurationService } from '../../config/configuration-service.service';
import { Http } from '@angular/http';
import { StompConfig, StompRService, StompState } from '@stomp/ng2-stompjs';
import { Subscription, Observable } from 'rxjs';
import { StompHeaders } from '@stomp/ng2-stompjs/src/stomp-headers';
import { Message } from '@stomp/stompjs';

@Injectable()
export class StompWSManager {
  private wsConf: StompConfig;
  wsPath: string = '/rabbitMq';

  subscriptions: Map<string, Subscription>;

  endExecution: boolean = false;

  constructor(private stompRService: StompRService, private http: Http, private configurationService: ConfigurationService) {
    this.subscriptions = new Map<string, any>();

    this.wsConf = new StompConfig();
    this.wsConf.debug = false;
    this.wsConf.heartbeat_out = 10000;
    this.wsConf.heartbeat_in = 10000;
    this.wsConf.reconnect_delay = 5000;

    this.wsConf.url = this.configurationService.configModel.hostWsServer + this.wsPath;
  }

  public configWSConnection(host?: string): void {
    if (host !== undefined) {
      this.wsConf.url = this.configurationService.configModel.hostWsServer + host;
    }

    this.stompRService.config = this.wsConf;
  }

  public subscribeToStompStatus(): Observable<string> {
    return this.stompRService.state.map((state: number) => StompState[state]);
  }

  public startConnect(): void {
    if (this.stompRService.config === null) {
      throw Error('Configuration required!');
    }

    // Prepare Client
    this.stompRService.initAndConnect();
  }

  /**
   * Subscribe
   */
  public subscribe(destination: string, callback: any, headers?: StompHeaders): Subscription {
    headers = headers || {};
    return this.stompRService
      .subscribe(destination, headers)

      .subscribe((message: Message) => {
        try {
          callback(JSON.parse(message.body), message.headers);
        } catch (e) {
          console.log(e);
        }
      });
  }

  /**
   * Send
   */
  public send(destination: string, body: any, headers?: StompHeaders): void {
    let message: string = JSON.stringify(body);
    headers = headers || {};
    this.stompRService.publish(destination, message, headers);
  }

  /**
   * Unsubscribe
   */
  public unsubscribe(subscription: Subscription): any {
    return subscription.unsubscribe();
  }

  /**
   * Connect
   */

  startWsConnection(): void {
    this.startConnect();

    this.subscribeToStompStatus().subscribe(
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
    this.stompRService.disconnect();
  }

  subscribeToTopicDestination(destination: string, callbackFunction: any, exchange?: string): void {
    if (!this.subscriptions.has(destination)) {
      console.log('SUBSCRIBED TO', destination);
      this.subscriptions.set(destination, this.subscribe('/topic/' + destination, callbackFunction));
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
      this.unsubscribe(value);
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
    this.send(destination, { data: 'data' });
  }
}
