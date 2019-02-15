import { Component, OnInit, Input } from '@angular/core';
import { TableService } from '../service/table.service';
import { LogComparisonModel, comparisonMode, viewMode } from '../model/log-comparison.model';
import { MonitoringService } from '../../shared/services/monitoring.service';

@Component({
  selector: 'elastest-logcomparator-report-comparison',
  templateUrl: './report-comparison.component.html',
  styleUrls: ['./report-comparison.component.scss'],
})
export class ReportComparisonComponent implements OnInit {
  @Input() logComparison: LogComparisonModel;

  diff: string;

  // Cache
  completeDiff: string;
  noTimestampDiff: string;
  timeDiffDiff: string;

  comparisonInProgress: boolean = false;

  comparisonMode: number = 1;
  comparisonCompleteBtnEnabled: boolean = true;
  comparisonNoTimestampBtnEnabled: boolean = false;
  comparisonTimeDiffBtnEnabled: boolean = true;

  viewMode: number = 0;
  viewCompleteBtnEnabled: boolean = false;
  viewTestsLogsBtnEnabled: boolean = true;
  viewFailedTestsBtnEnabled: boolean = true;

  execsRow: any[] = [];
  loadingData: boolean;
  resultData: any[] = [];

  constructor(private tableService: TableService, private monitoringService: MonitoringService) {
    this.comparisonInProgress = false;
    this.loadingData = true;
  }

  ngOnInit(): void {
    this.loadComparison();
  }

  loadComparison(comparison: comparisonMode = 'notimestamp', view: viewMode = 'complete'): void {
    //TODO view

    this.loadingData = true;
    this.resultData = [];

    let useCache: boolean = false;

    switch (comparison) {
      case 'complete':
        if (this.completeDiff && this.completeDiff !== '') {
          useCache = true;
          this.loadFromDiffData(this.completeDiff);
        }
        break;
      case 'notimestamp':
        if (this.noTimestampDiff && this.noTimestampDiff !== '') {
          useCache = true;
          this.loadFromDiffData(this.noTimestampDiff);
        }
        break;
      case 'timediff':
        if (this.timeDiffDiff && this.timeDiffDiff !== '') {
          useCache = true;
          this.loadFromDiffData(this.timeDiffDiff);
        }
        break;
      default:
        break;
    }

    if (!useCache && this.logComparison) {
      let components: string[] = this.logComparison.components;
      components = components && components.length > 0 ? components : [this.logComparison.component];

      this.monitoringService
        .compareLogsPair(
          this.logComparison.pair,
          this.logComparison.stream,
          components,
          this.logComparison.startDate,
          this.logComparison.endDate,
          true,
          true,
          comparison,
          view,
        )
        .subscribe(
          (diff: string) => {
            this.loadFromDiffData(diff);
          },
          (error: Error) => {
            console.log(error);
          },
        );
    }
  }

  loadFromDiffData(diff: string): void {
    this.diff = diff;
    this.cacheDiff(diff);
    this.resultData[0] = {
      logs: this.tableService.generateTable(this.diff),
    };
    this.comparisonInProgress = true;
    this.resetComparisonButtons();
    this.resetViewButtons();

    this.loadingData = false;
  }

  cacheDiff(diff: string): void {
    switch (this.comparisonMode) {
      // Complete
      case 0:
        this.completeDiff = diff;
        break;
      // No timestamp

      case 1:
        this.noTimestampDiff = diff;
        break;

      // Timediff
      case 2:
        this.timeDiffDiff = diff;
        break;

      default:
        break;
    }
  }

  getComparisonModeNameById(mode: number): comparisonMode {
    let comparison: comparisonMode = 'notimestamp';
    switch (mode) {
      // Complete
      case 0:
        comparison = 'complete';
        break;
      // Timediff
      case 2:
        comparison = 'timediff';
        break;
      // No timestamp
      case 1:
      default:
        break;
    }

    return comparison;
  }

  updateComparisonMode(mode: number): void {
    this.comparisonMode = mode;
    this.loadingData = true;
    let comparison: comparisonMode = this.getComparisonModeNameById(mode);
    this.loadComparison(comparison, this.getViewModeNameById(this.viewMode));
  }

  getViewModeNameById(mode: number): viewMode {
    let view: viewMode = 'complete';
    switch (this.viewMode) {
      // Tests Logs
      case 1:
        view = 'testslogs';
        break;
      // Failed tests
      case 2:
        view = 'failedtests';
        break;
      // Complete
      case 0:
      default:
        break;
    }

    return view;
  }
  updateViewMode(mode: number): void {
    this.viewMode = mode;
    this.loadingData = true;
    let view: viewMode = this.getViewModeNameById(mode);

    this.loadComparison(this.getComparisonModeNameById(this.comparisonMode), view);
  }

  private resetComparisonButtons(): void {
    this.comparisonCompleteBtnEnabled = true;
    this.comparisonNoTimestampBtnEnabled = true;
    this.comparisonTimeDiffBtnEnabled = true;
    switch (this.comparisonMode) {
      // Complete
      case 0:
        this.comparisonCompleteBtnEnabled = false;
        break;
      // No timestamp
      case 1:
        this.comparisonNoTimestampBtnEnabled = false;
        break;
      // TimeDiff
      case 2:
        this.comparisonTimeDiffBtnEnabled = false;
        break;
      default:
        break;
    }
  }

  private resetViewButtons(): void {
    this.viewCompleteBtnEnabled = true;
    this.viewTestsLogsBtnEnabled = true;
    this.viewFailedTestsBtnEnabled = true;
    switch (this.viewMode) {
      // Complete
      case 0:
        this.viewCompleteBtnEnabled = false;
        break;
      // Tests logs
      case 1:
        this.viewTestsLogsBtnEnabled = false;
        break;
      // Failed Tests
      case 2:
        this.viewFailedTestsBtnEnabled = false;
        break;
      default:
        break;
    }
  }
}
