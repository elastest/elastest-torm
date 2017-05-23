import { TestResult } from './test-result';
/**
 * Created by frdiaz on 19/04/2017.
 */
import { Injectable } from "@angular/core";
import { StompService } from 'ng2-stomp-service';
import { LogTrace } from "./log-trace";
import { TestManagerService } from "./test-manager.service";
import { BehaviorSubject } from 'rxjs/BehaviorSubject';
import { Http } from "@angular/http";
import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';


@Injectable()
export class StompWSManager {

  private wsConf = {
    host: '/logs',
    debug: true,
    queue:{'init':false}
  }

  private subscription: any;

  traces: LogTrace[] = [];

  endExecution: boolean = false;

  urlNoVNCClient: string[] = [];
  testResult: TestResult = new TestResult("", "", "", "");
  timer: Observable<number>;
  timer_subscription: Subscription;

  private _navItemSource = new BehaviorSubject<string>("");
  private _testResultSource = new BehaviorSubject<TestResult>(this.testResult);

  navItem$ = this._navItemSource.asObservable();
  testResult$ = this._testResultSource.asObservable();

  constructor(private stomp: StompService, private testManagerService: TestManagerService, private http: Http) { }

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
      this.subscription = this.stomp.subscribe('/topic/logs', this.response);
      this.subscription = this.stomp.subscribe('/topic/endExecutionTest', this.processEndExecutionTest);
      this.subscription = this.stomp.subscribe('/topic/urlsVNC', this.loadUrl);

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

  subscribeWSDestination() {
    /**
     * Subscribe.
     * @param {string} destination: subscibe destination.
     * @param {Function} callback(message,headers): called after server response.
     * @param {object} headers: optional headers.
     */
    this.subscription = this.stomp.subscribe('/topic/logs', this.response);
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
  public response = (data) => {
    console.log(data);
    this.traces.push(data);
  }

  // Response
  public processEndExecutionTest = (data) => {
    console.log(data);
    this.endExecution = true;
    this.testManagerService.getTestResults().subscribe(
      testResults => {
        console.log(testResults);
        this.testResult = testResults;
        this._testResultSource.next(this.testResult);
      }
    );
    console.log("Invoked getTestResults");
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

    this._navItemSource.next(data);
    //window.open(data);
  }

}