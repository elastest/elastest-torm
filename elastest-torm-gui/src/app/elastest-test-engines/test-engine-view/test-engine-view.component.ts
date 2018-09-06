import { EtPluginsService } from '../et-plugins.service';
import { ActivatedRoute, Params } from '@angular/router';
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

  constructor(private route: ActivatedRoute, private tetPluginsService: EtPluginsService) { }

  ngOnInit() {
    if (this.route.params !== null || this.route.params !== undefined) {
      this.route.params.subscribe((params: Params) => {
        this.engineName = params['name'];
        this.tetPluginsService.getUrl(params['name'])
          .subscribe((url: any) => {
            this.url = url;
          },
        )
      });
    }
  }

}
