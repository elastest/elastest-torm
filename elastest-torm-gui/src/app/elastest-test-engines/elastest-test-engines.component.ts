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
  styleUrls: ['./elastest-test-engines.component.scss'],
})
export class ElastestTestEnginesComponent implements OnInit {
  testEngines: TestEngineModel[];

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
    this.testEnginesService.getTestEngines().subscribe(
      (data: TestEngineModel[]) => {
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

  startTestEngine(testEngine: TestEngineModel): void {
    this.starting[testEngine.name] = true;
    this.testEnginesService.startTestEngineAsync(testEngine).subscribe(
      (engine: TestEngineModel) => {
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

  waitForReady(testEngine: TestEngineModel): void {
    this.timer = Observable.interval(2500);
    if (testEngine.isCreated() && !testEngine.isReady() && (this.subscription === null || this.subscription === undefined)) {
      this.subscription = this.timer.subscribe(() => {
        this.testEnginesService.getEngine(testEngine.name).subscribe((engine: TestEngineModel) => {
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

  stopTestEngine(testEngine: TestEngineModel): void {
    this.stopping[testEngine.name] = true;
    this.testEnginesService.stopTestEngine(testEngine).subscribe(
      (engine: TestEngineModel) => {
        this.updateTestEngine(testEngine, engine);
        this.stopping[testEngine.name] = false;
      },
      (error: Error) => {
        console.log(error);
        this.stopping[testEngine.name] = false;
      },
    );
  }

  updateTestEngine(testEngine: TestEngineModel, newTestEngine: TestEngineModel): void {
    let index: number = this.testEngines.indexOf(testEngine);
    if (index !== undefined) {
      this.testEngines[index] = newTestEngine;
      this.testEngines = [...this.testEngines];
    }
  }

  viewTestEngine(testEngine: TestEngineModel): void {
    this.router.navigate(['/test-engines', testEngine.name]);
  }
}
