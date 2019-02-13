import { Component, OnInit, Input } from '@angular/core';
import { LogComparisonModel } from './model/log-comparison.model';

@Component({
  selector: 'elastest-log-comparator',
  templateUrl: './elastest-log-comparator.component.html',
  styleUrls: ['./elastest-log-comparator.component.scss'],
})
export class ElastestLogComparatorComponent implements OnInit {
  @Input() public live: boolean;
  @Input() public model: LogComparisonModel;
  @Input() public remove: Function;

  constructor() {}

  ngOnInit(): void {}
}
