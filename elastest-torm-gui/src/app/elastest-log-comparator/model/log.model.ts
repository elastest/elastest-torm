export interface Log {
  id: string;
  level: string;
  log: string;
  logger: string;
  message: string;
  method: string;
  thread: string;
  test?: string;
  timestamp: any;
  type?: any;
}
