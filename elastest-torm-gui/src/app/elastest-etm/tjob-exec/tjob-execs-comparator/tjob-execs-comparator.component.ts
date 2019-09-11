import { Component, OnInit, ViewChild } from '@angular/core';
import { Router, ActivatedRoute, Params } from '@angular/router';
import { TitlesService } from '../../../shared/services/titles.service';
import { TJobExecService } from '../tjobExec.service';
import { TJobExecModel } from '../tjobExec-model';
import { TJobService } from '../../tjob/tjob.service';
import { TJobModel } from '../../tjob/tjob-model';
import { getResultIconByString } from '../../../shared/utils';
import { EtmMonitoringViewComponent } from '../../etm-monitoring-view/etm-monitoring-view.component';
import { ParameterModel } from '../../parameter/parameter-model';
import { MultiConfigModel } from '../../../shared/multi-config-view/multi-config-view.component';

@Component({
  selector: 'etm-tjob-execs-comparator',
  templateUrl: './tjob-execs-comparator.component.html',
  styleUrls: ['./tjob-execs-comparator.component.scss'],
})
export class TjobExecsComparatorComponent implements OnInit {
  @ViewChild('logsAndMetrics', { static: true })
  logsAndMetrics: EtmMonitoringViewComponent;
  showLogsAndMetrics: boolean = false;

  tJob: TJobModel;
  execsIds: number[] | string[] = [];
  execs: TJobExecModel[] = [];
  tJobExecParentAux: TJobExecModel = new TJobExecModel();

  loadingExecs: boolean = false;

  multiConfig: MultiConfigModel = new MultiConfigModel({ name: 'exec', configValues: [] });

  // TJob Exec Data
  tJobExecColumns: any[] = [
    { name: 'id', label: 'Id', width: 80 },
    { name: 'result', label: 'Result' },
    { name: 'startDate', label: 'Start Date' },
    { name: 'endDate', label: 'End Date' },
    { name: 'duration', label: 'Duration(sec)' },
    { name: 'monitoringStorageType', label: 'Mon. Storage', width: 123 },
    { name: 'sutExecution', label: 'Sut Execution', width: 116 },
    // { name: 'options', label: 'Options' },
  ];

  testCasesComparations: any[] = [];
  testCasesComparationsInverted: any[] = [];

  loadingTCComparations: boolean = false;

  testCasesComparationsColumns: any[] = [{ name: 'execId', label: 'Exec Id', width: 80 }];
  testCasesComparationsColumnsInverted: any[] = [{ name: 'testCase', label: 'Test Case' }];
  invertTestCasesComparation: boolean = false;

  constructor(
    private route: ActivatedRoute,
    public router: Router,
    private titlesService: TitlesService,
    private tJobExecService: TJobExecService,
    private tJobService: TJobService,
  ) {}

  ngOnInit(): void {
    this.titlesService.setHeadTitle('TJob Execs Comparator');
    this.titlesService.setPathName(this.router.routerState.snapshot.url);
    this.initComparator();
  }

  initComparator(): void {
    if (this.route.params !== null || this.route.params !== undefined) {
      this.loadingExecs = true;
      this.route.params
        .switchMap((params: Params) => this.tJobService.getTJob(params['tJobId']))
        .subscribe(
          (tJob: TJobModel) => {
            this.tJob = tJob;
            let queryParams: any = this.router.parseUrl(this.router.url).queryParams;
            if (
              queryParams !== undefined &&
              queryParams !== null &&
              queryParams.execs !== undefined &&
              queryParams.execs !== null
            ) {
              this.execsIds = (queryParams.execs as string).split(',');
              this.loadTJobExecs([...this.execsIds]);
            }
          },
          (error: Error) => {
            console.log(error);
            this.loadingExecs = false;
          },
        );
    }
  }

  loadTJobExecs(execsIdsToInit: number[] | string[]): void {
    this.loadingExecs = true;
    if (execsIdsToInit.length > 0) {
      let execId: number | string = execsIdsToInit.shift();
      this.tJobExecService.getTJobExecution(this.tJob, execId).subscribe(
        (tJobExec: TJobExecModel) => {
          // To simulate multiconfig tJob
          tJobExec = this.initSimulateMulticonfigTJob(tJobExec);

          this.execs.push(tJobExec);
          this.loadTJobExecs(execsIdsToInit);
        },
        (error: Error) => {
          console.log(error);
          this.loadingExecs = false;
        },
      );
    } else {
      this.loadingExecs = false;
      this.loadTestCasesComparations();
      this.tJobExecParentAux.type = 'PARENT';
      this.tJobExecParentAux.result = 'SUCCESS';
      this.tJobExecParentAux.execChilds = this.execs;
      this.tJob.multiConfigurations.push(this.multiConfig);
      if (this.logsAndMetrics) {
        this.logsAndMetrics.initView(this.tJob, this.tJobExecParentAux);
        this.showLogsAndMetrics = true;
      }
    }
  }

