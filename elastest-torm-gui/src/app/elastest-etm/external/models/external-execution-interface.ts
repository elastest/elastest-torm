import { Observable } from 'rxjs/Observable';
import { IExternalExecutionSaveModel } from './external-execution-save.model';

export interface IExternalExecution {
  saveExecution(): Observable<IExternalExecutionSaveModel>;
}