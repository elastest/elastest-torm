import { Component, OnInit, Input } from '@angular/core';
import { SupportServiceConfigModel } from '../support-service.model';

@Component({
  selector: 'esm-support-service-config-view',
  templateUrl: './support-service-config-view.component.html',
  styleUrls: ['./support-service-config-view.component.scss'],
})
export class SupportServiceConfigViewComponent implements OnInit {
  @Input() model: SupportServiceConfigModel;

  constructor() {}

  ngOnInit() {}

  switchBooleanConfig($event): void {
    this.model.value = $event.checked;
  }
}
