import { Component, OnInit, Input } from '@angular/core';
import { TableService } from '../service/table.service';
import { LogComparisonModel, comparisonMode } from '../model/log-comparison.model';
import { MonitoringService } from '../../shared/services/monitoring.service';

@Component({
  selector: 'elastest-logcomparator-report-comparison',
  templateUrl: './report-comparison.component.html',
  styleUrls: ['./report-comparison.component.scss'],
})
export class ReportComparisonComponent implements OnInit {
  @Input() logComparison: LogComparisonModel;

  diff: string;

  comparisonInProgress: boolean = false;
  comparisonMode: number = 1;
  comparisonCompleteBtnEnabled: boolean = true;
  comparisonNoTimestampBtnEnabled: boolean = false;
  comparisonTimeDiffBtnEnabled: boolean = true;

  execsRow: any[] = [];
  loadingData: boolean;
  resultData: any[] = [];
  viewButtonsClasses: string[] = ['accent', 'primary', 'primary', 'primary'];
  viewMode: number;

  constructor(private tableService: TableService, private monitoringService: MonitoringService) {
    this.comparisonInProgress = false;
    this.loadingData = true;
  }

  ngOnInit(): void {
    this.loadComparison();
  }

  loadComparison(comparison: comparisonMode = 'notimestamp'): void {
    this.loadingData = true;
    this.viewMode = 0;

    this.resultData = [];

    if (this.logComparison) {
      this.monitoringService
        .compareLogsPair(
          this.logComparison.pair,
          this.logComparison.stream,
          this.logComparison.component,
          this.logComparison.startDate,
          this.logComparison.endDate,
          true,
          true,
          comparison,
        )
        .subscribe(
          (diff: string) => {
            this.diff = diff;
            this.resultData[0] = {
              logs: this.tableService.generateTable(this.diff),
            };
            this.comparisonInProgress = true;
            this.resetComparisonButtons();

            this.loadingData = false;
          },
          (error: Error) => {
            console.log(error);
          },
        );
    }
  }

  updateComparisonMode(mode: number, $event: any): void {
    this.comparisonMode = mode;
    this.loadingData = true;
    let comparison: comparisonMode = 'notimestamp';
    switch (this.comparisonMode) {
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
    this.loadComparison(comparison);
  }

  updateViewMode(comp: number, mode: number): void {
    // this.viewMode = mode;
    // this.loadingData = true;
    // this.resetViewButtonsClasses();
    // switch (this.viewMode) {
    //   case 0:
    //     this.viewRaw(comp, true);
    //     break;
    //   case 1:
    //     this.viewByMethods(comp);
    //     break;
    //   case 2:
    //     this.viewByMethods(comp);
    //     break;
    //   case 3:
    //   default:
    //     this.viewRaw(comp, false);
    //     break;
    // }
    // if (this.comparisonInProgress) {
    //   this.updateComparisonMode(this.comparisonMode);
    // } else {
    //   this.loadingData = false;
    // }
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

  private resetViewButtonsClasses(): void {
    for (let i: number = 0; i < this.viewButtonsClasses.length; i++) {
      this.viewButtonsClasses[i] = 'primary';
    }
    this.viewButtonsClasses[this.viewMode] = 'accent';
  }
}
