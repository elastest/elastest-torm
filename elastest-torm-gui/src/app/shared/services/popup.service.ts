import { Injectable } from '@angular/core';
import { MdSnackBar } from '@angular/material';

@Injectable()
export class PopupService {
    constructor(private snackBar: MdSnackBar,
    ) { }

    openSnackBar(message: string, action: string) {
        return this.snackBar.open(message, action, {
            duration: 3500,
        });
    }
}