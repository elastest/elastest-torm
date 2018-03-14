import { TitlesService } from '../../../shared/services/titles.service';
import { TJobExecModel } from '../../tjob-exec/tjobExec-model';
import { TJobExecService } from '../../tjob-exec/tjobExec.service';
import { TJobService } from '../tjob.service';
import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'etm-tjobs-manager',
  templateUrl: './tjobs-manager.component.html',
  styleUrls: ['./tjobs-manager.component.scss']
})
export class TJobsManagerComponent implements OnInit {

  constructor(
    private titlesService: TitlesService, private tJobService: TJobService, private tJobExecService: TJobExecService,
  ) { }

  ngOnInit() {
    this.titlesService.setHeadTitle('TJobs');
  }


}
