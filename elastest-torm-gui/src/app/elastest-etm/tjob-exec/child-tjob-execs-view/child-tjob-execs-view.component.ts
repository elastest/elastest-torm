import { Router } from '@angular/router';
import { ETModelsTransformServices } from '../../../shared/services/et-models-transform.service';
import { TJobExecModel } from '../tjobExec-model';
import { Component, Input, OnInit, OnDestroy } from '@angular/core';
import { Subscription, Observable, interval } from 'rxjs';
import { TJobExecService } from '../tjobExec.service';

@Component({
  selector: 'etm-child-tjob-execs-view',
  templateUrl: './child-tjob-execs-view.component.html',
  styleUrls: ['./child-tjob-execs-view.component.scss'],
})
export class ChildTjobExecsViewComponent implements OnInit, OnDestroy {
  @Input()
  model: TJobExecModel;

  @Input()
  withReloadSubscription: boolean = false;

  childrens: TJobExecModel[] = [];
  checkChildsSubscription: Subscription;

  // TJob Exec Data
  tJobExecColumns: any[] = [
    { name: 'id', label: 'Id', width: 80 },
    { name: 'result', label: 'Result' },
    { name: 'parameters', label: 'Configurations' },
    { name: 'duration', label: 'Duration(sec)' },
    { name: 'startDate', label: 'Start Date' },
    { name: 'endDate', label: 'End Date' },
    { name: 'sutExecution', label: 'Sut Execution' },
    { name: 'options', label: 'Options' },
  ];

  constructor(
    private eTModelsTransformServices: ETModelsTransformServices,
    private router: Router,
    private tJobExecService: TJobExecService,
  ) {}

  ngOnInit(): void {
    // override the route reuse strategy
    this.router.routeReuseStrategy.shouldReuseRoute = function() {
      return false;
    };

    if (this.model && this.model.execChilds) {
      this.childrens = this.eTModelsTransformServices.jsonToTJobExecsList(this.model.execChilds);
      if (this.withReloadSubscription) {
        this.subscribeToChilds();
      }
    }
  }

  ngOnDestroy(): void {
    this.unsubscribeReloadChilds();
  }

  unsubscribeReloadChilds(): void {
    if (this.checkChildsSubscription !== undefined) {
      this.checkChildsSubscription.unsubscribe();
      this.checkChildsSubscription = undefined;
    }
  }

  subscribeToChilds(): void {
    let timer: Observable<number> = interval(1800);
    if (this.checkChildsSubscription === null || this.checkChildsSubscription === undefined) {
      this.checkChildsSubscription = timer.subscribe(() => {
        this.tJobExecService.getParentTJobExecChilds(this.model.id).subscribe(
          (childs: TJobExecModel[]) => {
            this.model.execChilds = childs;
            this.childrens = childs;
          },
          (error: Error) => {
            console.log(error);
          },
        );
      });
    }
  }

  viewTJobExec(tJobExec: TJobExecModel): void {
    this.router.navigate(['/projects', tJobExec.tJob.project.id, 'tjob', tJobExec.tJob.id, 'tjob-exec', tJobExec.id]);
  }

  viewInLogAnalyzer(tJobExec: TJobExecModel): void {
    this.router.navigate(['/loganalyzer'], { queryParams: { tjob: tJobExec.tJob.id, exec: tJobExec.id } });
  }
}
