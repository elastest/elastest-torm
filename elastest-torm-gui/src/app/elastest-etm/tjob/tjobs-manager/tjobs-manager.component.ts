import { TJobService } from '../tjob.service';
import { StompWSManager } from '../../stomp-ws-manager.service';
import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'etm-tjobs-manager',
  templateUrl: './tjobs-manager.component.html',
  styleUrls: ['./tjobs-manager.component.scss']
})
export class TJobsManagerComponent implements OnInit {

  idTJob: number;

  constructor(private stompWSManager: StompWSManager, private tJobService: TJobService) { }

  ngOnInit() {
  }

  public runTJob(){    
        
    this.tJobService.runTJob(this.idTJob)
      .subscribe(
        tjobExecution => {
          console.log('TJobExecutionId:'+ tjobExecution.id);
          this.createAndSubscribe(tjobExecution);
        },
        error => console.error("Error:" + error)
      );
  }

public createAndSubscribe(tjobExecution: any){
  this.stompWSManager.subscribeWSDestination('q-'+tjobExecution.id + '-test-metrics');
  
}

}
