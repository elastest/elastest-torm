import { TestEnginesService } from './test-engines.service';
import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'elastest-test-engines',
  templateUrl: './elastest-test-engines.component.html',
  styleUrls: ['./elastest-test-engines.component.scss']
})
export class ElastestTestEnginesComponent implements OnInit {

  testEngines: any[];

  testEnginesColumns: any[] = [
    { name: 'name', label: 'Name' },
  ];

  constructor(private testEnginesService: TestEnginesService) {
  }

  ngOnInit() {
    this.testEnginesService.getTestEngines().subscribe(
      (data: string[]) => {
        this.testEngines = data;
      },
      (error) => console.log(error)
    );
  }

}
