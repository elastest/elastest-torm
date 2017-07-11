import { TJobService } from '../tjob.service';
import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'etm-tjobs-manager',
  templateUrl: './tjobs-manager.component.html',
  styleUrls: ['./tjobs-manager.component.scss']
})
export class TJobsManagerComponent implements OnInit {

  idTJob: number;

  constructor(private tJobService: TJobService) { }

  ngOnInit() {
  }


}
