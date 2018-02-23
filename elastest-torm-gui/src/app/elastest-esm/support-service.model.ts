export class SupportServiceConfigModel {
  name: string;
  type: string;
  label: string;
  default: string;
  value: any;

  constructor() {
    this.name = '';
    this.type = '';
    this.label = '';
    this.default = '';
    this.value = undefined;
  }
}
