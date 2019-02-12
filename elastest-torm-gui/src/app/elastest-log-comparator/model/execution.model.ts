import {TestCase} from './test-case.model';

export interface Execution {
  id: number;
  entries: number;
  errors: number;
  failures: number;
  flakes: number;
  project: string;
  skipped: number;
  start_date: string;
  status: string;
  tests: number;
  testcases: TestCase[];
  time_elapsed: number;
}
