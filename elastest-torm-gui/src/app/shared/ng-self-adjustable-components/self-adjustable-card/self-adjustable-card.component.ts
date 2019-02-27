// EduJG
import { fxLayoutAlignHorizontal, fxLayoutAlignVertical } from '../models/fx-layout-align.model';
import { Component, OnInit, Input } from '@angular/core';

@Component({
  selector: 'self-adjustable-card',
  templateUrl: './self-adjustable-card.component.html',
  styleUrls: ['./self-adjustable-card.component.scss'],
})
export class SelfAdjustableCardComponent implements OnInit {
  @Input()
  private titleAlignHorizontal: fxLayoutAlignHorizontal | null = 'none';
  @Input()
  private titleAlignVertical: fxLayoutAlignVertical | null = 'none';

  @Input()
  private contentAlignHorizontal: fxLayoutAlignHorizontal | null = 'none';
  @Input()
  private contentAlignVertical: fxLayoutAlignVertical | null = 'none';

  @Input()
  private hideTitle: boolean = false;

  constructor() {}

  ngOnInit() {}
}
