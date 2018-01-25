import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ExecutionViewComponent } from './execution-view.component';

describe('ExecutionViewComponent', () => {
  let component: ExecutionViewComponent;
  let fixture: ComponentFixture<ExecutionViewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ExecutionViewComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ExecutionViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
