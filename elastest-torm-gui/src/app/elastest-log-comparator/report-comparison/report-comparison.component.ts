import { Component, OnInit, Input, HostListener, OnDestroy } from '@angular/core';
import { TableService } from '../service/table.service';
import { LogComparisonModel, comparisonMode, viewMode } from '../model/log-comparison.model';
import { MonitoringService } from '../../shared/services/monitoring.service';
import { Subscription, Observable, interval } from 'rxjs';

@Component({
  selector: 'elastest-logcomparator-report-comparison',
  templateUrl: './report-comparison.component.html',
  styleUrls: ['./report-comparison.component.scss'],
})
export class ReportComparisonComponent implements OnInit, OnDestroy {
  @Input() public logComparison: LogComparisonModel;

  diff: string;

  // Cache
  cacheMatrix: string[][] = [];

  comparisonInProgress: boolean = false;

  currentComparisonMode: number = 1;
  comparisonCompleteBtnEnabled: boolean = true;
  comparisonNoTimestampBtnEnabled: boolean = false;
  comparisonTimeDiffBtnEnabled: boolean = true;

  currentViewMode: number = 0;
  viewCompleteBtnEnabled: boolean = false;
  viewTestsLogsBtnEnabled: boolean = true;
  viewFailedTestsBtnEnabled: boolean = true;

  execsRow: any[] = [];
  loadingData: boolean;
  resultData: any[] = [];

  timer: Observable<number>;
  subscription: Subscription;

  constructor(private tableService: TableService, private monitoringService: MonitoringService) {
    this.comparisonInProgress = false;
    this.loadingData = true;
  }

  ngOnInit(): void {
    this.init();
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  @HostListener('window:beforeunload')
  beforeunloadHandler() {
    // On window closed leave session
    this.unsubscribe();
  }

  unsubscribe(): void {
    if (this.subscription !== undefined) {
      this.subscription.unsubscribe();
      this.subscription = undefined;
    }
  }

  init(): void {
    this.initcacheMatrix();
    this.loadComparison();
  }

  initcacheMatrix(): void {
    // Matrix of comparisonMode/viewMode
    this.cacheMatrix.push(['', '', '']);
    this.cacheMatrix.push(['', '', '']);
    this.cacheMatrix.push(['', '', '']);
  }

  getCurrentCacheValue(): string {
    if (this.cacheMatrix[this.currentComparisonMode] && this.cacheMatrix[this.currentComparisonMode][this.currentViewMode]) {
      return this.cacheMatrix[this.currentComparisonMode][this.currentViewMode];
    }
    return '';
  }

  cacheDiff(diff: string): void {
    this.cacheMatrix[this.currentComparisonMode][this.currentViewMode] = diff;
  }

  loadComparison(comparison: comparisonMode = 'notimestamp', view: viewMode = 'complete'): void {
    this.loadingData = true;
    this.resultData = [];

    let useCache: boolean = false;
    let cacheValue: string = this.getCurrentCacheValue();
    if (cacheValue && cacheValue !== '') {
      useCache = true;
      this.loadFromDiffData(cacheValue);
    }

    if (!useCache && this.logComparison) {
      let components: string[] = this.logComparison.components;
      components = components && components.length > 0 ? components : [this.logComparison.component];

      this.monitoringService
        .compareLogsPair(
          true,
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
          (processId: string) => {
            if (processId) {
              this.subscribeToComparisonResult(processId);
            } else {
              this.comparisonInProgress = true;
              this.resetComparisonButtons();
              this.resetViewButtons();

              this.loadingData = false;
            }
          },
          (error: Error) => {
            console.log(error);
            this.comparisonInProgress = true;
            this.resetComparisonButtons();
            this.resetViewButtons();

            this.loadingData = false;
          },
        );
    }
  }

  subscribeToComparisonResult(processId: string): void {
    let comparisonInProgressMsg: string = 'ET-PROCESSING';
    let comparisonString: string = comparisonInProgressMsg;

    this.timer = interval(4000);
    if (this.subscription === null || this.subscription === undefined) {
      this.subscription = this.timer.subscribe(() => {
        this.monitoringService.getComparisonByProcessId(processId).subscribe((comparison: string) => {
          comparisonString = comparison;
          if (comparisonString !== comparisonInProgressMsg) {
            this.unsubscribe();
            this.loadFromDiffData(comparisonString);
          }
        });
      });
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
    this.currentComparisonMode = mode;
    this.loadingData = true;
    let comparison: comparisonMode = this.getComparisonModeNameById(mode);
    this.loadComparison(comparison, this.getViewModeNameById(this.currentViewMode));
  }

  getViewModeNameById(mode: number): viewMode {
    let view: viewMode = 'complete';
    switch (mode) {
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
    this.currentViewMode = mode;
    this.loadingData = true;
    let view: viewMode = this.getViewModeNameById(mode);

    this.loadComparison(this.getComparisonModeNameById(this.currentComparisonMode), view);
  }

  private resetComparisonButtons(): void {
    this.comparisonCompleteBtnEnabled = true;
    this.comparisonNoTimestampBtnEnabled = true;
    this.comparisonTimeDiffBtnEnabled = true;
    switch (this.currentComparisonMode) {
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
    switch (this.currentViewMode) {
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
