import { Component, OnInit, Input } from '@angular/core';
import { TestCaseModel } from '../test-case-model';
import { TJobExecModel } from '../../tjob-exec/tjobExec-model';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'etm-test-case-detail',
  templateUrl: './test-case-detail.component.html',
  styleUrls: ['./test-case-detail.component.scss'],
})
export class TestCaseDetailComponent implements OnInit {
  @Input()
  testCase: TestCaseModel;

  constructor(public route: ActivatedRoute) {}

  ngOnInit(): void {}
}
