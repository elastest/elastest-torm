export class MonitorMarkModel {
  id: string;
  value: string;
  timestamp: Date;
  timestampStr: string;
  constructor() {
    this.id = '';
    this.value = '';
    this.timestamp = undefined;
    this.timestampStr = '';
  }

  initByGivenMsg(msg: string, timestampStr?: string): void {
    let etMonitorMarkPrefix: string = '##elastest-monitor-mark: ';

    if (msg) {
      msg = msg.replace(etMonitorMarkPrefix, '');
      let splittedMsg: string[] = msg.split(',');
      if (splittedMsg.length === 2) {
        this.id = this.getValueFromPair(splittedMsg[0]);
        this.value = this.getValueFromPair(splittedMsg[1]);
        if (timestampStr && timestampStr !== '') {
          this.timestampStr = timestampStr;
          try {
            this.timestamp = new Date(timestampStr);
          } catch (e) {
            console.log('Error on create Date from timestampStr in MonitorMarkModel:', e);
          }
        }
      }
    }
  }

  getValueFromPair(pair: string): string {
    return pair.split('=')[1];
  }

  isEmpty(): boolean {
    return this.id === '' || this.value === '';
  }
}
