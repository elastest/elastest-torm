import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ExecutionModalComponent } from './execution-modal.component';

describe('ExecutionModalComponent', () => {
  let component: ExecutionModalComponent;
  let fixture: ComponentFixture<ExecutionModalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ExecutionModalComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ExecutionModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
