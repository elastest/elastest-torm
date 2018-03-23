import { Injectable } from '@angular/core';
import { MdSnackBar, SimpleSnackBar, MdSnackBarRef } from '@angular/material';

@Injectable()
export class PopupService {
  constructor(private snackBar: MdSnackBar) {}

  openSnackBar(message: string, action: string = 'OK', duration?: number, extraClasses?: any[]): MdSnackBarRef<SimpleSnackBar> {
    return this.snackBar.open(message, action, {
      duration: duration ? duration : 3700,
      extraClasses: extraClasses ? extraClasses : [],
    });
  }
}
