import { ParameterModel } from '../../elastest-etm/parameter/parameter-model';
import { Component, Input, OnInit } from '@angular/core';

@Component({
  selector: 'parameters-view',
  templateUrl: './parameters-view.component.html',
  styleUrls: ['./parameters-view.component.scss'],
})
export class ParametersViewComponent implements OnInit {
  @Input()
  public model: ParameterModel[];

  public updateInProgress: boolean = false;

  constructor() {}

  ngOnInit(): void {}

  addParameter(parameter: ParameterModel = new ParameterModel()): void {
    this.model.push(parameter);
  }

  deleteParameter(position: number): void {
    if (this.model && this.model.length > 0) {
      this.model.splice(position, 1);
    }
  }

  updateModel(newModel: ParameterModel[]): void {
    this.updateInProgress = true;
    this.model = newModel;
    this.updateInProgress = false;
  }
}
