import { Component, OnInit, Input } from '@angular/core';

@Component({
  selector: 'string-list-view',
  templateUrl: './string-list-view.component.html',
  styleUrls: ['./string-list-view.component.scss'],
})
export class StringListViewComponent implements OnInit {
  @Input() model: string[];
  @Input() fieldName: string;

  constructor() {}

  ngOnInit() {
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
}
