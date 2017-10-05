import { TestEnginesService } from '../test-engines.service';
import { ActivatedRoute, Params } from '@angular/router';
import { TestEngineModel } from '../test-engine-model';
import { Component, Input, OnInit } from '@angular/core';

@Component({
  selector: 'test-engine-view',
  templateUrl: './test-engine-view.component.html',
  styleUrls: ['./test-engine-view.component.scss']
})
export class TestEngineViewComponent implements OnInit {
  @Input()
  engineName: string;

  url: string;

  constructor(private route: ActivatedRoute, private testEnginesService: TestEnginesService) { }

  ngOnInit() {
    if (this.route.params !== null || this.route.params !== undefined) {
      this.route.params.subscribe((params: Params) => {
        this.engineName = params['name'];
        this.testEnginesService.getUrl(params['name'])
          .subscribe((url: any) => {
            this.url = url;
          },
        )
      });
    }
  }

}
