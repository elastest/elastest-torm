import { Injectable } from '@angular/core';
import { MdSnackBar } from '@angular/material';

@Injectable()
export class PopupService {
    constructor(private snackBar: MdSnackBar,
    ) { }

    openSnackBar(message: string, action: string, duration?: number, extraClasses?: any[]) {
        return this.snackBar.open(message, action, {
            duration: duration ? duration : 3500,
            extraClasses: extraClasses ? extraClasses : []
        });
    }
}