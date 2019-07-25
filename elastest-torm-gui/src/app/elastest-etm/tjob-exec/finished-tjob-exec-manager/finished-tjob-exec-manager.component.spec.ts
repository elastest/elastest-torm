import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { FinishedTjobExecManagerComponent } from './finished-tjob-exec-manager.component';

describe('FinishedTjobExecManagerComponent', () => {
  let component: FinishedTjobExecManagerComponent;
  let fixture: ComponentFixture<FinishedTjobExecManagerComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [FinishedTjobExecManagerComponent],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FinishedTjobExecManagerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
