import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CredentialsDialogComponent } from './credentials-dialog.component';

describe('CredentialsDialogComponent', () => {
  let component: CredentialsDialogComponent;
  let fixture: ComponentFixture<CredentialsDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CredentialsDialogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CredentialsDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
