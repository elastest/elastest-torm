import { MatDialogRef } from '@angular/material';
import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material';
import { ConfigurationService } from '../../config/configuration-service.service';

@Component({
  selector: 'app-credentials-dialog',
  templateUrl: './credentials-dialog.component.html',
  styleUrls: ['./credentials-dialog.component.scss'],
})
export class CredentialsDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<CredentialsDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    public configService: ConfigurationService,
  ) {}

  onCloseConfirm(): void {
    this.dialogRef.close('Confirm');
  }
}
