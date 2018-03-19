import { ElasticSearchService } from '../services/elasticsearch.service';
import { LogViewModel } from './log-view-model';

import { Component, ElementRef, HostListener, Input, OnInit, ViewChild } from '@angular/core';
import { LogsViewTextComponent } from '../logs-view-text/logs-view-text.component';

@Component({
  selector: 'logs-view',
  templateUrl: './logs-view.component.html',
  styleUrls: ['./logs-view.component.scss'],
})
export class LogsViewComponent implements OnInit {
  @Input() public live: boolean;
  @ViewChild('logsViewText') private logsViewText: LogsViewTextComponent;
  @Input() public model: LogViewModel;
  @Input() public remove: Function;

  constructor() {}

  ngOnInit() {}

  scrollToElement(position: number): void {
    this.logsViewText.scrollToElement(position);
  }

  getElementsList(): any {
    return this.logsViewText.getElementsList();
  }
  getElementOffsetTop(element: HTMLLIElement): number {
    return this.logsViewText.getElementOffsetTop(element);
  }

  lockLogScroll(switchLock: boolean): void {
    this.logsViewText.lockLogScroll(switchLock);
  }
}
