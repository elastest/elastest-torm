import { ParameterModel } from '../../elastest-etm/parameter/parameter-model';
import { Component, Input, OnInit } from '@angular/core';

@Component({
  selector: 'parameters-view',
  templateUrl: './parameters-view.component.html',
  styleUrls: ['./parameters-view.component.scss']
})
export class ParametersViewComponent implements OnInit {
  @Input()
  public model: ParameterModel[];

  constructor() { }

  ngOnInit() {
  }

  addParameter() {
    this.model.push(new ParameterModel());
  }

  deleteParameter(position: number) {
    this.model.splice(position, 1);
  }
}
