import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RunTJobModalComponent } from './run-tjob-modal.component';

describe('RunTJobModalComponent', () => {
  let component: RunTJobModalComponent;
  let fixture: ComponentFixture<RunTJobModalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RunTJobModalComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RunTJobModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
