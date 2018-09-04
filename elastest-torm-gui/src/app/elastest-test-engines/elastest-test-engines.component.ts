import { TitlesService } from '../shared/services/titles.service';
import { PopupService } from '../shared/services/popup.service';
import { Router } from '@angular/router';
import { Observable, Subscription } from 'rxjs/Rx';

import { TestEnginesService } from './test-engines.service';
import { Component, OnInit } from '@angular/core';
import { EtPluginModel } from './et-plugin-model';

@Component({
  selector: 'elastest-test-engines',
  templateUrl: './elastest-test-engines.component.html',
  styleUrls: ['./elastest-test-engines.component.scss'],
})
export class ElastestTestEnginesComponent implements OnInit {
  testEngines: EtPluginModel[];

  testEnginesColumns: any[] = [
    { name: 'name', label: 'Name' },
    { name: 'status', label: 'Status' },
    { name: 'statusMsg', label: 'Info' },
    { name: 'url', label: 'Url' },
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
  ) {}

  ngOnInit() {
    this.titlesService.setHeadTitle('Test Engines');
    this.getTestEngines();
  }

  getTestEngines(): void {
    this.testEnginesService.getEtPlugins().subscribe(
      (data: EtPluginModel[]) => {
        this.testEngines = data;
        for (let testEngine of this.testEngines) {
          this.starting[testEngine.name] = false;
          this.stopping[testEngine.name] = false;

          if (testEngine.isCreated() && !testEngine.isReady()) {
            this.waitForReady(testEngine);
          }
        }
      },
      (error: Error) => console.log(error),
    );
  }

  startTestEngine(testEngine: EtPluginModel): void {
    this.starting[testEngine.name] = true;
    this.testEnginesService.startEtPlugin(testEngine).subscribe(
      (engine: EtPluginModel) => {
        this.updateTestEngine(testEngine, engine);
        this.waitForReady(engine);
        this.starting[testEngine.name] = false;
      },
      (error: Error) => {
        console.log(error);
        this.popupService.openSnackBar('Error starting service ' + testEngine.name, 'OK');
        this.starting[testEngine.name] = false;
      },
    );
  }

  waitForReady(testEngine: EtPluginModel): void {
    this.timer = Observable.interval(2500);
    if (testEngine.isCreated() && !testEngine.isReady() && (this.subscription === null || this.subscription === undefined)) {
      this.subscription = this.timer.subscribe(() => {
        this.testEnginesService.getEtPlugin(testEngine.name).subscribe((engine: EtPluginModel) => {
          this.updateTestEngine(testEngine, engine);
          testEngine = engine;
          if (engine.isReady()) {
            this.subscription.unsubscribe();
            this.subscription = undefined;
          }
        });
      });
    }
  }

  stopTestEngine(testEngine: EtPluginModel): void {
    this.stopping[testEngine.name] = true;
    this.testEnginesService.stopEtPlugin(testEngine).subscribe(
      (engine: EtPluginModel) => {
        this.updateTestEngine(testEngine, engine);
        this.stopping[testEngine.name] = false;
      },
      (error: Error) => {
        console.log(error);
        this.stopping[testEngine.name] = false;
      },
    );
  }

  updateTestEngine(testEngine: EtPluginModel, newTestEngine: EtPluginModel): void {
    let index: number = this.testEngines.indexOf(testEngine);
    if (index !== undefined) {
      this.testEngines[index] = newTestEngine;
      this.testEngines = [...this.testEngines];
    }
  }

  viewTestEngine(testEngine: EtPluginModel): void {
    this.router.navigate(['/test-engines', testEngine.name]);
  }
}
