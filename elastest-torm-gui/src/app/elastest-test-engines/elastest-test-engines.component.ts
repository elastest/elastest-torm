import { TitlesService } from '../shared/services/titles.service';
import { PopupService } from '../shared/services/popup.service';
import { Router } from '@angular/router';
import { Observable, Subscription } from 'rxjs/Rx';
import { TestEngineModel } from './test-engine-model';
import { TestEnginesService } from './test-engines.service';
import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'elastest-test-engines',
  templateUrl: './elastest-test-engines.component.html',
  styleUrls: ['./elastest-test-engines.component.scss']
})
export class ElastestTestEnginesComponent implements OnInit {
  testEngines: TestEngineModel[];

  testEnginesColumns: any[] = [
    { name: 'name', label: 'Name' },
    { name: 'started', label: 'Started' },
    { name: 'options', label: 'Options' },
  ];

  timer: Observable<number>;
  subscription: Subscription;
  starting: any = {};
  stopping: any = {};

  constructor(
    private titlesService: TitlesService,
    private testEnginesService: TestEnginesService,
    private router: Router,
    public popupService: PopupService,
  ) { }

  ngOnInit() {
    this.titlesService.setHeadTitle('Test Engines');
    this.getTestEngines();
  }


  getTestEngines() {
    this.testEnginesService.getTestEngines().subscribe(
      (data) => {
        this.testEngines = data;
        for (let testEngine of this.testEngines) {
          this.starting[testEngine.name] = false;
          this.stopping[testEngine.name] = false;
          this.testEnginesService.isStarted(testEngine).subscribe(
            (started) => {
              testEngine.started = started;
              if (started) {
                this.testEnginesService.getUrl(testEngine.name).subscribe(
                  (url: string) => { this.checkIfIsReady(testEngine, url); }
                );
              }
            },
            (error) => console.log(error),
          );
        }
      },
      (error) => console.log(error)
    );
  }

  startTestEngine(testEngine: TestEngineModel) {
    this.starting[testEngine.name] = true;
    this.testEnginesService.startTestEngine(testEngine).subscribe(
      (url) => {
        this.initStartedTest(testEngine, url);
        this.starting[testEngine.name] = false;
      },
      (error) => {
        console.log(error);
        this.popupService.openSnackBar('Error starting service ' + testEngine.name, 'OK');
        this.starting[testEngine.name] = false;
      },
    );
  }

  initStartedTest(testEngine: TestEngineModel, url: string) {
    this.updateTestEngine(testEngine, true, url);
    if (url === '') {
      this.popupService.openSnackBar('Error: Could not start service ' + testEngine.name, 'OK');
    }
    this.checkIfIsReady(testEngine, url);
  }

  checkIfIsReady(testEngine: TestEngineModel, url: string) {
    this.timer = Observable.interval(2000);
    if (testEngine.started && url !== '' && (this.subscription === null || this.subscription === undefined)) {
      this.subscription = this.timer
        .subscribe(() => {
          this.testEnginesService.isWorking(testEngine)
            .subscribe(
            (working: boolean) => {
              if (working) {
                this.subscription.unsubscribe();
                this.subscription = undefined;
                this.setReady(testEngine);
              }
            }
            );
        });
    }
  }

  updateTestEngine(testEngine: TestEngineModel, started: boolean, url: any = '') {
    for (let engine of this.testEngines) {
      if (engine.name === testEngine.name) {
        engine.started = started && url !== '';
        if (!engine.started) {
          engine.ready = false;
        }
        engine.url = url;
        break;
      }
    }
  }

  setReady(testEngine: TestEngineModel) {
    for (let engine of this.testEngines) {
      if (engine.name === testEngine.name) {
        engine.ready = true;
      }
    }
  }


  stopTestEngine(testEngine: TestEngineModel) {
    this.stopping[testEngine.name] = true;
    this.testEnginesService.stopTestEngine(testEngine).subscribe(
      (data) => {
        this.updateTestEngine(testEngine, false);
        this.stopping[testEngine.name] = false;
      },
      (error) => {
        console.log(error);
        this.stopping[testEngine.name] = false;
      },
    );
  }

  viewTestEngine(testEngine: TestEngineModel) {
    this.router.navigate(['/test-engines', testEngine.name]);
  }

}
