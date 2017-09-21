import { EsmService } from '../../esm-service.service';
import { EsmServiceInstanceModel } from '../../esm-service-instance.model';
import { ActivatedRoute, Params } from '@angular/router';
import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'esm-service-detail',
  templateUrl: './service-detail.component.html',
  styleUrls: ['./service-detail.component.scss']
})
export class ServiceDetailComponent implements OnInit {

  serviceInstance: EsmServiceInstanceModel;

  constructor(private route: ActivatedRoute, private esmService: EsmService) {
    this.serviceInstance = new EsmServiceInstanceModel(undefined);
   }

  ngOnInit() {

    if (this.route.params !== null || this.route.params !== undefined){
      this.route.params.switchMap((params: Params) => this.esmService.getSupportServiceInstance(params['id']))
      .subscribe ((serviceInstance: EsmServiceInstanceModel) => {
        this.serviceInstance = serviceInstance;
        console.log("Service instance: " + JSON.stringify(this.serviceInstance));
      }); 
    }
  }

}
