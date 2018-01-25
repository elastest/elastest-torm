import { Observable } from 'rxjs/Observable';

export interface IExternalExecution {
    saveExecution(): Observable<boolean>;
}