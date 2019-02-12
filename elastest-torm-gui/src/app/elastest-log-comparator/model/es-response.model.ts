import {Log} from './log.model';

export interface ESResponse {
  _index: any;
  _type: string;
  _id: string;
  _score: any;
  _source: Log;
  sort: any[]
}
