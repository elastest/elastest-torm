import {ESResponse} from './es-response.model';

export interface RD {
  took: number;
  timed_out: boolean;
  _shards: Object
  hits: AW
}

export interface AW {
  total: number;
  max_score: any;
  hits: ESResponse[]
}
