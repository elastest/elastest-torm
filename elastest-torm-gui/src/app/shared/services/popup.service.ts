import { Injectable } from '@angular/core';
import { MatSnackBar, SimpleSnackBar, MatSnackBarRef, MatSnackBarConfig } from '@angular/material';

@Injectable()
export class PopupService {
  constructor(private snackBar: MatSnackBar) {}

  openSnackBar(message: string, action: string = 'OK', duration?: number, cssClasses?: any[]): MatSnackBarRef<SimpleSnackBar> {
    let config: MatSnackBarConfig = new MatSnackBarConfig<any>();
    config.duration = duration ? duration : 3700;
    config.panelClass = cssClasses ? cssClasses : [];
    return this.openSnackBarByConfig(message, action, config);
  }

  openSnackBarByConfig(message: string, action: string = 'OK', config: MatSnackBarConfig): MatSnackBarRef<SimpleSnackBar> {
    return this.snackBar.open(message, action, config);
  }
}
