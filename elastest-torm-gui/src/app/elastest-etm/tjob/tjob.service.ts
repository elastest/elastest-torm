import { ETM_API } from '../../../config/api.config';
import { Http } from '@angular/http';
import { Injectable } from '@angular/core';
import 'rxjs/Rx';

@Injectable()
export class TJobService {
  constructor(private http: Http) { }

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

  public runTJob(){

    let url = ETM_API + '/tjob/1/exec' ;
    return this.http.post(url, {}) 
      .map( response => console.log(response.json()))
  }

  public getTJobsExecutions(){

  }

  public getTJobExecution(idTJobExecution: number){

  }

  public deleteTJobExecution(idTJobExecution: number){

  }


  

}
