import { Injectable } from '@angular/core';
import { Http, Response } from '@angular/http';
import { ConfigurationService } from '../../config/configuration-service.service';
import { PopupService } from '../../shared/services/popup.service';

@Injectable()
export class ExternalService {
    hostApi: string;

    constructor(
        private http: Http, private configurationService: ConfigurationService,
        // public eTTestlinkModelsTransformService: ETTestlinkModelsTransformService,
        public popupService: PopupService,
    ) {
        this.hostApi = this.configurationService.configModel.hostApi;
    }
}