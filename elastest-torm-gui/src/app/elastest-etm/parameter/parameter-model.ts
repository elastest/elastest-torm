export class ParameterModel {
  id: number;
  name: string;
  value: string;
  constructor(parameterJson: any = undefined) {
    this.id = 0;
    if (!parameterJson) {
      this.name = '';
      this.value = '';
    } else {
      this.name = parameterJson.name;
      this.value = parameterJson.value;
    }
  }
}
