import { ConfigurationService } from '../../config/configuration-service.service';
import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-edm-container',
  templateUrl: './edm-container.component.html',
  styleUrls: ['./edm-container.component.scss'],
})
export class EdmContainerComponent implements OnInit {
  serviceUrl: string = '';

  constructor(private configService: ConfigurationService) {}

  ngOnInit(): void {
    this.serviceUrl = this.configService.configModel.edmCommandUrl;
  }
}
