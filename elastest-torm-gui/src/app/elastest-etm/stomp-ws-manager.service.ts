/**
 * Created by frdiaz on 19/04/2017.
 */
import { EventEmitter, Injectable } from '@angular/core';
import { StompService } from 'ng2-stomp-service';
import { BehaviorSubject } from 'rxjs/BehaviorSubject';
import { Http } from "@angular/http";
import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';


@Injectable()
export class StompWSManager {

  private wsConf = {
    host: '/rabbitMq',
    debug: true,
    queue: { 'init': false }
  }

  private subscription: any;

  endExecution: boolean = false;

  urlNoVNCClient: string[] = [];
  timer: Observable<number>;
  timer_subscription: Subscription;
  public testDataUpdated: EventEmitter<any>;
  public sutDataUpdated: EventEmitter<any>;


  constructor(private stomp: StompService, private http: Http) {
    this.testDataUpdated = new EventEmitter();
    this.sutDataUpdated = new EventEmitter();
  }

  configWSConnection(host: string) {
    this.wsConf.host = host;
    this.urlNoVNCClient.push('');
    this.stomp.configure(this.wsConf);
  }

  startWsConnection() {
    /**sub
     * Start connection
     * @return {Promise} if resolved
     */
    this.stomp.startConnect().then(() => {
      console.log('connected');

      /**
       * Subscribe.
       * @param {string} destination: subscibe destination.
       * @param {Function} callback(message,headers): called after server response.
       * @param {object} headers: optional headers.
       */
      // this.subscription = this.stomp.subscribe('/queue/q-67-test-metrics', this.response);
      // this.subscription = this.stomp.subscribe('/queue/urlsVNC', this.loadUrl);

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

  subscribeWSDestinationTest(destination: string) {
    /**
     * Subscribe.
     * @param {string} destination: subscibe destination.
     * @param {Function} callback(message,headers): called after server response.
     * @param {object} headers: optional headers.
     */
    this.subscription = this.stomp.subscribe('/queue/' + destination, this.testResponse);
  }

    subscribeWSDestinationSut(destination: string) {
    this.subscription = this.stomp.subscribe('/queue/' + destination, this.sutResponse);
  }

  ususcribeWSDestination() {
    /**
     * Unsubscribe subscription.
     */
    this.subscription.unsubscribe();
  }

  sendWSMessage() {
    /**
     * Send message.
     * @param {string} destination: send destination.
     * @param {object} body: a object that sends.
     * @param {object} headers: optional headers.
     */
    this.stomp.send('/topic/logs', { "data": "data" });
  }

  // Response
  public testResponse = (data) => {
    console.log(data);
    this.testDataUpdated.emit(data)
  }

    public sutResponse = (data) => {
    console.log(data);
    this.sutDataUpdated.emit(data)
  }


  public loadUrl = (data) => {
    console.log("Load Url:" + data);
    // this.urlNoVNCClient = data;
    // this.urlNoVNCClient[0] = "http://www.elpais.com";


    // this.timer = Observable.interval(500);
    // this.timer_subscription = this.timer
    //   .subscribe(
    //   res => {
    //     this.testManagerService.checkUrlStatus(data)
    //       .subscribe(
    //       (data) => {
    //         this.timer_subscription.unsubscribe();
    //         console.log("Unsuscribe from timer.");
    //       },
    //       (err) => console.log("Show error:" +err)
    //       );
    //   },
    //   error => console.log("VNC client is not ready yet")
    //   );

    //window.open(data);
  }

}
