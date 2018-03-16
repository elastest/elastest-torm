import { ConfigurationService } from '../../config/configuration-service.service';
import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-emp-container',
  templateUrl: './emp-container.component.html',
  styleUrls: ['./emp-container.component.scss']
})
export class EmpContainerComponent implements OnInit {

  serviceUrl: string = "http://www.elmundo.es";
  
  constructor(private configService: ConfigurationService) { }

  ngOnInit() {
    this.serviceUrl = this.configService.configModel.empGrafanaUrl;
  }

}
