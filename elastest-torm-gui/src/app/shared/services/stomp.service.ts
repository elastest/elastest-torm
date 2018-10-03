import { Injectable } from '@angular/core';
import { StompRService, StompConfig, StompState } from '@stomp/ng2-stompjs';
import { StompHeaders } from '@stomp/ng2-stompjs/src/stomp-headers';
import { Message } from '@stomp/stompjs';
import { Subscription, Observable } from 'rxjs';

@Injectable()
export class StompService {
  constructor(private stompRService: StompRService) {}

  /**
   * Configure
   */
  public configure(config: StompConfig): void {
    this.stompRService.config = config;
  }

  public subscribeToStompStatus(): Observable<string> {
    return this.stompRService.state.map((state: number) => StompState[state]);
  }

  /**
   * Try to establish connection to server
   */
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
   * Unsubscribe
   */
  public unsubscribe(subscription: Subscription): any {
    return subscription.unsubscribe();
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
   * Disconnect stomp
   */
  public disconnect(): void {
    this.stompRService.disconnect();
  }
}
