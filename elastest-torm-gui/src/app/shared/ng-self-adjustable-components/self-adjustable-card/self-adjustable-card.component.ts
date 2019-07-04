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
  private contentPaddingPixels: number = undefined;

  @Input()
  private noTitleBackground: boolean = false;

  @Input()
  private noContentBackground: boolean = false;

  cardStyle: any = {};
  titleStyle: any = {};
  contentStyle: any = {};

  @Input()
  hideTitle: boolean = false;

  constructor() {}

  ngOnInit(): void {
    this.updateStyles();
  }

  updateStyles(): void {
    if (this.contentPaddingPixels !== undefined) {
      this.contentStyle['padding'] = this.contentPaddingPixels + 'px';
    }

    if (this.noContentBackground) {
      this.cardStyle['background'] = 'none';
      this.cardStyle['box-shadow'] = 'none';

      if (!this.noTitleBackground) {
        this.titleStyle['background'] = '#ffffff';
        this.titleStyle['box-shadow'] =
          '0px 2px 1px -1px rgba(0, 0, 0, 0.2), 0px 1px 1px 0px rgba(0, 0, 0, 0.14), 0px 1px 3px 0px rgba(0, 0, 0, 0.12)';
      }
    }
  }

  setNoContentBackground(noContentBackground: boolean): void {
    this.noContentBackground = noContentBackground;
    this.updateStyles();
  }

  setNoTitleBackground(noTitleBackground: boolean): void {
    this.noTitleBackground = noTitleBackground;
    this.updateStyles();
  }
}
