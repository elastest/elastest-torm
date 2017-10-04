import { Router } from '@angular/router';
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

  constructor(private testEnginesService: TestEnginesService, private router: Router, ) { }

  ngOnInit() {
    this.getTestEngines();
  }


  getTestEngines() {
    this.testEnginesService.getTestEngines().subscribe(
      (data) => {
        this.testEngines = data;
        for (let testEngine of this.testEngines) {
          this.testEnginesService.isStarted(testEngine).subscribe(
            (started) => {
              testEngine.started = started;
            },
            (error) => console.log(error),
          );
        }
      },
      (error) => console.log(error)
    );
  }

  startTestEngine(testEngine: TestEngineModel) {
    this.testEnginesService.startTestEngine(testEngine).subscribe(
      (url) => {
        this.updateTestEngine(testEngine, true, url);
      },
      (error) => console.log(error),
    );
  }

  updateTestEngine(testEngine: TestEngineModel, started: boolean, url: any = '') {
    for (let engine of this.testEngines) {
      if (engine.name === testEngine.name) {
        engine.started = started && url !== '';
        engine.url = url;
        break;
      }
    }
  }


  stopTestEngine(testEngine: TestEngineModel) {
    this.testEnginesService.stopTestEngine(testEngine).subscribe(
      (data) => this.updateTestEngine(testEngine, false),
      (error) => console.log(error),
    );
  }

  viewTestEngine(testEngine: TestEngineModel) {
    this.router.navigate(['/test-engines', testEngine.name]);
  }

}
