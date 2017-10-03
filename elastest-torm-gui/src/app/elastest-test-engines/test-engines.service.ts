import { ConfigurationService } from '../config/configuration-service.service';
import { Http, RequestOptions } from '@angular/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Rx';

@Injectable()
export class TestEnginesService {
    constructor(private http: Http, private configurationService: ConfigurationService,
    ) { }

    getTestEngines() {
        let url: string = this.configurationService.configModel.hostApi + '/engines/';
        return this.http.get(url)
            .map((response) => response.json());
    }
}