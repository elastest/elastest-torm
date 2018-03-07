import { Component, OnInit, Input } from '@angular/core';
import { ExternalTestExecutionModel } from '../external-test-execution-model';
import { ExternalTJobExecModel } from '../../external-tjob-execution/external-tjob-execution-model';
import { Router } from '@angular/router';
import { ExternalService } from '../../external.service';

@Component({
  selector: 'etm-external-test-executions-view',
  templateUrl: './external-test-executions-view.component.html',
  styleUrls: ['./external-test-executions-view.component.scss'],
})
export class ExternalTestExecutionsViewComponent implements OnInit {
  @Input() exTestExecs: ExternalTestExecutionModel[];
  @Input() exTJobExec: ExternalTJobExecModel;

  constructor(private router: Router, private externalService: ExternalService) {}

  ngOnInit() {}
}
