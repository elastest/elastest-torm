import { Component, OnInit, Input, ViewChild, ElementRef } from '@angular/core';
import { TestLinkService } from '../../testlink.service';
import { TestCaseExecutionModel } from '../../models/test-case-execution-model';
import { BuildModel } from '../../models/build-model';
import { TLTestCaseModel } from '../../models/test-case-model';
import { AfterViewChecked, OnChanges, SimpleChanges } from '@angular/core/src/metadata/lifecycle_hooks';
import { Observable } from 'rxjs/Observable';
import { Subject } from 'rxjs/Subject';
import { IExternalExecution } from '../../../elastest-etm/external/models/external-execution-interface';
import { ExternalTJobExecModel } from '../../../elastest-etm/external/external-tjob-execution/external-tjob-execution-model';
import { ExternalTestExecutionModel } from '../../../elastest-etm/external/external-test-execution/external-test-execution-model';
import { IExternalExecutionSaveModel } from '../../../elastest-etm/external/models/external-execution-save.model';
ElementRef;
@Component({
  selector: 'testlink-execution-form',
  templateUrl: './execution-form.component.html',
  styleUrls: ['./execution-form.component.scss'],
})
export class ExecutionFormComponent implements OnInit, OnChanges, AfterViewChecked, IExternalExecution {
  @Input() data: any;

  @ViewChild('notes') notes: ElementRef;
  alreadyFocused: boolean = false;

  // TestCaseSteps Data
  testCaseStepsColumns: any[] = [
    { name: 'id', label: 'Id' },
    { name: 'actions', label: 'Actions' },
    { name: 'expectedResults', label: 'Expected Results' },
    { name: 'executionType', label: 'Exec Type' },
  ];

  testCase: TLTestCaseModel;
  build: BuildModel;

  tcExec: TestCaseExecutionModel;

  constructor(private testLinkService: TestLinkService) {}

  ngOnChanges(changes: SimpleChanges) {
    if (changes.data) {
      this.alreadyFocused = false;
      this.ngOnInit();
    }
  }

  ngOnInit() {
    this.tcExec = new TestCaseExecutionModel();
    this.testCase = this.data.testCase;
    this.build = this.data.build;

    this.tcExec.testCaseVersionId = this.testCase.versionId;
    this.tcExec.testCaseVersionNumber = this.testCase.version;
    this.tcExec.executionType = this.testCase.executionType;
    this.tcExec.testPlanId = this.build.testPlanId;
    this.tcExec.buildId = this.build.id;
  }

  ngAfterViewChecked() {
    if (!this.alreadyFocused) {
      this.notes.nativeElement.focus();
      this.alreadyFocused = true;
    }
  }

  saveExecution(): Observable<IExternalExecutionSaveModel> {
    let _obs: Subject<IExternalExecutionSaveModel> = new Subject<IExternalExecutionSaveModel>();
    let obs: Observable<IExternalExecutionSaveModel> = _obs.asObservable();
    let oldNotes: string = this.tcExec.notes;
    if (this.data.additionalNotes) {
      this.tcExec.notes = this.tcExec.notes ? this.tcExec.notes + this.data.additionalNotes : this.data.additionalNotes;
    }
    this.testLinkService.saveExecution(this.tcExec, this.testCase.id).subscribe(
      (savedExecution: TestCaseExecutionModel) => {
        let savedResponse: IExternalExecutionSaveModel = new IExternalExecutionSaveModel();
        savedResponse.saved = true;
        savedResponse.response = savedExecution;
        _obs.next(savedResponse);
      },
      (error) => {
        this.tcExec.notes = oldNotes;
        _obs.error(error);
      },
    );
    return obs;
  }

  public isValidForm(): boolean {
    return this.tcExec.status !== undefined && this.tcExec.status !== null;
  }
}
