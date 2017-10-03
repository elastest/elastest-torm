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
    { name: 'url', label: 'Url' },
  ];

  constructor(private testEnginesService: TestEnginesService) { }

  ngOnInit() {
    this.testEnginesService.getTestEngines().subscribe(
      (data) => {
        this.testEngines = data;
      },
      (error) => console.log(error)
    );
  }

  startTestEngine(testEngine: TestEngineModel) {
    this.testEnginesService.startTestEngine(testEngine).subscribe(
      (data) => this.updateTestEngine(testEngine, data)
    );
  }

  updateTestEngine(testEngine: TestEngineModel, url: any) {
    for (let engine of this.testEngines) {
      if (engine.name === testEngine.name) {
        engine.started = true;
        engine.url = url;
        break;
      }
    }
  }


  stopTestEngine(testEngine: TestEngineModel) {

  }

  viewTestEngine(testEngine: TestEngineModel) {

  }

}