  initSimulateMulticonfigTJob(tJobExec: TJobExecModel): TJobExecModel {
    // To simulate multiconfig tJob
    if (!tJobExec.tJob) {
      tJobExec.tJob = new TJobModel();
    }
    this.multiConfig.configValues.push('' + tJobExec.id);
    tJobExec.tJob.multi = true;
    tJobExec.tJob.multiConfigurations.push(this.multiConfig);
    tJobExec.type = 'CHILD';
    tJobExec.parameters.push({ name: 'exec', value: tJobExec.id, multiConfig: true });

    // Parent start/end date
    if (!this.tJobExecParentAux.startDate) {
      this.tJobExecParentAux.startDate = tJobExec.startDate;
    } else {
      this.tJobExecParentAux.startDate =
        tJobExec.startDate.getTime() < this.tJobExecParentAux.startDate.getTime()
          ? tJobExec.startDate
          : this.tJobExecParentAux.startDate;
    }

    if (!this.tJobExecParentAux.endDate) {
      this.tJobExecParentAux.endDate = tJobExec.endDate;
    } else {
      if (!tJobExec.endDate) {
        tJobExec.endDate = new Date();
      }
      this.tJobExecParentAux.endDate =
        tJobExec.endDate.getTime() > this.tJobExecParentAux.endDate.getTime() ? tJobExec.endDate : this.tJobExecParentAux.endDate;
    }

    return tJobExec;
  }

  loadTestCasesComparations(): void {
    this.loadingTCComparations = true;
    if (this.execs) {
      let firstExec: boolean = true;
      for (let tJobExec of this.execs) {
        if (tJobExec && tJobExec.testSuites) {
          let testCasesComparation: any = { execId: tJobExec.id };
          for (let testSuite of tJobExec.testSuites) {
            if (testSuite && testSuite.testCases) {
              for (let testCase of testSuite.testCases) {
                let testCaseName: string = testCase.name;
                testCasesComparation[testCaseName] = testCase.getResult();
                if (firstExec) {
                  this.testCasesComparationsColumns.push({ name: testCaseName, label: testCaseName });
                }
              }
            }
          }
          this.testCasesComparations.push(testCasesComparation);
        }
        firstExec = false;
      }
    }

    this.loadTestCasesComparationsInverted();

    this.loadingTCComparations = false;
  }

  loadTestCasesComparationsInverted(): void {
    this.loadingTCComparations = true;
    if (this.execs) {
      let firstTJobExec: TJobExecModel = this.execs[0];
      if (firstTJobExec && firstTJobExec.testSuites) {
        let tmpMap: Map<string, any> = new Map();

        for (let testSuite of firstTJobExec.testSuites) {
          if (testSuite && testSuite.testCases) {
            for (let testCase of testSuite.testCases) {
              let testCaseName: string = testCase.name;
              tmpMap.set(testCaseName, { testCase: testCaseName });
            }
          }
        }

        for (let tJobExec of this.execs) {
          if (tJobExec && tJobExec.testSuites) {
            for (let testSuite of tJobExec.testSuites) {
              if (testSuite && testSuite.testCases) {
                for (let testCase of testSuite.testCases) {
                  let testCaseName: string = testCase.name;
                  let currentRow: any = tmpMap.get(testCaseName);
                  if (currentRow) {
                    currentRow[tJobExec.getIdAsString()] = testCase.getResult();
                  }
                }
              }
            }
            this.testCasesComparationsColumnsInverted.push({ name: tJobExec.getIdAsString(), label: tJobExec.getIdAsString() });
          }
        }
        this.testCasesComparationsInverted = this.testCasesComparationsInverted.concat(Array.from(tmpMap.values()));
      }
    }
  }

  switchInvertTestCasesComparisonAxis(): void {
    this.invertTestCasesComparation = !this.invertTestCasesComparation;
  }

  getResultIconByString(result: string): any {
    return getResultIconByString(result);
  }

  viewTJobExec(tJobExec: TJobExecModel): void {
    this.router.navigate(['/projects', this.tJob.project.id, 'tjob', this.tJob.id, 'tjob-exec', tJobExec.id]);
  }
}
