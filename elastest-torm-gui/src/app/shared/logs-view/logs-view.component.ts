import { LogViewModel } from './log-view-model';

import { Component, Input, OnInit, ViewChild } from '@angular/core';
import { LogsViewTextComponent } from '../logs-view-text/logs-view-text.component';
import { FilesService } from '../services/files.service';

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

  errors: number = 0;
  warnings: number = 0;

  constructor(private filesService: FilesService) {}

  ngOnInit(): void {}

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

  downloadLog(): void {
    if (this.model) {
      let logArray: string[] = this.convertTracesToLogArray();
      this.filesService.downloadStringAsTextFile(
        logArray.join('\n'),
        this.model.name && this.model.name !== '' ? this.model.name : 'logs',
        'log',
      );
    }
  }

  convertTracesToLogArray(): string[] {
    let logArray: string[] = [];
    if (this.model) {
      let traces: string[] = this.model.prevTraces.concat(this.model.traces);
      for (let trace of traces) {
        let logLine: string = (trace['timestamp'] ? trace['timestamp'] + ' : ' : '') + (trace['message'] ? trace['message'] : '');
        logArray.push(logLine);
      }
    }
    return logArray;
  }

  loadLevels(): void {
    this.errors = this.model.getErrors().length;
    this.warnings = this.model.getWarnings().length;
  }
}
