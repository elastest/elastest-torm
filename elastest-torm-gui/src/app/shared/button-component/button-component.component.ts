import { Component, OnInit, Input } from '@angular/core';
import { ButtonModel } from './button.model';

@Component({
  selector: 'button-component',
  templateUrl: './button-component.component.html',
  styleUrls: ['./button-component.component.scss'],
})
export class ButtonComponentComponent implements OnInit {
  @Input()
  model: ButtonModel;

  constructor() {}

  ngOnInit() {}
}
