export class ButtonModel {
  name: string;
  icon: string;
  hideIcon: boolean;
  showOnlyIcon: boolean;
  color: 'accent' | 'primary';
  buttonType: 'button' | 'raised-button' | 'flat-button' | 'stroked-button' | 'icon-button' | 'fab' | 'mini-fab';
  clickMethod: Function;
  clickMethodTooltip: string;
  disabled: boolean;

  constructor() {
    this.name = undefined;
    this.icon = undefined;
    this.hideIcon = true;
    this.showOnlyIcon = false;
    this.color = 'primary';
    this.buttonType = 'button';
    this.clickMethod = undefined;
    this.clickMethodTooltip = '';
    this.disabled = false;
  }
}
