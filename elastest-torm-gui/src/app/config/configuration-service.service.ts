import { Http } from '@angular/http';
import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { ConfigModel } from './config-model';
import { ETM_API } from '../../config/api.config';
import { Observable } from 'rxjs/Rx';

@Injectable()
export class ConfigurationService {

    public configModel: ConfigModel;

    constructor(private http: Http, ) {
    }

    load() {
        console.log("Starting configuration.");
        let host: string = window.location.host;
        let hostApi: string = 'http://' + host + '/api';

        return new Promise((resolve, reject) => {
            this.getServicesInfo(hostApi)
                .subscribe((servicesInfo) => {
                    let eusUrl = servicesInfo.elasTestExecMode === 'normal' && servicesInfo.eusSSInstance !== null
                    ? new URL(servicesInfo.eusSSInstance.urls.api) : null;
                    this.configModel = {
                        'hostName': window.location.hostname,
                        'host': 'http://' + host,
                        'hostApi': hostApi,
                        'hostElasticsearch': servicesInfo.elasticSearchUrl + '/',
                        'hostEIM': 'http://' + environment.hostEIM + '/',
                        'hostWsServer': 'ws://' + host + servicesInfo.rabbitPath,
                        'eusHost': eusUrl !== null ? eusUrl.hostname : null,
                        'eusPort': eusUrl !== null ? eusUrl.port : null,
                        'eusServiceUrlNoPath': 'http://' + environment.eus,
                        'eusServiceUrl': servicesInfo.elasTestExecMode === 'normal' && servicesInfo.eusSSInstance !== null
                            ? servicesInfo.eusSSInstance.urls.api : null,
                        'eusWebSocketUrl': servicesInfo.elasTestExecMode === 'normal' && servicesInfo.eusSSInstance !== null
                            ? servicesInfo.eusSSInstance.urls.eusWSapi : null,
                        'elasTestExecMode': servicesInfo.elasTestExecMode,
                        'testLinkStarted': servicesInfo.testLinkStarted,
                    };

                    resolve();
                    console.log("The configuration is completed.");
                },
            );
        });
    }

    public getServicesInfo(hostApi: string) {
        let url: string = hostApi + '/context/services/info';
        return this.http.get(url)
            .map((response) => response.json());
    }

    public getElasticsearchApi(hostApi: string) {
        let url: string = hostApi + '/context/elasticsearch/api';
        return this.http.get(url)
            .map((response) => response['_body']);
    }

    public getWSHost(hostApi: string) {
        let url: string = hostApi + '/context/ws-host';
        return this.http.get(url)
            .map((response) => response['_body']);
    }

    public getLogstashIp() {
        let hostApi: string = this.configModel.hostApi;
        let url: string = hostApi + '/context/logstash/ip';
        return this.http.get(url)
            .map((response) => response['_body']);
    }

    public getLogstashInfo() {
        let hostApi: string = this.configModel.hostApi;
        let url: string = hostApi + '/context/logstash/info';
        return this.http.get(url)
            .map((response) => response.json());
    }

    public getHelpInfo() {
        let url: string = this.configModel.hostApi + '/context/help/info';
        return this.http.get(url)
        .map((response) => response.json());
    }

}
