import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TestPlanExecutionComponent } from './test-plan-execution.component';

describe('TestPlanExecutionComponent', () => {
  let component: TestPlanExecutionComponent;
  let fixture: ComponentFixture<TestPlanExecutionComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TestPlanExecutionComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TestPlanExecutionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
