import { EtPluginsService } from '../et-plugins.service';
import { ActivatedRoute, Params } from '@angular/router';
import { Component, Input, OnInit } from '@angular/core';
import { EtPluginModel } from '../et-plugin-model';

@Component({
  selector: 'test-engine-view',
  templateUrl: './test-engine-view.component.html',
  styleUrls: ['./test-engine-view.component.scss'],
})
export class TestEngineViewComponent implements OnInit {
  @Input()
  engineName: string;

  @Input()
  engineDisplayName: string;

  url: string;

  etPlugin: EtPluginModel;

  constructor(private route: ActivatedRoute, private tetPluginsService: EtPluginsService) {}

  ngOnInit() {
    if (this.route.params !== null || this.route.params !== undefined) {
      this.route.params.subscribe((params: Params) => {
        this.engineName = params['name'];
        this.tetPluginsService.getEtPlugin(params['name']).subscribe((etPlugin: EtPluginModel) => {
          this.etPlugin = etPlugin;
          this.engineDisplayName = etPlugin.displayName && etPlugin.displayName !== '' ? etPlugin.displayName : this.engineName;
          this.url = etPlugin.url;
        });
      });
    }
  }
}
