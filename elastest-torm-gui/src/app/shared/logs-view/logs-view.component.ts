import { LogViewModel } from './log-view-model';

import { Component, Input, OnInit, ViewChild } from '@angular/core';
import { LogsViewTextComponent } from '../logs-view-text/logs-view-text.component';
import { FilesService } from '../services/files.service';
import { getErrorColor, getWarnColor } from '../utils';

@Component({
  selector: 'logs-view',
  templateUrl: './logs-view.component.html',
  styleUrls: ['./logs-view.component.scss'],
})
export class LogsViewComponent implements OnInit {
  @Input() public live: boolean;
  @ViewChild('logsViewText') public logsViewText: LogsViewTextComponent;
  @Input() public model: LogViewModel;
  @Input() public remove: Function;

  errors: any[] = [];
  warnings: any[] = [];
  errorsAndWarnings: any[] = [];

  errorColor: string = getErrorColor();
  warnColor: string = getWarnColor();

  errorsFiltered: boolean = false;
  warningsFiltered: boolean = false;

  filteredErrorsModel: LogViewModel;
  filteredWarningsModel: LogViewModel;
  filteredErrorsAndWarningsModel: LogViewModel;

  constructor(private filesService: FilesService) {}

  ngOnInit(): void {
    this.filteredErrorsModel = this.model.cloneModel();
    this.filteredWarningsModel = this.model.cloneModel();
    this.filteredErrorsAndWarningsModel = this.model.cloneModel();
  }

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

  loadErrors(): number {
    this.errors = this.model.getErrors();
    this.filteredErrorsModel.traces = this.errors;
    if (this.errorsFiltered && this.warningsFiltered) {
      this.loadErrorsAndWarnings();
    }
    return this.errors.length;
  }

  loadWarnings(): number {
    this.warnings = this.model.getWarnings();
    this.filteredWarningsModel.traces = this.warnings;
    if (this.errorsFiltered && this.warningsFiltered) {
      this.loadErrorsAndWarnings();
    }
    return this.warnings.length;
  }

  loadErrorsAndWarnings(): number {
    this.errorsAndWarnings = this.model.getErrorsAndWarnings();
    this.filteredErrorsAndWarningsModel.traces = this.errorsAndWarnings;
    return this.errorsAndWarnings.length;
  }

  switchFilterErrors(): void {
    this.errorsFiltered = !this.errorsFiltered;
  }

  switchFilterWarnings(): void {
    this.warningsFiltered = !this.warningsFiltered;
  }
}
