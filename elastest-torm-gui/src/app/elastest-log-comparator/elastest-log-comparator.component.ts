import { Component, OnInit, Input } from '@angular/core';
import { LogComparisonModel } from './model/log-comparison.model';

@Component({
  selector: 'elastest-log-comparator',
  templateUrl: './elastest-log-comparator.component.html',
  styleUrls: ['./elastest-log-comparator.component.scss'],
})
export class ElastestLogComparatorComponent implements OnInit {
  @Input() public live: boolean;
  @Input() public model: Map<string, LogComparisonModel[]>;
  @Input() public remove: Function;
  @Input() public keys: string[] = [];

  selectedLogTab: number;
  selectedLogComparisionTab: number;

  constructor() {}

  ngOnInit(): void {
  }

  goToLogTab(num: number): void {
    this.selectedLogTab = num;
  }

  goToLogComparisonTab(num: number): void {
    this.selectedLogComparisionTab = num;
  }
}
