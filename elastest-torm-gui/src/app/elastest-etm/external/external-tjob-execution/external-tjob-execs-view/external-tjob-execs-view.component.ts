import { Component, OnInit, Input } from '@angular/core';
import { ExternalService } from '../../external.service';
import { ExternalTJobExecModel } from '../external-tjob-execution-model';

@Component({
  selector: 'etm-external-tjob-execs-view',
  templateUrl: './external-tjob-execs-view.component.html',
  styleUrls: ['./external-tjob-execs-view.component.scss'],
})
export class ExternalTjobExecsViewComponent implements OnInit {
  @Input() exTJobId: number | string;
  exTJobExecs: ExternalTJobExecModel[] = [];

  execsColumns: any[] = [
    { name: 'id', label: 'Id' },
    { name: 'exTJob.id', label: 'External TJob Id' },
    { name: 'result', label: 'Result' },
    // { name: 'options', label: 'Options' },
  ];

  constructor(private externalService: ExternalService) {}

  ngOnInit() {
    this.externalService.getExternalTJobExecsByExternalTJobId(this.exTJobId).subscribe(
      (exTJobExecs: ExternalTJobExecModel[]) => {
        this.exTJobExecs = exTJobExecs;
      },
      (error) => console.log(error),
    );
  }
}
