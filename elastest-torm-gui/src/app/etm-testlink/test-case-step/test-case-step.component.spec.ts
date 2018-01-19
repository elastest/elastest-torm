import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TestCaseStepComponent } from './test-case-step.component';

describe('TestCaseStepComponent', () => {
  let component: TestCaseStepComponent;
  let fixture: ComponentFixture<TestCaseStepComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TestCaseStepComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TestCaseStepComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
