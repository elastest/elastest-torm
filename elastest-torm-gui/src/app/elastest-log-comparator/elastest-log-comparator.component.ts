import { Component, OnInit, Input } from '@angular/core';

@Component({
  selector: 'elastest-log-comparator',
  templateUrl: './elastest-log-comparator.component.html',
  styleUrls: ['./elastest-log-comparator.component.scss'],
})
export class ElastestLogComparatorComponent implements OnInit {
  @Input() public live: boolean;
  @Input() public model: string;
  @Input() public titleName: string;
  @Input() public remove: Function;

  constructor() {}

  ngOnInit(): void {
    console.log(this.model);
  }
}
