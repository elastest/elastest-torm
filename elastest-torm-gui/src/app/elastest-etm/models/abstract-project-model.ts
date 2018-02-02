import { SutModel } from '../sut/sut-model';
import { AbstractTJobModel } from './abstract-tjob-model';

export class AbstractProjectModel {
  id: number;
  name: string;
  suts: SutModel[];
  tjobs: AbstractTJobModel[];
  constructor() {
    this.id = 0;
    this.name = '';
    this.suts = [];
    this.tjobs = [];
  }
}
