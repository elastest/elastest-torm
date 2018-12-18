export type fxLayoutAlignHorizontal =
  | ''
  | 'start'
  | 'center'
  | 'end'
  | 'space-around'
  | 'space-between'
  | 'space-evenly'
  | 'none';
export type fxLayoutAlignVertical =
  | ''
  | 'none'
  | 'start'
  | 'center'
  | 'end'
  | 'space-around'
  | 'space-between'
  | 'space-evenly'
  | 'stretch';

export class FxLayoutAlignModel {
  horizontal: fxLayoutAlignHorizontal;
  vertical: fxLayoutAlignVertical;
  constructor() {
    this.horizontal = '';
    this.vertical = '';
  }
}
