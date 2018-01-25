import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ExecutionFormComponent } from './execution-form.component';

describe('ExecutionFormComponent', () => {
  let component: ExecutionFormComponent;
  let fixture: ComponentFixture<ExecutionFormComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ExecutionFormComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ExecutionFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
