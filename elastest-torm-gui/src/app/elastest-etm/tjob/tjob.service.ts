import { StompWSManager } from '../stomp-ws-manager.service';
import { ETM_API } from '../../../config/api.config';
import { Http } from '@angular/http';
import { Injectable } from '@angular/core';
import 'rxjs/Rx';

@Injectable()
export class TJobService {
  constructor(private http: Http, private stompWSManager: StompWSManager) { }

  public getTJobs(){

  }

  public getTJob(){

  }

  public createTJob(){

  }

  public modifyTJob(){
    
  }

  public deleteTJob(){

  }

  public runTJob(tJobId: number){

    let url = ETM_API + '/tjob/' + tJobId + '/exec' ;
    return this.http.post(url, {})
      .map( response => response.json())
  }

  public getTJobsExecutions(){

  }

  public getTJobExecution(idTJobExecution: number){

  }

  public deleteTJobExecution(idTJobExecution: number){

  }

private subscribeQueues(tjobExec: any){
  
}
  

}
