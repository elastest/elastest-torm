import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TestSuiteFormComponent } from './test-suite-form.component';

describe('TestSuiteFormComponent', () => {
  let component: TestSuiteFormComponent;
  let fixture: ComponentFixture<TestSuiteFormComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TestSuiteFormComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TestSuiteFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
