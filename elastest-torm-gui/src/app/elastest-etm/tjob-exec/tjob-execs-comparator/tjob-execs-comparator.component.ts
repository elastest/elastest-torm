import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute, Params } from '@angular/router';
import { TitlesService } from '../../../shared/services/titles.service';
import { TJobExecService } from '../tjobExec.service';
import { TJobExecModel } from '../tjobExec-model';
import { TJobService } from '../../tjob/tjob.service';
import { TJobModel } from '../../tjob/tjob-model';
import { getResultIconByString } from '../../../shared/utils';

@Component({
  selector: 'etm-tjob-execs-comparator',
  templateUrl: './tjob-execs-comparator.component.html',
  styleUrls: ['./tjob-execs-comparator.component.scss'],
})
export class TjobExecsComparatorComponent implements OnInit {
  tJob: TJobModel;
  execsIds: number[] | string[] = [];
  execs: TJobExecModel[] = [];

  loadingExecs: boolean = false;

  // TJob Exec Data
  tJobExecColumns: any[] = [
    { name: 'id', label: 'Id', width: 80 },
    { name: 'result', label: 'Result' },
    { name: 'lastExecutionDate', label: 'Last Execution' },
    { name: 'startDate', label: 'Start Date' },
    { name: 'endDate', label: 'End Date' },
    { name: 'duration', label: 'Duration(sec)' },
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
    this.titlesService.setPathName(this.router.routerState.snapshot.url, 'TJob Execs Comparator');
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
    }
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
}