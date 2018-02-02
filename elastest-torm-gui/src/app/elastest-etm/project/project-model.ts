import { SutModel } from '../sut/sut-model';
import { TJobModel } from '../tjob/tjob-model';
import { AbstractProjectModel } from '../models/abstract-project-model';

export class ProjectModel extends AbstractProjectModel {
  id: number;
  name: string;
  suts: SutModel[];
  tjobs: TJobModel[];
  constructor() {
    super();
    this.id = 0;
    this.name = '';
    this.suts = [];
    this.tjobs = [];
  }

  public getRouteString(): string {
    return this.name;
  }
}
