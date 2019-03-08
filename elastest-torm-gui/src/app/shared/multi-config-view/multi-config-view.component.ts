import { Component, Input, OnInit } from '@angular/core';

@Component({
  selector: 'multi-config-view',
  templateUrl: './multi-config-view.component.html',
  styleUrls: ['./multi-config-view.component.scss'],
})
export class MultiConfigViewComponent implements OnInit {
  @Input()
  public model: MultiConfigModel[];

  @Input()
  public btnText: string = 'MultiConfig Axis';

  @Input()
  public name: string = 'multi config';

  public updateInProgress: boolean = false;

  constructor() {}

  ngOnInit(): void {}

  addMultiConfig(multiConfig: MultiConfigModel = new MultiConfigModel()): void {
    this.model.push(multiConfig);
  }

  deleteMultiConfig(position: number): void {
    this.model.splice(position, 1);
  }

  addValueToMultiConfig(multiConfigModelPosition: number, value: string = ''): void {
    this.model[multiConfigModelPosition].configValues.push(value);
  }

  deleteValue(multiConfigModelPosition: number, valuePosition: number): void {
    this.model[multiConfigModelPosition].configValues.splice(valuePosition, 1);
    if (this.model[multiConfigModelPosition].configValues.length === 0) {
      this.addValueToMultiConfig(multiConfigModelPosition);
    }
  }

  updateModel(newModel: MultiConfigModel[]): void {
    this.updateInProgress = true;
    this.model = newModel;
    this.updateInProgress = false;
  }

  trackByFn(index: any, item: any): any {
    return index;
  }
}

export class MultiConfigModel {
  name: string;
  configValues: string[];

  constructor(multiConfigJson: any = undefined) {
    if (!multiConfigJson) {
      this.name = '';
      this.configValues = [''];
    } else {
      this.name = multiConfigJson.name;
      this.configValues = multiConfigJson.configValues;
    }
  }
}
