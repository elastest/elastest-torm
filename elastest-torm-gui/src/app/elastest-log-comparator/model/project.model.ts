export class Project {
  id: number;

  assigned_ids: number[];
  name: string;
  num_execs: number;

  constructor() {
    this.assigned_ids = [];
    this.name = '';
    this.num_execs = 0;
  }

  body() {
    const object = {
      id: this.id,
      name: this.name,
      'num_execs': this.num_execs
    };
    return JSON.stringify(object);
  }
}
