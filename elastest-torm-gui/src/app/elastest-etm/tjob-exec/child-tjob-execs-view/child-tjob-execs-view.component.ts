import { Router } from '@angular/router';
import { ETModelsTransformServices } from '../../../shared/services/et-models-transform.service';
import { TJobExecModel } from '../tjobExec-model';
import { Component, Input, OnInit } from '@angular/core';

@Component({
  selector: 'etm-child-tjob-execs-view',
  templateUrl: './child-tjob-execs-view.component.html',
  styleUrls: ['./child-tjob-execs-view.component.scss'],
})
export class ChildTjobExecsViewComponent implements OnInit {
  @Input()
  model: TJobExecModel;
  childrens: TJobExecModel[] = [];

  // TJob Exec Data
  tJobExecColumns: any[] = [
    { name: 'id', label: 'Id' },
    { name: 'result', label: 'Result' },
    { name: 'startDate', label: 'Start Date' },
    { name: 'endDate', label: 'End Date' },
    { name: 'lastExecutionDate', label: 'Last Execution' },
    { name: 'sutExecution', label: 'Sut Execution' },
    { name: 'options', label: 'Options' },
  ];

  constructor(private eTModelsTransformServices: ETModelsTransformServices, private router: Router) {}

  ngOnInit() {
    if (this.model && this.model.execChilds) {
      this.childrens = this.eTModelsTransformServices.jsonToTJobExecsList(this.model.execChilds);
    }
  }
  viewTJobExec(tJobExec: TJobExecModel): void {
    this.router.navigate(['/projects', tJobExec.tJob.project.id, 'tjob', tJobExec.tJob.id, 'tjob-exec', tJobExec.id]);
  }
}
