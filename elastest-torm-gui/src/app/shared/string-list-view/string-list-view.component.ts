import { Component, OnInit, Input } from '@angular/core';

@Component({
  selector: 'string-list-view',
  templateUrl: './string-list-view.component.html',
  styleUrls: ['./string-list-view.component.scss'],
})
export class StringListViewComponent implements OnInit {
  @Input()
  model: string[];
  @Input()
  fieldName: string;
  @Input()
  description: StringListViewDescription;

  fieldNameAsPrefix: string = 'fieldName';

  constructor() {}

  ngOnInit(): void {
    if (this.fieldName !== undefined) {
      this.fieldNameAsPrefix = this.fieldName.replace(/\s/g, '');
    }
    if (this.model.length === 0) {
      this.addField();
    }
  }

  addField(): void {
    this.model.push('');
  }

  deleteField(position: number): void {
    this.model.splice(position, 1);
  }

  trackByFn(index: any, item: any): any {
    return index;
  }
}

export class StringListViewDescription {
  label: string;
  sublabel: string;
  constructor(label?: string, sublabel?: string) {
    this.label = label;
    this.sublabel = sublabel;
  }
}
