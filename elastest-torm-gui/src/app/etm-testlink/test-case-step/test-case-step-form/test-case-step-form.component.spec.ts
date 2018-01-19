import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TestCaseStepFormComponent } from './test-case-step-form.component';

describe('TestCaseStepFormComponent', () => {
  let component: TestCaseStepFormComponent;
  let fixture: ComponentFixture<TestCaseStepFormComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TestCaseStepFormComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TestCaseStepFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
